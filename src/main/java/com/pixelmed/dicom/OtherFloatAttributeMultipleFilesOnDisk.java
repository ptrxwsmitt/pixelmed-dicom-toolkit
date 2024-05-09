/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;

import com.pixelmed.utils.CopyStream;
import com.pixelmed.utils.FileReaper;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A concrete class specializing {@link Attribute Attribute} for
 * Other Float (OF) attributes whose values are not memory resident but rather are stored in multiple files on disk.</p>
 *
 * @see Attribute
 * @see AttributeFactory
 * @see AttributeList
 *
 * @author	dclunie
 */
public class OtherFloatAttributeMultipleFilesOnDisk extends Attribute {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/OtherFloatAttributeMultipleFilesOnDisk.java,v 1.1 2022/03/06 17:46:40 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(OtherFloatAttributeMultipleFilesOnDisk.class);
	
	protected File[] files;
	protected long[] byteOffsets;
	protected long[] lengths;
	protected boolean deleteFilesWhenNoLongerNeeded;

	protected boolean bigEndian;

	/**
	 * <p>Construct an (empty) attribute.</p>
	 *
	 * Any file set later will be expected to be little endian.
	 *
	 * @param	t	the tag of the attribute
	 * @param	bigEndian	big endian, false if little endian
	 */
	public OtherFloatAttributeMultipleFilesOnDisk(AttributeTag t,boolean bigEndian) {
		super(t);
		this.bigEndian = bigEndian;
	}

	/**
	 * <p>Read an attribute from a set of files.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	files		the input files
	 * @param	byteOffsets	the byte offsets in the files of the start of the data, one entry for each file, or null if 0 for all files
	 * @param	lengths		the lengths in the files from the the start of the data, one entry for each file, or null if the remaining file length after the byteOffset, if any
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public OtherFloatAttributeMultipleFilesOnDisk(AttributeTag t,File[] files,long[] byteOffsets,long[] lengths,boolean bigEndian) throws IOException, DicomException {
		this(t,bigEndian);
		setFiles(files,byteOffsets,lengths);
	}

	/**
	 * <p>Read an attribute from a set of files.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	fileNames	the input files
	 * @param	byteOffsets	the byte offsets in the files of the start of the data, one entry for each file, or null if 0 for all files
	 * @param	lengths		the lengths in the files from the the start of the data, one entry for each file, or null if the remaining file length after the byteOffset, if any
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public OtherFloatAttributeMultipleFilesOnDisk(AttributeTag t,String[] fileNames,long[] byteOffsets,long[] lengths,boolean bigEndian) throws IOException, DicomException {
		this(t,bigEndian);
		File[] files = new File[fileNames.length];
		for (int i=0; i<fileNames.length; ++i) {
			files[i] = new File(fileNames[i]);
		}
		setFiles(files,byteOffsets,lengths);
	}

	/**
	 * <p>Read an attribute from a set of files.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	files		the input files
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public OtherFloatAttributeMultipleFilesOnDisk(AttributeTag t,File[] files,boolean bigEndian) throws IOException, DicomException {
		this(t,files,null,null,bigEndian);
	}

	/**
	 * <p>Read an attribute from a set of files.</p>
	 *
	 * @param	t			the tag of the attribute
	 * @param	fileNames	the input files
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public OtherFloatAttributeMultipleFilesOnDisk(AttributeTag t,String[] fileNames,boolean bigEndian) throws IOException, DicomException {
		this(t,fileNames,null,null,bigEndian);
	}

	/**
	 * @return		the files containing the data
	 */
	public File[] getFiles() { return files; }

	/**
	 * @return		the per-file byte offsets to the frame data
	 */
	public long[] getByteOffsets() { return byteOffsets; }

	/**
	 * @return		the per-file lengths of the data for each frame (after the byte offset) in bytes
	 */
	public long[] getLengths() { return lengths; }

	/**
	 * @param	files		the input files
	 * @param	byteOffsets	the byte offsets in the files of the start of the data, one entry for each file, or null if 0 for all files
	 * @param	lengths		the lengths in the files from the the start of the data, one entry for each file, or null if the remaining file length after the byteOffset, if any
	 * @throws	IOException
	 */
	public void setFiles(File[] files,long[] byteOffsets,long[] lengths) throws IOException {
		this.files = files;
		if (byteOffsets == null) {
			this.byteOffsets = new long[files.length];
		}
		else {
			this.byteOffsets = byteOffsets;
		}
		if (lengths == null) {
			this.lengths = new long[files.length];
		}
		else {
			this.lengths = lengths;
		}
	
		valueLength=0;
		for (int i=0; i<files.length; ++i) {
			long length = 0;
			if (lengths == null) {
				length = files[i].length();
//System.err.println("OtherFloatAttributeMultipleFilesOnDisk.setFiles(): files["+i+"] = "+files[i].getCanonicalPath()+" length() = "+length);
				if (byteOffsets != null) {
					length -= byteOffsets[i];
				}
				this.lengths[i] = length;
			}
			else {
				length = lengths[i];
			}
			valueLength += length;
		}
//System.err.println("OtherFloatAttributeMultipleFilesOnDisk.setFiles(): valueLength = "+valueLength);
	}

	/***/
	public long getPaddedVL() {
		long vl = getVL();
		if (vl%2 != 0) ++vl;
		return vl;
	}
	
	/**
	 * @param	o
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public void write(DicomOutputStream o) throws DicomException, IOException {
		writeBase(o);
		if (valueLength > 0) {
			for (int i=0; i<files.length; ++i) {
				File file = files[i];
				long byteOffset = byteOffsets[i];
				long length = lengths[i];
				BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
				CopyStream.skipInsistently(in,byteOffset);
				CopyStream.copy(in,o,length);
				in.close();
			}
			long npad = getPaddedVL() - valueLength;
			while (npad-- > 0) o.write(0x00);
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
	 */
	public void removeValues() {
		files=null;
		byteOffsets=null;
		lengths=null;
		valueMultiplicity=0;
		valueLength=0;
	}

	public void deleteFilesWhenNoLongerNeeded() {
		deleteFilesWhenNoLongerNeeded=true;
	}

	protected void finalize() throws Throwable {
//System.err.println("OtherFloatAttributeMultipleFilesOnDisk.finalize()");
		if (deleteFilesWhenNoLongerNeeded) {
			if (files != null) {
				for (int i=0; i<files.length; ++i) {
					File file = files[i];
					if (file != null) {
						if (file.delete()) {
//System.err.println("OtherFloatAttributeMultipleFilesOnDisk.finalize(): Successfully deleted temporary file "+file);
						}
						else {
//System.err.println("OtherFloatAttributeMultipleFilesOnDisk.finalize(): Failed to delete temporary file "+file+" so adding to reaper list");
							FileReaper.addFileToDelete(file.getCanonicalPath());
						}
					}
					files[i]=null;
				}
				files=null;
			}
		}
		super.finalize();
	}

	/**
	 * <p>Get the value representation of this attribute (OF).</p>
	 *
	 * @return	'O','F' in ASCII as a two byte array; see {@link ValueRepresentation ValueRepresentation}
	 */
	public byte[] getVR() { return ValueRepresentation.OF; }
}

