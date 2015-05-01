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
/**
 @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
 */

package org.eurocarbdb.application.glycoworkbench;

import org.eurocarbdb.application.glycoworkbench.plugin.reporting.*;
import org.eurocarbdb.application.glycoworkbench.plugin.*;
import org.eurocarbdb.application.glycanbuilder.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import javax.swing.SwingUtilities;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.helpers.AttributesImpl;

public class Scan implements BaseDocument.DocumentChangeListener {

	protected String name = "Scan";
	protected Double precursor_mz = null;
	protected boolean is_msms = true;

	protected GlycanDocument theStructures = null;
	protected FragmentDocument theFragments = null;
	protected SpectraDocument theSpectra = null;
	protected PeakList thePeakList = null;
	protected AnnotatedPeakList theAnnotatedPeakList = null;
	
	protected NotesDocument theNotes = null;
	protected Vector<AnnotationReportDocument> theAnnotationReports = new Vector<AnnotationReportDocument>();

	protected Scan parent = null;
	protected Vector<Scan> children = new Vector<Scan>();

	public Scan(GlycanWorkspace ws) {
		theStructures = new GlycanDocument(ws);
		theFragments = new FragmentDocument();
		theSpectra = new SpectraDocument();
		thePeakList = new PeakList();
		theAnnotatedPeakList = new AnnotatedPeakList();
		theNotes = new NotesDocument();
		theAnnotationReports = new Vector<AnnotationReportDocument>();

		theSpectra.addDocumentChangeListener(this);
	}

	public void initData() {
		name = "Scan";
		precursor_mz = null;
		is_msms = true;

		theStructures.initData();
		theFragments.initData();
		theSpectra.initData();
		thePeakList.initData();
		theAnnotatedPeakList.initData();
		theNotes.initData();
		theAnnotationReports = new Vector<AnnotationReportDocument>();

		parent = null;
		children = new Vector<Scan>();
	}

	public Scan getParent() {
		return parent;
	}

	public void setParent(Scan s) {
		parent = s;
	}

	public String getName() {
		return name;
	}

	public void setName(String n) {
		name = n;
	}

	public Double getPrecursorMZ() {
		return precursor_mz;
	}

	public void setPrecursorMZ(Double value) {
		precursor_mz = value;
	}

	public boolean isMsMs() {
		return is_msms;
	}

	public void setMsMs(boolean f) {
		is_msms = f;
	}

	public GlycanDocument getStructures() {
		return theStructures;
	}

	public FragmentDocument getFragments() {
		return theFragments;
	}

	public SpectraDocument getSpectra() {
		return theSpectra;
	}

	public PeakList getPeakList() {
		return thePeakList;
	}

	public AnnotatedPeakList getAnnotatedPeakList() {
		return theAnnotatedPeakList;
	}

	public NotesDocument getNotes() {
		return theNotes;
	}

	public Vector<AnnotationReportDocument> getAnnotationReports() {
		return theAnnotationReports;
	}

	public int getNoChildren() {
		return children.size();
	}

	public Vector<Scan> getChildren() {
		return children;
	}

	public Iterator<Scan> iterator() {
		return children.iterator();
	}

	public Scan childAt(int index) {
		return children.elementAt(index);
	}

	public int indexOf(Scan child) {
		return children.indexOf(child);
	}

	public boolean add(Scan toadd) {
		if (toadd != null) {
			children.add(toadd);
			toadd.setParent(this);
			return true;
		}
		return false;
	}

	public boolean remove(Scan toremove) {
		if (toremove != null && children.contains(toremove)) {
			children.remove(toremove);
			toremove.setParent(null);
			return true;
		}
		return false;
	}

	public boolean canRemove(Scan toremove) {
		return (toremove != null && children.contains(toremove));
	}

	public boolean containsSubTree(Scan s) {
		if (s == null)
			return false;

		if (this == s)
			return true;

		for (Scan c : children) {
			if (c.containsSubTree(s))
				return true;
		}
		return false;
	}

	public Scan findInternalDocument(BaseDocument doc) {
		if (doc == null)
			return null;

		if (doc == theStructures || doc == theFragments || doc == theSpectra
				|| doc == thePeakList || doc == theAnnotatedPeakList
				|| doc == theNotes || theAnnotationReports.contains(doc))
			return this;

		for (Scan c : children) {
			Scan ret = c.findInternalDocument(doc);
			if (ret != null)
				return ret;
		}
		return null;
	}

	// events

	public void documentInit(BaseDocument.DocumentChangeEvent e) {
		if (e.getSource() == theSpectra)
			is_msms = (theSpectra.getNoScans() > 0) ? (theSpectra
					.getScanDataAt(0).getMSLevel() > 1) : false;
	}

