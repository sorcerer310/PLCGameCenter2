package com.bsu.business.data;

import java.util.ArrayList;

/**
 * 按内存区区分的监视数据,其中每个区包含所有地址的数据
 * Created by fengchong on 2015/12/30.
 */
public class MonitorData {
    public String fins = "";                                                                                      //向plc发送的数据
    public String startunit = "";                                                                                //从哪个通道开始读取数据
    public String area = "";                                                                                      //查询的plc的区
    public ArrayList<AddressData> addressdatas = new ArrayList<AddressData>();                                   //每个区要检索的数据
}
