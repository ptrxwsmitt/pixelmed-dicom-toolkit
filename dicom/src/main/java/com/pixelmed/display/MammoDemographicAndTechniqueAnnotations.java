/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.display;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TagFromName;

/**
 * <p>A class to extract selected DICOM annotative attributes into defined displayed area relative positions for mammograms.</p>
 *
 * @author	dclunie
 */
public class MammoDemographicAndTechniqueAnnotations extends DemographicAndTechniqueAnnotations {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/display/MammoDemographicAndTechniqueAnnotations.java,v 1.25 2022/08/11 11:36:18 dclunie Exp $";
	
	private static final DicomDictionary dictionary = DicomDictionary.StandardDictionary;

	protected void initializeDefaultLayout() {
		layout=new Vector();
		
		// top
		
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null,"Patient [",true,true,0,0,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.PatientID,null,null,null,true,true,0,1,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null,"] ",true,true,0,2,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.PatientName,null,null,null,true,true,0,3,null,NOSPECIAL));
		
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null,"Study# ",true,true,1,0,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.StudyID,null,null,null,true,true,1,1,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," Acc# ",true,true,1,2,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.AccessionNumber,null,null,null,true,true,1,3,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," Series# ",true,true,1,4,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.SeriesNumber,null,null,null,true,true,1,5,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," Image# ",true,true,1,6,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.InstanceNumber,null,null,null,true,true,1,7,null,NOSPECIAL));
		
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null,"Acquired ",true,true,2,0,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.AcquisitionDate,null,null,null,true,true,2,1,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," ",true,true,2,3,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.AcquisitionTime,null,null,null,true,true,2,4,null,NOSPECIAL));

		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.PatientSex,null,null,null,true,true,3,0,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," DOB ",true,true,3,1,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.PatientBirthDate,null,null,null,true,true,3,2,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," Age ",true,true,3,3,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.PatientAge,null,null,null,true,true,3,4,null,NOSPECIAL));

		// bottom
		
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.Manufacturer,null,null,null,true,false,0,0,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," ",true,false,0,1,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.ManufacturerModelName,null,null,null,true,false,0,2,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," SN# ",true,false,0,3,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.DeviceSerialNumber,null,null,null,true,false,0,4,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," Det# ",true,false,0,5,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("DetectorID"),null,null,null,true,false,0,6,null,NOSPECIAL));

		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.InstitutionName,null,null,null,true,false,1,0,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," ",true,true,1,1,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.InstitutionAddress,null,null,null,true,false,1,2,null,NOSPECIAL));
		
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("FilterMaterial"),null,null,null,true,false,2,0,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null,"/",true,false,2,1,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("AnodeTargetMaterial"),null,null,null,true,false,2,2,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," ",true,false,2,3,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("CompressionForce"),null,null,null,true,false,2,4,"#.#",NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," N ",true,false,2,5,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("BodyPartThickness"),null,null,null,true,false,2,6,"#.#",NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," mm ",true,false,2,7,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("PositionerPrimaryAngle"),null,null,null,true,false,2,8,"#.#",NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," deg ",true,false,2,9,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("RelativeXRayExposure"),null,null,null,true,false,2,10,"#.#",NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," exp",true,false,2,11,null,NOSPECIAL));
		
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("KVP"),null,null,null,true,false,3,0,"#.#",NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," kVP ",true,false,3,1,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("Exposure"),null,null,null,true,false,3,3,"#.#",NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," mAs ",true,false,3,4,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("ExposureTime"),null,null,null,true,false,3,5,"#.#",NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," s Breast ",true,false,3,6,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("OrganDose"),null,null,null,true,false,3,7,"#.###",NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," dGy ESD ",true,false,3,8,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("EntranceDoseInmGy"),null,null,null,true,false,3,9,"#.#",NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," mGy",true,false,3,10,null,NOSPECIAL));

		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null,"Operator ",true,false,4,0,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.OperatorsName,null,null,null,true,false,4,1,null,NOSPECIAL));

		layout.add(new AnnotationLayoutConfigurationEntry(null,TagFromName.ReasonForRequestedProcedureCodeSequence,null,TagFromName.RequestAttributesSequence,null,true,false,5,0,null,CODEMEANING));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," ",true,false,5,1,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("PartialView"),null,null,"PARTIAL",true,false,5,2,null,TEXTIFYESNO));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," ",true,false,5,3,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("PartialViewCodeSequence"),null,null,null,true,false,5,4,null,CODEMEANING));
		layout.add(new AnnotationLayoutConfigurationEntry(null,null,null,null," ",true,false,5,5,null,NOSPECIAL));
		layout.add(new AnnotationLayoutConfigurationEntry(null,dictionary.getTagFromName("BreastImplantPresent"),null,null,"IMPLANT",true,false,5,6,null,TEXTIFYESNO));
	}

	/**
	 * <p>Return an abbreviation for a mammography view.</p>
	 *
	 * @param	list	the attributes of an item of ViewCodeSequence
	 * @return			a string value with an ACR/DICOM/IHE specified abbreviation
	 */
	protected static String getViewAbbreviationFromViewCodeSequenceAttributes(AttributeList list) {
		String abbreviation = "";
		if (list != null) {
			String codingSchemeDesignator = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.CodingSchemeDesignator);
			String codeValue = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.CodeValue);
			if (codingSchemeDesignator.equals("SCT")) {
				if (codeValue.equals("399260004")) {
					abbreviation = "ML";
				}
				else if (codeValue.equals("399368009")) {
					abbreviation = "MLO";
				}
				else if (codeValue.equals("399352003")) {
					abbreviation = "LM";
				}
				else if (codeValue.equals("399099002")) {
					abbreviation = "LMO";
				}
				else if (codeValue.equals("399162004")) {
					abbreviation = "CC";
				}
				else if (codeValue.equals("399196006")) {
					abbreviation = "FB";
				}
				else if (codeValue.equals("399188001")) {
					abbreviation = "SIO";
				}
				else if (codeValue.equals("441555000")) {
					abbreviation = "ISO";
				}
				else if (codeValue.equals("399265009")) {
					abbreviation = "XCC";
				}
				else if (codeValue.equals("399192008")) {
					abbreviation = "XCCL";
				}
				else if (codeValue.equals("399101009")) {
					abbreviation = "XCCM";
				}
			}
			else if (codingSchemeDesignator.equals("SNM3") || codingSchemeDesignator.equals("SRT")) {
				if (codeValue.equals("R-10224")) {
					abbreviation = "ML";
				}
				else if (codeValue.equals("R-10226")) {
					abbreviation = "MLO";
				}
				else if (codeValue.equals("R-10228")) {
					abbreviation = "LM";
				}
				else if (codeValue.equals("R-10230")) {
					abbreviation = "LMO";
				}
				else if (codeValue.equals("R-10242")) {
					abbreviation = "CC";
				}
				else if (codeValue.equals("R-10244")) {
					abbreviation = "FB";
				}
				else if (codeValue.equals("R-102D0")) {
					abbreviation = "SIO";
				}
				else if (codeValue.equals("R-40AAA")) {
					abbreviation = "ISO";
				}
				else if (codeValue.equals("R-102CF")) {
					abbreviation = "XCC";
				}
				else if (codeValue.equals("R-1024A") || codeValue.equals("Y-X1770")) {
					abbreviation = "XCCL";
				}
				else if (codeValue.equals("R-1024B") || codeValue.equals("Y-X1771")) {
					abbreviation = "XCCM";
				}
			}
		}
		return abbreviation;
	}

	/**
	 * <p>Return an abbreviation for a mammography view modifer.</p>
	 *
	 * @param	list	the attributes of an item of ViewModifierCodeSequence
	 * @return			a string value with an ACR/DICOM/IHE specified abbreviation, including a leading "..." or trailing "..." to indicate prefix or suffix, or neither if a replacement
	 */
	protected static String getViewModifierAbbreviationFromViewModifierCodeSequenceAttributes(AttributeList list) {
		String abbreviation = "";
		if (list != null) {
			String codingSchemeDesignator = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.CodingSchemeDesignator);
			String codeValue = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.CodeValue);
			if (codingSchemeDesignator.equals("SCT")) {
				if (codeValue.equals("399161006")) {
					abbreviation = "CV";
				}
				else if (codeValue.equals("399011000")) {
					abbreviation = "AT";
				}
				else if (codeValue.equals("399197002")) {
					abbreviation = "...RL";
				}
				else if (codeValue.equals("399226006")) {
					abbreviation = "...RM";
				}
				else if (codeValue.equals("414493004")) {
					abbreviation = "...RI";
				}
				else if (codeValue.equals("415670009")) {
					abbreviation = "...RS";
				}
				else if (codeValue.equals("399209000")) {
					abbreviation = "...ID";
				}
				else if (codeValue.equals("442581004")) {
					abbreviation = "...NP";
				}
				else if (codeValue.equals("441752004")) {
					abbreviation = "...AC";
				}
				else if (codeValue.equals("442593008")) {
					abbreviation = "...IMF";
				}
				else if (codeValue.equals("442580003")) {
					abbreviation = "...AX";
				}
				else if (codeValue.equals("399163009")) {
					abbreviation = "M...";
				}
				else if (codeValue.equals("399055006")) {
					abbreviation = "S...";
				}
				else if (codeValue.equals("399110001")) {
					abbreviation = "TAN";
				}
			}
			else if (codingSchemeDesignator.equals("SNM3") || codingSchemeDesignator.equals("SRT")) {
				if (codeValue.equals("R-102D2")) {
					abbreviation = "CV";
				}
				else if (codeValue.equals("R-102D1")) {
					abbreviation = "AT";
				}
				else if (codeValue.equals("R-102D3")) {
					abbreviation = "...RL";
				}
				else if (codeValue.equals("R-102D4")) {
					abbreviation = "...RM";
				}
				else if (codeValue.equals("R-102CA")) {
					abbreviation = "...RI";
				}
				else if (codeValue.equals("R-102C9")) {
					abbreviation = "...RS";
				}
				else if (codeValue.equals("R-102D5")) {
					abbreviation = "...ID";
				}
				else if (codeValue.equals("R-40AB3")) {
					abbreviation = "...NP";
				}
				else if (codeValue.equals("P2-00161")) {
					abbreviation = "...AC";
				}
				else if (codeValue.equals("R-40ABE")) {
					abbreviation = "...IMF";
				}
				else if (codeValue.equals("R-40AB2")) {
					abbreviation = "...AX";
				}
				else if (codeValue.equals("R-102D6")) {
					abbreviation = "M...";
				}
				else if (codeValue.equals("R-102D7")) {
					abbreviation = "S...";
				}
				else if (codeValue.equals("R-102C2")) {
					abbreviation = "TAN";
				}
			}
		}
		return abbreviation;
	}

	/**
	 * <p>Return the most specific frame, image or laterality.</p>
	 *
	 * @param	list
	 * @return			a single String value, null if cannot be obtained
	 */
	public static String getLaterality(AttributeList list) {
		String laterality = null;
		{
			SequenceAttribute sharedattr = (SequenceAttribute)list.get(TagFromName.SharedFunctionalGroupsSequence);	// may be null if absent
			AttributeList sharedlist = SequenceAttribute.getAttributeListFromSelectedItemWithinSequence(sharedattr,0);
			if (sharedlist != null) {
				laterality = SequenceAttribute.getSingleStringValueOfNamedAttributeFromWithinSequenceWithSingleItemOrDefault(sharedlist,TagFromName.FrameAnatomySequence,TagFromName.FrameLaterality,null);
			}
		}
		if (laterality == null || laterality.length() == 0) {
			laterality = Attribute.getSingleStringValueOrNull(list,TagFromName.ImageLaterality);
		}
		if (laterality == null || laterality.length() == 0) {
			laterality = Attribute.getSingleStringValueOrNull(list,TagFromName.Laterality);
		}
		return laterality;
	}

	/**
	 * <p>Return an abbreviation for laterality, view and view modifier.</p>
	 *
	 * @param	list	
	 * @return			a string value with an ACR/DICOM/IHE specified abbreviation
	 */
	public static String getAbbreviationFromImageLateralityViewModifierAndViewModifierCodeSequenceAttributes(AttributeList list) {
		StringBuffer buf = new StringBuffer();
		{
			String laterality = getLaterality(list);
			if (laterality != null) {
				buf.append(laterality);
			}
		}
		Attribute aViewCodeSequence = list.get(TagFromName.ViewCodeSequence);
		if (aViewCodeSequence != null && aViewCodeSequence instanceof SequenceAttribute) {
			SequenceAttribute saViewCodeSequence = (SequenceAttribute)aViewCodeSequence;
			if (saViewCodeSequence.getNumberOfItems() > 0) {
				SequenceItem iViewCodeSequence = saViewCodeSequence.getItem(0);
				if (iViewCodeSequence != null) {
					AttributeList alViewCodeSequence = iViewCodeSequence.getAttributeList();
					if (alViewCodeSequence != null) {
						String viewPrimary = getViewAbbreviationFromViewCodeSequenceAttributes(alViewCodeSequence);
						TreeSet viewPrefixes = new TreeSet();
						TreeSet viewSuffixes = new TreeSet();
						Attribute aViewModifierCodeSequence = alViewCodeSequence.get(TagFromName.ViewModifierCodeSequence);
						if (aViewModifierCodeSequence != null && aViewModifierCodeSequence instanceof SequenceAttribute) {
							SequenceAttribute saViewModifierCodeSequence = (SequenceAttribute)aViewModifierCodeSequence;
							Iterator sitems = saViewModifierCodeSequence.iterator();
							while (sitems.hasNext()) {
								SequenceItem sitem = (SequenceItem)sitems.next();
								AttributeList alViewModifierCodeSequence = sitem.getAttributeList();
								String viewModifier = getViewModifierAbbreviationFromViewModifierCodeSequenceAttributes(alViewModifierCodeSequence);
								if (viewModifier.endsWith("...")) {
									viewPrefixes.add(viewModifier.substring(0,viewModifier.length()-3));
								}
								else if (viewModifier.startsWith("...")) {
									viewSuffixes.add(viewModifier.substring(3,viewModifier.length()));
								}
								else if (viewModifier.length() > 0) {
									viewPrimary = viewModifier;		// replace if not empty, prefix or suffix
								}
							}
						}
						Iterator i = viewPrefixes.iterator();
						while (i.hasNext()) {
							buf.append((String)i.next());
						}
						buf.append(viewPrimary);
						i = viewSuffixes.iterator();
						while (i.hasNext()) {
							buf.append((String)i.next());
						}
					}
				}
			}
		}
		return buf.toString();
	}

	/**
	 * @param	list			the DICOM attributes of a single or multi-frame image
	 * @param	leftSide		whether the side to annotate (the side opposite the chest wall) is left (true) or right (false)
	 */
	public MammoDemographicAndTechniqueAnnotations(AttributeList list,boolean leftSide) {
		super(list,null,leftSide);
	}
}

