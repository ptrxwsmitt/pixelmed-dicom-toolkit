/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class TestDicomStreamCopier_ConvertTransferSyntaxes extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestDicomStreamCopier_ConvertTransferSyntaxes(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestDicomStreamCopier_ConvertTransferSyntaxes.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestDicomStreamCopier_ConvertTransferSyntaxes");
		
		suite.addTest(new TestDicomStreamCopier_ConvertTransferSyntaxes("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_Basic"));
		
		suite.addTest(new TestDicomStreamCopier_ConvertTransferSyntaxes("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_ValueTooLongForShortVR"));
		suite.addTest(new TestDicomStreamCopier_ConvertTransferSyntaxes("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitBig_ValueTooLongForShortVR"));
		suite.addTest(new TestDicomStreamCopier_ConvertTransferSyntaxes("TestDicomStreamCopier_ConvertTransferSyntaxes_RoundTripThroughExplicitBig_ValueTooLongForShortVR"));
		
		suite.addTest(new TestDicomStreamCopier_ConvertTransferSyntaxes("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_UnspecifiedShortAttribute_SignedPixelRepresentation"));

		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private static final DicomDictionary dictionary = DicomDictionary.StandardDictionary;

	private String patientName = "Smith^Mary";
	private String patientID = "3764913624";
	private String patientBirthDate = "19600101";
	private String patientAge = "041Y";
	private String patientWeight = "68";
	private String patientSize = "1.55";
	private String patientSex = "F";
	private String studyID = "612386812";
	private String seriesNumber = "12";
	private String instanceNumber = "38";
	private String referringPhysicianName = "Jones^Harriet";
	private String studyDate = "20010203";
	private String studyTime = "043000";

	private AttributeList makeAttributeList() {
		AttributeList list = new AttributeList();
		try {
			UIDGenerator u = new UIDGenerator("9999");
			String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
			String seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
			String studyInstanceUID = u.getNewStudyInstanceUID(studyID);

			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(SOPClass.CTImageStorage); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); list.put(a); }
			{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); a.addValue(patientBirthDate); list.put(a); }
			{ Attribute a = new AgeStringAttribute(TagFromName.PatientAge); a.addValue(patientAge); list.put(a); }
			{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue(patientSex); list.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PatientWeight); a.addValue(patientWeight); list.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PatientSize); a.addValue(patientSize); list.put(a); }
			{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
			{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue(referringPhysicianName); list.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); /*a.addValue(manufacturer);*/ list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }
		}
		catch (DicomException e) {
		}
		return list;
	}
		
	public void TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_Basic() throws Exception {
		File ivrleFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_Basic",".dcm");
		ivrleFile.deleteOnExit();
		AttributeList list = makeAttributeList();
		// do NOT add a metainformation header ...
		list.write(ivrleFile,TransferSyntax.ImplicitVRLittleEndian,false/*useMeta*/,true/*useBufferedStream*/);
		
		{
			// test is readable using same TransferSyntax without copying
			AttributeList readList = new AttributeList();
			readList.read(ivrleFile.getCanonicalPath(),TransferSyntax.ImplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists read OK",list.equals(readList));
		}

		{
			// test can be copied using DicomStreamCopier
			DicomInputStream dis = new DicomInputStream(new FileInputStream(ivrleFile),TransferSyntax.ImplicitVRLittleEndian,false/*tryMeta*/);
			File evrleFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_Basic",".dcm");
			evrleFile.deleteOnExit();
			DicomOutputStream dos = new DicomOutputStream(new FileOutputStream(evrleFile),null/*metaTransferSyntaxUID*/,TransferSyntax.ExplicitVRLittleEndian);
			dos.setWritingDataSet();
			new DicomStreamCopier(dis,dos);
			dos.close();
			dis.close();
			
			AttributeList copiedList = new AttributeList();
			copiedList.read(evrleFile.getCanonicalPath(),TransferSyntax.ExplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists copied using DicomStreamCopier OK",list.equals(copiedList));
		}
		
		{
			// test can be copied by reading and re-writing AttributeList with different TransferSyntax
			DicomInputStream dis = new DicomInputStream(new FileInputStream(ivrleFile),TransferSyntax.ImplicitVRLittleEndian,false/*tryMeta*/);
			File evrleFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_Basic",".dcm");
			evrleFile.deleteOnExit();
			{
				AttributeList readList = new AttributeList();
				readList.read(ivrleFile.getCanonicalPath(),TransferSyntax.ImplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
				readList.write(evrleFile,TransferSyntax.ExplicitVRLittleEndian,false/*useMeta*/,true/*useBufferedStream*/);
			}
			AttributeList copiedList = new AttributeList();
			copiedList.read(evrleFile.getCanonicalPath(),TransferSyntax.ExplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists copied by writing AttributeList OK",list.equals(copiedList));
		}
	}
		
	public void TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_ValueTooLongForShortVR() throws Exception {
		File ivrleFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_ValueTooLongForShortVR",".dcm");
		ivrleFile.deleteOnExit();
		AttributeList list = makeAttributeList();
		
		{
			Attribute a = new DecimalStringAttribute(TagFromName.DVHData);
			list.put(a);
			String dsv = "1.369547";		// 7 characters (will be 8 with backslash delimiter)
			for (int i=0; i<9000; ++i) {	// want to exceed limit of 65534 (largest even length 16 bit VL field) ... 65534/8 = 8191.75
				a.addValue(dsv);
			}
		}
		{
			Attribute a = new FloatDoubleAttribute(TagFromName.TableOfYBreakPoints);
			list.put(a);
			for (int i=0; i<8193; ++i) {
				a.addValue(i);
			}
		}
		
		// do NOT add a metainformation header ...
		list.write(ivrleFile,TransferSyntax.ImplicitVRLittleEndian,false/*useMeta*/,true/*useBufferedStream*/);
		
		{
			// test is readable using same TransferSyntax without copying
			AttributeList readList = new AttributeList();
			readList.read(ivrleFile.getCanonicalPath(),TransferSyntax.ImplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists read OK",list.equals(readList));
		}

		{
			// test can be copied using DicomStreamCopier
			DicomInputStream dis = new DicomInputStream(new FileInputStream(ivrleFile),TransferSyntax.ImplicitVRLittleEndian,false/*tryMeta*/);
			File evrleFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_ValueTooLongForShortVR",".dcm");
			evrleFile.deleteOnExit();
			DicomOutputStream dos = new DicomOutputStream(new FileOutputStream(evrleFile),null/*metaTransferSyntaxUID*/,TransferSyntax.ExplicitVRLittleEndian);
			dos.setWritingDataSet();
			new DicomStreamCopier(dis,dos);
			dos.close();
			dis.close();
			
			AttributeList copiedList = new AttributeList();
			copiedList.read(evrleFile.getCanonicalPath(),TransferSyntax.ExplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists copied using DicomStreamCopier OK",list.equals(copiedList));
		}
		
		{
			// test can be copied by reading and re-writing AttributeList with different TransferSyntax
			DicomInputStream dis = new DicomInputStream(new FileInputStream(ivrleFile),TransferSyntax.ImplicitVRLittleEndian,false/*tryMeta*/);
			File evrleFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_ValueTooLongForShortVR",".dcm");
			evrleFile.deleteOnExit();
			{
				AttributeList readList = new AttributeList();
				readList.read(ivrleFile.getCanonicalPath(),TransferSyntax.ImplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
				readList.write(evrleFile,TransferSyntax.ExplicitVRLittleEndian,false/*useMeta*/,true/*useBufferedStream*/);
			}
			AttributeList copiedList = new AttributeList();
			copiedList.read(evrleFile.getCanonicalPath(),TransferSyntax.ExplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists copied by writing AttributeList OK",list.equals(copiedList));
		}
	}
		
	public void TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitBig_ValueTooLongForShortVR() throws Exception {
		File ivrleFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitBig_ValueTooLongForShortVR",".dcm");
		ivrleFile.deleteOnExit();
		AttributeList list = makeAttributeList();
		
		{
			Attribute a = new DecimalStringAttribute(TagFromName.DVHData);
			list.put(a);
			String dsv = "1.369547";		// 7 characters (will be 8 with backslash delimiter)
			for (int i=0; i<9000; ++i) {	// want to exceed limit of 65534 (largest even length 16 bit VL field) ... 65534/8 = 8191.75
				a.addValue(dsv);
			}
		}
		//{	// cannot do this until we implement byte swapping in AttributeList.read() when converting UN to known VR :(
		//	Attribute a = new FloatDoubleAttribute(TagFromName.TableOfYBreakPoints);
		//	list.put(a);
		//	for (int i=0; i<8193; ++i) {
		//		a.addValue(i);
		//	}
		//}
		
		// do NOT add a metainformation header ...
		list.write(ivrleFile,TransferSyntax.ImplicitVRLittleEndian,false/*useMeta*/,true/*useBufferedStream*/);
		
		{
			// test is readable using same TransferSyntax without copying
			AttributeList readList = new AttributeList();
			readList.read(ivrleFile.getCanonicalPath(),TransferSyntax.ImplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists read OK",list.equals(readList));
		}

		{
			// test can be copied using DicomStreamCopier
			DicomInputStream dis = new DicomInputStream(new FileInputStream(ivrleFile),TransferSyntax.ImplicitVRLittleEndian,false/*tryMeta*/);
			File evrbeFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitBig_ValueTooLongForShortVR",".dcm");
			evrbeFile.deleteOnExit();
			DicomOutputStream dos = new DicomOutputStream(new FileOutputStream(evrbeFile),null/*metaTransferSyntaxUID*/,TransferSyntax.ExplicitVRBigEndian);
			dos.setWritingDataSet();
			new DicomStreamCopier(dis,dos);
			dos.close();
			dis.close();
			
			AttributeList copiedList = new AttributeList();
			copiedList.read(evrbeFile.getCanonicalPath(),TransferSyntax.ExplicitVRBigEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists copied using DicomStreamCopier OK",list.equals(copiedList));
		}
		
		{
			// test can be copied by reading and re-writing AttributeList with different TransferSyntax
			DicomInputStream dis = new DicomInputStream(new FileInputStream(ivrleFile),TransferSyntax.ImplicitVRLittleEndian,false/*tryMeta*/);
			File evrbeFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitBig_ValueTooLongForShortVR",".dcm");
			evrbeFile.deleteOnExit();
			{
				AttributeList readList = new AttributeList();
				readList.read(ivrleFile.getCanonicalPath(),TransferSyntax.ImplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
				readList.write(evrbeFile,TransferSyntax.ExplicitVRBigEndian,false/*useMeta*/,true/*useBufferedStream*/);
			}
			AttributeList copiedList = new AttributeList();
			copiedList.read(evrbeFile.getCanonicalPath(),TransferSyntax.ExplicitVRBigEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists copied by writing AttributeList OK",list.equals(copiedList));
		}
	}
		
	public void TestDicomStreamCopier_ConvertTransferSyntaxes_RoundTripThroughExplicitBig_ValueTooLongForShortVR() throws Exception {
		File ivrleFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_RoundTripThroughExplicitBig_ValueTooLongForShortVR",".dcm");
		ivrleFile.deleteOnExit();
		AttributeList list = makeAttributeList();
		
		{
			Attribute a = new DecimalStringAttribute(TagFromName.DVHData);
			list.put(a);
			String dsv = "1.369547";		// 7 characters (will be 8 with backslash delimiter)
			for (int i=0; i<9000; ++i) {	// want to exceed limit of 65534 (largest even length 16 bit VL field) ... 65534/8 = 8191.75
				a.addValue(dsv);
			}
		}
		{	// cannot do this until we implement byte swapping in AttributeList.read() when converting UN to known VR
			Attribute a = new FloatDoubleAttribute(TagFromName.TableOfYBreakPoints);
			list.put(a);
			for (int i=0; i<8193; ++i) {
				a.addValue(i);
			}
		}
		
		// do NOT add a metainformation header ...
		list.write(ivrleFile,TransferSyntax.ImplicitVRLittleEndian,false/*useMeta*/,true/*useBufferedStream*/);
		
		{
			// test is readable using same TransferSyntax without copying
			AttributeList readList = new AttributeList();
			readList.read(ivrleFile.getCanonicalPath(),TransferSyntax.ImplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists read OK",list.equals(readList));
		}

		{
			// test can be copied using DicomStreamCopier
			File evrbeFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_RoundTripThroughExplicitBig_ValueTooLongForShortVR",".dcm");
			evrbeFile.deleteOnExit();
			{
				DicomInputStream dis = new DicomInputStream(new FileInputStream(ivrleFile),TransferSyntax.ImplicitVRLittleEndian,false/*tryMeta*/);
				DicomOutputStream dos = new DicomOutputStream(new FileOutputStream(evrbeFile),null/*metaTransferSyntaxUID*/,TransferSyntax.ExplicitVRBigEndian);
				dos.setWritingDataSet();
				new DicomStreamCopier(dis,dos);
				dos.close();
				dis.close();
			}
			File evrleFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_RoundTripThroughExplicitBig_ValueTooLongForShortVR",".dcm");
			evrleFile.deleteOnExit();
			{
				DicomInputStream dis = new DicomInputStream(new FileInputStream(evrbeFile),TransferSyntax.ExplicitVRBigEndian,false/*tryMeta*/);
				DicomOutputStream dos = new DicomOutputStream(new FileOutputStream(evrleFile),null/*metaTransferSyntaxUID*/,TransferSyntax.ExplicitVRLittleEndian);
				dos.setWritingDataSet();
				new DicomStreamCopier(dis,dos);
				dos.close();
				dis.close();
			}
			
			AttributeList copiedList = new AttributeList();
			copiedList.read(evrleFile.getCanonicalPath(),TransferSyntax.ExplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists copied using DicomStreamCopier from IVRLE to EVRBE to EVRLE OK",list.equals(copiedList));
		}

		
		//{	// cannot do this until we implement byte swapping in Attribute.write() when writing too big value for known VR to UN in big endian Transfer Syntax :(
		//	// test can be copied by reading and re-writing AttributeList with different TransferSyntax
		//	DicomInputStream dis = new DicomInputStream(new FileInputStream(ivrleFile),TransferSyntax.ImplicitVRLittleEndian,false/*tryMeta*/);
		//	File evrbeFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicit",".dcm");
		//	{
		//		AttributeList readList = new AttributeList();
		//		readList.read(ivrleFile.getCanonicalPath(),TransferSyntax.ImplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
		//		readList.write(evrbeFile,TransferSyntax.ExplicitVRBigEndian,false/*useMeta*/,true/*useBufferedStream*/);						// <--- will throw Exception here
		//	}
		//	File evrleFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicit",".dcm");
		//	evrleFile.deleteOnExit();
		//	{
		//		AttributeList readList = new AttributeList();
		//		readList.read(evrbeFile.getCanonicalPath(),TransferSyntax.ExplicitVRBigEndian,false/*hasMeta*/,true/*useBufferedStream*/);
		//		readList.write(evrleFile,TransferSyntax.ExplicitVRLittleEndian,false/*useMeta*/,true/*useBufferedStream*/);
		//	}
		//
		//	AttributeList copiedList = new AttributeList();
		//	copiedList.read(evrleFile.getCanonicalPath(),TransferSyntax.ExplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
		//	assertTrue("AttributeLists copied by writing AttributeList from IVRLE to EVRBE to EVRLE OK",list.equals(copiedList));
		//}
	}
	
	public void TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_UnspecifiedShortAttribute_SignedPixelRepresentation() throws Exception {
		File ivrleFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_UnspecifiedShortAttribute_SignedPixelRepresentation",".dcm");
		ivrleFile.deleteOnExit();
		AttributeList list = makeAttributeList();
		
		// copied from TestAttributeListReadUnspecifiedShortAttribute
		{
			{ Attribute a = new UnsignedShortAttribute(TagFromName.PixelRepresentation); a.addValue(1); list.put(a); }
			{ Attribute a = new SignedShortAttribute(TagFromName.PixelPaddingValue); a.addValue(0xffff); list.put(a); }
			
			{
				SequenceAttribute sa = new SequenceAttribute(TagFromName.RealWorldValueMappingSequence);
				list.put(sa);
				AttributeList itemlist = new AttributeList();
				sa.addItem(itemlist);
				
				{ Attribute a = new SignedShortAttribute(TagFromName.RealWorldValueLastValueMapped); a.addValue(0xffff); itemlist.put(a); }
			}
			
			{
				SequenceAttribute sa = new SequenceAttribute(dictionary.getTagFromName("HistogramSequence"));
				list.put(sa);
				AttributeList itemlist = new AttributeList();
				sa.addItem(itemlist);
				
				{ Attribute a = new SignedShortAttribute(dictionary.getTagFromName("HistogramLastBinValue")); a.addValue(0xffff); itemlist.put(a); }
			}
		}
		
		// do NOT add a metainformation header ...
		list.write(ivrleFile,TransferSyntax.ImplicitVRLittleEndian,false/*useMeta*/,true/*useBufferedStream*/);
		
		{
			// test is readable using same TransferSyntax without copying
			AttributeList readList = new AttributeList();
			readList.read(ivrleFile.getCanonicalPath(),TransferSyntax.ImplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists read OK",list.equals(readList));
		}

		{
			// test can be copied using DicomStreamCopier
			DicomInputStream dis = new DicomInputStream(new FileInputStream(ivrleFile),TransferSyntax.ImplicitVRLittleEndian,false/*tryMeta*/);
			File evrleFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_UnspecifiedShortAttribute_SignedPixelRepresentation",".dcm");
			evrleFile.deleteOnExit();
			DicomOutputStream dos = new DicomOutputStream(new FileOutputStream(evrleFile),null/*metaTransferSyntaxUID*/,TransferSyntax.ExplicitVRLittleEndian);
			dos.setWritingDataSet();
			new DicomStreamCopier(dis,dos);
			dos.close();
			dis.close();
			
			AttributeList copiedList = new AttributeList();
			copiedList.read(evrleFile.getCanonicalPath(),TransferSyntax.ExplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists copied using DicomStreamCopier OK",list.equals(copiedList));
			
			// check that lists are equal for the right reasons ...
			
			{
				Attribute a = copiedList.get(TagFromName.PixelPaddingValue);
//System.err.println("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_UnspecifiedShortAttribute_SignedPixelRepresentation(): read PixelPaddingValue class "+a.getClass());
				assertTrue("Checking PixelPaddingValue is SS",a instanceof SignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking PixelPaddingValue value is -1",-1,v[0]);
				assertTrue("Checking PixelPaddingValue value is not 0xffff",v[0] != 0xffff);
			}
			
			{
				Attribute a = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(copiedList,TagFromName.RealWorldValueMappingSequence,TagFromName.RealWorldValueLastValueMapped);
//System.err.println("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_UnspecifiedShortAttribute_SignedPixelRepresentation(): read RealWorldValueLastValueMapped class "+a.getClass());
				assertTrue("Checking RealWorldValueLastValueMapped is SS",a instanceof SignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking RealWorldValueLastValueMapped value is -1",-1,v[0]);
				assertTrue("Checking RealWorldValueLastValueMapped value is not 0xffff",v[0] != 0xffff);
			}
			
			{
				Attribute a = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(copiedList,dictionary.getTagFromName("HistogramSequence"),dictionary.getTagFromName("HistogramLastBinValue"));
//System.err.println("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_UnspecifiedShortAttribute_SignedPixelRepresentation(): read HistogramLastBinValue class "+a.getClass());
				assertTrue("Checking HistogramLastBinValue is SS",a instanceof SignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking HistogramLastBinValue value is -1",-1,v[0]);
				assertTrue("Checking HistogramLastBinValue value is not 0xffff",v[0] != 0xffff);
			}
		}
		
		{
			// test can be copied by reading and re-writing AttributeList with different TransferSyntax
			DicomInputStream dis = new DicomInputStream(new FileInputStream(ivrleFile),TransferSyntax.ImplicitVRLittleEndian,false/*tryMeta*/);
			File evrleFile = File.createTempFile("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_UnspecifiedShortAttribute_SignedPixelRepresentation",".dcm");
			evrleFile.deleteOnExit();
			{
				AttributeList readList = new AttributeList();
				readList.read(ivrleFile.getCanonicalPath(),TransferSyntax.ImplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
				readList.write(evrleFile,TransferSyntax.ExplicitVRLittleEndian,false/*useMeta*/,true/*useBufferedStream*/);
			}
			AttributeList copiedList = new AttributeList();
			copiedList.read(evrleFile.getCanonicalPath(),TransferSyntax.ExplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			assertTrue("AttributeLists copied by writing AttributeList OK",list.equals(copiedList));
			
			// check that lists are equal for the right reasons ...
			
			{
				Attribute a = copiedList.get(TagFromName.PixelPaddingValue);
//System.err.println("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_UnspecifiedShortAttribute_SignedPixelRepresentation(): read PixelPaddingValue class "+a.getClass());
				assertTrue("Checking PixelPaddingValue is SS",a instanceof SignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking PixelPaddingValue value is -1",-1,v[0]);
				assertTrue("Checking PixelPaddingValue value is not 0xffff",v[0] != 0xffff);
			}
			
			{
				Attribute a = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(copiedList,TagFromName.RealWorldValueMappingSequence,TagFromName.RealWorldValueLastValueMapped);
//System.err.println("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_UnspecifiedShortAttribute_SignedPixelRepresentation(): read RealWorldValueLastValueMapped class "+a.getClass());
				assertTrue("Checking RealWorldValueLastValueMapped is SS",a instanceof SignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking RealWorldValueLastValueMapped value is -1",-1,v[0]);
				assertTrue("Checking RealWorldValueLastValueMapped value is not 0xffff",v[0] != 0xffff);
			}
			
			{
				Attribute a = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(copiedList,dictionary.getTagFromName("HistogramSequence"),dictionary.getTagFromName("HistogramLastBinValue"));
//System.err.println("TestDicomStreamCopier_ConvertTransferSyntaxes_ImplicitToExplicitLittle_UnspecifiedShortAttribute_SignedPixelRepresentation(): read HistogramLastBinValue class "+a.getClass());
				assertTrue("Checking HistogramLastBinValue is SS",a instanceof SignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking HistogramLastBinValue value is -1",-1,v[0]);
				assertTrue("Checking HistogramLastBinValue value is not 0xffff",v[0] != 0xffff);
			}
		}
	}

}
