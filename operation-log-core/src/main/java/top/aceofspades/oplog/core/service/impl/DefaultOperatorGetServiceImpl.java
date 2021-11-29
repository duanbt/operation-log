package top.aceofspades.oplog.core.service.impl;

import top.aceofspades.oplog.core.beans.Operator;
import top.aceofspades.oplog.core.service.IOperatorGetService;

/**
 * @author: duanbt
 * @create: 2021-11-12 11:50
 **/
public class DefaultOperatorGetServiceImpl implements IOperatorGetService {
    @Override
    public Operator getUser() {
        Operator operator = new Operator();
        operator.setOperatorId("default");
        return operator;
    }
}
