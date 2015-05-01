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
* Stores the settings for a derivatisation.
* - name of the derivatisation
* - mass
* 
* @author Logan
*
*/
public class CalculationDerivatisation 
{
    private String m_strID = null;
    private double m_dMass = 0;

    /**
     * Default constructor
     */
    public CalculationDerivatisation()
    {
        super();
    }
    
    /**
     * Constructor 
     * 
     * @param a_strId    name of the derivatisation
     * @param a_dMass    mass of the derivatisation
     */
    public CalculationDerivatisation(String a_strId, double a_dMass)
    {
        this.m_strID = a_strId;
        this.m_dMass = a_dMass;
    }
    
    /**
     * Sets the name of the derivatisation.
     * 
     * @param a_strId
     */
    public void setId(String a_strId)
    {
        this.m_strID = a_strId;
    }
    
    /**
     * Gives the name of the derivatisation
     * 
     * @return
     */
    public String getId()
    {
        return this.m_strID;
    }
    
    /**
     * Sets the mass of the derivatisation
     * 
     * @param a_dMass
     */
    public void setMass(double a_dMass)
    {
        this.m_dMass = a_dMass;
    }
    
    /**
     * Gives the name of the derivatisation
     * 
     * @return
     */
    public double getMass()
    {
        return this.m_dMass;
    }
}