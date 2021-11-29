package top.aceofspades.oplog.core.aop;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodClassKey;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import top.aceofspades.oplog.core.annotation.Log;
import top.aceofspades.oplog.core.beans.LogOperation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: duanbt
 * @create: 2021-11-11 16:49
 **/
public class LogOperationSource {

    private static final Collection<LogOperation> NULL_CACHING_ATTRIBUTE = Collections.emptyList();

    private final Map<MethodClassKey, Collection<LogOperation>> attributeCache = new ConcurrentHashMap<>(512);

    public boolean isCandidateClass(Class<?> targetClass) {
        return AnnotationUtils.isCandidateClass(targetClass, Log.class);
    }

    @Nullable
    public Collection<LogOperation> getLogOperations(Method method, Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }

        MethodClassKey cacheKey = getCacheKey(method, targetClass);
        Collection<LogOperation> cached = this.attributeCache.get(cacheKey);

        if (cached != null) {
            return (cached != NULL_CACHING_ATTRIBUTE ? cached : null);
        } else {
            Collection<LogOperation> logOps = computeLogOperations(method, targetClass);
            if (logOps != null) {
                this.attributeCache.put(cacheKey, logOps);
            } else {
                this.attributeCache.put(cacheKey, NULL_CACHING_ATTRIBUTE);
            }
            return logOps;
        }
    }

    @Nullable
    private Collection<LogOperation> computeLogOperations(Method method, Class<?> targetClass) {
        // 不允许非public方法记录日志
        if (!Modifier.isPublic(method.getModifiers())) {
            return null;
        }

        // The method may be on an interface, but we need attributes from the target class.
        // If the target class is null, the method will be unchanged.
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

        // Try: the method in the target class
        Collection<LogOperation> opDef = parseLogOperations(specificMethod);
        if (opDef != null) {
            return opDef;
        }

        if (specificMethod != method) {
            // Fallback: look at the original method 
            opDef = parseLogOperations(method);
            if (opDef != null) {
                return opDef;
            }
        }
        return null;
    }

    @Nullable
    private Collection<LogOperation> parseLogOperations(Method method) {
        Collection<Log> logAnnotations =
                AnnotatedElementUtils.getAllMergedAnnotations(method, Log.class);

        if (logAnnotations.isEmpty()) {
            return null;
        }

        final Collection<LogOperation> ops = new ArrayList<>(1);
        for (Log logAnnotation : logAnnotations) {
            ops.add(parseLogAnnotation(method, logAnnotation));
        }
        return ops;
    }

    private LogOperation parseLogAnnotation(Method method, Log log) {
        LogOperation logOperation = LogOperation.builder()
                .success(log.success())
                .fail(log.fail())
                .operatorId(log.operatorId())
                .condition(log.condition())
                .build();
        validateLogOperation(method, logOperation);
        return logOperation;
    }

    private void validateLogOperation(Method method, LogOperation logOperation) {
        if (!StringUtils.hasText(logOperation.getSuccess()) && !StringUtils.hasText(logOperation.getFail())) {
            throw new IllegalStateException("Invalid log annotation on '" + method.toString()
                    + "'. 'one of success and fail' attribute must be set.");
        }
    }

    private MethodClassKey getCacheKey(Method method, Class<?> targetClass) {
        return new MethodClassKey(method, targetClass);
    }
}
