/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.DicomException;
//import com.pixelmed.dicom.DicomFileUtilities;
import com.pixelmed.dicom.SetOfDicomFiles;

//import com.pixelmed.display.DialogMessageLogger;
//import com.pixelmed.display.SafeFileChooser;

import com.pixelmed.network.ApplicationEntity;
//import com.pixelmed.network.ApplicationEntityConfigurationDialog;
import com.pixelmed.network.ApplicationEntityMap;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.network.MultipleInstanceTransferStatusHandlerWithFileName;
import com.pixelmed.network.NetworkApplicationInformation;
import com.pixelmed.network.PresentationAddress;
import com.pixelmed.network.StorageSOPClassSCU;

import com.pixelmed.utils.FileUtilities;
import com.pixelmed.utils.MessageLogger;
//import com.pixelmed.utils.PrintStreamMessageLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

//import javax.swing.JFileChooser;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class to wait for incoming composite instance storage operations 
 * and process any single high resolution tiled whole slide image as it is received to produce a multi-resolution pyramid set of images
 * and send the original and the pyramid set of images to a pre-configured network remote Storage SCP AE.</p>
 *
 * <p>It is configured by use of a properties file that resides in the user's
 * home directory in <code>.com.pixelmed.apps.ProcessReceivedWholeSlideImagesMakeTiledPyramidAndSend.properties</code>.
 * The properties allow control over the user interface elements that are displayed
 * and record the settings changed by the user when the application closes.</p>
 *
 * <p>For a description of the network configuration properties, see {@link com.pixelmed.network.NetworkApplicationProperties NetworkApplicationProperties}.</p>
 *
 * <p>The properties that are specific to the application, and their default values, are as follows</p>
 *
 * <p><code>Application.SavedImagesFolderName=.com.pixelmed.apps.InstanceReceiver.receivedinstances</code> - where to store DICOM instances received</p>
 * <p><code>Application.PyramidImagesFolderName=.com.pixelmed.apps.ProcessReceivedWholeSlideImagesMakeTiledPyramidAndSend.pyramidimages</code> - where to store DICOM pyramid images created</p>
 * <p><code>Dicom.CurrentlySelectedStorageTargetAE=them</code> - the name of the selected remote AE listed amongst the Dicom.RemoteAEs</p>
 *
 * @author	dclunie
 */
public class ProcessReceivedWholeSlideImagesMakeTiledPyramidAndSend extends InstanceReceiver {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/ProcessReceivedWholeSlideImagesMakeTiledPyramidAndSend.java,v 1.6 2022/01/21 19:51:12 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(ProcessReceivedWholeSlideImagesMakeTiledPyramidAndSend.class);

	protected static String propertyName_PyramidImagesFolderName = "Application.PyramidImagesFolderName";
	protected static String propertyName_DicomCurrentlySelectedStorageTargetAE = "Dicom.CurrentlySelectedStorageTargetAE";

	protected String defaultPyramidImagesFolderName = ".com.pixelmed.apps.ProcessReceivedWholeSlideImagesMakeTiledPyramidAndSend.pyramidimages";
	protected String defaultDicomCurrentlySelectedStorageTargetAE = "them";
	
	protected File pyramidImagesFolder;
	
	protected String ourCallingAETitle;
	protected String remoteAETitle;
	protected String remoteHost;
	protected int remotePort;

	/**
	 * <p>Process any single high resolution tiled whole slide image received to produce a multi-resolution pyramid set of images.</p>
	 *
	 * @param	receivedFileName				the path name to a DICOM file
	 * @param	sourceApplicationEntityTitle	the Application Entity from which the file was received
	 * @param	transferSyntaxUID				the Transfer Syntax of the Data Set in the DICOM file
	 * @param	sopClassUID						the SOP Class of the Data Set in the DICOM file
	 */
	protected void doSomethingWithReceivedDicomFile(String receivedFileName,String sourceApplicationEntityTitle,String transferSyntaxUID,String sopClassUID) {
System.err.println("doSomethingWithReceivedDicomFile(): "+receivedFileName+" received from "+sourceApplicationEntityTitle+" in "+transferSyntaxUID+" is "+sopClassUID);
		boolean ok = true;
		try {
			new TiledPyramid(receivedFileName,pyramidImagesFolder.getCanonicalPath());
		}
		catch (Exception e) {
			slf4jlogger.error("Failed to build tiled pyramid",e);
			ok = false;
		}
		
		if (ok) {
			SetOfDicomFiles setOfDicomFiles = new SetOfDicomFiles();
			try {
				for (File file: pyramidImagesFolder.listFiles()) {
					if (slf4jlogger.isInfoEnabled()) slf4jlogger.info("Queueing {}",file.getCanonicalPath());
					setOfDicomFiles.add(file);
				}
				// send original file last
				slf4jlogger.info("Queueing {}",receivedFileName);
				setOfDicomFiles.add(receivedFileName);
			}
			catch (Exception e) {
				slf4jlogger.error("Failed to make SetOfDicomFiles",e);
			}

			// OK to do this synchronously, since we are on a separate thread already and this will not block receiving

			if (setOfDicomFiles != null & setOfDicomFiles.size() > 0) {
				{
					StorageSOPClassSCU storageSOPClassSCU = new StorageSOPClassSCU(remoteHost,remotePort,remoteAETitle,ourCallingAETitle,setOfDicomFiles,0,
						new OurMultipleInstanceTransferStatusHandlerWithFileName(0/*verbosityLevel*/,null/*MessageLogger*/),
						null,0);
					if (storageSOPClassSCU.encounteredTrappedExceptions()) {
						slf4jlogger.info("Sending queued files problem - connection or association failure ?");
					}
				}
			}
		}
		// else send nothing, not even the original received
	}

	
	protected class OurMultipleInstanceTransferStatusHandlerWithFileName extends MultipleInstanceTransferStatusHandlerWithFileName {
		int verbosityLevel;
		MessageLogger logger;
		
