/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.network;

import com.pixelmed.dicom.DicomException;

import java.io.IOException;

/**
 * <p>This abstract class provides a mechanism for detecting when an Association closes.</p>
 *
 * <p>Typically a private sub-class would be declared and instantiated
 * in an implementation using {@link StorageSOPClassSCPDispatcher StorageSOPClassSCPDispatcher}.</p>
 *
 * <p>For example:</p>
 * <pre>
private class OurAssociationStatusHandler extends AssociationStatusHandler {
    public void sendAssociationReleaseIndication(Association a) throws DicomNetworkException, DicomException, IOException {
        if (a != null) {
            System.err.println("Association "+a.getAssociationNumber()+" from "+a.getCallingAETitle()+" released");
        }
    }
}
 * </pre>
 *
 * @see Association
 * @see StorageSOPClassSCP
 * @see StorageSOPClassSCPDispatcher
 *
 * @author	dclunie
 */
abstract public class AssociationStatusHandler {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/AssociationStatusHandler.java,v 1.10 2022/01/21 19:51:24 dclunie Exp $";

	/**
	 * <p>Do something when an Association closes.</p>
	 *
	 * @param	a	the Association
	 * @throws	IOException
	 * @throws	DicomException
	 * @throws	DicomNetworkException
	 */
	abstract public void sendAssociationReleaseIndication(Association a) throws DicomNetworkException, DicomException, IOException;
}

