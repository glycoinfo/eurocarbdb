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

package org.eurocarbdb.application.glycoworkbench.plugin.grammar;

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;
import java.util.*;
import java.util.regex.*;

public class GrammarTree implements Comparable  {

    static Pattern pattern;    
    static {
    pattern = Pattern.compile("[1-9N\\?][abo\\?][1-3\\?]-[DL\\?]-[a-zA-Z]+");
    }

    public static final String END = "+";

    //
    
    String label = "";
    GrammarTree parent = null;
    Vector<GrammarTree> children = new Vector<GrammarTree>();
    
    // 

    public GrammarTree() {
    }

    public GrammarTree(String _label) {
    label = _label;
    }

    public String getLabel() {
    return label;
    }

    public GrammarTree getParent() {
    return parent;
    }

    public Collection<GrammarTree> getChildren() {
    return children;
    }

    public int getNoChildren() {
    return children.size();
    }

    public GrammarTree getRoot() {
    if( parent!=null )
        return parent.getRoot();
    return this;
    }

    public void addChild(GrammarTree toadd) {
    if( toadd==null )
        return;
    
    children.add(toadd);
    toadd.parent = this;
    }

    public GrammarTree clone() {
    return clone(null,null);
    }
    
    public GrammarTree clone(GrammarTree stop_at, GrammarTree stop_el) {
    if( this==stop_at )
        return stop_el;
    
    GrammarTree ret = new GrammarTree(label);
    for( GrammarTree child : children ) 
        ret.addChild(child.clone(stop_at,stop_el));

    return ret;
    }
 
    public int compareTo(Object o) {
    if( o==null || !(o instanceof GrammarTree))
        return 1;
    return this.toString(true).compareTo(((GrammarTree)o).toString(true));
    }

    //
    
    public boolean hasChildrenTagged() {
    for( GrammarTree child : children ) {
        if( child.isTagged() )
        return true;
    }
    return false;
    }

    public static void tag(GrammarTree gt, String tag, Collection<GrammarTree> to_be_tagged) {
    if( gt==null || to_be_tagged==null )
        return;
    if( to_be_tagged.contains(gt) ) 
        gt.label = addTag(gt.label,tag);
    
    for(GrammarTree child : gt.children )
        tag(child,tag,to_be_tagged);
    }

    public static String removeTag(String str) {
    if( str==null )
        return null;

    int ind = str.indexOf('#');    
    if( ind!=-1 ) 
        return str.substring(0,ind);
    return str;
    }

    public static String addTag(String str, String tag) {
    if( str.equals(END) )
        return str;
    return removeTag(str) + "#" + tag;
    }

    public boolean isTagged() {
    if( label==null )
        return false;
    return (label.indexOf('#')!=-1);
    }

    // serialization

    public static GrammarTree fromGlycan(Glycan structure, boolean add_info) throws Exception {
    if( structure.isFuzzy() )
        throw new Exception("Cannot convert a fuzzy structure into grammar tree");
    if( structure.isFragment() )
        throw new Exception("Cannot convert a fragmented structure into grammar tree");
    if( structure.hasRepetition() )
        throw new Exception("Cannot convert a repeating structure into grammar tree");

    return toGrammarTree(structure.getRoot(),add_info);
    }

    public Glycan toGlycan(MassOptions default_mass_options) throws Exception {
    return new Glycan(fromGrammarTree(this),true,default_mass_options);
    }

    public String toString() {
    return toString(false);
    }

    public String toString(boolean ordered) {
    StringBuilder sb = new StringBuilder();

    sb.append("(");
    sb.append(label);

    if( ordered) {
        // convert children to string and sort     
        LinkedList<String> sortedList = new LinkedList<String>();
        for( GrammarTree child : children ) {
        String str_child = child.toString(ordered);
        
        int index = Collections.binarySearch(sortedList, str_child);   
        if (index < 0) 
            sortedList.add(-index-1, str_child);
        else
            sortedList.add(index, str_child);    
        }
    
        // write children
        for( String child : sortedList ) 
        sb.append(child);
    }
    else {
        // write children directl
        for( GrammarTree child : children ) 
        sb.append(child.toString(ordered));
    }
    
    sb.append(")");

    return sb.toString();
    }

    private static GrammarTree toGrammarTree(Residue current, boolean add_info) {    
    if( current==null )
        return null;

    GrammarTree ret = new GrammarTree();
    
    // set label for current           
    ret.label="";
    if( current.isReducingEnd() && !current.isSaccharide() ) 
        ret.label = END;
    else {
        if( add_info ) {
        ret.label += current.getParentLinkage().getParentPositionsSingle();
        ret.label += current.getAnomericState();
        ret.label += current.getAnomericCarbon();
        ret.label += "-";
        ret.label += current.getChirality();
        ret.label += "-";
        }
        ret.label += current.getTypeName();
    }

    // add children
    for( Linkage l : current.getChildrenLinkages() ) 
        ret.addChild(toGrammarTree(l.getChildResidue(),add_info));

        
    return ret;
    }

    private static int countOpenPar(String str) {

    int count = 0;
    char[] buf = str.toCharArray();
    for( int i=0; i<buf.length; i++ )
        if( buf[i]=='(' )
        count++;
    return count;
    }
    

    private static int countClosePar(String str) {

    int count = 0;
    char[] buf = str.toCharArray();
    for( int i=0; i<buf.length; i++ )
        if( buf[i]==')' )
        count++;
    return count;
    }
    
    public static GrammarTree fromString(String str) throws Exception {
    
    str = TextUtils.trim(str);
    if( str==null || str.length()==0 )
        return null;    
    
    GrammarTree ret = new GrammarTree();

    // check parentheses
    if( str.charAt(0)!='(' )
        throw new Exception("Missing start parenthesis: " + str);
    if( str.charAt(str.length()-1)!=')' )
        throw new Exception("Missing end parenthesis: " + str);
    str = str.substring(1,str.length()-1);

    // get label
    int ind = str.indexOf('(');    
    if( ind==-1 )
        ind = str.length();
    ret.label = str.substring(0,ind);
    str = str.substring(ind);

    // parse children
    while(str.length()>0 ) {
        if( str.charAt(0)!='(' )
        throw new Exception("Missing start parenthesis: " + str);
        int ind2 = TextUtils.findEnclosed(str);
        if( ind2==-1 )
        throw new Exception("Missing end parenthesis: " + str);
        
        ret.addChild(fromString(str.substring(0,ind2+1)));
        str = str.substring(ind2+1);
    }

    return ret;
    }

    public static Residue fromGrammarTree(GrammarTree gt) throws Exception {
        
    // parse residue type;
    Residue ret = null;

    if( gt.label.equals(END) ) 
        ret = new Residue(ResidueType.createFreeReducingEnd());    
    else {
        // remove tag
        String type = removeTag(gt.label);

        // parse info
        Matcher m = pattern.matcher(type);
        if( m.matches() ) {
        ret = ResidueDictionary.newResidue(type.substring(6));
        ret.setParentLinkage(new Linkage(null,ret,type.charAt(0)));
        ret.setAnomericState(type.charAt(1));
        ret.setAnomericCarbon(type.charAt(2));
        ret.setChirality(type.charAt(4));
        }
        else {
        ret = ResidueDictionary.newResidue(type);    
        ret.setParentLinkage(new Linkage(null,ret));
        }
    }

    // parse children
    for( GrammarTree gtc : gt.children ) {     
        Residue child = fromGrammarTree(gtc);
        ret.addChild(child,child.getParentLinkage().getParentPositionsSingle());
    }

    return ret;
    }
}
