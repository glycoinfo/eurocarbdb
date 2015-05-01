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
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.ProgressMonitor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class AnnotationPlugin implements Plugin, ActionListener {

	protected PluginManager theManager = null;
	protected GlycoWorkbench theApplication = null;
	protected GlycanWorkspace theWorkspace = null;

	protected JTabbedPane theAnnotationToolsPane = null;
	protected PeakAnnotationStatsPanel thePeakAnnotationStatsPanel = null;
	protected PeakAnnotationDetailsPanel thePeakAnnotationDetailsPanel = null;
	protected PeakAnnotationSummaryPanel thePeakAnnotationSummaryPanel = null;
	protected PeakAnnotationCalibrationPanel thePeakAnnotationCalibrationPanel = null;
	protected PeakAnnotationReportPanel thePeakAnnotationReportPanel = null;

	protected boolean first_time_run = true;
	protected boolean annotate = false;
	protected boolean showTopResults = true;
	protected AnnotationThread theThread = null;
	protected ProgressMonitor progressDialog = null;
	protected Timer activityMonitor = null;
	protected ScanAnnotationCascadeThread scanThread = null;
	
	
	public AnnotationPlugin(GlycoWorkbench bench) {
		this.theApplication=bench;
		thePeakAnnotationStatsPanel = new PeakAnnotationStatsPanel();
		thePeakAnnotationDetailsPanel = new PeakAnnotationDetailsPanel();
		thePeakAnnotationSummaryPanel = new PeakAnnotationSummaryPanel();
		thePeakAnnotationCalibrationPanel = new PeakAnnotationCalibrationPanel();
		thePeakAnnotationReportPanel = new PeakAnnotationReportPanel();

		theAnnotationToolsPane = new JTabbedPane();
		theAnnotationToolsPane.add("Stats", thePeakAnnotationStatsPanel);
		theAnnotationToolsPane.add("Details", thePeakAnnotationDetailsPanel);
		theAnnotationToolsPane.add("Summary", thePeakAnnotationSummaryPanel);
		theAnnotationToolsPane.add("Calibration",
				thePeakAnnotationCalibrationPanel);
	}

	public PeakAnnotationStatsPanel getPeakAnnotationStatsPanel() {
		return thePeakAnnotationStatsPanel;
	}

	public PeakAnnotationDetailsPanel getPeakAnnotationDetailsPanel() {
		return thePeakAnnotationDetailsPanel;
	}

	public PeakAnnotationSummaryPanel getPeakAnnotationSummaryPanel() {
		return thePeakAnnotationSummaryPanel;
	}

	public PeakAnnotationCalibrationPanel getPeakAnnotationCalibrationPanel() {
		return thePeakAnnotationCalibrationPanel;
	}

	public PeakAnnotationReportPanel getPeakAnnotationReportPanel() {
		return thePeakAnnotationReportPanel;
	}

	public void init() {
	}

	public void exit() {
	}

	public String getName() {
		return "Annotation";
	}

	public int getMnemonic() {
		return KeyEvent.VK_A;
	}
	
	public ResizableIcon getResizableIcon(){
    	return FileUtils.getThemeManager().getResizableIcon("annpeaksdoc", ICON_SIZE.L3).getResizableIcon();
    }

	public ImageIcon getIcon() {
		return ThemeManager.getEmptyIcon(ICON_SIZE.TINY);
	}

	public int getViewPosition(String view) {
		if (view.equals("Report"))
			return PluginManager.VIEW_BOTTOM;
		return PluginManager.VIEW_RIGHT;
	}

	public java.awt.Component getLeftComponent() {
		return null;
	}

	public java.awt.Component getRightComponent() {
		return theAnnotationToolsPane;
	}

	public java.awt.Component getBottomComponent() {
		return thePeakAnnotationReportPanel;
	}

	public Collection<String> getViews() {
		Vector<String> views = new Vector<String>();
		views.add("Stats");
		views.add("Details");
		views.add("Summary");
		views.add("Calibration");
		views.add("Report");
		return views;
	}

	public Collection<GlycanAction> getActions() {
		Vector<GlycanAction> actions = new Vector<GlycanAction>();

		actions.add(new GlycanAction("options", ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),
				"Set plugin options", KeyEvent.VK_O, "", this));
		actions.add(null);
		actions
				.add(new GlycanAction(
						"findFragmentsCurrent",
						ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),
						"Find all fragments of the current structure with a given m/z value",
						KeyEvent.VK_C, "", this));
		actions
				.add(new GlycanAction(
						"findFragmentsSelected",
						this.theApplication.getThemeManager().getResizableIcon("findfragments", ICON_SIZE.L3),
						"Find all fragments of the selected structures with a given m/z value",
						KeyEvent.VK_S, "", this));
		actions
				.add(new GlycanAction(
						"findFragmentsAll",
						ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),
						"Find all fragments of all the structures with a given m/z value",
						KeyEvent.VK_A, "", this));
		actions.add(null);
		actions.add(new GlycanAction("matchFragmentsCurrent", ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),
				"Annotate peaks with fragments from current structure",
				KeyEvent.VK_U, "", this));
		actions.add(new GlycanAction("matchFragmentsSelected", this.theApplication.getThemeManager().getResizableIcon("findfragments", ICON_SIZE.L3),
				"Annotate peaks with fragments from selected structures",
				KeyEvent.VK_E, "", this));
		actions.add(new GlycanAction("matchFragmentsAll",
				ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),
				"Annotate peaks with fragments from all structures",
				KeyEvent.VK_L, "", this));
		actions.add(null);
		actions
				.add(new GlycanAction(
						"placeAntennae",
						ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),
						"Place uncertain antennae in current structure using the peak list",
						KeyEvent.VK_P, "", this));

		actions.add(new GlycanAction("cascadeAnnotation",
				ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3), "Cascade scan annotation",
				KeyEvent.VK_B, "", this));

		return actions;
	}

	public Collection<GlycanAction> getToolbarActions() {
		Vector<GlycanAction> actions = new Vector<GlycanAction>();

		actions
				.add(new GlycanAction(
						"findFragmentsSelected",
						this.theApplication.getThemeManager().getResizableIcon("findfragments", ICON_SIZE.L3),
						"Find all fragments of the selected structures with a given m/z value",
						KeyEvent.VK_S, "", this));
		actions.add(new GlycanAction("matchFragmentsSelected", this.theApplication.getThemeManager().getResizableIcon("findfragments", ICON_SIZE.L3),
				"Annotate peaks with fragments from selected structures",
				KeyEvent.VK_E, "", this));

		return actions;
	}

	public Collection<GlycanAction> getObjectActions(Object prototype,
			ActionListener al) {
		Vector<GlycanAction> actions = new Vector<GlycanAction>();
		return actions;
	}

	public void setManager(PluginManager manager) {
		theManager = manager;
		if (theManager != null)
			theManager
					.addMsMsPeakAction(new GlycanAction(
							"findFragmentsSelected",
							this.theApplication.getThemeManager().getResizableIcon("findfragments", ICON_SIZE.L3),
							"Find all fragments of the selected structures matching the peaks",
							KeyEvent.VK_S, "", this));
	}

	public void setApplication(GlycoWorkbench application) {
		theApplication = application;
		thePeakAnnotationStatsPanel.setApplication(application);
		thePeakAnnotationDetailsPanel.setApplication(application);
		thePeakAnnotationSummaryPanel.setApplication(application);
		thePeakAnnotationCalibrationPanel.setApplication(application);
		thePeakAnnotationReportPanel.setApplication(application);
	}

	public void setWorkspace(GlycanWorkspace workspace) {
		theWorkspace = workspace;
		thePeakAnnotationStatsPanel.setWorkspace(workspace);
		thePeakAnnotationDetailsPanel.setWorkspace(workspace);
		thePeakAnnotationSummaryPanel.setWorkspace(workspace);
		thePeakAnnotationCalibrationPanel.setWorkspace(workspace);
		thePeakAnnotationReportPanel.setWorkspace(workspace);
	}

	public PluginManager getManager() {
		return theManager;
	}

	public GlycoWorkbench getApplication() {
		return theApplication;
	}

	public GlycanWorkspace getWorkspace() {
		return theWorkspace;
	}

	public void show(String view) throws Exception {
		if (view.equals("Stats"))
			theAnnotationToolsPane
					.setSelectedComponent(thePeakAnnotationStatsPanel);
		else if (view.equals("Details"))
			theAnnotationToolsPane
					.setSelectedComponent(thePeakAnnotationDetailsPanel);
		else if (view.equals("Summary"))
			theAnnotationToolsPane
					.setSelectedComponent(thePeakAnnotationSummaryPanel);
		else if (view.equals("Calibration"))
			theAnnotationToolsPane
					.setSelectedComponent(thePeakAnnotationCalibrationPanel);
		else if (view.equals("Report")) {
		} else
			throw new Exception("Invalid view: " + view);
	}

	public boolean runAction(String action) throws Exception {

		if (action.startsWith("find")) {
			String m_z = JOptionPane.showInputDialog(theApplication,
					"Insert m/z value", theWorkspace.getRecentMZValue());
			if (m_z != null) {
				Double mz_value = Double.valueOf(m_z);
				theWorkspace.setRecentMZValue(mz_value);
				return runAction(true, action, new PeakList(mz_value));
			}
			return false;
		} else
			return runAction(true, action, theWorkspace.getPeakList());
	}

	public boolean runAction(String action, Object params) throws Exception {
		if (runAction(first_time_run, action, params)) {
			first_time_run = false;
			return true;
		}
		return false;
	}

	public boolean runAction(boolean ask, String action, Object params)
			throws Exception {

		if (!(params instanceof PeakList))
			throw new Exception("Invalid param object: PeakList needed");
		PeakList peaks = (PeakList) params;

		if (action.equals("options"))
			return setOptions();

		if (action.equals("findFragmentsCurrent")) {
			annotate = false;
			theApplication.getCanvas().enforceSelection();
			if (matchAllFragments(ask, theApplication.getCanvas()
					.getCurrentStructure(), peaks)) {
				theManager.show("Search", "Details");
				return true;
			}
			return false;
		}
		if (action.equals("findFragmentsSelected")) {
			annotate = false;
			theApplication.getCanvas().enforceSelection();
			if (matchAllFragments(ask, theApplication.getCanvas()
					.getSelectedStructures(), peaks)) {
				theManager.show("Search", (theApplication.getCanvas()
						.getSelectedStructures().size() > 1) ? "Summary"
						: "Details");
				return true;
			}
			return false;
		}
		if (action.equals("findFragmentsAll")) {
			annotate = false;
			if (matchAllFragments(ask, theWorkspace.getStructures()
					.getStructures(), peaks)) {
				theManager.show("Search",
						(theWorkspace.getStructures().size() > 1) ? "Summary"
								: "Details");
				return true;
			}
			return false;
		}

		if (action.equals("matchFragmentsCurrent")) {
			annotate = true;
			theApplication.getCanvas().enforceSelection();
			if (matchAllFragments(ask, theApplication.getCanvas()
					.getCurrentStructure(), peaks)) {
				theManager.show("Annotation", "Stats");
				return true;
			}
			return false;
		}
		if (action.equals("matchFragmentsSelected")) {
			annotate = true;
			theApplication.getCanvas().enforceSelection();
			if (matchAllFragments(ask, theApplication.getCanvas()
					.getSelectedStructures(), peaks)) {
				theManager.show("Annotation", "Stats");
				return true;
			}
			return false;
		}
		if (action.equals("matchFragmentsAll")) {
			annotate = true;

			if (matchAllFragments(ask, theWorkspace.getStructures()
					.getStructures(), peaks)) {
				theManager.show("Annotation", "Stats");
				return true;
			}
			return false;
		}

		if (action.equals("placeAntennae")) {
			annotate = true;
			theApplication.getCanvas().enforceSelection();
			if (placeAntennae(ask, theApplication.getCanvas()
					.getCurrentStructure(), peaks)) {
				theManager.show("Annotation", "Stats");
				return true;
			}
			return false;
		}

		if (action.equals("cascadeAnnotation")) {
			annotate = true;
			if (scanAnnotationCascade(ask, this.getWorkspace()
					.getAllParentScans())) {
				theManager.show("Annotation", "Stats");
				this.getWorkspace().fireDocumentChanged();
				return true;
			}
			return false;
		}

		throw new Exception("Invalid action: " + action);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			runAction(GlycanAction.getAction(e));
		} catch (Exception ex) {
			LogUtils.report(ex);
		}
	}

	public void updateViews() {
		thePeakAnnotationStatsPanel.updateView();
		thePeakAnnotationDetailsPanel.updateView();
		thePeakAnnotationSummaryPanel.updateView();
		thePeakAnnotationCalibrationPanel.updateView();
		thePeakAnnotationReportPanel.updateView();
	}

	public void updateMasses() {
	}

	// ------------
	// ACTIONS

	public boolean setOptions() {

		// show annotation options dialog
		FragmentOptions frag_opt = theWorkspace.getFragmentOptions();
		AnnotationOptions ann_opt = theWorkspace.getAnnotationOptions();
		AnnotationOptionsDialog adlg = new AnnotationOptionsDialog(
				theApplication, frag_opt, ann_opt, false, false);
		adlg.setVisible(true);
		if (!adlg.getReturnStatus().equals("OK"))
			return false;

		return true;
	}

	public boolean matchAllFragments(boolean ask_options, Glycan structure,
			PeakList peaks) {
		if (structure != null)
			return matchAllFragments(ask_options, Collections
					.singleton(structure), peaks);
		return false;
	}

	public boolean scanAnnotationCascade(boolean ask, Vector<Scan> parentScans) {
		if(setAnnotationOptions(ask)){
			theApplication.haltInteractions();
			showTopResults = false;
			
			scanThread=new ScanAnnotationCascadeThread(parentScans,theWorkspace.getFragmentOptions(), theWorkspace.getAnnotationOptions());
			scanThread.start();

			progressDialog = new ProgressMonitor(theApplication,"Parent scans completed", null, 0, scanThread.getTarget());

			// set up the timer action
			activityMonitor = new Timer(200, new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					int progress = scanThread.getProgress();
					// show progress
					progressDialog.setProgress(progress);

					// check if task is completed or canceled
					if (progress == scanThread.getTarget()
							|| progressDialog.isCanceled()) {

						System.err.println("Stopping activity monitor");
						activityMonitor.stop();
						progressDialog.close();

						if (progress != scanThread.getTarget()) {
							scanThread.interrupt();
							onAnnotationAborted(scanThread);
						} else {
							onAnnotationCompleted(scanThread);
						}

					}
				}
			});
			activityMonitor.start();
		}
		return true;
	}
	
	public boolean setAnnotationOptions(boolean ask_options){
		if (ask_options) {
			AnnotationOptionsDialog dlg = new AnnotationOptionsDialog(
					theApplication,theWorkspace.getFragmentOptions(), theWorkspace.getAnnotationOptions(), true, true);
			dlg.setVisible(true);
			if (!dlg.getReturnStatus().equals("OK")){
				return false;
			}else{
				return true;
			}
		}
		return true;
	}

	public boolean matchAllFragments(boolean ask_options,
			Collection<Glycan> structures, PeakList peaks) {

		if (structures == null || structures.size() == 0)
			return false;

		// retrieve peak list
		if (peaks.size() == 0) {
			JOptionPane.showMessageDialog(theApplication,
					"You must load a peak list first", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// show fragments dialog
		FragmentOptions frag_opt = theWorkspace.getFragmentOptions();
		AnnotationOptions ann_opt = theWorkspace.getAnnotationOptions();
		if (ask_options) {
			AnnotationOptionsDialog dlg = new AnnotationOptionsDialog(
					theApplication, frag_opt, ann_opt, true, true);
			dlg.setVisible(true);
			if (!dlg.getReturnStatus().equals("OK"))
				return false;
		}

		// create fragmenter
		Fragmenter frag = new Fragmenter(frag_opt);

		// check for linkages
		if ((frag.getComputeAFragments() || frag.getComputeXFragments())
				&& ask_options) {
			for (Glycan s : structures) {
				if (!s.checkLinkages()) {
					if (JOptionPane
							.showConfirmDialog(
									theApplication,
									"Cross ring fragments will not be computed for residues with incomplete linkage or anomeric information. Continue?",
									"Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
						return false;
					else
						break;
				}
			}
		}

		// halt interactions
		theApplication.haltInteractions();

		// compute fragments and match them with peaks
		showTopResults = false;
		return runAnnotation(peaks, structures, frag, ann_opt);
	}

	public boolean placeAntennae(boolean ask_options, Glycan current,
			PeakList peaks) {

		if (current == null)
			return false;

		if (current.getNoAntennae() > 2) {
			JOptionPane
					.showMessageDialog(
							theApplication,
							"This features is still experimental and the maximum number of antennae that can be placed is set to two.",
							"Warning", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		// retrieve peak list
		if (peaks.size() == 0) {
			JOptionPane.showMessageDialog(theApplication,
					"You must load a peak list first", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// show fragments dialog
		FragmentOptions frag_opt = theWorkspace.getFragmentOptions();
		AnnotationOptions ann_opt = theWorkspace.getAnnotationOptions();
		if (ask_options) {
			AnnotationOptionsDialog dlg = new AnnotationOptionsDialog(
					theApplication, frag_opt, ann_opt, true, true);
			dlg.setVisible(true);
			if (!dlg.getReturnStatus().equals("OK"))
				return false;
		}

		// create fragmenter
		Fragmenter frag = new Fragmenter(frag_opt);

		// check for linkages
		if ((frag.getComputeAFragments() || frag.getComputeXFragments())
				&& ask_options) {
			if (!current.checkLinkages()) {
				if (JOptionPane
						.showConfirmDialog(
								theApplication,
								"Cross ring fragments will not be computed for residues with incomplete linkage or anomeric information. Continue?",
								"Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
					return false;
			}
		}

		// halt interactions
		theApplication.haltInteractions();

		// create possible structures
		Vector<Glycan> structures = new Vector<Glycan>();
		current.placeAntennae(structures);

		// compute fragments and match them with peaks
		showTopResults = true;
		return runAnnotation(peaks, structures, frag, ann_opt);
	}

	protected boolean runAnnotation(PeakList _peaks,
			Collection<Glycan> _structures, Fragmenter _fragmenter,
			AnnotationOptions _ann_opt) {

		// start activity
		theThread = new AnnotationThread(_peaks, _structures, _fragmenter,
				_ann_opt);
		theThread.start();

		// launch progress dialog
		progressDialog = new ProgressMonitor(theApplication,
				"Matching peaks with fragments", null, 0, theThread.getTarget());

		// set up the timer action
		activityMonitor = new Timer(200, new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int progress = theThread.getProgress();
				// show progress
				progressDialog.setProgress(progress);

				// check if task is completed or canceled
				if (progress == theThread.getTarget()
						|| progressDialog.isCanceled()) {

					System.err.println("Stopping activity monitor");
					activityMonitor.stop();
					progressDialog.close();

					if (progress != theThread.getTarget()) {
						theThread.interrupt();
						onAnnotationAborted(theThread);
					} else {
						onAnnotationCompleted(theThread);
					}

				}
			}
		});
		System.err.println("Starting activity monitor");
		activityMonitor.start();

		// return control
		return true;
	}

	protected void onAnnotationCompleted(AnnotationThread t) {
		// show annotations

		AnnotatedPeakList dest = null;
		if (annotate)
			dest = theWorkspace.getAnnotatedPeakList();
		else {
			dest = theWorkspace.getSearchResults();
			theWorkspace.setSearchGenerator(this);
		}

		if (showTopResults)
			dest.copy(t.getAnnotatedPeaks().getFirst(20));
		else
			dest.copy(t.getAnnotatedPeaks());

		// restore interactions
		theApplication.restoreInteractions();

		// show status
		if (t.getNonEmptyStructures() > 0) {
			if (t.hasFuzzyStructures()) {
				if (t.getNonEmptyStructures() == 1)
					JOptionPane
							.showMessageDialog(
									theApplication,
									"Cannot compute fragments for structures with uncertain terminals",
									"Error", JOptionPane.ERROR_MESSAGE);
				else if (t.hasNonFuzzyStructures())
					JOptionPane
							.showMessageDialog(
									theApplication,
									"Cannot compute fragments for some structures with uncertain terminals",
									"Error", JOptionPane.ERROR_MESSAGE);
				else
					JOptionPane
							.showMessageDialog(
									theApplication,
									"Cannot compute fragments, all structures have uncertain terminals",
									"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	protected void onAnnotationAborted(AnnotationThread t) {
		// restore interactions
		theApplication.restoreInteractions();
	}
	
	protected void onAnnotationAborted(ScanAnnotationCascadeThread scanThread) {
		// restore interactions
		theApplication.restoreInteractions();
	}
	
	protected void onAnnotationCompleted(ScanAnnotationCascadeThread t) {
		HashMap<Scan,AnnotatedPeakList> scanToAnnotatedPeakList=t.getScanToAnnotatedPeaks();
		for(Scan scan:scanToAnnotatedPeakList.keySet()){
			theWorkspace.setCurrentScan(scan);
			AnnotatedPeakList dest = null;
			if (annotate)
				dest = theWorkspace.getAnnotatedPeakList();
			else {
				dest = theWorkspace.getSearchResults();
				theWorkspace.setSearchGenerator(this);
			}

			if (showTopResults)
				dest.copy(scanToAnnotatedPeakList.get(scan).getFirst(20));
			else
				dest.copy(scanToAnnotatedPeakList.get(scan));
		}
		
		// restore interactions
		theApplication.restoreInteractions();
	}


	@Override
	public void completeSetup() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<JRibbonBand> getBandsForToolBar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RibbonTask getRibbonTask() {
		// TODO Auto-generated method stub
		return null;
	}
}
