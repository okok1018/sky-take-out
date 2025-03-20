package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetMealMapper setMealMapper;


    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
//        判断当前加入到购物车中的商品是否已经存在了
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);//拷贝
        Long userId = BaseContext.getCurrentId();//通常用于在服务端应用中获取当前操作用户的 ID。
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);//查询购物车中的菜品

        if (!shoppingCarts.isEmpty()) {//不为空--商品在购物车里面存在--加1。
            //要么查不到，要么只有一条符合条件的数据
//            每次添加购物车之前先进行查询，只要查到，就实现加1操作
            ShoppingCart cart = shoppingCarts.get(0);
            cart.setNumber(cart.getNumber() + 1);//将number+1，再执行uodate语句更新表数据
            shoppingCartMapper.updateNumberById(cart);
        } else {//        不存在：插入一条购物车数据

//            判断是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();

            if (dishId != null) {//添加进来的是菜品
                Dish dish = dishMapper.getById(dishId);
                //设置其他属性,而这些属性只有菜品表才有
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            } else {//设置套餐属性套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setMealMapper.getById(setmealId);
                //设置其他属性,而这些属性只有菜品表才有
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }

            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    /**
     * @return
     */
    public List<ShoppingCart> showShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * 清空购物车
     */
    public void cleanShoppingCart() {
        Long UserId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(UserId);

    }

    /**
     * 删除购物车中的一条数据
     *
     * @param shoppingCartDTO
     */
    public void delete(ShoppingCartDTO shoppingCartDTO) {
        //拿到dish_id,setmeal_id,dish_flavor,
//        同菜不同口味，只有id和dish_flavor才能表示
//        三个数据封装，传入动态查询中，拿到唯一表示菜品的id，
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart= shoppingCartMapper.list(shoppingCart).get(0);
//        通过id拿到number;
        if (shoppingCart.getNumber() >= 2) {
            shoppingCart.setNumber(shoppingCart.getNumber() - 1);
            shoppingCartMapper.updateNumberById(shoppingCart);//更新number
        }else {
        shoppingCartMapper.delete(shoppingCart);//删除整条数据
            }
    }


}
