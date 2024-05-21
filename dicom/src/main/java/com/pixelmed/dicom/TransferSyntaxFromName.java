/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>A class to lookup the DICOM Transfer Syntax UID from a string name.</p>
 *
 * @author	dclunie
 */
public class TransferSyntaxFromName {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/TransferSyntaxFromName.java,v 1.14 2022/01/21 19:51:18 dclunie Exp $";
	
	static protected Map<String,String> uidByKeyword = new HashMap<String,String>();

	static {
		uidByKeyword.put("ImplicitVRLittleEndian",TransferSyntax.ImplicitVRLittleEndian);
		uidByKeyword.put("ExplicitVRLittleEndian",TransferSyntax.ExplicitVRLittleEndian);
		uidByKeyword.put("ExplicitVRBigEndian",TransferSyntax.ExplicitVRBigEndian);
		uidByKeyword.put("Default",TransferSyntax.Default);
		uidByKeyword.put("DeflatedExplicitVRLittleEndian",TransferSyntax.DeflatedExplicitVRLittleEndian);
		uidByKeyword.put("JPEGBaseline",TransferSyntax.JPEGBaseline);
		uidByKeyword.put("JPEGExtended",TransferSyntax.JPEGExtended);
		uidByKeyword.put("JPEGLossless",TransferSyntax.JPEGLossless);
		uidByKeyword.put("JPEGLosslessSV1",TransferSyntax.JPEGLosslessSV1);
		uidByKeyword.put("JPEGLS",TransferSyntax.JPEGLS);
		uidByKeyword.put("JPEGNLS",TransferSyntax.JPEGNLS);
		uidByKeyword.put("JPEG2000Lossless",TransferSyntax.JPEG2000Lossless);
		uidByKeyword.put("JPEG2000",TransferSyntax.JPEG2000);
		uidByKeyword.put("MPEG2MPML",TransferSyntax.MPEG2MPML);
		uidByKeyword.put("MPEG2MPHL",TransferSyntax.MPEG2MPHL);
		uidByKeyword.put("MPEG4HP41",TransferSyntax.MPEG4HP41);
		uidByKeyword.put("MPEG4HP41BD",TransferSyntax.MPEG4HP41BD);
		uidByKeyword.put("MPEG4HP422D",TransferSyntax.MPEG4HP422D);
		uidByKeyword.put("MPEG4HP423D",TransferSyntax.MPEG4HP423D);
		uidByKeyword.put("MPEG4HP42ST",TransferSyntax.MPEG4HP42ST);
		uidByKeyword.put("RLE",TransferSyntax.RLE);
		uidByKeyword.put("PixelMedBzip2ExplicitVRLittleEndian",TransferSyntax.PixelMedBzip2ExplicitVRLittleEndian);
		uidByKeyword.put("PixelMedEncapsulatedRawLittleEndian",TransferSyntax.PixelMedEncapsulatedRawLittleEndian);
		uidByKeyword.put("Papyrus3ImplicitVRLittleEndian",TransferSyntax.Papyrus3ImplicitVRLittleEndian);
	}

	static protected Map<String,String> keywordByUID  = new HashMap<String,String>();

	static {
		keywordByUID.put(TransferSyntax.ImplicitVRLittleEndian,"ImplicitVRLittleEndian");
		keywordByUID.put(TransferSyntax.ExplicitVRLittleEndian,"ExplicitVRLittleEndian");
		keywordByUID.put(TransferSyntax.ExplicitVRBigEndian,"ExplicitVRBigEndian");
		keywordByUID.put(TransferSyntax.Default,"Default");
		keywordByUID.put(TransferSyntax.DeflatedExplicitVRLittleEndian,"DeflatedExplicitVRLittleEndian");
		keywordByUID.put(TransferSyntax.JPEGBaseline,"JPEGBaseline");
		keywordByUID.put(TransferSyntax.JPEGExtended,"JPEGExtended");
		keywordByUID.put(TransferSyntax.JPEGLossless,"JPEGLossless");
		keywordByUID.put(TransferSyntax.JPEGLosslessSV1,"JPEGLosslessSV1");
		keywordByUID.put(TransferSyntax.JPEGLS,"JPEGLS");
		keywordByUID.put(TransferSyntax.JPEGNLS,"JPEGNLS");
		keywordByUID.put(TransferSyntax.JPEG2000Lossless,"JPEG2000Lossless");
		keywordByUID.put(TransferSyntax.JPEG2000,"JPEG2000");
		keywordByUID.put(TransferSyntax.MPEG2MPML,"MPEG2MPML");
		keywordByUID.put(TransferSyntax.MPEG2MPHL,"MPEG2MPHL");
		keywordByUID.put(TransferSyntax.MPEG4HP41,"MPEG4HP41");
		keywordByUID.put(TransferSyntax.MPEG4HP41BD,"MPEG4HP41BD");
		keywordByUID.put(TransferSyntax.MPEG4HP422D,"MPEG4HP422D");
		keywordByUID.put(TransferSyntax.MPEG4HP423D,"MPEG4HP423D");
		keywordByUID.put(TransferSyntax.MPEG4HP42ST,"MPEG4HP42ST");
		keywordByUID.put(TransferSyntax.RLE,"RLE");
		keywordByUID.put(TransferSyntax.PixelMedBzip2ExplicitVRLittleEndian,"PixelMedBzip2ExplicitVRLittleEndian");
		keywordByUID.put(TransferSyntax.PixelMedEncapsulatedRawLittleEndian,"PixelMedEncapsulatedRawLittleEndian");
		keywordByUID.put(TransferSyntax.Papyrus3ImplicitVRLittleEndian,"Papyrus3ImplicitVRLittleEndian");
	}

	private TransferSyntaxFromName() {
	}
	
	/**
	 * <p>Get the Transfer Syntax UID from the name.</p>
	 *
	 * @param		name	a string name of the transfer syntax
	 * @return				the UID if found, else the supplied argument if of UID form, else null
	 */
	static public String getUID(String name)	{
		String uid = null;
		if (name != null) {
			uid = (String)(uidByKeyword.get(name));
			if (uid == null) {
				// if string is a UID form, just return itself
				if (name.matches("[0-9.][0-9.]*")) {
					uid=name;
				}
			}
		}
		return uid;
	}

	/**
	 * @param	transferSyntaxUID	UID of the Transfer Syntax, as a String without trailing zero padding
	 * @return			a keyword identifying the Transfer Syntax, or an empty string if none
	 */
	public static String getKeywordFromUID(String transferSyntaxUID) {
		String keyword = keywordByUID.get(transferSyntaxUID);
		return keyword == null ? "" : keyword;
	}

	/**
	 * <p>Test.</p>
	 *
	 * @param	arg	none
	 */
	public static void main(String arg[]) {
		System.err.println("Default from name: "+(TransferSyntaxFromName.getUID("Default").equals(TransferSyntax.Default)));	// no need to use SLF4J since command line utility/test
		System.err.println("Default from uid : "+(TransferSyntaxFromName.getUID(TransferSyntax.Default).equals(TransferSyntax.Default)));
		System.err.println("Dummy from name  : "+(TransferSyntaxFromName.getUID("Dummy") == null));
	}

}

