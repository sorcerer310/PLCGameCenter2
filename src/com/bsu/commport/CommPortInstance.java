package com.bsu.commport;

import com.bsu.commport.SerialReader;
import com.bsu.commport.SerialWriter;
import com.bsu.system.tool.JSONBSUConfig;
import org.json.JSONException;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import java.io.IOException;
import java.util.Enumeration;

/**
 * 用来操作串口类
 * @author fengchong
 *
 */
public class CommPortInstance {
	private static com.bsu.commport.CommPortInstance instance = null;
	public static com.bsu.commport.CommPortInstance getInstance(){
		if(instance==null)
			instance = new com.bsu.commport.CommPortInstance();
		return instance;
	}
	private CommPortInstance(){}
	
	private CommPortIdentifier portId;		//端口标识
	private Enumeration portList;				//端口列表
	
	private SerialPort serialPort;			//串口对象
	private SerialReader sreader;				//串口读取对象
	private SerialWriter swriter;				//串口写对象
	/**
	 * 初始化串口端口
	 */
	public void initCommPort(){
		portList = CommPortIdentifier.getPortIdentifiers();
		while(portList.hasMoreElements()){
			portId = (CommPortIdentifier) portList.nextElement();
			if(portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				try {
					if (portId.getName().equals(JSONBSUConfig.getInstance().getPort())) {
						serialPort = (SerialPort) portId.open("SerialReader", 2000);                                //获得串口对象
						sreader = new com.bsu.commport.SerialReader(serialPort);                                        //生成串口读取对象
						swriter = new SerialWriter(serialPort);                                                        //生成串口写入对象
						System.out.println("======================init comm port success");
						break;
					}
				} catch (PortInUseException | JSONException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public SerialReader getSerialReader(){return sreader;}
	public SerialWriter getSerialWriter(){return swriter;}
	/**
	 * 关闭串口
	 */
	public void closeSerialPort(){
		serialPort.close();
	}
}
