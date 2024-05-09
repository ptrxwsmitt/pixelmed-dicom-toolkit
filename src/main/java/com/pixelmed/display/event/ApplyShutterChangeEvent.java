/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display.event;

import com.pixelmed.event.Event;
import com.pixelmed.event.EventContext;

/**
 * @author	dclunie
 */
public class ApplyShutterChangeEvent extends Event {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/event/ApplyShutterChangeEvent.java,v 1.9 2022/01/21 19:51:21 dclunie Exp $";

	private boolean shutter;

	/**
	 * @param	eventContext
	 * @param	shutter
	 */
	public ApplyShutterChangeEvent(EventContext eventContext,boolean shutter) {
		super(eventContext);
		this.shutter=shutter;
//System.err.println("ApplyShutterChangeEvent() shutter = "+shutter);
	}

	/***/
	public boolean applyShutter() { return shutter; }

}

