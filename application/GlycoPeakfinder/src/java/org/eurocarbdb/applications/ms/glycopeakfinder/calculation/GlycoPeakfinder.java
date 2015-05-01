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
package org.eurocarbdb.applications.ms.glycopeakfinder.calculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.AnnotationEntity;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationIon;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationMolecule;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationParameter;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationPeak;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationDerivatisation;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationFragment;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.PeakAnnotation;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.ResidueSpecial;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Scan;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.SpectraType;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.util.ComparatorCalculationPeak;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.util.ComparatorMolecule;

/**
* Does the glycopeakfinder calculation
* 
* @author Logan
* 
*/
public class GlycoPeakfinder 
{
    // calculation internal variables

    //     aus testParameter
    double m_dNonReducingFragmentAbzug             = 0;
    double m_dMassExchangeIon                    = 0;
    double m_dMinResidueMass                     = 0;
    double m_dChargeCount                         = 1;

    // maxima
    int m_iMaxChargeCount                        = 0;
    int m_iMaxExchangeCount                        = 0;
    int m_iMaxFragmentCount                        = 0;
    int m_iMaxAnnotationCount                    = 0;

    CalculationParameter m_objParameter            = null;
    // variables set by the parameter objects
    double m_dErgaenzungRed                     = 0;
    double m_dErgaenzungNonRed                     = 0;
    double m_dMassShift                         = 0;

    ArrayList<CalculationDerivatisation> m_aDerivates         = null;    
    ArrayList<ResidueSpecial> m_aResidues    = null;    
    ArrayList<CalculationIon> m_aIons               = null;
    ArrayList<CalculationIon> m_aExchangeIons    = null;    
    ArrayList<CalculationFragment> m_aFragmentsRed            = null;
    ArrayList<CalculationFragment> m_aFragmentsNonRed        = null;    
    ArrayList<CalculationMolecule> m_aGain        = null; 
    ArrayList<CalculationMolecule> m_aLoss        = null;

    ArrayList<CalculationPeak> m_aPeakListFragmented   = new ArrayList<CalculationPeak>();
    ArrayList<CalculationPeak> m_aPeakListProfile      = new ArrayList<CalculationPeak>();
    ArrayList<CalculationPeak> m_aPeakListCalculation  = new ArrayList<CalculationPeak>();

    boolean[] m_bIons                             = null; // [0] not used
    boolean[] m_bFragments                         = null; // [0] not used
    boolean[] m_bIonExchange                     = null; // [0] not used

    // Annotation
    PeakAnnotation m_objAnnotation                = new PeakAnnotation();
    ArrayList<String> m_aGainIDs                = new ArrayList<String>();
    int m_iMinResidueCount                         = 0;

    private boolean m_bLowestDeviation            = false;
    private boolean m_bNegativeResidueMass         = false;
    private double m_dMaxPeakMass                 = 0;
    
    public void setStoreOnlyLowestDeviation(boolean a_b)
    {
        this.m_bLowestDeviation = a_b;    
    }
    
    public boolean getStoreOnlyLowestDeviation()
    {
        return this.m_bLowestDeviation;
    }
    
