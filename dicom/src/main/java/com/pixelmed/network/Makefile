PATHTOROOT = ../../..

OBJS = \
AAbortPDU.class \
AnyExplicitTransferSyntaxSelectionPolicy.class \
AnyExplicitStorePresentationContextSelectionPolicy.class \
AnyExplicitStoreFindMoveGetPresentationContextSelectionPolicy.class \
ApplicationEntity.class \
ApplicationEntityConfigurationDialog.class \
ApplicationEntityMap.class \
AReleaseException.class \
AReleasePDU.class \
AssociateRejectPDU.class \
AssociateAcceptPDU.class \
AssociateRequestPDU.class \
AssociateRequestAcceptPDU.class \
Association.class \
AssociationAcceptor.class \
AssociationInitiator.class \
AssociationFactory.class \
AssociationOutputStream.class \
AssociationStatusHandler.class \
CEchoRequestCommandMessage.class \
CEchoResponseCommandMessage.class \
CFindRequestCommandMessage.class \
CFindResponseCommandMessage.class \
CGetRequestCommandMessage.class \
CGetResponseCommandMessage.class \
CMoveRequestCommandMessage.class \
CMoveResponseCommandMessage.class \
CommandMessage.class \
CompositeInstanceStoreAbstractSyntaxSelectionPolicy.class \
CompositeInstanceStoreFindMoveGetAbstractSyntaxSelectionPolicy.class \
CompositeResponseHandler.class \
CStoreRequestCommandMessage.class \
CStoreResponseCommandMessage.class \
DicomNetworkException.class \
FindSOPClassSCU.class \
GetSOPClassSCU.class \
IdentifierHandler.class \
IdentifierMessage.class \
MessageServiceElementCommand.class \
MoveSOPClassSCU.class \
MultipleInstanceTransferStatusHandler.class \
MultipleInstanceTransferStatusHandlerWithFileName.class \
NetworkApplicationConfigurationDialog.class \
NetworkApplicationInformation.class \
NetworkApplicationInformationFederated.class \
NetworkApplicationProperties.class \
NetworkConfigurationFromLDAP.class \
NetworkConfigurationFromMulticastDNS.class \
NetworkConfigurationSource.class \
NetworkDefaultValues.class \
NetworkMediaImporter.class \
NetworkUtilities.class \
PDataPDU.class \
PresentationAddress.class \
PresentationContext.class \
PresentationContextListFactory.class \
PresentationDataValue.class \
ProbeCapability.class \
ReceivedDataHandler.class \
ReceivedFilePathStrategy.class \
ReceivedObjectHandler.class \
RequestCommandMessage.class \
ResponseStatus.class \
SCUSCPRoleSelection.class \
StorageSOPClassSCP.class \
StorageSOPClassSCPDispatcher.class \
StorageSOPClassSCU.class \
StorageSOPClassSCUPerformanceTest.class \
TestSendingCommandAndDataInOnePDU.class \
TransferSyntaxSelectionPolicy.class \
UnencapsulatedExplicitTransferSyntaxSelectionPolicy.class \
UnencapsulatedExplicitStorePresentationContextSelectionPolicy.class \
UnencapsulatedExplicitStoreFindMoveGetPresentationContextSelectionPolicy.class \
VerificationSOPClassSCU.class

