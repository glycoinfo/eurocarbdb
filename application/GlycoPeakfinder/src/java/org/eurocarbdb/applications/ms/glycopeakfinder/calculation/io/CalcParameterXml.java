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
package org.eurocarbdb.applications.ms.glycopeakfinder.calculation.io;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.ParameterException;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.ParameterParsingException;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.AnnotationEntity;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationIon;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationMolecule;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationParameter;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationPeak;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationDerivatisation;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.CalculationFragment;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.PeakAnnotation;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Scan;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.SpectraType;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
* Object for loading and storing CalculationParameter from and to XML
* 
* @author rene
*/
public class CalcParameterXml
{
    private String m_strFile = "http://www.dkfz.de/spec/EuroCarbDB/applications/ms-tools/GlycoPeakfinder/schema/exchange.xsd";
    
    /**
     * Sets the URI for the schema file.
     *  
     * @param a_strFile
     */
    public void setSchema(String a_strFile)
    {
        this.m_strFile = a_strFile;
    }
    
    /**
     * Imports a parameter object from XML string
     * 
     * @param a_strXML    XML string
     * @return
     * @throws JDOMException
     * @throws IOException
     * @throws ParameterException
     * @throws ParameterParsingException
     */
    public CalculationParameter importParameter(File a_fFile) throws JDOMException, IOException, ParameterException, ParameterParsingException 
    {
        Document t_objDocument;
        t_objDocument = new SAXBuilder().build(a_fFile);
        Element t_objRoot = t_objDocument.getRootElement();
        return this.importParameter(t_objRoot);
    }
    
    /**
     * Imports a parameter object from XML string
     * 
     * @param a_strXML    XML string
     * @return
     * @throws JDOMException
     * @throws IOException
     * @throws ParameterException
     * @throws ParameterParsingException
     */
    public CalculationParameter importParameter(String a_strXML) throws JDOMException, IOException, ParameterException, ParameterParsingException 
    {
        Document t_objDocument;
        StringReader t_objReader = new StringReader(a_strXML);
        t_objDocument = new SAXBuilder().build(t_objReader);
        Element t_objRoot = t_objDocument.getRootElement();
        return this.importParameter(t_objRoot);
    }
    
