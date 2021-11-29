package top.aceofspades.oplog.samples.service.impl;

import org.springframework.stereotype.Service;
import top.aceofspades.oplog.core.annotation.Log;
import top.aceofspades.oplog.core.context.LogContext;
import top.aceofspades.oplog.samples.beans.Product;
import top.aceofspades.oplog.samples.service.IProductService;

/**
 * @author: duanbt
 * @create: 2021-11-26 10:32
 **/
@Service
public class ProductServiceImpl implements IProductService {

    @Log(
       success = "outerVar:{#outerVar}, innerVar:{#innerVar}。查询商品【{#productName}】的详情，结果：{#_result}"
    )
    @Override
    public Product getProductDetail(String productName) {
        LogContext.putVariable("innerVar", "innerVarValue");
        Product product = new Product();
        product.setProductName(productName);
        product.setPrice(100);
        return product;
    }
}
