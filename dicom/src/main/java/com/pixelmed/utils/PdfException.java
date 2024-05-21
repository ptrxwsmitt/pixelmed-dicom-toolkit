/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.utils;

/**
 * @author	dclunie
 */
public class PdfException extends Exception {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/utils/PdfException.java,v 1.9 2022/01/21 19:51:32 dclunie Exp $";

	/**
	 * @param	msg
	 */
	public PdfException(String msg) {
		super(msg);
	}
}


