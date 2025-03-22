package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;


    /**
     * 用户下单的业务逻辑实现方法
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

//        判断业务逻辑异常：1：购物车是否为空；2；地址是否为空
//        传入地址簿id，查询地址簿信息是否为空--addressBookId
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //        传入购物车id，查询购物车信息是否为空，为空则返回异常--通过userId进行查询
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);


        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
//        向订单中插入数据,先拷贝
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        //dto里面数据远远不够与orders，因此还得额外一一设置属性保证数据的完整性
        order.setOrderTime(LocalDateTime.now());
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setNumber(String.valueOf(System.currentTimeMillis()));//生成唯一订单号
        order.setPhone(addressBook.getPhone());
        order.setConsignee(addressBook.getConsignee());//获取收件人信息
        order.setUserId(userId);
        order.setAddress(addressBook.getDetail());

        System.out.println("order.toString() = " + order);
        orderMapper.insert(order);
        System.out.println("测试插入订单表");


//        new一个订单明细的一个list集合，进行批量插入
        List<OrderDetail> orderDetailList = new ArrayList<>();
//        向订单细节表中插入n条数据
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);//购物车的属性拷贝到订单明细对象上，但是订单id属性没有，因此得设置
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        System.out.println("测试批量插入订单明细表了没");

//        插入之后，需要清空购物车
        shoppingCartMapper.deleteByUserId(userId);

//        封装成数据vo返回给前端:构建vo对象
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();

//        因此，总共涉及到，购物车表，地址表，订单表，订单细节表四张表的操作
        return orderSubmitVO;

    }


    /**
     * @param ordersPaymentDTO 订单支付DTO
     */
    public void payment(OrdersPaymentDTO ordersPaymentDTO) {
        Orders order = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        order.setStatus(Orders.TO_BE_CONFIRMED);
        order.setPayStatus(Orders.PAID);
        order.setCheckoutTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 查询用户订单
     *
     * @param id
     * @return
     */
    public OrderVO getOrders(Long id) {


        Orders order = orderMapper.getOrders(id);//查询订单
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
//        查询订单细节，传入订单id，查询订单细节信息
        List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailsByOrderId(order.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
//统一封装在vo对象里
        return orderVO;

        //答案对比
        /*
          // 根据id查询订单
        Orders orders = orderMapper.getById(id);

        // 查询该订单对应的菜品/套餐明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将该订单及其详情封装到OrderVO并返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
         */
    }


    /**
     * 再来一单
     * @param id
     */
    public void repeatOrder(Long id) {
        //根据传入的订单id，查询订单数据，然后复制一份订单数据数据

        Orders order = orderMapper.getOrders(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 创建新订单对象并从原始订单复制属性，但不包括ID
        Orders newOrder = new Orders();
        BeanUtils.copyProperties(order, newOrder);
        newOrder.setId(null); // 清除ID以便插入时生成新记录
        newOrder.setOrderTime(LocalDateTime.now()); // 更新下单时间为当前时间
        newOrder.setStatus(1); // 假设默认为待付款状态或其他初始状态
        orderMapper.insert(order);//复制一份订单数据到订单表里面
//拿到orderId

        //查询多份订单细节数据，再复制多份订单细节数据，一份订单数据对应多分订单细节数据

        List<OrderDetail> orderDetails = orderDetailMapper.getOrderDetailsByOrderId(order.getId());
        List<OrderDetail> newOrderDetails = new ArrayList<>();//传入订单的新纪录
        for (OrderDetail detail : orderDetails) {
            OrderDetail newDetail = new OrderDetail();//通过循环实现list集合属性的拷贝
            BeanUtils.copyProperties(detail, newDetail);//拷贝
            newDetail.setId(null); // 清除ID以便插入时生成新记录
            newDetail.setOrderId(newOrder.getId()); // 更新新的订单ID
            newOrderDetails.add(newDetail);//更新list的集合对象
        }

        // 批量插入新订单详情
        if (!newOrderDetails.isEmpty()) {
            orderDetailMapper.insertBatch(newOrderDetails);
        }
    }

    /**
     * 取消订单
     *
     * @param id
     */
    public void cancelOrders(Long id) {
        //判断订单是否存在
        Orders order = orderMapper.getOrders(id);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (order.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //更新订单状态
        order.setStatus(Orders.CANCELLED); // 已取消
        order.setCancelReason("用户取消");//取消原因
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 查询所有订单
     *
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    public PageResult pageQuery(int pageNum, int pageSize, Integer status) {
        // 设置分页
        PageHelper.startPage(pageNum, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);//订单状态
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());//用户id


        Page<Orders> page=orderMapper.pageQuery(ordersPageQueryDTO);//此时已封装全部订单数据

        List<OrderVO> list = new ArrayList();
        // 查询出订单明细，并封装入OrderVO进行响应
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {//将page遍历

                Long orderId = orders.getId();// 拿到订单id

                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getOrderDetailsByOrderId(orderId);//查详细信息

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);//vo继承了orders，拷贝orders
                orderVO.setOrderDetailList(orderDetails);//传入菜品细节

                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);//total,record


    }

}
