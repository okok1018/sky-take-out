package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    //运用springtask技术，重写方法处理支付订单超时
    @Scheduled(cron = "0 * * * * ?")
    public void checkOutTimeOutOrder() {
        log.info("检查订单超时{}", new Date());
        //支付时间是15min，new一个15分钟前的时间点
        LocalDateTime outTime = LocalDateTime.now().plusMinutes(-15);
        //查询所有待支付的订单，用list集合接收
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, outTime);
        //判断有无超时的订单，如果有，则取消订单
        if (ordersList != null && !ordersList.isEmpty()) {
            //用foreach 来取消订单
            // 遍历订单列表，更新每个订单的状态为已取消，并记录取消原因和取消时间
            ordersList.forEach(orders -> {
                // 设置订单状态为已取消
                orders.setStatus(Orders.CANCELLED);
                // 设置订单取消原因为"订单超时，自动取消"
                orders.setCancelReason("订单超时，自动取消");
                // 设置订单取消时间为当前系统时间
                orders.setCancelTime(LocalDateTime.now());
                // 更新数据库中的订单信息
                orderMapper.update(orders);
            });
        }


    }



    //检查订单，每天凌晨1点检查一次
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkConfirmTimeOutOrder() {
        log.info("检查派送超时{}", new Date());
        //由于是每天凌晨1点出发检查，因此我需要获取昨天12点的时间点
        LocalDateTime outTime = LocalDateTime.now().plusMinutes(-60);
        //根据这个时间点和订单状态进行查询派送状态的订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, outTime);
        //先判断这个list结合是否是空的
        if (ordersList != null && !ordersList.isEmpty()) {
            //用foreach来设置订单已完成
            ordersList.forEach(orders -> {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            });
        }
    }

}
