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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
package org.eurocarbdb.application.glycoworkbench;

import org.eurocarbdb.application.glycanbuilder.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.TreeSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
   Contain a list of annotated peaks in a mass spectrum, where the
   annotations derive from a single structure (intact or fragment
   molecule)

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/


public class PeakAnnotationCollection implements SAXUtils.SAXWriter {    

    private double max_intensity = 0.;
    private Vector<PeakAnnotation> peak_annotations;

    /**
       Create an empty list of peak annotations
     */
    public PeakAnnotationCollection() {
    max_intensity = 0.;
    peak_annotations = new Vector<PeakAnnotation>();
    }

    /**
       Create a copy of the current object
     */
    public PeakAnnotationCollection clone() {
    PeakAnnotationCollection ret = new PeakAnnotationCollection();
    for( PeakAnnotation pa : peak_annotations )
        ret.addPeakAnnotation(pa);
    return ret;
    }

    /**
       Remove all peak annotations from the list
     */
    public void clear() {
    max_intensity = 0.;
    peak_annotations.clear();
    }

    /**
       Remove all annotations for a specific peak, leave the peak in
       the list
     */
    public void clearAnnotations(Peak p) {
    if( p==null )
        return;
    
    for( Iterator<PeakAnnotation> i=peak_annotations.iterator(); i.hasNext(); ) {
        PeakAnnotation pa = i.next();
        if( p.equals(pa.getPeak()) ) 
        i.remove();        
    }

    addPeakAnnotation(p);
    }


    /**
       Remove all annotations for a collection of peaks
     */
    public void clearAnnotations(Collection<Peak> peaks) {

    TreeSet<Peak> removed = new TreeSet<Peak>();
    for( Iterator<PeakAnnotation> i=peak_annotations.iterator(); i.hasNext(); ) {
        PeakAnnotation pa = i.next();
        for( Peak p : peaks ) {
        if( p.equals(pa.getPeak()) ) {
            removed.add(pa.getPeak());
            i.remove();
            break;
        }
        }
    }

    for( Peak p : removed ) 
        addPeakAnnotation(p);
    }

    /**
       Return the list of peak annotations
     */
    public Vector<PeakAnnotation> getPeakAnnotations() {
    return peak_annotations;
    }

    /**
       Return all annotations for a specific peak
     */
    public Vector<PeakAnnotation> getPeakAnnotations(Peak p) {
    Vector<PeakAnnotation> ret = new Vector<PeakAnnotation>();
    for( PeakAnnotation pa : peak_annotations ) {
        if( pa.getPeak().equals(p) )
        ret.add(pa);
    }
    return ret;
    }

    /**
       Return all annotations for a specific mass/charge value
     */
    public Collection<PeakAnnotation> getPeakAnnotations(double mz_ratio) {
    double mz_tol = 0.000001;
    Vector<PeakAnnotation> ret = new Vector<PeakAnnotation>();
    for( PeakAnnotation pa : peak_annotations ) {
        if( pa.getPeak().getMZ()>mz_ratio+mz_tol )
        return ret;
        if( pa.getPeak().getMZ()>mz_ratio-mz_tol )
        ret.add(pa);
    }
    return ret;
    }

    /**
       Return <code>true</code> if the list contain the requested peak
       annotation
     */
    public boolean contains(PeakAnnotation pa) {
    return peak_annotations.contains(pa);
    }

    /**
       Return an iterator over the list of annotations
     */
    public Iterator<PeakAnnotation> iterator() {
    return peak_annotations.iterator();
    }

    /**
       Return the peak annotation at the specified position in the
       list
     */
    public PeakAnnotation elementAt(int ind) {
    return peak_annotations.elementAt(ind);
    }

    /**
       Return the number of peak annotations
     */
    public int size() {
    return peak_annotations.size();
    }

    /**
       Return <code>true</code> if all peaks are annotated with intact
       glycan molecules
     */
    public boolean isProfile() {
    for( PeakAnnotation pa : peak_annotations )
        if( pa.getFragment()!=null && pa.getFragment().isFragment() )
        return false;
    return true;
    }

