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
   The dictionary of all core types available in the application.
   Information about core types is loaded at run time from a
   configuration file. The dictionary is a singleton and all
   information has class-wide access.

   @see CoreType   

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class CoreDictionary {
    
    static {
    dictionary = new TreeMap<String,CoreType>();
    superclasses = new Vector<String>();
    all_cores = new Vector<CoreType>();
    all_cores_map = new HashMap<String,Vector<CoreType> >();
    }
    
    private static TreeMap<String,CoreType>  dictionary;
    private static Vector<String>               superclasses;    
    private static Vector<CoreType>          all_cores;
    private static HashMap<String,Vector<CoreType> > all_cores_map;

    // --- Data access

    /**
       Return the core type with a given identifier.
       @throws Exception if the specified core type is not found
     */
    public static CoreType getCoreType(String type_name) throws Exception{
    if( dictionary.containsKey(type_name) )
        return dictionary.get(type_name);
    throw new Exception("Invalid type: <" + type_name + ">");
    }
   
    /**
       Return an iterator over the list of core types.
     */
    public static Iterator<CoreType> iterator() {
    return all_cores.iterator();
    }
    
    /**
       Return the list of classes of all core types.
     */
    public static Collection<String> getSuperclasses() {
    return superclasses;
    }

    /**
       Return the list of all core types.
     */
    public static Collection<CoreType> getCores() {
    return all_cores;
    }

    /**
       Return the list of all core types of a given class.
     */
    public static Collection<CoreType> getCores(String superclass) {
    return all_cores_map.get(superclass);
    }

    //

    /**
       Create a new structure from a core type with a given identifier.
       @return the root to the subtree.
       @throws Exception if the specified core type is not found       
     */
    public static Residue newCore(String type_name) throws Exception {
    return getCoreType(type_name).newCore();
    }

    /**
       Create a new structure from a core type with a given identifier.
       @throws Exception if the specified core type is not found       
     */
    public static Glycan newStructure(String type_name, MassOptions mass_opt) throws Exception {
    return new GWSParser().readGlycan(getCoreType(type_name).getStructure(),mass_opt);
    }    

    //---- init

    private CoreDictionary() {}

    /**
       Load the core types from a configuration file.
     */
    public static void loadDictionary(String filename) {
    // clear dict
    dictionary.clear();

    superclasses.clear();
    all_cores.clear();
    all_cores_map.clear();
    
    try {
        // open file
        java.net.URL file_url = CoreDictionary.class.getResource(filename);
        if( file_url==null )
        throw new FileNotFoundException(filename);
        BufferedReader is = new BufferedReader(new InputStreamReader(file_url.openStream()));
        
        // read dictionary
        String line;
        
        while( (line=is.readLine())!=null ) {
        line = TextUtils.trim(line);
        if( line.length()>0 && !line.startsWith("%") ) {
            CoreType type = new CoreType(line);
            dictionary.put(type.getName(),type);

            // collect cores
            addSuperclass(type.getSuperclass());         
            all_cores.add(type);
            all_cores_map.get(type.getSuperclass()).add(type);
        }
        }        

        is.close();
    }
    catch(Exception e) {
        LogUtils.report(e);
        dictionary.clear();
    }
    }
    
    private static void addSuperclass(String superclass) {
    for( Iterator<String> i=superclasses.iterator(); i.hasNext(); ) 
        if( i.next().equals(superclass) )
        return;
    superclasses.add(superclass);
    all_cores_map.put(superclass,new Vector<CoreType>());
    }

}