    /**
     * Tests the parameter set for the calculation
     * 
     * @param parameter
     * @throws ParameterException thrown if error in parameter
     */
    protected void testParameter() throws ParameterException
    {
        int t_iMax;
        Integer t_iNumber;
        this.m_dMassExchangeIon = this.m_objParameter.getExchangeIonMass();
        this.m_dNonReducingFragmentAbzug = this.m_objParameter.getNonReducingDifference();
        this.m_dErgaenzungRed = this.m_objParameter.getCompletionRed();
        this.m_dErgaenzungNonRed = this.m_objParameter.getCompletionNonRed();
        this.m_dMassShift = this.m_objParameter.getMassShift();
        this.m_iMaxAnnotationCount = this.m_objParameter.getMaxAnnotationPerPeak();
        // Derivatisation 
        this.m_aDerivates = this.m_objParameter.getDerivatisation();
        if ( this.m_aDerivates == null )
        {
            throw new ParameterException("Modification at the reducing end must be given.");
        }
        if ( this.m_aDerivates.size() == 0 )
        {
            throw new ParameterException("Modification at the reducing end must be given.");
        }
        // residues
        this.m_bNegativeResidueMass = false;
        CalculationMolecule t_objMolecule;
        ArrayList<String> t_aID = new ArrayList<String>();
        ArrayList<String> t_aResidueID = new ArrayList<String>();
        this.m_aResidues = new ArrayList<ResidueSpecial>();
        for (Iterator<CalculationMolecule> t_iterResidue = this.m_objParameter.getResidues().iterator(); t_iterResidue.hasNext();) 
        {
            t_objMolecule = t_iterResidue.next();
            if ( t_objMolecule.getMin() < 0 || t_objMolecule.getMax() <= 0 )
            {
                throw new ParameterException("Min and Max of residue " + t_objMolecule.getId() + " must be larger than 0.");
            }
            if ( t_objMolecule.getMin() > t_objMolecule.getMax() )
            {
                throw new ParameterException("Min of residue " + t_objMolecule.getId() + " must be larger than Max.");
            }
            if ( t_aResidueID.contains(t_objMolecule.getId()))
            {
                throw new ParameterException("Duplicated residue id : " + t_objMolecule.getId() + ".");
            }
            if ( t_objMolecule.getMass() < 0 )
            {
                this.m_bNegativeResidueMass = true;
            }
            t_aResidueID.add(t_objMolecule.getId());
            ResidueSpecial t_objRes = new ResidueSpecial(t_objMolecule);
            t_objRes.m_iCurrent = t_objRes.getMin();
            this.m_aResidues.add( t_objRes );
        }
        if ( this.m_aResidues.size() == 0 )
        {
            throw new ParameterException("At least one residue must be set.");
        }
        // sort residues by mass
        ComparatorMolecule t_objComparatorResidue = new ComparatorMolecule();
        Collections.sort( this.m_aResidues, t_objComparatorResidue );
        // ion
        CalculationIon t_objIon;
        t_aID.clear();
        for (Iterator<CalculationIon> t_iterIon = this.m_objParameter.getIons().iterator(); t_iterIon.hasNext();) 
        {
            t_objIon = t_iterIon.next();
            if ( t_objIon.getCharge() < 1 )
            {
                throw new ParameterException("Charge of ion " + t_objIon.getId() + "must be greater than 0.");
            }
            if ( t_aID.contains(t_objIon.getId()))
            {
                throw new ParameterException("Duplicated ion id : " + t_objIon.getId() + ".");
            }
            if ( t_objIon.getMass()< -1.5 )
            {
                throw new ParameterException("Mass of ion " + t_objIon.getId() + " must be greater than -1.5.");
            }
            t_aID.add(t_objIon.getId());
        }
        this.m_aIons = this.m_objParameter.getIons();
        if ( this.m_aIons.size() == 0 )
        {
            throw new ParameterException("At least one ion must be set.");
        }
        // ion exchange
        t_aID.clear();
        for (Iterator<CalculationIon> t_iterIon = this.m_objParameter.getIonExchangeIon().iterator(); t_iterIon.hasNext();) 
        {
            t_objIon = t_iterIon.next();
            if ( t_objIon.getCharge() < 1 )
            {
                throw new ParameterException("Charge of ion exchange ion " + t_objIon.getId() + "must be greater than 0.");
            }
            if ( t_aID.contains(t_objIon.getId()))
            {
                throw new ParameterException("Duplicated ion exchange ion id : " + t_objIon.getId() + ".");
            }
            if ( t_objIon.getMass()<= 0 )
            {
                throw new ParameterException("Mass of ion exchange ion " + t_objIon.getId() + " must be greater than 0.");
            }
            t_aID.add(t_objIon.getId());
        }
        this.m_aExchangeIons = this.m_objParameter.getIonExchangeIon();
        // Multifragmentation
        if ( this.m_objParameter.getSpectraType() != SpectraType.Profile )
        {
            t_iMax = this.m_objParameter.getMaxFragments();
            this.m_iMaxFragmentCount = t_iMax;
            if ( t_iMax <= 0 )
            {
                throw new ParameterException("Fragmentation level must be set (1-...).");
            }
            this.m_bFragments = new boolean[ t_iMax + 1 ];
            for (Iterator<Integer> t_iterFrags = this.m_objParameter.getMultiFragments().iterator(); t_iterFrags.hasNext();) 
            {
                t_iNumber = t_iterFrags.next();
                if ( t_iNumber > 0 )
                {
                    this.m_bFragments[t_iNumber] = true;
                }
                else
                {
                    throw new ParameterException("Number of fragmentation must be larger than 0.");
                }
            }
        }
        // ion state
        t_iMax = this.m_objParameter.getMaxCharges();
        if ( t_iMax <= 0 )
        {
            throw new ParameterException("Charge state must be set (1-...).");
        }
        this.m_iMaxChargeCount = t_iMax;
        this.m_bIons = new boolean[ t_iMax + 1 ];
        for (Iterator<Integer> t_iterIons = this.m_objParameter.getCharges().iterator(); t_iterIons.hasNext();) 
        {
            t_iNumber = t_iterIons.next();
            if ( t_iNumber > 0 )
            {
                this.m_bIons[t_iNumber] = true;
            }
            else
            {
                throw new ParameterException("Number of charges must be larger than 0.");
            }
        }
        // ion exchange state
        if ( this.m_aExchangeIons.size() != 0 )
        {
            t_iMax = this.m_objParameter.getMaxIonExchange();
            if ( t_iMax <= 0 )
            {
                throw new ParameterException("Number of ion exchanges must be largen than 0.");
            }
            this.m_iMaxExchangeCount = t_iMax;
            this.m_bIonExchange = new boolean[ t_iMax + 1 ];
            for (Iterator<Integer> t_iterIons = this.m_objParameter.getIonExchangeCount().iterator(); t_iterIons.hasNext();) 
            {
                t_iNumber = t_iterIons.next();
                if ( t_iNumber > 0 )
                {
                    this.m_bIonExchange[t_iNumber] = true;
                }
                else
                {
                    throw new ParameterException("Number of ion exchanges must be larger than 0.");
                }
            }
        }
        // gain
        t_aID.clear();
        for (Iterator<CalculationMolecule> t_iterGain = this.m_objParameter.getGainMolecules().iterator(); t_iterGain.hasNext();) 
        {
            t_objMolecule = t_iterGain.next();
            if ( t_objMolecule.getMin() < 0 || t_objMolecule.getMax() <= 0 )
            {
                throw new ParameterException("Min and Max of gain molecule " + t_objMolecule.getId() + " must be larger than 0.");
            }
            if ( t_objMolecule.getMin() > t_objMolecule.getMax() )
            {
                throw new ParameterException("Min of gain molecule " + t_objMolecule.getId() + " must be larger than Max.");
            }
            if ( t_objMolecule.getMass() <= 0 )
            {
                throw new ParameterException("Mass of gain molecule " + t_objMolecule.getId() + " must be larger than 0.");
            }
            if ( t_aID.contains(t_objMolecule.getId()))
            {
                throw new ParameterException("Duplicated gain molecule id : " + t_objMolecule.getId() + ".");
            }
            t_aID.add(t_objMolecule.getId());
        }
        this.m_aGain = this.m_objParameter.getGainMolecules();
        // loss
        t_aID.clear();
        for (Iterator<CalculationMolecule> t_iterLoss = this.m_objParameter.getLossMolecules().iterator(); t_iterLoss.hasNext();) 
        {
            t_objMolecule = t_iterLoss.next();
            if ( t_objMolecule.getMin() < 0 || t_objMolecule.getMax() <= 0 )
            {
                throw new ParameterException("Min and Max of loss molecule " + t_objMolecule.getId() + " must be larger than 0.");
            }
            if ( t_objMolecule.getMin() > t_objMolecule.getMax() )
            {
                throw new ParameterException("Min of loss molecule " + t_objMolecule.getId() + " must be larger than Max.");
            }
            if ( t_objMolecule.getMass() <= 0 )
            {
                throw new ParameterException("Mass of loss molecule " + t_objMolecule.getId() + " must be larger than 0.");
            }
            if ( t_aID.contains(t_objMolecule.getId()))
            {
                throw new ParameterException("Duplicated loss molecule id : " + t_objMolecule.getId() + ".");
            }
            t_aID.add(t_objMolecule.getId());
        }
        this.m_aLoss = this.m_objParameter.getLossMolecules();
        // Nonreducing fragment
        if ( this.m_objParameter.getSpectraType() != SpectraType.Profile )
        {
            CalculationFragment t_objFragment;
            t_aID.clear();
            for (Iterator<CalculationFragment> t_iterFrag = this.m_objParameter.getFragmentsNonRed().iterator(); t_iterFrag.hasNext();) 
            {
                t_objFragment = t_iterFrag.next();
                if ( t_aID.contains(t_objFragment.getId()))
                {
                    throw new ParameterException("Duplicated fragment id : " + t_objFragment.getId() + ".");
                }
                t_aID.add(t_objFragment.getId());
                /*if ( t_objFragment.getMass() <= 0 )
                {
                    throw new ParameterException("Mass of fragment : " + t_objFragment.getId() + " must be larger than 0.");
                }*/
                if ( t_objFragment.getResidueId() != null )
                {
                    if ( !t_aResidueID.contains( t_objFragment.getResidueId() ))
                    {
                        throw new ParameterException("Residue for fragment : " + t_objFragment.getId() + " is not part of the residue list.");
                    }
                }
            }
            this.m_aFragmentsNonRed = this.m_objParameter.getFragmentsNonRed();
            // reducing fragment
            t_aID.clear();
            for (Iterator<CalculationFragment> t_iterFrag = this.m_objParameter.getFragmentsRed().iterator(); t_iterFrag.hasNext();) 
            {
                t_objFragment = t_iterFrag.next();
                if ( t_aID.contains(t_objFragment.getId()))
                {
                    throw new ParameterException("Duplicated fragment id : " + t_objFragment.getId() + ".");
                }
                t_aID.add(t_objFragment.getId());
                /*if ( t_objFragment.getMass() <= 0 )
                {
                    throw new ParameterException("Mass of fragment : " + t_objFragment.getId() + " must be larger than 0.");
            }*/
                if ( t_objFragment.getResidueId() != null )
                {
                    if ( !t_aResidueID.contains( t_objFragment.getResidueId() ))
                    {
                        throw new ParameterException("Residue for fragment : " + t_objFragment.getId() + " is not part of the residue list.");
                    }
                }
            }
            this.m_aFragmentsRed = this.m_objParameter.getFragmentsRed();
            if ( this.m_aFragmentsNonRed.size() == 0 && this.m_aFragmentsRed.size() == 0 )
            {
                throw new ParameterException("No fragments given.");
            }
        }
        // accuracy
        if ( this.m_objParameter.getAccuracy() < 0 )
        {
            throw new ParameterException("Accuracy must be larger than 0.");
        }
        // create Peaklist
        this.m_aPeakListFragmented = new ArrayList<CalculationPeak>();
        this.m_aPeakListProfile = new ArrayList<CalculationPeak>();
        if ( this.m_objParameter.getSpectraType() == SpectraType.Profile )
        {
            if ( this.m_objParameter.getScan().getSubScan().size() > 0 )
            {
                throw new ParameterException("For spectrum type profile no sub scans are allowed.");
            }
            this.addScan(this.m_objParameter.getScan(),1);
        }
        else if ( this.m_objParameter.getSpectraType() == SpectraType.MS2 )
        {
            if ( this.m_objParameter.getScan().getPrecusorMass() == null )
            {
                throw new ParameterException("For spectrum type ms2 precursor mass has to be set.");
            }
            this.addScan(this.m_objParameter.getScan(),2);
        }
        else if ( this.m_objParameter.getSpectraType() == SpectraType.Fragmented )
        {
            this.addScan(this.m_objParameter.getScan(),3);
        }
        else if ( this.m_objParameter.getSpectraType() == SpectraType.MSxMS )
        {
            this.addScan(this.m_objParameter.getScan(),1);
        }
        ComparatorCalculationPeak t_objComparator = new ComparatorCalculationPeak();
        Collections.sort( this.m_aPeakListFragmented , t_objComparator );
        Collections.sort( this.m_aPeakListProfile , t_objComparator );
        if ( this.m_aPeakListFragmented.size() == 0 && this.m_aPeakListProfile.size() == 0 )
        {
            throw new ParameterException("At least one peak must be given.");
        }
        // calculate MinResidueMass;
        this.m_iMinResidueCount = 0;
        for (Iterator<ResidueSpecial> t_iterResidues = this.m_aResidues.iterator(); t_iterResidues.hasNext();) 
        {
            ResidueSpecial t_objResidue = t_iterResidues.next();
            this.m_dMinResidueMass += t_objResidue.getMin() * t_objResidue.getMass();
            t_objResidue.m_iCurrent = t_objResidue.getMin();
            this.m_iMinResidueCount += t_objResidue.getMin();
            t_objResidue.m_iFragment = 0;
        }
    }

