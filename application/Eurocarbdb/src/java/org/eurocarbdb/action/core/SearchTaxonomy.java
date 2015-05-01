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
*   Last commit: $Rev: 1553 $ by $Author: glycoslave $ on $Date:: 2009-07-20 #$  
*/

package org.eurocarbdb.action.core;

//  stdlib imports
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

//  3rd party imports 
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.EurocarbObject;
import org.eurocarbdb.dataaccess.core.Taxonomy;

import org.eurocarbdb.action.AbstractSearchAction;
import org.eurocarbdb.action.exception.InsufficientParams;

//  static imports
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class SearchTaxonomy  *//****************************************
*
*   Shows a detail page for a taxonomy entry given a taxonomy id.
*   See the execute method for the importance order of search predicates.
*
*   @author              mjh 
*   @version             $Rev: 1553 $
*/
public class SearchTaxonomy extends AbstractSearchAction<Taxonomy>
{

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging handle. */
    static final Logger log = Logger.getLogger( SearchTaxonomy.class );


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** The list of taxonomies that match the provided search criteria. */
    private List<Taxonomy> matchingTaxonomies = null;
    
    /** A eurocarb taxonomy id to lookup. */
    private int searchTaxonomyId = -1;

    /** A taxonomy name search string. */
    private String searchTaxonomyName = null;

    /** Human readable string representation of the query performed in execute(). */
    private StringBuffer searchCriteria = new StringBuffer();

    /** A NCBI ID with which to lookup a taxonomy. */
    private int searchNcbiId = 0;

    /** Returns all the result as a plain HTML list, to be used with AJAX **/ 
    private int showResultsAsList = 0;

    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    
    /*  getSearchCriteria  *//***************************************
    *
    *   Returns a humanised string representation of the current search
    *   criteria for human consuption.
    */
    public String getSearchCriteria() {  return searchCriteria.toString();  }
    
    
    /*  getMatchingTaxonomies  *//*********************************** 
    *
    *   Returns the list of Taxonomy(s) that match the provided search 
    *   parameters (via the setParameters() method). This list will most 
    *   likely be null if the execute() method has not yet been called. 
    */
    public List<Taxonomy> getMatchingTaxonomies() {  return matchingTaxonomies;  }
    
    
    /** Returns all the result as a plain HTML list, to be used with AJAX **/
    public void setShowList( int i ) 
    {
        showResultsAsList = i;
    }
    

    public int getShowList() 
    {
        return showResultsAsList;
    }
    

    /** Returns the search taxonomy id, or -1 if not set. */
    public int getTaxonomyId() {  return this.searchTaxonomyId;  }

    
    /** Sets a taxonomy ID to be used in a taxonomy ID lookup. */
    public void setTaxonomyId( int id ) {  this.searchTaxonomyId = id;  }

    
    /** Returns a taxonomy name search string, or null if not set. */
    public String getTaxonomyName() {  return this.searchTaxonomyName;  }

    
    /** Sets a string to be used in a taxonomy name search. */
    public void setTaxonomyName( String search_name ) {  this.searchTaxonomyName = search_name;  }

    
    /** Returns an integer NCBI search Id, or 0 if not set. */
    public int getNcbiId() {  return this.searchNcbiId;  }

    
    /** Sets an integer to be used for a NCBI Id search. */
    public void setNcbiId( int search_ncbi_id ) {  this.searchNcbiId = search_ncbi_id;  }
    

    
    /*  execute  *//*************************************************
    *
    *   Looks up taxonom(y/ies) using the predicates currently set. 
    *   Order of search predicate importance:
    *<ol>
    *       <li>searchTaxonomyName</li> 
    *       <li>searchNcbiId</li> 
    *       <li>searchTaxonomyId</li> 
    *<ol>
    *   ie: setting one of the above will cause the others to be ignored.
    */
    public String execute() throws InsufficientParams //, InvalidParams
    {
        EntityManager em = getEntityManager();

        //  try exact taxonomy name/synonym search
        if ( searchTaxonomyName != null && searchTaxonomyName.length() > 0 )
        {
            this.matchingTaxonomies 
                = Taxonomy.lookupExactNameOrSynonym( searchTaxonomyName );
    
            if ( matchingTaxonomies != null && matchingTaxonomies.size() > 0 )
            {
                //  we've got a result; great.
                searchCriteria.append( "taxonomy name or synonym exactly matches '" 
                                     + searchTaxonomyName
                                     + "'"
                                     );
            }
            else // retry search using wildcards  
            {
                this.matchingTaxonomies 
                    = Taxonomy.lookupNameOrSynonym( searchTaxonomyName );

                searchCriteria.append( "taxonomy name or synonym approximately matches '" 
                                     + searchTaxonomyName
                                     + "'"
                                     );
            }
        }
        
        //  NCBI id search
        else if ( searchNcbiId > 0 )
        {
            searchCriteria.append( "NCBI Id is " + searchNcbiId );

            Taxonomy t = Taxonomy.lookupNcbiId( searchNcbiId );           
 
            if ( t != null ) 
            {
                this.matchingTaxonomies = new ArrayList<Taxonomy>( 2 );
                this.matchingTaxonomies.add( t );
            }
        }
        
        //  eurocarb id search
        else if ( searchTaxonomyId > 0 )
        {
            searchCriteria.append( "Eurocarb taxonomy Id is " + searchTaxonomyId );
            
            Taxonomy t = em.lookup( Taxonomy.class, searchTaxonomyId );
            if ( t != null )
            {
                this.matchingTaxonomies = new ArrayList<Taxonomy>( 2 );
                this.matchingTaxonomies.add( t );
            }
        }
        
        //  other searches would go _here_
        
        if ( showResultsAsList != 0 )
            return "list";
        
        return ( matchingTaxonomies != null && matchingTaxonomies.size() > 0 ) 
            ?   SUCCESS
            :   INPUT
        ;
    }

} // end class
