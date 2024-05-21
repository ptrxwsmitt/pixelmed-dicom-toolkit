/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.*;
import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;
import com.pixelmed.utils.MessageLogger;
import com.pixelmed.utils.PrintStreamMessageLogger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>A class containing an application for adding or replacing top level and shared multi-frame functional group attributes from a JSON summary description.</p>
 *
 * <p>The JSON file used to describe the changes is not encoded in the same format as the standard PS3.18 Annex F DICOM JSON Model,
 * since (a) it allows the data elements to be changed by keyword in addition to the data element tag, and
 * (b) it compactly specifies whether the changes are to the top level dataset ("top") or the keyword
 * of the sequence corresponding to the functional group to be changed, and
 * (c) lists attributes in the top level data set to be removed ("remove") or those to be removed recursively from within sequences ("removeall"), and
 * (d) lists options that control the process of modification.</p>
 * <p>The required format of the JSON file is a single enclosing object containing a list of objects
 * named by "remove", "options", a functional group sequence keyword or "top".</p>
 * <p>The functional group sequence keyword or "top" entries each contains either
 * a single string value,
 * an array of string values (possibly empty) (for multi-valued attributes), or
 * an array of objects (possibly empty) each of which is a sequence item consisting of a list of attributes,
 * an object that contained a list of code
 * sequence item attributes (named as cv for CodeValue, csd for CodingSchemeDesignator and cm for
 * CodeMeaning) or
 * null for an empty (type 2) attribute or sequence.</p>
 * <p>The "remove" object contains a list of keywords and null values.</p>
 * <p>The "options" object contains a list of options and boolean values. Current options are
 * ReplaceCodingSchemeIdentificationSequence (default is true) and
 * AppendToContributingEquipmentSequence (default is true)</p>
 *
 * <p>E.g.:</p>
 * <pre>
 * {
 * 	"options" : {
 * 		"AppendToContributingEquipmentSequence" : false
 * 	},
 * 	"remove" : {
 * 		"ContributingEquipmentSequence" : null
 * 	},
 * 	"removeall" : {
 * 		"FrameType" : null
 * 	},
 * 	"top" : {
 * 		"00204000" : "new value of ImageComments",
 * 		"InstitutionalDepartmentName" : "Radiology",
 * 		"ImageType" : [ "DERIVED", "PRIMARY", "DIXON", "WATER" ],
 *		"PatientBreedCodeSequence" : null,
 *		"BreedRegistrationSequence" : [
 *			{
 *				"BreedRegistrationNumber" : "1234",
 *				"BreedRegistryCodeSequence" : { "cv" : "109200", "csd" : "DCM", "cm" : "America Kennel Club" }
 *			},
 *			{
 *				"BreedRegistrationNumber" : \"5678\",
 *				"BreedRegistryCodeSequence" : { "cv" : "109202", "csd" : "DCM", "cm" : "American Canine Association" }
 *			}
 *		],
 *		"StudyID" : null,
 *		"AccessionNumber" : [],
 *		"ReferencedStudySequence" : [],
 * 		"ContentCreatorName" : "Smith^John"
 * 	},
 * 	"FrameAnatomySequence" : {
 * 		"AnatomicRegionSequence" : { "cv" : "T-A0100", "csd" : "SRT", "cm" : "Brain" },
 * 		"FrameLaterality" : "B"
 * 	},
 * 	"ParametricMapFrameTypeSequence" : {
 * 		"FrameType" : [ "DERIVED", "PRIMARY", "DIXON", "WATER" ]
 * 	},
 * 	"FrameVOILUTSequence" : {
 * 		"WindowCenter" : "0.7",
 * 		"WindowWidth" : "0.7",
 * 		"VOILUTFunction" : "LINEAR_EXACT"
 * 	}
 * }
 * </pre>
 *
 * <p>Attributes are "merged" with the existing content of a functional group sequence, if any,
 * otherwise a new functional group sequence is created.</p>
 *
 * <p>Currently only the shared functional group sequence can be updated, since non-programmatic use cases
 * for replacing the content of the per-frame functional group sequence items have not yet been identified.</p>
 *
 * @author	dclunie
 */
public class SetCharacteristicsFromSummary {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/SetCharacteristicsFromSummary.java,v 1.29 2022/01/21 19:51:12 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(SetCharacteristicsFromSummary.class);
	
