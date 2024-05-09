/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.AgeStringAttribute;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.CodeStringAttribute;
import com.pixelmed.dicom.DateAttribute;
import com.pixelmed.dicom.DecimalStringAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomFileUtilities;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.IntegerStringAttribute;
import com.pixelmed.dicom.LongStringAttribute;
import com.pixelmed.dicom.PersonNameAttribute;
import com.pixelmed.dicom.ShortStringAttribute;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TimeAttribute;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.dicom.UIDGenerator;
import com.pixelmed.dicom.UniqueIdentifierAttribute;
import com.pixelmed.dicom.UnsignedShortAttribute;

import java.io.File;

import junit.framework.*;

public class TestIsDicomFile extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestIsDicomFile(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestIsDicomFile.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestIsDicomFile");
		
		suite.addTest(new TestIsDicomFile("TestIsDicomFile_NormalPreambleAndMetaInformation"));
		suite.addTest(new TestIsDicomFile("TestIsDicomFile_NoPreambleOrMetaInformation"));
		suite.addTest(new TestIsDicomFile("TestIsDicomFile_NormalPreambleAndCommandElementsBeforeMetaInformation"));
		suite.addTest(new TestIsDicomFile("TestIsDicomFile_NoPreambleOrMetaInformationButCommandElementsBeforeDataSet"));

		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}

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

	public void TestIsDicomFile_NormalPreambleAndMetaInformation() throws Exception  {
		File file = File.createTempFile("TestIsDicomFile_NormalPreambleAndMetaInformation",".dcm");
		file.deleteOnExit();
		AttributeList list = makeAttributeList();
		{
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			list.write(file,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		}
		
		assertTrue("isDicomOrAcrNemaFile",DicomFileUtilities.isDicomOrAcrNemaFile(file));
		
		list.removeMetaInformationHeaderAttributes();	// else later comparison will fail
		{
			AttributeList receivedList = new AttributeList();
			receivedList.read(file);
			receivedList.removeMetaInformationHeaderAttributes();
			assertTrue("Comparing list read with written",list.equals(receivedList));
		}
	}

	public void TestIsDicomFile_NoPreambleOrMetaInformation() throws Exception  {
		File file = File.createTempFile("TestIsDicomFile_NoPreambleOrMetaInformation",".dcm");
		file.deleteOnExit();
		AttributeList list = makeAttributeList();
		{
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			list.write(file,TransferSyntax.ExplicitVRLittleEndian,false/*useMeta*/,true/*useBufferedStream*/);
		}
		
		assertTrue("isDicomOrAcrNemaFile",DicomFileUtilities.isDicomOrAcrNemaFile(file));
		
		list.removeMetaInformationHeaderAttributes();	// else later comparison will fail
		{
			AttributeList receivedList = new AttributeList();
			receivedList.read(file);
			receivedList.removeMetaInformationHeaderAttributes();
			assertTrue("Comparing list read with written",list.equals(receivedList));
		}
	}

	public void TestIsDicomFile_NormalPreambleAndCommandElementsBeforeMetaInformation() throws Exception  {
		File file = File.createTempFile("TestIsDicomFile_NormalPreambleAndCommandElementsBeforeMetaInformation",".dcm");
		file.deleteOnExit();
		AttributeList list = makeAttributeList();
		{
			FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,"OURAETITLE");
			addCommandGroup(list);
			list.write(file,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
			//System.err.println("TestIsDicomFile_NormalPreambleAndCommandElementsBeforeMetaInformation(): AttributeList written:\n"+list);
		}
		
		assertTrue("isDicomOrAcrNemaFile",DicomFileUtilities.isDicomOrAcrNemaFile(file));

		list.removeMetaInformationHeaderAttributes();	// else later comparison will fail; also removes command data elements
		{
			AttributeList receivedList = new AttributeList();
			receivedList.read(file);
			//System.err.println("TestIsDicomFile_NormalPreambleAndCommandElementsBeforeMetaInformation(): AttributeList read:\n"+receivedList);
			receivedList.removeMetaInformationHeaderAttributes();	// also removes command data elements
			assertTrue("Comparing list read with written",list.equals(receivedList));
		}
	}
	
	public void TestIsDicomFile_NoPreambleOrMetaInformationButCommandElementsBeforeDataSet() throws Exception  {
		File file = File.createTempFile("TestIsDicomFile_NoPreambleOrMetaInformationButCommandElementsBeforeDataSet",".dcm");
		file.deleteOnExit();
		//System.err.println("TestIsDicomFile_NoPreambleOrMetaInformationButCommandElementsBeforeDataSet(): file ="+file.getAbsolutePath());
		AttributeList list = makeAttributeList();
		{
			addCommandGroup(list);
			list.write(file,TransferSyntax.ExplicitVRLittleEndian,false/*useMeta*/,true/*useBufferedStream*/);
			//System.err.println("TestIsDicomFile_NoPreambleOrMetaInformationButCommandElementsBeforeDataSet(): AttributeList written:\n"+list);
		}
		
		assertTrue("isDicomOrAcrNemaFile",!DicomFileUtilities.isDicomOrAcrNemaFile(file));		// currently does not recognize (001134)

		list.removeMetaInformationHeaderAttributes();	// else later comparison will fail; also removes command data elements
		
		// still reads it OK though ...
		{
			AttributeList receivedList = new AttributeList();
			receivedList.read(file);
			//System.err.println("TestIsDicomFile_NoPreambleOrMetaInformationButCommandElementsBeforeDataSet(): AttributeList read:\n"+receivedList);
			receivedList.removeMetaInformationHeaderAttributes();	// also removes command data elements
			assertTrue("Comparing list read with written",list.equals(receivedList));
		}
	}

}
