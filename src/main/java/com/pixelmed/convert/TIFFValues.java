package com.pixelmed.convert;

import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.ArrayList;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

public class TIFFValues {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/convert/TIFFValues.java,v 1.2 2021/06/17 17:12:43 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(TIFFValues.class);

	public static class TIFFUndefinedValues extends TIFFValues {
		byte[] values;
		
		TIFFUndefinedValues(byte[] values) {
			this.values = values;
		}
		
		public static TIFFUndefinedValues extractValues(byte[] buffer,int numberOfValues) {
			byte[] values = new byte[numberOfValues];
			System.arraycopy(buffer,0,values,0,numberOfValues);
			return new TIFFUndefinedValues(values);
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			String prefix = "";
			for (byte value : values) {
				buf.append(prefix);
				buf.append(((int)value)&0x00ff);
				prefix = " ";
			}
			return buf.toString();
		}
	
		public long getSingleNumericValue(int valueNumber,long defaultValue) {
			long returnValue = defaultValue;
			if (valueNumber < values.length) {
				returnValue = ((long)values[valueNumber]) & 0x00000000000000ffl;	// make sure they are unsigned
			}
			return returnValue;
		}
	
		public long[] getNumericValues() {
			long[] returnValues = null;
			if (values.length > 0) {
				returnValues = new long[values.length];
				for (int i=0; i<values.length; ++i) {
					returnValues[i] = ((long)values[i]) & 0x00000000000000ffl;	// make sure they are unsigned
				}
			}
			return returnValues;
		}
		
		public byte[] getByteValues() {
			return values;
		}
	}

	public static class TIFFByteValues extends TIFFValues {
		byte[] values;
		
		TIFFByteValues(byte[] values) {
			this.values = values;
		}
		
		public static TIFFByteValues extractValues(byte[] buffer,int numberOfValues) {
			byte[] values = new byte[numberOfValues];
			System.arraycopy(buffer,0,values,0,numberOfValues);
			return new TIFFByteValues(values);
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			String prefix = "";
			for (byte value : values) {
				buf.append(prefix);
				buf.append(((int)value)&0x00ff);
				prefix = " ";
			}
			return buf.toString();
		}
	
		public long getSingleNumericValue(int valueNumber,long defaultValue) {
			long returnValue = defaultValue;
			if (valueNumber < values.length) {
				returnValue = ((long)values[valueNumber]) & 0x00000000000000ffl;	// make sure they are unsigned
			}
			return returnValue;
		}
	
		public long[] getNumericValues() {
			long[] returnValues = null;
			if (values.length > 0) {
				returnValues = new long[values.length];
				for (int i=0; i<values.length; ++i) {
					returnValues[i] = ((long)values[i]) & 0x00000000000000ffl;	// make sure they are unsigned
				}
			}
			return returnValues;
		}
		
		public byte[] getByteValues() {
			return values;
		}
	}

	public static class TIFFSByteValues extends TIFFValues {
		byte[] values;
		
		TIFFSByteValues(byte[] values) {
			this.values = values;
		}
		
		public static TIFFSByteValues extractValues(byte[] buffer,int numberOfValues) {
			byte[] values = new byte[numberOfValues];
			System.arraycopy(buffer,0,values,0,numberOfValues);
			return new TIFFSByteValues(values);
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			String prefix = "";
			for (byte value : values) {
				buf.append(prefix);
				int svalue = ((int)value)&0x000000ff;
				if ((svalue & 0x00000080) == 0x00000080) {
					svalue |= 0xffffff80;	// sign extend -ve bytes
				}
				buf.append(svalue);
				prefix = " ";
			}
			return buf.toString();
		}
	
		public long getSingleNumericValue(int valueNumber,long defaultValue) {
			long returnValue = defaultValue;
			if (valueNumber < values.length) {
				returnValue = ((long)values[valueNumber]) & 0x00000000000000ffl;
				if ((returnValue & 0x0000000000000080l) == 0x0000000000000080l) {
					returnValue |= 0xffffffffffffff80l;	// sign extend -ve bytes
				}
			}
			return returnValue;
		}
	
		public long[] getNumericValues() {
			long[] returnValues = null;
			if (values.length > 0) {
				returnValues = new long[values.length];
				for (int i=0; i<values.length; ++i) {
					long returnValue = ((long)values[i]) & 0x00000000000000ffl;
					if ((returnValue & 0x0000000000000080l) == 0x0000000000000080l) {
						returnValue |= 0xffffffffffffff80l;	// sign extend -ve bytes
					}
					returnValues[i] = returnValue;
				}
			}
			return returnValues;
		}
		
