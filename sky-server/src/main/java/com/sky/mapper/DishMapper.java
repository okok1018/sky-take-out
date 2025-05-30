package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 插入菜品和口味
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);


    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     *
     * @param id
     * @return
     */
    @Select("select * from dish where id=#{id}")
    Dish getById(Long id);

    /**
     * 根据id集合批量删除菜品
     * @param ids
     */
    void DeleteByIds(List<Long> ids);

    /**
     *根据菜品id查询菜品
     * @param id
     * @return
     */
    DishVO getByIdWithFlavors(Long id);

    /**
     * 修改菜品表数据
     * @param dish
     */

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据分类id查询菜品信息
     * @param dish
     * @return
     */
    List<Dish> getByCategoryId(Dish dish);

    /**
     * 传入套餐id，并且对dish表和setmeal_dish表进行连接，查询所有对应菜品的信息
     * @param id
     * @return
     */
    @Select("select a.* " +
            "from dish a " +
            "left join setmeal_dish b " +
            "on a.id = b.dish_id " +
            "where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long id);

}
