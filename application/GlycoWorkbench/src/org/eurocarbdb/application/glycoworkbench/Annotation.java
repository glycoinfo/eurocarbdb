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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.helpers.AttributesImpl;


/**
   Represents a possible annotation that can be associated with a
   labeled peak
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class Annotation implements Comparable<Annotation>, SAXUtils.SAXWriter {    

    protected FragmentEntry fragmentEntry;
    protected IonCloud      ions;
    protected IonCloud      neutralExchanges;
    
    /**
       Create an empty annotation, can be used to identify
       non-annotated peaks
     */
    public Annotation() {
    fragmentEntry    = new FragmentEntry();
    ions             = new IonCloud();
    neutralExchanges = new IonCloud();

    fragmentEntry.setCharges(ions,neutralExchanges);
    }

    /**
       Create a new annotation
       @param f represent the intact or fragment structure that will
       be associated with a labeled peak
     */
    public Annotation(FragmentEntry f) {
    fragmentEntry = (f!=null) ?f.clone() :new FragmentEntry();
    ions          = fragmentEntry.getCharges();
    neutralExchanges = fragmentEntry.getNeutralExchanges();
    }    

    /**
       Create a new annotation
       @param f represent the intact or fragment structure that will
       be associated with a labeled peak
       @param c the charges associated with the annotation
     */
    public Annotation(FragmentEntry f, IonCloud c) {
    fragmentEntry = (f!=null) ?f.clone() :new FragmentEntry();
    ions          = (c!=null) ?c.clone() :new IonCloud();
    neutralExchanges = new IonCloud();

    fragmentEntry.setCharges(ions,neutralExchanges);
    }    

    /**
       Create a new annotation
       @param f represent the intact or fragment structure that will
       be associated with a labeled peak
       @param c the charges associated with the annotation
       @param e the exchanges associated with the annotation
     */
    public Annotation(FragmentEntry f, IonCloud c, IonCloud e) {
    fragmentEntry    = (f!=null) ?f.clone() :new FragmentEntry();
    ions             = (c!=null) ?c.clone() :new IonCloud();
    neutralExchanges = (e!=null) ?e.clone() :new IonCloud();

    fragmentEntry.setCharges(ions,neutralExchanges);
    }    

    /**
       Create a copy of this object
     */
    public Annotation clone() {
    return new Annotation(fragmentEntry,ions,neutralExchanges);
    }

    /**
       Return the {@link FragmentEntry} containing the structure
       associated with this annotation
     */
    public FragmentEntry getFragmentEntry() {
    return fragmentEntry;
    }

    /**
       Return the charges associated with this annotation
     */
    public IonCloud getIons() {
    return ions;
    }

    /**
       Return the exchanges associated with this annotation
     */
    public IonCloud getNeutralExchanges() {
    return neutralExchanges;
    }

    /**
       Return <code>true</code> if this annotation is associated with
       an empty structure
     */
    public boolean isEmpty() {
    return ((fragmentEntry.fragment==null || fragmentEntry.fragment.isEmpty()) && (fragmentEntry.name==null || fragmentEntry.name.length()==0));
    }

    public boolean equals(Object other) {
    if( !(other instanceof Annotation) )
        return false;
    
    Annotation a = (Annotation)other;
    return (fragmentEntry.equals(a.fragmentEntry) && ions.equals(a.ions) && neutralExchanges.equals(a.neutralExchanges));
    }

    public int hashCode() {
    int ret = 0;
    ret += fragmentEntry.hashCode();
    ret += ions.hashCode();
    ret += neutralExchanges.hashCode();
    return ret;
    }

    public int compareTo(Annotation a) {
    if( a==null )
        return 1;
    return this.fragmentEntry.compareTo(a.fragmentEntry);
    }  

    /**
       Return the difference between the experimental and the
       predicted mass/charge values
       @param peak the experimentally derived peak
     */
    public double getAccuracy(Peak peak) {
    if( isEmpty() )
        return 0.;    
    return (getMZ() - peak.getMZ());    
    }

    /**
       Return the difference between the experimental and the
       predicted mass/charge values in PPM
       @param peak the experimentally derived peak
     */
    public double getAccuracyPPM(Peak peak) {
    if( isEmpty() )
        return 0.;
    return (1000000*(getMZ()/peak.getMZ() - 1));
    }

    /**
       Return the predicted mass/charge value
     */
    public double getMZ() {
    return ions.computeMZ(neutralExchanges.getIonsMass() + fragmentEntry.mass);
    }

    /**
       Return the predicted number of charges
     */
    public int getZ() {
    return ions.getIonsNum();
    }

    // serialization

    public String toString() {
    return fragmentEntry + " " + ions + " " + neutralExchanges;
    }

    /**
       Create a new object from its XML representation as part of a
       DOM tree.
     */
    static public Annotation fromXML(Node ann_node) throws Exception {
    Annotation ret = new Annotation();
    
    Node fe_node = XMLUtils.assertChild(ann_node, "FragmentEntry");
    ret.fragmentEntry = FragmentEntry.fromXML(fe_node);
    
    ret.ions = IonCloud.fromString(XMLUtils.getAttribute(ann_node,"ions"));
    ret.neutralExchanges = IonCloud.fromString(XMLUtils.getAttribute(ann_node,"neutralExchanges"));

    ret.fragmentEntry.setCharges(ret.ions,ret.neutralExchanges,false);
    
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
    Element ann_node = document.createElement("Annotation");
    if( ann_node==null )
        return null;
    
    // add fragment
    Element fe_node = fragmentEntry.toXML(document);
    if( fe_node==null )
        return null;
    ann_node.appendChild(fe_node);    
    
    // add ions
    ann_node.setAttribute("ions",ions.toString());
    ann_node.setAttribute("neutralExchanges",neutralExchanges.toString());
    
    return ann_node;
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
        return "Annotation";
    }

    protected SAXUtils.ObjectTreeHandler getHandler(String namespaceURI, String localName, String qName) {
        if( qName.equals(FragmentEntry.SAXHandler.getNodeElementName()) )
        return new FragmentEntry.SAXHandler();
         return null;
    }
    
    protected void initContent(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        super.initContent(namespaceURI,localName,qName,atts);
    
        Annotation ret = new Annotation();    
        try {
        ret.ions = IonCloud.fromString(stringAttribute(atts,"ions",""));
        ret.neutralExchanges = IonCloud.fromString(stringAttribute(atts,"neutralExchanges",""));
        }
        catch( Exception e ){
        throw new SAXException(createMessage(e));
        }

        object = ret;
    }
    protected Object finalizeContent(String namespaceURI, String localName, String qName) throws SAXException{
        
        Annotation ret = (Annotation)object;
        ret.fragmentEntry = (FragmentEntry)getSubObject(FragmentEntry.SAXHandler.getNodeElementName(),true);       
        ret.fragmentEntry.setCharges(ret.ions,ret.neutralExchanges,false);
        
        return ret;
    }
    }

    public void write(TransformerHandler th) throws SAXException {

    AttributesImpl atts = new AttributesImpl();
    atts.addAttribute("","","ions","CDATA",ions.toString());
    atts.addAttribute("","","neutralExchanges","CDATA",neutralExchanges.toString());

    th.startElement("","","Annotation",atts);
    fragmentEntry.write(th);
    th.endElement("","","Annotation");
    }
}