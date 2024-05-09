#!/bin/sh

PIXELMEDDIR=.

java -Djava.awt.headless=true -Xmx1g -cp "${PIXELMEDDIR}/pixelmed.jar:${PIXELMEDDIR}/lib/additional/commons-compress-1.12.jar:${PIXELMEDDIR}/lib/additional/commons-codec-1.3.jar:${PIXELMEDDIR}/lib/additional/jai_imageio.jar" com.pixelmed.dicom.MoveDicomFilesIntoHierarchy $*
