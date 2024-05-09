/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.ClinicalTrialsAttributes;
import com.pixelmed.dicom.CodedSequenceItem;
import com.pixelmed.dicom.CodeStringAttribute;
import com.pixelmed.dicom.CompressedFrameDecoder;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.LongStringAttribute;
import com.pixelmed.dicom.MediaImporter;
import com.pixelmed.dicom.MoveDicomFilesIntoHierarchy;
import com.pixelmed.dicom.PersonNameAttribute;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.dicom.VersionAndConstants;

import com.pixelmed.utils.MessageLogger;
import com.pixelmed.utils.PrintStreamMessageLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class to implement bulk de-identification and pseudonymization of DICOM files with sequesteration of files that may have risk of identity leakage.</p>
 *
 * @author	dclunie
 */
public class PseudonymizeAndSequester {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/PseudonymizeAndSequester.java,v 1.15 2022/01/21 19:51:12 dclunie Exp $";
	
	private static final Logger slf4jlogger = LoggerFactory.getLogger(PseudonymizeAndSequester.class);
	
	private static final DicomDictionary dictionary = DicomDictionary.StandardDictionary;

	protected static String ourCalledAETitle = "OURAETITLE";
	
	//protected static int radixForRandomPseudonymousID = 10;
	protected static int radixForRandomPseudonymousID = Character.MAX_RADIX;
	
	//protected static Date epochForDateModification = new Date(0l);	// January 1, 1970, 00:00:00 GMT
	protected static Date epochForDateModification = new GregorianCalendar(2001,0,1).getTime();

	//protected static Date defaultEarliestDateInSet = new GregorianCalendar(2001,0,1).getTime();
	protected static Date defaultEarliestDateInSet = epochForDateModification;	// used only if can't get a Date from the AttributeList of the first object encountered for each patient
	
	// began by copying pattern used in DeidentifyAndRedact()

	/**
	 * <p>Make a suitable file name to use for a deidentified and redacted input file.</p>
	 *
	 * <p>The default is the UID plus "_Anon.dcm" in the outputFolderName (ignoring the inputFileName).</p>
	 *
	 * <p>Override this method in a subclass if a different file name is required.</p>
	 *
	 * @param		outputFolderName	where to store all the processed output files
	 * @param		inputFileName		the path to search for DICOM files
	 * @param		sopInstanceUID		the SOP Instance UID of the output file
	 * @exception	IOException			if a filename cannot be constructed
	 */
	protected static String makeOutputFileName(String outputFolderName,String inputFileName,String sopInstanceUID) throws IOException {
		// ignore inputFileName
		return new File(outputFolderName,(sopInstanceUID == null || sopInstanceUID.length() == 0 ? "NOSOPINSTANCEUID" : sopInstanceUID)+"_Anon.dcm").getCanonicalPath();
	}
	
	protected Map<String,String> newPatientIDByOriginalPatientID = new HashMap<String,String>();
	protected Map<String,String> newPatientIDByOriginalStudyInstanceUID = new HashMap<String,String>();
	protected Map<String,String> newPatientNameByNewPatientID = new HashMap<String,String>();
	protected Map<String,Date> earliestDateByOrignalPatientID = new HashMap<String,Date>();
	
