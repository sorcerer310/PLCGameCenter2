package com.bsu.business;

import com.bsu.commport.CommMessage;
import com.bsu.commport.CommPortInstance;
import com.bsu.commport.SerialWriter;
import com.bsu.system.tool.JSONBSUConfig;
import com.bsu.system.tool.U;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 用来处理处理所有循环的发送查询的代码
 * Created by fengchong on 2015/7/18.
 */
public class Map {
    private ArrayList<MapData> maps = new ArrayList<MapData>();
    private int currMapIndex = 0;
    private JSONBSUConfig jbc = null;
    private ArrayBlockingQueue<String> msgqueue = new ArrayBlockingQueue<String>(100);                                   //阻塞队列用于处理发送的消息
    private enum MSGSTATE {SEND,RECEIVE};                                                                           //消息的状态,分为发送与接收,系统根据状态,只能做发送或者接收一种状态的操作.用来模拟单工数据操作,保证数据完整性
    private MSGSTATE switchState = MSGSTATE.SEND;
    private CommMessage currMessage;                                                                                 //当前发送的串口命令,只有当前命令获得了返回值才会发送下一条,否则一直阻塞
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

        putMessages();                                                                                                  //启动发送消息的线程
        receiveData();                                                                                                  //设置接收数据的监听.
    }

    private boolean putflag = true;                                                                                 //增加消息到队列的线程标识
    /**
     * 开始向队列中增加查询消息
     */
    private void putMessages(){
        Runnable r = new Runnable(){
            @Override
            public void run() {
                while(putflag){
                    if(currMapIndex==-1){
                        putflag =false;
                        break;
                    }else{
                        try {

                            //当前状态为发送状态时才推送命令到命令队列中
//                            if(switchState==MSGSTATE.SEND) {
                                //当执行完14后,进入15和16后,循环检测16和15
                                if (currMapIndex == 16)
                                    currMapIndex = 15;
                                else if (currMapIndex == 15)
                                    currMapIndex = 16;

                                String cmd = U.replaceFcs(maps.get(currMapIndex).plcsend);
                                long timestamp = System.currentTimeMillis();
                                currMessage = new CommMessage(cmd, timestamp);
                                CommPortInstance.getInstance().putCommMessage(currMessage);                           //发送带时间戳命令
//                                switchState = MSGSTATE.RECEIVE;                                                      //把状态切换为接收
//                            }
                            Thread.currentThread().sleep(1000);                                                         //间隔1秒再发送下一条指令
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        };
        Thread t_putMessage = new Thread(r);
        t_putMessage.start();
    }

    /**
     * 执行接收数据的监听
     */
    public void receiveData(){
        CommPortInstance.getInstance().addCommPortReceiveListener(new CommPortInstance.CommPortReceiveListener() {
            @Override
            public void receive(CommMessage data) {
                //判断当前返回值是否为
                if(data.timestamp == currMessage.timestamp) {
                    String cmd = currMessage.data;
                    //如果收到的消息与当前对应的地图plcreceive消息一致,则向androidpn服务器发命令,
                    if(cmd.equals(maps.get(currMapIndex).plcreceive)) {
                        try {
                            //发送地图消息
                            U.sendPostRequestByForm(jbc.getAndroidpnUrl(), U.setParams(jbc.getAndroidpnUser(), jbc.getAndroidpnTitle(), jbc.getAndroidpnMsg(), "map:" + maps.get(currMapIndex).androidpncmd));

                            //当第4个脚踏灯踩亮时发送消息到手机,提示玩家放火
                            if(currMapIndex==7)
                                U.sendPostRequestByForm(jbc.getAndroidpnUrl(), U.setParams(jbc.getAndroidpnUser(), jbc.getAndroidpnTitle(), jbc.getAndroidpnMsg(), "fire:0"));
                                //当借东风完成发送消息到手机,提示玩家铁锁连环放火
                            else if(currMapIndex==13)
                                U.sendPostRequestByForm(jbc.getAndroidpnUrl(), U.setParams(jbc.getAndroidpnUser(), jbc.getAndroidpnTitle(), jbc.getAndroidpnMsg(), "fire:1"));
                                //当铁锁连环放完火,提示玩家选择追击道路.
                            else if(currMapIndex==14)
                                U.sendPostRequestByForm(jbc.getAndroidpnUrl(), U.setParams(jbc.getAndroidpnUser(), jbc.getAndroidpnTitle(), jbc.getAndroidpnMsg(), "followup"));

                            System.out.println("Map receive:"+cmd);
                            if (currMapIndex < maps.size() - 1) {
                                currMapIndex++;                                                                       //该条指令已收到正确数据,转到下一条
                            } else
                                currMapIndex = -1;

//                            switchState = MSGSTATE.SEND;                                                              //接收数据处理完后,转为发送状态

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });

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
