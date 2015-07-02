package com.bsu.servlet;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bsu.commport.CommPortInstance;
import com.bsu.commport.SerialReaderListener;
import com.bsu.system.tool.JSONBSUConfig;
import com.bsu.system.tool.U;
import org.json.JSONException;

/**
 * 接收串口数据的servlce,该servlte随tomcat启动,并在init函数中初始化串口的初始化操作,只执行一次.
 * 其他servlce需要使用串口时只需要获得CommPortInstance的实例就行,不必再对串口进行初始化
 * Servlet implementation class PLC_ReceiveSerial
 */
//@WebServlet(description = "接收串口数据到程序中", urlPatterns = { "/PLC_ReceiveSerial" },loadOnStartup = 5)
public class PLC_ReceiveSerial extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private CommPortInstance cpi = null;
	private ServletConfig pconfig;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PLC_ReceiveSerial() {
        super();
    }

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		//1:初始化JSONBSUConfig配置数据
		try {
			JSONBSUConfig.getInstance();
		} catch (IOException | JSONException e) {
			config.getServletContext().log(e.getMessage());
		}

		//3:初始化串口监听部分
		System.out.println("PLC_ReceiveSerial is init");
		config.getServletContext().log("======================PLC_ReceiveSerial is init");
		cpi = CommPortInstance.getInstance();
		cpi.initCommPort();
		if(cpi.getSerialReader()==null){
			System.out.println("comm port init fail");
			config.getServletContext().log("======================PLC_ReceiveSerial comm port init fail");
			return;
		}
		
//		final ServletConfig sc = config;
		pconfig = config;
		cpi.getSerialReader().setSerialReaderListener(new SerialReaderListener(){

			@Override
			public void readCompleted(String command) {
				// TODO Auto-generated method stub
			}

			@Override
			public void readCompleted(int command) {
				// TODO Auto-generated method stub
//				System.out.println(command);
			}

			@Override
			public void readCompleted(byte command) {
				pconfig.getServletContext().log("================comm port readCompleted:" + command);
				try {
					JSONBSUConfig cfg = JSONBSUConfig.getInstance();
					Iterator<String> it = cfg.getRecPlcData().keySet().iterator();
					//如果有匹配配置文件里的内容则向androidpn服务器发送对应的数据
					while(it.hasNext()){
						String key = it.next();
						if(key.equals(String.valueOf((int)command)))
							U.sendPostRequestByForm(cfg.getAndroidpnUrl()
									, U.setParams(cfg.getAndroidpnUser(), cfg.getAndroidpnTitle(),cfg.getAndroidpnMsg(),cfg.getRecPlcData().get(key)));
					}
				} catch (Exception e) {
					PLC_ReceiveSerial.this.pconfig.getServletContext().log(e.getMessage());
				}

				System.out.println("command:"+command);
			}
		});
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
