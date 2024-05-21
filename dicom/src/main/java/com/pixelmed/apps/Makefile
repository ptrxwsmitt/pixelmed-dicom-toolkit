PATHTOROOT = ../../..

OBJS = \
SynchronizeFromRemoteSCP.class \
WatchFolderAndSend.class \
RotateFlipSetOfImages.class \
DoseUtility.class \
InsertCodeSequence.class \
MergeCompositeContext.class \
MergeCompositeContextForOneEntitySelectively.class \
DoseReporterWithLegacyOCRAndAutoSendToRegistry.class \
MultiModalitySimulator.class \
ConvertPaletteColorToGrayscale.class \
ConvertPlanarConfiguration.class \
ConvertNonRGBToRGB.class \
UIDMapByMatchingPixelData.class \
DownloadOrTransmit.class \
RemapUIDs.class \
CopyOnlyHeadOfMultipleStructuredReportsAndReferences.class \
CheckAllUIDReferencesResolve.class \
AddHierarchicalEvidenceSequencetoStructuredReports.class \
IntegerScalingOfFloatingPointPixelData.class \
RemapFrameOfReferenceUIDs.class \
TranslateImagePositionPatient.class \
KeepOnlyImagePixelModule.class \
CompressDicomFiles.class \
DecompressDicomFiles.class \
Convert8To16Bits.class \
InsertRealWorldValueMap.class \
DeidentifyAndRedact.class \
DeidentifyAndRedactWithOriginalFileName.class \
FindAndCopySelectedDicomFiles.class \
FindAndCopySelectedDicomFilesUsingDatabase.class \
StudyReceiver.class \
MergeFunctionalGroups.class \
SetCharacteristicsFromSummary.class \
InstanceReceiver.class \
TiledPyramid.class \
ProcessReceivedWholeSlideImagesMakeTiledPyramidAndSend.class \
TabulateSegmentSequence.class \
PseudonymizeAndSequester.class \
ConvertSRTCodesToSCT.class \
ShrinkSegmentationToBoundingBox.class \
CompareSetsOfDicomFiles.class \
PixelMessageDigest.class \
MergeConcatenationInstances.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

# override javac command in Makefile.common.mk to add codec to classpath, until folded into main trunk :(
.java.class:
	export JAVAVERSIONTARGETJARFILE=`/usr/libexec/java_home -v ${JAVAVERSIONTARGET} | tail -1`/jre/lib/rt.jar; javac ${JAVACOPTIONS} \
		-classpath ${PATHTOROOT}:${SLF4JCOMPILEADDITIONALJAR}:${DATABASEADDITIONALJARS}:${FTPADDITIONALJARS}:${JPEGBLOCKREDACTIONJAR}:${PATHTOADDITIONAL}/javax.json-api-1.0.jar:${JIIOADDITIONALJARS} \
		-sourcepath ${PATHTOROOT} $<

archive:
	tar -cvf - Makefile *.java | gzip -best > ${PATHTOROOT}/apps.`date '+%Y%m%d'`.tar.gz

testsync:	SynchronizeFromRemoteSCP.class
	rm -rf /tmp/testdatabase*
	rm -rf /tmp/testimages
	mkdir -p /tmp/testimages
	java -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} -Djava.awt.headless=true \
		com.pixelmed.apps.SynchronizeFromRemoteSCP \
			/tmp/testdatabase \
			/tmp/testimages \
			localhost 11112 GRAYTOO_11112 \
			11113 LOCAL_11113

testsyncget:	SynchronizeFromRemoteSCP.class
	rm -rf /tmp/testdatabase*
	rm -rf /tmp/testimages
	mkdir -p /tmp/testimages
	java -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.defaultLogLevel=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.SynchronizeFromRemoteSCP=trace \
		com.pixelmed.apps.SynchronizeFromRemoteSCP \
			/tmp/testdatabase \
			/tmp/testimages \
			localhost 11112 GRAYTOO_11112 \
			11113 LOCAL_11113 \
			GET

testrotateflip90H:	RotateFlipSetOfImages.class
	rm -rf /tmp/testrotateflip90H
	mkdir -p /tmp/testrotateflip90H
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.RotateFlipSetOfImages \
			270 Y Y \
			$${HOME}/work/dctool.support/images/dicom/forjpeg2000/discimg \
			/tmp/testrotateflip90H

testwatchandsend:	WatchFolderAndSend.class
	@rm -rf /tmp/watchthisfolder
	@mkdir -p /tmp/watchthisfolder
	../network/runprocessinbackground.sh \
		/tmp/watchandsend.pid /tmp/watchandsend.stdout /tmp/watchandsend.stderr \
		java -cp ${PATHTOROOT} \
			com.pixelmed.apps.WatchFolderAndSend \
			/tmp/watchthisfolder \
			localhost 11112 STORESCP US
	#tail -f /tmp/watchandsend.stderr &
	@rm -rf /tmp/tesstoragescp.receivedfiles
	@mkdir -p /tmp/tesstoragescp.receivedfiles
	../network/runprocessinbackground.sh \
		/tmp/tesstoragescp.pid /tmp/tesstoragescp.stdout /tmp/tesstoragescp.stderr \
		java -cp ${PATHTOROOT} \
			com.pixelmed.network.StorageSOPClassSCPDispatcher 11112 STORESCP /tmp/tesstoragescp.receivedfiles INSECURE
	#tail -f /tmp/tesstoragescp.stderr &
	dcsmpte /tmp/testsmpte.dcm -r PatientName Test^Watch
	#java -cp ${PATHTOROOT} com.pixelmed.network.StorageSOPClassSCU localhost 11112 STORESCP US /tmp/testsmpte.dcm 0
	cp /tmp/testsmpte.dcm /tmp/watchthisfolder
	@echo "Sleeping for longer than two watch intervals in order to allow wait for being modified to expire"
	@sleep 25
	@echo "Back from sleep ... folder content should have been processed by now"
	@echo "Dumping logger messages"
	@cat /tmp/watchandsend.stderr
	@echo "List contents of watched directory ... expect it to be empty"
	@ls -lL /tmp/watchthisfolder
	@echo "List done"
	@echo "Comparing sent and received files - expect no difference except meta header"
	# the -a option to grep is necessary since sometimes it thinks the stderr file is binary and fails
	@receivedfilename=`grep -a 'sendReceivedObjectIndication() fileName' /tmp/tesstoragescp.stderr | tail -1 | sed -e 's/^.*fileName: //' -e 's/ from .*$$//'`; \
		andiff /tmp/testsmpte.dcm "$${receivedfilename}"; \
		dccmp /tmp/testsmpte.dcm "$${receivedfilename}"
	@echo "Comparison done"
	@kill -9 `cat /tmp/watchandsend.pid`
	@kill -9 `cat /tmp/tesstoragescp.pid`
	@rm -f /tmp/testsmpte.dcm
	@rm -rf /tmp/watchthisfolder
	@rm -f /tmp/watchandsend.pid /tmp/watchandsend.stdout /tmp/watchandsend.stderr
	@rm -rf /tmp/tesstoragescp.receivedfiles
	@rm -f /tmp/tesstoragescp.pid /tmp/tesstoragescp.stdout /tmp/tesstoragescp.stderr

testwatchandsenddialog:	WatchFolderAndSend.class
	java -cp ${PATHTOROOT} com.pixelmed.apps.WatchFolderAndSend

testdoseutility:	DoseUtility.class
	java -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${FTPADDITIONALJARS} \
		com.pixelmed.apps.DoseUtility

testdoseutilitywithanatomydebugging:	DoseUtility.class
	java -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${FTPADDITIONALJARS} \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.anatproc.CTAnatomy=debug \
		com.pixelmed.apps.DoseUtility

