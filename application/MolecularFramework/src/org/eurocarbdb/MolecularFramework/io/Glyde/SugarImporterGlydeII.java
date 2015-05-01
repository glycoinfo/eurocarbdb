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
package org.eurocarbdb.MolecularFramework.io.Glyde;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eurocarbdb.MolecularFramework.io.MonosaccharideBuilder;
import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.LinkageType;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.SubstituentType;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
* @author Logan
*
*/
public class SugarImporterGlydeII extends SugarImporter 
{
    private Element m_objRoot = null;
    private Document m_objDocument = null;
    private HashMap<String,GlycoNode> m_hashResidues = new HashMap<String,GlycoNode>(); 
    private ArrayList<Sugar> m_aSugars = new ArrayList<Sugar>(); 
    private ArrayList<GlydeLinkage> m_aLinkages = new ArrayList<GlydeLinkage>(); 
    
    public Sugar parse(String a_strXML)  throws SugarImporterException
    {
        this.m_aSugars.clear();
        SAXBuilder builder = new SAXBuilder();
        try 
        {
            this.m_objDocument = builder.build(new StringReader(a_strXML));
            if (builder.getValidation())
            {
                throw new SugarImporterException("XML Validation error");
            }
            // bis molecule gehen
            List t_lMainElements = this.m_objDocument.getRootElement().getChildren();            
            for (Iterator t_iterElements = t_lMainElements.iterator(); t_iterElements.hasNext();) 
            {
                Element t_objMain = (Element) t_iterElements.next();
                if ( t_objMain.getName().equals("molecule") )
                {
                    String t_strType = t_objMain.getAttributeValue("subtype"); 
                    if ( t_strType != null )
                    {
                        if ( t_strType.equals("glycan") )
                        {
                            this.parse(t_objMain);
                        }
                        this.m_aSugars.add(this.m_objSugar);
                    }                    
                }
            }
            if ( this.m_aSugars.size() != 1)
            {
                return null;         
            }
            else
            {
                return this.m_aSugars.get(0);
            }
        } 
        catch (JDOMException e) 
        {
            throw new SugarImporterException(e.getMessage(),e);
        }
        catch (IOException e) 
        {
            throw new SugarImporterException(e.getMessage(),e);
        } 
        catch (NumberFormatException e) 
        {
            throw new SugarImporterException(e.getMessage(),e);
        }
    }
    
