package top.aceofspades.oplog.core.beans;

import lombok.Builder;
import lombok.Data;

/**
 * @author: duanbt
 * @create: 2021-11-11 17:18
 **/
@Data
@Builder
public class LogOperation {
    private String success;
    private String fail;
    private String operatorId;
    private String condition;
}
