package top.aceofspades.oplog.boot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author: duanbt
 * @create: 2021-11-25 11:58
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ProxyOperationLogConfiguration.class)
public @interface EnableOperationLog {
}
