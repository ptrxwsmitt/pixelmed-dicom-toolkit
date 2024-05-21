/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.event;

//import java.util.EventObject;

/**
 * @author	dclunie
 */
public class Event /*extends EventObject*/ {	// why bother extending java.util.EventObject

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/event/Event.java,v 1.10 2022/01/21 19:51:23 dclunie Exp $";
	
	/***/
	private EventContext eventContext;			// may be null

	/**
	 */
	public Event() {
		//super(source);		// EventObject throws exception if source is null
	}

	/**
	 * @param	source
	 */
	public Event(Object source) {
		//super(source);		// EventObject throws exception if source is null
	}

	/**
	 * @param	source
	 * @param	eventContext
	 */
	public Event(Object source,EventContext eventContext) {
		//super(source);		// EventObject throws exception if source is null
		this.eventContext=eventContext;
	}

	/**
	 * @param	eventContext
	 */
	public Event(EventContext eventContext) {
		//super(source);		// EventObject throws exception if source is null
		this.eventContext=eventContext;
	}

	/**
	 * @return	the context of the event
	 */
	public final EventContext getEventContext() {
		return eventContext;
	}
}

