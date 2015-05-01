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
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;

//  3rd party imports 
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.dataaccess.EurocarbObject;
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.action.Result;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.BiologicalContext;
import org.eurocarbdb.dataaccess.core.Taxonomy;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class ShowTaxonomy  *//****************************************
*
*   Shows a detail page for a taxonomy entry given a taxonomy id.
*
*
*   @see      org.eurocarbdb.action.core.SearchTaxonomy
*   @author   mjh 
*   @version  $Rev: 1549 $
*/
public class ShowTaxonomy extends EurocarbAction
{
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    static final Logger log = Logger.getLogger( ShowTaxonomy.class );

    /** The taxonomy we will detail, created using given taxonomy id */
    private Taxonomy taxonomy = null;

    /** Taxonomy ID for the taxonomy to detail, populated from input parameters */
    private int searchTaxonomyId = 1;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Returns the Taxonomy that was looked up. This will most likely be 
    *   null if the execute() method has not yet been called. 
    */
    public Taxonomy getTaxonomy() {  return taxonomy;  }

    public void setTaxonomy( Taxonomy t ) {  taxonomy = t;  }

    /** Returns the taxonomy id that is being looked up. */
    public int getTaxonomyId() {  return searchTaxonomyId;  }
    
    /** Sets the taxonomy id to lookup. */
    public void setTaxonomyId( int search_id ) {  searchTaxonomyId = search_id;  }

       
    public String execute()
    {
        if ( ! (searchTaxonomyId > 0 || taxonomy != null) )
        {
            this.addActionError( "Invalid taxonomy id!" );
            return ERROR;
        }
    
        if ( taxonomy != null && taxonomy.getNcbiId() > 0) 
        {
            taxonomy = Taxonomy.lookupNcbiId(taxonomy.getNcbiId());
        }
        
        if( taxonomy == null && searchTaxonomyId > 0 )
            taxonomy = getEntityManager().lookup( Taxonomy.class, searchTaxonomyId );
    
        if ( taxonomy == null )
        {
            log.info( "No taxonomy associated with tax id " + searchTaxonomyId );
            this.addActionError( "No taxonomy exists for given taxonomy id" );
            return ERROR;

        }

        //  force initialisation of child taxonomies here for use in view.           
        Set<Taxonomy> children = taxonomy.getChildTaxonomies(); 
        
        return ! this.hasActionErrors() ? SUCCESS : ERROR;
    }

} // end class