	protected String ourAETitle = "OURAETITLE";
	
	protected String dstFolderName;
	
	protected Map<String,Boolean> options = new HashMap<String,Boolean>();
	
	protected Set<AttributeTag> topLevelRemovalList = new HashSet<AttributeTag>();
	
	protected Set<AttributeTag> recursiveRemovalList = new HashSet<AttributeTag>();
	
	protected AttributeList topLevelReplacementsList = new AttributeList();
	
	protected Map<AttributeTag,AttributeList> functionalGroupsReplacementsList = new HashMap<AttributeTag,AttributeList>();
	
	protected DicomDictionary dictionary = topLevelReplacementsList.getDictionary();
	
	// factored out and make protected so sub-classes can use a different mechanism than the standard dictionary
	protected AttributeTag getAttributeTagFromKeywordOrGroupAndElement(String name) throws DicomException {
		AttributeTag tag = null;
		if (name.length() == 8) {
			try {
				int ggggeeee = (int)(Long.parseLong(name,16));
				tag = new AttributeTag(ggggeeee>>>16,ggggeeee&0xffff);
			}
			catch (NumberFormatException e) {
				// ignore it
			}
		}
		if (tag == null) {
			tag = dictionary.getTagFromName(name);
		}
		slf4jlogger.debug("getAttributeTagFromKeywordOrGroupAndElement(): {}",tag);
		return tag;
	}
	
	protected Attribute makeNewAttribute(AttributeTag tag) throws DicomException {
		return AttributeFactory.newAttribute(tag);
	}
	
	protected Attribute makeNewStringAttribute(AttributeTag tag) throws DicomException {	 // (001295)
		Attribute a = AttributeFactory.newAttribute(tag);
		if (a instanceof UnknownAttribute) {
			a = new LongStringAttribute(tag);	// this is not ideal, but no way to do better unless we (a) have a private dictionary, or (b) add VR to the JSON input
		}
		return a;
	}

	protected Attribute makeNewSequenceAttribute(AttributeTag tag) throws DicomException {	 // (001295)
		Attribute a = AttributeFactory.newAttribute(tag);
		if (a instanceof UnknownAttribute) {
			a = new SequenceAttribute(tag);
		}
		return a;
	}

	protected Attribute parseAttributeFromJSON(JSONObject obj, String name) throws DicomException {
		Attribute a = null;		// lazy instantiation - wait until we know what class of VR to use in case not in dictionary (e.g., private) (001295)
		AttributeTag tag = getAttributeTagFromKeywordOrGroupAndElement(name);
		if (tag == null) {	// (001143)
			throw new DicomException("Unrecognized data element keyword for attribute: "+name);
		}
		Object entry = obj.get(name);
		if (entry instanceof String entryStr) {		// single valued attribute
			slf4jlogger.debug("parseAttributeFromJSON(): "+name+" : "+entryStr);
			if (entryStr != null && entryStr.length() > 0) {	// (001157)
				a = makeNewStringAttribute(tag);		// (001295)
				a.addValue(entryStr);
			}
			// if empty don't know VR to use for unknown attribute, fall through (001295)
		} else if (entry instanceof JSONObject entryObj) {	// coded sequence item
			a = makeNewSequenceAttribute(tag);	 // (001295)
			if (!entryObj.isNull("cv")) {
				String codeValue = entryObj.getString("cv");
				String codingSchemeDesignator = entryObj.getString("csd");
				String codeMeaning = entryObj.getString("cm");
				((SequenceAttribute)a).addItem(new CodedSequenceItem(codeValue,codingSchemeDesignator,codeMeaning).getAttributeList());
			}
			// else leave newly created attribute empty (presumably is Type 2)
		} else if (entry instanceof JSONArray arrayOfValues) {	// multi valued attribute or multiple sequence items
			for (Object arrayEntry : arrayOfValues) {
				if (arrayEntry instanceof String arrayEntryStr) {
					if (a == null) {
						a = makeNewStringAttribute(tag);	 // (001295)
					}
					a.addValue(arrayEntryStr);
				} else if (arrayEntry instanceof JSONObject arrayEntryObj) {	// sequence item
					AttributeList itemList = new AttributeList();
					parseAttributesFromJSON(arrayEntryObj,itemList);	// recursive, so may be nested
					processAttributeListAfterReplacements(itemList);			// in case sub-class needs to add private creator(s)
					if (a == null) {
						a = makeNewSequenceAttribute(tag);	 // (001295)
					}
					((SequenceAttribute)a).addItem(itemList);
				}
			}
			// if empty don't know VR to use for unknown attribute, fall through (001295)
		}
		if (a == null) {	 // (001295)
			a = makeNewAttribute(tag);
			slf4jlogger.debug("parseAttributeFromJSON(): falling through for empty attribute {}",a);
		}
		slf4jlogger.debug("parseAttributeFromJSON(): {}",a);
		return a;
	}
	
