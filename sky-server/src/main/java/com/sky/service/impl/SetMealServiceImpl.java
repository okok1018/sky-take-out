package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetMealServiceImpl implements SetMealService {
    @Autowired
    private SetMealMapper setmealMapper;
    @Autowired
    private SetMealDishMapper setmealDishMapper;

    /**
     * 新增套餐连带菜品
     *
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDishes(SetmealDTO setmealDTO) {
        //解析出对象，进行传参，sql语句实现插入功能实现业务新增套餐
        //传入id进行插入数据

        //接收前端提交的数据--解析出setmeal表数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //插入数据
        setmealMapper.insert(setmeal);

        //插入之后直接获取id，用于关联setmealDish的插入数据
        Long setmealId = setmeal.getId();


        //拿到前端提交的数据之后--解析出setmeals_dish表格的数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

//        传入已经设置好的id对象，进行插入操作
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /**
     * 套餐的分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //解析出dto对象。调用方法返回
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);//page类的对象继承了集合属性，一次相当于是集合

        return new PageResult(page.getTotal(), page.getResult());//方法返回的对象会在此返回给controller层传递给前端
    }

    /**
     * 根据id批量删除套餐
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {
//        接收ids，传入sql语句当中作为id in(,,,)的形式进行批量删除
        if (ids != null && !ids.isEmpty()) {
            setmealMapper.deleteBatch(ids);
        }

    }


}
