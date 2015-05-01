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

import java.awt.print.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.helpers.AttributesImpl;


public class GlycanWorkspace extends BuilderWorkspace implements SAXUtils.SAXWriter   {
    
    public static class Event extends java.util.EventObject {
    
    protected Scan parent;
    protected Scan child;
    protected Scan current;
    protected int  index;

    public Event(GlycanWorkspace _source, Scan _parent, Scan _child, int _index, Scan _current) {
        super(_source);
        parent  = _parent;
        child   = _child;
        index   = _index;
        current = _current;
    }

    public Scan getParentScan() {
        return parent;
    }

    public Scan getChildScan() {
        return child;
    }

    public int getIndex() {
        return index;
    }

    public Scan getCurrentScan() {
        return current;
    }
    }           
                     
    public interface Listener {
    
    public void currentScanChanged(Event e);

    public void scanAdded(Event e);

    public void scanRemoved(Event e);

    public void internalDocumentChanged(Event e);
    
    }

     
    // configuration
    protected FragmentOptions   theFragmentOptions;
    protected AnnotationOptions theAnnotationOptions;
 
    // documents
    protected Plugin            theSearchGenerator;
    protected AnnotatedPeakList theSearchResults;
    protected Vector<Scan>      theScanList;
    protected Scan              currentScan;
    protected Double recent_mz_value = null;     

    // listeners
    protected Vector<Listener> gw_listeners = new Vector<Listener>();
    
    //----
    
    public GlycanWorkspace() {
    super();
    }
    
    public GlycanWorkspace(String config_file, boolean create) {
    super(config_file,create);
    }

    // base document    

    public Collection<javax.swing.filechooser.FileFilter> getFileFormats() {
    Vector<javax.swing.filechooser.FileFilter> filters = new Vector<javax.swing.filechooser.FileFilter>();
    
    filters.add(new ExtensionFileFilter("gwp", "GlycoWorkbench workspace file"));
    return filters;
    }

    public javax.swing.filechooser.FileFilter getAllFileFormats() {
    return new ExtensionFileFilter("gwp", "Workspace files");
    }

    // 

    protected void createConfiguration() {
    super.createConfiguration();

    theFragmentOptions = new FragmentOptions();
    theAnnotationOptions = new AnnotationOptions();        
    }

    public void init(String config_file, boolean create, boolean keep_configuration ) {
    
    super.init(config_file,create,keep_configuration);
    
    // initialize documents
    theSearchResults = new AnnotatedPeakList();
    theScanList = new Vector<Scan>();
    theScanList.add(currentScan = new Scan(this));
    registerListeners(currentScan);    
    }

    protected void retrieveFromConfiguration() {
    super.retrieveFromConfiguration();

    theFragmentOptions.retrieve(theConfiguration);
    theAnnotationOptions.retrieve(theConfiguration);
    }

    protected void storeToConfiguration(boolean save_options) {
    super.storeToConfiguration(save_options);

    if( save_options ) {
        theFragmentOptions.store(theConfiguration);
        theAnnotationOptions.store(theConfiguration);
    }
    }     

    public Double getRecentMZValue() {
    return recent_mz_value;
    }

    public void setRecentMZValue(Double v) {
    recent_mz_value = v;
    }   

    public FragmentOptions getFragmentOptions() {
    return theFragmentOptions;
    }

    public void storeFragmentOptions() {
    theFragmentOptions.store(theConfiguration);
    }

    public AnnotationOptions getAnnotationOptions() {
    return theAnnotationOptions;
    }
    
    public void storeAnnotationOptions() {
    theAnnotationOptions.store(theConfiguration);
    }  
    
    public Scan scanAt(int ind) {
    return theScanList.elementAt(ind);
    }

    public int indexOf(Scan s) {
    return theScanList.indexOf(s);
    }

    public int getNoScans() {
    return theScanList.size();
    }

    public Collection<Scan> getScanList() {
    return theScanList;
    }
    
    public Scan getFirstScan() {
    return theScanList.firstElement();
    }

