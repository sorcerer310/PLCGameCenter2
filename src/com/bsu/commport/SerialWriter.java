package com.bsu.commport;

import com.bsu.commport.CommPortInstance;
import com.bsu.system.tool.JSONBSUConfig;
import org.json.JSONException;

import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 串口写数据对象
 * @author fengchong
 *
 */
public class SerialWriter {
	public final static byte[] WATCH_VIDEO_YES = {01};
	public final static byte[] WATCH_VIDEO_NO = {02};
	
	private SerialPort serialPort;
	private OutputStream outputStream;
	private JSONBSUConfig cfg = null;
	public SerialWriter(SerialPort sport){
		serialPort = sport;
		try {
			cfg = JSONBSUConfig.getInstance();
			outputStream = serialPort.getOutputStream();
			serialPort.setSerialPortParams(cfg.getBaudrate(),
					cfg.getDatabits(),
					cfg.getStopbits(),
					cfg.getParity());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 写入命令
	 * @param c		命令字节数组
	 * @throws IOException 
	 */
	public void writeCommand(byte[] c) throws IOException{
		outputStream.write(c);
	}
	
	public static void main(String[] args){
		CommPortInstance cp = CommPortInstance.getInstance();
		cp.initCommPort("COM2");
		try {
			cp.getSerialWriter().writeCommand(new byte[]{00});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
