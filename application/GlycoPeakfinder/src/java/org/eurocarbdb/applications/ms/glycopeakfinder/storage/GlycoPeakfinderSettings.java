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
import java.util.Collections;
import java.util.Iterator;

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.ParameterException;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationDerivatisation;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationFragment;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationIon;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationMolecule;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationParameter;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationPeak;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Scan;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.SpectraType;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.util.MassValueStorage;
import org.eurocarbdb.applications.ms.glycopeakfinder.io.PeakListLoaderTXT;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.ComparatorPeak;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.DBInterface;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.ErrorTextEnglish;

/**
* @author rene
*
*/
public class GlycoPeakfinderSettings
{
    // initialisiert?
    private boolean m_bInitialize;
    // Grenzwerte
    private LimitValues m_objLimits = new LimitValues(); 
    // mono ; avg
    private String m_strMassType;
    // profile ; fragmented
    private String m_strSpectraType;
    // Peaklist
    private ArrayList<GPPeak> m_aPeaks = new ArrayList<GPPeak>(); 
    // Fehler Liste
    private ArrayList<String> m_aErrors = new ArrayList<String>();    
    // none , pme , pdme, pdac , pac
    private String m_strPerSubstitution;
    // none , red , ab , pa , aa , lipid , peptid, other
    private String m_strDerivatisation;
    // other modification mass
    private double m_dOtherModification;
    // Amino acid sequence
    private String m_strAsSequence;
    // Sphingosin
    private ArrayList<Compound> m_aSphingosin = new ArrayList<Compound>();
    // Fatty Acid
    private ArrayList<Compound> m_aFattyAcid = new ArrayList<Compound>();
    // Fragmenttypes A,B ...
    private ArrayList<Compound> m_aFragmentType = new ArrayList<Compound>();
    // chargeState 1,2 ...
    private ArrayList<Compound> m_aCharges = new ArrayList<Compound>();
    // ion exchange 1,2 ...
    private ArrayList<Compound> m_aIonExchangeCount = new ArrayList<Compound>();
    // multi fragments 1,2 ...
    private ArrayList<Compound> m_aMultiFragmentation = new ArrayList<Compound>();
    // ion exchange on/off
    private boolean m_bIonExchange;
    // other ion mass
    private double m_dOtherIonMass;
    // other ion on/off
    private boolean m_bOtherIon;
    // accuracy value
    private double m_dAccuracy;
    // accuracy type ppm,u
    private String m_strAccuracyType;
    // ions
    private ArrayList<Compound> m_aIons = new ArrayList<Compound>();
    // ions exchange
    private ArrayList<Compound> m_aIonExchangeIons = new ArrayList<Compound>();
    // ions on/off
    private boolean m_bIons;
    // Categorie and Residues
    private ArrayList<ResidueCategory> m_aResidues = new ArrayList<ResidueCategory>(); 
    // other residues
    private GPOtherResidue m_objOtherResidueOne = new GPOtherResidue(); 
    private GPOtherResidue m_objOtherResidueTwo = new GPOtherResidue();
    private GPOtherResidue m_objOtherResidueThree = new GPOtherResidue();
    // examples
    private ArrayList<Compound> m_aExamples = new ArrayList<Compound>(); 
    // mass shift
    private double m_dMassShift = 0;
    // other names
    private String m_strOtherModification = "other";
    private String m_strOtherIon = "other";
    // other ion exchange ion
    private double m_dOtherIonExchangeIonMass;
    private boolean m_bOtherIonExchangeIon;
    private String m_strOtherIonExchangeIonName = "other";
    // loss/gain
    private ArrayList<MassMolecule> m_aLossGainMolecules = new ArrayList<MassMolecule>(); 
    private GPMolecule m_objOtherLossGainMolecule = new GPMolecule();    
    // motifs
    private ArrayList<Compound> m_aMotifs = new ArrayList<Compound>();
    // precursor
    private double m_dPrecursor = -1; 
    // max annotations per peak
    private int m_iAnnotationsPerPeak = 25;

    public void setAnnotationsPerPeak(String a_iNumber)
    {
        try
        {
            this.m_iAnnotationsPerPeak = Integer.parseInt(a_iNumber);            
        } 
        catch (NumberFormatException e) 
        {
            this.m_aErrors.add( String.format( ErrorTextEnglish.MAX_ANNOTATION ) );                        
        }   
    }
    
    public String getAnnotationsPerPeak()
    {
        return String.format("%d",this.m_iAnnotationsPerPeak);
    }
    
    public void setAnnotationsPerPeakValue(int a_iNumber)
    {
        this.m_iAnnotationsPerPeak = a_iNumber;
    }
    
    public int getAnnotationsPerPeakValue()
    {
        return this.m_iAnnotationsPerPeak;
    }

    public void setPrecursor(String a_strValue)
    {
        String t_strMass = a_strValue.replace(',','.');
        if ( t_strMass.trim().length() == 0 )
        {
            this.m_dPrecursor = -1;
        }
        else
        {
            try
            {
                this.m_dPrecursor = Double.parseDouble(t_strMass);            
            } 
            catch (NumberFormatException e) 
            {
                this.m_dPrecursor = -1;                        
            }
        }
    }
    
    public String getPrecursor()
    {
        if ( this.m_dPrecursor != -1 )
        {
            return String.format("%f",this.m_dPrecursor);
        }
        else
        {
            return "";
        }
    }

    public void setMotifs(ArrayList<Compound> a_aMotifs)
    {
        this.m_aMotifs = a_aMotifs;
    }

    public ArrayList<Compound> getMotifs()
    {
        return this.m_aMotifs;
    }

    public void setOtherLossGainMolecule(GPMolecule a_objMolecule)
    {
        this.m_objOtherLossGainMolecule = a_objMolecule;
    }

    public GPMolecule getOtherLossGainMolecule()
    {
        return this.m_objOtherLossGainMolecule;
    }

    public void setLossGainMolecules(ArrayList<MassMolecule> a_aMolecules)
    {
        this.m_aLossGainMolecules = a_aMolecules;
    }

    public ArrayList<MassMolecule> getLossGainMolecules()
    {
        return this.m_aLossGainMolecules;
    }

    public void setOtherIonExchangeBool(String a_dSet)
    {
        if ( a_dSet.equalsIgnoreCase("true") )
        {
            this.m_bOtherIonExchangeIon = true;
        }
        else
        {
            this.m_bOtherIonExchangeIon = false;    
        }        
    }

    public String getOtherIonExchangeBool()
    {
        if ( this.m_bOtherIonExchangeIon )
        {
            return "true";
        }
        return "false";        
    }

    public void setOtherIonExchangeIonName(String a_strName)
    {
        this.m_strOtherIonExchangeIonName = a_strName;
    }

    public String getOtherIonExchangeIonName()
    {
        return this.m_strOtherIonExchangeIonName;
    }

    public String getOtherIonExchangeIonMass()
    {
        return String.format("%f",this.m_dOtherIonExchangeIonMass);
    }

    public void setOtherIonExchangeIonMass(String a_strValue)
    {
        String t_strMass = a_strValue.replace(',','.');
        try
        {
            this.m_dOtherIonExchangeIonMass = Double.parseDouble(t_strMass);            
        } 
        catch (NumberFormatException e) 
        {
            this.m_dOtherIonExchangeIonMass = 0;                        
        }   
    }

    public String getOtherModificationName()
    {
        return this.m_strOtherModification;
    }

    public void setOtherModificationName(String a_strName)
    {
        this.m_strOtherModification = a_strName;
    }

    public String getOtherIonName()
    {
        return this.m_strOtherIon;
    }

    public void setOtherIonName(String a_strName)
    {
        this.m_strOtherIon = a_strName;
    }

    public double getMassShiftValue()
    {
        return this.m_dMassShift;
    }

    public String getMassShift()
    {
        return String.format("%f",this.m_dMassShift);
    }

    public void setMassShift(String a_strValue)
    {
        String t_strMass = a_strValue.replace(',','.');
        try
        {
            this.m_dMassShift = Double.parseDouble(t_strMass);            
        } 
        catch (NumberFormatException e) 
        {
            this.m_aErrors.add( String.format( ErrorTextEnglish.MASS_SHIFT ) );                        
        }   
    }

    public void setExamples( ArrayList<Compound> a_aExamples )
    {
        this.m_aExamples = a_aExamples;
    }

    public ArrayList<Compound> getExamples()
    {
        return this.m_aExamples;
    }

    public GlycoPeakfinderSettings()
    {
        this.m_bInitialize = false;
        this.resetMassPage();
        this.resetResiduePage();
        this.resetModificationPage();
        this.resetIonPage();    
        this.resetErrors();
    }

    public void resetIonPage()
    {        
        // iterate over page an reset values
        for (Iterator<Compound> t_iterElements = this.m_aFragmentType.iterator(); t_iterElements.hasNext();)
        {
            Compound t_objElement = t_iterElements.next();
            t_objElement.setUsed(false);
        }
        for (Iterator<Compound> t_iterElements = this.m_aCharges.iterator(); t_iterElements.hasNext();)
        {
            Compound t_objElement = t_iterElements.next();
            t_objElement.setUsed(false);
        }
        for (Iterator<Compound> t_iterElements = this.m_aIonExchangeCount.iterator(); t_iterElements.hasNext();)
        {
            Compound t_objElement = t_iterElements.next();
            t_objElement.setUsed(false);
        }
        for (Iterator<Compound> t_iterElements = this.m_aIons.iterator(); t_iterElements.hasNext();)
        {
            Compound t_objElement = t_iterElements.next();
            t_objElement.setUsed(false);
        }
        for (Iterator<Compound> t_iterElements = this.m_aIonExchangeIons.iterator(); t_iterElements.hasNext();)
        {
            Compound t_objElement = t_iterElements.next();
            t_objElement.setUsed(false);
        }
        for (Iterator<Compound> t_iterElements = this.m_aMultiFragmentation.iterator(); t_iterElements.hasNext();) 
        {
            Compound t_objElement = t_iterElements.next();
            t_objElement.setUsed(false);
        }
        for (Iterator<MassMolecule> t_iterElements = this.m_aLossGainMolecules.iterator(); t_iterElements.hasNext();) 
        {
            MassMolecule t_objElement = t_iterElements.next();
            t_objElement.setGain(0);
            t_objElement.setLoss(0);
        }
        this.m_objOtherLossGainMolecule.setLoss(0);
        this.m_objOtherLossGainMolecule.setGain(0);
        this.m_objOtherLossGainMolecule.setMass(0);

        this.m_bIonExchange = false;
        this.m_dOtherIonMass = 0;
        this.m_bOtherIon = false;
        this.m_bIons = false;
        this.m_strOtherIon = "other";
        this.m_bOtherIonExchangeIon = false;
        this.m_dOtherIonExchangeIonMass = 0;
        this.m_strOtherIonExchangeIonName = "other";
    }

    public void resetModificationPage()
    {        
        this.m_strDerivatisation = "none";
        this.m_strPerSubstitution = "none";
        this.m_strAsSequence = "";
        this.m_dOtherModification = 0;
        this.m_strOtherModification = "other";
    }

