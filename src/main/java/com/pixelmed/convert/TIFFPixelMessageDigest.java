/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.convert;

import com.pixelmed.dicom.CompressedFrameDecoder;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.TransferSyntax;

import java.awt.color.ColorSpace;
import java.awt.image.DataBufferByte;
import java.awt.image.BufferedImage;

//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.InputStream;
import java.io.IOException;

//import java.nio.ByteOrder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;

import javax.xml.bind.DatatypeConverter;	// for getting digest as hex string

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class for computing message digests of pixel data, decompressed if compressed, from TIFF files.</p>
 *
 * @author	dclunie
 */

public class TIFFPixelMessageDigest {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/convert/TIFFPixelMessageDigest.java,v 1.5 2022/01/21 19:51:13 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(TIFFPixelMessageDigest.class);

	private static byte[] stripSOIEOIMarkers(byte[] bytes) {
		byte[] newBytes = null;
		int l = bytes.length;
		if (l >= 4
		 && (bytes[0]&0xff) == 0xff
		 && (bytes[1]&0xff) == 0xd8
		 && (bytes[l-2]&0xff) == 0xff
		 && (bytes[l-1]&0xff) == 0xd9) {
			if (l > 4) {
				int newL = l-4;
				newBytes = new byte[newL];
				System.arraycopy(bytes,2,newBytes,0,newL);
			}
			// else leave it null since now empty
		}
		else {
			slf4jlogger.error("stripSOIEOIMarkers(): Unable to remove SOI and EOI markers");
			newBytes = bytes;
		}
		return newBytes;
	}
	
	private static byte[] insertJPEGTablesIntoAbbreviatedBitStream(byte[] bytes,byte[] jpegTables) {
		byte[] newBytes = null;
		int l = bytes.length;
		if (l > 2
		 && (bytes[0]&0xff) == 0xff
		 && (bytes[1]&0xff) == 0xd8) {
			int tableL = jpegTables.length;
			int newL = l + tableL;
			newBytes = new byte[newL];
			System.arraycopy(bytes,     0,newBytes,0,       2);
			System.arraycopy(jpegTables,0,newBytes,2,       tableL);
			System.arraycopy(bytes,     2,newBytes,2+tableL,l-2);
		}
		else {
			slf4jlogger.error("insertJPEGTablesIntoAbbreviatedBitStream(): Unable to insert JPEG Tables");
			newBytes = bytes;
		}
		return newBytes;
	}
	
	// https://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/JPEG.html#Adobe
	
	private static byte[] AdobeAPP14_RGB = {
		(byte)0xFF, (byte)0xEE,
		(byte)0x00, (byte)12,	/* big endian length includes lebgth itself but not the marker */
		(byte)'A', (byte)'d', (byte)'o', (byte)'b', (byte)'e',(byte)0x00,
		(byte)0x00, /* DCTEncodeVersion 0 */
		(byte)0x00, /* APP14Flags0 0 */
		(byte)0x00, /* APP14Flags1 0 */
		(byte)0x00 /* ColorTransform 0 = Unknown (RGB or CMYK) */
	};
	
	private static byte[] insertAdobeAPP14WithRGBTransformIntoBitStream(byte[] bytes) {
		byte[] newBytes = null;
		int l = bytes.length;
		if (l > 2
		 && (bytes[0]&0xff) == 0xff
		 && (bytes[1]&0xff) == 0xd8) {
			int app14L = AdobeAPP14_RGB.length;
			int newL = l + app14L;
			newBytes = new byte[newL];
			System.arraycopy(bytes,         0,newBytes,0,       2);
			System.arraycopy(AdobeAPP14_RGB,0,newBytes,2,       app14L);
			System.arraycopy(bytes,         2,newBytes,2+app14L,l-2);
		}
		else {
			slf4jlogger.error("insertAdobeAPP14WithRGBTransformIntoBitStream(): Unable to insert APP14");
			newBytes = bytes;
		}
		return newBytes;
	}

