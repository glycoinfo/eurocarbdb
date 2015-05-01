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

import org.eurocarbdb.application.glycoworkbench.plugin.reporting.*;
import org.eurocarbdb.application.glycoworkbench.plugin.grammar.*;
import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.event.*;

public class ReportingPlugin implements Plugin, ActionListener {

    protected PluginManager theManager = null;
    protected GlycoWorkbench theApplication = null;
    protected GlycanWorkspace theWorkspace = null;  

    protected Grammar theGrammar = null;

    protected AnnotationReportOptions theAnnotationReportOptions = new AnnotationReportOptions();
    protected ProfilesComparisonReportOptions theProfilesComparisonReportOptions = new ProfilesComparisonReportOptions();

    protected HashMap<AnnotationReportDocument,AnnotationReportEditor> openAnnotationReportEditors = new HashMap<AnnotationReportDocument,AnnotationReportEditor>();

    public ReportingPlugin(GlycoWorkbench bench) {
    	this.theApplication=bench;
    try {
        theGrammar = new Grammar("/conf/sim_human_ng_grammar.gwg",false);
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }       

    public void init() {
    if( theWorkspace!=null ) {
        theAnnotationReportOptions.retrieve(theWorkspace.getConfiguration());
        theProfilesComparisonReportOptions.retrieve(theWorkspace.getConfiguration());
        }
    }

    public void exit() {
    if( theWorkspace!=null ) {
        theAnnotationReportOptions.store(theWorkspace.getConfiguration());
        theProfilesComparisonReportOptions.store(theWorkspace.getConfiguration());
        }
    }

    public String getName() {
    return "Reporting";
    }

    public int getMnemonic() {
    return KeyEvent.VK_R;
    }

    //See future plans
    public ResizableIcon getResizableIcon(){
    	return FileUtils.getThemeManager().getResizableIcon("report", ICON_SIZE.L3).getResizableIcon();
    }
    
    public ImageIcon getIcon() {
    return FileUtils.defaultThemeManager.getImageIcon("report");
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
    actions.add(new GlycanAction("reportAnnotations",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Create a report of the annotations",KeyEvent.VK_C,"",this));
    actions.add(new GlycanAction("openAnnotationsReport",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Open a previous annotations report",KeyEvent.VK_O,"",this));
        actions.add(null);
    actions.add(new GlycanAction("deisotope",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Deisotope the annotated peaklist using the annotations",KeyEvent.VK_D,"",this));
    actions.add(new GlycanAction("reportProfilesComparison",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Create a report comparing different profiles",KeyEvent.VK_P,"",this));

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
    }
   
    public void setWorkspace(GlycanWorkspace workspace) {
    theWorkspace = workspace;
    if( theWorkspace!=null ) {
        theAnnotationReportOptions.retrieve(theWorkspace.getConfiguration());
        theProfilesComparisonReportOptions.retrieve(theWorkspace.getConfiguration());
        }
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
  
    public AnnotationReportOptions getAnnotationReportOptions() {
    return theAnnotationReportOptions;
    }

    public void show(String view) throws Exception {    
    throw new Exception("Invalid view: " + view);
    }

    public boolean runAction(String action) throws Exception{
    return runAction(action,theWorkspace.getPeakList());
    }


    public boolean runAction(String action, Object params) throws Exception {
    if( action.equals("reportAnnotations") )
        return reportAnnotations();
    if( action.equals("reportProfilesComparison") )
        return reportProfilesComparison();

    if( action.equals("deisotope") )
        return deisotopeAnnotatedPeakList(false);
    if( action.equals("openAnnotationsReport") )
        return openAnnotationsReport();

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
 
    public boolean reportAnnotations() {     
    if( theWorkspace.getAnnotatedPeakList().size()>0 ) {            
        AnnotationReportChoiceDialog dlg = new AnnotationReportChoiceDialog(theApplication,theWorkspace,theAnnotationReportOptions);
        dlg.setVisible(true);
        
        if( dlg.getReturnStatus().equals("OK") ) {
        AnnotationReportDocument doc = new AnnotationReportDocument(dlg.getStartMZ(),dlg.getEndMZ(), dlg.getPeakData(),dlg.getParentStructure(),dlg.getPeakAnnotationCollection(),theAnnotationReportOptions,theWorkspace.getGraphicOptions());
        theWorkspace.addAnnotationReport(doc);
        showAnnotationsReport(doc,true);
                return true;
        }
    }    
    else 
        javax.swing.JOptionPane.showMessageDialog(theApplication,"There are not annotation to display.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    return false;
    }


    public boolean reportProfilesComparison() {     
        ProfilesComparisonReportDialog dlg = new ProfilesComparisonReportDialog(theApplication,theWorkspace,theProfilesComparisonReportOptions);
        dlg.setVisible(true);

        if( dlg.getReturnStatus().equals("OK") && dlg.getFirstGroup().size()>0 ) {
        theApplication.haltInteractions();
        try {
        ProfilesComparisonReportDocument doc = new ProfilesComparisonReportDocument(dlg.getFirstGroup(),dlg.getSecondGroup(),dlg.getScanNameMap(),theGrammar,theProfilesComparisonReportOptions);
     
        theApplication.restoreInteractions();

        if( theProfilesComparisonReportOptions.REPRESENTATION == theProfilesComparisonReportOptions.TABLE )
            new ProfilesComparisonReportTableFrame(theApplication,doc,theProfilesComparisonReportOptions).setVisible(true);
        else
            new ProfilesComparisonReportChartFrame(theApplication,doc,theProfilesComparisonReportOptions).setVisible(true);
        return true;
        }
        catch(Exception e) {
        LogUtils.report(e);
        theApplication.restoreInteractions();
        return false;
        }
        }    
        return false;
    }

    public boolean openAnnotationsReport() {
    AnnotationReportDocument doc = new AnnotationReportDocument();
    if( theApplication.onOpen(null,doc,false) ) 
        showAnnotationsReport(doc,false);    
    return true;
    }

    public boolean showAnnotationsReport(AnnotationReportDocument doc, boolean init) {
    if( doc!=null ) {
        AnnotationReportEditor are = openAnnotationReportEditors.get(doc);
        if( are==null || !are.isVisible() ) {
        are = new AnnotationReportEditor(theApplication,this,doc,init);
        openAnnotationReportEditors.put(doc,are);
        are.setVisible(true);
        }
        return true;
    }
    return false;
    }


    public boolean deisotopeAnnotatedPeakList(boolean show_all) {
    if( theWorkspace.getAnnotatedPeakList().size()==0 ) {
        JOptionPane.showMessageDialog(theApplication, "The annotated peaklis is empty", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    if( JOptionPane.showConfirmDialog(theApplication, "Attention, the method will compute invalid intensities if the peaklist has already been deisotoped. Do you still want to continue?", null, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION ) {
        return false;
    }


    try {
        // retrieve compositions
        TreeMap<Peak,HashSet<Molecule>> data = new TreeMap<Peak,HashSet<Molecule>>();
        for( PeakAnnotationMultiple pam : theWorkspace.getAnnotatedPeakList().getAnnotations() ) {
        Peak p = pam.getPeak();
        
        HashSet<Molecule> compositions = new HashSet<Molecule>();        
        for( Vector<Annotation> annotations : pam.getAnnotations() ) {
            for( Annotation annotation : annotations ) {
            if( !annotation.isEmpty() )
                compositions.add(annotation.getFragmentEntry().fragment.computeIon());
            }
        }
        
        data.put(p.clone(),compositions);
        }

        // deisotope
        for( Map.Entry<Peak,HashSet<Molecule>> e : data.entrySet() ) {
        Peak p = e.getKey();
        HashSet<Molecule> compositions = e.getValue();
        if( p.getIntensity()==0. || compositions.size()==0 )
            continue;
        
        // compute isotope curves
        Vector<double[][]> all_curves = new Vector<double[][]>();
        for( Molecule m : compositions ) {            
            double[][] curve = MSUtils.getIsotopesCurve(1,m,show_all);
            MSUtils.adjust(curve,p.getMZ(),p.getIntensity());
            all_curves.add(curve);            
        }
        double[][] avg_curve = MSUtils.average(all_curves,show_all);
        
        // adjust intensity for this peak to the total intensity for this signal
        double sum = 0.;
        for( int i=0; i<avg_curve[0].length; i++ ) 
            sum += avg_curve[1][i];
        p.setIntensity(sum);
        
        // remove isotopes from the following peaks
        int i=1;
        int no_isotopes = avg_curve[0].length;
        double mass_tol = (show_all) ?0.0001 :0.5;    
        for( Map.Entry<Peak,HashSet<Molecule>> t : data.tailMap(p).entrySet() ) {
            Peak tp = t.getKey(); 
            if( i==no_isotopes )
            break;
            
            // search matching peak
            for( ; i<no_isotopes && tp.getMZ()>(avg_curve[0][i]+mass_tol); i++);        

            // remove isotope intensity if matching
            if( i<no_isotopes && Math.abs(tp.getMZ()-avg_curve[0][i])<=mass_tol ) 
            tp.setIntensity(Math.max(0.,tp.getIntensity()-avg_curve[1][i]));        
        }       
        }        

        // set new peak intensities for the annotated peaklist
        theWorkspace.getAnnotatedPeakList().updatePeaks(data.keySet());
    
        if( JOptionPane.showConfirmDialog(theApplication, "Do you want to update the intensities in the peaklist as well?", null, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ) {
        theWorkspace.getPeakList().updatePeaks(data.keySet());
        }

        return true;
    }
    catch(Exception ex) {
        LogUtils.report(ex);
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