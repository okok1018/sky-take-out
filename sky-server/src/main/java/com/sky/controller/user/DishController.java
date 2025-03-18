package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;//注入一个操作redis的对象

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {

        String key="dish_"+categoryId;//构造redis当中的key，规则：dish_分类id
        //查询redis里面有无记录,将记录返回给List集合封装
        List<DishVO> list= (List<DishVO>) redisTemplate.opsForValue().get(key);

        //判断集合是否为空
        if(list !=null&&list.size()>0){
            //redis有缓存数据
            return Result.success(list);
        }




        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品


        // 如果redis没有缓存数据-->查询数据库，再将数据库里面的数据放到redis

         list = dishService.listWithFlavor(dish);//查询数据库，返回给list集合
        redisTemplate.opsForValue().set(key,list);//将list集合存入redis中
        return Result.success(list);
    }


}
