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
* Stores the annotation for an peak
* - list of residues (composition)
* - list of fragments
* - list of ions
* - list of exchanged ions
* - calculated mass
* - list of lost small molecules
* - list of gained small molecules
* - deviation
* 
* Deviation is null for non reducing fragments. Fragment list is empty for profiles.
* 
* @author rene
*
*/
public class PeakAnnotation
{
    private ArrayList<AnnotationEntity> m_aResidues = new ArrayList<AnnotationEntity>();
    private ArrayList<AnnotationEntity> m_aFragments = new ArrayList<AnnotationEntity>();
    private ArrayList<AnnotationEntity> m_aIons = new ArrayList<AnnotationEntity>();
    private ArrayList<AnnotationEntity> m_aIonExchange = new ArrayList<AnnotationEntity>();
    private double m_dMass = 0;
    private ArrayList<AnnotationEntity> m_aLoss = new ArrayList<AnnotationEntity>();
    private ArrayList<AnnotationEntity> m_aGain = new ArrayList<AnnotationEntity>();
    private String m_objDerivatisation = null;
    private double m_dDiviation = 0;
    
    public void setDivValue(double a_d)
    {
        this.m_dDiviation = a_d; 
    }
    
    public double getDivValue()
    {
        return this.m_dDiviation;
    }
    
    /**
     * Sets id(name) of the derivatisation
     * 
     * @param a_aDer
     */
    public void setDerivatisation(String a_aDer)
    {
        this.m_objDerivatisation = a_aDer;
    }
    
    /**
     * Gives id(name) of the derivatisation
     * 
     * @return
     */
    public String getDerivatisation()
    {
        return this.m_objDerivatisation;        
    }

    /**
     * Sets list of residue annotations
     * 
     * @param a_aResidues
     */
    public void setResidues(ArrayList<AnnotationEntity> a_aResidues)
    {
        this.m_aResidues = a_aResidues;
    }
    
    /**
     * Gives list of residue annotations
     * 
     * @return
     */
    public ArrayList<AnnotationEntity> getResidues()
    {
        return this.m_aResidues;        
    }
    
    /**
     * Sets list of fragments (empty for profile)
     * 
     * @param a_aFragments
     */
    public void setFragments(ArrayList<AnnotationEntity> a_aFragments)
    {
        this.m_aFragments = a_aFragments;
    }
    
    /**
     * Gives list of fragments (empty for profile)
     * 
     * @return
     */
    public ArrayList<AnnotationEntity> getFragments()
    {
        return this.m_aFragments;
    }
    
    /**
     * Set calculated mass of annotation
     * 
     * @param a_dMass
     */
    public void setMass(double a_dMass)
    {
        this.m_dMass = a_dMass;
    }
    
    /**
     * Gives calculated mass of annotation
     * 
     * @return
     */
    public double getMass()
    {
        return this.m_dMass;
    }
    
    /**
     * Set list of ions.
     * 
     * @param a_aIons
     */
    public void setIons(ArrayList<AnnotationEntity> a_aIons)
    {
        this.m_aIons = a_aIons;
    }
    
    /**
     * Give list of Ions
     * 
     * @return
     */
    public ArrayList<AnnotationEntity> getIons()
    {
        return this.m_aIons;
    }
    
    /**
     * Sets list of exchanged ions
     * 
     * @param a_aIons
     */
    public void setIonExchange(ArrayList<AnnotationEntity> a_aIons)
    {
        this.m_aIonExchange = a_aIons;
    }
    
    /**
     * Gives list of exchange ions
     * 
     * @return
     */
    public ArrayList<AnnotationEntity> getIonExchange()
    {
        return this.m_aIonExchange;
    }

    /**
     * Set list of gained small molecules
     * 
     * @param a_strName
     */
    public void setGain(ArrayList<AnnotationEntity> a_strName) 
    {
        this.m_aGain = a_strName;        
    }

    /**
     * Gives list of gained small molecules
     *  
     * @return
     */
    public ArrayList<AnnotationEntity> getGain()
    {
        return this.m_aGain;
    }

    /**
     * Sets list of lost small molecules
     * 
     * @param a_strName
     */
    public void setLoss(ArrayList<AnnotationEntity> a_strName) 
    {
        this.m_aLoss = a_strName;        
    }

    /**
     * Gives list of lost small molecules
     * 
     * @return
     */
    public ArrayList<AnnotationEntity> getLoss()
    {
        return this.m_aLoss;
    }

    /**
     * @return
     */
    public boolean profileAnnotation() 
    {
        if ( this.m_aFragments.size() == 0 )
        {
            return true;
        }
        return false;
    }
}