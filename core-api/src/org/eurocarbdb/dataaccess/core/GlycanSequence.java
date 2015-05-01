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
*   Last commit: $Rev: 1940 $ by $Author: khaleefah $ on $Date:: 2010-08-10 #$  
*/
package org.eurocarbdb.dataaccess.core;

//  stdlib imports
import java.math.BigDecimal;
import java.util.*;
import java.io.Serializable;
import java.security.*;

//  3rd party imports
import org.apache.log4j.Logger;
import org.hibernate.Query;

//  eurocarb imports
import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Linkage;
import org.eurocarbdb.sugar.Basetype;
import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.GlycosidicLinkage;
import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.sugar.SequenceFormat;
import org.eurocarbdb.sugar.PotentiallyIndefinite;

import org.eurocarbdb.dataaccess.Contributed;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.EurocarbObject;
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.core.seq.GlycanResidue;
import org.eurocarbdb.dataaccess.exception.EurocarbException;
import org.eurocarbdb.dataaccess.exception.InvalidAssociationException;

import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycoworkbench.GlycanWorkspace;

import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.util.graph.Vertex;
import org.eurocarbdb.util.graph.Edge;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.*;
import org.eurocarbdb.MolecularFramework.io.namespace.*;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

//  static imports
import static java.util.Collections.emptySet;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.unmodifiableList;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.util.JavaUtils.checkNotNull;
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.sugar.SequenceFormat.Iupac;
import static org.eurocarbdb.sugar.SequenceFormat.Glycoct;


