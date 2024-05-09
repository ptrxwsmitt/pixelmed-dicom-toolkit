/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display.event;

import com.pixelmed.event.Event;
import com.pixelmed.event.EventContext;

/**
 * @author	dclunie
 */
public class GraphicDisplayChangeEvent extends Event {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/event/GraphicDisplayChangeEvent.java,v 1.10 2022/01/21 19:51:21 dclunie Exp $";

	private boolean overlays;

	/**
	 * @param	eventContext
	 * @param	overlays
	 */
	public GraphicDisplayChangeEvent(EventContext eventContext,boolean overlays) {
		super(eventContext);
		this.overlays=overlays;
//System.err.println("GraphicDisplayChangeEvent() overlays = "+overlays);
	}

	/***/
	public boolean showOverlays() { return overlays; }

}