# NOT NetworkApplicationConfigurationDialog_ja.properties
TRANSFORMEDPROPERTIES = \
	ApplicationEntityConfigurationDialog_cs.properties \
	ApplicationEntityConfigurationDialog_da.properties \
	ApplicationEntityConfigurationDialog_de.properties \
	ApplicationEntityConfigurationDialog_el.properties \
	ApplicationEntityConfigurationDialog_es.properties \
	ApplicationEntityConfigurationDialog_fi.properties \
	ApplicationEntityConfigurationDialog_fr.properties \
	ApplicationEntityConfigurationDialog_hu.properties \
	ApplicationEntityConfigurationDialog_it.properties \
	ApplicationEntityConfigurationDialog_ja.properties \
	ApplicationEntityConfigurationDialog_ko.properties \
	ApplicationEntityConfigurationDialog_nl.properties \
	ApplicationEntityConfigurationDialog_no.properties \
	ApplicationEntityConfigurationDialog_pl.properties \
	ApplicationEntityConfigurationDialog_pt.properties \
	ApplicationEntityConfigurationDialog_ro.properties \
	ApplicationEntityConfigurationDialog_ru.properties \
	ApplicationEntityConfigurationDialog_sv.properties \
	ApplicationEntityConfigurationDialog_tr.properties \
	ApplicationEntityConfigurationDialog_zh_CN.properties \
	NetworkApplicationConfigurationDialog_cs.properties \
	NetworkApplicationConfigurationDialog_da.properties \
	NetworkApplicationConfigurationDialog_de.properties \
	NetworkApplicationConfigurationDialog_el.properties \
	NetworkApplicationConfigurationDialog_es.properties \
	NetworkApplicationConfigurationDialog_fi.properties \
	NetworkApplicationConfigurationDialog_fr.properties \
	NetworkApplicationConfigurationDialog_hu.properties \
	NetworkApplicationConfigurationDialog_it.properties \
	NetworkApplicationConfigurationDialog_ja.properties \
	NetworkApplicationConfigurationDialog_ko.properties \
	NetworkApplicationConfigurationDialog_nl.properties \
	NetworkApplicationConfigurationDialog_no.properties \
	NetworkApplicationConfigurationDialog_pl.properties \
	NetworkApplicationConfigurationDialog_pt.properties \
	NetworkApplicationConfigurationDialog_ro.properties \
	NetworkApplicationConfigurationDialog_ru.properties \
	NetworkApplicationConfigurationDialog_sv.properties \
	NetworkApplicationConfigurationDialog_tr.properties \
	NetworkApplicationConfigurationDialog_zh_CN.properties

all:	${OBJS} ${TRANSFORMEDPROPERTIES}

include ${PATHTOROOT}/Makefile.common.mk

clean:
	rm -f *~ *.class core *.bak \
		${TRANSFORMEDPROPERTIES}

archive:
	tar -cvf - Makefile *.java | gzip -best > ../../../network.`date '+%Y%m%d'`.tar.gz

testprobe:	ProbeCapability.class
	java -cp ${PATHTOROOT} com.pixelmed.network.ProbeCapability localhost 4006 HELGRAY US
	java -cp ${PATHTOROOT} com.pixelmed.network.ProbeCapability localhost HELGRAY US
	java -cp ${PATHTOROOT} com.pixelmed.network.ProbeCapability localhost US
	java -cp ${PATHTOROOT} com.pixelmed.network.ProbeCapability localhost

testconfig:	NetworkConfigurationFromLDAP.class ApplicationEntity.class
	java -cp ${PATHTOROOT} com.pixelmed.network.NetworkConfigurationFromLDAP

testproperties:	NetworkApplicationProperties.class
	java -cp ${PATHTOROOT} com.pixelmed.network.NetworkApplicationProperties ../server/testserver.properties "o=pixelmed,c=us"

testconfigmdns:	NetworkConfigurationFromMulticastDNS.class ApplicationEntity.class
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jmdns.jar com.pixelmed.network.NetworkConfigurationFromMulticastDNS HELGRAY 4006 WSD

testscuperf: StorageSOPClassSCUPerformanceTest.class
	dcsmpte -rows 512 -columns 512 -bits 12 /tmp/testscuperf
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jmdns.jar com.pixelmed.network.StorageSOPClassSCUPerformanceTest localhost 4007 LOCAL4007 STORESCU 0 0 0 100 10 0 0 /tmp/testscuperf
	rm /tmp/testscuperf

testscuperfactual: StorageSOPClassSCUPerformanceTest.class
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jmdns.jar com.pixelmed.network.StorageSOPClassSCUPerformanceTest localhost 4007 LOCAL4007 STORESCU 0 0 0 1 1 0 0 /largetmp/112648/Blinded

