/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import junit.framework.*;

import java.io.File;

public class TestAttributeListWriteAndReadMetaInformation extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestAttributeListWriteAndReadMetaInformation(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestAttributeListWriteAndReadMetaInformation.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestAttributeListWriteAndReadMetaInformation");
		
		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_IVRLE_Dataset"));
		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_EVRLE_Dataset"));

		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_IVRLE_Dataset"));
		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_EVRLE_Dataset"));

		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteAndReadMetaInformation_EVRLE_Meta_EVRLE_Dataset_CommandGroup"));
		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteAndReadMetaInformation_NoMeta_EVRLE_Dataset_CommandGroup"));		// (001133)

		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteAndReadMetaInformation_WithTransferSyntaxUID"));
		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteAndReadMetaInformation_AbsentTransferSyntaxUID"));
		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteAndReadMetaInformation_EmptyTransferSyntaxUID"));

		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteMetaInformation_RefuseAbsentTransferSyntaxUID"));	// (001137)
		suite.addTest(new TestAttributeListWriteAndReadMetaInformation("TestAttributeListWriteMetaInformation_RefuseEmptyTransferSyntaxUID"));	// (001137)

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
	
	private void addCommandGroup(AttributeList list) {
		try {
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.AffectedSOPClassUID); a.addValue(SOPClass.CTImageStorage); list.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.CommandField); a.addValue(1); list.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.MessageID); a.addValue(1234); list.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.Priority); a.addValue(1); list.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.CommandDataSetType); a.addValue(0); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.AffectedSOPInstanceUID); a.addValue(Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SOPInstanceUID)); list.put(a); }
		}
		catch (DicomException e) {
		}
	}

	public void TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_IVRLE_Dataset() throws Exception {
//System.err.println("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_IVRLE_Dataset():");
		
		int wroteValue = 0x7010;
		AttributeTag useTag = dictionary.getTagFromName("ReferencedFrameNumbers");
		
		File testFile = File.createTempFile("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_IVRLE_Dataset",".dcm");
		testFile.deleteOnExit();
		{
			AttributeList list = makeAttributeList();
			
			{ Attribute a = new UnsignedShortAttribute(useTag); a.addValue(wroteValue); list.put(a); }	// something byte order and explicit/implicit VR form dependent
			
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ImplicitVRLittleEndian,"OURAETITLE");
			list.write(testFile,TransferSyntax.ImplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		{
			AttributeList list = new AttributeList();
			
			list.read(testFile);

			int readValue = Attribute.getSingleIntegerValueOrDefault(list,useTag,0);
			
			assertEquals("Checking value read equals wrote",wroteValue,readValue);
		}
	}
	
	public void TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_EVRLE_Dataset() throws Exception {
//System.err.println("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_EVRLE_Dataset():");
		
		int wroteValue = 0x7010;
		AttributeTag useTag = dictionary.getTagFromName("ReferencedFrameNumbers");
		
		File testFile = File.createTempFile("TestAttributeListWriteAndReadMetaInformation_Valid_EVRLE_Meta_EVRLE_Dataset",".dcm");
		testFile.deleteOnExit();
		{
			AttributeList list = makeAttributeList();
			
			{ Attribute a = new UnsignedShortAttribute(useTag); a.addValue(wroteValue); list.put(a); }	// something byte order and explicit/implicit VR form dependent
			
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		{
			AttributeList list = new AttributeList();
			
			list.read(testFile);
			
			int readValue = Attribute.getSingleIntegerValueOrDefault(list,useTag,0);
			
			assertEquals("Checking value read equals wrote",wroteValue,readValue);
		}
	}

	
	public void TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_IVRLE_Dataset() throws Exception {
//System.err.println("TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_IVRLE_Dataset():");
		
		int wroteValue = 0x7010;
		AttributeTag useTag = dictionary.getTagFromName("ReferencedFrameNumbers");
		
		File testFile = File.createTempFile("TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_IVRLE_Dataset",".dcm");
		testFile.deleteOnExit();
		{
			AttributeList list = makeAttributeList();
			
			{ Attribute a = new UnsignedShortAttribute(useTag); a.addValue(wroteValue); list.put(a); }	// something byte order and explicit/implicit VR form dependent
			
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ImplicitVRLittleEndian,"OURAETITLE");
			list.remove(TagFromName.TransferSyntaxUID);
			list.write(testFile,TransferSyntax.ImplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		{
			AttributeList list = new AttributeList();
			
			String transferSyntaxUID = Attribute.getDelimitedStringValuesOrEmptyString(list,TagFromName.TransferSyntaxUID);
			assertEquals("Checking no TransferSyntaxUID","",transferSyntaxUID);
			
			list.read(testFile);
			
			int readValue = Attribute.getSingleIntegerValueOrDefault(list,useTag,0);
			
			assertEquals("Checking value read equals wrote",wroteValue,readValue);
		}
	}
	
	public void TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_EVRLE_Dataset() throws Exception {
//System.err.println("TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_EVRLE_Dataset():");
		
		int wroteValue = 0x7010;
		AttributeTag useTag = dictionary.getTagFromName("ReferencedFrameNumbers");
		
		File testFile = File.createTempFile("TestAttributeListWriteAndReadMetaInformation_NoTransferSyntax_EVRLE_Meta_EVRLE_Dataset",".dcm");
		testFile.deleteOnExit();
		{
			AttributeList list = makeAttributeList();
			
			{ Attribute a = new UnsignedShortAttribute(useTag); a.addValue(wroteValue); list.put(a); }	// something byte order and explicit/implicit VR form dependent
			
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			list.remove(TagFromName.TransferSyntaxUID);
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		{
			AttributeList list = new AttributeList();
			
			list.read(testFile);

			String transferSyntaxUID = Attribute.getDelimitedStringValuesOrEmptyString(list,TagFromName.TransferSyntaxUID);
			assertEquals("Checking no TransferSyntaxUID","",transferSyntaxUID);

			int readValue = Attribute.getSingleIntegerValueOrDefault(list,useTag,0);
			
			assertEquals("Checking value read equals wrote",wroteValue,readValue);
		}
	}
	
	public void TestAttributeListWriteAndReadMetaInformation_EVRLE_Meta_EVRLE_Dataset_CommandGroup() throws Exception {
//System.err.println("TestAttributeListWriteAndReadMetaInformation_EVRLE_Meta_EVRLE_Dataset_CommandGroup():");
		
		File testFile = File.createTempFile("TestAttributeListWriteAndReadMetaInformation_EVRLE_Meta_EVRLE_Dataset_CommandGroup",".dcm");
		testFile.deleteOnExit();
		//System.err.println("TestAttributeListWriteAndReadMetaInformation_EVRLE_Meta_EVRLE_Dataset_CommandGroup(): file ="+testFile.getAbsolutePath());
		{
			AttributeList list = makeAttributeList();
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			addCommandGroup(list);
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
			//System.err.println("TestAttributeListWriteAndReadMetaInformation_EVRLE_Meta_EVRLE_Dataset_CommandGroup(): AttributeList written:\n"+list);
		}
		{
			AttributeList list = new AttributeList();
			list.read(testFile);
			//System.err.println("TestAttributeListWriteAndReadMetaInformation_EVRLE_Meta_EVRLE_Dataset_CommandGroup(): AttributeList read:\n"+list);

			String affectedSOPClassUID = Attribute.getDelimitedStringValuesOrEmptyString(list,TagFromName.AffectedSOPClassUID);
			assertEquals("Checking command group element present",SOPClass.CTImageStorage,affectedSOPClassUID);
		}
	}
	
	public void TestAttributeListWriteAndReadMetaInformation_NoMeta_EVRLE_Dataset_CommandGroup() throws Exception {
//System.err.println("TestAttributeListWriteAndReadMetaInformation_NoMeta_EVRLE_Dataset_CommandGroup():");
		
		File testFile = File.createTempFile("TestAttributeListWriteAndReadMetaInformation_NoMeta_EVRLE_Dataset_CommandGroup",".dcm");
		testFile.deleteOnExit();
		//System.err.println("TestAttributeListWriteAndReadMetaInformation_NoMeta_EVRLE_Dataset_CommandGroup(): file ="+testFile.getAbsolutePath());
		{
			AttributeList list = makeAttributeList();
			addCommandGroup(list);
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,false/*useMeta*/,true/*useBufferedStream*/);
			//System.err.println("TestAttributeListWriteAndReadMetaInformation_NoMeta_EVRLE_Dataset_CommandGroup(): AttributeList written:\n"+list);
		}
		{
			AttributeList list = new AttributeList();
			list.read(testFile);
			//System.err.println("TestAttributeListWriteAndReadMetaInformation_NoMeta_EVRLE_Dataset_CommandGroup(): AttributeList read:\n"+list);

			String affectedSOPClassUID = Attribute.getDelimitedStringValuesOrEmptyString(list,TagFromName.AffectedSOPClassUID);
			assertEquals("Checking command group element present",SOPClass.CTImageStorage,affectedSOPClassUID);
		}
	}
	
	public void TestAttributeListWriteAndReadMetaInformation_WithTransferSyntaxUID() throws Exception {
//System.err.println("TestAttributeListWriteAndReadMetaInformation_WithTransferSyntaxUID():");
		
		File testFile = File.createTempFile("TestAttributeListWriteAndReadMetaInformation_WithTransferSyntaxUID",".dcm");
		testFile.deleteOnExit();
		//System.err.println("TestAttributeListWriteAndReadMetaInformation_WithTransferSyntaxUID(): file ="+testFile.getAbsolutePath());
		AttributeList list = makeAttributeList();
		{
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
			//System.err.println("TestAttributeListWriteAndReadMetaInformation_WithTransferSyntaxUID(): AttributeList written:\n"+list);
		}
		list.removeMetaInformationHeaderAttributes();	// else later comparison will fail
		{
			AttributeList receivedList = new AttributeList();
			receivedList.read(testFile);
			//System.err.println("TestAttributeListWriteAndReadMetaInformation_WithTransferSyntaxUID(): AttributeList read:\n"+receivedList);
			assertEquals("Received list TransferSyntaxUID",TransferSyntax.ExplicitVRLittleEndian,Attribute.getSingleStringValueOrEmptyString(receivedList,TagFromName.TransferSyntaxUID));
			receivedList.removeMetaInformationHeaderAttributes();
			assertTrue("Comparing list read with written",list.equals(receivedList));
		}
	}
	
	public void TestAttributeListWriteAndReadMetaInformation_AbsentTransferSyntaxUID() throws Exception {
//System.err.println("TestAttributeListWriteAndReadMetaInformation_AbsentTransferSyntaxUID():");
		
		File testFile = File.createTempFile("TestAttributeListWriteAndReadMetaInformation_AbsentTransferSyntaxUID",".dcm");
		testFile.deleteOnExit();
		//System.err.println("TestAttributeListWriteAndReadMetaInformation_AbsentTransferSyntaxUID(): file ="+testFile.getAbsolutePath());
		AttributeList list = makeAttributeList();
		{
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			list.remove(TagFromName.TransferSyntaxUID);
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
			//System.err.println("TestAttributeListWriteAndReadMetaInformation_AbsentTransferSyntaxUID(): AttributeList written:\n"+list);
		}
		list.removeMetaInformationHeaderAttributes();	// else later comparison will fail
		{
			AttributeList receivedList = new AttributeList();
			receivedList.read(testFile);
			//System.err.println("TestAttributeListWriteAndReadMetaInformation_AbsentTransferSyntaxUID(): AttributeList read:\n"+receivedList);
			assertEquals("Received list TransferSyntaxUID",null,Attribute.getSingleStringValueOrNull(receivedList,TagFromName.TransferSyntaxUID));
			receivedList.removeMetaInformationHeaderAttributes();
			assertTrue("Comparing list read with written",list.equals(receivedList));
		}
	}
	
	public void TestAttributeListWriteAndReadMetaInformation_EmptyTransferSyntaxUID() throws Exception {
//System.err.println("TestAttributeListWriteAndReadMetaInformation_EmptyTransferSyntaxUID():");
		
		File testFile = File.createTempFile("TestAttributeListWriteAndReadMetaInformation_EmptyTransferSyntaxUID",".dcm");
		testFile.deleteOnExit();
		//System.err.println("TestAttributeListWriteAndReadMetaInformation_EmptyTransferSyntaxUID(): file ="+testFile.getAbsolutePath());
		AttributeList list = makeAttributeList();
		{
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.TransferSyntaxUID); list.put(a); }
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
			//System.err.println("TestAttributeListWriteAndReadMetaInformation_EmptyTransferSyntaxUID(): AttributeList written:\n"+list);
		}
		list.removeMetaInformationHeaderAttributes();	// else later comparison will fail
		{
			AttributeList receivedList = new AttributeList();
			receivedList.read(testFile);
			//System.err.println("TestAttributeListWriteAndReadMetaInformation_EmptyTransferSyntaxUID(): AttributeList read:\n"+receivedList);
			assertEquals("Received list TransferSyntaxUID","",Attribute.getSingleStringValueOrEmptyString(receivedList,TagFromName.TransferSyntaxUID));
			receivedList.removeMetaInformationHeaderAttributes();
			assertTrue("Comparing list read with written",list.equals(receivedList));
		}
	}
	
	public void TestAttributeListWriteMetaInformation_RefuseAbsentTransferSyntaxUID() throws Exception {
		AttributeList list = makeAttributeList();
		boolean threwException = false;
		try {
			FileMetaInformation.addFileMetaInformation(list,null,"OURAETITLE");
			assertTrue("Did not throw exception on adding meta information with null TransferSyntaxUID",false);
		}
		catch (DicomException e) {
			threwException = true;
		}
		assertTrue("Exception on adding meta information with null TransferSyntaxUID",threwException);
	}
	
	public void TestAttributeListWriteMetaInformation_RefuseEmptyTransferSyntaxUID() throws Exception {
		AttributeList list = makeAttributeList();
		boolean threwException = false;
		try {
			FileMetaInformation.addFileMetaInformation(list,"","OURAETITLE");
			assertTrue("Did not throw exception on adding meta information with empty TransferSyntaxUID",false);
		}
		catch (DicomException e) {
			threwException = true;
		}
		assertTrue("Exception on adding meta information with empty TransferSyntaxUID",threwException);
	}
}
