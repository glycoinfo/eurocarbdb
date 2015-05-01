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
*   Last commit: $Rev: 1593 $ by $Author: hirenj $ on $Date:: 2009-08-14 #$  
*/

package org.eurocarbdb.dataaccess.core;

//  stdlib imports
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.dataaccess.EurocarbObject;
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import org.eurocarbdb.dataaccess.Contributed;

//  static imports
import static java.util.Collections.emptySet;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.unmodifiableList;

import static org.eurocarbdb.util.JavaUtils.*;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*   Evidence is the base class for classes that represent a piece, 
*   or collection of, experimental data, such as a mass spectrum 
*   or HPLC trace.  
*
*   @author mjh
*/
public class Evidence extends BasicEurocarbObject 
implements Serializable, Contributed 
{
    /** Enumeration of Evidence types */
    public enum Type 
    {
        /** Generic evidence type */
        GENERIC,
        
        /** Mass-spectrometry evidence type */
        MS,

        /** HPLC (high-performance liquid chromatography) evidence type */
        HPLC,
        
        /** NMR (nuclear magnetic resonance) evidence type */
        NMR
        ;
        
    }
    

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//

    private static final Logger log = Logger.getLogger( Evidence.class );
    
    private static final String Q = "org.eurocarbdb.dataaccess.core.Evidence.";

    private static final String QUERY_ALL = "org.eurocarbdb.dataaccess.core.Evidence.ALL";

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Unique id for this piece of evidence */
    private int evidenceId;
     
    /** Type of evidence, defaults to {@link Type.GENERIC} */
    private Type evidenceType = Type.GENERIC;
    
    /** The contributor of this evidence; defaults to the current Contributor. */
    private Contributor contributor = null;
     
    /** The date this evidence was entered. */
    private Date dateEntered = new Date();
     
    /** The {@link ExperimentStep} to which this evidence is associated, 
    *   can be null if not associated to an {@link Experiment}. */
    private ExperimentStep experimentStep = null;

    /** The {@link Experiment} to which this evidence is associated, 
    *   can be null if not associated to an {@link Experiment}. */
    private Experiment experiment = null;
     
    /** The {@link Technique} used to create this evidence. */
    private Technique technique;
     
    /** Set of {@link GlycanSequence} associations, internal use only */
    private Set<GlycanSequenceEvidence> glycanSequenceEvidence 
        = new HashSet<GlycanSequenceEvidence>(0);

    /** Set of {@link Reference} associations, internal use only */
    private Set<ReferencedEvidence> referencedEvidence 
        = new HashSet<ReferencedEvidence>(0);

    /** 
    *   Set of {@link BiologicalContext} associations 
    *   (ie: the researcher's sample) from which this {@link Evidence} 
    *   was obtained. Not publically exposed. 
    *   @see #addBiologicalContext 
    *   @see #getBiologicalContexts 
    */
    private Set<EvidenceContext> evidenceContexts = new HashSet<EvidenceContext>(0);

    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** default constructor */
    public Evidence() 
    {
    }

    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~//
    
    /*  getAll  *//***********************************
    *
    *   Returns all (!!!) glycan_sequence objects in the data store.
    */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static List<Evidence> getAllEvidence()
    {
        return (List<Evidence>)(getEntityManager().getQuery(QUERY_ALL).list());
    }

    
    /**
    *   Returns a {@link Map} of {@link Evidence.Type} to the number
    *   of pieces of evidence of that type in the current data store.
    */
    public static Map<Type,Integer> getCountEvidenceByType()
    {
        List<Object[]> map_rows = (List<Object[]>) getEntityManager()
            .getQuery( Q + "GET_EVIDENCE_COUNT_BY_EVIDENCE_TYPE" )
            .list();
            
        Map<Type,Integer> map = new HashMap<Type,Integer>( map_rows.size() );
        for ( Object[] row : map_rows )
        {
            map.put( (Type) row[0], ((Long) row[1]).intValue() );   
        }
        
        return map;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /**
    *   Adds an association between the given {@link BiologicalContext} 
    *   and {@link Evidence}.
    *   @see #getBiologicalContexts()
    */
    public void addBiologicalContext( BiologicalContext bc )
    {
        checkNotNull( bc );
        
        log.info("adding new BiologicalContext-Evidence association, from "
                + bc
                + " to "
                + this );
        
        EvidenceContext ec = new EvidenceContext( this, bc );
        this.getEvidenceContexts().add( ec );
        bc.getEvidenceContexts().add( ec );
    }
   
    
    /**
    *   Adds a {@link Reference} to this piece of {@link Evidence},
    *   returning a {@link ReferencedEvidence}, which encapsulates
    *   this association.
    *
    *   @see #getReferences()
    */
    public ReferencedEvidence addReference( Reference r )
    {
        checkNotNull( r );
        
        log.info( "adding new Reference-Evidence association, from "
                + r 
                + " to " 
                + this );
        
        ReferencedEvidence ev2ref = new ReferencedEvidence( r, this );
        this.getReferencedEvidence().add( ev2ref );
        r.addReferencedEvidence( ev2ref );
        
        return ev2ref;
    }
    
    /**
    *   Removes a {@link Reference} to this piece of {@link Evidence},
    *   returning a {@link ReferencedEvidence}, that previously encapsulated
    *   this association.
    *
    *   @see #getReferences()
    *   @see #addReference()
    */   
    public ReferencedEvidence deleteReference( Reference reference )
    {
        assert reference != null;
        ReferencedEvidence ev2ref = new ReferencedEvidence(reference,this);
        ArrayList<ReferencedEvidence> ev2refs = new ArrayList<ReferencedEvidence>(this.getReferencedEvidence());
        ev2ref = ev2refs.get(ev2refs.indexOf( ev2ref ));
        this.getReferencedEvidence().remove( ev2ref );
        getEntityManager().remove(ev2ref);
        return ev2ref;        
    }

    /**
    *   Returns the {@link Set} of {@link BiologicalContext}s from
    *   which this {@link Evidence} was obtained.
    *   @see #addBiologicalContext(BiologicalContext)
    */
    public Set<BiologicalContext> getBiologicalContexts()
    {
        Set<EvidenceContext> ecs = this.getEvidenceContexts();
        if ( ecs == null || ecs.size() == 0 )
            return Collections.emptySet();
        
        Set<BiologicalContext> bcs = new HashSet<BiologicalContext>( ecs.size() );
        for ( EvidenceContext ec : ecs )
            bcs.add( ec.getBiologicalContext() );
        
        return bcs;
    }

    
    /** 
    *   Returns the canonical identifier of this piece of Evidence 
    *   This value should be unique across all evidence, regardless
    *   of {@link Technique} used. It is intended to be generated 
    *   internally. This value returned will be zero if this instance 
    *   is not saved to a data store.
    */
    public int getEvidenceId() 
    {
        return this.evidenceId;
    }
    
    
    /** 
    *   Returns the basic {@link Type} of this {@link Evidence}. 
    *   @see Evidence.Type
    */
    public Type getEvidenceType()
    {
        return this.evidenceType;   
    }
    
    
    /** 
    *   Sets the basic {@link Type} of this {@link Evidence}. 
    *   @see Evidence.Type
    */
    public void setEvidenceType( Evidence.Type t )
    {
        this.evidenceType = t;   
    }
    
    
    /** 
    *   Returns the {@link Experiment} to which this {@link Evidence}
    *   is associated, if any. Returns null if this evidence is not 
    *   associated to an experiment.
    */
    public Experiment getExperiment() 
    {
        return this.experiment;
    }
    
    
    /** 
    *   Sets the {@link Experiment} to which this {@link Evidence}
    *   is associated; null indicates this evidence is not 
    *   associated to an experiment.
    */
    public void setExperiment( Experiment e ) 
    {
        this.experiment = e;
    }

    
    /** 
    *   Returns the {@link ExperimentStep} to which this {@link Evidence}
    *   is associated, if any. Returns null if this evidence is not 
    *   associated to an experiment.
    */
    public ExperimentStep getExperimentStep() 
    {
        return this.experimentStep;
    }
    
    
    /** 
    *   Sets the {@link ExperimentStep} to which this {@link Evidence}
    *   is associated; null indicates this evidence is not 
    *   associated to an experiment.
    */
    public void setExperimentStep( ExperimentStep es ) 
    {
        this.experimentStep = es;
    }

    
    /*  getReferences  *//*******************************************
    *
    *   Returns an unmodifiable {@link List} of all {@link Reference}s 
    *   that are associated to this {@link Evidence}, returning an
    *   empty list if there are none. Note that this method returns 
    *   all References that reference this {@link GlycanSequence},
    *   including subclasses of {@link Reference}, such as journal 
    *   articles, represented as {@link JournalReference}s.
    *   @see #addReference
    *   @see Reference
    *   @see JournalReference
    */
    @SuppressWarnings("unchecked")
    public List<Reference> getReferences()
    {
        //  must perform a HQL lookup for associations that are polymorphic.
        int id = this.getEvidenceId();
        
        if ( log.isDebugEnabled() ) 
            log.debug("looking up all References for " + this );
        
        assert id > 0;
        
        List<Reference> reflist = (List<Reference>) getEntityManager()
                                .getQuery( Q + "GET_REFERENCES_FOR_EVIDENCE")
                                .setParameter("evidence_id", id )
                                .list();

        if ( reflist == null ) 
            return Collections.emptyList();
        
        return Collections.unmodifiableList( reflist );
    }
    
    /*  getTaxonomies  *//*******************************************
    *
    *   Returns an unmodifiable {@link Set} of {@link Taxonomy}s in 
    *   which this {@link Evidence} has been found.
    *
    *   @see #addBiologicalContext
    *   @see #getBiologicalContexts
    *   @see #getEvidenceContexts
    */
    public Set<Taxonomy> getTaxonomies() 
    {
        Set<EvidenceContext> ec_set = getEvidenceContexts();
    
        if ( ec_set == null || ec_set.size() == 0 )
            return emptySet();
        
        HashSet<Taxonomy> ret = new HashSet<Taxonomy>();
        for( EvidenceContext ec: ec_set ) 
            ret.add(ec.getBiologicalContext().getTaxonomy());    
        
        return unmodifiableSet( ret );
    }
    
      /*  getGlycanSequences  *//*********************************************
    *
    *   Returns an unmodifiable list of all {@link GlycanSequence} that is 
    *   recorded in the current data store for the current evidence.
    */
    @SuppressWarnings("unchecked")
    public List<GlycanSequence> getGlycanSequences()
    {
        int ev_id = this.getEvidenceId();
        assert ev_id <= 0;
        
        if ( log.isDebugEnabled() )
            log.debug("looking up all GlycanSequence for Evidence=" + ev_id );
        
        List<GlycanSequence> sequences = (List<GlycanSequence>) getEntityManager()
                                 .getQuery( Q + "GET_SEQUENCES_FOR_EVIDENCE" )
                                 .setParameter("evidence_id", ev_id )
                                 .list();

      
        if ( sequences == null )
            return Collections.emptyList();

        return unmodifiableList( sequences );
    }

    public List<GlycanSequenceContext> getGlycanSequenceContexts()
    {
        int ev_id = this.getEvidenceId();
        assert ev_id <= 0;
        
        if ( log.isDebugEnabled() )
            log.debug("looking up all GlycanSequenceContext for Evidence=" + ev_id );

        List<GlycanSequenceContext> sequences = (List<GlycanSequenceContext>) getEntityManager()
                                 .getQuery( Q + "GET_SEQUENCE_CONTEXTS_FOR_EVIDENCE" )
                                 .setParameter("evidence_id", ev_id )
                                 .list();

      
        if ( sequences == null )
            return Collections.emptyList();

        return unmodifiableList( sequences );
        
    }
    

    /** Returns the {@link Technique} used to create this evidence. */
    public Technique getTechnique() 
    {
        return this.technique;
    }
    
    
    /** Sets the {@link Technique} used to create this evidence. */
    public void setTechnique( Technique t ) 
    {
        checkNotNull( t );
        this.technique = t;
    }
    

    /* implementation of Contributed interface */
    
    /** Returns the original contributor of this evidence. */
    public Contributor getContributor() 
    {
        if ( this.contributor == null )
            // this.contributor = Contributor.getCurrentContributor();
            setContributor( Contributor.getCurrentContributor() );
        
        return this.contributor;
    }
    
    
    /** Sets the contributor of this evidence. */
    public void setContributor( Contributor c ) 
    {
        checkNotNull( c );
        this.contributor = c;
    }
    

    /** 
    *   Returns the {@link Date} this evidence was created. 
    *   Defaults to the date/time this object was instantiated. 
    */
    public Date getDateEntered() 
    {
        return this.dateEntered;
    }
    
    
    /** Sets the {@link Date} this evidence was created. */
    public void setDateEntered( Date dateEntered ) 
    {
        this.dateEntered = dateEntered;
    }


    //~~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~//
    
    /** internal use only */
    protected void setEvidenceId( int evidenceId ) 
    {
        this.evidenceId = evidenceId;
    }

    /** 
    *   Returns the {@link Set} of {@link BiologicalContext}s 
    *   (ie: the researcher's sample) from which this piece of 
    *   {@link Evidence} was obtained.
    *
    *   Internal use only! 
    *   @see #addBiologicalContext 
    *   @see #getBiologicalContexts 
    */
    Set<EvidenceContext> getEvidenceContexts() 
    {
        return this.evidenceContexts;
    }
    
    
    /** 
    *   Sets the {@link Set} of {@link BiologicalContext}s 
    *   (ie: the researcher's sample) from which this piece of 
    *   {@link Evidence} was obtained.
    *
    *   Internal use only! 
    *   @see #addBiologicalContext 
    *   @see #getBiologicalContexts 
    */
    void setEvidenceContexts( Set<EvidenceContext> evidenceContexts ) 
    {
        this.evidenceContexts = evidenceContexts;
    }
    
    
    /** Internal use only! @see #addReference */
    Set<ReferencedEvidence> getReferencedEvidence() 
    {
        return this.referencedEvidence;
    }
    
    
    /** Internal use only! @see #addReference */
    void setReferencedEvidence( Set<ReferencedEvidence> ev2ref ) 
    {
        this.referencedEvidence = ev2ref;
    }
    
    
    /** Internal use only! @see GlycanSequence#addEvidence */
    Set<GlycanSequenceEvidence> getGlycanSequenceEvidence() 
    {
        return this.glycanSequenceEvidence;
    }
    
    
    /** Internal use only! @see GlycanSequence#addEvidence */
    void setGlycanSequenceEvidence( Set<GlycanSequenceEvidence> gs2ev ) 
    {
        this.glycanSequenceEvidence = gs2ev;
    }
    
    
    @Override
    public void validate()
    {
        if ( this.contributor == null )
            setContributor( Contributor.getCurrentContributor() );
        
        super.validate();
    }
    
    
    @Override
    public int getId()
    {
        return getEvidenceId();    
    }
    
    
    @Override
    public Class<? extends EurocarbObject> getIdentifierClass()
    {
        return Evidence.class;    
    }

} // end class
