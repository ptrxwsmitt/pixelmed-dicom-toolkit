/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

import java.io.File;

public class TestAttributeListReadUnspecifiedShortAttribute extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestAttributeListReadUnspecifiedShortAttribute(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestAttributeListReadUnspecifiedShortAttribute.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestAttributeListReadUnspecifiedShortAttribute");
		
		// (000919)
		
		suite.addTest(new TestAttributeListReadUnspecifiedShortAttribute("TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR"));
		suite.addTest(new TestAttributeListReadUnspecifiedShortAttribute("TestAttributeListReadUnspecifiedShortAttribute_UnsignedPixelRepresentation_ImplicitVR"));
		suite.addTest(new TestAttributeListReadUnspecifiedShortAttribute("TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ExplicitVR"));
		suite.addTest(new TestAttributeListReadUnspecifiedShortAttribute("TestAttributeListReadUnspecifiedShortAttribute_UnsignedPixelRepresentation_ExplicitVR"));

		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private static final DicomDictionary dictionary = DicomDictionary.StandardDictionary;

	private String studyID = "612386812";
	private String seriesNumber = "12";
	private String instanceNumber = "38";
	
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

		}
		catch (DicomException e) {
		}
		return list;
	}
	
	public void TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR() throws Exception {
		File testFile = File.createTempFile("TestAttributeListReadUnspecifiedShortAttribute_SpecificAttribute",".dcm");
		testFile.deleteOnExit();
		{
			AttributeList list = makeAttributeList();

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
			
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ImplicitVRLittleEndian,"OURAETITLE");
			list.write(testFile,TransferSyntax.ImplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		{
			AttributeList list = new AttributeList();

			list.read(testFile);
			
			{
				Attribute a = list.get(TagFromName.PixelPaddingValue);
//System.err.println("TestAttributeListReadUnspecifiedShortAttribute.TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR(): read PixelPaddingValue class "+a.getClass());
				assertTrue("Checking PixelPaddingValue is SS",a instanceof SignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking PixelPaddingValue value is -1",-1,v[0]);
				assertTrue("Checking PixelPaddingValue value is not 0xffff",v[0] != 0xffff);
			}
			
			{
				Attribute a = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(list,TagFromName.RealWorldValueMappingSequence,TagFromName.RealWorldValueLastValueMapped);
//System.err.println("TestAttributeListReadUnspecifiedShortAttribute.TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR(): read RealWorldValueLastValueMapped class "+a.getClass());
				assertTrue("Checking RealWorldValueLastValueMapped is SS",a instanceof SignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking RealWorldValueLastValueMapped value is -1",-1,v[0]);
				assertTrue("Checking RealWorldValueLastValueMapped value is not 0xffff",v[0] != 0xffff);
			}
			
			{
				Attribute a = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(list,dictionary.getTagFromName("HistogramSequence"),dictionary.getTagFromName("HistogramLastBinValue"));
//System.err.println("TestAttributeListReadUnspecifiedShortAttribute.TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR(): read HistogramLastBinValue class "+a.getClass());
				assertTrue("Checking HistogramLastBinValue is SS",a instanceof SignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking HistogramLastBinValue value is -1",-1,v[0]);
				assertTrue("Checking HistogramLastBinValue value is not 0xffff",v[0] != 0xffff);
			}
		}
	}
	
	public void TestAttributeListReadUnspecifiedShortAttribute_UnsignedPixelRepresentation_ImplicitVR() throws Exception {
		File testFile = File.createTempFile("TestAttributeListReadUnspecifiedShortAttribute_SpecificAttribute",".dcm");
		testFile.deleteOnExit();
		{
			AttributeList list = makeAttributeList();

			{ Attribute a = new UnsignedShortAttribute(TagFromName.PixelRepresentation); a.addValue(0); list.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.PixelPaddingValue); a.addValue(0xffff); list.put(a); }
			
			{
				SequenceAttribute sa = new SequenceAttribute(TagFromName.RealWorldValueMappingSequence);
				list.put(sa);
				AttributeList itemlist = new AttributeList();
				sa.addItem(itemlist);
				
				{ Attribute a = new UnsignedShortAttribute(TagFromName.RealWorldValueLastValueMapped); a.addValue(0xffff); itemlist.put(a); }
			}
			
			{
				SequenceAttribute sa = new SequenceAttribute(dictionary.getTagFromName("HistogramSequence"));
				list.put(sa);
				AttributeList itemlist = new AttributeList();
				sa.addItem(itemlist);
				
				{ Attribute a = new UnsignedShortAttribute(dictionary.getTagFromName("HistogramLastBinValue")); a.addValue(0xffff); itemlist.put(a); }
			}

			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ImplicitVRLittleEndian,"OURAETITLE");
			list.write(testFile,TransferSyntax.ImplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		{
			AttributeList list = new AttributeList();

			list.read(testFile);
			
			{
				Attribute a = list.get(TagFromName.PixelPaddingValue);
//System.err.println("TestAttributeListReadUnspecifiedShortAttribute.TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR(): read PixelPaddingValue class "+a.getClass());
				assertTrue("Checking PixelPaddingValue is US",a instanceof UnsignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking PixelPaddingValue value is 0xffff",0xffff,v[0]);
				assertTrue("Checking PixelPaddingValue value is not -1",v[0] != -1);
			}
			
			{
				Attribute a = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(list,TagFromName.RealWorldValueMappingSequence,TagFromName.RealWorldValueLastValueMapped);
//System.err.println("TestAttributeListReadUnspecifiedShortAttribute.TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR(): read RealWorldValueLastValueMapped class "+a.getClass());
				assertTrue("Checking RealWorldValueLastValueMapped is US",a instanceof UnsignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking RealWorldValueLastValueMapped value is 0xffff",0xffff,v[0]);
				assertTrue("Checking RealWorldValueLastValueMapped value is not -1",v[0] != -1);
			}
			
			{
				Attribute a = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(list,dictionary.getTagFromName("HistogramSequence"),dictionary.getTagFromName("HistogramLastBinValue"));
//System.err.println("TestAttributeListReadUnspecifiedShortAttribute.TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR(): read HistogramLastBinValue class "+a.getClass());
				assertTrue("Checking HistogramLastBinValue is US",a instanceof UnsignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking HistogramLastBinValue value is 0xffff",0xffff,v[0]);
				assertTrue("Checking HistogramLastBinValue value is not -1",v[0] != -1);
			}
		}
	}

	
	public void TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ExplicitVR() throws Exception {
		File testFile = File.createTempFile("TestAttributeListReadUnspecifiedShortAttribute_SpecificAttribute",".dcm");
		testFile.deleteOnExit();
		{
			AttributeList list = makeAttributeList();

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

			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		{
			AttributeList list = new AttributeList();

			list.read(testFile);
			
			{
				Attribute a = list.get(TagFromName.PixelPaddingValue);
//System.err.println("TestAttributeListReadUnspecifiedShortAttribute.TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR(): read PixelPaddingValue class "+a.getClass());
				assertTrue("Checking PixelPaddingValue is SS",a instanceof SignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking PixelPaddingValue value is -1",-1,v[0]);
				assertTrue("Checking PixelPaddingValue value is not 0xffff",v[0] != 0xffff);
			}
			
			{
				Attribute a = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(list,TagFromName.RealWorldValueMappingSequence,TagFromName.RealWorldValueLastValueMapped);
//System.err.println("TestAttributeListReadUnspecifiedShortAttribute.TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR(): read RealWorldValueLastValueMapped class "+a.getClass());
				assertTrue("Checking RealWorldValueLastValueMapped is SS",a instanceof SignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking RealWorldValueLastValueMapped value is -1",-1,v[0]);
				assertTrue("Checking RealWorldValueLastValueMapped value is not 0xffff",v[0] != 0xffff);
			}
			
			{
				Attribute a = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(list,dictionary.getTagFromName("HistogramSequence"),dictionary.getTagFromName("HistogramLastBinValue"));
//System.err.println("TestAttributeListReadUnspecifiedShortAttribute.TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR(): read HistogramLastBinValue class "+a.getClass());
				assertTrue("Checking HistogramLastBinValue is SS",a instanceof SignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking HistogramLastBinValue value is -1",-1,v[0]);
				assertTrue("Checking HistogramLastBinValue value is not 0xffff",v[0] != 0xffff);
			}
		}
	}
	
	public void TestAttributeListReadUnspecifiedShortAttribute_UnsignedPixelRepresentation_ExplicitVR() throws Exception {
		File testFile = File.createTempFile("TestAttributeListReadUnspecifiedShortAttribute_SpecificAttribute",".dcm");
		testFile.deleteOnExit();
		{
			AttributeList list = makeAttributeList();

			{ Attribute a = new UnsignedShortAttribute(TagFromName.PixelRepresentation); a.addValue(0); list.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.PixelPaddingValue); a.addValue(0xffff); list.put(a); }
			
			{
				SequenceAttribute sa = new SequenceAttribute(TagFromName.RealWorldValueMappingSequence);
				list.put(sa);
				AttributeList itemlist = new AttributeList();
				sa.addItem(itemlist);
				
				{ Attribute a = new UnsignedShortAttribute(TagFromName.RealWorldValueLastValueMapped); a.addValue(0xffff); itemlist.put(a); }
			}
			
			{
				SequenceAttribute sa = new SequenceAttribute(dictionary.getTagFromName("HistogramSequence"));
				list.put(sa);
				AttributeList itemlist = new AttributeList();
				sa.addItem(itemlist);
				
				{ Attribute a = new UnsignedShortAttribute(dictionary.getTagFromName("HistogramLastBinValue")); a.addValue(0xffff); itemlist.put(a); }
			}

			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		{
			AttributeList list = new AttributeList();

			list.read(testFile);
			
			{
				Attribute a = list.get(TagFromName.PixelPaddingValue);
//System.err.println("TestAttributeListReadUnspecifiedShortAttribute.TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR(): read PixelPaddingValue class "+a.getClass());
				assertTrue("Checking PixelPaddingValue is US",a instanceof UnsignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking PixelPaddingValue value is 0xffff",0xffff,v[0]);
				assertTrue("Checking PixelPaddingValue value is not -1",v[0] != -1);
			}
			
			{
				Attribute a = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(list,TagFromName.RealWorldValueMappingSequence,TagFromName.RealWorldValueLastValueMapped);
//System.err.println("TestAttributeListReadUnspecifiedShortAttribute.TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR(): read RealWorldValueLastValueMapped class "+a.getClass());
				assertTrue("Checking RealWorldValueLastValueMapped is US",a instanceof UnsignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking RealWorldValueLastValueMapped value is 0xffff",0xffff,v[0]);
				assertTrue("Checking RealWorldValueLastValueMapped value is not -1",v[0] != -1);
			}
			
			{
				Attribute a = SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(list,dictionary.getTagFromName("HistogramSequence"),dictionary.getTagFromName("HistogramLastBinValue"));
//System.err.println("TestAttributeListReadUnspecifiedShortAttribute.TestAttributeListReadUnspecifiedShortAttribute_SignedPixelRepresentation_ImplicitVR(): read HistogramLastBinValue class "+a.getClass());
				assertTrue("Checking HistogramLastBinValue is US",a instanceof UnsignedShortAttribute);
				int[] v = a.getIntegerValues();
				assertEquals("Checking HistogramLastBinValue value is 0xffff",0xffff,v[0]);
				assertTrue("Checking HistogramLastBinValue value is not -1",v[0] != -1);
			}
		}
	}
	
}
