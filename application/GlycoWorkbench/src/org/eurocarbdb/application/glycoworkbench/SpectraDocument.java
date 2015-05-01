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

import org.eurocarbdb.application.glycanbuilder.*;

import java.io.*;
import java.util.*;
import org.jfree.data.Range;
import org.systemsbiology.jrap.MSXMLParser;
import org.proteomecommons.io.PeakList;
import org.proteomecommons.io.PeakListReader;
import org.proteomecommons.io.GenericPeakListReader;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.helpers.AttributesImpl;

public class SpectraDocument extends BaseDocument implements SAXUtils.SAXWriter {    

    private MMFCreator mmfc;
    protected Vector<ScanData> theScans = new Vector<ScanData>();
    protected Vector<PeakData> thePeaks = new Vector<PeakData>();

    public SpectraDocument() {
    super(false);
    }

    private MMFCreator getMMFCreator() {
    try {
        if( mmfc==null )
        mmfc = new MMFCreator();
        return mmfc;         
    }
    catch(Exception e) {
        LogUtils.report(e);
        return null;
    }
    }

    public String getName() {
    return "Spectra";
    }

    public javax.swing.ImageIcon getIcon() {
    return FileUtils.defaultThemeManager.getImageIcon("spectradoc");
    }


    public Collection<javax.swing.filechooser.FileFilter> getFileFormats() {
    Vector<javax.swing.filechooser.FileFilter> filters = new Vector<javax.swing.filechooser.FileFilter>();
    
    filters.add(new ExtensionFileFilter(new String[] {"mzdata" , "xml"}, "mzData spectra file"));
    filters.add(new ExtensionFileFilter(new String[] {"mzxml" , "xml"}, "mzXML spectra file"));
    filters.add(new ExtensionFileFilter(new String[] {"t2d"}, "ABI 4000 series spectra file"));
    filters.add(new ExtensionFileFilter(new String[] {"txt"}, "ASCII spectra file"));
    //filters.add(new ExtensionFileFilter(new String[] {"dat"}, "MassLynx spectra file"));
    filters.add(new ExtensionFileFilter(new String[] {"mzdata", "mzxml", "xml", "t2d", "txt"}, "All spectra files"));
    
    return filters;
    }

    public javax.swing.filechooser.FileFilter getAllFileFormats() {
    return new ExtensionFileFilter(new String[] {"mzdata", "mzxml", "xml", "t2d", "txt"}, "Spectra files");
    }

    public boolean open(String filename, boolean merge, boolean warning) {    
    try {                
        if( !readSpectra(filename,warning) ) 
        return false;

        setFilename(filename);
        fireDocumentInit();
        return true;
    }
    catch(Exception e) {
        LogUtils.report(e);
        return false;
    }
    }    
    
    protected void read(InputStream is, boolean merge) throws Exception {
    throw new Exception("Unsupported");
    }

    private boolean readSpectra(String filename, boolean warning) throws Exception {
    // try first to read a CSV file
    List<Peak> peaks = readCSV(filename);
    if( peaks.size()>0 ) {
        clear();
        PeakData pd = new PeakData(peaks,this.getMMFCreator());
        if( pd.getMaxMZ()>pd.getMinMZ() ) {
        ScanData sd = new ScanData(1);
        theScans.add(sd);
        thePeaks.add(pd);
        }
        return true;
    }    

    // try with JRAP
    try {
        MSXMLParser parser = new MSXMLParser(filename);        
        if( parser.getScanCount()>0 ) {
        clear();
        for( int i=1; i<=parser.getScanCount(); i++ ) {
            org.systemsbiology.jrap.Scan toadd = parser.rap(i);
            ScanData sd = new ScanData(toadd);
            PeakData pd = new PeakData(toadd,this.getMMFCreator());            
            if( pd.getMaxMZ()>pd.getMinMZ() ) {
            //System.out.println("scan " + sd.getNum() + "->" + sd.getParentNum() + " ms" + sd.getMSLevel() + " prec mz=" + sd.getPrecursorMZ() + " rt=" + sd.getRetentionTime() + " tic=" + sd.getTotalIonCurrent());
            theScans.add(sd);
            thePeaks.add(pd);
            }            
        }
        return true;
        }
    }
    catch(Throwable t) {
    }    

    PeakListReader plr = GenericPeakListReader.getPeakListReader(filename);
    if( plr==null ) {
        if( warning )
        throw new Exception("Spectra file format not recognized");
        return false;
    }
    
    // use proteomecommons-io
    clear();
    for(int i=1;;) {
        PeakList p = plr.getPeakList();
        if( p==null )
        break;

        PeakData pd = new PeakData(p,this.getMMFCreator());
        if( pd.getMaxMZ()>pd.getMinMZ() ) {
        ScanData sd = new ScanData(i++,p);
        theScans.add(sd);
        thePeaks.add(pd);
        }
    }

    return true;        
    }
    
