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
package org.eurocarbdb.dataaccess.core;


//  stdlib imports
import java.util.Set;
import java.util.List;
import java.util.HashSet;

//  3rd party imports
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;

//  eurocarb imports
import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.dataaccess.HibernateEntityManager;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.seq.SubstructureQuery;
import org.eurocarbdb.dataaccess.core.seq.SubstructureQueryCriterion;
import org.eurocarbdb.dataaccess.indexes.IndexByContributedDate;
import org.eurocarbdb.dataaccess.indexes.Index;

//  static imports
import static java.util.Collections.emptyList;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.not;
import static org.hibernate.criterion.Restrictions.idEq;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.dataaccess.core.seq.SubstructureQuery.Option.*;


/*  class GlycanSequenceRelations  *//*************************************
*<p>
*   Collection of utility methods for finding {@link GlycanSequence}s that
*   are structurally related to each other. Most of these methods are 
*   simple wrappers around the functionality of the {@link SubstructureQuery}
*   and {@link SubstructureQueryCriterion} classes.
*</p>
*<p>
*   Relational queries generally have 2 forms, one that returns a {@link List}
*   of {@link GlycanSequence}s ordered by a given {@link Index}, and a second
*   form of the same query that returns the underlying query as a 
*   {@link DetachedCriteria}.
*</p>
*
*   @see GlycanSequence#getRelations()
*   @see SubstructureQuery
*   @see SubstructureQueryCriterion
*   @see org.eurocarbdb.action.core.SearchSubstructure
*   @see Criteria
*   @see DetachedCriteria
*   @see HibernateEntityManager#convertToCriteria
*
*   @version $Rev: 1574 $
*   @author mjh
*/
public class GlycanSequenceRelations 
{
    /** The {@link GlycanSequence} that any relations query
    *   will be based on. */
    private final GlycanSequence gs;

    private Index<GlycanSequence> index 
        = new IndexByContributedDate<GlycanSequence>();
    

