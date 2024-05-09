/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.utils.StringUtilities;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonWriter;

import java.io.*;
import java.util.*;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class to encode a representation of a DICOM Structured Report object in a JSON form,
 * suitable for analysis as human-readable text.</p>
 *
 * <p>Note that JSON representations can either contain only the content tree, or also the additional
 * top level DICOM attributes other than those that encode the content tree, as individual
 * DICOM attributes, in the manner of {@link JSONRepresentationOfDicomObjectFactory JSONRepresentationOfDicomObjectFactory}.</p>
 *
 * <p>A typical example of usage to extract just the content tree would be:</p>
 * <pre>
try {
    AttributeList list = new AttributeList();
    list.read("dicomsrfile",null,true,true);
	StructuredReport sr = new StructuredReport(list);
    JsonArray document = new JSONRepresentationOfStructuredReportObjectFactory().getDocument(sr);
    JSONRepresentationOfStructuredReportObjectFactory.write(System.out,document);
} catch (Exception e) {
    slf4jlogger.error("",e);
 }
 * </pre>
 *
 * <p>or to include the top level attributes as well as the content tree, supply the attribute
 * list as well as the parsed SR content to the write() method:</p>
 * <pre>
try {
    AttributeList list = new AttributeList();
    list.read("dicomsrfile",null,true,true);
	StructuredReport sr = new StructuredReport(list);
    JsonArray document = new JSONRepresentationOfStructuredReportObjectFactory().getDocument(sr,list);
    JSONRepresentationOfStructuredReportObjectFactory.write(System.out,document);
} catch (Exception e) {
    slf4jlogger.error("",e);
 }
 * </pre>
 *
 * <p>or even simpler, if there is no further use for the JSON document or the SR tree model:</p>
 * <pre>
try {
    AttributeList list = new AttributeList();
    list.read("dicomsrfile",null,true,true);
    JSONRepresentationOfStructuredReportObjectFactory.createDocumentAndWriteIt(list,System.out);
} catch (Exception e) {
    slf4jlogger.error("",e);
 }
 * </pre>
 *
 * @see StructuredReport
 * @see JSONRepresentationOfDicomObjectFactory
 * @see com.pixelmed.utils.XPathQuery
 * @see org.w3c.dom.Document
 *
 * @author	dclunie
 */
public class JSONRepresentationOfStructuredReportObjectFactory {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/JSONRepresentationOfStructuredReportObjectFactory.java,v 1.3 2019/05/28 11:31:22 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(JSONRepresentationOfStructuredReportObjectFactory.class);
	
	protected static boolean elideSeparateContinuityOfContent = true;
	
	//public static String businessNameToUseForAnonymousContentItems = "@anon";
	public static String businessNameToUseForAnonymousContentItems = "";

	protected static String reservedKeywordForCodeValueInBusinessNamesFile = "@cv";
	protected static String reservedKeywordForCodingSchemeDesignatorInBusinessNamesFile = "@csd";
	protected static String reservedKeywordForCodeMeaningInBusinessNamesFile = "@cm";
	protected static String reservedKeywordForValueTypeInBusinessNamesFile = "@vt";
	protected static String reservedKeywordForRelationshipTypeInBusinessNamesFile = "@rel";

	protected static String reservedKeywordForContinuityOfContentAttributeInSRFile = "@cont";
	protected static String reservedKeywordForTemplateMappingResourceAttributeInSRFile = "@tmr";
	protected static String reservedKeywordForTemplateIdentifierAttributeInSRFile = "@tid";

	private JsonBuilderFactory factory;
	
	protected Map<String,CodedSequenceItem> businessNames = new HashMap<String,CodedSequenceItem>();
	protected Map<String,SortedSet<String>> valueTypesByBusinessName = new HashMap<String,SortedSet<String>>();
	protected Map<String,SortedSet<String>> relationshipTypesByBusinessName = new HashMap<String,SortedSet<String>>();

	public static String makeBusinessNameFromCodeMeaning(String codeMeaning,boolean titleCase) {
		String businessName = businessNameToUseForAnonymousContentItems;
		if (codeMeaning != null && codeMeaning.length() > 0) {
			businessName = titleCase ? StringUtilities.toTitleCase(codeMeaning) : codeMeaning;
			businessName = businessName.replaceAll("[^A-Za-z0-9]","");
		}
		return businessName;
	}
	
	public static String makeBusinessNameFromCodeMeaning(CodedSequenceItem conceptName) {
		return conceptName == null
			? businessNameToUseForAnonymousContentItems
			: makeBusinessNameFromCodeMeaning(conceptName.getCodeMeaning(),!"UCUM".equals(conceptName.getCodingSchemeDesignator()));
	}

	public JsonArray getBusinessNamesDocument() {
		slf4jlogger.debug("getBusinessNamesDocument():");
		JsonArrayBuilder arrayBuilder = factory.createArrayBuilder();
		for (String businessName : businessNames.keySet()) {
			CodedSequenceItem value = businessNames.get(businessName);
			if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("getBusinessNamesDocument(): Creating JSON business name {} for {}",businessName,value);
			// each entry will be object { "@cv" : "codevalue", "@csd" : "codingschemedesignator", "@cm" : "code meaning" }
			JsonObjectBuilder jsonCodedSequenceItem = factory.createObjectBuilder();
			jsonCodedSequenceItem.add(reservedKeywordForCodeValueInBusinessNamesFile,value.getCodeValue());
			jsonCodedSequenceItem.add(reservedKeywordForCodingSchemeDesignatorInBusinessNamesFile,value.getCodingSchemeDesignator());
			jsonCodedSequenceItem.add(reservedKeywordForCodeMeaningInBusinessNamesFile,value.getCodeMeaning());
			// should probably add version if present too :(
			{
				SortedSet<String> valueTypes = valueTypesByBusinessName.get(businessName);
				if (valueTypes != null && valueTypes.size() > 0) {
					JsonArrayBuilder jsonValueTypes = factory.createArrayBuilder();
					for (String valueType : valueTypes) {
						if (valueType != null && valueType.length() > 0) {
							jsonValueTypes.add(valueType);
						}
					}
					jsonCodedSequenceItem.add(reservedKeywordForValueTypeInBusinessNamesFile,jsonValueTypes);
				}
			}
			{
				SortedSet<String> relationshipTypes = relationshipTypesByBusinessName.get(businessName);
				if (relationshipTypes != null && relationshipTypes.size() > 0) {
					JsonArrayBuilder jsonRelationshipTypes = factory.createArrayBuilder();
					for (String relationshipType : relationshipTypes) {
						if (relationshipType != null && relationshipType.length() > 0) {
							jsonRelationshipTypes.add(relationshipType);
						}
					}
					jsonCodedSequenceItem.add(reservedKeywordForRelationshipTypeInBusinessNamesFile,jsonRelationshipTypes);
				}
			}

			JsonObjectBuilder jsonBusinessNameEntry = factory.createObjectBuilder();
			jsonBusinessNameEntry.add(businessName,jsonCodedSequenceItem);
			
			arrayBuilder.add(jsonBusinessNameEntry);
		}
		return arrayBuilder.build();
	}

