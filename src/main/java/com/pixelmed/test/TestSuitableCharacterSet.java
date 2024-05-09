/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;
import com.pixelmed.utils.HexDump;

import junit.framework.*;

import java.io.ByteArrayOutputStream;

public class TestSuitableCharacterSet extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestSuitableCharacterSet(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestSuitableCharacterSet.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestSuitableCharacterSet");
		
		suite.addTest(new TestSuitableCharacterSet("TestSuitableCharacterSet_PersonName_ASCII"));
		suite.addTest(new TestSuitableCharacterSet("TestSuitableCharacterSet_PersonName_IR100"));
		suite.addTest(new TestSuitableCharacterSet("TestSuitableCharacterSet_PersonName_IR192_TriggeredByHebrew"));
		suite.addTest(new TestSuitableCharacterSet("TestSuitableCharacterSet_PersonName_IR192_TriggeredByJapanese"));

		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	public void testPersonNameValues(String specificCharacterSetValue,String string1,String string2) throws Exception {
		{
			SpecificCharacterSet suppliedCharSet = new SpecificCharacterSet(new String[] { specificCharacterSetValue});
			assertEquals("Checking supplied getValueToUseInSpecificCharacterSetAttribute()",specificCharacterSetValue,suppliedCharSet.getValueToUseInSpecificCharacterSetAttribute());

			PersonNameAttribute a = new PersonNameAttribute(TagFromName.PersonName,suppliedCharSet);
			a.addValue(string1);
			a.addValue(string2);
			
			AttributeList list = new AttributeList();
			SequenceAttribute sa1 = new SequenceAttribute(TagFromName.ContentSequence);
			list.put(sa1);
			{
				AttributeList subList1_1 = new AttributeList();
				sa1.addItem(subList1_1);
				AttributeList subList1_2 = new AttributeList();
				sa1.addItem(subList1_2);
				
				subList1_2.put(a);
			}
			SpecificCharacterSet needCharSet = new SpecificCharacterSet(list);
			assertEquals("Checking needed getValueToUseInSpecificCharacterSetAttribute()",specificCharacterSetValue,needCharSet.getValueToUseInSpecificCharacterSetAttribute());
		}
	}

	public void TestSuitableCharacterSet_PersonName_ASCII() throws Exception {
		testPersonNameValues("","Aneas^Rudiger","Buc^Jerome");
	}

	public void TestSuitableCharacterSet_PersonName_IR100() throws Exception {
		testPersonNameValues("ISO_IR 100","Äneas^Rüdiger","Buc^Jérôme");
	}

	public void TestSuitableCharacterSet_PersonName_IR192_TriggeredByHebrew() throws Exception {
		testPersonNameValues("ISO_IR 192","שָׁרוֹן^דְּבוֹרָה","Sharon^Deborah");
	}
	
	public void TestSuitableCharacterSet_PersonName_IR192_TriggeredByJapanese() throws Exception {
		testPersonNameValues("ISO_IR 192","黒澤^明","Kurosawa^Akira");
	}

}
