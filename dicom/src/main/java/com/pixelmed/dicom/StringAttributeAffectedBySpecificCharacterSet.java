/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

/**
 * <p>An abstract class specializing {@link Attribute Attribute} for
 * the family of string attributes that support different specific character sets.</p>
 *
 * @author	dclunie
 */
abstract public class StringAttributeAffectedBySpecificCharacterSet extends StringAttribute {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/StringAttributeAffectedBySpecificCharacterSet.java,v 1.14 2022/01/21 19:51:18 dclunie Exp $";
	
	/**
	 * @param	t
	 */
	protected StringAttributeAffectedBySpecificCharacterSet(AttributeTag t) {
		super(t);
	}
	
	/**
	 * @param	t
	 * @param	specificCharacterSet
	 */
	protected StringAttributeAffectedBySpecificCharacterSet(AttributeTag t,SpecificCharacterSet specificCharacterSet) {
		super(t,specificCharacterSet);
	}

	/**
	 * @param	t
	 * @param	vl
	 * @param	i
	 * @param	specificCharacterSet
	 * @throws	IOException
	 * @throws	DicomException
	 */
	protected StringAttributeAffectedBySpecificCharacterSet(AttributeTag t,long vl,DicomInputStream i,SpecificCharacterSet specificCharacterSet) throws IOException, DicomException {
		super(t,vl,i,specificCharacterSet);
	}
}