	/**
	 * <p>Load the business names encoded in a JSON document.</p>
	 *
	 * @param		document		the JSON document
	 * @throws	DicomException
	 */
	public void loadBusinessNamesDocument(JsonArray document) throws DicomException {
		for (int i=0; i<document.size(); ++i) {
			try {
				JsonObject businessNameEntry = document.getJsonObject(i);
				if (businessNameEntry != null ) {
					// should be, e.g. {"SpecificImageFindings":{"@cv":"999000","@csd":"LNdemo","@cm":"Specific Image Findings"}}
					String businessName = businessNameEntry.keySet().iterator().next();
					if (businessName != null && businessName.length() > 0) {
						try {
							JsonObject businessNamePayload = (JsonObject)(businessNameEntry.get(businessName));
							try {
								JsonString jsonCodeValue = (JsonString)(businessNamePayload.get(reservedKeywordForCodeValueInBusinessNamesFile));
								if (jsonCodeValue != null) {
									// business name is a coded concept tuple
									String cv = jsonCodeValue.getString();
									String csd = null;
									String cm = null;
									{
										JsonString jsonCodingSchemeDesignator = (JsonString)(businessNamePayload.get(reservedKeywordForCodingSchemeDesignatorInBusinessNamesFile));
										if (jsonCodingSchemeDesignator != null) {
											csd = jsonCodingSchemeDesignator.getString();
										}
										else {
											throw new DicomException("Missing "+reservedKeywordForCodingSchemeDesignatorInBusinessNamesFile+" for code "+cv+" for business name "+businessName);
										}
									}
									{
										JsonString jsonCodeMeaning = (JsonString)(businessNamePayload.get(reservedKeywordForCodeMeaningInBusinessNamesFile));
										if (jsonCodeMeaning != null) {
											cm = jsonCodeMeaning.getString();
										}
										else {
											throw new DicomException("Missing "+reservedKeywordForCodeMeaningInBusinessNamesFile+" for code "+cv+" for business name "+businessName);
										}
									}

									if (cv != null && cv.length() > 0
										&& csd != null && csd.length() > 0
										&& cm != null && cm.length() > 0
									) {
										CodedSequenceItem value = new CodedSequenceItem(cv,csd,cm);
										if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("loadBusinessNamesDocument(): Loading JSON business name {} for {}",businessName,value);
										businessNames.put(businessName,value);
									}
									else {
										throw new DicomException("Incomplete code value, coding scheme designator or code meaning for code "+cv+" for business name "+businessName);
									}
								}
								else {
									throw new DicomException("Unrecognized business name pattern entry for business name "+businessName);
								}
								
								// extract value and relationship type regardless of whether code value is present or not, even though probably not usable in that case
								try {
									JsonArray jsonValueTypes = (JsonArray)(businessNamePayload.get(reservedKeywordForValueTypeInBusinessNamesFile));
									if (jsonValueTypes != null) {
										for (int j=0; j<jsonValueTypes.size(); ++j) {
											JsonValue jsonValueTypeObject = jsonValueTypes.get(j);
											try {
												String valueType = ((JsonString)jsonValueTypeObject).getString();
												if (valueType != null && valueType.length() > 0) {
													SortedSet<String> valueTypes = valueTypesByBusinessName.get(businessName);
													if (valueTypes == null) {
														valueTypes = new TreeSet<String>();
														valueTypesByBusinessName.put(businessName,valueTypes);
													}
													valueTypes.add(valueType);
												}
												else {
													throw new DicomException("Empty or missing value type for business name "+businessName);
												}
											}
											catch (ClassCastException e) {
												throw new DicomException("String value type required in array for business name "+businessName);
											}
										}
									}
									// else OK to be absent
								}
								catch (ClassCastException e) {
									throw new DicomException("Array of value types required for business name "+businessName);
								}

								try {
									JsonArray jsonRelationshipTypes = (JsonArray)(businessNamePayload.get(reservedKeywordForRelationshipTypeInBusinessNamesFile));
									if (jsonRelationshipTypes != null) {
										for (int j=0; j<jsonRelationshipTypes.size(); ++j) {
											JsonValue jsonRelationshipTypeObject = jsonRelationshipTypes.get(j);
											try {
												String relationshipType = ((JsonString)jsonRelationshipTypeObject).getString();
												if (relationshipType != null && relationshipType.length() > 0) {
													SortedSet<String> relationshipTypes = relationshipTypesByBusinessName.get(businessName);
													if (relationshipTypes == null) {
														relationshipTypes = new TreeSet<String>();
														relationshipTypesByBusinessName.put(businessName,relationshipTypes);
													}
													relationshipTypes.add(relationshipType);
												}
												else {
													throw new DicomException("Empty or missing relationship type for business name "+businessName);
												}
											}
											catch (ClassCastException e) {
												throw new DicomException("String relationship type required in array for business name "+businessName);
											}
										}
									}
									// else OK to be absent
								}
								catch (ClassCastException e) {
									throw new DicomException("Array of relationship types required for business name "+businessName);
								}
							}
							catch (ClassCastException e) {
								throw new DicomException("Expected strings for values of business name entry # "+i);
							}
						}
						catch (ClassCastException e) {
							throw new DicomException("Expected object as value of business name "+businessName+" entry # "+i);
						}
					}
					else {
						throw new DicomException("Missing or bad business name "+businessName+" in entry # "+i);
					}
				}
				else {
					throw new DicomException("Missing business name entry # "+i);
				}
			}
			catch (ClassCastException e) {
				throw new DicomException("Expected object for business name entry # "+i);
			}
		}
	}
	
