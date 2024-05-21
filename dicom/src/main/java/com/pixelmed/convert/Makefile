OBJS = \
	AnalyzeToDicom.class \
	AnalyzeHeader.class \
	AnalyzeException.class \
	CommonConvertedAttributeGeneration.class \
	ConvertAmicasJPEG2000FilesetToDicom.class \
	NIfTI1ToDicom.class \
	NIfTI1Header.class \
	NIfTI1Exception.class \
	NRRDToDicom.class \
	NRRDHeader.class \
	NRRDException.class \
	RawImageDescription.class \
	RawRGBInformation.class \
	RawRGBToDicomMultiFrame.class \
	RawToDicomMultiFrame.class \
	UnencapsulateCompressedPixelData.class \
	EncapsulateCompressedPixelData.class \
	IndexedLabelMapToSegmentation.class \
	EncapsulateData.class \
	UnencapsulateData.class \
	TIFFException.class \
	TIFFFile.class \
	TIFFImageFileDirectories.class \
	TIFFImageFileDirectory.class \
	TIFFImageFileDirectoryEntry.class \
	TIFFTags.class \
	TIFFTypes.class \
	TIFFValues.class \
	TIFFToDicom.class \
	TIFFPixelMessageDigest.class \
	AddTIFFOrOffsetTables.class \
	Immunostaining.class
	
all:	${OBJS}

PATHTOROOT = ../../..

include ${PATHTOROOT}/Makefile.common.mk

# override javac command in Makefile.common.mk to add javax.json-api-1.0.jar to classpath for RawImageDescription, add CSVADDITIONALJARS for TIFFToDicom
.java.class:
	export JAVAVERSIONTARGETJARFILE=`/usr/libexec/java_home -v ${JAVAVERSIONTARGET} | tail -1`/jre/lib/rt.jar; javac ${JAVACOPTIONS} \
		-classpath ${PATHTOROOT}:${SLF4JCOMPILEADDITIONALJAR}:${DICOMADDITIONALJARS}:${VIEWERADDITIONALJARS}:${FTPADDITIONALJARS}:${JUNITJAR}:${PATHTOADDITIONAL}/javax.json-api-1.0.jar:${CSVADDITIONALJARS} \
		-sourcepath ${PATHTOROOT} $<

testamicas:	ConvertAmicasJPEG2000FilesetToDicom.class
	mkdir -p /tmp/todicomMR1
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.convert.ConvertAmicasJPEG2000FilesetToDicom "$${HOME}/Pictures/Medical/AmicasJ2K/ELMR20030206" /tmp/todicomMR1
	mkdir -p /tmp/todicomCT1
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.convert.ConvertAmicasJPEG2000FilesetToDicom "$${HOME}/Pictures/Medical/AmicasJ2K/CD0105153529" /tmp/todicomCT1
	mkdir -p /tmp/todicomCT2
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.convert.ConvertAmicasJPEG2000FilesetToDicom "$${HOME}/Pictures/Medical/AmicasJ2K/CD0114154520" /tmp/todicomCT2

testamicasdialog:	ConvertAmicasJPEG2000FilesetToDicom.class
	java -cp ${PATHTOROOT}:${PATHTOADDITIONAL}/jai_imageio.jar com.pixelmed.convert.ConvertAmicasJPEG2000FilesetToDicom


testanalyzeheader:	AnalyzeHeader.class AnalyzeException.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.AnalyzeHeader \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.hdr"
		#"$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Analyze/Analyze75/MT_H13885_4.hdr"