	protected void parseAttributesFromJSON(JSONObject functionalGroupEntries,AttributeList list) throws DicomException {
		for (String name : functionalGroupEntries.keySet()) {
			Attribute a = parseAttributeFromJSON(functionalGroupEntries,name);
			list.put(a);
		}
	}
	
	protected void parseAttributeTagsFromJSON(JSONObject entries,Set<AttributeTag> tags) throws DicomException {
		for (String name : entries.keySet()) {
			AttributeTag t = getAttributeTagFromKeywordOrGroupAndElement(name);
			tags.add(t);
		}
	}
	
	protected void parseOptionsFromJSON(JSONObject entries) throws DicomException {
		slf4jlogger.debug("parseOptionsFromJSON():");
		for (String name : entries.keySet()) {
			Object entry = entries.get(name);
			if(entry instanceof Boolean optionBooleanValue) {
				slf4jlogger.debug("parseOptionsFromJSON(): {}} : {}",name,optionBooleanValue);
				options.put(name,optionBooleanValue);
			} else {
				throw new DicomException("Unexpected valueType "+(entry!=null?entry.getClass():null)+" in options for "+name);
			}
		}
	}
	
	protected void parseSummaryFile(String jsonfile) throws DicomException, IOException {
			final String jsonString = Files.readString(Paths.get(jsonfile));
			final JSONObject obj = new JSONObject(jsonString);
			for (String functionalGroupName : obj.keySet()) {
				final Object functionalGroup = obj.get(functionalGroupName);
				if(functionalGroup instanceof JSONObject functionalGroupEntries) {
					if (functionalGroupName.equals("options")) {
						parseOptionsFromJSON(functionalGroupEntries);        // sets global options
					} else if (functionalGroupName.equals("remove")) {
						parseAttributeTagsFromJSON(functionalGroupEntries, topLevelRemovalList);
					} else if (functionalGroupName.equals("removeall")) {
						parseAttributeTagsFromJSON(functionalGroupEntries, recursiveRemovalList);
					} else if (functionalGroupName.equals("top")) {
						parseAttributesFromJSON(functionalGroupEntries, topLevelReplacementsList);
					} else {
						AttributeTag functionalGroupTag = dictionary.getTagFromName(functionalGroupName);
						AttributeList list = functionalGroupsReplacementsList.get(functionalGroupTag);
						if (list == null) {
							list = new AttributeList();
							functionalGroupsReplacementsList.put(functionalGroupTag, list);
						}
						parseAttributesFromJSON(functionalGroupEntries, list);
					}
				} else {
					throw new DicomException("Unexpected valueType "+(functionalGroup!=null?functionalGroup.getClass():null)+" for "+functionalGroupName);
				}
			}
    }
	
	// in case sub classes want to do stuff, e.g., add private creators
	protected void processAttributeListAfterReplacements(AttributeList list) throws DicomException {
	}
	