	/**
	 * <p>Compute a message digest from tiled TIFF pixel data, decompressing it if necessary.</p>
	 *
	 * <p>Inserts factored out JPEG tables to turn abbreviated into interchange format JPEG bitstreams before decompressing.</p>
	 *
	 * @param	inputFile
	 * @param	numberOfTiles
	 * @param	tileOffsets
	 * @param	tileByteCounts
	 * @param	tileWidth
	 * @param	tileLength
	 * @param	bitsPerSample
	 * @param	compression				the compression value in the TIFF source
	 * @param	photometric				the photometric value in the TIFF source
	 * @param	jpegTables				the JPEG tables in the TIFF source to be inserted in to the abbreviated format JPEG stream to make interchange format or before decompression
	 * @return							the byte array value of the computed message digest
	 * @throws	IOException				if there is an error reading or writing
	 * @throws	TIFFException
	 * @throws	NoSuchAlgorithmException
	 * @throws	DicomException
	 */
	private byte[] computePixelMessageDigestFromTiles(TIFFFile inputFile,
				int numberOfTiles,long[] tileOffsets,long[] tileByteCounts,long tileWidth,long tileLength,
				long bitsPerSample,long compression,long photometric,byte[] jpegTables) throws IOException, TIFFException, NoSuchAlgorithmException, DicomException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		if (compression == 0 || compression == 1) {		// absent or specified as uncompressed
			if (bitsPerSample == 8) {
				for (int tileNumber=0; tileNumber<numberOfTiles; ++tileNumber) {
					long pixelOffset = tileOffsets[tileNumber];
					long pixelByteCount = tileByteCounts[tileNumber];
					byte[] values = new byte[(int)pixelByteCount];
					inputFile.seek(pixelOffset);
					inputFile.read(values);
					md.update(values);
				}
			}
			else {
				throw new TIFFException("Unsupported bitsPerSample = "+bitsPerSample+" for compression");
			}
		}
		else if ((compression == 7 || compression == 33005)) {		// "new" JPEG per TTN2 as used by Aperio in SVS, Aperio J2K RGB
			// decompress each frame
			if (bitsPerSample == 8) {
				String transferSyntax = compression == 7 ? TransferSyntax.JPEGBaseline : TransferSyntax.JPEG2000;	// take care to keep this in sync with enclosing test of supported schemes
				CompressedFrameDecoder decoder = new CompressedFrameDecoder(
																			transferSyntax,
																			1/*bytesPerSample*/,
																			(int)tileWidth,(int)tileLength,
																			3/*samples*/,		// hmmm ..../ :(
																			ColorSpace.getInstance(ColorSpace.CS_sRGB),
																			photometric == 6);	// should check for presence of TIFF ICC profile ? :(
				
				for (int tileNumber=0; tileNumber<numberOfTiles; ++tileNumber) {
					long pixelOffset = tileOffsets[tileNumber];
					long pixelByteCount = tileByteCounts[tileNumber];
					if (pixelByteCount > Integer.MAX_VALUE) {
						throw new TIFFException("For frame "+tileNumber+", compressed pixelByteCount to be read "+pixelByteCount+" exceeds maximum Java array size "+Integer.MAX_VALUE+" and fragmentation not yet supported");
					}
					byte[] values = new byte[(int)pixelByteCount];
					inputFile.seek(pixelOffset);
					inputFile.read(values);
					
					if (jpegTables != null) {		// should not be present for 33005
						values = insertJPEGTablesIntoAbbreviatedBitStream(values,jpegTables);
					}
					if (compression == 7/*JPEG*/ && photometric == 2/*RGB*/) {
						slf4jlogger.trace("JPEG RGB so adding APP14");
						values = insertAdobeAPP14WithRGBTransformIntoBitStream(values);
					}
					
					BufferedImage img = decoder.getDecompressedFrameAsBufferedImage(values);					// Hmm :( Do we need to be concerned about color space transformations?
					if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug(com.pixelmed.display.BufferedImageUtilities.describeImage(img));
					byte[] decompressedValues = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();	// Hmm :( Do we need to be concerned about the order of components and interleaving?
					
					md.update(decompressedValues);
				}
			}
			else {
				throw new TIFFException("Unsupported bitsPerSample = "+bitsPerSample+" for compression");
			}
		}
		else {
			throw new TIFFException("Unsupported compression = "+compression);
		}

