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

package org.eurocarbdb.application.glycoworkbench.plugin.peakpicker;
import org.eurocarbdb.application.glycanbuilder.*;
import org.eurocarbdb.application.glycoworkbench.*;

/**
   Management and storage of INI files.
   
   This class provides a means to associate string names to int/double/string values.
   It also supports hierarchical data and to save/load the contained data as XML.
   Hierachy levels are separated from each other and from the name by colons. 
   Example: 'common:file_options:default_file_open_path = /share/'
   
   In addition to the Type-Name-Value tuples descriptions can be added to each secection and value.
   See the setValue methods and setDescription(String). Newline characters in the description are
   possible.
   
   In the XML representation only the types 'int', 'string' ,'float' and 'double' are available.
   
   @see DefaultParamHandler
   
*/

import java.util.*;
import java.io.*;

public class Param {

    /// internal storage containers
    protected HashMap<String, Object> values_;
    protected HashMap<String, String> descriptions_;
    
    /**
       Maximum number of inheritance steps allowed.             
       Usually you really won't care about this, thus I don't provide accessor functions. (Clemens)
    */
    public int inheritance_steps_max;
    
    // ----
    
    
    /** Constructors and Destructors
     */
    
    /// Default construtor
    public Param() {
    values_ = new HashMap<String,Object>();
    descriptions_ = new HashMap<String,String>();
    inheritance_steps_max = 15;
    }
    
    /// Copy constructor
    public Param clone() {
    Param ret = new Param();
    ret.copy(this);
    return ret;
    }
    
    /// Assignment operator
    public void copy(Param rhs) {
    values_ = (HashMap<String,Object>)rhs.values_.clone();
    descriptions_ = (HashMap<String,String>)rhs.descriptions_.clone();
    }
    
    /// Equality operator
    public boolean equals(Param rhs) {
    return values_.equals(rhs.values_);
    }
    
    public Map<String,Object> getValues() {
    return values_;
    }

    /// Set a value.
    public void setValue(String key, Object value) {
    setValue(key,value,"");
    }
    
    public void setValue(String key, Object value, String description) {
    values_.put(key,value);
    setDescription(key,description);
    }
    
    /**
       Get a value by its key.
       
       To check if there is no value for the given key, compare the return value with DataValue::EMPTY
    */
    public Object getValue(String key) {
    return values_.get(key);
    }
            
    /**
       Sets a description for a key (section or actual value).             
        The description is only set when a corresponding section or value exists.
    */
    public void setDescription(String location, String description) {
    if( description!=null && description.length()>0 )
        descriptions_.put(location,description);        
    }
    
    /**
       Get a description by its key.
       To check if there is no description for the given key an empty string is returned.
    */
    public String getDescription(String key) {
    String ret = descriptions_.get(key);
    return ( ret==null ) ?"" :ret;
    }
    
    ///Returns the number of entries (leafs).
    public int size() {
    return values_.size();
    }
    
    ///Returns if there are no entries.
    public boolean empty() {
    return values_.size()==0;
    }
    
    /// Deletes all entries
    public void clear() {
    values_.clear();
    descriptions_.clear();
    }
    
