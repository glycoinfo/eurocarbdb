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
/**
* 
*/
package org.eurocarbdb.resourcesdb.glycoconjugate_derived;

public enum EcdbAnomer
{
    Alpha("alpha", "a"),
    Beta("beta", "b"),
    OpenChain("open-chain", "o"),
    Unknown("unknown", "x");

    /** Anomer verbose name */
    private String m_strFullname;

    /** Anomer short name. */
    private String m_strSymbol;

    /** Private constructor, see the forName methods for external use. */
    private EcdbAnomer( String fullname, String symbol )
    {
        this.m_strFullname = fullname;
        this.m_strSymbol = symbol;
    }

    /** Returns this anomer's full name  */
    public String getFullname() 
    {  
        return this.m_strFullname;  
    }

    /** Returns the abbreviated name (symbol) of this anomer.  */
    public String getSymbol() 
    {  
        return this.m_strSymbol;  
    }

    /** Returns the appropriate Anomer instance for the given character/symbol.  
     * @throws GlycoconjugateException */
    public static EcdbAnomer forName( char anomer ) throws GlycoconjugateException
    {
        for ( EcdbAnomer a : EcdbAnomer.values() )
        {
            if ( anomer == a.m_strSymbol.charAt(0) )
            {
                return a;
            }
        }
        throw new GlycoconjugateException("Invalid value for anomer");
    }
}
