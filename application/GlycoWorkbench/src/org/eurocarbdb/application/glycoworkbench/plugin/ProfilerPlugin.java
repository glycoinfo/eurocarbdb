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

import java.net.MalformedURLException;
import java.util.*;

import javax.swing.*;

import java.awt.event.*;

public class ProfilerPlugin implements Plugin, ActionListener, BaseDocument.DocumentChangeListener {


    public interface DictionariesChangeListener {   
    
    public void dictionariesChanged(DictionariesChangeEvent e);
    }

    public static class DictionariesChangeEvent extends java.util.EventObject {
    
    public static final int NONE = 0;
    public static final int INIT = 1;
    public static final int ADDED = 2;
    public static final int REMOVED = 3;
    public static final int CHANGED = 4;


    private int action = NONE;
    private StructureDictionary changed = null;
    
    public DictionariesChangeEvent(int _action, ProfilerPlugin _source, StructureDictionary _changed) {
        super(_source);

        action = _action;
        changed = _changed;
    }

    public int getAction() {
        return action;
    }
    
    public StructureDictionary getChanged() {
        return changed;
    }
    }

    // singletons

    protected PluginManager theManager = null;
    protected GlycoWorkbench theApplication = null;
    protected GlycanWorkspace theWorkspace = null;   
    
    protected JTabbedPane theDictionariesPane = null;
    protected DictionariesManagerPanel theDictionariesManagerPanel = null;
    protected DictionariesEditPanel theDictionariesEditPanel = null;
    protected DictionariesSearchPanel theDictionariesSearchPanel = null;
    
    protected TreeMap<String,StructureDictionary> theDictionaries = null;
    protected TreeMap<String,StructureDictionary> theUserDictionaries = null;

    protected ProfilerOptions theProfilerOptions = null;

    // actions
    protected boolean first_time_run = true;
    protected boolean showTopResults = true;
    protected ProfilerThread theThread = null;
    protected ProgressDialog progressDialog = null;
    protected javax.swing.Timer activityMonitor = null;

    protected boolean annotate = true;
    protected boolean merge = false;

    // events
    Vector<DictionariesChangeListener> dc_listeners = new Vector<DictionariesChangeListener>();


