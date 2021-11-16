package top.aceofspades.core.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * @author: duanbt
 * @create: 2021-11-11 15:21
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Log {

    /**
     * 操作成功的日志模板
     * <p>
     * 和{@link #fail()} 至少选一个
     */
    String success() default "";

    /**
     * 操作失败的日志模板
     * <p>
     * 和{@link #success()} 至少选一个
     */
    String fail() default "";

    /**
     * 操作人
     */
    String operator() default "";

    /**
     * 记录日志的条件，使用SpEL
     */
    String condition() default "";
}
