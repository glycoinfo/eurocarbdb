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

import java.util.ArrayList;
import java.util.Iterator;

/**
* @author rene
*
*/
public class GPPeakAnnotated
{
    private double m_dMZ = 0;
    private double m_dIntensity = 0;
    private ArrayList<GPAnnotation> m_aAnnotation = new ArrayList<GPAnnotation>();
    private int m_iNumber = 0;
    private Integer m_iCharge = 0;
    private int m_iAnnotationCount = 0;
    
    public void setComplette(boolean a_b)
    {}
    
    public boolean getComplette()
    {
        if ( this.m_aAnnotation.size() == this.m_iAnnotationCount )
        {
            return true;
        }
        return false;
    }
    
    public void setAnnotationCount(int a_iNumber)
    {
        this.m_iAnnotationCount = a_iNumber;
    }
    
    public int getAnnotationCount()
    {
        return this.m_iAnnotationCount;
    }
    
    public void setCharge(Integer a_iCharge)
    {
        this.m_iCharge = a_iCharge;
    }
    
    public Integer getCharge()
    {
        return this.m_iCharge;
    }
    
    public void setMz(double a_dMZ)
    {
        this.m_dMZ = a_dMZ;
    }
    
    public double getMz()
    {
        return this.m_dMZ;
    }
    
    public void setIntensity(double a_dIntensity)
    {
        this.m_dIntensity = a_dIntensity;
    }
    
    public double getIntensity()
    {
        return this.m_dIntensity;
    }
    
    public void setAnnotation(ArrayList<GPAnnotation> a_aAnnotation)
    {
        this.m_aAnnotation = a_aAnnotation;
    }
    
    public ArrayList<GPAnnotation> getAnnotation()
    {
        return this.m_aAnnotation;
    }
    
    public void setNumber(int a_iNumber)
    {
        this.m_iNumber = a_iNumber;
    }
    
    public int getNumber()
    {
        return this.m_iNumber;
    }
    
    public int getCount()
    {
        if ( this.m_iAnnotationCount > this.m_aAnnotation.size() )
        {
            return this.m_aAnnotation.size() + 1;
        }
        return this.m_aAnnotation.size();
    }
    
    public int getLowAnnoId()
    {
        for (Iterator<GPAnnotation> t_iterAnno = this.m_aAnnotation.iterator(); t_iterAnno.hasNext();) 
        {
            return t_iterAnno.next().getNumber();            
        }  
        return 0;
    }
    
    public void setLowAnnoId(int a_iNumber)
    {}

    public int getRealCount()
    {
        return this.m_aAnnotation.size();
    }
    
    public void setRealCount(int a_iNumber)
    {}
}
