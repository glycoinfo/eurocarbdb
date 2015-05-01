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

public class GAGDictionary implements Generator {
    
    protected TreeMap<String,GAGType> dictionary = new TreeMap<String,GAGType>();
    
    protected GAGOptions static_options = null;
 
    public GAGDictionary(String filename) {
    loadDictionary(filename);
    }

    public Collection<String> getFamilies() {
    return dictionary.keySet();
    }

    public Collection<GAGType> getTypes() {
    return dictionary.values();
    }

    public GAGType getType(String family) {
    return dictionary.get(family);
    }    

    public GAGType getType(String family, GAGOptions options) {
    GAGType ret = dictionary.get(family);
    if( ret==null )
        return null;

    if( options.ALLOW_UNLIKELY_ACETYLATION )
        ret = ret.allowUnlikelyAcetylation();
    ret = ret.applyModifications(options.MODIFICATIONS);
    return ret;
    }   

    public void setOptions(GAGOptions opt) {
    static_options = opt;
    }

    public Vector<Glycan> getMotifs() {
    return getMotifs(static_options);
    }

    public Vector<Glycan> getMotifs(GAGOptions options) {

    Vector<Glycan> ret = new Vector<Glycan>();

    for( int i=0; i<options.GAG_FAMILIES.length; i++ ) {
        GAGType gt = getType(options.GAG_FAMILIES[i],options);                
        ret.add(gt.getMotifStructure(options));
    }
    
    return ret;
    }

    public void generate(int motif_ind, GeneratorListener listener) {
    GAGType gt = getType(static_options.GAG_FAMILIES[motif_ind],static_options);
    gt.generateStructures(listener,static_options);    
    }

    public FragmentDocument generateStructures() {
    return generateStructures(static_options);
    }

    public FragmentDocument generateStructures(GAGOptions options) {
    FragmentDocument ret = new FragmentDocument();
    
    for( int i=0; i<options.GAG_FAMILIES.length; i++ ) {
        GAGType gt = getType(options.GAG_FAMILIES[i],options);
        
        FragmentCollection fc = gt.generateStructures(options);
        ret.addFragments(gt.getMotifStructure(options),fc);
    }
    
    return ret;
    }

    private void loadDictionary(String filename) {
    // clear dict
    dictionary.clear();
    
    try {
        // open file
        java.net.URL file_url = GAGDictionary.class.getResource(filename);
        if( file_url==null )
        throw new FileNotFoundException(filename);
        BufferedReader is = new BufferedReader(new InputStreamReader(file_url.openStream()));
        
        // read dictionary
        String line;
        while( (line=is.readLine())!=null ) {
        line = TextUtils.trim(line);
        if( line.length()>0 && !line.startsWith("%") ) {
            GAGType type = new GAGType(line);
            dictionary.put(type.getFamily(),type);
        }
        }
    }
    catch(Exception e) {
        LogUtils.report(e);
    }
    }
}

 