testscuperfactualtopixelmed: StorageSOPClassSCUPerformanceTest.class
	rm -f /tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr
	rm -rf /largetmp/crap
	mkdir -p /largetmp/crap
	./runprocessinbackground.sh \
		/tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr \
		java -cp ${PATHTOROOT} \
			com.pixelmed.network.StorageSOPClassSCPDispatcher 4007 LOCAL4007 /largetmp/crap
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jmdns.jar com.pixelmed.network.StorageSOPClassSCUPerformanceTest localhost 4007 LOCAL4007 STORESCU 0 0 0 1 1 0 0 /largetmp/112648/Blinded
	@kill -9 `cat /tmp/testpixelmedsendtopixelmed.pid`
	#rm /tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr

testscuperfdiscardtopixelmed: StorageSOPClassSCUPerformanceTest.class
	rm -f /tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr
	./runprocessinbackground.sh \
		/tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr \
		java -cp ${PATHTOROOT} \
			com.pixelmed.network.StorageSOPClassSCPDispatcher 4007 LOCAL4007 -
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jmdns.jar com.pixelmed.network.StorageSOPClassSCUPerformanceTest localhost 4007 LOCAL4007 STORESCU 0 0 0 1 1 0 0 /largetmp/112648/Blinded
	@kill -9 `cat /tmp/testpixelmedsendtopixelmed.pid`
	#rm /tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr

testimporter:	NetworkMediaImporter.class
	mkdir -p /tmp/testimport
	dcsmpte /tmp/testimport/crap
	java -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkMediaImporter \
		localhost 4006 HELGRAY IMPORTER /tmp/testimport
	rm -rf /tmp/testimport

testaedialog:	ApplicationEntityConfigurationDialog.class
	java -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.ApplicationEntityConfigurationDialog

testconfigurator:	NetworkApplicationConfigurationDialog.class
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorczech:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_cs.properties NetworkApplicationConfigurationDialog_cs.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=cs -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratordannish:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_da.properties NetworkApplicationConfigurationDialog_da.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=da -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorgerman:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_de.properties NetworkApplicationConfigurationDialog_de.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=de -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorgreek:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_el.properties NetworkApplicationConfigurationDialog_el.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=el -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorspanish:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_es.properties NetworkApplicationConfigurationDialog_es.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=es -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorfinnish:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_fi.properties NetworkApplicationConfigurationDialog_fi.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=fi -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorfrench:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_fr.properties NetworkApplicationConfigurationDialog_fr.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=fr -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorhungarian:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_hu.properties NetworkApplicationConfigurationDialog_hu.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=hu -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratoritalian:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_it.properties NetworkApplicationConfigurationDialog_it.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=it -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorjapanese:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_ja.properties NetworkApplicationConfigurationDialog_ja.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=ja -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorkorean:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_ko.properties NetworkApplicationConfigurationDialog_ko.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=ko -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratordutch:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_nl.properties NetworkApplicationConfigurationDialog_nl.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=nl -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratornorwegian:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_no.properties NetworkApplicationConfigurationDialog_no.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=no -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorpolish:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_pl.properties NetworkApplicationConfigurationDialog_pl.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=pl -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorportuguese:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_pt.properties NetworkApplicationConfigurationDialog_pt.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=pt -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorromanian:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_ro.properties NetworkApplicationConfigurationDialog_ro.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=ro -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorrussian:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_ru.properties NetworkApplicationConfigurationDialog_ru.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=ru -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorswedish:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_sv.properties NetworkApplicationConfigurationDialog_sv.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=sv -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorturkish:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_tr.properties NetworkApplicationConfigurationDialog_tr.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=tr -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratorchinesesimplified:	NetworkApplicationConfigurationDialog.class ApplicationEntityConfigurationDialog_zh_CN.properties NetworkApplicationConfigurationDialog_zh_CN.properties
	cp ${PATHTOROOT}/sample.com.pixelmed.display.DicomImageViewer.properties /tmp/testproperties
	java -Duser.language=zh -Duser.country=CN -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR}:${BZIP2ADDITIONALJAR} \
		com.pixelmed.network.NetworkApplicationConfigurationDialog /tmp/testproperties
	rm -f /tmp/testproperties

