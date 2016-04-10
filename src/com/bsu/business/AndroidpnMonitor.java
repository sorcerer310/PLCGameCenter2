package com.bsu.business;

import com.bsu.business.data.AddressData;
import com.bsu.business.data.PLCRealTimeMonitorData;
import com.bsu.system.tool.JSONBSUConfig;
import com.bsu.system.tool.U;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;

/**
 * 根据配置文件负责监视数据是否有改变,如果有改变则发消息给在线手机
 * PLCRealTimeMonitorData类保存的为PLC的实时内存映射,此类的hm_adata保存的才是
 * 满足监视数据条件做发送消息操作的值
 * Created by fengchong on 2015/12/30.
 */
public class AndroidpnMonitor {
    private HashMap<String,AddressData> hm_adata = new HashMap<String,AddressData>();                                  //保存配置文件中所有监视地址的值
    private JSONBSUConfig jbc = null;

    public AndroidpnMonitor(){
        try {
            jbc = JSONBSUConfig.getInstance();
            hm_adata = JSONBSUConfig.getInstance().makeAllMotionData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始为Androidpn进行监视数据
     */
    public void startAndroidpnMonitor(){
        PLCRealTimeMonitorData.getInstance().addOnMonitorDataChangeListener(new PLCRealTimeMonitorData.OnMonitorDataChangeListener() {
            @Override
            public void changed(String key, Boolean state) {
                //如果当前值未操作过,并且配置文件的目的值和plc返回值相同,则向androidpn下的手机发送消息
                AddressData ad = hm_adata.get(key);
                if(!ad.opted && ad.expectedval_bool==state){
                    try {
                        for(String s:ad.androidpncmd)
                            U.sendPostRequestByForm(jbc.getAndroidpnUrl(), U.setParams(jbc.getAndroidpnUser(), jbc.getAndroidpnTitle(), ad.msg, s));
                        ad.opted = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
   }

    /**
     * 重设所有的地图标记,用于复原机关
     */
    public void resetAndroidpnMonitorFlags(){
        try {
            hm_adata = JSONBSUConfig.getInstance().makeAllMotionData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
