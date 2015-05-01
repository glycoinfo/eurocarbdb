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

import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import org.jfree.data.Range;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Document containing a list of labeled peaks
 * 
 * @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
 */

public class PeakList extends BaseDocument implements SAXUtils.SAXWriter {

	protected Vector<Peak> peaks;
	protected double max_intensity;

	/**
	 * Create an empty peak list
	 */
	public PeakList() {
		super();

		max_intensity = 0.;
	}

	/**
	 * Create a list containing a single peak with a specified mass/charge value
	 * and 0 intensity
	 */
	public PeakList(double m_z) {
		super();

		max_intensity = 0.;
		put(m_z, 1.);
	}

	/**
	 * Create a peak list from the specified collection of peaks
	 */
	public PeakList(Collection<Peak> _peaks) {
		super();

		max_intensity = 0.;

		mergeData(_peaks);
	}

	public String getName() {
		return "PeakList";
	}

	public javax.swing.ImageIcon getIcon() {
		return FileUtils.defaultThemeManager.getImageIcon("peaksdoc");
	}

	public Collection<javax.swing.filechooser.FileFilter> getFileFormats() {
		Vector<javax.swing.filechooser.FileFilter> filters = new Vector<javax.swing.filechooser.FileFilter>();

		filters.add(new ExtensionFileFilter("mgf",
				"Mascot generic peaklist format files"));
		filters.add(new ExtensionFileFilter("xml", "Bruker peak list files"));
		filters
				.add(new ExtensionFileFilter("msa",
						"Cartoonist peak list files"));
		filters
				.add(new ExtensionFileFilter("ctd", "Cartoonist centroids file"));
		filters.add(new ExtensionFileFilter(new String[] { "txt", "csv" },
				"Comma separated peak list files"));
		filters.add(new ExtensionFileFilter(new String[] { "txt", "csv", "ctd",
				"mgf", "msa", "xml" }, "All peak list files"));

		return filters;
	}

	public javax.swing.filechooser.FileFilter getAllFileFormats() {
		return new ExtensionFileFilter(new String[] { "txt", "csv", "ctd",
				"mgf", "msa", "xml" }, "Peak list files");
	}

	public void initData() {
		peaks = new Vector<Peak>();
		max_intensity = 0.;
	}

	public int size() {
		return peaks.size();
	}
	
	

	private int putPVT(double _mz, double _int) {
		return putPVT(new Peak(_mz, _int));
	}

	private int putPVT(Peak toadd) {
		int ind = 0;
		for (ListIterator<Peak> i = peaks.listIterator(); i.hasNext(); ind++) {
			Peak p = i.next();

			// insert
			if (toadd.compareTo(p) < 0) {
				i.previous();
				i.add(toadd.clone());
				return ind;
			}

			// set
			if (toadd.compareTo(p) == 0) {
				p.setIntensity(toadd.getIntensity());
				return ind;
			}
		}

		// append
		peaks.add(toadd.clone());
		return ind;
	}

	/**
	 * Add a new peak to the list, if a previous peak with the same mass/charge
	 * value was present, the intensity is updated
	 * 
	 * @return the index of the new peak or -1 if the peak was <code>null</code>
	 */
	public int put(double _mz, double _int) {
		int ind = putPVT(_mz, _int);
		updateMaxIntensity();
		fireDocumentChanged();
		return ind;
	}

	/**
	 * Add a new peak to the list, if a previous peak with the same mass/charge
	 * value was present, the intensity is updated
	 * 
	 * @return the index of the new peak or -1 if the peak was <code>null</code>
	 */
	public int add(Peak p) {
		if (p == null)
			return -1;
		return put(p.getMZ(), p.getIntensity());
	}

	/**
	 * Add a collection of peaks to the list
	 */
	public void addAll(Collection<Peak> _peaks) {
		mergeData(_peaks);
	}

	/**
	 * Clear the list and add a collection of peaks to it
	 */
	public void setData(Collection<Peak> _peaks) {
		setData(_peaks, true);
	}

	private void setData(Collection<Peak> _peaks, boolean fire) {
		peaks.clear();
		max_intensity = 0.;

		for (Iterator<Peak> i = _peaks.iterator(); i.hasNext();)
			putPVT(i.next());

		updateMaxIntensity();
		if (fire)
			fireDocumentChanged();
	}

	/**
	 * Add a collection of peaks to the list
	 */
	public void mergeData(Collection<Peak> _peaks) {
		mergeData(_peaks, true);
	}

