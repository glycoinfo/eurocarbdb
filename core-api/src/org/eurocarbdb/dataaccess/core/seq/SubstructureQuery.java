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
*   Last commit: $Rev: 1574 $ by $Author: glycoslave $ on $Date:: 2009-07-24 #$  
*/

package org.eurocarbdb.dataaccess.core.seq;


//  stdlib imports
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.EnumSet;
import java.util.ArrayList;
import java.util.Collections;

//  3rd party imports
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Criteria;

//  eurocarb imports
import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.util.graph.Edge;
import org.eurocarbdb.util.graph.Vertex;
import org.eurocarbdb.util.graph.DepthFirstGraphVisitor;

import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.Anomer;
import org.eurocarbdb.sugar.Linkage;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.GlycosidicLinkage;

import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.HibernateEntityManager;

import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.seq.GlycanResidue;

//  static imports


/**
*   Implements carbohydrate sub-structure searching; specifically, 
*   performs the translation of a given {@link Sugar} (or 
*   {@link Graph} of {@link Linkage}s and {@link Residue}s) to
*   a hibernate query language (HQL) string using a 
*   {@link SubstructureQueryGenerator} along with 
*   {@link SubstructureQuery.Option}s, and returns the resulting
*   substructure search results as a {@link List} of 
*   {@link SubstructureQueryResult}s.
*
*<h2>Usage</h2>
*<pre>
*       //  construct a search Sugar structure (or equivalent search Graph).
*       Sugar search_structure = ...;
*
*       //  create a substruct query from the search structure 
*       SubstructureQuery query = new SubstructureQuery( search_structure );
*
*       //  get the HQL query string for the structure (not needed for search
*       //  -- only if you want it for some reason).
*       String query_string = query.getQueryString();
*
*       //  perform query
*       query.execute();
*
*       //  play with results...
*       List&lt;SubstructureQueryResult&gt; results = query.getResults();
*</pre>
*   Note that by default, returned results can contain the same 
*   {@link GlycanSequence} multiple times, if the substructure is found
*   multiple times within the same structure. Use the 
*   {@link SubstructureQuery.Option#Distinct} option if you want to suppress
*   multiples.
*
*   @see SubstructureQueryResult
*   @see SubstructureQueryCriterion
*   @see SubstructureQueryGenerator
*   @author mjh
*/
public class SubstructureQuery
{
    /** logging handle shared between all SubstructureQuery*.java classes. */
    static final Logger log = Logger.getLogger( SubstructureQuery.class );
    
    static boolean DEBUGGING = log.isDebugEnabled();
    
    /** If true, shows the full string representation of structures that 
    *   match to the {@link #log debug logs} for this class. it's really
    *   slow, so best to set false unless bug hunting. */
    static final boolean VERBOSE_LOGGING = false;
    
    /** This is the maximum number of residues allowed in a search 
    *   structure for a substructure search before the query will be
    *   truncated. The rationale is that the final result set is almost 
    *   always arrived at well before this limit is reached so truncating 
    *   the query makes no difference to the results but cuts the query time
    *   down a lot. */
    public static final int MAX_SUBSTRUCTURE_RESIDUES = 25;
    
    /** The search (sub-)structure as graph */
    private Graph<Linkage,Residue> graph;
    
    /** Set of essentially boolean options to modify characteristics of 
    *   this {@link SubstructureQuery}. Initially null; presence of an option
    *   in the {@link Set} means that option is TRUE. */
    EnumSet<Option> options = EnumSet.noneOf( Option.class );
    
    /** Query search string as SQL; null if query not yet performed. */
    private String queryString = null;
    
    /** Query search results; null if query not yet performed. */
    private List<SubstructureQueryResult> results = null;
    
