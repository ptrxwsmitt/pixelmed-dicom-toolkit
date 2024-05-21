/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display.event;

import com.pixelmed.event.Event;
import com.pixelmed.event.EventContext;

/**
 * @author	dclunie
 */
public class WindowingAccelerationValueChangeEvent extends Event {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/event/WindowingAccelerationValueChangeEvent.java,v 1.10 2022/01/21 19:51:21 dclunie Exp $";

	/***/
	private double value;

	/**
	 * @param	eventContext
	 * @param	value
	 */
	public WindowingAccelerationValueChangeEvent(EventContext eventContext,double value) {
		super(eventContext);
		this.value=value;
	}

	/**
	 * @return	the value selected
	 */
	public double getValue() { return value; }

	/**
	 * @return	description of the event
	 */
	public String toString() {
		return ("WindowingAccelerationValueChangeEvent: eventContext="+getEventContext()+" value="+value);
	}
}

