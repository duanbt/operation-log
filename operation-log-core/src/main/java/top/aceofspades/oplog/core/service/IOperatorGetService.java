package top.aceofspades.oplog.core.service;

import top.aceofspades.oplog.core.beans.Operator;

/**
 * @author: duanbt
 * @create: 2021-11-12 11:37
 **/
public interface IOperatorGetService {

    /**
     * 获取当前登陆的用户
     */
    Operator getUser();
}
