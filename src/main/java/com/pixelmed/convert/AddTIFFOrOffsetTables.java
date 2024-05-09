/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.convert;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.BinaryOutputStream;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.OtherByteAttribute;
import com.pixelmed.dicom.OtherByteAttributeMultipleCompressedFrames;
import com.pixelmed.dicom.OtherByteAttributeMultipleFilesOnDisk;
import com.pixelmed.dicom.OtherByteAttributeCompressedSeparateFramesOnDisk;
import com.pixelmed.dicom.OtherWordAttribute;
import com.pixelmed.dicom.OtherWordAttributeMultipleFilesOnDisk;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.util.Iterator;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class for to add make a dual-personality DICOM-TIFF file and/or add Basic or Extended Offset Tables.</p>
 *
 * @see	TIFFToDicom
 *
 * @author	dclunie
 */

public class AddTIFFOrOffsetTables {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/convert/AddTIFFOrOffsetTables.java,v 1.6 2022/05/19 11:33:17 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(AddTIFFOrOffsetTables.class);

	private int getTIFFPhotometricFromDICOMPhotometricInterpretation(String photometricInterpretation) {
		int photometric = -1;
		switch (photometricInterpretation) {
			case "MONOCHROME1":		photometric = 0; break;
			case "MONOCHROME2":		photometric = 1; break;
			case "RGB":				photometric = 2; break;
			case "PALETTE COLOR":	photometric = 3; break;
			case "TRANSPARENCY":	photometric = 4; break;		// not standard DICOM
			case "CMYK":			photometric = 5; break;		// retired in DICOM
			case "YBR_ICT":			photometric = 6; break;
			case "YBR_RCT":			photometric = 6; break;
			case "YBR_FULL_422":	photometric = 6; break;
			case "CIELAB":			photometric = 8; break;		// not standard DICOM
		}
		return photometric;
	}
	
	private int getTIFFCompressionFromTransferSyntax(String transferSyntax) {
		int compression = 0;
		switch (transferSyntax) {
			case TransferSyntax.ImplicitVRLittleEndian:		compression = 1; break;
			case TransferSyntax.ExplicitVRLittleEndian:		compression = 1; break;
			case TransferSyntax.ExplicitVRBigEndian:		compression = 1; break;			// ? big endian actually handled properly in TIFF
			case TransferSyntax.JPEGBaseline:				compression = 7; break;
			case TransferSyntax.JPEG2000:					compression = 33005; break;		// ? should check if RGB
			case TransferSyntax.JPEG2000Lossless:			compression = 33005; break;		// ? should check if RGB
		}
		return compression;
	}


	// reuse same private group and creator as for com.pixelmed.dicom.PrivatePixelData
	private static final String pixelmedPrivateCreatorForPyramidData = "PixelMed Publishing";
	private static final int pixelmedPrivatePyramidDataGroup = 0x7FDF;	// Must be BEFORE (7FE0,0010) because we assume elsewhere that DataSetTrailingPadding will immediately follow (7FE0,0010)
	private static final AttributeTag pixelmedPrivatePyramidDataBlockReservation = new AttributeTag(pixelmedPrivatePyramidDataGroup,0x0010);
	private static final AttributeTag pixelmedPrivatePyramidData = new AttributeTag(pixelmedPrivatePyramidDataGroup,0x1001);

	private static long UNSIGNED32_MAX_VALUE = 0xffffffffl;

