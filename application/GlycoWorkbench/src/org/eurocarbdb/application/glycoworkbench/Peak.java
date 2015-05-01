/*
 *   EuroCarbDB, a framework for carbohydrate bioinformatics
 *
 *   Copyright (c) 2006-2009, Eurocarb project, or third-party contributors as
 *   indicated by the @author tags or express copyright attribution
 *   statements applied by the authors.  
 *
 *   This copyrighted material is made available to anyone wishing to use, modify,
 *   copy, or redistribute it subject to the terms and conditions of the GNU
 *   Lesser General Public License, as published by the Free Software Foundation.
 *   A copy of this license accompanies this distribution in the file LICENSE.txt.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *   or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 *   for more details.
 *
 *   Last commit: $Rev: 1930 $ by $Author: david@nixbioinf.org $ on $Date:: 2010-07-29 #$  
 */

package org.eurocarbdb.application.glycoworkbench;

import org.eurocarbdb.application.glycanbuilder.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.omegahat.Environment.ObjectDataStructures.numeric;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Represent a labeled peak centroid in a mass spectrum
 * 
 * @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
 */

public class Peak implements Comparable<Peak>, SAXUtils.SAXWriter {

	static final protected Pattern pattern1;
	static final protected Pattern pattern2;
	static final protected Pattern pattern3;
	static final protected Pattern pattern4;
	static {
		String number = "(\\S+)";
		String separator = "(?:\\s+|[,;])";
		pattern1 = Pattern.compile("^\\s*" + number + "\\s*$" );
		pattern2 = Pattern.compile("^\\s*" + number + separator + number  + "\\s*$");
		pattern3 = Pattern.compile("^\\s*" + number + separator + number+ separator + number  + "\\s*$");
		pattern4 = Pattern.compile("^\\s*" + number + separator + number+ separator + number  + separator + number  + "\\s*$");
	}

	protected Double mz_ratio;
	protected Double intensity;
	private Integer charge=Integer.MIN_VALUE;

	/**
	 * Create a new peak object with 0 values for mass/charge and intensity
	 */
	public Peak() {
		mz_ratio = 0.;
		intensity = 0.;
	}

	/**
	 * Create a new peak object with specific values for mass/charge and
	 * intensity
	 * 
	 * @param _mz
	 *            the mass/charge value
	 * @param _int
	 *            the intensity value
	 */
	public Peak(double _mz, double _int) {
		mz_ratio = _mz;
		intensity = _int;
	}
	
	public Peak(double _mz, double _int, int _charge) {
		mz_ratio = _mz;
		intensity = _int;
		charge = _charge;
	}

	/**
	 * Create a copy of this object
	 */
	public Peak clone() {
		Peak clone=new Peak(mz_ratio, intensity);
		clone.setCharge(this.charge);
		return clone;
	}

	public int compareTo(Peak p) {
		if (this.mz_ratio < p.mz_ratio){
			return -1;
		}
		if (this.mz_ratio > p.mz_ratio)
			return +1;
		return 0;
	}

	public boolean equals(Object other) {
		if (!(other instanceof Peak))
			return false;

		Peak p = (Peak) other;
		return (Math.abs(this.mz_ratio - p.mz_ratio) < 0.0000000001 && Math
				.abs(this.intensity - p.intensity) < 0.0000000001);
	}

	public boolean mzEquals(Peak it){
		return (Math.abs(this.mz_ratio - it.mz_ratio) < 0.0000000001);
	}
	
	public boolean mzEquals(Peak it, MassUnit unit, double accuracy){
		if(unit==MassUnit.PPM){
			return (Math.abs(1. - it.getMZ() / mz_ratio) < (0.000001 * accuracy));	
		}
		return (Math.abs(mz_ratio - it.getMZ()) < accuracy);
	}
	
	public int hashCode() {
		return mz_ratio.hashCode() + intensity.hashCode();
	}

	/**
	 * Return the mass/charge value for this peak
	 */
	public double getMZ() {
		return mz_ratio;
	}

	/**
	 * Set the mass/charge value for this peak
	 */
	public void setMZ(double _mz) {
		mz_ratio = _mz;
	}

