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
package org.eurocarbdb.applications.ms.glycopeakfinder.calculation.util;

import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.ParameterException;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
* Object that gives default values for masses 
* 
* @author Logan
*/
public class DefaultMasses implements MassValueStorage 
{
    private Document m_docDefaultMasses;
    private Document m_docAX;
    
        /**
     * Constructor that creates the object from the given urls
     * 
     * @param a_defaultUrl
     * @param a_AXUrl
     * @throws JDOMException
     * @throws IOException
     */
    public DefaultMasses(URL a_defaultUrl,URL a_AXUrl) throws JDOMException, IOException 
    {        
        this.m_docAX = new SAXBuilder().build(a_AXUrl);
        this.m_docDefaultMasses = new SAXBuilder().build(a_defaultUrl);
    }   


    /**
     * Constructor that creates the object from the given xml files.
     * 
     * @param a_strDefaultFile
     * @param a_strAXFile
     * @throws JDOMException
     * @throws IOException
     */
    public DefaultMasses(String a_strDefaultFile,String a_strAXFile) throws JDOMException, IOException 
    {        
        this.m_docAX = new SAXBuilder().build(new File(a_strAXFile));
        this.m_docDefaultMasses = new SAXBuilder().build(new File(a_strDefaultFile));
    }   
    
