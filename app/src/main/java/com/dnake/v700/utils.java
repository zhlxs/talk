package com.dnake.v700;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class utils {
	public static String getLocalIp() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = (NetworkInterface) en.nextElement();
				for(Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress())
						return inetAddress.getHostAddress().toString();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getLocalMac() {
		String mac_s = "";
		try {
			byte[] mac;
			NetworkInterface ne = NetworkInterface.getByInetAddress(InetAddress.getByName(utils.getLocalIp()));

			mac = ne.getHardwareAddress();
			mac_s = String.format("%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mac_s;
	}

	public static void eth0_reset() {
		dmsg req = new dmsg();
		req.to("/control/eth/reset", null);
	}

	public static void process() {
		if (mBuzzerTs != 0 && Math.abs(System.currentTimeMillis()-mBuzzerTs) >= mBuzzerTick) {
			mBuzzerTs = 0;
			ioctl.buzzer(0);
		}
	}

	public static long mBuzzerTs = 0;
	public static int mBuzzerTick = 0;
	public static void buzzer(int tick) {
		mBuzzerTs = System.currentTimeMillis();
		mBuzzerTick = tick;
		ioctl.buzzer(1);
	}

	public static class ioctl {
		public static void buzzer(int onoff) {
			dmsg req = new dmsg();
			dxml p = new dxml();
			p.setInt("/params/onoff", onoff);
			req.to("/control/v170/buzzer", p.toString());
		}
	}
}
