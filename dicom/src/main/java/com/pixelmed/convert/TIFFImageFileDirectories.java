package com.pixelmed.convert;

import java.io.EOFException;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.ArrayList;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class encapsulating the content of one or more TIFF or BigTIFF Image File Directories (IFDs).</p>
 *
 * @author	dclunie
 */

public class TIFFImageFileDirectories {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/convert/TIFFImageFileDirectories.java,v 1.3 2022/02/27 11:07:29 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(TIFFImageFileDirectories.class);

	protected TIFFFile f;
	protected ArrayList<TIFFImageFileDirectory> ifdList;
	
	public TIFFFile getFile() { return f; }
	
	public ArrayList<TIFFImageFileDirectory> getListOfImageFileDirectories() { return ifdList; }
	
	public TIFFImageFileDirectories() {
		f = null;
		ifdList = new ArrayList<TIFFImageFileDirectory>();
	}

	public void read(String filename) throws EOFException, IOException, TIFFException {
		f = new TIFFFile(filename);

		long ifdOffset = f.getOffset();
		slf4jlogger.debug("read(): ifdOffset={}",ifdOffset);

		while (ifdOffset != 0) {
			TIFFImageFileDirectory ifd = new TIFFImageFileDirectory();
			ifdOffset = ifd.read(f,ifdOffset);
			ifdList.add(ifd);
			
			{
				TIFFImageFileDirectoryEntry subIFDOffsetsEntry = ifd.getEntry(TIFFTags.SUBIFD);	// if present, means that it is the top layer of OME-TIFF pyramid (001307)
				if (subIFDOffsetsEntry != null && subIFDOffsetsEntry.getNumberOfValues() > 0) {
					slf4jlogger.debug("read(): processing subIFDs");
					long[] subIFDOffsets = subIFDOffsetsEntry.getValues().getNumericValues();
					for (long subIFDOffset : subIFDOffsets) {
						slf4jlogger.debug("read(): subIFDOffset={}",subIFDOffset);
						TIFFImageFileDirectory subifd = new TIFFImageFileDirectory();
						subifd.read(f,subIFDOffset);		// ignore returned ifdOffsetNext, since each is a single subIFD referenced explicitly in subIFDOffsets
						ifdList.add(subifd);
					}
				}
			}

		}
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (f != null) {
			buf.append(f);
		}
		int dirNum = 0;
		String prefix = "";
		for (TIFFImageFileDirectory ifd : ifdList) {
			buf.append(prefix);
			buf.append("Directory ");
			buf.append(dirNum++);
			buf.append(": ");
			buf.append(ifd);
			prefix = "\n";
		}
		return buf.toString();
	}

	/**
	 * <p>Read TIFF or BigTIFF input file and extract its Image File Directories.</p>
	 *
	 * <p>Output to stderr mimics libtiff tiffdump tool, except that long lists of values are not truncated with ellipsis.</p>
	 *
	 * @param	arg	the TIFF or BigTIFF file 
	 */
	public static void main(String arg[]) {
		try {
			if (arg.length == 1) {
				TIFFImageFileDirectories ifds = new TIFFImageFileDirectories();
				ifds.read(arg[0]);
				System.err.print(ifds);
			}
			else {
				System.err.println("Error: Incorrect number of arguments");
				System.err.println("Usage: TIFFImageFileDirectories filename");
				System.exit(1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}

