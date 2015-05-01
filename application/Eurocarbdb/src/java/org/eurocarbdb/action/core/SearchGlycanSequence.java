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
*   Last commit: $Rev: 1573 $ by $Author: glycoslave $ on $Date:: 2009-07-24 #$  
*/

package org.eurocarbdb.action.core;

//  stdlib imports
import java.util.*;
import java.math.BigDecimal;

//  3rd party imports 
import org.apache.log4j.Logger;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;


import org.hibernate.criterion.Order;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Subqueries;

//  eurocarb imports
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.core.seq.*;
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.action.BrowseAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.HibernateEntityManager;
import org.eurocarbdb.dataaccess.indexes.*;

import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.application.glycanbuilder.Glycan;

import org.eurocarbdb.dataaccess.SavedGlycanSequenceSearch;
// import org.eurocarbdb.dataaccess.core.seq.SavedGlycanSubstructureSearch;

//  static imports
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.dataaccess.core.seq.SubstructureQuery.Option.*;


/*  class SearchGlycanSequence  *//****************************************
*
*   Finds a Set of {@link GlycanSequence} objects satisfying a given 
*   set of query predicates. Matched sequences are wrapped together 
*   with their query predicates in a {@link SavedGlycanSequenceSearch} object.
*   
*   @see SavedGlycanSequenceSearch
*   @author mjh
*   @author hirenj
*   @version $Rev: 1573 $
*/
public class SearchGlycanSequence extends BrowseAction<GlycanSequence>
{

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** Logging handle. */
    static final Logger log = Logger.getLogger( SearchGlycanSequence.class );

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private String taxonomyName = null;    
    private String tissueName = null;
    private String diseaseName = null;
    private String perturbationName = null;
    
    private double  lowMass = -1, 
                    highMass = -1, 
                    exactMass = -1, 
                    exactMassTolerance = -1;
    
    private boolean useAvgMass = false;
    private boolean useAvgMassGiven = false;

    private String sequenceGWS;

    // Position to search for the sequence
    private String sequencePosition = null;

    // Stores the input glycan ID that we wish to search for
    private int glycanId = -1;

    private boolean validated = false;

    private boolean isNewQuery = false;

    private List<SavedGlycanSequenceSearch> queryHistory 
        = new java.util.ArrayList<SavedGlycanSequenceSearch>();
    
    private List<SavedGlycanSequenceSearch> additionalQueries 
        = new java.util.ArrayList<SavedGlycanSequenceSearch>();

    private SavedGlycanSequenceSearch currentSearch;

    private int[] historicalQueriesToRun = {};
    
    private int[] historicalQueriesToRefine = {};

    /** The {@link List} of {@link Index}es supported by this Action. */
    public static final List<Index<GlycanSequence>> indexes = Arrays.asList(
        new IndexByContributedDate<GlycanSequence>(),
        new IndexByContributorName<GlycanSequence>(),
        new IndexByMostEvidence<GlycanSequence>(),
        new IndexByResidueCount<GlycanSequence>()
    );
    

    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   
    //~~~  criteria creation & access methods  ~~~~~

