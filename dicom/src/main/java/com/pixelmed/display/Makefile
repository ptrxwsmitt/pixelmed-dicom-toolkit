PATHTOROOT = ../../..

OBJS =	TestApp.class \
	TestAppMultiFile.class \
	TestAppMultiFrame.class \
	TestAppMultiFrameStackMode.class \
	TestAppMultiFrameCompare.class \
	DicomBrowser.class \
	ApplicationFrame.class \
	SingleImagePanel.class \
	SingleImagePanelWithRegionDetection.class \
	SingleImagePanelWithRegionDrawing.class \
	SingleImagePanelWithLineDrawing.class \
	SourceImage.class \
	SourceImageSubset.class \
	SourceImageSortOrderPanel.class \
	SourceImageVOILUTSelectorPanel.class \
	SourceImageWindowLinearCalculationSelectorPanel.class \
	SourceImageWindowingAccelerationSelectorPanel.class \
	SourceInstanceSortOrderPanel.class \
	SourceSpectra.class \
	SourceSpectrumSortOrderPanel.class \
	SpectraPanel.class \
	DisplayDicomDirectoryBrowser.class \
	DisplayStructuredReportBrowser.class \
	TextAnnotation.class \
	TextAnnotationPositioned.class \
	DicomImageViewer.class \
	PlotGraph.class \
	StatusBarManager.class \
	WindowCenterAndWidth.class \
	DemographicAndTechniqueAnnotations.class \
	OrientationAnnotations.class \
	ConsumerFormatImageMaker.class \
	BufferedImageUtilities.class \
	LocalizerManager.class \
	ImageLocalizerManager.class \
	SpectroscopyLocalizerManager.class \
	MammoImageViewer.class \
	MammoDemographicAndTechniqueAnnotations.class \
	ChestImageViewer.class \
	DisplayDeviceArea.class \
	DisplayedAreaSelection.class \
	ImageEditUtilities.class \
	DicomImageBlackout.class \
	DescribeGraphicsEnvironment.class \
	DialogMessageLogger.class \
	DrawingUtilities.class \
	DicomCleaner.class \
	IconListBrowser.class \
	IconDemographicAndTechniqueAnnotations.class \
	SourceImageShutterSelectorPanel.class \
	SuperimposedImage.class \
	SuperimposedDicomImage.class \
	SuperimposedDicomSegments.class \
	SafeFileChooser.class \
	SafeProgressBarUpdaterThread.class \
	SafeCursorChanger.class \
	TiledSourceImageSortOrderPanel.class

TRANSFORMEDPROPERTIES = \
	DicomCleaner_cs.properties \
	DicomCleaner_da.properties \
	DicomCleaner_de.properties \
	DicomCleaner_el.properties \
	DicomCleaner_es.properties \
	DicomCleaner_fi.properties \
	DicomCleaner_fr.properties \
	DicomCleaner_hu.properties \
	DicomCleaner_it.properties \
	DicomCleaner_ja.properties \
	DicomCleaner_ko.properties \
	DicomCleaner_nl.properties \
	DicomCleaner_no.properties \
	DicomCleaner_pl.properties \
	DicomCleaner_pt.properties \
	DicomCleaner_ro.properties \
	DicomCleaner_ru.properties \
	DicomCleaner_sv.properties \
	DicomCleaner_tr.properties \
	DicomCleaner_zh_CN.properties \
	DicomImageBlackout_cs.properties \
	DicomImageBlackout_da.properties \
	DicomImageBlackout_de.properties \
	DicomImageBlackout_el.properties \
	DicomImageBlackout_es.properties \
	DicomImageBlackout_fi.properties \
	DicomImageBlackout_fr.properties \
	DicomImageBlackout_hu.properties \
	DicomImageBlackout_it.properties \
	DicomImageBlackout_ja.properties \
	DicomImageBlackout_ko.properties \
	DicomImageBlackout_nl.properties \
	DicomImageBlackout_no.properties \
	DicomImageBlackout_pl.properties \
	DicomImageBlackout_pt.properties \
	DicomImageBlackout_ro.properties \
	DicomImageBlackout_ru.properties \
	DicomImageBlackout_sv.properties \
	DicomImageBlackout_tr.properties \
	DicomImageBlackout_zh_CN.properties \
	SafeFileChooser_cs.properties \
	SafeFileChooser_da.properties \
	SafeFileChooser_el.properties \
	SafeFileChooser_fi.properties \
	SafeFileChooser_hu.properties \
	SafeFileChooser_nl.properties \
	SafeFileChooser_no.properties \
	SafeFileChooser_pl.properties \
	SafeFileChooser_pt.properties \
	SafeFileChooser_ro.properties \
	SafeFileChooser_ru.properties \
	SafeFileChooser_tr.properties \
	ApplicationFrame_cs.properties \
	ApplicationFrame_da.properties \
	ApplicationFrame_el.properties \
	ApplicationFrame_fi.properties \
	ApplicationFrame_hu.properties \
	ApplicationFrame_nl.properties \
	ApplicationFrame_no.properties \
	ApplicationFrame_pl.properties \
	ApplicationFrame_pt.properties \
	ApplicationFrame_ro.properties \
	ApplicationFrame_ru.properties \
	ApplicationFrame_tr.properties

all:	${OBJS} ${TRANSFORMEDPROPERTIES}

include ${PATHTOROOT}/Makefile.common.mk

# override javac command in Makefile.common.mk to add codec to classpath, until folded into main trunk :(
.java.class:
	export JAVAVERSIONTARGETJARFILE=`/usr/libexec/java_home -v ${JAVAVERSIONTARGET} | tail -1`/jre/lib/rt.jar; javac ${JAVACOPTIONS} \
		-classpath ${PATHTOROOT}:${SLF4JCOMPILEADDITIONALJAR}:${DISPLAYADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} \
		-sourcepath ${PATHTOROOT} $<

TEST_512_16DCM = ${PATHTODCTOOLSUPPORT}/images/dicom/forjpeg2000/discimg/MR1
TEST_512_16RAW = /tmp/crap.512.16.raw
TEST_512_16JPG = /tmp/crap.512.16.jpg
TEST_512_16BMP = /tmp/crap.512.16.bmp
TEST_512_16J2K = /tmp/crap.512.16.j2k
TEST_512_16GIF = /tmp/crap.512.16.gif
TEST_512_16PNG = /tmp/crap.512.16.png
TEST_512_16TIF = /tmp/crap.512.16.tif
TEST_512_16PNM = /tmp/crap.512.16.pnm

TEST_BIG_16DCM = ${PATHTODCTOOLSUPPORT}/images/dicom/forjpeg2000/discimg/MG1
TEST_BIG_16RAW = /tmp/crap.big.16.raw

TEST_512_8DCM = /tmp/crap.512.8.dcm
TEST_512_8RAW = /tmp/crap.512.8.raw
TEST_512_8JPG = /tmp/crap.512.8.jpg

TEST_RGB_DCM = /tmp/crap.rgb.dcm
TEST_RGB_JPG = /tmp/crap.rgb.jpg

clean:
	(cd event; make clean)
	rm -f *~ *.class core *.bak \
		${TRANSFORMEDPROPERTIES} \
		${TEST_512_8DCM} \
		${TEST_512_16RAW} ${TEST_BIG_16RAW} ${TEST_512_8RAW} \
		${PATHTODCTOOLSUPPORT}/images/dicom/sr/mytests/imgforsr \
		${PATHTODCTOOLSUPPORT}/images/dicom/sr/mytests/testscoord.dcm

