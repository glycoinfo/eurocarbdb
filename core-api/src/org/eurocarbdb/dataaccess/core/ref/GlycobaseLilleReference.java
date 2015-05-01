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

package org.eurocarbdb.dataaccess.core.ref;

//  stdlib imports
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.core.Reference;
import org.eurocarbdb.dataaccess.core.ExternalDatabaseReference;

//  static imports
//import static org.eurocarbdb.util.JavaUtils.*;
//import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*   
*
*   @author mjh
*/
public class GlycobaseLilleReference 
extends ExternalDatabaseReference implements Serializable
{
    /** Default query base URL for constructing links */
    public static final String Default_GlycobaseLille_Query_Url 
        = "";
    
    static final Logger log = Logger.getLogger( GlycobaseLilleReference.class );

//    private static final String Q = "org.eurocarbdb.dataaccess.core.GlycobaseLilleReference."; 
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~  FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~~~~//


    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//

    /** default constructor */
    public GlycobaseLilleReference() 
    {
        //  default journal reference provider is pubmed.
        setExternalReferenceName("glycobase(lille)");
        
        //  objects of this class are the DatabaseEntry type by definition.
        setReferenceType( Reference.Type.DatabaseEntry.toString() );
    }

    
    //~~~~~~~~~~~~~~~~~~~~~~  STATIC METHODS  ~~~~~~~~~~~~~~~~~~~~~//
 
    /** 
    *   Returns GlycobaseLille id; this should be the same id as 
    *   {@link Reference#getExternalReferenceId()} 
    */
    public String getGlycobaseLilleId()
    {
        return this.getExternalReferenceId();
    }
    
    
    @Override
    public String getUrl()
    {
        // if ( this.url != null && url.length() > 0 )
            // return this.url;
        
        String base = Eurocarb.getProperty("glycobase_lille.query.url");
        if ( base == null || base.length() == 0 || ! base.startsWith("http://") )
            base = Default_GlycobaseLille_Query_Url;
        
        String id = this.getGlycobaseLilleId(); 
        
        return base + id;
    }
    
    
} // end class


