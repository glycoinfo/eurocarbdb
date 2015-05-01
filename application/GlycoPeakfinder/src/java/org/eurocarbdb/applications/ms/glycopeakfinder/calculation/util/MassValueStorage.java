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


import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.ParameterException;
import org.eurocarbdb.applications.ms.glycopeakfinder.calculation.storage.Persubstitution;
import org.jdom.JDOMException;

public interface MassValueStorage {

    /**
     * Gives the mass of a derivatisation
     * 
     * @param a_strType name of derivatisation
     * @param a_enumPersubst type of persubstitution
     * @param a_bMonoisotopic true if monoisotopic mass
     * @return
     * @throws JDOMException
     * @throws ParameterException thrown if name of derivatisation or persubstitution unknown
     */
    public abstract double getDerivatisationMass(String a_strType,
            Persubstitution a_enumPersubst, boolean a_bMonoisotopic)
            throws Exception, ParameterException;

    /**
     * Gives the mass of a small molecule
     * 
     * @param a_strType name of the molecule
     * @param a_bMonoisotopic true if monoisotopic mass
     * @return
     * @throws JDOMException
     * @throws ParameterException thrown if name of the molecule is unknown
     */
    public abstract double getMoleculeMass(String a_strType,
            boolean a_bMonoisotopic) throws Exception, ParameterException;

    /**
     * Gives the mass of the completion 
     *  
     * @param a_strType "red" or "nonred"
     * @param a_objPerSubst type of persubstitution
     * @param a_bMonoisotopic true if monoisotopic mass
     * @return
     * @throws ParameterException thrown if type or persubstitution is unknown
     * @throws JDOMException
     */
    public abstract double getCompletionMass(String a_strType,
            Persubstitution a_objPerSubst, boolean a_bMonoisotopic)
            throws ParameterException, Exception;

    /**
     * Gives the mass of an H
     * 
     * @param a_bMonoisotopic
     * @return
     */
    public abstract double getMassH(boolean a_bMonoisotopic) throws ParameterException, Exception;

    /**
     * Gives the mass of an OH
     * 
     * @param a_bMonoisotopic
     * @return
     */
    public abstract double getMassOH(boolean a_bMonoisotopic) throws ParameterException, Exception;

    /**
     * Gives the mass of O
     * 
     * @param a_bMonoisotopic
     * @return
     */
    public abstract double getMassO(boolean a_bMonoisotopic) throws ParameterException, Exception;

    /**
     * Gives the mass of H2O
     * 
     * @param a_bMonoisotopic
     * @return
     */
    public abstract double getMassH2O(boolean a_bMonoisotopic) throws ParameterException, Exception;

    /**
     * Gives the mass of an ion
     * 
     * @param a_strIon name of the ion
     * @param a_bMonoisotopic true if monoisotopic
     * @return
     * @throws ParameterException thrown if name is unknown
     * @throws JDOMException
     */
    public abstract double getIonMass(String a_strIon, boolean a_bMonoisotopic)
            throws ParameterException, Exception; 

    /**
     * Gives the mass of an glycosidic fragment 
     * 
     * @param a_strType "b","c","y","z"
     * @param a_bMonoisotopic true if monoisotopic mass
     * @return
     * @throws ParameterException thrown if type of fragment is unknwon 
     */
    public abstract double getGlycosidicFragmentMass(String a_strType,
            boolean a_bMonoisotopic) throws ParameterException, Exception;

    /**
     * Gives the increment mass of a residue
     * 
     * @param a_strResidue name of the residue
     * @param a_enumPersubst type of persubstitution
     * @param a_bMonoisotopic true if monoisotopic mass
     * @return
     * @throws JDOMException
     * @throws ParameterException thrown if name of residue or persubstitution type is unknown
     */
    public abstract double getResidueMass(String a_strResidue,
            Persubstitution a_enumPersubst, boolean a_bMonoisotopic)
            throws Exception, ParameterException;

    /**
     * Gives the mass of a crossring fragment
     * 
     * @param a_strType type of fragment "A" or "X"
     * @param a_enumPersubst type of persubstitution
     * @param a_bMonoisotopic true if monoisotopic mass
     * @param a_strResidue name if the residue of the A/X fragment
     * @param a_iPosOne cleavage position one
     * @param a_iPosTwo cleavage position two
     * @return
     * @throws NumberFormatException
     * @throws JDOMException
     * @throws ParameterException thrown if type of fragment or persubstitution type is unknown
     */
    public abstract double getCrossringFragmentMass(String a_strType,
            Persubstitution a_enumPersubst, boolean a_bMonoisotopic,
            String a_strResidue, int a_iPosOne, int a_iPosTwo)
             throws ParameterException, Exception;

    /**
     * Gives the mass of a residue based fragment (NOT a or x)
     * @param a_strType
     * @param a_enumPersubst
     * @param a_bMonoisotopic
     * @param a_strResidue
     * @return
     * @throws ParameterException
     * @throws Exception
     */
    public double getResidueFragmentMass(String a_strType, Persubstitution a_enumPersubst, 
            boolean a_bMonoisotopic, String a_strResidue) 
            throws ParameterException, Exception;
    
    /**
     * Gives the mass of exchanged ion (H)
     * 
     * @param a_bMonoisotopic
     * @return
     */
    public abstract double getExchangeIonMass(boolean a_bMonoisotopic) throws ParameterException, Exception;

    /**
     * Gives the mass of the difference for non reducing fragments
     * 
     * @param a_objPersub    type of persubstitution
     * @param a_bMonoisotopic true if monoisotopic mass
     * @return
     * @throws ParameterException thrown if type of persubstitution is unknown
     * @throws JDOMException
     */
    public abstract double getNonReducingDifference(
            Persubstitution a_objPersub, boolean a_bMonoisotopic)
            throws ParameterException, Exception;

    /**
     * Gives the mass of an electron
     * 
     * @param a_bMonoisotopic
     * @return
     */
    public abstract double getMassE(boolean a_bMonoisotopic) throws ParameterException, Exception;

    public abstract double getIncrementMass(Persubstitution a_strPerSub , boolean a_bMonoIsotopic) throws ParameterException, Exception;

    /**
     * Gives the mass of the increment for A/X fragments 
     * 
     * @param a_strPerSub type of persubstitution
     * @param a_bMonoIsotopic true if monoisotopic mass
     * @return
     * @throws JDOMException
     * @throws ParameterException thrown if type of persubstitution is unknown
     */
    public abstract double getIncrementMassAX(Persubstitution a_strPerSub , boolean a_bMonoIsotopic) throws ParameterException, Exception;

}