/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.convert;

import com.pixelmed.apps.SetCharacteristicsFromSummary;
import com.pixelmed.apps.TiledPyramid;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.AttributeTagAttribute;
import com.pixelmed.dicom.BinaryOutputStream;
import com.pixelmed.dicom.ClinicalTrialsAttributes;
import com.pixelmed.dicom.CodeStringAttribute;
import com.pixelmed.dicom.CodedSequenceItem;
import com.pixelmed.dicom.CodingSchemeIdentification;
import com.pixelmed.dicom.CompressedFrameDecoder;
import com.pixelmed.dicom.CompressedFrameEncoder;
import com.pixelmed.dicom.DateAttribute;
import com.pixelmed.dicom.DateTimeAttribute;
import com.pixelmed.dicom.DecimalStringAttribute;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.FloatDoubleAttribute;
import com.pixelmed.dicom.FloatSingleAttribute;
import com.pixelmed.dicom.FunctionalGroupUtilities;
import com.pixelmed.dicom.IntegerStringAttribute;
import com.pixelmed.dicom.LongStringAttribute;
import com.pixelmed.dicom.LongTextAttribute;
import com.pixelmed.dicom.OtherByteAttribute;
import com.pixelmed.dicom.OtherByteAttributeMultipleCompressedFrames;
import com.pixelmed.dicom.OtherByteAttributeMultipleFilesOnDisk;
import com.pixelmed.dicom.OtherFloatAttribute;
import com.pixelmed.dicom.OtherFloatAttributeMultipleFilesOnDisk;
import com.pixelmed.dicom.OtherWordAttribute;
import com.pixelmed.dicom.OtherWordAttributeMultipleFilesOnDisk;
import com.pixelmed.dicom.PersonNameAttribute;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.ShortStringAttribute;
import com.pixelmed.dicom.ShortTextAttribute;
import com.pixelmed.dicom.SOPClass;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TiledFramesIndex;
import com.pixelmed.dicom.TimeAttribute;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.dicom.UIDGenerator;
import com.pixelmed.dicom.UniqueIdentifierAttribute;
import com.pixelmed.dicom.UniversalResourceAttribute;
import com.pixelmed.dicom.UnlimitedTextAttribute;
import com.pixelmed.dicom.UnsignedLongAttribute;
import com.pixelmed.dicom.UnsignedShortAttribute;
import com.pixelmed.dicom.VersionAndConstants;

import com.pixelmed.display.BufferedImageUtilities;
import com.pixelmed.display.SourceImage;

import com.pixelmed.utils.ByteArray;
import com.pixelmed.utils.FileUtilities;
import com.pixelmed.utils.HexDump;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReadProc;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class for converting TIFF files into DICOM images of a specified or appropriate SOP Class.</p>
 *
 * <p>Defaults to producing single frame output unless a multi-frame SOP Class is explicitly
 * requested (e.g., for WSI, request Whole Slide Microscopy Image Storage, which is
 * "1.2.840.10008.5.1.4.1.1.77.1.6", and a Modality of "SM"), or there is information in the TIFF
 * metadata that is recognized as being appropriate for multi-frame conversion (e.g., SVS WSI files)
 * and auto-recognition has not been supressed.</p>
 *
 * <p>Supports conversion of tiled pyramidal whole slide images such as in Aperio/Leica SVS format.</p>
 *
 * <p>Supports creation of dual-personality DICOM-TIFF files using either classic TIFF or BigTIFF,
 * optionally with inclusion of a down-sampled pyramid inside the same file in a private DICOM attribute,
 * in order to support TIFF WSI viewers that won't work without a pyramid.</p>
 *
 * <p>Uses any ICC profile present in the TIFF file otherwise assumes sRGB.</p>
 *
 * <p>Uses a JSON summary description file as the source of identification and descriptive metadata
 * as described in {@link SetCharacteristicsFromSummary SetCharacteristicsFromSummary}.</p>
 *
 * <p>E.g.:</p>
 * <pre>
 * {
 * 	"top" : {
 * 		"PatientName" : "PixelMed^AperioCMU-1",
 * 		"PatientID" : "PX7832548325932",
 * 		"StudyID" : "S07-100",
 * 		"SeriesNumber" : "1",
 * 		"AccessionNumber" : "S07-100",
 * 		"ContainerIdentifier" : "S07-100 A 5 1",
 * 		"IssuerOfTheContainerIdentifierSequence" : [],
 * 		"ContainerTypeCodeSequence" : { "cv" : "433466003", "csd" : "SCT", "cm" : "Microscope slide" },
 * 		"SpecimenDescriptionSequence" : [
 * 	      {
 * 		    "SpecimenIdentifier" : "S07-100 A 5 1",
 * 		    "IssuerOfTheSpecimenIdentifierSequence" : [],
 * 		    "SpecimenUID" : "1.2.840.99790.986.33.1677.1.1.19.5",
 * 		    "SpecimenShortDescription" : "Part A: LEFT UPPER LOBE, Block 5: Mass (2 pc), Slide 1: H&amp;E",
 * 		    "SpecimenDetailedDescription" : "A: Received fresh for intraoperative consultation, labeled with the patient's name, number and 'left upper lobe,' is a pink-tan, wedge-shaped segment of soft tissue, 6.9 x 4.2 x 1.0 cm. The pleural surface is pink-tan and glistening with a stapled line measuring 12.0 cm. in length. The pleural surface shows a 0.5 cm. area of puckering. The pleural surface is inked black. The cut surface reveals a 1.2 x 1.1 cm, white-gray, irregular mass abutting the pleural surface and deep to the puckered area. The remainder of the cut surface is red-brown and congested. No other lesions are identified. Representative sections are submitted. Block 5: 'Mass' (2 pieces)",
 * 		    "SpecimenPreparationSequence" : [
 * 		      {
 * 			    "SpecimenPreparationStepContentItemSequence" : [
 * 			      {
 * 		    		"ValueType" : "TEXT",
 * 					"ConceptNameCodeSequence" : { "cv" : "121041", "csd" : "DCM", "cm" : "Specimen Identifier" },
 * 		    		"TextValue" : "S07-100 A 5 1"
 * 			      },
 * 			      {
 * 		    		"ValueType" : "CODE",
 * 					"ConceptNameCodeSequence" : { "cv" : "111701", "csd" : "DCM", "cm" : "Processing type" },
 * 					"ConceptCodeSequence" :     { "cv" : "127790008", "csd" : "SCT", "cm" : "Staining" }
 * 			      },
 * 			      {
 * 		    		"ValueType" : "CODE",
 * 					"ConceptNameCodeSequence" : { "cv" : "424361007", "csd" : "SCT", "cm" : "Using substance" },
 * 					"ConceptCodeSequence" :     { "cv" : "12710003", "csd" : "SCT", "cm" : "hematoxylin stain" }
 * 			      },
 * 			      {
 * 		    		"ValueType" : "CODE",
 * 					"ConceptNameCodeSequence" : { "cv" : "424361007", "csd" : "SCT", "cm" : "Using substance" },
 * 					"ConceptCodeSequence" :     { "cv" : "36879007", "csd" : "SCT", "cm" : "water soluble eosin stain" }
 * 			      }
 * 			    ]
 * 		      }
 * 		    ],
 * 		    "PrimaryAnatomicStructureSequence" : { "cv" : "44714003", "csd" : "SCT", "cm" : "Left Upper Lobe of Lung" }
 * 	      }
 * 		],
 * 		"OpticalPathSequence" : [
 * 	      {
 * 		    "OpticalPathIdentifier" : "1",
 * 		    "IlluminationColorCodeSequence" : { "cv" : "414298005", "csd" : "SCT", "cm" : "Full Spectrum" },
 * 		    "IlluminationTypeCodeSequence" :  { "cv" : "111744",  "csd" : "DCM", "cm" : "Brightfield illumination" }
 * 	      }
 * 		]
 * 	}
 * }
 * </pre>
 *
 * Improvements to this class have been funded in part with Federal funds from the
 * National Cancer Institute, National Institutes of Health, under Task Order No.
 * HHSN26110071 under Contract No. HHSN261201500003l (Imaging Data Commons).
 *
 * @see	SetCharacteristicsFromSummary
 * @see	TiledPyramid
 * @see	SOPClass
 *
 * @author	dclunie
 */

public class TIFFToDicom {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/convert/TIFFToDicom.java,v 1.129 2024/02/15 14:04:15 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(TIFFToDicom.class);
	
	private List<File> filesToDeleteAfterWritingDicomFile = null;

	private UIDGenerator u = new UIDGenerator();

	private static final String pixelmedPrivateCreator = "PixelMed Publishing";

	private static final int pixelmedPrivateOriginalFileNameDataGroup = 0x0009;
	private static final AttributeTag pixelmedPrivateOriginalFileNameDataBlockReservation = new AttributeTag(pixelmedPrivateOriginalFileNameDataGroup,0x0010);
	private static final AttributeTag pixelmedPrivateOriginalFileName = new AttributeTag(pixelmedPrivateOriginalFileNameDataGroup,0x1001);
	private static final AttributeTag pixelmedPrivateOriginalTIFFIFDIndex = new AttributeTag(pixelmedPrivateOriginalFileNameDataGroup,0x1002);
	
	private static boolean isValidJPEGFamilyBitstream(byte[] bytes) {	// (001382)
		// just check begins with SOI (JPEG) or SOC (J2K)
		int l = bytes.length;
		return (l > 2
		 &&  (bytes[0]&0xff) == 0xff
		 && ((bytes[1]&0xff) == 0xd8 || (bytes[1]&0xff) == 0x4f));
	}

	private static byte[] stripSOIEOIMarkers(byte[] bytes) {
		byte[] newBytes = null;
		int l = bytes.length;
		if (l >= 4
		 && (bytes[0]&0xff) == 0xff
		 && (bytes[1]&0xff) == 0xd8
		 && (bytes[l-2]&0xff) == 0xff
		 && (bytes[l-1]&0xff) == 0xd9) {
			if (l > 4) {
				int newL = l-4;
				newBytes = new byte[newL];
				System.arraycopy(bytes,2,newBytes,0,newL);
			}
			// else leave it null since now empty
		}
		else {
			slf4jlogger.error("stripSOIEOIMarkers(): Unable to remove SOI and EOI markers");
			newBytes = bytes;
		}
		return newBytes;
	}
	