archive:
	tar -cvf - Makefile *.java | gzip -best > ${PATHTOROOT}/display.`date '+%Y%m%d'`.tar.gz

test_512_8d1:	TestApp.class ${TEST_512_8DCM}
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.TestApp ${TEST_512_8DCM} 1 1

test_512_8d:	TestApp.class ${TEST_512_8DCM}
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.TestApp ${TEST_512_8DCM} 4 2

test_512_8r1:	TestApp.class ${TEST_512_8RAW}
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.TestApp ${TEST_512_8RAW} 512 512 8 1 1

test_512_8r:	TestApp.class ${TEST_512_8RAW}
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.TestApp ${TEST_512_8RAW} 512 512 8 2 6

${TEST_512_8DCM}:
	dcsmpte > $@

${TEST_512_8RAW}:	${TEST_512_8DCM}
	dctoraw $? $@

test_512_16d1:	TestApp.class ${TEST_512_16DCM}
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.TestApp ${TEST_512_16DCM} 1 1

test_512_16d:	TestApp.class ${TEST_512_16DCM}
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.TestApp ${TEST_512_16DCM} 2 6

test_512_16:	TestApp.class ${TEST_512_16RAW}
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.TestApp ${TEST_512_16RAW} 512 512 16 2 6

${TEST_512_16RAW}:	${TEST_512_16DCM}
	dctoraw $? $@

test_big_16d:	TestApp.class ${TEST_BIG_16DCM}
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.TestApp ${TEST_BIG_16DCM} 1 1

test_big_16:	TestApp.class ${TEST_BIG_16RAW}
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.TestApp ${TEST_BIG_16RAW} 3064 4664 16 1 1

${TEST_BIG_16RAW}:	${TEST_BIG_16DCM}
	dctoraw $? $@

test_512_8_single:	SingleImagePanel.class ${TEST_512_8DCM}
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.SingleImagePanel ${TEST_512_8DCM}


testbrowser:	DicomBrowser.class
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.DicomBrowser

testdir:	DicomBrowser.class \
				${PATHTODCTOOLSUPPORT}/images/dicom/forjpeg2000/discimg/DICOMDIR
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.DicomBrowser ${PATHTODCTOOLSUPPORT}/images/dicom/forjpeg2000/discimg/DICOMDIR

testcddir:	DicomBrowser.class \
				/mnt/cdrom/dicomdir
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.DicomBrowser /mnt/cdrom/dicomdir

${PATHTODCTOOLSUPPORT}/images/dicom/sr/mytests/testscoord.dcm:	${PATHTODCTOOLSUPPORT}/images/dicom/sr/mytests/testscoord.source
	ancreate < $< > $@.tmp
	dccp $@.tmp $@ -stamp 9999
	rm -f $@.tmp

${PATHTODCTOOLSUPPORT}/images/dicom/sr/mytests/imgforsr:
	dcsmpte > $@ -r PatientName "Homer^Jane^^^" -r PatientID 234567 \
		-r StudyID 345678 -r StudyDate 19991029 \
		-r StudyTime 154500 -r SeriesNumber 2 -r InstanceNumber 1 \
		-r StudyInstanceUID 1.2.3.4.5.6.7.100 \
		-r SeriesInstanceUID 1.2.3.4.5.6.7.400 \
		-r SOPInstanceUID 1.3.6.1.4.1.5962.1.1.17998.2.1.1001278501.11736.8323328 \
		-r AccessionNumber 123456 \
		-stamp 9999

${PATHTODCTOOLSUPPORT}/images/dicom/sr/mytests/DICOMDIR:	\
				${PATHTODCTOOLSUPPORT}/images/dicom/sr/mytests/testscoord.dcm \
				${PATHTODCTOOLSUPPORT}/images/dicom/sr/mytests/imgforsr
	(cd ${PATHTODCTOOLSUPPORT}/images/dicom/sr/mytests; dcdirmk -novalidatename testscoord.dcm imgforsr >DICOMDIR)

testsrdir:	DicomBrowser.class \
				${PATHTODCTOOLSUPPORT}/images/dicom/sr/mytests/DICOMDIR
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.DicomBrowser ${PATHTODCTOOLSUPPORT}/images/dicom/sr/mytests/DICOMDIR

testosdir:	DicomBrowser.class \
				${PATHTODCTOOLSUPPORT}/images/dicom/sr/osiris_work/DICOMDIR
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.DicomBrowser ${PATHTODCTOOLSUPPORT}/images/dicom/sr/osiris_work/DICOMDIR

testos2dir:	DicomBrowser.class \
				${PATHTODCTOOLSUPPORT}/images/dicom/sr/osiris_work2/DICOMDIR
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}  com.pixelmed.display.DicomBrowser ${PATHTODCTOOLSUPPORT}/images/dicom/sr/osiris_work2/DICOMDIR

testnemamfmr:	DicomImageViewer.class
	mkdir -p $${HOME}/.com.pixelmed.display.DicomImageViewer.images
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS}  \
		com.pixelmed.display.DicomImageViewer ${PATHTOROOT}/../../nemamfmr/DISCIMG/DICOMDIR

testnemamfmr.clean:
	rm -rf $${HOME}/.com.pixelmed.display.*

testnemamfct:	DicomImageViewer.class
	mkdir -p $${HOME}/.com.pixelmed.display.DicomImageViewer.images
	java -Xms512m -Xmx1024m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS}  \
		com.pixelmed.display.DicomImageViewer ${PATHTOROOT}/../../nemamfct/DISCIMG/DICOMDIR

testnemamfct.clean:
	rm -rf $${HOME}/.com.pixelmed.display.*

testplotgraph:
	rm -f ~/snap*.jpg; for i in 0 1 2 3 4 5 6 7; do echo "frame $$i"; java -classpath ${PATHTOROOT} com.pixelmed.display.PlotGraph ~/work/nemamfmr/srcimages/ucsfspectra/spectra/spectra/t2256_cor_sum.cmplx 512 12 12 $$i; mv ~/snap*.jpg frame_$$i.jpg; done

testspectrum.dcm:	Makefile
	rm -f /tmp/crap.empty
	touch /tmp/crap.empty
	${PATHTODCTOOLS}/appsrc/dcfile/dccp /tmp/crap.empty testspectrum.dcm \
		-r NumberOfFrames 8 -r Rows 12 -r Columns 12 -r DataPointRows 1 -r DataPointColumns 512 \
		-r DataRepresentation COMPLEX -r SignalDomainColumns FREQUENCY \
		-r PatientID 1234 -r StudyID 1234 -r SeriesNumber 1 -r InstanceNumber 1 \
		-r Modality MR -r SOPClassUID "1.2.840.10008.5.1.4.1.1.4.2" \
		-nodisclaimer -output-vr explicit -output-endian big
	echo "(0x5600,0x0020) OF Spectroscopy Data VR=<OF> VL=<0x480000> []" | ${PATHTODCTOOLS}/appsrc/acrnema/ancreate -e -b >> testspectrum.dcm
	cat ${PATHTOROOT}/../../nemamfmr/srcimages/ucsfspectra/spectra/spectra/t2256_cor_sum.cmplx >> testspectrum.dcm
	rm /tmp/crap.empty
	
testviewer:	DicomImageViewer.class
	if [ ! -L .com.pixelmed.display.DicomImageViewer.images ]; then mkdir -p $${HOME}/.com.pixelmed.display.DicomImageViewer.images; fi
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS} \
		com.pixelmed.display.DicomImageViewer

