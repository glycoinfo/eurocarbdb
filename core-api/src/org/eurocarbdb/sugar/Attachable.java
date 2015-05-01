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
*   Last commit: $Rev: 1253 $ by $Author: glycoslave $ on $Date:: 2009-06-24 #$  
*/

package org.eurocarbdb.sugar;

import java.util.Set;

/**
*   A generic interface for entities that can attach/detach other entities
*   on a positional basis.
*
*   @author mjh
*/
public interface Attachable<T>
{
    
    /** 
    *   Attaches the given object at the given position. Unknown or uncertain 
    *   positions may be specified by giving negative or zero positions, though
    *   the precise semantics are implementation-specific.
    *
    *   @param entity
    *       The thing to attach.
    *   @param position
    *       The position at which to attach.
    *   @throws PositionOccupiedException
    *       If the given {@link Substituent} and position conflicts with
    *       an existing position or if the number of substituents exceeds 
    *       {@link #countPositions} when substituents with unknown attachment 
    *       positions are considered
    *   @throws IllegalArgumentException
    *       If position is greater than the number of positions available 
    *       for this monosaccharide (see {@link #countPositions}), or if 
    *       position is negative.
    */
    public void attach( T entity, int position )
    throws PositionOccupiedException, IllegalArgumentException
    ;
    
    
    /** 
    *   Unattaches (frees) the object attached at the given position, 
    *   causing the freed position to revert to its default state. 
    */
    public void unattach( int position )
    throws PositionNotOccupiedException
    ;
    
    
    /** 
    *   Returns an (unmodifiable) {@link Set} of positions that are 
    *   currently available to be attached. 
    *   @see org.eurocarbdb.util.BitSet
    */
    public Set<Integer> getAttachablePositions()
    ;
    
    
    /** Returns the number of total positions (ie: free + attached). */
    public int countPositions()
    ;
    
    
    /** 
    *   Returns the attached object at the given position, or null 
    *   if that position is free. The precise semantics of what positions
    *   are "free" is implementation-specific.
    */
    public T getAttached( int position )
    ;
    
}
