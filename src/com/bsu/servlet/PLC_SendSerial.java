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

    }

	@Override
	public void init(ServletConfig config) throws ServletException {
//		System.out.println("PLC_SendSerial init");
		cpi = CommPortInstance.getInstance();
		sw = cpi.getSerialWriter();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		String plccommand = U.getRS(request,"plccmd");
		if(sw==null){
			U.p(response, "PLC_SendSerial comm port init fail");
			return;
		}

		try{
			JSONBSUConfig cfg = JSONBSUConfig.getInstance();
			HashMap<String,String> writedata = cfg.getWriteStarData();
			Iterator<String> it = cfg.getWriteStarData().keySet().iterator();
			//如果有匹配配置文件里的内容则向PLC发送对应的c-mode命令或FINS命令
			while(it.hasNext()){
				String key = it.next();
				if(key.equals(plccommand)) {
					String wdata = writedata.get(key).toString();
					//如果字符串最后有fcs标,要将该标记替换成fcs校验码和结束符.
					if(wdata.substring(wdata.length()-3,wdata.length()).equals("fcs"))
						wdata = wdata.substring(0,wdata.length()-3)+U.fcs(wdata.substring(0,wdata.length()-3))+"*\r";
					CommPortInstance.getInstance().putCommMessage(new CommMessage(wdata,-1));							//时间戳设置为-1,不需要获得返回数据
//					sw.writeCommand(wdata.getBytes());
//					this.getServletContext().log("===================send:" + key + "=" + writedata.get(key).toString());
					System.out.println("===================send:" + key + "=" + wdata);
					U.p(response,"===================send:"+key+"="+wdata);
				}
			}

//			cpi.closeSerialPort();
		}catch(JSONException | IOException e){
			e.printStackTrace();
//			this.getServletContext().log(e.getMessage());
		}
	}
}
