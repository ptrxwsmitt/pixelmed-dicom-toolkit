/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.convert;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.CodedSequenceItem;
import com.pixelmed.dicom.CodeStringAttribute;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.FloatSingleAttribute;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.UnlimitedTextAttribute;
import com.pixelmed.dicom.UnsignedShortAttribute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.pixelmed.slf4j.Logger;
import com.pixelmed.slf4j.LoggerFactory;

/**
 * <p>A class for encoding channel parameters describing immunostaining (including antibodies, fluorescence).</p>
 *
 * <p>Development of this class has been funded in part with Federal funds from the
 * National Cancer Institute, National Institutes of Health, under Task Order No.
 * HHSN26110071 under Contract No. HHSN261201500003l (Imaging Data Commons).
 *
 * @author	dclunie
 */

public class Immunostaining {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/convert/Immunostaining.java,v 1.19 2022/06/12 15:46:05 dclunie Exp $";

	private static final Logger slf4jlogger = LoggerFactory.getLogger(Immunostaining.class);
	
	private static Map<String,CodedSequenceItem> codedTargets = new HashMap<String,CodedSequenceItem>();
	private static Map<String,CodedSequenceItem> usingSubstance = new HashMap<String,CodedSequenceItem>();
	private static Map<String,CodedSequenceItem> codedFluorophores = new HashMap<String,CodedSequenceItem>();