/**
*   GlycanSequence is a data-access object implementing Eurocarb
*   glycan sequence entries.
*
*   @author mjh
*   @version $Rev: 1940 $
*/
public class GlycanSequence extends BasicEurocarbObject  
    implements Serializable, Contributed, PotentiallyIndefinite
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    static 
    {
        GlycanWorkspace theWorkspace = new GlycanWorkspace(null,false);        
    }

    private static final Logger log = Logger.getLogger( GlycanSequence.class );

    /** Standard string prefix for GlycanSequence queries */
    private static final String Q = "org.eurocarbdb.dataaccess.core.GlycanSequence.";
    
        
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Unique numeric identifier for this glycan sequence. GlycanSequences
    *   with the same glycan sequence have the same glycanSequenceId. */
    private int glycanSequenceId;
    
    /** The original (first) contributor of this glycan sequence. */
    private Contributor contributor;
    
    /** Numeric count of the number of Residues in this glycan sequence. */
    private int residueCount;
    
    /** Monoisotopic mass */
    private BigDecimal massMonoisotopic;
    
    /** Average mass */
    private BigDecimal massAverage;
    
    /** The date/time on which this sequence was entered into Eurocarb. */
    private Date dateEntered;
    
    /** Unsure - needs clarification? */
    private Date dateContributed;
    
    /** The set of {@link BiologicalContext}s associated with 
    *   this sequence, represented as a set of {@link GlycanSequenceContext}s. */
    private Set<GlycanSequenceContext> glycanContexts = new HashSet<GlycanSequenceContext>(0);
    
    /** The set of {@link Reference}s associated with this sequence,
    *   represented as a set of {@link GlycanSequenceReference}s. */
    private Set<GlycanSequenceReference> glycanReferences = new HashSet<GlycanSequenceReference>(0);
    
    /** The set of {@link Evidence} associated with this sequence,
    *   represented as a set of {@link GlycanSequenceEvidence}. */
    private Set<GlycanSequenceEvidence> glycanEvidence = new HashSet<GlycanSequenceEvidence>(0);
    
    /** Set of individual residues in the sugar sequence encapsulated 
    *   by this GlycanSequence. Each GlycanResidue object captures info about
    *   parent/child residues. */
    private Set<GlycanResidue> glycanResidues = new HashSet<GlycanResidue>();
    
    /** Contains the actual sequence of the glycan represented by this {@link GlycanSequence} */
    private SugarSequence sequence;

    /** sequence in iupac format.  @see SequenceFormat#Iupac */    
    private String sequenceIupac;
    
    /** sequence in glycoct format.  @see SequenceFormat#Glycoct */    
    private String sequenceCt;
    
    /** sequence in stalliano (GWS) format.  @see SequenceFormat#GWS */    
    private String sequenceGWS;

    /** True if the {@link SugarSequence} of this GlycanSequence 
    *   contains no unknown elements. */
    private Boolean isDefinite;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~//

    /**
    *   Creates a new, uninitialised {@link GlycanSequence}.
    *
    *   @see #setSugarSequence
    *   @see Contributor#getCurrentContributor()
    */
    //  note: null constructor needed for hibernate
    public GlycanSequence() 
    {
    }
    
    
    /** @deprecated use other contructors */
    @Deprecated
    public GlycanSequence( String raw_sequence ) 
    {
        //  TODO: parse sequence - but let's just assume
        //  it's a valid glycoCT sequence for now...
        
        setContributor( Contributor.getCurrentContributor() );
        
        initSequenceGWS(raw_sequence);
        sequenceCt = raw_sequence;
        sequenceIupac = raw_sequence; // <- temporary hack 

        //  init SugarSequence        
        getSugarSequence();
    }
    
    
    /**
    *   Creates a new {@link GlycanSequence} initialised to
    *   the given {@link SugarSequence} and the current {@link Contributor}.
    *
    *   @see #setSugarSequence
    *   @see Contributor#getCurrentContributor()
    */
    public GlycanSequence( SugarSequence ss )
    {
        setSugarSequence( ss );
        setContributor( Contributor.getCurrentContributor() );
    }
    
     
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~//

    /*  countSequences  *//******************************************
    *
    *   Returns the total number of unique {@link GlycanSequence}s
    *   in the current data store.
    *   @deprecated use getEntityManager().countAll( GlycanSequence.class )    
    */
    @Deprecated
    public static int countSequences()
    {
        return getEntityManager().countAll( GlycanSequence.class );
    }
    
    
    /*  lookupOrCreateNew  *//***************************************
    *
    *   Main method for adding sequence information to EurocarbDB, 
    *   taking a {@link SugarSequence} argument and returning either 
    *   a new or existing GlycanSequence object depending on the existence
    *   of the given SugarSequence in the current data store.
    *
    *   @see SugarSequence
    *   @see SequenceFormat
    */
    public static GlycanSequence lookupOrCreateNew( SugarSequence ss )
    {
        checkNotNull( ss );
        
        log.debug("checking if given SugarSequence already exists in data store...");
        GlycanSequence gs = lookupByExactSequence( ss );
        if ( gs == null )
        {
            log.debug( "SugarSequence does not exist in data store, "
                     + "returning new GlycanSequence");   
            return new GlycanSequence( ss );
        }
        else
        {
            if ( log.isDebugEnabled() )
                log.debug( "returning existing GlycanSequence (id=" 
                         + gs.getGlycanSequenceId() 
                         + ")" );   
            return gs;
        }
    }
    
    
    /**
    *   Looks up a {@link GlycanSequence} by an external reference name
    *   and id, for example ("carbbank", 12345) to find the GlycanSequence
    *   corresponding to Carbbank entry 12345. Returns null if no such 
    *   sequence exists.
    *   @see Reference
    */
    public static GlycanSequence lookupByExternalRef( String ref_name, int ref_id )
    {
        Object result = getEntityManager()
                        .getQuery( Q + "BY_EXTERNAL_REFERENCE" )
                        .setString( "ext_ref_name", ref_name )
                        .setString( "ext_ref_id", Integer.toString(ref_id) )
                        .uniqueResult(); 
                        
        return ( result != null ) ? (GlycanSequence) result : null;
    }
    
    
    /*  lookupByExactSequence  *//***********************************
    *
    *   Returns a non-null GlycanSequence if the passed {@link SugarSequence}
    *   exists in the current data store, or null if it does not. Note
    *   that the sequence format of the passed SugarSequence will be 
    *   normalised to the default {@link SequenceFormat} before being
    *   looked up. The general idiom for getting a SugarSequence from 
    *   a String is (assuming a Glycoct sequence):
    *<pre>
    *       String my_seq = ...
    *       SugarSequence sseq = new SugarSequence( my_seq, SequenceFormat.Glycoct );
    *</pre>
    *
    *   @throws IllegalArgumentException 
    *   if SugarSequence argument is null, or contains a null or 
    *   zero-length string sequence.
    *   @see SequenceFormat
    *   @author mjh
    */
    public static GlycanSequence lookupByExactSequence( SugarSequence ss )
    {
        checkNotNull( ss );
            
        String seq = ss.toString();         
    
        if ( seq == null || seq.length() == 0 )
            throw new IllegalArgumentException(
                "passed SugarSequence returned a null or zero-length sequence");
            
        log.debug("attempting to lookup GlycanSequence by an exact sequence");
        
        GlycanSequence g = (GlycanSequence) getEntityManager()
                          .getQuery( Q + "BY_EXACT_SEQUENCE" )
                          .setParameter("seq", seq )
                          .uniqueResult();
                          
        return g;
    }
    
    public static GlycanSequence lookupByExactSequenceString( String ss )
    {
        checkNotNull( ss );
            
        String seq = ss;         
    
        if ( seq == null || seq.length() == 0 )
            throw new IllegalArgumentException(
                "passed SugarSequence returned a null or zero-length sequence");
            
        log.debug("attempting to lookup GlycanSequence by an exact sequence");
        
        GlycanSequence g = (GlycanSequence) getEntityManager()
                          .getQuery( Q + "BY_EXACT_SEQUENCE" )
                          .setParameter("seq", seq )
                          .uniqueResult();
                          
        return g;
    }
    
       
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /**
    *   Associates this glycan sequence with a new {@link BiologicalContext},
    *   returning a new {@link GlycanSequenceContext} object that encapsulates
    *   this association.
    */
    public GlycanSequenceContext addBiologicalContext( BiologicalContext new_bc )
    {
        assert new_bc != null;
        log.info("adding association between " + this + " and " + new_bc );
        
        GlycanSequenceContext gsc = new GlycanSequenceContext();
        
        gsc.setBiologicalContext( new_bc );
        gsc.setGlycanSequence( this );
        
        // init collection, but add to glycanContext set directly
        //  because accessor for glycanContexts returns unmodifiable set.
        this.getGlycanSequenceContexts(); 
        glycanContexts.add( gsc );
        new_bc.getGlycanSequenceContexts().add( gsc );
        
        return gsc;
    }
    
    
    /**
    *   Adds a new {@link Evidence} association to this GlycanSequence entry.
    *   @return an object representing this association.
    */
    public GlycanSequenceEvidence addEvidence( Evidence new_evidence )
    {
        assert new_evidence != null;
        log.info("adding association between " + this + " and " + new_evidence );
        
        GlycanSequenceEvidence gs_ev = new GlycanSequenceEvidence();
        
        gs_ev.setEvidence( new_evidence );
        gs_ev.setGlycanSequence( this );
        
        this.getGlycanSequenceEvidence().add( gs_ev );
        new_evidence.getGlycanSequenceEvidence().add( gs_ev );
        
        return gs_ev;
    }
    
    
    /**
    *   Removes an existing {@link Reference} from this GlycanSequence entry.
    *   @return an object representing the previous association
    */
    public GlycanSequenceReference deleteReference( Reference reference )
    {
        assert reference != null;
        GlycanSequenceReference gs2r = new GlycanSequenceReference();
        gs2r.setReference( reference );
        gs2r.setGlycanSequence( this );
        ArrayList<GlycanSequenceReference> gs2rs = new ArrayList<GlycanSequenceReference>(this.getGlycanSequenceReferences());
        gs2r = gs2rs.get(gs2rs.indexOf( gs2r ));
        this.glycanReferences.remove( gs2r );
        getEntityManager().remove(gs2r);

        return gs2r;        
    }
    
    
    /**
    *   Adds a new {@link Reference} to this GlycanSequence entry.
    *   @return an object representing this association, or null if there
    *   is already an association between these 2 objects.
    */
    public GlycanSequenceReference addReference( Reference new_reference )
    {
        assert new_reference != null;    
        
        GlycanSequenceReference gs2r = new GlycanSequenceReference();
        gs2r.setReference( new_reference );
        gs2r.setGlycanSequence( this );
        
        log.info( "adding association between " 
            + this 
            + " and [Reference=" 
            + new_reference.getReferenceId() 
            + "]");

        boolean both_added = this.getGlycanSequenceReferences().add( gs2r );
        both_added &= new_reference.addGlycanSequenceReference( gs2r );

        if ( both_added )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug("added new glycan sequence to reference association between "
                     + this 
                     + " and " 
                     + new_reference 
                );
            }
            
            return gs2r;
        }
        else
        {
            if ( log.isDebugEnabled() )
            {
                log.debug("did not add new glycan sequence to reference association between "
                    + this 
                    + " and "
                    + new_reference
                    + " because association already exists"
                );
            }
            
            return null;
        }
    }
    
    
    /** Returns unique glycan sequence id. */
    public int getGlycanSequenceId() 
    {
        return this.glycanSequenceId;
    }
    
    
    /** Sets unique glycan sequence id; Internal use only! */
    void setGlycanSequenceId( int glycanSequenceId ) 
    {
        this.glycanSequenceId = glycanSequenceId;
    }

    
    /** 
    *   Returns the {@link Contributor} who initially contributed 
    *   this glycan sequence.
    */
    public Contributor getContributor() 
    {
        if ( contributor == null )
            contributor = Contributor.getCurrentContributor();
        
        return this.contributor;
    }
    
    
    /** 
    *   Sets the first {@link Contributor} of this glycan sequence.
    *   Note that many other people may contribute evidence for
    *   this structure; this method is only intended to be used
    *   to set the first contributor of a glycan sequence.
    */
    public void setContributor( Contributor contributor ) 
    {
        this.contributor = contributor;
    }

    
    /**
    *   Returns a {@link Set} of all the {@link Contributor}s who have
    *   contributed something to this sequence, specifically, they
    *   were the original contributor (see {@link #getContributor}),
    *   or they added a {@link BiologicalContext}, {@link Evidence}, 
    *   or {@link Reference} link to this sequence.
    */
    public Set<Contributor> getContributors()
    {
        Set<Contributor> contribs = new HashSet<Contributor>();
        
        contribs.add( this.getContributor() );
        
        for ( BiologicalContext c : this.getBiologicalContexts() )
        	for( BiologicalContextContributor cc : (Set<BiologicalContextContributor>) c.getBiologicalContextContributors())
        		contribs.add(cc.getContributor());
        
        for ( Contributed c : this.getEvidence() )
            contribs.add( c.getContributor() );
        
        for ( Contributed c : this.getReferences() )
            contribs.add( c.getContributor() );
        
        return contribs;
    }
    
    
    /** 
    *   Returns {@link getSugarSequence()}.toString(), ie: this sequence
    *   in the default {@link SequenceFormat}. 
    */
    public String getName() {  return this.getSugarSequence().toString();  }
    
    
    /** 
    *   Convenience method that returns the sequence of this 
    *   glycan sequence in {@link IupacSequenceFormat 
    *   Iupac sequence format}. Returns null if an iupac sequence
    *   cannot be generated for this sequence entry.
    *
    *   @see SequenceFormat
    *   @see SugarSequence
    */
    public String getSequenceIupac() 
    {
        //  mjh: this is a temporary implementation, it's going to change
        //  once iupac seq generation is finalised.
        if ( sequenceIupac == null || sequenceIupac.startsWith("RES") )
        {
            try
            {
                String seq = getSugarSequence().toString( Iupac );
                if ( seq != null )
                {
                    this.sequenceIupac = seq;
                    return seq;
                }
            }
            catch ( Exception ex )
            {
                log.warn(
                    "Couldn't get a iupac sequence for " 
                    + this
                    , ex
                );
                return null;
            }
        }
        return this.sequenceIupac;
    }
    
    
    /** 
    *   Sets the {@link SequenceFormat#Iupac Iupac} sequence
    *   of this {@link GlycanSequence} entry. Note that this *only*
    *   sets the iupac sequence, it doesn't affect the sequence
    *   representations of other formats.
    */
    void setSequenceIupac( String iupac ) 
    {
        assert iupac != null;
        this.sequenceIupac = iupac;
    }

    
    /** 
    *   Shortcut method for retrieving this glycan sequence in
    *   {@link SequenceFormat#Glycoct Glycoct condensed format}.
    */
    public String getSequenceCt() 
    {
        return this.sequenceCt;
    }
    
    
    /** 
    *   Sets the {@link SequenceFormat#Glycoct Glycoct} sequence
    *   of this {@link GlycanSequence} entry. Note that this *only*
    *   sets the Glycoct sequence, it doesn't affect the sequence
    *   representations of other formats.
    */
    void setSequenceCt( String glycoct_condensed ) 
    {
        assert glycoct_condensed != null;
        this.sequenceCt = glycoct_condensed;
    }


    /**
    *   Returns an object that is the gateway to finding various sequence 
    *   associations and relations of this {@link GlycanSequence}.
    */
    public GlycanSequenceRelations getRelations()
    {
        return GlycanSequenceRelations.of( this ); 
    }
    
    
    /**
    *   Returns a {@link Sugar} object that represents the glycan 
    *   structure encapsulated by this {@link GlycanSequence}.
    */
    public Sugar getSugar()
    {
        return getSugarSequence().getSugar();
    }
    
    
    /**
    *   Returns the (sequence format agnostic) {@link SugarSequence} 
    *   of this GlycanSequence.
    *
    *   @see #getSequenceCt
    *   @see SequenceFormat#Glycoct
    */
    public SugarSequence getSugarSequence()
    {
        if ( sequence == null )
        {
            String seq = this.getSequenceCt();
            if ( seq == null || seq.length() == 0 ) 
                return null;
            
            sequence = new SugarSequence( seq );    
        }
        
        return sequence;
    }
    
    
    /** 
    *   Preferred method for setting the glycan sequence.
    *   Internally handles any/all interconversion between sequence
    *   formats.
    *
    *   @throws NullPointerException
    *       if passed {@link SugarSequence} is null.
    */
    public void setSugarSequence( SugarSequence sseq )
    throws NullPointerException
    {
        String seq_glycoct = sseq.toString( Glycoct );
        initSequenceGWS( seq_glycoct );
        this.sequenceCt = seq_glycoct;   
        
        try
        {
            this.sequenceIupac = sseq.toString( Iupac );   
        }
        catch ( RuntimeException ex )
        {
            log.warn("Caught exception converting sequence to Iupac", ex );
            
            // next line is a necessary hack until iupac column is made nullable 
            this.sequenceIupac = seq_glycoct; 
        }
        
        this.sequence = sseq;
    }
    
    
    @Deprecated
    private void initSequenceGWS( String sequence_ct_cond ) 
    {
        // ensure backward compatibility
        Glycan ret = Glycan.fromGlycoCTCondensed(sequence_ct_cond,true);
        sequenceGWS = ( ret == null ) ? "" :ret.toString();           
    }

    
    /** 
    *   Shortcut method for retrieving this glycan sequence in
    *   {@link SequenceFormat#GWS Stalliano/GWS format}.
    *   This is a eurocarb-internal format.
    */
    public String getSequenceGWS() 
    {
        return sequenceGWS; 
    }

    
    void setSequenceGWS( String gws ) 
    {
        assert gws != null;
        this.sequenceGWS = gws;
    }

    
    /** 
    *   Returns the number of {@link Residue}s in the {@link Sugar}
    *   represented by this GlycanSequence; residues are defined
    *   in terms of Eurocarbdb {@link Monosaccharide}s: 
    *   {@link Basetype}s + {@link Substituent}s.
    *
    *   @see #getGlycanResidues
    */
    public int getResidueCount() 
    {
        return this.residueCount;
    }
    
    
    /** todo: don't use. */
    public BigDecimal getMassMonoisotopic() 
    {
        return this.massMonoisotopic;
    }
    
    
    /** todo: don't use. */
    public BigDecimal getMassAverage() 
    {
        return this.massAverage;
    }
    

    /** 
    *   Returns a simple {@link Map} of residue name to residue count,
    *   using normalised residue names. This method is likely to change
    *   in the near future, so marking as deprecated.
    *
    *   @deprecated the signature of this method is very likely to 
    *       change in the near future.
    */
    @Deprecated
    public Map<String,Integer> getComposition() 
    {
        Map<String,Integer> map = new HashMap<String,Integer>();
        try
        {
            Sugar s = getSugarSequence().getSugar();
            
            for ( Residue r : s )
            {
                String name = r.getName();
                // log.debug("residue: " + r + ", name: " + name );
                
                Integer count = map.get( name );
                if ( count == null || count == 0 )
                {
                    map.put( name, 1 );
                }
                else
                {
                    map.put( name, count + 1 );
                }
            }
            
            // log.debug("returning composition: " + map );
        }
        catch ( Exception ex )
        {
            log.warn("Caught exception while trying to derive composition", ex ); 
            return Collections.emptyMap();
        }
        
        return map;
    }
    
    
    /** {@inheritDoc} @see Contributed#getDateEntered */
    public Date getDateEntered() 
    {
        return this.dateEntered;
    }
    
    
    /** {@inheritDoc} @see Contributed#getDateEntered */
    public Date getDateContributed() 
    {
        return this.dateContributed;
    }
    
    
    /*  getEvidence  *//*********************************************
    *
    *   Returns an unmodifiable list of all {@link Evidence} that is 
    *   recorded in the current data store for the current glycan sequence.
    *   @see #addEvidence
    */
    @SuppressWarnings("unchecked")
    public List<Evidence> getEvidence()
    {
        int seq_id = this.getGlycanSequenceId();
        if ( seq_id <= 0 )
        {
            return Collections.emptyList();   
        }
        
        if ( log.isDebugEnabled() )
            log.debug("looking up all Evidence for GlycanSequence=" + seq_id );
        
        List<Evidence> evidence = (List<Evidence>) getEntityManager()
                                 .getQuery( Q + "GET_EVIDENCE_FOR_SEQUENCE" )
                                 .setParameter("sequence_id", seq_id )
                                 .list();

        //return (List<Evidence>) evidence;
        
        /* mjh: note: the below implementation will not work for subclasses of Evidence *
        List<Evidence> evs = new ArrayList<Evidence>();
        for ( GlycanSequenceEvidence gste : getGlycanSequenceEvidence() ) 
        {
            evs.add(gste.getEvidence());
        }
        */
        
        if ( evidence == null )
            return Collections.emptyList();

        return unmodifiableList( evidence );
    }
    
 
    /** 
    *   Returns the number of separate pieces of {@link Evidence} 
    *   for this {@link GlycanSequence}. 
    */
    public int getEvidenceCount()
    {
        return this.getGlycanSequenceEvidence().size();
    }
    
    
    /*  getBiologicalContexts  *//***********************************
    *
    *   Returns an unmodifiable list of all biological contexts in 
    *   which the current glycan sequence has been observed.
    *   @see #addBiologicalContext
    */
    @SuppressWarnings("unchecked")
    public List<BiologicalContext> getBiologicalContexts()
    {
        /*
        int seq_id = this.getGlycanSequenceId();
        assert seq_id > 0;

        if (this.getGlycanSequenceContexts() == null) {
            return new ArrayList<BiologicalContext>();
        }
        
        List bc_list = getEntityManager()
                        .getQuery( QUERY_ALL_BCS_FOR_SEQUENCE )
                        .setParameter("sequence_id", seq_id )
                        .list();

        return (List<BiologicalContext>) bc_list;
        */
        Set<GlycanSequenceContext> gsbc_list = this.getGlycanSequenceContexts();   
        if ( gsbc_list == null || gsbc_list.size() == 0 )
            return emptyList();
        
        List<BiologicalContext> bc_list = new ArrayList<BiologicalContext>( gsbc_list.size() );
        for ( GlycanSequenceContext c : gsbc_list ) 
            bc_list.add( c.getBiologicalContext());
        
        return unmodifiableList( bc_list );
    }
    

    /*  getUniqueBiologicalContexts  *//***********************************
    *
    *   Returns an unmodifiable list of all unique {@link BiologicalContext}s in 
    *   which the current glycan sequence has been observed. This differs from
    *   method {@link #getBiologicalContexts}, which may return contexts that 
    *   are equal by value, but which have different BC ids.
    *   @see #addBiologicalContext
    */
    @SuppressWarnings("unchecked")
    public List<BiologicalContext> getUniqueBiologicalContexts()
    {
        /* TODO:  replace this impl with a named query  */
        
        Set<GlycanSequenceContext> gsbc_list = this.getGlycanSequenceContexts();   
        if ( gsbc_list == null || gsbc_list.size() == 0 )
            return emptyList();
        
        List<BiologicalContext> bc_list = new ArrayList<BiologicalContext>( gsbc_list.size() );
        for ( GlycanSequenceContext c : gsbc_list ) 
        {
            boolean found = false;
            for ( BiologicalContext bc : bc_list ) 
            {
                if ( BiologicalContext.haveSameContent(bc,c.getBiologicalContext()) ) 
                {
                    found = true;
                    break;
                }
            }
            
            if ( ! found )
                bc_list.add(c.getBiologicalContext());
        }
        
        return unmodifiableList( bc_list );
    }

    

    /** 
    *   Returns an unmodifiable {@link Set} of all associations of
    *   {@link BiologicalContext} and this {@link GlycanSequence}. 
    *   @see #addBiologicalContext
    */
    public Set<GlycanSequenceContext> getGlycanSequenceContexts() 
    {
        return unmodifiableSet( this.glycanContexts );
    }
    
    
    /*  getTaxonomies  *//*******************************************
    *
    *   Returns an unmodifiable {@link List} of {@link Taxonomy}s in 
    *   which this {@link GlycanSequence} has been found. Note that 
    *   this List will contain multiples of the same Taxonomy if this 
    *   {@link GlycanSequence} has been associated to the same Taxonomy 
    *   more than once.
    *
    *   @see #getUniqueTaxonomies
    *   @see #addBiologicalContext
    *   @see #getBiologicalContexts
    *   @see #getGlycanSequenceContexts
    */
    public List<Taxonomy> getTaxonomies() 
    {
        return (List<Taxonomy>) 
            getEntityManager()
                .getQuery(Q + "GET_TAXONOMIES")
                .setParameter("id", this.glycanSequenceId)
                .list();
    }

        
    /** 
    *   Return the {@link Set} of the unique {@link Taxonomy}s that 
    *   have been associated with this GlycanSequence
    *
    *   @see #getTaxonomies
    */
    public Set<Taxonomy> getUniqueTaxonomies()
    {
        return new HashSet<Taxonomy>(getTaxonomies());
    }


    /*  getDiseases  *//*******************************************
    *
    *   Returns an unmodifiable {@link List} of {@link Disease}s in 
    *   which this {@link GlycanSequence} has been found. Note that 
    *   this List will contain multiples of the same Disease if this 
    *   {@link GlycanSequence} has been associated to the same Disease 
    *   more than once.
    *
    *   @see #getUniqueDiseases
    *   @see #addBiologicalContext
    *   @see #getBiologicalContexts
    *   @see #getGlycanSequenceContexts
    */    
    public  List<Disease> getDiseases()
    {
        return (List<Disease>) 
            getEntityManager()
                .getQuery(Q + "GET_DISEASES")
                .setParameter("id", this.glycanSequenceId)
                .list();        
    }


    /** 
    *   Returns the {@link Set} of unique {@link Disease}s associated 
    *   with this GlycanSequence.
    *
    *   @see #getDiseases
    *   @see #getBiologicalContexts
    */
    public Set<Disease> getUniqueDiseases()
    {
        return new HashSet<Disease>( getDiseases() );
    }


    /*  getTissues  *//*******************************************
    *
    *   Returns an unmodifiable {@link List} of {@link TissueTaxonomy}s in 
    *   which this {@link GlycanSequence} has been found. Note that 
    *   this List will contain multiples of the same tissue if this 
    *   {@link GlycanSequence} has been associated to the same tissue 
    *   more than once.
    *
    *   @see #getUniqueTissues
    *   @see #addBiologicalContext
    *   @see #getBiologicalContexts
    *   @see #getGlycanSequenceContexts
    */    
    public  List<TissueTaxonomy> getTissues()
    {
        return (List<TissueTaxonomy>) 
            getEntityManager()
                .getQuery(Q + "GET_TISSUES")
                .setParameter("id", this.glycanSequenceId)
                .list();        
    }


    /** 
    *   Return the {@link Set} of unique {@link Tissue}s associated 
    *   with this GlycanSequence
    *
    *   @see #getTissues
    *   @see #getBiologicalContexts
    */
    public Set<TissueTaxonomy> getUniqueTissues()
    {
        return new HashSet<TissueTaxonomy>( getTissues() );
    }

    
    /*  getReferences  *//*******************************************
    *
    *   Returns an unmodifiable {@link List} of all {@link Reference}s 
    *   that are linked to this {@link GlycanSequence}, returning an
    *   empty list if there are none. Note that this method returns 
    *   all References that reference this {@link GlycanSequence},
    *   including subclasses of {@link Reference}, such as journal 
    *   articles, represented as {@link JournalReference}s.
    *
    *   @see #addReference
    *   @see Reference
    *   @see JournalReference
    */
    @SuppressWarnings("unchecked")
    public List<Reference> getReferences()
    {
        //  must perform a HQL lookup for associations that are polymorphic.
        int id = this.getGlycanSequenceId();
        if ( log.isDebugEnabled() ) 
            log.debug("looking up all References for GlycanSequence=" + id );
        
        assert id > 0;
        
        List<Reference> reflist = (List<Reference>) getEntityManager()
                                .getQuery( Q + "GET_REFERENCES_FOR_SEQUENCE")
                                .setParameter("sequence_id", id )
                                .list();

        if ( reflist == null ) 
            return emptyList();
        
        return unmodifiableList( reflist );
    }
    
    
    /**
    *   Returns all {@link Reference}s for this {@link GlycanSequence}
    *   that have been contributed by the given {@link Contributor}.
    *   Otherwise similar to {@link #getReferences()}.
    *
    *   @see Contributed
    */
    public List<Reference> getReferences( Contributor c )
    {
        //  must perform a HQL lookup for associations that are polymorphic.
        int id = this.getGlycanSequenceId();
        if ( log.isDebugEnabled() ) 
            log.debug("looking up all References for GlycanSequence=" + id );
        
        assert id > 0;
        
        int contributorId = c.getContributorId();
        
        List<Reference> reflist = (List<Reference>) getEntityManager()
                                .getQuery( Q + "GET_REFERENCES_FOR_SEQUENCE_AND_CONTRIBUTOR")
                                .setParameter("sequence_id", id )
                                .setParameter("contributor_id", contributorId)
                                .list();

        if ( reflist == null ) 
            return emptyList();
        
        return unmodifiableList( reflist );        
    }
    
    
    /*  hasEvidence  *//*********************************************
    *
    *   Returns true if this {@link GlycanSequence} has at least 
    *   1 piece of associated {@link Evidence}.
    */    
    public boolean hasEvidence() 
    {
        Set<GlycanSequenceEvidence> gse_set = this.getGlycanSequenceEvidence();
        return ( gse_set!=null && gse_set.size() > 0 );
    }
    
    
    /*  hasMSEvidence  *//*******************************************
    *
    *   Returns true if this {@link GlycanSequence} has at least 
    *   1 piece of associated Mass Spec {@link Evidence}.
    *   @see org.eurocarbdb.dataaccess.ms.Acquisition
    */    
    public boolean hasMSEvidence() 
    {
        Set<GlycanSequenceEvidence> gse_set = this.getGlycanSequenceEvidence();
        if( gse_set==null )
            return false;
        for( GlycanSequenceEvidence gse : gse_set ) 
        {
            if( gse.getEvidence().getTechnique().isMS() )
            return true;
        }
        return false;
    }


    /*  hasHPLCEvidence  *//*****************************************
    *
    *   Returns true if this {@link GlycanSequence} has at least 
    *   1 piece of associated HPLC {@link Evidence}.
    *
    *   @see org.eurocarbdb.dataaccess.hplc.DigestProfile
    */    
    public boolean hasHPLCEvidence() 
    {
        Set<GlycanSequenceEvidence> gse_set = this.getGlycanSequenceEvidence();
        if( gse_set==null )
            return false;
        for( GlycanSequenceEvidence gse : gse_set ) 
        {
            if( gse.getEvidence().getTechnique().isHPLC() )
            return true;
        }
        return false;
    }

    
    /*  hasNMREvidence  *//******************************************
    *
    *   Returns true if this {@link GlycanSequence} has at least 
    *   1 piece of associated NMR {@link Evidence}.
    *
    *   @see org.eurocarbdb.dataaccess.nmr.NmrEvidence
    */    
    public boolean hasNMREvidence() 
    {
        Set<GlycanSequenceEvidence> gse_set = this.getGlycanSequenceEvidence();
        if( gse_set==null )
            return false;
        for( GlycanSequenceEvidence gse : gse_set ) 
        {
            if( gse.getEvidence().getTechnique().isNMR() )
            return true;
        }
        return false;
    }

    
    /** 
    *   Returns true if the {@link Sugar} returned by {@link #getSugar} 
    *   contains one or more unknown (indefinite) elements.
    *
    *   @see Sugar#isDefinite
    */
    public boolean isDefinite()
    {
        if ( isDefinite == null )
        {
            try 
            {  
                isDefinite = getSugar().isDefinite();  
            }
            catch ( Exception ex )
            {
                log.warn("Caught exception while determining if definite", ex );
                return false; // assume false...
            }
        }
        return isDefinite;
    }
    
    
    /**
    *<p>
    *   Translates the current {@link Sugar} (ie: {@link Graph} of 
    *   {@link Linkage}s and {@link Residue}s) contained within 
    *   this {@link GlycanSequence}, into a {@link Set} of 
    *   {@link GlycanResidue}s, which represent the tree structure
    *   of that Sugar. This is to enable sub-structure searches in the DB.
    *</p>
    *<p>
    *   Note: current code/DB implementation DOES NOT HANDLE Sugars that
    *   are not trees (ie: if they contain cycles). We will handle full graph
    *   searches in a different way, since sugar trees will be the general case.
    *</p>
    *<p>
    *   note: rough development version -- will become a protected/private method.
    *</p>
    */
    public void calculateSubstructureInfo()
    {
        // Set<GlycanResidue> set = this.glycanResidues;
        Set<GlycanResidue> glycan_residues = GlycanResidue.calculateResidueGraph( this );
         
        //this.setGlycanResidues( glycan_residues );
        
        if ( log.isDebugEnabled() )
        {
            try
            {
                log.debug(
                    "--------- glycan graph ----------\n"
                    + this.getSugarSequence().getSugar().getGraph() );
                 
                log.debug("--------- glycan residue list ----------");
                for ( GlycanResidue gr : glycan_residues )
                    log.debug( gr );
            }
            catch ( Exception ex ) 
            { 
                // skip these, Exception has already been logged...
                return;
            }
        }
        
        this.glycanResidues.clear();
        
        for ( GlycanResidue gr : glycan_residues )
            this.glycanResidues.add( gr );
        
        this.setResidueCount( glycan_residues.size() );
        
        return;        
    }
    
    
    /**
    *   Returns the (unmodifiable) {@link Set} of {@link Residue}s 
    *   that constitute the {@link SugarSequence} of this {@link GlycanSequence}.
    *
    *   @see #calculateSubstructureInfo()
    */
    public Set<GlycanResidue> getGlycanResidues()
    {
        return unmodifiableSet( glycanResidues );    
    }
    
    
    /** 
    *   Checks the validity of this {@link GlycanSequence} and all its 
    *   associations.
    */
    @Override
    public void validate() throws EurocarbException
    {
        /*  check our SugarSequence  */
        SugarSequence ss = this.getSugarSequence();
        if ( ss == null )
            throw new InvalidAssociationException(
                this, SugarSequence.class, "getSugarSequence() returned null" );
            
        //  check the Sugar of our SugarSequence            
        Sugar s = ss.getSugar();
        if ( s == null || s.countResidues() == 0 ) 
        {
            throw new InvalidAssociationException(
                this, Sugar.class, "Sugar object was null or contained 0 residues" );
        }        

        /*  check/calculate GlycanResidues  */
        //  sometime in the future this should also check if: 
        //  glycanResidues.size() != this.getResidueCount()
        if ( glycanResidues == null || glycanResidues.size() == 0 )    
        {
            this.calculateSubstructureInfo();
        }
        
        //  other checks go here...
        
        //  ...ok, object is valid.
        return;
    }
    
    
    public boolean equals( Object x )
    {
        if ( this == x )
            return true;        
        if ( (x == null) || (x.getClass() != this.getClass()) )
            return false;
        
        // objects are the same class
        GlycanSequence gs = (GlycanSequence) x;
        if (gs.sequence == null) {
            return (this.sequence == null);
        }
        
        return this.sequence.toString().equals(gs.sequence.toString());
    }
    

    //~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~//

    /** internal use only. @see #calculateSubstructureInfo() */
    void setGlycanResidues( Set<GlycanResidue> residues )
    {
        this.glycanResidues = residues;   
    }
    
    
    /** 
    *   mjh: this should be calculated from the underlying sugar, not set;
    *   internal use only!
    */
    void setResidueCount( int residueCount ) 
    {
        assert residueCount > 0;
        this.residueCount = residueCount;
    }

    
    /** mjh: this should be calculated from the underlying sugar, not set */
    void setMassMonoisotopic( BigDecimal massMonoisotopic ) 
    {
        this.massMonoisotopic = massMonoisotopic;
    }
    

    /** mjh: this should be calculated from the underlying sugar, not set */
    void setMassAverage( BigDecimal massAverage ) 
    {
        this.massAverage = massAverage;
    }
    

    /** mjh: this value is generated on insert by the DB; internal use only! */
    void setDateEntered( Date dateEntered ) 
    {
        this.dateEntered = dateEntered;
    }

    
    /** mjh: this value is generated on insert by the DB; internal use only! */
    void setDateContributed( Date dateContributed ) 
    {
        this.dateContributed = dateContributed;
    }

    
    /** Private use only, see {@link #getReferences}. */
    Set<GlycanSequenceReference> getGlycanSequenceReferences() 
    {
        return this.glycanReferences;
    }
    
    
    /** Private use only, see {@link #addReference}. */
    void setGlycanSequenceReferences( Set<GlycanSequenceReference> glycanReferences ) 
    {
        assert glycanReferences != null;
        this.glycanReferences = glycanReferences;
    }

    
    /** Private use only, see {@link #getEvidence}. */
    Set<GlycanSequenceEvidence> getGlycanSequenceEvidence() 
    {
        return this.glycanEvidence;
    }
    
    
    /** Private use only, see {@link #addEvidence}. */
    void setGlycanSequenceEvidence( Set<GlycanSequenceEvidence> glycanEvidence ) 
    {
        this.glycanEvidence = glycanEvidence;
    }

    public BiologicalContext getBiologicalContext(int id){
    	for(GlycanSequenceContext context:this.glycanContexts){
    		if(context.getBiologicalContext().getBiologicalContextId()==id){
    			return context.getBiologicalContext();
    		}
    	}
    	return null;
    }

} // end class