    public Criteria createCriteria()
    {      
      DetachedCriteria crit = createSerializableCriteria();
      crit.setProjection(
          Projections.distinct(
              Projections.property("glycanSequenceId")));
      
      Criteria criteria = getEntityManager().createQuery(GlycanSequence.class);
      
      criteria.add( Subqueries.propertyIn("glycanSequenceId",crit) );
      
      if( getIndex() != null ) 
      {
          getIndex().apply(criteria);
      }
      
      return criteria;
    }
    
    
    private DetachedCriteria createSerializableCriteria()
    {        
        // create base criteria    
        log.debug("creating GlycanSequence criteria");
        
      DetachedCriteria criteria;

      criteria = DetachedCriteria.forClass( GlycanSequence.class );        
            
        // create biological contexts criteria
        DetachedCriteria bc_criteria = null;
        DetachedCriteria tax_criteria = null;
        DetachedCriteria tissue_criteria = null;
        DetachedCriteria disease_criteria = null;
        DetachedCriteria perturbation_criteria = null;
        
        if( taxonomyName!=null || tissueName!=null || diseaseName!=null || perturbationName != null ) {
            isNewQuery = true;
            log.debug("creating Biological context criteria");
            bc_criteria = criteria.createCriteria("glycanContexts")
                          .createCriteria("biologicalContext", "bc");    

            // add taxonomy criteria        
            if( taxonomyName!=null ) {
                log.debug("adding taxonomy query predicates for input string '" + taxonomyName + "'");
            tax_criteria = bc_criteria.createCriteria("taxonomy", "taxa")
                                .createCriteria("taxonomySupertypes", "supertax")
                                .add( Restrictions.ilike( "taxon", taxonomyName, MatchMode.EXACT ) );            
            }
        
            // add tissue criteria
            if( tissueName!=null ) {
                log.debug("adding tissue query predicates for input string '" + tissueName + "'");
        
            tissue_criteria = bc_criteria.createCriteria("tissueTaxonomy", "ttax")
                                    .add( Restrictions.ilike("tissueTaxon", tissueName, MatchMode.EXACT ) );       
            }

            // add disease criteria
            if( diseaseName!=null ) {
                log.debug("adding disease query criteria for input string '" + diseaseName + "'");
              
                  disease_criteria = bc_criteria.createCriteria("diseaseContexts")
                                    .createCriteria("disease", "dis")
                                    .add( Restrictions.ilike("diseaseName", diseaseName, MatchMode.EXACT ) );
            }
            
            if ( perturbationName!=null ) {
                log.debug("adding perturbation query criteria for input string '" + perturbationName + "'");
              
                perturbation_criteria = bc_criteria.createCriteria("perturbationContexts")
                                    .createCriteria("perturbation", "per")
                                    .add( Restrictions.ilike("perturbationName", perturbationName, MatchMode.EXACT ) );                
            }
            
        }

        // add mass criteria
      boolean mass_query_is_given = false;
      boolean params_are_ok = false;
  
      if ( exactMass > 0 && exactMassTolerance > 0 )
      {
          isNewQuery = true;
          mass_query_is_given = true;
          lowMass = exactMass - exactMassTolerance;
          highMass = exactMass + exactMassTolerance;
          log.debug( "adding predicates for exactMass=" + exactMass + " Da +/- " + exactMassTolerance + " Da (ie: " + lowMass + "-" + highMass + " Da)" );
          params_are_ok = true;
      }
      else if ( lowMass > 0 && highMass > 0 )
      {
          isNewQuery = true;
          mass_query_is_given = true;
          exactMass = -1;
          exactMassTolerance = -1;
          log.debug( "adding predicates for mass range=(" + lowMass + ".." + highMass + " Da)" );
          params_are_ok = true;
        }
  
        if ( mass_query_is_given )
        {
            if (  params_are_ok )
            {
                isNewQuery = true;
                String property = useAvgMass ? "massAverage" : "massMonoisotopic";                
                criteria.add( Restrictions.between(property, new BigDecimal(lowMass), new BigDecimal(highMass) ) );
            }
            else
            {
                String msg = "Insufficient mass parameters given, either "
                    + "provide an exactMass + exactMassTolerence + useAvgMass preference, "
                    + "or provide a lowMass + highMass + useAvgMass preference";

                addActionError( msg );
                log.info( msg );
            }
        }

        Glycan glycan = null;
        
        if ( sequenceGWS != null )
        {
            glycan = Glycan.fromString(sequenceGWS);
            glycan.removeReducingEndModification();
            if (glycan.isEmpty())
            {
                glycan = null;
                sequenceGWS = null;
            }
        }
      
      
        if ( glycan != null ) 
        {
            isNewQuery = true;

            // search structure in DB
            String glycoct = glycan.toGlycoCTCondensed();
            SugarSequence seq = new SugarSequence( glycoct );
            SubstructureQuery query = new SubstructureQuery( seq );
            
            if ( sequencePosition != null )
            {
                if ( sequencePosition.equals("Core") || sequencePosition.equals("Core + Terminii") )
                    query.setOption( Must_Include_Reducing_Terminus );
                
                if ( sequencePosition.equals("Terminii") || sequencePosition.equals("Core + Terminii") )
                    query.setOption( Must_Include_All_Non_Reducing_Terminii );
            }
            
            criteria.add( query.getQueryCriterion() );
        }
        
        if ( this.additionalQueries.size() > 1 ) 
        {
            isNewQuery = true;
        }


        for ( SavedGlycanSequenceSearch oldQuery : this.additionalQueries ) 
        {
    
            DetachedCriteria oldCriteria = oldQuery.getQueryCriteria();
            
            criteria.add( Subqueries.propertyIn("glycanSequenceId", oldCriteria) );
            
            oldCriteria.setProjection(
                Projections.distinct(
                    Projections.property("glycanSequenceId")));
            
            this.currentSearch = oldQuery;
        }
            
        return criteria;
    }       
    //~~~~~~~~~~~~  Query history methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public List<SavedGlycanSequenceSearch> getQueryHistory() 
    {
        return queryHistory;
    }
    
    
    public void setQueryHistory(List<SavedGlycanSequenceSearch> history) 
    {
        this.queryHistory = history;
    }
    
    
    public int[] getHistoricalQueriesToRefine() 
    {
        return this.historicalQueriesToRefine;
    }

    
    public void setHistoricalQueriesToRefine( int[] ids ) 
    {
        this.historicalQueriesToRefine = ids;      

        for ( int index : ids ) 
        {
            if (this.getQueryHistory().size() > index) 
            {
                this.additionalQueries.add(this.getQueryHistory().get(index));
            }
        }
      
    }
    
    
    public int[] getHistoricalQueriesToRun() 
    {
        return this.historicalQueriesToRun;
    }
    
    
    public void setHistoricalQueriesToRun( int[] ids ) 
    {
        this.historicalQueriesToRun = ids;
        
        for( int index : ids ) 
        {
            if ( this.getQueryHistory().size() > index ) 
            {
                this.additionalQueries.add( this.getQueryHistory().get(index) );
            }
        }

    }
    
    
    public List<SavedGlycanSequenceSearch> getAdditionalQueries() 
    {
        return this.additionalQueries;
    }
    
    
    public SavedGlycanSequenceSearch getCurrentSearch() 
    {
        return this.currentSearch;
    }
    
    
    //~~~~~~~~~~~~  query predicate creation methods  ~~~~~~~~~~~~~~~