testconfiguratoralllocalizations: \
	testconfiguratorczech \
	testconfiguratordannish \
	testconfiguratorgerman \
	testconfiguratorgreek \
	testconfiguratorspanish \
	testconfiguratorfinnish \
	testconfiguratorfrench \
	testconfiguratorhungarian \
	testconfiguratoritalian \
	testconfiguratorjapanese \
	testconfiguratorkorean \
	testconfiguratordutch \
	testconfiguratornorwegian \
	testconfiguratorpolish \
	testconfiguratorportuguese \
	testconfiguratorromanian \
	testconfiguratorrussian \
	testconfiguratorswedish \
	testconfiguratorturkish \
	testconfiguratorchinesesimplified

testpixelmedsendtopixelmed:
	@rm -rf /tmp/testpixelmedsendtopixelmed.receivedfiles
	@mkdir -p /tmp/testpixelmedsendtopixelmed.receivedfiles
	./runprocessinbackground.sh \
		/tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr \
		java -cp ${PATHTOROOT} \
			com.pixelmed.network.StorageSOPClassSCPDispatcher 4006 STORESCP /tmp/testpixelmedsendtopixelmed.receivedfiles NONSECURE
	@#echo "PID of com.pixelmed.network.StorageSOPClassSCPDispatcher is "`cat /tmp/testpixelmedsendtopixelmed.pid`
	dcsmpte -nodisclaimer -r PatientName testpixelmedsendtopixelmed.1 /tmp/testpixelmedsendtopixelmed.1.dcm
	java -cp ${PATHTOROOT} com.pixelmed.network.StorageSOPClassSCU localhost 4006 STORESCP US /tmp/testpixelmedsendtopixelmed.1.dcm 0
	@echo "Comparing sent and received files - expect no difference except meta header"
	@receivedfilename=`grep 'sendReceivedObjectIndication() fileName' /tmp/testpixelmedsendtopixelmed.stderr | tail -1 | sed -e 's/^.*fileName: //' -e 's/ from .*$$//'`; \
		andiff /tmp/testpixelmedsendtopixelmed.1.dcm "$${receivedfilename}"; \
		dccmp /tmp/testpixelmedsendtopixelmed.1.dcm "$${receivedfilename}"
	@echo "Comparison done"
	@kill -9 `cat /tmp/testpixelmedsendtopixelmed.pid`
	@rm -rf /tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr \
		/tmp/testpixelmedsendtopixelmed.receivedfiles \
		/tmp/testpixelmedsendtopixelmed.1.dcm

testpixelmedsendmultitopixelmed:
	@rm -rf /tmp/testpixelmedsendtopixelmed.receivedfiles
	@mkdir -p /tmp/testpixelmedsendtopixelmed.receivedfiles
	./runprocessinbackground.sh \
		/tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr \
		java -cp ${PATHTOROOT} \
			com.pixelmed.network.StorageSOPClassSCPDispatcher 4006 STORESCP /tmp/testpixelmedsendtopixelmed.receivedfiles NONSECURE
	@#echo "PID of com.pixelmed.network.StorageSOPClassSCPDispatcher is "`cat /tmp/testpixelmedsendtopixelmed.pid`
	dcsmpte -nodisclaimer -r PatientName testpixelmedsendtopixelmed.1 /tmp/testpixelmedsendtopixelmed.1.dcm
	dcsmpte -nodisclaimer -r PatientName testpixelmedsendtopixelmed.2 /tmp/testpixelmedsendtopixelmed.2.dcm
	@echo "/tmp/testpixelmedsendtopixelmed.1.dcm" >/tmp/testpixelmedsendtopixelmed.filelist
	@echo "/tmp/testpixelmedsendtopixelmed.2.dcm" >>/tmp/testpixelmedsendtopixelmed.filelist
	java -cp ${PATHTOROOT} com.pixelmed.network.StorageSOPClassSCU localhost 4006 STORESCP US - 0 </tmp/testpixelmedsendtopixelmed.filelist
	#ls -l /tmp/testpixelmedsendtopixelmed.receivedfiles
	#cat /tmp/testpixelmedsendtopixelmed.stderr
	@echo "Comparing sent and received 1st files - expect no difference except meta header"
	@receivedfilename=`grep 'sendReceivedObjectIndication() fileName' /tmp/testpixelmedsendtopixelmed.stderr | tail -1 | sed -e 's/^.*fileName: //' -e 's/ from .*$$//'`; \
		andiff /tmp/testpixelmedsendtopixelmed.1.dcm "$${receivedfilename}"; \
		dccmp /tmp/testpixelmedsendtopixelmed.1.dcm "$${receivedfilename}"
	@echo "Comparison done"
	@echo "Comparing sent and received 2nd files - expect no difference except meta header"
	@receivedfilename=`grep 'sendReceivedObjectIndication() fileName' /tmp/testpixelmedsendtopixelmed.stderr | head -1 | sed -e 's/^.*fileName: //' -e 's/ from .*$$//'`; \
		andiff /tmp/testpixelmedsendtopixelmed.2.dcm "$${receivedfilename}"; \
		dccmp /tmp/testpixelmedsendtopixelmed.2.dcm "$${receivedfilename}"
	@echo "Comparison done"
	@kill -9 `cat /tmp/testpixelmedsendtopixelmed.pid`
	@rm -rf /tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr \
		/tmp/testpixelmedsendtopixelmed.receivedfiles \
		/tmp/testpixelmedsendtopixelmed.1.dcm \
		/tmp/testpixelmedsendtopixelmed.2.dcm \
		/tmp/testpixelmedsendtopixelmed.filelist

