/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.ClinicalTrialsAttributes;
import com.pixelmed.dicom.CodedSequenceItem;
import com.pixelmed.dicom.ContentItem;
import com.pixelmed.dicom.ContentItemFactory;
import com.pixelmed.dicom.ContentItemWithReference;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.StructuredReport;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.XMLRepresentationOfStructuredReportObjectFactory;

import java.util.Iterator;

import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import junit.framework.*;

public class TestDeidentifyStructuredContent extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestDeidentifyStructuredContent(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestStringUtilities.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestDeidentifyStructuredContent");
		
		suite.addTest(new TestDeidentifyStructuredContent("TestDeidentifyStructuredContent_KeepAll_FromSR"));
		suite.addTest(new TestDeidentifyStructuredContent("TestDeidentifyStructuredContent_RemoveAll_FromSR"));
		suite.addTest(new TestDeidentifyStructuredContent("TestDeidentifyStructuredContent_Modify_FromSR"));
		suite.addTest(new TestDeidentifyStructuredContent("TestDeidentifyStructuredContent_Modify_FromSR_KeepDescriptors"));
		suite.addTest(new TestDeidentifyStructuredContent("TestDeidentifyStructuredContent_Modify_FromSR_KeepDevice"));
		suite.addTest(new TestDeidentifyStructuredContent("TestDeidentifyStructuredContent_Modify_FromSR_KeepInstitution"));

		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}

	private static final String defaultDescriptionValue = "This is a potential identity leakage";
	private static final String defaultPersonNameValue = "Smith^RealPerson";
	private static final String defaultUIDValue = "1.2.3.4";
	
	private static boolean checkNoEmptyContentSequenceEtAl(AttributeList list,boolean isRoot) {
		boolean isGood = true;
		String valueType = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.ValueType);
		String referencedContentItemIdentifier = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.ReferencedContentItemIdentifier);
		if (valueType.length() == 0 && referencedContentItemIdentifier.length() == 0) {
			System.err.println("No ValueType and no ReferencedContentItemIdentifier");
			isGood = false;
		}
		if (isRoot) {
			if (!valueType.equals("CONTAINER")) {
				System.err.println("Root ValueType is not CONTAINER");
				isGood = false;
			}
		}
		else {
			String relationshipType = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.RelationshipType);
			if (relationshipType.length() == 0) {
				System.err.println("No RelationshipType");
				//System.err.println(list);
				isGood = false;
			}
		}
		SequenceAttribute aContentSequence = (SequenceAttribute)(list.get(TagFromName.ContentSequence));
		if (valueType.length() > 0) {
			if (valueType.equals("CONTAINER")) {
				if (aContentSequence == null) {
					System.err.println("Missing ContentSequence for CONTAINER");
					isGood = false;
				}
			}
			else {
				// expect at least one of these ...
				if (list.get(TagFromName.TextValue) == null
					&& list.get(TagFromName.DateTime) == null
					&& list.get(TagFromName.Date) == null
					&& list.get(TagFromName.Time) == null
					&& list.get(TagFromName.PersonName) == null
					&& list.get(TagFromName.UID) == null
					&& list.get(TagFromName.MeasuredValueSequence) == null
					&& list.get(TagFromName.NumericValueQualifierCodeSequence) == null
					&& list.get(TagFromName.ConceptCodeSequence) == null
					&& list.get(TagFromName.ReferencedSOPSequence) == null
					&& list.get(TagFromName.GraphicData) == null
					&& list.get(TagFromName.GraphicType) == null
					&& list.get(TagFromName.PixelOriginInterpretation) == null
					&& list.get(TagFromName.FiducialUID) == null
					&& list.get(TagFromName.ReferencedFrameOfReferenceUID) == null
					&& list.get(TagFromName.TemporalRangeType) == null
					&& list.get(TagFromName.ReferencedSamplePositions) == null
					&& list.get(TagFromName.ReferencedTimeOffsets) == null
					&& list.get(TagFromName.ReferencedDateTime) == null
				) {
					System.err.println("No value attributes for valueType "+valueType);
					isGood = false;
				}
			}
		}

		if (aContentSequence != null) {
			Iterator items = aContentSequence.iterator();
			if (items != null) {
				while (items.hasNext()) {
					SequenceItem item = (SequenceItem)(items.next());
					if (item != null) {
						AttributeList itemAttributeList = item.getAttributeList();
						if (itemAttributeList != null && !itemAttributeList.isEmpty()) {
							isGood =  isGood && checkNoEmptyContentSequenceEtAl(itemAttributeList,false/*isRoot*/);
						}
						else {
							System.err.println("Missing or empty AttributeList in item of ContentSequence");
							isGood = false;
						}
					}
				}
			}
			else {
				System.err.println("ContentSequence without items");
				isGood = false;
			}
		}
		return isGood;
	}

	private static boolean checkNoEmptyContentSequenceEtAl(AttributeList list) {
		return checkNoEmptyContentSequenceEtAl(list,true/*isRoot*/);
	}
	
	private static AttributeList makeSR() throws DicomException {
		// derived from TestStructuredReport_XMLRepresentation
		ContentItemFactory cif = new ContentItemFactory();
		ContentItem root = cif.new ContainerContentItem(null/*no parent since root*/,null/*no relationshipType since root*/,new CodedSequenceItem("111036","DCM","Mammography CAD Report"),true/*continuityOfContentIsSeparate*/,"DCMR","5000");
		{
			//(, DCM, "")
			cif.new CodeContentItem(root,"HAS OBS CONTEXT",new CodedSequenceItem("121005","DCM","Observer Type"),new CodedSequenceItem("121006","DCM","Person"));
			cif.new PersonNameContentItem(root,"HAS OBS CONTEXT",new CodedSequenceItem("121008","DCM","Person Observer Name"),defaultPersonNameValue);
			cif.new PersonNameContentItem(root,"HAS OBS CONTEXT",new CodedSequenceItem("121435","DCM","Service Performer"),defaultPersonNameValue);
			cif.new TextContentItem(root,"HAS OBS CONTEXT",new CodedSequenceItem("121435","DCM","Service Performer"),defaultPersonNameValue);
			cif.new PersonNameContentItem(root,"HAS OBS CONTEXT",new CodedSequenceItem("7235223","99CRAZYMADEUP","Silly Name"),defaultPersonNameValue);		// test all PNAME removed
			
			cif.new CodeContentItem(root,"HAS OBS CONTEXT",new CodedSequenceItem("121005","DCM","Observer Type"),new CodedSequenceItem("121007","DCM","Device"));
			cif.new UIDContentItem(root,"HAS OBS CONTEXT",new CodedSequenceItem("121012","DCM","Device Observer UID"),defaultUIDValue);
			cif.new TextContentItem(root,"HAS OBS CONTEXT",new CodedSequenceItem("121013","DCM","Device Observer Name"),defaultDescriptionValue);
			
			cif.new TextContentItem(root,"HAS OBS CONTEXT",new CodedSequenceItem("121017","DCM","Device Observer Physical Location During Observation"),defaultDescriptionValue);
		}
		{
			ContentItem imageLibrary = cif.new ContainerContentItem(root,"CONTAINS",new CodedSequenceItem("111028","DCM","Image Library"),true/*continuityOfContentIsSeparate*/);
			ContentItem image_1_1_1 = cif.new ImageContentItem(imageLibrary,"CONTAINS",null/*conceptName*/,
															   SOPClass.DigitalMammographyXRayImageStorageForPresentation,"1.3.6.1.4.1.5962.99.1.993064428.2122236180.1358202762732.2.0",
															   0/*referencedFrameNumber*/,0/*referencedSegmentNumber*/,
															   null/*presentationStateSOPClassUID*/,null/*presentationStateSOPInstanceUID*/,
															   null/*realWorldValueMappingSOPClassUID*/,null/*realWorldValueMappingSOPInstanceUID*/);
		}
		{
			ContentItem findingsSummary = cif.new CodeContentItem(root,"CONTAINS",new CodedSequenceItem("111017","DCM","CAD Processing and Findings Summary"),new CodedSequenceItem("111242","DCM","All algorithms succeeded; with findings"));
			{
				ContentItem individual = cif.new ContainerContentItem(findingsSummary,"CONTAINS",new CodedSequenceItem("111034","DCM","Individual Impression/Recommendation"),true/*continuityOfContentIsSeparate*/);
				cif.new CodeContentItem(individual,"CONTAINS",new CodedSequenceItem("111056","DCM","Rendering Intent"),new CodedSequenceItem("111150","DCM","Presentation Required: Rendering device is expected to present"));
				{
					ContentItem cluster = cif.new CodeContentItem(individual,"CONTAINS",new CodedSequenceItem("111059","DCM","Single Image Finding"),new CodedSequenceItem("F-01775","SRT","Calcification Cluster"));
					cif.new CodeContentItem(cluster,"HAS CONCEPT MOD",new CodedSequenceItem("111056","DCM","Rendering Intent"),new CodedSequenceItem("111150","DCM","Presentation Required: Rendering device is expected to present"));
					ContentItem clusterCoord = cif.new SpatialCoordinatesContentItem(individual,"CONTAINS",new CodedSequenceItem("111010","DCM","Center"),"POINT",new float[] { 165,2433 });
					new ContentItemWithReference(clusterCoord,"SELECTED FROM","1.1.1");
					cif.new NumericContentItem(cluster,"HAS PROPERTIES",new CodedSequenceItem("111038","DCM","Number of calcifications"),10,new CodedSequenceItem("111150","1","no units"));
				}
				{
					ContentItem single = cif.new CodeContentItem(individual,"CONTAINS",new CodedSequenceItem("111059","DCM","Single Image Finding"),new CodedSequenceItem("F-01776","SRT","Individual Calcification"));
					cif.new CodeContentItem(single,"HAS CONCEPT MOD",new CodedSequenceItem("111056","DCM","Rendering Intent"),new CodedSequenceItem("111150","DCM","Presentation Required: Rendering device is expected to present"));
					ContentItem singleCenterCoord = cif.new SpatialCoordinatesContentItem(individual,"CONTAINS",new CodedSequenceItem("111010","DCM","Center"),"POINT",new float[] { 198,2389 });
					new ContentItemWithReference(singleCenterCoord,"SELECTED FROM","1.1.1");
					ContentItem singleOutlineCoord = cif.new SpatialCoordinatesContentItem(individual,"CONTAINS",new CodedSequenceItem("111041","DCM","Outline"),"POLYLINE",new float[] { 199,2388,198,2388,197,2388,197,2389,197,2390,198,2390,199,2390,200,2390,200,2389 });
					new ContentItemWithReference(singleOutlineCoord,"SELECTED FROM","1.1.1");
				}
				{
					ContentItem selected = cif.new CodeContentItem(individual,"CONTAINS",new CodedSequenceItem("111059","DCM","Single Image Finding"),new CodedSequenceItem("111099","DCM","Selected Region"));
					cif.new TextContentItem(selected,"HAS PROPERTIES",new CodedSequenceItem("111058","DCM","Selected Region Description"),defaultDescriptionValue);
				}
			}
		}
		StructuredReport sr = new StructuredReport(root);
		AttributeList list = sr.getAttributeList();
		return list;
	}
	
	public void TestDeidentifyStructuredContent_KeepAll_FromSR() throws Exception  {
		AttributeList list = makeSR();
		AttributeList expectList = makeSR();

		//System.err.println("Before removeOrNullIdentifyingAttributes ...");
		//System.err.print(list.toString());
		
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,
																   ClinicalTrialsAttributes.HandleUIDs.keep,	// else before and after comparison will fail
																   false/*keepDescriptors*/,
																   false/*keepSeriesDescriptors*/,
																   false/*keepProtocolName*/,
																   false/*keepPatientCharacteristics)*/,
																   false/*keepDeviceIdentity)*/,
																   false/*keepInstitutionIdentity)*/,
																   ClinicalTrialsAttributes.HandleDates.remove,null/*epochForDateModification*/,null/*earliestDateInSet*/,
																   ClinicalTrialsAttributes.HandleStructuredContent.keep);
		
		//System.err.println("After removeOrNullIdentifyingAttributes with HandleStructuredContent.remove ...");
		//System.err.print(list.toString());
		
		list.remove(TagFromName.PatientIdentityRemoved);
		list.remove(TagFromName.DeidentificationMethod);
		list.remove(TagFromName.DeidentificationMethodCodeSequence);
		assertEquals("Checking before and after AttributeList",expectList,list);
	}

	public void TestDeidentifyStructuredContent_RemoveAll_FromSR() throws Exception  {
		AttributeList list = makeSR();
		
		//System.err.println("Before removeOrNullIdentifyingAttributes ...");
		//System.err.print(list.toString());
		
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,
																   ClinicalTrialsAttributes.HandleUIDs.keep,
																   false/*keepDescriptors*/,
																   false/*keepSeriesDescriptors*/,
																   false/*keepProtocolName*/,
																   false/*keepPatientCharacteristics)*/,
																   false/*keepDeviceIdentity)*/,
																   false/*keepInstitutionIdentity)*/,
																   ClinicalTrialsAttributes.HandleDates.remove,null/*epochForDateModification*/,null/*earliestDateInSet*/,
																   ClinicalTrialsAttributes.HandleStructuredContent.remove);
		
		//System.err.println("After removeOrNullIdentifyingAttributes with HandleStructuredContent.remove ...");
		//System.err.print(list.toString());
		
		assertTrue("Checking no ContentSequence ",list.get(TagFromName.ContentSequence) == null);
		assertTrue("Checking no ConceptNameCodeSequence ",list.get(TagFromName.ConceptNameCodeSequence) == null);
	}
	
	public void TestDeidentifyStructuredContent_Modify_FromSR() throws Exception  {
		AttributeList list = makeSR();
		
		//System.err.println("Before removeOrNullIdentifyingAttributes ...");
		//System.err.print(list.toString());
		
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,
																   ClinicalTrialsAttributes.HandleUIDs.remap,	// N.B.
																   false/*keepDescriptors*/,
																   false/*keepSeriesDescriptors*/,
																   false/*keepProtocolName*/,
																   false/*keepPatientCharacteristics)*/,
																   false/*keepDeviceIdentity)*/,
																   false/*keepInstitutionIdentity)*/,
																   ClinicalTrialsAttributes.HandleDates.remove,null/*epochForDateModification*/,null/*earliestDateInSet*/,
																   ClinicalTrialsAttributes.HandleStructuredContent.modify);
		
		//System.err.println("After removeOrNullIdentifyingAttributes with HandleStructuredContent.remove ...");
		//System.err.print(list.toString());
		
		assertTrue("Checking there is a top level ContentSequence ",list.get(TagFromName.ContentSequence) != null);
		assertTrue("Checking there is a top level ConceptNameCodeSequence ",list.get(TagFromName.ConceptNameCodeSequence) != null);

		{
			StructuredReport sr = new StructuredReport(list);
			//System.err.print("sr =\n"+sr);
			assertFalse("Checking SR is not null",sr == null);
			Document srDocument = new XMLRepresentationOfStructuredReportObjectFactory().getDocument(sr,list);
			assertFalse("Checking SR document is not null",srDocument == null);
			XPathFactory xpf = XPathFactory.newInstance();
			
			// not changed ...

			// changed ...
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/CODE ("111017","DCM","CAD Processing and Findings Summary") = ("111242","DCM","All algorithms succeeded; with findings")/CONTAINER ("111034","DCM","Individual Impression/Recommendation")/CODE ("111059","DCM","Single Image Finding") = ("111099","DCM","Selected Region")/("111058","DCM","Selected Region Description") = "This is a potential identity leakage"
			assertEquals("Selected Region Description",ClinicalTrialsAttributes.replacementForTextInStructuredContent,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/code[concept/@cv='111017']/container[concept/@cv='111034']/code[concept/@cv='111059']/text[concept/@cv='111058']/value",srDocument));

			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/ PNAME ("121008","DCM","Person Observer Name") = "Smith^RealPerson"
			assertEquals("Person Observer Name",ClinicalTrialsAttributes.replacementForPersonNameInStructuredContent,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/pname[concept/@cv='121008']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121012","DCM","Device Observer UID") = "1.2.3.4"
			assertTrue("Device Observer UID",!defaultUIDValue.equals(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/uidref[concept/@cv='121012']/value",srDocument)));

			// removed ...
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121435","DCM","Service Performer") = "Smith^RealPerson"
			assertEquals("Service Performer as PNAME removed","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/pname[concept/@cv='121435']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/TEXT ("121435","DCM","Service Performer") = "Smith^RealPerson"
			assertEquals("Service Performer as TEXT removed","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/text[concept/@cv='121435']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("7235223","99CRAZYMADEUP","Silly Name") = "Smith^RealPerson"
			assertEquals("Unrecognized PNAME removed","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/pname[concept/@cv='7235223']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121013","DCM","Device Observer Name") = "This is a potential identity leakage"
			assertEquals("Device Observer Name","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/text[concept/@cv='121013']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/TEXT("121017","DCM","Device Observer Physical Location During Observation") = "This is a potential identity leakage"
			assertEquals("Device Observer Physical Location During Observation","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/text[concept/@cv='121017']/value",srDocument));

		}
	}
	
	public void TestDeidentifyStructuredContent_Modify_FromSR_KeepDescriptors() throws Exception  {
		AttributeList list = makeSR();
		
		//System.err.println("Before removeOrNullIdentifyingAttributes ...");
		//System.err.print(list.toString());
		
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,
																   ClinicalTrialsAttributes.HandleUIDs.remap,	// N.B.
																   true/*keepDescriptors*/,
																   false/*keepSeriesDescriptors*/,
																   false/*keepProtocolName*/,
																   false/*keepPatientCharacteristics)*/,
																   false/*keepDeviceIdentity)*/,
																   false/*keepInstitutionIdentity)*/,
																   ClinicalTrialsAttributes.HandleDates.remove,null/*epochForDateModification*/,null/*earliestDateInSet*/,
																   ClinicalTrialsAttributes.HandleStructuredContent.modify);
		
		//System.err.println("After removeOrNullIdentifyingAttributes with HandleStructuredContent.remove ...");
		//System.err.print(list.toString());
		
		assertTrue("Checking there is a top level ContentSequence ",list.get(TagFromName.ContentSequence) != null);
		assertTrue("Checking there is a top level ConceptNameCodeSequence ",list.get(TagFromName.ConceptNameCodeSequence) != null);
		
		{
			StructuredReport sr = new StructuredReport(list);
			//System.err.print("sr =\n"+sr);
			assertFalse("Checking SR is not null",sr == null);
			Document srDocument = new XMLRepresentationOfStructuredReportObjectFactory().getDocument(sr,list);
			assertFalse("Checking SR document is not null",srDocument == null);
			XPathFactory xpf = XPathFactory.newInstance();
			
			// not changed ...
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/CODE ("111017","DCM","CAD Processing and Findings Summary") = ("111242","DCM","All algorithms succeeded; with findings")/CONTAINER ("111034","DCM","Individual Impression/Recommendation")/CODE ("111059","DCM","Single Image Finding") = ("111099","DCM","Selected Region")/("111058","DCM","Selected Region Description") = "This is a potential identity leakage"
			assertEquals("Selected Region Description",defaultDescriptionValue,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/code[concept/@cv='111017']/container[concept/@cv='111034']/code[concept/@cv='111059']/text[concept/@cv='111058']/value",srDocument));

			// changed ...
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121008","DCM","Person Observer Name") = "Smith^RealPerson"
			assertEquals("Person Observer Name",ClinicalTrialsAttributes.replacementForPersonNameInStructuredContent,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/pname[concept/@cv='121008']/value",srDocument));

			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121012","DCM","Device Observer UID") = "1.2.3.4"
			assertTrue("Device Observer UID",!defaultUIDValue.equals(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/uidref[concept/@cv='121012']/value",srDocument)));

			// removed ...
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121435","DCM","Service Performer") = "Smith^RealPerson"
			assertEquals("Service Performer as PNAME removed","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/pname[concept/@cv='121435']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/TEXT ("121435","DCM","Service Performer") = "Smith^RealPerson"
			assertEquals("Service Performer as TEXT removed","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/text[concept/@cv='121435']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("7235223","99CRAZYMADEUP","Silly Name") = "Smith^RealPerson"
			assertEquals("Unrecognized PNAME removed","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/pname[concept/@cv='7235223']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121013","DCM","Device Observer Name") = "This is a potential identity leakage"
			assertEquals("Device Observer Name","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/text[concept/@cv='121013']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/TEXT("121017","DCM","Device Observer Physical Location During Observation") = "This is a potential identity leakage"
			assertEquals("Device Observer Physical Location During Observation","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/text[concept/@cv='121017']/value",srDocument));

		}
	}
	
	public void TestDeidentifyStructuredContent_Modify_FromSR_KeepDevice() throws Exception  {
		AttributeList list = makeSR();
		
		//System.err.println("Before removeOrNullIdentifyingAttributes ...");
		//System.err.print(list.toString());
		
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,
																   ClinicalTrialsAttributes.HandleUIDs.remap,	// N.B. will cause Device UID to be changed
																   false/*keepDescriptors*/,
																   false/*keepSeriesDescriptors*/,
																   false/*keepProtocolName*/,
																   false/*keepPatientCharacteristics)*/,
																   true/*keepDeviceIdentity)*/,
																   false/*keepInstitutionIdentity)*/,
																   ClinicalTrialsAttributes.HandleDates.remove,null/*epochForDateModification*/,null/*earliestDateInSet*/,
																   ClinicalTrialsAttributes.HandleStructuredContent.modify);
		
		//System.err.println("After removeOrNullIdentifyingAttributes with HandleStructuredContent.remove ...");
		//System.err.print(list.toString());
		
		assertTrue("Checking there is a top level ContentSequence ",list.get(TagFromName.ContentSequence) != null);
		assertTrue("Checking there is a top level ConceptNameCodeSequence ",list.get(TagFromName.ConceptNameCodeSequence) != null);
		
		{
			StructuredReport sr = new StructuredReport(list);
			//System.err.print("sr =\n"+sr);
			assertFalse("Checking SR is not null",sr == null);
			Document srDocument = new XMLRepresentationOfStructuredReportObjectFactory().getDocument(sr,list);
			assertFalse("Checking SR document is not null",srDocument == null);
			XPathFactory xpf = XPathFactory.newInstance();
			
			// not changed ...
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121013","DCM","Device Observer Name") = "This is a potential identity leakage"
			assertEquals("Device Observer Name",defaultDescriptionValue,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/text[concept/@cv='121013']/value",srDocument));
			
			// changed ...
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/CODE ("111017","DCM","CAD Processing and Findings Summary") = ("111242","DCM","All algorithms succeeded; with findings")/CONTAINER ("111034","DCM","Individual Impression/Recommendation")/CODE ("111059","DCM","Single Image Finding") = ("111099","DCM","Selected Region")/("111058","DCM","Selected Region Description") = "This is a potential identity leakage"
			assertEquals("Selected Region Description",ClinicalTrialsAttributes.replacementForTextInStructuredContent,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/code[concept/@cv='111017']/container[concept/@cv='111034']/code[concept/@cv='111059']/text[concept/@cv='111058']/value",srDocument));

			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121008","DCM","Person Observer Name") = "Smith^RealPerson"
			assertEquals("Person Observer Name",ClinicalTrialsAttributes.replacementForPersonNameInStructuredContent,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/pname[concept/@cv='121008']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121012","DCM","Device Observer UID") = "1.2.3.4"
			assertTrue("Device Observer UID",!defaultUIDValue.equals(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/uidref[concept/@cv='121012']/value",srDocument)));
			
			// removed ...
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121435","DCM","Service Performer") = "Smith^RealPerson"
			assertEquals("Service Performer as PNAME removed","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/pname[concept/@cv='121435']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/TEXT ("121435","DCM","Service Performer") = "Smith^RealPerson"
			assertEquals("Service Performer as TEXT removed","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/text[concept/@cv='121435']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("7235223","99CRAZYMADEUP","Silly Name") = "Smith^RealPerson"
			assertEquals("Unrecognized PNAME removed","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/pname[concept/@cv='7235223']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/TEXT("121017","DCM","Device Observer Physical Location During Observation") = "This is a potential identity leakage"
			assertEquals("Device Observer Physical Location During Observation","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/text[concept/@cv='121017']/value",srDocument));

		}
	}
	
	public void TestDeidentifyStructuredContent_Modify_FromSR_KeepInstitution() throws Exception  {
		AttributeList list = makeSR();
		
		//System.err.println("Before removeOrNullIdentifyingAttributes ...");
		//System.err.print(list.toString());
		
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,
																   ClinicalTrialsAttributes.HandleUIDs.remap,	// N.B.
																   false/*keepDescriptors*/,
																   false/*keepSeriesDescriptors*/,
																   false/*keepProtocolName*/,
																   false/*keepPatientCharacteristics)*/,
																   false/*keepDeviceIdentity)*/,
																   true/*keepInstitutionIdentity)*/,
																   ClinicalTrialsAttributes.HandleDates.remove,null/*epochForDateModification*/,null/*earliestDateInSet*/,
																   ClinicalTrialsAttributes.HandleStructuredContent.modify);
		
		//System.err.println("After removeOrNullIdentifyingAttributes with HandleStructuredContent.remove ...");
		//System.err.print(list.toString());
		
		assertTrue("Checking there is a top level ContentSequence ",list.get(TagFromName.ContentSequence) != null);
		assertTrue("Checking there is a top level ConceptNameCodeSequence ",list.get(TagFromName.ConceptNameCodeSequence) != null);
		
		assertTrue("Checking no empty ContentSequence items after de-identification ",checkNoEmptyContentSequenceEtAl(list));
		
		{
			StructuredReport sr = new StructuredReport(list);
			//System.err.print("sr =\n"+sr);
			assertFalse("Checking SR is not null",sr == null);
			Document srDocument = new XMLRepresentationOfStructuredReportObjectFactory().getDocument(sr,list);
			assertFalse("Checking SR document is not null",srDocument == null);
			XPathFactory xpf = XPathFactory.newInstance();

			// not changed ...
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/CODE ("121005","DCM","Observer Type") = ("121006","DCM","Person")
			assertEquals("Observer is PERSON","121006",CodedSequenceItem.getSingleCodedSequenceItemOrNull(((SequenceAttribute)(list.get(TagFromName.ContentSequence))).getItem(0).getAttributeList(),TagFromName.ConceptCodeSequence).getCodeValue());
			{
				SequenceAttribute contentSequence = (SequenceAttribute)(list.get(TagFromName.ContentSequence));
				int itemNumber = CodedSequenceItem.getItemNumberContainingCodeSequence(contentSequence,TagFromName.ConceptNameCodeSequence,new CodedSequenceItem("121005","DCM","Observer Type"));
				assertEquals("Observer is PERSON","121006",CodedSequenceItem.getSingleCodedSequenceItemOrNull(contentSequence.getItem(itemNumber).getAttributeList(),TagFromName.ConceptCodeSequence).getCodeValue());
			}
			assertEquals("Observer is PERSON","121006",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/code[concept/@cv='121005']/value/@cv",srDocument));

			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/TEXT("121017","DCM","Device Observer Physical Location During Observation") = "This is a potential identity leakage"
			assertEquals("Device Observer Physical Location During Observation",defaultDescriptionValue,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/text[concept/@cv='121017']/value",srDocument));

			// changed ...
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/CODE ("111017","DCM","CAD Processing and Findings Summary") = ("111242","DCM","All algorithms succeeded; with findings")/CONTAINER ("111034","DCM","Individual Impression/Recommendation")/CODE ("111059","DCM","Single Image Finding") = ("111099","DCM","Selected Region")/("111058","DCM","Selected Region Description") = "This is a potential identity leakage"
			assertEquals("Selected Region Description",ClinicalTrialsAttributes.replacementForTextInStructuredContent,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/code[concept/@cv='111017']/container[concept/@cv='111034']/code[concept/@cv='111059']/text[concept/@cv='111058']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121008","DCM","Person Observer Name") = "Smith^RealPerson"
			assertEquals("Person Observer Name",ClinicalTrialsAttributes.replacementForPersonNameInStructuredContent,((SequenceAttribute)(list.get(TagFromName.ContentSequence))).getItem(1).getAttributeList().get(TagFromName.PersonName).getSingleStringValueOrEmptyString());
			assertEquals("Person Observer Name",ClinicalTrialsAttributes.replacementForPersonNameInStructuredContent,xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/pname[concept/@cv='121008']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121012","DCM","Device Observer UID") = "1.2.3.4"
			assertTrue("Device Observer UID",!defaultUIDValue.equals(xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/uidref[concept/@cv='121012']/value",srDocument)));
			
			// removed ...
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("121435","DCM","Service Performer") = "Smith^RealPerson"
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/TEXT ("121435","DCM","Service Performer") = "Smith^RealPerson"
			assertTrue("Service Performer as PNAME and TEXT removed",CodedSequenceItem.getItemNumberContainingCodeSequence(list,TagFromName.ContentSequence,TagFromName.ConceptNameCodeSequence,new CodedSequenceItem("121435","DCM","Service Performer")) == -1);
			assertEquals("Service Performer as PNAME removed","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/pname[concept/@cv='121435']/value",srDocument));
			assertEquals("Service Performer as TEXT removed","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/text[concept/@cv='121435']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/PNAME ("7235223","99CRAZYMADEUP","Silly Name") = "Smith^RealPerson"
			assertEquals("Unrecognized PNAME removed","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/pname[concept/@cv='7235223']/value",srDocument));
			
			//CONTAINER CodedSequenceItem("111036","DCM","Mammography CAD Report")/TEXT ("121013","DCM","Device Observer Name") = "This is a potential identity leakage"
			assertEquals("Device Observer Name","",xpf.newXPath().evaluate("/DicomStructuredReport/DicomStructuredReportContent/container[concept/@cv='111036']/text[concept/@cv='121013']/value",srDocument));
			
		}
	}
}
