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


package org.eurocarbdb.application.glycoworkbench.plugin.reporting;

import org.eurocarbdb.application.glycoworkbench.plugin.*;
import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.awt.geom.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.helpers.AttributesImpl;

public class AnnotationReportDocument extends BaseDocument implements SAXUtils.SAXWriter {    
    
    private static final int ANNOTATION = 1;
    private static final int ANCHOR     = 2;
    private static final int SCALE      = 3;
    private static final int CP         = 4;

    private MMFCreator mmfc = null;
    private BuilderWorkspace theWorkspace;
    private AnnotationReportOptions theOptions = null;
    private GraphicOptions theGraphicOptions = null;
  
    private boolean show_rel_int = false;
    private boolean show_empty_annotations = false;
    private double start_mz = 0;
    private double end_mz = 0;
    
    private PeakData thePeakData = null;
    private Glycan theParentStructure = null;
    private PeakAnnotationCollection thePeakAnnotationCollection = new PeakAnnotationCollection();    
 
    private Vector<AnnotationObject> annotations = new Vector<AnnotationObject>();
    private HashMap<AnnotationObject,Point2D> anchors = new HashMap<AnnotationObject,Point2D>();
    private HashMap<AnnotationObject,Point2D> control_points = new HashMap<AnnotationObject,Point2D>();
    private HashMap<AnnotationObject,Double> scales = new HashMap<AnnotationObject,Double>();

    public AnnotationReportDocument() { 

    theWorkspace = new BuilderWorkspace(null,false);
    
    theOptions = new AnnotationReportOptions();
    theGraphicOptions = theWorkspace.getGraphicOptions();

    theWorkspace.setDisplay(GraphicOptions.DISPLAY_COMPACT);    

    theGraphicOptions.SHOW_INFO = false;
    theGraphicOptions.SHOW_MASSES = false;
    theGraphicOptions.SHOW_REDEND = false;
    }
    
