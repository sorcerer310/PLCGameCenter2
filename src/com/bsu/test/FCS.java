package com.bsu.test;

/**
 * ”√¿¥≤‚ ‘FCSÀ„∑®
 * Created by Administrator on 2015/7/3.
 */
public class FCS {
    public static void main(String[] args){
        System.out.println("hello FCS");
//        String data = "@00FA000000000010231000000000101";
        String data = "@00FA000000000010231000001000101";
        int q = 0;
        for(int i=0;i<data.length();i++){
            String s = data.substring(i,i+1);
            byte b = s.getBytes()[0];
            q = q^b;
        }
        System.out.println(Integer.toHexString(q));
    }
}
