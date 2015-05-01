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
package org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage;

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.ParameterException;

/**
* Stores the type of the spectra.
* 
* @author Logan
*
*/
public enum SpectraType
{
    Profile("profile"),         // ms^1     nur profile ionen
    MS2("ms2"),                 // ms^2     precursor is profile, rest fragmented
    Fragmented("fragmented"),   // ms^>2    all ions are fragmented
    MSxMS("msxms");             //          completter run von ms^1 bis ms^x
    
    private String m_strFullname;

    /** Private constructor, see the forName methods for external use. */
    private SpectraType( String fullname)
    {
        this.m_strFullname = fullname;
    }

    /**
     * Gives the type of spectra
     * @return
     */
    public String getName() 
    {  
        return this.m_strFullname;  
    }

    /**
     * Gives a spetra type object for a name
     * 
     * @param type
     * @return
     * @throws ParameterException
     */
    public static SpectraType forName( String type ) throws ParameterException 
    {
        for ( SpectraType a : SpectraType.values() )
        {
            if ( type.equals(a.m_strFullname) )
            {
                return a;
            }
        }
        throw new ParameterException("Invalid value for spectra type.");
    }
}