	/**
	 * <p>Load the business names encoded in a JSON document.</p>
	 *
	 * @param		stream			the input stream containing the JSON document
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public void loadBusinessNamesDocument(InputStream stream) throws IOException, DicomException {
		JsonReader jsonReader = Json.createReader(stream);
		JsonArray document = jsonReader.readArray();
		jsonReader.close();
		loadBusinessNamesDocument(document);
	}
	
	/**
	 * <p>Load the business names encoded in a JSON document.</p>
	 *
	 * @param		file			the input file containing the JSON document
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public void loadBusinessNamesDocument(File file) throws IOException, DicomException {
		InputStream fi = new FileInputStream(file);
		BufferedInputStream bi = new BufferedInputStream(fi);
		try {
			loadBusinessNamesDocument(bi);
		}
		finally {
			bi.close();
			fi.close();
		}
	}
	
	/**
	 * <p>Load the business names encoded in a JSON document.</p>
	 *
	 * @param		name			the input file containing the JSON document
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public void loadBusinessNamesDocument(String name) throws IOException, DicomException {
		loadBusinessNamesDocument(new File(name));
	}

	/**
	 * @param	contentItem		content item node of the Structured Report
	 * @param	parentObject	the JSON object to add to
	 * @throws	DicomException
	 */
	private void addContentItemAndChildrenToJsonObject(ContentItem contentItem,JsonObjectBuilder parentObject) throws DicomException {
		if (contentItem != null) {
			String valueType = contentItem.getValueType();
			//if (valueType == null || valueType.length() == 0) {
			//	throw new DicomException("Converting by-reference relationships to JSON not supported");
			//}
			
			String relationshipType = contentItem.getRelationshipType();
			//String observationDateTime = contentItem.getObservationDateTime();
			//String observationUID = contentItem.getObservationUID();
			
			//String referencedContentItemIdentifier = contentItem.getReferencedContentItemIdentifier();
			//if (referencedContentItemIdentifier != null && referencedContentItemIdentifier.length() > 0) {
			//	throw new DicomException("Converting by-reference relationships to JSON not supported");
			//}
			
			CodedSequenceItem conceptName = contentItem.getConceptName();
			String businessName = makeBusinessNameFromCodeMeaning(conceptName);
			if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("addContentItemAndChildrenToJsonObject(): businessName is {} for conceptName {}",businessName,conceptName);
			// OK for conceptName to be missing ... this is a so-called anonymous content item, e.g., as is often the case for IMAGE value type
			if (conceptName != null && businessName != null && businessName.length() > 0) {
				businessNames.put(businessName,conceptName);
			}
			if (valueType != null && valueType.length() > 0) {
				SortedSet<String> valueTypes = valueTypesByBusinessName.get(businessName);
				if (valueTypes == null) {
					valueTypes = new TreeSet<String>();
					valueTypesByBusinessName.put(businessName,valueTypes);
				}
				valueTypes.add(valueType);
			}
			if (relationshipType != null && relationshipType.length() > 0) {
				SortedSet<String> relationshipTypes = relationshipTypesByBusinessName.get(businessName);
				if (relationshipTypes == null) {
					relationshipTypes = new TreeSet<String>();
					relationshipTypesByBusinessName.put(businessName,relationshipTypes);
				}
				relationshipTypes.add(relationshipType);
			}
			
			JsonArrayBuilder valuesAndChildren = factory.createArrayBuilder();
			
			if (contentItem instanceof ContentItemFactory.ContainerContentItem) {
				String continuityOfContent = ((ContentItemFactory.ContainerContentItem)contentItem).getContinuityOfContent();
				String templateMappingResource = ((ContentItemFactory.ContainerContentItem)contentItem).getTemplateMappingResource();
				String templateIdentifier = ((ContentItemFactory.ContainerContentItem)contentItem).getTemplateIdentifier();
				if (continuityOfContent != null && continuityOfContent.length() > 0 && !elideSeparateContinuityOfContent || !continuityOfContent.equals("SEPARATE")
				 || templateMappingResource != null && templateMappingResource.length() > 0
				 || templateIdentifier != null && templateIdentifier.length() > 0) {
					JsonObjectBuilder containerAttributesObject = factory.createObjectBuilder();
					if (continuityOfContent != null && continuityOfContent.length() > 0 && !elideSeparateContinuityOfContent || !continuityOfContent.equals("SEPARATE")) {
						containerAttributesObject.add(reservedKeywordForContinuityOfContentAttributeInSRFile,continuityOfContent);
					}
					if (templateMappingResource != null && templateMappingResource.length() > 0) {
						containerAttributesObject.add(reservedKeywordForTemplateMappingResourceAttributeInSRFile,templateMappingResource);
					}
					if (templateIdentifier != null && templateIdentifier.length() > 0) {
						containerAttributesObject.add(reservedKeywordForTemplateIdentifierAttributeInSRFile,templateIdentifier);
					}
					valuesAndChildren.add(containerAttributesObject);
				}
			}
			else if (contentItem instanceof ContentItemFactory.CodeContentItem) {
				CodedSequenceItem conceptCode = ((ContentItemFactory.CodeContentItem)contentItem).getConceptCode();
				if (conceptCode != null) {
					String businessNameForConceptCode = makeBusinessNameFromCodeMeaning(conceptCode);
					if (businessNameForConceptCode != null && businessNameForConceptCode.length() > 0) {
						if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("addContentItemAndChildrenToJsonObject(): businessNameForConceptCode is {} for conceptCode {}",businessNameForConceptCode,conceptCode);
						businessNames.put(businessNameForConceptCode,conceptCode);
						// obviously not adding value type and relationship type, since used as value not concept name
						valuesAndChildren.add(businessNameForConceptCode);
					}
					// else what does it mean not to be able to get a business name ? should be an exception :(
				}
			}
			else if (contentItem instanceof ContentItemFactory.NumericContentItem) {
				String value = ((ContentItemFactory.NumericContentItem)contentItem).getNumericValue();
				CodedSequenceItem unitsCode = ((ContentItemFactory.NumericContentItem)contentItem).getUnits();
				String businessNameForUnitsCode = null;
				if (unitsCode != null) {
					businessNameForUnitsCode = makeBusinessNameFromCodeMeaning(unitsCode);
					if (businessNameForUnitsCode != null && businessNameForUnitsCode.length() > 0) {
						if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("addContentItemAndChildrenToJsonObject(): businessNameForUnitsCode is {} for conceptCode {}",businessNameForUnitsCode,unitsCode);
						businessNames.put(businessNameForUnitsCode,unitsCode);
						// obviously not adding value type and relationship type, since used as value not concept name
					}
				}
				if (value != null && value.length() > 0
					&& businessNameForUnitsCode != null && businessNameForUnitsCode.length() > 0) {
					valuesAndChildren.add(value);
					valuesAndChildren.add(businessNameForUnitsCode);
				}
				// else what does it mean not to be able to get a value and units business name ? should be an exception :(
			}
			else if (contentItem instanceof ContentItemFactory.StringContentItem) {
				String value = ((ContentItemFactory.StringContentItem)contentItem).getConceptValue();
				if (value != null && value.length() > 0) {
					valuesAndChildren.add(value);
				}
				// else what does it mean not to be able to get a value ? should be an exception :(
			}
			else if (contentItem instanceof ContentItemFactory.SpatialCoordinatesContentItem) {
				String graphicType = ((ContentItemFactory.SpatialCoordinatesContentItem)contentItem).getGraphicType();
				if (graphicType != null) {	// regardless of whether zero length or not, need node to append data to
					float[] graphicData = ((ContentItemFactory.SpatialCoordinatesContentItem)contentItem).getGraphicData();
					if (graphicData != null) {
						JsonArrayBuilder graphicDataArray = factory.createArrayBuilder();
						for (int i=0; i<graphicData.length; ++i) {
							graphicDataArray.add((double)graphicData[i]);
						}
						valuesAndChildren.add(graphicType);
						valuesAndChildren.add(graphicDataArray);
					}
				}
			}
			//ContentItemFactory.SpatialCoordinates3DContentItem
			//ContentItemFactory.TemporalCoordinatesContentItem
			else if (contentItem instanceof ContentItemFactory.CompositeContentItem) {
				String referencedSOPClassUID = ((ContentItemFactory.CompositeContentItem)contentItem).getReferencedSOPClassUID();
				String referencedSOPInstanceUID = ((ContentItemFactory.CompositeContentItem)contentItem).getReferencedSOPInstanceUID();
				if (referencedSOPClassUID != null && referencedSOPClassUID.length() > 0
					&& referencedSOPInstanceUID != null && referencedSOPInstanceUID.length() > 0) {
					valuesAndChildren.add(referencedSOPClassUID);
					valuesAndChildren.add(referencedSOPInstanceUID);
					if (contentItem instanceof ContentItemFactory.ImageContentItem) {
						ContentItemFactory.ImageContentItem imageContentItem = (ContentItemFactory.ImageContentItem)contentItem;
						int referencedFrameNumber = imageContentItem.getReferencedFrameNumber();
						int referencedSegmentNumber = imageContentItem.getReferencedSegmentNumber();
						String presentationStateSOPClassUID = imageContentItem.getPresentationStateSOPClassUID();
						String presentationStateSOPInstanceUID = imageContentItem.getPresentationStateSOPInstanceUID();
						String realWorldValueMappingSOPClassUID = imageContentItem.getRealWorldValueMappingSOPClassUID();
						String realWorldValueMappingSOPInstanceUID = imageContentItem.getRealWorldValueMappingSOPInstanceUID();
						// forget about icon image sequence for now :(
						if (referencedFrameNumber != 0
							|| referencedSegmentNumber != 0
							|| (presentationStateSOPClassUID != null && presentationStateSOPClassUID.length() > 0)
							|| (presentationStateSOPInstanceUID != null && presentationStateSOPInstanceUID.length() > 0)
							|| (realWorldValueMappingSOPClassUID != null && realWorldValueMappingSOPClassUID.length() > 0)
							|| (realWorldValueMappingSOPInstanceUID != null && realWorldValueMappingSOPInstanceUID.length() > 0)
							) {
							if (referencedFrameNumber != 0) {
								valuesAndChildren.add(referencedFrameNumber);
							}
							else {
								valuesAndChildren.add((String)null);
							}
							if (referencedFrameNumber != 0) {
								valuesAndChildren.add(referencedSegmentNumber);
							}
							else {
								valuesAndChildren.add((String)null);
							}
							valuesAndChildren.add(presentationStateSOPClassUID);
							valuesAndChildren.add(presentationStateSOPInstanceUID);
							valuesAndChildren.add(realWorldValueMappingSOPClassUID);
							valuesAndChildren.add(realWorldValueMappingSOPInstanceUID);
						}
					}
				}
			}
			
			
			//else {
			//	throw new DicomException("Content item value type "+valueType+" conversion to JSON not yet supported");
			//}
			
			// now handle any children, which must be in array rather than object since concept names may not be unique
			int n = contentItem.getChildCount();
			if (n > 0) {
				JsonArrayBuilder children = factory.createArrayBuilder();
				for (int i=0; i<n; ++i) {
					JsonObjectBuilder child = factory.createObjectBuilder();
					addContentItemAndChildrenToJsonObject((ContentItem)(contentItem.getChildAt(i)),child);
					children.add(child);
				}
				valuesAndChildren.add(children);
			}
			
			parentObject.add(businessName,valuesAndChildren);	// don't do this until AFTER children have been added or it doesn't work (children are ignored)
		}
		
	}

	/**
	 * <p>Construct a factory object, which can be used to get JSON documents from DICOM objects.</p>
	 *
	 */
	public JSONRepresentationOfStructuredReportObjectFactory() {
		factory = Json.createBuilderFactory(null/*config*/);
	}
	
	/**
	 * <p>Given a DICOM attribute list encoding a Structured Report, get a JSON document.</p>
	 *
	 * @param		list			the attribute list
	 * @exception	DicomException
	 * @return						the JSON encoded DICOM SR document

	 */
	public JsonArray getDocument(AttributeList list) throws DicomException {
		return getDocument(null,list);
	}
	
	/**
	 * <p>Given a DICOM Structured Report, get a JSON document of the content tree only.</p>
	 *
	 * @param		sr				the Structured Report
	 * @exception	DicomException
	 * @return						the JSON encoded DICOM SR document
	 */
	public JsonArray getDocument(StructuredReport sr) throws DicomException {
		return getDocument(sr,null);
	}
	