    ///Insert all values of @p para and adds the prefix @p prefix.
    public void insert(String prefix, Param para) {
    if (prefix==null || prefix.length()==0 ) {
        for( Map.Entry<String,Object> e : para.values_.entrySet() )
        values_.put(e.getKey(),e.getValue());                
        for( Map.Entry<String, String> e : para.descriptions_.entrySet() )
        descriptions_.put(e.getKey(),e.getValue());
    }        
    else {
        if( !prefix.endsWith(":") )
        prefix = prefix + ':';
        
        for( Map.Entry<String,Object> e : para.values_.entrySet() )    
        values_.put(prefix + e.getKey(),e.getValue());    
        for( Map.Entry<String, String> e : para.descriptions_.entrySet() )
        descriptions_.put(prefix + e.getKey(),e.getValue());                
    }
    }
    
    
    ///Remove all entries that start with @p prefix.
    public void remove(String prefix) {
    
    for( Iterator<Map.Entry<String,Object>> it = values_.entrySet().iterator(); it.hasNext(); )    {
        Map.Entry<String,Object> e = it.next();
        if( e.getKey().startsWith(prefix) )
        it.remove();
    }
    
    for( Iterator<Map.Entry<String,String>> it = descriptions_.entrySet().iterator(); it.hasNext(); )    {
        Map.Entry<String,String> e = it.next();
        if( e.getKey().startsWith(prefix) )
        it.remove();
    }
    }
            
    
    public void setDefaults(Param defaults) {
    setDefaults(defaults,"",false);
    }
    
    
    public void setDefaults(Param defaults, String prefix) {
    setDefaults(defaults,prefix,false);
    }
    
    /**
       Insert all values of @p para and adds the prefix @p prefix, if the values are not already set.
       
       @param defaults The default values. 
       @param prefix The prefix to add to all defaults. 
       @param showMessage If <tt>true</tt> each default that is actually set is printed to stdout as well.
    */

    public void setDefaults(Param defaults, String prefix, boolean showMessage) {
    
    if( prefix!=null && prefix.length()>0 && !prefix.startsWith(":") )
        prefix = prefix + ":";
    
    for(Map.Entry<String,Object> e : defaults.values_.entrySet() ) {        
        if( values_.get(prefix+e.getKey())==null ) {
        if (showMessage)
            System.out.println("Setting " + (prefix+e.getKey()) + " to " + e.getValue());
        values_.put(prefix+e.getKey(),e.getValue());
        }
    }
    
    for(Map.Entry<String,String> e : defaults.descriptions_.entrySet() ) {
        if( descriptions_.get(prefix+e.getKey())==null ) 
        descriptions_.put(prefix+e.getKey(),e.getValue());
    }
    }
    
    
    
    public void checkDefaults(String name, Param defaults)  {
    checkDefaults(name,defaults,"",System.out);
    }
    
    public void checkDefaults(String name, Param defaults, String prefix ) {
    checkDefaults(name,defaults,prefix,System.out);
    }
    
    /**
       Warns if a parameter is present for which no default value is specified.
       
       @param name A name that is displayed in error messages.
       @param defaults The default values. 
       @param prefix The prefix where to check for the defaults. 
       @param os The output stream for the warnings.
    */

    public void checkDefaults(String name, Param defaults, String prefix, PrintStream os) {
    
    //Extract right parameters
    HashMap<String,Object> check_values;
    HashMap<String,String> check_descriptions;
    
    if ( prefix==null || prefix.length()>0 )    {
        check_values = values_;
        check_descriptions=descriptions_;
    }    
    else {
        if( !prefix.endsWith(":") )
        prefix = prefix + ":";
        
        Param copied = this.copy(prefix,true);
        check_values = copied.values_;
        check_descriptions = copied.descriptions_;
    }
    
    //check
    for(Map.Entry<String,Object> e : check_values.entrySet() ) {
        if( defaults.values_.get(e.getKey())==null ) {
        os.print("Warning: " + name + " received the unknown parameter '" + e.getKey() + "'");
        if( prefix.length()>0 ) 
            os.print( " in '" + prefix + "'");
        os.println("!");
        }
    }
    }
    
    
    public Param copy(String prefix) {
    return copy(prefix,false,"");
    }
    
    public Param copy(String prefix, boolean remove_prefix) {
    return copy(prefix,remove_prefix,"");
    }

    /**
       Returns a new Param object containing all entries that start with @p prefix.
       
       @param prefix should contain a ':' at the end if you want to extract a subtree.
       Otherwise not only nodes, but as well values with that prefix are copied.
       @param remove_prefix indicates if the prefix is removed before adding entries to the new Param
       @param new_prefix is added to the front of all keys
    */
        
