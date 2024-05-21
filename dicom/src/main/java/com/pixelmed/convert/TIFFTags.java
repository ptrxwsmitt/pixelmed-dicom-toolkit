package com.pixelmed.convert;

import java.util.HashMap;
import java.util.Map;

public class TIFFTags {

	// Derived from libtiff 4.0.3 tiff.h
	
	public static final int SUBFILETYPE = 254;	/* subfile data descriptor */
	public static final int OSUBFILETYPE = 255;	/* +kind of data in subfile */
	public static final int IMAGEWIDTH = 256;	/* image width in pixels */
	public static final int IMAGELENGTH = 257;	/* image height in pixels */
	public static final int BITSPERSAMPLE = 258;	/* bits per channel (sample) */
	public static final int COMPRESSION = 259;	/* data compression technique */
	public static final int PHOTOMETRIC = 262;	/* photometric interpretation */
	public static final int THRESHHOLDING = 263;	/* +thresholding used on data */
	public static final int CELLWIDTH = 264;	/* +dithering matrix width */
	public static final int CELLLENGTH = 265;	/* +dithering matrix height */
	public static final int FILLORDER = 266;	/* data order within a byte */
	public static final int DOCUMENTNAME = 269;	/* name of doc. image is from */
	public static final int IMAGEDESCRIPTION = 270;	/* */
	public static final int MAKE = 271;	/* */
	public static final int MODEL = 272;	/* */
	public static final int STRIPOFFSETS = 273;	/* offsets to data strips */
	public static final int ORIENTATION = 274;	/* +image orientation */
	public static final int SAMPLESPERPIXEL = 277;	/* samples per pixel */
	public static final int ROWSPERSTRIP = 278;	/* rows per strip of data */
	public static final int STRIPBYTECOUNTS = 279;	/* bytes counts for strips */
	public static final int MINSAMPLEVALUE = 280;	/* +minimum sample value */
	public static final int MAXSAMPLEVALUE = 281;	/* +maximum sample value */
	public static final int XRESOLUTION = 282;	/* pixels/resolution in x */
	public static final int YRESOLUTION = 283;	/* pixels/resolution in y */
	public static final int PLANARCONFIG = 284;	/* storage organization */
	public static final int PAGENAME = 285;	/* page name image is from */
	public static final int XPOSITION = 286;	/* x page offset of image lhs */
	public static final int YPOSITION = 287;	/* y page offset of image lhs */
	public static final int FREEOFFSETS = 288;	/* +byte offset to free block */
	public static final int FREEBYTECOUNTS = 289;	/* +sizes of free blocks */
	public static final int GRAYRESPONSEUNIT = 290;	/* $gray scale curve accuracy */
	public static final int GRAYRESPONSECURVE = 291;	/* $gray scale response curve */
	public static final int GROUP3OPTIONS = 292;	/* 32 flag bits */
	public static final int T4OPTIONS = 292;	/* TIFF 6.0 proper name alias */
	public static final int GROUP4OPTIONS = 293;	/* 32 flag bits */
	public static final int T6OPTIONS = 293;	/* TIFF 6.0 proper name */
	public static final int RESOLUTIONUNIT = 296;	/* units of resolutions */
	public static final int PAGENUMBER = 297;	/* page numbers of multi-page */
	public static final int COLORRESPONSEUNIT = 300;	/* $color curve accuracy */
	public static final int TRANSFERFUNCTION = 301;	/* !colorimetry info */
	public static final int SOFTWARE = 305;	/* name & release */
	public static final int DATETIME = 306;	/* creation date and time */
	public static final int ARTIST = 315;	/* creator of image */
	public static final int HOSTCOMPUTER = 316;	/* machine where created */
	public static final int PREDICTOR = 317;	/* prediction scheme w/ LZW */
	public static final int WHITEPOINT = 318;	/* image white point */
	public static final int PRIMARYCHROMATICITIES = 319;	/* !primary chromaticities */
	public static final int COLORMAP = 320;	/* RGB map for pallette image */
	public static final int HALFTONEHINTS = 321;	/* !highlight+shadow info */
	public static final int TILEWIDTH = 322;	/* !tile width in pixels */
	public static final int TILELENGTH = 323;	/* !tile height in pixels */
	public static final int TILEOFFSETS = 324;	/* !offsets to data tiles */
	public static final int TILEBYTECOUNTS = 325;	/* !byte counts for tiles */
	public static final int BADFAXLINES = 326;	/* lines w/ wrong pixel count */
	public static final int CLEANFAXDATA = 327;	/* regenerated line info */
	public static final int CONSECUTIVEBADFAXLINES = 328;	/* max consecutive bad lines */
	public static final int SUBIFD = 330;	/* subimage descriptors */
	public static final int INKSET = 332;	/* !inks in separated image */
	public static final int INKNAMES = 333;	/* !ascii names of inks */
	public static final int NUMBEROFINKS = 334;	/* !number of inks */
	public static final int DOTRANGE = 336;	/* !0% and 100% dot codes */
	public static final int TARGETPRINTER = 337;	/* !separation target */
	public static final int EXTRASAMPLES = 338;	/* !info about extra samples */
	public static final int SAMPLEFORMAT = 339;	/* !data sample format */
	public static final int SMINSAMPLEVALUE = 340;	/* !variable MinSampleValue */
	public static final int SMAXSAMPLEVALUE = 341;	/* !variable MaxSampleValue */
	public static final int CLIPPATH = 343;	/* %ClipPath [Adobe TIFF technote 2] */
	public static final int XCLIPPATHUNITS = 344;	/* %XClipPathUnits [Adobe TIFF technote 2] */
	public static final int YCLIPPATHUNITS = 345;	/* %YClipPathUnits [Adobe TIFF technote 2] */
	public static final int INDEXED = 346;	/* %Indexed [Adobe TIFF Technote 3] */
	public static final int JPEGTABLES = 347;	/* %JPEG table stream */
	public static final int OPIPROXY = 351;	/* %OPI Proxy [Adobe TIFF technote] */
	public static final int GLOBALPARAMETERSIFD = 400;	/* ! */
	public static final int PROFILETYPE = 401;	/* ! */
	public static final int FAXPROFILE = 402;	/* ! */
	public static final int CODINGMETHODS = 403;	/* !TIFF/FX coding methods */
	public static final int VERSIONYEAR = 404;	/* !TIFF/FX version year */
	public static final int MODENUMBER = 405;	/* !TIFF/FX mode number */
	public static final int DECODE = 433;	/* !TIFF/FX decode */
	public static final int IMAGEBASECOLOR = 434;	/* !TIFF/FX image base colour */
	public static final int T82OPTIONS = 435;	/* !TIFF/FX T.82 options */
	public static final int JPEGPROC = 512;	/* !JPEG processing algorithm */
	public static final int JPEGIFOFFSET = 513;	/* !pointer to SOI marker */
	public static final int JPEGIFBYTECOUNT = 514;	/* !JFIF stream length */
	public static final int JPEGRESTARTINTERVAL = 515;	/* !restart interval length */
	public static final int JPEGLOSSLESSPREDICTORS = 517;	/* !lossless proc predictor */
	public static final int JPEGPOINTTRANSFORM = 518;	/* !lossless point transform */
	public static final int JPEGQTABLES = 519;	/* !Q matrice offsets */
	public static final int JPEGDCTABLES = 520;	/* !DCT table offsets */
	public static final int JPEGACTABLES = 521;	/* !AC coefficient offsets */
	public static final int YCBCRCOEFFICIENTS = 529;	/* !RGB -> YCbCr transform */
	public static final int YCBCRSUBSAMPLING = 530;	/* !YCbCr subsampling factors */
	public static final int YCBCRPOSITIONING = 531;	/* !subsample positioning */
	public static final int REFERENCEBLACKWHITE = 532;	/* !colorimetry info */
	public static final int STRIPROWCOUNTS = 559;	/* !TIFF/FX strip row counts */
	public static final int XMLPACKET = 700;	/* %XML packet [Adobe XMP Specification, January 2004 */
	public static final int OPIIMAGEID = 32781;	/* %OPI ImageID [Adobe TIFF technote] */
	public static final int REFPTS = 32953;	/* image reference points */
	public static final int REGIONTACKPOINT = 32954;	/* region-xform tack point */
	public static final int REGIONWARPCORNERS = 32955;	/* warp quadrilateral */
	public static final int REGIONAFFINE = 32956;	/* affine transformation mat */
	public static final int MATTEING = 32995;	/* $use ExtraSamples */
	public static final int DATATYPE = 32996;	/* $use SampleFormat */
	public static final int IMAGEDEPTH = 32997;	/* z depth of image */
	public static final int TILEDEPTH = 32998;	/* z depth/data tile */
	public static final int PIXAR_IMAGEFULLWIDTH = 33300;	/* full image size in x */
	public static final int PIXAR_IMAGEFULLLENGTH = 33301;	/* full image size in y */
	public static final int PIXAR_TEXTUREFORMAT = 33302;	/* texture map format */
	public static final int PIXAR_WRAPMODES = 33303;	/* s & t wrap modes */
	public static final int PIXAR_FOVCOT = 33304;	/* cotan(fov) for env. maps */
	public static final int PIXAR_MATRIX_WORLDTOSCREEN = 33305;	/*  */
	public static final int PIXAR_MATRIX_WORLDTOCAMERA = 33306;	/*  */
	public static final int WRITERSERIALNUMBER = 33405;	/* device serial number */
	public static final int COPYRIGHT = 33432;	/* copyright string */
	public static final int RICHTIFFIPTC = 33723;	/*  */
	public static final int IT8SITE = 34016;	/* site name */
	public static final int IT8COLORSEQUENCE = 34017;	/* color seq. [RGB,CMYK,etc] */
	public static final int IT8HEADER = 34018;	/* DDES Header */
	public static final int IT8RASTERPADDING = 34019;	/* raster scanline padding */
	public static final int IT8BITSPERRUNLENGTH = 34020;	/* # of bits in short run */
	public static final int IT8BITSPEREXTENDEDRUNLENGTH = 34021;	/*  *//* # of bits in long run */
	public static final int IT8COLORTABLE = 34022;	/* LW colortable */
	public static final int IT8IMAGECOLORINDICATOR = 34023;	/* BP/BL image color switch */
	public static final int IT8BKGCOLORINDICATOR = 34024;	/* BP/BL bg color switch */
	public static final int IT8IMAGECOLORVALUE = 34025;	/* BP/BL image color value */
	public static final int IT8BKGCOLORVALUE = 34026;	/* BP/BL bg color value */
	public static final int IT8PIXELINTENSITYRANGE = 34027;	/* MP pixel intensity value */
	public static final int IT8TRANSPARENCYINDICATOR = 34028;	/* HC transparency switch */
	public static final int IT8COLORCHARACTERIZATION = 34029;	/* color character. table */
	public static final int IT8HCUSAGE = 34030;	/* HC usage indicator */
	public static final int IT8TRAPINDICATOR = 34031;	/* Trapping indicator */
	public static final int IT8CMYKEQUIVALENT = 34032;	/* CMYK color equivalents */
	public static final int FRAMECOUNT = 34232;	/* Sequence Frame Count */
	public static final int PHOTOSHOP = 34377;	/*  */
	public static final int EXIFIFD = 34665;	/* Pointer to EXIF private directory */
	public static final int ICCPROFILE = 34675;	/* ICC profile data */
	public static final int IMAGELAYER = 34732;	/* !TIFF/FX image layer information */
	public static final int JBIGOPTIONS = 34750;	/* JBIG options */
	public static final int GPSIFD = 34853;	/* Pointer to GPS private directory */
	public static final int FAXRECVPARAMS = 34908;	/* encoded Class 2 ses. parms */
	public static final int FAXSUBADDRESS = 34909;	/* received SubAddr string */
	public static final int FAXRECVTIME = 34910;	/* receive time (secs) */
	public static final int FAXDCS = 34911;	/* encoded fax ses. params, Table 2/T.30 */
/* tags 37439-37443 are registered to SGI <gregl@sgi.com> */
	public static final int STONITS = 37439;	/* Sample value to Nits */
/* tag 34929 is a private tag registered to FedEx */
	public static final int FEDEX_EDR = 34929;	/* unknown use */
	public static final int INTEROPERABILITYIFD = 40965;	/* Pointer to Interoperability private directory */
/* Adobe Digital Negative (DNG) format tags */
	public static final int DNGVERSION = 50706;	/* &DNG version number */
	public static final int DNGBACKWARDVERSION = 50707;	/* &DNG compatibility version */
	public static final int UNIQUECAMERAMODEL = 50708;	/* &name for the camera model */
	public static final int LOCALIZEDCAMERAMODEL = 50709;	/* &localized camera model name */
	public static final int CFAPLANECOLOR = 50710;	/* &CFAPattern->LinearRaw space mapping */
	public static final int CFALAYOUT = 50711;	/* &spatial layout of the CFA */
	public static final int LINEARIZATIONTABLE = 50712;	/* &lookup table description */
	public static final int BLACKLEVELREPEATDIM = 50713;	/* &repeat pattern size for the BlackLevel tag */
	public static final int BLACKLEVEL = 50714;	/* &zero light encoding level */
	public static final int BLACKLEVELDELTAH = 50715;	/* &zero light encoding level differences (columns) */
	public static final int BLACKLEVELDELTAV = 50716;	/* &zero light encoding level differences (rows) */
	public static final int WHITELEVEL = 50717;	/* &fully saturated encoding level */
	public static final int DEFAULTSCALE = 50718;	/* &default scale factors */
	public static final int DEFAULTCROPORIGIN = 50719;	/* &origin of the final image area */
	public static final int DEFAULTCROPSIZE = 50720;	/* &size of the final image area */
	public static final int COLORMATRIX1 = 50721;	/* &XYZ->reference color space transformation matrix 1 */
	public static final int COLORMATRIX2 = 50722;	/* &XYZ->reference color space transformation matrix 2 */
	public static final int CAMERACALIBRATION1 = 50723;	/* &calibration matrix 1 */
	public static final int CAMERACALIBRATION2 = 50724;	/* &calibration matrix 2 */
	public static final int REDUCTIONMATRIX1 = 50725;	/* &dimensionality reduction matrix 1 */
	public static final int REDUCTIONMATRIX2 = 50726;	/* &dimensionality reduction matrix 2 */
	public static final int ANALOGBALANCE = 50727;	/*  *//* &gain applied the stored raw values*/
	public static final int ASSHOTNEUTRAL = 50728;	/* &selected white balance in linear reference space */
	public static final int ASSHOTWHITEXY = 50729;	/* &selected white balance in x-y chromaticity coordinates */
	public static final int BASELINEEXPOSURE = 50730;	/* &how much to move the zero point */
	public static final int BASELINENOISE = 50731;	/* &relative noise level */
	public static final int BASELINESHARPNESS = 50732;	/* &relative amount of sharpening */
	public static final int BAYERGREENSPLIT = 50733;	/* &how closely the values ofthe green pixels in theblue/green rows track thevalues of the green pixelsin the red/green rows */
	public static final int LINEARRESPONSELIMIT = 50734;	/* &non-linear encoding range */
	public static final int CAMERASERIALNUMBER = 50735;	/* &camera's serial number */
	public static final int LENSINFO = 50736;	/* info about the lens */
	public static final int CHROMABLURRADIUS = 50737;	/* &chroma blur radius */
	public static final int ANTIALIASSTRENGTH = 50738;	/* &relative strength of the camera's anti-alias filter */
	public static final int SHADOWSCALE = 50739;	/* &used by Adobe Camera Raw */
	public static final int DNGPRIVATEDATA = 50740;	/* &manufacturer's private data */
	public static final int MAKERNOTESAFETY = 50741;	/* &whether the EXIF MakerNote tag is safe to preserve along with the rest of the EXIF data */
	public static final int CALIBRATIONILLUMINANT1 = 50778;	/* &illuminant 1 */
	public static final int CALIBRATIONILLUMINANT2 = 50779;	/* &illuminant 2 */
	public static final int BESTQUALITYSCALE = 50780;	/* &best quality multiplier */
	public static final int RAWDATAUNIQUEID = 50781;	/* &unique identifier for the raw image data */
	public static final int ORIGINALRAWFILENAME = 50827;	/* &file name of the original raw file */
	public static final int ORIGINALRAWFILEDATA = 50828;	/* &contents of the original raw file */
	public static final int ACTIVEAREA = 50829;	/* &active (non-masked) pixels of the sensor */
	public static final int MASKEDAREAS = 50830;	/* &list of coordinates of fully masked pixels */
	public static final int ASSHOTICCPROFILE = 50831;	/* &these two tags used to */
	public static final int ASSHOTPREPROFILEMATRIX = 50832;	/* map cameras's color space into ICC profile space */
	public static final int CURRENTICCPROFILE = 50833;	/* & */
	public static final int CURRENTPREPROFILEMATRIX = 50834;	/* & */
	public static final int DCSHUESHIFTVALUES = 65535;	/* hue shift correction data */

