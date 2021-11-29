package top.aceofspades.oplog.core.aop;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.lang.Nullable;

/**
 * @author: duanbt
 * @create: 2021-11-11 16:43
 **/
public class BeanFactoryLogOperationSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    @Nullable
    private LogOperationSource logOperationSource;

    private final LogOperationSourcePointcut pointcut = new LogOperationSourcePointcut() {
        @Override
        @Nullable
        protected LogOperationSource getOperationLogOperationSource() {
            return logOperationSource;
        }
    };

    public void setLogOperationSource(LogOperationSource logOperationSource) {
        this.logOperationSource = logOperationSource;
    }

    public void setClassFilter(ClassFilter classFilter) {
        this.pointcut.setClassFilter(classFilter);
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }
}
