/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

public interface ByteFrameSource {
	/**
	 * <p>Get the byte array for one selected frame.</p>
	 *
	 * @param	frameNumber		from 0
	 * @return					the values as an array of bytes (uncompressed or compressed
	 * @throws	DicomException	thrown if values cannot be read
	 */
	public byte[] getByteValuesForSelectedFrame(int frameNumber) throws DicomException;
}

