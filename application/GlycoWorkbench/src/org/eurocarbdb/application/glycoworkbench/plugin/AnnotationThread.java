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

package org.eurocarbdb.application.glycoworkbench.plugin;

import org.eurocarbdb.application.glycanbuilder.*;

import org.eurocarbdb.application.glycoworkbench.*;

import java.util.*;

public class AnnotationThread extends Thread implements GeneratorListener {

	protected int progress = 0;

	protected final int FRAGMENTER = 1;
	protected final int COLLECTION = 2;
	protected final int GENERATOR = 3;
	protected int mode = FRAGMENTER;

	protected int no_structures = 0;
	protected boolean has_fuzzy = false;
	protected boolean has_not_fuzzy = true;

	protected AnnotatedPeakList annotated_peaks = null;
	protected PeakList peaks = null;

	protected PeakAnnotationCollection match_coll = null;
	Vector<IonCloud> ion_clouds = null;

	protected Collection<Glycan> structures = null;
	protected Fragmenter fragmenter = null;
	protected FragmentDocument frag_doc = null;
	protected Generator generator = null;

	protected AnnotationOptions ann_opt = null;

	public AnnotationThread(PeakList _peaks, Collection<Glycan> _structures,
			Fragmenter _fragmenter, AnnotationOptions _ann_opt) {

		
		
		annotated_peaks = new AnnotatedPeakList();
		match_coll = new PeakAnnotationCollection();
		peaks = _peaks;

		structures = _structures;
		fragmenter = _fragmenter;
		frag_doc = null;
		generator = null;

		ann_opt = _ann_opt;

		mode = FRAGMENTER;
	}

	public AnnotationThread(PeakList _peaks, Glycan _structure,
			FragmentCollection _frag_coll, AnnotationOptions _ann_opt) {

		annotated_peaks = new AnnotatedPeakList();
		match_coll = new PeakAnnotationCollection();
		peaks = _peaks;

		structures = Collections.singleton(_structure);
		fragmenter = null;
		frag_doc = new FragmentDocument();
		frag_doc.addFragments(_structure, _frag_coll);
		generator = null;

		ann_opt = _ann_opt;

		mode = COLLECTION;
	}

	public AnnotationThread(PeakList _peaks, FragmentDocument _frag_doc,
			AnnotationOptions _ann_opt) {

		annotated_peaks = new AnnotatedPeakList();
		match_coll = new PeakAnnotationCollection();
		peaks = _peaks;

		structures = _frag_doc.getStructures();
		fragmenter = null;
		frag_doc = _frag_doc;
		generator = null;

		ann_opt = _ann_opt;

		mode = COLLECTION;
	}

	public AnnotationThread(PeakList _peaks, Generator _generator,
			AnnotationOptions _ann_opt) {
		annotated_peaks = new AnnotatedPeakList();
		match_coll = new PeakAnnotationCollection();
		peaks = _peaks;

		structures = _generator.getMotifs();
		fragmenter = null;
		frag_doc = null;
		generator = _generator;

		ann_opt = _ann_opt;

		mode = GENERATOR;
	}

	public AnnotatedPeakList getAnnotatedPeaks() {
		return annotated_peaks;
	}

	public int getProgress() {
		return progress;
	}

	public int getTarget() {
		return (structures != null && peaks != null) ? (structures.size() * peaks
				.size())
				: 0;
	}

	public int getNoStructures() {
		return (structures != null) ? structures.size() : 0;
	}

	public int getNonEmptyStructures() {
		return no_structures;
	}

	public boolean hasFuzzyStructures() {
		return has_fuzzy;
	}

	public boolean hasNonFuzzyStructures() {
		return has_not_fuzzy;
	}

