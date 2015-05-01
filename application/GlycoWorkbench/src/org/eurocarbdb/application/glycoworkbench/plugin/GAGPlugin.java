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
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

import java.util.Collection;
import java.util.Collections;
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

public class GAGPlugin implements Plugin, ActionListener {

    protected GAGDictionary theDictionary = null;
    protected GAGOptions theOptions = null;

    protected PluginManager theManager = null;
    protected GlycoWorkbench theApplication = null;
    protected GlycanWorkspace theWorkspace = null;   
    
    protected boolean first_time_run = true;
    protected boolean annotate = false;
    protected boolean showTopResults = true;
    protected AnnotationThread theThread = null;
    protected ProgressMonitor progressDialog = null;
    protected Timer activityMonitor = null;
    
    public GAGPlugin(GlycoWorkbench bench) {
    	this.theApplication=bench;
    theDictionary = new GAGDictionary("/conf/gag_backbones");
    theOptions = new GAGOptions();
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
    return "GAGs";
    }

    public int getMnemonic() {
    return KeyEvent.VK_G;
    }

    public ImageIcon getIcon() {
    return ThemeManager.getEmptyIcon(ICON_SIZE.TINY);
    }

    public int getViewPosition(String view) {
    return PluginManager.VIEW_NONE;
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

    actions.add(new GlycanAction("computeStructures",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Compute all the possible structures for a given family",KeyEvent.VK_M,"",this));
    actions.add(new GlycanAction("findStructures",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Find all the structures of a given family with a certain m/z value",KeyEvent.VK_F,"",this));
    actions.add(new GlycanAction("matchStructures",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Annotate peaks with structures from a given family",KeyEvent.VK_N,"",this));

    actions.add(null);

    actions.add(new GlycanAction("findSulfationsCurrent",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Find the sulfation patterns of the current structures with a given m/z value",KeyEvent.VK_C,"",this));
    actions.add(new GlycanAction("findSulfationsSelected",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Find the sulfation patterns of the selected structures with a given m/z value",KeyEvent.VK_S,"",this));
    actions.add(new GlycanAction("findSulfationsAll",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Find the sulfation patterns of all structures with a given m/z value",KeyEvent.VK_A,"",this));

    actions.add(null);

    actions.add(new GlycanAction("matchSulfationsCurrent",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Annotate peaks with sulfation patterns of the current structures",KeyEvent.VK_U,"",this));
    actions.add(new GlycanAction("matchSulfationsSelected",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Annotate peaks with sulfation patterns of the selected structures",KeyEvent.VK_E,"",this));
    actions.add(new GlycanAction("matchSulfationsAll",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Annotate peaks with sulfation patterns of all structures",KeyEvent.VK_L,"",this));    
    
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
    if( theManager!=null ) 
        theManager.addMsPeakAction(new GlycanAction("findStructures",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Find all GAG structures matching the peaks",KeyEvent.VK_F,"",this));
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
        String m_z = JOptionPane.showInputDialog(theApplication, "Insert m/z value"); 
        if( m_z!=null ) 
        return runAction(true,action,new PeakList(Double.valueOf(m_z)));
        return false;
    }
    return runAction(true,action,theWorkspace.getPeakList());
    }

    public boolean runAction(String action, Object params) throws Exception{

    if( !(params instanceof PeakList) )
        throw new Exception("Invalid param object: PeakList needed");
    if( runAction(first_time_run,action,(PeakList)params) ) {
        first_time_run = false;
        return true;
    }
    return false;
    }
    

    public boolean runAction(boolean ask, String action, PeakList peaks) throws Exception{
    
    if( action.equals("options") ) 
        return setOptions();

    if( action.equals("computeStructures") ) {
        if( computeAllStructures(ask) ) {
        theManager.show("Fragments","Details");
        return true;
        }
        return false;
    }

    if( action.equals("findStructures") ) {
        annotate = false;
        if( matchStructures(ask,peaks) ) {
        theManager.show("Search",(theWorkspace.getSearchResults().getNoStructures()>1) ?"Summary" :"Details");                        
        return true;        
        }
        return false;
    }
    
    if( action.equals("matchStructures") ) {
        annotate = true;
        if( matchStructures(ask,peaks) ) {
        theManager.show("Annotation",(theWorkspace.getAnnotatedPeakList().getNoStructures()>1) ?"Summary" :"Details");                        
        return true;
        }
        return false;
    }

    if( action.equals("findSulfationsCurrent") ) {
        annotate = false;
        theApplication.getCanvas().enforceSelection();    
        if( matchAllSulfations(ask,theApplication.getCanvas().getCurrentStructure(),peaks) ) {
        theManager.show("Search",(theWorkspace.getSearchResults().getNoStructures()>1) ?"Summary" :"Details");                        
        return true;
        }        
        return false;
    }
    if( action.equals("findSulfationsSelected") ) {
        annotate = false;
        theApplication.getCanvas().enforceSelection();    
        if( matchAllSulfations(ask,theApplication.getCanvas().getSelectedStructures(),peaks) ) {
        theManager.show("Search",(theWorkspace.getSearchResults().getNoStructures()>1) ?"Summary" :"Details");                        
        return true;        
        }
        return false;
    }
    if( action.equals("findSulfationsAll") ) {   
        annotate = false;
        if( matchAllSulfations(ask,theWorkspace.getStructures().getStructures(),peaks) ) {
        theManager.show("Search",(theWorkspace.getSearchResults().getNoStructures()>1) ?"Summary" :"Details");                        
        return true;        
        }
        return false;
    }

    if( action.equals("matchSulfationsCurrent") ) {
        annotate = true;
        theApplication.getCanvas().enforceSelection();    
        if( matchAllSulfations(ask,theApplication.getCanvas().getCurrentStructure(),peaks) ) {
        theManager.show("Annotation","Stats");
        return true;
        }
        return false;
    }
    if( action.equals("matchSulfationsSelected") ) {
        annotate = true;
        theApplication.getCanvas().enforceSelection();    
        if( matchAllSulfations(ask,theApplication.getCanvas().getSelectedStructures(),peaks) ) {
        theManager.show("Annotation","Stats");
        return true;
        }
        return false;
    }
    if( action.equals("matchSulfationsAll") ) {
        annotate = true;
        if( matchAllSulfations(ask,theWorkspace.getStructures().getStructures(),peaks) ) {
        theManager.show("Annotation","Stats");
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
    GAGOptionsDialog dlg = new GAGOptionsDialog(theApplication,theDictionary,theOptions);
    dlg.setVisible(true);     
    if( !dlg.getReturnStatus().equals("OK") )
        return false;
    return true;
    }
    
    public boolean computeAllStructures(boolean ask_options) {
    
    if( ask_options ) {
        // show options dialog
        GAGOptionsDialog dlg = new GAGOptionsDialog(theApplication,theDictionary,theOptions);
        dlg.setVisible(true);     
        if( !dlg.getReturnStatus().equals("OK") )
        return false;
    }

    // halt interactions
    theApplication.haltInteractions();

    // compute structures            
    FragmentDocument results = theDictionary.generateStructures(theOptions);
    theWorkspace.getFragments().copy(results);
  
    // restore interactions
    theApplication.restoreInteractions();

    return true;
    }

    public boolean matchStructures(boolean ask_options, PeakList peaks) {
    
    FragmentOptions frag_opt = theWorkspace.getFragmentOptions();
    AnnotationOptions ann_opt = theWorkspace.getAnnotationOptions();

    if( ask_options ) {
        // show gag options dialog
        GAGOptionsDialog gdlg = new GAGOptionsDialog(theApplication,theDictionary,theOptions);
        gdlg.setVisible(true);     
        if( !gdlg.getReturnStatus().equals("OK") )
        return false;    

        // show annotation options dialog
        AnnotationOptionsDialog adlg = new AnnotationOptionsDialog(theApplication,frag_opt,ann_opt,false,false);
        adlg.setVisible(true);     
        if( !adlg.getReturnStatus().equals("OK") )
        return false;    
    }

    // halt interactions
    theApplication.haltInteractions();

    // match structures    
    theDictionary.setOptions(theOptions);
    theThread = new AnnotationThread(peaks,theDictionary,ann_opt);
    return runAnnotation();
    }


    public boolean matchAllSulfations(boolean ask_options, Glycan structure, PeakList peaks) {
    if( structure!=null )
        return matchAllSulfations(ask_options,Collections.singleton(structure),peaks);
    return false;
    }

    public boolean matchAllSulfations(boolean ask_options, Collection<Glycan> structures, PeakList peaks) {

    // show annotation options dialog
    FragmentOptions frag_opt = theWorkspace.getFragmentOptions();
    AnnotationOptions ann_opt = theWorkspace.getAnnotationOptions();
    if( ask_options ) {
        AnnotationOptionsDialog adlg = new AnnotationOptionsDialog(theApplication,frag_opt,ann_opt,false,false);
        adlg.setVisible(true);     
        if( !adlg.getReturnStatus().equals("OK") )
        return false;        
    }

    // halt interactions
    theApplication.haltInteractions();

    // compute structures
    FragmentDocument fd = Fragmenter.generateLabilesConfigurations(structures);

    // match structures    
    theThread = new AnnotationThread(peaks,fd,ann_opt);
    return runAnnotation();

    }    
          
    private boolean runAnnotation() {

    theThread.start();

    // launch progress dialog
    progressDialog = new ProgressMonitor(theApplication, "Matching peaks with fragments", null, 0, theThread.getTarget());
    
    // set up the timer action
    activityMonitor = new Timer(200, new ActionListener() {  
        public void actionPerformed (ActionEvent event) {  
            int progress = theThread.getProgress();
            
            // show progress
            progressDialog.setProgress(progress);

            // check if task is completed or canceled
            if( progress==theThread.getTarget() || theThread.isInterrupted() ||  progressDialog.isCanceled ()) {  
            activityMonitor.stop();
            progressDialog.close();
            
            if( progress!=theThread.getTarget() ) {
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

    public void onAnnotationCompleted(AnnotationThread t) {
    // show annotations    
    AnnotatedPeakList dest = null;
    if( annotate )
        dest = theWorkspace.getAnnotatedPeakList();
    else
        dest = theWorkspace.getSearchResults();
    
    if( showTopResults )
        dest.copy(t.getAnnotatedPeaks().getFirst(20));
    else
        dest.copy(t.getAnnotatedPeaks());
        
    // restore interactions
    theApplication.restoreInteractions();           
            
    // show status
    if( t.getNonEmptyStructures()>0 ) {
        if( t.hasFuzzyStructures() ) {
        if( t.getNonEmptyStructures()==1 )
            JOptionPane.showMessageDialog(theApplication, "Cannot compute fragments for structures with uncertain terminals", "Error", JOptionPane.ERROR_MESSAGE);
        else if( t.hasNonFuzzyStructures() )
            JOptionPane.showMessageDialog(theApplication, "Cannot compute fragments for some structures with uncertain terminals", "Error", JOptionPane.ERROR_MESSAGE);
        else
            JOptionPane.showMessageDialog(theApplication, "Cannot compute fragments, all structures have uncertain terminals", "Error", JOptionPane.ERROR_MESSAGE);
        }    
    }
    }
        
    public void onAnnotationAborted(AnnotationThread t) { 
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

