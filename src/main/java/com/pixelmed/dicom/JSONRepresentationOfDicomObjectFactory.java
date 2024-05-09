/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import com.pixelmed.utils.StringUtilities;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.Base64;
import java.util.Iterator;
import java.util.Vector;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * <p>A class to encode a representation of a DICOM object in a JSON form, and to convert it back again.</p>
 *
 * <p>There are a number of characteristics of this form of output:</p>
 *
 * <p>Note that a round trip from DICOM to JSON and back again does not
 * always result in full fidelity, since:</p>
 *
 * <ul>
 * <li>Binary floating point values will lose precision when converted to string representation and back again</li>
 * <li>Leading and trailing white space and control characters in strings will be discarded</li>
 * <li>Meta information header elements will be changed</li>
 * <li>Structural elements such as group lengths will be removed and may or may not be replaced</li>
 * <li>Physical offsets such as in the DICOMDIR will be invalidated</li>
 * <li>Large attributes with OB and OW value representations will have their values discarded so as not to encode the bulk pixel data (probably should be added as an option)</li>
 * </ul>
 *
 * <p>A typical example of how to invoke this class to convert DICOM to JSON would be:</p>
 * <pre>
try {
    AttributeList list = new AttributeList();
    list.read("dicomfile",null,true,true);
    JsonArray document = new JSONRepresentationOfDicomObjectFactory().getDocument(list);
    JSONRepresentationOfDicomObjectFactory.write(System.out,document);
} catch (Exception e) {
    slf4jlogger.error("",e);
 }
 * </pre>
 *
 * <p>or even simpler, if there is no further use for the JSON document:</p>
 * <pre>
try {
    AttributeList list = new AttributeList();
    list.read("dicomfile",null,true,true);
    JSONRepresentationOfDicomObjectFactory.createDocumentAndWriteIt(list,System.out);
} catch (Exception e) {
    slf4jlogger.error("",e);
 }
 * </pre>
 *
 * <p>A typical example of converting JSON back to DICOM would be:</p>
 * <pre>
try {
    AttributeList list = new JSONRepresentationOfDicomObjectFactory().getAttributeList("jsonfile");
    list.insertSuitableSpecificCharacterSetForAllStringValues();
    list.write(System.out,TransferSyntax.ExplicitVRLittleEndian,true,true);
} catch (Exception e) {
    slf4jlogger.error("",e);
 }
 * </pre>
 *
 * <p>or if you need to handle the meta information properly:</p>
 * <pre>
try {
    AttributeList list = new JSONRepresentationOfDicomObjectFactory().getAttributeList("jsonfile");
    list.insertSuitableSpecificCharacterSetForAllStringValues();
    String sourceApplicationEntityTitle = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.SourceApplicationEntityTitle);
    list.removeMetaInformationHeaderAttributes();
    FileMetaInformation.addFileMetaInformation(list,TransferSyntax.ExplicitVRLittleEndian,sourceApplicationEntityTitle);
    list.write(System.out,TransferSyntax.ExplicitVRLittleEndian,true,true);
} catch (Exception e) {
    slf4jlogger.error("",e);
 }
 * </pre>
 *
 * <p>When the JSON is being converted to DICOM, the group, element and VR attributes are not needed if the element name is a keyword that can be found in
 * the dictionary; if they are present, then their values are checked against the dictionary values.</p>
 *
 * @author	dclunie
 */
public class JSONRepresentationOfDicomObjectFactory {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/JSONRepresentationOfDicomObjectFactory.java,v 1.25 2022/01/21 19:51:16 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(JSONRepresentationOfDicomObjectFactory.class);

	protected static String reservedKeywordForPersonNameAlphabeticPropertyInJsonRepresentation = "Alphabetic";
	protected static String reservedKeywordForPersonNameIdeographicPropertyInJsonRepresentation = "Ideographic";
	protected static String reservedKeywordForPersonNamePhoneticPropertyInJsonRepresentation = "Phonetic";

	private JsonBuilderFactory factory;
	
	protected static String substituteKeywordForUIDIfPossible(String value) {
		String keyword = null;
		if (value != null && value.length() > 0) {
			keyword = SOPClassDescriptions.getKeywordFromUID(value);	// returns empty string rather than null if not found
		}
		return keyword != null && keyword.length() > 0 ? keyword : value;
	}
	
	protected static String substituteKeywordForUIDIfPossibleAndRequested(String value,boolean substitute) {
		String keyword = null;
		if (substitute) {
			keyword = substituteKeywordForUIDIfPossible(value);
		}
		return keyword != null && keyword.length() > 0 ? keyword : value;
	}

	protected static String substituteKeywordForUIDIfPossibleAndAppropriateForVRAndRequested(String value, byte[] vr,boolean substitute) {
		String keyword = null;
		if (ValueRepresentation.isUniqueIdentifierVR(vr)) {
			keyword = substituteKeywordForUIDIfPossibleAndRequested(value,substitute);
		}
		return keyword != null && keyword.length() > 0 ? keyword : value;
	}
	
	protected static String substituteUIDForKeywordIfPossible(String value) {
		String uid = null;
		if (value != null && value.length() > 0) {
			uid = SOPClassDescriptions.getUIDFromKeyword(value);	// returns empty string rather than null if not found
		}
		return uid != null && uid.length() > 0 ? uid : value;
	}
	
	protected static String substituteUIDForKeywordIfPossibleAndAppropriateForVR(String value,byte[] vr) {
		String uid = null;
		if (ValueRepresentation.isUniqueIdentifierVR(vr)) {
			uid = substituteUIDForKeywordIfPossible(value);
		}
		return uid != null && uid.length() > 0 ? uid : value;
	}

	/**
	 * @param	tag
	 */
	private String makeElementNameFromHexadecimalGroupElementValues(AttributeTag tag) {
		StringBuffer str = new StringBuffer();
		String groupString = Integer.toHexString(tag.getGroup());
		for (int i=groupString.length(); i<4; ++i) str.append("0");
		str.append(groupString);
		String elementString = Integer.toHexString(tag.getElement());
		for (int i=elementString.length(); i<4; ++i) str.append("0");
		str.append(elementString);
		return str.toString();
	}

	
	/**
	 * <p>Parse an AttributeTag represented as an eight character hexadecimal representation as defined for the standard JSON or XML representation.</p>
	 *
	 * <p>Hex digits may be upper or lower case (though tthe standard requires upper only).</p>
	 *
	 * @return	AttributeTag or null if not in expected format
	 */
	protected static final AttributeTag getAttributeTagFromHexadecimalGroupElementValues(String s) {
		slf4jlogger.trace("getAttributeTagFromHexadecimalGroupElementValues(): string = {}",s);
		AttributeTag tag = null;
		if (s != null && s.length() == 8) {
			try {
				int group = Integer.parseInt(s.substring(0,4),16);
				slf4jlogger.trace("getAttributeTagFromHexadecimalGroupElementValues(): group = {}",group);
				int element = Integer.parseInt(s.substring(4,8),16);
				slf4jlogger.trace("getAttributeTagFromHexadecimalGroupElementValues(): element = {}",element);
				tag = new AttributeTag(group,element);
			}
			catch (NumberFormatException e) {
				// fall through to return null since not in expected format
				slf4jlogger.trace("getAttributeTagFromHexadecimalGroupElementValues(): ",e);
			}
		}
		return tag;
	}

