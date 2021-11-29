package top.aceofspades.oplog.core.beans;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author: duanbt
 * @create: 2021-11-24 18:25
 **/
@Data
@Builder
public class LogRecord {

    private Integer id;

    private String operatorId;

    private Boolean success;

    private String action;

    private Date createTime;
}
