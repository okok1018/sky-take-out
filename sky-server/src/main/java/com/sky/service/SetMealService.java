package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface SetMealService {

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void saveWithDishes(SetmealDTO setmealDTO);

    /**
     * 套餐的分页查询,传入dto对象
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据ids批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);
}
