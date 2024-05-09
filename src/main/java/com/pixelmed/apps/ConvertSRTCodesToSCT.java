/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.CodingSchemeIdentification;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.HierarchicalSOPInstanceReference;
import com.pixelmed.dicom.MediaImporter;
import com.pixelmed.dicom.MoveDicomFilesIntoHierarchy;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.ShortStringAttribute;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;


import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class to convert all SRT codes to SCT codes.</p>
 *
 * @author	dclunie
 */
public class ConvertSRTCodesToSCT {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/ConvertSRTCodesToSCT.java,v 1.4 2022/01/21 19:51:11 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(ConvertSRTCodesToSCT.class);
	
	protected String ourAETitle = "OURAETITLE";
	
	protected String xmlSRTSCTMappingResourceName = "/com/pixelmed/validate/srtsctmapping.xml";
	protected String xmlRetiredSRTNewSCTMappingResourceName = "/com/pixelmed/validate/retiredsrtnewsctmapping.xml";

	private Document readXMLSRTSCTMapping(String mappingResourceName) throws IOException, ParserConfigurationException, SAXException {
		InputStream i = ConvertSRTCodesToSCT.class.getResourceAsStream(mappingResourceName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(i);
	}
	
	Map<String,String> sctCodeBySRTCode = new HashMap<String,String>();
	
	private void loadMapping(String mappingResourceName) {
		try {
			Document xmlSRTSCTMappingDocument = readXMLSRTSCTMapping(mappingResourceName);

			Node mappingTableElement = xmlSRTSCTMappingDocument.getDocumentElement();
			if (!mappingTableElement.getNodeName().equals("definesrttosctmappingtable")) {
				throw new DicomException("Missing definesrttosctmappingtable root element in "+xmlSRTSCTMappingResourceName);
			}
			
			Node mappingElement = mappingTableElement.getFirstChild();
			while (mappingElement != null) {
				if (mappingElement.getNodeName().equals("mapping")) {
					NamedNodeMap mappingElementAttributes = mappingElement.getAttributes();
					String sct = mappingElementAttributes.getNamedItem("sct").getNodeValue();	// will fail with exception if not found but can trust input to be well formed :(
					String srt = mappingElementAttributes.getNamedItem("srt").getNodeValue();
					sctCodeBySRTCode.put(srt,sct);
				}
				mappingElement = mappingElement.getNextSibling();
			}
		}
		catch (Exception e) {
			slf4jlogger.error("Unable to construct SRT to SCT mapping from XML ",e);
		}

	}
	
	/**
	 * <p>Convert SRT codes to SCT codes (per CP 1850) in this ItemList.</p>
	 *
	 * <p>Does not recurse into SequenceAttributes.</p>
	 *
	 * @param	list
	 */
	public void convertSRTToSCTInThisItem(AttributeList list) {
		String csd = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.CodingSchemeDesignator);
		if (csd.length() > 0) {
			if (csd.equals("SRT") || csd.equals("SNM") || csd.equals("SNM3") || csd.equals("99SDM")) {
				String srt = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.CodeValue);
				if (srt.length() > 0) {
					String sct = sctCodeBySRTCode.get(srt);
					if (sct != null && sct.length() > 0) {
						slf4jlogger.info("Mapped SRT: {} to SCT: ",srt,sct);
						try {
							{ Attribute a = new ShortStringAttribute(TagFromName.CodeValue); a.addValue(sct); list.put(a); }
							{ Attribute a = new ShortStringAttribute(TagFromName.CodingSchemeDesignator); a.addValue("SCT"); list.put(a); }
						}
						catch (DicomException e) {
							slf4jlogger.error("Unable to replace CodeValue or CodingSchemeDesignator",e);
						}
					}
					else {
						slf4jlogger.error("Could not map SRT: {} to SCT",srt);
					}
				}
			}
		}
	}

	/**
	 * <p>Convert SRT codes to SCT codes (per CP 1850) in the this and its children.</p>
	 *
	 * <p>Recurses into SequenceAttributes.</p>
	 *
	 * @param	list
	 */
	public void findCodeSequenceItemsAndConvertSRTCodesToSCT(AttributeList list) {
		{
			Iterator<Attribute> it = list.values().iterator();
			while (it.hasNext()) {
				Attribute a = it.next();
				if (a != null) {
					if (a instanceof SequenceAttribute) {
						Iterator<SequenceItem> is = ((SequenceAttribute)a).iterator();
						while (is.hasNext()) {
							SequenceItem item = is.next();
							if (item != null) {
								AttributeList itemlist = item.getAttributeList();
								if (itemlist != null) {
									convertSRTToSCTInThisItem(itemlist);
									findCodeSequenceItemsAndConvertSRTCodesToSCT(itemlist);
								}
							}
						}
					}
				}
			}
		}
	}

	protected class OurMediaImporter extends MediaImporter {
	
		String dstFolderName;

		public OurMediaImporter(String dstFolderName) {
			super(null);
			this.dstFolderName = dstFolderName;
		}
	
		protected void doSomethingWithDicomFileOnMedia(String srcFileName) {
//System.err.println("Doing "+srcFileName);
			try {
				DicomInputStream i = new DicomInputStream(new File(srcFileName));
				AttributeList list = new AttributeList();
				list.setDecompressPixelData(false);
				list.read(i);
				i.close();
				
				list.removeGroupLengthAttributes();
				list.remove(TagFromName.DataSetTrailingPadding);

				String transferSyntaxUID = Attribute.getSingleStringValueOrDefault(list,TagFromName.TransferSyntaxUID,TransferSyntax.ExplicitVRLittleEndian);
				list.removeMetaInformationHeaderAttributes();

				findCodeSequenceItemsAndConvertSRTCodesToSCT(list);

				CodingSchemeIdentification.replaceCodingSchemeIdentificationSequenceWithCodingSchemesUsedInAttributeList(list);
				list.insertSuitableSpecificCharacterSetForAllStringValues();
				
				FileMetaInformation.addFileMetaInformation(list,transferSyntaxUID,ourAETitle);
				
				File dstFile = new File(dstFolderName,MoveDicomFilesIntoHierarchy.makeHierarchicalPathFromAttributes(list));
				if (dstFile.exists()) {
					throw new DicomException("\""+srcFileName+"\": new file \""+dstFile+"\" already exists - not overwriting");
				}
				else {
					File dstParentDirectory = dstFile.getParentFile();
					if (!dstParentDirectory.exists()) {
						if (!dstParentDirectory.mkdirs()) {
							throw new DicomException("\""+srcFileName+"\": parent directory creation failed for \""+dstFile+"\"");
						}
					}
					slf4jlogger.info("Copying from \"{}\" to \"{}\"",srcFileName,dstFile);
					list.write(dstFile,TransferSyntax.ExplicitVRLittleEndian,true,true);
				}

			}
			catch (Exception e) {
				slf4jlogger.error("File {}",srcFileName,e);
			}
		}
	}

	/**
	 * <p>Examine a set of SR files and copy them converting SRT codes to SCT codes (per CP 1850).</p>
	 *
	 * @param	srcs
	 * @param	dstFolderName
	 */
	public ConvertSRTCodesToSCT(String[] srcs,String dstFolderName) throws FileNotFoundException, IOException, DicomException {
		loadMapping(xmlSRTSCTMappingResourceName);
		loadMapping(xmlRetiredSRTNewSCTMappingResourceName);

		// 1st stage ... make a list of all the files  ...
		OurMediaImporter importer = new OurMediaImporter(dstFolderName);
		for (String src : srcs) {
			importer.importDicomFiles(src);
		}
	}
	
	/**
	 * <p>Examine a set of SR files and copy them converting SRT codes to SCT codes (per CP 1850).</p>
	 *
	 * @param	arg		array of 2 or more strings - one or more source folder or DICOMDIR and a destination folder
	 */
	public static void main(String arg[]) {
		try {
			if (arg.length >= 2) {
				int nSrcs = arg.length-1;
				String[] srcs = new String[nSrcs];
				System.arraycopy(arg,0,srcs,0,nSrcs);
				new ConvertSRTCodesToSCT(srcs,arg[nSrcs]);
			}
			else {
				System.err.println("Usage: java -cp ./pixelmed.jar com.pixelmed.apps.ConvertSRTCodesToSCT srcdir|DICOMDIR [srcdir|DICOMDIR]* dstdir");
			}
		}
		catch (Exception e) {
			slf4jlogger.error("",e);	// use SLF4J since may be invoked from script
			System.exit(0);
		}
	}
}

