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
*   Last commit: $Rev: 1199 $ by $Author: glycoslave $ on $Date:: 2009-06-11 #$  
*/
package org.eurocarbdb.dataaccess.core;
// Generated Oct 23, 2007 1:31:28 PM by Hibernate Tools 3.2.0.b9

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URLEncoder;

import org.eurocarbdb.dataaccess.core.ref.*;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.Reference;
import org.eurocarbdb.application.glycanbuilder.XMLUtils;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

import static org.eurocarbdb.util.JavaUtils.*;


/**
*   Represents a standard journal article or periodical reference.
*   Since ExternalDatabaseReferences are unique in the sense that only one
*   instance is ever used to implement a specific journal/article
*   reference, it is always best to use the {@link #createOrLookup}
*   method to find or create ExternalDatabaseReference instances.
*
*   @author mjh
*/
public class ExternalDatabaseReference extends Reference implements Serializable
{

    private static final String Q = "org.eurocarbdb.dataaccess.core.ExternalDatabaseReference."; 
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~  FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~~~~//

     
    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//

    /** default constructor */
    public ExternalDatabaseReference() 
    {
        setReferenceType( Reference.Type.DatabaseEntry.toString() );
    }

    public static Reference lookupOrCreateNew(String ext_name, String ext_id) 
    {
        // lookup for existing references
        Reference ret = lookup(ext_name,ext_id);
        if( ret!=null )
            return ret;
        
        // create new database reference
        if( ext_name.equals("bcsdb") )
            ret = new BcsdbReference();
        else if( ext_name.equals("Carbbank") )
            ret = new CarbbankReference();
        else if( ext_name.equals("cfg") )
            ret = new CfgReference();
        else if( ext_name.equals("glyaffinity") )
            ret = new GlyaffinityReference();
        else if( ext_name.equals("glycobase(dublin)") )
            ret = new GlycobaseDublinReference();
        else if( ext_name.equals("glycobase(lille)") )
            ret = new GlycobaseLilleReference();
        else if( ext_name.equals("glycosciences.de") )
            ret = new GlycosciencesDeReference();
        else if( ext_name.equals("kegg") )
            ret = new KeggReference();
        else
            ret = new ExternalDatabaseReference();
        
        // set values
        ret.setExternalReferenceName(ext_name);
        ret.setExternalReferenceId(ext_id);
        
        return ret;
    }

    
    public static Reference lookup(String ext_name, String ext_id) 
    {
        EntityManager em = getEntityManager();
        
        List<Reference> result = em.getQuery( "org.eurocarbdb.dataaccess.core.Reference.LOOKUP" )
            .setParameter( "type", Type.DatabaseEntry.toString())
            .setParameter( "ext_name", ext_name)
            .setParameter( "ext_id", ext_id)
            .list();
                
        if( result==null || result.size()==0 )
            return null;
        
        return result.iterator().next();
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~  STATIC METHODS  ~~~~~~~~~~~~~~~~~~~~~//
    
    /*
    @SuppressWarnings("unchecked") // cause hibernate is non-generic
    public static List<ExternalDatabaseReference> 
    lookupByDatabase( String database_name )
    {
        return null;
    }
    */
    
    /*
    @SuppressWarnings("unchecked") // cause hibernate is non-generic
    public static ExternalDatabaseReference 
    lookupByDatabase( String database_name, String database_id )
    {
        return null;
    }
    */
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~  METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** 
    *   Returns this journal reference as a formatted text journal 
    *   reference citation.
    *   eg: <code></code>
    */
    public String toString()
    {
        Contributor c = this.getContributor(); 
        assert c != null;
        
        return c.getContributorName() 
            + " reference id " 
            + this.getExternalReferenceId();
    }
    
    
} // end class


