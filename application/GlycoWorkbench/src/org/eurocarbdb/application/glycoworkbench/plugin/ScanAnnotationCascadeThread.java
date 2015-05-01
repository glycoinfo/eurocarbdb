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
 *   Last commit: $Rev$ by $Author$ on $Date::             $  
 */
/**
 @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
 */

package org.eurocarbdb.application.glycoworkbench.plugin;

import org.eurocarbdb.application.glycanbuilder.*;

import org.eurocarbdb.application.glycoworkbench.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ScanAnnotationCascadeThread extends Thread {
	protected AnnotationOptions ann_opt = null;
	protected FragmentOptions opts = null;
	protected HashMap<Scan, AnnotatedPeakList> scanToAnnotatedPeaks = null;
	protected Vector<Scan> parentScans;
	protected int progress;

	public ScanAnnotationCascadeThread(Vector<Scan> parentScans,
			FragmentOptions opts, AnnotationOptions _ann_opt) {
		this.opts = opts;
		this.ann_opt = _ann_opt;
		this.parentScans = parentScans;
		scanToAnnotatedPeaks = new HashMap<Scan, AnnotatedPeakList>();
	}

	public HashMap<Scan, AnnotatedPeakList> getScanToAnnotatedPeaks() {
		return scanToAnnotatedPeaks;
	}

	public void run() {
		annotateScans(parentScans);
	}

	private void annotateScans(Vector<Scan> scans) {
		for (Scan scan : scans) {
			annotateScan(scan);
			annotateScans(scan.getChildren());
			
			if(scan.getParent()==null)
				progress++ ;
		}
	}

	private void annotateScan(Scan scan) {
		
		if (scan.getParent() != null) {
			final Scan final_scan=scan;
			
			//Do this on the event thread
			try {
				SwingUtilities.invokeAndWait(new Runnable(){
					public void run(){
						final_scan.sync(ann_opt);
					}
				});
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		Fragmenter frag = new Fragmenter(this.opts);
		AnnotationThread annotationThread = new AnnotationThread(scan
				.getPeakList(), scan.getStructures().getStructures(), frag,
				ann_opt);
		annotationThread.run();
		this.scanToAnnotatedPeaks.put(scan, annotationThread
				.getAnnotatedPeaks());
		scan.getAnnotatedPeakList().copy(annotationThread.getAnnotatedPeaks(),false);
	}

	public int getTarget() {
		return this.parentScans.size();
	}

	public int getProgress() {
		return this.progress;
	}
}