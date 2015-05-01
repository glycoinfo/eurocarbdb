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
import org.eurocarbdb.application.glycoworkbench.plugin.peakpicker.*;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class SpectraPlugin implements Plugin, ActionListener {

    PluginManager theManager = null;
    GlycoWorkbench theApplication = null;
    GlycanWorkspace theWorkspace = null;
  
    PeakPickerCWT thePeakPicker = null;
    SpectraPanel theSpectraPanel = null;    
    
    public SpectraPlugin(GlycoWorkbench application) {
    thePeakPicker = new PeakPickerCWT();
    theSpectraPanel = new SpectraPanel();
    this.theApplication=application;
    }

    public void init() {
    if( theWorkspace!=null ) 
        thePeakPicker.retrieve(theWorkspace.getConfiguration());    
    }

    public void exit() {
    if( theWorkspace!=null ) 
        thePeakPicker.store(theWorkspace.getConfiguration()); 
    }

    public String getName() {
    return "Spectra";
    }

    public int getMnemonic() {
    return KeyEvent.VK_S;
    }
    
    public ResizableIcon getResizableIcon(){
    	return FileUtils.getThemeManager().getResizableIcon("spectradoc", ICON_SIZE.L3).getResizableIcon();
    }

    public ImageIcon getIcon() {
    return ThemeManager.getEmptyIcon(ICON_SIZE.TINY);
    }

    public int getViewPosition(String view) {
    return PluginManager.VIEW_BOTTOM;
    }

    public java.awt.Component getLeftComponent() {
    return null;
    }

    public java.awt.Component getRightComponent() {
    return null;
    }

    public java.awt.Component getBottomComponent() {
    return theSpectraPanel;
    }   
    
    public Collection<String> getViews() {
    return Collections.singleton("Spectra");
    }

    public Collection<GlycanAction> getActions() {
    Vector<GlycanAction> actions = new Vector<GlycanAction>();
    
    actions.add(new GlycanAction("addPeaks",this.theApplication.getThemeManager().getResizableIcon("addpeaks", ICON_SIZE.L3),"Add selected peaks to the peak list",KeyEvent.VK_A, "",this));
    actions.add(new GlycanAction("annotatePeaks",this.theApplication.getThemeManager().getResizableIcon("annotatepeaks", ICON_SIZE.L3),"Find possible annotations for selected peaks",KeyEvent.VK_N, "",this));
    actions.add(new GlycanAction("centroid",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Compute peak centroids",KeyEvent.VK_C, "",this));

    return actions;
    }

    public Collection<GlycanAction> getToolbarActions() {
    return new Vector<GlycanAction>();
    }

    public Collection<GlycanAction> getObjectActions(Object prototype, ActionListener al) {
    Vector<GlycanAction> actions = new Vector<GlycanAction>();
    return actions;
    }

    public void setManager(PluginManager manager) {
    theManager = manager;
    }

    public void setApplication(GlycoWorkbench application) {
    theApplication = application;
    theSpectraPanel.setApplication(application);
    }
  
    public void setWorkspace(GlycanWorkspace workspace) {
    theWorkspace = workspace;
    theSpectraPanel.setWorkspace(workspace);
    }


    public  PluginManager getManager() {
    return theManager;
    }

    public  GlycoWorkbench getApplication() {
    return theApplication;
    }

    public  GlycanWorkspace getWorkspace() {
    return theWorkspace;
    }
  
    public SpectraPanel getSpectraPanel() {
    return theSpectraPanel;
    }
   
    public void show(String view) throws Exception {
    if( !view.equals("Spectra") )
        throw new Exception("Invalid view: " + view);        
    }

    public boolean runAction(String action) throws Exception {
    if( action.equals("addPeaks") ) 
        return theSpectraPanel.onAddPeaks();        
    if( action.equals("annotatePeaks") )
        return theSpectraPanel.onAnnotatePeaks(null);
    if( action.equals("centroid") )
        return computeCentroids(theSpectraPanel.getCurrentInd());

    throw new Exception("Invalid action: " + action);
    }

    public boolean runAction(String action, Object param) throws Exception {
    if( action.equals("addIsotopeCurves") ) {        
        theSpectraPanel.addIsotopeCurves((TreeMap<Peak,Collection<Annotation>>)param);
        return true;        
    }

    return runAction(action);
    }

    public void actionPerformed(ActionEvent e) {
    try {
        runAction(GlycanAction.getAction(e));
    }
    catch(Exception ex) {
        LogUtils.report(ex);
    }
    }

    public void updateViews() {
    theSpectraPanel.updateView();
    }

    public void updateMasses() {
    }

    public boolean computeCentroids(int ind) {

    // get spectra
    if( theWorkspace.getSpectra()==null || theWorkspace.getSpectra().size()==0 ) {
        JOptionPane.showMessageDialog(theApplication, "Empty spectrum", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    // init variables
    ScanData sd = theWorkspace.getSpectra().getScanDataAt(ind);
    PeakData pd = theWorkspace.getSpectra().getPeakDataAt(ind);

    // show options dialogs
    PeakPickerOptionsDialog dlg = new PeakPickerOptionsDialog(theApplication,thePeakPicker);
    dlg.setVisible(true);
    if( dlg.getReturnStatus().equals("Cancel") )
        return false;
    double start_mz = dlg.getStartMZ();
    double end_mz = dlg.getEndMZ();

    // ask to overwrite peaklist    
    if( theWorkspace.getPeakList().size()>0 &&
        JOptionPane.showConfirmDialog(theApplication, "The current peaklist will be overwritten. Continue?", "Warning", JOptionPane.YES_NO_OPTION)==JOptionPane.NO_OPTION )
        return false;    

    theApplication.haltInteractions();    
    try {
        int ms_level = (theWorkspace.getCurrentScan().isMsMs()) ?2 :1;
        Vector<Peak> picked_peaks;
        if( end_mz==0. ) 
        picked_peaks = thePeakPicker.pick(pd.getData(),ms_level);
        else
        picked_peaks = thePeakPicker.pick(pd.getData(start_mz,end_mz),ms_level);

        theApplication.restoreInteractions();    
        
        if( picked_peaks.size()>0 ) {
        theWorkspace.getPeakList().setData(picked_peaks);        
        theManager.show("PeakList","Chart");
        return true;
        }

        JOptionPane.showMessageDialog(theApplication, "No peaks found", "Warning", JOptionPane.WARNING_MESSAGE);
        return false;
    }
    catch(Exception e) {
        theApplication.restoreInteractions();    
        LogUtils.report(e);       
        return false;
    }
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
