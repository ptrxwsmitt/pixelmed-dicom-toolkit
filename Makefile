all:	pixelmed.jar

PIXELMEDCODESIGNERKEYSTORE = "/Volumes/Access/Access/pixelmed_comodo_objectsigner.p12"
PIXELMEDCODESIGNERALIAS = "PixelMed Publishing's Sectigo Limited ID"
PIXELMEDCODESIGNEROPTIONS = -storetype pkcs12 -tsa http://timestamp.comodoca.com/rfc3161 -digestalg SHA-256

# use hard-dereference with gnutar in case we store the same file twice (though shouldn't do that), since hardlinks for duplicates causes bsdtar (e.g., on Mac) to freak out (000918)
TAR = gnutar --hard-dereference
#TAR = tar
COMPRESS = bzip2
COMPRESSEXT = bz2

SUBDIRS = \
	com/pixelmed/utils \
	com/pixelmed/dicom \
	com/pixelmed/geometry \
	com/pixelmed/validate \
	com/pixelmed/display \
	com/pixelmed/event \
	com/pixelmed/network \
	com/pixelmed/database \
	com/pixelmed/query \
	com/pixelmed/scpecg \
	com/pixelmed/displaywave \
	com/pixelmed/web \
	com/pixelmed/server \
	com/pixelmed/transfermonitor \
	com/pixelmed/convert \
	com/pixelmed/apps \
	com/pixelmed/anatproc \
	com/pixelmed/dose \
	com/pixelmed/doseocr \
	com/pixelmed/test \
	com/pixelmed/ftp \
	com/pixelmed/slf4j \
	apple/dts/samplecode/osxadapter
	

SUBPACKAGES = \
	com.pixelmed.dicom \
	com.pixelmed.geometry \
	com.pixelmed.validate \
	com.pixelmed.display \
	com.pixelmed.display.event \
	com.pixelmed.event \
	com.pixelmed.network \
	com.pixelmed.utils \
	com.pixelmed.database \
	com.pixelmed.query \
	com.pixelmed.scpecg \
	com.pixelmed.displaywave \
	com.pixelmed.web \
	com.pixelmed.server \
	com.pixelmed.transfermonitor \
	com.pixelmed.convert \
	com.pixelmed.apps \
	com.pixelmed.anatproc \
	com.pixelmed.dose \
	com.pixelmed.doseocr \
	com.pixelmed.test \
	com.pixelmed.ftp \
	com.pixelmed.slf4j \
	apple.dts.samplecode.osxadapter

ADDITIONALFILES = \
	COPYRIGHT \
	DeidentifyAndRedact.bat \
	DeidentifyAndRedact.sh \
	DeidentifyAndRedactWithOriginalFileName.bat \
	DicomAttributeBrowser.sh \
	DicomBrowser.sh \
	DicomCleanerAssumingJREInstalled.bat \
	DicomCleanerWithOwnJRE.bat \
	DicomImageViewer.bat \
	DicomImageViewer.sh \
	DicomImageViewerWithCDJRE.bat \
	DicomImageViewerWithOwnJRE.bat \
	DicomInstanceValidator.sh \
	DicomSRValidator.sh \
	DoseUtilityAssumingJREInstalled.bat \
	DoseUtilityWithOwnJRE.bat \
	ECGViewer.bat \
	ECGViewer.sh \
	MoveDicomFilesIntoHierarchy.sh \
	NetworkMediaImporter.bat \
	NetworkMediaImporter.sh \
	StructuredReportBrowser.sh \
	StudyReceiver.bat \
	studyreceiver.properties \
	Makefile \
	Makefile.common.mk \
	README \
	sample.com.pixelmed.display.DicomImageViewer.properties

