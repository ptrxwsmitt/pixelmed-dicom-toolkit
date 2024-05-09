/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.database.DatabaseInformationModel;
import com.pixelmed.database.PatientStudySeriesConcatenationInstanceModel;

import com.pixelmed.dicom.AgeStringAttribute;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.CodeStringAttribute;
import com.pixelmed.dicom.DateAttribute;
import com.pixelmed.dicom.DecimalStringAttribute;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
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
import com.pixelmed.network.MoveSOPClassSCU;
import com.pixelmed.network.NetworkApplicationInformation;
import com.pixelmed.network.NetworkUtilities;
import com.pixelmed.network.ReceivedObjectHandler;
import com.pixelmed.network.ResponseStatus;
import com.pixelmed.network.StorageSOPClassSCPDispatcher;
import com.pixelmed.network.StorageSOPClassSCU;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import junit.framework.*;

public class TestCMove extends TestCase {

	protected static final int    waitIntervalWhenSleeping = 10;	// in ms
	protected static final String scuAET = "TESTSTORESCU";
	protected static final String scpAET = "TESTSTORESCP";
	protected static final String scpAET1 = "TESTSTORESCP1";
	protected static final String scpAET2 = "TESTSTORESCP2";

	// constructor to support adding tests to suite ...
	
	public TestCMove(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestCMove.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestCMove");
		
		suite.addTest(new TestCMove("TestCMove_NoMatch"));
		suite.addTest(new TestCMove("TestCMove_MoveToSelf"));
		suite.addTest(new TestCMove("TestCMove_MoveToAnother"));

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
		}
		catch (DicomException e) {
		}
		return list;
	}
	
	protected volatile String lastReceivedDicomFileName;
	
	protected class OurReceivedObjectHandler extends ReceivedObjectHandler {
	
		protected DatabaseInformationModel databaseInformationModel;
		
		OurReceivedObjectHandler() {
			super();
			databaseInformationModel = null;
		}
		
		OurReceivedObjectHandler(DatabaseInformationModel databaseInformationModel) {
			super();
			this.databaseInformationModel = databaseInformationModel;
		}

		public void sendReceivedObjectIndication(String dicomFileName,String transferSyntax,String callingAETitle) throws DicomNetworkException, DicomException, IOException {
//System.err.println("Received: "+dicomFileName+" from "+callingAETitle+" in "+transferSyntax);
			lastReceivedDicomFileName = dicomFileName;
			
			// now conventional stuff to insert into database for later C-MOVE etc. (copied from DicomAndWebStorageServer)
			
			if (databaseInformationModel != null && dicomFileName != null) {
				try {
					FileInputStream fis = new FileInputStream(dicomFileName);
					DicomInputStream i = new DicomInputStream(new BufferedInputStream(fis));
					AttributeList list = new AttributeList();
					list.read(i,TagFromName.PixelData);
					i.close();
					fis.close();
					databaseInformationModel.insertObject(list,dicomFileName,DatabaseInformationModel.FILE_COPIED);
//System.err.println("Inserted "+dicomFileName+" into database");
				}
				catch (Exception e) {
					System.err.println("Unable to insert "+dicomFileName+" received from "+callingAETitle+" in "+transferSyntax+" into database");
					e.printStackTrace(System.err);
				}
			}

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
	
	public void TestCMove_NoMatch() throws Exception {
		System.err.println("TestCMove_NoMatch():");
		File savedImagesFolder = new File("./receivedfiles");

		int port = NetworkUtilities.getRandomUnusedPortToListenOnLocally();
//System.err.println("SCP will listen on port "+port);

		DatabaseInformationModel databaseInformationModel = new PatientStudySeriesConcatenationInstanceModel("testDatabase","testDatabase");
		
		StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(
			port,
			scpAET,
			savedImagesFolder,
			null/*storedFilePathStrategy*/,
			new OurReceivedObjectHandler(),
			new OurAssociationStatusHandler(),
			databaseInformationModel.getQueryResponseGeneratorFactory(),
			databaseInformationModel.getRetrieveResponseGeneratorFactory(),
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
		
		associationReleased = false;
		
		// Test move of non-existent study
		{
			AttributeList identifier = new AttributeList();
			{ Attribute a = new CodeStringAttribute(TagFromName.QueryRetrieveLevel); a.addValue("STUDY"); identifier.put(a); }
			{ AttributeTag t = TagFromName.StudyInstanceUID; Attribute a = new UniqueIdentifierAttribute(t); a.addValue("1.2.3.4"); identifier.put(t,a); }
//System.err.println("Identifier:\n"+identifier);
			boolean threwException = false;
			try {
				MoveSOPClassSCU moveSOPClassSCU = new MoveSOPClassSCU("localhost",port,scpAET/*calledAETitle*/,scuAET/*callingAETitle*/,scpAET/*moveDestination*/,SOPClass.StudyRootQueryRetrieveInformationModelMove,identifier);
				// should not get here
				assertTrue("Should not return from MoveSOPClassSCU",false);
				int status = moveSOPClassSCU.getStatus();
//System.err.println("final status = 0x"+Integer.toHexString(status));
				assertTrue("Should not return success status",status != 0);
			}
			catch (Exception e) {
				//e.printStackTrace(System.err);
				assertEquals("Move failed","com.pixelmed.network.DicomNetworkException: C-MOVE reports failure status 0xc000",e.toString());
				threwException = true;
			}
			assertTrue("Threw exception rather than returning from MoveSOPClassSCU",threwException);
		}
		
		storageSOPClassSCPDispatcher.shutdown();
		while (storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait until SCP is no longer ready ... otherwise make execute another test and try to bind same port for StorageSOPClassSCPDispatcher
		}

	}
	
	public void TestCMove_MoveToSelf() throws Exception {
		System.err.println("TestCMove_MoveToSelf():");
		File savedImagesFolder = new File("./receivedfiles");

		int port = NetworkUtilities.getRandomUnusedPortToListenOnLocally();
//System.err.println("SCP will listen on port "+port);

		DatabaseInformationModel databaseInformationModel = new PatientStudySeriesConcatenationInstanceModel("testDatabase","testDatabase");
		
		StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(
			port,
			scpAET,
			savedImagesFolder,
			null/*storedFilePathStrategy*/,
			new OurReceivedObjectHandler(databaseInformationModel),
			new OurAssociationStatusHandler(),
			databaseInformationModel.getQueryResponseGeneratorFactory(),
			databaseInformationModel.getRetrieveResponseGeneratorFactory(),
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

		File fileToStore = File.createTempFile("TestCMove_MoveToSelf",".dcm");
		fileToStore.deleteOnExit();
		AttributeList list = makeAttributeList();
		FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,scuAET);
		list.write(fileToStore,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		list.removeMetaInformationHeaderAttributes();	// else later comparison will fail
		
		associationReleased = false;
		
		new StorageSOPClassSCU("localhost",port,scpAET,scuAET,fileToStore.getCanonicalPath(),null/*affectedSOPClass*/,null/*affectedSOPInstance*/,0/*compressionLevel*/);
		
		while (lastReceivedDicomFileName == null) {
//System.err.println("Waiting for lastReceivedDicomFileName");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for it to arrive (needs to be volatile, since set in different thread)
		}

		while (!associationReleased) {
//System.err.println("Waiting for associationReleased");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for release (needs to be volatile, since set in different thread)
		}

		{
			AttributeList receivedList = new AttributeList();
			receivedList.read(lastReceivedDicomFileName);
			receivedList.removeMetaInformationHeaderAttributes();
			assertTrue("AttributeLists received from original C-STORE OK",list.equals(receivedList));
		}

		associationReleased = false;
		
		// Now test move to self
		{
			AttributeList identifier = new AttributeList();
			{ Attribute a = new CodeStringAttribute(TagFromName.QueryRetrieveLevel); a.addValue("STUDY"); identifier.put(a); }
			{ AttributeTag t = TagFromName.StudyInstanceUID; Attribute a = new UniqueIdentifierAttribute(t); a.addValue(Attribute.getSingleStringValueOrEmptyString(list,t)); identifier.put(t,a); }
			//{ AttributeTag t = TagFromName.SeriesInstanceUID; Attribute a = new UniqueIdentifierAttribute(t); a.addValue(Attribute.getSingleStringValueOrEmptyString(list,t)); identifier.put(t,a); }
			//{ AttributeTag t = TagFromName.SOPInstanceUID; Attribute a = new UniqueIdentifierAttribute(t); a.addValue(Attribute.getSingleStringValueOrEmptyString(list,t)); identifier.put(t,a); }
//System.err.println("Identifier:\n"+identifier);
			boolean threwException = false;
			try {
				MoveSOPClassSCU moveSOPClassSCU = new MoveSOPClassSCU("localhost",port,scpAET/*calledAETitle*/,scuAET/*callingAETitle*/,scpAET/*moveDestination*/,SOPClass.StudyRootQueryRetrieveInformationModelMove,identifier);
				// should not get here
				assertTrue("Should not return from MoveSOPClassSCU",false);
				int status = moveSOPClassSCU.getStatus();
//System.err.println("final status = 0x"+Integer.toHexString(status));
				assertTrue("Should not return success status",status != 0);
			}
			catch (Exception e) {
				//e.printStackTrace(System.err);
				assertEquals("Move failed","com.pixelmed.network.DicomNetworkException: C-MOVE reports failure status 0xa801",e.toString());
				threwException = true;
			}
			assertTrue("Threw exception rather than returning from MoveSOPClassSCU",threwException);
		}
		
		while (!associationReleased) {
//System.err.println("Waiting for associationReleased");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for release (needs to be volatile, since set in different thread)
		}

		//databaseInformationModel.close();								// else may throw exception
		//while (!databaseInformationModel.isClosed()) {
		//	Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait until database has closed (finished compacting) else may throw exception
		//}
		storageSOPClassSCPDispatcher.shutdown();
		while (storageSOPClassSCPDispatcher.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait until SCP is no longer ready ... otherwise make execute another test and try to bind same port for StorageSOPClassSCPDispatcher
		}

	}
	
	public void TestCMove_MoveToAnother() throws Exception {
		System.err.println("TestCMove_MoveToAnother():");
		File savedImagesFolder1 = new File("./receivedfiles1");
		File savedImagesFolder2 = new File("./receivedfiles2");

		int port1 = NetworkUtilities.getRandomUnusedPortToListenOnLocally();
//System.err.println("SCP1 will listen on port "+port1);
		int port2 = NetworkUtilities.getRandomUnusedPortToListenOnLocally();
//System.err.println("SCP2 will listen on port "+port2);

		DatabaseInformationModel databaseInformationModel1 = new PatientStudySeriesConcatenationInstanceModel("testDatabase1","testDatabase1");
		DatabaseInformationModel databaseInformationModel2 = new PatientStudySeriesConcatenationInstanceModel("testDatabase2","testDatabase2");
		
		NetworkApplicationInformation networkApplicationInformation1 = new NetworkApplicationInformation();
		networkApplicationInformation1.add(scpAET2,scpAET2,"localhost",port2,""/*queryModel*/,""/*primaryDeviceType*/);

		StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher1 = new StorageSOPClassSCPDispatcher(
			port1,
			scpAET1,
			savedImagesFolder1,
			null/*storedFilePathStrategy*/,
			new OurReceivedObjectHandler(databaseInformationModel1),
			new OurAssociationStatusHandler(),
			databaseInformationModel1.getQueryResponseGeneratorFactory(),
			databaseInformationModel1.getRetrieveResponseGeneratorFactory(),
			networkApplicationInformation1,
			null/*presentationContextSelectionPolicy*/,
			false/*secureTransport*/);
		
		Thread storageSOPClassSCPDispatcherThread1 = new Thread(storageSOPClassSCPDispatcher1);
		storageSOPClassSCPDispatcherThread1.start();
		while (storageSOPClassSCPDispatcherThread1.getState() != Thread.State.RUNNABLE) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		while (!storageSOPClassSCPDispatcher1.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}

		// now start another one ...
		
		StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher2 = new StorageSOPClassSCPDispatcher(
			port2,
			scpAET2,
			savedImagesFolder2,
			null/*storedFilePathStrategy*/,
			new OurReceivedObjectHandler(databaseInformationModel2),
			new OurAssociationStatusHandler(),
			databaseInformationModel2.getQueryResponseGeneratorFactory(),
			databaseInformationModel2.getRetrieveResponseGeneratorFactory(),
			null/*networkApplicationInformation*/,
			null/*presentationContextSelectionPolicy*/,
			false/*secureTransport*/);
		
		Thread storageSOPClassSCPDispatcherThread2 = new Thread(storageSOPClassSCPDispatcher2);
		storageSOPClassSCPDispatcherThread2.start();
		while (storageSOPClassSCPDispatcherThread2.getState() != Thread.State.RUNNABLE) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		while (!storageSOPClassSCPDispatcher2.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);	// wait until SCP is ready, else later send may fail
		}
		
		// now send file to scpAET1

		File fileToStore = File.createTempFile("TestCMove_MoveToAnother",".dcm");
		fileToStore.deleteOnExit();
		AttributeList list = makeAttributeList();
		FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,scuAET);
		list.write(fileToStore,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
		list.removeMetaInformationHeaderAttributes();	// else later comparison will fail
		
		associationReleased = false;
		
		new StorageSOPClassSCU("localhost",port1,scpAET1,scuAET,fileToStore.getCanonicalPath(),null/*affectedSOPClass*/,null/*affectedSOPInstance*/,0/*compressionLevel*/);
		
		while (lastReceivedDicomFileName == null) {
//System.err.println("Waiting for lastReceivedDicomFileName");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for it to arrive (needs to be volatile, since set in different thread)
		}

		while (!associationReleased) {
//System.err.println("Waiting for associationReleased");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for release (needs to be volatile, since set in different thread)
		}

		{
			AttributeList receivedList = new AttributeList();
			receivedList.read(lastReceivedDicomFileName);
			receivedList.removeMetaInformationHeaderAttributes();
			assertTrue("AttributeLists received from original C-STORE OK",list.equals(receivedList));
		}

		lastReceivedDicomFileName = null;
		
		associationReleased = false;
		
		// Now test move from scpAET1 to scpAET2
		{
			AttributeList identifier = new AttributeList();
			{ Attribute a = new CodeStringAttribute(TagFromName.QueryRetrieveLevel); a.addValue("STUDY"); identifier.put(a); }
			{ AttributeTag t = TagFromName.StudyInstanceUID; Attribute a = new UniqueIdentifierAttribute(t); a.addValue(Attribute.getSingleStringValueOrEmptyString(list,t)); identifier.put(t,a); }
			//{ AttributeTag t = TagFromName.SeriesInstanceUID; Attribute a = new UniqueIdentifierAttribute(t); a.addValue(Attribute.getSingleStringValueOrEmptyString(list,t)); identifier.put(t,a); }
			//{ AttributeTag t = TagFromName.SOPInstanceUID; Attribute a = new UniqueIdentifierAttribute(t); a.addValue(Attribute.getSingleStringValueOrEmptyString(list,t)); identifier.put(t,a); }
//System.err.println("Identifier:\n"+identifier);
			MoveSOPClassSCU moveSOPClassSCU = new MoveSOPClassSCU("localhost",port1,scpAET1/*calledAETitle*/,scuAET/*callingAETitle*/,scpAET2/*moveDestination*/,SOPClass.StudyRootQueryRetrieveInformationModelMove,identifier);
//System.err.println("final status = 0x"+Integer.toHexString(moveSOPClassSCU.getStatus()));
		}
		
		while (lastReceivedDicomFileName == null) {
//System.err.println("Waiting for lastReceivedDicomFileName");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for it to arrive (needs to be volatile, since set in different thread)
		}

		while (!associationReleased) {
//System.err.println("Waiting for associationReleased");
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait for release (needs to be volatile, since set in different thread)
		}

		{
			AttributeList receivedList = new AttributeList();
			receivedList.read(lastReceivedDicomFileName);
			receivedList.removeMetaInformationHeaderAttributes();
			assertTrue("AttributeLists received from move OK",list.equals(receivedList));
		}

		//databaseInformationModel1.close();
		//while (!databaseInformationModel1.isClosed()) {
		//	Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait until database has closed (finished compacting) else may throw exception
		//}
		storageSOPClassSCPDispatcher1.shutdown();
		while (storageSOPClassSCPDispatcher1.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait until SCP is no longer ready ... otherwise make execute another test and try to bind same port for StorageSOPClassSCPDispatcher
		}

		//databaseInformationModel2.close();
		//while (!databaseInformationModel2.isClosed()) {
		//	Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait until database has closed (finished compacting) else may throw exception (001128)
		//}
		storageSOPClassSCPDispatcher2.shutdown();
		while (storageSOPClassSCPDispatcher2.isReady()) {
			Thread.currentThread().sleep(waitIntervalWhenSleeping);		// wait until SCP is no longer ready ... otherwise make execute another test and try to bind same port for StorageSOPClassSCPDispatcher
		}

	}
	
}