	public void run() {

		progress = 0;
		no_structures = 0;
		has_fuzzy = false;
		has_not_fuzzy = true;

		if (peaks == null
				|| ann_opt == null
				|| (fragmenter == null && frag_doc == null && generator == null)
				|| (fragmenter != null && structures == null)) {
			interrupted();
			return;
		}

		// compute possible charges
		if (!ann_opt.DERIVE_FROM_PARENT || mode != FRAGMENTER)
			ion_clouds = IonCloudUtils.getPossibleIonClouds(ann_opt);

		// annotate
		if (mode == FRAGMENTER) {
			// annotate with computed fragments
			annotated_peaks.clear();
			for (Glycan structure : structures) {
				if (structure != null) {
					no_structures++;
					if (structure.isFuzzy(true)) {
						has_fuzzy = true;
						progress += peaks.size();
					} else {
						has_not_fuzzy = true;

						// compute fragments if requested
						FragmentCollection frag_coll = fragmenter
								.computeAllFragments(structure);

						// derive annotation options from parent if requested
						AnnotationOptions der_ann_opt = ann_opt;
						if (ann_opt.DERIVE_FROM_PARENT) {
							der_ann_opt = ann_opt.derive(structure);
							ion_clouds = IonCloudUtils
									.getPossibleIonClouds(der_ann_opt);
						}

						// annotate fragments
						match_coll = match(peaks, frag_coll, ion_clouds,
								der_ann_opt);
						annotated_peaks.addPeakAnnotations(structure,
								match_coll, false);
					}
				} else
					progress += peaks.size();
			}
		} else if (mode == COLLECTION) {
			// annotate with pre-computed fragments
			annotated_peaks.clear();
			for (int i = 0; i < frag_doc.getNoStructures(); i++) {
				match_coll = match(peaks, frag_doc.getFragments(i), ion_clouds,
						ann_opt);
				annotated_peaks.addPeakAnnotations(frag_doc.getStructure(i),
						match_coll, false);
			}
		} else if (mode == GENERATOR) {
			// annotate with generated structures
			annotated_peaks.clear();

			int ind = 0;
			for (Glycan s : structures) {

				// generate and match
				match_coll = new PeakAnnotationCollection();
				generator.generate(ind, this);

				// check if all peaks have matched
				for (Peak p : peaks.getPeaks()) {
					if (!match_coll.isAnnotated(p))
						match_coll.addPeakAnnotation(p);
				}
				progress += peaks.size();

				// save annotations
				annotated_peaks.addPeakAnnotations(s, match_coll, false);

				ind++;
			}
		}

		progress = getTarget();
	}

	static public Collection<FragmentEntry> computeChargesAndExchanges(
			Glycan structure, Collection<FragmentEntry> fragments,
			AnnotationOptions ann_opt) {
		// compute possible charges
		if (ann_opt.DERIVE_FROM_PARENT)
			ann_opt = ann_opt.derive(structure);
		Vector<IonCloud> ion_clouds = IonCloudUtils
				.getPossibleIonClouds(ann_opt);

		// compute charges and exchanges
		return computeChargesAndExchanges(fragments, ion_clouds, ann_opt);
	}

	static public Collection<FragmentEntry> computeChargesAndExchanges(
			Collection<FragmentEntry> fragments,
			Collection<IonCloud> ion_clouds, AnnotationOptions ann_opt) {

		Vector<FragmentEntry> ret = new Vector<FragmentEntry>();
		for (FragmentEntry fe : fragments) {
			if (ann_opt.COMPUTE_EXCHANGES) {
				Vector<IonCloud> neutral_exchanges = IonCloudUtils
						.getPossibleNeutralExchanges(
								fe.fragment.countCharges(), ann_opt);
				for (IonCloud nex : neutral_exchanges) {
					for (IonCloud cloud : ion_clouds) {
						if (cloud.and(nex).isRealistic())
							ret.add(fe.and(cloud, nex));
					}
				}
			} else {
				for (IonCloud cloud : ion_clouds)
					ret.add(fe.and(cloud));
			}
		}

		return ret;
	}

