/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.CompositeInstanceContext;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomFileUtilities;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.MoveDicomFilesIntoHierarchy;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SetOfDicomFiles;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.ValueRepresentation;

import com.pixelmed.display.SourceImage;

import com.pixelmed.utils.FileUtilities;
import com.pixelmed.utils.HexDump;

import java.awt.color.ColorSpace;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class to compare two sets of DICOM files.</p>
 *
 * @author	dclunie
 */
public class CompareSetsOfDicomFiles {
	private static final String identString = "@(#) $Header: $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(CompareSetsOfDicomFiles.class);
	
	private class StringPair implements Comparable {
		String string1;
		String string2;
		
		public StringPair(String string1,String string2) {
			this.string1 = string1;
			this.string2 = string2;
		}

		public int compareTo(Object o) {
			int result = -1;
			if (o instanceof StringPair) {
				StringPair osp = (StringPair)o;
				result = this.string1.compareTo(osp.string1);
				if (result == 0) {
					result = this.string2.compareTo(osp.string2);
				}
			}
			return result;
		}
		
		public boolean equals(Object o) {
			return compareTo(o) == 0;
		}
		
		public int hashCode() {
			return string1.hashCode()+string2.hashCode();
		}

	}
	
	private static SetOfDicomFiles buildSetOfDicomFiles(String path) {
		SetOfDicomFiles setOfDicomFiles = new SetOfDicomFiles();
		Collection<File> files = FileUtilities.listFilesRecursively(new File(path));
		for (File f : files) {
			try {
				if (DicomFileUtilities.isDicomOrAcrNemaFile(f)) {
					setOfDicomFiles.add(f.getCanonicalPath(),false/*keepList*/,false/*keepPixelData*/);
				}
			}
			catch (Exception e) {
				slf4jlogger.debug("While reading \"{}\"",f,e);	// do NOT call f.getCanonicalPath(), since may throw Exception !
			}
		}
		return setOfDicomFiles;
	}

	// returns false if digest should not be used, e.g., all zeroes
	public static boolean updateDigest(MessageDigest md,BufferedImage srcImage) throws DicomException, IOException {
		boolean success = false;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		int srcWidth = srcImage.getWidth();
		int srcHeight = srcImage.getHeight();

		ColorModel srcColorModel = srcImage.getColorModel();
		SampleModel srcSampleModel = srcImage.getSampleModel();
		WritableRaster srcRaster = srcImage.getRaster();
		DataBuffer srcDataBuffer = srcRaster.getDataBuffer();
		int srcDataBufferOffset = srcDataBuffer.getOffset();
		int srcNumBands = srcRaster.getNumBands();

		if (srcDataBuffer instanceof DataBufferUShort) {
			short srcPixels[] = ((DataBufferUShort)srcDataBuffer).getData();
			int srcPixelsLength = srcPixels.length;
			for (int srcY=0; srcY<srcHeight; ++srcY) {
				int srcRowOffset = srcDataBufferOffset + srcY*srcWidth;
				for (int srcX=0; srcX<srcWidth; ++srcX) {
					short sampleValue = srcPixels[srcRowOffset+srcX];
					if (sampleValue != 0) {
						success = true;
					}
					// little endian
					bos.write(sampleValue&0xff);
					bos.write((sampleValue>>8)&0xff);
				}
			}
		}
		else if (srcDataBuffer instanceof DataBufferByte) {
			byte srcPixels[] = ((DataBufferByte)srcDataBuffer).getData();
			int srcPixelsLength = srcPixels.length;
			for (int srcY=0; srcY<srcHeight; ++srcY) {
				int srcRowOffset = srcDataBufferOffset + srcY*srcWidth;
				for (int srcX=0; srcX<srcWidth; ++srcX) {
					byte sampleValue = srcPixels[srcRowOffset+srcX];
					if (sampleValue != 0) {
						success = true;
					}
					bos.write(sampleValue);
				}
			}
		}
		else {
			throw new DicomException("Unsupported BufferedImage type");
		}
        md.update(bos.toByteArray());
        return success;
	}

	private class FingerPrint {
		String pixelDataDigestAsString;
		
		String getDigestAsString() { return pixelDataDigestAsString; }
		
		public FingerPrint(SetOfDicomFiles.DicomFile df) throws DicomException, IOException, NoSuchAlgorithmException {
			String fileName = df.getFileName();
			if (fileName != null && fileName.length() > 0) {
				String sopClassUID = df.getSOPClassUID();
				if (sopClassUID != null && sopClassUID.length() > 0) {
					if (SOPClass.isImageStorage(sopClassUID)) {
						DicomInputStream i = new DicomInputStream(new BufferedInputStream(new FileInputStream(fileName)));
						AttributeList list = new AttributeList();
						list.setDecompressPixelData(false);	// defer decompression unitl (frame) read
						list.read(i);
						// compute has from BufferedImage so as not to have to manually deal with all permutations, of number of samples, bit depth, compression, etc.
						SourceImage sImg = new SourceImage(list);
						MessageDigest md = MessageDigest.getInstance("MD5");
						int nframes = sImg.getNumberOfFrames();
						boolean success = false;
						for (int f=0; f<nframes; ++f) {
							BufferedImage bi = sImg.getBufferedImage(f);
							boolean frameSuccess = updateDigest(md,bi);
							success = frameSuccess || success;
						}
						if (success) {
							byte [] pixelDataDigest = md.digest();
							pixelDataDigestAsString = HexDump.byteArrayToHexString(pixelDataDigest);
							slf4jlogger.debug("file \"{}\" fingerprint image message digest {}",fileName,pixelDataDigestAsString);
						}
						else {
							slf4jlogger.error("file \"{}\" fingerprint image message digest creation failed (e.g., all zero pixel values)",fileName);
						}
					}
					else {
						throw new DicomException("FingerPrint extraction - unsupported SOP Class UID in \""+fileName+"\"");
					}
				}
				else {
					throw new DicomException("FingerPrint extraction - missing SOP Class UID in \""+fileName+"\"");
				}
			}
			else {
				throw new DicomException("FingerPrint extraction - missing filename in "+df.toString());
			}
		}
	}
	
