/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.utils.DateUtilities;

import java.util.*;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>An abstract class of static methods to support removing identifying attributes and adding
 * Clinical Trials Patient, Study and Series Modules attributes.</p>
 *
 * <p>UID attributes are handled specially, in that they may be kept, removed or remapped. Remapping
 * means that any UID that is not standard (e.g., not a SOP Class, etc.) will be replaced consistently
 * with another generated UID, such that when that UID is encountered again, the same replacement
 * value will be used. The replacement mapping persists within the invocation of the JVM until it is explciitly
 * flushed. A different JVM invocation will replace the UIDs with different values. Therefore, multiple
 * instances that need to be remapped consistently must be cleaned within the same invocation.</p>
 *
 * <p>Note that this map could grow quite large and consumes resources in memory, and hence in a server
 * application should be flushed at appropriate intervals using the appropriate method.</p>
 *
 * @author	dclunie
 */
abstract public class ClinicalTrialsAttributes {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/ClinicalTrialsAttributes.java,v 1.139 2024/01/14 18:11:53 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(ClinicalTrialsAttributes.class);
	
	// these are public so that they are accessible for comparisons in tests ...
	public static final String defaultValueForMissingNonZeroLengthStrings = "NONE";
	public static final String defaultValueForMissingPossiblyZeroLengthStrings = "";
	public static final String replacementForDeviceSerialNumber = "SN000000";
	public static final String replacementForDetectorID = "DET00000";
	public static final String replacementForVerifyingObserverName = "Observer^Deidentified";
	public static final String replacementForVerifyingOrganization = "NoOrganization";	// CP 1801
	public static final String replacementForContainerIdentifier = "CNTR00000";
	public static final String replacementForSpecimenIdentifier = "SPC00000";
	public static final String replacementForDescriptionOrLabel = "REMOVED";
	public static final String replacementForTextInStructuredContent = "REMOVED";
	public static final String replacementForPersonNameInStructuredContent = "Nobody^Noone";
	public static final String replacementForDateTimeInStructuredContent = "19600101000000";
	public static final String replacementForDateInStructuredContent = "19600101";
	public static final String replacementForTimeInStructuredContent = "000000";
	public static final String replacementForContentCreatorName = "Nobody^Noone";	// CP 1801
	public static final String replacementForAgeString = "000Y";
	public static final String replacementForAET = "REMOVED";
	
	protected static final DicomDictionary dictionary = DicomDictionary.StandardDictionary;

	protected static Map mapOfOriginalToReplacementUIDs = null;
	protected static UIDGenerator uidGenerator = null;	

	private ClinicalTrialsAttributes() {};
	
	protected static void addType1LongStringAttribute(AttributeList list,AttributeTag t,String value,SpecificCharacterSet specificCharacterSet) throws DicomException {
		if (value == null || value.length() == 0) {
			value=defaultValueForMissingNonZeroLengthStrings;
		}
		Attribute a = new LongStringAttribute(t,specificCharacterSet);
		a.addValue(value);
		list.put(t,a);
	}

	protected static void addType2LongStringAttribute(AttributeList list,AttributeTag t,String value,SpecificCharacterSet specificCharacterSet) throws DicomException {
		if (value == null) {
			value=defaultValueForMissingPossiblyZeroLengthStrings;
		}
		Attribute a = new LongStringAttribute(t,specificCharacterSet);
		a.addValue(value);
		list.put(t,a);
	}

	protected static void addType3ShortTextAttribute(AttributeList list,AttributeTag t,String value,SpecificCharacterSet specificCharacterSet) throws DicomException {
		if (value != null) {
			Attribute a = new ShortTextAttribute(t,specificCharacterSet);
			a.addValue(value);
			list.put(t,a);
		}
	}

	protected static void addType3ShortStringAttribute(AttributeList list,AttributeTag t,String value,SpecificCharacterSet specificCharacterSet) throws DicomException {
		if (value != null) {
			Attribute a = new ShortStringAttribute(t,specificCharacterSet);
			a.addValue(value);
			list.put(t,a);
		}
	}

	protected static void addType3LongStringAttribute(AttributeList list,AttributeTag t,String value,SpecificCharacterSet specificCharacterSet) throws DicomException {
		if (value != null) {
			Attribute a = new LongStringAttribute(t,specificCharacterSet);
			a.addValue(value);
			list.put(t,a);
		}
	}

	protected static void addType3DateTimeAttribute(AttributeList list,AttributeTag t,String value) throws DicomException {
		if (value != null) {
			Attribute a = new DateTimeAttribute(t);
			a.addValue(value);
			list.put(t,a);
		}
	}

