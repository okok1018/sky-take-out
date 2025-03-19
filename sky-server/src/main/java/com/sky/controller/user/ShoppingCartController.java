package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端购物车相关接口")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result<String> add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("购物车信息： {}", shoppingCartDTO);

        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success("success");
    }

    @GetMapping("/list")
    @ApiOperation("C端查看购物车")
    public Result<List<ShoppingCart>> list() {
        List<ShoppingCart> list=shoppingCartService.showShoppingCart();
        return Result.success(list);
    }

    @DeleteMapping("/clean")
    @ApiOperation("C端的清空购物车")
    public Result clean(){
        shoppingCartService.cleanShoppingCart();
        return Result.success("success");
    }
}
