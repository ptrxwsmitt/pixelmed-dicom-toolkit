PATHTOROOT = ../../..

OBJS = \
ByteArray.class \
CopyStream.class \
DateUtilities.class \
FileUtilities.class \
FloatFormatter.class \
HexDump.class \
JTreeWithAdditionalKeyStrokeActions.class \
MessageLogger.class \
PdfDecoder.class \
PdfException.class \
PhoneticStringEncoder.class \
PhoneticStringEncoderException.class \
PrintStreamMessageLogger.class \
ScrollingTextAreaWriter.class \
Syslog.class \
StringUtilities.class \
MACAddress.class \
XPathQuery.class \
UUIDBasedOID.class \
Base64.class \
FilenameFilterByCaseInsensitiveSuffix.class \
ThreadUtilities.class \
ColorUtilities.class \
CapabilitiesAvailable.class \
FileReaper.class \
ExecuteTranslet.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

# override javac command in Makefile.common.mk to add jars needed for CapabilitiesAvailable
.java.class:
	export JAVAVERSIONTARGETJARFILE=`/usr/libexec/java_home -v ${JAVAVERSIONTARGET} | tail -1`/jre/lib/rt.jar; javac ${JAVACOPTIONS} \
		-classpath ${PATHTOROOT}:${JPEGBLOCKREDACTIONJAR} \
		-sourcepath ${PATHTOROOT} $<

archive:
	tar -cvf - Makefile *.java | gzip -best > ../../../utils.`date '+%Y%m%d'`.tar.gz

test:	testStringUtilities

testByteArray:	ByteArray.class
	java -cp ${PATHTOROOT} com.pixelmed.utils.ByteArray

testSyslog:	Syslog.class
	java -cp ${PATHTOROOT} com.pixelmed.utils.Syslog

testMACAddress:	MACAddress.class
	java -cp ${PATHTOROOT} com.pixelmed.utils.MACAddress

testxpathquery.xml:
	rm -f $@
	echo >>$@ '<?xml version="1.0" encoding="UTF-8"?>'
	echo >>$@ '	<DicomObject>'
	echo >>$@ '		<ImageType element="0008" group="0008" vr="CS">'
	echo >>$@ '			<value number="1">ORIGINAL</value>'
	echo >>$@ '			<value number="2">PRIMARY</value>'
	echo >>$@ '			<value number="3">CINE</value>'
	echo >>$@ '			<value number="4">NONE</value>'
	echo >>$@ '		</ImageType>'
	echo >>$@ '		<ContrastBolusAgentSequence element="0012" group="0018" vr="SQ">'
	echo >>$@ '		  <Item number="1">'
	echo >>$@ '		    <CodeValue element="0100" group="0008" vr="SH">'
	echo >>$@ '		      <value number="1">C-17800</value>'
	echo >>$@ '		    </CodeValue>'
	echo >>$@ '		  </Item>'
	echo >>$@ '		</ContrastBolusAgentSequence>'
	echo >>$@ '</DicomObject>'

testxpathquery:	XPathQuery.class testxpathquery.xml
	java -cp ${PATHTOROOT} com.pixelmed.utils.XPathQuery testxpathquery.xml "/DicomObject/ImageType"
	java -cp ${PATHTOROOT} com.pixelmed.utils.XPathQuery testxpathquery.xml "/DicomObject/ImageType/value"
	java -cp ${PATHTOROOT} com.pixelmed.utils.XPathQuery testxpathquery.xml "/DicomObject/ImageType/value[@number=1]"
	java -cp ${PATHTOROOT} com.pixelmed.utils.XPathQuery testxpathquery.xml "//ImageType/value[@number=1]"
	java -cp ${PATHTOROOT} com.pixelmed.utils.XPathQuery testxpathquery.xml "//ImageType/value[@number=1] = 'ORIGINAL'"
	java -cp ${PATHTOROOT} com.pixelmed.utils.XPathQuery testxpathquery.xml "/DicomObject/ContrastBolusAgentSequence/Item[@number=1]/CodeValue/value[@number=1]"
	java -cp ${PATHTOROOT} com.pixelmed.utils.XPathQuery testxpathquery.xml "//ContrastBolusAgentSequence/Item/CodeValue/value"
	rm -f testxpathquery.xml

testfloatformatter:	FloatFormatter.class
	java -cp ${PATHTOROOT} com.pixelmed.utils.FloatFormatter

testcolorutilities:	ColorUtilities.class
	java -cp ${PATHTOROOT} com.pixelmed.utils.ColorUtilities sRGB8toCIELab16 0 0 0
	java -cp ${PATHTOROOT} com.pixelmed.utils.ColorUtilities sRGB8toCIELab16 0x0 0x0 0x0
	java -cp ${PATHTOROOT} com.pixelmed.utils.ColorUtilities sRGB8toCIELab16 255 255 255
	java -cp ${PATHTOROOT} com.pixelmed.utils.ColorUtilities sRGB8toCIELab16 0xff 0xff 0xff
	java -cp ${PATHTOROOT} com.pixelmed.utils.ColorUtilities CIELab16tosRGB8 0 32896 32896
	java -cp ${PATHTOROOT} com.pixelmed.utils.ColorUtilities CIELab16tosRGB8 0x0 0x8080 0x8080
	java -cp ${PATHTOROOT} com.pixelmed.utils.ColorUtilities CIELab16tosRGB8 65535 32897 32893
	java -cp ${PATHTOROOT} com.pixelmed.utils.ColorUtilities CIELab16tosRGB8 0xffff 0x8081 0x807d

testcapabilitiesjrealone:	CapabilitiesAvailable.class
	java -cp ${PATHTOROOT} -Djava.awt.headless=true com.pixelmed.utils.CapabilitiesAvailable

testcapabilitiesjiionontative:	CapabilitiesAvailable.class
	java -cp ${PATHTOROOT}:${JIIOADDITIONALJARS} -Djava.awt.headless=true com.pixelmed.utils.CapabilitiesAvailable

testcapabilitiesjiiopixelmed:	CapabilitiesAvailable.class
	java -cp ${PATHTOROOT}:${PIXELMEDIMAGEIOJAR} -Djava.awt.headless=true -Dorg.slf4j.simpleLogger.log.com.pixelmed.utils.CapabilitiesAvailable=debug com.pixelmed.utils.CapabilitiesAvailable

testcopystreamunbuffered:	CopyStream.class
	rm -rf /tmp/crap.dcm /tmp/crap2.dcm
	dcsmpte -rows 4096 -columns 4096 /tmp/crap.dcm
	java -cp ${PATHTOROOT} \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.utils.CopyStream=debug \
		com.pixelmed.utils.CopyStream \
		/tmp/crap.dcm /tmp/crap2.dcm
	@echo "Binary comparison of copied file should succeed"
	cmp /tmp/crap.dcm /tmp/crap2.dcm
	rm /tmp/crap.dcm /tmp/crap2.dcm

testcopystreambuffered:	CopyStream.class
	rm -rf /tmp/crap.dcm /tmp/crap2.dcm
	dcsmpte -rows 4096 -columns 4096 /tmp/crap.dcm
	java -cp ${PATHTOROOT} \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.utils.CopyStream=debug \
		com.pixelmed.utils.CopyStream \
		/tmp/crap.dcm /tmp/crap2.dcm \
		32768 32768 32768
	@echo "Binary comparison of copied file should succeed"
	cmp /tmp/crap.dcm /tmp/crap2.dcm
	rm /tmp/crap.dcm /tmp/crap2.dcm


