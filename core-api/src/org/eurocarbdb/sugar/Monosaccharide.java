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
*   Last commit: $Rev: 1231 $ by $Author: glycoslave $ on $Date:: 2009-06-19 #$  
*/

package org.eurocarbdb.sugar;

//  stdlib imports
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

//  3rd party imports - commons logging
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.sugar.SequenceFormat;

import static java.util.Collections.unmodifiableList;


/**
*<p>
*   Describes the minimum interface for a monosaccharide residue.
*</p>
*<p>
*   Monosaccharides are based on a finite number of monosaccharides, 
*   which Eurocarbdb enumerates as standard "{@link Basetype}s". All
*   monosaccharides are considered as derivations of these standard basetypes.
*   The great majority of Monosaccharide will consist of a single Basetype,
*   infrequently, monosaccharides will also have {@link Modification}s and
*   {@link Substituent}s.
*</p>
*<p>
*   {@link Modification}s are chemical alterations specific to the monosaccharide
*   backbone (basetype). {@link Substituent}s are defined as non-monosaccharide
*   attachments to the basetype.
*</p>
*<p>
*   Accordingly, we anticipate multiple implementations of this interface
*   to handle these different classes of monosaccharide, with "common"/"simple" 
*   monosaccharides being typically handled by a lightweight implementation
*   and rarer/more complex monosaccharides handled by a more heavyweight 
*   implementation. 
*</p>
*
*   @see Basetype
*   @see SequenceFormat
*   @see MolecularLibrary
*
*   @author mjh
*/
public interface Monosaccharide extends Attachable<Substituent>, Residue
{
        
    /** 
    *   Returns the Eurocarb canonical name of this monosaccharide; see
    *   {@link SequenceFormat} for sequence format translation support.
    *
    *   @see SequenceFormat
    */
    public String getName() 
    ;    

    
    // public void addModification( Modification m ) 
    // ;
    

    /**
    *   Returns the <a href="http://en.wikipedia.org/wiki/Anomer">
    *   anomeric configuration</a> of this monosaccharide.
    */
    public Anomer getAnomer()
    ;
    
    
    /** 
    *   Sets the <a href="http://en.wikipedia.org/wiki/Anomer">
    *   anomeric configuration</a> of this monosaccharide.
    *
    *   @throws IllegalArgumentException if this monosaccharide 
    *   cannot adopt the given anomeric conformation; such as if 
    *   the current {@link RingConformation} is set to {@link OpenChain}.
    *   @throws NullPointerException if passed argument is null.
    */
    public void setAnomer( Anomer a ) throws IllegalArgumentException
    ;
    
        
    /** 
    *   Returns the basetype (core/backbone) monosaccharide on which
    *   this monosaccharide is based. 
    */
    public Basetype getBasetype() 
    ;
    
    
    /**
    *   Returns the current ring conformation (pyranose/furanose/open-chain) 
    *   this monosaccharide is in.
    */
    public RingConformation getRingConformation()
    ;
    
    
    /**
    *   Sets the current ring conformation (pyranose/furanose/open-chain) 
    *   this monosaccharide is in.
    *   @throws IllegalArgumentException if this monosaccharide 
    *   cannot adopt the given conformation.
    *   @throws NullPointerException if passed argument is null.
    */
    public void setRingConformation( RingConformation rc )
    throws IllegalArgumentException
    ;
    
    
    /**
    *   Returns the stereochemical (D/L) configuration of this monosaccharide.
    */
    public StereoConfig getStereoConfig()
    ;
    
    
    /** Returns the {@link Superclass} of this monosaccharide. */
    public Superclass getSuperclass()
    ;
    
    
} // end interface Monosaccharide



