/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.BinaryOutputStream;
import com.pixelmed.dicom.ClinicalTrialsAttributes;
import com.pixelmed.dicom.CompressedFrameEncoder;
import com.pixelmed.dicom.CodedSequenceItem;
import com.pixelmed.dicom.CodeStringAttribute;
import com.pixelmed.dicom.DecimalStringAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.IntegerStringAttribute;
import com.pixelmed.dicom.OtherByteAttributeMultipleCompressedFrames;
import com.pixelmed.dicom.OtherByteAttributeMultipleFilesOnDisk;
import com.pixelmed.dicom.OtherWordAttributeMultipleFilesOnDisk;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.ShortTextAttribute;
import com.pixelmed.dicom.SignedLongAttribute;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TiledFramesIndex;
import com.pixelmed.dicom.UIDGenerator;
import com.pixelmed.dicom.UniqueIdentifierAttribute;
import com.pixelmed.dicom.UnsignedLongAttribute;
import com.pixelmed.dicom.UnsignedShortAttribute;
import com.pixelmed.dicom.VersionAndConstants;

import com.pixelmed.display.SourceImage;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
//import java.awt.image.ComponentSampleModel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>Take a single high resolution tiled image and downsample it by successive factors of two to produce a multi-resolution pyramid set of images.</p>
 *
 * @author	dclunie
 */
public class TiledPyramid {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/TiledPyramid.java,v 1.23 2022/05/19 11:33:17 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(TiledPyramid.class);
	
	protected UIDGenerator generator = new UIDGenerator();

	protected static void updateImageOrFrameType(AttributeList list,AttributeList newList,AttributeTag t) throws DicomException {
		String[] values = null;
		int vm = 0;
		{
			Attribute a = list.get(t);
			if (a != null) {
				values = a.getStringValues();
				vm = values.length;
			}
		}
		if (vm < 4) {
			vm = 4;
		}
		{
			Attribute a = new CodeStringAttribute(t);
			for (int i=0; i<vm; ++i) {
				if (i == 0) {
					a.addValue("DERIVED");
				}
				else if (i == 3) {
					a.addValue("RESAMPLED");		// per PS3.3 Table C.8.12.4-3.
				}
				else {
					if (i<values.length) {
						a.addValue(values[i]);	// no reason to change or "fix" it
					}
					else {
						if (i == 1) {
							a.addValue("PRIMARY");
						}
						else if (i == 2) {
							a.addValue("VOLUME");
						}
						else {
							a.addValue("");		// should never happen, since vm will never be > 4 if number of old values less than 4, and all other cases already handled
						}
					}
				}
			}
			newList.put(a);
		}
	}

