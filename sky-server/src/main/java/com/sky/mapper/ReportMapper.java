package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

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
}
