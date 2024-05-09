/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.anatproc.*;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.CodeStringAttribute;
import com.pixelmed.dicom.DecimalStringAttribute;
import com.pixelmed.dicom.LossyImageCompression;
import com.pixelmed.dicom.TagFromName;

import junit.framework.*;

import java.io.File;

public class TestLossyImageCompressionDisplayAnnotation extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestLossyImageCompressionDisplayAnnotation(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestLaterality.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestLossyImageCompressionDisplayAnnotation");
		
		suite.addTest(new TestLossyImageCompressionDisplayAnnotation("TestLossyImageCompressionDisplayAnnotation_values"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private static final String[] suppliedLossy = {
		"",
		"01"
	};
	
	private static final String[] suppliedMethod = {
		"",
		"ISO_10918_1"
	};

	private static final String[] suppliedRatio = {
		"0",
		"0.0",
		"1",
		"1.50",
		"10",
		"10.",
		"10.0"
	};
	
	private static final String[][][] expected = {
		{
			{
				"",
				"",
				"Lossy 1:1",
				"Lossy 1.5:1",
				"Lossy 10:1",
				"Lossy 10:1",
				"Lossy 10:1"
			},
			{
				"Lossy JPEG",
				"Lossy JPEG",
				"JPEG 1:1",		// explicit because Lossy is "01" even though ratio is "1", which implies lossless - no specific check for this
				"JPEG 1.5:1",
				"JPEG 10:1",
				"JPEG 10:1",
				"JPEG 10:1"
			}
		},
		{
			{
				"Lossy",
				"Lossy",
				"Lossy 1:1",
				"Lossy 1.5:1",
				"Lossy 10:1",
				"Lossy 10:1",
				"Lossy 10:1"
			},
			{
				"Lossy JPEG",
				"Lossy JPEG",
				"JPEG 1:1",
				"JPEG 1.5:1",
				"JPEG 10:1",
				"JPEG 10:1",
				"JPEG 10:1"
			}
		}
	};
	
	public void TestLossyImageCompressionDisplayAnnotation_values() throws Exception {
		AttributeList list = new AttributeList();
		for (int l=0; l<suppliedLossy.length; ++l) {
			for (int m=0; m<suppliedMethod.length; ++m) {
				for (int r=0; r<suppliedRatio.length; ++r) {
					{ Attribute a = new CodeStringAttribute(TagFromName.LossyImageCompression); a.addValue(suppliedLossy[l]); list.put(a); }
					{ Attribute a = new CodeStringAttribute(TagFromName.LossyImageCompressionMethod); a.addValue(suppliedMethod[m]); list.put(a); }
					{ Attribute a = new DecimalStringAttribute(TagFromName.LossyImageCompressionRatio); a.addValue(suppliedRatio[r]); list.put(a); }
					String result = LossyImageCompression.describeLossyCompression(list);
					assertEquals("Checking l="+suppliedLossy[l]+", m="+suppliedMethod[m]+", r="+suppliedRatio[r]+" got ",expected[l][m][r],result);
				}
			}
		}
	}
	
}
