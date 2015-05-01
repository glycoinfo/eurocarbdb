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
*   Last commit: $Rev: 1262 $ by $Author: glycoslave $ on $Date:: 2009-06-26 #$  
*/

package org.eurocarbdb.dataaccess.core.seq;

//  stdlib imports
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collections;

import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports

import org.eurocarbdb.sugar.Linkage;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.sugar.GlycosidicLinkage;

import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.Anomer;
import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.Basetype;
import org.eurocarbdb.sugar.Superclass;
import org.eurocarbdb.sugar.StereoConfig;
import org.eurocarbdb.sugar.RingConformation;
import org.eurocarbdb.sugar.PositionOccupiedException;
import org.eurocarbdb.sugar.PositionNotOccupiedException;

import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.exception.DataException;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.sugar.Basetypes.getStereochemicalId;
import static org.eurocarbdb.sugar.Basetypes.getBasetypeId;


/**
*   Each GlycanMonosaccharide represents a specific {@link Monosaccharide} 
*   of the {@link Sugar} of a specific {@link GlycanSequence}.
*
*   @author mjh
*/
public class GlycanMonosaccharide 
extends GlycanResidue implements Serializable, Monosaccharide
{

    /** The specific monosaccharide in the {@link GlycanSequence} 
    *   given by the getGlycanSequence method of our superclass. */
    private Monosaccharide monosac;

    /** Local copy of anomeric configuration of {@link #monosac} */    
    private Anomer anomer = null;

    /** Local copy of Superclass of {@link #monosac} */
    private Superclass superclass = null;
    
    /** Local copy of RingConformation of {@link #monosac} */
    private RingConformation ringConformation = null;
    
    /** ID representing the stereochemistry and chiral positions 
    *   of the underlying monosaccharide, independent of functional groups. */
    private int basetypeId;
    
    /** ID representing the stereochemistry of the underlying 
    *   monosaccharide, independent of chiral position and functional groups. */
    private int stereochemId;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /* needed for hibernate */
    GlycanMonosaccharide() 
    {
    }
    
    
    public GlycanMonosaccharide( Monosaccharide m )
    {
        super();
        setResidue( m );
    }
    

    // public GlycanMonosaccharide( Monosaccharide m, GlycanSequence gs )
    // {
    //     super();
    //     setResidue( m );
    //     setGlycanSequence( gs );
    // }

    
    //~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
 
    /** 
    *   Returns the {@link Monosaccharide} residue encapsulated 
    *   by this GlycanResidue.
    */
    public Monosaccharide getMonosaccharide()
    {
        if ( monosac != null )
            return monosac;    
        
        log.warn("need to lookup Monosac '" + getResidueName() + "' here");
        return null;
    }
    
    
    /** Returns {@link #getMonosaccharide}. */
    public Residue getResidue()
    {
        return getMonosaccharide();
    }
    
    
    /** 
    *   Sets the {@link Residue} represented by this {@link GlycanResidue}
    *   to the given argument, which must be a {@link Monosaccharide}.
    *
    *   @throws ClassCastException
    *       if the passed argument cannot be cast to {@link Monosaccharide}
    */
    public void setResidue( Residue r ) throws ClassCastException
    {
        /*
        Monosaccharide m = (Monosaccharide) r; 
        
        anomer       = m.getAnomer();
        basetype     = m.getBasetype();
        superclass   = m.getSuperclass();
        conformation = m.getRingConformation();
        */
        
        Monosaccharide m = (Monosaccharide) r; 
        
        this.monosac = m;
        this.anomer = m.getAnomer();
        this.superclass = m.getSuperclass();
        
        Basetype b = m.getBasetype();
        this.basetypeId = getBasetypeId( b );
        this.stereochemId = getStereochemicalId( b );
        
        setResidueName( r.getName() );
        
        super.setResidue( r );
    }
    
    
    /** 
    *   Requires: (1) a {@link Monosaccharide} be set, either at 
    *   construction or by calling {@link #setResidue} with a 
    *   Monosaccharide argument; and (2) a non-null {@link GlycanSequence}.
    */
    public void validate()
    {
        if ( getMonosaccharide() == null )
            throw new DataException("getMonosaccharide must be set");
        
        if ( getGlycanSequence() == null )
            throw new DataException("getGlycanSequence must be set");
    }

    
    /*  impl of Attachable interface  */
    
    public void attach( Substituent entity, int position )
    throws PositionOccupiedException
    {
        getMonosaccharide().attach( entity, position );
    }
    
    
    public void unattach( int position ) throws PositionNotOccupiedException
    {
        getMonosaccharide().unattach( position );   
    }
    
    
    public Set<Integer> getAttachablePositions()
    {
        return getMonosaccharide().getAttachablePositions();   
    }
    
    
    public Substituent getAttached( int position )
    {
        return getMonosaccharide().getAttached( position );
    }
       

    public int countPositions()
    {
        return getMonosaccharide().countPositions();
    }
 
    
    /* impl of Monosaccharide interface */
    
    public Anomer getAnomer()
    {
        return getMonosaccharide().getAnomer();
    }
    
    
    public void setAnomer( Anomer a ) throws IllegalArgumentException
    {
        getMonosaccharide().setAnomer( a );   
    }
    
    
    public Basetype getBasetype() 
    {
        return getMonosaccharide().getBasetype();   
    }

    
    public RingConformation getRingConformation()
    {
        return getMonosaccharide().getRingConformation();   
    }
    
    
    public void setRingConformation( RingConformation rc )
    throws IllegalArgumentException
    {
        getMonosaccharide().setRingConformation( rc );   
    }
    
    
    public StereoConfig getStereoConfig()
    {
        return getMonosaccharide().getStereoConfig();   
    }
    
    
    public Superclass getSuperclass()
    {
        return getMonosaccharide().getSuperclass();   
    }
    
} // end class GlycanMonosaccharide


