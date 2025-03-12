package com.sky.mapper;

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
//select * from 。。 where id in(,,,)类似的形式
    List<Long> getSetMealIdByDishId(List<Long> dishIds);
}