    /**
     * tests the scan settings
     * 
     * @param a_objScan
     * @param a_iLevel
     */
    private void addScan(Scan a_objScan,int a_iLevel)
    {
        if ( this.m_objParameter.getAccuracyPpm() )
        {
            for (Iterator<CalculationPeak> t_iterPeaks = a_objScan.getPeaks().iterator(); t_iterPeaks.hasNext();) 
            {
                CalculationPeak t_objPeak = t_iterPeaks.next();
                t_objPeak.setAnnotation(new ArrayList<PeakAnnotation>());
                t_objPeak.calculateDeviationPpm( this.m_objParameter.getAccuracy() );
                if ( this.m_dMaxPeakMass < t_objPeak.maxMz() )
                {
                    this.m_dMaxPeakMass = t_objPeak.maxMz();
                }
                if ( a_iLevel == 1 )
                {
                    this.m_aPeakListProfile.add(t_objPeak);
                }
                else
                {
                    if ( a_iLevel == 2 )
                    {
                        this.m_aPeakListProfile.add(t_objPeak);
                        this.m_aPeakListFragmented.add(t_objPeak);
                    }
                    else
                    {
                        this.m_aPeakListFragmented.add(t_objPeak);
                    }
                }
            }
        }
        else
        {
            for (Iterator<CalculationPeak> t_iterPeaks = a_objScan.getPeaks().iterator(); t_iterPeaks.hasNext();) 
            {
                CalculationPeak t_objPeak = t_iterPeaks.next();
                t_objPeak.setAnnotation(new ArrayList<PeakAnnotation>());
                t_objPeak.calculateDeviationU( this.m_objParameter.getAccuracy() );
                if ( this.m_dMaxPeakMass < t_objPeak.maxMz() )
                {
                    this.m_dMaxPeakMass = t_objPeak.maxMz();
                }
                if ( a_iLevel == 1 )
                {
                    this.m_aPeakListProfile.add(t_objPeak);
                }
                else
                {
                    if ( a_iLevel == 2 && t_objPeak.getMz() == a_objScan.getPrecusorMass() )
                    {
                        this.m_aPeakListProfile.add(t_objPeak);
                    }
                    else
                    {
                        this.m_aPeakListFragmented.add(t_objPeak);
                    }
                }
            }
        }
        if ( a_objScan.getSubScan() != null )
        {
            for (Iterator<Scan> t_iterSubScan = a_objScan.getSubScan().iterator(); t_iterSubScan.hasNext();) 
            {
                this.addScan(t_iterSubScan.next(),a_iLevel+1);
            }
        }
    }

    /**
     * Do calculate based on a parameter object 
     * 
     * @param a_objParameter
     * @return
     * @throws ParameterException
     */
    public CalculationParameter calculate(CalculationParameter a_objParameter) throws ParameterException
    {
        // first save and test parameter values
        this.m_objParameter = a_objParameter;
        this.testParameter();
        this.m_objAnnotation = new PeakAnnotation();
        if ( this.m_aPeakListProfile.size() > 0 )
        {
            this.m_aPeakListCalculation = this.m_aPeakListProfile;
            this.calcDericatisation( this.m_dErgaenzungRed + this.m_dErgaenzungNonRed + this.m_dMassShift );
        }
        if ( this.m_aPeakListFragmented.size() > 0 )
        {
            this.m_aPeakListCalculation = this.m_aPeakListFragmented;
            this.calcFragmentReducing(this.m_dMassShift);
        }
        // post precession of ms2 stuff
        if ( this.m_objParameter.getSpectraType() == SpectraType.MS2 )
        {
            this.postProcessPreCursor( this.m_objParameter.getScan() );
        }
        else if ( this.m_objParameter.getSpectraType() == SpectraType.MSxMS )
        {
            for (Iterator<Scan> t_iterScans = this.m_objParameter.getScan().getSubScan().iterator(); t_iterScans.hasNext();) 
            {
                this.postProcessPreCursor( t_iterScans.next() );                
            }
        }
        return this.m_objParameter;
    }

    
    
