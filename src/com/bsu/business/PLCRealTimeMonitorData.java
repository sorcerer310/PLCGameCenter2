package com.bsu.business;

import com.bsu.system.tool.JSONBSUConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    private PLCRealTimeMonitorData(){
        //初始化所有地图配置数据的实时值
        JSONBSUConfig config = null;
        try {
            config = JSONBSUConfig.getInstance();
            JSONArray ja_areas = config.getWriteMonitorData();
            for(int i=0;i<ja_areas.length();i++){
                JSONObject jo_area = ja_areas.getJSONObject(i);
                JSONArray ja_address = jo_area.getJSONArray("address");
                for(int j=0;j<ja_address.length();j++){
                    JSONObject jo_ar = ja_address.getJSONObject(j);
                    //当前地址通道值为0时保存为true,为1时保存为false
                    //此处取反值是因为配置文件中保存的值为androidpn服务器发送消息的目标值
                    allStateData.put(jo_ar.getString("ar"),jo_ar.getInt("expectedval")==0?true:false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public HashMap<String,Boolean> allStateData = new HashMap<String,Boolean>();                                               //保存所有配置文件中地址通道的实时监控值

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
     * @param address   要设置的地址
     * @param b          设置的值
     */
    public void setVal(String address,byte b){
        if(b==0 || b==1)
            allStateData.put(address,b==0?false:true);
    }

    /**
     * 获得当前地址的值
     * @param address   要获得值的地址
     */
    public Boolean getVal(String address){
        return allStateData.get(address);
    }


}
