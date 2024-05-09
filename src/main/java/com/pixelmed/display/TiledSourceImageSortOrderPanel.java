/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display;

import java.awt.*; 
import java.awt.event.*; 
import java.awt.image.*; 
import javax.swing.*; 
import javax.swing.event.*;

import com.pixelmed.event.ApplicationEventDispatcher;
import com.pixelmed.event.EventContext; 
import com.pixelmed.event.SelfRegisteringListener;

import com.pixelmed.display.event.FrameSelectionChangeEvent; 
import com.pixelmed.display.event.FrameSortOrderChangeEvent; 
import com.pixelmed.display.event.SourceImageSelectionChangeEvent;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TiledFramesIndex;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * @author	dclunie
 */
class TiledSourceImageSortOrderPanel extends SourceInstanceSortOrderPanel {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/TiledSourceImageSortOrderPanel.java,v 1.19 2022/01/21 19:51:21 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(TiledSourceImageSortOrderPanel.class);

	// implement SourceImageSelectionChangeListener ...
	
	private OurSourceImageSelectionChangeListener ourSourceImageSelectionChangeListener;

	class OurSourceImageSelectionChangeListener extends SelfRegisteringListener {
	
		/**
		 * @param	eventContext
		 */
		public OurSourceImageSelectionChangeListener(EventContext eventContext) {
			super("com.pixelmed.display.event.SourceImageSelectionChangeEvent",eventContext);
//System.err.println("TiledSourceImageSortOrderPanel.OurSourceImageSelectionChangeListener():");
		}
		
		/**
		 * @param	e
		 */
		public void changed(com.pixelmed.event.Event e) {
			if (active) {
				SourceImageSelectionChangeEvent sis = (SourceImageSelectionChangeEvent)e;
				byFrameOrderButton.setSelected(true);
				nSrcInstances=sis.getNumberOfBufferedImages();			// sets in parent, else Slider won't appear when we update it later
				currentSrcInstanceAttributeList=sis.getAttributeList();
				replaceListOfDimensions(buildListOfDimensionsFromAttributeList(currentSrcInstanceAttributeList));
				currentSrcInstanceSortOrder=sis.getSortOrder();
				currentSrcInstanceIndex=sis.getIndex();
				updateCineSlider(1,nSrcInstances,currentSrcInstanceIndex+1);
				
				// may receive event if non-tiled image, so check before failing and throwing exception
				String sopClassUID = Attribute.getSingleStringValueOrEmptyString(currentSrcInstanceAttributeList,TagFromName.SOPClassUID);
				if (SOPClass.isTiledImageStorage(sopClassUID)) {
					try {
						tiledFramesIndex = new TiledFramesIndex(currentSrcInstanceAttributeList,true/*physical*/,true/*buildInverseIndex*/,false/*ignorePlanePosition*/);
						if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("\n{}",tiledFramesIndex.toString());		// could be a big string ... don't build it if we don't have to
						updateRowsSlider(1,tiledFramesIndex.getNumberOfRowsOfTiles(),1);
						updateColumnsSlider(1,tiledFramesIndex.getNumberOfColumnsOfTiles(),1);
					}
					catch (DicomException exc) {
						slf4jlogger.error("Failed to build index of tiled frames ",exc);
					}
				}
//System.err.println("TiledSourceImageSortOrderPanel.OurSourceImageSelectionChangeListener.changed(): on exit nSrcInstances = "+nSrcInstances);
//System.err.println("TiledSourceImageSortOrderPanel.OurSourceImageSelectionChangeListener.changed(): on exit currentSrcInstanceIndex = "+currentSrcInstanceIndex);
//System.err.println("TiledSourceImageSortOrderPanel.OurSourceImageSelectionChangeListener.changed(): on exit currentSrcInstanceSortOrder = "+currentSrcInstanceSortOrder);
			}
		}
	}

	// use instead of parent class so that we can update the row and column sliders when the frame changes
	
	class TiledSourceImageFrameSelectionChangeListener extends OurFrameSelectionChangeListener {
	
		public TiledSourceImageFrameSelectionChangeListener(EventContext eventContext) {
			super(eventContext);
			slf4jlogger.debug("TiledSourceImageFrameSelectionChangeListener():");
		}
		
