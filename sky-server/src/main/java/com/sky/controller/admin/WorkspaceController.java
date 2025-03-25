package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/workspace")
@Slf4j
@Api(tags = "工作台相关接口")
public class WorkspaceController {
    @Autowired
    private WorkspaceService overviewDishesService;

    /// admin/workspace/overviewDishes

    @GetMapping("/overviewDishes")
    @ApiOperation("菜品总览")
    public Result<DishOverViewVO> overviewDishes() {
        DishOverViewVO dishOverViewVO = overviewDishesService.overviewDishes();
        return Result.success(dishOverViewVO);
    }

    @GetMapping("/overviewSetmeals")
    @ApiOperation("套餐总览")
    public Result<SetmealOverViewVO> overviewSetmeals() {
        SetmealOverViewVO setmealOverViewVO = overviewDishesService.overviewSetmeals();
        return Result.success(setmealOverViewVO);
    }


}
