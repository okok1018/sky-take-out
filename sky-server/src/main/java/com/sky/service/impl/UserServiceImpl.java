package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    //微信服务接口地址
    private static final String wx_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;


    /**
     * 微信登录
     *
     * @param userLoginDTO
     * @return
     */
    public User wxLogin(UserLoginDTO userLoginDTO) {

//        调用下面的方法,传入code,拿到openid
        String openid = getOpenid(userLoginDTO.getCode());

        if (openid == null) {//如果openid没拿到
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);//抛出登录失败的异常


        }
//        不为空，判断是否是新用户(对于外卖系统来说，对比用户数据)
        User user = userMapper.getUserByOpenid(openid);

        if (user == null) {//说明表里没有数据,是新用户

            user = User.builder()//构建用户,可获得的信息如下:
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();

            userMapper.insert(user);
               //若是，自动完成注册
        }

//        返回用户对象
        return user;


    }

    /**
     * 调用微信接口服务,获取微信用户的openid,单独抽离出方法
     * @param code js_code
     * @return openid
     */
    private String getOpenid(String code) {
        //调用微信服务器接口，来获得的当前微信用户的openId,
        Map<String, String> map = new HashMap<>();
        //将请求参数封装进去
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");

//        发送请求，返回json字符串
        String json = HttpClientUtil.doGet(wx_LOGIN, map);//传入请求路径和封装了请求参数的map集合

        JSONObject jsonObject = JSON.parseObject(json);

        //判断openId有没有获取到，--是否为空，若为空，登录失败，
        String openid = jsonObject.getString("openid");//拿到openid
        return openid;

    }
}
