/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import junit.framework.*;

import com.pixelmed.dicom.*;

import java.io.File;

import javax.json.Json;
import javax.json.JsonArray;

import java.util.Locale;

public class TestJSONRepresentationOfStructuredReport extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestJSONRepresentationOfStructuredReport(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestJSONRepresentationOfStructuredReport.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestJSONRepresentationOfStructuredReport");
		
		suite.addTest(new TestJSONRepresentationOfStructuredReport("testStructuredReport_JSONRepresentationOfStructuredReport_DicomFile"));

		return suite;
	}
	
	private static String deviceUID = "1.2.3.4";
	private static String deviceName = "station1";
	private static String manufacturer = "Acme";
	private static String modelName = "Scanner";
	private static String serialNumber = "72349236741";
	private static String location = "Suite1";
	
	private static String operatorName = "Smith^John";
	private static String operatorID = "26354781234";
	private static String physicianName = "Jones^Mary";
	private static String physicianID = "23491234234";
	private static String idIssuer = "99BLA";
	private static String organization = "St. Elsewhere's";
	private static String patientName = "Yamada^Tarou=山田^太郎=やまだ^たろう";
	private static String patientID = "3764913624";
	private static String patientBirthDate = "19600101";
	private static String patientAge = "041Y";
	private static String patientWeight = "68";
	private static String patientSize = "1.55";
	private static String patientSex = "F";
	private static String studyID = "612386812";
	private static String seriesNumber = "12";
	private static String instanceNumber = "38";
	private static String referringPhysicianName = "Jones^Harriet";
	
	private static String consultingPhysicianName1 = "Jones^Harriet";			// to test empty values in PN
	private static String consultingPhysicianName2 = "Yamada^Tarou=山田^太郎=やまだ^たろう";
	private static String consultingPhysicianName3 = "";
	private static String consultingPhysicianName4 = "Wang^XiaoDong=王^小東";		// note the absence of trailing delimiter ... if present, round trip will fail
	private static String consultingPhysicianName5 = "";

	private static String studyDate = "20010203";
	private static String studyTime = "043000";
	
	private static String sopInstanceUID;
	private static String seriesInstanceUID;
	private static String studyInstanceUID;
	
	private static String sopClassUID = SOPClass.EnhancedSRStorage;
	
	private static String modalitiesInStudy1 = "CT";		// to test empty values in CS
	private static String modalitiesInStudy2 = "";
	private static String modalitiesInStudy3 = "MR";
	private static String modalitiesInStudy4 = "";
	
	private static String referencedFrameNumber1 = "1";		// to test 1-n values in IS - NB. Cannot yet handle empty values due to numeric array conversion :(
	private static String referencedFrameNumber2 = "2";
	private static String referencedFrameNumber3 = "3";
	private static String referencedFrameNumber4 = "4";
	
	private static long simpleFrameList1 = 5;
	private static long simpleFrameList2 = 6;
	
	private static double timeRange1 = 36.4;
	private static double timeRange2 = 59.0;

	private static byte[] makerNote = { 0x01b, 0x02b, 0x03b, 0x04b };
	private static short[] recommendedDisplayCIELabValueList = { (short)0xff01, (short)0x7090, (short)0x60a0 };
	private static float[] floatingPointValues = { 36.27f, 6.3e-7f };
	private static double[] volumetricCurvePoints = { 36.27, 6.3e-7 };
	private static int[] longPrimitivePointIndexList = { 0x00112233, 0x33221100 };
	private static long[] extendedOffsetTable = { 0x0011223344556677l, 0x7766554433221100l };
	private static byte[] unknownPrivateValues = { (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34 };
	
	private static String observationUID = "2.25.121653014693151198548584403358069116971";
	private static String observationDateTime = "20191230163732";

	private static StructuredReport createSRContent() throws DicomException {
		ContentItemFactory cif = new ContentItemFactory();
		ContentItem root = cif.new ContainerContentItem(null/*no parent since root*/,null/*no relationshipType since root*/,new CodedSequenceItem("111036","DCM","Mammography CAD Report"),true/*continuityOfContentIsSeparate*/,"DCMR","5000");

		ContentItem imageLibrary = cif.new ContainerContentItem(root,"CONTAINS",new CodedSequenceItem("111028","DCM","Image Library"),true/*continuityOfContentIsSeparate*/);
		ContentItem image_1_1_1 = cif.new ImageContentItem(imageLibrary,"CONTAINS",null/*conceptName*/,
										SOPClass.DigitalMammographyXRayImageStorageForPresentation,"1.3.6.1.4.1.5962.99.1.993064428.2122236180.1358202762732.2.0",
										0/*referencedFrameNumber*/,0/*referencedSegmentNumber*/,
										null/*presentationStateSOPClassUID*/,null/*presentationStateSOPInstanceUID*/,
										null/*realWorldValueMappingSOPClassUID*/,null/*realWorldValueMappingSOPInstanceUID*/);

		// AFTER imageLibrary so we don't screw up references
		{
			cif.new PersonNameContentItem(root,"HAS OBS CONTEXT",new CodedSequenceItem("121008","DCM","Person Observer Name"),consultingPhysicianName2);
			ContentItem forPNChildren = cif.new PersonNameContentItem(root,"HAS OBS CONTEXT",new CodedSequenceItem("121008","DCM","Person Observer Name"),consultingPhysicianName4,observationDateTime,observationUID);
			cif.new TextContentItem(forPNChildren,"HAS CONCEPT MOD",new CodedSequenceItem("128775","DCM","Identifier within Person Observer's Role"),"not realistic");
		}

		ContentItem findingsSummary = cif.new CodeContentItem(root,"CONTAINS",new CodedSequenceItem("111017","DCM","CAD Processing and Findings Summary"),new CodedSequenceItem("111242","DCM","All algorithms succeeded; with findings"));
		{
			ContentItem individual = cif.new ContainerContentItem(findingsSummary,"CONTAINS",new CodedSequenceItem("111034","DCM","Individual Impression/Recommendation"),true/*continuityOfContentIsSeparate*/);
			cif.new CodeContentItem(individual,"HAS CONCEPT MOD",new CodedSequenceItem("111056","DCM","Rendering Intent"),new CodedSequenceItem("111150","DCM","Presentation Required: Rendering device is expected to present"));

			{
				CodedSequenceItem value = new CodedSequenceItem("12345","99PMPTESTJSON","20191230","Value for extended attributes");	// version BEFORE meaning
				
				{ Attribute a = AttributeFactory.newAttribute(DicomDictionary.StandardDictionary.getTagFromName("LongCodeValue")); a.addValue("LONGCODE"); value.getAttributeList().put(a); }
				{ Attribute a = AttributeFactory.newAttribute(DicomDictionary.StandardDictionary.getTagFromName("URNCodeValue")); a.addValue("URNCODE"); value.getAttributeList().put(a); }
				{ Attribute a = AttributeFactory.newAttribute(DicomDictionary.StandardDictionary.getTagFromName("ContextIdentifier")); a.addValue("CIDVALUE"); value.getAttributeList().put(a); }
				{ Attribute a = AttributeFactory.newAttribute(DicomDictionary.StandardDictionary.getTagFromName("ContextUID")); a.addValue("CUIDVALUE"); value.getAttributeList().put(a); }
				{ Attribute a = AttributeFactory.newAttribute(DicomDictionary.StandardDictionary.getTagFromName("MappingResource")); a.addValue("MRVALUE"); value.getAttributeList().put(a); }
				{ Attribute a = AttributeFactory.newAttribute(DicomDictionary.StandardDictionary.getTagFromName("MappingResourceUID")); a.addValue("MRUIDALUE"); value.getAttributeList().put(a); }
				{ Attribute a = AttributeFactory.newAttribute(DicomDictionary.StandardDictionary.getTagFromName("MappingResourceName")); a.addValue("MRNVALUE"); value.getAttributeList().put(a); }
				{ Attribute a = AttributeFactory.newAttribute(DicomDictionary.StandardDictionary.getTagFromName("ContextGroupVersion")); a.addValue("CGVERSVALUE"); value.getAttributeList().put(a); }
				{ Attribute a = AttributeFactory.newAttribute(DicomDictionary.StandardDictionary.getTagFromName("ContextGroupExtensionFlag")); a.addValue("CGEXTFLAGVALUE"); value.getAttributeList().put(a); }
				{ Attribute a = AttributeFactory.newAttribute(DicomDictionary.StandardDictionary.getTagFromName("ContextGroupLocalVersion")); a.addValue("CGLVERSVALUE"); value.getAttributeList().put(a); }
				{ Attribute a = AttributeFactory.newAttribute(DicomDictionary.StandardDictionary.getTagFromName("ContextGroupExtensionCreatorUID")); a.addValue("CGEXTCUIDVALUE"); value.getAttributeList().put(a); }

				cif.new CodeContentItem(individual,"HAS CONCEPT MOD",new CodedSequenceItem("12346","99PMPTESTJSON","Concept name for extended attributes"),value);
			}
			
			ContentItem cluster = cif.new CodeContentItem(individual,"CONTAINS",new CodedSequenceItem("111059","DCM","Single Image Finding"),new CodedSequenceItem("F-01775","SRT","Calcification Cluster"));
			cif.new CodeContentItem(cluster,"HAS CONCEPT MOD",new CodedSequenceItem("111056","DCM","Rendering Intent"),new CodedSequenceItem("111150","DCM","Presentation Required: Rendering device is expected to present"));
			ContentItem clusterCoord = cif.new SpatialCoordinatesContentItem(individual,"CONTAINS",new CodedSequenceItem("111010","DCM","Center"),"POINT",new float[] { 165,2433 });
			new ContentItemWithReference(clusterCoord,"SELECTED FROM","1.1.1");
			
			//cif.new NumericContentItem(cluster,"HAS PROPERTIES",new CodedSequenceItem("111038","DCM","Number of calcifications"),10,new CodedSequenceItem("111150","1","no units"));
			cif.new NumericContentItem(cluster,"HAS PROPERTIES",new CodedSequenceItem("111038","DCM","Number of calcifications"),"10",10d,new Integer(20),new Long(2),new CodedSequenceItem("1","UCUM","no units"),new CodedSequenceItem("114006","DCM","Measurement failure"),observationDateTime,observationUID) ;
			
			ContentItem single = cif.new CodeContentItem(individual,"CONTAINS",new CodedSequenceItem("111059","DCM","Single Image Finding"),new CodedSequenceItem("F-01776","SRT","Individual Calcification"));
			cif.new CodeContentItem(single,"HAS CONCEPT MOD",new CodedSequenceItem("111056","DCM","Rendering Intent"),new CodedSequenceItem("111150","DCM","Presentation Required: Rendering device is expected to present"));
			ContentItem singleCenterCoord = cif.new SpatialCoordinatesContentItem(individual,"CONTAINS",new CodedSequenceItem("111010","DCM","Center"),"POINT",new float[] { 198,2389 });
			new ContentItemWithReference(singleCenterCoord,"SELECTED FROM","1.1.1");
			ContentItem singleOutlineCoord = cif.new SpatialCoordinatesContentItem(individual,"CONTAINS",new CodedSequenceItem("111041","DCM","Outline"),"POLYLINE",new float[] { 199,2388,198,2388,197,2388,197,2389,197,2390,198,2390,199,2390,200,2390,200,2389 });
			new ContentItemWithReference(singleOutlineCoord,"SELECTED FROM","1.1.1");
		}
		
		StructuredReport sr = new StructuredReport(root);
System.err.println(sr);
		return sr;
	}

	private static AttributeList createSRContentAttributeList() throws DicomException {
		StructuredReport sr = createSRContent();
		return sr.getAttributeList();
	}
	
	private static AttributeList createHeaderAttributeList() throws DicomException {
		Locale.setDefault(Locale.FRENCH);	// forces check that "," is not being used as decimal point in any double to string conversions
		AttributeList list = new AttributeList();
		{
			UIDGenerator u = new UIDGenerator("9999");
			sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
			seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
			studyInstanceUID = u.getNewStudyInstanceUID(studyID);
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(sopClassUID); list.put(a); }
			{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); a.addValue(patientBirthDate); list.put(a); }
			{ Attribute a = new AgeStringAttribute(TagFromName.PatientAge); a.addValue(patientAge); list.put(a); }
			{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue(patientSex); list.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PatientWeight); a.addValue(patientWeight); list.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PatientSize); a.addValue(patientSize); list.put(a); }
			{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
			{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue(referringPhysicianName); list.put(a); }
			{ Attribute a = new PersonNameAttribute(TagFromName.ConsultingPhysicianName);
				a.addValue(consultingPhysicianName1);
				a.addValue(consultingPhysicianName2);
				a.addValue(consultingPhysicianName3);
				a.addValue(consultingPhysicianName4);
				a.addValue(consultingPhysicianName5);
				list.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); /*a.addValue(manufacturer);*/ list.put(a); }
			//{ Attribute a = new LongStringAttribute(TagFromName.ManufacturerModelName); a.addValue(modelName); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }

			{ Attribute a = new CodeStringAttribute(TagFromName.ModalitiesInStudy);
				a.addValue(modalitiesInStudy1);
				a.addValue(modalitiesInStudy2);
				a.addValue(modalitiesInStudy3);
				a.addValue(modalitiesInStudy4);
				list.put(a); }

			{ Attribute a = new IntegerStringAttribute(TagFromName.ReferencedFrameNumber);
				a.addValue(referencedFrameNumber1);
				a.addValue(referencedFrameNumber2);
				a.addValue(referencedFrameNumber3);
				a.addValue(referencedFrameNumber4);
				list.put(a); }

			{ Attribute a = new UnsignedLongAttribute(DicomDictionary.StandardDictionary.getTagFromName("SimpleFrameList"));
				a.addValue(simpleFrameList1);
				a.addValue(simpleFrameList2);
				list.put(a); }

			{ Attribute a = new FloatDoubleAttribute(DicomDictionary.StandardDictionary.getTagFromName("TimeRange"));
				a.addValue(timeRange1);
				a.addValue(timeRange2);
				list.put(a); }

