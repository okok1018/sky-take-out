package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {
    /**
     * 根据条件查询总订单
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据条件查询用户数量
     * @param map
     * @return
     */
    Integer countUserByMap(Map map);

    /**
     * 条件查询订单数据
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 查询销量top10
     * @param beginTime
     * @param endTime
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
