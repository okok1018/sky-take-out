package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    //注入mapper
    @Autowired
    private ReportMapper reportMapper;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     */
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
//调用mapper

//       创建一个传入日期的list集合
        List<LocalDate> dateList = new ArrayList<>();
        //传入begin日期
        dateList.add(begin);
        //查询日期对应的营业额
        //循环遍历，把日期加到list集合中
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Double> turnoverList = new ArrayList();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("status", Orders.COMPLETED);
            map.put("begin", beginTime);
            map.put("end", endTime);
            Double turnover = reportMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(",", dateList))
                .turnoverList(StringUtils.join(",", turnoverList))
                .build();


    }

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 初始化新用户列表，用于存储新用户的信息
        List<Integer> newUserList = new ArrayList<>();

        // 初始化所有用户列表，用于存储系统中所有用户的信息
        List<Integer> totalUserList = new ArrayList<>();
        Result result = new Result(dateList, newUserList, totalUserList);

        for (LocalDate date : result.dateList()) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            Integer totalUser = reportMapper.countUserByMap(map);
            map.put("begin", beginTime);
            Integer newUser = reportMapper.countUserByMap(map);
            result.totalUserList().add(totalUser);
            result.newUserList().add(newUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(",", result.dateList()))
                .totalUserList(StringUtils.join(",", result.totalUserList()))
                .newUserList(StringUtils.join(",", result.newUserList()))
                .build();
    }

    private record Result(List<LocalDate> dateList, List<Integer> newUserList, List<Integer> totalUserList) {
    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        // 创建一个日期列表，用于存储从开始日期到结束日期之间的所有日期
        List<LocalDate> dateList = new ArrayList<>();
        // 将开始日期添加到日期列表中
        dateList.add(begin);
        // 循环遍历，直到开始日期等于结束日期
        while (!begin.equals(end)) {
            // 开始日期增加一天
            begin = begin.plusDays(1);
            // 将增加后的日期添加到日期列表中
            dateList.add(begin);
        }
        // 创建一个订单总数列表，用于存储每个日期对应的订单数量
        List<Integer> totalOrderCountList = new ArrayList<>();
        // 创建一个有效订单数量列表，用于存储每个日期对应的有效订单数量
        List<Integer> validOrderCountList = new ArrayList<>();
        // 创建一个订单完成率列表，用于存储每个日期对应的订单完成率
        List<Number> orderCompletionRate = new ArrayList<>();
        // 遍历日期列表，统计每天的订单数和有效订单数
        for (LocalDate date : dateList) {
            // 初始化当天的开始时间（00:00:00）
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            // 初始化当天的结束时间（23:59:59.999999999）
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);


            // 查询当天的订单总数
            Integer totalOrder = getOrderCount(beginTime, endTime, null);

            // 添加查询条件：订单状态为已完成
            // 查询当天的有效订单数
            Integer validOrder = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            // 将订单总数添加到列表中
            totalOrderCountList.add(totalOrder);
            // 将有效订单数添加到列表中
            validOrderCountList.add(validOrder);
        }
        //计算整个时间段内的订单总数
        Integer totalOrderSum = totalOrderCountList.stream().mapToInt(Integer::intValue).sum();

        //计算整个时间段内的有效订单总数
        Integer validOrderSum = validOrderCountList.stream().mapToInt(Integer::intValue).sum();

        //计算整个时间段内的总订单完成率
//        对订单完成率计算之前还需得判断一下
        Double orderCompletionRateOverall = 0.0;
        if (totalOrderSum != 0) {
            orderCompletionRateOverall = validOrderSum.doubleValue() / totalOrderSum.doubleValue();
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(",", dateList))
                .orderCompletionRate(orderCompletionRateOverall)
                .orderCountList(StringUtils.join(",", totalOrderCountList))
                .totalOrderCount(totalOrderSum)
                .validOrderCount(validOrderSum)
                .validOrderCountList(StringUtils.join(",", validOrderCountList))
                .build();
    }

    /**
     * 销量排名top10
     *
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        //时间格式转化
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = reportMapper.getSalesTop10(beginTime, endTime);
        // 通过流处理，将销售前10的商品的名称提取出来，收集到一个字符串列表中
        List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).toList();

        // 从salesTop10列表中提取每个商品的销售数量，并创建一个新的列表
        List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).toList();
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }

//        String nameList = StringUtils.join(salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()), ",");
//        String numberList = StringUtils.join(salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()), ",");
//
//        return SalesTop10ReportVO.builder()
//                .nameList(nameList)
//                .numberList(numberList)
//                .build();
//    }

    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        return reportMapper.countByMap(map);

///admin/workspace/businessData
    }
}