		/**
		 * @param	e
		 */
		public void changed(com.pixelmed.event.Event e) {
			if (active) {
				super.changed(e);
				FrameSelectionChangeEvent fse = (FrameSelectionChangeEvent)e;
				if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("TiledSourceImageFrameSelectionChangeListener.changed(): event={}",fse.toString());
				int currentSrcInstanceIndex=fse.getIndex();
				slf4jlogger.debug("TiledSourceImageFrameSelectionChangeListener.changed(): currentSrcInstanceIndex = {}",currentSrcInstanceIndex);
				try {
					if (currentSrcInstanceIndex != -1 && tiledFramesIndex != null) {	// (001074) (001045)
						slf4jlogger.debug("TiledSourceImageFrameSelectionChangeListener.changed(): updating row and column sliders");
						int row = tiledFramesIndex.getRow(currentSrcInstanceIndex+1);
						int column = tiledFramesIndex.getColumn(currentSrcInstanceIndex+1);
						updateRowsSlider(row+1);
						updateColumnsSlider(column+1);
					}
				}
				catch (Exception exc) {
					slf4jlogger.error("Failed to update row and column sliders",exc);
				}
			}
		}
	}
	

	// our own variables and methods ...

	protected JPanel rowsSliderControlsPanel;
	protected JSlider rowsSlider;
	protected int currentRowsSliderMinimum;
	protected int currentRowsSliderMaximum;

	protected JPanel columnsSliderControlsPanel;
	protected JSlider columnsSlider;
	protected int currentColumnsSliderMinimum;
	protected int currentColumnsSliderMaximum;

	protected ChangeListener rowsSliderChangeListener;
	protected ChangeListener columnsSliderChangeListener;
	
	protected TiledFramesIndex tiledFramesIndex;
	
	public TiledFramesIndex getTiledFramesIndex() { return tiledFramesIndex; }

	// our own listeners in addition to using parent CineSliderChangeListener
	
