package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 根据传入的openid进行表查询用户,来确定是否是新用户
     *
     * @param openid 传入的openid
     * @return 查询到的user对象
     */
    @Select("select * from user where openid=#{openid}")
    User getUserByOpenid(String openid);

    /**
     * 传入构建好的新用户信息,插入表中
     * @param user 构建的新用户
     */
    void insert(User user);
}
