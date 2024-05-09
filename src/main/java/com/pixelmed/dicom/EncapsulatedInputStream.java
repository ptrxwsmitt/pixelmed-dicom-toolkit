/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;			// for test timing of routines
import java.util.List;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class that extends {@link InputStream InputStream} by adding
 * a mechanism for unencapsulating an undefined length DICOM attribute, such as is used for
 * compressed Pixel Data.</p>
 *
 * <p>The read methods hide the fact that the data is encapsulated by removing the Items and
 * Item and Sequence delimiter tags, as well as skipping any Basic Offset Table
 * that may be present in the first Item.</p>
 *
 * <p>Since an individual frame may be fragmented and padded beyond the
 * JPEG EOI marker (0xffd9), and since the codec used for decoding may be "reading ahead"
 * this class also removes any padding bytes at the end of any fragment, back as
 * far as the EOI marker. Note that this means that theoretically frames could span
 * fragments as long as there was no padding between them.</p>
 *
 * @author	dclunie
 */
public class EncapsulatedInputStream extends InputStream {

	/***/
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/EncapsulatedInputStream.java,v 1.35 2022/01/21 19:51:16 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(EncapsulatedInputStream.class);

	/***/
	private BinaryInputStream i;
	/***/
	private boolean jpegEOIDetection;
	/***/
	private boolean oneFragmentPerFrame;
	/***/
	private boolean bigEndian;
	/***/
	private byte buffer[];			// just for read() one byte method
	/***/
	private boolean firstTime;
	/***/
	private byte fragment[];
	/***/
	private int fragmentSize;
	/***/
	private int fragmentOffset;
	/***/
	private int fragmentRemaining;
	/***/
	private boolean sequenceDelimiterEncountered;
	/***/
	private boolean endOfFrameEncountered;
	/***/
	private boolean currentFragmentContainsEndOfFrame;
	/***/
	private long bytesRead;
	
	protected long getBytesRead() {
		return bytesRead;
	}

	private List<Long> listOfByteOffsetsOfItemsForCurrentFrame = new ArrayList<Long>();
	private List<Long> listOfItemLengthsForCurrentFrame = new ArrayList<Long>();
	
	public long[] getByteOffsetsOfItemsForCurrentFrame(long startOfPixelData) {
		long[] byteOffsetsOfItemsForCurrentFrame = new long[listOfByteOffsetsOfItemsForCurrentFrame.size()];
		for (int i=0; i<listOfByteOffsetsOfItemsForCurrentFrame.size(); ++i) {
			byteOffsetsOfItemsForCurrentFrame[i] = listOfByteOffsetsOfItemsForCurrentFrame.get(i).longValue() + startOfPixelData;	// entry in list is relative to start of PixelData
			slf4jlogger.debug("getByteOffsetsOfItemsForCurrentFrame(): {} for item {}",byteOffsetsOfItemsForCurrentFrame[i],i);
		}
		return byteOffsetsOfItemsForCurrentFrame;
	}
	
	public long[] getItemLengthsForCurrentFrame() {
		long[] itemLengthsForCurrentFrame = new long[listOfItemLengthsForCurrentFrame.size()];
		for (int i=0; i<listOfItemLengthsForCurrentFrame.size(); ++i) {
			itemLengthsForCurrentFrame[i] = listOfItemLengthsForCurrentFrame.get(i).longValue();
			slf4jlogger.debug("getItemLengthsForCurrentFrame(): {} for item {}",itemLengthsForCurrentFrame[i],i);
		}
		return itemLengthsForCurrentFrame;
	}

	/**
	 * @throws	IOException		if an I/O error occurs
	 */
	private AttributeTag readAttributeTag() throws IOException {
		int group   = i.readUnsigned16();
		int element = i.readUnsigned16();
		slf4jlogger.debug("readAttributeTag(): back from reading group and element");
		bytesRead+=4;
		return new AttributeTag(group,element);
	}

