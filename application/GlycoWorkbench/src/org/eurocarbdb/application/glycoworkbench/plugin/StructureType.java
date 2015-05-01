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
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

public class StructureType implements FragmentSource {    

    //
    
    String  database = "";
    String  type = "";
    String  source = "";
    String  structure = "";

    //--

    public StructureType() {
    }

    public StructureType(String d, String t, String s, String g) {
    database = "" + d;
    type = "" + t;
    source = "" + s;
    structure = "" + g.split("\\$")[0]; // remove mass opt
    }   

    public StructureType(String init) throws Exception {
    Vector<String> tokens = TextUtils.tokenize(init,"\t");
    if( tokens.size()!=4 ) 
        throw new Exception("Invalid string format: " + init);

    database  = tokens.elementAt(0);
    type      = tokens.elementAt(1);
    source    = tokens.elementAt(2);    
    structure = tokens.elementAt(3).split("\\$")[0]; // remove mass opt;
    }

    public boolean equals(Object o) {
    if( o==null || !(o instanceof StructureType) )
        return false;
    
    StructureType other = (StructureType)o;
    return (other.database.equals(this.database) &&
        other.type.equals(this.type) &&
        other.source.equals(this.source) &&
        other.structure.equals(this.structure));
    }

    public int hashCode() {
    return database.hashCode() + type.hashCode() + source.hashCode() + structure.hashCode();    
    }

    public StructureType clone(String dict_name) {
    if( database==null || database.length()==0 || database.equals("unknown") )
        return new StructureType(dict_name,type,source,structure);
    else
        return new StructureType(database,type,source,structure);
    }

    public String getDatabase() {
    return database;
    }

    public String getType() {
    return type;
    }

    public void setType(String t) {
    type = t;
    }

    public String getSource() {
    return source;
    }

    public void setSource(String s) {
    source = s;
    }
    
    public String getStructure() {
    return structure;
    }

    public void setStructure(String s) {
    structure = s;
    }

    public boolean merge(StructureType other) {
    if( other==null )
        return false;
    if( !other.structure.equals(this.structure) )
        return false;
    
    boolean changed = false;
    if( !other.type.equals("unknown") && !TextUtils.tokenize(type,",").contains(other.type) ) {
        if( type.equals("unknown") )
        type = other.type;
        else
        type += ", " + other.type;
        changed = true;
    }

    if( !other.source.equals("unknown") && !TextUtils.tokenize(source,",").contains(other.source) ) {
        if( source.equals("unknown") )
        source = other.source;
        else
        source += ", " + other.source;
        changed = true;
    }
    return changed;
    }

    public FragmentEntry generateFragmentEntry(MassOptions mass_opt) throws Exception {
    Glycan g = generateStructure(mass_opt);
    FragmentEntry ret = new FragmentEntry();
    ret.fragment = g;
    ret.name = getDescription();
    ret.mass = g.computeMass();
    ret.mz_ratio = g.getChargesAndExchanges().computeMZ(ret.mass);
    ret.structure = structure;
    ret.source = this;
    return ret;
    }
    
    public Glycan generateStructure(ProfilerOptions opt) throws Exception {    
    Glycan ret = Glycan.fromString(structure,new MassOptions());
    ret.setMassOptions(opt.getMassOptions());
    return ret;
    }

    public Glycan generateStructure(MassOptions mass_opt) throws Exception {    
    Glycan ret = Glycan.fromString(structure,new MassOptions());
    ret.setMassOptions(mass_opt);
    return ret;
    }

    public Glycan generateStructure() throws Exception {
    return generateStructure(new MassOptions(MassOptions.NO_DERIVATIZATION));
    }

    public String getDescription() {
    StringBuilder desc = new StringBuilder();

    if( !type.equals("unknown") ) 
        desc.append(type);

    if( !source.equals("unknown") ) {
        if( desc.length()>0 )
        desc.append(": ");

        desc.append(source);
    }

    if( !source.equals("unknown") ) {
        if( desc.length()>0 )
        desc.append(' ');

        desc.append('(');
        desc.append(database);
        desc.append(')');
    }
    
    return desc.toString();
    }    

    static public StructureType fromString(String str) throws Exception {
    String[] tokens = str.split("\t");
    if( tokens.length!=4 )
        throw new Exception("Invalid format");
    
    StructureType ret = new StructureType();
    ret.database = tokens[0];
    ret.type = tokens[1];
    ret.source = tokens[2];
    ret.structure = tokens[3];
    return ret;
    }

    public String toString() {
    return database + "\t" + type + "\t" + source + "\t" + structure;
    }

    static public StructureType fromXML(Node st_node) throws Exception {
    
    StructureType ret = new StructureType();

    // read values
    ret.database = XMLUtils.getAttribute(st_node,"database");
    if( ret.database==null )
        ret.database = "";

    ret.type = XMLUtils.getAttribute(st_node,"type");
    if( ret.type==null )
        ret.type = "";
    
    ret.source = XMLUtils.getAttribute(st_node,"source");
    if( ret.source==null )
        ret.source = "";

    ret.structure = XMLUtils.getAttribute(st_node,"structure");
    if( ret.structure==null )
        ret.structure = "";

    return ret;
    }
  

    public Element toXML(Document document) {
    if( document==null )
        return null;
    
    // create root node
    Element st_node = document.createElement("StructureType");

    // set values
    st_node.setAttribute("database", "" + database);
    st_node.setAttribute("type", "" + type);
    st_node.setAttribute("source", "" + source);
    st_node.setAttribute("structure", "" + structure);
    
    return st_node;
    }

    public static class SAXHandler extends SAXUtils.ObjectTreeHandler {
    
    public boolean isElement(String namespaceURI, String localName, String qName) {
        return qName.equals(getNodeElementName());
    }

    public static String getNodeElementName() {
        return "StructureType";
    }

    protected void initContent(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        super.initContent(namespaceURI,localName,qName,atts);

        StructureType ret = new StructureType();    
        ret.database = stringAttribute(atts,"database","");
        ret.type = stringAttribute(atts,"type","");
        ret.source = stringAttribute(atts,"source","");
        ret.structure = stringAttribute(atts,"structure","");
        
        object = ret;
    }
    }    
}

