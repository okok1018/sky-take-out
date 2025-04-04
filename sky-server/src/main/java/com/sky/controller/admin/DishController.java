package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result<String> save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        String key="dish_"+dishDTO.getCategoryId();
        cleanCashe(key);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页请求{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }


    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result<String> delete(@RequestParam List<Long> ids) {
        //@RequestParam springmvc框架会将前端传输过来的string类型的数据
        // 转化成注解后指定的类型
        log.info("批量删除菜品");
        dishService.deleteBatch(ids);
        cleanCashe("dish_*");

        return Result.success();

    }

    @GetMapping("/{id}")
    @ApiOperation("根据菜品id查询")
    public Result<DishVO> find(@PathVariable Long id) {
        DishVO dishVO = dishService.getByIdWithFlavors(id);
        return Result.success(dishVO);

    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result<String> update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        cleanCashe("dish_*");

        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    //接收菜品id，返回相应一系列的菜品数据
    public Result<List<Dish>> findByCategoryId(Long categoryId) {
        List<Dish> list = dishService.getByCategoryId(categoryId);
        return Result.success(list);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("起售停售菜品")
    public Result<String> startOrStop(@PathVariable("status") Integer status, Long id) {
        dishService.startOrStop(status, id);
        cleanCashe("dish_*");
        return Result.success();
    }

    /**
     * 清理缓存数据
     *
     * @param pattern
     */
    private void cleanCashe(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);

    }

}
