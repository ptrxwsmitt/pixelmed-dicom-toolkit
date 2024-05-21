/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

import java.text.NumberFormat;

/**
 * <p>A concrete class specializing {@link Attribute Attribute} for
 * Unsigned Very Long (UV) attributes.</p>
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
public class UnsignedVeryLongAttribute extends Attribute {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/UnsignedVeryLongAttribute.java,v 1.4 2022/01/21 19:51:19 dclunie Exp $";

	short[] cachedShortCopy;
	int[] cachedIntCopy;
	long[] values;
	float[] cachedFloatCopy;
	double[] cachedDoubleCopy;

	static int bytesPerValue = 8;

	private void flushCachedCopies() {
		cachedShortCopy=null;
		cachedIntCopy=null;
		cachedFloatCopy=null;
		cachedDoubleCopy=null;
	}

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * @param	t	the tag of the attribute
	 */
	public UnsignedVeryLongAttribute(AttributeTag t) {
		super(t);
		flushCachedCopies();
		values=null;
	}

	/**
	 * <p>Read an attribute from an input stream.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	vl			the value length of the attribute
	 * @param	i			the input stream
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public UnsignedVeryLongAttribute(AttributeTag t,long vl,DicomInputStream i) throws IOException, DicomException {
		super(t);
		doCommonConstructorStuff(vl,i);
	}

	/**
	 * <p>Read an attribute from an input stream.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	vl			the value length of the attribute
	 * @param	i			the input stream
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public UnsignedVeryLongAttribute(AttributeTag t,Long vl,DicomInputStream i) throws IOException, DicomException {
		super(t);
		doCommonConstructorStuff(vl.longValue(),i);
	}

	/**
	 * @param	vl
	 * @param	i
	 * @throws	IOException
	 * @throws	DicomException
	 */
	private void doCommonConstructorStuff(long vl,DicomInputStream i) throws IOException, DicomException {
		flushCachedCopies();
		if (vl%bytesPerValue != 0) {
			throw new DicomException("incorrect value length ("+vl+" dec) for VR "+getVRAsString()+" - caller will need to skip value length bytes to get to next data element");
		}
		else {
			int vm=(int)(vl/bytesPerValue);
			values=null;
			for (int j=0; j<vm; ++j) addValue((int)(i.readUnsigned64()));
		}
	}

	/**
	 * @param	o
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public void write(DicomOutputStream o) throws DicomException, IOException {
		writeBase(o);
		long[] v = getLongValues();
		if (v != null) {
			for (int j=0; j<v.length; ++j) {
				o.writeUnsigned64(v[j]);
			}
		}
	}
	
	/***/
	public String toString(DicomDictionary dictionary) {
		StringBuffer str = new StringBuffer();
		str.append(super.toString(dictionary));
		str.append(" [");
		try {
			long[] v = getLongValues();
			if (v != null) {
				for (int j=0; j<v.length; ++j) {
					if (j > 0) str.append(",");
					str.append("0x");
					str.append(Long.toHexString(v[j]));
				}
			}
		}
		catch (DicomException e) {
			str.append("XXXX");
		}
		str.append("]");
		return str.toString();
	}

	/**
	 * @param	format		the format to use for each numerical or decimal value
	 * @throws	DicomException
	 */
	public String[] getStringValues(NumberFormat format) throws DicomException {
		String sv[] = null;
		long[] v = getLongValues();
		if (v != null) {
			sv=new String[v.length];
			for (int j=0; j<v.length; ++j) {
				sv[j] = (format == null) ? Long.toString(v[j]) : format.format(v[j]);
			}
		}
		return sv;
	}

	/**
	 * @throws	DicomException
	 */
	public short[] getShortValues() throws DicomException {
		if (cachedShortCopy == null) cachedShortCopy=ArrayCopyUtilities.copyUnsignedLongToShortArray(values);
		return cachedShortCopy;
	}

	/**
	 * @throws	DicomException
	 */
	public int[] getIntegerValues() throws DicomException {
		if (cachedIntCopy == null) cachedIntCopy=ArrayCopyUtilities.copyUnsignedLongToIntArray(values);
		return cachedIntCopy;
	}

	/**
	 * @throws	DicomException
	 */
	public long[] getLongValues() throws DicomException {
		return values;
	}

	/**
	 * @throws	DicomException
	 */
	public float[] getFloatValues() throws DicomException {
		if (cachedFloatCopy == null) cachedFloatCopy=ArrayCopyUtilities.copyUnsignedLongToFloatArray(values);
		return cachedFloatCopy;
	}

	/**
	 * @throws	DicomException
	 */
	public double[] getDoubleValues() throws DicomException {
		if (cachedDoubleCopy == null) cachedDoubleCopy=ArrayCopyUtilities.copyUnsignedLongToDoubleArray(values);
		return cachedDoubleCopy;
	}

	/**
	 * @param	v
	 * @throws	DicomException
	 */
	public void addValue(short v) throws DicomException {
		addValue((int)v);		// should we sign extend or not ? :(
	}

	/**
	 * @param	v
	 * @throws	DicomException
	 */
	public void addValue(int v) throws DicomException {
		addValue((long)v);		// should we sign extend or not ? :(
	}

	/**
	 * @param	v
	 * @throws	DicomException
	 */
	public void addValue(long v) throws DicomException {
		flushCachedCopies();
		values=ArrayCopyUtilities.expandArray(values);
		values[valueMultiplicity++]=v;
		valueLength+=8;
	}

	/**
	 * @param	v
	 * @throws	DicomException
	 */
	public void addValue(float v) throws DicomException {
		addValue((int)v);
	}

	/**
	 * @param	v
	 * @throws	DicomException
	 */
	public void addValue(double v) throws DicomException {
		addValue((int)v);
	}

	/**
	 * @param	v
	 * @throws	DicomException
	 */
	public void addValue(String v) throws DicomException {
		long longValue = 0;
		try {
			longValue=Long.parseLong(v);
		}
		catch (NumberFormatException e) {
			throw new DicomException(e.toString());
		}
		addValue(longValue);
	}

	/**
	 * @param	v
	 * @throws	DicomException
	 */
	public void setValues(long[] v) throws DicomException {
		values=v;
		valueMultiplicity=v.length;
		valueLength=v.length*8;
		flushCachedCopies();
	}
	
	/**
	 */
	public void removeValues() {
		flushCachedCopies();
		values=null;
		valueMultiplicity=0;
		valueLength=0;
	}
	
	/**
	 * <p>Get the value representation of this attribute (UL).</p>
	 *
	 * @return	'U','L' in ASCII as a two byte array; see {@link ValueRepresentation ValueRepresentation}
	 */
	public byte[] getVR() { return ValueRepresentation.UL; }

}

