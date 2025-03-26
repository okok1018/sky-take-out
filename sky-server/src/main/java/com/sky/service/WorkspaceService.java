package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkspaceService {
    /**
     * 查询菜品总览
     * @return
     */
    DishOverViewVO overviewDishes();

    /**
     *
     * @return
     */
    SetmealOverViewVO overviewSetmeals();

    /**
     * 查询订单总览
     * @return
     */
    OrderOverViewVO overviewOrders();

    /**
     * 传入时间查询概览运营数据
     * @param of
     * @param of1
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime of, LocalDateTime of1);
}
