#!/bin/sh

dicomfile="$*"

PIXELMEDDIR=.

# override new default limits added in 1.8.0_331 for xalan processor (https://www.oracle.com/java/technologies/javase/8u331-relnotes.html)
java \
    -Djdk.xml.xpathExprGrpLimit=0 \
    -Djdk.xml.xpathExprOpLimit=0 \
    -Djdk.xml.xpathTotalOpLimit=0 \
	-Xmx512m -Xms512m \
	-cp "${PIXELMEDDIR}/pixelmed.jar:${PIXELMEDDIR}/lib/additional/pixelmed_imageio.jar:${PIXELMEDDIR}/lib/additional/hsqldb.jar:${PIXELMEDDIR}/lib/additional/commons-compress-1.12.jar:${PIXELMEDDIR}/lib/additional/vecmath1.2-1.14.jar:${PIXELMEDDIR}/lib/additional/jmdns.jar:${PIXELMEDDIR}/lib/additional/commons-codec-1.3.jar:${PIXELMEDDIR}/lib/additional/jai_imageio.jar" \
	com.pixelmed.display.DicomImageViewer "${dicomfile}"
