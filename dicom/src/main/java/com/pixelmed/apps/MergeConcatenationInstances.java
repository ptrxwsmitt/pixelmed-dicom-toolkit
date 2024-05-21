/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.ClinicalTrialsAttributes;
import com.pixelmed.dicom.CodedSequenceItem;
import com.pixelmed.dicom.CodingSchemeIdentification;
import com.pixelmed.dicom.DateTimeAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.FunctionalGroupUtilities;
import com.pixelmed.dicom.IntegerStringAttribute;
import com.pixelmed.dicom.MediaImporter;
import com.pixelmed.dicom.MoveDicomFilesIntoHierarchy;
import com.pixelmed.dicom.OtherByteAttributeCompressedSeparateFramesOnDisk;
import com.pixelmed.dicom.OtherByteAttributeMultipleCompressedFrames;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.dicom.UniqueIdentifierAttribute;
import com.pixelmed.dicom.VersionAndConstants;

import com.pixelmed.utils.CapabilitiesAvailable;
import com.pixelmed.utils.MessageLogger;
import com.pixelmed.utils.PrintStreamMessageLogger;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class containing an application for merging instances of a concatenation to (re-)create the source instance.</p>
 *
 * @author	dclunie
 */
public class MergeConcatenationInstances {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/MergeConcatenationInstances.java,v 1.3 2022/01/21 19:51:12 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(MergeConcatenationInstances.class);
	
	protected String ourAETitle = "OURAETITLE";
	
	class Concatenation {
		String concatenationUID;
		String sopInstanceUIDOfConcatenationSource;
		int numberOfInstancesInConcatenation;
		Attribute[] pixelDataAttributes;
		Attribute[] perFrameFunctionalGroupsAttributes;
		int[] concatenationFrameOffsetNumbers;
		int[] numberOfFrames;
		AttributeList list;
		String transferSyntaxUID;

		Concatenation(String concatenationUID) {
			this.concatenationUID = concatenationUID;
			numberOfInstancesInConcatenation = 0;
			pixelDataAttributes = null;
		}
		
