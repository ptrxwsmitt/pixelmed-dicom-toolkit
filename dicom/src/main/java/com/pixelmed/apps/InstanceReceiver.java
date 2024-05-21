/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.InformationEntity;
//import com.pixelmed.dicom.MoveDicomFilesIntoHierarchy;
import com.pixelmed.dicom.StoredFilePathStrategy;
import com.pixelmed.dicom.TagFromName;

import com.pixelmed.network.AnyExplicitStorePresentationContextSelectionPolicy;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.network.NetworkApplicationInformation;
import com.pixelmed.network.NetworkApplicationInformationFederated;
import com.pixelmed.network.NetworkApplicationProperties;
import com.pixelmed.network.ReceivedObjectHandler;
import com.pixelmed.network.StorageSOPClassSCPDispatcher;

import com.pixelmed.utils.FileUtilities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

//import java.security.NoSuchAlgorithmException;

//import java.text.SimpleDateFormat;

//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
import java.util.Properties;
//import java.util.Set;
//import java.util.StringTokenizer;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class to wait for incoming composite instance storage operations and process each instance as it is received.</p>
 *
 * <p>The {@link #doSomethingWithReceivedDicomFile(String,String,String,String) doSomethingWithReceivedDicomFile()} method may be implemented in a sub-class to actually do something more useful than just storing the files.
 *
 * <p>It is configured by use of a properties file that resides in the user's
 * home directory in <code>.com.pixelmed.apps.ProcessReceivedWholeSlideImagesMakeTiledPyramidAndSend.properties</code>.
 * The properties allow control over the user interface elements that are displayed
 * and record the settings changed by the user when the application closes.</p>
 *
 * <p>For a description of the network configuration properties, see {@link NetworkApplicationProperties NetworkApplicationProperties}.</p>
 *
 * <p>The properties that are specific to the application, and their default values, are as follows</p>
 *
 * <p><code>Application.SavedImagesFolderName=.com.pixelmed.apps.InstanceReceiver.receivedinstances</code> - where to store DICOM instances received</p>
 *
 * @author	dclunie
 */
public class InstanceReceiver {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/InstanceReceiver.java,v 1.6 2022/01/21 19:51:11 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(InstanceReceiver.class);
	
	protected static String defaultPropertiesFileName = ".com.pixelmed.apps.InstanceReceiver.properties";

	protected static String propertyName_SavedImagesFolderName = "Application.SavedImagesFolderName";

	protected String defaultSavedImagesFolderName = ".com.pixelmed.apps.InstanceReceiver.receivedinstances";
	
	protected Properties properties;
	
	protected NetworkApplicationProperties networkApplicationProperties;
	protected NetworkApplicationInformationFederated networkApplicationInformation;
	
	protected String ourCalledAETitle;
	
	protected File savedImagesFolder;
	protected StoredFilePathStrategy storedFilePathStrategy = StoredFilePathStrategy.BYSOPINSTANCEUIDHASHSUBFOLDERS;
	
	
	/**
	 * <p>Load properties.</p>
	 *
	 * @throws	IOException	thrown if properties file is missing
	 */
	protected void loadProperties(String propertiesFileName) throws IOException {
		properties = new Properties(/*defaultProperties*/);
		FileInputStream in = new FileInputStream(propertiesFileName);
		properties.load(in);
		in.close();
	}
	
	// copied from SynchronizeFromRemoteSCP ... should refactor :(
	protected static class OurReadTerminationStrategy implements AttributeList.ReadTerminationStrategy {
		public boolean terminate(AttributeList attributeList,AttributeTag tag,long byteOffset) {
			return tag.getGroup() > 0x0020;
		}
	}
	
	protected final static AttributeList.ReadTerminationStrategy terminateAfterRelationshipGroup = new OurReadTerminationStrategy();

	
	protected class ReceivedFileProcessor implements Runnable {
		String receivedFileName;
		AttributeList list;
		
		ReceivedFileProcessor(String receivedFileName) {
			this.receivedFileName = receivedFileName;
		}
		
		public void run() {
			try {
				slf4jlogger.trace("ReceivedFileProcessor.run(): receivedFileName = {}",receivedFileName);
				FileInputStream fis = new FileInputStream(receivedFileName);
				DicomInputStream i = new DicomInputStream(new BufferedInputStream(fis));
				AttributeList list = new AttributeList();
				list.read(i,terminateAfterRelationshipGroup);
				i.close();
				fis.close();

				{
					String sourceApplicationEntityTitle = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SourceApplicationEntityTitle);
					String            transferSyntaxUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.TransferSyntaxUID);
					String                  sopClassUID = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.MediaStorageSOPClassUID);

					doSomethingWithReceivedDicomFile(receivedFileName,sourceApplicationEntityTitle,transferSyntaxUID,sopClassUID); // call this here rather than in OurReceivedObjectHandler since do not want to block/delay StorageSCP thread
				}
				
			}
			catch (Exception e) {
				slf4jlogger.error("",e);
			}
		}
	}
	
	/**
	 * <p>Do something with the received DICOM file.</p>
	 *
	 * <p>This method may be implemented in a sub-class to do something useful even if it is only logging to the user interface.
	 *
	 * <p>The default method does nothing.</p>
	 *
	 * <p>This method is called on the ReceivedFileProcessor thread.</p>
	 *
	 * <p>This method does not define any exceptions and hence must handle any errors locally.</p>
	 *
	 * @param	receivedFileName				the path name to a DICOM file
	 * @param	sourceApplicationEntityTitle	the Application Entity from which the file was received
	 * @param	transferSyntaxUID				the Transfer Syntax of the Data Set in the DICOM file
	 * @param	sopClassUID						the SOP Class of the Data Set in the DICOM file
	 */
	protected void doSomethingWithReceivedDicomFile(String receivedFileName,String sourceApplicationEntityTitle,String transferSyntaxUID,String sopClassUID) {
System.err.println("doSomethingWithReceivedDicomFile(): "+receivedFileName+" received from "+sourceApplicationEntityTitle+" in "+transferSyntaxUID+" is "+sopClassUID);
	}
	
	/**
	 *
	 */
	protected class OurReceivedObjectHandler extends ReceivedObjectHandler {
		/**
		 * @param	dicomFileName
		 * @param	transferSyntax
		 * @param	callingAETitle
		 * @throws	IOException
		 * @throws	DicomException
		 * @throws	DicomNetworkException
		 */
		public void sendReceivedObjectIndication(String dicomFileName,String transferSyntax,String callingAETitle)
				throws DicomNetworkException, DicomException, IOException {
			if (dicomFileName != null) {
				slf4jlogger.debug("Received: {} from {} in {}",dicomFileName,callingAETitle,transferSyntax);
				try {
					new Thread(new ReceivedFileProcessor(dicomFileName)).start();		// on separate thread, else will block and the C-STORE response will be delayed
				} catch (Exception e) {
					slf4jlogger.error("Unable to process {} received from {} in {}",dicomFileName,callingAETitle,transferSyntax,e);
				}
			}

		}
	}
	
	// derived from DatabaseApplicationProperties.getSavedImagesFolderCreatingItIfNecessary()
	/**
	 * <p>Return the folder, creating it if necessary.</p>
	 *
	 * <p>If not an absolute path, will be sought or created relative to the current user's home directory.</p>
	 *
	 * @return	the folder
	 */
	protected static File getFolderNameCreatingItIfNecessary(String folderName) throws IOException {
System.err.println("InstanceReceiver.getFolderNameCreatingItIfNecessary(): requesting folderName = "+folderName);
		File folder = new File(folderName);
		if (folder.isAbsolute()) {
			if (!folder.isDirectory() && !folder.mkdirs()) {
				throw new IOException("Cannot find or create absolute path "+folder);
			}
		}
		else {
			folder = new File(FileUtilities.makePathToFileInUsersHomeDirectory(folderName));
			if (!folder.isDirectory() && !folder.mkdirs()) {
				throw new IOException("Cannot find or create home directory relative path "+folder);
			}
		}
System.err.println("InstanceReceiver.getFolderNameCreatingItIfNecessary(): using folder = "+folder);
		return folder;
	}

	private StorageSOPClassSCPDispatcher storageSOPClassSCPDispatcher;
	
	/**
	 * <p>Start or restart DICOM storage listener.</p>
	 *
	 * <p>Shuts down existing listener, if any, so may be used to restart after configuration change.</p>
	 *
	 * @throws	DicomException
	 */
	public void activateStorageSCP() throws DicomException, IOException {
		shutdownStorageSCP();
		// Start up DICOM association listener in background for receiving images and responding to echoes ...
		if (networkApplicationProperties != null) {
			slf4jlogger.trace("Starting up DICOM association listener ...");
			int port = networkApplicationProperties.getListeningPort();
			storageSOPClassSCPDispatcher = new StorageSOPClassSCPDispatcher(port,ourCalledAETitle,
				networkApplicationProperties.getAcceptorMaximumLengthReceived(),networkApplicationProperties.getAcceptorSocketReceiveBufferSize(),networkApplicationProperties.getAcceptorSocketSendBufferSize(),
				savedImagesFolder,storedFilePathStrategy,
				new OurReceivedObjectHandler(),
				null/*AssociationStatusHandler*/,
				null/*queryResponseGeneratorFactory*/,
				null/*retrieveResponseGeneratorFactory*/,
				networkApplicationInformation,
				new AnyExplicitStorePresentationContextSelectionPolicy(),
				false/*secureTransport*/);
			new Thread(storageSOPClassSCPDispatcher).start();
		}
		else {
			throw new DicomException("Network application properties not supplied");
		}
	}
	
	/**
	 * <p>Shutdown DICOM storage listener.</p>
	 */
	public void shutdownStorageSCP()  {
		if (storageSOPClassSCPDispatcher != null) {
			slf4jlogger.trace("Shutdown DICOM association listener ...");
			storageSOPClassSCPDispatcher.shutdown();
			storageSOPClassSCPDispatcher = null;
		}
	}
	
	/**
	 * <p>Wait for incoming composite instance storage operations and process each instance as it is received.</p>
	 *
	 * @param	propertiesFileName
	 */
	public InstanceReceiver(String propertiesFileName) throws DicomException, DicomNetworkException, IOException, InterruptedException {
		loadProperties(propertiesFileName);		// do NOT trap exception; we must have properties

		savedImagesFolder = getFolderNameCreatingItIfNecessary(properties.getProperty(propertyName_SavedImagesFolderName,defaultSavedImagesFolderName));
		
		networkApplicationProperties = new NetworkApplicationProperties(properties,true/*addPublicStorageSCPsIfNoRemoteAEsConfigured*/);
		networkApplicationInformation = new NetworkApplicationInformationFederated();
		networkApplicationInformation.startupAllKnownSourcesAndRegister(networkApplicationProperties);
		ourCalledAETitle = networkApplicationProperties.getCalledAETitle();

		activateStorageSCP();
	}

	/**
	 * <p>Wait for incoming composite instance storage operations and process each instance as it is received.</p>
	 *
	 * @param	arg		none
	 */
	public static void main(String arg[]) {
		try {
			String propertiesFileName = arg.length > 0 ? arg[0] : FileUtilities.makePathToFileInUsersHomeDirectory(defaultPropertiesFileName);
			new InstanceReceiver(propertiesFileName);
		}
		catch (Exception e) {
			slf4jlogger.error("",e);	// use SLF4J since may be used as a background service
			System.exit(0);
		}
	}
}

