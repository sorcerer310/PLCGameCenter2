package com.bsu.business;

import java.util.HashMap;

/**
 * 用来保存plc中一些地址的状态
 * Created by fengchong on 2015/12/23.
 */
public class ResponseAddressData {
    public static HashMap<String,Boolean> allState = new HashMap<String,Boolean>();
    static{
        allState.put("dumpIsReady",false);                                                                          //记录鼓是否准备好
    }
}
