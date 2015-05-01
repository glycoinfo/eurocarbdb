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
*   $Id: ShowDisease.java 1549 2009-07-19 02:40:46Z glycoslave $
*   Last changed $Author: glycoslave $
*   EUROCarbDB Project
*------------------------------------------------------------------*/

package org.eurocarbdb.action.core;

import java.util.Set;
import java.util.List;
import java.util.Arrays;

import org.eurocarbdb.util.Logger;
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.Disease;
import org.hibernate.Query;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.util.mesh.MeshReference.markupMeshReferencesAsHTML;



/*  class ShowDisease  *//****************************************
*
*   Shows a detail page for a disease entry given a disease id.
*
*   @author              mjh
*   @version                 $Rev: 1549 $
*/
public class ShowDisease extends EurocarbAction
{
    //~~~ FIELDS ~~~//

    /** Logging handle. */
    protected static final Logger log 
        = Logger.getLogger( ShowDisease.class.getName() );

    /** Taxonomy ID for the disease to detail. 
    *   Defaults to 1, the disease tree's root id  */
    private int diseaseId = 1;
    
    /** The diseaseomy we will detail, created using given disease id */
    private Disease disease = null;


    //~~~ METHODS ~~~//
    
    /** Get the disease we are detailing. */
    public Disease getDisease() {  return disease;  }
    
    /** Set the disease we are detailing. */
    public void setDisease( Disease d ) {  disease = d;  }

    /** Get the ID of the disease we are detailing. */
    public int getDiseaseId() {  return diseaseId;  }
    
    /** Set the ID of the disease we are detailing. */
    public void setDiseaseId( int id ) {  diseaseId = id;  }

   
    /** Returns a list of child disease sorted alphabetically by disease name. */
    public List<Disease> getSortedChildDiseases()
    {
        assert disease != null;
        
        Set<Disease> children = disease.getChildDiseases();
        Disease[] sorted = children.toArray( new Disease[children.size()] );
                
        Arrays.sort( sorted );
        
        return Arrays.asList( sorted );
    }
    
    
    /** Returns a HTML-marked-up version of the description of this disease. */
    public String getMeshDescriptionHTML()
    {
        assert disease != null;
        return markupMeshReferencesAsHTML( disease.getDescription() );
    }


    /** Populates disease data if not already done. 
    *   @return SUCCESS if given diseaseomy ID corresponds to a valid
    *                    disease.
    *   @return ERROR   if error.
    */
    public String execute()
    {
        if ( ! (diseaseId > 0 || disease != null) )
        {
            this.addActionError( "Invalid disease id!" );
            return ERROR; 
        }
    
    
        if ( disease != null && disease.getMeshId() != null ) {
            Query diseaseQuery = getEntityManager().getQuery("org.eurocarbdb.dataaccess.core.Disease.DISEASE_BY_MESH_ID");            
            diseaseQuery.setParameter("mesh_id", disease.getMeshId());
            disease = (Disease) diseaseQuery.uniqueResult();
        }

        if( disease == null && diseaseId > 0 )
            disease = getEntityManager().lookup( Disease.class, diseaseId );
        
        if ( disease == null )
        {
            log.info( "No diseaseomy associated with tissue tax id " + diseaseId );
            this.addActionError( "No disease exists for given disease id" );
            return ERROR;
        }

        //  loading child diseaseomies here for use in view.           
        Set<Disease> children = disease.getChildDiseases();
           
        return ! this.hasActionErrors() ? SUCCESS : ERROR;
    }

} // end class



