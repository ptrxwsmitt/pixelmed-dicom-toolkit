PATHTOROOT = ../../..

OBJS = \
	DicomInstanceValidator.class \
	DicomIODDescriptionsCompiled.xsl \
	DicomSRValidator.class \
	DicomSRDescriptionsCompiled.xsl

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

clean:
	rm -f *~ *.class core *.bak ${OBJS} DicomIODDescriptionsCompiled.xsl DicomSRDescriptionsCompiled.xsl

archive:
	tar -cvf - Makefile *.java *.xml *.xsl | gzip -best > ../../../validate.`date '+%Y%m%d'`.tar.gz

DicomIODDescriptionsCompiled.xsl:	DicomIODDescriptionsSource.xml CompileDicomIODDescriptionsIntoXSLT.class
	java \
		-Xms512m -Xmx512m \
		-cp ${PATHTOROOT} ${XALANJAVAOPTS} \
		com.pixelmed.utils.ExecuteTranslet CompileDicomIODDescriptionsIntoXSLT.xsl DicomIODDescriptionsSource.xml DicomIODDescriptionsCompiled.xsl

CompileDicomIODDescriptionsIntoXSLT.class:	CompileDicomIODDescriptionsIntoXSLT.xsl CompileXSLTIntoTranslet.class
	java \
		-Xms512m -Xmx512m \
		-cp ${PATHTOROOT} \
		com.pixelmed.validate.CompileXSLTIntoTranslet CompileDicomIODDescriptionsIntoXSLT.xsl
	#rm CompileXSLTIntoTranslet.class

testvalidate:	DicomInstanceValidator.class DicomIODDescriptionsCompiled.xsl
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomInstanceValidator \
		$${HOME}/work/nemamfmr/DISCIMG/IMAGES/CARANGIO \
		DicomIODDescriptionsCompiled.xsl

DicomSRDescriptionsCompiled.xsl:	DicomSRDescriptionsSource.xml CompileDicomSRDescriptionsIntoXSLT.class
	java \
		-Xms512m -Xmx512m \
		-cp ${PATHTOROOT} ${XALANJAVAOPTS} \
		com.pixelmed.utils.ExecuteTranslet CompileDicomSRDescriptionsIntoXSLT.xsl DicomSRDescriptionsSource.xml DicomSRDescriptionsCompiled.xsl

CompileDicomSRDescriptionsIntoXSLT.class:	CompileDicomSRDescriptionsIntoXSLT.xsl DicomContextGroupsSource.xml CompileXSLTIntoTranslet.class
	java \
		-Xms512m -Xmx512m \
		-cp ${PATHTOROOT} ${XALANJAVAOPTS} \
		com.pixelmed.validate.CompileXSLTIntoTranslet CompileDicomSRDescriptionsIntoXSLT.xsl
	#rm CompileXSLTIntoTranslet.class

srtsctmapping.xml:
	#xsltproc -maxdepth 10000 --nonet \
	#	-o $@ \
	#	"$${HOME}/${PATHTOSTANDARDFROMHOME}/support/stylesheets/extractsrtsctmappingforpixelmed.xsl" \
	#	"$${HOME}/${PATHTOSTANDARDFROMHOME}/source/docbook/part16/part16.xml"
	#
	java -cp ${PATHTOROOT} ${XALANJAVAOPTS} com.pixelmed.utils.ExecuteTranslet \
		"$${HOME}/${PATHTOSTANDARDFROMHOME}/support/stylesheets/extractsrtsctmappingforpixelmed.xsl" \
		"$${HOME}/${PATHTOSTANDARDFROMHOME}/source/docbook/part16/part16.xml" \
		$@

testvalidatesr:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	echo "Validate original MammoCADSR ..."
	java -Xms2g -Xmx2g -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 $${HOME}/work/dctool.support/images/dicom/sr/mammocad/12946
	echo "Copy MammoCADSR into BasicTextSR SOP Class and validate ..."
	dccp $${HOME}/work/dctool.support/images/dicom/sr/mammocad/12946 /tmp/crap \
		-nodisclaimer -r SOPClassUID "1.2.840.10008.5.1.4.1.1.88.11"
	java -Xms2g -Xmx2g -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 /tmp/crap
	echo "Copy MammoCADSR into EnhancedSR SOP Class and validate ..."
	dccp $${HOME}/work/dctool.support/images/dicom/sr/mammocad/12946 /tmp/crap \
		-nodisclaimer -r SOPClassUID "1.2.840.10008.5.1.4.1.1.88.22"
	java -Xms2g -Xmx2g -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 /tmp/crap
	echo "Copy MammoCADSR into ComprehensiveSR SOP Class and validate ..."
	dccp $${HOME}/work/dctool.support/images/dicom/sr/mammocad/12946 /tmp/crap \
		-nodisclaimer -r SOPClassUID "1.2.840.10008.5.1.4.1.1.88.33"
	java -Xms2g -Xmx2g -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 /tmp/crap
	echo "Validate ChestCADSR masquerading as Comprehensive ..."
	java  -Xms2g -Xmx2g -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 $${HOME}/work/dctool.support/images/dicom/sr/mammocad/132214_11508_0000000008.sr
	echo "Copy ChestCADSR masquerading as Comprehensive into ChestCADSR SOP Class and validate ..."
	dccp $${HOME}/work/dctool.support/images/dicom/sr/mammocad/132214_11508_0000000008.sr /tmp/crap \
		-nodisclaimer -r SOPClassUID "1.2.840.10008.5.1.4.1.1.88.65"
	java -Xms2g -Xmx2g -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 /tmp/crap
	echo "Validate DoseSR masquerading as Enhanced ..."
	java  -Xms2g -Xmx2g -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 $${HOME}/Pictures/Medical/DoseSR/getestctinenhsr.dcm
	echo "Copy DoseSR masquerading as Comprehensive into ChestCADSR SOP Class and validate ..."
	dccp  $${HOME}/Pictures/Medical/DoseSR/getestctinenhsr.dcm /tmp/crap \
		-nodisclaimer -r SOPClassUID "1.2.840.10008.5.1.4.1.1.88.67"
	java -Xms2g -Xmx2g -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 /tmp/crap
	rm -f /tmp/crap

