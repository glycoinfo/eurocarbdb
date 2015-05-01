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
public class LimitValues 
{
    private double m_dMaxMZ = 8000;
    private int m_iMaxPeakCount = 250;
    private double m_dMaxAccuracyPPM = 2000;
    private double m_dMaxAccuracyU = 5;
    private int m_iMaxLossNumber = 5;
    private int m_iMaxGlycoSciencesResults = 25;
    private int m_iMaxAnnotationPerPeak = 50;
    
    public void setMaxAnnotationPerPeak(int a_iMax)
    {
        this.m_iMaxAnnotationPerPeak = a_iMax;
    }
    
    public int getMaxAnnotationPerPeak()
    {
        return this.m_iMaxAnnotationPerPeak;
    }
    
    public void setMaxGlycosciencesResults(int a_iNumber)
    {
        this.m_iMaxGlycoSciencesResults = a_iNumber;
    }
    
    public int getMaxGlycosciencesResults()
    {
        return this.m_iMaxGlycoSciencesResults;
    }
    
    public int getMaxLossNumber()
    {
        return this.m_iMaxLossNumber;
    }
    
    public void setMaxLossNumber(int a_iNumber)
    {
        this.m_iMaxLossNumber = a_iNumber;
    }
    
    public double getMaxMZ()
    {
        return this.m_dMaxMZ;
    }
    
    public void setMaxMZ(double a_dValue)
    {
        this.m_dMaxMZ = a_dValue;
    }
    
    public int getMaxPeakCount()
    {
        return this.m_iMaxPeakCount;
    }

    public void setMaxPeakCount(int a_iCount)
    {
        this.m_iMaxPeakCount = a_iCount;
    }

    public double getMaxAccuracyPPM()
    {
        return this.m_dMaxAccuracyPPM;
    }
    
    public void setMaxAccuracyPPM(double a_dValue)
    {
        this.m_dMaxAccuracyPPM = a_dValue;
    }

    public double getMaxAccuracyU()
    {
        return this.m_dMaxAccuracyU;
    }
    
    public void setMaxAccuracyU(double a_dValue)
    {
        this.m_dMaxAccuracyU = a_dValue;
    }
}