		public byte[] getByteValues() {
			return values;
		}
	}

	public static class TIFFAsciiValues extends TIFFValues {
		String[] values;
		
		TIFFAsciiValues(String[] values) {
			this.values = values;
		}
		
		public static TIFFAsciiValues extractValues(byte[] buffer,int numberOfValues) {
			String[] values = null;
			try {
				ArrayList<String> strings = new ArrayList<String>();
				int lng;
				if (buffer != null && (lng=buffer.length) > 0) {
					slf4jlogger.debug("TIFFAsciiValues.extractValues(): have buffer content");
					// extract multiple strings separated by null
					int start = 0;
					int next = 0;
					while (next < lng) {
						if (buffer[next] == 0) {
							String s = new String(buffer,start,next-start,"UTF-8");	// ?? should be just "US-ASCII" - OME-TIFF puts XML in ImageDescription :( (001285)
							strings.add(s);
							++next;
							start = next;
						}
						else {
							++next;
						}
					}
					values = strings.toArray(new String[strings.size()]);
				}
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace(System.err);
			}
			return new TIFFAsciiValues(values);
		}

		public String[] getStringValues() {
			return values;
		}

		public String toString() {
			StringBuffer buf = new StringBuffer();
			for (String value : values) {
				buf.append(value);
				buf.append("\n");	// use this as delimiter for now :(
			}
			return buf.toString();
		}
	}

	public static class TIFFShortValues extends TIFFValues {
		int[] values;
		
		TIFFShortValues(int[] values) {
			this.values = values;
		}
		
		public static TIFFShortValues extractValues(byte[] buffer,ByteOrder useByteOrder,int numberOfValues) {
			int[] values = new int[numberOfValues];
			int numberOfBytes = 2;
			int offset = 0;
			for (int i=0; i<numberOfValues; ++i) {
				values[i] = ((int)ByteBuffer.wrap(buffer,offset,numberOfBytes).order(useByteOrder).getShort()) & 0x0000ffff;			// always unsigned
				offset += numberOfBytes;
			}
			return new TIFFShortValues(values);
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			String prefix = "";
			for (int value : values) {
				buf.append(prefix);
				buf.append(value);
				prefix = " ";
			}
			return buf.toString();
		}
	
		public long getSingleNumericValue(int valueNumber,long defaultValue) {
			long returnValue = defaultValue;
			if (valueNumber < values.length) {
				returnValue = values[valueNumber];
			}
			return returnValue;
		}
	
		public long[] getNumericValues() {
			long[] returnValues = null;
			if (values.length > 0) {
				returnValues = new long[values.length];
				for (int i=0; i<values.length; ++i) {
					returnValues[i] = values[i];
				}
			}
			return returnValues;
		}
	}

	public static class TIFFSShortValues extends TIFFValues {
		int[] values;
		
		TIFFSShortValues(int[] values) {
			this.values = values;
		}
		
		public static TIFFSShortValues extractValues(byte[] buffer,ByteOrder useByteOrder,int numberOfValues) {
			int[] values = new int[numberOfValues];
			int numberOfBytes = 2;
			int offset = 0;
			for (int i=0; i<numberOfValues; ++i) {
				int value = ((int)ByteBuffer.wrap(buffer,offset,numberOfBytes).order(useByteOrder).getShort());
				if ((value & 0x00008000) == 0x00008000) {
					value |= 0xffff8000;	// sign extend -ve
				}
				values[i] = value;
				offset += numberOfBytes;
			}
			return new TIFFSShortValues(values);
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			String prefix = "";
			for (int value : values) {
				buf.append(prefix);
				buf.append(value);
				prefix = " ";
			}
			return buf.toString();
		}
	
		public long getSingleNumericValue(int valueNumber,long defaultValue) {
			long returnValue = defaultValue;
			if (valueNumber < values.length) {
				returnValue = values[valueNumber];
			}
			return returnValue;
		}
	
		public long[] getNumericValues() {
			long[] returnValues = null;
			if (values.length > 0) {
				returnValues = new long[values.length];
				for (int i=0; i<values.length; ++i) {
					returnValues[i] = values[i];
				}
			}
			return returnValues;
		}
	}

	public static class TIFFLongValues extends TIFFValues {
		long[] values;
		
		TIFFLongValues(long[] values) {
			this.values = values;
		}
		