	public boolean generatorCallback(FragmentEntry fe) {

		if (ann_opt.COMPUTE_EXCHANGES) {
			Vector<IonCloud> neutral_exchanges = IonCloudUtils
					.getPossibleNeutralExchanges(fe.fragment.countCharges(),
							ann_opt);
			for (IonCloud nex : neutral_exchanges) {
				for (IonCloud cloud : ion_clouds) {
					if (cloud.and(nex).isRealistic()) {
						for (Peak p : peaks.getPeaks()) {
							if (match(p, fe, cloud, nex, ann_opt))
								match_coll.addPeakAnnotation(p, fe.and(cloud,
										nex));
						}
					}
					if (interrupted())
						return false;
				}
			}
		} else {
			for (IonCloud cloud : ion_clouds) {
				for (Peak p : peaks.getPeaks()) {
					if (match(p, fe, cloud, ann_opt))
						match_coll.addPeakAnnotation(p, fe.and(cloud));
				}
				if (interrupted())
					return false;
			}
		}

		return true;
	}

	public PeakAnnotationCollection match(PeakList peaklist,
			FragmentCollection fc, Vector<IonCloud> ion_clouds,
			AnnotationOptions ann_opt) {

		// match
		PeakAnnotationCollection matched = new PeakAnnotationCollection();
		for (int i = 0; i < peaklist.size() && !interrupted(); i++, progress++) {
			Peak p = peaklist.getPeak(i);

			boolean has_matched = false;
			for (FragmentEntry fe : fc.getFragments()) {
				//System.err.println("Peak: "+p.getCharge()+"|fragment: "+fe.getCharges().getNoCharges());
				if(p.getCharge()!=Integer.MIN_VALUE &&
						p.getCharge()!=fe.getCharges().getNoCharges()){
					
					continue;
				}
				
				if (ann_opt.COMPUTE_EXCHANGES) {
					Vector<IonCloud> neutral_exchanges = IonCloudUtils
							.getPossibleNeutralExchanges(fe.fragment
									.countCharges(), ann_opt);
					for (IonCloud nex : neutral_exchanges) {
						for (IonCloud cloud : ion_clouds) {
							if (cloud.and(nex).isRealistic()) {
								if (match(p, fe, cloud, nex, ann_opt)) {
									matched.addPeakAnnotation(p, fe.and(cloud,
											nex));
									has_matched = true;
								}
							}
						}
					}
				} else {
					for (IonCloud cloud : ion_clouds) {
						if (match(p, fe, cloud, ann_opt)) {
							matched.addPeakAnnotation(p, fe.and(cloud));
							has_matched = true;
						}
					}
				}
			}
			if (!has_matched)
				matched.addPeakAnnotation(p);
		}

		return matched;
	}

	static public boolean match(double fmz, double mz_ratio,
			AnnotationOptions ann_opt) {
		if (ann_opt.MASS_ACCURACY_UNIT.equals(ann_opt.MASS_ACCURACY_PPM))
			return (Math.abs(1. - fmz / mz_ratio) < (0.000001 * ann_opt.MASS_ACCURACY));
		return (Math.abs(mz_ratio - fmz) < ann_opt.MASS_ACCURACY);
	}

	static public boolean match(Peak p, FragmentEntry fe,
			AnnotationOptions ann_opt) {
		
		double fmz = fe.mz_ratio;
		double mz_ratio = p.getMZ();
		return match(fmz, mz_ratio, ann_opt);
	}

	static public boolean match(Peak p, FragmentEntry fe, IonCloud ions,
			AnnotationOptions ann_opt) {
		double fmz = ions.computeMZ(fe.mass);
		double mz_ratio = p.getMZ();
		return match(fmz, mz_ratio, ann_opt);
	}

	static public boolean match(Peak p, FragmentEntry fe, IonCloud ions,
			IonCloud neutral_exchange, AnnotationOptions ann_opt) {
		double fmz = ions.computeMZ(neutral_exchange.getIonsMass() + fe.mass);
		double mz_ratio = p.getMZ();
		return match(fmz, mz_ratio, ann_opt);
	}
}