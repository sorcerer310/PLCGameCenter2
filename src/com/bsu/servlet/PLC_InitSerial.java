package com.bsu.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bsu.business.Map;
import com.bsu.business.Map1;
import com.bsu.commport.CommPortInstance;
import com.bsu.system.tool.JSONBSUConfig;
import org.json.JSONException;
import sun.org.mozilla.javascript.internal.ast.TryStatement;

/**
 * 用来做串口串口初始化的servlte,该servlte随tomcat启动,只执行一次.
 * 其他servlte需要使用串口时只需要获得CommPortInstance的实例就行,不必再对串口进行初始化
 * Servlet implementation class PLC_InitSerial
 */
public class PLC_InitSerial extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private CommPortInstance cpi = null;
	public Map1 map;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PLC_InitSerial() {
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

		//2:初始化串口监听和业务代理对象
		config.getServletContext().log("======================PLC_InitSerial is initing");
		cpi = CommPortInstance.getInstance();																			//串口对象
		cpi.initCommPort();

		if(cpi.getSerialReader()==null){
			config.getServletContext().log("======================PLC_InitSerial comm port init fail,no SerialReader");
			return;
		}else{
			config.getServletContext().log("======================PLC_InitSerial comm port init success");
			//如果初始化成功,可执行一些循环执行的业务代码。例如地图查询业务代码.
//			Map map = new Map();
			map = new Map1();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
		try {
			map.resetMapFlags();
			response.getWriter().print("map init success");
		}catch (Exception e){
			response.getWriter().print("map init failed:"+e.getMessage());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
