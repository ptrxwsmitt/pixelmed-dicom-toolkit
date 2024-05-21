/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display;

import com.pixelmed.display.event.FrameSelectionChangeEvent;
import com.pixelmed.display.event.FrameSortOrderChangeEvent;
import com.pixelmed.display.event.SourceImageSelectionChangeEvent;
import com.pixelmed.display.event.StatusChangeEvent;
import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.event.Event;
import com.pixelmed.event.EventContext;
import com.pixelmed.event.SelfRegisteringListener;
import com.pixelmed.geometry.GeometryOfVolume;
import com.pixelmed.geometry.LocalizerPoster;
import com.pixelmed.geometry.LocalizerPosterFactory;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

abstract class LocalizerManager {
	
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/LocalizerManager.java,v 1.15 2022/01/21 19:51:20 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(LocalizerManager.class);
	
	/***/
	protected LocalizerPoster localizerPoster;
	/***/
	protected GeometryOfVolume referenceImageGeometry;
	/***/
	protected SingleImagePanel referencedImagePanel;
	/***/
	protected int referenceIndex;			// Already has been mapped through referenceImageSortOrder, if present
	/***/
	protected int[] referenceImageSortOrder;
	
	public LocalizerManager() {
		try {
			//localizerPoster = LocalizerPosterFactory.getLocalizerPoster(true/*project*/,true/*plane (irrelevant)*/);
			//localizerPoster = LocalizerPosterFactory.getLocalizerPoster(false/*intersect*/,true/*plane*/);
			localizerPoster = LocalizerPosterFactory.getLocalizerPoster(false/*intersect*/,false/*volume*/);
		}
		catch (Exception e) {
			slf4jlogger.error("",e);
		}
		catch (NoClassDefFoundError e) {
			slf4jlogger.error("",e);
		}
	}

	/**
	 */
	protected abstract void drawOutlineOnLocalizerReferenceImagePanel();

	/***/
	protected OurReferenceSourceImageSelectionChangeListener referenceSourceImageSelectionChangeListener;

	class OurReferenceSourceImageSelectionChangeListener extends SelfRegisteringListener {
	
		/**
		 * @param	eventContext
		 */
		public OurReferenceSourceImageSelectionChangeListener(EventContext eventContext) {
			super("com.pixelmed.display.event.SourceImageSelectionChangeEvent",eventContext);
		}
		
