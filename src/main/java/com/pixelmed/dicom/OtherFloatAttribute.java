/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

/**
 * <p>A concrete class specializing {@link Attribute Attribute} for
 * Other Float (OF) attributes.</p>
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
public class OtherFloatAttribute extends Attribute {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/OtherFloatAttribute.java,v 1.19 2022/03/06 16:18:05 dclunie Exp $";

	private float[] values;

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * @param	t	the tag of the attribute
	 */
	public OtherFloatAttribute(AttributeTag t) {
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
	public OtherFloatAttribute(AttributeTag t,long vl,DicomInputStream i) throws IOException, DicomException {
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
	public OtherFloatAttribute(AttributeTag t,Long vl,DicomInputStream i) throws IOException, DicomException {
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
			float buffer[] = new float[len];
			i.readFloat(buffer,len);
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
			o.writeFloat(values,values.length);
			if (getVL() != values.length*4) {
				throw new DicomException("Internal error - float array length ("+values.length*4+") not equal to expected VL("+getVL()+")");
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
	public void setValues(float[] v) throws DicomException {
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
		int floatLength = v.length/4;
		float[] floatValues = new float[floatLength];
		int j = 0;
		for (int i=0; i<floatLength; ++i) {
			int v1 =  (int)(v[j++]&0xff);
			int v2 =  (int)(v[j++]&0xff);
			int v3 =  (int)(v[j++]&0xff);
			int v4 =  (int)(v[j++]&0xff);
			floatValues[i] =  Float.intBitsToFloat(big
				? (((((v1 << 8) | v2) << 8) | v3) << 8) | v4
				: (((((v4 << 8) | v3) << 8) | v2) << 8) | v1);
		}
		setValues(floatValues);
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
	public float[] getFloatValues() throws DicomException { return values; }

	/**
	 * @param	big
	 * @throws	DicomException
	 */
	public byte[] getByteValues(boolean big) throws DicomException {
		byte[] byteValues = null;
		if (values != null) {
			int floatLength = values.length;
			byteValues = new byte[floatLength*4];
			int j = 0;
			if (big) {
				for (int i=0; i<floatLength; ++i) {
					int v = Float.floatToRawIntBits(values[i]);
					byteValues[j++]=(byte)(v>>24);
					byteValues[j++]=(byte)(v>>16);
					byteValues[j++]=(byte)(v>>8);
					byteValues[j++]=(byte)v;
				}
			}
			else {
				for (int i=0; i<floatLength; ++i) {
					int v = Float.floatToRawIntBits(values[i]);
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
	 * <p>Get the value representation of this attribute (OF).</p>
	 *
	 * @return	'O','F' in ASCII as a two byte array; see {@link ValueRepresentation ValueRepresentation}
	 */
	public byte[] getVR() { return ValueRepresentation.OF; }

}