	/**
	 * @throws	IOException		if an I/O error occurs
	 */
	private long readItemTag() throws IOException {
		AttributeTag tag = readAttributeTag();
		slf4jlogger.debug("readItemTag(): tag={}",tag);
		long vl = i.readUnsigned32();		// always implicit VR form for items and delimiters
		slf4jlogger.debug("readItemTag(): back from reading vl={}",vl);
		bytesRead+=4;
		if (tag.equals(TagFromName.SequenceDelimitationItem)) {
			slf4jlogger.debug("readItemTag: SequenceDelimitationItem");
			vl=0;	// regardless of what was read
			sequenceDelimiterEncountered=true;
		}
		else if (!tag.equals(TagFromName.Item)) {
			slf4jlogger.debug("readItemTag(): not an Item tag - throwing an IOException");
			throw new IOException("Unexpected DICOM tag "+tag+" (vl="+vl+") in encapsulated data whilst expecting Item or SequenceDelimitationItem");
		}
		slf4jlogger.debug("readItemTag(): length={}",vl);
		return vl;
	}
	
//	private void readItemDelimiter() throws IOException {
//		AttributeTag tag = readAttributeTag();
//System.err.println("EncapsulatedInputStream.readItemDelimiter: tag="+tag);
//		i.readUnsigned32();		// always implicit VR form for items and delimiters
//		if (!tag.equals(TagFromName.ItemDelimitationItem)) {
//			throw new IOException("Expected DICOM Item Delimitation Item tag in encapsulated data");
//		}
//	}
	
	/**
	 * @throws	IOException		if an I/O error occurs
	 */
	public void readSequenceDelimiter() throws IOException {
		slf4jlogger.debug("readSequenceDelimiter():");
		if (!sequenceDelimiterEncountered) {
			slf4jlogger.debug("readSequenceDelimiter(): have not yet encountered SequenceDelimiter ... trying readItemTag()");
			readItemTag();
		}
		slf4jlogger.debug("readSequenceDelimiter(): sequenceDelimiterEncountered is "+sequenceDelimiterEncountered+" after trying readItemTag()");
		if (!sequenceDelimiterEncountered) {
			slf4jlogger.debug("readSequenceDelimiter(): sequenceDelimiterEncountered is false - throwing an IOException");
			throw new IOException("Expected DICOM Sequence Delimitation Item tag in encapsulated data");
		}
	}

	/**
	 * <p>Construct a byte ordered stream from the supplied stream.</p>
	 *
	 * <p>The byte order may be changed later.</p>
	 *
	 * @param	i					the input stream to read from
	 * @param	jpegEOIDetection	whether or not to detect JPEG EOI, to allow frames to span fragments
	 * @param	oneFragmentPerFrame	if not using jpegEOIDetection, whether or not to assume one fragment per frame (e.g., for RLE), rather than all fragments in one frame (e.g., for MPEG)
	 */
	public EncapsulatedInputStream(BinaryInputStream i,boolean jpegEOIDetection,boolean oneFragmentPerFrame) {
		this.i=i;
		this.jpegEOIDetection=jpegEOIDetection;
		slf4jlogger.debug("jpegEOIDetection={}",jpegEOIDetection);
		this.oneFragmentPerFrame=oneFragmentPerFrame;
		slf4jlogger.debug("oneFragmentPerFrame={}",oneFragmentPerFrame);
		bigEndian=i.isBigEndian();
		buffer=new byte[8];
		fragment=null;
		firstTime=true;
		sequenceDelimiterEncountered=false;
		endOfFrameEncountered=false;
	}

	/**
	 * <p>Construct a byte ordered stream from the supplied stream.</p>
	 *
	 * <p>The byte order may be changed later.</p>
	 *
	 * <p>JPEG EOI detection is enabled, to allow frames to span fragments.</p>
	 *
	 * <p>Do not use for RLE encapsulated data, since it will assume all fragments are in one frame, rather than one fragment per frame.</p>
	 *
	 * @param	i					the input stream to read from
	 */
	public EncapsulatedInputStream(BinaryInputStream i) {
		this(i,true/*jpegEOIDetection*/,false/*oneFragmentPerFrame*/);
	}
	