testpixelmedsendmanytopixelmed:
	@rm -rf /tmp/testpixelmedsendtopixelmed.receivedfiles
	@mkdir -p /tmp/testpixelmedsendtopixelmed.receivedfiles
	./runprocessinbackground.sh \
		/tmp/testpixelmedsendtopixelmed.pid /tmp/testpixelmedsendtopixelmed.stdout /tmp/testpixelmedsendtopixelmed.stderr \
		java -cp ${PATHTOROOT} \
			-Dorg.slf4j.simpleLogger.defaultLogLevel=debug \
			com.pixelmed.network.StorageSOPClassSCPDispatcher 4006 STORESCP /tmp/testpixelmedsendtopixelmed.receivedfiles NONSECURE
	@#echo "PID of com.pixelmed.network.StorageSOPClassSCPDispatcher is "`cat /tmp/testpixelmedsendtopixelmed.pid`
	@rm -f /tmp/testpixelmedsendtopixelmed.filelist
	count=0; while [ $${count} -lt 5 ] ; do dcsmpte -nodisclaimer -r PatientName "testpixelmedsendtopixelmed.$${count}" "/tmp/testpixelmedsendtopixelmed.$${count}.dcm" ; echo "/tmp/testpixelmedsendtopixelmed.$${count}.dcm" >>/tmp/testpixelmedsendtopixelmed.filelist ; count=`expr $${count} + 1` ; done
	#ls -l "/tmp/testpixelmedsendtopixelmed.*.dcm"
	java -cp ${PATHTOROOT} com.pixelmed.network.StorageSOPClassSCU localhost 4006 STORESCP US - 0 </tmp/testpixelmedsendtopixelmed.filelist
	@sent=`wc -l </tmp/testpixelmedsendtopixelmed.filelist | sed -e 's/[ ]*//'` ; received=`ls -1 /tmp/testpixelmedsendtopixelmed.receivedfiles | wc -l | sed -e 's/[ ]*//'` ; echo "Sent $${sent}, received $${received} files"
	@kill -9 `cat /tmp/testpixelmedsendtopixelmed.pid`