LOCALIZATIONFILES = \
	com/pixelmed/display/*.properties \
	com/pixelmed/display/*.utf-8_properties \
	com/pixelmed/network/*.properties \
	com/pixelmed/network/*.utf-8_properties

WEBSTARTIMAGEFILES = \
	webstart/images/DicomCleanerControlPanel.png \
	webstart/images/DicomCleanerCopyShortcut.png \
	webstart/images/DicomCleanerLocalConfiguration.png \
	webstart/images/DicomCleanerMainPanelAfterClean.png \
	webstart/images/DicomCleanerPasteShortcut.png \
	webstart/images/DicomCleanerRemoteConfiguration.png \
	webstart/images/DicomCleanerShowVersion.png \
	webstart/images/DicomImageBlackoutColorUltrasound.png \
	webstart/images/DicomImageBlackoutOverlayDose.png \
	webstart/images/DoseUtilityCopyShortcut.png \
	webstart/images/DoseUtilityDoseScreenImage.png \
	webstart/images/DoseUtilityDoseSRTree.png \
	webstart/images/DoseUtilityDoseSRValidation.png \
	webstart/images/DoseUtilityLocalConfiguration.png \
	webstart/images/DoseUtilityLocalFTPConfiguration.png \
	webstart/images/DoseUtilityLogOfReport.png \
	webstart/images/DoseUtilityMainPanel.png \
	webstart/images/DoseUtilityPasteShortcut.png \
	webstart/images/DoseUtilityRemoteConfiguration.png \
	webstart/images/DoseUtilityRemoteFTPConfiguration.png \
	webstart/images/PixelMedLogoAndTitle.gif \
	webstart/images/osi_standard_logo.png

WEBSTARTHTMLFILES = \
	webstart/ConvertAmicasJPEG2000FilesetToDicom.html \
	webstart/DicomImageViewer.html \
	webstart/DicomImageViewerUsage.html \
	webstart/DicomImageBlackout.html \
	webstart/DicomCleaner.html \
	webstart/DicomCleanerUsage.html \
	webstart/DoseUtility.html \
	webstart/DoseUtilityUsage.html \
	webstart/MediaImporter.html \
	webstart/WatchFolderAndSend.html

WEBSTARTJNLPFILES = \
	webstart/ConvertAmicasJPEG2000FilesetToDicom.jnlp \
	webstart/DicomCleaner.jnlp \
	webstart/DicomImageBlackout.jnlp \
	webstart/DicomImageViewer.jnlp \
	webstart/DoseUtility.jnlp \
	webstart/MediaImporter.jnlp \
	webstart/WatchFolderAndSend.jnlp

ADDITIONALSOURCEFILES = \
	Doxyfile \
	icons/logo_1024.png \
	icons/ConvertAmicasJPEG2000FilesetToDicom.icns \
	icons/ConvertAmicasJPEG2000FilesetToDicom.png \
	icons/ConvertAmicasJPEG2000FilesetToDicom.ico \
	icons/DicomCleaner.icns \
	icons/DicomCleaner_1024.png \
	icons/DicomCleaner.png \
	icons/DicomCleaner.ico \
	icons/DicomImageBlackout.icns \
	icons/DicomImageBlackout.png \
	icons/DicomImageBlackout.ico \
	icons/DicomImageViewer.icns \
	icons/DicomImageViewer.png \
	icons/DicomImageViewer.ico \
	icons/DoseUtility.icns \
	icons/DoseUtility_1024.png \
	icons/DoseUtility.png \
	icons/DoseUtility.ico \
	icons/DownloadOrTransmit.icns \
	icons/DownloadOrTransmit.png \
	icons/DownloadOrTransmit.ico \
	icons/Makefile \
	icons/makeiconsetfrompng.sh \
	icons/MediaImporter.icns \
	icons/MediaImporter.png \
	icons/MediaImporter.ico \
	icons/WatchFolderAndSend.icns \
	icons/WatchFolderAndSend.png \
	icons/WatchFolderAndSend.ico \
	${WEBSTARTHTMLFILES} \
	${WEBSTARTJNLPFILES} \
	${WEBSTARTIMAGEFILES} \
	webstart/Manifest.txt

WEBSTARTJAIFILES = \
	webstart/jai-imageio/early-access/1.1/linux-i586/clibwrapper_jiio.jar \
	webstart/jai-imageio/early-access/1.1/linux-i586/jai_imageio.jar \
	webstart/jai-imageio/early-access/1.1/linux-i586/libclib_jiio.so.jar \
	webstart/jai-imageio/early-access/1.1/solaris-i586/clibwrapper_jiio.jar \
	webstart/jai-imageio/early-access/1.1/solaris-i586/jai_imageio.jar \
	webstart/jai-imageio/early-access/1.1/solaris-i586/libclib_jiio.so.jar \
	webstart/jai-imageio/early-access/1.1/solaris-sparc/clibwrapper_jiio.jar \
	webstart/jai-imageio/early-access/1.1/solaris-sparc/jai_imageio.jar \
	webstart/jai-imageio/early-access/1.1/solaris-sparc/libclib_jiio.so.jar \
	webstart/jai-imageio/early-access/1.1/solaris-sparc/libclib_jiio_vis.so.jar \
	webstart/jai-imageio/early-access/1.1/solaris-sparc/libclib_jiio_vis2.so.jar \
	webstart/jai-imageio/early-access/1.1/windows-i586/clib_jiio.dll.jar \
	webstart/jai-imageio/early-access/1.1/windows-i586/clib_jiio_sse2.dll.jar \
	webstart/jai-imageio/early-access/1.1/windows-i586/clib_jiio_util.dll.jar \
	webstart/jai-imageio/early-access/1.1/windows-i586/clibwrapper_jiio.jar \
	webstart/jai-imageio/early-access/1.1/windows-i586/jai_imageio.jar \

WEBSTARTFILES = \
	webstart/pixelmed.jar \
	webstart/pixelmed_imageio.jar \
	webstart/${COMMONSCODECJARFILENAME} \
	webstart/commons-net-ftp-2.0.jar \
	webstart/hsqldb.jar \
	webstart/jmdns.jar \
	webstart/${COMMONSCOMPRESSJARFILENAME} \
	webstart/vecmath1.2-1.14.jar \
	webstart/icons/ConvertAmicasJPEG2000FilesetToDicom.png \
	webstart/icons/DicomCleaner.png \
	webstart/icons/DicomImageBlackout.png \
	webstart/icons/DicomImageViewer.png \
	webstart/icons/DoseUtility.png \
	webstart/icons/DownloadOrTransmit.png \
	webstart/icons/MediaImporter.png \
	webstart/icons/WatchFolderAndSend.png \
	webstart/icons/ConvertAmicasJPEG2000FilesetToDicom.ico \
	webstart/icons/DicomCleaner.ico \
	webstart/icons/DicomImageBlackout.ico \
	webstart/icons/DicomImageViewer.ico \
	webstart/icons/DoseUtility.ico \
	webstart/icons/DownloadOrTransmit.ico \
	webstart/icons/MediaImporter.ico \
	webstart/icons/WatchFolderAndSend.ico \
	webstart/sample.com.pixelmed.display.DicomImageViewer.properties \
	webstart/sample.com.pixelmed.display.DicomCleaner.properties \
	${WEBSTARTHTMLFILES} \
	${WEBSTARTJNLPFILES} \
	${WEBSTARTIMAGEFILES} \
	${WEBSTARTJAIFILES}

BINARYRELEASEFILES = \
	pixelmed.jar \
	BUILDDATE \
	${ADDITIONALFILES}

SOURCERELEASEFILES = \
	${ADDITIONALFILES} \
	${ADDITIONALSOURCEFILES} \
	${SUBDIRS}

DEPENDENCYRELEASEFILESWITHOUTREADME = \
	lib/additional/pixelmed_codec.jar \
	lib/additional/pixelmed_imageio.jar \
	${COMMONSCODECADDITIONALJAR} \
	lib/additional/commons-net-ftp-2.0.jar \
	${COMMONSCOMPRESSADDITIONALJAR} \
	lib/additional/hsqldb.jar \
	lib/additional/vecmath1.2-1.14.jar \
	lib/additional/jmdns.jar \
	lib/additional/aiviewer.jar \
	lib/additional/javax.json-1.0.4.jar \
	lib/additional/javax.json-api-1.0.jar \
	lib/additional/slf4j-api-1.7.13.jar \
	lib/additional/slf4j-simple-1.7.13.jar \
	lib/additional/opencsv-2.4.jar \
	lib/additional/saxon-he-11.5.jar \
	lib/additional/xmlresolver-4.6.4.jar \
	lib/junit/junit-4.8.1.jar \
	LICENSES

DEPENDENCYRELEASEFILES = \
	${DEPENDENCYRELEASEFILESWITHOUTREADME} \
	README

DICOMCLEANERDEPENDENCYRELEASEFILES = \
	lib/additional/pixelmed_imageio.jar \
	${COMMONSCODECADDITIONALJAR} \
	${COMMONSCOMPRESSADDITIONALJAR} \
	lib/additional/hsqldb.jar \
	lib/additional/vecmath1.2-1.14.jar \
	lib/additional/jmdns.jar \
	LICENSES/hsqldb_lic.txt

DOSEUTILITYDEPENDENCYRELEASEFILES = \
	${COMMONSCODECADDITIONALJAR} \
	lib/additional/commons-net-ftp-2.0.jar \
	${COMMONSCOMPRESSADDITIONALJAR} \
	lib/additional/hsqldb.jar \
	lib/additional/vecmath1.2-1.14.jar \
	lib/additional/jmdns.jar \
	LICENSES/hsqldb_lic.txt

WINDOWSJIIORELEASEFILES = \
	windows/lib/clib_jiio.dll \
	windows/lib/clib_jiio_sse2.dll \
	windows/lib/clib_jiio_util.dll \
	windows/lib/clibwrapper_jiio.jar \
	windows/lib/jai_imageio.jar \
	LICENSES/LICENSE-jai_imageio.txt \
	LICENSES/THIRD-PARTY-LICENSE-README-jai_imageio.txt

JAVADOCRELEASEFILES = \
	${JAVADOCFILES}


DOXYGENRELEASEFILES = \
	${DOXYGENFILES}

JAVADOCFILES = \
	docs/javadoc

DOXYGENFILES = \
        docs/doxygen

OTHERDOCRELEASEFILES = \
	docs/DicomImageViewer/Conformance.pdf \
	docs/DicomImageViewer/ReleaseNotes.pdf \
	docs/DicomImageViewer/UserManual.pdf \
	docs/ECGViewer/ReleaseNotes.pdf

MACAPPS = \
	DicomImageViewer_app \
	DicomCleaner_app \
	DoseUtility_app \
	ECGViewer_app

MACAPPRELEASEFILES = \
	DicomImageViewer.app/Contents/Info.plist \
	DicomImageViewer.app/Contents/MacOS/JavaAppLauncher \
	DicomImageViewer.app/Contents/PkgInfo \
	DicomImageViewer.app/Contents/Resources/DicomImageViewer.icns \
	DicomImageViewer.app/Contents/Java/pixelmed.jar \
	DicomImageViewer.app/Contents/Java/pixelmed_imageio.jar \
	DicomImageViewer.app/Contents/Java/hsqldb.jar \
	DicomImageViewer.app/Contents/Java/${COMMONSCOMPRESSJARFILENAME} \
	DicomImageViewer.app/Contents/Java/vecmath1.2-1.14.jar \
	DicomImageViewer.app/Contents/Java/${COMMONSCODECJARFILENAME} \
	DicomImageViewer.app/Contents/Java/jai_imageio.jar \
	DicomImageViewer.app/Contents/Java/jmdns.jar \
	DicomCleaner.app/Contents/Info.plist \
	DicomCleaner.app/Contents/MacOS/JavaAppLauncher \
	DicomCleaner.app/Contents/PkgInfo \
	DicomCleaner.app/Contents/Resources/DicomCleaner.icns \
	DicomCleaner.app/Contents/Java/${COMMONSCODECJARFILENAME} \
	DicomCleaner.app/Contents/Java/${COMMONSCOMPRESSJARFILENAME} \
	DicomCleaner.app/Contents/Java/hsqldb.jar \
	DicomCleaner.app/Contents/Java/jmdns.jar \
	DicomCleaner.app/Contents/Java/pixelmed.jar \
	DicomCleaner.app/Contents/Java/pixelmed_imageio.jar \
	DoseUtility.app/Contents/Info.plist \
	DoseUtility.app/Contents/MacOS/JavaAppLauncher \
	DoseUtility.app/Contents/PkgInfo \
	DoseUtility.app/Contents/Resources/DoseUtility.icns \
	DoseUtility.app/Contents/Java/pixelmed.jar \
	DoseUtility.app/Contents/Java/pixelmed_imageio.jar \
	DoseUtility.app/Contents/Java/hsqldb.jar \
	DoseUtility.app/Contents/Java/${COMMONSCOMPRESSJARFILENAME} \
	DoseUtility.app/Contents/Java/${COMMONSCODECJARFILENAME} \
	DoseUtility.app/Contents/Java/commons-net-ftp-2.0.jar \
	DoseUtility.app/Contents/Java/jmdns.jar \
	DoseUtility.app/Contents/Java/jai_imageio.jar

MACAPPECGRELEASEFILES = \
	ECGViewer.app/Contents/Info.plist \
	ECGViewer.app/Contents/MacOS/JavaAppLauncher \
	ECGViewer.app/Contents/PkgInfo \
	ECGViewer.app/Contents/Resources/GenericJavaApp.icns \
	ECGViewer.app/Contents/Java/pixelmed.jar \
	ECGViewer.app/Contents/Java/${COMMONSCODECJARFILENAME}

TESTSCPECGRESULTFILES = \
	testresults/scpecg

PATHTOROOT = .

include Makefile.common.mk

generateicons:
	(cd icons; make all)

pixelmed.jar:	generateicons
	(cd com/pixelmed/slf4j; make all)
	(cd com/pixelmed/utils; make all)
	(cd com/pixelmed/dicom; make all)
	(cd com/pixelmed/geometry; make all)
	(cd com/pixelmed/validate; make all)
	(cd com/pixelmed/network; make all)
	(cd com/pixelmed/database; make all)
	(cd com/pixelmed/query; make all)
	(cd com/pixelmed/event; make all)
	(cd com/pixelmed/display/event; make all)
	(cd com/pixelmed/display; make all)
	(cd com/pixelmed/scpecg; make all)
	(cd com/pixelmed/displaywave; make all)
	(cd com/pixelmed/web; make all)
	(cd com/pixelmed/server; make all)
	(cd com/pixelmed/transfermonitor; make all)
	(cd com/pixelmed/convert; make all)
	(cd com/pixelmed/anatproc; make all)
	(cd com/pixelmed/dose; make all)
	(cd com/pixelmed/doseocr; make all)
	(cd com/pixelmed/ftp; make all)
	(cd com/pixelmed/apps; make all)
	(cd com/pixelmed/test; make all)
	date >BUILDDATE
	jar -cvf $@ BUILDDATE COPYRIGHT \
		com/pixelmed/display/*.class \
		com/pixelmed/display/*.properties \
		com/pixelmed/event/*.class \
		com/pixelmed/display/event/*.class \
		com/pixelmed/geometry/*.class \
		com/pixelmed/dicom/*.class com/pixelmed/dicom/*.dat com/pixelmed/dicom/*.xml com/pixelmed/dicom/*.icc \
		com/pixelmed/validate/*.class \
		com/pixelmed/validate/CommonDicomIODValidationRules.xsl com/pixelmed/validate/DicomIODDescriptionsCompiled.xsl \
		com/pixelmed/validate/CommonDicomSRValidationRules.xsl com/pixelmed/validate/DicomSRDescriptionsCompiled.xsl com/pixelmed/validate/CheckSRContentItemsUsed.xsl \
		com/pixelmed/validate/DicomContextGroupsSource.xml \
		com/pixelmed/utils/*.class \
		com/pixelmed/network/*.class \
		com/pixelmed/network/*.properties \
		com/pixelmed/database/*.class \
		com/pixelmed/query/*.class \
		com/pixelmed/scpecg/*.class \
		com/pixelmed/displaywave/*.class \
		com/pixelmed/web/*.class com/pixelmed/web/*.css com/pixelmed/web/*.tpl com/pixelmed/web/*.ico com/pixelmed/web/index.html \
		com/pixelmed/server/*.class \
		com/pixelmed/transfermonitor/*.class \
		com/pixelmed/convert/*.class \
		com/pixelmed/apps/*.class \
		com/pixelmed/anatproc/*.class \
		com/pixelmed/dose/*.class \
		com/pixelmed/doseocr/*.class \
		com/pixelmed/doseocr/OCR_Glyphs_DoseScreen.xml \
		com/pixelmed/test/*.class \
		com/pixelmed/ftp/*.class \
		com/pixelmed/slf4j/*.class \
		apple/dts/samplecode/osxadapter/*.class

webstart:	${WEBSTARTFILES}

webstart.clean:
	rm -f webstart/*.jar
	rm -f webstart/icons/*.*
	rm -f webstart/*.properties

webstart/icons/ConvertAmicasJPEG2000FilesetToDicom.png:	icons/ConvertAmicasJPEG2000FilesetToDicom.png
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/DicomCleaner.png:	icons/DicomCleaner.png
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/DicomImageBlackout.png:	icons/DicomImageViewer.png
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/DicomImageViewer.png:	icons/DicomImageViewer.png
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/DoseUtility.png:	icons/DoseUtility.png
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/DownloadOrTransmit.png:	icons/DownloadOrTransmit.png
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/MediaImporter.png:	icons/DicomImageViewer.png
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/WatchFolderAndSend.png:	icons/WatchFolderAndSend.png
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/ConvertAmicasJPEG2000FilesetToDicom.ico:	icons/ConvertAmicasJPEG2000FilesetToDicom.ico
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/DicomCleaner.ico:	icons/DicomCleaner.ico
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/DicomImageBlackout.ico:	icons/DicomImageViewer.ico
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/DicomImageViewer.ico:	icons/DicomImageViewer.ico
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/DoseUtility.ico:	icons/DoseUtility.ico
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/DownloadOrTransmit.ico:	icons/DownloadOrTransmit.ico
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/MediaImporter.ico:	icons/DicomImageViewer.ico
	mkdir -p webstart/icons
	cp $< $@

webstart/icons/WatchFolderAndSend.ico:	icons/WatchFolderAndSend.ico
	mkdir -p webstart/icons
	cp $< $@

webstart/pixelmed.jar:	pixelmed.jar
	mkdir -p webstart
	cp $< $@
	jar ufm $@ webstart/Manifest.txt
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/pixelmed_imageio.jar:	lib/additional/pixelmed_imageio.jar
	mkdir -p webstart
	cp $< $@
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/${COMMONSCODECJARFILENAME}:	${COMMONSCODECADDITIONALJAR}
	mkdir -p webstart
	cp $< $@
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/commons-net-ftp-2.0.jar:	lib/additional/commons-net-ftp-2.0.jar
	mkdir -p webstart
	cp $< $@
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/hsqldb.jar:	lib/additional/hsqldb.jar
	mkdir -p webstart
	cp $< $@
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jmdns.jar:	lib/additional/jmdns.jar
	mkdir -p webstart
	cp $< $@
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/${COMMONSCOMPRESSJARFILENAME}:	${COMMONSCOMPRESSADDITIONALJAR}
	mkdir -p webstart
	cp $< $@
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/vecmath1.2-1.14.jar:	lib/additional/vecmath1.2-1.14.jar
	mkdir -p webstart
	cp $< $@
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart.jaifiles:	${WEBSTARTJAIFILES}

# remove the previous signatures else error about expired certificates, per http://stackoverflow.com/questions/8176166/invalid-sha1-signature-file-digest

webstart/jai-imageio/early-access/1.1/linux-i586/clibwrapper_jiio.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/linux-i586/clibwrapper_jiio.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/linux-i586/jai_imageio.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/linux-i586/jai_imageio.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/linux-i586/libclib_jiio.so.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/linux-i586/libclib_jiio.so.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/solaris-i586/clibwrapper_jiio.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/solaris-i586/clibwrapper_jiio.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/solaris-i586/jai_imageio.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/solaris-i586/jai_imageio.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/solaris-i586/libclib_jiio.so.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/solaris-i586/libclib_jiio.so.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/solaris-sparc/clibwrapper_jiio.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/solaris-sparc/clibwrapper_jiio.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/solaris-sparc/jai_imageio.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/solaris-sparc/jai_imageio.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/solaris-sparc/libclib_jiio.so.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/solaris-sparc/libclib_jiio.so.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/solaris-sparc/libclib_jiio_vis.so.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/solaris-sparc/libclib_jiio_vis.so.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/solaris-sparc/libclib_jiio_vis2.so.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/solaris-sparc/libclib_jiio_vis2.so.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/windows-i586/clib_jiio.dll.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/windows-i586/clib_jiio.dll.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/windows-i586/clib_jiio_sse2.dll.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/windows-i586/clib_jiio_sse2.dll.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/windows-i586/clib_jiio_util.dll.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/windows-i586/clib_jiio_util.dll.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/windows-i586/clibwrapper_jiio.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/windows-i586/clibwrapper_jiio.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/jai-imageio/early-access/1.1/windows-i586/jai_imageio.jar:	webstart/download.java.net/media/jai-imageio/webstart/early-access/1.1/windows-i586/jai_imageio.jar
	mkdir -p `dirname $@`
	cp $< $@
	rm -rf unjar
	mkdir unjar
	(cd unjar; jar -xf "../$<"; rm -f META-INF/*.SF; rm -f META-INF/*.DSA; rm -f META-INF/*.RSA; jar -cf "../$@" *)
	rm -r unjar
	jarsigner -keystore ${PIXELMEDCODESIGNERKEYSTORE} ${PIXELMEDCODESIGNEROPTIONS} $@ ${PIXELMEDCODESIGNERALIAS}
	jarsigner -verify $@

webstart/sample.com.pixelmed.display.DicomImageViewer.properties:	sample.com.pixelmed.display.DicomImageViewer.properties
	cp $< $@

webstart/sample.com.pixelmed.display.DicomCleaner.properties:	sample.com.pixelmed.display.DicomCleaner.properties
	cp $< $@

macapps:	${MACAPPS}

ECGViewer_app:	\
		ECGViewer.app/Contents/Java/pixelmed.jar \
		ECGViewer.app/Contents/Java/${COMMONSCODECJARFILENAME} \
		ECGViewer.app/Contents/MacOS/JavaAppLauncher
	rm -rf $${HOME}/Applications/ECGViewer.app
	cp -r ECGViewer.app $${HOME}/Applications/

ECGViewer.app/Contents/Java/pixelmed.jar:	pixelmed.jar
	mkdir -p ECGViewer.app/Contents/Java
	cp $< $@
	
ECGViewer.app/Contents/Java/${COMMONSCODECJARFILENAME}:	${COMMONSCODECADDITIONALJAR}
	mkdir -p ECGViewer.app/Contents/Java
	cp $< $@

ECGViewer.app/Contents/MacOS/JavaAppLauncher:
	mkdir -p ECGViewer.app/Contents/MacOS
	if [ -f $@ ]; then chmod u+w $@; fi
	cp $${HOME}/Distributions/java/JavaAppLauncher $@

DicomImageViewer_app:	\
		DicomImageViewer.app/Contents/Java/pixelmed.jar \
		DicomImageViewer.app/Contents/Java/pixelmed_imageio.jar \
		DicomImageViewer.app/Contents/Java/hsqldb.jar \
		DicomImageViewer.app/Contents/Java/${COMMONSCOMPRESSJARFILENAME} \
		DicomImageViewer.app/Contents/Java/vecmath1.2-1.14.jar \
		DicomImageViewer.app/Contents/Java/${COMMONSCODECJARFILENAME} \
		DicomImageViewer.app/Contents/Java/jai_imageio.jar \
		DicomImageViewer.app/Contents/Java/jmdns.jar \
		DicomImageViewer.app/Contents/Resources/DicomImageViewer.icns \
		DicomImageViewer.app/Contents/MacOS/JavaAppLauncher
	rm -rf $${HOME}/Applications/DicomImageViewer.app
	cp -r DicomImageViewer.app $${HOME}/Applications/

DicomImageViewer.app/Contents/Java/pixelmed.jar:	pixelmed.jar
	mkdir -p DicomImageViewer.app/Contents/Java
	cp $< $@

DicomImageViewer.app/Contents/Java/pixelmed_imageio.jar:	lib/additional/pixelmed_imageio.jar
	mkdir -p DicomImageViewer.app/Contents/Java
	cp $< $@

DicomImageViewer.app/Contents/Java/hsqldb.jar:	lib/additional/hsqldb.jar
	mkdir -p DicomImageViewer.app/Contents/Java
	cp $< $@

DicomImageViewer.app/Contents/Java/${COMMONSCOMPRESSJARFILENAME}:	${COMMONSCOMPRESSADDITIONALJAR}
	mkdir -p DicomImageViewer.app/Contents/Java
	cp $< $@

DicomImageViewer.app/Contents/Java/vecmath1.2-1.14.jar:	lib/additional/vecmath1.2-1.14.jar
	mkdir -p DicomImageViewer.app/Contents/Java
	cp $< $@

DicomImageViewer.app/Contents/Java/${COMMONSCODECJARFILENAME}:	${COMMONSCODECADDITIONALJAR}
	mkdir -p DicomImageViewer.app/Contents/Java
	cp $< $@

DicomImageViewer.app/Contents/Java/jai_imageio.jar:	lib/additional/jai_imageio.jar
	mkdir -p DicomImageViewer.app/Contents/Java
	cp $< $@

DicomImageViewer.app/Contents/Java/jmdns.jar:	lib/additional/jmdns.jar
	mkdir -p DicomImageViewer.app/Contents/Java
	cp $< $@

DicomImageViewer.app/Contents/Resources/DicomImageViewer.icns:	icons/DicomImageViewer.icns
	mkdir -p DicomImageViewer.app/Contents/Resources
	cp $< $@

DicomImageViewer.app/Contents/MacOS/JavaAppLauncher:
	mkdir -p DicomImageViewer.app/Contents/MacOS
	if [ -f $@ ]; then chmod u+w $@; fi
	cp $${HOME}/Distributions/java/JavaAppLauncher $@

DicomCleaner_app:	\
		DicomCleaner.app/Contents/Java/pixelmed.jar \
		DicomCleaner.app/Contents/Java/pixelmed_imageio.jar \
		DicomCleaner.app/Contents/Java/hsqldb.jar \
		DicomCleaner.app/Contents/Java/${COMMONSCOMPRESSJARFILENAME} \
		DicomCleaner.app/Contents/Java/${COMMONSCODECJARFILENAME} \
		DicomCleaner.app/Contents/Java/jmdns.jar \
		DicomCleaner.app/Contents/Java/jai_imageio.jar \
		DicomCleaner.app/Contents/Resources/DicomCleaner.icns \
		DicomCleaner.app/Contents/MacOS/JavaAppLauncher
	rm -rf $${HOME}/Applications/DicomCleaner.app
	cp -r DicomCleaner.app $${HOME}/Applications/

DicomCleaner.app/Contents/Java/pixelmed.jar:	pixelmed.jar
	mkdir -p DicomCleaner.app/Contents/Java
	cp $< $@

DicomCleaner.app/Contents/Java/pixelmed_imageio.jar:	lib/additional/pixelmed_imageio.jar
	mkdir -p DicomCleaner.app/Contents/Java
	cp $< $@

DicomCleaner.app/Contents/Java/hsqldb.jar:	lib/additional/hsqldb.jar
	mkdir -p DicomCleaner.app/Contents/Java
	cp $< $@

DicomCleaner.app/Contents/Java/${COMMONSCOMPRESSJARFILENAME}:	${COMMONSCOMPRESSADDITIONALJAR}
	mkdir -p DicomCleaner.app/Contents/Java
	cp $< $@

DicomCleaner.app/Contents/Java/${COMMONSCODECJARFILENAME}:	${COMMONSCODECADDITIONALJAR}
	mkdir -p DicomCleaner.app/Contents/Java
	cp $< $@

DicomCleaner.app/Contents/Java/jmdns.jar:	lib/additional/jmdns.jar
	mkdir -p DicomCleaner.app/Contents/Java
	cp $< $@
	
DicomCleaner.app/Contents/Java/jai_imageio.jar:	linux/lib/jai_imageio.jar
	mkdir -p DicomCleaner.app/Contents/Java
	cp $< $@

DicomCleaner.app/Contents/Resources/DicomCleaner.icns:	icons/DicomCleaner.icns
	mkdir -p DicomCleaner.app/Contents/Resources
	cp $< $@

DicomCleaner.app/Contents/MacOS/JavaAppLauncher:
	mkdir -p DicomCleaner.app/Contents/MacOS
	if [ -f $@ ]; then chmod u+w $@; fi
	cp $${HOME}/Distributions/java/JavaAppLauncher $@

DoseUtility_app:	\
		DoseUtility.app/Contents/Java/pixelmed.jar \
		DoseUtility.app/Contents/Java/pixelmed_imageio.jar \
		DoseUtility.app/Contents/Java/hsqldb.jar \
		DoseUtility.app/Contents/Java/${COMMONSCOMPRESSJARFILENAME} \
		DoseUtility.app/Contents/Java/${COMMONSCODECJARFILENAME} \
		DoseUtility.app/Contents/Java/commons-net-ftp-2.0.jar \
		DoseUtility.app/Contents/Java/jmdns.jar \
		DoseUtility.app/Contents/Java/jai_imageio.jar \
		DoseUtility.app/Contents/Resources/DoseUtility.icns \
		DoseUtility.app/Contents/MacOS/JavaAppLauncher
	rm -rf $${HOME}/Applications/DoseUtility.app
	cp -r DoseUtility.app $${HOME}/Applications/

DoseUtility.app/Contents/Java/pixelmed.jar:	pixelmed.jar
	mkdir -p DoseUtility.app/Contents/Java
	cp $< $@

DoseUtility.app/Contents/Java/pixelmed_imageio.jar:	lib/additional/pixelmed_imageio.jar
	mkdir -p DoseUtility.app/Contents/Java
	cp $< $@

DoseUtility.app/Contents/Java/hsqldb.jar:	lib/additional/hsqldb.jar
	mkdir -p DoseUtility.app/Contents/Java
	cp $< $@

DoseUtility.app/Contents/Java/${COMMONSCOMPRESSJARFILENAME}:	${COMMONSCOMPRESSADDITIONALJAR}
	mkdir -p DoseUtility.app/Contents/Java
	cp $< $@

DoseUtility.app/Contents/Java/${COMMONSCODECJARFILENAME}:	${COMMONSCODECADDITIONALJAR}
	mkdir -p DoseUtility.app/Contents/Java
	cp $< $@

DoseUtility.app/Contents/Java/commons-net-ftp-2.0.jar:	lib/additional/commons-net-ftp-2.0.jar
	mkdir -p DoseUtility.app/Contents/Java
	cp $< $@

DoseUtility.app/Contents/Java/jmdns.jar:	lib/additional/jmdns.jar
	mkdir -p DoseUtility.app/Contents/Java
	cp $< $@

DoseUtility.app/Contents/Java/jai_imageio.jar:	linux/lib/jai_imageio.jar
	mkdir -p DoseUtility.app/Contents/Java
	cp $< $@

DoseUtility.app/Contents/Resources/DoseUtility.icns:	icons/DoseUtility.icns
	mkdir -p DoseUtility.app/Contents/Resources
	cp $< $@

DoseUtility.app/Contents/MacOS/JavaAppLauncher:
	mkdir -p DoseUtility.app/Contents/MacOS
	if [ -f $@ ]; then chmod u+w $@; fi
	cp $${HOME}/Distributions/java/JavaAppLauncher $@

changelog:
	rm -f CHANGES
	cvsps -b HEAD -u -q | egrep -v '^(PatchSet|Author:|Branch:|Tag:|Members:|Log:)' | fgrep -v '*** empty log message ***' | grep -v '^[ ]*$$' | sed -e 's/:[0-9.]*->[0-9.]*//' -e 's/:INITIAL->[0-9.]*//' -e 's/^Date: \([0-9][0-9][0-9][0-9]\/[0-9][0-9]\/[0-9][0-9]\) [0-9:]*$$/\1/' >CHANGES
	bzip2 <CHANGES >CHANGES.bz2
	
