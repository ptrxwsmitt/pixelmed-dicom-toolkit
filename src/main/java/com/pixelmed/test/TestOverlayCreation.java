/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.Overlay;
import com.pixelmed.dicom.SingleOverlay;

import java.awt.Graphics2D;
import java.awt.Shape;

import java.awt.geom.GeneralPath;

//import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Iterator;
import java.util.Vector;

import java.awt.image.BufferedImage;

import junit.framework.*;

public class TestOverlayCreation extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestOverlayCreation(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestOverlay_Creation.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestOverlayCreation");
		
		suite.addTest(new TestOverlayCreation("TestOverlayCreation_Roundtrip"));
		
		return suite;
	}

	Vector<Shape> shapes = new Vector<Shape>();

	protected void setUp() {
			GeneralPath p = new GeneralPath();
			shapes.add(p);
		
			p.moveTo(189.003922, 181.000000);
		
			p.lineTo(186.000000, 181.003922);
			p.lineTo(182.000000, 181.003922);
			p.lineTo(178.003922, 182.000000);
			p.lineTo(176.000000, 183.003922);
			p.lineTo(174.000000, 185.003922);
			p.lineTo(171.000000, 186.003922);
			p.lineTo(169.000000, 188.003922);
			p.lineTo(167.003922, 191.000000);
			p.lineTo(166.000000, 193.003922);
			p.lineTo(164.000000, 195.003922);
			p.lineTo(162.003922, 198.000000);
			p.lineTo(160.003922, 200.000000);
			p.lineTo(159.003922, 203.000000);
			p.lineTo(158.000000, 205.003922);
			p.lineTo(156.003922, 208.000000);
			p.lineTo(155.003922, 211.000000);
			p.lineTo(154.003922, 214.000000);
			p.lineTo(154.003922, 218.000000);
			p.lineTo(154.003922, 222.000000);
			p.lineTo(155.000000, 225.996078);
			p.lineTo(157.000000, 227.996078);
			p.lineTo(159.000000, 229.996078);
			p.lineTo(160.003922, 232.000000);
			p.lineTo(160.003922, 236.000000);
			p.lineTo(159.003922, 239.000000);
			p.lineTo(157.003922, 241.000000);
			p.lineTo(157.000000, 244.003922);
			p.lineTo(156.000000, 247.003922);
			p.lineTo(155.000000, 250.003922);
			p.lineTo(154.000000, 253.003922);
			p.lineTo(153.003922, 257.000000);
			p.lineTo(152.003922, 260.000000);
			p.lineTo(152.003922, 264.000000);
			p.lineTo(152.003922, 268.000000);
			p.lineTo(152.003922, 272.000000);
			p.lineTo(152.003922, 276.000000);
			p.lineTo(153.003922, 279.000000);
			p.lineTo(154.003922, 282.000000);
			p.lineTo(156.003922, 284.000000);
			p.lineTo(158.003922, 286.000000);
			p.lineTo(161.000000, 287.996094);
			p.lineTo(164.000000, 288.996094);
			p.lineTo(167.003922, 289.000000);
			p.lineTo(171.000000, 289.996094);
			p.lineTo(175.000000, 289.996094);
			p.lineTo(178.000000, 288.996094);
			p.lineTo(182.000000, 288.996094);
			p.lineTo(185.000000, 287.996094);
			p.lineTo(187.000000, 285.996094);
			p.lineTo(189.000000, 283.996094);
			p.lineTo(191.000000, 281.996094);
			p.lineTo(193.996078, 280.000000);
			p.lineTo(195.996078, 278.000000);
			p.lineTo(197.996078, 276.000000);
			p.lineTo(198.996078, 273.000000);
			p.lineTo(201.000000, 271.996094);
			p.lineTo(203.996078, 270.000000);
			p.lineTo(205.996078, 268.000000);
			p.lineTo(207.996078, 266.000000);
			p.lineTo(209.996078, 264.000000);
			p.lineTo(211.000000, 261.996094);
			p.lineTo(213.000000, 259.996094);
			p.lineTo(213.996078, 256.000000);
			p.lineTo(213.996078, 252.000000);
			p.lineTo(213.996078, 248.000000);
			p.lineTo(211.996078, 246.000000);
			p.lineTo(209.000000, 244.003922);
			p.lineTo(207.000000, 242.003922);
			p.lineTo(204.000000, 241.003922);
			p.lineTo(200.996078, 241.000000);
			p.lineTo(197.000000, 240.003922);
			p.lineTo(194.996078, 239.000000);
			p.lineTo(194.996078, 235.000000);
			p.lineTo(194.996078, 231.000000);
			p.lineTo(195.000000, 227.996078);
			p.lineTo(196.996078, 225.000000);
			p.lineTo(197.996078, 222.000000);
			p.lineTo(198.996078, 219.000000);
			p.lineTo(200.996078, 217.000000);
			p.lineTo(201.000000, 213.996078);
			p.lineTo(202.000000, 210.996078);
			p.lineTo(203.996078, 208.000000);
			p.lineTo(205.996078, 206.000000);
			p.lineTo(206.996078, 203.000000);
			p.lineTo(206.996078, 199.000000);
			p.lineTo(206.000000, 195.003922);
			p.lineTo(204.000000, 193.003922);
			p.lineTo(202.000000, 191.003922);
			p.lineTo(200.996078, 189.000000);
			p.lineTo(200.996078, 185.000000);
			p.lineTo(199.000000, 182.003922);
			p.lineTo(197.000000, 180.003922);
			p.lineTo(193.000000, 180.003922);
	}
	
	protected void tearDown() {
	}
	
	
	public void TestOverlayCreation_Roundtrip() throws Exception {
		int rows = 300;
		int columns = 300;
		int frames = 1;
		short[] data = new short[(rows*columns*frames+1)/16+1];
	
		SingleOverlay singleOverlay = new SingleOverlay((short)0x6000/*group*/,data,
						 rows,columns,frames,0/*rowOrigin*/,0/*columnOrigin*/,0/*frameOrigin*/,0/*bitPosition*/,
						 ""/*type*/,""/*subtype*/,""/*label*/,""/*description*/,
						 0/*area*/,0d/*mean*/,0d/*standardDeviation*/);
		
		{
			BufferedImage bufferedImage = singleOverlay.getOverlayAsBinaryBufferedImage(0/*frame*/);
			
			Graphics2D g2d=(Graphics2D)(bufferedImage.getGraphics());
			//Iterator<Shape> i = shapes.iterator();
			//while (i.hasNext()) {
			//	g2d.draw(i.next());
			for (Shape shape : shapes) {
				g2d.draw(shape);
			}
			
			singleOverlay.setOverlayFromBinaryBufferedImage(bufferedImage,0/*frame*/);
		}

		//ArrayList listOfSingleOverlays = new ArrayList();
		//listOfSingleOverlays.add(singleOverlay);
		//Overlay overlay = new Overlay(listOfSingleOverlays,1/*numberOfFrames*/);

		SingleOverlay[] singleOverlays = new SingleOverlay[1];
		singleOverlays[0] = singleOverlay;
		
		Overlay overlay = new Overlay(singleOverlays);
		AttributeList list = overlay.getAttributeList();
		
System.err.println(list);

		{
			Overlay rtOverlay = new Overlay(list);
			
			// no need to go as far as implementing Overlay.equals() just to do the test ??
			assertEquals("getNumberOfOverlays",overlay.getNumberOfOverlays(0/*frame*/),rtOverlay.getNumberOfOverlays(0/*frame*/));
			
			{
				BufferedImage bufferedImage = overlay.getOverlayAsBinaryBufferedImage(0/*frame*/,0/*overlay*/);
				BufferedImage rtBufferedImage = rtOverlay.getOverlayAsBinaryBufferedImage(0/*frame*/,0/*overlay*/);
				//assertEquals("getOverlayAsBinaryBufferedImage",bufferedImage,rtBufferedImage);
				
				int pixels[] = null; // to disambiguate SampleModel.getPixels() method signature
				pixels = bufferedImage.getSampleModel().getPixels(0,0,bufferedImage.getWidth(),bufferedImage.getHeight(),pixels,bufferedImage.getRaster().getDataBuffer());
				
				int rtPixels[] = null; // to disambiguate SampleModel.getPixels() method signature
				rtPixels = rtBufferedImage.getSampleModel().getPixels(0,0,rtBufferedImage.getWidth(),rtBufferedImage.getHeight(),rtPixels,rtBufferedImage.getRaster().getDataBuffer());
				
//				assertTrue("getOverlayAsBinaryBufferedImage pixel arrays",Arrays.equals(pixels,rtPixels));
			}
		}
	}
	
}
