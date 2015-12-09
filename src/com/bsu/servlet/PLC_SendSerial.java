package com.bsu.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bsu.commport.CommMessage;
import com.bsu.commport.CommPortInstance;
import com.bsu.commport.SerialWriter;
import com.bsu.system.parser.FinsParser;
import com.bsu.system.tool.JSONBSUConfig;
import com.bsu.system.tool.U;
import org.json.JSONException;

/**
 * 用于向PLC发送串口数据,一般不需要返回值的时候用该servlet操作
 * 该servlet需要带入一个plccmd参数,参数值在bsuconfig.json文件的writedata节点获得。
 * Servlet implementation class PLC_SendSerial
 */
public class PLC_SendSerial extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private CommPortInstance cpi = null;
    private SerialWriter sw = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PLC_SendSerial() {
        super();

    }

	@Override
	public void init(ServletConfig config) throws ServletException {
		cpi = CommPortInstance.getInstance();
		sw = cpi.getSerialWriter();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	private String type = null;																							//指令类型:
																														//click:发送指令成功响应后，马上发送一个取反值
																														//nomal:发送指令后不做任何操作
																														//h-bridgeH桥指令,用于推拉杆、电机正反转操作

	private String area = null;																							//操作区域
	private String address1 = null;																						//操作地址
	private String address2 = null;																						//操作地址2，用于H桥电路的操作，普通操作可以不用
	private String value = null;																						//要操作的值
	private String readOrWrite = null;																					//读或写数据
	protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		String plccommand = U.getRS(request,"plccmd");						//plc指令参数

		String type = U.getRS(request,"type");

		area = U.getRS(request,"area");
		address1 = U.getRS(request,"address1");
		address2 = U.getRS(request,"address2");
		value = U.getRS(request,"value");
		readOrWrite = U.getRS(request,"readOrWrite");

		if(sw==null){
			U.p(response, "PLC_SendSerial comm port init fail");
			return;
		}

		responseContext = response;											//活的全局实用的response对象

		try{
			if(plccommand!=null && !plccommand.equals("")) {
				JSONBSUConfig cfg = JSONBSUConfig.getInstance();

				//合并3部分向plc发送的数据
				HashMap<String, String> writedata = cfg.getWriteAllData();                                                    //writedata节点中所有的数据

				Iterator<String> it = writedata.keySet().iterator();
				//如果有匹配配置文件里的内容则向PLC发送对应的c-mode命令或FINS命令
				while (it.hasNext()) {
					String key = it.next();
					if (key.equals(plccommand)) {
						String wdata = writedata.get(key).toString();
						//如果字符串最后有fcs标,要将该标记替换成fcs校验码和结束符.
//					if(wdata.substring(wdata.length()-3,wdata.length()).equals("fcs"))
//						wdata = wdata.substring(0,wdata.length()-3)+U.fcs(wdata.substring(0,wdata.length()-3))+"*\r";
						wdata = U.replaceFcs(wdata);
						CommPortInstance.getInstance().putCommMessage(new CommMessage(wdata, "", -1));                            //只用于发送数据,时间戳设置为-1,不需要获得返回数据

						System.out.println("===================send:" + key + "=" + wdata);
						U.p(response, "===================send:" + key + "=" + wdata);
					}
				}
			}else if(type!=null && !type.equals("")){
				switch(type){
					case "nomal":{
						String finsdata = FinsParser.makeFinsData(area,address1,value,readOrWrite);						//生成fins指令
						timestamp = System.currentTimeMillis();															//获得当前的时间戳
						CommPortInstance.getInstance().putCommMessage(new CommMessage(finsdata,"",timestamp));			//发送指令到消息队列中
//						CommPortInstance.getInstance().addCommPortReceiveListener(listener);							//增加监听器监听返回数据
						break;
					}
					case "click":{
						String finsdata = FinsParser.makeFinsData(area,address1,value,readOrWrite);						//生成fins指令
						timestamp = System.currentTimeMillis();
						CommPortInstance.getInstance().putCommMessage(new CommMessage(finsdata,"",timestamp));			//发送指令到消息队列中
						CommPortInstance.getInstance().addCommPortReceiveListener(listener);							//增加监听器监听返回数据
						break;
					}
					case "h-bridge":{
						String resetdata = FinsParser.makeFinsData(area,address2,"00",readOrWrite);						//先写复位的数据
						timestamp = System.currentTimeMillis();
						CommPortInstance.getInstance().putCommMessage(new CommMessage(resetdata,"",timestamp));			//发送数据
						CommPortInstance.getInstance().addCommPortReceiveListener(listener);							//增加监听器返回数据
						break;
					}
				}

//				System.out.println("================send finsdata:" + finsdata);
//				U.p(response,"================send finsdata:"+finsdata);
			}
		}catch(JSONException | IOException e){
			e.printStackTrace();
		}

	}

	private long timestamp = -1;
	private HttpServletResponse responseContext = null;
	/**
	 * 对返回数据进行监听
	 */
	private CommPortInstance.CommPortReceiveListener listener = new CommPortInstance.CommPortReceiveListener() {
		@Override
		public void receive(CommMessage data) {
			if(data.timestamp!=-1 && data.timestamp == timestamp){
				System.out.println("receive data,timestamp:"+timestamp+" data:"+data.data);
				switch(type){
					case "nomal":{
						break;
					}
					case "click":{
						//如果返回值成功
						if(data.data.equals("")){
							//如果返回值成功自动发送一个取反的值
							String finsdata = FinsParser.makeFinsData(area,address1,FinsParser.backValue(value),readOrWrite);
							CommPortInstance.getInstance().putCommMessage(new CommMessage(finsdata,"",-1));
						}
						break;
					}
					case "h-bridge":{
						//如果复位数据正确
						if(data.data.equals("")){
							String finsdata = FinsParser.makeFinsData(area,address1,value,readOrWrite);
							CommPortInstance.getInstance().putCommMessage(new CommMessage(finsdata,"",-1));
						}
						break;
					}
				}


				CommPortInstance.getInstance().removeCommPortRecerveListener(listener);
			}
		}
	};

}