		public static TIFFLongValues extractValues(byte[] buffer,ByteOrder useByteOrder,int numberOfValues) {
			long[] values = new long[numberOfValues];
			int numberOfBytes = 4;
			int offset = 0;
			for (int i=0; i<numberOfValues; ++i) {
				values[i] = ((long)ByteBuffer.wrap(buffer,offset,numberOfBytes).order(useByteOrder).getInt()) & 0x00000000ffffffffl;			// always unsigned
				offset += numberOfBytes;
			}
			return new TIFFLongValues(values);
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			String prefix = "";
			for (long value : values) {
				buf.append(prefix);
				buf.append(value);
				prefix = " ";
			}
			return buf.toString();
		}
	
		public long getSingleNumericValue(int valueNumber,long defaultValue) {
			long returnValue = defaultValue;
			if (valueNumber < values.length) {
				returnValue = values[valueNumber];
			}
			return returnValue;
		}
	
		public long[] getNumericValues() {
			return values;
		}
	}

	public static class TIFFSLongValues extends TIFFValues {
		long[] values;
		
		TIFFSLongValues(long[] values) {
			this.values = values;
		}
		
		public static TIFFSLongValues extractValues(byte[] buffer,ByteOrder useByteOrder,int numberOfValues) {
			long[] values = new long[numberOfValues];
			int numberOfBytes = 4;
			int offset = 0;
			for (int i=0; i<numberOfValues; ++i) {
				long value = ((long)ByteBuffer.wrap(buffer,offset,numberOfBytes).order(useByteOrder).getInt()) & 0x00000000ffffffffl;
				if ((value & 0x0000000080000000l) == 0x0000000080000000l) {
					value |= 0xffffffff80000000l;	// sign extend -ve
				}
				values[i] = value;
				offset += numberOfBytes;
			}
			return new TIFFSLongValues(values);
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			String prefix = "";
			for (long value : values) {
				buf.append(prefix);
				buf.append(value);
				prefix = " ";
			}
			return buf.toString();
		}
	
		public long getSingleNumericValue(int valueNumber,long defaultValue) {
			long returnValue = defaultValue;
			if (valueNumber < values.length) {
				returnValue = values[valueNumber];
			}
			return returnValue;
		}
	
		public long[] getNumericValues() {
			return values;
		}
	}

	public static class TIFFRationalValues extends TIFFValues {
		long[] numeratorValues;
		long[] denominatorValues;
		
		TIFFRationalValues(long[] numeratorValues,long[] denominatorValues) {
			this.numeratorValues = numeratorValues;
			this.denominatorValues = denominatorValues;
		}
		
		public static TIFFRationalValues extractValues(byte[] buffer,ByteOrder useByteOrder,int numberOfValues) {
			long[] numeratorValues = new long[numberOfValues];
			long[] denominatorValues = new long[numberOfValues];
			int numberOfBytes = 4;
			int offset = 0;
			for (int i=0; i<numberOfValues; ++i) {
				numeratorValues[i] = ((long)ByteBuffer.wrap(buffer,offset,numberOfBytes).order(useByteOrder).getInt()) & 0x00000000ffffffffl;			// always unsigned
				offset += numberOfBytes;
				denominatorValues[i] = ((long)ByteBuffer.wrap(buffer,offset,numberOfBytes).order(useByteOrder).getInt()) & 0x00000000ffffffffl;			// always unsigned
				offset += numberOfBytes;
			}
			return new TIFFRationalValues(numeratorValues,denominatorValues);
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			String prefix = "";
			for (int i=0; i<numeratorValues.length; ++i) {
				buf.append(prefix);
				buf.append(numeratorValues[i]);
				buf.append("/");
				buf.append(denominatorValues[i]);
				prefix = " ";
			}
			return buf.toString();
		}
	
		public long getSingleNumericValue(int valueNumber,long defaultValue) {
			long returnValue = defaultValue;
			if (valueNumber < denominatorValues.length && denominatorValues[valueNumber] != 0) {
				returnValue = (long)(((double)numeratorValues[valueNumber]) / denominatorValues[valueNumber]);
			}
			return returnValue;
		}
	}

	public static class TIFFSRationalValues extends TIFFValues {
		long[] numeratorValues;
		long[] denominatorValues;
		
		TIFFSRationalValues(long[] numeratorValues,long[] denominatorValues) {
			this.numeratorValues = numeratorValues;
			this.denominatorValues = denominatorValues;
		}
		