    public String getSequenceGWS() 
    {
        return sequenceGWS;
    }

    
    public void setSequenceGWS(String str) 
    {
        sequenceGWS = str;
    }      
    
    
    public boolean isSearchCore() 
    {
        return false;
    }

    
    public boolean isSearchTerminal() 
    {
        return false;
    }
      
    
    //   taxonomy query predicates  //
    
    public void setTaxonomyName( String namestring )
    {
      if( namestring!=null && namestring.trim().length()>0 )
          taxonomyName = namestring.trim();
        else
          taxonomyName = null;
    }
    

    public String getTaxonomyName() 
    {
        return taxonomyName;
    }  
      
    
    public void setTissueName( String namestring )
    {
        if( namestring!=null && namestring.trim().length()>0 )
          tissueName = namestring.trim();
        else
          tissueName = null;
    }
           
    
    public String getTissueName() 
    {
        return tissueName;
    }    
    

    public void setDiseaseName( String namestring )
    {
        if( namestring!=null && namestring.trim().length()>0 )
            diseaseName = namestring.trim();
        else
            diseaseName = null;
    }        
    
    
    public String getDiseaseName() 
    {
        return diseaseName;
    }  

    
    /**
    *  Get accessor for perturbationName
    *  Perturbation query string accessor
    */
    public String getPerturbationName()
    {
        return this.perturbationName;
    }

    
    /**
    *  Set accessor for perturbationName
    *  @param perturbationName Data to set
    *  Perturbation query string accessor
    */
    public void setPerturbationName(String namestring)
    {
        if( namestring!=null && namestring.trim().length()>0 )
            perturbationName = namestring.trim();
        else
            perturbationName = null;
    }
    
    //  mass query predicates  //
    
    public void setAvgMass( boolean b )
    {
        useAvgMass = b;   
        useAvgMassGiven = true;
    }

    public boolean getAvgMass() 
    {
        return useAvgMass;
    }
    
    
    public void setMonoisoMass( boolean b )
    {
        useAvgMass = ! b;   
        useAvgMassGiven = true;
    }
    
    
    public void setDiscreteMass( double mass ) 
    {
        exactMass = mass;
    }
    
    
    public double getDiscreteMass() 
    {
        return exactMass;
    }
    
    
    public void setDiscreteMassTolerance( double tolerance ) 
    {
        exactMassTolerance = Math.abs( tolerance );
    }
    
    
    public double getDiscreteMassTolerance() 
    {
        return exactMassTolerance;
    }
    
    
    public void setLowMass( double mass ) 
    {
        lowMass = mass;
    }
    
    
    public double getLowMass() 
    {
        return lowMass;
    }
    
    
    public void setHighMass( double mass ) 
    {
        highMass = mass;
    }

    
    public double getHighMass() 
    {
        return highMass;
    }

    
    /**
    *  Get accessor for sequencePosition
    *  Position to search for the sequence
    */
    public String getSequencePosition()
    {
        return this.sequencePosition;
    }

    /**
    *  Set accessor for sequencePosition
    *  @param sequencePosition Data to set
    *  Position to search for the sequence
    */
    public void setSequencePosition(String sequencePosition)
    {
        this.sequencePosition = sequencePosition;
    }

    /**
    *  Get accessor for glycanId
    *  Stores the input glycan ID that we wish to search for
    */
    public int getGlycanId()
    {
        return this.glycanId;
    }

