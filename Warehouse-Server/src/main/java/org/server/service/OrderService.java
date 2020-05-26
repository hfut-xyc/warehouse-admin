package org.server.service;

import org.server.entity.Order;
import org.server.entity.Product;
import org.server.exception.InsertException;
import org.server.exception.NotFoundException;
import org.server.exception.OutOfStockException;
import org.server.mapper.OrderMapper;
import org.server.mapper.ProductMapper;
import org.server.mapper.WarehouseMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class OrderService {

	@Resource
	private OrderMapper orderMapper;

	@Resource
	private WarehouseMapper warehouseMapper;

	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	public List<Order> getOrderList(Date startTime, Date endTime) {
		return orderMapper.getOrderList(startTime, endTime);
	}

	@Transactional
	public int addOrderWithOld(Order order) throws OutOfStockException, InsertException {
		// 1.先添加订单
		int res1 = orderMapper.addOrder(order);
		if (res1 != 1) {
			throw new InsertException("添加订单失败");
		}
		// 2.再更新库存，减少行级锁持有时间
		int res2 = warehouseMapper.updateProductByWid(order.getWid(), order.getPid(), order.getAmount());
		if (res2 != 1) {
			throw new OutOfStockException("库存不足");
		}
		// 3.更新缓存
		String warehousekey = "warehouse:" + order.getWid();
		String productKey = "product:" + order.getPid();
		Product product = (Product) redisTemplate.boundHashOps(warehousekey).get(productKey);
		product.setTotal(product.getTotal() + order.getAmount());
		redisTemplate.boundHashOps(warehousekey).put(productKey, product);
		return 1;
	}

	@Transactional
	public int addOrderWithNew(Order order) throws InsertException {
		// 新产品订单只能是入库，不需要查库存
//		int res1 = warehouseMapper.addProductByWid(order.getWid(), order.getPid(), order.getAmount());
//		int res2 = orderMapper.addOrder(order);
//		if (res1 != 1 || res2 != 1) {
//			throw new InsertException("添加订单失败");
//		}
		return 1;
	}
}
