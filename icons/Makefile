ICOFILES = \
	ConvertAmicasJPEG2000FilesetToDicom.ico \
	DicomCleaner.ico \
	DicomImageBlackout.ico \
	DicomImageViewer.ico \
	DoseUtility.ico \
	DownloadOrTransmit.ico \
	MediaImporter.ico \
	WatchFolderAndSend.ico


ICONFILES = \
	ConvertAmicasJPEG2000FilesetToDicom.icns \
	DicomCleaner.icns \
	DicomImageBlackout.icns \
	DicomImageViewer.icns \
	DoseUtility.icns \
	DownloadOrTransmit.icns \
	MediaImporter.icns \
	WatchFolderAndSend.icns

DERIVEDPNG1024FILES = \
	ConvertAmicasJPEG2000FilesetToDicom_1024.png \
	DicomImageBlackout_1024.png \
	DicomImageViewer_1024.png \
	DownloadOrTransmit_1024.png \
	MediaImporter_1024.png \
	WatchFolderAndSend_1024.png

PNG256FILES = \
	ConvertAmicasJPEG2000FilesetToDicom.png \
	DicomCleaner.png \
	DicomImageBlackout.png \
	DicomImageViewer.png \
	DoseUtility.png \
	DownloadOrTransmit.png \
	MediaImporter.png \
	WatchFolderAndSend.png

all:	${DERIVEDPNG1024FILES} ${PNG256FILES} ${ICOFILES} ${ICONFILES}

PATHTOROOT = ..

include ${PATHTOROOT}/Makefile.common.mk

.SUFFIXES:	.iconset .icns .ico .png

# http://www.winterdrache.de/freeware/png2ico/
# port install png2ico
# NB. unusual reverse order of arguments ... USAGE: png2ico icofile [--colors <num>] pngfile1 [pngfile2 ...]
.png.ico:
	png2ico $@ $<

.png.icns:
	./makeiconsetfrompng.sh $@


clean:
	rm -rf *~ *.class core *.bak test.* *.ico *.iconset *.icns
	rm -f ${DERIVEDPNG1024FILES} ${PNG256FILES}

# logo_1024.png is hand created by Save for web in AI from logo.svg as 1024 wide then pad canvas to square in Photoshop

ConvertAmicasJPEG2000FilesetToDicom_1024.png:	logo_1024.png
	cp $< $@

ConvertAmicasJPEG2000FilesetToDicom.png:	ConvertAmicasJPEG2000FilesetToDicom_1024.png
	sips -z 128 128 $< --out $@


DicomImageBlackout_1024.png:	logo_1024.png
	cp $< $@

DicomImageBlackout.png:	DicomImageBlackout_1024.png
	sips -z 128 128 $< --out $@


DicomImageViewer_1024.png:	logo_1024.png
	cp $< $@

DicomImageViewer.png:	DicomImageViewer_1024.png
	sips -z 128 128 $< --out $@


DownloadOrTransmit_1024.png:	logo_1024.png
	cp $< $@

DownloadOrTransmit.png:	DicomImageViewer_1024.png
	sips -z 128 128 $< --out $@


MediaImporter_1024.png:	logo_1024.png
	cp $< $@

MediaImporter.png:	DicomImageViewer_1024.png
	sips -z 128 128 $< --out $@


WatchFolderAndSend_1024.png:	logo_1024.png
	cp $< $@

WatchFolderAndSend.png:	DicomImageViewer_1024.png
	sips -z 128 128 $< --out $@


# These are the non-derived PNG files created by hand editing ....

# DoseUtility_1024.png is hand created by pasting RadiationWarningSymbol/Radiation_warning_symbol.svg into logo.svg then Save for web in AI from logo.svg as 1024 wide then pad canvas to square in Photoshop
DoseUtility.png:	DoseUtility_1024.png
	sips -z 128 128 $< --out $@

# DicomCleaner_1024.png is hand created by pasting WashingMachine/Symbol-washing-machine.svg into logo.svg then Save for web in AI from logo.svg as 1024 wide then pad canvas to square in Photoshop
DicomCleaner.png:	DicomCleaner_1024.png
	sips -z 128 128 $< --out $@




