/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.utils;

public class ThreadUtilities {
	
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/utils/ThreadUtilities.java,v 1.10 2022/01/21 19:51:32 dclunie Exp $";

	public final static void checkIsEventDispatchThreadElseException() {
		if (!java.awt.EventQueue.isDispatchThread()) {
			throw new RuntimeException("Not on AWT EventDispatchThread");
		}
	}
}