path=.;.\lib\additional;%path%
.\windows\jre\bin\java -Djdk.xml.xpathExprGrpLimit=0 -Djdk.xml.xpathExprOpLimit=0 -Djdk.xml.xpathTotalOpLimit=0 -Xmx384m -Xms384m -cp ".\pixelmed.jar;.\lib\additional\pixelmed_imageio.jar;.\lib\additional\hsqldb.jar;.\lib\additional\commons-compress-1.12.jar;.\lib\additional\vecmath1.2-1.14.jar;.\lib\additional\commons-codec-1.3.jar;.\lib\additional\jmdns.jar;.\lib\additional\jai_imageio.jar;.\lib\additional\clibwrapper_jiio.jar" com.pixelmed.display.DicomImageViewer .\dicomdir
pause
