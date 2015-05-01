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
* Stores the settings of the fragments
* - id of the fragment
* - mass of the fragment
* - name of the reside for a X/Y fragment (null for B,C,Y,Z)
* 
* @author rene
*
*/
public class CalculationFragment
{
    private String m_strId = null;
    private String m_strResidueId = null;
    private double m_dMass = 0;
    private String m_strType = null;
    
    /**
     * Default constructor
     */
    public CalculationFragment()
    {
        super();
    }
    
    /**
     * Constructor 
     * 
     * @param a_strName    name of the fragment
     * @param a_strResidueID name of the residue for a A/X fragment
     * @param a_dMass mass of the fragment
     */
    public CalculationFragment(String a_strName, String a_strResidueID, double a_dMass )
    {
        this.m_strId = a_strName;
        this.m_strResidueId = a_strResidueID;
        this.m_dMass = a_dMass;
        this.m_strType = a_strName;
    }
    
    public CalculationFragment(String a_strName, String a_strResidueID, double a_dMass, String a_strFragmenttype )
    {
        this.m_strId = a_strName;
        this.m_strResidueId = a_strResidueID;
        this.m_dMass = a_dMass;
        this.m_strType = a_strFragmenttype;
    }

    /**
     * Sets the name of the fragment.
     * 
     * @param a_strId
     */
    public void setId(String a_strId)
    {
        this.m_strId = a_strId;
    }
    
    /**
     * Gives the name of the fragment
     * 
     * @return
     */
    public String getId()
    {
        return this.m_strId;
    }
    
    /**
     * Sets the mass of the fragment
     * 
     * @param a_dMass
     */
    public void setMass(double a_dMass)
    {
        this.m_dMass = a_dMass;
    }
    
    /**
     * Gives the mass of the fragment
     * 
     * @return
     */
    public double getMass()
    {
        return this.m_dMass;
    }

    /**
     * Sets the id(name) of the residue an A/X fragments belongs to.
     * null for Glycosidic cleavages
     * @param a_strId
     */
    public void setResidueId(String a_strId)
    {
        this.m_strResidueId = a_strId;
    }
    
    /**
     * Gives the id(name) of the residue for an A/X fragment
     * 
     * @return
     */
    public String getResidueId()
    {
        return this.m_strResidueId;
    }
    
    public String getFragmentType()
    {
        return this.m_strType;
    }
    
    public void setFragmentType(String a_strType)
    {
        this.m_strType = a_strType;
    }
}
