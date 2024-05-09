/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;

import javax.imageio.ImageIO;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.ImageReader;
//import javax.imageio.ImageReadParam;
//import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import java.util.Iterator;
import java.util.Locale;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import com.pixelmed.utils.StringUtilities;
import com.pixelmed.utils.XPathQuery;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class for encapsulating compressed grayscale or RGB consumer image format input files (that JIIO can recognize) into DICOM images of a specified SOP Class, or single or multi frame DICOM Secondary Capture images.</p>
 *
 * @author	dclunie
 */

public class EncapsulateImageInDicom {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/EncapsulateImageInDicom.java,v 1.8 2022/01/21 19:51:16 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(EncapsulateImageInDicom.class);

	// the following should work but does not return text values for nodes, which seem to be added as values of nodes ... is the JIIO metadata tree in some way incorrectly formed ? :(
	//private static String dumpTree(Node tree) {
	//	java.io.StringWriter out = new java.io.StringWriter();
	//	try {
	//		javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(tree);
	//		javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(out);
	//		javax.xml.transform.Transformer transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
	//		java.util.Properties outputProperties = new java.util.Properties();
	//		outputProperties.setProperty(javax.xml.transform.OutputKeys.METHOD,"xml");
	//		outputProperties.setProperty(javax.xml.transform.OutputKeys.INDENT,"yes");
	//		outputProperties.setProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION,"yes");
	//		outputProperties.setProperty(javax.xml.transform.OutputKeys.ENCODING,"UTF-8");	// the default anyway
	//		transformer.setOutputProperties(outputProperties);
	//		transformer.transform(source, result);
	//	}
	//	catch (Exception e) {
	//		slf4jlogger.error("",e);
	//	}
	//	return out.toString();
	//}

	private static String dumpTree(Node node,int indent) {
		StringBuffer str = new StringBuffer();
		
		//for (int i=0; i<indent; ++i) str.append("    ");
		//short nodeType = node.getNodeType();
		//str.append("NodeType = "+Integer.toString(nodeType)+"\n");
		
		String elementName = node.getNodeName();
		for (int i=0; i<indent; ++i) str.append("    ");
		str.append("<");
		str.append(elementName);
		if (node.hasAttributes()) {
			NamedNodeMap attrs = node.getAttributes();
			for (int j=0; j<attrs.getLength(); ++j) {
				Node attr = attrs.item(j);
				if (attr != null) {
					str.append(" ");
					str.append(attr.getNodeName());
					str.append("=\"");
					str.append(attr.getNodeValue());
					str.append("\"");
				}
			}
		}
		str.append(">");
		
		String nodeValue = node.getNodeValue();			// element nodes should not have values, per the Jaavdoc of org.w3c.dom.Node, yet in JPEG metadata, this is where the text is :(
		if (nodeValue != null) {
			str.append(nodeValue);
		}
		
		str.append("\n");
		
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			str.append(dumpTree(child,indent+1));
		}
		
