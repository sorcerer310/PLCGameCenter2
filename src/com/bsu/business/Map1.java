package com.bsu.business;

import com.bsu.commport.CommMessage;
import com.bsu.commport.CommPortInstance;
import com.bsu.system.tool.JSONBSUConfig;
import com.bsu.system.tool.U;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.Map;

/**
 * 采用大块内存读取方式读取plc的数据
 * Created by fc on 2015/9/6.
 */
public class Map1 {
    private ArrayList<MapData> maps = new ArrayList<MapData>();
    private HashMap<String,MapData> hm_maps = new HashMap<String ,MapData>();
    private JSONBSUConfig jbc = null;
    private CommMessage currMessage;
    public Map1(){
        try{
            //初始化获得各种配置数据
            jbc = JSONBSUConfig.getInstance();
            JSONArray ja_map = JSONBSUConfig.getInstance().getWriteMapData1();
            for(int i=0;i<ja_map.length();i++){
                MapData md = new MapData();
                JSONObject jo = ((JSONObject)ja_map.get(i));
                md.plcsend = jo.getString("plcsend");                                                                 //发送的数据
                md.startunit = jo.getString("startunit");
                md.area = jo.getString("area");                                                                        //查询的区域

                //处理地址数据
                JSONArray ja_ad = jo.getJSONArray("address");                                                          //所有要检索的地址
                for(int j=0;j<ja_ad.length();j++) {
                    JSONObject jo_ad = ((JSONObject) ja_ad.get(j));
                    md.address.add(new AddressData(jo_ad.getString("ar"),jo_ad.getInt("val"),jo_ad.getString("androidpncmd")));
                }

                hm_maps.put(jo.getString("area"),md);                                                                 //数据按区开
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
    /**
     * 向串口消息队列中增加查询消息,分三种类型,I类型\O类型\W类型 3种
     */
    private void putMessages(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while(putflag){
                    //生成向plc发送的指令,并放到发送队列中
                    Iterator<Map.Entry<String,MapData>> it = hm_maps.entrySet().iterator();
                    while(it.hasNext()){
                        Map.Entry<String,MapData> entry = it.next();
                        String cmd = U.replaceFcs(entry.getValue().plcsend);
                        long timestamp = System.currentTimeMillis();

                        currMessage = new CommMessage(cmd,entry.getValue().area,timestamp);
                        CommPortInstance.getInstance().putCommMessage(currMessage);

                        //暂停1秒再进行下一条命令的发送
                        try {
                            Thread.currentThread().sleep(1000);
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
     * 接收返回数据
     */
    private void receiveData(){
        CommPortInstance.getInstance().addCommPortReceiveListener(new CommPortInstance.CommPortReceiveListener() {
            @Override
            public void receive(CommMessage data) {
                if (data.timestamp == currMessage.timestamp) {
                    String cmd = data.data;
                    String area = data.extdata.split("|")[0];                                                           //内存区

                    switch (area) {
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
//      cmd send:   @00FA0000000000101B0006600000603*
//      ReceiveData:@00FA004000000001010000003B0001000800000008000033*
//      0	0	0	0	0	0	0	0	0	0	1	1	1	0	1	1   003B
//      0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	1   0001
//      0	0	0	0	0	0	0	0	0	0	0	0	1	0	0	0   0008
//      0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0   0000
//      0	0	0	0	0	0	0	0	0	0	0	0	1	0	0	0   0008
//      0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
        ParseAreaData("O",data);
    }

    /**
     * 解析I区数据
     * @param data  plc返回数据
     */
    private void ParseIAreaData(String data){
//      cmd send:  @00FA0000000000101B0000000000104*
//      ReceiveData:@00FA004000000001010000000447*
//      0	0	0	0	0	0	0	0	0	0	0	0	0	1	0	0	0004
        ParseAreaData("I",data);
    }

    /**
     * 解析W区数据
     * @param data
     */
    private void ParseWAreaData(String data){
//      cmd send:  @00FA0000000000101B100020000090F*
//      ReceiveData:@00FA0040000000010100000000000000000000000000000000000000084B*
//      0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
//      0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
//      0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
//      0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
//      0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
//      0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
//      0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
//      0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
//      0	0	0	0	0	0	0	0	0	0	0	0	1	0	0	0	0008
        ParseAreaData("W",data);
    }

    private void ParseAreaData(String memoryArea,String data){
        MapData md = hm_maps.get(memoryArea);
        HashMap<String,byte[]> hm_unit = U.subPLCResponseData(md.startunit,data);
        ArrayList<AddressData> al_ad = md.address;                                                                     //所有要处理的地址
        for(int i=0;i<al_ad.size();i++){
            try {
                AddressData ad =  al_ad.get(i);
                //如果当前数据已经处理过了则跳过该条数据
                if(ad.opted)
                    continue;

                String androidpncmd = "map:"+ad.androidpncmd;                                                        //向androidpn服务器发送的命令
                String address = ad.ar;                                                                                 //获得当前要检索的地址
                String[] saddress = address.split(".");                                                                 //将地址拆成两部分
                String unit = saddress[0];                                                                              //第一部分通道地址
                int bit = Integer.valueOf(saddress[1]);                                                                 //第二部分位

                byte v = hm_unit.get(unit)[bit];
                if(v==ad.val){
                    U.sendPostRequestByForm(jbc.getAndroidpnUrl(), U.setParams(jbc.getAndroidpnUser(), jbc.getAndroidpnTitle(), jbc.getAndroidpnMsg(), androidpncmd));
                    ad.opted = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 要发送的地图的查询数据,包括发送命令,那个区的数据,和注释
     */
    class MapData{
        public String plcsend = "";                                                                                  //向plc发送的数据
        public String startunit = "";                                                                                //从哪个通道开始读取数据
        public String area = "";                                                                                      //查询的plc的区
        public ArrayList<AddressData> address = new ArrayList<AddressData>();                                        //每个区要检索的数据
    }

    /**
     * 要判断的地址数据
     */
    class AddressData{
        public String ar = "";
        public int val = -1;
        public String androidpncmd = "";
        public boolean opted = false;                                                                               //是否已处理过该数据
        public AddressData(String par,int pval,String apc){
            ar = par;
            pval = val;
            androidpncmd = apc;
        }
    }
}


