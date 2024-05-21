/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.anatproc.CTAnatomy;
import com.pixelmed.anatproc.DisplayableAnatomicConcept;
import com.pixelmed.anatproc.DisplayableLateralityConcept;
import com.pixelmed.anatproc.DisplayableViewConcept;
import com.pixelmed.anatproc.ProjectionXRayAnatomy;

import com.pixelmed.display.MammoDemographicAndTechniqueAnnotations;

import com.pixelmed.utils.FileUtilities;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>This class provides a main method that recursively searches the supplied paths for DICOM files
 * and moves them into a folder hierarchy based on their attributes.</p>
 *
 * <p>Various static utility methods that assist in this operation are also provided, such as to
 * create the hierarchical path name from the attributes, etc., since these may be useful in their
 * own right.</p>
 *
 * @author	dclunie
 */
public class MoveDicomFilesIntoHierarchy {
	
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/MoveDicomFilesIntoHierarchy.java,v 1.28 2022/01/21 19:51:16 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(MoveDicomFilesIntoHierarchy.class);

	static protected String defaultHierarchicalFolderName = "Sorted";
	static protected String defaultDuplicatesFolderNamePrefix = "Duplicates";
	
	public class FolderNamingStrategy {
		public static final int instanceByUID = 0;
		public static final int instanceByNumber = 1;
		public static final int instanceByNumberAnatomyLateralityView = 2;
		public static final int instanceByUIDUncleaned = 3;
	}
	
	static protected int folderNamingStrategyToUseIfUnspecified = FolderNamingStrategy.instanceByUIDUncleaned;
	
	protected boolean includeDateTimeInSeriesFolderName = false;
	protected boolean includeInConcatenationNumberWithInstanceNumber = false;

	static protected void processFilesRecursively(File file,String suffix) throws SecurityException, IOException, DicomException, NoSuchAlgorithmException {
		if (file != null && file.exists()) {
			if (file.isFile() && (suffix == null || suffix.length() == 0 || file.getName().endsWith(suffix))) {
				renameFileWithHierarchicalPathFromAttributes(file);
			}
			else if (file.isDirectory()) {
				{
					File[] filesAndDirectories = file.listFiles();
					if (filesAndDirectories != null && filesAndDirectories.length > 0) {
						for (int i=0; i<filesAndDirectories.length; ++i) {
							processFilesRecursively(filesAndDirectories[i],suffix);
						}
					}
				}
			}
			// else what else could it be
		}
	}

