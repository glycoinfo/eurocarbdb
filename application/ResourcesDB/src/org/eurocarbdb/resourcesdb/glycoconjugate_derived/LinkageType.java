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


/**
* @author rene, thomas
*
*/
public enum LinkageType
{
    H_LOSE('h'),
    DEOXY('d'),
    H_AT_OH('o'),
    NONMONOSACCHARID('n'),
    S_CONFIG('s'),
    R_CONFIG('r');

    private char m_cSymbol;

    private LinkageType(char a_cSymbol) {
        this.m_cSymbol = a_cSymbol;
    }
    
    public char getType() {  
        return this.m_cSymbol;  
    }
    
    public String getName() {
        return this.name();
    }

    public static LinkageType forName( char a_cName ) throws GlycoconjugateException {
        for(LinkageType lt : LinkageType.values() ) {
            if(a_cName == lt.m_cSymbol) {
                return lt;
            }
        }
        throw new GlycoconjugateException("Invalid value for a linkagetype: '" + a_cName + "'");
    }
    
    public static LinkageType forName(String name) throws GlycoconjugateException {
        if(name != null && name.length() > 0) {
            for(LinkageType lt : LinkageType.values()) {
                if(name.length() == 1 && name.charAt(0) == lt.m_cSymbol) {
                    return lt;
                }
                if(name.equalsIgnoreCase(lt.name())) {
                    return lt;
                }
            }
        }
        throw new GlycoconjugateException("Invalid value for a linkagetype: \"" + name + "\"");
   }

   
}
