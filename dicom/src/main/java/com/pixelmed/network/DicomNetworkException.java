/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

/**
 * @author	dclunie
 */
public class DicomNetworkException extends Exception {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/DicomNetworkException.java,v 1.12 2022/01/21 19:51:24 dclunie Exp $";

	/**
	 * @param	msg
	 */
	public DicomNetworkException(String msg) {
		super(msg);
	}
}