	private void mergeData(Collection<Peak> _peaks, boolean fire) {
		for (Iterator<Peak> i = _peaks.iterator(); i.hasNext();)
			putPVT(i.next());

		updateMaxIntensity();
		if (fire)
			fireDocumentChanged();
	}

	private void removePVT(int ind) {
		peaks.removeElementAt(ind);
	}

	/**
	 * Remove the specified peak from the list
	 * 
	 * @param ind
	 *            the index of the peak in the list
	 */
	public void remove(int ind) {
		peaks.removeElementAt(ind);
		updateMaxIntensity();
		fireDocumentChanged();
	}

	/**
	 * Remove the specified peaks from the list
	 * 
	 * @param inds
	 *            the indexes of the peak in the list
	 */
	public void remove(int[] inds) {
		Arrays.sort(inds);
		for (int i = 0; i < inds.length; i++){
			int corrected=inds[i] - i;
			if(corrected!=this.getPeaks().size()){
				removePVT(corrected);
			}
				
			
		}
		updateMaxIntensity();
		fireDocumentChanged();
	}

	/**
	 * Remove the specified peak from the list
	 * 
	 * @param mz
	 *            the mass/charge value of the peak to remove
	 */
	public void remove(double mz) {
		remove(indexOf(mz));
	}

	/**
	 * Return the index of a peak in the list
	 * 
	 * @param mz
	 *            the mass/charge value of the peak
	 */
	public int indexOf(double mz) {
		for (int i = 0; i < size(); i++) {
			if (mz < getMZ(i))
				return -1;
			if (Math.abs(mz - getMZ(i)) < 0.000001)
				return i;
		}
		return -1;
	}

	/**
	 * Return all the peaks in the list
	 */
	public Collection<Peak> getPeaks() {
		return peaks;
	}

	/**
	 * Return a specified peak from the list
	 * 
	 * @param ind
	 *            the index of the peak
	 */
	public Peak getPeak(int ind) {
		return peaks.elementAt(ind);
	}

	/**
	 * Set the mass/charge value of a peak in the list
	 * 
	 * @param ind
	 *            the index of the peak
	 * @param _mz
	 *            the new mass/charge value
	 */
	public void setMZ(int ind, double _mz) {
		Peak peak=this.peaks.get(ind);
		Peak clone=new Peak(_mz,peak.getIntensity());
		clone.setCharge(peak.getCharge());
		
		
		removePVT(ind);
		putPVT(clone);
		updateMaxIntensity();
		fireDocumentChanged();
	}
	
	public void setCharge(int ind,int charge){
		//System.err.println("Charge is: "+charge);
		Peak peak=this.peaks.get(ind);
		Peak clone=new Peak(peak.getMZ(),peak.getIntensity());
		clone.setCharge(charge);
		
		removePVT(ind);
		putPVT(clone);
		updateMaxIntensity();
		fireDocumentChanged();
	}

	/**
	 * Return the mass/charge value of a peak in the list
	 * 
	 * @param ind
	 *            the index of the peak
	 */
	public double getMZ(int ind) {
		return peaks.elementAt(ind).getMZ();
	}

	/**
	 * Set the intensity value of a peak in the list
	 * 
	 * @param ind
	 *            the index of the peak
	 * @param _int
	 *            the new intensity value
	 */
	public void setIntensity(int ind, double _int) {
		Peak peak=this.peaks.get(ind);
		Peak clone=new Peak(peak.getMZ(),_int);
		clone.setCharge(peak.getCharge());
		
		
		removePVT(ind);
		putPVT(clone);
		updateMaxIntensity();
		fireDocumentChanged();
	}

	/**
	 * Return the intensity value of a peak in the list
	 * 
	 * @param ind
	 *            the index of the peak
	 */
	public double getIntensity(int ind) {
		return peaks.elementAt(ind).getIntensity();
	}
	
	public int getCharge(int ind){
		return peaks.elementAt(ind).getCharge();
	}

	/**
	 * Return the intensity value of a peak normalized by the maximum intensity
	 * in the list
	 * 
	 * @param ind
	 *            the index of the peak
	 */
	public double getRelativeIntensity(int ind) {
		if (max_intensity == 0.)
			return getIntensity(ind);
		return getIntensity(ind) / max_intensity * 100;
	}

	/**
	 * Return the minimum mass/charge value of the peaks in the list
	 */
	public double getMinMZ() {
		return peaks.firstElement().getMZ();
	}