	/**
	 * Return the intensity value for this peak
	 */
	public double getIntensity() {
		return intensity;
	}

	/**
	 * Set the intensity value for this peak
	 */
	public void setIntensity(double _int) {
		intensity = _int;
	}

	// serializaation

	public String toString() {
		return mz_ratio + " " + intensity;
	}

	/**
	 * Create a new object from its string representation composed of a space
	 * separated list of numbers that must contain the mass/charge value and
	 * that can also contain the intensity and area of the peak (ignored, here
	 * for compatibility)
	 */
	static public Peak parseString(String str) {

		try {
			Matcher m = pattern4.matcher(str);
			if (m.matches())
				return new Peak(Double.valueOf(m.group(1)), Double.valueOf(m
						.group(2)),Integer.valueOf(m.group(4)));
			
			m = pattern3.matcher(str);
			if (m.matches())
				return new Peak(Double.valueOf(m.group(1)), Double.valueOf(m
						.group(2)),Integer.valueOf(m.group(3)));
			
			m = pattern2.matcher(str);
			if (m.matches())
				return new Peak(Double.valueOf(m.group(1)), Double.valueOf(m
						.group(2)));
			
			m = pattern1.matcher(str);
			if (m.matches())
				return new Peak(Double.valueOf(m.group(1)), 1);
		} catch (NumberFormatException nfe) {
		}

		return null;
	}

	/**
	 * Create a new object from its XML representation as part of a DOM tree.
	 */
	static public Peak fromXML(Node peak_node) throws Exception {
		Peak ret = new Peak();

		ret.mz_ratio = Double.valueOf(XMLUtils.getAttribute(peak_node,
				"mz_ratio"));
		ret.intensity = Double.valueOf(XMLUtils.getAttribute(peak_node,
				"intensity"));

		if (XMLUtils.getAttribute(peak_node, "charge") != null) {
			ret.charge = Integer.valueOf(XMLUtils.getAttribute(peak_node,
					"charge"));
		}

		return ret;
	}

	/**
	 * Create an XML representation of this object to be part of a DOM tree.
	 */
	public Element toXML(Document document) {
		if (document == null)
			return null;

		Element peak_node = document.createElement("Peak");
		if (peak_node == null)
			return null;

		peak_node.setAttribute("mz_ratio", "" + mz_ratio);
		peak_node.setAttribute("intensity", "" + intensity);
		
		if(this.getCharge()!=Integer.MIN_VALUE){
			peak_node.setAttribute("charge", "" + this.getCharge());
		}else{
			
		}
		

		return peak_node;
	}

	/**
	 * Default SAX handler to read a representation of this object from an XML
	 * stream.
	 */

	public static class SAXHandler extends SAXUtils.ObjectTreeHandler {

		public boolean isElement(String namespaceURI, String localName,
				String qName) {
			return qName.equals(getNodeElementName());
		}

		public static String getNodeElementName() {
			return "Peak";
		}

		protected void initContent(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {
			super.initContent(namespaceURI, localName, qName, atts);

			Peak ret = new Peak();
			ret.mz_ratio = doubleAttribute(atts, "mz_ratio", 0.);
			ret.intensity = doubleAttribute(atts, "intensity", 0.);
			ret.charge = integerAttribute(atts,"charge",Integer.MIN_VALUE);
			
			object = ret;
		}

	}

	public void write(TransformerHandler th) throws SAXException {
		AttributesImpl atts = new AttributesImpl();
		atts.addAttribute("", "", "mz_ratio", "CDATA", "" + mz_ratio);
		atts.addAttribute("", "", "intensity", "CDATA", "" + intensity);
		
		if(this.charge!=Integer.MIN_VALUE){
			atts.addAttribute("", "", "charge", "CDATA", "" + charge);
		}
		
		th.startElement("", "", "Peak", atts);
		th.endElement("", "", "Peak");
	}

	public void setCharge(int charge) {
		// TODO Auto-generated method stub
		this.charge = charge;
	}

	public int getCharge() {
		return this.charge;
	}
}