		void addConcatenationInstance(AttributeList newList,String mediaFileName) {
			slf4jlogger.debug("Concatenation.addConcatenationInstance(): processing File {}",mediaFileName);
			String sopInstanceUIDOfConcatenationSourceInInstance = Attribute.getSingleStringValueOrEmptyString(newList,TagFromName.SOPInstanceUIDOfConcatenationSource);
			slf4jlogger.debug("Concatenation.addConcatenationInstance(): File {} has SOPInstanceUIDOfConcatenationSource {}",mediaFileName,sopInstanceUIDOfConcatenationSourceInInstance);
			if (sopInstanceUIDOfConcatenationSourceInInstance.length() > 0) {
				if (sopInstanceUIDOfConcatenationSource == null) {
					sopInstanceUIDOfConcatenationSource = sopInstanceUIDOfConcatenationSourceInInstance;
				}
				if (sopInstanceUIDOfConcatenationSource.equals(sopInstanceUIDOfConcatenationSourceInInstance)) {
					// files may be in arbitrary order, not necessarily by InConcatenationNumber, so expand and fill in arrays as we encounter instances ...
					int inConcatenationNumber = Attribute.getSingleIntegerValueOrDefault(newList,TagFromName.InConcatenationNumber,0);	// valid values start from 1
					slf4jlogger.info("File {} has InconcatenationNumber {}",mediaFileName,inConcatenationNumber);
					//slf4jlogger.debug("Concatenation.addConcatenationInstance(): File {} has InconcatenationNumber {}",mediaFileName,inConcatenationNumber);
					if (inConcatenationNumber > 0) {
						if (inConcatenationNumber > numberOfInstancesInConcatenation) {
							numberOfInstancesInConcatenation = inConcatenationNumber;
						}
						if (pixelDataAttributes == null) {
							pixelDataAttributes = new Attribute[numberOfInstancesInConcatenation];
							perFrameFunctionalGroupsAttributes = new Attribute[numberOfInstancesInConcatenation];
							concatenationFrameOffsetNumbers = new int[numberOfInstancesInConcatenation];
							numberOfFrames = new int[numberOfInstancesInConcatenation];
						}
						else {
							// expand arrays on demand as higher values of numberOfInstancesInConcatenation encountered
							if (pixelDataAttributes.length < numberOfInstancesInConcatenation) {
								pixelDataAttributes = Arrays.copyOf(pixelDataAttributes,numberOfInstancesInConcatenation);
								perFrameFunctionalGroupsAttributes = Arrays.copyOf(perFrameFunctionalGroupsAttributes,numberOfInstancesInConcatenation);
								concatenationFrameOffsetNumbers = Arrays.copyOf(concatenationFrameOffsetNumbers,numberOfInstancesInConcatenation);
								numberOfFrames = Arrays.copyOf(numberOfFrames,numberOfInstancesInConcatenation);
							}
							// else arrays are already large enough - we must be encountering an earlier instance
						}
						pixelDataAttributes[inConcatenationNumber-1] = newList.get(TagFromName.PixelData);	// note one less since indexed from 1
						perFrameFunctionalGroupsAttributes[inConcatenationNumber-1] = newList.get(TagFromName.PerFrameFunctionalGroupsSequence);
						concatenationFrameOffsetNumbers[inConcatenationNumber-1] = Attribute.getSingleIntegerValueOrDefault(newList,TagFromName.ConcatenationFrameOffsetNumber,-1);		// will check it was not missing later
						numberOfFrames[inConcatenationNumber-1] = Attribute.getSingleIntegerValueOrDefault(newList,TagFromName.NumberOfFrames,0);
						
						String instanceTransferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(newList,TagFromName.TransferSyntaxUID);
						if (instanceTransferSyntaxUID.length() > 0) {
							if (transferSyntaxUID == null) {
								transferSyntaxUID = instanceTransferSyntaxUID;
							}
							if (transferSyntaxUID.equals(instanceTransferSyntaxUID)) {
								newList.remove(TagFromName.PixelData);
								newList.remove(TagFromName.PerFrameFunctionalGroupsSequence);
								newList.remove(TagFromName.SOPInstanceUIDOfConcatenationSource);
								newList.remove(TagFromName.InConcatenationNumber);
								newList.remove(TagFromName.ConcatenationFrameOffsetNumber);
								newList.remove(TagFromName.NumberOfFrames);		// otherwise will not compare esp. last (fewer frames) instance
								newList.remove(TagFromName.ConcatenationUID);
								newList.remove(TagFromName.SOPInstanceUID);		// will replace it with SOPInstanceUIDOfConcatenationSource later
								newList.removeGroupLengthAttributes();
								newList.removeMetaInformationHeaderAttributes();	// includes TransferSyntaxUID
								newList.remove(TagFromName.DataSetTrailingPadding);
								
								if (list == null) {
									list = newList;
								}
								else {
									if (!list.equals(newList)) {
										slf4jlogger.warn("File {} non-concatenation-related attributes in instance do not match those encountered so far - ignoring the discrepancy",mediaFileName);
									}
								}
							}
							else {
								slf4jlogger.error("File {} TransferSyntaxUID {} does not match that of files in Concatenation already encountered {} - ignoring (not adding)",mediaFileName,instanceTransferSyntaxUID,transferSyntaxUID);
							}
						
						}
						else {
							slf4jlogger.error("File {} missing TransferSyntaxUID - ignoring (not adding)",mediaFileName);
						}
					}
					else {
						slf4jlogger.error("File {} missing InconcatenationNumber - ignoring (not adding)",mediaFileName);
					}
				}
				else {
					slf4jlogger.error("File {} SOPInstanceUIDOfConcatenationSource {} does not match SOPInstanceUIDOfConcatenationSource {} already encountered for ConcatenationUID {}",mediaFileName,sopInstanceUIDOfConcatenationSourceInInstance,sopInstanceUIDOfConcatenationSource,concatenationUID);
				}
			}
			else {
				slf4jlogger.error("File {} is missing SOPInstanceUIDOfConcatenationSource - ignoring (not adding)",mediaFileName);
			}
		}
		
		boolean write(String dstFolderName) throws DicomException, IOException {
			boolean good = true;
			int totalNumberOfFrames = 0;
			int[] actualFrameOffsetNumbers = new int[numberOfInstancesInConcatenation]; // need to compute this ourselves to work around incorrect ConcatenationFrameOffsetNumber values; good value needed for merging frames later
			for (int i=0; i<numberOfInstancesInConcatenation; ++i) {
				slf4jlogger.debug("Concatenation.write(): instance # {} in concatenation {} totalNumberOfFrames = ",i+1,concatenationUID,totalNumberOfFrames);
				slf4jlogger.debug("Concatenation.write(): instance # {} in concatenation {} concatenationFrameOffsetNumbers = ",i+1,concatenationUID,concatenationFrameOffsetNumbers[i]);
				if (concatenationFrameOffsetNumbers[i] != totalNumberOfFrames) {
					slf4jlogger.warn("ConcatenationFrameOffsetNumber {} for instance # {} in concatenation {} does not match running total from NumberOfFrames {} - ignoring problem and assuming NumberOfFrames count is correct",concatenationFrameOffsetNumbers[i],i+1,concatenationUID,totalNumberOfFrames);
					//good = false;
					//break;
				}
				actualFrameOffsetNumbers[i]=totalNumberOfFrames;
				int instanceNumberOfFrames = numberOfFrames[i];
				slf4jlogger.debug("Concatenation.write(): instance # {} in concatenation {} instanceNumberOfFrames = ",i+1,concatenationUID,instanceNumberOfFrames);
				if (instanceNumberOfFrames > 0) {
					totalNumberOfFrames += instanceNumberOfFrames;
				}
				else {
					slf4jlogger.error("Missing information for instance # {} in concatenation {} - giving up and writing nothing",i+1,concatenationUID);
					good = false;
					break;
				}
			}
			{ Attribute a = new IntegerStringAttribute(TagFromName.NumberOfFrames); a.addValue(totalNumberOfFrames); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUIDOfConcatenationSource); list.put(a); }