clean:	cleanallexceptjar cleanapp webstart.clean windows.clean
	rm -f pixelmed.jar

cleanapp:
	rm -f DicomImageViewer.app/Contents/Java/*.jar
	rm -f DicomImageViewer.app/Contents/Resources/DicomImageViewer.icns
	rm -f DicomCleaner.app/Contents/Java/*.jar
	rm -f DicomCleaner.app/Contents/Resources/DicomCleaner.icns
	rm -f DoseUtility.app/Contents/Java/*.jar
	rm -f DoseUtility.app/Contents/Resources/DoseUtility.icns
	rm -f ECGViewer.app/Contents/Java/*.jar
	rm -f ECGViewer.app/Contents/Resources/ECGViewer.icns

cleanallexceptjar:	cleansubdirs
	(cd icons; make clean)
	rm -f *~ *.class .exclude.list

cleansubdirs:
	for d in ${SUBDIRS}; \
	do \
		if [ -d $$d ]; then \
			(cd $$d; echo "Cleaning in $$d"; make clean); \
		fi; \
	done

archivelocalizations: clean .exclude.list
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cv -X .exclude.list -f - ${LOCALIZATIONFILES} | ${COMPRESS} > pixelmedjavadicom_localizations_archive.`date '+%Y%m%d'`.tar.${COMPRESSEXT}

archivesource: clean .exclude.list
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cv -X .exclude.list -f - ${ADDITIONALFILES} ${SUBDIRS} | ${COMPRESS} > pixelmedjavadicom_source_archive.`date '+%Y%m%d'`.tar.${COMPRESSEXT}

archivejavadoc: .exclude.list #javadoc
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cv -X .exclude.list -f - ${JAVADOCFILES} | ${COMPRESS} > pixelmedjavadicom_javadoc_archive.`date '+%Y%m%d'`.tar.${COMPRESSEXT}

archivedoxygen: .exclude.list #doxygen
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cv -X .exclude.list -f - ${DOXYGENFILES} | ${COMPRESS} > pixelmedjavadicom_javadoc_archive.`date '+%Y%m%d'`.tar.${COMPRESSEXT}

# without doxygenrelease
releaseall:	changelog sourcerelease javadocrelease binaryrelease macapprelease macappecgrelease dependencyrelease otherdocrelease executablerelease

binaryrelease: cleanallexceptjar .exclude.list pixelmed.jar #javadoc doxygen
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cv -X .exclude.list -f - ${BINARYRELEASEFILES} | ${COMPRESS} > pixelmedjavadicom_binaryrelease.`date '+%Y%m%d'`.tar.${COMPRESSEXT}

otherdocrelease:
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cv -f - ${OTHERDOCRELEASEFILES} | ${COMPRESS} > pixelmedjavadicom_otherdocsrelease.`date '+%Y%m%d'`.tar.${COMPRESSEXT}

dependencyrelease:	.exclude.list
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cv -X .exclude.list -f - ${DEPENDENCYRELEASEFILES} | ${COMPRESS} > pixelmedjavadicom_dependencyrelease.`date '+%Y%m%d'`.tar.${COMPRESSEXT}

sourcerelease: clean .exclude.list generateicons # javadoc doxygen
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cv -X .exclude.list -f - ${SOURCERELEASEFILES} | ${COMPRESS} > pixelmedjavadicom_sourcerelease.`date '+%Y%m%d'`.tar.${COMPRESSEXT}

javadocrelease:  .exclude.list #clean javadoc doxygen
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cv -X .exclude.list -f - ${JAVADOCRELEASEFILES} | ${COMPRESS} > pixelmedjavadicom_javadocrelease.`date '+%Y%m%d'`.tar.${COMPRESSEXT}

doxygenrelease: .exclude.list #clean javadoc doxygen
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cv -X .exclude.list -f - ${DOXYGENRELEASEFILES} | ${COMPRESS} > pixelmedjavadicom_doxygenrelease.`date '+%Y%m%d'`.tar.${COMPRESSEXT}

macapprelease: ${MACAPPS} ${MACAPPRELEASEFILES} .macappexclude.list 
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cv -X .macappexclude.list -f - ${MACAPPRELEASEFILES} | ${COMPRESS} > pixelmedjavadicom_macapprelease.`date '+%Y%m%d'`.tar.${COMPRESSEXT}

macappecgrelease: ${MACAPPECGRELEASEFILES} .macappexclude.list 
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cv -X .macappexclude.list -f - ${MACAPPECGRELEASEFILES} | ${COMPRESS} > pixelmedjavadicom_macappecgrelease.`date '+%Y%m%d'`.tar.${COMPRESSEXT}

archivescpecgtestresults:
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cv -X .exclude.list -f - ${TESTSCPECGRESULTFILES} | ${COMPRESS} > scpecgtestresults.`date '+%Y%m%d'`.tar.${COMPRESSEXT}

archiveosiriswork2:
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cvf - $${HOME}/dctool.support/images/dicom/sr/osiris_work2 | ${COMPRESS} > osiris_work2.tar.${COMPRESSEXT}

archivetests:
	export COPYFILE_DISABLE=true; \
	export COPY_EXTENDED_ATTRIBUTES_DISABLE=true; \
	${TAR} -cvf - $${HOME}/dctool.support/images/dicom/sr/mytests | ${COMPRESS} > mytests.tar.${COMPRESSEXT}

.exclude.list:	Makefile
	echo "Making .exclude.list"
	echo ".DS_Store" > $@
	echo ".directory" >> $@
	echo "*.tar.gz" >> $@
	echo "*.tar.bz2" >> $@
	echo "*.tar.bz2" >> $@
	#ls *.jar | grep -v 'pixelmed.jar' >> $@
	echo "cleanerdst.*" >> $@
	echo "cleanersrc.*" >> $@
	#find . -path  '*/cleanerdst.*' | sed 's/[.][/]//' >>$@
	#find . -path  '*/cleanersrc.*' | sed 's/[.][/]//' >>$@
	find . -path '*/NOTES*' | sed 's/[.][/]//' >>$@
	find . -path '*/CVS*' | sed 's/[.][/]//' >>$@
	echo "com/pixelmed/web/favicon.ill" >> $@
	#cat $@

