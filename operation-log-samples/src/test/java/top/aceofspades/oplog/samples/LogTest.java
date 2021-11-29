package top.aceofspades.oplog.samples;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.aceofspades.oplog.samples.beans.Order;
import top.aceofspades.oplog.samples.service.IOrderService;

import java.util.Date;

@SpringBootTest
public class LogTest {

    @Autowired
    private IOrderService orderService;

    @Test
    void create() {
        Order order = new Order();
        order.setProductName("牙膏");
        order.setPurchaseName("张三");
        order.setCreateTime(new Date());
        orderService.create(order);
    }
}