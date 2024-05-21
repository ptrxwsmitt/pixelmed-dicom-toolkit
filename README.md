# Pixelmed DICOM Toolkit

This is an initial personal attempt of turning the Pixelmed DICOM Toolkit from Dr. David Clunie into a github project, switching from Makefiles to maven and integrating it into the build pipeline.

## About DICOM

Dicom is both, a technical format and a semantic reference for storing medical data into a file. \
It is mainly used for storing medical image data, such as CT or MRI Scans. \
Even if there is some common definition, different manufacturers tend to interpret the semantics of certain fields differently.

## Origin

The origin of the code can be found here, which is under the BSD Licence:
https://www.pixelmed.com/dicomtoolkit.html

## Disclaimer

The current state of this code is currently not recommended for production use.

## Status

WiP

### Work Done

- Moved all bash and shell scripts into folder 'scripts'
- Moved all java code into standard java folder structure 'src/main/java'
- Moved tests into standard java folder structure 'src/test/java'
- Moved Webstart to 'src/main/webstart'
- Removed Duplicate classes (might use git version branches instead, later)
- Added most dependecies or possible replacements into [pom.xml](pom.xml)
- Replaced depreacted JSON dependency with json.org
- Replaced depreacted CSV Library with openCSV
- Added codec as separate module
- Turned dicom code into dicom module


### Roadmap

- Run Test using maven
- Implement build pipeline
- Fix un-handled memory leaks e.g. on exceptions
- Apply java best practices to the code
- Use standard java resources structure for files like properties and CSV files
- Identify and create maven modules for separate components
- Run quality analysis for identifying possible improvements
- Codec Module: Extract Tests from Makefiles
- ...


### License

[BSD Licence](COPYRIGHT)