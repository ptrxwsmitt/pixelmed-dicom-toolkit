# Pixelmed DICOM Toolkit

This is an initial personal attempt of turning the Pixelmed DICOM Toolkit from Dr. David Clunie into a github project, switching from Makefiles to maven and integrating it into the build pipeline.


## Origin

The origin of the code can be found here, which is under the BSD Licence:
https://www.pixelmed.com/dicomtoolkit.html

## Work Done

- Moved all bash and shell scripts into folder 'scripts'
- Moved all java code into standard java folder structure 'src/main/java'
- Moved tests into standard java folder structure 'src/test/java'
- Moved Webstart to 'src/main/webstart'
- Removed Duplicate classes (might use git version branches instead, later)
- Added most dependecies or possible replacements into [pom.xml](pom.xml)
- Replaced depreacted JSON dependency with json.org
- Replaced depreacted CSV Library with openCSV

## Roadmap

- Still some work to do to fully replace the old JSON Library
- Compile using maven
- Run Test using maven
- Implement build pipeline
- Use standard java resources structure for files like properties and CSV files
- Identify and create maven modules for separate components
- Run quality analysis for identifying possible improvements
...

## License

[BSD Licence](COPYRIGHT)