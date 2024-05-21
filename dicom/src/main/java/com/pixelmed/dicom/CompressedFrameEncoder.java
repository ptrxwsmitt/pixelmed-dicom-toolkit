/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.BinaryOutputStream;
import com.pixelmed.dicom.DicomException;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Locale;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>The {@link CompressedFrameEncoder CompressedFrameEncoder} class implements compression of specified frames
 * in various supported Transfer Syntaxes, which can then be incorporated in DICOM encapsulated images.</p>
 *
 *
 * @author	dclunie
 */
public class CompressedFrameEncoder {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/CompressedFrameEncoder.java,v 1.16 2022/03/08 12:48:59 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(CompressedFrameEncoder.class);
	
	
	/**
	 * <p>Determine the output format string used by the JIIO codecs for the DICOM Transfer Syntax.</p>
	 *
	 * <p>Though not supported by the JIIO codecs, an output format string of "rle" is also recognized.</p>
	 *
	 * @param	transferSyntaxUID		the DICOM Transfer Syntax UID
	 * @return							the format string (lower case) or null if unrecognized
	 */
	public static String chooseOutputFormatForTransferSyntax(String transferSyntaxUID) {
		String outputFormat = null;
		if (transferSyntaxUID.equals(TransferSyntax.JPEG2000) || transferSyntaxUID.equals(TransferSyntax.JPEG2000Lossless)) {
			outputFormat = "jpeg2000";
		}
		else if (transferSyntaxUID.equals(TransferSyntax.JPEGLossless) || transferSyntaxUID.equals(TransferSyntax.JPEGLosslessSV1)) {
			outputFormat = "jpeg-lossless";
		}
		else if (transferSyntaxUID.equals(TransferSyntax.JPEGLS) || transferSyntaxUID.equals(TransferSyntax.JPEGNLS)) {
			outputFormat = "jpeg-ls";
		}
		else if (transferSyntaxUID.equals(TransferSyntax.JPEGBaseline) || transferSyntaxUID.equals(TransferSyntax.JPEGExtended)) {
			outputFormat = "jpeg";
		}
		else if (transferSyntaxUID.equals(TransferSyntax.RLE)) {
			outputFormat = "rle";
		}
		return outputFormat;
	}
	
	/**
	 * <p>Determine the DICOM Transfer Syntax for the output format string used by the JIIO codecs.</p>
	 *
	 * <p>Though not supported by the JIIO codecs, an output format string of "rle" is also recognized.</p>
	 *
	 * @param	outputFormat		the format string (lower case)
	 * @return						the DICOM Transfer Syntax UID or null if unrecognized
	 */
	public static String chooseTransferSyntaxForOutputFormat(String outputFormat) {
		String transferSyntaxUID = null;
		if (outputFormat.equals("jpeg2000")) {
			transferSyntaxUID = TransferSyntax.JPEG2000Lossless;
		}
		else if (outputFormat.equals("jpeg-lossless")) {
			transferSyntaxUID = TransferSyntax.JPEGLosslessSV1;
		}
		else if (outputFormat.equals("jpeg-ls")) {
			transferSyntaxUID = TransferSyntax.JPEGLS;
		}
		else if (outputFormat.equals("jpeg")) {
			transferSyntaxUID = TransferSyntax.JPEGBaseline;
		}
		else if (outputFormat.equals("rle")) {
			transferSyntaxUID = TransferSyntax.RLE;
		}
		return transferSyntaxUID;
	}
	
