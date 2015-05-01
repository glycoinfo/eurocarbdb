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

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.AnnotationEntity;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationParameter;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationPeak;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.PeakAnnotation;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Scan;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.ComparatorAnnotation;

/**
* @author rene
*
*/
public class GPResult
{
    private ArrayList<GPPeakAnnotated> m_aAnnotatedPeaks = new ArrayList<GPPeakAnnotated>();
    private ArrayList<GPPeakAnnotated> m_aPrecursor = new ArrayList<GPPeakAnnotated>();
    private boolean m_bIsInitialized = false;
    private Double m_dPreCursorMass = null;

    public void setPrecursor(ArrayList<GPPeakAnnotated> a_objPeak)
    {
        this.m_aPrecursor = a_objPeak;
    }

    public ArrayList<GPPeakAnnotated> getPrecursor()
    {
        return this.m_aPrecursor;
    }

    public void setInitialized(boolean a_bInit)
    {
        this.m_bIsInitialized = a_bInit;
    }

    public boolean getInitialized()
    {
        return this.m_bIsInitialized;
    }

    public void setAnnotatedPeaks( ArrayList<GPPeakAnnotated> a_aPeaks)
    {
        this.m_aAnnotatedPeaks = a_aPeaks;
    }

    public ArrayList<GPPeakAnnotated> getAnnotatedPeaks()
    {
        return this.m_aAnnotatedPeaks;
    }

