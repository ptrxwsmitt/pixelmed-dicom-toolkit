/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

/**
 * <p>A concrete class specializing {@link Attribute Attribute} for
 * Other Word (OW) attributes.</p>
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
public class OtherWordAttribute extends Attribute {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/OtherWordAttribute.java,v 1.24 2022/01/21 19:51:17 dclunie Exp $";

	private short[] values;

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * @param	t	the tag of the attribute
	 */
	public OtherWordAttribute(AttributeTag t) {
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
	public OtherWordAttribute(AttributeTag t,long vl,DicomInputStream i) throws IOException, DicomException {
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
	public OtherWordAttribute(AttributeTag t,Long vl,DicomInputStream i) throws IOException, DicomException {
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
			int len = (int)(vl/2);
			short buffer[] = new short[len];
			i.readUnsigned16(buffer,len);
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
			o.writeUnsigned16(values,values.length);
			if (getVL() != values.length*2) {
				throw new DicomException("Internal error - short array length ("+values.length*2+") not equal to expected VL("+getVL()+")");
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
	 * @param	big
	 * @throws	DicomException
	 */
	public void setValues(byte[] v,boolean big) throws DicomException {
		int shortLength = v.length/2;
		short[] shortValues = new short[shortLength];
		int j = 0;
		for (int i=0; i<shortLength; ++i) {
			short v1 =  (short)(v[j++]&0xff);
			short v2 =  (short)(v[j++]&0xff);
			shortValues[i] = (short) (big
				? (v1 << 8) | v2
				: (v2 << 8) | v1);
		}
		setValues(shortValues);
	}

	/**
	 * @param	v
	 * @throws	DicomException
	 */
	public void setValues(short[] v) throws DicomException {
		values=v;
		valueMultiplicity=1;		// different from normal value types where VM is size of array
		valueLength=v.length*2;
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
	public short[] getShortValues() throws DicomException { return values; }

	/**
	 * @param	big
	 * @throws	DicomException
	 */
	public byte[] getByteValues(boolean big) throws DicomException {
		byte[] byteValues = null;
		if (values != null) {
			int shortLength = values.length;
			byteValues = new byte[shortLength*2];
			int j = 0;
			if (big) {
				for (int i=0; i<shortLength; ++i) {
					short v = values[i];
					byteValues[j++]=(byte)(v>>8);
					byteValues[j++]=(byte)v;
				}
			}
			else {
				for (int i=0; i<shortLength; ++i) {
					short v = values[i];
					byteValues[j++]=(byte)v;
					byteValues[j++]=(byte)(v>>8);
				}
			}
		}
		return byteValues;
	}

	/**
	 * <p>Get the value representation of this attribute (OW).</p>
	 *
	 * @return	'O','W' in ASCII as a two byte array; see {@link ValueRepresentation ValueRepresentation}
	 */
	public byte[] getVR() { return ValueRepresentation.OW; }

}

