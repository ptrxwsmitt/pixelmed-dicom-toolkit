#
# Note that PATHTOROOT must have been specified prior to including this file
#

JUNITJAR = ${PATHTOROOT}/lib/junit/junit-4.8.1.jar

ADDITIONALJARDIRINROOT = lib/additional

PATHTODCTOOLSUPPORTFROMROOT = ../../dctool.support

PATHTODCTOOLSFROMROOT = ../../dicom3tools

PATHTOSCPECGSAMPLESFROMROOT = ../../../Documents/Medical/stuff/ECG/OpenECG

PATHTOTESTFILESFROMROOT = ./testpaths

PATHTOTESTRESULTSFROMROOT = ./testresults

PATHTOADDITIONAL = ${PATHTOROOT}/${ADDITIONALJARDIRINROOT}

PATHTODCTOOLSUPPORT = ${PATHTOROOT}/${PATHTODCTOOLSUPPORTFROMROOT}

PATHTODCTOOLS = ${PATHTOROOT}/${PATHTODCTOOLSFROMROOT}

PATHTOSTANDARDFROMHOME = DICOM_Publish_XML

# commons-compress for bzip2 not needed for compile, but useful for execution if available ...
COMMONSCOMPRESSJARFILENAME = commons-compress-1.12.jar
COMMONSCOMPRESSADDITIONALJAR = ${PATHTOADDITIONAL}/${COMMONSCOMPRESSJARFILENAME}

BZIP2ADDITIONALJAR = ${COMMONSCOMPRESSADDITIONALJAR}

JIIOADDITIONALJARS = ${PATHTOADDITIONAL}/jai_imageio.jar

VECMATHADDITIONALJAR = ${PATHTOADDITIONAL}/vecmath1.2-1.14.jar

JSONADDITIONALJAR = ${PATHTOADDITIONAL}/javax.json-1.0.4.jar

SLF4JCOMPILEADDITIONALJAR = ${PATHTOADDITIONAL}/slf4j-api-1.7.22.jar

SLF4JSIMPLEADDITIONALJAR = ${PATHTOADDITIONAL}/slf4j-api-1.7.22.jar:${PATHTOADDITIONAL}/slf4j-simple-1.7.22.jar

# commons-codec not needed for compile, but useful for execution if available ...
COMMONSCODECJARFILENAME = commons-codec-1.3.jar
COMMONSCODECADDITIONALJAR = ${PATHTOADDITIONAL}/${COMMONSCODECJARFILENAME}

DICOMADDITIONALJARS = ${BZIP2ADDITIONALJAR}:${COMMONSCODECADDITIONALJAR}:${VECMATHADDITIONALJAR}:${JSONADDITIONALJAR}

DISPLAYADDITIONALJARS = ${DICOMADDITIONALJARS}:${JIIOADDITIONALJARS}

DATABASEADDITIONALJARS = ${PATHTOADDITIONAL}/hsqldb.jar

FTPADDITIONALJARS = ${PATHTOADDITIONAL}/commons-net-ftp-2.0.jar

CSVADDITIONALJARS = ${PATHTOADDITIONAL}/opencsv-2.4.jar

NETWORKADDITIONALJARS = ${PATHTOADDITIONAL}/jmdns.jar

VIEWERADDITIONALJARS = ${DISPLAYADDITIONALJARS}:${DATABASEADDITIONALJARS}:${NETWORKADDITIONALJARS}

SERVERADDITIONALJARS = ${VIEWERADDITIONALJARS}

SAXONADDITIONALJARS = ${PATHTOADDITIONAL}/saxon-he-11.5.jar:${PATHTOADDITIONAL}/xmlresolver-4.6.4.jar

#JPEGBLOCKREDACTIONJAR = $${HOME}/work/codec/pixelmed_codec.jar
JPEGBLOCKREDACTIONJAR = ${PATHTOADDITIONAL}/pixelmed_codec.jar

#PIXELMEDIMAGEIOJAR = $${HOME}/work/codec/pixelmed_imageio.jar
PIXELMEDIMAGEIOJAR = ${PATHTOADDITIONAL}/pixelmed_imageio.jar

# override new default limits added in 1.8.0_331 for xalan processor (https://www.oracle.com/java/technologies/javase/8u331-relnotes.html)
XALANJAVAOPTS=-Djdk.xml.xpathExprGrpLimit=0 -Djdk.xml.xpathExprOpLimit=0 -Djdk.xml.xpathTotalOpLimit=0

SAXONJAVAOPTS=-Djavax.xml.transform.TransformerFactory=net.sf.saxon.TransformerFactoryImpl

JAVAVERSIONTARGET=1.7

JAVACTARGETOPTIONS=-target ${JAVAVERSIONTARGET} -source ${JAVAVERSIONTARGET} -bootclasspath $${JAVAVERSIONTARGETJARFILE}

.SUFFIXES:	.class .java .ico .png .properties .utf-8_properties

# -XDignore.symbol.file needed to find "package com.sun.image.codec.jpeg" ("http://stackoverflow.com/questions/1906673/import-com-sun-image-codec-jpeg")
JAVACOPTIONS = -O ${JAVACTARGETOPTIONS} -encoding "UTF8" -Xlint:deprecation -XDignore.symbol.file -Xdiags:verbose

.java.class:
	export JAVAVERSIONTARGETJARFILE=`/usr/libexec/java_home -v ${JAVAVERSIONTARGET} | tail -1`/jre/lib/rt.jar; javac ${JAVACOPTIONS} \
		-classpath ${PATHTOROOT}:${SLF4JCOMPILEADDITIONALJAR}:${DICOMADDITIONALJARS}:${VIEWERADDITIONALJARS}:${FTPADDITIONALJARS}:${JUNITJAR} \
		-sourcepath ${PATHTOROOT} $<

.utf-8_properties.properties:
	native2ascii -encoding UTF8 $< >$@

clean:
	rm -f *~ *.class core *.bak test.*

