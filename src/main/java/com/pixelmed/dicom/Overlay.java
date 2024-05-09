/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.awt.image.BufferedImage;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A set of bitmap overlays constructed from a DICOM attribute list.</p>
 *
 * <p>Note that multiple overlays may be present, they may be multi-frame, and they may be in the OverlayData element or the PixelData element.</p>
 *
 * @author	dclunie
 */
public class Overlay {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/Overlay.java,v 1.19 2022/01/21 19:51:17 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(Overlay.class);
	
	private int numberOfFrames;
	
	private SingleOverlay[] arrayOfOverlays;
	private SingleOverlay[][] arrayOfOverlaysPerFrame;
	private int[] numberOfOverlaysPerFrame;
	
	private void constructArrayOfOverlaysPerFrame() {
		slf4jlogger.debug("constructArrayOfOverlaysPerFrame(): numberOfFrames =  {}",numberOfFrames);
		if (arrayOfOverlaysPerFrame == null) {
			slf4jlogger.debug("constructArrayOfOverlaysPerFrame(): have arrayOfOverlaysPerFrame");
			arrayOfOverlaysPerFrame = new SingleOverlay[numberOfFrames][];
			numberOfOverlaysPerFrame = new int[numberOfFrames];
			for (int f=0; f<numberOfFrames; ++f) {
				slf4jlogger.debug("constructArrayOfOverlaysPerFrame(): doing frame {}",f);
				int numberOfSingleOverlays = arrayOfOverlays.length;
				arrayOfOverlaysPerFrame[f] = new SingleOverlay[numberOfSingleOverlays];
				for (int o=0; o<numberOfSingleOverlays; ++o) {
					SingleOverlay overlay = arrayOfOverlays[o];
					if (overlay != null && overlay.appliesToFrame(f)) {
						arrayOfOverlaysPerFrame[f][o]=overlay;
						++numberOfOverlaysPerFrame[f];
					}
				}
			}
		}
	}


	/**
	 * @param	arrayOfOverlays
	 */
	public Overlay(SingleOverlay[] arrayOfOverlays) {
		this.arrayOfOverlays = arrayOfOverlays;
		arrayOfOverlaysPerFrame = null;
		numberOfOverlaysPerFrame = null;
		numberOfFrames = 1;
		for (SingleOverlay singleOverlay : arrayOfOverlays) {
			int framesInThisSingleOverlay = singleOverlay.getFrames();
			int frameoriginForThisSingleOverlay = singleOverlay.getFrameOrigin();	// is from zero
			int numberOfFramesForThisSingleOverlay = frameoriginForThisSingleOverlay + framesInThisSingleOverlay;
			if (numberOfFramesForThisSingleOverlay > numberOfFrames) {
				numberOfFrames = numberOfFramesForThisSingleOverlay;
			}
		}
	}
	
	public AttributeList getAttributeList() throws DicomException {
		AttributeList list = new AttributeList();
		{Attribute a = new IntegerStringAttribute(TagFromName.NumberOfFrames); a.addValue(numberOfFrames); list.put(a); }

		for (SingleOverlay singleOverlay : arrayOfOverlays) {
			short overlayGroup = singleOverlay.getGroup();
			
			{Attribute a = new OtherWordAttribute(new AttributeTag(overlayGroup,TagFromName.OverlayData.getElement())); a.setValues(singleOverlay.getData()); list.put(a); }
			
			{Attribute a = new UnsignedShortAttribute(new AttributeTag(overlayGroup,TagFromName.OverlayRows.getElement())); a.addValue(singleOverlay.getRows()); list.put(a); }
			{Attribute a = new UnsignedShortAttribute(new AttributeTag(overlayGroup,TagFromName.OverlayColumns.getElement())); a.addValue(singleOverlay.getColumns()); list.put(a); }
			{Attribute a = new UnsignedShortAttribute(new AttributeTag(overlayGroup,TagFromName.OverlayBitPosition.getElement())); a.addValue(singleOverlay.getBitPosition()); list.put(a); }
			
			{Attribute a = new UnsignedShortAttribute(new AttributeTag(overlayGroup,TagFromName.OverlayBitsAllocated.getElement())); a.addValue(1); list.put(a); }
			{Attribute a = new SignedShortAttribute(new AttributeTag(overlayGroup,TagFromName.OverlayOrigin.getElement())); a.addValue(singleOverlay.getRowOrigin(0/*frame*/)+1); a.addValue(singleOverlay.getColumnOrigin(0/*frame*/)+1); list.put(a); }

			{Attribute a = new CodeStringAttribute(new AttributeTag(overlayGroup,TagFromName.OverlayType.getElement())); a.addValue("G"); list.put(a); }
		}
		
		return list;
	}
	
