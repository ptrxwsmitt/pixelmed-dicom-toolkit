/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

import java.text.NumberFormat;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A concrete class specializing {@link Attribute Attribute} for
 * Unknown (UN) attributes.</p>
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
public class UnknownAttribute extends Attribute {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/UnknownAttribute.java,v 1.29 2022/01/21 19:51:19 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(UnknownAttribute.class);
	
	protected byte[] originalLittleEndianByteValues;

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * @param	t	the tag of the attribute
	 */
	public UnknownAttribute(AttributeTag t) {
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
	public UnknownAttribute(AttributeTag t,long vl,DicomInputStream i) throws IOException, DicomException {
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
	public UnknownAttribute(AttributeTag t,Long vl,DicomInputStream i) throws IOException, DicomException {
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
		valueLength=vl;
		valueMultiplicity=1;
		try {
			//i.skipInsistently(vl);
			originalLittleEndianByteValues = new byte[(int)vl];
			i.readInsistently(originalLittleEndianByteValues,0,(int)vl);
		}
		catch (IOException e) {
			//throw new DicomException("Failed to skip value (length "+vl+" dec) in UN attribute "+getTag());
			throw new DicomException("Failed to read value (length "+vl+" dec) in UN attribute "+getTag());
		}
	}

	/**
	 * @param	o
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public void write(DicomOutputStream o) throws DicomException, IOException {
		writeBase(o);
		o.write(originalLittleEndianByteValues);
	}
	
	/***/
	public String toString(DicomDictionary dictionary) {
		return super.toString(dictionary)+" "+getVR()+" ";
	}

	/**
	 */
	public void removeValues() {
		valueMultiplicity=0;
		valueLength=0;
	}

	/**
	 * <p>Get the value representation of this attribute (UN).</p>
	 *
	 * @return	'U','U' in ASCII as a two byte array; see {@link ValueRepresentation ValueRepresentation}
	 */
	public byte[] getVR() { return ValueRepresentation.UN; }

	/**
	 * @param	v
	 * @throws	DicomException
	 */
	public void setValues(byte[] v) throws DicomException {
		originalLittleEndianByteValues=v;
		valueMultiplicity=1;		// different from normal value types where VM is size of array
		valueLength=v.length;
	}
	
	/**
	 * @param	v
	 * @param	big
	 * @throws	DicomException
	 */
	public void setValues(byte[] v,boolean big) throws DicomException {
		if (big) throw new DicomException("Setting UN VR bytes from big endian not supported");
		setValues(v);
	}

	/**
	 * <p>Get the values of this attribute as a byte array.</p>
	 *
	 * <p>Always to be interpreted as little endian, per the DICOM definition of UN, regardless of the received transfer syntax.</p>
	 *
	 * @return			the values as an array of bytes
	 */
	public byte[] getByteValues() {
		return originalLittleEndianByteValues;
	}

	/**
	 * @throws	DicomException
	 */
	public byte[] getByteValues(boolean big) throws DicomException  {
		if (big) throw new DicomException("Returning UN VR bytes in big endian not supported");
		return originalLittleEndianByteValues;
	}

	/**
	 * <p>Get the values of this attribute as strings.</p>
	 *
	 * <p>Assumes the caller knows that the UN VR is really a valid string (e.g., knows the VR of a private attribute).</p>
	 *
	 * <p>Assumes ASCII encoding (i.e., does not consider SpecificCharacterSet).</p>
	 *
	 * <p>The strings are first cleaned up into a canonical form, to remove leading and trailing padding.</p>
	 *
	 * @param	format		the format to use for each numerical or decimal value - ignored
	 * @return			the values as an array of {@link String String}
	 * @throws	DicomException	not thrown
	 */
	public String[] getStringValues(NumberFormat format) throws DicomException {
		String[] values = null;
		// ignore number format for generic string attributes
		// should really check for SpecificCharacterSet rather than using default encoding :(
		try {
			String[] originals = { new String(originalLittleEndianByteValues) };
			values = ArrayCopyUtilities.copyStringArrayRemovingLeadingAndTrailingPadding(originals);
		}
		catch (Exception e) {
		}
		return values;
	}

	/**
	 * <p>Get the values of this attribute as doubles.</p>
	 *
	 * <p>Assumes the caller knows that the UN VR is really a valid FD (e.g., knows the VR of a private attribute).</p>
	 *
	 * @throws	DicomException
	 */
	public double[] getDoubleValues() throws DicomException {
		double[] values = null;
		if (valueLength%FloatDoubleAttribute.bytesPerValue != 0) {
			throw new DicomException("incorrect value length ("+valueLength+" dec) for UN as FD");
		}
		else {
			try {
				BinaryInputStream ibs = new BinaryInputStream(new ByteArrayInputStream(originalLittleEndianByteValues),false/*big endian*/);
				int vm=(int)(valueLength/FloatDoubleAttribute.bytesPerValue);
				values=new double[vm];;
				for (int j=0; j<vm; ++j) {
					values[j] = ibs.readDouble();
				}
			}
			catch (IOException e) {
				slf4jlogger.error("", e);
				throw new DicomException(e.toString());
			}
		}
		return values;
	}

	/**
	 * <p>Get the values of this attribute as floats.</p>
	 *
	 * <p>Assumes the caller knows that the UN VR is really a valid FL (e.g., knows the VR of a private attribute).</p>
	 *
	 * @throws	DicomException
	 */
	public float[] getFloatValues() throws DicomException {
		float[] values = null;
		if (valueLength%FloatSingleAttribute.bytesPerValue != 0) {
			throw new DicomException("incorrect value length ("+valueLength+" dec) for UN as FD");
		}
		else {
			try {
				BinaryInputStream ibs = new BinaryInputStream(new ByteArrayInputStream(originalLittleEndianByteValues),false/*big endian*/);
				int vm=(int)(valueLength/FloatSingleAttribute.bytesPerValue);
				values=new float[vm];;
				for (int j=0; j<vm; ++j) {
					values[j] = ibs.readFloat();
				}
			}
			catch (IOException e) {
				slf4jlogger.error("", e);
				throw new DicomException(e.toString());
			}
		}
		return values;
	}

}

