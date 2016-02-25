package com.bsu.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bsu.business.PLCMonitor;
import com.bsu.business.data.PLCRealTimeMonitorData;
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
	private String area = null;																							//操作区域
	private String address1 = null;																						//操作地址
	private String address2 = null;																						//操作地址2，用于H桥电路的操作，普通操作可以不用
	private String val1,val2 = null;																						//要操作的值
	private String readOrWrite = null;																					//读或写数据
	protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		String plccommand = U.getRS(request,"plccmd");						//plc指令参数

		String type = U.getRS(request,"type");

		area = U.getRS(request,"area");
		address1 = U.getRS(request,"address1");
		address2 = U.getRS(request,"address2");
		val1 = U.getRS(request,"val1");
		val2 = U.getRS(request,"val2");
		readOrWrite = U.getRS(request,"readOrWrite");

		if(sw==null){
			U.p(response, "PLC_SendSerial comm port init fail");
			return;
		}

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

						//如果当前指令为success,判断O102.03的值为true不执行if下方的发送指令操作.继续执行while循环中的下一条内容
						//只有当前指令为success,并且O102.03的值为false时才继续执行发送该指令数据
						if(plccommand.equals("success") && PLCRealTimeMonitorData.getInstance().getVal("O102.03")==true){
							continue;
						}


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
						String finsdata = FinsParser.makeFinsData(area,address1,val1,readOrWrite);						//生成fins指令
						CommPortInstance.getInstance().putCommMessage(new CommMessage(finsdata,"",-1));					//发送指令到消息队列中
//						CommPortInstance.getInstance().addCommPortReceiveListener(listener);							//增加监听器监听返回数据
						break;
					}
					case "click":{
						String finsdata = FinsParser.makeFinsData(area,address1,val1,readOrWrite);						//生成fins指令
						CommPortInstance.getInstance().putCommMessage(new CommMessage(finsdata,"",-1));					//发送指令到消息队列中

						//再发送一个取反值,模拟click操作
						String finsdataback = FinsParser.makeFinsData(area,address1,FinsParser.backValue(val1),readOrWrite);
						CommPortInstance.getInstance().putCommMessage(new CommMessage(finsdataback,"",-1));
						break;
					}
					case "h-bridge":{
						String resetdata = FinsParser.makeFinsData(area,address2,val2,readOrWrite);						//先写复位的数据
						CommPortInstance.getInstance().putCommMessage(new CommMessage(resetdata,"",-1));				//发送数据

						String finsdata = FinsParser.makeFinsData(area,address1,val1,readOrWrite);						//再写入要操作的数据
						CommPortInstance.getInstance().putCommMessage(new CommMessage(finsdata,"",-1));					//发送数据
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
}

