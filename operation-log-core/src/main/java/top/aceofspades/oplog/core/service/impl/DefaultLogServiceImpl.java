package top.aceofspades.oplog.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import top.aceofspades.oplog.core.beans.LogRecord;
import top.aceofspades.oplog.core.service.ILogService;

/**
 * @author: duanbt
 * @create: 2021-11-12 11:49
 **/
@Slf4j
public class DefaultLogServiceImpl implements ILogService {
    @Override
    public void record(LogRecord logRecord) {
        log.info("【logRecord】log={}", logRecord);
    }
}