    /**
       Add a new peak with no annotations to the list
       @return <code>true</code> if the peak was added to the
       list and no duplicates were present
     */
    public boolean addPeakAnnotation(Peak p) {        
    if( p==null )
        return false;
    return addPeakAnnotation(new PeakAnnotation(p));
    }  

    /**
       Add a new peak annotation to the list
       @param f the annotation
       @return <code>true</code> if the annotation was added to the
       list and no duplicates were present
     */
    public boolean addPeakAnnotation(Peak p, FragmentEntry f) {        
    if( p==null )
        return false;
    return addPeakAnnotation(new PeakAnnotation(p,f));
    }    
    
    /**
       Add a new peak annotation to the list
       @param f the annotation
       @param c the charges associated with this annotation
       @return <code>true</code> if the annotation was added to the
       list and no duplicates were present
     */
    public boolean addPeakAnnotation(Peak p, FragmentEntry f, IonCloud c) {        
    if( p==null )
        return false;
    return addPeakAnnotation(new PeakAnnotation(p,f,c));
    }  

    /**
       Add a new peak annotation to the list
       @param f the annotation
       @param c the charges associated with this annotation
       @param e the exchanges associated with this annotation
       @return <code>true</code> if the annotation was added to the
       list and no duplicates were present
     */
    public boolean addPeakAnnotation(Peak p, FragmentEntry f, IonCloud c, IonCloud e) {
    if( p==null )
        return false;
    return addPeakAnnotation(new PeakAnnotation(p,f,c,e));
    }    


    /**
       Add a new peak annotation to the list
       @return <code>true</code> if the annotation was added to the
       list and no duplicates were present
    */
    public boolean addPeakAnnotation(PeakAnnotation toadd) {           
    if( toadd==null )
        return false;

    // update max intensity
    max_intensity = Math.max(max_intensity,toadd.getPeak().getIntensity());
    
    // add sorted (start from the end because the peaks are tested in increasing order)
    for( int i=peak_annotations.size()-1; i>=0; i-- ) {
        PeakAnnotation cur = peak_annotations.elementAt(i);

        if( !toadd.isAnnotated() && cur.getPeak().equals(toadd.getPeak()) )
        return false; // empty annotation not needed

        int comparison = cur.compareTo(toadd);        
        if( comparison<0 ) {
        peak_annotations.insertElementAt(toadd,i+1);
        if( !cur.isAnnotated() && cur.getPeak().equals(toadd.getPeak()) ) {
        // remove empty annotation                
            peak_annotations.removeElementAt(i); 
        }
        return true;
        }
        if( comparison==0 )
        return false;
    }
    peak_annotations.insertElementAt(toadd,0);
    return true;
    }       

    /**
       Add all the annotations from a collection to the list
       @return <code>true</code> if at least one annotation from the
       collection was added
     */
    public boolean addPeakAnnotations(PeakAnnotationCollection pac) {           
    if( pac==null )
        return false;
    
    boolean added = false;
    for( int l=pac.peak_annotations.size()-1, i=peak_annotations.size()-1; l>=0; l-- ) {
        PeakAnnotation toadd = pac.peak_annotations.elementAt(l);

        // update max intensity
        max_intensity = Math.max(max_intensity,toadd.getPeak().getIntensity());
    
        // find position
        int comparison = 0;
        PeakAnnotation cur = null;
        for( ; i>=0; i-- ) {
        cur = peak_annotations.elementAt(i);    
        if( !toadd.isAnnotated() && cur.getPeak().equals(toadd.getPeak()) ) {
            comparison = 0;
            break;
        }
        if( (comparison = cur.compareTo(toadd))<=0 )
            break; // position found
        }
        
        // add sorted
        if( i<0 || comparison<0 ) {
        peak_annotations.insertElementAt(toadd,i+1);
        if( i>=0 && !cur.isAnnotated() && cur.getPeak().equals(toadd.getPeak()) ) {
            peak_annotations.removeElementAt(i); // remove empty annotation    
            i--;
        }
    
        added = true;
        }
    }

    return added;
    }    

