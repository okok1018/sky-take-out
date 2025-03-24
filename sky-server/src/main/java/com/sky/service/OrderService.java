package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    /**
     * 用户下单的方法
     *
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 用户支付
     *
     * @param ordersPaymentDTO
     */
    void payment(OrdersPaymentDTO ordersPaymentDTO);

    /**
     * 查询用户订单
     *
     * @param id
     * @return
     */
    OrderVO getOrders(Long id);


    /**
     * 再来一单
     *
     * @param id
     */
    void repeatOrder(Long id);

    /**
     * 取消订单
     *
     * @param id
     */
    void cancelOrders(Long id);

    /**
     * 查询所有订单
     *
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    PageResult pageQueryUser(int page, int pageSize, Integer status);

    /**
     * 管理端订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各个状态的订单数量统计
     *
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 确认订单
     * 此方法用于处理订单的最终确认逻辑，包括但不限于更新订单状态、库存减少等
     * 具体业务逻辑可能涉及与库存系统、支付系统等的交互
     *
     * @param ordersConfirmDTO 包含订单确认所需信息的数据传输对象
     */
    void confirmOrder(OrdersConfirmDTO ordersConfirmDTO);


    /**
     * 根据订单ID获取订单详情
     *
     * @param id 订单ID，用于标识特定的订单
     * @return OrderVO 返回订单详情对象，包含订单的详细信息
     */
    OrderVO getDetails(Long id);

    /**
     * 拒单功能
     * @param ordersRejectionDTO 包含拒单信息
     */
    void rejecteOrder(OrdersRejectionDTO ordersRejectionDTO);
}


