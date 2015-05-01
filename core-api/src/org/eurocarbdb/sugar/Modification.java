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
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/

package org.eurocarbdb.sugar;

/**
*<p>
*   This class represents modifications that are <em>part of</em> a 
*   {@link Monosaccharide}, as distinct from {@link Substituent}s that 
*   are <em>attached to</em> a Monosaccharide. Defined modification types
*   are enumerated in the {@link ModificationType} enum.
*</p>
*<p>
*   Modifications may either be <em>single-point</em>, that is, occurring
*   at a single point in the monosaccharide ring, or <em>dual-point</em>,
*   occurring between 2 points in the monosaccharide ring. Modifications 
*   are also immutable once constructed, although they may be removed
*   from a Monosaccharide once {@link Monosaccharide#addModification added}
*   by the {@link Monosaccharide#removeModification} method.
*</p>
*   @author rene
*   @author mjh added some code and contributed doco.
*/
public class Modification extends BasicMolecule
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Constant indicating that an attachment position is not known. */
    public static final int UNKNOWN_POSITION = 0;
    
    /** Constant indicating that an attachment position is not useable. */
    private static final int INVALID_POSITION = -1;

    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** The first (lowest-numbered) position to which this 
    *   modification is attached.  */
    private final int position1; // = INVALID_POSITION;

    /** The second (lowest-numbered) position to which this 
    *   modification is attached, if applicable. This value is 
    *   set to {@link #INVALID_POSITION} for single point attachments */
    private final int position2; // = INVALID_POSITION;

    /** The modification type of this modification. */
    private final ModificationType modification;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** 
    *   Constructs a single-point modification. 
    *   @throws IllegalArgumentException if the given modification name
    *   is not a valid/known {@link ModificationType modification type}.
    */
    public Modification( String modificationName, int position ) 
    {
        this( ModificationType.forName( modificationName ), position );
    }

    /** 
    *   Constructs a dual-point modification. 
    *   @throws IllegalArgumentException if the given modification name
    *   is not a valid/known {@link ModificationType modification type}.
    */
    public Modification( String modificationName, int position1, int position2 ) 
    {
        this( ModificationType.forName( modificationName ), 
              position1, 
              position2 
        );
    }

    /** Constructs a single-point modification. */
    public Modification( ModificationType m, int position ) 
    {
        this.modification = m;
        //this.setPositionOne( position );

        _check_position( position );
        
        this.position1 = position;
        this.position2 = INVALID_POSITION;        
    }

    /** Constructs a dual-point modification. */
    public Modification( ModificationType m, int position1, int position2 ) 
    {
        this.modification = m;
        
        _check_positions( position1, position2 );
        this.position1 = position1;
        this.position2 = position2;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** 
    *   Gets the first (lowest-numbered) position in the monosaccharide ring 
    *   where this modification occurs. 
    */
    public int getPositionOne()
    {
        return this.position1;
    }

    
    /** 
    *   Gets the second (highest-numbered) position in the monosaccharide ring 
    *   where this modification occurs. If this is a single-point modification
    *   then calling this method throws an {@link UnsupportedOperationException}.
    *   @see #hasPositionTwo
    */
    public int getPositionTwo()
    {
        if ( position2 == INVALID_POSITION ) 
            throw new UnsupportedOperationException(
                "Modification does not have a second position set; " 
                + "it is a single-point modification" );
            
        return this.position2;
    }
    
    /**
    *   Returns the canonical name of this modification.
    */
    public String getName()
    {
        return this.modification.getName();
    }
    
    /**
    *   Returns true if this is a dual-point modification.
    */
    public boolean hasPositionTwo()
    {    
        return position2 != INVALID_POSITION;
    }  


    //~~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~//

    /*  mjh: no longer settable
    private void setPositionOne( int position )
    {
        if ( position < Modification.UNKNOWN_POSITION )
            throw new IllegalArgumentException(
                "Invalid value for attach position");

        this.position1 = position;
    }
    */

    /*  mjh: no longer settable
    private void setPositionTwo( int position ) 
    {
        if ( position < Modification.UNKNOWN_POSITION )
            throw new IllegalArgumentException(
                "Invalid value for attach position");

        this.position2 = position;
    }
    */
    
    /** Checks position is >= 0 */
    private final void _check_position( int position )
    {
        if ( position < Modification.UNKNOWN_POSITION )
            throw new IllegalArgumentException(
                "Invalid value for attach position");
    }

    /** Checks positions are >= 0 and p1 < p2 */
    private final void _check_positions( int p1, int p2 )
    {
        _check_position( p1 );
        _check_position( p2 );
        
        if ( p1 >= p2 )
            throw new IllegalArgumentException(
                "Position1 argument cannot be >= position2");
    }

} //end class



