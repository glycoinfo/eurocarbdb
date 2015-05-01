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
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/

package org.eurocarbdb.action.core;


//  stdlib imports
import java.util.*;

//  3rd party imports 
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.Reference;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class ShowReference  *//****************************************
*
*   Shows a detail page for an reference given an reference id.
*
*   @author   ac
*/

public class ShowReference extends EurocarbAction
{
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    static final Logger log = Logger.getLogger( ShowReference.class );

    /** The reference we will detail, created using given reference id */
    private Reference reference = null;

    /** Reference ID for the reference to detail, populated from input parameters */
    private int searchReferenceId = -1;    
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Returns the Reference that was looked up. This will most likely be 
    *   null if the execute() method has not yet been called. 
    */
    public Reference getReference() {  return reference;  }
    
    
    //public void setReference( Reference t ) {  reference = t;  }

    
    /** Returns the reference id that is being looked up. */
    public int getReferenceId() {  return searchReferenceId;  }
    
    
    /** Sets the reference id to lookup. */
    public void setReferenceId( int search_id ) {  searchReferenceId = search_id;  }

    
    public String execute()
    {
        if ( params.isEmpty() ) 
        {
            this.addFieldError( "referenceId", "You must specify a reference id ");
            return ERROR;
        }
        
        if ( searchReferenceId <= 0 ) 
        {
            this.addFieldError( "referenceId", "Invalid reference id '" 
                    + searchReferenceId 
                    + "'" );
            return ERROR;
        }
    
        reference = getEntityManager().lookup( Reference.class, searchReferenceId );
    
        if ( reference == null ) 
        {
            log.info( "No reference associated with reference sequence id " + searchReferenceId );
            this.addActionError( 
                "No reference exists for reference id '" 
                 + searchReferenceId 
                 + "'" 
             );
            return ERROR;
        }

        return ! this.hasActionErrors() ? SUCCESS : ERROR;
    }

} // end class
