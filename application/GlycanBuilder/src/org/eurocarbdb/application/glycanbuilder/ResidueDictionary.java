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

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;

/**
   The dictionary of all residue types available in the application.
   Information about residue types is loaded at run time from a
   configuration file. The dictionary is a singleton and all
   information has class-wide access.

   @see Residue
   @see ResidueType   
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class ResidueDictionary {
    
    static {
    dictionary = new HashMap<String,ResidueType>();

    superclasses = new Vector<String>();
    direct_residues = new TreeMap<Integer,ResidueType>();
    other_residues = new Vector<ResidueType>();
    all_residues = new Vector<ResidueType>();
    all_residues_map = new HashMap<String,Vector<ResidueType> >();

    initDictionary();
    }
    
    private static HashMap<String,ResidueType>  dictionary;

    private static Vector<String>               superclasses;
    private static TreeMap<Integer,ResidueType> direct_residues;
    private static Vector<ResidueType>          other_residues;
    private static Vector<ResidueType>          all_residues;
    private static HashMap<String,Vector<ResidueType> > all_residues_map;

    // --- Data access

    /**
       Return the residue type with the given name
       @see ResidueType
       @throws Exception if there is no residue type with such a name
     */
    public static ResidueType getResidueType(String type_name) throws Exception{
    ResidueType ret = findResidueType(type_name);
    if( ret==null )
        throw new Exception("Invalid type: <" + type_name + ">");
    return ret;
    }

    /**
       Return <code>true</code> if a residue type with such a name
       exists.
       @see ResidueType
     */
    public static boolean hasResidueType(String type_name) {
    return (findResidueType(type_name)!=null);
    }

    /**
       Return the residue type with the given name or
       <code>null</null> otherwise
       @see ResidueType
     */
    public static ResidueType findResidueType(String type_name) {
    ResidueType ret = dictionary.get(type_name.toLowerCase());
    if( ret!=null )
        return ret;

    if( type_name.indexOf('=')!=-1 ) {
        String[] tokens = type_name.split("=");
        String name = tokens[0];
        double mass = Double.valueOf(tokens[1].substring(0,tokens[1].length()-1));
        return ResidueType.createOtherReducingEnd(name,mass);
    }
        
    return null;
    }
   
    /**
       Return an iterator over all residue types.
     */
    public static Iterator<ResidueType> iterator() {
    return all_residues.iterator();
    }
    
    protected static Collection<ResidueType> directResidues() {
    return direct_residues.values();
    }

    protected static Collection<ResidueType> otherResidues() {
    return other_residues;
    }

    /**
       Return the collection of all residue types.
     */
    public static Collection<ResidueType> allResidues() {
    return all_residues;
    }

    /**
       Return the collection of all residue classes.
     */
    public static Collection<String> getSuperclasses() {
    return superclasses;
    }

    /**
       Return the collection of all residue types in a specific
       class.
     */
    public static Collection<ResidueType> getResidues(String superclass) {
    return all_residues_map.get(superclass);
    }

    /**
       Return the collection of all reducing end marker types.
     */
    public static Collection<ResidueType> getReducingEnds() {
    Vector<ResidueType> ret = new Vector<ResidueType>();    
    for( ResidueType rt : all_residues ) {
        if( rt.canBeReducingEnd() ) 
        ret.add(rt);
    }
    return ret;
    }

    /**
       Return the collection of all names of reducing end marker
       types.
     */
    public static Collection<String> getReducingEndsString() {
    Vector<String> ret = new Vector<String>();    
    for( ResidueType rt : all_residues ) {
        if( rt.canBeReducingEnd() ) 
        ret.add(rt.getName());
    }
    return ret;
    }


    //

    /**
       Create a new residue of a type specified by its name.
       @throws Exception if no residue type with the specified name
       exists
     */
    public static Residue newResidue(String type_name) throws Exception {
    return new Residue(getResidueType(type_name));
    }

    /**
       Create a free reducing end marker.
       @see ResidueType#createFreeReducingEnd
     */
    public static Residue createReducingEnd() {
    return new Residue(findResidueType("freeEnd"));
    }

    /**
       Create a begin repeat block residue.
       @see ResidueType#createStartRepetition
     */
    public static Residue createStartRepetition() {
    return new Residue(findResidueType("#startrep"));
    }

    /**
       Create a end repeat block residue.
       @see ResidueType#createEndRepetition
     */
    public static Residue createEndRepetition() {
    return new Residue(findResidueType("#endrep_?_?"));
    }

    /**
       Create a end repeat block residue with a specified interval.
       @see ResidueType#createEndRepetition
     */
    public static Residue createEndRepetition(String min, String max) {
    return new Residue(ResidueType.createEndRepetition(min,max));
    }

    /**
       Create a bracket residue.
       @see ResidueType#createBracket
     */
    public static Residue createBracket() {
    return new Residue(findResidueType("#bracket"));
    }

    /**
       Create an attach point.
       @see ResidueType#createAttachPoint
     */
    public static Residue createAttachPoint() {
    return new Residue(findResidueType("#attach"));
    }

    /**
       Create a B cleavage.
       @see ResidueType#createBCleavage
     */
    static public Residue createBCleavage() {
    return new Residue(findResidueType("#bcleavage"));
    }

    /**
       Create a C cleavage.
       @see ResidueType#createCCleavage
     */
    static public Residue createCCleavage() {
    return new Residue(findResidueType("#ccleavage"));
    }

    /**
       Create a Y cleavage.
       @see ResidueType#createYCleavage
     */
    static public Residue createYCleavage() {
    return new Residue(findResidueType("#ycleavage"));
    }

    /**
       Create a Z cleavage.
       @see ResidueType#createZCleavage
     */
    static public Residue createZCleavage() {
    return new Residue(findResidueType("#zcleavage"));
    }

    /**
       Create a labile cleavage.
       @see ResidueType#createLCleavage
     */
    static public Residue createLCleavage() {
    return new Residue(findResidueType("#lcleavage"));
    }

    //---- init

    private ResidueDictionary() {}

    /**
       Load the residue types from a configuration file.
     */
    public static void loadDictionary(String filename) {
    // clear dict
    initDictionary();

    superclasses.clear();
    direct_residues.clear();
    other_residues.clear();
    all_residues.clear();
    all_residues_map.clear();
    
    try {
        // open file
        java.net.URL file_url = ResidueDictionary.class.getResource(filename);
        if( file_url==null )
        throw new FileNotFoundException(filename);
        BufferedReader is = new BufferedReader(new InputStreamReader(file_url.openStream()));
        
        // read dictionary
        String line;
        while( (line=is.readLine())!=null ) {
        line = TextUtils.trim(line);
        if( line.length()>0 && !line.startsWith("%") ) 
            add(new ResidueType(line));        
        }

        is.close();
    }
    catch(Exception e) {
        LogUtils.report(e);
        dictionary.clear();
    }
    }

    private static void add(ResidueType type) {
    dictionary.put(type.getName().toLowerCase(),type);

    for( String s : type.getSynonyms() )
        dictionary.put(s.toLowerCase(),type);

    // add superclass
    String superclass = type.getSuperclass();
    if( all_residues_map.get(superclass)==null ) {
        superclasses.add(superclass);
        all_residues_map.put(superclass,new Vector<ResidueType>());
    }

    // collect residues
    if( type.getToolbarOrder()!=0 )
        direct_residues.put(type.getToolbarOrder(),type);
    else
        other_residues.add(type);
    all_residues.add(type);
    all_residues_map.get(superclass).add(type);    
    }

    private static void initDictionary() {
    dictionary.clear();
    
    add(new ResidueType());
    add(ResidueType.createAttachPoint());
    add(ResidueType.createBracket());
    add(ResidueType.createStartRepetition());
    add(ResidueType.createEndRepetition());
    add(ResidueType.createBCleavage());
    add(ResidueType.createCCleavage());
    add(ResidueType.createYCleavage());
    add(ResidueType.createZCleavage());
    add(ResidueType.createLCleavage());
    if( dictionary.get("freeEnd")==null )
        add(ResidueType.createFreeReducingEnd());
    }
        

}
