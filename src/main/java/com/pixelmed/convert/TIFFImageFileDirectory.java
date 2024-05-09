package com.pixelmed.convert;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.SortedMap;
import java.util.TreeMap;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

public class TIFFImageFileDirectory {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/convert/TIFFImageFileDirectory.java,v 1.2 2019/08/11 13:19:02 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(TIFFImageFileDirectory.class);

	protected long ifdOffsetThis;
	protected long ifdOffsetNext;
	
	protected SortedMap<Integer,TIFFImageFileDirectoryEntry> entries;
	
	public TIFFImageFileDirectory() {
		entries = new TreeMap<Integer,TIFFImageFileDirectoryEntry>();
	}
		
	public long read(TIFFFile f,long ifdOffset) throws EOFException, IOException, TIFFException {
		ifdOffsetThis = ifdOffset;
		if (f.isBigTIFF()) {
			f.seek(ifdOffset);
			long numberOfTagsInIFD = f.getUnsigned64();
			slf4jlogger.debug("read(): BigTIFF - numberOfTagsInIFD = {}",numberOfTagsInIFD);
			for (long tagIndex=0; tagIndex<numberOfTagsInIFD; ++tagIndex) {
				TIFFImageFileDirectoryEntry entry = TIFFImageFileDirectoryEntry.readBigIFDEntry(f);
				entries.put(new Integer(entry.getTagIdentifier()),entry);
			}
			ifdOffsetNext = f.getUnsigned64();
		}
		else {
			f.seek(ifdOffset);
			int numberOfTagsInIFD = f.getUnsigned16();
			slf4jlogger.debug("read(): not BigTIFF - numberOfTagsInIFD = {}",numberOfTagsInIFD);
			for (int tagIndex=0; tagIndex<numberOfTagsInIFD; ++tagIndex) {
				TIFFImageFileDirectoryEntry entry = TIFFImageFileDirectoryEntry.readClassicIFDEntry(f);
				entries.put(new Integer(entry.getTagIdentifier()),entry);
			}
			ifdOffsetNext = f.getUnsigned32();
		}
		slf4jlogger.debug("read(): Next ifdOffsetNext = {}",ifdOffsetNext);
		return ifdOffsetNext;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("offset ");
		buf.append(ifdOffsetThis);
		buf.append(" (0x");
		buf.append(Long.toHexString(ifdOffsetThis));
		buf.append(") next ");
		buf.append(ifdOffsetNext);
		buf.append(" (0x");
		buf.append(Long.toHexString(ifdOffsetNext));
		buf.append(")\n");
		
		for (TIFFImageFileDirectoryEntry entry : entries.values()) {
			buf.append(entry);
		}
		return buf.toString();
	}
	
	public TIFFImageFileDirectoryEntry getEntry(int tagIdentifier) {
		return entries.get(new Integer(tagIdentifier));
	}

	public double getSingleRationalValue(int tagIdentifier,int valueNumber,double defaultValue) {
		double returnValue = defaultValue;
		TIFFImageFileDirectoryEntry entry = getEntry(tagIdentifier);
		if (entry != null) {
			// special case of single value instead of pair, i.e., missing denominator
			if (entry.getNumberOfValues() == 1 && valueNumber == 0) {
				long numerator = entry.getValues().getSingleNumericValue(valueNumber,0);
				if (numerator == 0) {
					returnValue = defaultValue;	// will use this if encoded value is 0 :(
				}
				else {
					returnValue = numerator;
				}
			}
			else if (entry.getNumberOfValues() > (valueNumber+1)) {
				long numerator = entry.getValues().getSingleNumericValue(valueNumber,0);
				long denominator = entry.getValues().getSingleNumericValue(valueNumber+1,1);
				returnValue = ((double)numerator)/denominator;
			}
		}
		return returnValue;
	}

	public long getSingleNumericValue(int tagIdentifier,int valueNumber,long defaultValue) {
		long returnValue = defaultValue;
		TIFFImageFileDirectoryEntry entry = getEntry(tagIdentifier);
		if (entry != null && entry.getNumberOfValues() > valueNumber) {
			returnValue = entry.getValues().getSingleNumericValue(valueNumber,defaultValue);
		}
		return returnValue;
	}

	public long[] getNumericValues(int tagIdentifier) {
		long[] returnValue = null;
		TIFFImageFileDirectoryEntry entry = getEntry(tagIdentifier);
		if (entry != null && entry.getNumberOfValues() > 0) {
			returnValue = entry.getValues().getNumericValues();
		}
		return returnValue;
	}

	public byte[] getByteValues(int tagIdentifier) {
		byte[] returnValue = null;
		TIFFImageFileDirectoryEntry entry = getEntry(tagIdentifier);
		if (entry != null && entry.getNumberOfValues() > 0) {
			returnValue = entry.getValues().getByteValues();
		}
		return returnValue;
	}

	public String[] getStringValues(int tagIdentifier) {
		slf4jlogger.debug("getStringValues(): tagIdentifier = {}",tagIdentifier);
		String[] returnValue = null;
		TIFFImageFileDirectoryEntry entry = getEntry(tagIdentifier);
		if (entry != null && entry.getNumberOfValues() > 0) {
			returnValue = entry.getValues().getStringValues();
		}
		return returnValue;
	}

}

