/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestSafePrivateSiemensUltrasoundRawDataRelated extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSafePrivateSiemensUltrasoundRawDataRelated(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSafePrivateSiemensUltrasoundRawDataRelated.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSafePrivateSiemensUltrasoundRawDataRelated");
		
		suite.addTest(new TestSafePrivateSiemensUltrasoundRawDataRelated("TestSafePrivateSiemensUltrasoundRawDataRelated_ScaleFactors_FromTag"));
		suite.addTest(new TestSafePrivateSiemensUltrasoundRawDataRelated("TestSafePrivateSiemensUltrasoundRawDataRelated_ScaleFactors_FromList"));
		
		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestSafePrivateSiemensUltrasoundRawDataRelated_ScaleFactors_FromTag() throws Exception {
		assertTrue("Checking AcousticMetaInformationVersion is safe",ClinicalTrialsAttributes.isSafePrivateAttribute("SIEMENS Ultrasound SC2000",new AttributeTag(0x0119,0x0000)));
		assertTrue("Checking VolumeVersionID is safe",ClinicalTrialsAttributes.isSafePrivateAttribute("SIEMENS SYNGO ULTRA-SOUND TOYON DATA STREAMING",new AttributeTag(0x7fd1,0x0009)));
	}
	
	public void TestSafePrivateSiemensUltrasoundRawDataRelated_ScaleFactors_FromList() throws Exception {
		AttributeList list = new AttributeList();
		{ Attribute a = new LongStringAttribute(new AttributeTag(0x0119,0x0010)); a.addValue("SIEMENS Ultrasound SC2000"); list.put(a); }
		assertTrue("Checking AcousticMetaInformationVersion is safe",ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x0119,0x1000),list));
		{ Attribute a = new LongStringAttribute(new AttributeTag(0x0119,0x1000)); a.addValue("Bla"); list.put(a); }
		
		{ Attribute a = new LongStringAttribute(new AttributeTag(0x7fd1,0x0010)); a.addValue("SIEMENS SYNGO ULTRA-SOUND TOYON DATA STREAMING"); list.put(a); }
		assertTrue("Checking VolumeVersionID is safe",ClinicalTrialsAttributes.isSafePrivateAttribute(new AttributeTag(0x7fd1,0x1009),list));
		assertTrue("Checking VolumeVersionID is non-transient so won't be removed/replaced",!UniqueIdentifierAttribute.isTransient(new AttributeTag(0x7fd1,0x1009),list));
		{ Attribute a = new UniqueIdentifierAttribute(new AttributeTag(0x7fd1,0x1009)); a.addValue("1.2"); list.put(a); }
		
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(SOPClass.RawDataStorage); list.put(a); }		// check that adding private non-transient UID detection did not break standard non-transient UID detection
		
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,ClinicalTrialsAttributes.HandleUIDs.remap,false/*keepDescriptors*/,false/*keepSeriesDescriptors*/,false/*keepPatientCharacteristics)*/,false/*keepDeviceIdentity)*/,false/*keepInstitutionIdentity)*/);
		assertTrue("Checking AcousticMetaInformationVersion was not removed",list.get(new AttributeTag(0x0119,0x1000)) != null);
		assertTrue("Checking VolumeVersionID was not changed",Attribute.getSingleStringValueOrEmptyString(list,new AttributeTag(0x7fd1,0x1009)).equals("1.2"));
		assertTrue("Checking SOPClassUID was not changed",Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SOPClassUID).equals(SOPClass.RawDataStorage));
	}
}