    public Scan getNextScan(Scan s) {
    if( s==null )
        return null;

    int index = theScanList.indexOf(s);
    if( index==-1 )
        return null;
    if( index==theScanList.size()-1 ) {
        if( index>0 )        
        return theScanList.elementAt(index-1);
        return null;
    }
    return theScanList.elementAt(index+1);
    }
    

    public Scan getCurrentScan() {
    return currentScan;
    }
    
    public boolean setCurrentScan(Scan s) {
    if( s!=null ) {
        if( s!=currentScan ) {
        Scan old    = currentScan;
        Scan parent = old.getParent();
        int index   = (parent!=null) ?parent.indexOf(old) :-1;

        currentScan = s;
        fireCurrentScanChanged(new Event(this,parent,old,index,s));
        }
        return true;
    }
    return false;
    }

    public boolean addScan(Scan parent, Scan toadd) {
    if( toadd==null ) 
        return false;

    int index = -1;
    if( parent==null ) {
        theScanList.add(toadd);
        index = theScanList.indexOf(toadd);
    }
    else {
        parent.add(toadd);
        index = parent.indexOf(toadd);
    }
    
    registerListeners(toadd);
    
    fireScanAdded(new Event(this,parent,toadd,index,currentScan));
    fireDocumentChanged(this);
    return true;        
    }

    public boolean removeScan(Scan parent, Scan toremove) {
    if( toremove==null ) 
        return false;

    int index = -1;
    if( parent==null ) {
        index = theScanList.indexOf(toremove);
        if( index==-1 )
        return false;
        
        if( theScanList.size()==1 ) 
        addScan(null,new Scan(this));
        if( toremove.containsSubTree(currentScan) ) 
        setCurrentScan(getNextScan(currentScan));    
        
        theScanList.remove(toremove);
    }
    else { 
        index = parent.indexOf(toremove);
        if( index==-1 )
        return false;

        if( toremove.containsSubTree(currentScan) )
        setCurrentScan(parent);

        parent.remove(toremove);
    }
        
    deregisterListeners(toremove);
        
    fireScanRemoved(new Event(this,parent,toremove,index,currentScan));    
    fireDocumentChanged(this);
    return true;
    }      
    
    public boolean syncScan(Scan scan){
    	boolean matchFound=false;
    	Scan parentScan=scan.getParent();
		if(parentScan!=null){
			AnnotatedPeakList annotatedPeakList=parentScan.getAnnotatedPeakList();
			PeakAnnotationMultiple peakAnnotations=annotatedPeakList.getAnnotations(new Peak(scan.getPrecursorMZ(),.0));
			if(peakAnnotations!=null){
				
				for(Vector<Annotation> structureToAnnotations:peakAnnotations.getAnnotations()){
					for(Annotation annotation:structureToAnnotations){
						scan.getStructures().addStructure(annotation.getFragmentEntry().fragment);
					}
					
					if(structureToAnnotations.size() > 0){
						matchFound=true;
					}
				}
			}
		}else{
			
		}
    	
    	fireDocumentChanged(this);
    	return matchFound;
    }

    public Vector<Scan> getAllScans() {
        Vector<Scan> ret = new Vector<Scan>();
        for( Scan s : theScanList ) 
            getAllScans(s,ret);
        return ret;            
    }
    
    public Vector<Scan> getAllParentScans(){
    	Vector<Scan> ret = new Vector<Scan>();
    	ret.addAll(theScanList);
    	return ret;
    }
    
    private void getAllScans(Scan s, Vector<Scan> dst) {
        dst.add(s);
        for( Scan c : s.getChildren() )
            getAllScans(c,dst);
    }

    public Scan findInternalDocument(BaseDocument doc) {
    if( doc!=null ) {
        for( Scan s : theScanList ) {
        Scan ret = s.findInternalDocument(doc);
        if( ret!=null )
            return ret;
        }
    }
    return null;
    }

    public boolean addAnnotationReport(AnnotationReportDocument ard) {
    if( ard==null || currentScan==null )
        return false;
    
    currentScan.getAnnotationReports().add(ard);
    ard.addDocumentChangeListener(this);

    fireDocumentChanged();
    return true;
    }

