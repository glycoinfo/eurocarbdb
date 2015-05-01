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
* Stores the settings for a peak
* - mz value
* - intensity
* - min mz value (deviation)
* - max mz value (deviation)
* - list of annotations
*  
* @author Logan
*/
public class CalculationPeak 
{
    private double m_dMZ = 0;
    private double m_dMinMZ = 0;
    private double m_dMaxMZ = 0;
    private double m_dIntensity = 0;
    private ArrayList<PeakAnnotation> m_aAnnotation = new ArrayList<PeakAnnotation>();
    private Integer m_iChargeState = null;
    private int m_iAnnotationCount = 0; 

    /**
     * Default constructor
     */
    public CalculationPeak()
    {
        super();
    }
    
    /**
     * Constructor
     * 
     * @param a_dMZ mz value
     * @param a_dIntensity intensity
     */
    public CalculationPeak(double a_dMZ, double a_dIntensity)
    {
        super();
        this.m_dMZ = a_dMZ;
        this.m_dIntensity = a_dIntensity;
    }
    
    /**
     * Constructor
     * 
     * @param a_dMZ mz value
     * @param a_dIntensity intensity
     * @param a_iCharge charge of the peak
     */
    public CalculationPeak(double a_dMZ, double a_dIntensity,int a_iCharge)
    {
        super();
        this.m_dMZ = a_dMZ;
        this.m_dIntensity = a_dIntensity;
        this.m_iChargeState = a_iCharge;
    }

    public void setAnnotationCount(int a_iAnnotation)
    {
        this.m_iAnnotationCount = a_iAnnotation;
    }
    
    public int getAnnotationCount()
    {
        return this.m_iAnnotationCount;
    }
    
    public void incrementAnnotationCount()
    {
        this.m_iAnnotationCount++;
    }
    
    /**
     * Sets the mz value of the peak
     */
    public void setMz(double a_dMZ)
    {
        this.m_dMZ = a_dMZ;
    }
    
    /**
     * Gives mz value of the peak
     * 
     * @return
     */
    public double getMz()
    {
        return this.m_dMZ;
    }
    
    /**
     * Sets intensity value
     * 
     * @param a_dIntensity
     */
    public void setIntensity(double a_dIntensity)
    {
        this.m_dIntensity = a_dIntensity;
    }
    
    /**
     * Gives intensity value
     * 
     * @return
     */
    public double getIntensity()
    {
        return this.m_dIntensity;
    }
    
    /**
     * Set list of annotations for the peak
     * 
     * @param a_aAnnotation
     */
    public void setAnnotation(ArrayList<PeakAnnotation> a_aAnnotation)
    {
        this.m_aAnnotation = a_aAnnotation;
    }
    
    /**
     * Gives list of annotations 
     * 
     * @return
     */
    public ArrayList<PeakAnnotation> getAnnotation()
    {
        return this.m_aAnnotation;
    }
    
    /**
     * Gives min value of mz (-deviation)
     * 
     * @return
     */
    public double minMz()
    {
        return this.m_dMinMZ;
    }
    
    /**
     * Gives max value of mz (+deviation)
     * 
     * @return
     */
    public double maxMz()
    {
        return this.m_dMaxMZ;
    }

    /**
     * Calculates min and max mz based on a deviation in U
     * 
     * @param a_dDev
     */
    public void calculateDeviationU(double a_dDev)
    {
        this.m_dMaxMZ = this.m_dMZ + a_dDev;
        this.m_dMinMZ = this.m_dMZ - a_dDev;
    }
    
    /**
     * Calculates min and max mz based on a deviation in ppm
     *  
     * @param a_dDev
     */
    public void calculateDeviationPpm(double a_dDev)
    {
        double t_dDelta = (a_dDev * this.m_dMZ) / 1000000.0;
        this.m_dMinMZ = this.m_dMZ - t_dDelta;
        this.m_dMaxMZ = this.m_dMZ + t_dDelta;
    }

    /**
     * Adds an annotation for the peak
     * 
     * @param annotation
     */
    public void addAnnotation(PeakAnnotation a_objAnnotation) 
    {
        this.m_aAnnotation.add(a_objAnnotation);        
    }

    /**
     * Set charge of the peak
     * 
     * @param a_iCharge
     */
    public void setCharge(Integer a_iCharge)
    {
        this.m_iChargeState = a_iCharge;
    }
    
    /**
     * Give charge of the peak
     * 
     * @return
     */
    public Integer getCharge()
    {
        return this.m_iChargeState;
    }
}