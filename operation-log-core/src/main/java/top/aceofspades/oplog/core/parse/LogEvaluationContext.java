package top.aceofspades.oplog.core.parse;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationException;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: duanbt
 * @create: 2021-11-17 13:37
 **/
class LogEvaluationContext extends MethodBasedEvaluationContext {

    private final Set<String> unavailableVariables = new HashSet<>(1);

    public LogEvaluationContext(Object rootObject, Method method, Object[] arguments,
                                ParameterNameDiscoverer parameterNameDiscoverer) {
        super(rootObject, method, arguments, parameterNameDiscoverer);
    }

    public void addUnavailableVariable(String name) {
        this.unavailableVariables.add(name);
    }

    @Override
    public Object lookupVariable(String name) {
        if (this.unavailableVariables.contains(name)) {
            throw new EvaluationException("Variable not available: " + name);
        }
        return super.lookupVariable(name);
    }
}
