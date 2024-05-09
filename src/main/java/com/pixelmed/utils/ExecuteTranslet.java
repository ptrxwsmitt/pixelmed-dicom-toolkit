/* Copyright (c) 2001-2022, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.utils;

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

/**
 * <p>Use a translet from an XSL-T source file to transform one XML file to another.</p>
 *
 * @author	dclunie
 */
public class ExecuteTranslet {
	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/utils/ExecuteTranslet.java,v 1.3 2022/01/21 19:51:31 dclunie Exp $";

	/**
	 * <p>Apply the XSL-T translet.</p>
	 *
	 * @param	arg	the name of the class file containing the XSL-T translet, the name of the input XML file and the name of the output file
	 */
	public static void main(String arg[]) {
		try {
			Source transformSource = new StreamSource(arg[0]);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer(transformSource);
			StreamSource inputSource = new StreamSource(arg[1]);
			StreamResult outputResult = new StreamResult(new File(arg[2]));
			transformer.transform(inputSource,outputResult);
			
		} catch (Exception e) {
			e.printStackTrace(System.err);	// no need to use SLF4J since command line utility/test
		}
	}
}

