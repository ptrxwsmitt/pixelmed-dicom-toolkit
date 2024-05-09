/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestHierarchicalFileName extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestHierarchicalFileName(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestHierarchicalFileName.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestHierarchicalFileName");
		
		suite.addTest(new TestHierarchicalFileName("TestHierarchicalFileName_Default"));
		suite.addTest(new TestHierarchicalFileName("TestHierarchicalFileName_DefaultNoSOPInstanceUID"));
		suite.addTest(new TestHierarchicalFileName("TestHierarchicalFileName_DefaultSpecialCharactersNotRemovedFromUID"));
		suite.addTest(new TestHierarchicalFileName("TestHierarchicalFileName_SpecialCharactersRemovedFromUID"));
		suite.addTest(new TestHierarchicalFileName("TestHierarchicalFileName_InstanceNumber_Strategy"));
		suite.addTest(new TestHierarchicalFileName("TestHierarchicalFileName_NoInstanceNumberFallBackSOPInstanceUID_Strategy"));
		suite.addTest(new TestHierarchicalFileName("TestHierarchicalFileName_InstanceNumber_Override"));
		suite.addTest(new TestHierarchicalFileName("TestHierarchicalFileName_NoInstanceNumberFallBackSOPInstanceUID_Override"));

		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}

	private static final String patientID = "1234";
	private static final String patientName = "Name^Me";
	private static final String studyDate = "19750304";
	private static final String studyTime = "111111";
	private static final String studyID = "5678";
	private static final String studyDescription = "Pretty study";
	private static final String seriesNumber = "32";	// will be zero padded to three digits
	private static final String modality = "MR";
	private static final String seriesDescription = "Nice series";
	private static final String sopInstanceUID = "1.2.3.4";
	
	private AttributeList createattributeList() throws DicomException {
		AttributeList list = new AttributeList();
		{ Attribute a = new ShortStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.StudyDescription); a.addValue(studyDescription); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Modality); a.addValue(modality); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.SeriesDescription); a.addValue(seriesDescription); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
		return list;
	}

	public void TestHierarchicalFileName_Default() throws Exception {
		AttributeList list = createattributeList();
		String expectPathName = patientName+" ["+patientID+"]/"+studyDate+" "+studyTime+" ["+studyID+" - "+studyDescription+"]/Series 0"+seriesNumber+" ["+modality+" - "+seriesDescription+"]/"+sopInstanceUID+".dcm";
		String pathName = MoveDicomFilesIntoHierarchy.makeHierarchicalPathFromAttributes(list);
		assertEquals("Checking pathName",expectPathName,pathName);
	}

	public void TestHierarchicalFileName_DefaultNoSOPInstanceUID() throws Exception {
		AttributeList list = createattributeList();
		list.remove(TagFromName.SOPInstanceUID);
		String expectPathName = "";
		String pathName = MoveDicomFilesIntoHierarchy.makeHierarchicalPathFromAttributes(list);
		assertEquals("Checking pathName",expectPathName,pathName);
	}

	public void TestHierarchicalFileName_DefaultSpecialCharactersNotRemovedFromUID() throws Exception {
		AttributeList list = new AttributeList();
		{ Attribute a = new ShortStringAttribute(TagFromName.PatientID); a.addValue(".+^/"+patientID+"@#$%"); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(".+/"+patientName+"@#$%"); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(".+/^"+studyDate+"@#$%"); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue("+/^"+studyTime+"@#$%"); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(".+/^"+studyID+"@#$%"); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.StudyDescription); a.addValue(".+/^"+studyDescription+"@#$%"); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(".+/^"+seriesNumber+"@#$%"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Modality); a.addValue(".+/^"+modality+"@#$%"); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.SeriesDescription); a.addValue(".+/^"+seriesDescription+"@#$%"); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue("+/^"+sopInstanceUID+"@#$%"); list.put(a); }

		String expectPathName = "__"+patientName+" ["+patientID+"]/"+studyDate+" "+studyTime+" ["+studyID+" - "+studyDescription+"]/Series 0"+seriesNumber+" ["+modality+" - "+seriesDescription+"]/"+"+/^"+sopInstanceUID+"@#$%"+".dcm";
		String pathName = MoveDicomFilesIntoHierarchy.makeHierarchicalPathFromAttributes(list);
		assertEquals("Checking pathName",expectPathName,pathName);
	}

	public void TestHierarchicalFileName_SpecialCharactersRemovedFromUID() throws Exception {
		AttributeList list = new AttributeList();
		{ Attribute a = new ShortStringAttribute(TagFromName.PatientID); a.addValue(".+^/"+patientID+"@#$%"); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(".+/"+patientName+"@#$%"); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(".+/^"+studyDate+"@#$%"); list.put(a); }
		{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue("+/^"+studyTime+"@#$%"); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(".+/^"+studyID+"@#$%"); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.StudyDescription); a.addValue(".+/^"+studyDescription+"@#$%"); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(".+/^"+seriesNumber+"@#$%"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.Modality); a.addValue(".+/^"+modality+"@#$%"); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.SeriesDescription); a.addValue(".+/^"+seriesDescription+"@#$%"); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue("+/^"+sopInstanceUID+"@#$%"); list.put(a); }

		String expectPathName = "__"+patientName+" ["+patientID+"]/"+studyDate+" "+studyTime+" ["+studyID+" - "+studyDescription+"]/Series 0"+seriesNumber+" ["+modality+" - "+seriesDescription+"]/"+sopInstanceUID+".dcm";
		String pathName = new MoveDicomFilesIntoHierarchy().makeHierarchicalPathFromAttributes(list,MoveDicomFilesIntoHierarchy.FolderNamingStrategy.instanceByUID);
		assertEquals("Checking pathName",expectPathName,pathName);
	}

	protected class InstanceNumberMoveDicomFilesIntoHierarchy extends MoveDicomFilesIntoHierarchy {
		public String makeInstanceLabelFromAttributes(AttributeList list,int folderNamingStrategy) {
			String instanceLabel = "";
			String instanceNumber = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.InstanceNumber).replaceAll("[^0-9]","").trim();
			if (instanceNumber.length() > 0) {
				while (instanceNumber.length() < 3) { instanceNumber = "0" + instanceNumber; }
				instanceLabel = instanceNumber + ".dcm";
			}
			if (instanceLabel.length() == 0) {
				// do not want to leave unlabelled if we can find something else like SOPInstanceUID ...
				instanceLabel = super.makeInstanceLabelFromAttributes(list, FolderNamingStrategy.instanceByUID);
			}
			return instanceLabel;
		}
	}

	public void TestHierarchicalFileName_InstanceNumber_Strategy() throws Exception {
		String instanceNumber = "41";	// will be zero padded to six digits
		AttributeList list = createattributeList();
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(".+/^"+instanceNumber); list.put(a); }
		String expectPathName = patientName+" ["+patientID+"]/"+studyDate+" "+studyTime+" ["+studyID+" - "+studyDescription+"]/Series 0"+seriesNumber+" ["+modality+" - "+seriesDescription+"]/0000"+instanceNumber+".dcm";
		String pathName = new MoveDicomFilesIntoHierarchy().makeHierarchicalPathFromAttributes(list,MoveDicomFilesIntoHierarchy.FolderNamingStrategy.instanceByNumber);
		//System.err.println("TestHierarchicalFileName_InstanceNumber pathName"+pathName);
		assertEquals("Checking pathName",expectPathName,pathName);
	}
	
	public void TestHierarchicalFileName_NoInstanceNumberFallBackSOPInstanceUID_Strategy() throws Exception {
		AttributeList list = createattributeList();
		String expectPathName = patientName+" ["+patientID+"]/"+studyDate+" "+studyTime+" ["+studyID+" - "+studyDescription+"]/Series 0"+seriesNumber+" ["+modality+" - "+seriesDescription+"]/"+sopInstanceUID+".dcm";
		String pathName = new MoveDicomFilesIntoHierarchy().makeHierarchicalPathFromAttributes(list,MoveDicomFilesIntoHierarchy.FolderNamingStrategy.instanceByNumber);
		//System.err.println("TestHierarchicalFileName_InstanceNumber pathName"+pathName);
		assertEquals("Checking pathName",expectPathName,pathName);
	}

	public void TestHierarchicalFileName_InstanceNumber_Override() throws Exception {
		String instanceNumber = "41";	// will be zero padded to three digits
		AttributeList list = createattributeList();
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(".+/^"+instanceNumber); list.put(a); }
		String expectPathName = patientName+" ["+patientID+"]/"+studyDate+" "+studyTime+" ["+studyID+" - "+studyDescription+"]/Series 0"+seriesNumber+" ["+modality+" - "+seriesDescription+"]/0"+instanceNumber+".dcm";
		String pathName = new InstanceNumberMoveDicomFilesIntoHierarchy().makeHierarchicalPathFromAttributes(list,0/*strategy irrelevant*/);
		//System.err.println("TestHierarchicalFileName_InstanceNumber pathName"+pathName);
		assertEquals("Checking pathName",expectPathName,pathName);
	}
	
	public void TestHierarchicalFileName_NoInstanceNumberFallBackSOPInstanceUID_Override() throws Exception {
		AttributeList list = createattributeList();
		String expectPathName = patientName+" ["+patientID+"]/"+studyDate+" "+studyTime+" ["+studyID+" - "+studyDescription+"]/Series 0"+seriesNumber+" ["+modality+" - "+seriesDescription+"]/"+sopInstanceUID+".dcm";
		String pathName = new InstanceNumberMoveDicomFilesIntoHierarchy().makeHierarchicalPathFromAttributes(list,0/*strategy irrelevant*/);
		//System.err.println("TestHierarchicalFileName_InstanceNumber pathName"+pathName);
		assertEquals("Checking pathName",expectPathName,pathName);
	}
}
