package top.aceofspades.oplog.samples.service;

import top.aceofspades.oplog.samples.beans.Product;

/**
 * @author: duanbt
 * @create: 2021-11-26 10:30
 **/
public interface IProductService {

    Product getProductDetail(String productName);
}
