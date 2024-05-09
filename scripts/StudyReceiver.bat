path=.;.\lib\additional;%path%
java -Djava.awt.headless=true -Xmx256m -cp ".\pixelmed.jar;.\lib\additional\commons-compress-1.12.jar;.\lib\additional\commons-codec-1.3.jar;.\lib\additional\hsqldb.jar" com.pixelmed.apps.StudyReceiver studyreceiver.properties
pause
