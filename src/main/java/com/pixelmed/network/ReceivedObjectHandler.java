/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import com.pixelmed.dicom.DicomException;

import java.io.IOException;

/**
 * <p>This abstract class provides a mechanism for performing processing on
 * a DICOM data set that has been completely received and stored in a file.</p>
 *
 * <p>Typically a private sub-class would be declared and instantiated
 * in an implementation using {@link StorageSOPClassSCPDispatcher StorageSOPClassSCPDispatcher}.</p>
 *
 * <p>For example:</p>
 * <pre>
private class OurReceivedObjectHandler extends ReceivedObjectHandler {
    public void sendReceivedObjectIndication(String dicomFileName,String transferSyntax,String callingAETitle) throws DicomNetworkException, DicomException, IOException {
        if (dicomFileName != null) {
            System.err.println("Received: "+dicomFileName+" from "+callingAETitle+" in "+transferSyntax);
            try {
                DicomInputStream i = new DicomInputStream(new BufferedInputStream(new FileInputStream(dicomFileName)));
                AttributeList list = new AttributeList();
                list.read(i,TagFromName.PixelData);		// no need to read pixel data (much faster if one does not)
				i.close();
                databaseInformationModel.insertObject(list,dicomFileName);
            } catch (Exception e) {
                slf4jlogger.error("", e);;
            }
        }
    }
}
 * </pre>
 *
 * @see StorageSOPClassSCP
 * @see StorageSOPClassSCPDispatcher
 *
 * @author	dclunie
 */
abstract public class ReceivedObjectHandler {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/ReceivedObjectHandler.java,v 1.17 2022/01/21 19:51:25 dclunie Exp $";

	/**
	 * <p>Do something with the received data set stored in the specified file name.</p>
	 *
	 * @param	fileName		where the received data set has been stored
	 * @param	transferSyntax		the transfer syntax in which the data set was received and is stored
	 * @param	callingAETitle		the AE title of the caller who sent the data set
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	abstract public void sendReceivedObjectIndication(String fileName,String transferSyntax,String callingAETitle) throws DicomNetworkException, DicomException, IOException;
}

