package top.aceofspades.oplog.core.service;

/**
 * @author: duanbt
 * @create: 2021-11-17 15:38
 **/
public interface IFunctionService {

    Object apply(String functionName, Object[] args);

    boolean isBeforeFunction(String functionName);
}
