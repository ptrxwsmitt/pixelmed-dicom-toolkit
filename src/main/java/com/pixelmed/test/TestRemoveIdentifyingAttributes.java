/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestRemoveIdentifyingAttributes extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestRemoveIdentifyingAttributes(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestRemoveIdentifyingAttributes.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestRemoveIdentifyingAttributes");
		
		suite.addTest(new TestRemoveIdentifyingAttributes("TestRemoveIdentifyingAttributes_FromList"));
		
		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private static final DicomDictionary dictionary = DicomDictionary.StandardDictionary;

	private AttributeTag[] toBeRetained = {
		dictionary.getTagFromName("ProtocolName"),
		dictionary.getTagFromName("SeriesDescription"),
		dictionary.getTagFromName("StudyDescription")
	};
	
	private AttributeTag[] toBeDummied = {
		dictionary.getTagFromName("DetectorID"),
		dictionary.getTagFromName("DeviceSerialNumber"),
		dictionary.getTagFromName("ContentCreatorName"),
		dictionary.getTagFromName("VerifyingObserverName"),
		dictionary.getTagFromName("VerifyingOrganization")
	};
	
	private AttributeTag[] toBeZeroed = {
		dictionary.getTagFromName("AccessionNumber"),
		dictionary.getTagFromName("FillerOrderNumberImagingServiceRequest"),
		dictionary.getTagFromName("PatientBirthDate"),
		dictionary.getTagFromName("PatientID"),
		dictionary.getTagFromName("PatientName"),
		dictionary.getTagFromName("PlacerOrderNumberImagingServiceRequest"),
		dictionary.getTagFromName("ReferringPhysicianName"),
		dictionary.getTagFromName("StudyID"),
		dictionary.getTagFromName("VerifyingObserverIdentificationCodeSequence")
	};
	
	private AttributeTag[] toBeRemoved = {
		dictionary.getTagFromName("ActualHumanPerformersSequence"),
		dictionary.getTagFromName("ActualHumanPerformersSequence"),
		dictionary.getTagFromName("AdditionalPatientHistory"),
		dictionary.getTagFromName("AdmissionID"),
		dictionary.getTagFromName("AdmittingDate"),
		dictionary.getTagFromName("AdmittingDiagnosesCodeSequence"),
		dictionary.getTagFromName("AdmittingDiagnosesDescription"),
		dictionary.getTagFromName("AdmittingTime"),
		dictionary.getTagFromName("Allergies"),
		dictionary.getTagFromName("Arbitrary"),
		dictionary.getTagFromName("AuthorObserverSequence"),
		dictionary.getTagFromName("BranchOfService"),
		dictionary.getTagFromName("CassetteID"),
		dictionary.getTagFromName("ConfidentialityConstraintOnPatientDataDescription"),
		dictionary.getTagFromName("ContentCreatorIdentificationCodeSequence"),
		dictionary.getTagFromName("ContributionDescription"),
		dictionary.getTagFromName("CountryOfResidence"),
		dictionary.getTagFromName("CurrentPatientLocation"),
		dictionary.getTagFromName("CustodialOrganizationSequence"),
		dictionary.getTagFromName("DataSetTrailingPadding"),
		dictionary.getTagFromName("DerivationDescription"),
		dictionary.getTagFromName("DeviceUID"),
		dictionary.getTagFromName("DistributionAddress"),
		dictionary.getTagFromName("DistributionName"),
		dictionary.getTagFromName("GantryID"),
		dictionary.getTagFromName("GeneratorID"),
		dictionary.getTagFromName("HumanPerformerName"),
		dictionary.getTagFromName("HumanPerformerOrganization"),
		dictionary.getTagFromName("IconImageSequence"),
		dictionary.getTagFromName("IdentifyingComments"),
		dictionary.getTagFromName("InstitutionAddress"),
		dictionary.getTagFromName("InstitutionalDepartmentName"),
		dictionary.getTagFromName("InstitutionCodeSequence"),
		dictionary.getTagFromName("InstitutionName"),
		dictionary.getTagFromName("InsurancePlanIdentification"),
		dictionary.getTagFromName("IntendedRecipientsOfResultsIdentificationSequence"),
		dictionary.getTagFromName("InterpretationApproverSequence"),
		dictionary.getTagFromName("InterpretationAuthor"),
		dictionary.getTagFromName("InterpretationIDIssuer"),
		dictionary.getTagFromName("InterpretationRecorder"),
		dictionary.getTagFromName("InterpretationTranscriber"),
		dictionary.getTagFromName("IssuerOfAccessionNumberSequence"),
		dictionary.getTagFromName("IssuerOfAdmissionID"),
		dictionary.getTagFromName("IssuerOfAdmissionIDSequence"),
		dictionary.getTagFromName("IssuerOfPatientID"),
		dictionary.getTagFromName("IssuerOfPatientIDQualifiersSequence"),
		dictionary.getTagFromName("IssuerOfServiceEpisodeID"),
		dictionary.getTagFromName("IssuerOfServiceEpisodeIDSequence"),
		dictionary.getTagFromName("LastMenstrualDate"),
		dictionary.getTagFromName("MedicalAlerts"),
		dictionary.getTagFromName("MedicalRecordLocator"),
		dictionary.getTagFromName("MilitaryRank"),
		dictionary.getTagFromName("ModifyingDeviceID"),
		dictionary.getTagFromName("ModifyingDeviceManufacturer"),
		dictionary.getTagFromName("NameOfPhysiciansReadingStudy"),
		dictionary.getTagFromName("NamesOfIntendedRecipientsOfResults"),
		dictionary.getTagFromName("Occupation"),
		dictionary.getTagFromName("OperatorIdentificationSequence"),
		dictionary.getTagFromName("OperatorsName"),
		dictionary.getTagFromName("OrderCallbackPhoneNumber"),
		dictionary.getTagFromName("OrderEnteredBy"),
		dictionary.getTagFromName("OrderEntererLocation"),
		dictionary.getTagFromName("OtherPatientIDs"),
		dictionary.getTagFromName("OtherPatientIDsSequence"),
		dictionary.getTagFromName("OtherPatientNames"),
		dictionary.getTagFromName("ParticipantSequence"),
		dictionary.getTagFromName("PatientAddress"),
		dictionary.getTagFromName("PatientBirthName"),
		dictionary.getTagFromName("PatientBirthTime"),
		dictionary.getTagFromName("PatientComments"),
		dictionary.getTagFromName("PatientInsurancePlanCodeSequence"),
		dictionary.getTagFromName("PatientMotherBirthName"),
		dictionary.getTagFromName("PatientPrimaryLanguageCodeSequence"),
		dictionary.getTagFromName("PatientReligiousPreference"),
		dictionary.getTagFromName("PatientState"),
		dictionary.getTagFromName("PatientTelephoneNumbers"),
		dictionary.getTagFromName("PerformedLocation"),
		dictionary.getTagFromName("PerformedProcedureStepID"),
		dictionary.getTagFromName("PerformedStationAETitle"),
		dictionary.getTagFromName("PerformedStationGeographicLocationCodeSequence"),
		dictionary.getTagFromName("PerformedStationName"),
		dictionary.getTagFromName("PerformedStationNameCodeSequence"),
		dictionary.getTagFromName("PerformingPhysicianIdentificationSequence"),
		dictionary.getTagFromName("PerformingPhysicianName"),
		dictionary.getTagFromName("PersonAddress"),
		dictionary.getTagFromName("PersonIdentificationCodeSequence"),
		dictionary.getTagFromName("PersonName"),
		dictionary.getTagFromName("PersonTelephoneNumbers"),
		dictionary.getTagFromName("PhysicianApprovingInterpretation"),
		dictionary.getTagFromName("PhysiciansOfRecord"),
		dictionary.getTagFromName("PhysiciansOfRecordIdentificationSequence"),
		dictionary.getTagFromName("PhysiciansReadingStudyIdentificationSequence"),
		dictionary.getTagFromName("PlateID"),
		dictionary.getTagFromName("PreMedication"),
		dictionary.getTagFromName("ReferencedPatientAliasSequence"),
		dictionary.getTagFromName("ReferencedPatientSequence"),
		dictionary.getTagFromName("ReferencedStudySequence"),
		dictionary.getTagFromName("ReferringPhysicianAddress"),
		dictionary.getTagFromName("ReferringPhysicianIdentificationSequence"),
		dictionary.getTagFromName("ReferringPhysicianTelephoneNumbers"),
		dictionary.getTagFromName("RegionOfResidence"),
		dictionary.getTagFromName("RequestAttributesSequence"),
		dictionary.getTagFromName("RequestedProcedureID"),
		dictionary.getTagFromName("RequestedProcedureLocation"),
		dictionary.getTagFromName("RequestingPhysician"),
		dictionary.getTagFromName("RequestingService"),
		dictionary.getTagFromName("ResponsibleOrganization"),
		dictionary.getTagFromName("ResponsiblePerson"),
		dictionary.getTagFromName("ResultsDistributionListSequence"),
		dictionary.getTagFromName("ResultsIDIssuer"),
		dictionary.getTagFromName("ScheduledHumanPerformersSequence"),
		dictionary.getTagFromName("ScheduledPatientInstitutionResidence"),
		dictionary.getTagFromName("ScheduledPerformingPhysicianIdentificationSequence"),
		dictionary.getTagFromName("ScheduledPerformingPhysicianName"),
		dictionary.getTagFromName("ScheduledProcedureStepLocation"),
		dictionary.getTagFromName("ScheduledStationAETitle"),
		dictionary.getTagFromName("ScheduledStationGeographicLocationCodeSequence"),
		dictionary.getTagFromName("ScheduledStationName"),
		dictionary.getTagFromName("ScheduledStationNameCodeSequence"),
		dictionary.getTagFromName("ScheduledStudyLocation"),
		dictionary.getTagFromName("ScheduledStudyLocationAETitle"),
		dictionary.getTagFromName("ServiceEpisodeID"),
		dictionary.getTagFromName("SpecialNeeds"),
		dictionary.getTagFromName("StationName"),
		dictionary.getTagFromName("StudyIDIssuer"),
		dictionary.getTagFromName("StudyPriorityID"),
		dictionary.getTagFromName("StudyStatusID"),
		dictionary.getTagFromName("TextComments"),
		dictionary.getTagFromName("TextString"),
		dictionary.getTagFromName("TopicAuthor"),
		dictionary.getTagFromName("TopicKeywords"),
		dictionary.getTagFromName("TopicSubject"),
		dictionary.getTagFromName("TopicTitle")
	};
	
	public void TestRemoveIdentifyingAttributes_FromList() throws Exception {
		String originalValueToBeReplaced = "REPLACEMEPLEASE";
		AttributeList list = new AttributeList();
		for (AttributeTag t : toBeDummied) {
			Attribute a = AttributeFactory.newAttribute(t);
			list.put(a);
			a.addValue(originalValueToBeReplaced);
		}
		for (AttributeTag t : toBeZeroed) {
			Attribute a = AttributeFactory.newAttribute(t);
			list.put(a);
		}
		for (AttributeTag t : toBeRemoved) {
			Attribute a = AttributeFactory.newAttribute(t);
			list.put(a);
		}
		for (AttributeTag t : toBeRetained) {
			Attribute a = AttributeFactory.newAttribute(t);
			list.put(a);
		}
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,true/*keepUIDs*/,true/*keepDescriptors*/,true/*keepPatientCharacteristics*/);
		
		DicomDictionary dictionary = DicomDictionary.StandardDictionary;
		for (AttributeTag t : toBeDummied) {
			Attribute a = list.get(t);
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" is not removed",a != null);
			String replacedValue = a.getSingleStringValueOrNull();
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" is not null value",replacedValue != null);
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" has been replaced ",!originalValueToBeReplaced.equals(replacedValue));
		}
		for (AttributeTag t : toBeZeroed) {
			Attribute a = list.get(t);
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" is not removed",a != null);
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" is zero length",(a instanceof SequenceAttribute ? ((SequenceAttribute)a).getNumberOfItems() == 0 : a.getVL() == 0));
		}
		for (AttributeTag t : toBeRemoved) {
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" is removed",list.get(t) == null);
		}
		for (AttributeTag t : toBeRetained) {
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" is retained",list.get(t) != null);
		}


	}
	
}