	public static String getJsonPersonNameFromPropertiesInJsonObject(JSONObject jsonObjectValue, String alphabeticProperty, String ideographicProperty, String phoneticProperty) {
		StringBuffer buf = new StringBuffer();
		String alphabetic = "";
		String ideographic = "";
		String phonetic = "";
		{
			try {
				alphabetic = jsonObjectValue.getString(alphabeticProperty);
			}
			catch (NullPointerException e) {
				alphabetic = "";
			}
		}
		{
			try {
				ideographic = jsonObjectValue.getString(ideographicProperty);
			}
			catch (NullPointerException e) {
				ideographic = "";
			}
		}
		{
			try {
				phonetic = jsonObjectValue.getString(phoneticProperty);
			}
			catch (NullPointerException e) {
				phonetic = "";
			}
		}
		if (alphabetic.length() > 0 || ideographic.length() > 0 || phonetic.length() > 0) {
			buf.append(alphabetic);
			if (ideographic.length() > 0 || phonetic.length() > 0) {	// avoid trailing delimiters for empty groups
				buf.append("=");
				buf.append(ideographic);
			}
			if (phonetic.length() > 0) {								// avoid trailing delimiters for empty groups
				buf.append("=");
				buf.append(phonetic);
			}
		}
		// else empty
		return buf.toString();
	}

	/**
	 * @param	list
	 * @param	parent
	 * @param	ignoreUnrecognizedTags	whether or not to ignore unrecognized tags (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @param	ignoreSR				whether or not to ignore SR Content Items (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @throws	DicomException
	 */
	void addAttributesFromJsonObjectToList(AttributeList list,JSONObject parent,boolean ignoreUnrecognizedTags,boolean ignoreSR) throws DicomException {
		DicomDictionary dictionary = list.getDictionary();
		if (parent != null) {
			// a JsonObject is a Map<String,JsonValue>, so iterate through map entry keys
			for (String elementName : parent.keySet()) {
				slf4jlogger.debug("JSON elementName = {}",elementName);
				String jsonSingleValue = null;
				JSONObject jsonAttributeVRAndValue = null;
				{
					JsonValue jsonAttributeVRAndValueAsJsonValue = parent.get(elementName);
					if (jsonAttributeVRAndValueAsJsonValue != null) {
						if (jsonAttributeVRAndValueAsJsonValue.getValueType() == JsonValue.ValueType.OBJECT) {
							jsonAttributeVRAndValue = (JsonObject)jsonAttributeVRAndValueAsJsonValue;
						}
						else if (jsonAttributeVRAndValueAsJsonValue.getValueType() == JsonValue.ValueType.STRING) {
							jsonSingleValue = ((JsonString)jsonAttributeVRAndValueAsJsonValue).getString();
							slf4jlogger.debug("JsonValue.ValueType.STRING jsonSingleValue = {}",jsonSingleValue);
						}
						else if (jsonAttributeVRAndValueAsJsonValue.getValueType() == JsonValue.ValueType.NUMBER) {
							jsonSingleValue = ((JsonNumber)jsonAttributeVRAndValueAsJsonValue).toString();	// could mess with specific types but hope that BigDecimal.toString() always works
							if (jsonSingleValue == null) {
								jsonSingleValue = "";
							}
							if (jsonSingleValue.endsWith(".0")) {	// want to remove this or round trip will not match since FloatFormatter does not add this
								jsonSingleValue =  jsonSingleValue.substring(0,jsonSingleValue.length()-2);
							}
							slf4jlogger.debug("JsonValue.ValueType.NUMBER jsonSingleValue = {}",jsonSingleValue);
						}
						// else ignore, as may be other content like SR, which may use JsonArray rather than JsonObject
					}
					// else ignore ... missing vr and value will be handled later if necessary
				}
				byte[] vr = null;
				String vrString = null;
				AttributeTag tag = getAttributeTagFromHexadecimalGroupElementValues(elementName);
				slf4jlogger.debug("Tag from parsing JSON elementName = {}",tag);
				if (tag == null) {
					tag = dictionary.getTagFromName(elementName);
					slf4jlogger.debug("Tag from looking up JSON elementName in dictionary = {}",tag);
				}
				if (tag != null) {
					vr = dictionary.getValueRepresentationFromTag(tag);
					vrString = ValueRepresentation.getAsString(vr);
				}
				
				boolean addIt = true;
				
				if (ignoreSR && tag != null && (
				   	tag.equals(TagFromName.ContentSequence)
				 || tag.equals(TagFromName.ValueType)
				 || tag.equals(TagFromName.ConceptNameCodeSequence)
				 || tag.equals(TagFromName.ContinuityOfContent)
				 || tag.equals(TagFromName.ContentTemplateSequence)
				 || tag.equals(TagFromName.MappingResource)
				 || tag.equals(TagFromName.TemplateIdentifier)
			   )) {
					slf4jlogger.debug("Ignoring SR-related tag");
					addIt = false;
				}
				
				if (addIt) {
					if (tag != null) {
						{
							String explicitVRString = jsonAttributeVRAndValue ==  null ? "" : jsonAttributeVRAndValue.getString("vr","");	// set default of null in case missing, which is permitted
							byte[] explicitVR = explicitVRString.getBytes();
							if (vr == null) {
								vr = explicitVR;	// may still be null of not present in JSON
							}
							else if (!vrString.equals(explicitVRString)) {
								if (ValueRepresentation.isUnspecifiedShortVR(vr)) {
									if (ValueRepresentation.isUnsignedShortVR(explicitVR) || ValueRepresentation.isSignedShortVR(explicitVR)) {
										vr = explicitVR;
									}
									// else wrong or empty
								}
								else if (ValueRepresentation.isOtherUnspecifiedVR(vr)) {
									if (ValueRepresentation.isOtherByteOrWordVR(explicitVR)) {
										vr = explicitVR;
									}
									// else wrong or empty
								}
								else if (explicitVRString.length() > 0) {
									throw new DicomException("Dictionary VR <"+vrString+"> does not match VR in attribute <"+explicitVRString+"> of element "+elementName);
								}
								// else was empty so assume dictionary VR is correct
							}
							// else same so ignore
						}
						if (vr != null && vr.length == 2) {
							int group = tag.getGroup();
							int element = tag.getElement();
							if ((group%2 == 0 && element == 0) || (group == 0x0008 && element == 0x0001) || (group == 0xfffc && element == 0xfffc)) {
								//System.err.println("ignoring group length or length to end or dataset trailing padding "+tag);
							}
							else {
								// jsonSingleValue will already have been found and set by now, instead of jsonAttributeVRAndValue
								JSONArray values = null;
								String inLineBinary = null;
								if (jsonAttributeVRAndValue != null) {
									try {
										values = jsonAttributeVRAndValue.getJSONArray("Value");
									}
									catch (ClassCastException e) {
										throw new DicomException("Expected array values for tag "+elementName);
									}
									catch (NullPointerException e) {
										// OK to be missing
									}

									try {
										inLineBinary = jsonAttributeVRAndValue.getString("InlineBinary");
									}
									catch (ClassCastException e) {
										throw new DicomException("Expected string Base64 value for tag "+elementName);
									}
									catch (NullPointerException e) {
										// OK to be missing
									}
								}
								// else OK to be missing
								// if (values == null && inLineBinary == null) then no problem, just means empty zero length attribute
								
								if (ValueRepresentation.isSequenceVR(vr)) {
									SequenceAttribute a = new SequenceAttribute(tag);
									//System.err.println("Created "+a);
									if (values != null && values.length() > 0) {
										for (int i=0; i<values.length(); ++i) {
											JSONObject childJsonObject = null;
											try {
												childJsonObject = values.getJSONObject(i);
											}
											catch (ClassCastException e) {
												throw new DicomException("Expected object for sequence attribute "+elementName);
											}
											if (childJsonObject != null ) {
												//System.err.println("Adding item to sequence");
												AttributeList itemList = new AttributeList();
												addAttributesFromJsonObjectToList(itemList,childJsonObject,ignoreUnrecognizedTags,ignoreSR);
												a.addItem(itemList);
											}
										}
									}
									//System.err.println("Sequence Attribute is "+a);
									list.put(tag,a);
								}
								else {
									Attribute a = AttributeFactory.newAttribute(tag,vr);
									//System.err.println("Created "+a);
									if (jsonSingleValue != null) {
										// may have been single JsonValue.ValueType.STRING
										// may have been single JsonValue.ValueType.NUMBER - already handled conversion to string and removal of trailing ".0"
										String stringValue = StringUtilities.removeLeadingOrTrailingWhitespaceOrISOControl(jsonSingleValue);	// just in case ? need more eacape handling ? :(
										stringValue = substituteUIDForKeywordIfPossibleAndAppropriateForVR(stringValue,vr);
										a.addValue(stringValue);
									}
									else if (values != null && values.length() > 0) {
										// values is a List<JsonValue> so can use List.get method to get JsonValue and then switch on its type
										for (int i=0; i<values.length(); ++i) {
											JsonValue value = values.get(i);
											JsonValue.ValueType valueType = value.getValueType();
											if (valueType == JsonValue.ValueType.STRING) {
												String stringValue = ((JsonString)value).getString();
												slf4jlogger.debug("addAttributesFromJsonObjectToList(): String Value stringValue = {}",stringValue);
												if (stringValue == null) {
													stringValue = "";
												}
												stringValue = StringUtilities.removeLeadingOrTrailingWhitespaceOrISOControl(stringValue);	// just in case ? need more eacape handling ? :(
												stringValue = substituteUIDForKeywordIfPossibleAndAppropriateForVR(stringValue,vr);
												a.addValue(stringValue);
											}
											else if (valueType == JsonValue.ValueType.NUMBER) {
												String stringValue = ((JsonNumber)value).toString();	// could mess with specific types but hope that BigDecimal.toString() always works
												slf4jlogger.debug("addAttributesFromJsonObjectToList(): Number Value stringValue = {}",stringValue);
												//System.err.println("Value stringValue = "+stringValue);
												if (stringValue == null) {
													stringValue = "";
												}
												if (stringValue.endsWith(".0")) {	// want to remove this or round trip will not match since FloatFormatter does not add this
													stringValue =  stringValue.substring(0,stringValue.length()-2);
												}
												a.addValue(stringValue);
											}
											else if (valueType == JsonValue.ValueType.OBJECT && ValueRepresentation.isPersonNameVR(vr)) {
												JsonObject jsonObjectValue = (JsonObject)value;
												String stringValue = getJsonPersonNameFromPropertiesInJsonObject(jsonObjectValue,reservedKeywordForPersonNameAlphabeticPropertyInJsonRepresentation,reservedKeywordForPersonNameIdeographicPropertyInJsonRepresentation,reservedKeywordForPersonNamePhoneticPropertyInJsonRepresentation);
												a.addValue(stringValue);
											}
											else if (valueType == JsonValue.ValueType.NULL) {
												a.addValue("");		// empty value ... assumes string VR
											}
											else {
												throw new DicomException("Unrecognized type of value for attribute "+elementName);
											}
										}
									}
									else if (inLineBinary != null && inLineBinary.length() > 0) {
										if (ValueRepresentation.isBase64EncodedInJSON(vr)) {
											slf4jlogger.debug("addAttributesFromJsonObjectToList(): Base64 String VR");
											byte[] b = Base64.getDecoder().decode(inLineBinary);
											slf4jlogger.debug("addAttributesFromJsonObjectToList(): Base64 decodes as array of length {}",b.length);
											a.setValues(b,false/*big*/);
										}
										else {
											throw new DicomException("Expected single string Base64 InLineBinary for attribute "+elementName);
										}
									}
									else {
										slf4jlogger.debug("addAttributesFromJsonObjectToList(): Have no values");
									}
									if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("addAttributesFromJsonObjectToList(): Attribute is {}",a.toString());
									list.put(tag,a);
								}
							}
						}
						else {
							throw new DicomException("Cannot determine VR for "+tag+" "+elementName);
						}
					}
					else {
						if (!ignoreUnrecognizedTags) {
							throw new DicomException("Cannot determine tag for "+elementName);
						}
					}
				}
				// else not adding it
			}
		}
	}