	/**
	 * <p>Compress the supplied frame.</p>
	 *
	 * @param	list					the AttributeList from which the frame was extracted
	 * @param	renderedImage			the frame as an image
	 * @param	outputFormat			the compression format to use [jpeg2000|jpeg-lossless|jpeg-ls|rle]
	 * @param	tmpFrameFile			the file to write the compressed bit stream to
	 * @return							the file written to, or null if compression failed
	 * @throws	IOException				if there is an error writing the file
	 * @throws	FileNotFoundException	if the supplied file path cannot be found
	 * @throws	DicomException			if the image cannot be compressed
	 */
	public static File getCompressedFrameAsFile(AttributeList list,BufferedImage renderedImage,String outputFormat,File tmpFrameFile) throws IOException, FileNotFoundException, DicomException {
		File returnFile = null;
		if (outputFormat.equals("rle")) {
			// care about Samples per Pixel and Bits Allocated, not Photometric Intepretation (e.g., doesn't matter if PALETTE COLOR or RGB or YBR_FULL) or Bits Stored (but consider CP 1654)
			// unless single bit images (Bits Stored == 1), which we do not support
			int bitsStored = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsStored,0);
			if (bitsStored == 1) {
				throw new DicomException("Cannot compress single bit images using RLE");
			}
			int samplesPerPixel = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.SamplesPerPixel,0);
			int bitsAllocated = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsAllocated,0);
			int bytesPerSample = bitsAllocated/8;
			int numberOfSegmentsNeeded = samplesPerPixel * bytesPerSample;
			if (numberOfSegmentsNeeded > 15) {
				throw new DicomException("Cannot compress image with "+bitsAllocated+" Bits Allocated and "+samplesPerPixel+" Samples per Pixel using RLE since would require "+numberOfSegmentsNeeded+" segments");
			}
			byte[][] segments = new byte[numberOfSegmentsNeeded][];
			
			SampleModel sampleModel = renderedImage.getSampleModel();
			WritableRaster raster = renderedImage.getRaster();
			int numBands = raster.getNumBands();
			if (numBands != samplesPerPixel) {
				throw new DicomException("Cannot compress image with "+numBands+" bands but "+samplesPerPixel+" Samples per Pixel using RLE");
			}
			int dataType = sampleModel.getDataType();
			DataBuffer dataBuffer = raster.getDataBuffer();
			int w = renderedImage.getWidth();
			int h = renderedImage.getHeight();
			slf4jlogger.debug("w*h*samplesPerPixel*bytesPerSample={}",w*h*samplesPerPixel*bytesPerSample);
			
			byte[] literalRunBuffer = new byte[128];
			
			int nextSegment=0;
			for (int sample=0; sample<samplesPerPixel; ++sample) {
				for (int doingByte=0; doingByte<bytesPerSample; ++doingByte) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					
					int rightShift = (bytesPerSample - doingByte - 1) * 8;	// Do most significant byte first
					
					for (int y=0; y<h; ++y) {
						// "Each row of the image shall be encoded separately and not cross a row boundary"
						int literalRunBufferIndex = 0;
						int identicalByteCount = 0;
						byte identicalByte = 0;
						boolean haveIdenticalByte = false;
						for (int x=0; x<w; ++x) {
							int paddedCompositePixelCode = sampleModel.getSample(x,y,sample,dataBuffer);
							//if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("[{},{}] paddedCompositePixelCode {} dec",y,x,paddedCompositePixelCode);
							byte theByte = (byte)(paddedCompositePixelCode >> rightShift);
							//if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("[{},{}] byte {} dec",y,x,theByte&0xff);
							
							if (identicalByteCount > 0) {
								if (theByte == identicalByte) {
									++identicalByteCount;
									if (identicalByteCount >= 128) {
										// write the run
										if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("[{},{}] writing run of length {} of byte {} dec",y,x,identicalByteCount,identicalByte&0xff);
										baos.write(1-identicalByteCount);
										baos.write(identicalByte);
										// end the run and go to state neither in run nor anything in literalRunBuffer
										identicalByteCount = 0;
										// the value of identicalByte is irrelevant
										literalRunBufferIndex = 0;	// should already be, but just in case
									}
									// else we are good to keep accumulating (in run)
								}
								else {
									// different byte than current run, so write the run
									if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("[{},{}] end of run of length {} of byte {} dec caused by new byte {} dec",y,x,identicalByteCount,identicalByte&0xff,theByte&0xff);
									baos.write((1-identicalByteCount));
									baos.write(identicalByte);
									// end the run and go to state with a single byte in literalRunBuffer
									identicalByteCount = 0;
									// the value of identicalByte is irrelevant
									literalRunBuffer[0] = theByte;
									literalRunBufferIndex = 1;
								}
							}
							else {
								if (literalRunBufferIndex >= 128) {
									// the buffer is full so write it
									if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("[{},{}] writing full buffer length {}",y,x,literalRunBufferIndex);
									baos.write(literalRunBufferIndex-1);
									baos.write(literalRunBuffer,0,literalRunBufferIndex);
									// and put the new byte at the start of the buffer
									literalRunBuffer[0] = theByte;
									literalRunBufferIndex = 1;
								}
								else if (literalRunBufferIndex == 1 && theByte == literalRunBuffer[0]) {
									// start of new run since we have two bytes equal
									if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("[{},{}] detected start of run of {} dec so using single byte in existing buffer length {}",y,x,theByte&0xff,literalRunBufferIndex-1);
									identicalByte = theByte;
									identicalByteCount = 2;
									// but nothing to write since buffer empty after taking the byte for the new run
									literalRunBufferIndex = 0;
								}
								else if (literalRunBufferIndex > 1 && theByte == literalRunBuffer[literalRunBufferIndex-1]) {
									// have start of new run since we have two bytes equal
									// write the buffer contents except for the last byte that will become part of new run
									if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("[{},{}] detected start of run of {} dec so writing existing buffer length {}",y,x,theByte&0xff,literalRunBufferIndex-1);
									baos.write(literalRunBufferIndex-1-1);	// yes, the number of bytes to write is literalRunBufferIndex-1, and then one more is subtracted because that is the encoding
									baos.write(literalRunBuffer,0,literalRunBufferIndex-1);
									// start of new run since we have two bytes equal
									identicalByte = theByte;
									identicalByteCount = 2;
									literalRunBufferIndex = 0;
								}
								else {
									// the buffer is not full (may indeed be empty) so add byte to it
									literalRunBuffer[literalRunBufferIndex++]=theByte;
								}
							}
						}
						// at the end of the row, flush the run or buffer, whichever is outstanding
						if (identicalByteCount > 0) {
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("[{},] end of row flushing run of length {} of byte {} dec",y,identicalByteCount,identicalByte&0xff);
							baos.write(1-identicalByteCount);
							baos.write(identicalByte);
						}
						else if (literalRunBufferIndex > 0) {
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("[{},] end of row flushing buffer length {}",y,literalRunBufferIndex);
							baos.write(literalRunBufferIndex-1);
							baos.write(literalRunBuffer,0,literalRunBufferIndex);
						}
					}
					// "Each RLE segment must be an even number of bytes or padded at its end with zero to make it even"
					if (baos.size()%2 != 0) {
						baos.write(0);
					}
					segments[nextSegment] = baos.toByteArray();
					if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("segments[{}].length={} sample={} doingByte={}",nextSegment,segments[nextSegment].length,sample,doingByte);
					++nextSegment;
				}
			}
			slf4jlogger.debug("Have {} segments",nextSegment);
			{
				BinaryOutputStream o = new BinaryOutputStream(new FileOutputStream(tmpFrameFile),false/*big*/);
				o.writeUnsigned32(nextSegment);
				// write segment table
				long segmentOffset = 64;	// starts with the fixed length of the header segment offset table itself
				for (int segment=0; segment<15; ++segment) {
					if (segment<nextSegment) {
						slf4jlogger.debug("segments[{}] offset {}",segment,segmentOffset);
						o.writeUnsigned32(segmentOffset);
						segmentOffset += segments[segment].length;
					}
					else {
						slf4jlogger.debug("segments[{}] offset 0",segment);
						o.writeUnsigned32(0);
					}
				}
				for (int segment=0; segment<nextSegment; ++segment) {
					o.write(segments[segment]);
				}
				o.flush();
				o.close();
				returnFile = tmpFrameFile;
			}
		}
		else {
			Iterator writers = ImageIO.getImageWritersByFormatName(outputFormat);
			if (writers != null && writers.hasNext()) {
				ImageWriter writer = (ImageWriter)writers.next();
				if (writer != null) {
					if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("getCompressedFrameAsFile(): Using writer {} {} {}",writer.getOriginatingProvider().getDescription(Locale.US),writer.getOriginatingProvider().getVendorName(),writer.getOriginatingProvider().getVersion());
					ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(tmpFrameFile);
					writer.setOutput(imageOutputStream);
					try {
						boolean good = false;
						ImageWriteParam writeParameters = writer.getDefaultWriteParam();
						boolean canWriteCompressed = writeParameters.canWriteCompressed();
						if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("doSomethingWithDicomFileOnMedia(): ImageWriteParam.canWriteCompressed() = {}",canWriteCompressed);
						if (canWriteCompressed) {
							if (outputFormat.equals("jpeg2000")) {
								// Do not want to depend on presence of jai_imageio.jar, so use reflection to set write parameters (000950)
								//com.sun.media.imageio.plugins.jpeg2000.J2KImageWriteParam writeParameters = (com.sun.media.imageio.plugins.jpeg2000.J2KImageWriteParam)(writer.getDefaultWriteParam());
								//	writeParameters.setLossless(true);
								//	writeParameters.setComponentTransformation(true);
								Class classToUse = Class.forName("com.sun.media.imageio.plugins.jpeg2000.J2KImageWriteParam");	// may throw ClassNotFoundException if not present at runtime
								if (writeParameters != null && classToUse.isInstance(writeParameters)) {
									java.lang.reflect.Method canWriteCompressedMethod = classToUse.getMethod("canWriteCompressed");
									if (canWriteCompressedMethod == null) {
										throw new DicomException("Could not get J2KImageWriteParam.canWriteCompressed() method");
									}
									java.lang.reflect.Method setLossless = classToUse.getMethod("setLossless",Boolean.TYPE);
									if (setLossless == null) {
										throw new DicomException("Could not get J2KImageWriteParam.setLossless() method");
									}
									setLossless.invoke(writeParameters,Boolean.TRUE);
									
									java.lang.reflect.Method setComponentTransformation = classToUse.getMethod("setComponentTransformation",Boolean.TYPE);
									if (setComponentTransformation == null) {
										throw new DicomException("Could not get J2KImageWriteParam.setComponentTransformation() method");
									}
									setComponentTransformation.invoke(writeParameters,Boolean.TRUE);	// Assume RGB and assume always want to transform; other than YBR_RCT is illegal in DICOM anyway; JJ2000 will fail if set to false anyway (000981)

									java.lang.reflect.Method setWriteCodeStreamOnly = classToUse.getMethod("setWriteCodeStreamOnly",Boolean.TYPE);
									if (setWriteCodeStreamOnly == null) {
										throw new DicomException("Could not get setWriteCodeStreamOnly() method");
									}
									setWriteCodeStreamOnly.invoke(writeParameters,Boolean.TRUE);	// no JP2 box (001117)
									good = true;
								}
							}
							else if (outputFormat.equals("jpeg-lossless")) {
								writeParameters.setCompressionType("JPEG-LOSSLESS");
								// ? how to force SV1 ? empirically, it always seems to be set to 1 (in SOS StartOfSpectralOrPredictorSelection)
								good = true;
							}
							else if (outputFormat.equals("jpeg-ls")) {
								writeParameters.setCompressionType("JPEG-LS");
								good = true;
							}
							else if (outputFormat.equals("jpeg")) {
								// do NOT writeParameters.setCompressionType("JPEG") ... use (successful) ConsumerFormatImageMaker as a guide ...
								writeParameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
								writeParameters.setCompressionQuality(1f);	// best quality
								good = true;
							}
						}
						if (good) {
							IIOMetadata metadata = null;
							writer.write(metadata,new IIOImage(renderedImage,null/*no thumbnails*/,metadata),writeParameters);
							imageOutputStream.flush();
							imageOutputStream.close();
							returnFile = tmpFrameFile;
							
							try {
								slf4jlogger.trace("doSomethingWithDicomFileOnMedia(): Calling dispose() on writer");
								writer.dispose();
							}
							catch (Exception e) {
								slf4jlogger.error("",e);
							}
						}
						else {
							slf4jlogger.error("Could not setup writer with compression and necessary parameters");
						}
					}
					catch (Exception e) {
						slf4jlogger.error("Exception while setting parameters for writer",e);
					}
				}
				else {
					throw new DicomException("Cannot find writer for format "+outputFormat);
				}
			}
			else {
				throw new DicomException("Cannot find writer for format "+outputFormat);
			}
		}
		return returnFile;
	}

}