			// create new PerFrameFunctionalGroupsSequence if needed merging items in order ...
			if (perFrameFunctionalGroupsAttributes != null && perFrameFunctionalGroupsAttributes.length > 0 && perFrameFunctionalGroupsAttributes[0] != null) {
				slf4jlogger.info("Creating PerFrameFunctionalGroupsSequence");
				SequenceAttribute newPerFrameFunctionalGroupsSequence = new SequenceAttribute(TagFromName.PerFrameFunctionalGroupsSequence);
				list.put(newPerFrameFunctionalGroupsSequence);
				for (int i=0; i<numberOfInstancesInConcatenation; ++i) {
					SequenceAttribute instancePerFrameFunctionalGroupsSequence = (SequenceAttribute)(perFrameFunctionalGroupsAttributes[i]);
					if (instancePerFrameFunctionalGroupsSequence != null) {
						int nItems = instancePerFrameFunctionalGroupsSequence.getNumberOfItems();
						if (nItems == numberOfFrames[i]) {
							Iterator<SequenceItem> iti = instancePerFrameFunctionalGroupsSequence.iterator();
							while (iti.hasNext()) {
								SequenceItem item = iti.next();
								newPerFrameFunctionalGroupsSequence.addItem(item);
							}
						}
						else {
							slf4jlogger.error("PerFrameFunctionalGroupsSequence for instance # {} in concatenation {} - number of items {} does not match number of frames {} - giving up and writing nothing",i+1,concatenationUID,nItems,numberOfFrames[i]);
							good = false;
							break;
						}
					}
					else {
						slf4jlogger.error("Missing PerFrameFunctionalGroupsSequence for instance # {} in concatenation {} - giving up and writing nothing",i+1,concatenationUID);
						good = false;
						break;
					}
				}
			}
			else {
				slf4jlogger.info("Not creating PerFrameFunctionalGroupsSequence");
			}

			// create new PixelData attribute by merging tiles in order ...
			{
				// load everything into memory (blech :() because may have a mix of OtherByteAttributeMultipleCompressedFrames (memory or files) and OtherByteAttributeMultipleCompressedFrames that needs to be merged
				byte[][] frames = new byte[totalNumberOfFrames][];
				for (int i=0; i<numberOfInstancesInConcatenation; ++i) {
					Attribute aInstancePixelData = pixelDataAttributes[i];
					if (aInstancePixelData != null) {
						slf4jlogger.debug("Concatenation.write(): instance # {} in concatenation {} PixelData {}",i+1,concatenationUID,aInstancePixelData.getClass());
						if (aInstancePixelData instanceof OtherByteAttributeMultipleCompressedFrames) {
							OtherByteAttributeMultipleCompressedFrames a = (OtherByteAttributeMultipleCompressedFrames)aInstancePixelData;
							int runningOffset = actualFrameOffsetNumbers[i];
							slf4jlogger.debug("Concatenation.write(): instance # {} in concatenation {} copying {} frames to frame position {}",i+1,concatenationUID,numberOfFrames[i],runningOffset);
							for (int f=0; f<numberOfFrames[i]; ++f) {
								frames[runningOffset++] = a.getByteValuesForSelectedFrame(f);	// works regardless of whether in memory or on disk (reads if necessary)
							}
						}
						else if (aInstancePixelData instanceof OtherByteAttributeCompressedSeparateFramesOnDisk) {
							OtherByteAttributeCompressedSeparateFramesOnDisk a = (OtherByteAttributeCompressedSeparateFramesOnDisk)aInstancePixelData;
							int runningOffset = actualFrameOffsetNumbers[i];
							slf4jlogger.debug("Concatenation.write(): instance # {} in concatenation {} copying {} frames to frame position {}",i+1,concatenationUID,numberOfFrames[i],runningOffset);
							for (int f=0; f<numberOfFrames[i]; ++f) {
								frames[runningOffset++] = a.getByteValuesForSelectedFrame(f);
							}
						}
						else {
							slf4jlogger.error("Unsupported Attribute class for PixelData for instance # {} in concatenation {} - giving up and writing nothing",i+1,concatenationUID);
							good = false;
							break;
						}
					}
					else {
						slf4jlogger.error("Missing PixelData for instance # {} in concatenation {} - giving up and writing nothing",i+1,concatenationUID);
						good = false;
						break;
					}
				}
				if (good) {
					if (frames != null) {
						Attribute aPixelData = new OtherByteAttributeMultipleCompressedFrames(TagFromName.PixelData,frames);
						list.put(aPixelData);
					}
				}
			}
			