    private GlycanSequenceRelations( GlycanSequence gs )
    {
        this.gs = gs;    
    }
    
    
    /** Factory contructor. */
    public static GlycanSequenceRelations of( GlycanSequence gs )
    {
        return new GlycanSequenceRelations( gs );            
    }
    
    
    /** 
    *   Returns a {@link Criterion} that can be used in arbitrary 
    *   {@link Criteria} queries to search for {@link GlycanSequence}s
    *   that have this GlycanSequence as a substructure.
    */
    public SubstructureQueryCriterion asCriterion()
    {
        return new SubstructureQueryCriterion( new SubstructureQuery( gs ));
    }
    
    
    /**
    *   Returns a {@link List} of {@link GlycanSequence}s that are equivalent
    *   to (ie: more definite than) this one, ordered by {@link #getIndex}
    *   and limited to the given number of structures; see also
    *   {@link #getEquivalentsCriteria}.
    */
    public List<GlycanSequence> getEquivalents( int max_results )
    {
        return getResults( getEquivalentsCriteria(), max_results );
    }
    
    
    /**
    *<p>
    *   Returns a {@link DetachedCriteria} instance that searches for 
    *   {@link GlycanSequence}s that are equivalent to this one;
    *   "equivalent" in this context means structures that are more
    *   <em>definite</em> (that is, fewer <em>indefinite</em>(unknown)
    *   structural elements) versions of this sequence. This means
    *   that if this sequence contains no indefinite elements, that 
    *   no sequences will be returned from search.
    *</p>
    *
    *   @see HibernateEntityManager#convertToCriteria
    *   @see GlycanSequence#isDefinite
    */
    public DetachedCriteria getEquivalentsCriteria()
    {
        SubstructureQueryCriterion crit = this.asCriterion();
        
        crit.getSubstructureQuery()
            .setOption( Must_Include_All_Non_Reducing_Terminii )
            .setOption( Must_Include_Reducing_Terminus )
        ;
            
        return getDetachedCriteria()
            .setComment( "GlycanSequenceRelations.getEquivalentsCriteria" )
            .add( not( idEq( gs.getGlycanSequenceId() ) ) )
            .add( eq( "residueCount", gs.getResidueCount() ) )
            .add( crit )
        ;
    }
    
    
    /**
    *   Returns {@link GlycanSequence}s corresponding to 
    *   {@link #getLinkageIsomersCriteria}, ordered by {@link #getIndex}
    *   and limited to the given number of structures.
    *
    *   @param max_results 
    *       if negative, return all results, else return no more 
    *       than that number of results.
    */
    public List<GlycanSequence> getLinkageIsomers( int max_results )
    {
        return getResults( getLinkageIsomersCriteria(), max_results );
    }
    
    
    /**
    *   Returns a {@link DetachedCriteria} instance that searches for 
    *   {@link GlycanSequence}s that have the same connection of 
    *   Residues and ignoring Linkages (with no ordering).
    *
    *   @see HibernateEntityManager#convertToCriteria
    */
    public DetachedCriteria getLinkageIsomersCriteria()
    {
        SubstructureQueryCriterion crit = this.asCriterion();
        crit.getSubstructureQuery()
            .setOption( Must_Include_All_Non_Reducing_Terminii )
            .setOption( Must_Include_Reducing_Terminus )
            .setOption( Ignore_Linkages )
        ; 
        
        return getDetachedCriteria()
            .setComment( "GlycanSequenceRelations.getLinkageIsomersCriteria" )
            .add( not( idEq( gs.getGlycanSequenceId() ) ) )
            .add( eq( "residueCount", gs.getResidueCount() ) )
            .add( crit )
        ;
    }
    
    
    /**
    *   Returns {@link GlycanSequence}s that contain the GlycanSequence
    *   given at construction as a substructure (hence "superstructure"), 
    *   ordered by {@link #getIndex} and limited to the given number of structures.
    *
    *   @param max_results 
    *       if negative, return all results, else return no more 
    *       than that number of results.
    */
    public List<GlycanSequence> getSuperstructures( int max_results )
    {
        return getResults( getSuperstructuresCriteria(), max_results );
    }
    
    
    /**
    *   Returns a {@link DetachedCriteria} that finds {@link GlycanSequence}s 
    *   that contain the GlycanSequence given at construction as a 
    *   substructure (hence "superstructure").
    *
    *   @see HibernateEntityManager#convertToCriteria
    */
    public DetachedCriteria getSuperstructuresCriteria()
    {
        return getDetachedCriteria()
                .setComment( "GlycanSequenceRelations.getSuperstructuresCriteria" )
                .add( not( idEq( gs.getGlycanSequenceId() ) ) )
                .add( this.asCriterion() );
    }
    
    
    public List<GlycanSequence> getStereochemicalEquivalents( int max_results )
    {
        return getResults( getStereochemicalEquivalentsCriteria(), max_results );
    }
    
    
    /**
    *   Returns a {@link DetachedCriteria} instance that searches for 
    *   {@link GlycanSequence}s that have the same stereochemistry,
    *   disregarding {@link Substituent}s attached to {@link Monosaccharide}s,
    *   so for instance, GlcNAc is stereochemically equivalent to Glc.
    *
    *   @see org.eurocarbdb.sugar.Basetypes#getBasetypeId
    *   @see HibernateEntityManager#convertToCriteria
    */
    public DetachedCriteria getStereochemicalEquivalentsCriteria()
    {
        SubstructureQueryCriterion crit = this.asCriterion();
        crit.getSubstructureQuery()
            .setOption( Must_Include_All_Non_Reducing_Terminii )
            .setOption( Must_Include_Reducing_Terminus )
            .setOption( Ignore_Monosac_Substituents )
        ; 
        
        return getDetachedCriteria()
            .setComment( "GlycanSequenceRelations.getStereochemicalEquivalentsCriteria" )
            .add( not( idEq( gs.getGlycanSequenceId() ) ) )
            .add( eq( "residueCount", gs.getResidueCount() ) )
            .add( crit )
        ;
    }
    
    
    /**
    *   Returns the {@link Index} that will be used to order results returned 
    *   by search methods returning {@link List}s (but not those that return 
    *   {@link Criteria}).
    *
    *   @see Index#apply(Criteria)
    *   @see HibernateEntityManager#convertToCriteria
    */
    public Index<GlycanSequence> getIndex()
    {
        return index;   
    }
    
    
    /**
    *   Sets the {@link Index} that will be used to order results returned 
    *   by search methods returning {@link List}s (but not those that return 
    *   {@link Criteria}).
    *
    *   @see Index#apply(Criteria)
    *   @see HibernateEntityManager#convertToCriteria
    */
    public GlycanSequenceRelations setIndex( Index<GlycanSequence> i )
    {
        this.index = i;
        return this;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Creates a {@link GlycanSequence} {@link DetachedCriteria}. */
    final DetachedCriteria getDetachedCriteria()
    {
        // return getEntityManager().createQuery( GlycanSequence.class );
        return DetachedCriteria.forClass( GlycanSequence.class );
    }
    
    
    /** 
    *   Executes passed {@link DetachedCriteria}, returning given max results, 
    *   or all results if negative.
    *
    *   @see HibernateEntityManager#convertToCriteria
    */
    final List<GlycanSequence> getResults( DetachedCriteria dc, int max_results )
    {
        Criteria c = HibernateEntityManager.convertToCriteria( dc );
        
        if ( max_results > 0 )
            c.setMaxResults( max_results );
        
        getIndex().apply( c );
        
        List<GlycanSequence> results = (List<GlycanSequence>) c.list();

        if ( results == null )
            return emptyList();
        
        return results;
    }
    
} // end class