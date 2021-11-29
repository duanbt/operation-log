package top.aceofspades.oplog.samples.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.aceofspades.oplog.core.annotation.Log;
import top.aceofspades.oplog.core.context.LogContext;
import top.aceofspades.oplog.samples.beans.Order;
import top.aceofspades.oplog.samples.beans.Product;
import top.aceofspades.oplog.samples.service.IOrderService;
import top.aceofspades.oplog.samples.service.IProductService;

import java.util.Random;
import java.util.UUID;

/**
 * @author: duanbt
 * @create: 2021-11-25 14:47
 **/
@Service
@Slf4j
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private IProductService productService;


    @Log(
            success = "outerVar:{#outerVar}, innerVar:{#innerVar}。 {#userDetail(#order.purchaseName).age}岁的{#order.purchaseName}下了一个订单，购买商品【{#order.productName}】，订单编号:{#_result.orderNo}",
            fail = "创建订单失败，失败原因：【{#_errorMsg}】",
            operatorId = "#order.purchaseName",
            condition = "#order.purchaseName.equals('张三')")
    @Override
    public Order create(Order order) {
        LogContext.putVariable("outerVar", "outerVarValue");

        order.setOrderId(System.currentTimeMillis());
        order.setOrderNo(UUID.randomUUID().toString());

        Product productDetail = productService.getProductDetail(order.getProductName());
        log.info("商品详情：{}", productDetail);

        Random random = new Random();
        int randomInt = random.nextInt(10);
        if (randomInt > 5) {
            log.info("创建订单... orderNo={}", order.getOrderNo());
        } else {
            log.error("创建订单失败");
            throw new IllegalStateException("商品已售空");
        }
        return order;
    }
}
