#!/bin/sh

dicomdirfile="$*"

PIXELMEDDIR=.

java -Xmx512m -Xms512m -cp "${PIXELMEDDIR}/pixelmed.jar:${PIXELMEDDIR}/lib/additional/pixelmed_imageio.jar:${PIXELMEDDIR}/lib/additional/hsqldb.jar:${PIXELMEDDIR}/lib/additional/commons-compress-1.12.jar:${PIXELMEDDIR}/lib/additional/vecmath1.2-1.14.jar:${PIXELMEDDIR}/lib/additional/jmdns.jar:${PIXELMEDDIR}/lib/additional/commons-codec-1.3.jar:${PIXELMEDDIR}/lib/additional/jai_imageio.jar" com.pixelmed.display.DicomBrowser "${dicomdirfile}"
