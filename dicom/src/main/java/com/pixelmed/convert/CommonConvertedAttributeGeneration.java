/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.convert;

import com.pixelmed.dicom.*;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class to support converting proprietary image input format files into images of a specified or appropriate SOP Class.</p>
 *
 * @author	dclunie
 */

public class CommonConvertedAttributeGeneration {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/convert/CommonConvertedAttributeGeneration.java,v 1.29 2024/02/15 14:04:15 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(CommonConvertedAttributeGeneration.class);

	private static void addParametricMapFrameTypeSharedFunctionalGroup(AttributeList list) throws DicomException {
		Attribute aFrameType = new CodeStringAttribute(TagFromName.FrameType);
		aFrameType.addValue("DERIVED");
		aFrameType.addValue("PRIMARY");
		aFrameType.addValue("");		// do not have a value for Image Flavor, but value is required to be present
		aFrameType.addValue("");		// do not have a value for Derived Pixel Contrast, but value is required to be present
		FunctionalGroupUtilities.generateFrameTypeSharedFunctionalGroup(list,TagFromName.ParametricMapFrameTypeSequence,aFrameType);
	}
	
	private static void addModalitySpecificFrameTypeSharedFunctionalGroup(AttributeList list,String modality) throws DicomException {
		AttributeTag frameTypeSequenceTag = null;
		if (modality != null) {
			switch (modality) {
				case "CT":	frameTypeSequenceTag = TagFromName.CTImageFrameTypeSequence; break;
				case "MR":	frameTypeSequenceTag = TagFromName.MRImageFrameTypeSequence; break;
				case "PT":	frameTypeSequenceTag = TagFromName.PETFrameTypeSequence; break;
				case "DX":	frameTypeSequenceTag = TagFromName.XRay3DFrameTypeSequence; break;
				case "MG":	frameTypeSequenceTag = TagFromName.XRay3DFrameTypeSequence; break;
				case "XA":	frameTypeSequenceTag = TagFromName.XRay3DFrameTypeSequence; break;
			}
		}
		if (frameTypeSequenceTag != null) {
			Attribute aFrameType = new CodeStringAttribute(TagFromName.FrameType);
			aFrameType.addValue("DERIVED");
			aFrameType.addValue("PRIMARY");
			aFrameType.addValue("VOLUME");
			aFrameType.addValue("NONE");
			FunctionalGroupUtilities.generateFrameTypeSharedFunctionalGroup(list,frameTypeSequenceTag,aFrameType);
			{
				AttributeList macroList = ((SequenceAttribute)SequenceAttribute.getNamedAttributeFromWithinSelectedItemWithinSequence(list,TagFromName.SharedFunctionalGroupsSequence,0,frameTypeSequenceTag)).getItem(0).getAttributeList();
				{ Attribute a = new CodeStringAttribute(TagFromName.PixelPresentation);               a.addValue("MONOCHROME"); macroList.put(a); }		// should we bother checking PhotometricInterpretation ?
				{ Attribute a = new CodeStringAttribute(TagFromName.VolumetricProperties);            a.addValue("VOLUME");     macroList.put(a); }		// no way to check that we are something funky like a MIP
				{ Attribute a = new CodeStringAttribute(TagFromName.VolumeBasedCalculationTechnique); a.addValue("NONE");       macroList.put(a); }		// no information
			}
		}
	}
	
