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
*   Last commit: $Rev: 1987 $ by $Author: glycoslave $ on $Date:: 2010-09-08 #$  
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
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.Reference;
//import org.eurocarbdb.dataaccess.core.ComparatorExternalReference;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class ShowGlycan  *//****************************************
*
*   Shows a detail page for an glycan given an glycan id.
*
*
*   @author   mjh [glycoslave@gmail.com]
*   @version  $Rev: 1987 $
*/
public class ShowGlycan extends EurocarbAction
{
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    protected static final Logger log = Logger.getLogger( ShowGlycan.class );

    /** The glycan we will detail, created using given glycan id */
    private GlycanSequence glycan = null;

    /** GlycanSequence ID for the glycan to detail, populated from input parameters */
    private int searchGlycanId = -1;
    
    private List<Reference> listReference = new ArrayList<Reference>();

    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Returns the Glycan that was looked up. This will most likely be 
    *   null if the execute() method has not yet been called. 
    */
    public GlycanSequence getGlycan() {  return glycan;  }

    public GlycanSequence getGlycanSequence() {  return glycan;  }
    
    //public void setGlycanSequence( GlycanSequence t ) {  glycan = t;  }

    /** Returns the glycan id that is being looked up. */
    public int getGlycanSequenceId() {  return searchGlycanId;  }
    
    /** Sets the glycan id to lookup. */
    public void setGlycanSequenceId( int search_id ) {  searchGlycanId = search_id;  }

/*
    public getTaxonomyTreeJson()
    {

        List<BiologicalContext> contexts = getGlycan().getBiologicalContexts();
        Set<Integer> tax_ids = new HashSet<Integer>();
        for ( BiologicalContext bc : contexts )
        {
            tax_ids.add( bc.getTaxonomy().getTaxonomyId() );
        }        
        
        Graph<Void,Taxonomy> g = Taxonomy.getParentTaxonomyGraph( tax_ids );
        
    }*/
    
    
    public String execute()
    {
        if( submitAction!=null && submitAction.equals("Add MS data") ) 
        {
            return "add_ms_data";
        }

        if( submitAction!=null && submitAction.equals("Add HPLC data"))
        {
            return "add_hplc_data";
        }

        if ( params.isEmpty() ) 
            return INPUT;
        
        if ( searchGlycanId <= 0 )
        {
            this.addFieldError( "glycanId", "Invalid glycan id '" 
                                          + searchGlycanId 
                                          + "'" );
            return INPUT;
        }
    
        glycan = getEntityManager().lookup( GlycanSequence.class, searchGlycanId );
        
        if ( glycan == null )
        {
            log.info( "No glycan associated with glycan sequence id " + searchGlycanId );
            this.addActionError( "No glycan exists for glycan id '" 
                               + searchGlycanId 
                               + "'" 
                               );
            return ERROR;
        }
    
        for (Reference t_reference : glycan.getReferences() )
        {
            this.listReference.add(t_reference);
        }
    
        /*
        ComparatorExternalReference t_Comp = new ComparatorExternalReference();     
        
        Collections.sort(listReference,t_Comp);
    
        for(Reference t_ref : t_Comp.getToBeDeleted())
        {   
            listReference.remove(t_ref);    
        }
        */
    
        return ! this.hasActionErrors() ? SUCCESS : ERROR;
    }
    
    public List<Reference> getListReference()
    {
        return this.listReference;  
    }
    
    public void setListReference(List<Reference> ref)
    {
        this.listReference = ref;   
    }
} // end class