    /**
     * @param scan
     */
    private void postProcessPreCursor(Scan a_objScan) 
    {    
        CalculationPeak t_objPeak;
        PeakAnnotation t_objAnnotation;
        HashMap<String,Object> t_aPreCursorAnnotation = new HashMap<String, Object>();
        // find precursor
        for (Iterator<CalculationPeak> t_iterPeak = a_objScan.getPeaks().iterator(); t_iterPeak.hasNext();) 
        {
            t_objPeak = t_iterPeak.next();
            if ( t_objPeak.getMz() == a_objScan.getPrecusorMass() )
            {
                // create list of all precursor annotations
                for (Iterator<PeakAnnotation> t_iterAnnotation = t_objPeak.getAnnotation().iterator(); t_iterAnnotation.hasNext();) 
                {
                    t_objAnnotation = t_iterAnnotation.next();
                    if ( t_objAnnotation.profileAnnotation() )
                    {
                        t_aPreCursorAnnotation.put(this.createCompositionString(t_objAnnotation),null);
                    }
                }                
            }
        }
        ArrayList<PeakAnnotation> t_aDelete;
        ArrayList<PeakAnnotation> t_aOrgininal;
        // find all other peaks with profile annotations
        for (Iterator<CalculationPeak> t_iterPeak = a_objScan.getPeaks().iterator(); t_iterPeak.hasNext();) 
        {
            t_objPeak = t_iterPeak.next();
            t_aDelete = new ArrayList<PeakAnnotation>();
            // create list of all precursor annotations
            for (Iterator<PeakAnnotation> t_iterAnnotation = t_objPeak.getAnnotation().iterator(); t_iterAnnotation.hasNext();) 
            {
                t_objAnnotation = t_iterAnnotation.next();
                if ( t_objAnnotation.profileAnnotation() )
                {
                    if ( !t_aPreCursorAnnotation.containsKey(this.createCompositionString(t_objAnnotation) ) )
                    {
                        // no? ==> delete
                        t_aDelete.add(t_objAnnotation);
                    }
                }
            }                
            // delete
            t_aOrgininal = t_objPeak.getAnnotation();
            for (Iterator<PeakAnnotation> t_iterAnno = t_aDelete.iterator(); t_iterAnno.hasNext();) 
            {
                t_aOrgininal.remove(t_iterAnno.next());                
            }
        }        
    }

    /**
     * @param annotation
     * @return
     */
    private String createCompositionString(PeakAnnotation a_objAnnotation) 
    {
        String t_strResult = "";
        AnnotationEntity t_objResidue;
        ArrayList<String> t_aString = new ArrayList<String>();
        for (Iterator<AnnotationEntity> t_iterComposition = a_objAnnotation.getResidues().iterator(); t_iterComposition.hasNext();) 
        {
            t_objResidue = t_iterComposition.next();
            t_aString.add( String.format("%d%s", t_objResidue.getNumber(),t_objResidue.getId()));
        }
        Collections.sort(t_aString);
        for (Iterator<String> t_iterStrings = t_aString.iterator(); t_iterStrings.hasNext();) 
        {
            t_strResult += t_iterStrings.next();
        }
        return t_strResult;
    }

    /**
     * Adds derivatisation
     * 
     * @param a_dMass mass
     */
    private void calcDericatisation(double a_dMass) 
    {
        for (Iterator<CalculationDerivatisation> t_iterDerivate = this.m_aDerivates.iterator(); t_iterDerivate.hasNext();) 
        {
            CalculationDerivatisation t_objDerivate = t_iterDerivate.next();
            this.m_objAnnotation.setDerivatisation(t_objDerivate.getId());
            this.calcIon( a_dMass + t_objDerivate.getMass(),0,0,new ArrayList<AnnotationEntity>());            
        }
        this.m_objAnnotation.setDerivatisation(null);
    }

    /**
     * Adds reducing fragments
     * 
     * @param a_dMass mass
     * @throws ParameterException
     */
    private void calcFragmentReducing(double a_dMass) throws ParameterException
    {
        ArrayList<AnnotationEntity> t_aAnnotation = new ArrayList<AnnotationEntity>();
        // without A,B,C
        this.calcFragmentNonReducing( a_dMass, 0 , 0, t_aAnnotation , 0);
        // with one A,B,C
        for (int t_iCounter = 0; t_iCounter < this.m_aFragmentsRed.size(); t_iCounter++) 
        {
            CalculationFragment t_objSetting = this.m_aFragmentsRed.get(t_iCounter);
            AnnotationEntity t_objAnnotation = new AnnotationEntity();
            t_objAnnotation.setId(t_objSetting.getId());
            t_objAnnotation.setNumber(1);
            t_aAnnotation.add(t_objAnnotation);
            if ( t_objSetting.getResidueId() != null )
            {
                ResidueSpecial t_objResidue = this.findResidue(t_objSetting.getResidueId());
                t_objResidue.m_iCurrent++;
                t_objResidue.m_iFragment++;
                // only A,B,C
                this.finishFragments( a_dMass + t_objSetting.getMass(),1,0,t_aAnnotation);
                // with X,Y,Z
                this.calcFragmentNonReducing( a_dMass + t_objSetting.getMass(), 0 , 1 , t_aAnnotation ,0);
                t_objResidue.m_iCurrent--;
                t_objResidue.m_iFragment--;
            }
            else
            {
                // only A,B,C
                this.finishFragments( a_dMass + t_objSetting.getMass(),1,0,t_aAnnotation);
                // with X,Y,Z
                this.calcFragmentNonReducing( a_dMass + t_objSetting.getMass(), 0 , 1 , t_aAnnotation ,0);
            }
            t_aAnnotation.remove(t_objAnnotation);
        }
    }

    /**
     * adds non reducing fragments
     * 
     * @param a_dMass mass
     * @param a_iPosition position in the list of fragments
     * @param a_iCount number of fragments
     * @param a_aAnnotion list of fragment annotations
     * @param a_iNonReducingCount number non reducing fragments
     * @throws ParameterException
     */
    private void calcFragmentNonReducing(double a_dMass, int a_iPosition, int a_iCount, ArrayList<AnnotationEntity> a_aAnnotion, int a_iNonReducingCount) throws ParameterException 
    {
        AnnotationEntity t_objAnnotation = new AnnotationEntity();  
        if ( a_iCount < this.m_iMaxFragmentCount && a_iPosition < this.m_aFragmentsNonRed.size() )
        {
            double t_dMass = a_dMass;
            int t_iNonRedFragmentCounter = a_iNonReducingCount;
            CalculationFragment t_objSetting = this.m_aFragmentsNonRed.get(a_iPosition);
            // without current
            this.calcFragmentNonReducing( t_dMass, a_iPosition+1,a_iCount,a_aAnnotion,t_iNonRedFragmentCounter);
            // with current
            a_aAnnotion.add(t_objAnnotation);
            t_objAnnotation.setId(t_objSetting.getId());
            if ( t_objSetting.getResidueId() == null )
            {
                for ( int t_iAnzahl = a_iCount+1; t_iAnzahl <= this.m_iMaxFragmentCount; t_iAnzahl++ )
                {
                    t_dMass += t_objSetting.getMass();
                    t_iNonRedFragmentCounter++;
                    t_objAnnotation.setNumber(t_iAnzahl-a_iCount);
                    // recursion
                    this.calcFragmentNonReducing( t_dMass, a_iPosition + 1,t_iAnzahl,a_aAnnotion,t_iNonRedFragmentCounter);    
                    // start calculation
                    this.finishFragments(t_dMass,t_iAnzahl,t_iNonRedFragmentCounter,a_aAnnotion);
                }
            }
            else
            {
                ResidueSpecial t_objResidue = this.findResidue(t_objSetting.getResidueId());
                int t_iStore = t_objResidue.m_iCurrent;
                int t_iStore2 = t_objResidue.m_iFragment;
                for ( int t_iAnzahl = a_iCount+1; t_iAnzahl <= this.m_iMaxFragmentCount; t_iAnzahl++ )
                {
                    t_dMass += t_objSetting.getMass();
                    t_iNonRedFragmentCounter++;
                    t_objResidue.m_iCurrent++;
                    t_objResidue.m_iFragment++;
                    t_objAnnotation.setNumber(t_iAnzahl-a_iCount);
                    // recursion
                    this.calcFragmentNonReducing( t_dMass, a_iPosition + 1,t_iAnzahl,a_aAnnotion,t_iNonRedFragmentCounter); 
                    // start calculation
                    this.finishFragments(t_dMass,t_iAnzahl,t_iNonRedFragmentCounter,a_aAnnotion);
                }
                t_objResidue.m_iCurrent = t_iStore;
                t_objResidue.m_iFragment = t_iStore2;
            }
            a_aAnnotion.remove(t_objAnnotation);
        }
    }

