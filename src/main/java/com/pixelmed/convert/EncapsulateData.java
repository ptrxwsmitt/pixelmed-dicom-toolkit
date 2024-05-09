package com.pixelmed.convert;

import com.pixelmed.apps.SetCharacteristicsFromSummary;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.CodingSchemeIdentification;
import com.pixelmed.dicom.CompositeInstanceContext;
import com.pixelmed.dicom.DateTimeAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.LongStringAttribute;
import com.pixelmed.dicom.OtherByteAttributeOnDisk;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.ShortTextAttribute;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.dicom.UnsignedLongAttribute;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class to create a DICOM encapsulated data object from a data file and supplied metadata and/or composite context.</p>
 *
 * <p>E.g., to encapsulate a PDF, CDA or STL file.</p>
 *
 * @author	dclunie
 */
public class EncapsulateData {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/convert/EncapsulateData.java,v 1.4 2019/05/10 16:41:30 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(EncapsulateData.class);
	
	public static String determineMediaTypeFromFile(String filename) {
		String mediaType = "";
		
		if (filename.toLowerCase().endsWith(".pdf")) {
			mediaType = "application/pdf";
		}
		else if (filename.toLowerCase().endsWith(".cda")) {
			mediaType = "text/xml";		// not standard, e.g., "http://wiki.hl7.org/index.php?title=Media-types_for_various_message_formats"
		}
		else if (filename.toLowerCase().endsWith(".stl")) {
			mediaType = "model/stl";
		}

		return mediaType;
	}

	public static String determineSOPClassFromMediaType(String mediaType) {
		String sopClassUID = "";
		
		if (mediaType.toLowerCase().equals("application/pdf")) {
			sopClassUID = SOPClass.EncapsulatedPDFStorage;
		}
		else if (mediaType.toLowerCase().equals("text/xml")) {	// really should check schema
			sopClassUID = SOPClass.EncapsulatedCDAStorage;
		}
		else if (mediaType.toLowerCase().equals("model/stl")) {
			sopClassUID = SOPClass.EncapsulatedSTLStorage;
		}

		return sopClassUID;
	}
	
	public static String determineModalityFromSOPClass(String sopClassUID) {
		String modality = "";
		
		if (sopClassUID.equals(SOPClass.EncapsulatedPDFStorage)) {
			modality = "DOC";
		}
		else if (sopClassUID.equals(SOPClass.EncapsulatedCDAStorage)) {
			modality = "DOC";
		}
		else if (sopClassUID.equals(SOPClass.EncapsulatedSTLStorage)) {
			modality = "M3D";
		}

		return modality;
	}
	
	/**
	 * <p>Create a DICOM encapsulated data object from a data file and supplied metadata</p>
	 *
	 * <p>The SOP Class will be automatically determined from the supplied file type.</p>
	 *
	 * @param	inputFileName				file containing data to be encapsulated
	 * @param	metadataFileName			file containing metadata to be included, may be null or empty string
	 * @param	compositeContextFileName	file containing DICOM patient and study composite context to reuse, may be null or empty string
	 * @param	outputFileName				file to write the DICOM encapsulated object to
	 * @throws	FileNotFoundException		if a file cannot be found
	 * @throws	IOException					if there is a problem reading or writing
	 * @throws	DicomException				if there is a problem parsing or extracting required content
	 */
	public EncapsulateData(String inputFileName,String metadataFileName,String compositeContextFileName,String outputFileName) throws FileNotFoundException, IOException, DicomException {
		String mediaType = determineMediaTypeFromFile(inputFileName);
		String sopClassUID = determineSOPClassFromMediaType(mediaType);
		
		if (sopClassUID.length() == 0) {
			throw new DicomException("Cannot deduce SOP Class UID for input file"+inputFileName);
		}

		String modality = determineModalityFromSOPClass(sopClassUID);

		AttributeList list = new AttributeList();

		CommonConvertedAttributeGeneration.generateCommonAttributes(list,
			"Nobody^Noname"/*patientName*/,"000000"/*patientID*/,
			"000000"/*studyID*/,"1"/*seriesNumber*/,"1"/*instanceNumber*/,
			modality,sopClassUID,
			false/*generateUnassignedConverted*/);

		{ Attribute a = new LongStringAttribute(TagFromName.MIMETypeOfEncapsulatedDocument); a.addValue(mediaType); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.ManufacturerModelName); a.addValue(this.getClass().getName()); list.put(a); }
		{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime); list.put(a); }		// Type 2
		{ Attribute a = new ShortTextAttribute(TagFromName.DocumentTitle); list.put(a); }			// Type 2
		{ Attribute a = new SequenceAttribute(TagFromName.ConceptNameCodeSequence); list.put(a); }	// Type 2

		CodingSchemeIdentification.replaceCodingSchemeIdentificationSequenceWithCodingSchemesUsedInAttributeList(list);
		
		{
			OtherByteAttributeOnDisk encapsulatedData = new OtherByteAttributeOnDisk(TagFromName.EncapsulatedDocument);
			File inputFile = new File(inputFileName);
			encapsulatedData.setFile(inputFile,0l/*byteOffset*/);
			list.put(encapsulatedData);
			
			long inputFileLength = inputFile.length();
			{ Attribute a = new UnsignedLongAttribute(TagFromName.EncapsulatedDocumentLength); a.addValue(inputFileLength); list.put(a); }
		}
		
		if (metadataFileName != null && metadataFileName.length() > 0) {
			new SetCharacteristicsFromSummary(metadataFileName,list);
		}
		if (compositeContextFileName != null && compositeContextFileName.length() > 0) {
			AttributeList srcList = new AttributeList();
			srcList.read(compositeContextFileName);
			CompositeInstanceContext cic = new CompositeInstanceContext(srcList,false/*forSR*/);
			cic.removeInstance();
			cic.removeSeries();
			cic.removeEquipment();
			list.putAll(cic.getAttributeList());
		}
		
		list.insertSuitableSpecificCharacterSetForAllStringValues();	// (001158)
		FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
		list.write(outputFileName,TransferSyntax.ExplicitVRLittleEndian,true,true);
	}
	
	/**
	 * <p>Create a DICOM encapsulated data object from a data file and supplied metadata.</p>
	 *
	 * <p>The SOP Class will be automatically determined from the supplied file type.</p>
	 *
	 * @param	arg	two to four parameters, the input data file, optionally a metadata file, optionally a patient/study composite context source DICOM file, and the output file
	 */
	public static void main(String arg[]) {
		try {
			String inputFileName = arg[0];
			String metadataFileName = "";
			String compositeContextFileName = "";
			String outputFileName = "";
			if (arg.length == 2) {
				outputFileName = arg[1];
			}
			else if (arg.length == 3) {
				metadataFileName = arg[1];
				outputFileName = arg[2];
			}
			else if (arg.length == 4) {
				metadataFileName = arg[1];
				compositeContextFileName = arg[2];
				outputFileName = arg[3];
			}
			else {
				System.err.println("Error: Incorrect number of arguments");
				System.err.println("Usage: EncapsulateData inputFile [metadataFile [compositeContextFile]] outputFile");
				System.exit(1);
			}
			new EncapsulateData(inputFileName,metadataFileName,compositeContextFileName,outputFileName);
		}
		catch (Exception e) {
			slf4jlogger.error("",e);	// use SLF4J since may be invoked from script
		}
	}

}