	/**
	 * @param	jsonPersonNameValue
	 * @param	value
	 * @param	alphabeticProperty
	 * @param	ideographicProperty
	 * @param	phoneticProperty
	 */
	
	public static void addPersonNameAsComponentsToJsonObject(JsonObjectBuilder jsonPersonNameValue,String value,String alphabeticProperty,String ideographicProperty,String phoneticProperty) {
		Vector<String> nameComponentGroups = PersonNameAttribute.getNameComponentGroups(value);
		int numberOfNameComponentGroups = nameComponentGroups.size();
		if (numberOfNameComponentGroups >= 1) {
			String alphabetic = nameComponentGroups.get(0);
			if (alphabetic != null && alphabetic.length() > 0) {
				jsonPersonNameValue.add(alphabeticProperty,alphabetic);
			}
		}
		if (numberOfNameComponentGroups >= 2) {
			String ideographic = nameComponentGroups.get(1);
			if (ideographic != null && ideographic.length() > 0) {
				jsonPersonNameValue.add(ideographicProperty,ideographic);
			}
		}
		if (numberOfNameComponentGroups >= 3) {
			String phonetic = nameComponentGroups.get(2);
			if (phonetic != null && phonetic.length() > 0) {
				jsonPersonNameValue.add(phoneticProperty,phonetic);
			}
		}
	}

	/**
	 * @param	list
	 * @param	parentObjectBuilder
	 * @param	useKeywordInsteadOfTag	use the keyword from the DicomDictionary rather than the hexadecimal tag group and element
	 * @param	addTag					add the hexadecimal group and element as an additional attribute
	 * @param	addKeyword				add the DicomDictionary keyword as an additional attribute
	 * @param	ignoreSR				whether or not to ignore SR Content Items (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @deprecated
	 * @throws	DicomException
	 */
	void addAttributesFromListToJsonObject(AttributeList list,JsonObjectBuilder parentObjectBuilder,boolean useKeywordInsteadOfTag,boolean addTag,boolean addKeyword,boolean ignoreSR) throws DicomException {
		addAttributesFromListToJsonObject(list,parentObjectBuilder,useKeywordInsteadOfTag,addTag,addKeyword,true/*addVR*/,false/*collapseValueArrays*/,false/*collapseEmptyToNull*/,ignoreSR,false/*substituteUIDKeywords*/,false/*useNumberForIntegerOrDecimalString*/);
	}
	
