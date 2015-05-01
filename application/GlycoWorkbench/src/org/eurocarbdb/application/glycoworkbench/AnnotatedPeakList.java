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

import java.util.*;
import java.io.*;
import org.jfree.data.Range;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Stores a complete annotated list of peaks labeled from an MS or MS/MS
 * spectrum. Each peak can have multiple annotations. The annotations can come
 * from multiple structures, or from the same structure. A list of structures
 * from where the annotations have been originated, as well as a list of labeled
 * peaks are mantained. The annotations can be intact or fragment glycan
 * molecules. In case of an MS spectrum there is only one (empty) structure from
 * which the annotations are derived. The source structure can it-self be an
 * intact molecule, in case of an MS/MS spectrum, or a fragment molecule, in
 * case of an MSn spectrum.
 * 
 * @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
 */

public class AnnotatedPeakList extends BaseDocument implements
		SAXUtils.SAXWriter {

	private double max_intensity = 0.;
	private Vector<Glycan> structures;
	private Vector<PeakAnnotationCollection> peak_annotations_single;
	private Vector<PeakAnnotationMultiple> peak_annotations_multiple;

	private Vector<String> msa_header;

	// listeners

	private Vector<AnnotationChangeListener> ac_listeners = new Vector<AnnotationChangeListener>();

	/**
	 * Interface that must be implemented by all objects that want to register
	 * to listen for changes in the annotations
	 */
	public interface AnnotationChangeListener {

		/**
		 * Called when the number or identity of the source structures are
		 * changed
		 */
		public void structuresChanged(AnnotationChangeEvent e);

		/**
		 * Called when the number or identity of the annotations are changed
		 */
		public void annotationsChanged(AnnotationChangeEvent e);
	}

	/**
	 * Contains information about the event resulting from a change in the
	 * annotations
	 */
	static public class AnnotationChangeEvent {

		protected AnnotatedPeakList source;

		/**
		 * Constructor.
		 * 
		 * @param _source
		 *            the object that originated the event
		 */
		public AnnotationChangeEvent(AnnotatedPeakList _source) {
			source = _source;
		}

		/**
		 * Return the object that originated the event
		 */
		public AnnotatedPeakList getSource() {
			return source;
		}
	}

	/**
	 * Default constructor
	 */

	public AnnotatedPeakList() {
		super(false);
	}

	// -------- BaseDocument methods

	public String getName() {
		return "Annotated PeakList";
	}

	public javax.swing.ImageIcon getIcon() {
		return FileUtils.defaultThemeManager.getImageIcon("annpeaksdoc");
	}

	public Collection<javax.swing.filechooser.FileFilter> getFileFormats() {
		Vector<javax.swing.filechooser.FileFilter> filters = new Vector<javax.swing.filechooser.FileFilter>();

		filters.add(new ExtensionFileFilter("msa",
				"Cartoonist annotated peaklist file"));
		filters.add(new ExtensionFileFilter("gwa",
				"GlycoWorkbench annotated peaklist file"));
		filters.add(new ExtensionFileFilter(new String[] { "gwa", "msa" },
				"All annotated peaklist files"));

		return filters;
	}

	public javax.swing.filechooser.FileFilter getAllFileFormats() {
		return new ExtensionFileFilter(new String[] { "gwa", "msa" },
				"Annotated peaklist files");
	}

	public void initData() {
		max_intensity = 0.;
		structures = new Vector<Glycan>();
		peak_annotations_single = new Vector<PeakAnnotationCollection>();
		peak_annotations_multiple = new Vector<PeakAnnotationMultiple>();

		msa_header = new Vector<String>();
	}

	// ---- Data access

	private void addStructure(Glycan structure) {
		// update structures
		structures.add(structure);

		// update peak annotation singles
		peak_annotations_single.add(new PeakAnnotationCollection());

		// update peak annotation multiple
		for (PeakAnnotationMultiple pam : peak_annotations_multiple)
			pam.addStructure();
	}

	private void insertStructureAt(Glycan structure, int s_ind) {
		// update structures
		structures.insertElementAt(structure, s_ind);

		// update peak annotation singles
		peak_annotations_single.insertElementAt(new PeakAnnotationCollection(),
				s_ind);

		// update peak annotation multiple
		for (PeakAnnotationMultiple pam : peak_annotations_multiple)
			pam.insertStructureAt(s_ind);
	}

	private void removeStructureAt(int s_ind) {
		// update structures
		structures.removeElementAt(s_ind);

		// update peak annotation singles
		peak_annotations_single.removeElementAt(s_ind);

		// update peak annotation multiple
		for (PeakAnnotationMultiple pam : peak_annotations_multiple)
			pam.removeStructureAt(s_ind);

		if (peak_annotations_single.size() == 0)
			peak_annotations_multiple.clear();
	}

	/**
	 * Return the index of the specified structure in the list of structures
	 * from which the annotations are originated, or -1 if the structure cannot
	 * be found
	 */
	public int indexOf(Glycan structure) {
		if (structure == null)
			return -1;

		int ind = 0;
		for (Glycan s : structures) {
			if (s.compareTo(structure) == 0)
				return ind;
			ind++;
		}
		return -1;
	}

	/**
	 * Return the index of the peak object in the list of labeled peaks that
	 * have been annotated
	 * 
	 * @see PeakAnnotationMultiple
	 */
	public int indexOf(Peak p) {
		if (p == null)
			return -1;

		for (int i = 0; i < peak_annotations_multiple.size(); i++) {
			PeakAnnotationMultiple pam = peak_annotations_multiple.elementAt(i);
			if (pam.getPeak().compareTo(p) > 0)
				return -1;

			if (pam.getPeak().mzEquals(p))
				return i;
		}
		return -1;
	}
	
	/**
	 * Return the index of the peak object in the list of labeled peaks that
	 * have been annotated
	 * 
	 * @see PeakAnnotationMultiple
	 */
	public int indexOf(Peak p, double accuracy, MassUnit unit) {
		if (p == null)
			return -1;

		for (int i = 0; i < peak_annotations_multiple.size(); i++) {
			PeakAnnotationMultiple pam = peak_annotations_multiple.elementAt(i);
			if (pam.getPeak().compareTo(p) > 0)
				return -1;

			if (pam.getPeak().mzEquals(p,unit,accuracy))
				return i;
		}
		return -1;
	}

	/**
	 * Return the index of peak with the specified mass/charge value in the list
	 * of labeled peaks that have been annotated
	 * 
	 * @see PeakAnnotationMultiple
	 */
	public int indexOf(double mz_ratio) {
		double mz_tol = 0.000001;
		for (int i = 0; i < peak_annotations_multiple.size(); i++) {
			PeakAnnotationMultiple pam = peak_annotations_multiple.elementAt(i);
			if (pam.getPeak().getMZ() > mz_ratio + mz_tol)
				return -1;
			if (pam.getPeak().getMZ() > mz_ratio - mz_tol)
				return i;
		}
		return -1;
	}

	private void addPeakAnnotationsPVT(int ind, PeakAnnotationCollection pac) {

		// add the annotations to single
		peak_annotations_single.elementAt(ind).addPeakAnnotations(pac);

		// add the annotations to multiple
		for (PeakAnnotation pa : pac.getPeakAnnotations()) {
			PeakAnnotationMultiple pam = getAnnotations(pa.getPeak(), true);
			pam.addAnnotation(ind, pa.getAnnotation());
		}
	}

	private void addPeakAnnotationsPVT(Glycan structure,
			PeakAnnotationCollection pac, boolean merge) {

		// set structure
		int ind = -1;
		if (!merge || (ind = indexOf(structure)) == -1) {
			addStructure(structure);
			ind = structures.size() - 1;
		}

		addPeakAnnotationsPVT(ind, pac);
	}

	private void addPeakAnnotationPVT(int ind, PeakAnnotation pa) {

		// add the annotations to single
		peak_annotations_single.elementAt(ind).addPeakAnnotation(pa);

		// add the annotations to multiple
		PeakAnnotationMultiple pam = getAnnotations(pa.getPeak(), true);
		pam.addAnnotation(ind, pa.getAnnotation());
	}

	private void addPeakAnnotationPVT(Glycan structure, PeakAnnotation pa,
			boolean merge) {

		// set structure
		int ind = -1;
		if (!merge || (ind = indexOf(structure)) == -1) {
			addStructure(structure);
			ind = structures.size() - 1;
		}

		addPeakAnnotationPVT(ind, pa);
	}

	/**
	 * Add a collection of annotations deriving from a specified structure. If
	 * the structure is not existing it is first added to the list
	 * 
	 * @param structure
	 *            the molecule from which the annotations have been derived
	 * @param pac
	 *            the annotations collection
	 * @param merge
	 *            <code>true</code> if the annotations should be merged with the
	 *            existing ones
	 * @return <code>true</code> if the operation was successfull
	 */
	public boolean addPeakAnnotations(Glycan structure,
			PeakAnnotationCollection pac, boolean merge) {
		if (structure == null || pac == null)
			return false;

		addPeakAnnotationsPVT(structure, pac, merge);

		updateIntensities();
		fireStructuresChanged();
		fireDocumentChanged();
		return true;
	}

	/**
	 * Add a single annotation deriving from a specified structure. If the
	 * structure is not existing it is first added to the list
	 * 
	 * @param structure
	 *            the molecule from which the annotations have been derived
	 * @param pa
	 *            the annotation
	 * @param merge
	 *            <code>true</code> if the annotation should be merged with the
	 *            existing ones
	 * @return <code>true</code> if the operation was successfull
	 */
	public boolean addPeakAnnotation(Glycan structure, PeakAnnotation pa,
			boolean merge) {
		if (structure == null || pa == null)
			return false;

		addPeakAnnotationPVT(structure, pa, merge);

		updateIntensities();
		fireStructuresChanged();
		fireDocumentChanged();
		return true;
	}

	/**
	 * Add a collection of annotations deriving from a specified structure. The
	 * originating structure is added at the specified position in the list
	 * 
	 * @param structure
	 *            the molecule from which the annotations have been derived
	 * @param pac
	 *            the annotations collection
	 * @param s_ind
	 *            the index of the structure in the list
	 * @return <code>true</code> if the operation was successfull
	 */
	public boolean insertPeakAnnotationsAt(Glycan structure,
			PeakAnnotationCollection pac, int s_ind) {
		if (structure == null || pac == null)
			return false;

		// add the structure
		insertStructureAt(structure, s_ind);

		// add the annotations to single
		peak_annotations_single.setElementAt(pac, s_ind);

		// add the annotations to multiple
		for (PeakAnnotation pa : pac.getPeakAnnotations()) {
			PeakAnnotationMultiple pam = getAnnotations(pa.getPeak(), true);
			pam.addAnnotation(s_ind, pa.getAnnotation());
		}

		updateIntensities();
		fireStructuresChanged();
		fireDocumentChanged();
		return true;
	}

	/**
	 * Remove a peak from the list of labeled peaks and clear all its
	 * annotations
	 * 
	 * @return <code>true</code> if the operation was successfull
	 */
	public boolean removePeak(Peak p) {
		if (p == null)
			return false;

		if (!removeAnnotations(p))
			return false;

		for (PeakAnnotationCollection pac : peak_annotations_single)
			pac.removeAllPeakAnnotations(p);

		updateIntensities();
		fireDocumentChanged();
		fireAnnotationsChanged();
		return true;
	}

	private int removePeakAnnotation(int s_ind, PeakAnnotation pa, boolean fire) {
		if (pa == null || pa.getAnnotation() == null)
			return -1;

		// check if the peak annotation multiple exists
		PeakAnnotationMultiple pam = getAnnotations(pa.getPeak(), false);
		if (pam == null)
			return -1;

		// check if the the peak annotation can be removed
		int ret = peak_annotations_single.elementAt(s_ind)
				.removePeakAnnotation(pa, true);
		if (ret == -1)
			return -1;

		// remove the peak annotation multiple
		pam.removeAnnotation(s_ind, pa.getAnnotation());

		// fire events
		updateIntensities();
		if (fire) {
			fireDocumentChanged();
			fireAnnotationsChanged();
		}

		return ret;
	}

	/**
	 * Remove a specific annotation
	 * 
	 * @param s_ind
	 *            the index of the originating structure
	 * @param pa
	 *            the annotation to remove
	 * @return the index of the removed annotation or <code>-1</code> if it
	 *         couldn't be found
	 */
	public int removePeakAnnotation(int s_ind, PeakAnnotation pa) {
		return removePeakAnnotation(s_ind, pa, true);
	}

	/**
	 * Remove a collection of annotations
	 * 
	 * @param s_ind
	 *            the index of the originating structure
	 * @param toremove
	 *            the annotations to remove
	 * @return <code>true</code> if the operation was successfull
	 */
	public boolean removePeakAnnotations(int s_ind,
			Collection<PeakAnnotation> toremove) {
		if (toremove == null)
			return false;

		boolean removed = false;
		for (PeakAnnotation pa : toremove)
			removed |= (removePeakAnnotation(s_ind, pa, false) != -1);

		updateIntensities();
		if (removed) {
			fireDocumentChanged();
			fireAnnotationsChanged();
		}

		return removed;
	}

	/**
	 * Remove all annotations for a specific structure
	 * 
	 * @param s_ind
	 *            the index of the originating structure
	 */
	public void removePeakAnnotationsAt(int s_ind) {
		removeStructureAt(s_ind);
		updateIntensities();
		fireStructuresChanged();
		fireDocumentChanged();
	}

	/**
	 * Remove all annotations for a set of structures
	 * 
	 * @param s_inds
	 *            the index of the originating structure
	 */
	public void removePeakAnnotationsAt(int[] s_inds) {
		Arrays.sort(s_inds);
		for (int i = 0; i < s_inds.length; i++)
			removeStructureAt(s_inds[i] - i);

		updateIntensities();
		fireStructuresChanged();
		fireDocumentChanged();
	}

	/**
	 * Clear all annotations for a specified peak. Leave the peak in the list
	 */
	public void clearAnnotationsFor(Peak p) {
		for (PeakAnnotationCollection pac : peak_annotations_single)
			pac.clearAnnotations(p);

		for (PeakAnnotationMultiple pam : peak_annotations_multiple) {
			if (p.equals(pam.getPeak()))
				pam.clearAnnotations();
		}

		updateIntensities();
		fireDocumentChanged();
		fireAnnotationsChanged();
	}

	/**
	 * Clear all annotations for set of peaks. Leave the peaks in the list
	 */
	public void clearAnnotationsFor(Collection<Peak> peaks) {
		for (PeakAnnotationCollection pac : peak_annotations_single)
			pac.clearAnnotations(peaks);

		for (PeakAnnotationMultiple pam : peak_annotations_multiple) {
			if (peaks.contains(pam.getPeak()))
				pam.clearAnnotations();
		}

		updateIntensities();
		fireDocumentChanged();
		fireAnnotationsChanged();
	}

	private boolean removeAnnotations(Peak p) {
		int ind = indexOf(p);
		if (ind == -1)
			return false;

		peak_annotations_multiple.removeElementAt(ind);
		return true;
	}

	/**
	 * Update the annotations for all the peaks in a range of mass/charge
	 * values.
	 * 
	 * @param s_ind
	 *            the index of the structure
	 * @param start_mz
	 *            the minimum mass/charge value of the peaks to which the
	 *            annotations correspond
	 * @param end_mz
	 *            the maximum mass/charge value of the peaks to which the
	 *            annotations correspond
	 * @param toupdate
	 *            the new annotations
	 * @param merge
	 *            <code>true</code> if the old annotations should be kept
	 */
	public boolean updateAnnotations(int s_ind, double start_mz, double end_mz,
			Collection<PeakAnnotation> toupdate, boolean merge) {
		if (toupdate == null)
			return false;

		PeakAnnotationCollection pac = peak_annotations_single.get(s_ind);

		// remove non existing annotations
		boolean removed = false;
		if (!merge) {
			Vector<PeakAnnotation> peak_annotations = new Vector<PeakAnnotation>(
					pac.getPeakAnnotations()); // avoid concurrent modifications
			for (PeakAnnotation pa : peak_annotations) {
				if (start_mz == end_mz || pa.getPeak().getMZ() >= start_mz
						&& pa.getPeak().getMZ() <= end_mz) {
					if (!toupdate.contains(pa)) {
						removed = true;
						removePeakAnnotation(s_ind, pa, false);
					}
				}
			}
		}

		// add new annotations
		boolean added = false;
		for (PeakAnnotation pa : toupdate) {
			if (!pac.contains(pa)) {
				added = true;
				addPeakAnnotationPVT(s_ind, pa);
			}
		}

		updateAllIntensities();
		boolean changed = (removed || added);
		if (changed)
			fireDocumentChanged();
		return changed;
	}

	private PeakAnnotationMultiple getAnnotations(Peak p, boolean assertive) {

		// update max intensity
		// max_intensity = Math.max(max_intensity, p.getIntensity());

		// search position
		for (int i = 0; i < peak_annotations_multiple.size(); i++) {
			PeakAnnotationMultiple pam = peak_annotations_multiple.elementAt(i);
			if (pam.getPeak().compareTo(p) > 0) {
				if (assertive) {
					// insert here
					PeakAnnotationMultiple toadd = new PeakAnnotationMultiple(
							p, structures.size());
					peak_annotations_multiple.insertElementAt(toadd, i);
					return toadd;
				}
				return null;
			}
			if (pam.getPeak().equals(p))
				return pam;
		}

		if (assertive) {
			// append
			PeakAnnotationMultiple toadd = new PeakAnnotationMultiple(p,
					structures.size());
			peak_annotations_multiple.add(toadd);
			return toadd;
		}
		return null;
	}

	/**
	 * Return a new annotated peak list containing only the annotations with the
	 * best coverage
	 * 
	 * @param max_num
	 *            the number of annotations to return
	 * @see #getCoverage
	 */
	public AnnotatedPeakList getFirst(int max_num) {

		// get coverages and order them
		Vector<Pair<Double, Integer>> coverages = new Vector<Pair<Double, Integer>>();
		for (int i = 0; i < structures.size(); i++) {
			double cov = getCoverage(i);

			// find right position
			int ind = coverages.size();
			for (int l = 0; l < coverages.size(); l++) {
				if (cov > coverages.elementAt(l).getFirst()) {
					ind = l;
					break;
				}
			}

			// insert new value
			coverages.insertElementAt(new Pair<Double, Integer>(cov, i), ind);
		}

		// get best N structures
		AnnotatedPeakList ret = new AnnotatedPeakList();
		for (int i = 0; i < max_num && i < coverages.size(); i++) {
			int ind = coverages.elementAt(i).getSecond();
			ret.addPeakAnnotationsPVT(structures.elementAt(ind),
					peak_annotations_single.elementAt(ind), false);
		}
		ret.updateIntensities();

		return ret;
	}

	/**
	 * Return a new annotated peak list containing only the annotations
	 * originating from the specified structures
	 * 
	 * @param s_indexes
	 *            the indexes of the structures
	 */
	public AnnotatedPeakList extractCollections(int[] s_indexes) {
		AnnotatedPeakList ret = new AnnotatedPeakList();

		for (int i = 0; i < s_indexes.length; i++) {
			Glycan structure = structures.elementAt(s_indexes[i]);
			PeakAnnotationCollection pac = peak_annotations_single
					.elementAt(s_indexes[i]);
			ret.addPeakAnnotationsPVT(structure, pac, false);
		}
		ret.updateIntensities();

		return ret;
	}

	/**
	 * Return a new annotated peak list containing only the specified
	 * annotations originating from a single structure
	 * 
	 * @param s_ind
	 *            the index of the structure
	 * @param pac_indexes
	 *            the indexes of the annotations
	 * @see PeakAnnotationCollection
	 */
	public AnnotatedPeakList extractAnnotations(int s_ind, int[] pac_indexes) {
		AnnotatedPeakList ret = new AnnotatedPeakList();

		Glycan structure = structures.elementAt(s_ind);
		PeakAnnotationCollection pac = peak_annotations_single.elementAt(s_ind);

		ret.addStructure(structure);
		for (int l = 0; l < pac_indexes.length; l++)
			ret.addPeakAnnotationPVT(0, pac.getPeakAnnotation(pac_indexes[l]));
		ret.updateIntensities();

		return ret;
	}

	/**
	 * Return a new annotated peak list containing only the annotations for the
	 * specified peaks
	 * 
	 * @param pam_indexes
	 *            the indexes of the peaks
	 * @see PeakAnnotationMultiple
	 */
	public AnnotatedPeakList extractAnnotations(int[] pam_indexes) {

		AnnotatedPeakList ret = new AnnotatedPeakList();
		for (int i = 0; i < structures.size(); i++) {
			Glycan structure = structures.elementAt(i);
			PeakAnnotationCollection pac = peak_annotations_single.elementAt(i);

			ret.addStructure(structure);
			for (int l = 0; l < pam_indexes.length; l++) {
				PeakAnnotationMultiple pam = peak_annotations_multiple
						.elementAt(pam_indexes[l]);
				for (PeakAnnotation pa : pac.getPeakAnnotations(pam.getPeak()))
					ret.addPeakAnnotationPVT(i, pa);
			}
		}
		ret.updateIntensities();

		return ret;
	}

	/**
	 * Copy the content of another annotated peak list on the current one
	 */
	public void copy(AnnotatedPeakList src) {
		copy(src, true);
	}

	/**
	 * Copy the content of another annotated peak list on the current one
	 */
	public void copy(AnnotatedPeakList src,boolean fire) {
		setData(src, fire);
	}

	private void setData(AnnotatedPeakList src, boolean fire) {
		if (src != null) {
			// clear
			initData();

			
			
			// copy peak annotations
			for (int i = 0; i < src.structures.size(); i++)
				addPeakAnnotationsPVT(src.structures.elementAt(i),
						src.peak_annotations_single.elementAt(i), false);

			// fire events
			updateIntensities();
			if (fire) {
				fireStructuresChanged();
				fireDocumentChanged();
			}
		}
	}

	/**
	 * Merge the content of another annotated peak list with the current one
	 */
	public void merge(AnnotatedPeakList src) {
		mergeData(src, true);
	}

	private void mergeData(AnnotatedPeakList src, boolean fire) {
		if (src != null) {
			// copy peak annotations
			for (int i = 0; i < src.structures.size(); i++)
				addPeakAnnotationsPVT(src.structures.elementAt(i),
						src.peak_annotations_single.elementAt(i), true);

			// fire events
			updateIntensities();
			if (fire) {
				fireStructuresChanged();
				fireDocumentChanged();
			}
		}
	}

	private void updateIntensities() {
		// update intensities
		max_intensity = 0.;
		for (PeakAnnotationMultiple pam : peak_annotations_multiple)
			max_intensity = Math.max(max_intensity, pam.getPeak()
					.getIntensity());
	}

	private void updateAllIntensities() {
		updateIntensities();
		for (PeakAnnotationCollection pac : peak_annotations_single)
			pac.updateIntensities();
	}

	/**
	 * Return the list of labeled peaks
	 */
	public Vector<Peak> getPeaks() {
		Vector<Peak> ret = new Vector<Peak>();
		for (PeakAnnotationMultiple pam : peak_annotations_multiple)
			ret.add(pam.getPeak());
		return ret;
	}

	/**
	 * Return the list of labeled peaks as a 2xN table of mass/charge and
	 * intensity values
	 */
	public double[][] getPeakData() {

		int no_peaks = peak_annotations_multiple.size();
		double[][] ret = new double[2][];
		ret[0] = new double[no_peaks];
		ret[1] = new double[no_peaks];

		for (int i = 0; i < no_peaks; i++) {
			Peak p = peak_annotations_multiple.elementAt(i).getPeak();
			ret[0][i] = p.getMZ();
			ret[1][i] = p.getIntensity();
		}

		return ret;
	}

	// members access

	/**
	 * Return the number of labeled peaks
	 * 
	 * @see #getNoPeaks
	 */
	public int size() {
		return getNoPeaks();
	}

	/**
	 * Return the number of structures
	 */
	public int getNoStructures() {
		return structures.size();
	}

	/**
	 * Return a specific structure
	 * 
	 * @param s_ind
	 *            the index of the structure
	 */
	public Glycan getStructure(int s_ind) {
		return structures.elementAt(s_ind);
	}

	/**
	 * Return the list of structures
	 */
	public Vector<Glycan> getStructures() {
		return structures;
	}

	/**
	 * Return the number of peaks
	 */
	public int getNoPeaks() {
		return peak_annotations_multiple.size();
	}

	/**
	 * Return a specific peak
	 * 
	 * @param p_ind
	 *            the index of the peak
	 */
	public Peak getPeak(int p_ind) {
		return peak_annotations_multiple.elementAt(p_ind).getPeak();
	}

	/**
	 * Return the mass/charge value of a specific peak
	 * 
	 * @param p_ind
	 *            the index of the peak
	 */
	public double getMZ(int p_ind) {
		return getPeak(p_ind).getMZ();
	}

	/**
	 * Return the intensity of a specific peak
	 * 
	 * @param p_ind
	 *            the index of the peak
	 */
	public double getIntensity(int p_ind) {
		return getPeak(p_ind).getIntensity();
	}

	/**
	 * Return the intensity of a specific peak normalize by the maximum
	 * intensity in the list of labeled peaks
	 * 
	 * @param p_ind
	 *            the index of the peak
	 */
	public double getRelativeIntensity(int p_ind) {
		if (max_intensity == 0.)
			return getPeak(p_ind).getIntensity();
		else
			return 100. * getPeak(p_ind).getIntensity() / max_intensity;
	}

	/**
	 * Return the index of the peak whose mass/charge values is nearest to the
	 * specified value, or -1 if the annotated peak list is empty
	 */
	public int nearestTo(double mz) {
		if (structures.size() == 0)
			return -1;

		int ind = 0;
		double last_err = Double.POSITIVE_INFINITY;
		for (PeakAnnotationMultiple pam : peak_annotations_multiple) {
			double err = Math.abs(mz - pam.getPeak().getMZ());
			if (err > last_err)
				return ind - 1;
			last_err = err;
			ind++;
		}
		return ind - 1;
	}

	/**
	 * Return <code>true</code> if the specified peak is annotated
	 */
	public boolean isAnnotated(Peak p) {
		return peak_annotations_multiple.elementAt(indexOf(p)).isAnnotated();
	}

	/**
	 * Return <code>true</code> if the peak at the specified position is
	 * annotated
	 */
	public boolean isAnnotated(int p_ind) {
		return peak_annotations_multiple.elementAt(p_ind).isAnnotated();
	}

	/**
	 * Return all annotations for all peaks and from all structures. Each
	 * PeakAnnotationMultiple correspond to one peak
	 */
	public Collection<PeakAnnotationMultiple> getAnnotations() {
		return peak_annotations_multiple;
	}

	public PeakAnnotationMultiple getAnnotations(Peak p) {
		return getAnnotations(p, 0.0001 ,MassUnit.PPM);
	}
	
	/**
	 * Return all annotations for a specified peak and from all structures
	 */
	public PeakAnnotationMultiple getAnnotations(Peak p, double mz_accuracy, MassUnit unit) {
		int comparison = indexOf(p,mz_accuracy,unit);
		if (comparison != -1) {
			return peak_annotations_multiple.elementAt(comparison);
		} else {
			return null;
		}
	}

	/**
	 * Return all annotations for a specified peak and from all structures
	 */
	public PeakAnnotationMultiple getAnnotations(int p_ind) {
		return peak_annotations_multiple.elementAt(p_ind);
	}

	/**
	 * Return all annotations for a specified peak and from a specific structure
	 */
	public Vector<Annotation> getAnnotations(Peak p, int s_ind) {
		return peak_annotations_multiple.elementAt(indexOf(p)).getAnnotations(
				s_ind);
	}

	/**
	 * Return all annotations for a specified peak and from a specific structure
	 */
	public Vector<Annotation> getAnnotations(int p_ind, int s_ind) {
		return peak_annotations_multiple.elementAt(p_ind).getAnnotations(s_ind);
	}

	/**
	 * Return the structure of all annotations for a specified peak and from a
	 * specific structure
	 */
	public Vector<Glycan> getFragments(int p_ind, int s_ind) {
		Vector<Glycan> ret = new Vector<Glycan>();
		for (Annotation a : peak_annotations_multiple.elementAt(p_ind)
				.getAnnotations(s_ind))
			ret.add(a.getFragmentEntry().fragment);
		return ret;
	}

	/**
	 * Return all annotations for all peaks and from all structures Each
	 * {@link PeakAnnotationCollection} correspond to one structure
	 */
	public Collection<PeakAnnotationCollection> getPeakAnnotationCollections() {
		return peak_annotations_single;
	}

	/**
	 * Return all annotations for all peaks and from a specific structure.
	 */
	public PeakAnnotationCollection getPeakAnnotationCollection(int s_ind) {
		return peak_annotations_single.elementAt(s_ind);
	}

	/**
	 * Return all annotations for all peaks and from a specific structure.
	 * Return <code>null</code> if the structure is not found
	 */
	public PeakAnnotationCollection getPeakAnnotationCollection(Glycan structure) {
		int s_ind = structures.indexOf(structure);
		if (s_ind == -1)
			return null;
		return peak_annotations_single.elementAt(s_ind);
	}

	// statistics

	/**
	 * Return the sum of the intensities of the annotated peaks as a percentage
	 * of the total intensity
	 * 
	 * @param s_ind
	 *            the index of the structure
	 */
	public double getCoverage(int s_ind) {
		double assigned = 0.;
		double total = 0.;
		for (PeakAnnotationMultiple pam : peak_annotations_multiple) {
			Peak p = pam.getPeak();

			if (pam.isAnnotated(s_ind))
				assigned += p.getIntensity();
			total += p.getIntensity();
		}
		return 100. * assigned / total;
	}

	/**
	 * Return the RMSD between the mass/charge values of the peaks and the
	 * mass/charge values of their annotations
	 * 
	 * @param s_ind
	 *            the index of the structure
	 */
	public double getRMSD(int s_ind) {
		int count = 0;
		double rmsd = 0;
		for (PeakAnnotationMultiple pam : peak_annotations_multiple) {
			if (pam.isAnnotated(s_ind)) {
				rmsd += Math.pow(pam.getBestAccuracy(s_ind), 2.);
				count++;
			}
		}
		return (count > 0) ? Math.sqrt(rmsd / count) : 0.;
	}

	/**
	 * Return the RMSD between the mass/charge values of the peaks and the
	 * mass/charge values of their annotations in PPM
	 * 
	 * @param s_ind
	 *            the index of the structure
	 */
	public double getRMSD_PPM(int s_ind) {
		int count = 0;
		double rmsd = 0;
		for (PeakAnnotationMultiple pam : peak_annotations_multiple) {
			if (pam.isAnnotated(s_ind)) {
				rmsd += Math.pow(pam.getBestAccuracyPPM(s_ind), 2.);
				count++;
			}
		}
		return (count > 0) ? Math.sqrt(rmsd / count) : 0.;
	}

	/**
	 * Return the number of peaks with intensity greater than a certain value
	 * 
	 * @param min_rel_int
	 *            the minimum value of intensity
	 */
	public int getNoPeaks(double min_rel_int) {
		int count = 0;
		double min_int = min_rel_int / 100. * max_intensity;
		for (PeakAnnotationMultiple pam : peak_annotations_multiple) {
			Peak p = pam.getPeak();
			if (p.getIntensity() >= min_int)
				count++;
		}
		return count;
	}

	/**
	 * Return the number of peaks with annotations derived from a specific
	 * structure
	 * 
	 * @param s_ind
	 *            the index of the structure
	 */
	public int getNoAnnotatedPeaks(int s_ind) {
		return getNoAnnotatedPeaks(s_ind, 0.);
	}

	/**
	 * Return the number of peaks with annotations derived from a specific
	 * structure and intensity greater than a certain value
	 * 
	 * @param s_ind
	 *            the index of the structure
	 * @param min_rel_int
	 *            the minimum value of intensity
	 */
	public int getNoAnnotatedPeaks(int s_ind, double min_rel_int) {
		int count = 0;
		double min_int = min_rel_int / 100. * max_intensity;
		for (PeakAnnotationMultiple pam : peak_annotations_multiple) {
			Peak p = pam.getPeak();
			if (p.getIntensity() >= min_int && pam.isAnnotated(s_ind))
				count++;
		}
		return count;
	}

	/**
	 * Return the range of mass/charge values of the labeled peaks
	 */
	public Range getMZRange() {
		return new Range(getMinMZ(), getMaxMZ());
	}

	/**
	 * Return the minimum mass/charge value of the labeled peaks
	 */
	public double getMinMZ() {
		return peak_annotations_multiple.firstElement().getPeak().getMZ();
	}

	/**
	 * Return the maximum mass/charge value of the labeled peaks
	 */
	public double getMaxMZ() {
		return peak_annotations_multiple.lastElement().getPeak().getMZ();
	}

	/**
	 * Return the range of accuracies for all the annotations from the specific
	 * structure
	 */
	public Range getAccuracyRange(int s_ind) {
		return new Range(getMinAccuracy(s_ind), getMaxAccuracy(s_ind));
	}

	/**
	 * Return the minimum accuracy for all the annotations from the specific
	 * structure
	 */
	public double getMinAccuracy(int s_ind) {
		return peak_annotations_single.elementAt(s_ind).getMinAccuracy();
	}

	/**
	 * Return the maximum accuracy for all the annotations from the specific
	 * structure
	 */
	public double getMaxAccuracy(int s_ind) {
		return peak_annotations_single.elementAt(s_ind).getMaxAccuracy();
	}

	/**
	 * Return the range of accuracies in PPM for all the annotations from the
	 * specific structure
	 */
	public Range getAccuracyRangePPM(int s_ind) {
		return new Range(getMinAccuracyPPM(s_ind), getMaxAccuracyPPM(s_ind));
	}

	/**
	 * Return the minimum accuracy in PPM for all the annotations from the
	 * specific structure
	 */
	public double getMinAccuracyPPM(int s_ind) {
		return peak_annotations_single.elementAt(s_ind).getMinAccuracyPPM();
	}

	/**
	 * Return the maximum accuracy in PPM for all the annotations from the
	 * specific structure
	 */
	public double getMaxAccuracyPPM(int s_ind) {
		return peak_annotations_single.elementAt(s_ind).getMaxAccuracyPPM();
	}

	/**
	 * Return a 2xN table of mass/charge and accuracy values for all annotated
	 * peaks and a specific structures. These values can be use to check the
	 * calibration of the spectra
	 */
	public double[][] getCalibrationData(int s_ind) {
		PeakAnnotationCollection pac = peak_annotations_single.elementAt(s_ind);
		int n = pac.size();

		// count Annotated Peaks
		int count = 0;
		for (int i = 0; i < n; i++) {
			if (pac.isAnnotated(i))
				count++;
		}

		// get calibration data
		double[][] ret = new double[2][];
		ret[0] = new double[count];
		ret[1] = new double[count];
		for (int i = 0, l = 0; i < n; i++) {
			PeakAnnotation pa = pac.elementAt(i);
			if (pa.isAnnotated()) {
				ret[0][l] = pa.getPeak().getMZ();
				ret[1][l] = pa.getAccuracy();
				l++;
			}
		}

		return ret;
	}

	/**
	 * Return a 2xN table of mass/charge and accuracy values for all annotated
	 * peaks and a specific structures. Only the most accurate annotation for
	 * each peak is used. These values can be use to check the calibration of
	 * the spectra
	 */
	public double[][] getBestCalibrationData(int s_ind) {
		// count Annotated Peaks
		int count = 0;
		for (PeakAnnotationMultiple pam : peak_annotations_multiple) {
			if (pam.isAnnotated(s_ind))
				count++;
		}

		// get calibration data
		int n = peak_annotations_multiple.size();

		double[][] ret = new double[2][];
		ret[0] = new double[count];
		ret[1] = new double[count];
		for (int i = 0, l = 0; i < n; i++) {
			PeakAnnotationMultiple pam = peak_annotations_multiple.elementAt(i);
			if (pam.isAnnotated(s_ind)) {
				ret[0][l] = pam.getPeak().getMZ();
				ret[1][l] = pam.getBestAccuracy(s_ind);
				l++;
			}
		}

		return ret;
	}

	/**
	 * Return a 2xN table of mass/charge and accuracy values in PPM for all
	 * annotated peaks and a specific structures. These values can be use to
	 * check the calibration of the spectra
	 */
	public double[][] getCalibrationDataPPM(int s_ind) {
		PeakAnnotationCollection pac = peak_annotations_single.elementAt(s_ind);
		int n = size();

		// count Annotated Peaks
		int count = 0;
		for (int i = 0; i < n; i++) {
			if (pac.isAnnotated(i))
				count++;
		}

		// get calibration data
		double[][] ret = new double[2][];
		ret[0] = new double[count];
		ret[1] = new double[count];
		for (int i = 0, l = 0; i < n; i++) {
			PeakAnnotation pa = pac.elementAt(i);
			if (pa.isAnnotated()) {
				ret[0][l] = pa.getPeak().getMZ();
				ret[1][l] = pa.getAccuracyPPM();
				l++;
			}
		}

		return ret;
	}

	/**
	 * Return a 2xN table of mass/charge and accuracy values in PPM for all
	 * annotated peaks and a specific structures. Only the most accurate
	 * annotation for each peak is used. These values can be use to check the
	 * calibration of the spectra
	 */
	public double[][] getBestCalibrationDataPPM(int s_ind) {
		// count Annotated Peaks
		int count = 0;
		for (PeakAnnotationMultiple pam : peak_annotations_multiple) {
			if (pam.isAnnotated(s_ind))
				count++;
		}

		// get calibration data
		int n = peak_annotations_multiple.size();

		double[][] ret = new double[2][];
		ret[0] = new double[count];
		ret[1] = new double[count];
		for (int i = 0, l = 0; i < n; i++) {
			PeakAnnotationMultiple pam = peak_annotations_multiple.elementAt(i);
			if (pam.isAnnotated(s_ind)) {
				ret[0][l] = pam.getPeak().getMZ();
				ret[1][l] = pam.getBestAccuracyPPM(s_ind);
				l++;
			}
		}

		return ret;
	}

	private boolean updatePeakPVT(Peak p) {
		int ind = indexOf(p.getMZ());
		if (ind == -1)
			return false;

		PeakAnnotationMultiple pam = peak_annotations_multiple.get(ind);
		pam.getPeak().setIntensity(p.getIntensity());

		for (PeakAnnotationCollection pac : peak_annotations_single) {
			for (PeakAnnotation pa : pac.getPeakAnnotations(p.getMZ()))
				pa.getPeak().setIntensity(p.getIntensity());
		}

		return true;
	}

	/**
	 * Notify the document of a change in the intensity for a specific peak
	 */
	public boolean updatePeak(Peak p) {
		if (updatePeakPVT(p)) {
			updateAllIntensities();
			fireDocumentChanged();
			return true;
		}
		return false;
	}

	/**
	 * Notify the document of a change in the intensity for a list of peaks
	 */
	public boolean updatePeaks(Collection<Peak> peaks) {
		boolean changed = false;
		for (Peak p : peaks)
			changed |= updatePeakPVT(p);

		if (changed) {
			updateAllIntensities();
			fireDocumentChanged();
		}
		return changed;
	}

	// --------------
	// events

	/**
	 * Register a listener to receive events when the annotations are changed
	 */
	public void addAnnotationChangeListener(AnnotationChangeListener l) {
		if (l != null)
			ac_listeners.add(l);
	}

	/**
	 * De-register one of the listeners that was receiving events when the
	 * annotations are changed
	 */
	public void removeAnnotationChangeListener(AnnotationChangeListener l) {
		if (l != null)
			ac_listeners.remove(l);
	}

	/**
	 * Notify all listeners of a change in the number or identity of the
	 * structures
	 */
	public void fireStructuresChanged() {
		if (ac_listeners != null) {
			for (AnnotationChangeListener acl : ac_listeners)
				acl.structuresChanged(new AnnotationChangeEvent(this));
		}
	}

	/**
	 * Notify all listeners of a change in the number or identity of the
	 * annotations
	 */
	public void fireAnnotationsChanged() {
		if (ac_listeners != null) {
			for (AnnotationChangeListener acl : ac_listeners)
				acl.annotationsChanged(new AnnotationChangeEvent(this));
		}
	}

	public void fireDocumentInit() {
		super.fireDocumentInit();
		fireStructuresChanged();
	}

	public void fireDocumentInit(BaseDocument source) {
		super.fireDocumentInit(source);
		if (source == this)
			fireStructuresChanged();
	}

	// ----------------------------------------
	// Serialization

	public void fromString(String str, boolean merge) throws Exception {
		fromString(str, merge, false);
	}

	public void fromString(String str, boolean merge, boolean fire)
			throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes());
		read(bis, merge);

		if (fire) {
			fireStructuresChanged();
			fireDocumentChanged();
		}
	}

	protected void read(InputStream is, boolean merge) throws Exception {
		BufferedInputStream bis = new BufferedInputStream(is);
		if (bis.markSupported()) {
			// check for MSA content
			bis.mark(100);

			byte[] buf = new byte[18];
			if (bis.read(buf, 0, 18) == 18) {
				bis.reset();

				String str_buf = new String(buf);
				if (str_buf.startsWith("# .msa version 002")) {
					this.readMSA2(bis, merge);
					return;
				} else if (str_buf.startsWith("# .msa version 003")) {
					this.readMSA3(bis, merge);
					return;
				}
			} else
				bis.reset();
		}
		SAXUtils.read(bis, new SAXHandler(this, merge));
	}

	public String toString() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			write(bos);
			return bos.toString();
		} catch (Exception e) {
			LogUtils.report(e);
			return "";
		}
	}

	public void write(OutputStream os) throws Exception {
		/*
		 * Document document = XMLUtils.newDocument(); if( document==null )
		 * return "";
		 * 
		 * Element apl_node = toXML(document); if( apl_node == null ) return "";
		 * 
		 * document.appendChild(apl_node); XMLUtils.write(bos,document);
		 */
		SAXUtils.write(os, this);
	}

	/**
	 * Create a new document from its XML representation as part of a DOM tree.
	 */
	public void fromXML(Node apl_node, boolean merge) throws Exception {

		// clear
		if (!merge) {
			resetStatus();
			initData();
		} else
			setChanged(true);

		// read annotations
		Vector<Node> ann_nodes = XMLUtils.findAllChildren(apl_node,
				"Annotations");
		for (Node ann_node : ann_nodes) {
			Node s_node = XMLUtils.assertChild(ann_node, "Glycan");
			Glycan structure = Glycan.fromXML(s_node, new MassOptions());

			Node pac_node = XMLUtils.assertChild(ann_node,
					"PeakAnnotationCollection");
			PeakAnnotationCollection pac = PeakAnnotationCollection
					.fromXML(pac_node);

			addPeakAnnotationsPVT(structure, pac, true);
		}

		updateIntensities();
	}

	/**
	 * Create an XML representation of this object to be part of a DOM tree.
	 */
	public Element toXML(Document document) {
		if (document == null)
			return null;

		// create root node
		Element apl_node = document.createElement("AnnotatedPeakList");
		if (apl_node == null)
			return null;

		// add annotations
		for (int i = 0; i < structures.size(); i++) {
			Element s_node = structures.elementAt(i).toXML(document);
			if (s_node == null)
				continue;

			Element pac_node = peak_annotations_single.elementAt(i).toXML(
					document);
			if (pac_node == null)
				continue;

			Element ann_node = document.createElement("Annotations");
			if (ann_node == null)
				continue;

			ann_node.appendChild(s_node);
			ann_node.appendChild(pac_node);
			apl_node.appendChild(ann_node);
		}

		return apl_node;
	}

	/**
	 * Default SAX handler to read a representation of all annotations of a
	 * single structure from an XML stream.
	 */
	public static class AnnotationsSAXHandler extends
			SAXUtils.ObjectTreeHandler {

		public boolean isElement(String namespaceURI, String localName,
				String qName) {
			return qName.equals(getNodeElementName());
		}

		/**
		 * Return the element tag recognized by this handler
		 */
		public static String getNodeElementName() {
			return "Annotations";
		}

		protected SAXUtils.ObjectTreeHandler getHandler(String namespaceURI,
				String localName, String qName) {
			if (qName.equals(Glycan.SAXHandler.getNodeElementName()))
				return new Glycan.SAXHandler(new MassOptions());
			if (qName.equals(PeakAnnotationCollection.SAXHandler
					.getNodeElementName()))
				return new PeakAnnotationCollection.SAXHandler();
			return null;
		}

		protected Object finalizeContent(String namespaceURI, String localName,
				String qName) throws SAXException {
			Glycan g = (Glycan) getSubObject(Glycan.SAXHandler
					.getNodeElementName(), true);
			PeakAnnotationCollection pac = (PeakAnnotationCollection) getSubObject(
					PeakAnnotationCollection.SAXHandler.getNodeElementName(),
					true);
			return new Pair<Glycan, PeakAnnotationCollection>(g, pac);
		}
	}

	/**
	 * Default SAX handler to read a representation of this object from an XML
	 * stream.
	 */
	public static class SAXHandler extends SAXUtils.ObjectTreeHandler {

		private AnnotatedPeakList theDocument;
		private boolean merge;

		public SAXHandler(AnnotatedPeakList _doc, boolean _merge) {
			theDocument = _doc;
			merge = _merge;
		}

		public boolean isElement(String namespaceURI, String localName,
				String qName) {
			return qName.equals(getNodeElementName());
		}

		public static String getNodeElementName() {
			return "AnnotatedPeakList";
		}

		protected SAXUtils.ObjectTreeHandler getHandler(String namespaceURI,
				String localName, String qName) {
			if (qName.equals(AnnotationsSAXHandler.getNodeElementName()))
				return new AnnotationsSAXHandler();
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
			for (Object o : getSubObjects(AnnotationsSAXHandler
					.getNodeElementName())) {
				Pair<Glycan, PeakAnnotationCollection> p = (Pair<Glycan, PeakAnnotationCollection>) o;
				theDocument.addPeakAnnotationsPVT(p.getFirst(), p.getSecond(),
						true);
			}
			theDocument.updateIntensities();

			return (object = theDocument);
		}
	}

	public void write(TransformerHandler th) throws SAXException {
		th.startElement("", "", "AnnotatedPeakList", new AttributesImpl());

		for (int i = 0; i < structures.size(); i++) {
			th.startElement("", "", "Annotations", new AttributesImpl());
			structures.elementAt(i).write(th);
			peak_annotations_single.elementAt(i).write(th);
			th.endElement("", "", "Annotations");
		}

		th.endElement("", "", "AnnotatedPeakList");
	}

	// integration with cartoonist

	private void readMSA2(InputStream is, boolean merge) throws Exception {
		if (!merge) {
			this.resetStatus();
			this.initData();
		} else
			this.setChanged(true);

		String line;
		Glycan empty = new Glycan();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while ((line = br.readLine()) != null) {

			// parse line
			if (line.length() == 0 || line.startsWith("#")) {
				// store comments in the msa header
				msa_header.add(line);
				continue;
			}

			Vector<String> tokens = TextUtils.tokenize(line, " ");
			if (tokens.size() < 15)
				throw new Exception("Invalid format for line: " + line);

			// get peak
			double mz_value = Double.valueOf(tokens.get(1));
			int offset = Integer.valueOf(tokens.get(2));
			if (offset > 0)
				mz_value -= (double) offset; // reduce to C12 isotope

			Peak peak = new Peak(mz_value, 1);
			if (peak == null)
				throw new Exception("m/z value not found in peaklist: "
						+ mz_value);

			// get cartoons
			Vector<Glycan> cartoons = new Vector<Glycan>();
			if (tokens.size() > 15) {
				// set default mass options for MALDI on permethylated glycans
				MassOptions mopt = new MassOptions(MassOptions.PERMETHYLATED,
						"freeEnd"); // !!! o-glycans
				mopt.ION_CLOUD = new IonCloud("Na");

				// parse cartoons
				GlycoMindsParser parser = new GlycoMindsParser();
				String str_cartoons = tokens.get(15);
				str_cartoons = str_cartoons.substring(1,
						str_cartoons.length() - 1);

				for (String str_cartoon : TextUtils.tokenize(str_cartoons, ";"))
					cartoons.add(parser.readGlycan(str_cartoon, mopt));
			}

			// add annotations

			if (cartoons.size() > 0) {
				// add cartoon annotations
				for (Glycan cartoon : cartoons)
					this.addPeakAnnotation(empty, new PeakAnnotation(peak,
							new FragmentEntry(cartoon, "")), true);
			} else {
				// add empty annotation
				this.addPeakAnnotation(empty, new PeakAnnotation(peak), true);
			}

		}
	}

	private void readMSA3(InputStream is, boolean merge) throws Exception {
		if (!merge) {
			this.resetStatus();
			this.initData();
		} else
			this.setChanged(true);

		String line;
		Glycan empty = new Glycan();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while ((line = br.readLine()) != null) {

			// parse line
			if (line.length() == 0 || line.startsWith("#")) {
				// store comments in the msa header
				msa_header.add(line);
				continue;
			}

			Vector<String> tokens = TextUtils.tokenize(line, " ");
			if (tokens.size() < 17)
				throw new Exception("Invalid format for line: " + line);

			// get peak
			double mz_value = Double.valueOf(tokens.get(1));
			int offset = Integer.valueOf(tokens.get(2));
			if (offset > 0)
				mz_value -= (double) offset; // reduce to C12 isotope

			double int_value = Double.valueOf(tokens.get(5));
			Peak peak = new Peak(mz_value, int_value);

			// get cartoons
			Vector<Glycan> cartoons = new Vector<Glycan>();
			if (tokens.size() > 17) {
				// set default mass options for MALDI on permethylated glycans
				MassOptions mopt = new MassOptions(MassOptions.PERMETHYLATED,
						"freeEnd"); // !!! o-glycans

				mopt.ION_CLOUD = parseCharges(tokens.get(3));

				// parse cartoons
				GlycoMindsParser parser = new GlycoMindsParser();
				String str_cartoons = tokens.get(17);
				str_cartoons = str_cartoons.substring(1,
						str_cartoons.length() - 1);

				for (String str_cartoon : TextUtils.tokenize(str_cartoons, ";"))
					cartoons.add(parser.readGlycan(str_cartoon, mopt));
			}

			// add annotations
			if (cartoons.size() > 0) {
				// add cartoon annotations
				for (Glycan cartoon : cartoons)
					this.addPeakAnnotation(empty, new PeakAnnotation(peak,
							new FragmentEntry(cartoon, "")), true);
			} else {
				// add empty annotation
				this.addPeakAnnotation(empty, new PeakAnnotation(peak), true);
			}

		}
	}

	private IonCloud parseCharges(String charges) throws Exception {
		IonCloud ret = new IonCloud();
		String[] tokens = charges.split(",");

		for (int i = 0; i < tokens.length; i++) {
			// parse count;
			int c = 0;
			for (; c < tokens[i].length()
					&& Character.isDigit(tokens[i].charAt(c)); c++)
				;

			int count = 1;
			if (c > 0)
				count = Integer.valueOf(tokens[i].substring(0, c));

			// parse ion
			String ion = tokens[i].substring(c);
			if (ion.equals("H+"))
				ret.add(MassOptions.ION_H, count);
			else if (ion.equals("Na+"))
				ret.add(MassOptions.ION_NA, count);
			else if (ion.equals("Li+"))
				ret.add(MassOptions.ION_LI, count);
			else if (ion.equals("K+"))
				ret.add(MassOptions.ION_K, count);
			else
				throw new Exception("Unrecognized ion " + ion);
		}
		return ret;
	}
}
