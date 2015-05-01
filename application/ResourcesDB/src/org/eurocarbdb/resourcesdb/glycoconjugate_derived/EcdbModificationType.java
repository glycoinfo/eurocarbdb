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
* @author Logan
*
*/
public enum EcdbModificationType 
{
    DEOXY("d", false),
    ACID("a", false),
    KETO("keto", false),
    ALDI("aldi", false),
    DOUBLEBOND("en", false),
    UNKNOWN_BOUBLEBOND("enx", false),
    SP2_HYBRID("sp2", false),
    GEMINAL("geminal", false),
    ANHYDRO("anhydro", true),
    LACTONE("lactone", true);
    
    private String m_strSymbol = "";
    private boolean msdbOnly = false;
    
    /** Private constructor, see the forName methods for external use. */
    private EcdbModificationType(String symbol, boolean isMsdbOnly) {
        this.m_strSymbol = symbol;
        this.msdbOnly = isMsdbOnly;
    }

    /** Returns the appropriate Anomer instance for the given character/symbol.  
     * @throws GlycoconjugateException */
    public static EcdbModificationType forName( String a_strModi ) throws GlycoconjugateException
    {
        for ( EcdbModificationType a : EcdbModificationType.values() )
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
    
    /**
     * If the msdbOnly flag is set, the modification is only valid in MonosaccharideDB namespace.
     * In GlycoCT, the modification is represented as a substitution in that case.
     * @return the value of the msdbOnly flag
     */
    public boolean isMsdbOnly() {
        return this.msdbOnly;
    }
    
}