    //~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
    *   Creates a new SubstructureQuery for the passed {@link Sugar}. 
    */
    public SubstructureQuery( Sugar s )
    {
        assert s != null;
        this.graph = s.getGraph();
    }
    
    
    /** 
    *   Creates a new SubstructureQuery for the passed {@link SugarSequence}. 
    */
    public SubstructureQuery( SugarSequence ss )
    {
        this( ss.getSugar() );
    }
    
    
    /** 
    *   Creates a new SubstructureQuery built from the {@link Graph} of 
    *   the passed {@link Sugar}. 
    */
    public SubstructureQuery( GlycanSequence gs )
    {
        this( gs.getSugar() );
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
    *   Returns the {@link Sugar} {@link Graph} used to construct
    *   this query. 
    */
    protected Graph<Linkage,Residue> getGraph()
    {
        return graph;   
    }
    
    
    /**
    *   Returns a SQL query string for the search substructure
    *   {@link Sugar}/{@link Graph} given at construction.
    */
    public String getQueryString()
    {
        if ( queryString != null )
            return queryString;
        
        if ( DEBUGGING )
        {
            log.debug(
                "generating substructure query for graph:\n"
                + graph.toString()
            );
        }
        
        //  Impl2 is currently the fastest 
        SubstructureQueryGenerator generator 
            = new SubstructureQueryGeneratorImpl2( this );
        
        queryString = generator.getQueryString(); 
        
        //  query string is already logged in the visitor
        if ( log.isTraceEnabled() )
            log.trace("generated query string:\n " + queryString );
        
        return queryString;
    }
    
    
    /** 
    *   Returns this substructure query as a Hibernate 
    *   {@link Criterion} subclass, allowing it to be used in arbitrary
    *   {@link Criteria} queries. 
    *
    *   @see SubstructureQueryCriterion
    */
    public SubstructureQueryCriterion getQueryCriterion()
    {
        return new SubstructureQueryCriterion( this );
    }
    
    
    /**
    *   Returns a {@link List} of results for the given substructure
    *   search, or an empty list if there were no results.
    */
    public List<SubstructureQueryResult> getResults()
    {
        if ( results == null )
            execute();
            
        return results;
    }
    
    
    static final Session getHibernateSession()
    {
        EntityManager em = Eurocarb.getEntityManager();        
        if ( ! (em instanceof HibernateEntityManager) )
            throw new UnsupportedOperationException(
                "Need a HibernateEntityManager to perform substructure query");
            
        //  this is for Hibernate specifically, so cast
        HibernateEntityManager hem = (HibernateEntityManager) em;
        Session hbsession = hem.getHibernateSession();
        
        return hbsession;
    }
    
    
    /** Executes query; results available from {@link #getResults()}. */    
    public void execute()
    {
        //  mjh: the postgres genetic query optimiser must be either turned off
        //  or larger than the number of joins in the query. this is *critical* 
        //  for good performance of larger (> 6-10 residue) substruct queries. 
        //
        //  this setting is normally set on application startup, but if this 
        //  class is run outside this context, it must be set manually using 
        //  code similar to the following:
        //
        //-----
        // Session hbs = getHibernateSession();
        // if ( MAX_SUBSTRUCTURE_RESIDUES > 10 )
        // {
        //     int i = MAX_SUBSTRUCTURE_RESIDUES + 1;
        //     log.debug("setting genetic query optimiser threshold to " + i + " (force geqo OFF)" );
        //     String force_geqo_off = "set geqo_threshold = " + i;
        //     hbs.createSQLQuery( force_geqo_off ).executeUpdate();
        // }
        //-----
        //
        
        //  create & perform main substructure query
        Query q = getQuery(); 
        
        //  for sql version:
        // String sql = HibernateEntityManager.translateHql2Sql( queryString );
        // Query q = hbs.createSQLQuery( sql ).addEntity( GlycanSequence.class);
        
        List<GlycanSequence> sequences = (List<GlycanSequence>) q.list();
        
        //  if there are results, add them to a results list,
        //  else return an empty list.
        if ( sequences != null )
        {
            int count_results = sequences.size();
            
            log.info( "substructure search returned " 
                    + count_results 
                    + " result(s)" );
            
            this.results = new ArrayList<SubstructureQueryResult>( count_results );
            
            for ( int i = 0; i < count_results; i++ )
            {
                GlycanSequence gs = sequences.get(i);
                
                this.results.add( 
                    new SubstructureQueryResult( gs ) );                
            }
        }
        else
        {
            log.info( "substructure search returned no results" );
            this.results = Collections.emptyList();
        }
    }
    
    
    private Query getQuery()
    {
        String query = getQueryString();
        return getHibernateSession()
                .createSQLQuery( queryString )
                .addEntity( GlycanSequence.class );         
    }

    
    /** 
    *   Resets the query so that it may be run again, using for 
    *   example, different {@link Option}s.
    */
    public void reset()
    {
        queryString = null;   
    }
    
    
    /*  query options  */
    
    /** Returns true if the given search {@link Option} is set. */
    public boolean getOption( Option opt )
    {
        return options.contains( opt );   
    }
    

    /** 
    *   Returns true if the given search {@link Option} name is set. 
    *   @throws IllegalArgumentException if option_name is not 
    *   a valid {@link Option} name.
    */
    public boolean getOption( String option_name )
    {
        return options.contains( 
            Enum.valueOf( Option.class, option_name ) );   
    }

    
    /** Sets various search options to modify the characteristics of the search. */
    public SubstructureQuery setOption( Option opt )
    {
        options.add( opt );
        
        return this;
    }
    

    /*  enum Option  *//********************************************* 
    *
    *   Specifies various options and meta-data to modify the performance
    *   and results of a {@link SubstructureQuery}. These are currently
    *   all boolean options, with default value == false.
    *
    *   @author mjh
    */    
    public enum Option
    {
        /** 
        *   Specifies that multiple matches of the query substructure 
        *   should only return the matching {@link GlycanSequence} once,
        *   where ordinarily, every distinct match instance would be 
        *   returned.
        */
        Distinct
        ,
        
        /** 
        *   Specifies that the reducing terminus (root) {@link Residue} of the 
        *   given search sub-structure must also be the reducing terminus (root)
        *   terminus of all matching structures. 
        */
        Must_Include_Reducing_Terminus 
        {
            /** add a predicate that the root residue of the search substruct 
            *   must also be the root of all matching structures. */
            void modifyQuery( SubstructureQueryGenerator q, Set<Option> options )   
            {
                Residue root = q.getSearchGraph().getRootValue();
                String alias = q.getTableAliasFor( root ); 
                
                log.debug("adding root residue predicate for " + root );
                q.addPredicate( alias + ".parent_id is null" );
            }
        }
        ,
        
        /** 
        *   Specifies that all of the non-reducing terminal (leaf) {@link Residue}s 
        *   of the given search sub-structure must also be non-reducing 
        *   terminii in all matching structures. 
        */
        Must_Include_All_Non_Reducing_Terminii
        {
            /** 
            *   Adds a predicate that each leaf in the search substruct must also 
            *   be a leaf in matching structures. 
            */
            void modifyQuery( SubstructureQueryGenerator q, Set<Option> options )   
            {
                Set<Residue> leaves = q.getSearchGraph().getLeafValues();
                if ( leaves.size() == 0 )
                    throw new RuntimeException("Leaves shouldn't ever be empty...");
                
                for ( Residue r : leaves )
                {
                    String alias = q.getTableAliasFor( r ); 
                    log.debug("adding leaf residue predicate for " + r );
                    
                    q.addPredicate( 
                        alias 
                        + ".right_index - "
                        + alias
                        + ".left_index = 1"
                    );
                }
            }
        }
        ,
        
        /**
        *   Causes the query engine not to include constraints for 
        *   linkage elements (anomer, reducing and non-reducing terminal
        *   positions).
        */
        Ignore_Linkages
        // {
        //     /** Empties the linkage predicate list. */
        //     void modifyQuery( SubstructureQueryGenerator q, Set<Option> options )
        //     {
        //         if ( DEBUGGING )
        //             log.debug("Ignore_Linkages: clearing linkage predicates");
                
        //         if ( options.contains( Ignore_Residues ) )
        //             log.warn("Ignore_Linkages & Ignore_Residues are both set");
                
        //         q.linkagePredicates.clear();   
        //     }
        // }
        ,
        
        /**
        *   Causes the query engine not to include constraints for 
        *   residue identity -- effectively making so that only the linkages 
        *   in the query substructures matter. 
        */
        Ignore_Residues
        // {
        //     /** Empties the linkage predicate list. */
        //     void modifyQuery( SubstructureQueryGenerator q, Set<Option> options )
        //     {
        //         if ( DEBUGGING )
        //             log.debug("Ignore_Residues: clearing residue identity predicates");
                
        //         if ( options.contains( Ignore_Linkages ) )
        //             log.warn("Ignore_Linkages & Ignore_Residues are both set");

        //         q.residuePredicates.clear();   
        //     }
        // }
        ,
        
        /**
        *   Causes the query engine to disregard the residue name/identity
        *   of {@link Monosaccharide}s and focus purely on their stereochemistry 
        *   only, effectively disregarding substituents. This option has no 
        *   effect on {@link Residue}s that are {@link Substituent}s.
        */
        Ignore_Monosac_Substituents
        
        ; //--- end of enum constants ^^^
        
        
        /** 
        *   Callback for {@link Option} enum values to modify the 
        *   {@link SubstructureQuery} on which they have been set. 
        */
        void modifyQuery( SubstructureQueryGenerator q, Set<Option> options ) 
        {   
            /* do nothing by default */ 
        }
        
    } // end enum Option
    
} // end class SubstructureQuery