	/**
	 * @param	list
	 */
	public Overlay(AttributeList list) {
		arrayOfOverlays = new SingleOverlay[16];
		arrayOfOverlaysPerFrame = null;
		numberOfOverlaysPerFrame = null;
		numberOfFrames = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfFrames,1);
		for (short o=0; o<32; o+=2) {
			int index=o/2;
			arrayOfOverlays[index] = null;
			short overlayGroup = (short)(0x6000 + o);
			Attribute aOverlayData = list.get(new AttributeTag(overlayGroup,TagFromName.OverlayData.getElement()));
			Attribute aOverlayRows = list.get(new AttributeTag(overlayGroup,TagFromName.OverlayRows.getElement()));
			Attribute aOverlayColumns = list.get(new AttributeTag(overlayGroup,TagFromName.OverlayColumns.getElement()));
			Attribute aOverlayBitPosition = list.get(new AttributeTag(overlayGroup,TagFromName.OverlayBitPosition.getElement()));
			
			if (aOverlayData != null || aOverlayRows != null || aOverlayColumns != null || aOverlayBitPosition != null) {	// the presence of any one of this indicates there may be an overlay
				try {
					arrayOfOverlays[index] = new SingleOverlay(list,overlayGroup);
				}
				catch (DicomException e) {
					slf4jlogger.error("",e);
				}
			}
		}
	}
	
	/**
	 * Get the number of overlays available for a particular frame.
	 *
	 * @param	frame		numbered from zero; needed to select which overlay if frame-specific
	 * @return				the number of overlays available for the frame, 0 if none
	 */
	public int getNumberOfOverlays(int frame) {
		constructArrayOfOverlaysPerFrame();
		return numberOfOverlaysPerFrame[frame];
	}

	/**
	 * Get a binary image constructed from the overlay bitmap.
	 *
	 * @param	frame		numbered from zero; needed to select which overlay if frame-specific
	 * @param	overlay		numbered from zero
	 * @return				a java.awt.image.BufferedImage of type TYPE_BYTE_BINARY, or null if there is no such overlay for that frame
	 */
	public BufferedImage getOverlayAsBinaryBufferedImage(int frame,int overlay) {
		constructArrayOfOverlaysPerFrame();
		SingleOverlay singleOverlay = arrayOfOverlaysPerFrame[frame][overlay];
		return singleOverlay == null ? null : singleOverlay.getOverlayAsBinaryBufferedImage(frame);
	}
	
	/**
	 * Get the row orgin of the overlay.
	 *
	 * @param	frame		numbered from zero; needed to select which overlay if frame-specific
	 * @param	overlay		numbered from zero
	 * @return				the origin, with zero being the top row of the image (not 1 as in the DICOM OverlayOrigin attribute), or zero if there is no such overlay
	 */
	public int getRowOrigin(int frame,int overlay) {
		constructArrayOfOverlaysPerFrame();
		SingleOverlay singleOverlay = arrayOfOverlaysPerFrame[frame][overlay];
		return singleOverlay == null ? 0 : singleOverlay.getRowOrigin(frame);
	}
	
	/**
	 * Get the column orgin of the overlay.
	 *
	 * @param	frame		numbered from zero; needed to select which overlay if frame-specific
	 * @param	overlay		numbered from zero
	 * @return				the origin, with zero being the left column of the image (not 1 as in the DICOM OverlayOrigin attribute), or zero if there is no such overlay
	 */
	public int getColumnOrigin(int frame,int overlay) {
		constructArrayOfOverlaysPerFrame();
		SingleOverlay singleOverlay = arrayOfOverlaysPerFrame[frame][overlay];
		return singleOverlay == null ? 0 : singleOverlay.getColumnOrigin(frame);
	}
	
	/***/
	public final String toString() {
		StringBuffer strbuf = new StringBuffer();
		for (int i=0; i<arrayOfOverlays.length; ++i) {
			if (arrayOfOverlays[i] != null) {
				strbuf.append(arrayOfOverlays[i].toString());
				strbuf.append("\n");
			}
		}
		return strbuf.toString();
	}

	/**
	 * <p>Read the DICOM input file as a list of attributes and extract the information related to overlays.</p>
	 *
	 * @param	arg	array of one string (the filename to read and dump)
	 */
	public static void main(String arg[]) {
		if (arg.length == 1) {
			String inputFileName = arg[0];
			try {
				AttributeList list = new AttributeList();
				list.read(inputFileName);
				Overlay overlay = new Overlay(list);
				System.err.print(overlay);	// no need to use SLF4J since command line utility/test
				System.err.println("getNumberOfOverlays(frame 0) = "+overlay.getNumberOfOverlays(0));	// no need to use SLF4J since command line utility/test
				System.err.println("getOverlayAsBinaryBufferedImage(frame 0,overlay 0) = "+overlay.getOverlayAsBinaryBufferedImage(0,0));	// no need to use SLF4J since command line utility/test
			} catch (Exception e) {
				e.printStackTrace(System.err);	// no need to use SLF4J since command line utility/test
			}
		}
	}
}

