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

import java.util.ArrayList;


/**
* Stores the settings for a scan.
* - list of subscans
* - precursor mass (null for none)
* - list of peaks
* - id of the scan
* 
* @author rene
*/
public class Scan
{
    private ArrayList<Scan> m_aSubScan = new ArrayList<Scan>();
    private Double m_dPrecursorMass = null;
    private ArrayList<CalculationPeak> m_aPeaks = new ArrayList<CalculationPeak>();    
    private Integer m_iID = 0;
    /**
     * Sets the id of the scan
     * 
     * @param a_iId
     */
    public void setId(Integer a_iId)
    {
        this.m_iID = a_iId;
    }
    
    /**
     * Gives the id of the scan
     * 
     * @return
     */
    public Integer getId()
    {
        return this.m_iID;
    }
    
    /**
     * Sets the list of subscans
     * 
     * @param a_aSubscan
     */
    public void setSubScan ( ArrayList<Scan> a_aSubscan )
    {
        this.m_aSubScan = a_aSubscan;
    }
    
    /**
     * Gives the list of subscans
     * 
     * @return
     */
    public ArrayList<Scan> getSubScan()
    {
        return this.m_aSubScan;
    }
    
    /**
     * Sets the precursor mass (null for none)
     * 
     * @param a_dMass
     */
    public void setPrecursorMass(Double a_dMass)
    {
        this.m_dPrecursorMass = a_dMass;
    }
    
    /**
     * Gives the precorsor mass (null for none)
     * 
     * @return
     */
    public Double getPrecusorMass ()
    {
        return this.m_dPrecursorMass;
    }
    
    /**
     * Sets the list of peaks
     * 
     * @param a_aPeaks
     */
    public void setPeaks(ArrayList<CalculationPeak> a_aPeaks)
    {
        this.m_aPeaks = a_aPeaks;
    }
    
    /**
     * Gives the list of peaks
     * 
     * @return
     */
    public ArrayList<CalculationPeak> getPeaks()
    {
        return this.m_aPeaks;
    }

    /**
     * Adds an scan to the list of subscans
     * 
     * @param subScan
     */
    public boolean addSubScan(Scan a_objSubScan) 
    {
        return this.m_aSubScan.add(a_objSubScan);
    }
}
