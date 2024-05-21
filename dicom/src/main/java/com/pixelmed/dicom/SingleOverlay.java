/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A single bitmap overlay plane</p>
 *
 * @author	dclunie
 */
public class SingleOverlay {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/SingleOverlay.java,v 1.5 2022/01/21 19:51:18 dclunie Exp $";
	
	private static final Logger slf4jlogger = LoggerFactory.getLogger(SingleOverlay.class);
	
	private short group;
	private short[] data;
	private int rows;
	private int columns;
	private int frames;
	private int rowOrigin;			// from 0 (not 1 as in DICOM attribute)
	private int columnOrigin;		// from 0 (not 1 as in DICOM attribute)
	private int frameOrigin;		// from 0 (not 1 as in DICOM attribute)
	private int bitPosition;		// only used if data needs to be extracted from PixelData (not used in rendering overlay)
	private String type;
	private String subtype;
	private String label;
	private String description;
	private int area;
	private double mean;
	private double standardDeviation;
	
	private void doCommonConstructorStuff(short group,short[] data,
										  int rows,int columns,int frames,int rowOrigin,int columnOrigin,int frameOrigin,int bitPosition,
										  String type,String subtype,String label,String description,
										  int area,double mean,double standardDeviation) {
		this.group=group;
		this.data=data;
		this.rows=rows;
		this.columns=columns;
		this.frames=frames;
		this.rowOrigin=rowOrigin;
		this.columnOrigin=columnOrigin;
		this.frameOrigin=frameOrigin;
		this.bitPosition=bitPosition;
		this.type=type;
		this.subtype=subtype;
		this.label=label;
		this.description=description;
		this.area=area;
		this.mean=mean;
		this.standardDeviation=standardDeviation;
	}
	
	/**
	 * @param	group				the group number (0x6000 through 0x600f)
	 * @param	data
	 * @param	rows
	 * @param	columns
	 * @param	frames
	 * @param	rowOrigin			relative to center of top row of image, numbered from 0 (not 1 as in DICOM attribute)
	 * @param	columnOrigin		relative to center of left column pixel of image, numbered from 0 (not 1 as in DICOM attribute)
	 * @param	frameOrigin			relative to first frame of image, numbered from 0 (not 1 as in DICOM attribute)
	 * @param	bitPosition			only used if data needs to be extracted from PixelData (not used in rendering overlay)
	 * @param	type
	 * @param	subtype
	 * @param	label
	 * @param	description
	 * @param	area				0 if none
	 * @param	mean				0 if none
	 * @param	standardDeviation	0 if none
	 */
	public SingleOverlay(short group,short[] data,
						 int rows,int columns,int frames,int rowOrigin,int columnOrigin,int frameOrigin,int bitPosition,
						 String type,String subtype,String label,String description,
						 int area,double mean,double standardDeviation) {
		doCommonConstructorStuff(group,data,
								 rows,columns,frames,rowOrigin,columnOrigin,frameOrigin,bitPosition,
								 type,subtype,label,description,
								 area,mean,standardDeviation);
	}
	
