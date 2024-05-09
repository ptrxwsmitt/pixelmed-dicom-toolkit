/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.convert.IndexedLabelMapToSegmentation;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.OtherByteAttribute;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.dicom.UnsignedShortAttribute;

import com.pixelmed.geometry.GeometryOfSlice;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

//import java.awt.*;
//import java.awt.color.*;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import java.util.Vector;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class of static methods to read DICOM segmentation images, and shrink their extent to the minimum bounding box surrounding the segments.</p>
 *
 * @author	dclunie
 */
public class ShrinkSegmentationToBoundingBox {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/ShrinkSegmentationToBoundingBox.java,v 1.3 2022/01/21 19:51:12 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(ShrinkSegmentationToBoundingBox.class);
	
	private static final DicomDictionary dictionary = DicomDictionary.StandardDictionary;
	
	// follows pattern of GeometryOfVolumeFromAttributeList
	protected void addTLHCOfClippedRegionToImagePosition(AttributeList list,int[] box) throws DicomException {
		SequenceAttribute sharedFunctionalGroupsSequence = (SequenceAttribute)(list.get(TagFromName.SharedFunctionalGroupsSequence));
		SequenceAttribute perFrameFunctionalGroupsSequence = (SequenceAttribute)(list.get(TagFromName.PerFrameFunctionalGroupsSequence));
		int     oldColumns = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Columns,0);
		int        oldRows = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Rows,0);
		int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfFrames,1);
		if (sharedFunctionalGroupsSequence != null && perFrameFunctionalGroupsSequence != null) {
			SequenceAttribute sharedPlaneOrientationSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(sharedFunctionalGroupsSequence,TagFromName.PlaneOrientationSequence));
			SequenceAttribute sharedPlanePositionSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(sharedFunctionalGroupsSequence,TagFromName.PlanePositionSequence));
			SequenceAttribute sharedPixelMeasuresSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(sharedFunctionalGroupsSequence,TagFromName.PixelMeasuresSequence));
			if (sharedPlaneOrientationSequence != null && sharedPlanePositionSequence == null && sharedPixelMeasuresSequence != null) {
				Attribute aPixelSpacing = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(sharedPixelMeasuresSequence,TagFromName.PixelSpacing);
				if (aPixelSpacing != null) {
					double [] pixelSpacingArray = aPixelSpacing.getDoubleValues();	// adjacent row spacing, then adjacent column spacing
					if (pixelSpacingArray != null && pixelSpacingArray.length == 2) {
						Attribute aSpacingBetweenSlices = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(sharedPixelMeasuresSequence,TagFromName.SpacingBetweenSlices);
						if (aSpacingBetweenSlices == null) {
							aSpacingBetweenSlices = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(sharedPixelMeasuresSequence,TagFromName.SliceThickness);
							if (aSpacingBetweenSlices != null) {
								slf4jlogger.warn("Missing SpacingBetweenSlices in shared PixelMeasuresSequence but using SliceThickness as substitute");
							}
						}
						if (aSpacingBetweenSlices != null) {
							double spacingBetweenSlices=aSpacingBetweenSlices.getSingleDoubleValueOrDefault(0d);
							int newMinX = box[0];
							slf4jlogger.debug("addTLHCOfClippedRegionToImagePosition(): newMinX = {}",newMinX);
							int newMinY = box[1];
							slf4jlogger.debug("addTLHCOfClippedRegionToImagePosition(): newMinY = {}",newMinY);
							Attribute aImageOrientationPatient = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(sharedPlaneOrientationSequence,TagFromName.ImageOrientationPatient);
							if (aImageOrientationPatient != null) {
								double[] orientation = aImageOrientationPatient.getDoubleValues();
								if (orientation != null && orientation.length == 6) {	// along row X, Y, Z, along column X, Y, Z
									double[] rowArray = new double[3];
									double[] columnArray = new double[3];
									rowArray[0] = orientation[0];
									rowArray[1] = orientation[1];
									rowArray[2] = orientation[2];
									columnArray[0] = orientation[3];
									columnArray[1] = orientation[4];
									columnArray[2] = orientation[5];
									
									double[] voxelSpacingArray = new double[3];
									voxelSpacingArray[0] = pixelSpacingArray[0];
									voxelSpacingArray[1] = pixelSpacingArray[1];
									voxelSpacingArray[2] = spacingBetweenSlices;
									
									double[] dimensions = new double[3];
									dimensions[0] = oldColumns;
									dimensions[1] = oldRows;
									dimensions[2] = 1;

									for (int f=0; f<numberOfFrames; ++f) {
										SequenceAttribute perFramePlanePositionSequence = (SequenceAttribute)(perFrameFunctionalGroupsSequence.getItem(f).getAttributeList().get(TagFromName.PlanePositionSequence));
										if (perFramePlanePositionSequence != null) {
											Attribute aImagePositionPatient = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(perFramePlanePositionSequence,TagFromName.ImagePositionPatient);
											if (aImagePositionPatient != null) {
												double[] tlhc = aImagePositionPatient.getDoubleValues();
												slf4jlogger.debug("addTLHCOfClippedRegionToImagePosition(): frame {}: old tlhc = {},{},{}",f,tlhc[0],tlhc[1],tlhc[2]);
												if (tlhc != null && tlhc.length == 3) {
													GeometryOfSlice geometry = new GeometryOfSlice(rowArray,columnArray,tlhc,voxelSpacingArray,0/*sliceThickness*/,dimensions);
													// compensate for lookupImageCoordinate() subtracting 0.5 from row and column for sub-pixel resolution
													double[] newTLHC = geometry.lookupImageCoordinate(newMinX+0.5,newMinY+0.5);
													slf4jlogger.debug("addTLHCOfClippedRegionToImagePosition(): frame {}: new tlhc = {},{},{}",f,newTLHC[0],newTLHC[1],newTLHC[2]);
													aImagePositionPatient.removeValues();
													aImagePositionPatient.addValue(newTLHC[0]);
													aImagePositionPatient.addValue(newTLHC[1]);
													aImagePositionPatient.addValue(newTLHC[2]);
												}
												else {
													throw new DicomException("Missing or incorrect number of values for ImagePositionPatient in per-frame PlanePositionSequence");
												}
											}
											else {
												throw new DicomException("Missing ImagePositionPatient in per-frame PlanePositionSequence");
											}
										}
										else {
											throw new DicomException("Missing per-frame PlanePositionSequence");
										}
									}
								}
								else {
									throw new DicomException("Missing or incorrect number of values for ImageOrientationPatient in shared PlaneOrientationSequence");
								}
							}
							else {
								throw new DicomException("Missing ImageOrientationPatient in shared PlaneOrientationSequence");
							}
						}
						else {
							throw new DicomException("Missing SpacingBetweenSlices in shared PixelMeasuresSequence");
						}
					}
					else {
						throw new DicomException("Missing or incorrect number of values for PixelSpacing in shared PixelMeasuresSequence");
					}
				}
				else {
					throw new DicomException("Missing PixelSpacing in shared PixelMeasuresSequence");
				}
			}
			else {
				throw new DicomException("Expected share orientation and pixel measures and per-frame position functioanl groups");
			}
		}
		else {
			throw new DicomException("Missing SharedFunctionalGroupsSequence or PerFrameFunctionalGroupsSequence");
		}
	}
	
	protected int[] compute2DInPlaneBoundingBoxForSegments(AttributeList list) throws DicomException {
		int minx = Integer.MAX_VALUE;
		int maxx = 0;
		int miny = Integer.MAX_VALUE;
		int maxy = 0;
		
		int     oldColumns = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Columns,0);
		int        oldRows = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Rows,0);
		int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfFrames,1);
		int     bitsStored = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsStored,1);

		Attribute aPixelData = list.get(TagFromName.PixelData);
		if (aPixelData != null) {
			slf4jlogger.debug("compute2DInPlaneBoundingBoxForSegments(): aPixelData is {}",aPixelData.getClass());
			// this brings the whole segmentation into memory ... not very scalable :(
			byte[] oldBytes = aPixelData.getByteValues();
			byte[] newBytes = null;
			if (bitsStored == 1) {
				int expectedOldByteValuesSize = (numberOfFrames * oldRows * oldColumns - 1) / 8 + 1;	// 1 bit per pixel
				if (oldBytes.length >= expectedOldByteValuesSize) {
					for (int f=0; f<numberOfFrames; ++f) {
						slf4jlogger.debug("compute2DInPlaneBoundingBoxForSegments(): Searching frame {}",f);
						for (int row=0; row<oldRows; ++row) {
							for (int col=0; col<oldColumns; ++col) {
								int oldBit = getBit(oldBytes,f,row,col,oldRows,oldColumns);
								if (oldBit != 0) {
									slf4jlogger.debug("compute2DInPlaneBoundingBoxForSegments(): Non-zero pixel at col {} row {}",col,row);
									if (col < minx) minx=col;
									if (col > maxx) maxx=col;
									if (row < miny) miny=row;
									if (row > maxy) maxy=row;
								}
							}
						}
						slf4jlogger.debug("minx so far {}",minx);
						slf4jlogger.debug("maxx so far {}",maxx);
						slf4jlogger.debug("miny so far {}",miny);
						slf4jlogger.debug("maxy so far {}",maxy);
					}
				}
				else {
					throw new DicomException("For 1 bit image, expected "+expectedOldByteValuesSize+" bytes in PixelData but were "+oldBytes.length);
				}
			}
			else if (bitsStored == 8) {
				int expectedOldByteValuesSize = numberOfFrames * oldRows * oldColumns;	// 1 byte per pixel
				if (oldBytes.length >= expectedOldByteValuesSize) {
					int oldByteOffset = 0;
					for (int f=0; f<numberOfFrames; ++f) {
						for (int row=0; row<oldRows; ++row) {
							for (int col=0; col<oldColumns; ++col) {
								byte value = oldBytes[oldByteOffset++];
								if (value != 0) {
									slf4jlogger.debug("Non-zero pixel at col {} row {}",col,row);
									if (col < minx) minx=col;
									if (col > maxx) maxx=col;
									if (row < miny) miny=row;
									if (row > maxy) maxy=row;
								}
							}
						}
					}
				}
				else {
					throw new DicomException("For 8 bit image, expected "+expectedOldByteValuesSize+" bytes in PixelData but were "+oldBytes.length);
				}
			}
			else {
				throw new DicomException("BitsStored "+bitsStored+" not supported");
			}
		}
		else {
			throw new DicomException("Missing PixelData attribute");
		}

		slf4jlogger.debug("minx {}",minx);
		slf4jlogger.debug("maxx {}",maxx);
		slf4jlogger.debug("miny {}",miny);
		slf4jlogger.debug("maxy {}",maxy);
		
		// add padding but not beyond original
		
		//int pad = 0;
		int pad = 2;

		if (pad > 0) {
			for (int p=pad; minx > 0 && p > 0; --p) --minx;
			for (int p=pad; miny > 0 && p > 0; --p) --miny;

			for (int p=pad; maxx < (oldColumns-1) && p > 0; --p) ++maxx;
			for (int p=pad; maxy < (oldRows-1)    && p > 0; --p) ++maxy;

			slf4jlogger.debug("padded oldColumns {}",oldColumns);
			slf4jlogger.debug("padded oldRows {}",oldRows);
			slf4jlogger.debug("padded minx {}",minx);
			slf4jlogger.debug("padded maxx {}",maxx);
			slf4jlogger.debug("padded miny {}",miny);
			slf4jlogger.debug("padded maxy {}",maxy);
		}
		
		//int padtotaltomultipleof = 0;
		int padtotaltomultipleof = 16;

		if (bitsStored == 1 && padtotaltomultipleof != 0) {
			// clamp total number of bits per frame to multiple of padtotaltomultipleof (e.g., if we later want to split raw bytes into separate files per frame)
			while (true) {
				int newColumns = maxx - minx + 1;
				int    newRows = maxy - miny + 1;
				int bitsperframe = newRows * newColumns;
				if (bitsperframe%padtotaltomultipleof == 0
				 || maxx >= (oldColumns-1)
				 || maxy >= (oldRows-1)
				) break;
				++maxx;
			}
			while (true) {
				int newColumns = maxx - minx + 1;
				int    newRows = maxy - miny + 1;
				int bitsperframe = newRows * newColumns;
				if (bitsperframe%padtotaltomultipleof == 0
				 || maxx >= (oldColumns-1)
				 || maxy >= (oldRows-1)
				) break;
				++maxx;
			}

			slf4jlogger.debug("padded to total size multiple of {} minx {}",padtotaltomultipleof,minx);
			slf4jlogger.debug("padded to total size multiple of {} maxx {}",padtotaltomultipleof,maxx);
			slf4jlogger.debug("padded to total size multiple of {} miny {}",padtotaltomultipleof,miny);
			slf4jlogger.debug("padded to total size multiple of {} maxy {}",padtotaltomultipleof,maxy);
			int newColumns = maxx - minx + 1;
			int    newRows = maxy - miny + 1;
			int bitsperframe = newRows * newColumns;
			slf4jlogger.debug("padded to total size multiple of {} columns {}",padtotaltomultipleof,newColumns);
			slf4jlogger.debug("padded to total size multiple of {} rows {}",padtotaltomultipleof,newRows);
			slf4jlogger.debug("padded to total size multiple of {} bitsperframe {}",padtotaltomultipleof,bitsperframe);
		}

		int[] box = new int[4];
		box[0] = minx;
		box[1] = miny;
		box[2] = maxx;
		box[3] = maxy;
		return box;
	}

	// copied from IndexedLabelMapToSegmentation
	// could use LUT rather than shift to populate selector every time ? would be faster :(
	protected static void setBit(byte[] pixelData,int f,int r,int c,int rows,int columns) {
		long pixelsPerFrame = rows * columns;
		long pixelOffset = pixelsPerFrame * f + columns * r + c;
		int byteOffset = (int)(pixelOffset/8);
		int bitOffset = (int)(pixelOffset%8);
		int selector = 1<<bitOffset;	// fill from low end
		pixelData[byteOffset] = (byte)(pixelData[byteOffset] | selector);
	}
	
	protected static int getBit(byte[] pixelData,int f,int r,int c,int rows,int columns) {
		long pixelsPerFrame = rows * columns;
		long pixelOffset = pixelsPerFrame * f + columns * r + c;
		int byteOffset = (int)(pixelOffset/8);
		int bitOffset = (int)(pixelOffset%8);
		int selector = 1<<bitOffset;	// fill from low end
		int bit = (pixelData[byteOffset] & selector) == 0 ? 0 : 1;
		return bit;
	}

	protected void clipAllFramesToSpecifiedSize(AttributeList list,int[] box) throws DicomException {
		int     oldColumns = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Columns,0);
		int        oldRows = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Rows,0);
		int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfFrames,1);
		int     bitsStored = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsStored,1);

		int newMinX = box[0];
		int newMinY = box[1];
		int newMaxX = box[2];
		int newMaxY = box[3];

		int newColumns = newMaxX - newMinX + 1;
		int    newRows = newMaxY - newMinY + 1;

		slf4jlogger.debug("newColumns {}",newColumns);
		slf4jlogger.debug("newRows {}",newRows);

		Attribute aPixelData = list.get(TagFromName.PixelData);
		if (aPixelData != null) {
			// this brings the whole segmentation into memory ... not very scalable :(
			byte[] oldBytes = aPixelData.getByteValues();
			byte[] newBytes = null;
			if (bitsStored == 1) {
				int expectedOldByteValuesSize = (numberOfFrames * oldRows * oldColumns - 1) / 8 + 1;	// 1 bit per pixel
				if (oldBytes.length >= expectedOldByteValuesSize) {
					int newByteValuesSize = (numberOfFrames * newRows * newColumns) / 8;
					if (newByteValuesSize %2 != 0) ++newByteValuesSize;	// pad to even length but only if necessary
					newBytes = newByteValuesSize % 2 == 0 ? new byte[newByteValuesSize] : new byte[newByteValuesSize+1];	// pad to even word length for DICOM
					
					for (int f=0; f<numberOfFrames; ++f) {
						for (int newRow=0; newRow<newRows; ++newRow) {
							for (int newCol=0; newCol<newColumns; ++newCol) {
								int bit = getBit(oldBytes,f,newMinY+newRow,newMinX+newCol,oldRows,oldColumns);
								if (bit != 0) {
									setBit(newBytes,f,newRow,newCol,newRows,newColumns);
								}
							}
						}
					}
				}
				else {
					throw new DicomException("For 1 bit image, expected "+expectedOldByteValuesSize+" bytes in PixelData but were "+oldBytes.length);
				}
			}
			else if (bitsStored == 8) {
				int expectedOldByteValuesSize = numberOfFrames * oldRows * oldColumns;	// 1 byte per pixel
				if (oldBytes.length >= expectedOldByteValuesSize) {
					int newByteValuesSize = numberOfFrames * newRows * newColumns;
					newBytes = newByteValuesSize % 2 == 0 ? new byte[newByteValuesSize] : new byte[newByteValuesSize+1];	// pad to even word length for DICOM
					
					int oldByteOffset = 0;
					int newByteOffset = 0;
					for (int f=0; f<numberOfFrames; ++f) {
						for (int row=0; row<oldRows; ++row) {
							for (int col=0; col<oldColumns; ++col) {
								byte value = oldBytes[oldByteOffset++];
								if (row >= newMinY
								 && row <= newMaxY
								 && col >= newMinX
								 && col <= newMaxX) {
									newBytes[newByteOffset++] = value;
								}
							}
						}
					}
				}
				else {
					throw new DicomException("For 8 bit image, expected "+expectedOldByteValuesSize+" bytes in PixelData but were "+oldBytes.length);
				}
			}
			else {
				throw new DicomException("BitsStored "+bitsStored+" not supported");
			}
			
			if (newBytes != null) {
				aPixelData = new OtherByteAttribute(TagFromName.PixelData);
				aPixelData.setValues(newBytes);
				list.put(aPixelData);
				
				{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(newColumns); list.put(a); }
				{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows);    a.addValue(newRows);    list.put(a); }
			}
		}
		else {
			throw new DicomException("Missing PixelData attribute");
		}
	}

	/**
	 * <p>Read a DICOM segmentation image, and shrink its extent to the minimum bounding box surrounding the segments.</p>
	 *
	 * @param	inputFileName	the input file name
	 * @param	outputFileName	the output file name
	 */
	public ShrinkSegmentationToBoundingBox(String inputFileName,String outputFileName) throws DicomException, FileNotFoundException, IOException {
		AttributeList list = new AttributeList();
		DicomInputStream in = new DicomInputStream(new BufferedInputStream(new FileInputStream(inputFileName)));
		list.read(in);
		in.close();
		
		String sopClassUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SOPClassUID);
		if (!sopClassUID.equals(SOPClass.SegmentationStorage)) {
			throw new DicomException("Input file is not a segmentation image");
		}
		
		int[] box = compute2DInPlaneBoundingBoxForSegments(list);
		
		// add TLHC of box to every plane position origins projected onto plane of orientation
		
		clipAllFramesToSpecifiedSize(list,box);
		
		addTLHCOfClippedRegionToImagePosition(list,box);

		list.removeMetaInformationHeaderAttributes();
		FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
		list.write(outputFileName,TransferSyntax.ExplicitVRLittleEndian,true,true);
	}
	
	/**
	 * <p>Read a DICOM segmentation image, and shrink its extent to the minimum bounding box surrounding the segments.</p>
	 *
	 * @param	arg	two parameters, the inputFile, outputFile
	 */
	public static void main(String arg[]) {
		try {
			if (arg.length == 2) {
				new ShrinkSegmentationToBoundingBox(arg[0],arg[1]);
			}
			else {
				System.err.println("Error: Incorrect number of arguments");
				System.err.println("Usage: ShrinkSegmentationToBoundingBox inputFile outputFile");
				System.exit(1);
			}
		}
		catch (Exception e) {
			slf4jlogger.error("",e);	// use SLF4J since may be invoked from script
		}
	}
}
