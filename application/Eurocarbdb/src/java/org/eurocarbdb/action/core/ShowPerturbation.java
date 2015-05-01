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
*   $Id: ShowPerturbation.java 1549 2009-07-19 02:40:46Z glycoslave $
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
import org.eurocarbdb.dataaccess.core.Perturbation;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.util.mesh.MeshReference.markupMeshReferencesAsHTML;



/*  class ShowPerturbation  *//**************************************
*
*   Shows a detail page for a perturbation entry given a perturbation id.
*
*   @author              mjh
*   @version                 $Rev: 1549 $
*/
public class ShowPerturbation extends EurocarbAction
{
    //~~~ FIELDS ~~~//

    /** Logging handle. */
    protected static final Logger log 
        = Logger.getLogger( ShowPerturbation.class.getName() );

    /** Taxonomy ID for the perturbationomy to detail, populated from input parameters */
    private int perturbationId = 1;
    
    /** The perturbationomy we will detail, created using given perturbationomy id */
    private Perturbation perturbation = null;


    //~~~ METHODS ~~~//
    
    /** Get the perturbation we are detailing. */
    public Perturbation getPerturbation() {  return perturbation;  }
    
    /** Set the perturbation we are detailing. */
    public void setPerturbation( Perturbation p ) {  perturbation = p;  }

    /** Get the ID of the perturbation we are detailing. */
    public int getPerturbationId() {  return perturbationId;  }
    
    /** Set the ID of the perturbation we are detailing. */
    public void setPerturbationId( int id ) {  perturbationId = id;  }

   
    /** Returns a list of child perturbation sorted alphabetically by perturbation name. */
    public List<Perturbation> getSortedChildPerturbations()
    {
        assert perturbation != null;
        
        Set<Perturbation> children = perturbation.getChildPerturbations();
        Perturbation[] sorted = children.toArray( new Perturbation[children.size()] );
                
        Arrays.sort( sorted );
        
        return Arrays.asList( sorted );
    }
    
    
    /** Returns a HTML-marked-up version of the description of this perturbation. */
    public String getMeshDescriptionHTML()
    {
        assert perturbation != null;
        return markupMeshReferencesAsHTML( perturbation.getDescription() );
    }


    /** Populates perturbation data if not already done. 
    *   @return SUCCESS if given perturbation ID corresponds to a valid
    *                    perturbation.
    *   @return ERROR   if error.
    */
    public String execute()
    {
        if ( ! (perturbationId > 0) )
        {
            this.addActionError( "Invalid perturbation id!" );
            return ERROR; 
        }
    
        if( perturbationId > 0 )
            perturbation = getEntityManager().lookup( Perturbation.class, perturbationId );
    
        if ( perturbation == null )
        {
            log.info( "No perturbationomy associated with tissue tax id " + perturbationId );
            this.addActionError( "No perturbationomy exists for given perturbation id" );
            return ERROR;
        }

        //  loading child perturbationomies here for use in view.           
        Set<Perturbation> children = perturbation.getChildPerturbations();
           
        return ! this.hasActionErrors() ? SUCCESS : ERROR;
    }

} // end class



