/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed;

import junit.framework.*;

public class TestDeidentify_AllTests extends TestCase {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All JUnit Tests");
		suite.addTest(TestDeidentify.suite());
		suite.addTest(TestDeidentifyStructuredContent.suite());
		return suite;
	}
	
}
