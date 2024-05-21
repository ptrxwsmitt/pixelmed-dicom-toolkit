/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomFileUtilities;
import com.pixelmed.dicom.SetOfDicomFiles;

import com.pixelmed.display.DialogMessageLogger;
import com.pixelmed.display.SafeFileChooser;

import com.pixelmed.network.ApplicationEntity;
import com.pixelmed.network.ApplicationEntityConfigurationDialog;
import com.pixelmed.network.ApplicationEntityMap;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.network.MultipleInstanceTransferStatusHandlerWithFileName;
import com.pixelmed.network.NetworkApplicationInformation;
import com.pixelmed.network.PresentationAddress;
import com.pixelmed.network.StorageSOPClassSCU;

import com.pixelmed.utils.FileUtilities;
import com.pixelmed.utils.MessageLogger;
import com.pixelmed.utils.PrintStreamMessageLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

import javax.swing.JFileChooser;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class for watching a folder and sending any DICOM files that appear to a pre-configured network remote Storage SCP AE.</p>
 *
 * <p>The class has no public methods other than the constructor and a main method that is useful as a utility.
 *
 * <p>For example:</p>
 * <pre>
java -cp ./pixelmed.jar \
	com.pixelmed.apps.WatchFolderAndSend \
	watchthisfolder \
	graytoo 11112 GRAYTOO_DV_11112
 * </pre>
 * <p>or, with a GUI:</p>
 * <pre>
java -cp ./pixelmed.jar \
	com.pixelmed.apps.WatchFolderAndSend
 * </pre>
 *
 * @author	dclunie
 */
public class WatchFolderAndSend {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/WatchFolderAndSend.java,v 1.14 2022/01/21 19:51:12 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(WatchFolderAndSend.class);
	
	protected static int sleepTimeBetweenCheckingForFolderContent = 10000;	// ms
	protected static int intervalAfterLastModificationWithinWhichDoNotSendFileYet = 1000;		// ms
	
	protected static String propertiesFileName = ".com.pixelmed.apps.WatchFolderAndSend.properties";
	protected static String localnameForRemoteAE = "remote";

	protected static String propertiesFilePath = FileUtilities.makePathToFileInUsersHomeDirectory(propertiesFileName);
	