	/**
	 * Return the maximum mass/charge value of the peaks in the list
	 */
	public double getMaxMZ() {
		return peaks.lastElement().getMZ();
	}

	/**
	 * Return the range of mass/charge values of the peaks in the list
	 */
	public Range getMZRange() {
		return new Range(getMinMZ(), getMaxMZ());
	}

	/**
	 * Return the peaks at the specified positions
	 * 
	 * @param inds
	 *            the indexes of the peaks
	 */
	public Collection<Peak> extract(int[] inds) {
		Vector<Peak> ret = new Vector<Peak>();
		for (int i = 0; i < inds.length; i++)
			ret.add(peaks.elementAt(inds[i]).clone());
		return ret;
	}

	/**
	 * Update the maximum intensity value after a change in the peaks
	 */
	public void updateMaxIntensity() {
		// update max intensity
		max_intensity = 0.;
		for (Peak p : peaks)
			max_intensity = Math.max(max_intensity, p.getIntensity());
	}

	/**
	 * Return the maximum intensity value of the peaks in the list
	 */
	public double getMaxIntensity() {
		return max_intensity;
	}

	/**
	 * Return the maximum intensity value of the peaks in a specified
	 * mass/charge range
	 * 
	 * @param from_mz
	 *            minimum mass/charge value
	 * @param to_mz
	 *            maximum mass/charge value
	 */
	public double getMaxIntensity(double from_mz, double to_mz) {
		double ret = 0.;
		for (Peak p : peaks) {
			if (p.getMZ() >= from_mz && p.getMZ() <= to_mz)
				ret = Math.max(p.getIntensity(), ret);
		}
		return ret;
	}

	/**
	 * Return the peaks as a 2xN table contain mass/charge and intensity values
	 */
	public double[][] getData() {
		return getData(getMinMZ(), getMaxMZ(), 0., false);
	}

	/**
	 * Return the peaks as a 2xN table contain mass/charge and intensity values
	 * 
	 * @param rel_int
	 *            <code>true</code> if the intensities should be normalized by
	 *            the maximum intensity
	 */
	public double[][] getData(boolean rel_int) {
		return getData(getMinMZ(), getMaxMZ(), 0., rel_int);
	}

	/**
	 * Return the peaks as a 2xN table contain mass/charge and intensity values
	 * 
	 * @param from_mz
	 *            limit the minimum mass/charge value of the peaks
	 * @param to_mz
	 *            limit the maximum mass/charge value of the peaks
	 */
	public double[][] getData(double from_mz, double to_mz) {
		return getData(from_mz, to_mz, 0., false);
	}

	/**
	 * Return the peaks as a 2xN table contain mass/charge and intensity values
	 * 
	 * @param from_mz
	 *            limit the minimum mass/charge value of the peaks
	 * @param to_mz
	 *            limit the maximum mass/charge value of the peaks
	 * @param rel_int
	 *            <code>true</code> if the intensities should be normalized by
	 *            the maximum intensity
	 */
	public double[][] getData(double from_mz, double to_mz, boolean rel_int) {
		return getData(from_mz, to_mz, 0., rel_int);
	}

	/**
	 * Return the peaks as a 2xN table contain mass/charge and intensity values
	 * 
	 * @param from_mz
	 *            limit the minimum mass/charge value of the peaks
	 * @param to_mz
	 *            limit the maximum mass/charge value of the peaks
	 * @param add_boundaries
	 *            add two peaks at beginning and end of the table whose
	 *            mass/charge values differs from their neighbours of the
	 *            specified value
	 * @param rel_int
	 *            <code>true</code> if the intensities should be normalized by
	 *            the maximum intensity
	 */
	public double[][] getData(double from_mz, double to_mz,
			double add_boundaries, boolean rel_int) {

		// count peaks
		int no_peaks = 0;
		for (Peak p : peaks) {
			if (p.getMZ() >= from_mz && p.getMZ() <= to_mz)
				no_peaks++;
		}

		// init vectors
		int added = (add_boundaries > 0.) ? 2 : 0;

		double[][] ret = new double[2][];
		ret[0] = new double[no_peaks + added];
		ret[1] = new double[no_peaks + added];

		// copy peaks
		double div = 1.;
		if (rel_int)
			div = getMaxIntensity(from_mz, to_mz) / 100.;

		int i = 0;
		for (Peak p : peaks) {
			if (p.getMZ() >= from_mz && p.getMZ() <= to_mz) {
				ret[0][i + added / 2] = p.getMZ();
				ret[1][i + added / 2] = p.getIntensity() / div;
				i++;
			}
		}

		// add boundaries
		if (add_boundaries > 0.) {
			ret[0][0] = ret[0][1] - add_boundaries;
			ret[1][0] = 0.;

			ret[0][no_peaks + 1] = ret[0][no_peaks] + add_boundaries;
			ret[1][no_peaks + 1] = 0.;
		}

		return ret;
	}

