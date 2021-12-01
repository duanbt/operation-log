package top.aceofspades.oplog.boot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.aceofspades.oplog.core.aop.BeanFactoryLogOperationSourceAdvisor;
import top.aceofspades.oplog.core.aop.LogInterceptor;
import top.aceofspades.oplog.core.aop.LogOperationSource;
import top.aceofspades.oplog.core.parse.LogValueParser;
import top.aceofspades.oplog.core.service.IFunctionService;
import top.aceofspades.oplog.core.service.ILogService;
import top.aceofspades.oplog.core.service.IOperatorGetService;
import top.aceofspades.oplog.core.service.IParseFunction;
import top.aceofspades.oplog.core.service.impl.DefaultFunctionServiceImpl;
import top.aceofspades.oplog.core.service.impl.DefaultLogServiceImpl;
import top.aceofspades.oplog.core.service.impl.DefaultOperatorGetServiceImpl;

import java.util.List;

/**
 * @author: duanbt
 * @create: 2021-11-25 13:40
 **/
@Configuration
public class ProxyOperationLogConfiguration {

    @Bean
    public BeanFactoryLogOperationSourceAdvisor logAdvisor(LogInterceptor logInterceptor) {
        BeanFactoryLogOperationSourceAdvisor advisor = new BeanFactoryLogOperationSourceAdvisor();
        advisor.setLogOperationSource(logOperationSource());
        advisor.setAdvice(logInterceptor);
        return advisor;
    }

    @Bean
    public LogOperationSource logOperationSource() {
        return new LogOperationSource();
    }

    @Bean
    public LogInterceptor logInterceptor(ILogService logService,
                                         IFunctionService functionService,
                                         IOperatorGetService operatorGetService) {
        LogInterceptor interceptor = new LogInterceptor();
        interceptor.setLogOperationSource(logOperationSource());
        interceptor.setLogService(logService);
        LogValueParser parser = new LogValueParser();
        parser.setFunctionService(functionService);
        interceptor.setLogValueParser(parser);
        interceptor.setOperatorGetService(operatorGetService);
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public IOperatorGetService operatorGetService() {
        return new DefaultOperatorGetServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public IFunctionService functionService(List<IParseFunction> functions) {
        return new DefaultFunctionServiceImpl(functions);
    }

    @Bean
    @ConditionalOnMissingBean
    public ILogService logService() {
        return new DefaultLogServiceImpl();
    }
}
