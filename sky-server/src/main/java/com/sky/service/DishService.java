package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;


public interface DishService {

    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     *批量删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品信息
     *
     * @param id
     */
    DishVO getByIdWithFlavors(Long id);

    /**
     * 修改菜品连带着口味
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 根据分类id查询菜品信息
     * @param categoryId
     * @return
     */
    List<Dish> getByCategoryId(Long categoryId);

    /**
     * 菜品的起售和停售
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);
}
