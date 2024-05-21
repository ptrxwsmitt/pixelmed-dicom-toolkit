/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

/**
 * <p>A concrete class specializing {@link Attribute Attribute} for
 * Other Long (OL) attributes.</p>
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
public class OtherLongAttribute extends Attribute {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/OtherLongAttribute.java,v 1.9 2022/01/21 19:51:17 dclunie Exp $";

	private int[] values;

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * @param	t	the tag of the attribute
	 */
	public OtherLongAttribute(AttributeTag t) {
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
	public OtherLongAttribute(AttributeTag t,long vl,DicomInputStream i) throws IOException, DicomException {
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
	public OtherLongAttribute(AttributeTag t,Long vl,DicomInputStream i) throws IOException, DicomException {
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
			int len = (int)(vl/4);
			int buffer[] = new int[len];
			i.readUnsigned32(buffer,len);
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
			o.writeUnsigned32(values,values.length);
			if (getVL() != values.length*4) {
				throw new DicomException("Internal error - int array length ("+values.length*4+") not equal to expected VL("+getVL()+")");
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
	public void setValues(int[] v) throws DicomException {
		values=v;
		valueMultiplicity=1;		// different from normal value types where VM is size of array
		valueLength=v.length*4;
	}
	
	/**
	 * @param	v
	 * @param	big
	 * @throws	DicomException
	 */
	public void setValues(byte[] v,boolean big) throws DicomException {
		int intLength = v.length/4;
		int[] intValues = new int[intLength];
		int j = 0;
		for (int i=0; i<intLength; ++i) {
			int v1 =  (int)(v[j++]&0xff);
			int v2 =  (int)(v[j++]&0xff);
			int v3 =  (int)(v[j++]&0xff);
			int v4 =  (int)(v[j++]&0xff);
			intValues[i] = (int) (big
				? (((((v1 << 8) | v2) << 8) | v3) << 8) | v4
				: (((((v4 << 8) | v3) << 8) | v2) << 8) | v1);
		}
		setValues(intValues);
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
	public int[] getIntegerValues() throws DicomException { return values; }

	/**
	 * @param	big
	 * @throws	DicomException
	 */
	public byte[] getByteValues(boolean big) throws DicomException {
		byte[] byteValues = null;
		if (values != null) {
			int intLength = values.length;
			byteValues = new byte[intLength*4];
			int j = 0;
			if (big) {
				for (int i=0; i<intLength; ++i) {
					int v = values[i];
					byteValues[j++]=(byte)(v>>24);
					byteValues[j++]=(byte)(v>>16);
					byteValues[j++]=(byte)(v>>8);
					byteValues[j++]=(byte)v;
				}
			}
			else {
				for (int i=0; i<intLength; ++i) {
					int v = values[i];
					byteValues[j++]=(byte)v;
					byteValues[j++]=(byte)(v>>8);
					byteValues[j++]=(byte)(v>>16);
					byteValues[j++]=(byte)(v>>24);
				}
			}
		}
		return byteValues;
	}

	/**
	 * <p>Get the value representation of this attribute (OL).</p>
	 *
	 * @return	'O','L' in ASCII as a two byte array; see {@link ValueRepresentation ValueRepresentation}
	 */
	public byte[] getVR() { return ValueRepresentation.OL; }

}