    /**
       Remove all annotations from the specified peak, and also remove
       the peak from the list
       @return <code>true</code> if at leas one annotation was removed
     */
    public boolean removeAllPeakAnnotations(Peak p) {

    boolean removed = false;
    for( int i=0; i<peak_annotations.size(); i++ ) {
        PeakAnnotation pa = peak_annotations.elementAt(i);
        if( pa.getPeak().compareTo(p)>0 )
        return removed;
        if( pa.getPeak().equals(p) ) {
        peak_annotations.removeElementAt(i);
        i--;
        }
    }
    return removed;
    }

    /**
       Remove a specific peak annotation from the list
       @param leave_empty if <code>true</code> leave the non annotated
       peak in the list
       @return the index of the removed annotation
     */
    public int removePeakAnnotation(PeakAnnotation pa, boolean leave_empty) {           
    if( pa==null )
        return -1;

    // add sorted
    for( int i=0; i<peak_annotations.size(); i++ ) {
        PeakAnnotation e = peak_annotations.elementAt(i);
        if( e.equals(pa) ) {
        // remove annotation
        peak_annotations.removeElementAt(i);
        if( (i>0 && peak_annotations.elementAt(i-1).getPeak().equals(pa.getPeak())) ||
            (i<peak_annotations.size() && peak_annotations.elementAt(i).getPeak().equals(pa.getPeak())) )
            return i;

        if( leave_empty ) {
            // add empty annotation
            peak_annotations.insertElementAt(new PeakAnnotation(pa.getPeak()),i);
        }
        return i;
        }
    }
    return -1;
    }

    /**
       Recalculate the maximum intensity of all peaks after a change
       in the peak intensities
     */
    public void updateIntensities() {
    max_intensity = 0.;
    for( PeakAnnotation pa : peak_annotations ) 
        max_intensity = Math.max(max_intensity,pa.getPeak().getIntensity());
    }

    /**
       Return <code>true</code> if the specified peak is annotated
     */
    public boolean isAnnotated(Peak p) {
    for( PeakAnnotation pa : peak_annotations ) 
        if( pa.getPeak().equals(p) )
        return true;
    return false;
    }

    /**
       Equal to {@link #elementAt}
     */
    public PeakAnnotation getPeakAnnotation(int ind) {
    return elementAt(ind);
    } 

    /**
       Return the index of the first annotation for the peak with the
       specified mass/charge value
     */
    public int indexOf(double mz) {
    int ind = 0;
    double last_err = Double.POSITIVE_INFINITY;
    for( PeakAnnotation pa : peak_annotations ) {
        double err = Math.abs(mz - pa.getPeak().getMZ());
        if( err > last_err )
        return ind-1;
        last_err = err;
        ind++;
    }
    return ind-1;
    }

    /**
       Return the index of the specified annotation
     */
    public int indexOf(PeakAnnotation pa) {
    return peak_annotations.indexOf(pa);
    }

    /**
       Return the list of peaks in the collection as a 2xN table of
       mass/charge and intensity values
     */
    public double[][] getPeakData() {
    return getPeakData(0.,false);
    }

    /**
       Return the list of peaks in the collection as a 2xN table of
       mass/charge and intensity values
       @param rel_int output relative instead of absolute intensities,
       normalize by the maximum intensity in the collection
     */
    public double[][] getPeakData(boolean rel_int) {
    return getPeakData(0.,rel_int);
    }

    /**
       Return the list of peaks in the collection as a 2xN table of
       mass/charge and intensity values
       @param add_boundaries add two peaks at beginning and end of the
       table whose mass/charge values differs from their neighbours of
       the specified value
       @param rel_int output relative instead of absolute intensities,
       normalize by the maximum intensity in the collection
     */
    public double[][] getPeakData(double add_boundaries, boolean rel_int) {
      
    // init  vectors
    int no_peaks = size();
    int added = (add_boundaries>0.) ?2 :0;

    double[][] ret = new double[2][];
    ret[0] = new double[no_peaks+added];
    ret[1] = new double[no_peaks+added];

    // copy peaks
    double div = 1.;
    if( rel_int )
        div = getMaxIntensity()/100.;
       
    for( int i=0; i<no_peaks; i++ ) {
        Peak p = elementAt(i).getPeak();
        ret[0][i+added/2] = p.getMZ();
        ret[1][i+added/2] = p.getIntensity()/div;
    }

    // add boundaries
    if( add_boundaries>0. ) {
        ret[0][0] = ret[0][1]-add_boundaries;
        ret[1][0] = 0.;

        ret[0][no_peaks+1] = ret[0][no_peaks]+add_boundaries;
        ret[1][no_peaks+1] = 0.;
    }


    return ret;
    }
    

