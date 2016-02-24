package com.bsu.system.tool;

import com.bsu.business.data.AddressData;
import com.bsu.business.data.MonitorData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * json的配置文件,读取bsuconfig.json的内容
 * Created by FC on 2015/6/30.
 */
public class JSONBSUConfig {
    private static JSONBSUConfig instance = null;
    private JSONObject jo_cfg;

    //androidpn参数
    private String androidpnUrl,androidpnUser,androidpnTitle,androidpnMsg = "";                               //一定要指定用户,否则发送消息不好用

    //串口参数
    private String port = "COM2";
    private int baudrate = 9600;                                                                                     //比特率
    private int databits = 7;                                                                                        //数据位
    private int stopbits = 2;                                                                                        //停止位
    private int parity = 2;                                                                                          //奇偶校验,2为偶数

//    private HashMap<String,String> recPlcData = new HashMap<String,String>();                                         //plc接收数据
    private JSONArray writeMonitorData = new JSONArray();                                                              //plc新地图查询数据
    private HashMap<String,String> writeAllData = new HashMap<String,String>();                                     //所有的writeData结点中的数据

    public static JSONBSUConfig getInstance() throws IOException,JSONException{
        if(instance==null)
            instance = new JSONBSUConfig();
        return instance;
    }

    private JSONBSUConfig() throws IOException,JSONException{
//        System.out.println(getClass().getClassLoader());
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("bsuconfig.json");
        InputStreamReader sr = new InputStreamReader(inputStream,"UTF-8");
        BufferedReader br = new BufferedReader(sr);
        String line = null;
        StringBuffer sb = new StringBuffer();
        while ((line = br.readLine()) != null)
            sb.append(line);

        jo_cfg = new JSONObject(sb.toString());

        //便历sendcommand所有的发送命令的节点,并获得其中的JSONObject数据
        Iterator<String> keys = jo_cfg.getJSONObject("sendcommand").keys();
        while(keys.hasNext()){
            String key = keys.next();
            writeAllData.putAll(JSONObject2HashMap(jo_cfg.getJSONObject("sendcommand").getJSONObject(key)));
        }
        //转化新地图写入数据
        writeMonitorData = jo_cfg.getJSONObject("monitordata").getJSONArray("areas") ;

        //指定androidpn服务器的url和要发送的用户
        JSONObject jo_androidpn = jo_cfg.getJSONObject("androidpn");
        androidpnUrl = jo_androidpn.getString("androidpnUrl");
        androidpnUser = jo_androidpn.getString("androidpnUser");
        androidpnTitle = jo_androidpn.has("title")==true?"":jo_androidpn.getString("title");
        androidpnMsg = jo_androidpn.has("msg")==true?"":jo_androidpn.getString("msg");

        //设置串口配置数据
        JSONObject jo_commport = jo_cfg.getJSONObject("commport");
        port = jo_commport.getString("port");
        baudrate = jo_commport.getInt("baudrate");
        databits = jo_commport.getInt("databits");
        stopbits = jo_commport.getInt("stopbits");
        parity = jo_commport.getInt("parity");
    }

    /**
     * 将所有的监视数据转为按地址区分的数据结构
     * @return  将所有区的要监视PLC状态点都集合到一个hashmap对象中
     */
    public HashMap<String,AddressData> makeAllMotionData() throws JSONException {
        HashMap<String,AddressData> rethm = new HashMap<String,AddressData>();
        JSONArray ja_areas = getWriteMonitorData();
        for(int i=0;i<ja_areas.length();i++){
            JSONObject jo_area = ja_areas.getJSONObject(i);
            JSONArray ja_address = jo_area.getJSONArray("address");
            for(int j=0;j<ja_address.length();j++){
                JSONObject jo_ar = ja_address.getJSONObject(j);
                AddressData ad = new AddressData(jo_ar.getString("ar"),jo_ar.getInt("expectedval")
                        ,jo_ar.getString("androidpncmd"),jo_ar.isNull("msg")==false?jo_ar.getString("msg"):"");
                //当前地址通道值为0时保存为true,为1时保存为false
                rethm.put(jo_ar.getString("ar"),ad);
            }
        }
        return rethm;
    }