    public boolean removeAnnotationReport(AnnotationReportDocument ard) {
    if( ard==null || currentScan==null )
        return false;
    
    if( !currentScan.getAnnotationReports().contains(ard) )
        return false;

    currentScan.getAnnotationReports().remove(ard);
    ard.addDocumentChangeListener(this);

    fireDocumentChanged();
    return true;
    }


    private void registerListeners(Scan s) {
    if( s!=null ) {
        s.getStructures().addDocumentChangeListener(this);
        s.getFragments().addDocumentChangeListener(this);
        s.getSpectra().addDocumentChangeListener(this);
        s.getPeakList().addDocumentChangeListener(this);
        s.getAnnotatedPeakList().addDocumentChangeListener(this);
        s.getNotes().addDocumentChangeListener(this);
        for( AnnotationReportDocument ard : s.getAnnotationReports() )
        ard.addDocumentChangeListener(this);
    }
    }

    private void deregisterListeners(Scan s) {
    if( s!=null ) {
        s.getStructures().removeDocumentChangeListener(this);
        s.getFragments().removeDocumentChangeListener(this);
        s.getSpectra().removeDocumentChangeListener(this);
        s.getPeakList().removeDocumentChangeListener(this);
        s.getAnnotatedPeakList().removeDocumentChangeListener(this);
        s.getNotes().removeDocumentChangeListener(this);
        for( AnnotationReportDocument ard : s.getAnnotationReports() )
        ard.removeDocumentChangeListener(this);
    }
    }   

    private void addListeners(Scan node) {
    if( node==null )
        return;

    registerListeners(node);
    for( Scan s : node.getChildren()) 
        addListeners(s);
    }

    private void removeListeners(Scan node) {
    if( node==null )
        return;

    deregisterListeners(node);
    for( Scan s : node.getChildren()) 
        removeListeners(s);
    }
    
    public void setPrecursorMZ(Scan s, Double mz) {
    if( s!=null ) {
        s.setPrecursorMZ(mz);
        //fireCurrentScanChanged(new Event(this,s.getParent(),s,-1,s));
        setChanged(true);
        fireInternalDocumentChanged(s);
    }    
    }

    public void setMsMs(Scan s, boolean f) {
    if( s!=null ) {
        s.setMsMs(f);
        setChanged(true);
        fireInternalDocumentChanged(s);
    }    
    }

    public boolean isCurrent(Scan s) {
    return currentScan==s;
    }

    public Plugin getSearchGenerator() {
    return theSearchGenerator;
    }

    public void setSearchGenerator(Plugin p) {
    theSearchGenerator = p;
    }

    public AnnotatedPeakList getSearchResults() {
    return theSearchResults;
    }

    public GlycanDocument getStructures() {
    return currentScan.getStructures();
    }

    public FragmentDocument getFragments() {
    return currentScan.getFragments();
    }

    public SpectraDocument getSpectra() {
    return currentScan.getSpectra();
    }

    public PeakList getPeakList() {
    return currentScan.getPeakList();
    }

    public AnnotatedPeakList getAnnotatedPeakList() {
    return currentScan.getAnnotatedPeakList();
    }
    
    public NotesDocument getNotes() {
    return currentScan.getNotes();
    }

    public Collection<AnnotationReportDocument> getAnnotationReports() {
    return currentScan.getAnnotationReports();
    }           

    public Collection<BaseDocument> getAllDocuments() {
    Vector<BaseDocument> ret = new Vector<BaseDocument>();

    ret.add(this);
    ret.add(new AnnotationReportDocument());
    ret.add(currentScan.getNotes());
    ret.add(currentScan.getSpectra());
    ret.add(currentScan.getAnnotatedPeakList());
    ret.add(currentScan.getPeakList());
    ret.add(currentScan.getFragments());
    ret.add(currentScan.getStructures());

    return ret;
    }


    public Collection<BaseDocument> getUnsavedDocuments() {
    Vector<BaseDocument> ret = new Vector<BaseDocument>();

    for(Scan s : theScanList) 
        getUnsavedDocuments(ret,s);

    return ret;        
    }

