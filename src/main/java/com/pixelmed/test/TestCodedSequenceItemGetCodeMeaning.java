/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

import java.io.File;

public class TestCodedSequenceItemGetCodeMeaning extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestCodedSequenceItemGetCodeMeaning(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCodedSequenceItemGetCodeMeaning.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCodedSequenceItemGetCodeMeaning");
		
		suite.addTest(new TestCodedSequenceItemGetCodeMeaning("TestCodedSequenceItemGetCodeMeaning"));
		
		return suite;
	}
			
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestCodedSequenceItemGetCodeMeaning() throws Exception {
		String cv = "R-00339";
		String csd = "SRT";
		String cm = "No";
		
		AttributeList list = new AttributeList();
		{
			SequenceAttribute sa = new SequenceAttribute(TagFromName.MeasurementUnitsCodeSequence);
			list.put(sa);
			sa.addItem(new CodedSequenceItem(cv,csd,cm).getAttributeList());
		}
						
		assertEquals("Checking cm",cm,SequenceAttribute.getMeaningOfCodedSequenceAttributeOrEmptyString(list,TagFromName.MeasurementUnitsCodeSequence));
	}
	
}