    public Param copy(String prefix, boolean remove_prefix, String new_prefix) {
    if( new_prefix.length()>0 && !new_prefix.endsWith(":") )
        new_prefix = new_prefix + ":";
    
    Param out = new Param();
    String key;
    for( Map.Entry<String,Object> e : values_.entrySet() ) {
        if( e.getKey().startsWith(prefix) ) {
        
        //remove old prefix
        if (remove_prefix)            
            key = e.getKey().substring(prefix.length(),e.getKey().length() - prefix.length());            
        else
            key = e.getKey();
        
        // add new prefix
        if( new_prefix.length()>0 )            
            key = new_prefix + key;            
        
        out.values_.put(key,e.getValue());
        }
    }
    
    for( Map.Entry<String,String> e : descriptions_.entrySet() ) {
        if( e.getKey().startsWith(prefix) ) {
        
        //remove old prefix
        if (remove_prefix)            
            key = e.getKey().substring(prefix.length(),e.getKey().length() - prefix.length());            
        else
            key = e.getKey();
        
        // add new prefix
        if( new_prefix.length()>0 )            
            key = new_prefix + key;            
        
        out.descriptions_.put(key,e.getValue());
        }
    }
    
    return out;
    }
    
    /* 
    Like copy(), but with support for "inherit" items.
    
    Inheritance is considered for "nodes" only, i.e. if old_prefix ends
    with ':'.  The old_prefix is <em>always</em> removed and replaced with
    new_prefix.  (Keeping old_prefix seems to make no sense in combination
    with inheritance.)
    */
    //Param copyWithInherit(const String& old_prefix, const String& new_prefix="") const;
    
    ///Write XML file.
    //void store(const String& filename) const throw (Exception::UnableToCreateFile);
    ///Read XML file.
    //void load(const String& filename) throw (Exception::FileNotFound,Exception::ParseError);
    
    /*
       Parses command line arguments.
       
       This method discriminates three types of arguments:<BR>
       (1) options (starting with '-') that have a text argument<BR>
       (2) options (starting with '-') that have no text argument<BR>
       (3) text arguments (not starting with '-')
       
       Command line arguments '-a avalue -b -c bvalue misc1 misc2' would be stored like this:<BR>
       "prefix:-a" -> "avalue"<BR>
       "prefix:-b" -> ""<BR>
       "prefix:-c" -> "bvalue"<BR>
       "prefix:misc" -> "misc1 misc2"<BR>
       
       @param argc argc variable from command line
       @param argv argv varaible from command line
       @param prefix prefix for all options
    */
    //void parseCommandLine(const int argc , char** argv, String prefix = "");
    
    /*
       Parses command line arguments to specified key locations.
       
       @param argc argc variable from command line
       @param argv argv varaible from command line
       @param options_with_argument a map of options that are followed by an argument (with key where they are stored)
       @param options_without_argument a map of options that are not followed by an argument (with key where they are stored). Present options are set to the the string 'true'.
       @param misc key where all non-option arguments are stored
       @param unknown key where all unknown options are stored
    */
    //void parseCommandLine(const int argc , char** argv, const std::map<String, String>& options_with_argument, const std::map<String, String>& options_without_argument, const String& misc="misc", const String& unknown="unknown");        
    

    public void outputValues() {
    for( Map.Entry<String,Object> e : values_.entrySet() ) {
        System.out.println( e.getKey() + " = " + e.getValue().toString());
    }
    }

    public void store(String name, Configuration config) {
    for( Map.Entry<String,Object> e : values_.entrySet() ) 
        config.put(name,clean(e.getKey()),e.getValue());
    }

    public void retrieve(String name, Configuration config) {    
    for( Map.Entry<String,Object> e : values_.entrySet() ) 
        setValue(e.getKey(),config.get(name,clean(e.getKey()),e.getValue()));    
    }

    private String clean(String str) {
    return str.replace(':','_');
    }

}