	private static long getByteOffsetsAndLengthsOfTileDataFromStartOfFileForPixelDataAttribute(long byteoffset,Attribute a,int numberOfFrames,int numberOfTiles,boolean encodeAsExplicit,boolean encodeAsLittleEndian,long[] tileDataByteOffsets,long[] tileDataLengths,boolean mergeSamplesPerPixelTiles,long samplesPerPixel) throws DicomException {
		if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("0x{} ({} dec): {} {}",Long.toHexString(byteoffset),byteoffset,a.toString(),a.getClass());
		long vl = a.getVL();
		if (vl == 0xffffffffl) {
			if (mergeSamplesPerPixelTiles) {
				throw new DicomException("Planar Configuration with separate planes not supported for compressed pixel data");
			}
			// depending on the class of the OB attribute, get the offsets and lengths
			if (a instanceof OtherByteAttributeMultipleCompressedFrames) {
				OtherByteAttributeMultipleCompressedFrames ob = (OtherByteAttributeMultipleCompressedFrames)a;
				// simulate OtherByteAttributeMultipleCompressedFrames.write()
				byteoffset += a.getLengthOfBaseOfEncodedAttribute(encodeAsExplicit,encodeAsLittleEndian);
				byteoffset += 8;	// Item tag for empty basic offset table
				
				byte[][] frames = ob.getFrames();
				File[] files = ob.getFiles();
				
				int nFrames = 0;
				if (files != null) {
					nFrames = files.length;
				}
				else if (frames != null) {
					nFrames = frames.length;
				}
				else {
					throw new DicomException("Not yet implemented - calculating offsets and lengths from "+a.getClass()+" with all frames in one fragment");
				}
				
				if (nFrames != numberOfFrames) {
					throw new DicomException("Not yet implemented - calculating offsets and lengths from "+a.getClass()+" when number of files not the same as number of frames");
				}
				
				if (nFrames > 0) {
					for (int f=0; f<nFrames; ++f) {
						File file = null;
						byte[] frame = null;
						long frameLength = 0;
						if (files != null) {
							file = files[f];
							frameLength = file.length();
						}
						else {
							frame = frames[f];
							frameLength = frame.length;
						}
						tileDataLengths[f] = frameLength;		// does NOT include padding
						long padding = frameLength % 2;
						long paddedLength = frameLength + padding;
						byteoffset += 8;							// Item tag for one fragment per frame
						tileDataByteOffsets[f] = byteoffset;		// points to start of compressed data, not start of item
						if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Frame {} 0x{} ({} dec): length = 0x{} ({} dec)",f,Long.toHexString(byteoffset),byteoffset,Long.toHexString(frameLength),frameLength);
						byteoffset += paddedLength;
					}
				}
				byteoffset += 8;	// SequenceDelimitationItem
			}
			else if (a instanceof OtherByteAttributeCompressedSeparateFramesOnDisk) {
				OtherByteAttributeCompressedSeparateFramesOnDisk ob = (OtherByteAttributeCompressedSeparateFramesOnDisk)a;
				byteoffset += a.getLengthOfBaseOfEncodedAttribute(encodeAsExplicit,encodeAsLittleEndian);
				byteoffset += 8;	// Item tag for empty basic offset table
				
				int nFrames = ob.getNumberOfFrames();
				
				if (nFrames != numberOfFrames) {
					throw new DicomException("Not yet implemented - calculating offsets and lengths from "+a.getClass()+" but expected number of frames "+numberOfFrames+"does not match actual number of frames"+nFrames);
				}

				long[][] frameItemLengths = ob.getFrameItemLengths();
				if (frameItemLengths.length != nFrames) {
					throw new DicomException("Not yet implemented - calculating offsets and lengths from "+a.getClass()+" when number of frames "+nFrames+"does not match length of frameItemLengths"+frameItemLengths.length);
				}
				
				if (nFrames > 0) {
					for (int f=0; f<nFrames; ++f) {
						long[] fragmentLengths = frameItemLengths[f];
						if (fragmentLengths == null) {
							throw new DicomException("Not yet implemented - calculating offsets and lengths from "+a.getClass()+" but no fragment lengths for frame "+f);
						}
						else if (fragmentLengths.length == 0) {
							throw new DicomException("Not yet implemented - calculating offsets and lengths from "+a.getClass()+" but missing fragment length for frame "+f);
						}
						else if (fragmentLengths.length > 1) {
							throw new DicomException("Not yet implemented - calculating offsets and lengths from "+a.getClass()+" when more than one fragment per frame for frame "+f);
						}

						long frameLength = fragmentLengths[0];

						tileDataLengths[f] = frameLength;		// does NOT include padding
						long padding = frameLength % 2;
						long paddedLength = frameLength + padding;
						byteoffset += 8;							// Item tag for one fragment per frame
						tileDataByteOffsets[f] = byteoffset;		// points to start of compressed data, not start of item
						if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Frame {} 0x{} ({} dec): length = 0x{} ({} dec)",f,Long.toHexString(byteoffset),byteoffset,Long.toHexString(frameLength),frameLength);
						byteoffset += paddedLength;
					}
				}
				byteoffset += 8;	// SequenceDelimitationItem
			}
			else {
				throw new DicomException("Not yet implemented - calculating offsets and lengths from "+a.getClass());
			}
		}
		else {
			byteoffset += a.getLengthOfBaseOfEncodedAttribute(encodeAsExplicit,encodeAsLittleEndian);
			long frameLength = vl / numberOfFrames;
			if (numberOfFrames > 0) {
				if (mergeSamplesPerPixelTiles) {	// (001351)
					long tileLength = frameLength / samplesPerPixel;
					// DICOM encodes separate components successively within each tile (frame)
					for (int f=0; f<numberOfFrames; ++f) {
						for (int c=0; c<samplesPerPixel; ++c) {
							// TIFF encodes all tiles of one component before the next component
							int t = c*numberOfFrames + f;
							tileDataLengths[t] = tileLength;
							tileDataByteOffsets[t] = byteoffset;
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("DICOM Frame {} TIFF Tile {} 0x{} ({} dec): length = 0x{} ({} dec)",f,t,Long.toHexString(byteoffset),byteoffset,Long.toHexString(tileLength),tileLength);
							byteoffset += tileLength;
						}
					}
				}
				else {
					for (int f=0; f<numberOfFrames; ++f) {
						tileDataLengths[f] = frameLength;		// we need to return the lengths by populating the array that we were supplied with
						tileDataByteOffsets[f] = byteoffset;
						if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Frame {} 0x{} ({} dec): length = 0x{} ({} dec)",f,Long.toHexString(byteoffset),byteoffset,Long.toHexString(frameLength),frameLength);
						byteoffset += frameLength;
					}
				}
				if (a.getVL() != a.getPaddedVL()) ++byteoffset;		// even padding at end
			}
		}
		if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("getByteOffsetsAndLengthsOfTileDataFromStartOfFileForPixelDataAttribute(): returns 0x{} ({} dec): {} {}",Long.toHexString(byteoffset),byteoffset);
		return byteoffset;
	}
	