	public SingleOverlay(AttributeList list,short overlayGroup) throws DicomException {
		assert (overlayGroup%2 == 0 && overlayGroup >= 0x6000 && overlayGroup <= 0x601e);
		
		Attribute aOverlayData = list.get(new AttributeTag(overlayGroup,TagFromName.OverlayData.getElement()));
		
		int vRows = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Rows,-1);
		int vOverlayRows = Attribute.getSingleIntegerValueOrDefault(list,new AttributeTag(overlayGroup,TagFromName.OverlayRows.getElement()),vRows);
		int vColumns = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Columns,-1);
		int vOverlayColumns = Attribute.getSingleIntegerValueOrDefault(list,new AttributeTag(overlayGroup,TagFromName.OverlayColumns.getElement()),vColumns);
		
		int rowOriginFrom1 = 1;
		int columnOriginFrom1 = 1;
		{
			int[] vOverlayOrigin = Attribute.getIntegerValues(list,new AttributeTag(overlayGroup,TagFromName.OverlayOrigin.getElement()));	// should be of length 2
			if (vOverlayOrigin != null) {
				if (vOverlayOrigin.length >= 2) {		// should never actually be > 2
					rowOriginFrom1 = vOverlayOrigin[0];
					columnOriginFrom1 = vOverlayOrigin[1];
				}
				else if (vOverlayOrigin.length == 1) {		// illegal, but guess that this is what was intended
					rowOriginFrom1 = vOverlayOrigin[0];
					columnOriginFrom1 = vOverlayOrigin[0];
				}
			}
		}
		
		int vOverlayBitPosition = aOverlayData != null ? 0 : Attribute.getSingleIntegerValueOrDefault(list,new AttributeTag(overlayGroup,TagFromName.OverlayBitPosition.getElement()),(aOverlayData == null ? -1 : 0));
		int vBitsAllocated = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsAllocated,-1);
		int vOverlayBitsAllocated = aOverlayData != null ? 1 : Attribute.getSingleIntegerValueOrDefault(list,new AttributeTag(overlayGroup,TagFromName.OverlayBitsAllocated.getElement()),(vOverlayBitPosition == 0 ? 1 : vBitsAllocated));
		
		String vOverlayType = Attribute.getSingleStringValueOrDefault(list,new AttributeTag(overlayGroup,TagFromName.OverlayType.getElement()),"G").trim();		// default to Graphics rather than ROI (who really cares ?)
		String vOverlaySubtype = Attribute.getSingleStringValueOrDefault(list,new AttributeTag(overlayGroup,TagFromName.OverlaySubtype.getElement()),"").trim();
		String vOverlayLabel = Attribute.getSingleStringValueOrDefault(list,new AttributeTag(overlayGroup,TagFromName.OverlayLabel.getElement()),"").trim();
		String vOverlayDescription = Attribute.getSingleStringValueOrDefault(list,new AttributeTag(overlayGroup,TagFromName.OverlayDescription.getElement()),"").trim();
		
		int vNumberOfFramesInOverlay = Attribute.getSingleIntegerValueOrDefault(list,new AttributeTag(overlayGroup,TagFromName.NumberOfFramesInOverlay.getElement()),1);
		int vImageFrameOrigin = Attribute.getSingleIntegerValueOrDefault(list,new AttributeTag(overlayGroup,TagFromName.ImageFrameOrigin.getElement()),1);			// assume starts at first frame if absent
		
		int vROIArea = Attribute.getSingleIntegerValueOrDefault(list,new AttributeTag(overlayGroup,TagFromName.ROIArea.getElement()),0);
		double vROIMean = Attribute.getSingleDoubleValueOrDefault(list,new AttributeTag(overlayGroup,TagFromName.ROIMean.getElement()),0);
		double vROIStandardDeviation = Attribute.getSingleDoubleValueOrDefault(list,new AttributeTag(overlayGroup,TagFromName.ROIStandardDeviation.getElement()),0);
		
		short[] vOverlayData = null;
		if (vOverlayRows > 0 && vOverlayColumns > 0) {
			int lengthInWords = (vOverlayRows * vOverlayColumns * vNumberOfFramesInOverlay - 1) / 16 + 1;
			if (aOverlayData != null) {
				//System.err.println("SingleOverlay(): Extraction of overlay from OverlayData (class ="+aOverlayData.getClass()+")");
				if (aOverlayData instanceof OtherByteAttribute) {
					byte[] vOverlayDataBytes = aOverlayData.getByteValues();
					vOverlayData = new short[lengthInWords];
					int bi = 0;
					for (int wi = 0; wi < lengthInWords; ++wi) {
						if (bi < vOverlayDataBytes.length) {
							vOverlayData[wi] = (short)(vOverlayDataBytes[bi] & 0x00ff);
						}
						++bi;
						if (bi < vOverlayDataBytes.length) {
							vOverlayData[wi] = (short)(vOverlayData[wi] | (((short)(vOverlayDataBytes[bi]) << 8) & 0xff00));
						}
						++bi;
					}
				}
				else if (aOverlayData instanceof OtherWordAttribute) {
					vOverlayData =  aOverlayData.getShortValues();
				}
				else {
					throw new DicomException("OverlayData attribute present in group 0x"+Integer.toHexString(overlayGroup)+" but unsupported or bad VR");
				}
				if (vOverlayData == null) {
					throw new DicomException("OverlayData attribute present in group 0x"+Integer.toHexString(overlayGroup)+" but no values");
				}
				else if (vOverlayData.length < lengthInWords) {
					throw new DicomException("OverlayData in group 0x"+Integer.toHexString(overlayGroup)+" is too short (got "+vOverlayData.length+" dec words, expected "+lengthInWords+" dec words");
				}
			}
			else if (vOverlayBitPosition != -1 && vOverlayBitsAllocated > 0 && vOverlayBitsAllocated == vBitsAllocated) {
				vOverlayData = new short[lengthInWords];		// space is allocated now; actual entries will need to be set later when traversing PixelData for other reasons
				int vSamplesPerPixel = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.SamplesPerPixel,1);
				if (vSamplesPerPixel != 1) {
					throw new DicomException("SamplesPerPixel must be 1, not"+vSamplesPerPixel+", to use overlay in PixelData in group 0x"+Integer.toHexString(overlayGroup));
				}
				if (vOverlayRows != vRows || vOverlayColumns != vColumns) {
					throw new DicomException("OverlayRow and OverlayColumns must equal image Rows and Columns respectively, to use overlay in PixelData in group 0x"+Integer.toHexString(overlayGroup));
				}
				Attribute aPixelData = list.getPixelData();
				if (aPixelData == null) {
					throw new DicomException("No PixelData from which to extract overlay in group 0x"+Integer.toHexString(overlayGroup));
				}
				else {
					if (aPixelData instanceof OtherWordAttributeOnDisk) {
						slf4jlogger.info("SingleOverlay(): Extraction of overlay from PixelData left on disk not supported yet");
					}
					else {
						//System.err.println("SingleOverlay(): Extraction of overlay from PixelData in memory for bit position "+vOverlayBitPosition);
						int position = vOverlayBitPosition;
						int sourceMask = 1;
						while (position-- > 0) {
							sourceMask = sourceMask<<1;
						}
						//System.err.println("SingleOverlay(): sourceMask = 0x"+Integer.toHexString(sourceMask));
						short vPixelData[] = aPixelData.getShortValues();
						int sourceWordIndex = (vImageFrameOrigin-1) * vRows * vColumns;		// where in PixelData the first overlay frame begins
						//System.err.println("SingleOverlay(): sourceWordIndex = "+sourceWordIndex);
						int targetWordIndex = 0;
						int targetBitsDone = 0;
						int targetWord = 0;
						//System.err.println("SingleOverlay(): vNumberOfFramesInOverlay = "+vNumberOfFramesInOverlay);
						for (int f=0; f<vNumberOfFramesInOverlay; ++f) {
							for (int row=0; row<vRows; ++row) {
								for (int column=0; column<vColumns; ++column) {
									++targetBitsDone;
									if (targetBitsDone >= 16) {
										vOverlayData[targetWordIndex++] = (short)targetWord;
										targetWord = 0;
										targetBitsDone = 0;
									}
									else {
										targetWord = targetWord >> 1;
									}
									int bit = (vPixelData[sourceWordIndex++] & sourceMask) > 0 ? 0x8000 : 0;
									//if (bit != 0) System.err.println("SingleOverlay(): Setting bit for ("+f+","+row+","+column+")");
									targetWord |= bit;
								}
							}
						}
					}
				}
			}
			doCommonConstructorStuff(overlayGroup,vOverlayData,
									 vOverlayRows,vOverlayColumns,vNumberOfFramesInOverlay,
									 rowOriginFrom1-1,columnOriginFrom1-1,vImageFrameOrigin-1,vOverlayBitPosition,
									 vOverlayType,vOverlaySubtype,vOverlayLabel,vOverlayDescription,
									 vROIArea,vROIMean,vROIStandardDeviation);
		}
		else {
			throw new DicomException("Cannot construct overlay from inconsistent or missing overlay attributes in group 0x"+Integer.toHexString(overlayGroup));
		}
	}
	
	/**
	 * Does this overlay apply to a particular frame.
	 *
	 * @param	frame		numbered from zero
	 * @return				true if the overlay applies to this frame
	 */
	boolean appliesToFrame(int frame) {
		return frame >= frameOrigin && frame < frameOrigin + frames;
	}
	
	BufferedImage[] imagesForEachFrame = null;

	/**
	 * Set the overlay bitmap from a binary image.
	 *
	 * @param	image	the image for this frame
	 * @param	frame	numbered from zero; needed to select which overlay if frame-specific
	 */
	public void setOverlayFromBinaryBufferedImage(BufferedImage image,int frame) {
		Raster raster = image.getData();
		SampleModel sampleModel = raster.getSampleModel();
		DataBuffer dataBuffer = raster.getDataBuffer();
		int wordIndex=data[0] & 0xffff;
		int bitsUsed=0;
		int word = 0;
		for (int f=0; f<frames; ++f) {			// incredibly inefficient if we actually ever use more than one frame ! :(
			for (int row=0; row<rows; ++row) {
				for (int column=0; column<columns; ++column) {
					if (f == frame) {
						int sample = sampleModel.getSample(column,row,0/*bank*/,dataBuffer);
						int newword = word | ((sample & 0x0001) << bitsUsed);
//if (sample != 0) System.err.println("setOverlayFromBinaryBufferedImage(): in frame "+f+" row "+row+" column "+column+" bitsUsed "+bitsUsed+" sample "+sample+" word was 0x"+Integer.toHexString(word)+" is now 0x"+Integer.toHexString(newword));
						word = newword;
					}
					// else leave it alone
					++bitsUsed;
					if (bitsUsed >= 16) {
						data[wordIndex] = (short)word;
						word = data[++wordIndex] & 0xffff;
						bitsUsed=0;
					}
				}
			}
		}
	}

	/**
	 * Get a binary image constructed from the overlay bitmap.
	 *
	 * @param	frame		numbered from zero; needed to select which overlay if frame-specific
	 * @return				a java.awt.image.BufferedImage of type TYPE_BYTE_BINARY, or null if there is no such overlay for that frame
	 */
	public BufferedImage getOverlayAsBinaryBufferedImage(int frame) {
		boolean foundSomething = false;
		BufferedImage image = null;
		if (appliesToFrame(frame)) {
			// lazy instantiation of everything ...
			if (imagesForEachFrame == null) {
				imagesForEachFrame = new BufferedImage[frames];
				if (data != null) {
					byte[] r = { (byte)0, (byte)255 };
					byte[] g = { (byte)0, (byte)255 };
					byte[] b = { (byte)0, (byte)255 };
					IndexColorModel colorModel = new IndexColorModel(
																	 1 /* bits */,
																	 2 /* size */,
																	 r,g,b,
																	 0 /* the first index (black value) is to be transparent */);
					
					int wordIndex=0;
					int bitsRemaining=0;
					int word = 0;
					for (int f=0; f<frames; ++f) {
						//System.err.println("SingleOverlay.getOverlayAsBinaryBufferedImage(): Doing frame "+f);
						imagesForEachFrame[f] = new BufferedImage(columns,rows,BufferedImage.TYPE_BYTE_BINARY,colorModel);
						Raster raster = imagesForEachFrame[f].getData();
						SampleModel sampleModel = raster.getSampleModel();
						DataBuffer dataBuffer = raster.getDataBuffer();
						for (int row=0; row<rows; ++row) {
							for (int column=0; column<columns; ++column) {
								if (bitsRemaining <= 0) {
									word = data[wordIndex++] & 0xffff;
									bitsRemaining=16;
								}
								int bit = word & 0x0001;
								if (bit > 0) {
									sampleModel.setSample(column,row,0/*bank*/,1,dataBuffer);
									foundSomething = true;
								}
								word = word >>> 1;
								--bitsRemaining;
							}
						}
						imagesForEachFrame[f].setData(raster);
					}
					//System.err.println("SingleOverlay.getOverlayAsBinaryBufferedImage(): in "+this.toString()+" foundSomething = "+foundSomething);
				}
			}
			int frameIndex = frame-frameOrigin;
			image = imagesForEachFrame[frameIndex];
		}
		return image;
	}
	
	public int getRowOrigin(int frame)		{ return rowOrigin; }
	public int getColumnOrigin(int frame)	{ return columnOrigin; }

	public int getFrameOrigin()	{ return frameOrigin; }
	public int getFrames()	{ return frames; }
	
	public int getRows()		{ return rows; }
	public int getColumns()	{ return columns; }
	public int getBitPosition()	{ return bitPosition; }

	public short getGroup()	{ return group; }

	public short[] getData()	{ return data; }

	public final String toString() {
		return "Overlay group 0x"+Integer.toHexString(group)
		+": rows="+rows+",columns="+columns+",frames="+frames
		+"; rowOrigin="+rowOrigin+",columnOrigin="+columnOrigin+",frameOrigin="+frameOrigin
		+"; bitPosition="+bitPosition
		+"; type="+type+",subtype="+subtype+",label="+label+",description=\""+description+"\""
		+"; area="+area+",mean="+mean+",standardDeviation="+standardDeviation;
	}
}