	/**
	 * @param	list
	 * @param	parentObjectBuilder
	 * @param	useKeywordInsteadOfTag	use the keyword from the DicomDictionary rather than the hexadecimal tag group and element
	 * @param	addTag					add the hexadecimal group and element as an additional attribute
	 * @param	addKeyword				add the DicomDictionary keyword as an additional attribute
	 * @param	addVR					add the value representation as an additional attribute
	 * @param	collapseValueArrays		whether or not to elide value object and array when a single value and no other objects
	 * @param	collapseEmptyToNull		whether or not to elide empty object as value and send null instead when zero length attribute
	 * @param	ignoreSR				whether or not to ignore SR Content Items (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @param	substituteUIDKeywords	whether or not to substitute keywords for recognized standard SOP Classes
	 * @param	useNumberForIntegerOrDecimalString	whether or not to use JSON Number instead of JSON String for IS and DS attributes
	 * @throws	DicomException
	 */
	void addAttributesFromListToJsonObject(AttributeList list,JsonObjectBuilder parentObjectBuilder,boolean useKeywordInsteadOfTag,boolean addTag,boolean addKeyword,boolean addVR,boolean collapseValueArrays,boolean collapseEmptyToNull,boolean ignoreSR,boolean substituteUIDKeywords,boolean useNumberForIntegerOrDecimalString) throws DicomException {
		//DicomDictionary dictionary = list.getDictionary();
		Iterator i = list.values().iterator();
		while (i.hasNext()) {
			Attribute attribute = (Attribute)i.next();
			byte[] vr = attribute.getVR();
			AttributeTag tag = attribute.getTag();
			slf4jlogger.debug("addAttributesFromListToJsonObject(): Adding {}",tag);
			boolean addIt = true;

			String jsonSingleStringValue = null;
			BigDecimal jsonSingleBigDecimalValue = null;
			BigInteger jsonSingleBigIntegerValue = null;
			double jsonSingleNumericValue = 0;
			boolean haveJsonSingleNumericValue = false;

			JsonArrayBuilder jsonValues = null;
			String inLineBinary = null;
			
			if (ignoreSR && (
			   	tag.equals(TagFromName.ContentSequence)
			 || tag.equals(TagFromName.ValueType)
			 || tag.equals(TagFromName.ConceptNameCodeSequence)
			 || tag.equals(TagFromName.ContinuityOfContent)
			 || tag.equals(TagFromName.ContentTemplateSequence)
			 || tag.equals(TagFromName.MappingResource)
			 || tag.equals(TagFromName.TemplateIdentifier)
			   )) {
				slf4jlogger.debug("addAttributesFromListToJsonObject(): Ignoring SR-related tag");
				addIt = false;
			}
			else if (attribute instanceof SequenceAttribute) {
				SequenceAttribute sa = (SequenceAttribute)attribute;
				if (sa.getNumberOfItems() > 0) {
					jsonValues = factory.createArrayBuilder();
					Iterator si = sa.iterator();
					while (si.hasNext()) {
						SequenceItem item = (SequenceItem)si.next();
						AttributeList itemList = item.getAttributeList();
						JsonObjectBuilder jsonSequenceItemAttributeListValue = factory.createObjectBuilder();
						addAttributesFromListToJsonObject(itemList,jsonSequenceItemAttributeListValue,useKeywordInsteadOfTag,addTag,addKeyword,addVR,collapseValueArrays,collapseEmptyToNull,ignoreSR,substituteUIDKeywords,useNumberForIntegerOrDecimalString);
						jsonValues.add(jsonSequenceItemAttributeListValue);
					}
				}
				// else empty sequence so encode no values at all
			}
			else if (ValueRepresentation.isPersonNameVR(vr)) {
				String values[] = null;
				try {
					values=attribute.getStringValues();
				}
				catch (DicomException e) {
					slf4jlogger.debug("addAttributesFromListToJsonObject(): Ignoring exception",e);
				}
				if (values != null && values.length > 0) {
					jsonValues = factory.createArrayBuilder();
					for (int j=0; j<values.length; ++j) {
						String value = values[j];
						if (value != null && value.length() > 0) {
							JsonObjectBuilder jsonPersonNameValue = factory.createObjectBuilder();
							addPersonNameAsComponentsToJsonObject(jsonPersonNameValue,value,
								reservedKeywordForPersonNameAlphabeticPropertyInJsonRepresentation,
								reservedKeywordForPersonNameIdeographicPropertyInJsonRepresentation,
								reservedKeywordForPersonNamePhoneticPropertyInJsonRepresentation);
							jsonValues.add(jsonPersonNameValue);
						}
						else {
							jsonValues.addNull();
						}
					}
				}
				// else empty so encode no values at all
			}
			else if (ValueRepresentation.isDecimalNumberInJSON(vr)) {	// DS, FL, FD
				int vm = attribute.getVM();
				if (vm > 0) {
					if (vm == 1 && collapseValueArrays && !addVR && !tag.isPrivate()) {
						slf4jlogger.debug("addAttributesFromListToJsonObject(): have one decimal value to add without value array");
						if (ValueRepresentation.isDecimalStringVR(vr)) {
							String stringValues[] = attribute.getStringValues();
							// use the String that was originally supplied rather than reconverting double back to String
							if (useNumberForIntegerOrDecimalString) {
								// unfortunately javax.json Builders have no methods to add JSON Number from String :(
								jsonSingleBigDecimalValue =  new BigDecimal(stringValues[0]);	// does not preserve "-7.8e-1" :(
							}
							else {
								jsonSingleStringValue = stringValues[0];	// preserves "-7.8e-1"
							}
						}
						else {
							// JSON Number is always required for other than DS
							double doubleValues[] = null;
							doubleValues=attribute.getDoubleValues();
							jsonSingleNumericValue = doubleValues[0];
							haveJsonSingleNumericValue = true;
						}
					}
					else {
						jsonValues = factory.createArrayBuilder();
						if (ValueRepresentation.isDecimalStringVR(vr)) {
							String stringValues[] = attribute.getStringValues();
							// use the String that was originally supplied rather than reconverting double back to String
							for (int j=0; j<vm; ++j) {
								if (useNumberForIntegerOrDecimalString) {
									// unfortunately javax.json Builders have no methods to add JSON Number from String :(
									jsonValues.add(new BigDecimal(stringValues[j]));	// does not preserve "-7.8e-1" :(
								}
								else {
									jsonValues.add(stringValues[j]);	// preserves "-7.8e-1"
								}
							}
						}
						else {
							// JSON Number is always required for other than DS
							double doubleValues[] = null;
							doubleValues=attribute.getDoubleValues();
							for (int j=0; j<vm; ++j) {
								jsonValues.add(doubleValues[j]);
							}
						}
					}
				}
				// else empty so encode no values at all
			}
			else if (ValueRepresentation.isIntegerNumberInJSON(vr)) {	// IS, SS, SL, SV,  US, UL, UV
				int vm = attribute.getVM();
				if (vm > 0) {
					if (vm == 1 && collapseValueArrays && !addVR && !tag.isPrivate()) {
						slf4jlogger.debug("addAttributesFromListToJsonObject(): have one integer value to add without value array");
						if (ValueRepresentation.isIntegerStringVR(vr)) {
							String stringValues[] = attribute.getStringValues();
							// use the String that was originally supplied rather than reconverting long back to String
							if (useNumberForIntegerOrDecimalString) {
								// unfortunately javax.json Builders have no methods to add JSON Number from String :(
								jsonSingleBigIntegerValue =  new BigInteger(stringValues[0]);
							}
							else {
								jsonSingleStringValue = stringValues[0];
							}
						}
						else {
							// JSON Number is always required for other than IS
							long longValues[] = null;
							longValues=attribute.getLongValues();
							jsonSingleNumericValue = longValues[0];		// OK to pass through double since long fits in mantissa
							haveJsonSingleNumericValue = true;
						}
					}
					else {
						jsonValues = factory.createArrayBuilder();
						if (ValueRepresentation.isIntegerStringVR(vr)) {
							String stringValues[] = attribute.getStringValues();
							// use the String that was originally supplied rather than reconverting double back to String
							for (int j=0; j<vm; ++j) {
								if (useNumberForIntegerOrDecimalString) {
									// unfortunately javax.json Builders have no methods to add JSON Number from String :(
									jsonValues.add(new BigInteger(stringValues[j]));
								}
								else {
									jsonValues.add(stringValues[j]);
								}
							}
						}
						else {
							// JSON Number is always required for other than IS
							long longValues[] = null;
							longValues=attribute.getLongValues();
							for (int j=0; j<vm; ++j) {
								jsonValues.add(longValues[j]);
							}
						}
					}
				}
				// else empty so encode no values at all
			}
			else if (ValueRepresentation.isBase64EncodedInJSON(vr)) {
				byte values[] = attribute.getByteValues(false/*big*/);	// assume always little endian since big endian forbidden in PS3.18
				if (values != null && values.length > 0) {
					inLineBinary = javax.xml.bind.DatatypeConverter.printBase64Binary(values);
				}
			}
			else {
				slf4jlogger.debug("addAttributesFromListToJsonObject(): assuming String values");
				String values[] = null;
				try {
					values=attribute.getStringValues();			// TBD. should use VR to distinguish String from Number, Base64, etc. per PS3.18 Table F.2.3-1. :(
				}
				catch (DicomException e) {
					slf4jlogger.debug("addAttributesFromListToJsonObject(): Ignoring exception",e);
				}
				if (values != null) {
					slf4jlogger.debug("addAttributesFromListToJsonObject(): values.length = {}",values.length);
					if (values.length > 0) {
						if (values.length == 1 && collapseValueArrays && !addVR && !tag.isPrivate()) {
							slf4jlogger.debug("addAttributesFromListToJsonObject(): have one String value to add without value array {}",values[0]);
							jsonSingleStringValue = substituteKeywordForUIDIfPossibleAndAppropriateForVRAndRequested(values[0],vr,substituteUIDKeywords);
						}
						else {
							jsonValues = factory.createArrayBuilder();
							for (int j=0; j<values.length; ++j) {
								String value = values[j];
								if (value != null && value.length() > 0) {
									jsonValues.add(substituteKeywordForUIDIfPossibleAndAppropriateForVRAndRequested(value,vr,substituteUIDKeywords));
								}
								else {
									jsonValues.addNull();
								}
							}
						}
					}
					else {
						slf4jlogger.debug("addAttributesFromListToJsonObject(): have zero length values array");
					}
				}
				else {
					slf4jlogger.debug("addAttributesFromListToJsonObject(): have null values array");
				}
				// else empty so encode no values at all
			}

			if (addIt) {
				String keyword = (useKeywordInsteadOfTag || addKeyword) ? DicomDictionary.StandardDictionary.getNameFromTag(tag) : null;
				boolean haveKeyword = keyword != null && keyword.length() > 0;
				String hextag = (!haveKeyword || addTag) ? makeElementNameFromHexadecimalGroupElementValues(tag) : null;

				String elementName = useKeywordInsteadOfTag && haveKeyword ? keyword : hextag;

				JsonObjectBuilder jsonAttributeVRAndValue = factory.createObjectBuilder();
				if (addVR || tag.isPrivate()) {
					jsonAttributeVRAndValue.add("vr",ValueRepresentation.getAsString(vr));
				}
				
				boolean addJsonAttributeVRAndValue = false;
				
				if (addKeyword && haveKeyword) {
					jsonAttributeVRAndValue.add("keyword",keyword);		// non-standard (not PS3.18 Annex F)
					addJsonAttributeVRAndValue = true;
				}
				
				if (addTag) {
					jsonAttributeVRAndValue.add("tag",hextag);			// non-standard (not PS3.18 Annex F)
					addJsonAttributeVRAndValue = true;
				}

				if (jsonValues != null) {
					jsonAttributeVRAndValue.add("Value",jsonValues);
					addJsonAttributeVRAndValue = true;
				}
				else if (inLineBinary != null) {
					jsonAttributeVRAndValue.add("InlineBinary",inLineBinary);
					addJsonAttributeVRAndValue = true;
				}
				// else empty (zero length) so encode no Value at all, not empty Value

				if (jsonSingleStringValue != null) {
					slf4jlogger.debug("addAttributesFromListToJsonObject(): {} adding jsonSingleStringValue {}",elementName,jsonSingleStringValue);
					parentObjectBuilder.add(elementName,jsonSingleStringValue);
				}
				else if (haveJsonSingleNumericValue) {
					slf4jlogger.debug("addAttributesFromListToJsonObject(): {} adding jsonSingleNumericValue {}",elementName,jsonSingleNumericValue);
					parentObjectBuilder.add(elementName,jsonSingleNumericValue);
				}
				else if (jsonSingleBigDecimalValue != null) {
					slf4jlogger.debug("addAttributesFromListToJsonObject(): {} adding jsonSingleBigDecimalValue {}",elementName,jsonSingleBigDecimalValue);
					parentObjectBuilder.add(elementName,jsonSingleBigDecimalValue);
				}
				else if (jsonSingleBigIntegerValue != null) {
					slf4jlogger.debug("addAttributesFromListToJsonObject(): {} adding jsonSingleBigIntegerValue {}",elementName,jsonSingleBigIntegerValue);
					parentObjectBuilder.add(elementName,jsonSingleBigIntegerValue);
				}
				else if (addJsonAttributeVRAndValue || !collapseEmptyToNull) {
					slf4jlogger.debug("addAttributesFromListToJsonObject(): {} adding jsonAttributeVRAndValue {}",elementName,jsonAttributeVRAndValue);
					parentObjectBuilder.add(elementName,jsonAttributeVRAndValue);
				}
				else {
					slf4jlogger.debug("addAttributesFromListToJsonObject(): {} adding null {}",elementName);
					parentObjectBuilder.addNull(elementName);
				}
			}
		}
	}