testvalidateko:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	echo "Validate original KOS ..."
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		$${HOME}/Pictures/Medical/SRExamples/IHEPDI2005/GE/PAT00000/ST000000/SE000004/KO000000

testvalidateko2:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	echo "Validate original KOS ..."
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		$${HOME}/Pictures/Medical/KOFromPDI2004/STD3KO1

testvalidatetcemanifest1:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	echo "Validate TCE manfiest KOS ..."
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		$${HOME}/Pictures/Medical/TCE/KO.1.2.276.0.7230010.3.1.4.6446.1145570765726.32791.4.dcm

testvalidatetcemanifest2:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	echo "Validate TCE manfiest KOS ..."
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		$${HOME}/Pictures/Medical/TCE/testKOS13.dcm

testvalidatetceaddinfo:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	echo "Validate TCE manfiest KOS ..."
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		$${HOME}/Pictures/Medical/TCE/testATFI13.dcm

testvalidatemammocad:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	echo "Validate original MammoCADSR ..."
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 $${HOME}/work/dctool.support/images/dicom/sr/mammocad/12946

testvalidatemammocad2:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	echo "Validate original MammoCADSR ..."
	java  -Xms2g -Xmx2g -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 $${HOME}/Pictures/Medical/MammoCAD/MammoCADSRAndImages/CD0905120932_Cleaned_I1_SR.dcm

testvalidatemammocad3:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	echo "Validate original MammoCADSR ..."
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 $${HOME}/Pictures/Medical/MammoCAD/MammoCADSRAndImages/srFilesCopied/1.2.840.114191.3.5399.5883513.1.2.1.1354633124

testvalidatemammocad4:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	echo "Validate original MammoCADSR ..."
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 $${HOME}/Pictures/Medical/MammoCAD/MammoVolparaDensity/SRm.1.2.826.0.1.3680043.8.694.1.4.130606205.3696.1351472766.575.dcm

testvalidatechestcad:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	echo "Validate ChestCADSR ..."
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 $${HOME}/Pictures/Medical/ChestCAD/R2CAD/New/IMAGES/P02/092241_5215_0000000002.sr

testvalidatetid1500:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	java  -Xms2g -Xmx2g -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		"$${HOME}/Documents/Work/NCI/Slicer/QIICR/IowaTCIACheck/iowa/seg_sr/QIN-HEADNECK/QIN-HEADNECK-01-0003/1.3.6.1.4.1.14519.5.2.1.2744.7002.150059977302243314164020079415/1.2.276.0.7230010.3.1.3.8323329.18258.1440001306.617246/000000.dcm"

