package top.aceofspades.oplog.core.parse;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import top.aceofspades.oplog.core.context.LogContext;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: duanbt
 * @create: 2021-11-17 10:15
 **/
class LogExpressionEvaluator extends CachedExpressionEvaluator {

    public static final String RESULT_VARIABLE = "_result";
    public static final Object RESULT_UNAVAILABLE = new Object();

    public static final String ERROR_MSG_VARIABLE = "_errorMsg";
    public static final String ERROR_MSG_UNAVAILABLE = "_UNAVAILABLE";



    private final Map<ExpressionKey, Expression> expressionCache = new ConcurrentHashMap<>(64);

    private final Map<AnnotatedElementKey, Method> targetMethodCache = new ConcurrentHashMap<>(64);

    public EvaluationContext createEvaluationContext(Method method, Object[] args, Class<?> targetClass,
                                                     Object result, String errorMsg, BeanFactory beanFactory) {
        Method targetMethod = getTargetMethod(targetClass, method);
        LogEvaluationContext evaluationContext = new LogEvaluationContext(null, targetMethod, args, getParameterNameDiscoverer());
        if (result == RESULT_UNAVAILABLE) {
            evaluationContext.addUnavailableVariable(RESULT_VARIABLE);
        } else {
            evaluationContext.setVariable(RESULT_VARIABLE, result);
        }
        if (Objects.equals(errorMsg, ERROR_MSG_UNAVAILABLE)) {
            evaluationContext.addUnavailableVariable(ERROR_MSG_VARIABLE);
        } else {
            evaluationContext.setVariable(ERROR_MSG_VARIABLE, errorMsg);
        }
        Map<String, Object> variables = LogContext.getVariables();
        if (!CollectionUtils.isEmpty(variables)) {
            evaluationContext.setVariables(variables);
        }
        if (beanFactory != null) {
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return evaluationContext;
    }

    private Method getTargetMethod(Class<?> targetClass, Method method) {
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
        Method targetMethod = this.targetMethodCache.get(methodKey);
        if (targetMethod == null) {
            targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
            this.targetMethodCache.put(methodKey, targetMethod);
        }
        return targetMethod;
    }

    @Nullable
    public Object parseExpression(String expression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return getExpression(this.expressionCache, methodKey, expression).getValue(evalContext);
    }

}