    /**
     * Finish fragment calculation
     * 
     * @param a_dMass mass
     * @param a_iFragmentCount number of fragments
     * @param a_iNonReducingFragmentCount number of non reducing fragments
     * @param a_aFragments
     */
    private void finishFragments(double a_dMass, int a_iFragmentCount, int a_iNonReducingFragmentCount, ArrayList<AnnotationEntity> a_aFragments) 
    {
        if ( a_iFragmentCount > 0 && a_iFragmentCount <= this.m_iMaxFragmentCount )
        {
            if ( this.m_bFragments[a_iFragmentCount] )
            {
                double t_dMass = a_dMass;
                if ( a_iFragmentCount == a_iNonReducingFragmentCount )
                {
                    // only X,Y,Z
                    t_dMass += this.m_dErgaenzungRed; 
                }
                else if ( a_iNonReducingFragmentCount == 0 ) 
                {
                    t_dMass += this.m_dErgaenzungNonRed;
                }
                if ( a_iNonReducingFragmentCount > 0 )
                {
                    t_dMass -= (a_iNonReducingFragmentCount - 1 ) * this.m_dNonReducingFragmentAbzug;
                }
                this.m_objAnnotation.setFragments(a_aFragments);
                if ( a_iFragmentCount == a_iNonReducingFragmentCount )
                {
                    this.calcDericatisation(t_dMass);
                }
                else
                {
                    this.calcIon(t_dMass,0,0,new ArrayList<AnnotationEntity>());
                }
                this.m_objAnnotation.setFragments(new ArrayList<AnnotationEntity>());
            }
        }
    }

    /**
     * Adds ions 
     * 
     * @param a_dMass mass
     * @param a_iPosition position in the list of ions
     * @param a_iCount numver of charges
     * @param a_aAnnotation list of ion annotations
     */
    private void calcIon(double a_dMass , int a_iPosition , int a_iCount, ArrayList<AnnotationEntity> a_aAnnotation) 
    {
        if ( a_iCount < this.m_iMaxChargeCount && a_iPosition < this.m_aIons.size() )
        {
            double t_dMass = a_dMass;
            CalculationIon t_objSetting = this.m_aIons.get(a_iPosition);
            // without current
            this.calcIon( t_dMass, a_iPosition+1,a_iCount,a_aAnnotation);
            // with current
            AnnotationEntity t_objAnnotation = new AnnotationEntity();
            t_objAnnotation.setId(t_objSetting.getId());
            a_aAnnotation.add(t_objAnnotation);
            int t_iAnzahl = a_iCount;
            int t_iCounter = 0;
            while ( t_iAnzahl <= this.m_iMaxChargeCount )
            {
                t_iCounter++;
                t_iAnzahl += t_objSetting.getCharge();
                t_dMass += t_objSetting.getMass();
                t_objAnnotation.setNumber(t_iCounter);
                // recursion
                this.calcIon( t_dMass, a_iPosition + 1,t_iAnzahl,a_aAnnotation);    
                // start calculation
                this.finishIons(t_dMass,t_iAnzahl,a_aAnnotation);
            }
            a_aAnnotation.remove(t_objAnnotation);
        }
    }

    /**
     * Finish ion annotation
     * 
     * @param a_dMass mass
     * @param a_iAnzahl number of charges
     * @param a_aAnnotation list of ion annotations
     */
    private void finishIons(double a_dMass, int a_iAnzahl, ArrayList<AnnotationEntity> a_aAnnotation) 
    {
        if ( a_iAnzahl > 0 && a_iAnzahl <= this.m_iMaxChargeCount )
        {
            if ( this.m_bIons[a_iAnzahl] )
            {
                this.m_dChargeCount = a_iAnzahl;
                this.m_objAnnotation.setIons(a_aAnnotation);
                // without ion exchange
                this.startCalcResidue(a_dMass + this.m_dMinResidueMass);
                // with gain ( + loss )
                this.calcGain(a_dMass + this.m_dMinResidueMass,0,new ArrayList<AnnotationEntity>(),new ArrayList<String>());
                // without gain but loss
                this.m_aGainIDs.clear();
                this.calcLoss(a_dMass + this.m_dMinResidueMass,0, new ArrayList<AnnotationEntity>());
                // with ion exchange
                this.calcIonExchange(a_dMass,0,0,new ArrayList<AnnotationEntity>());
            }
        }
        this.m_objAnnotation.setIons(new ArrayList<AnnotationEntity>());
    }

    /**
     * adds ion exchange
     * 
     * @param a_dMass mass
     * @param a_iPosition position in the list of ion exchanges
     * @param a_iCount number of ion exchanges
     * @param a_aAnnotation list of ion exchange annotations
     */
    private void calcIonExchange(double a_dMass, int a_iPosition, int a_iCount, ArrayList<AnnotationEntity> a_aAnnotation)
    {
        if ( a_iCount < this.m_iMaxExchangeCount && a_iPosition < this.m_aExchangeIons.size() )
        {
            double t_dMass = a_dMass;
            CalculationIon t_objSetting = this.m_aExchangeIons.get(a_iPosition);
            // without current
            this.calcIonExchange( t_dMass, a_iPosition+1,a_iCount,a_aAnnotation);
            // with current
            AnnotationEntity t_objAnnotation = new AnnotationEntity();
            t_objAnnotation.setId(t_objSetting.getId());
            a_aAnnotation.add(t_objAnnotation);
            for ( int t_iAnzahl = a_iCount+1; t_iAnzahl <= this.m_iMaxExchangeCount; t_iAnzahl++ )
            {
                t_dMass += t_objSetting.getMass();
                t_objAnnotation.setNumber( t_iAnzahl - a_iCount );
                // recursion
                this.calcIonExchange( t_dMass, a_iPosition + 1,t_iAnzahl,a_aAnnotation);    
                // start calculation
                this.finishIonExchange(t_dMass,t_iAnzahl,a_aAnnotation);
            }
            a_aAnnotation.remove(t_objAnnotation);
        }
    }

