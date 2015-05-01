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

package org.eurocarbdb.util;

import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

/** Represents a numerical version of some other entity. */
public final class Version implements Comparable
{
    public static final SimpleDateFormat 
        Version_Date_Format = new SimpleDateFormat("yyyyMMddHHmm");
 
    /** logging handle */
    static Logger log = Logger.getLogger( Version.class );
        
        
    static 
    {
        //  always normalise to GMT
        Version_Date_Format.setTimeZone( TimeZone.getTimeZone("GMT") );
    }
    
    
    private Date date;
    
    private String version;
    
    
    public Version()
    {
        /* now */
        this( new Date() );
    }
    
    
    public Version( String version_string )
    {
        version = version_string;
    }
    
    
    public Version( Date d )
    {
        date = d;
        version = date2version( d );
    }
    
    
    /** 
    *   Converts a given {@link Date} into a version number/string, of 
    *   form "yyyyMMdd.HHmm" in {@link SimpleDateFormat}. For example,
    *   given 5:03pm on 14th August 2008 GMT, this method would return 
    *   "200808141703"
    */    
    public static final String date2version( Date d )
    {
        if ( d == null ) 
            return null;
        
        synchronized ( Version_Date_Format )
        {
            return Version_Date_Format.format( d );    
        }
    }
    
    
    /**
    *   Converts a version {@link String} back to a java {@link Date}.
    *   @see #date2version
    */
    public static final Date version2date( String version_string )
    {
        if ( version_string == null || version_string.length() == 0 ) 
            return null;
        
        synchronized ( Version_Date_Format )
        {
            try
            {
                return Version_Date_Format.parse( version_string );    
            }
            catch ( java.text.ParseException ex ) 
            {
                log.warn( 
                    "Couldn't parse version string =" 
                    + version_string
                    , ex 
                );
                return null;
            }
        }
    }
        
    
    public int compareTo( Object x )
    {
        if ( x == null )
            throw new NullPointerException(
                "Argument to compareTo cannot be null");
            
        //  don't check for cast success here because method signature
        //  already defines method to throw ClassCastException.
        Version v = (Version) x;
        
        return this.toLong().compareTo( v.toLong() );
    }
    
    
    public int compareTo( Long x ) {  return this.toLong().compareTo( x );  }

    
    /** Returns the {@link Date} corresponding to the current {@link Version}. */
    public Date getDate()
    {
        if ( date != null )
            return date;
        
        date = version2date( version );
        return date;
    }
    
    
    /** Returns this {@link Version} as a {@link Long}. */
    public Long toLong()
    {
        return new Long( version );   
    }
    
    
    /** Returns the {@link Version} {@link String} for this Version. */
    public String toString()
    {
        return version;   
    }

}

