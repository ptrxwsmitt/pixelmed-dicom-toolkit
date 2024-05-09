PATHTOROOT = ../../..

OBJS = \
OCR.class \
TestOCRAgainstTabulatedData.class \
ExposureDoseSequence.class \
RenderedDoseReport.class \
GenerateRadiationDoseStructuredReport.class \
LegacyRadiationDoseOCRDicomForwardingService.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

clean:
	rm -f *~ *.class core *.bak ${OBJS}

testocrtrain:	${OBJS}
	cp OCR_Glyphs_DoseScreen.xml /tmp/testocr_all.xml
	for i in ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/*.tif; \
	do \
		echo $$i; \
		java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR $$i -  - /tmp/testocr_all.xml /tmp/testocr_all.xml; \
	done
	@echo "New glyph dictionary source is in /tmp/testocr_all.xml"

testocr:	${OBJS}
	for i in ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/*.tif; \
	do \
		echo $$i; \
		java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR $$i - - - -; \
	done

testocronepgm:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ge_discoveryste_screen_original.pgm - - - -

testocronedcm:	${OBJS}
	@echo "Expect mismatch between specified and computed total DLP due to mixed phantoms and use of conversion factor rather than simple summation"
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ge_discoveryste_screen_original.dcm - - - -

testocronedcmunspecifiedglyphfile:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ge_discoveryste_screen_original.dcm \
		- - \
		-

testocronedcmdebuglevel:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.doseocr.OCR=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dose.CTDose=debug \
		com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ge_discoveryste_screen_original.dcm \
		- - \
		- -
	@echo "Expect mismatch between specified and computed total DLP due to mixed phantoms and use of conversion factor rather than simple summation"

testocronedcmtracelevel:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.doseocr.OCR=trace \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dose.CTDose=trace \
		com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ge_discoveryste_screen_original.dcm \
		- - \
		- -

testocronedcmwindow1:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ge_lightspeedvct_amc16bitsignedtoonarrowwindow.dcm \
		- - \
		- -
	@echo "Expect mismatch between specified and computed total DLP due to mixed phantoms and use of conversion factor rather than simple summation"

testocronedcmwindow2:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsWithBothDoseImageAndSR/Cleaned/CD0817141621_Cleaned/DICOM/I619 \
		- - \
		- -
		
testocronedcmwindow3:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseFilesCopied/series999FilesCopied/1.2.124.113532.10.8.8.91.20080701.165343.367049 \
		- - \
		- -
		
testocronedcmglyphfile:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ge_discoveryste_screen_original.dcm \
		- - \
		OCR_Glyphs_DoseScreen.xml
	@echo "Expect mismatch between specified and computed total DLP due to mixed phantoms and use of conversion factor rather than simple summation"

testocronedcmtrainglyphfile:	${OBJS}
	cp OCR_Glyphs_DoseScreen.xml /tmp/testocr_all.xml
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ge_discoveryste_screen_original.dcm \
		- - \
		/tmp/testocr_all.xml /tmp/testocr_all.xml
	@echo "Expect mismatch between specified and computed total DLP due to mixed phantoms and use of conversion factor rather than simple summation"
	@echo "New glyph dictionary source is in /tmp/testocr_all.xml"

testocronedcmallargshyphen:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ge_discoveryste_screen_original.dcm \
		- - - -
	@echo "Expect mismatch between specified and computed total DLP due to mixed phantoms and use of conversion factor rather than simple summation"

testocronedcmsroutput:	${OBJS}
	@rm -f /tmp/dosesrfile.dcm
	@echo "Building ..."
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ge_discoveryste_screen_original.dcm \
		- \
		/tmp/dosesrfile.dcm
	@echo "Expect mismatch between specified and computed total DLP due to mixed phantoms and use of conversion factor rather than simple summation"
	@echo "Dumping ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm

testocralltif:	${OBJS}
	for i in ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ctdosereportoriginals_allinonefolder/*.tif; \
	do \
		echo $$i; \
		java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR $$i ; \
	done

testocralldcm:	${OBJS}
	for i in ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ctdosereportoriginals_allinonefolder/*dcm ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseFilesCopied/series999FilesCopied/[0-9]* ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseFilesCopied/series501FilesCopied/[0-9]*; \
	do \
		echo $$i; \
		java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR $$i ; \
	done

testocralldcmsr_create:	${OBJS}
	mkdir -p ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/srcomparefiles
	#for i in ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ctdosereportoriginals_allinonefolder/*dcm ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseFilesCopied/series999FilesCopied/[0-9]* ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseFilesCopied/series501FilesCopied/[0-9]*;
	for i in ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ctdosereportoriginals_allinonefolder/*dcm ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseFilesCopied/series501FilesCopied/[0-9]*; \
	do \
		echo $$i; \
		srcomparebasename=`dckey -noerror -ignorereaderrors -k SOPInstanceUID $$i 2>&1 | egrep -iv '(error|warning)'`.sr; \
		java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR $$i - ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/srcomparefiles/$$srcomparebasename; \
	done

testocralldcmsr:	${OBJS}
	#for i in ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ctdosereportoriginals_allinonefolder/*dcm ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseFilesCopied/series999FilesCopied/[0-9]* ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseFilesCopied/series501FilesCopied/[0-9]*;
	for i in ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ctdosereportoriginals_allinonefolder/*dcm ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseFilesCopied/series501FilesCopied/[0-9]*; \
	do \
		echo $$i; \
		srcomparebasename=`dckey -noerror -ignorereaderrors -k SOPInstanceUID $$i 2>&1 | egrep -iv '(error|warning)'`.sr; \
		java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR $$i - /tmp/dosesrfile.dcm; \
		echo "Comparing SR file"; \
		dcsrdiff ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/srcomparefiles/$$srcomparebasename /tmp/dosesrfile.dcm; \
		dckey -describe -k TimezoneOffsetFromUTC $$i; \
		dckey -describe -k TimezoneOffsetFromUTC /tmp/dosesrfile.dcm; \
		rm -f /tmp/dosesrfile.dcm; \
	done

testocralldcmsrwithvalidation:	${OBJS}
	#for i in ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ctdosereportoriginals_allinonefolder/*dcm ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseFilesCopied/series999FilesCopied/[0-9]* ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseFilesCopied/series501FilesCopied/[0-9]*;
	for i in ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ctdosereportoriginals_allinonefolder/*dcm ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseFilesCopied/series501FilesCopied/[0-9]*; \
	do \
		echo $$i; \
		srcomparebasename=`dckey -noerror -ignorereaderrors -k SOPInstanceUID $$i 2>&1 | egrep -iv '(error|warning)'`.sr; \
		java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR $$i - /tmp/dosesrfile.dcm; \
		echo "Comparing SR file"; \
		dcsrdiff ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/srcomparefiles/$$srcomparebasename /tmp/dosesrfile.dcm; \
		echo "Validating structure ..."; \
		java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm; \
		echo "Validating DICOM encoding ..."; \
		dciodvfy /tmp/dosesrfile.dcm; \
		rm -f /tmp/dosesrfile.dcm; \
	done

testocrdifferentfont:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ge_lightspeedplus_differentfont_screen_original.pgm \
		- - \
		- /tmp/testocr_all.xml
	@echo "New glyph dictionary source is in /tmp/testocr_all.xml"
	
testocryetanotherdifferentfont:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ge_lightspeed16_yetanotherdifferentfont_screen.dcm \
		- - \
		- /tmp/testocr_all.xml
	@echo "New glyph dictionary source is in /tmp/testocr_all.xml"

testocracquiredimages:	${OBJS}
	@echo "Building ..."
	@rm -f /tmp/dosesrfile.dcm
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCDImageAcquiredImagesAndSeries999/A/A/A/K/Z04 \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCDImageAcquiredImagesAndSeries999/DICOMDIR \
		/tmp/dosesrfile.dcm
	@echo "Dumping ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm

testocracquiredimagesfindscreen:	${OBJS}
	@echo "Doing testocracquiredimagesfindscreen ..."
	@echo "Building ..."
	@rm -f /tmp/dosesrfile.dcm
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		- \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCDImageAcquiredImagesAndSeries999/DICOMDIR \
		/tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.build.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.build.log ] ; \
	then \
		echo "Comparing build log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.build.log /tmp/dosesrfile.dcm.build.log; \
	fi
	@echo "Dumping ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcsrdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.dcsrdump.log ] ; \
	then \
		echo "Comparing dcsrdump log without content item identifiers ..."; \
		sed -e 's/>[0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old; \
		sed -e 's/>[0-9.]*: //' </tmp/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		echo "Comparing dcsrdump log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.dcsrdump.log /tmp/dosesrfile.dcm.dcsrdump.log; \
	fi
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.DicomSRValidator.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.DicomSRValidator.log ] ; \
	then \
		echo "Comparing structure validation log without content item identifiers ..."; \
		sed -e 's/within [0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old; \
		sed -e 's/within [0-9.]*: //' </tmp/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		echo "Comparing structure validation log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.DicomSRValidator.log /tmp/dosesrfile.dcm.DicomSRValidator.log; \
	fi
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dciodvfy.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.dciodvfy.log ] ; \
	then \
		echo "Comparing DICOM encoding validation log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.dciodvfy.log /tmp/dosesrfile.dcm.dciodvfy.log; \
	fi

testocracquiredimagesfindscreen_saveforcomparison:	testocracquiredimagesfindscreen
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen
	cp /tmp/dosesrfile.dcm.build.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.build.log
	cp /tmp/dosesrfile.dcm.dcsrdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.dcsrdump.log
	cp /tmp/dosesrfile.dcm.DicomSRValidator.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.DicomSRValidator.log
	cp /tmp/dosesrfile.dcm.dciodvfy.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesfindscreen/dosesrfile.dcm.dciodvfy.log

testocrgenoacquiredimagesexposuredosesequenceinscreen:	${OBJS}
	@echo "Doing testocrgenoacquiredimagesexposuredosesequenceinscreen ..."
	@echo "Building ..."
	@rm -f /tmp/dosesrfile.dcm
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ACRLosslessCompressed/uncompressed/Test2.dcm \
		- \
		/tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.build.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.build.log ] ; \
	then \
		echo "Comparing build log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.build.log /tmp/dosesrfile.dcm.build.log; \
	fi
	@echo "Dumping SR ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcsrdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.dcsrdump.log ] ; \
	then \
		echo "Comparing dcsrdump log without content item identifiers ..."; \
		sed -e 's/>[0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old; \
		sed -e 's/>[0-9.]*: //' </tmp/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		echo "Comparing dcsrdump log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.dcsrdump.log /tmp/dosesrfile.dcm.dcsrdump.log; \
	fi
	@echo "Dumping ..."
	dcdump /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.dcdump.log ] ; \
	then \
		echo "Comparing dcdump log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.dcdump.log /tmp/dosesrfile.dcm.dcdump.log; \
	fi
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.DicomSRValidator.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.DicomSRValidator.log ] ; \
	then \
		echo "Comparing structure validation log without content item identifiers ..."; \
		sed -e 's/within [0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old; \
		sed -e 's/within [0-9.]*: //' </tmp/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		echo "Comparing structure validation log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.DicomSRValidator.log /tmp/dosesrfile.dcm.DicomSRValidator.log; \
	fi
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dciodvfy.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.dciodvfy.log ] ; \
	then \
		echo "Comparing DICOM encoding validation log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.dciodvfy.log /tmp/dosesrfile.dcm.dciodvfy.log; \
	fi

testocrgenoacquiredimagesexposuredosesequenceinscreen_saveforcomparison:	testocrgenoacquiredimagesexposuredosesequenceinscreen
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen
	cp /tmp/dosesrfile.dcm.build.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.build.log
	cp /tmp/dosesrfile.dcm.dcsrdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.dcsrdump.log
	cp /tmp/dosesrfile.dcm.dcdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.dcdump.log
	cp /tmp/dosesrfile.dcm.DicomSRValidator.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.DicomSRValidator.log
	cp /tmp/dosesrfile.dcm.dciodvfy.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgenoacquiredimagesexposuredosesequenceinscreen/dosesrfile.dcm.dciodvfy.log

testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition:	${OBJS}
	@echo "Doing testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition ..."
	@echo "Building ..."
	@rm -f /tmp/dosesrfile.dcm
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		- \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsWithSeries501or997or999or10999/CD0121151829/DICOMDIR" \
		/tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.build.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.build.log ] ; \
	then \
		echo "Comparing build log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.build.log /tmp/dosesrfile.dcm.build.log; \
	fi
	@echo "Dumping SR ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcsrdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.dcsrdump.log ] ; \
	then \
		echo "Comparing dcsrdump log without content item identifiers ..."; \
		sed -e 's/>[0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old; \
		sed -e 's/>[0-9.]*: //' </tmp/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		echo "Comparing dcsrdump log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.dcsrdump.log /tmp/dosesrfile.dcm.dcsrdump.log; \
	fi
	@echo "Dumping ..."
	dcdump /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.dcdump.log ] ; \
	then \
		echo "Comparing dcdump log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.dcdump.log /tmp/dosesrfile.dcm.dcdump.log; \
	fi
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.DicomSRValidator.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.DicomSRValidator.log ] ; \
	then \
		echo "Comparing structure validation log without content item identifiers ..."; \
		sed -e 's/within [0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old; \
		sed -e 's/within [0-9.]*: //' </tmp/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		echo "Comparing structure validation log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.DicomSRValidator.log /tmp/dosesrfile.dcm.DicomSRValidator.log; \
	fi
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dciodvfy.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.dciodvfy.log ] ; \
	then \
		echo "Comparing DICOM encoding validation log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.dciodvfy.log /tmp/dosesrfile.dcm.dciodvfy.log; \
	fi

testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition_saveforcomparison:	testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition
	cp /tmp/dosesrfile.dcm.build.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.build.log
	cp /tmp/dosesrfile.dcm.dcsrdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.dcsrdump.log
	cp /tmp/dosesrfile.dcm.dcdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.dcdump.log
	cp /tmp/dosesrfile.dcm.DicomSRValidator.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.DicomSRValidator.log
	cp /tmp/dosesrfile.dcm.dciodvfy.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition/dosesrfile.dcm.dciodvfy.log


testocrgeacquiredimagesandexposuredosesequence:	${OBJS}
	@echo "Doing testocrgeacquiredimagesandexposuredosesequence ..."
	@echo "Building ..."
	@rm -f /tmp/dosesrfile.dcm
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		- \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsWithSeries501or997or999or10999/CD0124140506/DICOMDIR" \
		/tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.build.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.build.log ] ; \
	then \
		echo "Comparing build log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.build.log /tmp/dosesrfile.dcm.build.log; \
	fi
	@echo "Dumping SR ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcsrdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.dcsrdump.log ] ; \
	then \
		echo "Comparing dcsrdump log without content item identifiers ..."; \
		sed -e 's/>[0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old; \
		sed -e 's/>[0-9.]*: //' </tmp/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		echo "Comparing dcsrdump log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.dcsrdump.log /tmp/dosesrfile.dcm.dcsrdump.log; \
	fi
	@echo "Dumping ..."
	dcdump /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.dcdump.log ] ; \
	then \
		echo "Comparing dcdump log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.dcdump.log /tmp/dosesrfile.dcm.dcdump.log; \
	fi
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.DicomSRValidator.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.DicomSRValidator.log ] ; \
	then \
		echo "Comparing structure validation log without content item identifiers ..."; \
		sed -e 's/within [0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old; \
		sed -e 's/within [0-9.]*: //' </tmp/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		echo "Comparing structure validation log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.DicomSRValidator.log /tmp/dosesrfile.dcm.DicomSRValidator.log; \
	fi
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dciodvfy.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.dciodvfy.log ] ; \
	then \
		echo "Comparing DICOM encoding validation log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.dciodvfy.log /tmp/dosesrfile.dcm.dciodvfy.log; \
	fi

testocrgeacquiredimagesandexposuredosesequence_saveforcomparison:	testocrgeacquiredimagesandexposuredosesequence
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence
	cp /tmp/dosesrfile.dcm.build.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.build.log
	cp /tmp/dosesrfile.dcm.dcsrdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.dcsrdump.log
	cp /tmp/dosesrfile.dcm.dcdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.dcdump.log
	cp /tmp/dosesrfile.dcm.DicomSRValidator.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.DicomSRValidator.log
	cp /tmp/dosesrfile.dcm.dciodvfy.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgeacquiredimagesandexposuredosesequence/dosesrfile.dcm.dciodvfy.log

testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity:	${OBJS}
	@echo "Doing testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity ..."
	@echo "Building ..."
	@rm -f /tmp/dosesrfile.dcm
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ACRDose-OCR/SampleDoseSheets/Misc/DOSE_REPORT_doseSC.dcm" \
		- \
		/tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.build.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.build.log ] ; \
	then \
		echo "Comparing build log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.build.log /tmp/dosesrfile.dcm.build.log; \
	fi
	@echo "Dumping SR ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcsrdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.dcsrdump.log ] ; \
	then \
		echo "Comparing dcsrdump log without content item identifiers ..."; \
		sed -e 's/>[0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old; \
		sed -e 's/>[0-9.]*: //' </tmp/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		echo "Comparing dcsrdump log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.dcsrdump.log /tmp/dosesrfile.dcm.dcsrdump.log; \
	fi
	@echo "Dumping ..."
	dcdump /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.dcdump.log ] ; \
	then \
		echo "Comparing dcdump log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.dcdump.log /tmp/dosesrfile.dcm.dcdump.log; \
	fi
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.DicomSRValidator.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.DicomSRValidator.log ] ; \
	then \
		echo "Comparing structure validation log without content item identifiers ..."; \
		sed -e 's/within [0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old; \
		sed -e 's/within [0-9.]*: //' </tmp/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		echo "Comparing structure validation log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.DicomSRValidator.log /tmp/dosesrfile.dcm.DicomSRValidator.log; \
	fi
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dciodvfy.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.dciodvfy.log ] ; \
	then \
		echo "Comparing DICOM encoding validation log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.dciodvfy.log /tmp/dosesrfile.dcm.dciodvfy.log; \
	fi

testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity_saveforcomparison:	testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity
	cp /tmp/dosesrfile.dcm.build.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.build.log
	cp /tmp/dosesrfile.dcm.dcsrdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.dcsrdump.log
	cp /tmp/dosesrfile.dcm.dcdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.dcdump.log
	cp /tmp/dosesrfile.dcm.DicomSRValidator.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.DicomSRValidator.log
	cp /tmp/dosesrfile.dcm.dciodvfy.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity/dosesrfile.dcm.dciodvfy.log

testocrgemultipagesmartstep:	${OBJS}
	mkdir -p /tmp/testocrgemultipagesmartstep
	rm -f /tmp/testocrgemultipagesmartstep/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ACRLosslessCompressed/uncompressed/Test1-1.dcm" /tmp/testocrgemultipagesmartstep/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/ACRLosslessCompressed/uncompressed/Test1-2.dcm" /tmp/testocrgemultipagesmartstep/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrgemultipagesmartstep \
		- - \
		- - 0

testocrdatafiles:	TestOCRAgainstTabulatedData.class ${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${JUNITJAR}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true org.junit.runner.JUnitCore com.pixelmed.doseocr.TestOCRAgainstTabulatedData

testphilipsonedcm:	${OBJS}
	@echo "Doing testphilipsonedcm ..."
	@echo "Building ..."
	@rm -f /tmp/dosesrfile.dcm
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.ExposureDoseSequence \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/philips_brilliance64_sc_original.dcm \
		- /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.build.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.build.log ] ; \
	then \
		echo "Comparing build log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.build.log /tmp/dosesrfile.dcm.build.log; \
	fi
	@echo "Dumping SR ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcsrdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.dcsrdump.log ] ; \
	then \
		echo "Comparing dcsrdump log without content item identifiers ..."; \
		sed -e 's/>[0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old; \
		sed -e 's/>[0-9.]*: //' </tmp/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		echo "Comparing dcsrdump log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.dcsrdump.log /tmp/dosesrfile.dcm.dcsrdump.log; \
	fi
	@echo "Dumping ..."
	dcdump /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.dcdump.log ] ; \
	then \
		echo "Comparing dcdump log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.dcdump.log /tmp/dosesrfile.dcm.dcdump.log; \
	fi
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.DicomSRValidator.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.DicomSRValidator.log ] ; \
	then \
		echo "Comparing structure validation log without content item identifiers ..."; \
		sed -e 's/within [0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old; \
		sed -e 's/within [0-9.]*: //' </tmp/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		echo "Comparing structure validation log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.DicomSRValidator.log /tmp/dosesrfile.dcm.DicomSRValidator.log; \
	fi
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dciodvfy.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.dciodvfy.log ] ; \
	then \
		echo "Comparing DICOM encoding validation log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.dciodvfy.log /tmp/dosesrfile.dcm.dciodvfy.log; \
	fi

testphilipsonedcm_saveforcomparison:	testphilipsonedcm
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm
	cp /tmp/dosesrfile.dcm.build.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.build.log
	cp /tmp/dosesrfile.dcm.dcsrdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.dcsrdump.log
	cp /tmp/dosesrfile.dcm.dcdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.dcdump.log
	cp /tmp/dosesrfile.dcm.DicomSRValidator.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.DicomSRValidator.log
	cp /tmp/dosesrfile.dcm.dciodvfy.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testphilipsonedcm/dosesrfile.dcm.dciodvfy.log

testphilipsonedcmnoacq:	${OBJS}
	@echo "Building ..."
	@rm -f /tmp/dosesrfile.dcm
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.ExposureDoseSequence \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/philips_loc_noacquisitiontype_original.dcm \
		- /tmp/dosesrfile.dcm
	@echo "Dumping ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm

testphilipsonedcmtotalonly:	${OBJS}
	@echo "Building ..."
	@rm -f /tmp/dosesrfile.dcm
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.ExposureDoseSequence \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/philips_loc_secondarytotaldlponly_original.dcm \
		- /tmp/dosesrfile.dcm
	@echo "Dumping ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm

testocracquiredimagesphilipsdcmsroutput:	${OBJS}
	@rm -f /tmp/dosesrfile.dcm
	@echo "Doing testocracquiredimagesphilipsdcmsroutput ..."
	@echo "Building ..."
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.ExposureDoseSequence \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/RSNA_demo_2010/Dose/PixelMed/P0001/DICOM/I509 \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/RSNA_demo_2010/Dose/PixelMed/P0001/DICOMDIR \
		/tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.build.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.build.log ] ; \
	then \
		echo "Comparing build log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.build.log /tmp/dosesrfile.dcm.build.log; \
	fi
	@echo "Dumping SR ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcsrdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.dcsrdump.log ] ; \
	then \
		echo "Comparing dcsrdump log without content item identifiers ..."; \
		sed -e 's/>[0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old; \
		sed -e 's/>[0-9.]*: //' </tmp/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		echo "Comparing dcsrdump log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.dcsrdump.log /tmp/dosesrfile.dcm.dcsrdump.log; \
	fi
	@echo "Dumping ..."
	dcdump /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.dcdump.log ] ; \
	then \
		echo "Comparing dcdump log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.dcdump.log /tmp/dosesrfile.dcm.dcdump.log; \
	fi
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.DicomSRValidator.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.DicomSRValidator.log ] ; \
	then \
		echo "Comparing structure validation log without content item identifiers ..."; \
		sed -e 's/within [0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old; \
		sed -e 's/within [0-9.]*: //' </tmp/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		echo "Comparing structure validation log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.DicomSRValidator.log /tmp/dosesrfile.dcm.DicomSRValidator.log; \
	fi
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dciodvfy.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.dciodvfy.log ] ; \
	then \
		echo "Comparing DICOM encoding validation log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.dciodvfy.log /tmp/dosesrfile.dcm.dciodvfy.log; \
	fi

testocracquiredimagesphilipsdcmsroutput_saveforcomparison:	testocracquiredimagesphilipsdcmsroutput
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput
	cp /tmp/dosesrfile.dcm.build.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.build.log
	cp /tmp/dosesrfile.dcm.dcsrdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.dcsrdump.log
	cp /tmp/dosesrfile.dcm.dcdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.dcdump.log
	cp /tmp/dosesrfile.dcm.DicomSRValidator.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.DicomSRValidator.log
	cp /tmp/dosesrfile.dcm.dciodvfy.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagesphilipsdcmsroutput/dosesrfile.dcm.dciodvfy.log

testocrsiemensone:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/interesting501/CD0714122112/SAMPLE/EXSAMPLE_CT SCAN/DICOM/PA000000/ST000000/SE000008/IM000000" \
		- - \
		- - 0

testocrtrainsiemens:	${OBJS}
	cp OCR_Glyphs_DoseScreen.xml /tmp/testocr_all.xml
	for i in ${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/series501FilesCopied/*; \
	do \
		echo $$i; \
		java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR $$i - - /tmp/testocr_all.xml /tmp/testocr_all.xml; \
	done
	@echo "New glyph dictionary source is in /tmp/testocr_all.xml"

testocronesiemensdcmsroutput:	${OBJS}
	@rm -f /tmp/dosesrfile.dcm
	@echo "Building ..."
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/interesting501/CD0714122112/SAMPLE/EXSAMPLE_CT SCAN/DICOM/PA000000/ST000000/SE000008/IM000000" \
		- \
		/tmp/dosesrfile.dcm
	@echo "Dumping ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm

# RSNA_demo_2010/Dose/SIEMENS_DOSE is interesting because it a) has supplied IrradiationEventUIDs, b) it spans series, and c) it has different BodyPartExamined values within that single event that should be combined

testocracquiredimagessiemensdcmsroutputwithoutcompare:	${OBJS}
	@rm -f /tmp/dosesrfile.dcm
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/RSNA_demo_2010/Dose/SIEMENS_DOSE/DICOM/20101025/16490000/03383065 \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/RSNA_demo_2010/Dose/SIEMENS_DOSE/DICOMDIR \
		/tmp/dosesrfile.dcm - - 1

testocracquiredimagessiemensdcmsroutput:	${OBJS}
	@rm -f /tmp/dosesrfile.dcm
	@echo "Doing testocracquiredimagessiemensdcmsroutput ..."
	@echo "Building ..."
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/RSNA_demo_2010/Dose/SIEMENS_DOSE/DICOM/20101025/16490000/03383065 \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/RSNA_demo_2010/Dose/SIEMENS_DOSE/DICOMDIR \
		/tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.build.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.build.log ] ; \
	then \
		echo "Comparing build log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.build.log /tmp/dosesrfile.dcm.build.log; \
	fi
	@echo "Dumping SR ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcsrdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.dcsrdump.log ] ; \
	then \
		echo "Comparing dcsrdump log without content item identifiers ..."; \
		sed -e 's/>[0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old; \
		sed -e 's/>[0-9.]*: //' </tmp/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		echo "Comparing dcsrdump log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.dcsrdump.log /tmp/dosesrfile.dcm.dcsrdump.log; \
	fi
	@echo "Dumping ..."
	dcdump /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.dcdump.log ] ; \
	then \
		echo "Comparing dcdump log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.dcdump.log /tmp/dosesrfile.dcm.dcdump.log; \
	fi
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.DicomSRValidator.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.DicomSRValidator.log ] ; \
	then \
		echo "Comparing structure validation log without content item identifiers ..."; \
		sed -e 's/within [0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old; \
		sed -e 's/within [0-9.]*: //' </tmp/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		echo "Comparing structure validation log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.DicomSRValidator.log /tmp/dosesrfile.dcm.DicomSRValidator.log; \
	fi
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dciodvfy.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.dciodvfy.log ] ; \
	then \
		echo "Comparing DICOM encoding validation log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.dciodvfy.log /tmp/dosesrfile.dcm.dciodvfy.log; \
	fi

testocracquiredimagessiemensdcmsroutput_saveforcomparison:	testocracquiredimagessiemensdcmsroutput
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput
	cp /tmp/dosesrfile.dcm.build.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.build.log
	cp /tmp/dosesrfile.dcm.dcsrdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.dcsrdump.log
	cp /tmp/dosesrfile.dcm.dcdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.dcdump.log
	cp /tmp/dosesrfile.dcm.DicomSRValidator.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.DicomSRValidator.log
	cp /tmp/dosesrfile.dcm.dciodvfy.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagessiemensdcmsroutput/dosesrfile.dcm.dciodvfy.log

testrenderedreportsiemens:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.RenderedDoseReport \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/RSNA_demo_2010/Dose/SIEMENS_DOSE/DICOMDIR

testrenderedreportsiemensfulltext:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.RenderedDoseReport \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/RSNA_demo_2010/Dose/SIEMENS_DOSE/DICOMDIR FULL TEXT

testrenderedreportsiemensfullhtml:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.RenderedDoseReport \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/RSNA_demo_2010/Dose/SIEMENS_DOSE/DICOMDIR FULL HTML

testrenderedreportsiemenssummarytext:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.RenderedDoseReport \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/RSNA_demo_2010/Dose/SIEMENS_DOSE/DICOMDIR SUMMARY TEXT

testrenderedreportsiemenssummaryhtml:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.RenderedDoseReport \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/RSNA_demo_2010/Dose/SIEMENS_DOSE/DICOMDIR SUMMARY HTML

testgeneratesrsiemens:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.GenerateRadiationDoseStructuredReport \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/RSNA_demo_2010/Dose/SIEMENS_DOSE/DICOMDIR

testgeneratesrsiemenswritesr:	${OBJS}
	rm -f /tmp/crap.sr
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.GenerateRadiationDoseStructuredReport \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/RSNA_demo_2010/Dose/SIEMENS_DOSE/DICOMDIR \
		/tmp/crap.sr

testocrsiemensfindinseries503:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		- "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsWithSeries503/20111025"

testocrsiemensmultipage:	${OBJS}
	mkdir -p /tmp/testocrsiemensmultipage
	rm -f /tmp/testocrsiemensmultipage/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/SiemensAndToshiba_20150629/Siemens_MultiPage_PatternFix/TEST.CT.ABDOMEN_ABDOMENSEQ_(ADULT).0501.0001.2015.06.16.16.49.46.383611.20332726.IMAout.dcm" /tmp/testocrsiemensmultipage/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/SiemensAndToshiba_20150629/Siemens_MultiPage_PatternFix/TEST.CT.ABDOMEN_ABDOMENSEQ_(ADULT).0501.0002.2015.06.16.16.49.46.383611.20333168.IMAout.dcm" /tmp/testocrsiemensmultipage/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrsiemensmultipage \
		- - \
		- - 0

testocrtoshibafontAmultipage:	${OBJS}
	mkdir -p /tmp/testocrtoshibafontAmultipage
	rm -f /tmp/testocrtoshibafontAmultipage/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0110165942/DICOMDAT/SDY00000/SRS00002/IMG00000" /tmp/testocrtoshibafontAmultipage/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0110165942/DICOMDAT/SDY00000/SRS00002/IMG00001" /tmp/testocrtoshibafontAmultipage/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibafontAmultipage \
		- - \
		- - 0

testocrtoshibafontAmultipagesecondexam:	${OBJS}
	mkdir -p /tmp/testocrtoshibafontAmultipage
	rm -f /tmp/testocrtoshibafontAmultipage/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0110165942/DICOMDAT/SDY00001/SRS00002/IMG00000" /tmp/testocrtoshibafontAmultipage/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0110165942/DICOMDAT/SDY00001/SRS00002/IMG00001" /tmp/testocrtoshibafontAmultipage/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibafontAmultipage \
		- - \
		- - 0

testocrtoshibafontAallimagesmultipagefirstexam:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		- \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0110165942/DICOMDAT/SDY00000" \
		- \
		- - 0

testocrtoshibafontAallimagesmultipagesecondexam:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		- \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0110165942/DICOMDAT/SDY00001" \
		- \
		- - 0

testocrtoshibafontBmultipage:	${OBJS}
	mkdir -p /tmp/testocrtoshibafontAmultipage
	rm -f /tmp/testocrtoshibafontAmultipage/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0419151058/DICOM/STD00001/IMG00225" /tmp/testocrtoshibafontAmultipage/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0419151058/DICOM/STD00001/IMG00226" /tmp/testocrtoshibafontAmultipage/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibafontAmultipage \
		- - \
		- - 0

testocrtoshibafontBallimagesmultipagefirstexam:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		- \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0419151058/DICOM/STD00001" \
		- \
		- - 0

testocrtoshibaeffdlpandpositionmultipage:	${OBJS}
	mkdir -p /tmp/testocrtoshibafontAmultipage
	rm -f /tmp/testocrtoshibafontAmultipage/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0315105444/DICOMDAT/SDY00000/SRS00004/IMG00000" /tmp/testocrtoshibafontAmultipage/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0315105444/DICOMDAT/SDY00000/SRS00004/IMG00001" /tmp/testocrtoshibafontAmultipage/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibafontAmultipage \
		- - \
		- - 0

testocrtoshibaeffdlpandpositionallimagesmultipagefirstexam:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		- \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0315105444/DICOMDAT/SDY00000" \
		- \
		- - 0

testocrtoshibacardiac3multipage:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/DEFRANCE TARGET/111HD-EFA-MDDX3/1.2.392.200036.9116.2.6.1.48.1215708794.1271652635.953525" \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/DEFRANCE TARGET/111HD-EFA-MDDX3/" \
		- \
		- - 0

testocrtoshibacardiac4multipage:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/DEFRANCE TARGET/111HD-EFA-MDDX4/1.2.392.200036.9116.2.6.1.48.1215708794.1283827447.346078" \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/DEFRANCE TARGET/111HD-EFA-MDDX4/" \
		- \
		- - 0

testocrtoshibatwoprotocolmultipage:	${OBJS}
	mkdir -p /tmp/testocrtoshibafontAmultipage
	rm -f /tmp/testocrtoshibafontAmultipage/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0418125811/DICOM/1487" /tmp/testocrtoshibafontAmultipage/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0418125811/DICOM/1488" /tmp/testocrtoshibafontAmultipage/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibafontAmultipage \
		- - \
		- - 0
testocrtoshibaheadphantommultipage:	${OBJS}
	mkdir -p /tmp/testocrtoshibafontAmultipage
	rm -f /tmp/testocrtoshibafontAmultipage/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0418125811/DICOM/1182" /tmp/testocrtoshibafontAmultipage/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0418125811/DICOM/1183" /tmp/testocrtoshibafontAmultipage/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibafontAmultipage \
		- - \
		- - 0

testocrtoshibatotalnameswitheandmultilinedetailmultipage:	${OBJS}
	mkdir -p /tmp/testocrtoshibafontAmultipage
	rm -f /tmp/testocrtoshibafontAmultipage/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0330150950/DICOM/EXP00000/EXP0378" /tmp/testocrtoshibafontAmultipage/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0330150950/DICOM/EXP00000/EXP0379" /tmp/testocrtoshibafontAmultipage/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibafontAmultipage \
		- - \
		- - 0

testocrtoshibasinglepagetotaldlponly:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0119125747/DICOM/S00001/SER00004/I00001" \
		- - \
		- - 0

testocrtoshiba3xoneprotocolmultipage:	${OBJS}
	mkdir -p /tmp/testocrtoshibafontAmultipage
	rm -f /tmp/testocrtoshibafontAmultipage/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/ACR/Toshiba_Sample_Exams_ACR_DIR_20110711/3.x with 1 protocol/V3.35_0.dcm" /tmp/testocrtoshibafontAmultipage/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/ACR/Toshiba_Sample_Exams_ACR_DIR_20110711/3.x with 1 protocol/V3.35_1.dcm" /tmp/testocrtoshibafontAmultipage/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibafontAmultipage \
		- - \
		- - 0

testocrtoshiba3xtwoprotocolsmultipage:	${OBJS}
	mkdir -p /tmp/testocrtoshibafontAmultipage
	rm -f /tmp/testocrtoshibafontAmultipage/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/ACR/Toshiba_Sample_Exams_ACR_DIR_20110711/3.x with 2 protocols/V3.35_2_0.dcm" /tmp/testocrtoshibafontAmultipage/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/ACR/Toshiba_Sample_Exams_ACR_DIR_20110711/3.x with 2 protocols/V3.35_2_1.dcm" /tmp/testocrtoshibafontAmultipage/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibafontAmultipage \
		- - \
		- - 0

testocrtoshiba4xmultipage:	${OBJS}
	mkdir -p /tmp/testocrtoshibafontAmultipage
	rm -f /tmp/testocrtoshibafontAmultipage/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/ACR/Toshiba_Sample_Exams_ACR_DIR_20110711/4x/SC.1.2.392.200036.9116.2.5.1.23590.168125963.1310116185.893403" /tmp/testocrtoshibafontAmultipage/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/ACR/Toshiba_Sample_Exams_ACR_DIR_20110711/4x/SC.1.2.392.200036.9116.2.5.1.23590.168125963.1310116185.925207" /tmp/testocrtoshibafontAmultipage/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibafontAmultipage \
		- - \
		- - 0

testocrtoshiba4xwithemultipage:	${OBJS}
	mkdir -p /tmp/testocrtoshibafontAmultipage
	rm -f /tmp/testocrtoshibafontAmultipage/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/ACR/Toshiba_Sample_Exams_ACR_DIR_20110711/4x with e/SC.1.2.392.200036.9116.2.5.1.23590.168125963.1310117287.419960" /tmp/testocrtoshibafontAmultipage/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/ACR/Toshiba_Sample_Exams_ACR_DIR_20110711/4x with e/SC.1.2.392.200036.9116.2.5.1.23590.168125963.1310117287.472355" /tmp/testocrtoshibafontAmultipage/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true  \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.doseocr.OCR=info \
		com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibafontAmultipage \
		- - \
		- -

testocrtoshibaheadandbodyphantomnepageonly:	${OBJS}
	mkdir -p /tmp/testocrtoshibaheadandbodyphantomnepageonly
	rm -f /tmp/testocrtoshibaheadandbodyphantomnepageonly/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/HeadAndBody/HeadAndBodyOnePageOnly/2_Venous__Phase_CE_1000/IM-0007-0001.dcm" /tmp/testocrtoshibaheadandbodyphantomnepageonly/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibaheadandbodyphantomnepageonly \
		- - \
		- - 0

testocrtoshibaheadandbodyphantommultipage:	${OBJS}
	mkdir -p /tmp/testocrtoshibaheadandbodyphantommultipage
	rm -f /tmp/testocrtoshibaheadandbodyphantommultipage/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCollected/cdsToshibaDoseScreensAndAllImages/CD0526090412/DICOM/ST00001/SE00008/IM00001" /tmp/testocrtoshibaheadandbodyphantommultipage/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCollected/cdsToshibaDoseScreensAndAllImages/CD0526090412/DICOM/ST00001/SE00008/IM00002" /tmp/testocrtoshibaheadandbodyphantommultipage/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibaheadandbodyphantommultipage \
		- - \
		- - 0

testocrtoshibadosereductionline:	${OBJS}
	mkdir -p /tmp/testocrtoshibadosereductionline
	rm -f /tmp/testocrtoshibadosereductionline/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Warden/toshiba-dose-screens/3A" /tmp/testocrtoshibadosereductionline/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Warden/toshiba-dose-screens/3B" /tmp/testocrtoshibadosereductionline/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibadosereductionline \
		- - \
		- - 0

testocrtoshibafivelines:	${OBJS}
	mkdir -p /tmp/testocrtoshibafivelines
	rm -f /tmp/testocrtoshibafivelines/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Warden/toshiba-dose-screens/2A" /tmp/testocrtoshibafivelines/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Warden/toshiba-dose-screens/2B" /tmp/testocrtoshibafivelines/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Warden/toshiba-dose-screens/2C" /tmp/testocrtoshibafivelines/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Warden/toshiba-dose-screens/2D" /tmp/testocrtoshibafivelines/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibafivelines \
		- - \
		- - 0

testocrtoshibaonelinewithsd:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCollected/cdsToshibaDoseScreensAndAllImages/CD0429113631/DICOM/FFA4837C/00B90328/6D3E629C" \
		- - \
		- - 0

testocrtoshibaonelinewithsdandmean:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCollected/cdsToshibaDoseScreensAndAllImages/CD0809152803/DICOM/04BB581D/030B91A0/89B959B6" \
		- - \
		- - 0

testocrtoshibaonelinewitheinctdivolanddlp:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCollected/cdsToshibaDoseScreensAndAllImages/CD0803134837/DICOMDAT/SDY00000/SRS00006" \
		- - \
		- - 0

testocrtoshibadifferentordersingleline:	${OBJS}
	mkdir -p /tmp/testocrtoshibadifferentordersingleline
	rm -f /tmp/testocrtoshibadifferentordersingleline/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCollected/cdsToshibaDoseScreensAndAllImages/CD0429150527/I1BZZHYX" /tmp/testocrtoshibadifferentordersingleline/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCollected/cdsToshibaDoseScreensAndAllImages/CD0429150527/NF2LX5G9" /tmp/testocrtoshibadifferentordersingleline/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibadifferentordersingleline \
		- - \
		- - 0

testocrtoshibaexposuretimeicharactersplit:	${OBJS}
	mkdir -p /tmp/testocrtoshibaexposuretimeicharactersplit
	rm -f /tmp/testocrtoshibaexposuretimeicharactersplit/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCollected/cdsToshibaDoseScreensAndAllImages/CD0523151323/IMAGES/IM000064" /tmp/testocrtoshibaexposuretimeicharactersplit/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCollected/cdsToshibaDoseScreensAndAllImages/CD0523151323/IMAGES/IM000065" /tmp/testocrtoshibaexposuretimeicharactersplit/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibaexposuretimeicharactersplit \
		- - \
		- - 0

testocrtoshibaonelineexposuretimectdivoldlpsdwithvolumescantype:	${OBJS}
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCollected/cdsToshibaDoseScreensAndAllImages/CD0524115601/DICOM/ST000000/SE000001" \
		- - \
		- - 0

testocrtoshibamultipagectdivoldlpsdonly:	${OBJS}
	mkdir -p /tmp/testocrtoshibamultipagectdivoldlpsdonly
	rm -f /tmp/testocrtoshibamultipagectdivoldlpsdonly/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/SiemensAndToshiba_20150629/Toshiba_MultiPage_FlowFix/87648962_O_2out.dcm" /tmp/testocrtoshibamultipagectdivoldlpsdonly/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/SiemensAndToshiba_20150629/Toshiba_MultiPage_FlowFix/87648963_O_2out.dcm" /tmp/testocrtoshibamultipagectdivoldlpsdonly/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibamultipagectdivoldlpsdonly \
		- - \
		- - 0

testocrtoshibaanothermultipage_bodyonly:	${OBJS}
	mkdir -p /tmp/testocrtoshibaanothermultipage_bodyonly
	rm -f /tmp/testocrtoshibaanothermultipage_bodyonly/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161209_Body/toshiba_sample/DICOM/I0" /tmp/testocrtoshibaanothermultipage_bodyonly/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161209_Body/toshiba_sample/DICOM/I1" /tmp/testocrtoshibaanothermultipage_bodyonly/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.doseocr.OCR=info \
		com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibaanothermultipage_bodyonly \
		- - \
		- -

testocrtoshibaanothermultipage_bodyonly2:	${OBJS}
	mkdir -p /tmp/testocrtoshibaanothermultipage_bodyonly2
	rm -f /tmp/testocrtoshibaanothermultipage_bodyonly2/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161209_Body/export/DICOM\\I2" /tmp/testocrtoshibaanothermultipage_bodyonly2/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161209_Body/export/DICOM\\I3" /tmp/testocrtoshibaanothermultipage_bodyonly2/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.doseocr.OCR=info \
		com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibaanothermultipage_bodyonly2 \
		- - \
		- -

testocrtoshibaanothermultipage_bodyonly3:	${OBJS}
	mkdir -p /tmp/testocrtoshibaanothermultipage_bodyonly3
	rm -f /tmp/testocrtoshibaanothermultipage_bodyonly3/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161209_Body/export/DICOM\\I4" /tmp/testocrtoshibaanothermultipage_bodyonly3/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161209_Body/export/DICOM\\I5" /tmp/testocrtoshibaanothermultipage_bodyonly3/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.doseocr.OCR=info \
		com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibaanothermultipage_bodyonly3 \
		- - \
		- -

testocrtoshibaanothermultipage_bodyonly4:	${OBJS}
	mkdir -p /tmp/testocrtoshibaanothermultipage_bodyonly4
	rm -f /tmp/testocrtoshibaanothermultipage_bodyonly4/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161209_Body/export/DICOM\\I6" /tmp/testocrtoshibaanothermultipage_bodyonly4/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161209_Body/export/DICOM\\I7" /tmp/testocrtoshibaanothermultipage_bodyonly4/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161209_Body/export/DICOM\\I8" /tmp/testocrtoshibaanothermultipage_bodyonly4/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.doseocr.OCR=info \
		com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibaanothermultipage_bodyonly4 \
		- - \
		- -

testocrtoshibaanothermultipage_bodyonly5:	${OBJS}
	mkdir -p /tmp/testocrtoshibaanothermultipage_bodyonly5
	rm -f /tmp/testocrtoshibaanothermultipage_bodyonly5/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161209_Body/export/DICOM\\I9" /tmp/testocrtoshibaanothermultipage_bodyonly5/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161209_Body/export/DICOM\\I10" /tmp/testocrtoshibaanothermultipage_bodyonly5/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.doseocr.OCR=info \
		com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibaanothermultipage_bodyonly5 \
		- - \
		- -

testocrtoshibaanothermultipage_headonly:	${OBJS}
	mkdir -p /tmp/testocrtoshibaanothermultipage_headonly
	rm -f /tmp/testocrtoshibaanothermultipage_headonly/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161213_Head/export/DICOM\\I0" /tmp/testocrtoshibaanothermultipage_headonly/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161213_Head/export/DICOM\\I1" /tmp/testocrtoshibaanothermultipage_headonly/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.doseocr.OCR=info \
		com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibaanothermultipage_headonly \
		- - \
		- -

testocrtoshibaanothermultipage_headonly2:	${OBJS}
	mkdir -p /tmp/testocrtoshibaanothermultipage_headonly2
	rm -f /tmp/testocrtoshibaanothermultipage_headonly2/*
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161213_Head/export/DICOM\\I2" /tmp/testocrtoshibaanothermultipage_headonly2/
	cp "${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/Bagyura_Toshiba_20161213_Head/export/DICOM\\I3" /tmp/testocrtoshibaanothermultipage_headonly2/
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.doseocr.OCR=info \
		com.pixelmed.doseocr.OCR \
		/tmp/testocrtoshibaanothermultipage_headonly2 \
		- - \
		- -

testocracquiredimagestoshibadcmsroutputnophantom:	${OBJS}
	@rm -f /tmp/dosesrfile.dcm
	@echo "Doing testocracquiredimagestoshibadcmsroutputnophantom ..."
	@echo "Building ..."
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		- \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/IOImages/cdsToshibaDoseScreensAndAllImages/CD0315105444/DICOMDAT/SDY00000" \
		/tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.build.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.build.log ] ; \
	then \
		echo "Comparing build log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.build.log /tmp/dosesrfile.dcm.build.log; \
	fi
	@echo "Dumping SR ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcsrdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.dcsrdump.log ] ; \
	then \
		echo "Comparing dcsrdump log without content item identifiers ..."; \
		sed -e 's/>[0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old; \
		sed -e 's/>[0-9.]*: //' </tmp/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		echo "Comparing dcsrdump log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.dcsrdump.log /tmp/dosesrfile.dcm.dcsrdump.log; \
	fi
	@echo "Dumping ..."
	dcdump /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.dcdump.log ] ; \
	then \
		echo "Comparing dcdump log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.dcdump.log /tmp/dosesrfile.dcm.dcdump.log; \
	fi
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.DicomSRValidator.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.DicomSRValidator.log ] ; \
	then \
		echo "Comparing structure validation log without content item identifiers ..."; \
		sed -e 's/within [0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old; \
		sed -e 's/within [0-9.]*: //' </tmp/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		echo "Comparing structure validation log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.DicomSRValidator.log /tmp/dosesrfile.dcm.DicomSRValidator.log; \
	fi
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dciodvfy.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.dciodvfy.log ] ; \
	then \
		echo "Comparing DICOM encoding validation log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.dciodvfy.log /tmp/dosesrfile.dcm.dciodvfy.log; \
	fi

testocracquiredimagestoshibadcmsroutputnophantom_saveforcomparison:	testocracquiredimagestoshibadcmsroutputnophantom
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutput
	cp /tmp/dosesrfile.dcm.build.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.build.log
	cp /tmp/dosesrfile.dcm.dcsrdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.dcsrdump.log
	cp /tmp/dosesrfile.dcm.dcdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.dcdump.log
	cp /tmp/dosesrfile.dcm.DicomSRValidator.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.DicomSRValidator.log
	cp /tmp/dosesrfile.dcm.dciodvfy.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputnophantom/dosesrfile.dcm.dciodvfy.log

testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly:	${OBJS}
	@rm -f /tmp/dosesrfile.dcm
	@echo "Doing testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly ..."
	@echo "Building ..."
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.OCR \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/HeadAndBody/HeadAndBodyOnePageOnly/2_Venous__Phase_CE_1000/IM-0007-0001.dcm" \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/Toshiba/HeadAndBody/HeadAndBodyOnePageOnly" \
		/tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.build.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.build.log ] ; \
	then \
		echo "Comparing build log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.build.log /tmp/dosesrfile.dcm.build.log; \
	fi
	@echo "Dumping SR ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcsrdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.dcsrdump.log ] ; \
	then \
		echo "Comparing dcsrdump log without content item identifiers ..."; \
		sed -e 's/>[0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old; \
		sed -e 's/>[0-9.]*: //' </tmp/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		echo "Comparing dcsrdump log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.dcsrdump.log /tmp/dosesrfile.dcm.dcsrdump.log; \
	fi
	@echo "Dumping ..."
	dcdump /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.dcdump.log ] ; \
	then \
		echo "Comparing dcdump log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.dcdump.log /tmp/dosesrfile.dcm.dcdump.log; \
	fi
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.DicomSRValidator.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.DicomSRValidator.log ] ; \
	then \
		echo "Comparing structure validation log without content item identifiers ..."; \
		sed -e 's/within [0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old; \
		sed -e 's/within [0-9.]*: //' </tmp/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		echo "Comparing structure validation log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.DicomSRValidator.log /tmp/dosesrfile.dcm.DicomSRValidator.log; \
	fi
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dciodvfy.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.dciodvfy.log ] ; \
	then \
		echo "Comparing DICOM encoding validation log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.dciodvfy.log /tmp/dosesrfile.dcm.dciodvfy.log; \
	fi

testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly_saveforcomparison:	testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly
	cp /tmp/dosesrfile.dcm.build.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.build.log
	cp /tmp/dosesrfile.dcm.dcsrdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.dcsrdump.log
	cp /tmp/dosesrfile.dcm.dcdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.dcdump.log
	cp /tmp/dosesrfile.dcm.DicomSRValidator.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.DicomSRValidator.log
	cp /tmp/dosesrfile.dcm.dciodvfy.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly/dosesrfile.dcm.dciodvfy.log

testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage:	${OBJS}
	@rm -f /tmp/dosesrfile.dcm
	@echo "Doing testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage ..."
	@echo "Building ..."
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.doseocr.OCR=info \
		com.pixelmed.doseocr.OCR \
		- \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImagesCollected/cdsToshibaDoseScreensAndAllImages/CD0526090412/DICOM/ST00001" \
		/tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.build.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.build.log ] ; \
	then \
		echo "Comparing build log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.build.log /tmp/dosesrfile.dcm.build.log; \
	fi
	@echo "Dumping SR ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcsrdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.dcsrdump.log ] ; \
	then \
		echo "Comparing dcsrdump log without content item identifiers ..."; \
		sed -e 's/>[0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old; \
		sed -e 's/>[0-9.]*: //' </tmp/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		echo "Comparing dcsrdump log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.dcsrdump.log /tmp/dosesrfile.dcm.dcsrdump.log; \
	fi
	@echo "Dumping ..."
	dcdump /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.dcdump.log ] ; \
	then \
		echo "Comparing dcdump log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.dcdump.log /tmp/dosesrfile.dcm.dcdump.log; \
	fi
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.DicomSRValidator.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.DicomSRValidator.log ] ; \
	then \
		echo "Comparing structure validation log without content item identifiers ..."; \
		sed -e 's/within [0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old; \
		sed -e 's/within [0-9.]*: //' </tmp/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		echo "Comparing structure validation log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.DicomSRValidator.log /tmp/dosesrfile.dcm.DicomSRValidator.log; \
	fi
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dciodvfy.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.dciodvfy.log ] ; \
	then \
		echo "Comparing DICOM encoding validation log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.dciodvfy.log /tmp/dosesrfile.dcm.dciodvfy.log; \
	fi

testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage_saveforcomparison:	testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage
	cp /tmp/dosesrfile.dcm.build.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.build.log
	cp /tmp/dosesrfile.dcm.dcsrdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.dcsrdump.log
	cp /tmp/dosesrfile.dcm.dcdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.dcdump.log
	cp /tmp/dosesrfile.dcm.DicomSRValidator.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.DicomSRValidator.log
	cp /tmp/dosesrfile.dcm.dciodvfy.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage/dosesrfile.dcm.dciodvfy.log

testgeexposuredosesequenceandcommentsonradiationdose:	${OBJS}
	@echo "Doing testgeexposuredosesequenceandcommentsonradiationdose ..."
	@echo "Building ..."
	@rm -f /tmp/dosesrfile.dcm
	java -Xmx512m -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar -Djava.awt.headless=true com.pixelmed.doseocr.ExposureDoseSequence \
		${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/DoseImages/GEAndToshibaFromSiemens_20150724/GE_ImageNotReading-DLP-from-CommentsOnRadiationDose/B_6.18.51.0.177644517668903683009262397252112201442715.dcm \
		- /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.build.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.build.log ] ; \
	then \
		echo "Comparing build log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.build.log /tmp/dosesrfile.dcm.build.log; \
	fi
	@echo "Dumping SR ..."
	dcsrdump -identifier /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcsrdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.dcsrdump.log ] ; \
	then \
		echo "Comparing dcsrdump log without content item identifiers ..."; \
		sed -e 's/>[0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old; \
		sed -e 's/>[0-9.]*: //' </tmp/dosesrfile.dcm.dcsrdump.log >/tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_old /tmp/dosesrfile.dcm.dcsrdump.log_withoutidentifiers_new; \
		echo "Comparing dcsrdump log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.dcsrdump.log /tmp/dosesrfile.dcm.dcsrdump.log; \
	fi
	@echo "Dumping ..."
	dcdump /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dcdump.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.dcdump.log ] ; \
	then \
		echo "Comparing dcdump log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.dcdump.log /tmp/dosesrfile.dcm.dcdump.log; \
	fi
	@echo "Validating structure ..."
	java -Xmx2g -Xss4m -XX:-UseGCOverheadLimit -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.validate.DicomSRValidator /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.DicomSRValidator.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.DicomSRValidator.log ] ; \
	then \
		echo "Comparing structure validation log without content item identifiers ..."; \
		sed -e 's/within [0-9.]*: //' <${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old; \
		sed -e 's/within [0-9.]*: //' </tmp/dosesrfile.dcm.DicomSRValidator.log >/tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		diff -a /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_old /tmp/dosesrfile.dcm.DicomSRValidator.log_withoutidentifiers_new; \
		echo "Comparing structure validation log with content item identifiers ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.DicomSRValidator.log /tmp/dosesrfile.dcm.DicomSRValidator.log; \
	fi
	@echo "Validating DICOM encoding ..."
	dciodvfy /tmp/dosesrfile.dcm >/tmp/dosesrfile.dcm.dciodvfy.log 2>&1
	@if [ -f ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.dciodvfy.log ] ; \
	then \
		echo "Comparing DICOM encoding validation log  ..."; \
		diff -a ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.dciodvfy.log /tmp/dosesrfile.dcm.dciodvfy.log; \
	fi

testgeexposuredosesequenceandcommentsonradiationdose_saveforcomparison:	testgeexposuredosesequenceandcommentsonradiationdose
	mkdir -p ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose
	cp /tmp/dosesrfile.dcm.build.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.build.log
	cp /tmp/dosesrfile.dcm.dcsrdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.dcsrdump.log
	cp /tmp/dosesrfile.dcm.dcdump.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.dcdump.log
	cp /tmp/dosesrfile.dcm.DicomSRValidator.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.DicomSRValidator.log
	cp /tmp/dosesrfile.dcm.dciodvfy.log ${PATHTOROOT}/${PATHTOTESTRESULTSFROMROOT}/testgeexposuredosesequenceandcommentsonradiationdose/dosesrfile.dcm.dciodvfy.log

testocrtoshibamultipages:	\
	testocrtoshibafontAmultipage \
	testocrtoshibafontBmultipage \
	testocrtoshibaeffdlpandpositionmultipage \
	testocrtoshibacardiac3multipage \
	testocrtoshibacardiac4multipage \
	testocrtoshibatwoprotocolmultipage \
	testocrtoshibaheadphantommultipage \
	testocrtoshibatotalnameswitheandmultilinedetailmultipage \
	testocrtoshibasinglepagetotaldlponly \
	testocrtoshiba3xoneprotocolmultipage \
	testocrtoshiba3xtwoprotocolsmultipage \
	testocrtoshiba4xmultipage \
	testocrtoshiba4xwithemultipage \
	testocrtoshibaheadandbodyphantomnepageonly \
	testocrtoshibaheadandbodyphantommultipage \
	testocrtoshibadosereductionline \
	testocrtoshibafivelines \
	testocrtoshibaonelinewithsd \
	testocrtoshibaonelinewithsdandmean \
	testocrtoshibaonelinewitheinctdivolanddlp \
	testocrtoshibadifferentordersingleline \
	testocrtoshibaexposuretimeicharactersplit \
	testocrtoshibaonelineexposuretimectdivoldlpsdwithvolumescantype \
	testocrtoshibamultipagectdivoldlpsdonly \
	testocrtoshibaanothermultipage_bodyonly \
	testocrtoshibaanothermultipage_bodyonly2 \
	testocrtoshibaanothermultipage_bodyonly3 \
	testocrtoshibaanothermultipage_bodyonly4 \
	testocrtoshibaanothermultipage_bodyonly5 \
	testocrtoshibaanothermultipage_headonly \
	testocrtoshibaanothermultipage_headonly2

testocrservice:	LegacyRadiationDoseOCRDicomForwardingService.class
	java -Xmx256m -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar \
			-server -Djava.awt.headless=true \
			com.pixelmed.doseocr.LegacyRadiationDoseOCRDicomForwardingService \
			4012 OCRANDFORWARD \
			localhost 4011 PACS \
			1 0

testallocrsrdiff:	testocracquiredimagessiemensdcmsroutput \
					testphilipsonedcm \
					testocracquiredimagesphilipsdcmsroutput \
					testocracquiredimagesfindscreen \
					testocrgenoacquiredimagesexposuredosesequenceinscreen \
					testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition \
					testocrgeacquiredimagesandexposuredosesequence \
					testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity \
					testocracquiredimagestoshibadcmsroutputnophantom \
					testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly \
					testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage \
					testgeexposuredosesequenceandcommentsonradiationdose

testallocrsrdiff_saveforcomparison:	\
					testocracquiredimagessiemensdcmsroutput_saveforcomparison \
					testphilipsonedcm_saveforcomparison \
					testocracquiredimagesphilipsdcmsroutput_saveforcomparison \
					testocracquiredimagesfindscreen_saveforcomparison \
					testocrgenoacquiredimagesexposuredosesequenceinscreen_saveforcomparison \
					testocrgeacquiredimagesbodyandheaddifferentphantomsamestudyheaddosechanginginsameacquisition_saveforcomparison \
					testocrgeacquiredimagesandexposuredosesequence_saveforcomparison \
					testocrgescreenandexposuredosesequencecardiachelicalstationaryaxialambiguity_saveforcomparison \
					testocracquiredimagestoshibadcmsroutputnophantom_saveforcomparison \
					testocracquiredimagestoshibadcmsroutputheadandbodyphantomonepageonly_saveforcomparison \
					testocracquiredimagestoshibadcmsroutputheadandbodyphantommultipage_saveforcomparison \
					testgeexposuredosesequenceandcommentsonradiationdose_saveforcomparison