testpixelmedcgetscutopixelmedcgetscp:
	@rm -rf /tmp/testpixelmedcgetscutopixelmedcgetscp.files
	@mkdir -p /tmp/testpixelmedcgetscutopixelmedcgetscp.files
	@rm -rf /tmp/testpixelmedcgetscutopixelmedcgetscp.db.*
	@rm -f testpixelmedcgetscutopixelmedcgetscp.properties
	@echo >> /tmp/testpixelmedcgetscutopixelmedcgetscp.properties "Application.DatabaseFileName=/tmp/testpixelmedcgetscutopixelmedcgetscp.db"
	@echo >> /tmp/testpixelmedcgetscutopixelmedcgetscp.properties "Application.SavedImagesFolderName=/tmp/testpixelmedcgetscutopixelmedcgetscp.files"
	@echo >> /tmp/testpixelmedcgetscutopixelmedcgetscp.properties "Dicom.ListeningPort=4006"
	@echo >> /tmp/testpixelmedcgetscutopixelmedcgetscp.properties "Dicom.CalledAETitle=TESTGETSCP"
	@echo >> /tmp/testpixelmedcgetscutopixelmedcgetscp.properties "Dicom.CallingAETitle=TESTGETSCP"
	./runprocessinbackground.sh \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.pid /tmp/testpixelmedcgetscutopixelmedcgetscp.stdout /tmp/testpixelmedcgetscutopixelmedcgetscp.stderr \
		java -cp ${PATHTOROOT}:${SERVERADDITIONALJARS} \
			-Dorg.slf4j.simpleLogger.defaultLogLevel=debug \
			com.pixelmed.server.DicomAndWebStorageServer /tmp/testpixelmedcgetscutopixelmedcgetscp.properties
	sleep 15
	@#echo "PID of com.pixelmed.server.DicomAndWebStorageServer is "`cat /tmp/testpixelmedcgetscutopixelmedcgetscp.pid`
	dcsmpte -nodisclaimer -r PatientName testpixelmedcgetscutopixelmedcgetscp /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm
	java -cp ${PATHTOROOT} com.pixelmed.network.StorageSOPClassSCU localhost 4006 TESTGETSCP US /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 0
	@rm -rf /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files
	@mkdir -p /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files
	java -cp ${PATHTOROOT} \
		-Dorg.slf4j.simpleLogger.defaultLogLevel=debug \
		com.pixelmed.network.GetSOPClassSCU localhost 4006 TESTGETSCP US \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.get.files \
		STUDY \
		`dckey -k StudyInstanceUID /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 2>&1` \
		2> /tmp/testpixelmedcgetscutopixelmedcgetscp.get.stderr
	@echo "Comparing sent and received files with STUDY C-GET - expect no difference except meta header"
	@andiff /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@dccmp  /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@rm -f /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@echo "Comparison done"
	java -cp ${PATHTOROOT} \
		-Dorg.slf4j.simpleLogger.defaultLogLevel=debug \
		com.pixelmed.network.GetSOPClassSCU localhost 4006 TESTGETSCP US \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.get.files \
		SERIES \
		`dckey -k StudyInstanceUID /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 2>&1` \
		`dckey -k SeriesInstanceUID /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 2>&1` \
		2> /tmp/testpixelmedcgetscutopixelmedcgetscp.get.stderr
	@echo "Comparing sent and received files with SERIES C-GET - expect no difference except meta header"
	@andiff /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@dccmp  /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@rm -f /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@echo "Comparison done"
	java -cp ${PATHTOROOT} \
		-Dorg.slf4j.simpleLogger.defaultLogLevel=debug \
		com.pixelmed.network.GetSOPClassSCU localhost 4006 TESTGETSCP US \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.get.files \
		IMAGE \
		`dckey -k StudyInstanceUID /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 2>&1` \
		`dckey -k SeriesInstanceUID /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 2>&1` \
		`dckey -k SOPInstanceUID /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm 2>&1` \
		2> /tmp/testpixelmedcgetscutopixelmedcgetscp.get.stderr
	@echo "Comparing sent and received files with IMAGE C-GET - expect no difference except meta header"
	@andiff /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@dccmp  /tmp/testpixelmedcgetscutopixelmedcgetscp.dcm /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@rm -f /tmp/testpixelmedcgetscutopixelmedcgetscp.get.files/*
	@echo "Comparison done"
	@kill -9 `cat /tmp/testpixelmedcgetscutopixelmedcgetscp.pid`
	@rm -rf /tmp/testpixelmedcgetscutopixelmedcgetscp.pid /tmp/testpixelmedcgetscutopixelmedcgetscp.stdout /tmp/testpixelmedcgetscutopixelmedcgetscp.stderr \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.files \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.db.* \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.properties \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.dcm \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.get.files \
		/tmp/testpixelmedcgetscutopixelmedcgetscp.get.stderr