//System.err.println("TestJSONRepresentationOfStructuredReport.setUp(): compositeInstanceContext.getAttributeList() =\n"+list);

			{ Attribute a = new OtherByteAttribute(DicomDictionary.StandardDictionary.getTagFromName("MakerNote")); a.setValues(makerNote); list.put(a); }
			{ Attribute a = new OtherWordAttribute(DicomDictionary.StandardDictionary.getTagFromName("RecommendedDisplayCIELabValueList")); a.setValues(recommendedDisplayCIELabValueList); list.put(a); }
			{ Attribute a = new OtherFloatAttribute(DicomDictionary.StandardDictionary.getTagFromName("FloatingPointValues")); a.setValues(floatingPointValues); list.put(a); }
			{ Attribute a = new OtherDoubleAttribute(DicomDictionary.StandardDictionary.getTagFromName("VolumetricCurvePoints")); a.setValues(volumetricCurvePoints); list.put(a); }
			{ Attribute a = new OtherLongAttribute(DicomDictionary.StandardDictionary.getTagFromName("LongPrimitivePointIndexList")); a.setValues(longPrimitivePointIndexList); list.put(a); }
			{ Attribute a = new OtherVeryLongAttribute(DicomDictionary.StandardDictionary.getTagFromName("ExtendedOffsetTable")); a.setValues(extendedOffsetTable); list.put(a); }
			{ Attribute a = new UnknownAttribute(new AttributeTag(0x0029,0x1001)); a.setValues(unknownPrivateValues); list.put(a); }

		}
		return list;
	}

	private static void checkEquivalenceOfAttributeLists(AttributeList list,AttributeList roundTripList) throws DicomException {

		//assertEquals("Checking round trip AttributeList",list,roundTripList);
		
		// check Attributes that do not have getString() values so are not checked by AttributeList.equals() :(
		{
			Attribute a = list.get(DicomDictionary.StandardDictionary.getTagFromName("MakerNote"));
			Attribute rta = roundTripList.get(DicomDictionary.StandardDictionary.getTagFromName("MakerNote"));
			byte[] b = a.getByteValues();
			byte[] rtb = rta.getByteValues();
			assertTrue("Checking round trip OtherByteAttribute",java.util.Arrays.equals(b,rtb));
		}
		{
			Attribute a = list.get(DicomDictionary.StandardDictionary.getTagFromName("RecommendedDisplayCIELabValueList"));
			Attribute rta = roundTripList.get(DicomDictionary.StandardDictionary.getTagFromName("RecommendedDisplayCIELabValueList"));
			short[] b = a.getShortValues();
			short[] rtb = rta.getShortValues();
			assertTrue("Checking round trip OtherWordAttribute",java.util.Arrays.equals(b,rtb));
		}
		{
			Attribute a = list.get(DicomDictionary.StandardDictionary.getTagFromName("FloatingPointValues"));
			Attribute rta = roundTripList.get(DicomDictionary.StandardDictionary.getTagFromName("FloatingPointValues"));
			float[] b = a.getFloatValues();
			float[] rtb = rta.getFloatValues();
			assertTrue("Checking round trip OtherFloatAttribute",java.util.Arrays.equals(b,rtb));
		}
		{
			Attribute a = list.get(DicomDictionary.StandardDictionary.getTagFromName("VolumetricCurvePoints"));
			Attribute rta = roundTripList.get(DicomDictionary.StandardDictionary.getTagFromName("VolumetricCurvePoints"));
			double[] b = a.getDoubleValues();
			double[] rtb = rta.getDoubleValues();
			assertTrue("Checking round trip OtherDoubleAttribute",java.util.Arrays.equals(b,rtb));
		}
		{
			Attribute a = list.get(DicomDictionary.StandardDictionary.getTagFromName("LongPrimitivePointIndexList"));
			Attribute rta = roundTripList.get(DicomDictionary.StandardDictionary.getTagFromName("LongPrimitivePointIndexList"));
			int[] b = a.getIntegerValues();
			int[] rtb = rta.getIntegerValues();
			assertTrue("Checking round trip OtherLongAttribute",java.util.Arrays.equals(b,rtb));
		}
		{
			Attribute a = list.get(DicomDictionary.StandardDictionary.getTagFromName("ExtendedOffsetTable"));
			Attribute rta = roundTripList.get(DicomDictionary.StandardDictionary.getTagFromName("ExtendedOffsetTable"));
			long[] b = a.getLongValues();
			long[] rtb = rta.getLongValues();
			assertTrue("Checking round trip OtherVeryLongAttribute",java.util.Arrays.equals(b,rtb));
		}
		{
			AttributeTag tag = new AttributeTag(0x0029,0x1001);
			Attribute a = list.get(tag);
			Attribute rta = roundTripList.get(tag);
			byte[] b = a.getByteValues(false/*big*/);
			byte[] rtb = rta.getByteValues(false/*big*/);
			assertTrue("Checking round trip UnknownAttribute",java.util.Arrays.equals(b,rtb));
		}
	}

	private static void checkEquivalenceOfStructuredReports(StructuredReport sr,StructuredReport roundTripSR) throws DicomException {
		assertEquals("Checking round trip StructuredReport",sr.toString(),roundTripSR.toString());		// blech :( NB. StructuredReport subclasses TreeModel so be wary of messing with real equals()
	}
	
	protected void tearDown() {
	}
	
	public void testStructuredReport_JSONRepresentationOfStructuredReport_DicomFile() throws Exception {
		File dicomFile = File.createTempFile("testStructuredReport_JSONRepresentationOfStructuredReport_DicomFile",".dcm");
		dicomFile.deleteOnExit();
		File jsonFile = File.createTempFile("testStructuredReport_JSONRepresentationOfStructuredReport_JsonFile",".json");
		jsonFile.deleteOnExit();
		File businessNamesFile = File.createTempFile("testStructuredReport_JSONRepresentationOfStructuredReport_BusinessNamesFile",".json");
		businessNamesFile.deleteOnExit();

		StructuredReport sr = createSRContent();
		AttributeList list = sr.getAttributeList();
		list.putAll(createHeaderAttributeList());
//System.err.println("list after createHeaderAttributeList\n"+list);

		list.insertSuitableSpecificCharacterSetForAllStringValues();
		FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
		list.write(dicomFile,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		
		{
			JSONRepresentationOfStructuredReportObjectFactory j = new JSONRepresentationOfStructuredReportObjectFactory();
			JsonArray jsonDocument = j.getDocument(dicomFile);
System.err.println(jsonDocument+"\n\n\n");
			JSONRepresentationOfStructuredReportObjectFactory.write(jsonFile,jsonDocument);
			JsonArray businessNamesDocument = j.getBusinessNamesDocument();
//System.err.println(businessNamesDocument+"\n\n\n");
			JSONRepresentationOfStructuredReportObjectFactory.write(businessNamesFile,businessNamesDocument);
		}
		
		{
			JSONRepresentationOfStructuredReportObjectFactory j = new JSONRepresentationOfStructuredReportObjectFactory();
			j.loadBusinessNamesDocument(businessNamesFile);
			AttributeList roundTripList = j.getAttributeList(jsonFile);
//System.err.println("Round roundTripList\n"+roundTripList);

			//list.removeMetaInformationHeaderAttributes();
			//roundTripList.removeMetaInformationHeaderAttributes();
			
			//AttributeList.clearByteOffset(list);
			//AttributeList.clearByteOffset(roundTripList);
			
//System.err.println("list after clearByteOffset\n"+list);
//System.err.println("roundTripList after clearByteOffset\n"+roundTripList);

			checkEquivalenceOfAttributeLists(list,roundTripList);	// includes check that private attribute in header survives

			StructuredReport roundTripSR = new StructuredReport(roundTripList);
//System.err.println("Round trip\n"+roundTripSR);
			checkEquivalenceOfStructuredReports(sr,roundTripSR);
			
			// should check values of extended attributes of ("12346","99PMPTESTJSON","Concept name for extended attributes")
		}
	}
	
}