.macappexclude.list:	Makefile
	echo "Making .exclude.list"
	echo ".DS_Store" > $@
	
executablerelease:	\
	DicomCleanerAssumingJREInstalled.zip \
	DicomCleanerWithOwnJRE.zip \
	DicomCleanerMac.zip \
	DoseUtilityAssumingJREInstalled.zip \
	DoseUtilityWithOwnJRE.zip \
	DoseUtilityMac.zip

DicomCleanerAssumingJREInstalled.zip: DicomCleanerAssumingJREInstalled.bat pixelmed.jar windows.lib .exclude.list
	rm -rf $@
	cp DicomCleanerAssumingJREInstalled.bat DicomCleaner.bat
	zip -r $@ DicomCleaner.bat pixelmed.jar ${DICOMCLEANERDEPENDENCYRELEASEFILES} ${WINDOWSJIIORELEASEFILES} -x@.exclude.list
	rm  DicomCleaner.bat

DicomCleanerWithOwnJRE.zip: DicomCleanerWithOwnJRE.bat pixelmed.jar windows.jre .exclude.list
	rm -rf $@
	cp DicomCleanerWithOwnJRE.bat DicomCleaner.bat
	zip -r $@ DicomCleaner.bat windows/jre pixelmed.jar ${DICOMCLEANERDEPENDENCYRELEASEFILES} -x@.exclude.list
	rm  DicomCleaner.bat
	
