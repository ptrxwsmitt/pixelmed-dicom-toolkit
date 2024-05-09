/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

/**
 * @author	dclunie
 */
abstract class RequestCommandMessage implements CommandMessage {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/RequestCommandMessage.java,v 1.9 2022/01/21 19:51:25 dclunie Exp $";

	static private int nextAvailableMessageID = 0;

	/**
	 * <p>Get a new message ID.</P>
	 *
	 * <p>Implemented as a simple counter on scope of JVM invocation, and will eventually wrapp around.</P>
	 *
	 * @return	a new unused integer message ID
	 */
	public final int getNextAvailableMessageID() { return ++nextAvailableMessageID; }
}
