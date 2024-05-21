/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

/**
 * <p>A concrete class specializing {@link Attribute Attribute} for
 * Other Double (OD) attributes.</p>
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
public class OtherDoubleAttribute extends Attribute {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/OtherDoubleAttribute.java,v 1.12 2022/03/06 16:18:05 dclunie Exp $";

	private double[] values;

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * @param	t	the tag of the attribute
	 */
	public OtherDoubleAttribute(AttributeTag t) {
		super(t);
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
	public OtherDoubleAttribute(AttributeTag t,long vl,DicomInputStream i) throws IOException, DicomException {
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
	public OtherDoubleAttribute(AttributeTag t,Long vl,DicomInputStream i) throws IOException, DicomException {
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
		values=null;
		valueLength=vl;

		if (vl > 0) {
			int len = (int)(vl/8);
			double buffer[] = new double[len];
			i.readDouble(buffer,len);
			setValues(buffer);
		}
	}

	/**
	 * @param	o
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public void write(DicomOutputStream o) throws DicomException, IOException {
		writeBase(o);
		if (values != null && values.length > 0) {
			o.writeDouble(values,values.length);
			if (getVL() != values.length*8) {
				throw new DicomException("Internal error - double array length ("+values.length*8+") not equal to expected VL("+getVL()+")");
			}
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
	public void setValues(double[] v) throws DicomException {
		values=v;
		valueMultiplicity=1;		// different from normal value types where VM is size of array
		valueLength=v.length*8;
	}

	/**
	 * @param	v
	 * @param	big
	 * @throws	DicomException
	 */
	public void setValues(byte[] v,boolean big) throws DicomException {
		int doubleLength = v.length/8;
		double[] doubleValues = new double[doubleLength];
		int j = 0;
		for (int i=0; i<doubleLength; ++i) {
			long v1 =  ((long)v[j++])&0xff;
			long v2 =  ((long)v[j++])&0xff;
			long v3 =  ((long)v[j++])&0xff;
			long v4 =  ((long)v[j++])&0xff;
			long v5 =  ((long)v[j++])&0xff;
			long v6 =  ((long)v[j++])&0xff;
			long v7 =  ((long)v[j++])&0xff;
			long v8 =  ((long)v[j++])&0xff;
			doubleValues[i] = Double.longBitsToDouble(big
				? (((((((((((((v1 << 8) | v2) << 8) | v3) << 8) | v4) << 8) | v5) << 8) | v6) << 8) | v7) << 8) | v8
				: (((((((((((((v8 << 8) | v7) << 8) | v6) << 8) | v5) << 8) | v4) << 8) | v3) << 8) | v2) << 8) | v1);
		}
		setValues(doubleValues);
	}

	/**
	 */
	public void removeValues() {
		values=null;
		valueMultiplicity=0;
		valueLength=0;
	}

	/**
	 * @throws	DicomException
	 */
	public double[] getDoubleValues() throws DicomException { return values; }

	/**
	 * @param	big
	 * @throws	DicomException
	 */
	public byte[] getByteValues(boolean big) throws DicomException {
		byte[] byteValues = null;
		if (values != null) {
			int doubleLength = values.length;
			byteValues = new byte[doubleLength*8];
			int j = 0;
			if (big) {
				for (int i=0; i<doubleLength; ++i) {
					long v = Double.doubleToRawLongBits(values[i]);
					byteValues[j++]=(byte)(v>>56);
					byteValues[j++]=(byte)(v>>48);
					byteValues[j++]=(byte)(v>>40);
					byteValues[j++]=(byte)(v>>32);
					byteValues[j++]=(byte)(v>>24);
					byteValues[j++]=(byte)(v>>16);
					byteValues[j++]=(byte)(v>>8);
					byteValues[j++]=(byte)v;
				}
			}
			else {
				for (int i=0; i<doubleLength; ++i) {
					long v = Double.doubleToRawLongBits(values[i]);
					byteValues[j++]=(byte)v;
					byteValues[j++]=(byte)(v>>8);
					byteValues[j++]=(byte)(v>>16);
					byteValues[j++]=(byte)(v>>24);
					byteValues[j++]=(byte)(v>>32);
					byteValues[j++]=(byte)(v>>40);
					byteValues[j++]=(byte)(v>>48);
					byteValues[j++]=(byte)(v>>56);
				}
			}
		}
		return byteValues;
	}

	/**
	 * <p>Get the value representation of this attribute (OD).</p>
	 *
	 * @return	'O','D' in ASCII as a two byte array; see {@link ValueRepresentation ValueRepresentation}
	 */
	public byte[] getVR() { return ValueRepresentation.OD; }

}

