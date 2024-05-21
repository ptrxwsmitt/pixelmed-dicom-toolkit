PATHTOROOT = ../../..

OBJS = \
	AppletDisplayRequestHandler.class \
	HttpServer.class \
	ImageDisplayRequestHandler.class \
	InstanceListRequestHandler.class \
	PathRequestHandler.class \
	PatientListRequestHandler.class \
	RequestHandler.class \
	RequestTypeServer.class \
	SeriesListRequestHandler.class \
	StudyListRequestHandler.class \
	WadoRequest.class \
	WadoRequestHandler.class \
	WadoServer.class \
	WebRequest.class \
	WebServerApplicationProperties.class \
	favicon.ico

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

# https://en.wikipedia.org/wiki/Favicon
# Manually created favicon.png in AI from inverted logo

# http://www.winterdrache.de/freeware/png2ico/
# port install png2ico
# NB. unusual reverse order of arguments ... USAGE: png2ico icofile [--colors <num>] pngfile1 [pngfile2 ...]
# .ico is already added to .SUFFIXES in Makefile.common.mk
.png.ico:
	png2ico $@ $<

archive:
	tar -cvf - Makefile *.java | gzip -best > ../../../network.`date '+%Y%m%d'`.tar.gz

testwado:	WadoRequest.class WebRequest.class
	java -cp ${PATHTOROOT} com.pixelmed.web.WadoRequest

testhttp:	HttpServer.class 
	java -cp ${PATHTOROOT} com.pixelmed.web.HttpServer

testwadoserver:	${OBJS}
	java  -Xms128m -Xmx512m -cp ${PATHTOROOT}:${SERVERADDITIONALJARS} com.pixelmed.web.WadoServer

testrequestserver:	${OBJS}
	java  -Djava.awt.headless=true -Xms128m -Xmx512m -cp ${PATHTOROOT}:${SERVERADDITIONALJARS} com.pixelmed.web.RequestTypeServer /tmp/testwadodb /tmp/testwadoimages 4007 WADOTEST 7091 localhost APPLETDISPLAY 0 2

archiveaiviewer:
	jar cvf ${PATHTOADDITIONAL}/aiviewer.jar dicomviewer/*.class dicomviewer/Dicom.dic

