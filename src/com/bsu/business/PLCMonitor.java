package com.bsu.business;

import com.bsu.business.data.AddressData;
import com.bsu.business.data.MonitorData;
import com.bsu.business.data.PLCRealTimeMonitorData;
import com.bsu.commport.CommMessage;
import com.bsu.commport.CommPortInstance;
import com.bsu.system.tool.JSONBSUConfig;
import com.bsu.system.tool.U;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.*;

/**
 * 采用大块内存读取方式读取plc的数据
 * Created by fc on 2015/9/6.
 */
public class PLCMonitor {
    private HashMap<String,MonitorData> hm_monitordata = new HashMap<String ,MonitorData>();
    private JSONBSUConfig jbc = null;
    private CommMessage currMessage;
    public PLCMonitor(){
        try{
            //初始化获得各种配置数据,monitordata监视数据,sendcommand发送命令,androidpn配置数据,commport串口配置数据
            jbc = JSONBSUConfig.getInstance();
            JSONArray ja_map = JSONBSUConfig.getInstance().getWriteMonitorData();
            hm_monitordata = jbc.makeMapDatas(ja_map);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        startMonitor();                                                                                                  //发送指令
        receiveData();                                                                                                  //接收数据
    }



    private boolean putflag = true;
    /**
     * 向串口消息队列中增加查询消息,分三种类型,I类型\O类型\W类型 3种
     */
    private void startMonitor(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while(putflag){
                    //生成向plc发送的指令,并放到发送队列中
                    Iterator<java.util.Map.Entry<String,MonitorData>> it = hm_monitordata.entrySet().iterator();
                    while(it.hasNext()){
                        java.util.Map.Entry<String,MonitorData> entry = it.next();
                        String cmd = U.replaceFcs(entry.getValue().fins);                                               //获得查询plc状态的指令
                        long timestamp = System.currentTimeMillis();

                        currMessage = new CommMessage(cmd,entry.getValue().area,timestamp);
                        CommPortInstance.getInstance().putCommMessage(currMessage);                                   //将消息发送到消息队列

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
                    String cmd = data.data;                                                                             //返回的数据
                    String area = data.extdata;                                                                        //返回的额外数据,保存了内存区标识
                    ParseAreaData(area,cmd);                                                                            //解析每个区的数据
                }
            }
        });
    }

    /**
     * 解析各个数据区的数据
     * O区数据举例:
     * cmd send:   @00FA0000000000101B0006600000603*
     * ReceiveData:@00FA004000000001010000003B0001000800000008000033*
     * 0	0	0	0	0	0	0	0	0	0	1	1	1	0	1	1   003B
     * 0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	1   0001
     * 0	0	0	0	0	0	0	0	0	0	0	0	1	0	0	0   0008
     * 0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0   0000
     * 0	0	0	0	0	0	0	0	0	0	0	0	1	0	0	0   0008
     * 0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
     * ---------------------------------------------------------------------
     * I区数据举例:
     * cmd send:  @00FA0000000000101B0000000000104*
     * ReceiveData:@00FA004000000001010000000447*
     * 0	0	0	0	0	0	0	0	0	0	0	0	0	1	0	0	0004
     * ---------------------------------------------------------------------
     * W区数据举例
     * cmd send:  @00FA0000000000101B100020000090F*
     * ReceiveData:@00FA0040000000010100000000000000000000000000000000000000084B*
     * 0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
     * 0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
     * 0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
     * 0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
     * 0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
     * 0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
     * 0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
     * 0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0	0000
     * 0	0	0	0	0	0	0	0	0	0	0	0	1	0	0	0	0008
     *
     * @param memoryArea    内存区标识
     * @param cmd            向该区发送的查询指令
     */
    private void ParseAreaData(String memoryArea,String cmd){
        MonitorData md = hm_monitordata.get(memoryArea);                                                              //获得当前区要监控的所有状态
        HashMap<String,byte[]> hm_unit = U.parsePLCResponseData(md.startunit, cmd);                                   //解析PLC返回监控查询的数据
        ArrayList<AddressData> al_ad = md.addressdatas;                                                               //所有要监控的地址通道值
        for(int i=0;i<al_ad.size();i++){
//            try {
                AddressData ad =  al_ad.get(i);                                                                         //处理一条通道值
                String address = ad.ar;                                                                                 //获得当前要检索的地址.通道。例如0.11
                String[] saddress = address.split("\\.");                                                              //拆成两部分,前半部分为地址,后半部分为通道
                String unit = saddress[0];                                                                              //第一部分地址      0
                int bit = Integer.valueOf(saddress[1]);                                                                 //第二部分通道位   11
                byte v = hm_unit.get(unit)[bit];                                                                        //该地址通道的值   通常为1或0，表示接通或未接通
                PLCRealTimeMonitorData.getInstance().setVal(memoryArea.toUpperCase()+address,v);                        //保存当前地址的值，将区与地址还有值一起保存 "W50.00",true

//                //如果当前数据已经处理过了则跳过该条数据
//                if(ad.opted)
//                    continue;
//                //否则如果当前的值达到了配置文件中的期望值,命令androidpn服务器向手机发送命令
//                if(v==ad.expectedval){
//                    U.sendPostRequestByForm(jbc.getAndroidpnUrl(), U.setParams(jbc.getAndroidpnUser(), jbc.getAndroidpnTitle(), ad.msg, ad.androidpncmd));
//                    ad.opted = true;
//                }

//            } catch (JSONException e) {
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }
}