    /**
     * 返回所有地址的监视数据,数据以bool型保存,
     * @return
     * @throws JSONException
     */
    public HashMap<String,Boolean> makeAllMotionBooleanData() throws JSONException{
        HashMap<String,Boolean> rethm = new HashMap<String,Boolean>();
        JSONArray ja_areas = getWriteMonitorData();
        for(int i=0;i<ja_areas.length();i++){
            JSONObject jo_area = ja_areas.getJSONObject(i);
            JSONArray ja_address = jo_area.getJSONArray("address");
            for(int j=0;j<ja_address.length();j++){
                JSONObject jo_ar = ja_address.getJSONObject(j);
                //当前地址通道值为0时保存为true,为1时保存为false
                rethm.put(jo_ar.getString("ar"),jo_ar.getInt("expectedval")==1?true:false);
            }
        }
        return rethm;
    }

    /**
     * 将所有的监视数据转为按内存区区分的数据结构
     * @param jsondata
     * @return
     * @throws JSONException
     */
    public HashMap<String,MonitorData> makeMapDatas(JSONArray jsondata) throws JSONException {
        HashMap<String,MonitorData> hm = new HashMap<String,MonitorData>();
        for(int i=0;i<jsondata.length();i++){
            MonitorData md = new MonitorData();
            JSONObject jo = ((JSONObject)jsondata.get(i));
            md.fins = jo.getString("fins");                                                                            //发送的fins指令
            md.area = jo.getString("area");                                                                            //查询的区域
            md.startunit = jo.getString("startunit");                                                                //从该区域的当前地址开始查询

            //处理地址数据
            JSONArray ja_ad = jo.getJSONArray("address");                                                              //所有要检索的地址
            for(int j=0;j<ja_ad.length();j++) {
                JSONObject jo_ad = ((JSONObject) ja_ad.get(j));
                //如果msg不为空带入msg数据
                String msg = "";
                if(!jo_ad.isNull("msg"))
                    msg = jo_ad.getString("msg");
                md.addressdatas.add(new AddressData(jo_ad.getString("ar"), jo_ad.getInt("expectedval"), jo_ad.getString("androidpncmd"), msg));
            }
            hm.put(jo.getString("area"),md);                                                                           //数据按区保存不同区查询plc状态的指令
        }
        return hm;
    }

    /**
     * 将JSONObject数据转换为HashMap容器数据
     * @param jo    带入的json数据
     */
    private HashMap<String,String> JSONObject2HashMap(JSONObject jo) throws JSONException {
        HashMap<String,String> hm = new HashMap<String,String>();
        Iterator<String> it_write = jo.keys();
        while(it_write.hasNext()){
            String key = it_write.next();
            String data = jo.getString(key);
            data = data.replace(" ","");
            hm.put(key,data);
        }
        return hm;
    }


    public String getAndroidpnUrl(){return androidpnUrl;}
    public String getAndroidpnUser(){return androidpnUser;}
    public String getAndroidpnTitle(){return androidpnTitle;}
    public String getAndroidpnMsg(){return androidpnMsg;}

    public JSONArray getWriteMonitorData() {return writeMonitorData;}
    public HashMap<String, String> getWriteAllData() {return writeAllData;}

    /**
     * 端口配置数据
     * @return
     */
    public String getPort() {
        return port;
    }

    /**
     * 比特率配置数据
     * @return
     */
    public int getBaudrate() {
        return baudrate;
    }

    /**
     * 停止位数据
     * @return
     */
    public int getStopbits() {
        return stopbits;
    }

    /**
     * 数据位数据
     * @return
     */
    public int getDatabits() {
        return databits;
    }

    /**
     * 奇偶校验
     * @return
     */
    public int getParity() {
        return parity;
    }

}