	public void documentChanged(BaseDocument.DocumentChangeEvent e) {
		if (e.getSource() == theSpectra)
			is_msms = (theSpectra.getNoScans() > 0) ? (theSpectra
					.getScanDataAt(0).getMSLevel() > 1) : false;
	}

	// serialization

	static public Scan fromXML(GlycanWorkspace ws, Node scan_node)
			throws Exception {

		Scan ret = new Scan(ws);

		// set name
		String n = XMLUtils.getAttribute(scan_node, "name");
		if (n != null && n.length() > 0)
			ret.name = n;

		// set precursor m/z
		String mz = XMLUtils.getAttribute(scan_node, "precursor_mz");
		if (mz != null && mz.length() > 0)
			ret.precursor_mz = Double.valueOf(mz);

		// set ms level
		String msms = XMLUtils.getAttribute(scan_node, "is_msm");
		if (msms != null)
			ret.is_msms = Boolean.valueOf(msms);

		// set structures
		Node c_node = XMLUtils.findChild(scan_node, "Structures");
		if (c_node != null)
			ret.theStructures.fromXML(c_node, false);

		// set fragments
		Node f_node = XMLUtils.findChild(scan_node, "Fragments");
		if (f_node != null)
			ret.theFragments.fromXML(f_node, false);

		// set spectra
		Node sd_node = XMLUtils.findChild(scan_node, "SpectraDocument");
		if (sd_node != null)
			ret.theSpectra.fromXML(sd_node, false);

		// set peaklist
		Node pl_node = XMLUtils.findChild(scan_node, "PeakList");
		if (pl_node != null)
			ret.thePeakList.fromXML(pl_node, false);

		// set annotated peaklist
		Node apl_node = XMLUtils.findChild(scan_node, "AnnotatedPeakList");
		if (apl_node != null)
			ret.theAnnotatedPeakList.fromXML(apl_node, false);

		// set notes
		Node n_node = XMLUtils.findChild(scan_node, "Notes");
		if (n_node != null)
			ret.theNotes.fromXML(n_node, false);

		// set annotation reports
		Vector<Node> ard_nodes = XMLUtils.findAllChildren(scan_node,
				"AnnotationReportDocument");
		for (Node ard_node : ard_nodes) {
			AnnotationReportDocument toadd = new AnnotationReportDocument();
			toadd.fromXML(ard_node);
			ret.theAnnotationReports.add(toadd);
		}

		// set children
		Vector<Node> child_nodes = XMLUtils.findAllChildren(scan_node, "Scan");
		for (Node s_node : child_nodes)
			ret.add(Scan.fromXML(ws, s_node));

		return ret;
	}

	public Element toXML(Document document) {
		if (document == null)
			return null;

		// create root node
		Element scan_node = document.createElement("Scan");
		if (name != null)
			scan_node.setAttribute("name", "" + name);
		if (precursor_mz != null)
			scan_node.setAttribute("precursor_mz", "" + precursor_mz);
		scan_node.setAttribute("is_msms", "" + is_msms);

		// create structures node
		scan_node.appendChild(theStructures.toXML(document));

		// create structures node
		scan_node.appendChild(theFragments.toXML(document));

		// create spectra node
		scan_node.appendChild(theSpectra.toXML(document));

		// create peak list node
		scan_node.appendChild(thePeakList.toXML(document));

		// create annotated peak list node
		scan_node.appendChild(theAnnotatedPeakList.toXML(document));

		// create notes node
		scan_node.appendChild(theNotes.toXML(document));

		// create annotation reports node
		for (AnnotationReportDocument ard : theAnnotationReports)
			scan_node.appendChild(ard.toXML(document));

		// add children
		for (Scan c : children)
			scan_node.appendChild(c.toXML(document));

		return scan_node;
	}

	public static class SAXHandler extends SAXUtils.ObjectTreeHandler {

		private GlycanWorkspace theWorkspace;

		public SAXHandler(GlycanWorkspace ws) {
			theWorkspace = ws;
		}

		public boolean isElement(String namespaceURI, String localName,
				String qName) {
			return qName.equals(getNodeElementName());
		}

		public static String getNodeElementName() {
			return "Scan";
		}

