/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.InputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>The {@link DicomDictionary DicomDictionary} class
 * is a complete standard dictionary of DICOM attributes and associated information.</p>
 *
 * <p>The accessor methods that an application would normally use are defined in the
 * {@link DicomDictionaryBase DicomDictionaryBase} class.</p>
 *
 * @author	dclunie
 */

public class DicomDictionary extends DicomDictionaryBase {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/DicomDictionary.java,v 1.166 2022/01/21 19:51:15 dclunie Exp $";
	
	private static final Logger slf4jlogger = LoggerFactory.getLogger(DicomDictionary.class);
	
	protected String xmlDicomDictionaryResourceName = "/dicom/dictionary/elmdict.xml";
	protected String xmlDicomDictionaryIEResourceName = "/dicom/dictionary/elmdictie.xml";

	private Document readXMLDicomDictionary() throws IOException, ParserConfigurationException, SAXException {
		InputStream i = DicomDictionary.class.getResourceAsStream(xmlDicomDictionaryResourceName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(i);
	}

	private Document readXMLDicomDictionaryInformationEntity() throws IOException, ParserConfigurationException, SAXException {
		InputStream i = DicomDictionary.class.getResourceAsStream(xmlDicomDictionaryIEResourceName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(i);
	}
	
	public static final DicomDictionary StandardDictionary = new DicomDictionary();

	public DicomDictionary() {
		super();	// will call createXXX() methods that create empty structures and then we add things as each line in XML is processed
		
		// if a sub-class has created its own tag list (by overriding createTagList()) then:
		// - we do NOT want to add anything extra (i.e., any more tags than are already in tagList
		// - we do DO want to populate all the other corresponding tables
		boolean alreadyHadTagList = !tagList.isEmpty();		// (001190)
		
		try {
			Document dicomDictionaryDocument = readXMLDicomDictionary();
			
			Node dataDictionaryElement = dicomDictionaryDocument.getDocumentElement();
			if (!dataDictionaryElement.getNodeName().equals("DataDictionary")) {
				throw new DicomException("Missing DataDictionary root element in "+xmlDicomDictionaryResourceName);
			}
			
			Node nodeDataElement = dataDictionaryElement.getFirstChild();
			while (nodeDataElement != null) {
				if (nodeDataElement.getNodeName().equals("DataElement")) {
					String groupAsHexString = null;
					String elementAsHexString = null;
					String vr = null;
					String vmmin = null;
					String vmmax = null;
					String keyword = null;
					String name = null;
					
					Node node = nodeDataElement.getFirstChild();
					while (node != null) {
						if (node.getNodeName().equals("Group")) {
							groupAsHexString = node.getTextContent().trim();
						}
						else if (node.getNodeName().equals("Element")) {
							elementAsHexString = node.getTextContent().trim();
						}
						else if (node.getNodeName().equals("VR")) {
							vr = node.getTextContent().trim();
						}
						else if (node.getNodeName().equals("VMMin")) {
							vmmin = node.getTextContent().trim();
						}
						else if (node.getNodeName().equals("VMMax")) {
							vmmax = node.getTextContent().trim();
						}
						else if (node.getNodeName().equals("Keyword")) {
							keyword = node.getTextContent().trim();
						}
						else if (node.getNodeName().equals("Name")) {
							name = node.getTextContent().trim();
						}
						// else ignore it
						node = node.getNextSibling();
					}
					
					//slf4jlogger.debug("Doing ({},{})",groupAsHexString,elementAsHexString);
					
					int group = Integer.parseInt(groupAsHexString.substring(2).replace('x','0'),16);
					int element = Integer.parseInt(elementAsHexString.substring(2).replace('x','0'),16);

					AttributeTag tag = new AttributeTag(group,element);

					// insert everything we know about this element
					
					if (!alreadyHadTagList
					 || tagList.contains(tag)) {
					 	if (!alreadyHadTagList) {
					 		tagList.add(tag);
						}
						valueRepresentationsByTag.put(tag,ValueRepresentation.getValueRepresentationFromString(vr));
						tagByName.put(keyword,tag);
						nameByTag.put(tag,keyword);
						fullNameByTag.put(tag,name);
					}
				}
				// else ignore it
				nodeDataElement = nodeDataElement.getNextSibling();
			}
		}
		catch (Exception e) {
			slf4jlogger.error("Unable to construct DicomDictionary ",e);
		}

		try {
			Document dicomDictionaryDocument = readXMLDicomDictionaryInformationEntity();
			
			Node dataDictionaryElement = dicomDictionaryDocument.getDocumentElement();
			if (!dataDictionaryElement.getNodeName().equals("DataDictionaryIE")) {
				throw new DicomException("Missing DataDictionaryIE root element in "+xmlDicomDictionaryIEResourceName);
			}
			
			Node nodeDataElement = dataDictionaryElement.getFirstChild();
			while (nodeDataElement != null) {
				if (nodeDataElement.getNodeName().equals("DataElementIE")) {
					String name = null;
					String ieString = null;

					Node node = nodeDataElement.getFirstChild();
					while (node != null) {
						if (node.getNodeName().equals("Name")) {		// is actually Keyword
							name = node.getTextContent().trim();
						}
						else if (node.getNodeName().equals("IE")) {
							ieString = node.getTextContent().trim();
						}
						// else ignore it
						node = node.getNextSibling();
					}
					
					AttributeTag tag = (AttributeTag)tagByName.get(name);
					if (tag == null) {
						if (alreadyHadTagList) {
							slf4jlogger.debug("Unrecognized keyword DataDictionaryIE.DataElementIE.Name {} - ignoring",name);
						}
						else {
							slf4jlogger.error("Unrecognized keyword DataDictionaryIE.DataElementIE.Name {} - ignoring",name);
						}
					}
					else {
						InformationEntity ie = InformationEntity.fromString(ieString);
						if (ie == null) {
							if (alreadyHadTagList) {
								slf4jlogger.debug("Unrecognized keyword DataDictionaryIE.DataElementIE.IE {} for keyword {} - ignoring",ieString,name);
							}
							else {
								slf4jlogger.error("Unrecognized keyword DataDictionaryIE.DataElementIE.IE {} for keyword {} - ignoring",ieString,name);
							}
						}
						else {
							informationEntityByTag.put(tag,ie);
						}
					}
				}
				// else ignore it
				nodeDataElement = nodeDataElement.getNextSibling();
			}
		}
		catch (Exception e) {
			slf4jlogger.error("Unable to construct DicomDictionary InformationEntity ",e);
		}
	}

	protected void createTagList() {
		slf4jlogger.debug("createTagList():");
		tagList = new TreeSet();	// sorted, based on AttributeTag's implementation of Comparable
	}

	protected void createValueRepresentationsByTag() {
		valueRepresentationsByTag = new HashMap(100);
	}

	protected void createInformationEntityByTag() {
		informationEntityByTag = new HashMap(100);
	}

	protected void createTagByName() {
		tagByName = new HashMap(100);
		// include old toolkit keywords prior to CP 850 establishment of official keywords, in case old code uses them ...
	}

	protected void createNameByTag() {
		nameByTag = new HashMap(100);
	}
	protected void createFullNameByTag() {
		fullNameByTag = new HashMap(100);
	}
}
