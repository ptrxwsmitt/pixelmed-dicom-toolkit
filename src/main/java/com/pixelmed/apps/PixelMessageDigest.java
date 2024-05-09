/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;

import com.pixelmed.display.SourceImage;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import java.io.File;
import java.io.IOException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;	// for getting digest as hex string

/**
 * <p>A class for computing message digests of pixel data, decompressed if compressed.</p>
 *
 * @author	dclunie
 */
public class PixelMessageDigest {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/PixelMessageDigest.java,v 1.4 2022/01/21 19:51:12 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(PixelMessageDigest.class);
	
	public byte[] computePixelMessageDigestFromFrames(File inputFile) throws DicomException, IOException, NoSuchAlgorithmException {
		slf4jlogger.info("computePixelMessageDigestFromFrames(): inputFile = {}",inputFile);
		AttributeList list = new AttributeList();
		list.setDecompressPixelData(false);
		list.read(inputFile);

		SourceImage sImg = new SourceImage(list);
		int nframes = sImg.getNumberOfFrames();

		MessageDigest md = MessageDigest.getInstance("MD5");
		for (int f=0; f<nframes; ++f) {
			BufferedImage img = sImg.getBufferedImage(f,false/*useICC*/);								// Hmm :( Do we need to be concerned about color space transformations?
			if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug(com.pixelmed.display.BufferedImageUtilities.describeImage(img));
			byte[] decompressedValues = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();	// Hmm :( Do we need to be concerned about the order of components and interleaving?
			md.update(decompressedValues);
		}
		return md.digest();
	}

	public PixelMessageDigest(String inputfilename) throws DicomException, IOException, NoSuchAlgorithmException {
		byte[] digest = computePixelMessageDigestFromFrames(new File(inputfilename));
		if (digest != null) {
			slf4jlogger.info("Digest={}",DatatypeConverter.printHexBinary(digest).toLowerCase());		// https://www.baeldung.com/java-md5
		}
	}

	/**
	 * <p>Read an image and compute message digest of pixel data, decompressed if compressed.</p>
	 *
	 * @param	arg	array of one string - the input image
	 */
	public static void main(String arg[]) {
		try {
			new PixelMessageDigest(arg[0]);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);	// no need to use SLF4J since command line utility/test
			System.exit(0);
		}
	}
}