		protected SAXUtils.ObjectTreeHandler getHandler(String namespaceURI,
				String localName, String qName) {
			if (qName.equals(GlycanDocument.SAXHandler.getNodeElementName()))
				return new GlycanDocument.SAXHandler(new GlycanDocument(
						theWorkspace), false);
			if (qName.equals(FragmentDocument.SAXHandler.getNodeElementName()))
				return new FragmentDocument.SAXHandler(new FragmentDocument(),
						false);
			if (qName.equals(SpectraDocument.SAXHandler.getNodeElementName()))
				return new SpectraDocument.SAXHandler(new SpectraDocument(),
						false);
			if (qName.equals(PeakList.SAXHandler.getNodeElementName()))
				return new PeakList.SAXHandler(new PeakList(), false);
			if (qName.equals(AnnotatedPeakList.SAXHandler.getNodeElementName()))
				return new AnnotatedPeakList.SAXHandler(
						new AnnotatedPeakList(), false);
			if (qName.equals(NotesDocument.SAXHandler.getNodeElementName()))
				return new NotesDocument.SAXHandler(new NotesDocument(), false);
			if (qName.equals(AnnotationReportDocument.SAXHandler
					.getNodeElementName()))
				return new AnnotationReportDocument.SAXHandler(
						new AnnotationReportDocument());
			if (qName.equals(Scan.SAXHandler.getNodeElementName()))
				return new Scan.SAXHandler(theWorkspace);
			return null;
		}

		protected void initContent(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {
			Scan ret = new Scan(theWorkspace);
			ret.name = stringAttribute(atts, "name", ret.name);
			ret.precursor_mz = doubleAttribute(atts, "precursor_mz",
					ret.precursor_mz);
			ret.is_msms = booleanAttribute(atts, "is_msm", ret.is_msms);
			object = ret;
		}

		protected Object finalizeContent(String namespaceURI, String localName,
				String qName) throws SAXException {

			Scan ret = (Scan) object;
			ret.theStructures = (GlycanDocument) getSubObject(
					GlycanDocument.SAXHandler.getNodeElementName(),
					ret.theStructures);
			ret.theFragments = (FragmentDocument) getSubObject(
					FragmentDocument.SAXHandler.getNodeElementName(),
					ret.theFragments);
			ret.theSpectra = (SpectraDocument) getSubObject(
					SpectraDocument.SAXHandler.getNodeElementName(),
					ret.theSpectra);
			ret.thePeakList = (PeakList) getSubObject(PeakList.SAXHandler
					.getNodeElementName(), ret.thePeakList);
			ret.theAnnotatedPeakList = (AnnotatedPeakList) getSubObject(
					AnnotatedPeakList.SAXHandler.getNodeElementName(),
					ret.theAnnotatedPeakList);
			ret.theNotes = (NotesDocument) getSubObject(
					NotesDocument.SAXHandler.getNodeElementName(), ret.theNotes);

			for (Object o : getSubObjects(AnnotationReportDocument.SAXHandler
					.getNodeElementName()))
				ret.theAnnotationReports.add((AnnotationReportDocument) o);
			for (Object o : getSubObjects(Scan.SAXHandler.getNodeElementName()))
				ret.add((Scan) o);

			return ret;
		}
	}

	public void write(TransformerHandler th) throws SAXException {

		AttributesImpl atts = new AttributesImpl();
		if (name != null)
			atts.addAttribute("", "", "name", "CDATA", "" + name);
		if (precursor_mz != null)
			atts.addAttribute("", "", "precursor_mz", "CDATA", ""
					+ precursor_mz);
		atts.addAttribute("", "", "is_msms", "CDATA", "" + is_msms);

		th.startElement("", "", "Scan", atts);

		// add documents
		theStructures.write(th);
		theFragments.write(th);
		theSpectra.write(th);
		thePeakList.write(th);
		theAnnotatedPeakList.write(th);
		theNotes.write(th);
		for (AnnotationReportDocument ard : theAnnotationReports)
			ard.write(th);

		// add children
		for (Scan c : children)
			c.write(th);

		th.endElement("", "", "Scan");
	}
	
	public boolean sync(AnnotationOptions ann_opts){
		return sync(this.getParent().getAnnotatedPeakList(),ann_opts);
	}

	public boolean sync(AnnotatedPeakList annotatedPeakList, AnnotationOptions ann_opts) {
		if(this.getPrecursorMZ()==null){
			return false;
		}
		
		boolean matchFound=false;
		Scan parentScan = this.getParent();
		if (parentScan != null) {
			this.getStructures().clear();
		
			PeakAnnotationMultiple peakAnnotations = annotatedPeakList
					.getAnnotations(new Peak(this.getPrecursorMZ(), .0),ann_opts.getMassAccuracy(),MassUnit.valueOfCompat(ann_opts.getMassAccuracyUnit()));
			if (peakAnnotations != null) {

				for (Vector<Annotation> structureToAnnotations : peakAnnotations
						.getAnnotations()) {
					for (Annotation annotation : structureToAnnotations) {
						this.getStructures().addStructure(
								annotation.getFragmentEntry().fragment);
					}

					if (structureToAnnotations.size() > 0) {
						matchFound = true;
					}
				}
			}
		} else {

		}
		return matchFound;
	}
}
