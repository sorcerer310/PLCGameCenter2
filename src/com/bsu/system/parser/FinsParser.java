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
            sb.append(write)
                    .append(w_bit)
                    .append("0001")                     //写入1位
                    .append(value)
                    .append(fcs);
            return sb.toString();
        }
        //读操作
        else if(readOrWrite.equals("read")){

        }
        return null;
    }
}