    /**
     * Finish ion exchange
     * 
     * @param a_dMass mass
     * @param a_iAnzahl number of ion exchanges
     * @param a_aAnnotation list of ion exchange annotations 
     */
    private void finishIonExchange(double a_dMass, int a_iAnzahl, ArrayList<AnnotationEntity> a_aAnnotation) 
    {
        if ( a_iAnzahl > 0 && a_iAnzahl <= this.m_iMaxExchangeCount )
        {
            if ( this.m_bIonExchange[a_iAnzahl] )
            {
                this.m_objAnnotation.setIonExchange(a_aAnnotation);
                // without gain/loos
                this.startCalcResidue(a_dMass - (a_iAnzahl * this.m_dMassExchangeIon) + this.m_dMinResidueMass);
                // with gain
                this.calcGain(a_dMass - (a_iAnzahl * this.m_dMassExchangeIon) + this.m_dMinResidueMass,0,new ArrayList<AnnotationEntity>(),new ArrayList<String>());
                // without gain only loss
                this.calcLoss(a_dMass - (a_iAnzahl * this.m_dMassExchangeIon) + this.m_dMinResidueMass,0,new ArrayList<AnnotationEntity>());
                this.m_objAnnotation.setIonExchange(new ArrayList<AnnotationEntity>());
            }
        }
    }

    /**
     * Adds gain of small molecules
     *  
     * @param a_dMass mass
     * @param a_iGainLossPos position in the list of small molecules
     * @param a_aAnnotation list of gain annotation 
     * @param a_aGainIDs list of the ids of gained molecules
     */
    private void calcGain(double a_dMass, int a_iGainLossPos,ArrayList<AnnotationEntity> a_aAnnotation,ArrayList<String> a_aGainIDs) 
    {
        if ( a_iGainLossPos < this.m_aGain.size() )
        {
            double t_dMass = a_dMass;
            CalculationMolecule t_objMolecule = this.m_aGain.get(a_iGainLossPos);
            // without the current one
            this.calcGain(t_dMass,a_iGainLossPos+1,a_aAnnotation,a_aGainIDs);
            // with the current one
            int t_iMax = t_objMolecule.getMax() + 1; 
            int t_iMin = t_objMolecule.getMin();
            if ( t_iMin == 0 )
            {
                t_iMin = 1;
            }
            AnnotationEntity t_objAnnotation = new AnnotationEntity();
            t_objAnnotation.setId(t_objMolecule.getId());
            a_aGainIDs.add(t_objMolecule.getId());
            a_aAnnotation.add(t_objAnnotation);
            for ( int t_iAnzahl = t_iMin ; t_iAnzahl < t_iMax ; t_iAnzahl++)
            {
                t_objAnnotation.setNumber(t_iAnzahl);
                t_dMass = a_dMass + ( t_iAnzahl * t_objMolecule.getMass() );
                this.finishGain(t_dMass,a_aAnnotation,a_aGainIDs);
                // recursion
                this.calcGain(t_dMass,a_iGainLossPos+1,a_aAnnotation,a_aGainIDs);
            }            
            a_aGainIDs.remove(t_objMolecule.getId());
            a_aAnnotation.remove(t_objAnnotation);
        }
    }

    /**
     * Finish gain of small molecules
     * 
     * @param a_dMass mass
     * @param a_aGain list of gain annotations 
     * @param a_aGainIDs list of the ids of gained molecules
     */
    private void finishGain(double a_dMass,ArrayList<AnnotationEntity> a_aGain,ArrayList<String> a_aGainIDs) 
    {
        if ( a_aGain.size() > 0 )
        {
            this.m_objAnnotation.setGain(a_aGain);
            // without loss
            this.startCalcResidue(a_dMass);
            // with loss
            this.m_aGainIDs = a_aGainIDs;
            this.calcLoss(a_dMass,0,new ArrayList<AnnotationEntity>());
            this.m_objAnnotation.setGain(new ArrayList<AnnotationEntity>());
            this.m_aGainIDs = new ArrayList<String>();
        }
    }

    /**
     * adds loss of small molecules
     * 
     * @param a_dMass mass
     * @param a_iGainLossPos position in the list of lost molecules
     * @param a_aAnnotation list of lost annotations 
     */
    private void calcLoss(double a_dMass, int a_iGainLossPos,ArrayList<AnnotationEntity> a_aAnnotation) 
    {
        if ( a_iGainLossPos < this.m_aLoss.size() )
        {
            double t_dMass = a_dMass;
            CalculationMolecule t_objMolecule = this.m_aLoss.get(a_iGainLossPos);
            // without the current one
            this.calcLoss(t_dMass,a_iGainLossPos+1,a_aAnnotation);
            if ( !this.m_aGainIDs.contains(t_objMolecule.getId()))
            {                
                // with the current one
                int t_iMax = t_objMolecule.getMax() + 1; 
                int t_iMin = t_objMolecule.getMin();
                if ( t_iMin == 0 )
                {
                    t_iMin = 1;
                }            
                AnnotationEntity t_objAnnotation = new AnnotationEntity();
                t_objAnnotation.setId(t_objMolecule.getId());
                a_aAnnotation.add(t_objAnnotation);
                for ( int t_iAnzahl = t_iMin ; t_iAnzahl < t_iMax ; t_iAnzahl++)
                {
                    t_objAnnotation.setNumber(t_iAnzahl);
                    t_dMass = a_dMass - ( t_iAnzahl * t_objMolecule.getMass() );
                    this.finishLoss(t_dMass,a_aAnnotation);
                    // recursion
                    this.calcLoss(t_dMass,a_iGainLossPos+1,a_aAnnotation);
                }            
                a_aAnnotation.remove(t_objAnnotation);
            }
        }
    }

    /**
     * Finish loss of small molecules
     * 
     * @param a_dMass mass
     * @param a_aLoss list of lost molecules annotation
     */
    private void finishLoss(double a_dMass,ArrayList<AnnotationEntity> a_aLoss) 
    {
        if ( a_aLoss.size() > 0 )
        {
            this.m_objAnnotation.setLoss(a_aLoss);
            this.startCalcResidue(a_dMass);
            this.m_objAnnotation.setLoss(new ArrayList<AnnotationEntity>());
        }
    }

    /**
     * Start residue calculation
     * 
     * @param a_dMass mass
     */
    private void startCalcResidue(double a_dMass)
    {
        if ( this.m_iMinResidueCount > 0 )
        {
            if ( this.m_bNegativeResidueMass )
            {
                this.finishResultWithNegativ(a_dMass,0,-1);
            }
            else
            {
                this.finishResult(a_dMass,0);
            }
        }
        this.calcResidue(a_dMass, 0, 0);
    }

