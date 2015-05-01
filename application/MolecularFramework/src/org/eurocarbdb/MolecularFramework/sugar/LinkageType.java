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
* @author rene
*
*/
public enum LinkageType
{
    H_LOSE('h'),
    DEOXY('d'),
    H_AT_OH('o'),
    UNKNOWN('x'),
    NONMONOSACCHARID('n'),
    S_CONFIG('s'),
    R_CONFIG('r'),
    UNVALIDATED('u');

    private char m_cSymbol;

    private LinkageType( char a_cSymbol )
    {
        this.m_cSymbol = a_cSymbol;
    }
    
    public char getType() 
    {  
        return this.m_cSymbol;  
    }

    public static LinkageType forName( char a_cName ) throws GlycoconjugateException
    {
        for ( LinkageType a : LinkageType.values() )
        {
            if ( a_cName == a.m_cSymbol )
            {
                return a;
            }
        }
        throw new GlycoconjugateException("Invalid value for a linkagetype");
    }

   
}