	protected void performReplacements(AttributeList list) throws DicomException {
		for (AttributeTag tag : topLevelRemovalList) {
			list.remove(tag);
		}
		
		for (AttributeTag tag : recursiveRemovalList) {
			list.removeRecursively(tag);
		}
		
		list.putAll(topLevelReplacementsList);
		
		if (functionalGroupsReplacementsList.size() > 0) {
			AttributeList sharedList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(list,TagFromName.SharedFunctionalGroupsSequence);
			if (sharedList == null) {
				sharedList = new AttributeList();
				SequenceAttribute a = new SequenceAttribute(TagFromName.SharedFunctionalGroupsSequence);
				a.addItem(sharedList);
				list.put(a);
			}
			for (AttributeTag functionalGroupSequenceTag : functionalGroupsReplacementsList.keySet()) {
				SequenceAttribute functionalGroupSequenceAttribute = (SequenceAttribute)(sharedList.get(functionalGroupSequenceTag));
				if (functionalGroupSequenceAttribute == null) {
					functionalGroupSequenceAttribute = new SequenceAttribute(functionalGroupSequenceTag);
					sharedList.put(functionalGroupSequenceAttribute);
				}
				AttributeList functionalGroupSequenceList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(functionalGroupSequenceAttribute);
				if (functionalGroupSequenceList == null) {
					functionalGroupSequenceList = new AttributeList();
					functionalGroupSequenceAttribute.addItem(functionalGroupSequenceList);
				}
				functionalGroupSequenceList.putAll(functionalGroupsReplacementsList.get(functionalGroupSequenceTag));
			}
		}
		
		{
			Boolean appendToContributingEquipmentSequence = options.get("AppendToContributingEquipmentSequence");
			if (appendToContributingEquipmentSequence == null	// default is true
				|| appendToContributingEquipmentSequence.booleanValue()) {
				slf4jlogger.debug("performReplacements(): calling addContributingEquipmentSequence()");
				ClinicalTrialsAttributes.addContributingEquipmentSequence(list,true,new CodedSequenceItem("109103","DCM","Modifying Equipment"),
																		  "PixelMed",													// Manufacturer
																		  "PixelMed",													// Institution Name
																		  "Software Development",										// Institutional Department Name
																		  "Bangor, PA",													// Institution Address
																		  null,															// Station Name
																		  "com.pixelmed.apps.SetCharacteristicsFromSummary",			// Manufacturer's Model Name
																		  null,															// Device Serial Number
																		  "Vers. "+VersionAndConstants.getBuildDate(),					// Software Version(s)
																		  "Set characteristics from summary");
			}
			// else does NOT remove any exist ContributingEquipmentSequence ... use explicit remove in JSON if necessary
		}
		
		{
			Boolean replaceCodingSchemeIdentificationSequence = options.get("ReplaceCodingSchemeIdentificationSequence");
			if (replaceCodingSchemeIdentificationSequence == null	// default is true
				|| replaceCodingSchemeIdentificationSequence.booleanValue()) {
				slf4jlogger.debug("performReplacements(): calling replaceCodingSchemeIdentificationSequenceWithCodingSchemesUsedInAttributeList()");
				CodingSchemeIdentification.replaceCodingSchemeIdentificationSequenceWithCodingSchemesUsedInAttributeList(list);
			}
			// else does NOT remove any exist CodingSchemeIdentificationSequence ... use explicit remove in JSON if necessary
		}
		
	}

	protected class OurMediaImporter extends MediaImporter {
		public OurMediaImporter(MessageLogger logger) {
			super(logger);
		}
		
		protected boolean isOKToImport(String sopClassUID,String transferSyntaxUID) {
			return transferSyntaxUID != null;	// do NOT require sopClassUID != null, since may want to fix file with missing value (001290)
		}
		