	// Derived from libtiff 4.0.3 tiffdump.cc
	
	public static class Description {
		int tag;
		String description;
		
		public Description(int tag,String description) {
			this.tag = tag;
			this.description = description;
		}
	}
	
	public static Description[] descriptions = {
		new Description(SUBFILETYPE,"SubFileType"),
		new Description(OSUBFILETYPE,"OldSubFileType"),
		new Description(IMAGEWIDTH,"ImageWidth"),
		new Description(IMAGELENGTH,"ImageLength"),
		new Description(BITSPERSAMPLE,"BitsPerSample"),
		new Description(COMPRESSION,"Compression"),
		new Description(PHOTOMETRIC,"Photometric"),
		new Description(THRESHHOLDING,"Threshholding"),
		new Description(CELLWIDTH,"CellWidth"),
		new Description(CELLLENGTH,"CellLength"),
		new Description(FILLORDER,"FillOrder"),
		new Description(DOCUMENTNAME,"DocumentName"),
		new Description(IMAGEDESCRIPTION,"ImageDescription"),
		new Description(MAKE,"Make"),
		new Description(MODEL,"Model"),
		new Description(STRIPOFFSETS,"StripOffsets"),
		new Description(ORIENTATION,"Orientation"),
		new Description(SAMPLESPERPIXEL,"SamplesPerPixel"),
		new Description(ROWSPERSTRIP,"RowsPerStrip"),
		new Description(STRIPBYTECOUNTS,"StripByteCounts"),
		new Description(MINSAMPLEVALUE,"MinSampleValue"),
		new Description(MAXSAMPLEVALUE,"MaxSampleValue"),
		new Description(XRESOLUTION,"XResolution"),
		new Description(YRESOLUTION,"YResolution"),
		new Description(PLANARCONFIG,"PlanarConfig"),
		new Description(PAGENAME,"PageName"),
		new Description(XPOSITION,"XPosition"),
		new Description(YPOSITION,"YPosition"),
		new Description(FREEOFFSETS,"FreeOffsets"),
		new Description(FREEBYTECOUNTS,"FreeByteCounts"),
		new Description(GRAYRESPONSEUNIT,"GrayResponseUnit"),
		new Description(GRAYRESPONSECURVE,"GrayResponseCurve"),
		new Description(GROUP3OPTIONS,"Group3Options"),
		new Description(GROUP4OPTIONS,"Group4Options"),
		new Description(RESOLUTIONUNIT,"ResolutionUnit"),
		new Description(PAGENUMBER,"PageNumber"),
		new Description(COLORRESPONSEUNIT,"ColorResponseUnit"),
		new Description(TRANSFERFUNCTION,"TransferFunction"),
		new Description(SOFTWARE,"Software"),
		new Description(DATETIME,"DateTime"),
		new Description(ARTIST,"Artist"),
		new Description(HOSTCOMPUTER,"HostComputer"),
		new Description(PREDICTOR,"Predictor"),
		new Description(WHITEPOINT,"Whitepoint"),
		new Description(PRIMARYCHROMATICITIES,"PrimaryChromaticities"),
		new Description(COLORMAP,"Colormap"),
		new Description(HALFTONEHINTS,"HalftoneHints"),
		new Description(TILEWIDTH,"TileWidth"),
		new Description(TILELENGTH,"TileLength"),
		new Description(TILEOFFSETS,"TileOffsets"),
		new Description(TILEBYTECOUNTS,"TileByteCounts"),
		new Description(BADFAXLINES,"BadFaxLines"),
		new Description(CLEANFAXDATA,"CleanFaxData"),
		new Description(CONSECUTIVEBADFAXLINES,"ConsecutiveBadFaxLines"),
		new Description(SUBIFD,"SubIFD"),
		new Description(INKSET,"InkSet"),
		new Description(INKNAMES,"InkNames"),
		new Description(NUMBEROFINKS,"NumberOfInks"),
		new Description(DOTRANGE,"DotRange"),
		new Description(TARGETPRINTER,"TargetPrinter"),
		new Description(EXTRASAMPLES,"ExtraSamples"),
		new Description(SAMPLEFORMAT,"SampleFormat"),
		new Description(SMINSAMPLEVALUE,"SMinSampleValue"),
		new Description(SMAXSAMPLEVALUE,"SMaxSampleValue"),
		new Description(JPEGPROC,"JPEGProcessingMode"),
		new Description(JPEGIFOFFSET,"JPEGInterchangeFormat"),
		new Description(JPEGIFBYTECOUNT,"JPEGInterchangeFormatLength"),
		new Description(JPEGRESTARTINTERVAL,"JPEGRestartInterval"),
		new Description(JPEGLOSSLESSPREDICTORS,"JPEGLosslessPredictors"),
		new Description(JPEGPOINTTRANSFORM,"JPEGPointTransform"),
		new Description(JPEGTABLES,"JPEGTables"),
		new Description(JPEGQTABLES,"JPEGQTables"),
		new Description(JPEGDCTABLES,"JPEGDCTables"),
		new Description(JPEGACTABLES,"JPEGACTables"),
		new Description(YCBCRCOEFFICIENTS,"YCbCrCoefficients"),
		new Description(YCBCRSUBSAMPLING,"YCbCrSubsampling"),
		new Description(YCBCRPOSITIONING,"YCbCrPositioning"),
		new Description(REFERENCEBLACKWHITE,"ReferenceBlackWhite"),
		new Description(REFPTS,"IgReferencePoints (Island Graphics)"),
		new Description(REGIONTACKPOINT,"IgRegionTackPoint (Island Graphics)"),
		new Description(REGIONWARPCORNERS,"IgRegionWarpCorners (Island Graphics)"),
		new Description(REGIONAFFINE,"IgRegionAffine (Island Graphics)"),
		new Description(MATTEING,"OBSOLETE Matteing (Silicon Graphics)"),
		new Description(DATATYPE,"OBSOLETE DataType (Silicon Graphics)"),
		new Description(IMAGEDEPTH,"ImageDepth (Silicon Graphics)"),
		new Description(TILEDEPTH,"TileDepth (Silicon Graphics)"),
		new Description(32768,"OLD BOGUS Matteing tag"),
		new Description(COPYRIGHT,"Copyright"),
		new Description(ICCPROFILE,"ICC Profile"),
		new Description(JBIGOPTIONS,"JBIG Options"),
		new Description(STONITS,"StoNits")
	};
	
	private static Map<Integer,String> indexOfDescriptionsByTag = null;
	
	private static void lazyInstantiationOfIndexOfDescriptionsByTag() {
		indexOfDescriptionsByTag = new HashMap<Integer,String>();
		for (Description entry : descriptions) {
			indexOfDescriptionsByTag.put(new Integer(entry.tag),entry.description);
		}
	}
	
	public static String getDescription(int tag) {
		if (indexOfDescriptionsByTag == null) {
			lazyInstantiationOfIndexOfDescriptionsByTag();
		}
		String description = indexOfDescriptionsByTag.get(new Integer(tag));
		if (description == null) {
			description = "--unrecognized--";
		}
		return description;
	}

}