	/**
	 * <p>Skip to the start of the first fragment for a frame, if not already there.</p>
	 */
	public void nextFrame() {
		slf4jlogger.debug("nextFrame()");
		// flush to start of next fragment unless already positioned at start of next fragment
		if (fragment == null) {
			slf4jlogger.debug("nextFrame(): fragment already null");
		}
		else if (fragmentOffset == 0) {
			slf4jlogger.debug("nextFrame(): fragment already positioned at start of next fragment");
		}
		else {
			slf4jlogger.debug("nextFrame(): fragment was not already null or positioned at start of next fragment: fragmentOffset = {}",fragmentOffset);
			fragment=null;
		}
		endOfFrameEncountered=false;
		listOfByteOffsetsOfItemsForCurrentFrame.clear();
		listOfItemLengthsForCurrentFrame.clear();
	}

	// Our own specific methods a la BinaryInputStream ...
	
	/**
	 * <p>Read an array of unsigned integer 16 bit values.</p>
	 *
	 * @param	w		an array of sufficient size in which to return the values read
	 * @param	offset		the offset in the array at which to begin storing values
	 * @param	len		the number of 16 bit values to read
	 * @throws	IOException		if an I/O error occurs
	 */
	public final void readUnsigned16(short[] w,int offset,int len) throws IOException {
		int blen = len*2;
		byte  b[] = new byte[blen];
		read(b,0,blen);				// read the bytes from the fragment(s)
		bytesRead+=blen;
		int bcount=0;
		int wcount=0;
		if (bigEndian) {
			for (;wcount<len;++wcount) {
				w[offset+wcount]=(short)((b[bcount++]<<8) + (b[bcount++]&0xff));	// assumes left to right evaluation
			}
		}
		else {
			for (;wcount<len;++wcount) {
				w[offset+wcount]=(short)((b[bcount++]&0xff) + (b[bcount++]<<8));	// assumes left to right evaluation
			}
		}
	}
	
	/**
	 * <p>Read an unsigned integer 32 bit values little endian regardless of endianness of stream.</p>
	 *
	 * @return					the value read
	 * @throws	IOException		if an I/O error occurs
	 */
	public final long readUnsigned32LittleEndian() throws IOException {
		byte  b[] = new byte[4];
		read(b,0,4);				// read the bytes from the fragment(s)
		long v1 =  ((long)b[0])&0xff;
		long v2 =  ((long)b[1])&0xff;
		long v3 =  ((long)b[2])&0xff;
		long v4 =  ((long)b[3])&0xff;
		return (((((v4 << 8) | v3) << 8) | v2) << 8) | v1;
	}
	
	/**
	 * <p>Read an array of unsigned integer 32 bit values little endian regardless of endianness of stream.</p>
	 *
	 * @param	w			an array of sufficient size in which to return the values read
	 * @param	offset		the offset in the array at which to begin storing values
	 * @param	len			the number of 32 bit values to read
	 * @throws	IOException		if an I/O error occurs
	 */
	public final void readUnsigned32LittleEndian(long[] w,int offset,int len) throws IOException {
		int blen = len*4;
		byte  b[] = new byte[blen];
		read(b,0,blen);				// read the bytes from the fragment(s)
		bytesRead+=blen;
		int bcount=0;
		int wcount=0;
		{
			for (;wcount<len;++wcount) {
				long v1 =  ((long)b[bcount++])&0xff;
				long v2 =  ((long)b[bcount++])&0xff;
				long v3 =  ((long)b[bcount++])&0xff;
				long v4 =  ((long)b[bcount++])&0xff;
				w[offset+wcount]=(((((v4 << 8) | v3) << 8) | v2) << 8) | v1;
			}
		}
	}

