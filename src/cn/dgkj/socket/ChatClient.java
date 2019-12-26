//客户端
package cn.dgkj.socket;
 
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
 
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
 
/**
 * 聊天室客户端
 * @author fendou
 */
 
public class ChatClient extends JFrame{
	
	Socket socket;
	PrintWriter out;
	BufferedReader in;
	JPanel p;
	JScrollPane sp;
	JTextArea txtContent;
	JLabel lblName,lblSend;
	JTextField txtName,txtSend;
	JButton btnSend;
	
	public ChatClient() {
		
		super("客户聊天");
		txtContent = new JTextArea();
		// 设置文本域只读
		txtContent.setEditable(false);
		sp = new JScrollPane(txtContent);
		lblName = new JLabel("姓名：");
		txtName = new JTextField(5);
		lblSend = new JLabel("请输入：");
		txtSend = new JTextField(20);
		btnSend = new JButton("发送");
		p = new JPanel();
		p.add(lblName);
		p.add(txtName);
		p.add(lblSend);
		p.add(txtSend);
		p.add(btnSend);
		this.add(p,BorderLayout.SOUTH);
		this.add(sp);
		this.setSize(500, 400);// 宽500，高400
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		try {
			// 创建一个套接字
			socket = new Socket("127.0.0.1",1218);
			// 创建一个往套接字中写数据的管道，即输出流，给服务器发送信息
			out = new PrintWriter(socket.getOutputStream());
			// 创建一个从套接字读数据的管道，即输入流，读服务器的返回信息
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("没有找到主机或主机未打开。");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// 注册监听
		btnSend.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// 获取用户输入的文本
				String strName = txtName.getText();
				String strMsg = txtSend.getText();
				if(!strMsg.equals("")) {
					// 通过输出流将数据发送给服务器
					out.println(strName+"说："+strMsg);
					out.flush();
					// 清空文本框
					txtSend.setText("");
				}
			}
		});
		// 启动线程
		new GetMsgFromServer().start();
	}
	
	// 接收服务器的返回信息
	class GetMsgFromServer extends Thread {
		@Override
		public void run() {
			while(this.isAlive()) {
				
				try {
					String strMsg = in.readLine();
					
					if(strMsg != null) {
						// 在文本域中显示聊天信息
						txtContent.append(strMsg+"\n");
					}
					Thread.sleep(50);
					
				} catch (Exception e) {
					e.printStackTrace();
				}	
			
			}
			
		}
		
	}
	
	public static void main(String[] args) {
		
		ChatClient f = new ChatClient();
		f.setVisible(true);
		
	}
 
}
 
/**
 * 上述代码在构造方法中先创建客户端图形界面，然后创建一个Socket连接服务器，
 * 并获取Socket的输入流和输出流，用于对Socket中的数据进行读/写。
 * 当用户点击“发送”按钮时，将用户在文本框中输入的数据通过输出流写到Socket中，并发送给服务器。
 * 启动GetMsgFromServer线程，通过输入流循环接收服务器发送的返回信息。
 * 
 * 
 */
 
 