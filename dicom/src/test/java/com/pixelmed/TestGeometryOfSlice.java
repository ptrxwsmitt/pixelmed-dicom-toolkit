/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed;

import com.pixelmed.geometry.*;

import junit.framework.*;

public class TestGeometryOfSlice extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestGeometryOfSlice(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestGeometryOfSlice.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestGeometryOfSlice");
		
		suite.addTest(new TestGeometryOfSlice("TestGeometryOfSlice_3D2DConversions1"));
		suite.addTest(new TestGeometryOfSlice("TestGeometryOfSlice_3D2DConversions2"));
		suite.addTest(new TestGeometryOfSlice("TestGeometryOfSlice_3D2DConversions3"));

		suite.addTest(new TestGeometryOfSlice("TestGeometryOfSlice_NormalDirection_Transverse_FromBelow_RowTowardsLeft_ColTowardsBack"));
		suite.addTest(new TestGeometryOfSlice("TestGeometryOfSlice_NormalDirection_Transverse_FromAbove_RowTowardsRight_ColTowardsBack_OriginalEMIScanner"));
		suite.addTest(new TestGeometryOfSlice("TestGeometryOfSlice_NormalDirection_Transverse_FromAbove_RowTowardsLeft_ColTowardsFront_Neurosurgery"));

		suite.addTest(new TestGeometryOfSlice("TestGeometryOfSlice_NormalDirection_Coronal_FromFront_RowTowardsLeft_ColTowardsFeet"));
		
		suite.addTest(new TestGeometryOfSlice("TestGeometryOfSlice_NormalDirection_Sagittal_FromLeftSide_RowTowardsBack_ColTowardsFeet"));
		suite.addTest(new TestGeometryOfSlice("TestGeometryOfSlice_NormalDirection_Sagittal_FromRightSide_RowTowardsFront_ColTowardsFeet"));

		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	protected boolean epsilonEquals(double value1,double value2,double epsilon) {
		return Math.abs(value1-value2) < epsilon;
	}

	protected boolean epsilonEquals(double[] value1,double[] value2,double epsilon) {
		boolean areEqual = true;
		if (value1 == null) {
			areEqual = false;
		}
		else if (value2 == null) {
			areEqual = false;
		}
		else if (value1.length != value2.length) {
			areEqual = false;
		}
		else {
			for (int i=0; i<value1.length; ++i) {
				if (!epsilonEquals(value1[i],value2[i],epsilon)) {
					areEqual = false;
					break;
				}
			}
		}
		return areEqual;
	}

	protected void check3D2DConversion(GeometryOfSlice geometry,double column,double row,double expectX,double expectY,double expectZ) {
		double[] location3D = geometry.lookupImageCoordinate(column,row);
		//System.err.println("check3D2DConversion(): lookup 2d ("+column+","+row+") returns 3d ("+location3D[0]+","+location3D[1]+","+location3D[2]+")");
		assertTrue("Lookup 2d("+column+","+row+")",epsilonEquals(expectX,location3D[0],0.0001d));
		assertTrue("Lookup 2d("+column+","+row+")",epsilonEquals(expectY,location3D[1],0.0001d));
		assertTrue("Lookup 2d("+column+","+row+")",epsilonEquals(expectZ,location3D[2],0.0001d));
			
		double[] roundTripLocation2D = geometry.lookupImageCoordinate(location3D);
		//System.err.println("check3D2DConversion(): lookup 2d ("+column+","+row+") round trip returns 2d ("+roundTripLocation2D[0]+","+roundTripLocation2D[1]+")");
		assertTrue("Round trip 2d("+column+","+row+") column",epsilonEquals(column,roundTripLocation2D[0],0.0001d));
		assertTrue("Round trip 2d("+column+","+row+") row",   epsilonEquals(   row,roundTripLocation2D[1],0.0001d));
	}
	
	public void TestGeometryOfSlice_3D2DConversions1() throws Exception {
		double[] rowArray          = { 1, 0, 0 };
		double[] columnArray       = { 0, 1, 0 };
		double[] tlhcArray         = { 1, 1, 1 };	// set the X and Y origins to 1 to match the center of the voxel of TLHC "pixel number" 1
		double[] voxelSpacingArray = { 1, 1, 1 };
		double   sliceThickness    =   1;
		double[] dimensions        = { 512, 512, 1 };
		GeometryOfSlice geometry = new GeometryOfSlice(rowArray,columnArray,tlhcArray,voxelSpacingArray,sliceThickness,dimensions);

		check3D2DConversion(geometry,0d,    0d,      0.5d,  0.5d,1.0d);	// expectZ will always just be the specified Z origin
		check3D2DConversion(geometry,0.5d,  0.5d,    1.0d,  1.0d,1.0d);
		check3D2DConversion(geometry,1d,    1d,      1.5d,  1.5d,1.0d);
		check3D2DConversion(geometry,511d,  511d,  511.5d,511.5d,1.0d);
		check3D2DConversion(geometry,511.5d,511.5d,512.0d,512.0d,1.0d);
		check3D2DConversion(geometry,512d,  512d,  512.5d,512.5d,1.0d);
	}
	
	public void TestGeometryOfSlice_3D2DConversions2() throws Exception {
		double[] rowArray          = { 1, 0, 0 };
		double[] columnArray       = { 0, 1, 0 };
		double[] tlhcArray         = { 0, 0, 0 };
		double[] voxelSpacingArray = { 1, 1, 1 };
		double   sliceThickness    =   1;
		double[] dimensions        = { 512, 512, 1 };
		GeometryOfSlice geometry = new GeometryOfSlice(rowArray,columnArray,tlhcArray,voxelSpacingArray,sliceThickness,dimensions);

		check3D2DConversion(geometry,0d,    0d,     -0.5d, -0.5d,0.0d);	// expectZ will always just be the specified Z origin
		check3D2DConversion(geometry,0.5d,  0.5d,    0.0d,  0.0d,0.0d);
		check3D2DConversion(geometry,1d,    1d,      0.5d,  0.5d,0.0d);
		check3D2DConversion(geometry,511d,  511d,  510.5d,510.5d,0.0d);
		check3D2DConversion(geometry,511.5d,511.5d,511.0d,511.0d,0.0d);
		check3D2DConversion(geometry,512d,  512d,  511.5d,511.5d,0.0d);
	}
	
	public void TestGeometryOfSlice_3D2DConversions3() throws Exception {
		double[] rowArray          = { 1, 0, 0 };
		double[] columnArray       = { 0, 1, 0 };
		double[] tlhcArray         = { 0, 0, 0 };
		double[] voxelSpacingArray = { 0.4d, 0.4d, 0.4d };
		double   sliceThickness    =   1;
		double[] dimensions        = { 512, 512, 1 };
		GeometryOfSlice geometry = new GeometryOfSlice(rowArray,columnArray,tlhcArray,voxelSpacingArray,sliceThickness,dimensions);

		check3D2DConversion(geometry,0d,    0d,     -0.2d, -0.2d,0.0d);
		check3D2DConversion(geometry,0.5d,  0.5d,    0.0d,  0.0d,0.0d);
		check3D2DConversion(geometry,1d,    1d,      0.2d,  0.2d,0.0d);
		check3D2DConversion(geometry,511d,  511d,  204.2d,204.2d,0.0d);
		check3D2DConversion(geometry,511.5d,511.5d,204.4d,204.4d,0.0d);
		check3D2DConversion(geometry,512d,  512d,  204.6d,204.6d,0.0d);
	}
	
	// DICOM coordinate system is right handed (http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_C.7.6.2.html)
	// Nice visulaization of cross-product and righ-handedness at https://physics.nfshost.com/textbook/01-Vectors/05-Cross.php
	// Normal should increase in the direction away from the viewer
	// (001277)

	public void TestGeometryOfSlice_NormalDirection_Transverse_FromBelow_RowTowardsLeft_ColTowardsBack() throws Exception {
		double[] rowArray          = { 1, 0, 0 };
		double[] columnArray       = { 0, 1, 0 };
		
		double[] tlhcArray         = { 0, 0, 0 };		// don't care
		double[] voxelSpacingArray = { 1, 1, 1 };		// don't care
		double   sliceThickness    =   1;				// don't care
		double[] dimensions        = { 512, 512, 1 };	// don't care
		
		GeometryOfSlice geometry = new GeometryOfSlice(rowArray,columnArray,tlhcArray,voxelSpacingArray,sliceThickness,dimensions);

		double[] expectedNormalArray = { 0, 0, 1 };		// Towards head
		double[] actualNormalArray = geometry.getNormalArray();

		assertTrue("Normal not as expected",epsilonEquals(expectedNormalArray,actualNormalArray,0.0001d));
	}
	
	
	public void TestGeometryOfSlice_NormalDirection_Transverse_FromAbove_RowTowardsRight_ColTowardsBack_OriginalEMIScanner() throws Exception {
		double[] rowArray          = { -1, 0, 0 };
		double[] columnArray       = { 0, 1, 0 };
		
		double[] tlhcArray         = { 0, 0, 0 };		// don't care
		double[] voxelSpacingArray = { 1, 1, 1 };		// don't care
		double   sliceThickness    =   1;				// don't care
		double[] dimensions        = { 512, 512, 1 };	// don't care
		
		GeometryOfSlice geometry = new GeometryOfSlice(rowArray,columnArray,tlhcArray,voxelSpacingArray,sliceThickness,dimensions);

		double[] expectedNormalArray = { 0, 0, -1 };		// Towards feet
		double[] actualNormalArray = geometry.getNormalArray();

		assertTrue("Normal not as expected",epsilonEquals(expectedNormalArray,actualNormalArray,0.0001d));
	}
	

	public void TestGeometryOfSlice_NormalDirection_Transverse_FromAbove_RowTowardsLeft_ColTowardsFront_Neurosurgery() throws Exception {
		double[] rowArray          = { 1, 0, 0 };
		double[] columnArray       = { 0, -1, 0 };
		
		double[] tlhcArray         = { 0, 0, 0 };		// don't care
		double[] voxelSpacingArray = { 1, 1, 1 };		// don't care
		double   sliceThickness    =   1;				// don't care
		double[] dimensions        = { 512, 512, 1 };	// don't care
		
		GeometryOfSlice geometry = new GeometryOfSlice(rowArray,columnArray,tlhcArray,voxelSpacingArray,sliceThickness,dimensions);

		double[] expectedNormalArray = { 0, 0, -1 };		// Towards feet
		double[] actualNormalArray = geometry.getNormalArray();

		assertTrue("Normal not as expected",epsilonEquals(expectedNormalArray,actualNormalArray,0.0001d));
	}

	public void TestGeometryOfSlice_NormalDirection_Coronal_FromFront_RowTowardsLeft_ColTowardsFeet() throws Exception {
		double[] rowArray          = { 1, 0, 0 };
		double[] columnArray       = { 0, 0, -1 };
		
		double[] tlhcArray         = { 0, 0, 0 };		// don't care
		double[] voxelSpacingArray = { 1, 1, 1 };		// don't care
		double   sliceThickness    =   1;				// don't care
		double[] dimensions        = { 512, 512, 1 };	// don't care
		
		GeometryOfSlice geometry = new GeometryOfSlice(rowArray,columnArray,tlhcArray,voxelSpacingArray,sliceThickness,dimensions);

		double[] expectedNormalArray = { 0, 1, 0 };		// Towards back
		double[] actualNormalArray = geometry.getNormalArray();

		assertTrue("Normal not as expected",epsilonEquals(expectedNormalArray,actualNormalArray,0.0001d));
	}

	public void TestGeometryOfSlice_NormalDirection_Sagittal_FromLeftSide_RowTowardsBack_ColTowardsFeet() throws Exception {
		double[] rowArray          = { 0, 1, 0 };
		double[] columnArray       = { 0, 0, -1 };
		
		double[] tlhcArray         = { 0, 0, 0 };		// don't care
		double[] voxelSpacingArray = { 1, 1, 1 };		// don't care
		double   sliceThickness    =   1;				// don't care
		double[] dimensions        = { 512, 512, 1 };	// don't care
		
		GeometryOfSlice geometry = new GeometryOfSlice(rowArray,columnArray,tlhcArray,voxelSpacingArray,sliceThickness,dimensions);

		double[] expectedNormalArray = { -1, 0, 0 };	// Towards right
		double[] actualNormalArray = geometry.getNormalArray();

		assertTrue("Normal not as expected",epsilonEquals(expectedNormalArray,actualNormalArray,0.0001d));
	}

	public void TestGeometryOfSlice_NormalDirection_Sagittal_FromRightSide_RowTowardsFront_ColTowardsFeet() throws Exception {
		double[] rowArray          = { 0, -1, 0 };
		double[] columnArray       = { 0, 0, -1 };
		
		double[] tlhcArray         = { 0, 0, 0 };		// don't care
		double[] voxelSpacingArray = { 1, 1, 1 };		// don't care
		double   sliceThickness    =   1;				// don't care
		double[] dimensions        = { 512, 512, 1 };	// don't care
		
		GeometryOfSlice geometry = new GeometryOfSlice(rowArray,columnArray,tlhcArray,voxelSpacingArray,sliceThickness,dimensions);

		double[] expectedNormalArray = { 1, 0, 0 };		// Towards left
		double[] actualNormalArray = geometry.getNormalArray();

		assertTrue("Normal not as expected",epsilonEquals(expectedNormalArray,actualNormalArray,0.0001d));
	}

}
