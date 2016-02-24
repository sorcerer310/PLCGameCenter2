package com.bsu.business.data;

import com.bsu.system.tool.JSONBSUConfig;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 用来保存在plc中配置文件地址的状态
 * Created by fengchong on 2015/12/23.
 */
public class PLCRealTimeMonitorData {
    private static PLCRealTimeMonitorData instance = null;
    public static PLCRealTimeMonitorData getInstance(){
        if(instance==null)
            instance = new PLCRealTimeMonitorData();
        return instance;
    }

    /**
     * 初始化函数,根据配置文件中的数据,获得要监测的数据
     */
    private PLCRealTimeMonitorData(){
        //初始化所有地图配置数据的实时值
        try {
            allStateData = JSONBSUConfig.getInstance().makeAllMotionBooleanData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public HashMap<String,Boolean> allStateData = new HashMap<String,Boolean>();                                    //保存所有配置文件中地址通道的实时监控值
    private ArrayList<OnMonitorDataChangeListener> listeners = new ArrayList<OnMonitorDataChangeListener>();                                  //保存所有监听器

    /**
     * 设置当前地址的值
     * @param address   要设置的地址
     * @param b          设置的值
     */
    public void setVal(String address,boolean b){
        allStateData.put(address,b);
    }

    /**
     * 以byte类型参数来保存值,当参数为0时保存为false,参数为1时保存为true
     * @param address   要设置的区和地址，形式为"W50.00"
     * @param b          设置的值
     */
    public void setVal(String address,byte b){
        if(b==0 || b==1) {
            Boolean bs = b==0?false:true;
//            if(bs==allStateData.get(address)) {
                for(OnMonitorDataChangeListener listener:listeners)
                    listener.changed(address,bs);
                allStateData.put(address, b == 0 ? false : true);
//            }
        }
    }

    /**
     * 获得当前地址的值
     * @param address   要获得值的地址
     */
    public Boolean getVal(String address){
        String paddress = address.toUpperCase();                                                                        //如果区域字母为小写转为大写
        return allStateData.get(paddress);
    }

    /**
     * 用于监测监视数据是否有改变
     */
    public static interface OnMonitorDataChangeListener{
        public void changed(String key,Boolean state);                                                                //改变状态通知所有监听器
    }

    /**
     * 增加监听器到容器中
     * @param listener  要增加的监听器
     */
    public boolean addOnMonitorDataChangeListener(OnMonitorDataChangeListener listener){
        return listeners.add(listener);
    }

    /**
     * 移除监视数据的监听器
     * @param listener  要移除的监听器
     * @return
     */
    public boolean removeOnMonitorDataChangeListener(OnMonitorDataChangeListener listener){
        return listeners.remove(listener);
    }
}
