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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
package org.eurocarbdb.MolecularFramework.sugar;

/**
* @author Logan
*
*/
public enum ModificationType 
{
    DEOXY("d"),
    ACID("a"),
    KETO("keto"),
    ALDI("aldi"),
    DOUBLEBOND("en"),
    UNKNOWN_DOUBLEBOND("enx"),
    SP2_HYBRID("sp2"),
    TRIPLEBOND("sp"),
    GEMINAL("geminal");
    
    private String m_strSymbol = "";
    
    /** Private constructor, see the forName methods for external use. */
    private ModificationType( String symbol )
    {
        this.m_strSymbol = symbol;
    }

    /** Returns the appropriate Anomer instance for the given character/symbol.  
     * @throws GlycoconjugateException */
    public static ModificationType forName( String a_strModi ) throws GlycoconjugateException
    {
        for ( ModificationType a : ModificationType.values() )
        {
            if ( a_strModi.equalsIgnoreCase( a.m_strSymbol) )
            {
                return a;
            }
        }
        throw new GlycoconjugateException("Invalid value for modification");
    }

    public String getName()
    {
        return this.m_strSymbol;
    }
    
}
