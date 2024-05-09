/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.TagFromName;

import junit.framework.*;

public class TestDecompressedImagePixelModuleAndLossyImageCompressionHistory extends TestCase {

	// constructor to support adding tests to suite ...
	
	public TestDecompressedImagePixelModuleAndLossyImageCompressionHistory(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCStore.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCStore");
		
		suite.addTest(new TestDecompressedImagePixelModuleAndLossyImageCompressionHistory("TestDecompressedImagePixelModuleAndLossyImageCompressionHistory_ReadJPEGBaseline_Decompressing"));
		suite.addTest(new TestDecompressedImagePixelModuleAndLossyImageCompressionHistory("TestDecompressedImagePixelModuleAndLossyImageCompressionHistory_ReadJPEGBaseline_NotDecompressing"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private static final String filenameJPEGBaseline = "testcstorefile_jpegbaseline.dcm";
	private static final long compressionRatioJPEGBaseline = 13;	// 12.956

	public void TestDecompressedImagePixelModuleAndLossyImageCompressionHistory_ReadJPEGBaseline_Decompressing() throws Exception {
		System.err.println("TestDecompressedImagePixelModuleAndLossyImageCompressionHistory_ReadJPEGBaseline_Decompressing():");
		AttributeList list = new AttributeList();
		list.setDecompressPixelData(true);
		list.read(filenameJPEGBaseline);

		assertEquals("Before PhotometricInterpretation","YBR_FULL_422",Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PhotometricInterpretation));
		
		list.remove(TagFromName.LossyImageCompression);
		list.remove(TagFromName.LossyImageCompressionMethod);
		list.remove(TagFromName.LossyImageCompressionRatio);
		list.remove(TagFromName.PlanarConfiguration);

		list.correctDecompressedImagePixelModule();
		list.insertLossyImageCompressionHistoryIfDecompressed();

		assertEquals("After PhotometricInterpretation","RGB",Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PhotometricInterpretation));
		assertEquals("After PlanarConfiguration",0,Attribute.getSingleIntegerValueOrDefault(list,TagFromName.PlanarConfiguration,-1));
		assertEquals("After LossyImageCompression","01",Attribute.getSingleStringValueOrEmptyString(list,TagFromName.LossyImageCompression));
		assertEquals("After LossyImageCompressionMethod","ISO_10918_1",Attribute.getSingleStringValueOrEmptyString(list,TagFromName.LossyImageCompressionMethod));
		assertTrue("After LossyImageCompressionRatio",Math.round(Double.parseDouble(Attribute.getSingleStringValueOrEmptyString(list,TagFromName.LossyImageCompressionRatio))) == compressionRatioJPEGBaseline);
		
	}

	public void TestDecompressedImagePixelModuleAndLossyImageCompressionHistory_ReadJPEGBaseline_NotDecompressing() throws Exception {
		System.err.println("TestDecompressedImagePixelModuleAndLossyImageCompressionHistory_ReadJPEGBaseline_Decompressing():");
		AttributeList list = new AttributeList();
		list.setDecompressPixelData(false);
		list.read(filenameJPEGBaseline);

		assertEquals("Before PhotometricInterpretation","YBR_FULL_422",Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PhotometricInterpretation));
		
		list.remove(TagFromName.LossyImageCompression);
		list.remove(TagFromName.LossyImageCompressionMethod);
		list.remove(TagFromName.LossyImageCompressionRatio);
		list.remove(TagFromName.PlanarConfiguration);

		list.correctDecompressedImagePixelModule(true/*deferredDecompression*/);
		list.insertLossyImageCompressionHistoryIfDecompressed(true/*deferredDecompression*/);

		assertEquals("After PhotometricInterpretation","RGB",Attribute.getSingleStringValueOrEmptyString(list,TagFromName.PhotometricInterpretation));
		assertEquals("After PlanarConfiguration",0,Attribute.getSingleIntegerValueOrDefault(list,TagFromName.PlanarConfiguration,-1));
		assertEquals("After LossyImageCompression","01",Attribute.getSingleStringValueOrEmptyString(list,TagFromName.LossyImageCompression));
		assertEquals("After LossyImageCompressionMethod","ISO_10918_1",Attribute.getSingleStringValueOrEmptyString(list,TagFromName.LossyImageCompressionMethod));
		assertTrue("After LossyImageCompressionRatio",Math.round(Double.parseDouble(Attribute.getSingleStringValueOrEmptyString(list,TagFromName.LossyImageCompressionRatio))) == compressionRatioJPEGBaseline);
		
	}
}