    public void createFromParameter(CalculationParameter a_objCalcParam,String a_strDerivatisation,String a_strOtherDerivate, GlycoPeakfinderSettings a_objSettings) 
    {
        String t_strDerivate = a_strDerivatisation;
        ComparatorAnnotation t_objComparator = new ComparatorAnnotation();
        if ( a_strDerivatisation.equalsIgnoreCase("other") )
        {
            t_strDerivate = a_strOtherDerivate;
        }
        this.m_aAnnotatedPeaks = new ArrayList<GPPeakAnnotated>();
        this.m_aPrecursor = new ArrayList<GPPeakAnnotated>();
        ArrayList<CalculationPeak> t_aCalcPeaks = a_objCalcParam.getScan().getPeaks();
        this.m_dPreCursorMass = a_objCalcParam.getScan().getPrecusorMass();
        int t_iCounterFragment = 0;
        int t_iCounterProfile = 0;
        GPPeakAnnotated t_objAnnoPeak;
        CalculationPeak t_objCalcPeak;
        PeakAnnotation t_objAnnoCalc;
        GPAnnotation t_objAnno;
        ArrayList<GPAnnotation> t_aAnnotationFragment = new ArrayList<GPAnnotation>();
        ArrayList<GPAnnotation> t_aAnnotationProfile = new ArrayList<GPAnnotation>();
        for (Iterator<CalculationPeak> t_iterPeaks = t_aCalcPeaks.iterator(); t_iterPeaks.hasNext();) 
        {
            t_aAnnotationFragment = new ArrayList<GPAnnotation>();
            t_aAnnotationProfile = new ArrayList<GPAnnotation>();
            t_objCalcPeak = t_iterPeaks.next();
            for (Iterator<PeakAnnotation> t_iterAnno = t_objCalcPeak.getAnnotation().iterator(); t_iterAnno.hasNext();) 
            {
                t_objAnnoCalc = t_iterAnno.next();
                t_objAnno = new GPAnnotation();
                t_objAnno.setAnnotations(t_objAnnoCalc);
                String t_strValue = "";
                AnnotationEntity t_objEntry;
                // fragments
                for (Iterator<AnnotationEntity> t_iterFragment = t_objAnnoCalc.getFragments().iterator(); t_iterFragment.hasNext();) 
                {
                    t_objEntry = t_iterFragment.next();
                    t_strValue += ";" + t_objEntry.getId();
                }
                if ( t_strValue.length() > 0 )
                {
                    t_objAnno.setFragments(t_strValue.substring(1));
                }
                else
                {
                    t_objAnno.setFragments("");
                }
                // gain / Loss
                t_strValue = "";
                for (Iterator<AnnotationEntity> t_iterGL = t_objAnnoCalc.getGain().iterator(); t_iterGL.hasNext();) 
                {
                    t_objEntry = t_iterGL.next();
                    if ( t_objEntry.getNumber() > 1 )
                    {
                        t_strValue += String.format("+%d%s",t_objEntry.getNumber(),t_objEntry.getId());
                    }
                    else
                    {
                        t_strValue += "+" + t_objEntry.getId();
                    }                    
                }
                for (Iterator<AnnotationEntity> t_iterGL = t_objAnnoCalc.getLoss().iterator(); t_iterGL.hasNext();) 
                {
                    t_objEntry = t_iterGL.next();
                    if ( t_objEntry.getNumber() > 1 )
                    {
                        t_strValue += String.format("-%d%s",t_objEntry.getNumber(),t_objEntry.getId());
                    }
                    else
                    {
                        t_strValue += "-" + t_objEntry.getId();
                    }                    
                }
                if ( t_strValue.length() > 0 )
                {
                    t_objAnno.setGainLossString( t_strValue );
                }
                else
                {
                    t_objAnno.setGainLossString("");
                }
                // ions
                t_strValue = "";
                int t_iCharge = 0;
                for (Iterator<AnnotationEntity> t_oterIon = t_objAnnoCalc.getIons().iterator(); t_oterIon.hasNext();) 
                {
                    t_objEntry = t_oterIon.next();
                    if ( t_objEntry.getNumber() > 1 )
                    {
                        t_strValue += String.format(";%d%s",t_objEntry.getNumber(),t_objEntry.getId());                        
                    }
                    else
                    {
                        t_strValue += ";" + t_objEntry.getId();
                    }
                    t_iCharge += t_objEntry.getNumber();
                }
                if ( t_objAnnoCalc.getIonExchange().size() > 0 )
                {
                    if ( t_objAnnoCalc.getIonExchange().size() > 1 )
                    {
                        t_strValue += String.format(";-%dH+",t_objAnnoCalc.getIonExchange().size());
                    }
                    else
                    {
                        t_strValue += ";-H+";
                    }                    
                    for (Iterator<AnnotationEntity> t_oterIon = t_objAnnoCalc.getIonExchange().iterator(); t_oterIon.hasNext();) 
                    {
                        t_objEntry = t_oterIon.next();
                        if ( t_objEntry.getNumber() > 1 )
                        {
                            t_strValue += String.format(";%d%s",t_objEntry.getNumber(),t_objEntry.getId());
                        }
                        else
                        {
                            t_strValue += ";" + t_objEntry.getId();
                        }
                    }
                }
                if ( t_strValue.length() > 0 )
                {
                    t_objAnno.setIons(t_strValue.substring(1));
                }
                else
                {
                    t_objAnno.setIons("");
                }
                t_objAnno.setMass( t_objAnnoCalc.getMass() );
                if ( t_objAnnoCalc.getMass() > 0 )
                {
                    t_objAnno.setDeviation(( (t_objAnnoCalc.getMass() - t_objCalcPeak.getMz()) * 1000000.0 ) / t_objAnnoCalc.getMass());
                }
                else
                {
                    t_objAnno.setDeviation(0);
                }
                GPResidue t_objResi;
                t_strValue = "";
                ArrayList<GPResidue> t_aReidues = new ArrayList<GPResidue>();
                int t_iResidueCounter = 0;
                for (Iterator<AnnotationEntity> t_iterRes = t_objAnnoCalc.getResidues().iterator(); t_iterRes.hasNext();) 
                {
                    t_objEntry = t_iterRes.next();
                    t_objResi = new GPResidue();
                    t_objResi.setName(t_objEntry.getId());
                    t_objResi.setMax(t_objEntry.getNumber());
                    t_objResi.setMin(t_objEntry.getNumber());
                    t_strValue += String.format("%s%d",t_objEntry.getId(),t_objEntry.getNumber());
                    t_aReidues.add(t_objResi);
                    t_iResidueCounter++;
                }
                t_objAnno.setResidues(t_aReidues);    
                if ( t_objAnnoCalc.getDerivatisation() == null )
                {
                    t_objAnno.setComposition(t_strValue);
                }
                else
                {
                    if ( !t_objAnnoCalc.getDerivatisation().equals("none") )
                    {
                        t_objAnno.setComposition(t_strValue + "-" + t_strDerivate.toUpperCase());
                    }
                    else
                    {
                        t_objAnno.setComposition(t_strValue);
                    }
                }
                if ( t_objAnnoCalc.getFragments().size() == 0 )
                {
                    t_aAnnotationProfile.add(t_objAnno);
                }
                else
                {
                    t_aAnnotationFragment.add(t_objAnno);
                }
            }
            Collections.sort( t_aAnnotationFragment , t_objComparator );
            Collections.sort( t_aAnnotationProfile , t_objComparator );
            int t_iAnnoCount = 0;
            for (Iterator<GPAnnotation> t_iterAnnotation = t_aAnnotationFragment.iterator(); t_iterAnnotation.hasNext();) 
            {
                GPAnnotation t_anno = t_iterAnnotation.next();
                t_anno.setNumber(t_iAnnoCount);
                t_iAnnoCount++;    
            }
            t_iAnnoCount = 0;
            for (Iterator<GPAnnotation> t_iterAnnotation = t_aAnnotationProfile.iterator(); t_iterAnnotation.hasNext();) 
            {
                GPAnnotation t_anno = t_iterAnnotation.next();
                t_anno.setNumber(t_iAnnoCount);
                t_iAnnoCount++;    
            }            
            if ( a_objSettings.getSpectraType().equals("ms2") )
            {
                if ( t_aAnnotationProfile.size() > 0 )
                {
                    t_objAnnoPeak = new GPPeakAnnotated();
                    t_objAnnoPeak.setAnnotation( t_aAnnotationProfile );
                    t_objAnnoPeak.setAnnotationCount(t_aAnnotationProfile.size());
                    t_objAnnoPeak.setIntensity( t_objCalcPeak.getIntensity() );
                    t_objAnnoPeak.setMz( t_objCalcPeak.getMz() );
                    t_objAnnoPeak.setCharge(t_objCalcPeak.getCharge());
                    t_objAnnoPeak.setNumber(t_iCounterProfile);
                    this.m_aPrecursor.add(t_objAnnoPeak);
                    t_iCounterProfile++;
                }
                t_objAnnoPeak = new GPPeakAnnotated();
                t_objAnnoPeak.setAnnotation( t_aAnnotationFragment );
                t_objAnnoPeak.setAnnotationCount(t_aAnnotationFragment.size());
                t_objAnnoPeak.setIntensity( t_objCalcPeak.getIntensity() );
                t_objAnnoPeak.setMz( t_objCalcPeak.getMz() );
                t_objAnnoPeak.setCharge(t_objCalcPeak.getCharge());
                t_objAnnoPeak.setNumber(t_iCounterFragment);
                this.m_aAnnotatedPeaks.add(t_objAnnoPeak);
                t_iCounterFragment++;
            }
            else if ( a_objSettings.getSpectraType().equals("profile") )
            {
                t_objAnnoPeak = new GPPeakAnnotated();
                t_objAnnoPeak.setAnnotation( t_aAnnotationProfile );
                t_objAnnoPeak.setAnnotationCount(t_aAnnotationProfile.size());
                t_objAnnoPeak.setIntensity( t_objCalcPeak.getIntensity() );
                t_objAnnoPeak.setMz( t_objCalcPeak.getMz() );
                t_objAnnoPeak.setCharge(t_objCalcPeak.getCharge());
                t_objAnnoPeak.setNumber(t_iCounterProfile);
                this.m_aAnnotatedPeaks.add(t_objAnnoPeak);
                t_iCounterProfile++;
            }
            else
            {
                t_objAnnoPeak = new GPPeakAnnotated();
                t_objAnnoPeak.setAnnotation( t_aAnnotationFragment );
                t_objAnnoPeak.setAnnotationCount(t_aAnnotationFragment.size());
                t_objAnnoPeak.setIntensity( t_objCalcPeak.getIntensity() );
                t_objAnnoPeak.setMz( t_objCalcPeak.getMz() );
                t_objAnnoPeak.setCharge(t_objCalcPeak.getCharge());
                t_objAnnoPeak.setNumber(t_iCounterFragment);
                this.m_aAnnotatedPeaks.add(t_objAnnoPeak);
                t_iCounterFragment++;
            }
        }                
    }

