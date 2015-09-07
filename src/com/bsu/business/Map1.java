package com.bsu.business;

import com.bsu.commport.CommMessage;
import com.bsu.commport.CommPortInstance;
import com.bsu.system.tool.JSONBSUConfig;
import com.bsu.system.tool.U;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 采用大块内存读取方式读取plc的数据
 * Created by fc on 2015/9/6.
 */
public class Map1 {
    private ArrayList<MapData> maps = new ArrayList<MapData>();
    private JSONBSUConfig jbc = null;
    private CommMessage currMessage;
    public Map1(){
        try{
            //初始化获得各种配置数据
            jbc = JSONBSUConfig.getInstance();
            JSONArray ja_map = JSONBSUConfig.getInstance().getWriteMapData1();
            for(int i=0;i<ja_map.length();i++){
                MapData md = new MapData();
                md.plcsend = ((JSONObject)ja_map.get(i)).getString("plcsend");                                       //发送的数据
                md.area = ((JSONObject)ja_map.get(i)).getString("area");                                              //查询的区域
                md.address = ((JSONObject) ja_map.get(i)).getJSONArray("address");                                   //所有要检索的地址
                maps.add(md);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        putMessages();
        receiveData();
    }

    private boolean putflag = true;
    private int currMapIndex = 0;                                                                                      //要检索的语句索引
    /**
     * 向串口消息队列中增加查询消息,分三种类型,I类型\O类型\W类型 3种
     */
    private void putMessages(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while(putflag){
                    //确定现在要检索maps中的哪个元素
                    if(currMapIndex >=0 && currMapIndex <maps.size()-1)
                        currMapIndex++;
                    else
                        currMapIndex = 0;
                    //生成向plc发送的指令,并放到发送队列中
                    String cmd = U.replaceFcs(maps.get(currMapIndex).plcsend);
                    long timestamp = System.currentTimeMillis();
//                    long timestamp = -1;
                    currMessage = new CommMessage(cmd,maps.get(currMapIndex).area,timestamp);
                    CommPortInstance.getInstance().putCommMessage(currMessage);

                    //暂停1秒再进行下一条命令的发送
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t_putMessage = new Thread(r);
        t_putMessage.start();
    }

    /**
     * 接收返回数据
     */
    private void receiveData(){
        CommPortInstance.getInstance().addCommPortReceiveListener(new CommPortInstance.CommPortReceiveListener(){
            @Override
            public void receive(CommMessage data) {
                if(data.timestamp == currMessage.timestamp){
                    String cmd = data.data;
                    String area = data.extdata;
                    switch(area){
                        case "O":
                            ParseOAreaData(cmd);
                            break;
                        case "I":
                            ParseIAreaData(cmd);
                            break;
                        case "W":
                            ParseWAreaData(cmd);
                            break;
                    }
                }
            }
        });
    }

    /**
     * 解析O区数据
     * @param data  plc返回数据
     */
    private void ParseOAreaData(String data){
//        "@00FA 00 40 00 00 00 0101 0000 1234 47*"

    }

    /**
     * 解析I区数据
     * @param data  plc返回数据
     */
    private void ParseIAreaData(String data){

    }

    /**
     * 解析W区数据
     * @param data
     */
    private void ParseWAreaData(String data){

    }

    /**
     * 要发送的地图的查询数据,包括发送命令,那个区的数据,和注释
     */
    class MapData{
        public String plcsend = "";                                                                                  //向plc发送的数据
        public String area = "";                                                                                      //查询的plc的区
        public JSONArray address = new JSONArray();                                                                  //每个区要检索的数据
    }
}


