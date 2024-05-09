@rem To always send to a particular DICOM Storage SCP the contents of a particular drive or folder, edit the following variables:

@set host=192.168.1.100
@set port=4006
@set calledaet=HELGRAY
@set callingaet=IMPORTER
@set driveorpath=H:\

@echo Reading from drive or path %driveorpath% and sending to %host% %port% %calledaet%
@java -Xms128m -Xmx512m -cp ".\pixelmed.jar;.\lib\additional\commons-compress-1.12.jar;.\lib\additional\commons-codec-1.3.jar" com.pixelmed.network.NetworkMediaImporter %host% %port% %calledaet% %callingaet% %driveorpath%
@pause
