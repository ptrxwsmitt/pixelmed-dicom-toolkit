/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.display.BufferedImageUtilities;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.spi.*;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>The {@link CompressedFrameDecoder CompressedFrameDecoder} class implements decompression of selected frames
 * in various supported Transfer Syntaxes once already extracted from DICOM encapsulated images.</p>
 *
 *
 * @author	dclunie
 */
public class CompressedFrameDecoder {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/CompressedFrameDecoder.java,v 1.34 2022/01/21 19:51:15 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(CompressedFrameDecoder.class);
	
	private String transferSyntaxUID;
	private byte[][] frames;
	private ByteFrameSource compressedDataFrameSource;
	private int bytesPerSample;
	private int width;		// needed for RLE
	private int height;		// needed for RLE
	private int samples;	// needed for RLE and lossy JPEG if doing own color space conversion
	private ColorSpace colorSpace;
	private boolean photometricInterpretationIsKnown;
	private boolean photometricInterpretationIsYBR;

	private static boolean haveScannedForCodecs;

	public static void scanForCodecs() {
		slf4jlogger.debug("scanForCodecs(): Scanning for ImageIO plugin codecs");
		ImageIO.scanForPlugins();
		ImageIO.setUseCache(false);		// disk caches are too slow :(
		haveScannedForCodecs=true;
	}
	
	//private boolean pixelDataWasLossy = false;			// set if decompressed from lossy transfer syntax during reading of Pixel Data attribute in this AttributeList instance
	//private String lossyMethod = null;
	private IIOMetadata[] iioMetadata = null;			// will be set during compression if reader is capable of it
	private boolean colorSpaceWillBeConvertedToRGBDuringDecompression = false;	// set if color space will be converted to RGB during compression
	
	private String readerWanted;

	private boolean isJPEGFamily = false;
	private boolean isRLE = false;
	
	private ImageReader reader = null;
	
	private Set<String> blacklistedReaders = null;		// (descriptions of) readers that have been tried and have failed (001099)
	
	private int lastFrameDecompressed = -1;
	private IIOMetadata iioMetadataForLastFrameDecompressed = null;
	