testviewerdebugdisplaynoslf:	DicomImageViewer.class
	if [ ! -L .com.pixelmed.display.DicomImageViewer.images ]; then mkdir -p $${HOME}/.com.pixelmed.display.DicomImageViewer.images; fi
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS} \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.display.WindowCenterAndWidth=debug -Dorg.slf4j.simpleLogger.log.com.pixelmed.display.SourceImage=debug -Dorg.slf4j.simpleLogger.log.com.pixelmed.display.SingleImagePanel=debug \
		com.pixelmed.display.DicomImageViewer

testviewerdebugdisplayslfsimple:	DicomImageViewer.class
	if [ ! -L .com.pixelmed.display.DicomImageViewer.images ]; then mkdir -p $${HOME}/.com.pixelmed.display.DicomImageViewer.images; fi
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${SLF4JSIMPLEADDITIONALJAR}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS} \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.display.WindowCenterAndWidth=debug -Dorg.slf4j.simpleLogger.log.com.pixelmed.display.SourceImage=debug -Dorg.slf4j.simpleLogger.log.com.pixelmed.display.SingleImagePanel=debug \
		com.pixelmed.display.DicomImageViewer

testviewerlimitedmemory:	DicomImageViewer.class
	if [ ! -L .com.pixelmed.display.DicomImageViewer.images ]; then mkdir -p $${HOME}/.com.pixelmed.display.DicomImageViewer.images; fi
	java -Xms128m -Xmx128m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS} \
		com.pixelmed.display.DicomImageViewer

testviewerquaqua:	DicomImageViewer.class
	if [ ! -L .com.pixelmed.display.DicomImageViewer.images ]; then mkdir -p $${HOME}/.com.pixelmed.display.DicomImageViewer.images; fi
	java -Xms512m -Xmx512m -Dswing.defaultlaf=ch.randelshofer.quaqua.QuaquaLookAndFeel -Djava.library.path=${PATHTOADDITIONAL} -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR}:${PATHTOADDITIONAL}/quaqua.jar ${XALANJAVAOPTS}  \
		com.pixelmed.display.DicomImageViewer

testnemamfmrspectrum:	DicomImageViewer.class testspectrum.dcm
	mkdir -p $${HOME}/.com.pixelmed.display.DicomImageViewer.images
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS}  \
		com.pixelmed.display.DicomImageViewer testspectrum.dcm

testcharsets:	DicomImageViewer.class
	mkdir -p $${HOME}/.com.pixelmed.display.DicomImageViewer.images
	java -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS} \
		com.pixelmed.display.DicomImageViewer $${HOME}/Pictures/Medical/charsettests/DICOMDIR

testnema97:	DicomImageViewer.class
	mkdir -p $${HOME}/.com.pixelmed.display.DicomImageViewer.images
	java -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS} \
		com.pixelmed.display.DicomImageViewer $${HOME}/Pictures/Medical/NEMA97CD/DICOMDIR

testviewerontestimages:	
	(for i in `cat ${PATHTOROOT}/${PATHTODCTOOLSUPPORTFROMROOT}/images/dicom/testlist`; do echo "$$i"; java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS} com.pixelmed.display.DicomImageViewer "${PATHTOROOT}/${PATHTODCTOOLSUPPORTFROMROOT}/images/dicom/$$i"; done)

testsingleimagepanelontestimages:
	(for i in `cat ${PATHTOROOT}/${PATHTODCTOOLSUPPORTFROMROOT}/images/dicom/testlist`; do echo "$$i"; java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS} com.pixelmed.display.SingleImagePanel "${PATHTOROOT}/${PATHTODCTOOLSUPPORTFROMROOT}/images/dicom/$$i"; done)

testviewerontestimagesubset:
	(for i in `cat /tmp/testlist`; do echo "$$i"; java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS} com.pixelmed.display.DicomImageViewer "${PATHTOROOT}/${PATHTODCTOOLSUPPORTFROMROOT}/images/dicom/$$i"; done)

