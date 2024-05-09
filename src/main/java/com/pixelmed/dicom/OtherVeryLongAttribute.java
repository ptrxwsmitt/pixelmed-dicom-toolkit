/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

/**
 * <p>A concrete class specializing {@link Attribute Attribute} for
 * Other Very Long (OV) attributes.</p>
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
public class OtherVeryLongAttribute extends Attribute {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/OtherVeryLongAttribute.java,v 1.5 2022/01/21 19:51:17 dclunie Exp $";

	private long[] values;

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * @param	t	the tag of the attribute
	 */
	public OtherVeryLongAttribute(AttributeTag t) {
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
	public OtherVeryLongAttribute(AttributeTag t,long vl,DicomInputStream i) throws IOException, DicomException {
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
	public OtherVeryLongAttribute(AttributeTag t,Long vl,DicomInputStream i) throws IOException, DicomException {
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
			long buffer[] = new long[len];
			i.readUnsigned64(buffer,len);
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
			o.writeUnsigned64(values,values.length);
			if (getVL() != values.length*8) {
				throw new DicomException("Internal error - int array length ("+values.length*8+") not equal to expected VL("+getVL()+")");
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
	public void setValues(long[] v) throws DicomException {
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
		int longLength = v.length/8;
		long[] longValues = new long[longLength];
		int j = 0;
		for (int i=0; i<longLength; ++i) {
			long v1 =  ((long)v[j++])&0xff;
			long v2 =  ((long)v[j++])&0xff;
			long v3 =  ((long)v[j++])&0xff;
			long v4 =  ((long)v[j++])&0xff;
			long v5 =  ((long)v[j++])&0xff;
			long v6 =  ((long)v[j++])&0xff;
			long v7 =  ((long)v[j++])&0xff;
			long v8 =  ((long)v[j++])&0xff;
			longValues[i] = (long) (big
				? (((((((((((((v1 << 8) | v2) << 8) | v3) << 8) | v4) << 8) | v5) << 8) | v6) << 8) | v7) << 8) | v8
				: (((((((((((((v8 << 8) | v7) << 8) | v6) << 8) | v5) << 8) | v4) << 8) | v3) << 8) | v2) << 8) | v1);
		}
		setValues(longValues);
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
	public long[] getLongValues() throws DicomException { return values; }

	/**
	 * @param	big
	 * @throws	DicomException
	 */
	public byte[] getByteValues(boolean big) throws DicomException {
		byte[] byteValues = null;
		if (values != null) {
			int longLength = values.length;
			byteValues = new byte[longLength*8];
			int j = 0;
			if (big) {
				for (int i=0; i<longLength; ++i) {
					long v = values[i];
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
				for (int i=0; i<longLength; ++i) {
					long v = values[i];
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
	 * <p>Get the value representation of this attribute (OV).</p>
	 *
	 * @return	'O','V' in ASCII as a two byte array; see {@link ValueRepresentation ValueRepresentation}
	 */
	public byte[] getVR() { return ValueRepresentation.OV; }

}

