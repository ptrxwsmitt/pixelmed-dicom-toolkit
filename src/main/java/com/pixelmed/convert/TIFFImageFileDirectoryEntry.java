package com.pixelmed.convert;

import java.io.EOFException;
import java.io.IOException;

public class TIFFImageFileDirectoryEntry {

	protected int tagIdentifier;
	protected int tagType;
	protected long numberOfValues;
	protected TIFFValues values;
	
	private static final byte[] buffer32 = new byte[4];
	private static final byte[] buffer64 = new byte[8];
	
	public int getTagIdentifier() { return tagIdentifier; };
	
	public int getTagType() { return tagType; };
	
	public long getNumberOfValues() { return numberOfValues; };
	
	public TIFFValues getValues() { return values; };

	public TIFFImageFileDirectoryEntry(int tagIdentifier,int tagType,long numberOfValues,TIFFValues values) {
		this.tagIdentifier = tagIdentifier;
		this.tagType = tagType;
		this.numberOfValues = numberOfValues;
		this.values = values;
	}

	public static TIFFImageFileDirectoryEntry readBigIFDEntry(TIFFFile f) throws EOFException, IOException, TIFFException {
		int tagIdentifier = f.getUnsigned16();
		int tagType = f.getUnsigned16();
		long numberOfValues = f.getUnsigned64();

		int numberOfBytesPerValue = TIFFTypes.getNumberOfBytesPerValue(tagType);
		long totalLength = numberOfValues * numberOfBytesPerValue;
		TIFFValues values = null;
		if (totalLength <= 8) {
			// values are contained in valueOrOffset field
			f.read(buffer64);
			values = TIFFValues.extractValues(tagType,buffer64,f.getByteOrder(),(int)numberOfValues);
//System.err.println(TIFFTags.getDescription(tagIdentifier)+" ("+tagIdentifier+") "+TIFFTypes.getDescription(tagType)+" ("+tagType+") "+numberOfValues+"<"+values+">");
		}
		else {
			// values are located at valueOrOffset field
			long valueOrOffset = f.getUnsigned64();
			long currentPositionToReturnTo = f.getFilePointer();
			byte[] buffer = new byte[(int)totalLength];
			f.seek(valueOrOffset);
			f.read(buffer);
			f.seek(currentPositionToReturnTo);
			values = TIFFValues.extractValues(tagType,buffer,f.getByteOrder(),(int)numberOfValues);
//System.err.println(TIFFTags.getDescription(tagIdentifier)+" ("+tagIdentifier+") "+TIFFTypes.getDescription(tagType)+" ("+tagType+") "+numberOfValues+"@"+valueOrOffset+"<"+values+">");
		}
		return new TIFFImageFileDirectoryEntry(tagIdentifier,tagType,numberOfValues,values);
	}
	
	public static TIFFImageFileDirectoryEntry readClassicIFDEntry(TIFFFile f) throws EOFException, IOException, TIFFException {
		int tagIdentifier = f.getUnsigned16();
		int tagType = f.getUnsigned16();
		long numberOfValues = f.getUnsigned32();

		int numberOfBytesPerValue = TIFFTypes.getNumberOfBytesPerValue(tagType);
		long totalLength = numberOfValues * numberOfBytesPerValue;
		TIFFValues values = null;
		if (totalLength <= 4) {
			// values are contained in valueOrOffset field
			f.read(buffer32);
			values = TIFFValues.extractValues(tagType,buffer32,f.getByteOrder(),(int)numberOfValues);
//System.err.println(TIFFTags.getDescription(tagIdentifier)+" ("+tagIdentifier+") "+TIFFTypes.getDescription(tagType)+" ("+tagType+") "+numberOfValues+"<"+values+">");
		}
		else {
			// values are located at valueOrOffset field
			long valueOrOffset = f.getUnsigned32();
			long currentPositionToReturnTo = f.getFilePointer();
			byte[] buffer = new byte[(int)totalLength];
			f.seek(valueOrOffset);
			f.read(buffer);
			f.seek(currentPositionToReturnTo);
			values = TIFFValues.extractValues(tagType,buffer,f.getByteOrder(),(int)numberOfValues);
//System.err.println(TIFFTags.getDescription(tagIdentifier)+" ("+tagIdentifier+") "+TIFFTypes.getDescription(tagType)+" ("+tagType+") "+numberOfValues+"@"+valueOrOffset+"<"+values+">");
		}
		return new TIFFImageFileDirectoryEntry(tagIdentifier,tagType,numberOfValues,values);
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(TIFFTags.getDescription(tagIdentifier));
		buf.append(" (");
		buf.append(tagIdentifier);
		buf.append(") ");
		buf.append(TIFFTypes.getDescription(tagType));
		buf.append(" (");
		buf.append(tagType);
		buf.append(") ");
		buf.append(numberOfValues);
		buf.append("<");
		buf.append(values);
		buf.append(">\n");
		return buf.toString();
	}
}
