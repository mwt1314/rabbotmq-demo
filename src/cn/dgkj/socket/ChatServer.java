//服务器端
package cn.dgkj.socket;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
 
/**
 * 聊天室服务器端
 * @author fendou
 */
 
public class ChatServer {
	
	ServerSocket serverSocket;
	ArrayList<BufferedReader> ins = new ArrayList<BufferedReader>();
	ArrayList<PrintWriter> outs = new ArrayList<PrintWriter>();
	LinkedList<String> msgList = new LinkedList<String>();
	
	public ChatServer() {
		
		try {
			serverSocket = new ServerSocket(1218);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// 创建 AcceptSocketThread 线程，并启动
		new AcceptSocketThread().start();
		// 创建 SendMsgToClient 线程，并启动
		new SendMsgToClient().start();
		System.out.println("Server Start...");
		
	}
	
	// 接收客户端套接字线程
	class AcceptSocketThread extends Thread{
		@Override
		public void run() {
			while(this.isAlive()) {
				
				try {
					// 接收套接字
					Socket socket = serverSocket.accept();
					if(socket != null) {
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						ins.add(in);
						outs.add(new PrintWriter(socket.getOutputStream()));
						// 开启一个线程接收客户端的聊天信息
						new GetMsgFromClient(in).start();
						
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
	// 接收客户的聊天信息的线程
	class GetMsgFromClient extends Thread {
		BufferedReader in;
		public GetMsgFromClient(BufferedReader in) {
			this.in = in;
		}
		
		@Override
		public void run() {
			while(this.isAlive()) {
				
				try {
					String strMsg = in.readLine();
					
					if(strMsg != null) {
						msgList.add(strMsg);
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
	// 给所有客户发送聊天信息的线程
	class SendMsgToClient extends Thread{
		
		@Override
		public void run() {
			
			while(this.isAlive()) {
				try {
					if(!msgList.isEmpty()) {
						String s = msgList.removeLast();
						for(int i = 0;i < outs.size(); i++) {
							outs.get(i).println(s);
							outs.get(i).flush();
						}
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		}
		
	}
	
	
	public static void main(String[] args) {
		
		new ChatServer();
		
	}
	
	
}
 
 
/**
 * 上述代码中，服务器端应用程序有多个线程：
 * 
 * AcceptSocketThread 线程用于循环接收客户端发来的Socket连接，
 * 并将与该Socket通信的输入流和输出流保存到ArrayList集合中；
 * 
 * GetMsgFromClient 线程用于接收客户端发来的聊天信息，
 * 并将信息保存到LinkedList集合中；
 * 
 * SendMsgToClient 线程用于将LinkedList集合中的聊天信息发给所有客户端。
 * 
 * 
 */