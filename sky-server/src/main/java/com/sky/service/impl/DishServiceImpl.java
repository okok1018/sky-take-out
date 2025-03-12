package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetMealDishMapper setMealDishMapper;

    /**
     * 新增菜品和口味
     *
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
//插入菜品--1条
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        Long dishId = dish.getId();
//        插入口味表数据，可能多条
//        DTO里面提取出来flavor集合，对属性进行相应的注入
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);

        }

    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
//        传入页号和页面尺寸
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());

    }

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {


//        一次可以删除一个菜品，也可以批量删除菜品
        //起售中的菜品不可以删除，根据菜品id查询菜品的起售状态，删除之后，关联的口味数据也需要删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (Objects.equals(dish.getStatus(), StatusConstant.ENABLE)) {//如果在起售状态，则返回一场提示不可以删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);//抛出定义好的一场传入自定义好的信息，减少硬编码。
            }

        }
        //被套餐关联的菜品不能删除，根据id查询到菜品是否被关联，删除之后，关联的口味数据也需要删除
        List<Long> setMealIdByDishIds = setMealDishMapper.getSetMealIdByDishId(ids);

        if (!setMealIdByDishIds.isEmpty()) { //判断有没有获取到，如果没获取到，说明没关联，可以删
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);//抛出定义好的一场传入自定义好的信息，减少硬编码。
        }
//删除菜品以及菜品关联数据
//直接根据id集合进行批量删除
        dishMapper.DeleteByIds(ids);
        dishFlavorMapper.DeleteByIds(ids);
    }

    /**
     * 根据id查询菜品信息
     *
     * @param id
     */

    public DishVO getByIdWithFlavors(Long id) {
        log.info("根据口味id查询菜品信息进行回显方便用户进行修改{}",id);
//此操作涉及到查询两张表格，因此，调用两个方法查询到后，封装到一个vo对象返回给前端
        DishVO dishVO = new DishVO();//new一个dishVO来接收查询的数据，进行封装，传输到前端
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        BeanUtils.copyProperties(dish, dishVO); //把dish里面的属性拷贝给dishVO
        dishVO.setFlavors(flavors); //最后把口味数据拷过去
        return dishVO;
    }


}