    public ProfilerPlugin(GlycoWorkbench bench) {
    	this.theApplication=bench;
    try {
        // set dictionaries
        theDictionaries = new TreeMap<String,StructureDictionary>();
        theUserDictionaries = new TreeMap<String,StructureDictionary>();

        addDictionary(new StructureDictionary("/conf/carbbankraw_dict.gwd",false,null));    
        addDictionary(new StructureDictionary("/conf/cfg_dict.gwd",false,null));    
        addDictionary(new StructureDictionary("/conf/glycosciences_dict.gwd",false,null));    
        addDictionary(new StructureDictionary("/conf/glycomedb_dict.gwd",false,null));    
        
        // set panels
        theDictionariesPane = new JTabbedPane();
        theDictionariesManagerPanel = new DictionariesManagerPanel(this);
        theDictionariesPane.add("Databases", theDictionariesManagerPanel);
        theDictionariesEditPanel = new DictionariesEditPanel(this);
        theDictionariesPane.add("Structures", theDictionariesEditPanel);
        theDictionariesSearchPanel = new DictionariesSearchPanel(this);
        theDictionariesPane.add("Search", theDictionariesSearchPanel);

        // set options

        theProfilerOptions = new ProfilerOptions();        
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }       

    public DictionariesManagerPanel getDictionariesManagerPanel() {
    return theDictionariesManagerPanel;
    }

    public DictionariesEditPanel getDictionariesEditPanel() {
    return theDictionariesEditPanel;
    }

    public DictionariesSearchPanel getDictionariesSearchPanel() {
    return theDictionariesSearchPanel;
    }
    
    public boolean containsDictionary(StructureDictionary dict) {
    if( dict==null )
        return false;

    for( StructureDictionary sd : theDictionaries.values() ) {
        if( sd.getDictionaryName().equals(dict.getDictionaryName()) || sd.getFileName().equals(dict.getFileName()) )
        return true;
    }
    return false;
    }
   
    public void addDictionary(StructureDictionary dict) {
    if( dict==null || containsDictionary(dict) )
        return;

    dict.addDocumentChangeListener(this);

    theDictionaries.put(dict.getDictionaryName(),dict);

    fireDictionaryAdded(dict);
    }


    public void removeDictionary(StructureDictionary dict) {
    if( !containsDictionary(dict) ) 
        return;    
        
    dict.removeDocumentChangeListener(this);

    theDictionaries.remove(dict.getDictionaryName());
    theUserDictionaries.remove(dict.getDictionaryName());
    theProfilerOptions.USER_DICTIONARIES_FILENAME.remove(dict.getFileName());

    fireDictionaryRemoved(dict);
    }

    public void addUserDictionary(StructureDictionary dict) {
    if( dict==null || containsDictionary(dict) )
        return;

    dict.addDocumentChangeListener(this);

    theUserDictionaries.put(dict.getDictionaryName(),dict);
    theDictionaries.put(dict.getDictionaryName(),dict);    
    theProfilerOptions.USER_DICTIONARIES_FILENAME.add(dict.getFileName());

    fireDictionaryAdded(dict);
    }

    public void saveUserDictionaries() throws Exception {
    for( StructureDictionary sd : theUserDictionaries.values() ) 
        sd.save();
    }

    public void init() {
      if( theWorkspace!=null ) {
        theProfilerOptions.retrieve(theWorkspace.getConfiguration());

        try {
        for( String filename : theProfilerOptions.USER_DICTIONARIES_FILENAME ) {
            if( FileUtils.exists(filename) ) {
            StructureDictionary dict = new StructureDictionary(filename,true,null);
            theUserDictionaries.put(dict.getDictionaryName(),dict);
            theDictionaries.put(dict.getDictionaryName(),dict);
            }
        }
        fireDictionariesInit(); 
        }
        catch(Exception e) {
        LogUtils.report(e);
        }
    }
    }

    public void exit() {
    if( theWorkspace!=null ) {
        theProfilerOptions.store(theWorkspace.getConfiguration());
    }
    }

    public StructureDictionary getDictionary(String name) {
    if( name==null )
        return null;
    return theDictionaries.get(name);
    }
    
    public Collection<StructureDictionary> getDictionaries() {
    return theDictionaries.values();
    }

    public Collection<StructureDictionary> getUserDictionaries() {
    return theUserDictionaries.values();
    }   

    public Collection<String> getDictionaryNames() {
    return theDictionaries.keySet();
    }

    public Collection<String> getUserDictionaryNames() {
    return theUserDictionaries.keySet();
    }

    public String getName() {
    return "Profiler";
    }

    public int getMnemonic() {
    return KeyEvent.VK_P;
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
    return theDictionariesPane;
    }

    public java.awt.Component getBottomComponent() {
    return null;
    } 
    
    public Collection<String> getViews() {
    Vector<String> views = new Vector<String>();
    views.add("Databases");
    views.add("Structures");
    views.add("Search");
    return views;
    }

    public Collection<GlycanAction> getActions() {
    Vector<GlycanAction> actions = new Vector<GlycanAction>();

    actions.add(new GlycanAction("options",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Set plugin options",KeyEvent.VK_O,"",this));

    actions.add(null);

    actions.add(new GlycanAction("findDatabase",this.theApplication.getThemeManager().getResizableIcon("finddatabase",ICON_SIZE.L3),"Find all structures with a given m/z value",KeyEvent.VK_F,"",this));
    actions.add(new GlycanAction("matchDatabase",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Annotate peaks with structures from the database",KeyEvent.VK_N,"",this));    

    actions.add(null);

    actions.add(new GlycanAction("matchStructuresCurrent",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Annotate peaks with current structure",KeyEvent.VK_C,"",this));
    actions.add(new GlycanAction("matchStructuresSelected",this.theApplication.getThemeManager().getResizableIcon("matchstructures",ICON_SIZE.L3),"Annotate peaks with selected structures",KeyEvent.VK_S,"",this));
    actions.add(new GlycanAction("matchStructuresAll",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Annotate peaks with all structures",KeyEvent.VK_A,"",this));

    actions.add(null);

    actions.add(new GlycanAction("search",this.theApplication.getThemeManager().getResizableIcon("search",ICON_SIZE.L3),"Search the databases",KeyEvent.VK_D,"",this));
    actions.add(new GlycanAction("searchAgain",this.theApplication.getThemeManager().getResizableIcon("searchagain",ICON_SIZE.L3),"Filter the search results",KeyEvent.VK_R,"",this));
    actions.add(new GlycanAction("searchCurrent",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Search for the current structure in the databases",KeyEvent.VK_H,"",this));

    actions.add(null);

    actions.add(new GlycanAction("storeStructuresCurrent",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Store current structure in a user database",KeyEvent.VK_U,"",this));
    actions.add(new GlycanAction("storeStructuresSelected",this.theApplication.getThemeManager().getResizableIcon("storedatabase",ICON_SIZE.L3),"Store selected structures in a user database",KeyEvent.VK_E,"",this));
    actions.add(new GlycanAction("storeStructuresAll",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Store all structures in a user database",KeyEvent.VK_L,"",this));

    return actions;
    }

    public Collection<GlycanAction> getToolbarActions() {
    Vector<GlycanAction> actions = new Vector<GlycanAction>();

    actions.add(new GlycanAction("matchStructuresSelected",this.theApplication.getThemeManager().getResizableIcon("matchstructures",ICON_SIZE.L3),"Annotate peaks with selected structures",KeyEvent.VK_S,"",this));
    actions.add(new GlycanAction("findDatabase",this.theApplication.getThemeManager().getResizableIcon("finddatabase",ICON_SIZE.L3),"Find all structures with a given m/z value",KeyEvent.VK_F,"",this));
    actions.add(new GlycanAction("storeStructuresSelected",this.theApplication.getThemeManager().getResizableIcon("storedatabase",ICON_SIZE.L3),"Store selected structures in a user database",KeyEvent.VK_E,"",this));
    actions.add(new GlycanAction("search",this.theApplication.getThemeManager().getResizableIcon("search",ICON_SIZE.L3),"Search the databases",KeyEvent.VK_D,"",this));
    actions.add(new GlycanAction("searchAgain",this.theApplication.getThemeManager().getResizableIcon("searchagain",ICON_SIZE.L3),"Filter the search results",KeyEvent.VK_R,"",this));
    return actions;
    }

    public Collection<GlycanAction> getObjectActions(Object prototype, ActionListener al) {
    Vector<GlycanAction> actions = new Vector<GlycanAction>();
    /*if( prototype instanceof PeakAnnotation ) {
        al = (al==null) ?this :al;        
        actions.add(new GlycanAction("storeAnnotations",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Store structures in a user database",-1,"",al));
        actions.add(new GlycanAction("editAnnotations",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Edit structure details",-1,"",al));
        actions.add(new GlycanAction("removeAnnotations",ThemeManager.getResizableEmptyIcon(ICON_SIZE.L3),"Remove structures from the user database",-1,"",al));
        }*/
    return actions;
    }

    public void setManager(PluginManager manager) {
    theManager = manager;
        if( theManager!=null ) 
        theManager.addMsPeakAction(new GlycanAction("findDatabase",this.theApplication.getThemeManager().getResizableIcon("finddatabase",ICON_SIZE.L3),"Find all structures matching the peaks",KeyEvent.VK_D,"",this));
    }

    public void setApplication(GlycoWorkbench application) {
    theApplication = application;    
    theDictionariesManagerPanel.setApplication(application);
    theDictionariesEditPanel.setApplication(application);
    theDictionariesSearchPanel.setApplication(application);
    }
   
    public void setWorkspace(GlycanWorkspace workspace) {
    theWorkspace = workspace;
    theDictionariesManagerPanel.setWorkspace(workspace);
    theDictionariesEditPanel.setWorkspace(workspace);
    theDictionariesSearchPanel.setWorkspace(workspace);
    if( theWorkspace!=null ) 
        theProfilerOptions.retrieve(theWorkspace.getConfiguration());
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
  
    public ProfilerOptions getOptions() {
    return theProfilerOptions;
    }

    public void show(String view) throws Exception {    
    if( view.equals("Databases") )
        theDictionariesPane.setSelectedComponent(theDictionariesManagerPanel);
    else if( view.equals("Structures") )
        theDictionariesPane.setSelectedComponent(theDictionariesEditPanel);
    else if( view.equals("Search") )
        theDictionariesPane.setSelectedComponent(theDictionariesSearchPanel);
    else
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

    static private PeakList assertPeakList(Object params) throws Exception{
    if( !(params instanceof PeakList) )
        throw new Exception("Invalid param object: PeakList needed");
    return (PeakList)params;    
    }

    static private Collection<PeakAnnotation> assertPACollection(Object params) throws Exception {
    if( !(params instanceof Collection) )
        throw new Exception("Invalid param object: Collection<PeakAnnotation> needed");
    for( Object o : (Collection)params ) {
        if( !(o instanceof PeakAnnotation) )
        throw new Exception("Invalid param object: Collection<PeakAnnotation> needed");
    }
    return (Collection<PeakAnnotation>)params;
    }    

    public boolean runAction(boolean ask, String action, Object params) throws Exception{
    
    if( action.equals("options") ) 
        return setOptions();

    // annotation actions
    if( action.equals("findDatabase") ) {
        annotate = false;
        merge = false;
        if( matchStructuresDictionaries(ask,assertPeakList(params)) ) {
        theManager.show("Search","Details");
        return true;        
        }
        return false;
    }    
    if( action.equals("matchDatabase") ) {
        annotate = true;
        merge = false;
        if( matchStructuresDictionaries(ask,assertPeakList(params)) ) {
        theManager.show("Annotation","Details");
        return true;
        }
        return false;
    }

    if( action.equals("matchStructuresCurrent") ) {
        annotate = true;
        merge = true;
        if( matchStructures(ask,assertPeakList(params),Collections.singleton(theApplication.getCanvas().getCurrentStructure())) ) {
        theManager.show("Annotation","Details");
        return true;
        }
        return false;
    }
    if( action.equals("matchStructuresSelected") ) {
        annotate = true;
        merge = true;
        if( matchStructures(ask,assertPeakList(params),theApplication.getCanvas().getSelectedStructures()) ) {
        theManager.show("Annotation","Details");
        return true;
        }
        return false;
    }
    if( action.equals("matchStructuresAll") ) {     
        annotate = true;
        merge = true;
        if( matchStructures(ask,assertPeakList(params),theWorkspace.getStructures().getStructures()) ) {
        theManager.show("Annotation","Details");
        return true;
        }
        return false;
    }

    // search actions
    if( action.equals("search") ) {
        if( search(null,null,false) ) {
        theManager.show("Profiler","Search");
        return true;
        }
        return false;
    }
    if( action.equals("searchAgain") ) { 
        if( search(null,null,true) ) {
        theManager.show("Profiler","Search");
        return true;
        }
        return false;  
    }
    if( action.equals("searchCurrent") ) {
        theApplication.getCanvas().enforceSelection();    
        if( search(theApplication.getCanvas().getCurrentStructure(),null,false) ) {      
        theManager.show("Profiler","Search");                        
        return true;
        }
        return false;   
    }

    // database actions 
    if( action.equals("storeStructuresCurrent") ) {
        theApplication.getCanvas().enforceSelection();    
        return storeStructures(Collections.singleton(theApplication.getCanvas().getCurrentStructure()));
    }
    if( action.equals("storeStructuresSelected") ) {
        theApplication.getCanvas().enforceSelection();    
        return storeStructures(theApplication.getCanvas().getSelectedStructures());
    }
    if( action.equals("storeStructuresAll") ) 
        return storeStructures(theWorkspace.getStructures().getStructures());

    // annotations actions
    /*
    if( action.equals("storeAnnotations") ) 
        return storeStructures(getStructures(assertPACollection(params)));    
    else if( action.equals("editAnnotations") ) {
        return editAnnotations(assertPACollection(params));
    }
    else if( action.equals("removeAnnotations") ) {
        return removeAnnotations(assertPACollection(params));
    }
    */
    
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
    theDictionariesManagerPanel.updateView();
     theDictionariesEditPanel.updateView();
     theDictionariesSearchPanel.updateView();
    }

    public void updateMasses() {
     theDictionariesEditPanel.updateMasses();
    theDictionariesSearchPanel.updateMasses();
    }

    //------------
    // ACTIONS
       
    public boolean setOptions() {
    // show profiler options dialog
    ProfilerOptionsDialog pdlg = new ProfilerOptionsDialog(theApplication,this,theProfilerOptions);
    pdlg.setVisible(true);     
    if( !pdlg.getReturnStatus().equals("OK") )
        return false;

    // show annotation options dialog
    FragmentOptions frag_opt = theWorkspace.getFragmentOptions();
    AnnotationOptions ann_opt = theWorkspace.getAnnotationOptions();
    AnnotationOptionsDialog adlg = new AnnotationOptionsDialog(theApplication,frag_opt,ann_opt,false,false);
    adlg.setVisible(true);     
    if( !adlg.getReturnStatus().equals("OK") )
        return false;    
   
    return true;
    }

    public boolean matchStructuresDictionaries(boolean ask,PeakList peaks) throws Exception {
    if( ask ) {
        // show profiler options dialog
        ProfilerOptionsDialog pdlg = new ProfilerOptionsDialog(theApplication,this,theProfilerOptions);
        pdlg.setVisible(true);     
        if( !pdlg.getReturnStatus().equals("OK") )
        return false;
    }

    // create the generator
    DictionaryStructureGenerator generator = new DictionaryStructureGenerator();
    for( int i=0; i<theProfilerOptions.DICTIONARIES.length; i++ )
        generator.add(theDictionaries.get(theProfilerOptions.DICTIONARIES[i]));

    // run matching
    return matchStructures(ask,peaks,generator);
    }

    public boolean matchStructures(boolean ask, PeakList peaks, StructureGenerator generator) throws Exception {

    // show annotation options dialog
    FragmentOptions frag_opt = theWorkspace.getFragmentOptions();
    AnnotationOptions ann_opt = theWorkspace.getAnnotationOptions();
    if( ask ) {
        AnnotationOptionsDialog adlg = new AnnotationOptionsDialog(theApplication,frag_opt,ann_opt,false,false);
        adlg.setVisible(true);     
        if( !adlg.getReturnStatus().equals("OK") )
        return false;    
    }    

    // halt interactions
    theApplication.haltInteractions();

    // match structures        
    theThread = new ProfilerThread(peaks,new Glycan(),generator,theProfilerOptions,ann_opt);
    theThread.setAddUnmatchedPeaks(true);
    //theThread.setAnnotatedPeaks(theWorkspace.getAnnotatedPeakList());

    return runAnnotation();
    }
   
    public boolean matchStructures(boolean ask, PeakList peaks, Collection<Glycan> structures) throws Exception {

    // show annotation options dialog
    FragmentOptions frag_opt = theWorkspace.getFragmentOptions();
    AnnotationOptions ann_opt = theWorkspace.getAnnotationOptions();
    if( ask ) {
        AnnotationOptionsDialog adlg = new AnnotationOptionsDialog(theApplication,frag_opt,ann_opt,false,false);
        adlg.setVisible(true);     
        if( !adlg.getReturnStatus().equals("OK") )
        return false;    
    }    

    // halt interactions
    theApplication.haltInteractions();

    // match structures        
    CollectionStructureGenerator generator = new CollectionStructureGenerator(structures);
    theThread = new ProfilerThread(peaks,new Glycan(),generator,theProfilerOptions,ann_opt);
    theThread.setAddUnmatchedPeaks(false);
    //theThread.setAnnotatedPeaks(theWorkspace.getAnnotatedPeakList());

    return runAnnotation();
    }

      
    public boolean storeStructures(Collection<Glycan> tostore) throws Exception {
    if( theUserDictionaries.size()==0 ) {
        theManager.show("Profiler","Databases");
        if( !theDictionariesManagerPanel.createUserDatabase() )
        return false;
    }        
    
    // open dialog
    StoreStructuresDialog dlg = new StoreStructuresDialog(theApplication,this,null);
    dlg.setVisible(true);
    if( dlg.getReturnStatus().equals("Cancel") )
        return false;
    
    // get dictionary
    StructureDictionary dict = getDictionary(dlg.getDictionaryName());
    if( dict==null )
        return false;
           
    // store
    if( dict.addAll(tostore,dlg.getType(),dlg.getSource()) ) {
        try {
        dict.save();    
        return true;
        }
        catch(Exception e) {
        LogUtils.report(e);
        return false;
        }
    }
    
    return false;
    }

    private boolean runAnnotation() {

    theThread.start();

    // launch progress dialog
    progressDialog = new ProgressDialog(theApplication, "Matching peaks with structures", null, -1,-1);
    progressDialog.setVisible(true);
    
    // set up the timer action
    activityMonitor = new javax.swing.Timer(200, new ActionListener() {  
        public void actionPerformed (ActionEvent event) {  
            // show progress
            progressDialog.setNote(theThread.getProgress() + " structures tested, " + theThread.getMatches() + " matched");

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

    public void onAnnotationCompleted(ProfilerThread t) {
        
    // show annotations                
    AnnotatedPeakList dest = null;
    if( annotate )
        dest = theWorkspace.getAnnotatedPeakList();
    else {
        dest = theWorkspace.getSearchResults();    
        theWorkspace.setSearchGenerator(this);
    }

    if( merge )
        dest.merge(t.getAnnotatedPeaks());
    else
        dest.copy(t.getAnnotatedPeaks());
        
    // restore interactions
    theApplication.restoreInteractions();               
    }
        
    public void onAnnotationAborted(ProfilerThread t) { 
    // show annotations            
    AnnotatedPeakList dest = null;
    if( annotate )
        dest = theWorkspace.getAnnotatedPeakList();
    else {
        dest = theWorkspace.getSearchResults();    
        theWorkspace.setSearchGenerator(this);
    }
    dest.copy(t.getAnnotatedPeaks());

    // restore interactions
    theApplication.restoreInteractions();                 
    }
    
    static private Collection<Glycan> getStructures(Collection<PeakAnnotation> pac) {
    Vector<Glycan> ret = new Vector<Glycan>();
    for( PeakAnnotation pa : pac ) {
        if( pa.isAnnotated() )
        ret.add(pa.getFragment());
    }
    return ret;
    }

    public boolean search(Glycan g, StructureDictionary d, final boolean search_again) throws MalformedURLException {
    // show dialog
    final SearchDialog dlg = new SearchDialog(theApplication,this,search_again,theWorkspace.getGraphicOptions());
    
    Runnable run=new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
		    if( !dlg.getReturnStatus().equals("OK") ) 
		        return ;

		    // init search set
		    Collection<StructureType> entries;
		    if( search_again )
		        entries = theDictionariesSearchPanel.getData();
		    else 
		        entries = dlg.getDatabase().getStructureTypes();
		    
		    try{ 
		        // init parameters
		        MassOptions mass_opt = new MassOptions();
		        Glycan structure = dlg.getStructure();
		        String type = dlg.getType().toLowerCase();
		        String source = dlg.getSource().toLowerCase();
		        boolean include_redend = dlg.getIncludeRedEnd();
		        boolean include_all_leafs = dlg.getIncludeAllLeafs();
		        structure.setMassOptions(mass_opt);        
		        
		        // filter entries
		        Collection<StructureType> found = new Vector<StructureType>();
		        if( !structure.isEmpty() || type.length()>0 || source.length()>0 ) {
		        for( StructureType st : entries ) {
		            Glycan s = st.generateStructure(mass_opt);
		            if( (structure.isEmpty() || s.contains(structure,include_redend,include_all_leafs)) &&
		            (type.length()==0 || st.type.toLowerCase().indexOf(type)!=-1) &&
		            (source.length()==0 || st.source.toLowerCase().indexOf(source)!=-1) )
		            found.add(st);
		        }                    
		        }
		        else 
		        found = entries;        
		        
		        // show results
		        theDictionariesSearchPanel.setData(found);
		        theManager.show("Profiler","Search");
		        
		    }
		    catch(Exception e) {
		        LogUtils.report(e);
		        //return false
		    }
			
		}
    	
    };

    dlg.setRunnable(run);
    dlg.setStructure(g);
    dlg.setDatabase(d);
    dlg.setVisible(true);
    
    
    return true;
    
        
    }

    /*
    public boolean editAnnotations(Collection<PeakAnnotation> pac) throws Exception {
    if( pac.size()==0 )
        return false;

    // check if annotation has valid source
    PeakAnnotation pa = pac.iterator().next();
    FragmentEntry fe = pa.getFragmentEntry();
    if( fe.isEmpty() || fe.source==null || !(fe.source instanceof StructureType) ) {
        JOptionPane.showMessageDialog(theApplication,"Only the information about structures contained in user databases can be modified.", "Structure cannot be changed.", JOptionPane.ERROR_MESSAGE);    
        return false;
    }
        
    // check if structure is in user DB
    StructureType st = (StructureType)fe.source;
    StructureDictionary sd = theUserDictionaries.get(st.getDatabase());
    if( sd==null ) {
        JOptionPane.showMessageDialog(theApplication,"Only the information about structures contained in user databases can be modified.", "Structure cannot be changed.", JOptionPane.ERROR_MESSAGE);    
        return false;
    }

    // open dialog
    StoreStructuresDialog dlg = new StoreStructuresDialog(theApplication,st,this);
    dlg.setVisible(true);
    if( dlg.getReturnStatus().equals("Cancel") )
        return false;

    // edit type
    st.setType(dlg.getType());
    st.setSource(dlg.getSource());
    sd.save();

    JOptionPane.showMessageDialog(theApplication,"Details of the selected structure have been updated.", "Details updated.", JOptionPane.INFORMATION_MESSAGE);
    return true;
    }

    public boolean removeAnnotations(Collection<PeakAnnotation> pac) throws Exception {

    // ask for confirmation
    if( JOptionPane.showConfirmDialog(theApplication,"Are you sure you want to permanently remove the selected structures?")!=JOptionPane.YES_OPTION ) 
        return false;
    
    // remove structures
    boolean removed_any = false;
    boolean nonuserdb_any = false;

    for( PeakAnnotation pa : pac) {
        FragmentEntry fe = pa.getFragmentEntry();

        // check if annotation has valid source
        if( fe.isEmpty() )
        continue;
        if( fe.source==null || !(fe.source instanceof StructureType) ) {
        nonuserdb_any = true;
        continue;
        }
        
        // check if structure is in user DB
        StructureType st = (StructureType)fe.source;
        StructureDictionary sd = theUserDictionaries.get(st.getDatabase());
        if( sd==null ) {
        nonuserdb_any = true;
        continue;
        }
        
        // remove structure from db
        removed_any = sd.remove(st);
    }
    
    // show results
    if( removed_any ) { 
        saveUserDictionaries();

        if( nonuserdb_any ) 
        JOptionPane.showMessageDialog(theApplication,"Only the selected structures contained in user database(s) have been removed.", "Structures removed.", JOptionPane.WARNING_MESSAGE);
        else 
        JOptionPane.showMessageDialog(theApplication,"All selected structures have been removed from the user database(s).", "Structures removed.", JOptionPane.INFORMATION_MESSAGE);
    }
    else
        JOptionPane.showMessageDialog(theApplication,"Only structures contained in user databases can be removed.", "No structures removed.", JOptionPane.ERROR_MESSAGE);
    return removed_any;
    }
    */


    // listeners

    public void documentInit(BaseDocument.DocumentChangeEvent e) {
    fireDictionaryChanged((StructureDictionary)e.getSource());
    }

    public void documentChanged(BaseDocument.DocumentChangeEvent e) {
    fireDictionaryChanged((StructureDictionary)e.getSource());
    }

    public void addDictionariesChangeListener(DictionariesChangeListener l) {
    if( l!=null && !dc_listeners.contains(l) )
        dc_listeners.add(l);
    }


    public void removeDictionariesChangeListener(DictionariesChangeListener l) {
    if( l!=null )
        dc_listeners.remove(l);
    }

    public void fireDictionariesInit() {
    for( DictionariesChangeListener dcl : dc_listeners ) 
        dcl.dictionariesChanged(new DictionariesChangeEvent(DictionariesChangeEvent.INIT,this,null));    
    }

    public void fireDictionaryAdded(StructureDictionary added) {
    for( DictionariesChangeListener dcl : dc_listeners ) 
        dcl.dictionariesChanged(new DictionariesChangeEvent(DictionariesChangeEvent.ADDED,this,added));    
    }

    public void fireDictionaryRemoved(StructureDictionary removed) {
    for( DictionariesChangeListener dcl : dc_listeners ) 
        dcl.dictionariesChanged(new DictionariesChangeEvent(DictionariesChangeEvent.REMOVED,this,removed));    
    }

    public void fireDictionaryChanged(StructureDictionary changed) {
    for( DictionariesChangeListener dcl : dc_listeners ) 
        dcl.dictionariesChanged(new DictionariesChangeEvent(DictionariesChangeEvent.CHANGED,this,changed));    
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

