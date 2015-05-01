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
package org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage;

/**
* Stores the settings for an ion.
* - id(name) of the ion
* - mass of the ion
* - charge of the ion
* 
* @author Logan
*/
public class CalculationIon 
{
    private String m_strID = null;
    private double m_dMass = 0;
    private Integer m_iCharge = 1;
    
    /**
     * Default constructor
     */
    public CalculationIon()
    {
        super();
    }
    
    /**
     * Constructor
     * 
     * @param a_strID name of the ion
     * @param a_dMass mass of the ion
     * @param a_iCharge charge of the ion
     */
    public CalculationIon(String a_strID,double a_dMass, int a_iCharge )
    {
        this.m_strID = a_strID;
        this.m_dMass = a_dMass;
        this.m_iCharge = a_iCharge;
    }
    
    /**
     * Sets charge of the ion
     * 
     * @param a_iCharge
     */
    public void setCharge(Integer a_iCharge)
    {
        this.m_iCharge = a_iCharge;
    }
    
    /**
     * Gives charge of the ion
     * 
     * @return
     */
    public Integer getCharge()
    {
        return this.m_iCharge;
    }
    
    /**
     * Sets id(name) of the ion
     * 
     * @param a_strId
     */
    public void setId(String a_strId)
    {
        this.m_strID = a_strId;
    }
    
    /**
     * Gives id(name) of the ion
     * 
     * @return
     */
    public String getId()
    {
        return this.m_strID;
    }
    
    /** 
     * Sets mass of the ion
     * 
     * @param a_dMass
     */
    public void setMass(double a_dMass)
    {
        this.m_dMass = a_dMass;
    }
    
    /**
     * Gives mass of the ion
     * 
     * @return
     */
    public double getMass()
    {
        return this.m_dMass;
    }
}