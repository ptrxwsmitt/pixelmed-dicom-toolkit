PATHTOROOT = ../../..

OBJS = \
DicomAndWebStorageServer.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

archive:
	tar -cvf - Makefile *.java | gzip -best > ../../../server.`date '+%Y%m%d'`.tar.gz

cleantestserver:
	# must match what is defined in testserver.properties
	rm -rf /tmp/testserverdb.* /tmp/testserverimages

testserver:	${OBJS}
	java -Djava.awt.headless=true -Xmx256m -cp ${PATHTOROOT}:${SERVERADDITIONALJARS} \
		-Dorg.slf4j.simpleLogger.defaultLogLevel=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.database.DatabaseInformationModel=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.database.DicomDatabaseQueryResponseGenerator=info \
		-Dorg.slf4j.simpleLogger.log.com.pixelmed.network.StorageSOPClassSCP=debug \
		-Dorg.slf4j.simpleLogger.showDateTime=true \
		com.pixelmed.server.DicomAndWebStorageServer \
		testserver.properties

testservernoargs:	${OBJS}
	java  -Djava.awt.headless=true -Xmx256m -cp ${PATHTOROOT}:${SERVERADDITIONALJARS} com.pixelmed.server.DicomAndWebStorageServer

testserverwithjaronly:
	java  -Djava.awt.headless=true -Xmx256m -cp ${PATHTOROOT}/pixelmed.jar:${PATHTOADDITIONAL}/aiviewer.jar:${SERVERADDITIONALJARS} com.pixelmed.server.DicomAndWebStorageServer testserver.properties

graymaxserver:
	java  -Djava.awt.headless=true -Xmx256m -cp ${PATHTOROOT}/pixelmed.jar:${PATHTOADDITIONAL}/aiviewer.jar:${SERVERADDITIONALJARS} com.pixelmed.server.DicomAndWebStorageServer graymax.properties