	// package private scope so that TIFFToDICOM can use it
	static long getByteOffsetsAndLengthsOfTileDataFromStartOfFile(AttributeList list,String transferSyntaxUID,long[][] tileDataByteOffsets,long[][] tileDataLengths,long[] imageWidths,long[] imageLengths,boolean mergeSamplesPerPixelTiles,long samplesPerPixel) throws DicomException {
		long byteoffset = 132;						// assume preamble and magic number, else no reason to call this method
		boolean inDataSetPastMetaHeader = false;	// starting in metaheader
		boolean encodeAsExplicit = true;			// always for metaheader
		boolean encodeAsLittleEndian = true;		// always for metaheader
		TransferSyntax transferSyntax = new TransferSyntax(transferSyntaxUID);
		boolean dataSetIsExplicitVR = transferSyntax.isExplicitVR();
		boolean dataSetIsLittleEndian = transferSyntax.isLittleEndian();
		Iterator<Attribute> i = list.values().iterator();
		while (i.hasNext()) {
			Attribute a = ((Attribute)i.next());
			AttributeTag t = a.getTag();
			if (!inDataSetPastMetaHeader && !t.isFileMetaInformationGroup()) {
				// once set, stay set
				inDataSetPastMetaHeader = true;
				encodeAsExplicit = dataSetIsExplicitVR;
				encodeAsLittleEndian = dataSetIsLittleEndian;
			}
			if (t.equals(TagFromName.PixelData) || t.equals(TagFromName.FloatPixelData) || t.equals(TagFromName.DoubleFloatPixelData)) {	// (001323)
				slf4jlogger.debug("getByteOffsetsAndLengthsOfTileDataFromStartOfFile(): Have PixelData or FloatPixelData or DoubleFloatPixelData");
				imageWidths[0] = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.TotalPixelMatrixColumns,0);
				imageLengths[0] = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.TotalPixelMatrixRows,0);
				int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfFrames,1);
				slf4jlogger.debug("getByteOffsetsAndLengthsOfTileDataFromStartOfFile(): numberOfFrames = {}",numberOfFrames);
				int numberOfTiles = numberOfFrames * (mergeSamplesPerPixelTiles ? (int)samplesPerPixel : 1);	// (001351)
				slf4jlogger.debug("getByteOffsetsAndLengthsOfTileDataFromStartOfFile(): numberOfTiles = {}",numberOfTiles);
				tileDataByteOffsets[0] = new long[numberOfTiles];
				tileDataLengths[0] = new long[numberOfTiles];
				byteoffset = getByteOffsetsAndLengthsOfTileDataFromStartOfFileForPixelDataAttribute(byteoffset,a,numberOfFrames,numberOfTiles,encodeAsExplicit,encodeAsLittleEndian,tileDataByteOffsets[0],tileDataLengths[0],mergeSamplesPerPixelTiles,samplesPerPixel);
			}
			else if (t.equals(pixelmedPrivatePyramidData)) {	// don't need to check creator since could not be here any other way
				int pyramidLevel = 1;
				SequenceAttribute pyramidData = (SequenceAttribute)a;
				byteoffset += pyramidData.getLengthOfBaseOfEncodedAttribute(encodeAsExplicit,encodeAsLittleEndian);
				Iterator<SequenceItem> iti = pyramidData.iterator();
				while (iti.hasNext()) {
					byteoffset += 8;		// length of Item
					SequenceItem it = iti.next();
					AttributeList ilist = it.getAttributeList();
					if (ilist != null) {
						// PlanarConfiguration of our own downsampled layers should always be 0 (DICOM, color-by-pixel) but check just in case ... (001351)
						int pyramidSamplesPerPixel = Attribute.getSingleIntegerValueOrDefault(ilist,TagFromName.SamplesPerPixel,0);
						int pyramidPlanarConfiguration = Attribute.getSingleIntegerValueOrDefault(ilist,TagFromName.PlanarConfiguration,0);
						boolean pyramidMergeSamplesPerPixelTiles = pyramidPlanarConfiguration == 1 && pyramidSamplesPerPixel > 1;  // DICOM PlanarConfiguration 1 is TIFF PlanarConfig 2
						slf4jlogger.debug("getByteOffsetsAndLengthsOfTileDataFromStartOfFile(): for PrivatePyramidData, mergeSamplesPerPixelTiles = {}",pyramidMergeSamplesPerPixelTiles);
						Iterator<Attribute> ili = ilist.values().iterator();
						while (ili.hasNext()) {
							Attribute aa = ili.next();
							AttributeTag tt = aa.getTag();
							if (tt.equals(TagFromName.PixelData)) {
								imageWidths[pyramidLevel] = Attribute.getSingleIntegerValueOrDefault(ilist,TagFromName.TotalPixelMatrixColumns,0);
								imageLengths[pyramidLevel] = Attribute.getSingleIntegerValueOrDefault(ilist,TagFromName.TotalPixelMatrixRows,0);
								int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(ilist,TagFromName.NumberOfFrames,1);
								slf4jlogger.debug("getByteOffsetsAndLengthsOfTileDataFromStartOfFile(): numberOfFrames = {}",numberOfFrames);
								int numberOfTiles = numberOfFrames * (mergeSamplesPerPixelTiles ? (int)samplesPerPixel : 1);	// (001351);
								slf4jlogger.debug("getByteOffsetsAndLengthsOfTileDataFromStartOfFile(): numberOfTiles = {}",numberOfTiles);
								tileDataByteOffsets[pyramidLevel] = new long[numberOfTiles];
								tileDataLengths[pyramidLevel] = new long[numberOfTiles];
								byteoffset = getByteOffsetsAndLengthsOfTileDataFromStartOfFileForPixelDataAttribute(byteoffset,aa,numberOfFrames,numberOfTiles,encodeAsExplicit,encodeAsLittleEndian,tileDataByteOffsets[pyramidLevel],tileDataLengths[pyramidLevel],pyramidMergeSamplesPerPixelTiles,pyramidSamplesPerPixel);
							}
							else {
								byteoffset += aa.getLengthOfEntireEncodedAttribute(encodeAsExplicit,encodeAsLittleEndian);
							}
						}
					}
					++pyramidLevel;
					byteoffset += 8;		// length of Item Delimiter
				}
				byteoffset += 8;			// length of Sequence Delimiter
			}
			else {
				long l = a.getLengthOfEntireEncodedAttribute(encodeAsExplicit,encodeAsLittleEndian);
				if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("0x{} ({} dec): {} encoded length = 0x{} ({} dec)",Long.toHexString(byteoffset),byteoffset,a.toString(),Long.toHexString(l),l);
				byteoffset += l;
			}
		}
		long byteOffsetFromFileStartOfNextAttributeAfterPixelData = byteoffset;
		return byteOffsetFromFileStartOfNextAttributeAfterPixelData;
	}
	
	private static void writeUnsigned32Or64Bits(BinaryOutputStream o,boolean use64,long value) throws IOException {
		if (use64) { o.writeUnsigned64(value); } else { o.writeUnsigned32(value); }
	}
	
	// package private scope so that TIFFToDICOM can use it
	static byte[] makeTIFFInPreambleAndAddDataSetTrailingPadding(long byteOffsetFromFileStartOfNextAttributeAfterPixelData,int numberOfPyramidLevels,long[][] tileDataByteOffsets,long[][] tileDataLengths,long[] imageWidths,long[] imageLengths,
			AttributeList list,long tileWidth,long tileLength,long bitsPerSample,long compression,long basePhotometric,long lowerPhotometric,long samplesPerPixel,long planarConfig,long sampleFormat,
			byte[] iccProfile,double mmPerPixelXBaseLayer,double mmPerPixelYBaseLayer,boolean useBigTIFF) throws DicomException, IOException {
		
		//boolean useBigTIFF = true;
		slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): useBigTIFF = {}",useBigTIFF);
		
		boolean useResolution = true;
		slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): useResolution = {}",useResolution);
		
		boolean useSampleFormat = samplesPerPixel == 1 && bitsPerSample > 8;
		
		boolean includeYCbCrSubSampling = false;	// if present, causes Sedeen to fail to view the TIFF file
		slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): includeYCbCrSubSampling = {}",includeYCbCrSubSampling);

		boolean haveICCProfile = iccProfile != null && iccProfile.length > 0;

		byte[] preamble = null;
		slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): byteOffsetFromFileStartOfNextAttributeAfterPixelData = {}",byteOffsetFromFileStartOfNextAttributeAfterPixelData);
		// offset of IFD is start of value part of DataSetTrailingPadding
		long offsetofIFD = byteOffsetFromFileStartOfNextAttributeAfterPixelData + 12 /* group and element, reserved bytes + VR,  32 bit value length */;
		slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): offsetofIFD = {}",offsetofIFD);
		{
		
			ByteArrayOutputStream pbaos = new ByteArrayOutputStream(8);
			BinaryOutputStream pos = new BinaryOutputStream(pbaos,false/* not big endian*/);
			pos.write((byte)'I');	// Intel (little endian) byte order
			pos.write((byte)'I');

			if (useBigTIFF) {
				// "http://www.awaresystems.be/imaging/tiff/bigtiff.html"
				pos.writeUnsigned16(0x002B);			// TIFF Version number
				pos.writeUnsigned16(8);					// Bytesize of offsets - Always 8 in BigTIFF
				pos.writeUnsigned16(0);					// Always 0
				pos.writeUnsigned64(offsetofIFD);
			}
			else {
				pos.writeUnsigned16(0x002A);			// TIFF Version number
				if (offsetofIFD > UNSIGNED32_MAX_VALUE) {
					throw new DicomException("Offset of first IFD is too large to fit in 32 bits 0x"+Long.toHexString(offsetofIFD)+" ("+offsetofIFD+" dec)");
				}
				pos.writeUnsigned32(offsetofIFD);
			}
			pos.flush();
			pos.close();
			
			byte[] populated = pbaos.toByteArray();
			
			preamble = new byte[128];
			System.arraycopy(populated,0,preamble,0,populated.length);
		}

		{
			ByteArrayOutputStream ifdaos = new ByteArrayOutputStream(8);
			BinaryOutputStream ifdos = new BinaryOutputStream(ifdaos,false/* not big endian*/);
			
			double mmPerPixelX = mmPerPixelXBaseLayer;
			double mmPerPixelY = mmPerPixelYBaseLayer;
			slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): numberOfPyramidLevels = {}",numberOfPyramidLevels);
			for (int pyramidLevel=0; pyramidLevel<numberOfPyramidLevels; ++pyramidLevel) {
				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): pyramidLevel = {}",pyramidLevel);

				int numberOfTiles = tileDataByteOffsets[pyramidLevel].length;
				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): numberOfTiles = {}",numberOfTiles);

				int numberOfIFDEntries = pyramidLevel == 0 ? 14: 15;		// Entry count
				
				if (useSampleFormat) {
					++numberOfIFDEntries;
				}
				
				if (includeYCbCrSubSampling) {
					++numberOfIFDEntries;
				}

				if (haveICCProfile) {
					++numberOfIFDEntries;
				}
				
				if (useBigTIFF) {
					ifdos.writeUnsigned64(numberOfIFDEntries);
				}
				else {
					ifdos.writeUnsigned16(numberOfIFDEntries);
				}
				
				long lengthOfIFD =
					  (useBigTIFF ? 8 : 2)							// numberOfIFDEntries is 2 or 8
					+ numberOfIFDEntries * (useBigTIFF ? 20 : 12)	// each entry is 12 or 20 bytes fixed length regardless of type
					+ (useBigTIFF ? 8 : 4);							// offset of next IFD is 4 or 8 bytes
				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): lengthOfIFD = {}",lengthOfIFD);

				long bitsPerSampleOffset = offsetofIFD + lengthOfIFD;
				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): bitsPerSampleOffset = {}",bitsPerSampleOffset);
				
				boolean useBitsPerSampleOffset = (useBigTIFF && samplesPerPixel > 4) || (!useBigTIFF && samplesPerPixel > 2);
				
				long xResolutionOffset = bitsPerSampleOffset + (useBitsPerSampleOffset ? 2*samplesPerPixel : 0);	// bitsPerSample is samplesPerPixel two byte (short) entries
				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): xResolutionOffset = {}",xResolutionOffset);
				
				long yResolutionOffset = xResolutionOffset + (useBigTIFF ? 0 : 8);	// xResolution was two four byte (long) entries if not BigTIFF
				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): yResolutionOffset = {}",yResolutionOffset);
				
				long iccProfileOffset = yResolutionOffset + (useBigTIFF ? 0 : 8);	// yResolution was two four byte (long) entries if not BigTIFF
				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): iccProfileOffset = {}",iccProfileOffset);

				boolean useTileOffsetAndByteCountsOffset = numberOfTiles > 1;	//otherwise will fit in place

				long tileOffsetsOffset = iccProfileOffset + (haveICCProfile ? iccProfile.length : 0);
				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): tileOffsetsOffset = {}",tileOffsetsOffset);
				
				long tileByteCountsOffset = tileOffsetsOffset + (useTileOffsetAndByteCountsOffset ? (numberOfTiles * (useBigTIFF ? 8 : 4)) : 0);	// tileOffsets was LONG8 or LONG
				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): tileByteCountsOffset = {}",tileByteCountsOffset);

				long offsetOfNextIFD = (pyramidLevel+1<numberOfPyramidLevels) ? (tileByteCountsOffset + (useTileOffsetAndByteCountsOffset ? (numberOfTiles * (useBigTIFF ? 8 : 4)) : 0)) : 0;	// tileByteCounts was LONG8 or LONG
				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): offsetOfNextIFD = {}",offsetOfNextIFD);
				if (!useBigTIFF) {
					if (offsetOfNextIFD > UNSIGNED32_MAX_VALUE) {
						throw new DicomException("Offset for IFD is too large to fit in 32 bits 0x"+Long.toHexString(offsetOfNextIFD)+" ("+offsetOfNextIFD+" dec)");
					}
				}

				if (pyramidLevel > 0) {
					ifdos.writeUnsigned16(0x00fe);	// NewSubfileType
					ifdos.writeUnsigned16(0x0004);	// - type   LONG
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,0x0001);	// - length 1
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,0x0001);	// - reduced resolution version
				}
				{
					ifdos.writeUnsigned16(TIFFTags.IMAGEWIDTH);
					ifdos.writeUnsigned16(0x0004);	// - type   LONG
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,0x0001);	// - length 1
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,imageWidths[pyramidLevel]);
				}
				{
					ifdos.writeUnsigned16(TIFFTags.IMAGELENGTH);
					ifdos.writeUnsigned16(0x0004);	// - type   LONG
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,0x0001);	// - length 1
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,imageLengths[pyramidLevel]);
				}
				{
					ifdos.writeUnsigned16(TIFFTags.BITSPERSAMPLE);
					ifdos.writeUnsigned16(0x0003);	// - type   SHORT
					if (useBigTIFF) {	// up to 4 values fit in 8 byte value field
						ifdos.writeUnsigned64(samplesPerPixel);		// - length usually 1 or 3
						if (samplesPerPixel <= 4) {
							ifdos.writeUnsigned16((int)bitsPerSample);
							ifdos.writeUnsigned16(samplesPerPixel > 1 ? (int)bitsPerSample : 0);
							ifdos.writeUnsigned16(samplesPerPixel > 2 ? (int)bitsPerSample : 0);
							ifdos.writeUnsigned16(samplesPerPixel > 3 ? (int)bitsPerSample : 0);
						}
						else {
							ifdos.writeUnsigned64(bitsPerSampleOffset);
						}
					}
					else {	// three values (for RGB) do not fit in value field
						ifdos.writeUnsigned32(samplesPerPixel);		// - length usually 1 or 3
						if (samplesPerPixel <= 2) {
							ifdos.writeUnsigned16((int)bitsPerSample);
							ifdos.writeUnsigned16(samplesPerPixel > 1 ? (int)bitsPerSample : 0);
						}
						else {
							ifdos.writeUnsigned32(bitsPerSampleOffset);
						}
					}
				}
				{
					ifdos.writeUnsigned16(TIFFTags.COMPRESSION);
					ifdos.writeUnsigned16(0x0003);	// - type   SHORT
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,0x0001);	// - length 1
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,compression);
				}
				{
					ifdos.writeUnsigned16(TIFFTags.PHOTOMETRIC);
					ifdos.writeUnsigned16(0x0003);	// - type   SHORT
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,0x0001);	// - length 1
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,pyramidLevel == 0 ? basePhotometric : lowerPhotometric);	// the original for the first layer and then the default for the compression for later levels
				}
				{
					ifdos.writeUnsigned16(TIFFTags.SAMPLESPERPIXEL);
					ifdos.writeUnsigned16(0x0003);	// - type   SHORT
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,0x0001);	// - length 1
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,samplesPerPixel);
				}
				
				//XResolution (282) RATIONAL (5) 1<10>	The number of pixels per ResolutionUnit in the ImageWidth (typically, horizontal - see Orientation) direction
				//YResolution (283) RATIONAL (5) 1<10>

				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): mmPerPixelX = {}",mmPerPixelX);
				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): mmPerPixelY = {}",mmPerPixelY);
				long resolutionNumeratorX = (!useResolution || mmPerPixelX == 0) ? 1 : Math.round(10.0d/mmPerPixelX);	// 10.0, because units are cm, not mm
				long resolutionNumeratorY = (!useResolution || mmPerPixelY == 0) ? 1 : Math.round(10.0d/mmPerPixelY);	// 10.0, because units are cm, not mm
				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): resolutionNumeratorX = {}",resolutionNumeratorX);
				slf4jlogger.debug("makeTIFFInPreambleAndAddDataSetTrailingPadding(): resolutionNumeratorY = {}",resolutionNumeratorY);
				{
					ifdos.writeUnsigned16(TIFFTags.XRESOLUTION);
					ifdos.writeUnsigned16(0x0005);			// - type RATIONAL
					if (useBigTIFF) {						// two LONG values do fit in 8 byte value field
						ifdos.writeUnsigned64(0x0001);		// - length 1
						ifdos.writeUnsigned32(resolutionNumeratorX);
						ifdos.writeUnsigned32(0x0001);		// denominator
					}
					else {									// two LONG values do fit in 4 byte value field
						ifdos.writeUnsigned32(0x0001);		// - length 1
						ifdos.writeUnsigned32(xResolutionOffset);
					}
				}
				{
					ifdos.writeUnsigned16(TIFFTags.YRESOLUTION);
					ifdos.writeUnsigned16(0x0005);			// - type RATIONAL
					if (useBigTIFF) {						// two LONG values do fit in 8 byte value field
						ifdos.writeUnsigned64(0x0001);		// - length 1
						ifdos.writeUnsigned32(resolutionNumeratorY);
						ifdos.writeUnsigned32(0x0001);		// denominator
					}
					else {									// two LONG values do fit in 4 byte value field
						ifdos.writeUnsigned32(0x0001);		// - length 1
						ifdos.writeUnsigned32(yResolutionOffset);
					}
				}

				{
					ifdos.writeUnsigned16(TIFFTags.PLANARCONFIG);
					ifdos.writeUnsigned16(0x0003);	// - type   SHORT
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,0x0001);	// - length 1
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,pyramidLevel == 0 ? planarConfig : 1);	// downsampled pyramid always color-by-pixel, but top level may vary though should have been changed to color-by-pixel (at least for WSI) by this point (001351); could check this by passing in from parent or extracting from list :(
				}
				
				{
					ifdos.writeUnsigned16(TIFFTags.RESOLUTIONUNIT);
					ifdos.writeUnsigned16(0x0003);	// - type   SHORT
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,0x0001);	// - length 1
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,useResolution ? 0x0003 : 0x0001);	// 1 is none, 3 is cm (!)
				}
				
				{
					ifdos.writeUnsigned16(TIFFTags.TILEWIDTH);
					ifdos.writeUnsigned16(0x0003);	// - type   SHORT
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,0x0001);	// - length 1
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,tileWidth);
				}
				{
					ifdos.writeUnsigned16(TIFFTags.TILELENGTH);
					ifdos.writeUnsigned16(0x0003);	// - type   SHORT
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,0x0001);	// - length 1
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,tileLength);
				}
				{
					ifdos.writeUnsigned16(TIFFTags.TILEOFFSETS);
					ifdos.writeUnsigned16(useBigTIFF ? 0x0010 : 0x0004);		// - type   LONG8  or LONG
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,numberOfTiles);	// - length
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,useTileOffsetAndByteCountsOffset ? tileOffsetsOffset : tileDataByteOffsets[pyramidLevel][0]);
				}
				{
					ifdos.writeUnsigned16(TIFFTags.TILEBYTECOUNTS);
					ifdos.writeUnsigned16(useBigTIFF ? 0x0010 : 0x0004);		// - type   LONG8  or LONG
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,numberOfTiles);	// - length
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,useTileOffsetAndByteCountsOffset ? tileByteCountsOffset : tileDataLengths[pyramidLevel][0]);
				}
				if (useSampleFormat) {
					ifdos.writeUnsigned16(TIFFTags.SAMPLEFORMAT);
					ifdos.writeUnsigned16(0x0003);	// - type   SHORT
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,0x0001);	// - length 1
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,sampleFormat);
				}
				if (includeYCbCrSubSampling) {
					ifdos.writeUnsigned16(TIFFTags.YCBCRSUBSAMPLING);
					ifdos.writeUnsigned16(0x0003);	// - type   SHORT
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,0x0002);	// - length 2
					ifdos.writeUnsigned16(pyramidLevel > 0 ? 0x0001 : 0x0001);	// horizontal subsampling - original is 1 (SVS file lies), default from Java codec is 1; override TIFF default of 2
					ifdos.writeUnsigned16(pyramidLevel > 0 ? 0x0001 : 0x0001);	// vertical   subsampling - original is 1 (SVS file lies), default from Java codec is 1; override TIFF default of 2
					if (useBigTIFF) {
						ifdos.writeUnsigned32(0x0000);		// value field is 64 not 32
					}
				}
				if (haveICCProfile) {
					ifdos.writeUnsigned16(TIFFTags.ICCPROFILE);		// This is a private tag - see icc1v42.pdf "B.4 Embedding ICC profiles in TIFF files"
					ifdos.writeUnsigned16(0x0007);		// - type   UNDEFINED
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,iccProfile.length);	// - length
					writeUnsigned32Or64Bits(ifdos,useBigTIFF,iccProfileOffset);
				}

				writeUnsigned32Or64Bits(ifdos,useBigTIFF,offsetOfNextIFD);
				
				// should now be positioned at bitsPerSampleOffset - write SHORT

				if (useBitsPerSampleOffset) {
					for (int i=0; i<samplesPerPixel; ++i) {
						ifdos.writeUnsigned16((int)bitsPerSample);
					}
				}
				// else wrote them in the value field since they fit
				
				// should now be positioned at xResolutionOffset

				if (!useBigTIFF) {
					ifdos.writeUnsigned32(resolutionNumeratorX);
					ifdos.writeUnsigned32(0x0001);	// denominator
				}
				// else wrote them in the longer value field since they fit

				// should now be positioned at yResolutionOffset

				if (!useBigTIFF) {
					ifdos.writeUnsigned32(resolutionNumeratorY);
					ifdos.writeUnsigned32(0x0001);	// denominator
				}
				// else wrote them in the longer value field since they fit
				
				// should now be positioned at iccProfileOffset

				if (iccProfile != null && iccProfile.length > 0) {
					ifdos.write(iccProfile);
				}
				
				// should now be positioned at tileOffsetsOffset
				
				for (int i=0; i<numberOfTiles; ++i) {
					long offset = tileDataByteOffsets[pyramidLevel][i];
					slf4jlogger.trace("makeTIFFInPreambleAndAddDataSetTrailingPadding(): pyramid level {} tile {} offset = {}",pyramidLevel,i,offset);
					if (useBigTIFF) {
						ifdos.writeUnsigned64(offset);
					}
					else {
						if (offset > UNSIGNED32_MAX_VALUE) {
							throw new DicomException("Offset for frame "+i+" is too large to fit in 32 bits 0x"+Long.toHexString(offset)+" ("+offset+" dec)");
						}
						ifdos.writeUnsigned32(offset);
					}
				}

				// should now be positioned at tileByteCountsOffset

				for (int i=0; i<numberOfTiles; ++i) {
					long length = tileDataLengths[pyramidLevel][i];
					slf4jlogger.trace("makeTIFFInPreambleAndAddDataSetTrailingPadding(): pyramid level {} tile {} length = {}",pyramidLevel,i,length);
					if (useBigTIFF) {
						ifdos.writeUnsigned64(length);
					}
					else {
						if (length > UNSIGNED32_MAX_VALUE) {
							throw new DicomException("Length for frame "+i+" is too large to fit in 32 bits 0x"+Long.toHexString(length)+" ("+length+" dec)");
						}
						ifdos.writeUnsigned32(length);
					}
				}

				offsetofIFD = offsetOfNextIFD;
				mmPerPixelX*=2;
				mmPerPixelY*=2;
			}
			
			ifdos.flush();
			ifdos.close();
			
			{
				Attribute dataSetTrailingPadding = new OtherByteAttribute(TagFromName.DataSetTrailingPadding);
				dataSetTrailingPadding.setValues(ifdaos.toByteArray());
				list.put(dataSetTrailingPadding);
			}
		}
		
		return preamble;
	}

	/**
	 * <p>Read a DICOM image input format file and create a dual-personality DICOM-TIFF file and/or add Basic or Extended Offset Tables.</p>
	 *
	 * <p>Does not change the Transfer Syntax.</p>
	 *
	 * @param	inputFileName
	 * @param	outputFileName
	 * @param	addTIFF		whether or not to add a TIFF IFD in the DICOM preamble to make a dual=personality DICOM-TIFF file sharing the same pixel data
	 * @param	useBigTIFF	whether or not to create a BigTIFF rather than Classic TIFF file
	 * @exception			IOException
	 * @exception			DicomException
	 * @exception			NumberFormatException
	 */
	public AddTIFFOrOffsetTables(String inputFileName,String outputFileName,boolean addTIFF,boolean useBigTIFF)
			throws IOException, DicomException, TIFFException, NumberFormatException {
		AttributeList list = new AttributeList();
		list.setDecompressPixelData(false);
		list.read(inputFileName);
		
		String transferSyntax = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.TransferSyntaxUID);
		if (transferSyntax == null || transferSyntax.length() == 0) {
			throw new DicomException("Missing or empty TransferSyntaxUID in "+inputFileName);
		}

		list.removeGroupLengthAttributes();
		list.removeMetaInformationHeaderAttributes();
		list.remove(TagFromName.DataSetTrailingPadding);	// may be existing if already TIFF or some other reason, and needs to be removed first else screws up lengths

		FileMetaInformation.addFileMetaInformation(list,transferSyntax,"OURAETITLE");	// need to do this before addTIFF

		byte[] preamble = null;
		
		if (addTIFF) {
			long tileWidth = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Columns,0);
			long tileLength = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.Rows,0);
			long bitsPerSample = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsAllocated,0);
			long outputCompression = getTIFFCompressionFromTransferSyntax(transferSyntax);
			long samplesPerPixel = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.SamplesPerPixel,0);
			long planarConfig = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.PlanarConfiguration,0) + 1;	// TIFF is 1 or 2 but sometimes absent (0), DICOM is 0 or 1
			long sampleFormat = 0x0001;	// if used, always unsigned integer data
			byte[] iccProfile = null;
			{
				Attribute aICCProfile = SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(list,TagFromName.OpticalPathSequence,0,TagFromName.ICCProfile);
				if (aICCProfile != null) {
					iccProfile = aICCProfile.getByteValues();
				}
			}
			double mmPerPixelXBaseLayer = 0;
			double mmPerPixelYBaseLayer = 0;
			{
				Attribute aPixelSpacing = list.get(TagFromName.PixelSpacing);
				if (aPixelSpacing == null) {
					SequenceAttribute sharedFunctionalGroupsSequence = (SequenceAttribute)(list.get(TagFromName.SharedFunctionalGroupsSequence));
					if (sharedFunctionalGroupsSequence != null) {
						SequenceAttribute sharedPixelMeasuresSequence = (SequenceAttribute)(SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(
							sharedFunctionalGroupsSequence,TagFromName.PixelMeasuresSequence));
						if (sharedPixelMeasuresSequence != null) {
							aPixelSpacing = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(sharedPixelMeasuresSequence,TagFromName.PixelSpacing);
						}
					}
				}
				if (aPixelSpacing != null) {
					// note that order in DICOM in PixelSpacing is "adjacent row spacing" (Y), then "adjacent column spacing" (X) ...
					double[] pixelSpacing = aPixelSpacing.getDoubleValues();
					if (pixelSpacing != null && pixelSpacing.length == 2) {		// (001327)
						mmPerPixelYBaseLayer = pixelSpacing[0];	// between rows
						mmPerPixelXBaseLayer = pixelSpacing[1];	// between cols
					}
				}
			}

			long outputPhotometric = getTIFFPhotometricFromDICOMPhotometricInterpretation(Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PhotometricInterpretation));
			long lowerPhotometric =	outputPhotometric; // what to use for lower pyramidal levels, since may be compressed differently than base layer
			{
				int numberOfPyramidLevels = 1;
				if (Attribute.getSingleStringValueOrEmptyString(list,pixelmedPrivatePyramidDataBlockReservation).equals(pixelmedPrivateCreatorForPyramidData)) {
					SequenceAttribute pyramidData = (SequenceAttribute)(list.get(pixelmedPrivatePyramidData));
					if (pyramidData != null) {
						numberOfPyramidLevels = pyramidData.getNumberOfItems() + 1;	// i.e., include base layer in PixelData
						if (numberOfPyramidLevels >= 2) {
							Attribute a = SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(pyramidData,0,TagFromName.PhotometricInterpretation);
							if (a != null) {
								String lowerPhotometricInterpretation = a.getSingleStringValueOrDefault("");
								lowerPhotometric = getTIFFPhotometricFromDICOMPhotometricInterpretation(lowerPhotometricInterpretation);
							}
						}
					}
				}
				slf4jlogger.debug("AddTIFFOrOffsetTables(): numberOfPyramidLevels = {}",numberOfPyramidLevels);
				long[][] tileDataByteOffsets = new long[numberOfPyramidLevels][];
				long[][] tileDataLengths = new long[numberOfPyramidLevels][];
				long[] imageWidths = new long[numberOfPyramidLevels];
				long[] imageLengths = new long[numberOfPyramidLevels];
				// numberOfSourceTiles will == number of DICOM tiles for planarConfig 1 (color-by-pixel) and be samplesPerPixel * number of DICOM tiles for planarConfig 2 (color-by-plane) (TIFF6 p 68) (001351)
				boolean mergeSamplesPerPixelTiles = planarConfig == 2 && samplesPerPixel > 1;
				slf4jlogger.debug("AddTIFFOrOffsetTables(): mergeSamplesPerPixelTiles = {}",mergeSamplesPerPixelTiles);
				long  byteOffsetFromFileStartOfNextAttributeAfterPixelData = getByteOffsetsAndLengthsOfTileDataFromStartOfFile(list,transferSyntax,tileDataByteOffsets,tileDataLengths,imageWidths,imageLengths,mergeSamplesPerPixelTiles,samplesPerPixel);
				
				preamble = makeTIFFInPreambleAndAddDataSetTrailingPadding(byteOffsetFromFileStartOfNextAttributeAfterPixelData,numberOfPyramidLevels,tileDataByteOffsets,tileDataLengths,imageWidths,imageLengths,list,
					tileWidth,tileLength,bitsPerSample,outputCompression,outputPhotometric,lowerPhotometric,samplesPerPixel,planarConfig,sampleFormat,iccProfile,mmPerPixelXBaseLayer,mmPerPixelYBaseLayer,useBigTIFF);
			}
		}
		list.write(outputFileName,transferSyntax,true,true,preamble);
	}
	
	/**
	 * <p>Read a DICOM image input format file and create a dual-personality DICOM-TIFF file and/or add Basic or Extended Offset Tables.</p>
	 *
	 * <p>Options are:</p>
	 * <p>ADDTIFF | DONOTADDTIFF (default)</p>
	 * <p>USEBIGTIFF (default) | DONOTUSEBIGTIFF</p>
	 *
	 * @param	arg	two parameters plus options, the DICOM inputFile, the DICOM outputFile, then various options controlling conversion
	 */
	public static void main(String arg[]) {
		try {
			boolean addTIFF = false;
			boolean useBigTIFF = true;
	
			int numberOfFixedArguments = 2;
			int numberOfFixedAndOptionalArguments = 2;
			int endOptionsPosition = arg.length;
			boolean bad = false;
			
			if (endOptionsPosition < numberOfFixedArguments) {
				bad = true;
			}
			boolean keepLooking = true;
			while (keepLooking && endOptionsPosition > numberOfFixedArguments) {
				String option = arg[endOptionsPosition-1].trim().toUpperCase();
				switch (option) {
					case "ADDTIFF":				addTIFF = true; --endOptionsPosition; break;
					case "DONOTADDTIFF":		addTIFF = false; --endOptionsPosition; break;
					
					case "USEBIGTIFF":			useBigTIFF = true; --endOptionsPosition; break;
					case "DONOTUSEBIGTIFF":		useBigTIFF = false; --endOptionsPosition; break;

					default:	if (endOptionsPosition > numberOfFixedAndOptionalArguments) {
									slf4jlogger.error("Unrecognized argument {}",option);
									bad = true;
								}
								keepLooking = false;
								break;
				}
			}
			
			if (!bad) {
				String inputFile = arg[0];
				String outputFile = arg[1];
				
				new AddTIFFOrOffsetTables(inputFile,outputFile,addTIFF,useBigTIFF);
			}
			else {
				System.err.println("Error: Incorrect number of arguments or bad arguments");
				System.err.println("Usage: AddTIFFOrOffsetTables inputFile outputFile"
					+" [ADDTIFF|DONOTADDTIFF]"
					+" [USEBIGTIFF|DONOTUSEBIGTIFF]"
				);
				System.exit(1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}