    public void fillParameter(CalculationParameter a_objParameter)
    {
        Scan t_objScan = new Scan();
        GPPeakAnnotated t_objGPPeak;
        CalculationPeak t_objCalcPeak;
        GPAnnotation t_objAnno;
        ArrayList<CalculationPeak> t_aPeaks = new ArrayList<CalculationPeak>();
        for (Iterator<GPPeakAnnotated> t_iterPeaks = this.m_aAnnotatedPeaks.iterator(); t_iterPeaks.hasNext();) 
        {
            t_objGPPeak = t_iterPeaks.next();
            t_objCalcPeak = new CalculationPeak();
            t_objCalcPeak.setCharge(t_objGPPeak.getCharge());
            t_objCalcPeak.setIntensity(t_objGPPeak.getIntensity());
            t_objCalcPeak.setMz(t_objGPPeak.getMz());
            ArrayList<PeakAnnotation> t_aAnnos = new ArrayList<PeakAnnotation>();
            for (Iterator<GPAnnotation> t_iterAnnos = t_objGPPeak.getAnnotation().iterator(); t_iterAnnos.hasNext();) 
            {
                t_objAnno = t_iterAnnos.next();
                t_aAnnos.add(t_objAnno.getAnnotations());
            }
            t_objCalcPeak.setAnnotation(t_aAnnos);
            // precursor?
            for (Iterator<GPPeakAnnotated> t_iterPrePeak = this.m_aPrecursor.iterator(); t_iterPrePeak.hasNext();) 
            {
                GPPeakAnnotated t_objPrePeak = t_iterPrePeak.next();
                if ( t_objPrePeak.getMz() == t_objGPPeak.getMz() )
                {
                    // add annotations
                    for (Iterator<GPAnnotation> t_iterPreAnno = t_objPrePeak.getAnnotation().iterator(); t_iterPreAnno.hasNext();) 
                    {
                        GPAnnotation t_objPreAnno = t_iterPreAnno.next();
                        t_aAnnos.add(t_objPreAnno.getAnnotations());
                    }
                }
            }
            t_objCalcPeak.setAnnotationCount(t_objCalcPeak.getAnnotation().size());
            t_aPeaks.add(t_objCalcPeak);
        }        
        t_objScan.setPeaks(t_aPeaks);
        t_objScan.setPrecursorMass(this.m_dPreCursorMass);
        a_objParameter.setScan(t_objScan);
    }