	/**
	 * <p>Add the Patient's Age derived from the Patient's Birth Date and study-related date.</p>
	 *
	 * <p>Does nothing if no value for Patient's Birth Date.</p>
	 *
	 * <p>Uses the Study, Series, Acquisition or Content Date in that order if present, else does nothing.</p>
	 *
	 * @param	list	the list of attributes in which to find the dob and date and to which to add the age
	 */
	public static void addAgeDerivedFromBirthDateAndStudyRelatedDate(AttributeList list) {
		String dob = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("PatientBirthDate"));
		if (dob.length() > 0) {
			String useAsCurrentDateForAge = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("StudyDate"));
			if (useAsCurrentDateForAge.length() == 0) {
				useAsCurrentDateForAge = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("SeriesDate"));
			}
			if (useAsCurrentDateForAge.length() == 0) {
				useAsCurrentDateForAge = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("AcquisitionDate"));
			}
			if (useAsCurrentDateForAge.length() == 0) {
				useAsCurrentDateForAge = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("ContentDate"));
			}
			if (useAsCurrentDateForAge.length() > 0) {
				try {
					String age = DateUtilities.getAgeBetweenAsDICOMAgeString(dob,useAsCurrentDateForAge);
					if (age != null && age.length() > 0) {
						Attribute aPatientAge = new AgeStringAttribute(dictionary.getTagFromName("PatientAge"));
						aPatientAge.addValue(age);
						list.put(aPatientAge);
					}
				}
				catch (Exception e) {
					slf4jlogger.error("While deriving age",e);
				}
			}
		}
	}

	/**
	 * <p>Add the attributes of the Contributing Equipment Sequence to a list of attributes.</p>
	 *
	 * <p>Attributes are added if supplied string value are added if not null. May be zero length.</p>
	 *
	 * <p>Retains any existing items in Contributing Equipment Sequence.</p>
	 *
	 * <p>Uses <code>("109104","DCM","De-identifying Equipment")</code> for the Purpose of Reference.</p>
	 *
	 * <p>Uses <code>"Deidentified"</code> for the Contribution Description.</p>
	 *
	 * <p>Uses the current date and time for the Contribution DateTime.</p>
	 *
	 * @param	list							the list of attributes to which to add the Contributing Equipment Sequence
	 * @param	manufacturer					the manufacturer
	 * @param	institutionName					the institution name
	 * @param	institutionalDepartmentName		the institution department name
	 * @param	institutionAddress				the institution address
	 * @param	stationName						the station name
	 * @param	manufacturerModelName			the manufacturer model name
	 * @param	deviceSerialNumber				the device serial number
	 * @param	softwareVersion					the software version
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void addContributingEquipmentSequence(AttributeList list,
			String manufacturer,
			String institutionName,
			String institutionalDepartmentName,
			String institutionAddress,
			String stationName,
			String manufacturerModelName,
			String deviceSerialNumber,
			String softwareVersion) throws DicomException {
		addContributingEquipmentSequence(list,true,new CodedSequenceItem("109104","DCM","De-identifying Equipment"),	// per CP 892
			manufacturer,institutionName,institutionalDepartmentName,institutionAddress,stationName,manufacturerModelName,deviceSerialNumber,softwareVersion,
			"Deidentified");
	}

	/**
	 * <p>Add the attributes of the Contributing Equipment Sequence to a list of attributes.</p>
	 *
	 * <p>Attributes are added if supplied string value are added if not null. May be zero length.</p>
	 *
	 * @param	list							the list of attributes to which to add the Contributing Equipment Sequence
	 * @param	retainExistingItems				if true, retain any existing items in Contributing Equipment Sequence, otherwise remove them
	 * @param	purposeOfReferenceCodeSequence	the purpose of reference
	 * @param	manufacturer					the manufacturer
	 * @param	institutionName					the institution name
	 * @param	institutionalDepartmentName		the institution department name
	 * @param	institutionAddress				the institution address
	 * @param	stationName						the station name
	 * @param	manufacturerModelName			the manufacturer model name
	 * @param	deviceSerialNumber				the device serial number
	 * @param	softwareVersion					the software version
	 * @param	contributionDescription			the contribution description
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void addContributingEquipmentSequence(AttributeList list,boolean retainExistingItems,
			CodedSequenceItem purposeOfReferenceCodeSequence,
			String manufacturer,
			String institutionName,
			String institutionalDepartmentName,
			String institutionAddress,
			String stationName,
			String manufacturerModelName,
			String deviceSerialNumber,
			String softwareVersion,
			String contributionDescription) throws DicomException {
		addContributingEquipmentSequence(list,true,purposeOfReferenceCodeSequence,
			manufacturer,institutionName,institutionalDepartmentName,institutionAddress,stationName,manufacturerModelName,deviceSerialNumber,softwareVersion,
			contributionDescription,DateTimeAttribute.getFormattedStringDefaultTimeZone(new Date()),
			null,null);
	}

	/**
	 * <p>Add the attributes of the Contributing Equipment Sequence to a list of attributes.</p>
	 *
	 * <p>Attributes are added if supplied string value are added if not null. May be zero length.</p>
	 *
	 * @param	list							the list of attributes to which to add the Contributing Equipment Sequence
	 * @param	retainExistingItems				if true, retain any existing items in Contributing Equipment Sequence, otherwise remove them
	 * @param	purposeOfReferenceCodeSequence	the purpose of reference
	 * @param	manufacturer					the manufacturer
	 * @param	institutionName					the institution name
	 * @param	institutionalDepartmentName		the institution department name
	 * @param	institutionAddress				the institution address
	 * @param	stationName						the station name
	 * @param	manufacturerModelName			the manufacturer model name
	 * @param	deviceSerialNumber				the device serial number
	 * @param	softwareVersion					the software version
	 * @param	contributionDescription			the contribution description
	 * @param	contributionDateTime			the contribution datetime
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void addContributingEquipmentSequence(AttributeList list,boolean retainExistingItems,
			CodedSequenceItem purposeOfReferenceCodeSequence,
			String manufacturer,
			String institutionName,
			String institutionalDepartmentName,
			String institutionAddress,
			String stationName,
			String manufacturerModelName,
			String deviceSerialNumber,
			String softwareVersion,
			String contributionDescription,
			String contributionDateTime) throws DicomException {
		addContributingEquipmentSequence(list,true,purposeOfReferenceCodeSequence,
			manufacturer,institutionName,institutionalDepartmentName,institutionAddress,stationName,manufacturerModelName,deviceSerialNumber,softwareVersion,
			contributionDescription,contributionDateTime,
			null,null);
	}
	
	/**
	 * <p>Add the attributes of the Contributing Equipment Sequence to a list of attributes.</p>
	 *
	 * <p>Attributes are added if supplied string value are added if not null. May be zero length.</p>
	 *
	 * @param	list							the list of attributes to which to add the Contributing Equipment Sequence
	 * @param	retainExistingItems				if true, retain any existing items in Contributing Equipment Sequence, otherwise remove them
	 * @param	purposeOfReferenceCodeSequence	the purpose of reference
	 * @param	manufacturer					the manufacturer
	 * @param	institutionName					the institution name
	 * @param	institutionalDepartmentName		the institution department name
	 * @param	institutionAddress				the institution address
	 * @param	stationName						the station name
	 * @param	manufacturerModelName			the manufacturer model name
	 * @param	deviceSerialNumber				the device serial number
	 * @param	softwareVersion					the software version
	 * @param	contributionDescription			the contribution description
	 * @param	contributionDateTime			the contribution datetime
	 * @param	operatorNames					an array of Strings of one or more operator's names, or null if not to be added
	 * @param	operatorIdentifications			an array of {@link PersonIdentification PersonIdentification}, or null if not to be added
	 * @throws	DicomException					if error in DICOM encoding
	 */
	public static void addContributingEquipmentSequence(AttributeList list,boolean retainExistingItems,
			CodedSequenceItem purposeOfReferenceCodeSequence,
			String manufacturer,
			String institutionName,
			String institutionalDepartmentName,
			String institutionAddress,
			String stationName,
			String manufacturerModelName,
			String deviceSerialNumber,
			String softwareVersion,
			String contributionDescription,
			String contributionDateTime,
			String[] operatorNames,
			PersonIdentification[] operatorIdentifications) throws DicomException {
		addContributingEquipmentSequence(list,retainExistingItems,
			purposeOfReferenceCodeSequence,
			manufacturer,
			institutionName,
			institutionalDepartmentName,
			null/*institutionalDepartmentTypeCodeSequence*/,
			institutionAddress,
			stationName,
			manufacturerModelName,
			deviceSerialNumber,
			softwareVersion,
			contributionDescription,
			contributionDateTime,
			operatorNames,
			operatorIdentifications);
	}
	
	/**
	 * <p>Add the attributes of the Contributing Equipment Sequence to a list of attributes.</p>
	 *
	 * <p>Attributes are added if supplied string value are added if not null. May be zero length.</p>
	 *
	 * @param	list									the list of attributes to which to add the Contributing Equipment Sequence
	 * @param	retainExistingItems						if true, retain any existing items in Contributing Equipment Sequence, otherwise remove them
	 * @param	purposeOfReferenceCodeSequence			the purpose of reference
	 * @param	manufacturer							the manufacturer
	 * @param	institutionName							the institution name
	 * @param	institutionalDepartmentName				the institution department name
	 * @param	institutionalDepartmentTypeCodeSequence	the institution department type
	 * @param	institutionAddress						the institution address
	 * @param	stationName								the station name
	 * @param	manufacturerModelName					the manufacturer model name
	 * @param	deviceSerialNumber						the device serial number
	 * @param	softwareVersion							the software version
	 * @param	contributionDescription					the contribution description
	 * @param	contributionDateTime					the contribution datetime
	 * @param	operatorNames							an array of Strings of one or more operator's names, or null if not to be added
	 * @param	operatorIdentifications					an array of {@link PersonIdentification PersonIdentification}, or null if not to be added
	 * @throws	DicomException							if error in DICOM encoding
	 */
	public static void addContributingEquipmentSequence(AttributeList list,boolean retainExistingItems,
			CodedSequenceItem purposeOfReferenceCodeSequence,
			String manufacturer,
			String institutionName,
			String institutionalDepartmentName,
			CodedSequenceItem institutionalDepartmentTypeCodeSequence,
			String institutionAddress,
			String stationName,
			String manufacturerModelName,
			String deviceSerialNumber,
			String softwareVersion,
			String contributionDescription,
			String contributionDateTime,
			String[] operatorNames,
			PersonIdentification[] operatorIdentifications) throws DicomException {

		Attribute aSpecificCharacterSet = list.get(dictionary.getTagFromName("SpecificCharacterSet"));
		SpecificCharacterSet specificCharacterSet = aSpecificCharacterSet == null ? null : new SpecificCharacterSet(aSpecificCharacterSet.getStringValues());

		AttributeList newItemList = new AttributeList();
		
		if (purposeOfReferenceCodeSequence != null) {
			SequenceAttribute aPurposeOfReferenceCodeSequence = new SequenceAttribute(dictionary.getTagFromName("PurposeOfReferenceCodeSequence"));
			aPurposeOfReferenceCodeSequence.addItem(purposeOfReferenceCodeSequence.getAttributeList());
			newItemList.put(aPurposeOfReferenceCodeSequence);
		}
		addType3LongStringAttribute (newItemList,dictionary.getTagFromName("Manufacturer"),manufacturer,specificCharacterSet);
		addType3LongStringAttribute (newItemList,dictionary.getTagFromName("InstitutionName"),institutionName,specificCharacterSet);
		addType3LongStringAttribute (newItemList,dictionary.getTagFromName("InstitutionalDepartmentName"),institutionalDepartmentName,specificCharacterSet);
		if (institutionalDepartmentTypeCodeSequence != null) {
			SequenceAttribute aInstitutionalDepartmentTypeCodeSequence = new SequenceAttribute(dictionary.getTagFromName("InstitutionalDepartmentTypeCodeSequence"));
			aInstitutionalDepartmentTypeCodeSequence.addItem(institutionalDepartmentTypeCodeSequence.getAttributeList());
			newItemList.put(aInstitutionalDepartmentTypeCodeSequence);
		}
		addType3ShortTextAttribute  (newItemList,dictionary.getTagFromName("InstitutionAddress"),institutionAddress,specificCharacterSet);
		addType3ShortStringAttribute(newItemList,dictionary.getTagFromName("StationName"),stationName,specificCharacterSet);
		addType3LongStringAttribute (newItemList,dictionary.getTagFromName("ManufacturerModelName"),manufacturerModelName,specificCharacterSet);
		addType3LongStringAttribute (newItemList,dictionary.getTagFromName("DeviceSerialNumber"),deviceSerialNumber,specificCharacterSet);
		addType3LongStringAttribute (newItemList,dictionary.getTagFromName("SoftwareVersions"),softwareVersion,specificCharacterSet);
		addType3ShortTextAttribute  (newItemList,dictionary.getTagFromName("ContributionDescription"),contributionDescription,specificCharacterSet);
		addType3DateTimeAttribute   (newItemList,dictionary.getTagFromName("ContributionDateTime"),contributionDateTime);
		
		if (operatorNames != null && operatorNames.length > 0) {
			Attribute aOperatorName = new PersonNameAttribute(dictionary.getTagFromName("OperatorsName"));
			for (int i=0; i<operatorNames.length; ++i) {
				aOperatorName.addValue(operatorNames[i]);
			}
			newItemList.put(aOperatorName);
		}
		
		if (operatorIdentifications != null && operatorIdentifications.length > 0) {
			SequenceAttribute aOperatorIdentificationSequence = new SequenceAttribute(dictionary.getTagFromName("OperatorIdentificationSequence"));
			for (int i=0; i<operatorIdentifications.length; ++i) {
				PersonIdentification operator = operatorIdentifications[i];
				if (operator != null) {
					aOperatorIdentificationSequence.addItem(new SequenceItem(operator.getAttributeList()));
				}
			}
			newItemList.put(aOperatorIdentificationSequence);
		}
		
		SequenceAttribute aContributingEquipmentSequence = null;
		if (retainExistingItems) {
			aContributingEquipmentSequence = (SequenceAttribute)list.get(dictionary.getTagFromName("ContributingEquipmentSequence"));	// may be absent
		}
		if (aContributingEquipmentSequence == null) {
			aContributingEquipmentSequence = new SequenceAttribute(dictionary.getTagFromName("ContributingEquipmentSequence"));
		}
		aContributingEquipmentSequence.addItem(newItemList);
		list.remove(dictionary.getTagFromName("ContributingEquipmentSequence"));
		list.put(aContributingEquipmentSequence);
	}
	
	/**
	 * <p>Remove the attributes of the Clinical Trials Patient, Study and Series Modules, from a list of attributes.</p>
	 *
	 * @param	list	the list of attributes from which to remove the attributes
	 */
	public static void removeClinicalTrialsAttributes(AttributeList list) {
		list.remove(dictionary.getTagFromName("ClinicalTrialSponsorName"));
		list.remove(dictionary.getTagFromName("ClinicalTrialProtocolID"));
		list.remove(dictionary.getTagFromName("ClinicalTrialProtocolName"));
		list.remove(dictionary.getTagFromName("ClinicalTrialSiteID"));
		list.remove(dictionary.getTagFromName("ClinicalTrialSiteName"));
		list.remove(dictionary.getTagFromName("ClinicalTrialSubjectID"));
		list.remove(dictionary.getTagFromName("ClinicalTrialSubjectReadingID"));
		list.remove(dictionary.getTagFromName("ClinicalTrialTimePointID"));
		list.remove(dictionary.getTagFromName("ClinicalTrialTimePointDescription"));
		list.remove(dictionary.getTagFromName("ClinicalTrialCoordinatingCenterName"));
		list.remove(dictionary.getTagFromName("ClinicalTrialSeriesDescription"));
		list.remove(dictionary.getTagFromName("ClinicalTrialSeriesID"));
		list.remove(dictionary.getTagFromName("ClinicalTrialProtocolEthicsCommitteeName"));
		list.remove(dictionary.getTagFromName("ClinicalTrialProtocolEthicsCommitteeApprovalNumber"));
	}
	
	/**
	 * <p>Add the attributes of the Clinical Trials Patient, Study and Series Modules, to a list of attributes.</p>
	 *
	 * @param	list								the list of attributes to which to add the attributes
	 * @param	replaceConventionalAttributes		if true, use the supplied clinical trials attributes in place of the conventional ID attributes as well
	 * @param	clinicalTrialSponsorName			the sponsor name
	 * @param	clinicalTrialProtocolID				the protocol ID
	 * @param	clinicalTrialProtocolName			the protocol name
	 * @param	clinicalTrialSiteID					the site ID
	 * @param	clinicalTrialSiteName				the site name
	 * @param	clinicalTrialSubjectID				the subject ID
	 * @param	clinicalTrialSubjectReadingID		the subject reading ID
	 * @param	clinicalTrialTimePointID			the time point ID
	 * @param	clinicalTrialTimePointDescription	the time point description
	 * @param	clinicalTrialCoordinatingCenterName	the coordinating center name
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void addClinicalTrialsAttributes(AttributeList list,boolean replaceConventionalAttributes,
			String clinicalTrialSponsorName,
			String clinicalTrialProtocolID,
			String clinicalTrialProtocolName,
			String clinicalTrialSiteID,
			String clinicalTrialSiteName,
			String clinicalTrialSubjectID,
			String clinicalTrialSubjectReadingID,
			String clinicalTrialTimePointID,
			String clinicalTrialTimePointDescription,
			String clinicalTrialCoordinatingCenterName) throws DicomException {
			
		Attribute aSpecificCharacterSet = list.get(dictionary.getTagFromName("SpecificCharacterSet"));
		SpecificCharacterSet specificCharacterSet = aSpecificCharacterSet == null ? null : new SpecificCharacterSet(aSpecificCharacterSet.getStringValues());
			
		// Clinical Trial Subject Module

		addType1LongStringAttribute(list,dictionary.getTagFromName("ClinicalTrialSponsorName"),clinicalTrialSponsorName,specificCharacterSet);
		addType1LongStringAttribute(list,dictionary.getTagFromName("ClinicalTrialProtocolID"),clinicalTrialProtocolID,specificCharacterSet);
		addType2LongStringAttribute(list,dictionary.getTagFromName("ClinicalTrialProtocolName"),clinicalTrialProtocolName,specificCharacterSet);
		addType2LongStringAttribute(list,dictionary.getTagFromName("ClinicalTrialSiteID"),clinicalTrialSiteID,specificCharacterSet);
		addType2LongStringAttribute(list,dictionary.getTagFromName("ClinicalTrialSiteName"),clinicalTrialSiteName,specificCharacterSet);
		if (clinicalTrialSubjectID != null || clinicalTrialSubjectReadingID == null)	// must be one or the other present
			addType1LongStringAttribute(list,dictionary.getTagFromName("ClinicalTrialSubjectID"),clinicalTrialSubjectID,specificCharacterSet);
		if (clinicalTrialSubjectReadingID != null)
			addType1LongStringAttribute(list,dictionary.getTagFromName("ClinicalTrialSubjectReadingID"),clinicalTrialSubjectReadingID,specificCharacterSet);

		// Clinical Trial Study Module

		addType2LongStringAttribute(list,dictionary.getTagFromName("ClinicalTrialTimePointID"),clinicalTrialTimePointID,specificCharacterSet);
		addType3ShortTextAttribute(list,dictionary.getTagFromName("ClinicalTrialTimePointDescription"),clinicalTrialTimePointDescription,specificCharacterSet);

		// Clinical Trial Series Module

		addType2LongStringAttribute(list,dictionary.getTagFromName("ClinicalTrialCoordinatingCenterName"),clinicalTrialCoordinatingCenterName,specificCharacterSet);
		
		if (replaceConventionalAttributes) {
			// Use ClinicalTrialSubjectID to replace both PatientName and PatientID
			{
				String value = clinicalTrialSubjectID;
				if (value == null) value=defaultValueForMissingNonZeroLengthStrings;
				{
					//list.remove(dictionary.getTagFromName("PatientName"));
					Attribute a = new PersonNameAttribute(dictionary.getTagFromName("PatientName"),specificCharacterSet);
					a.addValue(value);
					list.put(dictionary.getTagFromName("PatientName"),a);
				}
				{
					//list.remove(dictionary.getTagFromName("PatientID"));
					Attribute a = new LongStringAttribute(dictionary.getTagFromName("PatientID"),specificCharacterSet);
					a.addValue(value);
					list.put(dictionary.getTagFromName("PatientID"),a);
				}
			}
			// Use ClinicalTrialTimePointID to replace Study ID
			{
				String value = clinicalTrialTimePointID;
				if (value == null) value=defaultValueForMissingNonZeroLengthStrings;
				{
					//list.remove(dictionary.getTagFromName("StudyID"));
					Attribute a = new ShortStringAttribute(dictionary.getTagFromName("StudyID"),specificCharacterSet);
					a.addValue(value);
					list.put(dictionary.getTagFromName("StudyID"),a);
				}
			}
		}
	}
	
	/**
	 * <p>Is a private tag safe?</p>
	 *
	 * <p>Safe private attributes are all those that are known not to contain individually identifiable information.</p>
	 *
	 * <p>Private creators are always considered safe.</p>
	 *
	 * <p>Private transient UIDs are also considered "safe", since they can then be removed/remapped based in a subsequent step.
	 *
	 * @param	tag		the tag in question
	 * @param	list	the list in which the tag is contained from which the private creator can be extracted
	 * @return			true if safe
	 */
	public static boolean isSafePrivateAttribute(AttributeTag tag,AttributeList list) {
//System.err.println("ClinicalTrialsAttributes.isSafePrivateAttribute(): checking "+tag);
		boolean safe = false;
		if (tag.isPrivateCreator()) {
			safe = true;		// keep all creators, since may need them, and are harmless (and need them to check real private tags later)
		}
		else {
			String creator = list.getPrivateCreatorString(tag);
			safe = isSafePrivateAttribute(creator,tag);
		}
//System.err.println("ClinicalTrialsAttributes.isSafePrivateAttribute(): safe="+safe);
		return safe;
	}
	
	/**
	 * <p>Is a private tag safe?</p>
	 *
	 * <p>Safe private attributes are all those that are known not to contain individually identifiable informationR.</p>
	 *
	 * <p>Private creators are always considered safe, though there is no point in calling this method for private creator tags ... use AttributeTag.isPrivateCreator() instead.</p>
	 *
	 * <p>Private transient UIDs are also considered "safe", since they can then be removed/remapped based in a subsequent step.
	 *
	 * @param	creator	the private creator of the block containing the tag
	 * @param	tag		the tag in question
	 * @return			true if safe
	 */
	public static boolean isSafePrivateAttribute(String creator,AttributeTag tag) {
//System.err.println("ClinicalTrialsAttributes.isSafePrivateAttribute(): checking "+tag+" creator "+creator);
		boolean safe = false;
		if (tag.isPrivateCreator()) {
			safe = true;
		}
		else {
			int group = tag.getGroup();
			int element = tag.getElement();
			int elementInBlock = element & 0x00ff;
			if (group == 0x7053 && creator.equals("Philips PET Private Group")) {
				if      (elementInBlock == 0x0000) {	// DS	SUV Factor - Multiplying stored pixel values by Rescale Slope then this factor results in SUVbw in g/l
					safe = true;
				}
				else if (elementInBlock == 0x0009) {	// DS	Activity Concentration Factor - Multiplying stored pixel values by Rescale Slope then this factor results in MBq/ml.
					safe = true;
				}
			}
			else if (group == 0x2005 && (creator.equals("Philips MR Imaging DD 001") || creator.equals("PHILIPS MR IMAGING DD 001"))) {
				if      (elementInBlock == 0x000d) {	// FL	Scale Intercept
					safe = true;
				}
				else if (elementInBlock == 0x000e) {	// FL	Scale Slope
					safe = true;
				}
			}
			//else if (group == 0x2001 && creator.equals("Philips Imaging DD 001")) {
			//	if      (elementInBlock == 0x0063) {	// CS	 Examination Source
			//		safe = true;
			//	}
			//}
			//else if (group == 0x200d && creator.equals("Philips US Imaging DD 017")) {
			//	if      (elementInBlock == 0x0005) {	// LO	???	"4D"
			//		safe = true;
			//	}
			//}
			//else if (group == 0x200d && creator.equals("Philips US Imaging DD 021")) {
			//	if      (elementInBlock == 0x0007) {	// LO	???	"CUSTOM_SELECTION_13"
			//		safe = true;
			//	}
			//}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 023")) {
			//	if      (elementInBlock == 0x0037) {	// DA	???	:(
			//		safe = true;
			//	}
			//	else if (elementInBlock == 0x0038) {	// TM	???	:(
			//		safe = true;
			//	}
				/*else */if (elementInBlock == 0x0045) {	// IS	???
					safe = true;
				}
			}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 033")) {
				if      (elementInBlock == 0x0000) {	// OB	??? bulk data of some kind
					safe = true;
				}
				else if (elementInBlock == 0x0001) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0002) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0003) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0004) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0005) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0006) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0007) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0008) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000d) {	// LO	Private Native Data Type
					safe = true;
				}
				else if (elementInBlock == 0x000f) {	// OB	??? bulk data of some kind
					safe = true;
				}
				else if (elementInBlock == 0x0010) {	// IS	Private Native Total Num Sample
					safe = true;
				}
				else if (elementInBlock == 0x0011) {	// IS	Native Data Sample Size
					safe = true;
				}
				else if (elementInBlock == 0x0014) {	// IS	???
					safe = true;
				}
				//else if (elementInBlock == 0x0015) {	// LO	??? "Frustum"
				//	safe = true;
				//}
				else if (elementInBlock == 0x0021) {	// IS	Private Native Data Instance Num
					safe = true;
				}
			}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 034")) {
				if      (elementInBlock == 0x0001) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0002) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0003) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0004) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0005) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0008) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0009) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000a) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000b) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000c) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000d) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000e) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000f) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0010) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0011) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0012) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0013) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0014) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0017) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0018) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x001b) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x001c) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x001d) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x001e) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x001f) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0020) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0021) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0022) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0023) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0024) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0025) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0026) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0027) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0028) {	// LO	???
					safe = true;
				}
			}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 035")) {
				if      (elementInBlock == 0x0001) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0003) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0004) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0007) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0008) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0009) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000a) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000c) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000d) {	// LO	???
					safe = true;
				}
			}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 036")) {
				if      (elementInBlock == 0x0015) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0016) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0017) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0018) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0019) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0020) {	// LO	???
					safe = true;
				}
			}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 038")) {
				if      (elementInBlock == 0x0001) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0002) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0003) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0004) {	// LO	???
					safe = true;
				}
			}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 039")) {
				if      (elementInBlock == 0x0001) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0002) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0003) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0004) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0005) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0006) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0007) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0008) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0009) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000a) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000b) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000c) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x000d) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0015) {	// LO	???
					safe = true;
				}
			}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 040")) {
				if      (elementInBlock == 0x0001) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0002) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0003) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0004) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0005) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0006) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0007) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0020) {	// LO	???
					safe = true;
				}
			}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 042")) {
				if      (elementInBlock == 0x0015) {	// IS	???
					safe = true;
				}
				else if (elementInBlock == 0x0016) {	// FD	???
					safe = true;
				}
				else if (elementInBlock == 0x0020) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0030) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0031) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0040) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0050) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0051) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0052) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0053) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0054) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0055) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0056) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0057) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0058) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0059) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x005a) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x005b) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x005c) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x005d) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x005e) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x005f) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0060) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0070) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0071) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0072) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0073) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0074) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0075) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0076) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0077) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0078) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x008c) {	// LO	???
					safe = true;
				}
			}
			//else if (group == 0x200d && creator.equals("Philips US Imaging DD 043")) {
			//	if      (elementInBlock == 0x0005) {	// SH	"ACCEPTED" ???
			//		safe = true;
			//	}
			//}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 045")) {
				if      (elementInBlock == 0x00f1) {	// SQ
					safe = true;
				}
				else if (elementInBlock == 0x00f3) {	// OB	??? bulk data of some kind
					safe = true;
				}
				else if (elementInBlock == 0x00f4) {	// SQ
					safe = true;
				}
				else if (elementInBlock == 0x00f5) {	// SQ
					safe = true;
				}
				else if (elementInBlock == 0x00f6) {	// SQ
					safe = true;
				}
				else if (elementInBlock == 0x00f8) {	// SQ
					safe = true;
				}
				else if (elementInBlock == 0x00fa) {	// CS	(e.g. "ZLib")
					safe = true;
				}
				else if (elementInBlock == 0x00fb) {	// OB	??? bulk data of some kind
					safe = true;
				}
			}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 046")) {
				if      (elementInBlock == 0x0017) {	// FD	???
					safe = true;
				}
			}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 048")) {
				if      (elementInBlock == 0x0001) {	// LO	???
					safe = true;
				}
			}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 065")) {
				if      (elementInBlock == 0x0007) {	// LO	???
					safe = true;
				}
			}
			else if (group == 0x200d && creator.equals("Philips US Imaging DD 066")) {
				if      (elementInBlock == 0x0000) {	// OB	??? bulk data of some kind
					safe = true;
				}
				else if (elementInBlock == 0x0001) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0003) {	// LO	???
					safe = true;
				}
				else if (elementInBlock == 0x0004) {	// LO	???
					safe = true;
				}
			}
			else if (group == 0x00E1 && creator.equals("ELSCINT1")) {
				if      (elementInBlock == 0x0021) {	// DS	DLP
					safe = true;
				}
				else if (elementInBlock == 0x0050) {	// DS	Acquisition Duration
					safe = true;
				}
			}
			else if (group == 0x01E1 && creator.equals("ELSCINT1")) {
				if      (elementInBlock == 0x0026) {	// CS	Phantom Type
					safe = true;
				}
			}
			else if (group == 0x01F1 && creator.equals("ELSCINT1")) {
				if      (elementInBlock == 0x0001) {	// CS	Acquisition Type
					safe = true;
				}
				else if (elementInBlock == 0x0007) {	// DS	Table Velocity
					safe = true;
				}
				else if (elementInBlock == 0x0026) {	// DS	Pitch
					safe = true;
				}
				else if (elementInBlock == 0x0027) {	// DS	Rotation Time
					safe = true;
				}
			}
			else if (group == 0x0019 && creator.equals("GEMS_ACQU_01")) {
				if      (elementInBlock == 0x0023) {	// DS	Table Speed [mm/rotation]
					safe = true;
				}
				else if (elementInBlock == 0x0024) {	// DS	Mid Scan Time [sec]
					safe = true;
				}
				else if (elementInBlock == 0x0027) {	// DS	Rotation Speed (Gantry Period)
					safe = true;
				}
				else if (elementInBlock == 0x009e) {	// LO	Internal Pulse Sequence Name
					safe = true;
				}
			}
			else if (group == 0x0025 && creator.equals("GEMS_SERS_01")) {
				if      (elementInBlock == 0x0007) {	// SL	Images In Series
					safe = true;
				}
			}
			else if (group == 0x0043 && creator.equals("GEMS_PARM_01")) {
				if      (elementInBlock == 0x0027) {	// SH	Scan Pitch Ratio in the form "n.nnn:1"
					safe = true;
				}
				else if (elementInBlock == 0x006f) {	// DS	Scanner Table Entry + Gradient Coil Selected (VM is 3 or 4)
					safe = true;
				}
			}
			else if (group == 0x0045 && creator.equals("GEMS_HELIOS_01")) {
				if      (elementInBlock == 0x0001) {	// SS	Number of Macro Rows in Detector
					safe = true;
				}
				else if (elementInBlock == 0x0002) {	// FL	Macro width at ISO Center
					safe = true;
				}
			}
			else if (group == 0x0045 && creator.equals("GEMS_SENO_02")) {
				if      (elementInBlock == 0x0006) {	// DS	Stereo angle
					safe = true;
				}
				else if (elementInBlock == 0x001B) {	// CS	Clinical View
					safe = true;
				}
				else if (elementInBlock == 0x0020) {	// DS	Estimated Anat mean
					safe = true;
				}
				else if (elementInBlock == 0x0027) {	// IS	Set Number
					safe = true;
				}
				else if (elementInBlock == 0x0029) {	// DS	Windowing parameters
					safe = true;
				}
				else if (elementInBlock == 0x002A) {	// IS	2DLocX
					safe = true;
				}
				else if (elementInBlock == 0x002B) {	// IS	2DLocY
					safe = true;
				}
				else if (elementInBlock == 0x0049) {	// DS	Radiological Thickness
					safe = true;
				}
				else if (elementInBlock == 0x0058) {	// DS	mu
					safe = true;
				}
				else if (elementInBlock == 0x0059) {	// IS	Threshold
					safe = true;
				}
				else if (elementInBlock == 0x0060) {	// IS	Breast ROI X
					safe = true;
				}
				else if (elementInBlock == 0x0061) {	// IS	Breast ROI Y
					safe = true;
				}
				else if (elementInBlock == 0x0062) {	// IS	User Window Center
					safe = true;
				}
				else if (elementInBlock == 0x0063) {	// IS	User Window Width
					safe = true;
				}
				else if (elementInBlock == 0x0064) {	// IS	Segm Threshold
					safe = true;
				}
				else if (elementInBlock == 0x0071) {	// OB	STX buffer
					safe = true;
				}
				else if (elementInBlock == 0x0072) {	// DS	Image Crop point
					safe = true;
				}
				else if (elementInBlock == 0x0090) {	// ST	Premium View beta
					safe = true;
				}
				else if (elementInBlock == 0x00A0) {	// DS	Signal Average Factor
					safe = true;
				}
				else if (elementInBlock == 0x00A1) {	// DS	Organ Dose for source images
					safe = true;
				}
				else if (elementInBlock == 0x00A2) {	// DS	Entrance dose in mGy for source images
					safe = true;
				}
				else if (elementInBlock == 0x00A4) {	// DS	Organ Dose in dGy for the complete DBT sequence
					safe = true;
				}
				else if (elementInBlock == 0x00A7) {	// LO	Reconstruction parameters
					safe = true;
				}
				else if (elementInBlock == 0x00A8) {	// DS	Entrance Dose in dGy for the complete DBT sequence
					safe = true;
				}
				else if (elementInBlock == 0x00AB) {	// DS	Cumulative Organ Dose in dGy
					safe = true;
				}
				else if (elementInBlock == 0x00AC) {	// DS	Cumulative Entrance Dose in dGy
					safe = true;
				}
				else if (elementInBlock == 0x00AD) {	// LO	Paddle Properties
					safe = true;
				}
			}
			else if (group == 0x0073 && creator.equals("GEMS_IDI_01")) {
				if      (elementInBlock == 0x0020) {	// DS	Height Map Plane Distanceg
					safe = true;
				}
				else if (elementInBlock == 0x0021) {	// DS	Height Map Plane Offset
					safe = true;
				}
				else if (elementInBlock == 0x0030) {	// OW	Height Map Plane Indices
					safe = true;
				}
				else if (elementInBlock == 0x0031) {	// OW	X Map Plane Indices
					safe = true;
				}
				else if (elementInBlock == 0x0032) {	// OW	Y Map Plane Indices
					safe = true;
				}
				else if (elementInBlock == 0x0040) {	// DS	Central Projection Detector Secondary Angle
					safe = true;
				}
				else if (elementInBlock == 0x0050) {	// DS	Detector Active Dimensions
					safe = true;
				}
			}
			else if (group == 0x0903 && creator.equals("GEIIS PACS")) {
				if      (elementInBlock == 0x0010) {	// US	Reject Image Flag
					safe = true;
				}
				else if (elementInBlock == 0x0011) {	// US	Significant Flag
					safe = true;
				}
				else if (elementInBlock == 0x0012) {	// US	Confidential Flag
					safe = true;
				}
			}
			else if ((group == 0x7e01 || group == 0x7f01) && creator.equals("HOLOGIC, Inc.")) {
				if      (elementInBlock == 0x0001) {	// LO	Codec Version
					safe = true;
				}
				else if (elementInBlock == 0x0002) {	// SH	Codec Content Type
					safe = true;
				}
				else if (elementInBlock == 0x0010) {	// SQ	High Resolution Data Sequence
					safe = true;
				}
				else if (elementInBlock == 0x0011) {	// SQ	Low Resolution Data Sequence
					safe = true;
				}
				else if (elementInBlock == 0x0012) {	// OB	Codec Content
					safe = true;
				}
			}
			else if (group == 0x0019 && creator.equals("LORAD Selenia")) {
				if      (elementInBlock == 0x0006) {	// LO	Paddle ID
					safe = true;
				}
				else if (elementInBlock == 0x0007) {	// SH	Paddle Position
					safe = true;
				}
				else if (elementInBlock == 0x0008) {	// LO	Collimation Size
					safe = true;
				}
				else if (elementInBlock == 0x0016) {	// DS	Paddle Angle
					safe = true;
				}
				else if (elementInBlock == 0x0026) {	// LO	Paddle ID Description
					safe = true;
				}
				else if (elementInBlock == 0x0027) {	// SH	Paddle Position Description
					safe = true;
				}
				else if (elementInBlock == 0x0028) {	// LO	Collimation Size Description
					safe = true;
				}
				else if (elementInBlock == 0x0029) {	// LO	AEC User Density Scale Factor Description
					safe = true;
				}
				else if (elementInBlock == 0x0030) {	// US	AEC User Density Scale Factor
					safe = true;
				}
				else if (elementInBlock == 0x0031) {	// US	AEC System Density Scale Factor
					safe = true;
				}
				else if (elementInBlock == 0x0032) {	// US	AEC Calculated mAs
					safe = true;
				}
				else if (elementInBlock == 0x0033) {	// US	AEC Auto Pixel 1
					safe = true;
				}
				else if (elementInBlock == 0x0034) {	// US	AEC Auto Pixel 2
					safe = true;
				}
				else if (elementInBlock == 0x0035) {	// US	AEC Sensor
					safe = true;
				}
				else if (elementInBlock == 0x0037) {	// LO	NPT Mode
					safe = true;
				}
				else if (elementInBlock == 0x0040) {	// DS	Skin Edge
					safe = true;
				}
				else if (elementInBlock == 0x0041) {	// DS	Exposure Index
					safe = true;
				}
				else if (elementInBlock == 0x0050) {	// DS	Display Minimum OD
					safe = true;
				}
				else if (elementInBlock == 0x0051) {	// DS	Dispaly Maximum OD
					safe = true;
				}
				else if (elementInBlock == 0x0052) {	// IS	Display Minimum Nits
					safe = true;
				}
				else if (elementInBlock == 0x0053) {	// IS	Display Maximum Nits
					safe = true;
				}
				else if (elementInBlock == 0x0060) {	// LT	Geometry Calibration
					safe = true;
				}
				else if (elementInBlock == 0x0070) {	// LO	Frame of Reference ID
					safe = true;
				}
				else if (elementInBlock == 0x0071) {	// CS	Paired Position
					safe = true;
				}
				else if (elementInBlock == 0x0080) {	// SH	Detector Image Offset
					safe = true;
				}
				else if (elementInBlock == 0x0090) {	// DS	Conventional Tomo Angle
					safe = true;
				}
			}
			else if (group == 0x0019 && creator.equals("HOLOGIC, Inc.")) {
				if      (elementInBlock == 0x0006) {	// LO	Paddle ID
					safe = true;
				}
				else if (elementInBlock == 0x0007) {	// SH	Paddle Position
					safe = true;
				}
				else if (elementInBlock == 0x0008) {	// LO	Collimation Size
					safe = true;
				}
				else if (elementInBlock == 0x0016) {	// DS	Paddle Angle
					safe = true;
				}
				else if (elementInBlock == 0x0025) {	// SH	? but always observed to be safe string like "NORMAL"
					safe = true;
				}
				else if (elementInBlock == 0x0026) {	// LO	Paddle ID Description
					safe = true;
				}
				else if (elementInBlock == 0x0027) {	// SH	Paddle Position Description
					safe = true;
				}
				else if (elementInBlock == 0x0028) {	// LO	Collimation Size Description
					safe = true;
				}
				else if (elementInBlock == 0x0029) {	// LO	AEC User Density Scale Factor Description
					safe = true;
				}
				else if (elementInBlock == 0x0030) {	// US	AEC User Density Scale Factor
					safe = true;
				}
				else if (elementInBlock == 0x0031) {	// US	AEC System Density Scale Factor
					safe = true;
				}
				else if (elementInBlock == 0x0032) {	// US	AEC Calculated mAs
					safe = true;
				}
				else if (elementInBlock == 0x0033) {	// US	AEC Auto Pixel 1
					safe = true;
				}
				else if (elementInBlock == 0x0034) {	// US	AEC Auto Pixel 2
					safe = true;
				}
				else if (elementInBlock == 0x0035) {	// US	AEC Sensor
					safe = true;
				}
				else if (elementInBlock == 0x0037) {	// LO	NPT Mode
					safe = true;
				}
				else if (elementInBlock == 0x0040) {	// DS	Skin Edge
					safe = true;
				}
				else if (elementInBlock == 0x0041) {	// DS	Exposure Index
					safe = true;
				}
				else if (elementInBlock == 0x0042) {	// IS	Exposure Index Target
					safe = true;
				}
				else if (elementInBlock == 0x0043) {	// DS	Short Index Ratio
					safe = true;
				}
				else if (elementInBlock == 0x0044) {	// DS	Scout kVp
					safe = true;
				}
				else if (elementInBlock == 0x0045) {	// IS	Scout mA
					safe = true;
				}
				else if (elementInBlock == 0x0046) {	// IS	Scout mAs
					safe = true;
				}
				else if (elementInBlock == 0x0050) {	// DS	Display Minimum OD
					safe = true;
				}
				else if (elementInBlock == 0x0051) {	// DS	Dispaly Maximum OD
					safe = true;
				}
				else if (elementInBlock == 0x0052) {	// IS	Display Minimum Nits
					safe = true;
				}
				else if (elementInBlock == 0x0053) {	// IS	Display Maximum Nits
					safe = true;
				}
				else if (elementInBlock == 0x0060) {	// LT	Geometry Calibration
					safe = true;
				}
				else if (elementInBlock == 0x0061) {	// OB	3D IP Parameters
					safe = true;
				}
				else if (elementInBlock == 0x0062) {	// LO	2D IP Parameters
					safe = true;
				}
				else if (elementInBlock == 0x0070) {	// LO	Frame of Reference ID
					safe = true;
				}
				else if (elementInBlock == 0x0071) {	// CS	Paired Position
					safe = true;
				}
				else if (elementInBlock == 0x0080) {	// SH	Detector Image Offset
					safe = true;
				}
				else if (elementInBlock == 0x0085) {	// SH	Image Source
					safe = true;
				}
				else if (elementInBlock == 0x0087) {	// LO	Marker Text (this seems to be safe, since fixed string like LCC, not operator entered free text)
					safe = true;
				}
				else if (elementInBlock == 0x0089) {	// DS	Marker Location
					safe = true;
				}
				else if (elementInBlock == 0x008A) {	// SQ	Marker Sequence
					safe = true;
				}
				else if (elementInBlock == 0x0090) {	// DS	Conventional Tomo Angle
					safe = true;
				}
				else if (elementInBlock == 0x0097) {	// SH	Markers Burned Into Image
					safe = true;
				}
				else if (elementInBlock == 0x0098) {	// LO	Grid Line Correction
					safe = true;
				}
			}
			else if (group == 0x0099 && creator.equals("NQHeader")) {
				if      (elementInBlock == 0x0001) {	// UI	Version ... is UI VR but does not seem to really be a UI at all
					safe = true;
				}
				else if (elementInBlock == 0x0004) {	// SS	ReturnCode
					safe = true;
				}
				else if (elementInBlock == 0x0005) {	// LT	ReturnMessage
					safe = true;
				}
				else if (elementInBlock == 0x0010) {	// FL	MI
					safe = true;
				}
				else if (elementInBlock == 0x0020) {	// SH	Units
					safe = true;
				}
				else if (elementInBlock == 0x0021) {	// FL	ICV
					safe = true;
				}
			}
			else if (group == 0x0199 && creator.equals("NQLeft")) {
				if (elementInBlock >= 0x0001 && elementInBlock <= 0x003a) {
					safe = true;
				}
			}
			else if (group == 0x0299 && creator.equals("NQRight")) {
				if (elementInBlock >= 0x0001 && elementInBlock <= 0x003a) {
					safe = true;
				}
			}
			else if (group == 0x0119 && creator.equals("SIEMENS Ultrasound SC2000")) {
				if (elementInBlock == 0x0000) {	// LO	Acoustic Meta Information Version
					safe = true;
				}
				else if (elementInBlock == 0x0001) {	// OB	Common Acoustic Meta Information
					safe = true;
				}
				else if (elementInBlock == 0x0002) {	// SQ	Multi Stream Sequence
					safe = true;
				}
				else if (elementInBlock == 0x0003) {	// SQ	Acoustic Data Sequence
					safe = true;
				}
				else if (elementInBlock == 0x0004) {	// OB	Per Transaction Acoustic Control Information
					safe = true;
				}
				else if (elementInBlock == 0x0005) {	// UL	Acoustic Data Offset
					safe = true;
				}
				else if (elementInBlock == 0x0006) {	// UL	Acoustic Data Length
					safe = true;
				}
				else if (elementInBlock == 0x0007) {	// UL	Footer Offset
					safe = true;
				}
				else if (elementInBlock == 0x0008) {	// UL	Footer Length
					safe = true;
				}
				else if (elementInBlock == 0x0009) {	// SS	Acoustic Stream Number
					safe = true;
				}
				else if (elementInBlock == 0x0010) {	// SH	Acoustic Stream Type
					safe = true;
				}
				else if (elementInBlock == 0x0011) {	// UN	Stage Timer Time
					safe = true;
				}
				else if (elementInBlock == 0x0012) {	// UN	Stop Watch Time
					safe = true;
				}
				else if (elementInBlock == 0x0013) {	// IS	Volume Rate
					safe = true;
				}
				else if (elementInBlock == 0x0021) {	// SH	?
					safe = true;
				}
			}
			else if (group == 0x0129 && creator.equals("SIEMENS Ultrasound SC2000")) {
				if (elementInBlock == 0x0000) {	// SQ	MPR View Sequence
					safe = true;
				}
				else if (elementInBlock == 0x0002) {	// UI	Bookmark UID ... by including as known safe allows removal/remapping of UIDs (obviously not safe if not removed/remapped)
					safe = true;
				}
				else if (elementInBlock == 0x0003) {	// UN	Plane Origin Vector
					safe = true;
				}
				else if (elementInBlock == 0x0004) {	// UN	Row Vector
					safe = true;
				}
				else if (elementInBlock == 0x0005) {	// UN	Column Vector
					safe = true;
				}
				else if (elementInBlock == 0x0006) {	// SQ	Visualization Sequence
					safe = true;
				}
				else if (elementInBlock == 0x0007) {	// UI	Bookmark UID ... by including as known safe allows removal/remapping of UIDs (obviously not safe if not removed/remapped)
					safe = true;
				}
				else if (elementInBlock == 0x0008) {	// OB	Visualization Information
					safe = true;
				}
				else if (elementInBlock == 0x0009) {	// SQ	Application State Sequence
					safe = true;
				}
				else if (elementInBlock == 0x0010) {	// OB	Application State Information
					safe = true;
				}
				else if (elementInBlock == 0x0011) {	// SQ	Referenced Bookmark Sequence
					safe = true;
				}
				else if (elementInBlock == 0x0012) {	// UI	Referenced Bookmark UID ... by including as known safe allows removal/remapping of UIDs (obviously not safe if not removed/remapped)
					safe = true;
				}
				else if (elementInBlock == 0x0020) {	// SQ	Cine Parameters Sequence
					safe = true;
				}
				else if (elementInBlock == 0x0021) {	// OB	Cine Parameters Schema
					safe = true;
				}
				else if (elementInBlock == 0x0022) {	// OB	Values of Cine Parameters
					safe = true;
				}
				else if (elementInBlock == 0x0029) {	// OB	?
					safe = true;
				}
				else if (elementInBlock == 0x0030) {	// CS	Raw Data Object Type
					safe = true;
				}
			}
			else if (group == 0x0139 && creator.equals("SIEMENS Ultrasound SC2000")) {
				if (elementInBlock == 0x0001) {	// SL	Physio Capture ROI
					safe = true;
				}
			}
			else if (group == 0x0149 && creator.equals("SIEMENS Ultrasound SC2000")) {
				if (elementInBlock == 0x0001) {	// FD	Vector of BROI Points
					safe = true;
				}
				else if (elementInBlock == 0x0002) {	// FD	Start/End Timestamps of Strip Stream
					safe = true;
				}
				else if (elementInBlock == 0x0003) {	// FD	Timestamps of Visible R-waves
					safe = true;
				}
			}
			else if (group == 0x7fd1 && (creator.equals("SIEMENS Ultrasound SC2000") || creator.equals("SIEMENS SYNGO ULTRA-SOUND TOYON DATA STREAMING"))) {
				if (elementInBlock == 0x0001) {	// OB	Acoustic Image and Footer Data
					safe = true;
				}
				else if (elementInBlock == 0x0009) {	// UI	Volume Version ID ... is UI VR but does not seem to really be a UI at all
					safe = true;
				}
				else if (elementInBlock == 0x0010) {	// OB	Volume Payload
					safe = true;
				}
				else if (elementInBlock == 0x0011) {	// OB	After Payload
					safe = true;
				}
			}
			else if (group == 0x1129 && creator.equals("Eigen Artemis")) {
				if (elementInBlock == 0x0004) {			// IS	EncoderHome
					safe = true;
				}
				else if (elementInBlock == 0x0005) {	// IS	EncoderCurrent
					safe = true;
				}
				else if (elementInBlock == 0x0006) {	// DS	NeedleGuidePoint
					safe = true;
				}
				else if (elementInBlock == 0x0007) {	// DS	TargetNeedlePoint
					safe = true;
				}
				else if (elementInBlock == 0x0008) {	// DS	ProstateSpecificAntigen
					safe = true;
				}
				else if (elementInBlock == 0x0009) {	// IS	NeedleGauge
					safe = true;
				}
				else if (elementInBlock == 0x0010) {	// DS	ThrowDistance
					safe = true;
				}
				else if (elementInBlock == 0x0011) {	// DS	NotchLength
					safe = true;
				}
				else if (elementInBlock == 0x0012) {	// DS	NeedleTipLength
					safe = true;
				}
				else if (elementInBlock == 0x0013) {	// DS	PivotDistance
					safe = true;
				}
				else if (elementInBlock == 0x0015) {	// IS	EncoderCountsPerDegree
					safe = true;
				}
				else if (elementInBlock == 0x0016) {	// DS	VoxelSize
					safe = true;
				}
				else if (elementInBlock == 0x0017) {	// DS	ImageLag
					safe = true;
				}
				else if (elementInBlock == 0x0018) {	// CS	RoomHandedness
					safe = true;
				}
				else if (elementInBlock == 0x0019) {	// DS	ProstateVolume
					safe = true;
				}
				else if (elementInBlock == 0x0020) {	// CS	VolumeMeasureMethod
					safe = true;
				}
				else if (elementInBlock == 0x0021) {	// CS	BiopsyPlan
					safe = true;
				}
				else if (elementInBlock == 0x0023) {	// IS	NumberEncoders
					safe = true;
				}
				else if (elementInBlock == 0x0024) {	// DS	EncoderTransformMatrix
					safe = true;
				}
				else if (elementInBlock == 0x0025) {	// DS	AxisOfRotation
					safe = true;
				}
				else if (elementInBlock == 0x0026) {	// DS	SweepAngle
					safe = true;
				}
				else if (elementInBlock == 0x0027) {	// DS	HomeAngle
					safe = true;
				}
				else if (elementInBlock == 0x0029) {	// DS	ProstateSemiAutoVolume
					safe = true;
				}
				else if (elementInBlock == 0x0030) {	// DS	ProstateMaualVolume
					safe = true;
				}
				else if (elementInBlock == 0x0031) {	// DS	MeasuredLengthPoints
					safe = true;
				}
				else if (elementInBlock == 0x0032) {	// DS	MeasuredWidthPoints
					safe = true;
				}
				else if (elementInBlock == 0x0033) {	// DS	MeasuredHeightPoints
					safe = true;
				}
				else if (elementInBlock == 0x0036) {	// DS	PlannedLocationPoint
					safe = true;
				}
				else if (elementInBlock == 0x0037) {	// DS	RecordedLocationPoint
					safe = true;
				}
				else if (elementInBlock == 0x0038) {	// DS	EntryLocationPoint
					safe = true;
				}
				else if (elementInBlock == 0x0039) {	// DS	TipLocationPoint
					safe = true;
				}
				else if (elementInBlock == 0x0040) {	// DS	CorBotLocationPoint
					safe = true;
				}
				else if (elementInBlock == 0x0041) {	// DS	CorTipLocationPoint
					safe = true;
				}
				else if (elementInBlock == 0x0070) {	// CS	MarkerSide
					safe = true;
				}
				else if (elementInBlock == 0x0071) {	// DS	GrabSize
					safe = true;
				}
				else if (elementInBlock == 0x0072) {	// DS	GrabROI
					safe = true;
				}
				else if (elementInBlock == 0x0073) {	// DS	Depth
					safe = true;
				}
				else if (elementInBlock == 0x0074) {	// DS	CORO
					safe = true;
				}
				else if (elementInBlock == 0x0075) {	// DS	MisAng
					safe = true;
				}
				else if (elementInBlock == 0x0076) {	// LO	ProbeOrientation
					safe = true;
				}
				else if (elementInBlock == 0x0078) {	// DS	VideoClipROI
					safe = true;
				}
				else if (elementInBlock == 0x0079) {	// DS	ROIGroupSizes
					safe = true;
				}
				else if (elementInBlock == 0x0080) {	// DS	ROIGroupsLocations
					safe = true;
				}
				else if (elementInBlock == 0x0081) {	// DS	ROIsliceThickness
					safe = true;
				}
				else if (elementInBlock == 0x0082) {	// DS	VideoToVolumeMatrix
					safe = true;
				}
			}
		}
		return safe;
	}
	
	/**
	 * <p>Is a private tag a safe sequence VR that needs to be read as SQ if UN.</p>
	 *
	 *
	 * @param	creator	the private creator of the block containing the tag
	 * @param	tag		the tag in question
	 * @return			true if a safe SQ private tag
	 */
	public static boolean isSafePrivateSequenceAttribute(String creator,AttributeTag tag) {
//System.err.println("ClinicalTrialsAttributes.isSafePrivateSequenceAttribute(): checking "+tag+" creator "+creator);
		boolean safeSequence = false;
		if (!tag.isPrivateCreator()) {
			int group = tag.getGroup();
			int element = tag.getElement();
			int elementInBlock = element & 0x00ff;
			// do NOT include (0x7e01/0x7f01,0x0010/0x0011) High/Low Resolution Data Sequence,
			// since if supplied in UN VR will be IVRLE not EVRLE encoded and parsing them as SQ will fail
			// and we have no need to recurse into them checking for unsafe private elements anyway
			if (group == 0x0019 && creator.equals("HOLOGIC, Inc.")) {
				if (elementInBlock == 0x008A) {	// SQ	Marker Sequence
					safeSequence = true;
				}
			}
			// do NOT include into (0x200d,0x00f1,"Philips US Imaging DD 045") private 3D data sequence
			// do NOT include into (0x200d,0x00f5,"Philips US Imaging DD 045") private 3D data sequence
			// since everything in it can be considered safe, and same issue with IVRLE vs. EVRLE
			//else if (group == 0x200d && creator.equals("Philips US Imaging DD 045")) {
			//	if (elementInBlock == 0x00f1) {	// SQ
			//		safeSequence = true;
			//	}
			//	else if (elementInBlock == 0x00f5) {	// SQ
			//		safeSequence = true;
			//	}
			//}
		}
		return safeSequence;
	}
	
	/**
	 * <p>Flush (remove all entries in) the map of original UIDs to replacement UIDs.</p>
	 */
	public static void flushMapOfUIDs() {
		mapOfOriginalToReplacementUIDs = null;
	}
	
	/**
	 * <p>Get the map of original UIDs to replacement UIDs.</p>
	 */
	public static Map<String,String> getMapOfOriginalToReplacementUIDs() {
		return mapOfOriginalToReplacementUIDs;
	}

	public class HandleUIDs {
		public static final int keep = 0;
		public static final int remove = 1;
		public static final int remap = 2;
	}

	/**
	 * <p>Remap UID attributes in a list of attributes, recursively iterating through nested sequences.</p>
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void remapUIDAttributes(AttributeList list) throws DicomException {
		removeOrRemapUIDAttributes(list,HandleUIDs.remap);
	}
	
	/**
	 * <p>Remove UID attributes in a list of attributes, recursively iterating through nested sequences.</p>
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeUIDAttributes(AttributeList list) throws DicomException {
		removeOrRemapUIDAttributes(list,HandleUIDs.remove);
	}
	
	/**
	 * <p>Remove or remap UID attributes in a list of attributes, recursively iterating through nested sequences.</p>
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @param	handleUIDs	remove or remap the UIDs
	 * @throws	DicomException	if error in DICOM encoding
	 */
	protected static void removeOrRemapUIDAttributes(AttributeList list,int handleUIDs) throws DicomException {
		// iterate through list to remove all UIDs, and recursively iterate through any sequences ...
		LinkedList forRemovalOrRemapping = null;
		Iterator i = list.values().iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (o instanceof SequenceAttribute) {
				SequenceAttribute a = (SequenceAttribute)o;
				Iterator items = a.iterator();
				if (items != null) {
					while (items.hasNext()) {
						SequenceItem item = (SequenceItem)(items.next());
						if (item != null) {
							AttributeList itemAttributeList = item.getAttributeList();
							if (itemAttributeList != null) {
								removeOrRemapUIDAttributes(itemAttributeList,handleUIDs);
							}
						}
					}
				}
			}
			else if (handleUIDs != HandleUIDs.keep && o instanceof UniqueIdentifierAttribute) {
				// remove all UIDs except those that are not instance-related
				UniqueIdentifierAttribute a = (UniqueIdentifierAttribute)o;
				AttributeTag tag = a.getTag();
//if (tag.equals(dictionary.getTagFromName("SOPInstanceUID"))) { System.err.println("ClinicalTrialsAttributes.removeOrRemapUIDAttributes(): encountered SOP Instance UID"); }
				if (UniqueIdentifierAttribute.isTransient(tag,list)) {
					if (forRemovalOrRemapping == null) {
						forRemovalOrRemapping = new LinkedList();
					}
					forRemovalOrRemapping.add(tag);
//if (tag.equals(dictionary.getTagFromName("SOPInstanceUID"))) { System.err.println("ClinicalTrialsAttributes.removeOrRemapUIDAttributes(): added SOP Instance UID to list"); }
				}
			}
		}
		if (forRemovalOrRemapping != null) {
			Iterator i2 = forRemovalOrRemapping.iterator();
			while (i2.hasNext()) {
				AttributeTag tag = (AttributeTag)(i2.next());
				if (handleUIDs == HandleUIDs.remove) {
					list.remove(tag);
				}
				else if (handleUIDs == HandleUIDs.remap) {
					String originalUIDValue = Attribute.getSingleStringValueOrNull(list,tag);
//if (tag.equals(dictionary.getTagFromName("SOPInstanceUID"))) { System.err.println("ClinicalTrialsAttributes.removeOrRemapUIDAttributes(): requesting replacement of SOP Instance UID "+originalUIDValue); }
					if (originalUIDValue != null) {
						String replacementUIDValue = null;
						if (mapOfOriginalToReplacementUIDs == null) {
							mapOfOriginalToReplacementUIDs = new HashMap();
						}
						replacementUIDValue = (String)(mapOfOriginalToReplacementUIDs.get(originalUIDValue));
						if (replacementUIDValue == null) {
							if (uidGenerator == null) {
								uidGenerator = new UIDGenerator();
							}
							replacementUIDValue = uidGenerator.getAnotherNewUID();
							mapOfOriginalToReplacementUIDs.put(originalUIDValue,replacementUIDValue);
						}
						assert replacementUIDValue != null;
						list.remove(tag);
						Attribute a = new UniqueIdentifierAttribute(tag);
						a.addValue(replacementUIDValue);
						list.put(tag,a);
//if (tag.equals(dictionary.getTagFromName("SOPInstanceUID"))) { System.err.println("ClinicalTrialsAttributes.removeOrRemapUIDAttributes(): replacing SOP Instance UID "+originalUIDValue+" with "+replacementUIDValue); }
					}
					else {
						// we have a problem ... just remove it to be safe
						list.remove(tag);
					}
				}
			}
		}
	}
	
	public class HandleDates {
		public static final int keep = 0;
		public static final int remove = 1;
		public static final int modify = 2;
	}
	
	/**
	 * <p>Get the Time Attribute corresponding to the Date Attribute.</p>
	 *
	 * @param	dateTag
	 * @return									the AttributeTag of the corresponding Time Attribute, if any, otherwise null
	 */
	public static AttributeTag getTagOfTimeAttributeCorrespondingToDateAttribute(AttributeTag dateTag) {
		AttributeTag timeTag = null;
		if (dateTag != null && !dateTag.equals(dictionary.getTagFromName("Date"))) {		// do not match generic Date as used in structured content since that is handled at content item level
			String dateName = dictionary.getNameFromTag(dateTag);
			if (dateName != null) {
				String timeName = dateName.replace("Date","Time");
				if (!timeName.equals(dateName)) {
					timeTag = dictionary.getTagFromName(timeName);
				}
			}
		}		
		return timeTag;
	}
	
	protected static boolean isValidCandidateForEarliestDateTime(Date candidate) {
		return candidate != null && candidate.getTime() >= 0;	// exclude ridiculous dates that are obvious dummy values (e.g., Applicare Presentation States with Annotation Creation Date of 18991230)
	}
	
	/**
	 * <p>Get the earliest patient event related date and time.</p>
	 *
	 * <p>Ignores equipment related dates like calibration, patient related dates like birth date, and non-patient instance related dates like effective and information issue.</p>
	 *
	 * @param	list							the list of attributes from the top level data set
	 * @param	earliestSoFar					for recursion
	 * @param	dateToUseForUnaccompaniedTimes	the date to use for time attributes that don't have a date sibling
	 * @return									the earliest date and time
	 */
	protected static Date findEarliestDateTime(AttributeList list,Date earliestSoFar,String dateToUseForUnaccompaniedTimes) {
//System.err.println("ClinicalTrialsAttributes.findEarliestDateTime(): Start with earliestSoFar "+earliestSoFar);
		// iterate through list to process all Date, Time and DateTime attributes, and recursively iterate through any sequences ...
		Iterator<Attribute> i = list.values().iterator();
		while (i.hasNext()) {
			Attribute o = i.next();
			if (o instanceof SequenceAttribute) {
				SequenceAttribute a = (SequenceAttribute)o;
				Iterator items = a.iterator();
				if (items != null) {
					while (items.hasNext()) {
						SequenceItem item = (SequenceItem)(items.next());
						if (item != null) {
							AttributeList itemAttributeList = item.getAttributeList();
							if (itemAttributeList != null) {
								Date candidate = findEarliestDateTime(itemAttributeList,earliestSoFar,dateToUseForUnaccompaniedTimes);
								if (isValidCandidateForEarliestDateTime(candidate) && (earliestSoFar == null || candidate.before(earliestSoFar))) {
									earliestSoFar = candidate;
								}
							}
						}
					}
				}
			}
			else {
				String dateString = null;
				AttributeTag tag = o.getTag();
				if (o instanceof DateTimeAttribute
				   && !tag.equals(dictionary.getTagFromName("HangingProtocolCreationDateTime"))		// exclude non-event related dates
				   && !tag.equals(dictionary.getTagFromName("EffectiveDateTime"))
				   && !tag.equals(dictionary.getTagFromName("InformationIssueDateTime"))
				) {
					dateString = ((DateTimeAttribute)o).getSingleStringValueOrEmptyString();
				}
				else if (o instanceof DateAttribute
						&& !tag.equals(dictionary.getTagFromName("PatientBirthDate"))				// exclude non-event related dates
						&& !tag.equals(dictionary.getTagFromName("LastMenstrualDate"))				// is sort of event related, but not desirable as the earliest
						&& !tag.equals(dictionary.getTagFromName("DateOfLastCalibration"))
						&& !tag.equals(dictionary.getTagFromName("DateOfLastDetectorCalibration"))
						&& !tag.equals(dictionary.getTagFromName("DateOfInstallation"))				// CP 2258
						&& !tag.equals(dictionary.getTagFromName("DateOfManufacture"))				// CP 2258
					) {
					dateString = ((DateAttribute)o).getSingleStringValueOrEmptyString();
					if (dateString.length() == 8) {
						AttributeTag timeTag = getTagOfTimeAttributeCorrespondingToDateAttribute(tag);
						if (timeTag != null) {
							String timeString = Attribute.getSingleStringValueOrEmptyString(list,timeTag);
							if (timeString.length() > 0) {
								dateString = dateString + timeString;
							}
						}
					}
					else {
						dateString = null;
					}
				}
				else if (o instanceof TimeAttribute && o.getVL() > 0) {
					// need to handle time only attributes that have values and are relative to some other date (such as AcquisitionDate (NM) or SeriesDate (PET) or unspecified)
					// very important that this list match exactly what is used in removeOrRemapDateAndTimeAttributes, otherwise risk crossing midnight
					if (tag.equals(dictionary.getTagFromName("RadiopharmaceuticalStartTime"))
					 || tag.equals(dictionary.getTagFromName("RadiopharmaceuticalStopTime"))
					 || tag.equals(dictionary.getTagFromName("InterventionDrugStartTime"))
					 || tag.equals(dictionary.getTagFromName("InterventionDrugStopTime"))
					 || tag.equals(dictionary.getTagFromName("ContrastBolusStartTime"))
					 || tag.equals(dictionary.getTagFromName("ContrastBolusStopTime"))) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): "+o);
						if (dateToUseForUnaccompaniedTimes != null && dateToUseForUnaccompaniedTimes.length() == 8) {
							dateString = dateToUseForUnaccompaniedTimes + o.getSingleStringValueOrEmptyString();
						}
					}
				}
				
				if (dateString != null && dateString.length() >= 8) {
//System.err.println("ClinicalTrialsAttributes.findEarliestDateTime(): Attribute "+o);
//System.err.println("ClinicalTrialsAttributes.findEarliestDateTime(): Checking dateString "+dateString);
					try {
						Date candidate = DateTimeAttribute.getDateFromFormattedString(dateString);
						if (isValidCandidateForEarliestDateTime(candidate) && (earliestSoFar == null || candidate.before(earliestSoFar))) {
							earliestSoFar = candidate;
//System.err.println("ClinicalTrialsAttributes.findEarliestDateTime(): Checking earliestSoFar updated to "+earliestSoFar);
						}
					}
					catch (java.text.ParseException Exception) {
					}
				}
			}
		}
		return earliestSoFar;
	}

	/**
	 * <p>Get the earliest patient event related date and time.</p>
	 *
	 * <p>Ignores equipment related dates like calibration, patient related dates like birth date, and non-patient instance related dates like effective and information issue.</p>
	 *
	 * @param	list							the list of attributes from the top level data set
	 * @return									the earliest date and time
	 */
	public static Date findEarliestDateTime(AttributeList list) {
		String dateToUseForUnaccompaniedTimes = getDateToUseForUnaccompaniedTimes(list);
		return findEarliestDateTime(list,null/*earliestSoFar*/,dateToUseForUnaccompaniedTimes);
	}
	
	/**
	 * <p>Move the date and time by the offset from the earliest date to the epoch.</p>
	 *
	 * @param	existingDate
	 * @param	epochForDateModification
	 * @param	earliest
	 * @return			the updated date and time
	 */
	public static Date getDateOffsetByEarliestMovedToEpoch(Date existingDate,Date epochForDateModification,Date earliest) {
		long existingTime = existingDate.getTime();
		long epochTime = epochForDateModification.getTime();
		long earliestTime = earliest.getTime();
		long newTime = existingTime - earliestTime + epochTime;
		Date newDate = new Date(newTime);
		return newDate;
	}
	
	/**
	 * <p>Get the date to use for time attributes that don't have a date sibling and which may be nested in sequences.</p>
	 *
	 * <p>E.g., to use with RadiopharmaceuticalStartTime.</p>
	 *
	 * @param	list	the list of attributes from the top level data set
	 * @return			a date to use, or an empty string if none
	 */
	public static String getDateToUseForUnaccompaniedTimes(AttributeList list) {
		String dateString = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("AcquisitionDate"));
		if (dateString.length() == 0) {
			dateString = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("ContentDate"));
		}
		if (dateString.length() == 0) {
			dateString = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("SeriesDate"));
		}
		if (dateString.length() == 0) {
			dateString = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("StudyDate"));
		}
