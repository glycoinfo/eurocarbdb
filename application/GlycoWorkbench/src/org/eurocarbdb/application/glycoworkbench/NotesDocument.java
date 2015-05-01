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
import java.util.regex.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.helpers.AttributesImpl;

public class NotesDocument extends BaseDocument implements SAXUtils.SAXWriter  {

    private String text = "";
    
    public NotesDocument() {
    super();
    }

    public NotesDocument(String t) {
    super();

    if( t!=null )
        text = t;
    }

    public String getName() {
    return "Notes";
    }

    public javax.swing.ImageIcon getIcon() {
    return FileUtils.defaultThemeManager.getImageIcon("notes");
    }
    
    
    public Collection<javax.swing.filechooser.FileFilter> getFileFormats() {
    Vector<javax.swing.filechooser.FileFilter> filters = new Vector<javax.swing.filechooser.FileFilter>();    
    filters.add(new ExtensionFileFilter(new String[] {"txt"}, "Text files"));
    return filters;
    }
    
    public javax.swing.filechooser.FileFilter getAllFileFormats() {
    return new ExtensionFileFilter(new String[] {"txt"}, "Text files");
    }

    public void initData() {
    text = "";
    }

    public int size() {
    return text.length();
    }

    public String getText() {
    return text;
    }

    public void setText(String t) {
    fill(t);
    }

    public String toString() {
    return text;
    }

    public void fromString(String str, boolean merge) throws Exception {
    if( merge )
        text += str;
    else
        text = str;
    }

    public Element toXML(Document document) {
    if( document==null )
        return null;
    
    Element n_node = document.createElement("Notes");
    XMLUtils.setText(n_node,text);

    return n_node;
    }

    public void fromXML(Node n_node, boolean merge) throws Exception {
    if( !merge )
        text = "";

    String t = XMLUtils.getText(n_node);
    if( t!=null && t.length()>0 )
        text += t;    
    }
    
    public static class SAXHandler extends SAXUtils.ObjectTreeHandler {
    
    private NotesDocument theDocument;
    private boolean merge;
    
    public SAXHandler(NotesDocument _doc, boolean _merge) {
        theDocument = _doc;
        merge = _merge;
    }

    public boolean isElement(String namespaceURI, String localName, String qName) {
        return qName.equals(getNodeElementName());
    }

    public static String getNodeElementName() {
        return "Notes";
    }    

    protected Object finalizeContent(String namespaceURI, String localName, String qName) throws SAXException{

        if( !merge )
        theDocument.text = "";

        theDocument.text += text.toString();        

        return (object = theDocument);
    }
    }  

    public void write(TransformerHandler th) throws SAXException {
    th.startElement("","","Notes",new AttributesImpl());
    th.characters(text.toCharArray(),0,text.length());
    th.endElement("","","Notes");
    }
}