DicomCleanerMac.zip:	DicomCleaner_app
	rm -rf $@
	zip -r $@ DicomCleaner.app

DoseUtilityAssumingJREInstalled.zip: DoseUtilityAssumingJREInstalled.bat pixelmed.jar windows.lib .exclude.list
	rm -rf $@
	cp DoseUtilityAssumingJREInstalled.bat DoseUtility.bat
	zip -r $@ DoseUtility.bat pixelmed.jar ${DOSEUTILITYDEPENDENCYRELEASEFILES} ${WINDOWSJIIORELEASEFILES} -x@.exclude.list
	rm  DoseUtility.bat

DoseUtilityWithOwnJRE.zip: DoseUtilityWithOwnJRE.bat pixelmed.jar windows.jre .exclude.list
	rm -rf $@
	cp DoseUtilityWithOwnJRE.bat DoseUtility.bat
	zip -r $@ DoseUtility.bat windows/jre pixelmed.jar ${DOSEUTILITYDEPENDENCYRELEASEFILES} -x@.exclude.list
	rm  DoseUtility.bat
	
DoseUtilityMac.zip:	DoseUtility_app
	rm -rf $@
	zip -r $@ DoseUtility.app

windows.jre:
	if [ -d windows ]; then \
		(cd windows; make jre); \
	fi