	private boolean updatePeakPVT(Peak p) {
		int ind = indexOf(p.getMZ());
		if (ind == -1)
			return false;

		peaks.get(ind).setIntensity(p.getIntensity());
		return true;
	}

	/**
	 * Update the intensity of a peak
	 * 
	 * @param p
	 *            specifies the mass/charge value and the new intensity
	 */
	public boolean updatePeak(Peak p) {
		if (updatePeakPVT(p)) {
			updateMaxIntensity();
			fireDocumentChanged();
			return true;
		}
		return false;
	}

	/**
	 * Update the intensities of a collection of peaks
	 * 
	 * @param peaks
	 *            specifies the mass/charge values and the new intensities
	 */
	public boolean updatePeaks(Collection<Peak> peaks) {
		boolean changed = false;
		for (Peak p : peaks)
			changed |= updatePeakPVT(p);

		if (changed) {
			updateMaxIntensity();
			fireDocumentChanged();
		}
		return changed;
	}

	// --------------
	// Serialization

	private String transform(String buffer, String transformer_path)
			throws Exception {
		TransformerFactory tf = TransformerFactory.newInstance();
		StreamSource in_xslt = new StreamSource(PeakList.class.getResource(
				transformer_path).toString());
		Transformer t = tf.newTransformer(in_xslt);

		StreamSource in = new StreamSource(new StringReader(buffer));
		StringWriter sw = new StringWriter();
		StreamResult out = new StreamResult(sw);
		t.transform(in, out);

		return sw.getBuffer().toString();
	}

	@Override
	public boolean open(File file, boolean merge, boolean warning) {
		System.err.println("In here ?");
		try {
			System.err.println("1");
			// try CSV
			if (readCSV(file, merge))
				return true;
			System.err.println("2");
			// try MGF peak list
			if (readMGF(file, merge))
				return true;
			System.err.println("3");
			// try MSA peak list
			if (readMSA(file, merge))
				return true;
			System.err.println("4");
			// try bruker xml
			if (readBruker(file, merge)){
				System.err.println("Read bruker");
				return true;
			}else{
				System.err.println("Failed to read bruker?");
			}
			System.err.println("5");

			// bail out
			init();
			if (warning){
				System.err.println("Unrecognized peak list format");
				throw new Exception("Unrecognized peak list format");
			}

			return false;
		} catch (Exception e) {
			System.err.println("Caught exception");
			LogUtils.report(e);
			return false;
		}
	}

