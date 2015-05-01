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

package org.eurocarbdb.dataaccess.core;

import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Collections;

import org.apache.log4j.Logger;

import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import org.eurocarbdb.dataaccess.EntityManager;
//import org.eurocarbdb.util.MeshReference;
import org.eurocarbdb.util.ncbi.NcbiTaxonomy;
import org.eurocarbdb.util.StringUtils;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.util.StringUtils.ucfirst;


/*  class Taxonomy  *//**********************************************
*
*   Data access object for Taxonomy information, which together with 
*   {@link TissueTaxonomy}, are the minimum information comprising a
*   {@link BiologicalContext}.
*
*   @see BiologicalContext
*
*   @author mjh 
*/
public class Taxonomy 
extends BasicEurocarbObject 
implements Serializable, Comparable
{
    /**
    *   Enumeration of valid values for Taxonomy rank, as defined
    *   by standard taxonomy nomenclature. Note that NCBI Taxonomy
    *   uses additional ranks such as 'subclass' and 'subfamily'.
    */
    public enum Rank
    {
        Kingdom,
        Phylum,
        Class,
        Order,
        Family,
        Genus,
        Species,
        Subspecies,
        Unranked;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Logging handle. */
    protected static final Logger log 
        = Logger.getLogger( Taxonomy.class );

    private static final String Q = "org.eurocarbdb.dataaccess.core.Taxonomy.";
        
    /** Named query for getting taxonomies by name or synonym. 
    *   @see the Taxonomy.hbm.xml mapping file.  */
    private static final String QUERY_MATCHING_NAME_OR_SYNONYM
        = "org.eurocarbdb.dataaccess.core.Taxonomy.MATCHING_NAME_OR_SYNONYM";

    /** Named query for getting taxonomies by NCBI ID. 
    *   @see the Taxonomy.hbm.xml mapping file.  */
    private static final String QUERY_MATCHING_NCBI_ID
        = "org.eurocarbdb.dataaccess.core.Taxonomy.MATCHING_NCBI_ID";

    private static final String QUERY_ALL_CHILD_TAXONOMIES
        = "org.eurocarbdb.dataaccess.core.Taxonomy.ALL_CHILD_TAXONOMIES";

    private static final String QUERY_ALL_CHILD_TAXONOMIES_WITH_CONTEXT
        = "org.eurocarbdb.dataaccess.core.Taxonomy.ALL_CHILD_TAXONOMIES_WITH_CONTEXT";
        
    /** Root taxonomy. @see #UnknownTaxonomy() */
    private static Taxonomy UnknownTaxonomy = null;

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private int taxonomyId;
    
    private Taxonomy parentTaxonomy;
    
    //private NcbiTaxonomy ncbiTaxonomy;
    private int ncbiId;
    
    private String rank;
    
    /** Taxon name. Note that internally this is always lower-case, and 
    *   first-letter-upper-cased on access. */
    private String taxon;
    
    private TaxonomyRelations relations;
    
    private Set<Taxonomy> childTaxonomies = new HashSet<Taxonomy>(0);
    
    private Set<BiologicalContext> biologicalContexts = new HashSet<BiologicalContext>(0);
    
    private Set<TaxonomySynonym> taxonomySynonyms = new HashSet<TaxonomySynonym>(0);

    private Set<TaxonomySubtype> taxonomySubtypes = new HashSet<TaxonomySubtype>(0);

    private Set<TaxonomySubtype> taxonomySupertypes = new HashSet<TaxonomySubtype>(0);
    
    
    //~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor */
    public Taxonomy() {}

    /** Minimal constructor */
    Taxonomy( Taxonomy parentTaxonomy, int ncbi_id, String rank, String taxon ) 
    {
        //  this is a special case of where the ROOT of the tree is intended
        //  to be self-referential to satisfy the requirement that a 
        //  parent cannot be null.
        if ( parentTaxonomy == null ) 
            parentTaxonomy = this;
        
        this.parentTaxonomy = parentTaxonomy;
        this.ncbiId = ncbi_id;
        this.rank = rank;
        this.taxon = taxon;
    }
    
    /** Full constructor */
    Taxonomy( 
        Taxonomy parentTaxonomy, 
        int ncbi_id, 
        String rank, 
        String taxon, 
        TaxonomyRelations relations, 
        Set<Taxonomy> childTaxonomies, 
        Set<BiologicalContext> biologicalContexts, 
        Set<TaxonomySynonym> taxonomySynonyms,
        Set<TaxonomySubtype> taxonomySubtypes,
        Set<TaxonomySubtype> taxonomySupertypes
    ) 
    {
        //  this is a special case of where the ROOT of the tree is intended
        //  to be self-referential to satisfy the requirement that a 
        //  parent cannot be null.
        if ( parentTaxonomy == null ) 
            parentTaxonomy = this;
        
        this.parentTaxonomy = parentTaxonomy;
        this.ncbiId = ncbi_id;
        this.rank = rank;
        this.taxon = taxon;
        this.relations = relations;
        this.childTaxonomies = childTaxonomies;
        this.biologicalContexts = biologicalContexts;
        this.taxonomySynonyms = taxonomySynonyms;
        this.taxonomySubtypes = taxonomySubtypes;
        this.taxonomySupertypes = taxonomySupertypes;
    }
    

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /*  UnknownTaxonomy  *//*****************************************
    *
    *   Returns the taxonomy representing the root of the taxonomy hierachy,
    *   note that this is effectively also equivalent to the canonical 
    *   'unknown' taxonomy. 
    */
    public static Taxonomy UnknownTaxonomy() 
    {
        if ( UnknownTaxonomy == null )
        {
            try
            {
                log.debug("looking up canonical taxonomy root");
                UnknownTaxonomy = getEntityManager().lookup( Taxonomy.class, 1 ); 
                if ( UnknownTaxonomy == null )
                    throw new RuntimeException(
                        "No root taxonomy (id == 1) in the current data store");
            }
            catch ( Exception e )
            {
                //  mjh: most likely here because there's no data in the 
                //  data store for tissues. for now, complain about it 
                //  and return null. 
                log.warn("Caught " + e + " while looking up root taxonomy:", e );
                log.warn("This may mean there's no Taxonomy information in the data store");
                UnknownTaxonomy = new Taxonomy();
            }
        }

        return UnknownTaxonomy;
    }
    
        
    /*  lookupNameOrSynonym  *//*************************************
    *
    *   Returns the list of taxonomies whose name or synonym fields 
    *   match the passed search string. 
    *   @throws UnsupportedOperationException 
    *   if search string is null or shorter than 3 characters.
    */
    @SuppressWarnings("unchecked")
    public static List<Taxonomy> lookupNameOrSynonym( String search_string )
    throws UnsupportedOperationException
    {
        if ( search_string == null || search_string.length() < 3 )
            throw new UnsupportedOperationException(
                "Taxonomy name/synonym searches of < 3 chars not supported");
            
        if ( log.isDebugEnabled() )
            log.debug( "looking up taxonomies whose name or synonym loosely matches '" 
                     + search_string 
                     + "'" );
            
        EntityManager em = getEntityManager();
        List result = em.getQuery( QUERY_MATCHING_NAME_OR_SYNONYM )
                        .setParameter( "taxon_name", '%' + search_string.toLowerCase() + '%' )
                        .list();
                                            
        return (List<Taxonomy>) result;
    }
   
    
    /*  lookupExactNameOrSynonym  *//*************************************
    *
    *   Returns the list of taxonomies whose name or synonym fields 
    *   exactly match the passed search string. Otherwise identical to
    *   {@link #lookupNameOrSynonym}.
    */
    //@SuppressWarnings("unchecked")
    public static List<Taxonomy> lookupExactNameOrSynonym( String search_string )
    throws UnsupportedOperationException
    {
        if ( search_string == null )
            throw new UnsupportedOperationException(
                "search_string argument cannot be null");
            
        if ( log.isDebugEnabled() )
            log.debug( "looking up taxonomies whose name or synonym exactly matches '" 
                     + search_string 
                     + "'" );
            
        EntityManager em = getEntityManager();
        Object result = em.getQuery( QUERY_MATCHING_NAME_OR_SYNONYM )
                        .setParameter( "taxon_name", search_string.toLowerCase() )
                        .list();
                                         
        if ( result == null )
            return Collections.emptyList();
        
        return (List<Taxonomy>) result;
    }
    
    
    /*  lookupNcbiId  *//********************************************
    *
    *   Returns the taxonomy corresponding with the passed NCBI Taxonomy 
    *   Id, or null if there is no match.
    */
    public static Taxonomy lookupNcbiId( int ncbi_id )
    {
        if ( log.isDebugEnabled() )
            log.debug( "looking up taxonomy whose NCBI Id == " + ncbi_id );

        EntityManager em = getEntityManager();
        return (Taxonomy) em.getQuery( QUERY_MATCHING_NCBI_ID )
                            .setParameter( "ncbi_id", ncbi_id )
                            .uniqueResult();
    }
    
    
    /* mjh todo: just an idea...
    
    public Graph<Integer,Taxonomy> getGraph( Collection<Taxonomy> taxa )
    {
        Graph<Integer,Set<Taxonomy>> g = Graph.create();
        
        for ( Taxonomy t : taxa )
        {
            g.addVertex( t );
            
        }
        
        return g;
    }
    */
    
    
    public boolean isChildOf( Taxonomy t )
    {
        int i = this.getRelations().getLeftIndex();
        return i > t.getRelations().getLeftIndex() 
            && i < t.getRelations().getRightIndex();
    }
    
    
    public boolean isParentOf( Taxonomy t )
    {
        int i = t.getRelations().getLeftIndex();
        return i > this.getRelations().getLeftIndex() 
            && i < this.getRelations().getRightIndex();
    }
    
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /*  getTaxonomyId  *//*******************************************
    *
    *   Returns canonic (Eurocarb-supplied) taxonomy id. Note that
    *   this is *not* equal to its NCBI Taxonomy Id.
    */
    public int getTaxonomyId() 
    {
        return this.taxonomyId;
    }
    
    /*  setTaxonomyId  *//*******************************************
    *
    *   Returns canonic Eurocarb taxonomy id. Note that
    *   this is *not* equal to its NCBI Taxonomy Id.
    */
    public void setTaxonomyId(int taxonomyId) 
    {
        this.taxonomyId = taxonomyId;
    }

    /*  getParentTaxonomy  *//***************************************
    *
    *   Returns the taxonomy that is the parent of this taxon
    *   in the taxonomy tree hierachy.
    */
    public Taxonomy getParentTaxonomy() 
    {
        return this.parentTaxonomy;
    }
    
    /*  setParentTaxonomy  *//***************************************
    *
    *   Sets the passed taxonomy as the parent of this taxon
    *   in the taxonomy tree hierachy.
    */
    public void setParentTaxonomy( Taxonomy parentTaxonomy ) 
    {
        /* 
        assert parentTaxonomy != null;
        assert parentTaxonomy != this;
        */        
        this.parentTaxonomy = parentTaxonomy;
    }

    /*  getNcbiId  *//***********************************************
    *
    *   Returns the NCBI Taxonomy Id for this taxon.
    *   @see NcbiTaxonomy
    */
    public int getNcbiId() 
    {
        return this.ncbiId;
    }

    /*  setNcbiId  *//***********************************************
    *
    *   Sets the NCBI Taxonomy Id for this taxon.
    *   @see NcbiTaxonomy
    */
    public void setNcbiId( int id ) 
    {
        assert id > 0;
        this.ncbiId = id;
    }
    
    public int getncbiId() 
    {
        return this.ncbiId;
    }

    /*  setNcbiId  *//***********************************************
    *
    *   Sets the NCBI Taxonomy Id for this taxon.
    *   @see NcbiTaxonomy
    */
    public void setncbiId( int id ) 
    {
        assert id > 0;
        this.ncbiId = id;
    }
    
    
    /*  getNcbiTaxonomy  *//*****************************************
    *
    *   Gets the NCBI Taxonomy associated with this taxon. This method
    *   is not finished!
    */
    public NcbiTaxonomy getNcbiTaxonomy()
    {
        return null;   
    }
    
    
    /*  getRank  *//*************************************************
    *
    *   Returns the NCBI Taxomomy rank of this taxon, where rank
    *   may be equal to any of the taxonomic classifications defined
    *   in the Taxonomy.Rank enum.
    */
    public String getRank() 
    {
        return this.rank;
    }
    
    
    /** 
    *   Returns whether this Taxonomy has a valid rank, that is, the 
    *   rank can be found in the {@link Taxonomy.Rank} enumeration.
    */
    public boolean hasValidRank()
    {
        try 
        {  
            Rank.valueOf( StringUtils.ucfirst(this.rank) );  
        } 
        catch ( IllegalArgumentException e ) 
        {  
            return false;  
        }
        return true;
    }
    
    
    /** Returns true if this taxonomy is the root taxonomy of the taxonomy tree. */
    public boolean isRoot()
    {
        return ( this.taxonomyId == 1 || this.taxon.equals("root") );
    }
    
    
    /*  setRank  *//*************************************************
    *
    *   Sets the NCBI Taxomomy rank of this taxon, where rank
    *   may be equal to any of the taxonomic classifications defined
    *   in the Taxonomy.Rank enum.
    */
    public void setRank( String rank ) 
    {
        this.rank = rank;
    }

    
    /*  getTaxon  *//************************************************
    *
    *   Returns the name of this taxon.
    */
    public String getTaxon() 
    {
        if ( this.isRoot() ) 
            return "root";
        
        return ucfirst( this.taxon );
    }
    
    
    /*  setTaxon  *//************************************************
    *
    *   Sets the name of this taxon.
    *   @throws NullPointerException if taxon is null.
    */
    public void setTaxon( String taxon ) 
    {
        if ( taxon == "Taxonomy" ) 
            taxon = "root";
        
        this.taxon = taxon.toLowerCase();
    }

    
    /** Returns {@link getTaxon()}, ie: the name of this taxonomy. */
    public String getName() {  return this.getTaxon();  }

    
    /** 
    *   Get all the synonyms for this Taxonomy as a list of strings
    */
    public List<String> getSynonyms()
    {
        List<String> synonyms = new java.util.ArrayList<String>();
        for ( TaxonomySynonym synonym : getTaxonomySynonyms() ) 
        {
            synonyms.add( synonym.getSynonym() );
        }
        
        return synonyms;
    }
    
    
    /** */
    public TaxonomyRelations getRelations() 
    {
        return this.relations;
    }

    
    /** */
    public void setRelations( TaxonomyRelations relations ) 
    {
        this.relations = relations;
    }

    
    /** 
    *   Returns a {@link Set} of all the {@link Taxonomy}s below
    *   this Taxonomy in the hierachy. 
    */
    public Set<Taxonomy> getChildTaxonomies() 
    {
        return this.childTaxonomies;
    }

    
    /** */
    public void setChildTaxonomies(Set<Taxonomy> childTaxonomies) 
    {
        this.childTaxonomies = childTaxonomies;
    }

    
    /** 
    *   Returns the {@link Set} of {@link BiologicalContext}s this
    *   taxonomy has been used in.
    */
    public Set<BiologicalContext> getBiologicalContexts() 
    {
        return this.biologicalContexts;
    }

    
    /** may be removed in future! */
    public void setBiologicalContexts(Set<BiologicalContext> biologicalContexts) 
    {
        this.biologicalContexts = biologicalContexts;
    }

    
    /** Returns the set of synonyms for this taxon. */
    public Set<TaxonomySynonym> getTaxonomySynonyms() 
    {
        return this.taxonomySynonyms;
    }
    
    
    /** */
    public void setTaxonomySynonyms(Set<TaxonomySynonym> taxonomySynonyms) 
    {
        this.taxonomySynonyms = taxonomySynonyms;
    }
    
    
    /** */
    public Set<TaxonomySubtype> getTaxonomySubtypes() 
    {
        return this.taxonomySubtypes;
    }
    
    
    /** */
    public void setTaxonomySubtypes(Set<TaxonomySubtype> taxonomySubtypes) 
    {
        this.taxonomySubtypes = taxonomySubtypes;
    }

    
    /** */
    public Set<TaxonomySubtype> getTaxonomySupertypes() 
    {
        return this.taxonomySupertypes;
    }
    
    
    /** */
    public void setTaxonomySupertypes(Set<TaxonomySubtype> taxonomySupertypes) 
    {
        this.taxonomySupertypes = taxonomySupertypes;
    }
    
    
    public Set<Taxonomy> getSubTaxonomies() 
    {
        HashSet<Taxonomy> ret = new HashSet<Taxonomy>();
        for( TaxonomySubtype ts: getTaxonomySubtypes() )
            ret.add( ts.getSubTaxonomy());
        return ret;
    }


    public Set<Taxonomy> getSuperTaxonomies() 
    {
        HashSet<Taxonomy> ret = new HashSet<Taxonomy>();
        for( TaxonomySubtype ts: getTaxonomySupertypes() )
            ret.add( ts.getTaxonomy());
        return ret;
    }
    

    //~~~ ADDED CONVENIENCE FUNCTIONS ~~~//
    
    /*  getAllParentTaxonomies  *//**********************************
    *
    *   Returns a list of all the parent taxonomies of this
    *   taxon up to the root of the hierachy such that the root
    *   taxonomy will be element 0, and the immediate parent of 
    *   this taxon as the last element in the list. Returns an
    *   empty list if this taxon has no parents.
    *
    *   @author mjh
    */
    public List<Taxonomy> getAllParentTaxonomies()
    {
        /*
        List<Taxonomy> list = new ArrayList<Taxonomy>();
        Taxonomy cursor = this.getParentTaxonomy();
        
        while ( cursor != null )
        {
            list.add( 0, cursor );
            cursor = cursor.getParentTaxonomy();
            if ( list.get( 0 ) == cursor ) break;   
        }
         
        return list;
        */
        
        // /* --- mjh: this query is actually slower than the above,  ---
        // --- even though the query here is a single statement    ---
        // --- and the above is n-1 selects (!!!). this may well   ---
        // --- be a database optimisation issue.                   ---
        EntityManager em = getEntityManager();
        
        Object result = em.getQuery( "org.eurocarbdb.dataaccess.core.Taxonomy.ALL_PARENT_TAXONOMIES" )
                        .setParameter( "taxonomy_id", this.getTaxonomyId() )
                        .list();

        if ( result == null )
            return Collections.emptyList();
                        
        return (List<Taxonomy>) result;
    }
    
    
    /** 
    *   Returns the {@link List} of unique {@link GlycanSequence}s that are directly 
    *   associated to this {@link Taxonomy}.
    */
    public List<GlycanSequence> getAssociatedGlycanSequences()
    {
        Object result = getEntityManager()
                        .getQuery( Taxonomy.class.getName() + ".GET_SEQUENCES")
                        .setComment("Taxonomy.getAssociatedGlycanSequences")
                        .setParameter("id", this.getTaxonomyId() )
                        .list();
                        
        if ( result == null || !(result instanceof List))
            return Collections.emptyList();
        
        return (List<GlycanSequence>) result;
    }
    
    
    /** 
    *   Returns a subset of the parent taxonomies of this taxon, 
    *   specifically, those that have taxonomically significant
    *   ranks (such as Kingdom, Phyla, Family, etc), oldest parent first. 
    */
    public List<Taxonomy> getParentTaxonomySubset()
    {
        EntityManager em = getEntityManager();
        
        Object result = this.getParentsByIteration(); 
        /* 
        em.getQuery( "org.eurocarbdb.dataaccess.core.Taxonomy.selective_parent_taxonomies" )
                      .setParameter( "taxonomy_id", this.getTaxonomyId() )
                      .list();
        */
        if ( result == null )
            return Collections.emptyList();
        
        List<Taxonomy> smaller_list = new ArrayList<Taxonomy>();
        for ( Taxonomy t : (List<Taxonomy>) result )
            if ( t.hasValidRank() )
                smaller_list.add( t );
            
        return smaller_list;
    }      

    
    private Object getParentsByIteration()
    {
        List<Taxonomy> list = new ArrayList<Taxonomy>();
        Taxonomy cursor = this.getParentTaxonomy();
        
        while ( cursor != null )
        {
            list.add( 0, cursor );
            cursor = cursor.getParentTaxonomy();
            if ( list.get( 0 ) == cursor ) break;   
        }
         
        return list;        
    }
    

    /*  getAllChildTaxonomies  *//****************************
    *
    *   Returns an iterator over an unsorted collection of all taxonomies
    *   that are descendants of this taxonomy
    */
    @Deprecated
    @SuppressWarnings("unchecked")    
    public List<Taxonomy> getAllChildTaxonomies()
    {
        EntityManager em = getEntityManager();
        
        List<Taxonomy> result = em.getQuery( QUERY_ALL_CHILD_TAXONOMIES )
                                  .setParameter( "taxonomy_id", this.getTaxonomyId() )
                                  .list();
                
        assert result != null;

        return result;
    }

    
    /*  getAllChildTaxonomiesWithContext  *//****************************
    *
    *   Returns an iterator over an unsorted collection of all taxonomies
    *   that are descendants of this taxonomy, and that have been used in 
    *   a biological context somewhere. 
    */
    @Deprecated
    @SuppressWarnings("unchecked")    
    public List<Taxonomy> getAllChildTaxonomiesWithContext()
    {
        EntityManager em = getEntityManager();
        
        List<Taxonomy> result = em.getQuery( QUERY_ALL_CHILD_TAXONOMIES_WITH_CONTEXT )
                                  .setComment("Taxonomy.getAllChildTaxonomiesWithContext")
                                  .setParameter( "taxonomy_id", this.getTaxonomyId() )
                                  .list();
                
        assert result != null;

        return result;
    }
    

    /*  getAllTaxonomiesWithContext  *//****************************
    *
    *   Returns a list of taxonomies that are either descended from this 
    *   taxonomy (including this taxonomy) and have a biological context 
    *   associated with it
    *   @return List of taxonomies
    *   @deprecated is this method even used?
    */
    @Deprecated
    public List<Taxonomy> getAllTaxonomiesWithContext() 
    {
        List<Taxonomy> results = new ArrayList<Taxonomy>();
        results.addAll(getAllChildTaxonomiesWithContext());
        if (getBiologicalContexts().size() > 0)
            results.add(this);
        
        return results;
    }
    


    /** 
    *   Doco out of date,    
    *   see Taxonomy.count_structures_for_sub_taxonomies query.
    */
    public List<Object[]> getListOfSubTaxonomiesGlycanSequenceCount() 
    {
        EntityManager em = getEntityManager();
        try
        {
            Object result = em.getQuery( Taxonomy.class.getName()
                                       + ".count_structures_for_sub_taxonomies" )
                              .setComment( "Taxonomy.getListOfSubTaxonomiesGlycanSequenceCount")
                              .setParameter( "taxonomy_id", this.getTaxonomyId() )
                              .list();
                              
            if ( result == null ) 
                return Collections.emptyList(); 
            
            assert result instanceof List;
            List<Object[]> result_list = (List<Object[]>) result;
            
            return result_list;
        }
        catch ( Exception e ) 
        {  
            log.warn( "Caught exception while counting sequences for taxonomy", e ); 
            return Collections.emptyList();//Collections.emptyMap();
        }
    }
    

    /** 
    *   Returns a {@link List} of all child taxonomies of this {@link Taxonomy} 
    *   and a count of all the {@link GlycanSequence}s associated to all their
    *   subtaxonomies.  Note that only taxonomy ids that have at
    *   least 1 associated sequence will be present in the Map.
    */
    public List<Object[]> getListOfChildTaxonomiesGlycanSequenceCount() 
    {
        EntityManager em = getEntityManager();
        try
        {
            Object result = em.getQuery( "org.eurocarbdb.dataaccess.core.Taxonomy." 
                                       + "count_structures_for_child_taxonomies" )
                              .setParameter( "taxonomy_id", this.getTaxonomyId() )
                              .list();
                              
            if ( result == null ) 
                return Collections.emptyList(); 
            
            assert result instanceof List;
            List<Object[]> result_list = (List<Object[]>) result;
            
            return result_list;
        }
        catch ( Exception e ) 
        {  
            log.warn( "Caught exception while counting sequences for taxonomy", e ); 
            return Collections.emptyList();//Collections.emptyMap();
        }
              
    }


    /** 
    *   Returns a count {@link GlycanSequence}s associated to this
    *   {@link Taxonomy} and all its sub-taxonomies.
    */
    public int getSubTaxonomiesGlycanSequenceCount() 
    {
        
        
        EntityManager em = getEntityManager();
        try
        {
            Object result = em.getQuery( "org.eurocarbdb.dataaccess.core.Taxonomy." 
                                       + "count_structures_for_this_taxonomy" )
                              .setParameter( "taxonomy_id", this.getTaxonomyId() )
                              .list();                              

            if ( result == null ) 
                return 0; 
            
            assert result instanceof List;
            List result_list = (List) result;
        if( result_list.size()==0 )
        return 0;

            return (Integer)result_list.iterator().next();
        }
        catch ( Exception e ) 
        {  
            log.warn( "Caught exception while counting sequences for taxonomy", e ); 
            return 0;
        }
    }
    
    
    /** 
    *   Get all the biological contexts found in the database that have this
    *   particular Taxonomy as the parent of their taxonomy tree, or are
    *   associated with this taxonomy
    *   @return Set of biological contexts
    */
    public Set<BiologicalContext> getAllBiologicalContexts() 
    {
        Set<BiologicalContext> contexts = new HashSet<BiologicalContext>(); 

        contexts.addAll(getBiologicalContexts());
        
        for (Taxonomy tax : getAllChildTaxonomiesWithContext()) 
            contexts.addAll(tax.getBiologicalContexts());
        
        return contexts;
    }
    
    
    /** Compares taxon names alphabetically. */
    public int compareTo( Object obj )
    {
        return this.toString().compareTo( obj.toString() );
    }
    
    
    /** Returns taxon name. @see #getTaxon() */
    public String toString()
    {
        return this.getTaxon(); 
    }
    
    
    public boolean equals( Object x )
    {
        if ( this == x )
            return true;        
        if ( (x == null) || (x.getClass() != this.getClass()) )
            return false;
        
        // objects are the same class
        Taxonomy t = (Taxonomy) x;

        return this.ncbiId==t.ncbiId && this.rank.equals(t.rank) && this.taxon.equals(t.taxon);
    }
    
    
    public int hashCode() 
    {
        String unique = "" + ncbiId + taxon + rank;
        return unique.hashCode(); 
    }
    
   
} // end of class
