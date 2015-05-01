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
public class MassMolecule 
{
    private String m_strName = "";
    private String m_strAbbr = "";
    private int m_iLoss = 0;
    private int m_iGain = 0;
    private double m_dMassMono = 0;
    private double m_dMassAvg = 0;
    private String m_strID = "";
    
    public void setId(String a_strID)
    {
        this.m_strID = a_strID;
    }
    
    public String getId()
    {
        return this.m_strID;
    }

    public void setMassMono(double a_dMass)
    {
        this.m_dMassMono = a_dMass;
    }
    
    public double getMassMono()
    {
        return this.m_dMassMono;
    }
    
    public void setMassAvg(double a_dMass)
    {
        this.m_dMassAvg = a_dMass;
    }
    
    public double getMassAvg()
    {
        return this.m_dMassAvg;
    }

    public void setName(String a_strName)
    {
        this.m_strName = a_strName;
    }
    
    public String getName()
    {
        return this.m_strName;
    }
    
    public void setLoss(int a_iMin)
    {
        this.m_iLoss = a_iMin;
    }
    
    public int getLoss()
    {
        return this.m_iLoss;
    }
    
    public void setGain(int a_iMax)
    {
        this.m_iGain = a_iMax;
    }
    
    public int getGain()
    {
        return this.m_iGain;
    }
    
    public void setAbbr(String a_strAbbr) 
    {
        this.m_strAbbr = a_strAbbr;
    }
    
    public String getAbbr()
    {
        return this.m_strAbbr;
    }
}