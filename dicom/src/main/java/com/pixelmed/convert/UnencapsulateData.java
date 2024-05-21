package com.pixelmed.convert;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.BinaryInputStream;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.OtherByteAttributeOnDisk;
import com.pixelmed.dicom.TagFromName;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class to extract the content of a DICOM encapsulated data object into a file.</p>
 *
 * <p>E.g., to extract an encapsulated PDF, CDA or STL file.</p>
 *
 * @author	dclunie
 */
public class UnencapsulateData {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/convert/UnencapsulateData.java,v 1.3 2019/05/10 16:41:30 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(UnencapsulateData.class);
	
	private static final int BUFSIZE = 32768;
	
	/**
	 * <p>Extract the content of a DICOM encapsulated data object into a file.</p>
	 *
	 * <p>The SOP Class will be automatically determined from the supplied file type.</p>
	 *
	 * @param	inputFileName				DICOM file containing encapsulated data
	 * @param	outputFileName				file to write the encapsulated data to
	 * @throws	FileNotFoundException		if a file cannot be found
	 * @throws	IOException					if there is a problem reading or writing
	 * @throws	DicomException				if there is a problem parsing or extracting required content
	 */
	public UnencapsulateData(String inputFileName,String outputFileName) throws FileNotFoundException, IOException, DicomException {
		AttributeList list = new AttributeList();
		list.read(inputFileName);
		Attribute encapsulatedDocument = list.get(TagFromName.EncapsulatedDocument);
		if (encapsulatedDocument == null) {
			throw new DicomException("Missing EncapsulatedDocument Attribute");
		}
		long encapsulatedDocumentLength = Attribute.getSingleLongValueOrDefault(list,TagFromName.EncapsulatedDocumentLength,0l);
		if (encapsulatedDocumentLength == 0) {
			encapsulatedDocumentLength = encapsulatedDocument.getVL();
		}
		slf4jlogger.debug("UnencapsulateData(): encapsulatedDocumentLength = {}",encapsulatedDocumentLength);

		OutputStream o = new FileOutputStream(outputFileName);
		if (encapsulatedDocumentLength > 0) {
			if (encapsulatedDocument instanceof OtherByteAttributeOnDisk) {
				slf4jlogger.debug("UnencapsulateData(): OtherByteAttributeOnDisk");
				long byteOffset = ((OtherByteAttributeOnDisk)encapsulatedDocument).getByteOffset();
				File file = ((OtherByteAttributeOnDisk)encapsulatedDocument).getFile();
				int bufferSize = encapsulatedDocumentLength < BUFSIZE ? ((int)encapsulatedDocumentLength) : BUFSIZE;
				byte[] buffer = new byte[bufferSize];
				try {
					BinaryInputStream i = new BinaryInputStream(new FileInputStream(file),false/*bigEndian - byte order is irrelevant*/);
					i.skipInsistently(byteOffset);
					long remaining = encapsulatedDocumentLength;
					while (remaining > 0) {
						int count = remaining > BUFSIZE ? BUFSIZE : ((int)remaining);
						i.readInsistently(buffer,0,count);
						o.write(buffer,0,count);
						remaining -= count;
					}
					i.close();
				}
				catch (IOException e) {
					slf4jlogger.error("", e);
					throw new DicomException("Failed to read value (length "+encapsulatedDocumentLength+" dec) in read of EncapsulatedDocument from disk");
				}
			}
			else {
				slf4jlogger.debug("UnencapsulateData(): OtherByteAttribute in memory");
				byte[] values = encapsulatedDocument.getByteValues();	// will never be larger than Integer.MAX_VALUE
				o.write(values,0,(int)encapsulatedDocumentLength);		// so OK to downcast long encapsulatedDocumentLength
			}
		}
		o.close();
	}
	
	/**
	 * <p>Extract the content of a DICOM encapsulated data object into a file.</p>
	 *
	 * @param	arg	two parameters, the input DICOM file and the output file
	 */
	public static void main(String arg[]) {
		try {
			if (arg.length == 2) {
				String inputFileName = arg[0];
				String outputFileName = arg[1];
				new UnencapsulateData(inputFileName,outputFileName);
			}
			else {
				System.err.println("Error: Incorrect number of arguments");
				System.err.println("Usage: UnencapsulateData inputFile outputFile");
				System.exit(1);
			}
		}
		catch (Exception e) {
			slf4jlogger.error("",e);	// use SLF4J since may be invoked from script
		}
	}

}

