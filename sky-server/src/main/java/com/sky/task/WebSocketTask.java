package com.sky.task;

import com.sky.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//@Component
//public class WebSocketTask {
//    @Autowired
//    private WebSocketServer webSocketServer;
//
//    /**
//     * 通过WebSocket每隔5秒向客户端发送消息
//     */
//// 每5秒执行一次，向所有客户端发送当前时间的消息
//
//    @Scheduled(cron = "0/5 * * * * ?")
////    @Scheduled(fixedRate = 10)
//    public void sendMessageToClient() {
//        // 获取当前时间并格式化为HH:mm:ss形式，然后发送消息
//        webSocketServer.sendToAllClient("这是来自服务端的消息：" + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()));
//    }
//
//}