	static {
		try {
			// matcher will capitalize key first
			codedTargets.put("NUCLEI",new CodedSequenceItem("84640000","SCT","Nucleus"));
			codedTargets.put("DNA",new CodedSequenceItem("24851008","SCT","DNA"));

			codedTargets.put("DAPI",new CodedSequenceItem("24851008","SCT","DNA"));
			usingSubstance.put("DAPI",new CodedSequenceItem("C122989","NCIt","DAPI"));
			
			codedTargets.put("HOECHST",new CodedSequenceItem("24851008","SCT","DNA"));
			usingSubstance.put("HOECHST",new CodedSequenceItem("C122989","NCIt","DAPI"));

			codedTargets.put("HEMATOXYLIN",new CodedSequenceItem("84640000","SCT","Nucleus"));
			usingSubstance.put("HEMATOXYLIN",new CodedSequenceItem("12710003","SCT","Hematoxylin"));

			codedTargets.put("DSDNA",new CodedSequenceItem("13925004","SCT","DNA, Double-Stranded"));
			usingSubstance.put("DSDNA",new CodedSequenceItem("C114565","NCIt","Anti-ds DNA Antibody"));

			codedTargets.put("CD3",new CodedSequenceItem("44706009","SCT","CD3"));
			codedTargets.put("CD3-001",new CodedSequenceItem("44706009","SCT","CD3"));
			usingSubstance.put("CD3",new CodedSequenceItem("C112883","NCIt","Anti-CD3 Monoclonal Antibody"));
			usingSubstance.put("CD3-001",new CodedSequenceItem("C112883","NCIt","Anti-CD3 Monoclonal Antibody"));

			codedTargets.put("CD4",new CodedSequenceItem("24655002","SCT","CD4"));
			codedTargets.put("CD4R",new CodedSequenceItem("24655002","SCT","CD4"));	// ? different in some way :(
			usingSubstance.put("CD4",new CodedSequenceItem("C112884","NCIt","Anti-CD4 Monoclonal Antibody"));
			usingSubstance.put("CD4R",new CodedSequenceItem("C112884","NCIt","Anti-CD4 Monoclonal Antibody"));
			
			codedTargets.put("CD8",new CodedSequenceItem("54237000","SCT","CD8"));
			codedTargets.put("CD8--02",new CodedSequenceItem("54237000","SCT","CD8"));	// ? different in some way :(
			codedTargets.put("CD8R",new CodedSequenceItem("54237000","SCT","CD8"));	// ? different in some way :(
			usingSubstance.put("CD8",new CodedSequenceItem("C112888","NCIt","Anti-CD8 Monoclonal Antibody"));
			usingSubstance.put("CD8--02",new CodedSequenceItem("C112888","NCIt","Anti-CD8 Monoclonal Antibody"));
			usingSubstance.put("CD8R",new CodedSequenceItem("C112888","NCIt","Anti-CD8 Monoclonal Antibody"));
			
			codedTargets.put("CD8A",new CodedSequenceItem("C104109","NCIt","CD8a"));
			
			codedTargets.put("CD11B",new CodedSequenceItem("27130004","SCT","CD11b"));
			codedTargets.put("CD11C",new CodedSequenceItem("46959001","SCT","CD11c"));
			
			codedTargets.put("CD20",new CodedSequenceItem("82753007","SCT","CD20"));
			codedTargets.put("CD20P",new CodedSequenceItem("82753007","SCT","CD20"));	// ? different in some way :(
			usingSubstance.put("CD20",new CodedSequenceItem("C118798","NCIt","Anti-CD20 Antibody"));	// no "monoclonal" in NCIt
			usingSubstance.put("CD20P",new CodedSequenceItem("C118798","NCIt","Anti-CD20 Antibody"));

			codedTargets.put("CD31",new CodedSequenceItem("4167003","SCT","CD31"));
			
			codedTargets.put("CD44",new CodedSequenceItem("C17772","NCIt","CD44"));
			// there is an NCIt C116881 Anti-CD44 Monoclonal Antibody RO5429083 but the RO5429083 seems too specific
			
			codedTargets.put("CD45",new CodedSequenceItem("19677004","SCT","CD45"));
			usingSubstance.put("CD45",new CodedSequenceItem("C70798","NCIt","Anti-CD45 Monoclonal Antibody"));
			
			codedTargets.put("CD45RA",new CodedSequenceItem("5404007","SCT","CD45RA"));
			usingSubstance.put("CD45RA",new CodedSequenceItem("C158687","NCIt","Anti-CD45RA Monoclonal Antibody"));

			codedTargets.put("CD45RO",new CodedSequenceItem("86076000","SCT","CD45RO"));
			usingSubstance.put("CD45RO",new CodedSequenceItem("C153104","NCIt","CD45RO Antibody"));

			codedTargets.put("CD56",new CodedSequenceItem("42891003","SCT","CD56"));
			codedTargets.put("CD66B",new CodedSequenceItem("8164002","SCT","CD66B"));	// apparently the same as CD67 :(
			codedTargets.put("CD68",new CodedSequenceItem("31001006","SCT","CD68"));
			codedTargets.put("CD74",new CodedSequenceItem("62998003","SCT","CD74"));
			codedTargets.put("CD163",new CodedSequenceItem("C104064","NCIt","CD163"));
			codedTargets.put("CD169",new CodedSequenceItem("C0142251","UMLS","CD169"));

			codedTargets.put("CDX2",new CodedSequenceItem("C25899","NCIt","CDX2"));
			codedTargets.put("CDX-2",new CodedSequenceItem("C25899","NCIt","CDX2"));
			
			codedTargets.put("CSF1R",new CodedSequenceItem("C17392","NCIt","CSF1R"));
			codedTargets.put("CSF1-R",new CodedSequenceItem("C17392","NCIt","CSF1R"));
			codedTargets.put("CSF-1-R",new CodedSequenceItem("C17392","NCIt","CSF1R"));

			codedTargets.put("CD208",new CodedSequenceItem("C0142251","UMLS","CD208"));
			codedTargets.put("DCLAMP",new CodedSequenceItem("C0142251","UMLS","CD208"));
			codedTargets.put("DC-LAMP",new CodedSequenceItem("C0142251","UMLS","CD208"));
			codedTargets.put("TSC403",new CodedSequenceItem("C0142251","UMLS","CD208"));

			codedTargets.put("CD209",new CodedSequenceItem("C18489","NCIt","CD209"));
			codedTargets.put("CDSIGN",new CodedSequenceItem("C18489","NCIt","CD209"));
			codedTargets.put("DCSIGN",new CodedSequenceItem("C18489","NCIt","CD209"));
			codedTargets.put("DC-SIGN",new CodedSequenceItem("C18489","NCIt","CD209"));

			codedTargets.put("CD223",new CodedSequenceItem("C104623","NCIt","LAG-3"));
			codedTargets.put("LAG3",new CodedSequenceItem("C104623","NCIt","LAG-3"));
			codedTargets.put("LAG-3",new CodedSequenceItem("C104623","NCIt","LAG-3"));

			codedTargets.put("PDL2",new CodedSequenceItem("C45438","NCIt","PD-L2"));	// Programmed Cell Death 1 Ligand 2
			codedTargets.put("PD-L2",new CodedSequenceItem("C45438","NCIt","PD-L2"));
			codedTargets.put("CD273",new CodedSequenceItem("C45438","NCIt","PD-L2"));

			codedTargets.put("PDL1",new CodedSequenceItem("C96024","NCIt","PDL1"));	// Programmed Cell Death 1 Ligand 1
			codedTargets.put("PD-L1",new CodedSequenceItem("C96024","NCIt","PDL1"));
			codedTargets.put("CD274",new CodedSequenceItem("C96024","NCIt","PDL1"));

			codedTargets.put("CD278",new CodedSequenceItem("C94803","NCIt","Inducible T-Cell Costimulator"));
			codedTargets.put("ICOS",new CodedSequenceItem("C94803","NCIt","Inducible T-Cell Costimulator"));

			codedTargets.put("PD1",new CodedSequenceItem("C94697","NCIt","PD1"));	// Programmed Cell Death Protein 1
			codedTargets.put("PD-1",new CodedSequenceItem("C94697","NCIt","PD1"));
			codedTargets.put("CD279",new CodedSequenceItem("C94697","NCIt","PD1"));

			codedTargets.put("CD340",new CodedSequenceItem("C17319","NCIt","ERBB2"));
			codedTargets.put("ERBB2",new CodedSequenceItem("C17319","NCIt","ERBB2"));
			codedTargets.put("HER2",new CodedSequenceItem("C17319","NCIt","ERBB2"));
			codedTargets.put("HER-2",new CodedSequenceItem("C17319","NCIt","ERBB2"));
			codedTargets.put("NEU",new CodedSequenceItem("C17319","NCIt","ERBB2"));
			usingSubstance.put("CD340",new CodedSequenceItem("C129670","NCIt","ERBB2 Antibody"));
			usingSubstance.put("ERBB2",new CodedSequenceItem("C129670","NCIt","ERBB2 Antibody"));
			usingSubstance.put("HER2",new CodedSequenceItem("C129670","NCIt","ERBB2 Antibody"));
			usingSubstance.put("HER-2",new CodedSequenceItem("C129670","NCIt","ERBB2 Antibody"));
			usingSubstance.put("NEU",new CodedSequenceItem("C129670","NCIt","ERBB2 Antibody"));

			codedTargets.put("CCR2",new CodedSequenceItem("C101577","NCIt","CCR2"));
			codedTargets.put("EOMES",new CodedSequenceItem("C102928","NCIt","EOMES"));
			codedTargets.put("FOXP3",new CodedSequenceItem("C104394","NCIt","FOXP3"));
			codedTargets.put("GATA3",new CodedSequenceItem("C75494","NCIt","GATA3"));
			codedTargets.put("GRZB",new CodedSequenceItem("130636000","SCT"," Granzyme B"));
			codedTargets.put("GRANZYME B",new CodedSequenceItem("130636000","SCT"," Granzyme B"));
			codedTargets.put("IDO",new CodedSequenceItem("C79782","NCIt","IDO"));
			
			codedTargets.put("KI67",new CodedSequenceItem("C0208804","UMLS","KI67"));
			codedTargets.put("ANTIGEN KI67",new CodedSequenceItem("C0208804","UMLS","KI67"));
			codedTargets.put("KI67-001",new CodedSequenceItem("C0208804","UMLS","KI67"));
			usingSubstance.put("KI67",new CodedSequenceItem("C118803","NCIt","Anti-KI-67 Antibody"));
			usingSubstance.put("ANTIGEN KI67",new CodedSequenceItem("C118803","NCIt","Anti-KI-67 Antibody"));
			usingSubstance.put("KI67-001",new CodedSequenceItem("C118803","NCIt","Anti-KI-67 Antibody"));
			
			codedTargets.put("NKP46",new CodedSequenceItem("C129059","NCIt","NKP46"));	// also NCR1
			codedTargets.put("RORGT",new CodedSequenceItem("C106314","NCIt","RORGT"));
			codedTargets.put("RORGAMMAT",new CodedSequenceItem("C106314","NCIt","RORGT"));
			codedTargets.put("TBET",new CodedSequenceItem("C104249","NCIt","TBET"));	// also TBX21
			codedTargets.put("PCNA",new CodedSequenceItem("C17323","NCIt","PCNA"));

			codedTargets.put("HLAI",new CodedSequenceItem("C20706","NCIt","HLA Class I"));
			codedTargets.put("HLA I",new CodedSequenceItem("C20706","NCIt","HLA Class I"));
			codedTargets.put("HLAI-001",new CodedSequenceItem("C20706","NCIt","HLA Class I"));
			
			codedTargets.put("HLAII",new CodedSequenceItem("C20705","NCIt","HLA Class II"));
			codedTargets.put("HLA II",new CodedSequenceItem("C20705","NCIt","HLA Class II"));
			codedTargets.put("MHCII",new CodedSequenceItem("C20705","NCIt","HLA Class II"));

			codedTargets.put("HLADR",new CodedSequenceItem("C16692","NCIt","HLA-DR"));
			codedTargets.put("HLA-DR",new CodedSequenceItem("C16692","NCIt","HLA-DR"));

			codedTargets.put("HLA DRB1",new CodedSequenceItem("C52341","NCIt","HLA DRB1"));

			codedTargets.put("CYTOKERATIN",new CodedSequenceItem("259987000","SCT","Cytokeratin"));
			codedTargets.put("PANCYTOKERATIN",new CodedSequenceItem("259987000","SCT","Cytokeratin"));
			codedTargets.put("PAN-CYTOKERATIN",new CodedSequenceItem("259987000","SCT","Cytokeratin"));
			codedTargets.put("PANKERATIN",new CodedSequenceItem("259987000","SCT","Cytokeratin"));
			codedTargets.put("PAN KERATIN",new CodedSequenceItem("259987000","SCT","Cytokeratin"));
			codedTargets.put("PANCK",new CodedSequenceItem("259987000","SCT","Cytokeratin"));
			
			codedTargets.put("VIMENTIN",new CodedSequenceItem("75925000","SCT","Vimentin"));
			codedTargets.put("VIM",new CodedSequenceItem("75925000","SCT","Vimentin"));
			usingSubstance.put("VIMENTIN",new CodedSequenceItem("C118804","NCIt","Anti-Vimentin Antibody"));
			usingSubstance.put("VIM",new CodedSequenceItem("C118804","NCIt","Anti-Vimentin Antibody"));

			codedTargets.put("EGFR",new CodedSequenceItem("86960007","SCT","Epidermal growth factor-urogastrone receptor"));
			usingSubstance.put("EGFR",new CodedSequenceItem("C122777","NCIt","EGFR Antibody"));
			
			codedTargets.put("BCL2",new CodedSequenceItem("LP37239-8","LN","BCL2 Antigen"));
			usingSubstance.put("BCL2",new CodedSequenceItem("C118801","NCIt","Anti-BCL2 Antibody"));
			
			codedTargets.put("CAV1",new CodedSequenceItem("C17971","NCIt","Caveolin-1"));
			codedTargets.put("GRNZB",new CodedSequenceItem("130636000","SCT","Granzyme B"));
			
			codedTargets.put("COLLAGEN",new CodedSequenceItem("61472002","SCT","Collagen"));
			codedTargets.put("T1 COLLAGEN",new CodedSequenceItem("58520002","SCT","Collagen type I"));
			
			codedTargets.put("DESMIN",new CodedSequenceItem("83475004","SCT","Desmin"));
			
			codedTargets.put("ECADHERIN",new CodedSequenceItem("61792","FMA","E-Cadherin"));
			codedTargets.put("E-CADHERIN",new CodedSequenceItem("61792","FMA","E-Cadherin"));
			codedTargets.put("ECAD",new CodedSequenceItem("61792","FMA","E-Cadherin"));
			
			codedTargets.put("LAMIN-A/B/C",new CodedSequenceItem("C17307","NCIt","Lamin"));
			codedTargets.put("NA/K ATPASE",new CodedSequenceItem("23736002","SCT","Na/K-transporting ATPase"));
			
			codedTargets.put("AORTIC SMOOTH MUSCLE ACTIN",new CodedSequenceItem("C103972","NCIt","Actin, Aortic Smooth Muscle"));
			// ? A is aortic or alpha :(
			codedTargets.put("ASMA",new CodedSequenceItem("LP35599-7","LN","Smooth Muscle Actin"));
			codedTargets.put("SMA",new CodedSequenceItem("LP35599-7","LN","Smooth Muscle Actin"));
			// only generic concept found ...
			usingSubstance.put("AORTIC SMOOTH MUSCLE ACTIN",new CodedSequenceItem("21122009","SCT","Smooth muscle antibody"));
			usingSubstance.put("ASMA",new CodedSequenceItem("21122009","SCT","Smooth muscle antibody"));
			usingSubstance.put("SMA",new CodedSequenceItem("21122009","SCT","Smooth muscle antibody"));

			codedTargets.put("TRYPTASE",new CodedSequenceItem("130616001","SCT","Tryptase"));
			codedTargets.put("GLUT1",new CodedSequenceItem("D051272","MSH","Glucose Transporter Type 1"));
			codedTargets.put("GH2AX",new CodedSequenceItem("C468783","MSH","gamma-H2AX protein"));
			
			// could use "LP412182-0","LN","Phosphohistone H3", but NCIt also has more specific forms
			codedTargets.put("PHH3",new CodedSequenceItem("C16685","NCIt","Histone H3"));
			codedTargets.put("HISTONE H3",new CodedSequenceItem("C16685","NCIt","Histone H3"));

			codedTargets.put("H3K4",new CodedSequenceItem("C107427","NCIt","Histone H3 Lysine 4"));
			codedTargets.put("H3K27",new CodedSequenceItem("C116018","NCIt","Histone H3 Lysine 28"));

			codedTargets.put("PS6RP",new CodedSequenceItem("LP173628-1","LN","Phospho-S6 ribosomal protein"));

			codedTargets.put("CSIF",new CodedSequenceItem("C20512","NCIt","Interleukin-10"));
			codedTargets.put("CSIF-10",new CodedSequenceItem("C20512","NCIt","Interleukin-10"));
			codedTargets.put("IL10",new CodedSequenceItem("C20512","NCIt","Interleukin-10"));
			codedTargets.put("TGIF",new CodedSequenceItem("C20512","NCIt","Interleukin-10"));
	
			codedTargets.put("LAMB1",new CodedSequenceItem("C478221","MSH","LAMB1 protein"));
			codedTargets.put("PDPN",new CodedSequenceItem("C117468","MSH","PDPN protein"));
			codedTargets.put("RAD51",new CodedSequenceItem("C495153","MSH","RAD51 protein"));

			codedTargets.put("PRB",new CodedSequenceItem("D016160","MSH","Retinoblastoma Protein"));
			
			codedTargets.put("COXIV",new CodedSequenceItem("D003576","MSH","Cytochrome c Oxidase Subunit IV"));

			codedTargets.put("AR",new CodedSequenceItem("C17063","NCIt","Androgen Receptor"));
			
			codedTargets.put("ER",new CodedSequenceItem("C17069","NCIt","Estrogen Receptor"));
			usingSubstance.put("ER",new CodedSequenceItem("C118805","NCIt","Anti-Estrogen Receptor Antibody"));
			
			codedTargets.put("PGR",new CodedSequenceItem("C17075","NCIt","Progesterone Receptor"));
			
			codedTargets.put("COLI",new CodedSequenceItem("58520002","SCT","Collagen type I"));
			codedTargets.put("COLII",new CodedSequenceItem("61944000","SCT","Collagen type II"));
			codedTargets.put("COLIII",new CodedSequenceItem("57090003","SCT","Collagen type III"));
			codedTargets.put("COLIV",new CodedSequenceItem("89048009","SCT","Collagen type IV"));

			codedTargets.put("CK5",new CodedSequenceItem("D053555","MSH","Cytokeratin 5"));
			codedTargets.put("CK5P",new CodedSequenceItem("D053555","MSH","Cytokeratin 5"));	// ? different in some way :(
			
			codedTargets.put("CK7",new CodedSequenceItem("D053552","MSH","Cytokeratin 7"));
			codedTargets.put("CK8",new CodedSequenceItem("D053533","MSH","Cytokeratin 8"));
			codedTargets.put("CK14",new CodedSequenceItem("D053547","MSH","Cytokeratin 14"));
			codedTargets.put("CK17",new CodedSequenceItem("D053537","MSH","Cytokeratin 17"));
			codedTargets.put("CK19",new CodedSequenceItem("709132000","SCT","Cytokeratin 19"));
			codedTargets.put("CK20",new CodedSequenceItem("259615002","SCT","Cytokeratin 20"));

			codedTargets.put("CONTROL",new CodedSequenceItem("C156442","NCIt","Control analyte"));
			
			// ? what to do when multiple, e.g. in syn24988848.csv:
			// 10	CD320NKP46	11	01	anti CD3/anti CD30/anti NKp46	SP7/SP32/195314	Thermo/Abcam/R&D Bio	MA1-90582/ab64088/MAB1850500	T
			
			// cannot find standard codes for these targets...
			// LamAC - ?? laminin A
			
			// RRIDs ...
			
			usingSubstance.put("AB_2857973",new CodedSequenceItem("AB_2857973","RRID","Recombinant Anti-CD31 antibody [EPR3094] (Alexa Fluor 647)"));
			codedTargets.put("AB_2857973",new CodedSequenceItem("4167003","SCT","CD31"));
			
			usingSubstance.put("AB_10626776",new CodedSequenceItem("AB_10626776","RRID","Hoechst 33342"));
			codedTargets.put("AB_10626776",new CodedSequenceItem("24851008","SCT","DNA"));

			//usingSubstance.put("AB_2535794",new CodedSequenceItem("AB_2535794","RRID","Donkey anti-Rat IgG (H+L) Highly Cross-Adsorbed Secondary Antibody, Alexa Fluor 488"));	// too long > 64
			usingSubstance.put("AB_2535794",new CodedSequenceItem("AB_2535794","RRID","Donkey anti-Rat IgG (H+L) Secondary AB, Alexa Fluor 488"));
			codedTargets.put("AB_2535794",new CodedSequenceItem("146671000146103","SCT","Rat protein"));

			//usingSubstance.put("AB_11217482",new CodedSequenceItem("AB_11217482","RRID","Pan Cytokeratin Monoclonal Antibody (AE1/AE3), eFluor 570, eBioscience"));	// too long > 64
			usingSubstance.put("AB_11217482",new CodedSequenceItem("AB_11217482","RRID","Pan Cytokeratin Monoclonal AB (AE1/AE3), eFluor 570, eBioscience"));
			codedTargets.put("AB_11217482",new CodedSequenceItem("259987000","SCT","Cytokeratin"));

			//usingSubstance.put("AB_2574361",new CodedSequenceItem("AB_2574361","RRID","Alpha-Smooth Muscle Actin Monoclonal Antibody (1A4), eFluor 660, eBioscience"));	// too long > 64
			usingSubstance.put("AB_2574361",new CodedSequenceItem("AB_2574361","RRID","Alpha-Smooth Muscle Actin MC AB (1A4), eFluor 660, eBioscience"));
			
			codedTargets.put("AB_2574361",new CodedSequenceItem("LP35599-7","LN","Smooth Muscle Actin"));

			usingSubstance.put("AB_2889191",new CodedSequenceItem("AB_2889191","RRID","Recombinant Alexa Fluor 488 Anti-CD4 antibody [EPR6855]"));
			codedTargets.put("AB_2889191",new CodedSequenceItem("24655002","SCT","CD4"));

			usingSubstance.put("AB_2562057",new CodedSequenceItem("AB_2562057","RRID","PE anti-human CD45"));
			codedTargets.put("AB_2562057",new CodedSequenceItem("19677004","SCT","CD45"));

			usingSubstance.put("AB_2728811",new CodedSequenceItem("AB_2728811","RRID","Anti-PD1 antibody"));
			codedTargets.put("AB_2728811",new CodedSequenceItem("C94697","NCIt","PD1"));

			usingSubstance.put("AB_10734358",new CodedSequenceItem("AB_10734358","RRID","CD20 Monoclonal Antibody (L26), Alexa Fluor 488, eBioscience"));
			codedTargets.put("AB_10734358",new CodedSequenceItem("82753007","SCT","CD20"));

			usingSubstance.put("AB_2799935",new CodedSequenceItem("AB_2799935","RRID","CD68 (D4B9C) XP Rabbit mAb (PE Conjugate)"));
			codedTargets.put("AB_2799935",new CodedSequenceItem("31001006","SCT","CD68"));

			usingSubstance.put("AB_2574149",new CodedSequenceItem("AB_2574149","RRID","CD8a Monoclonal Antibody (AMC908), eFluor 660, eBioscience"));
			codedTargets.put("AB_2574149",new CodedSequenceItem("C104109","NCIt","CD8a"));

			//usingSubstance.put("AB_162543",new CodedSequenceItem("AB_162543","RRID","Donkey anti-Rabbit IgG (H+L) Highly Cross-Adsorbed Secondary Antibody, Alexa Fluor 555"));	// too long > 64
			usingSubstance.put("AB_162543",new CodedSequenceItem("AB_162543","RRID","Donkey anti-Rabbit IgG (H+L) Secondary AB, Alexa Fluor 555"));
			codedTargets.put("AB_162543",new CodedSequenceItem("146321000146106","SCT","Rabbit protein"));

			//usingSubstance.put("AB_2889155",new CodedSequenceItem("AB_2889155","RRID","Recombinant Alexa Fluor 488 Anti-CD163 antibody [EPR14643-36] - C-terminal"));	// too long > 64
			usingSubstance.put("AB_2889155",new CodedSequenceItem("AB_2889155","RRID","Recomb Alexa Fluor 488 Anti-CD163 AB [EPR14643-36] - C-term"));
			
			codedTargets.put("AB_2889155",new CodedSequenceItem("C104064","NCIt","CD163"));

			usingSubstance.put("AB_2573608",new CodedSequenceItem("AB_2573608","RRID","FOXP3 Monoclonal Antibody (236A/E7), eFluor 570, eBioscience"));
			codedTargets.put("AB_2573608",new CodedSequenceItem("C104394","NCIt","FOXP3"));

			usingSubstance.put("AB_2728832",new CodedSequenceItem("AB_2728832","RRID","PD-L1 (E1L3N) XP (Alexa Fluor 647 Conjugate)"));
			codedTargets.put("AB_2728832",new CodedSequenceItem("C96024","NCIt","PDL1"));

			usingSubstance.put("AB_10691457",new CodedSequenceItem("AB_10691457","RRID","E-Cadherin (24E10) Rabbit mAb (Alexa Fluor 488 Conjugate)"));
			codedTargets.put("AB_10691457",new CodedSequenceItem("61792","FMA","E-Cadherin"));

			usingSubstance.put("AB_10859896",new CodedSequenceItem("AB_10859896","RRID","Vimentin (D21H3) XP Rabbit mAb (Alexa Fluor 555 Conjugate)"));
			codedTargets.put("AB_10859896",new CodedSequenceItem("75925000","SCT","Vimentin"));

			usingSubstance.put("AB_2889213",new CodedSequenceItem("AB_2889213","RRID","Recombinant Alexa Fluor 647 Anti-CDX2 antibody [EPR2764Y]"));
			codedTargets.put("AB_2889213",new CodedSequenceItem("C25899","NCIt","CDX2"));

			usingSubstance.put("AB_2728786",new CodedSequenceItem("AB_2728786","RRID","Anti-Lamin B1"));
			codedTargets.put("AB_2728786",new CodedSequenceItem("C17307","NCIt","Lamin"));

			//usingSubstance.put("AB_162542",new CodedSequenceItem("AB_162542","RRID","Donkey Anti-Mouse IgG (H+L) Polyclonal Antibody, Alexa Fluor 647 Conjugated"));	// too long > 64
			usingSubstance.put("AB_162542",new CodedSequenceItem("AB_162542","RRID","Donkey Anti-Mouse IgG (H+L) Polyclonal AB, Alexa Fluor 647"));
			codedTargets.put("AB_162542",new CodedSequenceItem("146681000146101","SCT","Mouse protein"));	// is specifically mus musculus (house mouse)
			//codedTargets.put("AB_162542",new CodedSequenceItem("C26532","NCIt","Mouse protein"));

			//usingSubstance.put("AB_2890164",new CodedSequenceItem("AB_2890164","RRID","Recombinant Alexa Fluor 555 Anti-Desmin antibody [Y66] - Cytoskeleton Marker"));	// too long > 64
			usingSubstance.put("AB_2890164",new CodedSequenceItem("AB_2890164","RRID","Recomb Alexa Fluor 555 Anti-Desmin AB [Y66] - Cytoskel Mrkr"));
			codedTargets.put("AB_2890164",new CodedSequenceItem("83475004","SCT","Desmin"));

			usingSubstance.put("AB_11178664",new CodedSequenceItem("AB_11178664","RRID","PCNA (PC10) Mouse mAb (Alexa Fluor 488 Conjugate)"));
			codedTargets.put("AB_11178664",new CodedSequenceItem("C17323","NCIt","PCNA"));

			usingSubstance.put("AB_11220088",new CodedSequenceItem("AB_11220088","RRID","Ki-67 Monoclonal Antibody (20Raj1), eFluor 570, eBioscience"));
			codedTargets.put("AB_11220088",new CodedSequenceItem("C0208804","UMLS","KI67"));

			usingSubstance.put("AB_2687824",new CodedSequenceItem("AB_2687824","RRID","Ki-67 (D3B5) Rabbit Antibody (Alexa Fluor 488 Conjugate)"));
			codedTargets.put("AB_2687824",new CodedSequenceItem("C0208804","UMLS","KI67"));

			//usingSubstance.put("AB_10854267",new CodedSequenceItem("AB_10854267","RRID","Collagen IV Monoclonal Antibody (1042), Alexa Fluor 647, eBioscience"));	// too long > 64
			usingSubstance.put("AB_10854267",new CodedSequenceItem("AB_10854267","RRID","Collagen IV MC AB (1042), Alexa Fluor 647, eBioscience"));
			codedTargets.put("AB_10854267",new CodedSequenceItem("89048009","SCT","Collagen type IV"));

			// Fluorophores ...
			codedFluorophores.put("ALEXA FLUOR 488",new CodedSequenceItem("102384756","PUBCHEM_CID","Alexa Fluor 488"));
			codedFluorophores.put("ALEXA FLUOR 555",new CodedSequenceItem("9832481","PUBCHEM_CID","Alexa Fluor 555"));
			codedFluorophores.put("ALEXA FLUOR 647",new CodedSequenceItem("102227060","PUBCHEM_CID","Alexa Fluor 647"));
			codedFluorophores.put("PE",new CodedSequenceItem("34101007","SCT","Phycoerythrin"));	// Use SNOMED rather than PUBCHEM since only have SID (53788505) not CID
			// eFluor 570
			// eFluor 660
			codedFluorophores.put("HOECHST 33342",new CodedSequenceItem("1464","PUBCHEM_CID","Bisbenzimide"));	// HOECHST 33342 is Bisbenzimide, and is itself fluorescent

		}
		catch (DicomException e) {
			// ignore
		}
	}
	