			if (good) {
				ClinicalTrialsAttributes.addContributingEquipmentSequence(list,true,new CodedSequenceItem("109103","DCM","Modifying Equipment"),
																		  "PixelMed",													// Manufacturer
																		  "PixelMed",													// Institution Name
																		  "Software Development",										// Institutional Department Name
																		  "Bangor, PA",													// Institution Address
																		  null,															// Station Name
																		  "com.pixelmed.apps.MergeConcatenationInstances",				// Manufacturer's Model Name
																		  null,															// Device Serial Number
																		  "Vers. "+VersionAndConstants.getBuildDate(),					// Software Version(s)
																		  "Merged concatenation instances");
			
				CodingSchemeIdentification.replaceCodingSchemeIdentificationSequenceWithCodingSchemesUsedInAttributeList(list);
				list.insertSuitableSpecificCharacterSetForAllStringValues();	// (001158)
				//already performed removeGroupLengthAttributes(), removeMetaInformationHeaderAttributes() and remove(TagFromName.DataSetTrailingPadding)
				FileMetaInformation.addFileMetaInformation(list,transferSyntaxUID,ourAETitle);
			
				File dstFile = new File(dstFolderName,MoveDicomFilesIntoHierarchy.makeHierarchicalPathFromAttributes(list));
				if (dstFile.exists()) {
					throw new DicomException("Concatenation "+concatenationUID+": new file \""+dstFile+"\" already exists - not overwriting");
				}
				else {
					File dstParentDirectory = dstFile.getParentFile();
					if (!dstParentDirectory.exists()) {
						if (!dstParentDirectory.mkdirs()) {
							throw new DicomException("Concatenation "+concatenationUID+": parent directory creation failed for \""+dstFile+"\"");
						}
					}
					//logLn("Writing with new functional groups file "+dstFile);
					slf4jlogger.info("Writing merged instances of Concatenation {} into file {}",concatenationUID,dstFile);
					list.write(dstFile,transferSyntaxUID,true,true);
				}
			}