    public Sugar parse(Element a_objRoot)  throws SugarImporterException
    {
        try 
        {
            this.clear();
            this.m_objSugar = new Sugar();
            this.m_objRoot = a_objRoot;

            if ( !a_objRoot.getName().equals("molecule") )
            {
                throw new SugarImporterException("Importer can only read molecule tags.");
            }
            String t_strType = a_objRoot.getAttributeValue("subtype"); 
            if ( t_strType == null )
            {
                throw new SugarImporterException("Molecule tag must have a subtype=glycan attribute.");
            }                    
            if ( !t_strType.equals("glycan") )
            {
                throw new SugarImporterException("Molecule tag must have a subtype=glycan attribute.");
            }
            List t_lMainElements = this.m_objRoot.getChildren();            
            for (Iterator t_iterElements = t_lMainElements.iterator(); t_iterElements.hasNext();) 
            {
                Element t_objMain = (Element) t_iterElements.next();
                if (t_objMain.getName().equals("residue"))
                {
                    this.parseResidueEntry(t_objMain);
                }
                else if (t_objMain.getName().equals("residue_link"))
                {
                    this.parseLinkageEntry(t_objMain);
                }
                else if (t_objMain.getName().equals("repeat_block"))
                {
                    this.parseRepeatEntry(t_objMain);
                }
                else if (t_objMain.getName().equals("combination"))
                {
                    this.parseCombinationEntry(t_objMain);
                }
            }            
            this.buildSugar();
            return this.m_objSugar;
        }
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException(e.getMessage(),e);
        }
        catch (JDOMException e) 
        {
            throw new SugarImporterException(e.getMessage(),e);
        }
    }

    private void clear() 
    {
        this.m_objRoot = null;
        this.m_objDocument = null;
        this.m_hashResidues.clear();
        this.m_aLinkages.clear();
    }

    private void parseResidueEntry(Element a_objMainElement) throws GlycoconjugateException, JDOMException, SugarImporterException 
    {
        String t_strSubtype = a_objMainElement.getAttributeValue("subtype");
        String t_strName = a_objMainElement.getAttributeValue("ref");
        String t_strID = a_objMainElement.getAttributeValue("partid");
        int t_iPosition = t_strName.lastIndexOf('=');
        GlycoNode t_objResidue;
        if ( t_strSubtype == null || t_strName == null || t_strID == null )
        {
            throw new SugarImporterException("Error in XML format (subtype, ref or partid missing).");
        }
        if ( t_strSubtype.equals("substituent") )
        {
            if ( t_iPosition > -1 )
            {
                t_strName = t_strName.substring(t_iPosition + 1);
            }
            t_objResidue = new Substituent(SubstituentType.forName(t_strName));
        }
        else if ( t_strSubtype.equals("base_type") )
        {
            if ( t_iPosition > -1 )
            {
                t_strName = t_strName.substring(t_iPosition + 1);
            }
            t_objResidue = MonosaccharideBuilder.fromGlycoCT(t_strName);
            if ( t_objResidue == null )
            {
                throw new SugarImporterException("The name of the base_type is not in GlycoCT format.");
            }
        }
        else
        {
            throw new SugarImporterException("Only substituent and base_type subtypes are supportet in Glycan molecules.");
        }
        if ( this.m_hashResidues.get(t_strID) != null )
        {
            throw new SugarImporterException("Dublicated partid in glycan.");
        }
        this.m_hashResidues.put(t_strID,t_objResidue);
    }

    /**
     * @param main
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void parseLinkageEntry(Element a_objLinkage) throws SugarImporterException, GlycoconjugateException 
    {
        GlydeLinkage t_objLinkage = new GlydeLinkage();
        t_objLinkage.m_strFromID = a_objLinkage.getAttributeValue("from");
        t_objLinkage.m_strToID = a_objLinkage.getAttributeValue("to");
        if ( t_objLinkage.m_strToID == null || t_objLinkage.m_strFromID == null )
        {
            throw new SugarImporterException("Missing from or to attribute in residue_link.");
        }
        String t_strTemp = a_objLinkage.getAttributeValue("stat");
        if ( t_strTemp != null )
        {
            try
            {
                t_objLinkage.m_dStatistical = Double.parseDouble(t_strTemp);
                throw new SugarImporterException("Statistical distribution not supported at the moment.");
            } 
            catch (NumberFormatException  e)
            {
                throw new SugarImporterException(e.getMessage(),e);
            }            
        }
        t_objLinkage.m_objLinkage = new GlycoEdge();
        List t_lMainElements = a_objLinkage.getChildren();            
        for (Iterator t_iterElements = t_lMainElements.iterator(); t_iterElements.hasNext();) 
        {
            Element t_objAtomLink = (Element) t_iterElements.next();
            if (t_objAtomLink.getName().equals("atom_link"))
            {
                Linkage t_objLinkageObject = new Linkage();
                t_strTemp = t_objAtomLink.getAttributeValue("bond_order");
                if ( t_strTemp != null )
                {
                    if ( !t_strTemp.equals("1") )
                    {
                        throw new SugarImporterException("Only bondorder 1 is allowed for glycosidic linkages.");
                    }
                }
                String t_strFrom = t_objAtomLink.getAttributeValue("from");
                String t_strTo = t_objAtomLink.getAttributeValue("to");
                String t_strFromReplace = t_objAtomLink.getAttributeValue("from_replaces");
                String t_strToReplace = t_objAtomLink.getAttributeValue("to_replaces");
                if ( t_strFrom == null || t_strTo == null )
                {
                    throw new SugarImporterException("Missing from and/or to attribute in residue_link.");
                }
                t_objLinkageObject.setChildLinkages( this.getFromLinkages(t_strFrom) );
                t_objLinkageObject.setParentLinkages( this.getToLinkages(t_strTo) );
                t_objLinkageObject.setChildLinkageType( this.getFromLinkageType(t_strFrom,t_strFromReplace,t_strToReplace));
                t_objLinkageObject.setParentLinkageType( this.getToLinkageType(t_strTo,t_strFromReplace,t_strToReplace));
                t_objLinkage.m_objLinkage.addGlycosidicLinkage(t_objLinkageObject);
            }
        }    
        this.m_aLinkages.add(t_objLinkage);
    }

    /**
     * @param from
     * @param fromReplace
     * @param toReplace
     * @return
     * @throws SugarImporterException 
     */
    private LinkageType getToLinkageType(String a_strTo, String a_strFromReplace, String a_strToReplace) throws SugarImporterException
    {
        String t_strBefore = "";
        int t_iPosition;
        t_iPosition = this.findNumber( a_strTo );
        if (t_iPosition == -1 )
        {
            throw new SugarImporterException("Linkageposition of to is not valid.");
        }
        t_strBefore = a_strTo.substring(0,t_iPosition);
        if ( t_strBefore.equals("C") )
        {
            return LinkageType.DEOXY;
        }    
        else if ( t_strBefore.equals("O") )
        {
            return LinkageType.H_AT_OH;
        }
        else if ( t_strBefore.equals("X") )
        {
            return LinkageType.NONMONOSACCHARID;
        }
        else
        {
            throw new SugarImporterException("Unknown linkage atom (only C,O,X allowed in this version).");
        }
    }

    /**
     * @param from
     * @param fromReplace
     * @param toReplace
     * @return
     * @throws SugarImporterException 
     */
    private LinkageType getFromLinkageType(String a_strFrom, String a_strFromReplace, String a_strToReplace) throws SugarImporterException
    {
        String t_strBefore = "";
        int t_iPosition;
        t_iPosition = this.findNumber( a_strFrom );
        if (t_iPosition == -1 )
        {
            throw new SugarImporterException("Linkageposition of to is not valid.");
        }
        t_strBefore = a_strFrom.substring(0,t_iPosition);
        if ( t_strBefore.equals("C") )
        {
            return LinkageType.DEOXY;
        }    
        else if ( t_strBefore.equals("O") )
        {
            return LinkageType.H_AT_OH;
        }
        else if ( t_strBefore.equals("X") )
        {
            return LinkageType.NONMONOSACCHARID;
        }
        else
        {
            throw new SugarImporterException("Unknown linkage atom (only C,O,X allowed in this version).");
        }
    }

    /**
     * @param to
     * @return
     * @throws SugarImporterException 
     */
    private ArrayList<Integer> getToLinkages(String a_strTo) throws SugarImporterException
    {
        ArrayList<Integer> t_aPositions = new ArrayList<Integer>(); 
        String[] a_aParts = a_strTo.split("\\|");
        String t_strBefore = "";
        String t_strAfter = "";
        String t_strType = null;
        int t_iPosition;
        for (int t_iCounter = 0; t_iCounter < a_aParts.length; t_iCounter++)
        {
            t_iPosition = this.findNumber( a_aParts[t_iCounter] );
            if (t_iPosition == -1 )
            {
                throw new SugarImporterException("Linkageposition of to is not valid.");
            }
            t_strBefore = a_aParts[t_iCounter].substring(0,t_iPosition);
            t_strAfter = a_aParts[t_iCounter].substring(t_iPosition);
            if ( t_strType == null )
            {
                t_strType = t_strBefore;
            }
            if ( !t_strType.equals(t_strBefore) )
            {
                throw new SugarImporterException("Different LinkageTypes for a atom_link are not supported.");
            }
            try
            {
                t_aPositions.add( Integer.parseInt(t_strAfter) );
            }
            catch (NumberFormatException e) 
            {
                throw new SugarImporterException("Linkageposition to is not a valid number.");
            }                        
        }
        return t_aPositions;
    }

    /**
     * @param from
     * @return
     * @throws SugarImporterException 
     */
    private ArrayList<Integer> getFromLinkages(String a_strFrom) throws SugarImporterException
    {
        ArrayList<Integer> t_aPositions = new ArrayList<Integer>(); 
        String[] a_aParts = a_strFrom.split("\\|");
        String t_strBefore = "";
        String t_strAfter = "";
        String t_strType = null;
        int t_iPosition;
        for (int t_iCounter = 0; t_iCounter < a_aParts.length; t_iCounter++)
        {
            t_iPosition = this.findNumber( a_aParts[t_iCounter] );
            if (t_iPosition == -1 )
            {
                throw new SugarImporterException("Linkageposition of from is not valid.");
            }
            t_strBefore = a_aParts[t_iCounter].substring(0,t_iPosition);
            t_strAfter = a_aParts[t_iCounter].substring(t_iPosition);
            if ( t_strType == null )
            {
                t_strType = t_strBefore;
            }
            if ( !t_strType.equals(t_strBefore) )
            {
                throw new SugarImporterException("Different LinkageTypes for a atom_link are not supported.");
            }
            try
            {
                t_aPositions.add( Integer.parseInt(t_strAfter) );
            }
            catch (NumberFormatException e) 
            {
                throw new SugarImporterException("Linkageposition from is not a valid number.");
            }                        
        }
        return t_aPositions;
    }

    /**
     * @param string
     * @return
     */
    private int findNumber(String a_strText)
    {
        int t_iLength = a_strText.length();
        for (int t_iCounter = 0; t_iCounter < t_iLength; t_iCounter++)
        {
            int t_iDigit = (int)a_strText.charAt(t_iCounter);
            if ( t_iDigit > 47 && t_iDigit < 58 )
            {
                return t_iCounter;
            }
            if ( a_strText.charAt(t_iCounter) == '-' )
            {
                return t_iCounter;
            }
        }
        return -1;
    }

    /**
     * @param residue
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void parseRepeatEntry(Element a_objRepeat) throws SugarImporterException, GlycoconjugateException 
    {
        throw new SugarImporterException("Repeat units are not supported in this version of the Translator.");
    }

    /**
     * @param residue
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void parseCombinationEntry(Element a_objCombination) throws SugarImporterException, GlycoconjugateException 
    {
        throw new SugarImporterException("Combinations are not supported in this version of the Translator.");
    }
    
    private void buildSugar() throws SugarImporterException, GlycoconjugateException 
    {
//        // create ArrayList of all residues
//        ArrayList<String> t_aResidues = new ArrayList<String>(); 
//        for (Iterator<String> t_iterResidues = this.m_hashResidues.keySet().iterator(); t_iterResidues.hasNext();) 
//        {
//            t_aResidues.add(t_iterResidues.next());            
//        }
//        // delete all non root residues
//        for (Iterator<GlydeLinkage> t_iterLinkages = this.m_aLinkages.iterator(); t_iterLinkages.hasNext();) 
//        {
//            GlydeLinkage t_objLinkage = t_iterLinkages.next();
//            t_aResidues.remove(t_objLinkage.m_strFromID);            
//        }
//        if ( t_aResidues.size() != 1 )
//        {
//            throw new SugarImporterException("Unconnected sugars are not supported.");
//        }
//        Iterator<String> t_iterResidues = t_aResidues.iterator();
//        // get the root Residue
//        String t_strRoot = t_iterResidues.next();
        // add all nodes to the sugar
        for (Iterator<String> t_iterResidues = this.m_hashResidues.keySet().iterator(); t_iterResidues.hasNext();) 
        {
            this.m_objSugar.addNode( this.m_hashResidues.get(t_iterResidues.next()));            
        }
        // add the edges to the sugar
        for (Iterator<GlydeLinkage> t_iterLinkages = this.m_aLinkages.iterator(); t_iterLinkages.hasNext();) 
        {
            GlydeLinkage t_objLinkage = t_iterLinkages.next();
            GlycoNode t_objParent = this.m_hashResidues.get(t_objLinkage.m_strToID);
            GlycoNode t_objChild  = this.m_hashResidues.get(t_objLinkage.m_strFromID);
            if ( t_objChild == null || t_objParent == null )
            {
                throw new SugarImporterException("Unknown residue in linkage.");
            }
            this.m_objSugar.addEdge(t_objParent,t_objChild,t_objLinkage.m_objLinkage);
        }
    }

}