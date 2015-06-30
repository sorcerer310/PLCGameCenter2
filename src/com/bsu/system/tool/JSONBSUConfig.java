package com.bsu.system.tool;

import org.androidpn.server.util.ConfigManager;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.stream.util.StreamReaderDelegate;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * json�������ļ�,��ȡbsuconfig.json������
 * Created by FC on 2015/6/30.
 */
public class JSONBSUConfig {
    private static JSONBSUConfig instance = null;
    private JSONObject jo_cfg;

    private String androidpnUrl,androidpnUser;	                                 //�˴�һ��Ҫָ���û�

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

        //ת��receivedata����
        JSONObject jo_recdata = jo_cfg.getJSONObject("receivedata");
        Iterator<String> it = jo_recdata.keys();
        while(it.hasNext()) {
            String key = it.next();
            recPlcData.put(key,jo_recdata.getString(key));
        }

        //ָ��androidpn��������url��Ҫ���͵��û�
        JSONObject jo_androidpn = jo_cfg.getJSONObject("androidpn");
        androidpnUrl = jo_androidpn.getString("androidpnUrl");
        androidpnUser = jo_androidpn.getString("androidpnUser");


    }

    /**
     * ���androidpn��������ַ
     * @return  ����androidpn��������ַ
     */
    public String getAndroidpnUrl(){
        return androidpnUrl;
    }

    /**
     * ���Ҫ������Ϣ���û�
     * @return  ����Ҫͨ��androidpn������Ϣ���û�
     */
    public String getAndroidpnUser(){
        return androidpnUser;
    }

    /**
     * ��ý���PLC������
     * @return   ���ؽ��յ���PLC������
     */
    public HashMap<String, String> getRecPlcData() {
        return recPlcData;
    }
}