    private List<Peak> readCSV(String filename) throws Exception {
    System.out.println("reading csv" );
    try{
        List<Peak> ret = new Vector<Peak>();
        FileInputStream fis = new FileInputStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String line = "";
        boolean header = true;
        for( int i=0; (line = br.readLine())!=null; i++ ) {
        if( line.length()==0 )
            continue;

        Peak p = Peak.parseString(line);
        if( p==null && !header ) {
            System.out.println("wrong format in line " + i);
            return Collections.emptyList();
        }
        else if( p!=null ) {
            if( header )
            System.out.println("first peak at line " + i);
            header = false;
            ret.add(p);
        }
        if( header && i>10 ) {
            System.out.println("too many lines of header");
            return Collections.emptyList();
        }
        }
        System.out.println("read " + ret.size());
        return ret;
    }
    catch(Exception e) {
        System.out.println("exception " + e.getMessage());    
        return Collections.emptyList();
    }
    }
    
    public boolean save(String filename) {
    return false;    
    }

    public void fromString(String str, boolean merge) {    
    }

    public String toString() {
    return "";
    }

    public int size() {
    return getNoScans();
    }

    public Vector<ScanData> getScans() {
    return theScans;
    }

    public Vector<PeakData> getPeaks() {
    return thePeaks;
    }

    public ScanData getScanDataAt(int ind) {
    return theScans.elementAt(ind);
    }
 
    public PeakData getPeakDataAt(int ind) {
    return thePeaks.elementAt(ind);
    }

    public void initData() {
    theScans = new Vector<ScanData>();
    thePeaks = new Vector<PeakData>();
   }   

    public void removeScan(int ind) {
    theScans.removeElementAt(ind);
    thePeaks.removeElementAt(ind);
    fireDocumentChanged();
    }

    public int getNoScans() {
    if( theScans==null )
        return 0;
    return theScans.size();
    }

    public int getScanNum(int ind) {
    return theScans.elementAt(ind).getNum();
    }   

    public void baselineCorrection(int ind) {
    thePeaks.elementAt(ind).baselineCorrection();
    fireDocumentChanged();
    }

    public void recalibrate(int ind, List<Double> params ) {
    thePeaks.elementAt(ind).recalibrate(params);
    fireDocumentChanged();
    }

    public void noiseFilter(int ind) {
    thePeaks.elementAt(ind).noiseFilter();
    fireDocumentChanged();
    }

    public void fromXML(Node sd_node, boolean merge) throws Exception {
    // clear
    if( !merge ) {
        resetStatus();
        initData(); 
    }
    else {
        setChanged(true);
    }

    // get last scan num
    int num = (merge && theScans.size()>0) ?(theScans.lastElement().getNum() + 1) :0;
    
    // read scans
    Vector<Node> s_nodes = XMLUtils.findAllChildren(sd_node, "ScanData");
    for( Node s_node : s_nodes) {
        ScanData s = ScanData.fromXML(s_node);
        theScans.add(s);
        if( merge )
        s.setNum(num++);
    }


    // read peaks
    Vector<Node> p_nodes = XMLUtils.findAllChildren(sd_node, "PeakData");
    for( Node p_node : p_nodes) 
        thePeaks.add(PeakData.fromXML(p_node,this.getMMFCreator()));
    }

    public Element toXML(Document document) {
    if( document==null )
        return null;
    
    // create root node
    Element sd_node = document.createElement("SpectraDocument");
    if( sd_node==null )
        return null;

    // add scan objects
    for( ScanData s : theScans  ) 
        sd_node.appendChild(s.toXML(document));

    // add peak objects
    for( PeakData p : thePeaks  ) 
        sd_node.appendChild(p.toXML(document));
    
    return sd_node;
    }

    public static class SAXHandler extends SAXUtils.ObjectTreeHandler {
    
    private SpectraDocument theDocument;
    private boolean merge;
    
    public SAXHandler(SpectraDocument _doc, boolean _merge) {
        theDocument = _doc;
        merge = _merge;
    }

    public boolean isElement(String namespaceURI, String localName, String qName) {
        return qName.equals(getNodeElementName());
    }

    public static String getNodeElementName() {
        return "SpectraDocument";
    }

    protected SAXUtils.ObjectTreeHandler getHandler(String namespaceURI, String localName, String qName) {
        if( qName.equals(ScanData.SAXHandler.getNodeElementName()) )
        return new ScanData.SAXHandler();
        if( qName.equals(PeakData.SAXHandler.getNodeElementName()) )
        return new PeakData.SAXHandler(theDocument.getMMFCreator());
        return null;
    }

    protected Object finalizeContent(String namespaceURI, String localName, String qName) throws SAXException{
        // clear
        if( !merge ) {
        theDocument.resetStatus();
        theDocument.initData(); 
        }
        else
        theDocument.setChanged(true);
        
        // get last scan num
        int num = (merge && theDocument.theScans.size()>0) ?(theDocument.theScans.lastElement().getNum() + 1) :0;
    
        // read scans
        for(Object o : getSubObjects(ScanData.SAXHandler.getNodeElementName())) {        
        theDocument.theScans.add((ScanData)o);
        if( merge )
            ((ScanData)o).setNum(num++);
        }

        // read peaks
        for(Object o : getSubObjects(PeakData.SAXHandler.getNodeElementName()))         
        theDocument.thePeaks.add((PeakData)o);
        
        return (object = theDocument);
    }
    }

    public void write(TransformerHandler th) throws SAXException {
    th.startElement("","","SpectraDocument",new AttributesImpl());

    for(ScanData s : theScans ) 
        s.write(th);
    for(PeakData p : thePeaks ) 
        p.write(th);

    th.endElement("","","SpectraDocument");
    }
}
