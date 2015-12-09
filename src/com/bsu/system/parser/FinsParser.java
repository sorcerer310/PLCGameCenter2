package com.bsu.system.parser;

/**
 * fins指令解析
 * Created by fengchong on 15/12/9.
 */
public class FinsParser {
    private static String headdata = "@00FA000000000";             //头部数据
    private static String read = "0101";                           //读内存方式
    private static String write = "0102";                          //写内存方式
    private static String w_bit = "31";                            //以bit方式向w区写数据
    private static String r_bit = "B1";                            //以word方式读取w区数据
    private static String fcs = "fcs";                             //结尾

    /**
     * 生成fins指令
     * @param area          操作的区
     * @param address       指令
     * @param value         设置的值
     * @param readOrWrite   读或者写
     * @return              返回生成的指令
     */
    public static String makeFinsData(String area,String address,String value,String readOrWrite){
        //写操作
        if(readOrWrite.equals("write")){
            StringBuffer sb = new StringBuffer(headdata);
            sb.append(write)                            //写内存方式
                    .append(w_bit)                      //以bit方式向w区写数据
                    .append(address)                    //操作地址
                    .append("0001")                     //写入1位
                    .append(value)                      //要写入的值
                    .append(fcs);                       //fcs替换位
            return sb.toString();
        }
        //读操作
        else if(readOrWrite.equals("read")){
            StringBuffer sb = new StringBuffer(headdata);
            sb.append(read)
                    .append(r_bit)                      //读内存方式
                    .append(address)                    //操作地址
                    .append("0001")                     //读取1位
                    .append(fcs);                       //fcs替换位
        }
        return null;
    }

    /**
     * 如果值为00返回01，值为01返回00
     * @param value     操作的值
     * @return          取反后返回的值
     */
    public static String backValue(String value){
        if(value.equals("00"))
            return "01";
        else if(value.equals("01"))
            return "00";
        else
            return value;
    }
}
