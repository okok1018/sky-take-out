package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ReportMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;

import static com.sky.entity.Orders.PENDING_PAYMENT;
import static com.sky.entity.Orders.TO_BE_CONFIRMED;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private SetMealMapper setMealMapper;
    @Autowired
    private ReportService reportService;


    /**
     * 菜品总览
     *
     * @return
     */
    public DishOverViewVO overviewDishes() {
        Integer sold = orderMapper.countByStatus(PENDING_PAYMENT);
        Integer discontinued = orderMapper.countByStatus(TO_BE_CONFIRMED);
        return new DishOverViewVO(sold, discontinued);
    }

    /**
     * 套餐总览
     *
     * @return
     */
    public SetmealOverViewVO overviewSetmeals() {
        Integer sold = setMealMapper.countByStatus(PENDING_PAYMENT);
        Integer discontinued = setMealMapper.countByStatus(TO_BE_CONFIRMED);
        return new SetmealOverViewVO(sold, discontinued);
    }

    /**
     * 查询订单管理数据
     *
     * @return
     */
    public OrderOverViewVO overviewOrders() {
        HashMap map = new HashMap();
// 将当前日期与时间的开始时刻关联到键"begin"
        map.put("begin", LocalDateTime.now().with(LocalTime.MIN));
        map.put("status", TO_BE_CONFIRMED);
        orderMapper.countByMap(map);
        //待接单
        Integer waitingOrders = orderMapper.countByMap(map);
        //待派送
        map.put("status", Orders.CONFIRMED);
        Integer deliveredOrders = orderMapper.countByMap(map);
        //已完成
        map.put("status", Orders.COMPLETED);
        Integer completedOrders = orderMapper.countByMap(map);
        //已取消
        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.countByMap(map);
        //全部订单
        map.put("status", null);
        Integer allOrders = orderMapper.countByMap(map);
        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 查询今日运营数据
     *
     * @return
     */
    public BusinessDataVO getBusinessData(LocalDateTime beginTime, LocalDateTime endTime) {
//统计新增用户数，订单完成率，营业额，平均客单价，有效订单数

//传入时间点，统计出新增用户数
        HashMap map = new HashMap();
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);

        //新增用户数
        Integer newUsers = orderMapper.countByMap(map);
//订单完成率  COMPLETED
        map.put("status", Orders.COMPLETED);
        Integer validOrderCount = orderMapper.countByMap(map);//有效订单数
        Integer totalOrderCount = orderMapper.countByMap(map);//订单总数
        //订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount.doubleValue();
        }
        //营业额，已完成订单的的amount总和
        Double turnover = orderMapper.sumByMap(map);

//        平均客单价
        Double unitPrice = 0.0;
        if (validOrderCount != 0) {
//            平均客单价 = 营业额 ÷ 有效订单数
            unitPrice = turnover / validOrderCount.doubleValue();
        }
        return BusinessDataVO.builder()
                .newUsers(newUsers)
                .orderCompletionRate(orderCompletionRate)
                .turnover(turnover)
                .unitPrice(unitPrice)
                .validOrderCount(validOrderCount)
                .build();
    }




}
