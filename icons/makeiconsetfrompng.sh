#!/bin/sh

# derived from http://stackoverflow.com/questions/12306223/how-to-manually-create-icns-files-using-iconutil

nametouse=`basename "$1" .icns`

mkdir "${nametouse}".iconset
sips -z 16 16     "${nametouse}.png" --out "${nametouse}".iconset/icon_16x16.png
sips -z 32 32     "${nametouse}.png" --out "${nametouse}".iconset/icon_16x16@2x.png
sips -z 32 32     "${nametouse}.png" --out "${nametouse}".iconset/icon_32x32.png
sips -z 64 64     "${nametouse}.png" --out "${nametouse}".iconset/icon_32x32@2x.png
sips -z 128 128   "${nametouse}.png" --out "${nametouse}".iconset/icon_128x128.png
sips -z 256 256   "${nametouse}.png" --out "${nametouse}".iconset/icon_128x128@2x.png
sips -z 256 256   "${nametouse}.png" --out "${nametouse}".iconset/icon_256x256.png
sips -z 512 512   "${nametouse}.png" --out "${nametouse}".iconset/icon_256x256@2x.png
sips -z 512 512   "${nametouse}.png" --out "${nametouse}".iconset/icon_512x512.png
cp "${nametouse}.png" "${nametouse}".iconset/icon_512x512@2x.png
iconutil --convert icns "${nametouse}".iconset
#rm -rf "${nametouse}".iconset
