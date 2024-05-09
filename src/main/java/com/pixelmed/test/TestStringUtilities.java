/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.utils.StringUtilities;

import junit.framework.*;

public class TestStringUtilities extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestStringUtilities(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestStringUtilities.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestStringUtilities");
		
		suite.addTest(new TestStringUtilities("TestStringUtilities_RemoveTrailingSpaces"));
		suite.addTest(new TestStringUtilities("TestStringUtilities_CompareEmbeddedNonZeroPaddedIntegers"));
		suite.addTest(new TestStringUtilities("TestStringUtilities_Truncate"));

		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void TestStringUtilities_RemoveTrailingSpaces() throws Exception  {
		assertEquals("Checking no spaces","1234",StringUtilities.removeTrailingSpaces("1234"));
		assertEquals("Checking trailing spaces","1234",StringUtilities.removeTrailingSpaces("1234  "));
		assertEquals("Checking middle and trailing spaces","12  34",StringUtilities.removeTrailingSpaces("12  34  "));
		assertEquals("Checking leading spaces","  1234",StringUtilities.removeTrailingSpaces("  1234"));
		assertEquals("Checking leading and trailing spaces","  1234",StringUtilities.removeTrailingSpaces("  1234  "));
		assertEquals("Checking single digit","1",StringUtilities.removeTrailingSpaces("1"));
		assertEquals("Checking just space","",StringUtilities.removeTrailingSpaces(" "));
		assertEquals("Checking just spaces","",StringUtilities.removeTrailingSpaces("    "));
		assertEquals("Checking nothing","",StringUtilities.removeTrailingSpaces(""));
	}
	
	public void TestStringUtilities_CompareEmbeddedNonZeroPaddedIntegers() throws Exception  {
		assertTrue("Checking equal",         StringUtilities.compareStringsWithEmbeddedNonZeroPaddedIntegers("this is 2 way","this is 2 way") == 0);
		assertTrue("Checking less",          StringUtilities.compareStringsWithEmbeddedNonZeroPaddedIntegers("this is 2 way","this is 10 way") < 0);
		assertTrue("Checking greater",       StringUtilities.compareStringsWithEmbeddedNonZeroPaddedIntegers("this is 10 way","this is 2 way") > 0);
		assertTrue("Checking equal longer",  StringUtilities.compareStringsWithEmbeddedNonZeroPaddedIntegers("this is 2 way","this is 2 way plus") == 0);
		assertTrue("Checking less longer",   StringUtilities.compareStringsWithEmbeddedNonZeroPaddedIntegers("this is 2 way","this is 10 way plus") < 0);
		assertTrue("Checking greater longer",StringUtilities.compareStringsWithEmbeddedNonZeroPaddedIntegers("this is 10 way","this is 2 way plus") > 0);
	}

	public void TestStringUtilities_Truncate() throws Exception  {
		assertEquals("Checking equal","123",StringUtilities.getStringNoLongerThanTruncatedIfNecessary("123",3));
		assertEquals("Checking longer","123",StringUtilities.getStringNoLongerThanTruncatedIfNecessary("1234",3));
		assertEquals("Checking shorter","12",StringUtilities.getStringNoLongerThanTruncatedIfNecessary("12",3));
		assertEquals("Checking empty","",StringUtilities.getStringNoLongerThanTruncatedIfNecessary("",3));
	}
}
