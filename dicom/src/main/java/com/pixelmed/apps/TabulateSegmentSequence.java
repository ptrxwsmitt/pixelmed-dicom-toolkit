/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.apps;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.CodedSequenceItem;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.TagFromName;

import com.pixelmed.utils.ColorUtilities;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Iterator;

/**
 * <p>A utility class with a main method to read a DICOM segmentation object and tabulate the description of the segments.</p>
 *
 * @author	dclunie
 */
public class TabulateSegmentSequence {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/apps/TabulateSegmentSequence.java,v 1.7 2022/01/21 19:51:12 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(TabulateSegmentSequence.class);
	
	public static void tabulateSegmentSequence(String inputfilename,PrintStream out) throws IOException, FileNotFoundException, NumberFormatException, DicomException {
		AttributeList list = new AttributeList();
		list.read(inputfilename,TagFromName.PixelData);
		SequenceAttribute aSegmentSequence = (SequenceAttribute)list.get(TagFromName.SegmentSequence);
		if (aSegmentSequence != null) {
            out.println("number\tlabel\talg type\ttrackingID\ttrackingUID\tgray\tCIEL\tCIEa\tCIEb\tR\tG\trB\tanat reg CV\tanat reg CSD\tanat reg CSV\tanat reg CM\tanat reg mod CV\tanat reg mod CSD\tanat reg mod CSV\tanat reg mod CM\tseg ctgry CV\tseg ctgry CSD\tseg ctgry CSV\tseg ctgry CM\tseg type CV\tseg type CSD\tseg type CSV\tseg type CM\tsegtype mod CV\tsegtype mod CSD\tsegtype mod CSV\tsegtype mod CM");
			Iterator<SequenceItem> i = aSegmentSequence.iterator();
			while (i.hasNext()) {
				SequenceItem item = i.next();
				if (item != null) {
					AttributeList silist = item.getAttributeList();
					if (silist != null) {
						String segmentNumber = Attribute.getSingleStringValueOrEmptyString(silist,TagFromName.SegmentNumber);
						String segmentLabel = Attribute.getSingleStringValueOrEmptyString(silist,TagFromName.SegmentLabel);
						String segmentAlgorithmType = Attribute.getSingleStringValueOrEmptyString(silist,TagFromName.SegmentAlgorithmType);
						
						// assume only one anatomnic region even though allowed to be multiple since CP 1591 :(
						String anatomicRegionSequenceValue = "\t\t\t";
						String anatomicRegionModifierSequenceValue = "\t\t\t";
						{
							SequenceAttribute aAnatomicRegionSequence = (SequenceAttribute)silist.get(TagFromName.AnatomicRegionSequence);
							if (aAnatomicRegionSequence != null) {
								CodedSequenceItem anatomicRegionSequence = CodedSequenceItem.getSingleCodedSequenceItemOrNull(aAnatomicRegionSequence);
								if (anatomicRegionSequence != null) {
									anatomicRegionSequenceValue =  anatomicRegionSequence.toTabDelimitedString();
								}
								SequenceAttribute aAnatomicRegionModifierSequence = (SequenceAttribute)SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aAnatomicRegionSequence,TagFromName.AnatomicRegionModifierSequence);
								if (aAnatomicRegionModifierSequence != null) {
									CodedSequenceItem anatomicRegionModifierSequence = CodedSequenceItem.getSingleCodedSequenceItemOrNull(aAnatomicRegionModifierSequence);
									if (anatomicRegionModifierSequence != null) {
										anatomicRegionModifierSequenceValue =  anatomicRegionModifierSequence.toTabDelimitedString();
									}
								}
							}
						}
						
						String segmentedPropertyCategoryCodeSequenceValue = "\t\t\t";
						{
							CodedSequenceItem segmentedPropertyCategoryCodeSequence = CodedSequenceItem.getSingleCodedSequenceItemOrNull(silist,TagFromName.SegmentedPropertyCategoryCodeSequence);
							if (segmentedPropertyCategoryCodeSequence != null) {
								segmentedPropertyCategoryCodeSequenceValue =  segmentedPropertyCategoryCodeSequence.toTabDelimitedString();
							}
						}
						String segmentedPropertyTypeCodeSequenceValue = "\t\t\t";
						String segmentedPropertyTypeModifierCodeSequenceValue = "\t\t\t";
						{
							SequenceAttribute aSegmentedPropertyTypeCodeSequence = (SequenceAttribute)silist.get(TagFromName.SegmentedPropertyTypeCodeSequence);
							if (aSegmentedPropertyTypeCodeSequence != null) {
								CodedSequenceItem segmentedPropertyTypeCodeSequence = CodedSequenceItem.getSingleCodedSequenceItemOrNull(aSegmentedPropertyTypeCodeSequence);
								if (segmentedPropertyTypeCodeSequence != null) {
									segmentedPropertyTypeCodeSequenceValue =  segmentedPropertyTypeCodeSequence.toTabDelimitedString();
								}
								SequenceAttribute aSegmentedPropertyTypeModifierCodeSequence = (SequenceAttribute)SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(aSegmentedPropertyTypeCodeSequence,TagFromName.SegmentedPropertyTypeModifierCodeSequence);
								if (aSegmentedPropertyTypeModifierCodeSequence != null) {
									CodedSequenceItem segmentedPropertyTypeModifierCodeSequence = CodedSequenceItem.getSingleCodedSequenceItemOrNull(aSegmentedPropertyTypeModifierCodeSequence);
									if (segmentedPropertyTypeModifierCodeSequence != null) {
										segmentedPropertyTypeModifierCodeSequenceValue =  segmentedPropertyTypeModifierCodeSequence.toTabDelimitedString();
									}
								}
							}
						}

						String trackingID = Attribute.getSingleStringValueOrEmptyString(silist,TagFromName.TrackingID);
						String trackingUID = Attribute.getSingleStringValueOrEmptyString(silist,TagFromName.TrackingUID);
						
						String recommendedDisplayGrayscaleValue = Attribute.getSingleStringValueOrEmptyString(silist,TagFromName.RecommendedDisplayGrayscaleValue);
						
						String recommendedDisplayCIELValue = "";
						String recommendedDisplayCIEaValue = "";
						String recommendedDisplayCIEbValue = "";
						String recommendedDisplayRValue = "";
						String recommendedDisplayGValue = "";
						String recommendedDisplayBValue = "";
						String[] recommendedDisplayCIELabValues = Attribute.getStringValues(silist,TagFromName.RecommendedDisplayCIELabValue);
						if (recommendedDisplayCIELabValues != null && recommendedDisplayCIELabValues.length == 3) {
							recommendedDisplayCIELValue = recommendedDisplayCIELabValues[0];
							recommendedDisplayCIEaValue = recommendedDisplayCIELabValues[1];
							recommendedDisplayCIEbValue = recommendedDisplayCIELabValues[2];
							
							int[] cieLabScaled = new int[3];
							cieLabScaled[0] = Integer.parseInt(recommendedDisplayCIELValue);
							cieLabScaled[1] = Integer.parseInt(recommendedDisplayCIEaValue);
							cieLabScaled[2] = Integer.parseInt(recommendedDisplayCIEbValue);
							int[] sRGB = ColorUtilities.getSRGBFromIntegerScaledCIELabPCS(cieLabScaled);
							recommendedDisplayRValue = Integer.toString(sRGB[0]);
							recommendedDisplayGValue = Integer.toString(sRGB[1]);
							recommendedDisplayBValue = Integer.toString(sRGB[2]);
						}

						out.println(segmentNumber+"\t"+segmentLabel+"\t"+segmentAlgorithmType+"\t"+trackingID+"\t"+trackingUID+"\t"+recommendedDisplayGrayscaleValue+"\t"+recommendedDisplayCIELValue+"\t"+recommendedDisplayCIEaValue+"\t"+recommendedDisplayCIEbValue+"\t"+recommendedDisplayRValue+"\t"+recommendedDisplayGValue+"\t"+recommendedDisplayBValue+"\t"+anatomicRegionSequenceValue+"\t"+anatomicRegionModifierSequenceValue+"\t"+segmentedPropertyCategoryCodeSequenceValue+"\t"+segmentedPropertyTypeCodeSequenceValue+"\t"+segmentedPropertyTypeModifierCodeSequenceValue);
					}
				}
			}
		}
		else {
			throw new DicomException("Missing SegmentSequence");
		}
	}

	/**
	 * <p>Read a DICOM segmentation object and tabulate the description of the segments.</p>
	 *
	 * @param	arg	one parameter, the inputFile
	 */
	public static void main(String arg[]) {
		try {
			if (arg.length >= 1) {
				tabulateSegmentSequence(arg[0],System.out);
			}
			else {
				System.err.println("Error: Incorrect number of arguments");
				System.err.println("Usage: TabulateSegmentSequence inputFile");
				System.exit(1);
			}
		}
		catch (Exception e) {
			slf4jlogger.error("",e);	// use SLF4J since may be invoked from script
		}
	}
}

