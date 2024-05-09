/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.DateTimeAttribute;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.*;

public class TestDateTimeAttributeTimeZone extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestDateTimeAttributeTimeZone(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestDateTimeAttributeTimeZone.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestDateTimeAttributeTimeZone");
		
		suite.addTest(new TestDateTimeAttributeTimeZone("TestDateTimeAttributeTimeZone_Compare"));
		suite.addTest(new TestDateTimeAttributeTimeZone("TestDateTimeAttributeTimeZone_WinterNorthernHemisphere"));
		suite.addTest(new TestDateTimeAttributeTimeZone("TestDateTimeAttributeTimeZone_SummerNorthernHemisphere"));
		suite.addTest(new TestDateTimeAttributeTimeZone("TestDateTimeAttributeTimeZone_WinterSouthernHemisphere"));
		suite.addTest(new TestDateTimeAttributeTimeZone("TestDateTimeAttributeTimeZone_SummerSouthernHemisphere"));

		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestDateTimeAttributeTimeZone_Compare() {
		String[] dicom = {
			"+0000",
			"+0000",
			"-0800",
			"+0100",
			"+1000"
		};
		TimeZone[] java = {
			TimeZone.getTimeZone("GMT"),
			TimeZone.getTimeZone("Europe/London"),
			TimeZone.getTimeZone("America/Los_Angeles"),
			TimeZone.getTimeZone("Europe/Berlin"),
			TimeZone.getTimeZone("Australia/Melbourne")
		};
		
		for (int i=0; i<dicom.length; ++i ) {
			assertEquals(dicom[i],java[i].getRawOffset(),DateTimeAttribute.getTimeZone(dicom[i]).getRawOffset());	// test raw offset, not just equality, since have different IDs
		}
	}
	
	public void TestDateTimeAttributeTimeZone_WinterNorthernHemisphere() {
		String[] dicom = {
			"+0000",
			"+0000",
			"-0800",
			"+0100",
		};
		TimeZone[] java = {
			TimeZone.getTimeZone("GMT"),
			TimeZone.getTimeZone("Europe/London"),
			TimeZone.getTimeZone("America/Los_Angeles"),
			TimeZone.getTimeZone("Europe/Berlin"),
		};
		
		for (int i=0; i<dicom.length; ++i ) {
			Calendar calendarInSeason = Calendar.getInstance(java[i],Locale.US);
			calendarInSeason.set(2000,12,31);
			Date dateInSeason = calendarInSeason.getTime();
			assertEquals(dicom[i],dicom[i],DateTimeAttribute.getTimeZone(java[i],dateInSeason));
		}
	}
	
	public void TestDateTimeAttributeTimeZone_SummerNorthernHemisphere() {
		String[] dicom = {
			"+0000",
			"+0100",
			"-0700",
			"+0200",
		};
		TimeZone[] java = {
			TimeZone.getTimeZone("GMT"),
			TimeZone.getTimeZone("Europe/London"),
			TimeZone.getTimeZone("America/Los_Angeles"),
			TimeZone.getTimeZone("Europe/Berlin"),
		};
		
		for (int i=0; i<dicom.length; ++i ) {
			Calendar calendarInSeason = Calendar.getInstance(java[i],Locale.US);
			calendarInSeason.set(2000,06,30);
			Date dateInSeason = calendarInSeason.getTime();
			assertEquals(dicom[i],dicom[i],DateTimeAttribute.getTimeZone(java[i],dateInSeason));
		}
	}

	public void TestDateTimeAttributeTimeZone_WinterSouthernHemisphere() {
		String[] dicom = {
			"+1000"
		};
		TimeZone[] java = {
			TimeZone.getTimeZone("Australia/Melbourne")
		};
		
		for (int i=0; i<dicom.length; ++i ) {
			Calendar calendarInSeason = Calendar.getInstance(java[i],Locale.US);
			calendarInSeason.set(2000,06,30);
			Date dateInSeason = calendarInSeason.getTime();
			assertEquals(dicom[i],dicom[i],DateTimeAttribute.getTimeZone(java[i],dateInSeason));
		}
	}

	public void TestDateTimeAttributeTimeZone_SummerSouthernHemisphere() {
		String[] dicom = {
			"+1100"
		};
		TimeZone[] java = {
			TimeZone.getTimeZone("Australia/Melbourne")
		};
		
		for (int i=0; i<dicom.length; ++i ) {
			Calendar calendarInSeason = Calendar.getInstance(java[i],Locale.US);
			calendarInSeason.set(2000,12,31);
			Date dateInSeason = calendarInSeason.getTime();
			assertEquals(dicom[i],dicom[i],DateTimeAttribute.getTimeZone(java[i],dateInSeason));
		}
	}

}
