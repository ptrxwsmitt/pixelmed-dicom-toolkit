/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.dicom;

import java.io.*;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>A class to encapsulate the attributes contained within a Sequence Item that represents
 * a Coded Sequence item.</p>
 *
 * @author	dclunie
 */
public class CodedSequenceItem {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/dicom/CodedSequenceItem.java,v 1.26 2022/01/21 19:51:14 dclunie Exp $";

	protected AttributeList list;
	
	/**
	 *
	 */
	public boolean equals(Object o) {
//System.err.println("CodedSequenceItem.equals():");
		boolean match = false;
		if (o instanceof CodedSequenceItem) {
//System.err.println("CodedSequenceItem.equals(): is CodedSequenceItem");
			match = list.equals(((CodedSequenceItem)o).getAttributeList());
		}
//System.err.println("CodedSequenceItem.equals(): match = "+match);
		return match;
	}
	
	/**
	 *
	 */
	public int hashCode() {
		return list.hashCode();
	}

	/**
	 * <p>Construct a <code>CodedSequenceItem</code> from a list of attributes.</p>
	 *
	 * @param	l	the list of attributes to include in the item
	 */
	public CodedSequenceItem(AttributeList l) {
		list=l;
	}

	/**
	 * <p>Construct a <code>CodedSequenceItem</code> from string values for code value, scheme and meaning.</p>
	 *
	 * @param	codeValue				the code value
	 * @param	codingSchemeDesignator	the coding scheme designator
	 * @param	codeMeaning				the code meaning
	 * @throws	DicomException			if error in DICOM encoding
	 */
	public CodedSequenceItem(String codeValue,String codingSchemeDesignator,String codeMeaning) throws DicomException {
		list = new AttributeList();
		{ Attribute a = new ShortStringAttribute(TagFromName.CodeValue); a.addValue(codeValue); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.CodingSchemeDesignator); a.addValue(codingSchemeDesignator); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.CodeMeaning); a.addValue(codeMeaning); list.put(a); }
	}

	/**
	 * <p>Construct a <code>CodedSequenceItem</code> from string values for code value, scheme, version and meaning.</p>
	 *
	 * @param	codeValue				the code value
	 * @param	codingSchemeDesignator	the coding scheme designator
	 * @param	codingSchemeVersion		the coding scheme version
	 * @param	codeMeaning				the code meaning
	 * @throws	DicomException			if error in DICOM encoding
	 */
	public CodedSequenceItem(String codeValue,String codingSchemeDesignator,String codingSchemeVersion,String codeMeaning) throws DicomException {
		list = new AttributeList();
		{ Attribute a = new ShortStringAttribute(TagFromName.CodeValue); a.addValue(codeValue); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.CodingSchemeDesignator); a.addValue(codingSchemeDesignator); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.CodingSchemeVersion); a.addValue(codingSchemeVersion); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.CodeMeaning); a.addValue(codeMeaning); list.put(a); }
	}

	/**
	 * <p>Construct a <code>CodedSequenceItem</code> from a single string representation of the tuple enclosed in parentheses.</p>
	 *
	 * <p>I.e., "(cv,csd,cm)" or "(cv,csd,csv,cm)".</p>
	 *
	 * <p>The supplied tuple is expected to be enclosed in parentheses.</p>
	 *
	 * <p>Any items of the tuple may be enclosed in double quotes.</p>
	 *
	 * <p>White space is ignored (outside quoted strings".</p>
	 *
	 * @param	tuple			single string representation of the tuple enclosed in parentheses.
	 * @throws	DicomException	if error in DICOM encoding
	 */
	public CodedSequenceItem(String tuple) throws DicomException {
		String codeValue = "";
		String codingSchemeDesignator = "";
		String codingSchemeVersion = "";
		String codeMeaning = "";
		
		Pattern pThreeTuple = Pattern.compile("[ ]*[(][ ]*\"?([^,\"]+)\"?[ ]*,[ ]*\"?([^,\"]+)\"?[ ]*,[ ]*\"?([^,\"]+)\"?[ ]*[)][ ]*");
		Matcher mThreeTuple = pThreeTuple.matcher(tuple);
		if (mThreeTuple.matches()) {
			int groupCount = mThreeTuple.groupCount();
			if (groupCount >= 3) {
				codeValue = mThreeTuple.group(1);				// NB. starts from 1 not 0
				codingSchemeDesignator = mThreeTuple.group(2);
				codeMeaning = mThreeTuple.group(3);
			}
		}
		else {
			Pattern pFourTuple = Pattern.compile("[ ]*[(][ ]*\"?([^,\"]+)\"?[ ]*,[ ]*\"?([^,\"]+)\"?[ ]*,[ ]*\"?([^,\"]+)\"?[ ]*,[ ]*\"?([^,\"]+)\"?[ ]*[)][ ]*");
			Matcher mFourTuple = pFourTuple.matcher(tuple);
			if (mFourTuple.matches()) {
				int groupCount = mFourTuple.groupCount();
				if (groupCount >= 4) {
					codeValue = mFourTuple.group(1);				// NB. starts from 1 not 0
					codingSchemeDesignator = mFourTuple.group(2);
					codingSchemeVersion = mFourTuple.group(3);
					codeMeaning = mFourTuple.group(4);
				}
			}
		}
		
		if (codeValue.length() > 0 && codingSchemeDesignator.length() > 0 && codeMeaning.length() > 0) {
			list = new AttributeList();
			{ Attribute a = new ShortStringAttribute(TagFromName.CodeValue); a.addValue(codeValue); list.put(a); }
			{ Attribute a = new ShortStringAttribute(TagFromName.CodingSchemeDesignator); a.addValue(codingSchemeDesignator); list.put(a); }
			if (codingSchemeVersion.length() > 0) { Attribute a = new ShortStringAttribute(TagFromName.CodingSchemeVersion); a.addValue(codingSchemeVersion); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.CodeMeaning); a.addValue(codeMeaning); list.put(a); }
		}
		else {
			throw new DicomException("Unable to recognize pattern of tuple");
		}
	}

	/**
	 * <p>Get the list of attributes in the <code>CodedSequenceItem</code>.</p>
	 *
	 * @return	all the attributes in the <code>CodedSequenceItem</code>
	 */
	public AttributeList getAttributeList() { return list; }

	/**
	 * <p>Get the code value.</p>
	 *
	 * @return	a string containing the code value, or an empty string if none
	 */
	public String getCodeValue() {
		return Attribute.getSingleStringValueOrEmptyString(list,TagFromName.CodeValue);
	}

	/**
	 * <p>Get the coding scheme designator.</p>
	 *
	 * @return	a string containing the coding scheme designator, or an empty string if none
	 */
	public String getCodingSchemeDesignator() {
		return Attribute.getSingleStringValueOrEmptyString(list,TagFromName.CodingSchemeDesignator);
	}

	/**
	 * <p>Get the coding scheme version.</p>
	 *
	 * @return	a string containing the coding scheme version, or an empty string if none
	 */
	public String getCodingSchemeVersion() {
		return Attribute.getSingleStringValueOrEmptyString(list,TagFromName.CodingSchemeVersion);
	}

	/**
	 * <p>Get the code meaning.</p>
	 *
	 * @return	a string containing the code meaning, or an empty string if none
	 */
	public String getCodeMeaning() {
		return Attribute.getSingleStringValueOrEmptyString(list,TagFromName.CodeMeaning);
	}

	/**
	 * <p>Get a {@link String String} representation of the contents of the <code>CodedSequenceItem</code>.</p>
	 *
	 * @return	a string containing the code value, coding scheme designator, coding scheme version (if present) and code meaning values
	 */
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("(");
		str.append(getCodeValue());
		str.append(",");
		str.append(getCodingSchemeDesignator());
		String version = getCodingSchemeVersion();
		if (version != null && version.length() > 0) {
			str.append(",");
			str.append(version);
		}
		str.append(",\"");
		str.append(getCodeMeaning());
		str.append("\")");
		return str.toString();
	}

	/**
	 * <p>Get a tab-delimited {@link String String} representation of the contents of the <code>CodedSequenceItem</code>.</p>
	 *
	 * @return	a string containing the code value, coding scheme designator, coding scheme version (if present) and code meaning values separated by tabs and without quotes
	 */
	public String toTabDelimitedString() {
		StringBuffer str = new StringBuffer();
		str.append(getCodeValue());
		str.append("\t");
		str.append(getCodingSchemeDesignator());
		String version = getCodingSchemeVersion();
		str.append("\t");
		if (version != null && version.length() > 0) {
			str.append(version);
		}
		str.append("\t");
		str.append(getCodeMeaning());
		return str.toString();
	}

	// Some static convenience methods ...

	/**
	 * <p>Extract the first (hopefully only) item of a coded sequence attribute contained
	 * within a list of attributes.</p>
	 *
	 * @param	list	the list in which to look for the Sequence attribute
	 * @param	tag	the tag of the Sequence attribute to extract
	 * @return		the (first) coded sequence item if found, otherwise null
	 */
	public static CodedSequenceItem getSingleCodedSequenceItemOrNull(AttributeList list,AttributeTag tag) {
		CodedSequenceItem value = null;
		if (list != null) {
			value = getSingleCodedSequenceItemOrNull(list.get(tag));
		}
		return value;
	}

	/**
	 * <p>Extract the first (hopefully only) item of a coded sequence attribute.</p>
	 *
	 * @param	a	the attribute
	 * @return		the (first) coded sequence item if found, otherwise null
	 */
	public static CodedSequenceItem getSingleCodedSequenceItemOrNull(Attribute a) {
		CodedSequenceItem value = null;
		if (a != null && a instanceof SequenceAttribute) {
			SequenceAttribute sa = (SequenceAttribute)(a);
			Iterator i = sa.iterator();
			if (i.hasNext()) {
				SequenceItem item = ((SequenceItem)i.next());
				if (item != null) value=new CodedSequenceItem(item.getAttributeList());
			}
		}
		return value;
	}

	/**
	 * <p>Extract the items of a coded sequence attribute contained
	 * within a list of attributes.</p>
	 *
	 * @param	list	the list in which to look for the Sequence attribute
	 * @param	tag	the tag of the Sequence attribute to extract
	 * @return		the coded sequence items if found, otherwise null
	 */
	public static CodedSequenceItem[] getArrayOfCodedSequenceItemsOrNull(AttributeList list,AttributeTag tag) {
		CodedSequenceItem[] values = null;
		if (list != null) {
			values = getArrayOfCodedSequenceItemsOrNull(list.get(tag));
		}
		return values;
	}

	/**
	 * <p>Extract the items of a coded sequence attribute.</p>
	 *
	 * @param	a	the attribute
	 * @return		the coded sequence items if found, otherwise null
	 */
	public static CodedSequenceItem[] getArrayOfCodedSequenceItemsOrNull(Attribute a) {
		ArrayList listOfItems = new ArrayList();
		if (a != null && a instanceof SequenceAttribute) {
			SequenceAttribute sa = (SequenceAttribute)(a);
			Iterator i = sa.iterator();
			if (i.hasNext()) {
				SequenceItem item = ((SequenceItem)i.next());
				if (item != null) {
					listOfItems.add(new CodedSequenceItem(item.getAttributeList()));
				}
			}
		}
		return listOfItems.size() == 0 ? null : (CodedSequenceItem[])(listOfItems.toArray());
	}

	/**
	 * <p>Create a single item coded sequence attribute and add it to the list.</p>
	 *
	 * @param	list					the AttributeList to which to add the new SequenceAttribute
	 * @param	tag						the AttributeTag of the SequenceAttribute to create
	 * @param	codeValue				the code value
	 * @param	codingSchemeDesignator	the coding scheme designator
	 * @param	codeMeaning				the code meaning
	 * @throws	DicomException			if error in DICOM encoding
	 */
	public static void putSingleCodedSequenceItem(AttributeList list,AttributeTag tag,String codeValue,String codingSchemeDesignator,String codeMeaning) throws DicomException {
		SequenceAttribute a = new SequenceAttribute(tag);
		list.put(a);
		{
			AttributeList itemList = new AttributeList();
			a.addItem(itemList);
			itemList.putAll(new CodedSequenceItem(codeValue,codingSchemeDesignator,codeMeaning).getAttributeList());
		}
	}

	/**
	 * <p>Create a single item coded sequence attribute and add it to the list.</p>
	 *
	 * @param	list	the AttributeList to which to add the new SequenceAttribute
	 * @param	tag		the AttributeTag of the SequenceAttribute to create
	 * @param	csi		the coded sequence item
	 * @return			the sequence attribute
	 */
	public static SequenceAttribute putSingleCodedSequenceAttribute(AttributeList list,AttributeTag tag,CodedSequenceItem csi) {
		SequenceAttribute a = newCodedSequenceAttribute(tag,csi);
		list.put(a);
		return a;
	}

	/**
	 * <p>Create a single item coded sequence attribute.</p>
	 *
	 * @param	tag		the AttributeTag of the SequenceAttribute to create
	 * @param	csi		the coded sequence item
	 * @return			the sequence attribute
	 */
	public static SequenceAttribute newCodedSequenceAttribute(AttributeTag tag,CodedSequenceItem csi) {
		SequenceAttribute a = new SequenceAttribute(tag);
		{
			AttributeList itemList = new AttributeList();
			a.addItem(itemList);
			itemList.putAll(csi.getAttributeList());
		}
		return a;
	}
	
	/**
	 * <p>Find the item of a SequenceAttribute that contains a coded sequence attribute with a specified value.</p>
	 *
	 * @param	a						the SequenceAttribute whose items are to be searched
	 * @param	codedSequenceItemTag	the CodedSequenceItem Attribute wanted
	 * @param	wanted					the value of the CodedSequenceItem wanted
	 * @return							the item number (from 0) or -1 if not found
	 */
	public static int getItemNumberContainingCodeSequence(SequenceAttribute a,AttributeTag codedSequenceItemTag, CodedSequenceItem wanted) {
		int itemNumber = -1;
		if (a != null) {
			int n = a.getNumberOfItems();
			for (int i=0; i<n; ++i) {
				SequenceItem item = a.getItem(i);
				if (item != null) {
					AttributeList itemList = item.getAttributeList();
					if (itemList != null) {
						CodedSequenceItem test = getSingleCodedSequenceItemOrNull(itemList,codedSequenceItemTag);
						if (test != null && test.equals(wanted)) {
							itemNumber = i;
							break;
						}
					}
				}
			}
		}
		return itemNumber;
	}
	
	/**
	 * <p>Find the item of a SequenceAttribute in an AttributeList that contains a coded sequence attribute with a specified value.</p>
	 *
	 * @param	list					the AttributeList in which to look for the SequenceAttribute
	 * @param	sequenceAttributeTag	the SequenceAttribute whose items are to be searched
	 * @param	codedSequenceItemTag	the CodedSequenceItem Attribute wanted
	 * @param	wanted					the value of the CodedSequenceItem wanted
	 * @return							the item number (from 0) or -1 if not found
	 */

	public static int getItemNumberContainingCodeSequence(AttributeList list,AttributeTag sequenceAttributeTag,AttributeTag codedSequenceItemTag,CodedSequenceItem wanted) {
		int itemNumber = -1;
		SequenceAttribute a = (SequenceAttribute)(list.get(sequenceAttributeTag));
		if (a != null) {
			itemNumber = getItemNumberContainingCodeSequence(a,codedSequenceItemTag,wanted);
		}
		return itemNumber;
	}

}

