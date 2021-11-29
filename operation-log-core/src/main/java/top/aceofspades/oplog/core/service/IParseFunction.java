package top.aceofspades.oplog.core.service;

/**
 * @author: duanbt
 * @create: 2021-11-17 17:35
 **/
public interface IParseFunction {

    default boolean isBefore() {
        return false;
    }

    String functionName();

    Object apply(Object[] args);
}