    /**
     * Imports a parameter object from JDOM element (root element)
     * 
     * @param a_objRoot
     * @return
     * @throws ParameterException
     * @throws ParameterParsingException
     */
    public CalculationParameter importParameter(Element a_objRoot) throws ParameterException , ParameterParsingException
    {
        CalculationParameter t_objParamter = new CalculationParameter();
        Element t_objElement;
        Element t_objSubElement;
        String t_strValue = "";
        CalculationMolecule t_objMolecule;
        ArrayList<CalculationMolecule> t_aMolecules;
        ArrayList<CalculationMolecule> t_aMoleculesLoss;
        ArrayList<Integer> t_aInteger;
        ArrayList<CalculationIon> t_aIons;
        // persubstitition
        t_strValue = a_objRoot.getAttributeValue("persubstitution");
        if ( t_strValue != null )
        {
            t_objParamter.setPersubstitution(Persubstitution.forAbbr(t_strValue.trim()));    
        }    
        // max_annotation_per_peak
        t_strValue = a_objRoot.getAttributeValue("max_annotation_per_peak");
        if ( t_strValue != null )
        {
            t_objParamter.setMaxAnnotationPerPeak(Integer.parseInt(t_strValue));
        }
        // spectra type
        t_strValue = a_objRoot.getAttributeValue("spectra_type");
        if ( t_strValue == null )
        {
            throw new ParameterParsingException("Attribute spectra_type missing.");
        }
        t_objParamter.setSpectraType(SpectraType.forName(t_strValue.trim()));
        // mass type
        t_strValue = a_objRoot.getAttributeValue("mass_type");
        if ( t_strValue != null )
        {
            if ( t_strValue.trim().equals("average") )
            {
                t_objParamter.setMonoisotopic(false);
            }
            else
            {
                t_objParamter.setMonoisotopic(true);
            }
        }
        // accuracy
        t_strValue = a_objRoot.getAttributeValue("accuracy");
        if ( t_strValue == null )
        {
            throw new ParameterParsingException("Attribute accuracy missing.");
        }
        try
        {
            t_objParamter.setAccuracy(Double.parseDouble(t_strValue.trim()));
        } 
        catch (NumberFormatException e)
        {
            throw new ParameterParsingException("Attribute accuracy is not a float number.");
        }
        t_strValue = a_objRoot.getAttributeValue("accuracy_type");
        if ( t_strValue == null )
        {
            throw new ParameterParsingException("Attribute accuracy_type missing.");
        }
        if ( t_strValue.trim().equals("ppm") )
        {
            t_objParamter.setAccuracyPpm(true);
        }
        else
        {
            t_objParamter.setAccuracyPpm(false);
        }
        // mass shift
        t_strValue = a_objRoot.getAttributeValue("mass_shift");
        if ( t_strValue == null )
        {
            t_objParamter.setMassShift(0);
        }
        else
        {
            try
            {
                t_objParamter.setMassShift(Double.parseDouble(t_strValue.trim()));
            } 
            catch (NumberFormatException e)
            {
                throw new ParameterParsingException("Attribute mass_shift is not a float number.");
            }   
        }
        // completion reducing
        t_strValue = a_objRoot.getAttributeValue("completion_red");
        if ( t_strValue == null )
        {
            throw new ParameterParsingException("Attribute completion_red missing.");
        }
        try
        {
            t_objParamter.setCompletionRed(Double.parseDouble(t_strValue.trim()));
        } 
        catch (NumberFormatException e)
        {
            throw new ParameterParsingException("Attribute completion_red is not a float number.");
        }
        // completion non reducing
        t_strValue = a_objRoot.getAttributeValue("completion_nonred");
        if ( t_strValue == null )
        {
            throw new ParameterParsingException("Attribute completion_nonred missing.");
        }
        try
        {
            t_objParamter.setCompletionNonRed(Double.parseDouble(t_strValue.trim()));
        } 
        catch (NumberFormatException e)
        {
            throw new ParameterParsingException("Attribute completion_nonred is not a float number.");
        }
        // residue
        t_objElement = a_objRoot.getChild("residues");
        t_aMolecules = new ArrayList<CalculationMolecule>(); 
        if ( t_objElement == null )
        {
            throw new ParameterParsingException("Tag residues missing.");
        }
        for (Iterator t_iterSub = t_objElement.getChildren("residue").iterator(); t_iterSub.hasNext();)
        {
            t_objSubElement = (Element) t_iterSub.next();
            t_objMolecule = new CalculationMolecule();
            t_aMolecules.add(t_objMolecule);
            // id
            t_strValue = t_objSubElement.getAttributeValue("id");
            if ( t_strValue == null )
            {
                throw new ParameterParsingException("Attribute id for tag residue missing.");
            }
            t_objMolecule.setId(t_strValue.trim());
            // mass
            t_strValue = t_objSubElement.getAttributeValue("mass");
            if ( t_strValue == null )
            {
                throw new ParameterParsingException("Attribute mass for tag residue missing.");
            }
            try
            {
                t_objMolecule.setMass(Double.parseDouble(t_strValue.trim()));
            } 
            catch (NumberFormatException e)
            {
                throw new ParameterParsingException("Attribute mass of tag residue is not a float number.");
            }   
            // min
            t_strValue = t_objSubElement.getAttributeValue("min");
            if ( t_strValue == null )
            {
                throw new ParameterParsingException("Attribute min for tag residue missing.");
            }
            try
            {
                t_objMolecule.setMin(Integer.parseInt(t_strValue.trim()));
            } 
            catch (NumberFormatException e)
            {
                throw new ParameterParsingException("Attribute min of tag residue is not a number.");
            }
            // max
            t_strValue = t_objSubElement.getAttributeValue("max");
            if ( t_strValue == null )
            {
                throw new ParameterParsingException("Attribute max for tag residue missing.");
            }
            try
            {
                t_objMolecule.setMax(Integer.parseInt(t_strValue.trim()));
            } 
            catch (NumberFormatException e)
            {
                throw new ParameterParsingException("Attribute max of tag residue is not a number.");
            }
        }
        t_objParamter.setResidues(t_aMolecules);
        // scan
        t_objElement = a_objRoot.getChild("scan");
        if ( t_objElement == null )
        {
            throw new ParameterParsingException("Tag scan missing.");
        }
        t_objParamter.setScan(this.parseScan(t_objElement));
        // multifragment + fragments
        t_objElement = a_objRoot.getChild("fragments");
        ArrayList<CalculationFragment> t_aFragmentRed = new ArrayList<CalculationFragment>();
        ArrayList<CalculationFragment> t_aFragmentNonRed = new ArrayList<CalculationFragment>();
        if ( t_objElement != null )
        {
            for (Iterator t_iterFragm = t_objElement.getChildren("fragment").iterator(); t_iterFragm.hasNext();)
            {
                t_objSubElement = (Element) t_iterFragm.next();
                CalculationFragment t_objFrag = new CalculationFragment();
                t_strValue = t_objSubElement.getAttributeValue("id");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute id of tag fragment missing.");
                }
                t_objFrag.setId(t_strValue.trim());
                t_strValue = t_objSubElement.getAttributeValue("mass");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute mass of tag fragment missing.");
                }
                try
                {
                    t_objFrag.setMass(Double.parseDouble(t_strValue.trim()));
                } 
                catch (NumberFormatException e)
                {
                    throw new ParameterParsingException("Attribute mass of tag fragment is not a number.");
                }
                t_strValue = t_objSubElement.getAttributeValue("fragment_type");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute fragment_type of tag fragment missing.");
                }
                t_objFrag.setFragmentType(t_strValue);
                t_strValue = t_objSubElement.getAttributeValue("type");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute tpye of tag fragment missing.");
                }
                if ( t_strValue.trim().equals("reducing") )
                {
                    t_aFragmentRed.add(t_objFrag);
                }
                else if (  t_strValue.trim().equals("non-reducing") )
                {
                    t_aFragmentNonRed.add(t_objFrag);
                }
                t_strValue = t_objSubElement.getAttributeValue("residue");
                if ( t_strValue != null )
                {
                    t_objFrag.setResidueId(t_strValue.trim());
                }
            }
            t_objParamter.setFragmentsRed(t_aFragmentRed);
            t_objParamter.setFragmentsNonRed(t_aFragmentNonRed);
            // multifragments
            t_aInteger = new ArrayList<Integer>();
            for (Iterator t_iterFrag = t_objElement.getChildren("level").iterator(); t_iterFrag.hasNext();)
            {
                t_objSubElement = (Element) t_iterFrag.next();
                t_strValue = t_objSubElement.getAttributeValue("number");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute number of tag level missing.");
                }
                try
                {
                    t_aInteger.add(Integer.parseInt(t_strValue.trim()));
                } 
                catch (NumberFormatException e)
                {
                    throw new ParameterParsingException("Attribute number of tag level is not a number.");
                }
            }
            t_objParamter.setMultiFragments(t_aInteger);
            // non red abzug
            t_strValue = t_objElement.getAttributeValue("non_red_diff");
            if ( t_strValue == null )
            {
                throw new ParameterParsingException("Attribute non_red_diff missing.");
            }
            try
            {
                t_objParamter.setNonReducingDifference(Double.parseDouble(t_strValue.trim()));
            } 
            catch (NumberFormatException e)
            {
                throw new ParameterParsingException("Attribute non_red_diff is not a float number.");
            }
        }
        // ion
        t_objElement = a_objRoot.getChild("ions");
        t_aIons = new ArrayList<CalculationIon>();
        t_aInteger = new ArrayList<Integer>();
        if ( t_objElement == null )
        {
            throw new ParameterParsingException("Tag ions missing.");
        }
        for (Iterator t_iterIons = t_objElement.getChildren().iterator(); t_iterIons.hasNext();)
        {
            t_objSubElement = (Element) t_iterIons.next();
            if (t_objSubElement.getName().equals("ion") )
            {
                // ion
                // id
                t_strValue = t_objSubElement.getAttributeValue("id");
                CalculationIon t_objIon = new CalculationIon();
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute id of tag ion missing.");
                }
                t_objIon.setId(t_strValue.trim());
                // mass
                t_strValue = t_objSubElement.getAttributeValue("mass");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute mass for tag ion missing.");
                }
                try
                {
                    t_objIon.setMass(Double.parseDouble(t_strValue.trim()));
                } 
                catch (NumberFormatException e)
                {
                    throw new ParameterParsingException("Attribute mass of tag ion is not a float number.");
                }   
                // charge
                t_strValue = t_objSubElement.getAttributeValue("charge");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute charge for tag ion missing.");
                }
                try
                {
                    t_objIon.setCharge(Integer.parseInt(t_strValue.trim()));
                } 
                catch (NumberFormatException e)
                {
                    throw new ParameterParsingException("Attribute charge of tag ion is not a float number.");
                }
                t_aIons.add(t_objIon);
            }
            else if ( t_objSubElement.getName().equals("charge") )
            {
                // charge
                t_strValue = t_objSubElement.getAttributeValue("count");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute count of tag charge missing.");
                }
                try
                {
                    t_aInteger.add(Integer.parseInt(t_strValue.trim()));
                } 
                catch (NumberFormatException e)
                {
                    throw new ParameterParsingException("Attribute count of tag charge is not a number.");
                }
            }
        }
        t_objParamter.setCharges(t_aInteger);
        t_objParamter.setIons(t_aIons);
        // ion exchange
        t_objElement = a_objRoot.getChild("ionexchange");
        t_aIons = new ArrayList<CalculationIon>();
        t_aInteger = new ArrayList<Integer>();
        if ( t_objElement != null )
        {
            for (Iterator t_iterIons = t_objElement.getChildren().iterator(); t_iterIons.hasNext();)
            {
                t_objSubElement = (Element) t_iterIons.next();
                if (t_objSubElement.getName().equals("ion") )
                {
                    // ion
                    // id
                    t_strValue = t_objSubElement.getAttributeValue("id");
                    CalculationIon t_objIon = new CalculationIon();
                    if ( t_strValue == null )
                    {
                        throw new ParameterParsingException("Attribute id of tag ion missing.");
                    }
                    t_objIon.setId(t_strValue.trim());
                    // mass
                    t_strValue = t_objSubElement.getAttributeValue("mass");
                    if ( t_strValue == null )
                    {
                        throw new ParameterParsingException("Attribute mass for tag ion missing.");
                    }
                    try
                    {
                        t_objIon.setMass(Double.parseDouble(t_strValue.trim()));
                    } 
                    catch (NumberFormatException e)
                    {
                        throw new ParameterParsingException("Attribute mass of tag ion is not a float number.");
                    }   
                    // charge
                    t_strValue = t_objSubElement.getAttributeValue("charge");
                    if ( t_strValue == null )
                    {
                        throw new ParameterParsingException("Attribute charge for tag ion missing.");
                    }
                    try
                    {
                        t_objIon.setCharge(Integer.parseInt(t_strValue.trim()));
                    } 
                    catch (NumberFormatException e)
                    {
                        throw new ParameterParsingException("Attribute charge of tag ion is not a float number.");
                    }
                    t_aIons.add(t_objIon);
                }
                else if ( t_objSubElement.getName().equals("quantity") )
                {
                    // charge
                    t_strValue = t_objSubElement.getAttributeValue("count");
                    if ( t_strValue == null )
                    {
                        throw new ParameterParsingException("Attribute count of tag quantity missing.");
                    }
                    try
                    {
                        t_aInteger.add(Integer.parseInt(t_strValue.trim()));
                    } 
                    catch (NumberFormatException e)
                    {
                        throw new ParameterParsingException("Attribute count of tag quantity is not a number.");
                    }
                }
            }
            t_objParamter.setIonExchangeCount(t_aInteger);
            t_objParamter.setIonExchangeIon(t_aIons);
            // exchange ion mass
            t_strValue = t_objElement.getAttributeValue("exchange_ion_mass");
            if ( t_strValue == null )
            {
                throw new ParameterParsingException("Attribute exchange_ion_mass missing.");
            }
            try
            {
                t_objParamter.setExchangeIonMass(Double.parseDouble(t_strValue.trim()));
            } 
            catch (NumberFormatException e)
            {
                throw new ParameterParsingException("Attribute exchange_ion_mass is not a float number.");
            }
        }
        // derivative
        t_objElement = a_objRoot.getChild("derivatisation");
        ArrayList<CalculationDerivatisation> t_aDerivates = new ArrayList<CalculationDerivatisation>();
        if ( t_objElement ==  null )
        {
            throw new ParameterParsingException("Tag derivatisation missing.");
        }
        for (Iterator t_iterDeri = t_objElement.getChildren("derivative").iterator(); t_iterDeri.hasNext();)
        {
            t_objSubElement = (Element) t_iterDeri.next();
            CalculationDerivatisation t_objDerivate = new CalculationDerivatisation();
            // id
            t_strValue = t_objSubElement.getAttributeValue("id");
            if ( t_strValue == null )
            {
                throw new ParameterParsingException("Attribute id of tag derivative missing.");
            }
            t_objDerivate.setId(t_strValue);
            // mass
            t_strValue = t_objSubElement.getAttributeValue("mass");
            if ( t_strValue == null )
            {
                throw new ParameterParsingException("Attribute mass for tag derivative missing.");
            }
            try
            {
                t_objDerivate.setMass(Double.parseDouble(t_strValue.trim()));
            } 
            catch (NumberFormatException e)
            {
                throw new ParameterParsingException("Attribute mass of tag derivative is not a float number.");
            }            
            t_aDerivates.add(t_objDerivate);
        }
        t_objParamter.setDerivatisation(t_aDerivates);
        // gain / loss
        t_objElement = a_objRoot.getChild("molecules");        
        if ( t_objElement != null )
        {
            t_aMolecules = new ArrayList<CalculationMolecule>();
            t_aMoleculesLoss = new ArrayList<CalculationMolecule>();
            for (Iterator t_iterMol = t_objElement.getChildren("molecule").iterator(); t_iterMol.hasNext();)
            {
                t_objSubElement = (Element) t_iterMol.next();
                t_objMolecule = new CalculationMolecule();
                // id
                t_strValue = t_objSubElement.getAttributeValue("id");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute id for tag molecule missing.");
                }
                t_objMolecule.setId(t_strValue.trim());
                // mass
                t_strValue = t_objSubElement.getAttributeValue("mass");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute mass for tag molecule missing.");
                }
                try
                {
                    t_objMolecule.setMass(Double.parseDouble(t_strValue.trim()));
                } 
                catch (NumberFormatException e)
                {
                    throw new ParameterParsingException("Attribute mass of tag molecule is not a float number.");
                }   
                // min
                t_strValue = t_objSubElement.getAttributeValue("min");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute min for tag molecule missing.");
                }
                try
                {
                    t_objMolecule.setMin(Integer.parseInt(t_strValue.trim()));
                } 
                catch (NumberFormatException e)
                {
                    throw new ParameterParsingException("Attribute min of tag molecule is not a number.");
                }
                // max
                t_strValue = t_objSubElement.getAttributeValue("max");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute max for tag molecule missing.");
                }
                try
                {
                    t_objMolecule.setMax(Integer.parseInt(t_strValue.trim()));
                } 
                catch (NumberFormatException e)
                {
                    throw new ParameterParsingException("Attribute max of tag molecule is not a number.");
                }   
                // type
                t_strValue = t_objSubElement.getAttributeValue("type");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute type for tag molecule missing.");
                }
                if ( t_strValue.trim().equals("gain") )
                {
                    t_aMolecules.add(t_objMolecule);
                }
                else if ( t_strValue.trim().equals("loss") )
                {
                    t_aMoleculesLoss.add(t_objMolecule);                    
                }
            }
            t_objParamter.setGainMolecules(t_aMolecules);
            t_objParamter.setLossMolecules(t_aMoleculesLoss);
        }
        return t_objParamter;
    }
    
    /**
     * Imports a scan from a scan element
     * 
     * @param a_objElementScan
     * @return
     * @throws ParameterParsingException
     */
    private Scan parseScan(Element a_objElementScan) throws ParameterParsingException 
    {
        String t_strValue;
        Element t_objElement;
        Element t_objSubElement;
        Element t_objElementEntry;
        Scan t_objScan = new Scan();
        // id
        t_strValue = a_objElementScan.getAttributeValue("id");
        if ( t_strValue == null )
        {
            throw new ParameterParsingException("Attribute id of tag scan missing.");
        }
        try
        {
            t_objScan.setId(Integer.parseInt(t_strValue.trim()));
        } 
        catch (NumberFormatException e)
        {
            throw new ParameterParsingException("Attribute id of tag scan is not a number.");
        }   
        // precursor
        t_strValue = a_objElementScan.getAttributeValue("precursor");
        if ( t_strValue != null )
        {
            try
            {
                t_objScan.setPrecursorMass(Double.parseDouble(t_strValue.trim()));
            } 
            catch (NumberFormatException e)
            {
                throw new ParameterParsingException("Attribute precursor of tag scan is not a number.");
            }   
        }
        // sub scans and peaks
        ArrayList<CalculationPeak> t_aPeaks = new ArrayList<CalculationPeak>();
        ArrayList<Scan> t_aScans = new ArrayList<Scan>();
        for (Iterator t_iterChild = a_objElementScan.getChildren().iterator(); t_iterChild.hasNext();) 
        {
            t_objElement = (Element) t_iterChild.next();
            if ( t_objElement.getName().equals("scan") )
            {
                t_aScans.add(this.parseScan(t_objElement));
            }
            else if ( t_objElement.getName().equals("peak") )
            {
                CalculationPeak t_objPeak = new CalculationPeak();
                // mz
                t_strValue = t_objElement.getAttributeValue("mz");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute mz of tag peak missing.");
                }
                try
                {
                    t_objPeak.setMz(Double.parseDouble(t_strValue.trim()));
                } 
                catch (NumberFormatException e)
                {
                    throw new ParameterParsingException("Attribute mz of tag peak is not a number.");
                }        
                // intensity
                t_strValue = t_objElement.getAttributeValue("intensity");
                if ( t_strValue == null )
                {
                    throw new ParameterParsingException("Attribute intensity of tag peak missing.");
                }
                try
                {
                    t_objPeak.setIntensity(Double.parseDouble(t_strValue.trim()));
                } 
                catch (NumberFormatException e)
                {
                    throw new ParameterParsingException("Attribute intensity of tag peak is not a number.");
                }
                // charge
                t_strValue = t_objElement.getAttributeValue("charge");
                if ( t_strValue != null )
                {
                    try
                    {
                        t_objPeak.setCharge(Integer.parseInt(t_strValue.trim()));
                    } 
                    catch (NumberFormatException e)
                    {
                        throw new ParameterParsingException("Attribute charge of tag peak is not a number.");
                    }
                }
                // annotation_count
                t_strValue = t_objElement.getAttributeValue("annotation_count");
                if ( t_strValue != null )
                {
                    try
                    {
                        t_objPeak.setAnnotationCount(Integer.parseInt(t_strValue.trim()));
                    } 
                    catch (NumberFormatException e)
                    {
                        throw new ParameterParsingException("Attribute annotation_count of tag peak is not a number.");
                    }
                }
                // annotation
                ArrayList<PeakAnnotation> t_aAnnotations = new ArrayList<PeakAnnotation>();
                for (Iterator t_iterAnno = t_objElement.getChildren("annotation").iterator(); t_iterAnno.hasNext();) 
                {
                    t_objSubElement = (Element) t_iterAnno.next();
                    PeakAnnotation t_objPeakAnnotation = new PeakAnnotation();
                    // mass
                    t_strValue = t_objSubElement.getAttributeValue("mz");
                    if ( t_strValue == null )
                    {
                        throw new ParameterParsingException("Attribute mz of tag annotation missing.");
                    }
                    try
                    {
                        t_objPeakAnnotation.setMass(Double.parseDouble(t_strValue.trim()));
                    } 
                    catch (NumberFormatException e)
                    {
                        throw new ParameterParsingException("Attribute mz of tag peak is not a number.");
                    }
                    // derivatisation
                    t_strValue = t_objSubElement.getAttributeValue("derivative");
                    if ( t_strValue != null )
                    {
                        t_objPeakAnnotation.setDerivatisation(t_strValue.trim());
                    }
                    // other annotations
                    ArrayList<AnnotationEntity> t_aResidue = new ArrayList<AnnotationEntity>();
                    ArrayList<AnnotationEntity> t_aFragment = new ArrayList<AnnotationEntity>();
                    ArrayList<AnnotationEntity> t_aIon = new ArrayList<AnnotationEntity>();
                    ArrayList<AnnotationEntity> t_aIonExchange = new ArrayList<AnnotationEntity>();
                    ArrayList<AnnotationEntity> t_aGain = new ArrayList<AnnotationEntity>();
                    ArrayList<AnnotationEntity> t_aLoss = new ArrayList<AnnotationEntity>();
                    for (Iterator t_iterOtherAnnons = t_objSubElement.getChildren("entry").iterator(); t_iterOtherAnnons.hasNext();) 
                    {
                        t_objElementEntry = (Element) t_iterOtherAnnons.next();
                        AnnotationEntity t_objEntity = new AnnotationEntity();
                        // id
                        t_strValue = t_objElementEntry.getAttributeValue("id");
                        if ( t_strValue == null )
                        {
                            throw new ParameterParsingException("Attribute id of tag entry missing.");
                        }
                        t_objEntity.setId(t_strValue.trim());
                        // number
                        t_strValue = t_objElementEntry.getAttributeValue("number");
                        if ( t_strValue == null )
                        {
                            throw new ParameterParsingException("Attribute number of tag entry missing.");
                        }
                        try
                        {
                            t_objEntity.setNumber(Integer.parseInt(t_strValue.trim()));
                        } 
                        catch (NumberFormatException e)
                        {
                            throw new ParameterParsingException("Attribute number of tag entry is not a number.");
                        }
                        // type                    
                        t_strValue = t_objElementEntry.getAttributeValue("type");
                        if ( t_strValue == null )
                        {
                            throw new ParameterParsingException("Attribute type of tag entry missing.");
                        }
                        t_strValue = t_strValue.trim();
                        if ( t_strValue.equals("residue"))
                        {
                            t_aResidue.add(t_objEntity);
                        }
                        else if ( t_strValue.equals("fragment") )
                        {
                            t_aFragment.add(t_objEntity);
                        }
                        else if ( t_strValue.equals("ion") )
                        {
                            t_aIon.add(t_objEntity);
                        }
                        else if ( t_strValue.equals("ionexchange") )
                        {
                            t_aIonExchange.add(t_objEntity);
                        }
                        else if ( t_strValue.equals("gain") )
                        {
                            t_aGain.add(t_objEntity);
                        }
                        else if ( t_strValue.equals("loss") )
                        {
                            t_aLoss.add(t_objEntity);
                        }
                    }
                    t_objPeakAnnotation.setResidues(t_aResidue);
                    t_objPeakAnnotation.setFragments(t_aFragment);
                    t_objPeakAnnotation.setIons(t_aIon);
                    t_objPeakAnnotation.setIonExchange(t_aIonExchange);
                    t_objPeakAnnotation.setGain(t_aGain);
                    t_objPeakAnnotation.setLoss(t_aLoss);
                    t_aAnnotations.add(t_objPeakAnnotation);
                }
                t_objPeak.setAnnotation(t_aAnnotations);
                if ( t_objPeak.getAnnotationCount() == 0 )
                {
                    t_objPeak.setAnnotationCount(t_aAnnotations.size());
                }
                else
                {
                    if ( t_objPeak.getAnnotationCount() < t_aAnnotations.size() )
                    {
                        throw new ParameterParsingException("Attribute annotation_count is smaller than the real number of annotations.");
                    }
                }
                t_aPeaks.add(t_objPeak);
            }            
        }        
        t_objScan.setSubScan(t_aScans);
        t_objScan.setPeaks(t_aPeaks);
        return t_objScan;
    }
    
    /** 
     * Exports a parameter object to an XML string
     * 
     * @param t_objSetting
     * @return XML-string
     * @throws IOException
     */
    public String exportParameter(CalculationParameter t_objSetting) throws IOException
    {
        // Erzeugung eines XML-Dokuments
        Document t_objDocument = new Document();
        // Erzeugung des Root-XML-Elements 
        Element t_objRoot = new Element("glycopeakfinder_calculation");
        Namespace xsiNS = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");        
        t_objRoot.addNamespaceDeclaration(xsiNS);
        t_objRoot.setAttribute(new Attribute("noNamespaceSchemaLocation",this.m_strFile, xsiNS));
        // write Settings to element
        this.exportParameter(t_objRoot, t_objSetting);
        // Und jetzt haengen wir noch das Root-Element an das Dokument
        t_objDocument.setRootElement(t_objRoot);
        // Damit das XML-Dokument schoen formattiert wird holen wir uns ein Format
        Format t_objFormat = Format.getPrettyFormat();
        t_objFormat.setEncoding("iso-8859-1");
        // Erzeugung eines XMLOutputters dem wir gleich unser Format mitgeben
        XMLOutputter t_objExportXML = new XMLOutputter(t_objFormat);
        // Schreiben der XML-Datei in einen String
        StringWriter t_objWriter = new StringWriter();
        t_objExportXML.output(t_objDocument, t_objWriter );
        return t_objWriter.toString();
    }

    /**
     * Exports a parameter object to an JDOM element
     *  
     * @param a_objRoot Element to export the settings
     * @param a_objSetting 
     */
    public void exportParameter(Element a_objRoot, CalculationParameter a_objSetting)
    {
        Element t_objElement;
        Element t_objSubElement;
        // residues
        t_objElement = new Element("residues");
        CalculationMolecule t_objMolecule;
        for (Iterator<CalculationMolecule> t_iterResidues = a_objSetting.getResidues().iterator(); t_iterResidues.hasNext();)
        {
            t_objMolecule = t_iterResidues.next();
            t_objSubElement = new Element("residue");
            t_objSubElement.setAttribute("id",t_objMolecule.getId());
            t_objSubElement.setAttribute("mass",Double.toString(t_objMolecule.getMass()));
            t_objSubElement.setAttribute("min",Integer.toString(t_objMolecule.getMin()));
            t_objSubElement.setAttribute("max",Integer.toString(t_objMolecule.getMax()));
            t_objElement.addContent(t_objSubElement);
        }
        a_objRoot.addContent(t_objElement);
        // Scan
        this.exportScan(a_objRoot,a_objSetting.getScan());
        // type
        a_objRoot.setAttribute("spectra_type",a_objSetting.getSpectraType().getName());
        if ( a_objSetting.getMonoisotopic() )
        {
            t_objElement.setAttribute("mass_type","monoisotopic");
        }
        else
        {
            t_objElement.setAttribute("mass_type","average");
        }
        // persustitution
        a_objRoot.setAttribute("persubstitution",a_objSetting.getPersubstitution().getAbbr());
        // max annotation per peak
        a_objRoot.setAttribute("max_annotation_per_peak",Integer.toString(a_objSetting.getMaxAnnotationPerPeak()));
        // multi-fragment
        Integer t_iEntity;
        // reducing fragments + non reducing fragments + multi-fragment
        if ( (a_objSetting.getFragmentsNonRed().size() + a_objSetting.getFragmentsRed().size() ) > 0 )
        {
            t_objElement = new Element("fragments");
            t_objElement.setAttribute("non_red_diff",Double.toString(a_objSetting.getNonReducingDifference()));
            CalculationFragment t_objFrag;
            for (Iterator<CalculationFragment> t_iterFrag = a_objSetting.getFragmentsRed().iterator(); t_iterFrag.hasNext();)
            {
                t_objFrag = t_iterFrag.next();
                t_objSubElement = new Element("fragment");
                t_objSubElement.setAttribute("type","reducing");
                t_objSubElement.setAttribute("fragment_type",t_objFrag.getFragmentType());
                t_objSubElement.setAttribute("id",t_objFrag.getId());
                t_objSubElement.setAttribute("mass",Double.toString(t_objFrag.getMass()));
                if ( t_objFrag.getResidueId() != null )
                {
                    t_objSubElement.setAttribute("residue",t_objFrag.getResidueId());
                }
                t_objElement.addContent(t_objSubElement);
            }
            for (Iterator<CalculationFragment> t_iterFrag = a_objSetting.getFragmentsNonRed().iterator(); t_iterFrag.hasNext();)
            {
                t_objFrag = t_iterFrag.next();
                t_objSubElement = new Element("fragment");
                t_objSubElement.setAttribute("type","non-reducing");
                t_objSubElement.setAttribute("fragment_type",t_objFrag.getFragmentType());
                t_objSubElement.setAttribute("id",t_objFrag.getId());
                t_objSubElement.setAttribute("mass",Double.toString(t_objFrag.getMass()));
                if ( t_objFrag.getResidueId() != null )
                {
                    t_objSubElement.setAttribute("residue",t_objFrag.getResidueId());
                }
                t_objElement.addContent(t_objSubElement);
            }
            for (Iterator<Integer> t_iterMulti = a_objSetting.getMultiFragments().iterator(); t_iterMulti.hasNext();)
            {
                t_iEntity = t_iterMulti.next();
                t_objSubElement = new Element("level");
                t_objSubElement.setAttribute("number",t_iEntity.toString());
                t_objElement.addContent(t_objSubElement);
            }
            a_objRoot.addContent(t_objElement);
        }
        // accuracy type + value
        a_objRoot.setAttribute("accuracy",Double.toString(a_objSetting.getAccuracy()));
        if ( a_objSetting.getAccuracyPpm() )
        {
            a_objRoot.setAttribute("accuracy_type","ppm");            
        }
        else
        {
            a_objRoot.setAttribute("accuracy_type","u");
        }
        // ions + ion/charge count
        t_objElement = new Element("ions");
        CalculationIon t_objIon;
        for (Iterator<CalculationIon> t_iterIons = a_objSetting.getIons().iterator(); t_iterIons.hasNext();)
        {
            t_objIon = t_iterIons.next();
            t_objSubElement = new Element("ion");
            t_objSubElement.setAttribute("id",t_objIon.getId());
            t_objSubElement.setAttribute("mass",Double.toString(t_objIon.getMass()));
            t_objSubElement.setAttribute("charge",t_objIon.getCharge().toString());
            t_objElement.addContent(t_objSubElement);
        }
        for (Iterator<Integer> t_iterCount = a_objSetting.getCharges().iterator(); t_iterCount.hasNext();)
        {
            t_objSubElement = new Element("charge");
            t_objSubElement.setAttribute("count",t_iterCount.next().toString());
            t_objElement.addContent(t_objSubElement);
        }
        a_objRoot.addContent(t_objElement);
        // ion exchange + ion exchange count
        if ( a_objSetting.getIonExchangeIon().size() > 0 )
        {
            t_objElement = new Element("ionexchange");
            t_objElement.setAttribute("exchange_ion_mass",Double.toString(a_objSetting.getExchangeIonMass()));
            for (Iterator<CalculationIon> t_iterIons = a_objSetting.getIonExchangeIon().iterator(); t_iterIons.hasNext();)
            {
                t_objIon = t_iterIons.next();
                t_objSubElement = new Element("ion");
                t_objSubElement.setAttribute("id",t_objIon.getId());
                t_objSubElement.setAttribute("mass",Double.toString(t_objIon.getMass()));
                t_objSubElement.setAttribute("charge",t_objIon.getCharge().toString());
                t_objElement.addContent(t_objSubElement);
            }
            for (Iterator<Integer> t_iterCount = a_objSetting.getIonExchangeCount().iterator(); t_iterCount.hasNext();)
            {
                t_objSubElement = new Element("quantity");
                t_objSubElement.setAttribute("count",t_iterCount.next().toString());
                t_objElement.addContent(t_objSubElement);
            }
            a_objRoot.addContent(t_objElement);
        }
        // mass shift
        a_objRoot.setAttribute("mass_shift",Double.toString(a_objSetting.getMassShift()));
        // derivatisation
        t_objElement = new Element("derivatisation");
        CalculationDerivatisation t_objDeri;
        for (Iterator<CalculationDerivatisation> t_iterDeri = a_objSetting.getDerivatisation().iterator(); t_iterDeri.hasNext();)
        {
            t_objDeri = t_iterDeri.next();
            t_objSubElement = new Element("derivative");
            t_objSubElement.setAttribute("id",t_objDeri.getId());
            t_objSubElement.setAttribute("mass",Double.toString(t_objDeri.getMass()));
            t_objElement.addContent(t_objSubElement);
        }
        a_objRoot.addContent(t_objElement);
        // gain + loss
        t_objElement = new Element("molecules");
        for (Iterator<CalculationMolecule> t_iterMol = a_objSetting.getGainMolecules().iterator(); t_iterMol.hasNext();)
        {
            t_objMolecule = t_iterMol.next();
            t_objSubElement = new Element("molecule");
            t_objSubElement.setAttribute("type","gain");
            t_objSubElement.setAttribute("id",t_objMolecule.getId());
            t_objSubElement.setAttribute("mass",Double.toString(t_objMolecule.getMass()));
            t_objSubElement.setAttribute("min",Integer.toString(t_objMolecule.getMin()));
            t_objSubElement.setAttribute("max",Integer.toString(t_objMolecule.getMax()));
            t_objElement.addContent(t_objSubElement);
        }
        for (Iterator<CalculationMolecule> t_iterMol = a_objSetting.getLossMolecules().iterator(); t_iterMol.hasNext();)
        {
            t_objMolecule = t_iterMol.next();
            t_objSubElement = new Element("molecule");
            t_objSubElement.setAttribute("type","loss");
            t_objSubElement.setAttribute("id",t_objMolecule.getId());
            t_objSubElement.setAttribute("mass",Double.toString(t_objMolecule.getMass()));
            t_objSubElement.setAttribute("min",Integer.toString(t_objMolecule.getMin()));
            t_objSubElement.setAttribute("max",Integer.toString(t_objMolecule.getMax()));
            t_objElement.addContent(t_objSubElement);
        }
        a_objRoot.addContent(t_objElement);
        // completions
        a_objRoot.setAttribute("completion_red",Double.toString(a_objSetting.getCompletionRed()));
        a_objRoot.setAttribute("completion_nonred",Double.toString(a_objSetting.getCompletionNonRed()));
    }
    
    /**
     * Exports a scan object to an element
     * 
     * @param a_objParentElement
     * @param a_objScan
     */
    private void exportScan(Element a_objParentElement, Scan a_objScan)
    {
        Element t_objElement;
        Element t_objElementPeak;
        Element t_objElementAnnotation;
        Element t_objElementEntry;
        // scan 
        t_objElement = new Element("scan");
        t_objElement.setAttribute("id",a_objScan.getId().toString());
        if ( a_objScan.getPrecusorMass() != null )
        {
            t_objElement.setAttribute("precursor",a_objScan.getPrecusorMass().toString());
        }
        // peaks
        CalculationPeak t_objPeak;
        PeakAnnotation t_objAnnotation;
        AnnotationEntity t_objEntity;
        for (Iterator<CalculationPeak> t_iterPeak = a_objScan.getPeaks().iterator(); t_iterPeak.hasNext();)
        {
            t_objPeak = t_iterPeak.next();
            t_objElementPeak = new Element("peak");
            t_objElementPeak.setAttribute("mz",Double.toString(t_objPeak.getMz()));
            t_objElementPeak.setAttribute("intensity",Double.toString(t_objPeak.getIntensity()));
            t_objElementPeak.setAttribute("annotation_count",Integer.toString(t_objPeak.getAnnotationCount()));
            if ( t_objPeak.getCharge() != null )
            {
                t_objElementPeak.setAttribute("charge",Integer.toString(t_objPeak.getCharge()));
            }
            // annotations
            for (Iterator<PeakAnnotation> t_iterAnnotation = t_objPeak.getAnnotation().iterator(); t_iterAnnotation.hasNext();)
            {
                t_objAnnotation = t_iterAnnotation.next();
                t_objElementAnnotation = new Element("annotation");
                // residue
                for (Iterator<AnnotationEntity> t_iterResidue = t_objAnnotation.getResidues().iterator(); t_iterResidue.hasNext();)
                {
                    t_objEntity = t_iterResidue.next();
                    t_objElementEntry = new Element("entry");
                    t_objElementEntry.setAttribute("id",t_objEntity.getId());
                    t_objElementEntry.setAttribute("type","residue");
                    t_objElementEntry.setAttribute("number",Integer.toString(t_objEntity.getNumber()));
                    t_objElementAnnotation.addContent(t_objElementEntry);
                }
                // fragment
                for (Iterator<AnnotationEntity> t_iterResidue = t_objAnnotation.getFragments().iterator(); t_iterResidue.hasNext();)
                {
                    t_objEntity = t_iterResidue.next();
                    t_objElementEntry = new Element("entry");
                    t_objElementEntry.setAttribute("id",t_objEntity.getId());
                    t_objElementEntry.setAttribute("type","fragment");
                    t_objElementEntry.setAttribute("number",Integer.toString(t_objEntity.getNumber()));
                    t_objElementAnnotation.addContent(t_objElementEntry);
                }
                for (Iterator<AnnotationEntity> t_iterResidue = t_objAnnotation.getIons().iterator(); t_iterResidue.hasNext();)
                {
                    t_objEntity = t_iterResidue.next();
                    t_objElementEntry = new Element("entry");
                    t_objElementEntry.setAttribute("id",t_objEntity.getId());
                    t_objElementEntry.setAttribute("type","ion");
                    t_objElementEntry.setAttribute("number",Integer.toString(t_objEntity.getNumber()));
                    t_objElementAnnotation.addContent(t_objElementEntry);
                }
                for (Iterator<AnnotationEntity> t_iterResidue = t_objAnnotation.getIonExchange().iterator(); t_iterResidue.hasNext();)
                {
                    t_objEntity = t_iterResidue.next();
                    t_objElementEntry = new Element("entry");
                    t_objElementEntry.setAttribute("id",t_objEntity.getId());
                    t_objElementEntry.setAttribute("type","ionexchange");
                    t_objElementEntry.setAttribute("number",Integer.toString(t_objEntity.getNumber()));
                    t_objElementAnnotation.addContent(t_objElementEntry);
                }
                for (Iterator<AnnotationEntity> t_iterResidue = t_objAnnotation.getGain().iterator(); t_iterResidue.hasNext();)
                {
                    t_objEntity = t_iterResidue.next();
                    t_objElementEntry = new Element("entry");
                    t_objElementEntry.setAttribute("id",t_objEntity.getId());
                    t_objElementEntry.setAttribute("type","gain");
                    t_objElementEntry.setAttribute("number",Integer.toString(t_objEntity.getNumber()));
                    t_objElementAnnotation.addContent(t_objElementEntry);
                }
                for (Iterator<AnnotationEntity> t_iterResidue = t_objAnnotation.getLoss().iterator(); t_iterResidue.hasNext();)
                {
                    t_objEntity = t_iterResidue.next();
                    t_objElementEntry = new Element("entry");
                    t_objElementEntry.setAttribute("id",t_objEntity.getId());
                    t_objElementEntry.setAttribute("type","loss");
                    t_objElementEntry.setAttribute("number",Integer.toString(t_objEntity.getNumber()));
                    t_objElementAnnotation.addContent(t_objElementEntry);
                }
                if ( t_objAnnotation.getDerivatisation() != null )
                {
                    t_objElementAnnotation.setAttribute("derivative", t_objAnnotation.getDerivatisation());
                }
                t_objElementAnnotation.setAttribute("mz", Double.toString(t_objAnnotation.getMass()));                
                t_objElementPeak.addContent(t_objElementAnnotation);
            }
            t_objElement.addContent(t_objElementPeak);
        }
        for (Iterator<Scan> t_iterScan = a_objScan.getSubScan().iterator(); t_iterScan.hasNext();)
        {
            this.exportScan(t_objElement, t_iterScan.next());            
        }
        a_objParentElement.addContent(t_objElement);
    }
}