    /**
     * Adds residues
     * 
     * @param a_dMass mass
     * @param a_iPosition position in the list of residues
     * @param a_iResultPos  position in the peaklist
     */
    private void calcResidue(double a_dMass, int a_iPosition, int a_iResultPos) 
    {
        if ( a_iPosition < this.m_aResidues.size() )
        {
            int t_iPos = a_iResultPos;
            double t_dMass = a_dMass;
            ResidueSpecial t_objResidue = this.m_aResidues.get(a_iPosition);
            int t_iMax = t_objResidue.getMax();
            int t_iStore = t_objResidue.m_iCurrent;
            // without this residue
            this.calcResidue(t_dMass,a_iPosition+1,t_iPos);
            // with this residue
            for (int t_iAnzahl = t_objResidue.m_iCurrent+1; t_iAnzahl <= t_iMax;t_iAnzahl++)
            {
                t_objResidue.m_iCurrent++;
                t_dMass += t_objResidue.getMass();
                // test
                if ( this.m_bNegativeResidueMass )
                {
                    t_iPos = this.finishResultWithNegativ(t_dMass,t_iPos,t_objResidue.getMass());    
                }
                else
                {
                    t_iPos = this.finishResult(t_dMass,t_iPos);
                }
                if ( t_iPos == -1 )
                {
                    // abbruch
                    t_iAnzahl = t_iMax+1;
                }
                else
                {
                    // recursion
                    this.calcResidue(t_dMass,a_iPosition+1,t_iPos);
                }
            }
            t_objResidue.m_iCurrent = t_iStore;
        }
    }

    /**
     * Test if calculated mass match with a peak and store annotation
     * 
     * @param a_dMass calculated mass
     * @param a_iPos position in the peaklist to start
     * 
     * @return position in the peaklist with the last peak smaller than this mass or -1 if end of list is reached
     */
    private int finishResult(double a_dMass, int a_iPos) 
    {
        double t_dMassCalculatet = a_dMass / this.m_dChargeCount;
        int t_iMinima = a_iPos;
        CalculationPeak t_objPeak;
        double t_dMinMass;
        double t_dMaxMass;
        int t_iPeakCount = this.m_aPeakListCalculation.size();

        for (int t_iCounter = t_iMinima; t_iCounter < t_iPeakCount; t_iCounter++ )
        {
            t_objPeak = this.m_aPeakListCalculation.get(t_iCounter);
            t_dMinMass = t_objPeak.minMz();
            if ( t_dMinMass > t_dMassCalculatet )
            {
                // minimal mass of the current residue is larger than the calcultated mass ==> stop criterion
                t_iCounter = t_iPeakCount;
            }
            else
            {
                // minimal mass of the current residue is smaller than the calculated mass
                t_dMaxMass = t_objPeak.maxMz();
                if ( t_dMaxMass < t_dMassCalculatet )
                {
                    // no need to search for this mass again
                    t_iMinima = t_iCounter+1;
                }
                else
                {
                    // found a result ==> store it if charges match
                    if ( t_objPeak.getCharge() != null )
                    {
                        if ( t_objPeak.getCharge() == ((int)this.m_dChargeCount) )
                        {
                            if ( this.m_iMaxAnnotationCount > 0 )
                            {
                                if ( this.m_iMaxAnnotationCount > t_objPeak.getAnnotationCount() )
                                {
                                    PeakAnnotation t_objAnnotation = this.createAnnotation(t_dMassCalculatet,t_objPeak.getMz());
                                    t_objPeak.addAnnotation(t_objAnnotation);
                                    t_objPeak.incrementAnnotationCount();
                                }
                                else
                                {
                                    if ( this.m_bLowestDeviation )
                                    {
                                        PeakAnnotation t_objAnnotation = this.createAnnotation(t_dMassCalculatet,t_objPeak.getMz());
                                        this.replaceAnnotation(t_objPeak,t_objAnnotation);
                                    }
                                    t_objPeak.incrementAnnotationCount();
                                }
                            }
                            else
                            {
                                PeakAnnotation t_objAnnotation = this.createAnnotation(t_dMassCalculatet,t_objPeak.getMz());
                                t_objPeak.addAnnotation(t_objAnnotation);
                                t_objPeak.incrementAnnotationCount();
                            }
                        }
                    }
                    else
                    {
                        if ( this.m_iMaxAnnotationCount > 0 )
                        {                        
                            if ( this.m_iMaxAnnotationCount > t_objPeak.getAnnotationCount() )
                            {
                                PeakAnnotation t_objAnnotation = this.createAnnotation(t_dMassCalculatet,t_objPeak.getMz());
                                t_objPeak.addAnnotation(t_objAnnotation);
                                t_objPeak.incrementAnnotationCount();
                            }
                            else
                            {
                                if ( this.m_bLowestDeviation )
                                {
                                    PeakAnnotation t_objAnnotation = this.createAnnotation(t_dMassCalculatet,t_objPeak.getMz());
                                    this.replaceAnnotation(t_objPeak,t_objAnnotation);
                                }
                                t_objPeak.incrementAnnotationCount();
                            }
                        }
                        else
                        {
                            PeakAnnotation t_objAnnotation = this.createAnnotation(t_dMassCalculatet,t_objPeak.getMz());
                            t_objPeak.addAnnotation(t_objAnnotation);
                            t_objPeak.incrementAnnotationCount();
                        }
                    }
                }
            }
        }
        if ( t_iMinima >= t_iPeakCount )
        {
            return -1;
        }
        return t_iMinima;
    }

