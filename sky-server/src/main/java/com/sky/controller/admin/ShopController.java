package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;


@RestController("adminShopController")
//由于类名同于用户名，因此创建完放入spring容器当中会蝉声冲突，这里起别名
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String KEY="SHOP_STATUS";//定义为常量，减少下面传入key的硬编码

    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setstatus(@PathVariable Integer status) {

        log.info("店铺营业状态设置为{}", status == 1 ? "营业中" : "打样中");
        redisTemplate.opsForValue().set(KEY, status);//通过redis连接对象设置key-value
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取到店铺的营业状态为{}", status == 1 ? "营业中" : "打样中");
        return Result.success(status);
    }

}
