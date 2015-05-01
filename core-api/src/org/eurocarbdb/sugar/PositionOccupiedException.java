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
*   Last commit: $Rev: 1254 $ by $Author: glycoslave $ on $Date:: 2009-06-25 #$  
*/

package org.eurocarbdb.sugar;

/** 
*   Exception class for indicating the state of an {@link Attachable} 
*   entity being unable to attach a new object due to a conflict with
*   an existing position.
*
*   @see Attachable
*   @author mjh
*/
public class PositionOccupiedException extends SugarException
{
    Attachable<?> target;
    
    int position;
    
    String message;
    
    /** Construct a non-standard exception based on an arbitrary string. */
    public PositionOccupiedException( String message )
    {
        this.message = message;
    }
    
    
    /** Construct an auto-formatted exception. */
    public PositionOccupiedException( Attachable<?> entity, int position )
    {
        this(
            "Attachable object '" 
            + entity 
            + "' cannot attach entities at position '"
            + position
            + "'; it is already occupied by entity '"
            + entity.getAttached( position )
            + "'"
        );
        this.target = entity;
        this.position = position;
    }
    
    
    public String getMessage()
    {
        return message;   
    }
}