	public static CodedSequenceItem getCodeForChannelInMap(Map<String,CodedSequenceItem> map,String name) {
		String useName = name.toUpperCase().trim();
		CodedSequenceItem csi = map.get(useName);
		if (csi == null) {
			if (useName.contains("(")) {
				// e.g., "DNA (4)"
				useName = useName.replaceAll("[(][0-9]+[)]","").trim();
				csi = map.get(useName);
			}
			else if (useName.startsWith("DAPI")) {
				// e.g., "DAPI4"
				useName = useName.replaceAll("[0-9]+$","").trim();
				csi = map.get(useName);
			}
			else if (useName.startsWith("ANTI")) {
				// e.g., "DAPI4"
				useName = useName.replaceFirst("ANTI[- ]*","").trim();
				csi = map.get(useName);
			}
			else if (useName.startsWith("CONTROL")) {
				// e.g., "Control-488nm"
				useName = "CONTROL";
				csi = map.get(useName);
			}
		}
		return csi;
	}
	
	public static CodedSequenceItem getCodedTarget(String name) {
		return getCodeForChannelInMap(codedTargets,name);
	}
	
	public static CodedSequenceItem getCodedUsingSubstance(String name) {
		return getCodeForChannelInMap(usingSubstance,name);
	}
	
	public static CodedSequenceItem getCodedFluorophore(String name) {
		return getCodeForChannelInMap(codedFluorophores,name);
	}