    public GPAnnotation findAnnotation(int a_iPeakID, int a_iAnnotationID) 
    {
        for (Iterator<GPPeakAnnotated> t_iterPeak = this.m_aAnnotatedPeaks.iterator(); t_iterPeak.hasNext();) 
        {
            GPPeakAnnotated t_objPeak = t_iterPeak.next();
            if ( t_objPeak.getNumber() == a_iPeakID )
            {
                for (Iterator<GPAnnotation> t_iterAnno = t_objPeak.getAnnotation().iterator(); t_iterAnno.hasNext();) 
                {
                    GPAnnotation t_objAnnotation = t_iterAnno.next();
                    if ( t_objAnnotation.getNumber() == a_iAnnotationID )
                    {
                        return t_objAnnotation;
                    }
                }
                return null;
            }        
        }
        return null;
    }

    /**
     * @param major
     * @param minor
     */
    public void deleteAnnotation(int a_iPeakID, int a_iAnnotationID) 
    {
        for (Iterator<GPPeakAnnotated> t_iterPeak = this.m_aAnnotatedPeaks.iterator(); t_iterPeak.hasNext();) 
        {
            GPPeakAnnotated t_objPeak = t_iterPeak.next();
            if ( t_objPeak.getNumber() == a_iPeakID )
            {
                for (Iterator<GPAnnotation> t_iterAnno = t_objPeak.getAnnotation().iterator(); t_iterAnno.hasNext();) 
                {
                    GPAnnotation t_objAnnotation = t_iterAnno.next();
                    if ( t_objAnnotation.getNumber() == a_iAnnotationID )
                    {
                        if ( t_objPeak.getComplette() )
                        {
                            t_objPeak.getAnnotation().remove(t_objAnnotation);
                            t_objPeak.setAnnotationCount(t_objPeak.getAnnotation().size());
                        }
                        else
                        {
                            t_objPeak.getAnnotation().remove(t_objAnnotation);
                        }
                        return;
                    }
                }
                return;
            }        
        }
    }

    /**
     * @param minor
     */
    public void deletePrecursorAnnotation(int a_iPrecursor, int a_iAnnotationID) 
    {
        for (Iterator<GPPeakAnnotated> t_iterPeak = this.m_aPrecursor.iterator(); t_iterPeak.hasNext();) 
        {
            GPPeakAnnotated t_objPeak = t_iterPeak.next();
            if ( t_objPeak.getNumber() == a_iPrecursor )
            {
                for (Iterator<GPAnnotation> t_iterAnno = t_objPeak.getAnnotation().iterator(); t_iterAnno.hasNext();) 
                {
                    GPAnnotation t_objAnnotation = t_iterAnno.next();
                    if ( t_objAnnotation.getNumber() == a_iAnnotationID )
                    {
                        if ( t_objPeak.getComplette() )
                        {
                            t_objPeak.getAnnotation().remove(t_objAnnotation);
                            t_objPeak.setAnnotationCount(t_objPeak.getAnnotation().size());
                        }
                        else
                        {
                            t_objPeak.getAnnotation().remove(t_objAnnotation);
                        }
                        return;
                    }
                }
                return;
            }        
        }
    }

    /**
     * @param major
     * @param minor
     * @return
     */
    public GPAnnotation findPrecursorAnnotation(int a_iMajor, int a_iAnnotationID) 
    {
        for (Iterator<GPPeakAnnotated> t_iterPeak = this.m_aPrecursor.iterator(); t_iterPeak.hasNext();) 
        {
            GPPeakAnnotated t_objPeak = t_iterPeak.next();
            if ( t_objPeak.getNumber() == a_iMajor )
            {
                for (Iterator<GPAnnotation> t_iterAnno = t_objPeak.getAnnotation().iterator(); t_iterAnno.hasNext();) 
                {
                    GPAnnotation t_objAnnotation = t_iterAnno.next();
                    if ( t_objAnnotation.getNumber() == a_iAnnotationID )
                    {
                        return t_objAnnotation;
                    }
                }
                return null;
            }        
        }
        return null;
    }
}