		for (int i=0; i<indent; ++i) str.append("    ");
		str.append("</");
		str.append(elementName);
		str.append(">\n");
		return str.toString();
	}
	
	private static String dumpTree(Node node) {
		return dumpTree(node,0);
	}

	protected static String getCompressionType(Node metadata) {
		String compressionType = null;
		try {
			// the following should work but returns nothing ... is the JIIO metadata tree in some way incorrectly formed ? :(
			//compressionType = XPathFactory.newInstance().newXPath().evaluate("/javax_imageio_1.0/Compression/CompressionTypeName/@value",metadata);
			compressionType = XPathQuery.getNamedAttributeValueOfElementNode((Node)(XPathFactory.newInstance().newXPath().evaluate("//CompressionTypeName",metadata,XPathConstants.NODE)),"value");
		}
		catch (javax.xml.xpath.XPathExpressionException e) {
			slf4jlogger.error("",e);
		}
		return compressionType;
	}
	
	protected static short getBitsPerSample(Node metadata) {
		short bitsPerSample = 0;
		try {
			//String bitsPerSampleString = XPathFactory.newInstance().newXPath().evaluate("/javax_imageio_1.0//Data/BitsPerSample/@value",metadata);
			String bitsPerSampleString = XPathQuery.getNamedAttributeValueOfElementNode((Node)(XPathFactory.newInstance().newXPath().evaluate("//BitsPerSample",metadata,XPathConstants.NODE)),"value");
			if (bitsPerSampleString != null && bitsPerSampleString.length() > 0) {
				bitsPerSample = (short)(Integer.parseInt(bitsPerSampleString));
			}
		}
		catch (NumberFormatException e) {
			slf4jlogger.error("",e);
		}
		catch (javax.xml.xpath.XPathExpressionException e) {
			slf4jlogger.error("",e);
		}
		return bitsPerSample;
	}
	
	protected static String getPhotometricInterpretation(Node metadata) {
		String photometricInterpretation = null;
		try {
			String colorSpaceType = XPathQuery.getNamedAttributeValueOfElementNode((Node)(XPathFactory.newInstance().newXPath().evaluate("//ColorSpaceType",metadata,XPathConstants.NODE)),"name");
			slf4jlogger.debug("getPhotometricInterpretation(): colorSpaceType = {}",colorSpaceType);
			if (colorSpaceType != null && colorSpaceType.length() > 0) {
				if (colorSpaceType.equals("YCbCr")) {
					photometricInterpretation = "YBR_FULL_422";		// don't check actual chrominance downsampling since DICOM always uses this regardless
				}
			}
		}
		catch (javax.xml.xpath.XPathExpressionException e) {
			slf4jlogger.error("",e);
		}
		return photometricInterpretation;
	}
	
	protected static short getElementAttributeFromMetadata(Node metadata,String elementName,String attributeName) {
		short v = 0;
		try {
			String s = XPathQuery.getNamedAttributeValueOfElementNode((Node)(XPathFactory.newInstance().newXPath().evaluate("//"+elementName,metadata,XPathConstants.NODE)),attributeName);
			if (s != null && s.length() > 0) {
				v = (short)(Integer.parseInt(s));
			}
		}
		catch (NumberFormatException e) {
			slf4jlogger.error("",e);
		}
		catch (javax.xml.xpath.XPathExpressionException e) {
			slf4jlogger.error("",e);
		}
		return v;
	}
	
	public static short getColumns(Node metadata) {
		return getElementAttributeFromMetadata(metadata,"sof","samplesPerLine");
	}
	
	public static short getRows(Node metadata) {
		return getElementAttributeFromMetadata(metadata,"sof","numLines");
	}
	
	protected static short getSamplesPerPixel(Node metadata) {
		return getElementAttributeFromMetadata(metadata,"sof","numFrameComponents");		// could also use NumChannels value from JIIO tree rather than JPEG tree
	}
	
	protected static short getSamplePrecision(Node metadata) {
		return getElementAttributeFromMetadata(metadata,"sof","samplePrecision");
	}
	
	protected static short getJPEGProcess(Node metadata) {
		return getElementAttributeFromMetadata(metadata,"sof","process");
	}

	
	
	protected static String getTransferSyntaxUIDFromJPEGProcess(short process) {
		String transferSyntaxUID = null;
		if (process == 0 || process == 2) {
			transferSyntaxUID = TransferSyntax.JPEGBaseline;
		}
		return transferSyntaxUID;
	}

	/**
	 * <p>Read a consumer image format input file (anything JIIO can recognize), and create a single frame DICOM Image Pixel Module.</p>
	 *
	 * @param	inputFile		a compressed consumer format image file (e.g., 8 or &gt; 8 bit JPEG, JPEG 2000)
	 * @param	list			an existing (possibly empty) attribute list, if null, a new one will be created; may already include "better" image pixel module attributes to use
	 * @return					attribute list with Image Pixel Module (including Pixel Data) added
	 * @throws	IOException		if an I/O error occurs
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static AttributeList generateDICOMPixelModuleFromConsumerImageFile(String inputFile,AttributeList list) throws IOException, DicomException {
		return generateDICOMPixelModuleFromConsumerImageFile(new File(inputFile),list);
	}
	
	/**
	 * <p>Read a consumer image format input file (anything JIIO can recognize), and create a single frame DICOM Image Pixel Module.</p>
	 *
	 * @param	inputFile		a compressed consumer format image file (e.g., 8 or &gt; 8 bit JPEG, JPEG 2000)
	 * @param	list			an existing (possibly empty) attribute list, if null, a new one will be created; may already include "better" image pixel module attributes to use
	 * @return					attribute list with Image Pixel Module (including Pixel Data) added
	 * @throws	IOException		if an I/O error occurs
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static AttributeList generateDICOMPixelModuleFromConsumerImageFile(File inputFile,AttributeList list) throws IOException, DicomException {
		int numberOfFrames = 0;
		Node metadataTree = null;
		Node jpegMetadataTree = null;
		ImageReader reader = null;
		FileImageInputStream fiis = new FileImageInputStream(inputFile);
		{
			Iterator readers = ImageIO.getImageReaders(fiis);
			if (readers.hasNext()) {
				reader = (ImageReader)readers.next();	// assume 1st supplied reader is the "best" one to use :(
			}
			if (reader == null) {
				slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): Failed to find reader to get Metadata based on FileImageInputStream");
				String filename = inputFile.getName();
				int period = filename.lastIndexOf('.');
				if (period != -1 && period < filename.length()) {
					String fileSuffix = filename.substring(period+1);
					slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): fileSuffix {}",fileSuffix);
					readers = ImageIO.getImageReadersBySuffix(fileSuffix);
					if (readers.hasNext()) {
						reader = (ImageReader)readers.next();	// assume 1st supplied reader is the "best" one to use :(
					}
					if (reader == null) {
						slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): Failed to find reader to get Metadata based on fileSuffix {}",fileSuffix);
					}
				}
			}
		}
		
		
		if (reader == null) {
			throw new DicomException("Cannot obtain reader to get Metadata so will not convert "+inputFile);
		}
		else {
			slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): Using reader {} {} {}",reader.getOriginatingProvider().getDescription(Locale.US),reader.getOriginatingProvider().getVendorName(),reader.getOriginatingProvider().getVersion());
			reader.setInput(fiis);
			//try {
			//	numberOfFrames =  reader.getNumImages(true/*allowSearch*/);
			//}
			//catch (Exception e) {	// IOException or IllegalStateException
				numberOfFrames = 1;
			//}
			
			IIOMetadata metadata = reader.getImageMetadata(0);
			if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): metadata = {}",metadata);
			if (metadata != null) {
				String[] formatNames = metadata.getMetadataFormatNames();
				if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): formatNames = {}",StringUtilities.toString(formatNames));
				if (formatNames != null) {
					for (String formatName : formatNames) {
						if (formatName != null) {
							if (formatName.equals("javax_imageio_1.0")) {
								metadataTree = metadata.getAsTree(formatName);
								if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): {} JIIO tree = {}",formatName,dumpTree(metadataTree));
							}
							else if (formatName.equals("javax_imageio_jpeg_image_1.0")) {
								jpegMetadataTree = metadata.getAsTree(formatName);
								if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): {} JPEG tree = {}",formatName,dumpTree(jpegMetadataTree));
							}
							else {
								Node otherMetadataTree = metadata.getAsTree(formatName);
								if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): {} unrecognized tree = {}",formatName,dumpTree(otherMetadataTree));
							}
						}
					}
				}
			}
			try {
				slf4jlogger.trace("generateDICOMPixelModuleFromConsumerImageFile(): Calling dispose() on reader");
				reader.dispose();
			}
			catch (Exception e) {
				slf4jlogger.error("",e);
			}
		}
		fiis.close();
		
		byte[] iccProfileData = null;
		
		if (jpegMetadataTree == null) {
			throw new DicomException("Cannot obtain JPEG Metadata");
		}

		short rows = getRows(jpegMetadataTree);
		slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): rows = {}",rows);
		short columns = getColumns(jpegMetadataTree);
		slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): columns = {}",columns);

		Attribute pixelData = null;
		short bitsStored = getSamplePrecision(jpegMetadataTree);
		short bitsAllocated = (short)(bitsStored <= 8 ? 8 : 16);
		short highBit = (short)(bitsStored-1);
		short samplesPerPixel = getSamplesPerPixel(jpegMetadataTree);
		slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): samplesPerPixel = {}",samplesPerPixel);
		short pixelRepresentation = 0;
		String photometricInterpretation = samplesPerPixel == 3 ? getPhotometricInterpretation(metadataTree) : (samplesPerPixel == 1 ? "MONOCHROME2" : "");
		slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): photometricInterpretation = {}",photometricInterpretation);
		short planarConfiguration = 0;	// by pixel
		
		if (list == null) {
			list = new AttributeList();
		}
		
		{
			short jpegProcess = getJPEGProcess(jpegMetadataTree);
			slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): jpegProcess = {}",jpegProcess);
			String outputTransferSyntax = getTransferSyntaxUIDFromJPEGProcess(jpegProcess);
			slf4jlogger.debug("generateDICOMPixelModuleFromConsumerImageFile(): outputTransferSyntax = {}",outputTransferSyntax);
			if (outputTransferSyntax != null && outputTransferSyntax.length() > 0) {
				{ Attribute a = new UniqueIdentifierAttribute(TagFromName.TransferSyntaxUID); a.addValue(outputTransferSyntax); list.put(a); }
				{
					TransferSyntax ts = new TransferSyntax(outputTransferSyntax);
					if (ts != null) {
						String lossyImageCompression = ts.isLossy() ? "01" : "";		// unknown rather than "00" if don't know is lossy
						{ Attribute a = new CodeStringAttribute(TagFromName.LossyImageCompression); a.addValue(lossyImageCompression); list.put(a); }
					}
				}
			}
		}
		
		{
			File[] files = new File[1/*numberOfFrames*/];
			files[0] = inputFile;
			pixelData = new OtherByteAttributeMultipleCompressedFrames(TagFromName.PixelData,files);
		}
		
		if (pixelData != null) {
			list.put(pixelData);
			
			{
				int existingBitsStored = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsStored,-1);
				// only add it if not already present ... externally specified value is better than JIIO decoder
				if (existingBitsStored == -1) {
					{ Attribute a = new UnsignedShortAttribute(TagFromName.BitsStored); a.addValue(bitsStored); list.put(a); }
				}
				int existingHighBit = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.HighBit,-1);
				if (existingHighBit == -1) {
					if (existingBitsStored != -1) {
						highBit = (short)(existingBitsStored - 1);		// override assumed high bit with one less than externally specified BitsStored
					}
					{ Attribute a = new UnsignedShortAttribute(TagFromName.HighBit); a.addValue(highBit); list.put(a); }
				}
			}
			
			{
				int existingPixelRepresentation = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.PixelRepresentation,-1);
				// only add it if not already present ... externally specified value is better than JIIO decoder
				if (existingPixelRepresentation == -1) {
					{ Attribute a = new UnsignedShortAttribute(TagFromName.PixelRepresentation); a.addValue(pixelRepresentation); list.put(a); }
				}
			}
			
			{
				String existingPhotometricInterpretation = Attribute.getSingleStringValueOrNull(list,TagFromName.PhotometricInterpretation);
				// only add it if not already present ... externally specified value is better than JIIO decoder
				if (existingPhotometricInterpretation == null) {
					{ Attribute a = new CodeStringAttribute(TagFromName.PhotometricInterpretation); a.addValue(photometricInterpretation); list.put(a); }
				}
			}
			
			{ list.remove(TagFromName.BitsAllocated); Attribute a = new UnsignedShortAttribute(TagFromName.BitsAllocated); a.addValue(bitsAllocated); list.put(a); }
			{ list.remove(TagFromName.Rows); Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue(rows); list.put(a); }
			{ list.remove(TagFromName.Columns); Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue(columns); list.put(a); }
			
			list.remove(TagFromName.NumberOfFrames);
			if (numberOfFrames > 1) {
				Attribute a = new IntegerStringAttribute(TagFromName.NumberOfFrames); a.addValue(numberOfFrames); list.put(a);
			}
			
			{ list.remove(TagFromName.SamplesPerPixel); Attribute a = new UnsignedShortAttribute(TagFromName.SamplesPerPixel); a.addValue(samplesPerPixel); list.put(a); }
						
			list.remove(TagFromName.PlanarConfiguration);
			if (samplesPerPixel > 1) {
				 Attribute a = new UnsignedShortAttribute(TagFromName.PlanarConfiguration); a.addValue(planarConfiguration); list.put(a);
			}
			
			if (iccProfileData != null) {
				Attribute a = new OtherByteAttribute(TagFromName.ICCProfile);
				a.setValues(iccProfileData);	// will be padded to even length on write if necessary
				list.put(a);
			}
		}
		return list;
	}

	
	/**
	 * <p>Read a consumer image format input file (anything JIIO can recognize), and create a single frame DICOM Image Pixel Module.</p>
	 *
	 * @param	inputFile		a consumer format image file (e.g., 8 or &gt; 8 bit JPEG, JPEG 2000, GIF, etc.)
	 * @return					a new attribute list with Image Pixel Module (including Pixel Data) added
	 * @throws	IOException		if an I/O error occurs
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static AttributeList generateDICOMPixelModuleFromConsumerImageFile(String inputFile) throws IOException, DicomException {
		return generateDICOMPixelModuleFromConsumerImageFile(inputFile,null);
	}
	
	/**
	 * <p>Read a consumer image format input file (anything JIIO can recognize), and create a DICOM image of the specified SOP Class, or a single or multi frame DICOM Secondary Capture image.</p>
	 *
	 * @param	inputFile		consumer image format input file
	 * @param	outputFile		DICOM output image
	 * @param	patientName		patient name
	 * @param	patientID		patient ID
	 * @param	studyID			study ID
	 * @param	seriesNumber	series number
	 * @param	instanceNumber	instance number
	 * @param	modality		may be null
	 * @param	sopClass		may be null
	 * @param	sourceList		list of attributes to use rather than supplied or generated values, may be null or empty
	 * @throws	IOException		if an I/O error occurs
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public EncapsulateImageInDicom(String inputFile,String outputFile,String patientName,String patientID,String studyID,String seriesNumber,String instanceNumber,String modality,String sopClass,AttributeList sourceList)
			throws IOException, DicomException {

		AttributeList list = generateDICOMPixelModuleFromConsumerImageFile(inputFile);
		String outputTransferSyntax = Attribute.getSingleStringValueOrNull(list,TagFromName.TransferSyntaxUID);
		
		// various Type 1 and Type 2 attributes for mandatory SC modules ...
	
		UIDGenerator u = new UIDGenerator();	

		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber)); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(u.getNewSeriesInstanceUID(studyID,seriesNumber)); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(u.getNewStudyInstanceUID(studyID)); list.put(a); }

		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue("^^^^"); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.AccessionNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientOrientation); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Laterality); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.BurnedInAnnotation); a.addValue("YES"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.ImageType); a.addValue("DERIVED"); a.addValue("SECONDARY"); list.put(a); }
		
		{
			java.util.Date currentDateTime = new java.util.Date();
			{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(new java.text.SimpleDateFormat("yyyyMMdd").format(currentDateTime)); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(new java.text.SimpleDateFormat("HHmmss.SSS").format(currentDateTime)); list.put(a); }
		}
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.InstanceCreatorUID); a.addValue(VersionAndConstants.instanceCreatorUID); list.put(a); }
		
		int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfFrames,1);
		int samplesPerPixel = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.SamplesPerPixel,1);

		if (sopClass == null) {
			// if modality were not null, could actually attempt to guess SOP Class based on modality here :(
			sopClass = SOPClass.SecondaryCaptureImageStorage;
			if (numberOfFrames > 1) {
				if (samplesPerPixel == 1) {
					int bitsAllocated = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsAllocated,1);
					if (bitsAllocated == 8) {
						sopClass = SOPClass.MultiframeGrayscaleByteSecondaryCaptureImageStorage;
					}
					else if (bitsAllocated == 16) {
						sopClass = SOPClass.MultiframeGrayscaleWordSecondaryCaptureImageStorage;
					}
				}
				else if (samplesPerPixel == 3) {
					sopClass = SOPClass.MultiframeTrueColorSecondaryCaptureImageStorage;
				}
				// no current mechanism in generateDICOMPixelModuleFromConsumerImageFile() for creating MultiframeSingleBitSecondaryCaptureImageStorage, only 8 or 16
			}
		}

		if (numberOfFrames > 1) {
			{ AttributeTagAttribute a = new AttributeTagAttribute(TagFromName.FrameIncrementPointer); a.addValue(TagFromName.PageNumberVector); list.put(a); }
			{
				Attribute a = new IntegerStringAttribute(TagFromName.PageNumberVector);
				for (int page=1; page <= numberOfFrames; ++page) {
					a.addValue(page);
				}
				list.put(a);
			}
		}

		if (SOPClass.isMultiframeSecondaryCaptureImageStorage(sopClass)) {
			if (samplesPerPixel == 1) {
				{ Attribute a = new CodeStringAttribute(TagFromName.PresentationLUTShape); a.addValue("IDENTITY"); list.put(a); }
				{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleSlope); a.addValue("1"); list.put(a); }
				{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleIntercept); a.addValue("0"); list.put(a); }
				{ Attribute a = new LongStringAttribute(TagFromName.RescaleType); a.addValue("US"); list.put(a); }
			}
		}

		slf4jlogger.debug("SOP Class = {}",sopClass);
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(sopClass); list.put(a); }
		
		if (SOPClass.isSecondaryCaptureImageStorage(sopClass)) {
			{ Attribute a = new CodeStringAttribute(TagFromName.ConversionType); a.addValue("WSD"); list.put(a); }
		}
		else {
			{ Attribute a = new SequenceAttribute(TagFromName.AcquisitionContextSequence); list.put(a); }
		}

		if (modality == null) {
			// could actually attempt to guess modality based on SOP Class here :(
			modality = "OT";
		}
		{ Attribute a = new CodeStringAttribute(TagFromName.Modality); a.addValue(modality); list.put(a); }
		
		
		if (sourceList != null) {
			list.putAll(sourceList);
		}
		
		CodingSchemeIdentification.replaceCodingSchemeIdentificationSequenceWithCodingSchemesUsedInAttributeList(list);
		list.insertSuitableSpecificCharacterSetForAllStringValues();	// (001158)
		FileMetaInformation.addFileMetaInformation(list,outputTransferSyntax,"OURAETITLE");
		list.write(outputFile,TransferSyntax.ExplicitVRLittleEndian,true,true);
	}
	
	/**
	 * <p>Read a compressed grayscale or RGB consumer image format input file (that JIIO can recognize), and encapsulate it in an image of the specified SOP Class, or a single or multi frame DICOM Secondary Capture image.</p>
	 *
	 * @param	arg	seven, eight or nine parameters, the inputFile, outputFile, patientName, patientID, studyID, seriesNumber, instanceNumber, and optionally the modality, and SOP Class
	 */
	public static void main(String arg[]) {
		String modality = null;
		String sopClass = null;
		try {
			if (arg.length == 7) {
			}
			else if (arg.length == 8) {
				modality = arg[7];
			}
			else if (arg.length == 9) {
				modality = arg[7];
				sopClass = arg[8];
			}
			else {
				System.err.println("Error: Incorrect number of arguments");
				System.err.println("Usage: EncapsulateImageInDicom inputFile outputFile patientName patientID studyID seriesNumber instanceNumber [modality [SOPClass]]");
				System.exit(1);
			}
			new EncapsulateImageInDicom(arg[0],arg[1],arg[2],arg[3],arg[4],arg[5],arg[6],modality,sopClass,null/*sourceList*/);
		}
		catch (Exception e) {
			slf4jlogger.error("",e);	// use SLF4J since may be invoked from script
		}
	}
}
