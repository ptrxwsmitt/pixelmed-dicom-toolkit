/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import java.util.Random;

/**
 * <p>Various static methods helpful for network activities.</p>
 *
 * @author	dclunie
 */
public class NetworkUtilities {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/NetworkUtilities.java,v 1.5 2022/01/21 19:51:25 dclunie Exp $";

	//private static final Logger slf4jlogger = LoggerFactory.getLogger(NetworkUtilities.class);

	private NetworkUtilities() {}
	
	// https://codereview.stackexchange.com/questions/31557/checking-if-a-port-is-in-use
	
	public static boolean isPortResponding(String host,int port) {
		try {
			new Socket(host,port).close();
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}
	
	public static boolean isPortResponding(InetAddress address,int port) {
		try {
			new Socket(address,port).close();
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}
	
	public static boolean isPortOnLocalHostResponding(int port) throws UnknownHostException {
		return isPortResponding(InetAddress.getLocalHost(),port);
	}
	
	public static int getRandomUnusedPortToListenOnLocally() throws UnknownHostException {
		int port;
		do {
			port = 1025 + new Random().nextInt(10000);
			if (isPortOnLocalHostResponding(port)) {
				port = 0;
			}
		} while (port == 0);
		return port;
	}
}