	// NB. reuses various attributes when copied to new list, so their value in the old list becomes unreliable.
	protected static void copyFunctionalGroupsSequenceWithDownsampledValues(AttributeList list,AttributeList newList,
			TiledFramesIndex index,int oldNumberOfFrames,int newNumberOfFrames,int newNumberOfColumnsOfTiles,int newNumberOfRowsOfTiles,
			int columns,int rows
		) throws DicomException {
		SequenceAttribute sharedFunctionalGroupsSequence = (SequenceAttribute)list.get(TagFromName.SharedFunctionalGroupsSequence);
		if (sharedFunctionalGroupsSequence == null || sharedFunctionalGroupsSequence.getNumberOfItems() != 1) {
			throw new DicomException("Missing SharedFunctionalGroupsSequence or incorrect number of items");
		}
		else {
			newList.put(sharedFunctionalGroupsSequence);
			AttributeList sharedFunctionalGroupsSequenceItemList = sharedFunctionalGroupsSequence.getItem(0).getAttributeList();	// OK since checked has 1 item
			{
				SequenceAttribute pixelMeasuresSequence = (SequenceAttribute)sharedFunctionalGroupsSequenceItemList.get(TagFromName.PixelMeasuresSequence);
				if (pixelMeasuresSequence == null || pixelMeasuresSequence.getNumberOfItems() != 1) {
					throw new DicomException("Missing PixelMeasuresSequence in SharedFunctionalGroupsSequence or incorrect number of items");
				}
				else {
					AttributeList pixelMeasuresSequenceItemList = SequenceAttribute.getAttributeListFromSelectedItemWithinSequence(pixelMeasuresSequence,0);
					Attribute aPixelSpacing = pixelMeasuresSequenceItemList.get(TagFromName.PixelSpacing);
					if (aPixelSpacing == null || aPixelSpacing.getVM() != 2) {
						throw new DicomException("Missing PixelSpacing in PixelMeasuresSequence in SharedFunctionalGroupsSequence or incorrect number of values");
					}
					else {
						double[] vPixelSpacing = aPixelSpacing.getDoubleValues();
						slf4jlogger.debug("copyFunctionalGroupsSequenceWithDownsampledValues(): PixelSpacing[0] = {}",vPixelSpacing[0]);
						slf4jlogger.debug("copyFunctionalGroupsSequenceWithDownsampledValues(): PixelSpacing[1] = {}",vPixelSpacing[1]);
						vPixelSpacing[0] = vPixelSpacing[0] * 2;
						vPixelSpacing[1] = vPixelSpacing[1] * 2;
						slf4jlogger.debug("copyFunctionalGroupsSequenceWithDownsampledValues(): new PixelSpacing[0] = {}",vPixelSpacing[0]);
						slf4jlogger.debug("copyFunctionalGroupsSequenceWithDownsampledValues(): new PixelSpacing[1] = {}",vPixelSpacing[1]);
						{ Attribute a = new DecimalStringAttribute(TagFromName.PixelSpacing); a.addValue(vPixelSpacing[0]); a.addValue(vPixelSpacing[1]); pixelMeasuresSequenceItemList.put(a); }
					}
				}
			}
			{
				SequenceAttribute wholeSlideMicroscopyImageFrameTypeSequence = (SequenceAttribute)sharedFunctionalGroupsSequenceItemList.get(TagFromName.WholeSlideMicroscopyImageFrameTypeSequence);
				if (wholeSlideMicroscopyImageFrameTypeSequence == null || wholeSlideMicroscopyImageFrameTypeSequence.getNumberOfItems() != 1) {
					slf4jlogger.warn("Missing or bad WholeSlideMicroscopyImageFrameTypeSequence in SharedFunctionalGroupsSequence - adding it");
					wholeSlideMicroscopyImageFrameTypeSequence = new SequenceAttribute(TagFromName.WholeSlideMicroscopyImageFrameTypeSequence);
					sharedFunctionalGroupsSequenceItemList.put(wholeSlideMicroscopyImageFrameTypeSequence);
					wholeSlideMicroscopyImageFrameTypeSequence.addItem(new AttributeList());
				}
				{
					AttributeList wholeSlideMicroscopyImageFrameTypeSequenceItemList = SequenceAttribute.getAttributeListFromSelectedItemWithinSequence(wholeSlideMicroscopyImageFrameTypeSequence,0);
					updateImageOrFrameType(wholeSlideMicroscopyImageFrameTypeSequenceItemList,wholeSlideMicroscopyImageFrameTypeSequenceItemList,TagFromName.FrameType);	// OK for old and new lists to be the same
				}
			}
			{
				// Add source image reference in DerivationImageSequence (001205)
				// replace any existing, since only want to reference immediate predecessor
				SequenceAttribute derivationImageSequence = new SequenceAttribute(TagFromName.DerivationImageSequence);
				sharedFunctionalGroupsSequenceItemList.put(derivationImageSequence);
				AttributeList derivationImageSequenceItemList = new AttributeList();
				derivationImageSequence.addItem(derivationImageSequenceItemList);
				{ Attribute a = new ShortTextAttribute(TagFromName.DerivationDescription); a.addValue("Downsampled by 2 in row and column dimensions by arithmetic mean of adjacent pixels"); derivationImageSequenceItemList.put(a); }
				
				CodedSequenceItem.putSingleCodedSequenceAttribute(derivationImageSequenceItemList,TagFromName.DerivationCodeSequence,new CodedSequenceItem("113085","DCM","Spatial resampling"));

				{
					SequenceAttribute sourceImageSequence = new SequenceAttribute(TagFromName.SourceImageSequence);
					derivationImageSequenceItemList.put(sourceImageSequence);
					String sourceSOPInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SOPInstanceUID);
					String sourceSOPClassUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SOPClassUID);
					if (sourceSOPInstanceUID.length() > 0 && sourceSOPClassUID.length() > 0) {
						AttributeList sourceImageSequenceItemList = new AttributeList();
						sourceImageSequence.addItem(sourceImageSequenceItemList);
						{ Attribute a = new UniqueIdentifierAttribute(TagFromName.ReferencedSOPInstanceUID); a.addValue(sourceSOPInstanceUID); sourceImageSequenceItemList.put(a); }
						{ Attribute a = new UniqueIdentifierAttribute(TagFromName.ReferencedSOPClassUID);    a.addValue(sourceSOPClassUID);    sourceImageSequenceItemList.put(a); }
						CodedSequenceItem.putSingleCodedSequenceAttribute(sourceImageSequenceItemList,TagFromName.PurposeOfReferenceCodeSequence,new CodedSequenceItem("121322","DCM","Source image for image processing operation"));
					}
					else {
						slf4jlogger.warn("Missing SOPInstanceUID or SOPClassUID for constructing DerivationImageSequence in SharedFunctionalGroupsSequence");
					}
				}
			}
			//slf4jlogger.debug("createDownsampledDICOMFile(): \n{}",sharedFunctionalGroupsSequence.toString());
		}

		SequenceAttribute oldPerFrameFunctionalGroupsSequence = (SequenceAttribute)list.get(TagFromName.PerFrameFunctionalGroupsSequence);
		if (oldPerFrameFunctionalGroupsSequence == null) {	// (001107)
			String dimensionOrganizationType = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.DimensionOrganizationType);
			if (dimensionOrganizationType == null || !dimensionOrganizationType.equals("TILED_FULL")) {
				throw new DicomException("Missing PerFrameFunctionalGroupsSequence or incorrect number of items");
			}
			// else OK to be have no PerFrameFunctionalGroupsSequence
		}
		else if (oldPerFrameFunctionalGroupsSequence.getNumberOfItems() != oldNumberOfFrames) {
			throw new DicomException("Incorrect number of items in PerFrameFunctionalGroupsSequence");
		}
		else {
			SequenceAttribute newPerFrameFunctionalGroupsSequence = new SequenceAttribute(TagFromName.PerFrameFunctionalGroupsSequence);
			newList.put(newPerFrameFunctionalGroupsSequence);
			// the new PixelData will be encoded in raster scan order of the new rows and columns of tiles regardless of the original order
			// so iterate through the new tiles, pulling the top left of the four averaged source per-frame functional group information from the old
			for (int newRow=0; newRow<newNumberOfRowsOfTiles; ++newRow) {
				int oldRow = newRow * 2;
				for (int newColumn=0; newColumn<newNumberOfColumnsOfTiles; ++newColumn) {
					int oldColumn = newColumn * 2;
					int oldFrame = index.getFrameNumber(oldRow,oldColumn);	// returns frame numbered from 1
					//slf4jlogger.debug("createDownsampledDICOMFile(): making new PerFrameFunctionalGroupsSequence item  from old row {} column {} frame {} item {}",oldRow,oldColumn,oldFrame,oldFrame-1);
					AttributeList oldPerFrameList = SequenceAttribute.getAttributeListFromSelectedItemWithinSequence(oldPerFrameFunctionalGroupsSequence,oldFrame-1);	// items are number from 0
					newPerFrameFunctionalGroupsSequence.addItem(oldPerFrameList);
					SequenceAttribute planePositionSlideSequence = (SequenceAttribute)oldPerFrameList.get(TagFromName.PlanePositionSlideSequence);
					if (planePositionSlideSequence == null || planePositionSlideSequence.getNumberOfItems() != 1) {
						throw new DicomException("Missing PlanePositionSlideSequence in PerFrameFunctionalGroupsSequence item "+oldFrame+" or incorrect number of items");
					}
					else {
						AttributeList planePositionSlideSequenceItemList = SequenceAttribute.getAttributeListFromSelectedItemWithinSequence(planePositionSlideSequence,0);
						{ Attribute a = new SignedLongAttribute(TagFromName.RowPositionInTotalImagePixelMatrix);    a.addValue(newRow*rows+1);    planePositionSlideSequenceItemList.put(a); }
						{ Attribute a = new SignedLongAttribute(TagFromName.ColumnPositionInTotalImagePixelMatrix); a.addValue(newColumn*columns+1); planePositionSlideSequenceItemList.put(a); }
						// ?? need to update X and Y :(
					}
				}
			}
		}
	}
	
	public static void setClipRectForIncompletelyFilledTilesIfNecessary(SourceImage sImg,TiledFramesIndex index,AttributeList list) throws DicomException {
		if (index == null) {
			slf4jlogger.info("setClipRectForIncompletelyFilledTilesIfNecessary(): constructing TiledFramesIndex since none supplied");
			index = new TiledFramesIndex(list,false/*physical*/,false/*buildInverseIndex*/,false/*ignorePlanePosition*/);
		}
		setClipRectForIncompletelyFilledTilesIfNecessary(sImg,index,
			index.getNumberOfColumnsOfTiles(),
			index.getNumberOfRowsOfTiles(),
			Attribute.getSingleIntegerValueOrDefault(list,TagFromName.TotalPixelMatrixColumns,0),
			Attribute.getSingleIntegerValueOrDefault(list,TagFromName.TotalPixelMatrixRows,0),
			Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Columns,0),
			Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Rows,0)
		);
	}
	
	public static void setClipRectForIncompletelyFilledTilesIfNecessary(SourceImage sImg,TiledFramesIndex index,
			int numberOfColumnsOfTiles,int numberOfRowsOfTiles,
			int totalPixelMatrixColumns,int totalPixelMatrixRows,
			int columns,int rows) {

		slf4jlogger.debug("setClipRectForIncompletelyFilledTilesIfNecessary(): numberOfRowsOfTiles = {}",numberOfRowsOfTiles);
		slf4jlogger.debug("setClipRectForIncompletelyFilledTilesIfNecessary(): numberOfColumnsOfTiles = {}",numberOfColumnsOfTiles);

		// only need to consider right-most column and bottom-most row, with BRHC as special case since may have both row and column clips ...
		
		int remainderRight = totalPixelMatrixColumns%columns;
		if (remainderRight != 0) {
			slf4jlogger.debug("setClipRectForIncompletelyFilledTilesIfNecessary(): clipping right-most column at remainderRight = {}",remainderRight);
			Rectangle rect = new Rectangle(0,0,remainderRight,rows);
			// clip right-most column
			for (int row=0; row<numberOfRowsOfTiles; ++row) {
				int frame = index.getFrameNumber(row,numberOfColumnsOfTiles-1) - 1;
				slf4jlogger.debug("setClipRectForIncompletelyFilledTilesIfNecessary(): clipping right-most column frame = {}",frame);
				sImg.setClipRect(frame,rect);
			}
		}
		
		int remainderBottom = totalPixelMatrixRows%rows;
		if (remainderBottom != 0) {
			slf4jlogger.debug("setClipRectForIncompletelyFilledTilesIfNecessary(): clipping bottom-most row at remainderBottom = {}",remainderBottom);
			Rectangle rect = new Rectangle(0,0,columns,remainderBottom);
			// clip bottom-most row
			for (int col=0; col<numberOfColumnsOfTiles; ++col) {
				int frame = index.getFrameNumber(numberOfRowsOfTiles-1,col) - 1;
				slf4jlogger.debug("setClipRectForIncompletelyFilledTilesIfNecessary(): clipping bottom-most row frame = {}",frame);
				sImg.setClipRect(frame,rect);
			}
		}
		
		// overwrite BRHC rect that has already been set
		if (remainderRight != 0 && remainderBottom != 0) {
			slf4jlogger.debug("setClipRectForIncompletelyFilledTilesIfNecessary(): clipping BRHC tile at remainderRight = {} and remainderBottom = {}",remainderRight,remainderBottom);
			Rectangle rect = new Rectangle(0,0,remainderRight,remainderBottom);
			if (numberOfRowsOfTiles > 0 && numberOfColumnsOfTiles > 0) {
				int frame = index.getFrameNumber(numberOfRowsOfTiles-1,numberOfColumnsOfTiles-1) - 1;
				slf4jlogger.debug("setClipRectForIncompletelyFilledTilesIfNecessary(): clipping BRHC tile frame = {}",frame);
				sImg.setClipRect(frame,rect);
			}
		}
	}

	protected static void downsamplePixelData(AttributeList list,AttributeList newList,
			TiledFramesIndex index,int oldNumberOfFrames,int newNumberOfFrames,
			int oldTotalPixelMatrixColumns,int oldTotalPixelMatrixRows,
			int oldNumberOfColumnsOfTiles,int oldNumberOfRowsOfTiles,
			int newNumberOfColumnsOfTiles,int newNumberOfRowsOfTiles,
			int columns,int rows,String outputFormat
		) throws DicomException, IOException {
		
		int samplesPerPixel = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.SamplesPerPixel,1);	// default to 1 if missing
		int bitsAllocated = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsAllocated,8);		// default to 8 if missing
		int mask = 0;
		int maxval = 0;
		if (bitsAllocated == 8) {
			mask = 0xff;
			maxval = 0xff;
		}
		else if (bitsAllocated == 16) {
			mask = 0xffff;
			maxval = 0xffff;
		}
		else {
			throw new DicomException("Unsupported bit depth - BitsAllocated "+bitsAllocated);
		}

		File[] frameFiles = new File[newNumberOfFrames];	// don't forget to delete these later :(

		SourceImage sImg = new SourceImage(list,false/*applyICCProfileIfPresent*/);	// ICC is re=specified in down-sampled images, so don't apply it to pixel data to downsample (001288)
		setClipRectForIncompletelyFilledTilesIfNecessary(sImg,index,oldNumberOfColumnsOfTiles,oldNumberOfRowsOfTiles,oldTotalPixelMatrixColumns,oldTotalPixelMatrixRows,columns,rows);
		// reuse the raster arrays for each tile
		int[] downsampledcount =  new int[rows*columns*samplesPerPixel];
		int[] downsampledtotal =  new int[rows*columns*samplesPerPixel];
		byte[] downsampledbytedata =  bitsAllocated == 8 ? new byte[rows*columns*samplesPerPixel] : null;
		short[] downsampledshortdata =  bitsAllocated == 16 ? new short[rows*columns*samplesPerPixel] : null;
		ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);		// should really check DICOM ColorSpace attribute ... makes no difference to result though
		// the new PixelData will be encoded in raster scan order of the new rows and columns of tiles regardless of the original order ... must match the PerFrameFunctionalGroupsSequence item order
		int newFrameFromZero = 0;
		for (int newRow=0; newRow<newNumberOfRowsOfTiles; ++newRow) {
			int oldRowTLHCOfFour = newRow * 2;
			for (int newColumn=0; newColumn<newNumberOfColumnsOfTiles; ++newColumn) {
				int oldColumnTLHCOfFour = newColumn * 2;
				int oldFrameTLHC = oldRowTLHCOfFour <  oldNumberOfRowsOfTiles    && oldColumnTLHCOfFour <  oldNumberOfColumnsOfTiles    ? index.getFrameNumber(oldRowTLHCOfFour,  oldColumnTLHCOfFour  ) : 0;
				int oldFrameTRHC = oldRowTLHCOfFour <  oldNumberOfRowsOfTiles    && oldColumnTLHCOfFour < (oldNumberOfColumnsOfTiles-1) ? index.getFrameNumber(oldRowTLHCOfFour,  oldColumnTLHCOfFour+1) : 0;
				int oldFrameBLHC = oldRowTLHCOfFour < (oldNumberOfRowsOfTiles-1) && oldColumnTLHCOfFour <  oldNumberOfColumnsOfTiles    ? index.getFrameNumber(oldRowTLHCOfFour+1,oldColumnTLHCOfFour  ) : 0;
				int oldFrameBRHC = oldRowTLHCOfFour < (oldNumberOfRowsOfTiles-1) && oldColumnTLHCOfFour < (oldNumberOfColumnsOfTiles-1) ? index.getFrameNumber(oldRowTLHCOfFour+1,oldColumnTLHCOfFour+1) : 0;

				slf4jlogger.trace("downsamplePixelData(): Four old frames TLHC = {}, TRHC = {}, BLHC = {}, BRHC = {}",oldFrameTLHC,oldFrameTRHC,oldFrameBLHC,oldFrameBRHC);
				
				BufferedImage tlhcFrame = oldFrameTLHC != 0 ? sImg.getBufferedImage(oldFrameTLHC-1) : null;	// NB. getBufferedImage() takes argument numbered from 0 :(
				BufferedImage trhcFrame = oldFrameTRHC != 0 ? sImg.getBufferedImage(oldFrameTRHC-1) : null;
				BufferedImage blhcFrame = oldFrameBLHC != 0 ? sImg.getBufferedImage(oldFrameBLHC-1) : null;
				BufferedImage brhcFrame = oldFrameBRHC != 0 ? sImg.getBufferedImage(oldFrameBRHC-1) : null;

				// have encountered images (e.g., Aperio SVS TIFF) in which the last row is only partially encoded in the JPEG source, i.e., BufferedImage.getHeight() < rows (001067)
				int tlhcActualRows = (tlhcFrame != null) ? tlhcFrame.getHeight() : 0;
				int trhcActualRows = (trhcFrame != null) ? trhcFrame.getHeight() : 0;
				int blhcActualRows = (blhcFrame != null) ? blhcFrame.getHeight() : 0;
				int brhcActualRows = (brhcFrame != null) ? brhcFrame.getHeight() : 0;
				
				if (tlhcFrame != null && tlhcActualRows != rows) slf4jlogger.warn("Top row (TLHC) height {} is less than expected {}",tlhcActualRows,rows);
				if (trhcFrame != null && trhcActualRows != rows) slf4jlogger.warn("Top row (TRHC) height {} is less than expected {}",trhcActualRows,rows);
				if (blhcFrame != null && blhcActualRows != rows) slf4jlogger.warn("Bottom row (BLHC) height {} is less than expected {}",blhcActualRows,rows);
				if (brhcFrame != null && brhcActualRows != rows) slf4jlogger.warn("Bottom row (BRHC) height {} is less than expected {}",brhcActualRows,rows);
				
				int tlhcFramePixels[] = null; // to disambiguate SampleModel.getPixels() method signature
				int trhcFramePixels[] = null;
				int blhcFramePixels[] = null;
				int brhcFramePixels[] = null;
				
				tlhcFramePixels = tlhcFrame != null ? tlhcFrame.getSampleModel().getPixels(0,0,columns,tlhcActualRows,tlhcFramePixels,tlhcFrame.getRaster().getDataBuffer()) : null;
				trhcFramePixels = trhcFrame != null ? trhcFrame.getSampleModel().getPixels(0,0,columns,trhcActualRows,trhcFramePixels,trhcFrame.getRaster().getDataBuffer()) : null;
				blhcFramePixels = blhcFrame != null ? blhcFrame.getSampleModel().getPixels(0,0,columns,blhcActualRows,blhcFramePixels,blhcFrame.getRaster().getDataBuffer()) : null;
				brhcFramePixels = brhcFrame != null ? brhcFrame.getSampleModel().getPixels(0,0,columns,brhcActualRows,brhcFramePixels,brhcFrame.getRaster().getDataBuffer()) : null;
				
				// push rather than pull, by scanning source, adding to total and counting, then dividing total by count to produce byte pixel, for each of interlevaed RGB
				// zero our counts and totals since reusing arrays
				for (int i=0; i<downsampledcount.length; ++i) {
					downsampledcount[i]=0;
					downsampledtotal[i]=0;
				}
				
				if (tlhcFramePixels != null) {
					for (int r=0; r<tlhcActualRows; ++r) {
						int dstRow = r/2;
						for (int c=0; c<columns; ++c) {
							int dstColumn = c/2;
							for (int sample=0; sample<samplesPerPixel; ++sample) {
								int srcPixel = tlhcFramePixels[(r*columns+c)*samplesPerPixel+sample]&mask;	// do not sign extend
								int dstIndex = (dstRow*columns+dstColumn)*samplesPerPixel+sample;
								++downsampledcount[dstIndex];
								downsampledtotal[dstIndex]+=srcPixel;
							}
						}
					}
				}
				if (trhcFramePixels != null) {
					int offsetalongrow = columns;	// may be odd
					
					for (int r=0; r<trhcActualRows; ++r) {
						int dstRow = r/2;
						for (int c=0; c<columns; ++c) {
							int dstColumn = (offsetalongrow+c)/2;
							for (int sample=0; sample<samplesPerPixel; ++sample) {
								int srcPixel = trhcFramePixels[(r*columns+c)*samplesPerPixel+sample]&mask;	// do not sign extend
								int dstIndex = (dstRow*columns+dstColumn)*samplesPerPixel+sample;
								++downsampledcount[dstIndex];
								downsampledtotal[dstIndex]+=srcPixel;
							}
						}
					}
				}
				if (blhcFramePixels != null) {
					int offsetalongcolumn = rows;	// may be odd
					
					for (int r=0; r<blhcActualRows; ++r) {
						int dstRow = (offsetalongcolumn+r)/2;
						for (int c=0; c<columns; ++c) {
							int dstColumn = c/2;
							for (int sample=0; sample<samplesPerPixel; ++sample) {
								int srcPixel = blhcFramePixels[(r*columns+c)*samplesPerPixel+sample]&mask;	// do not sign extend
								int dstIndex = (dstRow*columns+dstColumn)*samplesPerPixel+sample;
								++downsampledcount[dstIndex];
								downsampledtotal[dstIndex]+=srcPixel;
							}
						}
					}
				}
				if (brhcFramePixels != null) {
					int offsetalongrow = columns;	// may be odd
					int offsetalongcolumn = rows;	// may be odd
					
					for (int r=0; r<brhcActualRows; ++r) {
						int dstRow = (offsetalongcolumn+r)/2;
						for (int c=0; c<columns; ++c) {
							int dstColumn = (offsetalongrow+c)/2;
							for (int sample=0; sample<samplesPerPixel; ++sample) {
								int srcPixel = brhcFramePixels[(r*columns+c)*samplesPerPixel+sample]&mask;	// do not sign extend
								int dstIndex = (dstRow*columns+dstColumn)*samplesPerPixel+sample;
								++downsampledcount[dstIndex];
								downsampledtotal[dstIndex]+=srcPixel;
							}
						}
					}
				}
				
				if (downsampledbytedata != null) {
					for (int i=0; i<downsampledcount.length; ++i) {
						downsampledbytedata[i] = (byte)(downsampledcount[i] == 0 ? maxval : downsampledtotal[i]/downsampledcount[i]);	// 0 count happens if no source of input, so use white (not black)
					}
				}
				else if (downsampledshortdata != null) {
					for (int i=0; i<downsampledcount.length; ++i) {
						downsampledshortdata[i] = (short)(downsampledcount[i] == 0 ? maxval : downsampledtotal[i]/downsampledcount[i]);	// 0 count happens if no source of input, so use white (not black)
					}
				}
				
				File tmpFrameFile = File.createTempFile("TiledPyramid_tmp",".tmp");
				tmpFrameFile.deleteOnExit();
				if (outputFormat == null) {
					// do not need to make rendered image ... just write uncompressed array (in correct byte order)
					if (downsampledbytedata != null) {
						OutputStream o = new BufferedOutputStream(new FileOutputStream(tmpFrameFile));
						o.write(downsampledbytedata);
						o.close();
					}
					else if (downsampledshortdata != null) {
						BinaryOutputStream o = new BinaryOutputStream(new BufferedOutputStream(new FileOutputStream(tmpFrameFile)),false/*big*/);
						o.writeUnsigned16(downsampledshortdata,downsampledshortdata.length);
						o.close();
					}
					else {
						throw new DicomException("Unsupported combination of BitsAllocated "+bitsAllocated+" and SamplesPerPixel "+samplesPerPixel);
					}
				}
				else {
					BufferedImage renderedImage = null;
					if (samplesPerPixel == 3) {
						if (downsampledbytedata != null) {
							renderedImage = SourceImage.createPixelInterleavedByteThreeComponentColorImage(columns,rows,downsampledbytedata,0/*offset*/,colorSpace,false/*isChrominanceHorizontallyDownsampledBy2*/);
						}
						else if (downsampledshortdata != null) {
							renderedImage = SourceImage.createPixelInterleavedShortThreeComponentColorImage(columns,rows,downsampledshortdata,0/*offset*/,colorSpace);
						}
					}
					else if (samplesPerPixel == 1) {
						if (downsampledbytedata != null) {
							renderedImage = SourceImage.createByteGrayscaleImage(columns,rows,downsampledbytedata,0/*offset*/);
						}
						else if (downsampledshortdata != null) {
							renderedImage = SourceImage.createUnsignedShortGrayscaleImage(columns,rows,downsampledshortdata,0/*offset*/);
						}
					}
					if (renderedImage == null) {
						throw new DicomException("Unsupported combination of BitsAllocated "+bitsAllocated+" and SamplesPerPixel "+samplesPerPixel);
					}
					CompressedFrameEncoder.getCompressedFrameAsFile(list,renderedImage,outputFormat,tmpFrameFile);
				}
				frameFiles[newFrameFromZero++] = tmpFrameFile;
			}
		}
		Attribute aPixelData = outputFormat == null
			? (bitsAllocated == 8
				? new OtherByteAttributeMultipleFilesOnDisk(TagFromName.PixelData,frameFiles)
				: new OtherWordAttributeMultipleFilesOnDisk(TagFromName.PixelData,frameFiles,false/*bigEndian*/)
			  )
			: new OtherByteAttributeMultipleCompressedFrames(TagFromName.PixelData,frameFiles);
		newList.put(aPixelData);
		
		if (samplesPerPixel > 1) {	// overrides whatever may have been copied from the source (001351)
			Attribute a = new UnsignedShortAttribute(TagFromName.PlanarConfiguration);
			a.addValue(0); // always color-by-pixel, whether compressed or not
			newList.put(a);
		}
	}
	
	public static void createDownsampledDICOMAttributes(AttributeList list,AttributeList newList,TiledFramesIndex index,String outputformat,
				boolean populateunchangedimagepixeldescriptionmacroattributes,boolean populatefunctionalgroups) throws DicomException, IOException {
			int    rows = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Rows,0);
			int columns = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Columns,0);
			slf4jlogger.debug("createDownsampledDICOMAttributes(): rows = {}",rows);
			slf4jlogger.debug("createDownsampledDICOMAttributes(): columns = {}",columns);

			int totalPixelMatrixColumns = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.TotalPixelMatrixColumns,0);
			int    totalPixelMatrixRows = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.TotalPixelMatrixRows,0);
			slf4jlogger.debug("createDownsampledDICOMAttributes(): totalPixelMatrixColumns = {}",totalPixelMatrixColumns);
			slf4jlogger.debug("createDownsampledDICOMAttributes(): totalPixelMatrixRows = {}",totalPixelMatrixRows);
		
			int newTotalPixelMatrixColumns = (totalPixelMatrixColumns+2)/2-1;	// e.g, 512->256, 513->257, 514->257
			//int newTotalPixelMatrixColumns = (int)Math.round(totalPixelMatrixColumns/2.0);
			int newTotalPixelMatrixRows = (totalPixelMatrixRows+2)/2-1;
			//int newTotalPixelMatrixRows = (int)Math.round(totalPixelMatrixRows/2.0);
			slf4jlogger.debug("createDownsampledDICOMAttributes(): new TotalPixelMatrixColumns = {}",newTotalPixelMatrixColumns);
			slf4jlogger.debug("createDownsampledDICOMAttributes(): new TotalPixelMatrixRows = {}",newTotalPixelMatrixRows);
		
			//if (newTotalPixelMatrixColumns%2 == 1) {	// make TotalPixelMatrixRows even
			//	++newTotalPixelMatrixColumns;
			//	slf4jlogger.info("createDownsampledDICOMAttributes(): incrementing TotalPixelMatrixColumns to make it even = {}",newTotalPixelMatrixColumns);
			//}
			//if (newTotalPixelMatrixRows%2 == 1) {	// make TotalPixelMatrixRows even
			//	++newTotalPixelMatrixRows;
			//	slf4jlogger.info("createDownsampledDICOMAttributes(): incrementing TotalPixelMatrixRows to make it even = {}",newTotalPixelMatrixRows);
			//}

			if (totalPixelMatrixColumns == newTotalPixelMatrixColumns || totalPixelMatrixRows == newTotalPixelMatrixRows) {
				slf4jlogger.debug("createDownsampledDICOMAttributes(): stopping because unchanged");
			}
			else if (newTotalPixelMatrixColumns <= 0 || newTotalPixelMatrixRows <= 0) {
				slf4jlogger.error("createDownsampledDICOMAttributes(): stopping because matrix zero or less ... should not happen");
			}
		
			int numberOfColumnsOfTiles = index.getNumberOfColumnsOfTiles();
			int numberOfRowsOfTiles = index.getNumberOfRowsOfTiles();
			slf4jlogger.debug("createDownsampledDICOMAttributes(): numberOfColumnsOfTiles = {}",numberOfColumnsOfTiles);
			slf4jlogger.debug("createDownsampledDICOMAttributes(): numberOfRowsOfTiles = {}",numberOfRowsOfTiles);
		
			// the following computation is copied from TiledFramesIndex():

			int newNumberOfColumnsOfTiles = newTotalPixelMatrixColumns/columns;
			if (newTotalPixelMatrixColumns % columns != 0) {
				slf4jlogger.debug("createDownsampledDICOMAttributes(): New TotalPixelMatrixColumns {} is not an exact multiple of Columns {}",newTotalPixelMatrixColumns,columns);
				++newNumberOfColumnsOfTiles;
			}
			slf4jlogger.debug("createDownsampledDICOMAttributes(): New numberOfColumnsOfTiles = {}",newNumberOfColumnsOfTiles);
		
			int newNumberOfRowsOfTiles = newTotalPixelMatrixRows/rows;
			if (newTotalPixelMatrixRows % rows != 0) {
				slf4jlogger.debug("createDownsampledDICOMAttributes(): New TotalPixelMatrixRows {} is not an exact multiple of Rows {}",newTotalPixelMatrixRows,rows);
				++newNumberOfRowsOfTiles;
			}
			slf4jlogger.debug("createDownsampledDICOMAttributes(): New newNumberOfRowsOfTiles = {}",newNumberOfRowsOfTiles);

			int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfFrames,1);	// default to 1 if missing
			slf4jlogger.debug("createDownsampledDICOMAttributes(): NumberOfFrames = {}",numberOfFrames);
		
			int newNumberOfFrames = newNumberOfColumnsOfTiles * newNumberOfRowsOfTiles;
			slf4jlogger.debug("createDownsampledDICOMAttributes(): new NumberOfFrames = {}",newNumberOfFrames);
			if (newNumberOfFrames == 0) {
				slf4jlogger.error("createDownsampledDICOMAttributes(): stopping because new NumberOfFrames zero or less ... should not happen");
			}
			{ Attribute a = new IntegerStringAttribute(TagFromName.NumberOfFrames); a.addValue(newNumberOfFrames); newList.put(a); }

			{ Attribute a = new UnsignedLongAttribute(TagFromName.TotalPixelMatrixColumns); a.addValue(newTotalPixelMatrixColumns); newList.put(a); }
			{ Attribute a = new UnsignedLongAttribute(TagFromName.TotalPixelMatrixRows); a.addValue(newTotalPixelMatrixRows); newList.put(a); }

			if (populateunchangedimagepixeldescriptionmacroattributes) {
				newList.put(list.get(TagFromName.Rows));
				newList.put(list.get(TagFromName.Columns));
				newList.put(list.get(TagFromName.BitsStored));
				newList.put(list.get(TagFromName.BitsAllocated));
				newList.put(list.get(TagFromName.HighBit));
				newList.put(list.get(TagFromName.SamplesPerPixel));
				{
					// will be absent if MONOCHROME, e.g., for fluorescence
					Attribute aPlanarConfiguration = list.get(TagFromName.PlanarConfiguration);
					if (aPlanarConfiguration != null) {
						newList.put(aPlanarConfiguration);	// will be overidden by downsamplePixelData() anyway, since regardless of source, downsampled will be color-by-pixel, compressed or not 001351)
					}
				}
			}
		
			{
				// override whatever source may have been
				slf4jlogger.debug("createDownsampledDICOMAttributes(): outputformat = {}",outputformat);
				String photometricInterpretation = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PhotometricInterpretation);
				slf4jlogger.debug("createDownsampledDICOMAttributes(): source list PhotometricInterpretation = {}",photometricInterpretation);
				if (outputformat != null) {
					if (outputformat.equals("jpeg")) {
						photometricInterpretation = "YBR_FULL_422";		// even if input really was RGB, e.g., Aperio, we are recompressing using default lossy
					}
					else if (outputformat.equals("jpeg2000")) {
						photometricInterpretation = "YBR_RCT";			// even if input really was RGB, e.g., Aperio, we are recompressing using reversible (that's what CompressedFrameEncoder.getCompressedFrameAsFile() does)
					}
				}
				if (photometricInterpretation.length() == 0) {
					photometricInterpretation = "RGB";
				}
				// else use value from existing list already extracted
				slf4jlogger.debug("createDownsampledDICOMAttributes(): new list PhotometricInterpretation = {}",photometricInterpretation);
				{ Attribute a = new CodeStringAttribute(TagFromName.PhotometricInterpretation); a.addValue(photometricInterpretation); newList.put(a); }
			}
		
			updateImageOrFrameType(list,newList,TagFromName.ImageType);
		
			if (populatefunctionalgroups) {
				copyFunctionalGroupsSequenceWithDownsampledValues(list,newList,index,numberOfFrames,newNumberOfFrames,newNumberOfColumnsOfTiles,newNumberOfRowsOfTiles,columns,rows);
			}
		
			// Do not add derivation information as General Reference Module at top level (Type U in IOD), since already added to shared functional group (where it is C, "Required if the image or frame has been derived from another SOP Instance")
			// but presence of SOP Instance references in DerivationImageSequence functional group macros triggers the need for ReferencedSeriesSequence in the Common Instance Reference Module (001205)
		
			{
				SequenceAttribute referencedSeriesSequence = new SequenceAttribute(TagFromName.ReferencedSeriesSequence);
				newList.put(referencedSeriesSequence);
				String seriesInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesInstanceUID);
				if (seriesInstanceUID.length() > 0) {
					AttributeList referencedSeriesSequenceItemList = new AttributeList();
					referencedSeriesSequence.addItem(referencedSeriesSequenceItemList);
					{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); referencedSeriesSequenceItemList.put(a); }

					SequenceAttribute referencedInstanceSequence = new SequenceAttribute(TagFromName.ReferencedInstanceSequence);
					referencedSeriesSequenceItemList.put(referencedInstanceSequence);
					String sourceSOPInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SOPInstanceUID);
					String sourceSOPClassUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SOPClassUID);
					if (sourceSOPInstanceUID.length() > 0 && sourceSOPClassUID.length() > 0) {
						AttributeList referencedInstanceSequenceItemList = new AttributeList();
						referencedInstanceSequence.addItem(referencedInstanceSequenceItemList);
						{ Attribute a = new UniqueIdentifierAttribute(TagFromName.ReferencedSOPInstanceUID); a.addValue(sourceSOPInstanceUID); referencedInstanceSequenceItemList.put(a); }
						{ Attribute a = new UniqueIdentifierAttribute(TagFromName.ReferencedSOPClassUID);    a.addValue(sourceSOPClassUID);    referencedInstanceSequenceItemList.put(a); }

					}
					else {
						slf4jlogger.warn("Missing SOPInstanceUID or SOPClassUID for constructing ReferencedInstanceSequence in Common Instance Reference Module");
					}
				}
				else {
					slf4jlogger.warn("Missing SeriesInstanceUID for constructing ReferencedSeriesSequence in Common Instance Reference Module");
				}
			}
		
			downsamplePixelData(list,newList,index,numberOfFrames,newNumberOfFrames,totalPixelMatrixColumns,totalPixelMatrixRows,numberOfColumnsOfTiles,numberOfRowsOfTiles,newNumberOfColumnsOfTiles,newNumberOfRowsOfTiles,columns,rows,outputformat);
	}
	
	public File createDownsampledDICOMFile(File inputFile,File outputFolder) throws DicomException, IOException {
		slf4jlogger.info("createDownsampledDICOMFile(): inputFile = {}",inputFile);
		File newFile = null;
		AttributeList list = new AttributeList();
		list.setDecompressPixelData(false);
		list.read(inputFile);

		String transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.TransferSyntaxUID);
		if (transferSyntaxUID.length() == 0) {
			throw new DicomException("Missing TransferSyntaxUID");
		}
		slf4jlogger.debug("transferSyntaxUID = {}",transferSyntaxUID);
		String outputformat = CompressedFrameEncoder.chooseOutputFormatForTransferSyntax(transferSyntaxUID);
		slf4jlogger.debug("outputformat = {}",outputformat);
		
		list.removePrivateAttributes();	// since may no longer be consistent with PixelData content

		list.removeGroupLengthAttributes();
		list.remove(TagFromName.DataSetTrailingPadding);
		// do NOT removeMetaInformationHeaderAttributes() yet because need TransferSyntaxUID left in the old list for deferred decmpression
						
		TiledFramesIndex index = new TiledFramesIndex(list,false/*physical*/,false/*buildInverseIndex*/,false/*ignorePlanePosition*/);
		int numberOfColumnsOfTiles = index.getNumberOfColumnsOfTiles();
		slf4jlogger.debug("createDownsampledDICOMFile(): numberOfColumnsOfTiles = {}",numberOfColumnsOfTiles);
		int numberOfRowsOfTiles = index.getNumberOfRowsOfTiles();
		slf4jlogger.debug("createDownsampledDICOMFile(): numberOfRowsOfTiles = {}",numberOfRowsOfTiles);
		if (numberOfColumnsOfTiles > 1 || numberOfRowsOfTiles > 1) {
			slf4jlogger.debug("createDownsampledDICOMFile(): creating downsampled file");
			AttributeList newList = new AttributeList();
			newList.putAll(list);
			newList.remove(TagFromName.PixelData);
			
			// did putAll() so do not need to repeat populateunchangedimagepixeldescriptionmacroattributes
			createDownsampledDICOMAttributes(list,newList,index,outputformat,false/*populateunchangedimagepixeldescriptionmacroattributes*/,true/*populatefunctionalgroups*/);
			
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(generator.getAnotherNewUID()); newList.put(a); }

			ClinicalTrialsAttributes.addContributingEquipmentSequence(newList,false/*retainExistingItems*/,
				new CodedSequenceItem("109106","DCM","Enhanced Multi-frame Conversion Equipment"),
				"PixelMed",														// Manufacturer
				"PixelMed",														// Institution Name
				"Software Development",											// Institutional Department Name
				"Bangor, PA",													// Institution Address
				null,															// Station Name
				"TiledPyramid",													// Manufacturer's Model Name
				null,															// Device Serial Number
				"Vers. "+VersionAndConstants.getBuildDate(),					// Software Version(s)
				"Tiled pyramid created from single high resolution image");
			
			newList.insertSuitableSpecificCharacterSetForAllStringValues();
			
			newList.removeMetaInformationHeaderAttributes();
			FileMetaInformation.addFileMetaInformation(newList,transferSyntaxUID,"OURAETITLE");

			newFile = new File(outputFolder,Attribute.getSingleStringValueOrDefault(newList,TagFromName.SOPInstanceUID,"NONAME"));
			newList.write(newFile,transferSyntaxUID,true,true);

			{
				Attribute aPixelData = newList.get(TagFromName.PixelData);
				if (aPixelData != null) {
					File[] frameFiles = null;
					if (aPixelData instanceof OtherByteAttributeMultipleCompressedFrames) {
						frameFiles = ((OtherByteAttributeMultipleCompressedFrames)aPixelData).getFiles();
					}
					else if (aPixelData instanceof OtherByteAttributeMultipleFilesOnDisk) {
						frameFiles = ((OtherByteAttributeMultipleFilesOnDisk)aPixelData).getFiles();
					}
					else if (aPixelData instanceof OtherWordAttributeMultipleFilesOnDisk) {
						frameFiles = ((OtherWordAttributeMultipleFilesOnDisk)aPixelData).getFiles();
					}
					if (frameFiles != null) {
						for (int f=0; f<frameFiles.length; ++f) {
							slf4jlogger.debug("deleting = {}",outputformat);
							frameFiles[f].delete();
							frameFiles[f] = null;
						}
					}
				}
			}
		}
		else {
			slf4jlogger.debug("createDownsampledDICOMFile(): not creating downsampled file ... stopping");
		}
		return newFile;
	}

	public TiledPyramid(String inputfilename,String outputPath) throws DicomException, IOException {
		File outputFolder = new File(outputPath);
		if (!outputFolder.isDirectory()) {
			throw new DicomException("Output folder "+outputFolder+" does not exist");
		}
		File lastfile = new File(inputfilename);
		do {
			lastfile = createDownsampledDICOMFile(lastfile,outputFolder);
		} while (lastfile != null);
	}

	/**
	 * <p>Take a single high resolution tiled image and downsample it by successive factors of two to produce multi-resolution pyramid set of images.</p>
	 *
	 * @param	arg	array of two strings - the source image and the target directory (which must already exist)
	 */
	public static void main(String arg[]) {
		try {
			new TiledPyramid(arg[0],arg[1]);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);	// no need to use SLF4J since command line utility/test
			System.exit(0);
		}
	}

}
