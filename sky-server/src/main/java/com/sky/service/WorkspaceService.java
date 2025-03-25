package com.sky.service;

import com.sky.vo.DishOverViewVO;
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
}