    public void resetResiduePage()
    {            
        ResidueCategory t_objCategorie;
        ArrayList<MassResidue> t_aResidues;
        MassResidue t_objResidue;
        for (Iterator<ResidueCategory> t_iterCategorie = this.m_aResidues.iterator(); t_iterCategorie.hasNext();) 
        {
            t_objCategorie = t_iterCategorie.next();
            t_aResidues = t_objCategorie.getResidues();
            for (Iterator<MassResidue> t_iterResidues = t_aResidues.iterator(); t_iterResidues.hasNext();) 
            {
                t_objResidue = t_iterResidues.next();
                t_objResidue.setUseAX(false);
                t_objResidue.setUseE(false);
                t_objResidue.setUseF(false);
                t_objResidue.setUseG(false);
                t_objResidue.setUseH(false);
                t_objResidue.setMin(0);
                t_objResidue.setMax(0);
            }
        }
        this.m_objOtherResidueOne.setName("OR1");
        this.m_objOtherResidueOne.setMin(0);
        this.m_objOtherResidueOne.setMax(0);
        this.m_objOtherResidueOne.setMass(0);
        this.m_objOtherResidueTwo.setName("OR2");
        this.m_objOtherResidueTwo.setMin(0);
        this.m_objOtherResidueTwo.setMax(0);
        this.m_objOtherResidueTwo.setMass(0);
        this.m_objOtherResidueThree.setName("OR3");
        this.m_objOtherResidueThree.setMin(0);
        this.m_objOtherResidueThree.setMax(0);
        this.m_objOtherResidueThree.setMass(0);
    }

    public void resetResidueMinMax()
    {            
        ResidueCategory t_objCategorie;
        ArrayList<MassResidue> t_aResidues;
        MassResidue t_objResidue;
        for (Iterator<ResidueCategory> t_iterCategorie = this.m_aResidues.iterator(); t_iterCategorie.hasNext();) 
        {
            t_objCategorie = t_iterCategorie.next();
            t_aResidues = t_objCategorie.getResidues();
            for (Iterator<MassResidue> t_iterResidues = t_aResidues.iterator(); t_iterResidues.hasNext();) 
            {
                t_objResidue = t_iterResidues.next();
                t_objResidue.setMin(0);
                t_objResidue.setMax(0);
            }
        }
        this.m_objOtherResidueOne.setName("OR1");
        this.m_objOtherResidueOne.setMin(0);
        this.m_objOtherResidueOne.setMax(0);
        this.m_objOtherResidueOne.setMass(0);
        this.m_objOtherResidueTwo.setName("OR2");
        this.m_objOtherResidueTwo.setMin(0);
        this.m_objOtherResidueTwo.setMax(0);
        this.m_objOtherResidueTwo.setMass(0);
        this.m_objOtherResidueThree.setName("OR3");
        this.m_objOtherResidueThree.setMin(0);
        this.m_objOtherResidueThree.setMax(0);
        this.m_objOtherResidueThree.setMass(0);
    }

    public void resetMassPage()
    {
        this.m_strAccuracyType = "ppm";
        this.m_dAccuracy = 100;
        this.m_strMassType = "mono";
        this.m_strSpectraType = "profile";
        this.m_aPeaks.clear();
    }

    public void resetErrors()
    {
        this.m_aErrors.clear();
    }

    public void setErrorList( ArrayList<String> a_aErrors)
    {
        this.m_aErrors = a_aErrors;
    }

    public ArrayList<String> getErrorList()
    {
        return this.m_aErrors;
    }

    public void setLimits( LimitValues a_objLimits)
    {
        this.m_objLimits = a_objLimits;
    }

    public LimitValues getLimits()
    {
        return this.m_objLimits;
    }

    public void setPeaks(ArrayList<GPPeak> a_aPeaks)
    {
        this.m_aPeaks = a_aPeaks;
    }

    public ArrayList<GPPeak> getPeaks()
    {
        return this.m_aPeaks;
    }

    public void setMassType(String a_strType)
    {
        this.m_strMassType = a_strType;
        for (Iterator<ResidueCategory> t_iterCategorie = this.m_aResidues.iterator(); t_iterCategorie.hasNext();) 
        {
            ResidueCategory t_objCategorie = t_iterCategorie.next();
            for (Iterator<MassResidue> t_iterResidue = t_objCategorie.getResidues().iterator(); t_iterResidue.hasNext();) 
            {
                MassResidue t_objResidue = t_iterResidue.next();
                t_objResidue.setMassType(this.m_strMassType);
            }
        }
    }

    public String getMassType()
    {
        return this.m_strMassType;
    }

    public void setSpectraType(String a_strType)
    {
        this.m_strSpectraType = a_strType;
    }

    public String getSpectraType()
    {
        return this.m_strSpectraType;
    }

    public void setPeakList(String a_strPeaklist)
    {
        GPPeak t_objPeak;
        String t_strLine = "";
        String[] t_strNumbers = null;
        PeakListLoaderTXT t_objLoader = new PeakListLoaderTXT();
        this.m_aPeaks.clear();
        // replace , with .
        String t_strPeaklist = a_strPeaklist.replace(',','.');
        // split lines by \n
        String[] a_strLines = t_strPeaklist.split("\n");
        for (int t_iCounter = 0; t_iCounter < a_strLines.length; t_iCounter++) 
        {
            t_strLine = a_strLines[t_iCounter].trim();
            if (  t_strLine.length() > 0 )
            {
                try 
                {
                    t_objPeak = t_objLoader.parseLine(t_strLine);
                    if ( t_objPeak != null )
                    {
                        this.m_aPeaks.add( t_objPeak );
                    }
                } 
                catch (NumberFormatException e) 
                {
                    this.m_aErrors.add( String.format( ErrorTextEnglish.PEAKLIST_SYMBOL , t_strNumbers[0]) );
                }
            }            
        }
        this.sortPeaklist();
    }

    public void sortPeaklist()
    {
        // sorting the peaklist
        ComparatorPeak t_objComparator = new ComparatorPeak();
        Collections.sort( this.m_aPeaks , t_objComparator );
    }

    public String getPeakList()
    {
        String t_strResult = "";
        int t_iCount = this.m_aPeaks.size();
        GPPeak t_objPeak;
        for (int t_iCounter = 0; t_iCounter < t_iCount; t_iCounter++) 
        {
            t_objPeak = this.m_aPeaks.get(t_iCounter);
            if ( t_objPeak.getCharge() == null )
            {
                t_strResult += String.format("%f %f\n",t_objPeak.getMZ(),t_objPeak.getIntensity());
            }
            else
            {
                t_strResult += String.format("%f %f %d\n",t_objPeak.getMZ(),t_objPeak.getIntensity(),t_objPeak.getCharge());
            }
        }
        return t_strResult;
    }

