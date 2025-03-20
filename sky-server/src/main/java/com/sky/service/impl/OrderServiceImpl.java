package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
}
