/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

/**
 * @author	dclunie
 */
public class PresentationAddress {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/PresentationAddress.java,v 1.11 2022/01/21 19:51:25 dclunie Exp $";
	
	private String hostname;
	private int port;
	
	public boolean equals(Object obj) {
		if (obj instanceof PresentationAddress) {
			PresentationAddress paComp = (PresentationAddress)obj;
			return ((hostname == null && paComp.getHostname() == null) || hostname.equals(paComp.getHostname())) && (port == paComp.getPort());
		}
		else {
			return super.equals(obj);
		}
	}
		
	public PresentationAddress(String hostname,int port) {
		this.hostname=hostname;
		this.port=port;
	}
	
	public String getHostname() { return hostname; }
	public int getPort() { return port; }
}