    private void getUnsavedDocuments(Vector<BaseDocument> buffer, Scan scan) {

    if( scan.getStructures().hasChanged() )
        buffer.add(scan.getStructures());
    if( scan.getFragments().hasChanged() )
        buffer.add(scan.getFragments());
    if( scan.getSpectra().hasChanged() )
        buffer.add(scan.getSpectra());
    if( scan.getPeakList().hasChanged() )
        buffer.add(scan.getPeakList());
    if( scan.getAnnotatedPeakList().hasChanged() )
        buffer.add(scan.getAnnotatedPeakList());
    if( scan.getNotes().hasChanged() )
        buffer.add(scan.getNotes());

    for( AnnotationReportDocument ard : scan.getAnnotationReports() )
        if( ard.hasChanged() )
        buffer.add(ard);
    
    for( Scan c : scan.getChildren() ) 
        getUnsavedDocuments(buffer,c);    
    }

    public void resetChanges() {
    this.resetStatus();

    for(Scan s : theScanList)
        resetChanges(s);    
    
    fireDocumentInit();
    }

    private void resetChanges(Scan s) {
    s.getStructures().resetStatus();
    s.getFragments().resetStatus();
    s.getSpectra().resetStatus();
    s.getPeakList().resetStatus();
    s.getAnnotatedPeakList().resetStatus();
    s.getNotes().resetStatus();

    for( AnnotationReportDocument ard : s.getAnnotationReports() )
        ard.resetStatus();

    for( Scan c : s.getChildren() )
        resetChanges(c);
    }    

    // listeners
    
    public void addWorkspaceListener(Listener l) {
    if( l!=null )
        gw_listeners.add(l);
    }

    public void removeWorkspaceListener(Listener l) {
    gw_listeners.remove(l);
    }

    public void fireCurrentScanChanged(Event e) {
    for( Listener l : gw_listeners ) 
        l.currentScanChanged(e);
    }

    public void fireScanAdded(Event e) {
    for( Listener l : gw_listeners ) 
        l.scanAdded(e);    
    }

    public void fireScanRemoved(Event e) {
    for( Listener l : gw_listeners ) 
        l.scanRemoved(e);
    }

    public void fireInternalDocumentChanged(BaseDocument doc) {
    Scan s = findInternalDocument(doc);
    fireInternalDocumentChanged(s);
    }

    public void fireInternalDocumentChanged(Scan s) {
    if( s==null || gw_listeners==null )
        return;

    Event e = new Event(this,s,null,-1,currentScan);
    for( Listener l : gw_listeners ) 
        l.internalDocumentChanged(e);
    }
 

    public void fireDocumentInit() {
    for(Scan s : theScanList)
        fireDocumentInit(s);
    super.fireDocumentInit();
    }   

    private void fireDocumentInit(Scan s) {
    s.getStructures().fireDocumentInit();
    s.getFragments().fireDocumentInit();
    s.getSpectra().fireDocumentInit();
    s.getPeakList().fireDocumentInit();
    s.getAnnotatedPeakList().fireDocumentInit();
    s.getNotes().fireDocumentInit();

    for( AnnotationReportDocument ard : s.getAnnotationReports() )
        ard.fireDocumentInit();

    for( Scan c : s.getChildren() )
        fireDocumentInit(c);
    }    

    public void documentInit(BaseDocument.DocumentChangeEvent e) {    
    // there's a change in some internal document
    setChanged(true);
    fireInternalDocumentChanged((BaseDocument)e.getSource());
    }

    public void documentChanged(BaseDocument.DocumentChangeEvent e) {
    // there's a change in some internal document
    setChanged(true);
    fireInternalDocumentChanged((BaseDocument)e.getSource());
    }

    // serialization 

    protected void read(InputStream is, boolean merge) throws Exception {
    /*Document document = XMLUtils.read(is);
    if( document==null )
        throw new Exception("Cannot read from string");    
        fromXML(XMLUtils.assertChild(document,"GlycanWorkspace"),merge);*/
    SAXUtils.read(is,new SAXHandler(this,merge));           
    }