		return md.digest();
	}

	/**
	 * <p>Read a TIFF image input format file consisting of one or more pages or tiles, and compute message digest of pixel data, decompressed if compressed.</p>
	 *
	 * @param	inputFileName
	 * @exception			IOException
	 * @exception			TIFFException
	 * @exception			NumberFormatException
	 */
	public TIFFPixelMessageDigest(String inputFileName)
			throws IOException, TIFFException, NumberFormatException {
			
		TIFFImageFileDirectories ifds = new TIFFImageFileDirectories();
		ifds.read(inputFileName);
		
		int dirNum = 0;
		ArrayList<TIFFImageFileDirectory> ifdlist = ifds.getListOfImageFileDirectories();
		for (TIFFImageFileDirectory ifd : ifdlist) {
			slf4jlogger.info("Directory={}",dirNum);
		
			// SubFileType (254) LONG (4) 1<0>
			long imageWidth = ifd.getSingleNumericValue(TIFFTags.IMAGEWIDTH,0,0);
			slf4jlogger.debug("imageWidth={}",imageWidth);
			long imageLength = ifd.getSingleNumericValue(TIFFTags.IMAGELENGTH,0,0);
			slf4jlogger.debug("imageLength={}",imageLength);
			long bitsPerSample = ifd.getSingleNumericValue(TIFFTags.BITSPERSAMPLE,0,0);
			slf4jlogger.debug("bitsPerSample={}",bitsPerSample);
			long compression = ifd.getSingleNumericValue(TIFFTags.COMPRESSION,0,0);
			slf4jlogger.debug("compression={}",compression);
			long photometric = ifd.getSingleNumericValue(TIFFTags.PHOTOMETRIC,0,0);
			slf4jlogger.debug("photometric={}",photometric);
			long samplesPerPixel = ifd.getSingleNumericValue(TIFFTags.SAMPLESPERPIXEL,0,0);
			slf4jlogger.debug("samplesPerPixel={}",samplesPerPixel);

			byte[] jpegTables = null;
			if (compression == 7) {
				jpegTables = ifd.getByteValues(TIFFTags.JPEGTABLES);
				if (jpegTables != null) {
					slf4jlogger.debug("jpegTables present");
					jpegTables = stripSOIEOIMarkers(jpegTables);
				}
			}
			
			long tileWidth = ifd.getSingleNumericValue(TIFFTags.TILEWIDTH,0,0);
			slf4jlogger.debug("tileWidth={}",tileWidth);
			long tileLength = ifd.getSingleNumericValue(TIFFTags.TILELENGTH,0,0);
			slf4jlogger.debug("tileLength={}",tileLength);
			
			byte[] digest = null;
			
			try {
				long[] tileOffsets = ifd.getNumericValues(TIFFTags.TILEOFFSETS);
				long[] tileByteCounts = ifd.getNumericValues(TIFFTags.TILEBYTECOUNTS);
				if (tileOffsets != null) {
					int numberOfTiles = tileOffsets.length;
					if (tileByteCounts.length != numberOfTiles) {
						throw new TIFFException("Number of tiles uncertain: tileOffsets length = "+tileOffsets.length+" different from tileByteCounts length "+tileByteCounts.length);
					}
					slf4jlogger.debug("numberOfTiles={}",numberOfTiles);
					digest = computePixelMessageDigestFromTiles(ifds.getFile(),numberOfTiles,tileOffsets,tileByteCounts,tileWidth,tileLength,bitsPerSample,compression,photometric,jpegTables);
				}
				else {
					throw new TIFFException("Unsupported encoding");
				}
			}
			catch (Exception e) {
				slf4jlogger.error("Failed to compute Message Digest: ",e);
			}
			if (digest != null) {
				slf4jlogger.info("Digest={}",DatatypeConverter.printHexBinary(digest).toLowerCase());		// https://www.baeldung.com/java-md5
			}
			else {
				slf4jlogger.error("Failed to compute Message Digest");
			}
			++dirNum;
		}
	}
	
	/**
	 * <p>Read a TIFF image input format file consisting of one or more pages or tiles, and compute message digest of pixel data, decompressed if compressed.</p>
	 *
	 * @param	arg	one parameter, the TIFF inputFile
	 */
	public static void main(String arg[]) {
		try {
			if (arg.length == 1) {
				String inputFile = arg[0];
				
				new TIFFPixelMessageDigest(inputFile);
			}
			else {
				System.err.println("Error: Incorrect number of arguments or bad arguments");
				System.err.println("Usage: TIFFPixelMessageDigest inputFile");
				System.exit(1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

