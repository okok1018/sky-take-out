package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param order 订单
     */
    void insert(Orders order);

    /**
     * 根据订单号查询订单
     * @param orderNumber 订单号
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param order 订单
     */
    void update(Orders order);

    /**
     * 查询订单
     * @param id
     */
    @Select("select * from orders where id=#{id}")
    Orders getOrders(Long id);

    /**
     * 根据订单id删除订单信息
     * @param id
     */
    @Delete("delete from orders where id=#{id}")
    void deleteById(Long id);

    /**
     * 传入OrdersPageQueryDTO，进行分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 传入状态返回统计值
     * @param status
     * @return
     */
    @Select("select count(*) from orders where status=#{status}")
    Integer countByStatus(Integer status);

    /**
     *  根据状态和下单时间查询订单
     * @param status
     * @param outTime
     * @return
     */
    @Select("select * from orders where status=#{status} and order_time < #{outTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime outTime);

    /**
     * 根据map查询订单总数
     * @param map
     * @return
     */
    Integer countByMap(HashMap map);

    /**
     * 根据map查询订单总金额
     * @param map
     * @return
     */
    Double  sumByMap(HashMap map);
}
