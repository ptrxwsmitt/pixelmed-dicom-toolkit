#!/bin/sh

dicomfile="$*"

PIXELMEDDIR=.

java -Xmx4g -cp "${PIXELMEDDIR}/pixelmed.jar:${PIXELMEDDIR}/lib/additional/commons-codec-1.3.jar" com.pixelmed.displaywave.ECGViewer "${dicomfile}"