testinsertcode:	InsertCodeSequence.class
	rm -f /tmp/testinsertcodesrc.dcm
	dcsmpte /tmp/testinsertcodesrc.dcm
	rm -rf /tmp/testinsertcodedst
	mkdir -p /tmp/testinsertcodedst
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.InsertCodeSequence \
			AnatomicRegionSequence "T-A0100" "SRT" "" "Brain" \
			/tmp/testinsertcodesrc.dcm \
			/tmp/testinsertcodedst
	dcdiff /tmp/testinsertcodesrc.dcm /tmp/testinsertcodedst/*
	rm -f /tmp/testinsertcodesrc.dcm
	rm -rf /tmp/testinsertcodedst

testinsertrwvm:	InsertRealWorldValueMap.class
	rm -f /tmp/testinsertrwvmsrc.dcm
	dcsmpte /tmp/testinsertrwvmsrc.dcm
	rm -rf /tmp/testinsertrwvmdst
	mkdir -p /tmp/testinsertrwvmdst
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.InsertRealWorldValueMap \
			0 255 0 1 "SUVbw" "GML" \
			"g/l" "UCUM" "" "g/l" \
			/tmp/testinsertrwvmsrc.dcm \
			/tmp/testinsertrwvmdst
	dcdiff /tmp/testinsertrwvmsrc.dcm /tmp/testinsertrwvmdst/*
	dciodvfy /tmp/testinsertrwvmdst/*
	rm -f /tmp/testinsertrwvmsrc.dcm
	rm -rf /tmp/testinsertrwvmdst

testinsertrwvmwithquantity:	InsertRealWorldValueMap.class
	rm -f /tmp/testinsertrwvmsrc.dcm
	dcsmpte /tmp/testinsertrwvmsrc.dcm
	rm -rf /tmp/testinsertrwvmdst
	mkdir -p /tmp/testinsertrwvmdst
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.InsertRealWorldValueMap \
			0 255 0 1 "SUVbw" "GML" \
			"{SUVbw}g/ml" "UCUM" "" "SUVbw in g/ml" \
			CODE "(G-C1C6, SRT, Quantity)" '(126400, DCM, "Standardized Uptake Value")' \
			CODE '(G-C036, SRT, "Measurement Method")' '(126410, DCM, "SUV body weight calculation method")' \
			NUMERIC '(G-7174, SRT, "Delay time")' "60" '(s, UCUM, "s")' \
			TEXT '(121401, DCM, "Derivation")' "My funky method" \
			/tmp/testinsertrwvmsrc.dcm \
			/tmp/testinsertrwvmdst
	dcdiff /tmp/testinsertrwvmsrc.dcm /tmp/testinsertrwvmdst/*
	dciodvfy /tmp/testinsertrwvmdst/*
	rm -f /tmp/testinsertrwvmsrc.dcm
	rm -rf /tmp/testinsertrwvmdst

testmergecontext:	MergeCompositeContext.class
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeCompositeContext \
			/tmp/crapq/QIBA_CT_1B \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm' >/tmp/crapd_filelist.dat
	cat /tmp/crapd_filelist.dat
	dcentvfy -f /tmp/crapd_filelist.dat

testmergecontexttwo:	MergeCompositeContext.class
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeCompositeContext \
			/tmp/crapq/QIBA_CT_1B/Blinded \
			/tmp/crapq/QIBA_CT_1B/Results \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm' >/tmp/crapd_filelist.dat
	cat /tmp/crapd_filelist.dat
	dcentvfy -f /tmp/crapd_filelist.dat

testmergecontextsronly:	MergeCompositeContext.class
	rm -rf /tmp/crapx
	mkdir /tmp/crapx
	(find /tmp/crapq/QIBA_CT_1B/Results -name '*.sr' -exec cp '{}' /tmp/crapx/ ';')
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeCompositeContext \
			/tmp/crapx \
			/tmp/crapq/QIBA_CT_1B/Blinded \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm' >/tmp/crapd_filelist.dat
	cat /tmp/crapd_filelist.dat
	dcentvfy -f /tmp/crapd_filelist.dat

testmergecontextthree:	MergeCompositeContext.class
	rm -rf /tmp/crapx
	mkdir /tmp/crapx
	dcsmpte /tmp/crapx/initialcontext.dcm \
		-nodisclaimer \
		-r PatientName "MSKCC0001" \
		-r PatientID "MSKCC0001" \
		-r ClinicalTrialSubjectID "MSKCC0001" \
		-r ClinicalTrialSponsorName "QIBA" \
		-r ClinicalTrialProtocolID "QIBA_CT_1B" \
		-r ClinicalTrialProtocolName " " \
		-r ClinicalTrialSiteID "001" \
		-r ClinicalTrialSiteName " " \
		-r ClinicalTrialSubjectID "MSKCC0001" \
		-r ClinicalTrialSubjectReadingID "MSKCC0001" \
		-r StudyInstanceUID "1.3.6.1.4.1.9328.50.1.336855045631083138173485506844321353749"
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeCompositeContext \
			/tmp/crapq/QIBA_CT_1B/Blinded \
			/tmp/crapq/QIBA_CT_1B/Results \
			/tmp/crapx \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm' >/tmp/crapd_filelist.dat
	cat /tmp/crapd_filelist.dat
	dcentvfy -f /tmp/crapd_filelist.dat

testmergecontextselectivelydefaultpatient:	MergeCompositeContextForOneEntitySelectively.class
	rm -rf /tmp/crapx
	mkdir /tmp/crapx
	dcsmpte /tmp/crapx/initialcontext.dcm \
		-nodisclaimer \
		-r PatientName "MSKCC0001" \
		-r PatientID "MSKCC0001" \
		-r StudyDate "19710607" \
		-r SeriesNumber "36" \
		-r InstanceNumber "12243" \
		-r FrameOfReferenceUID 1.3.6.1.4.1.5962.1.3.0.36.1433088289.31557.9999
	dcsmpte /tmp/crapx/differentcontext.dcm \
		-nodisclaimer \
		-r PatientName "BLABLA" \
		-r PatientID "BLABLA" \
		-r StudyDate "19200403" \
		-r SeriesNumber "42" \
		-r InstanceNumber "873652835" \
		-r FrameOfReferenceUID 1.3.6.1.4.1.5962.1.3.0.36.1433088289.31557.8888
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeCompositeContextForOneEntitySelectively \
			/tmp/crapx/initialcontext.dcm \
			/tmp/crapx/differentcontext.dcm \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm' >/tmp/crapd_filelist.dat
	cat /tmp/crapd_filelist.dat
	dcentvfy -f /tmp/crapd_filelist.dat
	dctable -describe -k StudyInstanceUID -k SeriesInstanceUID -k FrameOfReferenceUID -k SOPInstanceUID -k PatientID -k PatientName -k SeriesNumber -k InstanceNumber
	@xargs -L1 -I% </tmp/crapd_filelist.dat dctable -k StudyInstanceUID -k SeriesInstanceUID -k FrameOfReferenceUID -k SOPInstanceUID -k PatientID -k PatientName -k SeriesNumber -k InstanceNumber "%"

testmergecontextselectivelypatientstudy:	MergeCompositeContextForOneEntitySelectively.class
	rm -rf /tmp/crapx
	mkdir /tmp/crapx
	dcsmpte /tmp/crapx/initialcontext.dcm \
		-nodisclaimer \
		-r PatientName "MSKCC0001" \
		-r PatientID "MSKCC0001" \
		-r StudyDate "19710607" \
		-r SeriesNumber "36" \
		-r InstanceNumber "12243" \
		-r FrameOfReferenceUID 1.3.6.1.4.1.5962.1.3.0.36.1433088289.31557.9999
	dcsmpte /tmp/crapx/differentcontext.dcm \
		-nodisclaimer \
		-r PatientName "BLABLA" \
		-r PatientID "BLABLA" \
		-r StudyDate "19200403" \
		-r SeriesNumber "42" \
		-r InstanceNumber "873652835" \
		-r FrameOfReferenceUID 1.3.6.1.4.1.5962.1.3.0.36.1433088289.31557.8888
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeCompositeContextForOneEntitySelectively \
			-patient -study \
			/tmp/crapx/initialcontext.dcm \
			/tmp/crapx/differentcontext.dcm \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm' >/tmp/crapd_filelist.dat
	cat /tmp/crapd_filelist.dat
	dcentvfy -f /tmp/crapd_filelist.dat
	dctable -describe -k StudyInstanceUID -k SeriesInstanceUID -k FrameOfReferenceUID -k SOPInstanceUID -k PatientID -k PatientName -k SeriesNumber -k InstanceNumber
	@xargs -L1 -I% </tmp/crapd_filelist.dat dctable -k StudyInstanceUID -k SeriesInstanceUID -k FrameOfReferenceUID -k SOPInstanceUID -k PatientID -k PatientName -k SeriesNumber -k InstanceNumber "%"

testmergecontextselectivelypatientstudyseriesfor:	MergeCompositeContextForOneEntitySelectively.class
	rm -rf /tmp/crapx
	mkdir /tmp/crapx
	dcsmpte /tmp/crapx/initialcontext.dcm \
		-nodisclaimer \
		-r PatientName "MSKCC0001" \
		-r PatientID "MSKCC0001" \
		-r StudyDate "19710607" \
		-r SeriesNumber "36" \
		-r InstanceNumber "12243" \
		-r FrameOfReferenceUID 1.3.6.1.4.1.5962.1.3.0.36.1433088289.31557.9999
	dcsmpte /tmp/crapx/differentcontext.dcm \
		-nodisclaimer \
		-r PatientName "BLABLA" \
		-r PatientID "BLABLA" \
		-r StudyDate "19200403" \
		-r SeriesNumber "42" \
		-r InstanceNumber "873652835" \
		-r FrameOfReferenceUID 1.3.6.1.4.1.5962.1.3.0.36.1433088289.31557.8888
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeCompositeContextForOneEntitySelectively \
			-patient -study -series -frameofreference \
			/tmp/crapx/initialcontext.dcm \
			/tmp/crapx/differentcontext.dcm \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm' >/tmp/crapd_filelist.dat
	cat /tmp/crapd_filelist.dat
	dcentvfy -f /tmp/crapd_filelist.dat
	dctable -describe -k StudyInstanceUID -k SeriesInstanceUID -k FrameOfReferenceUID -k SOPInstanceUID -k PatientID -k PatientName -k SeriesNumber -k InstanceNumber
	@xargs -L1 -I% </tmp/crapd_filelist.dat dctable -k StudyInstanceUID -k SeriesInstanceUID -k FrameOfReferenceUID -k SOPInstanceUID -k PatientID -k PatientName -k SeriesNumber -k InstanceNumber "%"

testdosereporter:	DoseReporterWithLegacyOCRAndAutoSendToRegistry.class
	java -server -Djava.awt.headless=true -Xmx256m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${DATABASEADDITIONALJARS}:${FTPADDITIONALJARS} \
		com.pixelmed.apps.DoseReporterWithLegacyOCRAndAutoSendToRegistry \
		testdosereporter.properties

testdosereporterclean:
	rm -rf testdosereporterdb.*
	rm -rf ~/testdosereporterimages

teststudyreceiver:	StudyReceiver.class
	mkdir -p /tmp/teststudyreceiverimages
	mkdir -p /tmp/teststudyreceivercompletedstudyimages
	java -server -Djava.awt.headless=true -Xmx256m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${DATABASEADDITIONALJARS} \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.StudyReceiver=debug \
		com.pixelmed.apps.StudyReceiver \
		teststudyreceiver.properties

teststudyreceiverclean:
	rm -rf /tmp/teststudyreceiverreporterdb.*
	rm -rf /tmp/teststudyreceiverimages
	rm -rf /tmp/teststudyreceivercompletedstudyimages

testinstancereceiver:	InstanceReceiver.class
	mkdir -p /tmp/testinstancereceiverinstances
	java -server -Djava.awt.headless=true \
		-Xmx2g \
		-cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		-Dorg.slf4j.simpleLogger.showDateTime=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.InstanceReceiver=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.StorageSOPClassSCP=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.StorageSOPClassSCPDispatcher=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.Association=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.AssociationAcceptor=debug \
		com.pixelmed.apps.InstanceReceiver \
		testinstancereceiver.properties

testinstancereceiverclean:
	rm -rf /tmp/testinstancereceiverinstances

testwsireceiverpyramidsender:	ProcessReceivedWholeSlideImagesMakeTiledPyramidAndSend.class
	mkdir -p /tmp/testwsireceiverimages
	mkdir -p /tmp/testwsireceiverpyramid
	java -server -Djava.awt.headless=true \
		-Xmx2g \
		-cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${JIIOADDITIONALJARS} \
		-Dorg.slf4j.simpleLogger.showDateTime=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.ProcessReceivedWholeSlideImagesMakeTiledPyramidAndSend=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.InstanceReceiver=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.StorageSOPClassSCP=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.StorageSOPClassSCPDispatcher=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.Association=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.AssociationAcceptor=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.AssociationInitiator=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.StorageSOPClassSCU=debug \
		com.pixelmed.apps.ProcessReceivedWholeSlideImagesMakeTiledPyramidAndSend \
		testwsireceiverpyramidsender.properties

testwsireceiverpyramidsenderclean:
	rm -rf /tmp/testwsireceiverimages
	rm -rf /tmp/testwsireceiverpyramid


testmodsimdatabase:
	if [ ! -f /tmp/modsimdatabase.properties ]; \
	then \
		rm -rf /tmp/modsimdatabase.*; \
		mkdir -p /tmp/modsimimages; \
		dcsmpte -r Modality CT -r PatientID 1 -r PatientName 1 /tmp/modsimimages/image1.dcm; \
		java -Xmx512m -cp ${PATHTOROOT}:${DATABASEADDITIONALJARS}:${DICOMADDITIONALJARS} \
			com.pixelmed.database.RebuildDatabaseFromInstanceFiles \
			PatientStudySeriesConcatenationInstanceModel \
			/tmp/modsimdatabase \
			/tmp/modsimimages; \
	fi

testmodsimdatabaseclean:
	rm -rf /tmp/modsimdatabase.*
	rm -rf /tmp/modsimimages

testmodsim:	MultiModalitySimulator.class testmodsimdatabase
	java -server -Djava.awt.headless=true -Xmx384m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${DATABASEADDITIONALJARS} \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.MultiModalitySimulator=debug \
		com.pixelmed.apps.MultiModalitySimulator \
			localhost 11112 GRAYTOO_11112 \
			/tmp/modsimdatabase

testconvertpalette:	ConvertPaletteColorToGrayscale.class
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.ConvertPaletteColorToGrayscale \
		$${HOME}/Pictures/Medical/Color/PaletteColorOfGrayscale/OBJ_0001 \
		/tmp/palette_converted_to_grayscale

testconvertplanar:	ConvertPlanarConfiguration.class
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.ConvertPlanarConfiguration \
		$${HOME}/Pictures/Medical/Color/YBRFULLBYPLANE/ybrfullbyplanecleaned.dcm \
		/tmp/ybrfull_planar_converted_to_pixel.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.ConvertPlanarConfiguration \
		/tmp/ybrfull_planar_converted_to_pixel.dcm \
		/tmp/ybrfull_planar_converted_back.dcm
	dccmp \
		$${HOME}/Pictures/Medical/Color/YBRFULLBYPLANE/ybrfullbyplanecleaned.dcm \
		/tmp/ybrfull_planar_converted_back.dcm

testconvertybr:	ConvertNonRGBToRGB.class testconvertplanar
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.ConvertNonRGBToRGB \
		$${HOME}/Pictures/Medical/Color/YBRFULLBYPLANE/ybrfullbyplanecleaned.dcm \
		/tmp/ybrfull_planar_converted_to_rgb.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.ConvertNonRGBToRGB \
		/tmp/ybrfull_planar_converted_to_pixel.dcm \
		/tmp/ybrfull_pixel_converted_to_rgb.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.ConvertPlanarConfiguration \
		/tmp/ybrfull_pixel_converted_to_rgb.dcm \
		/tmp/ybrfull_pixel_converted_to_rgb_converted_back_to_planar.dcm
	dccmp \
		/tmp/ybrfull_planar_converted_to_rgb.dcm \
		/tmp/ybrfull_pixel_converted_to_rgb_converted_back_to_planar.dcm

testuidmappixeldata8:	UIDMapByMatchingPixelData.class
	dcsmpte /tmp/crap1
	dcsmpte /tmp/crap2
	dctable -describe -k SOPInstanceUID -k SeriesInstanceUID -k StudyInstanceUID /tmp/crap1 /tmp/crap2
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.UIDMapByMatchingPixelData \
		/tmp/crap1 /tmp/crap2

testuidmappixeldata16:	UIDMapByMatchingPixelData.class
	dcsmpte -bits 16 /tmp/crap1
	dcsmpte -bits 16 /tmp/crap2
	dctable -describe -k SOPInstanceUID -k SeriesInstanceUID -k StudyInstanceUID /tmp/crap1 /tmp/crap2
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.UIDMapByMatchingPixelData \
		/tmp/crap1 /tmp/crap2

testdownloadortx:	DownloadOrTransmit.class
	java -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${FTPADDITIONALJARS} \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.DownloadOrTransmit=info \
		com.pixelmed.apps.DownloadOrTransmit

		#-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.AssociationAcceptor=trace \
		#-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.AssociationInitiator=trace \
		#-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.QueryInformationModel=trace \
		#-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.MoveSOPClassSCU=trace \
		#-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.StorageSOPClassSCP=trace \

testremapuids:	RemapUIDs.class
	rm -rf /tmp/crap_first
	rm -rf /tmp/crap_second
	rm -rf /tmp/crap_dst
	mkdir /tmp/crap_first
	mkdir /tmp/crap_second
	mkdir /tmp/crap_dst
	dcsmpte /tmp/crap_first/crap1
	dcsmpte /tmp/crap_second/crap2
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.UIDMapByMatchingPixelData \
		/tmp/crap_first/crap1 /tmp/crap_second/crap2 \
		>/tmp/crapuidmap 2>&1
	cat /tmp/crapuidmap
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.RemapUIDs \
		/tmp/crapuidmap \
		/tmp/crap_second \
		/tmp/crap_dst
	# UIDs in second file should have been mapped to and now match those in first file that was used to generate the map
	outputfile=`find /tmp/crap_dst -name '*.dcm'`; dctable -describe -k SOPInstanceUID -k SeriesInstanceUID -k StudyInstanceUID /tmp/crap_first/crap1 /tmp/crap_second/crap2 "$${outputfile}"; dcdiff /tmp/crap_first/crap1 "$${outputfile}"

testcopysrhead:	CopyOnlyHeadOfMultipleStructuredReportsAndReferences.class
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.CopyOnlyHeadOfMultipleStructuredReportsAndReferences \
			/tmp/crapq/QIBA_CT_1B/Results \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm'

testresolve:	CheckAllUIDReferencesResolve.class
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.CheckAllUIDReferencesResolve \
			/tmp/crapq/QIBA_CT_1B

testaddevidence:	AddHierarchicalEvidenceSequencetoStructuredReports.class
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.AddHierarchicalEvidenceSequencetoStructuredReports \
			/tmp/crapq/QIBA_CT_1B \
			/tmp/crapd
	find /tmp/crapd -name '*.dcm'

testfloatscaling:	IntegerScalingOfFloatingPointPixelData.class
	rm -f /tmp/testsyntheticfloatscaledtoint.dcm
	rm -f /tmp/testsyntheticfloatscaledtointthenmadefloatagain.dcm
	rm -f /tmp/testsyntheticfloatscaledtointthenmadefloatagainintagain.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		$${HOME}/work/experiments/testsyntheticfloat10.dcm /tmp/testsyntheticfloatscaledtoint.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toFLOAT /tmp/testsyntheticfloatscaledtoint.dcm /tmp/testsyntheticfloatscaledtointthenmadefloatagain.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toINT /tmp/testsyntheticfloatscaledtointthenmadefloatagain.dcm /tmp/testsyntheticfloatscaledtointthenmadefloatagainintagain.dcm
	#dccmp /tmp/testsyntheticfloatscaledtoint.dcm /tmp/testsyntheticfloatscaledtointthenmadefloatagainintagain.dcm
	andiff /tmp/testsyntheticfloatscaledtoint.dcm /tmp/testsyntheticfloatscaledtointthenmadefloatagainintagain.dcm

testdoublescaling:	IntegerScalingOfFloatingPointPixelData.class
	rm -f /tmp/testsyntheticdoublescaledtoint.dcm
	rm -f /tmp/testsyntheticdoublescaledtointthenmadedoubleagain.dcm
	rm -f /tmp/testsyntheticdoublescaledtointthenmadedoubleagainintagain.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		$${HOME}/work/experiments/testsyntheticdouble10.dcm /tmp/testsyntheticdoublescaledtoint.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toDOUBLE /tmp/testsyntheticdoublescaledtoint.dcm /tmp/testsyntheticdoublescaledtointthenmadedoubleagain.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toINT /tmp/testsyntheticdoublescaledtointthenmadedoubleagain.dcm /tmp/testsyntheticdoublescaledtointthenmadedoubleagainintagain.dcm
	#dccmp /tmp/testsyntheticdoublescaledtoint.dcm /tmp/testsyntheticdoublescaledtointthenmadedoubleagainintagain.dcm
	andiff /tmp/testsyntheticdoublescaledtoint.dcm /tmp/testsyntheticdoublescaledtointthenmadedoubleagainintagain.dcm

testremapforuids:	RemapFrameOfReferenceUIDs.class
	rm -rf /tmp/testremapforuids
	mkdir -p /tmp/testremapforuids/input/series1
	mkdir -p /tmp/testremapforuids/input/series2
	mkdir -p /tmp/testremapforuids/output
	dcsmpte /tmp/testremapforuids/input/series1/image1 -r StudyInstanceUID 0.1.1.1 -r SeriesInstanceUID 1.1.1.1 -r FrameOfReferenceUID 2.1.1.1
	dcsmpte /tmp/testremapforuids/input/series1/image2 -r StudyInstanceUID 0.1.1.1 -r SeriesInstanceUID 1.1.1.1 -r FrameOfReferenceUID 2.1.1.2
	dcsmpte /tmp/testremapforuids/input/series2/image3 -r StudyInstanceUID 0.1.1.1 -r SeriesInstanceUID 1.1.1.2 -r FrameOfReferenceUID 2.1.1.3
	dcsmpte /tmp/testremapforuids/input/series2/image4 -r StudyInstanceUID 0.1.1.1 -r SeriesInstanceUID 1.1.1.2 -r FrameOfReferenceUID 2.1.1.4
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.RemapFrameOfReferenceUIDs \
		SERIES \
		/tmp/testremapforuids/input \
		/tmp/testremapforuids/output

testtranslateposn:	TranslateImagePositionPatient.class
	rm -rf /tmp/craptranslate
	mkdir -p /tmp/craptranslate/TCGA-GBM_FixedBadExplicitVR/TCGA-14-3477_Results
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.TranslateImagePositionPatient \
		XYZ 0.026248860744917658 0.08305511807304704 1.2504172730164407 \
		$${HOME}/Documents/Work/NCI/AIM/Emory/images/TCGA-GBM_FixedBadExplicitVR/TCGA-14-3477_Results/1.2.276.0.7230010.3.1.4.1564572511.2560.1353374333.316.seg \
		/tmp/craptranslate/TCGA-GBM_FixedBadExplicitVR/TCGA-14-3477_Results

testkeeponlyimage:	KeepOnlyImagePixelModule.class
	rm -rf /tmp/testkeeponlyimage
	mkdir -p /tmp/testkeeponlyimage
	dcsmpte /tmp/testkeeponlyimage/input.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.KeepOnlyImagePixelModule \
		/tmp/testkeeponlyimage/input.dcm \
		/tmp/testkeeponlyimage/output.dcm
	dcdiff /tmp/testkeeponlyimage/input.dcm \
		   /tmp/testkeeponlyimage/output.dcm

testdecompress:	DecompressDicomFiles.class
	rm -rf /tmp/testdecompress.smpte.dcm /tmp/testdecompress.smpte.jpeg.dcm /tmp/testdecompress.output
	dcsmpte /tmp/testdecompress.smpte.dcm
	dcjpeg /tmp/testdecompress.smpte.dcm /tmp/testdecompress.smpte.jpeg.dcm 1.2.840.10008.1.2.4.50 -q 1
	mkdir -p /tmp/testdecompress.output
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testdecompress.smpte.jpeg.dcm /tmp/testdecompress.output
	ls -l /tmp/testdecompress.output/*
	andiff /tmp/testdecompress.smpte.jpeg.dcm /tmp/testdecompress.output/*
	#rm -rf /tmp/testdecompress.smpte.dcm /tmp/testdecompress.smpte.jpeg.dcm /tmp/testdecompress.output

testdecompressrle:	DecompressDicomFiles.class
	cp "$${HOME}/Pictures/Medical/RLE/US16BitsPaletteColorRLE.dcm" /tmp/testdecompress.dcm
	mkdir -p /tmp/testdecompress.output
	rm -f /tmp/testdecompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testdecompress.dcm /tmp/testdecompress.output
	ls -l /tmp/testdecompress.output/*
	andiff /tmp/testdecompress.dcm /tmp/testdecompress.output/*
	#rm -rf /tmp/testdecompress.dcm /tmp/testdecompress.output

testdecompressourjpeglossless:	DecompressDicomFiles.class
	cp "$${HOME}/Pictures/Medical/JPEGLossless/RGB_8Bit_JPEGLossless/original.dcm" /tmp/testdecompress.dcm
	mkdir -p /tmp/testdecompress.output
	rm -f /tmp/testdecompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT}:${PIXELMEDIMAGEIOJAR} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testdecompress.dcm /tmp/testdecompress.output
	ls -l /tmp/testdecompress.output/*
	andiff /tmp/testdecompress.dcm /tmp/testdecompress.output/*
	#rm -rf /tmp/testdecompress.dcm /tmp/testdecompress.output

testcompress:	CompressDicomFiles.class
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output /tmp/testdecompress.output
	dcsmpte /tmp/testcompress.smpte.dcm
	mkdir -p /tmp/testcompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testcompress.smpte.dcm /tmp/testcompress.output
	find /tmp/testcompress.output -type f
	andiff /tmp/testcompress.smpte.dcm /tmp/testcompress.output/*
	dctoraw /tmp/testcompress.smpte.dcm /tmp/testcompress.smpte.raw
	mkdir -p /tmp/testdecompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testcompress.output/* /tmp/testdecompress.output
	dctoraw /tmp/testdecompress.output/* /tmp/testcompress.output_decompressed.raw
	@echo "Comparing raw output of original file with decompressed compressed J2K file ..."
	cmp /tmp/testcompress.smpte.raw /tmp/testcompress.output_decompressed.raw
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output /tmp/testdecompress.output /tmp/testcompress.smpte.raw /tmp/testcompress.output_decompressed.raw

testcompresswithoutputformatjpeg2000:	CompressDicomFiles.class
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output /tmp/testdecompress.output
	dcsmpte /tmp/testcompress.smpte.dcm
	mkdir -p /tmp/testcompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testcompress.smpte.dcm /tmp/testcompress.output jpeg2000
	andiff /tmp/testcompress.smpte.dcm /tmp/testcompress.output/*
	dctoraw /tmp/testcompress.smpte.dcm /tmp/testcompress.smpte.raw
	mkdir -p /tmp/testdecompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testcompress.output/* /tmp/testdecompress.output
	dctoraw /tmp/testdecompress.output/* /tmp/testcompress.output_decompressed.raw
	@echo "Comparing raw output of original file with decompressed compressed J2K file ..."
	cmp /tmp/testcompress.smpte.raw /tmp/testcompress.output_decompressed.raw
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output /tmp/testdecompress.output /tmp/testcompress.smpte.raw /tmp/testcompress.output_decompressed.raw

testcompresswithtsjpeg2000:	CompressDicomFiles.class
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output /tmp/testdecompress.output
	dcsmpte /tmp/testcompress.smpte.dcm
	mkdir -p /tmp/testcompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testcompress.smpte.dcm /tmp/testcompress.output 1.2.840.10008.1.2.4.90
	andiff /tmp/testcompress.smpte.dcm /tmp/testcompress.output/*
	dctoraw /tmp/testcompress.smpte.dcm /tmp/testcompress.smpte.raw
	mkdir -p /tmp/testdecompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testcompress.output/* /tmp/testdecompress.output
	dctoraw /tmp/testdecompress.output/* /tmp/testcompress.output_decompressed.raw
	@echo "Comparing raw output of original file with decompressed compressed J2K file ..."
	cmp /tmp/testcompress.smpte.raw /tmp/testcompress.output_decompressed.raw
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output /tmp/testdecompress.output /tmp/testcompress.smpte.raw /tmp/testcompress.output_decompressed.raw

# expect to fail on Mac because no lossless codec (only native available)
testcompresswithoutputformatjpeglossless:	CompressDicomFiles.class
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output
	dcsmpte /tmp/testcompress.smpte.dcm
	mkdir -p /tmp/testcompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testcompress.smpte.dcm /tmp/testcompress.output jpeg-lossless
	andiff /tmp/testcompress.smpte.dcm /tmp/testcompress.output/*
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output

# expect to fail on Mac because no lossless codec (only native available)
testcompresswithtsjpeglossless:	CompressDicomFiles.class
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output
	dcsmpte /tmp/testcompress.smpte.dcm
	mkdir -p /tmp/testcompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.CompressDicomFiles=trace \
		-Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testcompress.smpte.dcm /tmp/testcompress.output 1.2.840.10008.1.2.4.70
	andiff /tmp/testcompress.smpte.dcm /tmp/testcompress.output/*
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output

testcompresswithoutputformatrle:	CompressDicomFiles.class
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output /tmp/testdecompress.output
	dcsmpte /tmp/testcompress.smpte.dcm
	mkdir -p /tmp/testcompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.CompressDicomFiles=info \
		-Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testcompress.smpte.dcm /tmp/testcompress.output rle
	andiff /tmp/testcompress.smpte.dcm /tmp/testcompress.output/*
	dctoraw /tmp/testcompress.smpte.dcm /tmp/testcompress.smpte.raw
	mkdir -p /tmp/testdecompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testcompress.output/* /tmp/testdecompress.output
	dctoraw /tmp/testdecompress.output/* /tmp/testcompress.output_decompressed.raw
	@echo "Comparing raw output of original file with decompressed compressed J2K file ..."
	cmp /tmp/testcompress.smpte.raw /tmp/testcompress.output_decompressed.raw
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output /tmp/testdecompress.output /tmp/testcompress.smpte.raw /tmp/testcompress.output_decompressed.raw

testcompresswithtsrle:	CompressDicomFiles.class
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output /tmp/testdecompress.output
	dcsmpte /tmp/testcompress.smpte.dcm
	mkdir -p /tmp/testcompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.CompressDicomFiles=info \
		-Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testcompress.smpte.dcm /tmp/testcompress.output 1.2.840.10008.1.2.5
	andiff /tmp/testcompress.smpte.dcm /tmp/testcompress.output/*
	dctoraw /tmp/testcompress.smpte.dcm /tmp/testcompress.smpte.raw
	mkdir -p /tmp/testdecompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testcompress.output/* /tmp/testdecompress.output
	dctoraw /tmp/testdecompress.output/* /tmp/testcompress.output_decompressed.raw
	@echo "Comparing raw output of original file with decompressed compressed J2K file ..."
	cmp /tmp/testcompress.smpte.raw /tmp/testcompress.output_decompressed.raw
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output /tmp/testdecompress.output /tmp/testcompress.smpte.raw /tmp/testcompress.output_decompressed.raw

testrecompress16BitsPaletteColorRLEAsJ2KReversible:	DecompressDicomFiles.class CompressDicomFiles.class
	cp "$${HOME}/Pictures/Medical/RLE/US16BitsPaletteColorRLE.dcm" /tmp/testdecompress.dcm
	mkdir -p /tmp/testdecompress.output
	rm -rf /tmp/testdecompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testdecompress.dcm /tmp/testdecompress.output
	mkdir -p /tmp/testcompress.output
	rm -rf /tmp/testcompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testdecompress.output/* /tmp/testcompress.output
	andiff /tmp/testdecompress.dcm /tmp/testcompress.output/*
	mkdir -p /tmp/testdecompressrecompress.output
	rm -f /tmp/testdecompressrecompress.output/*
	#ls -l /tmp/testdecompressrecompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testcompress.output/* /tmp/testdecompressrecompress.output
	#ls -l /tmp/testdecompressrecompress.output
	dctoraw /tmp/testdecompressrecompress.output/* /tmp/testdecompressrecompress.output_decompressed.raw
	dctoraw /tmp/testdecompress.output/* /tmp/testdecompress.output.raw
	@echo "Comparing raw output of decompressed original RLE file with decompressed recompressed J2K file ..."
	cmp /tmp/testdecompress.output.raw /tmp/testdecompressrecompress.output_decompressed.raw
	rm -rf /tmp/testdecompress.dcm /tmp/testdecompress.output /tmp/testcompress.output /tmp/testdecompressrecompress.output /tmp/testdecompress.output.raw /tmp/testdecompressrecompress.output_decompressed.raw

testrecompress16BitsPaletteColorRLEAsRLE:	DecompressDicomFiles.class CompressDicomFiles.class
	cp "$${HOME}/Pictures/Medical/RLE/US16BitsPaletteColorRLE.dcm" /tmp/testdecompress.dcm
	mkdir -p /tmp/testdecompress.output
	rm -rf /tmp/testdecompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testdecompress.dcm /tmp/testdecompress.output
	mkdir -p /tmp/testcompress.output
	rm -rf /tmp/testcompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testdecompress.output/* /tmp/testcompress.output rle
	andiff /tmp/testdecompress.dcm /tmp/testcompress.output/*
	mkdir -p /tmp/testdecompressrecompress.output
	rm -f /tmp/testdecompressrecompress.output/*
	#ls -l /tmp/testdecompressrecompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testcompress.output/* /tmp/testdecompressrecompress.output
	#ls -l /tmp/testdecompressrecompress.output
	dctoraw /tmp/testdecompressrecompress.output/* /tmp/testdecompressrecompress.output_decompressed.raw
	dctoraw /tmp/testdecompress.output/* /tmp/testdecompress.output.raw
	@echo "Comparing raw output of decompressed original RLE file with decompressed recompressed RLE file ..."
	cmp /tmp/testdecompress.output.raw /tmp/testdecompressrecompress.output_decompressed.raw
	rm -rf /tmp/testdecompress.dcm /tmp/testdecompress.output /tmp/testcompress.output /tmp/testdecompressrecompress.output /tmp/testdecompress.output.raw /tmp/testdecompressrecompress.output_decompressed.raw

testrecompressRGBRLEAsRLE:	DecompressDicomFiles.class CompressDicomFiles.class
	#cp "/Volumes/Toshiba5TEnc/CluniePortable15TB3_Enc/MDDX/20160620_LargeCasesExpanding/128530/IM001" /tmp/testdecompress.dcm
	cp "$${HOME}/Pictures/Medical/RLE/CU7WJ28E" /tmp/testdecompress.dcm
	mkdir -p /tmp/testdecompress.output
	rm -f /tmp/testdecompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testdecompress.dcm /tmp/testdecompress.output
	mkdir -p /tmp/testcompress.output
	rm -f /tmp/testcompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testdecompress.output/* /tmp/testcompress.output rle
	andiff /tmp/testdecompress.dcm /tmp/testcompress.output/*
	mkdir -p /tmp/testdecompressrecompress.output
	rm -f /tmp/testdecompressrecompress.output/*
	#ls -l /tmp/testdecompressrecompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testcompress.output/* /tmp/testdecompressrecompress.output
	#ls -l /tmp/testdecompressrecompress.output
	dctoraw /tmp/testdecompressrecompress.output/* /tmp/testdecompressrecompress.output_decompressed.raw
	dctoraw /tmp/testdecompress.output/* /tmp/testdecompress.output.raw
	@echo "Comparing raw output of decompressed original RLE file with decompressed recompressed RLE file ... expect to differ, since original is RLE but color-by-pixel (?illegal) and decompressed recompressed is RGB color-by-plane :("
	cmp /tmp/testdecompress.output.raw /tmp/testdecompressrecompress.output_decompressed.raw
	rm -rf /tmp/testdecompress.dcm /tmp/testdecompress.output /tmp/testcompress.output /tmp/testdecompressrecompress.output /tmp/testdecompress.output.raw /tmp/testdecompressrecompress.output_decompressed.raw

testrecompressRGBRLEAsJ2KReversible:	DecompressDicomFiles.class CompressDicomFiles.class
	#cp "/Volumes/Toshiba5TEnc/CluniePortable15TB3_Enc/MDDX/20160620_LargeCasesExpanding/128530/IM001" /tmp/testdecompress.dcm
	# CU7WJ28E has an "incorrect" PlanarConfiguration of 0, but read handles it being reorganizing it :(
	cp "$${HOME}/Pictures/Medical/RLE/CU7WJ28E" /tmp/testdecompress.dcm
	mkdir -p /tmp/testdecompress.output
	rm -f /tmp/testdecompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testdecompress.dcm /tmp/testdecompress.output
	mkdir -p /tmp/testcompress.output
	rm -f /tmp/testcompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testdecompress.output/* /tmp/testcompress.output
	andiff /tmp/testdecompress.dcm /tmp/testcompress.output/*
	mkdir -p /tmp/testdecompressrecompress.output
	rm -f /tmp/testdecompressrecompress.output/*
	#ls -l /tmp/testdecompressrecompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testcompress.output/* /tmp/testdecompressrecompress.output
	#ls -l /tmp/testdecompressrecompress.output
	dctoraw /tmp/testdecompressrecompress.output/* /tmp/testdecompressrecompress.output_decompressed.raw
	dctoraw /tmp/testdecompress.output/* /tmp/testdecompress.output.raw
	@echo "Comparing raw output of decompressed original RLE file with decompressed recompressed J2K file ..."
	cmp /tmp/testdecompress.output.raw /tmp/testdecompressrecompress.output_decompressed.raw
	rm -rf /tmp/testdecompress.dcm /tmp/testdecompress.output /tmp/testcompress.output /tmp/testdecompressrecompress.output /tmp/testdecompress.output.raw /tmp/testdecompressrecompress.output_decompressed.raw

testrecompressRGBRLEAsJ2KReversible_onestep:	DecompressDicomFiles.class CompressDicomFiles.class
	# CU7WJ28E has an "incorrect" PlanarConfiguration of 0, but read handles it being reorganizing it :(
	cp "$${HOME}/Pictures/Medical/RLE/CU7WJ28E" /tmp/testdecompress.dcm
	mkdir -p /tmp/testdecompress.output
	rm -f /tmp/testdecompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testdecompress.dcm /tmp/testdecompress.output
	mkdir -p /tmp/testcompress.output
	rm -f /tmp/testcompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testdecompress.dcm /tmp/testcompress.output
	andiff /tmp/testdecompress.dcm /tmp/testcompress.output/*
	mkdir -p /tmp/testdecompressrecompress.output
	rm -f /tmp/testdecompressrecompress.output/*
	#ls -l /tmp/testdecompressrecompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testcompress.output/* /tmp/testdecompressrecompress.output
	#ls -l /tmp/testdecompressrecompress.output
	dctoraw /tmp/testdecompressrecompress.output/* /tmp/testdecompressrecompress.output_decompressed.raw
	dctoraw /tmp/testdecompress.output/* /tmp/testdecompress.output.raw
	@echo "Comparing raw output of decompressed original RLE file with decompressed recompressed J2K file ..."
	cmp /tmp/testdecompress.output.raw /tmp/testdecompressrecompress.output_decompressed.raw
	rm -rf /tmp/testdecompress.dcm /tmp/testdecompress.output /tmp/testcompress.output /tmp/testdecompressrecompress.output /tmp/testdecompress.output.raw /tmp/testdecompressrecompress.output_decompressed.raw

testrecompressYBRFULLRLEAsRLE:	DecompressDicomFiles.class CompressDicomFiles.class
	#cp "$${HOME}/Pictures/Medical/RLE/view0002.dcm" /tmp/testdecompress.dcm
	cp "$${HOME}/Pictures/Medical/RLE/ge_ybrfull_rle_IM001.59.dcm" /tmp/testdecompress.dcm
	mkdir -p /tmp/testdecompress.output
	rm -f /tmp/testdecompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testdecompress.dcm /tmp/testdecompress.output
	mkdir -p /tmp/testcompress.output
	rm -f /tmp/testcompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testdecompress.output/* /tmp/testcompress.output rle
	andiff /tmp/testdecompress.dcm /tmp/testcompress.output/*
	mkdir -p /tmp/testdecompressrecompress.output
	rm -f /tmp/testdecompressrecompress.output/*
	#ls -l /tmp/testdecompressrecompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testcompress.output/* /tmp/testdecompressrecompress.output
	#ls -l /tmp/testdecompressrecompress.output
	dctoraw /tmp/testdecompressrecompress.output/* /tmp/testdecompressrecompress.output_decompressed.raw
	dctoraw /tmp/testdecompress.output/* /tmp/testdecompress.output.raw
	@echo "Comparing raw output of decompressed original RLE file with decompressed recompressed RLE file"
	cmp /tmp/testdecompress.output.raw /tmp/testdecompressrecompress.output_decompressed.raw
	rm -rf /tmp/testdecompress.dcm /tmp/testdecompress.output /tmp/testcompress.output /tmp/testdecompressrecompress.output /tmp/testdecompress.output.raw /tmp/testdecompressrecompress.output_decompressed.raw

testrecompressYBRFULLRLEAsJ2KReversible:	DecompressDicomFiles.class CompressDicomFiles.class
	#cp "$${HOME}/Pictures/Medical/RLE/view0002.dcm" /tmp/testdecompress.dcm
	cp "$${HOME}/Pictures/Medical/RLE/ge_ybrfull_rle_IM001.59.dcm" /tmp/testdecompress.dcm
	mkdir -p /tmp/testdecompress.output
	rm -f /tmp/testdecompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testdecompress.dcm /tmp/testdecompress.output
	@echo "PhotometricInterpretation of decompressed file is "`dckey -k PhotometricInterpretation /tmp/testdecompress.output/* 2>&1`
	@echo "PlanarConfiguration of decompressed file is "`dckey -k PlanarConfiguration /tmp/testdecompress.output/* 2>&1`
	mkdir -p /tmp/testcompress.output
	rm -f /tmp/testcompress.output/*
	# this will fail since only RGB to YBR_RCT allowed
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testdecompress.output/* /tmp/testcompress.output
	rm -rf /tmp/testdecompress.dcm /tmp/testdecompress.output /tmp/testcompress.output

testrecompressYBRFULLRLEAsJ2KReversible_onestep:	DecompressDicomFiles.class CompressDicomFiles.class
	#cp "$${HOME}/Pictures/Medical/RLE/view0002.dcm" /tmp/testdecompress.dcm
	cp "$${HOME}/Pictures/Medical/RLE/ge_ybrfull_rle_IM001.59.dcm" /tmp/testdecompress.dcm
	mkdir -p /tmp/testdecompress.output
	rm -f /tmp/testdecompress.output/*
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.apps.DecompressDicomFiles /tmp/testdecompress.dcm /tmp/testdecompress.output
	mkdir -p /tmp/testcompress.output
	rm -f /tmp/testcompress.output/*
	# this will fail since only RGB to YBR_RCT allowed
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testdecompress.dcm /tmp/testcompress.output
	rm -rf /tmp/testdecompress.dcm /tmp/testdecompress.output /tmp/testcompress.output

testcannotcompressnonimage:	CompressDicomFiles.class
	rm -rf /tmp/testcompress.smpte.dcm /tmp/testcompress.output
	cp ${PATHTOROOT}/testpaths/DoseSR/getestctinenhsr.dcm /tmp/testcompress.dcm
	mkdir -p /tmp/testcompress.output
	java -Xmx512m -cp .:${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.apps.CompressDicomFiles /tmp/testcompress.dcm /tmp/testcompress.output
	find /tmp/testcompress.output -type f
	rm -rf /tmp/testcompress.dcm /tmp/testcompress.output

testconvert8to16:	Convert8To16Bits.class
	dcsmpte /tmp/crap8.dcm
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.Convert8To16Bits \
		/tmp/crap8.dcm \
		/tmp/crap12.dcm \
		12
	dcstats /tmp/crap8.dcm
	dcstats /tmp/crap12.dcm
	dchist -h /tmp/crap8.dcm >/tmp/crap8.hist 2>&1
	dchist -h /tmp/crap12.dcm >/tmp/crap12.hist 2>&1
	diff /tmp/crap8.hist /tmp/crap12.hist
	rm -f /tmp/crap8.dcm /tmp/crap8.hist /tmp/crap12.dcm /tmp/crap12.hist

testdeidentifyjpeg:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >>/tmp/testdeidentifedregions.txt
	echo "640x480 = (300,200,100,80)" >>/tmp/testdeidentifedregions.txt
	echo "574x681 = (300,200,100,80)" >>/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v \
		"$${HOME}/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" \
		/tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.DeidentifyAndRedact=debug \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		PatientName "Doe^Jane" \
		PatientID "000000"
	#dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	#dcfile /tmp/testdeidentifedresult/*
	ls -l /tmp/testdeidentifedresult/*

	#"$${HOME}/Pictures/Medical/VL/Ophthalmology/Joslin/D0000001.DCM"
	#"$${HOME}/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031"
	#"$${HOME}/Documents/Work/NCI/Deidentification/ProjectZero/final/SHEPARD^MARY^ZELDA [7813644]/19990827 180011 [ - ]/Series 001 [XC - Patient]/1.dcm"  - redaction generates value outside supplied Huffman table range - throws exception (can work around using jpegtran with no arguments)

testdeidentifyjpegwithexplicitblock:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "$${HOME}/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		BLOCK \
		PatientName "Doe^Jane" \
		PatientID "000000"
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyjpegwithexplicitblockandallprivate:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "$${HOME}/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		BLOCK \
		KEEPALLPRIVATE \
		PatientName "Doe^Jane" \
		PatientID "000000"
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyjpegwithexplicitblockandsafeprivate:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "$${HOME}/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		BLOCK \
		KEEPSAFEPRIVATE \
		PatientName "Doe^Jane" \
		PatientID "000000"
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyjpegwithexplicitallprivate:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "$${HOME}/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		KEEPALLPRIVATE \
		PatientName "Doe^Jane" \
		PatientID "000000"
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyjpegwithexplicitallprivatenoreplacements:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "$${HOME}/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		KEEPALLPRIVATE
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyjpegwithexplicitsafeprivate:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "$${HOME}/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		KEEPSAFEPRIVATE \
		PatientName "Doe^Jane" \
		PatientID "000000"
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyjpegwithdecompress:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "640x480 = (0,0,128,34)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "$${HOME}/Pictures/Medical/NEMA/WG12/Acuson/Sequoia/EXAMS/EXAM0000/CLIPS/CLIP0031" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		DECOMPRESS \
		PatientName "Doe^Jane" \
		PatientID "000000"
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyuncompressed:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "512x512 = (0,0,511,150)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	dcsmpte /tmp/testdeidentifysource/testfile.dcm -r PatientName "Smith^Mary"
	touch /tmp/testdeidentifysource/emptyfile.dcm	# a bad one to check it is rejected and reported
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyuncompressedwithoutcontributingequipment:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "512x512 = (0,0,511,150)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	dcsmpte /tmp/testdeidentifysource/testfile.dcm -r PatientName "Smith^Mary"
	touch /tmp/testdeidentifysource/emptyfile.dcm	# a bad one to check it is rejected and reported
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		DONOTADDCONTRIBUTINGEQUIPMENT
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyuncompressedivrlemh:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "512x512 = (0,0,511,150)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	dcsmpte /tmp/testdeidentifysource/testfile.dcm -r PatientName "Smith^Mary" -output-endian little -output-vr implicit
	dcfile /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyuncompressedivrlenomh:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "512x512 = (0,0,511,150)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	dcsmpte /tmp/testdeidentifysource/testfile.dcm -r PatientName "Smith^Mary" -output-endian little -output-vr implicit -nometa
	dcfile /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifynoredaction:	DeidentifyAndRedact.class
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	dcsmpte /tmp/testdeidentifysource/testfile.dcm -r PatientName "Smith^Mary"
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		""
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifyrle:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "800x600 = (0,0,569,59)" >/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifysource
	rm -rf /tmp/testdeidentifysource/*
	cp -v "$${HOME}/Pictures/Medical/RLE/US16BitsPaletteColorRLE.dcm" /tmp/testdeidentifysource/testfile.dcm
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		/tmp/testdeidentifysource \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt
	dcdiff /tmp/testdeidentifysource/testfile.dcm /tmp/testdeidentifedresult/*
	dcfile /tmp/testdeidentifedresult/*

testdeidentifycoordinatecheck:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "1024x768 = (10, 9, 520, 62)" >/tmp/testdeidentifedregions.txt
	echo "1024x768 = (530, 35, 340, 36)" >>/tmp/testdeidentifedregions.txt
	echo "800x600 = (10, 2, 400, 52)" >>/tmp/testdeidentifedregions.txt
	echo "800x600 = (510, 35, 265, 19)" >>/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		"$${HOME}/Documents/Clients/MDDX/20140611_Coordinates/13395/397511552810" \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		PatientName "Doe^Jane" \
		PatientID "000000"

testdeidentifycoordinatecheckwrongmf:	DeidentifyAndRedact.class
	rm -f /tmp/testdeidentifedregions.txt
	echo "800x600 = (10, 9, 520, 62)" >/tmp/testdeidentifedregions.txt
	echo "800x600 = (530, 35, 340, 36)" >>/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		"$${HOME}/Documents/Clients/MDDX/20140611_Coordinates/13395/397511552810/BOV549E8" \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		PatientName "Doe^Jane" \
		PatientID "000000"

testdeidentifycoordinatecheckanothersf:	DeidentifyAndRedact.class
	echo "1024x768 = (10, 9, 520, 62)" >/tmp/testdeidentifedregions.txt
	echo "1024x768 = (530, 35, 340, 36)" >>/tmp/testdeidentifedregions.txt
	mkdir -p /tmp/testdeidentifedresult
	rm -rf /tmp/testdeidentifedresult/*
	java -Xmx512m -cp .:${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} -Djava.awt.headless=true \
		com.pixelmed.apps.DeidentifyAndRedact \
		"$${HOME}/Documents/Clients/MDDX/20140611_Coordinates/file9_anon" \
		/tmp/testdeidentifedresult \
		/tmp/testdeidentifedregions.txt \
		PatientName "Doe^Jane" \
		PatientID "000000"

testfindandcopysrbyuid:	FindAndCopySelectedDicomFiles.class
	mkdir -p /tmp/crap_src
	rm -f /tmp/crap_src/*
	mkdir -p /tmp/crap_dst
	rm -f /tmp/crap_dst/*
	dcsmpte /tmp/crap_src/sc.dcm
	dcsmpte /tmp/crap_src/sr.dcm -r SOPClassUID 1.2.840.10008.5.1.4.1.1.88.11
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.FindAndCopySelectedDicomFiles \
		/tmp/crap_src \
		/tmp/crap_dst \
		1.2.840.10008.5.1.4.1.1.88.11
	ls -l /tmp/crap_dst

testfindandcopysrbymodality:	FindAndCopySelectedDicomFiles.class
	mkdir -p /tmp/crap_src
	rm -f /tmp/crap_src/*
	mkdir -p /tmp/crap_dst
	rm -f /tmp/crap_dst/*
	dcsmpte /tmp/crap_src/sc.dcm
	dcsmpte /tmp/crap_src/sr.dcm -r SOPClassUID 1.2.840.10008.5.1.4.1.1.88.11
	java -Xmx512m -cp .:${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.FindAndCopySelectedDicomFiles \
		/tmp/crap_src \
		/tmp/crap_dst \
		SR
	ls -l /tmp/crap_dst

testmergefunctionalgroupsdefault:	MergeFunctionalGroups.class
	rm -rf /tmp/crapx
	mkdir /tmp/crapx
	cp "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Bruker/DICOM/EnhancedMR/H13885_9M_H13885_PKD__E4_P1/EnIm1.dcm" /tmp/crapx/1.dcm
	cp "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Converted/DICOM/MT_H13885_Processed.dcm" /tmp/crapx/2.dcm
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeFunctionalGroups \
			/tmp/crapx/1.dcm \
			/tmp/crapx/2.dcm \
			/tmp/crapd

testmergefunctionalgroupsspatial:	MergeFunctionalGroups.class
	rm -rf /tmp/crapx
	mkdir /tmp/crapx
	cp "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Bruker/DICOM/EnhancedMR/H13885_9M_H13885_PKD__E4_P1/EnIm1.dcm" /tmp/crapx/1.dcm
	cp "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Converted/DICOM/MT_H13885_Processed.dcm" /tmp/crapx/2.dcm
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeFunctionalGroups \
			-spatial \
			/tmp/crapx/1.dcm \
			/tmp/crapx/2.dcm \
			/tmp/crapd

testmergefunctionalgroupsframecontent:	MergeFunctionalGroups.class
	rm -rf /tmp/crapx
	mkdir /tmp/crapx
	cp "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Bruker/DICOM/EnhancedMR/H13885_9M_H13885_PKD__E4_P1/EnIm1.dcm" /tmp/crapx/1.dcm
	cp "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Converted/DICOM/MT_H13885_Processed.dcm" /tmp/crapx/2.dcm
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.MergeFunctionalGroups \
			-framecontent \
			/tmp/crapx/1.dcm \
			/tmp/crapx/2.dcm \
			/tmp/crapd

testsetcharacteristics:	SetCharacteristicsFromSummary.class
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"options\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"ReplaceCodingSchemeIdentificationSequence\" : true,"
	echo >>/tmp/testsetcharacteristics.json "		\"AppendToContributingEquipmentSequence\" : true"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"remove\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"PhotometricInterpretation\" : null",
	echo >>/tmp/testsetcharacteristics.json "		\"00204000\" : null"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"top\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"00080064\" : \"SYN\","
	echo >>/tmp/testsetcharacteristics.json "		\"PatientSpeciesDescription\" : \"Mouse\","
	echo >>/tmp/testsetcharacteristics.json "		\"PatientBreedCodeSequence\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"BreedRegistrationSequence\" : ["
	echo >>/tmp/testsetcharacteristics.json "			{"
	echo >>/tmp/testsetcharacteristics.json "				\"BreedRegistrationNumber\" : \"1234\","
	echo >>/tmp/testsetcharacteristics.json "				\"BreedRegistryCodeSequence\" : { \"cv\" : \"109200\", \"csd\" : \"DCM\", \"cm\" : \"America Kennel Club\" }"
	echo >>/tmp/testsetcharacteristics.json "			},"
	echo >>/tmp/testsetcharacteristics.json "			{"
	echo >>/tmp/testsetcharacteristics.json "				\"BreedRegistrationNumber\" : \"5678\","
	echo >>/tmp/testsetcharacteristics.json "				\"BreedRegistryCodeSequence\" : { \"cv\" : \"109202\", \"csd\" : \"DCM\", \"cm\" : \"American Canine Association\" }"
	echo >>/tmp/testsetcharacteristics.json "			}"
	echo >>/tmp/testsetcharacteristics.json "		],"
	echo >>/tmp/testsetcharacteristics.json "		\"PatientSexNeutered\" : \"\","
	echo >>/tmp/testsetcharacteristics.json "		\"StudyID\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"AccessionNumber\" : [],"
	echo >>/tmp/testsetcharacteristics.json "		\"ReferencedStudySequence\" : [],"
	echo >>/tmp/testsetcharacteristics.json "		\"ImageType\" : [ \"DERIVED\", \"PRIMARY\", \"MTR\", \"RATIO\" ],"
	echo >>/tmp/testsetcharacteristics.json "		\"OriginalAttributesSequence\" : ["
	echo >>/tmp/testsetcharacteristics.json "			{"
	echo >>/tmp/testsetcharacteristics.json "				\"SourceOfPreviousValues\" : null,"
	echo >>/tmp/testsetcharacteristics.json "				\"AttributeModificationDateTime\" : \"20191225045800\","
	echo >>/tmp/testsetcharacteristics.json "				\"ModifyingSystem\" : \"My Workstation\","
	echo >>/tmp/testsetcharacteristics.json "				\"ReasonForTheAttributeModification\" : \"COERCE\","
	echo >>/tmp/testsetcharacteristics.json "				\"ModifiedAttributesSequence\" : ["
	echo >>/tmp/testsetcharacteristics.json "					{"
	echo >>/tmp/testsetcharacteristics.json "						\"PatientName\" : \"Mr. Mouse\","
	echo >>/tmp/testsetcharacteristics.json "						\"PatientID\" : \"56789\""
	echo >>/tmp/testsetcharacteristics.json "					}"
	echo >>/tmp/testsetcharacteristics.json "				]"
	echo >>/tmp/testsetcharacteristics.json "			}"
	echo >>/tmp/testsetcharacteristics.json "		]"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"FrameAnatomySequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"AnatomicRegionSequence\" : { \"cv\" : \"T-71000\", \"csd\" : \"SRT\", \"cm\" : \"Kidney\" },"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameLaterality\" : \"B\""
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"ParametricMapFrameTypeSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameType\" : [ \"DERIVED\", \"PRIMARY\", \"MTR\", \"RATIO\" ]"
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	cat -n /tmp/testsetcharacteristics.json
	rm -rf /tmp/crapx
	mkdir /tmp/crapx
	dcsmpte /tmp/crapx/testsetcharacteristics.dcm
	rm -rf /tmp/crapd
	mkdir /tmp/crapd
	rm -f /tmp/testsetcharacteristics.log
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.SetCharacteristicsFromSummary \
			/tmp/testsetcharacteristics.json \
			/tmp/crapx/testsetcharacteristics.dcm \
			/tmp/crapd \
		2>/tmp/testsetcharacteristics.log
	cat /tmp/testsetcharacteristics.log
	rm -f /tmp/filetouse.dat
	grep </tmp/testsetcharacteristics.log 'Writing with characteristics set file' | tail -1 | sed -e 's/^.*\/tmp/\/tmp/' >/tmp/filetouse.dat
	dcdiff /tmp/crapx/testsetcharacteristics.dcm "`cat /tmp/filetouse.dat`"

testtiledpyramid:	TiledPyramid.class
	mkdir -p "/tmp/pyramid"
	(cd "/tmp/"; rm -rf pyramid/*)
	java -Djava.awt.headless=true -XX:-UseGCOverheadLimit -Xmx2g \
		-cp ${PATHTOROOT}:${JIIOADDITIONALJARS} \
		-Dorg.slf4j.simpleLogger.showDateTime=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.TiledPyramid=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dicom.TiledFramesIndex=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dicom.AttributeList=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.display.SourceImage=info \
		com.pixelmed.apps.TiledPyramid \
		"/Volumes/VTK1TB/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG26/Philips_RCT/Philips^Amy [123456]/20181003 095253/Series 001 [SM]/CSTORERQ_SM_1.3.46.670589.45.1.1.4993912214784.1.5436.1538560606509.5-iwatt.dcm" \
		"/tmp/pyramid"
	ls -l "/tmp/pyramid"

		#"$${HOME}/Pictures/Medical/NEMA/WG26/Hamamatsu/Human_15x15_20x.dcm"
		#"$${HOME}/Pictures/Medical/WSI/WG26Demo2017/AsOf20170923/wsiimages/Tulip^Chris [P-45214579]/20121212 000000 [T13 0121]/Series 003 [SM]/1.3.46.670589.45.1.1.219124696743444.1.4600.1388967452753.3.dcm"
		#"$${HOME}/Pictures/Medical/WSI/WG26Demo2017/Philips_tests/CSTORERQ_SM_1.3.46.670589.45.1.1.18115406402816.1.4524.1506070439276.3-iwatt.dcm.updated.dcm"
		#"$${HOME}/Pictures/Medical/WSI/WG26Demo2017/Philips_tests/test-reverse.dcm.updated.dcm"
		#"$${HOME}/Pictures/Medical/WSI/WG26Demo2017/Philips_tests/testZoomInTopLeft12x4-4.dcm"
		#"$${HOME}/Pictures/Medical/WSI/WG26Demo2017/Philips_tests/D17-1001.dcm.updated.dcm"
		#"$${HOME}/Pictures/Medical/WSI/WG26Demo2017/Philips/D17-1001.dcm"
		#"$${HOME}/Pictures/Medical/WSI/WG26Demo2017/Philips/D17-2001.dcm"
		#"$${HOME}/Pictures/Medical/WSI/WG26Demo2017/Philips/D17-2002.dcm"
		#"$${HOME}/Pictures/Medical/WSI/WG26Demo2017/Philips/D17-3001.dcm"
		#"$${HOME}/Pictures/Medical/WSI/WG26Demo2017/Philips/D17-3002.dcm"
		#"$${HOME}/Pictures/Medical/WSI/WG26Demo2017/Philips/D17-2001.dcm"
		#"/Volumes/Extreme 500/Pictures/Medical/WSI/WG26Demo2017/AsOf20171205_fromConnectathon/wsiimages/Alpha^Amy [P-2811523466]/20170928 115236/Series 001 [SM]/1.3.46.670589.45.1.1.13461215855660.1.2548.1506597645355.5.dcm"
		#"/Volumes/Extreme 500/Pictures/Medical/WSI/WG26Demo2017/AsOf20171205_fromConnectathon/wsiimages/Alpha^Amy [P-2811523466]/20170928 115236/Series 002 [SM]/1.3.46.670589.45.1.1.13461215855660.1.2548.1506597613336.2.dcm"
		#"/Volumes/Extreme 500/Pictures/Medical/WSI/Philips_20180419/CSTORERQ_SM_1.3.46.670589.45.1.1.18115406402816.1.4524.1506070519391.5-iwatt.dcm"
		#"/Volumes/Extreme 500/Pictures/Medical/WSI/Philips_20180419/CSTORERQ_SM_1.3.46.670589.45.1.1.18115406402816.1.4524.1506070659988.5-iwatt.dcm"
		#"$${HOME}/Pictures/Medical/WSI_More/PathcoreServer_201804_05_Connectathon/wsiimages/Check^Chris [345678]/20170926 151841/Series 001 [SM]/1.3.46.670589.45.1.1.18115406402816.1.4524.1506070659988.5.dcm"
		#"$${HOME}/Pictures/Medical/WSI_More/PathcoreServer_201804_05_Connectathon/wsiimages/Bob^Beta [234567]/20170926 152421/Series 002 [SM]/1.3.46.670589.45.1.1.18115406402816.1.4524.1506070537188.5.dcm"
		#"$${HOME}/Pictures/Medical/WSI_More/PathcoreServer_201804_05_Connectathon/wsiimages/Bob^Beta [234567]/20170926 152421/Series 001 [SM]/1.3.46.670589.45.1.1.18115406402816.1.4524.1506070652863.5.dcm"
		#"$${HOME}/Pictures/Medical/WSI_More/Philips_20180515_ForECDP/D17-1001/CSTORERQ_SM_1.3.46.670589.45.1.1.18115406402816.1.4524.1506070439276.3-iwatt.dcm"
		#"/Volumes/VTK1TB/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG26/Philips_RCT/Philips^Amy [123456]/20181003 095253/Series 001 [SM]/CSTORERQ_SM_1.3.46.670589.45.1.1.4993912214784.1.5436.1538560606509.5-iwatt.dcm"

testtabulateseg:	TabulateSegmentSequence.class
	rm -f /tmp/testtabulateseg.dat
	(cd ../convert; make /tmp/seg2013_nac_hncma.dcm)
	java -Xms512m -Xmx512m -cp ${PATHTOROOT} com.pixelmed.apps.TabulateSegmentSequence \
		/tmp/seg2013_nac_hncma.dcm >/tmp/testtabulateseg.dat
	cat /tmp/testtabulateseg.dat

testpseudorandom:	PseudonymizeAndSequester.class
	dcsmpte /tmp/testpseudoinput.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2"\
		-r StudyDescription "I am a happy study" \
		-r SeriesDescription "I am a nice series" \
		-r ProtocolName "Good protocol" \
		-r PatientWeight "32" \
		-r DeviceSerialNumber "12345" \
		-r InstitutionName "My Hospital" \
		-r StudyDate "20180921"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt
	find /tmp/testpseudoclean -name '*.dcm' -exec dcdiff /tmp/testpseudoinput.dcm '{}' ';'
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudorandomnocontrib:	PseudonymizeAndSequester.class
	dcsmpte /tmp/testpseudoinput.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2"\
		-r StudyDescription "I am a happy study" \
		-r SeriesDescription "I am a nice series" \
		-r ProtocolName "Good protocol" \
		-r PatientWeight "32" \
		-r DeviceSerialNumber "12345" \
		-r InstitutionName "My Hospital" \
		-r StudyDate "20180921"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
	/tmp/testpseudoinput.dcm \
	/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		DONOTADDCONTRIBUTINGEQUIPMENT
	find /tmp/testpseudoclean -name '*.dcm' -exec dcdiff /tmp/testpseudoinput.dcm '{}' ';'
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudorandomkeepnothing:	PseudonymizeAndSequester.class
	rm -rf /tmp/contentsequence.ancreate
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa730) SQ Content Sequence 	 VR=<SQ>   VL=<0xffffffff>  	[]'
	@echo >>/tmp/contentsequence.ancreate '%item'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa040) CS Value Type 	 VR=<CS>   VL=<0x0004>  <TEXT>'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa043) SQ Concept Name Code Sequence 	 VR=<SQ>   VL=<0xffffffff>  	[]'
	@echo >>/tmp/contentsequence.ancreate '%item'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0100) SH Code Value    VR=<SH>   VL=<0x0006>  <121022>'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0102) SH Coding Scheme Designator      VR=<SH>   VL=<0x0004>  <DCM >'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0104) LO Code Meaning          VR=<LO>   VL=<0x0004>  <Accession Number>'
	@echo >>/tmp/contentsequence.ancreate '%enditem'
	@echo >>/tmp/contentsequence.ancreate '%endseq'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa160) UT Text Value 	 VR=<UT>   VL=<0x0006>  <123456>'
	@echo >>/tmp/contentsequence.ancreate '%enditem'
	@echo >>/tmp/contentsequence.ancreate '%endseq'
	ancreate -e </tmp/contentsequence.ancreate >/tmp/contentsequence.dcm
	dcsmpte /tmp/testpseudoinputnoseq.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2" \
		-r StudyDescription "I am a happy study" \
		-r SeriesDescription "I am a nice series" \
		-r ProtocolName "Good protocol" \
		-r PatientWeight "32" \
		-r DeviceSerialNumber "12345" \
		-r InstitutionName "My Hospital" \
		-r StudyDate "20180921"
	dcmerge -nodisclaimer -of /tmp/testpseudoinput.dcm /tmp/testpseudoinputnoseq.dcm /tmp/contentsequence.dcm
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		DONOTKEEPDESCRIPTORS \
		DONOTKEEPSERIESDESCRIPTORS \
		DONOTKEEPPROTOCOLNAME \
		DONOTKEEPPATIENTCHARACTERISTICS \
		DONOTKEEPDEVICEIDENTITY \
		DONOTKEEPINSTITUTIONIDENTITY \
		DONOTKEEPDATES \
		DONOTKEEPSTRUCTUREDCONTENT \
		DONOTADDCONTRIBUTINGEQUIPMENT
	find /tmp/testpseudoclean -name '*.dcm' -exec dcdiff /tmp/testpseudoinput.dcm '{}' ';'
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

/tmp/contentsequence.dcm:
	# use TEXT (113724, DCM, "Calibration Responsible Party") since action is D rather than X and not affected by other options like keep device identity, except keepDescriptors
	# use TEXT (125010, DCM, "Identifier") since action is X and not affected by other options
	# use PNAME (76, NCDR [2.0b], "Catheterization Operator") since action is D rather than X
	# use PNAME (121088, DCM, "Fellow") since action is X and not affected by other options
	# use DATE (127857, DCM, "Glucose Measurement Date")" since action is D rather than X but affected by date handling
	rm -rf /tmp/contentsequence.ancreate
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa730) SQ Content Sequence 	 VR=<SQ>   VL=<0xffffffff>  	[]'
	@echo >>/tmp/contentsequence.ancreate '%item'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa040) CS Value Type 	 VR=<CS>   VL=<0x0004>  <TEXT>'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa043) SQ Concept Name Code Sequence 	 VR=<SQ>   VL=<0xffffffff>  	[]'
	@echo >>/tmp/contentsequence.ancreate '%item'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0100) SH Code Value    VR=<SH>   VL=<0x0006>  <113724>'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0102) SH Coding Scheme Designator      VR=<SH>   VL=<0x0004>  <DCM >'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0104) LO Code Meaning          VR=<LO>   VL=<0x001e>  <Calibration Responsible Party>'
	@echo >>/tmp/contentsequence.ancreate '%enditem'
	@echo >>/tmp/contentsequence.ancreate '%endseq'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa160) UT Text Value 	 VR=<UT>   VL=<0x0006>  <123456>'
	@echo >>/tmp/contentsequence.ancreate '%enditem'
	@echo >>/tmp/contentsequence.ancreate '%item'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa040) CS Value Type 	 VR=<CS>   VL=<0x0004>  <TEXT>'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa043) SQ Concept Name Code Sequence 	 VR=<SQ>   VL=<0xffffffff>  	[]'
	@echo >>/tmp/contentsequence.ancreate '%item'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0100) SH Code Value    VR=<SH>   VL=<0x0006>  <125010>'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0102) SH Coding Scheme Designator      VR=<SH>   VL=<0x0004>  <DCM >'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0104) LO Code Meaning          VR=<LO>   VL=<0x000a>  <Identifier>'
	@echo >>/tmp/contentsequence.ancreate '%enditem'
	@echo >>/tmp/contentsequence.ancreate '%endseq'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa160) UT Text Value 	 VR=<UT>   VL=<0x0006>  <ABCDEF>'
	@echo >>/tmp/contentsequence.ancreate '%enditem'
	@echo >>/tmp/contentsequence.ancreate '%item'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa040) CS Value Type 	 VR=<CS>   VL=<0x0006>  <PNAME >'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa043) SQ Concept Name Code Sequence 	 VR=<SQ>   VL=<0xffffffff>  	[]'
	@echo >>/tmp/contentsequence.ancreate '%item'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0100) SH Code Value    VR=<SH>   VL=<0x0002>  <76>'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0102) SH Coding Scheme Designator      VR=<SH>   VL=<0x000C>  <NCDR [2.0b] >'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0104) LO Code Meaning          VR=<LO>   VL=<0x0018>  <Catheterization Operator>'
	@echo >>/tmp/contentsequence.ancreate '%enditem'
	@echo >>/tmp/contentsequence.ancreate '%endseq'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa123) PN Person Name 	 VR=<PN>   VL=<0x000C>  <Johnson^Mary>'
	@echo >>/tmp/contentsequence.ancreate '%enditem'
	@echo >>/tmp/contentsequence.ancreate '%item'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa040) CS Value Type 	 VR=<CS>   VL=<0x0006>  <PNAME >'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa043) SQ Concept Name Code Sequence 	 VR=<SQ>   VL=<0xffffffff>  	[]'
	@echo >>/tmp/contentsequence.ancreate '%item'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0100) SH Code Value    VR=<SH>   VL=<0x0006>  <121088>'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0102) SH Coding Scheme Designator      VR=<SH>   VL=<0x0004>  <DCM >'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0104) LO Code Meaning          VR=<LO>   VL=<0x0006>  <Fellow>'
	@echo >>/tmp/contentsequence.ancreate '%enditem'
	@echo >>/tmp/contentsequence.ancreate '%endseq'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa123) PN Person Name 	 VR=<PN>   VL=<0x000E>  <Jones^David^Dr>'
	@echo >>/tmp/contentsequence.ancreate '%enditem'
	@echo >>/tmp/contentsequence.ancreate '%item'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa040) CS Value Type 	 VR=<CS>   VL=<0x0004>  <DATE>'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa043) SQ Concept Name Code Sequence 	 VR=<SQ>   VL=<0xffffffff>  	[]'
	@echo >>/tmp/contentsequence.ancreate '%item'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0100) SH Code Value    VR=<SH>   VL=<0x0006>  <127857>'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0102) SH Coding Scheme Designator      VR=<SH>   VL=<0x0004>  <DCM >'
	@echo >>/tmp/contentsequence.ancreate '(0x0008,0x0104) LO Code Meaning          VR=<LO>   VL=<0x0018>  <Glucose Measurement Date>'
	@echo >>/tmp/contentsequence.ancreate '%enditem'
	@echo >>/tmp/contentsequence.ancreate '%endseq'
	@echo >>/tmp/contentsequence.ancreate '(0x0040,0xa121) DA Date 	 VR=<DA>   VL=<0x0008>  <20181012>'
	@echo >>/tmp/contentsequence.ancreate '%enditem'
	@echo >>/tmp/contentsequence.ancreate '%endseq'
	ancreate -e </tmp/contentsequence.ancreate >/tmp/contentsequence.dcm

testpseudorandommodifystructuredcontentanddates:	PseudonymizeAndSequester.class /tmp/contentsequence.dcm
	dcsmpte /tmp/testpseudoinputnoseq.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2"
	dcmerge -nodisclaimer -of /tmp/testpseudoinput.dcm /tmp/testpseudoinputnoseq.dcm /tmp/contentsequence.dcm
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		DONOTKEEPDESCRIPTORS \
		MODIFYDATES \
		MODIFYSTRUCTUREDCONTENT \
		DONOTADDCONTRIBUTINGEQUIPMENT
	@echo "Expect 113724 TextValue to have been replaced ..."
	@echo "Expect 125010 TextValue to have been removed ..."
	@echo "Expect 76 PersonName to have been replaced ..."
	@echo "Expect 121088 PersonName to have been removed ..."
	@echo "Expect 127857 Date to have been replaced ..."
	find /tmp/testpseudoclean -name '*.dcm' -exec dcdiff /tmp/testpseudoinput.dcm '{}' ';'
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudorandommodifystructuredcontentbutremovedates:	PseudonymizeAndSequester.class /tmp/contentsequence.dcm
	dcsmpte /tmp/testpseudoinputnoseq.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2"
	dcmerge -nodisclaimer -of /tmp/testpseudoinput.dcm /tmp/testpseudoinputnoseq.dcm /tmp/contentsequence.dcm
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		DONOTKEEPDESCRIPTORS \
		DONOTKEEPDATES \
		MODIFYSTRUCTUREDCONTENT \
		DONOTADDCONTRIBUTINGEQUIPMENT
	@echo "Expect 113724 TextValue to have been replaced ..."
	@echo "Expect 125010 TextValue to have been removed ..."
	@echo "Expect 76 PersonName to have been replaced ..."
	@echo "Expect 121088 PersonName to have been removed ..."
	@echo "Expect 127857 Date to have been replaced with fixed (rather than epoch adjusted) value ..."
	find /tmp/testpseudoclean -name '*.dcm' -exec dcdiff /tmp/testpseudoinput.dcm '{}' ';'
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudorandomremovestructuredcontent:	PseudonymizeAndSequester.class /tmp/contentsequence.dcm
	dcsmpte /tmp/testpseudoinputnoseq.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2"
	dcmerge -nodisclaimer -of /tmp/testpseudoinput.dcm /tmp/testpseudoinputnoseq.dcm /tmp/contentsequence.dcm
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		DONOTKEEPDESCRIPTORS \
		MODIFYDATES \
		DONOTKEEPSTRUCTUREDCONTENT \
		DONOTADDCONTRIBUTINGEQUIPMENT
	@echo "Expect only top level empty ContentSequence to remain ..."
	find /tmp/testpseudoclean -name '*.dcm' -exec dcdiff /tmp/testpseudoinput.dcm '{}' ';'
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudorandommodifydates:	PseudonymizeAndSequester.class
	mkdir -p /tmp/testpseudoinput
	rm -rf /tmp/testpseudoinput/*
	dcsmpte /tmp/testpseudoinput/clean.dcm -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2" \
		-r StudyDate "20180921"
	dcsmpte /tmp/testpseudoinput/dirty.dcm -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "56" -r Modality OT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2" \
		-r StudyDate "20181021"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		MODIFYDATES \
		DONOTADDCONTRIBUTINGEQUIPMENT
	find /tmp/testpseudoclean -name '*.dcm' -exec dcdiff /tmp/testpseudoinput/clean.dcm '{}' ';'
	find /tmp/testpseudodirty -name '*.dcm' -exec dcdiff /tmp/testpseudoinput/dirty.dcm '{}' ';'
	@dctable -k StudyDate /tmp/testpseudoinput/clean.dcm
	@find /tmp/testpseudoclean -name '*.dcm' -exec dctable -k StudyDate '{}' ';'
	@dctable -k StudyDate /tmp/testpseudoinput/dirty.dcm
	@find /tmp/testpseudodirty -name '*.dcm' -exec dctable -k StudyDate '{}' ';'

testpseudorandombadarg:	PseudonymizeAndSequester.class
	dcsmpte /tmp/testpseudoinput.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		BADARG

testpseudorandomseed:	PseudonymizeAndSequester.class
	dcsmpte /tmp/testpseudoinput.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	dcdiff /tmp/testpseudoinput.dcm "/tmp/testpseudoclean/Nobody^7RL2Y8 [7RL2Y8]/19000101 000000/Series 000 [CT]/000045.dcm"
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudorandomseedanatomyct:	PseudonymizeAndSequester.class
	dcsmpte /tmp/testpseudoinput.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2" \
		-r BodyPartExamined "KIDNEY" \
		-r Laterality "L"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	dcdiff /tmp/testpseudoinput.dcm "/tmp/testpseudoclean/Nobody^7RL2Y8 [7RL2Y8]/19000101 000000/Series 000 [CT]/000045_KIDNEY_L.dcm"
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudorandomseedanatomycr:	PseudonymizeAndSequester.class
	# need -nodisclaimer else "AP" in "THE OUTPUT OF THIS SOFTWARE IS FOR INVESTIGATIONAL USE ONLY - NOT TESTED OR APPROVED FOR CLINICAL APPLICATION" is detected as AP view :(
	dcsmpte /tmp/testpseudoinput.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CR -r SOPClassUID "1.2.840.10008.5.1.4.1.1.1" \
		-r BodyPartExamined "FOOT" \
		-r Laterality "R" \
		-r ViewPosition "AP"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	# 	-Dorg.slf4j.simpleLogger.log.com.pixelmed.anatproc.ProjectionXRayAnatomy=debug
	java -cp ${PATHTOROOT} \
		com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	dcdiff /tmp/testpseudoinput.dcm "/tmp/testpseudoclean/Nobody^7RL2Y8 [7RL2Y8]/19000101 000000/Series 000 [CR]/000045_FOOT_RAP.dcm"
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudorandomseedanatomydx:	PseudonymizeAndSequester.class
	rm -rf /tmp/viewcodesequence.ancreate
	@echo >>/tmp/viewcodesequence.ancreate '(0x0008,0x2218) SQ Anatomic Region Sequence      VR=<SQ>   VL=<0xffffffff>      []'
	@echo >>/tmp/viewcodesequence.ancreate '%item'
	@echo >>/tmp/viewcodesequence.ancreate '(0x0008,0x0100) SH Code Value    VR=<SH>   VL=<0x0008>  <T-D9700 >'
	@echo >>/tmp/viewcodesequence.ancreate '(0x0008,0x0102) SH Coding Scheme Designator      VR=<SH>   VL=<0x0004>  <SRT >'
	@echo >>/tmp/viewcodesequence.ancreate '(0x0008,0x0104) LO Code Meaning          VR=<LO>   VL=<0x0004>  <FOOT>'
	@echo >>/tmp/viewcodesequence.ancreate '%enditem'
	@echo >>/tmp/viewcodesequence.ancreate '%endseq'
	@echo >>/tmp/viewcodesequence.ancreate '(0x0054,0x0220) SQ View Code Sequence    VR=<SQ>   VL=<0xffffffff>      []'
	@echo >>/tmp/viewcodesequence.ancreate '%item'
	@echo >>/tmp/viewcodesequence.ancreate '(0x0008,0x0100) SH Code Value    VR=<SH>   VL=<0x0008>  <R-10206 >'
	@echo >>/tmp/viewcodesequence.ancreate '(0x0008,0x0102) SH Coding Scheme Designator      VR=<SH>   VL=<0x0004>  <SRT >'
	@echo >>/tmp/viewcodesequence.ancreate '(0x0008,0x0104) LO Code Meaning          VR=<LO>   VL=<0x000e>  <antero-posterior>'
	@echo >>/tmp/viewcodesequence.ancreate '(0x0054,0x0222) SQ View Modifier Code Sequence   VR=<SQ>   VL=<0xffffffff>      []'
	@echo >>/tmp/viewcodesequence.ancreate '%endseq'
	@echo >>/tmp/viewcodesequence.ancreate '%enditem'
	@echo >>/tmp/viewcodesequence.ancreate '%endseq'
	ancreate -e </tmp/viewcodesequence.ancreate >/tmp/viewcodesequence.dcm
	dcsmpte /tmp/testpseudoinputnoseq.dcm -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality DX -r SOPClassUID "1.2.840.10008.5.1.4.1.1.1.1" \
		-r ImageLaterality "R"
	dcmerge -nodisclaimer -of /tmp/testpseudoinput.dcm /tmp/testpseudoinputnoseq.dcm /tmp/viewcodesequence.dcm
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	dcdiff /tmp/testpseudoinput.dcm "/tmp/testpseudoclean/Nobody^7RL2Y8 [7RL2Y8]/19000101 000000/Series 000 [DX]/000045_FOOT_RAP.dcm"
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudorandomseedanatomymg:	PseudonymizeAndSequester.class
	rm -rf /tmp/viewcodesequence.ancreate
	@echo >>/tmp/viewcodesequence.ancreate '(0x0054,0x0220) SQ View Code Sequence    VR=<SQ>   VL=<0xffffffff>      []'
	@echo >>/tmp/viewcodesequence.ancreate '%item'
	@echo >>/tmp/viewcodesequence.ancreate '(0x0008,0x0100) SH Code Value    VR=<SH>   VL=<0x0008>  <R-10242 >'
	@echo >>/tmp/viewcodesequence.ancreate '(0x0008,0x0102) SH Coding Scheme Designator      VR=<SH>   VL=<0x0004>  <SRT >'
	@echo >>/tmp/viewcodesequence.ancreate '(0x0008,0x0104) LO Code Meaning          VR=<LO>   VL=<0x000e>  <cranio-caudal >'
	@echo >>/tmp/viewcodesequence.ancreate '(0x0054,0x0222) SQ View Modifier Code Sequence   VR=<SQ>   VL=<0xffffffff>      []'
	@echo >>/tmp/viewcodesequence.ancreate '%endseq'
	@echo >>/tmp/viewcodesequence.ancreate '%enditem'
	@echo >>/tmp/viewcodesequence.ancreate '%endseq'
	ancreate -e </tmp/viewcodesequence.ancreate >/tmp/viewcodesequence.dcm
	dcsmpte /tmp/testpseudoinputnoseq.dcm -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality MG -r SOPClassUID "1.2.840.10008.5.1.4.1.1.1.2.1" \
		-r ImageLaterality "L"
	dcmerge -nodisclaimer -of /tmp/testpseudoinput.dcm /tmp/testpseudoinputnoseq.dcm /tmp/viewcodesequence.dcm
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	dcdiff /tmp/testpseudoinput.dcm "/tmp/testpseudoclean/Nobody^7RL2Y8 [7RL2Y8]/19000101 000000/Series 000 [MG]/000045_BREAST_LCC.dcm"
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudodirty1randomseed:	PseudonymizeAndSequester.class
	dcsmpte /tmp/testpseudoinput.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r ImageType "DERIVED\\PRIMARY" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	dcdiff /tmp/testpseudoinput.dcm "/tmp/testpseudodirty/Nobody^7RL2Y8 [7RL2Y8]/19000101 000000/Series 000 [CT]/000045.dcm"
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudodirty2randomseed:	PseudonymizeAndSequester.class
	# use modality CT to force check is on SOP Class
	dcsmpte /tmp/testpseudoinput.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.7"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	dcdiff /tmp/testpseudoinput.dcm "/tmp/testpseudodirty/Nobody^7RL2Y8 [7RL2Y8]/19000101 000000/Series 000 [CT]/000045.dcm"
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudodirty3randomseed:	PseudonymizeAndSequester.class
	# use modality CT to force check is on SOP Class
	dcsmpte /tmp/testpseudoinput.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.11.3"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	dcdiff /tmp/testpseudoinput.dcm "/tmp/testpseudodirty/Nobody^7RL2Y8 [7RL2Y8]/19000101 000000/Series 000 [CT]/000045.dcm"
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudodirtydetectortypefilm:	PseudonymizeAndSequester.class
	dcsmpte /tmp/testpseudoinput.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r DetectorType "FILM" -r Modality MG -r SOPClassUID "1.2.840.10008.5.1.4.1.1.1.2"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	#find /tmp/testpseudodirty -name '*.dcm' -exec dcdiff /tmp/testpseudoinput.dcm '{}' ';'
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudocleandetectortypenotfilm:	PseudonymizeAndSequester.class
	dcsmpte /tmp/testpseudoinput.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r DetectorType "DIRECT" -r Modality MG -r SOPClassUID "1.2.840.10008.5.1.4.1.1.1.2"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	#find /tmp/testpseudodirty -name '*.dcm' -exec dcdiff /tmp/testpseudoinput.dcm '{}' ';'
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudomammopluscadrandomseed:	PseudonymizeAndSequester.class
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/ImagesWithCAD \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	find /tmp/testpseudodirty -name '*.dcm' -exec dcdiff /tmp/ImagesWithCAD/iCADResults.dcm '{}' ';'
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudodosescreens:	PseudonymizeAndSequester.class
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.apps.PseudonymizeAndSequester \
		$${HOME}/Pictures/Medical/DoseImages \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudodosescreensmodifydates:	PseudonymizeAndSequester.class
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.apps.PseudonymizeAndSequester \
		$${HOME}/Pictures/Medical/DoseImages \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465 \
		MODIFYDATES
	find /tmp/testpseudodirty -type f -exec dctable -k StudyDate '{}' ';'
	find /tmp/testpseudoclean -type f -exec dctable -k StudyDate '{}' ';'

testpseudooverlays:	PseudonymizeAndSequester.class
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.apps.PseudonymizeAndSequester \
		$${HOME}/Pictures/Medical/Overlays \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudoencappdf:	PseudonymizeAndSequester.class
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.apps.PseudonymizeAndSequester \
		$${HOME}/Pictures/Medical/EncapsulatedPDF \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudosrwithnameintext:	PseudonymizeAndSequester.class
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		${HOME}/Pictures/Medical/SRForDeidentification/IM000001_PhilipsUSPrivateContentItemNameInTextValueType.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		"" \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt \
		85312365145242465 \
		DONOTKEEPDESCRIPTORS \
		MODIFYDATES \
		MODIFYSTRUCTUREDCONTENT \
		DONOTADDCONTRIBUTINGEQUIPMENT
	find /tmp/testpseudodirty -name '*.dcm' -exec dcdiff ${HOME}/Pictures/Medical/SRForDeidentification/IM000001_PhilipsUSPrivateContentItemNameInTextValueType.dcm '{}' ';'
	find /tmp/testpseudodirty -name '*.dcm' -exec dcsrdiff ${HOME}/Pictures/Medical/SRForDeidentification/IM000001_PhilipsUSPrivateContentItemNameInTextValueType.dcm '{}' ';'
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean

testpseudorandomcontrol:	PseudonymizeAndSequester.class
	dcsmpte /tmp/testpseudoinput.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2"\
		-r StudyDescription "I am a happy study" \
		-r SeriesDescription "I am a nice series" \
		-r ProtocolName "Good protocol" \
		-r PatientWeight "32" \
		-r DeviceSerialNumber "12345" \
		-r InstitutionName "My Hospital" \
		-r StudyDate "20180921"
	echo  >/tmp/testpseudocontrol.txt "originalPatientID	newPatientID	newPatientName"
	echo >>/tmp/testpseudocontrol.txt "1234	REPLID	Name^Replaced"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		/tmp/testpseudocontrol.txt \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt
	find /tmp/testpseudoclean -name '*.dcm' -exec dcdiff /tmp/testpseudoinput.dcm '{}' ';'
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean
	@echo "Checking what was done is what was asked - i.e., no difference between /tmp/testpseudocontrol.txt and /tmp/testpseudoresultpatientid.txt"
	diff /tmp/testpseudocontrol.txt /tmp/testpseudoresultpatientid.txt

testpseudorandomcontroluid:	PseudonymizeAndSequester.class
	dcsmpte /tmp/testpseudoinput.dcm -nodisclaimer -r PatientID 1234 -r PatientName "Name^1234" -r InstanceNumber "45" -r Modality CT -r SOPClassUID "1.2.840.10008.5.1.4.1.1.2"\
		-r StudyDescription "I am a happy study" \
		-r SeriesDescription "I am a nice series" \
		-r ProtocolName "Good protocol" \
		-r PatientWeight "32" \
		-r DeviceSerialNumber "12345" \
		-r InstitutionName "My Hospital" \
		-r StudyDate "20180921" \
		-r StudyInstanceUID "1.2.3.4.5.6.7.8"
	echo  >/tmp/testpseudocontrol.txt "originalStudyInstanceUID	newPatientID	newPatientName"
	echo >>/tmp/testpseudocontrol.txt "1.2.3.4.5.6.7.8	REPLID	Name^Replaced"
	mkdir -p /tmp/testpseudoclean
	rm -rf /tmp/testpseudoclean/*
	mkdir -p /tmp/testpseudodirty
	rm -rf /tmp/testpseudodirty/*
	java -cp ${PATHTOROOT} com.pixelmed.apps.PseudonymizeAndSequester \
		/tmp/testpseudoinput.dcm \
		/tmp/testpseudoclean \
		/tmp/testpseudodirty \
		/tmp/testpseudocontrol.txt \
		/tmp/testpseudoresultpatientid.txt \
		/tmp/testpseudoresultstudyuid.txt \
		/tmp/testpseudofailed.txt \
		/tmp/testpseudouids.txt
	find /tmp/testpseudoclean -name '*.dcm' -exec dcdiff /tmp/testpseudoinput.dcm '{}' ';'
	find /tmp/testpseudodirty
	find /tmp/testpseudoclean
	@echo "Checking what was done is what was asked - i.e., no difference between /tmp/testpseudocontrol.txt and /tmp/testpseudoresultstudyuid.txt"
	diff /tmp/testpseudocontrol.txt /tmp/testpseudoresultstudyuid.txt

testconvertsrtsct:	ConvertSRTCodesToSCT.class
	@rm -rf /tmp/testconvertsrtsct
	@mkdir -p /tmp/testconvertsrtsct
	java -Djava.awt.headless=true -cp ${PATHTOROOT} \
		com.pixelmed.apps.ConvertSRTCodesToSCT \
		"$${HOME}/Documents/Work/NCI/Slicer/QIICR/IowaCompleteCaseDownloadForJSONSR/QIN-HEADNECK/QIN-HEADNECK-01-0024/08-10-1986-Thorax1HEADNECKPETCT-15969/75-tumor measurements - User1 SemiAuto trial 1-62668/000000.dcm" \
		/tmp/testconvertsrtsct
	@rm -rf /tmp/filetouse.dat
	@find /tmp/testconvertsrtsct -name '*.dcm' >/tmp/filetouse.dat
	# expect ...
	dcdiff \
		"$${HOME}/Documents/Work/NCI/Slicer/QIICR/IowaCompleteCaseDownloadForJSONSR/QIN-HEADNECK/QIN-HEADNECK-01-0024/08-10-1986-Thorax1HEADNECKPETCT-15969/75-tumor measurements - User1 SemiAuto trial 1-62668/000000.dcm" \
		"`cat /tmp/filetouse.dat`"
	# expect ...
	dcsrdiff \
		"$${HOME}/Documents/Work/NCI/Slicer/QIICR/IowaCompleteCaseDownloadForJSONSR/QIN-HEADNECK/QIN-HEADNECK-01-0024/08-10-1986-Thorax1HEADNECKPETCT-15969/75-tumor measurements - User1 SemiAuto trial 1-62668/000000.dcm" \
		"`cat /tmp/filetouse.dat`"
	@rm /tmp/filetouse.dat

testcomparesets:	CompareSetsOfDicomFiles.class
	@rm -rf /tmp/crap1.dcm
	dcsmpte /tmp/crap1.dcm
	@rm -rf /tmp/crap2.dcm
	dcsmpte /tmp/crap2.dcm
	java -Djava.awt.headless=true -cp ${PATHTOROOT} \
		com.pixelmed.apps.CompareSetsOfDicomFiles \
		/tmp/crap1.dcm \
		/tmp/crap2.dcm
	@rm /tmp/crap1.dcm
	@rm /tmp/crap2.dcm
