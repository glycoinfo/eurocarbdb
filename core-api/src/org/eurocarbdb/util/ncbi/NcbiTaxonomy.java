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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/

package org.eurocarbdb.util.ncbi;

import java.net.URL;
import java.io.Serializable;
import org.apache.log4j.Logger;
import org.eurocarbdb.dataaccess.Eurocarb;

/*  class NcbiTaxonomy  *//******************************************
*
*   Represents a single taxonomy entry in the 
*   {@link http://www.ncbi.nlm.nih.gov/Taxonomy/taxonomyhome.html/ 
*   NCBI Taxonomy database}.
*
*   @see Taxonomy
*   @author mjh [glycoslave@gmail.com]
*/
public class NcbiTaxonomy implements Serializable
{
    /** Logging handle. */
    private static final Logger log = Logger.getLogger( NcbiTaxonomy.class );

    /** Default query URL with which to lookup taxonomy entries */
    public static final String Default_Query_Url
        = "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=";
    
    /** Name of the Eurocarb property that defines a query URL 
    *   for looking up NCBI entries. */
    public static final String Eurocarb_Property_Name = "ncbi.query.url";
        
    
    /** NCBI id */
    private int ncbiId;

    
    public NcbiTaxonomy() {}
    
    
    public NcbiTaxonomy( int ncbi_id ) 
    {  
        this.setNcbiId( ncbi_id );  
    }
    
    
    /** Returns the NCBI Id for this entry */
    public int getNcbiId()
    {
        return ncbiId;   
    }
    
    
    /**
    *   Returns an {@link URL} that links directly to this NCBI entry.
    *   The base URL returned by this method is determined from the 
    *   value of {@link #Eurocarb_Property_Name}, or from 
    *   {@link #Default_Query_Url} if not defined.
    */
    public URL getUrl()
    {
        return getUrl( this.ncbiId );
    }
    
    
    /**
    *   Returns an {@link URL} that links directly to the given NCBI id.
    *   The base URL returned by this method is determined from the 
    *   value of {@link #Eurocarb_Property_Name}, or from 
    *   {@link #Default_Query_Url} if not defined.
    */
    public static final URL getUrl( final int ncbiId )
    {
        assert ncbiId > 0;
        String base_url = Eurocarb.getProperty( Eurocarb_Property_Name );
        if ( base_url == null || base_url.length() == 0 )
        {
            //  complain long, hard, and frequently if missing
            log.warn( "Missing or zero-length value for property '"
                    + Eurocarb_Property_Name
                    + "', using hard-wired default value '"
                    + Default_Query_Url
                    + "'..."
                    );
            
            base_url = Default_Query_Url;
        }
        
        try {  return new URL( base_url + ncbiId );  }
        catch ( java.net.MalformedURLException e )
        {
            log.warn( "Invalid URL '" + base_url + ncbiId + "'", e );
            return null;
        }
    }
    
    
    /** Sets the NCBI Id for this entry */
    void setNcbiId( int id )
    {
        ncbiId = id;   
    }
    
} // end class