	/**
	 * <p>Returns a whether or not a DICOM file contains pixel data that can be decompressed using this class.</p>
	 *
	 * @param	file	the file
	 * @return	true if file can be decompressed using this class
	 */
	public static boolean canDecompress(File file) {
		slf4jlogger.debug("canDecompress(): file "+file);
		boolean canDecompressPixelData = false;
		AttributeList list = new AttributeList();
		try {
			list.readOnlyMetaInformationHeader(file);
			String transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.TransferSyntaxUID);
			slf4jlogger.debug("canDecompress(): transferSyntaxUID {}",transferSyntaxUID);
			if (transferSyntaxUID.equals(TransferSyntax.RLE)) {
				canDecompressPixelData = true;				// (000787)
			}
			else {
				TransferSyntax ts = new TransferSyntax(transferSyntaxUID);
				slf4jlogger.debug("canDecompress(): {}",ts.dump());
				int maximumBytesPerSampleNeededForTransferSyntax = 2;
				if (ts.isJPEGFamily()) {
					slf4jlogger.debug("canDecompress(): transferSyntaxUID is JPEG family");
					String readerWanted = null;
					if (transferSyntaxUID.equals(TransferSyntax.JPEGBaseline)) {
						readerWanted="JPEG";
						maximumBytesPerSampleNeededForTransferSyntax = 1;
					}
					else if (transferSyntaxUID.equals(TransferSyntax.JPEGExtended)) {
						readerWanted="JPEG";
					}
					else if (transferSyntaxUID.equals(TransferSyntax.JPEGFullProgressionNonHierarchical1012)) {
						readerWanted="JPEG";
					}
					else if (transferSyntaxUID.equals(TransferSyntax.JPEG2000) || transferSyntaxUID.equals(TransferSyntax.JPEG2000Lossless)) {
						readerWanted="JPEG2000";
					}
					else if (transferSyntaxUID.equals(TransferSyntax.JPEGLossless) || transferSyntaxUID.equals(TransferSyntax.JPEGLosslessSV1)) {
						readerWanted="jpeg-lossless";
					}
					else if (transferSyntaxUID.equals(TransferSyntax.JPEGLS) || transferSyntaxUID.equals(TransferSyntax.JPEGNLS)) {
						readerWanted="jpeg-ls";
					}
					if (readerWanted != null) {
						slf4jlogger.debug("canDecompress(): readerWanted {}",readerWanted);
						try {
							ImageReader reader = selectReaderFromCodecsAvailable(readerWanted,transferSyntaxUID,maximumBytesPerSampleNeededForTransferSyntax);
							if (reader != null) {
								canDecompressPixelData = true;
							}
						}
						catch (Exception e) {
							slf4jlogger.debug("ignore any exception at this point, since harmless", e);
						}
					}
				}
			}
		}
		catch (DicomException e) {
			slf4jlogger.error("", e);
		}
		catch (IOException e) {
			slf4jlogger.error("", e);
		}
		slf4jlogger.debug("canDecompress(): returns {}",canDecompressPixelData);
		return canDecompressPixelData;
	}
	
	/**
	 * <p>Returns a whether or not a DICOM file contains pixel data that can be decompressed using this class.</p>
	 *
	 * @param	filename	the file
	 * @return	true		if file can be decompressed using this class
	 */
	public static boolean canDecompress(String filename) {
		return canDecompress(new File(filename));
	}
	
	/**
	 * <p>Returns a reference to the {@link IIOMetadata IIOMetadata} object for the selected frame, or null if none was available during reading. </p>
	 *
	 * @param	frame	the frame number, from 0
	 * @return	an {@link IIOMetadata IIOMetadata} object, or null.
	 */
	//public IIOMetadata getIIOMetadata(int frame) {
	//	return frame == lastFrameDecompressed ? iioMetadataForLastFrameDecompressed : null;
	//}

	/**
	 * <p>Returns a whether or not the color space will be converted to RGB during compression if it was YBR in the first place.</p>
	 *
	 * @return	true if RGB after compression
	 */
	public boolean getColorSpaceConvertedToRGBDuringDecompression() {
		return colorSpaceWillBeConvertedToRGBDuringDecompression;
	}
	
	// compare this to AttributeList.extractCompressedPixelDataCharacteristics()
	private void chooseReaderWantedBasedOnTransferSyntax() {
		TransferSyntax ts = new TransferSyntax(transferSyntaxUID);
		isJPEGFamily = ts.isJPEGFamily();
		isRLE = transferSyntaxUID.equals(TransferSyntax.RLE);

		colorSpaceWillBeConvertedToRGBDuringDecompression = false;		// do not set this generally ... be specific to each scheme (00704)
		//pixelDataWasLossy=false;
		//lossyMethod=null;
		readerWanted = null;
		slf4jlogger.debug("chooseReader(): TransferSyntax = {}",transferSyntaxUID);
		if (isRLE) {
			// leave colorSpaceWillBeConvertedToRGBDuringDecompression false;	// (000832)
			slf4jlogger.debug("Undefined length encapsulated Pixel Data in RLE");
		}
		else if (isJPEGFamily) {
			if (transferSyntaxUID.equals(TransferSyntax.JPEGBaseline) || transferSyntaxUID.equals(TransferSyntax.JPEGExtended) || transferSyntaxUID.equals(TransferSyntax.JPEGFullProgressionNonHierarchical1012)) {
				readerWanted="JPEG";
				colorSpaceWillBeConvertedToRGBDuringDecompression = true;
				//pixelDataWasLossy=true;
				//lossyMethod="ISO_10918_1";
				slf4jlogger.debug("chooseReader(): Undefined length encapsulated Pixel Data in JPEG Lossy");
			}
			else if (transferSyntaxUID.equals(TransferSyntax.JPEG2000)) {
				readerWanted="JPEG2000";
				colorSpaceWillBeConvertedToRGBDuringDecompression = true;
				//pixelDataWasLossy=true;
				//lossyMethod="ISO_15444_1";
				slf4jlogger.debug("chooseReader(): Undefined length encapsulated Pixel Data in JPEG 2000");
			}
			else if (transferSyntaxUID.equals(TransferSyntax.JPEG2000Lossless)) {
				readerWanted="JPEG2000";
				colorSpaceWillBeConvertedToRGBDuringDecompression = true;
				slf4jlogger.debug("chooseReader(): Undefined length encapsulated Pixel Data in JPEG 2000");
			}
			else if (transferSyntaxUID.equals(TransferSyntax.JPEGLossless) || transferSyntaxUID.equals(TransferSyntax.JPEGLosslessSV1)) {
				readerWanted="jpeg-lossless";
				colorSpaceWillBeConvertedToRGBDuringDecompression = false;		// NB. (00704)
				slf4jlogger.debug("chooseReader(): Undefined length encapsulated Pixel Data in JPEG Lossless");
			}
			else if (transferSyntaxUID.equals(TransferSyntax.JPEGLS)) {
				readerWanted="jpeg-ls";
				colorSpaceWillBeConvertedToRGBDuringDecompression = false;		// Hmmm :(
				slf4jlogger.debug("chooseReader(): Undefined length encapsulated Pixel Data in JPEG-LS");
			}
			else if (transferSyntaxUID.equals(TransferSyntax.JPEGNLS)) {
				readerWanted="jpeg-ls";
				colorSpaceWillBeConvertedToRGBDuringDecompression = false;		// Hmmm :(
				//pixelDataWasLossy=true;
				//lossyMethod="ISO_14495_1";
				slf4jlogger.debug("chooseReader(): Undefined length encapsulated Pixel Data in JPEG-LS");
			}
			else {
				readerWanted="JPEG";
				colorSpaceWillBeConvertedToRGBDuringDecompression = true;
				slf4jlogger.warn("Unrecognized JPEG family Transfer Syntax {} for encapsulated PixelData - guessing {}",transferSyntaxUID,readerWanted);
			}
		}
		else {
			slf4jlogger.error("Unrecognized Transfer Syntax {} for encapsulated PixelData - cannot find reader",transferSyntaxUID);
		}
		slf4jlogger.debug("chooseReader(): Based on Transfer Syntax, colorSpaceWillBeConvertedToRGBDuringDecompression = {}",colorSpaceWillBeConvertedToRGBDuringDecompression);
	}
	
	public static boolean isStandardJPEGReader(ImageReader reader) {
		return reader.getOriginatingProvider().getDescription(Locale.US).equals("Standard JPEG Image Reader") && (reader.getOriginatingProvider().getVendorName().equals("Sun Microsystems, Inc.") || reader.getOriginatingProvider().getVendorName().equals("Oracle Corporation"));
	}
	
	public static boolean isPixelMedLosslessJPEGReader(ImageReader reader) {
		return reader.getOriginatingProvider().getDescription(Locale.US).equals("PixelMed JPEG Lossless Image Reader");		// cannot reference com.pixelmed.imageio.JPEGLosslessImageReaderSpi.getDescription() because may not be available at compile time
	}
	
	public static ImageReader selectReaderFromCodecsAvailable(String readerWanted,String transferSyntaxUID,int bytesPerSample,Set<String> blacklistedReaders) throws DicomException {
		ImageReader reader = null;
		// Do NOT assume that first reader found is the best one ... check them all and make explicit choices ...
		// Cannot assume that they are returned in any particular order ...
		// Cannot assume that there are only two that match ...
		// Cannot assume that all of them are available on any platform or configuration ...
		//try {
		Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(readerWanted);
		while (it.hasNext()) {
			ImageReader nextReader = it.next();
			String nextReaderDescription = nextReader.getOriginatingProvider().getDescription(Locale.US);	// cannot use reader itself for comparison with blacklistedReaders, since different instance each time
			if (blacklistedReaders != null && blacklistedReaders.contains(nextReaderDescription)) {
				slf4jlogger.debug("selectReaderFromCodecsAvailable(): Skipping blacklisted reader {}",nextReaderDescription);
				continue;	// ignore it completely since already tried it and it failed (001099)
			}
			if (reader == null) {
				reader = nextReader;
				slf4jlogger.debug("selectReaderFromCodecsAvailable(): First reader found is {} {} {}",reader.getOriginatingProvider().getDescription(Locale.US),reader.getOriginatingProvider().getVendorName(),reader.getOriginatingProvider().getVersion());
			}
			else {
				ImageReader otherReader = nextReader;
				slf4jlogger.debug("selectReaderFromCodecsAvailable(): Found another reader {} {} {}",otherReader.getOriginatingProvider().getDescription(Locale.US),otherReader.getOriginatingProvider().getVendorName(),otherReader.getOriginatingProvider().getVersion());
				
				if (isStandardJPEGReader(reader)) {
					// prefer any other reader to the standard one, since the standard one is limited, and any other is most likely JAI JIIO
					reader = otherReader;
					slf4jlogger.debug("selectReaderFromCodecsAvailable(): Choosing reader {} {} {} over Standard JPEG Image Reader",reader.getOriginatingProvider().getDescription(Locale.US),reader.getOriginatingProvider().getVendorName(),reader.getOriginatingProvider().getVersion());
				}
				else if (isPixelMedLosslessJPEGReader(reader)) {
					slf4jlogger.debug("selectReaderFromCodecsAvailable(): Choosing reader {} {} {} over any other reader",reader.getOriginatingProvider().getDescription(Locale.US),reader.getOriginatingProvider().getVendorName(),reader.getOriginatingProvider().getVersion());
					break;
				}
				else if (isPixelMedLosslessJPEGReader(otherReader)) {
					reader = otherReader;
					slf4jlogger.debug("selectReaderFromCodecsAvailable(): Choosing reader {} {} {} over any other reader",reader.getOriginatingProvider().getDescription(Locale.US),reader.getOriginatingProvider().getVendorName(),reader.getOriginatingProvider().getVersion());
					break;
				}
			}
		}
		if (reader != null) {
			// The JAI JIIO JPEG reader is OK since it handles both 8 and 12 bit JPEGExtended, but the "standard" reader that comes with the JRE only supports 8 bit
			// Arguably 8 bits in JPEGExtended is not valid (PS3.5 10.2) but since it is sometimes encountered, deal with it if we can, else throw specific exception ...
			if (transferSyntaxUID.equals(TransferSyntax.JPEGExtended) && bytesPerSample > 1 && reader.getOriginatingProvider().getDescription(Locale.US).equals("Standard JPEG Image Reader") && (reader.getOriginatingProvider().getVendorName().equals("Sun Microsystems, Inc.") || reader.getOriginatingProvider().getVendorName().equals("Oracle Corporation"))) {
				throw new DicomException("Reader "+reader.getOriginatingProvider().getDescription(Locale.US)+" "+reader.getOriginatingProvider().getVendorName()+" "+reader.getOriginatingProvider().getVersion()+" does not support extended lossy JPEG Transfer Syntax "+transferSyntaxUID+" other than for 8 bit data");
			}
			slf4jlogger.debug("selectReaderFromCodecsAvailable(): Using reader {} {} {}",reader.getOriginatingProvider().getDescription(Locale.US),reader.getOriginatingProvider().getVendorName(),reader.getOriginatingProvider().getVersion());
		}
		else {
			//CapabilitiesAvailable.dumpListOfAllAvailableReaders(System.err);
			throw new DicomException("No reader for "+readerWanted+" available for Transfer Syntax "+transferSyntaxUID);
		}
		//}
		//catch (Exception e) {
		//	slf4jlogger.error("", e);
		//	CapabilitiesAvailable.dumpListOfAllAvailableReaders(System.err);
		//	throw new DicomException("No reader for "+readerWanted+" available for Transfer Syntax "+transferSyntaxUID+"\nCaused by: "+e);
		//}
		return reader;
	}

	public static ImageReader selectReaderFromCodecsAvailable(String readerWanted,String transferSyntaxUID,int bytesPerSample) throws DicomException {
		return selectReaderFromCodecsAvailable(readerWanted,transferSyntaxUID,bytesPerSample,null/*blacklistedReaders*/);
	}

	public CompressedFrameDecoder(String transferSyntaxUID,int bytesPerSample,int width,int height,int samples,ColorSpace colorSpace) throws DicomException {
		// no source or frames ... will use methods that supply byte[] frameBytes on each invocation
		doCommonConstructorStuff(transferSyntaxUID,bytesPerSample,width,height,samples,colorSpace);
	}

	public CompressedFrameDecoder(String transferSyntaxUID,int bytesPerSample,int width,int height,int samples,ColorSpace colorSpace,boolean photometricInterpretationIsYBR) throws DicomException {
		// no source or frames ... will use methods that supply byte[] frameBytes on each invocation
		doCommonConstructorStuff(transferSyntaxUID,bytesPerSample,width,height,samples,colorSpace,true/*photometricInterpretationIsKnown*/,photometricInterpretationIsYBR);
	}

	public CompressedFrameDecoder(String transferSyntaxUID,ByteFrameSource compressedDataFrameSource,int bytesPerSample,int width,int height,int samples,ColorSpace colorSpace) throws DicomException {
		if (compressedDataFrameSource == null)  {
			throw new DicomException("no compressed frame source supplied to decompress");
		}
		this.compressedDataFrameSource = compressedDataFrameSource;
		doCommonConstructorStuff(transferSyntaxUID,bytesPerSample,width,height,samples,colorSpace);
	}

	public CompressedFrameDecoder(String transferSyntaxUID,ByteFrameSource compressedDataFrameSource,int bytesPerSample,int width,int height,int samples,ColorSpace colorSpace,boolean photometricInterpretationIsYBR) throws DicomException {
		if (compressedDataFrameSource == null)  {
			throw new DicomException("no compressed frame source supplied to decompress");
		}
		this.compressedDataFrameSource = compressedDataFrameSource;
		doCommonConstructorStuff(transferSyntaxUID,bytesPerSample,width,height,samples,colorSpace,true/*photometricInterpretationIsKnown*/,photometricInterpretationIsYBR);
	}

	public CompressedFrameDecoder(String transferSyntaxUID,byte[][] frames,int bytesPerSample,int width,int height,int samples,ColorSpace colorSpace) throws DicomException {
		if (frames == null)  {
			throw new DicomException("no array of compressed data per frame supplied to decompress");
		}
		this.frames = frames;
		doCommonConstructorStuff(transferSyntaxUID,bytesPerSample,width,height,samples,colorSpace);
	}

	public CompressedFrameDecoder(String transferSyntaxUID,byte[][] frames,int bytesPerSample,int width,int height,int samples,ColorSpace colorSpace,boolean photometricInterpretationIsYBR) throws DicomException {
		if (frames == null)  {
			throw new DicomException("no array of compressed data per frame supplied to decompress");
		}
		this.frames = frames;
		doCommonConstructorStuff(transferSyntaxUID,bytesPerSample,width,height,samples,colorSpace,true/*photometricInterpretationIsKnown*/,photometricInterpretationIsYBR);
	}

	protected void doCommonConstructorStuff(String transferSyntaxUID,int bytesPerSample,int width,int height,int samples,ColorSpace colorSpace) throws DicomException {
		doCommonConstructorStuff(transferSyntaxUID,bytesPerSample,width,height,samples,colorSpace,false/*photometricInterpretationIsKnown*/,false/*photometricInterpretationIsYBR*/);
	}
	
	protected void doCommonConstructorStuff(String transferSyntaxUID,int bytesPerSample,int width,int height,int samples,ColorSpace colorSpace,
			boolean photometricInterpretationIsKnown,boolean photometricInterpretationIsYBR) throws DicomException {
		this.transferSyntaxUID = transferSyntaxUID;
		slf4jlogger.debug("CompressedFrameDecoder(): transferSyntaxUID = {}",transferSyntaxUID);
		this.bytesPerSample = bytesPerSample;
		this.width = width;
		this.height = height;
		this.samples = samples;
		this.colorSpace = colorSpace;
		this.photometricInterpretationIsKnown = photometricInterpretationIsKnown;
		this.photometricInterpretationIsYBR = photometricInterpretationIsYBR;

		scanForCodecs();
		
		chooseReaderWantedBasedOnTransferSyntax();
		slf4jlogger.debug("CompressedFrameDecoder(): Based on Transfer Syntax, colorSpaceWillBeConvertedToRGBDuringDecompression = {}",colorSpaceWillBeConvertedToRGBDuringDecompression);
		if (readerWanted != null) {
			reader = selectReaderFromCodecsAvailable(readerWanted,transferSyntaxUID,bytesPerSample);
		}
		else if (!isRLE) {
			slf4jlogger.debug("CompressedFrameDecoder(): Unrecognized Transfer Syntax {} for encapsulated PixelData",transferSyntaxUID);
			throw new DicomException("Unrecognized Transfer Syntax "+transferSyntaxUID+" for encapsulated PixelData");
		}
	}

	public BufferedImage getDecompressedFrameAsBufferedImage(int f) throws DicomException, IOException {
		slf4jlogger.debug("getDecompressedFrameAsBufferedImage(): Starting frame {}",f);
		byte[] frameBytes = null;
		if (frames != null) {
			frameBytes = frames[f];
		}
		else {
			frameBytes = compressedDataFrameSource.getByteValuesForSelectedFrame(f);
		}
		return getDecompressedFrameAsBufferedImage(frameBytes);
	}
	
	public BufferedImage getDecompressedFrameAsBufferedImage(byte[] frameBytes) throws DicomException, IOException {
		BufferedImage image = null;
		if (isRLE) {
			image = getDecompressedFrameAsBufferedImageUsingRLE(frameBytes);
		}
		else {
			while (image == null && reader != null) {
				try {
					image = getDecompressedFrameAsBufferedImageUsingImageReader(frameBytes);
					slf4jlogger.debug("getDecompressedFrameAsBufferedImage(): on return from getDecompressedFrameAsBufferedImageUsingImageReader, image is {}",image);
				}
				catch (Exception e) {
					slf4jlogger.error("Failed to read frame using selected reader: ",e);
					if (blacklistedReaders == null) {
						blacklistedReaders = new HashSet<String>();
					}
					String readerDescription = reader.getOriginatingProvider().getDescription(Locale.US);
					blacklistedReaders.add(readerDescription);
					slf4jlogger.debug("getDecompressedFrameAsBufferedImage(): Adding reader to blacklist {}",readerDescription);

					try {
						reader = null;	// in case exception, so that loop will terminate (001138)
						reader = selectReaderFromCodecsAvailable(readerWanted,transferSyntaxUID,bytesPerSample,blacklistedReaders);
						slf4jlogger.debug("getDecompressedFrameAsBufferedImage(): Next reader to try is {}",reader);
					}
					catch (Exception e2) {
						slf4jlogger.debug("getDecompressedFrameAsBufferedImage(): Exception selecting reader: ",e2);
						slf4jlogger.debug("getDecompressedFrameAsBufferedImage(): reader after exception is {}",reader);
						if (readerWanted.equals("jpeg-lossless")) {
							slf4jlogger.debug("getDecompressedFrameAsBufferedImage(): No more readers for jpeg-lossless, so try ordinary jpeg");
							readerWanted = "jpeg";		// just in case compressed bitstream is not really lossless, try any codec that can handle JPEG (001099)
							try {
								reader = null;	// in case exception, so that loop will terminate
								reader = selectReaderFromCodecsAvailable(readerWanted,transferSyntaxUID,bytesPerSample,blacklistedReaders);
							}
							catch (Exception e3) {
								slf4jlogger.debug("getDecompressedFrameAsBufferedImage(): Exception selecting reader to try jpeg after jpeg-lossless: ",e3);
							}
						}
						else {
							slf4jlogger.debug("getDecompressedFrameAsBufferedImage(): No more readers to try and reader wanted is not jpeg-lossless");
						}
					}
					slf4jlogger.debug("getDecompressedFrameAsBufferedImage(): reader after processing reader failure exception is {}",reader);
					if (reader == null) {
						throw new DicomException("No more readers to try");
					}
				}
				slf4jlogger.debug("getDecompressedFrameAsBufferedImage(): reader before looping is {}",reader);
			}
		}
		if (image == null) {
			throw new DicomException("Unable to read image - no more readers to try");
		}
		return image;
	}
	
	protected class ByteArrayInputStreamWithOffsetCounterAndOurMethods extends InputStream {
		protected byte[] buf;
		protected int pos;
		protected int count;
		
		public ByteArrayInputStreamWithOffsetCounterAndOurMethods(byte[] buf) {
			super();
			this.buf = buf;
			pos = 0;
			count = buf.length;
		}
		
		public int read() {
			int i = -1;
			if (pos < count) {
				i = ((int)buf[pos++]) & 0xff;
			}
			return i;
		}
		
		public int read(byte[] b,int off,int len) {
			int remaining = count - pos;
			if (remaining < 0) {
				len = -1;
			}
			else {
				if (len > remaining) {
					len = remaining;
				}
				System.arraycopy(buf,pos,b,off,len);
				pos+=len;
			}
			return len;
		}
		
		public long skip(long n) {
			long remaining = (long)count - pos;
			if (remaining < 0) {
				n = 0;
			}
			else if (n > remaining) {
				n = remaining;
			}
			pos = pos + (int)n;
			return n;
		}
		
		public int available() {
			return count - pos;
		}
		
		public boolean markSupported() {
			return false;
		}
		
		// these are copied from EncapsulatedInputStream ...
		
		public final long readUnsigned32LittleEndian() {
			byte  b[] = new byte[4];
			read(b,0,4);
			long v1 =  ((long)b[0])&0xff;
			long v2 =  ((long)b[1])&0xff;
			long v3 =  ((long)b[2])&0xff;
			long v4 =  ((long)b[3])&0xff;
			return (((((v4 << 8) | v3) << 8) | v2) << 8) | v1;
		}

		public final void readUnsigned32LittleEndian(long[] w,int offset,int len) throws IOException {
			int blen = len*4;
			byte  b[] = new byte[blen];
			read(b,0,blen);
			int bcount=0;
			int wcount=0;
			{
				for (;wcount<len;++wcount) {
					long v1 =  ((long)b[bcount++])&0xff;
					long v2 =  ((long)b[bcount++])&0xff;
					long v3 =  ((long)b[bcount++])&0xff;
					long v4 =  ((long)b[bcount++])&0xff;
					w[offset+wcount]=(((((v4 << 8) | v3) << 8) | v2) << 8) | v1;
				}
			}
		}

		public void skipInsistently(long length) throws IOException {
			long remaining = length;
			while (remaining > 0) {
//System.err.println("skipInsistently(): looping remaining="+remaining);
				long bytesSkipped = skip(remaining);
//System.err.println("skipInsistently(): asked for ="+remaining+" got="+bytesSkipped);
				if (bytesSkipped <= 0) throw new IOException("skip failed with "+remaining+" bytes remaining to be skipped, wanted "+length);
				remaining-=bytesSkipped;
			}
		}
		
		public int getOffsetOfNextByteToReadFromStartOfFragment() {
			return pos;
		}

	}
	
	public BufferedImage getDecompressedFrameAsBufferedImageUsingRLE(int f) throws DicomException, IOException {
		slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingRLE(): Starting frame {}",f);
		byte[] frameBytes = null;
		if (frames != null) {
			frameBytes = frames[f];
		}
		else {
			frameBytes = compressedDataFrameSource.getByteValuesForSelectedFrame(f);
		}
		return getDecompressedFrameAsBufferedImageUsingRLE(frameBytes);
	}
	
	public BufferedImage getDecompressedFrameAsBufferedImageUsingRLE(byte[] frameBytes) throws DicomException, IOException {		// (000787)
		BufferedImage image = null;
		ByteArrayInputStreamWithOffsetCounterAndOurMethods bi = new ByteArrayInputStreamWithOffsetCounterAndOurMethods(frameBytes);

		// copied from AttributeList.read() ... should refactor and share code except that input is ByteArrayInputStreamWithOffsetCounterAndOurMethods not EncapsulatedInputStream, and output is BufferedImage not Attribute :(
		int pixelsPerFrame = height*width;
		if (bytesPerSample == 1) {
			slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingRLE(): bytesPerSample = 1");
			slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingRLE(): pixelsPerFrame = {}",pixelsPerFrame);
			byte[] bytePixelData = new byte[pixelsPerFrame*samples];
			{
				// The RLE "header" consists of 16 long values
				// the 1st value is the number of segments
				// the remainder are the byte offsets of each of up to 15 segments
				int numberofSegments = (int)(bi.readUnsigned32LittleEndian());
				slf4jlogger.trace("getDecompressedFrameAsBufferedImageUsingRLE(): Number of segments = {}",numberofSegments);
				long[] segmentOffsets = new long[15];
				bi.readUnsigned32LittleEndian(segmentOffsets,0,15);
				for (int soi=0; soi<15; ++soi) {
					if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("getDecompressedFrameAsBufferedImageUsingRLE(): Segment [{}] offset = {}",soi,segmentOffsets[soi]);
					if (segmentOffsets[soi]%2 != 0) {
						System.err.println("Error: fragment offset for segment "+soi+" is not even length ("+segmentOffsets[soi]+") but ignoring and using odd offset anyway");
					}
				}
				// does not matter whether DICOM AttributeList contained PixelRepresentation that was color-by-plane or -pixel, since RLE is always by plane and we just make that kind of BufferedImage */
				{
					for (int s=0; s < samples; ++s) {
						slf4jlogger.trace("getDecompressedFrameAsBufferedImageUsingRLE(): Doing sample = {}",s);
						int currentOffset = bi.getOffsetOfNextByteToReadFromStartOfFragment();
						slf4jlogger.trace("getDecompressedFrameAsBufferedImageUsingRLE(): At fragment offset {}",currentOffset);
						int bytesToSkipToStartOfSegment = (int)(segmentOffsets[s]) - currentOffset;
						if (bytesToSkipToStartOfSegment > 0) {
							bi.skipInsistently(bytesToSkipToStartOfSegment);
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("getDecompressedFrameAsBufferedImageUsingRLE(): Skipped {} to segment offset {}",bytesToSkipToStartOfSegment,segmentOffsets[s]);
						}
						else if (bytesToSkipToStartOfSegment < 0) {
							throw new DicomException("Already read past start of next segment "+s+" - at "+currentOffset+" need to be at "+segmentOffsets[s]);
						}
						if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("getDecompressedFrameAsBufferedImageUsingRLE(): Now at fragment offset {}",bi.getOffsetOfNextByteToReadFromStartOfFragment());
						// else right on already
						int got = UnPackBits.decode(bi,bytePixelData,pixelsPerFrame*s/*offset*/,pixelsPerFrame);	// entire planes of samples
						slf4jlogger.trace("getDecompressedFrameAsBufferedImageUsingRLE(): got = {} pixels",got);
					}
				}
				if (samples == 1) {
					ComponentColorModel cm=new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
																   new int[] {8},
																   false,		// has alpha
																   false,		// alpha premultipled
																   Transparency.OPAQUE,
																   DataBuffer.TYPE_BYTE
																   );
					
					ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_BYTE,
																	   width,
																	   height,
																	   1,
																	   width,
																	   new int[] {0}
																	   );
					
					DataBuffer buf = new DataBufferByte(bytePixelData,width,0);
					WritableRaster wr = Raster.createWritableRaster(sm,buf,new Point(0,0));
					image = new BufferedImage(cm,wr,true,null);	// no properties hash table
				}
				else if (samples == 3) {
					ComponentColorModel cm=new ComponentColorModel(colorSpace,
																   new int[] {8,8,8},
																   false,		// has alpha
																   false,		// alpha premultipled
																   Transparency.OPAQUE,
																   DataBuffer.TYPE_BYTE
																   );
					
					ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_BYTE,
																	   width,
																	   height,
																	   1,
																	   width,
																	   new int[] {0,pixelsPerFrame,pixelsPerFrame*2}
																	   );
					
					DataBuffer buf = new DataBufferByte(bytePixelData,width,0);
					WritableRaster wr = Raster.createWritableRaster(sm,buf,new Point(0,0));
					image = new BufferedImage(cm,wr,true,null);	// no properties hash table
				}
				else {
					throw new DicomException("Creation of BufferedImage for RLE compressed frame of more samples other than 1 or 3 not supported yet (got "+samples+")");
				}
			}
		}
		else if (bytesPerSample == 2) {
			// for each frame, have to read all high bytes first for a sample, then low bytes :(
			slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingRLE(): bytesPerSample = 2");
			slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingRLE(): pixelsPerFrame = {}",pixelsPerFrame);
			short[] shortPixelData = new short[pixelsPerFrame*samples];
			{
				// The RLE "header" consists of 16 long values
				// the 1st value is the number of segments
				// the remainder are the byte offsets of each of up to 15 segments
				int numberofSegments = (int)(bi.readUnsigned32LittleEndian());
				slf4jlogger.trace("getDecompressedFrameAsBufferedImageUsingRLE(): Number of segments = {}",numberofSegments);
				long[] segmentOffsets = new long[15];
				bi.readUnsigned32LittleEndian(segmentOffsets,0,15);
				for (int soi=0; soi<15; ++soi) {
					if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("getDecompressedFrameAsBufferedImageUsingRLE(): Segment [{}] offset = {}",soi,segmentOffsets[soi]);
					if (segmentOffsets[soi]%2 != 0) {
						System.err.println("Error: fragment offset for segment "+soi+" is not even length ("+segmentOffsets[soi]+") but ignoring and using odd offset anyway");
					}
				}
				// does not matter whether DICOM AttributeList contained PixelRepresentation that was color-by-plane or -pixel, since RLE is always by plane and we just make that kind of BufferedImage */
				{
					int sampleOffset = 0;
					int segment = 0;
					for (int s=0; s < samples; ++s) {
						slf4jlogger.trace("Doing sample = {}",s);
						slf4jlogger.trace("Doing firstsegment");
						byte[] firstsegment = new byte[pixelsPerFrame];
						{
							int currentOffset = bi.getOffsetOfNextByteToReadFromStartOfFragment();
							slf4jlogger.trace("At fragment offset {}",currentOffset);
							int bytesToSkipToStartOfSegment = (int)(segmentOffsets[segment]) - currentOffset;
							if (bytesToSkipToStartOfSegment > 0) {
								bi.skipInsistently(bytesToSkipToStartOfSegment);
								if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Skipped {} to segment offset {}",bytesToSkipToStartOfSegment,segmentOffsets[s]);
							}
							else if (bytesToSkipToStartOfSegment < 0) {
								throw new DicomException("Already read past start of next segment "+s+" - at "+currentOffset+" need to be at "+segmentOffsets[s]);
							}
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Now at fragment offset {}",bi.getOffsetOfNextByteToReadFromStartOfFragment());
							// else right on already
						}
						int got = UnPackBits.decode(bi,firstsegment,pixelsPerFrame*s/*offset*/,pixelsPerFrame);	// entire planes of samples
						slf4jlogger.trace("got = {} bytes for first segment",got);
						slf4jlogger.trace("Doing secondsegment");
						++segment;
						byte[] secondsegment = new byte[pixelsPerFrame];
						{
							int currentOffset = bi.getOffsetOfNextByteToReadFromStartOfFragment();
							slf4jlogger.trace("At fragment offset {}",currentOffset);
							int bytesToSkipToStartOfSegment = (int)(segmentOffsets[segment]) - currentOffset;
							if (bytesToSkipToStartOfSegment > 0) {
								bi.skipInsistently(bytesToSkipToStartOfSegment);
								if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Skipped {} to segment offset {}",bytesToSkipToStartOfSegment,segmentOffsets[s]);
							}
							else if (bytesToSkipToStartOfSegment < 0) {
								throw new DicomException("Already read past start of next segment "+s+" - at "+currentOffset+" need to be at "+segmentOffsets[s]);
							}
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Now at fragment offset {}",bi.getOffsetOfNextByteToReadFromStartOfFragment());
							// else right on already
						}
						got = UnPackBits.decode(bi,secondsegment,pixelsPerFrame*s/*offset*/,pixelsPerFrame);	// entire planes of samples
						slf4jlogger.trace("got = {} bytes for second segment",got);
						for (int p=0; p<pixelsPerFrame; ++p) {
							shortPixelData[sampleOffset + p] = (short)( ((firstsegment[p]&0xff) << 8) + (secondsegment[p]&0xff));
						}
						sampleOffset+=pixelsPerFrame;
						++segment;
					}
				}
				if (samples == 1) {
					ComponentColorModel cm=new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY),
																   new int[] {16},
																   false,		// has alpha
																   false,		// alpha premultipled
																   Transparency.OPAQUE,
																   DataBuffer.TYPE_USHORT
																   );
					
					ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_USHORT,
																	   width,
																	   height,
																	   1,
																	   width,
																	   new int[] {0}
																	   );
					
					DataBuffer buf = new DataBufferUShort(shortPixelData,width,0);
					WritableRaster wr = Raster.createWritableRaster(sm,buf,new Point(0,0));
					image = new BufferedImage(cm,wr,true,null);	// no properties hash table
				}
				else if (samples == 3) {
					ComponentColorModel cm=new ComponentColorModel(colorSpace,
																   new int[] {16,16,16},
																   false,		// has alpha
																   false,		// alpha premultipled
																   Transparency.OPAQUE,
																   DataBuffer.TYPE_USHORT
																   );
					
					ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_USHORT,
																	   width,
																	   height,
																	   1,
																	   width,
																	   new int[] {0,pixelsPerFrame,pixelsPerFrame*2}
																	   );
					
					DataBuffer buf = new DataBufferUShort(shortPixelData,width,0);
					WritableRaster wr = Raster.createWritableRaster(sm,buf,new Point(0,0));
					image = new BufferedImage(cm,wr,true,null);	// no properties hash table
				}
				else {
					throw new DicomException("Creation of BufferedImage for RLE compressed frame of more samples other than 1 or 3 not supported yet (got "+samples+")");
				}
			}
		}
		else {
			throw new DicomException("RLE of more than 2 bytes per sample not supported (got "+bytesPerSample+")");
		}
		return image;
	}

	/**
	 * @param	node
	 * @param	indent
	 */
	private static String toString(Node node,int indent) {
		StringBuffer str = new StringBuffer();
		for (int i=0; i<indent; ++i) str.append("    ");
		//str.append(node);
		str.append(node.getNodeName());
		str.append(" = ");
		str.append(node.getNodeValue());
		if (node.hasAttributes()) {
			str.append(" Attributes: ");
			NamedNodeMap attrs = node.getAttributes();
			for (int j=0; j<attrs.getLength(); ++j) {
				Node attr = attrs.item(j);
				//str.append(toString(attr,indent+2));
				str.append("; ");
				//str.append(attr);
				str.append(toString(attr));
			}
		}
		str.append("\n");
		++indent;
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			str.append(toString(child,indent));
			//str.append("\n");
		}
		return str.toString();
	}
	
	/**
	 * @param	node
	 */
	private static String toString(Node node) {
		return toString(node,0);
	}

	protected boolean haveProcessedMetaDataForFrameRequest = false;
	protected boolean haveJFIF;
	protected boolean haveAdobe;
	protected boolean areDownsampled;
	protected boolean areNumberedFromOneByOne;

	public BufferedImage getDecompressedFrameAsBufferedImageUsingImageReader(int f) throws DicomException, IOException {
		slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): Starting frame {}",f);
		byte[] frameBytes = null;
		if (frames != null) {
			slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): using frames already in memory");
			frameBytes = frames[f];
		}
		else {
			slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): using frames from disk");
			frameBytes = compressedDataFrameSource.getByteValuesForSelectedFrame(f);
		}
		BufferedImage image = getDecompressedFrameAsBufferedImageUsingImageReader(frameBytes);
		lastFrameDecompressed = f;
		return image;
	}
	
	public BufferedImage getDecompressedFrameAsBufferedImageUsingImageReader(byte[] frameBytes) throws DicomException, IOException {
		BufferedImage image = null;
		if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("getDecompressedFrameAsBufferedImageUsingImageReader() frame bytes =\n{}",com.pixelmed.utils.HexDump.dump(frameBytes));
		ImageInputStream iiois = ImageIO.createImageInputStream(new ByteArrayInputStream(frameBytes));
		reader.setInput(iiois,true/*seekForwardOnly*/,true/*ignoreMetadata*/);
		
		slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): Calling reader");
		//IIOImage iioImage = null;		// (000911) don't use this until Oracle fixes bug in readAll()
		try {
			//iioImage = reader.readAll(0,null/*ImageReadParam*/);

			if (samples == 3 && transferSyntaxUID.equals(TransferSyntax.JPEGBaseline)
			 && (reader.getOriginatingProvider().getVendorName().equals("Sun Microsystems, Inc.") || reader.getOriginatingProvider().getVendorName().equals("Oracle Corporation"))) {
				
				slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): checking if we need to handle JPEG YCbCr conversion ourselves");

				// JRE has strange rules about JPEG color space conversion "https://docs.oracle.com/javase/8/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html#color"
				// and assumes RGB if no JFIF or Adobe APPE, components not numbered from 1 and channels not downsampled, which fails when they really are YCbCr :(
				// so do it ourselves if we absolutely have to, i.e., cannot trust the codec (001041) (BEFORE applying any ICC profile)
				// JRE codecs do OK though if they really are RGB components and this is signalled in an Adobe APPE segment as no color transform
				// (and it would be a pain to check that ourselves and not do the convertYBRToRGB for Adobe no transform

				//{
				//	ImageTypeSpecifier its = reader.getRawImageType(0);
				//	slf4jlogger.info("getDecompressedFrameAsBufferedImageUsingImageReader(): Raw ImageTypeSpecifier = {}",its.toString());
				//	ColorModel cm = its.getColorModel();
				//	slf4jlogger.info("getDecompressedFrameAsBufferedImageUsingImageReader(): Raw ImageTypeSpecifier ColorModel = {}",BufferedImageUtilities.describeColorModel(cm));
				//}
				
				// do this once for each instantiation of CompressedFrameDecoder and assume that it is the same for all frames - circumvents failure on repeated calls to reader.getImageMetadata(0) ()
				if (!haveProcessedMetaDataForFrameRequest) {
					slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): haveProcessedMetaDataForFrameRequest is false");
					haveProcessedMetaDataForFrameRequest = true;

					haveJFIF = false;
					haveAdobe = false;
					areDownsampled = false;
					areNumberedFromOneByOne = true;
					
					try {
		
					IIOMetadata	metadata = reader.getImageMetadata(0);
				
					if (slf4jlogger.isDebugEnabled()) {
					//if (true) {
						//slf4jlogger.info("getDecompressedFrameAsBufferedImageUsingImageReader(): metadata = {}",toString(metadata.getAsTree(metadata.getNativeMetadataFormatName())));
						for (String name : metadata.getMetadataFormatNames()) {
							slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): format {} metadata = {}",name,toString(metadata.getAsTree(name)));
						}
					}
				
					IIOMetadataNode tree = (IIOMetadataNode)(metadata.getAsTree("javax_imageio_jpeg_image_1.0"));
				
					 haveJFIF = tree.getElementsByTagName("app0JFIF").getLength() > 0;
					haveAdobe = tree.getElementsByTagName("app14Adobe").getLength() > 0;
					IIOMetadataNode sof = (IIOMetadataNode)(tree.getElementsByTagName("sof").item(0));
					NodeList components = sof.getElementsByTagName("componentSpec");
					for (int c=0; c<components.getLength(); ++c) {
						IIOMetadataNode componentNode = (IIOMetadataNode)(components.item(c));
						{
							int cnum = Integer.parseInt(componentNode.getAttribute("componentId"));
							if (cnum != (c+1)) {
								areNumberedFromOneByOne = false;
							}
						}
						{
							int hSamplingFactor = Integer.parseInt(componentNode.getAttribute("HsamplingFactor"));
							int vSamplingFactor = Integer.parseInt(componentNode.getAttribute("VsamplingFactor"));
							if (hSamplingFactor != 1 || vSamplingFactor != 1) {
								areDownsampled = true;
							}
						}

					}

					if (slf4jlogger.isDebugEnabled() && haveAdobe) {
						try {
							IIOMetadataNode app14Adobe = (IIOMetadataNode)(tree.getElementsByTagName("app14Adobe").item(0));
							int transform = Integer.parseInt(app14Adobe.getAttribute("transform"));
							slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): app14Adobe transform = {}",transform);
						}
						catch (Exception e) {
							slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): Exception getting transform from app14Adobe marker segment",e);
						}
					}
					}
					catch (Exception e) {
						slf4jlogger.error("getDecompressedFrameAsBufferedImageUsingImageReader(): Failed to determine whether or not to handle JPEG YCbCr conversion ourselves from IIOMetadata",e);
					}
				}
				slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): haveJFIF = {}",haveJFIF);
				slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): haveAdobe = {}",haveAdobe);
				slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): areDownsampled = {}",areDownsampled);
				slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): areNumberedFromOneByOne = {}",areNumberedFromOneByOne);
				slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): photometricInterpretationIsKnown = {}",photometricInterpretationIsKnown);
				slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): photometricInterpretationIsYBR = {}",photometricInterpretationIsYBR);

				if (!haveJFIF && !haveAdobe && !areDownsampled && !areNumberedFromOneByOne && (!photometricInterpretationIsKnown || photometricInterpretationIsYBR)) {
					slf4jlogger.info("getDecompressedFrameAsBufferedImageUsingImageReader(): Trying to handle JPEG YCbCr conversion ourselves because no JFIF/Adobe APP and not downsampled and components numbered unconventionally");
					if (reader.canReadRaster()) {
						slf4jlogger.info("getDecompressedFrameAsBufferedImageUsingImageReader(): Handle JPEG YCbCr conversion ourselves by reading Raster");
						Raster raster = reader.readRaster(0,null/*ImageReadParam*/);
						int w = raster.getWidth();
						int h = raster.getHeight();
						byte[] data = (byte[])(raster.getDataElements(0,0,w,h,null));	// do NOT use form without width and height

						ComponentColorModel cm = new ComponentColorModel(colorSpace,
															   new int[] {8,8,8},
															   false,		// has alpha
															   false,		// alpha premultipled
															   Transparency.OPAQUE,
															   DataBuffer.TYPE_BYTE
															   );
				
						// pixel interleaved
						ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_BYTE,
																   w,
																   h,
																   3,
																   w*3,
																   new int[] {0,1,2}
																   );

						DataBuffer buf = new DataBufferByte(data,w,0/*offset*/);
						WritableRaster wr = Raster.createWritableRaster(sm,buf,new Point(0,0));
						image = BufferedImageUtilities.convertYBRToRGB(new BufferedImage(cm,wr,true,null));
					}
					else {
						slf4jlogger.info("getDecompressedFrameAsBufferedImageUsingImageReader(): reading Raster is not supported ... assuming Reader will leave the color space alone and so we do the YCbCr conversion ourselves");
						image =  BufferedImageUtilities.convertYBRToRGB(reader.read(0));
					}
				}
			}
			if (image == null) {
				slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): leaving color space conversion (if any) to Reader");
				image = reader.read(0);
			}
		}
		catch (IIOException e) {
			slf4jlogger.error("", e);
			slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): \"{}\"",e.toString());
			//if (transferSyntaxUID.equals(TransferSyntax.JPEGBaseline) && reader.getOriginatingProvider().getDescription(Locale.US).equals("Standard JPEG Image Reader") && (reader.getOriginatingProvider().getVendorName().equals("Sun Microsystems, Inc.") || reader.getOriginatingProvider().getVendorName().equals("Oracle Corporation"))
			// && e.toString().equals("javax.imageio.IIOException: Inconsistent metadata read from stream")) {
			//	throw new DicomException("Reader "+reader.getOriginatingProvider().getDescription(Locale.US)+" "+reader.getOriginatingProvider().getVendorName()+" "+reader.getOriginatingProvider().getVersion()+" does not support JPEG images with components numbered from 0");
			//}
		}
		slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): Back from frame reader.readAll()");
		//if (iioImage == null) {
		if (image == null) {
			throw new DicomException("Reader "+reader.getOriginatingProvider().getDescription(Locale.US)+" "+reader.getOriginatingProvider().getVendorName()+" "+reader.getOriginatingProvider().getVersion()
				+" returned null image for Transfer Syntax "+transferSyntaxUID);
		}
		else {
			slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): Back from reader.read() BufferedImage={}",image);
			image = makeNewBufferedImageIfNecessary(image,colorSpace);		// works around different YBR result from standard versus native JPEG codec (000785), also multibank return from lossless pixelmed codec if RGB else cannot be blacked out (001031) :(
		}
		slf4jlogger.debug("getDecompressedFrameAsBufferedImageUsingImageReader(): returning image = {}",image);
		return image;
	}
	
	private BufferedImage makeNewBufferedImageIfNecessary(BufferedImage image,ColorSpace colorSpace) {
		slf4jlogger.debug("makeNewBufferedImage(): starting with BufferedImage: {}", BufferedImageUtilities.describeImage(image));
		BufferedImage newImage = image;
		Raster raster = image.getData();
		if (raster.getTransferType() == DataBuffer.TYPE_BYTE && raster.getNumBands() > 1) {		// we only need to do this for color not grayscale, and the models we are about to create contain 3 bands
			slf4jlogger.debug("makeNewBufferedImageIfNecessary(): have multiband byte image - making a new image from it");
			int w = raster.getWidth();
			int h = raster.getHeight();
			byte[] data = (byte[])(raster.getDataElements(0,0,w,h,null));	// do NOT use form without width and height
			if (data != null) {
				ComponentColorModel cm=new ComponentColorModel(colorSpace,
															   new int[] {8,8,8},
															   false,		// has alpha
															   false,		// alpha premultipled
															   Transparency.OPAQUE,
															   DataBuffer.TYPE_BYTE
															   );
				
				// pixel interleaved
				ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_BYTE,
																   w,
																   h,
																   3,
																   w*3,
																   new int[] {0,1,2}
																   );

				// band interleaved
				//ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_BYTE,
				//												   w,
				//												   h,
				//												   1,
				//												   w,
				//												   new int[] {0,w*h,w*h*2}
				//											   );
																
				DataBuffer buf = new DataBufferByte(data,w,0/*offset*/);
				
				WritableRaster wr = Raster.createWritableRaster(sm,buf,new Point(0,0));
				
				newImage = new BufferedImage(cm,wr,true,null);	// no properties hash table
//System.err.print("CompressedFrameDecoder.makeNewBufferedImage(): returns new BufferedImage: ");
//com.pixelmed.display.BufferedImageUtilities.describeImage(newImage,System.err);
			}
		}
		slf4jlogger.debug("makeNewBufferedImage(): finishing with BufferedImage: {}", BufferedImageUtilities.describeImage(newImage));
		return newImage;
	}

	public void dispose() throws Throwable {
		slf4jlogger.debug("dispose()");
			if (reader != null) {
				try {
		slf4jlogger.debug("dispose(): Calling dispose() on reader");
					reader.dispose();	// http://info.michael-simons.eu/2012/01/25/the-dangers-of-javas-imageio/
				}
				catch (Exception e) {
					slf4jlogger.error("", e);
				}
			}
		}

}