windows.clean:
	if [ -d windows ]; then \
		(cd windows; make clean); \
	fi
	
windows.lib:
	if [ -d windows ]; then \
		(cd windows; make lib); \
	fi

# used to link to "http://www.junit.org/apidocs/" but this no longer works ... use version-specific "http://javasourcecode.org/html/open-source/junit/junit-4.8.1/" instead
# used to link to "http://jpedal.org/javadoc/" but this no longer works ... tried archive.org copy ("http://web.archive.org/web/20121002020430/http://jpedal.org/javadoc/") instead but gives "error fetching" ... see also possibly related "http://javadoc.idrsolutions.com/" (does not respond), "https://domaingang.com/domain-law/jpedal-org-complainant-gets-domain-back-after-it-lapsed-via-the-udrp-process/"
javadoc:
	rm -rf docs/javadoc
	javadoc \
		-nodeprecated \
		-classpath .:lib/additional/slf4j-api-1.7.22.jar:${COMMONSCOMPRESSADDITIONALJAR}:lib/additional/hsqldb.jar:lib/additional/vecmath1.2-1.14.jar:${COMMONSCODECADDITIONALJAR}:lib/additional/commons-net-ftp-2.0.jar:lib/additional/jmdns.jar:lib/additional/jpedalSTD.jar:lib/junit/junit-4.8.1.jar:lib/additional/javax.json-api-1.0.jar \
		-link http://docs.oracle.com/javase/7/docs/api/ \
		-link http://www.hsqldb.org/doc/src/ \
		-link http://www.slf4j.org/api \
		-protected -d docs/javadoc \
		-encoding "UTF8" \
		${SUBPACKAGES}