    public AnnotationReportDocument(double smz, double emz, PeakData pd, Glycan parent, PeakAnnotationCollection pac, AnnotationReportOptions opt, GraphicOptions gopt) {   
    this();

    assert pac!=null;
    
    theOptions = opt;

    show_rel_int = opt.SHOW_RELATIVE_INTENSITIES;
    show_empty_annotations = opt.SHOW_EMPTY_ANNOTATIONS;

    start_mz = smz;
    end_mz = emz;
    thePeakData = (opt.SHOW_RAW_SPECTRUM) ?pd :null;
    theParentStructure = parent;
    thePeakAnnotationCollection = pac;

    theWorkspace.setNotation(gopt.NOTATION);
    theGraphicOptions.ORIENTATION = (thePeakAnnotationCollection.isProfile()) ?GraphicOptions.BT :GraphicOptions.RL;

    initData();
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

    // member access
    public BuilderWorkspace getWorkspace() {
    return theWorkspace;
    }

    public AnnotationReportOptions getAnnotationReportOptions() {
    return theOptions;
    }

    public GraphicOptions getGraphicOptions() {
    return theGraphicOptions;
    }

  
    public int size() {
    return annotations.size();
    }

    public String getName() {
    return "Annotation report";
    }

    public javax.swing.ImageIcon getIcon() {
    return FileUtils.defaultThemeManager.getImageIcon("annrepdoc");
    }

    public Collection<javax.swing.filechooser.FileFilter> getFileFormats() {
    Vector<javax.swing.filechooser.FileFilter> filters = new Vector<javax.swing.filechooser.FileFilter>();
    
    filters.add(new ExtensionFileFilter("gwr", "Annotation report file"));
    return filters;
    }

    public javax.swing.filechooser.FileFilter getAllFileFormats() {
    return new ExtensionFileFilter("gwr", "Annotation report file");
    }

    public boolean isShowRelativeIntensities() {
    return show_rel_int;
    }

    public boolean isShowEmptyAnnotations() {
    return show_empty_annotations;
    }

    public double getStartMZ() {
    return start_mz;
    }

    public double getEndMZ() {
    return end_mz;
    }  
    
    public PeakData getPeakData() {
    return thePeakData;
    }

    public Glycan getParentStructure() {
    return theParentStructure;
    }

    public PeakAnnotationCollection getPeakAnnotationCollection() {
    return thePeakAnnotationCollection;
    }

    public Vector<AnnotationObject> getAnnotations() {
    return annotations;
    }

    public Vector<PeakAnnotation> getPeakAnnotations() {
    Vector<PeakAnnotation> ret = new Vector<PeakAnnotation>();
    for( AnnotationObject a : annotations) 
        ret.addAll(a.getPeakAnnotations());
    return ret;
    }

    public Point2D getAnchor(AnnotationObject a) {
    return anchors.get(a);
    }

    public HashMap<AnnotationObject,Point2D> getAnchors() {
    return anchors;
    }

    public Point2D getControlPoint(AnnotationObject a) {
    return control_points.get(a);
    }

    public HashMap<AnnotationObject,Point2D> getControlPoints() {
    return control_points;
    }

    public Double getScale(AnnotationObject a) {
    return scales.get(a);
    }

    public HashMap<AnnotationObject,Double> getScales() {
    return scales;
    }    

    public boolean isDisplayed(AnnotationObject a) {
    return annotations.contains(a);    
    }

    public double[][] getData(Rectangle2D data_area) {
    return getData(data_area,show_rel_int);
    }
         

    public double[][] getData(Rectangle2D data_area, boolean rel_int) {
    if( thePeakData!=null ) {
        if( start_mz!=end_mz ) {
        double mz_toll = (end_mz-start_mz)/data_area.getWidth()/2;    
        return thePeakData.getData(start_mz,end_mz,mz_toll,rel_int);      
        }
        else {
        double mz_toll = thePeakData.getMZRange().getLength()/data_area.getWidth()/2;
        return thePeakData.getData(mz_toll,rel_int);      
        }
    }
    else {
        if( start_mz!=end_mz )
        return thePeakAnnotationCollection.getPeakData(start_mz,end_mz,1.,rel_int);
        else
        return thePeakAnnotationCollection.getPeakData(1.,rel_int);
    }
    }

    // actions

    private Peak findNearestPeak(TreeMap<Double,Double> theData, double mz, double mz_toll) {    
    
    double max_mz = mz;
    double max_int = 0.;
    
    Object[] before = theData.subMap(mz-mz_toll,mz).entrySet().toArray();
    if( before!=null ) {
        for( int i=before.length-1; i>=0; i-- ) {
        Map.Entry<Double,Double> e = (Map.Entry<Double,Double>)before[i];
        if( e.getValue()<max_int )
            break;
        max_mz = e.getKey();
        max_int = e.getValue();
        }
    }        

    Object[] after = theData.subMap(mz,mz+mz_toll).entrySet().toArray();
    if( after!=null ) {
        for( int i=0; i<after.length; i++ ) {
        Map.Entry<Double,Double> e = (Map.Entry<Double,Double>)after[i];       
        if( e.getValue()<max_int )
            break;
        max_mz = e.getKey();
        max_int = e.getValue();
        }
    }
        
    return new Peak(max_mz,max_int);
    }


    public void initData() {
    if( annotations==null )
        return;

    annotations.clear();
    anchors.clear();
    control_points.clear();
    scales.clear();

    // update visible data
    double max_int = 0.;
    TreeMap<Double,Double> visibleData = new TreeMap<Double,Double>();
    if( thePeakData!=null ) {
        double[][] data;
        if( start_mz!=end_mz ) 
        data = thePeakData.getData(start_mz,end_mz,0.1);      
        else 
        data = thePeakData.getData(0.1,show_rel_int);      
        
        for( int i=0; i<data[0].length; i++ ) {
        visibleData.put(data[0][i],data[1][i]);
        max_int = Math.max(max_int,data[1][i]);
        }
    }
    else {
        if( start_mz!=end_mz )
        max_int = thePeakAnnotationCollection.getMaxIntensity(start_mz,end_mz);
        else
        max_int = thePeakAnnotationCollection.getMaxIntensity();
    }
    
    // add annotations
    AnnotationObject last = null;
    for( PeakAnnotation pa : thePeakAnnotationCollection.getPeakAnnotations() ) {
        if( show_empty_annotations || pa.isAnnotated() ) {
        Peak p = pa.getPeak();        
        if( start_mz==end_mz || p.getMZ()>=start_mz && p.getMZ()<=end_mz ) {
            // get peak point         
            if( thePeakData!=null ) 
            p = findNearestPeak(visibleData,p.getMZ(),1.);
            double x = p.getMZ();
            double y = (show_rel_int) ?100.*p.getIntensity()/max_int :p.getIntensity();

            // create annotation object
            AnnotationObject a = new AnnotationObject(x,y,pa);
            if( last!=null && last.canGroup(a) )
            last.group(a);
            else {
            annotations.add(a);
            anchors.put(a,new Point2D.Double(x,y));
            control_points.put(a,new Point2D.Double(x,y));
            scales.put(a,1.);
            last = a;
            }
        }
        }
    }

    fireDocumentInit();
    }      

    /*
    public boolean contains(Collection<PeakAnnotation> annotations, PeakAnnotation pa) {
    for( PeakAnnotation a : annotations ) {
        if( a.getPeak().equals(pa.getPeak()) ) {
        if( !a.equals(pa) ) {
            System.out.println(a + " <> " + pa);
            if( !a.getAnnotation().getFragmentEntry().equals(pa.getAnnotation().getFragmentEntry()) ) {
            System.out.println("different fragmententry");
            FragmentEntry fa = a.getAnnotation().getFragmentEntry();
            FragmentEntry fb = pa.getAnnotation().getFragmentEntry();
            if( !fa.name.equals(fb.name) )
                System.out.println( "\t" + fa.name + " <> " + fb.name);
            if( !fa.mass.equals(fb.mass) )
                System.out.println( "\t" + fa.mass + " <> " + fb.mass);
            if( !fa.fragment.equalsStructure(fb.fragment) )
                System.out.println( "\t" + fa.fragment + " <> " + fb.fragment);
            }
            if( !a.getAnnotation().getIons().equals(pa.getAnnotation().getIons()) )
            System.out.println("different ions");
            if( !a.getAnnotation().getNeutralExchanges().equals(pa.getAnnotation().getNeutralExchanges()) )
            System.out.println("different exchanges");
        }
        else
            return true;
        }
    }
    return false;
    }
    */

    public boolean updateData(Glycan parent, PeakAnnotationCollection pac, Collection<AnnotationObject> added, boolean fire, boolean merge) {

    // update objects
    theParentStructure = parent;
    thePeakAnnotationCollection = pac;

    // refresh visible data
    double max_int = 0.;
    TreeMap<Double,Double> visibleData = new TreeMap<Double,Double>();
    if( thePeakData!=null ) {
        double[][] data;
        if( start_mz!=end_mz ) 
        data = thePeakData.getData(start_mz,end_mz,0.1);      
        else 
        data = thePeakData.getData(0.1,show_rel_int);      
        
        for( int i=0; i<data[0].length; i++ ) {
        visibleData.put(data[0][i],data[1][i]);
        max_int = Math.max(max_int,data[1][i]);
        }
    }
    else {
        if( start_mz!=end_mz )
        max_int = thePeakAnnotationCollection.getMaxIntensity(start_mz,end_mz);
        else
        max_int = thePeakAnnotationCollection.getMaxIntensity();
    }

    // remove non existing annotations
    boolean removed = false;
    if( !merge ) {
        for( int i=0; i<annotations.size(); i++ ) {
        AnnotationObject a = annotations.get(i);
        
        // check if peak annotations are still existing
        for( int l=0; l<a.getPeakAnnotations().size(); l++ ) {
            PeakAnnotation pa = a.getPeakAnnotations().get(l);
            if( !thePeakAnnotationCollection.getPeakAnnotations().contains(pa) ) {
            //System.out.println("removing " + i + " " + l);
            removed = true;
            a.remove(pa);
            l--;
            }
        }
        
        // if not remove the annotation object
        if( a.size()==0 ) {
            //System.out.println("removing " + i);
            removed = true;            
            annotations.removeElementAt(i);
            anchors.remove(a);
            control_points.remove(a);
            scales.remove(a);        
            i--;
        }
        }    
    }

    // add new annotations
    AnnotationObject last = null;
    for( PeakAnnotation pa : thePeakAnnotationCollection.getPeakAnnotations() ) {
        if( getAnnotation(pa)==null && (show_empty_annotations || pa.isAnnotated()) ) {
        Peak p = pa.getPeak();        
        if( start_mz==end_mz || p.getMZ()>=start_mz && p.getMZ()<=end_mz ) {
            // get peak point         
            if( thePeakData!=null ) 
            p = findNearestPeak(visibleData,p.getMZ(),1.);
            double x = p.getMZ();
            double y = (show_rel_int) ?100.*p.getIntensity()/max_int :p.getIntensity();

            // create annotation object
            AnnotationObject a = new AnnotationObject(x,y,pa);
            if( last!=null && last.canGroup(a) )
            last.group(a);
            else {
            added.add(a);
            annotations.add(a);
            anchors.put(a,new Point2D.Double(x,y));
            control_points.put(a,new Point2D.Double(x,y));
            scales.put(a,1.);
            last = a;
            }            
        }
        }
    }

    boolean changed = (removed || (added.size()>0));
    if( changed && fire ) 
        fireDocumentChanged();
    return changed;
    }

    public AnnotationObject getAnnotation(PeakAnnotation pa) {
    for( AnnotationObject a : annotations ) {
        if( a.getPeakAnnotations().contains(pa) )
        return a;
    }
    return null;        
    }
    
    public void remove(AnnotationObject a) {
    if( a.size()==0 || !show_empty_annotations ) {
        annotations.remove(a);
        anchors.remove(a);
        control_points.remove(a);
        scales.remove(a);
    }
    else 
        a.clear();

    fireDocumentChanged();
    }

    public void remove(Collection<AnnotationObject> toremove) {
    for( AnnotationObject a : toremove ) {
        if( a.size()==0 || !show_empty_annotations ) {
        annotations.remove(a);
        anchors.remove(a);
        control_points.remove(a);
        scales.remove(a);
        }
        else 
        a.clear();
    }
    fireDocumentChanged();
    }

    public Point2D computeControlPoint(Point2D anchor, double mz, double intensity) {
    return computeControlPoint(anchor,new Point2D.Double(mz,intensity));
    }
    
    public Point2D computeControlPoint(Point2D anchor, Point2D peak) {
    if( anchor.getY()<=peak.getY() ) 
        return new Point2D.Double((anchor.getX()+peak.getX())/2,(anchor.getY()+peak.getY())/2);
    return new Point2D.Double(peak.getX(),(anchor.getY()+peak.getY())/2);
    }

    public void move(Collection<AnnotationObject> tomove, double ddx, double ddy) {
    for( AnnotationObject a : tomove ) {
        Point2D anchor = anchors.get(a);
        if( anchor!=null ) {
        anchor.setLocation(anchor.getX()+ddx,anchor.getY()-ddy);
        control_points.put(a,computeControlPoint(anchor,a.getPeakPoint()));
        }
    }
    fireDocumentChanged();
    }


    public void move(AnnotationObject tomove, double ddx, double ddy) {
    Point2D anchor = anchors.get(tomove);
    if( anchor!=null ) {
        anchor.setLocation(anchor.getX()+ddx,anchor.getY()-ddy);
        control_points.put(tomove,computeControlPoint(anchor,tomove.getPeakPoint()));
    
        fireDocumentChanged();
    }
    }


    public void moveTo(AnnotationObject tomove, double x, double y) {
    Point2D anchor = anchors.get(tomove);
    if( anchor!=null ) {
        anchor.setLocation(x,y);
        control_points.put(tomove,computeControlPoint(anchor,tomove.getPeakPoint()));
        
        fireDocumentChanged();
    }
    }

    public void moveControlPointTo(AnnotationObject tomove, double x, double y) {
    Point2D cp = control_points.get(tomove);
    if( cp!=null ) {
        cp.setLocation(x,y);
     
        fireDocumentChanged();
    }
    }

    public void rescale(Collection<AnnotationObject> toscale, double factor) {
    for( AnnotationObject a : toscale ) 
        scales.put(a,scales.get(a)*factor);
    fireDocumentChanged();
    }

    public void resetScale(Collection<AnnotationObject> toscale) {
    for( AnnotationObject a : toscale ) 
        scales.put(a,1.);
    fireDocumentChanged();
    }

    public void setHighlighted(Collection<AnnotationObject> toset, boolean highlight) {
    for( AnnotationObject a : toset ) 
        a.setHighlighted(highlight);
    fireDocumentChanged();
    }

    public boolean canGroup(Collection<AnnotationObject> togroup) {
    if( togroup==null || togroup.size()<2 )
        return false;
    
    AnnotationObject first = togroup.iterator().next();
    for( AnnotationObject a : togroup ) {
        if( a!=first && !first.canGroup(a) )
        return false;
    }
    return true;
    }

    public void group(Collection<AnnotationObject> togroup) {
    if( canGroup(togroup) ) {
        AnnotationObject grouped = togroup.iterator().next();
        for( AnnotationObject a : togroup ) {
        if( a!=grouped ) {
            annotations.remove(a);
            anchors.remove(a);
            control_points.remove(a);
            scales.remove(a);

            grouped.group(a);
        }
        }     
        fireDocumentChanged();
    }    
    }

    public void ungroup(Collection<AnnotationObject> toungroup, AnnotationReportCanvas theCanvas) {
    boolean changed = false;
    for( AnnotationObject grouped : toungroup ) {
        if( grouped.size()>1 ) {

        // compute new objects
        int ind = annotations.indexOf(grouped);

        Point2D anchor = anchors.get(grouped);
        Double scale = scales.get(grouped);
        for( AnnotationObject a : grouped.ungroup() ) {
            annotations.insertElementAt(a,ind++);

            anchors.put(a,anchor);
            control_points.put(a,computeControlPoint(anchor,a.getPeakPoint()));
            scales.put(a,scale);           

            anchor = new Point2D.Double(anchor.getX(), anchor.getY()+theCanvas.computeSizeData(a).getHeight());       
        }
        
        // remove old group
        annotations.remove(grouped);
        anchors.remove(grouped);
        control_points.remove(grouped);
        scales.remove(grouped);

        changed = true;
        }
    }
    if( changed )
        fireDocumentChanged();
    }

    // serialization

    public String toString() {    
    StringBuilder ret = new StringBuilder();

    if( annotations!=null ) {
        for( AnnotationObject a : annotations ) {
        ret.append(ANNOTATION);
        ret.append(' ');
        ret.append(a.isHighlighted());
        ret.append(' ');
        ret.append(a.getPeakPoint().getX());
        ret.append(' ');
        ret.append(a.getPeakPoint().getY());
        ret.append(' ');
        ret.append(a.getPeakAnnotations().size());
        for( PeakAnnotation pa : a.getPeakAnnotations() ) {
            ret.append(' ');
            ret.append(thePeakAnnotationCollection.indexOf(pa));
        }
        ret.append('\n');
        }
    }

    if( anchors!=null ) {
        for( Map.Entry<AnnotationObject,Point2D> e : anchors.entrySet()) {
        ret.append(ANCHOR);
        ret.append(' ');
        ret.append(annotations.indexOf(e.getKey()));
        ret.append(' ');
        ret.append(e.getValue().getX());
        ret.append(' ');
        ret.append(e.getValue().getY());
        ret.append('\n');
        }
    }

    if( control_points!=null ) {
        for( Map.Entry<AnnotationObject,Point2D> e : control_points.entrySet()) {
        ret.append(CP);
        ret.append(' ');
        ret.append(annotations.indexOf(e.getKey()));
        ret.append(' ');
        ret.append(e.getValue().getX());
        ret.append(' ');
        ret.append(e.getValue().getY());
        ret.append('\n');
        }
    }

    if( scales!=null ) {
        for( Map.Entry<AnnotationObject,Double> e : scales.entrySet()) {
        ret.append(SCALE);
        ret.append(' ');
        ret.append(annotations.indexOf(e.getKey()));
        ret.append(' ');
        ret.append(e.getValue());
        ret.append('\n');
        }
    }

    return ret.toString();
    }

    public void fromString(String str, boolean merge) throws Exception {    

    if( !merge ) {
        annotations.clear();
        anchors.clear();
        control_points.clear();
        scales.clear();
    }

    String line;
    BufferedReader br = new BufferedReader(new StringReader(str));
    while( (line=br.readLine())!=null ) {
        String[] tokens = line.split("\\s");
        int type = Integer.valueOf(tokens[0]);
        
        if( type==ANNOTATION ) {
        if( tokens.length<4 )
            throw new Exception("Invalid format for ANNOTATION line: " + line);

        int off = 0;
        boolean highlighted = false;
        if( tokens[1].equals("true") || tokens[1].equals("false") ) {
            highlighted = Boolean.valueOf(tokens[1]);
            off = 1;
        }
            
            
    
        double x = Double.valueOf(tokens[1+off]); 
        double y = Double.valueOf(tokens[2+off]);         
    
        int size = Integer.valueOf(tokens[3+off]);
        if( tokens.length!=(size+4+off) )
            throw new Exception("Invalid format for ANNOTATION line: " + line);

        AnnotationObject read = new AnnotationObject(x,y);
        read.setHighlighted(highlighted);
        for( int i=0; i<size; i++ ) {
            int ind = Integer.valueOf(tokens[i+4+off]);
            read.add(thePeakAnnotationCollection.getPeakAnnotation(ind));
        }
        annotations.add(read);
        }        
        else if( type==ANCHOR ) {
        if( tokens.length!=4 )
            throw new Exception("Invalid format for ANCHOR line: " + line);
        
        int ind = Integer.valueOf(tokens[1]);
        double x = Double.valueOf(tokens[2]); 
        double y = Double.valueOf(tokens[3]);         
        anchors.put(annotations.elementAt(ind),new Point2D.Double(x,y));        
        }
        else if( type==CP ) {
        if( tokens.length!=4 )
            throw new Exception("Invalid format for CP line: " + line);
    
        int ind = Integer.valueOf(tokens[1]);
        double x = Double.valueOf(tokens[2]); 
        double y = Double.valueOf(tokens[3]);         
        control_points.put(annotations.elementAt(ind),new Point2D.Double(x,y));
        }
        else if( type==SCALE ) {
        if( tokens.length!=3 )
            throw new Exception("Invalid format for SCALE line: " + line);

        int ind = Integer.valueOf(tokens[1]);
        double s = Double.valueOf(tokens[2]); 
        scales.put(annotations.elementAt(ind),s);
        }
        else
        throw new Exception("Invalid type: " + type);
    }
    }    

    public void fromXMLString(String src) throws Exception
    {
    ByteArrayInputStream bis = new ByteArrayInputStream(src.getBytes());
    read(bis,false);

    // 
    setFilename("");        
    fireDocumentInit();
    }  

    public void fromXMLString(byte[] src) throws Exception
    {
    ByteArrayInputStream bis = new ByteArrayInputStream(src);
    read(bis,false);

    // 
    setFilename("");        
    fireDocumentInit();
    }    

    public String toXMLString() {
    try{
        // open stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        write(baos);

        return baos.toString();
    }
    catch( Exception e ) {
        LogUtils.report(e);
        return "";
    }
    }
  
    public void read(InputStream is, boolean merge) throws Exception {
    /*Document document = XMLUtils.read(is);
    if( document==null ) 
        throw new Exception("Cannot read from file");
    
        fromXML(XMLUtils.assertChild(document,"AnnotationReportDocument"));    */
    SAXUtils.read(is,new SAXHandler(this));        
    }    

    
    public void write(OutputStream os) throws Exception {
    /*
    // create XML
    Document document = XMLUtils.newDocument();
    if( document==null )
    return "";

    Element ard_node = toXML(document);
    if( ard_node == null )
    return "";
    
    document.appendChild(ard_node);    
    
    // write XML
    XMLUtils.write(baos,document);
    */
    SAXUtils.write(os,this);
    }
    
   
    public void fromXML(Node ard_node) throws Exception {        
    if( ard_node==null )
        throw new Exception("empty node");
    
    show_rel_int = Boolean.valueOf(XMLUtils.getAttribute(ard_node,"show_relative_intensities"));
    show_empty_annotations = Boolean.valueOf(XMLUtils.getAttribute(ard_node,"show_empty_annotations"));
    start_mz = Double.valueOf(XMLUtils.getAttribute(ard_node,"start_mz"));
    end_mz = Double.valueOf(XMLUtils.getAttribute(ard_node,"end_mz"));

    // read settings
    Node c_node = XMLUtils.findChild(ard_node,"Configuration");
    if( c_node!=null ) {        
        Configuration config = new Configuration();
        config.fromXML(c_node);
        theOptions.retrieve(config);
        theGraphicOptions.retrieve(config);
    }

    // read peak data
    Node pd_node = XMLUtils.findChild(ard_node,"PeakData");
    if( pd_node!=null )
        thePeakData = PeakData.fromXML(pd_node,this.getMMFCreator());
    else
        thePeakData = null;

    // read peak annotations
    thePeakAnnotationCollection = PeakAnnotationCollection.fromXML(XMLUtils.assertChild(ard_node,"PeakAnnotationCollection"));

    // read graphic information
    annotations = new Vector<AnnotationObject>();
    for( Node node : XMLUtils.findAllChildren(ard_node, "Annotation") ) {
        double x = Double.valueOf(XMLUtils.getAttribute(node,"x")); 
        double y = Double.valueOf(XMLUtils.getAttribute(node,"y"));     
        int size = Integer.valueOf(XMLUtils.getAttribute(node,"size")); 

        AnnotationObject read = new AnnotationObject(x,y);
        for( int i=0; i<size; i++ ) {
        int ind = Integer.valueOf(XMLUtils.getAttribute(node,"pa"+i)); 
        read.add(thePeakAnnotationCollection.getPeakAnnotation(ind));
        }
        annotations.add(read);
    }        
    
    anchors = new HashMap<AnnotationObject,Point2D>();
    for( Node node : XMLUtils.findAllChildren(ard_node, "Anchor") ) {
        int ind = Integer.valueOf(XMLUtils.getAttribute(node,"ind")); 
        double x = Double.valueOf(XMLUtils.getAttribute(node,"x")); 
        double y = Double.valueOf(XMLUtils.getAttribute(node,"y"));     
        anchors.put(annotations.elementAt(ind),new Point2D.Double(x,y));        
    }

    control_points = new HashMap<AnnotationObject,Point2D>();
    for( Node node : XMLUtils.findAllChildren(ard_node, "ControlPoint") ) {
        int ind = Integer.valueOf(XMLUtils.getAttribute(node,"ind")); 
        double x = Double.valueOf(XMLUtils.getAttribute(node,"x")); 
        double y = Double.valueOf(XMLUtils.getAttribute(node,"y"));     
        control_points.put(annotations.elementAt(ind),new Point2D.Double(x,y));
    }
    
    scales = new HashMap<AnnotationObject,Double>();
    for( Node node : XMLUtils.findAllChildren(ard_node, "Scale") ) {
        int ind = Integer.valueOf(XMLUtils.getAttribute(node,"ind")); 
        double s = Double.valueOf(XMLUtils.getAttribute(node,"s")); 
        scales.put(annotations.elementAt(ind),s);    
    }
    }

    public Element toXML(Document document) {
    if( document==null )
        return null;

    // create root node
    Element ard_node = document.createElement("AnnotationReportDocument");

    // add settings
    Configuration config = new Configuration();
    theOptions.store(config);
    theGraphicOptions.store(config);
    ard_node.appendChild(config.toXML(document));

    ard_node.setAttribute("show_relative_intensities",""+show_rel_int);
    ard_node.setAttribute("show_empty_annotations",""+show_empty_annotations);
    ard_node.setAttribute("start_mz", ""+start_mz);
    ard_node.setAttribute("end_mz",""+end_mz);
    
    // add peak data
    if( thePeakData!=null )
        ard_node.appendChild(thePeakData.toXML(document));

    // add peak annotations
    ard_node.appendChild(thePeakAnnotationCollection.toXML(document));
    
    // add graphic information
    if( annotations!=null ) {
        for( AnnotationObject a : annotations ) {
        int i=0;
        Element node = document.createElement("Annotation");
        node.setAttribute("x",""+a.getPeakPoint().getX());
        node.setAttribute("y",""+a.getPeakPoint().getY());
        node.setAttribute("size",""+a.getPeakAnnotations().size());        
        for( PeakAnnotation pa : a.getPeakAnnotations() ) 
            node.setAttribute("pa"+(i++),""+thePeakAnnotationCollection.indexOf(pa));
        ard_node.appendChild(node);
        }
    }

    if( anchors!=null ) {
        for( Map.Entry<AnnotationObject,Point2D> e : anchors.entrySet()) {
        Element node = document.createElement("Anchor");
        node.setAttribute("ind",""+annotations.indexOf(e.getKey()));
        node.setAttribute("x",""+e.getValue().getX());
        node.setAttribute("y",""+e.getValue().getY());
        ard_node.appendChild(node);
        }
    }

    if( control_points!=null ) {
        for( Map.Entry<AnnotationObject,Point2D> e : control_points.entrySet()) {
        Element node = document.createElement("ControlPoint");
        node.setAttribute("ind",""+annotations.indexOf(e.getKey()));
        node.setAttribute("x",""+e.getValue().getX());
        node.setAttribute("y",""+e.getValue().getY());           
        ard_node.appendChild(node);
        }
    }

    if( scales!=null ) {
        for( Map.Entry<AnnotationObject,Double> e : scales.entrySet()) {
        Element node = document.createElement("Scale");
        node.setAttribute("ind",""+annotations.indexOf(e.getKey()));
        node.setAttribute("s",""+e.getValue());
        ard_node.appendChild(node);
        }
    }
    
    return ard_node;
    }
    
    public static class SAXHandler extends SAXUtils.ObjectTreeHandler {
    
    private AnnotationReportDocument theDocument;
    
    public SAXHandler(AnnotationReportDocument _doc) {
        theDocument = _doc;
    }

    public boolean isElement(String namespaceURI, String localName, String qName) {
        return qName.equals(getNodeElementName());
    }

    public static String getNodeElementName() {
        return "AnnotationReportDocument";
    }

    protected SAXUtils.ObjectTreeHandler getHandler(String namespaceURI, String localName, String qName) {
        if( qName.equals(Configuration.SAXHandler.getNodeElementName()) )
        return new Configuration.SAXHandler(new Configuration());
        if( qName.equals(Glycan.SAXHandler.getNodeElementName()) )
        return new Glycan.SAXHandler(theDocument.theWorkspace.getDefaultMassOptions());
        if( qName.equals(PeakAnnotationCollection.SAXHandler.getNodeElementName()) )
        return new PeakAnnotationCollection.SAXHandler();
        if( qName.equals(PeakData.SAXHandler.getNodeElementName()) )
        return new PeakData.SAXHandler(theDocument.getMMFCreator());
        if( qName.equals(AnnotationSAXHandler.getNodeElementName()) )
        return new AnnotationSAXHandler(theDocument.thePeakAnnotationCollection);
        if( qName.equals(AnchorSAXHandler.getNodeElementName()) )
        return new AnchorSAXHandler();
        if( qName.equals(ControlPointSAXHandler.getNodeElementName()) )
        return new ControlPointSAXHandler();
        if( qName.equals(ScaleSAXHandler.getNodeElementName()) )
        return new ScaleSAXHandler();
        return null;
    }
    
    protected void initContent(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        super.initContent(namespaceURI,localName,qName,atts);
    
        theDocument.show_rel_int = booleanAttribute(atts,"show_relative_intensities",false);
        theDocument.show_empty_annotations = booleanAttribute(atts,"show_empty_annotations",false);
        theDocument.start_mz = doubleAttribute(atts,"start_mz",0.);
        theDocument.end_mz = doubleAttribute(atts,"end_mz",0.);
    }
    
    protected Object finalizeContent(String namespaceURI, String localName, String qName) throws SAXException{
        
        // read configuration
        Configuration config = (Configuration)getSubObject(Configuration.SAXHandler.getNodeElementName(),false);
        if( config!=null ) {
        theDocument.theOptions.retrieve(config);
        theDocument.theGraphicOptions.retrieve(config);
        }

        // read peak data
        theDocument.thePeakData = (PeakData)getSubObject(PeakData.SAXHandler.getNodeElementName(),false);

        // read parent structure
        theDocument.theParentStructure = (Glycan)getSubObject(Glycan.SAXHandler.getNodeElementName(),false);

        // read peak annotations
        theDocument.thePeakAnnotationCollection = (PeakAnnotationCollection)getSubObject(PeakAnnotationCollection.SAXHandler.getNodeElementName(),true);
        
        // graphic objects
        theDocument.annotations = new Vector<AnnotationObject>();
        for( Object o : getSubObjects(AnnotationSAXHandler.getNodeElementName()) ) {
        Pair<AnnotationObject,int[]> annotation = (Pair<AnnotationObject,int[]>)o;
        for( int ind : annotation.getSecond() )
            annotation.getFirst().add(theDocument.thePeakAnnotationCollection.getPeakAnnotation(ind));       
        theDocument.annotations.add(annotation.getFirst());    
        }

        theDocument.anchors = new HashMap<AnnotationObject,Point2D>();
        for( Object o : getSubObjects(AnchorSAXHandler.getNodeElementName()) ) {          
        Pair<Integer,Point2D> anchor = (Pair<Integer,Point2D>)o;       
        theDocument.anchors.put(theDocument.annotations.elementAt(anchor.getFirst()),anchor.getSecond());
        }

        theDocument.control_points = new HashMap<AnnotationObject,Point2D>();
        for( Object o : getSubObjects(ControlPointSAXHandler.getNodeElementName()) ) {          
        Pair<Integer,Point2D> control_point = (Pair<Integer,Point2D>)o;       
        theDocument.control_points.put(theDocument.annotations.elementAt(control_point.getFirst()),control_point.getSecond());
        }

        theDocument.scales = new HashMap<AnnotationObject,Double>();
        for( Object o : getSubObjects(ScaleSAXHandler.getNodeElementName()) ) {          
        Pair<Integer,Double> scale = (Pair<Integer,Double>)o;       
        theDocument.scales.put(theDocument.annotations.elementAt(scale.getFirst()),scale.getSecond());
        }
        
        return (object = theDocument);
    }
    }   

    public static class AnnotationSAXHandler extends SAXUtils.ObjectTreeHandler {
    
    private PeakAnnotationCollection thePeakAnnotationCollection;
    
    public AnnotationSAXHandler(PeakAnnotationCollection pac) {
        thePeakAnnotationCollection = pac;
    }

    public boolean isElement(String namespaceURI, String localName, String qName) {
        return qName.equals(getNodeElementName());
    }

    public static String getNodeElementName() {
        return "Annotation";
    }

    protected void initContent(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        super.initContent(namespaceURI,localName,qName,atts);

        double x = doubleAttribute(atts,"x",0.); 
        double y = doubleAttribute(atts,"y",0.);     
        boolean h = booleanAttribute(atts,"highlighted",false);

        AnnotationObject read = new AnnotationObject(x,y);
        read.setHighlighted(h);

        int[] inds = new int[integerAttribute(atts,"size",0)]; 
        for( int i=0; i<inds.length; i++ ) 
        inds[i] = integerAttribute(atts,"pa"+i,-1);      
        
        object = new Pair<AnnotationObject,int[]>(read,inds);
    }
    }

    public static class AnchorSAXHandler extends SAXUtils.ObjectTreeHandler {    

    public boolean isElement(String namespaceURI, String localName, String qName) {
        return qName.equals(getNodeElementName());
    }

    public static String getNodeElementName() {
        return "Anchor";
    }

    protected void initContent(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        super.initContent(namespaceURI,localName,qName,atts);

        int ind = integerAttribute(atts,"ind",-1); 
        double x = doubleAttribute(atts,"x",0.); 
        double y = doubleAttribute(atts,"y",0.);     

        object = new Pair<Integer,Point2D>(ind,new Point2D.Double(x,y));
    }
    }

    public static class ControlPointSAXHandler extends SAXUtils.ObjectTreeHandler {    

    public boolean isElement(String namespaceURI, String localName, String qName) {
        return qName.equals(getNodeElementName());
    }

    public static String getNodeElementName() {
        return "ControlPoint";
    }

    protected void initContent(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        super.initContent(namespaceURI,localName,qName,atts);

        int ind = integerAttribute(atts,"ind",-1); 
        double x = doubleAttribute(atts,"x",0.); 
        double y = doubleAttribute(atts,"y",0.);     

        object = new Pair<Integer,Point2D>(ind,new Point2D.Double(x,y));
    }
    }

    public static class ScaleSAXHandler extends SAXUtils.ObjectTreeHandler {    

    public boolean isElement(String namespaceURI, String localName, String qName) {
        return qName.equals(getNodeElementName());
    }

    public static String getNodeElementName() {
        return "Scale";
    }

    protected void initContent(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        super.initContent(namespaceURI,localName,qName,atts);

        int ind = integerAttribute(atts,"ind",-1); 
        double s = doubleAttribute(atts,"s",0.); 
        
        object = new Pair<Integer,Double>(ind,s);
    }
    }

    public void write(TransformerHandler th) throws SAXException {

    // create root node
    AttributesImpl atts = new AttributesImpl();
    atts.addAttribute("","","show_relative_intensities","CDATA",""+show_rel_int);
    atts.addAttribute("","","show_empty_annotations","CDATA",""+show_empty_annotations);
    atts.addAttribute("","","start_mz","CDATA", ""+start_mz);
    atts.addAttribute("","","end_mz","CDATA",""+end_mz);
    th.startElement("","","AnnotationReportDocument",atts);
    
    // add settings
    Configuration config = new Configuration();
    theOptions.store(config);
    theGraphicOptions.store(config);
    config.write(th);

    // add peak data
    if( thePeakData!=null )
        thePeakData.write(th);

    // add parent structure
    if( theParentStructure!=null )
        theParentStructure.write(th);

    // add peak annotations
    thePeakAnnotationCollection.write(th);
    
    // add graphic information
    if( annotations!=null ) {
        for( AnnotationObject a : annotations ) {
        int i=0;
        AttributesImpl aatts = new AttributesImpl();
        aatts.addAttribute("","","x","CDATA",""+a.getPeakPoint().getX());
        aatts.addAttribute("","","y","CDATA",""+a.getPeakPoint().getY());
        aatts.addAttribute("","","highlighted","CDATA",""+a.isHighlighted());
        aatts.addAttribute("","","size","CDATA",""+a.getPeakAnnotations().size());        
        for( PeakAnnotation pa : a.getPeakAnnotations() ) 
            aatts.addAttribute("","","pa"+(i++),"CDATA",""+thePeakAnnotationCollection.indexOf(pa));

        th.startElement("","","Annotation",aatts);
        th.endElement("","","Annotation");
        }
    }

    if( anchors!=null ) {
        for( Map.Entry<AnnotationObject,Point2D> e : anchors.entrySet()) {
        AttributesImpl aatts = new AttributesImpl();
        aatts.addAttribute("","","ind","CDATA",""+annotations.indexOf(e.getKey()));
        aatts.addAttribute("","","x","CDATA",""+e.getValue().getX());
        aatts.addAttribute("","","y","CDATA",""+e.getValue().getY());
        
        th.startElement("","","Anchor",aatts);
        th.endElement("","","Anchor");
        }
    }

    if( control_points!=null ) {
        for( Map.Entry<AnnotationObject,Point2D> e : control_points.entrySet()) {
        AttributesImpl cpatts = new AttributesImpl();
        cpatts.addAttribute("","","ind","CDATA",""+annotations.indexOf(e.getKey()));
        cpatts.addAttribute("","","x","CDATA",""+e.getValue().getX());
        cpatts.addAttribute("","","y","CDATA",""+e.getValue().getY());
        
        th.startElement("","","ControlPoint",cpatts);
        th.endElement("","","ControlPoint");
        }
    }

    if( scales!=null ) {
        for( Map.Entry<AnnotationObject,Double> e : scales.entrySet()) {
        AttributesImpl satts = new AttributesImpl();
        satts.addAttribute("","","ind","CDATA",""+annotations.indexOf(e.getKey()));
        satts.addAttribute("","","s","CDATA",""+e.getValue());
        
        th.startElement("","","Scale",satts);
        th.endElement("","","Scale");
        }
    }
    
    th.endElement("","","AnnotationReportDocument");

    }

}
