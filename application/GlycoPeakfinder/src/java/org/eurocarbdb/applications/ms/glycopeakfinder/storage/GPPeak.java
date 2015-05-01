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
package org.eurocarbdb.applications.ms.glycopeakfinder.storage;

/**
* @author Logan
*
*/
public class GPPeak 
{
    private double m_dMZ = 0;
    private double m_dIntensity = 0;
    private Integer m_iChargeState = null;
    
    public double getMZ()
    {
        return this.m_dMZ;
    }
    
    public void setMZ(double a_dMZ)
    {
        this.m_dMZ = a_dMZ;
    }
    
    public double getIntensity()
    {
        return this.m_dIntensity;
    }

    public void setIntensity(double a_dIntensity)
    {
        this.m_dIntensity = a_dIntensity;
    }

    public void setCharge(Integer a_iCharge)
    {
        this.m_iChargeState = a_iCharge;
    }
    
    public Integer getCharge() 
    {
        return this.m_iChargeState;
    }
}
