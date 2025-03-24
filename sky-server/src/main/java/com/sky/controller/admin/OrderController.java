package com.sky.controller.admin;

import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.naming.ldap.PagedResultsControl;

@RestController("adminOrderController")//管理端和用户端都有此控制类，起别名作为区分标志
@Slf4j
@Api(tags = "管理端订单相关接口")
@RequestMapping("/admin/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

//    /admin/order/details/{id}  get请求
//id  请求参数
//    返回参数：OrderVO+里面的orderDetail的list集合


    @GetMapping("/conditionSearch")
    @ApiOperation("搜索订单")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    // /admin/order/statistics get
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics() {
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    @GetMapping("details/{id}")
    @ApiOperation("查询订单")
    public Result<OrderVO> details(@PathVariable Long id) {
        OrderVO orderVO = orderService.getDetails(id);
        return Result.success(orderVO);
    }

    @PutMapping("/confirm")
    @ApiOperation("接单功能")
    public Result<String> confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        orderService.confirmOrder(ordersConfirmDTO);
        return Result.success("订单已接单");
    }

    @PutMapping("/rejection")
    @ApiOperation("拒单功能")
    public Result<String> confirmOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        orderService.rejecteOrder(ordersRejectionDTO);
        return Result.success("已巨蛋");
    }

}