    /**
       Return the list of peaks in the collection as a 2xN table of
       mass/charge and intensity values
       @param from_mz limit the minimum mass/charge value of the peaks
       to be output
       @param to_mz limit the maximum mass/charge value of the peaks
       to be output
     */
    public double[][] getPeakData(double from_mz, double to_mz) {
    return getPeakData(from_mz,to_mz,0.,false);
    }

    /**
       Return the list of peaks in the collection as a 2xN table of
       mass/charge and intensity values
       @param from_mz limit the minimum mass/charge value of the peaks
       to be output
       @param to_mz limit the maximum mass/charge value of the peaks
       to be output
       @param rel_int output relative instead of absolute intensities,
       normalize by the maximum intensity in the collection
     */
    public double[][] getPeakData(double from_mz, double to_mz, boolean rel_int) {
    return getPeakData(from_mz,to_mz,0.,rel_int);
    }

    /**
       Return the list of peaks in the collection as a 2xN table of
       mass/charge and intensity values
       @param from_mz limit the minimum mass/charge value of the peaks
       to be output
       @param to_mz limit the maximum mass/charge value of the peaks
       to be output
       @param add_boundaries add two peaks at beginning and end of the
       table whose mass/charge values differs from their neighbours of
       the specified value
       @param rel_int output relative instead of absolute intensities,
       normalize by the maximum intensity in the collection
     */
    public double[][] getPeakData(double from_mz, double to_mz, double add_boundaries, boolean rel_int) {
      
    // count peaks
    int no_peaks = 0;
    for( PeakAnnotation pa : peak_annotations ) {
        if( pa.getPeak().getMZ()>=from_mz && pa.getPeak().getMZ()<=to_mz )
        no_peaks++;
    }

    // init  vectors
    int added = (add_boundaries>0.) ?2 :0;

    double[][] ret = new double[2][];
    ret[0] = new double[no_peaks+added];
    ret[1] = new double[no_peaks+added];

    // copy peaks
    double div = 1.;
    if( rel_int )
        div = getMaxIntensity(from_mz,to_mz)/100.;

    int i=0;
    for( PeakAnnotation pa : peak_annotations ) {
        Peak p = pa.getPeak();
        if( pa.getPeak().getMZ()>=from_mz && pa.getPeak().getMZ()<=to_mz ) {
        ret[0][i+added/2] = p.getMZ();
        ret[1][i+added/2] = p.getIntensity()/div;
        i++;
        }
    }

    // add boundaries
    if( add_boundaries>0. ) {
        ret[0][0] = ret[0][1]-add_boundaries;
        ret[1][0] = 0.;

        ret[0][no_peaks+1] = ret[0][no_peaks]+add_boundaries;
        ret[1][no_peaks+1] = 0.;
    }


    return ret;
    }

    /**
       Return the peak associated with the peak annotation at the specified
       index
    */
    public Peak getPeak(int ind) {
    return elementAt(ind).getPeak();
    }

    /**
       Return the annotation associated with the peak annotation at
       the specified index
    */
    public Annotation getAnnotation(int ind) {
    return elementAt(ind).getAnnotation();
    }

    /**
       Return the annotation associated with the peak annotation at
       the specified index
    */
    public FragmentEntry getFragmentEntry(int ind) {
    return elementAt(ind).getAnnotation().getFragmentEntry();
    }


    /**
       Return the charges associated with the peak annotation at
       the specified index
    */
    public IonCloud getIons(int ind) {
    return elementAt(ind).getAnnotation().getIons();
    }

