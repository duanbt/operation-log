package top.aceofspades.core.aop;

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
import top.aceofspades.core.beans.LogOperation;
import top.aceofspades.core.context.LogContext;
import top.aceofspades.core.service.ILogService;
import top.aceofspades.core.service.IOperatorGetService;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author: duanbt
 * @create: 2021-11-12 11:26
 **/
@Slf4j
public class LogInterceptor implements MethodInterceptor, BeanFactoryAware, InitializingBean, Serializable {

    @Nullable
    private BeanFactory beanFactory;
    @Nullable
    private LogOperationSource logOperationSource;
    @Nullable
    private ILogService logService;
    @Nullable
    private IOperatorGetService operatorGetService;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return execute(invocation, invocation.getThis(), invocation.getMethod(), invocation.getArguments());
    }

    private Object execute(MethodInvocation invocation, Object target, Method method, Object[] args) throws Throwable {
        Class<?> targetClass = getTargetClass(target);
        if (logOperationSource == null) {
            return invocation.proceed();
        }

        LogContext.putEmptySpan();

        Collection<LogOperation> logOperations = Collections.emptyList();
        Map<String, String> functionKeyAndReturnMap = Collections.emptyMap();
        try {
            logOperations = logOperationSource.getLogOperations(method, targetClass);
            if (logOperations != null) {
                functionKeyAndReturnMap = processBeforeFunctionTemplates(logOperations, targetClass, method, args);
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
                recordLog(ret, method, logOperations, targetClass,
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
        List<String> spElTemplates = new ArrayList<>(3);
        spElTemplates.add(logOperation.getOperator());
        spElTemplates.add(logOperation.getCondition());
        spElTemplates.add(action);
        return spElTemplates;
    }

    /**
     * 处理before function调用，获取函数返回值
     *
     * @return functionKey -> 返回值
     */
    private Map<String, String> processBeforeFunctionTemplates(Collection<LogOperation> logOperations, Class<?> targetClass, Method method, Object[] args) {
        List<String> spElTemplates = new ArrayList<>();
        for (LogOperation logOperation : logOperations) {
            List<String> templates = getSpElTemplates(logOperation, logOperation.getSuccess());
            if (!CollectionUtils.isEmpty(templates)) {
                spElTemplates.addAll(templates);
            }
        }

        for (String template : spElTemplates) {

        }

        return null;
    }

    private void recordLog(Object ret, Method method, Collection<LogOperation> logOperations, Class<?> targetClass, boolean success, String errorMsg, Map<String, String> functionNameAndReturnMap) {
        //TODO
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

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(this.logOperationSource != null, "The 'logOperationSource' property is required");
        Assert.state(this.logService != null, "The 'logService' property is required");
        Assert.state(this.operatorGetService != null, "The 'operatorGetService' property is required");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @AllArgsConstructor
    @Getter
    static class MethodInvokeResult {
        private final boolean success;
        private final Throwable throwable;
        private final String errorMsg;
    }
}
