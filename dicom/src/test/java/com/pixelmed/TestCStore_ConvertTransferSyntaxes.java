/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed;

import com.pixelmed.dicom.AgeStringAttribute;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.CodeStringAttribute;
import com.pixelmed.dicom.DateAttribute;
import com.pixelmed.dicom.DecimalStringAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.IntegerStringAttribute;
import com.pixelmed.dicom.LongStringAttribute;
import com.pixelmed.dicom.PersonNameAttribute;
import com.pixelmed.dicom.ShortStringAttribute;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TimeAttribute;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.dicom.UIDGenerator;
import com.pixelmed.dicom.UniqueIdentifierAttribute;

import com.pixelmed.network.Association;
import com.pixelmed.network.AssociationStatusHandler;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.network.NetworkUtilities;
//import com.pixelmed.network.PresentationContext;
import com.pixelmed.network.ReceivedObjectHandler;
import com.pixelmed.network.StorageSOPClassSCPDispatcher;
import com.pixelmed.network.StorageSOPClassSCU;
//import com.pixelmed.network.TransferSyntaxSelectionPolicy;
//import com.pixelmed.network.UnencapsulatedExplicitStoreFindMoveGetPresentationContextSelectionPolicy;

import java.io.File;
import java.io.IOException;

import junit.framework.*;

public class TestCStore_ConvertTransferSyntaxes extends TestCase {

	protected static final int    waitIntervalWhenSleeping = 10;	// in ms
	protected static final String scpAET = "TESTSTORESCP";
	protected static final String scuAET = "TESTSTORESCU";

	// constructor to support adding tests to suite ...
	
	public TestCStore_ConvertTransferSyntaxes(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCStore_ConvertTransferSyntaxes.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCStore_ConvertTransferSyntaxes");
		
		suite.addTest(new TestCStore_ConvertTransferSyntaxes("TestCStore_ConvertTransferSyntaxes_SendImplicitVROriginalToDefaultSCPPresentationContextSelectionPolicySuccessfully"));
		
		return suite;
	}
	
	protected void setUp() {
	}
	
	protected void tearDown() {
	}

	private String patientName = "Smith^Mary";
	private String patientID = "3764913624";
	private String patientBirthDate = "19600101";
	private String patientAge = "041Y";
	private String patientWeight = "68";
	private String patientSize = "1.55";
	private String patientSex = "F";
	private String studyID = "612386812";
	private String seriesNumber = "12";
	private String instanceNumber = "38";
	private String referringPhysicianName = "Jones^Harriet";
	private String studyDate = "20010203";
	private String studyTime = "043000";