		protected void doSomethingWithDicomFileOnMedia(String mediaFileName) {
			//logLn("OurFirstPassMediaImporter.doSomethingWithDicomFile(): "+mediaFileName);
			try {
				DicomInputStream i = new DicomInputStream(new File(mediaFileName));
				AttributeList list = new AttributeList();
				list.setDecompressPixelData(false);
				list.read(i);
				i.close();
				
				String transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.TransferSyntaxUID);
				
				performReplacements(list);

				processAttributeListAfterReplacements(list);
				
				list.removeGroupLengthAttributes();
				list.removeMetaInformationHeaderAttributes();
				list.remove(TagFromName.DataSetTrailingPadding);
				list.insertSuitableSpecificCharacterSetForAllStringValues();	// (001158)
				FileMetaInformation.addFileMetaInformation(list,transferSyntaxUID,ourAETitle);
				
				File dstFile = new File(dstFolderName,MoveDicomFilesIntoHierarchy.makeHierarchicalPathFromAttributes(list));
				if (dstFile.exists()) {
					throw new DicomException("\""+mediaFileName+"\": new file \""+dstFile+"\" already exists - not overwriting");
				}
				else {
					File dstParentDirectory = dstFile.getParentFile();
					if (!dstParentDirectory.exists()) {
						if (!dstParentDirectory.mkdirs()) {
							throw new DicomException("\""+mediaFileName+"\": parent directory creation failed for \""+dstFile+"\"");
						}
					}
					//logLn("Writing with characteristics set file "+dstFile);
					slf4jlogger.info("Writing with characteristics set file {}",dstFile);
					list.write(dstFile,transferSyntaxUID,true,true);
				}
			}
			catch (Exception e) {
				//logLn("Error: File "+mediaFileName+" exception "+e);
				slf4jlogger.error("",e);
			}
		}
	}
	
	/**
	 * <p>Update top level and shared multi-frame functional group attributes from a JSON summary description.</p>
	 *
	 * <p>Updates SpecificCharacterSet before writing new file.</p>
	 *
	 * @param	jsonfile		JSON file describing the functional groups and attributes and values to be added or replaced
	 * @param	src				source folder or DICOMDIR
	 * @param	dstFolderName	destination folder
	 * @param	logger			logger to send progress, warnings and errors
	 */
	public SetCharacteristicsFromSummary(String jsonfile,String src,String dstFolderName,MessageLogger logger) throws IOException, DicomException {
//System.err.println("SetCharacteristicsFromSummary(): dstFolderName = "+dstFolderName);
		this.dstFolderName = dstFolderName;
		parseSummaryFile(jsonfile);
		MediaImporter importer = new OurMediaImporter(logger);
		importer.importDicomFiles(src);
	}
	
	/**
	 * <p>Update top level and shared multi-frame functional group attributes from a JSON summary description.</p>
	 *
	 * <p>Updates SpecificCharacterSet before writing new file.</p>
	 *
	 * @param	jsonfile		JSON file describing the functional groups and attributes and values to be added or replaced
	 * @param	srcs			source folders or DICOMDIRs
	 * @param	dstFolderName	destination folder
	 * @param	logger			logger to send progress, warnings and errors
	 */
	public SetCharacteristicsFromSummary(String jsonfile,String[] srcs,String dstFolderName,MessageLogger logger) throws IOException, DicomException {
//System.err.println("SetCharacteristicsFromSummary(): dstFolderName = "+dstFolderName);
		this.dstFolderName = dstFolderName;
		parseSummaryFile(jsonfile);
		MediaImporter importer = new OurMediaImporter(logger);
		for (String src : srcs) {
			importer.importDicomFiles(src);
		}
	}
	
	/**
	 * <p>Update top level and shared multi-frame functional group attributes from a JSON summary description.</p>
	 *
	 * <p>Does not update SpecificCharacterSet in AttributeList.</p>
	 *
	 * @param	jsonfile		JSON file describing the functional groups and attributes and values to be added or replaced
	 * @param	list			the list to update
	 */
	public SetCharacteristicsFromSummary(String jsonfile,AttributeList list) throws IOException, DicomException {
		parseSummaryFile(jsonfile);
		performReplacements(list);
		processAttributeListAfterReplacements(list);
	}

	/**
	 * <p>Update top level and shared multi-frame functional group attributes from a JSON summary description.</p>
	 *
	 * @param	arg		array of three or more strings - a JSON file describing the functional groups and attributes and values to be added or replaced,
	 *                  followed by one or more source folders or DICOMDIR,
	 *                  and a destination folder
	 */
	public static void main(String arg[]) {
		try {
			String jsonfile = arg[0];
			String dst = arg[arg.length-1];
			if (arg.length == 3) {
				MessageLogger logger = new PrintStreamMessageLogger(System.err);
				new SetCharacteristicsFromSummary(jsonfile,arg[1],dst,logger);
			}
			else if (arg.length > 3) {
				MessageLogger logger = new PrintStreamMessageLogger(System.err);
				int nSrcs = arg.length-2;
				String[] srcs = new String[nSrcs];
				System.arraycopy(arg,1,srcs,0,nSrcs);
				new SetCharacteristicsFromSummary(jsonfile,srcs,dst,logger);
			}
			else {
				System.err.println("Usage: java -cp ./pixelmed.jar com.pixelmed.apps.SetCharacteristicsFromSummary jsonfile srcdir|DICOMDIR [srcdir|DICOMDIR]* dstdir");
			}
		}
		catch (Exception e) {
			slf4jlogger.error("",e);	// use SLF4J since may be invoked from script
			System.exit(0);
		}
	}
}

