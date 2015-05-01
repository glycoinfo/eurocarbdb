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
/*-------------------------------------------------------------------
*   $Id: ShowTissueTaxonomy.java 1549 2009-07-19 02:40:46Z glycoslave $
*   Last changed $Author: glycoslave $
*   EUROCarbDB Project
*------------------------------------------------------------------*/

package org.eurocarbdb.action.core;

import java.util.Set;
import java.util.List;
import java.util.Arrays;

import org.hibernate.Query;

import org.eurocarbdb.util.Logger;
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.Disease;
import org.eurocarbdb.dataaccess.core.TissueTaxonomy;
import org.eurocarbdb.util.mesh.MeshReference;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;



/*  class ShowTissueTaxonomy  *//****************************************
*
*   Shows a detail page for a tissueTaxonomy entry given a tissueTaxonomy id.
*
*   @author              mjh
*   @version             $Rev: 1549 $
*/
public class ShowTissueTaxonomy extends EurocarbAction
{
    //~~~ FIELDS ~~~//

    /** Logging handle. */
    protected static final Logger log 
        = Logger.getLogger( ShowTissueTaxonomy.class.getName() );

    /** Taxonomy ID for the tissue taxonomy to detail */
    private int tissueTaxonomyId = 1;
    
    /** The tissue taxonomy we will detail, created using given tissue taxonomy id */
    private TissueTaxonomy tissueTaxonomy = null;


    //~~~ METHODS ~~~//
    
    /** Get the tissue taxon we are detailing. */
    public TissueTaxonomy getTissueTaxonomy() {  return tissueTaxonomy;  }
    
    /** Set the tissue taxon we are detailing. */
    public void setTissueTaxonomy( TissueTaxonomy t ) {  tissueTaxonomy = t;  }

    /** Get the ID of the tissue taxon we are detailing. */
    public int getTissueTaxonomyId() {  return tissueTaxonomyId;  }
    
    /** Set the ID of the tissue taxon we are detailing. */
    public void setTissueTaxonomyId( int id ) {  tissueTaxonomyId = id;  }

   
    /** Returns a list of child taxonomies sorted alphabetically by taxon name. */
    public List<TissueTaxonomy> getSortedChildTissueTaxonomies()
    {
        assert tissueTaxonomy != null;
        
        Set<TissueTaxonomy> children = tissueTaxonomy.getChildTissueTaxonomies();
        TissueTaxonomy[] sorted = children.toArray( new TissueTaxonomy[children.size()] );
                
        //Arrays.sort( sorted );
        
        return Arrays.asList( sorted );
    }
    
    
    /** Returns a HTML-marked-up version of the description of this tissue taxon. */
    public String getMeshDescriptionHTML()
    {
        assert tissueTaxonomy != null;
        return MeshReference.markupMeshReferencesAsHTML( 
                tissueTaxonomy.getDescription() );
    }


    /** Populates tissue taxon data if not already done. 
    *   @return SUCCESS if given tissue taxonomy ID corresponds to a valid
    *                    tissue taxon.
    *   @return ERROR   if error.
    */
    public String execute()
    {
        if ( ! (tissueTaxonomyId > 0 || tissueTaxonomy != null) )
        {
            this.addActionError( "Invalid tissueTaxonomy id!" );
            return ERROR; 
        }
    
        if ( tissueTaxonomy != null && tissueTaxonomy.getMeshId() != null )
        {
            Query tissueQuery = getEntityManager().getQuery("org.eurocarbdb.dataaccess.core.TissueTaxonomy.TISSUE_TAXONOMY_BY_MESH_ID");            
            tissueQuery.setParameter("mesh_id", tissueTaxonomy.getMeshId());
            tissueTaxonomy = (TissueTaxonomy) tissueQuery.uniqueResult();
        }
        
        if( tissueTaxonomy == null && tissueTaxonomyId > 0 )
            tissueTaxonomy = getEntityManager().lookup( TissueTaxonomy.class, tissueTaxonomyId );
    
        if ( tissueTaxonomy == null )
        {
            log.info( "No tissue taxonomy associated with tissue tax id " + tissueTaxonomyId );
            this.addActionError( "No tissue taxonomy exists for given tissueTaxonomy id" );
            return ERROR;
        }

        //  loading child tissue taxonomies here for use in view.           
        Set<TissueTaxonomy> children = tissueTaxonomy.getChildTissueTaxonomies();
           
        return ! this.hasActionErrors() ? SUCCESS : ERROR;
    }

} // end class