    /**
       Return the neutral exchanges associated with the peak annotation at
       the specified index
    */
    public IonCloud getNeutralExchanges(int ind) {
    return elementAt(ind).getAnnotation().getNeutralExchanges();
    }

    /**
       Return the mass/charge value of the peak associated with the
       peak annotation at the specified index
    */
    public double getMZ(int ind) {
    return elementAt(ind).getPeak().getMZ();
    }      
    
    /**
       Return the intensity of the peak associated with the peak
       annotation at the specified index
    */
    public double getIntensity(int ind) {
    return elementAt(ind).getPeak().getIntensity();
    }    

    /**
       Return the relative intensity of the peak associated with the
       peak annotation at the specified index, normalize the value by
       the maximum intensity of the peaks in the collection
    */
    public double getRelativeIntensity(int ind) {
    if( max_intensity == 0. )
        return getIntensity(ind);
    return 100.*getIntensity(ind)/max_intensity;
    }

    /**
       Return the maximum value of the intensity of the peaks in the
       collection
     */
    public double getMaxIntensity() {
    return max_intensity;
    }

    /**
       Return the maximum value of the intensity of the peaks in the
       collection
       @param from_mz limit the minimum mass/charge value of the peaks
       for which the maximum intensity must be computed
       @param to_mz limit the maximum mass/charge value of the peaks
       for which the maximum intensity must be computed
     */
    public double getMaxIntensity(double from_mz, double to_mz) {
    double ret = 0.;
    for( PeakAnnotation pa : peak_annotations ) {
        if( pa.getPeak().getMZ()>=from_mz && pa.getPeak().getMZ()<=to_mz )
        ret = Math.max(pa.getPeak().getIntensity(),ret);
    }
    return ret;
    }

    /**
       Return the minimum difference between experimental and
       predicted mass/charge values
     */
    public double getMinAccuracy() {    
    double min_acc = Double.MAX_VALUE;
    for( PeakAnnotation pa : peak_annotations) {
        if( pa.isAnnotated() )
        min_acc = Math.min(pa.getAccuracy(),min_acc);
    }
    return min_acc;
    }

    /**
       Return the maximum difference between experimental and
       predicted mass/charge values
     */
    public double getMaxAccuracy() {
    double max_acc = Double.MIN_VALUE;
    for( PeakAnnotation pa : peak_annotations) {
        if( pa.isAnnotated() )
        max_acc = Math.max(pa.getAccuracy(),max_acc);
    }
    return max_acc;
    }

    /**
       Return the minimum difference between experimental and
       predicted mass/charge values expressed in PPM
     */
    public double getMinAccuracyPPM() {    
    double min_acc = Double.MAX_VALUE;
    for( PeakAnnotation pa : peak_annotations) {
        if( pa.isAnnotated() )
        min_acc = Math.min(pa.getAccuracyPPM(),min_acc);
    }
    return min_acc;
    }

    /**
       Return the maximum difference between experimental and
       predicted mass/charge values expressed in PPM
     */
    public double getMaxAccuracyPPM() {
    double max_acc = Double.MIN_VALUE;
    for( PeakAnnotation pa : peak_annotations) {
        if( pa.isAnnotated() )
        max_acc = Math.max(pa.getAccuracyPPM(),max_acc);
    }
    return max_acc;
    }

    /**
       Return the difference between experimental and predicted
       mass/charge values for the peak annotation at the specified
       position
     */
    public double getAccuracy(int ind) {
    if( !isAnnotated(ind) )
        return 0.;
    return elementAt(ind).getAccuracy();
    }

    /**
       Return the difference in PPM between experimental and predicted
       mass/charge values for the peak annotation at the specified
       position
     */
    public double getAccuracyPPM(int ind) {
    if( !isAnnotated(ind) )
        return 0.;
    return elementAt(ind).getAccuracyPPM();
    }

    /**
       Return <code>true</code> if the peak annotation at the
       specified position contains a non-empty structure
     */
    public boolean isAnnotated(int ind) {
    return elementAt(ind).isAnnotated();
    }

