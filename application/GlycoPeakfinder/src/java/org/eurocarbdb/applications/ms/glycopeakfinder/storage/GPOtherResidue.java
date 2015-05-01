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
public class GPOtherResidue 
{
    private String m_strName = "";
    private int m_iMin = 0;
    private int m_iMax = 0;
    private double m_dMass = 0;
    
    public void setName(String a_strName)
    {
        this.m_strName = a_strName;
    }
    
    public String getName()
    {
        return this.m_strName;
    }
    
    public void setMin(int a_iMin)
    {
        this.m_iMin = a_iMin;
    }
    
    public int getMin()
    {
        return this.m_iMin;
    }
    
    public void setMax(int a_iMax)
    {
        this.m_iMax = a_iMax;
    }
    
    public int getMax()
    {
        return this.m_iMax;
    }

    public void setMass(double a_dMass)
    {
        this.m_dMass = a_dMass;
    }
    
    public double getMass()
    {
        return this.m_dMass;
    }
}