//System.err.println("ClinicalTrialsAttributes.getDateToUseForUnaccompaniedTimes(): using  "+dateString);
		return dateString;
	}

	/**
	 * <p>Remove or remap Date, Time and DateTime attributes (other than Patient Birth) in a list of attributes, recursively iterating through nested sequences.</p>
	 *
	 * @param	list							the list of attributes to be cleaned up
	 * @param	handleDates						keep, remove or modify dates and times
	 * @param	epochForDateModification		used if handleDates is modify dates, otherwise null
	 * @param	earliest						used to determine the offset from the epoch to remove from all dates and times - MUST be the earliest date and time else unaccompanied time attributes may cross midnight
	 * @param	dateToUseForUnaccompaniedTimes	used for time attributes that don't have a date sibling and which may be nested in sequences, e.g., RadiopharmaceuticalStartTime
	 * @throws	DicomException	if error in DICOM encoding
	 */
	protected static void removeOrRemapDateAndTimeAttributes(AttributeList list,int handleDates,Date epochForDateModification,Date earliest,String dateToUseForUnaccompaniedTimes) throws DicomException {
//System.err.println("removeOrRemapDateAndTimeAttributes(): handleDates = "+handleDates);
		AttributeList replacementList = new AttributeList();			// to prevent concurrent modification
		List<AttributeTag> removeList = new LinkedList<AttributeTag>();	// to prevent concurrent modification
		// iterate through list to process all Date, Time and DateTime attributes, and recursively iterate through any sequences ...
		Iterator<Attribute> i = list.values().iterator();
		while (i.hasNext()) {
			Attribute o = i.next();
			if (o instanceof SequenceAttribute) {
				SequenceAttribute a = (SequenceAttribute)o;
				Iterator items = a.iterator();
				if (items != null) {
					while (items.hasNext()) {
						SequenceItem item = (SequenceItem)(items.next());
						if (item != null) {
							AttributeList itemAttributeList = item.getAttributeList();
							if (itemAttributeList != null) {
								removeOrRemapDateAndTimeAttributes(itemAttributeList,handleDates,epochForDateModification,earliest,dateToUseForUnaccompaniedTimes);
							}
						}
					}
				}
			}
			else if (handleDates == HandleDates.remove) {
				AttributeTag tag = o.getTag();
				if (o instanceof DateAttribute || o instanceof TimeAttribute || o instanceof DateTimeAttribute) {
					if (tag.equals(dictionary.getTagFromName("StudyDate")) || tag.equals(dictionary.getTagFromName("ContentDate"))) {			// PS 3.15 profile Z (Content is actually Z/D :( )
						{ Attribute a = new DateAttribute(tag); replacementList.put(a); }
					}
					else if (tag.equals(dictionary.getTagFromName("StudyTime")) || tag.equals(dictionary.getTagFromName("ContentTime"))) {		// PS 3.15 profile Z (Content is actually Z/D :( )
						{ Attribute a = new TimeAttribute(tag); replacementList.put(a); }
					}
					else if (tag.equals(dictionary.getTagFromName("PatientBirthDate")) || tag.equals(dictionary.getTagFromName("PatientBirthTime"))) {
						// do nothing ... handled seperately
					}
					else {																					// PS 3.15 profile X (Series Date, Time are actually X/D ... just assume X :( )
						removeList.add(tag);
					}
				}
				else if (tag.equals(dictionary.getTagFromName("TimezoneOffsetFromUTC"))) {
					removeList.add(tag);
				}
			}
			else if (handleDates == HandleDates.modify) {
				AttributeTag tag = o.getTag();
				if (o instanceof DateTimeAttribute) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): Doing DT "+o);
					String dateString = o.getSingleStringValueOrEmptyString();
					if (dateString.length() > 0) {
						try {
							Date existingDate = DateTimeAttribute.getDateFromFormattedString(dateString);
							if (existingDate != null) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): was DateTime "+dateString+" ("+existingDate+")");
								Date newDate = getDateOffsetByEarliestMovedToEpoch(existingDate,epochForDateModification,earliest);
								String newDateString = DateTimeAttribute.getFormattedString(newDate,TimeZone.getTimeZone("GMT"));
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): now "+newDateString+" ("+newDate+")");
								{ Attribute a = new DateTimeAttribute(tag); a.addValue(newDateString); replacementList.put(a); }
							}
							else {
								// problem ... set to zero length
								{ Attribute a = new DateTimeAttribute(tag); replacementList.put(a); }
							}
						}
						catch (java.text.ParseException Exception) {
							// problem ... set to zero length
							{ Attribute a = new DateTimeAttribute(tag); replacementList.put(a); }
						}
					}
					// else leave zero length DT alone
				}
				else if (o instanceof DateAttribute && !tag.equals(dictionary.getTagFromName("PatientBirthDate"))) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): Doing DA "+o);
					AttributeTag timeTag = getTagOfTimeAttributeCorrespondingToDateAttribute(((DateAttribute)o).getTag());
					String dateString = o.getSingleStringValueOrEmptyString();
					if (dateString.length() == 8) {
						String timeString = "";
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): timeTag "+timeTag);
						if (timeTag != null) {
							timeString = Attribute.getSingleStringValueOrDefault(list,timeTag,"");
						}
						if (timeString.length() > 0) {
							dateString = dateString + timeString;
						}
						try {
							Date existingDate = DateTimeAttribute.getDateFromFormattedString(dateString);
							if (existingDate != null) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): was Date and Time "+dateString+" ("+existingDate+")");
								Date newDate = getDateOffsetByEarliestMovedToEpoch(existingDate,epochForDateModification,earliest);
								String newDateString = DateTimeAttribute.getFormattedString(newDate,TimeZone.getTimeZone("GMT"),false/*tzSuffix*/);
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): now "+newDateString+" ("+newDate+")");
								{ Attribute a = new DateAttribute(tag);     a.addValue(newDateString.substring(0,8)); replacementList.put(a); }
								if (timeTag != null) { Attribute a = new TimeAttribute(timeTag); a.addValue(newDateString.substring(8)); replacementList.put(a); }
							}
						}
						catch (java.text.ParseException Exception) {
							// problem ... set to zero length
							{ Attribute a = new DateAttribute(tag);     replacementList.put(a); }
							if (timeTag != null) { Attribute a = new TimeAttribute(timeTag); replacementList.put(a); }
						}
					}
					else {
						// incorrect length so invalid format ... set to zero length (001392)
						{ Attribute a = new DateAttribute(tag);     replacementList.put(a); }
						if (timeTag != null) { Attribute a = new TimeAttribute(timeTag); replacementList.put(a); }
					}
				}
				else if (o instanceof TimeAttribute && o.getVL() > 0) {
					// need to handle time only attributes that have values and are relative to some other date (such as AcquisitionDate (NM) or SeriesDate (PET) or unspecified)
					// very important that this list match exactly what is used in findEarliestDateTime, otherwise risk crossing midnight
					if (tag.equals(dictionary.getTagFromName("RadiopharmaceuticalStartTime"))
					 || tag.equals(dictionary.getTagFromName("RadiopharmaceuticalStopTime"))
					 || tag.equals(dictionary.getTagFromName("InterventionDrugStartTime"))
					 || tag.equals(dictionary.getTagFromName("InterventionDrugStopTime"))
					 || tag.equals(dictionary.getTagFromName("ContrastBolusStartTime"))
					 || tag.equals(dictionary.getTagFromName("ContrastBolusStopTime"))) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): Doing TM "+o);
						if (dateToUseForUnaccompaniedTimes.length() == 8) {
							String dateString = dateToUseForUnaccompaniedTimes + o.getSingleStringValueOrEmptyString();
							try {
								Date existingDate = DateTimeAttribute.getDateFromFormattedString(dateString);
								if (existingDate != null) {
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): was Time only special handling "+dateString+" ("+existingDate+")");
									Date newDate = getDateOffsetByEarliestMovedToEpoch(existingDate,epochForDateModification,earliest);
									String newDateString = DateTimeAttribute.getFormattedString(newDate,TimeZone.getTimeZone("GMT"),false/*tzSuffix*/);
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): now "+newDateString+" ("+newDate+")");
									{ Attribute a = new TimeAttribute(tag); a.addValue(newDateString.substring(8)); replacementList.put(a); }
								}
							}
							catch (java.text.ParseException Exception) {
								// problem ... leave it alone - not ideal, but not identity leak, so no need to zero it or remove it
							}
						}
						// else leave it alone - not ideal, but not identity leak, so no need to zero it or remove it
					}
				}
			}
		}
		for (AttributeTag t : removeList) {
			list.remove(t);
		}