testvieweronallmfmrimages:
	(for i in ${PATHTOROOT}/../../nemamfmr/DISCIMG/IMAGES/*; do echo "$$i"; java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS} com.pixelmed.display.DicomImageViewer "$$i"; done)

testvieweronallmfctimages:	
	(for i in ${PATHTOROOT}/../../nemamfct/DISCIMG/IMAGES/*; do echo "$$i"; java -Xms512m -Xmx1024m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS} com.pixelmed.display.DicomImageViewer "$$i"; done)
	
testconsumer8:	${TEST_512_8DCM} ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker ${TEST_512_8DCM} ${TEST_512_8JPG} jpeg 127.5 256 384 384 50 all

testconsumer8_subregion:	${TEST_512_8DCM} ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker ${TEST_512_8DCM} /tmp/crap.512.8.subregion.jpg jpeg 127.5 256 384 384 50 all 127 127 256 256

testconsumer16:	${TEST_512_16DCM} ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker ${TEST_512_16DCM} ${TEST_512_16JPG} jpeg 600 1600 384 384 50 all

testconsumer16bmp:	${TEST_512_16DCM} ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker ${TEST_512_16DCM} ${TEST_512_16BMP} bmp

testconsumer16j2k:	${TEST_512_16DCM} ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker ${TEST_512_16DCM} ${TEST_512_16J2K} jpeg2000

testconsumer16gif:	${TEST_512_16DCM} ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker ${TEST_512_16DCM} ${TEST_512_16GIF} gif

testconsumer16png:	${TEST_512_16DCM} ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker ${TEST_512_16DCM} ${TEST_512_16PNG} png

testconsumer16tif:	${TEST_512_16DCM} ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker ${TEST_512_16DCM} ${TEST_512_16TIF} tiff

testconsumer16pnm:	${TEST_512_16DCM} ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker ${TEST_512_16DCM} ${TEST_512_16PNM} pnm

testconsumernegativewindow:	${TEST_512_16DCM} ConsumerFormatImageMaker.class
	rm -f /tmp/crap.jpg
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker \
		"$${HOME}/work/dctool.support/images/dicom/rt/varian_portal_rtimage_negativewindow_tinyrescaleslope_curvedata.dcm" \
		/tmp/crap.jpg jpeg 1.28 0.8 0 0 50 all

testconsumermfoverridesharedwindow:	ConsumerFormatImageMaker.class
	rm -rf /tmp/MultiframeForRendering
	(cd /tmp; bzcat </Volumes/Toshiba5TEnc/Pictures/Medical/QIBA_CT_1B/QIBA_1B_RadPharm_RoundOne_MultiframeForRendering.tar.bz2 | tar -xvf - MultiframeForRendering/series103mf.dcm)
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker \
		/tmp/MultiframeForRendering/series103mf.dcm \
		/tmp/testconsumermfoverridesharedwindow.jpg \
		jpeg 127.5 256 384 384 50 all
	rm -r /tmp/MultiframeForRendering

testconsumermfoverrideperframewindow:	ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker \
		$${HOME}/Pictures/Medical/EnhancedMultiframe/Philips_Multiframe_MR/IM_0022 \
		/tmp/testconsumermfoverrideperframewindow.jpg \
		jpeg 127.5 256 384 384 50 all

testconsumermfdefaultwindow:	ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker \
		$${HOME}/Pictures/Medical/EnhancedMultiframe/Philips_Multiframe_MR/IM_0022 \
		/tmp/testconsumermfdefaultwindow.jpg \
		jpeg 0 0 384 384 50 all

testconsumermfdefaultwindowsizequalitynoannotations:	ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker \
		$${HOME}/Pictures/Medical/EnhancedMultiframe/Philips_Multiframe_MR/IM_0022 \
		/tmp/testconsumermfdefaultwindow.jpg \
		jpeg 0 0 0 0 -1 none

testconsumermfnoarguments:	ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker \
		$${HOME}/Pictures/Medical/EnhancedMultiframe/Philips_Multiframe_MR/IM_0022 \
		/tmp/testconsumermfdefaultwindow.jpg

testconsumermfoneselectedframe:	ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker \
		$${HOME}/Pictures/Medical/EnhancedMultiframe/Philips_Multiframe_MR/IM_0022 \
		/tmp/testconsumermfoneselectedframe.jpg \
		jpeg 0 0 384 384 50 all_color 0 0 0 0 100 100

testconsumermftenselectedframes:	ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker \
		$${HOME}/Pictures/Medical/EnhancedMultiframe/Philips_Multiframe_MR/IM_0022 \
		/tmp/testconsumermfoneselectedframe.jpg \
		jpeg 0 0 384 384 50 all_color 0 0 0 0 100 110

testconsumerybrfull:	ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker \
		$${HOME}/Pictures/Medical/Color/YBRFULLBYPLANE/ybrfullbyplanecleaned.dcm \
		/tmp/testconsumerybrfull.jpg \
		jpeg

testconsumerybrfullwithwindow:	ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker \
		$${HOME}/Pictures/Medical/Color/YBRFULLBYPLANE/ybrfullbyplanecleaned.dcm \
		/tmp/testconsumerybrfullwithwindow.jpg \
		jpeg 50 128

testconsumerybrfull422:	ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker \
		$${HOME}/Pictures/Medical/Color/YBR_FULL_422_Uncompressed/574C2EB2_cleaned \
		/tmp/testconsumerybrfull422.jpg \
		jpeg

${TEST_RGB_DCM}:
	java -Djava.awt.headless=true -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.dicom.ImageToDicom \
		${PATHTOROOT}/../../experiments/davwork.jpg \
		${TEST_RGB_DCM} \
		"Smith^John" "P001" "1234" "001" "027"

testconsumerrgb_subregion:	${TEST_RGB_DCM} ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker ${TEST_RGB_DCM} ${TEST_RGB_JPG} jpeg 0 0 0 0 50 none 120 120 100 100

testconsumerrgb_noannotationsatall:	${TEST_RGB_DCM} ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker ${TEST_RGB_DCM} ${TEST_RGB_JPG} jpeg 0 0 0 0 50 none

testconsumerrgb_nocolorannotations:	${TEST_RGB_DCM} ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker ${TEST_RGB_DCM} ${TEST_RGB_JPG} jpeg 0 0 0 0 50 all

testconsumerrgb_colorannotations:	${TEST_RGB_DCM} ConsumerFormatImageMaker.class
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker ${TEST_RGB_DCM} ${TEST_RGB_JPG} jpeg 0 0 0 0 50 all_color

testconsumermfrlenoarguments:	ConsumerFormatImageMaker.class
	cp "$${HOME}/Pictures/Medical/RLE/S3U_000E" /tmp/crap.dcm # MF 8 bit PALETTE COLOR
	java -Xms512m -Xmx512m -Djava.awt.headless=true -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ConsumerFormatImageMaker \
		/tmp/crap.dcm \
		/tmp/testconsumermfrledefaults.jpg

testmammohome:
	if [ ! -L testmammohome ] ; then if [ -d $${HOME}/Pictures/Medical/ihemammotests/IHETESTS ] ; then ln -s $${HOME}/Pictures/Medical/ihemammotests/IHETESTS testmammohome; elif [ -d /Volumes/SEAGATE4/IHEMammo ] ; then ln -s /Volumes/SEAGATE4/IHEMammo/server1.radiology.nmh.org/IHETESTS testmammohome; elif [ -d /Volumes/IHETESTS ] ; then ln -s /Volumes/IHETESTS testmammohome; elif [ -d /mnt/windows/h/IHEMammo ] ; then ln -s /mnt/windows/h/IHEMammo/server1.radiology.nmh.org/IHETESTS testmammohome; fi ; fi

testmammo1:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.display.MammoImageViewer=debug \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/001001/CURLCC \
		testmammohome/IMAGES/001001/CURLMLO \
		testmammohome/IMAGES/001001/CURRCC \
		testmammohome/IMAGES/001001/CURRMLO \
		testmammohome/IMAGES/001001/PRILCC \
		testmammohome/IMAGES/001001/PRILMLO \
		testmammohome/IMAGES/001001/PRIRCC \
		testmammohome/IMAGES/001001/PRIRMLO

testmammo1curpriorsamesidesameview:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/001001/CURLCC \
		testmammohome/IMAGES/001001/PRILCC

testmammo1samedatesameviewdifferentside:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/001001/CURLMLO \
		testmammohome/IMAGES/001001/CURRMLO

testmammo2:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/001002/CURLCC \
		testmammohome/IMAGES/001002/CURLMLO \
		testmammohome/IMAGES/001002/CURRCC \
		testmammohome/IMAGES/001002/CURRMLO \
		testmammohome/IMAGES/001002/PRILCC \
		testmammohome/IMAGES/001002/PRILMLO \
		testmammohome/IMAGES/001002/PRIRCC \
		testmammohome/IMAGES/001002/PRIRMLO

testmammo3:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/001003/CURLCC \
		testmammohome/IMAGES/001003/CURLMLO \
		testmammohome/IMAGES/001003/CURRCC \
		testmammohome/IMAGES/001003/CURRMLO \
		testmammohome/IMAGES/001003/PRILCC \
		testmammohome/IMAGES/001003/PRILMLO \
		testmammohome/IMAGES/001003/PRIRCC \
		testmammohome/IMAGES/001003/PRIRMLO

testmammo4:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/001004/CURLCC \
		testmammohome/IMAGES/001004/CURLMLO \
		testmammohome/IMAGES/001004/CURRCC \
		testmammohome/IMAGES/001004/CURRMLO \
		testmammohome/IMAGES/001004/PRILCC \
		testmammohome/IMAGES/001004/PRILMLO \
		testmammohome/IMAGES/001004/PRIRCC \
		testmammohome/IMAGES/001004/PRIRMLO

testmammo5:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/001005/CURLCC \
		testmammohome/IMAGES/001005/CURLMLO \
		testmammohome/IMAGES/001005/CURRCC \
		testmammohome/IMAGES/001005/CURRMLO \
		testmammohome/IMAGES/001005/PRILCC \
		testmammohome/IMAGES/001005/PRILMLO \
		testmammohome/IMAGES/001005/PRIRCC \
		testmammohome/IMAGES/001005/PRIRMLO

testmammo6:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/001006/CURLCC \
		testmammohome/IMAGES/001006/CURLMLO \
		testmammohome/IMAGES/001006/CURRCC \
		testmammohome/IMAGES/001006/CURRMLO \
		testmammohome/IMAGES/001006/PRILCC \
		testmammohome/IMAGES/001006/PRILMLO \
		testmammohome/IMAGES/001006/PRIRCC \
		testmammohome/IMAGES/001006/PRIRMLO

testmammoburnlut1:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/009001/VOILUTL1 \
		testmammohome/IMAGES/009001/VOILUTBU

testmammoburnlut2:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/009002/VOILUTSC \
		testmammohome/IMAGES/009002/VOILUTBU

testmammoburnlut3:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/009003/VOILUTSC \
		testmammohome/IMAGES/009003/VOILUTBU

testmammosigmoidfunc:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/009004/LUTSHPSG

testmammolinearfunc:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/009005/LUTSHPLN

testmammopixpad1:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/011001/PIXPADLO \
		testmammohome/IMAGES/011001/PIXPADHI

testmammodiffmatrix:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/012001/RMLO \
		testmammohome/IMAGES/012001/RCC \
		testmammohome/IMAGES/012001/LMLO \
		testmammohome/IMAGES/012001/LCC

testmammoruler:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/004001/RMLO \
		testmammohome/IMAGES/004001/RCC \
		testmammohome/IMAGES/004001/LMLO \
		testmammohome/IMAGES/004001/LCC

testmammospotmagan:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		testmammohome/IMAGES/007001/SPOTMGAN

testmammofuji:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		"testmammohome/../VendorSampleImages/Fuji/mg-ForPres/WithMetaHeader/Case2/1.2.392.200036.9125.4.0.34601962.1089736960.440607579" \
		"testmammohome/../VendorSampleImages/Fuji/mg-ForPres/WithMetaHeader/Case2/1.2.392.200036.9125.4.0.34601962.1173623040.440607579" \
		"testmammohome/../VendorSampleImages/Fuji/mg-ForPres/WithMetaHeader/Case2/1.2.392.200036.9125.4.0.34601962.1257509120.440607579" \
		"testmammohome/../VendorSampleImages/Fuji/mg-ForPres/WithMetaHeader/Case2/1.2.392.200036.9125.4.0.34601962.1341395200.440607579"

testmammohologic1:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		"testmammohome/../VendorSampleImages/Hologic/Hologic Lorad Lorad 4view 2004 0122 raw and processed [26221]/20040122 103234 [2]/Series 001 [MG - ]/1.2.840.113681.2200408748.773.3252222433.61.1.dcm" \
		"testmammohome/../VendorSampleImages/Hologic/Hologic Lorad Lorad 4view 2004 0122 raw and processed [26221]/20040122 103234 [2]/Series 002 [MG - ]/1.2.840.113681.2200408748.773.3252222433.65.1.dcm" \
		"testmammohome/../VendorSampleImages/Hologic/Hologic Lorad Lorad 4view 2004 0122 raw and processed [26221]/20040122 103234 [2]/Series 004 [MG - ]/1.2.840.113681.2200408748.773.3252222433.59.1.dcm" \
		"testmammohome/../VendorSampleImages/Hologic/Hologic Lorad Lorad 4view 2004 0122 raw and processed [26221]/20040122 103234 [2]/Series 005 [MG - ]/1.2.840.113681.2200408748.773.3252222433.63.1.dcm"

testmammohologic2:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		"testmammohome/../VendorSampleImages/Hologic/Hologic Lorad Lorad 4view 2005 0804 raw and processed [27127]/20050804 154500 [2]/Series 001 [MG - ]/1.2.840.113681.2203808218.755.3300604934.416.1.dcm" \
		"testmammohome/../VendorSampleImages/Hologic/Hologic Lorad Lorad 4view 2005 0804 raw and processed [27127]/20050804 154500 [2]/Series 002 [MG - ]/1.2.840.113681.2203808218.755.3300604934.418.1.dcm" \
		"testmammohome/../VendorSampleImages/Hologic/Hologic Lorad Lorad 4view 2005 0804 raw and processed [27127]/20050804 154500 [2]/Series 005 [MG - ]/1.2.840.113681.2203808218.755.3300604934.414.1.dcm" \
		"testmammohome/../VendorSampleImages/Hologic/Hologic Lorad Lorad 4view 2005 0804 raw and processed [27127]/20050804 154500 [2]/Series 009 [MG - ]/1.2.840.113681.2203808218.755.3300604934.420.1.dcm"

testmammosectra:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		"testmammohome/../VendorSampleImages/Sectra/19470311002520050526/19470311002520050526/im_1/i0000,0000b.dcm" \
		"testmammohome/../VendorSampleImages/Sectra/19470311002520050526/19470311002520050526/im_2/i0000,0000b.dcm" \
		"testmammohome/../VendorSampleImages/Sectra/19470311002520050526/19470311002520050526/im_3/i0000,0000b.dcm" \
		"testmammohome/../VendorSampleImages/Sectra/19470311002520050526/19470311002520050526/im_4/i0000,0000b.dcm"

testmammofischermag:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		"testmammohome/../VendorSampleImages/ExtraViews/Fischer R, L LMs [2063]/20040206 132524 [2]/Series 001 [MG - ]/1.2.840.113725.2.2.2200635504.1076099241.dcm"

testmammohologicmag:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		"testmammohome/../VendorSampleImages/ExtraViews/Selenia R,L LMs [3651]/20051209 101500 [2]/Series 002 [MG - ]/1.2.840.113681.2203808218.754.3311581779.333.1.dcm"

testmammocad:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		"testmammohome/IMAGES/013001/RMLO" \
		"testmammohome/IMAGES/013001/LMLO" \
		"testmammohome/IMAGES/013001/CAD" \
		"testmammohome/IMAGES/013001/RCC" \
		"testmammohome/IMAGES/013001/LCC"

testmammocad2:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		"testmammohome/IMAGES/013002/RMLO" \
		"testmammohome/IMAGES/013002/LMLO" \
		"testmammohome/IMAGES/013002/CAD" \
		"testmammohome/IMAGES/013002/RCC" \
		"testmammohome/IMAGES/013002/LCC"

testmammocad3:	MammoImageViewer.class testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		"testmammohome/IMAGES/013003/RMLO" \
		"testmammohome/IMAGES/013003/LMLO" \
		"testmammohome/IMAGES/013003/CAD" \
		"testmammohome/IMAGES/013003/RCC" \
		"testmammohome/IMAGES/013003/LCC"

testmammowithsmpte:	MammoImageViewer.class ${TEST_512_8DCM}
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		${TEST_512_8DCM} ${TEST_512_8DCM} ${TEST_512_8DCM} ${TEST_512_8DCM} ${TEST_512_8DCM} ${TEST_512_8DCM} ${TEST_512_8DCM} ${TEST_512_8DCM}

testbrowsesrmammocad:	testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.dicom.StructuredReportBrowser \
		"testmammohome/IMAGES/013001/CAD"

testbrowsedicommammocad:	testmammohome
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.dicom.StandaloneAttributeTreeBrowser \
		"testmammohome/IMAGES/013001/CAD"

testmammospacing:	MammoImageViewer.class
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer $${HOME}/Work/pixelspacingtestimages/DISCIMG/IMAGES/MGIMAGE

testmammodbt:	MammoImageViewer.class
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.MammoImageViewer \
		$${HOME}/Documents/Medical/stuff/medical.nema.org/Dicom/DataSets/WG15/BreastTomoImage/GE_BreastTomo_2014_07/GE-Breast_Tomo-Example-0.5mm-2014_07/p136/e148/s2433/i11428.MGDC.2 \
		$${HOME}/Documents/Medical/stuff/medical.nema.org/Dicom/DataSets/WG15/BreastTomoImage/GE_BreastTomo_2014_07/GE-Breast_Tomo-Example-0.5mm-2014_07/p136/e148/s2434/i11430.MGDC.2 \
		$${HOME}/Documents/Medical/stuff/medical.nema.org/Dicom/DataSets/WG15/BreastTomoImage/GE_BreastTomo_2014_07/GE-Breast_Tomo-Example-0.5mm-2014_07/p136/e148/s2435/i11432.MGDC.2 \
		$${HOME}/Documents/Medical/stuff/medical.nema.org/Dicom/DataSets/WG15/BreastTomoImage/GE_BreastTomo_2014_07/GE-Breast_Tomo-Example-0.5mm-2014_07/p136/e148/s2436/i11434.MGDC.2

testblackoutwithfiles:	DicomImageBlackout.class ${TEST_512_8DCM}
	dcsmpte /tmp/testblackoutwithfiles1 -rows 512 -columns 512
	dcsmpte /tmp/testblackoutwithfiles2 -bits 12 -rows 400 -columns 768
	dcsmpte /tmp/testblackoutwithfiles3 -bits 16 -rows 5000 -columns 5000	# large enough to trigger leaving on disk
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.display.DicomImageBlackout \
		/tmp/testblackoutwithfiles1 \
		/tmp/testblackoutwithfiles1 \
		/tmp/testblackoutwithfiles2 \
		/tmp/testblackoutwithfiles2 \
		/tmp/testblackoutwithfiles1 \
		/tmp/testblackoutwithfiles3 \
		/tmp/testblackoutwithfiles1
	rm -f /tmp/testblackoutwithfiles1 /tmp/testblackoutwithfiles2 /tmp/testblackoutwithfiles3

testblackoutwithlossyjpegfiles:	DicomImageBlackout.class
	rm -f /tmp/crap.dcm
	cp "$${HOME}/Pictures/Medical/LossyJPEGFragmented/test_n_fragments.dcm" /tmp/crap.dcm
	dcfile /tmp/crap.dcm
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.display.DicomImageBlackout \
		/tmp/crap.dcm
	rm /tmp/crap.dcm

testblackoutwithlossyjpegfilesblockredact:	DicomImageBlackout.class
	rm -f /tmp/crap.dcm
	cp "$${HOME}/Pictures/Medical/LossyJPEGFragmented/test_n_fragments.dcm" /tmp/crap.dcm
	dcfile /tmp/crap.dcm
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${JPEGBLOCKREDACTIONJAR} com.pixelmed.display.DicomImageBlackout \
		/tmp/crap.dcm
	rm /tmp/crap.dcm

testblackoutwithlosslessjpegfiles:	DicomImageBlackout.class
	rm -f /tmp/crap.dcm
	#cp "$${HOME}/Pictures/Medical/JPEGLossless/eightbitrgbsingleframe.dcm" /tmp/crap.dcm	# SF 8 bit RGB
	#cp "$${HOME}/Pictures/Medical/JPEGLossless/BadWithPVRGCodecButOKWithOther/000caecb.dcm" /tmp/crap.dcm	# SF 12 bit MONOCHROME2
	#cp "$${HOME}/Pictures/Medical/JPEGLossless/EightBitJIIOCodecProblem/seq0" /tmp/crap.dcm	# MF 8 bit MONOCHROME2
	cp "$${HOME}/Pictures/Medical/JPEGLossless/ivus_thousandsofframes_losslessjpeg.dcm" /tmp/crap.dcm	# MF++ 8 bit MONOCHROME2
	dcfile /tmp/crap.dcm
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomImageBlackout \
		/tmp/crap.dcm
	rm /tmp/crap.dcm

testblackoutwithrlefiles:	DicomImageBlackout.class
	rm -f /tmp/crap.dcm
	#cp "$${HOME}/Pictures/Medical/RLE/CU7WJ28E" /tmp/crap.dcm	# MF 8 bit RGB
	#cp "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG12/GE_VingMed/GEMS_IMG/2001_DEC/11/6M163410/2929J686" /tmp/crap.dcm	# MF 8 bit YBR_FULL
	#cp "$${HOME}/Pictures/Medical/RLE/0020.DCM" /tmp/crap.dcm	# MF 8 bit PALETTE COLOR
	#cp "$${HOME}/Pictures/Medical/RLE/view0002.dcm" /tmp/crap.dcm # SF 8 bit YBR_FULL
	#cp "$${HOME}/Pictures/Medical/RLE/rleybrfull8_badoddsegmentoffset.dcm" /tmp/crap.dcm # SF 8 bit YBR_FULL
	#cp "$${HOME}/Pictures/Medical/RLE/09DKNID3_anon_badoddsegmentoffsetin8bitrle.dcm" /tmp/crap.dcm # SF 8 bit YBR_FULL illegal odd offset
	#cp "$${HOME}/Pictures/Medical/RLE/CX50_DemoImages_PedEcho/PC1X00/PC1XXX00" /tmp/crap.dcm # MF 8 bit PALETTE COLOR
	#cp "$${HOME}/Pictures/Medical/RLE/US16BitsPaletteColorRLE.dcm" /tmp/crap.dcm # MF 16 bit PALETTE COLOR
	cp "$${HOME}/Pictures/Medical/RLE/S3U_000E" /tmp/crap.dcm # MF 8 bit PALETTE COLOR
	dcfile /tmp/crap.dcm
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${JPEGBLOCKREDACTIONJAR} com.pixelmed.display.DicomImageBlackout \
		/tmp/crap.dcm
	rm /tmp/crap.dcm

testblackoutwithj2kfiles:	DicomImageBlackout.class
	rm -f /tmp/crap.dcm
	#cp "$${HOME}/Pictures/Medical/JPEG2000Various/FromDaveHarvey/anon/JPEG2000/SC-COL-RGB-24-bit.90" /tmp/crap.dcm
	cp "$${HOME}/Pictures/Medical/JPEG2000Various/FromDaveHarvey/anon/JPEG2000/CT-MONO-16-bit-signed.91-real" /tmp/crap.dcm
	dcfile /tmp/crap.dcm
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${JIIOADDITIONALJARS} com.pixelmed.display.DicomImageBlackout \
		/tmp/crap.dcm
	rm /tmp/crap.dcm

testblackoutwithchooser:	DicomImageBlackout.class
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${JPEGBLOCKREDACTIONJAR} com.pixelmed.display.DicomImageBlackout

testblackoutsamesizewindowclearingproblem:	DicomImageBlackout.class
	cp $${HOME}/Work/imagesamples/COMPTESTS/DISCIMG/IMAGES/REF/RG1_UNC /tmp/crap1
	cp $${HOME}/Work/imagesamples/COMPTESTS/DISCIMG/IMAGES/REF/NM1_UNC /tmp/crap2
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.display.DicomImageBlackout \
		/tmp/crap1 /tmp/crap2
	rm -f /tmp/crap1 /tmp/crap2

testdescribedisplay:		DescribeGraphicsEnvironment.class
	java -cp ${PATHTOROOT} com.pixelmed.display.DescribeGraphicsEnvironment
	
testchest:	ChestImageViewer.class
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ChestImageViewer \
		$${HOME}/Work/dctool.support/images/dicom/forjpeg2000/discimg/RG1 \
		$${HOME}/Work/dctool.support/images/dicom/forjpeg2000/discimg/RG1

testchest1:	ChestImageViewer.class
	dccp -nodisclaimer -r PatientID "123-45-6789" -r PatientName "Smith^John" "$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1002210022/2/38083/0.dcm" /tmp/chestview.dcm
	dccp -nodisclaimer -r PatientID "ILO-33-RR"   -r PatientName "Reference Set^Profusion 3/3 Shape-Size r/r" -r Manufacturer "ILO" "$${HOME}/Pictures/Medical/NIOSH/NIOSH-ILO-CR (D_)/050503/20030505/RG/RG155540009.dcm" /tmp/chestref.dcm
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ChestImageViewer \
		/tmp/chestview.dcm /tmp/chestref.dcm
	rm -rf /tmp/chestview.dcm /tmp/chestref.dcm

testchest2:	ChestImageViewer.class
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ChestImageViewer \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000010000/2/1001/1001.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000110001/2/330009/1009.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000210002/2/1001/1001.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000310003/2/1001/1001.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000410004/2/1/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000510005/2/35534/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000610006/2/8393/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000710007/2/40553/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000810008/2/17676/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000910009/2/969/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1001010010/2/7282/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1001110011/2/1/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1001210012/2/1/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1001310013/2/1001/1001.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1001410014/2/1/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1001510015/2/1/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1001610016/2/1/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1001710017/2/2/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1001810018/2/6602/0.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1001910019/2/19685/0.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1002010020/2/19599/0.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1002110021/2/17324/0.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1002210022/2/38083/0.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1002310023/2/45254/0.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1002410024/2/4441/0.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1002510025/2/86446/0.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1002610026/2/2/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1002710027/2/1/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1002810028/2/1/2.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1002910029/2/1/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1003010030/2/1/1.dcm

testchest3:	ChestImageViewer.class
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ChestImageViewer \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000110001/2/330009/1009.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000310003/2/1001/1001.dcm

testchest4:	ChestImageViewer.class
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ChestImageViewer \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1001110011/2/1/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000510005/2/35534/1.dcm

testchest5:	ChestImageViewer.class
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ChestImageViewer \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000010000/2/1001/1001.dcm
	
testchest6:	ChestImageViewer.class
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.ChestImageViewer \
		$${HOME}/Desktop/Downloads/DISCIMG/IMAGES/DXIMAGE
	
testsinglepanel:	SingleImagePanel.class
	dcsmpte /tmp/crap.dcm
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.SingleImagePanel /tmp/crap.dcm
	rm /tmp/crap.dcm

testcleaner:	DicomCleaner.class
	java -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

# can also use -Duser.country=JP, then if no ja_JP suffix file falls back to using just _ja

testcleanerczech:	DicomCleaner.class DicomCleaner_cs.properties DicomImageBlackout_cs.properties SafeFileChooser_cs.properties ApplicationFrame_cs.properties
	java -Duser.language=cs -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerdannish:	DicomCleaner.class DicomCleaner_da.properties DicomImageBlackout_da.properties SafeFileChooser_da.properties ApplicationFrame_da.properties
	java -Duser.language=da -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanergerman:	DicomCleaner.class DicomCleaner_de.properties DicomImageBlackout_de.properties
	java -Duser.language=de -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanergreek:	DicomCleaner.class DicomCleaner_el.properties DicomImageBlackout_el.properties SafeFileChooser_el.properties ApplicationFrame_el.properties
	java -Duser.language=el -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerspanish:	DicomCleaner.class DicomCleaner_es.properties DicomImageBlackout_es.properties
	java -Duser.language=es -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerfinnish:	DicomCleaner.class DicomCleaner_fi.properties DicomImageBlackout_fi.properties SafeFileChooser_fi.properties ApplicationFrame_fi.properties
	java -Duser.language=fi -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerfrench:	DicomCleaner.class DicomCleaner_fr.properties DicomImageBlackout_fr.properties
	java -Duser.language=fr -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerhungarian:	DicomCleaner.class DicomCleaner_hu.properties DicomImageBlackout_hu.properties SafeFileChooser_hu.properties ApplicationFrame_hu.properties
	java -Duser.language=hu -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleaneritalian:	DicomCleaner.class DicomCleaner_it.properties DicomImageBlackout_it.properties
	java -Duser.language=it -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerjapanese:	DicomCleaner.class DicomCleaner_ja.properties DicomImageBlackout_ja.properties
	java -Duser.language=ja -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerkorean:	DicomCleaner.class DicomCleaner_ko.properties DicomImageBlackout_ko.properties
	java -Duser.language=ko -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerdutch:	DicomCleaner.class DicomCleaner_nl.properties DicomImageBlackout_nl.properties SafeFileChooser_nl.properties ApplicationFrame_nl.properties
	java -Duser.language=nl -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanernorwegian:	DicomCleaner.class DicomCleaner_no.properties DicomImageBlackout_no.properties SafeFileChooser_no.properties ApplicationFrame_no.properties
	java -Duser.language=no -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerpolish:	DicomCleaner.class DicomCleaner_pl.properties DicomImageBlackout_pl.properties SafeFileChooser_pl.properties ApplicationFrame_pl.properties
	java -Duser.language=pl -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerportuguese:	DicomCleaner.class DicomCleaner_pt.properties DicomImageBlackout_pt.properties SafeFileChooser_pt.properties ApplicationFrame_pt.properties
	java -Duser.language=pt -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerromanian:	DicomCleaner.class DicomCleaner_ro.properties DicomImageBlackout_ro.properties SafeFileChooser_ro.properties ApplicationFrame_ro.properties
	java -Duser.language=ru -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerrussian:	DicomCleaner.class DicomCleaner_ru.properties DicomImageBlackout_ru.properties SafeFileChooser_ru.properties ApplicationFrame_ru.properties
	java -Duser.language=ru -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerswedish:	DicomCleaner.class DicomCleaner_sv.properties DicomImageBlackout_sv.properties
	java -Duser.language=sv -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerturkish:	DicomCleaner.class DicomCleaner_tr.properties DicomImageBlackout_tr.properties SafeFileChooser_tr.properties ApplicationFrame_tr.properties
	java -Duser.language=tr -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleanerchinesesimplified:	DicomCleaner.class DicomCleaner_zh_CN.properties DicomImageBlackout_zh_CN.properties
	java -Duser.language=zh_CN -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testcleaneralllocalizations: \
	testcleanerczech \
	testcleanerdannish \
	testcleanergerman \
	testcleanergreek \
	testcleanerspanish \
	testcleanerfinnish \
	testcleanerfrench \
	testcleanerhungarian \
	testcleaneritalian \
	testcleanerjapanese \
	testcleanerkorean \
	testcleanerdutch \
	testcleanernorwegian \
	testcleanerpolish \
	testcleanerportuguese \
	testcleanerromanian \
	testcleanerrussian \
	testcleanerswedish \
	testcleanerturkish \
	testcleanerchinesesimplified

testcleanerwithoutjpegblockredaction:	DicomCleaner.class
	java -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS} com.pixelmed.display.DicomCleaner

testcleanerlimitedmemory:	DicomCleaner.class
	java -Xms256m -Xmx256m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.DicomCleaner

testiconlistbrowser:	IconListBrowser.class IconDemographicAndTechniqueAnnotations.class
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.IconListBrowser \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1001110011/2/1/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000510005/2/35534/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1001110011/2/1/1.dcm \
		$${HOME}/Pictures/Medical/GoodCRDX/Deidentified/1000510005/2/35534/1.dcm

testsuperimposed:	SuperimposedImage.class SuperimposedDicomImage.class
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.SuperimposedDicomImage \
		$${HOME}/Documents/Work/QIBA/CT/QIBA_1B_RadPharm/RoundOne/MultiframeForRendering/series103mf.dcm \
		$${HOME}/Documents/Work/QIBA/CT/QIBA_1B_RadPharm/RoundOne/MultiframeForRendering/seg.dcm

testsuperimposedjpeg:	SuperimposedImage.class SuperimposedDicomImage.class
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.SuperimposedDicomImage \
		$${HOME}/Documents/Work/QIBA/CT/QIBA_1B_RadPharm/RoundOne/MultiframeForRendering/series103mf.dcm \
		$${HOME}/Documents/Work/QIBA/CT/QIBA_1B_RadPharm/RoundOne/MultiframeForRendering/seg.dcm \
		/tmp/testsuperimposed.jpg

testsuperimposedoneseg:	SuperimposedDicomSegments.class SuperimposedImage.class SuperimposedDicomImage.class SourceImageSubset.class
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.SuperimposedDicomSegments \
		$${HOME}/Documents/Work/QIBA/CT/QIBA_1B_RadPharm/RoundOne/MultiframeForRendering/series103mf.dcm \
		$${HOME}/Documents/Work/QIBA/CT/QIBA_1B_RadPharm/RoundOne/MultiframeForRendering/seg.dcm

testsuperimposedmultiseg2013_nac_hncma:	SuperimposedDicomSegments.class SuperimposedImage.class SuperimposedDicomImage.class SourceImageSubset.class
	(cd ../convert; make /tmp/grayscale2013_nac_hncma.dcm /tmp/seg2013_nac_hncma.dcm)
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.SuperimposedDicomSegments \
		/tmp/grayscale2013_nac_hncma.dcm \
		/tmp/seg2013_nac_hncma.dcm

testsuperimposedmultiseg2013_nac_hncma_jpeg:	SuperimposedDicomSegments.class SuperimposedImage.class SuperimposedDicomImage.class SourceImageSubset.class
	(cd ../convert; make /tmp/grayscale2013_nac_hncma.dcm /tmp/seg2013_nac_hncma.dcm)
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} -Djava.awt.headless=true com.pixelmed.display.SuperimposedDicomSegments \
		/tmp/grayscale2013_nac_hncma.dcm \
		/tmp/seg2013_nac_hncma.dcm \
		/tmp/testsuperimposed.jpg

testsuperimposedmultiseg2017_nac_hncma:	SuperimposedDicomSegments.class SuperimposedImage.class SuperimposedDicomImage.class SourceImageSubset.class
	(cd ../convert; make /tmp/grayscale2017_nac_hncma.dcm /tmp/seg2017_nac_hncma.dcm)
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.SuperimposedDicomSegments \
		/tmp/grayscale2017_nac_hncma.dcm \
		/tmp/seg2017_nac_hncma.dcm

testsuperimposedmultiseg2008:	SuperimposedDicomSegments.class SuperimposedImage.class SuperimposedDicomImage.class SourceImageSubset.class
	(cd ../convert; make /tmp/grayscale2008.dcm /tmp/seg2008.dcm)
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.SuperimposedDicomSegments \
		/tmp/grayscale2008.dcm \
		/tmp/seg2008.dcm

# for this to work, need to relax tolerance value to 1.5mm in SuperimposedImage.java :(
testsuperimposedprostate:	SuperimposedDicomSegments.class SuperimposedImage.class SuperimposedDicomImage.class SourceImageSubset.class
	(cd ../convert; make /tmp/grayscaleprostate.dcm /tmp/segprostate.dcm)
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.SuperimposedDicomSegments \
		/tmp/grayscaleprostate.dcm \
		/tmp/segprostate.dcm
		
testsuperimposedpet:	SuperimposedDicomSegments.class SuperimposedImage.class SuperimposedDicomImage.class SourceImageSubset.class
	(cd ../dicom; make testpetforseg)
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.SuperimposedDicomSegments \
		/tmp/petforseg/* \
		$${HOME}/Pictures/Medical/QIICRDICOMSampleData/Iowa-QIN/cerebellum.dcm

# doesn't display at because seg not transverse (is sagittal) (001187) :(
testsuperimposedkidneylesion:	SuperimposedDicomSegments.class SuperimposedImage.class SuperimposedDicomImage.class SourceImageSubset.class
	(cd ../convert; make /tmp/grayscalekidneylesion.dcm)
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.SuperimposedDicomSegments \
		/tmp/grayscalekidneylesion.dcm \
		"$${HOME}/Pictures/Medical/TCIA/C4KC-KiTS/C4KC-KiTS/KiTS-00000/06-29-2003-threephaseabdomen-41748/300-Segmentation-01366/000000.dcm"

# displays in wrong place (whether use corrected version of seg or not) - orientation is rotated to -> P\L (i.e, 0\1\0 1\0\0) rather than source image L\P 1\0\0 0\1\0 (001187) :(
testsuperimposedbrainlesion:	SuperimposedDicomSegments.class SuperimposedImage.class SuperimposedDicomImage.class SourceImageSubset.class
	(cd ../convert; make /tmp/grayscalebrainlesion.dcm)
	java -Xms512m -Xmx512m -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.SuperimposedDicomSegments \
		/tmp/grayscalebrainlesion.dcm \
		"$${HOME}/Pictures/Medical/Segmentations/TCGA-06-0119/DSO/seg_7-AxFLAIR-thin__for__surgery-06923.dcm"
	#"$${HOME}/Pictures/Medical/Segmentations/TCGA-06-0119/DSO/seg_corrected_7-AxFLAIR-thin__for__surgery-06923.dcm"

testsuperimposedsegreadtime:	SuperimposedDicomSegments.class SuperimposedImage.class SuperimposedDicomImage.class SourceImageSubset.class
	java -Xms2g -Xmx2g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} com.pixelmed.display.SuperimposedDicomSegments \
		$${HOME}/Pictures/Medical/Segmentations/TotalBodySegmenter/ABD_LYMPH_008_SEG.dcm

test_iccprofile_defaultcmm:	TestAppMultiFile.class
	java -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.display.TestAppMultiFile \
		$${HOME}/Pictures/Medical/ColorProfileTests/converted/IMAGES/[A-Z]*

test_iccprofile_kcms:	TestAppMultiFile.class
	java -Dsun.java2d.cmm="sun.java2d.cmm.kcms.CMM" \
		-cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.display.TestAppMultiFile \
		$${HOME}/Pictures/Medical/ColorProfileTests/converted/IMAGES/[A-Z]*

test_iccprofile_lcms:	TestAppMultiFile.class
	java -Dsun.java2d.cmm="sun.java2d.cmm.lcms.LcmsServiceProvider" \
		-cp ${PATHTOROOT}:${DICOMADDITIONALJARS} com.pixelmed.display.TestAppMultiFile \
		$${HOME}/Pictures/Medical/ColorProfileTests/converted/IMAGES/[A-Z]*

testdisplayrle:	SingleImagePanel.class
	java -Xms1024m -Xmx1024m -cp ${PATHTOROOT}:${DISPLAYADDITIONALJARS}  \
		com.pixelmed.display.SingleImagePanel \
		"$${HOME}/Pictures/Medical/RLE/view0002.dcm"
		#"$${HOME}/Pictures/Medical/RLE/0020.DCM"
		#"$${HOME}/Pictures/Medical/RLE/US16BitsPaletteColorRLE.dcm"

testviewertiled:	DicomImageViewer.class
	mkdir -p $${HOME}/.com.pixelmed.display.DicomImageViewer.images
	java -Xmx4g -cp ${PATHTOROOT}:${VIEWERADDITIONALJARS}:${PIXELMEDIMAGEIOJAR} ${XALANJAVAOPTS}  \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dicom.TiledFramesIndex=debug \
		com.pixelmed.display.DicomImageViewer \
		"$${HOME}/Pictures/Medical/WSI/WG26Demo2017/AsOf20170923/wsiimages/Tulip^Chris [P-45214579]/20121212 000000 [T13 0121]/Series 003 [SM]/1.3.46.670589.45.1.1.219124696743444.1.4600.1388967452753.3.dcm"
		#"$${HOME}/Pictures/Medical/NEMA/WG26/Hamamatsu/Human_15x15_20x.dcm"