# (000875)
testvalidatelargetid1500:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	java  -Xms2g -Xmx2g -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${SAXONADDITIONALJARS} ${SAXONJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		"$${HOME}/Pictures/Medical/SRExamples/TotalSegmentatorLarge/1.2.840.113654.2.55.216614002338888733987350522981366678482_sr.dcm"

# (000875)
testvalidatelargedosesr:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	java  -Xms2g -Xmx2g -XX:-UseGCOverheadLimit -cp ${PATHTOROOT}:${DICOMADDITIONALJARS}:${SAXONADDITIONALJARS} ${SAXONJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		"$${HOME}/Pictures/Medical/DoseSR/XA_Moreno_20151103/IM-0001-0309.dcm"

testvalidatetce:	DicomSRValidator.class DicomSRDescriptionsCompiled.xsl
	echo "Validate TCE sample ..."
	echo "Expect misspelling of Discussion and yes/no swapped"
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomSRValidator \
		 $${HOME}/Pictures/Medical/TCE/testATFI13.dcm

testvalidatelegacyct:	DicomInstanceValidator.class DicomIODDescriptionsCompiled.xsl
	mkdir -p /tmp/testtoenhanced
	rm -rf /tmp/testtoenhanced/*
	java -Xms3g -Xmx3g -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR} com.pixelmed.dicom.MultiFrameImageFactory \
		"/Volumes/CluniePortable1TB1/Pictures/Medical/TCIA/RIDERLungCT/RIDER Lung CT/RIDER-1129164940/1.3.6.1.4.1.9328.50.1.216116555221814778114703363464001196508/000001" /tmp/testtoenhanced
		#"$${HOME}/Pictures/Medical/PETCTPhilips/PET-CT/CT" /tmp/testtoenhanced
		#"$${HOME}/Documents/Work/QIBA/CT/QIBA_1B_RadPharm/RoundOne/Recontexted/107166 [RIDER 2151469008]/20070208 000000/Series 103 [CT]" /tmp/testtoenhanced
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomInstanceValidator \
		`ls -1d /tmp/testtoenhanced/* | head -1` \
		DicomIODDescriptionsCompiled.xsl
	$${HOME}/work/dicom3tools/appsrc/dcfile/dciodvfy `ls -1d /tmp/testtoenhanced/* | head -1`
	#rm -rf /tmp/testtoenhanced/*

testvalidatelegacymr:	DicomInstanceValidator.class DicomIODDescriptionsCompiled.xsl
	mkdir -p /tmp/testtoenhanced
	rm -rf /tmp/testtoenhanced/*
	java -Xms3g -Xmx3g -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR} com.pixelmed.dicom.MultiFrameImageFactory \
		"$${HOME}/Pictures/Medical/MRDualEcho/BrainSpinEcho/deid/DICOM" /tmp/testtoenhanced
		#"$${HOME}/Pictures/Medical/QIICRDICOMSampleData/BWH-QIN_SF/5-AX_T2" /tmp/testtoenhanced
		#"$${HOME}/Pictures/Medical/QIICRDICOMSampleData/BWH-QIN_SF/9-3D_DCE" /tmp/testtoenhanced
		#"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/PCAMPMRI-0241-DCE" /tmp/testtoenhanced
		#"$${HOME}/Pictures/Medical/MRBrainArtifactPropagatedToMPRSMistakenAsDVA/20131031_MRBrain/ThoroughlyCleaned/hier/RealOrArtifact^BrainMR [74659465245]/20110907 040037 []/Series 009 [MR - AX T1 3D]" \
		#	"$${HOME}/Pictures/Medical/MRBrainArtifactPropagatedToMPRSMistakenAsDVA/20131031_MRBrain/ThoroughlyCleaned/hier/RealOrArtifact^BrainMR [74659465245]/20110907 040037 []/Series 011 [MR - AX T1 FS 3D GAD]" \
		#	/tmp/testtoenhanced
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.validate.DicomInstanceValidator ${XALANJAVAOPTS} \
		`ls -1d /tmp/testtoenhanced/* | head -1` \
		DicomIODDescriptionsCompiled.xsl
	$${HOME}/work/dicom3tools/appsrc/dcfile/dciodvfy `ls -1d /tmp/testtoenhanced/* | head -1`
	#rm -rf /tmp/testtoenhanced/*

testvalidateseg:	DicomInstanceValidator.class DicomIODDescriptionsCompiled.xsl
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.validate.DicomInstanceValidator ${XALANJAVAOPTS} \
		$${HOME}/Pictures/Medical/QIICRDICOMSampleData/Iowa-QIN/cerebellum.dcm
	$${HOME}/work/dicom3tools/appsrc/dcfile/dciodvfy $${HOME}/Pictures/Medical/QIICRDICOMSampleData/Iowa-QIN/cerebellum.dcm

testvalidatesegatlas:	DicomInstanceValidator.class DicomIODDescriptionsCompiled.xsl
	rm -rf /tmp/seg2013_nac_hncma_20160817
	(cd /tmp; bzcat <"$${HOME}/Pictures/Medical/QIICRDICOMSampleData/seg2013_nac_hncma_20160817.tar.bz2" | tar -xvf - seg2013_nac_hncma_20160817/seg2013_nac_hncma.dcm )
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomInstanceValidator \
		"/tmp/seg2013_nac_hncma_20160817/seg2013_nac_hncma.dcm"
	rm -rf /tmp/seg2013_nac_hncma_20160817

testvalidateraw:	DicomInstanceValidator.class DicomIODDescriptionsCompiled.xsl
	java  -Xms512m -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} ${XALANJAVAOPTS} \
		com.pixelmed.validate.DicomInstanceValidator \
		"$${HOME}/Pictures/Medical/NEMA/WG16/Philips/EnhancedMR/Brain/DICOM/XX_0003"
	$${HOME}/work/dicom3tools/appsrc/dcfile/dciodvfy "$${HOME}/Pictures/Medical/NEMA/WG16/Philips/EnhancedMR/Brain/DICOM/XX_0003"

