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
*   Last commit: $Rev: 1870 $ by $Author: david@nixbioinf.org $ on $Date:: 2010-02-23 #$  
*/

package org.eurocarbdb.action.core;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.dataaccess.Eurocarb.getHqlQuery;
import static org.eurocarbdb.util.StringUtils.join;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.action.ParameterChecking;
import org.eurocarbdb.dataaccess.core.Disease;
import org.eurocarbdb.dataaccess.core.Perturbation;
import org.eurocarbdb.dataaccess.core.Taxonomy;
import org.eurocarbdb.dataaccess.core.TissueTaxonomy;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/*  class Autocompleter
*
*   Class to produce lists of strings using like queries for various data sources
*   
*   
*
*/

@ParameterChecking( whitelist = {"queryType","queryString"})
public class Autocompleter extends EurocarbAction
{
    private String queryType;
    private String queryString;

    private final String[] PARAMETERS_WHITELIST = new String[]{"queryType","queryString"};

    public String[] parametersWhitelist() {
        return PARAMETERS_WHITELIST;
    }

    /**
     * Inner class for the Autocomplete results. Works as a struct containing
     * the information that will be returned in a list to the autocomplete
     * result rendering template
     */
    public class AutocompleteResult
    {
        public String   description = null;
        public String   synonyms =  null;
        public String   entrySource = null;
        public String   supplemental = "";
        public Object   entry = null;
        
        public String getDescription() {
            return description;
        }
        
        public String getSynonyms() {
            return synonyms;
        }
        /*
         * Entry source for the autocomplete result
         * Usually set to either "user" or nothing
         */
        public String getEntrySource() {
            return entrySource;
        }
        
        public String getSupplemental() {
            return supplemental;
        }
        
        
        public int hashCode() {
            if (this.entry != null) {
                return this.entry.hashCode();
            }
            
            int hashCode = 1;
            
            if (this.description != null) {
                hashCode *= this.description.hashCode();
            }
            if (this.synonyms != null) {
                hashCode *= this.synonyms.hashCode();
            }

            return hashCode;            
        }
        
        public boolean equals(Object x) {
            if ( this == x )
                return true;

            if ( (x == null) || ! (x instanceof AutocompleteResult) )
                return false;

            // objects are the same class
            AutocompleteResult r = (AutocompleteResult) x;

            if (this.entry != null) {
                return this.entry.equals(r.entry);
            }
            
            return (( this.description == null && r.description == null ) || (this.description != null && this.description.equals(r.description)))
                && (( this.synonyms == null && r.synonyms == null ) || (this.synonyms != null && this.synonyms.equals(r.synonyms)))
                ;
        }
    }

    protected Set<AutocompleteResult> results = new LinkedHashSet<AutocompleteResult>();
    
    
    
    public String getQueryType() 
    {
        return this.queryType;
    }
    
    public void setQueryType(String queryType) 
    {
        this.queryType = queryType;
    }
    
    public String getQueryString() 
    {
        return this.queryString;
    }
    
    public void setQueryString(String queryString) 
    {
        this.queryString = queryString;
    }
    
    public Set<AutocompleteResult> getResults() 
    {
        return this.results;
    }
    
    public String execute() 
    {
        if (queryType == null || queryString == null) 
        {
            return SUCCESS;
        }
        
        queryString = queryString.toLowerCase();
        
        if (queryType.equals("taxonomy_name")) 
        {
            findTaxonomiesMatchingString(queryString);
        }
        else if (queryType.equals("tissue_name")) 
        {
            findTissuesMatchingString(queryString);
        }
        else if (queryType.equals("disease_name")) 
        {
            findDiseasesMatchingString(queryString);
        }
        else if (queryType.equals("perturbation_name")) 
        {
            findPerturbationsMatchingString(queryString);
        }
        
        return SUCCESS;
    }
    
    protected AutocompleteResult addResult(String description, Object entry)
    {
        return addResult(description,"","",entry);
    }

    protected AutocompleteResult addResult(String description, String synonyms, Object entry)
    {
        return addResult(description,synonyms,"",entry);
    }

    
    protected AutocompleteResult addResult(String description, String synonyms, String entrySource, Object entry)
    {
        AutocompleteResult result = new AutocompleteResult();
        result.description = description;
        result.synonyms = synonyms;
        result.entrySource = entrySource;
        result.entry = entry;
        results.add(result);
        return result;
    }
    
    protected void findTissuesMatchingString(String query) 
    {
        log.debug("Querying tissue for " + query);
        for ( TissueTaxonomy tissue : TissueTaxonomy.lookupNameOrSynonym( query )) 
        {            
            AutocompleteResult res = addResult(tissue.getName(),tissue);
            res.supplemental = join( " > ", tissue.getAllParentTissueTaxonomies());
        }
    }
    
    protected void findDiseasesMatchingString(String query) 
    {
        log.debug("Querying disease for " + query);
        for ( Disease disease : Disease.lookupNameOrSynonym( query )) 
        {
            AutocompleteResult res = addResult(disease.getDiseaseName(),disease);
            res.supplemental = join( " > ", disease.getAllParentDiseases());
        }
    }
    