	private void buildFingerPrints(
			SetOfDicomFiles setOfDicomFiles,
			Map<String,FingerPrint> fileNameToFingerPrints,
			Map<String,SetOfDicomFiles> fingerPrintDigestStringToSetOfDicomFiles,
			Set<String> failedFileNames) {
		Iterator<SetOfDicomFiles.DicomFile> i = setOfDicomFiles.iterator();
		while (i.hasNext()) {
			SetOfDicomFiles.DicomFile df = i.next();
			String fileName = df.getFileName();
			if (fileName != null && fileName.length() > 0) {
				if (fileNameToFingerPrints.get(fileName) == null) {
					try {
						FingerPrint fingerPrint = new FingerPrint(df);
						fileNameToFingerPrints.put(fileName,fingerPrint);
						String digestAsString = fingerPrint.getDigestAsString();
						
						// may be more than one file in the set being fingerprinted that has the same fingerprint (e.g., two SMPTE patterns
						SetOfDicomFiles dfs = fingerPrintDigestStringToSetOfDicomFiles.get(digestAsString);
						if (dfs == null) {
							dfs = new SetOfDicomFiles();
							fingerPrintDigestStringToSetOfDicomFiles.put(digestAsString,dfs);
						}
						dfs.add(df);
					}
					catch (Exception e) {
						slf4jlogger.error("file \"{}\" fingerprint extraction failed {}",fileName,e);
						failedFileNames.add(fileName);
					}
				}
				else {
					slf4jlogger.error("file \"{}\" has already had fingerprint extracted - ignoring duplicate",fileName);
				}
			}
			else {
				slf4jlogger.error("DicomFile {} has no file name - cannot add fingerprint to map",df.toString());
			}
		}
	}
	
	private AttributeList readAttributeListWithoutPixelData(SetOfDicomFiles.DicomFile df,String reasonForRead) {
		AttributeList list = new AttributeList();
		String fileName = df.getFileName();
		if (fileName != null && fileName.length() > 0) {
			try {
				DicomInputStream i = new DicomInputStream(new BufferedInputStream(new FileInputStream(fileName)));
				list.setDecompressPixelData(false);
				list.read(i,TagFromName.PixelData);
			}
			catch (Exception e) {
				slf4jlogger.error("file \"{}\" {} failed to {}",fileName,reasonForRead,e);
			}
		}
		else {
			slf4jlogger.error("DicomFile {} has no file name - cannot {}",df.toString(),reasonForRead);
		}
		return list;
	}
	
	private CompositeInstanceContext extractCompositeInstanceContext(SetOfDicomFiles.DicomFile df) {
		CompositeInstanceContext cic = null;
		AttributeList list = readAttributeListWithoutPixelData(df,"extract CompositeInstanceContext");
		if (list != null) {
			cic = new CompositeInstanceContext(list,false/*isSR*/);
			{
				AttributeList cicList;
				cicList = cic.getAttributeList();
				cicList.remove(TagFromName.PatientIdentityRemoved);
				cicList.remove(TagFromName.DeidentificationMethod);
				cicList.remove(TagFromName.DeidentificationMethodCodeSequence);
			}
		}
		return cic;
	}
	