	/**
	 * <p>Construct a factory object, which can be used to get JSON documents from DICOM objects.</p>
	 */
	public JSONRepresentationOfDicomObjectFactory() {
		factory = Json.createBuilderFactory(null/*config*/);
	}
	
	/**
	 * <p>Given a DICOM instance encoded as a list of attributes, get a JSON document.</p>
	 *
	 * @param	list					the list of DICOM attributes
	 * @param	useKeywordInsteadOfTag	use the keyword from the DicomDictionary rather than the hexadecimal tag group and element
	 * @param	addTag					add the hexadecimal group and element as an additional attribute
	 * @param	addKeyword				add the DicomDictionary keyword as an additional attribute
	 * @param	ignoreSR				whether or not to ignore SR Content Items (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @return							the JSON document
	 * @deprecated
	 * @throws	DicomException
	 */
	public JsonArray getDocument(AttributeList list,boolean useKeywordInsteadOfTag,boolean addTag,boolean addKeyword,boolean ignoreSR) throws DicomException {
		return getDocument(list,useKeywordInsteadOfTag,addTag,addKeyword,true/*addVR*/,false/*collapseValueArrays*/,false/*collapseEmptyToNull*/,ignoreSR,false/*substituteUIDKeywords*/,false/*useNumberForIntegerOrDecimalString*/);
	}
	/**
	 * <p>Given a DICOM instance encoded as a list of attributes, get a JSON document.</p>
	 *
	 * @param	list					the list of DICOM attributes
	 * @param	useKeywordInsteadOfTag	use the keyword from the DicomDictionary rather than the hexadecimal tag group and element
	 * @param	addTag					add the hexadecimal group and element as an additional attribute
	 * @param	addKeyword				add the DicomDictionary keyword as an additional attribute
	 * @param	addVR					add the value representation as an additional attribute
	 * @param	collapseValueArrays		whether or not to elide value object and array when a single value and no other objects
	 * @param	collapseEmptyToNull		whether or not to elide empty object as value and send null instead when zero length attribute
	 * @param	ignoreSR				whether or not to ignore SR Content Items (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @param	substituteUIDKeywords	whether or not to substitute keywords for recognized standard SOP Classes
	 * @param	useNumberForIntegerOrDecimalString	whether or not to use JSON Number instead of JSON String for IS and DS attributes
	 * @return							the JSON document
	 * @throws	DicomException
	 */
	public JsonArray getDocument(AttributeList list,boolean useKeywordInsteadOfTag,boolean addTag,boolean addKeyword,boolean addVR,boolean collapseValueArrays,boolean collapseEmptyToNull,boolean ignoreSR,boolean substituteUIDKeywords,boolean useNumberForIntegerOrDecimalString) throws DicomException {
		JsonObjectBuilder topLevelObjectBuilder = factory.createObjectBuilder();
		addAttributesFromListToJsonObject(list,topLevelObjectBuilder,useKeywordInsteadOfTag,addTag,addKeyword,addVR,collapseValueArrays,collapseEmptyToNull,ignoreSR,substituteUIDKeywords,useNumberForIntegerOrDecimalString);
		return factory.createArrayBuilder().add(topLevelObjectBuilder).build();
	}

