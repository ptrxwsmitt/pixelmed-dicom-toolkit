path=.;.\lib\additional;.\windows\lib;%path%
java -Xms256m -Xmx768m -Xss1m -cp ".\pixelmed.jar;.\lib\additional\pixelmed_imageio.jar;.\lib\additional\hsqldb.jar;.\lib\additional\commons-compress-1.12.jar;.\lib\additional\vecmath1.2-1.14.jar;.\lib\additional\commons-codec-1.3.jar;.\lib\additional\commons-net-ftp-2.0.jar;.\lib\additional\jmdns.jar;.\windows\lib\clibwrapper_jiio.jar;.\windows\lib\jai_imageio.jar" com.pixelmed.apps.DoseUtility
pause
