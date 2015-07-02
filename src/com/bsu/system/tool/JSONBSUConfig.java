package com.bsu.system.tool;

import org.androidpn.server.util.ConfigManager;
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
    private String androidpnUrl,androidpnUser,title,msg = "";                                                    //一定要指定用户,否则发送消息不好用

    //串口参数
    private String port = "COM2";
    private int baudrate = 9600;                                                                                    //比特率
    private int databits = 7;                                                                                       //数据位
    private int stopbits = 2;                                                                                       //停止位
    private int parity = 2;                                                                                         //奇偶校验,2为偶校验

    private HashMap<String,String> recPlcData = new HashMap<String,String>();
    private HashMap<String,String> writePlcData = new HashMap<String,String>();
    public static JSONBSUConfig getInstance() throws IOException,JSONException{
        if(instance==null)
            instance = new JSONBSUConfig();
        return instance;
    }

    private JSONBSUConfig() throws IOException,JSONException{
//        System.out.println(getClass().getClassLoader());
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("bsuconfig.json");
        InputStreamReader sr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(sr);
        String line = null;
        StringBuffer sb = new StringBuffer();
        while ((line = br.readLine()) != null)
            sb.append(line);

        jo_cfg = new JSONObject(sb.toString());

        //转化receivedata数据
        JSONObject jo_recdata = jo_cfg.getJSONObject("receivedata");
        Iterator<String> it = jo_recdata.keys();
        while(it.hasNext()) {
            String key = it.next();
            recPlcData.put(key,jo_recdata.getString(key));
        }

        //指定androidpn服务器的url和要发送的用户
        JSONObject jo_androidpn = jo_cfg.getJSONObject("androidpn");
        androidpnUrl = jo_androidpn.getString("androidpnUrl");
        androidpnUser = jo_androidpn.getString("androidpnUser");
        title = jo_androidpn.has("title")==true?"":jo_androidpn.getString("title");
        msg = jo_androidpn.has("msg")==true?"":jo_androidpn.getString("msg");

        //设置串口配置数据
        JSONObject jo_commport = jo_cfg.getJSONObject("commport");
        port = jo_commport.getString("port");
        baudrate = jo_commport.getInt("baudrate");
        databits = jo_commport.getInt("databits");
        stopbits = jo_commport.getInt("stopbits");
        parity = jo_commport.getInt("parity");
    }

    /**
     * 获得androidpn服务器地址
     * @return  返回androidpn服务器地址
     */
    public String getAndroidpnUrl(){
        return androidpnUrl;
    }

    /**
     * 获得要发送消息的用户
     * @return  返回要通过androidpn发送消息的用户
     */
    public String getAndroidpnUser(){
        return androidpnUser;
    }

    /**
     * 获得接受PLC的数据
     * @return   返回接收到的PLC的数据
     */
    public HashMap<String, String> getRecPlcData() {
        return recPlcData;
    }

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
