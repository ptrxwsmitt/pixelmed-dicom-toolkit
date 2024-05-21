/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

/**
 * <p>A concrete class specializing {@link Attribute Attribute} for
 * Other Byte (OB) attributes.</p>
 *
 * <p>Though an instance of this class may be created
 * using its constructors, there is also a factory class, {@link AttributeFactory AttributeFactory}.</p>
 *
 * @see Attribute
 * @see AttributeFactory
 * @see AttributeList
 *
 * @author	dclunie
 */
public class OtherByteAttribute extends Attribute {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/OtherByteAttribute.java,v 1.25 2022/01/21 19:51:17 dclunie Exp $";

	private byte[] values;

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * @param	t	the tag of the attribute
	 */
	public OtherByteAttribute(AttributeTag t) {
		super(t);
	}

	/**
	 * <p>Read an attribute from an input stream.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	vl			the value length of the attribute
	 * @param	i			the input stream
	 * @throws	IOException		if an I/O error occurs
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public OtherByteAttribute(AttributeTag t,long vl,DicomInputStream i) throws IOException, DicomException {
		super(t);
		doCommonConstructorStuff(vl,i);
	}

	/**
	 * <p>Read an attribute from an input stream.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	vl			the value length of the attribute
	 * @param	i			the input stream
	 * @throws	IOException		if an I/O error occurs
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public OtherByteAttribute(AttributeTag t,Long vl,DicomInputStream i) throws IOException, DicomException {
		super(t);
		doCommonConstructorStuff(vl.longValue(),i);
	}

	/**
	 * @param	vl
	 * @param	i
	 * @throws	IOException		if an I/O error occurs
	 * @throws	DicomException	if error in DICOM encoding
	 */
	private void doCommonConstructorStuff(long vl,DicomInputStream i) throws IOException, DicomException {
		values=null;
		valueLength=vl;

		if (vl > 0) {
			byte[] buffer = new byte[(int)vl];
			try {
				i.readInsistently(buffer,0,(int)vl);
			}
			catch (IOException e) {
				throw new DicomException("Failed to read value (length "+vl+" dec) in "+ValueRepresentation.getAsString(getVR())+" attribute "+getTag());
			}
			setValues(buffer);
		}
	}

	public long getPaddedVL() {
		long vl = getVL();
		if (vl%2 != 0) ++vl;
		return vl;
	}
	
	public void write(DicomOutputStream o) throws DicomException, IOException {
		writeBase(o);
		if (values != null && values.length > 0) {
			o.write(values);
			if (getVL() != values.length) {
				throw new DicomException("Internal error - byte array length ("+values.length+") not equal to expected VL("+getVL()+")");
			}
			long npad = getPaddedVL() - values.length;
			while (npad-- > 0) o.write(0x00);
		}
	}
	
	/***/
	public String toString(DicomDictionary dictionary) {
		StringBuffer str = new StringBuffer();
		str.append(super.toString(dictionary));
		str.append(" []");		// i.e. don't really dump values ... too many
		return str.toString();
	}

	/**
	 * @param	v
	 * @throws	DicomException
	 */
	public void addValue(String v) throws DicomException {
		if (values == null) {
			// see also StringAttribute translateStringToByteArray()
			if (v != null && v.length() > 0) {
				byte[] b = v.getBytes();
				int length = b.length;
				if (length%2 == 0) {
					values=b;
				}
				else {
					values= new byte[length+1];
					System.arraycopy(b,0,values,0,length);
					values[length]=(byte)0x20;	// space not null since ostensibly a string
				}
				valueMultiplicity=1;		// different from normal value types where VM is size of array
				valueLength=values.length;
			}
		}
		else {
			throw new DicomException("internal error - cannot add more than one string value for attribute "+getTag()+" "+getClass().getName());
		}
	}
	
	/**
	 * @param	v
	 * @throws	DicomException
	 */
	public void setValues(byte[] v) throws DicomException {
		values=v;
		valueMultiplicity=1;		// different from normal value types where VM is size of array
		valueLength=v.length;
	}
	/**
	 * @param	v
	 * @param	big
	 * @throws	DicomException
	 */
	public void setValues(byte[] v,boolean big) throws DicomException { setValues(v); }

	/**
	 * @throws	DicomException
	 */
	public byte[] getByteValues() throws DicomException { return values; }

	/**
	 * @throws	DicomException
	 */
	public byte[] getByteValues(boolean big) throws DicomException  { return values; }

	/**
	 */
	public void removeValues() {
		values=null;
		valueMultiplicity=0;
		valueLength=0;
	}

	/**
	 * <p>Get the value representation of this attribute (OB).</p>
	 *
	 * @return	'O','B' in ASCII as a two byte array; see {@link ValueRepresentation ValueRepresentation}
	 */
	public byte[] getVR() { return ValueRepresentation.OB; }
}

