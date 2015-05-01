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
import java.util.Iterator;



/**
* Object which contains all settings for a calculation
* 
* @author rene
*
*/
public class CalculationParameter
{
    // ResidueList
    private ArrayList<CalculationMolecule> m_aResidue = new ArrayList<CalculationMolecule>();
    // PeakList
    private Scan m_aPeakList = null;
    // profile ; fragmented ; msxms ; 
    private SpectraType m_strSpectraType = SpectraType.Profile;
    // multi fragmentation
    private ArrayList<Integer> m_aMultiFragmentation = new ArrayList<Integer>();
    // ion exchange 1,2 ...
    private ArrayList<Integer> m_aIonExchange = new ArrayList<Integer>();
    // Fragmenttypes A,B ...
    private ArrayList<CalculationFragment> m_aFragmentTypeNonRed = new ArrayList<CalculationFragment>();
    private ArrayList<CalculationFragment> m_aFragmentTypeRed = new ArrayList<CalculationFragment>();
    // chargeState 1,2 ...
    private ArrayList<Integer> m_aCharges = new ArrayList<Integer>();
    // accuracy value
    private double m_dAccuracy = 0;
    // accuracy type ppm,u
    private boolean m_bPPM = true;
    // ions : h+ , na+ , k+ , li+ , -h+ , other
    private ArrayList<CalculationIon> m_aIons = new ArrayList<CalculationIon>();
    // ions exchange
    private ArrayList<CalculationIon> m_aIonExchangeIons = new ArrayList<CalculationIon>();
    // mass shift
    private double m_dMassShift = 0;
    // mass of the derivatisation
    private ArrayList<CalculationDerivatisation> m_aDerivatisation = new ArrayList<CalculationDerivatisation>();
    // loss / gain
    private ArrayList<CalculationMolecule> m_aMoleculesLoss = new ArrayList<CalculationMolecule>(); 
    private ArrayList<CalculationMolecule> m_aMoleculesGain = new ArrayList<CalculationMolecule>();
    // completion
    private double m_dCompletionRed = 0;    
    private double m_dCompletionNonRed = 0;
    private double m_dNonRedFragmentAbzug = 0;
    private double m_dExchangeIonMass = 0;
    // some unused things
    private Persubstitution m_enumPersubstitution = Persubstitution.None;
    private boolean m_bMonoisotopic = true;
    // number of max annotations
    private int m_iMaxAnnotationPerPeak = 0;

    public void setMaxAnnotationPerPeak(int a_iNumber)
    {
        this.m_iMaxAnnotationPerPeak = a_iNumber;
    }
    
    public int getMaxAnnotationPerPeak()
    {
        return this.m_iMaxAnnotationPerPeak;
    }
    
    public void setMonoisotopic(boolean a_bMono)
    {
        this.m_bMonoisotopic = a_bMono;
    }
    
    public boolean getMonoisotopic()
    {
        return this.m_bMonoisotopic;
    }


    public void setPersubstitution(Persubstitution a_enumPersub)
    {
        this.m_enumPersubstitution = a_enumPersub;
    }
    
    public Persubstitution getPersubstitution()
    {
        return this.m_enumPersubstitution;
    }
    
    /**
     * Sets the list of Ionexchange ions
     * 
     * @param a_aValues
     */
    public void setIonExchangeIon(ArrayList<CalculationIon> a_aValues)
    {
        this.m_aIonExchangeIons = a_aValues;
    }
    
    /**
     * Gives the list of ionexchange ions
     * 
     * @return
     */
    public ArrayList<CalculationIon> getIonExchangeIon()
    {
        return this.m_aIonExchangeIons;
    }

    /**
     * Set the list of gain molecules
     * 
     * @param a_aMolecules
     */
    public void setGainMolecules(ArrayList<CalculationMolecule> a_aMolecules)
    {
        this.m_aMoleculesGain = a_aMolecules;
    }
    
    /**
     * Gives the list of gain molecules
     * 
     * @return
     */
    public ArrayList<CalculationMolecule> getGainMolecules()
    {
        return this.m_aMoleculesGain;
    }
    
    /**
     * Set the list of loss molecules
     * 
     * @param a_aMolecules
     */
    public void setLossMolecules(ArrayList<CalculationMolecule> a_aMolecules)
    {
        this.m_aMoleculesLoss = a_aMolecules;
    }
    
