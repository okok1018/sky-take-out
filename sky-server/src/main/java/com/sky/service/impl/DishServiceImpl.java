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

import java.util.Collections;
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
        Long dishId = dish.getId();//不可以合并到setDishId(dishId),这是用来插入数据获取id用来回显
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
        log.info("根据口味id查询菜品信息进行回显方便用户进行修改{}", id);
//此操作涉及到查询两张表格，因此，调用两个方法查询到后，封装到一个vo对象返回给前端
        DishVO dishVO = new DishVO();//new一个dishVO来接收查询的数据，进行封装，传输到前端
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        BeanUtils.copyProperties(dish, dishVO); //把dish里面的属性拷贝给dishVO
        dishVO.setFlavors(flavors); //最后把口味数据拷过去
        return dishVO;
    }

    /**
     * 修改菜品连带着口味
     *
     * @param dishDTO
     */
    public void updateWithFlavor(DishDTO dishDTO) {
//        真正的代码逻辑加工都放在服务层
//        解析出数据，加工哪些表，先把数据分离出来
        //接受到了dto，这已经是前端已经修改好的数据，等待上传给数据库的
//        因此我现在得解析出来，对不同类型的数据进行表数据的更新
//        根据数据内容，我得操作两个表，一个是dish表，一个是dish_flavor表，因此先new出来，进行属性拷贝
//        表dish
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
//        表dish_flavor
//        对于这个表格，用户会对口味数据进行多种变化，
//        比如：删除某些口味数据，或者新增某些口味数据，因此带来了更新层面的困难
//        现在，先根据id对口味表进行删除数据，然后对前端提交的口味数据进行插入，减少技术层面的复杂性
        dishFlavorMapper.DeleteByIds(Collections.singletonList(dishDTO.getId()));
//        这里为了代码的复用性，直接调用了批量删除口味的方法，传入的是id的集合，这里进行转化成list集合即可
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);

        }

//        将这些数据提取出来，作为参数传递给sql语句然后进行数据库的操作。
    }

    /**
     * 根据分类id查询菜品信息
     *
     * @param categoryId
     * @return
     */
    public List<Dish> getByCategoryId(Long categoryId) {
//        接收分类的id，调用查询语句查询相应的菜品信息，
        Dish dish = new Dish().builder()
                .status(StatusConstant.ENABLE)
                .categoryId(categoryId)
                .build();//由于前端页面填写的信息没有status和categoryId，因此得构建出来
        return dishMapper.getByCategoryId(dish);
    }


}
