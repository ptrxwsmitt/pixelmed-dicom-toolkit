OBJS = \
	EntropyCodedSegment.class \
	HuffmanTable.class \
	Markers.class \
	MarkerSegmentAPP0JFIF.class \
	MarkerSegmentDHT.class \
	MarkerSegmentDQT.class \
	MarkerSegmentSOF.class \
	MarkerSegmentSOS.class \
	Parse.class \
	QuantizationTable.class \
	Utilities.class \
	OutputArrayOrStream.class

all:	${OBJS}

PATHTOROOT = ../../../..

include ${PATHTOROOT}/Makefile.common.mk

clean:
	rm -f *~ *.class core *.bak

testparse:	${OBJS}
	rm -f /tmp/crap_copied.jpg
	rm -f /tmp/crap_source.jpg
	cp -v \
		"$${HOME}/Documents/Clients/MDDX/Experiment20130905/crap.jpg" \
		/tmp/crap_source.jpg
	java -Djava.awt.headless=true  -cp ${PATHTOROOT} com.pixelmed.codec.jpeg.Parse \
		/tmp/crap_source.jpg \
		/tmp/crap_copied.jpg
	# use make -i to continue to dump
	ls -l /tmp/crap_source.jpg
	ls -l /tmp/crap_copied.jpg
	hexdump -C /tmp/crap_source.jpg | tail -3
	hexdump -C /tmp/crap_copied.jpg | tail -3
	@echo "Comparing source and copied ... may fail with EOF if padding after EOI marker that is not copied, or missing EOI marker is added, both of which are OK"
	cmp /tmp/crap_source.jpg /tmp/crap_copied.jpg
	@echo "Finished comparing"

# without restart and working
		#"$${HOME}/Documents/Medical/compression/JPEG/10918-1/jpeg-6/testimg.jpg"
		#"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/colorpngresavedfrompreview.jpg"
		#"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/huff_simple0.jpg"
		#"$${HOME}/Documents/Clients/MDDX/Experiment20130905/crap.jpg"
		#"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/smpte_8_q1.jpg"

# without restart and not working

# with restart and working
		#"$${HOME}/Pictures/Interesting/Me/clunie_737_cropped_close.jpg"
		#"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/smpte_8_cjpeg_rst1.jpg"
		#"$${HOME}/Documents/Medical/compression/JPEG/10918-2/ITU T83/T83_process1/A1.JPG"

# with restart and not working

# premature EOF without EOI marker working (Heartlab)
		# dctoraw "/Volumes/Toshiba5TEnc/MDDX/20160928_CorruptionDuringMasking/dicom File.dcm" "/Volumes/Toshiba5TEnc/MDDX/20160928_CorruptionDuringMasking/dicom File.jpg"
		# "/Volumes/Toshiba5TEnc/MDDX/20160928_CorruptionDuringMasking/dicom File.jpg"

		# dctoraw "/Volumes/Toshiba5TEnc/MDDX/20170320_Assembla2719_MissingJPEGEOI/corruptedfile147652/corruptedfile147652/IM001(35)" "/Volumes/Toshiba5TEnc/MDDX/20170320_Assembla2719_MissingJPEGEOI/corruptedfile147652_IM001_35.jpg"
		# "/Volumes/Toshiba5TEnc/MDDX/20170320_Assembla2719_MissingJPEGEOI/corruptedfile147652_IM001_35.jpg"


testdecompress:	${OBJS}
	rm -f /tmp/crap_source.jpg
	rm -f /tmp/crap_decompressed*.raw
	cp -v \
		"$${HOME}/Documents/Medical/compression/JPEG/10918-2/ITU T83/T83_process14/O1.JPG" \
		/tmp/crap_source.jpg
	java -Djava.awt.headless=true  -cp ${PATHTOROOT} com.pixelmed.codec.jpeg.Parse \
		/tmp/crap_source.jpg \
		"" \
		/tmp/crap_decompressed.raw
	ls -l /tmp/crap_decompressed*.raw

		#"$${HOME}/Documents/Medical/compression/JPEG/10918-2/ITU T83/T83_process14/O1.JPG" \
		#"$${HOME}/Documents/Medical/compression/JPEG/10918-2/ITU T83/T83_process14/O2.JPG" \

testcannotdecompress:	${OBJS}
	rm -f /tmp/crap_source.jpg
	rm -f /tmp/crap_decompressed*.raw
	cp -v \
		"${PATHTOROOT}/${PATHTOTESTFILESFROMROOT}/smpte_8_q1.jpg" \
		/tmp/crap_source.jpg
	java -Djava.awt.headless=true  -cp ${PATHTOROOT} com.pixelmed.codec.jpeg.Parse \
		/tmp/crap_source.jpg \
		"" \
		/tmp/crap_decompressed.raw

testdecompressfromdicom:	${OBJS}
	rm -f /tmp/crap_source.dcm
	rm -f /tmp/crap_source.jpg
	rm -f /tmp/crap_decompressed*.raw
	cp -v \
		"$${HOME}/Pictures/Medical/JPEGVarious/z18" \
		/tmp/crap_source.dcm
	dctoraw \
		/tmp/crap_source.dcm \
		/tmp/crap_source.jpg
	java -Djava.awt.headless=true  -cp ${PATHTOROOT} com.pixelmed.codec.jpeg.Parse \
		/tmp/crap_source.jpg \
		"" \
		/tmp/crap_decompressed.raw
	ls -l /tmp/crap_decompressed*.raw
	rm -f /tmp/crap_source_dcunjpeg.*
	dcunjpeg /tmp/crap_source.dcm /tmp/crap_source_dcunjpeg_little.dcm
	dccp -endian big -vr explicit /tmp/crap_source_dcunjpeg_little.dcm /tmp/crap_source_dcunjpeg_big.dcm
	dctoraw /tmp/crap_source_dcunjpeg_big.dcm /tmp/crap_source_dcunjpeg.raw
	@echo "Comparing decompressed with pixelmed codec and decompressed with whatever codec dcunjpeg uses"
	cmp /tmp/crap_decompressed.raw /tmp/crap_source_dcunjpeg.raw
	@echo "Finished comparing"
	hexdump -C /tmp/crap_decompressed.raw | head -3
	hexdump -C /tmp/crap_source_dcunjpeg.raw | head -3

		# "$${HOME}/Pictures/Medical/JPEGVarious/z18"
