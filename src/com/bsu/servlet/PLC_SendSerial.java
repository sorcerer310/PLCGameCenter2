package com.bsu.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bsu.commport.CommPortInstance;
import com.bsu.commport.SerialWriter;
import com.bsu.system.tool.JSONBSUConfig;
import com.bsu.system.tool.U;
import org.json.JSONException;

/**
 * 用于向PLC发送串口数据
 * Servlet implementation class PLC_SendSerial
 */
//@WebServlet("/PLC_SendSerial")
public class PLC_SendSerial extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private CommPortInstance cpi = null;
    private SerialWriter sw = null;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PLC_SendSerial() {
        super();
        cpi = CommPortInstance.getInstance();
        sw = cpi.getSerialWriter();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		String plccommand = U.getRS(request,"plccmd");
		if(sw==null){
			U.p(response, "comm port init fail");
			return;
		}

		try{
			JSONBSUConfig cfg = JSONBSUConfig.getInstance();
			HashMap<String,String> writedata = new HashMap<String,String>();
			Iterator<String> it = cfg.getWritePlcData().keySet().iterator();
			//如果有匹配配置文件里的内容则向PLC发送对应的c-mode命令或FINS命令
			while(it.hasNext()){
				String key = it.next();
				if(key.equals(plccommand)) {
					sw.writeCommand(writedata.get(key).toString().getBytes());
					this.getServletContext().log("===================send:"+key+"="+writedata.get(key).toString());
					System.out.println("===================send:"+key+"="+writedata.get(key).toString());
				}
			}
		}catch(JSONException | IOException e){
			this.getServletContext().log(e.getMessage());
		}
	}
}
