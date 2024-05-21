/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed;

import junit.framework.*;

public class TestSafePrivate_AllTests extends TestCase {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All JUnit Tests");
		suite.addTest(TestSafePrivatePhilipsPETRelated.suite());
		suite.addTest(TestSafePrivatePhilipsDoseRelated.suite());
		suite.addTest(TestSafePrivateGEDoseRelated.suite());
		suite.addTest(TestSafePrivateGEPACSRelated.suite());
		suite.addTest(TestSafePrivateGEMRRelated.suite());
		suite.addTest(TestSafePrivateNQResultsRelated.suite());
		suite.addTest(TestSafePrivateSiemensUltrasoundRawDataRelated.suite());
		return suite;
	}
	
}
