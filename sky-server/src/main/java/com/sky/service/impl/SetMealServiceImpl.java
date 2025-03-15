package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
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

import java.util.Collections;
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
     *
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
     *
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
//        接收ids，传入sql语句当中作为id in(,,,)的形式进行批量删除
//        判断套餐的状态是不是可删，起售当中的不可以删
//        不仅仅只是对一个表进行操作删除，还需要对关联表进行一一删除，不能有残留的数据
        ids.forEach(id -> {//这里是对ids集合进行取出，用id接收
            Setmeal setmeal = setmealMapper.getById(id);//传入id拿到对应的套餐数据
            if (StatusConstant.ENABLE.equals(setmeal.getStatus())) {//对套餐的状态进行比对
                //起售中的套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
//            删除套餐表的数据
        setmealMapper.deleteBatch(ids);//删setmeal表中的数据
//删除关联数据
        setmealDishMapper.deleteBySetmealId(ids);//删setmeal_dish表的数据
    }
}