	/***/
	private class RowsSliderChangeListener implements ChangeListener {
		/**
		 * @param	e
		 */
		public void stateChanged(ChangeEvent e) {
			int wantRow = rowsSlider == null ? 0 : rowsSlider.getValue()-1;
			int wantColumn = columnsSlider == null ? 0 : columnsSlider.getValue()-1;	// (001063)
			slf4jlogger.debug("RowsSliderChangeListener.stateChanged(): wantRow = {}",wantRow);
			slf4jlogger.debug("RowsSliderChangeListener.stateChanged(): wantColumn = {}",wantColumn);
			int frame = tiledFramesIndex.getFrameNumber(wantRow,wantColumn);
			slf4jlogger.debug("RowsSliderChangeListener.stateChanged(): tiledFramesIndex.getFrameNumber() = {}",frame);
			if (frame > 0) {
				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new FrameSelectionChangeEvent(typeOfPanelEventContext,frame-1));
			}
			else {
				slf4jlogger.warn("RowsSliderChangeListener.stateChanged(): request for unencoded tile row {} column {} in sparse matrix ",wantRow,wantColumn);
				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new FrameSelectionChangeEvent(typeOfPanelEventContext,-1));		// -1 is signal to clear the window
			}
		}
	}
	
	/***/
	private class ColumnsSliderChangeListener implements ChangeListener {
		/**
		 * @param	e
		 */
		public void stateChanged(ChangeEvent e) {
			int wantRow = rowsSlider == null ? 0 : rowsSlider.getValue()-1;
			int wantColumn = columnsSlider == null ? 0 : columnsSlider.getValue()-1;
			slf4jlogger.debug("ColumnsSliderChangeListener.stateChanged(): wantRow = {}",wantRow);
			slf4jlogger.debug("ColumnsSliderChangeListener.stateChanged(): wantColumn = {}",wantColumn);
			int frame = tiledFramesIndex.getFrameNumber(wantRow,wantColumn);
			slf4jlogger.debug("ColumnsSliderChangeListener.stateChanged(): tiledFramesIndex.getFrameNumber() = {}",frame);
			if (frame > 0) {
				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new FrameSelectionChangeEvent(typeOfPanelEventContext,frame-1));
			}
			else {
				slf4jlogger.warn("ColumnsSliderChangeListener.stateChanged(): request for unencoded tile row {} column {} in sparse matrix ",wantRow,wantColumn);
				ApplicationEventDispatcher.getApplicationEventDispatcher().processEvent(new FrameSelectionChangeEvent(typeOfPanelEventContext,-1));		// -1 is signal to clear the window
			}
		}
	}
	
	/**
	 * @param	min
	 * @param	max
	 * @param	value
	 */
	protected void updateColumnsSlider(int min,int max,int value) {
		slf4jlogger.debug("updateColumnsSlider(): min = {}",min);
		slf4jlogger.debug("updateColumnsSlider(): max = {}",max);
		slf4jlogger.debug("updateColumnsSlider(): value = {}",value);
		slf4jlogger.debug("updateColumnsSlider(): currentColumnsSliderMinimum = {}",currentColumnsSliderMinimum);
		slf4jlogger.debug("updateColumnsSlider(): currentColumnsSliderMaximum = {}",currentColumnsSliderMaximum);
		if (min != currentColumnsSliderMinimum || max != currentColumnsSliderMaximum) {
			slf4jlogger.debug("updateColumnsSlider(): removing and rebuilding slider (if needed)");
			columnsSliderControlsPanel.removeAll();
			if (max > min) {
				columnsSlider = new JSlider(min,max,value);	// don't leave to default, which is 50 and may be outside range
				columnsSlider.setLabelTable(columnsSlider.createStandardLabels(max-1,min));	// just label the ends
				columnsSlider.setPaintLabels(true);
				columnsSliderControlsPanel.add(new JLabel("Column:"));
				columnsSliderControlsPanel.add(columnsSlider);
				columnsSlider.addChangeListener(columnsSliderChangeListener);
			}
			else {
				slf4jlogger.debug("updateColumnsSlider(): slider not needed");
				columnsSlider=null;	// else single column so no slider
			}
			currentColumnsSliderMinimum=min;
			currentColumnsSliderMaximum=max;
		}
		
		if (columnsSlider != null && columnsSlider.getValue() != value) {
			columnsSlider.setValue(value);
		}
	}
	
	/**
	 * @param	value
	 */
	protected void updateColumnsSlider(int value) {
		slf4jlogger.debug("updateColumnsSlider(): value = {}",value);
		if (columnsSlider != null && columnsSlider.getValue() != value) columnsSlider.setValue(value);
	}
	
	/**
	 * @param	min
	 * @param	max
	 * @param	value
	 */
	protected void updateRowsSlider(int min,int max,int value) {
		slf4jlogger.debug("updateRowsSlider(): min = {}",min);
		slf4jlogger.debug("updateRowsSlider(): max = {}",max);
		slf4jlogger.debug("updateRowsSlider(): value = {}",value);
		slf4jlogger.debug("updateRowsSlider(): currentRowsSliderMinimum = {}",currentRowsSliderMinimum);
		slf4jlogger.debug("updateRowsSlider(): currentRowsSliderMaximum = {}",currentRowsSliderMaximum);
		if (min != currentRowsSliderMinimum || max != currentRowsSliderMaximum) {
			slf4jlogger.debug("updateRowsSlider(): removing and rebuilding slider (if needed)");
			rowsSliderControlsPanel.removeAll();
			if (max > min) {
				rowsSlider = new JSlider(min,max,value);	// don't leave to default, which is 50 and may be outside range
				rowsSlider.setLabelTable(rowsSlider.createStandardLabels(max-1,min));	// just label the ends
				rowsSlider.setPaintLabels(true);
				rowsSliderControlsPanel.add(new JLabel("Row:"));
				rowsSliderControlsPanel.add(rowsSlider);
				rowsSlider.addChangeListener(rowsSliderChangeListener);
			}
			else {
				slf4jlogger.debug("updateRowsSlider(): slider not needed");
				rowsSlider=null;	// else single row so no slider
			}
			currentRowsSliderMinimum=min;
			currentRowsSliderMaximum=max;
		}
		
		if (rowsSlider != null && rowsSlider.getValue() != value) {
			rowsSlider.setValue(value);
		}
	}
	
	/**
	 * @param	value
	 */
	protected void updateRowsSlider(int value) {
		slf4jlogger.debug("updateRowsSlider(): value = {}",value);
		if (rowsSlider != null && rowsSlider.getValue() != value) rowsSlider.setValue(value);
	}
	
	// overide parent class constructor to build our own GUI with two sliders instead of one
	
	/**
	 * @param	typeOfPanelEventContext
	 */
	public TiledSourceImageSortOrderPanel(EventContext typeOfPanelEventContext) {
		super(typeOfPanelEventContext);
		
		ourFrameSelectionChangeListener = new TiledSourceImageFrameSelectionChangeListener(typeOfPanelEventContext);
		
		rowsSliderControlsPanel = new JPanel();
		add(rowsSliderControlsPanel);
		rowsSliderChangeListener = new RowsSliderChangeListener();
		
		columnsSliderControlsPanel = new JPanel();
		add(columnsSliderControlsPanel);
		columnsSliderChangeListener = new ColumnsSliderChangeListener();

		ourSourceImageSelectionChangeListener = new OurSourceImageSelectionChangeListener(typeOfPanelEventContext);
	}
	
}


