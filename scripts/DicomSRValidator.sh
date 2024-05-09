#!/bin/sh

PIXELMEDDIR=.

# override new default limits added in 1.8.0_331 for xalan processor (https://www.oracle.com/java/technologies/javase/8u331-relnotes.html)
#java \
#    -Djdk.xml.xpathExprGrpLimit=0 \
#    -Djdk.xml.xpathExprOpLimit=0 \
#    -Djdk.xml.xpathTotalOpLimit=0 \
#	-Xmx4g -XX:-UseGCOverheadLimit \
#	-cp "${PIXELMEDDIR}/pixelmed.jar:${PIXELMEDDIR}/lib/additional/slf4j-api-1.7.13.jar:${PIXELMEDDIR}/lib/additional/slf4j-simple-1.7.13.jar:${PIXELMEDDIR}/lib/additional/excalibur-bzip2-1.0.jar:${PIXELMEDDIR}/lib/additional/commons-codec-1.3.jar" \
#	com.pixelmed.validate.DicomSRValidator $*

# use Saxon to avoid running out of heap space in Xalan with large files (000875)
# turn off FEATURE_SECURE_PROCESSING that may trigger if there are very large number of numeric values in an attribute (001385)
java \
	-Xmx4g -XX:-UseGCOverheadLimit \
	-Djdk.xml.xpathExprGrpLimit=0 -Djdk.xml.xpathExprOpLimit=0 -Djdk.xml.xpathTotalOpLimit=0 \
	-Djavax.xml.transform.TransformerFactory=net.sf.saxon.TransformerFactoryImpl \
	-cp "${PIXELMEDDIR}/pixelmed.jar:${PIXELMEDDIR}/lib/additional/slf4j-api-1.7.13.jar:${PIXELMEDDIR}/lib/additional/slf4j-simple-1.7.13.jar:${PIXELMEDDIR}/lib/additional/excalibur-bzip2-1.0.jar:${PIXELMEDDIR}/lib/additional/commons-codec-1.3.jar:${PIXELMEDDIR}/lib/additional/saxon-he-11.5.jar:${PIXELMEDDIR}/lib/additional/xmlresolver-4.6.4.jar" \
	com.pixelmed.validate.DicomSRValidator $*