	private static byte[] insertJPEGTablesIntoAbbreviatedBitStream(byte[] bytes,byte[] jpegTables) {
		byte[] newBytes = null;
		int l = bytes.length;
		if (l > 2
		 && (bytes[0]&0xff) == 0xff
		 && (bytes[1]&0xff) == 0xd8) {
			int tableL = jpegTables.length;
			int newL = l + tableL;
			newBytes = new byte[newL];
			System.arraycopy(bytes,     0,newBytes,0,       2);
			System.arraycopy(jpegTables,0,newBytes,2,       tableL);
			System.arraycopy(bytes,     2,newBytes,2+tableL,l-2);
		}
		else {
			slf4jlogger.error("insertJPEGTablesIntoAbbreviatedBitStream(): Unable to insert JPEG Tables");
			slf4jlogger.error("insertJPEGTablesIntoAbbreviatedBitStream(): Unable to insert JPEG Tables: l = {}",l);
			if (l >=2 ) {
				slf4jlogger.error("insertJPEGTablesIntoAbbreviatedBitStream(): Unable to insert JPEG Tables: bytes[0]&0xff = {}",HexDump.byteToPaddedHexString(bytes[0]&0xff));
				slf4jlogger.error("insertJPEGTablesIntoAbbreviatedBitStream(): Unable to insert JPEG Tables: bytes[1]&0xff = {}",HexDump.byteToPaddedHexString(bytes[1]&0xff));
				if (slf4jlogger.isTraceEnabled()) {
					try {
						File tmpFile = File.createTempFile("TIFFToDicom_insertJPEGTablesIntoAbbreviatedBitStream_badframe",".jpeg");
						BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(tmpFile));
						o.write(bytes);
						o.flush();
						o.close();
						slf4jlogger.trace("insertJPEGTablesIntoAbbreviatedBitStream(): wrote bad JPEG frame to {}",tmpFile.toString());
						// do not delete on exit
					}
					catch (IOException e) {
						slf4jlogger.trace("insertJPEGTablesIntoAbbreviatedBitStream(): unable to write bad JPEG frame: {}",e);
					}
				}
			}
			newBytes = bytes;
		}
		return newBytes;
	}
	
	// http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/JPEG.html#Adobe
	// per JPEG-EPS.pdf 18 Adobe Application-Specific JPEG Marker
	// http://fileformats.archiveteam.org/wiki/JPEG#Color_format
	// http://docs.oracle.com/javase/8/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html#color
	// http://exiftool.org/forum/index.php?topic=8695.0
	
	private static byte[] AdobeAPP14_RGB = {
		(byte)0xFF, (byte)0xEE,
		(byte)0x00, (byte)14,	/* big endian length includes length itself but not the marker */
		(byte)'A', (byte)'d', (byte)'o', (byte)'b', (byte)'e',
		(byte)0x00,(byte)0x65, /* DCTEncodeVersion 0x65 */
		(byte)0x00,(byte)0x00, /* APP14Flags0 0 */
		(byte)0x00,(byte)0x00, /* APP14Flags1 0 */
		(byte)0x00 /* ColorTransform 0 = Unknown (RGB or CMYK) */
	};
	
	private static byte[] insertAdobeAPP14WithRGBTransformIntoBitStream(byte[] bytes) {
		byte[] newBytes = null;
		int l = bytes.length;
		if (l > 2
		 && (bytes[0]&0xff) == 0xff
		 && (bytes[1]&0xff) == 0xd8) {
			int app14L = AdobeAPP14_RGB.length;
			int newL = l + app14L;
			newBytes = new byte[newL];
			System.arraycopy(bytes,         0,newBytes,0,       2);
			System.arraycopy(AdobeAPP14_RGB,0,newBytes,2,       app14L);
			System.arraycopy(bytes,         2,newBytes,2+app14L,l-2);
		}
		else {
			slf4jlogger.error("insertAdobeAPP14WithRGBTransformIntoBitStream(): Unable to insert APP14");
			newBytes = bytes;
		}
		return newBytes;
	}
	
	private static byte[] removeAPP0Segment(byte[] bytes) {
		// Offset 0x0000 Marker 0xffd8 SOI Start of Image
		// Offset 0x0002 Marker 0xffe0 APP0 Reserved for Application Use length variable 0x10
		byte[] newBytes = null;
		int l = bytes.length;
		if (l >= 6
		 && (bytes[2]&0xff) == 0xff
		 && (bytes[3]&0xff) == 0xe0) {
		 	// have APP0 segment that is expected to be JFIF header
		 	// variable length - length in 16 bits big endian
		 	int lseg = (bytes[4]&0xff) << 8 | (bytes[5]&0xff);
			slf4jlogger.trace("removeAPP0Segment(): lseg = {}",lseg);
		 	if (lseg > 0) {
		 		// remove 2 byte marker (not 2 byte length since included in lseg) + lseg bytes from array from offset 2
		 		int remove = 2 + lseg;
				slf4jlogger.trace("removeAPP0Segment(): remove = {}",remove);
		 		int newL = l - remove;
				newBytes = new byte[newL];
		 		int afterEndOfRemoved = 2 + remove;
		 		int remaining = newL - 2;
				System.arraycopy(bytes,                0,newBytes,0,        2);
				System.arraycopy(bytes,afterEndOfRemoved,newBytes,2,remaining);
				slf4jlogger.trace("removeAPP0Segment(): Removed APP0 segment");
			}
			else {
				slf4jlogger.warn("removeAPP0Segment(): APP0 segment with zero length - not removing");
			}
		}
		else {
			slf4jlogger.trace("removeAPP0Segment(): No APP0 segment to remove");
			newBytes = bytes;
		}
		return newBytes;
	}

	private static byte[] createEmptyCompressedTile(long tileWidth,long tileLength,long compression,long photometric,long samplesPerPixel) {
		byte[] values = null;
		String format = null;
		if (compression == 7) {
			format = "jpeg";
		}
		else if (compression == 33003 || compression == 33005) {
			format = "jpeg2000";
		}
		else {
			slf4jlogger.error("Unrecognized compression "+compression+" while trying to create empty compressed tile");
		}
		if (format != null) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				if (samplesPerPixel == 3 && (photometric == 2 || photometric == 6)) {	// RGB or YBR
					// ignore the color space and the color profile - it is black anyway :(
					// ignore whether it is lossy or not - there will probably be a JFIF header too :(
					BufferedImage img = new BufferedImage((int)tileWidth,(int)tileLength,BufferedImage.TYPE_INT_RGB);
					ImageIO.write(img,format,bos);
					values =  bos.toByteArray();
					values = removeAPP0Segment(values);
				}
				else {
					slf4jlogger.error("Unsupported samplesPerPixel "+samplesPerPixel+" or phommetric "+photometric+" while trying to create empty compressed tile");
				}
			}
			catch (Exception e) {
				slf4jlogger.error("Attempt to use compressed writer to create empty compressed tile failed",e);
			}
		}
		return values;
	}

	private static boolean encounteredZeroLengthTiles = false;
	private static boolean encounteredInvalidJPEGFamilyBitstreamTiles = false;
	
	private static byte[] readJPEGFamilyCompressedPixelValuesAndFixAsNecessary(TIFFFile inputFile,int tileNumber,long pixelOffset,long pixelByteCount,byte[] jpegTables,long tileWidth,long tileLength,long compression,long photometric,long samplesPerPixel) throws IOException, TIFFException {
		byte[] values = null;
		if (pixelByteCount == 0) {
			if (!encounteredZeroLengthTiles) {
				slf4jlogger.warn("Encountered tiles with pixelByteCount of zero - assume empty tile");
				encounteredZeroLengthTiles = true;
			}
			slf4jlogger.trace("For frame {}, pixelByteCount is zero - assume empty tile",tileNumber);
			values = createEmptyCompressedTile(tileWidth,tileLength,compression,photometric,samplesPerPixel);	// will have JPEG tables and APP14 if necessary
			if (values == null || values.length == 0) {
				throw new TIFFException("For frame "+tileNumber+", pixelByteCount is zero and could not create empty compressed tile");
			}
		}
		else {
			values = new byte[(int)pixelByteCount];
			inputFile.seek(pixelOffset);
			{
				int nread = inputFile.read(values);
				slf4jlogger.trace("For frame {}, bytes read = {}, expected = {}",tileNumber,nread,pixelByteCount);
				if (nread != (int)pixelByteCount) {
					throw new TIFFException("For frame "+tileNumber+", bytes read "+nread+" not equal to compressed pixelByteCount to be read "+pixelByteCount);
				}
			}
			if (isValidJPEGFamilyBitstream(values)) {	// (001382)
				if (compression == 7/*JPEG*/) {
					values = removeAPP0Segment(values);	// Will conflict with APP14, and don't want it regardless (001352) and need to remove before adding anything (001383)
					if (jpegTables != null) {
						values = insertJPEGTablesIntoAbbreviatedBitStream(values,jpegTables);
					}
					if (photometric == 2/*RGB*/) {
						slf4jlogger.trace("JPEG RGB so adding APP14");
						values = insertAdobeAPP14WithRGBTransformIntoBitStream(values);
					}
				}
			}
			else {
				// (001382)
				if (!encounteredInvalidJPEGFamilyBitstreamTiles) {
					slf4jlogger.warn("Encountered tiles with invalid JPEG family bitstream - assume empty tile");
					encounteredInvalidJPEGFamilyBitstreamTiles = true;
				}
				slf4jlogger.trace("For frame {}, invalid JPEG family bitstream - assume empty tile",tileNumber);
				values = createEmptyCompressedTile(tileWidth,tileLength,compression,photometric,samplesPerPixel);	// will have JPEG tables and APP14 if necessary
				if (values == null || values.length == 0) {
					throw new TIFFException("For frame "+tileNumber+", invalid JPEG family bitstream and could not create empty compressed tile");
				}
			}
		}
		return values;
	}

	private class UIDMap {
	
		private Map<String,String> studyInstanceUIDByFileName = new HashMap<String,String>();
		private Map<String,String> seriesInstanceUIDByFileName = new HashMap<String,String>();
		private Map<String,String> specimenUIDByFileName = new HashMap<String,String>();
		private Map<String,String> pyramidUIDByFileName = new HashMap<String,String>();
		private Map<String,String> acquisitionUIDByFileName = new HashMap<String,String>();
		
		private Map<String,Map<String,String>> sopInstanceUIDsByFileName = new HashMap<String,Map<String,String>>();
		private Map<String,Map<String,String>> frameOfReferenceUIDsByFileName = new HashMap<String,Map<String,String>>();
		private Map<String,Map<String,String>> dimensionOrganizationUIDsByFileName = new HashMap<String,Map<String,String>>();

		private class UIDMapCSVReadProc implements CSVReadProc {
			private String[] columnNames = null;
			
			private void process(String keyword,String fileName,String uid,Map<String,String> map) {
				if (fileName != null && fileName.length() > 0) {
					if (uid != null && uid.length() > 0) {
						String existing = map.get(fileName);
						if (existing == null) {
							slf4jlogger.debug("UIDMap.UIDMapCSVReadProc.process(): Adding {} for file {} to {} map",uid,fileName,keyword);
							map.put(fileName,uid);
						}
						else if (!existing.equals(uid)) {
							slf4jlogger.error("Different {} for filename {} {} than previously encountered {} - ignoring",keyword,fileName,uid,existing);
						}
						// else same so OK
					}
					else {
						slf4jlogger.error("Missing uid in UID map entry for filename {} for {}",fileName,keyword);
					}
				}
				else {
					slf4jlogger.error("Missing filename in UID map entry");
				}
			}
			
			private void process(String keyword,String fileName,String ifd,String uid,Map<String,Map<String,String>> map) {
				if (fileName != null && fileName.length() > 0) {
					if (ifd != null && ifd.length() > 0) {
						if (uid != null && uid.length() > 0) {
							Map<String,String> existingUIDByIFD = map.get(fileName);
							if (existingUIDByIFD == null) {
								existingUIDByIFD = new HashMap<String,String>();
								map.put(fileName,existingUIDByIFD);
							}
							String existing = existingUIDByIFD.get(ifd);
							if (existing == null) {
								slf4jlogger.debug("UIDMap.UIDMapCSVReadProc.process(): Adding {} for file {} IFD {} to {} map",uid,fileName,ifd,keyword);
								existingUIDByIFD.put(ifd,uid);
							}
							else if (!existing.equals(uid)) {
								slf4jlogger.error("Different {} for filename {} IFD {} {} than previously encountered {} - ignoring",keyword,fileName,ifd,uid,existing);
							}
							// else same so OK
						}
						else {
							slf4jlogger.error("Missing uid in UID map entry for filename {} IFD {} for {}",fileName,ifd,keyword);
						}
					}
					else {
						slf4jlogger.error("Missing IFD in UID map entry for filename {} for {}",fileName,keyword);
					}
				}
				else {
					slf4jlogger.error("Missing filename in UID map entry");
				}
			}
			
			public void procRow(int rowIndex, String... values) {
				//System.err.println("values.length = "+values.length);
				if (values.length > 0) {
					if (values[0].contains("Filename")) {
						// "Filename","IFD","Keyword","UID"
						columnNames = values;
					}
					else {
						// "110356/11456.svs","5","SOPInstanceUID","1.3.6.1.4.1.5962.99.1.498297511.1205746030.1641175837351.42.0"
						Map<String,String> row = new HashMap<String,String>();
						for (int i=0; i<values.length; ++i) {
							String value = values[i];
							if (value.length() > 0) {
								row.put(columnNames[i],value);
							}
						}
						{
							String fileName = row.get("Filename");
							String ifd = row.get("IFD");
							String keyword = row.get("Keyword");
							String uid = row.get("UID");
							
							if (keyword != null && keyword.length() > 0) {
								if (keyword.equals("StudyInstanceUID")) {
									process(keyword,fileName,uid,studyInstanceUIDByFileName);
								}
								else if (keyword.equals("SeriesInstanceUID")) {
									process(keyword,fileName,uid,seriesInstanceUIDByFileName);
								}
								else if (keyword.equals("SOPInstanceUID")) {
									process(keyword,fileName,ifd,uid,sopInstanceUIDsByFileName);
								}
								else if (keyword.equals("FrameOfReferenceUID")) {
									process(keyword,fileName,ifd,uid,frameOfReferenceUIDsByFileName);
								}
								else if (keyword.equals("SpecimenUID")) {
									process(keyword,fileName,uid,specimenUIDByFileName);
								}
								else if (keyword.equals("DimensionOrganizationUID")) {
									process(keyword,fileName,ifd,uid,dimensionOrganizationUIDsByFileName);
								}
								else if (keyword.equals("PyramidUID")) {
									process(keyword,fileName,uid,pyramidUIDByFileName);
								}
								else if (keyword.equals("AcquisitionUID")) {
									process(keyword,fileName,uid,acquisitionUIDByFileName);
								}
							}
							else {
								slf4jlogger.error("Missing keyword in UID map entry for filename {} IFD {}",fileName,ifd);
							}
						}
					}
				}
			}
		}
		
		UIDMap(String uidFileName) {
			slf4jlogger.debug("Using UIDs from UID file {}",uidFileName);
			CSV csv = CSV
				.separator(',')  // delimiter of fields
				.quote('"')      // quote character
				.create();       // new instance is immutable
				
			csv.read(uidFileName,new UIDMapCSVReadProc());
		}
		
		String getUIDForFileNameAndIFDFromMap(Map<String,Map<String,String>> map,String inputFileName,int dirNum,String keyword) {
			// may not be exact match since may have path preamble - use endsWith(), which entails a search ...
			for (String f : map.keySet()) {
				if (inputFileName.endsWith(f)) {
					Map<String,String> ifdmap = map.get(f);
					if (ifdmap != null) {
						String ifd = Integer.toString(dirNum);
						slf4jlogger.debug("Looking for {} ifd {} in IFD map for file {}",keyword,ifd,inputFileName);
						String uid = ifdmap.get(ifd);
						if (uid != null && uid.length() > 0) {
							return uid;
						}
						else {
							slf4jlogger.error("Could not find {} dirNum {} in IFD map for file {}",keyword,dirNum,inputFileName);
							return null;
						}
					}
					else {
						slf4jlogger.error("Could not find {} IFD map for file {} in map",keyword,inputFileName);
						return null;
					}
				}
			}
			slf4jlogger.debug("Could not find {} match for file {} in map",keyword,inputFileName);
			return null;
		}
		
		String getUIDForFileNameFromMap(Map<String,String> map,String inputFileName,String keyword) {
			// may not be exact match since may have path preamble - use endsWith(), which entails a search ...
			for (String f : map.keySet()) {
				if (inputFileName.endsWith(f)) {
					String uid = map.get(f);
					return uid;
				}
			}
			slf4jlogger.debug("Could not find {} match for file {} in map",keyword,inputFileName);
			return null;
		}

		void replaceUIDs(AttributeList list,String inputFileName,int dirNum) throws DicomException {
			{
				String uid = getUIDForFileNameFromMap(studyInstanceUIDByFileName,inputFileName,"StudyInstanceUID");
				if (uid != null && uid.length() > 0) {
					slf4jlogger.debug("Using supplied StudyInstanceUID {}",uid);
					Attribute a; a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(uid); list.put(a);
				}
			}
			{
				String uid = getUIDForFileNameFromMap(seriesInstanceUIDByFileName,inputFileName,"SeriesInstanceUID");
				if (uid != null && uid.length() > 0) {
					slf4jlogger.debug("Using supplied SeriesInstanceUID {}",uid);
					Attribute a; a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(uid); list.put(a);
				}
			}
			{
				String uid = getUIDForFileNameAndIFDFromMap(sopInstanceUIDsByFileName,inputFileName,dirNum,"SOPInstanceUID");
				if (uid != null && uid.length() > 0) {
					slf4jlogger.debug("Using supplied SOPInstanceUID {}",uid);
					Attribute a; a = new UniqueIdentifierAttribute(TagFromName.SOPInstanceUID); a.addValue(uid); list.put(a);
				}
			}
			{
				String uid = getUIDForFileNameAndIFDFromMap(frameOfReferenceUIDsByFileName,inputFileName,dirNum,"FrameOfReferenceUID");
				if (uid != null && uid.length() > 0) {
					slf4jlogger.debug("Using supplied FrameOfReferenceUID {}",uid);
					Attribute a; a = new UniqueIdentifierAttribute(TagFromName.FrameOfReferenceUID); a.addValue(uid); list.put(a);
				}
			}
			{
				String uid = getUIDForFileNameFromMap(pyramidUIDByFileName,inputFileName,"PyramidUID");
				if (uid != null && uid.length() > 0) {
					slf4jlogger.debug("Using supplied PyramidUID {}",uid);
					Attribute a; a = new UniqueIdentifierAttribute(TagFromName.PyramidUID); a.addValue(uid); list.put(a);
				}
			}
			{
				String uid = getUIDForFileNameFromMap(acquisitionUIDByFileName,inputFileName,"AcquisitionUID");
				if (uid != null && uid.length() > 0) {
					slf4jlogger.debug("Using supplied AcquisitionUID {}",uid);
					Attribute a; a = new UniqueIdentifierAttribute(TagFromName.AcquisitionUID); a.addValue(uid); list.put(a);
				}
			}
			{
				String uid = getUIDForFileNameFromMap(specimenUIDByFileName,inputFileName,"SpecimenUID");
				if (uid != null && uid.length() > 0) {
					slf4jlogger.debug("Using supplied SpecimenUID {}",uid);
					SequenceAttribute aSpecimenDescriptionSequence = (SequenceAttribute)(list.get(TagFromName.SpecimenDescriptionSequence));
					if (aSpecimenDescriptionSequence != null) {
						Iterator<SequenceItem> sitems = aSpecimenDescriptionSequence.iterator();
						while (sitems.hasNext()) {
							AttributeList itemList = sitems.next().getAttributeList();
							{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SpecimenUID); a.addValue(uid); itemList.put(a); }
						}
					}
					else {
						slf4jlogger.error("Missing SpecimenDescriptionSequence - unable to replace SpecimenUID");
					}
				}
			}
			{
				String uid = getUIDForFileNameAndIFDFromMap(dimensionOrganizationUIDsByFileName,inputFileName,dirNum,"DimensionOrganizationUID");
				if (uid != null && uid.length() > 0) {
					slf4jlogger.debug("Using supplied DimensionOrganizationUID {}",uid);
					{
						SequenceAttribute aDimensionOrganizationSequence = (SequenceAttribute)(list.get(TagFromName.DimensionOrganizationSequence));
						if (aDimensionOrganizationSequence != null) {
							Iterator<SequenceItem> sitems = aDimensionOrganizationSequence.iterator();
							while (sitems.hasNext()) {
								AttributeList itemList = sitems.next().getAttributeList();
								{ Attribute a = new UniqueIdentifierAttribute(TagFromName.DimensionOrganizationUID); a.addValue(uid); itemList.put(a); }
							}
						}
						else {
							slf4jlogger.error("Missing DimensionOrganizationSequence - unable to replace DimensionOrganizationUID");
						}
					}
					{
						SequenceAttribute aDimensionIndexSequence = (SequenceAttribute)(list.get(TagFromName.DimensionIndexSequence));
						if (aDimensionIndexSequence != null) {
							Iterator<SequenceItem> sitems = aDimensionIndexSequence.iterator();
							while (sitems.hasNext()) {
								AttributeList itemList = sitems.next().getAttributeList();
								{ Attribute a = new UniqueIdentifierAttribute(TagFromName.DimensionOrganizationUID); a.addValue(uid); itemList.put(a); }
							}
						}
						else {
							slf4jlogger.error("Missing aDimensionIndexSequence - unable to replace DimensionOrganizationUID");
						}
					}
				}
			}
		}
	}

	private void addTotalPixelMatrixOriginSequence(AttributeList list,double xOffsetInSlideCoordinateSystem,double yOffsetInSlideCoordinateSystem) throws DicomException {
		SequenceAttribute aTotalPixelMatrixOriginSequence = new SequenceAttribute(DicomDictionary.StandardDictionary.getTagFromName("TotalPixelMatrixOriginSequence"));
		list.put(aTotalPixelMatrixOriginSequence);
		{
			AttributeList itemList = new AttributeList();
			aTotalPixelMatrixOriginSequence.addItem(itemList);
			{ Attribute a = new DecimalStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("XOffsetInSlideCoordinateSystem")); a.addValue(xOffsetInSlideCoordinateSystem); itemList.put(a); }
			{ Attribute a = new DecimalStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("YOffsetInSlideCoordinateSystem")); a.addValue(yOffsetInSlideCoordinateSystem); itemList.put(a); }
		}
	}

	private static byte[] convertPlanarConfigurationToColorByPixel(byte[] sourceValues,long tileWidth,long tileLength,long samplesPerPixel) {
		int pixelsPerFrame = (int)(tileWidth * tileLength);
		byte[] destinationValues = new byte[(int)(pixelsPerFrame*samplesPerPixel)];
		// after com.pixelmed.apps.ConvertPlanarConfiguration
		for (int pixel=0; pixel<pixelsPerFrame; ++pixel) {
			for (int c=0; c<samplesPerPixel; ++c) {
				int dstByPixelIndex = pixel * (int)samplesPerPixel +                  c;
				int srcByPlaneIndex = pixel                        + pixelsPerFrame * c;
				destinationValues[dstByPixelIndex] = sourceValues[srcByPlaneIndex];
			}
		}
		return destinationValues;
	}

	private static short[] convertPlanarConfigurationToColorByPixel(short[] sourceValues,long tileWidth,long tileLength,long samplesPerPixel) {
		int pixelsPerFrame = (int)(tileWidth * tileLength);
		short[] destinationValues = new short[(int)(pixelsPerFrame*samplesPerPixel)];
		// after com.pixelmed.apps.ConvertPlanarConfiguration
		for (int pixel=0; pixel<pixelsPerFrame; ++pixel) {
			for (int c=0; c<samplesPerPixel; ++c) {
				int dstByPixelIndex = pixel * (int)samplesPerPixel +                  c;
				int srcByPlaneIndex = pixel                        + pixelsPerFrame * c;
				destinationValues[dstByPixelIndex] = sourceValues[srcByPlaneIndex];
			}
		}
		return destinationValues;
	}

	private static void assertPixelDataEncodingIsCompatibleWithTransferSyntax(AttributeList list,String transferSyntaxUID) throws DicomException {
		boolean isEncapsulatedTransferSyntax = new TransferSyntax(transferSyntaxUID).isEncapsulated();
		Attribute a = list.get(TagFromName.PixelData);
		if (a != null) {
			if (isEncapsulatedTransferSyntax) {
				if (a instanceof OtherByteAttributeMultipleCompressedFrames
				// || a instanceof OtherByteAttributeMultipleCompressedFilesOnDisk
				// || a instanceof OtherByteAttributeCompressedSeparateFramesOnDisk
				) {
					// good
				}
				else {
					throw new DicomException("Encapsulated TransferSyntax "+transferSyntaxUID+" inconsistent with non-encapsulated pixel data encoded in "+a.getClass().toString());
				}
			}
			else {
				if (a instanceof OtherByteAttribute
				 || a instanceof OtherWordAttribute
				// || a instanceof OtherByteAttributeOnDisk
				// || a instanceof OtherWordAttributeOnDisk
				 || a instanceof OtherByteAttributeMultipleFilesOnDisk
				 || a instanceof OtherWordAttributeMultipleFilesOnDisk
				// || a instanceof OtherByteAttributeMultipleFrameArrays
				// || a instanceof OtherWordAttributeMultipleFrameArrays
				) {
					// good
				}
				else {
					throw new DicomException("Non-encapsulated TransferSyntax "+transferSyntaxUID+" inconsistent with encapsulated pixel data encoded in "+a.getClass().toString());
				}
			}
		}
		else {
			a = list.get(TagFromName.FloatPixelData);
			if (a != null) {
				if (isEncapsulatedTransferSyntax) {
					throw new DicomException("Encapsulated TransferSyntax "+transferSyntaxUID+" inconsistent with float pixel data encoded in "+a.getClass().toString());
				}
			}
			//else {
			//	a = list.get(TagFromName.DoublePixelData);
			//	if (isEncapsulatedTransferSyntax) {
			//		throw new DicomException("Encapsulated TransferSyntax "+transferSyntaxUID+" inconsistent with double pixel data encoded in "+a.getClass().toString());
			//	}
			//}
		}
	}

	/**
	 * <p>Create a multi-frame DICOM Pixel Data attribute from the TIFF pixel data, recompressing it if requested.</p>
	 *
	 * <p>Recompresses the frames if requested, returning an updated photometric value if changed by recompression.</p>
	 *
	 * <p>Otherwise uses the supplied compressed bitstream, fixing it if necessary to signal RGB if really RGB not YCbCr and
	 * inserting factored out JPEG tables to turn abbreviated into interchange format JPEG bitstreams.</p>
	 *
	 * @param	inputFile
	 * @param	list
	 * @param	mergeSamplesPerPixelTiles			whether or not to merge samplesPerPixel TIFF tiles into one DICOM tile
	 * @param	alwaysMakeColorByPixelPlanarConfig	whether or not to convert to color-by-pixel rather than color-by-plane if not that way already
	 * @param	numberOfSourceTiles					the number of TIFF tiles, i.e., length of tileOffsets and tileByteCounts arrays
	 * @param	numberOfDestinationTiles			the number of DICOM tiles
	 * @param	tileOffsets
	 * @param	tileByteCounts
	 * @param	tileWidth
	 * @param	tileLength
	 * @param	bitsPerSample
	 * @param	sampleFormat
	 * @param	compression				the compression value in the TIFF source
	 * @param	photometric				the photometric value in the TIFF source
	 * @param	samplesPerPixel
	 * @param	planarConfig			the planarConfig value in the TIFF source (1=color-by-pixel 2=color-by-plane)
	 * @param	predictor
	 * @param	jpegTables				the JPEG tables in the TIFF source to be inserted in to the abbreviated format JPEG stream to make interchange format or before decompression
	 * @param	iccProfile				the ICC Profile value in the TIFF source, if any
	 * @param	recompressAsFormat		scheme to recompress uncompressed or previously compressed data if different than what was read, either "jpeg" or "jpeg2000"
	 * @param	recompressLossy			use lossy rather than lossless recompression if supported by scheme (not yet implemented)
	 * @return							the updated TIFF photometric value, which may be changed by recompression
	 * @throws	IOException				if there is an error reading or writing
	 * @throws	DicomException			if the image cannot be compressed
	 * @throws	TIFFException
	 */
	private long generateDICOMPixelDataMultiFrameImageFromTIFFFile(TIFFFile inputFile,AttributeList list,
				boolean mergeSamplesPerPixelTiles,boolean alwaysMakeColorByPixelPlanarConfig,int numberOfSourceTiles,int numberOfDestinationTiles,
				long[] tileOffsets,long[] tileByteCounts,long tileWidth,long tileLength,
				long bitsPerSample,long sampleFormat,long compression,long photometric,long samplesPerPixel,long planarConfig,long predictor,
				byte[] jpegTables,byte[] iccProfile,
				String recompressAsFormat,boolean recompressLossy) throws IOException, DicomException, TIFFException {


		slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): compression = {}",compression);
		slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): predictor = {}",predictor);
		slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): planarConfig = {}",planarConfig);
		slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): samplesPerPixel = {}",samplesPerPixel);
		slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): bitsPerSample = {}",bitsPerSample);
		int bytesPerPixelForHorizontalDifferencing = (int)(planarConfig == 1 ? samplesPerPixel * bitsPerSample/8 : bitsPerSample/8);
		slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): bytesPerPixelForHorizontalDifferencing = {}",bytesPerPixelForHorizontalDifferencing);
		slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): sampleFormat = {}",sampleFormat);
		boolean isFloat = sampleFormat == 3;	// (001320)
		
		slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): mergeSamplesPerPixelTiles = {}",mergeSamplesPerPixelTiles);
		slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): alwaysMakeColorByPixelPlanarConfig = {}",alwaysMakeColorByPixelPlanarConfig);
		slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): numberOfSourceTiles = {}",numberOfSourceTiles);
		slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): numberOfDestinationTiles = {}",numberOfDestinationTiles);

		long expectedUncompressedTileByteCount = tileWidth * tileLength * (bitsPerSample/8) * samplesPerPixel;	// (001397)

		long outputPhotometric = photometric;
		
		if (list == null) {
			list = new AttributeList();
		}
		
		if (numberOfDestinationTiles > 2147483647l) {	// (2^31)-1 IS positive value limit
			throw new TIFFException("Number of tiles exceeds maximum IS value for NumberOfFrames = "+numberOfDestinationTiles);
		}
		if (tileWidth > 65535l || tileLength > 65535l) {	// maximum US value
			throw new TIFFException("tileWidth "+tileWidth+" and/or tileLength "+tileLength+" exceeds maximum US value for Columns and/or Rows");
		}
		
		if (compression == 0 || compression == 1	// absent or specified as uncompressed
		 || compression == 5) {					// LZW is always decompressed
			if (recompressAsFormat == null || recompressAsFormat.length() == 0) {
				slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): no output compression requested");
				boolean useSourceFileForOutputPixelData = false;
				File[] files = null;
				if (compression == 5) {
					slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): Decompressing LZW to write as uncompressed ");
					if (mergeSamplesPerPixelTiles) {	// (001351)
						slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): Using LZW tiles as source merging separate tiles per channel into one tile");
						long destinationTileNumberOfBytes = tileWidth * tileLength * samplesPerPixel * bitsPerSample/8;
						byte[] destinationValues = new byte[(int)destinationTileNumberOfBytes];
						files = new File[numberOfDestinationTiles];
						// TIFF6 p 68: "For PlanarConfiguration = 2, the offsets for the first component plane are stored first, followed by all the offsets for the second component plane, and so on."
						for (int destinationTileNumber=0; destinationTileNumber<numberOfDestinationTiles; ++destinationTileNumber) {
							int offsetIntoDestinationValues = 0;
							for (int c=0; c<samplesPerPixel; ++c) {
								int sourceTileNumber = c * numberOfDestinationTiles + destinationTileNumber;	// numberOfDestinationTiles is also the number of tiles per component in the source tiles
								long pixelOffset = tileOffsets[sourceTileNumber];
								long pixelByteCount = tileByteCounts[sourceTileNumber];
								//if (slf4jlogger.isDebugEnabled() && pixelByteCount != expectedSingleChannelTileSizeBytes) slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): tileByteCount[{}] {} != tileWidth * tileLength {}",sourceTileNumber,pixelByteCount,expectedSingleChannelTileSizeBytes);
								byte[] sourceValues = new byte[(int)pixelByteCount];
								inputFile.seek(pixelOffset);
								inputFile.read(sourceValues);

								sourceValues = lzwUncompress(sourceValues);
								if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): decompressed LZW tile length = {}",sourceValues.length);
								// apply horizontal differencing as per ij/io/ImageReader.java and TIFF6 page 64
								if (predictor == 2) {	// horizontalDifferencing
									reverseHorizontalDifferencing(sourceValues,bytesPerPixelForHorizontalDifferencing,tileWidth);
								}
							
								// not sure why this is needed, whether or not it should account for inputFile.getByteOrder(), and why setting this in new OtherWordAttributeMultipleFilesOnDisk later doesn't make a difference :(
								if (bitsPerSample == 16) {
									ByteArray.swapEndianness(sourceValues,sourceValues.length,2);
								}
								
								System.arraycopy(sourceValues,0,destinationValues,offsetIntoDestinationValues,(int)sourceValues.length);
								offsetIntoDestinationValues += sourceValues.length;
							}
							
							if (alwaysMakeColorByPixelPlanarConfig && planarConfig == 2) {
								destinationValues = convertPlanarConfigurationToColorByPixel(destinationValues,tileWidth,tileLength,samplesPerPixel);
							}
							
							File tmpFile = File.createTempFile("TIFFToDicom",".raw");
							files[destinationTileNumber] = tmpFile;
							tmpFile.deleteOnExit();
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Tile {} created uncompressed temporary file {}",destinationTileNumber,tmpFile.toString());
							if (filesToDeleteAfterWritingDicomFile == null) {
								filesToDeleteAfterWritingDicomFile = new ArrayList<File>();
							}
							else {
								filesToDeleteAfterWritingDicomFile.add(tmpFile);
							}
						
							BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(tmpFile));
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Tile {} writing {} bytes to temporary file {}",destinationTileNumber,destinationValues.length,tmpFile.toString());
							o.write(destinationValues);
							o.flush();
							o.close();
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Tile {} after write, temporary file length = {}",destinationTileNumber,tmpFile.length());
						}
					}
					else {
						files = new File[numberOfDestinationTiles];
						for (int tileNumber=0; tileNumber<numberOfDestinationTiles; ++tileNumber) {
							long pixelOffset = tileOffsets[tileNumber];
							long pixelByteCount = tileByteCounts[tileNumber];
							byte[] values = new byte[(int)pixelByteCount];
							inputFile.seek(pixelOffset);
							inputFile.read(values);
							
							values = lzwUncompress(values);
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): decompressed LZW tile length = {}",values.length);
							// apply horizontal differencing as per ij/io/ImageReader.java and TIFF6 page 64
							if (predictor == 2) {	// horizontalDifferencing
								reverseHorizontalDifferencing(values,bytesPerPixelForHorizontalDifferencing,tileWidth);
							}
							
							// not sure why this is needed, whether or not it should account for inputFile.getByteOrder(), and why setting this in new OtherWordAttributeMultipleFilesOnDisk later doesn't make a difference :(
							if (bitsPerSample == 16) {
								ByteArray.swapEndianness(values,values.length,2);
							}
							
							File tmpFile = File.createTempFile("TIFFToDicom",".raw");
							files[tileNumber] = tmpFile;
							tmpFile.deleteOnExit();
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Tile {} created uncompressed temporary file {}",tileNumber,tmpFile.toString());
							if (filesToDeleteAfterWritingDicomFile == null) {
								filesToDeleteAfterWritingDicomFile = new ArrayList<File>();
							}
							else {
								filesToDeleteAfterWritingDicomFile.add(tmpFile);
							}
							
							BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(tmpFile));
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Tile {} writing {} bytes to temporary file {}",tileNumber,values.length,tmpFile.toString());
							o.write(values);
							o.flush();
							o.close();
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Tile {} after write, temporary file length = {}",tileNumber,tmpFile.length());
						}
					}
				}
				else {
					slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): Using uncompressed tiles as source");
					if (mergeSamplesPerPixelTiles) {	// (001351)
						slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): Using uncompressed tiles as source merging separate tiles per channel into one tile");
						long destinationTileNumberOfBytes = tileWidth * tileLength * samplesPerPixel * bitsPerSample/8;
						byte[] destinationValues = new byte[(int)destinationTileNumberOfBytes];
						files = new File[numberOfDestinationTiles];
						// TIFF6 p 68: "For PlanarConfiguration = 2, the offsets for the first component plane are stored first, followed by all the offsets for the second component plane, and so on."
						for (int destinationTileNumber=0; destinationTileNumber<numberOfDestinationTiles; ++destinationTileNumber) {
							int offsetIntoDestinationValues = 0;
							for (int c=0; c<samplesPerPixel; ++c) {
								int sourceTileNumber = c * numberOfDestinationTiles + destinationTileNumber;	// numberOfDestinationTiles is also the number of tiles per component in the source tiles
								long pixelOffset = tileOffsets[sourceTileNumber];
								long pixelByteCount = tileByteCounts[sourceTileNumber];
								//if (slf4jlogger.isDebugEnabled() && pixelByteCount != expectedSingleChannelTileSizeBytes) slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): tileByteCount[{}] {} != tileWidth * tileLength {}",sourceTileNumber,pixelByteCount,expectedSingleChannelTileSizeBytes);
								byte[] sourceValues = new byte[(int)pixelByteCount];
								inputFile.seek(pixelOffset);
								inputFile.read(sourceValues);
								System.arraycopy(sourceValues,0,destinationValues,offsetIntoDestinationValues,(int)pixelByteCount);
								offsetIntoDestinationValues += pixelByteCount;
							}
							
							if (alwaysMakeColorByPixelPlanarConfig && planarConfig == 2) {
								destinationValues = convertPlanarConfigurationToColorByPixel(destinationValues,tileWidth,tileLength,samplesPerPixel);
							}
							
							File tmpFile = File.createTempFile("TIFFToDicom",".raw");
							files[destinationTileNumber] = tmpFile;
							tmpFile.deleteOnExit();
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Tile {} created uncompressed temporary file {}",destinationTileNumber,tmpFile.toString());
							if (filesToDeleteAfterWritingDicomFile == null) {
								filesToDeleteAfterWritingDicomFile = new ArrayList<File>();
							}
							else {
								filesToDeleteAfterWritingDicomFile.add(tmpFile);
							}
						
							BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(tmpFile));
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Tile {} writing {} bytes to temporary file {}",destinationTileNumber,destinationValues.length,tmpFile.toString());
							o.write(destinationValues);
							o.flush();
							o.close();
							if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Tile {} after write, temporary file length = {}",destinationTileNumber,tmpFile.length());
						}
					}
					else {
						slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): Using uncompressed tiles as source with source tiles same as destination tiles");
						useSourceFileForOutputPixelData = true;
						File file = new File(inputFile.getFileName());
						files = new File[numberOfSourceTiles];
						for (int i=0; i<numberOfSourceTiles; ++i) {
							{
								// check for truncated tiles ... (001397)
								long pixelByteCount = tileByteCounts[i];
								if (pixelByteCount != expectedUncompressedTileByteCount) {
									slf4jlogger.warn("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): Tile {} pixelByteCount {} != expected {}",i,pixelByteCount,expectedUncompressedTileByteCount);
								}
							}
							// if pixelByteCount == 0 then need to create empty uncompressed tile and write to temporary file (001397) :(
							files[i] = file;	// repeat the same file for every tile so that we can reuse existing MultipleFilesOnDisk classes
						}
					}
				}
				if (bitsPerSample == 8) {
					if (!isFloat) {
						slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): copying uncompressed or LZW decompressed 8 bit input to output");
						Attribute aPixelData = new OtherByteAttributeMultipleFilesOnDisk(TagFromName.PixelData,files,
							useSourceFileForOutputPixelData ? tileOffsets : null,
							useSourceFileForOutputPixelData ? tileByteCounts : null);
						long vl = aPixelData.getPaddedVL();
						if ((vl & 0xfffffffel) != vl) {
							throw new TIFFException("Value length of Pixel Data "+vl+" exceeds maximum Value Length supported by DICOM");
						}
						list.put(aPixelData);
					}
					else {
						throw new TIFFException("Unsupported 8 bit float pixel data");
					}
				}
				else if (bitsPerSample == 16) {
					if (!isFloat) {
						slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): copying uncompressed or LZW decompressed 16 bit input to output");
						Attribute aPixelData = new OtherWordAttributeMultipleFilesOnDisk(TagFromName.PixelData,files,
							useSourceFileForOutputPixelData ? tileOffsets : null,
							useSourceFileForOutputPixelData ? tileByteCounts : null,
							inputFile.getByteOrder() == ByteOrder.BIG_ENDIAN);
						long vl = aPixelData.getPaddedVL();
						if ((vl & 0xfffffffel) != vl) {
							throw new TIFFException("Value length of Pixel Data "+vl+" exceeds maximum Value Length supported by DICOM");
						}
						list.put(aPixelData);
					}
					else {
						throw new TIFFException("Unsupported 16 bit float pixel data");
					}
				}
				else if (bitsPerSample == 32) {
					if (!isFloat) {
						throw new TIFFException("Unsupported 32 bit unsigned or signed integer or undefined pixel data");
					}
					else {
						// (001320)
						slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): copying uncompressed or LZW decompressed 32 bit float input to output");
						Attribute aPixelData = new OtherFloatAttributeMultipleFilesOnDisk(TagFromName.FloatPixelData,files,
							useSourceFileForOutputPixelData ? tileOffsets : null,
							useSourceFileForOutputPixelData ? tileByteCounts : null,
							inputFile.getByteOrder() == ByteOrder.BIG_ENDIAN);
						long vl = aPixelData.getPaddedVL();
						if ((vl & 0xfffffffel) != vl) {
							throw new TIFFException("Value length of Pixel Data "+vl+" exceeds maximum Value Length supported by DICOM");
						}
						list.put(aPixelData);
					}
				}
				else {
					throw new TIFFException("Unsupported bitsPerSample = "+bitsPerSample);
				}
				// photometric unchanged
			}
			else {
				slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): output compression requested");
				if (bitsPerSample == 8) {
					slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): compressing uncompressed or LZW decompressed 8 bit input");

					File[] files = new File[numberOfDestinationTiles];
					for (int tileNumber=0; tileNumber<numberOfDestinationTiles; ++tileNumber) {
						long pixelOffset = tileOffsets[tileNumber];
						long pixelByteCount = tileByteCounts[tileNumber];
							{
								// check for truncated tiles ... (001397)
								if (pixelByteCount != expectedUncompressedTileByteCount && pixelByteCount != 0) {
									// will warn about 0 length when dealing with it later
									slf4jlogger.warn("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): Tile {} pixelByteCount {} != expected {}",tileNumber,pixelByteCount,expectedUncompressedTileByteCount);
								}
							}
						byte[] values;
						if (pixelByteCount > 0) {
							values = new byte[(int)pixelByteCount];
							inputFile.seek(pixelOffset);
							inputFile.read(values);
							
							if (compression == 5) {
								values = lzwUncompress(values);
								slf4jlogger.trace("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): decompressed LZW tile length in bytes = {}",values.length);
								slf4jlogger.trace("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): expected tileWidth*tileLength*bytesPerPixelForHorizontalDifferencing = {}",tileWidth*tileLength*bytesPerPixelForHorizontalDifferencing);
								// apply horizontal differencing as per ij/io/ImageReader.java and TIFF6 page 64
								if (predictor == 2) {	// horizontalDifferencing
									reverseHorizontalDifferencing(values,bytesPerPixelForHorizontalDifferencing,tileWidth);
								}
							}
						}
						else {
							// pixelByteCount == 0 so need to create empty uncompressed tile (001397)
							slf4jlogger.warn("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): Tile {} pixelByteCount {} so creating empty tile",tileNumber,pixelByteCount);
							values = new byte[(int)expectedUncompressedTileByteCount];
						}
						
						BufferedImage img = null;
						if (samplesPerPixel == 1) {
							img = SourceImage.createByteGrayscaleImage((int)tileWidth,(int)tileLength,values,0/*offset*/);
						}
						else if (samplesPerPixel == 3) {
							if (planarConfig == 1) {
								img = SourceImage.createPixelInterleavedByteThreeComponentColorImage(
									(int)tileWidth,(int)tileLength,values,0/*offset*/,
									ColorSpace.getInstance(ColorSpace.CS_sRGB),	// should check for presence of TIFF ICC profile ? :(
									false/*isChrominanceHorizontallyDownsampledBy2*/);
							}
							else {
								throw new TIFFException("Unsupported planarConfig = "+planarConfig+" for re-compression");
							}
						}
						else {
							throw new TIFFException("Unsupported samplesPerPixel = "+samplesPerPixel+" for re-compression");
						}
						
						// recompressLossy not yet implemented ... default for JPEG is best quality, J2K is lossless :(
						// will always transform color space by default
						File tmpFile = CompressedFrameEncoder.getCompressedFrameAsFile(new AttributeList(),img,recompressAsFormat,File.createTempFile("TIFFToDicom","."+recompressAsFormat));
						files[tileNumber] = tmpFile;
						tmpFile.deleteOnExit();
						if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Tile {} created compressed temporary file {}",tileNumber,tmpFile.toString());
						// if not grayscale, photometric changed, since CompressedFrameEncoder always transforms color space
						if (samplesPerPixel == 3) {
							outputPhotometric = 6;	// TIFF definition of YCbCr is generic, so use it to signal YBR_FULL_422 for JPEG and YBR_RCT or YBR_ICT for J2K
						}
					}
					Attribute aPixelData = new OtherByteAttributeMultipleCompressedFrames(TagFromName.PixelData,files);
					list.put(aPixelData);
					if (filesToDeleteAfterWritingDicomFile == null) {
						filesToDeleteAfterWritingDicomFile = new ArrayList<>(Arrays.asList(files));	// make a copy because Arrays.asList() is documented not to support any adding elements "https://stackoverflow.com/questions/5755477/java-list-add-unsupportedoperationexception"
					}
					else {
						Collections.addAll(filesToDeleteAfterWritingDicomFile,files);
					}
				}
				else if (bitsPerSample == 16 && samplesPerPixel == 1 && recompressAsFormat.equals("jpeg2000")) {	// (001308)
					slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): compressing uncompressed or LZW decompressed 16 bit grayscale input as jpeg2000");

					File[] files = new File[numberOfDestinationTiles];
					for (int tileNumber=0; tileNumber<numberOfDestinationTiles; ++tileNumber) {
						long pixelOffset = tileOffsets[tileNumber];
						long pixelByteCount = tileByteCounts[tileNumber];
						short[] values = null;

						if (compression == 0 || compression == 1) {	// absent or specified as uncompressed
							values = new short[(int)pixelByteCount/2];
							inputFile.seek(pixelOffset);
							inputFile.read(values);
						}
						else if (compression == 5) {
							byte[] lzwCompressedValues = new byte[(int)pixelByteCount];
							inputFile.seek(pixelOffset);
							inputFile.read(lzwCompressedValues);
							byte[] decompressedValues = lzwUncompress(lzwCompressedValues);
							slf4jlogger.trace("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): decompressed LZW tile length in bytes = {}",decompressedValues.length);
							slf4jlogger.trace("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): expected tileWidth*tileLength*bytesPerPixelForHorizontalDifferencing = {}",tileWidth*tileLength*bytesPerPixelForHorizontalDifferencing);
							// apply horizontal differencing as per ij/io/ImageReader.java and TIFF6 page 64
							if (predictor == 2) {	// horizontalDifferencing
								reverseHorizontalDifferencing(decompressedValues,bytesPerPixelForHorizontalDifferencing,tileWidth);
							}
							values = new short[(int)(tileWidth*tileLength)];
							ByteBuffer.wrap(decompressedValues).order(inputFile.getByteOrder()).asShortBuffer().get(values);
						}
						
						if (values.length != tileWidth*tileLength) {
							throw new TIFFException("Length of short pixel array "+values.length+" does not match tile size "+tileWidth*tileLength+" from width "+tileWidth+" and length "+tileLength);
						}

						BufferedImage img = SourceImage.createUnsignedShortGrayscaleImage((int)tileWidth,(int)tileLength,values,0/*offset*/);
						if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): BufferedImage for compression is \n{}\n",BufferedImageUtilities.describeImage(img));
						
						File tmpFile = CompressedFrameEncoder.getCompressedFrameAsFile(new AttributeList(),img,recompressAsFormat,File.createTempFile("TIFFToDicom","."+recompressAsFormat));
						files[tileNumber] = tmpFile;
						tmpFile.deleteOnExit();
						if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Tile {} created compressed temporary file {}",tileNumber,tmpFile.toString());
						// grayscale so no concerns about changing photometric due to CompressedFrameEncoder transforming color space
					}
					Attribute aPixelData = new OtherByteAttributeMultipleCompressedFrames(TagFromName.PixelData,files);
					list.put(aPixelData);
					if (filesToDeleteAfterWritingDicomFile == null) {
						filesToDeleteAfterWritingDicomFile = new ArrayList<>(Arrays.asList(files));	// make a copy because Arrays.asList() is documented not to support any adding elements "https://stackoverflow.com/questions/5755477/java-list-add-unsupportedoperationexception"
					}
					else {
						Collections.addAll(filesToDeleteAfterWritingDicomFile,files);
					}
				}
				else {
					throw new TIFFException("Unsupported bitsPerSample = "+bitsPerSample+" and samplesPerPixel = "+samplesPerPixel+" for recompression as "+recompressAsFormat);
				}
				//throw new TIFFException("Compression as "+(recompressLossy ? "lossy" : "lossless")+" "+recompressAsFormat+" not supported");
			}
		}
		else if (compression == 7 && recompressAsFormat.equals("jpeg")				// "new" JPEG per TTN2 as used by Aperio in SVS
			  || (compression == 33003 || compression == 33005) && recompressAsFormat.equals("jpeg2000")) {	// Aperio J2K YCbCr or RGB
			slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): copying compressed bit stream from input to output without recompressing it");
			// because we need to edit the stream to insert the jpegTables, need to write lots of temporary files to feed to OtherByteAttributeMultipleCompressedFrames file-based constructor
			File[] files = new File[numberOfDestinationTiles];
			for (int tileNumber=0; tileNumber<numberOfDestinationTiles; ++tileNumber) {
				long pixelOffset = tileOffsets[tileNumber];
				slf4jlogger.trace("For frame {}, pixelOffset = {}",tileNumber,pixelOffset);
				long pixelByteCount = tileByteCounts[tileNumber];
				if (pixelByteCount > Integer.MAX_VALUE) {
					throw new TIFFException("For frame "+tileNumber+", compressed pixelByteCount to be read "+pixelByteCount+" exceeds maximum Java array size "+Integer.MAX_VALUE+" and fragmentation not yet supported");
				}
				byte[] values = readJPEGFamilyCompressedPixelValuesAndFixAsNecessary(inputFile,tileNumber,pixelOffset,pixelByteCount,jpegTables,tileWidth,tileLength,compression,photometric,samplesPerPixel);
				if (values.length > 0xfffffffel) {
					throw new TIFFException("For frame "+tileNumber+", compressed pixelByteCount to be written "+values.length+" exceeds maximum single fragment size 0xfffffffe and fragmentation not yet supported");
				}
				File tmpFile = File.createTempFile("TIFFToDicom",".jpeg");
				files[tileNumber] = tmpFile;
				tmpFile.deleteOnExit();
				BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(tmpFile));
				o.write(values);
				o.flush();
				o.close();
				if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Tile {} wrote {} bytes to {}",tileNumber,values.length,tmpFile.toString());
				if (compression == 33003
				 /*|| compression == 33005*/) {	// do NOT change since no MCT (value is 0 in SGcod) (001263)
					outputPhotometric = 6;	// TIFF definition of YCbCr is generic, so use it to signal YBR_RCT or YBR_ICT for J2K
				}
				//else photometric unchanged
			}
			Attribute aPixelData = new OtherByteAttributeMultipleCompressedFrames(TagFromName.PixelData,files);
			list.put(aPixelData);
			if (filesToDeleteAfterWritingDicomFile == null) {
				filesToDeleteAfterWritingDicomFile = new ArrayList<>(Arrays.asList(files));	// make a copy because Arrays.asList() is documented not to support any adding elements "https://stackoverflow.com/questions/5755477/java-list-add-unsupportedoperationexception"
			}
			else {
				Collections.addAll(filesToDeleteAfterWritingDicomFile,files);
			}
		}
		else if ((compression == 7 || compression == 33003 || compression == 33005)		// "new" JPEG per TTN2 as used by Aperio in SVS, Aperio J2K YCbCr or RGB
			  && (recompressAsFormat.equals("jpeg") || recompressAsFormat.equals("jpeg2000"))) {
			// decompress and recompress each frame
			{
				if (bitsPerSample == 8) {
					slf4jlogger.debug("generateDICOMPixelDataMultiFrameImageFromTIFFFile(): decompressing each 8 bit input frame and recompressing it");
					String transferSyntax = compression == 7 ? TransferSyntax.JPEGBaseline : TransferSyntax.JPEG2000;	// take care to keep this in sync with enclosing test of supported schemes
					CompressedFrameDecoder decoder = new CompressedFrameDecoder(
						transferSyntax,
						1/*bytesPerSample*/,
						(int)tileWidth,(int)tileLength,
						3/*samples*/,		// hmmm ..../ :(
						ColorSpace.getInstance(ColorSpace.CS_sRGB),
						photometric == 6);	// should check for presence of TIFF ICC profile ? :(

					File[] files = new File[numberOfDestinationTiles];
					for (int tileNumber=0; tileNumber<numberOfDestinationTiles; ++tileNumber) {
						long pixelOffset = tileOffsets[tileNumber];
						long pixelByteCount = tileByteCounts[tileNumber];
						if (pixelByteCount > Integer.MAX_VALUE) {
							throw new TIFFException("For frame "+tileNumber+", compressed pixelByteCount to be read "+pixelByteCount+" exceeds maximum Java array size "+Integer.MAX_VALUE+" and fragmentation not yet supported");
						}
						
						byte[] values = readJPEGFamilyCompressedPixelValuesAndFixAsNecessary(inputFile,tileNumber,pixelOffset,pixelByteCount,jpegTables,tileWidth,tileLength,compression,photometric,samplesPerPixel);
						
						BufferedImage img = decoder.getDecompressedFrameAsBufferedImage(values);
						
						// recompressLossy not yet implemented ... default for JPEG is best quality, J2K is lossless :(
						// will always transform color space by default
						File tmpFile = CompressedFrameEncoder.getCompressedFrameAsFile(new AttributeList(),img,recompressAsFormat,File.createTempFile("TIFFToDicom","."+recompressAsFormat));
						files[tileNumber] = tmpFile;
						tmpFile.deleteOnExit();
						if (slf4jlogger.isTraceEnabled()) slf4jlogger.trace("Tile {} created compressed temporary file {}",tileNumber,tmpFile.toString());
						// photometric changed, since CompressedFrameEncoder always transforms color space
						outputPhotometric = 6;	// TIFF definition of YCbCr is generic, so use it to signal YBR_FULL_422 for JPEG and YBR_RCT or YBR_ICT for J2K
					}
					Attribute aPixelData = new OtherByteAttributeMultipleCompressedFrames(TagFromName.PixelData,files);
					list.put(aPixelData);
					if (filesToDeleteAfterWritingDicomFile == null) {
						filesToDeleteAfterWritingDicomFile = new ArrayList<>(Arrays.asList(files));	// make a copy because Arrays.asList() is documented not to support any adding elements "https://stackoverflow.com/questions/5755477/java-list-add-unsupportedoperationexception"
					}
					else {
						Collections.addAll(filesToDeleteAfterWritingDicomFile,files);
					}
				}
				else {
					throw new TIFFException("Unsupported bitsPerSample = "+bitsPerSample+" for compression");
				}
				//throw new TIFFException("Recompression as "+(recompressLossy ? "lossy" : "lossless")+" "+recompressAsFormat+" not supported");
			}
		}
		else {
			throw new TIFFException("Unsupported compression = "+compression+" or unsupported transformation to "+recompressAsFormat);
		}

		return outputPhotometric;
	}
	
	/**
	 * Utility method for decoding an LZW-compressed image strip.
	 * Derived from io/ImageReader.java
	 * Adapted from the TIFF 6.0 Specification:
	 * http://partners.adobe.com/asn/developer/pdfs/tn/TIFF6.pdf (page 61)
	 * @author Curtis Rueden (ctrueden at wisc.edu)
	 */
	private byte[] lzwUncompress(byte[] input) {
		if (input == null || input.length == 0)
			return input;
		byte[][] symbolTable = new byte[4096][1];
		int bitsToRead = 9;
		int nextSymbol = 258;
		int code;
		int oldCode = -1;
		ByteVector out = new ByteVector(8192);
		BitBuffer bb = new BitBuffer(input);
		byte[] byteBuffer1 = new byte[16];
		byte[] byteBuffer2 = new byte[16];
		
		while (true) {
			code = bb.getBits(bitsToRead);
			if (code == 257/*EOI_CODE*/ || code == -1)
				break;
			if (code == 256/*CLEAR_CODE*/) {
				// initialize symbol table
				for (int i = 0; i < 256; i++)
					symbolTable[i][0] = (byte)i;
				nextSymbol = 258;
				bitsToRead = 9;
				code = bb.getBits(bitsToRead);
				if (code == 257/*EOI_CODE*/ || code == -1)
					break;
				out.add(symbolTable[code]);
				oldCode = code;
			} else {
				if (code < nextSymbol) {
					// code is in table
					out.add(symbolTable[code]);
					// add string to table
					ByteVector symbol = new ByteVector(byteBuffer1);
					symbol.add(symbolTable[oldCode]);
					symbol.add(symbolTable[code][0]);
					symbolTable[nextSymbol] = symbol.toByteArray(); //**
					oldCode = code;
					nextSymbol++;
				} else {
					// out of table
					ByteVector symbol = new ByteVector(byteBuffer2);
					symbol.add(symbolTable[oldCode]);
					symbol.add(symbolTable[oldCode][0]);
					byte[] outString = symbol.toByteArray();
					out.add(outString);
					symbolTable[nextSymbol] = outString; //**
					oldCode = code;
					nextSymbol++;
				}
				if (nextSymbol == 511) { bitsToRead = 10; }
				if (nextSymbol == 1023) { bitsToRead = 11; }
				if (nextSymbol == 2047) { bitsToRead = 12; }
			}
		}
		return out.toByteArray();
	}

	/** A growable array of bytes. */
	private class ByteVector {
		private byte[] data;
		private int size;
		
		public ByteVector() {
			data = new byte[10];
			size = 0;
		}
		
		public ByteVector(int initialSize) {
			data = new byte[initialSize];
			size = 0;
		}
		
		public ByteVector(byte[] byteBuffer) {
			data = byteBuffer;
			size = 0;
		}
		
		public void add(byte x) {
			if (size>=data.length) {
				doubleCapacity();
				add(x);
			} else
				data[size++] = x;
		}
		
		public int size() {
			return size;
		}
		
		public void add(byte[] array) {
			int length = array.length;
			while (data.length-size<length)
				doubleCapacity();
			System.arraycopy(array, 0, data, size, length);
			size += length;
		}
		
		void doubleCapacity() {
			//IJ.log("double: "+data.length*2);
			byte[] tmp = new byte[data.length*2 + 1];
			System.arraycopy(data, 0, tmp, 0, data.length);
			data = tmp;
		}
		
		public void clear() {
			size = 0;
		}
		
		public byte[] toByteArray() {
			byte[] bytes = new byte[size];
			System.arraycopy(data, 0, bytes, 0, size);
			return bytes;
		}
	}
	
	/**
	 * Copied from ij/io/BitBuffer.java
	 * A class for reading arbitrary numbers of bits from a byte array.
	 * @author Eric Kjellman egkjellman at wisc.edu
	 */
	public class BitBuffer {
		
		private int currentByte;
		private int currentBit;
		private byte[] byteBuffer;
		private int eofByte;
		private int[] backMask;
		private int[] frontMask;
		private boolean eofFlag;
		
		public BitBuffer(byte[] byteBuffer) {
			this.byteBuffer = byteBuffer;
			currentByte = 0;
			currentBit = 0;
			eofByte = byteBuffer.length;
			backMask = new int[] {0x0000, 0x0001, 0x0003, 0x0007,
				0x000F, 0x001F, 0x003F, 0x007F};
			frontMask = new int[] {0x0000, 0x0080, 0x00C0, 0x00E0,
				0x00F0, 0x00F8, 0x00FC, 0x00FE};
		}
		
		public int getBits(int bitsToRead) {
			if (bitsToRead == 0)
				return 0;
			if (eofFlag)
				return -1; // Already at end of file
			int toStore = 0;
			while(bitsToRead != 0  && !eofFlag) {
				if (bitsToRead >= 8 - currentBit) {
					if (currentBit == 0) { // special
						toStore = toStore << 8;
						int cb = ((int) byteBuffer[currentByte]);
						toStore += (cb<0 ? (int) 256 + cb : (int) cb);
						bitsToRead -= 8;
						currentByte++;
					} else {
						toStore = toStore << (8 - currentBit);
						toStore += ((int) byteBuffer[currentByte]) & backMask[8 - currentBit];
						bitsToRead -= (8 - currentBit);
						currentBit = 0;
						currentByte++;
					}
				} else {
					toStore = toStore << bitsToRead;
					int cb = ((int) byteBuffer[currentByte]);
					cb = (cb<0 ? (int) 256 + cb : (int) cb);
					toStore += ((cb) & (0x00FF - frontMask[currentBit])) >> (8 - (currentBit + bitsToRead));
					currentBit += bitsToRead;
					bitsToRead = 0;
				}
				if (currentByte == eofByte) {
					eofFlag = true;
					return toStore;
				}
			}
			return toStore;
		}
	}

	private void reverseHorizontalDifferencing(byte[] stripOrTile,int bytesPerPixel,long width) {
		slf4jlogger.trace("reverseHorizontalDifferencing(): reversing horizontal differencing");
		// bytesPerPixel will be 3 for planarConfig = 1 (color-by-pixel), or 1 if planarConfig = 2 (color-by-plane), assuming BitsPerSample 8 for all channels
		for (int b=0; b<stripOrTile.length; b++) {
			if (b / bytesPerPixel % width == 0) {	// e.g., 0,1,2 if 3
				slf4jlogger.trace("reverseHorizontalDifferencing(): start of row byte index = {}",b);
			}
			else {
				stripOrTile[b] += stripOrTile[b - bytesPerPixel];
			}
		}
	}
	// end code derived from ImageJ

	private long generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(TIFFFile inputFile,AttributeList list,boolean alwaysMakeColorByPixelPlanarConfig,
				long imageWidth,long imageLength,
				long[] pixelOffset,long[] pixelByteCount,long pixelWidth,long rowsPerStrip,
				long bitsPerSample,long sampleFormat,long compression,long photometric,long samplesPerPixel,long planarConfig,long predictor,
				byte[] jpegTables,byte[] iccProfile,String recompressAsFormat,boolean recompressLossy) throws IOException, DicomException, TIFFException {
		
		long outputPhotometric = photometric;
		
		if (list == null) {
			list = new AttributeList();
		}
		
		slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): compression = {}",compression);
		slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): predictor = {}",predictor);
		slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): planarConfig = {}",planarConfig);
		slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): samplesPerPixel = {}",samplesPerPixel);
		slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): bitsPerSample = {}",bitsPerSample);
		int bytesPerPixelForHorizontalDifferencing = (int)(planarConfig == 1 ? samplesPerPixel * bitsPerSample/8 : bitsPerSample/8);
		slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): bytesPerPixelForHorizontalDifferencing = {}",bytesPerPixelForHorizontalDifferencing);
		slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): alwaysMakeColorByPixelPlanarConfig = {}",alwaysMakeColorByPixelPlanarConfig);


		if (compression == 0 || compression == 1		// absent or specified as uncompressed
		 || compression == 5) {							// LZW
			if (recompressAsFormat == null || recompressAsFormat.length() == 0) {
				if (bitsPerSample == 8) {
					slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): merging uncompressed or LZW compressed 8 bit strips");
					long totalLength = imageWidth * imageLength * samplesPerPixel;
					if (totalLength %2 != 0) ++totalLength;
					//long totalLength = 0;
					//for (int i=0; i<pixelByteCount.length; ++i) {
					//	totalLength += pixelByteCount[i];
					//}
					//if (totalLength%2 == 1) ++totalLength;
					slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): totalLength = {}",totalLength);
					if (totalLength > Integer.MAX_VALUE) {
						throw new TIFFException("Uncompressed image too large to allocate = "+totalLength);
					}
					byte[] values = new byte[(int)totalLength];
					int offsetIntoValues = 0;
					for (int i=0; i<pixelOffset.length; ++i) {
						slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): doing strip = {} of {}",i,pixelOffset.length);
						slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): offsetIntoValue = {}",offsetIntoValues);
						long fileOffset = pixelOffset[i];
						slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): pixelOffset[{}] = {}",i,fileOffset);
						inputFile.seek(fileOffset);
						int bytesToRead = (int)pixelByteCount[i];
						slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): pixelByteCount[{}] = {}",i,bytesToRead);
						if (compression == 5) {
							byte[] lzwvalues = new byte[bytesToRead];
							inputFile.read(lzwvalues,0,bytesToRead);
							byte[] decompressedStrip = lzwUncompress(lzwvalues);
							slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): decompressed LZW strip length = {}",decompressedStrip.length);
							// apply horizontal differencing as per ij/io/ImageReader.java and TIFF6 page 64
							if (predictor == 2) {	// horizontalDifferencing
								reverseHorizontalDifferencing(decompressedStrip,bytesPerPixelForHorizontalDifferencing,imageWidth);
							}
							// sometimes decompressed strip exceeds what is needed (? why) so do not exceed array bounds and only copy what is needed (001329)
							slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): values.length = {} offsetIntoValues+decompressedStrip.length = {}",values.length,offsetIntoValues+decompressedStrip.length);
							int spaceremaininginvalues = values.length - offsetIntoValues;
							slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): spaceremaininginvalues = {}",spaceremaininginvalues);
							int bytestocopy = 0;
							if (decompressedStrip.length > spaceremaininginvalues) {
								slf4jlogger.warn("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): decompressed LZW strip is too long for size of pixel matrix - have {} bytes, space remaining is only {} bytes for strip {} (from 0) of {}",decompressedStrip.length,spaceremaininginvalues,i,pixelOffset.length);
								bytestocopy = spaceremaininginvalues;
							}
							else {
								bytestocopy = decompressedStrip.length;
							}
							System.arraycopy(decompressedStrip,0,values,offsetIntoValues,bytestocopy);
							offsetIntoValues += bytestocopy;
						}
						else {
							inputFile.read(values,offsetIntoValues,bytesToRead);
							offsetIntoValues += bytesToRead;
						}
					}

					if (alwaysMakeColorByPixelPlanarConfig && planarConfig == 2) {
						values = convertPlanarConfigurationToColorByPixel(values,imageWidth,imageLength,samplesPerPixel);
					}

					Attribute aPixelData = new OtherByteAttribute(TagFromName.PixelData);
					aPixelData.setValues(values);
					list.put(aPixelData);
				}
				else if (bitsPerSample == 16) {		// (001343)
					long totalLength = imageWidth * imageLength * samplesPerPixel;
					slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): totalLength = {}",totalLength);
					if (totalLength > Integer.MAX_VALUE) {
						throw new TIFFException("Uncompressed image too large to allocate = "+totalLength+" 16-bit words");
					}
					short[] values = new short[(int)totalLength];
					if (compression == 5) {
						slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): merging LZW compressed 16 bit strips");
						long totalLengthInBytes = totalLength*2;
						if (totalLengthInBytes > Integer.MAX_VALUE) {
							throw new TIFFException("Uncompressed image too large to allocate = "+totalLengthInBytes+" bytes");
						}
						byte[] byteValues = new byte[(int)totalLengthInBytes];
						int offsetIntoValues = 0;
						for (int i=0; i<pixelOffset.length; ++i) {
							slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): doing strip = {} of {}",i,pixelOffset.length);
							slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): offsetIntoValue = {}",offsetIntoValues);
							long fileOffset = pixelOffset[i];
							slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): pixelOffset[{}] = {}",i,fileOffset);
							inputFile.seek(fileOffset);
							int bytesToRead = (int)pixelByteCount[i];
							slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): pixelByteCount[{}] = {}",i,bytesToRead);
							{
								byte[] lzwvalues = new byte[bytesToRead];
								inputFile.read(lzwvalues,0,bytesToRead);
								byte[] decompressedStrip = lzwUncompress(lzwvalues);
								slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): decompressed LZW strip length = {}",decompressedStrip.length);
								// apply horizontal differencing as per ij/io/ImageReader.java and TIFF6 page 64
								if (predictor == 2) {	// horizontalDifferencing
									reverseHorizontalDifferencing(decompressedStrip,bytesPerPixelForHorizontalDifferencing,imageWidth);
								}
								// sometimes decompressed strip exceeds what is needed (? why) so do not exceed array bounds and only copy what is needed (001329)
								slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): byteValues.length = {} offsetIntoValues+decompressedStrip.length = {}",byteValues.length,offsetIntoValues+decompressedStrip.length);
								int spaceremaininginvalues = byteValues.length - offsetIntoValues;
								slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): spaceremaininginvalues = {}",spaceremaininginvalues);
								int bytestocopy = 0;
								if (decompressedStrip.length > spaceremaininginvalues) {
									slf4jlogger.warn("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): decompressed LZW strip is too long for size of pixel matrix - have {} bytes, space remaining is only {} bytes for strip {} (from 0) of {}",decompressedStrip.length,spaceremaininginvalues,i,pixelOffset.length);
									bytestocopy = spaceremaininginvalues;
								}
								else {
									bytestocopy = decompressedStrip.length;
								}
								System.arraycopy(decompressedStrip,0,byteValues,offsetIntoValues,bytestocopy);
								offsetIntoValues += bytestocopy;
							}
						}
						ByteBuffer.wrap(byteValues).order(inputFile.getByteOrder()).asShortBuffer().get(values);
					}
					else {
						slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): merging uncompressed 16 bit strips");
						int offsetIntoValues = 0;
						for (int i=0; i<pixelOffset.length; ++i) {
							long fileOffset = pixelOffset[i];
							slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): pixelOffset[{}] = {}",i,fileOffset);
							inputFile.seek(fileOffset);
							int shortsToRead = (int)(pixelByteCount[i]/2);
							slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): pixelByteCount[{}] = {}, shortsToRead = {}",i,pixelByteCount[i],shortsToRead);
							inputFile.read(values,offsetIntoValues,shortsToRead);
							offsetIntoValues += shortsToRead;
						}
					}

					if (alwaysMakeColorByPixelPlanarConfig && planarConfig == 2) {
						values = convertPlanarConfigurationToColorByPixel(values,imageWidth,imageLength,samplesPerPixel);
					}

					Attribute aPixelData = new OtherWordAttribute(TagFromName.PixelData);
					aPixelData.setValues(values);
					list.put(aPixelData);
				}
				else {
					throw new TIFFException("Unsupported bitsPerSample = "+bitsPerSample);
				}
			}
			else {
				throw new TIFFException("Compression as "+(recompressLossy ? "lossy" : "lossless")+" "+recompressAsFormat+" not supported");
			}
		}
		else if (compression == 7) {				// "new" JPEG per TTN2 as used by Aperio in SVS
			if (recompressAsFormat == null || recompressAsFormat.length() == 0) {
				// decompress each strip
				if (bitsPerSample == 8) {
					slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): decompressing each 8 bit input strip");
					CompressedFrameDecoder decoder = new CompressedFrameDecoder(
						TransferSyntax.JPEGBaseline,
						1/*bytesPerSample*/,
						(int)pixelWidth,(int)rowsPerStrip,
						3/*samples*/,		// hmmm ..../ :(
						ColorSpace.getInstance(ColorSpace.CS_sRGB),
						photometric == 6);	// should check for presence of TIFF ICC profile ? :(
					
					long totalLength = imageWidth * imageLength * samplesPerPixel;
					if (totalLength %2 != 0) ++totalLength;
					slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): totalLength = {}",totalLength);
					if (totalLength > Integer.MAX_VALUE) {
						throw new TIFFException("Uncompressed image too large to allocate = "+totalLength);
					}
					byte[] values = new byte[(int)totalLength];
					int offsetIntoValues = 0;
					for (int i=0; i<pixelOffset.length; ++i) {
						byte[] compressedValues = readJPEGFamilyCompressedPixelValuesAndFixAsNecessary(inputFile,0/*tileNumber*/,pixelOffset[i],pixelByteCount[i],jpegTables,pixelWidth,rowsPerStrip,compression,photometric,samplesPerPixel);
						BufferedImage img = decoder.getDecompressedFrameAsBufferedImage(compressedValues);
						int decompressedWidth = img.getWidth();
						slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): strip {} decompressedWidth = {}",i,decompressedWidth);
						int decompressedHeight = img.getHeight();
						slf4jlogger.trace("generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(): strip {} decompressedHeight = {}",i,decompressedHeight);
						Raster raster = img.getData();
						if (raster.getTransferType() == DataBuffer.TYPE_BYTE) {
							byte[] decompressedStrip = (byte[])(raster.getDataElements(0,0,decompressedWidth,decompressedHeight,null));
							System.arraycopy(decompressedStrip,0,values,offsetIntoValues,decompressedStrip.length);
							offsetIntoValues += decompressedStrip.length;
						}
					}
					Attribute aPixelData = new OtherByteAttribute(TagFromName.PixelData);
					aPixelData.setValues(values);
					list.put(aPixelData);
				}
				else {
					throw new TIFFException("Unsupported bitsPerSample = "+bitsPerSample);
				}
			}
			else {
				throw new TIFFException("Compression as "+(recompressLossy ? "lossy" : "lossless")+" "+recompressAsFormat+" not supported");
			}
		}
		else {
			throw new TIFFException("Unsupported compression = "+compression);
		}

		return outputPhotometric;
	}

	private long generateDICOMPixelDataSingleFrameImageFromTIFFFile(TIFFFile inputFile,AttributeList list,
				long pixelOffset,long pixelByteCount,long pixelWidth,long pixelLength,
				long bitsPerSample,long sampleFormat,long compression,long photometric,long samplesPerPixel,byte[] jpegTables,byte[] iccProfile,String recompressAsFormat,boolean recompressLossy) throws IOException, DicomException, TIFFException {
		
		slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFile(): sampleFormat = {}",sampleFormat);
		boolean isFloat = sampleFormat == 3;	// (001320)

		long outputPhotometric = photometric;
		
		if (list == null) {
			list = new AttributeList();
		}
		
		inputFile.seek(pixelOffset);
		if (compression == 0 || compression == 1) {		// absent or specified as uncompressed
			if (recompressAsFormat == null || recompressAsFormat.length() == 0) {
				if (bitsPerSample == 8) {
					if (!isFloat) {
						slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFile(): copying uncompressed 8 bit input to output");
						byte[] values = new byte[(int)pixelByteCount];
						inputFile.read(values);
						Attribute aPixelData = new OtherByteAttribute(TagFromName.PixelData);
						aPixelData.setValues(values);
						list.put(aPixelData);
					}
					else {
						throw new TIFFException("Unsupported 8 bit float pixel data");
					}
				}
				else if (bitsPerSample == 16) {
					if (!isFloat) {
						slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFile(): copying uncompressed 16 bit input to output");
						short[] values = new short[(int)(pixelByteCount/2)];
						inputFile.read(values);
						Attribute aPixelData = new OtherWordAttribute(TagFromName.PixelData);
						aPixelData.setValues(values);
						list.put(aPixelData);
					}
					else {
						throw new TIFFException("Unsupported 16 bit float pixel data");
					}
				}
				else if (bitsPerSample == 32) {
					if (!isFloat) {
						throw new TIFFException("Unsupported 32 bit unsigned or signed integer or undefined pixel data");
					}
					else {
						// (001320)
						slf4jlogger.debug("generateDICOMPixelDataSingleFrameImageFromTIFFFile(): copying uncompressed 32 bit float input to output");
						float[] values = new float[(int)(pixelByteCount/2)];
						inputFile.read(values);
						Attribute aPixelData = new OtherFloatAttribute(TagFromName.FloatPixelData);
						aPixelData.setValues(values);
						list.put(aPixelData);
					}
				}
				else {
					throw new TIFFException("Unsupported bitsPerSample = "+bitsPerSample);
				}
			}
			else {
				throw new TIFFException("Compression as "+(recompressLossy ? "lossy" : "lossless")+" "+recompressAsFormat+" not supported");
			}
		}
		else if (compression == 7				// "new" JPEG per TTN2 as used by Aperio in SVS
			  || compression == 33003			// Aperio J2K YCbCr
			  || compression == 33005) {		// Aperio J2K RGB
			byte[] values = readJPEGFamilyCompressedPixelValuesAndFixAsNecessary(inputFile,0/*tileNumber*/,pixelOffset,pixelByteCount,jpegTables,pixelWidth,pixelLength,compression,photometric,samplesPerPixel);
			byte[][] frames = new byte[1][];
			frames[0] = values;
			Attribute aPixelData = new OtherByteAttributeMultipleCompressedFrames(TagFromName.PixelData,frames);
			list.put(aPixelData);
			if (compression == 33003
			 /*|| compression == 33005*/) {	// do NOT change since no MCT (value is 0 in SGcod) (001263)
				outputPhotometric = 6;	// TIFF definition of YCbCr is generic, so use it to signal YBR_RCT or YBR_ICT for J2K
			}
			//else photometric unchanged
		}
		else {
			throw new TIFFException("Unsupported compression = "+compression);
		}

		return outputPhotometric;
	}
	
	private static AttributeList generateDICOMPixelDataModuleAttributes(AttributeList list,
			int numberOfFrames,long pixelWidth,long pixelLength,
			long bitsPerSample,long compression,long photometric,long samplesPerPixel,long planarConfig,long sampleFormat,String recompressAsFormat,boolean recompressLossy,String sopClass) throws IOException, DicomException, TIFFException {
		
		if (list == null) {
			list = new AttributeList();
		}
		
		String photometricInterpretation = "";
		switch ((int)photometric) {
			case 0:	photometricInterpretation = "MONOCHROME1"; break;
			case 1:	photometricInterpretation = "MONOCHROME2"; break;
			case 2:	photometricInterpretation = "RGB"; break;
			case 3:	photometricInterpretation = "PALETTE COLOR"; break;
			case 4:	photometricInterpretation = "TRANSPARENCY"; break;		// not standard DICOM
			case 5:	photometricInterpretation = "CMYK"; break;				// retired in DICOM
			case 6:	photometricInterpretation = (recompressAsFormat != null && recompressAsFormat.equals("jpeg2000")) ? (recompressLossy ? "YBR_ICT" : "YBR_RCT") : "YBR_FULL_422"; break;
			case 8:	photometricInterpretation = "CIELAB"; break;			// not standard DICOM
		}
		{ Attribute a = new CodeStringAttribute(TagFromName.PhotometricInterpretation); a.addValue(photometricInterpretation); list.put(a); }

		{ Attribute a = new UnsignedShortAttribute(TagFromName.BitsAllocated); a.addValue((int)bitsPerSample); list.put(a); }
		{ Attribute a = new UnsignedShortAttribute(TagFromName.Rows); a.addValue((int)pixelLength); list.put(a); }
		{ Attribute a = new UnsignedShortAttribute(TagFromName.Columns); a.addValue((int)pixelWidth); list.put(a); }
			
		boolean signed = false;
		boolean isFloat = false;
		if (sampleFormat == 2) {
			signed = true;
		}
		else if (sampleFormat == 3) {	// (001320)
			isFloat = true;
		}
		if (!isFloat) {	// (001320)
			{ Attribute a = new UnsignedShortAttribute(TagFromName.BitsStored); a.addValue((int)bitsPerSample); list.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.HighBit); a.addValue((int)bitsPerSample-1); list.put(a); }
			{ Attribute a = new UnsignedShortAttribute(TagFromName.PixelRepresentation); a.addValue(signed ? 1 : 0); list.put(a); }
		}
		
		list.remove(TagFromName.NumberOfFrames);
		if (SOPClass.isMultiframeImageStorage(sopClass)) {
			Attribute a = new IntegerStringAttribute(TagFromName.NumberOfFrames); a.addValue(numberOfFrames); list.put(a);
		}
			
		{ Attribute a = new UnsignedShortAttribute(TagFromName.SamplesPerPixel); a.addValue((int)samplesPerPixel); list.put(a); }
						
		list.remove(TagFromName.PlanarConfiguration);
		if (samplesPerPixel > 1) {
				Attribute a = new UnsignedShortAttribute(TagFromName.PlanarConfiguration); a.addValue((int)planarConfig-1); list.put(a);	// TIFF is 1 or 2 but sometimes absent (0), DICOM is 0 or 1
		}

		return list;
	}

	// copied and derived from CommonConvertedAttributeGeneration.addParametricMapFrameTypeSharedFunctionalGroup() - should refactor :(
	private static AttributeList addWholeSlideMicroscopyImageFrameTypeSharedFunctionalGroup(AttributeList list,String imageFlavor,String imageDerivation) throws DicomException {
		// override default from CommonConvertedAttributeGeneration; same as FrameType; no way of determining this and most are VOLUME not LABEL or LOCALIZER :(
		Attribute aFrameType = new CodeStringAttribute(TagFromName.FrameType);
		aFrameType.addValue("DERIVED");
		aFrameType.addValue("PRIMARY");
		aFrameType.addValue(imageFlavor);
		aFrameType.addValue(imageDerivation);
		list = FunctionalGroupUtilities.generateFrameTypeSharedFunctionalGroup(list,DicomDictionary.StandardDictionary.getTagFromName("WholeSlideMicroscopyImageFrameTypeSequence"),aFrameType);
		return list;
	}
	
	private byte[] addICCProfileToOpticalPathSequence(AttributeList list,byte[] iccProfile) throws DicomException {
		AttributeList opticalPathSequenceItemList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(list,DicomDictionary.StandardDictionary.getTagFromName("OpticalPathSequence"));
		if (opticalPathSequenceItemList != null) {
			if (iccProfile == null || iccProfile.length == 0) {
				InputStream iccProfileStream = getClass().getResourceAsStream("/com/pixelmed/dicom/sRGBColorSpaceProfileInputDevice.icc");
				try {
					iccProfile = FileUtilities.readAllBytes(iccProfileStream);
					int iccProfileLength = iccProfile.length;
					if (iccProfileLength %2 != 0) {
						byte[] newICCProfile = new byte[iccProfileLength+1];
						System.arraycopy(iccProfile,0,newICCProfile,0,iccProfileLength);
						iccProfile = newICCProfile;
						iccProfileLength = iccProfile.length;
					}
				}
				catch (IOException e) {
					throw new DicomException("Failed to read ICC profile resource: "+e);
				}
				{ Attribute a = new CodeStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("ColorSpace")); a.addValue("SRGB"); opticalPathSequenceItemList.put(a); }
			}
			else {
				slf4jlogger.debug("Using ICC Profile from TIFF IFD");
				// do not add ColorSpace since we do not know what it is or if it is any recognized standard value
			}
			if (iccProfile != null && iccProfile.length > 0) {
				{ Attribute a = new OtherByteAttribute(TagFromName.ICCProfile); a.setValues(iccProfile); opticalPathSequenceItemList.put(a); }
				slf4jlogger.debug("addICCProfileToOpticalPathSequence(): Created ICC Profile attribute of length {}",iccProfile.length);
			}
		}
		return iccProfile;
	}

	// (001270)
	private void addObjectiveLensPowerToOpticalPathSequence(AttributeList list,double objectiveLensPower) throws DicomException {
		if (objectiveLensPower != 0) {
			AttributeList opticalPathSequenceItemList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(list,DicomDictionary.StandardDictionary.getTagFromName("OpticalPathSequence"));
			if (opticalPathSequenceItemList != null) {
				double existingObjectiveLensPower = Attribute.getSingleDoubleValueOrDefault(list,DicomDictionary.StandardDictionary.getTagFromName("ObjectiveLensPower"),0);
				if (existingObjectiveLensPower == 0) {
					{ Attribute a = new DecimalStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("ObjectiveLensPower")); a.addValue(objectiveLensPower); opticalPathSequenceItemList.put(a); }
					slf4jlogger.debug("addObjectiveLensPowerToOpticalPathSequence(): added ObjectiveLensPower {}",objectiveLensPower);
				}
				else {
					slf4jlogger.debug("addObjectiveLensPowerToOpticalPathSequence(): not overriding non-zero ObjectiveLensPower {} with replacement {}",existingObjectiveLensPower,objectiveLensPower);
				}
			}
			else {
				slf4jlogger.debug("addObjectiveLensPowerToOpticalPathSequence(): no opticalPathSequenceItemList to add to - not trying to create it now (too late)");
			}
		}
		else {
			slf4jlogger.debug("addObjectiveLensPowerToOpticalPathSequence(): no ObjectiveLensPower value to use, so nothing to do");
		}
	}

	// (001319)
	private void addObjectiveLensNumericalApertureToOpticalPathSequence(AttributeList list,double objectiveLensNumericalAperture) throws DicomException {
		if (objectiveLensNumericalAperture != 0) {
			AttributeList opticalPathSequenceItemList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(list,DicomDictionary.StandardDictionary.getTagFromName("OpticalPathSequence"));
			if (opticalPathSequenceItemList != null) {
				double existingObjectiveLensNumericalAperture = Attribute.getSingleDoubleValueOrDefault(list,DicomDictionary.StandardDictionary.getTagFromName("ObjectiveLensNumericalAperture"),0);
				if (existingObjectiveLensNumericalAperture == 0) {
					{ Attribute a = new DecimalStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("ObjectiveLensNumericalAperture")); a.addValue(objectiveLensNumericalAperture); opticalPathSequenceItemList.put(a); }
					slf4jlogger.debug("addObjectiveLensNumericalApertureToOpticalPathSequence(): added ObjectiveLensNumericalAperture {}",objectiveLensNumericalAperture);
				}
				else {
					slf4jlogger.debug("addObjectiveLensNumericalApertureToOpticalPathSequence(): not overriding non-zero ObjectiveLensNumericalAperture {} with replacement {}",existingObjectiveLensNumericalAperture,objectiveLensNumericalAperture);
				}
			}
			else {
				slf4jlogger.debug("addObjectiveLensNumericalApertureToOpticalPathSequence(): no opticalPathSequenceItemList to add to - not trying to create it now (too late)");
			}
		}
		else {
			slf4jlogger.debug("addObjectiveLensNumericalApertureToOpticalPathSequence(): no ObjectiveLensNumericalAperture value to use, so nothing to do");
		}
	}

	// (001285)
	private void addOpticalPathIdentifierAndDescriptionToOpticalPathSequence(AttributeList list,String opticalPathIdentifier,String opticalPathDescription) throws DicomException {
		if (opticalPathIdentifier != null && opticalPathIdentifier.length() > 0
		 || opticalPathDescription != null && opticalPathDescription.length() > 0) {
			AttributeList opticalPathSequenceItemList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(list,DicomDictionary.StandardDictionary.getTagFromName("OpticalPathSequence"));
			if (opticalPathSequenceItemList != null) {
				if (opticalPathIdentifier != null && opticalPathIdentifier.length() > 0) {
					{ Attribute a = new ShortStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("OpticalPathIdentifier")); a.addValue(opticalPathIdentifier); opticalPathSequenceItemList.put(a); }
					slf4jlogger.debug("addOpticalPathIdentifierAndDescriptionToOpticalPathSequence(): added or replacing opticalPathIdentifier {}",opticalPathIdentifier);
				}
				if (opticalPathDescription != null && opticalPathDescription.length() > 0) {
					{ Attribute a = new ShortTextAttribute(DicomDictionary.StandardDictionary.getTagFromName("OpticalPathDescription")); a.addValue(opticalPathDescription); opticalPathSequenceItemList.put(a); }
					slf4jlogger.debug("addOpticalPathIdentifierAndDescriptionToOpticalPathSequence(): added or replacing opticalPathDescription {}",opticalPathDescription);
				}
			}
			else {
				slf4jlogger.debug("addOpticalPathIdentifierAndDescriptionToOpticalPathSequence(): no opticalPathSequenceItemList to add to - not trying to create it now (too late)");
			}
		}
		else {
			slf4jlogger.debug("addOpticalPathIdentifierAndDescriptionToOpticalPathSequence(): no OpticalPathIdentifier or OpticalPathDescription value to use, so nothing to do");
		}
	}
	
	private void addOpticalPathAttributesForChannel(AttributeList list,AttributeList opticalPathAttributesForChannel) {
		if (opticalPathAttributesForChannel != null) {
			AttributeList opticalPathSequenceItemList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(list,DicomDictionary.StandardDictionary.getTagFromName("OpticalPathSequence"));
			if (opticalPathSequenceItemList != null) {
				opticalPathSequenceItemList.putAll(opticalPathAttributesForChannel);
			}
			else {
				slf4jlogger.debug("addOpticalPathAttributesForChannel(): no opticalPathSequenceItemList to add to - not trying to create it now (too late)");
			}
		}
		else {
			slf4jlogger.debug("addOpticalPathAttributesForChannel(): no opticalPathAttributesForChannel, so nothing to do");
		}
	}

	

	private final long totalArrayValues(long[] values) {
		long total = 0;
		if (values != null) {
			for (long value : values) {
				total+=value;
			}
		}
		return total;
	}

	private AttributeList insertLossyImageCompressionHistory(AttributeList list,
			long compression,long outputCompression,
			boolean recompressLossy,		// (001304)
			String pastHistoryOfLossyCompression,	//	(001359)
			long originalCompressedByteCount,long imageWidth,long imageLength,long bitsPerSample,long samplesPerPixel
			) throws DicomException {
		
		if (list == null) {
			list = new AttributeList();
		}
		
		// inspired by AttributeList.insertLossyImageCompressionHistoryIfDecompressed() but can't reuse directly - should refactor :(

		{
			String lossyImageCompression="00";
			Set<String> lossyImageCompressionMethod = new HashSet<String>();
			boolean wasOriginallyCompressed = false;
			boolean wasRecompressedLossy = false;
			
			if (pastHistoryOfLossyCompression != null && pastHistoryOfLossyCompression.length() > 0) {
				lossyImageCompression="01";
				String scheme = pastHistoryOfLossyCompression.toUpperCase().trim();
				if (scheme.equals("JPEG")) {
					lossyImageCompressionMethod.add("ISO_10918_1");
				}
				else if (scheme.equals("JPEG-2000")) {
					lossyImageCompressionMethod.add("ISO_15444_1");
				}
			}

			if (compression == 7) {			// "new" JPEG per TTN2 as used by Aperio in SVS
				wasOriginallyCompressed = true;
				lossyImageCompression="01";
				lossyImageCompressionMethod.add("ISO_10918_1");
			}
			else if (compression == 33003	// Aperio J2K YCbCr
				  || compression == 33005	// Aperio J2K RGB
			) {
				// can only get here when converting TIFF SVS file, so can assume any J2K use is not caused by us abusing 33003 for reversible J2k
				wasOriginallyCompressed = true;
				lossyImageCompression="01";
				lossyImageCompressionMethod.add("ISO_15444_1");
			}
			
			if (outputCompression == 7) {		// "new" JPEG per TTN2 as used by Aperio in SVS
				lossyImageCompression="01";
				lossyImageCompressionMethod.add("ISO_10918_1");
				if (wasOriginallyCompressed && compression != outputCompression) {
					wasRecompressedLossy = true;
				}
			}
			else if (outputCompression == 33003	// Aperio J2K YCbCr
				  || outputCompression == 33005	// Aperio J2K RGB
			) {
				if (recompressLossy) {	// (001304)
					lossyImageCompression="01";
					lossyImageCompressionMethod.add("ISO_15444_1");
					if (wasOriginallyCompressed && compression != outputCompression) {
						wasRecompressedLossy = true;
					}
				}
				// else may be non-Leica J2K reversible that we are creating masquerading as 33003 since no other standard or proprietary TIFF value for J2K :( (001304)
			}
			
			{ Attribute a = new CodeStringAttribute(TagFromName.LossyImageCompression); a.addValue(lossyImageCompression); list.put(a); }
			
			if (!lossyImageCompressionMethod.isEmpty()) {
				Attribute a = new CodeStringAttribute(TagFromName.LossyImageCompressionMethod);
				for (String v : lossyImageCompressionMethod) {
					a.addValue(v);
				}
				list.put(a);
			}
			
			if (wasOriginallyCompressed) {
				// compute CR with precision of three decimal places
				long bytesPerSample = (bitsPerSample-1)/8+1;
				long decompressedByteCount = imageWidth*imageLength*samplesPerPixel*bytesPerSample;
				double compressionRatio = (long)(decompressedByteCount*1000/originalCompressedByteCount);
				compressionRatio = compressionRatio / 1000;
				slf4jlogger.debug("insertLossyImageCompressionHistory(): decompressedByteCount = {}",decompressedByteCount);
				slf4jlogger.debug("insertLossyImageCompressionHistory(): wasOriginallyCompressed with originalCompressedByteCount = {}",originalCompressedByteCount);
				slf4jlogger.debug("insertLossyImageCompressionHistory(): wasOriginallyCompressed with compressionRatio = {}",compressionRatio);
				Attribute a = new DecimalStringAttribute(TagFromName.LossyImageCompressionRatio);
				a.addValue(compressionRatio);
				list.put(a);
			}
			
			if (wasRecompressedLossy) {
				// should add new compression ratio value based on recompressedByteCount, which we haven't calulated yet :( (001305)
			}
		}

		return list;
	}
	
	private AttributeList generateDICOMWholeSlideMicroscopyImageAttributes(AttributeList list,
			long imageWidth,long imageLength,String frameOfReferenceUID,double mmPerPixelX,double mmPerPixelY,double sliceThickness,double objectiveLensPower,double objectiveLensNumericalAperture,
			String opticalPathIdentifier,String opticalPathDescription,
			double xOffsetInSlideCoordinateSystem,double yOffsetInSlideCoordinateSystem,
			String containerIdentifier,String specimenIdentifier,String specimenUID,
			String imageFlavor,String imageDerivation,String pyramidUID,String acquisitionUID) throws DicomException {
		
		if (list == null) {
			list = new AttributeList();
		}

		boolean isLabel = "LABEL".equals(imageFlavor);							// CP 2086
		boolean isLabelOrOverview = isLabel || "OVERVIEW".equals(imageFlavor);	// CP 2086

		// Frame of Reference Module
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.FrameOfReferenceUID); a.addValue(frameOfReferenceUID); list.put(a); }	// (001244) (001256)
		
		boolean havePositionReference = !(xOffsetInSlideCoordinateSystem == 0 && yOffsetInSlideCoordinateSystem == 0);	// (001362)
		{ Attribute a = new LongStringAttribute(TagFromName.PositionReferenceIndicator); a.addValue(havePositionReference ? "SLIDE_CORNER" : "UNKNOWN"); list.put(a); }	// (001272)

		// Whole Slide Microscopy Series Module
		
		// Multi-frame Functional Groups Module
		
		addWholeSlideMicroscopyImageFrameTypeSharedFunctionalGroup(list,imageFlavor,imageDerivation);

		{
			SequenceAttribute aSharedFunctionalGroupsSequence = (SequenceAttribute)list.get(TagFromName.SharedFunctionalGroupsSequence);
			AttributeList sharedFunctionalGroupsSequenceList = SequenceAttribute.getAttributeListFromWithinSequenceWithSingleItem(aSharedFunctionalGroupsSequence);

			SequenceAttribute aPixelMeasuresSequence = new SequenceAttribute(TagFromName.PixelMeasuresSequence);
			sharedFunctionalGroupsSequenceList.put(aPixelMeasuresSequence);
			AttributeList itemList = new AttributeList();
			aPixelMeasuresSequence.addItem(itemList);
			
			// note that order in DICOM in PixelSpacing is "adjacent row spacing" (Y), then "adjacent column spacing" (X) ...
			if (!isLabel) {				// CP 2086, except that since we know mmPerPixel for SVS overview images, we should include it
				Attribute a = new DecimalStringAttribute(TagFromName.PixelSpacing); a.addValue(mmPerPixelY); a.addValue(mmPerPixelX); itemList.put(a);
			}
			if (!isLabelOrOverview) {	// CP 2086
				Attribute a = new DecimalStringAttribute(TagFromName.SliceThickness); a.addValue(sliceThickness); itemList.put(a);	// (001315),(001317) Is in mm
			}
			//{ Attribute a = new DecimalStringAttribute(TagFromName.SpacingBetweenSlices); a.addValue(sliceSpacing); itemList.put(a); }
		}

		
		// Multi-frame Dimension Module - add it even though we are using TILED_FULL so not adding Per-Frame Functional Group :(
		{
			// derived from IndexedLabelMapToSegmentation.IndexedLabelMapToSegmentation() - should refactor :(
			String dimensionOrganizationUID = u.getAnotherNewUID();
			{
				SequenceAttribute saDimensionOrganizationSequence = new SequenceAttribute(TagFromName.DimensionOrganizationSequence);
				list.put(saDimensionOrganizationSequence);
				{
					AttributeList itemList = new AttributeList();
					saDimensionOrganizationSequence.addItem(itemList);
					{ Attribute a = new UniqueIdentifierAttribute(TagFromName.DimensionOrganizationUID); a.addValue(dimensionOrganizationUID); itemList.put(a); }
				}
			}
			{ Attribute a = new CodeStringAttribute(TagFromName.DimensionOrganizationType); a.addValue("TILED_FULL"); list.put(a); }
			{
				SequenceAttribute saDimensionIndexSequence = new SequenceAttribute(TagFromName.DimensionIndexSequence);
				list.put(saDimensionIndexSequence);
				{
					AttributeList itemList = new AttributeList();
					saDimensionIndexSequence.addItem(itemList);
					{ AttributeTagAttribute a = new AttributeTagAttribute(TagFromName.DimensionIndexPointer); a.addValue(TagFromName.RowPositionInTotalImagePixelMatrix); itemList.put(a); }
					{ AttributeTagAttribute a = new AttributeTagAttribute(TagFromName.FunctionalGroupPointer); a.addValue(TagFromName.PlanePositionSlideSequence); itemList.put(a); }
					{ Attribute a = new UniqueIdentifierAttribute(TagFromName.DimensionOrganizationUID); a.addValue(dimensionOrganizationUID); itemList.put(a); }
					{ Attribute a = new LongStringAttribute(TagFromName.DimensionDescriptionLabel); a.addValue("Row Position"); itemList.put(a); }
				}
				{
					AttributeList itemList = new AttributeList();
					saDimensionIndexSequence.addItem(itemList);
					{ AttributeTagAttribute a = new AttributeTagAttribute(TagFromName.DimensionIndexPointer); a.addValue(TagFromName.ColumnPositionInTotalImagePixelMatrix); itemList.put(a); }
					{ AttributeTagAttribute a = new AttributeTagAttribute(TagFromName.FunctionalGroupPointer); a.addValue(TagFromName.PlanePositionSlideSequence); itemList.put(a); }
					{ Attribute a = new UniqueIdentifierAttribute(TagFromName.DimensionOrganizationUID); a.addValue(dimensionOrganizationUID); itemList.put(a); }
					{ Attribute a = new LongStringAttribute(TagFromName.DimensionDescriptionLabel); a.addValue("Column Position"); itemList.put(a); }
				}
			}
		}


		// Specimen Module

		{ Attribute a = new LongStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("ContainerIdentifier")); a.addValue(containerIdentifier); list.put(a); }					// Dummy value - should be able to override this :(
		{ Attribute a = new SequenceAttribute(DicomDictionary.StandardDictionary.getTagFromName("IssuerOfTheContainerIdentifierSequence")); list.put(a); }
		CodedSequenceItem.putSingleCodedSequenceItem(list,DicomDictionary.StandardDictionary.getTagFromName("ContainerTypeCodeSequence"),"433466003","SCT","Microscope slide");	// No way of determining this :(
		{
			SequenceAttribute aSpecimenDescriptionSequence = new SequenceAttribute(DicomDictionary.StandardDictionary.getTagFromName("SpecimenDescriptionSequence"));
			list.put(aSpecimenDescriptionSequence);
			{
				AttributeList itemList = new AttributeList();
				aSpecimenDescriptionSequence.addItem(itemList);
				{ Attribute a = new LongStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("SpecimenIdentifier")); a.addValue(specimenIdentifier); itemList.put(a); }	// Dummy value - should be able to override this :(
				{ Attribute a = new SequenceAttribute(DicomDictionary.StandardDictionary.getTagFromName("IssuerOfTheSpecimenIdentifierSequence")); itemList.put(a); }
				{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SpecimenUID); a.addValue(specimenUID); itemList.put(a); }
				{ Attribute a = new SequenceAttribute(DicomDictionary.StandardDictionary.getTagFromName("SpecimenPreparationSequence")); itemList.put(a); }						// Would be nice to be able to populate this :(
			}
		}
		
		// Multi-Resolution Pyramid Module CP 2135
		if (pyramidUID != null && pyramidUID.length() > 0 ) {
			Attribute a = new UniqueIdentifierAttribute(TagFromName.PyramidUID); a.addValue(pyramidUID); list.put(a);
		}
		// PyramidLabel
		// PyramidDescription
		
		// General Acquisition Module CP 2135
		if (acquisitionUID != null && acquisitionUID.length() > 0 ) {	// (001361)
			Attribute a = new UniqueIdentifierAttribute(TagFromName.AcquisitionUID); a.addValue(acquisitionUID); list.put(a);
		}

		// Whole Slide Microscopy Image Module
		
		{ Attribute a = new CodeStringAttribute(TagFromName.ImageType); a.addValue("DERIVED"); a.addValue("PRIMARY"); a.addValue(imageFlavor); a.addValue(imageDerivation); list.put(a); }	// override default from CommonConvertedAttributeGeneration; same as FrameType

		if (!isLabel) {	// CP 2086 - actually condition is "is VOLUME", but we want it for THUMBNAIL too, except that since we know mmPerPixel for SVS overview images, we should include it
			Attribute a = new FloatSingleAttribute(DicomDictionary.StandardDictionary.getTagFromName("ImagedVolumeWidth"));  a.addValue(imageWidth*mmPerPixelX); list.put(a);
		}
		if (!isLabel) {	// CP 2086 - actually condition is "is VOLUME", but we want it for THUMBNAIL too, except that since we know mmPerPixel for SVS overview images, we should include it
			Attribute a = new FloatSingleAttribute(DicomDictionary.StandardDictionary.getTagFromName("ImagedVolumeHeight")); a.addValue(imageLength*mmPerPixelY); list.put(a);
		}
		if (!isLabelOrOverview) {	// CP 2086 - actually condition is "is VOLUME", but we want it for THUMBNAIL too
			Attribute a = new FloatSingleAttribute(DicomDictionary.StandardDictionary.getTagFromName("ImagedVolumeDepth"));  a.addValue(sliceThickness*1000); list.put(a);	// (001315),(001317) Is in microns, not mm
		}

		{ Attribute a = new UnsignedLongAttribute(DicomDictionary.StandardDictionary.getTagFromName("TotalPixelMatrixColumns")); a.addValue(imageWidth);  list.put(a); }
		{ Attribute a = new UnsignedLongAttribute(DicomDictionary.StandardDictionary.getTagFromName("TotalPixelMatrixRows")); a.addValue(imageLength); list.put(a); }
		{ Attribute a = new UnsignedLongAttribute(DicomDictionary.StandardDictionary.getTagFromName("TotalPixelMatrixFocalPlanes")); a.addValue(1); list.put(a); }
		
		addTotalPixelMatrixOriginSequence(list,xOffsetInSlideCoordinateSystem,yOffsetInSlideCoordinateSystem);
		
		// assume slide on its side with label on left, which seems to be what Aperio, Hamamatsu, AIDPATH are
		{ Attribute a = new DecimalStringAttribute(TagFromName.ImageOrientationSlide); a.addValue(0.0); a.addValue(-1.0); a.addValue(0.0); a.addValue(-1.0); a.addValue(0.0); a.addValue(0.0); list.put(a); }
		{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime); list.put(a); }							// No way of determining this :(
		// AcquisitionDuration is optional after CP 1821
		
		// Lossy Image Compression - handled later by insertLossyImageCompressionHistory
		// Lossy Image Compression Ratio
		// Lossy Image Compression Method
		
		if (Attribute.getSingleIntegerValueOrDefault(list,TagFromName.SamplesPerPixel,0) == 1) {
			{ Attribute a = new CodeStringAttribute(TagFromName.PresentationLUTShape); a.addValue("IDENTITY"); list.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleIntercept); a.addValue(0); list.put(a); }
			{ Attribute a = new DecimalStringAttribute(TagFromName.RescaleSlope); a.addValue(1); list.put(a); }
		}
		
		{ Attribute a = new CodeStringAttribute(TagFromName.VolumetricProperties); a.addValue("VOLUME"); list.put(a); }
		{ Attribute a = new CodeStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("SpecimenLabelInImage")); a.addValue("LABEL".equals(imageFlavor) || "OVERVIEW".equals(imageFlavor) ? "YES" : "NO"); list.put(a); }		// Otherwise no way of determining this and most not :( (001328)
		{ Attribute a = new CodeStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("FocusMethod")); a.addValue("AUTO"); list.put(a); }			// No way of determining this and most are :(
		{ Attribute a = new CodeStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("ExtendedDepthOfField")); a.addValue("NO"); list.put(a); }		// No way of determining this and most not :(
		// NumberOfFocalPlanes - not need if ExtendedDepthOfField NO
		// DistanceBetweenFocalPlanes - not need if ExtendedDepthOfField NO
		// AcquisitionDeviceProcessingDescription - Type 3
		// ConvolutionKernel - Type 3
		{ Attribute a = new UnsignedShortAttribute(DicomDictionary.StandardDictionary.getTagFromName("RecommendedAbsentPixelCIELabValue")); a.addValue(0xFFFF); a.addValue(0); a.addValue(0); list.put(a); }		// white (0xFFFF is 100 per PS3.3 C.10.7.1.1)

		// Optical Path Module

		{ Attribute a = new UnsignedLongAttribute(DicomDictionary.StandardDictionary.getTagFromName("NumberOfOpticalPaths")); a.addValue(1); list.put(a); }
		{
			SequenceAttribute aOpticalPathSequence = new SequenceAttribute(DicomDictionary.StandardDictionary.getTagFromName("OpticalPathSequence"));
			list.put(aOpticalPathSequence);
			{
				AttributeList opticalPathSequenceItemList = new AttributeList();
				aOpticalPathSequence.addItem(opticalPathSequenceItemList);
				{ Attribute a = new ShortStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("OpticalPathIdentifier")); a.addValue("0"); opticalPathSequenceItemList.put(a); }
				CodedSequenceItem.putSingleCodedSequenceItem(opticalPathSequenceItemList,DicomDictionary.StandardDictionary.getTagFromName("IlluminationColorCodeSequence"),"414298005","SCT","Full Spectrum");
				CodedSequenceItem.putSingleCodedSequenceItem(opticalPathSequenceItemList,DicomDictionary.StandardDictionary.getTagFromName("IlluminationTypeCodeSequence"),"111744","DCM","Brightfield illumination");
				// ICCProfile and ColorSpace are added later
				// ObjectiveLensPower could be supplied later in which case this default will be overridden but may be reapplied later
				if (objectiveLensPower != 0) {	// (001270)
					{ Attribute a = new DecimalStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("ObjectiveLensPower")); a.addValue(objectiveLensPower); opticalPathSequenceItemList.put(a); }
				}
				if (objectiveLensNumericalAperture != 0) {	// (001319)
					{ Attribute a = new DecimalStringAttribute(DicomDictionary.StandardDictionary.getTagFromName("ObjectiveLensNumericalAperture")); a.addValue(objectiveLensNumericalAperture); opticalPathSequenceItemList.put(a); }
				}
			}
		}

		// Multi-Resolution Navigation Module - Retired
		
		// Slide Label Module (001328)
		if ("LABEL".equals(imageFlavor)) {
			// Type 2 and no values known since do not have capability to OCR label image
			// only replace them if not already populated (e.g., from ImageDescription)
			if (list.get(DicomDictionary.StandardDictionary.getTagFromName("BarcodeValue")) == null)
				{ Attribute a = new LongTextAttribute(DicomDictionary.StandardDictionary.getTagFromName("BarcodeValue")); list.put(a); }
			if (list.get(DicomDictionary.StandardDictionary.getTagFromName("LabelText")) == null)
				{ Attribute a = new UnlimitedTextAttribute(DicomDictionary.StandardDictionary.getTagFromName("LabelText")); list.put(a); }
		}

		return list;
	}
	
	// reuse same private group and creator as for com.pixelmed.dicom.PrivatePixelData
	private static final String pixelmedPrivateCreatorForPyramidData = pixelmedPrivateCreator;
	private static final int pixelmedPrivatePyramidDataGroup = 0x7FDF;	// Must be BEFORE (7FE0,0010) because we assume elsewhere that DataSetTrailingPadding will immediately follow (7FE0,0010)
	private static final AttributeTag pixelmedPrivatePyramidDataBlockReservation = new AttributeTag(pixelmedPrivatePyramidDataGroup,0x0010);
	private static final AttributeTag pixelmedPrivatePyramidData = new AttributeTag(pixelmedPrivatePyramidDataGroup,0x1001);
	
	private void queueTemporaryPixelDataFilesForDeletion(Attribute aPixelData) {
		if (aPixelData != null) {
			File[] frameFiles = null;
			if (aPixelData instanceof OtherByteAttributeMultipleCompressedFrames) {
				frameFiles = ((OtherByteAttributeMultipleCompressedFrames)aPixelData).getFiles();
			}
			else if (aPixelData instanceof OtherByteAttributeMultipleFilesOnDisk) {
				frameFiles = ((OtherByteAttributeMultipleFilesOnDisk)aPixelData).getFiles();
			}
			else if (aPixelData instanceof OtherWordAttributeMultipleFilesOnDisk) {
				frameFiles = ((OtherWordAttributeMultipleFilesOnDisk)aPixelData).getFiles();
			}
			if (frameFiles != null) {
				if (filesToDeleteAfterWritingDicomFile == null) {
					filesToDeleteAfterWritingDicomFile = new ArrayList<>(Arrays.asList(frameFiles));	// make a copy because Arrays.asList() is documented not to support any adding elements "https://stackoverflow.com/questions/5755477/java-list-add-unsupportedoperationexception"
				}
				else {
					Collections.addAll(filesToDeleteAfterWritingDicomFile,frameFiles);
				}
			}
		}
	}

	private int generateDICOMPyramidPixelDataModule(AttributeList list,String outputformat,String transferSyntax) throws DicomException, IOException {
		int numberOfPyramidLevels = 1;
		{ Attribute a = new LongStringAttribute(pixelmedPrivatePyramidDataBlockReservation); a.addValue(pixelmedPrivateCreatorForPyramidData); list.put(a); }
		SequenceAttribute pyramidData = new SequenceAttribute(pixelmedPrivatePyramidData);
		list.put(pyramidData);
		
		boolean isFirstList = true;
		AttributeList oldList = list;
		while (true) {
			TiledFramesIndex index = new TiledFramesIndex(oldList,true/*physical*/,false/*buildInverseIndex*/,true/*ignorePlanePosition*/);
			int numberOfColumnsOfTiles = index.getNumberOfColumnsOfTiles();
			int numberOfRowsOfTiles = index.getNumberOfRowsOfTiles();
			if (numberOfColumnsOfTiles <= 1 && numberOfRowsOfTiles <= 1) break;
			++numberOfPyramidLevels;
			slf4jlogger.debug("generateDICOMPyramidPixelDataModule(): downsampling from numberOfColumnsOfTiles = {}, numberOfRowsOfTiles = {}",numberOfColumnsOfTiles,numberOfRowsOfTiles);
			AttributeList newList = new AttributeList();
			if (!isFirstList) {
				Attribute a = new UniqueIdentifierAttribute(TagFromName.TransferSyntaxUID); a.addValue(transferSyntax); oldList.put(a);	// need this or won't decompress
			}
			TiledPyramid.createDownsampledDICOMAttributes(oldList,newList,index,outputformat,true/*populateunchangedimagepixeldescriptionmacroattributes*/,false/*populatefunctionalgroups*/);
			if (!isFirstList) {
				oldList.remove(TagFromName.TransferSyntaxUID);	// need to remove it again since not allowed anywhere except meta header, which will be added later
			}
			pyramidData.addItem(newList);
			queueTemporaryPixelDataFilesForDeletion(newList.get(TagFromName.PixelData));	// PixelData in newList will use files that need to be deleted after writing
			oldList = newList;
			isFirstList = false;
		}
		return numberOfPyramidLevels;
	}
	
	private String mergeImageDescription(String[] description) {
		StringBuffer buf = new StringBuffer();
		if (description != null && description.length > 0) {
			slf4jlogger.debug("mergeImageDescription(): description.length = {}",description.length);
			for (String d : description) {
				if (buf.length() > 0) {
					buf.append("\n");
				}
				buf.append(d);
			}
		}
		return buf.toString();
	}
	
	// (001398)
	private void getEquipmentFromTIFFImageDescription(String[] description,AttributeList descriptionList) throws DicomException {
		String manufacturer = "";
		String manufacturerModelName = "";
		String softwareVersions = "";
		String deviceSerialNumber = "";
		String aperioImageLibraryVersion = "";
		if (description != null && description.length > 0) {
			slf4jlogger.trace("getEquipmentFromTIFFImageDescription(): description.length = {}",description.length);
			for (String d : description) {
				slf4jlogger.trace("getEquipmentFromTIFFImageDescription(): String = {}",d);
				
				// need to check XML first, since string "Aperio" may appear in XML
				if (d.contains("DPUfsImport")) {	// (001389) Philips DPUfsImport as in "<?xml ... <DataObject ObjectType="DPUfsImport">"
					slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): Parsing DPUfsImport XML metadata");
					try {
						Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(d)));
						XPathFactory xpf = XPathFactory.newInstance();
						
						// <DataObject ObjectType="DPUfsImport">
						// 	<Attribute Name="DICOM_MANUFACTURER" Group="0x0008" Element="0x0070" PMSVR="IString">3D Histech</Attribute>
						manufacturer = xpf.newXPath().evaluate("/DataObject[@ObjectType='DPUfsImport']/Attribute[@Name='DICOM_MANUFACTURER']",document).replaceAll("\"","");
						slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): found manufacturer {}",manufacturer);
						
						// <DataObject ObjectType="DPUfsImport">
						// 	<Attribute Name="DICOM_SOFTWARE_VERSIONS" Group="0x0018" Element="0x1020" PMSVR="IStringArray">&quot;4.0.3&quot;</Attribute>
						softwareVersions = xpf.newXPath().evaluate("/DataObject[@ObjectType='DPUfsImport']/Attribute[@Name='DICOM_SOFTWARE_VERSIONS']",document).replaceAll("\"","");
						slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): found softwareVersions {}",softwareVersions);
					}
					catch (Exception e) {
						slf4jlogger.error("Failed to parse DPUfsImport XML metadata in ImageDescription ",e);
					}
				}
				else if (d.contains("<OME")) {	// (001285) (001322) (001390) not just any XML
					slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): Parsing OME-TIFF XML metadata");
					try {
						Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(d)));
						XPathFactory xpf = XPathFactory.newInstance();
						
						// OME/Instrument/Microscope@  Model="Aperio AT2" Manufacturer="Leica Biosystems"
						manufacturer = xpf.newXPath().evaluate("/OME/Instrument/Microscope/@Manufacturer",document);
						manufacturerModelName = xpf.newXPath().evaluate("/OME/Instrument/Microscope/@Model",document);
						slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): found manufacturer {}",manufacturer);
						slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): found manufacturerModelName {}",manufacturerModelName);
						{
							// <Description>Aperio Image Library v12.0.15 </Description>
							String imageDescription = xpf.newXPath().evaluate("/OME/Image/Description",document);
							Pattern p = Pattern.compile(".*Aperio Image Library (v[A-Z0-9][A-Z0-9. ]*[0-9]).*");
							Matcher m = p.matcher(imageDescription);
							if (m.matches()) {
								slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have Aperio Image Library match");
								int groupCount = m.groupCount();
								if (groupCount == 1) {
									slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have Aperio Image Library correct groupCount");
									softwareVersions = m.group(1);
									slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): found softwareVersions (Aperio Image Library) {}",softwareVersions);
								}
							}
						}
						
						// OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Key-Value pairs
						// <StructuredAnnotations><XMLAnnotation ID="Annotation:5" Namespace="openmicroscopy.org/OriginalMetadata"><Value>
						// <OriginalMetadata><Key>Series 0 ScanScope ID</Key><Value>SS7632</Value></OriginalMetadata>
						{
							NodeList originalMetadataKeyValuePairs = (NodeList)(xpf.newXPath().evaluate("//OriginalMetadata",document,XPathConstants.NODESET)); // blech :(
							if (originalMetadataKeyValuePairs != null && originalMetadataKeyValuePairs.getLength() > 0) {
								for (int pair=0; pair<originalMetadataKeyValuePairs.getLength(); ++pair) {
									Node originalMetadataKeyValuePair = originalMetadataKeyValuePairs.item(pair);
									String key = xpf.newXPath().evaluate("Key",originalMetadataKeyValuePair);
									String value = xpf.newXPath().evaluate("Value",originalMetadataKeyValuePair);
									if (key != null && key.length() > 0
										&& value != null && value.length() > 0) {
										slf4jlogger.trace("getEquipmentFromTIFFImageDescription(): Have OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Key {} Value {}",key,value);
										if (key.matches("^Series [0-9]+ ScanScope ID$")) {
											slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): Have OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata ScanScope ID {}",value);
											deviceSerialNumber = value;
											if (manufacturer == null || manufacturer.length() == 0) {
												manufacturer = "Leica Biosystems";
												slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): because ScanScope ID is present, using manufacturer {}",manufacturer);
											}
											if (manufacturerModelName == null || manufacturerModelName.length() == 0) {
												manufacturerModelName = "Aperio";	// in absence of more specific information about whether AT2 (DX), CS2, GT450 (DX)
												slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): because ScanScope ID is present, using manufacturerModelName {}",manufacturerModelName);
											}
										}
										//else {
										//	slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): Ignoring OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Key {} Value {}",key,value);
										//}
									}
									else {
										slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): Cannot find Key and Value for OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata ",originalMetadataKeyValuePair.toString());
									}
								}
							}
							else {
								slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): no OME-TIFF XML OriginalMetadata");
							}
						}
					}
					catch (Exception e) {
						slf4jlogger.error("Failed to parse OME-TIFF XML metadata in ImageDescription ",e);
					}
				}
				else if (d.contains("Aperio")) {
					// Aperio Image Library v10.0.51
					// 46920x33014 [0,100 46000x32914] (256x256) JPEG/RGB Q=30|AppMag = 20|StripeWidth = 2040|ScanScope ID = CPAPERIOCS|Filename = CMU-1|Date = 12/29/09|Time = 09:59:15|User = b414003d-95c6-48b0-9369-8010ed517ba7|Parmset = USM Filter|MPP = 0.4990|Left = 25.691574|Top = 23.449873|LineCameraSkew = -0.000424|LineAreaXOffset = 0.019265|LineAreaYOffset = -0.000313|Focus Offset = 0.000000|ImageID = 1004486|OriginalWidth = 46920|Originalheight = 33014|Filtered = 5|ICC Profile = ScanScope v1

					// Aperio Image Library v10.0.51
					// 46000x32914 -> 1024x732 - |AppMag = 20|StripeWidth = 2040|ScanScope ID = CPAPERIOCS|Filename = CMU-1|Date = 12/29/09|Time = 09:59:15|User = b414003d-95c6-48b0-9369-8010ed517ba7|Parmset = USM Filter|MPP = 0.4990|Left = 25.691574|Top = 23.449873|LineCameraSkew = -0.000424|LineAreaXOffset = 0.019265|LineAreaYOffset = -0.000313|Focus Offset = 0.000000|ImageID = 1004486|OriginalWidth = 46920|Originalheight = 33014|Filtered = 5|ICC Profile = ScanScope v1

					// Aperio Image Library v10.0.51
					// 46920x33014 [0,100 46000x32914] (256x256) -> 11500x8228 JPEG/RGB Q=65
				
					// Aperio Image Library v11.2.1
					// 46000x32914 [0,0 46000x32893] (240x240) J2K/KDU Q=30;CMU-1;Aperio Image Library v10.0.51
					// 46920x33014 [0,100 46000x32914] (256x256) JPEG/RGB Q=30|AppMag = 20|StripeWidth = 2040|ScanScope ID = CPAPERIOCS|Filename = CMU-1|Date = 12/29/09|Time = 09:59:15|User = b414003d-95c6-48b0-9369-8010ed517ba7|Parmset = USM Filter|MPP = 0.4990|Left = 25.691574|Top = 23.449873|LineCameraSkew = -0.000424|LineAreaXOffset = 0.019265|LineAreaYOffset = -0.000313|Focus Offset = 0.000000|ImageID = 1004486|OriginalWidth = 46920|Originalheight = 33014|Filtered = 5|OriginalWidth = 46000|OriginalHeight = 32914

					// Aperio Image Library v11.2.1
					// 46000x32893 -> 1024x732 - ;CMU-1;Aperio Image Library v10.0.51
					// 46920x33014 [0,100 46000x32914] (256x256) JPEG/RGB Q=30|AppMag = 20|StripeWidth = 2040|ScanScope ID = CPAPERIOCS|Filename = CMU-1|Date = 12/29/09|Time = 09:59:15|User = b414003d-95c6-48b0-9369-8010ed517ba7|Parmset = USM Filter|MPP = 0.4990|Left = 25.691574|Top = 23.449873|LineCameraSkew = -0.000424|LineAreaXOffset = 0.019265|LineAreaYOffset = -0.000313|Focus Offset = 0.000000|ImageID = 1004486|OriginalWidth = 46920|Originalheight = 33014|Filtered = 5|OriginalWidth = 46000|OriginalHeight = 32914

					// Aperio Image Library v11.2.1
					// macro 1280x431
					
					// Aperio Image Library v11.0.37
					// 29600x42592 (256x256) J2K/KDU Q=70;BioImagene iScan|Scanner ID = BI10N0294|AppMag = 20|MPP = 0.46500
					
					// Aperio Leica Biosystems GT450 v1.0.1
					// 152855x79623 [0,0,152855x79623] (256x256) JPEG/YCC Q=91|AppMag = 40|Date = 07/22/2020|Exposure Scale = 0.000001|Exposure Time = 8|Filtered = 3|Focus Offset = 0.089996|Gamma = 2.2|Left = 6.8904371261597|MPP = 0.263592|Rack = 1|ScanScope ID = SS45002|Slide = 1|StripeWidth = 4096|Time = 08:34:09|Time Zone = GMT+0100|Top = 23.217206954956

					// 99200x89856 (256x256) J2K/KDU Q=70;NanoZoomer Digital Pathology Image|AppMag = 40|MPP = 0.2265

					// 99328x110848 -> 688x768 - ;Mirax Digital Slide|AppMag = 20|MPP = 0.23250

					try {
						BufferedReader r = new BufferedReader(new StringReader(d));
						String line = null;
						while ((line=r.readLine()) != null) {
							{
								// Aperio Leica Biosystems GT450 v1.0.1
								Pattern p = Pattern.compile(".*Aperio Leica Biosystems GT450 (v[0-9][0-9.]*[0-9]).*");
								Matcher m = p.matcher(line);
								if (m.matches()) {
									slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have Aperio Leica Biosystems GT450 match");
									manufacturerModelName = "GT450";
									int groupCount = m.groupCount();
									if (groupCount == 1) {
										slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have Aperio Leica Biosystems GT450 correct groupCount");
										softwareVersions = m.group(1);
										slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): found softwareVersions (Aperio Leica Biosystems GT450) {}",softwareVersions);
									}
								}
							}
							{
								// |ScanScope ID = CPAPERIOCS|
								// |ScanScope ID = SS1302|
								Pattern p = Pattern.compile(".*[|]ScanScope ID[ ]*=[ ]*([^|]*)[|].*");
								Matcher m = p.matcher(line);
								if (m.matches()) {
									slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have ScanScope ID match");
									int groupCount = m.groupCount();
									if (groupCount == 1) {
										slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have ScanScope ID correct groupCount");
										deviceSerialNumber = m.group(1);
										slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): found deviceSerialNumber (ScanScope ID) {}",deviceSerialNumber);
									}
								}
							}
							{
								// |Scanner ID = BI10N0294|
								Pattern p = Pattern.compile(".*[|]Scanner ID[ ]*=[ ]*([^|]*)[|].*");
								Matcher m = p.matcher(line);
								if (m.matches()) {
									slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have Scanner ID match");
									int groupCount = m.groupCount();
									if (groupCount == 1) {
										slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have Scanner ID correct groupCount");
										deviceSerialNumber = m.group(1);
										slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): found deviceSerialNumber (Scanner ID) {}",deviceSerialNumber);
									}
								}
							}
							{
								// ;BioImagene iScan|
								Pattern p = Pattern.compile(".*;BioImagene iScan[|].*");
								Matcher m = p.matcher(line);
								if (m.matches()) {
									slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have BioImagene iScan match");
									manufacturer="BioImagene";
									manufacturerModelName="iScan";
								}
							}
							{
								// ;NanoZoomer Digital Pathology Image|
								Pattern p = Pattern.compile(".*;NanoZoomer Digital Pathology Image[|].*");	// (001363)
								Matcher m = p.matcher(line);
								if (m.matches()) {
									slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have NanoZoomer Digital Pathology Image match");
									manufacturer="Hamamatsu";
									manufacturerModelName="NanoZoomer";
								}
							}
							{
								// ;Mirax Digital Slide|
								Pattern p = Pattern.compile(".*;Mirax Digital Slide[|].*");	// (001363)
								Matcher m = p.matcher(line);
								if (m.matches()) {
									slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have Mirax Digital Slide match");
									manufacturer="Carl Zeiss";
									manufacturerModelName="Mirax";
								}
							}
							{
								// |Copyright=Hamamatsu Photonics KK|
								Pattern p = Pattern.compile(".*[|]Copyright=Hamamatsu Photonics KK[|].*");	// (001363)
								Matcher m = p.matcher(line);
								if (m.matches()) {
									slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have Hamamatsu Photonics KK match");
									manufacturer="Hamamatsu";
									manufacturerModelName="";
								}
							}
							{
								// Aperio Image Library v10.0.51
								// Aperio Image Library vFS90 01
								Pattern p = Pattern.compile(".*Aperio Image Library (v[A-Z0-9][A-Z0-9. ]*[0-9]).*");
								Matcher m = p.matcher(line);
								if (m.matches()) {
									slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have Aperio Image Library match");
									int groupCount = m.groupCount();
									if (groupCount == 1) {
										slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): have Aperio Image Library correct groupCount");
										aperioImageLibraryVersion = m.group(1);
										slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): found Aperio Image Library version {}",aperioImageLibraryVersion);
									}
								}
							}
						}
					}
					catch (IOException e) {
						slf4jlogger.error("Failed to parse ImageDescription ",e);
					}
					if (manufacturer.length() == 0) {
						manufacturer = "Leica Biosystems";
						manufacturerModelName = "Aperio";	// in absence of more specific information about whether AT2 (DX), CS2, GT450 (DX)
						slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): SVS so assuming manufacturer {}",manufacturer);
						slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): SVS so assuming  manufacturerModelName {}",manufacturerModelName);
					}
				}
				else if (d.contains("X scan size")) {
					// encountered in 3D Histech uncompressed TIFF samples
					manufacturer = "3D Histech";
					slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): guessing manufacturer {}",manufacturer);
				}
				else {
					slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): nothing recognized in ImageDescription");
				}
			}
			
			if (softwareVersions.length() == 0 && manufacturer.equals("Leica Biosystems")) {
				softwareVersions = aperioImageLibraryVersion;
			}
			// else do not use Aperio library version as version of another manufacturer's equipment

			if (manufacturer.length() > 0) {
				slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): setting DICOM Manufacturer to {}",manufacturer);
				{ Attribute a = new LongStringAttribute(TagFromName.Manufacturer); a.addValue(manufacturer); descriptionList.put(a); }
			}
			if (manufacturerModelName.length() > 0) {
				slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): setting DICOM ManufacturerModelName to {}",manufacturerModelName);
				{ Attribute a = new LongStringAttribute(TagFromName.ManufacturerModelName); a.addValue(manufacturerModelName); descriptionList.put(a); }
			}
			if (softwareVersions.length() > 0) {
				slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): setting DICOM SoftwareVersions to {}",softwareVersions);
				{ Attribute a = new LongStringAttribute(TagFromName.SoftwareVersions); a.addValue(softwareVersions); descriptionList.put(a); }
			}
			if (deviceSerialNumber.length() > 0) {
				slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): setting DICOM DeviceSerialNumber to {}",deviceSerialNumber);
				{ Attribute a = new LongStringAttribute(TagFromName.DeviceSerialNumber); a.addValue(deviceSerialNumber); descriptionList.put(a); }
			}
		}
		else {
				slf4jlogger.debug("getEquipmentFromTIFFImageDescription(): no ImageDescription");
		}
	}
	
	private void parseTIFFImageDescription(String[] description,AttributeList descriptionList) throws DicomException {
		String derivationDescription = "";
		String barcodeValue = "";
		if (description != null && description.length > 0) {
			slf4jlogger.trace("parseTIFFImageDescription(): description.length = {}",description.length);
			for (String d : description) {
				slf4jlogger.trace("parseTIFFImageDescription(): String = {}",d);
				
				// need to check XML first, since string "Aperio" may appear in XML
				if (d.contains("DPUfsImport")) {	// (001389) Philips DPUfsImport as in "<?xml ... <DataObject ObjectType="DPUfsImport">"
					slf4jlogger.debug("parseTIFFImageDescription(): Parsing DPUfsImport XML metadata");
					try {
						Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(d)));
						XPathFactory xpf = XPathFactory.newInstance();
						
						// <DataObject ObjectType="DPUfsImport">
						// 	<Attribute Name="PIM_DP_UFS_BARCODE" Group="0x301D" Element="0x1002" PMSVR="IString">MzY0NDkzNQ==</Attribute>
						barcodeValue = xpf.newXPath().evaluate("/DataObject[@ObjectType='DPUfsImport']/Attribute[@Name='PIM_DP_UFS_BARCODE']",document);
						slf4jlogger.debug("parseTIFFImageDescription(): found barcodeValue {}",barcodeValue);	// have not seen quotes around it so don't mess with it

						// <DataObject ObjectType="DPUfsImport">
						// 	<Attribute Name="PIM_DP_SCANNED_IMAGES" Group="0x301D" Element="0x1003" PMSVR="IDataObjectArray">
						// 		<Array>
						// 			<DataObject ObjectType="DPScannedImage">
						// 				<Attribute Name="DICOM_DERIVATION_DESCRIPTION" Group="0x0008" Element="0x2111" PMSVR="IString">tiff-useBigTIFF=1-useRgb=0-levels=10003,10002,10000,10001-processing=0-q80-sourceFilename=&quot;T11-07929-I1-6&quot;</Attribute>
						derivationDescription = xpf.newXPath().evaluate("/DataObject[@ObjectType='DPUfsImport']/Attribute[@Name='PIM_DP_SCANNED_IMAGES']/Array/DataObject[@ObjectType='DPScannedImage']/Attribute[@Name='DICOM_DERIVATION_DESCRIPTION']",document);
						slf4jlogger.debug("parseTIFFImageDescription(): found derivationDescription {}",derivationDescription);	// leave embedded quotes because may be meaningful
					}
					catch (Exception e) {
						slf4jlogger.error("Failed to parse DPUfsImport XML metadata in ImageDescription ",e);
					}
				}
				else if (d.contains("<OME")) {	// (001285) (001322) (001390) not just any XML
					slf4jlogger.debug("parseTIFFImageDescription(): Parsing OME-TIFF XML metadata");
					// nothing wanted ...
				}
				else if (d.contains("Aperio")) {
					// nothing wanted ...
				}
				else if (d.contains("X scan size")) {
					// encountered in 3D Histech uncompressed TIFF samples
				}
				else {
					slf4jlogger.debug("parseTIFFImageDescription(): nothing recognized in ImageDescription");
				}
			}

			if (derivationDescription.length() > 0) {
				slf4jlogger.debug("parseTIFFImageDescription(): setting DICOM DerivationDescription to {}",derivationDescription);
				{ Attribute a = new ShortTextAttribute(TagFromName.DerivationDescription); a.addValue(derivationDescription); descriptionList.put(a); }
			}
			if (barcodeValue.length() > 0) {
				slf4jlogger.debug("parseTIFFImageDescription(): setting DICOM BarcodeValue to {}",barcodeValue);
				{ Attribute a = new LongTextAttribute(DicomDictionary.StandardDictionary.getTagFromName("BarcodeValue")); a.addValue(barcodeValue); descriptionList.put(a); }
			}
		}
		else {
				slf4jlogger.debug("parseTIFFImageDescription(): no ImageDescription");
		}
	}
	
	private String selectSOPClassBasedOnTIFFCharacteristics(TIFFImageFileDirectories ifds) {
		String sopClass = null;
		//boolean isOMETIFFXML = false;
		boolean isAperioSVS = false;
		boolean isDPUfsImport = false;
		ArrayList<TIFFImageFileDirectory> ifdlist = ifds.getListOfImageFileDirectories();
		if (ifdlist.size() > 0) {
			TIFFImageFileDirectory ifd = ifdlist.get(0);
			String[] description = ifd.getStringValues(TIFFTags.IMAGEDESCRIPTION);
			if (description != null && description.length > 0) {
				slf4jlogger.trace("selectSOPClassBasedOnTIFFCharacteristics(): description.length = {}",description.length);
				for (String d : description) {
					slf4jlogger.trace("selectSOPClassBasedOnTIFFCharacteristics(): String = {}",d);
					// need to check XML first, since string "Aperio" may appear in XML
					//if (d.startsWith("<?xml") || d.startsWith("<OME")) {	// (001285) (001322)
					//	isOMETIFFXML = true;
					//}
					/*else */
					if (d.contains("Aperio")) {
						isAperioSVS = true;			// assume SVS but could be other Aperio format, theoretically ? :(
					}
					else if (d.contains("DPUfsImport")) {	// (001389) Philips DPUfsImport as in "<?xml ... <DataObject ObjectType="DPUfsImport">"
						isDPUfsImport = true;		// assume digital pathology, but can imports be of a different modality :(
					}
				}
			}
		}
		//slf4jlogger.debug("selectSOPClassBasedOnTIFFCharacteristics(): isOMETIFFXML={}",isOMETIFFXML);
		slf4jlogger.debug("selectSOPClassBasedOnTIFFCharacteristics(): isAperioSVS={}",isAperioSVS);
		if (isAperioSVS) {
			slf4jlogger.info("selectSOPClassBasedOnTIFFCharacteristics(): isAperioSVS so selecting VL Whole Slide Microscopy Image Storage SOP Class");
			sopClass = SOPClass.VLWholeSlideMicroscopyImageStorage;
		}
		else if (isDPUfsImport) {
			slf4jlogger.info("selectSOPClassBasedOnTIFFCharacteristics(): isDPUfsImport so selecting VL Whole Slide Microscopy Image Storage SOP Class");
			sopClass = SOPClass.VLWholeSlideMicroscopyImageStorage;
		}
		return sopClass;
	}

	private String getPastHistoryOfLossyCompression(ArrayList<TIFFImageFileDirectory> ifdlist) {
		String pastHistoryOfLossyCompression = "";
		if (ifdlist.size() > 0) {
			TIFFImageFileDirectory ifd = ifdlist.get(0);
			String[] description = ifd.getStringValues(TIFFTags.IMAGEDESCRIPTION);
			if (description != null && description.length > 0) {
				slf4jlogger.trace("getPastHistoryOfLossyCompression(): description.length = {}",description.length);
				for (String d : description) {
					slf4jlogger.trace("getPastHistoryOfLossyCompression(): String = {}",d);
					// need to check XML first, since string "Aperio" may appear in XML
					if (d.startsWith("<?xml") || d.startsWith("<OME")) {
						slf4jlogger.debug("getPastHistoryOfLossyCompression(): Parsing OME-TIFF XML metadata");
						try {
							Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(d)));
							XPathFactory xpf = XPathFactory.newInstance();
							
							// OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Key-Value pairs
							// <StructuredAnnotations><XMLAnnotation ID="Annotation:5" Namespace="openmicroscopy.org/OriginalMetadata"><Value>
							// <OriginalMetadata><Key>Compression</Key><Value>JPEG</Value></OriginalMetadata>
							// <OriginalMetadata><Key>Series 0 22352x27650 [0,100 21912x27550] (240x240) JPEG/RGB Q</Key><Value>70</Value></OriginalMetadata>
							//
							// <OriginalMetadata><Key>Compression</Key><Value>JPEG-2000</Value></OriginalMetadata>
							// <OriginalMetadata><Key>Series 2 46000x32914 [0,0 46000x32893] (240x240) - 11500x8223 J2K/KDU Q</Key><Value>30</Value></OriginalMetadata>
							{
								NodeList originalMetadataKeyValuePairs = (NodeList)(xpf.newXPath().evaluate("//OriginalMetadata",document,XPathConstants.NODESET)); // blech :(
								if (originalMetadataKeyValuePairs != null && originalMetadataKeyValuePairs.getLength() > 0) {
									for (int pair=0; pair<originalMetadataKeyValuePairs.getLength(); ++pair) {
										Node originalMetadataKeyValuePair = originalMetadataKeyValuePairs.item(pair);
										String key = xpf.newXPath().evaluate("Key",originalMetadataKeyValuePair);
										String value = xpf.newXPath().evaluate("Value",originalMetadataKeyValuePair);
										if (key != null && key.length() > 0
											&& value != null && value.length() > 0) {
											slf4jlogger.trace("getPastHistoryOfLossyCompression(): Have OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Key {} Value {}",key,value);
											if (key.matches("^Compression$")) {
												slf4jlogger.debug("getPastHistoryOfLossyCompression(): Have OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Compression {}",value);
												pastHistoryOfLossyCompression = value;
											}
											//else {
											//	slf4jlogger.debug("getPastHistoryOfLossyCompression(): Ignoring OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Key {} Value {}",key,value);
											//}
										}
										else {
											slf4jlogger.debug("getPastHistoryOfLossyCompression(): Cannot find Key and Value for OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata ",originalMetadataKeyValuePair.toString());
										}
									}
								}
								else {
									slf4jlogger.debug("getPastHistoryOfLossyCompression(): no OME-TIFF XML OriginalMetadata");
								}
							}
						}
						catch (Exception e) {
							slf4jlogger.error("Failed to parse OME-TIFF XML metadata in ImageDescription ",e);
						}
					}
					else if (d.contains("Aperio")) {
						// ? need to extract
					}
				}
			}
		}
		return pastHistoryOfLossyCompression;
	}
	
	private String[][] getImageFlavorAndDerivationByIFD(ArrayList<TIFFImageFileDirectory> ifdlist) {
		int numberOfIFDs = ifdlist.size();
		String[][] imageFlavorAndDerivationByIFD = new String[numberOfIFDs][];
		boolean isDPUfsImport = false;
		boolean isOMETIFFXML = false;
		boolean isAperioSVS = false;
		int dirNum = 0;
		for (TIFFImageFileDirectory ifd : ifdlist) {
			slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): Directory={}",dirNum);
			
			boolean downsampled = ifd.getSingleNumericValue(TIFFTags.SUBFILETYPE,0,0) == 1;	// Subfile Type: reduced-resolution image (1 = 0x1)	(001356)
			slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): based on Subfile Type: reduced-resolution image, downsampled = {}",downsampled);

			imageFlavorAndDerivationByIFD[dirNum] = new String[2];
			imageFlavorAndDerivationByIFD[dirNum][0] = "VOLUME";	// default flavor if not otherwise recognized
			imageFlavorAndDerivationByIFD[dirNum][1] = downsampled ? "RESAMPLED" : "NONE";		// default derivation if not otherwise recognized (001356)
			if (dirNum == 0) {
				String[] description = ifd.getStringValues(TIFFTags.IMAGEDESCRIPTION);
				if (description != null && description.length > 0) {
					slf4jlogger.trace("getImageFlavorAndDerivationByIFD(): description.length = {}",description.length);
					for (String d : description) {
						slf4jlogger.trace("getImageFlavorAndDerivationByIFD(): String = {}",d);
						// need to check OME and DPUfsImport first, since string "Aperio" may appear in either of those XML formats
						if (d.contains("<DataObject ObjectType=\"DPUfsImport\"")) {	// (001391) Philips DPUfsImport
							isDPUfsImport = true;
						}
						else if (d.contains("<OME")) {	// (001285) (001322) (001390) not just any XML (e.g., could be DPUfsImport)
							isOMETIFFXML = true;
						}
						else if (d.contains("Aperio")) {
							isAperioSVS = true;			// assume SVS but could be other Aperio format, theoretically ? :(
						}
					}
				}
				slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): isDPUfsImport={}",isDPUfsImport);
				slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): isOMETIFFXML={}",isOMETIFFXML);
				slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): isAperioSVS={}",isAperioSVS);
			}
			if (isDPUfsImport) {	// (001391) Philips DPUfsImport
				boolean isMacro = false;
				// don't know about label yet, if ever any
				{
					String[] description = ifd.getStringValues(TIFFTags.IMAGEDESCRIPTION);
					if (description != null && description.length > 0) {
						slf4jlogger.trace("getImageFlavorAndDerivationByIFD(): description.length = {}",description.length);
						for (String d : description) {
							slf4jlogger.trace("getImageFlavorAndDerivationByIFD(): String = {}",d);
							if (d.contains("Macro")) {		// sentence case "Macro" as sole value of ImageDescription offurs in Macro IFD as distinct from '<Attribute Name="PIM_DP_IMAGE_TYPE" Group="0x301D" Element="0x1004" PMSVR="IString">MACROIMAGE</Attribute>' in 1st IFD
								isMacro = true;
							}
							// don't know about label yet, if ever any
						}
					}
				}
				
				if (isMacro) {
					slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): is macro based on IMAGEDESCRIPTION so using OVERVIEW flavor");
					imageFlavorAndDerivationByIFD[dirNum][0] = "OVERVIEW";
					imageFlavorAndDerivationByIFD[dirNum][1] = "NONE";
				}
				// don't know about label yet, if ever any
				else {
					// unlike Aperio, where we use being tiled inter alia to distinguish the top of the pyramid, in Philips that is tiled with 1 tile and is last before macro
					// assume by order among IFDs - could actually check sizes :(
					imageFlavorAndDerivationByIFD[dirNum][0] = "VOLUME";
					if (dirNum == 0) {
						imageFlavorAndDerivationByIFD[dirNum][1] = "NONE";
					}
					else {
						imageFlavorAndDerivationByIFD[dirNum][1] = "RESAMPLED";
					}
				}
			}
			else if (isOMETIFFXML) {
				// do nothing special ... assume defaults are OK
			}
			else if (isAperioSVS) {
				// SVS layout per "MAN-0069_RevB_Digital_Slides_and_Third-party_data_interchange.pdf":
				//
				// "The first image in an SVS file is always the baseline image (full resolution). This image is always tiled, usually with a tile size of 240 x 240 pixels.
				// The second image is always a thumbnail, typically with dimensions of about 1024 x 768 pixels. Unlike the other slide images, the thumbnail image is always stripped.
				// Following the thumbnail there may be one or more intermediate “pyramid” images. These are always compressed with the same type of compression as the baseline image, and have a tiled organization with the same tile size.
				// Optionally at the end of an SVS file there may be a slide label image, which is a low resolution picture taken of the slide’s label,
				// and/or a macro camera image, which is a low resolution picture taken of the entire slide.
				// The label and macro images are always stripped. If present the label image is compressed with LZW compression, and the macro image with JPEG compression."
				//
				// "The intermediate resolution images in an SVS file are typically 1/4th resolution of the previous image"

				boolean isMacro = false;
				boolean isLabel = false;
				{
					String[] description = ifd.getStringValues(TIFFTags.IMAGEDESCRIPTION);
					if (description != null && description.length > 0) {
						slf4jlogger.trace("getImageFlavorAndDerivationByIFD(): description.length = {}",description.length);
						for (String d : description) {
							slf4jlogger.trace("getImageFlavorAndDerivationByIFD(): String = {}",d);
							if (d.contains("macro")) {
								isMacro = true;
							}
							else if (d.contains("label")) {
								isLabel = true;
							}
						}
					}
				}
				
				if (isMacro) {
					slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): is macro based on IMAGEDESCRIPTION so using OVERVIEW flavor");
					imageFlavorAndDerivationByIFD[dirNum][0] = "OVERVIEW";
					imageFlavorAndDerivationByIFD[dirNum][1] = "NONE";
				}
				else if (isLabel) {
					slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): is label based on IMAGEDESCRIPTION so using LABEL flavor");
					imageFlavorAndDerivationByIFD[dirNum][0] = "LABEL";
					imageFlavorAndDerivationByIFD[dirNum][1] = "NONE";
				}
				else {
					long[] tileOffsets = ifd.getNumericValues(TIFFTags.TILEOFFSETS);
					boolean isTiled = tileOffsets != null;
					
					if (isTiled) {
						slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): is tiled so using VOLUME flavor");
						imageFlavorAndDerivationByIFD[dirNum][0] = "VOLUME";
						if (dirNum == 0) {
							imageFlavorAndDerivationByIFD[dirNum][1] = "NONE";
						}
						else {
							imageFlavorAndDerivationByIFD[dirNum][1] = "RESAMPLED";		// (001258)
						}
					}
					else {
						if (dirNum == 1) {
							slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): is not tiled and 2nd IFD entry so using THUMBNAIL flavor");
							imageFlavorAndDerivationByIFD[dirNum][0] = "THUMBNAIL";		// CP 2102, to distinguish from VOLUME and OVERVIEW, not LOCALIZER (as Leica does, which has been retired)
							imageFlavorAndDerivationByIFD[dirNum][1] = "RESAMPLED";		// (001258)
						}
						else if (dirNum == (numberOfIFDs-1) || dirNum == (numberOfIFDs-2)) {
							long compression = ifd.getSingleNumericValue(TIFFTags.COMPRESSION,0,0);
							slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): compression={}",compression);
					
							if (compression == 5) {			// LZW
								slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): is not tiled and last or 2nd last IFD entry and LZW so using THUMBNAIL flavor");
								imageFlavorAndDerivationByIFD[dirNum][0] = "LABEL";
								imageFlavorAndDerivationByIFD[dirNum][1] = "NONE";
							}
							else if (compression == 7) {	// new JPEG
								slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): is not tiled and last or 2nd last IFD entry and JPEG so using OVERVIEW flavor");
								imageFlavorAndDerivationByIFD[dirNum][0] = "OVERVIEW";
								imageFlavorAndDerivationByIFD[dirNum][1] = "NONE";
							}
						}
					}
				}
			}
			slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): imageFlavorAndDerivationByIFD[{}] flavor={}",dirNum,imageFlavorAndDerivationByIFD[dirNum][0]);
			slf4jlogger.debug("getImageFlavorAndDerivationByIFD(): imageFlavorAndDerivationByIFD[{}] derivation={}",dirNum,imageFlavorAndDerivationByIFD[dirNum][1]);
			++dirNum;
		}
		return imageFlavorAndDerivationByIFD;
	}
	
	private String chooseTransferSyntaxForCompressionSchemeIfNotSpecifiedExplicitly(String transferSyntax,long compression) throws TIFFException {
		slf4jlogger.debug("chooseTransferSyntaxForCompressionSchemeIfNotSpecifiedExplicitly(): transferSyntax specified as = {}",transferSyntax);
		slf4jlogger.debug("chooseTransferSyntaxForCompressionSchemeIfNotSpecifiedExplicitly(): compression = {}",compression);
		if (transferSyntax == null || transferSyntax.length() == 0) {
			if (compression == 0 || compression == 1		// absent or specified as uncompressed
			 || compression == 5) {							// LZW will always be decompressed
				transferSyntax = TransferSyntax.ExplicitVRLittleEndian;
			}
			else if (compression == 7) {		// "new" JPEG per TTN2 as used by Aperio in SVS
				// really should check what is in there ... could be lossless, or 12 bit per TTN2 :(
				transferSyntax = TransferSyntax.JPEGBaseline;
			}
			else if (compression == 33003 || compression == 33005) {	// Aperio J2K YCbCr or RGB
				transferSyntax = TransferSyntax.JPEG2000;
			}
			else {
				throw new TIFFException("Unsupported compression = "+compression);
			}
		}
		slf4jlogger.debug("chooseTransferSyntaxForCompressionSchemeIfNotSpecifiedExplicitly(): transferSyntax now = {}",transferSyntax);
		return transferSyntax;
	}

	private String chooseRecompressAsFormatFromTransferSyntax(String transferSyntax) throws TIFFException {
		//slf4jlogger.debug("chooseTransferSyntaxForCompressionSchemeIfNotSpecifiedExplicitly(): transferSyntax = {}",transferSyntax);
		String recompressAsFormat = null;
		if (transferSyntax != null && transferSyntax.length() > 0) {
			recompressAsFormat = CompressedFrameEncoder.chooseOutputFormatForTransferSyntax(transferSyntax);
		}
		slf4jlogger.debug("chooseRecompressAsFormatFromTransferSyntax(): recompressAsFormat = {}",recompressAsFormat);
		return recompressAsFormat;
	}

	private long chooseOutputCompressionForRecompressAsFormatGivenInputCompression(String recompressAsFormat,long compression) throws TIFFException {
		slf4jlogger.debug("chooseOutputCompressionForRecompressAsFormatGivenInputCompression(): recompressAsFormat = {}",recompressAsFormat);
		slf4jlogger.debug("chooseOutputCompressionForRecompressAsFormatGivenInputCompression(): compression = {}",compression);
		long outputCompression = compression;	// default is same as input
		if (recompressAsFormat == null || recompressAsFormat.length() == 0) {
			outputCompression = 1;
		}
		else {
			if (recompressAsFormat.equals("jpeg")) {
				outputCompression = 7;
			}
			else if (recompressAsFormat.equals("jpeg2000")) {
				if (compression != 33003 && compression != 33005) {
					outputCompression = 33003;		// if recompressing, and need to choose something
				}
			}
		}
		slf4jlogger.debug("chooseOutputCompressionForRecompressAsFormatGivenInputCompression(): outputCompression = {}",outputCompression);
		return outputCompression;
	}

	private void createOrAppendToManufacturerModelNameAndInsertOrReplace(AttributeList list) throws DicomException {
		String manufacturerModelName = Attribute.getSingleStringValueOrEmptyString(list,TagFromName.ManufacturerModelName);
		if (manufacturerModelName.length() > 0) {
			manufacturerModelName = manufacturerModelName + " converted by ";
		}
		manufacturerModelName = manufacturerModelName + this.getClass().getName();
		{ Attribute a = new LongStringAttribute(TagFromName.ManufacturerModelName); a.addValue(manufacturerModelName); list.put(a); }
	}

	private void createOrAppendToSoftwareVersionsAndInsertOrReplace(AttributeList list) throws DicomException {
		Attribute a = list.get(TagFromName.SoftwareVersions);
		if (a == null) {
			a = new LongStringAttribute(TagFromName.SoftwareVersions);
			list.put(a);
		}
		if (!a.getDelimitedStringValuesOrEmptyString().contains(VersionAndConstants.getBuildDate())) {	// do not keep adding it if already present !
			a.addValue(VersionAndConstants.getBuildDate());
		}
	}

	private void addContributingEquipmentSequence(AttributeList list) throws DicomException {
		ClinicalTrialsAttributes.addContributingEquipmentSequence(list,true,new CodedSequenceItem("109103","DCM","Modifying Equipment"),
																  "PixelMed",													// Manufacturer
																  "PixelMed",													// Institution Name
																  "Software Development",										// Institutional Department Name
																  "Bangor, PA",													// Institution Address
																  null,															// Station Name
																  this.getClass().getName(),									// Manufacturer's Model Name
																  null,															// Device Serial Number
																  "Vers. "+VersionAndConstants.getBuildDate(),					// Software Version(s)
																  "TIFF to DICOM conversion");
	}

	// (001345)
	private void addSpecimenPreparationStepContentItemSequence(SequenceAttribute specimenPreparationStepContentItemSequence,String specimenIdentifier,AttributeList list) throws DicomException {
		// only now add channel-specific SpecimenPreparationStepContentItemSequence from specimenPreparationStepContentItemSequenceByChannelID merging specimenIdentifier into it
		// SpecimenDescriptionSequence.SpecimenPreparationSequence.SpecimenPreparationStepContentItemSequence
		if (specimenPreparationStepContentItemSequence != null) {
			SequenceAttribute specimenPreparationSequence = (SequenceAttribute)SequenceAttribute.getNamedAttributeFromWithinSequenceWithSingleItem(list,
				TagFromName.SpecimenDescriptionSequence,
				DicomDictionary.StandardDictionary.getTagFromName("SpecimenPreparationSequence"));
			if (specimenPreparationSequence != null) {
				slf4jlogger.debug("addSpecimenPreparationStepContentItemSequence(): Merging in channel-specific SpecimenPreparationStepContentItemSequence information");
				// slide-specific SpecimenIdentifier may have been overridden by SetCharacteristicsFromSummary - if so, use it (001346) - argument from caller was probably default anyway :(
				String useSpecimenIdentifier = SequenceAttribute.getSingleStringValueOfNamedAttributeFromWithinSequenceWithSingleItemOrDefault(list,TagFromName.SpecimenDescriptionSequence,TagFromName.SpecimenIdentifier,specimenIdentifier);
				slf4jlogger.debug("addSpecimenPreparationStepContentItemSequence(): Using slide-specific SpecimenIdentifier {}",useSpecimenIdentifier);
				// add specimenIdentifier as first entry in SpecimenPreparationStepContentItemSequence
				SequenceAttribute useSpecimenPreparationStepContentItemSequence = new SequenceAttribute(DicomDictionary.StandardDictionary.getTagFromName("SpecimenPreparationStepContentItemSequence"));
				{
					AttributeList itemList = new AttributeList();
					{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("TEXT"); itemList.put(a); }
					CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"121041","DCM","Specimen Identifier");
					{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(useSpecimenIdentifier); itemList.put(a); }
					useSpecimenPreparationStepContentItemSequence.addItem(itemList);
				}
				{
					Iterator<SequenceItem> sitems = specimenPreparationStepContentItemSequence.iterator();
					while (sitems.hasNext()) {
						useSpecimenPreparationStepContentItemSequence.addItem(sitems.next());
					}
				}
				// add SpecimenPreparationStepContentItemSequence in new item of existing SpecimenPreparationSequence
				{
					AttributeList itemList = new AttributeList();
					itemList.put(useSpecimenPreparationStepContentItemSequence);
					specimenPreparationSequence.addItem(itemList);
				}
			}
			else {
				slf4jlogger.error("addSpecimenPreparationStepContentItemSequence(): Could not get SpecimenPreparationSequence into which to merge supplied channel-specific SpecimenPreparationStepContentItemSequence");
			}
		}
	}

	private void convertTIFFPixelsToDicomMultiFrame(String jsonfile,UIDMap uidMap,TIFFFile inputFile,int dirNum,String outputFileName,int instanceNumber,
				long imageWidth,long imageLength,
				long[] tileOffsets,long[] tileByteCounts,long tileWidth,long tileLength,
				long bitsPerSample,long compression,byte[] jpegTables,byte[] iccProfile,long photometric,long samplesPerPixel,long planarConfig,long sampleFormat,long predictor,
				String frameOfReferenceUID,double mmPerPixelX,double mmPerPixelY,double sliceThickness,double objectiveLensPower,double objectiveLensNumericalAperture,
				String opticalPathIdentifier,String opticalPathDescription,AttributeList opticalPathAttributesForChannel,
				double xOffsetInSlideCoordinateSystem,double yOffsetInSlideCoordinateSystem,
				String modality,String sopClass,String transferSyntax,
				String containerIdentifier,String specimenIdentifier,String specimenUID,
				String imageFlavor,String imageDerivation,String imageDescription,AttributeList descriptionList,
				boolean addTIFF,boolean useBigTIFF,boolean alwaysWSI,boolean addPyramid,
				SequenceAttribute specimenPreparationStepContentItemSequence,
				String pyramidUID,String acquisitionUID,
				boolean includeCopyOfImageDescription,
				String pastHistoryOfLossyCompression) throws IOException, DicomException, TIFFException {

		slf4jlogger.debug("convertTIFFPixelsToDicomMultiFrame(): instanceNumber = {}",instanceNumber);
		slf4jlogger.debug("convertTIFFPixelsToDicomMultiFrame(): transferSyntax supplied = {}",transferSyntax);
		slf4jlogger.debug("convertTIFFPixelsToDicomMultiFrame(): photometric in TIFF file = {}",photometric);

		transferSyntax = chooseTransferSyntaxForCompressionSchemeIfNotSpecifiedExplicitly(transferSyntax,compression);
		String recompressAsFormat = chooseRecompressAsFormatFromTransferSyntax(transferSyntax);
		boolean recompressLossy = new TransferSyntax(transferSyntax).isLossy();
		slf4jlogger.debug("convertTIFFPixelsToDicomMultiFrame(): recompressLossy = {}",recompressLossy);

		AttributeList list = new AttributeList();
		
		// numberOfSourceTiles will == number of DICOM tiles for planarConfig 1 (color-by-pixel) and be samplesPerPixel * number of DICOM tiles for planarConfig 2 (color-by-plane) (TIFF6 p 68) (001351)
		boolean mergeSamplesPerPixelTiles = planarConfig == 2 && samplesPerPixel > 1;
		slf4jlogger.debug("convertTIFFPixelsToDicomMultiFrame(): mergeSamplesPerPixelTiles = {}",mergeSamplesPerPixelTiles);
		if (mergeSamplesPerPixelTiles && !(compression == 0 || compression == 1 || compression == 5)) {
			throw new DicomException("Merging of separately compressed color planes not supported");	// (001353)
		}
		
		int numberOfSourceTiles = tileOffsets.length;	// this is the number of TIFF tiles
		slf4jlogger.debug("convertTIFFPixelsToDicomMultiFrame(): numberOfSourceTiles = {}",numberOfSourceTiles);
		int numberOfDestinationTiles = (int)(mergeSamplesPerPixelTiles ? numberOfSourceTiles/samplesPerPixel : numberOfSourceTiles);
		slf4jlogger.debug("convertTIFFPixelsToDicomMultiFrame(): numberOfDestinationTiles = {}",numberOfDestinationTiles);
		
		boolean alwaysMakeColorByPixelPlanarConfig = alwaysWSI || SOPClass.VLWholeSlideMicroscopyImageStorage.equals(sopClass);	// (001351)
		long outputPlanarConfig = alwaysMakeColorByPixelPlanarConfig ? 1 : planarConfig;
		long outputPhotometric = generateDICOMPixelDataMultiFrameImageFromTIFFFile(inputFile,list,mergeSamplesPerPixelTiles,alwaysMakeColorByPixelPlanarConfig,numberOfSourceTiles,numberOfDestinationTiles,tileOffsets,tileByteCounts,tileWidth,tileLength,bitsPerSample,sampleFormat,compression,photometric,samplesPerPixel,planarConfig,predictor,jpegTables,iccProfile,recompressAsFormat,recompressLossy);
		long outputCompression = chooseOutputCompressionForRecompressAsFormatGivenInputCompression(recompressAsFormat,compression);

		slf4jlogger.debug("convertTIFFPixelsToDicomMultiFrame(): planarConfig {}changed from {} to {}",(planarConfig == outputPlanarConfig ? "un" : ""),planarConfig,outputPlanarConfig);
		slf4jlogger.debug("convertTIFFPixelsToDicomMultiFrame(): photometric {}changed from {} to {}",(photometric == outputPhotometric ? "un" : ""),photometric,outputPhotometric);
		slf4jlogger.debug("convertTIFFPixelsToDicomMultiFrame(): compression {}changed from {} to {}",(compression == outputCompression ? "un" : ""),compression,outputCompression);

		generateDICOMPixelDataModuleAttributes(list,numberOfDestinationTiles,tileWidth,tileLength,bitsPerSample,outputCompression,outputPhotometric,samplesPerPixel,outputPlanarConfig,sampleFormat,recompressAsFormat,recompressLossy,sopClass);
		
		CommonConvertedAttributeGeneration.generateCommonAttributes(list,""/*patientName*/,""/*patientID*/,""/*studyID*/,""/*seriesNumber*/,Integer.toString(instanceNumber),modality,sopClass,false/*generateUnassignedConverted*/);
		list.remove(TagFromName.SoftwareVersions);		// will set later - do not want default from CommonConvertedAttributeGeneration.generateCommonAttributes
		list.remove(TagFromName.DeviceSerialNumber);	// will be overridden by descriptionList +/- generated value - do not want default from CommonConvertedAttributeGeneration.generateCommonAttributes (001398)
		
		if (alwaysWSI || SOPClass.VLWholeSlideMicroscopyImageStorage.equals(sopClass)) {	// (001321)
			generateDICOMWholeSlideMicroscopyImageAttributes(list,imageWidth,imageLength,frameOfReferenceUID,mmPerPixelX,mmPerPixelY,sliceThickness,objectiveLensPower,objectiveLensNumericalAperture,opticalPathIdentifier,opticalPathDescription,xOffsetInSlideCoordinateSystem,yOffsetInSlideCoordinateSystem,containerIdentifier,specimenIdentifier,specimenUID,imageFlavor,imageDerivation,pyramidUID,acquisitionUID);
		}

		insertLossyImageCompressionHistory(list,compression,outputCompression,recompressLossy,pastHistoryOfLossyCompression,totalArrayValues(tileByteCounts),imageWidth,imageLength,bitsPerSample,samplesPerPixel);

		if (includeCopyOfImageDescription && imageDescription != null && imageDescription.length() > 0) {	// (001347)
			if (imageDescription.length() <= 10240) {	// (001281)
				{ Attribute a = new LongTextAttribute(TagFromName.ImageComments); a.addValue(imageDescription); list.put(a); }
			}
			else {
				{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(imageDescription); list.put(a); }
			}
		}

		if (descriptionList != null) {
			// override such things as Manufacturer, DeviceSerialNumber, UIDs, dates, times, TotalPixelMatrixOriginSequence, if they were obtained from the ImageDescription TIFF tag or need to be common
			list.putAll(descriptionList);
		}

		createOrAppendToManufacturerModelNameAndInsertOrReplace(list);
		createOrAppendToSoftwareVersionsAndInsertOrReplace(list);

		addContributingEquipmentSequence(list);

		new SetCharacteristicsFromSummary(jsonfile,list);
		
		addSpecimenPreparationStepContentItemSequence(specimenPreparationStepContentItemSequence,specimenIdentifier,list);	// (001345)
		
		if (uidMap != null) {
			uidMap.replaceUIDs(list,inputFile.getFileName(),dirNum);	// (001360)
		}

		// only now add ICC profile, so as not be overriden by any OpticalPathSequence in SetCharacteristicsFromSummary
		if (alwaysWSI || SOPClass.VLWholeSlideMicroscopyImageStorage.equals(sopClass)) {	// (001321)
			if (samplesPerPixel > 1) {	// (001229)
				iccProfile = addICCProfileToOpticalPathSequence(list,iccProfile);	// adds known or default, since required
			}
		}
		else if (iccProfile != null && iccProfile.length > 0) {		// add known
			{ Attribute a = new OtherByteAttribute(TagFromName.ICCProfile); a.setValues(iccProfile); list.put(a); }
			slf4jlogger.debug("convertTIFFPixelsToDicomMultiFrame(): Created ICC Profile attribute of length {}",iccProfile.length);
		}
		// else do not add default ICC Profile

		// only now add ObjectiveLensPower and ObjectiveLensNumericalAperture, so as not to be overriden by any OpticalPathSequence if present without ObjectiveLensPower and ObjectiveLensNumericalAperture in any addition from SetCharacteristicsFromSummary (001270)
		if (alwaysWSI || SOPClass.VLWholeSlideMicroscopyImageStorage.equals(sopClass)) {	// (001321)
			addObjectiveLensPowerToOpticalPathSequence(list,objectiveLensPower);
			addObjectiveLensNumericalApertureToOpticalPathSequence(list,objectiveLensNumericalAperture);	// (001319)
			addOpticalPathIdentifierAndDescriptionToOpticalPathSequence(list,opticalPathIdentifier,opticalPathDescription);	// (001285)
			addOpticalPathAttributesForChannel(list,opticalPathAttributesForChannel);
		}

		CodingSchemeIdentification.replaceCodingSchemeIdentificationSequenceWithCodingSchemesUsedInAttributeList(list);
		
		list.insertSuitableSpecificCharacterSetForAllStringValues();	// (001158)

		FileMetaInformation.addFileMetaInformation(list,transferSyntax,"OURAETITLE");
		
		int numberOfPyramidLevels = 1;	// at a minimum the base layer in top level data set PixelData

		//boolean addPyramid = alwaysWSI || SOPClass.VLWholeSlideMicroscopyImageStorage.equals(sopClass);
		//boolean addPyramid = false;

		if (addPyramid) {
			try {
				numberOfPyramidLevels = generateDICOMPyramidPixelDataModule(list,recompressAsFormat,transferSyntax);	// will use existing PixelData attribute contents ... need to do this after TransferSyntax is set in FileMetaInformation
			}
			catch (DicomException e) {
				e.printStackTrace(System.err);
			}
		}

		byte[] preamble = null;
		
		if (addTIFF) {
			long lowerPhotometric =	// what to use when/if making lower pyramidal levels
				transferSyntax.equals(TransferSyntax.JPEGBaseline) || transferSyntax.equals(TransferSyntax.JPEG2000)
				? 6				// YCbCr, since that is what the codec will do regardless, i.e., be consistent with what TiledPyramid.createDownsampledDICOMAttributes() does
				: photometric;	// leave it the same unless we are recompressing it; is independent of whatever outputPhotometric happens to be
			slf4jlogger.debug("convertTIFFPixelsToDicomMultiFrame(): lowerPhotometric = {}",lowerPhotometric);

			try {
				long[][] tileDataByteOffsets = new long[numberOfPyramidLevels][];
				long[][] tileDataLengths = new long[numberOfPyramidLevels][];
				long[] imageWidths = new long[numberOfPyramidLevels];
				long[] imageLengths = new long[numberOfPyramidLevels];
				long  byteOffsetFromFileStartOfNextAttributeAfterPixelData = AddTIFFOrOffsetTables.getByteOffsetsAndLengthsOfTileDataFromStartOfFile(list,transferSyntax,tileDataByteOffsets,tileDataLengths,imageWidths,imageLengths,mergeSamplesPerPixelTiles,samplesPerPixel);
				preamble = AddTIFFOrOffsetTables.makeTIFFInPreambleAndAddDataSetTrailingPadding(byteOffsetFromFileStartOfNextAttributeAfterPixelData,numberOfPyramidLevels,tileDataByteOffsets,tileDataLengths,imageWidths,imageLengths,list,
					tileWidth,tileLength,bitsPerSample,outputCompression,outputPhotometric,lowerPhotometric,samplesPerPixel,outputPlanarConfig,sampleFormat,iccProfile,mmPerPixelX,mmPerPixelY,useBigTIFF);
			}
			catch (DicomException e) {
				e.printStackTrace(System.err);
			}
		}
		
		assertPixelDataEncodingIsCompatibleWithTransferSyntax(list,transferSyntax);
		list.write(outputFileName,transferSyntax,true,true,preamble);
		
		if (filesToDeleteAfterWritingDicomFile != null) {
			for (File tmpFile : filesToDeleteAfterWritingDicomFile) {
				tmpFile.delete();
			}
			filesToDeleteAfterWritingDicomFile = null;
		}
	}

	private void convertTIFFPixelsToDicomSingleFrameMergingStrips(String jsonfile,UIDMap uidMap,TIFFFile inputFile,int dirNum,String outputFileName,int instanceNumber,
				long imageWidth,long imageLength,
				long[] pixelOffset,long[] pixelByteCount,long pixelWidth,long rowsPerStrip,
				long bitsPerSample,long compression,
				byte[] jpegTables,byte[] iccProfile,long photometric,long samplesPerPixel,long planarConfig,long sampleFormat,long predictor,
				String frameOfReferenceUID,double mmPerPixelX,double mmPerPixelY,double sliceThickness,double objectiveLensPower,double objectiveLensNumericalAperture,
				String opticalPathIdentifier,String opticalPathDescription,AttributeList opticalPathAttributesForChannel,
				double xOffsetInSlideCoordinateSystem,double yOffsetInSlideCoordinateSystem,
				String modality,String sopClass,String transferSyntax,
				String containerIdentifier,String specimenIdentifier,String specimenUID,
				String imageFlavor,String imageDerivation,String imageDescription,AttributeList descriptionList,
				boolean addTIFF,boolean useBigTIFF,boolean alwaysWSI,
				SequenceAttribute specimenPreparationStepContentItemSequence,
				String pyramidUID,String acquisitionUID,
				boolean includeCopyOfImageDescription,
				String pastHistoryOfLossyCompression) throws IOException, DicomException, TIFFException {

		slf4jlogger.debug("convertTIFFPixelsToDicomSingleFrameMergingStrips(): instanceNumber = {}",instanceNumber);

		transferSyntax = chooseTransferSyntaxForCompressionSchemeIfNotSpecifiedExplicitly(transferSyntax,compression);
		String recompressAsFormat = chooseRecompressAsFormatFromTransferSyntax(transferSyntax);
		boolean recompressLossy = new TransferSyntax(transferSyntax).isLossy();
		slf4jlogger.debug("convertTIFFPixelsToDicomSingleFrameMergingStrips(): recompressLossy = {}",recompressLossy);

		AttributeList list = new AttributeList();
		
		boolean alwaysMakeColorByPixelPlanarConfig = alwaysWSI || SOPClass.VLWholeSlideMicroscopyImageStorage.equals(sopClass);	// (001351)
		long outputPlanarConfig = alwaysMakeColorByPixelPlanarConfig ? 1 : planarConfig;
		long outputPhotometric = generateDICOMPixelDataSingleFrameImageFromTIFFFileMergingStrips(inputFile,list,alwaysMakeColorByPixelPlanarConfig,imageWidth,imageLength,pixelOffset,pixelByteCount,pixelWidth,rowsPerStrip,bitsPerSample,sampleFormat,compression,photometric,samplesPerPixel,planarConfig,predictor,jpegTables,iccProfile,recompressAsFormat,recompressLossy);
		long outputCompression = chooseOutputCompressionForRecompressAsFormatGivenInputCompression(recompressAsFormat,compression);

		slf4jlogger.debug("convertTIFFPixelsToDicomSingleFrameMergingStrips(): planarConfig {}changed from {} to {}",(planarConfig == outputPlanarConfig ? "un" : ""),planarConfig,outputPlanarConfig);
		slf4jlogger.debug("convertTIFFPixelsToDicomSingleFrameMergingStrips(): photometric {}changed from {} to {}",(photometric == outputPhotometric ? "un" : ""),photometric,outputPhotometric);
		slf4jlogger.debug("convertTIFFPixelsToDicomSingleFrameMergingStrips(): compression {}changed from {} to {}",(compression == outputCompression ? "un" : ""),compression,outputCompression);

		generateDICOMPixelDataModuleAttributes(list,1/*numberOfFrames*/,imageWidth,imageLength,bitsPerSample,outputCompression,outputPhotometric,samplesPerPixel,outputPlanarConfig,sampleFormat,recompressAsFormat,recompressLossy,sopClass);

		CommonConvertedAttributeGeneration.generateCommonAttributes(list,""/*patientName*/,""/*patientID*/,""/*studyID*/,""/*seriesNumber*/,Integer.toString(instanceNumber),modality,sopClass,false/*generateUnassignedConverted*/);
		list.remove(TagFromName.SoftwareVersions);		// will set later - do not want default from CommonConvertedAttributeGeneration.generateCommonAttributes
		list.remove(TagFromName.DeviceSerialNumber);	// will be overridden by descriptionList +/- generated value - do not want default from CommonConvertedAttributeGeneration.generateCommonAttributes (001398)

		if (alwaysWSI || SOPClass.VLWholeSlideMicroscopyImageStorage.equals(sopClass)) {
			generateDICOMWholeSlideMicroscopyImageAttributes(list,imageWidth,imageLength,frameOfReferenceUID,mmPerPixelX,mmPerPixelY,sliceThickness,objectiveLensPower,objectiveLensNumericalAperture,opticalPathIdentifier,opticalPathDescription,xOffsetInSlideCoordinateSystem,yOffsetInSlideCoordinateSystem,containerIdentifier,specimenIdentifier,specimenUID,imageFlavor,imageDerivation,pyramidUID,acquisitionUID);
		}

		insertLossyImageCompressionHistory(list,compression,outputCompression,recompressLossy,pastHistoryOfLossyCompression,totalArrayValues(pixelByteCount),imageWidth,imageLength,bitsPerSample,samplesPerPixel);

		if (includeCopyOfImageDescription && imageDescription != null && imageDescription.length() > 0) {	// (001347)
			if (imageDescription.length() <= 10240) {	// (001281)
				{ Attribute a = new LongTextAttribute(TagFromName.ImageComments); a.addValue(imageDescription); list.put(a); }
			}
			else {
				{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(imageDescription); list.put(a); }
			}
		}

		if (descriptionList != null) {
			// override such things as Manufacturer, DeviceSerialNumber, UIDs, dates, times, TotalPixelMatrixOriginSequence, if they were obtained from the ImageDescription TIFF tag or need to be common
			list.putAll(descriptionList);
		}

		createOrAppendToManufacturerModelNameAndInsertOrReplace(list);
		createOrAppendToSoftwareVersionsAndInsertOrReplace(list);
		
		addContributingEquipmentSequence(list);

		new SetCharacteristicsFromSummary(jsonfile,list);
		
		addSpecimenPreparationStepContentItemSequence(specimenPreparationStepContentItemSequence,specimenIdentifier,list);	// (001345)
		
		if (uidMap != null) {
			uidMap.replaceUIDs(list,inputFile.getFileName(),dirNum);	// (001360)
		}

		// only now add ICC profile, so as not be overriden by any OpticalPathSequence in SetCharacteristicsFromSummary
		if (alwaysWSI || SOPClass.VLWholeSlideMicroscopyImageStorage.equals(sopClass)) {	// (001321)
			if (samplesPerPixel > 1) {	// (001229)
				iccProfile = addICCProfileToOpticalPathSequence(list,iccProfile);	// adds known or default, since required
			}
		}
		else if (iccProfile != null && iccProfile.length > 0) {		// add known
			{ Attribute a = new OtherByteAttribute(TagFromName.ICCProfile); a.setValues(iccProfile); list.put(a); }
			slf4jlogger.debug("convertTIFFPixelsToDicomSingleFrameMergingStrips(): Created ICC Profile attribute of length {}",iccProfile.length);
		}
		// else do not add default ICC Profile

		// only now add ObjectiveLensPower and ObjectiveLensNumericalAperture, so as not to be overriden by any OpticalPathSequence if present without ObjectiveLensPower and ObjectiveLensNumericalAperture in any addition from SetCharacteristicsFromSummary (001270)
		if (alwaysWSI || SOPClass.VLWholeSlideMicroscopyImageStorage.equals(sopClass)) {	// (001321)
			addObjectiveLensPowerToOpticalPathSequence(list,objectiveLensPower);
			addObjectiveLensNumericalApertureToOpticalPathSequence(list,objectiveLensNumericalAperture);	// (001319)
			addOpticalPathIdentifierAndDescriptionToOpticalPathSequence(list,opticalPathIdentifier,opticalPathDescription);	// (001285)
			addOpticalPathAttributesForChannel(list,opticalPathAttributesForChannel);
		}

		CodingSchemeIdentification.replaceCodingSchemeIdentificationSequenceWithCodingSchemesUsedInAttributeList(list);

		list.insertSuitableSpecificCharacterSetForAllStringValues();

		FileMetaInformation.addFileMetaInformation(list,transferSyntax,"OURAETITLE");

		byte[] preamble = null;
		
		if (addTIFF) {
			try {
				// no pyramids
				long[][] tileDataByteOffsets = new long[1][];
				long[][] tileDataLengths = new long[1][];
				long[] imageWidths = new long[1];
				long[] imageLengths = new long[1];
				long  byteOffsetFromFileStartOfNextAttributeAfterPixelData = AddTIFFOrOffsetTables.getByteOffsetsAndLengthsOfTileDataFromStartOfFile(list,transferSyntax,tileDataByteOffsets,tileDataLengths,imageWidths,imageLengths,false/*mergeSamplesPerPixelTiles*/,samplesPerPixel);
				preamble = AddTIFFOrOffsetTables.makeTIFFInPreambleAndAddDataSetTrailingPadding(byteOffsetFromFileStartOfNextAttributeAfterPixelData,1/*numberOfPyramidLevels*/,tileDataByteOffsets,tileDataLengths,imageWidths,imageLengths,list,
					imageWidth,imageLength,bitsPerSample,outputCompression,outputPhotometric,outputPhotometric/*lowerPhotometric*/,samplesPerPixel,outputPlanarConfig,sampleFormat,iccProfile,mmPerPixelX,mmPerPixelY,useBigTIFF);
			}
			catch (DicomException e) {
				e.printStackTrace(System.err);
			}
		}
		
		assertPixelDataEncodingIsCompatibleWithTransferSyntax(list,transferSyntax);
		list.write(outputFileName,transferSyntax,true,true,preamble);

		if (filesToDeleteAfterWritingDicomFile != null) {
			for (File tmpFile : filesToDeleteAfterWritingDicomFile) {
				tmpFile.delete();
			}
			filesToDeleteAfterWritingDicomFile = null;
		}
	}
	
	private void convertTIFFPixelsToDicomSingleFrame(String jsonfile,UIDMap uidMap,TIFFFile inputFile,int dirNum,String outputFileName,int instanceNumber,
				long imageWidth,long imageLength,
				long pixelOffset,long pixelByteCount,long pixelWidth,long pixelLength,
				long bitsPerSample,long compression,byte[] jpegTables,byte[] iccProfile,long photometric,long samplesPerPixel,long planarConfig,long sampleFormat,long predictor,
				String frameOfReferenceUID,double mmPerPixelX,double mmPerPixelY,double sliceThickness,double objectiveLensPower,double objectiveLensNumericalAperture,
				String opticalPathIdentifier,String opticalPathDescription,AttributeList opticalPathAttributesForChannel,
				double xOffsetInSlideCoordinateSystem,double yOffsetInSlideCoordinateSystem,
				String modality,String sopClass,String transferSyntax,
				String containerIdentifier,String specimenIdentifier,String specimenUID,
				String imageFlavor,String imageDerivation,String imageDescription,AttributeList descriptionList,
				boolean addTIFF,boolean useBigTIFF,boolean alwaysWSI,
				SequenceAttribute specimenPreparationStepContentItemSequence,
				String pyramidUID,String acquisitionUID,
				boolean includeCopyOfImageDescription,
				String pastHistoryOfLossyCompression) throws IOException, DicomException, TIFFException {

		slf4jlogger.debug("convertTIFFPixelsToDicomSingleFrame(): instanceNumber = {}",instanceNumber);

		transferSyntax = chooseTransferSyntaxForCompressionSchemeIfNotSpecifiedExplicitly(transferSyntax,compression);
		String recompressAsFormat = chooseRecompressAsFormatFromTransferSyntax(transferSyntax);
		boolean recompressLossy = new TransferSyntax(transferSyntax).isLossy();
		slf4jlogger.debug("convertTIFFPixelsToDicomMultiFrame(): recompressLossy = {}",recompressLossy);

		AttributeList list = new AttributeList();
		
		long outputPhotometric = generateDICOMPixelDataSingleFrameImageFromTIFFFile(inputFile,list,pixelOffset,pixelByteCount,pixelWidth,pixelLength,bitsPerSample,sampleFormat,compression,photometric,samplesPerPixel,jpegTables,iccProfile,recompressAsFormat,recompressLossy);
		long outputCompression = chooseOutputCompressionForRecompressAsFormatGivenInputCompression(recompressAsFormat,compression);

		slf4jlogger.debug("convertTIFFPixelsToDicomSingleFrame(): photometric {}changed from {} to {}",(photometric == outputPhotometric ? "un" : ""),photometric,outputPhotometric);
		slf4jlogger.debug("convertTIFFPixelsToDicomSingleFrame(): compression {}changed from {} to {}",(compression == outputCompression ? "un" : ""),compression,outputCompression);

		generateDICOMPixelDataModuleAttributes(list,1/*numberOfFrames*/,pixelWidth,pixelLength,bitsPerSample,outputCompression,outputPhotometric,samplesPerPixel,planarConfig,sampleFormat,recompressAsFormat,recompressLossy,sopClass);

		CommonConvertedAttributeGeneration.generateCommonAttributes(list,""/*patientName*/,""/*patientID*/,""/*studyID*/,""/*seriesNumber*/,Integer.toString(instanceNumber),modality,sopClass,false/*generateUnassignedConverted*/);
		list.remove(TagFromName.SoftwareVersions);		// will set later - do not want default from CommonConvertedAttributeGeneration.generateCommonAttributes
		list.remove(TagFromName.DeviceSerialNumber);	// will be overridden by descriptionList +/- generated value - do not want default from CommonConvertedAttributeGeneration.generateCommonAttributes (001398)

		if (alwaysWSI || SOPClass.VLWholeSlideMicroscopyImageStorage.equals(sopClass)) {
			generateDICOMWholeSlideMicroscopyImageAttributes(list,imageWidth,imageLength,frameOfReferenceUID,mmPerPixelX,mmPerPixelY,sliceThickness,objectiveLensPower,objectiveLensNumericalAperture,opticalPathIdentifier,opticalPathDescription,xOffsetInSlideCoordinateSystem,yOffsetInSlideCoordinateSystem,containerIdentifier,specimenIdentifier,specimenUID,imageFlavor,imageDerivation,pyramidUID,acquisitionUID);
		}

		insertLossyImageCompressionHistory(list,compression,outputCompression,recompressLossy,pastHistoryOfLossyCompression,pixelByteCount,imageWidth,imageLength,bitsPerSample,samplesPerPixel);

		if (includeCopyOfImageDescription && imageDescription != null && imageDescription.length() > 0) {	// (001347)
			if (imageDescription.length() <= 10240) {	// (001281)
				{ Attribute a = new LongTextAttribute(TagFromName.ImageComments); a.addValue(imageDescription); list.put(a); }
			}
			else {
				{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(imageDescription); list.put(a); }
			}
		}

		if (descriptionList != null) {
			// override such things as Manufacturer, DeviceSerialNumber, UIDs, dates, times, TotalPixelMatrixOriginSequence, if they were obtained from the ImageDescription TIFF tag or need to be common
			list.putAll(descriptionList);
		}

		createOrAppendToManufacturerModelNameAndInsertOrReplace(list);
		createOrAppendToSoftwareVersionsAndInsertOrReplace(list);

		addContributingEquipmentSequence(list);
		
		new SetCharacteristicsFromSummary(jsonfile,list);
		
		addSpecimenPreparationStepContentItemSequence(specimenPreparationStepContentItemSequence,specimenIdentifier,list);	// (001345)
		
		if (uidMap != null) {
			uidMap.replaceUIDs(list,inputFile.getFileName(),dirNum);	// (001360)
		}

		// only now add ICC profile, so as not be overriden by any OpticalPathSequence in SetCharacteristicsFromSummary
		if (alwaysWSI || SOPClass.VLWholeSlideMicroscopyImageStorage.equals(sopClass)) {	// (001321)
			if (samplesPerPixel > 1) {	// (001229)
				iccProfile = addICCProfileToOpticalPathSequence(list,iccProfile);	// adds known or default, since required
			}
		}
		else if (iccProfile != null && iccProfile.length > 0) {		// add known
			{ Attribute a = new OtherByteAttribute(TagFromName.ICCProfile); a.setValues(iccProfile); list.put(a); }
			slf4jlogger.debug("convertTIFFPixelsToDicomSingleFrameMergingStrips(): Created ICC Profile attribute of length {}",iccProfile.length);
		}
		// else do not add default ICC Profile

		// only now add ObjectiveLensPower and ObjectiveLensNumericalAperture, so as not to be overriden by any OpticalPathSequence if present without ObjectiveLensPower and ObjectiveLensNumericalAperture in any addition from SetCharacteristicsFromSummary (001270)
		if (alwaysWSI || SOPClass.VLWholeSlideMicroscopyImageStorage.equals(sopClass)) {	// (001321)
			addObjectiveLensPowerToOpticalPathSequence(list,objectiveLensPower);
			addObjectiveLensNumericalApertureToOpticalPathSequence(list,objectiveLensNumericalAperture);	// (001319)
			addOpticalPathIdentifierAndDescriptionToOpticalPathSequence(list,opticalPathIdentifier,opticalPathDescription);	// (001285)
			addOpticalPathAttributesForChannel(list,opticalPathAttributesForChannel);
		}

		CodingSchemeIdentification.replaceCodingSchemeIdentificationSequenceWithCodingSchemesUsedInAttributeList(list);

		list.insertSuitableSpecificCharacterSetForAllStringValues();

		FileMetaInformation.addFileMetaInformation(list,transferSyntax,"OURAETITLE");

		byte[] preamble = null;
		
		//if (addTIFF) slf4jlogger.warn("convertTIFFPixelsToDicomSingleFrame(): Adding TIFF not yet implemented for single frame conversion");
		if (addTIFF) {
			try {
				// no pyramids
				long[][] tileDataByteOffsets = new long[1][];
				long[][] tileDataLengths = new long[1][];
				long[] imageWidths = new long[1];
				long[] imageLengths = new long[1];
				long  byteOffsetFromFileStartOfNextAttributeAfterPixelData = AddTIFFOrOffsetTables.getByteOffsetsAndLengthsOfTileDataFromStartOfFile(list,transferSyntax,tileDataByteOffsets,tileDataLengths,imageWidths,imageLengths,false/*mergeSamplesPerPixelTiles*/,samplesPerPixel);
				preamble = AddTIFFOrOffsetTables.makeTIFFInPreambleAndAddDataSetTrailingPadding(byteOffsetFromFileStartOfNextAttributeAfterPixelData,1/*numberOfPyramidLevels*/,tileDataByteOffsets,tileDataLengths,imageWidths,imageLengths,list,
					imageWidth,imageLength,bitsPerSample,outputCompression,outputPhotometric,outputPhotometric/*lowerPhotometric*/,samplesPerPixel,planarConfig,sampleFormat,iccProfile,mmPerPixelX,mmPerPixelY,useBigTIFF);
			}
			catch (DicomException e) {
				e.printStackTrace(System.err);
			}
		}
		
		assertPixelDataEncodingIsCompatibleWithTransferSyntax(list,transferSyntax);
		list.write(outputFileName,transferSyntax,true,true,preamble);

		if (filesToDeleteAfterWritingDicomFile != null) {
			for (File tmpFile : filesToDeleteAfterWritingDicomFile) {
				tmpFile.delete();
			}
			filesToDeleteAfterWritingDicomFile = null;
		}
	}
	
	private class WSIFrameOfReference {
		String uidPyramid;
		String uidOverview;
		int dirNumOfOverview;
		double mmPerPixelXBaseLayerDefault;
		double mmPerPixelYBaseLayerDefault;
		double mmPerPixelXBaseLayer;
		double mmPerPixelYBaseLayer;
		double mmPerPixelOverviewImage;	// assume always square
		double[] mmPerPixelX;			// indexed by IFD (dirNum)
		double[] mmPerPixelY;
		double objectiveLensPower;
		double objectiveLensNumericalAperture;
		double xOffsetInSlideCoordinateSystemPyramid;
		double yOffsetInSlideCoordinateSystemPyramid;
		double xOffsetInSlideCoordinateSystemOverview;
		double yOffsetInSlideCoordinateSystemOverview;
		String[] channelID;				// indexed by channel number from 0 (001323)
		String[] channelName;	// indexed by channel number from 0
		SortedMap<String,String> channelNamesByChannelID;
		double sliceThickness;
		
		int numberOfChannels;
		int numberOfZSections;
		int numberOfTimepoints;
		int[]   channelForIFD;	// indexed by IFD (dirNum)
		int[]  zSectionForIFD;	// indexed by IFD (dirNum)
		int[] timepointForIFD;	// indexed by IFD (dirNum)

		double getmmPerPixelXForIFD(int dirNum) {
			return mmPerPixelX[dirNum];
		}
		
		double getmmPerPixelYForIFD(int dirNum) {
			return mmPerPixelY[dirNum];
		}
		
		double getObjectiveLensPower() {
			return objectiveLensPower;
		}
		
		double getObjectiveLensNumericalAperture() {	// (001319)
			return objectiveLensNumericalAperture;
		}
		
		double getSliceThickness() {	// (001315),(001317)
			return sliceThickness;
		}

		double getXOffsetInSlideCoordinateSystemForIFD(int dirNum) {
			return (dirNumOfOverview != -1 && dirNumOfOverview == dirNum) ? xOffsetInSlideCoordinateSystemOverview : xOffsetInSlideCoordinateSystemPyramid;
		}
		
		double getYOffsetInSlideCoordinateSystemForIFD(int dirNum) {
			return (dirNumOfOverview != -1 && dirNumOfOverview == dirNum) ? yOffsetInSlideCoordinateSystemOverview : yOffsetInSlideCoordinateSystemPyramid;
		}
		
		String getFrameOfReferenceUIDForIFD(int dirNum) {
			return (dirNumOfOverview != -1 && dirNumOfOverview == dirNum) ? uidOverview : uidPyramid;
		}
		
		String getChannelIDForChannel(int channel) {
			return (channelID == null || channel>= channelID.length) ? null : channelID[channel];
		}
		
		String getChannelNameForChannel(int channel) {
			return (channelName == null || channel>= channelName.length) ? null : channelName[channel];		// 
		}

		int getNumberOfChannels() {
			return numberOfChannels;
		}
		
		int getNumberOfZSections() {
			return numberOfZSections;
		}
		
		int getNumberOfTimepoints() {
			return numberOfTimepoints;
		}

		int getChannelForIFD(int dirNum) {
			return channelForIFD == null ? -1 : channelForIFD[dirNum];
		}

		int getZSectionForIFD(int dirNum) {
			return zSectionForIFD == null ? -1 : zSectionForIFD[dirNum];
		}

		int getTimepointForIFD(int dirNum) {
			return timepointForIFD == null ? -1 : timepointForIFD[dirNum];
		}
		
		SortedMap<String,String> getChannelNamesByChannelID() {
			if (channelNamesByChannelID == null) {
				// lazy instantiation
				if (channelID != null && channelName != null) {
					channelNamesByChannelID = new TreeMap<String,String>();
					for (int i=0; i<channelID.length; ++i) {
						String id = channelID[i];
						String desc = channelName[i];
						channelNamesByChannelID.put(id,desc);
					}
				}
			}
			return channelNamesByChannelID;
		}
		
		/*
		 * @param	ifdlist
		 * @param	imageFlavorAndDerivationByIFD
		 * @param	spacingrowmm	PixelSpacing 1st value - spacing between adjacent rows (i.e. "y" spacing) (in mm) to use to override base layer spacing in case it is incorrect or not present (may be 0 if not supplied)
		 * @param	spacingcolmm	PixelSpacing 2nd value - spacing between adjacent columns (i.e. "x" spacing) (in mm) to use to override base layer spacing in case it is incorrect or not present (may be 0 if not supplied)
		 * @param	thicknessmm	SliceThickness and ImagedVolumeDepth value (in mm) to use (may be 0 if not supplied)
		 */
		WSIFrameOfReference(ArrayList<TIFFImageFileDirectory> ifdlist,String[][] imageFlavorAndDerivationByIFD,double spacingrowmm,double spacingcolmm,double thicknessmm) throws DicomException {
			uidPyramid = u.getAnotherNewUID();
			
			dirNumOfOverview = -1;	// flag that we have not encountered an overview image yet (not 0, since 0 is a valid dirNum)
			
			sliceThickness=thicknessmm;	// (001315)
			slf4jlogger.debug("WSIFrameOfReference(): sliceThickness (set to externally supplied thickness)={}",sliceThickness);

			if (spacingcolmm > 0 && spacingrowmm > 0) {		// (001316) (001327)
				mmPerPixelXBaseLayerDefault = spacingcolmm;
				mmPerPixelYBaseLayerDefault = spacingrowmm;
				slf4jlogger.debug("WSIFrameOfReference(): mmPerPixelXBaseLayerDefault (set to externally supplied spacing)={}",mmPerPixelXBaseLayerDefault);
				slf4jlogger.debug("WSIFrameOfReference(): mmPerPixelYBaseLayerDefault (set to externally supplied spacing)={}",mmPerPixelYBaseLayerDefault);
			}
			else {
				mmPerPixelXBaseLayerDefault = 0.5/1000;	// typically 20× (0.5 μm/pixel) and 40× (0.25 μm/pixel) - assume 20x for 1st IFD if not overriden later :(
				mmPerPixelYBaseLayerDefault = mmPerPixelXBaseLayerDefault;	// assume square
				slf4jlogger.debug("WSIFrameOfReference(): mmPerPixelXBaseLayerDefault (assuming 20x)={}",mmPerPixelXBaseLayerDefault);
				slf4jlogger.debug("WSIFrameOfReference(): mmPerPixelYBaseLayerDefault (assuming 20x)={}",mmPerPixelYBaseLayerDefault);
			}
			mmPerPixelXBaseLayer = 0;		// keep track of this for computing pixel spacing for lower pyramid layers - will set the value (or pick default) once we start parsing the IFDs (001265)
			mmPerPixelYBaseLayer = 0;
			
			mmPerPixelX = new double[ifdlist.size()];	// between adjacent columns
			mmPerPixelY = new double[ifdlist.size()];

			objectiveLensPower = 0;
			objectiveLensNumericalAperture = 0;

			numberOfChannels = 1;
			numberOfZSections = 1;
			numberOfTimepoints = 1;
			channelForIFD = null;
			zSectionForIFD = null;
			timepointForIFD = null;

			channelName = null;		// (001285)

			boolean haveOverviewRelativeTop = false;
			boolean haveOverviewRelativeLeft = false;
			boolean haveOverviewPixelSpacing = false;
			
			long widthOfFirstIFDInPixels = 0;	// (001312)
			long widthOfBaseLayerInPixels = 0;
			long widthOfOverviewInPixels = 0;
			long lengthOfOverviewInPixels = 0;	// i.e., height

			double distanceLongAxisSlideFromMacroLeftEdge = 0d;
			double distanceShortAxisSlideFromMacroBottomEdge = 0d;
			
			int numberOfIFDs = ifdlist.size();

			int dirNum = 0;
			for (TIFFImageFileDirectory ifd : ifdlist) {
				slf4jlogger.debug("WSIFrameOfReference(): Directory={}",dirNum);
				
				boolean downsampled = ifd.getSingleNumericValue(TIFFTags.SUBFILETYPE,0,0) == 1;	// Subfile Type: reduced-resolution image (1 = 0x1)	(001307)
				slf4jlogger.debug("WSIFrameOfReference(): based on Subfile Type: reduced-resolution image, downsampled = {}",downsampled);

				String[] description = ifd.getStringValues(TIFFTags.IMAGEDESCRIPTION);	// not ASCII, in case is XML and uses special characters like "µ"
				{
					double micronsPerPixelX = 0d;
					double micronsPerPixelY = 0d;
					double magnification = 0d;

					if (description != null && description.length > 0) {
						slf4jlogger.trace("WSIFrameOfReference(): description.length = {}",description.length);
						for (String d : description) {
							slf4jlogger.trace("WSIFrameOfReference(): String = {}",d);
							if (d.contains("<DataObject ObjectType=\"DPUfsImport\"")) {	// (001391) Philips DPUfsImport
								slf4jlogger.debug("WSIFrameOfReference(): Parsing DPUfsImport XML metadata");
								try {
									//DocumentBuilderFactory.setNamespaceAware(true);	// don't do this - stops XPath from recognizing attributes :(
									Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(d)));
									XPathFactory xpf = XPathFactory.newInstance();
									
									// <DataObject ObjectType="DPUfsImport">
									// 	...
									// 	<Attribute Name="PIM_DP_SCANNED_IMAGES" Group="0x301D" Element="0x1003" PMSVR="IDataObjectArray">
									// 		<Array>
									// 			<DataObject ObjectType="DPScannedImage">
									// 				...
									// 				<Attribute Name="DICOM_PIXEL_SPACING" Group="0x0028" Element="0x0030" PMSVR="IDoubleArray">&quot;0.000243094&quot; &quot;0.000243094&quot;</Attribute>
									// 				...
									// 				<Attribute Name="PIIM_PIXEL_DATA_REPRESENTATION_SEQUENCE" Group="0x1001" Element="0x8B01" PMSVR="IDataObjectArray">
									// 					<Array>
									// 						<DataObject ObjectType="PixelDataRepresentation">
									// 							<Attribute Name="DICOM_PIXEL_SPACING" Group="0x0028" Element="0x0030" PMSVR="IDoubleArray">&quot;0.000243902&quot; &quot;0.000243902&quot;</Attribute>
									// 							<Attribute Name="PIIM_DP_PIXEL_DATA_REPRESENTATION_POSITION" Group="0x101D" Element="0x100B" PMSVR="IDoubleArray">&quot;0&quot; &quot;0&quot; &quot;0&quot;</Attribute>
									// 							<Attribute Name="PIIM_PIXEL_DATA_REPRESENTATION_COLUMNS" Group="0x2001" Element="0x115E" PMSVR="IUInt32">97792</Attribute>
									// 							<Attribute Name="PIIM_PIXEL_DATA_REPRESENTATION_NUMBER" Group="0x1001" Element="0x8B02" PMSVR="IUInt16">0</Attribute>
									// 							<Attribute Name="PIIM_PIXEL_DATA_REPRESENTATION_ROWS" Group="0x2001" Element="0x115D" PMSVR="IUInt32">221184</Attribute>
									
									// the PixelDataRepresentation element repeats, one for each pyramid IFD from 0
									// in the example above, the DPScannedImage/DICOM_PIXEL_SPACING values exactly equals the 1st (0th) IFD PixelDataRepresentation/DICOM_PIXEL_SPACING values
									// but that is not always the case :( - don't know why - use the nested PixelDataRepresentation values for our mmPerPixelX and Y
									{
										NodeList pixelDataRepresentationDataObject = (NodeList)(xpf.newXPath().evaluate("//DataObject[@ObjectType='PixelDataRepresentation']",document,XPathConstants.NODESET));
										slf4jlogger.debug("WSIFrameOfReference(): have pixelDataRepresentationDataObject length = {}",pixelDataRepresentationDataObject.getLength());
										if (pixelDataRepresentationDataObject.getLength() > 0) {
											for (int i=0; i<pixelDataRepresentationDataObject.getLength(); ++i) {
												String pixelDataRepresentationNumber = xpf.newXPath().evaluate("Attribute[@Name='PIIM_PIXEL_DATA_REPRESENTATION_NUMBER']",pixelDataRepresentationDataObject.item(i));
												slf4jlogger.debug("WSIFrameOfReference(): pixelDataRepresentationNumber() = {}",pixelDataRepresentationNumber);
												if (pixelDataRepresentationNumber != null && pixelDataRepresentationNumber.length() > 0) {
													try {
														int indexIntoIFD = Integer.parseInt(pixelDataRepresentationNumber);
														slf4jlogger.debug("WSIFrameOfReference(): indexIntoIFD {}",indexIntoIFD);
														
														// could extract PIIM_PIXEL_DATA_REPRESENTATION_COLUMNS and PIIM_PIXEL_DATA_REPRESENTATION_ROWS and check against IFD TIFF Image Width and Image Length to confirm correct IFD :(

														String dicomPixelSpacing = xpf.newXPath().evaluate("Attribute[@Name='DICOM_PIXEL_SPACING']",pixelDataRepresentationDataObject.item(i));
														slf4jlogger.debug("WSIFrameOfReference(): dicomPixelSpacing() = {}",dicomPixelSpacing);
														// comes back as pair of quoted strings, e.g., '"0.000243902" "0.000243902"'
														String[] dicomPixelSpacingArray = dicomPixelSpacing.split("[ ]+");
														if (dicomPixelSpacingArray != null && dicomPixelSpacingArray.length == 2) {
															try {
																// since nominally a DICOM attribute, adjacent row spacing (Y) is first
																mmPerPixelX[indexIntoIFD] = Double.parseDouble(dicomPixelSpacingArray[1].replaceAll("\"",""));
																slf4jlogger.debug("WSIFrameOfReference(): set mmPerPixelX[{}}] to {}",indexIntoIFD,mmPerPixelX[indexIntoIFD]);
																mmPerPixelY[indexIntoIFD] = Double.parseDouble(dicomPixelSpacingArray[0].replaceAll("\"",""));
																slf4jlogger.debug("WSIFrameOfReference(): set mmPerPixelY[{}}] to {}",indexIntoIFD,mmPerPixelY[indexIntoIFD]);
															}
															catch (NumberFormatException e) {
																slf4jlogger.error("Failed to parse dicomPixelSpacingArray to double ",e);
															}
														}
													}
													catch (NumberFormatException e) {
														slf4jlogger.error("Failed to parse pixelDataRepresentationNumber to int for indexIntoIFD ",e);
													}
												}
												else {
													slf4jlogger.debug("WSIFrameOfReference(): Missing PIIM_PIXEL_DATA_REPRESENTATION_NUMBER in DPUfsImport XML");
												}
											}
										}
									}
								}
								catch (Exception e) {
									slf4jlogger.error("Failed to parse DPUfsImport XML metadata in ImageDescription ",e);
								}
							}
							else if (d.contains("<OME")) {	// (001285) (001322) (001390) not just any XML (e.g., could be Philips DPUfsImport)
								slf4jlogger.debug("WSIFrameOfReference(): Parsing OME-TIFF XML metadata");
								try {
									//DocumentBuilderFactory.setNamespaceAware(true);	// don't do this - stops XPath from recognizing attributes :(
									Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(d)));
									XPathFactory xpf = XPathFactory.newInstance();
									
									// https://docs.openmicroscopy.org/ome-model/6.2.2/ome-tiff/specification.html
									// https://www.openmicroscopy.org/Schemas/Documentation/Generated/OME-2016-06/ome.html
									
									// OME/Pixels DimensionOrder="XYCZT" SizeC="12" SizeT="1" SizeZ="1"
									String dimensionOrder = xpf.newXPath().evaluate("/OME/Image/Pixels/@DimensionOrder",document);
									slf4jlogger.debug("WSIFrameOfReference(): found dimensionOrder {}",dimensionOrder);
									{
										String sizeC = xpf.newXPath().evaluate("/OME/Image/Pixels/@SizeC",document);
										slf4jlogger.debug("WSIFrameOfReference(): found sizeC {}",sizeC);
										if (sizeC != null && sizeC.length() > 0) {
											try {
												numberOfChannels = Integer.parseInt(sizeC);
												slf4jlogger.debug("WSIFrameOfReference(): found numberOfChannels {}",numberOfChannels);
											}
											catch (NumberFormatException e) {
												slf4jlogger.error("Failed to parse sizeC to int ",e);
											}
										}
									}
									{
										String sizeT = xpf.newXPath().evaluate("/OME/Image/Pixels/@SizeT",document);
										slf4jlogger.debug("WSIFrameOfReference(): found sizeT {}",sizeT);
										if (sizeT != null && sizeT.length() > 0) {
											try {
												numberOfTimepoints = Integer.parseInt(sizeT);
												slf4jlogger.debug("WSIFrameOfReference(): found numberOfTimepoints {}",numberOfTimepoints);
											}
											catch (NumberFormatException e) {
												slf4jlogger.error("Failed to parse sizeT to int ",e);
											}
										}
									}
									{
										String sizeZ = xpf.newXPath().evaluate("/OME/Image/Pixels/@SizeZ",document);
										slf4jlogger.debug("WSIFrameOfReference(): found sizeZ {}",sizeZ);
										if (sizeZ != null && sizeZ.length() > 0) {
											try {
												numberOfZSections = Integer.parseInt(sizeZ);
												slf4jlogger.debug("WSIFrameOfReference(): found numberOfZSections {}",numberOfZSections);
											}
											catch (NumberFormatException e) {
												slf4jlogger.error("Failed to parse sizeZ to int ",e);
											}
										}
									}
									if (dimensionOrder != null) {
										try {
											if (dimensionOrder.length() == 5 && dimensionOrder.startsWith("XY")) {
												// permute dimensionOrder across number of channels, Z-sections and timepoints to produce mapping of IFD# to channel, Z-section and timepoint ...
												char  minorOrder = dimensionOrder.charAt(2);
												char middleOrder = dimensionOrder.charAt(3);
												char  majorOrder = dimensionOrder.charAt(4);
												
												channelForIFD = new int[numberOfIFDs];
												zSectionForIFD = new int[numberOfIFDs];
												timepointForIFD = new int[numberOfIFDs];
												
												int minorCount=0;
												int[] minorOrderArray = null;
												switch (minorOrder) {
													case 'C': minorOrderArray=channelForIFD;   minorCount=numberOfChannels;   break;
													case 'Z': minorOrderArray=zSectionForIFD;  minorCount=numberOfZSections;  break;
													case 'T': minorOrderArray=timepointForIFD; minorCount=numberOfTimepoints; break;
													default:
														slf4jlogger.error("Unrecognized character {} in DimensionOrder {}",minorOrder,dimensionOrder);
												}
												int middleCount=0;
												int[] middleOrderArray = null;
												switch (middleOrder) {
													case 'C': middleOrderArray=channelForIFD;   middleCount=numberOfChannels;   break;
													case 'Z': middleOrderArray=zSectionForIFD;  middleCount=numberOfZSections;  break;
													case 'T': middleOrderArray=timepointForIFD; middleCount=numberOfTimepoints; break;
													default:
														slf4jlogger.error("Unrecognized character {} in DimensionOrder {}",middleOrder,dimensionOrder);
												}
												int majorCount=0;
												int[] majorOrderArray = null;
												switch (majorOrder) {
													case 'C': majorOrderArray=channelForIFD;   majorCount=numberOfChannels;   break;
													case 'Z': majorOrderArray=zSectionForIFD;  majorCount=numberOfZSections;  break;
													case 'T': majorOrderArray=timepointForIFD; majorCount=numberOfTimepoints; break;
													default:
														slf4jlogger.error("Unrecognized character {} in DimensionOrder {}",majorOrder,dimensionOrder);
												}
												if (minorOrderArray != null && middleOrderArray != null && majorOrderArray != null) {
													int planeCount = numberOfChannels * numberOfZSections * numberOfTimepoints;
													if (numberOfIFDs >= planeCount) {	// will be at least this large if subIFDs
														{
															int ifdNumber = 0;
															for (int major=0; major<majorCount; ++major) {
																for (int middle=0; middle<middleCount; ++middle) {
																	for (int minor=0; minor<minorCount; ++minor) {
																		minorOrderArray[ifdNumber] = minor;
																		middleOrderArray[ifdNumber] = middle;
																		majorOrderArray[ifdNumber] = major;
																		TIFFImageFileDirectoryEntry subIFDOffsetsEntry = ifd.getEntry(TIFFTags.SUBIFD);	// if present, means that it is the top layer of OME-TIFF pyramid and same dimensions apply to subIFDs (001311)
																		if (subIFDOffsetsEntry == null || subIFDOffsetsEntry.getNumberOfValues() == 0) {
																			++ifdNumber;
																		}
																		else {
																			// propagate dimensions over lower levels of pyramid in subIFDs - probably cannot assume # of subIFDs is the same for every IFD, so enumerate for each one rather than counting in advance (001311)
																			long[] subIFDOffsets = subIFDOffsetsEntry.getValues().getNumericValues();
																			for (long subIFDOffset : subIFDOffsets) {
																				++ifdNumber;
																				minorOrderArray[ifdNumber] = minor;
																				middleOrderArray[ifdNumber] = middle;
																				majorOrderArray[ifdNumber] = major;
																			}
																			++ifdNumber;
																		}
																	}
																}
															}
														}
														if (slf4jlogger.isDebugEnabled()) {
															for (int ifdNumber=0; ifdNumber<numberOfIFDs; ++ifdNumber) {
																slf4jlogger.debug("IFD {} Z{}-T{}-C{}",ifdNumber,zSectionForIFD[ifdNumber],timepointForIFD[ifdNumber],channelForIFD[ifdNumber]);
															}
														}
													}
													else {
														slf4jlogger.error("Number of IFDs {} does not match or exceed planeCount {} calculated from SizeC, SizeZ and SizeT",numberOfIFDs,planeCount);
														channelForIFD = null;
														zSectionForIFD = null;
														timepointForIFD = null;
													}
												}
											}
											else {
												slf4jlogger.error("DimensionOrder {} is not supported",dimensionOrder);
											}
										}
										catch (Exception e) {
											slf4jlogger.error("Failed to process DimensionOrder",e);
										}
									}
									// OME/Image/Pixels@ PhysicalSizeX="0.5022" PhysicalSizeXUnit="µm" PhysicalSizeY="0.5022" PhysicalSizeYUnit="µm"
									{
										String physicalSizeX = xpf.newXPath().evaluate("OME/Image/Pixels/@PhysicalSizeX",document);
										slf4jlogger.debug("WSIFrameOfReference(): found physicalSizeX {}",physicalSizeX);
										String physicalSizeY = xpf.newXPath().evaluate("OME/Image/Pixels/@PhysicalSizeY",document);
										slf4jlogger.debug("WSIFrameOfReference(): found PhysicalSizeY {}",physicalSizeY);
										String physicalSizeXUnit = xpf.newXPath().evaluate("OME/Image/Pixels/@PhysicalSizeXUnit",document);
										slf4jlogger.debug("WSIFrameOfReference(): found PhysicalSizeXUnit {}",physicalSizeXUnit);
										String physicalSizeYUnit = xpf.newXPath().evaluate("OME/Image/Pixels/@PhysicalSizeYUnit",document);
										slf4jlogger.debug("WSIFrameOfReference(): found PhysicalSizeYUnit {}",physicalSizeYUnit);
										// could theoretically handle non-square pixels in DICOM, but happen not to have in WSIFrameOfReference - revisit if ever encountered :(
										
										//only matches "µm" if ImageDescription bytes extracted as "UTF-8" not "US_ASCII", otherwise is pair of Unicode "unrecognized characters"
										if (physicalSizeX.length() > 0 && physicalSizeY.equals(physicalSizeX) && physicalSizeXUnit.equals("µm")) {
											micronsPerPixelX = Double.parseDouble(physicalSizeX);
											slf4jlogger.debug("WSIFrameOfReference(): set micronsPerPixelX to {}",micronsPerPixelX);
											micronsPerPixelY = Double.parseDouble(physicalSizeY);
											slf4jlogger.debug("WSIFrameOfReference(): set micronsPerPixelY to {}",micronsPerPixelY);
										}
										else {
											slf4jlogger.debug("WSIFrameOfReference(): OME/Image/Pixels PhysicalSizeX,Y attributes not as expected, not used");
										}
									}
									{
										// (001317)
										String physicalSizeZ = xpf.newXPath().evaluate("OME/Image/Pixels/@PhysicalSizeZ",document);
										slf4jlogger.debug("WSIFrameOfReference(): found physicalSizeZ {}",physicalSizeZ);
										String physicalSizeZUnit = xpf.newXPath().evaluate("OME/Image/Pixels/@PhysicalSizeZUnit",document);
										slf4jlogger.debug("WSIFrameOfReference(): found PhysicalSizeZUnit {}",physicalSizeZUnit);
										
										//only matches "µm" if ImageDescription bytes extracted as "UTF-8" not "US_ASCII", otherwise is pair of Unicode "unrecognized characters"
										if (physicalSizeZ.length() > 0 && physicalSizeZUnit.equals("µm")) {
											sliceThickness = Double.parseDouble(physicalSizeZ)/1000;	// will override any externally supplied value
											slf4jlogger.debug("WSIFrameOfReference(): set sliceThickness to {}",sliceThickness);
										}
										else {
											slf4jlogger.debug("WSIFrameOfReference(): OME/Image/Pixels PhysicalSizeZ attributes not as expected, not used");
										}
									}
									// OME/Instrument/Objective@ NominalMagnification="20.0"
									{
										String nominalMagnification = xpf.newXPath().evaluate("OME/Instrument/Objective/@NominalMagnification",document);
										slf4jlogger.debug("WSIFrameOfReference(): found nominalMagnification {}",nominalMagnification);
										if (nominalMagnification.length() > 0) {
											objectiveLensPower = Double.parseDouble(nominalMagnification);
										}
									}
									// OME/Instrument/Objective@ LensNA="0.8"
									{
										// (001319)
										String lensNA = xpf.newXPath().evaluate("OME/Instrument/Objective/@LensNA",document);
										slf4jlogger.debug("WSIFrameOfReference(): found lensNA {}",lensNA);
										if (lensNA.length() > 0) {
											objectiveLensNumericalAperture = Double.parseDouble(lensNA);
										}
									}
									// OME/Image/Pixels/Channel@ ID="Channel:0:0" Name="NUCLEI" SamplesPerPixel="1"
									// OME/Image/Pixels/Channel@ ID="Channel:0:1" Name="PD1" SamplesPerPixel="1"
									{
										NodeList channels = (NodeList)(xpf.newXPath().evaluate("OME/Image/Pixels/Channel",document,XPathConstants.NODESET));
										if (channels.getLength() == numberOfChannels) {
											channelID = new String[numberOfChannels];
											channelName = new String[numberOfChannels];
											// the channels are indexed by their sequential order and this may be different than the values n in "Channel:0:n" that is the ChannelID (001323)
											for (int channelNumber=0; channelNumber<channels.getLength(); ++channelNumber) {
												Node channel = channels.item(channelNumber);
												String suppliedChannelID = xpf.newXPath().evaluate("@ID",channel);	// will be of the form "Channel:0:n"
												channelID[channelNumber] = suppliedChannelID == null ? null : suppliedChannelID.replace("Channel:0:","");
												slf4jlogger.debug("WSIFrameOfReference(): for channelNumber {} found channelID {} will actually use {}",channelNumber,suppliedChannelID,channelID[channelNumber]);
												channelName[channelNumber] = xpf.newXPath().evaluate("@Name",channel);
												slf4jlogger.debug("WSIFrameOfReference(): for channelNumber {} found Channel Name {}",channelNumber,channelName[channelNumber]);
											}
										}
										else {
											slf4jlogger.error("WSIFrameOfReference(): number of channels in OME-TIFF XML metadata {} does not match numberOfChannels {}",channels.getLength(),numberOfChannels);
										}
									}

									// OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Key-Value pairs
									// <StructuredAnnotations><XMLAnnotation ID="Annotation:5" Namespace="openmicroscopy.org/OriginalMetadata"><Value>
									// <OriginalMetadata><Key>Series 0 Left</Key><Value>23.235266</Value></OriginalMetadata>
									{
										NodeList originalMetadataKeyValuePairs = (NodeList)(xpf.newXPath().evaluate("//OriginalMetadata",document,XPathConstants.NODESET)); // blech :(
										if (originalMetadataKeyValuePairs != null && originalMetadataKeyValuePairs.getLength() > 0) {
											for (int pair=0; pair<originalMetadataKeyValuePairs.getLength(); ++pair) {
												Node originalMetadataKeyValuePair = originalMetadataKeyValuePairs.item(pair);
												String key = xpf.newXPath().evaluate("Key",originalMetadataKeyValuePair);
												String value = xpf.newXPath().evaluate("Value",originalMetadataKeyValuePair);
												if (key != null && key.length() > 0
													&& value != null && value.length() > 0) {
													slf4jlogger.trace("WSIFrameOfReference(): Have OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Key {} Value {}",key,value);
													if (key.matches("^Series [0-9]+ Left$")) {
														slf4jlogger.debug("WSIFrameOfReference(): Have OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Left {}",value);
														try {
															// SVS, so can assume slide on its side with label on left
															distanceLongAxisSlideFromMacroLeftEdge = Double.parseDouble(value);
															slf4jlogger.debug("WSIFrameOfReference(): found Left {}",distanceLongAxisSlideFromMacroLeftEdge);
															haveOverviewRelativeLeft = true;
														}
														catch (NumberFormatException e) {
															slf4jlogger.error("Failed to parse Left to double ",e);
														}
													}
													else if (key.matches("^Series [0-9]+ Top$")) {
														slf4jlogger.debug("WSIFrameOfReference(): Have OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Top {}",value);
														try {
															// SVS, so can assume slide on its side with label on left
															distanceShortAxisSlideFromMacroBottomEdge = Double.parseDouble(value);
															slf4jlogger.debug("WSIFrameOfReference(): found Top {}",distanceShortAxisSlideFromMacroBottomEdge);
															haveOverviewRelativeTop = true;
														}
														catch (NumberFormatException e) {
															slf4jlogger.error("Failed to parse Top to double ",e);
														}
													}
													//else {
													//	slf4jlogger.debug("WSIFrameOfReference(): Ignoring OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Key {} Value {}",key,value);
													//}
												}
												else {
													slf4jlogger.debug("WSIFrameOfReference(): Cannot find Key and Value for OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata ",originalMetadataKeyValuePair.toString());
												}
											}
										}
										else {
											slf4jlogger.debug("WSIFrameOfReference(): no OME-TIFF XML OriginalMetadata");
										}
									}
								}
								catch (Exception e) {
									slf4jlogger.error("Failed to parse OME-TIFF XML metadata in ImageDescription ",e);
								}
							}
							else if (d.contains("Aperio")) {
								// 46920x33014 [0,100 46000x32914] (256x256) JPEG/RGB Q=30|AppMag = 20|StripeWidth = 2040|ScanScope ID = CPAPERIOCS|Filename = CMU-1|Date = 12/29/09|Time = 09:59:15|User = b414003d-95c6-48b0-9369-8010ed517ba7|Parmset = USM Filter|MPP = 0.4990|Left = 25.691574|Top = 23.449873|LineCameraSkew = -0.000424|LineAreaXOffset = 0.019265|LineAreaYOffset = -0.000313|Focus Offset = 0.000000|ImageID = 1004486|OriginalWidth = 46920|Originalheight = 33014|Filtered = 5|ICC Profile = ScanScope v1

								// 46000x32914 -> 1024x732 - |AppMag = 20|StripeWidth = 2040|ScanScope ID = CPAPERIOCS|Filename = CMU-1|Date = 12/29/09|Time = 09:59:15|User = b414003d-95c6-48b0-9369-8010ed517ba7|Parmset = USM Filter|MPP = 0.4990|Left = 25.691574|Top = 23.449873|LineCameraSkew = -0.000424|LineAreaXOffset = 0.019265|LineAreaYOffset = -0.000313|Focus Offset = 0.000000|ImageID = 1004486|OriginalWidth = 46920|Originalheight = 33014|Filtered = 5|ICC Profile = ScanScope v1

								// 46920x33014 [0,100 46000x32914] (256x256) -> 11500x8228 JPEG/RGB Q=65
				
								// 46000x32914 [0,0 46000x32893] (240x240) J2K/KDU Q=30;CMU-1;Aperio Image Library v10.0.51
								// 46920x33014 [0,100 46000x32914] (256x256) JPEG/RGB Q=30|AppMag = 20|StripeWidth = 2040|ScanScope ID = CPAPERIOCS|Filename = CMU-1|Date = 12/29/09|Time = 09:59:15|User = b414003d-95c6-48b0-9369-8010ed517ba7|Parmset = USM Filter|MPP = 0.4990|Left = 25.691574|Top = 23.449873|LineCameraSkew = -0.000424|LineAreaXOffset = 0.019265|LineAreaYOffset = -0.000313|Focus Offset = 0.000000|ImageID = 1004486|OriginalWidth = 46920|Originalheight = 33014|Filtered = 5|OriginalWidth = 46000|OriginalHeight = 32914

								// 46000x32893 -> 1024x732 - ;CMU-1;Aperio Image Library v10.0.51
								// 46920x33014 [0,100 46000x32914] (256x256) JPEG/RGB Q=30|AppMag = 20|StripeWidth = 2040|ScanScope ID = CPAPERIOCS|Filename = CMU-1|Date = 12/29/09|Time = 09:59:15|User = b414003d-95c6-48b0-9369-8010ed517ba7|Parmset = USM Filter|MPP = 0.4990|Left = 25.691574|Top = 23.449873|LineCameraSkew = -0.000424|LineAreaXOffset = 0.019265|LineAreaYOffset = -0.000313|Focus Offset = 0.000000|ImageID = 1004486|OriginalWidth = 46920|Originalheight = 33014|Filtered = 5|OriginalWidth = 46000|OriginalHeight = 32914

								// 29600x42592 (256x256) J2K/KDU Q=70;BioImagene iScan|Scanner ID = BI10N0294|AppMag = 20|MPP = 0.46500
					
								// 152855x79623 [0,0,152855x79623] (256x256) JPEG/YCC Q=91|AppMag = 40|Date = 07/22/2020|Exposure Scale = 0.000001|Exposure Time = 8|Filtered = 3|Focus Offset = 0.089996|Gamma = 2.2|Left = 6.8904371261597|MPP = 0.263592|Rack = 1|ScanScope ID = SS45002|Slide = 1|StripeWidth = 4096|Time = 08:34:09|Time Zone = GMT+0100|Top = 23.217206954956

								downsampled = d.contains("->");	// need to detect this when MPP of base layer described for downsampled layer
								slf4jlogger.debug("WSIFrameOfReference(): found Aperio downsampled indicator = {}",downsampled);
								
								try {
									BufferedReader r = new BufferedReader(new StringReader(d));
									String line = null;
									while ((line=r.readLine()) != null) {
										{
											// |MPP = 0.4990| or |MPP = 0.4990 end of line with no trailing delimeter
											Pattern p = Pattern.compile(".*[|]MPP[ ]*=[ ]*([0-9][0-9]*[.][0-9][0-9]*)[|]*.*");
											Matcher m = p.matcher(line);
											if (m.matches()) {
												slf4jlogger.debug("WSIFrameOfReference(): have MPP match");
												int groupCount = m.groupCount();
												if (groupCount == 1) {
													slf4jlogger.debug("WSIFrameOfReference(): have MPP correct groupCount");
													try {
														micronsPerPixelX = Double.parseDouble(m.group(1));
														micronsPerPixelY = micronsPerPixelX;
														slf4jlogger.debug("WSIFrameOfReference(): found micronsPerPixel (MPP) {}",micronsPerPixelX);
													}
													catch (NumberFormatException e) {
														slf4jlogger.error("Failed to parse MPP to double ",e);
													}
												}
											}
										}
										{
											// |AppMag = 20| or |AppMag = 20 end of line with no trailing delimeter
											Pattern p = Pattern.compile(".*[|]AppMag[ ]*=[ ]*([0-9][0-9]*)[|]*.*");
											Matcher m = p.matcher(line);
											if (m.matches()) {
												slf4jlogger.debug("WSIFrameOfReference(): have AppMag match");
												int groupCount = m.groupCount();
												if (groupCount == 1) {
													slf4jlogger.debug("WSIFrameOfReference(): have AppMag correct groupCount");
													try {
														objectiveLensPower = Double.parseDouble(m.group(1));
														slf4jlogger.debug("WSIFrameOfReference(): found objectiveLensPower (AppMag) {}",objectiveLensPower);
													}
													catch (NumberFormatException e) {
														slf4jlogger.error("Failed to parse AppMag to double ",e);
													}
												}
											}
										}
										{
											// |Left = 25.691574|
											Pattern p = Pattern.compile(".*[|]Left[ ]*=[ ]*([0-9][0-9]*[.][0-9][0-9]*)[|].*");
											Matcher m = p.matcher(line);
											if (m.matches()) {
												slf4jlogger.debug("WSIFrameOfReference(): have Left match");
												int groupCount = m.groupCount();
												if (groupCount == 1) {
													slf4jlogger.debug("WSIFrameOfReference(): have Left correct groupCount");
													try {
														// SVS, so can assume slide on its side with label on left
														distanceLongAxisSlideFromMacroLeftEdge = Double.parseDouble(m.group(1));
														slf4jlogger.debug("WSIFrameOfReference(): found Left {}",distanceLongAxisSlideFromMacroLeftEdge);
														haveOverviewRelativeLeft = true;
													}
													catch (NumberFormatException e) {
														slf4jlogger.error("Failed to parse Left to double ",e);
													}
												}
											}
										}
										{
											// |Top = 23.449873|
											Pattern p = Pattern.compile(".*[|]Top[ ]*=[ ]*([0-9][0-9]*[.][0-9][0-9]*)[|].*");
											Matcher m = p.matcher(line);
											if (m.matches()) {
												slf4jlogger.debug("WSIFrameOfReference(): have Top match");
												int groupCount = m.groupCount();
												if (groupCount == 1) {
													slf4jlogger.debug("WSIFrameOfReference(): have Top correct groupCount");
													try {
														// SVS, so can assume slide on its side with label on left
														distanceShortAxisSlideFromMacroBottomEdge = Double.parseDouble(m.group(1));
														slf4jlogger.debug("WSIFrameOfReference(): found Top {}",distanceShortAxisSlideFromMacroBottomEdge);
														haveOverviewRelativeTop = true;
													}
													catch (NumberFormatException e) {
														slf4jlogger.error("Failed to parse Top to double ",e);
													}
												}
											}
										}
									}
								}
								catch (IOException e) {
									slf4jlogger.error("Failed to parse ImageDescription ",e);
								}
							}
							else if (d.contains("X scan size")) {
								// encountered in 3D Histech uncompressed TIFF samples
								
								// ImageDescription: X scan size = 4.27mm
								// Y scan size = 28.90mm
								// X offset = 74.00mm
								// Y offset = 23.90mm
								// X resolution = 17067
								// Y resolution = 115600
								// Triple Simultaneous Acquisition
								// Resolution (um) = 0.25
								// Tissue Start Pixel = 40400
								// Tissue End Pixel = 108800
								// Source = Bright Field
								
								try {
									BufferedReader r = new BufferedReader(new StringReader(d));
									String line = null;
									while ((line=r.readLine()) != null) {
										{
											// Resolution (um) = 0.25
											Pattern p = Pattern.compile(".*Resolution[ ]*[(]um[)][ ]*=[ ]*([0-9][0-9]*[.][0-9][0-9]*).*");
											Matcher m = p.matcher(line);
											if (m.matches()) {
												slf4jlogger.debug("WSIFrameOfReference(): have Resolution (um) match");
												int groupCount = m.groupCount();
												if (groupCount == 1) {
													slf4jlogger.debug("WSIFrameOfReference(): have Resolution (um) correct groupCount");
													try {
														micronsPerPixelX = Double.parseDouble(m.group(1));
														micronsPerPixelY = micronsPerPixelX;
														slf4jlogger.debug("WSIFrameOfReference(): found Resolution (um) {}",micronsPerPixelX);
													}
													catch (NumberFormatException e) {
														slf4jlogger.error("Failed to parse Resolution to double ",e);
													}
												}
											}
										}
										{
											// X offset = 74.00mm
											Pattern p = Pattern.compile(".*X offset[ ]*=[ ]*([0-9][0-9]*[.][0-9][0-9]*)[ ]*mm.*");
											Matcher m = p.matcher(line);
											if (m.matches()) {
												slf4jlogger.debug("WSIFrameOfReference(): have X offset match");
												int groupCount = m.groupCount();
												if (groupCount == 1) {
													slf4jlogger.debug("WSIFrameOfReference(): have X offset correct groupCount");
													try {
														// just a guess that this is what 'X' is based on observed values ...
														double xOffset = Double.parseDouble(m.group(1));
														slf4jlogger.debug("WSIFrameOfReference(): found X offset (mm) {}",xOffset);
														yOffsetInSlideCoordinateSystemPyramid = xOffset;	// DICOM Slide Coordinate System is different X and Y
													}
													catch (NumberFormatException e) {
														slf4jlogger.error("Failed to parse X offset to double ",e);
													}
												}
											}
										}
										{
											// Y offset = 23.90mm
											Pattern p = Pattern.compile(".*Y offset[ ]*=[ ]*([0-9][0-9]*[.][0-9][0-9]*)[ ]*mm.*");
											Matcher m = p.matcher(line);
											if (m.matches()) {
												slf4jlogger.debug("WSIFrameOfReference(): have Y offset match");
												int groupCount = m.groupCount();
												if (groupCount == 1) {
													slf4jlogger.debug("WSIFrameOfReference(): have Y offset correct groupCount");
													try {
														double yOffset = Double.parseDouble(m.group(1));
														slf4jlogger.debug("WSIFrameOfReference(): found Y offset (mm) {}",yOffset);
														xOffsetInSlideCoordinateSystemPyramid = yOffset;	// DICOM Slide Coordinate System is different X and Y
													}
													catch (NumberFormatException e) {
														slf4jlogger.error("Failed to parse Y offset to double ",e);
													}
												}
											}
										}
									}
								}
								catch (IOException e) {
									slf4jlogger.error("Failed to parse ImageDescription ",e);
								}
							}
						}
						
						if (!downsampled && micronsPerPixelX != 0&& micronsPerPixelY != 0) {		// take care not to use MPP of base layer specified for downsampled layer, e.g., for 2nd directory of strips
							mmPerPixelX[dirNum] = micronsPerPixelX/1000.0d;
							mmPerPixelY[dirNum] = micronsPerPixelY/1000.0d;
							slf4jlogger.debug("WSIFrameOfReference(): mmPerPixelX[{}] set to {} for non-downsampled layer",dirNum,mmPerPixelX[dirNum]);
							slf4jlogger.debug("WSIFrameOfReference(): mmPerPixelY[{}] set to {} for non-downsampled layer",dirNum,mmPerPixelY[dirNum]);
						}
					}
					else {
						slf4jlogger.debug("WSIFrameOfReference(): no ImageDescription");
					}
				}
				slf4jlogger.debug("mmPerPixelX[{}] extracted from descriptionList={}",dirNum,mmPerPixelX[dirNum]);
				slf4jlogger.debug("mmPerPixelY[{}] extracted from descriptionList={}",dirNum,mmPerPixelY[dirNum]);

				if (mmPerPixelX[dirNum] == 0 || mmPerPixelY[dirNum] == 0) {
					slf4jlogger.debug("mmPerPixelX or Y is zero after parsing descriptionList");
					
					// XResolution (282) RATIONAL (5) 1<10>			Generic-TIFF/CMU-1.tiff - obviously invalid
					// YResolution (283) RATIONAL (5) 1<10>
					// XResolution (282) RATIONAL (5) 1<72>			HTAN-Vanderbilt/H-and-E/HTA11_6147_20000010116110030010000009999.tif - obviously invalid for WSI (001318)
					// YResolution (283) RATIONAL (5) 1<72>
					// XResolution (282) RATIONAL (5) 1<40000/1>
					// YResolution (283) RATIONAL (5) 1<40000/1>
					// XResolution (282) RATIONAL (5) 1<20576.4>	PESO - missing denominator
					// YResolution (283) RATIONAL (5) 1<20576.4>
					double xResolution = ifd.getSingleRationalValue(TIFFTags.XRESOLUTION,0,0);
					slf4jlogger.debug("xResolution={}",xResolution);
					double yResolution = ifd.getSingleRationalValue(TIFFTags.YRESOLUTION,0,0);
					slf4jlogger.debug("yResolution={}",yResolution);

					//if (xResolution > 0 && (!isWSI || xResolution > 10)) {		// not just greater than missing value of 0, but greater than meaningless incorrect value of 10 in Generic-TIFF/CMU-1.tiff
					if (xResolution > 72 && yResolution > 72) {						// not just greater than missing value of 0, but greater than meaningless incorrect value of 72 likely to be nominal ancient default 72dpi (001318)
						if (xResolution != yResolution) {
							slf4jlogger.warn("using non-square or uncalibrated X/YRESOLUTION for mmPerPixel");
						}
						{
							// ResolutionUnit (296) SHORT (3) 1<3>
							long resolutionUnit = ifd.getSingleNumericValue(TIFFTags.RESOLUTIONUNIT,0,2);	// 1 = none, 2 = inch (default), 3 = cm
							slf4jlogger.debug("resolutionUnit={}",resolutionUnit);

							if (resolutionUnit == 2 || resolutionUnit == 3) {		// inch or cm
								double mmPerPixelXFromResolution = (resolutionUnit == 2 ? 25.4d : 10.0d) / xResolution;
								double mmPerPixelYFromResolution = (resolutionUnit == 2 ? 25.4d : 10.0d) / yResolution;
								if (downsampled) {
									if (mmPerPixelXBaseLayer > 0 && mmPerPixelYBaseLayer > 0) {
										double scaleFactor = mmPerPixelXFromResolution/mmPerPixelXBaseLayer;
										if (scaleFactor > 1.001d) {		// is .1% the appropriate fuzz factor for this floating point comparison? :(
											slf4jlogger.debug("downsampled and not the same as baselayer, so using value computed from XRESOLUTION for {} for mmPerPixelX",mmPerPixelXFromResolution);
											slf4jlogger.debug("downsampled and not the same as baselayer, so using value computed from YRESOLUTION for {} for mmPerPixelY",mmPerPixelYFromResolution);
											mmPerPixelX[dirNum] = mmPerPixelXFromResolution;
											mmPerPixelY[dirNum] = mmPerPixelYFromResolution;
										}
										else {
											// this happens in bfconvert produced OME-TIFF files with downsampled layers in subIFDs that have resolution values the same as the base layer :( (001307)
											// should actually compare width,length (columns,rows) of baselayer and current IFD :(
											slf4jlogger.debug("downsampled but value computed from XRESOLUTION for {} for mmPerPixelX is too close to value for baselayer {} and presumably wrong so ignoring",mmPerPixelXFromResolution,mmPerPixelXBaseLayer);
											slf4jlogger.debug("downsampled but value computed from YRESOLUTION for {} for mmPerPixelY is too close to value for baselayer {} and presumably wrong so ignoring",mmPerPixelYFromResolution,mmPerPixelYBaseLayer);
										}
									}
									else {
										slf4jlogger.debug("downsampled but baselayer value not known, so using value computed from XRESOLUTION for {} for mmPerPixelX",mmPerPixelXFromResolution);
										slf4jlogger.debug("downsampled but baselayer value not known, so using value computed from YRESOLUTION for {} for mmPerPixelY",mmPerPixelYFromResolution);
										mmPerPixelX[dirNum] = mmPerPixelXFromResolution;
										mmPerPixelY[dirNum] = mmPerPixelYFromResolution;
									}
								}
								else {
									slf4jlogger.debug("not downsampled so using value computed from XRESOLUTION for {} for mmPerPixelX",mmPerPixelXFromResolution);
									slf4jlogger.debug("not downsampled so using value computed from YRESOLUTION for {} for mmPerPixelY",mmPerPixelYFromResolution);
									mmPerPixelX[dirNum] = mmPerPixelXFromResolution;
									mmPerPixelY[dirNum] = mmPerPixelYFromResolution;
								}
							}
							else if (resolutionUnit == 1) {
								slf4jlogger.debug("not using no meaningful RESOLUTIONUNIT for mmPerPixel");
							}
							else {
								slf4jlogger.debug("not using unrecognized RESOLUTIONUNIT {} for mmPerPixel",resolutionUnit);
							}
						}
					}
					else {
						slf4jlogger.debug("not using missing or obviously invalid XRESOLUTION of {} for mmPerPixel",xResolution);
						slf4jlogger.debug("not using missing or obviously invalid YRESOLUTION of {} for mmPerPixel",yResolution);
					}
					slf4jlogger.debug("mmPerPixel is {} after checking XRESOLUTION and RESOLUTIONUNIT",mmPerPixelX[dirNum]);
					slf4jlogger.debug("mmPerPixel is {} after checking YRESOLUTION and RESOLUTIONUNIT",mmPerPixelY[dirNum]);
				}
				
				//if (isWSI) {
				{
					long imageWidth = ifd.getSingleNumericValue(TIFFTags.IMAGEWIDTH,0,0);
					slf4jlogger.debug("imageWidth={}",imageWidth);
					long imageLength = ifd.getSingleNumericValue(TIFFTags.IMAGELENGTH,0,0);
					slf4jlogger.debug("imageLength={}",imageLength);
					
					TIFFImageFileDirectoryEntry subIFDOffsetsEntry = ifd.getEntry(TIFFTags.SUBIFD);	// if present, means that it is the top layer of OME-TIFF pyramid (001307)
					slf4jlogger.debug("subIFDOffsets is {}",subIFDOffsetsEntry == null ? "absent" : "present");

					if (dirNum == 0) {
						widthOfBaseLayerInPixels = imageWidth;				// store this to calculate pixel spacing for subsequent (lower) layers of pyramid
						widthOfFirstIFDInPixels = imageWidth;				// store this to use for checking back to base layer for multidimensional (e.g., multichannel) OME-TIFF files after processing sub-IFDs when resolution tags not present (001312)
					}
					if (mmPerPixelX[dirNum] == 0 || mmPerPixelY[dirNum] == 0) {
						slf4jlogger.debug("mmPerPixelX or Y is zero");
						if (imageFlavorAndDerivationByIFD[dirNum][0].equals("OVERVIEW")) {
							slf4jlogger.debug("computing mmPerPixel for macro (OVERVIEW) from standard slide height");
							mmPerPixelOverviewImage = 25.4d / imageLength;	// (001267)
							mmPerPixelX[dirNum] = mmPerPixelOverviewImage;
							mmPerPixelY[dirNum] = mmPerPixelOverviewImage;	// assume square
							dirNumOfOverview = dirNum;
							haveOverviewPixelSpacing = true;
							widthOfOverviewInPixels = imageWidth;			// store this to calculate overview origin in frame of reference
							lengthOfOverviewInPixels = imageLength;
						}
						else if (downsampled) {								// (001307)
							slf4jlogger.debug("deriving mmPerPixelX from pixel width {} relative to base layer width {} and pixel spacing {}",imageWidth,widthOfBaseLayerInPixels,mmPerPixelXBaseLayer);
							double scaleFactorX = ((double)widthOfBaseLayerInPixels)/imageWidth;	// assumes all images are same physical width (001265) (001349)
							slf4jlogger.debug("scaleFactorX = {}",scaleFactorX);
							mmPerPixelX[dirNum] = mmPerPixelXBaseLayer * scaleFactorX;
							// do not want to make downsampled appear to have non-square pixels or different aspect ratio than base layer ... so re-use scaleFactorX rather than computing for Y ...
							mmPerPixelY[dirNum] = mmPerPixelYBaseLayer * scaleFactorX;

						}
						else if (imageWidth == widthOfFirstIFDInPixels && mmPerPixelX[0] != 0 && mmPerPixelY[0] != 0) {	// (001312) OME-TIFF
							slf4jlogger.debug("reusing first IFD {} mmPerPixelX, since not specified, not overview, not downsampled and same width in pixels",mmPerPixelX[0]);
							slf4jlogger.debug("reusing first IFD {} mmPerPixelY, since not specified, not overview, not downsampled and same width in pixels",mmPerPixelY[0]);
							mmPerPixelX[dirNum] = mmPerPixelX[0];
							mmPerPixelY[dirNum] = mmPerPixelY[0];
						}
						else {
							slf4jlogger.debug("using default {} mmPerPixelX, since not specified, not overview and not downsampled",mmPerPixelXBaseLayerDefault);
							slf4jlogger.debug("using default {} mmPerPixelY, since not specified, not overview and not downsampled",mmPerPixelYBaseLayerDefault);
							mmPerPixelX[dirNum] = mmPerPixelXBaseLayerDefault;
							mmPerPixelY[dirNum] = mmPerPixelYBaseLayerDefault;
							mmPerPixelXBaseLayer = mmPerPixelX[dirNum];		// (001265)
							mmPerPixelYBaseLayer = mmPerPixelY[dirNum];
						}
					}
					else {
						if (subIFDOffsetsEntry != null) {					// (001307)
							mmPerPixelXBaseLayer = mmPerPixelX[dirNum];		// keep track of this, for computing pixel spacing of lower layers of pyramid (001265)
							mmPerPixelYBaseLayer = mmPerPixelY[dirNum];
							slf4jlogger.debug("setting mmPerPixelXBaseLayer={} because has subIFDOffsets",mmPerPixelXBaseLayer);
							slf4jlogger.debug("setting mmPerPixelYBaseLayer={} because has subIFDOffsets",mmPerPixelYBaseLayer);
						}
						else if (dirNum == 0) {								// assume base layer is first ... could check this with extra pass through IFDs :(
							mmPerPixelXBaseLayer = mmPerPixelX[dirNum];		// keep track of this, for computing pixel spacing of lower layers of pyramid (001265)
							mmPerPixelYBaseLayer = mmPerPixelY[dirNum];
							slf4jlogger.debug("setting mmPerPixelXBaseLayer={} because is first IFD",mmPerPixelXBaseLayer);
							slf4jlogger.debug("setting mmPerPixelYBaseLayer={} because is first IFD",mmPerPixelYBaseLayer);
						}
					}
					slf4jlogger.debug("Using mmPerPixelX[{}]={}",dirNum,mmPerPixelX[dirNum]);
					slf4jlogger.debug("Using mmPerPixelY[{}]={}",dirNum,mmPerPixelY[dirNum]);
					slf4jlogger.debug("Using mmPerPixelXBaseLayer={}",mmPerPixelXBaseLayer);
					slf4jlogger.debug("Using mmPerPixelYBaseLayer={}",mmPerPixelYBaseLayer);
				}
				
				++dirNum;
			}

			slf4jlogger.debug("haveOverviewPixelSpacing={}",haveOverviewPixelSpacing);
			slf4jlogger.debug("haveOverviewRelativeLeft={}",haveOverviewRelativeLeft);
			slf4jlogger.debug("haveOverviewRelativeTop={}",haveOverviewRelativeTop);
			slf4jlogger.debug("xOffsetInSlideCoordinateSystemPyramid={}",xOffsetInSlideCoordinateSystemPyramid);
			slf4jlogger.debug("yOffsetInSlideCoordinateSystemPyramid={}",yOffsetInSlideCoordinateSystemPyramid);

			// (001256) (001268)
			if (haveOverviewPixelSpacing
			 && haveOverviewRelativeLeft
			 && haveOverviewRelativeTop) {
				slf4jlogger.debug("Establishing common frame of reference between OVERVIEW and pyramid");
				
				uidOverview = uidPyramid;

				// (001255)
				// Aperio SVS seems to always be slide on its side with label on left
				// In that orientation, DICOM origin is bottom right corner of slide, short axis is +X and long axis +Y (001269)
							
				xOffsetInSlideCoordinateSystemPyramid = distanceShortAxisSlideFromMacroBottomEdge;
				yOffsetInSlideCoordinateSystemPyramid = mmPerPixelOverviewImage * widthOfOverviewInPixels - distanceLongAxisSlideFromMacroLeftEdge; // (001269)

				xOffsetInSlideCoordinateSystemOverview = mmPerPixelOverviewImage * lengthOfOverviewInPixels;
				yOffsetInSlideCoordinateSystemOverview = mmPerPixelOverviewImage * widthOfOverviewInPixels; // (001269)
			}
			else if (xOffsetInSlideCoordinateSystemPyramid != 0 || yOffsetInSlideCoordinateSystemPyramid != 0) {
				slf4jlogger.debug("Have explicit X and Y offsets for pyramid frame of reference");
				uidOverview = u.getAnotherNewUID();		// probably is no OVERVIEW (in 3D Histech) but just in case
				// xOffsetInSlideCoordinateSystemPyramid and yOffsetInSlideCoordinateSystemPyramid already set
				xOffsetInSlideCoordinateSystemOverview = 0;
				yOffsetInSlideCoordinateSystemOverview = 0;
			}
			else {
				slf4jlogger.debug("Cannot establish common frame of reference between OVERVIEW and pyramid");

				uidOverview = u.getAnotherNewUID();
				
				xOffsetInSlideCoordinateSystemPyramid = 0;
				yOffsetInSlideCoordinateSystemPyramid = 0;
				
				xOffsetInSlideCoordinateSystemOverview = 0;
				yOffsetInSlideCoordinateSystemOverview = 0;
			}
			slf4jlogger.debug("xOffsetInSlideCoordinateSystemPyramid={}",xOffsetInSlideCoordinateSystemPyramid);
			slf4jlogger.debug("yOffsetInSlideCoordinateSystemPyramid={}",yOffsetInSlideCoordinateSystemPyramid);
			slf4jlogger.debug("xOffsetInSlideCoordinateSystemOverview={}",xOffsetInSlideCoordinateSystemOverview);
			slf4jlogger.debug("yOffsetInSlideCoordinateSystemOverview={}",yOffsetInSlideCoordinateSystemOverview);
		}

	}
	
	private String getDateTimeStringForIFDTIFFTagOrImageDescription(TIFFImageFileDirectory ifd) {
		String dateTimeString = null;
		
		return dateTimeString;
	}

	// returns strings of the form yyyyMMddHHmmss[.SSS]
	// (001340) (001341)
	private String[] addCommonDateTimeInformation(ArrayList<TIFFImageFileDirectory> ifdlist,AttributeList descriptionList) throws DicomException {
		String[] dateTimeStringByIFDNumber = new String[ifdlist.size()];
		String useCommonDate = null;
		String useCommonTime = null;
		
		// search IFDs for Date and Time values, and use first encountered (no check for earliest)
		{
			int dirNum = 0;
			for (TIFFImageFileDirectory ifd : ifdlist) {
				slf4jlogger.debug("addCommonDateTimeInformation(): Directory={}",dirNum);
				// first check for dates and times in IMAGEDESCRIPTION
				{
					{
						String dateString = null;
						String timeString = null;
						String[] description = ifd.getStringValues(TIFFTags.IMAGEDESCRIPTION);
						if (description != null && description.length > 0) {
							slf4jlogger.trace("addCommonDateTimeInformation(): description.length = {}",description.length);
							for (String d : description) {
								slf4jlogger.trace("addCommonDateTimeInformation(): String = {}",d);
								// need to check XML first, since string "Aperio" may appear in XML
								if (d.startsWith("<?xml") || d.startsWith("<OME")) {
									// (001341)
									slf4jlogger.debug("addCommonDateTimeInformation(): Parsing OME-TIFF XML metadata");
									try {
										Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(d)));
										XPathFactory xpf = XPathFactory.newInstance();
										// OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Key-Value pairs
										// <StructuredAnnotations><XMLAnnotation ID="Annotation:0" Namespace="openmicroscopy.org/OriginalMetadata">
										// <OriginalMetadata><Key>Series 0 Date</Key><Value>Removed</Value></OriginalMetadata>
										// <OriginalMetadata><Key>Series 0 Time</Key><Value>15:05:01</Value></OriginalMetadata>
										// <OriginalMetadata><Key>Series 0 Time Zone</Key><Value>GMT-08:00</Value></OriginalMetadata>
										{
											// https://stackoverflow.com/questions/4440451/how-to-ignore-namespaces-with-xpath
											//NodeList originalMetadataKeyValuePairs = (NodeList)(xpf.newXPath().evaluate("OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata",document,XPathConstants.NODESET));
											//NodeList originalMetadataKeyValuePairs = (NodeList)(xpf.newXPath().evaluate("OME/StructuredAnnotations/*[local-name() = 'XMLAnnotation']/OriginalMetadata",document,XPathConstants.NODESET));
											NodeList originalMetadataKeyValuePairs = (NodeList)(xpf.newXPath().evaluate("//OriginalMetadata",document,XPathConstants.NODESET)); // blech :(
											if (originalMetadataKeyValuePairs != null && originalMetadataKeyValuePairs.getLength() > 0) {
												for (int pair=0; pair<originalMetadataKeyValuePairs.getLength(); ++pair) {
													Node originalMetadataKeyValuePair = originalMetadataKeyValuePairs.item(pair);
													String key = xpf.newXPath().evaluate("Key",originalMetadataKeyValuePair);
													String value = xpf.newXPath().evaluate("Value",originalMetadataKeyValuePair);
													if (key != null && key.length() > 0
													 && value != null && value.length() > 0) {
														slf4jlogger.trace("addCommonDateTimeInformation(): Have OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Key {} Value {}",key,value);
														if (key.matches("^Series [0-9]+ Date$")) {
															slf4jlogger.debug("addCommonDateTimeInformation(): Have OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Date {}",value);
															// 12/29/09
															Pattern p = Pattern.compile("([0-9][0-9])/([0-9][0-9])/([0-9][0-9])");
															Matcher m = p.matcher(value);
															if (m.matches()) {
																slf4jlogger.debug("addCommonDateTimeInformation(): have OME-TIFF XML OriginalMetadata SVS-style Date match");
																int groupCount = m.groupCount();
																if (groupCount == 3) {
																	slf4jlogger.debug("addCommonDateTimeInformation(): have OME-TIFF XML OriginalMetadata SVS-style Date correct groupCount");
																	String month = m.group(1);
																	String day = m.group(2);
																	String twodigityear = m.group(3);
																	dateString = "20" + twodigityear + month + day;
																	slf4jlogger.debug("addCommonDateTimeInformation(): found OME-TIFF XML OriginalMetadata SVS-style Date {}",dateString);
																}
															}
															else {
																slf4jlogger.warn("addCommonDateTimeInformation(): Unrecognized value format OME-TIFF XML OriginalMetadata SVS-style Date Key \"{}\" Value \"{}\"",key,value);
															}
														}
														else if (key.matches("^Series [0-9]+ Time$")) {
															slf4jlogger.debug("addCommonDateTimeInformation(): Have OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata Time {}",value);
															// 09:59:15
															Pattern p = Pattern.compile("([0-9][0-9]):([0-9][0-9]):([0-9][0-9])");
															Matcher m = p.matcher(value);
															if (m.matches()) {
																slf4jlogger.debug("addCommonDateTimeInformation(): have OME-TIFF XML OriginalMetadata SVS-style Time match");
																int groupCount = m.groupCount();
																if (groupCount == 3) {
																	slf4jlogger.debug("addCommonDateTimeInformation(): have OME-TIFF XML OriginalMetadata SVS-style Time correct groupCount");
																	String hh = m.group(1);
																	String mm = m.group(2);
																	String ss = m.group(3);
																	timeString = hh + mm + ss;
																	slf4jlogger.debug("addCommonDateTimeInformation(): found OME-TIFF XML OriginalMetadata SVS-style Time {}",timeString);
																}
															}
															else {
																slf4jlogger.warn("addCommonDateTimeInformation(): Unrecognized value format OME-TIFF XML OriginalMetadata SVS-style Time Key \"{}\" Value \"{}\"",key,value);
															}
														}
														// should do timezone :(
													}
													else {
														slf4jlogger.debug("addCommonDateTimeInformation(): Cannot find Key and Value for OME/StructuredAnnotations/XMLAnnotation/OriginalMetadata ",originalMetadataKeyValuePair.toString());
													}
												}
											}
											else {
												slf4jlogger.debug("addCommonDateTimeInformation(): no OME-TIFF XML OriginalMetadata");
											}
										}
									}
									catch (Exception e) {
										slf4jlogger.error("Failed to parse OME-TIFF XML metadata in ImageDescription ",e);
									}
								}
								else if (d.contains("Aperio")) {
									// (001340)
									// assume SVS but could be other Aperio format, theoretically ? :(
									// 46920x33014 [0,100 46000x32914] (256x256) JPEG/RGB Q=30|AppMag = 20|StripeWidth = 2040|ScanScope ID = CPAPERIOCS|Filename = CMU-1|Date = 12/29/09|Time = 09:59:15|User = b414003d-95c6-48b0-9369-8010ed517ba7|Parmset = USM Filter|MPP = 0.4990|Left = 25.691574|Top = 23.449873|LineCameraSkew = -0.000424|LineAreaXOffset = 0.019265|LineAreaYOffset = -0.000313|Focus Offset = 0.000000|ImageID = 1004486|OriginalWidth = 46920|Originalheight = 33014|Filtered = 5|ICC Profile = ScanScope v1
									// 95744x86336 (256x256) J2K/KDU Q=70;Leica SCN400;Leica SCN ver.1.5.1.10804 2012/05/10 13:29:07;1.5.1.10864|Filename = ImageCollection_0000000935|Date = 2014-07-23T16:33:58.37Z|AppMag = 40.000000|MPP = 0.250000|OriginalWidth = 86336|OriginalHeight = 95744
									slf4jlogger.debug("addCommonDateTimeInformation(): processing SVS ImageDescription {}");
									try {
										BufferedReader r = new BufferedReader(new StringReader(d));
										String line = null;
										while ((line=r.readLine()) != null) {
											{
												// |Date = 12/29/09|
												Pattern p = Pattern.compile(".*[|]Date[ ]*=[ ]*([0-9][0-9])/([0-9][0-9])/([0-9][0-9])[|].*");
												Matcher m = p.matcher(line);
												if (m.matches()) {
													slf4jlogger.debug("addCommonDateTimeInformation(): have SVS date match");
													int groupCount = m.groupCount();
													if (groupCount == 3) {
														slf4jlogger.debug("addCommonDateTimeInformation(): have SVS date correct groupCount");
														String month = m.group(1);
														String day = m.group(2);
														String twodigityear = m.group(3);
														dateString = "20" + twodigityear + month + day;
														slf4jlogger.debug("addCommonDateTimeInformation(): found SVS date {}",dateString);
													}
												}
											}
											{
												// |Time = 09:59:15|
												Pattern p = Pattern.compile(".*[|]Time[ ]*=[ ]*([0-9][0-9]):([0-9][0-9]):([0-9][0-9])[|].*");
												Matcher m = p.matcher(line);
												if (m.matches()) {
													slf4jlogger.debug("addCommonDateTimeInformation(): have SVS time match");
													int groupCount = m.groupCount();
													if (groupCount == 3) {
														slf4jlogger.debug("addCommonDateTimeInformation(): have SVS time correct groupCount");
														String hh = m.group(1);
														String mm = m.group(2);
														String ss = m.group(3);
														timeString = hh + mm + ss;
														slf4jlogger.debug("addCommonDateTimeInformation(): found SVS time {}",timeString);
													}
												}
											}
											{
												// 001339
												// |Date = 2014-07-23T16:33:58.37Z|
												Pattern p = Pattern.compile(".*[|]Date[ ]*=[ ]*([0-9][0-9][0-9][0-9])-([0-9][0-9])-([0-9][0-9])T([0-9][0-9]):([0-9][0-9]):([0-9][0-9])[.]([0-9][0-9])Z[|].*");
												Matcher m = p.matcher(line);
												if (m.matches()) {
													slf4jlogger.debug("addCommonDateTimeInformation(): have SVS SCN style datetime match");
													int groupCount = m.groupCount();
													if (groupCount == 7) {
														slf4jlogger.debug("addCommonDateTimeInformation(): have SVS SCN style datetime correct groupCount");
														String year = m.group(1);
														String month = m.group(2);
														String day = m.group(3);
														dateString = year + month + day;
														String hh = m.group(4);
														String mm = m.group(5);
														String ss = m.group(6);
														String fraction = m.group(7);
														timeString = hh + mm + ss + "." + fraction;
														slf4jlogger.debug("addCommonDateTimeInformation(): found SVS SCN style date {}",dateString);
														slf4jlogger.debug("addCommonDateTimeInformation(): found SVS SCN style time {}",timeString);
													}
												}
											}
										}
									}
									catch (IOException e) {
										slf4jlogger.error("Failed to parse SVS ImageDescription ",e);
									}
								}
							}
						}
						// regardless of whether these came from SVS or OME-TIFF ImageDescription
						if (dateString != null && dateString.length() > 0
							&& timeString != null && timeString.length() > 0) {
							String dateTimeString = dateString + timeString;
							slf4jlogger.debug("addCommonDateTimeInformation(): found SVS date and time for IFD {} {}",dirNum,dateTimeString);
							dateTimeStringByIFDNumber[dirNum] = dateTimeString;
						}
						// regardless of which IFD (though usually the 1st) ...
						if (dateString != null && dateString.length() > 0) {
							if (useCommonDate == null || useCommonDate.length() == 0) {
								slf4jlogger.debug("addCommonDateTimeInformation(): first encounter, so setting useCommonDate from SVS date to {}",dateString);
								useCommonDate = dateString;
							}
							else if (!useCommonDate.equals(dateString)) {
								slf4jlogger.warn("addCommonDateTimeInformation(): different SVS date {} than previously encountered useCommonDate {}",dateString,useCommonDate);
							}
						}
						if (timeString != null && timeString.length() > 0) {
							if (useCommonTime == null || useCommonTime.length() == 0) {
								slf4jlogger.debug("addCommonDateTimeInformation(): first encounter, so setting useCommonTime from SVS time to {}",timeString);
								useCommonTime = timeString;
							}
							else if (!useCommonTime.equals(timeString)) {
								slf4jlogger.warn("addCommonDateTimeInformation(): different SVS time {} than previously encountered useCommonTime {}",timeString,useCommonTime);
							}
						}
					}
				}

				// then check for dates and times in in TIFF DATETIME tag (even if IMAGEDESCRIPTION was parsed and analyzed
				{
					String[] tiffDateTimes = ifd.getStringValues(TIFFTags.DATETIME);	// (001340)
					if (tiffDateTimes != null && tiffDateTimes.length > 0) {
						// should only be one, only check first
						if (tiffDateTimes[0] != null && tiffDateTimes[0].length() > 0) {
							String tiffDateTime = tiffDateTimes[0].trim();
							// e.g.: DateTime (306) ASCII (2) 20<2020:10:02 18:58:43\0>
							slf4jlogger.debug("addCommonDateTimeInformation(): TIFF DATETIME = {}",tiffDateTime);
							// per TIFF6: The format is: YYYY:MM:DD HH:MM:SS
							Pattern p = Pattern.compile("([0-9][0-9][0-9][0-9]):([0-9][0-9]):([0-9][0-9]) ([0-9][0-9]):([0-9][0-9]):([0-9][0-9]).*");
							Matcher m = p.matcher(tiffDateTime);
							if (m.matches()) {
								slf4jlogger.debug("addCommonDateTimeInformation(): have TIFF DATETIME match");
								int groupCount = m.groupCount();
								if (groupCount == 6) {
									slf4jlogger.debug("addCommonDateTimeInformation(): have TIFF DATETIME correct groupCount");
									String fourdigityear = m.group(1);
									String month = m.group(2);
									String day = m.group(3);
									String hours = m.group(4);
									String minutes = m.group(5);
									String seconds = m.group(6);
									String dateString = fourdigityear + month + day;
									String timeString = hours + minutes + seconds;
									String dateTimeString = dateString + timeString;
									slf4jlogger.debug("addCommonDateTimeInformation(): found TIFF DATETIME for IFD {} {}",dirNum,dateTimeString);
									dateTimeStringByIFDNumber[dirNum] = dateTimeString;
									if (useCommonDate == null || useCommonDate.length() == 0) {
										slf4jlogger.debug("addCommonDateTimeInformation(): first encounter, so setting useCommonDate to {}",dateString);
										useCommonDate = dateString;
									}
									else if (!useCommonDate.equals(dateString)) {
										slf4jlogger.warn("addCommonDateTimeInformation(): different TIFF DATETIME date {} than previously encountered useCommonDate {}",dateString,useCommonDate);
									}
									if (useCommonTime == null || useCommonTime.length() == 0) {
										slf4jlogger.debug("addCommonDateTimeInformation(): first encounter, so setting useCommonTime to {}",timeString);
										useCommonTime = timeString;
									}
									else if (!useCommonTime.equals(timeString)) {
										slf4jlogger.warn("addCommonDateTimeInformation(): different TIFF DATETIME time {} than previously encountered useCommonTime {}",timeString,useCommonTime);
									}
								}
							}
							else {
								slf4jlogger.warn("addCommonDateTimeInformation(): unrecognized format for TIFF DATETIME {} - ignoring",tiffDateTime);
							}
						}
						else {
							slf4jlogger.warn("addCommonDateTimeInformation(): missing value for TIFF DATETIME - ignoring");
						}
					}
					else {
						slf4jlogger.debug("addCommonDateTimeInformation(): no TIFF DATETIME present");
					}
				}
			}
		}
		{
			java.util.Date currentDateTime = new java.util.Date();
			if (useCommonDate == null || useCommonDate.length() == 0) {
				useCommonDate = new java.text.SimpleDateFormat("yyyyMMdd").format(currentDateTime);
				slf4jlogger.debug("addCommonDateTimeInformation(): using default current date for StudyDate, etc. since nothing found within TIFF {}",useCommonDate);
			}
			else {
				slf4jlogger.debug("addCommonDateTimeInformation(): using extracted date from TIFF for StudyDate, etc. {}",useCommonDate);
			}
			if (useCommonTime == null || useCommonTime.length() == 0) {
				useCommonTime = new java.text.SimpleDateFormat("HHmmss.SSS").format(currentDateTime);
				slf4jlogger.debug("addCommonDateTimeInformation(): using default current time for StudyTime, etc. since nothing found within TIFF {}",useCommonTime);
			}
			else {
				slf4jlogger.debug("addCommonDateTimeInformation(): using extracted time from TIFF for StudyTime, etc. {}",useCommonTime);
			}
		}
		{
			{ Attribute a = new DateAttribute(TagFromName.StudyDate);   	a.addValue(useCommonDate); descriptionList.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.SeriesDate);  	a.addValue(useCommonDate); descriptionList.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.ContentDate); 	a.addValue(useCommonDate); descriptionList.put(a); }
			{ Attribute a = new DateAttribute(TagFromName.AcquisitionDate); a.addValue(useCommonDate); descriptionList.put(a); }

			{ Attribute a = new TimeAttribute(TagFromName.StudyTime);   	a.addValue(useCommonTime); descriptionList.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.SeriesTime);  	a.addValue(useCommonTime); descriptionList.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.ContentTime);		a.addValue(useCommonTime); descriptionList.put(a); }
			{ Attribute a = new TimeAttribute(TagFromName.AcquisitionTime); a.addValue(useCommonTime); descriptionList.put(a); }

			{ Attribute a = new DateTimeAttribute(TagFromName.AcquisitionDateTime); a.addValue(useCommonDate+useCommonTime); descriptionList.put(a); }
		}
		return dateTimeStringByIFDNumber;
	}

	/**
	 * <p>Read a TIFF image input format file and create an image of a specified or appropriate SOP Class.</p>
	 *
	 * @param	jsonfile		JSON file describing the functional groups and attributes and values to be added or replaced
	 * @param	inputFileName
	 * @param	outputFilePrefix
	 * @param	outputFileSuffix
	 * @param	modality	may be null
	 * @param	sopClass	may be null
	 * @param	transferSyntax	may be null, used if multiframe, ignored if single frame (which is always written uncompressed)
	 * @param	addTIFF		whether or not to add a TIFF IFD in the DICOM preamble to make a dual=personality DICOM-TIFF file sharing the same pixel data
	 * @param	useBigTIFF	whether or not to create a BigTIFF rather than Classic TIFF file
	 * @param	alwaysWSI	whether or not to add WSI-related information even if not WSI SOP Class, e.g., for float converted to parametric map
	 * @param	addPyramid	whether or not to add multi-resolution pyramid (downsampled) layers to the TIFF IFD and a corresponding DICOM private data element in the same file
	 * @param	mergeStrips	whether or not to merge an image with more than one strip into a single DICOM image, or to create a separate image or frame for each strip
	 * @param	autoRecognize	whether or not to try to determine SOP Class and Modality to use from TIFF metadata
	 * @param	channelFileName	may be null
	 * @param	includeFileName	whether or not to record the original filename in the DICOM output
	 * @param	spacingrowmm	PixelSpacing 1st value - spacing between adjacent rows (i.e. "y" spacing) (in mm) to use to override base layer spacing in case it is incorrect or not present (may be 0 if not supplied)
	 * @param	spacingcolmm	PixelSpacing 2nd value - spacing between adjacent columns (i.e. "x" spacing) (in mm) to use to override base layer spacing in case it is incorrect or not present (may be 0 if not supplied)
	 * @param	thicknessmm	SliceThickness and ImagedVolumeDepth value (in mm) to use (may be 0 if not supplied)
	 * @param	includeCopyOfImageDescription	whether or not to copy TIFF ImageDescription into ImageComments
	 * @param	uidFileName		CSV file mapping filename and IFD number (from 0) to keyword and UID pair, may be null
	 * @exception			IOException
	 * @exception			DicomException
	 * @exception			TIFFException
	 * @exception			NumberFormatException
	 */
	public TIFFToDicom(String jsonfile,String inputFileName,String outputFilePrefix,String outputFileSuffix,String modality,String sopClass,String transferSyntax,boolean addTIFF,boolean useBigTIFF,boolean alwaysWSI,boolean addPyramid,boolean mergeStrips,boolean autoRecognize,String channelFileName,boolean includeFileName,double spacingrowmm,double spacingcolmm,double thicknessmm,boolean includeCopyOfImageDescription,String uidFileName)
			throws IOException, DicomException, TIFFException, NumberFormatException {
		
		TIFFImageFileDirectories ifds = new TIFFImageFileDirectories();
		ifds.read(inputFileName);
		ArrayList<TIFFImageFileDirectory> ifdlist = ifds.getListOfImageFileDirectories();

		if (autoRecognize) {
			if (sopClass == null || sopClass.length() == 0) {
				String autoSOPClass = selectSOPClassBasedOnTIFFCharacteristics(ifds);
				if (autoSOPClass != null && autoSOPClass.length() > 0) {
					sopClass = autoSOPClass;
					slf4jlogger.info("autoRecognize SOP Class {}",sopClass);
					if (modality == null || modality.length() == 0) {
						// only if autorecognized SOP Class ...
						modality = SOPClass.selectModalityForSOPClass(sopClass);
						slf4jlogger.info("autoRecognize Modality {} for SOP Class {}",modality,sopClass);
					}
				}
				else {
					slf4jlogger.warn("autoRecognize SOP Class failed to find any hints");
				}
			}
			else {
				slf4jlogger.warn("autoRecognize requested but not performed since overridden by supplied SOP Class");
			}
		}
		
		boolean isWSI = alwaysWSI || sopClass != null && sopClass.equals(SOPClass.VLWholeSlideMicroscopyImageStorage);	// (001321)
		slf4jlogger.debug("isWSI={}",isWSI);
		
		String pyramidUID = null;
		String acquisitionUID = null;
		if (isWSI) {
			pyramidUID = u.getAnotherNewUID();
			acquisitionUID = u.getAnotherNewUID();
		}
		
		byte[] iccProfileOfBaseLayer = null;

		AttributeList descriptionList = new AttributeList();		// keep track of stuff defined once but reusable for subsequent images
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.SeriesInstanceUID); a.addValue(u.getAnotherNewUID()); descriptionList.put(a); }	// (001163)
		{ Attribute a = new UniqueIdentifierAttribute(TagFromName.StudyInstanceUID); a.addValue(u.getAnotherNewUID()); descriptionList.put(a); }	// (001163)
		
		// not only add common date and time for StudyDate etc., but track per IFD in case want to use for FrameAcquisitionDateTime TBD. :(
		String[] dateTimeStringByIFDNumber = addCommonDateTimeInformation(ifdlist,descriptionList);	// (001340)

		String containerIdentifier = "SLIDE_1";		// may be overridden except by SetCharacteristicsFromSummary
		String specimenIdentifier = "SPECIMEN_1";	// may be overridden except by SetCharacteristicsFromSummary
		String specimenUID = u.getAnotherNewUID();
		
		String[][] imageFlavorAndDerivationByIFD = getImageFlavorAndDerivationByIFD(ifdlist);

		WSIFrameOfReference wsifor = new WSIFrameOfReference(ifdlist,imageFlavorAndDerivationByIFD,spacingrowmm,spacingcolmm,thicknessmm);	// (001315),(001316),(001327)
		double sliceThickness = wsifor.getSliceThickness();
		
		String pastHistoryOfLossyCompression = getPastHistoryOfLossyCompression(ifdlist);

		Map<String,AttributeList> opticalPathAttributesByChannelID = null;
		Map<String,SequenceAttribute> specimenPreparationStepContentItemSequenceByChannelID = null;
		{
			Immunostaining immunostaining = null;
			if (channelFileName != null && channelFileName.length() > 0) {
				try {
					immunostaining = new Immunostaining(new File(channelFileName));
				}
				catch (Exception e) {
					slf4jlogger.error("Failed to read and process Immunostaining channel file "+channelFileName+": ",e);
				}
			}
			if (immunostaining == null) {	// whether no channel file, or reading/parsing it failed
				immunostaining = new Immunostaining(wsifor.getChannelNamesByChannelID());
			}
			if (immunostaining != null) {
				opticalPathAttributesByChannelID = immunostaining.getMapOfOpticalPathAttributesByChannelID();
				specimenPreparationStepContentItemSequenceByChannelID = immunostaining.getMapOfSpecimenPreparationStepContentItemSequenceByChannelID();
				// does not contain Specimen Identifier - will need to be added later
				// may still be null
			}
		}
		
		// (001398)
		// Assumes all the equipment info is in the 1st IFD rather than searching them all and consolidating :(
		{
			TIFFImageFileDirectory ifd = ifdlist.get(0);
			if (ifd != null) {
				String[] imageDescriptionArray = ifd.getStringValues(TIFFTags.IMAGEDESCRIPTION);
				getEquipmentFromTIFFImageDescription(imageDescriptionArray,descriptionList);
			}
			if (Attribute.getSingleStringValueOrEmptyString(descriptionList,TagFromName.DeviceSerialNumber).length() == 0) {
				String deviceSerialNumber = new java.rmi.dgc.VMID().toString();
				slf4jlogger.debug("No deviceSerialNumber found so synthesizing = {}",deviceSerialNumber);
				Attribute a = new LongStringAttribute(TagFromName.DeviceSerialNumber);
				a.addValue(deviceSerialNumber);
				descriptionList.put(a);
			}
		}
		
		UIDMap uidMap = uidFileName == null ? null : new UIDMap(uidFileName);
		
		int dirNum = 0;
		for (TIFFImageFileDirectory ifd : ifdlist) {
			slf4jlogger.debug("Directory={}",dirNum);
		
			// SubFileType (254) LONG (4) 1<0>
			long imageWidth = ifd.getSingleNumericValue(TIFFTags.IMAGEWIDTH,0,0);
			slf4jlogger.debug("imageWidth={}",imageWidth);
			long imageLength = ifd.getSingleNumericValue(TIFFTags.IMAGELENGTH,0,0);
			slf4jlogger.debug("imageLength={}",imageLength);
			long bitsPerSample = ifd.getSingleNumericValue(TIFFTags.BITSPERSAMPLE,0,0);
			slf4jlogger.debug("bitsPerSample={}",bitsPerSample);
			long compression = ifd.getSingleNumericValue(TIFFTags.COMPRESSION,0,0);
			slf4jlogger.debug("compression={}",compression);
			long photometric = ifd.getSingleNumericValue(TIFFTags.PHOTOMETRIC,0,0);
			slf4jlogger.debug("photometric={}",photometric);
			// Orientation (274) SHORT (3) 1<1>
			long samplesPerPixel = ifd.getSingleNumericValue(TIFFTags.SAMPLESPERPIXEL,0,0);
			slf4jlogger.debug("samplesPerPixel={}",samplesPerPixel);

			long planarConfig = ifd.getSingleNumericValue(TIFFTags.PLANARCONFIG,0,1);	// default is 1 (color-by-pixel not color-by-plane)
			slf4jlogger.debug("planarConfig={}",planarConfig);

			long sampleFormat = ifd.getSingleNumericValue(TIFFTags.SAMPLEFORMAT,0,1);	// assume unsigned if absent, and assume same for all samples (though that is not required)
			slf4jlogger.debug("sampleFormat={}",sampleFormat);

			byte[] jpegTables = null;
			if (compression == 7) {
				jpegTables = ifd.getByteValues(TIFFTags.JPEGTABLES);
				if (jpegTables != null) {
					slf4jlogger.debug("jpegTables present");
					jpegTables = stripSOIEOIMarkers(jpegTables);
				}
			}
			
			byte[] iccProfile = ifd.getByteValues(TIFFTags.ICCPROFILE);
			if (iccProfile != null) {
				slf4jlogger.debug("ICC profile present, of length {}",iccProfile.length);
			}
			if (iccProfile != null && iccProfile.length > 0) {
				if (dirNum == 0) {
					iccProfileOfBaseLayer = iccProfile;		// store this in case need not specified in subsequent layers
				}
			}
			else {
				if (isWSI && iccProfileOfBaseLayer != null && iccProfileOfBaseLayer.length > 0) {
					slf4jlogger.debug("ICC profile absent or empty so using profile of base layer");
					iccProfile = iccProfileOfBaseLayer;		// use base layer profile if not specified in subsequent layers
				}
			}
			
			boolean makeMultiFrame = SOPClass.isMultiframeImageStorage(sopClass);
			slf4jlogger.debug("makeMultiFrame={}",makeMultiFrame);

			// PageNumber (297) SHORT (3) 2<4 5>
			long tileWidth = ifd.getSingleNumericValue(TIFFTags.TILEWIDTH,0,0);
			slf4jlogger.debug("tileWidth={}",tileWidth);
			long tileLength = ifd.getSingleNumericValue(TIFFTags.TILELENGTH,0,0);
			slf4jlogger.debug("tileLength={}",tileLength);
			
			long predictor = ifd.getSingleNumericValue(TIFFTags.PREDICTOR,0,0);
			slf4jlogger.debug("predictor={}",predictor);
			
			String imageDescription = null;
			{
				String[] imageDescriptionArray = ifd.getStringValues(TIFFTags.IMAGEDESCRIPTION);
				parseTIFFImageDescription(imageDescriptionArray,descriptionList);
				imageDescription = mergeImageDescription(imageDescriptionArray);
			}
			
			{
				String[] software = ifd.getStringValues(TIFFTags.SOFTWARE);	// (001391) seen in Philips DPUfsImport images
				if (software != null && software.length > 0) {
					// use as ManufacturerModelName if not extracted from ImageDescription
					// assume and use only one value :(
					if (descriptionList.get(TagFromName.ManufacturerModelName) == null) {
						slf4jlogger.debug("Using software[{}] as ManufacturerModelName {}",dirNum,software[0]);
						{ Attribute a = new LongStringAttribute(TagFromName.ManufacturerModelName); a.addValue(software[0]); descriptionList.put(a); }
					}
				}
			}

			if (includeFileName) {	// (001314)
				{ Attribute a = new LongStringAttribute(pixelmedPrivateOriginalFileNameDataBlockReservation); a.addValue(pixelmedPrivateCreator); descriptionList.put(a); }
				{ Attribute a = new UnlimitedTextAttribute(pixelmedPrivateOriginalFileName); a.addValue(inputFileName); descriptionList.put(a); }
				// OriginalTIFFIFDIndex is not actually common but will be overwritten with every pass through the loop, but misnamed (?) descriptionList is what is passed to convertTIFFPixelsToDicom*()
				{ Attribute a = new UnsignedShortAttribute(pixelmedPrivateOriginalTIFFIFDIndex); a.addValue(dirNum); descriptionList.put(a); }
			}

			String frameOfReferenceUID = wsifor.getFrameOfReferenceUIDForIFD(dirNum);	// (001256)
			slf4jlogger.debug("Using frameOfReferenceUID[{}] from WSI Frame of Reference {} to make DICOM file",dirNum,frameOfReferenceUID);
			double mmPerPixelX = wsifor.getmmPerPixelXForIFD(dirNum);
			slf4jlogger.debug("Using mmPerPixelX[{}] from WSI Frame of Reference {} to make DICOM file",dirNum,mmPerPixelX);
			double mmPerPixelY = wsifor.getmmPerPixelYForIFD(dirNum);
			slf4jlogger.debug("Using mmPerPixelX[{}] from WSI Frame of Reference {} to make DICOM file",dirNum,mmPerPixelY);
			double objectiveLensPower = wsifor.getObjectiveLensPower();
			slf4jlogger.debug("Using objectiveLensPower from WSI Frame of Reference {} to make DICOM file",objectiveLensPower);
			double objectiveLensNumericalAperture = wsifor.getObjectiveLensNumericalAperture();
			slf4jlogger.debug("Using objectiveLensNumericalAperture from WSI Frame of Reference {} to make DICOM file",objectiveLensNumericalAperture);

			String opticalPathIdentifier = null;
			String opticalPathDescription = null;
			AttributeList opticalPathAttributesForChannel = null;
			SequenceAttribute specimenPreparationStepContentItemSequence = null;
			int channel = wsifor.getChannelForIFD(dirNum);
			if (channel != -1) {
				opticalPathIdentifier = wsifor.getChannelIDForChannel(channel);	// this is NOT always the sequential index from 0 monotonically increasing by 1 (001323)
				slf4jlogger.debug("Using opticalPathIdentifier from WSI Frame of Reference {} to make DICOM file",opticalPathIdentifier);
				if (opticalPathIdentifier == null || opticalPathIdentifier.length() == 0) {
					opticalPathIdentifier = Integer.toString(channel);
					slf4jlogger.debug("In absence of opticalPathIdentifier from WSI Frame of Reference, using sequential channel index value",opticalPathIdentifier);
				}
				opticalPathDescription = wsifor.getChannelNameForChannel(channel);
				slf4jlogger.debug("Using opticalPathDescription from WSI Frame of Reference {} to make DICOM file",opticalPathDescription);
				
				if (opticalPathAttributesByChannelID != null) {
					opticalPathAttributesForChannel = opticalPathAttributesByChannelID.get(opticalPathIdentifier);
					if (opticalPathAttributesForChannel == null) {
						slf4jlogger.debug("Could not find Optical Path attributes for opticalPathIdentifier {}",opticalPathIdentifier);
					}
				}
				
				if (specimenPreparationStepContentItemSequenceByChannelID != null) {
					specimenPreparationStepContentItemSequence = specimenPreparationStepContentItemSequenceByChannelID.get(opticalPathIdentifier);	// (001323)
					if (specimenPreparationStepContentItemSequence == null) {
						slf4jlogger.debug("Could not find SpecimenPreparationStepContentItemSequence for opticalPathIdentifier {}",opticalPathIdentifier);
					}
				}
			}
			else {
				slf4jlogger.debug("Could not find channel to use for opticalPathIdentifier from WSI Frame of Reference {}",opticalPathIdentifier);
			}
			// else no information - cannot just set to dirNum, since may be sub-resolutions (e.g., in SVS) rather than multiple channels :(
			
			double xOffsetInSlideCoordinateSystem = wsifor.getXOffsetInSlideCoordinateSystemForIFD(dirNum);
			slf4jlogger.debug("Using xOffsetInSlideCoordinateSystem[{}] from WSI Frame of Reference {} to make DICOM file",dirNum,xOffsetInSlideCoordinateSystem);
			double yOffsetInSlideCoordinateSystem = wsifor.getYOffsetInSlideCoordinateSystemForIFD(dirNum);
			slf4jlogger.debug("Using yOffsetInSlideCoordinateSystem[{}] from WSI Frame of Reference {} to make DICOM file",dirNum,yOffsetInSlideCoordinateSystem);

			try {
				long[] tileOffsets = ifd.getNumericValues(TIFFTags.TILEOFFSETS);
				long[] tileByteCounts = ifd.getNumericValues(TIFFTags.TILEBYTECOUNTS);
				if (tileOffsets != null) {
					int numberOfTiles = tileOffsets.length;
					if (tileByteCounts.length != numberOfTiles) {
						throw new TIFFException("Number of tiles uncertain: tileOffsets length = "+tileOffsets.length+" different from tileByteCounts length "+tileByteCounts.length);
					}
					slf4jlogger.debug("numberOfTiles={}",numberOfTiles);
					if (makeMultiFrame) {
						String outputFileName = outputFilePrefix + "_" + dirNum + outputFileSuffix;
						slf4jlogger.info("outputFileName={}",outputFileName);
						int instanceNumber = dirNum+1;
						convertTIFFPixelsToDicomMultiFrame(jsonfile,uidMap,ifds.getFile(),dirNum,outputFileName,instanceNumber,imageWidth,imageLength,tileOffsets,tileByteCounts,tileWidth,tileLength,bitsPerSample,compression,jpegTables,iccProfile,photometric,samplesPerPixel,planarConfig,sampleFormat,predictor,
														  frameOfReferenceUID,mmPerPixelX,mmPerPixelY,sliceThickness,objectiveLensPower,objectiveLensNumericalAperture,
														  opticalPathIdentifier,opticalPathDescription,opticalPathAttributesForChannel,
														  xOffsetInSlideCoordinateSystem,yOffsetInSlideCoordinateSystem,
														  modality,sopClass,transferSyntax,containerIdentifier,specimenIdentifier,specimenUID,imageFlavorAndDerivationByIFD[dirNum][0],imageFlavorAndDerivationByIFD[dirNum][1],
														  imageDescription,descriptionList,addTIFF,useBigTIFF,alwaysWSI,addPyramid,specimenPreparationStepContentItemSequence,pyramidUID,acquisitionUID,includeCopyOfImageDescription,pastHistoryOfLossyCompression);
					}
					else {
						for (int tileNumber=0; tileNumber<numberOfTiles; ++tileNumber) {
							String outputFileName = outputFilePrefix + "_" + dirNum + "_" + tileNumber + outputFileSuffix;
							slf4jlogger.info("outputFileName={}",outputFileName);
							int instanceNumber = (dirNum+1)*100000+(tileNumber+1);
							convertTIFFPixelsToDicomSingleFrame(jsonfile,uidMap,ifds.getFile(),dirNum,outputFileName,instanceNumber,imageWidth,imageLength,tileOffsets[tileNumber],tileByteCounts[tileNumber],tileWidth,tileLength,bitsPerSample,compression,
													jpegTables,iccProfile,photometric,samplesPerPixel,planarConfig,sampleFormat,predictor,
													frameOfReferenceUID,mmPerPixelX,mmPerPixelY,sliceThickness,objectiveLensPower,objectiveLensNumericalAperture,
													opticalPathIdentifier,opticalPathDescription,opticalPathAttributesForChannel,
													xOffsetInSlideCoordinateSystem,yOffsetInSlideCoordinateSystem,
													modality,sopClass,transferSyntax,containerIdentifier,specimenIdentifier,specimenUID,imageFlavorAndDerivationByIFD[dirNum][0],imageFlavorAndDerivationByIFD[dirNum][1],
													imageDescription,descriptionList,addTIFF,useBigTIFF,alwaysWSI,specimenPreparationStepContentItemSequence,pyramidUID,acquisitionUID,includeCopyOfImageDescription,pastHistoryOfLossyCompression);
						}
					}
				}
				else {
					long rowsPerStrip = ifd.getSingleNumericValue(TIFFTags.ROWSPERSTRIP,0,0);
					slf4jlogger.debug("rowsPerStrip={}",rowsPerStrip);
					long[] stripOffsets = ifd.getNumericValues(TIFFTags.STRIPOFFSETS);
					long[] stripByteCounts = ifd.getNumericValues(TIFFTags.STRIPBYTECOUNTS);
					if (stripByteCounts != null) {
						slf4jlogger.debug("Strips rather than tiled");
						int numberOfStrips = stripOffsets.length;
						slf4jlogger.debug("numberOfStrips={}",numberOfStrips);
						if (stripByteCounts.length != numberOfStrips) {
							throw new TIFFException("Number of strips uncertain: stripOffsets length = "+stripOffsets.length+" different from stripByteCounts length "+stripByteCounts.length);
						}
						if (rowsPerStrip == imageLength) {
							slf4jlogger.debug("Single strip for entire image");
							if (numberOfStrips != 1) {
								throw new TIFFException("Number of strips uncertain: stripOffsets length = "+stripOffsets.length+" > 1 but rowsPerStrip == imageLength of "+rowsPerStrip);
							}
							String outputFileName = outputFilePrefix + "_" + dirNum + outputFileSuffix;
							slf4jlogger.info("outputFileName={}",outputFileName);
							int instanceNumber = dirNum+1;
							if (compression == 5) {
								// pretend it is merging strips, since we want to force decompression, which is not supported by convertTIFFPixelsToDicomSingleFrame()
								convertTIFFPixelsToDicomSingleFrameMergingStrips(jsonfile,uidMap,ifds.getFile(),dirNum,outputFileName,instanceNumber,imageWidth,imageLength,stripOffsets,stripByteCounts,imageWidth,rowsPerStrip,bitsPerSample,compression,
													jpegTables,iccProfile,photometric,samplesPerPixel,planarConfig,sampleFormat,predictor,
													frameOfReferenceUID,mmPerPixelX,mmPerPixelY,sliceThickness,objectiveLensPower,objectiveLensNumericalAperture,
													opticalPathIdentifier,opticalPathDescription,opticalPathAttributesForChannel,
													xOffsetInSlideCoordinateSystem,yOffsetInSlideCoordinateSystem,
													modality,sopClass,TransferSyntax.ExplicitVRLittleEndian/*since always decompressed*/,containerIdentifier,specimenIdentifier,specimenUID,imageFlavorAndDerivationByIFD[dirNum][0],imageFlavorAndDerivationByIFD[dirNum][1],
													imageDescription,descriptionList,addTIFF,useBigTIFF,alwaysWSI,specimenPreparationStepContentItemSequence,pyramidUID,acquisitionUID,includeCopyOfImageDescription,pastHistoryOfLossyCompression);
							}
							else {
								convertTIFFPixelsToDicomSingleFrame(jsonfile,uidMap,ifds.getFile(),dirNum,outputFileName,instanceNumber,imageWidth,imageLength,stripOffsets[0],stripByteCounts[0],imageWidth,rowsPerStrip,bitsPerSample,compression,
													jpegTables,iccProfile,photometric,samplesPerPixel,planarConfig,sampleFormat,predictor,
													frameOfReferenceUID,mmPerPixelX,mmPerPixelY,sliceThickness,objectiveLensPower,objectiveLensNumericalAperture,
													opticalPathIdentifier,opticalPathDescription,opticalPathAttributesForChannel,
													xOffsetInSlideCoordinateSystem,yOffsetInSlideCoordinateSystem,
													modality,sopClass,transferSyntax,containerIdentifier,specimenIdentifier,specimenUID,imageFlavorAndDerivationByIFD[dirNum][0],imageFlavorAndDerivationByIFD[dirNum][1],
													imageDescription,descriptionList,addTIFF,useBigTIFF,alwaysWSI,specimenPreparationStepContentItemSequence,pyramidUID,acquisitionUID,includeCopyOfImageDescription,pastHistoryOfLossyCompression);
							}
						}
						else {
							if (mergeStrips && numberOfStrips > 1) {	// (001391) Philips DPUfsImport has single strip for compressed JPEG so don't decompress or try to merge it
								slf4jlogger.debug("Merging strips into single image");
								String outputFileName = outputFilePrefix + "_" + dirNum + outputFileSuffix;
								slf4jlogger.info("outputFileName={}",outputFileName);
								int instanceNumber = dirNum+1;
								// if merging strips, always output decompressed, since compression is per strip, don't have ability to merge compressed bitstreams, and do not want additional compression loss of recompressing
								convertTIFFPixelsToDicomSingleFrameMergingStrips(jsonfile,uidMap,ifds.getFile(),dirNum,outputFileName,instanceNumber,imageWidth,imageLength,stripOffsets,stripByteCounts,imageWidth,rowsPerStrip,bitsPerSample,compression,
													jpegTables,iccProfile,photometric,samplesPerPixel,planarConfig,sampleFormat,predictor,
													frameOfReferenceUID,mmPerPixelX,mmPerPixelY,sliceThickness,objectiveLensPower,objectiveLensNumericalAperture,
													opticalPathIdentifier,opticalPathDescription,opticalPathAttributesForChannel,
													xOffsetInSlideCoordinateSystem,yOffsetInSlideCoordinateSystem,
													modality,sopClass,TransferSyntax.ExplicitVRLittleEndian/*since always decompressed*/,containerIdentifier,specimenIdentifier,specimenUID,imageFlavorAndDerivationByIFD[dirNum][0],imageFlavorAndDerivationByIFD[dirNum][1],
													imageDescription,descriptionList,addTIFF,useBigTIFF,alwaysWSI,specimenPreparationStepContentItemSequence,pyramidUID,acquisitionUID,includeCopyOfImageDescription,pastHistoryOfLossyCompression);
							}
							else {
								slf4jlogger.debug("Not merging strips or was only one strip anyway - each becomes single frame or image");
								if (makeMultiFrame) {
									String outputFileName = outputFilePrefix + "_" + dirNum + outputFileSuffix;
									slf4jlogger.info("outputFileName={}",outputFileName);
									int instanceNumber = dirNum+1;
									convertTIFFPixelsToDicomMultiFrame(jsonfile,uidMap,ifds.getFile(),dirNum,outputFileName,instanceNumber,imageWidth,imageLength,stripOffsets,stripByteCounts,imageWidth,rowsPerStrip,bitsPerSample,compression,jpegTables,iccProfile,photometric,samplesPerPixel,planarConfig,sampleFormat,predictor,
													frameOfReferenceUID,mmPerPixelX,mmPerPixelY,sliceThickness,objectiveLensPower,objectiveLensNumericalAperture,
													opticalPathIdentifier,opticalPathDescription,opticalPathAttributesForChannel,
													xOffsetInSlideCoordinateSystem,yOffsetInSlideCoordinateSystem,
													modality,sopClass,transferSyntax,containerIdentifier,specimenIdentifier,specimenUID,imageFlavorAndDerivationByIFD[dirNum][0],imageFlavorAndDerivationByIFD[dirNum][1],
													imageDescription,descriptionList,addTIFF,useBigTIFF,alwaysWSI,addPyramid,specimenPreparationStepContentItemSequence,pyramidUID,acquisitionUID,includeCopyOfImageDescription,pastHistoryOfLossyCompression);
								}
								else {
									for (int stripNumber=0; stripNumber<numberOfStrips; ++stripNumber) {
										String outputFileName = outputFilePrefix + "_" + dirNum + "_" + stripNumber + outputFileSuffix;
										slf4jlogger.info("outputFileName={}",outputFileName);
										int instanceNumber = (dirNum+1)*100000+(stripNumber+1);
										convertTIFFPixelsToDicomSingleFrame(jsonfile,uidMap,ifds.getFile(),dirNum,outputFileName,instanceNumber,imageWidth,imageLength,stripOffsets[stripNumber],stripByteCounts[stripNumber],imageWidth,rowsPerStrip,bitsPerSample,compression,
													jpegTables,iccProfile,photometric,samplesPerPixel,planarConfig,sampleFormat,predictor,
													frameOfReferenceUID,mmPerPixelX,mmPerPixelY,sliceThickness,objectiveLensPower,objectiveLensNumericalAperture,
													opticalPathIdentifier,opticalPathDescription,opticalPathAttributesForChannel,
													xOffsetInSlideCoordinateSystem,yOffsetInSlideCoordinateSystem,
													modality,sopClass,transferSyntax,containerIdentifier,specimenIdentifier,specimenUID,imageFlavorAndDerivationByIFD[dirNum][0],imageFlavorAndDerivationByIFD[dirNum][1],
													imageDescription,descriptionList,addTIFF,useBigTIFF,alwaysWSI,specimenPreparationStepContentItemSequence,pyramidUID,acquisitionUID,includeCopyOfImageDescription,pastHistoryOfLossyCompression);
									}
								}
							}
						}
					}
					else {
						throw new TIFFException("Unsupported encoding");
					}
				}
			}
			catch (Exception e) {
				slf4jlogger.error("Failed to construct DICOM image: ",e);
			}
			++dirNum;
		}
	}

	/**
	 * <p>Read a TIFF image input format file consisting of one or more pages or tiles, and create one or more images of a specified or appropriate SOP Class.</p>
	 *
	 * <p>Options are:</p>
	 * <p>ADDTIFF | DONOTADDTIFF (default)</p>
	 * <p>USEBIGTIFF (default) | DONOTUSEBIGTIFF</p>
	 * <p>ALWAYSWSI | NOTALWAYSWSI (default)</p>
	 * <p>ADDPYRAMID | DONOTADDPYRAMID (default)</p>
	 * <p>MERGESTRIPS (default) | DONOTMERGESTRIPS</p>
	 * <p>AUTORECOGNIZE (default) | DONOTAUTORECOGNIZE</p>
	 * <p>ADDDCMSUFFIX (default) | DONOTADDDCMSUFFIX</p>
	 * <p>INCLUDEFILENAME (default) | DONOTINCLUDEFILENAME</p>
	 * <p>INCLUDEIMAGEDESCRIPTION (default) | DONOTINCLUDEIMAGEDESCRIPTION</p>
	 * <p>CHANNELFILE channelfile</p>
	 * <p>SPACINGMM spacingmm</p>
	 * <p>SPACINGROWCOLMM spacingrowmm(y) spacingcolmm(x)</p>
	 * <p>THICKNESSMM thickness</p>
	 * <p>UIDFILE uidfile</p>
	 *
	 * @param	arg	three, four, five or six parameters plus options, a JSON file describing the functional groups and attributes and values to be added or replaced, the TIFF inputFile, DICOM file outputFilePrefix, and optionally the modality, the SOP Class, and the Transfer Syntax to use if multi-frame, then various options controlling conversion including an optional channel file describing content of each channel, and spacing and thickness information
	 */
	public static void main(String arg[]) {
		try {
			boolean addTIFF = false;
			boolean useBigTIFF = true;
			boolean alwaysWSI = false;
			boolean addPyramid = false;
			boolean mergeStrips = true;
			boolean autoRecognize = true;
			boolean includeFileName = false;
			boolean includeCopyOfImageDescription = true;	// (001347)
			
			String outputFileSuffix = ".dcm";
			
			String channelFileName = null;
			double spacingcolmm = 0;
			double spacingrowmm = 0;
			double thicknessmm = 0;
			String uidFileName = null;

			int numberOfFixedArguments = 3;
			int numberOfFixedAndOptionalArguments = 6;
			int endOptionsPosition = arg.length;
			boolean bad = false;
			
			if (endOptionsPosition < numberOfFixedArguments) {
				bad = true;
			}
			boolean keepLooking = true;
			while (keepLooking && endOptionsPosition > numberOfFixedArguments) {
				String option = arg[endOptionsPosition-1].trim().toUpperCase();
				switch (option) {
					case "ADDTIFF":				addTIFF = true;  --endOptionsPosition; break;
					case "DONOTADDTIFF":		addTIFF = false; --endOptionsPosition; break;
					
					case "USEBIGTIFF":			useBigTIFF = true;  --endOptionsPosition; break;
					case "DONOTUSEBIGTIFF":		useBigTIFF = false; --endOptionsPosition; break;
					
					case "ALWAYSWSI":			alwaysWSI = true;  --endOptionsPosition; break;
					case "NOTALWAYSWSI":		alwaysWSI = false; --endOptionsPosition; break;

					case "ADDPYRAMID":			addPyramid = true;  --endOptionsPosition; break;
					case "DONOTADDPYRAMID":		addPyramid = false; --endOptionsPosition; break;

					case "MERGESTRIPS":			mergeStrips = true;  --endOptionsPosition; break;
					case "DONOTMERGESTRIPS":	mergeStrips = false; --endOptionsPosition; break;

					case "AUTORECOGNIZE":		autoRecognize = true;  --endOptionsPosition; break;
					case "DONOTAUTORECOGNIZE":	autoRecognize = false; --endOptionsPosition; break;

					case "ADDDCMSUFFIX":		outputFileSuffix = ".dcm"; --endOptionsPosition; break;
					case "DONOTADDDCMSUFFIX":	outputFileSuffix = "";     --endOptionsPosition; break;

					case "INCLUDEFILENAME":		includeFileName = true;  --endOptionsPosition; break;
					case "DONOTINCLUDEFILENAME":includeFileName = false; --endOptionsPosition; break;

					case "INCLUDEIMAGEDESCRIPTION":			includeCopyOfImageDescription = true;  --endOptionsPosition; break;	// (001347)
					case "DONOTINCLUDEIMAGEDESCRIPTION":	includeCopyOfImageDescription = false; --endOptionsPosition; break;

					default:	if (arg[endOptionsPosition-2].trim().toUpperCase().equals("CHANNELFILE")) {	// (001313)
									channelFileName = arg[endOptionsPosition-1];
									endOptionsPosition-=2;
								}
								else if (arg[endOptionsPosition-2].trim().toUpperCase().equals("SPACINGMM")) {	// (001316)
									try {
										spacingcolmm = Double.parseDouble(arg[endOptionsPosition-1]);
										spacingrowmm = spacingcolmm;
									}
									catch (NumberFormatException e) {
										slf4jlogger.error("Spacing value is invalid",e);
									}
									endOptionsPosition-=2;
								}
								else if (arg[endOptionsPosition-3].trim().toUpperCase().equals("SPACINGROWCOLMM")) {	// (001327)
									try {
										spacingrowmm = Double.parseDouble(arg[endOptionsPosition-2]);
										spacingcolmm = Double.parseDouble(arg[endOptionsPosition-1]);
									}
									catch (NumberFormatException e) {
										slf4jlogger.error("Spacing value is invalid",e);
									}
									endOptionsPosition-=3;
								}
								else if (arg[endOptionsPosition-2].trim().toUpperCase().equals("THICKNESSMM")) {	// (001315)
									try {
										thicknessmm = Double.parseDouble(arg[endOptionsPosition-1]);
									}
									catch (NumberFormatException e) {
										slf4jlogger.error("Thickness value is invalid",e);
									}
									endOptionsPosition-=2;
								}
								else if (arg[endOptionsPosition-2].trim().toUpperCase().equals("UIDFILE")) {	// (001360)
									uidFileName = arg[endOptionsPosition-1];
									endOptionsPosition-=2;
								}
								else if (endOptionsPosition > numberOfFixedAndOptionalArguments) {
									slf4jlogger.error("Unrecognized argument {}",option);
									bad = true;
									keepLooking = false;
								}
								else {
									keepLooking = false;
								}
								break;
				}
			}
			
			if (!bad) {
				String jsonfile = arg[0];
				String inputFile = arg[1];
				String outputFilePrefix = arg[2];
				String modality = null;
				String sopClass = null;
				String transferSyntax = null;

				if (endOptionsPosition >= 4) {
					modality = arg[3];
				}
				if (endOptionsPosition >= 5) {
					sopClass = arg[4];
				}
				if (endOptionsPosition >= 6) {
					transferSyntax = arg[5];
				}

				slf4jlogger.debug("modality = {}",modality);
				slf4jlogger.debug("sopClass = {}",sopClass);
				slf4jlogger.debug("transferSyntax = {}",transferSyntax);
				slf4jlogger.debug("channelFileName = {}",channelFileName);
				slf4jlogger.debug("spacingrowmm = {}",spacingrowmm);
				slf4jlogger.debug("spacingcolmm = {}",spacingcolmm);
				slf4jlogger.debug("thicknessmm = {}",thicknessmm);
				slf4jlogger.debug("uidFileName = {}",uidFileName);

				new TIFFToDicom(jsonfile,inputFile,outputFilePrefix,outputFileSuffix,modality,sopClass,transferSyntax,addTIFF,useBigTIFF,alwaysWSI,addPyramid,mergeStrips,autoRecognize,channelFileName,includeFileName,spacingrowmm,spacingcolmm,thicknessmm,includeCopyOfImageDescription,uidFileName);
			}
			else {
				System.err.println("Error: Incorrect number of arguments or bad arguments");
				System.err.println("Usage: TIFFToDicom jsonfile inputFile outputFilePrefix [modality [SOPClass [TransferSyntax]]]"
					+" [ADDTIFF|DONOTADDTIFF]"
					+" [USEBIGTIFF|DONOTUSEBIGTIFF]"
					+" [ALWAYSWSI|NOTALWAYSWSI]"
					+" [ADDPYRAMID|DONOTADDPYRAMID]"
					+" [MERGESTRIPS|SPLITSTRIPS]"
					+" [AUTORECOGNIZE|DONOTAUTORECOGNIZE]"
					+" [ADDDCMSUFFIX|DONOTADDDCMSUFFIX]"
					+" [INCLUDEFILENAME|DONOTINCLUDEFILENAME]"
					+" [INCLUDEIMAGEDESCRIPTION|DONOTINCLUDEIMAGEDESCRIPTION]"
					+" [CHANNELFILE channelfile]"
					+" [SPACINGMM spacingmm]"
					+" [SPACINGROWCOLMM spacingrowmm(y) spacingcolmm(x)]"
					+" [THICKNESSMM thickness]"
					+" [UIDFILE uidfile]"
				);
				System.exit(1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

