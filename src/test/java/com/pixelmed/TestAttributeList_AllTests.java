/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed;

import junit.framework.*;

public class TestAttributeList_AllTests extends TestCase {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("All JUnit Tests");
		suite.addTest(TestUnknownSequenceValueRepresentation.suite());
		suite.addTest(TestPrivateCreatorValueRepresentation.suite());
		suite.addTest(TestAttributeListReadTerminationStrategy.suite());
		suite.addTest(TestDecompressedImagePixelModuleAndLossyImageCompressionHistory.suite());
		suite.addTest(TestAttributeListReadUnspecifiedShortAttribute.suite());
		suite.addTest(TestAttributeListWriteAndReadTextAttribute.suite());
		suite.addTest(TestAttributeListWriteAndReadMetaInformation.suite());
		return suite;
	}
	
}
