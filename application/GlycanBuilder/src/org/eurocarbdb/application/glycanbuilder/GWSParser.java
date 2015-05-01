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

package org.eurocarbdb.application.glycanbuilder;

import java.util.*;
import java.util.regex.*;

/**
   Read and write glycan structures in the GlycoWorkbench internal
   format.
   
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class GWSParser implements GlycanParser {

    private static Pattern residue_pattern;
    private static Pattern link_pattern;
    
    static {
    String link_old_pattern_str = "-([1-9N\\?])"; 
    String link_pattern_str = "--(?:((?:[1-9N\\?]/)*[1-9N\\?]=[1-9N\\?]),)*((?:[1-9N\\?]/)*[1-9N\\?])";
    link_pattern = Pattern.compile("(?:" + link_old_pattern_str + ")|(?:" + link_pattern_str + ")");

    String start_repeat_str = "\\[";
    String end_repeat_str = "\\](?:\\_([0-9]+))?+(?:\\^([0-9]+))?+"; 
    String residue_str = "([abo\\?][1-9N\\?])?+([DL]-)?+([a-zA-z0-9_#=\\.]+)(?:,([\\?opf]))?+";
    String cleaved_str = "/([a-zA-z0-9_#]+)";
    String place_str = "@(-?[0-9]+s?)";

    residue_pattern = Pattern.compile("(?:" + start_repeat_str + ")|(?:" + end_repeat_str + ")|" +
                      "(?:" + residue_str + "(?:" + cleaved_str + ")?+)" +
                      "(?:" + place_str + ")?+" );
    }
    
    /**
       Default Constructor
     */
    public GWSParser() {
    }

    public void setTolerateUnknown(boolean f) {
    }

    public String writeGlycan(Glycan structure) {
    return toString(structure,false,true);
    }

    /**
       Create a unique representation of a glycan structure using the
       lexical ordering between the children of each residue.
     */
    public String writeGlycanOrdered(Glycan structure) {
    return toString(structure,true,true);
    }

    public Glycan readGlycan(String str, MassOptions default_mass_options) throws Exception {
    return fromString(str,default_mass_options);
    }

    /**
       Static method for creating string representation of glycan
       structures.
       @param structure the structure to be converted
     */
    static public String toString(Glycan structure) {
    return toString(structure,false,true);
    }

    /**
       Static method for creating string representation of glycan
       structures.
       @param structure the structure to be converted
       @param ordered <code>true</code> if the representation must use
       the lexical ordering between children
     */
    static public String toString(Glycan structure, boolean ordered) {
    return toString(structure,ordered,true);
    }

    /**
       Static method for creating string representation of glycan
       structures.
       @param structure the structure to be converted
       @param ordered <code>true</code> if the representation must use
       the lexical ordering between children
       @param add_massopt <code>true</code> if the representation must
       contain the mass options
     */
    static public String toString(Glycan structure, boolean ordered, boolean add_massopt) {
    if( structure==null )
        return "";

    StringBuilder ss = new StringBuilder();
    if( structure.getRoot()!=null ) {
        ss.append(writeSubtree(structure.getRoot(),ordered));
        if( structure.getBracket()!=null ) 
        ss.append(writeSubtree(structure.getBracket(),ordered));
                
        if( add_massopt ) {
        ss.append("$");
        ss.append(structure.getMassOptions().toString());                    
        }
    }
    return ss.toString();

    }

    /**
       Static method for creating glycan structures from their string representation
       @param default_mass_options the mass options to use for the new
       structure if they are not specified in the string
       representation
       @throws Exception if the string cannot be parsed    
     */
    static public Glycan fromString(String str, MassOptions default_mass_options) throws Exception {
    str = TextUtils.trim(str);
    
    // read mass options
    MassOptions mass_opt = default_mass_options.clone();
    int ind1 = str.indexOf('$');
    if( ind1!=-1 ) {
        mass_opt = MassOptions.fromString(str.substring(ind1+1));       
        str = str.substring(0,ind1);
    }
        
    // read structure
    Glycan ret = null;
    int ind2 = str.indexOf('}');
    if( ind2==-1 ) 
        ret = new Glycan(readSubtree(str,true),false,mass_opt);
    else {
        // read structure with bracket
        ret = new Glycan(readSubtree(str.substring(0,ind2),true),
                 readSubtree(str.substring(ind2),true),
                 false,mass_opt);        
    }    

    return ret;
    }

    static protected String writeResidueType(Residue r) {
    String str = "";

    if( r.isBracket() )
        str += '}';
    else if( r.isStartRepetition() ) {
        str += '[';
    }
    else if( r.isEndRepetition() ) {
        str += ']';
        if( r.getType().getMinRepetitions()>=0 )
        str += "_" + r.getType().getMinRepetitions();
        if( r.getType().getMaxRepetitions()>=0 )
        str += "^" + r.getType().getMaxRepetitions();
    }
    else if( r.isCleavage() ) {
        Residue cleaved_residue = r.getCleavedResidue();
        str += writeResidueType(cleaved_residue) + "/" + r.getTypeName();    
    }
    else {
        if( r.hasAnomericState() || r.hasAnomericCarbon() ) 
        str += r.getAnomericState() + "" + r.getAnomericCarbon();        
        if( r.hasChirality() ) 
        str += r.getChirality() + "-";        
        str += r.getTypeName();
        if( r.hasRingSize() ) 
        str += "," + r.getRingSize();            
    }
    
    return str;
    }

    static protected String writeSubtree(Residue r, boolean ordered ) {
    
    //------------
    // write type
    String str = writeResidueType(r);    

    // write placement
    if( r.getCleavedResidue()!=null ) {
        Residue cleaved_residue = r.getCleavedResidue();
        if( cleaved_residue.hasPreferredPlacement() )
        str += "@" + placementToString(cleaved_residue.getPreferredPlacement());    
    }
    else { 
        if( r.hasPreferredPlacement() )
        str += "@" + placementToString(r.getPreferredPlacement());    
    }
    
    //-----------------
    // write children
    
    Vector<String> str_children = new Vector<String>();
    for( Linkage l : r.getChildrenLinkages() )
        str_children.add(writeSubtree(l,ordered));
    
    if( ordered ) 
        Collections.sort(str_children);    

    // add parenthesis    
    for( int i=0; i<r.getChildrenLinkages().size()-1; i++ ) 
        str += "(";       

    // write children
    for( Iterator<String> i=str_children.iterator(); i.hasNext(); ) {
        str += i.next();
        
        // close parenthesis
        if( i.hasNext() ) 
        str += ")";                
    }    

    return str;
    }

    static protected String writeSubtree(Linkage l, boolean ordered) {
    return ("--" + toStringLinkage(l) + writeSubtree(l.getChildResidue(),ordered));
    }

    static protected String toStringLinkage(Linkage link) {        
    StringBuilder sb = new StringBuilder();
    for( Iterator<Bond> i=link.getBonds().iterator(); i.hasNext(); ) {
        Bond b = i.next();

        if( sb.length()>0 ) 
        sb.append(',');
     
        // write parent positions
        char[] p_poss = b.getParentPositions();
        for( int l=0; l<p_poss.length; l++ ) {
        if( l>0 )
            sb.append('/');
        sb.append(p_poss[l]);
        }
        
        // write child position for non-glycosidic bonds
        if( i.hasNext() ) {
        sb.append('=');
        sb.append(b.getChildPosition());
        }
    }        
    return sb.toString();
    }

    static protected Residue readSubtree(String str, boolean accept_empty) throws Exception {    
    if( str.length()==0 ) {
        if( accept_empty ) 
        return null;
        throw new Exception("Empty node");
    }

    Residue ret = null;
    if( str.charAt(0)=='}' ) {
        ret = ResidueDictionary.createBracket();    
        str = str.substring(1);
    }
    else {
        //------------------
        // create residue
    
        Matcher m = residue_pattern.matcher(str);
        if( !m.lookingAt() ) 
        throw new Exception("Invalid format for string: " + str );
        
        if( str.charAt(0)=='[' ) 
        ret = ResidueDictionary.createStartRepetition();       
        else if( str.charAt(0)==']' ) 
        ret = ResidueDictionary.createEndRepetition(m.group(1),m.group(2));        
        else {
        // get stereochemistry
        char ret_anom_state = '?';
        char ret_anom_carbon = '?';
        char ret_chirality = '?';        
        if( m.group(3)!=null ) {
            ret_anom_state = m.group(3).charAt(0);
            ret_anom_carbon = m.group(3).charAt(1);
        }
        if( m.group(4)!=null )
            ret_chirality = m.group(4).charAt(0);
                       
        // get type name
        String typename = m.group(5);
        
        // get ring size
        char ret_ring_size = '?';
        if( m.group(6)!=null ) 
            ret_ring_size = m.group(6).charAt(0);

        // create residue
        ret = ResidueDictionary.newResidue(typename);
        ret.setAnomericState(ret_anom_state);
        ret.setAnomericCarbon(ret_anom_carbon);
        ret.setChirality(ret_chirality);
        ret.setRingSize(ret_ring_size);

        // create cleavage
        String cleavage_typename = m.group(7);
        if( cleavage_typename!=null ) {
            Residue cleavage = null;
            if(  cleavage_typename.indexOf('_')!=-1 ) 
            cleavage = CrossRingFragmentDictionary.newFragment(cleavage_typename,ret);                
            else                
            cleavage = ResidueDictionary.newResidue(cleavage_typename);        

            cleavage.setCleavedResidue(ret);
            ret = cleavage;
        }        
        }

        // get placement
        if( m.group(8)!=null ) {
        ResiduePlacement pref_place = placementFromString(m.group(8));
        if( ret.getCleavedResidue()!=null )
            ret.getCleavedResidue().setPreferredPlacement(pref_place);
        else
            ret.setPreferredPlacement(pref_place);
        }

        str = str.substring(m.end());
    }

    //-----------------
    // parse children

    // skip open parentheses
    int nopars = 0;
    for( ; nopars<str.length() && str.charAt(nopars)=='('; nopars++ );
    str = str.substring(nopars);
    
    // add children
    while(str.length()>0) {
        Linkage child_link = null;
        if( nopars>0 ) {
        // find subtree enclosed in parenthesis
        int ind = TextUtils.findClosedParenthesis(str);
        if( ind==-1 ) throw new Exception("Invalid string format: " + str);
        
        child_link = readSubtreeLinkage(str.substring(0,ind));
        str = str.substring(ind+1);
        nopars--;
        }
        else {
        // add last child
        child_link = readSubtreeLinkage(str); 
        str = "";
        }
        
        // add child
        child_link.setParentResidue(ret);
        ret.getChildrenLinkages().add(child_link);
    }
    
    return ret;
    }

    
    
    static protected Linkage readSubtreeLinkage(String str) throws Exception {
    
    Matcher m = link_pattern.matcher(str);
    if( !m.lookingAt() ) 
        throw new Exception("invalid format for linkage: " + str);

    if( m.group(1)!=null ) {
        // old style

        // parse child
        Residue child = readSubtree(str.substring(m.end()),false);

        // create linkage
        return new Linkage(null,child,m.group(1).charAt(0));
    }
    
    // parse bonds
    Vector<Bond> ret_bonds = new Vector<Bond>();    
    for( int i=2; i<=m.groupCount(); i++ ) {
        String str_bond = m.group(i);
        if( i<m.groupCount() ) {
        // parse non glyco bonds
        if( str_bond!=null && str_bond.length()>0 ) {
            String[] fields = str_bond.split("=");
            char[] p_poss = parsePositions(fields[0]);
            char   c_pos  = fields[1].charAt(0);
            ret_bonds.add(new Bond(p_poss,c_pos));
        }

        }
        else {
        // parse glyco bond
        char[] p_poss = parsePositions(str_bond);
        ret_bonds.add(new Bond(p_poss,'?')); // anomeric carbon position is added later
        }
    }
    
    // parse child
    Residue child = readSubtree(str.substring(m.end()),false);
    
    // create linkage
    Linkage ret = new Linkage(null,child);
    ret.setBonds(ret_bonds);
    ret.getChildResidue().setParentLinkage(ret);

    return ret;
    }


    static private char[] parsePositions(String str) throws Exception {
    int c = 0;
    char[] ret = new char[(str.length()+1)/2];
    for( int i=0; i<str.length(); i+=2 ) {
        if( i>0 && str.charAt(i-1)!='/' )
        throw new Exception("Invalid positions string: " + str);
        ret[c++] = str.charAt(i);
    }
    return ret;
    }    


    static private String placementToString(ResiduePlacement rp) {
    if( rp==null )
        return "";

    String str = "" + (rp.getPositions()[0].getIntAngle()+360);
    if( rp.isSticky() )
        str += 's';
    return str;
    }

    static private ResiduePlacement placementFromString(String str) throws Exception {
    if( str.length()==0 )
        return null;

    boolean _sticky = false;
    if( str.charAt(str.length()-1)=='s' ) {
        _sticky = true;
        str = str.substring(0,str.length()-1);
    }

    return new ResiduePlacement(new ResAngle(Integer.parseInt(str)),false,_sticky);
    }
   

}