    /**
     * 
     */
    public void validateMassPage() 
    {
        int t_iCount = this.m_aPeaks.size();
        if ( t_iCount < 1)
        {
            this.m_aErrors.add( String.format( ErrorTextEnglish.EMPTY_MASS));
        }
        else
        {
            if ( t_iCount >= this.m_objLimits.getMaxPeakCount() )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.MAX_PEAK , this.m_objLimits.getMaxPeakCount() , t_iCount ));
            }
            GPPeak t_objPeak;
            for (Iterator<GPPeak> t_iterPeak = this.m_aPeaks.iterator(); t_iterPeak.hasNext();) 
            {
                t_objPeak = t_iterPeak.next();
                if ( t_objPeak.getMZ() > this.m_objLimits.getMaxMZ() )
                {
                    this.m_aErrors.add( String.format( ErrorTextEnglish.MAX_MASS , this.m_objLimits.getMaxMZ() , this.m_aPeaks.get(t_iCount-1).getMZ() ));
                }    
                if ( t_objPeak.getCharge() != null )
                {
                    if ( t_objPeak.getCharge() < 1 || t_objPeak.getCharge() > 4 )
                    {
                        this.m_aErrors.add( ErrorTextEnglish.MAX_CHARGE );                        
                    }
                }
            }
        }
        if ( this.m_dPrecursor != -1 )
        {
            GPPeak t_objPeak;
            boolean t_bFound = false;
            for (Iterator<GPPeak> t_iterPeak = this.m_aPeaks.iterator(); t_iterPeak.hasNext();) 
            {
                t_objPeak = t_iterPeak.next();
                if ( t_objPeak.getMZ() == this.m_dPrecursor )
                {
                    t_bFound = true;
                }
            }
            if ( !t_bFound )
            {
                this.m_aErrors.add(ErrorTextEnglish.PRECURSOR_PEAKLIST);
            }
        }
        if ( this.m_strSpectraType.equals("ms2") && this.m_dPrecursor < 1 )
        {
            this.m_aErrors.add(ErrorTextEnglish.MISSING_PRECUSROR);
        }
        // accuracy
        if ( this.m_dAccuracy <= 0 )
        {
            this.m_aErrors.add( String.format( ErrorTextEnglish.ACCURACY_NEGATIV) );
        }
        if ( this.m_strAccuracyType.equalsIgnoreCase("ppm") )
        {
            if ( this.m_dAccuracy > this.m_objLimits.getMaxAccuracyPPM() )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.ACCURACY_PPM , this.m_objLimits.getMaxAccuracyPPM() , this.m_dAccuracy )); 
            }
        }
        else
        {
            if ( this.m_dAccuracy > this.m_objLimits.getMaxAccuracyU() )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.ACCURACY_U , this.m_objLimits.getMaxAccuracyU() , this.m_dAccuracy )); 
            }            
        }    
        if ( this.m_iAnnotationsPerPeak < 1 || this.m_iAnnotationsPerPeak > this.m_objLimits.getMaxAnnotationPerPeak() )
        {
            this.m_aErrors.add( String.format( ErrorTextEnglish.MAX_ANNOTATION_RANGE , this.m_objLimits.getMaxAnnotationPerPeak()));        
        }
    }

    public void setInitialized(boolean a_bInit)
    {
        this.m_bInitialize = a_bInit;
    }

    public boolean getInitialized()
    {
        return this.m_bInitialize;
    }

    public String getPerSubstitution()
    {
        return this.m_strPerSubstitution;
    }

    public void setPerSubstitution(String a_strSub)
    {
        this.m_strPerSubstitution = a_strSub;
        for (Iterator<ResidueCategory> t_iterCategorie = this.m_aResidues.iterator(); t_iterCategorie.hasNext();) 
        {
            ResidueCategory t_objCategorie = t_iterCategorie.next();
            for (Iterator<MassResidue> t_iterResidue = t_objCategorie.getResidues().iterator(); t_iterResidue.hasNext();) 
            {
                MassResidue t_objResidue = t_iterResidue.next();
                t_objResidue.setPersubstitution(this.m_strPerSubstitution);
            }
        }
    }

    public void setDerivatisation(String a_strDerivat)
    {
        this.m_strDerivatisation = a_strDerivat;
    }

    public String getDerivatisation()
    {
        return this.m_strDerivatisation;
    }

    public String getAsSequence()
    {
        return this.m_strAsSequence;
    }

    public void setAsSequence(String a_strSequence)
    {
        // TODO: Validationchecks
        this.m_strAsSequence = a_strSequence;
    }

    public String getOtherModMass()
    {
        return String.format("%f",this.m_dOtherModification);
    }

    public void setOtherModMass(String a_strMass)
    {
        String t_strMass = a_strMass.replace(',','.');
        try
        {
            this.m_dOtherModification = Double.parseDouble(t_strMass);
            if ( this.m_dOtherModification < 0 )
            {
                this.m_dOtherModification = 0;
                this.m_aErrors.add( String.format( ErrorTextEnglish.MODI_OTHER_MASS) );
            }
        } 
        catch (NumberFormatException e) 
        {
            this.m_aErrors.add( String.format( ErrorTextEnglish.MODI_OTHER_MASS) );                        
        }    
    }

    public void setSphingosinList(ArrayList<Compound> a_aList)
    {
        this.m_aSphingosin = a_aList;
    }

    public ArrayList<Compound> getSphingosinList()
    {
        return this.m_aSphingosin;
    }

    public void setFattyAcidList(ArrayList<Compound> a_aList)
    {
        this.m_aFattyAcid = a_aList;
    }

    public ArrayList<Compound> getFattyAcidList()
    {
        return this.m_aFattyAcid;
    }

    public void setSphingosin(String a_strValue)
    {
        if ( !a_strValue.equalsIgnoreCase("") )
        {
            for (Iterator<Compound> t_iterCompound = this.m_aSphingosin.iterator(); t_iterCompound.hasNext();) 
            {
                Compound t_objElement = t_iterCompound.next();
                if ( a_strValue.equalsIgnoreCase(t_objElement.getId()))
                {
                    t_objElement.setUsed(true);
                }
                else
                {
                    t_objElement.setUsed(false);
                }
            }
        }
    }

    public String getSphingosin()
    {
        return "";
    }

    public void setFattyAcid(String a_strValue)
    {
        if ( !a_strValue.equalsIgnoreCase("") )
        {
            for (Iterator<Compound> t_iterCompound = this.m_aFattyAcid.iterator(); t_iterCompound.hasNext();) 
            {
                Compound t_objElement = t_iterCompound.next();
                if ( a_strValue.equalsIgnoreCase(t_objElement.getId()))
                {
                    t_objElement.setUsed(true);
                }
                else
                {
                    t_objElement.setUsed(false);
                }
            }

        }
    }

    public String getFattyAcid()
    {
        return "";
    }

    /**
     * 
     */
    public void validateModificationPage() 
    {
        if ( this.m_strDerivatisation.equalsIgnoreCase("lipid"))
        {
            // there must be a sphingosin and a fatty acid selected
            boolean t_bSelect = false;
            for (Iterator<Compound> t_iterCompound = this.m_aSphingosin.iterator(); t_iterCompound.hasNext();) 
            {
                Compound t_objElement = t_iterCompound.next();
                if ( t_objElement.getUsed() )
                {
                    t_bSelect = true;
                }
            }
            if ( !t_bSelect )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.LIPID_SPHINGO) );
            }
            t_bSelect = false;
            for (Iterator<Compound> t_iterCompound = this.m_aFattyAcid.iterator(); t_iterCompound.hasNext();) 
            {
                Compound t_objElement = t_iterCompound.next();
                if ( t_objElement.getUsed() )
                {
                    t_bSelect = true;
                }
            }
            if ( !t_bSelect )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.LIPID_FATTY_ACID) );
            }
        }
        if ( this.m_strDerivatisation.equalsIgnoreCase("peptid"))
        {
            if ( this.m_strAsSequence.equalsIgnoreCase("") )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.PEPTID_EMPTY) );
            }
        }
        if ( this.m_strDerivatisation.equalsIgnoreCase("other"))
        {
            if ( this.m_dOtherModification < 0 )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.MODI_OTHER_MASS) );
            }
            if ( this.m_strOtherModification.trim().equalsIgnoreCase("") )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.MODI_OTHER_NAME) );
            }
        }
        if ( this.m_strDerivatisation.equalsIgnoreCase("peptid") || this.m_strDerivatisation.equalsIgnoreCase("lipid") )
        {
            if ( !this.m_strPerSubstitution.equalsIgnoreCase("none") )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.NO_PER_ALLOWED) );
            }
        }
    }

    /**
     * 
     */
    public void validateIonPage() 
    {
        boolean t_bTest;
        // fragmenation type ... only by non profiling
        if ( !this.m_strSpectraType.equalsIgnoreCase("profile") )
        {
            t_bTest = false;
            for (Iterator<Compound> t_iterElement = this.m_aFragmentType.iterator(); t_iterElement.hasNext();)
            {
                Compound t_objElement = t_iterElement.next();
                if (t_objElement.getUsed())
                {
                    t_bTest = true;
                }
            }
            if ( !t_bTest )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.NO_FRAGMENTTYPE) );
            }
        }
        // multifragmentation
        if ( !this.m_strSpectraType.equalsIgnoreCase("profile") )
        {
            t_bTest = false;
            for (Iterator<Compound> t_iterElement = this.m_aMultiFragmentation.iterator(); t_iterElement.hasNext();)
            {
                Compound t_objElement = t_iterElement.next();
                if (t_objElement.getUsed())
                {
                    t_bTest = true;
                }
            }
            if ( !t_bTest )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.NO_FRAGMENT_NUMBER) );
            }
        }
        // chargestate
        t_bTest = false;
        for (Iterator<Compound> t_iterElement = this.m_aCharges.iterator(); t_iterElement.hasNext();)
        {
            Compound t_objElement = t_iterElement.next();
            if (t_objElement.getUsed())
            {
                t_bTest = true;
            }
        }
        if ( !t_bTest )
        {
            this.m_aErrors.add( String.format( ErrorTextEnglish.NO_CHARGE) );
        }
        // ions
        if ( this.m_bIons )
        {
            boolean t_bPos = false;
            boolean t_bNeg = false;
            for (Iterator<Compound> t_iterElement = this.m_aIons.iterator(); t_iterElement.hasNext();) 
            {
                Compound t_objElement = t_iterElement.next();
                if ( t_objElement.getUsed() )
                {
                    if ( t_objElement.getName().equalsIgnoreCase("e") )
                    {
                        t_bNeg = true;
                    }
                    else
                    {
                        t_bPos = true;
                    }
                }
            }
            if ( this.m_bOtherIon )
            {
                if ( t_bNeg )
                {
                    this.m_aErrors.add( String.format( ErrorTextEnglish.POS_NEG_IONS) );
                }
                if ( this.m_dOtherIonMass <= 0 )
                {
                    this.m_aErrors.add( String.format( ErrorTextEnglish.ION_OTHER_MASS) );
                }
                if ( this.m_strOtherIon.trim().equalsIgnoreCase("") )
                {
                    this.m_aErrors.add( String.format( ErrorTextEnglish.ION_OTHER_NAME) );
                }
            }
            else
            {
                if ( t_bNeg && t_bPos )
                {
                    this.m_aErrors.add( String.format( ErrorTextEnglish.POS_NEG_IONS) );
                }
            }            
        }
        else
        {
            if ( this.m_bOtherIon )
            {
                if ( this.m_dOtherIonMass <= 0 )
                {
                    this.m_aErrors.add( String.format( ErrorTextEnglish.ION_OTHER_MASS) );
                }
            }
            else
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.ION_NO_SELECT) );
            }
        }
        // ion exchange
        if ( this.m_bIonExchange || this.m_bOtherIonExchangeIon )
        {
            t_bTest = false;
            for (Iterator<Compound> t_iterElement = this.m_aIonExchangeCount.iterator(); t_iterElement.hasNext();)
            {
                Compound t_objElement = t_iterElement.next();
                if (t_objElement.getUsed())
                {
                    t_bTest = true;
                }
            }
            if ( !t_bTest )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.NO_EXCHANGE) );
            }
            // ions
            if ( this.m_bIonExchange )
            {
                t_bTest = false;
                for (Iterator<Compound> t_iterElement = this.m_aIonExchangeIons.iterator(); t_iterElement.hasNext();) 
                {
                    Compound t_objElement = t_iterElement.next();
                    if ( t_objElement.getUsed() )
                    {
                        t_bTest = true;
                    }
                }
                if ( !t_bTest )
                {
                    this.m_aErrors.add( ErrorTextEnglish.NO_EXCHANGE_ION );
                }
            }
            if ( this.m_bOtherIonExchangeIon )
            {
                if ( this.m_strOtherIonExchangeIonName.trim().equals("") )
                {
                    this.m_aErrors.add( ErrorTextEnglish.NO_OTHER_EXCHANGE_ION_NAME );
                }
                if ( this.m_dOtherIonExchangeIonMass <= 0 )
                {
                    this.m_aErrors.add( ErrorTextEnglish.NO_OTHER_EXCHANGE_ION_MASS );
                }
            }
        }
        // molecules
        for (Iterator<MassMolecule> t_iterMolecules = this.m_aLossGainMolecules.iterator(); t_iterMolecules.hasNext();) 
        {
            MassMolecule t_objElement = t_iterMolecules.next();
            if ( t_objElement.getGain() < 0 )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.MOLECULES_GAIN , t_objElement.getName() ) );
            }
            if ( t_objElement.getLoss() < 0 )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.MOLECULES_LOSS_MIN , t_objElement.getName() ) );
            }
            if ( t_objElement.getLoss() > this.m_objLimits.getMaxLossNumber() )
            {
                this.m_aErrors.add( String.format( ErrorTextEnglish.MOLECULES_LOSS_MAX , t_objElement.getName() , this.m_objLimits.getMaxLossNumber() , t_objElement.getLoss() ) );
            }
        }
        if ( this.m_objOtherLossGainMolecule.getGain() < 0 )
        {
            this.m_aErrors.add( String.format( ErrorTextEnglish.MOLECULES_GAIN , "other Molecule" ) );
        }
        if ( this.m_objOtherLossGainMolecule.getLoss() < 0 )
        {
            this.m_aErrors.add( String.format( ErrorTextEnglish.MOLECULES_LOSS_MIN , "other Molecule" ) );
        }
        if ( this.m_objOtherLossGainMolecule.getLoss() > this.m_objLimits.getMaxLossNumber() )
        {
            this.m_aErrors.add( String.format( ErrorTextEnglish.MOLECULES_LOSS_MAX , "other Molecule" , this.m_objLimits.getMaxLossNumber() , this.m_objOtherLossGainMolecule.getLoss() ));
        }
        if ( this.m_objOtherLossGainMolecule.getLoss() > 0 || this.m_objOtherLossGainMolecule.getGain() > 0 )
        {
            if ( this.m_objOtherLossGainMolecule.getName().trim().equals("") )
            {
                this.m_aErrors.add( ErrorTextEnglish.MOLECULES_OTHER_NAME );
            }
            if ( this.m_objOtherLossGainMolecule.getMass() <= 0 )
            {
                this.m_aErrors.add( ErrorTextEnglish.MOLECULES_OTHER_MASS );
            }
        }
        this.m_objOtherLossGainMolecule.setAbbr(this.m_objOtherLossGainMolecule.getName());
    }

    public void setIonList(ArrayList<Compound> a_aValues)
    {
        this.m_aIons = a_aValues;
    }

    public ArrayList<Compound> getIonList()
    {
        return this.m_aIons;
    }

    public String[] getIonExchangeIon()
    {
        return null;
    }

    public void setIonExchangeIon(String[] a_aIons)
    {
        if ( a_aIons != null )
        {
            for (int t_iCounter = 0; t_iCounter < a_aIons.length; t_iCounter++) 
            {
                for (Iterator<Compound> t_iterElement = this.m_aIonExchangeIons.iterator(); t_iterElement.hasNext();) 
                {
                    Compound t_objElement = t_iterElement.next();
                    if ( t_objElement.getName().equalsIgnoreCase(a_aIons[t_iCounter]))
                    {
                        t_objElement.setUsed(true);
                    }
                }
            }
        }
    }

    public void setIonExchangeIonList(ArrayList<Compound> a_aValues)
    {
        this.m_aIonExchangeIons = a_aValues;
    }

    public ArrayList<Compound> getIonExchangeIonList()
    {
        return this.m_aIonExchangeIons;
    }

    public void setIonExchangeCountList(ArrayList<Compound> a_aValues)
    {
        this.m_aIonExchangeCount = a_aValues;
    }

    public ArrayList<Compound> getIonExchangeCountList()
    {
        return this.m_aIonExchangeCount;
    }

    public void setMultiFragmentationList(ArrayList<Compound> a_aValues)
    {
        this.m_aMultiFragmentation = a_aValues;
    }

    public ArrayList<Compound> getMultiFragmentationList()
    {
        return this.m_aMultiFragmentation;
    }

    public void setFragmentTypeList(ArrayList<Compound> a_aValues)
    {
        this.m_aFragmentType = a_aValues;
    }

    public ArrayList<Compound> getFragmentTypeList()
    {
        return this.m_aFragmentType;
    }

    public void setChargeList(ArrayList<Compound> a_aValues)
    {
        this.m_aCharges = a_aValues;
    }

    public ArrayList<Compound> getChargeList()
    {
        return this.m_aCharges;
    }

    public void setOtherIonBool(String a_strValue)
    {        
        if ( a_strValue.equalsIgnoreCase("true") )
        {
            this.m_bOtherIon = true;
        }
        else
        {
            this.m_bOtherIon = false;    
        }
    }

    public String getOtherIonBool()
    {
        if ( this.m_bOtherIon )
        {
            return "true";
        }
        return "false";
    }

    public void setIonBool(String a_strValue)
    {        
        if ( a_strValue.equalsIgnoreCase("true") )
        {
            this.m_bIons = true;
        }
        else
        {
            this.m_bIons = false;    
        }
    }

    public String getIonBool()
    {
        if ( this.m_bIons )
        {
            return "true";
        }
        return "false";
    }

    public void setIonExchangeBool(String a_strValue)
    {        
        if ( a_strValue.equalsIgnoreCase("true") )
        {
            this.m_bIonExchange = true;
        }
        else
        {
            this.m_bIonExchange = false;    
        }
    }

    public String getIonExchangeBool()
    {
        if ( this.m_bIonExchange )
        {
            return "true";
        }
        return "false";
    }

    public String getOtherIonMass()
    {
        return String.format("%f",this.m_dOtherIonMass);
    }

    public void setOtherIonMass(String a_strValue)
    {
        String t_strMass = a_strValue.replace(',','.');
        try
        {
            this.m_dOtherIonMass = Double.parseDouble(t_strMass);            
        } 
        catch (NumberFormatException e) 
        {
            this.m_dOtherIonMass = 0;
        }   
    }

    public String getOtherLossGainMoleculeMass()
    {
        return String.format("%f",this.m_objOtherLossGainMolecule.getMass());
    }

    public void setOtherLossGainMoleculeMass(String a_strValue)
    {
        String t_strMass = a_strValue.replace(',','.');
        try
        {
            this.m_objOtherLossGainMolecule.setMass(Double.parseDouble(t_strMass));            
        } 
        catch (NumberFormatException e) 
        {
            this.m_objOtherLossGainMolecule.setMass(0);
        }
    }

    public String getAccuracy()
    {
        return String.format("%f",this.m_dAccuracy);
    }

    public void setAccuracy(String a_strValue)
    {
        String t_strMass = a_strValue.replace(',','.');
        try
        {
            this.m_dAccuracy = Double.parseDouble(t_strMass);            
        } 
        catch (NumberFormatException e) 
        {
            this.m_aErrors.add( String.format( ErrorTextEnglish.ACCURACY_NEGATIV) );                        
        }   
    }

    public void setAccuracyType(String a_strType)
    {
        this.m_strAccuracyType = a_strType;
    }

    public String getAccuracyType()
    {
        return this.m_strAccuracyType;
    }

    public void setFragmentType(String[] a_aFragments)
    {
        if ( a_aFragments != null )
        {
            for (int t_iCounter = 0; t_iCounter < a_aFragments.length; t_iCounter++) 
            {
                for (Iterator<Compound> t_iterElement = this.m_aFragmentType.iterator(); t_iterElement.hasNext();) 
                {
                    Compound t_objElement = t_iterElement.next();
                    if ( t_objElement.getName().equalsIgnoreCase(a_aFragments[t_iCounter]))
                    {
                        t_objElement.setUsed(true);
                    }
                }
            }
        }
    }

    public String[] getFragmentType()
    {
        return null;
    }

    public void setCharge(String[] a_aValue)
    {
        if ( a_aValue != null )
        {
            for (int t_iCounter = 0; t_iCounter < a_aValue.length; t_iCounter++) 
            {
                for (Iterator<Compound> t_iterElement = this.m_aCharges.iterator(); t_iterElement.hasNext();) 
                {
                    Compound t_objElement = t_iterElement.next();
                    if ( t_objElement.getName().equalsIgnoreCase(a_aValue[t_iCounter]))
                    {
                        t_objElement.setUsed(true);
                    }
                }
            }
        }
    }

    public String[] getCharge()
    {
        return null;
    }

    public void setIon(String[] a_aValue)
    {
        if ( a_aValue != null )
        {
            for (int t_iCounter = 0; t_iCounter < a_aValue.length; t_iCounter++) 
            {
                for (Iterator<Compound> t_iterElement = this.m_aIons.iterator(); t_iterElement.hasNext();) 
                {
                    Compound t_objElement = t_iterElement.next();
                    if ( t_objElement.getName().equalsIgnoreCase(a_aValue[t_iCounter]))
                    {
                        t_objElement.setUsed(true);
                    }
                }
            }
        }
    }

    public String[] getIon()
    {
        return null;
    }

    public void setIonExchangeCount(String[] a_aValue)
    {
        if ( a_aValue != null )
        {
            for (int t_iCounter = 0; t_iCounter < a_aValue.length; t_iCounter++) 
            {
                for (Iterator<Compound> t_iterElement = this.m_aIonExchangeCount.iterator(); t_iterElement.hasNext();) 
                {
                    Compound t_objElement = t_iterElement.next();
                    if ( t_objElement.getName().equalsIgnoreCase(a_aValue[t_iCounter]))
                    {
                        t_objElement.setUsed(true);
                    }
                }
            }
        }
    }

    public String[] getIonExchangeCount()
    {
        return null;
    }

    public void setMultiFragmentation(String[] a_aValue)
    {
        if ( a_aValue != null )
        {
            for (int t_iCounter = 0; t_iCounter < a_aValue.length; t_iCounter++) 
            {
                for (Iterator<Compound> t_iterElement = this.m_aMultiFragmentation.iterator(); t_iterElement.hasNext();) 
                {
                    Compound t_objElement = t_iterElement.next();
                    if ( t_objElement.getName().equalsIgnoreCase(a_aValue[t_iCounter]))
                    {
                        t_objElement.setUsed(true);
                    }
                }
            }
        }
    }

    public String[] getMultiFragmentation()
    {
        return null;
    }

    /**
     * 
     */
    public void validateResiduePage() 
    {
        int t_iMax = 0;
        ResidueCategory t_objCategorie;
        MassResidue t_objResidue;
        for (Iterator<ResidueCategory> t_iterCategorie = this.m_aResidues.iterator(); t_iterCategorie.hasNext();) 
        {
            t_objCategorie = t_iterCategorie.next();
            for (Iterator<MassResidue> t_iterResidues = t_objCategorie.getResidues().iterator(); t_iterResidues.hasNext();) 
            {
                t_objResidue = t_iterResidues.next();
                t_iMax += t_objResidue.getMax();
                if ( t_objResidue.getMin() < 0 )
                {
                    t_objResidue.setMin(0);
                    this.m_aErrors.add( String.format( ErrorTextEnglish.RESIDUE_NUMBER , t_objResidue.getAbbr()) );
                }
                if ( t_objResidue.getMax() < 0 )
                {
                    t_objResidue.setMin(0);
                    this.m_aErrors.add( String.format( ErrorTextEnglish.RESIDUE_NUMBER , t_objResidue.getAbbr()) );
                }
                if ( t_objResidue.getMin() > t_objResidue.getMax() )
                {
                    this.m_aErrors.add( String.format( ErrorTextEnglish.RESIDUE_MIN_MAX , t_objResidue.getAbbr()) );
                }                
            }
        }   
        // other 1
        if ( this.m_objOtherResidueOne.getMax() < 0 )
        {
            this.m_objOtherResidueOne.setMax(0);
            this.m_aErrors.add( String.format( ErrorTextEnglish.RESIDUE_NUMBER , this.m_objOtherResidueOne.getName()));
        }
        else
        {
            t_iMax += this.m_objOtherResidueOne.getMax();
        }
        if ( this.m_objOtherResidueOne.getMin() < 0 )
        {
            this.m_objOtherResidueOne.setMin(0);
            this.m_aErrors.add( String.format( ErrorTextEnglish.RESIDUE_NUMBER , this.m_objOtherResidueOne.getName()));
        }
        if ( this.m_objOtherResidueOne.getMass() < 0 )
        {
            this.m_aErrors.add( ErrorTextEnglish.RESIDUE_OTHER_MASS );
        }
        // other 2
        if ( this.m_objOtherResidueTwo.getMax() < 0 )
        {
            this.m_objOtherResidueTwo.setMax(0);
            this.m_aErrors.add( String.format( ErrorTextEnglish.RESIDUE_NUMBER , this.m_objOtherResidueTwo.getName()));
        }
        else
        {
            t_iMax += this.m_objOtherResidueTwo.getMax();
        }
        if ( this.m_objOtherResidueTwo.getMin() < 0 )
        {
            this.m_objOtherResidueTwo.setMin(0);
            this.m_aErrors.add( String.format( ErrorTextEnglish.RESIDUE_NUMBER , this.m_objOtherResidueTwo.getName()));
        }
        if ( this.m_objOtherResidueTwo.getMass() < 0 )
        {
            this.m_aErrors.add( ErrorTextEnglish.RESIDUE_OTHER_MASS );
        }
        // other 3
        if ( this.m_objOtherResidueThree.getMax() < 0 )
        {
            this.m_objOtherResidueThree.setMax(0);
            this.m_aErrors.add( String.format( ErrorTextEnglish.RESIDUE_NUMBER , this.m_objOtherResidueThree.getName()));
        }
        else
        {
            t_iMax += this.m_objOtherResidueThree.getMax();
        }
        if ( this.m_objOtherResidueThree.getMin() < 0 )
        {
            this.m_objOtherResidueThree.setMin(0);
            this.m_aErrors.add( String.format( ErrorTextEnglish.RESIDUE_NUMBER , this.m_objOtherResidueThree.getName()));
        }
        if ( this.m_objOtherResidueThree.getMass() < 0 )
        {
            this.m_aErrors.add( ErrorTextEnglish.RESIDUE_OTHER_MASS );
        }
        if ( t_iMax < 1)
        {
            this.m_aErrors.add( ErrorTextEnglish.NO_RESIDUE );
        }
    }

    /**
     * @param category
     */
    public void setCategorie(ArrayList<ResidueCategory> a_aCategorie) 
    {
        this.m_aResidues = a_aCategorie;
    }

    public ArrayList<ResidueCategory> getCategorie()
    {
        return this.m_aResidues;
    }

    /**
     * @param string
     */
    public void addError(String a_strErrorString)
    {
        this.m_aErrors.add(a_strErrorString);        
    }

    public void setOtherResidueOne(GPOtherResidue a_objResidue)
    {
        this.m_objOtherResidueOne = a_objResidue;
    }

    public GPOtherResidue getOtherResidueOne()
    {
        return this.m_objOtherResidueOne;
    }

    public void setOtherResidueTwo(GPOtherResidue a_objResidue)
    {
        this.m_objOtherResidueTwo = a_objResidue;
    }

    public GPOtherResidue getOtherResidueTwo()
    {
        return this.m_objOtherResidueTwo;
    }

    public void setOtherResidueThree(GPOtherResidue a_objResidue)
    {
        this.m_objOtherResidueThree = a_objResidue;
    }

    public GPOtherResidue getOtherResidueThree()
    {
        return this.m_objOtherResidueThree;
    }

    public CalculationParameter generateCalculationParameters(MassValueStorage a_objMasses,DBInterface a_objDB) throws ParameterException, Exception 
    {
        boolean t_bMonoisotopic = true;
        if ( this.m_strMassType.equals("avg") )
        {
            t_bMonoisotopic = false;
        }
        Persubstitution t_objPersub = this.generatePersubitution();
        CalculationParameter t_objResult = new CalculationParameter();
        // spectra type
        if ( this.m_strSpectraType.equals("profile") )
        {
            t_objResult.setSpectraType(SpectraType.Profile);
        }
        else if ( this.m_strSpectraType.equals("ms2") )
        {
            t_objResult.setSpectraType(SpectraType.MS2);
        }
        else
        {
            t_objResult.setSpectraType(SpectraType.Fragmented);                
        }
        // max annotations
        t_objResult.setMaxAnnotationPerPeak(this.m_iAnnotationsPerPeak);
        // scan
        Scan t_objScan = new Scan();
        t_objScan.setId(1);
        ArrayList<CalculationPeak> t_aPeaks = new ArrayList<CalculationPeak>();
        GPPeak t_objGPPeak;
        CalculationPeak t_objCalcPeak;
        for (Iterator<GPPeak> t_iterPeaks = this.m_aPeaks.iterator(); t_iterPeaks.hasNext();) 
        {
            t_objGPPeak = t_iterPeaks.next();
            t_objCalcPeak = new CalculationPeak();
            t_objCalcPeak.setIntensity(t_objGPPeak.getIntensity());
            t_objCalcPeak.setMz(t_objGPPeak.getMZ());
            t_objCalcPeak.setCharge(t_objGPPeak.getCharge());
            t_aPeaks.add(t_objCalcPeak);
        }
        t_objScan.setPeaks(t_aPeaks);
        if ( this.m_strSpectraType.equals("ms2") && this.m_dPrecursor > 0 )
        {
            t_objScan.setPrecursorMass(this.m_dPrecursor);
        }
        t_objResult.setScan(t_objScan);
        // mass shift
        t_objResult.setMassShift(this.m_dMassShift);
        // accuracy
        if ( this.m_strAccuracyType.equals("ppm") )
        {
            t_objResult.setAccuracyPpm(true);
        }
        else
        {
            t_objResult.setAccuracyPpm(false);
        }
        t_objResult.setAccuracy(this.m_dAccuracy);
        // residues
        ArrayList<CalculationMolecule> t_aResidues = new ArrayList<CalculationMolecule>();
        CalculationMolecule t_objMol;
        for (Iterator<ResidueCategory> t_iterCategory = this.m_aResidues.iterator(); t_iterCategory.hasNext();) 
        {
            for (Iterator<MassResidue> t_iterResidue = t_iterCategory.next().getResidues().iterator(); t_iterResidue.hasNext();) 
            {
                MassResidue t_objResidue = t_iterResidue.next();
                if ( t_objResidue.getMax() > 0 )
                {
                    t_objMol = new CalculationMolecule();
                    t_objMol.setId(t_objResidue.getAbbr());
                    t_objMol.setMass( a_objMasses.getResidueMass(t_objResidue.getAbbr(), t_objPersub, t_bMonoisotopic));
                    t_objMol.setMin( t_objResidue.getMin() );
                    t_objMol.setMax( t_objResidue.getMax() );
                    t_aResidues.add(t_objMol);
                }
            }
        }
        t_objResult.setResidues(t_aResidues);
        // add other residues
        if ( this.m_objOtherResidueOne.getMass() != 0 && this.m_objOtherResidueOne.getMax() > 0 )
        {
            t_objMol = new CalculationMolecule();
            t_objMol.setId(this.m_objOtherResidueOne.getName() );
            t_objMol.setMass( this.m_objOtherResidueOne.getMass() );
            t_objMol.setMin( this.m_objOtherResidueOne.getMin() );
            t_objMol.setMax( this.m_objOtherResidueOne.getMax() );
            t_aResidues.add(t_objMol);
        }
        if ( this.m_objOtherResidueTwo.getMass() != 0 && this.m_objOtherResidueTwo.getMax() > 0 )
        {
            t_objMol = new CalculationMolecule();
            t_objMol.setId(this.m_objOtherResidueTwo.getName() );
            t_objMol.setMass( this.m_objOtherResidueTwo.getMass() );
            t_objMol.setMin( this.m_objOtherResidueTwo.getMin() );
            t_objMol.setMax( this.m_objOtherResidueTwo.getMax() );
            t_aResidues.add(t_objMol);
        }
        if ( this.m_objOtherResidueThree.getMass() != 0 && this.m_objOtherResidueTwo.getMax() > 0 )
        {
            t_objMol = new CalculationMolecule();
            t_objMol.setId(this.m_objOtherResidueThree.getName() );
            t_objMol.setMass( this.m_objOtherResidueThree.getMass() );
            t_objMol.setMin( this.m_objOtherResidueThree.getMin() );
            t_objMol.setMax( this.m_objOtherResidueThree.getMax() );
            t_aResidues.add(t_objMol);
        }
        t_objResult.setResidues(t_aResidues);
        // ions
        ArrayList<CalculationIon> t_aIons = new ArrayList<CalculationIon>();
        CalculationIon t_objIon = null;
        Compound t_objCompound = null;
        for (Iterator<Compound> t_iterIon = this.m_aIons.iterator(); t_iterIon.hasNext();) 
        {
            t_objCompound = t_iterIon.next();
            if ( t_objCompound.getUsed() )
            {
                t_objIon = new CalculationIon();
                t_objIon.setId( t_objCompound.getName() );
                t_objIon.setCharge(1);
                t_objIon.setMass( a_objMasses.getIonMass(t_objCompound.getName(), t_bMonoisotopic));
                t_aIons.add(t_objIon);
            }
        }
        // other ion
        if ( this.m_bOtherIon )
        {
            t_objIon = new CalculationIon();
            t_objIon.setId( this.m_strOtherIon );
            t_objIon.setCharge(1);
            t_objIon.setMass( this.m_dOtherIonMass );
            t_aIons.add(t_objIon);
        }
        t_objResult.setIons( t_aIons );
        // ion count
        ArrayList<Integer> t_aCharges = new ArrayList<Integer>();
        for (Iterator<Compound> t_iterCharges = this.m_aCharges.iterator(); t_iterCharges.hasNext();) 
        {
            t_objCompound = t_iterCharges.next();
            if ( t_objCompound.getUsed() )
            {
                t_aCharges.add( Integer.parseInt(t_objCompound.getAbbr()) );
            }
        }
        t_objResult.setCharges(t_aCharges);
        // ion exchange ions
        t_aIons = new ArrayList<CalculationIon>();
        for (Iterator<Compound> t_iterIon = this.m_aIonExchangeIons.iterator(); t_iterIon.hasNext();) 
        {
            t_objCompound = t_iterIon.next();
            if ( t_objCompound.getUsed() )
            {
                t_objIon = new CalculationIon();
                t_objIon.setId( t_objCompound.getName() );
                t_objIon.setCharge(1);
                t_objIon.setMass( a_objMasses.getIonMass(t_objCompound.getName(), t_bMonoisotopic));
                t_aIons.add(t_objIon);
            }
        }
        // ion exchange ions
        if ( this.m_bOtherIonExchangeIon )
        {
            t_objIon = new CalculationIon();
            t_objIon.setId( this.m_strOtherIonExchangeIonName );
            t_objIon.setCharge(1);
            t_objIon.setMass( this.m_dOtherIonExchangeIonMass );
            t_aIons.add(t_objIon);
        }
        t_objResult.setIonExchangeIon( t_aIons );
        // ion exchange ion count
        t_aCharges = new ArrayList<Integer>();
        for (Iterator<Compound> t_iterCharges = this.m_aIonExchangeCount.iterator(); t_iterCharges.hasNext();) 
        {
            t_objCompound = t_iterCharges.next();
            if ( t_objCompound.getUsed() )
            {
                t_aCharges.add( Integer.parseInt(t_objCompound.getAbbr()) );
            }
        }
        t_objResult.setIonExchangeCount(t_aCharges);
        // gain
        ArrayList<CalculationMolecule> t_aMol = new ArrayList<CalculationMolecule>();
        MassMolecule t_objMolecule = null;
        for (Iterator<MassMolecule> t_iterMol = this.m_aLossGainMolecules.iterator(); t_iterMol.hasNext();) 
        {
            t_objMolecule = t_iterMol.next();
            if ( t_objMolecule.getGain() > 0 )
            {
                CalculationMolecule t_objNewMol = new CalculationMolecule();
                t_objNewMol.setId( t_objMolecule.getAbbr() );
                t_objNewMol.setMin(0);
                t_objNewMol.setMax(t_objMolecule.getGain());
                t_objNewMol.setMass( a_objMasses.getMoleculeMass(t_objMolecule.getName(), t_bMonoisotopic));
                t_aMol.add(t_objNewMol);
            }        
        }
        // other gain mol
        if ( this.m_objOtherLossGainMolecule.getMass() != 0 && this.m_objOtherLossGainMolecule.getGain() > 0 )
        {
            CalculationMolecule t_objNewMol = new CalculationMolecule();
            t_objNewMol.setId( this.m_objOtherLossGainMolecule.getName() );
            t_objNewMol.setMin(0);
            t_objNewMol.setMax( this.m_objOtherLossGainMolecule.getGain() );
            t_objNewMol.setMass( this.m_objOtherLossGainMolecule.getMass() );
            t_aMol.add(t_objNewMol);
        }
        t_objResult.setGainMolecules(t_aMol);
        // lose
        t_aMol = new ArrayList<CalculationMolecule>();
        t_objMolecule = null;
        for (Iterator<MassMolecule> t_iterMol = this.m_aLossGainMolecules.iterator(); t_iterMol.hasNext();) 
        {
            t_objMolecule = t_iterMol.next();
            if ( t_objMolecule.getLoss() > 0 )
            {
                CalculationMolecule t_objNewMol = new CalculationMolecule();
                t_objNewMol.setId( t_objMolecule.getAbbr() );
                t_objNewMol.setMin(0);
                t_objNewMol.setMax(t_objMolecule.getLoss());
                t_objNewMol.setMass( a_objMasses.getMoleculeMass(t_objMolecule.getName(), t_bMonoisotopic));
                t_aMol.add(t_objNewMol);
            }        
        }
        // other lose mol
        if ( this.m_objOtherLossGainMolecule.getMass() != 0 && this.m_objOtherLossGainMolecule.getLoss() > 0 )
        {
            CalculationMolecule t_objNewMol = new CalculationMolecule();
            t_objNewMol.setId( this.m_objOtherLossGainMolecule.getName() );
            t_objNewMol.setMin(0);
            t_objNewMol.setMax( this.m_objOtherLossGainMolecule.getLoss() );
            t_objNewMol.setMass( this.m_objOtherLossGainMolecule.getMass() );
            t_aMol.add(t_objNewMol);
        }
        t_objResult.setLossMolecules(t_aMol);
        // fragment count
        if ( !this.m_strSpectraType.equals("profile") )
        {
            t_aCharges = new ArrayList<Integer>();
            for (Iterator<Compound> t_iterFrag = this.m_aMultiFragmentation.iterator(); t_iterFrag.hasNext();) 
            {
                t_objCompound = t_iterFrag.next();
                if ( t_objCompound.getUsed() )
                {
                    t_aCharges.add( Integer.parseInt(t_objCompound.getAbbr() ) );
                }
            }
            t_objResult.setMultiFragments(t_aCharges);
        }
        // reducing end mass : none , red , ab , pa , aa , lipid , peptid, other
        CalculationDerivatisation t_objDerivate = new CalculationDerivatisation();
        if ( this.m_strDerivatisation.equalsIgnoreCase("other") )
        {
            t_objDerivate.setMass(this.m_dOtherModification);
            t_objDerivate.setId(this.m_strOtherModification);
        }
        else if (this.m_strDerivatisation.equalsIgnoreCase("peptid")) 
        {
            t_objDerivate.setMass(a_objDB.getAsSequenceMass(this.m_strAsSequence,t_bMonoisotopic));
            t_objDerivate.setId("peptide");
        } 
        else if (this.m_strDerivatisation.equalsIgnoreCase("lipid"))
        {
            String t_strFatty = "";
            String t_strSphingo = "";
            for (Iterator<Compound> t_iterFatty = this.m_aFattyAcid.iterator(); t_iterFatty.hasNext();) 
            {
                t_objCompound = t_iterFatty.next();
                if ( t_objCompound.getUsed() )
                {
                    t_strFatty = t_objCompound.getAbbr();
                }
            }
            for (Iterator<Compound> t_iterSp = this.m_aSphingosin.iterator(); t_iterSp.hasNext();) 
            {
                t_objCompound = t_iterSp.next();
                if ( t_objCompound.getUsed() )
                {
                    t_strSphingo = t_objCompound.getAbbr();
                }
            }
            t_objDerivate.setMass( a_objDB.getFattyAcid(t_strFatty,t_bMonoisotopic) 
                    - (2 * a_objMasses.getMassH2O(t_bMonoisotopic))
                    + a_objDB.getSphingosin(t_strSphingo,t_bMonoisotopic) );
            t_objDerivate.setId("lipide");
        }
        else 
        {
            t_objDerivate.setMass(a_objMasses.getDerivatisationMass(this.m_strDerivatisation, t_objPersub, t_bMonoisotopic));
            t_objDerivate.setId(this.m_strDerivatisation);
        }
        ArrayList<CalculationDerivatisation> t_aDerivates = new ArrayList<CalculationDerivatisation>();
        t_aDerivates.add(t_objDerivate);
        t_objResult.setDerivatisation(t_aDerivates);
        // fragments
        if ( !this.m_strSpectraType.equals("profile") )
        {
            ArrayList<CalculationFragment> t_aFragmentsRed = new ArrayList<CalculationFragment>();
            ArrayList<CalculationFragment> t_aFragmentsNonRed = new ArrayList<CalculationFragment>();
            CalculationFragment t_objFragment = null;
            MassResidue t_objResidue = null;
            for (Iterator<Compound> t_iterFrag = this.m_aFragmentType.iterator(); t_iterFrag.hasNext();) 
            {
                t_objCompound = t_iterFrag.next();
                if ( t_objCompound.getUsed() )
                {
                    if ( t_objCompound.getName().equals("A") )
                    {
                        for (Iterator<ResidueCategory> t_iterCate = this.m_aResidues.iterator(); t_iterCate.hasNext();) 
                        {
                            for (Iterator<MassResidue> t_iterResidue = t_iterCate.next().getResidues().iterator(); t_iterResidue.hasNext();) 
                            {
                                t_objResidue = t_iterResidue.next();
                                if ( t_objResidue.getUseAx() && t_objResidue.getMax() > 0 )
                                {
                                    a_objDB.addA(t_objResidue,t_aFragmentsRed,t_objPersub,t_bMonoisotopic,
                                            a_objMasses.getMassOH(t_bMonoisotopic) - a_objMasses.getIncrementMassAX(t_objPersub, t_bMonoisotopic) );
                                }                            
                            }                        
                        }
                    }
                    else if ( t_objCompound.getName().equals("B") )
                    {
                        t_objFragment = new CalculationFragment("B",null,a_objMasses.getGlycosidicFragmentMass("b",t_bMonoisotopic));
                        t_objFragment.setFragmentType("B");
                        t_aFragmentsRed.add(t_objFragment);
                    }
                    else if ( t_objCompound.getName().equals("C") )
                    {
                        t_objFragment = new CalculationFragment("C",null,a_objMasses.getGlycosidicFragmentMass("c",t_bMonoisotopic));
                        t_objFragment.setFragmentType("C");
                        t_aFragmentsRed.add(t_objFragment);
                    }
                    else if ( t_objCompound.getName().equals("E") )
                    {
                        for (Iterator<ResidueCategory> t_iterCate = this.m_aResidues.iterator(); t_iterCate.hasNext();) 
                        {
                            for (Iterator<MassResidue> t_iterResidue = t_iterCate.next().getResidues().iterator(); t_iterResidue.hasNext();) 
                            {
                                t_objResidue = t_iterResidue.next();
                                if ( t_objResidue.getUseE() && t_objResidue.getMax() > 0 )
                                {
                                    a_objDB.addE(t_objResidue,t_aFragmentsRed,t_objPersub,t_bMonoisotopic,a_objMasses );
                                }                            
                            }                        
                        }
                    }
                    else if ( t_objCompound.getName().equals("F") )
                    {
                        for (Iterator<ResidueCategory> t_iterCate = this.m_aResidues.iterator(); t_iterCate.hasNext();) 
                        {
                            for (Iterator<MassResidue> t_iterResidue = t_iterCate.next().getResidues().iterator(); t_iterResidue.hasNext();) 
                            {
                                t_objResidue = t_iterResidue.next();
                                if ( t_objResidue.getUseF() && t_objResidue.getMax() > 0 )
                                {
                                    a_objDB.addF(t_objResidue,t_aFragmentsNonRed,t_objPersub,t_bMonoisotopic ,a_objMasses);
                                }                            
                            }                        
                        }
                    }
                    else if ( t_objCompound.getName().equals("G") )
                    {
                        for (Iterator<ResidueCategory> t_iterCate = this.m_aResidues.iterator(); t_iterCate.hasNext();) 
                        {
                            for (Iterator<MassResidue> t_iterResidue = t_iterCate.next().getResidues().iterator(); t_iterResidue.hasNext();) 
                            {
                                t_objResidue = t_iterResidue.next();
                                if ( t_objResidue.getUseG() && t_objResidue.getMax() > 0 )
                                {
                                    a_objDB.addG(t_objResidue,t_aFragmentsNonRed,t_objPersub,t_bMonoisotopic ,a_objMasses);
                                }                            
                            }                        
                        }
                    }
                    else if ( t_objCompound.getName().equals("H") )
                    {
                        for (Iterator<ResidueCategory> t_iterCate = this.m_aResidues.iterator(); t_iterCate.hasNext();) 
                        {
                            for (Iterator<MassResidue> t_iterResidue = t_iterCate.next().getResidues().iterator(); t_iterResidue.hasNext();) 
                            {
                                t_objResidue = t_iterResidue.next();
                                if ( t_objResidue.getUseH() && t_objResidue.getMax() > 0 )
                                {
                                    a_objDB.addH(t_objResidue,t_aFragmentsNonRed,t_objPersub,t_bMonoisotopic ,a_objMasses);
                                }                            
                            }                        
                        }
                    }
                    else if ( t_objCompound.getName().equals("X") )
                    {
                        for (Iterator<ResidueCategory> t_iterCate = this.m_aResidues.iterator(); t_iterCate.hasNext();) 
                        {
                            for (Iterator<MassResidue> t_iterResidue = t_iterCate.next().getResidues().iterator(); t_iterResidue.hasNext();) 
                            {
                                t_objResidue = t_iterResidue.next();
                                if ( t_objResidue.getUseAx() && t_objResidue.getMax() > 0 )
                                {
                                    a_objDB.addX(t_objResidue,t_aFragmentsNonRed,t_objPersub,t_bMonoisotopic,
                                            a_objMasses.getMassH(t_bMonoisotopic) - a_objMasses.getIncrementMassAX(t_objPersub, t_bMonoisotopic) );
                                }                            
                            }                        
                        }
                    }
                    else if ( t_objCompound.getName().equals("Y") )
                    {
                        t_objFragment = new CalculationFragment("Y",null,a_objMasses.getGlycosidicFragmentMass("y",t_bMonoisotopic));
                        t_objFragment.setFragmentType("Y");
                        t_aFragmentsNonRed.add(t_objFragment);
                    }
                    else if ( t_objCompound.getName().equals("Z") )
                    {
                        t_objFragment = new CalculationFragment("Z",null,a_objMasses.getGlycosidicFragmentMass("z",t_bMonoisotopic));
                        t_objFragment.setFragmentType("Z");
                        t_aFragmentsNonRed.add(t_objFragment);
                    }
                    else 
                    {
                        throw new ParameterException("Unknown fragmentation type : "  + t_objCompound.getName() );
                    }
                }            
            }
            t_objResult.setFragmentsNonRed(t_aFragmentsNonRed);
            t_objResult.setFragmentsRed(t_aFragmentsRed);
        }
        // Ergaenzungen
        t_objResult.setCompletionNonRed(a_objMasses.getCompletionMass("nonred",t_objPersub,t_bMonoisotopic));
        t_objResult.setCompletionRed(a_objMasses.getCompletionMass("red",t_objPersub,t_bMonoisotopic));
        t_objResult.setNonReducingDifference(a_objMasses.getNonReducingDifference(t_objPersub,t_bMonoisotopic));
        t_objResult.setExchangeIonMass(a_objMasses.getExchangeIonMass(t_bMonoisotopic));
        return t_objResult;
    }

    /**
     * @return
     * @throws ParameterException 
     */
    private Persubstitution generatePersubitution() throws ParameterException 
    {
        // none , pme , pdme, pdac , pac
        if ( this.m_strPerSubstitution.equals("none") )
        {
            return Persubstitution.None;
        }
        else if ( this.m_strPerSubstitution.equals("pme") )
        {
            return Persubstitution.Me;
        }
        else if ( this.m_strPerSubstitution.equals("pdme") )
        {
            return Persubstitution.DMe;
        }
        else if ( this.m_strPerSubstitution.equals("pac") )
        {
            return Persubstitution.Ac;
        }
        else if ( this.m_strPerSubstitution.equals("pdac") )
        {
            return Persubstitution.DAc;
        }
        else
        {
            throw new ParameterException("Unknown persubstitution :" + this.m_strPerSubstitution );
        }
    }

    /**
     * @param calcParam
     */
    public ArrayList<String> generateFromCalculationParameter(CalculationParameter a_objCalcParam) 
    {
        this.resetErrors();
        this.resetIonPage();
        this.resetMassPage();
        this.resetModificationPage();
        this.resetResiduePage();

        Compound t_objCompound = null;
        String t_strValue = null;
        boolean t_bFound = false;
        // accuracy & massshift
        ArrayList<String> t_aErrors = new ArrayList<String>();
        this.m_dAccuracy = a_objCalcParam.getAccuracy();
        this.m_dMassShift = a_objCalcParam.getMassShift();
        if ( a_objCalcParam.getAccuracyPpm() )
        {
            this.m_strAccuracyType = "ppm";
        }
        else
        {
            this.m_strAccuracyType = "u";
        }
        // max annotations
        if ( a_objCalcParam.getMaxAnnotationPerPeak() > 0 )
        {
            this.m_iAnnotationsPerPeak = a_objCalcParam.getMaxAnnotationPerPeak();
        }
        // charges & ions
        for (Iterator<Integer> t_iterCharge = a_objCalcParam.getCharges().iterator(); t_iterCharge.hasNext();) 
        {
            t_strValue = t_iterCharge.next().toString();
            t_bFound = false;
            for (Iterator<Compound> t_iterGPCarge = this.m_aCharges.iterator(); t_iterGPCarge.hasNext();) 
            {
                t_objCompound = t_iterGPCarge.next();
                if ( t_objCompound.getId().equals(t_strValue) )
                {
                    t_objCompound.setUsed(true);
                    t_bFound = true;
                }            
            }
            if ( !t_bFound )
            {
                t_aErrors.add("Chargestate " + t_strValue + " is not supported.");
            }
        }  
        boolean t_bOther = false;
        boolean t_bNormal = false;
        for (Iterator<CalculationIon> t_iterIons = a_objCalcParam.getIons().iterator(); t_iterIons.hasNext();) 
        {
            CalculationIon t_objIon = t_iterIons.next();
            t_bFound = false;
            if ( t_objIon.getCharge() == 1 )
            {
                for (Iterator<Compound> t_iterGPIon = this.m_aIons.iterator(); t_iterGPIon.hasNext();) 
                {
                    t_objCompound = t_iterGPIon.next();
                    if ( t_objCompound.getName().equalsIgnoreCase(t_objIon.getId()))
                    {
                        t_objCompound.setUsed(true);
                        t_bFound = true;
                        t_bNormal = true;
                    }
                }
                if ( !t_bFound )
                {
                    if ( t_bOther )
                    {
                        t_aErrors.add("More than one other ion is not supported (" + t_objIon.getId() + ")");
                    }
                    else
                    {
                        this.m_bOtherIon = true;
                        this.m_strOtherIon = t_objIon.getId();
                        this.m_dOtherIonMass = t_objIon.getMass();
                        t_bOther = true;
                    }
                }
            }
            else
            {
                t_aErrors.add("Multiple charged ion " + t_objIon.getId() + " is not supported.");
            }
        }
        if ( t_bNormal )
        {
            this.m_bIons = true;
        }
        else
        {
            this.m_bIons = false;
        }
        // ion exchange
        for (Iterator<Integer> t_iterCharge = a_objCalcParam.getIonExchangeCount().iterator(); t_iterCharge.hasNext();) 
        {
            t_strValue = t_iterCharge.next().toString();
            t_bFound = false;
            for (Iterator<Compound> t_iterGPCarge = this.m_aIonExchangeCount.iterator(); t_iterGPCarge.hasNext();) 
            {
                t_objCompound = t_iterGPCarge.next();
                if ( t_objCompound.getId().equals(t_strValue) )
                {
                    t_objCompound.setUsed(true);
                    t_bFound = true;
                }            
            }
            if ( !t_bFound )
            {
                t_aErrors.add("Ion exchange state " + t_strValue + " is not supported.");
            }
        }  
        t_bOther = false;
        t_bNormal = false;
        for (Iterator<CalculationIon> t_iterIons = a_objCalcParam.getIonExchangeIon().iterator(); t_iterIons.hasNext();) 
        {
            CalculationIon t_objIon = t_iterIons.next();
            t_bFound = false;
            if ( t_objIon.getCharge() == 1 )
            {
                for (Iterator<Compound> t_iterGPIon = this.m_aIonExchangeIons.iterator(); t_iterGPIon.hasNext();) 
                {
                    t_objCompound = t_iterGPIon.next();
                    if ( t_objCompound.getName().equalsIgnoreCase(t_objIon.getId()))
                    {
                        t_objCompound.setUsed(true);
                        t_bFound = true;
                        t_bNormal = true;
                    }
                }
                if ( !t_bFound )
                {
                    if ( t_bOther )
                    {
                        t_aErrors.add("More than one other ion for ion exchange is not supported (" + t_objIon.getId() + ")");
                    }
                    else
                    {
                        this.m_bOtherIon = true;
                        this.m_strOtherIon = t_objIon.getId();
                        this.m_dOtherIonMass = t_objIon.getMass();
                        t_bOther = true;
                    }
                }
            }
            else
            {
                t_aErrors.add("Multiple charged ion exchange ion " + t_objIon.getId() + " is not supported.");
            }
        }
        if ( t_bNormal )
        {
            this.m_bIonExchange = true;
        }
        else
        {
            this.m_bIonExchange = false;
        }
        // loss & gain
        t_bOther = false;
        for (Iterator<CalculationMolecule> t_iterLoss = a_objCalcParam.getLossMolecules().iterator(); t_iterLoss.hasNext();) 
        {
            CalculationMolecule t_objMolecule = t_iterLoss.next();
            t_bFound = false;
            if ( t_objMolecule.getMin() != 0 )
            {
                t_aErrors.add("Loss of small molecules other than 0-... is not supported (minimum loss must be 0");
            }
            else
            {
                for (Iterator<MassMolecule> t_iterGPLG = this.m_aLossGainMolecules.iterator(); t_iterGPLG.hasNext();) 
                {
                    MassMolecule t_objGPMol = t_iterGPLG.next();
                    if ( t_objGPMol.getAbbr().equalsIgnoreCase(t_objMolecule.getId()))
                    {
                        t_objGPMol.setLoss(t_objMolecule.getMax());
                        t_bFound = true;
                    }
                }
                if ( !t_bFound )
                {
                    if ( t_bOther )
                    {
                        t_aErrors.add("More than one other small molecule is not supported (" + t_objMolecule.getId() + ")");
                    }
                    else
                    {
                        this.m_objOtherLossGainMolecule.setAbbr(t_objMolecule.getId());
                        this.m_objOtherLossGainMolecule.setName(t_objMolecule.getId());
                        this.m_objOtherLossGainMolecule.setLoss(t_objMolecule.getMax());
                        t_bOther = true;
                    }
                }    
            }
        }
        for (Iterator<CalculationMolecule> t_iterLoss = a_objCalcParam.getGainMolecules().iterator(); t_iterLoss.hasNext();) 
        {
            CalculationMolecule t_objMolecule = t_iterLoss.next();
            t_bFound = false;
            if ( t_objMolecule.getMin() != 0 )
            {
                t_aErrors.add("Gain of small molecules other than 0-... is not supported (minimum gain must be 0");
            }
            else
            {
                for (Iterator<MassMolecule> t_iterGPLG = this.m_aLossGainMolecules.iterator(); t_iterGPLG.hasNext();) 
                {
                    MassMolecule t_objGPMol = t_iterGPLG.next();
                    if ( t_objGPMol.getAbbr().equalsIgnoreCase(t_objMolecule.getId()))
                    {
                        t_objGPMol.setGain(t_objMolecule.getMax());
                        t_bFound = true;
                    }
                }
                if ( !t_bFound )
                {
                    if ( t_bOther )
                    {
                        if ( !this.m_objOtherLossGainMolecule.getAbbr().equalsIgnoreCase(t_objMolecule.getId()))
                        {
                            t_aErrors.add("More than one other small molecule is not supported (" + t_objMolecule.getId() + ")");
                        }
                        else
                        {
                            this.m_objOtherLossGainMolecule.setGain(t_objMolecule.getMax());    
                        }
                    }
                    else
                    {
                        this.m_objOtherLossGainMolecule.setAbbr(t_objMolecule.getId());
                        this.m_objOtherLossGainMolecule.setName(t_objMolecule.getId());
                        this.m_objOtherLossGainMolecule.setLoss(t_objMolecule.getMax());
                        t_bOther = true;
                    }
                }
            }
        }
        // residues
        boolean t_bOther2 = false;
        boolean t_bOther3 = false;
        t_bOther = false;
        for (Iterator<CalculationMolecule> t_iterResidue = a_objCalcParam.getResidues().iterator(); t_iterResidue.hasNext();) 
        {
            CalculationMolecule t_objResidue = t_iterResidue.next();
            t_bFound = false;
            for (Iterator<ResidueCategory> t_iterGPCategorie = this.m_aResidues.iterator(); t_iterGPCategorie.hasNext();) 
            {
                for (Iterator<MassResidue> t_iterGPResidue = t_iterGPCategorie.next().getResidues().iterator(); t_iterGPResidue.hasNext();) 
                {
                    MassResidue t_objGPResidue = t_iterGPResidue.next();
                    if ( t_objGPResidue.getAbbr().equalsIgnoreCase(t_objResidue.getId()))
                    {
                        t_objGPResidue.setMin(t_objResidue.getMin());
                        t_objGPResidue.setMax(t_objResidue.getMax());
                        t_bFound = true;
                    }    
                }
            }
            if ( !t_bFound )
            {
                if ( !t_bOther )
                {
                    this.m_objOtherResidueOne.setName(t_objResidue.getId());
                    this.m_objOtherResidueOne.setMass(t_objResidue.getMass());
                    this.m_objOtherResidueOne.setMin(t_objResidue.getMin());
                    this.m_objOtherResidueOne.setMax(t_objResidue.getMax());
                    t_bOther = true;
                }
                else if ( !t_bOther2 )
                {
                    this.m_objOtherResidueTwo.setName(t_objResidue.getId());
                    this.m_objOtherResidueTwo.setMass(t_objResidue.getMass());
                    this.m_objOtherResidueTwo.setMin(t_objResidue.getMin());
                    this.m_objOtherResidueTwo.setMax(t_objResidue.getMax());
                    t_bOther2 = true;
                }
                else if ( !t_bOther3 )
                {
                    this.m_objOtherResidueThree.setName(t_objResidue.getId());
                    this.m_objOtherResidueThree.setMass(t_objResidue.getMass());
                    this.m_objOtherResidueThree.setMin(t_objResidue.getMin());
                    this.m_objOtherResidueThree.setMax(t_objResidue.getMax());
                    t_bOther3 = true;
                }
                else
                {
                    t_aErrors.add("More than three other residues are not supported (" + t_objResidue.getId() + ")");                    
                }
            }
        }
        // Fragmentation level
        for (Iterator<Integer> t_iterMulti = a_objCalcParam.getMultiFragments().iterator(); t_iterMulti.hasNext();) 
        {
            t_strValue = t_iterMulti.next().toString();
            t_bFound = false;
            for (Iterator<Compound> t_iterGPCarge = this.m_aMultiFragmentation.iterator(); t_iterGPCarge.hasNext();) 
            {
                t_objCompound = t_iterGPCarge.next();
                if ( t_objCompound.getId().equals(t_strValue) )
                {
                    t_objCompound.setUsed(true);
                    t_bFound = true;
                }            
            }
            if ( !t_bFound )
            {
                t_aErrors.add("Multifragmentation state " + t_strValue + " is not supported.");
            }
        }  
        // spectra type
        if ( a_objCalcParam.getSpectraType() == SpectraType.Fragmented )
        {
            this.m_strSpectraType = "fragmented";
        }
        else if ( a_objCalcParam.getSpectraType() == SpectraType.Profile )
        {
            this.m_strSpectraType = "profile";
        }
        else if ( a_objCalcParam.getSpectraType() == SpectraType.MS2 )
        {
            this.m_strSpectraType = "ms2";
            this.m_dPrecursor = a_objCalcParam.getScan().getPrecusorMass();
        }
        else
        {
            t_aErrors.add("Spectra type " + a_objCalcParam.getSpectraType().getName() + " is not supported.");
        }
        // scan
        if ( a_objCalcParam.getScan().getSubScan().size() != 0 )
        {
            t_aErrors.add("More than one scan is not supported.");
        }
        for (Iterator<CalculationPeak> t_iterPeaks = a_objCalcParam.getScan().getPeaks().iterator(); t_iterPeaks.hasNext();) 
        {
            CalculationPeak t_objPeak = t_iterPeaks.next();
            GPPeak t_objGPPeak = new GPPeak();
            t_objGPPeak.setMZ(t_objPeak.getMz());
            t_objGPPeak.setIntensity(t_objPeak.getIntensity());
            t_objGPPeak.setCharge(t_objPeak.getCharge());
            this.m_aPeaks.add(t_objGPPeak);
        }
        // Fragments
        for (Iterator<CalculationFragment> t_iterFrag = a_objCalcParam.getFragmentsRed().iterator(); t_iterFrag.hasNext();) 
        {
            CalculationFragment t_objFrag = t_iterFrag.next();
            t_bFound = false;
            for (Iterator<Compound> t_iterGPFrag = this.m_aFragmentType.iterator(); t_iterGPFrag.hasNext();) 
            {
                t_objCompound = t_iterGPFrag.next();
                if ( t_objCompound.getName().equalsIgnoreCase(t_objFrag.getFragmentType()))
                {
                    t_objCompound.setUsed(true);                    
                    t_bFound = true;
                    if ( t_objCompound.getName().equalsIgnoreCase("A") || t_objCompound.getName().equalsIgnoreCase("E") )
                    {
                        if ( t_objFrag.getResidueId() == null )
                        {
                            t_aErrors.add("Residue Id missing for fragment type " + t_objFrag.getFragmentType() + ".");
                        }
                        else
                        {
                            t_bOther = false;
                            for (Iterator<ResidueCategory> t_iterGPCategorie = this.m_aResidues.iterator(); t_iterGPCategorie.hasNext();) 
                            {
                                for (Iterator<MassResidue> t_iterGPResidue = t_iterGPCategorie.next().getResidues().iterator(); t_iterGPResidue.hasNext();) 
                                {
                                    MassResidue t_objGPResidue = t_iterGPResidue.next();
                                    if ( t_objGPResidue.getAbbr().equalsIgnoreCase(t_objFrag.getResidueId()))
                                    {
                                        if ( t_objCompound.getName().equalsIgnoreCase("A") )
                                        {
                                            if ( t_objGPResidue.getHasAx() )
                                            {
                                                t_objGPResidue.setUseAX(true);
                                            }
                                            else
                                            {
                                                t_aErrors.add("Residue " + t_objGPResidue.getAbbr() + " can not have A fragments.");
                                            }
                                        }
                                        else if ( t_objCompound.getName().equalsIgnoreCase("E") )
                                        {
                                            if ( t_objGPResidue.getHasE() )
                                            {
                                                t_objGPResidue.setUseE(true);
                                            }
                                            else
                                            {
                                                t_aErrors.add("Residue " + t_objGPResidue.getAbbr() + " can not have E fragments.");
                                            }
                                        }
                                        t_bOther = true;
                                    }    
                                }
                            }
                            if ( !t_bOther )
                            {
                                t_aErrors.add("Unknown Residue Id " + t_objFrag.getResidueId() + " for fragment type " + t_objFrag.getFragmentType() + ".");
                            }
                        }
                    }
                }
            }
            if ( !t_bFound )
            {
                t_aErrors.add("Fragment type " + t_objFrag.getFragmentType() + " is not supported.");
            }
        }
        for (Iterator<CalculationFragment> t_iterFrag = a_objCalcParam.getFragmentsNonRed().iterator(); t_iterFrag.hasNext();) 
        {
            CalculationFragment t_objFrag = t_iterFrag.next();
            t_bFound = false;
            for (Iterator<Compound> t_iterGPFrag = this.m_aFragmentType.iterator(); t_iterGPFrag.hasNext();) 
            {
                t_objCompound = t_iterGPFrag.next();
                if ( t_objCompound.getName().equalsIgnoreCase(t_objFrag.getFragmentType()))
                {
                    t_objCompound.setUsed(true);                    
                    t_bFound = true;
                    if ( !t_objCompound.getName().equalsIgnoreCase("Y") && !t_objCompound.getName().equalsIgnoreCase("Z") )
                    {
                        if ( t_objFrag.getResidueId() == null )
                        {
                            t_aErrors.add("Residue Id missing for fragment type " + t_objFrag.getFragmentType() + ".");
                        }
                        else
                        {
                            t_bOther = false;
                            for (Iterator<ResidueCategory> t_iterGPCategorie = this.m_aResidues.iterator(); t_iterGPCategorie.hasNext();) 
                            {
                                for (Iterator<MassResidue> t_iterGPResidue = t_iterGPCategorie.next().getResidues().iterator(); t_iterGPResidue.hasNext();) 
                                {
                                    MassResidue t_objGPResidue = t_iterGPResidue.next();
                                    if ( t_objGPResidue.getAbbr().equalsIgnoreCase(t_objFrag.getResidueId()))
                                    {
                                        if ( t_objCompound.getName().equalsIgnoreCase("X") )
                                        {
                                            if ( t_objGPResidue.getHasAx() )
                                            {
                                                t_objGPResidue.setUseAX(true);
                                            }
                                            else
                                            {
                                                t_aErrors.add("Residue " + t_objGPResidue.getAbbr() + " can not have X fragments.");
                                            }
                                        }
                                        else if ( t_objCompound.getName().equalsIgnoreCase("F") )
                                        {
                                            if ( t_objGPResidue.getHasF() )
                                            {
                                                t_objGPResidue.setUseF(true);
                                            }
                                            else
                                            {
                                                t_aErrors.add("Residue " + t_objGPResidue.getAbbr() + " can not have F fragments.");
                                            }
                                        }
                                        else if ( t_objCompound.getName().equalsIgnoreCase("G") )
                                        {
                                            if ( t_objGPResidue.getHasG() )
                                            {
                                                t_objGPResidue.setUseG(true);
                                            }
                                            else
                                            {
                                                t_aErrors.add("Residue " + t_objGPResidue.getAbbr() + " can not have G fragments.");
                                            }
                                        }
                                        else if ( t_objCompound.getName().equalsIgnoreCase("H") )
                                        {
                                            if ( t_objGPResidue.getHasH() )
                                            {
                                                t_objGPResidue.setUseH(true);
                                            }
                                            else
                                            {
                                                t_aErrors.add("Residue " + t_objGPResidue.getAbbr() + " can not have H fragments.");
                                            }
                                        }
                                        t_bOther = true;
                                    }    
                                }
                            }
                            if ( !t_bOther )
                            {
                                t_aErrors.add("Unknown Residue Id " + t_objFrag.getResidueId() + " for fragment type " + t_objFrag.getFragmentType() + ".");
                            }
                        }
                    }
                }
            }
            if ( !t_bFound )
            {
                t_aErrors.add("Fragment type " + t_objFrag.getFragmentType() + " is not supported.");
            }
        }
        if ( a_objCalcParam.getDerivatisation().size() != 1 )
        {
            t_aErrors.add("Onyl one derivatisation of the reducing end is supported.");
        }
        else
        {
            for (Iterator<CalculationDerivatisation> t_iterDer = a_objCalcParam.getDerivatisation().iterator(); t_iterDer.hasNext();) 
            {
                CalculationDerivatisation t_objDerivate = t_iterDer.next();
                if ( t_objDerivate.getId().equalsIgnoreCase("PA") )
                {
                    this.m_strDerivatisation = "PA";
                }
                else if ( t_objDerivate.getId().equalsIgnoreCase("2AB") )
                {
                    this.m_strDerivatisation = "2AB";
                }
                else if ( t_objDerivate.getId().equalsIgnoreCase("AA") )
                {
                    this.m_strDerivatisation = "AA";
                }
                else if ( t_objDerivate.getId().equalsIgnoreCase("red") )
                {
                    this.m_strDerivatisation = "red";
                }
                else if ( t_objDerivate.getId().equalsIgnoreCase("PHN") )
                {
                    this.m_strDerivatisation = "PHN";
                }
                else if ( t_objDerivate.getId().equalsIgnoreCase("none") )
                {
                    this.m_strDerivatisation = "none";
                }
                else if ( t_objDerivate.getId().equalsIgnoreCase("DAP") )
                {
                    this.m_strDerivatisation = "DAP";
                }
                else if ( t_objDerivate.getId().equalsIgnoreCase("DAPMAB") )
                {
                    this.m_strDerivatisation = "DAPMAB";
                }
                else if ( t_objDerivate.getId().equalsIgnoreCase("AMC") )
                {
                    this.m_strDerivatisation = "AMC";
                }
                else if ( t_objDerivate.getId().equalsIgnoreCase("6AQ") )
                {
                    this.m_strDerivatisation = "6AQ";
                }
                else if ( t_objDerivate.getId().equalsIgnoreCase("2AA") )
                {
                    this.m_strDerivatisation = "2AA";
                }
                else if ( t_objDerivate.getId().equalsIgnoreCase("FMC") )
                {
                    this.m_strDerivatisation = "FMC";
                }
                else if ( t_objDerivate.getId().equalsIgnoreCase("DH") )
                {
                    this.m_strDerivatisation = "DH";
                }
                else
                {
                    this.m_strOtherModification = t_objDerivate.getId();
                    this.m_dOtherModification = t_objDerivate.getMass();
                }                    
            }
        }
        this.m_strPerSubstitution = this.generatePersubitutionString(a_objCalcParam.getPersubstitution(),t_aErrors);
        this.m_bInitialize = true;
        this.m_aErrors = t_aErrors;
        return t_aErrors;
    }

    private String generatePersubitutionString(Persubstitution a_objPersubstitution,ArrayList<String> a_aErrors) 
    {
        // none , pme , pdme, pdac , pac
        if ( a_objPersubstitution == Persubstitution.None )
        {
            return "none";
        }
        else if ( a_objPersubstitution == Persubstitution.Ac )
        {
            return "pac";
        }
        else if ( a_objPersubstitution == Persubstitution.DAc )
        {
            return "pdac";
        }
        else if ( a_objPersubstitution == Persubstitution.Me )
        {
            return "pme";
        }
        else if ( a_objPersubstitution == Persubstitution.DMe )
        {
            return "pdme";
        }
        else
        {
            a_aErrors.add("Unknown persubstitution :" + a_objPersubstitution.getAbbr() );
        }
        return "none";
    }

    public double getPrecursorMass() 
    {
        return this.m_dPrecursor;
    }
    
    public void setPrecursorMass(double a_dMass)
    {}
}
