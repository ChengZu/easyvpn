package com.vcvnc.xiguavpn;
/*
 * 服务器套接字
 * 接收新客户端，移除已关闭客户端
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class VpnServer implements Runnable {
	//服务器套接字
	private ServerSocket server = null;
	//客户端保存数组
	private ArrayList<Client> clients = new ArrayList<Client>();
	//线程运行状态
	private boolean closed = false;
	//接收新客户端线程
	private Thread thread;

	public VpnServer() {
		try {
			//初始化服务器套接字
			server = new ServerSocket(Config.PORT);
			//创建新线程
			thread = new Thread(this);
			//启动新线程
			thread.start();
		} catch (IOException e) {
			e.printStackTrace();;
		}
	}
	
	/*
	 * 关闭服务器套接字和客户端
	 */
	public void close(){
		closed = true;
		thread.interrupt();
		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//关闭所有客户端
		for (Client client : clients) {
			client.close();
		}
	}

	@Override
	public void run() {
		boolean noError = true;
		System.out.printf("Server listen on: %s.\n", server.getLocalSocketAddress());
		while (!closed && noError) {
			try {
				Socket socket = server.accept();
				//移除已关闭客户端
				for (int i = 0; i < clients.size(); i++) {
					if (clients.get(i).isClose()) {
						clients.remove(i);
						i--;
					}
				}
				//移除已过期客户端
				for (int i = 0; i < clients.size(); i++) {
					if (clients.get(i).isExpire()) {
						clients.get(i).close();
						clients.remove(i);
						i--;
					}
				}
				//是否建立客户端
				if (clients.size() < Config.MAX_CLIENT_NUM) {
					Client client = new Client(socket);
					clients.add(client);
					System.out.printf("New client(%d) connect, total client num %d.\n", client.id, clients.size());
				} else {
					socket.close();
					System.out.println("Client connect max, close connect.");
				}
			} catch (IOException e) {
				noError = false;
				e.printStackTrace();
			}
		}
		close();
	}

}
