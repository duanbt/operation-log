package top.aceofspades.oplog.core.aop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.aceofspades.oplog.core.beans.LogOperation;
import top.aceofspades.oplog.core.beans.LogRecord;
import top.aceofspades.oplog.core.context.LogContext;
import top.aceofspades.oplog.core.parse.LogValueParser;
import top.aceofspades.oplog.core.service.ILogService;
import top.aceofspades.oplog.core.service.IOperatorGetService;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author: duanbt
 * @create: 2021-11-12 11:26
 **/
@Slf4j
public class LogInterceptor implements MethodInterceptor, BeanFactoryAware, InitializingBean, Serializable {


    private LogOperationSource logOperationSource;

    private ILogService logService;

    private IOperatorGetService operatorGetService;

    private LogValueParser logValueParser;
    @Nullable
    private BeanFactory beanFactory;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return execute(invocation, invocation.getThis(), invocation.getMethod(), invocation.getArguments());
    }

    private Object execute(MethodInvocation invocation, Object target, Method method, Object[] args) throws Throwable {
        checkProperty();
        Class<?> targetClass = getTargetClass(target);
        LogContext.putEmptySpan();

        Collection<LogOperation> logOperations = Collections.emptyList();
        Map<String, Object> functionKeyAndReturnMap = Collections.emptyMap();
        try {
            logOperations = logOperationSource.getLogOperations(method, targetClass);
            if (logOperations != null) {
                List<String> spElTemplates = getBeforeFunctionExecuteTemplate(logOperations);
                functionKeyAndReturnMap = logValueParser.parseBeforeFunction(spElTemplates, targetClass, method, args, beanFactory);
            }
        } catch (Exception e) {
            log.error("log parse before method invoke exception", e);
        }

        MethodInvokeResult methodInvokeResult;
        Object ret = null;
        try {
            ret = invocation.proceed();
            methodInvokeResult = new MethodInvokeResult(true, null, null);
        } catch (Exception e) {
            methodInvokeResult = new MethodInvokeResult(false, e, e.getMessage());
        }

        try {
            if (!CollectionUtils.isEmpty(logOperations)) {
                recordLog(logOperations, targetClass, method, args, ret,
                        methodInvokeResult.isSuccess(),
                        methodInvokeResult.getErrorMsg(),
                        functionKeyAndReturnMap);
            }
        } catch (Exception e) {
            //记录日志出错不影响业务方法执行
            log.error("log parse or record exception", e);
        } finally {
            LogContext.clear();
        }
        if (methodInvokeResult.getThrowable() != null) {
            throw methodInvokeResult.getThrowable();
        }
        return ret;
    }

    private List<String> getSpElTemplates(LogOperation logOperation, String action) {
        List<String> spElTemplates = new ArrayList<>();
        if (StringUtils.hasText(logOperation.getOperatorId())) {
            spElTemplates.add(logOperation.getOperatorId());
        }
        if (StringUtils.hasText(logOperation.getCondition())) {
            spElTemplates.add(logOperation.getCondition());
        }
        spElTemplates.add(action);
        return spElTemplates;
    }


    private List<String> getBeforeFunctionExecuteTemplate(Collection<LogOperation> logOperations) {
        List<String> spElTemplates = new ArrayList<>();
        for (LogOperation operation : logOperations) {
            List<String> templates = getSpElTemplates(operation, operation.getSuccess());
            if (!CollectionUtils.isEmpty(templates)) {
                spElTemplates.addAll(templates);
            }
        }
        return spElTemplates;
    }

    private void recordLog(Collection<LogOperation> logOperations, Class<?> targetClass, Method method, Object[] args, Object ret, boolean success, String errorMsg, Map<String, Object> functionKeyAndReturnMap) {
        for (LogOperation operation : logOperations) {
            String actionTemplate = getActionTemplate(success, operation);
            if (!StringUtils.hasText(actionTemplate)) {
                //没有日志则忽略
                continue;
            }

            List<String> spElTemplates = getSpElTemplates(operation, actionTemplate);

            Map<String, String> expressionValues = logValueParser.parseTemplate(spElTemplates, targetClass,
                    method, args, ret, errorMsg, functionKeyAndReturnMap, beanFactory);
            if (logConditionPassed(operation.getCondition(), expressionValues)) {
                LogRecord logRecord = LogRecord.builder()
                        .operatorId(getRealOperatorId(operation.getOperatorId(), expressionValues))
                        .action(expressionValues.get(actionTemplate))
                        .success(success)
                        .createTime(new Date())
                        .build();

                logService.record(logRecord);
            }
        }

    }

    private String getRealOperatorId(String operatorId, Map<String, String> expressionValues) {
        String realOperatorId;
        if (!StringUtils.hasText(operatorId)) {
            realOperatorId = getOperatorIdFromService(operatorId);
        } else {
            realOperatorId = expressionValues.get(operatorId);
        }
        return realOperatorId;
    }

    private boolean logConditionPassed(String condition, Map<String, String> expressionValues) {
        return !StringUtils.hasText(condition) || "true".equalsIgnoreCase(expressionValues.get(condition));
    }


    private String getOperatorIdFromService(String operatorId) {
        operatorId = operatorGetService.getUser().getOperatorId();
        if (!StringUtils.hasText(operatorId)) {
            throw new IllegalStateException(String.format("get operatorId from %s : null", operatorGetService.getClass().getSimpleName()));
        }
        return operatorId;
    }

    private String getActionTemplate(boolean success, LogOperation operation) {
        String action;
        if (success) {
            action = operation.getSuccess();
        } else {
            action = operation.getFail();
        }
        return action;
    }

    private Class<?> getTargetClass(Object target) {
        return AopProxyUtils.ultimateTargetClass(target);
    }


    public void setLogOperationSource(@Nullable LogOperationSource logOperationSource) {
        this.logOperationSource = logOperationSource;
    }

    public void setLogService(@Nullable ILogService logService) {
        this.logService = logService;
    }

    public void setOperatorGetService(@Nullable IOperatorGetService operatorGetService) {
        this.operatorGetService = operatorGetService;
    }

    public void setLogValueParser(@Nullable LogValueParser logValueParser) {
        this.logValueParser = logValueParser;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        checkProperty();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private void checkProperty() {
        Assert.notNull(this.logOperationSource, "The 'logOperationSource' property is required");
        Assert.notNull(this.logService, "The 'logService' property is required");
        Assert.notNull(this.operatorGetService, "The 'operatorGetService' property is required");
        Assert.notNull(this.logValueParser, "The 'logValueParser' property is required");
    }


    @AllArgsConstructor
    @Getter
    static class MethodInvokeResult {
        private final boolean success;
        private final Throwable throwable;
        private final String errorMsg;
    }
}
