path=.;.\lib\additional;%path%
java -Xmx384m -Xms384m -cp ".\pixelmed.jar;.\lib\additional\pixelmed_imageio.jar;.\lib\additional\commons-compress-1.12.jar;.\lib\additional\commons-codec-1.3.jar;.\lib\additional\jai_imageio.jar;.\lib\additional\clibwrapper_jiio.jar" com.pixelmed.apps.DeidentifyAndRedact %1 %2 %3
pause