    /**
     * Constructor that creates the object from the xml files in the jar archive
     * 
     * @throws JDOMException
     * @throws IOException
     */
    public DefaultMasses() throws JDOMException, IOException
    {
        this.m_docDefaultMasses = new SAXBuilder().build(this.getClass().getResource("/default_masses.xml"));
        this.m_docAX = new SAXBuilder().build(this.getClass().getResource("/residue_fragments.xml"));        
    }
    
    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getDerivatisationMass(java.lang.String, org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution, boolean)
     */
    public double getDerivatisationMass(String a_strType, Persubstitution a_enumPersubst, boolean a_bMonoisotopic) throws Exception, ParameterException 
    {
        XPath xpath = XPath.newInstance("/defaults/dericatisation/derivate");
        for (Iterator t_iterPersub = xpath.selectNodes( this.m_docDefaultMasses ).iterator(); t_iterPersub.hasNext();) 
        {
            Element t_objElementMain = (Element) t_iterPersub.next();
            if ( t_objElementMain.getAttributeValue("abbr").equals(a_strType) )
            {
                String t_strTag = "mass";
                if ( a_enumPersubst != Persubstitution.None )
                {
                    t_strTag += "_" + a_enumPersubst.getAbbr();
                }
                if ( a_bMonoisotopic )
                {
                    t_strTag += "_mono";
                }
                else
                {
                    t_strTag += "_avg";
                }
                Element t_objElementMass = t_objElementMain.getChild(t_strTag);
                return Double.parseDouble(t_objElementMass.getTextTrim());
            }
        }
        throw new ParameterException("Unknown derivatisation.");
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getMoleculeMass(java.lang.String, boolean)
     */
    public double getMoleculeMass(String a_strType, boolean a_bMonoisotopic) throws Exception, ParameterException 
    {
        XPath xpath = XPath.newInstance("/defaults/molecules/small_molecules");
        for (Iterator t_iterPersub = xpath.selectNodes( this.m_docDefaultMasses ).iterator(); t_iterPersub.hasNext();) 
        {
            Element t_objElementMain = (Element) t_iterPersub.next();
            if ( t_objElementMain.getChild("name").getTextTrim().equals(a_strType) )
            {
                Element t_objElementMass = null;
                if ( a_bMonoisotopic ) 
                {
                    t_objElementMass = t_objElementMain.getChild("mass_mono");
                }
                else
                {
                    t_objElementMass = t_objElementMain.getChild("mass_avg");
                }
                return Double.parseDouble(t_objElementMass.getTextTrim());
            }
        }
        throw new ParameterException("Unknown small molecule.");
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getCompletionMass(java.lang.String, org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution, boolean)
     */
    public double getCompletionMass(String a_strType, Persubstitution a_objPerSubst, boolean a_bMonoisotopic) throws ParameterException, Exception
    {
        XPath xpath = XPath.newInstance("/defaults/persubstitutions/persubstitution");
        for (Iterator t_iterPersub = xpath.selectNodes( this.m_docDefaultMasses ).iterator(); t_iterPersub.hasNext();) 
        {
            Element t_objElementPersub = (Element) t_iterPersub.next();
            if ( t_objElementPersub.getChild("name").getTextTrim().equals(a_objPerSubst.getAbbr()) )
            {
                Element t_objElementMass = null;
                if ( a_bMonoisotopic ) 
                {
                    if ( a_strType.equals("red") )
                    {
                        t_objElementMass = t_objElementPersub.getChild("ergaenzung_red_mono");                        
                    }
                    else if ( a_strType.equals("nonred") )
                    {
                        t_objElementMass = t_objElementPersub.getChild("ergaenzung_nonred_mono");
                    }
                    else if ( a_strType.equals("profile") )
                    {
                        t_objElementMass = t_objElementPersub.getChild("mono");
                    }                    
                }
                else
                {
                    if ( a_strType.equals("red") )
                    {
                        t_objElementMass = t_objElementPersub.getChild("ergaenzung_red_avg");                        
                    }
                    else if ( a_strType.equals("nonred") )
                    {
                        t_objElementMass = t_objElementPersub.getChild("ergaenzung_nonred_avg");
                    }
                    else if ( a_strType.equals("profile") )
                    {
                        t_objElementMass = t_objElementPersub.getChild("avg");
                    }                    
                }
                if ( t_objElementMass == null )
                {
                    throw new ParameterException("Unknown completion type.");
                }
                if ( a_strType.equals("profile") )
                {
                    return ( Double.parseDouble(t_objElementMass.getTextTrim()) - this.getMassH(a_bMonoisotopic));
                }
                else
                {
                    return Double.parseDouble(t_objElementMass.getTextTrim());
                }
            }
        }
        throw new ParameterException("Unknown persubstitution type.");
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getMassH(boolean)
     */
    public double getMassH(boolean a_bMonoisotopic) throws ParameterException, Exception
    {
        if ( a_bMonoisotopic )
        {
            return Double.parseDouble("1.007825");
        }
        else
        {
            return Double.parseDouble("1.007947");
        }
    }
    
    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getMassOH(boolean)
     */
    public double getMassOH(boolean a_bMonoisotopic) throws ParameterException, Exception
    {
        if ( a_bMonoisotopic )
        {
            return Double.parseDouble("17.0027396");
        }
        else
        {
            return Double.parseDouble("17.007377");
        }
    }
    
    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getMassO(boolean)
     */
    public double getMassO(boolean a_bMonoisotopic) throws ParameterException, Exception
    {
        if ( a_bMonoisotopic )
        {
            return Double.parseDouble("15.9949146");
        }
        else
        {
            return Double.parseDouble("15.99943");
        }
    }
    
    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getMassH2O(boolean)
     */
    public double getMassH2O(boolean a_bMonoisotopic) throws ParameterException, Exception
    {
        if ( a_bMonoisotopic )
        {
            return Double.parseDouble("18.0106646");
        }
        else
        {
            return Double.parseDouble("18.015324");
        }
    }
    
    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getIonMass(java.lang.String, boolean)
     */
    public double getIonMass(String a_strIon, boolean a_bMonoisotopic) throws ParameterException, Exception 
    {
        XPath xpath = XPath.newInstance("/defaults/ions/ion");
        for (Iterator t_iterIon = xpath.selectNodes( this.m_docDefaultMasses ).iterator(); t_iterIon.hasNext();) 
        {
            Element t_objElementMain = (Element) t_iterIon.next();
            if ( t_objElementMain.getChild("formula").getTextTrim().equals(a_strIon) )
            {
                Element t_objElementMass = null;
                if ( a_bMonoisotopic ) 
                {
                    t_objElementMass = t_objElementMain.getChild("mass_mono");
                }
                else
                {
                    t_objElementMass = t_objElementMain.getChild("mass_avg");
                }
                return (Double.parseDouble(t_objElementMass.getTextTrim()) - this.getMassE(a_bMonoisotopic));
            }
        }
        throw new ParameterException("Unknown ion.");
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getGlycosidicFragmentMass(java.lang.String, boolean)
     */
    public double getGlycosidicFragmentMass(String a_strType, boolean a_bMonoisotopic) throws ParameterException, Exception
    {
        if ( a_strType.equalsIgnoreCase("y"))
        {
            return this.getMassH(a_bMonoisotopic);
        }
        if ( a_strType.equalsIgnoreCase("z"))
        {
            return 0 - this.getMassOH(a_bMonoisotopic);
        }
        if ( a_strType.equalsIgnoreCase("c"))
        {
            return this.getMassOH(a_bMonoisotopic);
        }
        if ( a_strType.equalsIgnoreCase("b"))
        {
            return 0 - this.getMassH(a_bMonoisotopic);
        }    
        throw new ParameterException("Unknown fragment type.");
    }
    
    /**
     * Gives the mass of the increment 
     * 
     * @param a_strPerSub type of persubstitution
     * @param a_bMonoIsotopic true if monoisotopic mass
     * @return
     * @throws JDOMException
     * @throws ParameterException thrown if type of persubstitution is unknown
     */
    public double getIncrementMass(Persubstitution a_strPerSub , boolean a_bMonoIsotopic) throws ParameterException, Exception
    {
        XPath xpath = XPath.newInstance("/defaults/persubstitutions/persubstitution");
        for (Iterator t_iterPersub = xpath.selectNodes( this.m_docDefaultMasses ).iterator(); t_iterPersub.hasNext();) 
        {
            Element t_objElementPersub = (Element) t_iterPersub.next();
            if ( t_objElementPersub.getChild("name").getTextTrim().equals(a_strPerSub.getAbbr()) )
            {
                Element t_objElementMass = null;
                if ( a_bMonoIsotopic )
                {
                    t_objElementMass = t_objElementPersub.getChild("increment_mono");
                }
                else
                {
                    t_objElementMass = t_objElementPersub.getChild("increment_avg");
                }
                return Double.parseDouble(t_objElementMass.getTextTrim());
            }
        }
        throw new ParameterException("Unknown persubstitution.");
    }

    /**
     * Gives the mass of the increment for A/X fragments 
     * 
     * @param a_strPerSub type of persubstitution
     * @param a_bMonoIsotopic true if monoisotopic mass
     * @return
     * @throws JDOMException
     * @throws ParameterException thrown if type of persubstitution is unknown
     */
    public double getIncrementMassAX(Persubstitution a_strPerSub , boolean a_bMonoIsotopic) throws ParameterException, Exception
    {
        XPath xpath = XPath.newInstance("/defaults/persubstitutions/persubstitution");
        for (Iterator t_iterPersub = xpath.selectNodes( this.m_docDefaultMasses ).iterator(); t_iterPersub.hasNext();) 
        {
            Element t_objElementPersub = (Element) t_iterPersub.next();
            if ( t_objElementPersub.getChild("name").getTextTrim().equals(a_strPerSub.getAbbr()) )
            {
                if ( a_bMonoIsotopic )
                {
                    return ( Double.parseDouble(t_objElementPersub.getChild("increment_mono").getTextTrim()) 
                            - Double.parseDouble(t_objElementPersub.getChild("mono").getTextTrim()));
                }
                else
                {
                    return ( Double.parseDouble(t_objElementPersub.getChild("increment_avg").getTextTrim()) 
                            - Double.parseDouble(t_objElementPersub.getChild("avg").getTextTrim()));
                }
            }
        }
        throw new ParameterException("Unknown persubstitution.");
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getResidueMass(java.lang.String, org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution, boolean)
     */
    public double getResidueMass(String a_strResidue, Persubstitution a_enumPersubst, boolean a_bMonoisotopic) throws ParameterException, Exception 
    {
        String t_strKey = "mass_";
        if ( a_enumPersubst == Persubstitution.Me )
        {
            t_strKey += "pm_";
        }
        else if ( a_enumPersubst == Persubstitution.DMe ) 
        {
            t_strKey += "pdm_";
            
        }
        else if ( a_enumPersubst == Persubstitution.Ac ) 
        {
            t_strKey += "pac_";
            
        }
        else if ( a_enumPersubst == Persubstitution.DAc ) 
        {
            t_strKey += "pdac_";
            
        }            
        if ( a_bMonoisotopic )
        {
            t_strKey += "mono";
        }
        else
        {
            t_strKey += "avg";
        }
        XPath xpath = XPath.newInstance("/defaults/residues/residue");
        for (Iterator t_iterPersub = xpath.selectNodes( this.m_docDefaultMasses ).iterator(); t_iterPersub.hasNext();) 
        {
            Element t_objElementPersub = (Element) t_iterPersub.next();
            if ( t_objElementPersub.getChild("abbr").getTextTrim().equals(a_strResidue.trim()) )
            {
                double t_dResult = Double.parseDouble(t_objElementPersub.getChild(t_strKey).getText());
                if ( t_objElementPersub.getChild("increment").getTextTrim().equals("0") )
                {
                    // calculate increment
                    t_dResult -= this.getIncrementMass(a_enumPersubst, a_bMonoisotopic);
                }
                return t_dResult;
            }
        }    
        throw new ParameterException("Unknown residue.");
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getCrossringFragmentMass(java.lang.String, org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution, boolean, java.lang.String, int, int)
     */
    public double getCrossringFragmentMass(String a_strType, Persubstitution a_enumPersubst, boolean a_bMonoisotopic, String a_strResidue, int a_iPosOne, int a_iPosTwo ) throws ParameterException, Exception
    {
        String t_strKey = "mass_";
        if ( a_enumPersubst == Persubstitution.Me )
        {
            t_strKey += "pm_";
        }
        else if ( a_enumPersubst == Persubstitution.DMe ) 
        {
            t_strKey += "pdm_";
            
        }
        else if ( a_enumPersubst == Persubstitution.Ac ) 
        {
            t_strKey += "pac_";
            
        }
        else if ( a_enumPersubst == Persubstitution.DAc ) 
        {
            t_strKey += "pdac_";
            
        }            
        if ( a_bMonoisotopic )
        {
            t_strKey += "mono";
        }
        else
        {
            t_strKey += "avg";
        }
        XPath xpath = XPath.newInstance("/fragments/fragment_ax");
        for (Iterator t_iterAX = xpath.selectNodes( this.m_docAX ).iterator(); t_iterAX.hasNext();) 
        {
            Element t_objElementPersub = (Element) t_iterAX.next();
            if ( t_objElementPersub.getChild("residue_id").getTextTrim().equals(a_strResidue) )
            {
                // right residue
                if ( t_objElementPersub.getChild("type").getTextTrim().equals(a_strType) )
                {
                    // right Type
                    int t_iPos = Integer.parseInt(t_objElementPersub.getChild("cleav_one").getTextTrim());
                    if ( t_iPos == a_iPosOne )
                    {
                        // right Start position
                        t_iPos = Integer.parseInt(t_objElementPersub.getChild("cleav_two").getTextTrim());
                        if ( t_iPos == a_iPosTwo )
                        {
                            // right end position
                            double t_dMass = Double.parseDouble(t_objElementPersub.getChild(t_strKey).getTextTrim());
                            if ( a_strType.equalsIgnoreCase("A") ) 
                            {    
                                t_dMass = t_dMass 
                                    - this.getIncrementMassAX(a_enumPersubst, a_bMonoisotopic) 
                                    + this.getMassOH(a_bMonoisotopic);
                            }
                            else
                            {
                                t_dMass = t_dMass 
                                    - this.getIncrementMassAX(a_enumPersubst, a_bMonoisotopic)
                                    + this.getMassH(a_bMonoisotopic);
                            }
                            return t_dMass;
                        }
                    }
                }                
            }            
        }
        throw new ParameterException("Unknown fragment.");
    }

    public double getResidueFragmentMass(String a_strType, Persubstitution a_enumPersubst, boolean a_bMonoisotopic, String a_strResidue) throws ParameterException, Exception
    {
        String t_strKey = "mass_";
        if ( a_enumPersubst == Persubstitution.Me )
        {
            t_strKey += "pm_";
        }
        else if ( a_enumPersubst == Persubstitution.DMe ) 
        {
            t_strKey += "pdm_";
            
        }
        else if ( a_enumPersubst == Persubstitution.Ac ) 
        {
            t_strKey += "pac_";
            
        }
        else if ( a_enumPersubst == Persubstitution.DAc ) 
        {
            t_strKey += "pdac_";
            
        }            
        if ( a_bMonoisotopic )
        {
            t_strKey += "mono";
        }
        else
        {
            t_strKey += "avg";
        }
        XPath xpath = XPath.newInstance("/fragments/fragment");
        for (Iterator t_iterAX = xpath.selectNodes( this.m_docAX ).iterator(); t_iterAX.hasNext();) 
        {
            Element t_objElementPersub = (Element) t_iterAX.next();
            if ( t_objElementPersub.getChild("residue_id").getTextTrim().equals(a_strResidue) )
            {
                // right residue
                if ( t_objElementPersub.getChild("type").getTextTrim().equals(a_strType) )
                {
                    // right end position
                    double t_dMass = Double.parseDouble(t_objElementPersub.getChild(t_strKey).getTextTrim());
                    if ( a_strType.equalsIgnoreCase("E") ) 
                    {    
                        t_dMass = t_dMass 
                        - this.getIncrementMassAX(a_enumPersubst, a_bMonoisotopic) 
                        + this.getMassOH(a_bMonoisotopic);
                    }
                    else
                    {
                        t_dMass = t_dMass 
                        - this.getIncrementMassAX(a_enumPersubst, a_bMonoisotopic)
                        + this.getMassH(a_bMonoisotopic);
                    }
                    return t_dMass;
                }
            }            
        }
        throw new ParameterException("Unknown fragment.");
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getExchangeIonMass(boolean)
     */
    public double getExchangeIonMass(boolean a_bMonoisotopic) throws ParameterException, Exception
    {
        return this.getMassH(a_bMonoisotopic);
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getNonReducingDifference(org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution, boolean)
     */
    public double getNonReducingDifference(Persubstitution a_objPersub, boolean a_bMonoisotopic) throws ParameterException, Exception
    {
        XPath xpath = XPath.newInstance("/defaults/persubstitutions/persubstitution");
        for (Iterator t_iterPersub = xpath.selectNodes( this.m_docDefaultMasses ).iterator(); t_iterPersub.hasNext();) 
        {
            Element t_objElementPersub = (Element) t_iterPersub.next();
            if ( t_objElementPersub.getChild("name").getTextTrim().equals(a_objPersub.getAbbr()) )
            {
                Element t_objElementMass = null;
                if ( a_bMonoisotopic ) 
                {
                    t_objElementMass = t_objElementPersub.getChild("ergaenzung_nonred_mono");
                }
                else
                {
                    t_objElementMass = t_objElementPersub.getChild("ergaenzung_nonred_avg");
                }
                if ( t_objElementMass == null )
                {
                    throw new ParameterException("Unknown persubstitution type.");
                }
                return Double.parseDouble(t_objElementMass.getTextTrim());
            }
        }
        throw new ParameterException("Unknown persubstitution type.");
    }
    
    /* (non-Javadoc)
     * @see org.eurocarbdb.applications.ms.glycopeakfinder.calculation.MassValueStorage#getMassE(boolean)
     */
    public double getMassE(boolean a_bMonoisotopic) throws ParameterException, Exception
    {
        if ( a_bMonoisotopic )
        {
            return Double.parseDouble("0.0005486");
        }
        else
        {
            return Double.parseDouble("0.0005486");
        }
    }
}