			return good;
		}
	}
	
	SortedMap<String,Concatenation> concatenationsByConcatenationUID = null;
	
	protected void mergeConcatenationInstances(AttributeList newList,String mediaFileName) throws DicomException {
		String concatenationUID = Attribute.getSingleStringValueOrEmptyString(newList,TagFromName.ConcatenationUID);
		if (concatenationUID.length() > 0) {
			if (concatenationsByConcatenationUID == null) {
				concatenationsByConcatenationUID = new TreeMap<String,Concatenation>();
			}
			Concatenation concatenation = concatenationsByConcatenationUID.get(concatenationUID);
			if (concatenation == null) {
				concatenation = new Concatenation(concatenationUID);
				concatenationsByConcatenationUID.put(concatenationUID,concatenation);
			}
			concatenation.addConcatenationInstance(newList,mediaFileName);
		}
		else {
			slf4jlogger.warn("File {} is not an instance of a concatenation - ignoring (not copying)",mediaFileName);
		}
	}

	protected class OurMediaImporter extends MediaImporter {
		
		public OurMediaImporter(MessageLogger logger) {
			super(logger);
		}
		
		protected void doSomethingWithDicomFileOnMedia(String mediaFileName) {
			//logLn("OurMediaImporter.doSomethingWithDicomFile(): "+mediaFileName);
			try {
				DicomInputStream i = new DicomInputStream(new File(mediaFileName));
				AttributeList list = new AttributeList();
				list.setDecompressPixelData(false);
				list.read(i);
				i.close();

				mergeConcatenationInstances(list,mediaFileName);
			}
			catch (Exception e) {
				//logLn("Error: File "+mediaFileName+" exception "+e);
				slf4jlogger.error("File "+mediaFileName,e);
			}
		}

		// mimic DicomCleaner behavior ...
		
		protected boolean acceptAnyTransferSyntax = false;
		protected boolean canUseBzip = CapabilitiesAvailable.haveBzip2Support();

		// override base class isOKToImport(), which rejects unsupported compressed transfer syntaxes
		
		protected boolean isOKToImport(String sopClassUID,String transferSyntaxUID) {
			slf4jlogger.debug("isOKToImport(): transferSyntaxUID {}",transferSyntaxUID);
			if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("isOKToImport(): {}",(transferSyntaxUID != null && transferSyntaxUID.length() > 0) ? new TransferSyntax(transferSyntaxUID).dump() : "");
			slf4jlogger.debug("isOKToImport(): sopClassUID {}",sopClassUID);
			slf4jlogger.debug("isOKToImport(): isImageStorage {}",SOPClass.isImageStorage(sopClassUID));
			return sopClassUID != null
				&& SOPClass.isImageStorage(sopClassUID)
				&& (transferSyntaxUID != null	/* fail if missing from meta information or no meta information, i.e., do not support fix for (001136) for this usage */
				 || (acceptAnyTransferSyntax && new TransferSyntax(transferSyntaxUID).isRecognized())
				 || transferSyntaxUID.equals(TransferSyntax.ImplicitVRLittleEndian)
				 || transferSyntaxUID.equals(TransferSyntax.ExplicitVRLittleEndian)
				 || transferSyntaxUID.equals(TransferSyntax.ExplicitVRBigEndian)
				 || transferSyntaxUID.equals(TransferSyntax.DeflatedExplicitVRLittleEndian)
				 || (transferSyntaxUID.equals(TransferSyntax.PixelMedBzip2ExplicitVRLittleEndian) && canUseBzip)
				 || transferSyntaxUID.equals(TransferSyntax.RLE)
				 || transferSyntaxUID.equals(TransferSyntax.JPEGBaseline)
				 || CapabilitiesAvailable.haveJPEGLosslessCodec() && (transferSyntaxUID.equals(TransferSyntax.JPEGLossless) || transferSyntaxUID.equals(TransferSyntax.JPEGLosslessSV1))
				 || CapabilitiesAvailable.haveJPEG2000Part1Codec() && (transferSyntaxUID.equals(TransferSyntax.JPEG2000) || transferSyntaxUID.equals(TransferSyntax.JPEG2000Lossless))
				 || CapabilitiesAvailable.haveJPEGLSCodec() && (transferSyntaxUID.equals(TransferSyntax.JPEGLS) || transferSyntaxUID.equals(TransferSyntax.JPEGNLS))
				);
		}
	}
	
	/**
	 * <p>Merge instances of a concatenation to (re-)create the source instance.</p>
	 *
	 * @param	srcs			source folders or DICOMDIRs
	 * @param	dstFolderName	destination folder
	 * @param	logger			logger to send progress, warnings and errors
	 * @throws	IOException		if there is a problem reading or writing
	 * @throws	DicomException	if there is a problem parsing or extracting required content
	 */
	public MergeConcatenationInstances(String[] srcs,String dstFolderName,MessageLogger logger) throws IOException, DicomException {
//System.err.println("MergeConcatenationInstances(): dstFolderName = "+dstFolderName);
		
		OurMediaImporter importer = new OurMediaImporter(logger);
		for (String src : srcs) {
			importer.importDicomFiles(src);
		}
		
		for (String uid : concatenationsByConcatenationUID.keySet()) {		// do this by sorted key only so order of processing is deterministic
			Concatenation concatenation = concatenationsByConcatenationUID.get(uid);
			concatenation.write(dstFolderName);
		}
	}
	
	/**
	 * <p>Merge instances of a concatenation to (re-)create the source instance.</p>
	 *
	 * @param	arg		array of 2 or more strings - one or more source folders or DICOMDIR (to merge), and a destination folder
	 */
	public static void main(String arg[]) {
		try {
			if (arg.length >= 2) {
				MessageLogger logger = new PrintStreamMessageLogger(System.err);
				new MergeConcatenationInstances(Arrays.copyOf(arg,arg.length-1),arg[arg.length-1],logger);
			}
			else {
				System.err.println("Usage: java -cp ./pixelmed.jar com.pixelmed.apps.MergeConcatenationInstances srcdir|DICOMDIR [srcdir|DICOMDIR]* dstdir");
			}
		}
		catch (Exception e) {
			slf4jlogger.error("",e);	// use SLF4J since may be invoked from script
			System.exit(0);
		}
	}
}