	public class ImmunostainingChannel {
		//Channel ID	Channel Name	Cycle Number	Sub-Cycle #	Antibody name	Clone	Vendor	Catalog #
		String channelID;
		String channelName;
		String cycleNumber;
		String subCycleNumber;
		String targetName;
		String antibodyName;
		String antibodyRole;
		String rrid;
		String fluorophore;
		String clone;
		String lot;
		String vendor;
		String catalogNumber;
		String excitationWavelength;
		String emissionWavelength;
		String dilution;
		String concentration;

		public ImmunostainingChannel(String channelID,String channelName,String cycleNumber,String subCycleNumber,String targetName,String antibodyName,String antibodyRole,
									 String rrid,String fluorophore,String clone,String lot,String vendor,String catalogNumber,
									 String excitationWavelength,String emissionWavelength,String dilution,String concentration) {
			this.channelID = channelID;
			this.channelName = channelName;
			this.cycleNumber = cycleNumber;
			this.subCycleNumber = subCycleNumber;
			this.targetName = targetName;
			this.antibodyName = antibodyName;
			this.antibodyRole = antibodyRole;
			this.rrid = rrid;
			this.fluorophore = fluorophore;
			this.clone = clone;
			this.lot = lot;
			this.vendor = vendor;
			this.catalogNumber = catalogNumber;
			this.excitationWavelength = excitationWavelength;
			this.emissionWavelength = emissionWavelength;
			this.dilution = dilution;
			this.concentration = concentration;
		}
		
