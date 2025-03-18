package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
public class SetMealController {
    @Autowired
    private SetMealService setmealService;

    @PostMapping
    @ApiOperation("新增套餐")
    //精确清理
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")//key:setmealCache::100
    public Result<String> save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐{}", setmealDTO);
        setmealService.saveWithDishes(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("套餐的分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("分页查询{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("批量删除菜品")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)//删除所有缓存
    public Result<String> delete(@RequestParam List<Long> ids) {
        //前端传过来的是string类型的ids
        //@RequestParam springmvc框架会将前端传输过来的string类型的数据
        // 转化成注解后指定的类型
        setmealService.deleteBatch(ids);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("根据套餐id查询套餐信息")
    public Result<SetmealVO> find(@PathVariable Long id) {
        SetmealVO setmealVO = setmealService.getByIdWithDishes(id);
        return Result.success(setmealVO);
    }

    @PutMapping
    @ApiOperation("修改套餐功能")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)//删除所有缓存
    public Result<String> update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐{}", setmealDTO);
        //传入dto对象
        setmealService.updateWithDish(setmealDTO);
        return Result.success("修改成功");
    }

    @PostMapping("/status/{status}")
    @ApiOperation("调整套餐的起售停售状态")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result<String> startOrStop(@PathVariable("status") Integer status, Long id){
        setmealService.startOrStop(status,id);
        return Result.success("调整成功");
    }
}
