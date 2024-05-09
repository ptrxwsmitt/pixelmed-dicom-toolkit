/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class to categorize DICOM images as having been lossy compressed or not.</p>
 *
 * @author	dclunie
 */
public class LossyImageCompression {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/LossyImageCompression.java,v 1.16 2022/01/21 19:51:16 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(LossyImageCompression.class);

	/**
	 * <p>determine if an image has ever been lossy compressed.</p>
	 *
	 * @param	list	list of attributes representing a DICOM image
	 * @return		true if has ever been lossy compressed
	 */
	public static boolean hasEverBeenLossyCompressed(AttributeList list) {
		// ignore the fact that the LossyImageCompression is supposed to be a string with values "00" or "01" ... works this way even if (incorrectly) a numeric
		// ignore the fact that LossyImageCompressionRatio may be multi-valued
		// ignore the fact that LossyImageCompressionMethod may be multi-valued
		if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("hasEverBeenLossyCompressed(): Checking LossyImageCompression {}",Attribute.getSingleIntegerValueOrDefault(list,TagFromName.LossyImageCompression,-1));
		if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("hasEverBeenLossyCompressed(): Checking LossyImageCompressionRatio {}",Attribute.getSingleDoubleValueOrDefault(list,TagFromName.LossyImageCompressionRatio,-1));
		if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("hasEverBeenLossyCompressed(): Checking LossyImageCompressionMethod length {}",Attribute.getSingleStringValueOrEmptyString(list,TagFromName.LossyImageCompressionMethod).length());
		if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("hasEverBeenLossyCompressed(): Checking DerivationDescription {}",Attribute.getDelimitedStringValuesOrEmptyString(list,TagFromName.DerivationDescription).toLowerCase(java.util.Locale.US).indexOf("lossy"));
		if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("hasEverBeenLossyCompressed(): Checking TransferSyntaxUID {}",Attribute.getSingleStringValueOrEmptyString(list,TagFromName.TransferSyntaxUID));
		if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("hasEverBeenLossyCompressed(): Checking TransferSyntaxUID lossy {}",new TransferSyntax(Attribute.getSingleStringValueOrEmptyString(list,TagFromName.TransferSyntaxUID)).isLossy());
		return Attribute.getSingleIntegerValueOrDefault(list,TagFromName.LossyImageCompression,-1) > 0
		    || Attribute.getSingleDoubleValueOrDefault(list,TagFromName.LossyImageCompressionRatio,-1) > 0
		    || Attribute.getSingleStringValueOrEmptyString(list,TagFromName.LossyImageCompressionMethod).length() > 0
		    || Attribute.getDelimitedStringValuesOrEmptyString(list,TagFromName.DerivationDescription).toLowerCase(java.util.Locale.US).indexOf("lossy") > -1
		    || new TransferSyntax(Attribute.getSingleStringValueOrEmptyString(list,TagFromName.TransferSyntaxUID)).isLossy()
		    ;
	}

	/**
	 * <p>Describe the nature of lossy compressed that has ever been applied to an image.</p>
	 *
	 * @param	list	list of attributes representing a DICOM image	
	 * @return		a string describing the compression, including method and ratio if possible, or a zero length string if never lossy compressed
	 */
	public static String describeLossyCompression(AttributeList list) {
		String value;
		if (LossyImageCompression.hasEverBeenLossyCompressed(list)) {
			//String ratio = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.LossyImageCompressionRatio);
			String ratio = Double.toString(Attribute.getSingleDoubleValueOrDefault(list,TagFromName.LossyImageCompressionRatio,-1));
			slf4jlogger.debug("describeLossyCompression(): LossyImageCompressionRatio "+ratio);
			if (ratio.length() > 0) {
				if (ratio.contains(".")) {
					ratio = ratio.replaceFirst("0+$","");
					slf4jlogger.debug("describeLossyCompression(): LossyImageCompressionRatio after removing trailing zeroes if period "+ratio);
				}
				ratio = ratio.replaceFirst("[.]$","");
				slf4jlogger.debug("describeLossyCompression(): LossyImageCompressionRatio after possibly removing trailing period "+ratio);
				if (ratio.equals("0")) {
					ratio="";
				}
				slf4jlogger.debug("describeLossyCompression(): LossyImageCompressionRatio after possibly removing only zero "+ratio);
			}
			String method = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.LossyImageCompressionMethod);
			if (method.length() > 0) {
				if (method.equals("ISO_10918_1")) {
					method="JPEG";
				}
				else if (method.equals("ISO_14495_1")) {
					method="JLS";
				}
				else if (method.equals("ISO_15444_1")) {
					method="J2K";
				}
				else if (method.equals("ISO_13818_2")) {
					method="MPEG2";
				}
			}
			if (method.length() == 0 && ratio.length() == 0) {
				value = "Lossy";
			}
			else if (ratio.length() == 0) {
				value = "Lossy "+method;
			}
			else if (method.length() == 0) {
				value = "Lossy "+ratio+":1";
			}
			else {
				value = method+" "+ratio+":1";
			}
		}
		else {
			value = "";
		}
		slf4jlogger.debug("describeLossyCompression(): LossyImageCompressionRatio returning "+value);
		return value;
	}

	/**
	 * <p>Read a DICOM image input file, and determine if it has ever been lossy compressed.</p>
	 *
	 * @param	arg	one required parameters, the input file name
	 */
	public static void main(String arg[]) {
		String dicomFileName = arg[0];
		try {
			AttributeList list = new AttributeList();
			DicomInputStream in = new DicomInputStream(new BufferedInputStream(new FileInputStream(dicomFileName)));
			list.read(in,TagFromName.PixelData);
			in.close();
			System.out.println(hasEverBeenLossyCompressed(list));	// no need to use SLF4J since command line utility/test
		}
		catch (Exception e) {
			e.printStackTrace(System.err);	// no need to use SLF4J since command line utility/test
		}
	}
}