    protected void findPerturbationsMatchingString(String query) 
    {
        log.debug("Querying perturbations for " + query);
        for ( Perturbation perturbation : Perturbation.lookupNameOrSynonym( query )) 
        {
            addResult(perturbation.getPerturbationName(),perturbation);
        }
    }
    
    
    private List<Taxonomy> runTaxonomyQuery(String query)
    {
        MatchMode[] modes = {MatchMode.EXACT, MatchMode.START, MatchMode.ANYWHERE};
        LinkedHashSet<Taxonomy> results = new LinkedHashSet<Taxonomy>();
        
        for ( MatchMode mode : modes ) {        
        	log.info("Running match mode: "+mode);
            List<Taxonomy> taxes = runTaxonomyQuery(query,mode);
            results.addAll(taxes);
            if(results.size() >= 10){
            	log.info("Exiting because of results.size() being >= 10");
            }else if(results.size() > 0 && mode == MatchMode.EXACT){
            	log.info("Exiting because of exact match");
            }
            
            if ( (results.size() >= 10) || (results.size() > 0 && mode == MatchMode.EXACT) ) {
            	log.info("Results size:"+results.size());
                return new ArrayList<Taxonomy>(results);
            }
        }
        
        return new ArrayList<Taxonomy>(results);
    }
    
    private List<Taxonomy> runTaxonomyQueryProteomeRanked(String query)
    {
    	log.debug("Fetching matching taxonomies---");
    	Query queryA;
    	if(query.matches("^[0-9]+$")){
    		log.debug("Found integer taxonomic search: "+query);
    		queryA=getHqlQuery("SELECT a FROM Taxonomy AS a, TaxonomyProteomeSkRanked c  " +
    				"WHERE  CAST(a.ncbiId as string) LIKE ? AND " +
    				"a.ncbiId=c.ncbiId " +
    				"ORDER BY c.rank ");
    		queryA.setString(0, ""+query+"%");
    	}else{
    		log.debug("Found string taxonomic search: "+query);
    		queryA=getHqlQuery("SELECT a FROM Taxonomy as a " +
    				"LEFT JOIN FETCH a.taxonomySynonyms  b, TaxonomyProteomeSkRanked c  " +
    				"WHERE (lower(b.synonym) LIKE ? OR lower(a.taxon) LIKE ?) and " +
    				"a.ncbiId=c.ncbiId order by c.rank ");
    		queryA.setString(0, ""+query+"%");
        	queryA.setString(1, ""+query+"%");
    	}
    	
    	queryA.setMaxResults(10);
    
    	List<Taxonomy> ids=queryA.list();
    	ArrayList<Taxonomy> uniqueList=new ArrayList<Taxonomy>();
    	HashSet<String> uniqueTaxons=new HashSet<String>();
    	for(Taxonomy c:ids){
    		if(!uniqueTaxons.contains(c.getTaxon())){
    			uniqueTaxons.add(c.getTaxon());
    			uniqueList.add(c);
    		}
    	}
    	
        return uniqueList;
    }
    
    
    /*
    * Run the taxonomy query, searching within taxonomies and 
    * their synonyms for the query string.
    * 
    * re.relativeImportance is given by the formula "right_index - left_index" within 
    * "core-api/src/org/eurocarbdb/dataaccess/core/TaxonomyRelations.hbm.xml".  Relationships
    * are stored so that they can be retrieved using a modified preorder tree traversal
    * algorithm.  There's an example below of a tree with left and right numbers annotated;
    * taken from http://www.sitepoint.com/print/hierarchical-data-database/.
    * 
    * 			          [1] Food [18]
    * 				            |
    * 	          -----------------------------
    * 	    	  |							  |
    *  		[2] Fruit [7]			   ...............
    * 	          |
    *  	   ---------------
    *      |		     |
    * [3] Red [4]   [5] Blue [6]
    * 
    * Therefore to find all descendants of Fruit you look for all nodes with an Lvalue greater than
    * 2 and an Rvalue less than 7.  The difference between the Lvalue and Rvalue indicates how many
    * nodes are children of the given node.  The actual number of child nodes is given by 
    * (Rvalue-Lvalue-1)/2; we must minus one to reset the Rvalue to that of the last child to prevent
    * counting the parent node, and we must divide by two because each node increments the counter
    * by two. 
    */
    @SuppressWarnings("unchecked")
    private List<Taxonomy> runTaxonomyQuery(String query,MatchMode matchMode) 
    {
            
        Criteria criteria = getEntityManager().createQuery(Taxonomy.class);
        criteria.createAlias("taxonomySynonyms","syn",Criteria.LEFT_JOIN);
        criteria.createAlias("relations","rel",Criteria.INNER_JOIN);
        
        criteria.add(
            Restrictions.or(
                Restrictions.like( "taxon", query, matchMode ),
                Restrictions.like( "syn.synonym", query, matchMode )
            )
        );
        
        criteria.addOrder(Order.asc("rel.relativeImportance"));
        //criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setMaxResults(10);
        
        return criteria.list();
    }  
    
  
    
    protected void findTaxonomiesMatchingString(String query) 
    {
    	this.results.clear();
    	//log.setLevel(Level.ALL);
        log.debug("Querying taxonomy for " + query);
        
        for (Taxonomy tax : runTaxonomyQueryProteomeRanked(query)) 
        {
            List<String> synonyms = tax.getSynonyms();
            String synonymString = "";
            if ( synonyms.size() > 0 ) {
                synonymString = "(" 
                    + join(",",synonyms) 
                    + ")" ;
            }
            addResult(tax.getName(),synonymString,tax);
        }
    }
}