	private void extractCompositeInstanceContextForMatchingFiles(
			Map<String,Set<String>> mapOfFirstSetStudyInstanceUIDToSecondSetStudyInstanceUIDs,
			Map<CompositeInstanceContext,Set<CompositeInstanceContext>> mapOfFirstSetStudyCICToSecondSetStudyCICs,
			Map<String,Set<String>> mapOfFirstSetSeriesInstanceUIDToSecondSetSeriesInstanceUIDs,
			Map<CompositeInstanceContext,Set<CompositeInstanceContext>> mapOfFirstSetSeriesCICToSecondSetSeriesCICs,
			SetOfDicomFiles inFirstSet,
			SetOfDicomFiles inSecondSet) {

		Set<String> firstSetStudyInstanceUIDs =  new HashSet<String>();
		Set<String> secondSetStudyInstanceUIDs =  new HashSet<String>();
		
		Set<String> firstSetSeriesInstanceUIDs =  new HashSet<String>();
		Set<String> secondSetSeriesInstanceUIDs =  new HashSet<String>();
		
		Set<CompositeInstanceContext> firstSetStudyCICs =  new HashSet<CompositeInstanceContext>();
		Set<CompositeInstanceContext> secondSetStudyCICs =  new HashSet<CompositeInstanceContext>();
		
		Set<CompositeInstanceContext> firstSetSeriesCICs =  new HashSet<CompositeInstanceContext>();
		Set<CompositeInstanceContext> secondSetSeriesCICs =  new HashSet<CompositeInstanceContext>();
		{
			Iterator<SetOfDicomFiles.DicomFile> it = inFirstSet.iterator();
			while (it.hasNext()) {
				SetOfDicomFiles.DicomFile df = it.next();
				CompositeInstanceContext cic = extractCompositeInstanceContext(df);
				if (cic != null) {
					AttributeList list = cic.getAttributeList();
					{
						String studyInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyInstanceUID);
						if (studyInstanceUID.length() > 0) {
							firstSetStudyInstanceUIDs.add(studyInstanceUID);
						}
						else {
							slf4jlogger.error("DicomFile {} has Study Instance UID",df.toString());
						}
					}
					{
						String seriesInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesInstanceUID);
						if (seriesInstanceUID.length() > 0) {
							firstSetSeriesInstanceUIDs.add(seriesInstanceUID);
						}
						else {
							slf4jlogger.error("DicomFile {} has Series Instance UID",df.toString());
						}
					}
					{
						CompositeInstanceContext cicStudy = new CompositeInstanceContext(list,false/*isSR*/);
						cicStudy.removeAllButStudy();
						firstSetStudyCICs.add(cicStudy);
					}
					{
						CompositeInstanceContext cicSeries = new CompositeInstanceContext(list,false/*isSR*/);
						cicSeries.removeAllButSeries();
						firstSetSeriesCICs.add(cicSeries);
					}
				}
			}
		}
		{
			Iterator<SetOfDicomFiles.DicomFile> it = inSecondSet.iterator();
			while (it.hasNext()) {
				SetOfDicomFiles.DicomFile df = it.next();
				CompositeInstanceContext cic = extractCompositeInstanceContext(df);
				if (cic != null) {
					AttributeList list = cic.getAttributeList();
					{
						String studyInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyInstanceUID);
						if (studyInstanceUID.length() > 0) {
							secondSetStudyInstanceUIDs.add(studyInstanceUID);
						}
						else {
							slf4jlogger.error("DicomFile {} missing Study Instance UID",df.toString());
						}
					}
					{
						String seriesInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesInstanceUID);
						if (seriesInstanceUID.length() > 0) {
							secondSetSeriesInstanceUIDs.add(seriesInstanceUID);
						}
						else {
							slf4jlogger.error("DicomFile {} missing Series Instance UID",df.toString());
						}
					}
					{
						CompositeInstanceContext cicStudy = new CompositeInstanceContext(list,false/*isSR*/);
						cicStudy.removeAllButStudy();
						secondSetStudyCICs.add(cicStudy);
					}
					{
						CompositeInstanceContext cicSeries = new CompositeInstanceContext(list,false/*isSR*/);
						cicSeries.removeAllButSeries();
						secondSetSeriesCICs.add(cicSeries);
					}
				}
			}
		}
		
		for (String firstStudyInstanceUID : firstSetStudyInstanceUIDs) {
			Set<String> secondSetStudyInstanceUIDsInMap = mapOfFirstSetStudyInstanceUIDToSecondSetStudyInstanceUIDs.get(firstStudyInstanceUID);
			if (secondSetStudyInstanceUIDsInMap == null) {
				secondSetStudyInstanceUIDsInMap = new HashSet<String>();
				mapOfFirstSetStudyInstanceUIDToSecondSetStudyInstanceUIDs.put(firstStudyInstanceUID,secondSetStudyInstanceUIDsInMap);
			}
			for (String secondStudyInstanceUID: secondSetStudyInstanceUIDs) {
				secondSetStudyInstanceUIDsInMap.add(secondStudyInstanceUID);
			}
		}
		
		for (String firstSeriesInstanceUID : firstSetSeriesInstanceUIDs) {
			Set<String> secondSetSeriesInstanceUIDsInMap = mapOfFirstSetSeriesInstanceUIDToSecondSetSeriesInstanceUIDs.get(firstSeriesInstanceUID);
			if (secondSetSeriesInstanceUIDsInMap == null) {
				secondSetSeriesInstanceUIDsInMap = new HashSet<String>();
				mapOfFirstSetSeriesInstanceUIDToSecondSetSeriesInstanceUIDs.put(firstSeriesInstanceUID,secondSetSeriesInstanceUIDsInMap);
			}
			for (String secondSeriesInstanceUID: secondSetSeriesInstanceUIDs) {
				secondSetSeriesInstanceUIDsInMap.add(secondSeriesInstanceUID);
			}
		}

		for (CompositeInstanceContext firstStudyCIC: firstSetStudyCICs) {
			Set<CompositeInstanceContext> secondSetStudyCICsInMap = mapOfFirstSetStudyCICToSecondSetStudyCICs.get(firstStudyCIC);
			if (secondSetStudyCICsInMap == null) {
				secondSetStudyCICsInMap = new HashSet<CompositeInstanceContext>();
				mapOfFirstSetStudyCICToSecondSetStudyCICs.put(firstStudyCIC,secondSetStudyCICsInMap);
			}
			for (CompositeInstanceContext secondStudyCIC: secondSetStudyCICs) {
				secondSetStudyCICsInMap.add(secondStudyCIC);
			}
		}

		for (CompositeInstanceContext firstSeriesCIC: firstSetSeriesCICs) {
			Set<CompositeInstanceContext> secondSetSeriesCICsInMap = mapOfFirstSetSeriesCICToSecondSetSeriesCICs.get(firstSeriesCIC);
			if (secondSetSeriesCICsInMap == null) {
				secondSetSeriesCICsInMap = new HashSet<CompositeInstanceContext>();
				mapOfFirstSetSeriesCICToSecondSetSeriesCICs.put(firstSeriesCIC,secondSetSeriesCICsInMap);
			}
			for (CompositeInstanceContext secondSeriesCIC: secondSetSeriesCICs) {
				secondSetSeriesCICsInMap.add(secondSeriesCIC);
			}
		}
	}

	private void extractCompositeInstanceContextForUnmatchedFiles(
			Set<String> setOfStudyInstanceUIDs,
			Set<CompositeInstanceContext> setOfStudyCICs,
			Set<String> setOfSeriesInstanceUIDs,
			Set<CompositeInstanceContext> setOfSeriesCICs,
			SetOfDicomFiles setOfDicomFiles) {
		{
			Iterator<SetOfDicomFiles.DicomFile> it = setOfDicomFiles.iterator();
			while (it.hasNext()) {
				SetOfDicomFiles.DicomFile df = it.next();
				CompositeInstanceContext cic = extractCompositeInstanceContext(df);
				if (cic != null) {
					AttributeList list = cic.getAttributeList();
					{
						String studyInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyInstanceUID);
						if (studyInstanceUID.length() > 0) {
							setOfStudyInstanceUIDs.add(studyInstanceUID);
						}
						else {
							slf4jlogger.error("DicomFile {} missing Study Instance UID",df.toString());
						}
					}
					{
						String seriesInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesInstanceUID);
						if (seriesInstanceUID.length() > 0) {
							setOfSeriesInstanceUIDs.add(seriesInstanceUID);
						}
						else {
							slf4jlogger.error("DicomFile {} missing Series Instance UID",df.toString());
						}
					}
					{
						CompositeInstanceContext cicStudy = new CompositeInstanceContext(list,false/*isSR*/);
						cicStudy.removeAllButStudy();
						setOfStudyCICs.add(cicStudy);
					}
					{
						CompositeInstanceContext cicSeries = new CompositeInstanceContext(list,false/*isSR*/);
						cicSeries.removeAllButSeries();
						setOfSeriesCICs.add(cicSeries);
					}
				}
			}
		}
	}
	
	private static MoveDicomFilesIntoHierarchy moverForSummary = new MoveDicomFilesIntoHierarchy();
	
	private String summarizeStudy(CompositeInstanceContext cic) {
		AttributeList list = cic.getAttributeList();
		return
			moverForSummary.makeStudyLabelFromAttributes(list,MoveDicomFilesIntoHierarchy.FolderNamingStrategy.instanceByUIDUncleaned)
			+ " ("
			+ Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyInstanceUID)
			+ ")"
			;
	}
	
	private String summarizeSeries(CompositeInstanceContext cic) {
		AttributeList list = cic.getAttributeList();
		return
			moverForSummary.makeSeriesLabelFromAttributes(list,MoveDicomFilesIntoHierarchy.FolderNamingStrategy.instanceByUIDUncleaned)
			+ " ("
			+ Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesInstanceUID)
			+ ")"
			;
	}

	// Fetching Annex E stuff is derived from com.pixelmed.test.TestDeidentify - should refactor :(
	// except that instead of building "/tmp/confidentialityprofiledataelementsandhandling.xml" in Makefile
	// extraction of current Annex E has already been done and added to jar file as a resource

	private String xmlConfidentialityProfileDataElementsResourceName = "/com/pixelmed/dicom/confidentialityprofiledataelementsandhandling.xml";

	private Document readDataElementHandlingFile() throws IOException, ParserConfigurationException, SAXException {
		InputStream i = this.getClass().getResourceAsStream(xmlConfidentialityProfileDataElementsResourceName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(i);
	}

	private SortedSet<AttributeTag> getAttributeTagsOfDataElementsFromFile() throws IOException, ParserConfigurationException, SAXException {
		SortedSet<AttributeTag> tags = new TreeSet<AttributeTag>();
		Document document = readDataElementHandlingFile();
		Element root = document.getDocumentElement();
		if (root.getTagName().equals("DataElements")) {
			NodeList deNodes = root.getChildNodes();
			for (int i=0; i<deNodes.getLength(); ++i) {
				Node deNode = deNodes.item(i);
				if (deNode.getNodeType() == Node.ELEMENT_NODE && ((Element)deNode).getTagName().equals("DataElement")) {
					String name = "";
					String group = "";
					String element = "";
					String action = "";
					{
						NamedNodeMap attributes = deNode.getAttributes();
						if (attributes != null) {
							{
								Node attribute = attributes.getNamedItem("name");
								if (attribute != null) {
									name = attribute.getTextContent();
								}
							}
							{
								Node attribute = attributes.getNamedItem("group");
								if (attribute != null) {
									group = attribute.getTextContent();
								}
							}
							{
								Node attribute = attributes.getNamedItem("element");
								if (attribute != null) {
									element = attribute.getTextContent();
								}
							}
							{
								Node attribute = attributes.getNamedItem("action");
								if (attribute != null) {
									action = attribute.getTextContent();
								}
							}
						}
					}
					if (group.length() > 0 && element.length() > 0 && !name.toLowerCase().trim().equals("private attributes")) {
						AttributeTag tag = new AttributeTag(
											   Integer.parseInt(group.replaceAll("[Xx]","0"),16),	// e.g., 50xx curve and 60xx overlay groups
											   Integer.parseInt(element.replaceAll("[Xx]","0"),16)	// e.g., curve data 50xx,xxxx
											   );
						tags.add(tag);
					}
					else {
						slf4jlogger.debug("getAttributeTagsOfDataElementsFromFile(): Ignoring "+name);
					}
				}
			}
		}
		return tags;
	}

	private static final DicomDictionary dictionary = DicomDictionary.StandardDictionary;

	private SortedSet<AttributeTag> confidentialityProfileDataElementTags;

	private static final AttributeTag curveDataTag = new AttributeTag(0x5000,0x0000);
	private static final AttributeTag overlayDataTag = new AttributeTag(0x6000,0x3000);
	private static final AttributeTag graphicAnnotationSequence = TagFromName.GraphicAnnotationSequence;
	private static final AttributeTag acquisitionContextSequence = TagFromName.AcquisitionContextSequence;
	private static final AttributeTag contentSequence = TagFromName.ContentSequence;

	// derived from com.pixelmed.test.TestDeidentify.checkIsDeidentifiedproperly
	// really should recurse
	private void checkIsDeidentifiedproperly(
			SetOfDicomFiles.DicomFile df1,
			SetOfDicomFiles.DicomFile df2,
			Map<AttributeTag,SortedSet<StringPair>> changedValuesForTag,
			Map<AttributeTag,SortedSet<String>> unchangedValuesForTag
	  ) {
		AttributeList list1 = readAttributeListWithoutPixelData(df1,"check second set is de-identified");
		AttributeList list2 = readAttributeListWithoutPixelData(df2,"check second set is de-identified");
		
		String fileName1 = df1.getFileName();
		String fileName2 = df2.getFileName();

		if (confidentialityProfileDataElementTags == null) {
			try {
				confidentialityProfileDataElementTags = getAttributeTagsOfDataElementsFromFile();
			}
			catch (Exception e) {
				slf4jlogger.error("Unable to load confidentialityProfileDataElementTags {}",e);
			}
		}
		
		SortedSet<AttributeTag> allTags = new TreeSet<AttributeTag>();
		{
			Set<AttributeTag> tags1 = list1.keySet();
			Set<AttributeTag> tags2 = list2.keySet();
			allTags.addAll(tags1);
			allTags.addAll(tags2);
		}
		
		//for (AttributeTag tag : confidentialityProfileDataElementTags) {
		for (AttributeTag tag : allTags) {
			//System.err.println("Testing "+tag);
			if (tag.equals(curveDataTag)) {
				slf4jlogger.debug("Not checking curveDataTag {}",tag.toString(dictionary));
			}
			else if (tag.equals(overlayDataTag)) {
				slf4jlogger.debug("Not checking overlayDataTag {}",tag.toString(dictionary));
			}
			else if (tag.equals(graphicAnnotationSequence)) {
				slf4jlogger.debug("Not checking graphicAnnotationSequence {}",tag.toString(dictionary));
			}
			else if (tag.equals(acquisitionContextSequence)) {
				slf4jlogger.debug("Not checking acquisitionContextSequence {}",tag.toString(dictionary));
			}
			else if (tag.equals(contentSequence)) {
				slf4jlogger.debug("Not checking contentSequence {}",tag.toString(dictionary));
			}
			else {
				String needsDeidentification = confidentialityProfileDataElementTags.contains(tag) ? " [DEID]" : "";
				slf4jlogger.debug("{} confidentialityProfileDataElementTags {}",tag.toString(dictionary),needsDeidentification);
				byte[] vr = dictionary.getValueRepresentationFromTag(tag);
				String vrAsString = (vr == null ? "null" : ValueRepresentation.getAsString(vr));
				if (vr == null
					|| ValueRepresentation.isUnknownVR(vr)
					|| ValueRepresentation.isOtherByteVR(vr)
					|| ValueRepresentation.isOtherDoubleVR(vr)
					|| ValueRepresentation.isOtherFloatVR(vr)
					|| ValueRepresentation.isOtherLongVR(vr)
					|| ValueRepresentation.isOtherWordVR(vr)
					|| ValueRepresentation.isOtherUnspecifiedVR(vr)
				  ) {
					Attribute a1 = list1.get(tag);
					Attribute a2 = list2.get(tag);
					if (a2 == null || a2.getVL() == 0) {
						if (a1 == null || a1.getVL() == 0) {
							slf4jlogger.debug("In \"{}\": Was not present or was empty before {}{} VR {}",fileName2,tag.toString(dictionary),needsDeidentification,vrAsString);
						}
						else {
							slf4jlogger.debug("In \"{}\": Removed or is empty {}{} VR {}",fileName2,tag.toString(dictionary),needsDeidentification,vrAsString);
						}
					}
					else {
						slf4jlogger.error("In \"{}\": Not removed or made zero length {}{} VR {}",fileName2,tag.toString(dictionary),needsDeidentification,vrAsString);
					}
				}
				else if (ValueRepresentation.isSequenceVR(vr)) {
					Attribute a1 = list1.get(tag);
					Attribute a2 = list2.get(tag);
					if (a2 == null || ((SequenceAttribute)a2).getNumberOfItems() == 0) {
						if (a1 == null || ((SequenceAttribute)a1).getNumberOfItems() == 0) {
							slf4jlogger.debug("In \"{}\": Was not present or was empty before {}{} VR SQ",fileName2,tag.toString(dictionary),needsDeidentification);
						}
						else {
							slf4jlogger.debug("In \"{}\": Removed or is empty {}{} VR SQ",fileName2,tag.toString(dictionary),needsDeidentification);
						}
					}
					else {
						slf4jlogger.error("In \"{}\": Not removed or made zero length {}{} VR SQ",fileName2,tag.toString(dictionary),needsDeidentification);
					}
				}
				else {
					String valueInList1 = Attribute.getDelimitedStringValuesOrEmptyString(list1,tag);
					String valueInList2 = Attribute.getDelimitedStringValuesOrEmptyString(list2,tag);
					if (!valueInList2.equals(valueInList1)) {
						slf4jlogger.debug("Comparing \"{}\" with \"{}\": different {}{} VR {} \"{}\" -> \"{}\"",fileName1,fileName2,tag.toString(dictionary),needsDeidentification,vrAsString,valueInList1,valueInList2);
						SortedSet<StringPair> changedValues = changedValuesForTag.get(tag);
						if (changedValues == null) {
							changedValues = new TreeSet<StringPair>();
							changedValuesForTag.put(tag,changedValues);
						}
						changedValues.add(new StringPair(valueInList1,valueInList2));
					}
					else if (valueInList1.length() == 0) {
						slf4jlogger.debug("Comparing \"{}\" with \"{}\": empty in both \"{}\"",fileName1,fileName2,tag.toString(dictionary));
					}
					else if (ValueRepresentation.isPersonNameVR(vr) && valueInList1.replaceAll("(\\^|=)","").length() == 0) {
						slf4jlogger.debug("Comparing \"{}\" with \"{}\": same {}{} VR PN after accounting for component delimiters \"{}\" -> \"{}\"",fileName1,fileName2,tag.toString(dictionary),needsDeidentification,valueInList1,valueInList2);
					}
					else {
						slf4jlogger.error("Comparing \"{}\" with \"{}\": not different {}{} VR {} \"{}\" ",fileName1,fileName2,tag.toString(dictionary),needsDeidentification,vrAsString,valueInList1);
						SortedSet<String> unchangedValues = unchangedValuesForTag.get(tag);
						if (unchangedValues == null) {
							unchangedValues = new TreeSet<String>();
							unchangedValuesForTag.put(tag,unchangedValues);
						}
						unchangedValues.add(valueInList1);
					}
				}
			}
		}
	}

	private void checkMatchingFilesAreDeidentifiedInSecondSet(
			SetOfDicomFiles inFirstSet,
			SetOfDicomFiles inSecondSet,
			Map<AttributeTag,SortedSet<StringPair>> changedValuesForTag,
			Map<AttributeTag,SortedSet<String>> unchangedValuesForTag) {
		// all in first set have same finger print as those in second set - usually one in each but could be more than one, hence pairwise comaprison of all ...
		Iterator<SetOfDicomFiles.DicomFile> it1 = inFirstSet.iterator();
		while (it1.hasNext()) {
			SetOfDicomFiles.DicomFile df1 = it1.next();
			Iterator<SetOfDicomFiles.DicomFile> it2 = inSecondSet.iterator();
			while (it2.hasNext()) {
				SetOfDicomFiles.DicomFile df2 = it2.next();
				checkIsDeidentifiedproperly(df1,df2,changedValuesForTag,unchangedValuesForTag);
			}
		}
	}

	private void compareSets(SetOfDicomFiles firstSetOfDicomFiles,SetOfDicomFiles secondSetOfDicomFiles) {
		Map<String,FingerPrint> firstSetFileNameToFingerPrints = new HashMap<String,FingerPrint>();
		Map<String,SetOfDicomFiles> firstSetFingerPrintDigestStringToSetOfDicomFiles = new HashMap<String,SetOfDicomFiles>();
		Set<String> firstSetFailedFileNames = new HashSet<String>();
		buildFingerPrints(firstSetOfDicomFiles,firstSetFileNameToFingerPrints,firstSetFingerPrintDigestStringToSetOfDicomFiles,firstSetFailedFileNames);
		
		Map<String,FingerPrint> secondSetFileNameToFingerPrints = new HashMap<String,FingerPrint>();
		Map<String,SetOfDicomFiles> secondSetFingerPrintDigestStringToSetOfDicomFiles = new HashMap<String,SetOfDicomFiles>();
		Set<String> secondSetFailedFileNames = new HashSet<String>();
		buildFingerPrints(secondSetOfDicomFiles,secondSetFileNameToFingerPrints,secondSetFingerPrintDigestStringToSetOfDicomFiles,secondSetFailedFileNames);
		
		Set<String>  setOfFirstSetStudyInstanceUIDsWithUnmatchedFiles = new HashSet<String>();
		Set<String> setOfSecondSetStudyInstanceUIDsWithUnmatchedFiles = new HashSet<String>();
		
		Map<String,Set<String>> mapOfFirstSetStudyInstanceUIDToSecondSetStudyInstanceUIDs = new TreeMap<String,Set<String>>();
		
		Set<CompositeInstanceContext>  setOfFirstSetStudyCICsWithUnmatchedFiles = new HashSet<CompositeInstanceContext>();
		Set<CompositeInstanceContext> setOfSecondSetStudyCICsWithUnmatchedFiles = new HashSet<CompositeInstanceContext>();

		Map<CompositeInstanceContext,Set<CompositeInstanceContext>> mapOfFirstSetStudyCICToSecondSetStudyCICs = new HashMap<CompositeInstanceContext,Set<CompositeInstanceContext>>();
		
		Set<String>  setOfFirstSetSeriesInstanceUIDsWithUnmatchedFiles = new HashSet<String>();
		Set<String> setOfSecondSetSeriesInstanceUIDsWithUnmatchedFiles = new HashSet<String>();
		
		Map<String,Set<String>> mapOfFirstSetSeriesInstanceUIDToSecondSetSeriesInstanceUIDs = new TreeMap<String,Set<String>>();
		
		Set<CompositeInstanceContext>  setOfFirstSetSeriesCICsWithUnmatchedFiles = new HashSet<CompositeInstanceContext>();
		Set<CompositeInstanceContext> setOfSecondSetSeriesCICsWithUnmatchedFiles = new HashSet<CompositeInstanceContext>();

		Map<CompositeInstanceContext,Set<CompositeInstanceContext>> mapOfFirstSetSeriesCICToSecondSetSeriesCICs = new HashMap<CompositeInstanceContext,Set<CompositeInstanceContext>>();

		Map<AttributeTag,SortedSet<StringPair>> changedValuesForTag = new TreeMap<AttributeTag,SortedSet<StringPair>>();
		Map<AttributeTag,SortedSet<String>> unchangedValuesForTag = new TreeMap<AttributeTag,SortedSet<String>>();

		// for each file in set 1, see if there is a fingerprint match in set 2
		for (String probe : firstSetFingerPrintDigestStringToSetOfDicomFiles.keySet()) {
			slf4jlogger.debug("Looking for {}",probe);
			SetOfDicomFiles  inFirstSet =  firstSetFingerPrintDigestStringToSetOfDicomFiles.get(probe);
			SetOfDicomFiles inSecondSet = secondSetFingerPrintDigestStringToSetOfDicomFiles.get(probe);
			if (inSecondSet == null) {
				slf4jlogger.info("For digest {}:\n\t Only in 1st set contains {}",probe,inFirstSet.toString());
				extractCompositeInstanceContextForUnmatchedFiles(
					setOfFirstSetStudyInstanceUIDsWithUnmatchedFiles,
					setOfFirstSetStudyCICsWithUnmatchedFiles,
					setOfFirstSetSeriesInstanceUIDsWithUnmatchedFiles,
					setOfFirstSetSeriesCICsWithUnmatchedFiles,
					inFirstSet);
			}
			else {
				slf4jlogger.info("For digest {}:\n\t 1st set contains {}\n\t 2nd set contains {}",probe,inFirstSet.toString(),inSecondSet.toString());
				extractCompositeInstanceContextForMatchingFiles(
					mapOfFirstSetStudyInstanceUIDToSecondSetStudyInstanceUIDs,
					mapOfFirstSetStudyCICToSecondSetStudyCICs,
					mapOfFirstSetSeriesInstanceUIDToSecondSetSeriesInstanceUIDs,
					mapOfFirstSetSeriesCICToSecondSetSeriesCICs,
					inFirstSet,inSecondSet);
				
				checkMatchingFilesAreDeidentifiedInSecondSet(
					inFirstSet,
					inSecondSet,
					changedValuesForTag,
					unchangedValuesForTag);
			}
		}

		// for each file in set 2, list those that are not in set 1 (those in both will already have been listed)
		for (String probe : secondSetFingerPrintDigestStringToSetOfDicomFiles.keySet()) {
			slf4jlogger.debug("Looking for {}",probe);
			SetOfDicomFiles inFirstSet = firstSetFingerPrintDigestStringToSetOfDicomFiles.get(probe);
			if (inFirstSet == null) {
				SetOfDicomFiles inSecondSet = secondSetFingerPrintDigestStringToSetOfDicomFiles.get(probe);
				slf4jlogger.info("For digest {}:\n\t Only in 2nd set contains {}",probe,inSecondSet.toString());
				extractCompositeInstanceContextForUnmatchedFiles(
					setOfSecondSetStudyInstanceUIDsWithUnmatchedFiles,
					setOfSecondSetStudyCICsWithUnmatchedFiles,
					setOfSecondSetSeriesInstanceUIDsWithUnmatchedFiles,
					setOfSecondSetSeriesCICsWithUnmatchedFiles,
					inSecondSet);
			}
			// else already listed in earlier pass
		}

		{
			slf4jlogger.debug("First set failed fingerprinting:");
			for (String f : firstSetFailedFileNames) {
				slf4jlogger.info("\tFailed to fingerprint first set {}",f);
			}
			slf4jlogger.debug("Second set failed fingerprinting:");
			for (String f : secondSetFailedFileNames) {
				slf4jlogger.info("\tFailed to fingerprint second set {}",f);
			}
		}
		
		{
			for (CompositeInstanceContext firstSetStudyCIC : mapOfFirstSetStudyCICToSecondSetStudyCICs.keySet()) {
				slf4jlogger.info("StudyCIC first set: {}",summarizeStudy(firstSetStudyCIC));
				slf4jlogger.debug("StudyCIC first set:\n{}",firstSetStudyCIC);
				Set<CompositeInstanceContext> secondSetStudyCICs = mapOfFirstSetStudyCICToSecondSetStudyCICs.get(firstSetStudyCIC);
				for (CompositeInstanceContext secondSetStudyCIC : secondSetStudyCICs) {
					slf4jlogger.info("Maps to StudyCIC second set: {}",summarizeStudy(secondSetStudyCIC));
					slf4jlogger.debug("Maps to StudyCIC second set:\n{}",secondSetStudyCIC);
				}
			}
		}

		{
			for (CompositeInstanceContext firstSetStudyCIC : setOfFirstSetStudyCICsWithUnmatchedFiles) {
				slf4jlogger.info("StudyCIC first set has one or more unmatched files: {}",summarizeStudy(firstSetStudyCIC));
				slf4jlogger.debug("StudyCIC first set has one or more unmatched files:\n{}",firstSetStudyCIC);
			}
		}

		{
			for (CompositeInstanceContext secondSetStudyCIC : setOfSecondSetStudyCICsWithUnmatchedFiles) {
				slf4jlogger.info("StudyCIC second set has one or more unmatched files: {}",summarizeStudy(secondSetStudyCIC));
				slf4jlogger.debug("StudyCIC second set has one or more unmatched files:\n{}",secondSetStudyCIC);
			}
		}

		{
			for (CompositeInstanceContext firstSetSeriesCIC : mapOfFirstSetSeriesCICToSecondSetSeriesCICs.keySet()) {
				slf4jlogger.info("SeriesCIC first set: {}",summarizeSeries(firstSetSeriesCIC));
				slf4jlogger.debug("SeriesCIC first set:\n{}",firstSetSeriesCIC);
				Set<CompositeInstanceContext> secondSetSeriesCICs = mapOfFirstSetSeriesCICToSecondSetSeriesCICs.get(firstSetSeriesCIC);
				for (CompositeInstanceContext secondSetSeriesCIC : secondSetSeriesCICs) {
					slf4jlogger.info("Maps to SeriesCIC second set: {}",summarizeSeries(secondSetSeriesCIC));
					slf4jlogger.debug("Maps to SeriesCIC second set:\n{}",secondSetSeriesCIC);
				}
			}
		}

		{
			for (CompositeInstanceContext firstSetSeriesCIC : setOfFirstSetSeriesCICsWithUnmatchedFiles) {
				slf4jlogger.info("SeriesCIC first set has one or more unmatched files: {}",summarizeSeries(firstSetSeriesCIC));
				slf4jlogger.debug("SeriesCIC first set has one or more unmatched files:\n{}",firstSetSeriesCIC);
			}
		}

		{
			for (CompositeInstanceContext secondSetSeriesCIC : setOfSecondSetSeriesCICsWithUnmatchedFiles) {
				slf4jlogger.info("SeriesCIC second set has one or more unmatched files: {}",summarizeSeries(secondSetSeriesCIC));
				slf4jlogger.debug("SeriesCIC second set has one or more unmatched files:\n{}",secondSetSeriesCIC);
			}
		}

		{
			for (String firstSetStudyInstanceUID : mapOfFirstSetStudyInstanceUIDToSecondSetStudyInstanceUIDs.keySet()) {
				Set<String> secondSetStudyInstanceUIDs = mapOfFirstSetStudyInstanceUIDToSecondSetStudyInstanceUIDs.get(firstSetStudyInstanceUID);
				if (secondSetStudyInstanceUIDs == null) {
					slf4jlogger.info("StudyInstanceUID first set {} has no match",firstSetStudyInstanceUID);
				}
				else {
					for (String secondSetStudyInstanceUID : secondSetStudyInstanceUIDs) {
						slf4jlogger.info("StudyInstanceUID first set {} -> second set {}",firstSetStudyInstanceUID,secondSetStudyInstanceUID);
					}
				}
			}
		}

		{
			for (String firstSetStudyInstanceUID : setOfFirstSetStudyInstanceUIDsWithUnmatchedFiles) {
				slf4jlogger.info("StudyInstanceUID first set {} has one or more unmatched files",firstSetStudyInstanceUID);
			}
		}

		{
			for (String secondSetStudyInstanceUID : setOfSecondSetStudyInstanceUIDsWithUnmatchedFiles) {
				slf4jlogger.info("StudyInstanceUID second set {} has one or more unmatched files",secondSetStudyInstanceUID);
			}
		}

		{
			for (String firstSetSeriesInstanceUID : mapOfFirstSetSeriesInstanceUIDToSecondSetSeriesInstanceUIDs.keySet()) {
				Set<String> secondSetSeriesInstanceUIDs = mapOfFirstSetSeriesInstanceUIDToSecondSetSeriesInstanceUIDs.get(firstSetSeriesInstanceUID);
				if (secondSetSeriesInstanceUIDs == null) {
					slf4jlogger.info("SeriesInstanceUID first set {} has no match",firstSetSeriesInstanceUID);
				}
				else {
					for (String secondSetSeriesInstanceUID : secondSetSeriesInstanceUIDs) {
						slf4jlogger.info("SeriesInstanceUID first set {} -> second set {}",firstSetSeriesInstanceUID,secondSetSeriesInstanceUID);
					}
				}
			}
		}

		{
			for (String firstSetSeriesInstanceUID : setOfFirstSetSeriesInstanceUIDsWithUnmatchedFiles) {
				slf4jlogger.info("SeriesInstanceUID first set {} has one or more unmatched files",firstSetSeriesInstanceUID);
			}
		}

		{
			for (String secondSetSeriesInstanceUID : setOfSecondSetSeriesInstanceUIDsWithUnmatchedFiles) {
				slf4jlogger.info("SeriesInstanceUID second set {} has one or more unmatched files",secondSetSeriesInstanceUID);
			}
		}

		
		{
			for (AttributeTag tag : changedValuesForTag.keySet()) {
				SortedSet<StringPair> changedValues = changedValuesForTag.get(tag);
				String needsDeidentification = confidentialityProfileDataElementTags.contains(tag) ? " [DEID]" : "";
				slf4jlogger.warn("{}{} values that were different:",tag.toString(dictionary),needsDeidentification);
				for (StringPair changedValue : changedValues) {
					slf4jlogger.warn("\t\"{}\" -> \"{}\"",changedValue.string1,changedValue.string2);
				}
			}
		}

		{
			for (AttributeTag tag : unchangedValuesForTag.keySet()) {
				SortedSet<String> unchangedValues = unchangedValuesForTag.get(tag);
				String needsDeidentification = confidentialityProfileDataElementTags.contains(tag) ? " [DEID]" : "";
				slf4jlogger.warn("{}{} values that were not different:",tag.toString(dictionary),needsDeidentification);
				for (String unchangedValue : unchangedValues) {
					slf4jlogger.warn("\t\"{}\"",unchangedValue);
				}
			}
		}
	}
	public CompareSetsOfDicomFiles(String firstPath,String secondPath) {
		SetOfDicomFiles firstSetOfDicomFiles = buildSetOfDicomFiles(firstPath);
		if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("1st set:\n{}",firstSetOfDicomFiles.toString());
		
		SetOfDicomFiles secondSetOfDicomFiles = buildSetOfDicomFiles(secondPath);
		if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("2nd set:\n{}",secondSetOfDicomFiles.toString());
		
		compareSets(firstSetOfDicomFiles,secondSetOfDicomFiles);
	}

	/**
	 * <p>For testing, read all DICOM files and build a set of them.</p>
	 *
	 * @param	arg	the filenames
	 */
	public static void main(String arg[]) {
		new CompareSetsOfDicomFiles(arg[0],arg[1]);
	}
}

