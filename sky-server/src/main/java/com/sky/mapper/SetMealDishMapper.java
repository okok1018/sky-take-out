package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    /**
     * 根据传入的菜品id查询返回套餐id，后续根据套餐id来删除信息
     *
     * @param dishIds
     * @return
     */
    List<Long> getSetMealIdByDishId(List<Long> dishIds);


    /**
     * 根据传入的菜品id插入菜品数据
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);



}
