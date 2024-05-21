/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeFactory;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.ClinicalTrialsAttributes;
import com.pixelmed.dicom.CodeStringAttribute;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.ValueRepresentation;

import java.io.InputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.*;

public class TestDeidentify extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestDeidentify(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestStringUtilities.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestDeidentify");
		
		suite.addTest(new TestDeidentify("TestDeidentify_NoOptions_CurrentAnnexE"));
		suite.addTest(new TestDeidentify("TestDeidentify_NoOptions_Aryanto2016"));

		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private static final DicomDictionary dictionary = DicomDictionary.StandardDictionary;

	private String xmlConfidentialityProfileDataElementsResourceName = "/com/pixelmed/dicom/confidentialityprofiledataelementsandhandling.xml";

	private Document readDataElementHandlingFile() throws IOException, ParserConfigurationException, SAXException {
		InputStream i = this.getClass().getResourceAsStream(xmlConfidentialityProfileDataElementsResourceName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(i);
	}
	
	private Set<AttributeTag> getAttributeTagsOfDataElementsFromFile() throws IOException, ParserConfigurationException, SAXException {
//System.err.println("getAttributeTagsOfDataElementsFromFile():");
		Set<AttributeTag> tags = new HashSet<AttributeTag>();
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
						System.err.println("Ignoring "+name);
					}
				}
			}
		}
		return tags;
	}
	
	private Set<AttributeTag> getAttributeTagsOfDataElementsFromAryanto2016Paper() {
		Set<AttributeTag> tags = new HashSet<AttributeTag>();
		tags.add(dictionary.getTagFromName("StudyDate"));
		tags.add(dictionary.getTagFromName("SeriesDate"));
		tags.add(dictionary.getTagFromName("AcquisitionDate"));
		tags.add(dictionary.getTagFromName("ContentDate"));
		tags.add(dictionary.getTagFromName("OverlayDate"));
		tags.add(dictionary.getTagFromName("CurveDate"));
		tags.add(dictionary.getTagFromName("AcquisitionDateTime"));
		tags.add(dictionary.getTagFromName("StudyTime"));
		tags.add(dictionary.getTagFromName("SeriesTime"));
		tags.add(dictionary.getTagFromName("AcquisitionTime"));
		tags.add(dictionary.getTagFromName("ContentTime"));
		tags.add(dictionary.getTagFromName("OverlayTime"));
		tags.add(dictionary.getTagFromName("CurveTime"));
		tags.add(dictionary.getTagFromName("AccessionNumber"));
		tags.add(dictionary.getTagFromName("InstitutionName"));
		tags.add(dictionary.getTagFromName("InstitutionAddress"));
		tags.add(dictionary.getTagFromName("ReferringPhysicianName"));
		tags.add(dictionary.getTagFromName("ReferringPhysicianAddress"));
		tags.add(dictionary.getTagFromName("ReferringPhysicianTelephoneNumbers"));
		tags.add(dictionary.getTagFromName("ReferringPhysicianIdentificationSequence"));
		tags.add(dictionary.getTagFromName("InstitutionalDepartmentName"));
		tags.add(dictionary.getTagFromName("PhysiciansOfRecord"));
		tags.add(dictionary.getTagFromName("PhysiciansOfRecordIdentificationSequence"));
		tags.add(dictionary.getTagFromName("PerformingPhysicianName"));
		tags.add(dictionary.getTagFromName("PerformingPhysicianIdentificationSequence"));
		tags.add(dictionary.getTagFromName("NameOfPhysiciansReadingStudy"));
		tags.add(dictionary.getTagFromName("PhysiciansReadingStudyIdentificationSequence"));
		tags.add(dictionary.getTagFromName("OperatorsName"));
		tags.add(dictionary.getTagFromName("PatientName"));
		tags.add(dictionary.getTagFromName("PatientID"));
		tags.add(dictionary.getTagFromName("IssuerOfPatientID"));
		tags.add(dictionary.getTagFromName("PatientBirthDate"));
		tags.add(dictionary.getTagFromName("PatientBirthTime"));
		tags.add(dictionary.getTagFromName("PatientSex"));
		tags.add(dictionary.getTagFromName("OtherPatientIDs"));
		tags.add(dictionary.getTagFromName("OtherPatientNames"));
		tags.add(dictionary.getTagFromName("PatientBirthName"));
		tags.add(dictionary.getTagFromName("PatientAge"));
		tags.add(dictionary.getTagFromName("PatientAddress"));
		tags.add(dictionary.getTagFromName("PatientMotherBirthName"));
		tags.add(dictionary.getTagFromName("CountryOfResidence"));
		tags.add(dictionary.getTagFromName("RegionOfResidence"));
		tags.add(dictionary.getTagFromName("PatientTelephoneNumbers"));
		tags.add(dictionary.getTagFromName("StudyID"));
		tags.add(dictionary.getTagFromName("CurrentPatientLocation"));
		tags.add(dictionary.getTagFromName("PatientInstitutionResidence"));
		tags.add(dictionary.getTagFromName("DateTime"));
		tags.add(dictionary.getTagFromName("Date"));
		tags.add(dictionary.getTagFromName("Time"));
		tags.add(dictionary.getTagFromName("PersonName"));
		return tags;
	}
	
	private static final byte[] dirtyByteArray = {0x45, 0x47, 0x45, 0x47, 0x45, 0x47, 0x45, 0x47};
	private static final short[] dirtyShortArray = {0x4547, 0x4547, 0x4547, 0x4547};

	//private void createAttributeAndValuesAppropiateForVRAndAddToList(AttributeTag tag,AttributeList list,Map<AttributeTag,String> identifiableValues,Map<AttributeTag,String> expectedCleanedValues) throws DicomException {
	private void createAttributeAndValuesAppropiateForVRAndAddToList(AttributeTag tag,AttributeList list,Map<AttributeTag,String> identifiableValues) throws DicomException {
//System.err.println("createAttributeAndValuesAppropiateForVRAndAddToList(): tag = "+tag);
		Attribute a = AttributeFactory.newAttribute(tag);
		String identifiableValue = "";
		String expectedCleanedValue = "";
		byte[] vr = a.getVR();
		if (ValueRepresentation.isApplicationEntityVR(vr)) {
			identifiableValue = "DIRTY";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isAgeStringVR(vr)) {
			identifiableValue = "037Y";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isAttributeTagVR(vr)) {
		}
		else if (ValueRepresentation.isCodeStringVR(vr)) {
			identifiableValue = "DIRTY";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isDateVR(vr)) {
			identifiableValue = "20180927";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isDateTimeVR(vr)) {
			identifiableValue = "20180927180143";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isDecimalStringVR(vr)) {
			identifiableValue = "37.2";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isFloatDoubleVR(vr)) {
			identifiableValue = "37.2";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isFloatSingleVR(vr)) {
			identifiableValue = "37.2";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isIntegerStringVR(vr)) {
			identifiableValue = "37";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isLongStringVR(vr)) {
			identifiableValue = "DIRTY";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isLongTextVR(vr)) {
			identifiableValue = "DIRTY";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isOtherByteVR(vr)) {
			a.setValues(dirtyByteArray);
		}
		else if (ValueRepresentation.isOtherDoubleVR(vr)) {
			a.setValues(dirtyByteArray);
		}
		else if (ValueRepresentation.isOtherFloatVR(vr)) {
			a.setValues(dirtyByteArray);
		}
		else if (ValueRepresentation.isOtherLongVR(vr)) {
			a.setValues(dirtyByteArray);
		}
		else if (ValueRepresentation.isOtherWordVR(vr)) {
			a.setValues(dirtyShortArray);
		}
		else if (ValueRepresentation.isOtherUnspecifiedVR(vr)) {
			a.setValues(dirtyByteArray);
		}
		else if (ValueRepresentation.isPersonNameVR(vr)) {
			identifiableValue = "SMITH^HARRY";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isSequenceVR(vr)) {
			AttributeList itemList = new AttributeList();
			{ Attribute itemAttribute = new CodeStringAttribute(TagFromName.ImageType); itemAttribute.addValue("DIRTY"); itemList.put(itemAttribute); }
			((SequenceAttribute)a).addItem(itemList);
		}
		else if (ValueRepresentation.isShortStringVR(vr)) {
			identifiableValue = "DIRTY";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isSignedLongVR(vr)) {
			identifiableValue = "37";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isSignedShortVR(vr)) {
			identifiableValue = "37";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isShortTextVR(vr)) {
			identifiableValue = "DIRTY";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isTimeVR(vr)) {
			identifiableValue = "180143";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isUniqueIdentifierVR(vr)) {
			identifiableValue = "1.2.3.4";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isUnsignedLongVR(vr)) {
			identifiableValue = "37";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isUnknownVR(vr)) {
		}
		else if (ValueRepresentation.isUnsignedShortVR(vr)) {
			identifiableValue = "37";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isUnspecifiedShortVR(vr)) {
			identifiableValue = "37";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isUnspecifiedShortOrOtherWordVR(vr)) {
			identifiableValue = "37";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isUniversalResourceVR(vr)) {
			identifiableValue = "http://www.pixelmed.com/";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isUnlimitedCharactersVR(vr)) {
			identifiableValue = "DIRTY";
			expectedCleanedValue = "";
		}
		else if (ValueRepresentation.isUnlimitedTextVR(vr)) {
			identifiableValue = "DIRTY";
			expectedCleanedValue = "";
		}
		else {
		}
		if (identifiableValue.length() > 0) {
			a.addValue(identifiableValue);
		}
		if (a.getVL() == 0) {
			System.err.println("No value for unsupported VR for "+tag);
		}
		list.put(a);
		identifiableValues.put(tag,identifiableValue);
		//expectedCleanedValues.put(tag,expectedCleanedValue);
	}
	
	private static final AttributeTag curveDataTag = new AttributeTag(0x5000,0x0000);
	private static final AttributeTag overlayDataTag = new AttributeTag(0x6000,0x3000);
	private static final AttributeTag graphicAnnotationSequence = TagFromName.GraphicAnnotationSequence;
	private static final AttributeTag acquisitionContextSequence = TagFromName.AcquisitionContextSequence;
	private static final AttributeTag contentSequence = TagFromName.ContentSequence;

	private void checkIsDeidentifiedproperly(AttributeList list,Set<AttributeTag> tags,Map<AttributeTag,String> identifiableValues) {
		for (AttributeTag tag : tags) {
			//System.err.println("Testing "+tag);
			if (tag.equals(curveDataTag)) {
				System.err.println("Not checking curveDataTag "+tag);
			}
			else if (tag.equals(overlayDataTag)) {
				System.err.println("Not checking overlayDataTag "+tag);
			}
			else if (tag.equals(graphicAnnotationSequence)) {
				System.err.println("Not checking graphicAnnotationSequence "+tag);
			}
			else if (tag.equals(acquisitionContextSequence)) {
				System.err.println("Not checking acquisitionContextSequence "+tag);
			}
			else if (tag.equals(contentSequence)) {
				System.err.println("Not checking contentSequence "+tag);
			}
			else {
				byte[] vr = dictionary.getValueRepresentationFromTag(tag);
				if (vr == null
					|| ValueRepresentation.isUnknownVR(vr)
					|| ValueRepresentation.isOtherByteVR(vr)
					|| ValueRepresentation.isOtherDoubleVR(vr)
					|| ValueRepresentation.isOtherFloatVR(vr)
					|| ValueRepresentation.isOtherLongVR(vr)
					|| ValueRepresentation.isOtherWordVR(vr)
					|| ValueRepresentation.isOtherUnspecifiedVR(vr)
					) {
					Attribute a = list.get(tag);
					assertTrue("Checking attribute has been removed or is empty "+tag+" VR "+(vr == null ? "null" : ValueRepresentation.getAsString(vr)),a == null || a.getVL() == 0);
				}
				else if (ValueRepresentation.isSequenceVR(vr)) {
					Attribute a = list.get(tag);
					assertTrue("Checking sequence attribute has been removed or is empty "+tag,a == null || ((SequenceAttribute)a).getNumberOfItems() == 0);
				}
				else {
					String actualValue = Attribute.getSingleStringValueOrEmptyString(list,tag);
					String identifiableValue = identifiableValues.get(tag);
					//if (ValueRepresentation.isUniqueIdentifierVR(vr)) {
					// just needs to be different
					//System.err.println("For "+tag+" expected something other than <"+identifiableValue+"> got <"+actualValue+">");
					assertTrue("Checking is different value for "+tag,!actualValue.equals(identifiableValue));
					//else {
					//	assertEquals("Checking "+tag,expectedCleanedValues.get(tag),actualValue);
					//}
				}
			}
		}
	}

	public void TestDeidentify_NoOptions_CurrentAnnexE() throws Exception  {
		Set<AttributeTag> tags = getAttributeTagsOfDataElementsFromFile();
		AttributeList list = new AttributeList();
		Map<AttributeTag,String> identifiableValues = new HashMap<AttributeTag,String>();
		//Map<AttributeTag,String> expectedCleanedValues = new HashMap<AttributeTag,String>();
		for (AttributeTag tag : tags) {
			//createAttributeAndValuesAppropiateForVRAndAddToList(tag,list,identifiableValues,expectedCleanedValues);
			createAttributeAndValuesAppropiateForVRAndAddToList(tag,list,identifiableValues);
		}
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,
																   ClinicalTrialsAttributes.HandleUIDs.remove,
																   false/*keepDescriptors*/,
																   false/*keepSeriesDescriptors*/,
																   false/*keepProtocolName*/,
																   false/*keepPatientCharacteristics)*/,
																   false/*keepDeviceIdentity)*/,
																   false/*keepInstitutionIdentity)*/,
																   ClinicalTrialsAttributes.HandleDates.remove,null/*epochForDateModification*/,null/*earliestDateInSet*/);
		ClinicalTrialsAttributes.removeClinicalTrialsAttributes(list);
		checkIsDeidentifiedproperly(list,tags,identifiableValues);
	}

	public void TestDeidentify_NoOptions_Aryanto2016() throws Exception  {
		Set<AttributeTag> tags = getAttributeTagsOfDataElementsFromAryanto2016Paper();
		AttributeList list = new AttributeList();
		Map<AttributeTag,String> identifiableValues = new HashMap<AttributeTag,String>();
		//Map<AttributeTag,String> expectedCleanedValues = new HashMap<AttributeTag,String>();
		for (AttributeTag tag : tags) {
			//createAttributeAndValuesAppropiateForVRAndAddToList(tag,list,identifiableValues,expectedCleanedValues);
			createAttributeAndValuesAppropiateForVRAndAddToList(tag,list,identifiableValues);
		}
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,
																   ClinicalTrialsAttributes.HandleUIDs.remove,
																   false/*keepDescriptors*/,
																   false/*keepSeriesDescriptors*/,
																   false/*keepProtocolName*/,
																   false/*keepPatientCharacteristics)*/,
																   false/*keepDeviceIdentity)*/,
																   false/*keepInstitutionIdentity)*/,
																   ClinicalTrialsAttributes.HandleDates.remove,null/*epochForDateModification*/,null/*earliestDateInSet*/);
		checkIsDeidentifiedproperly(list,tags,identifiableValues);
	}
}
