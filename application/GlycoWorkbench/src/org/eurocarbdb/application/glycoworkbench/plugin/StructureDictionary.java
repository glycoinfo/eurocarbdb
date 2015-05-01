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
/**
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

package org.eurocarbdb.application.glycoworkbench.plugin;

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

public class StructureDictionary extends BaseDocument {
    
    private boolean on_file_system = false;
    private String dictionary_name = "";
    private StructureScorer theScorer = null;
    private TreeMap<String,StructureType> dictionary = new TreeMap<String,StructureType>();       
    
    //---- init

    public StructureDictionary(String _name) {
    super(false);
    dictionary_name = _name;
    }
    
    public StructureDictionary(String _filename, boolean _on_file_system, StructureScorer scorer) throws Exception {
    super(false);
    load(_filename,_on_file_system,scorer);
    }


    public StructureDictionary(InputStream is, StructureScorer scorer) throws Exception {
    super(false);
    load(is,scorer);
    }

    // base document methods

    public int size() {
    return dictionary.size();
    }
    
    public String getName() {
    return "StructureDictionary";
    }

    public String getDictionaryName() {
    return dictionary_name;
    }

    public void setDictionaryName(String n) {
    dictionary_name = n;
    }

    public Collection<javax.swing.filechooser.FileFilter> getFileFormats() {
    Vector<javax.swing.filechooser.FileFilter> filters = new Vector<javax.swing.filechooser.FileFilter>();    
    filters.add(new ExtensionFileFilter(new String[] {"gwd"}, "GlycoWorkbench dictionary file"));
    return filters;
    }

    public javax.swing.filechooser.FileFilter getAllFileFormats() {
    return new ExtensionFileFilter(new String[] {"gwd"}, "GlycoWorkbench dictionary file");
    }

    public void initData() {
    on_file_system = false;
    theScorer = null;
    dictionary = new TreeMap<String,StructureType>();
    }
    

    // --- Data access

    public boolean isOnFileSystem() {
    return on_file_system;
    }

   
    public Iterator<StructureType> iterator() {
    return dictionary.values().iterator();
    }
    
    public Collection<StructureType> getStructureTypes() {
    return dictionary.values();
    }     
  
    public StructureScorer getScorer() {
    return theScorer;
    }

    public void setScorer(StructureScorer scorer) {
    theScorer = scorer;
    }

    private boolean removePVT(StructureType st) {
    if( dictionary.containsValue(st) ) {
        dictionary.remove(st.getStructure());
        return true;
    }
    return false;
    }

    public boolean remove(StructureType st) {
    if( removePVT(st) ) {
        fireDocumentChanged();
        return true;
    }
    return false;
    }

    public boolean removeAll(Collection<StructureType> sts) {
    boolean removed = false;
    for( StructureType st : sts ) {
        if( removePVT(st) )
        removed = true;
    }
    
    if( removed ) {
        fireDocumentChanged();
        return true;
    }
    return false;
    }

    public boolean setType(StructureType st, String type) {
    if( dictionary.containsValue(st) ) {
        st.setType(type);
        fireDocumentChanged();
        return true;
    }
    return false;
    }

    public boolean setSource(StructureType st, String source) {
    if( dictionary.containsValue(st) ) {
        st.setSource(source);
        fireDocumentChanged();
        return true;
    }
    return false;
    }

    private boolean addPVT(StructureType st) {
    if( st==null )
        return false;

    StructureType old = dictionary.get(st.getStructure());
    if( old==null ) {
        dictionary.put(st.getStructure(),st);
        return true;
    }
    
    return old.merge(st);
    }

    public boolean add(StructureType st) {
    if( st!=null && addPVT(st.clone(this.dictionary_name)) ) {
        fireDocumentChanged();
        return true;        
    }
    return false;
    }

    public boolean addAll(Collection<StructureType> sts) {
    boolean added = false;
    for( StructureType st : sts )  {
        if( addPVT(st.clone(this.dictionary_name)) )
        added = true;
    }
    
    if( added ) {
        fireDocumentChanged();
        return true;
    }
    return false;
    }

    public boolean addAll(Collection<Glycan> gs, String type, String source) {
    boolean added = false;
    for( Glycan g : gs ) {
        if( addPVT(new StructureType(this.dictionary_name,type,source,g.toString())) )
        added = true;
    }
    
    if( added ) {
        fireDocumentChanged();
        return true;
    }
    return false;
    }
       
    public FragmentCollection generateStructures(ProfilerOptions opt) throws Exception{
    FragmentCollection fc = new FragmentCollection();
    generateStructures(fc,opt);
    return fc;
    }
    
    public void generateStructures(FragmentCollection fc, ProfilerOptions opt) throws Exception{
    for( StructureType st : dictionary.values() )
        fc.addFragment(st.generateStructure(opt),st.getDescription());
    }

    // -- serialization

    public boolean open(File file, boolean merge, boolean warning) {
    return load(file.getAbsolutePath(),true,null);
    }
   
    public boolean load(String _filename, boolean _on_file_system, StructureScorer scorer) {
    try {
        load(FileUtils.open(this.getClass(),_filename,!_on_file_system),scorer);
        on_file_system = _on_file_system;
        setFilename(_filename);

        fireDocumentInit();

        return true;
    }
    catch(Exception e) {
        LogUtils.report(e);
        return false;
    }
    }

    private void load(InputStream is, StructureScorer scorer) throws Exception {
    //Document d = XMLUtils.read(is);
    //this.fromXML(XMLUtils.assertChild(d,"StructureDictionary"),false,scorer);
    read(is,false);
    }

    public boolean save() {
    return save(filename);
    }

    public boolean save(String _filename) {
    try {
        // write to tmp file
        File tmpfile = File.createTempFile("gwb",null);
        write(new FileOutputStream(tmpfile));

        // copy to dest file and delete tmp file
        FileUtils.copy(tmpfile,new File(filename));
        tmpfile.delete();

        on_file_system = true;  
        setFilename(_filename);   
    
        //
        fireDocumentInit();
       
        return true;
    }
    catch( Exception e ) {
        LogUtils.report(e);
        return false;
    }
    }

    public void write(OutputStream os) throws Exception {
    Document d = XMLUtils.newDocument();
    d.appendChild(toXML(d));
    XMLUtils.write(os,d);
    }
 

    // serialization

    public String toString() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    Document document = XMLUtils.newDocument();
    document.appendChild(toXML(document));
    XMLUtils.write(bos,document);

    return bos.toString();
    }

    public void fromString(String str, boolean merge) throws Exception {
    ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes());
    read(bis,merge);
    }
    
    protected void read(InputStream is, boolean merge) throws Exception {
    /*Document document = XMLUtils.read(is);
    if( document==null )
        throw new Exception("Cannot read from stream");    
    fromXML(XMLUtils.assertChild(document,"StructureDictionary"),merge,null);
    */
    SAXUtils.read(is,new SAXHandler(this,merge));    
    }

    public static StructureDictionary fromXML(Node d_node, StructureScorer scorer) throws Exception {
    StructureDictionary ret = new StructureDictionary("");
    ret.fromXML(d_node,false,scorer);
    return ret;
    }


    public void fromXML(Node d_node, boolean merge,StructureScorer scorer) throws Exception {
    if( !merge ) 
        dictionary.clear();

    // read name
    dictionary_name = XMLUtils.getAttribute(d_node,"name");
    if( dictionary_name==null )
        dictionary_name = "";
    
    // read options
    if( scorer!=null ) {
        Node s_node = XMLUtils.findChild(d_node,scorer.getScorerType());
        if( s_node !=null ) 
        theScorer = scorer.fromXML(s_node);
    }

    // read types
    Vector<Node> st_nodes = XMLUtils.findAllChildren(d_node, "StructureType");
    for( Node st_node : st_nodes) {
        StructureType st = StructureType.fromXML(st_node);
        if( st!=null ) 
        dictionary.put(st.getStructure(),st);
    }        
    }

    public Element toXML(Document document) {
    if( document==null )
        return null;
    
    // create root node
    Element d_node = document.createElement("StructureDictionary");
    if( d_node==null )
        return null;

    // add name
    d_node.setAttribute("name", dictionary_name);

    // add scorer
    if( theScorer!=null ) 
        d_node.appendChild(theScorer.toXML(document));
    
    // add types
    for( StructureType st : dictionary.values() ) {
        Element st_node = st.toXML(document);
        if( st_node!=null )
        d_node.appendChild(st_node);
    }
    
    return d_node;
    }

    public static class SAXHandler extends SAXUtils.ObjectTreeHandler {
    
    private StructureDictionary theDocument;
    private boolean merge;
    
    public SAXHandler(StructureDictionary _doc, boolean _merge) {
        theDocument = _doc;
        merge = _merge;
    }

    public boolean isElement(String namespaceURI, String localName, String qName) {
        return qName.equals(getNodeElementName());
    }

    public static String getNodeElementName() {
        return "StructureDictionary";
    }

    protected SAXUtils.ObjectTreeHandler getHandler(String namespaceURI, String localName, String qName) {
        if( qName.equals(StructureType.SAXHandler.getNodeElementName()) )
        return new StructureType.SAXHandler();
         return null;
    }
    
    protected void initContent(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        super.initContent(namespaceURI,localName,qName,atts);
    
        theDocument.dictionary_name = stringAttribute(atts,"name","");
    }
    
    protected Object finalizeContent(String namespaceURI, String localName, String qName) throws SAXException{
        
        if( !merge ) 
        theDocument.dictionary.clear();

        for( Object o : getSubObjects(StructureType.SAXHandler.getNodeElementName()) )  {
        StructureType st = (StructureType)o;
        theDocument. dictionary.put(st.getStructure(),st);
        }
        
        return (object = theDocument);
    }
    }
}
