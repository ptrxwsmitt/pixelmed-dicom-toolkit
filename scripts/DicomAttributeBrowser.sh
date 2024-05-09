#!/bin/sh

PIXELMEDDIR=.

java -Xmx512m -Xms512m -cp "${PIXELMEDDIR}/pixelmed.jar:${PIXELMEDDIR}/lib/additional/commons-compress-1.12.jar:${PIXELMEDDIR}/lib/additional/commons-codec-1.3.jar" com.pixelmed.dicom.AttributeTreeBrowser "$*"
