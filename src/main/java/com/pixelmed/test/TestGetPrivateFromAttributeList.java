/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestGetPrivateFromAttributeList extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestGetPrivateFromAttributeList(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestGetPrivateFromAttributeList.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestGetPrivateFromAttributeList");
		
		suite.addTest(new TestGetPrivateFromAttributeList("TestGetPrivateFromAttributeList_CreatorFromList"));
		
		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}

	private static final AttributeTag diffusionDirectionTag = new AttributeTag(0x0021,0x005a);
	private static final String diffusionDirectionCreator = "GEMS_RELA_01";
	
	public void TestGetPrivateFromAttributeList_CreatorFromList() throws Exception {
		{
			long longValue = 132l;
			AttributeList list = new AttributeList();
			{ Attribute a = new LongStringAttribute(new AttributeTag(0x0021,0x0010)); a.addValue(diffusionDirectionCreator); list.put(a); }
			{ Attribute a = new SignedLongAttribute(new AttributeTag(0x0021,0x105a)); a.addValue(longValue); list.put(a); }
			assertEquals("get(tag,creator) works",longValue,list.get(diffusionDirectionTag,diffusionDirectionCreator).getSingleLongValueOrDefault(0l));
		}
	}
}