doxygen:
	rm -rf docs/doxygen
	doxygen Doxyfile

docs/supportedstoragesopclasses.txt:	com/pixelmed/dicom/SOPClass.java $${HOME}/work/dicom3tools/libsrc/standard/sopcl.tpl Makefile
	if [ -f docs/supportedstoragesopclasses.txt ]; then \
		rm -f docs/supportedstoragesopclasses.txt.old; \
		mv docs/supportedstoragesopclasses.txt docs/supportedstoragesopclasses.txt.old; \
	fi
	sed < com/pixelmed/dicom/SOPClass.java \
			-e '1,/public static final String.. arrayOfStorageSOPClasses = {/d' \
			-e '/};/,$$d' \
			-e 's/^[^A-Za-z]*//' \
			-e 's/,//' \
		| xargs -I % grep % $${HOME}/work/dicom3tools/libsrc/standard/sopcl.tpl \
		| sed -e 's/^.*Desc="\([^"]*\)".*Uid="\([0-9.]*\)".*$$/\1:\2/' \
		| awk -F: '{print $$1 "\t" $$2}' \
		>docs/supportedstoragesopclasses.txt
	if [ -f docs/supportedstoragesopclasses.txt.old ]; then \
		diff docs/supportedstoragesopclasses.txt.old docs/supportedstoragesopclasses.txt; \
	fi