		public static TIFFSRationalValues extractValues(byte[] buffer,ByteOrder useByteOrder,int numberOfValues) {
			long[] numeratorValues = new long[numberOfValues];
			long[] denominatorValues = new long[numberOfValues];
			int numberOfBytes = 4;
			int offset = 0;
			for (int i=0; i<numberOfValues; ++i) {
				long numeratorValue = ((long)ByteBuffer.wrap(buffer,offset,numberOfBytes).order(useByteOrder).getInt()) & 0x00000000ffffffffl;
				if ((numeratorValue & 0x0000000080000000l) == 0x0000000080000000l) {
					numeratorValue |= 0xffffffff80000000l;	// sign extend -ve
				}
				numeratorValues[i] = numeratorValue;
				offset += numberOfBytes;
				long denominatorValue = ((long)ByteBuffer.wrap(buffer,offset,numberOfBytes).order(useByteOrder).getInt()) & 0x00000000ffffffffl;
				if ((denominatorValue & 0x0000000080000000l) == 0x0000000080000000l) {
					denominatorValue |= 0xffffffff80000000l;	// sign extend -ve
				}
				denominatorValues[i] = denominatorValue;
				offset += numberOfBytes;
			}
			return new TIFFSRationalValues(numeratorValues,denominatorValues);
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			String prefix = "";
			for (int i=0; i<numeratorValues.length; ++i) {
				buf.append(prefix);
				buf.append(numeratorValues[i]);
				buf.append("/");
				buf.append(denominatorValues[i]);
				prefix = " ";
			}
			return buf.toString();
		}
	
		public long getSingleNumericValue(int valueNumber,long defaultValue) {
			long returnValue = defaultValue;
			if (valueNumber < denominatorValues.length && denominatorValues[valueNumber] != 0) {
				returnValue = (long)(((double)numeratorValues[valueNumber]) / denominatorValues[valueNumber]);
			}
			return returnValue;
		}
	}

	public static class TIFFIfdValues extends TIFFValues {
		long[] values;
		
		TIFFIfdValues(long[] values) {
			this.values = values;
		}
		
		public static TIFFIfdValues extractValues(byte[] buffer,ByteOrder useByteOrder,int numberOfValues) {
			long[] values = new long[numberOfValues];
			int numberOfBytes = 4;
			int offset = 0;
			for (int i=0; i<numberOfValues; ++i) {
				values[i] = ((long)ByteBuffer.wrap(buffer,offset,numberOfBytes).order(useByteOrder).getInt()) & 0x00000000ffffffffl;			// always unsigned
				offset += numberOfBytes;
			}
			return new TIFFIfdValues(values);
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			String prefix = "";
			for (long value : values) {
				buf.append(prefix);
				buf.append(value);
				prefix = " ";
			}
			return buf.toString();
		}
	
		public long getSingleNumericValue(int valueNumber,long defaultValue) {
			long returnValue = defaultValue;
			if (valueNumber < values.length) {
				returnValue = values[valueNumber];
			}
			return returnValue;
		}
	
		public long[] getNumericValues() {
			return values;
		}
	}

	public static class TIFFLong8Values extends TIFFValues {
		long[] values;
		
		TIFFLong8Values(long[] values) {
			this.values = values;
		}
		
		public static TIFFLong8Values extractValues(byte[] buffer,ByteOrder useByteOrder,int numberOfValues) {
			long[] values = new long[numberOfValues];
			int numberOfBytes = 8;
			int offset = 0;
			for (int i=0; i<numberOfValues; ++i) {
				values[i] = ByteBuffer.wrap(buffer,offset,numberOfBytes).order(useByteOrder).getLong();
				offset += numberOfBytes;
			}
			return new TIFFLong8Values(values);
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			String prefix = "";
			for (long value : values) {
				buf.append(prefix);
				buf.append(value);
				prefix = " ";
			}
			return buf.toString();
		}
	
		public long getSingleNumericValue(int valueNumber,long defaultValue) {
			long returnValue = defaultValue;
			if (valueNumber < values.length) {
				returnValue = values[valueNumber];
			}
			return returnValue;
		}
	
		public long[] getNumericValues() {
			return values;
		}
	}

	public static class TIFFSlong8Values extends TIFFValues {
		long[] values;
		
		TIFFSlong8Values(long[] values) {
			this.values = values;
		}
		
		public static TIFFSlong8Values extractValues(byte[] buffer,ByteOrder useByteOrder,int numberOfValues) {
			long[] values = new long[numberOfValues];
			int numberOfBytes = 8;
			int offset = 0;
			for (int i=0; i<numberOfValues; ++i) {
				values[i] = ByteBuffer.wrap(buffer,offset,numberOfBytes).order(useByteOrder).getLong();
				offset += numberOfBytes;
			}
			return new TIFFSlong8Values(values);
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			String prefix = "";
			for (long value : values) {
				buf.append(prefix);
				buf.append(value);
				prefix = " ";
			}
			return buf.toString();
		}
	