    /**
     * Give the list of loss molecules
     * 
     * @return
     */    
    public ArrayList<CalculationMolecule> getLossMolecules()
    {
        return this.m_aMoleculesLoss;
    }

    /**
     * Set the mass shift
     * 
     * @param a_dShift
     */
    public void setMassShift( double a_dShift )
    {
        this.m_dMassShift = a_dShift;
    }
    
    /** 
     * Give the mass shift
     * 
     * @return
     */
    public double getMassShift()
    {
        return this.m_dMassShift;
    }
    
    /**
     * Set the list of fragmentation level
     * 
     * @param a_aMulti
     */
    public void setMultiFragments(ArrayList<Integer> a_aMulti)
    {
        this.m_aMultiFragmentation = a_aMulti;
    }
    
    /**
     * Give the list of fragmentation level
     * 
     * @return
     */
    public ArrayList<Integer> getMultiFragments()
    {
        return this.m_aMultiFragmentation;
    }
    
    /**
     * Set the list of ionexchange counts
     * 
     * @param a_aExchange
     */
    public void setIonExchangeCount(ArrayList<Integer> a_aExchange)
    {
        this.m_aIonExchange = a_aExchange;
    }
    
    /**
     * Give the list of ionexchange counts
     * 
     * @return
     */
    public ArrayList<Integer> getIonExchangeCount()
    {
        return this.m_aIonExchange;
    }
    
    /**
     * Set the list of non reducing fragments
     * 
     * @param a_aTypes
     */
    public void setFragmentsNonRed( ArrayList<CalculationFragment> a_aTypes )
    {
        this.m_aFragmentTypeNonRed = a_aTypes;
    }
    
    /**
     * Give the list of non reducing fragments
     * 
     * @return
     */
    public ArrayList<CalculationFragment> getFragmentsRed()
    {
        return this.m_aFragmentTypeRed;
    }
    
    /**
     * Set the list of reducing fragments
     * 
     * @param a_aTypes
     */
    public void setFragmentsRed( ArrayList<CalculationFragment> a_aTypes )
    {
        this.m_aFragmentTypeRed = a_aTypes;
    }
    
    /**
     * Give the list of reducing fragments
     * 
     * @return
     */
    public ArrayList<CalculationFragment> getFragmentsNonRed()
    {
        return this.m_aFragmentTypeNonRed;
    }

    /**
     * Set the list of charges
     * 
     * @param a_aCharge
     */
    public void setCharges( ArrayList<Integer> a_aCharge)
    {
        this.m_aCharges = a_aCharge;
    }
    
    /**
     * Give the list of charges
     * 
     * @return
     */
    public ArrayList<Integer> getCharges()
    {
        return this.m_aCharges;
    }
    
    /**
     * Set accuracy value
     * 
     * @param a_dAccuracy
     */
    public void setAccuracy(double a_dAccuracy)
    {
        this.m_dAccuracy = a_dAccuracy;
    }
    
    /**
     * Give accuracy value
     * 
     * @return
     */
    public double getAccuracy()
    {
        return this.m_dAccuracy;
    }
    
    /**
     * Set accurracy type (true for ppm ; false for u)
     * 
     * @param a_bPpm
     */
    public void setAccuracyPpm(boolean a_bPpm)
    {
        this.m_bPPM = a_bPpm;
    }
    
    /**
     * Give accurracy type (true for ppm ; false for u)
     * 
     * @return
     */
    public boolean getAccuracyPpm()
    {
        return this.m_bPPM;
    }
    
    /**
     * Set list of derivatisations
     * 
     * @param a_strValue
     */
    public void setDerivatisation(ArrayList<CalculationDerivatisation> a_strValue)
    {
        this.m_aDerivatisation = a_strValue;
    }
    
    /**
     * Give list of derivatisations
     * 
     * @return
     */
    public ArrayList<CalculationDerivatisation> getDerivatisation()
    {
        return this.m_aDerivatisation;
    }
    
    /** 
     * Set list of ions
     * 
     * @param a_aIons
     */
    public void setIons(ArrayList<CalculationIon> a_aIons)
    {
        this.m_aIons = a_aIons;
    }
    
