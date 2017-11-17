package com.wzb.smartcard.util;

import java.util.logging.LogManager;

import org.w3c.dom.Text;

import android.R.integer;
import android.device.IccManager;
import android.text.TextUtils;
import android.util.Log;

public class CardManager {

	public static IccManager iccM = new IccManager();
	
	public static String read_card(int offset,int len){
		byte[] rsp=new byte[256];
		byte[] state=new byte[2];
		String res = "";
		String cmd="00B0"+numToHex16(offset)+numToHex8(len);
		LogUtil.logMessage("wzb", "read_card cmd:"+cmd);
		int rsp_len=iccM.apduTransmit(Convert.hexStringToByteArray(cmd), Convert.hexStringToByteArray(cmd).length, rsp, state);
		if(rsp_len<=0){
			LogUtil.logMessage("wzb", "rsp_len err:"+rsp_len);
			return res;
		}
		LogUtil.logMessage("wzb", "read card rsp len:"+rsp_len);
		return Convert.bytesToHexString(rsp,0,rsp_len-2);
		
	}
	
	public static String write_card(int offset,int len ,String datahexstr){
		byte[] rsp=new byte[256];
		byte[] state=new byte[2];
		String res = "";
		String cmd="00D6"+numToHex16(offset)+numToHex8(len)+datahexstr;
		LogUtil.logMessage("wzb", "write_card cmd:"+cmd);
		int rsp_len=iccM.apduTransmit(Convert.hexStringToByteArray(cmd), Convert.hexStringToByteArray(cmd).length, rsp, state);
		if(rsp_len<=0){
			LogUtil.logMessage("wzb", "rsp_len err:"+rsp_len);
			return res;
		}
		LogUtil.logMessage("wzb", "write_card rsp:"+Convert.bytesToHexString(rsp,0,rsp_len));
		LogUtil.logMessage("wzb", "write_card state:"+Convert.bytesToHexString(state,0,2));
		return Convert.bytesToHexString(state,0,2);
	}
	
	//使用1字节就可以表示b
	public static String numToHex8(int b) {
	        return String.format("%02x", b);//2表示需要两个16进行数
	    }
	//需要使用2字节表示b
	public static String numToHex16(int b) {
	        return String.format("%04x", b);
	    }
	//需要使用4字节表示b
	public static String numToHex32(int b) {
	        return String.format("%08x", b);
	    }

