/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.utils.CopyStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A concrete class specializing {@link Attribute Attribute} for
 * Other Byte (OB) attributes whose values are memory or file resident compressed pixel data frames.</p>
 *
 * @see Attribute
 * @see AttributeList
 *
 * @author	dclunie
 */
public class OtherByteAttributeMultipleCompressedFrames extends Attribute implements ByteFrameSource {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/OtherByteAttributeMultipleCompressedFrames.java,v 1.19 2022/01/21 19:51:17 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(OtherByteAttributeMultipleCompressedFrames.class);
	
	protected byte[] allframes;
	protected byte[][] frames;
	protected File[] files;

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * @param	t	the tag of the attribute
	 */
	private OtherByteAttributeMultipleCompressedFrames(AttributeTag t) {
		super(t);
	}

	/**
	 * <p>Construct an attribute from a single byte array containing all compressed frames.</p>
	 *
	 * <p>The VL is not required, since it is undefined by definition.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	allframes	the frames
	 */
	public OtherByteAttributeMultipleCompressedFrames(AttributeTag t,byte[] allframes) {
		super(t);
		this.allframes = allframes;
		this.frames = null;
		this.files = null;
		valueLength=0xffffffffl;
		valueMultiplicity=1;
	}

	/**
	 * <p>Construct an attribute from a set of compressed frames.</p>
	 *
	 * <p>The VL is not required, since it is undefined by definition.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	frames		the frames
	 */
	public OtherByteAttributeMultipleCompressedFrames(AttributeTag t,byte[][] frames) {
		super(t);
		this.allframes = null;
		this.frames = frames;
		this.files = null;
		valueLength=0xffffffffl;
		valueMultiplicity=1;
	}

	/**
	 * <p>Construct an attribute from a set of compressed frames.</p>
	 *
	 * <p>The VL is not required, since it is undefined by definition.</p>
	 *
	 * @param	t		the tag of the attribute
	 * @param	files	the files containing the compressed bit streams
	 */
	public OtherByteAttributeMultipleCompressedFrames(AttributeTag t,File[] files) {
		super(t);
		this.allframes = null;
		this.frames = null;
		this.files = files;
		valueLength=0xffffffffl;
		valueMultiplicity=1;
	}
	
	protected static final AttributeTag itemTag = TagFromName.Item;
	
	protected void writeItemTag(DicomOutputStream o,long length) throws IOException {
		o.writeUnsigned16(itemTag.getGroup());
		o.writeUnsigned16(itemTag.getElement());
		o.writeUnsigned32(length);
	}
	
	protected static final AttributeTag sequenceDelimitationItemTag = TagFromName.SequenceDelimitationItem;
	
	protected void writeSequenceDelimitationItemTag(DicomOutputStream o) throws IOException {
		o.writeUnsigned16(sequenceDelimitationItemTag.getGroup());
		o.writeUnsigned16(sequenceDelimitationItemTag.getElement());
		o.writeUnsigned32(0);
	}

	/**
	 * @param	o
	 * @throws	IOException
	 * @throws	DicomException	if no byte array or files containing the compressed bitstream have been supplied
	 */
	public void write(DicomOutputStream o) throws DicomException, IOException {
		writeBase(o);
		writeItemTag(o,0);	// empty basic offset table
		if (allframes != null) {
			long fragmentLength = allframes.length;
			long padding = fragmentLength % 2;
			long paddedLength = fragmentLength + padding;
			writeItemTag(o,paddedLength);	// one fragment for all frames
			o.write(allframes);
			if (padding > 0) {
				o.write(0);
			}
		}
		else {
			int nFrames = 0;
			if (files != null) {
				nFrames = files.length;
			}
			else if (frames != null) {
				nFrames = frames.length;
			}
			if (nFrames > 0) {
				for (int f=0; f<nFrames; ++f) {
					File file = null;
					byte[] frame = null;
					long frameLength = 0;
					if (files != null) {
//System.err.println("OtherByteAttributeMultipleCompressedFrames.write(): Doing compressed file for frame "+f);
						file = files[f];
						frameLength = file.length();
					}
					else {
//System.err.println("OtherByteAttributeMultipleCompressedFrames.write(): Doing compressed frame "+f);
						frame = frames[f];
						frameLength = frame.length;
					}
					long padding = frameLength % 2;
					long paddedLength = frameLength + padding;
					writeItemTag(o,paddedLength);	// always one fragment per frame at this time :(
					if (file != null) {
						InputStream in = new FileInputStream(file);
						CopyStream.copy(in,o);
						in.close();
					}
					else {
						o.write(frame);
					}
					if (padding > 0) {
						o.write(0);
					}
				}
			}
			else {
				throw new DicomException("No source of compressed pixel data to write");
			}
		}
		writeSequenceDelimitationItemTag(o);
	}
	