		public long getSingleNumericValue(int valueNumber,long defaultValue) {
			long returnValue = defaultValue;
			if (valueNumber < values.length) {
				returnValue = values[valueNumber];
			}
			return returnValue;
		}
	
		public long[] getNumericValues() {
			return values;
		}
	}

	public static class TIFFIfd8Values extends TIFFValues {
		long[] values;
		
		TIFFIfd8Values(long[] values) {
			this.values = values;
		}
		
		public static TIFFIfd8Values extractValues(byte[] buffer,ByteOrder useByteOrder,int numberOfValues) {
			long[] values = new long[numberOfValues];
			int numberOfBytes = 8;
			int offset = 0;
			for (int i=0; i<numberOfValues; ++i) {
				values[i] = ByteBuffer.wrap(buffer,offset,numberOfBytes).order(useByteOrder).getLong();
				offset += numberOfBytes;
			}
			return new TIFFIfd8Values(values);
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			String prefix = "";
			for (long value : values) {
				buf.append(prefix);
				buf.append(value);
				prefix = " ";
			}
			return buf.toString();
		}
	
		public long getSingleNumericValue(int valueNumber,long defaultValue) {
			long returnValue = defaultValue;
			if (valueNumber < values.length) {
				returnValue = values[valueNumber];
			}
			return returnValue;
		}
	
		public long[] getNumericValues() {
			return values;
		}
	}

	public static TIFFValues extractValues(int tagType,byte[] buffer,ByteOrder useByteOrder,int numberOfValues) {
		TIFFValues values = null;
		switch (tagType) {
			case TIFFTypes.BYTE:		values = TIFFByteValues     .extractValues(buffer,numberOfValues); break;
			case TIFFTypes.ASCII:		values = TIFFAsciiValues    .extractValues(buffer,numberOfValues); break;
			case TIFFTypes.SHORT:		values = TIFFShortValues    .extractValues(buffer,useByteOrder,numberOfValues); break;
			case TIFFTypes.LONG:		values = TIFFLongValues     .extractValues(buffer,useByteOrder,numberOfValues); break;
			case TIFFTypes.RATIONAL:	values = TIFFRationalValues .extractValues(buffer,useByteOrder,numberOfValues); break;
			case TIFFTypes.SBYTE:		values = TIFFSByteValues    .extractValues(buffer,numberOfValues); break;
			case TIFFTypes.UNDEFINED:	values = TIFFUndefinedValues.extractValues(buffer,numberOfValues); break;
			case TIFFTypes.SSHORT:		values = TIFFSShortValues   .extractValues(buffer,useByteOrder,numberOfValues); break;
			case TIFFTypes.SLONG:		values = TIFFSLongValues    .extractValues(buffer,useByteOrder,numberOfValues); break;
			case TIFFTypes.SRATIONAL:	values = TIFFSRationalValues.extractValues(buffer,useByteOrder,numberOfValues); break;
			//case TIFFTypes.FLOAT:		values = TIFFFloatValues    .extractValues(buffer,useByteOrder,numberOfValues); break;
			//case TIFFTypes.DOUBLE:	values = TIFFDoubleValues   .extractValues(buffer,useByteOrder,numberOfValues); break;
			case TIFFTypes.IFD:			values = TIFFIfdValues      .extractValues(buffer,useByteOrder,numberOfValues); break;
			case TIFFTypes.LONG8:		values = TIFFLong8Values    .extractValues(buffer,useByteOrder,numberOfValues); break;
			case TIFFTypes.SLONG8:		values = TIFFSlong8Values   .extractValues(buffer,useByteOrder,numberOfValues); break;
			case TIFFTypes.IFD8:		values = TIFFIfd8Values     .extractValues(buffer,useByteOrder,numberOfValues); break;
		}
		return values;
	}
	
	public long getSingleNumericValue(int valueNumber,long defaultValue) {
		return defaultValue;	// unless overridden by sub-class
	}
	
	public static long getSingleNumericValue(TIFFValues values,int valueNumber,long defaultValue) {
		return values.getSingleNumericValue(valueNumber,defaultValue);
	}
	
	public long[] getNumericValues() {
		return null;	// unless overridden by sub-class
	}

	public byte[] getByteValues() {
		return null;	// unless overridden by sub-class
	}

	public String[] getStringValues() {
		return null;	// unless overridden by sub-class
	}
}

