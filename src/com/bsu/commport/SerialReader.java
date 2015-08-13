package com.bsu.commport;

import com.bsu.system.tool.JSONBSUConfig;
import org.json.JSONException;

import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.TooManyListenersException;

/**
 * 串口读取类
 * @author fengchong
 *
 */
public class SerialReader{
	private SerialPort serialPort;
	private InputStream inputStream;
	private Thread readThread;
	private SerialReaderListener listener = null;										//监听对象
	private JSONBSUConfig cfg = null;
	private StringBuffer sb = new StringBuffer();
	public SerialReader(SerialPort sport){
		try{
			cfg = JSONBSUConfig.getInstance();

			serialPort = sport; 
			inputStream = serialPort.getInputStream();									//从串口获得输入流
			serialPort.addEventListener(new SerialPortEventListener() {                    //为串口增加事件监听
				@Override
				public void serialEvent(SerialPortEvent event) {
					switch (event.getEventType()) {
						case SerialPortEvent.BI:
						case SerialPortEvent.OE:
						case SerialPortEvent.FE:
						case SerialPortEvent.PE:
						case SerialPortEvent.CD:
						case SerialPortEvent.CTS:
						case SerialPortEvent.DSR:
						case SerialPortEvent.RI:
						case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
							break;
						case SerialPortEvent.DATA_AVAILABLE:                                                         //当有可用数据时
							try{
								while(inputStream.available()>0){
									char b = (char) inputStream.read();
									if(b!='\r')
										sb.append(b);
									else {
										listener.readCommpleted(sb.toString().getBytes());                            //通知外部数据读取完成
										sb = new StringBuffer();
										break;
									}
								}
					}catch(IOException e){
						e.printStackTrace();
					}
					break;
				}
			}
			});

			serialPort.notifyOnDataAvailable(true);
			serialPort.setSerialPortParams(cfg.getBaudrate(),
					cfg.getDatabits(),
					cfg.getStopbits(),
					cfg.getParity());
			
			readThread = new Thread(new Runnable(){
				@Override
				public void run() {
					try{
						Thread.sleep(20000);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			});
			readThread.start();
		}catch(IOException | TooManyListenersException | UnsupportedCommOperationException | JSONException e){
			e.printStackTrace();
		}
	}
	/**
	 * 设置串口读取的监听器
	 * @param l		从外部设置进来的监听器对象
	 */
	public void setSerialReaderListener(SerialReaderListener l){
		listener = l;
	}
	/**
	 * 字节数组转int型
	 * @param b		字节数组数据
	 * @return		返回int型数据
	 */
	private int byteArrayToInt(byte[] b) {  
	    return   b[3] & 0xFF |  
	            (b[2] & 0xFF) << 8 |  
	            (b[1] & 0xFF) << 16 |  
	            (b[0] & 0xFF) << 24;

	}

	public static interface SerialReaderListener {
		void readCommpleted(byte[] command);
	};

	public static void main(String[] args){
		CommPortInstance cp = CommPortInstance.getInstance();
		cp.initCommPort();
		cp.getSerialReader().setSerialReaderListener(new SerialReaderListener(){

			@Override
			public void readCommpleted(byte[] command) {
				System.out.println("=============read command byte[]:"+command);
			}
		});
	}
}