	/**
	 * <p>Given a DICOM instance encoded as a list of attributes, get a JSON document.</p>
	 *
	 * @param	list	the list of DICOM attributes
	 * @return							the JSON document
	 * @throws	DicomException
	 */
	public JsonArray getDocument(AttributeList list) throws DicomException {
		return getDocument(list,false/*useKeywordInsteadOfTag*/,false/*addTag*/,false/*addKeyword*/,false/*ignoreSR*/);
	}

	/**
	 * <p>Given a DICOM object encoded as a list of attributes, get a JSON document.</p>
	 *
	 * @param	file	the DICOM file
	 * @param	useKeywordInsteadOfTag	use the keyword from the DicomDictionary rather than the hexadecimal tag group and element
	 * @param	addTag					add the hexadecimal group and element as an additional attribute
	 * @param	addKeyword				add the DicomDictionary keyword as an additional attribute
	 * @param	ignoreSR				whether or not to ignore SR Content Items (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @return							the JSON document
	 * @deprecated
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public JsonArray getDocument(File file,boolean useKeywordInsteadOfTag,boolean addTag,boolean addKeyword,boolean ignoreSR) throws IOException, DicomException {
		return getDocument(file,useKeywordInsteadOfTag,addTag,addKeyword,true/*addVR*/,false/*collapseValueArrays*/,false/*collapseEmptyToNull*/,ignoreSR,false/*substituteUIDKeywords*/,false/*useNumberForIntegerOrDecimalString*/);
	}

	/**
	 * <p>Given a DICOM instance encoded as a list of attributes, get a JSON document.</p>
	 *
	 * @param	file	the DICOM file
	 * @param	useKeywordInsteadOfTag	use the keyword from the DicomDictionary rather than the hexadecimal tag group and element
	 * @param	addTag					add the hexadecimal group and element as an additional attribute
	 * @param	addKeyword				add the DicomDictionary keyword as an additional attribute
	 * @param	addVR					add the value representation as an additional attribute
	 * @param	collapseValueArrays		whether or not to elide value object and array when a single value and no other objects
	 * @param	collapseEmptyToNull		whether or not to elide empty object as value and send null instead when zero length attribute
	 * @param	ignoreSR				whether or not to ignore SR Content Items (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @param	substituteUIDKeywords	whether or not to substitute keywords for recognized standard SOP Classes
	 * @param	useNumberForIntegerOrDecimalString	whether or not to use JSON Number instead of JSON String for IS and DS attributes
	 * @return							the JSON document
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public JsonArray getDocument(File file,boolean useKeywordInsteadOfTag,boolean addTag,boolean addKeyword,boolean addVR,boolean collapseValueArrays,boolean collapseEmptyToNull,boolean ignoreSR,boolean substituteUIDKeywords,boolean useNumberForIntegerOrDecimalString) throws IOException, DicomException {
		AttributeList list = new AttributeList();
		list.setDecompressPixelData(false);
		list.read(file);
		return getDocument(list,useKeywordInsteadOfTag,addTag,addKeyword,addVR,collapseValueArrays,collapseEmptyToNull,ignoreSR,substituteUIDKeywords,useNumberForIntegerOrDecimalString);
	}

	/**
	 * <p>Given a DICOM instance encoded as a list of attributes, get a JSON document.</p>
	 *
	 * @param	file					the DICOM file
	 * @return							the JSON document
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public JsonArray getDocument(File file) throws IOException, DicomException {
		return getDocument(file,false/*useKeywordInsteadOfTag*/,false/*addTag*/,false/*addKeyword*/,false/*ignoreSR*/);
	}

	/**
	 * <p>Given a DICOM instance encoded as a list of attributes, get a JSON document.</p>
	 *
	 * @param	filename				the DICOM file name
	 * @param	useKeywordInsteadOfTag	use the keyword from the DicomDictionary rather than the hexadecimal tag group and element
	 * @param	addTag					add the hexadecimal group and element as an additional attribute
	 * @param	addKeyword				add the DicomDictionary keyword as an additional attribute
	 * @param	ignoreSR				whether or not to ignore SR Content Items (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @return							the JSON document
	 * @deprecated
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public JsonArray getDocument(String filename,boolean useKeywordInsteadOfTag,boolean addTag,boolean addKeyword,boolean ignoreSR) throws IOException, DicomException {
		return getDocument(filename,useKeywordInsteadOfTag,addTag,addKeyword,true/*addVR*/,false/*collapseValueArrays*/,false/*collapseEmptyToNull*/,ignoreSR,false/*substituteUIDKeywords*/,false/*useNumberForIntegerOrDecimalString*/);
	}

	/**
	 * <p>Given a DICOM instance encoded as a list of attributes, get a JSON document.</p>
	 *
	 * @param	filename				the DICOM file name
	 * @param	useKeywordInsteadOfTag	use the keyword from the DicomDictionary rather than the hexadecimal tag group and element
	 * @param	addTag					add the hexadecimal group and element as an additional attribute
	 * @param	addKeyword				add the DicomDictionary keyword as an additional attribute
	 * @param	addVR					add the value representation as an additional attribute
	 * @param	collapseValueArrays		whether or not to elide value object and array when a single value and no other objects
	 * @param	collapseEmptyToNull		whether or not to elide empty object as value and send null instead when zero length attribute
	 * @param	ignoreSR				whether or not to ignore SR Content Items (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @param	substituteUIDKeywords	whether or not to substitute keywords for recognized standard SOP Classes
	 * @param	useNumberForIntegerOrDecimalString	whether or not to use JSON Number instead of JSON String for IS and DS attributes
	 * @return							the JSON document
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public JsonArray getDocument(String filename,boolean useKeywordInsteadOfTag,boolean addTag,boolean addKeyword,boolean addVR,boolean collapseValueArrays,boolean collapseEmptyToNull,boolean ignoreSR,boolean substituteUIDKeywords,boolean useNumberForIntegerOrDecimalString) throws IOException, DicomException {
		return getDocument(new File(filename),useKeywordInsteadOfTag,addTag,addKeyword,addVR,collapseValueArrays,collapseValueArrays,ignoreSR,substituteUIDKeywords,useNumberForIntegerOrDecimalString);
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
		return getDocument(filename,false/*useKeywordInsteadOfTag*/,false/*addTag*/,false/*addKeyword*/,false/*ignoreSR*/);
	}
	
	/**
	 * <p>Given a DICOM object encoded in a JSON document
	 * convert it to a list of attributes.</p>
	 *
	 * @param	topLevelObject			the first object of the array that is the JSON document
	 * @param	ignoreUnrecognizedTags	whether or not to ignore unrecognized tags (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @param	ignoreSR				whether or not to ignore SR Content Items (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @return							the list of DICOM attributes
	 * @throws	DicomException
	 */
	public AttributeList getAttributeList(JsonObject topLevelObject,boolean ignoreUnrecognizedTags,boolean ignoreSR) throws DicomException {
		AttributeList list = new AttributeList();
		addAttributesFromJsonObjectToList(list,topLevelObject,ignoreUnrecognizedTags,ignoreSR);
//System.err.println("JSONRepresentationOfDicomObjectFactory.getAttributeList(JsonObject topLevelObject): List is "+list);
		return list;
	}
	
	/**
	 * <p>Given a DICOM instance encoded as a JsonObject in JsonArray in a JSON document
	 * convert it to a list of attributes.</p>
	 *
	 * @param	document				the JSON document
	 * @param	ignoreUnrecognizedTags	whether or not to ignore unrecognized tags (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @param	ignoreSR				whether or not to ignore SR Content Items (e.g., when used with JSONRepresentationOfStructuredReportObjectFactory)
	 * @return							the list of DICOM attributes
	 * @throws	DicomException
	 */
	public AttributeList getAttributeList(JsonArray document,boolean ignoreUnrecognizedTags,boolean ignoreSR) throws DicomException {
		AttributeList list = null;
		JsonObject topLevelObject = null;
		try {
			topLevelObject = document.getJsonObject(0);
		}
		catch (IndexOutOfBoundsException e) {
			throw new DicomException("Could not parse JSON document - exactly one object in top level array expected "+e);
		}
		catch (ClassCastException e) {
			throw new DicomException("Could not parse JSON document - expected object in top level array "+e);
		}
		return getAttributeList(topLevelObject,ignoreUnrecognizedTags,ignoreSR);
	}

	/**
	 * <p>Given a DICOM instance encoded as a JSON document
	 * convert it to a list of attributes.</p>
	 *
	 * @param		document		the JSON document
	 * @return						the list of DICOM attributes
	 * @throws	DicomException
	 */
	public AttributeList getAttributeList(JsonArray document) throws DicomException {
		return getAttributeList(document,false/*ignoreUnrecognizedTags*/,false/*ignoreSR*/);
	}

	/**
	 * <p>Given a DICOM instance encoded as a JSON document
	 * convert it to a list of attributes.</p>
	 *
	 * @param		document		the JSON document
	 * @return						the list of DICOM attributes
	 * @throws	DicomException
	 */
	public AttributeList getAttributeList(JsonObject document) throws DicomException {
		return getAttributeList(document,false/*ignoreUnrecognizedTags*/,false/*ignoreSR*/);
	}

	/**
	 * <p>Given a DICOM instance encoded as a JSON document in a stream
	 * convert it to a list of attributes.</p>
	 *
	 * @param		stream			the input stream containing the JSON document
	 * @return						the list of DICOM attributes
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public AttributeList getAttributeList(InputStream stream) throws IOException, DicomException {
		JsonReader jsonReader = Json.createReader(stream);
		JsonStructure document = jsonReader.read();
		jsonReader.close();
		if (document instanceof JsonArray) {
			return getAttributeList((JsonArray)document);
		}
		else if (document instanceof JsonObject) {
			return getAttributeList((JsonObject)document);
		}
		else {
			throw new DicomException("Could not parse JSON document - expected object or array at top level");
		}
	}
	
	/**
	 * <p>Given a DICOM instance encoded as a JSON document in a file
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
	 * <p>Given a DICOM instance encoded as a JSON document in a named file
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
	 * <p>Serialize a JSON document created from a DICOM attribute list.</p>
	 *
	 * @param	list	the list of DICOM attributes
	 * @param	out		the output stream to write to
	 * @throws	DicomException
	 */
	public static void createDocumentAndWriteIt(AttributeList list,OutputStream out) throws DicomException {
		try {
			JsonArray document = new JSONRepresentationOfDicomObjectFactory().getDocument(list);
			write(out,document);
		}
		catch (Exception e) {
			throw new DicomException("Could not create JSON document - could not transform to JSON "+e);
		}
	}

	/**
	 * <p>Serialize a JSON document created from a DICOM attribute list.</p>
	 *
	 * @param	list			the list of DICOM attributes
	 * @param	outputFile		the output file to write to
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public static void createDocumentAndWriteIt(AttributeList list,File outputFile) throws IOException, DicomException {
		OutputStream out = new FileOutputStream(outputFile);
		createDocumentAndWriteIt(list,out);
		out.close();
	}

	/**
	 * <p>Serialize a JSON document created from a DICOM attribute list.</p>
	 *
	 * @param	list		the list of DICOM attributes
	 * @param	outputPath	the output path to write to
	 * @throws	IOException
	 * @throws	DicomException
	 */
	public static void createDocumentAndWriteIt(AttributeList list,String outputPath) throws IOException, DicomException {
		createDocumentAndWriteIt(list,new File(outputPath));
	}

	/**
	 * <p>Read a DICOM dataset and write a JSON representation of it to the standard output or specified path, or vice versa.</p>
	 *
	 * @param	arg	either one input path of the file containing the DICOM/JSON dataset, or a direction argument (toDICOM or toJSON, case insensitive, defaults to toJSON) and an input path, and optionally an output path
	 */
	public static void main(String arg[]) {
		try {
			boolean toJSON = true;
			
			String inputPath = null;
			String outputPath = null;
			
			boolean useKeywordInsteadOfTag = false;
			boolean addTag = false;
			boolean addKeyword = false;
			boolean addVR = true;
			boolean collapseValueArrays = false;
			boolean collapseEmptyToNull = false;
			boolean ignoreSR = false;
			boolean substituteUIDKeywords = false;
			boolean useNumberForIntegerOrDecimalString = false;	// see also discussion at "http://www.npmjs.com/package/fhir#decimal-types"

			int numberOfFixedArguments = 1;
			int numberOfFixedAndOptionalArguments = 3;
			int endOptionsPosition = arg.length;
			
			boolean bad = false;
			
			if (endOptionsPosition < numberOfFixedArguments) {
				bad = true;
			}
			boolean keepLooking = true;
			while (keepLooking && endOptionsPosition > numberOfFixedArguments) {
				String option = arg[endOptionsPosition-1].trim().toUpperCase();
				switch (option) {
					case "USEKEYWORD":			useKeywordInsteadOfTag = true; --endOptionsPosition; break;
					case "USETAG":				useKeywordInsteadOfTag = false; --endOptionsPosition; break;
					
					case "ADDTAG":				addTag = true; --endOptionsPosition; break;
					case "DONOTADDTAG":			addTag = false; --endOptionsPosition; break;
					
					case "ADDKEYWORD":			addKeyword = true; --endOptionsPosition; break;
					case "DONOTADDKEYWORD":		addKeyword = false; --endOptionsPosition; break;
					
					case "ADDVR":				addVR = true; --endOptionsPosition; break;
					case "DONOTADDVR":			addVR = false; --endOptionsPosition; break;

					case "COLLAPSEVALUEARRAYS":			collapseValueArrays = true; --endOptionsPosition; break;
					case "DONOTCOLLAPSEVALUEARRAYS":	collapseValueArrays = false; --endOptionsPosition; break;
					
					case "COLLAPSEEMPTYTONULL":			collapseEmptyToNull = true; --endOptionsPosition; break;
					case "DONOTCOLLAPSEEMPTYTONULL":	collapseEmptyToNull = false; --endOptionsPosition; break;

					case "IGNORESR":			ignoreSR = true; --endOptionsPosition; break;
					case "DONOTIGNORESR":		ignoreSR = false; --endOptionsPosition; break;

					case "USEUIDKEYWORDS":			substituteUIDKeywords = true; --endOptionsPosition; break;
					case "DONOTUSEUIDKEYWORDS":		substituteUIDKeywords = false; --endOptionsPosition; break;

					case "USENUMBERFORISDS":		useNumberForIntegerOrDecimalString = true; --endOptionsPosition; break;
					case "DONOTUSENUMBERFORISDS":	useNumberForIntegerOrDecimalString = false; --endOptionsPosition; break;

					default:	if (endOptionsPosition > numberOfFixedAndOptionalArguments) {
									slf4jlogger.error("Unrecognized argument {}",option);
									bad = true;
								}
								keepLooking = false;
								break;
				}
			}

			if (!bad) {
				if (endOptionsPosition == 1) {
					toJSON = true;
					inputPath = arg[0];
				}
				else if (endOptionsPosition == 2) {
					if (arg[0].toLowerCase(java.util.Locale.US).equals("tojson")) {
						inputPath = arg[1];
						toJSON = true;
					}
					else if (arg[0].toLowerCase(java.util.Locale.US).equals("todicom") || arg[0].toLowerCase(java.util.Locale.US).equals("todcm")) {
						inputPath = arg[1];
						bad = false;
						toJSON = false;
					}
					else {
						inputPath = arg[0];
						outputPath = arg[1];
						toJSON = true;
					}
				}
				else if (endOptionsPosition == 3) {
					if (arg[0].toLowerCase(java.util.Locale.US).equals("tojson")) {
						inputPath = arg[1];
						outputPath = arg[2];
						toJSON = true;
					}
					else if (arg[0].toLowerCase(java.util.Locale.US).equals("todicom") || arg[0].toLowerCase(java.util.Locale.US).equals("todcm")) {
						inputPath = arg[1];
						outputPath = arg[2];
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
			
			if (!toJSON && (useKeywordInsteadOfTag || addTag || addKeyword || !addVR || collapseValueArrays)) {
				System.err.println("Unexpected options specified for conversion to DICOM that are only applicable to conversion to JSON");
				bad = true;
			}
			
			if (bad) {
				System.err.println("usage: JSONRepresentationOfDicomObjectFactory [toJSON|toDICOM] inputpath [outputpath]"
					+" [USEKEYWORD|USETAG]"
					+" [ADDTAG|DONOTADDTAG]"
					+" [ADDKEYWORD|DONOTADDKEYWORD]"
					+" [ADDVR|DONOTADDVR]"
					+" [COLLAPSEVALUEARRAYS|DONOTCOLLAPSEVALUEARRAYS]"
					+" [COLLAPSEEMPTYTONULL|DONOTCOLLAPSEEMPTYTONULL]"
					+" [IGNORESR|DONOTIGNORESR]"
					+" [USEUIDKEYWORDS|DONOTUSEUIDKEYWORDS]"
					+" [USENUMBERFORISDS|DONOTUSENUMBERFORISDS]"
				);
				System.err.println("usage: JSONRepresentationOfDicomObjectFactory toDICOM inputpath [outputpath]");
				System.exit(1);
			}
			else {
				if (toJSON) {
					JsonArray document = new JSONRepresentationOfDicomObjectFactory().getDocument(inputPath,useKeywordInsteadOfTag,addTag,addKeyword,addVR,collapseValueArrays,collapseEmptyToNull,ignoreSR,substituteUIDKeywords,useNumberForIntegerOrDecimalString);
					//System.err.println(toString(document));
					if (outputPath == null) {
						write(System.out,document);
					}
					else {
						write(outputPath,document);
					}
				}
				else {
//long startReadTime = System.currentTimeMillis();
					AttributeList list = new JSONRepresentationOfDicomObjectFactory().getAttributeList(inputPath);
//System.err.println("AttributeList.main(): read JSON and create DICOM AttributeList - done in "+(System.currentTimeMillis()-startReadTime)+" ms");
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

