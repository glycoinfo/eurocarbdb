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

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.PeakAnnotation;

/**
* @author rene
* 
*/
public class GPAnnotation
{
    private String m_strComposition = "";
    private ArrayList<GPResidue> m_aResidues = new ArrayList<GPResidue>();
    private String m_strFragments = "";
    private String m_strIons = "";
    private double m_dMass = 0;
    private double m_dDeviation = 0;
    private int m_iNumber = 0;
    private String m_strGainLoss = "";
    private PeakAnnotation m_objAnnotations = null;

    public void setAnnotations(PeakAnnotation a_objAnno)
    {
        this.m_objAnnotations = a_objAnno;
    }
    
    public PeakAnnotation getAnnotations()
    {
        return this.m_objAnnotations;
    }
    
    public void setComposition(String a_strComposition)
    {
        this.m_strComposition = a_strComposition;        
    }
    
    public String getComposition()
    {
        return this.m_strComposition;
    }
    
    public void setResidues(ArrayList<GPResidue> a_aResidues)
    {
        this.m_aResidues = a_aResidues;
    }
    
    public ArrayList<GPResidue> getResidues()
    {
        return this.m_aResidues;        
    }
    
    public void setFragments(String a_aFragments)
    {
        this.m_strFragments = a_aFragments;
    }
    
    public String getFragments()
    {
        return this.m_strFragments;
    }
    
    public void setMass(double a_dMass)
    {
        this.m_dMass = a_dMass;
    }
    
    public double getMass()
    {
        return this.m_dMass;
    }
    
    public void setDeviation(double a_dDiv)
    {
        this.m_dDeviation = a_dDiv;
    }
    
    public double getDeviation()
    {
        return this.m_dDeviation;
    }
    
    public void setIons(String a_aIons)
    {
        this.m_strIons = a_aIons;
    }
    
    public String getIons()
    {
        return this.m_strIons;
    }
    
    public void setNumber(int a_iNumber)
    {
        this.m_iNumber = a_iNumber;
    }
    
    public int getNumber()
    {
        return this.m_iNumber;
    }

    public void setGainLossString(String a_strName) 
    {
        this.m_strGainLoss = a_strName;        
    }

    public String getGainLossString()
    {
        return this.m_strGainLoss;
    }
}