		OurMultipleInstanceTransferStatusHandlerWithFileName(int verbosityLevel,MessageLogger logger) {
			this.verbosityLevel = verbosityLevel;
			this.logger = logger;
		}
		
		public  void updateStatus(int nRemaining,int nCompleted,int nFailed,int nWarning,String sopInstanceUID,String pathName,boolean success) {
			File file = new File(pathName);
			String fileName = file.getName();
			if (verbosityLevel > 0 && logger != null) { logger.sendLn("Send of "+fileName+" "+(success ? "succeeded" : "failed")); }
			slf4jlogger.info("Send of {} {}",fileName,(success ? "succeeded" : "failed"));
			if (success) {
				if (file.exists() && file.isFile()) {
					slf4jlogger.debug("removing {}",fileName);
					if (file.delete()) {
						if (verbosityLevel > 0 && logger != null) { logger.sendLn("Removed "+fileName); }
						slf4jlogger.info("Removed {}",fileName);
					}
					else {
						if (verbosityLevel > 0 && logger != null) { logger.sendLn("Failed to remove "+fileName); }
						slf4jlogger.info("Failed to remove {}",fileName);
					}
				}
			}
			else {
				if (verbosityLevel > 0 && logger != null) { logger.sendLn("Leaving "+fileName); }
				slf4jlogger.info("Leaving {}",fileName);
			}
		}
	}

	/**
	 * <p>Wait for incoming composite instance storage operations
	 * and process any single high resolution tiled whole slide image as it is received to produce a multi-resolution pyramid set of images
	 * and send the original and the pyramid set of images to a pre-configured network remote Storage SCP AE.</p>
	 *
	 * @param	propertiesFileName
	 */
	public ProcessReceivedWholeSlideImagesMakeTiledPyramidAndSend(String propertiesFileName) throws DicomException, DicomNetworkException, IOException, InterruptedException {
		super(propertiesFileName);	// parent class loads properties and activates StorageSCP, from which all the work is initiated
		pyramidImagesFolder =  getFolderNameCreatingItIfNecessary(properties.getProperty(propertyName_PyramidImagesFolderName,defaultPyramidImagesFolderName));
		{
			String remoteAE = properties.getProperty(propertyName_DicomCurrentlySelectedStorageTargetAE);
			ourCallingAETitle = networkApplicationProperties.getCallingAETitle();
			remoteAETitle = networkApplicationInformation.getApplicationEntityTitleFromLocalName(remoteAE);
			PresentationAddress presentationAddress = networkApplicationInformation.getApplicationEntityMap().getPresentationAddress(remoteAETitle);
			remoteHost = presentationAddress.getHostname();
			remotePort = presentationAddress.getPort();
		}
	}

	/**
	 * <p>Wait for incoming composite instance storage operations
	 * and process any single high resolution tiled whole slide image as it is received to produce a multi-resolution pyramid set of images
	 * and send the original and the pyramid set of images to a pre-configured network remote Storage SCP AE.</p>
	 *
	 * @param	arg		none
	 */
	public static void main(String arg[]) {
		try {
			String propertiesFileName = arg.length > 0 ? arg[0] : FileUtilities.makePathToFileInUsersHomeDirectory(defaultPropertiesFileName);
			new ProcessReceivedWholeSlideImagesMakeTiledPyramidAndSend(propertiesFileName);
		}
		catch (Exception e) {
			slf4jlogger.error("",e);	// use SLF4J since may be used as a background service
			System.exit(0);
		}
	}
}