	/**
	 * <p>Read a file mapping original PatientID or StudyInstanceUID to new PatientID and PatientName and add them to the maps.</p>
	 *
	 * Type of file is detected based on header line of the form:
	 *
	 *   originalPatientID	newPatientID	newPatientName
	 *
	 * or
	 *
	 *   originalStudyInstanceUID	newPatientID	newPatientName
	 *
	 * @param		pseudonymizationControlFileName	the control file, if any
	 */
	protected void readPseudonymizationControlFile(String pseudonymizationControlFileName) throws IOException {
		boolean useOriginalPatientID = false;
		boolean useOriginalStudyInstanceUID = false;
		if (pseudonymizationControlFileName != null && pseudonymizationControlFileName.length() > 0) {
			BufferedReader r =  new BufferedReader(new FileReader(pseudonymizationControlFileName));
			String line = null;
			while ((line=r.readLine()) != null) {
				// originalPatientID newPatientID newPatientName
				Pattern p = Pattern.compile("(.*)\t(.*)\t(.*)");
				Matcher m = p.matcher(line);
				if (m.matches()) {
					slf4jlogger.trace("readPseudonymizationControlFile(): have match");
					int groupCount = m.groupCount();
					if (groupCount == 3) {
						slf4jlogger.trace("readPseudonymizationControlFile(): have correct groupCount");
						// originalPatientID	newPatientID	newPatientName
						if (m.group(1).equals("originalPatientID")) {
							useOriginalPatientID = true;
						}
						else if (m.group(1).equals("originalStudyInstanceUID")) {
							useOriginalStudyInstanceUID = true;
						}
						else if (useOriginalPatientID) {
							String originalPatientID = m.group(1);
							String newPatientID = m.group(2);
							String newPatientName = m.group(3);
							slf4jlogger.debug("readPseudonymizationControlFile(): found values {},{},{}",originalPatientID,newPatientID,newPatientName);
							if (originalPatientID.length() > 0
								&& newPatientID.length() > 0
								&& newPatientName.length() > 0) {
								newPatientIDByOriginalPatientID.put(originalPatientID,newPatientID);
								newPatientNameByNewPatientID.put(newPatientID,newPatientName);
							}
							else {
								slf4jlogger.warn("readPseudonymizationControlFile(): not adding - empty components in line {}",line);
							}
						}
						else if (useOriginalStudyInstanceUID) {
							String originalStudyInstanceUID = m.group(1);
							String newPatientID = m.group(2);
							String newPatientName = m.group(3);
							slf4jlogger.debug("readPseudonymizationControlFile(): found values {},{},{}",originalStudyInstanceUID,newPatientID,newPatientName);
							if (originalStudyInstanceUID.length() > 0
								&& newPatientID.length() > 0
								&& newPatientName.length() > 0) {
								newPatientIDByOriginalStudyInstanceUID.put(originalStudyInstanceUID,newPatientID);
								newPatientNameByNewPatientID.put(newPatientID,newPatientName);
							}
							else {
								slf4jlogger.warn("readPseudonymizationControlFile(): not adding - empty components in line {}",line);
							}
						}
						else {
							slf4jlogger.error("readPseudonymizationControlFile(): Unrecognized pattern in control file in line {}",line);
						}
					}
					else {
						slf4jlogger.warn("readPseudonymizationControlFile(): bad groupCount in line {}",line);
					}
				}
				else {
						slf4jlogger.warn("readPseudonymizationControlFile(): no pattern match for line {}",line);
				}
			}
			r.close();
		}
	}
	
	protected Random random;

	/**
	 * <p>Create a new PatientID and PatientName and them to the maps.</p>
	 *
	 * @param		originalPatientID			the old PatientID
	 * @param		originalStudyInstanceUID	the old StudyInstanceUID
	 * @return									the new PatientID
	 */
	protected String createNewPseudonymousPatientAndAddToMaps(String originalPatientID,String originalStudyInstanceUID) {
		slf4jlogger.debug("createNewPseudonymousPatientAndAddToMaps for {}",originalPatientID);
		String newPatientID = Integer.toString(random.nextInt(Integer.MAX_VALUE),radixForRandomPseudonymousID).toUpperCase();
		String newPatientName = "Nobody^" + newPatientID;
		
		newPatientIDByOriginalPatientID.put(originalPatientID,newPatientID);
		newPatientIDByOriginalStudyInstanceUID.put(originalStudyInstanceUID,newPatientID);
		newPatientNameByNewPatientID.put(newPatientID,newPatientName);
		
		return newPatientID;
	}
	
	protected void writePseudonymizationResultByOriginalPatientID(PrintWriter w) {
		if (!newPatientIDByOriginalPatientID.isEmpty()) {
			// originalPatientID	newPatientID	newPatientName
			w.print("originalPatientID");
			w.print("\t");
			w.print("newPatientID");
			w.print("\t");
			w.println("newPatientName");
			for (String originalPatientID : newPatientIDByOriginalPatientID.keySet()) {
				String newPatientID = newPatientIDByOriginalPatientID.get(originalPatientID);
				String newPatientName = newPatientNameByNewPatientID.get(newPatientID);
				w.print(originalPatientID);
				w.print("\t");
				w.print(newPatientID);
				w.print("\t");
				w.println(newPatientName);
			}
		}
	}
	
	protected void writePseudonymizationResultByOriginalStudyInstanceUID(PrintWriter w) {
		if (!newPatientIDByOriginalStudyInstanceUID.isEmpty()) {
			// originalStudyInstanceUID	newPatientID	newPatientName
			w.print("originalStudyInstanceUID");
			w.print("\t");
			w.print("newPatientID");
			w.print("\t");
			w.println("newPatientName");
			for (String originalStudyInstanceUID : newPatientIDByOriginalStudyInstanceUID.keySet()) {
				String newPatientID = newPatientIDByOriginalStudyInstanceUID.get(originalStudyInstanceUID);
				String newPatientName = newPatientNameByNewPatientID.get(newPatientID);
				w.print(originalStudyInstanceUID);
				w.print("\t");
				w.print(newPatientID);
				w.print("\t");
				w.println(newPatientName);
			}
		}
	}

	protected void writeUIDMapResult(PrintWriter uidMapResultWriter) {
		// originalUIDValue	replacementUIDValue
		uidMapResultWriter.print("originalUIDValue");
		uidMapResultWriter.print("\t");
		uidMapResultWriter.println("replacementUIDValue");
		Map<String,String> mapOfOriginalToReplacementUIDs = ClinicalTrialsAttributes.getMapOfOriginalToReplacementUIDs();	// is static in mapOfOriginalToReplacementUIDs
		for (String originalUIDValue : mapOfOriginalToReplacementUIDs.keySet()) {
			String replacementUIDValue = mapOfOriginalToReplacementUIDs.get(originalUIDValue);
			uidMapResultWriter.print(originalUIDValue);
			uidMapResultWriter.print("\t");
			uidMapResultWriter.println(replacementUIDValue);
		}
	}
	
