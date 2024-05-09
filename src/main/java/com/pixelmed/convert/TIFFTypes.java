package com.pixelmed.convert;

public class TIFFTypes {

	public final static int BYTE = 1;
	public final static int ASCII = 2;
	public final static int SHORT = 3;
	public final static int LONG = 4;
	public final static int RATIONAL = 5;
	public final static int SBYTE = 6;
	public final static int UNDEFINED = 7;
	public final static int SSHORT = 8;
	public final static int SLONG = 9;
	public final static int SRATIONAL = 10;
	public final static int FLOAT = 11;
	public final static int DOUBLE = 12;
	public final static int IFD = 13;			// TIFF Supplement 1 - SubIFDs
	public final static int LONG8 = 16;
	public final static int SLONG8 = 17;
	public final static int IFD8 = 18;

	public static String getDescription(int tagType) {
		String description = "--unrecognized--";
		switch (tagType) {
			case BYTE:		description="BYTE"; break;
			case ASCII:		description="ASCII"; break;
			case SHORT:		description="SHORT"; break;
			case LONG:		description="LONG"; break;
			case RATIONAL:	description="RATIONAL"; break;
			case SBYTE:		description="SBYTE"; break;
			case UNDEFINED:	description="UNDEFINED"; break;
			case SSHORT:	description="SSHORT"; break;
			case SLONG:		description="SLONG"; break;
			case SRATIONAL:	description="SRATIONAL"; break;
			case FLOAT:		description="FLOAT"; break;
			case DOUBLE:	description="DOUBLE"; break;
			case IFD:		description="IFD"; break;
			case LONG8:		description="LONG8"; break;
			case SLONG8:	description="SLONG8"; break;
			case IFD8:		description="IFD8"; break;
		}
		return description;
	}

	public static int getNumberOfBytesPerValue(int tagType) {
		int numberOfBytes = 0;
		switch (tagType) {
			case BYTE:		numberOfBytes = 1; break;
			case ASCII:		numberOfBytes = 1; break;
			case SHORT:		numberOfBytes = 2; break;
			case LONG:		numberOfBytes = 4; break;
			case RATIONAL:	numberOfBytes = 8; break;
			case SBYTE:		numberOfBytes = 1; break;
			case UNDEFINED:	numberOfBytes = 1; break;
			case SSHORT:	numberOfBytes = 2; break;
			case SLONG:		numberOfBytes = 4; break;
			case SRATIONAL:	numberOfBytes = 8; break;
			case FLOAT:		numberOfBytes = 4; break;
			case DOUBLE:	numberOfBytes = 8; break;
			case IFD:		numberOfBytes = 4; break;
			case LONG8:		numberOfBytes = 8; break;
			case SLONG8:	numberOfBytes = 8; break;
			case IFD8:		numberOfBytes = 8; break;
		}
		return numberOfBytes;
	}

}