		/**
		 * @param	e
		 */
		public void changed(Event e) {
			SourceImageSelectionChangeEvent sis = (SourceImageSelectionChangeEvent)e;
			if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("OurReferenceSourceImageSelectionChangeListener.changed(): event {}",sis.toString());
			referenceImageSortOrder = sis.getSortOrder();
			referenceIndex = sis.getIndex();
			if (referenceImageSortOrder != null) {
				referenceIndex=referenceImageSortOrder[referenceIndex];
			}
			referenceImageGeometry=sis.getGeometryOfVolume();
			if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("OurReferenceSourceImageSelectionChangeListener.changed(): referenceImageGeometry {}",referenceImageGeometry.toString());
			if (referenceImageGeometry != null && referenceImageGeometry.getGeometryOfSlices() == null) {
				slf4jlogger.debug("OurReferenceSourceImageSelectionChangeListener.changed(): getGeometryOfSlices() is null, so not using referenceImageGeometry");
				referenceImageGeometry=null;
				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new StatusChangeEvent("Selected reference image does not contain necessary geometry for localization."));
			}
		}
	}
	
	/***/
	//protected EventContext referenceSourceImageSelectionContext;
	
	/*
	 * @param	referenceSourceImageSelectionContext
	 */
	public void setReferenceSourceImageSelectionContext(EventContext referenceSourceImageSelectionContext) {
		//this.referenceSourceImageSelectionContext=referenceSourceImageSelectionContext;
		if (referenceSourceImageSelectionChangeListener == null) {
			referenceSourceImageSelectionChangeListener = new OurReferenceSourceImageSelectionChangeListener(referenceSourceImageSelectionContext);
		}
		else {
			referenceSourceImageSelectionChangeListener.setEventContext(referenceSourceImageSelectionContext);
		}
	}
	
	/***/
	protected OurReferenceImageFrameSelectionChangeListener referenceImageFrameSelectionChangeListener;

	class OurReferenceImageFrameSelectionChangeListener extends SelfRegisteringListener {
	
		/**
		 * @param	eventContext
		 */
		public OurReferenceImageFrameSelectionChangeListener(EventContext eventContext) {
			super("com.pixelmed.display.event.FrameSelectionChangeEvent",eventContext);
		}
		
		/**
		 * @param	e
		 */
		public void changed(Event e) {
			FrameSelectionChangeEvent fse = (FrameSelectionChangeEvent)e;
			if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("OurReferenceImageFrameSelectionChangeListener.changed(): event {}",fse.toString());
			referenceIndex = fse.getIndex();
			if (referenceImageSortOrder != null) {
				referenceIndex=referenceImageSortOrder[referenceIndex];
			}
			if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("OurReferenceImageFrameSelectionChangeListener.changed(): referenceImageGeometry {}",referenceImageGeometry.toString());
			if (referenceImageGeometry != null) {
				drawOutlineOnLocalizerReferenceImagePanel();
			}
		}
	}
	
	/***/
	//protected EventContext referenceImageFrameSelectionContext;
	
	/*
	 * @param	referenceImageFrameSelectionContext
	 */
	public void setReferenceImageFrameSelectionContext(EventContext referenceImageFrameSelectionContext) {
		//this.referenceImageFrameSelectionContext=referenceImageFrameSelectionContext;
		if (referenceImageFrameSelectionChangeListener == null) {
			referenceImageFrameSelectionChangeListener = new OurReferenceImageFrameSelectionChangeListener(referenceImageFrameSelectionContext);
		}
		else {
			referenceImageFrameSelectionChangeListener.setEventContext(referenceImageFrameSelectionContext);
		}
	}
	
	/***/
	protected OurReferenceImageFrameSortOrderChangeListener referenceImageFrameSortOrderChangeListener;

	class OurReferenceImageFrameSortOrderChangeListener extends SelfRegisteringListener {
	
		/**
		 * @param	eventContext
		 */
		public OurReferenceImageFrameSortOrderChangeListener(EventContext eventContext) {
			super("com.pixelmed.display.event.FrameSortOrderChangeEvent",eventContext);
		}
		
		/**
		 * @param	e
		 */
		public void changed(Event e) {
			slf4jlogger.debug("OurReferenceImageFrameSortOrderChangeListener.changed():");
			FrameSortOrderChangeEvent fso = (FrameSortOrderChangeEvent)e;
			referenceImageSortOrder = fso.getSortOrder();
			referenceIndex = fso.getIndex();
			if (referenceImageSortOrder != null) {
				referenceIndex=referenceImageSortOrder[referenceIndex];
			}
			if (referenceImageGeometry != null) {
				drawOutlineOnLocalizerReferenceImagePanel();
			}
		}
	}

	/***/
	//protected EventContext referenceImageFrameSortOrderContext;
	
	/*
	 * @param	referenceSourceImageSelectionContext
	 */
	public void setReferenceImageFrameSortOrderContext(EventContext referenceImageFrameSortOrderContext) {
		//this.referenceImageFrameSortOrderContext=referenceImageFrameSortOrderContext;
		if (referenceImageFrameSortOrderChangeListener == null) {
			referenceImageFrameSortOrderChangeListener = new OurReferenceImageFrameSortOrderChangeListener(referenceImageFrameSortOrderContext);
		}
		else {
			referenceImageFrameSortOrderChangeListener.setEventContext(referenceImageFrameSortOrderContext);
		}
	}
	
	public void setReferenceImagePanel(SingleImagePanel referencedImagePanel) {
		slf4jlogger.debug("setReferenceImagePanel():");
		this.referencedImagePanel=referencedImagePanel;
	}
		
	public void reset() {
		slf4jlogger.debug("reset():");
		referenceImageGeometry=null;
		referencedImagePanel=null;
		referenceIndex=0;
		referenceImageSortOrder=null;
	}
	
}