    /**
     * Test if calculated mass match with a peak and store annotation (methode for calculation with negative mass residues)
     * 
     * @param a_dMass calculated mass
     * @param a_iPos position in the peaklist to start
     * 
     * @return position in the peaklist with the last peak smaller than this mass or -1 if end of list is reached
     */
    private int finishResultWithNegativ(double a_dMass, int a_iPos , double a_dLastResidueMass) 
    {
        double t_dMassCalculatet = a_dMass / this.m_dChargeCount;
        CalculationPeak t_objPeak;
        double t_dMinMass;
        double t_dMaxMass = 0;

        for (Iterator<CalculationPeak> t_iterPeak = this.m_aPeakListCalculation.iterator(); t_iterPeak.hasNext();) 
        {
            t_objPeak = t_iterPeak.next();
            t_dMinMass = t_objPeak.minMz();
            if ( t_dMinMass <= t_dMassCalculatet )
            {
                // minimal mass of the current residue is smaller than the calculated mass
                t_dMaxMass = t_objPeak.maxMz();
                if ( t_dMaxMass >= t_dMassCalculatet )
                {
                    // found a result ==> store it if charges match
                    if ( t_objPeak.getCharge() != null )
                    {
                        if ( t_objPeak.getCharge() == ((int)this.m_dChargeCount) )
                        {
                            if ( this.m_iMaxAnnotationCount > 0 )
                            {
                                if ( this.m_iMaxAnnotationCount > t_objPeak.getAnnotationCount() )
                                {
                                    PeakAnnotation t_objAnnotation = this.createAnnotation(t_dMassCalculatet,t_objPeak.getMz());
                                    t_objPeak.addAnnotation(t_objAnnotation);
                                    t_objPeak.incrementAnnotationCount();
                                }
                                else
                                {
                                    if ( this.m_bLowestDeviation )
                                    {
                                        PeakAnnotation t_objAnnotation = this.createAnnotation(t_dMassCalculatet,t_objPeak.getMz());
                                        this.replaceAnnotation(t_objPeak,t_objAnnotation);
                                    }
                                    t_objPeak.incrementAnnotationCount();
                                }
                            }
                            else
                            {
                                PeakAnnotation t_objAnnotation = this.createAnnotation(t_dMassCalculatet,t_objPeak.getMz());
                                t_objPeak.addAnnotation(t_objAnnotation);
                                t_objPeak.incrementAnnotationCount();
                            }
                        }
                    }
                    else
                    {
                        if ( this.m_iMaxAnnotationCount > 0 )
                        {                        
                            if ( this.m_iMaxAnnotationCount > t_objPeak.getAnnotationCount() )
                            {
                                PeakAnnotation t_objAnnotation = this.createAnnotation(t_dMassCalculatet,t_objPeak.getMz());
                                t_objPeak.addAnnotation(t_objAnnotation);
                                t_objPeak.incrementAnnotationCount();
                            }
                            else
                            {
                                if ( this.m_bLowestDeviation )
                                {
                                    PeakAnnotation t_objAnnotation = this.createAnnotation(t_dMassCalculatet,t_objPeak.getMz());
                                    this.replaceAnnotation(t_objPeak,t_objAnnotation);
                                }
                                t_objPeak.incrementAnnotationCount();
                            }
                        }
                        else
                        {
                            PeakAnnotation t_objAnnotation = this.createAnnotation(t_dMassCalculatet,t_objPeak.getMz());
                            t_objPeak.addAnnotation(t_objAnnotation);
                            t_objPeak.incrementAnnotationCount();
                        }
                    }
                }
            }
        }
        if ( this.m_dMaxPeakMass < t_dMassCalculatet && a_dLastResidueMass > 0 )
        {
            return -1;
        }
        return 0;
    }

    /**
     * @param peak
     * @param annotation
     */
    private void replaceAnnotation(CalculationPeak a_objPeak, PeakAnnotation a_objAnnotation) 
    {
        PeakAnnotation t_objMax = null;
        PeakAnnotation t_objCurrent = null;
        for (Iterator<PeakAnnotation> t_iterMax = a_objPeak.getAnnotation().iterator(); t_iterMax.hasNext();) 
        {
            t_objCurrent = t_iterMax.next();
            if ( t_objMax == null )
            {
                t_objMax = t_objCurrent;
            }
            else
            {
                if ( t_objMax.getDivValue() < t_objCurrent.getDivValue() )
                {
                    t_objMax = t_objCurrent;    
                }
            }
        }
        if ( t_objMax != null )
        {
            if ( t_objMax.getDivValue() > a_objAnnotation.getDivValue() )
            {
                a_objPeak.getAnnotation().remove(t_objMax);
                a_objPeak.addAnnotation(a_objAnnotation);
            }
        }
    }

    /**
     * Creates an annotation object 
     * 
     * @param a_dMass
     * @return
     */
    private PeakAnnotation createAnnotation(double a_dMassCalc,double a_dMassPeak) 
    {
        PeakAnnotation t_objAnnotation = new PeakAnnotation();
        ArrayList<AnnotationEntity> t_aAnnotations;
        ResidueSpecial t_objResidue;
        AnnotationEntity t_objEntity;
        // residue
        t_aAnnotations = new ArrayList<AnnotationEntity>();
        for (Iterator<ResidueSpecial> t_iterResidues = this.m_aResidues.iterator(); t_iterResidues.hasNext();) 
        {
            t_objResidue = t_iterResidues.next();
            if ( (t_objResidue.m_iCurrent - t_objResidue.m_iFragment) != 0 )
            {
                t_objEntity = new AnnotationEntity();
                t_objEntity.setId( t_objResidue.getId() );
                t_objEntity.setNumber(t_objResidue.m_iCurrent - t_objResidue.m_iFragment);
                t_aAnnotations.add(t_objEntity);
            }
        }    
        t_objAnnotation.setResidues(t_aAnnotations);
        // fragment
        t_aAnnotations = new ArrayList<AnnotationEntity>();
        for (Iterator<AnnotationEntity> t_iterAnno = this.m_objAnnotation.getFragments().iterator(); t_iterAnno.hasNext();) 
        {
            t_aAnnotations.add(t_iterAnno.next().copy());
        }
        t_objAnnotation.setFragments(t_aAnnotations);
        // ions
        t_aAnnotations = new ArrayList<AnnotationEntity>();
        for (Iterator<AnnotationEntity> t_iterAnno = this.m_objAnnotation.getIons().iterator(); t_iterAnno.hasNext();) 
        {
            t_aAnnotations.add(t_iterAnno.next().copy());
        }
        t_objAnnotation.setIons(t_aAnnotations);
        // ion exchange
        t_aAnnotations = new ArrayList<AnnotationEntity>();
        for (Iterator<AnnotationEntity> t_iterAnno = this.m_objAnnotation.getIonExchange().iterator(); t_iterAnno.hasNext();) 
        {
            t_aAnnotations.add(t_iterAnno.next().copy());
        }
        t_objAnnotation.setIonExchange(t_aAnnotations);
        // derivatisation
        t_objAnnotation.setDerivatisation( this.m_objAnnotation.getDerivatisation() );
        // mass
        t_objAnnotation.setMass(a_dMassCalc);
        double t_dValue = ( (a_dMassPeak - a_dMassCalc) * 1000000.0 ) / a_dMassPeak;
        if ( t_dValue < 0 )
        {
            t_objAnnotation.setDivValue( t_dValue * (-1) );
        }
        else
        {
            t_objAnnotation.setDivValue( t_dValue );
        }
        // gain
        t_aAnnotations = new ArrayList<AnnotationEntity>();
        for (Iterator<AnnotationEntity> t_iterAnno = this.m_objAnnotation.getGain().iterator(); t_iterAnno.hasNext();) 
        {
            t_aAnnotations.add(t_iterAnno.next().copy());
        }
        t_objAnnotation.setGain(t_aAnnotations);
        // loss
        t_aAnnotations = new ArrayList<AnnotationEntity>();
        for (Iterator<AnnotationEntity> t_iterAnno = this.m_objAnnotation.getLoss().iterator(); t_iterAnno.hasNext();) 
        {
            t_aAnnotations.add(t_iterAnno.next().copy());
        }
        t_objAnnotation.setLoss(t_aAnnotations);
        return t_objAnnotation;
    }

    /**
     * Gives the residue object for an id(name) of an residue
     * 
     * @param a_strResidue
     * @return
     * @throws ParameterException
     */
    private ResidueSpecial findResidue(String a_strResidue) throws ParameterException
    {
        for (Iterator<ResidueSpecial> t_iterResidue = this.m_aResidues.iterator(); t_iterResidue.hasNext();)
        {
            ResidueSpecial t_objRes = t_iterResidue.next();
            if ( t_objRes.getId().equals(a_strResidue) )
            {
                return t_objRes;
            }
        }
        throw new ParameterException("Residue " + a_strResidue + " for A/X fragment is missing.");
    }
}
