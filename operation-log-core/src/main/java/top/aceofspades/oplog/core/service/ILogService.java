package top.aceofspades.oplog.core.service;

import top.aceofspades.oplog.core.beans.LogRecord;

/**
 * @author: duanbt
 * @create: 2021-11-12 11:36
 **/
public interface ILogService {

    /**
     * 保存日志
     *
     * @param logRecord 日志数据
     */
    void record(LogRecord logRecord);
}
