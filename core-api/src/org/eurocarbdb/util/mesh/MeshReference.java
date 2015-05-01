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
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/

package org.eurocarbdb.util.mesh;

//  3rd party imports 
import org.apache.log4j.Logger;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.eurocarbdb.dataaccess.core.Reference;

import org.eurocarbdb.dataaccess.BasicEurocarbObject;

/**
*   Abstract base class for data objects based on 
*   {@link http://www.nlm.nih.gov/mesh/ MeSH}. 
*   @author mjh
*/
public abstract class MeshReference extends BasicEurocarbObject
{
    /*  enum Category  *//*******************************************
    *
    *   Enumeration of known MeSH categories.
    *
    */
    public enum Category
    {
        Anatomy( 'A', "Anatomy" ),
        Organisms( 'B', "Organisms" ),
        Diseases( 'C', "Diseases" ),
        Chemicals_Drugs( 'D', "Chemicals and Drugs" )
        
        /* there are others but they're just not listed here */
        ;
        
        public final char id;
        public final String name;
        
        Category( char id, String name ) {  this.id = id; this.name = name;  }
    }

    /** Logging handle. */
    static final Logger log = Logger.getLogger( MeshReference.class );

    //~~~ STATIC FIELDS ~~~//

    /** Pattern to match an embedded MeSH reference. This is typically
    *   just a sequence of one or more upper-case words LIKE THIS. These
    *   are references to other MeSH entries, and are typically only found
    *   in the DESCRIPTION field of an entry.  
    */
    public static final Pattern Regexp_Mesh_Reference 
        = Pattern.compile("\\b([A-Z]{4,}(?:[\\s-]?[A-Z]{3,})*)\\b");



    //~~~ FIELDS ~~~//

    //private String uniqueId;
    
    protected Category category = null; // TODO


    //~~~ STATIC METHODS ~~~//

    /*  markupMeshReferencesAsHTML  *//****************************** 
    *
    *   Returns a HTML marked-up version of the embedded MeSH references
    *   found in a passed string.
    *   @see #Regexp_Mesh_Reference
    */
    public static final String markupMeshReferencesAsHTML( String string )
    {
        assert string != null;
        if ( string.length() == 0 ) return string;
    
        Matcher m = Regexp_Mesh_Reference.matcher( string );
        StringBuffer sb = new StringBuffer();
        
        while ( m.find() )
        {
            String s = m.group().toLowerCase();
            try 
            {
                m.appendReplacement( 
                    sb, 
                    "<a href=\"http://www.nlm.nih.gov/cgi/mesh/2006/MB_cgi?term="
                    + java.net.URLEncoder.encode( s, "UTF-8" )
                    + "\" title=\"MeSH reference for '" 
                    + s
                    + "'\" >"
                    + s
                    + "</a>"
                );
            }
            catch ( java.io.UnsupportedEncodingException fuck_you_java )
            {
                //log.fatal( fuck_you_java );   
            }
        }
        m.appendTail( sb );        
       
        return sb.toString();
    }

    
    //~~~ CONSTRUCTORS ~~~//


    //~~~ METHODS ~~~//
    
    /*  getBriefDescription  *//*************************************
    *
    *   Returns a single sentence description of this disease.
    *   @see #getDescription()
    */
    public String getBriefDescription() 
    {
        String desc = this.getDescription();
        if ( desc == null || desc.length() == 0 ) return "";
        
        int fullstop = desc.indexOf('.');
        
        return ( fullstop == -1 ) ? desc : desc.substring( 0, fullstop );
    }
    
    
    public abstract String getDescription();

    /*  @see BiologicalContextAssociation#getExternalReference  */    
    public Reference getExternalReference()
    {
        //  TODO - return a MeshReference object here
        return null;   
    }

    
    public abstract String getMeshId();
    
    
} // end class


