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
public enum Superclass
{
    SUG("sug",0),
    TRI("tri",3),
    TET("tet",4),
    PEN("pen",5),
    HEX("hex",6),
    HEP("hep",7),
    OCT("oct",8),
    NON("non",9),
    DEC("dec",10),
    S11("s11",11),
    S12("s12",12),
    S13("s13",13),
    S14("s14",14),
    S15("s15",15),
    S16("s16",16),
    S17("s17",17),
    S18("s18",18),
    S19("s19",19),
    S20("s20",20);
    
    private String m_strName;
    private int m_iCount;
    
    private Superclass( String a_strName, int a_iCount )
    {
        this.m_strName = a_strName;
        this.m_iCount = a_iCount;
    }
    
    public String getName() 
    {  
        return this.m_strName;  
    }
    
    public int getCAtomCount() 
    {  
        return this.m_iCount;  
    }
    
    public static Superclass forName( String a_strName )
    {
        String t_strName = a_strName.toUpperCase();
        for ( Superclass t_objSuperclass : Superclass.values() )
        {
            if ( t_objSuperclass.m_strName.equalsIgnoreCase(t_strName) )
            {
                return t_objSuperclass;
            }
        }
        return null;
    }    

    public static Superclass forCAtomCount( int a_iNumber )
    {
        for ( Superclass t_objSuperclass : Superclass.values() )
        {
            if ( t_objSuperclass.m_iCount == a_iNumber )
            {
                return t_objSuperclass;
            }
        }
        return null;
    }    

}
