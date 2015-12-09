package com.bsu.commport;

import com.bsu.commport.SerialReader;
import com.bsu.commport.SerialWriter;
import com.bsu.system.tool.JSONBSUConfig;
import org.json.JSONException;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 用来操作串口类
 * 用户会使用putCommMessage函数为消息队列中增加消息.
 * 使用SEND RECEIVE两种状态切换来保证数据的单运做.
 * 使用时间戳来标记发送和接收的命令的配对唯一性.
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
	
	private CommPortIdentifier portId;																				//端口标识
	private Enumeration portList;																						//端口列表
	
	private SerialPort serialPort;																					//串口对象
	private SerialReader sreader;																						//串口读取对象
	private SerialWriter swriter;																						//串口写对象

	private ArrayBlockingQueue<CommMessage> msgqueue = new ArrayBlockingQueue<CommMessage>(100);								//消息队列,用队列来保存要发送的消息,保证每个消息都不会被遗漏
	private enum MSGSTATE {SEND,RECEIVE};																			//消息处理状态,默认为发送状态
	private MSGSTATE switchState = MSGSTATE.SEND;
	private long currTimestamp = -1;																				//当前消息的时间戳
	private String extData = "";
	private ArrayList<CommPortReceiveListener> listeners = new ArrayList<>();

	/**
	 * 初始化串口端口
	 */
	public void initCommPort(){
		portList = CommPortIdentifier.getPortIdentifiers();
//		System.out.println("===============has comm port:" + portList.hasMoreElements());
		while(portList.hasMoreElements()){
			portId = (CommPortIdentifier) portList.nextElement();
			if(portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				try {
					if (portId.getName().equals(JSONBSUConfig.getInstance().getPort())) {
						serialPort = (SerialPort) portId.open("SerialReader", 2000);                               //获得串口对象
						sreader = new SerialReader(serialPort);                                        				//生成串口读取对象

						System.out.println("======================init comm port success");

						//监听SerialReader的接收数据,当收到数据时马上发送给所有的监听器
						sreader.setSerialReaderListener(new SerialReader.SerialReaderListener() {
							@Override
							public void readCommpleted(byte[] command) {
								Iterator<CommPortReceiveListener> it = listeners.iterator();
								while(it.hasNext())
									it.next().receive(new CommMessage(new String(command),extData,currTimestamp));
								System.out.println("===================ReceiveData:"+new String(command));
								switchState = MSGSTATE.SEND;															//接收操作完成后,切换为发送状态,以进行下一条数据的发送
							}
						});

						swriter = new SerialWriter(serialPort);                                                      //生成串口写入对象
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
//					System.out.println(e.getMessage());
				}
			}
		}

		sendData();																										//执行发送消息操作
	}

	private int ex_rs232 = 0;
	private boolean sendflag = true;
	/**
	 * 启用一个线程用于发送数据,当状态为发送状态每500毫秒取出一条指令进行发送
	 */
	private void sendData(){
		Thread t_sendData = new Thread(new Runnable() {
			@Override
			public void run() {
				while(sendflag){
					try{
						//如果当前状态为发送消息,则发送一条队列中的消息//
						if(switchState==MSGSTATE.SEND) {
							//每500毫秒发送一次指令
							CommMessage msg = msgqueue.poll();
							if (msg != null) {
								currTimestamp = msg.timestamp;
								String cmd = msg.data;
								extData = msg.extdata;
//								cmd = "@00TS==HelloPLC5A*";
								swriter.writeCommand(cmd.getBytes());
								System.out.println("===================cmd send:  " + cmd);
								switchState = MSGSTATE.RECEIVE;                                                      //发送完消息后,将状态切换为接收,保证上一条数据能正确执行接收操作.
							}
						}
//						}else{
//							ex_rs232++;
////							System.out.println("ex_rs232 count:" + ex_rs232);
//						}
//
//						if(ex_rs232>=5&&ex_rs232<6){
//////							CommPortInstance.getInstance().closeSerialPort();
//////							CommPortInstance.getInstance().initCommPort();
//
//							serialPort.removeEventListener();
//							sreader.close();
//							sreader = new SerialReader(serialPort);                                                        //生成串口读取对象
//
//							//监听SerialReader的接收数据,当收到数据时马上发送给所有的监听器
//							sreader.setSerialReaderListener(new SerialReader.SerialReaderListener() {
//								@Override
//								public void readCommpleted(byte[] command) {
//									Iterator<CommPortReceiveListener> it = listeners.iterator();
//									while(it.hasNext())
//										it.next().receive(new CommMessage(new String(command),extData,currTimestamp));
//									System.out.println("===================ReceiveData:"+new String(command));
//								switchState = MSGSTATE.SEND;															//接收操作完成后,切换为发送状态,以进行下一条数据的发送
//								}
//							});
//ng
//							System.out.println("}}}}}}}}}}}}}}}}}}}}}}}}}}read comm is restart");
//							switchState=MSGSTATE.SEND;
//							ex_rs232 = 999;
//						}

						Thread.currentThread().sleep(300);																//每300毫秒检查一次队列中是否有消息
					}catch (IOException | InterruptedException e){
						e.printStackTrace();
					}
				}
			}
		});
		t_sendData.start();
	}

	/**
	 * 想消息队列中增加消息
	 * @param cm	带时间戳的消息对象
	 */
	public void putCommMessage(CommMessage cm){
		try {
			msgqueue.put(cm);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public SerialReader getSerialReader(){return sreader;}
	public SerialWriter getSerialWriter(){return swriter;}
	/**
	 * 关闭串口
	 */
	public void closeSerialPort(){
		try {
			sreader.close();
			swriter.close();
			serialPort.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	/**
	 * CommPortInstance的接收数据监听器
	 */
	public static interface CommPortReceiveListener{
		void receive(CommMessage data);
	}

	/**
	 * 增加一个监听器到容器中.
	 * @param listener	接收数据监听器
	 */
	public void addCommPortReceiveListener(CommPortReceiveListener listener){
		listeners.add(listener);
	}

	/**
	 * 将一个监听器从容器中移除
	 * @param listener	要移除的监听器
	 */
	public void removeCommPortRecerveListener(CommPortReceiveListener listener){listeners.remove(listener);}

}


