# Pixelmed DICOM Toolkit

This is an initial personal attempt of turning the Pixelmed DICOM Toolkit from Dr. David Clunie into a github project, switching from Makefiles to maven and integrating it into the build pipeline.


## Origin

The origin of the code can be found here, which is under the BSD Licence:
https://www.pixelmed.com/dicomtoolkit.html

## Work Done

- Moved all bash and shell scripts into 'scripts' folder
- Moved all java code into standard java folder structure (src/main/java)
- Moved tests into standard java folder structure (src/test/java)
- Moved Webstart to src/main/webstart
- Removed Duplicate classes
- Added most dependecies or possible replacements into [pom.xml](pom.xml)

## Roadmap

- Identify dependencies and manage them using maven
- Compile using maven
- Implement build pipeline
- Use standard java resources structure for files like properties and CSV files
- Identify and create maven modules for separate components
- Run quality analysis for identifying possible improvements
...

## License

[BSD Licence](COPYRIGHT)