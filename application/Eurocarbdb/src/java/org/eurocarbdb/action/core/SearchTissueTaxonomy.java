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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/
/*-------------------------------------------------------------------
*   $Id: SearchTissueTaxonomy.java 1932 2010-08-05 07:12:33Z glycoslave $
*   Last changed $Author: glycoslave $
*   EUROCarbDB Project
*------------------------------------------------------------------*/

package org.eurocarbdb.action.core;

//  stdlib imports
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

//  3rd party imports 
//import com.opensymphony.webwork.interceptor.ParameterAware;
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.dataaccess.core.TissueTaxonomy;
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.action.exception.InsufficientParams;

//  static imports
import static org.eurocarbdb.util.StringUtils.join;
//import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class SearchTissueTaxonomy  *//****************************************
*
*   Shows a detail page for a tissue taxonomy entry given a tissue taxonomy id.
*
*   @author              mjh <glycoslave@gmail.com>
*   @version             $Rev: 1932 $
*/
public class SearchTissueTaxonomy extends EurocarbAction 
{

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /***/
    //public static final String ERROR__NO_PARAMS = "error__no_params";

    /** Logging handle. */
    protected static final Logger log 
        = Logger.getLogger( SearchTissueTaxonomy.class.getName() );


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** The list of taxonomies that match the provided search criteria. */
    private List<TissueTaxonomy> matchingTissueTaxonomies = null;
    
    /** A taxonomy name search string. */
    private String searchTissueTaxonomyName = null;

    /** Human readable string representation of the query performed in execute(). */
    private StringBuffer searchCriteria = new StringBuffer();

    /** A NCBI ID with which to lookup a tissue taxonomy. */
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
    
    
    /*  getMatchingTissueTaxonomies  *//*****************************
    *
    *   Returns the list of TissueTaxonomy(s) that match the provided search 
    *   parameters (via the setParameters() method). This list will most 
    *   likely be null if the execute() method has not yet been called. 
    */
    public List<TissueTaxonomy> getMatchingTissueTaxonomies() {  return matchingTissueTaxonomies;  }
    
    /** Returns all the result as a plain HTML list, to be used with AJAX **/
    public void setShowList(int i) {
    showResultsAsList = i;
    }

    public int getShowList() {
    return showResultsAsList;
    }
    
    /** Returns a tissue taxonomy name search string, or null if not set. */
    public String getTissueTaxonomyName() {  return this.searchTissueTaxonomyName;  }

    /** Sets a string to be used in a tissue taxonomy name search. */
    public void setTissueTaxonomyName( String search_name ) {  this.searchTissueTaxonomyName = search_name;  }
    

    
    /*  execute  *///************************************************
    public String execute() throws InsufficientParams //, InvalidParams
    {
        EntityManager em = getEntityManager();

        //  tissue taxonomy name or synonym search
        if ( searchTissueTaxonomyName != null )
        {
            searchCriteria.append( "tissue taxonomy name or synonym matches '" 
                                 + searchTissueTaxonomyName
                                 + "'"
                                 );

            this.matchingTissueTaxonomies 
                = TissueTaxonomy.lookupNameOrSynonym( searchTissueTaxonomyName );
        if( showResultsAsList!=0 )
        return "list";
            return SUCCESS;
        }
        
        //  other searches would go here...
        
    if( showResultsAsList!=0 )
        return "list";
    return INPUT;
    }

} // end class