	protected static ApplicationEntity getPropertiesEditInDialogAndSave() {
		ApplicationEntity ae = null;
		Properties properties = new Properties(/*defaultProperties*/);
		try {
			File propertiesFile = new File(propertiesFilePath);
			if (propertiesFile.exists()) {
				FileInputStream in = new FileInputStream(propertiesFile);
				properties.load(in);
				in.close();
				slf4jlogger.debug("getPropertiesEditInDialogAndSave(): got existing properties {}",properties);
				NetworkApplicationInformation nai = new NetworkApplicationInformation(properties);
				String aet = nai.getApplicationEntityTitleFromLocalName(localnameForRemoteAE);
				ApplicationEntityMap aemap = nai.getApplicationEntityMap();
				if (aet != null && aemap != null) {
					ae = new ApplicationEntity(aet);
					PresentationAddress pa = aemap.getPresentationAddress(aet);
					if (pa != null) {
						ae.setPresentationAddress(pa);
					}
					slf4jlogger.debug("getPropertiesEditInDialogAndSave(): extracted AE {}",ae);
				}
			}
		}
		catch (Exception e) {
			slf4jlogger.error("",e);
		}
		ae = ae == null
			? new ApplicationEntityConfigurationDialog(null/*Component parent*/,localnameForRemoteAE)
			: new ApplicationEntityConfigurationDialog(null/*Component parent*/,localnameForRemoteAE,ae);
		try {
			NetworkApplicationInformation nai = new NetworkApplicationInformation();
			nai.add(localnameForRemoteAE,ae);
			properties = nai.getProperties(properties);
			slf4jlogger.debug("getPropertiesEditInDialogAndSave(): saving revised properties {}",properties);
			FileOutputStream out = new FileOutputStream(propertiesFilePath);
			properties.store(out,"Reconfigured from dialog");
			out.close();
		}
		catch (Exception e) {
			slf4jlogger.error("",e);
		}
		return ae;
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
	 * <p>Watch a folder and send any DICOM files that appear to a network remote Storage SCP AE.</p>
	 *
	 * <p>Removes files only after successful send attempt, and leaves them and retries if connection or send fails.</p>
	 *
	 * <p>Tries not to send files that are still being modified.</p>
	 *
	 * <p>Does NOT RECURSE into sub-folders, only processes file in supplied directory itself.</p>
	 *
	 * @param	watchedFolderName
	 * @param	remoteHost
	 * @param	remotePort
	 * @param	remoteAE
	 * @param	localAE
	 * @param	verbosityLevel	only used if a logger is present
	 * @param	logger			should only be used for logging to user interface dialogs, otherwise use SLF4J logging levels
	 */
	public WatchFolderAndSend(String watchedFolderName,
				String remoteHost,int remotePort,String remoteAE,
				String localAE,int verbosityLevel,
				MessageLogger logger)
			throws DicomException, DicomNetworkException, IOException, InterruptedException {
		if (watchedFolderName != null) {
			File watchedFolder = new File(watchedFolderName);
			while (watchedFolder.exists() && watchedFolder.isDirectory()) {
				slf4jlogger.debug("watched folder exists");
				SetOfDicomFiles setOfDicomFiles = new SetOfDicomFiles();
				File[] files = watchedFolder.listFiles();
				if (files != null && files.length > 0) {
					for (int i=0; i<files.length; ++i) {
						File file = files[i];
						if (file.exists() && file.isFile()) {
							String fileName = file.getName();
							long lastModified = file.lastModified();
							long currentTime = System.currentTimeMillis();
							slf4jlogger.debug("lastModified = {}",lastModified);
							slf4jlogger.debug("currentTime  = {}",currentTime);
							if ((currentTime - lastModified) > intervalAfterLastModificationWithinWhichDoNotSendFileYet) {
								if (DicomFileUtilities.isDicomOrAcrNemaFile(file)) {
									if (verbosityLevel > 0 && logger != null) { logger.sendLn("Queueing "+fileName); }
									slf4jlogger.info("Queueing {}",fileName);
									setOfDicomFiles.add(file);
								}
								else {
									if (verbosityLevel > 0 && logger != null) { logger.sendLn("Skipping non-DICOM file "+fileName); }
									slf4jlogger.info("Skipping non-DICOM file {}",fileName);
								}
							}
							else {
								if (verbosityLevel > 0 && logger != null) { logger.sendLn("Skipping file still being modified "+fileName); }
								slf4jlogger.info("Skipping file still being modified {}",fileName);
							}
						}
					}
					if (setOfDicomFiles != null & setOfDicomFiles.size() > 0) {
						if (new StorageSOPClassSCU(remoteHost,remotePort,remoteAE,localAE,setOfDicomFiles,0,
								new OurMultipleInstanceTransferStatusHandlerWithFileName(verbosityLevel,logger),
								null,0).encounteredTrappedExceptions()) {
							if (verbosityLevel > 0 && logger != null) { logger.sendLn("Sending queued files problem - connection or association failure ?"); }
							slf4jlogger.info("Sending queued files problem - connection or association failure ?");
						}
					}
					// removal is done in OurMultipleInstanceTransferStatusHandlerWithFileName()
				}
				slf4jlogger.debug("sleeping for "+sleepTimeBetweenCheckingForFolderContent+" mS");
				Thread.currentThread().sleep(sleepTimeBetweenCheckingForFolderContent);
			}
		}
	}

	/**
	 * <p>Watch a folder and send any DICOM files that appear to a network remote Storage SCP AE.</p>
	 *
	 * <p>The verbosity level controls the detail of the logging to a graphical user interface and has not been replaced with calls to the SLF4J logging framework.
	 * It is ignored if there is no graphical user interface, in which case messages are sent to the the SLF4J logging framework at the INFO logging level.</p>
	 *
	 * @param	arg		none if parameters are to be requested through a graphical interface, otherwise an array of 4 to 6 strings - the fully qualified path of the watched folder,
	 *					the remote hostname, remote port, and remote AE Title and optionally our AE Title and a verbosity level of 0 or 1
	 */
	public static void main(String arg[]) {
		try {
			if (arg.length == 0 || (arg.length >= 4 && arg.length <= 6)) {
				String watchedFolderName = null;
				String remoteHost = null;
				int remotePort = 0;
				String remoteAE = null;
				String localAE = "US";
				int verbosityLevel = 0;
				MessageLogger logger = null;
				if (arg.length == 0) {
					SafeFileChooser.SafeFileChooserThread fileChooserThread = new SafeFileChooser.SafeFileChooserThread(JFileChooser.DIRECTORIES_ONLY,null,"Select Watched Folder ...");
					java.awt.EventQueue.invokeAndWait(fileChooserThread);
					watchedFolderName=fileChooserThread.getSelectedFileName();
					if (watchedFolderName == null) {
						System.exit(0);
					}
					ApplicationEntity ae = getPropertiesEditInDialogAndSave();
					remoteHost = ae.getPresentationAddress().getHostname();
					remotePort = ae.getPresentationAddress().getPort();
					remoteAE   = ae.getDicomAETitle();
					verbosityLevel = 1;
					logger = new DialogMessageLogger("WatchFolderAndSend Log",512,384,true/*exitApplicationOnClose*/,true/*visible*/);
				}
				else {
					watchedFolderName = arg[0];
					remoteHost = arg[1];
					remotePort = Integer.parseInt(arg[2]);
					remoteAE = arg[3];
					if (arg.length > 4) { localAE = arg[4]; }
					if (arg.length > 5) {
						//verbosityLevel = Integer.parseInt(arg[5]);
						slf4jlogger.warn("Verbosity level ignored");
					}
					//logger = new PrintStreamMessageLogger(System.err);
					logger = null;	// i.e., do not send to both SLF4J and System.err
				}
				File watchedFolder = new File(watchedFolderName);
				new WatchFolderAndSend(watchedFolderName,remoteHost,remotePort,remoteAE,localAE,verbosityLevel,logger);
			}
			else {
				System.err.println("Usage: java -cp ./pixelmed.jar com.pixelmed.apps.WatchFolderAndSend [watchedfolder remoteHost remotePort remoteAET [ourAET [verbositylevel]]]");
			}
		}
		catch (Exception e) {
			slf4jlogger.error(",e");	// use SLF4J since make be used as service
			System.exit(0);
		}
	}
}




