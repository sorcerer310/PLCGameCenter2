package com.bsu.business;

import com.bsu.commport.SerialReader;
import com.bsu.commport.SerialWriter;
import com.bsu.system.tool.JSONBSUConfig;
import com.bsu.system.tool.U;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 用来处理地图的业务代码
 * Created by fengchong on 2015/7/18.
 */
public class Map implements BusinessAdapter.IBusiness{
    private ArrayList<MapData> maps = new ArrayList<MapData>();
    private int currMapIndex = 0;
    private JSONBSUConfig jbc = null;
    public Map(){

        try {
            //初始化获得各种配置数据
            jbc = JSONBSUConfig.getInstance();
            JSONArray ja_map = JSONBSUConfig.getInstance().getWriteMapsData();
            for(int i=0;i<ja_map.length();i++){
                MapData md = new MapData();
                md.plcreceive = ((JSONObject)ja_map.get(i)).getString("plcreceive");
                md.plcsend = ((JSONObject)ja_map.get(i)).getString("plcsend").replace(" ","");
                md.androidpncmd = ((JSONObject)ja_map.get(i)).getString("androidpncmd");
                maps.add(md);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean tstopflag = true;                                       //线程是否停止标识

    /**
     * 获得发送数据对象,向串口发送数据
     * @param sw
     */
    public void sendData(SerialWriter sw){
        final SerialWriter serialWriter = sw;
        Runnable r = new Runnable(){
            @Override
            public void run() {
                while(tstopflag) {
                    if(currMapIndex == -1) {
                        tstopflag = false;
                        break;
                    }
                    try {
                        //每500毫秒发送一次查询指令
                        serialWriter.writeCommand(U.replaceFcs(maps.get(currMapIndex).plcsend).getBytes());
                        System.out.println("Map send:" + currMapIndex + "  " + U.replaceFcs(maps.get(currMapIndex ).plcsend));
                        Thread.currentThread().sleep(500);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        new Thread(r).start();

    }

    @Override
    public void receiveData(byte[] data) {

    }

    @Override
    public void receiveData(String cmd){
//        System.out.println("===============Map:"+cmd);

        //如果收到的消息与当前对应的地图plcreceive消息一致,则向androidpn服务器发命令,
        // 并切换到下一条命令继续循环发送
        if(cmd.equals(maps.get(currMapIndex).plcreceive)) {
            try {
                U.sendPostRequestByForm(jbc.getAndroidpnUrl(), U.setParams(jbc.getAndroidpnUser(), jbc.getAndroidpnTitle(), jbc.getAndroidpnMsg(), "map:" + maps.get(currMapIndex).androidpncmd));
                System.out.println("Map receive:"+cmd);
                if (currMapIndex < maps.size() - 1) {
                    currMapIndex++;                                                                                       //该条指令已收到正确数据,转到下一条
                } else
                    currMapIndex = -1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

/**
 * 地图各关键点位置
 */
class MapData {
    public String plcsend = "";
    public String plcreceive = "";
    public String androidpncmd = "";
}
