package com.vcvnc.xiguavpn;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.net.SocketException;

public class UdpProxy implements Runnable {
	//连接目标服务器UDP套接字
	private DatagramSocket datagramSocket;
	//保存UDP连接信息
	//头部信息
	private byte[] headerBytes = null;
	//线程退出符号
	private boolean closed = false;
	//源ip
	private int srcIp;
	//源端口
	private short srcPort;
	//目标ip
	private int destIp;
	//目标端口
	private short destPort;
	//客户端
	private Client client;
	//接收目标服务器数据线程
	private Thread thread = null;
	//最后活动时间
	public long lastRefreshTime = System.currentTimeMillis();
	private static long UID = 0;
	public long id;
	public static final int HEADER_SIZE = IPHeader.IP4_HEADER_SIZE + UDPHeader.UDP_HEADER_SIZE;

	public UdpProxy(Client client, byte[] packet) {
		UID++;
		id = UID;
		try {

			this.client = client;
			
			IPHeader ipHeader = new IPHeader(packet, 0);
			int ipHeaderLen = ipHeader.getHeaderLength();
			UDPHeader udpHeader = new UDPHeader(packet, ipHeaderLen);
			srcIp = ipHeader.getSourceIP();
			srcPort = udpHeader.getSourcePort();
			destIp = ipHeader.getDestinationIP();
			destPort = udpHeader.getDestinationPort();
			
			headerBytes = new byte[UdpProxy.HEADER_SIZE];
			arraycopy(packet, 0, headerBytes, 0, IPHeader.IP4_HEADER_SIZE);
			arraycopy(packet, ipHeaderLen, headerBytes, IPHeader.IP4_HEADER_SIZE, UDPHeader.UDP_HEADER_SIZE);
			ipHeader = new IPHeader(headerBytes, 0);
			udpHeader = new UDPHeader(headerBytes, IPHeader.IP4_HEADER_SIZE);
			ipHeader.setSourceIP(destIp);
			udpHeader.setSourcePort(destPort);
			ipHeader.setDestinationIP(srcIp);
			udpHeader.setDestinationPort(srcPort);
			ipHeader.setHeaderLength(20);
			ipHeader.setProtocol((byte) IPHeader.UDP);
			ipHeader.setFlagsAndOffset((short) 0);
			
			datagramSocket = new DatagramSocket();
			thread = new Thread(this, "UdpProxy(" + id + ")");
			thread.start();

		} catch (SocketException e) {
			close();
			e.printStackTrace();
		}
	}

	/*
	 * 发送数据给目标服务器
	 */
	public void sendToServer(byte[] packet) {
		lastRefreshTime = System.currentTimeMillis();
		IPHeader ipHeader = new IPHeader(packet, 0);
		int ipHeaderLen = ipHeader.getHeaderLength();
		int dataSize = ipHeader.getTotalLength() - ipHeaderLen - UDPHeader.UDP_HEADER_SIZE;

		byte[] data = new byte[dataSize];
		System.arraycopy(packet, UdpProxy.HEADER_SIZE, data, 0, dataSize);
		DatagramPacket clientPacket = new DatagramPacket(data, dataSize, CommonMethods.ipIntToInet4Address(destIp & 0xFFFFFFFF), destPort & 0xFFFF);
		
		try {
			datagramSocket.send(clientPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			close();
			//e.printStackTrace();
		}
	}

	public void close() {
		closed = true;
		if(datagramSocket != null)
			datagramSocket.close();
	}

	public boolean isClose() {
		return closed;
	}

	@Override
	public void run() {
		boolean noError = true;
		//最大接收缓存
		byte[] revBuf = new byte[Config.MUTE - HEADER_SIZE];
		DatagramPacket revPacket = new DatagramPacket(revBuf, revBuf.length);
		int size = 0;
		while (size != -1 && !closed && noError) {
			try {
				datagramSocket.receive(revPacket);
				size = revPacket.getLength();
				synchronized (this) {
					if (size > 0) {
						byte[] packet = new byte[HEADER_SIZE + size];
						arraycopy(headerBytes, 0, packet, 0, HEADER_SIZE);
						arraycopy(revPacket.getData(), 0, packet, HEADER_SIZE, size);
						updateUDPBuffer(packet, size);
						// 将数据包发送给客户端
						noError = client.sendToClient(packet, 0, HEADER_SIZE + size);
					}
					lastRefreshTime = System.currentTimeMillis();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				noError = false;
				//e.printStackTrace();
			}
		}
		close();
	}

	public boolean isExpire(){
		long now = System.currentTimeMillis();
		return (now - lastRefreshTime) > Config.PROXY_EXPIRE_TIME;
	}
	
	public boolean equal(byte[] packet) {
		IPHeader ipHeader = new IPHeader(packet, 0);
		UDPHeader udpHeader = new UDPHeader(packet, ipHeader.getHeaderLength());
		return srcIp == ipHeader.getSourceIP() && srcPort == udpHeader.getSourcePort()
				&& destIp == ipHeader.getDestinationIP()
				&& destPort == udpHeader.getDestinationPort();
	}
	
	public void arraycopy(byte[] srcbuf, int srcoffset, byte[] desbuf, int desoffset, int size){
		for(int i=0; i<size; i++){
			desbuf[i+desoffset] = srcbuf[i+srcoffset];
		}
	}
	
	public void updateUDPBuffer(byte[] packet, int size){
		IPHeader ipHeader = new IPHeader(packet, 0);
		UDPHeader udpHeader = new UDPHeader(packet, IPHeader.IP4_HEADER_SIZE);
		ipHeader.setTotalLength((short) (UdpProxy.HEADER_SIZE + size));
		udpHeader.setTotalLength((short) (UDPHeader.UDP_HEADER_SIZE + size));
		udpHeader.ComputeUDPChecksum(ipHeader);
	}
}