	/**
	 * <p>Given a DICOM Structured Report, get a JSON document of the content tree and the top level DICOM elements.</p>
	 *
	 * @param		sr				the Structured Report			may be null if list is not - will build an sr tree model
	 * @param		list			the attribute list				may be null if only the sr content tree is to be added
	 * @exception	DicomException
	 * @return						the JSON encoded DICOM SR document
	 */
	public JsonArray getDocument(StructuredReport sr,AttributeList list) throws DicomException {
		JsonObjectBuilder topLevelObjectBuilder = factory.createObjectBuilder();
		if (sr == null) {
			try {
				sr = new StructuredReport(list);
			}
			catch (DicomException e) {
				slf4jlogger.error("",e);
			}
		}
		if (list != null) {
			AttributeList clonedList = (AttributeList)(list.clone());
 			clonedList.removePrivateAttributes();
 			clonedList.removeGroupLengthAttributes();
 			clonedList.removeMetaInformationHeaderAttributes();
 			//clonedList.remove(TagFromName.ContentSequence);
			//clonedList.remove(TagFromName.ValueType);
			//clonedList.remove(TagFromName.ContentTemplateSequence);
			//clonedList.remove(TagFromName.ContinuityOfContent);
			//clonedList.remove(TagFromName.ConceptNameCodeSequence);
			
			new JSONRepresentationOfDicomObjectFactory().addAttributesFromListToJsonObject(clonedList,topLevelObjectBuilder,true/*useKeywordInsteadOfTag*/,false/*addTag*/,false/*addKeyword*/,true/*ignoreSR*/);
		}
		if (sr != null) {
			addContentItemAndChildrenToJsonObject((ContentItem)(sr.getRoot()),topLevelObjectBuilder);
		}
		return factory.createArrayBuilder().add(topLevelObjectBuilder).build();
	}
	
