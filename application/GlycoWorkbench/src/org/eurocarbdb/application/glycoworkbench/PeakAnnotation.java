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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.helpers.AttributesImpl;


/**
   Represent a peak annotated with an intact or fragment glycan
   molecule. An empty structure can represent a non annotated peak

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/


public class PeakAnnotation implements Comparable<PeakAnnotation>, SAXUtils.SAXWriter {

    protected Peak       peak;
    protected Annotation annotation;    

    /**
       Create an empty object
     */
    public PeakAnnotation() {
    peak       = new Peak();
    annotation = new Annotation();
    }

    /**
       Create a non-annotated peak
       @param _mz the mass/charge value of the peak
       @param _int intensity value of the peak
     */
    public PeakAnnotation(double _mz, double _int) {
    peak = new Peak(_mz,_int);
    annotation = new Annotation();
    }
   

    /**
       Create a non-annotated peak
    */
    public PeakAnnotation(Peak p) {
    peak       = (p!=null) ?p.clone() :new Peak();
    annotation = new Annotation();
    }

    /**
       Create an annotated peak
       @param f the annotation
    */
    public PeakAnnotation(Peak p, FragmentEntry f) {
    peak       = (p!=null) ?p.clone() :new Peak();
    annotation = new Annotation(f);
    }

    /**
       Create an annotated peak
       @param f the annotation
       @param c the charges associated with this annotation
    */
    public PeakAnnotation(Peak p, FragmentEntry f, IonCloud c) {
    peak       = (p!=null) ?p.clone() :new Peak();
    annotation = new Annotation(f,c);
    }


    /**
       Create an annotated peak
       @param f the annotation
       @param c the charges associated with this annotation
       @param ex the exchanges associated with this annotation     
    */
    public PeakAnnotation(Peak p, FragmentEntry f, IonCloud c, IonCloud ex) {
    peak       = (p!=null) ?p.clone() :new Peak();
    annotation = new Annotation(f,c,ex);
    }

    /**
       Create an annotated peak
       @param a the annotation
    */
    public PeakAnnotation(Peak p, Annotation a) {
    peak       = (p!=null) ?p.clone() :new Peak();
    annotation = (a!=null) ?a.clone() :new Annotation();
    }

    /**
       Return a copy of this peak annotation
     */
    public PeakAnnotation clone() {
    return new PeakAnnotation(peak,annotation);
    }    
    
    public int compareTo(PeakAnnotation pa) {
    if( pa==null )
        return 1;

    // compare peak
    int cp = this.peak.compareTo(pa.peak);
    if( cp!=0 ) return cp;    
    
    // compare accuracy
    double ma1 = Math.round(this.getAccuracy()*10000);
    double ma2 = Math.round(pa.getAccuracy()*10000);    
    if( ma1<ma2 ) return -1;
    if( ma1>ma2 ) return 1;

    // compare annotations
    return this.annotation.compareTo(pa.annotation);
    }

    public boolean equals(Object other) {
    if( !(other instanceof PeakAnnotation) )
        return false;
        
    PeakAnnotation pa = (PeakAnnotation)other;
    return (peak.equals(pa.peak) && annotation.equals(pa.annotation));
    }

    public int hashCode() {
    return peak.hashCode() + annotation.hashCode();
    }

    /**
       Return the peak associated with this peak annotation
    */
    public Peak getPeak() {
    return peak;
    }

    /**
       Return the annotation associated with this peak annotation
    */
    public Annotation getAnnotation() {
    return annotation;
    }

    /**
       Return the mass/charge value of the peak associated with this
       peak annotation
    */
    public Double getMZ() {
    return peak.getMZ();
    }

    /**
       Return the intensity value of the peak associated with this
       peak annotation
    */
    public Double getIntensity() {
    return peak.getIntensity();
    }
    
    /**
       Return the annotation associated with this peak annotation 
    */
    public FragmentEntry getFragmentEntry() {
    return annotation.getFragmentEntry();
    }

    /**
       Return the glycan structure associated with this peak
       annotation
       @see FragmentEntry#getFragment
    */
    public Glycan getFragment() {
    return annotation.getFragmentEntry().fragment;
    }

    /**
       Return the type of fragment associated with this peak
       annotation
       @see FragmentEntry#getName
    */
    public String getFragmentType() {
    return annotation.getFragmentEntry().name;
    }

    /**
       Return <code>true</code> if the peak annotation contains a
       non-empty structure
     */
    public boolean isAnnotated() {
    return !annotation.isEmpty();
    }       

    /**
       Return the difference between experimental and predicted
       mass/charge values
    */
    public double getAccuracy() {
    return annotation.getAccuracy(peak);
    }

    /**
       Return the difference between experimental and predicted
       mass/charge values in PPM
    */
    public double getAccuracyPPM() {
    return annotation.getAccuracyPPM(peak);
    }

    /**
       Return the predicted mass/charge value associated with the peak
       annotation 
    */
    public double getAnnotationMZ() {
    return annotation.getMZ();
    }

    /**
       Return the number of charges associated with the peak
       annotation
    */
    public int getAnnotationZ() {
    return annotation.getZ();
    }

    /**
       Return the list of charges associated with the peak
       annotation
    */
    public IonCloud getIons() {
    return annotation.getIons();
    }

    /**
       Return the list of exchanges associated with the peak
       annotation
    */
    public IonCloud getNeutralExchanges() {
    return annotation.getNeutralExchanges();
    }
    
    // serialization
    
    public String toString() {
    return peak + " " + annotation;
    }

    /**
       Create a new object from its XML representation as part of a
       DOM tree.
     */
    static public PeakAnnotation fromXML(Node pa_node) throws Exception {
    PeakAnnotation ret = new PeakAnnotation();
    
    Node peak_node = XMLUtils.assertChild(pa_node,"Peak");
    ret.peak = Peak.fromXML(peak_node);
    
    Node ann_node = XMLUtils.assertChild(pa_node,"Annotation");
    ret.annotation = Annotation.fromXML(ann_node);
                        
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
    Element pa_node = document.createElement("PeakAnnotation");
    if( pa_node==null )
        return null;

    // add peak
    Element peak_node = peak.toXML(document);
    if( peak_node==null )
        return null;
    pa_node.appendChild(peak_node);    

    // add annotation
    Element ann_node = annotation.toXML(document);
    if( ann_node==null )
        return null;
    pa_node.appendChild(ann_node);    
    
    return pa_node;
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
        return "PeakAnnotation";
    }

    protected SAXUtils.ObjectTreeHandler getHandler(String namespaceURI, String localName, String qName) {
        if( qName.equals(Peak.SAXHandler.getNodeElementName()) )
        return new Peak.SAXHandler();
        if( qName.equals(Annotation.SAXHandler.getNodeElementName()) )
        return new Annotation.SAXHandler();
        return null;
    }

    protected Object finalizeContent(String namespaceURI, String localName, String qName) throws SAXException{
        Peak p = (Peak)getSubObject(Peak.SAXHandler.getNodeElementName(),true);
        Annotation a = (Annotation)getSubObject(Annotation.SAXHandler.getNodeElementName(),true);
        return new PeakAnnotation(p,a);
    }
    }

    public void write(TransformerHandler th) throws SAXException {
    th.startElement("","","PeakAnnotation",new AttributesImpl());
    peak.write(th);
    annotation.write(th);
    th.endElement("","","PeakAnnotation");
    }
}
    