	public static boolean SelectCPU_EF() {
		boolean resbool = false;
		if (iccM.open((byte) 0, (byte) 1, (byte) 2) != 0) {
			LogUtil.logMessage("wzb", "iccm open fail");
			return false;
		}
		byte[] atr = new byte[1024];

		int activate_state = iccM.activate(atr);
		if (activate_state < 0) {
			LogUtil.logMessage("wzb", "iccm activate fail");
			return false;
		}
		LogUtil.logMessage("wzb",
				"activate state:" + activate_state + " rsp:" + Convert.bytesToHexString(atr, 0, activate_state));

		byte[] apdu1 = { (byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x0, (byte) 0x2, (byte) 0x3F, (byte) 0x00 };
		byte[] rsl1 = new byte[1024];
		byte[] sw1 = new byte[2];
		int rsp1_len = iccM.apduTransmit(apdu1, 7, rsl1, sw1);
		if (rsp1_len <= 0) {
			LogUtil.logMessage("wzb", "rsp1 fail");
			return false;
		}
		LogUtil.logMessage("wzb",
				"11:" + Convert.bytesToHexString(rsl1, 0, rsp1_len) + "/" + Convert.bytesToHexString(sw1, 0, 2));
		if(!Convert.bytesToHexString(sw1, 0, 2).equals("9000")){
			return false;
		}
		byte[] apdu2 = { (byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xDF, (byte) 0x01 };
		byte[] rsl2 = new byte[1024];
		byte[] sw2 = new byte[1024];
		int rsp2_len = iccM.apduTransmit(apdu2, 7, rsl2, sw2);
		if (rsp2_len <= 0) {
			LogUtil.logMessage("wzb", "rsp2 fail");
			return false;
		}
		LogUtil.logMessage("wzb", "22:" + Convert.bytesToHexString(rsl2,0,rsp2_len) + "/" + Convert.bytesToHexString(sw2,0,2));
		if(!Convert.bytesToHexString(sw2,0,2).equals("9000")){
			return false;
		}
		byte[] akey = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		if (Authenticate_CPU(1, akey)) {
			byte[] apdu3 = { (byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00,
					(byte) 0x01 };
			byte[] rsl3 = new byte[1024];
			byte[] sw3 = new byte[2];
			int rsp3_len=iccM.apduTransmit(apdu3, 7, rsl3, sw3);
			if(rsp3_len<=0){
				LogUtil.logMessage("wzb", "rsp3 fail");
				resbool=false;
			}else{
				LogUtil.logMessage("wzb", "33:" + Convert.bytesToHexString(rsl3,0,rsp3_len) + "/" + Convert.bytesToHexString(sw3,0,2));
				if(!Convert.bytesToHexString(sw3,0,2).equals("9000")){
					return false;
				}
				resbool = true;
			}
		
		}else{
			LogUtil.logMessage("wzb", "Authenticate_CPU fail");
		}

		return resbool;

	}

	public static Boolean Authenticate_CPU(int index, byte[] authKey) {
		// 获取随机数
		String clgHex = GetChallenge();

		// byte[] clgBuf = hexStringToBytes(clgHex);
		String indexStr = Integer.toHexString(index);
		
		LogUtil.logMessage("wzb", "clghex:"+clgHex);
		LogUtil.logMessage("wzb", "indexstr:"+indexStr);
		if (indexStr.length() < 2) {
			indexStr = "0" + indexStr;
		}
		//String authKeyStr = ByteToHexString(authKey, authKey.length);
		String authInfo = null;
//		try {
//			authInfo = TripleDES.encryptToBase64(clgHex, authKey);
//		} catch (Exception e) {
//			LogUtil.logMessage("wzb", "TripleDES exception:"+e.toString());
//			return false;
//		}
		authInfo=TripleDES.encodeToHexStr(clgHex, authKey);
		
		LogUtil.logMessage("wzb", "authinfo:" + authInfo);
		if(TextUtils.isEmpty(authInfo)){
			return false;
		}
		// 下发认证信息
		authInfo = "008200" + indexStr + "08" + authInfo;

		byte[] apdu4 = hexStringToBytes(authInfo);
		byte[] rsl4 = new byte[1024];
		byte[] sw4 = new byte[2];
		int rsp4_len=iccM.apduTransmit(apdu4, apdu4.length, rsl4, sw4);
		if (rsp4_len > 0) {
			LogUtil.logMessage("wzb", "44:" + Convert.bytesToHexString(rsl4,0,rsp4_len) + "/" + Convert.bytesToHexString(sw4,0,2));
			if(Convert.bytesToHexString(sw4,0,2).equals("9000")){
				return true;
			}else{
				return false;
				//return true;//for test
			}
		} else {
			return false;
		}
	}

	public static String GetChallenge() {
		String res = "";
		byte[] apdu3 = { (byte) 0x00, (byte) 0x84, (byte) 0x00, (byte) 0x00, (byte) 0x08 };
		byte[] rsl3 = new byte[1024];
		byte[] sw3 = new byte[2];
		int rsp_len=iccM.apduTransmit(apdu3, 5, rsl3, sw3);
		if(rsp_len<=0){
			return res;
		}
		LogUtil.logMessage("wzb", "GetChallenge rs:"+Convert.bytesToHexString(rsl3, 0, rsp_len)+"/status:"+Convert.bytesToHexString(sw3, 0,2));
		res = Convert.bytesToHexString(rsl3, 0, rsp_len-2);
		return res;

	}

	public static String ByteToHexString(byte[] src, int length) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	public static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static String ReadCard_CPU(int index, int length) {

		short rlen = 0;
		StringBuilder sb = new StringBuilder();
		int offset = index;
		while ((offset - index) < length) {
			int packLen = 0;
			// 每包读200个字节
			if ((offset + 200 - index) > length) {
				// 不足200字节,最后一包
				packLen = length - (offset - index);
			} else {
				packLen = 200;
			}

			String offSetStr = Integer.toHexString(offset);
			while (offSetStr.length() < 4) {
				offSetStr = "0" + offSetStr;
			}
			byte[] offsetByte = hexStringToBytes(offSetStr);

			byte[] cmd = new byte[5];
			cmd[0] = (byte) 0x00;
			cmd[1] = (byte) 0xB0;
			cmd[2] = offsetByte[0];
			cmd[3] = offsetByte[1];
			cmd[4] = (byte) packLen;
			byte[] rsl = new byte[202];
			byte[] sw = new byte[202];
			if (iccM.apduTransmit(cmd, cmd.length, rsl, sw) == 0) {
				return null;
			}
			String s = ByteToHexString(rsl, rsl.length - 2);
			sb.append(s);
			offset += packLen;
		}
		return sb.toString();
	}

}