	/**
	 * <p>Skip as many bytes as requested, unless an exception occurs.</p>
	 *
	 * @param	length		number of bytes to read (no more and no less)
	 * @throws	IOException		if an I/O error occurs
	 */
	public void skipInsistently(long length) throws IOException {
		long remaining = length;
		while (remaining > 0) {
//System.err.println("skipInsistently(): looping remaining={}",remaining);
			long bytesSkipped = skip(remaining);
//System.err.println("skipInsistently(): asked for ="+remaining+" got={}",bytesSkipped);
			if (bytesSkipped <= 0) throw new IOException("skip failed with "+remaining+" bytes remaining to be skipped, wanted "+length);
			remaining-=bytesSkipped;
		}
	}
	
	// Override the necessary methods from InputStream ...

	/**
	 * <p>Extracts the next byte of data from the current or
	 * subsequent fragments.</p>
	 *
	 * @return     the next byte of data, or -1 if there is no more data because the end of the stream has been reached.
	 * @throws  IOException  if an I/O error occurs.
	 */
	public final int read() throws IOException {
		int count = read(buffer,0,1);
		return count == -1 ? -1 : (buffer[0]&0xff);		// do not sign extend
	}

	/**
	 * <p>Extracts <code>byte.length</code> bytes of data from the current or
	 * subsequent fragments.</p> 
	 * <p>This method simply performs the call <code>read(b, 0, b.length)</code> and returns
	 * the  result.</p>
	 *
	 * @param      b   the buffer into which the data is read.
	 * @return     	   the total number of bytes read into the buffer (always whatever was asked for), or -1 if there is no more data because the end of the stream has been reached.
	 * @throws  IOException  if an I/O error occurs.
	 * @see        #read(byte[], int, int)
	 */
	public final int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * <p>Extracts <code>len</code> bytes of data from the current or
	 * subsequent fragments.</p> 
	 *
	 * @param      b     the buffer into which the data is read.
	 * @param      off   the start offset of the data.
	 * @param      len   the number of bytes read.
	 * @return     	     the total number of bytes read into the buffer (always whatever was asked for), or -1 if there is no more data because the end of a frame has been reached.
	 * @throws  IOException  if an I/O error occurs.
	 */
	public final int read(byte b[], int off, int len) throws IOException {
		slf4jlogger.debug("read(byte [],"+off+","+len+")");
		slf4jlogger.debug("read() at start, fragmentRemaining={}",fragmentRemaining);
		slf4jlogger.debug("read() at start, endOfFrameEncountered={}",endOfFrameEncountered);
		slf4jlogger.debug("read() at start, currentFragmentContainsEndOfFrame={}",currentFragmentContainsEndOfFrame);
		if (endOfFrameEncountered) {
		slf4jlogger.debug("read() returning -1 since endOfFrameEncountered");
			return -1;						// i.e., won't advance until nextFrame() is called to reset this state
		}
		int count=0;
		int remainingToDo = len;
		int offsetAtStartOfRead = off;
		while (remainingToDo > 0 && !sequenceDelimiterEncountered && !endOfFrameEncountered) {
			slf4jlogger.debug("read() remainingToDo={}",remainingToDo);
			if (fragment == null) {
				if (firstTime) {
					slf4jlogger.debug("read() firstTime");
					// first time ... skip offset table ...
					long offsetTableLength = readItemTag();
					if (sequenceDelimiterEncountered) {
						throw new IOException("Expected offset table item tag; got sequence delimiter");
					}
					slf4jlogger.debug("read() skipping offsetTableLength={}",offsetTableLength);
					i.skipInsistently(offsetTableLength);
					bytesRead+=offsetTableLength;
					firstTime=false;
				}
				// load a new fragment ...
				slf4jlogger.debug("read() loading a new fragment");
				long vl = readItemTag();	// if sequenceDelimiterEncountered, vl will be zero and no more will be done
				if (vl != 0) {
					currentFragmentContainsEndOfFrame=false;
					fragmentRemaining = fragmentSize = (int)vl;
					if (fragment == null || fragment.length < fragmentSize) {
						fragment = new byte[fragmentSize];
					}
					// else reuse it rather than discarding and reallocating
					slf4jlogger.debug("read() bytesRead at start of frame (ByteOffsetsOfItemsForCurrentFrame)={}",bytesRead);
					listOfByteOffsetsOfItemsForCurrentFrame.add(new Long(bytesRead));	// bytesRead is relative to start of PixelData
					i.readInsistently(fragment,0,fragmentSize);
					bytesRead+=fragmentSize;
					fragmentOffset=0;
					slf4jlogger.debug("read() fragmentRemaining initially={}",fragmentRemaining);
					if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("read() fragment = {}",com.pixelmed.utils.HexDump.dump(fragment));
					if (jpegEOIDetection) {
						// Ignore everything between (the last) EOI marker and the end of the fragment
						int positionOfEOI = fragmentRemaining-1;
						while (--positionOfEOI > 0) {
							int firstMarkerByte  = fragment[positionOfEOI  ]&0xff;
							int secondMarkerByte = fragment[positionOfEOI +1]&0xff;
							if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("read() fragment fragment[{}] = 0x{}",positionOfEOI,Integer.toHexString(firstMarkerByte));
							if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("read() fragment fragment[{}] = 0x{}",(positionOfEOI+1),Integer.toHexString(secondMarkerByte));
							if (firstMarkerByte == 0xff && secondMarkerByte == 0xd9) {
								currentFragmentContainsEndOfFrame=true;
								break;
							}
						}
						slf4jlogger.debug("read() positionOfEOI={}",positionOfEOI);
						if (positionOfEOI > 0) {			// will be zero if we did not find one
							fragmentRemaining = positionOfEOI+2;	// effectively skips all (hopefully padding) bytes after the EOI
						}
						slf4jlogger.debug("read() fragmentRemaining after removing trailing padding={}",fragmentRemaining);
						listOfItemLengthsForCurrentFrame.add(new Long(fragmentRemaining));	// (001050) use fragmentRemaining instead of vl, to make sure don't read past EOI marker if trailing padding (may be odd), else J2K reader fails
					}
					else if (oneFragmentPerFrame) {
						currentFragmentContainsEndOfFrame=true;
					}
				}
			}
			int amountToCopyFromThisFragment = remainingToDo < fragmentRemaining ? remainingToDo : fragmentRemaining;
			slf4jlogger.debug("read() amountToCopyFromThisFragment={}",amountToCopyFromThisFragment);
			if (amountToCopyFromThisFragment > 0) {
				System.arraycopy(fragment,fragmentOffset,b,off,amountToCopyFromThisFragment);
				off+=amountToCopyFromThisFragment;
				fragmentOffset+=amountToCopyFromThisFragment;
				fragmentRemaining-=amountToCopyFromThisFragment;
				remainingToDo-=amountToCopyFromThisFragment;
				count+=amountToCopyFromThisFragment;
			}
			if (fragmentRemaining <= 0) {
				fragment=null;
				if (currentFragmentContainsEndOfFrame) {
					endOfFrameEncountered = true;			// once EOI has been seen in a fragment, use the rest of the fragment including the EOI, but no further
				}
			}
		}
		slf4jlogger.debug("read() returning count={}",count);
		if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("read() returning={}",com.pixelmed.utils.HexDump.dump(b,offsetAtStartOfRead,count));
		return count == 0 ? -1 : count;		// always returns more than 0 unless end, which is signalled by -1
	}
	
	public int getOffsetOfNextByteToReadFromStartOfFragment() { return fragmentOffset; }
	
	/**
	 * <p>Are frames separated (seperable)?</p>
	 *
	 * <p>This will be either because the stream contains recognizable end of frame markers (e.g., JPEG) or the Transfer Syntax specifies one fragment per frame (e.g., RLE).</p>
	 *
	 * @return  true if frames are separated rather than being lumped together in one byte stream/array
	 */
	public boolean framesAreSeparated() { return jpegEOIDetection || oneFragmentPerFrame; }
}


