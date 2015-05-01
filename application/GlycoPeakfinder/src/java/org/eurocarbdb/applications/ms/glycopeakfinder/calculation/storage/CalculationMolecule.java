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
* Stores the settings of an molecule
* Used for residues, gain and loss
* - id(name) of the molecule
* - mass of the molecule
* - min occurrence of the molecule
* - max occurrence of the molecule
*  
* @author Logan
*/
public class CalculationMolecule 
{
    protected String m_strID = null;
    protected double m_dMass = 0;
    protected int m_iMin = 0;
    protected int m_iMax = 0;
    
    /**
     * Default constructor
     */
    public CalculationMolecule()
    {
        super();        
    }
    
    /**
     * Constructor
     * 
     * @param a_strID name of the residue
     * @param a_dMass mass of the residue
     * @param a_iMin min occurrence of the molecule
     * @param a_iMax max occurrence of the molecule
     */
    public CalculationMolecule( String a_strID , double a_dMass, int a_iMin, int a_iMax)
    {
        super();
        this.m_strID = a_strID;
        this.m_dMass = a_dMass;
        this.m_iMin = a_iMin;
        this.m_iMax = a_iMax;
    }
    
    /**
     * Sets id(name) of the molecule/residue
     * 
     * @param a_strId
     */
    public void setId(String a_strId)
    {
        this.m_strID = a_strId;
    }
    
    /**
     * Gives id(name) of the molecule/residue
     * 
     * @return
     */
    public String getId()
    {
        return this.m_strID;
    }
    
    /**
     * Sets mass of the molecule/residue
     * 
     * @param a_dMass
     */
    public void setMass(double a_dMass)
    {
        this.m_dMass = a_dMass;
    }
    
    /**
     * Gives mass of the molecule/residue
     * 
     * @return
     */
    public double getMass()
    {
        return this.m_dMass;
    }
    
    /**
     * Sets min occurrence of the molecule/residue
     * 
     * @param a_iValue
     */
    public void setMin(int a_iValue )
    {
        this.m_iMin = a_iValue;
    }
    
    /**
     * Gives min occurrence of the molecule/residue
     * 
     * @return
     */
    public int getMin()
    {
        return this.m_iMin;
    }

    /**
     * Sets max occurrence of the molecule/residue
     * 
     * @param a_iValue
     */
    public void setMax(int a_iValue )
    {
        this.m_iMax = a_iValue;
    }
    
    /**
     * Gives max occurrence of the molecule/residue
     * 
     * @return
     */
    public int getMax()
    {
        return this.m_iMax;
    }
}