//System.err.println("ClinicalTrialsAttributes.removeOrRemapDateAndTimeAttributes(): replacementList "+replacementList);
		list.putAll(replacementList);
	}

	public class HandleStructuredContent {
		public static final int keep = 0;
		public static final int remove = 1;
		public static final int modify = 2;
	}
	
	protected static void removeStructuredContent(AttributeList list) {
		list.remove(dictionary.getTagFromName("ConceptNameCodeSequence"));
		list.remove(dictionary.getTagFromName("ObservationUID"));
		list.remove(dictionary.getTagFromName("ObservationDateTime"));
		list.remove(dictionary.getTagFromName("ReferencedContentItemIdentifier"));
		list.remove(dictionary.getTagFromName("RelationshipType"));
		list.remove(dictionary.getTagFromName("ContentSequence"));		// not really consistent with CP 1801, which says replace with dummy :(
		list.remove(dictionary.getTagFromName("ValueType"));
		list.remove(dictionary.getTagFromName("TextValue"));
		list.remove(dictionary.getTagFromName("DateTime"));
		list.remove(dictionary.getTagFromName("Date"));
		list.remove(dictionary.getTagFromName("Time"));
		list.remove(dictionary.getTagFromName("PersonName"));
		list.remove(dictionary.getTagFromName("UID"));
		list.remove(dictionary.getTagFromName("MeasuredValueSequence"));
		list.remove(dictionary.getTagFromName("NumericValueQualifierCodeSequence"));
		list.remove(dictionary.getTagFromName("ConceptCodeSequence"));
		list.remove(dictionary.getTagFromName("ReferencedSOPSequence"));
		list.remove(dictionary.getTagFromName("GraphicData"));
		list.remove(dictionary.getTagFromName("GraphicType"));
		list.remove(dictionary.getTagFromName("PixelOriginInterpretation"));
		list.remove(dictionary.getTagFromName("FiducialUID"));
		list.remove(dictionary.getTagFromName("ReferencedFrameOfReferenceUID"));
		list.remove(dictionary.getTagFromName("TemporalRangeType"));
		list.remove(dictionary.getTagFromName("ReferencedSamplePositions"));
		list.remove(dictionary.getTagFromName("ReferencedTimeOffsets"));
		list.remove(dictionary.getTagFromName("ReferencedDateTime"));
		list.remove(dictionary.getTagFromName("ContinuityOfContent"));
		list.remove(dictionary.getTagFromName("ContentTemplateSequence"));
	}
	
	/**
	 * <p>Remove or clean structured content including the SR content tree (whether an SR object or not) in a list of attributes, recursively iterating through nested sequences.</p>
	 *
	 * @param	list						the list of attributes to be cleaned up
	 * @param	handleStructuredContent		keep, remove or modify structured content
	 * @param	keepDescriptors				if true, keep the text description and comment attributes
	 * @param	keepSeriesDescriptors		if true, keep the series description even if all other descriptors are removed
	 * @param	keepProtocolName			if true, keep protocol name even if all other descriptors are removed
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @param	keepDeviceIdentity			if true, keep device identity
	 * @param	keepInstitutionIdentity		if true, keep institution identity
	 * @param	handleUIDs					remove or remap the UIDs
	 * @param	handleDates					keep, remove or modify dates and times
	 * @throws	DicomException	if error in DICOM encoding
	 */
	protected static void removeOrCleanStructuredContent(AttributeList list,int handleStructuredContent,boolean keepDescriptors,boolean keepSeriesDescriptors,boolean keepProtocolName,boolean keepPatientCharacteristics,boolean keepDeviceIdentity,boolean keepInstitutionIdentity,int handleUIDs,int handleDates) throws DicomException {
		AttributeList replacementList = new AttributeList();			// to prevent concurrent modification
		
		// iterate through list to recursively iterate through any sequences depth first, and prune any removed nodes ...
		Iterator<Attribute> i = list.values().iterator();
		while (i.hasNext()) {
			Attribute o = i.next();
			if (o instanceof SequenceAttribute) {
				List<SequenceItem> removeEmptySequenceItemList = new LinkedList<SequenceItem>();	// to prevent concurrent modification
				SequenceAttribute a = (SequenceAttribute)o;
				Iterator items = a.iterator();
				if (items != null) {
					while (items.hasNext()) {
						SequenceItem item = (SequenceItem)(items.next());
						if (item != null) {
							AttributeList itemAttributeList = item.getAttributeList();
							if (itemAttributeList != null && !itemAttributeList.isEmpty()) {
								removeOrCleanStructuredContent(itemAttributeList,handleStructuredContent,keepDescriptors,keepSeriesDescriptors,keepProtocolName,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity,handleUIDs,handleDates);
								if (itemAttributeList.isEmpty()) {
									removeEmptySequenceItemList.add(item);
								}
							}
						}
					}
				}
				for (SequenceItem item : removeEmptySequenceItemList) {
					a.remove(item);
				}
			}
		}
		{
			boolean removeStructuredContentInThisList = false;
			CodedSequenceItem conceptNameCodeSequence = CodedSequenceItem.getSingleCodedSequenceItemOrNull(list,dictionary.getTagFromName("ConceptNameCodeSequence"));
			if (conceptNameCodeSequence != null) {
				if (handleStructuredContent == HandleStructuredContent.remove) {
					removeStructuredContentInThisList = true;	// not really consistent with CP 1801, which now says replace with dummy :(
				}
				else if (handleStructuredContent == HandleStructuredContent.modify) {
					String cv = conceptNameCodeSequence.getCodeValue();
					String csd = conceptNameCodeSequence.getCodingSchemeDesignator();
					String valueType = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("ValueType"));
//System.err.println("Doing "+valueType+" ("+cv+", "+csd+", \""+conceptNameCodeSequence.getCodeMeaning()+"\")");

					if (cv.equals("121022") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Accession Number"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("113795") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Acquired Image"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("126201") && csd.equals("DCM") && valueType.equals("DATE")) {	// "Acquisition Date"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("125203") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Acquisition Protocol"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("126202") && csd.equals("DCM") && valueType.equals("TIME")) {	// "Acquisition Time"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("C67447") && csd.equals("NCIt") && valueType.equals("TEXT")) {	// "Activity Session"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("440252007") && csd.equals("SCT") && valueType.equals("TEXT")) {	// "Administration of radiopharmaceutical"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("15") && csd.equals("NCDR [2.0b]") && valueType.equals("DATETIME")) {	// "Admission DateTime"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("112050") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Anatomic Identifier"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("DF-0070B") && csd.equals("SRT") && valueType.equals("DATETIME")) {	// "Anesthesia Finish Time"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("DF-0068E") && csd.equals("SRT") && valueType.equals("DATETIME")) {	// "Anesthesia Start Time"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121080") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Best illustration of finding"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121080") && csd.equals("DCM") && valueType.equals("WAVEFORM")) {	// "Best illustration of finding"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113723") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "Calibration DateTime"
						if (handleDates == HandleDates.remove) { Attribute a = new DateTimeAttribute(dictionary.getTagFromName("DateTime")); a.addValue(replacementForDateTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("113720") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Calibration Protocol"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113724") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Calibration Responsible Party"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("76") && csd.equals("NCDR [2.0b]") && valueType.equals("PNAME")) {	// "Catheterization Operator"
						{ Attribute a = new PersonNameAttribute(dictionary.getTagFromName("PersonName")); a.addValue(replacementForPersonNameInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121120") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Cath Lab Procedure Log"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("R-42B89") && csd.equals("SRT") && valueType.equals("COMPOSITE")) {	// "Clinical Report"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("R-42B89") && csd.equals("SRT") && valueType.equals("TEXT")) {	// "Clinical Report"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121106") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Comment"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("DD-60002") && csd.equals("SRT") && valueType.equals("TEXT")) {	// "Complication of Procedure"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("112347") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Component ID"
						{ Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121077") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Conclusion"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111018") && csd.equals("DCM") && valueType.equals("DATE")) {	// "Content Date"
						if (handleDates == HandleDates.remove) { Attribute a = new DateAttribute(dictionary.getTagFromName("Date")); a.addValue(replacementForDateInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("111019") && csd.equals("DCM") && valueType.equals("TIME")) {	// "Content Time"
						if (handleDates == HandleDates.remove) { Attribute a = new TimeAttribute(dictionary.getTagFromName("Time")); a.addValue(replacementForTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("122073") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Current procedure evidence"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("11955-2") && csd.equals("LN") && valueType.equals("DATE")) {	// "Date of last menstrual period"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121431") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "DateTime Concern Noted"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121432") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "DateTime Concern Resolved"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111527") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "DateTime Ended"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("122165") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "DateTime of Death"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("122105") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "DateTime of Intervention"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111536") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "DateTime of last evaluation"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111702") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "DateTime of processing"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121125") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "DateTime of Recording of Log Entry"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111535") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "DateTime problem observed"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121433") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "DateTime Problem Resolved"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111526") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "DateTime Started"
						if (handleDates == HandleDates.remove) { Attribute a = new DateTimeAttribute(dictionary.getTagFromName("DateTime")); a.addValue(replacementForDateTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("112363") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Degree of Freedom ID"
						{ Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("112357") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Derived Fiducial"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("112373") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Derived Planning Data"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("112372") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Derived Planning Images"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111021") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Description of Change"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121145") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Description of Material"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113877") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Device Name"
						if (!keepDeviceIdentity) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121013") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Device Observer Name"
						if (!keepDeviceIdentity) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121017") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Device Observer Physical Location During Observation"
						if (!keepInstitutionIdentity) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121016") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Device Observer Serial Number"
						if (!keepDeviceIdentity) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121012") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Device Observer UID"
						if (handleUIDs == HandleUIDs.remove) {			// regardless of keepDeviceIdentity ?? :(
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113880") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Device Serial Number"
						if (!keepDeviceIdentity) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121193") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Device Subject Name"
						if (!keepDeviceIdentity) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121197") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Device Subject Physical Location during observation"
						if (!keepInstitutionIdentity) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121196") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Device Subject Serial Number"
						if (!keepDeviceIdentity) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121198") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Device Subject UID"
						if (!keepDeviceIdentity && handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("122163") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "Discharge DateTime"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121342") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Dose Image"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("122083") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Drug administered"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("122082") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "Drug end"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("122081") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "Drug start"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("F-00033") && csd.equals("SRT") && valueType.equals("TEXT")) {	// "ECG Finding"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("11778-8") && csd.equals("LN") && valueType.equals("DATE")) {	// "EDD"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113810") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "End of X-Ray Irradiation"
						if (handleDates == HandleDates.remove) { Attribute a = new DateTimeAttribute(dictionary.getTagFromName("DateTime")); a.addValue(replacementForDateTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121122") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Equipment Identification"
						if (!keepDeviceIdentity && !keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128429") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Event UID Used"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121088") && csd.equals("DCM") && valueType.equals("PNAME")) {	// "Fellow"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("11951-1") && csd.equals("LN") && valueType.equals("TEXT")) {	// "Fetus ID"
						{ Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121021") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Filler Number"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("121071") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Finding"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("G-C0E3") && csd.equals("SRT") && valueType.equals("TEXT")) {	// "Finding Site"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("112227") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Frame of Reference UID"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("127857") && csd.equals("DCM") && valueType.equals("DATE")) {	// "Glucose Measurement Date"
						if (handleDates == HandleDates.remove) { Attribute a = new DateAttribute(dictionary.getTagFromName("Date")); a.addValue(replacementForDateInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("127858") && csd.equals("DCM") && valueType.equals("TIME")) {	// "Glucose Measurement Time"
						if (handleDates == HandleDates.remove) { Attribute a = new TimeAttribute(dictionary.getTagFromName("Time")); a.addValue(replacementForTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("11329-0") && csd.equals("LN") && valueType.equals("TEXT")) {	// "History"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113832") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Identification of the X-Ray Source"
						if (!keepDeviceIdentity) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("125010") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Identifier"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("128775") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Identifier within Person Observer's Role"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("112229") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Identifying Segment"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("125201") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Illustration of Finding"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121200") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Illustration of ROI"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121138") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Image Acquired"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("122712") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "Imaging Start DateTime"
						if (handleDates == HandleDates.remove) { Attribute a = new DateTimeAttribute(dictionary.getTagFromName("DateTime")); a.addValue(replacementForDateTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("112366") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Implant Assembly Template"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111033") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Impression Description"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("18785-6") && csd.equals("LN") && valueType.equals("TEXT")) {	// "Indications for Procedure"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121154") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Intervention attempt identifier"
						{ Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("113850") && csd.equals("DCM") && valueType.equals("PNAME")) {	// "Irradiation Authorizing"
						{ Attribute a = new PersonNameAttribute(dictionary.getTagFromName("PersonName")); a.addValue(replacementForPersonNameInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("113605") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Irradiation Event Label"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("113769") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Irradiation Event UID"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("110190") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Issuer of Identifier"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("111706") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Issuer of Parent Specimen Identifier"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("111724") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Issuer of Specimen Identifier"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("113012") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Key Object Description"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("18118-0") && csd.equals("LN") && valueType.equals("TEXT")) {	// "LV Wall Motion Segmental Findings"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("112371") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Manufacturer Implant Template"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("112352") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Mating Feature ID"
						{ Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("112351") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Mating Feature Set ID"
						{ Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("111516") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Medication Type"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121036") && csd.equals("DCM") && valueType.equals("PNAME")) {	// "Mother of fetus"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("113873") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Organization Name"
						if (!keepInstitutionIdentity) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111040") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Original Source"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111705") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Parent Specimen Identifier"
						{ Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("112361") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Patient Data Used During Planning"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("112354") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Patient Image"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113815") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Patient Model"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121110") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Patient Presentation"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128425") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Patient Radiation Dose Model Data"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128425") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Patient Radiation Dose Model Data"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128425") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Patient Radiation Dose Model Data"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128426") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Patient Radiation Dose Model Reference"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("109054") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Patient State"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("122128") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Patient Transferred From"
						if (!keepInstitutionIdentity) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121126") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Performed Procedure Step SOP Instance UID"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121114") && csd.equals("DCM") && valueType.equals("PNAME")) {	// "Performing Physician"
						{ Attribute a = new PersonNameAttribute(dictionary.getTagFromName("PersonName")); a.addValue(replacementForPersonNameInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121152") && csd.equals("DCM") && valueType.equals("PNAME")) {	// "Person administering drug/contrast"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("113871") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Person ID"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("113872") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Person ID Issuer"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("113870") && csd.equals("DCM") && valueType.equals("PNAME")) {	// "Person Name"
						{ Attribute a = new PersonNameAttribute(dictionary.getTagFromName("PersonName")); a.addValue(replacementForPersonNameInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("128774") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Person Observer's Login Name"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("121009") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Person Observer's Organization Name"
						if (!keepInstitutionIdentity) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121008") && csd.equals("DCM") && valueType.equals("PNAME")) {	// "Person Observer Name"
						{ Attribute a = new PersonNameAttribute(dictionary.getTagFromName("PersonName")); a.addValue(replacementForPersonNameInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121173") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Physician Note"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121020") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Placer Number"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("113516") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Prescription Identifier"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("122075") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Prior report for current patient"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121124") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Procedure Action ID"
						{ Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("122146") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "Procedure DateTime"
						if (handleDates == HandleDates.remove) { Attribute a = new DateTimeAttribute(dictionary.getTagFromName("DateTime")); a.addValue(replacementForDateTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("52") && csd.equals("NCDR [2.0b]") && valueType.equals("DATETIME")) {	// "Procedure DateTime"
						if (handleDates == HandleDates.remove) { Attribute a = new DateTimeAttribute(dictionary.getTagFromName("DateTime")); a.addValue(replacementForDateTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121065") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Procedure Description"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("53") && csd.equals("NCDR [2.0b]") && valueType.equals("TEXT")) {	// "Procedure Number in this admission"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("122177") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Procedure Result"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121019") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Procedure Study Component UID"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121018") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Procedure Study Instance UID"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("122701") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "Procedure Time Base"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111703") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Processing step description"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("126071") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Protocol Time Point Identifier"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128230") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Pulse Sequence Name"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121002") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Quoted Source"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128436") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Radiation Dose Composite Parameters"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128403") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Radiation Dose Estimate Name"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("128414") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Radiation Dose Representation Data"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128414") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Radiation Dose Representation Data"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113514") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Radionuclide Identifier"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("113503") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Radiopharmaceutical Administration Event UID"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113511") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Radiopharmaceutical Dispense Unit Identifier"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("113512") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Radiopharmaceutical Lot Identifier"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("123003") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "Radiopharmaceutical Start DateTime"
						if (handleDates == HandleDates.remove) { Attribute a = new DateTimeAttribute(dictionary.getTagFromName("DateTime")); a.addValue(replacementForDateTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("123004") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "Radiopharmaceutical Stop DateTime"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113513") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Reagent Vial Identifier"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("126100") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Real World Value Map used for measurement"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113907") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Reason for Proceeding"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113552") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Recent Physical Activity"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121075") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Recommendation"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111054") && csd.equals("DCM") && valueType.equals("DATE")) {	// "Recommended Follow-up Date"
						if (handleDates == HandleDates.remove) { Attribute a = new DateAttribute(dictionary.getTagFromName("Date")); a.addValue(replacementForDateInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121191") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Referenced Segment"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121214") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Referenced Segmentation Frame"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("112364") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Related Patient Data Not Used During Planning"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121121") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Room Identification"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("111469") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "Sampling DateTime"
						if (handleDates == HandleDates.remove) { Attribute a = new DateTimeAttribute(dictionary.getTagFromName("DateTime")); a.addValue(replacementForDateTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("111058") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Selected Region Description"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("112002") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Series Instance UID"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113985") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Series or Instance used for Water Equivalent Diameter estimation"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121434") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Service Delivery Location"
						if (!keepInstitutionIdentity) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121435") && csd.equals("DCM") && valueType.equals("PNAME")) {	// "Service Performer"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("121435") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Service Performer"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("F-02455") && csd.equals("SRT") && valueType.equals("TEXT")) {	// "Social History"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121233") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Source image for segmentation"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121112") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "Source of Measurement"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121112") && csd.equals("DCM") && valueType.equals("WAVEFORM")) {	// "Source of Measurement"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121232") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Source series for segmentation"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128447") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Spatial Fiducials"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("112353") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Spatial Registration"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128444") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Spatial Registration Reference"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111700") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Specimen Container Identifier"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("121041") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Specimen Identifier"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("121039") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Specimen UID"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128416") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "SR Instance Used"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("G-D321") && csd.equals("SRT") && valueType.equals("DATETIME")) {	// "Start DateTime"
						if (handleDates == HandleDates.remove) { Attribute a = new DateTimeAttribute(dictionary.getTagFromName("DateTime")); a.addValue(replacementForDateTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("113809") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "Start of X-Ray Irradiation"
						if (handleDates == HandleDates.remove) { Attribute a = new DateTimeAttribute(dictionary.getTagFromName("DateTime")); a.addValue(replacementForDateTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("110119") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Station AE Title"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("122173") && csd.equals("DCM") && valueType.equals("DATETIME")) {	// "ST Elevation Onset DateTime"
						if (handleDates == HandleDates.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("G-D320") && csd.equals("SRT") && valueType.equals("DATETIME")) {	// "Stop DateTime"
						if (handleDates == HandleDates.remove) { Attribute a = new DateTimeAttribute(dictionary.getTagFromName("DateTime")); a.addValue(replacementForDateTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("109056") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Stress Protocol"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111060") && csd.equals("DCM") && valueType.equals("DATE")) {	// "Study Date"
						if (handleDates == HandleDates.remove) { Attribute a = new DateAttribute(dictionary.getTagFromName("Date")); a.addValue(replacementForDateInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("110180") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Study Instance UID"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("111061") && csd.equals("DCM") && valueType.equals("TIME")) {	// "Study Time"
						if (handleDates == HandleDates.remove) { Attribute a = new TimeAttribute(dictionary.getTagFromName("Time")); a.addValue(replacementForTimeInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121033") && csd.equals("DCM") && valueType.equals("NUM")) {	// "Subject Age"
						if (!keepPatientCharacteristics) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121031") && csd.equals("DCM") && valueType.equals("DATE")) {	// "Subject Birth Date"
						removeStructuredContentInThisList = true;
					}
					else if (cv.equals("121030") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Subject ID"
						{ Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121029") && csd.equals("DCM") && valueType.equals("PNAME")) {	// "Subject Name"
						{ Attribute a = new PersonNameAttribute(dictionary.getTagFromName("PersonName")); a.addValue(replacementForPersonNameInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("121032") && csd.equals("DCM") && valueType.equals("CODE")) {	// "Subject Sex"
						if (!keepPatientCharacteristics) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("126070") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Subject Time Point Identifier"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121028") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Subject UID"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121111") && csd.equals("DCM") && valueType.equals("TEXT")) {	// "Summary"
						if (!keepDescriptors) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("112359") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "Supporting Information"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("C2348792") && csd.equals("UMLS") && valueType.equals("TEXT")) {	// "Time Point"
						if (!keepDescriptors) { Attribute a = new UnlimitedTextAttribute(dictionary.getTagFromName("TextValue")); a.addValue(replacementForTextInStructuredContent); replacementList.put(a); }
					}
					else if (cv.equals("112040") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "Tracking Unique Identifier"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("112356") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "User Selected Fiducial"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("121143") && csd.equals("DCM") && valueType.equals("WAVEFORM")) {	// "Waveform Acquired"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128470") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "X-Ray Attenuator Model Data"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128470") && csd.equals("DCM") && valueType.equals("IMAGE")) {	// "X-Ray Attenuator Model Data"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("128470") && csd.equals("DCM") && valueType.equals("UIDREF")) {	// "X-Ray Attenuator Model Data"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					else if (cv.equals("113701") && csd.equals("DCM") && valueType.equals("COMPOSITE")) {	// "X-Ray Radiation Dose Report"
						if (handleUIDs == HandleUIDs.remove) {
							removeStructuredContentInThisList = true;
						}
					}
					// gets this far only if combination of code and valueType unrecognized ...
					else if (valueType.equals("PNAME")) {
						removeStructuredContentInThisList = true;
					}
					else if (valueType.equals("TEXT")) {	// regardless of keepDescriptors since may be names and identifiers in private content items :(
						removeStructuredContentInThisList = true;
					}

					//else {
					//	System.err.println("Doing nothing - no match - to ("+cv+", "+csd+", \""+conceptNameCodeSequence.getCodeMeaning()+"\")");
					//}
				}
				// else do nothing ... shouldn't have been called in the first place
			}
			if (removeStructuredContentInThisList) {
				removeStructuredContent(list);
			}
		}
		list.putAll(replacementList);
	}

	/**
	 * <p>De-identify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences, other than Context Sequence.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Leaves dates and times alone (other than Patient Birth).</p>
	 *
	 * <p>Does not de-identify any specific content items in structured content, e.g., within Content Sequence.</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @deprecated	retained for compatibility with previous releases; does NOT keep patient characteristics (such as might be needed for PET SUV calculations); use {@link ClinicalTrialsAttributes#removeOrNullIdentifyingAttributes(AttributeList,boolean,boolean,boolean) removeOrNullIdentifyingAttributes()} instead
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @param	keepUIDs	if true, keep the UIDs
	 * @param	keepDescriptors	if true, keep the text description and comment attributes
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,boolean keepUIDs,boolean keepDescriptors) throws DicomException {
		removeOrNullIdentifyingAttributes(list,keepUIDs,keepDescriptors,false/*keepPatientCharacteristics*/);
	}

	/**
	 * <p>De-identify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences, other than Context Sequence.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Leaves dates and times alone (other than Patient Birth).</p>
	 *
	 * <p>Does not de-identify any specific content items in structured content, e.g., within Content Sequence.</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @param	keepUIDs	if true, keep the UIDs
	 * @param	keepDescriptors	if true, keep the text description and comment attributes
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,boolean keepUIDs,boolean keepDescriptors,boolean keepPatientCharacteristics) throws DicomException {
		removeOrNullIdentifyingAttributes(list,keepUIDs ? HandleUIDs.keep : HandleUIDs.remove,keepDescriptors,keepPatientCharacteristics);
	}
	
	/**
	 * <p>De-identify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences, other than Context Sequence.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Leaves dates and times alone (other than Patient Birth).</p>
	 *
	 * <p>Does not de-identify any specific content items in structured content, e.g., within Content Sequence.</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @param	handleUIDs	keep, remove or remap the UIDs
	 * @param	keepDescriptors	if true, keep the text description and comment attributes
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,int handleUIDs,boolean keepDescriptors,boolean keepPatientCharacteristics) throws DicomException {
		removeOrNullIdentifyingAttributes(list,handleUIDs,keepDescriptors,false/*keepSeriesDescriptors*/,keepPatientCharacteristics,false/*keepDeviceIdentity*/,false/*keepInstitutionIdentity*/);
	}

	/**
	 * <p>De-identify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences, other than Context Sequence.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Leaves dates and times alone (other than Patient Birth).</p>
	 *
	 * <p>Does not de-identify any specific content items in structured content, e.g., within Content Sequence.</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @param	list		the list of attributes to be cleaned up
	 * @param	handleUIDs	keep, remove or remap the UIDs
	 * @param	keepDescriptors	if true, keep the text description and comment attributes
	 * @param	keepSeriesDescriptors	if true, keep the series description even if all other descriptors are removed
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @param	keepDeviceIdentity	if true, keep device identity
	 * @param	keepInstitutionIdentity	if true, keep institution identity
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,int handleUIDs,boolean keepDescriptors,boolean keepSeriesDescriptors,boolean keepPatientCharacteristics,boolean keepDeviceIdentity,boolean keepInstitutionIdentity) throws DicomException {
		removeOrNullIdentifyingAttributes(list,handleUIDs,keepDescriptors,keepSeriesDescriptors,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity,HandleDates.keep,null/*epochForDateModification*/,null/*earliestDateInSet*/);
	}
	
	/**
	 * <p>De-identify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences, other than Context Sequence.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Handles dates and times as requested (other than Patient Birth), including within nested sequences, including Context Sequence.</p>
	 *
	 * <p>Does not de-identify any specific content items in structured content, e.g., within Content Sequence.</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @param	list						the list of attributes to be cleaned up
	 * @param	handleUIDs					keep, remove or remap the UIDs
	 * @param	keepDescriptors				if true, keep the text description and comment attributes
	 * @param	keepSeriesDescriptors		if true, keep the series description even if all other descriptors are removed
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @param	keepDeviceIdentity			if true, keep device identity
	 * @param	keepInstitutionIdentity		if true, keep institution identity
	 * @param	handleDates					keep, remove or modify dates and times
	 * @param	epochForDateModification	the epoch to which to move the earliest date, used if handleDates is modify dates, otherwise null
	 * @param	earliestDateInSet			the known earliest date to move to the specified epoch, used if handleDates is modify dates, otherwise null; if null, the earliest in the supplied list is used; MUST be the earliest date and time else unaccompanied time attributes may cross midnight
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,int handleUIDs,boolean keepDescriptors,boolean keepSeriesDescriptors,boolean keepPatientCharacteristics,boolean keepDeviceIdentity,boolean keepInstitutionIdentity,int handleDates,Date epochForDateModification,Date earliestDateInSet) throws DicomException {
		removeOrNullIdentifyingAttributes(list,handleUIDs,keepDescriptors,keepSeriesDescriptors,false/*keepProtocolName*/,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity,handleDates,epochForDateModification,earliestDateInSet);	// (001104)
	}

	
	/**
	 * <p>De-identify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Content Sequence.</p>
	 *
	 * <p>Handles dates and times as requested (other than Patient Birth), including within nested sequences, including Content Sequence.</p>
	 *
	 * <p>Does not de-identify any specific content items in structured content, e.g., within Content Sequence.</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @param	list						the list of attributes to be cleaned up
	 * @param	handleUIDs					keep, remove or remap the UIDs
	 * @param	keepDescriptors				if true, keep the text description and comment attributes
	 * @param	keepSeriesDescriptors		if true, keep the series description even if all other descriptors are removed
	 * @param	keepProtocolName			if true, keep protocol name even if all other descriptors are removed
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @param	keepDeviceIdentity			if true, keep device identity
	 * @param	keepInstitutionIdentity		if true, keep institution identity
	 * @param	handleDates					keep, remove or modify dates and times
	 * @param	epochForDateModification	the epoch to which to move the earliest date, used if handleDates is modify dates, otherwise null
	 * @param	earliestDateInSet			the known earliest date to move to the specified epoch, used if handleDates is modify dates, otherwise null; if null, the earliest in the supplied list is used; MUST be the earliest date and time else unaccompanied time attributes may cross midnight
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,int handleUIDs,boolean keepDescriptors,boolean keepSeriesDescriptors,boolean keepProtocolName,boolean keepPatientCharacteristics,boolean keepDeviceIdentity,boolean keepInstitutionIdentity,int handleDates,Date epochForDateModification,Date earliestDateInSet) throws DicomException {
		removeOrNullIdentifyingAttributes(list,handleUIDs,keepDescriptors,keepSeriesDescriptors,false/*keepProtocolName*/,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity,handleDates,epochForDateModification,earliestDateInSet,HandleStructuredContent.keep,false/*aggregateAgesOver89*/);
	}

	
	/**
	 * <p>De-identify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Content Sequence.</p>
	 *
	 * <p>Handles dates and times as requested (other than Patient Birth), including within nested sequences, including Content Sequence.</p>
	 *
	 * <p>Handles structured content as requested, including within nested sequences, including (and not just confined to) Content Sequence.</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @param	list						the list of attributes to be cleaned up
	 * @param	handleUIDs					keep, remove or remap the UIDs
	 * @param	keepDescriptors				if true, keep the text description and comment attributes
	 * @param	keepSeriesDescriptors		if true, keep the series description even if all other descriptors are removed
	 * @param	keepProtocolName			if true, keep protocol name even if all other descriptors are removed
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @param	keepDeviceIdentity			if true, keep device identity
	 * @param	keepInstitutionIdentity		if true, keep institution identity
	 * @param	handleDates					keep, remove or modify dates and times
	 * @param	epochForDateModification	the epoch to which to move the earliest date, used if handleDates is modify dates, otherwise null
	 * @param	earliestDateInSet			the known earliest date to move to the specified epoch, used if handleDates is modify dates, otherwise null; if null, the earliest in the supplied list is used; MUST be the earliest date and time else unaccompanied time attributes may cross midnight
	 * @param	handleStructuredContent		keep, remove or modify structured content
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,int handleUIDs,boolean keepDescriptors,boolean keepSeriesDescriptors,boolean keepProtocolName,boolean keepPatientCharacteristics,boolean keepDeviceIdentity,boolean keepInstitutionIdentity,int handleDates,Date epochForDateModification,Date earliestDateInSet,int handleStructuredContent) throws DicomException {
		removeOrNullIdentifyingAttributes(list,handleUIDs,keepDescriptors,keepSeriesDescriptors,keepProtocolName,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity,handleDates,epochForDateModification,earliestDateInSet,handleStructuredContent,false/*aggregateAgesOver89*/);
	}
	
	/**
	 * <p>De-identify a list of attributes.</p>
	 *
	 * <p>De-identifies attributes within nested sequences.</p>
	 *
	 * <p>Handles UIDs as requested, including within nested sequences, including Content Sequence.</p>
	 *
	 * <p>Handles dates and times as requested (other than Patient Birth), including within nested sequences, including Content Sequence.</p>
	 *
	 * <p>Can optionally group ages over 89 years into single age of 90 as per HIPAA PR §164.514(b)(2)(i)(C).</p>
	 *
	 * <p>Handles structured content as requested, including within nested sequences, including (and not just confined to) Content Sequence.</p>
	 *
	 * <p>Also adds record that de-identification has been performed.</p>
	 *
	 * @param	list						the list of attributes to be cleaned up
	 * @param	handleUIDs					keep, remove or remap the UIDs
	 * @param	keepDescriptors				if true, keep the text description and comment attributes
	 * @param	keepSeriesDescriptors		if true, keep the series description even if all other descriptors are removed
	 * @param	keepProtocolName			if true, keep protocol name even if all other descriptors are removed
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @param	keepDeviceIdentity			if true, keep device identity
	 * @param	keepInstitutionIdentity		if true, keep institution identity
	 * @param	handleDates					keep, remove or modify dates and times
	 * @param	epochForDateModification	the epoch to which to move the earliest date, used if handleDates is modify dates, otherwise null
	 * @param	earliestDateInSet			the known earliest date to move to the specified epoch, used if handleDates is modify dates, otherwise null; if null, the earliest in the supplied list is used; MUST be the earliest date and time else unaccompanied time attributes may cross midnight
	 * @param	handleStructuredContent		keep, remove or modify structured content
	 * @param	aggregateAgesOver89			if true, and keepPatientCharacteristics true, combine patient ages over 89 years into single 90 year category
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public static void removeOrNullIdentifyingAttributes(AttributeList list,int handleUIDs,boolean keepDescriptors,boolean keepSeriesDescriptors,boolean keepProtocolName,boolean keepPatientCharacteristics,boolean keepDeviceIdentity,boolean keepInstitutionIdentity,int handleDates,Date epochForDateModification,Date earliestDateInSet,int handleStructuredContent,boolean aggregateAgesOver89) throws DicomException {
//System.err.println("removeOrNullIdentifyingAttributes():");
		removeOrNullIdentifyingAttributesRecursively(list,handleUIDs,keepDescriptors,keepSeriesDescriptors,keepProtocolName,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity,aggregateAgesOver89);
		
		if (handleUIDs != HandleUIDs.keep) {
			removeOrRemapUIDAttributes(list,handleUIDs);
		}
		
		if (handleDates != HandleDates.keep) {
//System.err.println("removeOrNullIdentifyingAttributes(): handleDates != HandleDates.keep");
			String dateToUseForUnaccompaniedTimes = getDateToUseForUnaccompaniedTimes(list);
			if (earliestDateInSet == null) {
				earliestDateInSet = findEarliestDateTime(list,null/*earliestSoFar*/,dateToUseForUnaccompaniedTimes);
			}
			removeOrRemapDateAndTimeAttributes(list,handleDates,epochForDateModification,earliestDateInSet,dateToUseForUnaccompaniedTimes);
		}
		
		if (handleStructuredContent != HandleStructuredContent.keep) {
			// UIDs and dates already handled generically
			removeOrCleanStructuredContent(list,handleStructuredContent,keepDescriptors,keepSeriesDescriptors,keepProtocolName,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity,handleUIDs,handleDates);
		}
		
		{ AttributeTag tag = dictionary.getTagFromName("PatientIdentityRemoved"); list.remove(tag); Attribute a = new CodeStringAttribute(tag); a.addValue("YES"); list.put(tag,a); }
		{
			AttributeTag tag = dictionary.getTagFromName("DeidentificationMethod");
			Attribute a = list.get(tag);
			if (a == null) {
				a = new LongStringAttribute(tag);
				list.put(tag,a);
			}
			a.addValue("Deidentified");
			a.addValue("Descriptors " + (keepDescriptors ? "retained" : ("removed" + (keepSeriesDescriptors ? " except series" : "") + (keepProtocolName ? " except protocol" : ""))));
			a.addValue("Patient Characteristics " + (keepPatientCharacteristics ? (aggregateAgesOver89 ? "retained with ages >89 set to 90" : "retained") : "removed"));
			a.addValue("Device identity " + (keepDeviceIdentity ? "retained" : "removed"));
			a.addValue("Institution identity " + (keepInstitutionIdentity ? "retained" : "removed"));
			if (handleUIDs != HandleUIDs.keep) {
				a.addValue("UIDs " + (handleUIDs == HandleUIDs.remap ? "remapped" : "removed"));
			}
			a.addValue("Dates " + (handleDates == HandleDates.keep ? "retained" : (handleDates == HandleDates.modify ? "modified" : "removed")));
			a.addValue("Structured content " + (handleStructuredContent == HandleStructuredContent.keep ? "retained" : (handleStructuredContent == HandleStructuredContent.modify ? "modified" : "removed")));
		}
		{
			AttributeTag tag = dictionary.getTagFromName("DeidentificationMethodCodeSequence");
			SequenceAttribute a = (SequenceAttribute)(list.get(tag));
			if (a == null) {
				a = new SequenceAttribute(tag);
				list.put(tag,a);
			}
			a.addItem(new CodedSequenceItem("113100","DCM","Basic Application Confidentiality Profile").getAttributeList());
			
			if (keepDescriptors) {
				a.addItem(new CodedSequenceItem("210005","99PMP","Retain all descriptors unchanged").getAttributeList());
			}
			else {
				if (keepSeriesDescriptors && keepProtocolName) {
					a.addItem(new CodedSequenceItem("210008","99PMP","Remove all descriptors except Series Description & Protocol Name").getAttributeList());
				}
				else if (keepProtocolName) {
					a.addItem(new CodedSequenceItem("210009","99PMP","Remove all descriptors except Protocol Name").getAttributeList());
				}
				else if (keepSeriesDescriptors) {
					a.addItem(new CodedSequenceItem("210003","99PMP","Remove all descriptors except Series Description").getAttributeList());
				}
				else {
					a.addItem(new CodedSequenceItem("210004","99PMP","Remove all descriptors").getAttributeList());
				}
			}
			
			if (keepPatientCharacteristics) {
				a.addItem(new CodedSequenceItem("113108","DCM","Retain Patient Characteristics Option").getAttributeList());
				if (aggregateAgesOver89) {	// (001325)
					a.addItem(new CodedSequenceItem("210012","99PMP","Patient ages > 89 years set to 90").getAttributeList());
				}
			}
			
			if (keepDeviceIdentity) {
				a.addItem(new CodedSequenceItem("113109","DCM","Retain Device Identity Option").getAttributeList());
			}

			if (keepInstitutionIdentity) {
				a.addItem(new CodedSequenceItem("113112","DCM","Retain Institution Identity Option").getAttributeList());
			}

			if (handleUIDs == HandleUIDs.keep) {
				a.addItem(new CodedSequenceItem("113110","DCM","Retain UIDs Option").getAttributeList());
			}
			else if (handleUIDs == HandleUIDs.remap) {
				a.addItem(new CodedSequenceItem("210001","99PMP","Remap UIDs").getAttributeList());
			}
			else if (handleUIDs == HandleUIDs.remove) {
				a.addItem(new CodedSequenceItem("210007","99PMP","Remove UIDs").getAttributeList());
			}

			if (handleDates == HandleDates.keep) {
				a.addItem(new CodedSequenceItem("113106","DCM","Retain Longitudinal Temporal Information Full Dates Option").getAttributeList());
			}
			else if (handleDates == HandleDates.modify) {
				a.addItem(new CodedSequenceItem("113107","DCM","Retain Longitudinal Temporal Information Modified Dates Option").getAttributeList());
			}

			if (handleStructuredContent == HandleStructuredContent.keep) {
				a.addItem(new CodedSequenceItem("210011","99PMP","Retain all structured content unchanged").getAttributeList());
			}
			else if (handleStructuredContent == HandleStructuredContent.modify) {
				a.addItem(new CodedSequenceItem("113104","DCM","Clean Structured Content Option").getAttributeList());
			}
			else if (handleStructuredContent == HandleStructuredContent.remove) {
				a.addItem(new CodedSequenceItem("210010","99PMP","Remove all structured content").getAttributeList());
			}
		}
	}

	
	/**
	 * <p>De-identify a list of attributes, recursively iterating through nested sequences.</p>
	 *
	 * <p>Does not process UIDs, but does remove sequences that would be invalidated by removing UIDs, e.g., Source Image Sequence and Referenced Image Sequence. If necessary caller should use {@link ClinicalTrialsAttributes#removeOrRemapUIDAttributes(AttributeList,int) removeOrRemapUIDAttributes()}.</p>
	 *
	 * <p>Does not process dates and times (other than Patient Birth). If necessary caller should use {@link ClinicalTrialsAttributes#removeOrRemapDateAndTimeAttributes(AttributeList,int,Date,Date,String) removeOrRemapDateAndTimeAttributes()}.</p>
	 *
	 * @param	list						the list of attributes to be cleaned up
	 * @param	handleUIDs					keep, remove or remap the UIDs
	 * @param	keepDescriptors				if true, keep the text description and comment attributes
	 * @param	keepSeriesDescriptors		if true, keep the series description even if all other descriptors are removed
	 * @param	keepProtocolName			if true, keep protocol name even if all other descriptors are removed
	 * @param	keepPatientCharacteristics	if true, keep patient characteristics (such as might be needed for PET SUV calculations)
	 * @param	keepDeviceIdentity			if true, keep device identity
	 * @param	keepInstitutionIdentity		if true, keep device identity
	 * @param	aggregateAgesOver89			if true, and keepPatientCharacteristics true, combine patient ages over 89 years into single 90 year category
	 * @throws	DicomException	if error in DICOM encoding
	 */
	protected static void removeOrNullIdentifyingAttributesRecursively(AttributeList list,int handleUIDs,boolean keepDescriptors,boolean keepSeriesDescriptors,boolean keepProtocolName,boolean keepPatientCharacteristics,boolean keepDeviceIdentity,boolean keepInstitutionIdentity,boolean aggregateAgesOver89) throws DicomException {
		// use the list from the Basic Application Level Confidentiality Profile in PS 3.15 2003, as updated per draft of Sup 142
	
		if (!keepDescriptors) {
			list.remove(dictionary.getTagFromName("StudyDescription"));
			if (!keepSeriesDescriptors) {
				list.remove(dictionary.getTagFromName("SeriesDescription"));
				list.remove(dictionary.getTagFromName("ClinicalTrialSeriesDescription"));
			}
			if (!keepProtocolName) {
				list.remove(dictionary.getTagFromName("ProtocolName"));
			}
		}

		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("AccessionNumber"));
		
		if (!keepInstitutionIdentity) {
			list.remove(dictionary.getTagFromName("InstitutionCodeSequence"));
			list.remove(dictionary.getTagFromName("InstitutionName"));
			list.remove(dictionary.getTagFromName("InstitutionAddress"));
			list.remove(dictionary.getTagFromName("InstitutionalDepartmentName"));
			list.remove(dictionary.getTagFromName("InstitutionalDepartmentTypeCodeSequence"));
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("SourceOfPreviousValues"));	// CP 2088
		}
		
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("ReferringPhysicianName"));
		list.remove(dictionary.getTagFromName("ReferringPhysicianAddress"));
		list.remove(dictionary.getTagFromName("ReferringPhysicianTelephoneNumbers"));
		list.remove(dictionary.getTagFromName("PhysiciansOfRecord"));
		list.remove(dictionary.getTagFromName("PerformingPhysicianName"));
		list.remove(dictionary.getTagFromName("ConsultingPhysicianName"));
		list.remove(dictionary.getTagFromName("ConsultingPhysicianIdentificationSequence"));
		list.remove(dictionary.getTagFromName("NameOfPhysiciansReadingStudy"));
		list.remove(dictionary.getTagFromName("RequestingPhysician"));		// not in IOD; from Detached Study Mx; seen in Philips CT, ADAC NM
		list.remove(dictionary.getTagFromName("ReviewerName"));				// not in IOD; from Approval Module; seen in Philips US
		
		if (keepPatientCharacteristics && Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("PatientAge")).length() == 0) {
			addAgeDerivedFromBirthDateAndStudyRelatedDate(list);	// need to do this BEFORE replacing PatientBirthDate with zero length, obviously
		}

		list.remove(dictionary.getTagFromName("OperatorsName"));
		list.remove(dictionary.getTagFromName("AdmittingDiagnosesDescription"));
		list.remove(dictionary.getTagFromName("DerivationDescription"));
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("PatientName"));
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("PatientID"));
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("PatientBirthDate"));
		list.remove(dictionary.getTagFromName("PatientBirthTime"));
		list.remove(dictionary.getTagFromName("ReferencedPatientPhotoSequence"));	// CP 1343
		list.remove(dictionary.getTagFromName("OtherPatientIDs"));
		list.remove(dictionary.getTagFromName("OtherPatientIDsSequence"));
		list.remove(dictionary.getTagFromName("OtherPatientNames"));

		if (keepPatientCharacteristics) {
			// (001325)
			if (aggregateAgesOver89) {
				// should be exactly four characters, no padding, ending in D,W,M,Y, with zero padded 3 digit age,
				// but make robust to errors such as leading or trailing space padding and non-zero padded numeric component and lower case units
				String ageString = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("PatientAge")).trim().toUpperCase();
				if (ageString.length() > 0 && ageString.endsWith("Y")) {
					try {
						int age = Integer.parseInt(ageString.substring(0,ageString.length()-1));
						if (age > 89) {
							{ Attribute a = new AgeStringAttribute(dictionary.getTagFromName("PatientAge")); a.addValue("090Y"); list.put(a); }
						}
					}
					catch (NumberFormatException e) {
						// hmmm - what should we do with bad age strings ? same is if this option were not specified - i.e., leave it alone :(
					}
				}
				// else may be different age units (D,W,M), all of which imply not > 089Y
				// or could be bad - what should we do with bad age strings ? same is if this option were not specified - i.e., leave it alone :(
			}
		}
		else {
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("PatientSex"));
			list.remove(dictionary.getTagFromName("PatientAge"));
			list.remove(dictionary.getTagFromName("PatientSize"));
			list.remove(dictionary.getTagFromName("PatientWeight"));
			list.remove(dictionary.getTagFromName("EthnicGroup"));
			list.remove(dictionary.getTagFromName("PregnancyStatus"));		// not in IOD; from Detached Patient Mx
			list.remove(dictionary.getTagFromName("SmokingStatus"));			// not in IOD; from Detached Patient Mx
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("PatientSexNeutered"));
			list.remove(dictionary.getTagFromName("SpecialNeeds"));
		}
		
		list.remove(dictionary.getTagFromName("MedicalRecordLocator"));
		list.remove(dictionary.getTagFromName("Occupation"));
		list.remove(dictionary.getTagFromName("AdditionalPatientHistory"));
		list.remove(dictionary.getTagFromName("PatientComments"));

		if (!keepDeviceIdentity) {
			list.remove(dictionary.getTagFromName("StationName"));
			list.replaceWithValueIfPresent(dictionary.getTagFromName("DeviceSerialNumber"),replacementForDeviceSerialNumber);
			list.remove(dictionary.getTagFromName("DeviceUID"));
			list.remove(dictionary.getTagFromName("PlateID"));
			list.remove(dictionary.getTagFromName("GantryID"));
			list.remove(dictionary.getTagFromName("CassetteID"));
			list.remove(dictionary.getTagFromName("GeneratorID"));
			list.replaceWithValueIfPresent(dictionary.getTagFromName("DetectorID"),replacementForDetectorID);
			list.remove(dictionary.getTagFromName("PerformedStationAETitle"));
			list.remove(dictionary.getTagFromName("PerformedStationGeographicLocationCodeSequence"));
			list.remove(dictionary.getTagFromName("PerformedStationName"));
			list.remove(dictionary.getTagFromName("PerformedStationNameCodeSequence"));
			list.remove(dictionary.getTagFromName("ScheduledProcedureStepLocation"));
			list.remove(dictionary.getTagFromName("ScheduledStationAETitle"));
			list.remove(dictionary.getTagFromName("ScheduledStationGeographicLocationCodeSequence"));
			list.remove(dictionary.getTagFromName("ScheduledStationName"));
			list.remove(dictionary.getTagFromName("ScheduledStationNameCodeSequence"));
			list.remove(dictionary.getTagFromName("ScheduledStudyLocation"));
			list.remove(dictionary.getTagFromName("ScheduledStudyLocationAETitle"));
			list.remove(dictionary.getTagFromName("SourceManufacturer"));	// CP 1720
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("SourceSerialNumber"));	// CP 1720, CP 1801
			list.remove(dictionary.getTagFromName("TreatmentMachineName"));	// CP 1720
			list.remove(dictionary.getTagFromName("UDISequence"));			// cp_dac477_moreonuniquedeviceid
			list.remove(dictionary.getTagFromName("UniqueDeviceIdentifier"));// cp_dac477_moreonuniquedeviceid
			list.remove(dictionary.getTagFromName("DeviceDescription"));		// cp_dac477_moreonuniquedeviceid
			list.remove(dictionary.getTagFromName("LensMake"));				// CP 1736
			list.remove(dictionary.getTagFromName("LensModel"));			// CP 1736
			list.remove(dictionary.getTagFromName("LensSerialNumber"));		// CP 1736
			list.remove(dictionary.getTagFromName("LensSpecification"));	// CP 1736
			list.replaceWithValueIfPresent(dictionary.getTagFromName("XRayDetectorID"),replacementForDetectorID);	// CP 1928
			list.remove(dictionary.getTagFromName("XRayDetectorLabel"));	// CP 1928
			list.replaceWithValueIfPresent(dictionary.getTagFromName("XRaySourceID"),replacementForDetectorID);		// CP 1928
			list.remove(dictionary.getTagFromName("TransducerIdentificationSequence"));	// CP 1951
			list.remove(dictionary.getTagFromName("DateOfLastCalibration"));			// (001387)
			list.remove(dictionary.getTagFromName("DateOfLastDetectorCalibration"));	// (001387)
			list.remove(dictionary.getTagFromName("TimeOfLastCalibration"));			// (001387)
			list.remove(dictionary.getTagFromName("TimeOfLastDetectorCalibration"));	// (001387)
		}
		
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("StudyID"));
		list.remove(dictionary.getTagFromName("RequestAttributesSequence"));
		
		// remove all issuers, whether in composite IODs or not
		list.remove(dictionary.getTagFromName("IssuerOfAccessionNumberSequence"));
		list.remove(dictionary.getTagFromName("IssuerOfPatientID"));
		list.remove(dictionary.getTagFromName("IssuerOfPatientIDQualifiersSequence"));
		list.remove(dictionary.getTagFromName("StudyIDIssuer"));
		list.remove(dictionary.getTagFromName("IssuerOfAdmissionID"));
		list.remove(dictionary.getTagFromName("IssuerOfAdmissionIDSequence"));		// CP 1801
		list.remove(dictionary.getTagFromName("IssuerOfServiceEpisodeID"));
		list.remove(dictionary.getTagFromName("IssuerOfServiceEpisodeIDSequence"));	// CP 1801
		list.remove(dictionary.getTagFromName("ResultsIDIssuer"));
		list.remove(dictionary.getTagFromName("InterpretationIDIssuer"));
		
		list.remove(dictionary.getTagFromName("StudyStatusID"));			// not in IOD; from Detached Study Mx; seen in Philips CT
		list.remove(dictionary.getTagFromName("StudyPriorityID"));		// not in IOD; from Detached Study Mx; seen in Philips CT
		list.remove(dictionary.getTagFromName("CurrentPatientLocation"));	// not in IOD; from Detached Study Mx; seen in Philips CT

		list.remove(dictionary.getTagFromName("PatientInstitutionResidence"));
		list.remove(dictionary.getTagFromName("PatientTransportArrangements"));
		
		list.remove(dictionary.getTagFromName("PatientAddress"));					// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("MilitaryRank"));						// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("BranchOfService"));					// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("PatientBirthName"));					// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("PatientMotherBirthName"));				// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("ConfidentialityConstraintOnPatientDataDescription"));	// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("PatientInsurancePlanCodeSequence"));			// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("PatientPrimaryLanguageCodeSequence"));			// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("PatientPrimaryLanguageModifierCodeSequence"));
		list.remove(dictionary.getTagFromName("CountryOfResidence"));					// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("RegionOfResidence"));					// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("PatientTelephoneNumbers"));				// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("PatientReligiousPreference"));				// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("MedicalAlerts"));						// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("Allergies"));							// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("LastMenstrualDate"));					// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("SpecialNeeds"));						// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("PatientState"));						// not in IOD; from Detached Patient Mx
		list.remove(dictionary.getTagFromName("AdmissionID"));						// not in IOD
		list.remove(dictionary.getTagFromName("AdmittingDate"));						// not in IOD
		list.remove(dictionary.getTagFromName("AdmittingTime"));						// not in IOD

		if (!keepDescriptors) {
			list.remove(dictionary.getTagFromName("ImageComments"));
			list.remove(dictionary.getTagFromName("FrameComments"));
		}

		// ContentSequence - not removed despite PS3.15 Annex E (or replace with dummy per CP 1801) - handle seperately using removeOrCleanStructuredContent()
		
		// ContentIdentificationMacro
		
		list.replaceWithValueIfPresent(dictionary.getTagFromName("ContentCreatorName"),replacementForContentCreatorName);	// CP 1801
		list.remove(dictionary.getTagFromName("ContentCreatorIdentificationCodeSequence"));

		// others that it would seem necessary to remove ...
		
		list.remove(dictionary.getTagFromName("ReferencedPatientSequence"));
		list.remove(dictionary.getTagFromName("ReferringPhysicianIdentificationSequence"));
		list.remove(dictionary.getTagFromName("PhysiciansOfRecordIdentificationSequence"));
		list.remove(dictionary.getTagFromName("PhysiciansReadingStudyIdentificationSequence"));
		list.remove(dictionary.getTagFromName("ReferencedStudySequence"));
		list.remove(dictionary.getTagFromName("AdmittingDiagnosesCodeSequence"));
		list.remove(dictionary.getTagFromName("PerformingPhysicianIdentificationSequence"));
		list.remove(dictionary.getTagFromName("OperatorIdentificationSequence"));
		list.remove(dictionary.getTagFromName("ScheduledProcedureStepID"));	// CP 
		list.remove(dictionary.getTagFromName("PerformedProcedureStepID"));
		list.remove(dictionary.getTagFromName("DataSetTrailingPadding"));
		
		list.remove(dictionary.getTagFromName("DigitalSignaturesSequence"));
		list.remove(dictionary.getTagFromName("ReferencedDigitalSignatureSequence"));
		list.remove(dictionary.getTagFromName("ReferencedSOPInstanceMACSequence"));
		list.remove(dictionary.getTagFromName("MAC"));
		
		list.remove(dictionary.getTagFromName("ModifiedAttributesSequence"));
		list.remove(dictionary.getTagFromName("OriginalAttributesSequence"));

		list.remove(dictionary.getTagFromName("ActualHumanPerformersSequence"));
		list.remove(dictionary.getTagFromName("AddressTrial"));
		list.remove(dictionary.getTagFromName("Arbitrary"));
		list.remove(dictionary.getTagFromName("AuthorObserverSequence"));
		list.remove(dictionary.getTagFromName("ContributionDescription"));
		list.remove(dictionary.getTagFromName("CurrentObserverTrial"));
		list.remove(dictionary.getTagFromName("CustodialOrganizationSequence"));
		list.remove(dictionary.getTagFromName("DistributionAddress"));
		list.remove(dictionary.getTagFromName("DistributionName"));
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("FillerOrderNumberImagingServiceRequest"));
		list.remove(dictionary.getTagFromName("HumanPerformerName"));
		list.remove(dictionary.getTagFromName("HumanPerformerOrganization"));
		list.remove(dictionary.getTagFromName("IconImageSequence"));
		list.remove(dictionary.getTagFromName("IdentifyingComments"));
		list.remove(dictionary.getTagFromName("InsurancePlanIdentification"));
		list.remove(dictionary.getTagFromName("IntendedRecipientsOfResultsIdentificationSequence"));
		list.remove(dictionary.getTagFromName("InterpretationApproverSequence"));
		list.remove(dictionary.getTagFromName("InterpretationAuthor"));
		list.remove(dictionary.getTagFromName("InterpretationRecorder"));
		list.remove(dictionary.getTagFromName("InterpretationTranscriber"));
		list.remove(dictionary.getTagFromName("InstanceOriginStatus"));
		list.remove(dictionary.getTagFromName("ModifyingDeviceID"));
		list.remove(dictionary.getTagFromName("ModifyingDeviceManufacturer"));
		list.remove(dictionary.getTagFromName("NamesOfIntendedRecipientsOfResults"));
		list.remove(dictionary.getTagFromName("OrderCallbackPhoneNumber"));
		list.remove(dictionary.getTagFromName("OrderCallbackTelecomInformation"));
		list.remove(dictionary.getTagFromName("OrderEnteredBy"));
		list.remove(dictionary.getTagFromName("OrderEntererLocation"));
		list.remove(dictionary.getTagFromName("ParticipantSequence"));
		list.remove(dictionary.getTagFromName("PatientTelecomInformation"));
		list.remove(dictionary.getTagFromName("PerformedLocation"));
		list.remove(dictionary.getTagFromName("PersonAddress"));
		list.remove(dictionary.getTagFromName("PersonIdentificationCodeSequence"));
		list.remove(dictionary.getTagFromName("PersonName"));
		list.remove(dictionary.getTagFromName("PersonTelecomInformation"));
		list.remove(dictionary.getTagFromName("PersonTelephoneNumbers"));
		list.remove(dictionary.getTagFromName("PhysicianApprovingInterpretation"));
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("PlacerOrderNumberImagingServiceRequest"));
		list.remove(dictionary.getTagFromName("PreMedication"));
		list.remove(dictionary.getTagFromName("ReferencedPatientAliasSequence"));
		list.remove(dictionary.getTagFromName("RequestedProcedureLocation"));
		list.remove(dictionary.getTagFromName("RequestedProcedureID"));
		list.remove(dictionary.getTagFromName("RequestingService"));
		list.remove(dictionary.getTagFromName("ResponsibleOrganization"));
		list.remove(dictionary.getTagFromName("ResponsiblePerson"));
		list.remove(dictionary.getTagFromName("ResultsDistributionListSequence"));
		list.remove(dictionary.getTagFromName("ScheduledHumanPerformersSequence"));
		list.remove(dictionary.getTagFromName("ScheduledPatientInstitutionResidence"));
		list.remove(dictionary.getTagFromName("ScheduledPerformingPhysicianIdentificationSequence"));
		list.remove(dictionary.getTagFromName("ScheduledPerformingPhysicianName"));
		list.remove(dictionary.getTagFromName("ServiceEpisodeID"));
		list.remove(dictionary.getTagFromName("TelephoneNumberTrial"));
		list.remove(dictionary.getTagFromName("TextComments"));
		list.remove(dictionary.getTagFromName("TextString"));
		list.remove(dictionary.getTagFromName("TopicAuthor"));
		list.remove(dictionary.getTagFromName("TopicKeywords"));
		list.remove(dictionary.getTagFromName("TopicSubject"));
		list.remove(dictionary.getTagFromName("TopicTitle"));
		list.remove(dictionary.getTagFromName("VerbalSourceTrial"));
		list.remove(dictionary.getTagFromName("VerbalSourceIdentifierCodeSequenceTrial"));
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("VerifyingObserverIdentificationCodeSequence"));
		list.replaceWithValueIfPresent(dictionary.getTagFromName("VerifyingObserverName"),replacementForVerifyingObserverName);
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("VerifyingObserverSequence"));
		list.replaceWithValueIfPresent(dictionary.getTagFromName("VerifyingOrganization"),replacementForVerifyingOrganization);	// CP 1801
		
		// CP 1736
		
		list.remove(dictionary.getTagFromName("CameraOwnerName"));
		list.remove(dictionary.getTagFromName("GPSAltitude"));
		list.remove(dictionary.getTagFromName("GPSAltitudeRef"));
		list.remove(dictionary.getTagFromName("GPSAreaInformation"));
		list.remove(dictionary.getTagFromName("GPSDestBearing"));
		list.remove(dictionary.getTagFromName("GPSDestBearingRef"));
		list.remove(dictionary.getTagFromName("GPSDestDistance"));
		list.remove(dictionary.getTagFromName("GPSDestDistanceRef"));
		list.remove(dictionary.getTagFromName("GPSDestLatitude"));
		list.remove(dictionary.getTagFromName("GPSDestLatitudeRef"));
		list.remove(dictionary.getTagFromName("GPSDestLongitude"));
		list.remove(dictionary.getTagFromName("GPSDestLongitudeRef"));
		list.remove(dictionary.getTagFromName("GPSDifferential"));
		list.remove(dictionary.getTagFromName("GPSDOP"));
		list.remove(dictionary.getTagFromName("GPSImgDirection"));
		list.remove(dictionary.getTagFromName("GPSImgDirectionRef"));
		list.remove(dictionary.getTagFromName("GPSLatitude"));
		list.remove(dictionary.getTagFromName("GPSLatitudeRef"));
		list.remove(dictionary.getTagFromName("GPSLongitude"));
		list.remove(dictionary.getTagFromName("GPSLongitudeRef"));
		list.remove(dictionary.getTagFromName("GPSMapDatum"));
		list.remove(dictionary.getTagFromName("GPSMeasureMode"));
		list.remove(dictionary.getTagFromName("GPSProcessingMethod"));
		list.remove(dictionary.getTagFromName("GPSSatellites"));
		list.remove(dictionary.getTagFromName("GPSSpeed"));
		list.remove(dictionary.getTagFromName("GPSSpeedRef"));
		list.remove(dictionary.getTagFromName("GPSStatus"));
		list.remove(dictionary.getTagFromName("GPSTimeStamp"));
		list.remove(dictionary.getTagFromName("GPSTrack"));
		list.remove(dictionary.getTagFromName("GPSTrackRef"));
		list.remove(dictionary.getTagFromName("GPSVersionID"));

		// CP 1826
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("BarcodeValue"));
		list.remove(dictionary.getTagFromName("ContainerComponentID"));
		list.replaceWithValueIfPresent(dictionary.getTagFromName("ContainerIdentifier"),replacementForContainerIdentifier);
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("IssuerOfTheContainerIdentifierSequence"));
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("IssuerOfTheSpecimenIdentifierSequence"));
		list.remove(dictionary.getTagFromName("SlideIdentifier"));
		list.remove(dictionary.getTagFromName("SpecimenAccessionNumber"));
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("SpecimenPreparationSequence"));		// one day should handle with clean structured content :(
		list.replaceWithValueIfPresent(dictionary.getTagFromName("SpecimenIdentifier"),replacementForSpecimenIdentifier);
		// SpecimenUID is handled like any other UID
		if (!keepDescriptors) {
			list.remove(dictionary.getTagFromName("ContainerDescription"));
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("LabelText"));
			list.remove(dictionary.getTagFromName("SpecimenDetailedDescription"));
			list.remove(dictionary.getTagFromName("SpecimenShortDescription"));
		}
		
		// Sup 175

		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("RTAccessoryDeviceSlotID"));
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("RTAccessoryHolderSlotID"));
		if (!keepDescriptors) {
			list.remove(dictionary.getTagFromName("EquipmentFrameOfReferenceDescription"));
			list.replaceWithValueIfPresent(dictionary.getTagFromName("RadiationDoseIdentificationLabel"),replacementForDescriptionOrLabel);
			list.replaceWithValueIfPresent(dictionary.getTagFromName("RadiationDoseInVivoMeasurementLabel"),replacementForDescriptionOrLabel);
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("RadiationGenerationModeDescription"));
			list.replaceWithValueIfPresent(dictionary.getTagFromName("RadiationGenerationModeLabel"),replacementForDescriptionOrLabel);
			list.replaceWithValueIfPresent(dictionary.getTagFromName("RTToleranceSetLabel"),replacementForDescriptionOrLabel);
			list.replaceWithValueIfPresent(dictionary.getTagFromName("TreatmentPositionGroupLabel"),replacementForDescriptionOrLabel);
		}
		// handled like any other UID ...
		//ManufacturerDeviceClassUID
		//PatientSetupUID
		//TreatmentPositionGroupUID
		
		// CP 1878
		list.remove(dictionary.getTagFromName("PrescriptionNotesSequence"));
		
		// handled like any other UID ...
		//ConceptualVolumeUID
		//ReferencedConceptualVolumeUID
		//ConstituentConceptualVolumeUID
		//SourceConceptualVolumeUID
		//ReferencedFiducialsUID
		//RTTreatmentPhaseUID
		//DosimetricObjectiveUID
		//ReferencedDosimetricObjectiveUID
		
		// handled like any other Date ...
		//IntendedPhaseStartDate
		//IntendedPhaseEndDate
		
		// CP 2038
		
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("ROIInterpreter"));
		//StructureSetDate - handled like any other Date
		//StructureSetTime - handled like any other Time

		// CP cp_dac559_DeIdNonConforming
		
		list.remove(dictionary.getTagFromName("NonconformingModifiedAttributesSequence"));
		list.remove(dictionary.getTagFromName("NonconformingDataElementValue"));
		
		// CP 2088
		
		list.remove(dictionary.getTagFromName("InterpretationID"));
		list.remove(dictionary.getTagFromName("ResultsID"));
		
		if (!keepDeviceIdentity) {
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("DeviceAlternateIdentifier"));
			list.replaceWithValueIfPresent(dictionary.getTagFromName("DeviceLabel"),replacementForDescriptionOrLabel);
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("ManufacturerDeviceIdentifier"));
			list.replaceWithValueIfPresent(dictionary.getTagFromName("ModifyingSystem"),replacementForDescriptionOrLabel);	// CP 2088
		}
		if (!keepDescriptors) {
			list.remove(dictionary.getTagFromName("LongDeviceDescription"));
			list.remove(dictionary.getTagFromName("ConceptualVolumeCombinationDescription"));
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("ConceptualVolumeDescription"));
			list.replaceWithValueIfPresent(dictionary.getTagFromName("UserContentLabel"),replacementForDescriptionOrLabel);
			list.replaceWithValueIfPresent(dictionary.getTagFromName("UserContentLongLabel"),replacementForDescriptionOrLabel);
			list.replaceWithValueIfPresent(dictionary.getTagFromName("EntityLabel"),replacementForDescriptionOrLabel);
			list.remove(dictionary.getTagFromName("EntityName"));
			list.remove(dictionary.getTagFromName("EntityDescription"));
			list.replaceWithValueIfPresent(dictionary.getTagFromName("EntityLongLabel"),replacementForDescriptionOrLabel);
			list.replaceWithValueIfPresent(dictionary.getTagFromName("RTPrescriptionLabel"),replacementForDescriptionOrLabel);
			list.replaceWithValueIfPresent(dictionary.getTagFromName("RTTreatmentApproachLabel"),replacementForDescriptionOrLabel);
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("RTPhysicianIntentNarrative"));
			list.remove(dictionary.getTagFromName("ReasonForSuperseding"));
			list.remove(dictionary.getTagFromName("PriorTreatmentDoseDescription"));
			list.remove(dictionary.getTagFromName("TreatmentSites"));	// CP 2157
			list.replaceWithValueIfPresent(dictionary.getTagFromName("TreatmentSite"),replacementForDescriptionOrLabel);
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("TreatmentTechniqueNotes"));
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("PrescriptionNotes"));
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("FractionationNotes"));

			list.remove(dictionary.getTagFromName("PerformedProcedureStepDescription"));
			list.remove(dictionary.getTagFromName("CommentsOnThePerformedProcedureStep"));
			list.remove(dictionary.getTagFromName("AcquisitionComments"));
			list.remove(dictionary.getTagFromName("ReasonForStudy"));		// not in IOD; from Detached Study Mx; seen in Philips CT
			list.remove(dictionary.getTagFromName("RequestedProcedureDescription"));	// not in IOD; from Detached Study Mx; seen in Philips CT
			list.remove(dictionary.getTagFromName("StudyComments"));			// not in IOD; from Detached Study Mx; seen in Philips CT
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("AcquisitionDeviceProcessingDescription"));
			list.remove(dictionary.getTagFromName("DischargeDiagnosisDescription"));
			list.remove(dictionary.getTagFromName("ImagePresentationComments"));
			list.remove(dictionary.getTagFromName("ImagingServiceRequestComments"));
			list.remove(dictionary.getTagFromName("Impressions"));
			list.remove(dictionary.getTagFromName("InterpretationDiagnosisDescription"));
			list.remove(dictionary.getTagFromName("InterpretationText"));
			list.remove(dictionary.getTagFromName("OverlayComments"));
			list.remove(dictionary.getTagFromName("ReasonForTheImagingServiceRequest"));
			list.remove(dictionary.getTagFromName("ContrastBolusAgent"));
			list.remove(dictionary.getTagFromName("RequestedContrastAgent"));
			list.remove(dictionary.getTagFromName("RequestedProcedureComments"));
			list.remove(dictionary.getTagFromName("ResultsComments"));
			list.remove(dictionary.getTagFromName("ScheduledProcedureStepDescription"));
			list.remove(dictionary.getTagFromName("ServiceEpisodeDescription"));
			list.remove(dictionary.getTagFromName("VisitComments"));
			list.remove(dictionary.getTagFromName("ModifiedImageDescription"));
			list.remove(dictionary.getTagFromName("AcquisitionProtocolDescription"));
			list.remove(dictionary.getTagFromName("ReasonForOmissionDescription"));

			// CP 1720 ...
			list.remove(dictionary.getTagFromName("BeamDescription"));
			list.remove(dictionary.getTagFromName("BolusDescription"));
			list.remove(dictionary.getTagFromName("CompensatorDescription"));
			list.remove(dictionary.getTagFromName("DoseReferenceDescription"));
			list.remove(dictionary.getTagFromName("FixationDeviceDescription"));
			list.remove(dictionary.getTagFromName("FractionGroupDescription"));
			list.remove(dictionary.getTagFromName("PrescriptionDescription"));
			list.remove(dictionary.getTagFromName("RespiratoryMotionCompensationTechniqueDescription"));
			list.remove(dictionary.getTagFromName("RTPlanLabel"));
			list.remove(dictionary.getTagFromName("RTPlanName"));
			list.remove(dictionary.getTagFromName("RTPlanDescription"));
			list.remove(dictionary.getTagFromName("ShieldingDeviceDescription"));
			list.remove(dictionary.getTagFromName("SetupTechniqueDescription"));
			
			// CP 1736
			
			list.remove(dictionary.getTagFromName("DeviceSettingDescription"));
			list.remove(dictionary.getTagFromName("MakerNote"));

			// CP 1837
			
			list.remove(dictionary.getTagFromName("ReasonForTheRequestedProcedure"));
			list.remove(dictionary.getTagFromName("ReasonForRequestedProcedureCodeSequence"));
			list.remove(dictionary.getTagFromName("ReasonForVisit"));
			list.remove(dictionary.getTagFromName("ReasonForVisitCodeSequence"));
			
			// Sup 202
			
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("FlowIdentifierSequence"));
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("FlowIdentifier"));		// OB so don't replace with string
			list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("SourceIdentifier"));		// OB so don't replace with string

			// CP 1928
			
			list.remove(dictionary.getTagFromName("DecompositionDescription"));
			list.remove(dictionary.getTagFromName("MultienergyAcquisitionDescription"));
			
			// CP 2038
			
			list.replaceWithValueIfPresent(dictionary.getTagFromName("StructureSetLabel"),replacementForDescriptionOrLabel);
			list.remove(dictionary.getTagFromName("StructureSetName"));
			list.remove(dictionary.getTagFromName("StructureSetDescription"));
			list.replaceWithValueIfPresent(dictionary.getTagFromName("ROIName"),replacementForDescriptionOrLabel);
			list.remove(dictionary.getTagFromName("ROIDescription"));
			list.remove(dictionary.getTagFromName("ROIGenerationDescription"));
			list.remove(dictionary.getTagFromName("ROIObservationLabel"));
			list.remove(dictionary.getTagFromName("ROIObservationDescription"));

			// Sup 199
			
			list.remove(dictionary.getTagFromName("InterlockDescription"));
			list.remove(dictionary.getTagFromName("InterlockOriginDescription"));
			list.remove(dictionary.getTagFromName("TreatmentToleranceViolationDescription"));
			
			// CP 2012
			
			list.remove(dictionary.getTagFromName("CommentsOnRadiationDose"));
			
			// Sup 212
			
			list.replaceWithValueIfPresent(dictionary.getTagFromName("AcquisitionFieldOfViewLabel"),replacementForDescriptionOrLabel);
			list.remove(dictionary.getTagFromName("RequestedSeriesDescription"));
			
			// CP 2088
			
			list.replaceWithValueIfPresent(dictionary.getTagFromName("ReasonForTheAttributeModification"),replacementForDescriptionOrLabel);
			
			// Sup 222
			
			list.replaceWithValueIfPresent(dictionary.getTagFromName("AnnotationGroupLabel"),replacementForDescriptionOrLabel);
			list.remove(dictionary.getTagFromName("AnnotationGroupDescription"));
			
			// Sup 160
			
			list.remove(dictionary.getTagFromName("PatientTreatmentPreparationProcedureParameterDescription"));
			list.remove(dictionary.getTagFromName("PatientTreatmentPreparationMethodDescription"));
			list.remove(dictionary.getTagFromName("PatientSetupPhotoDescription"));
			list.remove(dictionary.getTagFromName("DisplacementReferenceLabel"));
			
			// CP 2116

			// not DA, DT, TM since handled generically with shift etc.
			list.replaceWithValueIfPresent(dictionary.getTagFromName("SelectorASValue"),replacementForAgeString);
			list.replaceWithValueIfPresent(dictionary.getTagFromName("SelectorLOValue"),replacementForDescriptionOrLabel);
			list.replaceWithValueIfPresent(dictionary.getTagFromName("SelectorLTValue"),replacementForDescriptionOrLabel);
			list.remove(dictionary.getTagFromName("SelectorOBValue"));	// should really replace with dummy per profile and IOD :(
			list.replaceWithValueIfPresent(dictionary.getTagFromName("SelectorPNValue"),replacementForPersonNameInStructuredContent);
			list.replaceWithValueIfPresent(dictionary.getTagFromName("SelectorSHValue"),replacementForDescriptionOrLabel);
			list.replaceWithValueIfPresent(dictionary.getTagFromName("SelectorSTValue"),replacementForDescriptionOrLabel);
			list.remove(dictionary.getTagFromName("SelectorUNValue"));	// should really replace with dummy per profile and IOD :(
			list.replaceWithValueIfPresent(dictionary.getTagFromName("SelectorURValue"),replacementForDescriptionOrLabel);
			list.replaceWithValueIfPresent(dictionary.getTagFromName("SelectorUTValue"),replacementForDescriptionOrLabel);
			
			list.remove(dictionary.getTagFromName("CertifiedTimestamp"));
			list.remove(dictionary.getTagFromName("ContextGroupLocalVersion"));
			list.remove(dictionary.getTagFromName("ContextGroupVersion"));
			
			// CP 2135
			
			list.remove(dictionary.getTagFromName("PyramidDescription"));
			list.remove(dictionary.getTagFromName("PyramidLabel"));
			
			// CP 2162
			list.remove(dictionary.getTagFromName("WaveformFilterDescription"));
			list.remove(dictionary.getTagFromName("FilterLookupTableDescription"));

			// Sup 213
			list.remove(dictionary.getTagFromName("PositionAcquisitionTemplateName"));
			list.remove(dictionary.getTagFromName("PositionAcquisitionTemplateDescription"));
		}

		if (handleUIDs == HandleUIDs.remove) {
			// these are not UI VR, and hence don't get taken care of later,
			// but if left, would be made invalid when Instance UIDs were removed and
			// incomplete items left
			list.remove(dictionary.getTagFromName("ReferencedImageSequence"));
			list.remove(dictionary.getTagFromName("SourceImageSequence"));
			list.remove(dictionary.getTagFromName("ReferencedPerformedProcedureStepSequence"));
		}

		// Sup 202
		list.replaceWithZeroLengthIfPresent(dictionary.getTagFromName("FrameOriginTimestamp"));	// OB not DT so not handled by date time code :(

		// CP 2192

		list.remove(dictionary.getTagFromName("CertificateOfSigner"));	// should not occur outside of Digital Signatures Sequence, so removing it should not be non-compliant

		// CP 2193

		list.remove(dictionary.getTagFromName("EncapsulatedDocument"));	// this will render the instance useless and non-compliant, but too hard to do better
		
		// CP 2227 - Add Application Entity Titles (AETs) to de-identification profile
		// ideally would clean when keepDeviceIdentity, but have not means to do this, so remove/replace regardless of keepDeviceIdentity
		{
			list.remove(dictionary.getTagFromName("RetrieveAETitle"));
			list.remove(dictionary.getTagFromName("StationAETitle"));
			list.remove(dictionary.getTagFromName("NetworkID"));
			list.remove(dictionary.getTagFromName("ScheduledStudyLocationAETitle"));
			list.remove(dictionary.getTagFromName("ScheduledStationAETitle"));
			list.remove(dictionary.getTagFromName("PerformedStationAETitle"));
			list.replaceWithValueIfPresent(dictionary.getTagFromName("SelectorAEValue"),replacementForAET);
			list.remove(dictionary.getTagFromName("ReceivingAE"));
			list.remove(dictionary.getTagFromName("RequestingAE"));
			list.remove(dictionary.getTagFromName("Originator"));
			list.replaceWithValueIfPresent(dictionary.getTagFromName("DestinationAE"),replacementForAET);
		}
		
		// CP 2258 - Add manufacture date and installation date to Device and Equipment Identification
		if (!keepDeviceIdentity) {
			list.remove(dictionary.getTagFromName("DateOfInstallation"));
			list.remove(dictionary.getTagFromName("DateOfManufacture"));
		}
		
		// CP 2296 - rovide additional ROI parameters to avoid parsing strings
		
		//ROIDateTime
		//ROIObservationDateTime
		list.remove(dictionary.getTagFromName("ROICreatorSequence"));
		list.remove(dictionary.getTagFromName("ROIInterpreterSequence"));

		Iterator i = list.values().iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (o instanceof SequenceAttribute) {
				SequenceAttribute a = (SequenceAttribute)o;
				if (!a.getTag().equals(dictionary.getTagFromName("ContentSequence"))) {	// i.e. do NOT descend into SR content tree
					Iterator items = a.iterator();
					if (items != null) {
						while (items.hasNext()) {
							SequenceItem item = (SequenceItem)(items.next());
							if (item != null) {
								AttributeList itemAttributeList = item.getAttributeList();
//System.err.println("Recursed into item of "+a);
								if (itemAttributeList != null) {
									removeOrNullIdentifyingAttributesRecursively(itemAttributeList,handleUIDs,keepDescriptors,keepSeriesDescriptors,keepProtocolName,keepPatientCharacteristics,keepDeviceIdentity,keepInstitutionIdentity,aggregateAgesOver89);
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * <p>For testing.</p>
	 *
	 * <p>Read a DICOM object from the file specified on the command line, and remove identifying attributes, and add sample clinical trials attributes.</p>
	 *
	 * @param	arg	two arguments, a DICOM input file and a DICOM output file
	 */
	public static void main(String arg[]) {

		System.err.println("do it buffered, looking for metaheader, no uid specified");		// no need to use SLF4J since command line utility/test
		try {
			AttributeList list = new AttributeList();
			//list.read(arg[0],null,true,true);
			list.read(arg[0]);
			System.err.println("As read ...");
			System.err.print(list.toString());
			
			list.removeUnsafePrivateAttributes();
			System.err.println("After remove unsafe private ...");
			System.err.print(list.toString());
			
			list.removePrivateAttributes();
			System.err.println("After remove private ...");
			System.err.print(list.toString());
			
			list.removeGroupLengthAttributes();
			System.err.println("After remove group lengths ...");
			System.err.print(list.toString());
			
			list.removeMetaInformationHeaderAttributes();
			System.err.println("After remove meta information header ...");
			System.err.print(list.toString());

			removeOrNullIdentifyingAttributes(list, HandleUIDs.keep,true/*keepDescriptors*/,true/*keepSeriesDescriptors*/,true/*keepPatientCharacteristics)*/,true/*keepDeviceIdentity)*/,true/*keepInstitutionIdentity)*/);
			System.err.println("After deidentify, keeping descriptions and patient characteristics and device identity and institution identity and UIDs ...");
			System.err.print(list.toString());
			
			removeOrNullIdentifyingAttributes(list, HandleUIDs.keep,true/*keepDescriptors*/,true/*keepSeriesDescriptors*/,true/*keepPatientCharacteristics)*/,true/*keepDeviceIdentity)*/,false/*keepInstitutionIdentity)*/);
			System.err.println("After deidentify, keeping descriptions and patient characteristics and device identity and UIDs ...");
			System.err.print(list.toString());
			
			removeOrNullIdentifyingAttributes(list, HandleUIDs.keep,true/*keepDescriptors*/,true/*keepSeriesDescriptors*/,true/*keepPatientCharacteristics)*/,false/*keepDeviceIdentity)*/,false/*keepInstitutionIdentity)*/);
			System.err.println("After deidentify, keeping descriptions and patient characteristics and UIDs ...");
			System.err.print(list.toString());
			
			removeOrNullIdentifyingAttributes(list, HandleUIDs.keep,false/*keepDescriptors*/,true/*keepSeriesDescriptors*/,true/*keepPatientCharacteristics)*/,false/*keepDeviceIdentity)*/,false/*keepInstitutionIdentity)*/);
			System.err.println("After deidentify, keeping patient characteristics and series description and UIDs ...");
			System.err.print(list.toString());
			
			removeOrNullIdentifyingAttributes(list, HandleUIDs.keep,false/*keepDescriptors*/,false/*keepSeriesDescriptors*/,true/*keepPatientCharacteristics)*/,false/*keepDeviceIdentity)*/,false/*keepInstitutionIdentity)*/);
			System.err.println("After deidentify, keeping patient characteristics and UIDs ...");
			System.err.print(list.toString());
			
			removeOrNullIdentifyingAttributes(list, HandleUIDs.keep,false/*keepDescriptors*/,false/*keepSeriesDescriptors*/,false/*keepPatientCharacteristics)*/,false/*keepDeviceIdentity)*/,false/*keepInstitutionIdentity)*/);
			System.err.println("After deidentify, keeping only UIDs ...");
			System.err.print(list.toString());
			
			removeOrNullIdentifyingAttributes(list,HandleUIDs.remap,false/*keepDescriptors*/,false/*keepSeriesDescriptors*/,false/*keepPatientCharacteristics)*/,false/*keepDeviceIdentity)*/,false/*keepInstitutionIdentity)*/);
			System.err.println("After deidentify, remapping UIDs ...");
			System.err.print(list.toString());
			
			Date epochForDateModification = null;
			{
				Calendar epoch = new GregorianCalendar(2000,0,1);	// NB. month is zero based
				epoch.setTimeZone(TimeZone.getTimeZone("GMT"));
				epochForDateModification = epoch.getTime();
			}
			removeOrNullIdentifyingAttributes(list,HandleUIDs.remap,false/*keepDescriptors*/,false/*keepSeriesDescriptors*/,false/*keepPatientCharacteristics)*/,false/*keepDeviceIdentity)*/,false/*keepInstitutionIdentity)*/,HandleDates.modify,epochForDateModification,null/*earliestDateInSet*/);
			System.err.println("After deidentify, modifying dates and times ...");
			System.err.print(list.toString());
			
			removeOrNullIdentifyingAttributes(list,HandleUIDs.remove,false/*keepDescriptors*/,false/*keepSeriesDescriptors*/,false/*keepPatientCharacteristics)*/,false/*keepDeviceIdentity)*/,false/*keepInstitutionIdentity)*/,HandleDates.remove,null/*epochForDateModification*/,null/*earliestDateInSet*/);
			System.err.println("After deidentify, removing everything ...");
			System.err.print(list.toString());
			{
				// need to create minimal set of UIDs to be valid
				// should probably also do FrameOfReferenceUID
				String studyID = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("StudyID"));
				String seriesNumber = Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("SeriesNumber"));
				String instanceNumber =  Attribute.getSingleStringValueOrEmptyString(list,dictionary.getTagFromName("InstanceNumber"));
				UIDGenerator u = new UIDGenerator();	
				{ Attribute a = new UniqueIdentifierAttribute(dictionary.getTagFromName("SOPInstanceUID")); a.addValue(u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber)); list.put(a); }
				{ Attribute a = new UniqueIdentifierAttribute(dictionary.getTagFromName("SeriesInstanceUID")); a.addValue(u.getNewSeriesInstanceUID(studyID,seriesNumber)); list.put(a); }
				{ Attribute a = new UniqueIdentifierAttribute(dictionary.getTagFromName("StudyInstanceUID")); a.addValue(u.getNewStudyInstanceUID(studyID)); list.put(a); }
			}
			
			addClinicalTrialsAttributes(list,true/*replaceConventionalAttributes*/,
				"ourSponsorName",
				"ourProtocolID",
				"ourProtocolName",
				"ourSiteID",
				"ourSiteName",
				"ourSubjectID",
				"ourSubjectReadingID",
				"ourTimePointID",
				"ourTimePointDescription",
				"ourCoordinatingCenterName");
			
			System.err.println("After addClinicalTrialsAttributes ...");
			System.err.print(list.toString());

			String[] operatorNames = { "smithj","doej" };
			CodedSequenceItem[] operatorPersonIdentificationCodeSequence1 =  { new CodedSequenceItem("634872364","99MYH","Smith^John") };
			CodedSequenceItem[] operatorPersonIdentificationCodeSequence2 =  { new CodedSequenceItem("346234622","99MYH","Doe^Jane") };
			String[] phoneNumbers1 = { "555-1212" };
			PersonIdentification[] operatorIdentifications =  new PersonIdentification[2];
			operatorIdentifications[0] = new PersonIdentification(operatorPersonIdentificationCodeSequence1,"John address",phoneNumbers1,null,"My hospital address",new CodedSequenceItem("47327864","99MYH","My Hospital"));
			operatorIdentifications[1] = new PersonIdentification(operatorPersonIdentificationCodeSequence2,"Jane address",phoneNumbers1,"My hospital","My hospital address",(CodedSequenceItem)null);
			//operatorIdentifications[1] = new PersonIdentification(operatorPersonIdentificationCodeSequence2,null,null,"My hospital",null,(CodedSequenceItem)null);
			//operatorIdentifications[1] = new PersonIdentification(operatorPersonIdentificationCodeSequence2,"Jane address",phoneNumbers1,null,"My hospital address",(CodedSequenceItem)null);
			
			addContributingEquipmentSequence(list,
				true,
				new CodedSequenceItem("109104","DCM","De-identifying Equipment"),
				"PixelMed",														// Manufacturer
				"PixelMed",														// Institution Name
				"Software Development",											// Institutional Department Name
				"Bangor, PA",													// Institution Address
				null,															// Station Name
				"com.pixelmed.dicom.ClinicalTrialsAttributes.main()",			// Manufacturer's Model Name
				null,															// Device Serial Number
				"Vers. 20080429",												// Software Version(s)
				"Deidentified",
				DateTimeAttribute.getFormattedStringDefaultTimeZone(new Date()),
				operatorNames,
				operatorIdentifications);
			
			System.err.println("After addContributingEquipmentSequence ...");
			System.err.print(list.toString());

			list.remove(dictionary.getTagFromName("DataSetTrailingPadding"));
			list.correctDecompressedImagePixelModule();
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			list.write(arg[1],TransferSyntax.ExplicitVRLittleEndian,true,true);
		} catch (Exception e) {
			e.printStackTrace(System.err);	// no need to use SLF4J since command line utility/test
		}
	}

}

