package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

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
     * 查询今日运营数据
     * @return
     */
    BusinessDataVO businessData();
}
