/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed;

import com.pixelmed.dicom.*;

import junit.framework.*;

import java.io.File;

public class TestUnknownSequenceValueRepresentation extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestUnknownSequenceValueRepresentation(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestUnknownSequenceValueRepresentation.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestUnknownSequenceValueRepresentation");
		
		suite.addTest(new TestUnknownSequenceValueRepresentation("TestUnknownSequenceValueRepresentation_FromExplicitVR_WithDefaultBufferedInput"));
		suite.addTest(new TestUnknownSequenceValueRepresentation("TestUnknownSequenceValueRepresentation_FromExplicitVR_WithExplicitBufferedInput"));
		suite.addTest(new TestUnknownSequenceValueRepresentation("TestUnknownSequenceValueRepresentation_FromExplicitVR_WithoutBufferedInput"));
		suite.addTest(new TestUnknownSequenceValueRepresentation("TestUnknownSequenceValueRepresentation_FromImplicitVR"));
		
		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	protected class OurUnknownAttribute extends UnknownAttribute {
		OurUnknownAttribute(AttributeTag t,byte[] originalLittleEndianByteValues) {
			super(t);
			this.originalLittleEndianByteValues = originalLittleEndianByteValues;
			valueLength=originalLittleEndianByteValues.length;
			assert(valueLength%2 == 0);
			valueMultiplicity=1;
		}
	}
	
//(0x0019,0x0010) LO PrivateCreator 	 VR=<LO>   VL=<0x000e>  <HOLOGIC, Inc. >
//(0x0019,0x108a) SQ Marker Sequence 	 VR=<SQ>   VL=<0xffffffff>
//  ----:
//    > (0x0019,0x0010) LO PrivateCreator 	 VR=<LO>   VL=<0x000e>  <HOLOGIC, Inc. >
//    > (0x0019,0x1087) LO Marker Text 	 VR=<LO>   VL=<0x0004>  <LMLO>
//    > (0x0019,0x1089) DS Marker Location 	 VR=<DS>   VL=<0x0008>  <1868\419>
//
//(0x0019,0x108a) SQ Marker Sequence 	 VR=<UN>   VL=<0x003a>  [0xfe,0xff,0x00,0xe0,0x32,0x00,0x00,0x00,0x19,0x00,0x10,0x00,0x0e,0x00,0x00,0x00,0x48,0x4f,0x4c,0x4f,0x47,0x49,0x43,0x2c,0x20,0x49,0x6e,0x63,0x2e,0x20,0x19,0x00,0x87,0x10,0x04,0x00,0x00,0x00,0x4c,0x4d,0x4c,0x4f,0x19,0x00,0x89,0x10,0x08,0x00,0x00,0x00,0x31,0x38,0x36,0x38,0x5c,0x34,0x31,0x39] 

	private static final byte[] unknownBytes = {
			(byte)0xfe,(byte)0xff,(byte)0x00,(byte)0xe0,(byte)0x32,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x19,(byte)0x00,
			(byte)0x10,(byte)0x00,(byte)0x0e,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x48,(byte)0x4f,(byte)0x4c,(byte)0x4f,
			(byte)0x47,(byte)0x49,(byte)0x43,(byte)0x2c,(byte)0x20,(byte)0x49,(byte)0x6e,(byte)0x63,(byte)0x2e,(byte)0x20,
			(byte)0x19,(byte)0x00,(byte)0x87,(byte)0x10,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x4c,(byte)0x4d,
			(byte)0x4c,(byte)0x4f,(byte)0x19,(byte)0x00,(byte)0x89,(byte)0x10,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00,
			(byte)0x31,(byte)0x38,(byte)0x36,(byte)0x38,(byte)0x5c,(byte)0x34,(byte)0x31,(byte)0x39};

	
	public void TestUnknownSequenceValueRepresentation_FromExplicitVR_WithDefaultBufferedInput() throws Exception {
		File testFile = File.createTempFile("TestUnknownSequenceValueRepresentation_FromExplicitVR",".dcm");
		testFile.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new LongStringAttribute(new AttributeTag(0x0019,0x0010)); a.addValue("HOLOGIC, Inc."); list.put(a); }
			list.put(new OurUnknownAttribute(new AttributeTag(0x0019,0x108a),unknownBytes));
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			AttributeList list = new AttributeList();
			list.read(testFile);
			Attribute a = list.get(new AttributeTag(0x0019,0x108a));
			assertTrue("Checking UN was read as SQ",a instanceof SequenceAttribute);

		}
	}
	
	public void TestUnknownSequenceValueRepresentation_FromExplicitVR_WithExplicitBufferedInput() throws Exception {
		File testFile = File.createTempFile("TestUnknownSequenceValueRepresentation_FromExplicitVR_WithExplicitBufferedInput",".dcm");
		testFile.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new LongStringAttribute(new AttributeTag(0x0019,0x0010)); a.addValue("HOLOGIC, Inc."); list.put(a); }
			list.put(new OurUnknownAttribute(new AttributeTag(0x0019,0x108a),unknownBytes));
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			AttributeList list = new AttributeList();
			list.read(testFile.getCanonicalPath(),TransferSyntax.ExplicitVRLittleEndian,false/*hasMeta*/,true/*useBufferedStream*/);
			Attribute a = list.get(new AttributeTag(0x0019,0x108a));
			assertTrue("Checking UN was read as SQ",a instanceof SequenceAttribute);

		}
	}
	
	public void TestUnknownSequenceValueRepresentation_FromExplicitVR_WithoutBufferedInput() throws Exception {
		File testFile = File.createTempFile("TestUnknownSequenceValueRepresentation_FromExplicitVR_WithoutBufferedInput",".dcm");
		testFile.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new LongStringAttribute(new AttributeTag(0x0019,0x0010)); a.addValue("HOLOGIC, Inc."); list.put(a); }
			list.put(new OurUnknownAttribute(new AttributeTag(0x0019,0x108a),unknownBytes));
			list.write(testFile,TransferSyntax.ExplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			AttributeList list = new AttributeList();
			list.read(testFile.getCanonicalPath(),TransferSyntax.ExplicitVRLittleEndian,false/*hasMeta*/,false/*useBufferedStream*/);
			Attribute a = list.get(new AttributeTag(0x0019,0x108a));
			assertTrue("Checking UN was read as SQ",a instanceof SequenceAttribute);			// do not need to depend on rewind to mark any more: (000845) fix does better than earlier fix for (000819)

		}
	}
	
	public void TestUnknownSequenceValueRepresentation_FromImplicitVR() throws Exception {
		File testFile = File.createTempFile("TestUnknownSequenceValueRepresentation_FromImplicitVR",".dcm");
		testFile.deleteOnExit();
		{
			AttributeList list = new AttributeList();
			{ Attribute a = new LongStringAttribute(new AttributeTag(0x0019,0x0010)); a.addValue("HOLOGIC, Inc."); list.put(a); }
			list.put(new OurUnknownAttribute(new AttributeTag(0x0019,0x108a),unknownBytes));
			list.write(testFile,TransferSyntax.ImplicitVRLittleEndian,false/*no meta header*/,true/*buffered*/);
		}
		{
			AttributeList list = new AttributeList();
			list.read(testFile);
			Attribute a = list.get(new AttributeTag(0x0019,0x108a));
			assertTrue("Checking UN was read as SQ",a instanceof SequenceAttribute);

		}
	}

}
