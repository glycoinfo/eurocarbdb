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
*   Last commit: $Rev: 1561 $ by $Author: glycoslave $ on $Date:: 2009-07-21 #$  
*/

package org.eurocarbdb.sugar;

import org.apache.log4j.Logger;

import java.util.List;

import org.eurocarbdb.util.BitSet;
import org.eurocarbdb.sugar.Monosaccharide;

import static org.eurocarbdb.sugar.Superclass.*;
import static org.eurocarbdb.sugar.StereoConfig.*;
import static org.eurocarbdb.sugar.RingConformation.*;
import static org.eurocarbdb.sugar.CommonSubstituent.*;
import static org.eurocarbdb.util.StringUtils.join;


/**
*<p>
*   Basetypes are invariant definitions of fundamental aspects of 
*   the chemistry of {@link Monosaccharide}s, in particular, the unique 
*   stereochemistry of monosaccharides that defines their identity.
*   All Monosaccharides are considered to comprise one or more basetypes.
*</p>
*<p>
*   Two implementations of this interface are provided by Eurocarb:
*   <ul>
*   <li>a simple and fast {@link Enum}-based implementation ({@link CommonBasetype})
*   of invariant basetypes that are generally regarded (at least by Eurocarb) 
*   as "common"</li> 
*   <li>and a more heavyweight implementation, {@link CustomBasetype}, in which
*   every basetype element can be specified.</li>    
*   </ul>
*</p>
*
*   @see Monosaccharide
*   @author mjh
*/
public interface Basetype extends Molecule, PotentiallyIndefinite
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
    *   Returns the {@link StereoConfig} this basetype has been defined as. 
    */
    public StereoConfig getStereoConfig()
    ;

    
    /** 
    *   Returns a BitSet that returns true when a given integer position 
    *   in the basetype is chiral.
    */
    public BitSet getChiralPositions()
    ;    
    
    
    /** 
    *   Returns a List of Substituent functional groups, indexed by terminus 
    *   position minus one (ie: C1 == position 1 == index 0). 
    */
    public List<Substituent> getFunctionalGroups()
    ;
    
    
    /** 
    *<p>
    *   Returns a BitSet that encapsulates the Fischer stereochemistry 
    *   of the Basetype -- a value of true at a given position indicates
    *   that the hydroxyl at that position is oriented RIGHT in a Fischer
    *   projection. The stereochemistry returned is for the {@link StereoConfig}
    *   returned by {@link #getDefaultStereoConfig}. 
    *</p>
    *<p>
    *   The scientific definition of D/L refers to the orientation of the 
    *   furthest hydroxyl from the carbonyl -- sugars having the same orientation 
    *   as that of Glyceraldehyde at this position are designated D. 
    *</p>
    */
    public BitSet getStereochemistry()
    ;
    
    
    /** Returns the Superclass of the current Basetype. */
    public Superclass getSuperclass()
    ;
 
}


