package com.bsu.business;

import com.bsu.commport.CommPortInstance;
import com.bsu.commport.SerialReader;
import com.bsu.commport.SerialWriter;
import com.bsu.system.tool.JSONBSUConfig;
import com.bsu.system.tool.U;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 业务代码代理,用来接收串口数据并将数据发送到各个业务逻辑代码
 * Created by fengchong on 2015/7/18.
 */
public class BusinessAdapter {
    private static BusinessAdapter instance = null;
    public static BusinessAdapter getInstance(){
        if(instance==null)
            instance = new BusinessAdapter();
        return instance;
    }
    private BusinessAdapter(){
        map = new Map();
        businesses.add(map);
    }

    //业务代码对象容器
    private ArrayList<IBusiness> businesses = new ArrayList<IBusiness>();
    private SerialReader sreader ;
    private SerialWriter swriter;
    private Map map ;

    /**
     * 设置串口的读写对象
     * @param sr
     * @param sw
     */
    public void setSerialReaderWriter(SerialReader sr,SerialWriter sw){
        sreader = sr;
        swriter = sw;
        map.sendData(sw);
    }
    /**
     * 收到数据后转发给所有业务对象
     * @param data
     */
    public void receive(byte[] data){

        //将字节数组转为字符串命令
        String strcmd = new String(data);
        for(IBusiness ib:businesses){
            ib.receiveData(data);                                                                                       //发送字节数组到各个IBusiness对象中
            ib.receiveData(strcmd);                                                                                     //发送字符串命令到各个IBusiness对象中
        }
    }

    static interface IBusiness{
        void receiveData(byte[] data);
        void receiveData(String cmd);
    };
}
