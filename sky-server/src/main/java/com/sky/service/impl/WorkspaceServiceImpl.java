package com.sky.service.impl;

import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private SetMealMapper setMealMapper;

    /**
     * 菜品总览
     *
     * @return
     */
    public DishOverViewVO overviewDishes() {
        Integer sold = orderMapper.countByStatus(1);
        Integer discontinued = orderMapper.countByStatus(2);
        return new DishOverViewVO(sold, discontinued);
    }

    /**
     * 套擦总览
     *
     * @return
     */
    public SetmealOverViewVO overviewSetmeals() {
        Integer sold = setMealMapper.countByStatus(1);
        Integer discontinued = setMealMapper.countByStatus(2);
        return new SetmealOverViewVO(sold, discontinued);
    }
}