    /**
    *  Set accessor for glycanId
    *  @param glycanId Data to set
    *  Stores the input glycan ID that we wish to search for
    */
    public void setGlycanId(int glycanId)
    {
        this.glycanId = glycanId;
    }

    
    public String[] getQueryDescription()
    {
        ArrayList<String> descriptions = new ArrayList<String>();
        if ( getTaxonomyName() != null )
        {
            descriptions.add( "taxonomy equals " + getTaxonomyName() );
        }
        if ( getTissueName() != null )
        {
            descriptions.add( "tissue equals " + getTissueName() );
        }
        if ( getDiseaseName() != null )
        {
            descriptions.add( "disease equals " + getDiseaseName() );
        }
        if ( getPerturbationName() != null )
        {
            descriptions.add( "perturbation equals " + getPerturbationName() );
        }
        if ( exactMass > 0 || lowMass > 0 ) 
        {
            String massType = useAvgMass ? "average" : "monoisotopic";
            if ( exactMass > 0 ) 
            {
                descriptions.add( massType 
                                + " mass equals " 
                                + exactMass 
                                + " ± " 
                                + exactMassTolerance 
                                + " Da"
                                );
            } 
            else 
            {
                descriptions.add( massType 
                                + " between " 
                                + lowMass 
                                +  " and " 
                                + highMass 
                                +  " Da"
                                );
            }
        }
        
        if ( getSequenceGWS() != null )
        {
            descriptions.add( 
                " substructure is found"
                + ((sequencePosition != null && ! "Anywhere".equals( sequencePosition ) ) 
                    ? " at " + sequencePosition.toLowerCase() 
                    : "" ) 
            );
        }
        
        for ( SavedGlycanSequenceSearch query : this.additionalQueries ) 
        {
            if ( query.description != null ) 
            {
                descriptions.add( query.description );
            }
        }
        
        return descriptions.toArray( new String[0] );
    }
    
    
    //~~~~~~~~~~~~~~~~~ composition query options ~~~~~~~~~~~~~~~~~~~
    
    public void setExactComp( String s ) { /* TODO */ }
    public void setMinComp( String s )   { /* TODO */ }
    public void setMaxComp( String s )   { /* TODO */ }
    
    //~~~~~~~~~~~~~~~~~~~~~~~~ indexing ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** Default index is the first index in the list of indexes */
    @Override
    public Index<GlycanSequence> getDefaultIndex()
    {
        return indexes.get( 0 );    
    }
    
    
    @Override
    public List<Index<GlycanSequence>> getIndexes()
    {
        return indexes;
    }

    
    public final Class<GlycanSequence> getIndexableType()
    {
        return GlycanSequence.class;
    }
    
    //~~~~~~~~~~~~~~~~~~~~~~~ validation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public void validate()
    {
        //  return unless we're processing input params
        if ( getParameters() == null || getParameters().size() == 0 )
            return;
        
        //  only perform validation once, per request
        if ( validated ) return;               
        
        validated = true;
    }


    public List<GlycanSequence> getQueryResults()
    {
        Criteria query = createCriteria();

        log.info( "Performing query: " + query.toString() );
        setMessage(query.toString());
            
        ScrollableResults scroll = null;
        try 
        {
        
            scroll = query.scroll();
            
            scroll.last();
            setTotalResults(scroll.getRowNumber()+1);
            
            int count = getTotalResults();

            int first = getOffset();
            int max = getMaxResults();
            
            
            if ( first > 0 ) 
            {
                query.setFirstResult(first);
            }

            if ( max > 0 ) 
            {
                query.setMaxResults(max);
            }
            
            
            
            List<GlycanSequence> ret = (List<GlycanSequence>) query.list();

            log.debug( "query executed ok, results count=" + ret.size() );
            return ret;     
        }
        catch ( HibernateException e ) 
        {
            log.warn( "Caught " 
                  + e.getClass().getName() 
                  + " performing query:", e 
                  );
                    
            return Collections.emptyList();            
        }
        finally
        {
            if ( scroll != null )
                scroll.close();
        }
    }
      
    
    /*  execute  *///************************************************
    @SuppressWarnings("unchecked")
    public String execute()
    {
        if ( getParameters() == null || getParameters().size() == 0 ) 
        {
            log.debug("no input params given, returning 'input' view");
            return "input";
        }
    
        if ( getParameters().get("historicalQueriesToRefine") != null) {
            log.debug("refining an existing query, returning 'input' view");
            return "input";
        }
    
        if ( glycanId > 0 ) 
        {
            return "show";
        }
    
        validate();
        setResults( getQueryResults() );
        if ( isNewQuery ) 
        {
            SavedGlycanSequenceSearch savedSearch = new SavedGlycanSequenceSearch();
            savedSearch.queryCriteria = createSerializableCriteria();
            savedSearch.description = join(" AND ", getQueryDescription());
            savedSearch.resultCount = getTotalResults();
            savedSearch.sequence = sequenceGWS;
            
            List<SavedGlycanSequenceSearch> history = this.getQueryHistory();
            history.add(savedSearch);
            this.setQueryHistory(history);              
            this.currentSearch = savedSearch;
        }
        return hasActionErrors() ? "input" : "success";
    }
    
    
    
    
    
} // end class
