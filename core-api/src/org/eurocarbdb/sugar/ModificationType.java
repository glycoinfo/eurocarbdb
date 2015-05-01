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



/**
*   @author Rene
*/
public enum ModificationType 
{
    // DEOXY("d"),
    // ACID("a"),
    // KETO("keto"),
    // ALDI("aldi"),
    DOUBLEBOND("en"),
    UNKNOWN_DOUBLEBOND("enx"),
    SP2_HYBRID("sp2"),
    GEMINAL("geminal");
    
    private String symbol = "";
    
    /** Private constructor, see the forName methods for external use. */
    private ModificationType( String symbol )
    {
        this.symbol = symbol;
    }

    /** Returns the appropriate Anomer instance for the given character/symbol.  
    *   @throws IllegalArgumentException */
    public static ModificationType forName( String name ) throws IllegalArgumentException
    {
        for ( ModificationType a : ModificationType.values() )
            if ( name.equalsIgnoreCase( a.symbol) )
                return a;

        throw new IllegalArgumentException("Invalid value for modification");
    }

    public String getName()
    {
           return this.symbol;
    }
    
}