	private boolean readMGF(File file, boolean merge) {
		try {
			org.proteomecommons.io.mgf.MascotGenericFormatPeakListReader mgf_reader = new org.proteomecommons.io.mgf.MascotGenericFormatPeakListReader(
					file.getPath());
			org.proteomecommons.io.Peak[] mgf_peaks = mgf_reader.getPeakList()
					.getPeaks();	
			if (!merge)
				clear();
			for (int i = 0; i < mgf_peaks.length; i++){
				putPVT(mgf_peaks[i]);
			}
			updateMaxIntensity();
			setFilename(file.getAbsolutePath());
			fireDocumentInit();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private int putPVT(org.proteomecommons.io.Peak peakCommons){
		
		Peak peak=new Peak();
		peak.setIntensity(peakCommons.getIntensity());
		peak.setMZ(peakCommons.getMassOverCharge());
		peak.setCharge(peakCommons.getCharge());
		
		return putPVT(peak);

	}

	private boolean readCSV(File file, boolean merge) {
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String buffer = consume(br);

			// try text peak list
			Collection<Peak> read = parseString(buffer);
			if (read.size() > 0) {
				if (merge)
					mergeData(read, false);
				else
					setData(read, false);

				setFilename(file.getAbsolutePath());
				fireDocumentInit();
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean readBruker(File file, boolean merge) {
		try {
			System.err.println("In bruker read");
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String buffer = consume(br);

			String str=transform(buffer,
			"/transforms/bruker_peak_list.xsl");
			
			System.err.println(str);
			
			Collection<Peak> read = parseString(transform(buffer,
					"/transforms/bruker_peak_list.xsl"));
			if (read.size() > 0) {
				if (merge)
					mergeData(read, false);
				else
					setData(read, false);

				setFilename(file.getAbsolutePath());
				fireDocumentInit();
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean readMSA(File file, boolean merge) {
		AnnotatedPeakList read = new AnnotatedPeakList();
		if (!read.open(file, merge, false)){
			return false;
		}
		
		if(read.getPeaks().size()==0){
			return false;
		}

		if (merge)
			mergeData(read.getPeaks(), false);
		else
			setData(read.getPeaks(), false);

		setFilename(file.getAbsolutePath());
		fireDocumentInit();
		return true;
	}

	/**
	 * Create a string representation of the peak list where the peaks are
	 * separated by newlines
	 */
	public String toString() {
		return toString(peaks);
	}

	/**
	 * Create a string representation of a collection of peaks where the peaks
	 * are separated by newlines
	 */
	static public String toString(Collection<Peak> peaks) {

		StringBuilder ret = new StringBuilder();
		for (Iterator<Peak> i = peaks.iterator(); i.hasNext();) {
			ret.append(i.next());
			ret.append("\n");
		}

		return ret.toString();
	}

	/**
	 * Load a list of peaks to the current list from a string representaion of a
	 * collection of peaks separated by newlines
	 * 
	 * @param merge
	 *            if <code>false</code> the list is emptied before adding the
	 *            new peaks
	 */
	public void fromString(String str, boolean merge) throws Exception {
		System.err.println("HERE");
		if (merge)
			mergeData(parseString(str), false);
		else
			setData(parseString(str), false);
	}

	/**
	 * Create a list of peaks from a string representaion of a collection of
	 * peaks separated by newlines
	 */
	static public Collection<Peak> parseString(String str) throws Exception {
		Vector<Peak> ret = new Vector<Peak>();

		String line;
		BufferedReader br = new BufferedReader(new StringReader(str));
		while ((line = br.readLine()) != null) {
			Peak p = Peak.parseString(line);
			if (p != null)
				ret.add(p);
		}
		return ret;
	}

	/**
	 * Create a new object from its XML representation as part of a DOM tree.
	 */
	public void fromXML(Node pl_node, boolean merge) throws Exception {
		// clear
		if (!merge) {
			resetStatus();
			initData();
		} else
			setChanged(true);

		// read
		Vector<Node> p_nodes = XMLUtils.findAllChildren(pl_node, "Peak");
		for (Node p_node : p_nodes) {
			Peak p = Peak.fromXML(p_node);
			putPVT(p);
		}
		updateMaxIntensity();
	}

	/**
	 * Create an XML representation of this object to be part of a DOM tree.
	 */
	public Element toXML(Document document) {
		if (document == null)
			return null;

		// create root node
		Element pl_node = document.createElement("PeakList");
		if (pl_node == null)
			return null;

		// add peaks
		for (Peak p : peaks) {
			Element p_node = p.toXML(document);
			if (p_node != null)
				pl_node.appendChild(p_node);
		}

		return pl_node;
	}

	/**
	 * Default SAX handler to read a representation of this object from an XML
	 * stream.
	 */
	public static class SAXHandler extends SAXUtils.ObjectTreeHandler {

		private PeakList theDocument;
		private boolean merge;

		public SAXHandler(PeakList _doc, boolean _merge) {
			theDocument = _doc;
			merge = _merge;
		}

		public boolean isElement(String namespaceURI, String localName,
				String qName) {
			return qName.equals(getNodeElementName());
		}

		public static String getNodeElementName() {
			return "PeakList";
		}

		protected SAXUtils.ObjectTreeHandler getHandler(String namespaceURI,
				String localName, String qName) {
			if (qName.equals(Peak.SAXHandler.getNodeElementName()))
				return new Peak.SAXHandler();
			return null;
		}

		protected Object finalizeContent(String namespaceURI, String localName,
				String qName) throws SAXException {
			// clear
			if (!merge) {
				theDocument.resetStatus();
				theDocument.initData();
			} else
				theDocument.setChanged(true);

			// read data
			for (Object o : getSubObjects(Peak.SAXHandler.getNodeElementName()))
				theDocument.putPVT((Peak)o);
			theDocument.updateMaxIntensity();

			return (object = theDocument);
		}
	}

	public void write(TransformerHandler th) throws SAXException {
		th.startElement("", "", "PeakList", new AttributesImpl());
		for (Peak p : peaks)
			p.write(th);
		th.endElement("", "", "PeakList");
	}
}
