package com.bsu.system.tool;

import org.androidpn.server.util.ConfigManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.stream.util.StreamReaderDelegate;
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

    private HashMap<String,String> recPlcData = new HashMap<String,String>();                                        //plc接收数据
    private JSONArray writeMapData = new JSONArray();                                                               //plc星星写入查询数据
    private HashMap<String,String> writeStarData = new HashMap<String,String>();                                    //plc星星数据写入
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

        //转化receivedata数据
        JSONObject jo_recdata = jo_cfg.getJSONObject("receivedata");
        Iterator<String> it_rec = jo_recdata.keys();
        while(it_rec.hasNext()) {
            String key = it_rec.next();
            recPlcData.put(key,jo_recdata.getString(key));
        }

        //转化星星写入数据
        JSONObject jo_wdata = jo_cfg.getJSONObject("writedata").getJSONObject("stars");
        Iterator<String> it_write = jo_wdata.keys();
        while(it_write.hasNext()){
            String key = it_write.next();
            String data = jo_wdata.getString(key);
            data = data.replace(" ","");
            writeStarData.put(key,data);
        }

        //转化地图写入数据
        writeMapData = jo_cfg.getJSONObject("writedata").getJSONArray("maps");

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

    public String getAndroidpnUrl(){return androidpnUrl;}
    public String getAndroidpnUser(){return androidpnUser;}
    public String getAndroidpnTitle(){return androidpnTitle;}
    public String getAndroidpnMsg(){return androidpnMsg;}

    public HashMap<String, String> getRecPlcData() {return recPlcData;}
    public HashMap<String,String> getWriteStarData(){ return writeStarData;}
    public JSONArray getWriteMapsData(){return writeMapData;}

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
