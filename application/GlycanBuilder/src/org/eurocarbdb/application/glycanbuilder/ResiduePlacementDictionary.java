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

import java.util.*;
import java.text.*;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import javax.swing.*;

/**
   The dictionary of the residue placements available in the
   application for a certain cartoon notation. Information about
   residue placements is loaded at run time from a configuration
   file. There is a single dictionary for each notation. The style
   dictionaries are stored in the workspace and passed to the
   renderers.

   @see ResiduePlacement

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class ResiduePlacementDictionary {

    
    private Vector<ResiduePlacement> placements = new Vector<ResiduePlacement>();
    
    //---- init

    /**
       Load the dictionary from a configuration file
     */

    public void loadPlacements(String filename) {
    // clear dict
    placements.clear();
    
    try {
        // open file
        java.net.URL file_url = ResiduePlacementDictionary.class.getResource(filename);
        if( file_url==null )
        throw new FileNotFoundException(filename);
               BufferedReader is = new BufferedReader(new InputStreamReader(file_url.openStream()));
        
        // read dictionary
        String line;
        while( (line=is.readLine())!=null ) {
        line = TextUtils.trim(line);
        if( line.length()>0 && !line.startsWith("%") ) 
            placements.add(new ResiduePlacement(line));        
        }

        is.close();
    }
    catch(Exception e) {
        LogUtils.report(e);
        placements.clear();
    }
    }
   
    //------------------
    // Member access

    /**
       Return a residue placement for a specific residue or a default
       one if none is found.
       @param link the linkage from the residue to its parent
       @see ResiduePlacement
    */
    public ResiduePlacement getPlacement(Linkage link) {
    return getPlacement(link.getParentResidue(),link,link.getChildResidue());
    }

    /**
       Return a residue placement for a specific residue or a default
       one if none is found.
       @param link the linkage from the residue to its parent
       @param sticky specify if the parent placement had the sticky
       flag set
       @see ResiduePlacement
    */
    public ResiduePlacement getPlacement(Linkage link, boolean sticky) {
    return getPlacement(link.getParentResidue(),link,link.getChildResidue(),sticky);
    }

    /**
       Return a residue placement for a specific residue or a default
       one if none is found.
       @param parent the parent residue
       @param link the linkage to the parent
       @param child the residue for which the placement should be
       @see ResiduePlacement
    */
    public ResiduePlacement getPlacement(Residue parent, Linkage link, Residue child) {
    for(Iterator<ResiduePlacement> i=placements.iterator(); i.hasNext(); ) {
        ResiduePlacement p = i.next();
        if( p.matches(parent,link,child) ) 
        return p;        
    }
    return new ResiduePlacement();
    }
    
    /**
       Return a residue placement for a specific residue or a default
       one if none is found.
       @param parent the parent residue
       @param link the linkage to the parent
       @param child the residue for which the placement should be
       @param sticky specify if the parent placement had the sticky
       flag set
       @see ResiduePlacement
    */
    public ResiduePlacement getPlacement(Residue parent, Linkage link, Residue child, boolean sticky) {
    for(Iterator<ResiduePlacement> i=placements.iterator(); i.hasNext(); ) {
        ResiduePlacement p = i.next();
        if( p.matches(parent,link,child) ) 
        return (sticky) ?p.getIfSticky() :p;
    }
    return new ResiduePlacement();
    }
}