    public void write(OutputStream os) throws Exception {
    /*
      Document document = XMLUtils.newDocument();
      document.appendChild(toXML(document));
      XMLUtils.write(fos,document);
    */
    SAXUtils.write(os,this);
    }

    public String toString() {
    try{ 
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        write(bos);
        return bos.toString();
    }
    catch(Exception e) {
        LogUtils.report(e);
        return "";
    }
    }

    public void fromString(String str, boolean merge) throws Exception {
    ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes());
    read(bis,merge);
    }

    public void fromXML(Node w_node, boolean merge) throws Exception {
    
    resetStatus();
    
    // set configuration
    //Node c_node = XMLUtils.assertChild(w_node,"Configuration");
    //theConfiguration = Configuration.fromXML(c_node);
    //retrieveFromConfiguration();

    if( !merge )
        theScanList = new Vector<Scan>();

    // set scan list
    Vector<Node> s_nodes = XMLUtils.findAllChildren(w_node, "Scan");
    for( Node s_node : s_nodes) {
        Scan read = Scan.fromXML(this,s_node);
        theScanList.add(read);
        addListeners(read);
    }

    currentScan = getFirstScan(); 

    // set glycan document
    Node gd_node = XMLUtils.findChild(w_node,"Structures");
    if( gd_node!=null )
        currentScan.getStructures().fromXML(gd_node,merge);    
    
    }
        

    public Element toXML(Document document) {
    if( document==null )
        return null;
    
    // create root node
    Element w_node = document.createElement("GlycanWorkspace");

    // create configuration node
    storeToConfiguration(true);
    w_node.appendChild(theConfiguration.toXML(document));

    // create scan node
    for(Scan s : theScanList) 
        w_node.appendChild(s.toXML(document));
    
    return w_node;
    }   

  public static class SAXHandler extends SAXUtils.ObjectTreeHandler {
    
    private GlycanWorkspace theDocument;
    private boolean merge;
    
    public SAXHandler(GlycanWorkspace _doc, boolean _merge) {
        theDocument = _doc;
        merge = _merge;
    }

    public boolean isElement(String namespaceURI, String localName, String qName) {
        return qName.equals(getNodeElementName());
    }

    public static String getNodeElementName() {
        return "GlycanWorkspace";
    }

    protected SAXUtils.ObjectTreeHandler getHandler(String namespaceURI, String localName, String qName) {
        if( qName.equals(GlycanDocument.SAXHandler.getNodeElementName()) )
        return new GlycanDocument.SAXHandler(new GlycanDocument(theDocument),merge);
        if( qName.equals(Scan.SAXHandler.getNodeElementName()) )
        return new Scan.SAXHandler(theDocument);
        return null;
    }

    protected Object finalizeContent(String namespaceURI, String localName, String qName) throws SAXException{
        // clear
        if( !merge ) {
        // clear changed status
        theDocument.resetStatus();
        
        // remove listeners
        for( Scan s : theDocument.theScanList )
            theDocument.removeListeners(s);
        
        // init scans
        theDocument.theScanList = new Vector<Scan>();
        }
        else {
        // set changed status
        theDocument.setChanged(true);
        }
        
        // set scan list
        for( Object o : getSubObjects(Scan.SAXHandler.getNodeElementName()) ) {
        theDocument.theScanList.add((Scan)o);        
        theDocument.addListeners((Scan)o);
        }
        theDocument.currentScan = theDocument.getFirstScan(); 

        // set glycan document
        GlycanDocument gd = (GlycanDocument)getSubObject(GlycanDocument.SAXHandler.getNodeElementName(),false);
        if( gd!=null )
        theDocument.currentScan.getStructures().setStructures(gd.getStructures());

        return (object = theDocument);
    }
    }

    public void write(TransformerHandler th) throws SAXException {
    th.startElement("","","GlycanWorkspace",new AttributesImpl());

    // add configuration 
    storeToConfiguration(true);
    theConfiguration.write(th);

    // add scans
    for(Scan s : theScanList) 
        s.write(th);

    th.endElement("","","GlycanWorkspace");
    }

    
}
