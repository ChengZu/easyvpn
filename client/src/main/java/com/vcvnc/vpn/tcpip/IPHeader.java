package com.vcvnc.vpn.tcpip;

import com.vcvnc.vpn.utils.CommonMethods;

public class IPHeader {

	/**
	 * IP报文格式
	 * 0                                   　　　　       15  16　　　　　　　　　　　　　　　　　　　　　　　　   31
	 * ｜　－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
	 * ｜  ４　位     ｜   ４位首     ｜      ８位服务类型      ｜      　　         １６位总长度            　   ｜
	 * ｜  版本号     ｜   部长度     ｜      （ＴＯＳ）　      ｜      　 　 （ｔｏｔａｌ　ｌｅｎｇｔｈ）    　    ｜
	 * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
	 * ｜  　　　　　　　　１６位标识符                         ｜　３位    ｜　　　　１３位片偏移                 ｜
	 * ｜            （ｉｎｄｅｎｔｉｆｉｅｒ）                 ｜　标志    ｜      （ｏｆｆｓｅｔ）　　           ｜
	 * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
	 * ｜      ８位生存时间ＴＴＬ      ｜       ８位协议        ｜　　　　　　　　１６位首部校验和                  ｜
	 * ｜（ｔｉｍｅ　ｔｏ　ｌｉｖｅ）　　｜   （ｐｒｏｔｏｃｏｌ） ｜              （ｃｈｅｃｋｓｕｍ）               ｜
	 * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
	 * ｜                              ３２位源ＩＰ地址（ｓｏｕｒｃｅ　ａｄｄｒｅｓｓ）                           ｜
	 * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
	 * ｜                         ３２位目的ＩＰ地址（ｄｅｓｔｉｎａｔｉｏｎ　ａｄｄｒｅｓｓ）                     ｜
	 * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
	 * ｜                                          ３２位选项（若有）                                        ｜
	 * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
	 * ｜                                                                                                  ｜
	 * ｜                                               数据                                               ｜
	 * ｜                                                                                                  ｜
	 * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
	 **/
	public static final int IP4_HEADER_SIZE = 20;
	public static final short IP = 0x0800;
	public static final char ICMP = 1;
	public static final char TCP = 6;  //6: TCP协议号
	public static final char UDP = 17; //17: UDP协议号
	public static final char offset_proto = 9; //9：8位协议偏移
	public static final int offset_src_ip = 12; //12：源ip地址偏移
	public static final int offset_dest_ip = 16; //16：目标ip地址偏移
	public static final char offset_ver_ihl = 0; //0: 版本号（4bits） + 首部长度（4bits）
	public static final char offset_tos = 1; //1：服务类型偏移
	public static final short offset_tlen = 2; //2：总长度偏移
	public static final short offset_identification = 4; //4：16位标识符偏移
	public static final short offset_flags_fo = 6; //6：标志（3bits）+ 片偏移（13bits）
	public static final char offset_ttl = 8; //8：生存时间偏移
	public static final short offset_crc = 10; //10：首部校验和偏移
	public static final int offset_op_pad = 20; //20：选项 + 填充

	public byte[] mData;
	public int mOffset;

	public IPHeader(byte[] data, int offset) {
		mData = data;
		mOffset = offset;
	}

	public void Default() {
		setHeaderLength(20);
		setTos((byte) 0);
		setTotalLength((short)0);
		setIdentification((short)0);
		setFlagsAndOffset((short) 0);
		setTTL((byte) 64);
	}

	public int getDataLength() {
		return getTotalLength() - getHeaderLength();
	}

	public int getHeaderLength() {
		return (mData[mOffset + offset_ver_ihl] & 0x0F) * 4;
	}

	public void setHeaderLength(int value) {
		// 4 << 4 表示版本为IPv4
		mData[mOffset + offset_ver_ihl] = (byte) ((4 << 4) | (value / 4));
	}

	public byte getTos() {
		return mData[mOffset + offset_tos];
	}

	public void setTos(byte value) {
		mData[mOffset + offset_tos] = value;
	}

	public short getTotalLength() {
		return CommonMethods.readShort(mData, mOffset + offset_tlen);
	}

	public void setTotalLength(short value) {
		CommonMethods.writeShort(mData, mOffset + offset_tlen, value);
	}

	public short getIdentification() {
		return CommonMethods.readShort(mData, mOffset + offset_identification);
	}

	public void setIdentification(short value) {
		CommonMethods.writeShort(mData, mOffset + offset_identification, value);
	}

	public short getFlagsAndOffset() {
		return CommonMethods.readShort(mData, mOffset + offset_flags_fo);
	}

	public void setFlagsAndOffset(short value) {
		CommonMethods.writeShort(mData, mOffset + offset_flags_fo, value);
	}

	public byte getTTL() {
		return (byte) (mData[mOffset + offset_ttl] & 0xFF);
	}

	public void setTTL(byte value) {
		mData[mOffset + offset_ttl] = value;
	}

	public byte getProtocol() {
		return mData[mOffset + offset_proto];
	}

	public void setProtocol(byte value) {
		mData[mOffset + offset_proto] = value;
	}

	public short getCrc() {
		return CommonMethods.readShort(mData, mOffset + offset_crc);
	}

	public void setCrc(short value) {
		CommonMethods.writeShort(mData, mOffset + offset_crc, value);
	}

	public int getSourceIP() {
		return CommonMethods.readInt(mData, mOffset + offset_src_ip);
	}

	public void setSourceIP(int value) {
		CommonMethods.writeInt(mData, mOffset + offset_src_ip, value);
	}

	public int getDestinationIP() {
		return CommonMethods.readInt(mData, mOffset + offset_dest_ip);
	}

	public void setDestinationIP(int value) {
		CommonMethods.writeInt(mData, mOffset + offset_dest_ip, value);
	}

	//计算校验和
	public short checksum(long sum, byte[] buf, int offset, int len) {
		sum += getsum(buf, offset, len);
		while ((sum >> 16) > 0) {
			sum = (sum & 0xFFFF) + (sum >> 16);
		}
		return (short) ~sum;
	}

	public long getsum(byte[] buf, int offset, int len) {
		//	Log.d(TAG,"getsum offset  "+offset+"  len"+len+"  length"+buf.length);
		long sum = 0;
		while (len > 1) {
			sum += CommonMethods.readShort(buf, offset) & 0xFFFF;
			offset += 2;
			len -= 2;
		}

		if (len > 0) {
			sum += (buf[offset] & 0xFF) << 8;
		}

		return sum;
	}

	//计算IP包的校验和
	public boolean ComputeIPChecksum() {
		short oldCrc = getCrc();
		setCrc((short) 0);
		short newCrc = checksum(0, mData, mOffset, getHeaderLength());
		setCrc(newCrc);
		return oldCrc == newCrc;
	}

	@Override
	public String toString() {
		return String.format("%s->%s Pro=%s, HLen=%d", CommonMethods.ipIntToString(getSourceIP()),
				CommonMethods.ipIntToString(getDestinationIP()), getProtocol(), getHeaderLength());
	}
}