    /** 
     * Give list of ions
     * 
     * @return
     */
    public ArrayList<CalculationIon> getIons()
    {
        return this.m_aIons;
    }
    
    /** 
     * Set list of residues
     * 
     * @param a_aResidues
     */
    public void setResidues( ArrayList<CalculationMolecule> a_aResidues )
    {
        this.m_aResidue = a_aResidues;
    }
    
    /** 
     * Give list of residues
     * 
     * @return
     */
    public ArrayList<CalculationMolecule> getResidues()
    {
        return this.m_aResidue;
    }
    
    /** 
     * Set scan
     * 
     * @param a_aPeaks
     */
    public void setScan( Scan a_aPeaks )
    {
        this.m_aPeakList = a_aPeaks;
    }
    
    /**
     * Give scan
     * 
     * @return
     */
    public Scan getScan()
    {
        return this.m_aPeakList;
    }
    
    /** 
     * Set type of spectra 
     * 
     * @param a_strType
     */
    public void setSpectraType(SpectraType a_strType)
    {
        this.m_strSpectraType = a_strType;
    }
    
    /**
     * Give Type of Spectra
     * 
     * @return
     */
    public SpectraType getSpectraType()
    {
        return this.m_strSpectraType;
    }
    
    /**
     * Give non reducing completion
     * 
     * @return
     */
    public double getCompletionNonRed()
    {
        return this.m_dCompletionNonRed;
    }
    
    /**
     * Set reducing completion
     * 
     * @param a_dMass
     */
    public void setCompletionRed(double a_dMass)
    {
        this.m_dCompletionRed = a_dMass;
    }

    /**
     * Give reducing completion
     * 
     * @return
     */
    public double getCompletionRed()
    {
        return this.m_dCompletionRed;
    }
    
    /**
     * Set non reducing completion
     * 
     * @param a_dMass
     */
    public void setCompletionNonRed(double a_dMass)
    {
        this.m_dCompletionNonRed = a_dMass;
    }

    /**
     * Give larges fragmentations level
     * 
     * @return
     */
    public int getMaxFragments()
    {
        int t_iMax = 0;
        Integer t_iCurrent= 0;
        for (Iterator<Integer> t_iterFrag = this.m_aMultiFragmentation.iterator(); t_iterFrag.hasNext();) 
        {
            t_iCurrent = t_iterFrag.next();
            if ( t_iMax < t_iCurrent )
            {
                t_iMax = t_iCurrent;
            }
        }
        return t_iMax;
    }

    /**
     * Give larges charge state
     * 
     * @return
     */
    public int getMaxCharges()
    {
        int t_iMax = 0;
        Integer t_iCurrent= 0;
        for (Iterator<Integer> t_iterFrag = this.m_aCharges.iterator(); t_iterFrag.hasNext();) 
        {
            t_iCurrent = t_iterFrag.next();
            if ( t_iMax < t_iCurrent )
            {
                t_iMax = t_iCurrent;
            }
        }
        return t_iMax;
    }

    /**
     * Give larges ionexchange level
     * @return
     */
    public int getMaxIonExchange()
    {
        int t_iMax = 0;
        Integer t_iCurrent= 0;
        for (Iterator<Integer> t_iterFrag = this.m_aIonExchange.iterator(); t_iterFrag.hasNext();) 
        {
            t_iCurrent = t_iterFrag.next();
            if ( t_iMax < t_iCurrent )
            {
                t_iMax = t_iCurrent;
            }
        }
        return t_iMax;
    }
    
    /**
     * Give mass of exchanged ion
     * 
     * @return
     */
    public double getExchangeIonMass() 
    {
        return this.m_dExchangeIonMass;
    }

    /**
     * Set mass of exchanged ion
     * 
     * @param a_dMass
     */
    public void setExchangeIonMass(double a_dMass)
    {
        this.m_dExchangeIonMass = a_dMass;        
    }
    
    /**
     * Give mass of non reducing difference
     * 
     * @return
     */
    public double getNonReducingDifference() 
    {
        return this.m_dNonRedFragmentAbzug;
    }
    
    /**
     * Set mass of non reducing difference
     * 
     * @param a_dMass
     */
    public void setNonReducingDifference(double a_dMass)
    {
        this.m_dNonRedFragmentAbzug = a_dMass;
    }
}