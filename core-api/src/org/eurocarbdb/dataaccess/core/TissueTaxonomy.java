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
// Generated 3/08/2006 16:48:25 by Hibernate Tools 3.1.0.beta4

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;

import org.eurocarbdb.util.mesh.MeshReference;
import org.eurocarbdb.dataaccess.EntityManager;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class TissueTaxonomy  *//****************************************
*<p>
*   Data access object for TissueTaxonomy (anatomy) information.
*   Due to the sheer number of combinatirial possiblities and the
*   lack of a highly curated vocabulary of taxonomical+anatomical
*   designations, tissue taxonomy (anatomy) is handled independantly
*   of Taxonomy.
*</p>
*<p>
*   Obviously, this can lead to numerous nonsensical Taxonomy-TissueTaxonomy
*   combinations; the idea is that users of Eurocarb will be sufficiently 
*   enlightened to select plausible combinations of taxonomy/anatomy
*   that make sense.
*</p>
*   @author mjh [glycoslave@gmail.com]
*/
public class TissueTaxonomy extends MeshReference 
implements Serializable, Comparable
{
    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Logging handle. */
    protected static final Logger log 
        = Logger.getLogger( Taxonomy.class.getName() );
    
    /** Named query for getting taxonomies by name or synonym. 
    *   @see the TissueTaxonomy.hbm.xml mapping file.  */
    private static final String Q = TissueTaxonomy.class.getName() + '.';

    /** The root tissue of the tissue taxonomy hierachy, which also 
    *   represents the canonical unknown tissue. */
    private static TissueTaxonomy UnknownTissue = null;
        
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private int tissueTaxonomyId;
    
    /** The direct parent TissueTaxonomy of this one. */
    private TissueTaxonomy parentTissueTaxonomy;
    
    /** Given name for this tissue taxon. */
    private String tissueTaxon;
    
    /** MeSH Identifier */
    private String meshId;
    
    /** Textual description of this tissue taxon, as given by MeSH */
    private String description;
    
    /** Date this entry was last modified. */
    private Date dateLastModified;
    
    /** Set of synonyms for this tissue taxon. */
    private Set<TissueTaxonomySynonym> tissueTaxonomySynonyms = new HashSet<TissueTaxonomySynonym>(0);
    
    /** The full set of biological contexts that refer to this tissue taxon. */
    private Set<BiologicalContext> biologicalContexts = new HashSet<BiologicalContext>(0);
    
    /** Hierachical data for this taxon using Nested Sets. */
    private TissueTaxonomyRelations relations;
    
    /** Direct children of this TissueTaxonomy. */
    private Set<TissueTaxonomy> childTissueTaxonomies = new HashSet<TissueTaxonomy>(0);


    //~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor */
    public TissueTaxonomy() {}

    
    /** Minimal constructor */
    TissueTaxonomy(  TissueTaxonomy parentTissueTaxonomy, 
                     String tissueTaxon, 
                     String meshId  ) 
    {
        //  this is a special case of where the ROOT of the tree is intended
        //  to be self-referential to satisfy the requirement that a 
        //  parent cannot be null.
        if ( parentTissueTaxonomy == null )
            parentTissueTaxonomy = this;
        
        this.parentTissueTaxonomy = parentTissueTaxonomy;
        this.tissueTaxon = tissueTaxon;
        this.meshId = meshId;
    }
    
    
    /** Full constructor */
    TissueTaxonomy(  TissueTaxonomy parentTissueTaxonomy, 
                     String tissueTaxon, 
                     String meshId, 
                     String description, 
                     Date dateLastModified, 
                     Set<TissueTaxonomySynonym> tissueTaxonomySynonyms, 
                     Set<BiologicalContext> biologicalContexts, 
                     Set<TissueTaxonomy> childTissueTaxonomies  ) 
    {
        //  this is a special case of where the ROOT of the tree is intended
        //  to be self-referential to satisfy the requirement that a 
        //  parent cannot be null.
        if ( parentTissueTaxonomy == null )
            parentTissueTaxonomy = this;

        this.parentTissueTaxonomy = parentTissueTaxonomy;
        this.tissueTaxon = tissueTaxon;
        this.meshId = meshId;
        this.description = description;
        this.dateLastModified = dateLastModified;
        this.tissueTaxonomySynonyms = tissueTaxonomySynonyms;
        this.biologicalContexts = biologicalContexts;
        this.childTissueTaxonomies = childTissueTaxonomies;
    }
    

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~//

    /** 
    *   Returns the tissue taxonomy representing the root of the 
    *   tissue taxonomy hierachy, which is effectively also equivalent 
    *   to the canonical 'unknown' tissue taxonomy. 
    */
    public static TissueTaxonomy UnknownTissue() 
    {
        if ( UnknownTissue  == null )
        {
            try
            {
                log.debug("looking up canonical tissue taxonomy root");
                UnknownTissue = getEntityManager().lookup( TissueTaxonomy.class, 1 );
                if ( UnknownTissue == null )
                    throw new RuntimeException(
                        "No root tissue (id == 1) in the current data store");
            }
            catch ( Exception e )
            {
                //  mjh: most likely here because there's no data in the 
                //  data store for tissues. for now, complain about it 
                //  and return null. 
                log.warn("Caught " + e + " while looking up root tissue:", e );
                return null;
            }
        }
        
        return UnknownTissue;
    }


    /*  lookupByMeshId  *//******************************************
    *
    *   Returns the TissueTaxonomy with the given mesh id, or null if nothing
    *   matches.
    *
    *   @throws IllegalArgumentException if the given {@link String}
    *   is not a valid MESH id.
    *   @see MeshReference
    */
    public static TissueTaxonomy lookupByMeshId( String mesh_id )
    { 
        if ( mesh_id == null || mesh_id.length() < 3 )
            throw new IllegalArgumentException(
                "Invalid MESH id string '" + mesh_id + "'");
            
        if ( log.isDebugEnabled() )
            log.debug( "looking up tissue matching mesh id=" + mesh_id );
            
        EntityManager em = getEntityManager();
        Object result = em.getQuery( Q + "TISSUE_TAXONOMY_BY_MESH_ID" )
                        .setParameter( "mesh_id", mesh_id )
                        .uniqueResult();
                                            
        return (TissueTaxonomy) result;
    }

    
    /*  lookupNameOrSynonym  *//*************************************
    *
    *   Returns the list of taxonomies whose name or synonym fields 
    *   match the passed search string. Throws an UnsupportedOperationException 
    *   if search string is null or shorter than 3 characters.
    */
    @SuppressWarnings("unchecked")
    public static List<TissueTaxonomy> lookupNameOrSynonym( String search_string )
    throws UnsupportedOperationException
    {
        if ( search_string == null || search_string.length() < 3 )
            throw new UnsupportedOperationException(
                "Tissuet taxonomy name/synonym searches of < 3 chars not supported");
            
        if ( log.isDebugEnabled() )
            log.debug( "looking up tissue taxonomies whose name or synonym matches '" 
                     + search_string 
                     + "'" );
            
        EntityManager em = getEntityManager();
        List result = em.getQuery( Q + "MATCHING_NAME_OR_SYNONYM" )
                        .setParameter( "taxon_name", '%' + search_string + '%' )
                        .list();
                                            
        return (List<TissueTaxonomy>) result;
    }

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Returns {@link getTissueTaxon()}, ie: the name of this tissue. */
    public String getName() {  return this.getTissueTaxon();  }
    
    
    /*  getTissueTaxonomyId  *//*************************************
    *
    */
    public int getTissueTaxonomyId() 
    {
        return this.tissueTaxonomyId;
    }
    
    
    /*  setTissueTaxonomyId  *//*************************************
    *
    */
    public void setTissueTaxonomyId( int tissueTaxonomyId ) 
    {
        this.tissueTaxonomyId = tissueTaxonomyId;
    }


    /*  getParentTissueTaxonomy  *//*********************************
    *
    */
    public TissueTaxonomy getParentTissueTaxonomy() 
    {
        return this.parentTissueTaxonomy;
    }
    
    
    /*  setParentTissueTaxonomy  *//*********************************
    *
    */
    public void setParentTissueTaxonomy( TissueTaxonomy parentTissueTaxonomy ) 
    {
        this.parentTissueTaxonomy = parentTissueTaxonomy;
    }
    

    /*  getTissueTaxon  *//******************************************
    *
    */
    public String getTissueTaxon() 
    {
        return this.tissueTaxon;
    }
    
    
    /*  setTissueTaxon  *//******************************************
    *
    */
    public void setTissueTaxon( String tissueTaxon ) 
    {
        this.tissueTaxon = tissueTaxon;
    }
    

    /*  getMeshId  *//***********************************************
    *
    */
    public String getMeshId() 
    {
        return this.meshId;
    }
    
    
    /*  setMeshId  *//***********************************************
    *
    */
    public void setMeshId( String meshId ) 
    {
        this.meshId = meshId;
    }
    
    
    /** Returns a link (URL) to this MeSH entry for this {@link TissueTaxonomy}. */
    public String getMeshUrl()
    {
        String taxon = this.getTissueTaxon();
        return "http://www.nlm.nih.gov/cgi/mesh/2006/MB_cgi?term=" + taxon;
    }
    

    /*  getDescription  *//******************************************
    *
    *   Returns a medium length description of this tissue taxon. 
    *   Returned text may range from an empty string to a few paragraphs.
    */
    public String getDescription() 
    {
        return this.description;
    }
        
    
    /*  setDescription  *//******************************************
    *
    */
    public void setDescription( String description ) 
    {
        this.description = description;
    }


    /*  getDateLastModified  *//*************************************
    *
    */
    public Date getDateLastModified() 
    {
        return this.dateLastModified;
    }
    
    
    /*  setDateLastModified  *//*************************************
    *
    */
    public void setDateLastModified( Date dateLastModified ) 
    {
        this.dateLastModified = dateLastModified;
    }


    /*  getTissueTaxonomySynonyms  *//*******************************
    *
    */
    public Set<TissueTaxonomySynonym> getTissueTaxonomySynonyms() 
    {
        return this.tissueTaxonomySynonyms;
    }

    
    /*  setTissueTaxonomySynonyms  *//*******************************
    *
    */
    public void setTissueTaxonomySynonyms( Set<TissueTaxonomySynonym> tissueTaxonomySynonyms ) 
    {
        this.tissueTaxonomySynonyms = tissueTaxonomySynonyms;
    }


    /*  getBiologicalContexts  *//***********************************
    *
    */
    public Set<BiologicalContext> getBiologicalContexts() 
    {
        return this.biologicalContexts;
    }
    
    
    /*  setBiologicalContexts  *//***********************************
    *
    */
    public void setBiologicalContexts( Set<BiologicalContext> biologicalContexts ) 
    {
        this.biologicalContexts = biologicalContexts;
    }


    /*  getRelations  *//********************************************
    *
    *   Returns a set of data that specifies the relative position of 
    *   this tissue taxon within the overall hierachy of tissue taxa. 
    */ 
    public TissueTaxonomyRelations getRelations() 
    {
        return this.relations;
    }

    
    /*  setRelations  *//********************************************
    *
    *   Sets the position of this tissue taxon in the overall hierachy of tissue taxa 
    *   -- be careful using this method, it can break stuff badly!!! 
    */
    public void setRelations( TissueTaxonomyRelations relations ) 
    {
        this.relations = relations;
    }


    /*  getChildTissueTaxonomies  *//******************************** 
    *   
    *   Gets the set of tissue taxonomies that are direct children of this one. 
    *
    */
    public Set<TissueTaxonomy> getChildTissueTaxonomies() 
    {
        return this.childTissueTaxonomies;
    }
    
    
    /*  setChildTissueTaxonomies  *//********************************
    *
    *   Sets the set of tissue taxonomies that are direct children of this one. 
    *
    */
    public void setChildTissueTaxonomies( Set<TissueTaxonomy> childTissueTaxonomies ) 
    {
        this.childTissueTaxonomies = childTissueTaxonomies;
    }
   
   
    /*  getAllParentTissueTaxonomies  *//****************************
    *
    *   Returns a list of all the parent tissue taxonomies of this
    *   tissue taxon up to the root of the hierachy such that the root
    *   tissue taxonomy will be element 0, and the immediate parent of 
    *   this tissue taxon as the last element in the list. Returns an
    *   empty list if this tissue taxon has no parents.
    *
    *   @author mjh
    */
    public List<TissueTaxonomy> getAllParentTissueTaxonomies()
    {
        List<TissueTaxonomy> list = new ArrayList<TissueTaxonomy>();
        TissueTaxonomy cursor = this.getParentTissueTaxonomy();
        
        while ( cursor != null )
        {
            list.add( 0, cursor );
            cursor = cursor.getParentTissueTaxonomy();
            if ( list.get( 0 ) == cursor ) break;   
        }
         
        return list;
    }


    public List<Object[]> getListOfSubTissuesGlycanSequenceCount() 
    {
        EntityManager em = getEntityManager();
        try
        {
            Object result = em.getQuery( TissueTaxonomy.class.getName()
                                       + ".count_structures_for_sub_tissues" )
                              .setComment( "TissueTaxonomy.getListOfSubTissuesGlycanSequenceCount")
                              .setParameter( "tissue_taxonomy_id", this.getTissueTaxonomyId() )
                              .list();
                              
            if ( result == null ) 
                return Collections.emptyList(); 
            
            assert result instanceof List;
            List<Object[]> result_list = (List<Object[]>) result;
            
            return result_list;
        }
        catch ( Exception e ) 
        {  
            log.warn( "Caught exception while counting sequences for tissue", e ); 
            return Collections.emptyList();//Collections.emptyMap();
        }
              
    }

    /** 
    *   Returns a count {@link GlycanSequence}s associated to this
    *   {@link TissueTaxonomy} and all its sub-tissueTaxonomies.
    */
    public int getSubTissueTaxonomiesGlycanSequenceCount() 
    {
        
        
        EntityManager em = getEntityManager();
        try
        {
            Object result = em.getQuery( "org.eurocarbdb.dataaccess.core.TissueTaxonomy." 
                                       + "count_structures_for_this_tissue_taxonomy" )
                              .setParameter( "tissue_taxonomy_id", this.getTissueTaxonomyId() )
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
            log.warn( "Caught exception while counting sequences for tissue taxonomy", e ); 
            return 0;
        }
    }


    public int compareTo( Object obj )
    {
        return this.toString().compareTo( obj.toString() );
    }
    
    
    public String toString()
    {
        return this.getTissueTaxon();
    }
    
    /** 
    *   Returns true if this TissueTaxonomy is the canonical 
    *   root TissueTaxonomy. 
    */
    public boolean isRoot()
    {
        if (  this.tissueTaxonomyId == 0 || this.tissueTaxon == null )
            return false;
        
        return ( this.tissueTaxonomyId == 1 
            || this.tissueTaxon.equalsIgnoreCase("Anatomy")
            || this.getParentTissueTaxonomy() == this );
    }
   
} // end class