	/**
	 * <p>Select a suitable SOP Class based on modality, number of frames and pixel data characteristics.</p>
	 *
	 * <p>If multi-frame selects legacy converted rather than true enhanced multi-frame SOP Classes.</p>
	 *
	 * <p>Worst case returns the Secondary Capture Image Storage SOP Class.</p>
	 *
	 * @param	modality			may be null or empty string
	 * @param	numberOfFrames
	 * @param	samplesPerPixel
	 * @param	bitsAllocated
	 * @param	isFloatPixelData
	 * @return						the selected SOP Class (never null or empty)
	 */
	public static String selectSOPClassForModalityNumberOfFramesAndPixelCharacteristics(String modality,int numberOfFrames,int samplesPerPixel,int bitsAllocated,boolean isFloatPixelData) {		// (001239)
		slf4jlogger.trace("selectSOPClassForModalityNumberOfFramesAndPixelCharacteristics(): modality = {}",modality);
		slf4jlogger.trace("selectSOPClassForModalityNumberOfFramesAndPixelCharacteristics(): numberOfFrames = {}",numberOfFrames);
		slf4jlogger.trace("selectSOPClassForModalityNumberOfFramesAndPixelCharacteristics(): samplesPerPixel = {}",samplesPerPixel);
		slf4jlogger.trace("selectSOPClassForModalityNumberOfFramesAndPixelCharacteristics(): bitsAllocated = {}",bitsAllocated);
		slf4jlogger.trace("selectSOPClassForModalityNumberOfFramesAndPixelCharacteristics(): isFloatPixelData = {}",isFloatPixelData);

		String sopClass = null;
		if (isFloatPixelData) {
			sopClass = SOPClass.ParametricMapStorage;
		}
		else if (modality != null && modality.length() > 0) {
			if (numberOfFrames > 1) {
				switch (modality) {
					case "CT":	sopClass = SOPClass.LegacyConvertedEnhancedCTImageStorage; break;
					case "MR":	sopClass = SOPClass.LegacyConvertedEnhancedMRImageStorage; break;
					case "PT":	sopClass = SOPClass.LegacyConvertedEnhancedPETImageStorage; break;
					case "MG":	sopClass = SOPClass.BreastTomosynthesisImageStorage; break;
					case "SM":	sopClass = SOPClass.VisibleLightSlideCoordinatesMicroscopicImageStorage; break;	// (001388)
					case "GM":	sopClass = SOPClass.VisibleLightMicroscopicImageStorage; break;	// (001388)
					// no default ... fall through to SecondaryCapture assignment later (001388)
				}
			}
			else {
				switch (modality) {
					case "CT":	sopClass = SOPClass.CTImageStorage; break;
					case "MR":	sopClass = SOPClass.MRImageStorage; break;
					case "PT":	sopClass = SOPClass.PETImageStorage; break;
					case "MG":	sopClass = SOPClass.DigitalMammographyXRayImageStorageForPresentation; break;
					case "SM":	sopClass = SOPClass.VLWholeSlideMicroscopyImageStorage; break;	// (001388)
					// no default ... fall through to SecondaryCapture assignment later (001388)
				}
			}
		}
		// Use a SecondaryCapture SOP Class if nothing else recognized (001388)
		if (sopClass == null) {
			sopClass = SOPClass.SecondaryCaptureImageStorage;
			if (numberOfFrames > 1) {
				if (samplesPerPixel == 1) {
					if (bitsAllocated == 8) {
						sopClass = SOPClass.MultiframeGrayscaleByteSecondaryCaptureImageStorage;
					}
					else if (bitsAllocated == 16) {
						sopClass = SOPClass.MultiframeGrayscaleWordSecondaryCaptureImageStorage;
					}
				}
				else if (samplesPerPixel == 3) {
					sopClass = SOPClass.MultiframeTrueColorSecondaryCaptureImageStorage;
				}
				// no current mechanism for creating MultiframeSingleBitSecondaryCaptureImageStorage, only 8 or 16 :(
			}
		}
		slf4jlogger.trace("selectSOPClassForModalityNumberOfFramesAndPixelCharacteristics(): SOP Class = {}",sopClass);
		return sopClass;
	}
	