	protected static boolean containsOverlay(AttributeList list) {
		boolean foundOverlay = false;
		// only check top level dataset
		Iterator<Attribute> i = list.values().iterator();
		while (i.hasNext()) {
			Attribute a = i.next();
			if (a.getTag().isOverlayGroup()) {
				foundOverlay = true;
				break;
			}
		}
		return foundOverlay;
	}
	
	protected static boolean isDirty(AttributeList list) {
		boolean dirty = false;
		String burnedInAnnotation = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.BurnedInAnnotation);
		if (burnedInAnnotation.equals("YES")) {
			dirty = true;
		}
		else {
			String imageTypeAllValues = Attribute.getDelimitedStringValuesOrEmptyString(list,TagFromName.ImageType).toUpperCase();
			if (imageTypeAllValues.contains("DERIVED")
				|| (imageTypeAllValues.contains("SECONDARY") && !imageTypeAllValues.equals("ORIGINAL\\SECONDARY\\AXIAL"))
				|| imageTypeAllValues.contains("SCREEN")
				) {
				dirty = true;
			}
			else {
				String sopClassUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SOPClassUID);
				if (SOPClass.isSecondaryCaptureImageStorage(sopClassUID)
					|| SOPClass.isNonImageStorage(sopClassUID)
					|| SOPClass.isPrivateImageStorage(sopClassUID)
					|| sopClassUID.equals(SOPClass.UltrasoundImageStorage)
					|| sopClassUID.equals(SOPClass.UltrasoundImageStorageRetired)
					|| sopClassUID.equals(SOPClass.XRayAngiographicImageStorage)
					|| sopClassUID.equals(SOPClass.XRayAngiographicBiplaneImageStorage)
					|| sopClassUID.equals(SOPClass.EnhancedXAImageStorage)
					|| sopClassUID.equals(SOPClass.XRay3DAngiographicImageStorage)
					|| sopClassUID.equals(SOPClass.XRayRadioFlouroscopicImageStorage)
					|| sopClassUID.equals(SOPClass.EnhancedXRFImageStorage)
					) {
					dirty = true;
				}
				else {
					String modality = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.Modality).toUpperCase();
					if (modality.equals("SR")
						|| modality.equals("PR")
						|| modality.equals("OT")
						|| modality.equals("SC")
						|| modality.equals("US")
						|| modality.equals("XA")
						|| modality.equals("XRF")
						) {
						dirty = true;
					}
					else {
						// check for MG scanned film
						String detectorType = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("DetectorType")).toUpperCase();
						if (detectorType.equals("FILM")) {
							dirty = true;
						}
						else{
							if (containsOverlay(list)
								|| list.get(TagFromName.GraphicAnnotationSequence) != null
								) {
								dirty = true;
							}
						}
					}
				}
			}
		}
		return dirty;
	}
	
	/**
	 * <p>A protected class that actually does all the work of finding and processing the files.</p>
	 *
	 */
	protected class OurMediaImporter extends MediaImporter {
		
		String outputFolderCleanName;
		String outputFolderDirtyName;
		PrintWriter pseudonymizationResultByOriginalPatientIDWriter;
		PrintWriter pseudonymizationResultByOriginalStudyInstanceUIDWriter;
		PrintWriter failedFilesWriter;
		PrintWriter uidMapResultWriter;
		boolean keepAllPrivate;
		boolean addContributingEquipmentSequence;
		boolean keepDescriptors;
		boolean keepSeriesDescriptors;
		boolean keepProtocolName;
		boolean keepPatientCharacteristics;
		boolean keepDeviceIdentity;
		boolean keepInstitutionIdentity;
		int handleDates;
		int handleStructuredContent;

		/**
		 * <p>De-identify the DICOM Attributes and pseudonymize them and sequester any files that may have risk of identity leakage.</p>
		 *
		 * @param	logger
		 * @param	outputFolderCleanName				where to store all the low risk processed output files (must already exist)
		 * @param	outputFolderDirtyName				where to store all the high risk processed output files (must already exist)
		 * @param	pseudonymizationControlFileName		values to use for pseudonymization, may be null or empty in which case random values are used
		 * @param	pseudonymizationResulttByOriginalPatientIDFileName				file into which to store pseudonymization by original PatientID performed
		 * @param	pseudonymizationResultByOriginalStudyInstanceUIDFileName		file into which to store pseudonymization by original StudyInstanceUID performed
		 * @param	failedFilesFileName					file into which to store the paths of files that failed to process
		 * @param	uidMapResultFileName				file into which to store the map of original to new UIDs
		 * @param	keepAllPrivate						retain all private attributes, not just known safe ones
		 * @param	addContributingEquipmentSequence	whether or not to add ContributingEquipmentSequence
		 * @param	keepDescriptors						if true, keep the text description and comment attributes
		 * @param	keepSeriesDescriptors				if true, keep the series description even if all other descriptors are removed
		 * @param	keepProtocolName					if true, keep protocol name even if all other descriptors are removed
		 * @param	keepPatientCharacteristics			if true, keep patient characteristics (such as might be needed for PET SUV calculations)
		 * @param	keepDeviceIdentity					if true, keep device identity
		 * @param	keepInstitutionIdentity				if true, keep institution identity
		 * @param	handleDates							keep, remove or modify dates and times
		 * @param	handleStructuredContent				keep, remove or modify structured content
		 */
		public OurMediaImporter(MessageLogger logger,String inputPathName,String outputFolderCleanName,String outputFolderDirtyName,String pseudonymizationControlFileName,
								String pseudonymizationResulttByOriginalPatientIDFileName,
								String pseudonymizationResultByOriginalStudyInstanceUIDFileName,
								String failedFilesFileName,String uidMapResultFileName,
								boolean keepAllPrivate,boolean addContributingEquipmentSequence,
								boolean keepDescriptors,boolean keepSeriesDescriptors,boolean keepProtocolName,boolean keepPatientCharacteristics,boolean keepDeviceIdentity,boolean keepInstitutionIdentity,
								int handleDates,int handleStructuredContent
								) throws FileNotFoundException, IOException {
			super(logger);
			this.outputFolderCleanName = outputFolderCleanName;
			this.outputFolderDirtyName = outputFolderDirtyName;
			this.pseudonymizationResultByOriginalPatientIDWriter = new PrintWriter(new FileWriter(pseudonymizationResulttByOriginalPatientIDFileName));
			this.pseudonymizationResultByOriginalStudyInstanceUIDWriter = new PrintWriter(new FileWriter(pseudonymizationResultByOriginalStudyInstanceUIDFileName));
			this.failedFilesWriter = new PrintWriter(new FileWriter(failedFilesFileName));
			this.uidMapResultWriter = new PrintWriter(new FileWriter(uidMapResultFileName));
			this.keepAllPrivate = keepAllPrivate;
			this.addContributingEquipmentSequence = addContributingEquipmentSequence;
			this.keepDescriptors = keepDescriptors;
			this.keepSeriesDescriptors = keepSeriesDescriptors;
			this.keepProtocolName = keepProtocolName;
			this.keepPatientCharacteristics = keepPatientCharacteristics;
			this.keepDeviceIdentity = keepDeviceIdentity;
			this.keepInstitutionIdentity = keepInstitutionIdentity;
			this.handleDates = handleDates;
			this.handleStructuredContent = handleStructuredContent;
			
			readPseudonymizationControlFile(pseudonymizationControlFileName);
		}
		
		// not in parent class (yet :()
		protected void finish() throws IOException {
			slf4jlogger.info("OurMediaImporter.finish()");
			if (pseudonymizationResultByOriginalPatientIDWriter != null) {
				writePseudonymizationResultByOriginalPatientID(pseudonymizationResultByOriginalPatientIDWriter);
				pseudonymizationResultByOriginalPatientIDWriter.close();
				pseudonymizationResultByOriginalPatientIDWriter = null;
			}
			if (pseudonymizationResultByOriginalStudyInstanceUIDWriter != null) {
				writePseudonymizationResultByOriginalStudyInstanceUID(pseudonymizationResultByOriginalStudyInstanceUIDWriter);
				pseudonymizationResultByOriginalStudyInstanceUIDWriter.close();
				pseudonymizationResultByOriginalStudyInstanceUIDWriter = null;
			}
			if (failedFilesWriter != null) {
				failedFilesWriter.close();
				failedFilesWriter = null;
			}
			if (uidMapResultWriter != null) {
				writeUIDMapResult(uidMapResultWriter);
				uidMapResultWriter.close();
				uidMapResultWriter = null;
			}
		}
		
		/**
		 * <p>De-identify the DICOM Attributes and pseudonymize them and sequester if may have risk of identity leakage.</p>
		 *
		 * <p>Implements the following options of {@link ClinicalTrialsAttributes#removeOrNullIdentifyingAttributes(AttributeList,int,boolean,boolean,boolean,boolean,boolean,boolean,int,Date,Date) ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes()}:</p>
		 * <p>keepDescriptors, keepSeriesDescriptors, keepProtocolName, keepPatientCharacteristics, keepDeviceIdentity, keepInstitutionIdentity, ClinicalTrialsAttributes.HandleDates.keep</p>
		 *
		 * <p>Also performs {@link AttributeList#removeUnsafePrivateAttributes() AttributeList.removeUnsafePrivateAttributes()}</p>
		 * <p>Also performs {@link ClinicalTrialsAttributes#remapUIDAttributes(AttributeList) ClinicalTrialsAttributes.remapUIDAttributes(AttributeList)}</p>
		 *
		 * <p>The pixel data is not redacted.</p>
		 *
		 * <p>The output file is stored in the outputFolderName specified in the constructor and is named ... </p>
		 *
		 * <p>Any exceptions encountered during processing are logged to stderr, and processing of the next file will continue.</p>
		 *
		 * @param	dicomFileName			the fully qualified path name to a DICOM file
		 * @param	inputTransferSyntaxUID	the Transfer Syntax of the Data Set if a DICOM file, from the DICOMDIR or Meta Information Header
		 * @param	sopClassUID				the SOP Class of the Data Set if a DICOM file, from the DICOMDIR or Meta Information Header
		 */
		protected void doSomethingWithDicomFileOnMedia(String dicomFileName,String inputTransferSyntaxUID,String sopClassUID) {
			//System.err.println("PseudonymizeAndSequester.OurMediaImporter.doSomethingWithDicomFile(): "+dicomFileName);
			slf4jlogger.info("Processing {} Transfer Syntax {}",dicomFileName,inputTransferSyntaxUID);
			try {
				// copied from DeidentifyAndRedact and in turn DicomCleaner.copyFromOriginalToCleanedPerformingAction() and GUI stuff removed ... should refactor :(
				File file = new File(dicomFileName);
				DicomInputStream i = new DicomInputStream(file);
				AttributeList list = new AttributeList();
				list.setDecompressPixelData(false);
				list.read(i);
				i.close();
				
				String outputTransferSyntaxUID = Attribute.getSingleStringValueOrDefault(list,TagFromName.TransferSyntaxUID,TransferSyntax.ExplicitVRLittleEndian);

				String patientID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientID);
				if (patientID.length() == 0) {
					throw new DicomException("Cannot pseudonymize file with empty or missing PatientID: "+dicomFileName);
				}

				String studyInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyInstanceUID);
				if (studyInstanceUID.length() == 0) {
					throw new DicomException("Cannot pseudonymize file with empty or missing StudyInstanceUID: "+dicomFileName);
				}

				// Strategy is to use same date for each patient, but different dates for different patients
				Date earliestDateInSet = earliestDateByOrignalPatientID.get(patientID);
				if (earliestDateInSet == null) {
					earliestDateInSet = ClinicalTrialsAttributes.findEarliestDateTime(list);
					if (earliestDateInSet != null) {
						earliestDateByOrignalPatientID.put(patientID,earliestDateInSet);
					}
				}
				if (earliestDateInSet == null) {
					slf4jlogger.warn("Cannot get earliestDateInSet for {}",dicomFileName);
					earliestDateInSet = defaultEarliestDateInSet;
				}
				
				String outputFolderSelectedName = isDirty(list) ? outputFolderDirtyName : outputFolderCleanName;

				list.removeGroupLengthAttributes();
				//list.correctDecompressedImagePixelModule(true/*deferredDecompression*/);					// make sure to correct even if decompression was deferred
				//list.insertLossyImageCompressionHistoryIfDecompressed(true/*deferredDecompression*/);
				list.removeMetaInformationHeaderAttributes();
				
				ClinicalTrialsAttributes.removeClinicalTrialsAttributes(list);
				ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,
																		   ClinicalTrialsAttributes.HandleUIDs.keep,
																		   keepDescriptors,
																		   keepSeriesDescriptors,
																		   keepProtocolName,
																		   keepPatientCharacteristics,
																		   keepDeviceIdentity,
																		   keepInstitutionIdentity,
																		   handleDates,
																		   epochForDateModification,
																		   earliestDateInSet,
																		   handleStructuredContent
																		   );
				
				
				// OK to put in replacements now ...
				String newPatientID = newPatientIDByOriginalPatientID.get(patientID);				// will be empty if not in control file or seen before
				if (newPatientID == null) {
					newPatientID = newPatientIDByOriginalStudyInstanceUID.get(studyInstanceUID);	// will be empty if not in control file or seen before
					if (newPatientID == null) {
						newPatientID = createNewPseudonymousPatientAndAddToMaps(patientID,studyInstanceUID);
					}
					else {
						slf4jlogger.debug("Found patient match on original StudyInstanceUID");
					}
				}
				else {
					slf4jlogger.debug("Found patient match on original PatientID");
				}
				{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(newPatientID); list.put(a); }
				{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(newPatientNameByNewPatientID.get(newPatientID)); list.put(a); }

				// NB. ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes() will have already
				// added both DeidentificationMethod and DeidentificationMethodCodeSequence
				// so now we can assume their presence without checking
				
				Attribute aDeidentificationMethod = list.get(TagFromName.DeidentificationMethod);
				SequenceAttribute aDeidentificationMethodCodeSequence = (SequenceAttribute)(list.get(TagFromName.DeidentificationMethodCodeSequence));
				
				if (keepAllPrivate) {
					aDeidentificationMethod.addValue("All private retained");
					aDeidentificationMethodCodeSequence.addItem(new CodedSequenceItem("210002","99PMP","Retain all private elements").getAttributeList());
				}
				else {
					list.removeUnsafePrivateAttributes();
					aDeidentificationMethod.addValue("Unsafe private removed");
					aDeidentificationMethodCodeSequence.addItem(new CodedSequenceItem("113111","DCM","Retain Safe Private Option").getAttributeList());
				}
				
				ClinicalTrialsAttributes.remapUIDAttributes(list);
				aDeidentificationMethod.addValue("UIDs remapped");
				
				{
					// remove the default Retain UIDs added by ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes() with the ClinicalTrialsAttributes.HandleUIDs.keep option
					Iterator<SequenceItem> it = aDeidentificationMethodCodeSequence.iterator();
					while (it.hasNext()) {
						SequenceItem item = it.next();
						if (item != null) {
							CodedSequenceItem testcsi = new CodedSequenceItem(item.getAttributeList());
							if (testcsi != null) {
								String cv = testcsi.getCodeValue();
								String csd = testcsi.getCodingSchemeDesignator();
								if (cv != null && cv.equals("113110") && csd != null && csd.equals("DCM")) {	// "Retain UIDs Option"
									it.remove();
								}
							}
						}
					}
				}
				aDeidentificationMethodCodeSequence.addItem(new CodedSequenceItem("210001","99PMP","Remap UIDs").getAttributeList());
				
				if (addContributingEquipmentSequence) {
					ClinicalTrialsAttributes.addContributingEquipmentSequence(list,
																			  true,
																			  new CodedSequenceItem("109104","DCM","De-identifying Equipment"),	// per CP 892
																			  "PixelMed",														// Manufacturer
																			  null,															// Institution Name
																			  null,															// Institutional Department Name
																			  null		,													// Institution Address
																			  ourCalledAETitle,												// Station Name
																			  "PseudonymizeAndSequester.main()",									// Manufacturer's Model Name
																			  null,															// Device Serial Number
																			  VersionAndConstants.getBuildDate(),							// Software Version(s)
																			  "Deidentified and Pseudonymized");
				}
				
				FileMetaInformation.addFileMetaInformation(list,outputTransferSyntaxUID,ourCalledAETitle);
				list.insertSuitableSpecificCharacterSetForAllStringValues();	// E.g., may have de-identified Kanji name and need new character set
				
				File cleanedFile = new File(makeOutputFileName(outputFolderSelectedName,dicomFileName,Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SOPInstanceUID)));
				list.write(cleanedFile,outputTransferSyntaxUID,true/*useMeta*/,true/*useBufferedStream*/);
				slf4jlogger.info("Deidentified {} into {}",dicomFileName,cleanedFile.getCanonicalPath());
				String movedFileName = new MoveDicomFilesIntoHierarchy().renameFileWithHierarchicalPathFromAttributes(cleanedFile,list,outputFolderSelectedName,"DUP"/*duplicatesFolderNamePrefix*/,MoveDicomFilesIntoHierarchy.FolderNamingStrategy.instanceByNumberAnatomyLateralityView);
				slf4jlogger.info("Moved deidentified {} into {}",cleanedFile.getCanonicalPath(),movedFileName);
			}
			catch (Exception e) {
				slf4jlogger.error("Failed to process "+dicomFileName+" ",e);
				failedFilesWriter.println(dicomFileName);
			}
		}

		// override base class isOKToImport(), which rejects unsupported compressed transfer syntaxes
		
		/**
		 * <p>Allows all types of DICOM files, images or not, with any Transfer Syntax to be processed</p>
		 *
		 * @param	sopClassUID
		 * @param	transferSyntaxUID
		 */
		protected boolean isOKToImport(String sopClassUID,String transferSyntaxUID) {
			return sopClassUID != null
			&& (SOPClass.isImageStorage(sopClassUID) || (SOPClass.isNonImageStorage(sopClassUID) && ! SOPClass.isDirectory(sopClassUID)))
			&& transferSyntaxUID != null;
		}
	}

	/**
	 * <p>Read DICOM format image files, de-identify and pseudonymize them and sequester any files that may have risk of identity leakage.</p>
	 *
	 * <p>Searches the specified input path recursively for suitable files.</p>
	 *
	 * <p>The pseudonymizationControlFileName and pseudonymizationResultFileName files are three columns of tab delimited UTF-8 text, the original PatientID, the new PatientID and the new PatientName.</p>
	 *
	 * @param		inputPathName						the path to search for DICOM files
	 * @param		outputFolderCleanName				where to store all the low risk processed output files (must already exist)
	 * @param		outputFolderDirtyName				where to store all the high risk processed output files (must already exist)
	 * @param		pseudonymizationControlFileName		values to use for pseudonymization, may be null or empty in which case random values are used
	 * @param		pseudonymizationResultByOriginalPatientIDFileName				file into which to store pseudonymization by original PatientID performed
	 * @param		pseudonymizationResultByOriginalStudyInstanceUIDFileName		file into which to store pseudonymization by original StudyInstanceUID performed
	 * @param		failedFilesFileName					file into which to store the paths of files that failed to process
	 * @param		uidMapResultFileName				file into which to store the map of original to new UIDs
	 * @param		seed								the initial seed to generate random pseudonymous identifiers, long integer as string or null or zero length if none (for deterministic creation of pseudonyms)
	 * @param		keepAllPrivate						retain all private attributes, not just known safe ones
	 * @param		addContributingEquipmentSequence	whether or not to add ContributingEquipmentSequence
	 * @param		keepDescriptors						if true, keep the text description and comment attributes
	 * @param		keepSeriesDescriptors				if true, keep the series description even if all other descriptors are removed
	 * @param		keepProtocolName					if true, keep protocol name even if all other descriptors are removed
	 * @param		keepPatientCharacteristics			if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @param		keepDeviceIdentity					if true, keep device identity
	 * @param		keepInstitutionIdentity				if true, keep institution identity
	 * @param		handleDates							keep, remove or modify dates and times
	 * @param		handleStructuredContent				keep, remove or modify structured content
	 * @exception	DicomException
	 * @exception	IOException
	 * @exception	FileNotFoundException
	 */
	public PseudonymizeAndSequester(String inputPathName,String outputFolderCleanName,String outputFolderDirtyName,String pseudonymizationControlFileName,
									String pseudonymizationResultByOriginalPatientIDFileName,
									String pseudonymizationResultByOriginalStudyInstanceUIDFileName,
									String failedFilesFileName,String uidMapResultFileName,String seed,
									boolean keepAllPrivate,boolean addContributingEquipmentSequence,
									boolean keepDescriptors,boolean keepSeriesDescriptors,boolean keepProtocolName,boolean keepPatientCharacteristics,boolean keepDeviceIdentity,boolean keepInstitutionIdentity,
									int handleDates,int handleStructuredContent
									) throws DicomException, FileNotFoundException, IOException {
		// not redacting so do not need to disable memory mapping for SourceImage on Windows platform
		MessageLogger logger = new PrintStreamMessageLogger(System.err);
		if (seed != null && seed.length() > 0) {
			random = new Random(Long.parseLong(seed));
		}
		else {
			random = new Random();
		}
		OurMediaImporter importer = new OurMediaImporter(logger,inputPathName,outputFolderCleanName,outputFolderDirtyName,pseudonymizationControlFileName,
														 pseudonymizationResultByOriginalPatientIDFileName,
														 pseudonymizationResultByOriginalStudyInstanceUIDFileName,
														 failedFilesFileName,uidMapResultFileName,
														 keepAllPrivate,addContributingEquipmentSequence,
														 keepDescriptors,keepSeriesDescriptors,keepProtocolName,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity,
														 handleDates,handleStructuredContent);
		importer.importDicomFiles(inputPathName);
		importer.finish();
	}

	/**
	 * <p>Read DICOM format image files, de-identify and pseudonymize them and sequester any files that may have risk of identity leakage.</p>
	 *
	 * Searches the specified input path recursively for suitable files
	 *
	 * The pseudonymizationControlFile and pseudonymizationResultFile are tab delimited with a header row containing either:
	 *
	 *   originalPatientID	newPatientID	newPatientName
	 *
	 * or
	 *
	 *   originalStudyInstanceUID	newPatientID	newPatientName
	 *
	 * @param	arg	seven or eight parameters plus options, the inputPath (file or folder), outputFolderClean, outputFolderDirty, pseudonymizationControlFile, pseudonymizationResultByOriginalPatientIDFile, pseudonymizationResultByOriginalStudyInstanceUIDFile, failedFilesFile, uidMapResultFile, and optionally a random seed for deterministic creation of pseudonyms, then various options controlling de-identification
	 */
	public static void main(String arg[]) {
		try {
			boolean bad = false;
			boolean keepAllPrivate = false;
			boolean addContributingEquipmentSequence = true;
			boolean keepDescriptors = true;
			boolean keepSeriesDescriptors = true;
			boolean keepProtocolName = true;
			boolean keepPatientCharacteristics = true;
			boolean keepDeviceIdentity = true;
			boolean keepInstitutionIdentity = true;
			int handleDates = ClinicalTrialsAttributes.HandleDates.keep;
			int handleStructuredContent = ClinicalTrialsAttributes.HandleStructuredContent.keep;
			String seed = null;
			int numberOfFixedArguments = 8;
			int startOptionsPosition = numberOfFixedArguments;
			if (arg.length >= (numberOfFixedArguments+1)) {
				boolean seedPresent = false;
				// "https://stackoverflow.com/questions/5439529/determine-if-a-string-is-an-integer-in-java"
				Scanner sc = new Scanner(arg[numberOfFixedArguments]);
				if (sc.hasNextLong()) {		// needs to be Long not Int
					sc.nextLong();
					seedPresent = !sc.hasNext();
				}
				if (seedPresent) {
					seed = arg[numberOfFixedArguments];
					startOptionsPosition = (numberOfFixedArguments+1);
				}
			}
			if (arg.length >= (numberOfFixedArguments+1)) {
				for (int i=arg.length-1; i>=startOptionsPosition; --i) {
					String option = arg[i].trim().toUpperCase();
					switch (option) {
						case "KEEPALLPRIVATE":	keepAllPrivate = true; break;
						case "KEEPSAFEPRIVATE":	keepAllPrivate = false; break;
							
						case "ADDCONTRIBUTINGEQUIPMENT":		addContributingEquipmentSequence = true; break;
						case "DONOTADDCONTRIBUTINGEQUIPMENT":	addContributingEquipmentSequence = false; break;

						case "KEEPDESCRIPTORS":			keepDescriptors = true; break;
						case "DONOTKEEPDESCRIPTORS":	keepDescriptors = false; break;

						case "KEEPSERIESDESCRIPTORS":		keepSeriesDescriptors = true; break;
						case "DONOTKEEPSERIESDESCRIPTORS":	keepSeriesDescriptors = false; break;

						case "KEEPPROTOCOLNAME":		keepProtocolName = true; break;
						case "DONOTKEEPPROTOCOLNAME":	keepProtocolName = false; break;

						case "KEEPPATIENTCHARACTERISTICS":		keepPatientCharacteristics = true; break;
						case "DONOTKEEPPATIENTCHARACTERISTICS":	keepPatientCharacteristics = false; break;

						case "KEEPDEVICEIDENTITY":		keepDeviceIdentity = true; break;
						case "DONOTKEEPDEVICEIDENTITY":	keepDeviceIdentity = false; break;

						case "KEEPINSTITUTIONIDENTITY":			keepInstitutionIdentity = true; break;
						case "DONOTKEEPINSTITUTIONIDENTITY":	keepInstitutionIdentity = false; break;

						case "KEEPDATES":			handleDates = ClinicalTrialsAttributes.HandleDates.keep; break;
						case "MODIFYDATES":			handleDates = ClinicalTrialsAttributes.HandleDates.modify; break;
						case "DONOTKEEPDATES":		handleDates = ClinicalTrialsAttributes.HandleDates.remove; break;

						case "KEEPSTRUCTUREDCONTENT":			handleStructuredContent = ClinicalTrialsAttributes.HandleStructuredContent.keep; break;
						case "MODIFYSTRUCTUREDCONTENT":			handleStructuredContent = ClinicalTrialsAttributes.HandleStructuredContent.modify; break;
						case "DONOTKEEPSTRUCTUREDCONTENT":		handleStructuredContent = ClinicalTrialsAttributes.HandleStructuredContent.remove; break;

						default:	slf4jlogger.error("Unrecognized argument {}",option); bad = true; break;
					}
				}
			}
			if (!bad) {
				if (arg.length >= numberOfFixedArguments) {
					new PseudonymizeAndSequester(arg[0],arg[1],arg[2],arg[3],arg[4],arg[5],arg[6],arg[7],seed,
												 keepAllPrivate,addContributingEquipmentSequence,
												 keepDescriptors,keepSeriesDescriptors,keepProtocolName,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity,
												 handleDates,handleStructuredContent);
				}
				else {
					bad = true;
				}
			}
			if (bad) {
				slf4jlogger.error("Usage: PseudonymizeAndSequester inputPath outputFolderClean outputFolderDirty pseudonymizationControlFile pseudonymizationResultByOriginalPatientIDFile pseudonymizationResultByOriginalStudyInstanceUIDFile failedFilesFile uidMapResultFile [seed]"
								  +" [KEEPALLPRIVATE|KEEPSAFEPRIVATE]"
								  +" [ADDCONTRIBUTINGEQUIPMENT|DONOTADDCONTRIBUTINGEQUIPMENT]"
								  +" [KEEPDESCRIPTORS|DONOTKEEPDESCRIPTORS]"
								  +" [KEEPSERIESDESCRIPTORS|DONOTKEEPSERIESDESCRIPTORS]"
								  +" [KEEPPROTOCOLNAME|DONOTKEEPPROTOCOLNAME]"
								  +" [KEEPPATIENTCHARACTERISTICS|DONOTKEEPPATIENTCHARACTERISTICS]"
								  +" [KEEPDEVICEIDENTITY|DONOTKEEPDEVICEIDENTITY]"
								  +" [KEEPINSTITUTIONIDENTITY|DONOTKEEPINSTITUTIONIDENTITY]"
								  +" [KEEPDATES|MODIFYDATES|DONOTKEEPDATES]"
								  +" [KEEPSTRUCTUREDCONTENT|MODIFYSTRUCTUREDCONTENT|DONOTKEEPSTRUCTUREDCONTENT]"
				);
				System.exit(1);
			}
		}
		catch (Exception e) {
			slf4jlogger.error("",e);	// use SLF4J since may be invoked from script
		}
	}
}



