package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;

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
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }


    /**
     * 导出运营数据报表
     *
     * @param response
     */
    public void exportBusiness(HttpServletResponse response) {
//        拿到前30天的时间间隔
        LocalDate begin = LocalDate.now().minusDays(30);//获取当前日期的前30天
        LocalDate end = LocalDate.now().minusDays(1);//获取当前日期的前1天
        //查询概览运营数据，提供给Excel模板文件
        BusinessDataVO businessData =
                workspaceService.getBusinessData(
                        LocalDateTime.of(begin, LocalTime.MIN),
                        LocalDateTime.of(end, LocalTime.MAX)
                );//拿到数据，准备写入已准备的exel表格当中
        // 相当于你在书包里找模板本
        InputStream inputStream = this
                .getClass()// 确认你现在用的课本（当前类）
                .getClassLoader()// 找到装课本的书包拉链（类加载器）
                .getResourceAsStream(// 打开书包找东西
                        "template/运营数据报表模板.xlsx"); // 模板本在书包的"template"夹层里
        try {
            //基于提供好的模板文件创建一个新的Excel表格对象
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //获得Excel文件中的一个Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //拿到第二行第二列，注入数据
            sheet.getRow(1)
                    .getCell(1)
                    .setCellValue(begin + "至"
                            + end + "营业额："
                            + businessData.getTurnover());
            //获取第4行
            XSSFRow row = sheet.getRow(3);
            //设置第四行第三格的数据，注入营业额
            row.getCell(2).setCellValue(businessData.getTurnover());
            //将订单完成率注入到第5格
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            //将新增用户数注入到第7格
            row.getCell(6).setCellValue(businessData.getNewUsers());

            //获取第5行
            row = sheet.getRow(4);
            //将有效订单注入到第3格
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            //将平均客单价注入到第5格
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                //从第8行开始
                row = sheet.getRow(7 + i);
                //准备明细数据
                businessData =
                        workspaceService.getBusinessData(
                                LocalDateTime.of(begin, LocalTime.MIN),
                                LocalDateTime.of(end, LocalTime.MAX)
                        );//拿到数据，准备写入已准备的exel表格当中
                //第1格单元格注入日期
                row.getCell(1).setCellValue(date.toString());
                //第2格单元格注入营业额
                row.getCell(2).setCellValue(businessData.getTurnover());
                //第3格单元格注入有效订单数
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                //第4格单元格注入订单完成率
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                //第5格单元格注入平均客单价
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                //第6格单元格注入新增用户数
                row.getCell(6).setCellValue(businessData.getNewUsers());

            }
                //通过输出流将文件下载到客户端浏览器中
            // 输出流，前面已经写好，因此现在将文件吐出，吐到用户浏览器，供其下载
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            out.flush();
            out.close();
            excel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        return reportMapper.countByMap(map);

///admin/workspace/businessData
    }
}


