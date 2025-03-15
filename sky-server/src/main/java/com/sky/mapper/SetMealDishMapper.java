package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /**
     *传入套餐id进行删除数据
     * @param ids
     */
    void deleteBySetmealId(List<Long> ids);


    /**
     * 根据套餐菜品id查询套餐信息，方便后续用户对套餐进行修改时的数据回显
     * @param id
     */
    @Select("select * from setmeal_dish where setmeal_id=#{id}")
    List<SetmealDish> getById(Long id);

}
