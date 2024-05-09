#!/bin/sh

src="$1"
dst="$2"
shapes="$3"

PIXELMEDDIR=.

java -Xmx1g -Xms1g -Djava.awt.headless=true \
  -cp "${PIXELMEDDIR}/pixelmed.jar:${PIXELMEDDIR}/lib/additional/pixelmed_imageio.jar:${PIXELMEDDIR}/lib/additional/commons-compress-1.12.jar:${PIXELMEDDIR}/lib/additional/commons-codec-1.3.jar:${PIXELMEDDIR}/lib/additional/jai_imageio.jar" \
  com.pixelmed.apps.DeidentifyAndRedact \
  "${src}" \
  "${dst}" \
  "${shapes}"

