package com.bsu.commport;

/**
 * 串口消息包括时间戳,用来确定是哪一条消息
 * Created by FC on 2015/8/5.
 */
public class CommMessage {
    public String data;
    public long timestamp;
    public CommMessage(String d,long t){
        data = d;
        timestamp = t;
    }
}