	private AttributeList makeAttributeList() {
		AttributeList list = new AttributeList();
		try {
			UIDGenerator u = new UIDGenerator("9999");
			String sopInstanceUID = u.getNewSOPInstanceUID(studyID,seriesNumber,instanceNumber);
			String seriesInstanceUID = u.getNewSeriesInstanceUID(studyID,seriesNumber);
			String studyInstanceUID = u.getNewStudyInstanceUID(studyID);

			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(SOPClass.CTImageStorage); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(sopInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(seriesInstanceUID); list.put(a); }
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(studyInstanceUID); list.put(a); }
			{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); a.addValue(patientBirthDate); list.put(a); }
			{ Attribute a = new AgeStringAttribute(TagFromName.PatientAge); a.addValue(patientAge); list.put(a); }
			{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); a.addValue(patientSex); list.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PatientWeight); a.addValue(patientWeight); list.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.PatientSize); a.addValue(patientSize); list.put(a); }
			{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
			{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue(referringPhysicianName); list.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
			{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); /*a.addValue(manufacturer);*/ list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.StudyDate); a.addValue(studyDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.StudyTime); a.addValue(studyTime); list.put(a); }

			{
				Attribute a = new DecimalStringAttribute(TagFromName.DVHData);
				list.put(a);
				String dsv = "1.369547";		// 7 characters (will be 8 with backslash delimiter)
				for (int i=0; i<9000; ++i) {	// want to exceed limit of 65534 (largest even length 16 bit VL field) ... 65534/8 = 8191.75
					a.addValue(dsv);
				}
			}
		}
		catch (DicomException e) {
		}
		return list;
	}
	
	protected volatile String lastReceivedDicomFileName;
	
	protected class OurReceivedObjectHandler extends ReceivedObjectHandler {
		public void sendReceivedObjectIndication(String dicomFileName,String transferSyntax,String callingAETitle) throws DicomNetworkException, DicomException, IOException {
//System.err.println("Received: "+dicomFileName+" from "+callingAETitle+" in "+transferSyntax);
			lastReceivedDicomFileName = dicomFileName;
		}
	}

	protected volatile boolean associationReleased;
	
	private class OurAssociationStatusHandler extends AssociationStatusHandler {
		public void sendAssociationReleaseIndication(Association a) throws DicomNetworkException, DicomException, IOException {
			if (a != null) {
//System.err.println("Association "+a.getAssociationNumber()+" from "+a.getCallingAETitle()+" released");
			}
			associationReleased = true;
		}
	}
	
	public void TestCStore_ConvertTransferSyntaxes_SendImplicitVROriginalToDefaultSCPPresentationContextSelectionPolicySuccessfully() throws Exception {
		System.err.println("TestCStore_ConvertTransferSyntaxes_SendImplicitVROriginalToDefaultSCPPresentationContextSelectionPolicySuccessfully():");
		File savedImagesFolder = new File("./receivedfiles");

		int port = NetworkUtilities.getRandomUnusedPortToListenOnLocally();
//System.err.println("SCP will listen on port "+port);
		
		StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(
			port,
			scpAET,
			savedImagesFolder,
			null/*storedFilePathStrategy*/,
			new OurReceivedObjectHandler(),
			new OurAssociationStatusHandler(),
			null/*queryResponseGeneratorFactory*/,
			null/*retrieveResponseGeneratorFactory*/,
			null/*networkApplicationInformation*/,
			null/*presentationContextSelectionPolicy*/,
			false/*secureTransport*/);
			
		Thread storageSOPClassSCPDispatcherThread = new Thread(storageSOPClassSCPDispatcher);
		storageSOPClassSCPDispatcherThread.start();
		while (storageSOPClassSCPDispatcherThread.getState() != Thread.State.RUNNABLE) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		while (!storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}

		File ivrleFile = File.createTempFile("TestCStore_ConvertTransferSyntaxes_SendImplicitVROriginalToDefaultSCPPresentationContextSelectionPolicySuccessfully",".dcm");
		ivrleFile.deleteOnExit();
		AttributeList list = makeAttributeList();
		FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ImplicitVRLittleEndian,"OURAETITLE");
		list.write(ivrleFile,TransferSyntax.ImplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		list.removeMetaInformationHeaderAttributes();	// else later comparison will fail
		
		new StorageSOPClassSCU("localhost",port,scpAET,scuAET,ivrleFile.getCanonicalPath(),null/*affectedSOPClass*/,null/*affectedSOPInstance*/,0/*compressionLevel*/);
		
		while (lastReceivedDicomFileName == null) {
//System.err.println("Waiting for lastReceivedDicomFileName");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for it to arrive (needs to be volatile, since set in different thread)
		}

		while (!associationReleased) {
//System.err.println("Waiting for associationReleased");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for release (needs to be volatile, since set in different thread)
		}

		storageSOPClassSCPDispatcher.shutdown();
		while (storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait until SCP is no longer ready ... otherwise make execute another test and try to bind same port for StorageSOPClassSCPDispatcher
		}
		
		{
			AttributeList receivedList = new AttributeList();
			receivedList.read(lastReceivedDicomFileName);
			assertEquals("Received TransferSyntax is Explicit VR Little Endian",TransferSyntax.ExplicitVRLittleEndian,Attribute.getSingleStringValueOrEmptyString(receivedList,TagFromName.TransferSyntaxUID));
			receivedList.removeMetaInformationHeaderAttributes();
			assertTrue("AttributeLists received OK",list.equals(receivedList));
		}
	}
	
}

