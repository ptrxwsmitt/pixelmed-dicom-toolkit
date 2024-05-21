/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import junit.framework.*;

import java.io.File;

public class TestAttributeListWriteAndReadTextAttribute extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestAttributeListWriteAndReadTextAttribute(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestAttributeListWriteAndReadTextAttribute.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestAttributeListWriteAndReadTextAttribute");
		
		suite.addTest(new TestAttributeListWriteAndReadTextAttribute("TestAttributeListWriteAndReadTextAttribute_AESAsUT"));

		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}

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
	
	public void TestAttributeListWriteAndReadTextAttribute_AESAsUT() throws Exception {
	
//System.err.println("TestAttributeListWriteAndReadTextAttribute_AESAsUT():");
	
		String wroteValue = "w6qdg5GRqdGPMcogEBYp1X2ivNofRhvAp8YIMUIV5z3/VneQVNmSBmNCppNOMN1scm8wgcecif71IWrIDBHiSQVAM6ghqPMHfYJMIbJfRoXOHlJzSH5G3T7lu3VatBWn";
		AttributeTag useTag = TagFromName.TextValue;
	
		File testFile = File.createTempFile("TestAttributeListWriteAndReadTextAttribute_AESAsUT",".dcm");
		testFile.deleteOnExit();
		{
			AttributeList list = makeAttributeList();

			{ Attribute a = new UnlimitedTextAttribute(useTag); a.addValue(wroteValue); list.put(a); }
			
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ImplicitVRLittleEndian,"OURAETITLE");
			list.write(testFile,TransferSyntax.ImplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		{
			AttributeList list = new AttributeList();

			list.read(testFile);
			
			String readValue = Attribute.getDelimitedStringValuesOrEmptyString(list,useTag);
			
			assertEquals("Checking value read equals wrote",wroteValue,readValue);
		}
	}
	
}