testanalyzeconvert:	AnalyzeToDicom.class AnalyzeHeader.class AnalyzeException.class
	rm -f /tmp/avg152T1_LR-marked.dcm
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.AnalyzeToDicom \
		"$${HOME}/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.hdr" \
		"/tmp/avg152T1_LR-marked.dcm" \
		"H13885" H13885_9M "" 40001 1 MR
	if [ -f "$${HOME}/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "$${HOME}/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.dcm" "/tmp/avg152T1_LR-marked.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp  "$${HOME}/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.dcm" "/tmp/avg152T1_LR-marked.dcm"; \
		echo "Finished comparing"; \
	fi

# has orient = 0 TRANSVERSE_UNFLIPPED, unsigned pixdim values (001188)
testanalyze_OAS10001:	AnalyzeToDicom.class AnalyzeHeader.class AnalyzeException.class
	rm -f "/tmp/OAS10001.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.AnalyzeToDicom \
		"$${HOME}/Pictures/Medical/OAS10001/OAS10001.hdr" \
		"/tmp/OAS10001.dcm" \
		"OAS10001" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "$${HOME}/Pictures/Medical/OAS10001/OAS10001.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "$${HOME}/Pictures/Medical/OAS10001/OAS10001/OAS10001.dcm" "/tmp/OAS10001.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp  "$${HOME}/Pictures/Medical/OAS10001/OAS10001/OAS10001.dcm" "/tmp/OAS10001.dcm"; \
		echo "Finished comparing"; \
	fi

testanalyzecompare:	testanalyzeconvert

testanalyzecreate:
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.dcm"
	make testanalyzecompare
	cp /tmp/avg152T1_LR-marked.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/Analyze/avg152T1_LR-marked/avg152T1_LR-marked.dcm"

# expect differences between Bruker original enhanced MF and pixelmed converted ... take care not to overwrite original during testing
testanalyzemtmouse:	AnalyzeToDicom.class AnalyzeHeader.class AnalyzeException.class
	rm -f /tmp/MT_H13885_4.dcm
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.AnalyzeToDicom \
		"$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Analyze/Analyze75/MT_H13885_4.hdr" \
		"/tmp/MT_H13885_4.dcm" \
		"H13885" H13885_9M "" 40001 1 MR
	if [ -f "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Bruker/DICOM/EnhancedMR/H13885_9M_H13885_PKD__E4_P1/EnIm1.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Bruker/DICOM/EnhancedMR/H13885_9M_H13885_PKD__E4_P1/EnIm1.dcm" "/tmp/MT_H13885_4.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/DataSets/WG30/Mayo/MR/KlineErickson_MTexample_05192015/Bruker/DICOM/EnhancedMR/H13885_9M_H13885_PKD__E4_P1/EnIm1.dcm" "/tmp/MT_H13885_4.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1header:	NIfTI1Header.class NIfTI1Exception.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1Header \
		"$${HOME}/Pictures/Medical/ParametricMaps/DICOM_FP/GE_Normalized_Boxerman_rBFMap_LeakageCorrected.nii"
		#"$${HOME}/Pictures/Medical/ParametricMaps/DICOM_FP/dti_adc.nii"

testnifti1convert:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f /tmp/testnifti1fp.dcm
	rm -f /tmp/testnifti1fp_scaledtoint.dcm
	rm -f /tmp/testnifti1fp_scaledtointthenmadefloatagain.dcm
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"$${HOME}/Pictures/Medical/ParametricMaps/DICOM_FP/DICOM_FP/GE_Normalized_Boxerman_rBFMap_LeakageCorrected.nii" \
		/tmp/testnifti1fp.dcm \
		"Doe^Jane" 1234 5678 100 1
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		/tmp/testnifti1fp.dcm /tmp/testnifti1fp_scaledtoint.dcm
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toFLOAT /tmp/testnifti1fp_scaledtoint.dcm /tmp/testnifti1fp_scaledtointthenmadefloatagain.dcm
	andump /tmp/testnifti1fp.dcm
	#dciodvfy /tmp/testnifti1fp.dcm
	andump /tmp/testnifti1fp_scaledtoint.dcm
	dciodvfy /tmp/testnifti1fp_scaledtoint.dcm
	#dchist -h /tmp/testnifti1fp_scaledtoint.dcm
	#andiff /tmp/testnifti1fp.dcm /tmp/testnifti1fp_scaledtoint.dcm
	@echo "Checking round trip to int and back to float ..."
	#dccmp /tmp/testnifti1fp.dcm /tmp/testnifti1fp_scaledtointthenmadefloatagain.dcm
	andiff /tmp/testnifti1fp.dcm /tmp/testnifti1fp_scaledtointthenmadefloatagain.dcm

testnifti1talairach:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "$${HOME}/Documents/Medical/stuff/Atlases/Talairach/talairach.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"$${HOME}/Documents/Medical/stuff/Atlases/Talairach/talairach.nii" \
		"$${HOME}/Documents/Medical/stuff/Atlases/Talairach/talairach.dcm" \
		"Talairach" 1234 5678 100 1 MR

testnifti1dti:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f /tmp/dti20.dcm
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/dcm2nii_TestDataSets/dti20/dti20.nii" \
		"/tmp/dti20.dcm" \
		"DTI" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm" "/tmp/dti20.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm" "/tmp/dti20.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1test4d1:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/filtered_func_data.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.nii" \
		"/tmp/filtered_func_data.dcm" \
		"4D" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm" "/tmp/filtered_func_data.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm" "/tmp/filtered_func_data.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testLR:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/avg152T1_LR_nifti.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.nii" \
		"/tmp/avg152T1_LR_nifti.dcm" \
		"LR" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" "/tmp/avg152T1_LR_nifti.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" "/tmp/avg152T1_LR_nifti.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testLR_hdr:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/avg152T1_LR_nifti.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.hdr" \
		"/tmp/avg152T1_LR_nifti.dcm" \
		"LR" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" "/tmp/avg152T1_LR_nifti.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm" "/tmp/avg152T1_LR_nifti.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testRL:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/avg152T1_RL_nifti.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.nii" \
		"/tmp/avg152T1_RL_nifti.dcm" \
		"RL" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm" "/tmp/avg152T1_RL_nifti.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm" "/tmp/avg152T1_RL_nifti.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testZ:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/zstat1.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.nii" \
		"/tmp/zstat1.dcm" \
		"RL" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm" "/tmp/zstat1.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm" "/tmp/zstat1.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1testfunc:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/newsirp_final_XML.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.nii" \
		"/tmp/newsirp_final_XML.dcm" \
		"RL" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm" "/tmp/newsirp_final_XML.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm" "/tmp/newsirp_final_XML.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1brainlab:	NIfTI1ToDicom.class NIfTI1Header.class NIfTI1Exception.class
	rm -f "/tmp/1010_brain_mr_06_pixelmed.dcm"
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NIfTI1ToDicom \
		"/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/BrainLabQuaternionTest/1010_brain_mr_06.nii" \
		"/tmp/1010_brain_mr_06_pixelmed.dcm" \
		"BrainLab^Quaternion" 1234 5678 100 1 MR "1.2.840.10008.5.1.4.1.1.4.4"
	if [ -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/BrainLabQuaternionTest/1010_brain_mr_06_pixelmed.dcm" ]; \
	then \
		echo "Comparing header ..."; \
		dcdiff "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/BrainLabQuaternionTest/1010_brain_mr_06_pixelmed.dcm" "/tmp/1010_brain_mr_06_pixelmed.dcm"; \
		echo "Comparing pixel data ..."; \
		dccmp "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/BrainLabQuaternionTest/1010_brain_mr_06_pixelmed.dcm" "/tmp/1010_brain_mr_06_pixelmed.dcm"; \
		echo "Finished comparing"; \
	fi

testnifti1compare:	testnifti1dti testnifti1test4d1 testnifti1testLR testnifti1testRL testnifti1testZ testnifti1testfunc testnifti1brainlab

testnifti1create:
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm"
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm"
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm"
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm"
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm"
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm"
	rm -f "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/BrainLabQuaternionTest/1010_brain_mr_06_pixelmed.dcm"
	make testnifti1compare
	cp /tmp/newsirp_final_XML.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/sirp_fmri_study_ver4/newsirp_final_XML.dcm"
	cp /tmp/zstat1.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/zstat1.dcm"
	cp /tmp/avg152T1_RL_nifti.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_RL_nifti.dcm"
	cp /tmp/avg152T1_LR_nifti.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/avg152T1_LR_nifti.dcm"
	cp /tmp/filtered_func_data.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/NIfTI-1_TestDataSets/filtered_func_data.dcm"
	cp /tmp/dti20.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/dcm2nii_TestDataSets/dti20.dcm"
	cp /tmp/1010_brain_mr_06_pixelmed.dcm "/Volumes/Toshiba5TEnc/Pictures/Medical/NIfTI/BrainLabQuaternionTest/1010_brain_mr_06_pixelmed.dcm"

testnrrdheaderseparate:	NRRDHeader.class NRRDException.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDHeader \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume.nhdr"

testnrrdheadercombined:	NRRDHeader.class NRRDException.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDHeader \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume frame 21.nrrd"

testnrrdconvertseparate:	NRRDToDicom.class NRRDHeader.class NRRDException.class
	rm -f /tmp/testnrrdfp.dcm
	rm -f /tmp/testnrrdfp_scaledtoint.dcm
	rm -f /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	java -Xmx2g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume.nhdr" \
		/tmp/testnrrdfp.dcm \
		"Doe^Jane" 1234 5678 100 1
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		/tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtoint.dcm
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toDOUBLE /tmp/testnrrdfp_scaledtoint.dcm /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	#andump /tmp/testnrrdfp.dcm
	#dciodvfy /tmp/testnrrdfp.dcm
	andump /tmp/testnrrdfp_scaledtoint.dcm
	dciodvfy /tmp/testnrrdfp_scaledtoint.dcm
	#dchist -h /tmp/testnrrdfp_scaledtoint.dcm
	#andiff /tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtoint.dcm
	#@echo "Checking round trip to int and back to float ..."
	#dccmp /tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm		#dccmp fails (hangs for ever during dctoraw ... ? why :()
	andiff /tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm

testnrrdconvertcombined:	NRRDToDicom.class NRRDHeader.class NRRDException.class
	rm -f /tmp/testnrrdfp.dcm
	rm -rf /tmp/testnrrdfp_withrwvm
	mkdir -p /tmp/testnrrdfp_withrwvm
	rm -f /tmp/testnrrdfp_scaledtoint.dcm
	rm -f /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/OncoQuant-AutoAIF-Results/PCAMPMRI-0241_GE-Ktrans.nrrd" \
		/tmp/testnrrdfp_norwvm.dcm \
		"Doe^Jane" 1234 5678 100 1 \
		MR
	#"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume frame 21.nrrd"
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.InsertRealWorldValueMap \
			0 5 0 1 "Ktrans" "Ktrans" \
			"/min" "UCUM" "" "/min" \
			CODE "(246205007, SCT, Quantity)" "(126312, DCM, Ktrans)" \
			CODE '(370129005, SCT, "Measurement Method")' '(126340, DCM, "Standard Tofts Model")' \
			/tmp/testnrrdfp_norwvm.dcm \
			/tmp/testnrrdfp_withrwvm
	@echo "Comparing before and after adding RWVM ..."
	andiff /tmp/testnrrdfp_withrwvm/* /tmp/testnrrdfp_norwvm.dcm
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		/tmp/testnrrdfp_withrwvm/* /tmp/testnrrdfp_scaledtoint.dcm
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toFLOAT /tmp/testnrrdfp_scaledtoint.dcm /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	#andump /tmp/testnrrdfp_withrwvm/*
	#dciodvfy /tmp/testnrrdfp_withrwvm/*
	andump /tmp/testnrrdfp_scaledtoint.dcm
	dciodvfy /tmp/testnrrdfp_scaledtoint.dcm
	#dchist -h /tmp/testnrrdfp_scaledtoint.dcm
	#andiff /tmp/testnrrdfp.dcm /tmp/testnrrdfp_scaledtoint.dcm
	@echo "Checking round trip to int and back to float ..."
	#dccmp /tmp/testnrrdfp_withrwvm/* /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	andiff /tmp/testnrrdfp_withrwvm/* /tmp/testnrrdfp_scaledtointthenmadefloatagain.dcm
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"top\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"ImageType\" : [ \"DERIVED\", \"PRIMARY\", \"PERFUSION\", \"KTRANS\" ]"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"FrameAnatomySequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"AnatomicRegionSequence\" : { \"cv\" : \"41216001\", \"csd\" : \"SCT\", \"cm\" : \"Prostate\" },"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameLaterality\" : \"U\""
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"ParametricMapFrameTypeSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameType\" : [ \"DERIVED\", \"PRIMARY\", \"PERFUSION\", \"KTRANS\" ]"
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	cat -n /tmp/testsetcharacteristics.json
	rm -rf /tmp/testnrrdfp_withrwvmandcharacteristics
	mkdir /tmp/testnrrdfp_withrwvmandcharacteristics
	rm -f /tmp/testsetcharacteristics.log
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.SetCharacteristicsFromSummary \
			/tmp/testsetcharacteristics.json \
			/tmp/testnrrdfp_withrwvm/* \
			/tmp/testnrrdfp_withrwvmandcharacteristics \
		2>/tmp/testsetcharacteristics.log
	cat /tmp/testsetcharacteristics.log
	rm -f /tmp/filetouse.dat
	grep </tmp/testsetcharacteristics.log 'Writing with characteristics set file' | tail -1 | sed -e 's/^.*\/tmp/\/tmp/' >/tmp/filetouse.dat
	@echo "check result with dciodvfy ..."
	dciodvfy "`cat /tmp/filetouse.dat`"
	@echo "test that dciodvfy does not require PatientOrientation ..."
	rm -f /tmp/testnrrdfpwithoutpatientorientation.dcm
	dccp -nodisclaimer -noadddicom -d PatientOrientation "`cat /tmp/filetouse.dat`" /tmp/testnrrdfpwithoutpatientorientation.dcm
	dciodvfy /tmp/testnrrdfpwithoutpatientorientation.dcm
	rm /tmp/testnrrdfpwithoutpatientorientation.dcm

testnrrdheaderseparate:	NRRDHeader.class NRRDException.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDHeader \
		"$${HOME}/Pictures/Medical/ParametricMaps/PkModeling_verification/PCAMPMRI-0241/SlicerScene/MRMLMultiVolume.nhdr"

testrawrgbheader:	RawRGBInformation.class
	if [ ! -f "/tmp/vhm/testfiles/README" ]; \
	then \
		mkdir -p "/tmp/vhm/testfiles"; \
		cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/README" "/tmp/vhm/testfiles/README"; \
	fi
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBInformation \
		"/tmp/vhm/testfiles/README"

testrawrgbvhmuncompressed:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/uncompressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/README" "/tmp/vhm/testfiles/"
	gzcat <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2000.rgb.gz" >"/tmp/vhm/testfiles/srcimages/uncompressed/2000.rgb"
	gzcat <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2001.rgb.gz" >"/tmp/vhm/testfiles/srcimages/uncompressed/2001.rgb"
	gzcat <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2002.rgb.gz" >"/tmp/vhm/testfiles/srcimages/uncompressed/2002.rgb"
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/uncompressed" \
		".rgb" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmcompressed:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/compressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/README" "/tmp/vhm/testfiles/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2000.rgb.gz" "/tmp/vhm/testfiles/srcimages/compressed/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2001.rgb.gz" "/tmp/vhm/testfiles/srcimages/compressed/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/70mm/fullbody/2002.rgb.gz" "/tmp/vhm/testfiles/srcimages/compressed/"
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/compressed" \
		".rgb.gz" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmuncompressedsmall:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/uncompressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" "/tmp/vhm/testfiles/"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1455.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1455.raw"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1456.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1456.raw"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1457.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1457.raw"
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/uncompressed" \
		".raw" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmunixcompressed:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/compressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" "/tmp/vhm/testfiles/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1455.raw.Z" "/tmp/vhm/testfiles/srcimages/compressed/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1456.raw.Z" "/tmp/vhm/testfiles/srcimages/compressed/"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1457.raw.Z" "/tmp/vhm/testfiles/srcimages/compressed/"
	# BZIP2ADDITIONALJAR is now commons-compress, which contains Unix Z decompressor ...
	java -cp ${PATHTOROOT}:${BZIP2ADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/compressed" \
		".raw.Z" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmbzip2:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/uncompressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	cp "$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" "/tmp/vhm/testfiles/"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1455.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1455.raw"
	bzip2 "/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1455.raw"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1456.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1456.raw"
	bzip2 "/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1456.raw"
	uncompress <"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm1457.raw.Z" >"/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1457.raw"
	bzip2 "/tmp/vhm/testfiles/srcimages/uncompressed/a_vm1457.raw"
	java -cp ${PATHTOROOT}:${BZIP2ADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"/tmp/vhm/testfiles/README" \
		"/tmp/vhm/testfiles/srcimages/uncompressed" \
		".raw.bz2" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmentiremaleabdomensmall:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	java -cp ${PATHTOROOT}:${BZIP2ADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" \
		"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen" \
		".raw.Z" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testrawrgbvhmsubsetmaleabdomensmall:	RawRGBToDicomMultiFrame.class RawRGBInformation.class
	rm -rf "/tmp/vhm/testfiles"
	mkdir -p "/tmp/vhm/testfiles/srcimages/compressed"
	mkdir -p "/tmp/vhm/testfiles/dstimages/testrawrgbvhm"
	for i in \
		$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm14[0-9][0-9].raw.Z \
		$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm15[0-9][0-9].raw.Z \
		$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/abdomen/a_vm16[0-9][0-9].raw.Z \
		; do cp $$i /tmp/vhm/testfiles/srcimages/compressed/; done
	java -cp ${PATHTOROOT}:${BZIP2ADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.RawRGBToDicomMultiFrame \
		"$${HOME}/Pictures/Medical/vhm/vhnet.nlm.nih.gov/Male/Fullcolor/README" \
		"/tmp/vhm/testfiles/srcimages/compressed" \
		".raw.Z" \
		"/tmp/vhm/testfiles/dstimages/testrawrgbvhm" \
		"Doe^John" 1234 5678 100

testunencap:	UnencapsulateCompressedPixelData.class
	mkdir -p /tmp/crapunencap
	rm -f /tmp/crapunencap/*
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.UnencapsulateCompressedPixelData \
			"$${HOME}/Pictures/Medical/MultiframeEncapsulated/JPEGBaseline/Acuson/ACUSON-24-YBR_FULL_422-Jpeg_Baseline_1.dcm" \
			/tmp/crapunencap/frame_

testreencap:	UnencapsulateCompressedPixelData.class EncapsulateCompressedPixelData.class
	cp "$${HOME}/Pictures/Medical/MultiframeEncapsulated/JPEGBaseline/Acuson/ACUSON-24-YBR_FULL_422-Jpeg_Baseline_1.dcm" /tmp/srcfile.dcm
	mkdir -p /tmp/crapunencap
	rm -f /tmp/crapunencap/*
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.UnencapsulateCompressedPixelData \
			/tmp/srcfile.dcm \
			/tmp/crapunencap/frame_
	rm -f /tmp/crapreencap.dcm
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.EncapsulateCompressedPixelData \
			/tmp/crapreencap.dcm \
			/tmp/srcfile.dcm \
			/tmp/crapunencap/frame_*
	#rm -f /tmp/srcfile.dcm

/tmp/grayscale2008.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2008/SPL_PNL_Brain_Atlas2008/01-MRI/grayscale.nrrd" \
		/tmp/grayscale2008.dcm \
		"Brain^Atlas SPL-PNL 2008" 2008 5678 100 1 \
		MR

/tmp/labelmap2008.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2008/SPL_PNL_Brain_Atlas2008/02-labels/seg.nrrd" \
		/tmp/labelmap2008.dcm \
		"Brain^Atlas SPL-PNL 2008" 2008 5678 101 1

# use 2007 label map since none for 2008 ... some values are missing (and some in map are unused, mostly duplicates, but also pituitary)
testlabelmapbrainatlas2008:	IndexedLabelMapToSegmentation.class /tmp/labelmap2008.dcm /tmp/grayscale2008.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmap2008.dcm \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2007/SPL-BrainAtlas-label_list_fields_RGB.csv" \
		/tmp/seg2008.dcm \
		/tmp/grayscale2008.dcm \
		102 "Segmented Brain" BRAIN_ATLAS_2008 "SPL-PNL Brain Atlas 2008" "SPL-PNL^Talos et al"
	dcentvfy /tmp/seg2008.dcm /tmp/grayscale2008.dcm



/tmp/grayscale2013_10.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-brain-atlas-1.0/volumes/A1_grayT1.nrrd" \
		/tmp/grayscale2013_10.dcm \
		"Brain^Atlas NAC 2013" 2013 5678 100 1 \
		MR

/tmp/labelmap2013_10.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-brain-atlas-1.0/volumes/A1_labels.nrrd" \
		/tmp/labelmap2013_10.dcm \
		"Brain^Atlas NAC 2013" 2013 5678 101 1

testlabelmapbrainatlas2013_10:	IndexedLabelMapToSegmentation.class /tmp/labelmap2013_10.dcm /tmp/grayscale2013_10.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmap2013_10.dcm \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/BrainAtlas-label_list_fields_RGB.csv" \
		/tmp/seg2013_10.dcm \
		/tmp/grayscale2013_10.dcm \
		102 "Segmented Brain" BRAIN_ATLAS_2013 "NAC Brain Atlas 2013" "NAC^Halle et al"
	dcentvfy /tmp/seg2013_10.dcm /tmp/grayscale2013_10.dcm



/tmp/grayscale2013_nac_hncma.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-hncma-atlas2013-Slicer4Version/Data/A1_grayT1.nrrd" \
		/tmp/grayscale2013_nac_hncma_fp.dcm \
		"Brain^Atlas NAC HNCMA 2013" 2013 5678 100 1 \
		MR
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"top\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"ImageType\" : [ \"DERIVED\", \"PRIMARY\", \"PRE_CONTRAST\", \"NONE\" ]"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"FrameAnatomySequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"AnatomicRegionSequence\" : { \"cv\" : \"12738006\", \"csd\" : \"SCT\", \"cm\" : \"Brain\" },"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameLaterality\" : \"B\""
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"RealWorldValueMappingSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"DoubleFloatRealWorldValueFirstValueMapped\" : \"0\","
	echo >>/tmp/testsetcharacteristics.json "		\"DoubleFloatRealWorldValueLastValueMapped\" : \"32767\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueIntercept\" : \"0\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueSlope\" : \"1\","
	echo >>/tmp/testsetcharacteristics.json "		\"LUTExplanation\" : \"Unitless\","
	echo >>/tmp/testsetcharacteristics.json "		\"LUTLabel\" : \"US\","
	echo >>/tmp/testsetcharacteristics.json "		\"MeasurementUnitsCodeSequence\" : { \"cv\" : \"1\", \"csd\" : \"UCUM\", \"cm\" : \"1\" }"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"ParametricMapFrameTypeSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameType\" : [ \"DERIVED\", \"PRIMARY\", \"PRE_CONTRAST\", \"NONE\" ]"
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	cat /tmp/testsetcharacteristics.json
	mkdir -p /tmp/withcharacteristics
	rm -rf /tmp/withcharacteristics/*
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.SetCharacteristicsFromSummary \
			/tmp/testsetcharacteristics.json \
			/tmp/grayscale2013_nac_hncma_fp.dcm \
			/tmp/withcharacteristics \
		2>/tmp/setcharacteristics.log
	rm -f /tmp/filetouse.dat
	grep </tmp/setcharacteristics.log 'Writing with characteristics set file' | tail -1 | sed -e 's/^.*\/tmp/\/tmp/' >/tmp/filetouse.dat
	#@echo "Comparing header before and after characteristics added ..."
	#dcdiff /tmp/grayscale2013_nac_hncma_fp.dcm "`cat /tmp/filetouse.dat`"
	rm -f /tmp/grayscale2013_nac_hncma_fp.dcm
	mv "`cat /tmp/filetouse.dat`" /tmp/grayscale2013_nac_hncma_fp.dcm
	rm -rf /tmp/withcharacteristics
	rm /tmp/testsetcharacteristics.json
	rm /tmp/filetouse.dat
	rm /tmp/setcharacteristics.log
	# toIntTruncate produces spotty result :(
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toInt \
		/tmp/grayscale2013_nac_hncma_fp.dcm /tmp/grayscale2013_nac_hncma.dcm
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"remove\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"PatientOrientation\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"ContentLabel\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"ContentDescription\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"ContentCreatorName\" : null"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"removeall\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"ParametricMapFrameTypeSequence\" : null"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"top\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"PixelPresentation\" : \"MONOCHROME\","
	echo >>/tmp/testsetcharacteristics.json "		\"VolumetricProperties\" : \"VOLUME\","
	echo >>/tmp/testsetcharacteristics.json "		\"VolumeBasedCalculationTechnique\" : \"NONE\","
	echo >>/tmp/testsetcharacteristics.json "		\"SOPClassUID\" : \"1.2.840.10008.5.1.4.1.1.4.4\""
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"RealWorldValueMappingSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueFirstValueMapped\" : \"0\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueLastValueMapped\" : \"65535\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueIntercept\" : \"0\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueSlope\" : \"1\","
	echo >>/tmp/testsetcharacteristics.json "		\"LUTExplanation\" : \"Unitless\","
	echo >>/tmp/testsetcharacteristics.json "		\"LUTLabel\" : \"US\","
	echo >>/tmp/testsetcharacteristics.json "		\"MeasurementUnitsCodeSequence\" : { \"cv\" : \"1\", \"csd\" : \"UCUM\", \"cm\" : \"1\" }"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"MRImageFrameTypeSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameType\" : [ \"DERIVED\", \"PRIMARY\", \"PRE_CONTRAST\", \"NONE\" ],"
	echo >>/tmp/testsetcharacteristics.json "		\"PixelPresentation\" : \"MONOCHROME\","
	echo >>/tmp/testsetcharacteristics.json "		\"VolumetricProperties\" : \"VOLUME\","
	echo >>/tmp/testsetcharacteristics.json "		\"VolumeBasedCalculationTechnique\" : \"NONE\""
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	cat /tmp/testsetcharacteristics.json
	mkdir -p /tmp/withcharacteristics
	rm -rf /tmp/withcharacteristics/*
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.SetCharacteristicsFromSummary \
			/tmp/testsetcharacteristics.json \
			/tmp/grayscale2013_nac_hncma.dcm \
			/tmp/withcharacteristics \
		2>/tmp/setcharacteristics.log
	rm -f /tmp/filetouse.dat
	grep </tmp/setcharacteristics.log 'Writing with characteristics set file' | tail -1 | sed -e 's/^.*\/tmp/\/tmp/' >/tmp/filetouse.dat
	#@echo "Comparing header before and after characteristics added ..."
	#dcdiff /tmp/grayscale2013_nac_hncma.dcm "`cat /tmp/filetouse.dat`"
	rm -f /tmp/grayscale2013_nac_hncma.dcm
	mv "`cat /tmp/filetouse.dat`" /tmp/grayscale2013_nac_hncma.dcm
	rm -rf /tmp/withcharacteristics
	rm /tmp/testsetcharacteristics.json
	rm /tmp/filetouse.dat
	# need to give the integer version a different SOP Instance UID ...
	mv /tmp/grayscale2013_nac_hncma.dcm /tmp/grayscale2013_nac_hncma.dcm.bak
	dccp -d SOPInstanceUID -nodisclaimer /tmp/grayscale2013_nac_hncma.dcm.bak /tmp/grayscale2013_nac_hncma.dcm
	@echo "Comparing header before and after replacing SOP Instance UID added ..."
	dcdiff /tmp/grayscale2013_nac_hncma.dcm.bak /tmp/grayscale2013_nac_hncma.dcm
	rm /tmp/grayscale2013_nac_hncma.dcm.bak
	# now clean up floating point now that it has been used to make legacy converted MR ...
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"removeall\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"UnassignedSharedConvertedAttributesSequence\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"UnassignedPerFrameConvertedAttributesSequence\" : null"
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	cat /tmp/testsetcharacteristics.json
	mkdir -p /tmp/withcharacteristics
	rm -rf /tmp/withcharacteristics/*
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.SetCharacteristicsFromSummary \
			/tmp/testsetcharacteristics.json \
			/tmp/grayscale2013_nac_hncma_fp.dcm \
			/tmp/withcharacteristics \
		2>/tmp/setcharacteristics.log
	rm -f /tmp/filetouse.dat
	grep </tmp/setcharacteristics.log 'Writing with characteristics set file' | tail -1 | sed -e 's/^.*\/tmp/\/tmp/' >/tmp/filetouse.dat
	#@echo "Comparing header before and after characteristics added ..."
	#dcdiff /tmp/grayscale2013_nac_hncma_fp.dcm "`cat /tmp/filetouse.dat`"
	rm -f /tmp/grayscale2013_nac_hncma_fp.dcm
	mv "`cat /tmp/filetouse.dat`" /tmp/grayscale2013_nac_hncma_fp.dcm
	rm -rf /tmp/withcharacteristics
	rm /tmp/testsetcharacteristics.json
	rm /tmp/filetouse.dat
	rm /tmp/setcharacteristics.log

/tmp/labelmap2013_nac_hncma.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-hncma-atlas2013-Slicer4Version/Data/hncma-atlas.nrrd" \
		/tmp/labelmap2013_nac_hncma.dcm \
		"Brain^Atlas NAC HNCMA 2013" 2013 5678 101 1

/tmp/seg2013_nac_hncma.dcm:	IndexedLabelMapToSegmentation.class /tmp/labelmap2013_nac_hncma.dcm /tmp/grayscale2013_nac_hncma.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmap2013_nac_hncma.dcm \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/BrainAtlas-label_list_fields_RGB.csv" \
		/tmp/seg2013_nac_hncma.dcm \
		/tmp/grayscale2013_nac_hncma.dcm \
		102 "Segmented Brain Head Neck Muscles" BRAIN_ATLAS_2013 "NAC HNCMA Brain Atlas 2013" "NAC^Halle et al"

testlabelmapbrainatlas2013_nac_hncma:	/tmp/seg2013_nac_hncma.dcm
	#dcentvfy /tmp/seg2013_nac_hncma.dcm /tmp/grayscale2013_nac_hncma.dcm

/tmp/labelmap2013_nac_hncma_skin.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/nac-hncma-atlas2013-Slicer4Version/Data/skin.nrrd" \
		/tmp/labelmap2013_nac_hncma_skin.dcm \
		"Brain^Atlas NAC HNCMA 2013" 2013 5678 101 1

/tmp/seg2013_nac_hncma_skin.dcm:	IndexedLabelMapToSegmentation.class /tmp/labelmap2013_nac_hncma_skin.dcm /tmp/grayscale2013_nac_hncma.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmap2013_nac_hncma_skin.dcm \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/BrainAtlas-label_list_fields_RGB.csv" \
		/tmp/seg2013_nac_hncma_skin.dcm \
		/tmp/grayscale2013_nac_hncma.dcm \
		102 "Segmented Brain Head Neck Muscles" BRAIN_ATLAS_2013 "NAC HNCMA Brain Atlas 2013" "NAC^Halle et al"

testlabelmapbrainatlas2013_nac_hncma_skin:	/tmp/labelmap2013_nac_hncma_skin.dcm
	#dcentvfy /tmp/seg2013_nac_hncma_skin.dcm /tmp/grayscale2013_nac_hncma_skin.dcm

cleanlabelmapbrainatlas2013_nac:
	rm -f \
		/tmp/grayscale2013_nac_hncma.dcm \
		/tmp/grayscale2013_nac_hncma_fp.dcm \
		/tmp/labelmap2013_nac_hncma.dcm \
		/tmp/labelmap2013_nac_hncma_skin.dcm \
		/tmp/seg2013_nac_hncma.dcm \
		/tmp/seg2013_nac_hncma_skin.dcm

testlabelmapbrainatlas2013_nac:	/tmp/grayscale2013_nac_hncma.dcm /tmp/seg2013_nac_hncma.dcm /tmp/seg2013_nac_hncma_skin.dcm
	@echo "Performing dcentvfy"
	dcentvfy \
		/tmp/grayscale2013_nac_hncma.dcm \
		/tmp/grayscale2013_nac_hncma_fp.dcm \
		/tmp/labelmap2013_nac_hncma.dcm \
		/tmp/labelmap2013_nac_hncma_skin.dcm \
		/tmp/seg2013_nac_hncma.dcm \
		/tmp/seg2013_nac_hncma_skin.dcm
	@echo "Performing dciodvfy"
	dciodvfy -filename /tmp/grayscale2013_nac_hncma.dcm
	#dciodvfy -filename /tmp/grayscale2013_nac_hncma_fp.dcm
	#dciodvfy -filename /tmp/labelmap2013_nac_hncma.dcm
	#dciodvfy -filename /tmp/labelmap2013_nac_hncma_skin.dcm
	dciodvfy -filename /tmp/seg2013_nac_hncma.dcm
	dciodvfy -filename /tmp/seg2013_nac_hncma_skin.dcm

archivelabelmapbrainatlas2013_nac:
	rm -f /tmp/seg2013_nac_hncma.tar /tmp/seg2013_nac_hncma.tar.bz2
	(cd /tmp; tar -cvf seg2013_nac_hncma.tar \
		grayscale2013_nac_hncma.dcm \
		seg2013_nac_hncma.dcm \
		seg2013_nac_hncma_skin.dcm \
	)
	bzip2 /tmp/seg2013_nac_hncma.tar


/tmp/grayscale2017_nac_hncma.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/brain-2017-01/volumes/imaging/A1_grayT1.nrrd" \
		/tmp/grayscale2017_nac_hncma_fp.dcm \
		"Brain^Atlas NAC HNCMA 2017" 2017 5678 100 1 \
		MR
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"top\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"ImageType\" : [ \"DERIVED\", \"PRIMARY\", \"PRE_CONTRAST\", \"NONE\" ]"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"FrameAnatomySequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"AnatomicRegionSequence\" : { \"cv\" : \"12738006\", \"csd\" : \"SCT\", \"cm\" : \"Brain\" },"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameLaterality\" : \"B\""
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"RealWorldValueMappingSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"DoubleFloatRealWorldValueFirstValueMapped\" : \"0\","
	echo >>/tmp/testsetcharacteristics.json "		\"DoubleFloatRealWorldValueLastValueMapped\" : \"32767\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueIntercept\" : \"0\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueSlope\" : \"1\","
	echo >>/tmp/testsetcharacteristics.json "		\"LUTExplanation\" : \"Unitless\","
	echo >>/tmp/testsetcharacteristics.json "		\"LUTLabel\" : \"US\","
	echo >>/tmp/testsetcharacteristics.json "		\"MeasurementUnitsCodeSequence\" : { \"cv\" : \"1\", \"csd\" : \"UCUM\", \"cm\" : \"1\" }"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"ParametricMapFrameTypeSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameType\" : [ \"DERIVED\", \"PRIMARY\", \"PRE_CONTRAST\", \"NONE\" ]"
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	cat /tmp/testsetcharacteristics.json
	mkdir -p /tmp/withcharacteristics
	rm -rf /tmp/withcharacteristics/*
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.SetCharacteristicsFromSummary \
			/tmp/testsetcharacteristics.json \
			/tmp/grayscale2017_nac_hncma_fp.dcm \
			/tmp/withcharacteristics \
		2>/tmp/setcharacteristics.log
	rm -f /tmp/filetouse.dat
	grep </tmp/setcharacteristics.log 'Writing with characteristics set file' | tail -1 | sed -e 's/^.*\/tmp/\/tmp/' >/tmp/filetouse.dat
	#@echo "Comparing header before and after characteristics added ..."
	#dcdiff /tmp/grayscale2017_nac_hncma_fp.dcm "`cat /tmp/filetouse.dat`"
	rm -f /tmp/grayscale2017_nac_hncma_fp.dcm
	mv "`cat /tmp/filetouse.dat`" /tmp/grayscale2017_nac_hncma_fp.dcm
	rm -rf /tmp/withcharacteristics
	rm /tmp/testsetcharacteristics.json
	rm /tmp/filetouse.dat
	rm /tmp/setcharacteristics.log
	# toIntTruncate produces spotty result :(
	java -Xmx512m -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.apps.IntegerScalingOfFloatingPointPixelData \
		toInt \
		/tmp/grayscale2017_nac_hncma_fp.dcm /tmp/grayscale2017_nac_hncma.dcm
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"remove\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"PatientOrientation\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"ContentLabel\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"ContentDescription\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"ContentCreatorName\" : null"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"removeall\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"ParametricMapFrameTypeSequence\" : null"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"top\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"PixelPresentation\" : \"MONOCHROME\","
	echo >>/tmp/testsetcharacteristics.json "		\"VolumetricProperties\" : \"VOLUME\","
	echo >>/tmp/testsetcharacteristics.json "		\"VolumeBasedCalculationTechnique\" : \"NONE\","
	echo >>/tmp/testsetcharacteristics.json "		\"SOPClassUID\" : \"1.2.840.10008.5.1.4.1.1.4.4\""
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"RealWorldValueMappingSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueFirstValueMapped\" : \"0\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueLastValueMapped\" : \"65535\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueIntercept\" : \"0\","
	echo >>/tmp/testsetcharacteristics.json "		\"RealWorldValueSlope\" : \"1\","
	echo >>/tmp/testsetcharacteristics.json "		\"LUTExplanation\" : \"Unitless\","
	echo >>/tmp/testsetcharacteristics.json "		\"LUTLabel\" : \"US\","
	echo >>/tmp/testsetcharacteristics.json "		\"MeasurementUnitsCodeSequence\" : { \"cv\" : \"1\", \"csd\" : \"UCUM\", \"cm\" : \"1\" }"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"MRImageFrameTypeSequence\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"FrameType\" : [ \"DERIVED\", \"PRIMARY\", \"PRE_CONTRAST\", \"NONE\" ],"
	echo >>/tmp/testsetcharacteristics.json "		\"PixelPresentation\" : \"MONOCHROME\","
	echo >>/tmp/testsetcharacteristics.json "		\"VolumetricProperties\" : \"VOLUME\","
	echo >>/tmp/testsetcharacteristics.json "		\"VolumeBasedCalculationTechnique\" : \"NONE\""
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	cat /tmp/testsetcharacteristics.json
	mkdir -p /tmp/withcharacteristics
	rm -rf /tmp/withcharacteristics/*
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.SetCharacteristicsFromSummary \
			/tmp/testsetcharacteristics.json \
			/tmp/grayscale2017_nac_hncma.dcm \
			/tmp/withcharacteristics \
		2>/tmp/setcharacteristics.log
	rm -f /tmp/filetouse.dat
	grep </tmp/setcharacteristics.log 'Writing with characteristics set file' | tail -1 | sed -e 's/^.*\/tmp/\/tmp/' >/tmp/filetouse.dat
	#@echo "Comparing header before and after characteristics added ..."
	#dcdiff /tmp/grayscale2017_nac_hncma.dcm "`cat /tmp/filetouse.dat`"
	rm -f /tmp/grayscale2017_nac_hncma.dcm
	mv "`cat /tmp/filetouse.dat`" /tmp/grayscale2017_nac_hncma.dcm
	rm -rf /tmp/withcharacteristics
	rm /tmp/testsetcharacteristics.json
	rm /tmp/filetouse.dat
	# need to give the integer version a different SOP Instance UID ...
	mv /tmp/grayscale2017_nac_hncma.dcm /tmp/grayscale2017_nac_hncma.dcm.bak
	dccp -d SOPInstanceUID -nodisclaimer /tmp/grayscale2017_nac_hncma.dcm.bak /tmp/grayscale2017_nac_hncma.dcm
	@echo "Comparing header before and after replacing SOP Instance UID added ..."
	dcdiff /tmp/grayscale2017_nac_hncma.dcm.bak /tmp/grayscale2017_nac_hncma.dcm
	rm /tmp/grayscale2017_nac_hncma.dcm.bak
	# now clean up floating point now that it has been used to make legacy converted MR ...
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"removeall\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"UnassignedSharedConvertedAttributesSequence\" : null,"
	echo >>/tmp/testsetcharacteristics.json "		\"UnassignedPerFrameConvertedAttributesSequence\" : null"
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	cat /tmp/testsetcharacteristics.json
	mkdir -p /tmp/withcharacteristics
	rm -rf /tmp/withcharacteristics/*
	java -Xmx512m -cp ${PATHTOROOT}:${DICOMADDITIONALJARS} \
		com.pixelmed.apps.SetCharacteristicsFromSummary \
			/tmp/testsetcharacteristics.json \
			/tmp/grayscale2017_nac_hncma_fp.dcm \
			/tmp/withcharacteristics \
		2>/tmp/setcharacteristics.log
	rm -f /tmp/filetouse.dat
	grep </tmp/setcharacteristics.log 'Writing with characteristics set file' | tail -1 | sed -e 's/^.*\/tmp/\/tmp/' >/tmp/filetouse.dat
	#@echo "Comparing header before and after characteristics added ..."
	#dcdiff /tmp/grayscale2017_nac_hncma_fp.dcm "`cat /tmp/filetouse.dat`"
	rm -f /tmp/grayscale2017_nac_hncma_fp.dcm
	mv "`cat /tmp/filetouse.dat`" /tmp/grayscale2017_nac_hncma_fp.dcm
	rm -rf /tmp/withcharacteristics
	rm /tmp/testsetcharacteristics.json
	rm /tmp/filetouse.dat
	rm /tmp/setcharacteristics.log

/tmp/labelmap2017_nac_hncma.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/brain-2017-01/volumes/labels/hncma-atlas.nrrd" \
		/tmp/labelmap2017_nac_hncma.dcm \
		"Brain^Atlas NAC HNCMA 2017" 2017 5678 101 1

/tmp/seg2017_nac_hncma.dcm:	IndexedLabelMapToSegmentation.class /tmp/labelmap2017_nac_hncma.dcm /tmp/grayscale2017_nac_hncma.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmap2017_nac_hncma.dcm \
		"$${HOME}/Documents/Work/NCI/Slicer/BrainAtlas/2013/BrainAtlas-label_list_fields_RGB.csv" \
		/tmp/seg2017_nac_hncma.dcm \
		/tmp/grayscale2017_nac_hncma.dcm \
		102 "Segmented Brain Head Neck Muscles" BRAIN_ATLAS_2017 "NAC HNCMA Brain Atlas 2017" "NAC^Halle et al"

testlabelmapbrainatlas2017_nac_hncma:	/tmp/seg2017_nac_hncma.dcm
	#dcentvfy /tmp/seg2017_nac_hncma.dcm /tmp/grayscale2017_nac_hncma.dcm

cleanlabelmapbrainatlas2017_nac:
	rm -f \
		/tmp/grayscale2017_nac_hncma.dcm \
		/tmp/grayscale2017_nac_hncma_fp.dcm \
		/tmp/labelmap2017_nac_hncma.dcm \
		/tmp/seg2017_nac_hncma.dcm

testlabelmapbrainatlas2017_nac:	/tmp/grayscale2017_nac_hncma.dcm /tmp/seg2017_nac_hncma.dcm
	@echo "Performing dcentvfy"
	dcentvfy \
		/tmp/grayscale2017_nac_hncma.dcm \
		/tmp/grayscale2017_nac_hncma_fp.dcm \
		/tmp/labelmap2017_nac_hncma.dcm \
		/tmp/seg2017_nac_hncma.dcm
	@echo "Performing dciodvfy"
	dciodvfy -filename /tmp/grayscale2017_nac_hncma.dcm
	#dciodvfy -filename /tmp/grayscale2017_nac_hncma_fp.dcm
	#dciodvfy -filename /tmp/labelmap2017_nac_hncma.dcm
	dciodvfy -filename /tmp/seg2017_nac_hncma.dcm

archivelabelmapbrainatlas2017_nac:
	rm -f /tmp/seg2017_nac_hncma.tar /tmp/seg2017_nac_hncma.tar.bz2
	(cd /tmp; tar -cvf seg2017_nac_hncma.tar \
		grayscale2017_nac_hncma.dcm \
		seg2017_nac_hncma.dcm \
	)
	bzip2 /tmp/seg2017_nac_hncma.tar



/tmp/grayscaleprostate.dcm:
	mkdir -p /tmp/grayscaleprostate
	rm -rf /tmp/grayscaleprostate/*
	java -Xms3g -Xmx3g -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR} com.pixelmed.dicom.MultiFrameImageFactory \
		"$${HOME}/Pictures/Medical/NCI-ISBI_2013_ProstateChallenge/TrainingFromTCIA/PROSTATE-DIAGNOSIS/ProstateDx-01-0006/1.3.6.1.4.1.14519.5.2.1.4792.2002.208539110710641195855467414500/000000" \
		/tmp/grayscaleprostate
	mv /tmp/grayscaleprostate/* /tmp/grayscaleprostate.dcm

/tmp/labelmapprostate.dcm:
	java -Xmx1g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.NRRDToDicom \
		"$${HOME}/Pictures/Medical/NCI-ISBI_2013_ProstateChallenge/ProstateDx-NRRD-T2W_TSE_AX-05-07-2012/ProstateDx-01-0006_T2W_TSE_AX.nrrd" \
		/tmp/labelmapprostate.dcm \
		ProstateDx-01-0006 ProstateDx-01-0006 "" 100 1

testllabelmapprostate:	IndexedLabelMapToSegmentation.class /tmp/labelmapprostate.dcm /tmp/grayscaleprostate.dcm
	java -Xms6g -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.IndexedLabelMapToSegmentation \
		/tmp/labelmapprostate.dcm \
		"$${HOME}/Pictures/Medical/NCI-ISBI_2013_ProstateChallenge/labelmapfortruth_diagnosis.csv" \
		/tmp/segprostate.dcm \
		/tmp/grayscaleprostate.dcm \
		102 "Segmented Prostate" PROSTATE_DIAG "Prostate Diagnosis Collection Segmentations" "ProstateDx-01-0006"
	#dcentvfy /tmp/segprostate.dcm /tmp/grayscaleprostate.dcm

/tmp/grayscalekidneylesion.dcm:
	mkdir -p /tmp/grayscalekidneylesion
	rm -rf /tmp/grayscalekidneylesion/*
	java -Xms3g -Xmx3g -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR} com.pixelmed.dicom.MultiFrameImageFactory \
		"$${HOME}/Pictures/Medical/TCIA/C4KC-KiTS/C4KC-KiTS/KiTS-00000/06-29-2003-threephaseabdomen-41748/7-arterial-66994" \
		/tmp/grayscalekidneylesion
	mv /tmp/grayscalekidneylesion/* /tmp/grayscalekidneylesion.dcm

/tmp/grayscalebrainlesion.dcm:
	mkdir -p /tmp/grayscalebrainlesion
	rm -rf /tmp/grayscalebrainlesion/*
	java -Xms3g -Xmx3g -cp ${PATHTOROOT}:${COMMONSCODECADDITIONALJAR} com.pixelmed.dicom.MultiFrameImageFactory \
		"$${HOME}/Pictures/Medical/Segmentations/TCGA-06-0119/12-26-2003-90543/7-AxFLAIR-thin for surgery-06923" \
		/tmp/grayscalebrainlesion
	mv /tmp/grayscalebrainlesion/* /tmp/grayscalebrainlesion.dcm

testrawformatdump:	RawImageDescription.class
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} \
		com.pixelmed.convert.RawImageDescription \
		/Volumes/Toshiba5TEnc/Pictures/Medical/GESpeedyCTImageDeliveryChallenge/Apple_32bit_raw_09.json

testrawfloattodicom:	RawImageDescription.class RawToDicomMultiFrame.class
	rm -f /tmp/testrawfloattodicom.raw /tmp/testrawfloattodicom.dcm
	cat /Volumes/Toshiba5TEnc/Pictures/Medical/GESpeedyCTImageDeliveryChallenge/Apple_32bit_raw_09/*.raw >/tmp/testrawfloattodicom.raw
	java -Xms4g -cp ${PATHTOROOT}:${JSONADDITIONALJAR} \
		com.pixelmed.convert.RawToDicomMultiFrame \
		/Volumes/Toshiba5TEnc/Pictures/Medical/GESpeedyCTImageDeliveryChallenge/Apple_32bit_raw_09.json \
		/tmp/testrawfloattodicom.raw \
		/tmp/testrawfloattodicom.dcm \
		"Apple_09" 1234 5678 100 1
	rm -f /tmp/testrawfloattodicom.raw

testencapsulatepdfsmallevenlength:		EncapsulateData.class UnencapsulateData.class
	rm -f /tmp/crap.pdf /tmp/crap.dcm /tmp/crap_rt.pdf
	cp ../../../docs/DicomImageViewer/ReleaseNotes.pdf /tmp/crap.pdf
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.EncapsulateData \
		/tmp/crap.pdf \
		/tmp/crap.dcm
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.UnencapsulateData=debug \
		com.pixelmed.convert.UnencapsulateData \
		/tmp/crap.dcm \
		/tmp/crap_rt.pdf
	ls -l /tmp/crap.pdf /tmp/crap_rt.pdf
	@echo "Comparing round trip"
	cmp /tmp/crap.pdf /tmp/crap_rt.pdf
	dciodvfy /tmp/crap.dcm

testencapsulatepdfsmalloddlength:		EncapsulateData.class UnencapsulateData.class
	rm -f /tmp/crap.pdf /tmp/crap.dcm /tmp/crap_rt.pdf
	cp "$${HOME}/Documents/Medical/cp/cp052_ft.pdf" /tmp/crap.pdf
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.EncapsulateData \
		/tmp/crap.pdf \
		/tmp/crap.dcm
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.UnencapsulateData=debug \
		com.pixelmed.convert.UnencapsulateData \
		/tmp/crap.dcm \
		/tmp/crap_rt.pdf
	ls -l /tmp/crap.pdf /tmp/crap_rt.pdf
	@echo "Comparing round trip"
	cmp /tmp/crap.pdf /tmp/crap_rt.pdf
	dciodvfy /tmp/crap.dcm

testencapsulatepdflargeevenlength:		EncapsulateData.class UnencapsulateData.class
	rm -f /tmp/crap.pdf /tmp/crap.dcm /tmp/crap_rt.pdf
	cp "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/2019a/output/pdf/part03_changes.pdf" /tmp/crap.pdf
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.EncapsulateData \
		/tmp/crap.pdf \
		/tmp/crap.dcm
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.UnencapsulateData=debug \
		com.pixelmed.convert.UnencapsulateData \
		/tmp/crap.dcm \
		/tmp/crap_rt.pdf
	ls -l /tmp/crap.pdf /tmp/crap_rt.pdf
	@echo "Comparing round trip"
	cmp /tmp/crap.pdf /tmp/crap_rt.pdf
	dciodvfy /tmp/crap.dcm

testencapsulatepdflargeoddlength:		EncapsulateData.class UnencapsulateData.class
	rm -f /tmp/crap.pdf /tmp/crap.dcm /tmp/crap_rt.pdf
	cp "$${HOME}/Documents/Medical/stuff/medical.nema.org/MEDICAL/Dicom/2019a/output/pdf/part03.pdf" /tmp/crap.pdf
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.EncapsulateData \
		/tmp/crap.pdf \
		/tmp/crap.dcm
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.UnencapsulateData=debug \
		com.pixelmed.convert.UnencapsulateData \
		/tmp/crap.dcm \
		/tmp/crap_rt.pdf
	ls -l /tmp/crap.pdf /tmp/crap_rt.pdf
	@echo "Comparing round trip"
	cmp /tmp/crap.pdf /tmp/crap_rt.pdf
	dciodvfy /tmp/crap.dcm

testencapsulatepdfwithmetadata:		EncapsulateData.class UnencapsulateData.class
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"options\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"AppendToContributingEquipmentSequence\" : false"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"top\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"DocumentTitle\" : \"Release Notes for DicomImageViewer\","
	echo >>/tmp/testsetcharacteristics.json "		\"ConceptNameCodeSequence\" : { \"cv\" : \"18748-4\", \"csd\" : \"LN\", \"cm\" : \"Diagnostic Imaging Report\" }"
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	#cat /tmp/testsetcharacteristics.json
	rm -f /tmp/crap_pdf_meta.dcm
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.EncapsulateData \
		../../../docs/DicomImageViewer/ReleaseNotes.pdf \
		/tmp/testsetcharacteristics.json \
		/tmp/crap_pdf_meta.dcm
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.UnencapsulateData \
		/tmp/crap_pdf_meta.dcm \
		/tmp/crap_pdf_meta.pdf
	ls -l ../../../docs/DicomImageViewer/ReleaseNotes.pdf /tmp/crap_pdf_meta.pdf
	@echo "Comparing round trip"
	cmp ../../../docs/DicomImageViewer/ReleaseNotes.pdf /tmp/crap_pdf_meta.pdf
	dciodvfy /tmp/crap_pdf_meta.dcm

testencapsulatepdfwithmetadatacompctx:		EncapsulateData.class UnencapsulateData.class
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"options\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"AppendToContributingEquipmentSequence\" : false"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"top\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"DocumentTitle\" : \"Release Notes for DicomImageViewer\","
	echo >>/tmp/testsetcharacteristics.json "		\"ConceptNameCodeSequence\" : { \"cv\" : \"18748-4\", \"csd\" : \"LN\", \"cm\" : \"Diagnostic Imaging Report\" }"
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	#cat /tmp/testsetcharacteristics.json
	dcsmpte /tmp/crap_patientstudy.dcm -r PatientName "Composite^Person" -r PatientID 123456 -r AccessionNumber 789012 -r StudyID 82347 -r StudyDescription "Really good study"
	rm -f /tmp/crap_pdf_meta_compctx.dcm
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.EncapsulateData \
		../../../docs/DicomImageViewer/ReleaseNotes.pdf \
		/tmp/testsetcharacteristics.json \
		/tmp/crap_patientstudy.dcm \
		/tmp/crap_pdf_meta_compctx.dcm
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.UnencapsulateData \
		/tmp/crap_pdf_meta_compctx.dcm \
		/tmp/crap_pdf_meta_compctx.pdf
	ls -l ../../../docs/DicomImageViewer/ReleaseNotes.pdf /tmp/crap_pdf_meta_compctx.pdf
	@echo "Comparing round trip"
	cmp ../../../docs/DicomImageViewer/ReleaseNotes.pdf /tmp/crap_pdf_meta_compctx.pdf
	dciodvfy /tmp/crap_pdf_meta_compctx.dcm

testencapsulatestl:		EncapsulateData.class UnencapsulateData.class
	echo  >/tmp/testsetcharacteristics.json "{"
	echo >>/tmp/testsetcharacteristics.json "	\"options\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"AppendToContributingEquipmentSequence\" : false"
	echo >>/tmp/testsetcharacteristics.json "	},"
	echo >>/tmp/testsetcharacteristics.json "	\"top\" : {"
	echo >>/tmp/testsetcharacteristics.json "		\"PatientID\" : \"3DPX-008530\","
	echo >>/tmp/testsetcharacteristics.json "		\"PatientName\" : \"Tetralogy of Fallot^Contegra RV-PA conduit\","
	echo >>/tmp/testsetcharacteristics.json "		\"StudyDate\" : \"20180111\","
	echo >>/tmp/testsetcharacteristics.json "		\"StudyTime\" : \"110900\","
	echo >>/tmp/testsetcharacteristics.json "		\"MeasurementUnitsCodeSequence\" : { \"cv\" : \"mm\", \"csd\" : \"UCUM\", \"cm\" : \"mm\" },"
	echo >>/tmp/testsetcharacteristics.json "		\"DocumentTitle\" : \"Tetralogy of Fallot, status-post pulmonary valve replacement with a Contegra RV-PA conduit\","
	echo >>/tmp/testsetcharacteristics.json "		\"ConceptNameCodeSequence\" : { \"cv\" : \"85041-2\", \"csd\" : \"LN\", \"cm\" : \"MR 3D CAM model\" },"
	echo >>/tmp/testsetcharacteristics.json "		\"ModelModification\" : \"NO\","
	echo >>/tmp/testsetcharacteristics.json "		\"ModelMirroring\" : \"NO\","
	echo >>/tmp/testsetcharacteristics.json "		\"ModelUsageCodeSequence\" : { \"cv\" : \"129012\", \"csd\" : \"DCM\", \"cm\" : \"Educational Intent\" },"
	echo >>/tmp/testsetcharacteristics.json "		\"ContentDescription\" : \"http://3dprint.nih.gov/discover/3dpx-008530\""
	echo >>/tmp/testsetcharacteristics.json "	}"
	echo >>/tmp/testsetcharacteristics.json "}"
	#cat /tmp/testsetcharacteristics.json
	rm -f /tmp/crap_stl.dcm
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.EncapsulateData \
		"$${HOME}/Pictures/Medical/3DPrinting/NIH/Contegra.stl" \
		/tmp/testsetcharacteristics.json \
		/tmp/crap_stl.dcm
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.UnencapsulateData \
		/tmp/crap_stl.dcm \
		/tmp/crap_stl
	ls -l "$${HOME}/Pictures/Medical/3DPrinting/NIH/Contegra.stl" /tmp/crap_stl
	@echo "Comparing round trip"
	cmp "$${HOME}/Pictures/Medical/3DPrinting/NIH/Contegra.stl" /tmp/crap_stl
	dciodvfy /tmp/crap_stl.dcm

testbigtiffifd:	TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.TIFFImageFileDirectories \
		"$${HOME}/Pictures/Medical/WSI_More/HistoGeneX/Tiff/Brightfield/HistoGeneX_04050732B0018S.tif"

testtiffifdsmpte8:	TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	dcsmpte -tiff /tmp/crap8.dcm
	hexdump -C /tmp/crap8.dcm | head -1
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.TIFFImageFileDirectories \
		/tmp/crap8.dcm
	dcsmpte -tiff -output-endian big -output-vr explicit /tmp/crap8.dcm
	hexdump -C /tmp/crap8.dcm | head -1
	java -cp ${PATHTOROOT} -Djava.awt.headless=true\
		com.pixelmed.convert.TIFFImageFileDirectories \
		/tmp/crap8.dcm

testbigtiffifdsamples:	TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	for f in $${HOME}/Documents/Medical/stuff/Formats/TIFF/BigTIFF/BigTIFFSamples/*.tif; \
	do \
		echo "$$f"; \
		java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.TIFFImageFileDirectories "$$f"; \
	done

/tmp/testtifftodicom.json:	Makefile
	echo  >/tmp/testtifftodicom.json "{"
	echo >>/tmp/testtifftodicom.json "	\"top\" : {"
	echo >>/tmp/testtifftodicom.json "		\"PatientName\" : \"Test\","
	echo >>/tmp/testtifftodicom.json "		\"PatientID\" : \"1234\","
	echo >>/tmp/testtifftodicom.json "		\"StudyID\" : \"5678\","
	echo >>/tmp/testtifftodicom.json "		\"SeriesNumber\" : \"1\""
	echo >>/tmp/testtifftodicom.json "	}"
	echo >>/tmp/testtifftodicom.json "}"

testclassictifftodicom:	/tmp/testtifftodicom.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.TIFFImageFileDirectories \
		$${HOME}/Documents/Medical/stuff/Formats/TIFF/BigTIFF/BigTIFFSamples/Classic.tif
	mkdir -p /tmp/Classic_SingleFrame
	rm -f /tmp/Classic_SingleFrame/*.dcm
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/testtifftodicom.json \
		$${HOME}/Documents/Medical/stuff/Formats/TIFF/BigTIFF/BigTIFFSamples/Classic.tif \
		/tmp/Classic_SingleFrame/img
	rm -f /tmp/classtifftodicom.json

testbigtifftilestodicom:	/tmp/testtifftodicom.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.TIFFImageFileDirectories \
		$${HOME}/Documents/Medical/stuff/Formats/TIFF/BigTIFF/BigTIFFSamples/BigTIFFLong8Tiles.tif
	mkdir -p /tmp/BigTIFFLong8Tiles
	rm -f /tmp/BigTIFFLong8Tiles/*.dcm
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/testtifftodicom.json \
		$${HOME}/Documents/Medical/stuff/Formats/TIFF/BigTIFF/BigTIFFSamples/BigTIFFLong8Tiles.tif \
		/tmp/BigTIFFLong8Tiles/img
	rm -f /tmp/classtifftodicom.json

testbigtifftilestodicommf:	/tmp/testtifftodicom.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		com.pixelmed.convert.TIFFImageFileDirectories \
		$${HOME}/Documents/Medical/stuff/Formats/TIFF/BigTIFF/BigTIFFSamples/BigTIFFLong8Tiles.tif
	mkdir -p /tmp/BigTIFFLong8Tiles
	rm -f /tmp/BigTIFFLong8Tiles/*.dcm
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/testtifftodicom.json \
		$${HOME}/Documents/Medical/stuff/Formats/TIFF/BigTIFF/BigTIFFSamples/BigTIFFLong8Tiles.tif \
		/tmp/BigTIFFLong8Tiles/img \
		SM 1.2.840.10008.5.1.4.1.1.7.4
	rm -f /tmp/classtifftodicom.json
	ls -l /tmp/BigTIFFLong8Tiles/*

/tmp/bigtiffuncompressedtodicom.json:	Makefile
	echo  >/tmp/bigtiffuncompressedtodicom.json "{"
	echo >>/tmp/bigtiffuncompressedtodicom.json "	\"top\" : {"
	echo >>/tmp/bigtiffuncompressedtodicom.json "		\"PatientName\" : \"UncompressedOriginal^455601\","
	echo >>/tmp/bigtiffuncompressedtodicom.json "		\"PatientID\" : \"455601\","
	echo >>/tmp/bigtiffuncompressedtodicom.json "		\"StudyID\" : \"234\","
	echo >>/tmp/bigtiffuncompressedtodicom.json "		\"SeriesNumber\" : \"1000\""
	echo >>/tmp/bigtiffuncompressedtodicom.json "	}"
	echo >>/tmp/bigtiffuncompressedtodicom.json "}"

testbigtiffuncompressedtodicom:	/tmp/bigtiffuncompressedtodicom.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/HistoGeneX_04050732B0018S_SingleFrame
	rm -f /tmp/HistoGeneX_04050732B0018S_SingleFrame/*
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffuncompressedtodicom.json \
		"$${HOME}/Pictures/Medical/WSI/WSI_Uncompressed_Leeds/455601.tif" \
		"/tmp/HistoGeneX_04050732B0018S_SingleFrame/img" \
		SM
	rm -f /tmp/bigtiffuncompressedtodicom.json

testbigtiffuncompressedtodicommf:	/tmp/bigtiffuncompressedtodicom.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	rm -rf /tmp/WSI_MultiFrame_tmp
	mkdir -p /tmp/WSI_MultiFrame_tmp
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true -Djava.io.tmpdir=/tmp/WSI_MultiFrame_tmp \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.TiledPyramid=debug \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffuncompressedtodicom.json \
		"/Volumes/SeagatePort5TBNonEnc/Pictures/Medical/WSI_HTAN_Related/Synapse_Rashid/LUNG-1-LN/40X/ometiff/LUNG-1-LN_40X.ome.tif" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 \
		1.2.840.10008.1.2.1 \
		ADDTIFF ADDPYRAMID
	rm -f /tmp/bigtiffuncompressedtodicom.json
	rm -r /tmp/WSI_MultiFrame_tmp
	ls -l /tmp/WSI_MultiFrame/*

testbigtiffuncompressedtodicommfjpeg:	/tmp/bigtiffuncompressedtodicom.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	rm -rf /tmp/WSI_MultiFrame_tmp
	mkdir -p /tmp/WSI_MultiFrame_tmp
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true -Djava.io.tmpdir=/tmp/WSI_MultiFrame_tmp \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.TiledPyramid=debug \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffuncompressedtodicom.json \
		"$${HOME}/Pictures/Medical/WSI/WSI_Uncompressed_Leeds/455601.tif" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 \
		1.2.840.10008.1.2.4.50 \
		ADDTIFF ADDPYRAMID
	rm -f /tmp/bigtiffuncompressedtodicom.json
	rm -r /tmp/WSI_MultiFrame_tmp
	ls -l /tmp/WSI_MultiFrame/*

testbigtiffuncompressedtodicommfj2klossless:	/tmp/bigtiffuncompressedtodicom.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	rm -rf /tmp/WSI_MultiFrame_tmp
	mkdir -p /tmp/WSI_MultiFrame_tmp
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true -Djava.io.tmpdir=/tmp/WSI_MultiFrame_tmp \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.TiledPyramid=debug \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffuncompressedtodicom.json \
		"$${HOME}/Pictures/Medical/WSI/WSI_Uncompressed_Leeds/455601.tif" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 \
		1.2.840.10008.1.2.4.90 \
		ADDTIFF ADDPYRAMID
	rm -f /tmp/bigtiffuncompressedtodicom.json
	rm -r /tmp/WSI_MultiFrame_tmp
	ls -l /tmp/WSI_MultiFrame/*

	#"/Volumes/SeagatePort5TBNonEnc/Pictures/Medical/WSI_HTAN_Related/Synapse_Rashid/LUNG-1-LN/40X/ometiff/LUNG-1-LN_40X.ome.tif"
	#"$${HOME}/Pictures/Medical/WSI/WSI_Uncompressed_Leeds/455601.tif" ... too big to store as uncompressed DICOM (1.2.840.10008.1.2.1)
	#"$${HOME}/Pictures/Medical/WSI_More/3DHISTECH/Uncompressed_AsTIFF/BF Lossless Slide 01 40x_Wholeslide_Default_Extended.tif" ... too big to store as uncompressed DICOM (1.2.840.10008.1.2.1)
	#"$${HOME}/Pictures/Medical/WSI_More/HistoGeneX/Tiff/Brightfield/HistoGeneX_04050732B0018S.tif"
	#"$${HOME}/Pictures/Medical/WSI_More/HistoGeneX/Tiff/Brightfield/HistoGeneX_04050736B0043S.tif"
	#"$${HOME}/Pictures/Medical/WSI_More/HistoGeneX/Tiff/Brightfield/HistoGeneX_04050736B0045S.tif"


/tmp/bigtiffjpegtodicom.json:	Makefile
	echo  >/tmp/bigtiffjpegtodicom.json "{"
	echo >>/tmp/bigtiffjpegtodicom.json "	\"top\" : {"
	echo >>/tmp/bigtiffjpegtodicom.json "		\"PatientName\" : \"8917\","
	echo >>/tmp/bigtiffjpegtodicom.json "		\"PatientID\" : \"8917\","
	echo >>/tmp/bigtiffjpegtodicom.json "		\"StudyID\" : \"234\","
	echo >>/tmp/bigtiffjpegtodicom.json "		\"SeriesNumber\" : \"1000\""
	echo >>/tmp/bigtiffjpegtodicom.json "	}"
	echo >>/tmp/bigtiffjpegtodicom.json "}"

testbigtiffjpegtodicomsf:	/tmp/bigtiffjpegtodicom.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_SingleFrame
	rm -f /tmp/WSI_SingleFrame/*
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffjpegtodicom.json \
		"$${HOME}/Pictures/Medical/WSI/Aperio/8917.svs" \
		"/tmp/WSI_SingleFrame/img" \
		SM
	rm -f /tmp/bigtiffjpegtodicom.json

/tmp/bigtiffjpegtodicommf.json:	Makefile
	# use simplified description and identification from PS3.17 Annex NN example, even though the tissue is not actually lung ...
	echo  >/tmp/bigtiffjpegtodicommf.json "{"
	echo >>/tmp/bigtiffjpegtodicommf.json "	\"top\" : {"
	echo >>/tmp/bigtiffjpegtodicommf.json "		\"PatientName\" : \"PixelMed^AperioCMU-1\","
	echo >>/tmp/bigtiffjpegtodicommf.json "		\"PatientID\" : \"PX7832548325932\","
	echo >>/tmp/bigtiffjpegtodicommf.json "		\"StudyID\" : \"S07-100\","
	echo >>/tmp/bigtiffjpegtodicommf.json "		\"SeriesNumber\" : \"1\","
	echo >>/tmp/bigtiffjpegtodicommf.json "		\"AccessionNumber\" : \"S07-100\","
	echo >>/tmp/bigtiffjpegtodicommf.json "		\"ContainerIdentifier\" : \"S07-100 A 5 1\","
	echo >>/tmp/bigtiffjpegtodicommf.json "		\"IssuerOfTheContainerIdentifierSequence\" : [],"
	echo >>/tmp/bigtiffjpegtodicommf.json "		\"ContainerTypeCodeSequence\" : { \"cv\" : \"433466003\", \"csd\" : \"SCT\", \"cm\" : \"Microscope slide\" },"
	echo >>/tmp/bigtiffjpegtodicommf.json "		\"SpecimenDescriptionSequence\" : ["
	echo >>/tmp/bigtiffjpegtodicommf.json "	      {"
	echo >>/tmp/bigtiffjpegtodicommf.json "		    \"SpecimenIdentifier\" : \"S07-100 A 5 1\","
	echo >>/tmp/bigtiffjpegtodicommf.json "		    \"IssuerOfTheSpecimenIdentifierSequence\" : [],"
	echo >>/tmp/bigtiffjpegtodicommf.json "		    \"SpecimenUID\" : \"1.2.840.99790.986.33.1677.1.1.19.5\","
	echo >>/tmp/bigtiffjpegtodicommf.json "		    \"SpecimenShortDescription\" : \"Part A: LEFT UPPER LOBE, Block 5: Mass (2 pc), Slide 1: H&E\","
	echo >>/tmp/bigtiffjpegtodicommf.json "		    \"SpecimenDetailedDescription\" : \"A: Received fresh for intraoperative consultation, labeled with the patient's name, number and 'left upper lobe,' is a pink-tan, wedge-shaped segment of soft tissue, 6.9 x 4.2 x 1.0 cm. The pleural surface is pink-tan and glistening with a stapled line measuring 12.0 cm. in length. The pleural surface shows a 0.5 cm. area of puckering. The pleural surface is inked black. The cut surface reveals a 1.2 x 1.1 cm, white-gray, irregular mass abutting the pleural surface and deep to the puckered area. The remainder of the cut surface is red-brown and congested. No other lesions are identified. Representative sections are submitted. Block 5: 'Mass' (2 pieces)\","
	echo >>/tmp/bigtiffjpegtodicommf.json "		    \"SpecimenPreparationSequence\" : ["
	echo >>/tmp/bigtiffjpegtodicommf.json "		      {"
	echo >>/tmp/bigtiffjpegtodicommf.json "			    \"SpecimenPreparationStepContentItemSequence\" : ["
	echo >>/tmp/bigtiffjpegtodicommf.json "			      {"
	echo >>/tmp/bigtiffjpegtodicommf.json "		    		\"ValueType\" : \"TEXT\","
	echo >>/tmp/bigtiffjpegtodicommf.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"121041\", \"csd\" : \"DCM\", \"cm\" : \"Specimen Identifier\" },"
	echo >>/tmp/bigtiffjpegtodicommf.json "		    		\"TextValue\" : \"S07-100 A 5 1\""
	echo >>/tmp/bigtiffjpegtodicommf.json "			      },"
	echo >>/tmp/bigtiffjpegtodicommf.json "			      {"
	echo >>/tmp/bigtiffjpegtodicommf.json "		    		\"ValueType\" : \"CODE\","
	echo >>/tmp/bigtiffjpegtodicommf.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"111701\", \"csd\" : \"DCM\", \"cm\" : \"Processing type\" },"
	echo >>/tmp/bigtiffjpegtodicommf.json "					\"ConceptCodeSequence\" :     { \"cv\" : \"127790008\", \"csd\" : \"SCT\", \"cm\" : \"Staining\" }"
	echo >>/tmp/bigtiffjpegtodicommf.json "			      },"
	echo >>/tmp/bigtiffjpegtodicommf.json "			      {"
	echo >>/tmp/bigtiffjpegtodicommf.json "		    		\"ValueType\" : \"CODE\","
	echo >>/tmp/bigtiffjpegtodicommf.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"424361007\", \"csd\" : \"SCT\", \"cm\" : \"Using substance\" },"
	echo >>/tmp/bigtiffjpegtodicommf.json "					\"ConceptCodeSequence\" :     { \"cv\" : \"12710003\", \"csd\" : \"SCT\", \"cm\" : \"hematoxylin stain\" }"
	echo >>/tmp/bigtiffjpegtodicommf.json "			      },"
	echo >>/tmp/bigtiffjpegtodicommf.json "			      {"
	echo >>/tmp/bigtiffjpegtodicommf.json "		    		\"ValueType\" : \"CODE\","
	echo >>/tmp/bigtiffjpegtodicommf.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"424361007\", \"csd\" : \"SCT\", \"cm\" : \"Using substance\" },"
	echo >>/tmp/bigtiffjpegtodicommf.json "					\"ConceptCodeSequence\" :     { \"cv\" : \"36879007\", \"csd\" : \"SCT\", \"cm\" : \"water soluble eosin stain\" }"
	echo >>/tmp/bigtiffjpegtodicommf.json "			      }"
	echo >>/tmp/bigtiffjpegtodicommf.json "			    ]"
	echo >>/tmp/bigtiffjpegtodicommf.json "		      }"
	echo >>/tmp/bigtiffjpegtodicommf.json "		    ],"
	echo >>/tmp/bigtiffjpegtodicommf.json "		    \"PrimaryAnatomicStructureSequence\" : { \"cv\" : \"44714003\", \"csd\" : \"SCT\", \"cm\" : \"Left Upper Lobe of Lung\" }"
	echo >>/tmp/bigtiffjpegtodicommf.json "	      }"
	echo >>/tmp/bigtiffjpegtodicommf.json "		],"
	echo >>/tmp/bigtiffjpegtodicommf.json "		\"OpticalPathSequence\" : ["
	echo >>/tmp/bigtiffjpegtodicommf.json "	      {"
	echo >>/tmp/bigtiffjpegtodicommf.json "		    \"OpticalPathIdentifier\" : \"1\","
	echo >>/tmp/bigtiffjpegtodicommf.json "		    \"IlluminationColorCodeSequence\" : { \"cv\" : \"414298005\", \"csd\" : \"SCT\", \"cm\" : \"Full Spectrum\" },"
	echo >>/tmp/bigtiffjpegtodicommf.json "		    \"IlluminationTypeCodeSequence\" :  { \"cv\" : \"111744\",  \"csd\" : \"DCM\", \"cm\" : \"Brightfield illumination\" }"
	echo >>/tmp/bigtiffjpegtodicommf.json "	      }"
	echo >>/tmp/bigtiffjpegtodicommf.json "		]"
	echo >>/tmp/bigtiffjpegtodicommf.json "	}"
	echo >>/tmp/bigtiffjpegtodicommf.json "}"

testbigtiffjpegtodicommf:	/tmp/bigtiffjpegtodicommf.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR}:${COMMONSCOMPRESSADDITIONALJAR} -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFImageFileDirectories=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dicom.CompressedFrameDecoder=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dicom.OtherByteAttributeMultipleCompressedFrames=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.display.SourceImage=info \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffjpegtodicommf.json \
		"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1.svs" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6
	rm -f /tmp/bigtiffjpegtodicommf.json
	ls -l /tmp/WSI_MultiFrame/*

testbigtiffjpegtodicommfauto:	/tmp/bigtiffjpegtodicommf.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR}:${COMMONSCOMPRESSADDITIONALJAR} -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFImageFileDirectories=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dicom.CompressedFrameDecoder=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dicom.OtherByteAttributeMultipleCompressedFrames=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.display.SourceImage=info \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffjpegtodicommf.json \
		"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1.svs" \
		"/tmp/WSI_MultiFrame/img"
	rm -f /tmp/bigtiffjpegtodicommf.json
	ls -l /tmp/WSI_MultiFrame/*

testbigtiffjpegtodicommfaddtiff:	/tmp/bigtiffjpegtodicommf.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dicom.CompressedFrameDecoder=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dicom.OtherByteAttributeMultipleCompressedFrames=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.display.SourceImage=info \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffjpegtodicommf.json \
		"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1.svs" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 \
		ADDTIFF
	rm -f /tmp/bigtiffjpegtodicommf.json
	ls -l /tmp/WSI_MultiFrame/*

testbigtiffjpegtodicommfaddtiffandpyramid:	/tmp/bigtiffjpegtodicommf.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dicom.CompressedFrameDecoder=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dicom.OtherByteAttributeMultipleCompressedFrames=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.display.SourceImage=info \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffjpegtodicommf.json \
		"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1.svs" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 \
		ADDTIFF ADDPYRAMID
	rm -f /tmp/bigtiffjpegtodicommf.json
	ls -l /tmp/WSI_MultiFrame/*

testbigtiffjpegtodicommfaddtiffandpyramidthenreaddtiff:	testbigtiffjpegtodicommfaddtiffandpyramid
	rm -rf /tmp/WSI_MultiFrame_TIFFagain.dcm
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} \
		-Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.AddTIFFOrOffsetTables=info \
		com.pixelmed.convert.AddTIFFOrOffsetTables \
		/tmp/WSI_MultiFrame/img_0.dcm \
		/tmp/WSI_MultiFrame_TIFFagain.dcm \
		ADDTIFF
	#echo comparing
	cmp /tmp/WSI_MultiFrame/img_0.dcm /tmp/WSI_MultiFrame_TIFFagain.dcm
	#echo comparison done
	#rm /tmp/WSI_MultiFrame_TIFFagain.dcm

testpyramidsmadefromdicommf:
	mkdir -p /tmp/pyramid
	rm -rf /tmp/pyramid/*
	java -Djava.awt.headless=true -XX:-UseGCOverheadLimit -Xmx2g \
		-cp ${PATHTOROOT}:${JIIOADDITIONALJARS} \
		-Dorg.slf4j.simpleLogger.showDateTime=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.apps.TiledPyramid=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dicom.AttributeList=info \
		com.pixelmed.apps.TiledPyramid \
		"/tmp/WSI_MultiFrame/img_0.dcm" \
		/tmp/pyramid
	find /tmp/pyramid/1.* -exec dctable -recurse -decimal -k ImageType -k TotalPixelMatrixRows -k TotalPixelMatrixColumns -k PixelSpacing -k Rows -k Columns '{}' ';'
	find /tmp/pyramid/1.* -exec ls -l '{}' ';'
	dcentvfy /tmp/pyramid/1.*

testbigtiffjpegtilesdigest:	TIFFPixelMessageDigest.class TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFPixelMessageDigest=debug \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.dicom.CompressedFrameDecoder=info \
		com.pixelmed.convert.TIFFPixelMessageDigest \
		"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1.svs"

		#"$${HOME}/Pictures/Medical/WSI/Aperio/8917.svs"
		#"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1.svs"
		#"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1-Small-Region.svs"
		#"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1-JP2K-33005.svs"
		#"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/JP2K-33003-1.svs"
		#"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Generic-TIFF/CMU-1.tiff"
		#"$${HOME}/Pictures/Medical/WSI_More/TIL/gdc_download_20180518_121436/18cf46a6-a344-43dc-807a-c92bcd085ed8/TCGA-CF-A47X-01A-03-TSC.A7484F9B-2ECD-41EF-ACE4-66DFB432D58C.svs"
		#"$${HOME}/Pictures/Medical/WSI_More/TIL/gdc_download_20180518_121436/18cf46a6-a344-43dc-807a-c92bcd085ed8/TCGA-CF-A47X-01A-03-TSC.A7484F9B-2ECD-41EF-ACE4-66DFB432D58C.svs"
		#"/Volumes/Elements4TBEnc2/Pictures/Medical/WSI/PESO/peso_training_wsi_1/pds_2_HE.tif"
		#"/Volumes/SeagatePort5TBNonEnc/Pictures/Medical/WSI_More/WSI_Leeds/25/SS45002_LH20-13000_1_1_1_083408.svs"
		#"$${HOME}/Documents/Clients/BWH/IDC/HTAN/ActualHTAN/HT190P1 H7.svs"	# LZW label too many bytes when decompressed (001329)
		#"$${HOME}/Pictures/Medical/WSI/Grundium/Anonymized-Colon.svs"
		#/Volumes/Elements5TBNonEncD/Pictures/Medical/WSI/RMS_DONOTSHARE/Test images of IDC/PANJXS-0BMX7D_A2.tif
		#/Volumes/Elements5TBNonEncE/Pictures/Medical/WSI/CAMELYON/CAMELYON16/images/normal_001.tif
		#/Volumes/Elements5TBNonEncE/Pictures/Medical/WSI/CAMELYON/CAMELYON16/images/normal_101.tif

testbigtiffjpegtodicommfdigestafter:
	ls -l /tmp/WSI_MultiFrame/*
	for i in /tmp/WSI_MultiFrame/*.dcm; do \
		echo "Computing digest for $$i"; \
		java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
			-Dorg.slf4j.simpleLogger.log.com.pixelmed.display.SourceImage=info \
			com.pixelmed.apps.PixelMessageDigest \
			"$$i"; \
	done

testbigtiffjpegtodicommfjpeg:	/tmp/bigtiffjpegtodicommf.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true -Djava.io.tmpdir=/tmp \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffjpegtodicommf.json \
		"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1.svs" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 1.2.840.10008.1.2.4.50
	rm -f /tmp/bigtiffjpegtodicommf.json
	ls -l /tmp/WSI_MultiFrame/*

testbigtiffjpegtodicommfjpegaddtiff:	/tmp/bigtiffjpegtodicommf.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true -Djava.io.tmpdir=/tmp \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffjpegtodicommf.json \
		"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1.svs" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 1.2.840.10008.1.2.4.50 \
		ADDTIFF
	rm -f /tmp/bigtiffjpegtodicommf.json
	ls -l /tmp/WSI_MultiFrame/*

testbigtiffjpegtodicommfjpegaddtiffnobigtiff:	/tmp/bigtiffjpegtodicommf.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true -Djava.io.tmpdir=/tmp \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffjpegtodicommf.json \
		"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1.svs" \
		"/tmp/WSI_MultiFrame/" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 \
		1.2.840.10008.1.2.4.50 \
		ADDTIFF DONOTUSEBIGTIFF
	rm -f /tmp/bigtiffjpegtodicommf.json
	ls -l /tmp/WSI_MultiFrame/*

testbigtiffjpegtodicommfj2klossless:	/tmp/bigtiffjpegtodicommf.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true -Djava.io.tmpdir=/tmp \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffjpegtodicommf.json \
		"$${HOME}/Pictures/Medical/WSI/WSI_Uncompressed_Leeds/455601.tif" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 \
		1.2.840.10008.1.2.4.90 \
		ADDTIFF USEBIGTIFF
	rm -f /tmp/bigtiffjpegtodicommf.json
	ls -l /tmp/WSI_MultiFrame/*

testbigtiffjpegtodicommfj2klossy:	/tmp/bigtiffjpegtodicommf.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true -Djava.io.tmpdir=/tmp \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffjpegtodicommf.json \
		"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1.svs" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 \
		1.2.840.10008.1.2.4.91 \
		ADDTIFF DONOTUSEBIGTIFF
	rm -f /tmp/bigtiffjpegtodicommf.json
	ls -l /tmp/WSI_MultiFrame/*

testbigtiffj2ktodicommfjpeg:	/tmp/bigtiffjpegtodicommf.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true -Djava.io.tmpdir=/tmp \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffjpegtodicommf.json \
		"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1-JP2K-33005.svs" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 \
		1.2.840.10008.1.2.4.50 \
		ADDTIFF DONOTUSEBIGTIFF
	rm -f /tmp/bigtiffjpegtodicommf.json
	ls -l /tmp/WSI_MultiFrame/*

	# not all the Aperio images are J2K ... the strips are JPEG RGB ... expect:
	# dctable -k PhotometricInterpretation /tmp/WSI_MultiFrame/img_*.dcm
	# "/tmp/WSI_MultiFrame/img_0.dcm"	"YBR_FULL_422"
	# "/tmp/WSI_MultiFrame/img_1.dcm"	"RGB "
	# "/tmp/WSI_MultiFrame/img_2.dcm"	"YBR_FULL_422"
	# "/tmp/WSI_MultiFrame/img_3.dcm"	"YBR_FULL_422"
	# "/tmp/WSI_MultiFrame/img_5.dcm"	"RGB "

		# WSI is 1.2.840.10008.5.1.4.1.1.77.1.6
		# MFTCSC is 1.2.840.10008.5.1.4.1.1.7.4

		#$${HOME}/Documents/Medical/stuff/Formats/TIFF/BigTIFF/BigTIFFSamples/BigTIFFLong8Tiles.tif
		#"$${HOME}/Pictures/Medical/WSI/Aperio/8917.svs" \
		#"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1-JP2K-33005.svs"
		#"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Generic-TIFF/CMU-1.tiff"
		#"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Leica/Leica-1.scn"
		#"$${HOME}/Pictures/Medical/WSI/CMU/openslide.cs.cmu.edu/download/openslide-testdata/Aperio/CMU-1-JP2K-33005.svs"
		#"$${HOME}/Pictures/Medical/WSI/NEMAWG26_TestImages/BioImagene/Test1.tif"
		#"$${HOME}/Pictures/Medical/WSI/HistoGeneX/SampleFromUncompressedTransfer/HistoGeneX_04050732B0018S.tif"
		#"/Volumes/Toshiba5TEnc2/Pictures/Medical/WSI/HistoGeneX/Tiff/Fluorescence/Slide 109/109_A647.tif"
		#"$${HOME}/Pictures/Medical/WSI_More/HistoGeneX/Tiff/Brightfield/HistoGeneX_04050732B0018S.tif"
		#"$${HOME}/Pictures/Medical/WSI/WSI_Uncompressed_Leeds/455601.tif"

/tmp/testbigtiffjpegtodicommfnucseg.json:	Makefile
	# in TCGA, DXn slides are FFPE ...
	# 433841000124109 is only in SNOMEDCT_US
	# 433831000124104 is only in SNOMEDCT_US
	# 702941008 is not in PS3.16
	echo  >/tmp/testbigtiffjpegtodicommfnucseg.json "{"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "	\"top\" : {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		\"PatientName\" : \"TCGA-A7-A13E-01Z-00^\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		\"PatientID\" : \"TCGA-A7-A13E-01Z-00\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		\"StudyID\" : \"TCGA-A7-A13E-01Z-00-DX1\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		\"SeriesNumber\" : \"1\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		\"AccessionNumber\" : \"TCGA-A7-A13E-01Z-00\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		\"ContainerIdentifier\" : \"TCGA-A7-A13E-01Z-00-DX1\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		\"IssuerOfTheContainerIdentifierSequence\" : [],"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		\"ContainerTypeCodeSequence\" : { \"cv\" : \"433466003\", \"csd\" : \"SCT\", \"cm\" : \"Microscope slide\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		\"SpecimenDescriptionSequence\" : ["
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "	      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    \"SpecimenIdentifier\" : \"TCGA-A7-A13E-01Z-00\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    \"IssuerOfTheSpecimenIdentifierSequence\" : [],"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    \"SpecimenUID\" : \"1.3.6.1.4.1.5962.1.1.0.0.0.1551616167.5736.0\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    \"SpecimenShortDescription\" : \"Breast invasive carcinoma\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    \"SpecimenPreparationSequence\" : ["
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			    \"SpecimenPreparationStepContentItemSequence\" : ["
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"ValueType\" : \"TEXT\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"121041\", \"csd\" : \"DCM\", \"cm\" : \"Specimen Identifier\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"TextValue\" : \"TCGA-A7-A13E-01Z-00\""
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"ValueType\" : \"CODE\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"111703\", \"csd\" : \"DCM\", \"cm\" : \"Processing step description\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptCodeSequence\" :     { \"cv\" : \"433841000124109\", \"csd\" : \"SCT\", \"cm\" : \"Fixation of specimen using formalin\" }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"ValueType\" : \"CODE\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"111701\", \"csd\" : \"DCM\", \"cm\" : \"Processing type\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptCodeSequence\" :     { \"cv\" : \"433831000124104\", \"csd\" : \"SCT\", \"cm\" : \"Specimen preservation\" }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"ValueType\" : \"CODE\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"430864009\", \"csd\" : \"SCT\", \"cm\" : \"Tissue Fixative\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptCodeSequence\" :     { \"cv\" : \"431510009\", \"csd\" : \"SCT\", \"cm\" : \"Formalin\" }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			    ]"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		      },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			    \"SpecimenPreparationStepContentItemSequence\" : ["
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"ValueType\" : \"TEXT\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"121041\", \"csd\" : \"DCM\", \"cm\" : \"Specimen Identifier\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"TextValue\" : \"TCGA-A7-A13E-01Z-00\""
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"ValueType\" : \"CODE\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"111703\", \"csd\" : \"DCM\", \"cm\" : \"Processing step description\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptCodeSequence\" :     { \"cv\" : \"702941008\", \"csd\" : \"SCT\", \"cm\" : \"Paraffin embedding\" }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"ValueType\" : \"CODE\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"111701\", \"csd\" : \"DCM\", \"cm\" : \"Processing type\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptCodeSequence\" :     { \"cv\" : \"9265001\", \"csd\" : \"SCT\", \"cm\" : \"Specimen Processing\" }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"ValueType\" : \"CODE\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"430863003\", \"csd\" : \"SCT\", \"cm\" : \"Embedding medium\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptCodeSequence\" :     { \"cv\" : \"255667006\", \"csd\" : \"SCT\", \"cm\" : \"Paraffin\" }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			    ]"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		      },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			    \"SpecimenPreparationStepContentItemSequence\" : ["
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"ValueType\" : \"TEXT\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"121041\", \"csd\" : \"DCM\", \"cm\" : \"Specimen Identifier\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"TextValue\" : \"TCGA-A7-A13E-01Z-00\""
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"ValueType\" : \"CODE\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"111701\", \"csd\" : \"DCM\", \"cm\" : \"Processing type\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptCodeSequence\" :     { \"cv\" : \"127790008\", \"csd\" : \"SCT\", \"cm\" : \"Staining\" }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"ValueType\" : \"CODE\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"424361007\", \"csd\" : \"SCT\", \"cm\" : \"Using substance\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptCodeSequence\" :     { \"cv\" : \"12710003\", \"csd\" : \"SCT\", \"cm\" : \"hematoxylin stain\" }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    		\"ValueType\" : \"CODE\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptNameCodeSequence\" : { \"cv\" : \"424361007\", \"csd\" : \"SCT\", \"cm\" : \"Using substance\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "					\"ConceptCodeSequence\" :     { \"cv\" : \"36879007\", \"csd\" : \"SCT\", \"cm\" : \"water soluble eosin stain\" }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			      }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "			    ]"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		      }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    ],"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    \"PrimaryAnatomicStructureSequence\" : { \"cv\" : \"76752008\", \"csd\" : \"SCT\", \"cm\" : \"Breast\" }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "	      }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		],"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		\"OpticalPathSequence\" : ["
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "	      {"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    \"OpticalPathIdentifier\" : \"1\","
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    \"IlluminationColorCodeSequence\" : { \"cv\" : \"414298005\", \"csd\" : \"SCT\", \"cm\" : \"Full Spectrum\" },"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		    \"IlluminationTypeCodeSequence\" :  { \"cv\" : \"111744\",  \"csd\" : \"DCM\", \"cm\" : \"Brightfield illumination\" }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "	      }"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "		]"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "	}"
	echo >>/tmp/testbigtiffjpegtodicommfnucseg.json "}"

testbigtiffjpegtodicommfnucsegsubimage:	/tmp/testbigtiffjpegtodicommfnucseg.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true -Djava.io.tmpdir=/tmp \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/testbigtiffjpegtodicommfnucseg.json \
		"$${HOME}/Pictures/Medical/WSI_More/WeeblyNucleiSegmentation/Tissue images/TCGA-18-5592-01Z-00-DX1.tif" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 \
		1.2.840.10008.1.2.4.50
	rm -f /tmp/testbigtiffjpegtodicommfnucseg.json
	# will have failed to produce anything until we a) implement LZW decompression for tiled images, b) we implement decompression of multiple JPEG strips into single frame for last image
	ls -l /tmp/WSI_MultiFrame/*

testbigtiffjpegtodicommfnucsegfullimage:	/tmp/testbigtiffjpegtodicommfnucseg.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true -Djava.io.tmpdir=/tmp \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/testbigtiffjpegtodicommfnucseg.json \
		"$${HOME}/Pictures/Medical/WSI_More/WeeblyNucleiSegmentation/CorrespondingSVSFilesFromTCGA/TCGA-A7-A13E-01Z-00-DX1.891954FF-316A-4562-AA14-429631944F22.svs" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 \
		1.2.840.10008.1.2.4.50
	rm -f /tmp/testbigtiffjpegtodicommfnucseg.json
	ls -l /tmp/WSI_MultiFrame/*



# device and acquisition description from "http://www.adcis.net/en/Download-Third-Party/Messidor.html""
/tmp/testretinatifftodicom.json:	Makefile
	echo  >/tmp/testretinatifftodicom.json "{"
	echo >>/tmp/testretinatifftodicom.json "	\"top\" : {"
	echo >>/tmp/testretinatifftodicom.json "		\"PatientName\" : \"Messidor^51625\","
	echo >>/tmp/testretinatifftodicom.json "		\"PatientID\" : \"51625\","
	echo >>/tmp/testretinatifftodicom.json "		\"StudyID\" : \"51625\","
	echo >>/tmp/testretinatifftodicom.json "		\"StudyDate\" : \"20051021\","
	echo >>/tmp/testretinatifftodicom.json "		\"SeriesDate\" : \"20051021\","
	echo >>/tmp/testretinatifftodicom.json "		\"ContentDate\" : \"20051021\","
	echo >>/tmp/testretinatifftodicom.json "		\"InstanceCreationDate\" : \"20051021\","
	echo >>/tmp/testretinatifftodicom.json "		\"AcquisitionDateTime\" : \"20051021\","
	echo >>/tmp/testretinatifftodicom.json "		\"SeriesNumber\" : \"1\","
	echo >>/tmp/testretinatifftodicom.json "		\"InstitutionName\" : \"H\\u00F4pital Lariboisi\\u00E8re Paris\","
	echo >>/tmp/testretinatifftodicom.json "		\"InstitutionalDepartmentName\" : \"Service d'Ophtalmologie\","
	echo >>/tmp/testretinatifftodicom.json "		\"Manufacturer\" : \"Topcon\","
	echo >>/tmp/testretinatifftodicom.json "		\"ManufacturerModelName\" : \"TRC NW6\","
	echo >>/tmp/testretinatifftodicom.json "		\"ImageType\" : \"ORIGINAL\\\\\PRIMARY\","
	echo >>/tmp/testretinatifftodicom.json "		\"AcquisitionDeviceTypeCodeSequence\" : { \"cv\" : \"409898007\", \"csd\" : \"SCT\", \"cm\" : \"Fundus Camera\" },"
	echo >>/tmp/testretinatifftodicom.json "		\"IlluminationTypeCodeSequence\" : null,"
	echo >>/tmp/testretinatifftodicom.json "		\"LightPathFilterTypeStackCodeSequence\" : null,"
	echo >>/tmp/testretinatifftodicom.json "		\"ImagePathFilterTypeStackCodeSequence\" : null,"
	echo >>/tmp/testretinatifftodicom.json "		\"LensesCodeSequence\" : null,"
	echo >>/tmp/testretinatifftodicom.json "		\"DetectorType\" : \"CCD\","
	echo >>/tmp/testretinatifftodicom.json "		\"PatientEyeMovementCommanded\" : \"NO\","
	echo >>/tmp/testretinatifftodicom.json "		\"HorizontalFieldOfView\" : \"45\","
	echo >>/tmp/testretinatifftodicom.json "		\"RefractiveStateSequence\" : null,"
	echo >>/tmp/testretinatifftodicom.json "		\"EmmetropicMagnification\" : null,"
	echo >>/tmp/testretinatifftodicom.json "		\"IntraOcularPressure\" : null,"
	echo >>/tmp/testretinatifftodicom.json "		\"PupilDilated\" : \"YES\","
	echo >>/tmp/testretinatifftodicom.json "		\"MydriaticAgentSequence\" : ["
	echo >>/tmp/testretinatifftodicom.json "			{"
	echo >>/tmp/testretinatifftodicom.json "				\"MydriaticAgentCodeSequence\" : { \"cv\" : \"9190005\", \"csd\" : \"SCT\", \"cm\" : \"Tropicamide\" }"
	echo >>/tmp/testretinatifftodicom.json "			}"
	echo >>/tmp/testretinatifftodicom.json "		],"
	echo >>/tmp/testretinatifftodicom.json "		\"DegreeOfDilation\" : null,"
	echo >>/tmp/testretinatifftodicom.json "		\"ImageLaterality\" : \"L\","
	echo >>/tmp/testretinatifftodicom.json "		\"AnatomicRegionSequence\" : { \"cv\" : \"5665001\", \"csd\" : \"SCT\", \"cm\" : \"Retina\" }"
	echo >>/tmp/testretinatifftodicom.json "	}"
	echo >>/tmp/testretinatifftodicom.json "}"

testretinatifftodicom:	/tmp/testretinatifftodicom.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/Retina_SingleFrame
	rm -f /tmp/Retina_SingleFrame/*.dcm
	java -cp ${PATHTOROOT}:${JSONADDITIONALJAR} -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/testretinatifftodicom.json \
		$${HOME}/Pictures/Medical/VL_more/Ophthalmology/Messidor/Original/Base12/20051021_51625_0100_PP.tif \
		/tmp/Retina_SingleFrame/img \
		OP 1.2.840.10008.5.1.4.1.1.77.1.5.1
	#rm -f /tmp/testretinatifftodicom.json

testimmunostaininghtan:	Immunostaining.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.Immunostaining=debug \
		com.pixelmed.convert.Immunostaining \
		/Volumes/Elements5TBNonEncB/Pictures/Medical/HTAN/idc-htan-transfer/HTAN-V1-Feb-2022/2022-02-04_channel-metadata/channel_metadata_files/syn24988818.csv

testometiffjpegtodicommfj2klosslesschannelmetadata:	/tmp/bigtiffjpegtodicommf.json TIFFToDicom.class TIFFException.class TIFFFile.class TIFFImageFileDirectories.class TIFFImageFileDirectory.class TIFFImageFileDirectoryEntry.class TIFFTags.class TIFFTypes.class TIFFValues.class
	mkdir -p /tmp/WSI_MultiFrame
	rm -f /tmp/WSI_MultiFrame/*
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS}:${JSONADDITIONALJAR} -Djava.awt.headless=true -Djava.io.tmpdir=/tmp \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.convert.TIFFToDicom=debug \
		com.pixelmed.convert.TIFFToDicom \
		/tmp/bigtiffjpegtodicommf.json \
		"/Volumes/Elements5TBNonEncB/Pictures/Medical/HTAN/idc-htan-transfer/HTAN-V1-Feb-2022/HTAN OHSU/CyCIF/BEMS267865_Scene-001.ome.tif" \
		"/tmp/WSI_MultiFrame/img" \
		SM 1.2.840.10008.5.1.4.1.1.77.1.6 \
		1.2.840.10008.1.2.4.90 \
		ALWAYSWSI MERGESTRIPS DONOTINCLUDEIMAGEDESCRIPTION \
		CHANNELFILE "$${HOME}/Documents/Clients/BWH/IDC/HTAN/ActualHTAN/channel_metadata_files/syn29781517.csv"
	rm -f /tmp/bigtiffjpegtodicommf.json
	ls -l /tmp/WSI_MultiFrame/*
