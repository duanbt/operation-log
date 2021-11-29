package top.aceofspades.oplog.core.aop;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author: duanbt
 * @create: 2021-11-11 16:46
 **/
abstract class LogOperationSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {

    protected LogOperationSourcePointcut() {
        setClassFilter(new LogOperationSourceClassFilter());
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        LogOperationSource logOperationSource = getOperationLogOperationSource();
        return (logOperationSource != null
                && !CollectionUtils.isEmpty(logOperationSource.getLogOperations(method, targetClass)));
    }

    protected abstract LogOperationSource getOperationLogOperationSource();

    private class LogOperationSourceClassFilter implements ClassFilter {
        @Override
        public boolean matches(Class<?> clazz) {
            LogOperationSource logOperationSource = getOperationLogOperationSource();
            return (logOperationSource == null || logOperationSource.isCandidateClass(clazz));
        }
    }
}