	/**
	 * <p>Create a patient label based on the DICOM attributes of the form:</p>
	 *
	 * <pre>PatientName [PatientID]</pre>
	 *
	 * If missing values defaults to <pre>NONAME [NOID]</pre>
	 *
	 * Non-static so that sub-classes can override it.
	 *
	 * @param	list	list of attributes
	 * @param	folderNamingStrategy
	 * @return			the patient label
	 */
	public String makePatientLabelFromAttributes(AttributeList list,int folderNamingStrategy) {
		String patientID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientID)
			.replaceAll("[^A-Za-z0-9 -]","_").replaceAll("^[ _]*","").replaceAll("[ _]*$","").replaceAll("[ ][ ]*"," ").replaceAll("[_][_]*","_").replaceAll("[_][ ]"," ");
		if (patientID.length() == 0) {
			patientID = "NOID";
		}
		String patientName = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PatientName)
			.replaceAll("[^A-Za-z0-9 ^=,.-]","_").replaceAll("^[ _]*","").replaceAll("[ _]*$","").replaceAll("[ ][ ]*"," ").replaceAll("[_][_]*","_").replaceAll("[_][ ]"," ").replaceAll("^[.]","_");
		if (patientName.length() == 0) {
			patientName = "NONAME";
		}
		return patientName + " [" + patientID + "]";
	}

	/**
	 * <p>Create a study label based on the DICOM attributes of the form:</p>
	 *
	 * <pre>StudyDate StudyTime [StudyID - StudyDescription]</pre>
	 *
	 * Non-static so that sub-classes can override it.
	 *
	 * @param	list	list of attributes
	 * @param	folderNamingStrategy
	 * @return			the study label
	 */
	public String makeStudyLabelFromAttributes(AttributeList list,int folderNamingStrategy) {
		String studyDate = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyDate).replaceAll("[^0-9]","").trim();
		if (studyDate.length() == 0) { studyDate = "19000101"; }
		while (studyDate.length() < 8) { studyDate = studyDate + "0"; }
		
		String studyTime = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyTime).replaceFirst("[.].*$","").replaceAll("[^0-9]","");
		while (studyTime.length() < 6) { studyTime = studyTime + "0"; }
		
		String studyID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyID)
			.replaceAll("[^A-Za-z0-9 ]","_").replaceAll("^[ _]*","").replaceAll("[ _]*$","").replaceAll("[ ][ ]*"," ").replaceAll("[_][_]*","_").replaceAll("[_][ ]*"," ");
		
		String studyDescription = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.StudyDescription)
			.replaceAll("[^A-Za-z0-9 ]","_").replaceAll("^[ _]*","").replaceAll("[ _]*$","").replaceAll("[ ][ ]*"," ").replaceAll("[_][_]*","_").replaceAll("[_][ ]*"," ");
		
		String studyLabel = "";
		if (studyID.length() == 0) {
			if (studyDescription.length() == 0) {
				studyLabel = studyDate + " " + studyTime;
			}
			else {
				studyLabel = studyDate + " " + studyTime + " [ - " + studyDescription + "]";
			}
		}
		else {
			if (studyDescription.length() == 0) {
				studyLabel = studyDate + " " + studyTime + " [" + studyID + "]";
			}
			else {
				studyLabel = studyDate + " " + studyTime + " [" + studyID + " - " + studyDescription + "]";
			}
		}
		return studyLabel;
	}

	/**
	 * <p>Create a series label based on the DICOM attributes of the form:</p>
	 *
	 * <pre>Series SeriesNumber [Modality - SeriesDescription]</pre>
	 *
	 * Non-static so that sub-classes can override it.
	 *
	 * @param	list	list of attributes
	 * @param	folderNamingStrategy
	 * @return			the series label
	 */
	 public String makeSeriesLabelFromAttributes(AttributeList list,int folderNamingStrategy) {
		String seriesNumber = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesNumber).replaceAll("[^0-9]","");
		while (seriesNumber.length() < 3) { seriesNumber = "0" + seriesNumber; }
		
		String seriesDescription = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesDescription)
			.replaceAll("[^A-Za-z0-9 ]","_").replaceAll("^[ _]*","").replaceAll("[ _]*$","").replaceAll("[ ][ ]*"," ").replaceAll("[_][_]*","_").replaceAll("[_][ ]*"," ");
		
		String modality = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.Modality)
			.replaceAll("[^A-Za-z0-9 ]","_").replaceAll("^[ _]*","").replaceAll("[ _]*$","").replaceAll("[ ][ ]*"," ").replaceAll("[_][_]*","_").replaceAll("[_][ ]*"," ")
			.toUpperCase(Locale.US);

		String seriesLabel = "";
		if (modality.length() == 0) {
			if (seriesDescription.length() == 0) {
				seriesLabel = "Series " + seriesNumber + " []";
			}
			else {
				seriesLabel = "Series " + seriesNumber + " [ - " + seriesDescription + "]";
			}
		}
		else {
			if (seriesDescription.length() == 0) {
				seriesLabel = "Series " + seriesNumber + " [" + modality + "]";
			}
			else {
				seriesLabel = "Series " + seriesNumber + " [" + modality + " - " + seriesDescription + "]";
			}
		}

		String dateTime = "";
		if (includeDateTimeInSeriesFolderName) {
			String seriesDate = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesDate).replaceAll("[^0-9]","").trim();
			String seriesTime = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SeriesTime).replaceAll("[^0-9]","").trim();
			if (seriesDate.length() > 0 && seriesTime.length() > 0) {
				while (seriesDate.length() < 8) { seriesDate = seriesDate + "0"; }
				while (seriesTime.length() < 6) { seriesTime = seriesTime + "0"; }
				dateTime = seriesDate + seriesTime;
			}
			else {
				String acquisitionDate = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.AcquisitionDate).replaceAll("[^0-9]","").trim();
				String acquisitionTime = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.AcquisitionTime).replaceAll("[^0-9]","").trim();
				if (acquisitionDate.length() > 0 && acquisitionTime.length() > 0) {
					while (acquisitionDate.length() < 8) { acquisitionDate = acquisitionDate + "0"; }
					while (acquisitionTime.length() < 6) { acquisitionTime = acquisitionTime + "0"; }
					dateTime = acquisitionDate + acquisitionTime;
				}
				else {
					String acquisitionDateTime = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.AcquisitionDateTime).replaceAll("[^0-9+-]","").trim();
					if (acquisitionDateTime.length() > 0) {
						dateTime = acquisitionDateTime;
					}
				}
			}
			if (dateTime.length() > 0) {
				seriesLabel = seriesLabel + " " + dateTime;
			}
		}
		return seriesLabel;
	}

	private String makeInstanceLabelFromAttributesUsingInstanceNumberAnatomyLateralityView(AttributeList list) {
		slf4jlogger.debug("makeInstanceLabelFromAttributesUsingInstanceNumberAnatomyLateralityView():");
		String instanceLabel = "";
		String instanceNumber = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.InstanceNumber).replaceAll("[^0-9]","").trim();
		slf4jlogger.debug("makeInstanceLabelFromAttributesUsingInstanceNumberAnatomyLateralityView(): instanceNumber = {}",instanceNumber);
		String inConcatenationNumber = includeInConcatenationNumberWithInstanceNumber ? Attribute.getSingleStringValueOrEmptyString(list,TagFromName.InConcatenationNumber).replaceAll("[^0-9]","").trim() : "";
		slf4jlogger.debug("makeInstanceLabelFromAttributesUsingInstanceNumberAnatomyLateralityView(): includeInConcatenationNumberWithInstanceNumber {} inConcatenationNumber = {}",includeInConcatenationNumberWithInstanceNumber,inConcatenationNumber);

		if (instanceNumber.length() > 0) {
			while (instanceNumber.length() < 6) { instanceNumber = "0" + instanceNumber; }
			if (inConcatenationNumber.length() > 0) {
				while (inConcatenationNumber.length() < 6) { inConcatenationNumber = "0" + inConcatenationNumber; }
			}
			
			String anatomy = "";
			String laterality = "";
			String view = "";
			{
				String modality = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.Modality);
				if (modality.equals("MG")) {
					anatomy="BREAST";
				}
				else if (modality.equals("CT") || modality.equals("MR") || modality.equals("PT")) {
					DisplayableAnatomicConcept anatomicConcept = CTAnatomy.findAnatomicConcept(list);
					if (anatomicConcept != null) {
						//anatomy = anatomicConcept.getCodeMeaning().toUpperCase().replaceAll("[ ]","").trim();
						anatomy = anatomicConcept.getCodeStringEquivalent();
					}
				}
				else {
					DisplayableAnatomicConcept anatomicConcept = ProjectionXRayAnatomy.findAnatomicConcept(list);
					if (anatomicConcept != null) {
						anatomy =  anatomicConcept.getCodeMeaning().toUpperCase().replaceAll("[ ]","").trim();
						DisplayableLateralityConcept lateralityConcept = ProjectionXRayAnatomy.findLaterality(list,anatomicConcept);
						if (lateralityConcept != null) {
							//laterality = lateralityConcept.getCodeMeaning().toUpperCase().replaceAll("[ ]","").trim();
							laterality = lateralityConcept.getCodeStringEquivalent();
						}
					}
					DisplayableViewConcept viewConcept = ProjectionXRayAnatomy.findView(list);
					if (viewConcept != null) {
						//view = viewConcept.getCodeMeaning().toUpperCase().replaceAll("[ ]","").trim();
						view = viewConcept.getCodeStringEquivalent();
					}
				}
			}
			
			String lateralityAndView = "";
			if (laterality.length() > 0 && view.length() > 0) {
				lateralityAndView = laterality + view;
			}
			else {
				lateralityAndView = MammoDemographicAndTechniqueAnnotations.getAbbreviationFromImageLateralityViewModifierAndViewModifierCodeSequenceAttributes(list);
				if (lateralityAndView.length() == 0) {
					if (laterality.length() == 0) {
						laterality = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.ImageLaterality).toUpperCase().replaceAll("[^A-Z0-9]","").trim();
					}
					if (laterality.length() == 0) {
						laterality = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.Laterality).toUpperCase().replaceAll("[^A-Z0-9]","").trim();
					}
					if (view.length() == 0) {
						view = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.ViewPosition).toUpperCase().replaceAll("[^A-Z0-9]","").trim();
					}
					lateralityAndView = laterality + view;
				}
			}
			
			instanceLabel = instanceNumber + (inConcatenationNumber.length() == 0 ? "" : ("_"+inConcatenationNumber)) + (anatomy.length() == 0 ? "" : ("_"+anatomy)) + (lateralityAndView.length() == 0 ? "" : ("_"+lateralityAndView)) + ".dcm";
			slf4jlogger.debug("makeInstanceLabelFromAttributesUsingInstanceNumberAnatomyLateralityView(): instanceLabel = {}",instanceLabel);
		}
		if (instanceLabel.length() == 0) {
			// do not want to leave unlabelled if we can find something else like SOPInstanceUID ...
			slf4jlogger.debug("makeInstanceLabelFromAttributesUsingInstanceNumberAnatomyLateralityView(): no instanceLabel so using SOPInstanceUID");
			instanceLabel = makeInstanceLabelFromAttributesUsingSOPInstanceUID(list);
		}
		return instanceLabel;
	}

	private String makeInstanceLabelFromAttributesUsingInstanceNumberOnly(AttributeList list) {
		String instanceLabel = "";
		String instanceNumber = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.InstanceNumber).replaceAll("[^0-9]","").trim();
		slf4jlogger.debug("makeInstanceLabelFromAttributesUsingInstanceNumberOnly(): instanceNumber = {}",instanceNumber);
		String inConcatenationNumber = includeInConcatenationNumberWithInstanceNumber ? Attribute.getSingleStringValueOrEmptyString(list,TagFromName.InConcatenationNumber).replaceAll("[^0-9]","").trim() : "";
		slf4jlogger.debug("makeInstanceLabelFromAttributesUsingInstanceNumberOnly(): includeInConcatenationNumberWithInstanceNumber {} inConcatenationNumber = {}",includeInConcatenationNumberWithInstanceNumber,inConcatenationNumber);
		if (instanceNumber.length() > 0) {
			while (instanceNumber.length() < 6) { instanceNumber = "0" + instanceNumber; }
			if (inConcatenationNumber.length() > 0) {
				while (inConcatenationNumber.length() < 6) { inConcatenationNumber = "0" + inConcatenationNumber; }
			}
			instanceLabel = instanceNumber + (inConcatenationNumber.length() == 0 ? "" : ("_"+inConcatenationNumber)) + ".dcm";
		}
		if (instanceLabel.length() == 0) {
			// do not want to leave unlabelled if we can find something else like SOPInstanceUID ...
			slf4jlogger.debug("makeInstanceLabelFromAttributesUsingInstanceNumberOnly(): no instanceLabel so using SOPInstanceUID");
			instanceLabel = makeInstanceLabelFromAttributesUsingSOPInstanceUID(list);
		}
		return instanceLabel;
	}

	private String makeInstanceLabelFromAttributesUsingSOPInstanceUID(AttributeList list) {
		String instanceLabel = "";
		String sopInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SOPInstanceUID).replaceAll("[^0-9.]","").trim();
		if (sopInstanceUID.length() > 0) {
			instanceLabel = sopInstanceUID + ".dcm";
		}
		return instanceLabel;
	}

	private String makeInstanceLabelFromAttributesUsingSOPInstanceUIDUncleaned(AttributeList list) {
		String instanceLabel = "";
		String sopInstanceUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SOPInstanceUID).trim();
		if (sopInstanceUID.length() > 0) {
			instanceLabel = sopInstanceUID + ".dcm";
		}
		return instanceLabel;
	}

	/**
	 * <p>Create an instance label based on the DICOM attributes of the form:</p>
	 *
	 * <p>If no information to create an instance label (the SOPInstanceUID is missing), an empty String is returned.</p>
	 *
	 * <pre>SOPInstanceUID.dcm</pre>
	 *
	 * Non-static so that sub-classes can override it.
	 *
	 * @param	list					list of attributes
	 * @param	folderNamingStrategy
	 * @return							the instance label or an empty string
	 */
	public String makeInstanceLabelFromAttributes(AttributeList list,int folderNamingStrategy) {
		String instanceLabel = "";
		switch (folderNamingStrategy) {
			case FolderNamingStrategy.instanceByUID:							instanceLabel = makeInstanceLabelFromAttributesUsingSOPInstanceUID(list); break;
			case FolderNamingStrategy.instanceByNumber:							instanceLabel = makeInstanceLabelFromAttributesUsingInstanceNumberOnly(list); break;
			case FolderNamingStrategy.instanceByNumberAnatomyLateralityView:	instanceLabel = makeInstanceLabelFromAttributesUsingInstanceNumberAnatomyLateralityView(list); break;
			case FolderNamingStrategy.instanceByUIDUncleaned:					instanceLabel = makeInstanceLabelFromAttributesUsingSOPInstanceUIDUncleaned(list); break;
		}
		return instanceLabel;
	}
	
	/**
	 * <p>Create a folder structure based on the DICOM attributes of the form:</p>
	 *
	 * <pre>PatientName [PatientID]/StudyDate StudyTime [StudyID - StudyDescription]/Series SeriesNumber [Modality - SeriesDescription]/xxx.dcm</pre>
	 *
	 * <p>SeriesNumber is zero padded to three digits to better sort in browser.</p>
	 *
	 * <p>The form of the instance level name is controlled by the folderNamingStrategy parameter.</p>
	 *
	 * <p>If no information to create an instance label (the SOPInstanceUID is missing), an empty String is returned.</p>
	 *
	 * Non-static so that sub-classes can override it and callers can call this method rather than static version that will not use overridden subclasses.
	 *
	 * @param	list					list of attributes
	 * @param	folderNamingStrategy
	 * @return							the folder structure as a path
	 */
	public String makeHierarchicalPathFromAttributes(AttributeList list,int folderNamingStrategy) {
		String hierarchicalPathName = "";
		{
			String instanceLabel = makeInstanceLabelFromAttributes(list,folderNamingStrategy);
			if (instanceLabel.length() > 0) {
				hierarchicalPathName =
				makePatientLabelFromAttributes(list,folderNamingStrategy)
				+ "/" + makeStudyLabelFromAttributes(list,folderNamingStrategy)
				+ "/" + makeSeriesLabelFromAttributes(list,folderNamingStrategy)
				+ "/" + instanceLabel;
//System.err.println(hierarchicalPathName);
			}
		}
		return hierarchicalPathName;
	}
	
	/**
	 * <p>Create a folder structure based on the DICOM attributes of the form:</p>
	 *
	 * <pre>PatientName [PatientID]/StudyDate StudyTime [StudyID - StudyDescription]/Series SeriesNumber [Modality - SeriesDescription]/SOPInstanceUID.dcm</pre>
	 *
	 * <p>SeriesNumber is zero padded to three digits to better sort in browser.</p>
	 *
	 * <p>If no information to create an instance label (the SOPInstanceUID is missing), an empty String is returned.</p>
	 *
	 * Static so will NOT use overridden label methods in sub-classes - use non-static {@link MoveDicomFilesIntoHierarchy#makeHierarchicalPathFromAttributes(AttributeList,int) makeHierarchicalPathFromAttributes()} if necessary instead.
	 *
	 * @param	list	list of attributes
	 * @return			the folder structure as a path
	 */
	public static String makeHierarchicalPathFromAttributes(AttributeList list) {
		MoveDicomFilesIntoHierarchy us = new MoveDicomFilesIntoHierarchy();	// we will be calling non-static methods that may be overriden in sub-classes
		return us.makeHierarchicalPathFromAttributes(list,folderNamingStrategyToUseIfUnspecified);
	}

	/**
	 * <p>Rename a DICOM file into a folder hierarchy based on its attributes that are already available.</p>
	 *
	 * <p>Creates a folder structure in the current working directory of the form:</p>
	 *
	 * <pre>hierarchicalFolderName/PatientName [PatientID]/StudyDate StudyTime [StudyID - StudyDescription]/Series SeriesNumber [Modality - Series Description]/SOPInstanceUID.dcm</pre>
	 *
	 * <p>The form of the instance level name is controlled by the folderNamingStrategy parameter.</p>
	 *
	 * <p>If the destination file already exists and is identical in content, the original is removed.</p>
	 *
	 * <p>If the destination file already exists and is different in content, it is not overwritten, and
	 * the duplicate is moved into a separate duplicatesFolderNamePrefix_n folder.</p>
	 *
	 * Non-static so that sub-classes can override it and callers can call this method rather than static version that will not use overridden subclasses.
	 *
	 * @param	file						the DICOM file
	 * @param	list						the attributes of the file (already read in)
	 * @param	hierarchicalFolderName		where to store the renamed file
	 * @param	duplicatesFolderNamePrefix	where to store the renamed file if it is a non-identical duplicate of the existing file
	 * @param	folderNamingStrategy
	 * @return								the path to the new file if successful, null if not
	 * @throws	IOException					if an error occurs renaming the files
	 * @throws	DicomException				if there is an error parsing the attribute list
	 * @throws	NoSuchAlgorithmException	if there is an error checking duplicate files contain identical content caused by absence of a hash algorithm
	 */
	public String renameFileWithHierarchicalPathFromAttributes(File file,AttributeList list,String hierarchicalFolderName,String duplicatesFolderNamePrefix,int folderNamingStrategy)
			throws IOException, DicomException, NoSuchAlgorithmException {
		boolean success = false;
		File newFile = null;
		{
			String newFileName = makeHierarchicalPathFromAttributes(list,folderNamingStrategy);
			if (newFileName.length() > 0) {
				newFile = new File(hierarchicalFolderName,newFileName);
				if (file.getCanonicalPath().equals(newFile.getCanonicalPath())) {		// Note that file.equals(newFile) is NOT sufficient, and if used will lead to deletion when hash values match below
					System.err.println("\""+file+"\": source and destination same - doing nothing");
				}
				else {
					int duplicateCount=0;
					boolean proceed = false;
					boolean skipMove = false;
					while (!proceed) {
						File newParentDirectory = newFile.getParentFile();
						if (newParentDirectory != null && !newParentDirectory.exists()) {
							if (!newParentDirectory.mkdirs()) {
								System.err.println("\""+file+"\": parent directory creation failed for \""+newFile+"\"");
								// don't suppress move; might still succeed
							}
						}
						if (newFile.exists()) {
							if (FileUtilities.md5(file.getCanonicalPath()).equals(FileUtilities.md5(newFile.getCanonicalPath()))) {
								System.err.println("\""+file+"\": destination exists and is identical - not overwriting - removing original \""+newFile+"\"");
								if (!file.delete()) {
									System.err.println("\""+file+"\": deletion of duplicate original unsuccessful");
								}
								skipMove=true;
								proceed=true;
							}
							else {
								System.err.println("\""+file+"\": destination exists and is different - not overwriting - move duplicate elsewhere \""+newFile+"\"");
								boolean foundNewHome = false;
								newFile = new File(duplicatesFolderNamePrefix+"_"+Integer.toString(++duplicateCount),newFileName);
								// loop around rather than proceed
							}
						}
						else {
							proceed=true;
						}
					}
					if (!skipMove) {
						if (file.renameTo(newFile)) {
							success = true;
							System.err.println("\""+file+"\" moved to \""+newFile+"\"");
						}
						else {
							System.err.println("\""+file+"\": move attempt failed to \""+newFile+"\"");
						}
					}
				}
			}
			else {
				System.err.println("\""+file+"\": no instance label (may be no SOP Instance UID) - doing nothing");
			}
		}
		return success ? (newFile == null ? null : newFile.getCanonicalPath()) : null;
	}

	/**
	 * <p>Rename a DICOM file into a folder hierarchy based on its attributes that are already available.</p>
	 *
	 * <p>Creates a folder structure in the current working directory of the form:</p>
	 *
	 * <pre>hierarchicalFolderName/PatientName [PatientID]/StudyDate StudyTime [StudyID - StudyDescription]/Series SeriesNumber [Modality - Series Description]/SOPInstanceUID.dcm</pre>
	 *
	 * <p>If the destination file already exists and is identical in content, the original is removed.</p>
	 *
	 * <p>If the destination file already exists and is different in content, it is not overwritten, and
	 * the duplicate is moved into a separate duplicatesFolderNamePrefix_n folder.</p>
	 *
	 * Static so will NOT use overridden label methods in sub-classes - use non-static {@link MoveDicomFilesIntoHierarchy#renameFileWithHierarchicalPathFromAttributes(File,AttributeList,String,String) renameFileWithHierarchicalPathFromAttributes()} if necessary instead.
	 *
	 * @param	file						the DICOM file
	 * @param	list						the attributes of the file (already read in)
	 * @param	hierarchicalFolderName		where to store the renamed file
	 * @param	duplicatesFolderNamePrefix	where to store the renamed file if it is a non-identical duplicate of the existing file
	 * @return								the path to the new file if successful, null if not
	 * @throws	IOException					if an error occurs renaming the files
	 * @throws	DicomException				if there is an error parsing the attribute list
	 * @throws	NoSuchAlgorithmException	if there is an error checking duplicate files contain identical content caused by absence of a hash algorithm
	 */
	static public String renameFileWithHierarchicalPathFromAttributes(File file,AttributeList list,String hierarchicalFolderName,String duplicatesFolderNamePrefix)
			throws IOException, DicomException, NoSuchAlgorithmException {
		MoveDicomFilesIntoHierarchy us = new MoveDicomFilesIntoHierarchy();	// we will be calling non-static methods that may be overriden in sub-classes
		return us.renameFileWithHierarchicalPathFromAttributes(file,list,hierarchicalFolderName,duplicatesFolderNamePrefix,folderNamingStrategyToUseIfUnspecified);
	}

	protected static class OurReadTerminationStrategy implements AttributeList.ReadTerminationStrategy {
		public boolean terminate(AttributeList attributeList,AttributeTag tag,long byteOffset) {
			return tag.getGroup() > 0x0020;
		}
	}
	
	protected final static AttributeList.ReadTerminationStrategy terminateAfterRelationshipGroup = new OurReadTerminationStrategy();
	
	/**
	 * <p>Rename a DICOM file into a folder hierarchy based on its attributes.</p>
	 *
	 * <p>Creates a folder structure in the current working directory of the form:</p>
	 *
	 * <pre>hierarchicalFolderName/PatientName [PatientID]/StudyDate StudyTime [StudyID - StudyDescription]/Series SeriesNumber [Modality - Series Description]/SOPInstanceUID.dcm</pre>
	 *
	 * <p>If the destination file already exists and is identical in content, the original is removed.</p>
	 *
	 * <p>If the destination file already exists and is different in content, it is not overwritten, and
	 * the duplicate is moved into a separate duplicatesFolderNamePrefix_n folder.</p>
	 *
	 * @param	file						the DICOM file
	 * @param	hierarchicalFolderName		where to store the renamed file
	 * @param	duplicatesFolderNamePrefix	where to store the renamed file if it is a non-identical duplicate of the existing file
	 * @return								the path to the new file if successful, null if not (e.g., if not a DICOM file)
	 * @throws	IOException					if an error occurs renaming the files
	 * @throws	DicomException				if there is an error parsing the attribute list
	 * @throws	NoSuchAlgorithmException	if there is an error checking duplicate files contain identical content caused by absence of a hash algorithm
	 */
	static public String renameFileWithHierarchicalPathFromAttributes(File file,String hierarchicalFolderName,String duplicatesFolderNamePrefix) throws IOException, DicomException, NoSuchAlgorithmException {
		String newFileName = null;
		if (DicomFileUtilities.isDicomOrAcrNemaFile(file)) {
			AttributeList list = new AttributeList();
			list.read(file,terminateAfterRelationshipGroup);
			newFileName = renameFileWithHierarchicalPathFromAttributes(file,list,hierarchicalFolderName,duplicatesFolderNamePrefix);
		}
		else {
			System.err.println("\""+file+"\": not a DICOM file - doing nothing");
		}
		return newFileName;
	}

	/**
	 * <p>Rename a DICOM file into a folder hierarchy based on its attributes.</p>
	 *
	 * <p>Creates a folder structure in the current working directory of the form:</p>
	 *
	 * <pre>Sorted/PatientName [PatientID]/StudyDate StudyTime [StudyID - StudyDescription]/Series SeriesNumber [Modality - Series Description]/SOPInstanceUID.dcm</pre>
	 *
	 * <p>If the destination file already exists and is identical in content, the original is removed.</p>
	 *
	 * <p>If the destination file already exists and is different in content, it is not overwritten, and
	 * the duplicate is moved into a separate Duplicates_n folder.</p>
	 *
	 * @param	file						the DICOM file
	 * @return								the path to the new file if successful, null if not (e.g., if not a DICOM file)
	 * @throws	IOException					if an error occurs renaming the files
	 * @throws	DicomException				if there is an error parsing the attribute list
	 * @throws	NoSuchAlgorithmException	if there is an error checking duplicate files contain identical content caused by absence of a hash algorithm
	 */
	static public String renameFileWithHierarchicalPathFromAttributes(File file) throws IOException, DicomException, NoSuchAlgorithmException {
		return renameFileWithHierarchicalPathFromAttributes(file,defaultHierarchicalFolderName,defaultDuplicatesFolderNamePrefix);
	}
	
	/**
	 * <p>Rename a DICOM file into a folder hierarchy based on its attributes.</p>
	 *
	 * <p>Creates a folder structure in the current working directory of the form:</p>
	 *
	 * <pre>Sorted/PatientName [PatientID]/StudyDate StudyTime [StudyID - StudyDescription]/Series SeriesNumber [Modality - Series Description]/SOPInstanceUID.dcm</pre>
	 *
	 * <p>If the destination file already exists and is identical in content, the original is removed.</p>
	 *
	 * <p>If the destination file already exists and is different in content, it is not overwritten, and
	 * the duplicate is moved into a separate Duplicates_n folder.</p>
	 *
	 * @param	fileName					the DICOM file
	 * @return								the path to the new file if successful, null if not (e.g., if not a DICOM file)
	 * @throws	IOException					if an error occurs renaming the files
	 * @throws	DicomException				if there is an error parsing the attribute list
	 * @throws	NoSuchAlgorithmException	if there is an error checking duplicate files contain identical content caused by absence of a hash algorithm
	 */
	static public String renameFileWithHierarchicalPathFromAttributes(String fileName) throws IOException, DicomException, NoSuchAlgorithmException {
		return renameFileWithHierarchicalPathFromAttributes(new File(fileName),defaultHierarchicalFolderName,defaultDuplicatesFolderNamePrefix);
	}
	
	/**
	 * <p>Recursively search the supplied paths for DICOM files and move them into a folder hierarchy based on their attributes.</p>
	 *
	 * <p>Creates a folder structure in the current working directory of the form:</p>
	 *
	 * <pre>Sorted/PatientName [PatientID]/StudyDate StudyTime [StudyID - StudyDescription]/Series SeriesNumber [Modality - Series Description]/SOPInstanceUID.dcm</pre>
	 *
	 * <p>If the destination file already exists and is identical in content, the original is removed.</p>
	 *
	 * <p>If the destination file already exists and is different in content, it is not overwritten, and the duplicate is moved into a separate Duplicates_n folder.</p>
	 *
	 * @param	arg	array of one or more file or directory names
	 */
	public static void main(String[] arg) {
		try {
			for (int i=0; i<arg.length; ++i) {
				processFilesRecursively(new File(arg[i]),null);
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);	// no need to use SLF4J since command line utility/test
		}
	}	
}
