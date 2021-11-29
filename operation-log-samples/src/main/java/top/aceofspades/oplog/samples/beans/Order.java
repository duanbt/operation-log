package top.aceofspades.oplog.samples.beans;

import lombok.Data;

import java.util.Date;

/**
 * @author: duanbt
 * @create: 2021-11-25 14:30
 **/
@Data
public class Order {

    private Long orderId;

    private String orderNo;

    private String productName;

    private String purchaseName;

    private Date createTime;
}