		public ImmunostainingChannel(String channelID,String channelName) {
			this.channelID = channelID;
			this.channelName = channelName;
		}

		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("channelID = "+channelID);
			buf.append(", channelName = "+channelName);
			buf.append(", cycleNumber = "+cycleNumber);
			buf.append(", subCycleNumber = "+subCycleNumber);
			buf.append(", targetName = "+targetName);
			buf.append(", antibodyName = "+antibodyName);
			buf.append(", antibodyRole = "+antibodyRole);
			buf.append(", rrid = "+rrid);
			buf.append(", fluorophore = "+fluorophore);
			buf.append(", clone = "+clone);
			buf.append(", lot = "+lot);
			buf.append(", vendor = "+vendor);
			buf.append(", catalogNumber = "+catalogNumber);
			buf.append(", excitationWavelength = "+excitationWavelength);
			buf.append(", emissionWavelength = "+emissionWavelength);
			buf.append(", dilution = "+dilution);
			buf.append(", concentration = "+concentration);
			return buf.toString();
		}
		
		SequenceAttribute getSpecimenPreparationStepContentItemSequence() throws DicomException {
			SequenceAttribute specimenPreparationStepContentItemSequence = new SequenceAttribute(DicomDictionary.StandardDictionary.getTagFromName("SpecimenPreparationStepContentItemSequence"));
			// TID 8001 Specimen Preparation (M)
			// Rows 1,2 Specimen Identifier +/- Issuer will be added by caller later
			// Row 3 Processing type (M)
			{
				AttributeList itemList = new AttributeList();
				{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("CODE"); itemList.put(a); }
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"111701","DCM","Processing type");
				//CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptCodeSequence,"406858009","SCT","Fluorescent staining");	// ?? CP 2082
				//CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptCodeSequence,"703857004","SCT","Staining");	// (703857004, SCT, "Staining technique (qualifier value)")
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptCodeSequence,"127790008","SCT","Staining");	// (127790008, SCT, "Staining method (procedure)") as of 2022b
				specimenPreparationStepContentItemSequence.addItem(itemList);
			}
			// Row 4 DateTime of processing - not used
			// Row 5 TEXT Processing step description - not used
			// Row 6 CODE Processing step description - not used
			// new stuff ...
			// 6b Channel
			{
				AttributeList itemList = new AttributeList();
				{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("TEXT"); itemList.put(a); }
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"C44170","NCIt","Channel");	// generic
				{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(channelID); itemList.put(a); }
				specimenPreparationStepContentItemSequence.addItem(itemList);
			}
			// 6c Cycle
			if (cycleNumber != null && cycleNumber.length() > 0) {
				AttributeList itemList = new AttributeList();
				{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("TEXT"); itemList.put(a); }
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"C25472","NCIt","Cycle");
				{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(cycleNumber); itemList.put(a); }
				specimenPreparationStepContentItemSequence.addItem(itemList);
			}
			else {
				slf4jlogger.info("getSpecimenPreparationStepContentItemSequence(): missing cycleNumber for channelID {}",channelID);
			}
			// 6d Subcycle
			if (subCycleNumber != null && subCycleNumber.length() > 0) {
				AttributeList itemList = new AttributeList();
				{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("TEXT"); itemList.put(a); }
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"313001","99PMP","Subcycle");
				{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(subCycleNumber); itemList.put(a); }
				specimenPreparationStepContentItemSequence.addItem(itemList);
			}
			else {
				slf4jlogger.info("getSpecimenPreparationStepContentItemSequence(): missing subCycleNumber for channelID {}",channelID);
			}

			// Include TID x8005 Immunohistochemistry
			// Row 1 CODE Component investigated
			// try RRID then targetName then channelName
			{
				CodedSequenceItem targetCode = null;
				if (rrid != null && rrid.length() > 0) {
					if (rrid.toUpperCase().startsWith("AB_")) {
						targetCode = getCodedTarget(rrid);
						if (targetCode != null) {
							slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): have Target code {} for RRID {}",targetCode,rrid);
						}
						else {
							slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): cannot find Target code for RRID {}",rrid);
						}
					}
					else {
						slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): ignoring RRID that is not antibody {}",rrid);
					}
				}
				if (targetCode == null && targetName != null && targetName.length() > 0) {
					targetCode = getCodedTarget(targetName);
					if (targetCode != null) {
						slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): have Target code {} for targetName {}",targetCode,targetName);
					}
					else {
						slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): cannot find Target code for targetName {}",targetName);
					}
				}
				if (targetCode == null && channelName != null && channelName.length() > 0) {
					targetCode = getCodedTarget(channelName);
					if (targetCode != null) {
						slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): have Target code {} for channelName {}",targetCode,channelName);
					}
					else {
						slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): cannot find Target code for channelName {}",channelName);
					}
				}
				if (targetCode != null) {
					AttributeList itemList = new AttributeList();
					slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): using Target code {}",targetCode);
					{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("CODE"); itemList.put(a); }
					CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"246094008","SCT","Component investigated");	// hmmm - very generic, but "Antigen" is too specific :(
					CodedSequenceItem.putSingleCodedSequenceAttribute(itemList,TagFromName.ConceptCodeSequence,targetCode);
					specimenPreparationStepContentItemSequence.addItem(itemList);
				}
				else {
					slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): cannot find Target code for RRID {}, targetName {} or channelName {}",rrid,targetName,channelName);
				}
			}
			
			// Row 2 TEXT Component investigated
			// try targetName then channelName
			{
				String componentInvestigated = null;
				if (targetName != null && targetName.length() > 0) {
					componentInvestigated = targetName;
				}
				if (componentInvestigated == null && channelName != null && channelName.length() > 0) {
					componentInvestigated = channelName;
				}
				if (componentInvestigated != null && componentInvestigated.length() > 0){
					AttributeList itemList = new AttributeList();
					{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("TEXT"); itemList.put(a); }
					CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"246094008","SCT","Component investigated");	// rt. "C25702","NCIt","Target"
					{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(componentInvestigated); itemList.put(a); }
					specimenPreparationStepContentItemSequence.addItem(itemList);
				}
			}

			// Row 3 Tracer = Fluorophore
			{
				AttributeList itemList = new AttributeList();
				{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("CODE"); itemList.put(a); }
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"C2480","NCIt","Tracer");
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptCodeSequence,"C0598447","UMLS","Fluorophore");
				specimenPreparationStepContentItemSequence.addItem(itemList);
			}
			
			// Row 5 Include TID x8007 Row 1 CODE Fluorophore
			// Row 5 Include TID x8007 Row 2 TEXT Fluorophore
			if (fluorophore != null && fluorophore.length() > 0) {
				{
					CodedSequenceItem fluorophoreCode = getCodedFluorophore(fluorophore);
					if (fluorophoreCode != null) {
						slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): have fluorophoreCode code {} for {}",fluorophoreCode,fluorophore);
						AttributeList itemList = new AttributeList();
						{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("CODE"); itemList.put(a); }
						CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"C0598447","UMLS","Using Fluorophore");
						CodedSequenceItem.putSingleCodedSequenceAttribute(itemList,TagFromName.ConceptCodeSequence,fluorophoreCode);
						specimenPreparationStepContentItemSequence.addItem(itemList);
					}
					else {
						slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): cannot find fluorophoreCode code for fluorophore {}",fluorophore);
					}
				}
				{
					AttributeList itemList = new AttributeList();
					{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("TEXT"); itemList.put(a); }
					CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"C0598447","UMLS","Using Fluorophore");
					{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(fluorophore); itemList.put(a); }
					specimenPreparationStepContentItemSequence.addItem(itemList);
				}
			}
			
			// Row 6 CODE Using substance
			// try RRID then antibodyName then channelName
			{
				CodedSequenceItem usingSubstanceCode = null;
				if (rrid != null && rrid.length() > 0) {
					if (rrid.toUpperCase().startsWith("AB_")) {
						usingSubstanceCode = getCodedUsingSubstance(rrid);
						if (usingSubstanceCode != null) {
							slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): have UsingSubstance code {} for RRID {}",usingSubstanceCode,rrid);
						}
						else {
							slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): cannot find UsingSubstance code for RRID {}",rrid);
						}
					}
					else {
						slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): ignoring RRID that is not antibody {}",rrid);
					}
				}
				if (usingSubstanceCode == null && antibodyName != null && antibodyName.length() > 0) {
						usingSubstanceCode = getCodedUsingSubstance(antibodyName);
						if (usingSubstanceCode != null) {
							slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): have UsingSubstance code {} for antibodyName {}",usingSubstanceCode,antibodyName);
						}
						else {
							slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): cannot find UsingSubstance code for antibody {}",antibodyName);
						}
				}
				if (usingSubstanceCode == null && channelName != null && channelName.length() > 0) {
					usingSubstanceCode = getCodedUsingSubstance(channelName);
					if (usingSubstanceCode != null) {
						slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): have UsingSubstance code {} for channelName {}",usingSubstanceCode,channelName);
					}
					else {
						slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): cannot find UsingSubstance code for channelName {}",channelName);
					}
				}
				if (usingSubstanceCode != null) {
					slf4jlogger.debug("getSpecimenPreparationStepContentItemSequence(): have UsingSubstance code {} ",usingSubstanceCode);
					AttributeList itemList = new AttributeList();
					{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("CODE"); itemList.put(a); }
					CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"424361007","SCT","Using substance");	// same concept name code as used later for TEXT Antibody
					CodedSequenceItem.putSingleCodedSequenceAttribute(itemList,TagFromName.ConceptCodeSequence,usingSubstanceCode);
					specimenPreparationStepContentItemSequence.addItem(itemList);
				}
			}
			// Row 7 TEXT Using substance
			if (antibodyName != null && antibodyName.length() > 0) {
				{
					AttributeList itemList = new AttributeList();
					{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("TEXT"); itemList.put(a); }
					CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"424361007","SCT","Using substance");
					{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(antibodyName); itemList.put(a); }
					specimenPreparationStepContentItemSequence.addItem(itemList);
				}
			}
			// Row 8 CODE Staining Technique
			{
				AttributeList itemList = new AttributeList();
				{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("CODE"); itemList.put(a); }
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"703857004","SCT","Staining Technique");
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptCodeSequence,"406858009","SCT","Fluorescent staining");
				specimenPreparationStepContentItemSequence.addItem(itemList);
			}
			// Row 9 TEXT Staining Technique - not used
			// Row 10 Clone
			if (clone != null && clone.length() > 0) {
				AttributeList itemList = new AttributeList();
				{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("TEXT"); itemList.put(a); }
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"C37925","NCIt","Clone");		// this is generic "A group of genetically identical cells, organisms, or DNA molecules" - would prefer more specific "antibody clone" :(
				{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(clone); itemList.put(a); }
				specimenPreparationStepContentItemSequence.addItem(itemList);
			}
			else {
				slf4jlogger.info("getSpecimenPreparationStepContentItemSequence(): missing clone for channelID {}",channelID);
			}
			// Row 11 Manufacturer Name
			if (vendor != null && vendor.length() > 0) {
				AttributeList itemList = new AttributeList();
				{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("TEXT"); itemList.put(a); }
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"C0947322","UMLS","Manufacturer Name");
				{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(vendor); itemList.put(a); }
				specimenPreparationStepContentItemSequence.addItem(itemList);
			}
			else {
				slf4jlogger.info("getSpecimenPreparationStepContentItemSequence(): missing vendor for channelID {}",channelID);
			}
			// Row 12 Brand Name
			if (catalogNumber != null && catalogNumber.length() > 0) {
				AttributeList itemList = new AttributeList();
				{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("TEXT"); itemList.put(a); }
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"111529","DCM","Brand Name");
				{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(catalogNumber); itemList.put(a); }
				specimenPreparationStepContentItemSequence.addItem(itemList);
			}
			else {
				slf4jlogger.info("getSpecimenPreparationStepContentItemSequence(): missing catalogNumber for channelID {}",channelID);
			}
			// Row 13 Dilution
			if (dilution != null && dilution.length() > 0) {
				AttributeList itemList = new AttributeList();
				{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("TEXT"); itemList.put(a); }
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"C4281604","UMLS","Dilution");
				{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(dilution); itemList.put(a); }
				specimenPreparationStepContentItemSequence.addItem(itemList);
			}
			else {
				slf4jlogger.info("getSpecimenPreparationStepContentItemSequence(): missing dilution for channelID {}",channelID);
			}
			// Row 14 Exposure Time - not used (not known)
			// Row 15 Lot Identifier
			if (lot != null && lot.length() > 0) {
				AttributeList itemList = new AttributeList();
				{ Attribute a = new CodeStringAttribute(TagFromName.ValueType); a.addValue("TEXT"); itemList.put(a); }
				CodedSequenceItem.putSingleCodedSequenceItem(itemList,TagFromName.ConceptNameCodeSequence,"C70848","NCIt","Lot Identifier");
				{ Attribute a = new UnlimitedTextAttribute(TagFromName.TextValue); a.addValue(lot); itemList.put(a); }
				specimenPreparationStepContentItemSequence.addItem(itemList);
			}
			else {
				slf4jlogger.info("getSpecimenPreparationStepContentItemSequence(): missing lot for channelID {}",channelID);
			}

			return specimenPreparationStepContentItemSequence;
		}
	}
	
	SortedMap<String,ImmunostainingChannel> channelsByChannelID = null;
	SortedMap<String,AttributeList> opticalPathAttributesByChannelID = null;
	SortedMap<String,SequenceAttribute> specimenPreparationStepContentItemSequenceByChannelID = null;

	public SortedMap<String,AttributeList> getMapOfOpticalPathAttributesByChannelID() throws DicomException {
		if (opticalPathAttributesByChannelID == null) {	// lazy instantiation
			if (channelsByChannelID != null) {
				opticalPathAttributesByChannelID = new TreeMap<String,AttributeList>();
				for (ImmunostainingChannel channel : channelsByChannelID.values()) {
					AttributeList list = new AttributeList();
					opticalPathAttributesByChannelID.put(channel.channelID,list);
					if (channel.excitationWavelength != null && channel.excitationWavelength.length() > 0) {
						try {
							{ Attribute a = new FloatSingleAttribute(DicomDictionary.StandardDictionary.getTagFromName("IlluminationWaveLength")); a.addValue(channel.excitationWavelength); list.put(a); }
						}
						catch (NumberFormatException e) {
							slf4jlogger.error("getMapOfOpticalPathAttributesByChannelID(): excitationWavelength {} not valid numeric float format",channel.excitationWavelength);
						}
					}
					if (channel.emissionWavelength != null && channel.emissionWavelength.length() > 0) {
						try {
							// LightPathFilterPassThroughWavelength may not be the best choice :(
							{ Attribute a = new UnsignedShortAttribute(DicomDictionary.StandardDictionary.getTagFromName("LightPathFilterPassThroughWavelength")); a.addValue(channel.emissionWavelength); list.put(a); }
						}
						catch (NumberFormatException e) {
							slf4jlogger.error("getMapOfOpticalPathAttributesByChannelID(): excitationWavelength {} not valid numeric format",channel.emissionWavelength);
						}
					}
				}
			}
			// else will return null - no reason to return an empty map
		}
		return opticalPathAttributesByChannelID;
	}
	
	public SortedMap<String,SequenceAttribute> getMapOfSpecimenPreparationStepContentItemSequenceByChannelID() throws DicomException {
		if (specimenPreparationStepContentItemSequenceByChannelID == null) {	// lazy instantiation
			if (channelsByChannelID != null) {
				specimenPreparationStepContentItemSequenceByChannelID = new TreeMap<String,SequenceAttribute>();
				for (ImmunostainingChannel channel : channelsByChannelID.values()) {
					specimenPreparationStepContentItemSequenceByChannelID.put(channel.channelID,channel.getSpecimenPreparationStepContentItemSequence());
				}
			}
			// else will return null - no reason to return an empty map
		}
		return specimenPreparationStepContentItemSequenceByChannelID;
	}
	
	private String getColumnValueIfPresent(String columnName,Map<String,Integer> columnIndexByColumnHeader,String[] values) {
		String v = null;
		Integer columnIndex = columnIndexByColumnHeader.get(columnName);
		if (columnIndex != null) {
			int i = columnIndex.intValue();
			if (i < values.length) {
				v = values[i];
			}
			else {
				// this will happen if trailing comma with no value following, because of the way split() works, so don't warn about it ...
				slf4jlogger.trace("getColumnValueIfPresent(): columnIndex {} for columnName {} is not present in row",i,columnName);
			}
		}
		else {
			slf4jlogger.debug("getColumnValueIfPresent(): unrecognized columnName {}",columnName);
		}
		return v;
	}
	
	private void readFile(File file) throws FileNotFoundException, IOException, DicomException {
		Map<String,Integer> columnIndexByColumnHeader = null;
		BufferedReader r = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line=r.readLine()) != null) {
			if (columnIndexByColumnHeader == null) {
				String[] columnHeaders = null;
				if (line.contains("\t")) {
					slf4jlogger.debug("readFile(): have tab separated values");
					columnHeaders = line.split("\t");
				}
				else if (line.contains(",")) {
					slf4jlogger.debug("readFile(): have comma separated values");
					columnHeaders = line.split(",");
				}
				if (columnHeaders != null) {
					columnIndexByColumnHeader = new HashMap<String,Integer>();
					int columnIndex=0;
					for (String columnHeader : columnHeaders) {
						columnIndexByColumnHeader.put(columnHeader.toUpperCase().replaceAll(" ","").replaceAll("-","").replaceAll("NUMBER","#").trim(),new Integer(columnIndex));
						++columnIndex;
					}
				}
				else {
					throw new DicomException("Unrecognized form of header row in input file");
				}
			}
			else {
				// process actual row of data
				String[] values = null;
				if (line.contains("\t")) {
					values = line.split("\t");
				}
				else if (line.contains(",")) {
					values = line.split(",");
				}
				if (values != null) {
					//Channel ID	Channel Name	Cycle Number	Sub-Cycle #	Antibody name	Clone	Vendor	Catalog #
					//Channel ID,Channel Name,Passed QC
					
					//Channel ID,Channel Name,Channel Passed QC,Cycle Number,Sub Cycle Number,Target Name,Antibody Name,Antibody Role,RRID identifier,Fluorophore,Clone,Lot,Vendor,Catalog Number,Excitation Wavelength,Emission Wavelength,Excitation Bandwidth,Emission Bandwidth,Metal Isotope Element,Metal Isotope Mass,Oligo Barcode Upper Strand,Oligo Barcode Lower Strand,Dilution,Concentration
					//Channel:0:0,DNA (1),Yes,1,,DNA,,,AB_10626776,Hoechst 33342,,,Cell Signaling Technology,4082S,395,438,25,26,,,,,1:20000,
					//Channel:0:5,CD3,Yes,2,,CD3,,primary,AB_2889189,N/A,CD3-12,,Abcam,ab11089,485,522,25,20,,,,,1:400,

					String channelID = getColumnValueIfPresent("CHANNELID",columnIndexByColumnHeader,values);
					if (channelID == null || channelID.length() == 0) {
						throw new DicomException("Missing Channel ID");
					}
					else {
						// 1
						// Channel:0:11
						channelID=channelID.replace("Channel:0:","");	// need to be consistent about this when matching against any ID from OME-TIFF-XML (001323)
						String channelName = getColumnValueIfPresent("CHANNELNAME",columnIndexByColumnHeader,values);
						String cycleNumber = getColumnValueIfPresent("CYCLE#",columnIndexByColumnHeader,values);
						String subCycleNumber = getColumnValueIfPresent("SUBCYCLE#",columnIndexByColumnHeader,values);
						String targetName = getColumnValueIfPresent("TARGETNAME",columnIndexByColumnHeader,values);
						String antibodyName = getColumnValueIfPresent("ANTIBODYNAME",columnIndexByColumnHeader,values);
						String antibodyRole = getColumnValueIfPresent("ANTIBODYROLE",columnIndexByColumnHeader,values);
						String rrid = getColumnValueIfPresent("RRIDIDENTIFIER",columnIndexByColumnHeader,values);
						String fluorophore = getColumnValueIfPresent("FLUOROPHORE",columnIndexByColumnHeader,values);
						String clone = getColumnValueIfPresent("CLONE",columnIndexByColumnHeader,values);
						String lot = getColumnValueIfPresent("LOT",columnIndexByColumnHeader,values);
						String vendor = getColumnValueIfPresent("VENDOR",columnIndexByColumnHeader,values);
						String catalogNumber = getColumnValueIfPresent("CATALOG#",columnIndexByColumnHeader,values);
						String excitationWavelength = getColumnValueIfPresent("EXCITATIONWAVELENGTH",columnIndexByColumnHeader,values);
						String emissionWavelength = getColumnValueIfPresent("EMISSIONWAVELENGTH",columnIndexByColumnHeader,values);
						// ignore Excitation Bandwidth
						// ignore Emission Bandwidth
						// ignore Metal Isotope Element
						// ignore Metal Isotope Mass
						// ignore Oligo Barcode Upper Strand
						// ignore Oligo Barcode Lower Strand
						String dilution = getColumnValueIfPresent("DILUTION",columnIndexByColumnHeader,values);
						String concentration = getColumnValueIfPresent("CONCENTRATION",columnIndexByColumnHeader,values);

						ImmunostainingChannel channel = new ImmunostainingChannel(channelID,channelName,cycleNumber,subCycleNumber,targetName,antibodyName,antibodyRole,
															rrid,fluorophore,clone,lot,vendor,catalogNumber,excitationWavelength,emissionWavelength,dilution,concentration);
						if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("readFile(): have channel {}",channel.toString());
						channelsByChannelID.put(channelID,channel);
					}
				}
				else {
					throw new DicomException("Unrecognized form of data row in input file "+file);
				}

			}
		}
		r.close();
	}
	
	public Immunostaining(File file) throws FileNotFoundException, IOException, DicomException {
		channelsByChannelID = new TreeMap<String,ImmunostainingChannel>();
		readFile(file);
	}
	
	public Immunostaining(SortedMap<String,String> channelNamesByChannelID) {
		if (channelNamesByChannelID != null) {
			channelsByChannelID = new TreeMap<String,ImmunostainingChannel>();
			for (String channelID : channelNamesByChannelID.keySet()) {
				String channelName = channelNamesByChannelID.get(channelID);
				if (channelName != null && channelName.length() > 0) {
					ImmunostainingChannel channel = new ImmunostainingChannel(channelID,channelName);
					if (slf4jlogger.isDebugEnabled()) slf4jlogger.debug("Have channel {}",channel.toString());
					channelsByChannelID.put(channelID,channel);
				}
			}
		}
	}

	/**
	 * <p>Read a tab or comma separated values file containing channel parameters describing immunostaining (including antibodies, fluorescence).</p>
	 */
	public static void main(String arg[]) {
		try {
			new Immunostaining(new File(arg[0]));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

