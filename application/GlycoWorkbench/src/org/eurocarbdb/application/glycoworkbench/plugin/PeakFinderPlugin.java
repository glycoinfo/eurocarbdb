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

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

import java.util.*;

import javax.swing.*;

import java.awt.event.*;

public class PeakFinderPlugin implements Plugin, ActionListener {

    protected PluginManager theManager = null;
    protected GlycoWorkbench theApplication = null;
    protected GlycanWorkspace theWorkspace = null;   
    
    protected PeakFinderOptions theOptions = null;

    protected boolean first_time_run = true;

    protected PeakFinderThread theThread = null;
    protected ProgressDialog progressDialog = null;
    protected javax.swing.Timer activityMonitor = null;

    public PeakFinderPlugin(GlycoWorkbench bench) {
    	this.theApplication=bench;
    try {
        theOptions = new PeakFinderOptions();
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }       

    public void init() {
      if( theWorkspace!=null ) 
        theOptions.retrieve(theWorkspace.getConfiguration());
    }

    public void exit() {
    if( theWorkspace!=null ) 
        theOptions.store(theWorkspace.getConfiguration());
    }

    public String getName() {
    return "Glyco-Peakfinder";
    }

    public int getMnemonic() {
    return KeyEvent.VK_K;
    }

    public ImageIcon getIcon() {
    return ThemeManager.getEmptyIcon(ICON_SIZE.TINY);
    }

    public int getViewPosition(String view) {
    return PluginManager.VIEW_RIGHT;
    }

    public java.awt.Component getLeftComponent() {
    return null;
    }

    public java.awt.Component getRightComponent() {
    return null;
    }

    public java.awt.Component getBottomComponent() {
    return null;
    } 
    
    public Collection<String> getViews() {
    Vector<String> views = new Vector<String>();
    return views;
    }

    public Collection<GlycanAction> getActions() {
    Vector<GlycanAction> actions = new Vector<GlycanAction>();

    actions.add(new GlycanAction("options",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Set plugin options",KeyEvent.VK_O,"",this));
    actions.add(null);

    actions.add(new GlycanAction("findCompositions",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Find structure compositions with a given m/z value",KeyEvent.VK_F,"",this));
    actions.add(new GlycanAction("matchCompositions",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Find structure compositions matching the peak list",KeyEvent.VK_P,"",this));
    
    actions.add(null);
    
    actions.add(new GlycanAction("findFragmentCompositions",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Find fragments compositions with a given m/z value",KeyEvent.VK_I,"",this));
    actions.add(new GlycanAction("matchFragmentCompositions",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Find fragments compositions matching the peak list",KeyEvent.VK_L,"",this));
    
    return actions;
    }

    public Collection<GlycanAction> getToolbarActions() {
    Vector<GlycanAction> actions = new Vector<GlycanAction>();
    return actions;
    }

    public Collection<GlycanAction> getObjectActions(Object prototype, ActionListener al) {
    Vector<GlycanAction> actions = new Vector<GlycanAction>();
    return actions;
    }

    public void setManager(PluginManager manager) {
    theManager = manager;
    theManager.addMsPeakAction(new GlycanAction("findCompositions",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Find structure compositions matching the peaks",KeyEvent.VK_P,"",this));
    theManager.addMsMsPeakAction(new GlycanAction("findFragmentCompositions",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Find fragments compositions matching the peaks",KeyEvent.VK_P,"",this));

    }

    public void setApplication(GlycoWorkbench application) {
    theApplication = application;    
    }
   
    public void setWorkspace(GlycanWorkspace workspace) {
    theWorkspace = workspace;
    if( theWorkspace!=null ) 
        theOptions.retrieve(theWorkspace.getConfiguration());
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


    public void show(String view) throws Exception {    
    throw new Exception("Invalid view: " + view);
    }



    public boolean runAction(String action) throws Exception{

    if( action.startsWith("find") ) {
        String m_z = JOptionPane.showInputDialog(theApplication, "Insert m/z value",theWorkspace.getRecentMZValue()); 
        if( m_z!=null ) {
        Double mz_value = Double.valueOf(m_z);
        theWorkspace.setRecentMZValue(mz_value);
        return runAction(true,action,new PeakList(mz_value));
        }
        return false;
    }
    else
        return runAction(true,action,theWorkspace.getPeakList());
    }


    public boolean runAction(String action, Object params) throws Exception{
    if( runAction(first_time_run,action,params) ) {
        first_time_run = false;
        return true;
    }
    return false;        
    }

    public boolean runAction(boolean ask, String action, Object params) throws Exception{
    if( !(params instanceof PeakList) )
        throw new Exception("Invalid param object: PeakList needed");
    PeakList peaks = (PeakList)params;
    
    if( action.equals("options") ) 
        return setOptions();

    if( action.equals("findCompositions") ) {
        if( matchCompositions(ask,peaks,false) ) {
        theManager.show("Search","Details");
        return true;        
        }
        return false;
    }    
    if( action.equals("matchCompositions") ) {
        if( matchCompositions(ask,peaks,false) ) {
        theManager.show("Search","Details");
        return true;
        }
        return false;
    }

    if( action.equals("findFragmentCompositions") ) {
        if(  matchCompositions(ask, peaks,true) ) {
        theManager.show("Search","Details");
        return true;        
        }
        return false;
    }    
    if( action.equals("matchFragmentCompositions") ) {
        if( matchCompositions(ask,peaks,true) ) {
        theManager.show("Search","Details");
        return true;
        }
        return false;
    }

    throw new Exception("Invalid action: " + action);
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
    }

    public void updateMasses() {
    }


    //------------
    // ACTIONS
        
    public boolean setOptions() {
    // show options dialog
    PeakFinderOptionsDialog pdlg = new PeakFinderOptionsDialog(theApplication,theOptions);
    pdlg.setVisible(true);     
    if( !pdlg.getReturnStatus().equals("OK") )
        return false;       
    
    // show annotation options dialog
    FragmentOptions frag_opt = theWorkspace.getFragmentOptions();
    AnnotationOptions ann_opt = theWorkspace.getAnnotationOptions();
    AnnotationOptionsDialog adlg = new AnnotationOptionsDialog(theApplication,frag_opt,ann_opt,true,false);
    adlg.setVisible(true);     
    if( !adlg.getReturnStatus().equals("OK") )
        return false;       
    return true;
    }

    public boolean matchCompositions(boolean ask, PeakList peaks, boolean compute_fragments) throws Exception {
    if( ask ) {
        // show options dialog
        PeakFinderOptionsDialog pdlg = new PeakFinderOptionsDialog(theApplication,theOptions);
        pdlg.setVisible(true);     
        if( !pdlg.getReturnStatus().equals("OK") )
        return false;       

    }
     
    
    FragmentOptions frag_opt = theWorkspace.getFragmentOptions();
    AnnotationOptions ann_opt = theWorkspace.getAnnotationOptions();
    if( ask ) {
        // show annotation options dialog
        AnnotationOptionsDialog adlg = new AnnotationOptionsDialog(theApplication,frag_opt,ann_opt,compute_fragments,false);
        adlg.setVisible(true);     
        if( !adlg.getReturnStatus().equals("OK") )
        return false;        
    }

    // halt interactions
    theApplication.haltInteractions();

    // match structures        
    if( compute_fragments ) 
        theThread = new PeakFinderThread(peaks,theOptions,ann_opt,frag_opt);
    else
        theThread = new PeakFinderThread(peaks,theOptions,ann_opt,null);

    return runAnnotation();
    }      

    private boolean runAnnotation() {

    theThread.start();

    // launch progress dialog
    progressDialog = new ProgressDialog(theApplication, "Matching peaks with compositions", null, -1,-1);
    progressDialog.setVisible(true);
    
    // set up the timer action
    activityMonitor = new javax.swing.Timer(200, new ActionListener() {  
        public void actionPerformed (ActionEvent event) {           
            // check if task is completed or canceled
            if( !theThread.isAlive() || theThread.isInterrupted() ||  progressDialog.isCanceled ()) {  
            activityMonitor.stop();
            progressDialog.setVisible(false);
            
            if( progressDialog.isCanceled() ) {
                theThread.interrupt();    
                onAnnotationAborted(theThread);
            }
            else {
                onAnnotationCompleted(theThread);                
            }
            }
        }
        });
    activityMonitor.start();    
    
    // return control
    return true;
    }

    public void onAnnotationCompleted(PeakFinderThread t) {
        
    // show annotations                
    theWorkspace.setSearchGenerator(this);
    theWorkspace.getSearchResults().copy(t.getAnnotatedPeaks());
        
    // restore interactions
    theApplication.restoreInteractions();               
    }
        
    public void onAnnotationAborted(PeakFinderThread t) { 
    // show annotations            
    theWorkspace.setSearchGenerator(this);
    theWorkspace.getSearchResults().copy(t.getAnnotatedPeaks());

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

