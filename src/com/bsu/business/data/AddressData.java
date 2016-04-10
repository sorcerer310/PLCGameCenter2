package com.bsu.business.data;

import java.util.ArrayList;

/**
 * 要监视的地址数据
 * Created by fengchong on 2015/12/30.
 */
public class AddressData {
    public String ar = "";                                                                                              //要检查的地址
    public int expectedval = -1;
    public boolean expectedval_bool = false;                                                                            //expectedval_bool的布尔值
    public ArrayList<String> androidpncmd = new ArrayList<>();
    public String msg = "";                                                                                           //要发送的消息
    public boolean opted = false;                                                                                   //是否已处理过该数据
    public AddressData(String par,int pval,ArrayList<String> apc,String pmsg){
        ar = par;
        expectedval = pval;
        expectedval_bool = expectedval==1?true:false;
        androidpncmd = apc;
        msg = pmsg;
    }
}