    /**
       Return the glycan structure associated with the peak annotation
       at the specified position 
       @see FragmentEntry#getFragment
    */
    public Glycan getFragment(int ind) {
    return elementAt(ind).getAnnotation().getFragmentEntry().getFragment();
    }

    /**
       Return the type of fragment associated with the peak annotation
       at the specified position
       @see FragmentEntry#getName
    */
    public String getFragmentType(int ind) {
    return elementAt(ind).getAnnotation().getFragmentEntry().getName();
    }

    /**
       Return the score associated with the peak annotation at the
       specified position
       @see FragmentEntry#getScore
    */
    public double getFragmentScore(int ind) {
    return elementAt(ind).getAnnotation().getFragmentEntry().getScore();
    }

    /**
       Return the mass of the fragment associated with the peak annotation
       at the specified position
       @see FragmentEntry#getMass
    */
    public double getFragmentMass(int ind) {
    return elementAt(ind).getAnnotation().getFragmentEntry().getMass();
    }   

    /*public MZSeries getFragmentMZs(int ind) {
    return elementAt(ind).getAnnotation().getFragmentEntry().getMZs();
    }   
    */

    /**
       Return the predicted mass/charge value associated with the peak
       annotation at the specified position
       @see PeakAnnotation#getAnnotationMZ
    */
    public double getAnnotationMZ(int ind) {
    return elementAt(ind).getAnnotationMZ();
    }   

    /**
       Return the number of charges associated with the peak
       annotation at the specified position
       @see PeakAnnotation#getAnnotationZ
    */
    public int getAnnotationZ(int ind) {
    return elementAt(ind).getAnnotationZ();
    }   

    // serialization

    /**
       Create a new object from its XML representation as part of a
       DOM tree.
     */
    static public PeakAnnotationCollection fromXML(Node pac_node) throws Exception {
    PeakAnnotationCollection ret = new PeakAnnotationCollection();
    if( pac_node==null )
        return ret;

    Vector<Node> pa_nodes = XMLUtils.findAllChildren(pac_node, "PeakAnnotation");
    for( Node pa_node : pa_nodes) 
        ret.addPeakAnnotation(PeakAnnotation.fromXML(pa_node));
    
    return ret;    
    }
    
    /**
       Create an XML representation of this object to be part of a DOM
       tree.
    */
    public Element toXML(Document document) {
    if( document==null )
        return null;
    
    // create root node
    Element pac_node = document.createElement("PeakAnnotationCollection");
    if( pac_node==null )
        return null;

    // add PeakAnnotation nodes
    for(PeakAnnotation pa : peak_annotations) {
        Element pa_node = pa.toXML(document);
        if( pa_node!=null )
        pac_node.appendChild(pa_node);
    }
    
    return pac_node;
    }
       
    /**
       Default SAX handler to read a representation of this object
       from an XML stream.
     */ 
    public static class SAXHandler extends SAXUtils.ObjectTreeHandler {
     
    public boolean isElement(String namespaceURI, String localName, String qName) {
        return qName.equals(getNodeElementName());
    }

    public static String getNodeElementName() {
        return "PeakAnnotationCollection";
    }

    protected SAXUtils.ObjectTreeHandler getHandler(String namespaceURI, String localName, String qName) {
        if( qName.equals(PeakAnnotation.SAXHandler.getNodeElementName()) )
        return new PeakAnnotation.SAXHandler();
        return null;
    }

    protected Object finalizeContent(String namespaceURI, String localName, String qName) {
        PeakAnnotationCollection ret = new PeakAnnotationCollection();

        for( Object o : getSubObjects(PeakAnnotation.SAXHandler.getNodeElementName()) ) 
        ret.addPeakAnnotation((PeakAnnotation)o);

        return (object = ret);
    }
    }

    public void write(TransformerHandler th) throws SAXException {
    th.startElement("","","PeakAnnotationCollection",new AttributesImpl());
    for(PeakAnnotation pa : peak_annotations) 
        pa.write(th);
    th.endElement("","","PeakAnnotationCollection");
    }

}