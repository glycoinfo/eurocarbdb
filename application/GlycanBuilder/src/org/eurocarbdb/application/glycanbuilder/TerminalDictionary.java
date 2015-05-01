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
   The dictionary of all terminal types available in the application.
   Information about terminal types is loaded at run time from a
   configuration file. The dictionary is a singleton and all
   information has class-wide access.

   @see TerminalType   

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class TerminalDictionary {
    
    static {
    dictionary = new TreeMap<String,TerminalType>();
    superclasses = new Vector<String>();
    all_terminals = new Vector<TerminalType>();
    all_terminals_map = new HashMap<String,Vector<TerminalType> >();
    }
    
    private static TreeMap<String,TerminalType>  dictionary;
    private static Vector<String>               superclasses;    
    private static Vector<TerminalType>          all_terminals;
    private static HashMap<String,Vector<TerminalType> > all_terminals_map;

    // --- Data access

    /**
       Return the terminal type with a given identifier.
       @throws Exception if the specified terminal type is not found
     */
    public static TerminalType getTerminalType(String type_name) throws Exception{
    if( dictionary.containsKey(type_name) )
        return dictionary.get(type_name);
    throw new Exception("Invalid type: <" + type_name + ">");
    }
   
    /**
       Return an iterator over the list of terminal types.
     */
    public static Iterator<TerminalType> iterator() {
    return all_terminals.iterator();
    }
    
    /**
       Return the list of classes of all terminal types.
     */
    public static Collection<String> getSuperclasses() {
    return superclasses;
    }

    /**
       Return the list of all terminal types.
     */
    public static Collection<TerminalType> getTerminals() {
    return all_terminals;
    }

    /**
       Return the list of all terminal types of a given class.
     */
    public static Collection<TerminalType> getTerminals(String superclass) {
    return all_terminals_map.get(superclass);
    }

    //

    /**
       Create a new structure from a terminal type with a given identifier.
       @return the root to the subtree.
       @throws Exception if the specified terminal type is not found
     */
    public static Residue newTerminal(String type_name) throws Exception {
    if( type_name.length()>2 && type_name.charAt(1)=='-' )
        return getTerminalType(type_name.substring(2)).newTerminal(type_name.charAt(0));
    return getTerminalType(type_name).newTerminal();
    }
    
    //---- init

    private TerminalDictionary() {}

    public static void loadDictionary(String filename) {
    // clear dict
    dictionary.clear();

    superclasses.clear();
    all_terminals.clear();
    all_terminals_map.clear();
    
    try {
        // open file
        java.net.URL file_url = TerminalDictionary.class.getResource(filename);
        if( file_url==null )
        throw new FileNotFoundException(filename);
        BufferedReader is = new BufferedReader(new InputStreamReader(file_url.openStream()));
        
        // read dictionary
        String line;
        while( (line=is.readLine())!=null ) {
        line = TextUtils.trim(line);
        if( line.length()>0 && !line.startsWith("%") ) {
            TerminalType type = new TerminalType(line);
            dictionary.put(type.getName(),type);

            // collect terminals
            addSuperclass(type.getSuperclass());         
            all_terminals.add(type);
            all_terminals_map.get(type.getSuperclass()).add(type);
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
    for( String sc : superclasses ) 
        if( sc.equals(superclass) )
        return;
    superclasses.add(superclass);
    all_terminals_map.put(superclass,new Vector<TerminalType>());
    }

}