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

import java.util.Iterator;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;

/**
   The dictionary of the residue styles available in the application
   for a certain cartoon notation. Information about residue styles is
   loaded at run time from a configuration file. There is a single
   dictionary for each notation. The style dictionaries are stored in
   the workspace and passed to the renderers.

   @see ResidueStyle

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/


public class ResidueStyleDictionary {

    private TreeMap<String,ResidueStyle> styles = new TreeMap<String,ResidueStyle>();

    //---- init

    /**
       Load the dictionary from a configuration file
     */

    public void loadStyles(String filename) {
    // clear dict
    styles.clear();
    
    try {
        // open file
        java.net.URL file_url = ResidueStyleDictionary.class.getResource(filename);
        if( file_url==null )
        throw new FileNotFoundException(filename);
        BufferedReader is = new BufferedReader(new InputStreamReader(file_url.openStream()));
        
        // read dictionary
        String line;
        while( (line=is.readLine())!=null ) {
        line = TextUtils.trim(line);
        if( line.length()>0 && !line.startsWith("%") ) {
            ResidueStyle toadd = new ResidueStyle(line);
            styles.put(toadd.getName(),toadd);
        }
        }

        is.close();
    }
    catch(Exception e) {
        LogUtils.report(e);
        styles.clear();
    }
    }
      
    // --- Data access

    /**
       Return the residue style for a specific residue, or a default
       one (text representation only) if none is found.
       @param node the residue for which the style should be
       retrieved, the type name is used to identify the style
       @see ResidueStyle
     */

    public ResidueStyle getStyle(Residue node) {
    if( node==null )
        return new ResidueStyle();

    ResidueType type = node.getType();
    String type_name = type.getName();

    if( styles.containsKey(type_name) )
        return styles.get(type_name);
    if( type_name.startsWith("#startrep") )
        return ResidueStyle.createStartRepetition();        
    if( type_name.startsWith("#endrep") )
        return ResidueStyle.createEndRepetition();        
    if( type_name.equals("#attach") )
        return ResidueStyle.createAttachPoint();        
    if( type_name.equals("#redend") )
        return ResidueStyle.createReducingEnd();
    if( type_name.equals("#bracket") )
        return ResidueStyle.createBracket();

    if( type_name.startsWith("#acleavage") ) {
        CrossRingFragmentType crt = (CrossRingFragmentType)type;
        return ResidueStyle.createACleavage(crt.getStartPos(),crt.getEndPos());
    }
    if( type_name.equals("#bcleavage") )
        return ResidueStyle.createBCleavage();
    if( type_name.equals("#ccleavage") )
        return ResidueStyle.createCCleavage();

    if( type_name.startsWith("#xcleavage") ) {
        CrossRingFragmentType crt = (CrossRingFragmentType)type;
        return ResidueStyle.createXCleavage(crt.getStartPos(),crt.getEndPos());
    }
    if( type_name.equals("#ycleavage") )
        return ResidueStyle.createYCleavage();
    if( type_name.equals("#zcleavage") )
        return ResidueStyle.createZCleavage();
    if( type.isCustomType() ) 
        return ResidueStyle.createText(type.getResidueName());    

    return new ResidueStyle();
    }
     
    /**
       Return an iterator of all residue styles contained in the
       dictionary.
     */

    public Iterator<ResidueStyle> iterator() {
    return styles.values().iterator();
    }

}