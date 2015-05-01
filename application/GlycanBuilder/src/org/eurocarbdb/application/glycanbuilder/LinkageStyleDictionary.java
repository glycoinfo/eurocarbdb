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
import java.util.Vector;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;

/**
   The dictionary of the linkage styles available in the application
   for a certain cartoon notation. Information about linkage styles is
   loaded at run time from a configuration file. There is a single
   dictionary for each notation. The style dictionaries are stored in
   the workspace and passed to the renderers.

   @see LinkageStyle

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class LinkageStyleDictionary {
    
    private Vector<LinkageStyle> styles = new Vector<LinkageStyle>();
        
    //---- init

    /**
       Load the dictionary from a configuration file
     */
    public void loadStyles(String filename) {
    // clear dict
    styles.clear();
    
    try {
        // open file
        java.net.URL file_url = LinkageStyleDictionary.class.getResource(filename);
        if( file_url==null )
        throw new FileNotFoundException(filename);
        BufferedReader is = new BufferedReader(new InputStreamReader(file_url.openStream()));
        
        // read dictionary
        String line;
        while( (line=is.readLine())!=null ) {
        line = TextUtils.trim(line);
        if( line.length()>0 && !line.startsWith("%") ) {
            LinkageStyle toadd = new LinkageStyle(line);
            styles.add(toadd);
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
       Return a residue style with a give identifier or a default one
       (simple straight line to represent an edge) if none is found.
       @param link the linkage for which the style should be
       retrieved, the information about parent residue, and child
       residue is also used
       @see LinkageStyle
     */
    public LinkageStyle getStyle(Linkage link) {
    return getStyle(link.getParentResidue(),link,link.getChildResidue());
    }

    /**
       Return a residue style with a give identifier or a default one
       (simple straight line to represent an edge) if none is found.
       @param parent the parent residue in the linkage
       @param link the linkage for which the style should be retrieved
       @param child the child residue in the linkage       
       @see LinkageStyle#matches
     */
    public LinkageStyle getStyle(Residue parent, Linkage link, Residue child) {        
    for(Iterator<LinkageStyle> i=styles.iterator(); i.hasNext(); ) {
        LinkageStyle s = i.next();
        if( s.matches(parent,link,child) ) 
        return s;
    }
    return new LinkageStyle();
    }
    
}