	/**
	 * <p>Get the byte arrays for each frame.</p>
	 *
	 * @return						an array of byte arrays for each frame
	 */
	public byte[][] getFrames() {
		return frames;
	}
	
	/**
	 * <p>Get the files for each frame.</p>
	 *
	 * @return						an array of files for each frame
	 */
	public File[] getFiles() {
		return files;
	}

	/**
	 * <p>Get the value of this attribute as a byte array for one selected frame.</p>
	 *
	 * @param	frameNumber		from 0
	 * @return					the values as an array of bytes
	 * @throws	DicomException	thrown if values cannot be read
	 */
	public byte[] getByteValuesForSelectedFrame(int frameNumber) throws DicomException {
		byte[] frame = null;
		if (frames != null) {
			frame = frames[frameNumber];
		}
		else if (files != null) {
			slf4jlogger.debug("getByteValuesForSelectedFrame(): lazy read of selected frame {} into heap allocated memory",frameNumber);
			File file = files[frameNumber];
			if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("getByteValuesForSelectedFrame(): Reading from file() = {}",file.toString());
			int remaining = (int)file.length();		// should check for 2GB overrun :(
			slf4jlogger.trace("getByteValuesForSelectedFrame(): file.length() = {}",remaining);
			frame = new byte[remaining];
			int offset = 0;
			int count;
			try {
				InputStream i = new BufferedInputStream(new FileInputStream(file));
				while (remaining > 0 && (count=i.read(frame,offset,remaining)) > 0) {
					offset += count;
					remaining -= count;
				}
				i.close();
			}
			catch (IOException e) {
				throw new DicomException("Failed to read compressed frame "+frameNumber+", offset "+offset+" dec bytes in delayed read of "+ValueRepresentation.getAsString(getVR())+" attribute "+getTag());
			}
		}
		else {
			throw new DicomException("Failed to read compressed frame {} - no memory or file source"+frameNumber);
		}
		//if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace(com.pixelmed.utils.HexDump.dump(frame));
		return frame;
	}

	/**
	 * <p>Get the number of frames.</p>
	 *
	 * @return	number of frames, or zero if all frames are in one fragment
	 */
	public int getNumberOfFrames() {
		return frames == null ? (files == null ? 0 : files.length) : frames.length;
	}
	
	/***/
	public String toString(DicomDictionary dictionary) {
		StringBuffer str = new StringBuffer();
		str.append(super.toString(dictionary));
		str.append(" []");		// i.e. don't really dump values ... too many
		return str.toString();
	}

	/**
	 */
	public void removeValues() {
		frames=null;
		files=null;
		valueMultiplicity=0;
		valueLength=0;
	}

	/**
	 * <p>Get the value representation of this attribute (OB).</p>
	 *
	 * @return	'O','B' in ASCII as a two byte array; see {@link ValueRepresentation ValueRepresentation}
	 */
	public byte[] getVR() { return ValueRepresentation.OB; }

	/**
	 * <p>Get the length of the entire attribute when encoded, accounting for the characteristics of the Transfer Syntax and the need for even-length padding.</p>
	 *
	 * @param	explicit			true if the Transfer Syntax to be used for encoding is explicit VR
	 * @param	littleEndian		true if the Transfer Syntax to be used for encoding is little endian
	 * @return	the length in bytes
	 * @throws	DicomException		if the VL is too long to be written in Explicit VR Transfer Syntax
	 * @deprecated	experimental - incomplete implementation
	 */

 	// must EXACTLY mirror behavior of write()
	public long getLengthOfEntireEncodedAttribute(boolean explicit,boolean littleEndian) throws DicomException {
		long l = getLengthOfBaseOfEncodedAttribute(explicit,littleEndian);
		l += 8;		// empty basic offset table
		if (allframes != null) {
			l += 8;		// length of Item Delimiter
			long fragmentLength = allframes.length;
			long padding = fragmentLength % 2;
			long paddedLength = fragmentLength + padding;
			l += paddedLength;
		}
		else {
			int nFrames = files != null ? files.length : frames.length;
			if (nFrames > 0) {
				for (int f=0; f<nFrames; ++f) {
					l += 8;		// length of Item Delimiter
					long frameLength = files != null ? files[f].length() : frames[f].length;
					long padding = frameLength % 2;
					long paddedLength = frameLength + padding;
					l += paddedLength;
				}
			}
		}
		l += 8;		// SequenceDelimitationItemTag
		return l;
	}
}