	/**
	 * <p>Given a DICOM object encoded as a list of attributes, get a JSON document.</p>
	 *
	 * @param	file	the DICOM file
	 * @return							the JSON document
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public JsonArray getDocument(File file) throws IOException, DicomException {
		AttributeList list = new AttributeList();
		list.setDecompressPixelData(false);
		list.read(file);
		return getDocument(null/*sr*/,list);	// will build an sr tree model from list
	}
	
	/**
	 * <p>Given a DICOM object encoded as a list of attributes, get a JSON document.</p>
	 *
	 * @param	filename				the DICOM file name
	 * @return							the JSON document
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public JsonArray getDocument(String filename) throws IOException, DicomException {
		return getDocument(new File(filename));
	}

	protected ContentItemFactory contentItemFactory;
	
	protected String getStringFromSelectedContentItemValue(JsonArray contentItemValues,int which,String what,String valueType,CodedSequenceItem concept) {
		String value = null;
		JsonValue entry = contentItemValues.get(which);
		if (entry != null && entry.getValueType() == JsonValue.ValueType.STRING) {
			value = ((JsonString)entry).getString();
			if (value == null || value.length() == 0) {
				slf4jlogger.error("Missing {} in {} content item for concept {} ",what,valueType,concept);
			}
		}
		else {
			slf4jlogger.error("Missing {} string in {} content item for concept {} ",what,valueType,concept);
		}
		return value;
	}


	protected String getSingleStringValueOrNullFromJsonContentItemValue(JsonValue contentItemValue,String valueType,CodedSequenceItem concept) {
		String value = null;
		if (contentItemValue != null && contentItemValue.getValueType() == JsonValue.ValueType.ARRAY) {
			JsonArray contentItemValueAndChildrenArray = (JsonArray)contentItemValue;
			if (contentItemValueAndChildrenArray.size() > 0) {
				JsonValue entry = contentItemValueAndChildrenArray.get(0);
				if (entry != null && entry.getValueType() == JsonValue.ValueType.STRING) {
					value = ((JsonString)entry).getString();
					if (value == null || value.length() == 0) {
						slf4jlogger.error("Missing string value in {} content item for concept {} ",valueType,concept);
					}
				}
				else {
					slf4jlogger.error("Missing numeric value string in {} content item for concept {} ",valueType,concept);
				}
			}
			else {
				slf4jlogger.error("No value in {} content item for concept {} ",valueType,concept);
			}
		}
		else {
			slf4jlogger.error("No array of value +/- children in {} content item for concept {} ",valueType,concept);
		}
		return value;
	}

	protected boolean haveChildrenForSingleStringJsonContentItemValue(JsonValue contentItemValue) {
		return contentItemValue != null && contentItemValue.getValueType() == JsonValue.ValueType.ARRAY && ((JsonArray)contentItemValue).size() > 1;
	}
	
	protected String determineUnknownValueType(String parentValueType,JsonValue childObjectValue) {
		if (parentValueType.equals("TEXT") || parentValueType.equals("CODE") || parentValueType.equals("NUM")) {	// per PS3.3 Table A.35.3-2. Relationship Content Constraints for Comprehensive SR IOD
			if (childObjectValue.getValueType() == JsonValue.ValueType.ARRAY) {
				JsonArray childValueAndChildrenArray = (JsonArray)childObjectValue;
				if (childValueAndChildrenArray.size() > 0) {
					JsonValue firstEntry = childValueAndChildrenArray.get(0);
					if (firstEntry.getValueType() == JsonValue.ValueType.STRING) {
						String firstEntryString = ((JsonString)firstEntry).getString();
						if (firstEntryString != null && firstEntryString.length() > 0) {
							if (firstEntryString.equals("POINT")
							 || firstEntryString.equals("MULTIPOINT")
							 || firstEntryString.equals("POLYLINE")
							 || firstEntryString.equals("CIRCLE")
							 || firstEntryString.equals("ELLIPSE")
							) {
								return "SCOORD";
							}
						}
					}
				}
			}
		}
		// regardless of parentValueType
		{
			if (childObjectValue.getValueType() == JsonValue.ValueType.ARRAY) {
				JsonArray childValueAndChildrenArray = (JsonArray)childObjectValue;
				if (childValueAndChildrenArray.size() > 1) {	// only going to look at the 1st value, but IMAGE, WAVEFORM, COMPOSITE always have at 2 or more
					JsonValue firstEntry = childValueAndChildrenArray.get(0);
					if (firstEntry.getValueType() == JsonValue.ValueType.STRING) {
						String sopClassUID = ((JsonString)firstEntry).getString();
						if (sopClassUID != null && sopClassUID.length() > 0) {
							if (SOPClass.isImageStorage(sopClassUID)) {
								return "IMAGE";
							}
							else if (SOPClass.isWaveform(sopClassUID)) {
								return "WAVEFORM";
							}
							else if (SOPClass.isStorage(sopClassUID)) {
								return "COMPOSITE";
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	protected String determineUnknownRelationshipType(String parentValueType,String childValueType,JsonValue childObjectValue) {
		if (parentValueType.equals("TEXT") || parentValueType.equals("CODE") || parentValueType.equals("NUM")) {	// per PS3.3 Table A.35.3-2. Relationship Content Constraints for Comprehensive SR IOD
			if ("SCOORD".equals(childValueType)) {	// allow for null, e.g. by reference
				return "INFERRED FROM";
			}
		}
		if (parentValueType.equals("SCOORD")) {	// per PS3.3 Table A.35.3-2. Relationship Content Constraints for Comprehensive SR IOD
			if ("IMAGE".equals(childValueType)) {	// allow for null, e.g. by reference
				return "SELECTED FROM";
			}
		}
		if (parentValueType.equals("CONTAINER")) {
			return "CONTAINS";
		}
		return null;
	}

	protected ContentItem getContentItemAndChildrenFromJSONObjectValue(String businessName,CodedSequenceItem concept,String valueType,String relationshipType,JsonValue contentItemValue) throws DicomException {
		slf4jlogger.debug("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): businessName = "+businessName);
		if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): concept = {}",concept);
		ContentItem contentItem = null;
		if (/*concept != null && */contentItemValue != null) {
			slf4jlogger.debug("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): valueType = "+valueType);
			slf4jlogger.debug("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): relationshipType = "+relationshipType);
			//String observationDateTime = null;
			//System.err.println("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): observationDateTime = "+observationDateTime);
			//String observationUID = null;
			//System.err.println("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): observationUID = "+observationUID);
			JsonArray contentItemChildren = null;	// as we process each type of SR content item, we will find and populate this, if not a leaf
			JsonArray lastEntryInValueArray = null;	// depending on SR content item value type, this will probably be what we want for contentItemChildren
			{
				if (contentItemValue != null && contentItemValue.getValueType() == JsonValue.ValueType.ARRAY) {
					JsonArray contentItemValueAndChildrenArray = (JsonArray)contentItemValue;
					if (contentItemValueAndChildrenArray.size() > 0) {
						JsonValue lastEntryCandidate = contentItemValueAndChildrenArray.get(contentItemValueAndChildrenArray.size()-1);
						if (lastEntryCandidate != null && lastEntryCandidate.getValueType() == JsonValue.ValueType.ARRAY) {
							lastEntryInValueArray = (JsonArray)(lastEntryCandidate);
							if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): have potential children in last array entry for concept = {}",concept);
						}
					}
				}
			}
			
			if (valueType.equals("CONTAINER")) {
				String continuity = null;
				String template = null;
				String templatemappingresource = null;
				// containers have no value, but may have an object preceding the children for the attributes specific to containers
				if (contentItemValue != null && contentItemValue.getValueType() == JsonValue.ValueType.ARRAY) {
					JsonArray containerContentItemAttributesAndChildrenArray = (JsonArray)contentItemValue;
					if (containerContentItemAttributesAndChildrenArray.size() > 0) {
						JsonValue firstEntryCandidate = containerContentItemAttributesAndChildrenArray.get(0);
						if (firstEntryCandidate != null && firstEntryCandidate.getValueType() == JsonValue.ValueType.OBJECT) {
							JsonObject containerAttributesObject = (JsonObject)firstEntryCandidate;
							for (String attributeName : containerAttributesObject.keySet()) {
								JsonValue attributeValue = containerAttributesObject.get(attributeName);
								if (attributeValue != null && attributeValue.getValueType() == JsonValue.ValueType.STRING) {
									String attributeValueString = ((JsonString)attributeValue).getString();
									if (attributeName.equals(reservedKeywordForContinuityOfContentAttributeInSRFile)) {
										continuity = attributeValueString;
										System.err.println("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): continuity = "+continuity);
									}
									else if (attributeName.equals(reservedKeywordForTemplateIdentifierAttributeInSRFile)) {
										template = attributeValueString;
										System.err.println("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): template = "+template);
									}
									else if (attributeName.equals(reservedKeywordForTemplateMappingResourceAttributeInSRFile)) {
										templatemappingresource = attributeValueString;
										System.err.println("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): templatemappingresource = "+templatemappingresource);
									}
									else {
										slf4jlogger.error("Unrecognized CONTAINER attribute {} for concept {} ",attributeName,concept);
									}
								}
								else {
									slf4jlogger.error("Incorrect JSON type for value of attribute {} for concept {} ",attributeName,concept);
								}
							}
							
							if (containerContentItemAttributesAndChildrenArray.size() > 1) {
								JsonValue secondEntryCandidate = containerContentItemAttributesAndChildrenArray.get(1);
								if (secondEntryCandidate != null && secondEntryCandidate.getValueType() == JsonValue.ValueType.ARRAY) {
									contentItemChildren = (JsonArray)secondEntryCandidate;
									if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): CONTAINER with children and attributes concept = {}",concept);
								}
								else {
									slf4jlogger.error("Incorrrect second array JSON type (expected array of children) for CONTAINER content item for concept {} ",concept);
								}
							}
							else {
								if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): empty CONTAINER with only attributes concept = {}",concept);
							}
						}
						else if (firstEntryCandidate != null && firstEntryCandidate.getValueType() == JsonValue.ValueType.ARRAY) {
							contentItemChildren = (JsonArray)firstEntryCandidate;
							if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): CONTAINER with children but no attributes concept = {}",concept);
						}
						else {
							slf4jlogger.error("Malformed non-empty array for CONTAINER content item for concept {} ",concept);
						}
					}
					else {
						if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): no attributes or children for CONTAINER concept = {}",concept);
					}
				}
				else {
					slf4jlogger.error("No array of value +/- children in CODE content item for concept {} ",concept);
				}

				contentItem = contentItemFactory.makeContainerContentItem(
																		  null /* parent will be set later by addChild() operation */,
																		  relationshipType,
																		  concept,
																		  continuity != null && continuity.equals("SEPARATE"),
																		  templatemappingresource,template/*,
																		  observationDateTime,observationUID*/);
			}
			else if (valueType.equals("CODE")) {
				CodedSequenceItem value = null;
				if (contentItemValue != null && contentItemValue.getValueType() == JsonValue.ValueType.ARRAY) {
					JsonArray contentItemValueAndChildrenArray = (JsonArray)contentItemValue;
					if (contentItemValueAndChildrenArray.size() > 0) {
						JsonValue firstEntryCandidate = contentItemValueAndChildrenArray.get(0);
						if (firstEntryCandidate != null && firstEntryCandidate.getValueType() == JsonValue.ValueType.STRING) {
							String valueBusinessName = ((JsonString)firstEntryCandidate).getString();
							value = businessNames.get(valueBusinessName);
							if (value == null) {
								slf4jlogger.error("Unrecognized business name {}} for value in CODE content item for concept {} ",valueBusinessName,concept);
							}
						}
						else {
							slf4jlogger.error("Missing business name string in CODE content item for concept {} ",concept);
						}
						// check we have children
						if (contentItemValueAndChildrenArray.size() > 1) {
							contentItemChildren = lastEntryInValueArray;
							if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): have children for CODE concept = {}",concept);
						}
						else {
							// else we only had one entry that was the code value
							if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): no children for CODE concept = {}",concept);
						}
					}
					else {
						slf4jlogger.error("No value in CODE content item for concept {} ",concept);
					}
				}
				else {
					slf4jlogger.error("No array of value +/- children in CODE content item for concept {} ",concept);
				}
				contentItem = contentItemFactory.makeCodeContentItem(
					null /* parent will be set later by addChild() operation */,
					relationshipType,
					concept,
					value/*,
					observationDateTime,observationUID*/);
			}
			else if (valueType.equals("NUM")) {
				String value = null;
				CodedSequenceItem units = null;
				if (contentItemValue != null && contentItemValue.getValueType() == JsonValue.ValueType.ARRAY) {
					JsonArray contentItemValueAndChildrenArray = (JsonArray)contentItemValue;
					if (contentItemValueAndChildrenArray.size() > 1) {
						value = getStringFromSelectedContentItemValue(contentItemValueAndChildrenArray,0,"numeric",valueType,concept);	// should this be NUMBER instead ?
						{
							JsonValue secondEntryCandidate = contentItemValueAndChildrenArray.get(1);
							if (secondEntryCandidate != null && secondEntryCandidate.getValueType() == JsonValue.ValueType.STRING) {
								String unitsBusinessName = ((JsonString)secondEntryCandidate).getString();
								units = businessNames.get(unitsBusinessName);
								if (value == null) {
									slf4jlogger.error("Unrecognized business name {} for units in NUM content item for concept {} ",unitsBusinessName,concept);
								}
							}
							else {
								slf4jlogger.error("Missing business name string in NUM content item for concept {}",concept);
							}
						}
						// check we have children
						if (contentItemValueAndChildrenArray.size() > 2) {
							contentItemChildren = lastEntryInValueArray;
							if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): have children for NUM concept = {}",concept);
						}
						else {
							// else we only had one entry that was the code value
							if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("JSONRepresentationOfStructuredReportObjectFactory.getContentItemAndChildrenFromJSONObjectValue(): no children for NUM concept = {}",concept);
						}
					}
					else {
						slf4jlogger.error("No value and units in NUM content item for concept {} ",concept);
					}
				}
				else {
					slf4jlogger.error("No array of value +/- children in NUM content item for concept {} ",concept);
				}
				contentItem = contentItemFactory.makeNumericContentItem(
						null /* parent will be set later by addChild() operation */,
						relationshipType,
						concept,
						value/*,
						floatingPointValue,
						rationalNumeratorValue,
						rationalDenominatorValue*/,
						units/*,
						qualifier,
						observationDateTime,observationUID*/);
			}
			else if (valueType.equals("DATETIME")) {
				String value = getSingleStringValueOrNullFromJsonContentItemValue(contentItemValue,valueType,concept);
				if (haveChildrenForSingleStringJsonContentItemValue(contentItemValue)) {
					contentItemChildren = lastEntryInValueArray;
				}
				contentItem = contentItemFactory.makeDateTimeContentItem(
																		 null /* parent will be set later by addChild() operation */,
																		 relationshipType,
																		 concept,
																		 value/*,
																			   observationDateTime,observationUID*/);
			}
			else if (valueType.equals("DATE")) {
				String value = getSingleStringValueOrNullFromJsonContentItemValue(contentItemValue,valueType,concept);
				if (haveChildrenForSingleStringJsonContentItemValue(contentItemValue)) {
					contentItemChildren = lastEntryInValueArray;
				}
				contentItem = contentItemFactory.makeDateContentItem(
																	 null /* parent will be set later by addChild() operation */,
																	 relationshipType,
																	 concept,
																	 value/*,
																		   observationDateTime,observationUID*/);
			}
			else if (valueType.equals("TIME")) {
				String value = getSingleStringValueOrNullFromJsonContentItemValue(contentItemValue,valueType,concept);
				if (haveChildrenForSingleStringJsonContentItemValue(contentItemValue)) {
					contentItemChildren = lastEntryInValueArray;
				}
				contentItem = contentItemFactory.makeTimeContentItem(
																	 null /* parent will be set later by addChild() operation */,
																	 relationshipType,
																	 concept,
																	 value/*,
																		   observationDateTime,observationUID*/);
			}
			else if (valueType.equals("PNAME")) {
				String value = getSingleStringValueOrNullFromJsonContentItemValue(contentItemValue,valueType,concept);
				if (haveChildrenForSingleStringJsonContentItemValue(contentItemValue)) {
					contentItemChildren = lastEntryInValueArray;
				}
				contentItem = contentItemFactory.makePersonNameContentItem(
																		   null /* parent will be set later by addChild() operation */,
																		   relationshipType,
																		   concept,
																		   value/*,
																				 observationDateTime,observationUID*/);
			}
			else if (valueType.equals("UIDREF")) {
				String value = getSingleStringValueOrNullFromJsonContentItemValue(contentItemValue,valueType,concept);
				if (haveChildrenForSingleStringJsonContentItemValue(contentItemValue)) {
					contentItemChildren = lastEntryInValueArray;
				}
				contentItem = contentItemFactory.makeUIDContentItem(
																	null /* parent will be set later by addChild() operation */,
																	relationshipType,
																	concept,
																	value/*,
																		  observationDateTime,observationUID*/);
			}
			else if (valueType.equals("TEXT")) {
				String value = getSingleStringValueOrNullFromJsonContentItemValue(contentItemValue,valueType,concept);
				if (haveChildrenForSingleStringJsonContentItemValue(contentItemValue)) {
					contentItemChildren = lastEntryInValueArray;
				}
				contentItem = contentItemFactory.makeTextContentItem(
																	 null /* parent will be set later by addChild() operation */,
																	 relationshipType,
																	 concept,
																	 value/*,
																		   observationDateTime,observationUID*/);
			}
			else if (valueType.equals("SCOORD")) {
				String graphicType = null;
				float[] graphicData = null;
				if (contentItemValue != null && contentItemValue.getValueType() == JsonValue.ValueType.ARRAY) {
					JsonArray contentItemValueAndChildrenArray = (JsonArray)contentItemValue;
					if (contentItemValueAndChildrenArray.size() > 1) {
						graphicType = getStringFromSelectedContentItemValue(contentItemValueAndChildrenArray,0,"graphicType",valueType,concept);
						{
							JsonValue secondEntryCandidate = contentItemValueAndChildrenArray.get(1);
							if (secondEntryCandidate != null && secondEntryCandidate.getValueType() == JsonValue.ValueType.ARRAY) {
								JsonArray graphicDataArray = (JsonArray)secondEntryCandidate;
								int n = graphicDataArray.size();
								if (n > 0) {
									graphicData = new float[n];
									for (int i=0; i<n; ++i) {
										JsonValue arrayValue = graphicDataArray.get(i);
										if (arrayValue != null && arrayValue.getValueType() == JsonValue.ValueType.NUMBER) {
											graphicData[i] = (float)(((JsonNumber)arrayValue).doubleValue());
										}
										else {
											slf4jlogger.error("Missing graphicData array value type {} in SCOORD content item for concept {} ",arrayValue.getValueType(),concept);
										}
									}
								}
							}
							else {
								slf4jlogger.error("Missing graphicData array in SCOORD content item for concept {}",concept);
							}
						}
						// check we have children
						if (contentItemValueAndChildrenArray.size() > 2) {
							contentItemChildren = lastEntryInValueArray;
						}
					}
					else {
						slf4jlogger.error("No value and units in SCOORD content item for concept {} ",concept);
					}
				}
				else {
					slf4jlogger.error("No array of value +/- children in SCOORD content item for concept {} ",concept);
				}
				contentItem = contentItemFactory.makeSpatialCoordinatesContentItem(
					null /* parent will be set later by addChild() operation */,
					relationshipType,
					concept,
					graphicType,
					graphicData/*,
					observationDateTime,observationUID*/);
			}
			else if (valueType.equals("IMAGE")) {
				String referencedSOPClassUID = null;
				String referencedSOPInstanceUID = null;
				if (contentItemValue != null && contentItemValue.getValueType() == JsonValue.ValueType.ARRAY) {
					JsonArray contentItemValueAndChildrenArray = (JsonArray)contentItemValue;
					if (contentItemValueAndChildrenArray.size() > 1) {
						referencedSOPClassUID = getStringFromSelectedContentItemValue(contentItemValueAndChildrenArray,0,"SOP Class",valueType,concept);
						referencedSOPInstanceUID = getStringFromSelectedContentItemValue(contentItemValueAndChildrenArray,1,"SOP Instance",valueType,concept);

						// check we have children - need to improve this if we do the other paramaters like frame, # etc.
						if (contentItemValueAndChildrenArray.size() > 2) {
							contentItemChildren = lastEntryInValueArray;
						}
					}
					else {
						slf4jlogger.error("No SOP Class and SOP Instance in IMAGE content item for concept {} ",concept);
					}
				}
				else {
					slf4jlogger.error("No array of SOP Class and SOP Instance +/- children in IMAGE content item for concept {} ",concept);
				}

				contentItem = contentItemFactory.makeImageContentItem(
						null /* parent will be set later by addChild() operation */,
						relationshipType,
						concept,
						referencedSOPClassUID,
						referencedSOPInstanceUID,
						0/*referencedFrameNumber*/,
						0/*referencedSegmentNumber*/,
						null/*presentationStateSOPClassUID*/,null/*presentationStateSOPInstanceUID*/,
						null/*realWorldValueMappingSOPClassUID*/,null/*realWorldValueMappingSOPInstanceUID*//*,
						observationDateTime,observationUID*/);
			}
			else {
				// unrecognized content item valueType ... so what ?
				slf4jlogger.error("Unrecognized value type {} for concept {}",valueType,concept);
			}
			
			if (contentItemChildren != null && contentItemChildren.size() > 0) {
				int n = contentItemChildren.size();
				for (int i=0; i<n; ++i) {
					slf4jlogger.debug("getContentItemAndChildrenFromJSONObjectValue(): Processing child # {}",i);
					JsonValue arrayValue = contentItemChildren.get(i);
					if (arrayValue != null && arrayValue.getValueType() == JsonValue.ValueType.OBJECT) {
						JsonObject contentItemChild = (JsonObject)arrayValue;
						Set<String> childBusinessNames = contentItemChild.keySet();
						if (childBusinessNames.size() == 1) {
							String childBusinessName = childBusinessNames.iterator().next();
							slf4jlogger.debug("getContentItemAndChildrenFromJSONObjectValue(): JSON child businessName = {}",childBusinessName);
							if (childBusinessName != null) {
								CodedSequenceItem childBusinessNameCode = businessNames.get(childBusinessName);
								JsonValue childObjectValue = contentItemChild.get(childBusinessName);
								String childValueType = null;
								{
									Set<String> childValueTypes = valueTypesByBusinessName.get(childBusinessName);
									if (childValueTypes != null && childValueTypes.size() > 0) {
										if (childValueTypes.size() == 1) {
											childValueType = childValueTypes.iterator().next();
										}
										else {
											childValueType = childValueTypes.iterator().next();
											slf4jlogger.error("Ambiguous choice of value types for {} - using the first one {}",childBusinessName,childValueType);
										}
									}
								}
								slf4jlogger.debug("getContentItemAndChildrenFromJSONObjectValue(): childValueType from lookup {} for {}",childValueType,childBusinessName);
								
								if (childValueType == null) {
									childValueType = determineUnknownValueType(valueType,childObjectValue);
									slf4jlogger.debug("getContentItemAndChildrenFromJSONObjectValue(): childValueType determined to be {} for {}",childValueType,childBusinessName);
								}
								
								String childRelationshipType = null;
								{
									Set<String> childRelationshipTypes = relationshipTypesByBusinessName.get(childBusinessName);
									if (childRelationshipTypes != null && childRelationshipTypes.size() > 0) {
										if (childRelationshipTypes.size() == 1) {
											childRelationshipType = childRelationshipTypes.iterator().next();
										}
										else {
											childRelationshipType = childRelationshipTypes.iterator().next();
											slf4jlogger.error("Ambiguous choice of relationship types for {} - using the first one {}",childBusinessName,childRelationshipType);
										}
									}
								}
								slf4jlogger.debug("getContentItemAndChildrenFromJSONObjectValue(): childRelationshipType from lookup {} for business name {}",childRelationshipType,childBusinessName);
								
								if (childRelationshipType == null) {
									childRelationshipType = determineUnknownRelationshipType(valueType,childValueType,childObjectValue);
									slf4jlogger.debug("getContentItemAndChildrenFromJSONObjectValue(): childRelationshipType determined to be {} for {}",childRelationshipType,childBusinessName);
								}
								
								if (childValueType != null && childRelationshipType != null) {
									ContentItem childContentItem = getContentItemAndChildrenFromJSONObjectValue(childBusinessName,childBusinessNameCode,childValueType,childRelationshipType,childObjectValue);	// recurses ... will convert all its children now too
									if (childContentItem != null) {
										contentItem.addChild(childContentItem);
									}
								}
								else {
									slf4jlogger.error("Missing value type {} or relationship type {} for {}",childValueType,childRelationshipType,childBusinessName);
								}
							}
							else {
								slf4jlogger.debug("getContentItemAndChildrenFromJSONObjectValue(): Ignoring anonymous business name for now");	// can this even happen ?? :(
							}
						}
						else {
							slf4jlogger.error("Expected only one entry for child object # {} in array of children",i);
						}
					}
				}
			}
		}
		// else don't have a concept name and value/children ... so what ? :(
		return contentItem;
	}
	
	/**
	 * <p>Given a DICOM SR object encoded in a JSON document
	 * convert it to a StructuredReport using the content tree and ignoring any header attributes.</p>
	 *
	 * @param		topLevelObject	the first object of the array that is the JSON document
	 * @return						the StructuredReport
	 * @throws	DicomException
	 */
	public StructuredReport getStructuredReport(JsonObject topLevelObject) throws DicomException {
		DicomDictionary dictionary = DicomDictionary.StandardDictionary;
		StructuredReport structuredReport = null;
		try {
			if (topLevelObject != null) {
				slf4jlogger.debug("Looking for SR container entry in top level object amongst all the DICOM keywords");
				String rootContainerBusinessName = null;
				CodedSequenceItem rootContainerBusinessNameCode = null;
				// a JsonObject is a Map<String,JsonValue>, so iterate through map entry keys
				for (String businessName : topLevelObject.keySet()) {
					slf4jlogger.debug("JSON businessName = {}",businessName);
					if (businessName != null) {
						// we are at the top level, so DICOM standard keywords override any coded business names, since may be duplicates, e.g., (111060,DCM,"Study Date")
						if (dictionary.getTagFromName(businessName) == null) {
							CodedSequenceItem businessNameCode = businessNames.get(businessName);
							if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("getStructuredReport(): businessName {} is {}",businessName,businessNameCode);
							if (businessNameCode != null) {
								if (rootContainerBusinessNameCode == null) {
									rootContainerBusinessName = businessName;
									rootContainerBusinessNameCode = businessNameCode;
								}
								else {
									throw new DicomException("Could not parse JSON document - more than one potential root content item business name");
								}
							}
							else {
								throw new DicomException("Could not parse JSON document - unrecognized business name {} that is neither a DICOM data element keyword nor in the supplied business name dictionary");
							}
						}
						else {
							slf4jlogger.debug("getStructuredReport(): Ignoring businessName {} that is a DICOM keyword",businessName);
						}
					}
					else {
						throw new DicomException("Could not parse JSON document - missing business name");
					}
				}
				// if we get here, we found one and only one coded business name to use as rootContainerBusinessNameCode
				JsonValue topLevelObjectValue = topLevelObject.get(rootContainerBusinessName);
				if (topLevelObjectValue != null) {
					contentItemFactory = new ContentItemFactory();
					ContentItem root = getContentItemAndChildrenFromJSONObjectValue(rootContainerBusinessName,rootContainerBusinessNameCode,"CONTAINER",null/*relationshipType*/,topLevelObjectValue);	// processes entire tree
					structuredReport = new StructuredReport(root);
				}
				else {
					throw new DicomException("Could not parse JSON document - missing top level object value");
				}
			}
			else {
				throw new DicomException("Could not parse JSON document - missing top level object");
			}
		}
		catch (IndexOutOfBoundsException e) {
			throw new DicomException("Could not parse JSON document - exactly one object in top level array expected "+e);
		}
		catch (ClassCastException e) {
			throw new DicomException("Could not parse JSON document - expected object in top level array "+e);
		}
//System.err.println("JSONRepresentationOfStructuredReportObjectFactory.getStructuredReport(JsonObject topLevelObject): structuredReport is "+structuredReport);
		return structuredReport;
	}

	
	/**
	 * <p>Given a DICOM SR object encoded as a JSON document
	 * convert it to a StructuredReport using the content tree and ignoring any header attributes.</p>
	 *
	 * @param		document		the JSON document
	 * @return						the StructuredReport
	 * @throws	DicomException
	 */
	public StructuredReport getStructuredReport(JsonArray document) throws DicomException {
		StructuredReport structuredReport = null;
		try {
			JsonObject topLevelObject = document.getJsonObject(0);
			structuredReport = getStructuredReport(topLevelObject);
		}
		catch (IndexOutOfBoundsException e) {
			throw new DicomException("Could not parse JSON document - exactly one object in top level array expected "+e);
		}
		catch (ClassCastException e) {
			throw new DicomException("Could not parse JSON document - expected object in top level array "+e);
		}
//System.err.println("JSONRepresentationOfStructuredReportObjectFactory.getStructuredReport(JsonArray document): structuredReport is "+structuredReport);
		return structuredReport;
	}
	
	/**
	 * <p>Given a DICOM SR object encoded in a JSON document
	 * convert it to a list of attributes.</p>
	 *
	 * @param		topLevelObject	the first object of the array that is the JSON document
	 * @return						the list of DICOM attributes
	 * @throws	DicomException
	 */
	public AttributeList getAttributeList(JsonObject topLevelObject) throws DicomException {
		AttributeList list = null;
		list = new JSONRepresentationOfDicomObjectFactory().getAttributeList(topLevelObject,true/*ignoreUnrecognizedTags*/,true/*ignoreSR*/);
		{
			StructuredReport structuredReport = getStructuredReport(topLevelObject);
			AttributeList structuredReportList = structuredReport.getAttributeList();
			if (structuredReportList != null) {
				list.putAll(structuredReportList);
			}
			// else wasn't an SR so ignore it
		}
//System.err.println("JSONRepresentationOfStructuredReportObjectFactory.getAttributeList(JsonObject topLevelObject): List is "+list);
		return list;
	}

	/**
	 * <p>Given a DICOM SR object encoded as a JSON document
	 * convert it to a list of attributes.</p>
	 *
	 * @param		document		the JSON document
	 * @return						the list of DICOM attributes
	 * @throws	DicomException
	 */
	public AttributeList getAttributeList(JsonArray document) throws DicomException {
		AttributeList list = null;
		try {
			JsonObject topLevelObject = document.getJsonObject(0);
			list = getAttributeList(topLevelObject);
		}
		catch (IndexOutOfBoundsException e) {
			throw new DicomException("Could not parse JSON document - exactly one object in top level array expected "+e);
		}
		catch (ClassCastException e) {
			throw new DicomException("Could not parse JSON document - expected object in top level array "+e);
		}
//System.err.println("JSONRepresentationOfStructuredReportObjectFactory.getAttributeList(JsonArray document): List is "+list);
		return list;
	}
	
	/**
	 * <p>Given a DICOM SR object encoded as a JSON document in a stream
	 * convert it to a list of attributes.</p>
	 *
	 * @param		stream			the input stream containing the JSON document
	 * @return						the list of DICOM attributes
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public AttributeList getAttributeList(InputStream stream) throws IOException, DicomException {
		JsonReader jsonReader = Json.createReader(stream);
		JsonArray document = jsonReader.readArray();
		jsonReader.close();
		return getAttributeList(document);
	}
	
	/**
	 * <p>Given a DICOM SR object encoded as a JSON document in a file
	 * convert it to a list of attributes.</p>
	 *
	 * @param		file			the input file containing the JSON document
	 * @return						the list of DICOM attributes
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public AttributeList getAttributeList(File file) throws IOException, DicomException {
		InputStream fi = new FileInputStream(file);
		BufferedInputStream bi = new BufferedInputStream(fi);
		AttributeList list = null;
		try {
			list = getAttributeList(bi);
		}
		finally {
			bi.close();
			fi.close();
		}
		return list;
	}
	
	/**
	 * <p>Given a DICOM SR object encoded as a JSON document in a named file
	 * convert it to a list of attributes.</p>
	 *
	 * @param		name			the input file containing the JSON document
	 * @return						the list of DICOM attributes
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public AttributeList getAttributeList(String name) throws IOException, DicomException {
		return getAttributeList(new File(name));
	}

	/**
	 * <p>Serialize a JSON document.</p>
	 *
	 * @param	out		the output stream to write to
	 * @param	document	the JSON document
	 * @throws	IOException
	 */
	public static void write(OutputStream out,JsonArray document) throws IOException {
		JsonWriter writer = Json.createWriterFactory(null/*config*/).createWriter(out);	// charset is UTF-8
		writer.writeArray(document);
		writer.close();
	}

	/**
	 * <p>Serialize a JSON document.</p>
	 *
	 * @param	outputFile	the output file to write to
	 * @param	document	the JSON document
	 * @throws	IOException
	 */
	public static void write(File outputFile,JsonArray document) throws IOException {
		OutputStream out = new FileOutputStream(outputFile);
		write(out,document);
		out.close();
	}

	/**
	 * <p>Serialize a JSON document.</p>
	 *
	 * @param	outputPath	the output path to write to
	 * @param	document	the JSON document
	 * @throws	IOException
	 */
	public static void write(String outputPath,JsonArray document) throws IOException {
		write(new File(outputPath),document);
	}

	/**
	 * <p>Serialize a JSON document created from a DICOM Structured Report.</p>
	 *
	 * @param	list	the attribute list
	 * @param	out		the output stream to write to
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public static void createDocumentAndWriteIt(AttributeList list,OutputStream out) throws IOException, DicomException {
		createDocumentAndWriteIt(null,list,out);
	}
	
	/**
	 * <p>Serialize a JSON document created from a DICOM Structured Report.</p>
	 *
	 * @param	sr		the Structured Report
	 * @param	out		the output stream to write to
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public static void createDocumentAndWriteIt(StructuredReport sr,OutputStream out) throws IOException, DicomException {
		createDocumentAndWriteIt(sr,null,out);
	}
	
	/**
	 * <p>Serialize a JSON document created from a DICOM attribute list.</p>
	 *
	 * @param	sr		the Structured Report - may be null if list is not - will build an sr tree model
	 * @param	list	the list of DICOM attributes
	 * @param	out		the output stream to write to
	 * @throws	DicomException
	 */
	public static void createDocumentAndWriteIt(StructuredReport sr,AttributeList list,OutputStream out) throws DicomException {
		try {
			JsonArray document = new JSONRepresentationOfStructuredReportObjectFactory().getDocument(sr,list);
			write(out,document);
		}
		catch (Exception e) {
			throw new DicomException("Could not create JSON document - could not transform to JSON "+e);
		}
	}

	/**
	 * <p>Serialize a JSON document created from a DICOM attribute list.</p>
	 *
	 * @param	sr				the Structured Report - may be null if list is not - will build an sr tree model
	 * @param	list			the list of DICOM attributes
	 * @param	outputFile		the output file to write to
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public static void createDocumentAndWriteIt(StructuredReport sr,AttributeList list,File outputFile) throws IOException, DicomException {
		OutputStream out = new FileOutputStream(outputFile);
		createDocumentAndWriteIt(sr,list,out);
		out.close();
	}

	/**
	 * <p>Serialize a JSON document created from a DICOM attribute list.</p>
	 *
	 * @param	sr			the Structured Report - may be null if list is not - will build an sr tree model
	 * @param	list		the list of DICOM attributes
	 * @param	outputPath	the output path to write to
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public static void createDocumentAndWriteIt(StructuredReport sr,AttributeList list,String outputPath) throws IOException, DicomException {
		createDocumentAndWriteIt(sr,list,new File(outputPath));
	}

	/**
	 * <p>Read a DICOM dataset (that contains a structured report) and write a JSON representation of it to the standard output or specified path, or vice versa.</p>
	 *
	 * @param	arg	either one input path of the file containing the DICOM/JSON dataset and a business name file path to read or write, or a direction argument (toDICOM or toJSON, case insensitive) and an input path and an input or output business name path, and optionally an output path
	 */
	public static void main(String arg[]) {
		try {
			boolean toJSON = true;
			
			String inputPath = null;
			String businessNamesPath = null;
			String outputPath = null;
			
			//boolean useKeywordInsteadOfTag = false;

			int numberOfFixedArguments = 2;
			int numberOfFixedAndOptionalArguments = 4;
			int endOptionsPosition = arg.length;
			
			boolean bad = false;
			
			if (endOptionsPosition < numberOfFixedArguments) {
				bad = true;
			}
			boolean keepLooking = true;
			while (keepLooking && endOptionsPosition > numberOfFixedArguments) {
				String option = arg[endOptionsPosition-1].trim().toUpperCase();
				switch (option) {
					//case "USEKEYWORD":			useKeywordInsteadOfTag = true; --endOptionsPosition; break;
					//case "USETAG":				useKeywordInsteadOfTag = false; --endOptionsPosition; break;
					
					default:	if (endOptionsPosition > numberOfFixedAndOptionalArguments) {
									slf4jlogger.error("Unrecognized argument {}",option);
									bad = true;
								}
								keepLooking = false;
								break;
				}
			}

			if (!bad) {
				if (endOptionsPosition == 2) {
					toJSON = true;
					inputPath = arg[0];
					businessNamesPath = arg[1];
				}
				else if (endOptionsPosition == 3) {
					if (arg[0].toLowerCase(Locale.US).equals("tojson")) {
						inputPath = arg[1];
						businessNamesPath = arg[2];
						toJSON = true;
					}
					else if (arg[0].toLowerCase(Locale.US).equals("todicom") || arg[0].toLowerCase(Locale.US).equals("todcm")) {
						inputPath = arg[1];
						businessNamesPath = arg[2];
						bad = false;
						toJSON = false;
					}
					else {
						inputPath = arg[0];
						businessNamesPath = arg[1];
						outputPath = arg[2];
						toJSON = true;
					}
				}
				else if (endOptionsPosition == 4) {
					if (arg[0].toLowerCase(Locale.US).equals("tojson")) {
						inputPath = arg[1];
						businessNamesPath = arg[2];
						outputPath = arg[3];
						toJSON = true;
					}
					else if (arg[0].toLowerCase(Locale.US).equals("todicom") || arg[0].toLowerCase(Locale.US).equals("todcm")) {
						inputPath = arg[1];
						businessNamesPath = arg[2];
						outputPath = arg[3];
						toJSON = false;
					}
					else {
						bad = true;
					}
				}
				else {
					bad = true;
				}
			}
			
			//if (!toJSON && (useKeywordInsteadOfTag || addTag || addKeyword)) {
			//	System.err.println("Unexpected options specified for conversion to DICOM that are only applicable to conversion to JSON");
			//	bad = true;
			//}
			
			if (bad) {
				System.err.println("usage: JSONRepresentationOfDicomObjectFactory [toJSON] inputpath businessnamespath [outputpath]"
					//+" [USEKEYWORD|USETAG]"
				);
				//System.err.println("usage: JSONRepresentationOfDicomObjectFactory toDICOM inputpath businessnamespath [outputpath]");
				System.exit(1);
			}
			else {
				if (toJSON) {
					JSONRepresentationOfStructuredReportObjectFactory j = new JSONRepresentationOfStructuredReportObjectFactory();
					{
						JsonArray document = j.getDocument(inputPath);
						//System.err.println(toString(document));
						if (outputPath == null) {
							write(System.out,document);
						}
						else {
							write(outputPath,document);
						}
					}
					if (businessNamesPath != null) {
						JsonArray businessNamesDocument = j.getBusinessNamesDocument();
						write(businessNamesPath,businessNamesDocument);
					}
				}
				else {
					JSONRepresentationOfStructuredReportObjectFactory j = new JSONRepresentationOfStructuredReportObjectFactory();
					j.loadBusinessNamesDocument(businessNamesPath);
					AttributeList list = j.getAttributeList(inputPath);
					
					//System.err.println(list);
					
					String sourceApplicationEntityTitle = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SourceApplicationEntityTitle);
					list.insertSuitableSpecificCharacterSetForAllStringValues();	// (001158)
					list.removeMetaInformationHeaderAttributes();
					FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,sourceApplicationEntityTitle);
					if (outputPath == null) {
						list.write(System.out,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
					}
					else {
						list.write(outputPath,TransferSyntax.ExplicitVRLittleEndian,true/*useMeta*/,true/*useBufferedStream*/);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);	// no need to use SLF4J since command line utility/test
		}
	}
}

