package com.sky.service.impl;

import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            Integer totalUser = reportMapper.countUserByMap(map);
            map.put("begin", beginTime);
            Integer newUser = reportMapper.countUserByMap(map);
            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(",", dateList))
                .totalUserList(StringUtils.join(",", totalUserList))
                .newUserList(StringUtils.join(",", newUserList))
                .build();
    }
}