	/**
	 * <p>Generate common attributes for converted images.</p>
	 *
	 * <p>Does NOT add ManufacturerModelName ... that should be added by caller.</p>
	 *
	 * <p>Does NOT call CodingSchemeIdentification.replaceCodingSchemeIdentificationSequenceWithCodingSchemesUsedInAttributeList ... that should be done by caller.</p>
	 *
	 * <p>Creates some Series and Equipment IE Attributes, so caller needs to override these if multiple Instances in Series to assure common values.</p>
	 *
	 * @param	list
	 * @param	patientName
	 * @param	patientID
	 * @param	studyID
	 * @param	seriesNumber
	 * @param	instanceNumber
	 * @param	modality	may be null
	 * @param	sopClass	may be null
	 * @param	generateUnassignedConverted	whether or not to generate empty Unassigned Converted Attributes Sequences (populates Shared and Per-Frame Functional Groups)
	 * @throws			DicomException
	 * @throws			NumberFormatException
	 */
	public static void generateCommonAttributes(AttributeList list,String patientName,String patientID,String studyID,String seriesNumber,String instanceNumber,String modality,String sopClass,boolean generateUnassignedConverted) throws  DicomException {
		UIDGenerator u = new UIDGenerator();
	
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(u.getAnotherNewUID()); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(u.getAnotherNewUID()); list.put(a); }
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(u.getAnotherNewUID()); list.put(a); }

		{ Attribute a = new PersonNameAttribute(TagFromName.PatientName); a.addValue(patientName); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.PatientID); a.addValue(patientID); list.put(a); }
		{ Attribute a = new DateAttribute(TagFromName.PatientBirthDate); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.PatientSex); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.StudyID); a.addValue(studyID); list.put(a); }
		{ Attribute a = new PersonNameAttribute(TagFromName.ReferringPhysicianName); a.addValue("^^^^"); list.put(a); }
		{ Attribute a = new ShortStringAttribute(TagFromName.AccessionNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.SeriesNumber); a.addValue(seriesNumber); list.put(a); }
		{ Attribute a = new IntegerStringAttribute(TagFromName.InstanceNumber); a.addValue(instanceNumber); list.put(a); }
		
		{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); a.addValue("PixelMed"); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.DeviceSerialNumber); a.addValue(new java.rmi.dgc.VMID().toString()); list.put(a); }
		{ Attribute a = new LongStringAttribute(TagFromName.SoftwareVersions); a.addValue(VersionAndConstants.getBuildDate()); list.put(a); }
		
		{ Attribute a = new CodeStringAttribute(TagFromName.BurnedInAnnotation); a.addValue("NO"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.RecognizableVisualFeatures); a.addValue("NO"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(TagFromName.ContentQualification); a.addValue("RESEARCH"); list.put(a); }
		
		{
			java.util.Date currentDateTime = new java.util.Date();
			String currentDate = new java.text.SimpleDateFormat("yyyyMMdd").format(currentDateTime);
			String currentTime = new java.text.SimpleDateFormat("HHmmss.SSS").format(currentDateTime);
			{ Attribute a = new DateAttribute(TagFromName.StudyDate);						a.addValue(currentDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.StudyTime);						a.addValue(currentTime); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.SeriesDate);						a.addValue(currentDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);						a.addValue(currentTime); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.ContentDate);						a.addValue(currentDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime);						a.addValue(currentTime); list.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.InstanceCreationDate);			a.addValue(currentDate); list.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.InstanceCreationTime);			a.addValue(currentTime); list.put(a); }
			{ Attribute a = new ShortStringAttribute(TagFromName.TimezoneOffsetFromUTC);	a.addValue(DateTimeAttribute.getTimeZone(java.util.TimeZone.getDefault(),currentDateTime)); list.put(a); }
		}
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.InstanceCreatorUID); a.addValue(VersionAndConstants.instanceCreatorUID); list.put(a); }
		
		int numberOfFrames = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.NumberOfFrames,1);
		int samplesPerPixel = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.SamplesPerPixel,1);

		if (sopClass == null) {
			int bitsAllocated = Attribute.getSingleIntegerValueOrDefault(list,TagFromName.BitsAllocated,1);
			Attribute aPixelData = list.getPixelData();
			boolean isFloatPixelData = aPixelData instanceof OtherFloatAttribute || aPixelData instanceof OtherDoubleAttribute;
			sopClass = selectSOPClassForModalityNumberOfFramesAndPixelCharacteristics(modality,numberOfFrames,samplesPerPixel,bitsAllocated,isFloatPixelData);	// (001239)
		}

		if (SOPClass.isImageWithFrameOfReferenceStorage(sopClass)
		 || sopClass.equals(SOPClass.RawDataStorage)
		 || sopClass.equals(SOPClass.EncapsulatedSTLStorage)
		) {
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.FrameOfReferenceUID); a.addValue(u.getAnotherNewUID()); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.PositionReferenceIndicator); list.put(a); }
		}
		
		if (SOPClass.isImageWithSynchronizationStorage(sopClass)
		 || sopClass.equals(SOPClass.RawDataStorage)
		) {
			{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SynchronizationFrameOfReferenceUID); a.addValue("1.2.840.10008.15.1.1"); list.put(a); }	// UTC
			{ Attribute a = new CodeStringAttribute(TagFromName.SynchronizationTrigger);  a.addValue("NO TRIGGER"); list.put(a); }
			{ Attribute a = new CodeStringAttribute(TagFromName.AcquisitionTimeSynchronized);  a.addValue("N"); list.put(a); }
		}

		if (!SOPClass.isEncapsulatedDocument(sopClass)) {	// add image related attributes
			if (!SOPClass.isLegacyConvertedEnhancedImageStorage(sopClass)) {	// (001111)
				Attribute a = new CodeStringAttribute(TagFromName.PatientOrientation); list.put(a);
			}
			{ Attribute a = new CodeStringAttribute(TagFromName.LossyImageCompression); a.addValue("00"); list.put(a); }
		}

		if (sopClass.equals(SOPClass.ParametricMapStorage)) {
			addParametricMapFrameTypeSharedFunctionalGroup(list);
			{ Attribute a = new CodeStringAttribute(TagFromName.ContentLabel); a.addValue("LABEL1"); list.put(a); }
			{ Attribute a = new LongStringAttribute(TagFromName.ContentDescription); list.put(a); }
			{ Attribute a = new PersonNameAttribute(TagFromName.ContentCreatorName); list.put(a); }
		}

		if (SOPClass.isEnhancedMultiframeImageStorage(sopClass)) {
			if (samplesPerPixel == 1) {
				double windowWidth = Attribute.getSingleDoubleValueOrDefault(list,TagFromName.WindowWidth,0);
				if (windowWidth > 0) {
					double windowCenter = Attribute.getSingleDoubleValueOrDefault(list,TagFromName.WindowCenter,0);
					String voiLUTFunction = Attribute.getSingleStringValueOrDefault(list,TagFromName.VOILUTFunction,"LINEAR");
					FunctionalGroupUtilities.generateVOILUTFunctionalGroup(list,numberOfFrames,windowWidth,windowCenter,voiLUTFunction);
					list.remove(TagFromName.WindowCenter);
					list.remove(TagFromName.WindowWidth);
					list.remove(TagFromName.VOILUTFunction);
				}
			}
			
			double rescaleSlope = Attribute.getSingleDoubleValueOrDefault(list,TagFromName.RescaleSlope,0);
			if (rescaleSlope > 0) {
				double rescaleIntercept = Attribute.getSingleDoubleValueOrDefault(list,TagFromName.RescaleIntercept,0);
				String rescaleType = Attribute.getSingleStringValueOrDefault(list,TagFromName.RescaleType,"US");
				FunctionalGroupUtilities.generatePixelValueTransformationFunctionalGroup(list,numberOfFrames,rescaleSlope,rescaleIntercept,rescaleType);
				list.remove(TagFromName.RescaleSlope);
				list.remove(TagFromName.RescaleIntercept);
				list.remove(TagFromName.RescaleType);
			}
			
			// create unassigned groups, even if empty and even if not used by SOP Class (e.g., parametric map) in case later converted ...
			if (generateUnassignedConverted) {
				FunctionalGroupUtilities.generateUnassignedConvertedAttributesSequenceFunctionalGroups(list,numberOfFrames);
			}
			
			// four values required unless Segmentation
			if (sopClass.equals(SOPClass.SegmentationStorage)) {	// (001113)
				{ Attribute a = new CodeStringAttribute(TagFromName.ImageType); a.addValue("DERIVED"); a.addValue("PRIMARY"); list.put(a); }
			}
			else {
				{ Attribute a = new CodeStringAttribute(TagFromName.ImageType); a.addValue("DERIVED"); a.addValue("PRIMARY"); a.addValue("VOLUME");  a.addValue("NONE"); list.put(a); }
			}
			if (!sopClass.equals(SOPClass.SegmentationStorage)	// (001112)
			 && !sopClass.equals(SOPClass.VLWholeSlideMicroscopyImageStorage)	// (001120)
			) {
				{ Attribute a = new CodeStringAttribute(TagFromName.PixelPresentation);               a.addValue("MONOCHROME"); list.put(a); }		// should we bother checking PhotometricInterpretation ?
				{ Attribute a = new CodeStringAttribute(TagFromName.VolumetricProperties);            a.addValue("VOLUME");     list.put(a); }		// no way to check that we are something funky like a MIP
				{ Attribute a = new CodeStringAttribute(TagFromName.VolumeBasedCalculationTechnique); a.addValue("NONE");       list.put(a); }		// no information
			}
			addModalitySpecificFrameTypeSharedFunctionalGroup(list,modality);

		}
		else if (!SOPClass.isEncapsulatedDocument(sopClass)) {
			// two values will usually do
			{ Attribute a = new CodeStringAttribute(TagFromName.ImageType); a.addValue("DERIVED"); a.addValue("PRIMARY"); list.put(a); }
		}
		if (SOPClass.isMultiframeImageStorage(sopClass)) {
			if (SOPClass.isMultiframeSecondaryCaptureImageStorage(sopClass)) {
				if (list.get(TagFromName.PerFrameFunctionalGroupsSequence) != null) {
					{ AttributeTagAttribute a = new AttributeTagAttribute(TagFromName.FrameIncrementPointer); a.addValue(TagFromName.PerFrameFunctionalGroupsSequence); list.put(a); }
				}
				else {
					if (numberOfFrames > 1) {
						{ AttributeTagAttribute a = new AttributeTagAttribute(TagFromName.FrameIncrementPointer); a.addValue(TagFromName.PageNumberVector); list.put(a); }
						{
							Attribute a = new IntegerStringAttribute(TagFromName.PageNumberVector);
							for (int page=1; page <= numberOfFrames; ++page) {
								a.addValue(page);
							}
							list.put(a);
						}
					}
				}
			}
			else if (!SOPClass.isEnhancedMultiframeImageStorage(sopClass)) {
				// need FrameIncrementPointer so use dummy FrameTime
				{ AttributeTagAttribute a = new AttributeTagAttribute(TagFromName.FrameIncrementPointer); a.addValue(TagFromName.FrameTime); list.put(a); }
				if (list.get(TagFromName.FrameTime) == null) {
					{ Attribute a = new DecimalStringAttribute(TagFromName.FrameTime); a.addValue(0); list.put(a); }
				}
			}
		}

		slf4jlogger.debug("CommonConvertedAttributeGeneration.generateCommonAttributes(): SOP Class = {}",sopClass);
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SOPClassUID); a.addValue(sopClass); list.put(a); }
		
		if (SOPClass.isSecondaryCaptureImageStorage(sopClass)
		 || (SOPClass.isEncapsulatedDocument(sopClass) && !sopClass.equals(SOPClass.EncapsulatedSTLStorage))) {
			{ Attribute a = new CodeStringAttribute(TagFromName.ConversionType); a.addValue("WSD"); list.put(a); }
		}
		
		if (!SOPClass.isSecondaryCaptureImageStorage(sopClass)
		 && !SOPClass.isEncapsulatedDocument(sopClass)
		 && !sopClass.equals(SOPClass.SegmentationStorage)	// (001112)
			) {
			{ Attribute a = new SequenceAttribute(TagFromName.AcquisitionContextSequence); list.put(a); }	// not in SC IODs (001080)
		}
		
		if (modality == null || modality.length() == 0) {	// (001333)
			modality = SOPClass.selectModalityForSOPClass(sopClass);	// (001334)
			slf4jlogger.debug("CommonConvertedAttributeGeneration.generateCommonAttributes(): selected Modality {} for SOP Class {}",modality,sopClass);
		}
		{ Attribute a = new CodeStringAttribute(TagFromName.Modality); a.addValue(modality); list.put(a); }
	}

	/**
	 * <p>Generate common attributes for converted images.</p>
	 *
	 * <p>Does NOT add ManufacturerModelName ... that should be added by caller.</p>
	 *
	 * <p>Does NOT call CodingSchemeIdentification.replaceCodingSchemeIdentificationSequenceWithCodingSchemesUsedInAttributeList ... that should be done by caller.</p>
	 *
	 * <p>Does generate empty Unassigned Converted Attributes Sequences (populates Shared and Per-Frame Functional Groups).</p>
	 *
	 * @param	list
	 * @param	patientName
	 * @param	patientID
	 * @param	studyID
	 * @param	seriesNumber
	 * @param	instanceNumber
	 * @param	modality	may be null
	 * @param	sopClass	may be null
	 * @throws			DicomException
	 * @throws			NumberFormatException
	 * @deprecated
	 */
	public static void generateCommonAttributes(AttributeList list,String patientName,String patientID,String studyID,String seriesNumber,String instanceNumber,String modality,String sopClass) throws  DicomException {
		generateCommonAttributes(list,patientName,patientID,studyID,seriesNumber,instanceNumber,modality,sopClass,true/*generateUnassignedConverted*/);
	}
}


