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
package org.eurocarbdb.MolecularFramework.io.GlycoCT;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.sugar.Anomer;
import org.eurocarbdb.MolecularFramework.sugar.BaseType;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraphAlternative;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.LinkageType;
import org.eurocarbdb.MolecularFramework.sugar.Modification;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.SubstituentType;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.Superclass;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
* @author Logan
*
*/
public class SugarImporterGlycoCT extends SugarImporter 
{
    public static final int MAX_BASE_TYPE   = 5;
    
    private Element m_objRoot = null;
    private Document m_objDocument = null;
    private HashMap<Integer,GlycoNode> m_hashResidues = new HashMap<Integer,GlycoNode>(); 
    private HashMap<Integer,GlycoEdge> m_hashLinkages = new HashMap<Integer,GlycoEdge>();
    private HashMap<Integer,SugarUnitRepeat> m_hashRepeats = new HashMap<Integer,SugarUnitRepeat>();
    private HashMap<Integer,SugarUnitAlternative> m_hashAlternatives = new HashMap<Integer,SugarUnitAlternative>();
    private GlycoGraph m_objSugarUnit = null;
    private HashMap<GlycoNode,GlycoGraph> m_hashGraphs = new HashMap<GlycoNode,GlycoGraph>(); 
    
    public Sugar parse(String a_strXML)  throws SugarImporterException
    {
        SAXBuilder builder = new SAXBuilder();
        try 
        {
            this.m_objDocument = builder.build(new StringReader(a_strXML));
            if (builder.getValidation())
            {
                throw new SugarImporterException("XML Validation error");
            }
            return this.parse(this.m_objDocument.getRootElement());        
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
            this.m_objSugarUnit = this.m_objSugar;
            this.m_objRoot = a_objRoot;
            List t_lMainElements = this.m_objRoot.getChildren();            
            for (Iterator t_iterElements = t_lMainElements.iterator(); t_iterElements.hasNext();) 
            {
                Element t_objMain = (Element) t_iterElements.next();
                if (t_objMain.getName().equals("residues"))
                {
                    this.parseResidueSection(t_objMain);
                }
                else if (t_objMain.getName().equals("linkages"))
                {
                    this.parseLinkageSection(t_objMain);
                }
                else if (t_objMain.getName().equals("repeat"))
                {
                    this.parseRepeatSection(t_objMain);
                }
                else if (t_objMain.getName().equals("aglyca"))
                {
                    this.parseAglycaSection(t_objMain);
                }
                else if (t_objMain.getName().equals("underDeterminedSubtrees"))
                {
                    this.parseUnderdetermindedSubtreeSection(t_objMain);
                }
                else if (t_objMain.getName().equals("alternative"))
                {
                    this.parseAlternativeSection(t_objMain);
                }
            }            
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
        this.m_objSugarUnit = null;
        this.m_hashResidues.clear(); 
        this.m_hashResidues.clear();
        this.m_hashRepeats.clear();
        this.m_hashAlternatives.clear();
        this.m_hashGraphs.clear();
        this.m_hashLinkages.clear();
    }

    private void parseResidueSection(Element a_objMainElement) throws GlycoconjugateException, JDOMException, SugarImporterException 
    {
        // Parse substructure
        List t_lMainElements = a_objMainElement.getChildren();
        for (Iterator t_iterElements = t_lMainElements.iterator(); t_iterElements.hasNext();) 
        {
            Element t_objResidue = (Element) t_iterElements.next();
            
            if ( t_objResidue.getName().equals("basetype") )
            {
                this.parseBasetype(t_objResidue);
            }
            else if ( t_objResidue.getName().equals("substituent") )
            {
                this.parseSubstitutent(t_objResidue);
            }
            else if ( t_objResidue.getName().equals("repeat") )
            {
                this.parseRepeat(t_objResidue);
            }
            else if ( t_objResidue.getName().equals("alternative") )
            {
                this.parseAlternative(t_objResidue);
            }
            else if ( t_objResidue.getName().equals("monosaccharide") )
            {
                this.parseBasetype(t_objResidue);
            }
        }
    }
    
    /**
     * @param residue
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void parseSubstitutent(Element a_objResidue) throws SugarImporterException, GlycoconjugateException 
    {
        String t_strAttribute;
        t_strAttribute = a_objResidue.getAttributeValue("name");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<res> must have a name attribute.");            
        }
        Substituent t_objSubst = new Substituent(SubstituentType.forName(t_strAttribute));
        // add ms to sugar
        this.m_objSugarUnit.addNode(t_objSubst);
        this.m_hashGraphs.put(t_objSubst,this.m_objSugarUnit);
        t_strAttribute = a_objResidue.getAttributeValue("id");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<res> must have a id.");
        }
        Integer t_iID = Integer.parseInt(t_strAttribute);
        if ( this.m_hashResidues.containsKey(t_iID) )
        {
            throw new SugarImporterException("Dupplicated residue ID.");
        }
        this.m_hashResidues.put(t_iID,t_objSubst);
    }

    private void parseBasetype(Element a_objResidue) throws GlycoconjugateException, SugarImporterException 
    {
        // anomer
        String t_strAttribute = a_objResidue.getAttributeValue("anomer");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<res> of type b must have an anomer.");
        }
        Anomer t_enumAnomer = Anomer.forSymbol(t_strAttribute.charAt(0));
        // superclass
        t_strAttribute = a_objResidue.getAttributeValue("superclass");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<res> of type b must have a superclass.");
        }
        Superclass t_enumSuperclass = Superclass.forName(t_strAttribute);
        // new MS 
        Monosaccharide t_objMS = new Monosaccharide(t_enumAnomer,t_enumSuperclass);
        // ring
        t_strAttribute = a_objResidue.getAttributeValue("ringStart");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<res> of type b must have a ring start.");
        }
        int t_iStart = Integer.parseInt(t_strAttribute);
        t_strAttribute = a_objResidue.getAttributeValue("ringEnd");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<res> of type b must have a ring end.");
        }
        int t_iEnd = Integer.parseInt(t_strAttribute);
        t_objMS.setRing(t_iStart,t_iEnd);
        // sub tags
        List t_lMainElements = a_objResidue.getChildren();
        Modification t_objModi;
        String t_strName;
        int t_iPosOne;
        // prepare Basetype
        ArrayList<BaseType> t_aBaseType = new ArrayList<BaseType>();
        for (int i = 0; i < SugarImporterGlycoCT.MAX_BASE_TYPE; i++) 
        {
            t_aBaseType.add(null);
        }
        
        for (Iterator t_iterElements = t_lMainElements.iterator(); t_iterElements.hasNext();) 
        {
            Element t_objSubTag = (Element) t_iterElements.next();
            if (t_objSubTag.getName().equals("basetype"))
            {
                // basetype
                t_strAttribute = t_objSubTag.getAttributeValue("id");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<basetype> of must have a id.");
                }
                int t_iPos = Integer.parseInt(t_strAttribute);
                t_iPos--;
                if ( t_iPos<0 || t_iPos> SugarImporterGlycoCT.MAX_BASE_TYPE )
                {
                    throw new SugarImporterException("<basetype> id must be a number between 0 and " + SugarImporterGlycoCT.MAX_BASE_TYPE);
                }
                t_strAttribute = t_objSubTag.getAttributeValue("type");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<basetype> of must have a type.");
                }
                t_aBaseType.set(t_iPos,BaseType.forName(t_strAttribute));
            }
            else if (t_objSubTag.getName().equals("stemtype"))
            {
                // basetype
                t_strAttribute = t_objSubTag.getAttributeValue("id");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<stemtype> of must have a id.");
                }
                int t_iPos = Integer.parseInt(t_strAttribute);
                t_iPos--;
                if ( t_iPos<0 || t_iPos> SugarImporterGlycoCT.MAX_BASE_TYPE )
                {
                    throw new SugarImporterException("<stemtype> id must be a number between 0 and " + SugarImporterGlycoCT.MAX_BASE_TYPE);
                }
                t_strAttribute = t_objSubTag.getAttributeValue("type");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<stemtype> of must have a type.");
                }
                t_aBaseType.set(t_iPos,BaseType.forName(t_strAttribute));
            }
            else if (t_objSubTag.getName().equals("modification"))
            {
                // modification
                t_strName = t_objSubTag.getAttributeValue("type");
                if ( t_strName == null )
                {
                    throw new SugarImporterException("<modification> of must have a type.");
                }
                t_strAttribute = t_objSubTag.getAttributeValue("pos_one");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<modification> of must have a pos_one attribute.");
                }
                t_iPosOne = Integer.parseInt(t_strAttribute);
                t_strAttribute = t_objSubTag.getAttributeValue("pos_two");
                if ( t_strAttribute == null )
                {
                    t_objModi = new Modification(t_strName,t_iPosOne);
                }
                else
                {
                    int t_iPosTwo = Integer.parseInt(t_strAttribute);
                    t_objModi = new Modification(t_strName,t_iPosOne,t_iPosTwo);
                }
                t_objMS.addModification(t_objModi);                
            }
        }
        // store BaseType
        boolean t_bNull = false;
        for (int i = 0; i < SugarImporterGlycoCT.MAX_BASE_TYPE; i++) 
        {
            if ( t_bNull )
            {
                if ( t_aBaseType.get(i) != null )
                {
                    throw new SugarImporterException("<basetype> id " + i + " is missing.");
                }
            }
            else
            {
                if ( t_aBaseType.get(i) == null )
                {
                    t_bNull = true;
                }
                else
                {
                    t_objMS.addBaseType(t_aBaseType.get(i));
                }
            }
        }
        // test with glycoct name
        t_strAttribute = a_objResidue.getAttributeValue("name");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<res> must have a name attribute.");            
        }
        if ( !t_objMS.getGlycoCTName().equalsIgnoreCase(t_strAttribute) )
        {
            throw new SugarImporterException("Calculated glycoCT name and name are not equal : " + t_objMS.getGlycoCTName() + " != " + t_strAttribute);
        }
        // add ms to sugar
        this.m_objSugarUnit.addNode(t_objMS);
        this.m_hashGraphs.put(t_objMS,this.m_objSugarUnit);
        t_strAttribute = a_objResidue.getAttributeValue("id");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<res> must have a id.");
        }
        Integer t_iID = Integer.parseInt(t_strAttribute);
        if ( this.m_hashResidues.containsKey(t_iID) )
        {
            throw new SugarImporterException("Dupplicated residue ID.");
        }
        this.m_hashResidues.put(t_iID,t_objMS);
    }

    /**
     * @param main
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void parseLinkageSection(Element a_objLinkage) throws SugarImporterException, GlycoconjugateException 
    {
// so siehts aus
//          <linkages>
//            <connection id="0" parent="0" child="1">
//              <lin parenttype="d" childtype="n">
//                <from pos="2" />
//                <to pos="1" />
//              </lin>
//            </connection>
//            ...
//          </linkages>
        String t_strAttribute;
        GlycoNode t_objParent;
        GlycoNode t_objChild;
        Integer t_objID;
        List t_lMainElements = a_objLinkage.getChildren();
        List t_lSubElements;
        List t_lSubSubElements;
        Linkage t_objLinkage;
        Element t_objLin;
        Element t_objConnection;
        Element t_objFromTo;
        for (Iterator t_iterElements = t_lMainElements.iterator(); t_iterElements.hasNext();) 
        {
            t_objConnection = (Element) t_iterElements.next();
            if ( t_objConnection.getName().equals("connection"))
            {
                GlycoEdge t_objEdge = new GlycoEdge();
                t_lSubElements = t_objConnection.getChildren();
                for (Iterator t_iterSubElements = t_lSubElements.iterator(); t_iterSubElements.hasNext();) 
                {
                    t_objLin = (Element) t_iterSubElements.next();
                    if ( t_objLin.getName().equals("linkage"))
                    {
                        t_objLinkage = new Linkage();
                        // parent type
                        t_strAttribute = t_objLin.getAttributeValue("parentType");
                        if ( t_strAttribute == null )
                        {
                            throw new SugarImporterException("<linkage> must have a parentType.");
                        }
                        t_objLinkage.setParentLinkageType( LinkageType.forName(t_strAttribute.charAt(0)));
                        // child tpye
                        t_strAttribute = t_objLin.getAttributeValue("childType");
                        if ( t_strAttribute == null )
                        {
                            throw new SugarImporterException("<linkage> must have a childType.");
                        }
                        t_objLinkage.setChildLinkageType( LinkageType.forName(t_strAttribute.charAt(0)));
                        // positions
                        t_lSubSubElements = t_objLin.getChildren();
                        for (Iterator t_iterSubSubElements = t_lSubSubElements.iterator(); t_iterSubSubElements.hasNext();)
                        {
                            t_objFromTo = (Element) t_iterSubSubElements.next();
                            if ( t_objFromTo.getName().equals("parent") )
                            {
                                t_strAttribute = t_objFromTo.getAttributeValue("pos");
                                if ( t_strAttribute == null )
                                {
                                    throw new SugarImporterException("<from> or <to> must have a pos attribute.");
                                }
                                t_objID = Integer.parseInt(t_strAttribute); 
                                t_objLinkage.addParentLinkage(t_objID);
                            }
                            else if ( t_objFromTo.getName().equals("child") )
                            {
                                t_strAttribute = t_objFromTo.getAttributeValue("pos");
                                if ( t_strAttribute == null )
                                {
                                    throw new SugarImporterException("<from> or <to> must have a pos attribute.");
                                }
                                t_objID = Integer.parseInt(t_strAttribute); 
                                t_objLinkage.addChildLinkage(t_objID);
                            }
                        }
                        t_objEdge.addGlycosidicLinkage(t_objLinkage);
                    }
                }    
                if ( t_objEdge.getGlycosidicLinkages().size() == 0 )
                {
                    throw new SugarImporterException("<connection> must have at least on <lin> subtag.");
                }
                // add to sugar
                t_strAttribute = t_objConnection.getAttributeValue("parent");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<connection> must have a parent.");
                }
                t_objID = Integer.parseInt(t_strAttribute); 
                t_objParent = this.m_hashResidues.get(t_objID);
                t_strAttribute = t_objConnection.getAttributeValue("child");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<connection> must have a child.");
                }
                t_objID = Integer.parseInt(t_strAttribute); 
                t_objChild = this.m_hashResidues.get(t_objID);
                if ( t_objParent == null || t_objChild == null )
                {
                    throw new SugarImporterException("parent or child id invalde in <connection> .");
                }
                this.m_objSugarUnit.addEdge(t_objParent,t_objChild,t_objEdge);
                t_strAttribute = t_objConnection.getAttributeValue("id");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<connection> must have a id.");
                }
                Integer t_iID = Integer.parseInt(t_strAttribute);
                if ( this.m_hashLinkages.containsKey(t_iID) )
                {
                    throw new SugarImporterException("Dupplicated linkage ID.");
                }
                this.m_hashLinkages.put(t_iID,t_objEdge);
            }
        }
    }

    /**
     * @param residue
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void parseRepeat(Element a_objRepeat) throws SugarImporterException, GlycoconjugateException 
    {
        String t_strAttribute;
        SugarUnitRepeat t_objRepeat = new SugarUnitRepeat();
        // add ms to sugar
        this.m_objSugarUnit.addNode(t_objRepeat);
        this.m_hashGraphs.put(t_objRepeat,this.m_objSugarUnit);
        t_strAttribute = a_objRepeat.getAttributeValue("id");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<res> must have a id.");
        }
        Integer t_iID = Integer.parseInt(t_strAttribute);
        if ( this.m_hashResidues.containsKey(t_iID) )
        {
            throw new SugarImporterException("Dupplicated residue ID.");
        }
        this.m_hashResidues.put(t_iID,t_objRepeat);
        // repeat ID
        t_strAttribute = a_objRepeat.getAttributeValue("repeatId");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<res> must have a name attribute.");            
        }        
        t_iID = Integer.parseInt(t_strAttribute);
        if ( this.m_hashRepeats.containsKey(t_iID) )
        {
            throw new SugarImporterException("Dupplicated repeat ID.");
        }
        this.m_hashRepeats.put(t_iID,t_objRepeat);
    }

    /**
     * @param main
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void parseAglycaSection(Element a_objAglyca) throws SugarImporterException, GlycoconjugateException 
    {
        List t_lMainElements = a_objAglyca.getChildren();
        for (Iterator t_iterElements = t_lMainElements.iterator(); t_iterElements.hasNext();) 
        {
            Element t_objCategory = (Element) t_iterElements.next();
            if (t_objCategory.getName().equals("historicalData"))
            {
                this.parseHistorical(t_objCategory);
            }
            else
            {
                // TODO:
                throw new SugarImporterException("Not supported yet " + t_objCategory.getName());
            }
        }
    }

    /**
     * @param category
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void parseHistorical(Element a_objCategory) throws SugarImporterException, GlycoconjugateException 
    {
        String t_strAttribute;
        List t_lMainElements = a_objCategory.getChildren();
        for (Iterator t_iterElements = t_lMainElements.iterator(); t_iterElements.hasNext();) 
        {
            Element t_objAGL = (Element) t_iterElements.next();
            if ( !t_objAGL.getName().equals("entry") )
            {
                throw new SugarImporterException("Forbiden tag " + t_objAGL.getName() + " in historicalData section."); 
            }
            t_strAttribute = t_objAGL.getAttributeValue("name");
            if ( t_strAttribute == null )
            {
                throw new SugarImporterException("<entry> must have a name attribute.");            
            }        
            NonMonosaccharide t_objHistorical = new NonMonosaccharide(t_strAttribute);
            t_strAttribute = t_objAGL.getAttributeValue("fromResidue");
            if ( t_strAttribute != null )
            {
                GlycoEdge t_objEdge = new GlycoEdge();
                int t_iID = Integer.parseInt(t_strAttribute);
                // at the non reducing end
                List t_lElements = t_objAGL.getChildren();
                for (Iterator t_iterSElements = t_lElements.iterator(); t_iterSElements.hasNext();) 
                {
                    Element t_objLin = (Element) t_iterSElements.next();
                    if ( !t_objLin.getName().equals("linkage"))
                    {
                        throw new SugarImporterException("Invalde tag " + t_objLin.getName() + " in connection section.");
                    }
                    Linkage t_objLinkage = new Linkage();
                    // parent type
                    t_strAttribute = t_objLin.getAttributeValue("parentType");
                    if ( t_strAttribute == null )
                    {
                        throw new SugarImporterException("<linkage> must have a parentType.");
                    }
                    t_objLinkage.setParentLinkageType( LinkageType.forName(t_strAttribute.charAt(0)));
                    // child tpye
                    t_strAttribute = t_objLin.getAttributeValue("childType");
                    if ( t_strAttribute == null )
                    {
                        throw new SugarImporterException("<linkage> must have a childType.");
                    }
                    t_objLinkage.setChildLinkageType( LinkageType.forName(t_strAttribute.charAt(0)));
                    // positions
                    List t_lSubElements = t_objLin.getChildren();
                    for (Iterator t_iterSubElements = t_lSubElements.iterator(); t_iterSubElements.hasNext();)
                    {
                        Element t_objFromTo = (Element) t_iterSubElements.next();
                        t_strAttribute = t_objFromTo.getAttributeValue("pos");
                        if ( t_strAttribute == null )
                        {
                            throw new SugarImporterException("<from> or <to> must have a pos attribute.");
                        }
                        Integer t_objID = Integer.parseInt(t_strAttribute);
                        if ( t_objFromTo.getName().equals("parent") )
                        {
                            t_objLinkage.addParentLinkage(t_objID);
                        }
                        else if ( t_objFromTo.getName().equals("child") )
                        {
                            t_objLinkage.addChildLinkage(t_objID);
                        }
                        else
                        {
                            throw new SugarImporterException("Invalde tag " + t_objFromTo.getName() + " in lin section.");
                        }
                    }
                    t_objEdge.addGlycosidicLinkage(t_objLinkage);
                }   
                if ( t_objEdge.getGlycosidicLinkages().size() == 0 )
                {
                    throw new SugarImporterException("<entry> must have at least on <lin> subtag.");
                }
                GlycoNode t_objResidue = this.m_hashResidues.get(t_iID);
                this.m_objSugarUnit.addEdge(t_objResidue,t_objHistorical,t_objEdge);
            }
            else
            {                  
                t_strAttribute = t_objAGL.getAttributeValue("toResidue");
                if ( t_strAttribute != null )
                {
                    GlycoEdge t_objEdge = new GlycoEdge();
                    int t_iID = Integer.parseInt(t_strAttribute);
                    // at the reducing end
                    List t_lElements = t_objAGL.getChildren();
                    for (Iterator t_iterSElements = t_lElements.iterator(); t_iterSElements.hasNext();) 
                    {
                        Element t_objLin = (Element) t_iterSElements.next();
                        if ( !t_objLin.getName().equals("linkage"))
                        {
                            throw new SugarImporterException("Invalde tag " + t_objLin.getName() + " in connection section.");
                        }
                        Linkage t_objLinkage = new Linkage();
                        // parent type
                        t_strAttribute = t_objLin.getAttributeValue("parentType");
                        if ( t_strAttribute == null )
                        {
                            throw new SugarImporterException("<linkage> must have a parentType.");
                        }
                        t_objLinkage.setParentLinkageType( LinkageType.forName(t_strAttribute.charAt(0)));
                        // child tpye
                        t_strAttribute = t_objLin.getAttributeValue("childType");
                        if ( t_strAttribute == null )
                        {
                            throw new SugarImporterException("<linkage> must have a childType.");
                        }
                        t_objLinkage.setChildLinkageType( LinkageType.forName(t_strAttribute.charAt(0)));
                        // positions
                        List t_lSubElements = t_objLin.getChildren();
                        for (Iterator t_iterSubElements = t_lSubElements.iterator(); t_iterSubElements.hasNext();)
                        {
                            Element t_objFromTo = (Element) t_iterSubElements.next();
                            t_strAttribute = t_objFromTo.getAttributeValue("pos");
                            if ( t_strAttribute == null )
                            {
                                throw new SugarImporterException("<from> or <to> must have a pos attribute.");
                            }
                            Integer t_objID = Integer.parseInt(t_strAttribute);
                            if ( t_objFromTo.getName().equals("parent") )
                            {
                                t_objLinkage.addParentLinkage(t_objID);
                            }
                            else if ( t_objFromTo.getName().equals("child") )
                            {
                                t_objLinkage.addChildLinkage(t_objID);
                            }
                            else
                            {
                                throw new SugarImporterException("Invalde tag " + t_objFromTo.getName() + " in lin section.");
                            }
                        }
                        t_objEdge.addGlycosidicLinkage(t_objLinkage);
                    }    
                    if ( t_objEdge.getGlycosidicLinkages().size() == 0 )
                    {
                        throw new SugarImporterException("<entry> must have at least on <lin> subtag.");
                    }
                    GlycoNode t_objResidue = this.m_hashResidues.get(t_iID);
                    this.m_objSugarUnit.addEdge(t_objHistorical,t_objResidue,t_objEdge);
                }
                else
                {
                    // unconnected ms
                }
            }
        }
    }

    private void parseRepeatSection(Element a_objMainElement) throws SugarImporterException, GlycoconjugateException, JDOMException
    {
//      so sieht aus
//      <repeat>
//        <unit id="0" minOccur="-1" maxOccur="-1">
//          <residues>
//            <res id="3" type="b" anomer="a" superclass="hex" ringStart="1" ringEnd="5" name="a-dglc-HEX-1:5">
//              <basetype id="0" type="dglc" />
//            </res>
//          </residues>
//          <linkages />
//          <internalLinkage parent="3" child="3">
//            <lin parentType="u" childType="u">
//              <from pos="2" />
//              <to pos="1" />
//            </lin>
//          </internalLinkage>
//        </unit>
//      </repeat>
        String t_strAttribute;
        SugarUnitRepeat t_objRepeat;
        List t_lMainElements = a_objMainElement.getChildren();
        for (Iterator t_iterElements = t_lMainElements.iterator(); t_iterElements.hasNext();) 
        {
            Element t_objUnit = (Element) t_iterElements.next();
            if ( t_objUnit.getName().equals("unit") )
            {
                // find repeat unit
                t_strAttribute = t_objUnit.getAttributeValue("id");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<unit> must have a id.");
                }
                t_objRepeat = this.m_hashRepeats.get(Integer.parseInt(t_strAttribute));
                if ( t_objRepeat == null )
                {
                    throw new SugarImporterException("Critical error repeat unit id " + t_strAttribute + "never declarated before.");
                }
                // min / max
                t_strAttribute = t_objUnit.getAttributeValue("minOccur");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<unit> must have a minOccur.");
                }
                t_objRepeat.setMinRepeatCount(Integer.parseInt(t_strAttribute));
                t_strAttribute = t_objUnit.getAttributeValue("maxOccur");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<unit> must have a maxOccur.");
                }
                t_objRepeat.setMaxRepeatCount(Integer.parseInt(t_strAttribute));
                this.m_objSugarUnit = t_objRepeat;
                List t_lSubElements = t_objUnit.getChildren();            
                for (Iterator t_iterSubElements = t_lSubElements.iterator(); t_iterSubElements.hasNext();) 
                {
                    Element t_objMain = (Element) t_iterSubElements.next();
                    if (t_objMain.getName().equals("residues"))
                    {
                        this.parseResidueSection(t_objMain);
                    }
                    else if (t_objMain.getName().equals("linkages"))
                    {
                        this.parseLinkageSection(t_objMain);
                    }
                    else if (t_objMain.getName().equals("internalLinkage"))
                    {
                        this.parseInternalLinkage(t_objMain,t_objRepeat);
                    }
                }
            }
        }
    }

    /**
     * @param a_objRepeat 
     * @param main
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void parseInternalLinkage(Element a_objInternal, SugarUnitRepeat a_objRepeat) throws SugarImporterException, GlycoconjugateException
    {
//      <internalLinkage parent="3" child="3">
//        <lin parentType="u" childType="u">
//          <from pos="2" />
//          <to pos="1" />
//        </lin>
//      </internalLinkage>
        String t_strAttribute = a_objInternal.getAttributeValue("parent");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<internalLinkage> must have a parent attribute.");
        }
        GlycoNode t_objParent = this.m_hashResidues.get(Integer.parseInt(t_strAttribute));
        t_strAttribute = a_objInternal.getAttributeValue("child");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<internalLinkage> must have a child attribute.");
        }
        GlycoNode t_objChild = this.m_hashResidues.get(Integer.parseInt(t_strAttribute));
        if ( t_objChild == null || t_objParent == null )
        {
            throw new SugarImporterException("Error in <internalLinkage> residues are not declareded.");
        }
        // edge
        GlycoEdge t_objEdge = new GlycoEdge();
        // fill with linkages
        List t_lElements = a_objInternal.getChildren();
        Element t_objLin;
        Linkage t_objLinkage;
        List t_lSubElements;
        Element t_objFromTo;
        Integer t_objID; 
        for (Iterator t_iterElements = t_lElements.iterator(); t_iterElements.hasNext();) 
        {
            t_objLin = (Element) t_iterElements.next();
            if ( t_objLin.getName().equals("linkage"))
            {
                t_objLinkage = new Linkage();
                // parent type
                t_strAttribute = t_objLin.getAttributeValue("parentType");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<linkage> must have a parentType.");
                }
                t_objLinkage.setParentLinkageType( LinkageType.forName(t_strAttribute.charAt(0)));
                // child tpye
                t_strAttribute = t_objLin.getAttributeValue("childType");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<linkage> must have a childType.");
                }
                t_objLinkage.setChildLinkageType( LinkageType.forName(t_strAttribute.charAt(0)));
                // positions
                t_lSubElements = t_objLin.getChildren();
                for (Iterator t_iterSubElements = t_lSubElements.iterator(); t_iterSubElements.hasNext();)
                {
                    t_objFromTo = (Element) t_iterSubElements.next();
                    if ( t_objFromTo.getName().equals("parent") )
                    {
                        t_strAttribute = t_objFromTo.getAttributeValue("pos");
                        if ( t_strAttribute == null )
                        {
                            throw new SugarImporterException("<from> or <to> must have a pos attribute.");
                        }
                        t_objID = Integer.parseInt(t_strAttribute); 
                        t_objLinkage.addParentLinkage(t_objID);
                    }
                    else if ( t_objFromTo.getName().equals("child") )
                    {
                        t_strAttribute = t_objFromTo.getAttributeValue("pos");
                        if ( t_strAttribute == null )
                        {
                            throw new SugarImporterException("<from> or <to> must have a pos attribute.");
                        }
                        t_objID = Integer.parseInt(t_strAttribute); 
                        t_objLinkage.addChildLinkage(t_objID);
                    }
                }
                t_objEdge.addGlycosidicLinkage(t_objLinkage);
            }
        }    
        if ( t_objEdge.getGlycosidicLinkages().size() == 0 )
        {
            throw new SugarImporterException("<connection> must have at least on <lin> subtag.");
        }
        // add edge
        a_objRepeat.setRepeatLinkage(t_objEdge,t_objParent,t_objChild);
    }

    /**
     * @param main
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     * @throws JDOMException 
     */
    private void parseUnderdetermindedSubtreeSection(Element a_objMainElement) throws SugarImporterException, GlycoconjugateException, JDOMException 
    {
        String t_strAttribute;
        List t_lMainElements = a_objMainElement.getChildren();
        for (Iterator t_iterElements = t_lMainElements.iterator(); t_iterElements.hasNext();) 
        {
            UnderdeterminedSubTree t_objSubtree = new UnderdeterminedSubTree(); 
            Element t_objTree = (Element) t_iterElements.next();
            if ( t_objTree.getName().equals("tree") )
            {
                t_strAttribute = t_objTree.getAttributeValue("probLow");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<tree> must have a probLow.");
                }
                Double t_dLow = Double.parseDouble(t_strAttribute); 
                t_strAttribute = t_objTree.getAttributeValue("probUp");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<tree> must have a probUp.");
                }
                Double t_dHigh = Double.parseDouble(t_strAttribute); 
                t_objSubtree.setProbability(t_dLow,t_dHigh);
                this.m_objSugarUnit = t_objSubtree;
                List t_lSubElements = t_objTree.getChildren();
                ArrayList<GlycoNode> t_aParents = new ArrayList<GlycoNode>();
                for (Iterator t_iterSubElements = t_lSubElements.iterator(); t_iterSubElements.hasNext();) 
                {
                    Element t_objSubElement = (Element) t_iterSubElements.next();
                    if (t_objSubElement.getName().equals("residues"))
                    {
                        this.parseResidueSection(t_objSubElement);
                    }
                    else if (t_objSubElement.getName().equals("linkages"))
                    {
                        this.parseLinkageSection(t_objSubElement);
                    }
                    else if (t_objSubElement.getName().equals("parents"))
                    {
                        t_aParents = this.parseParents(t_objSubElement);
                    }
                    else if (t_objSubElement.getName().equals("connection"))
                    {
                        t_objSubtree.setConnection(this.parseConnection(t_objSubElement));
                    }
                }           
                GlycoGraph t_objUnit = this.m_hashGraphs.get( t_aParents.get(0) );
                GlycoNode t_objNode;
                if ( t_objUnit.getClass() == SugarUnitRepeat.class )
                {
                    SugarUnitRepeat t_objRepeat = (SugarUnitRepeat)t_objUnit;
                    t_objRepeat.addUndeterminedSubTree(t_objSubtree);
                    for (Iterator<GlycoNode> t_iterParents = t_aParents.iterator(); t_iterParents.hasNext();) 
                    {
                        t_objNode = t_iterParents.next();
                        if ( this.m_hashGraphs.get( t_objNode ) != t_objUnit )
                        {
                            throw new SugarImporterException("Error in <underDeterminedSubtree> all parents must be in the same unit.");
                        }
                        t_objRepeat.addUndeterminedSubTreeParent(t_objSubtree,t_objNode);
                    }
                }
                else if ( t_objUnit.getClass() == Sugar.class )
                {
                    Sugar t_objRepeat = (Sugar)t_objUnit;
                    t_objRepeat.addUndeterminedSubTree(t_objSubtree);
                    for (Iterator<GlycoNode> t_iterParents = t_aParents.iterator(); t_iterParents.hasNext();) 
                    {
                        t_objNode = t_iterParents.next();
                        if ( this.m_hashGraphs.get( t_objNode ) != t_objUnit )
                        {
                            throw new SugarImporterException("Error in <underDeterminedSubtree> all parents must be in the same unit.");
                        }
                        t_objRepeat.addUndeterminedSubTreeParent(t_objSubtree,t_objNode);
                    }
                }
                else
                {
                    throw new SugarImporterException("Error in <underDeterminedSubtree>: Tree is connected to a " + t_objUnit.getClass().getName() + ".");
                }
            }
        }
    }

    /**
     * @param subElement
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private GlycoEdge parseConnection(Element a_objConnection) throws SugarImporterException, GlycoconjugateException 
    {
        String t_strAttribute;
        // edge
        GlycoEdge t_objEdge = new GlycoEdge();
        // fill with linkages
        List t_lElements = a_objConnection.getChildren();
        Element t_objLin;
        Linkage t_objLinkage;
        List t_lSubElements;
        Element t_objFromTo;
        Integer t_objID; 
        for (Iterator t_iterElements = t_lElements.iterator(); t_iterElements.hasNext();) 
        {
            t_objLin = (Element) t_iterElements.next();
            if ( t_objLin.getName().equals("linkage"))
            {
                t_objLinkage = new Linkage();
                // parent type
                t_strAttribute = t_objLin.getAttributeValue("parentType");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<linkage> must have a parentType.");
                }
                t_objLinkage.setParentLinkageType( LinkageType.forName(t_strAttribute.charAt(0)));
                // child tpye
                t_strAttribute = t_objLin.getAttributeValue("childType");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<linkage> must have a childType.");
                }
                t_objLinkage.setChildLinkageType( LinkageType.forName(t_strAttribute.charAt(0)));
                // positions
                t_lSubElements = t_objLin.getChildren();
                for (Iterator t_iterSubElements = t_lSubElements.iterator(); t_iterSubElements.hasNext();)
                {
                    t_objFromTo = (Element) t_iterSubElements.next();
                    if ( t_objFromTo.getName().equals("parent") )
                    {
                        t_strAttribute = t_objFromTo.getAttributeValue("pos");
                        if ( t_strAttribute == null )
                        {
                            throw new SugarImporterException("<from> or <to> must have a pos attribute.");
                        }
                        t_objID = Integer.parseInt(t_strAttribute); 
                        t_objLinkage.addParentLinkage(t_objID);
                    }
                    else if ( t_objFromTo.getName().equals("child") )
                    {
                        t_strAttribute = t_objFromTo.getAttributeValue("pos");
                        if ( t_strAttribute == null )
                        {
                            throw new SugarImporterException("<from> or <to> must have a pos attribute.");
                        }
                        t_objID = Integer.parseInt(t_strAttribute); 
                        t_objLinkage.addChildLinkage(t_objID);
                    }
                }
                t_objEdge.addGlycosidicLinkage(t_objLinkage);
            }
        }    
        if ( t_objEdge.getGlycosidicLinkages().size() == 0 )
        {
            throw new SugarImporterException("<connection> must have at least on <lin> subtag.");
        }
        return t_objEdge;
    }

    /**
     * @param subElement
     * @return
     * @throws SugarImporterException 
     */
    private ArrayList<GlycoNode> parseParents(Element a_objParentElement) throws SugarImporterException 
    {
        String t_strAttribute;
        ArrayList<GlycoNode> t_aParents = new ArrayList<GlycoNode>();
        List t_lSubElements = a_objParentElement.getChildren();
        Integer t_iID;
        GlycoNode t_objParentNode;
        for (Iterator t_iterSubElements = t_lSubElements.iterator(); t_iterSubElements.hasNext();) 
        {
            Element t_objParent = (Element) t_iterSubElements.next();
            if ( t_objParent.getName().equals("parent") )
            {
                t_strAttribute = t_objParent.getAttributeValue("res_id");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<parent> must have a res_id attribute.");
                }
                t_iID = Integer.parseInt(t_strAttribute);
                t_objParentNode = this.m_hashResidues.get(t_iID);
                if ( t_objParentNode == null )
                {
                    throw new SugarImporterException("Invalide <parent>. Residue was not declareted before.");
                }
                t_aParents.add(t_objParentNode);
            }
        }
        return t_aParents;
    }

    /**
     * @param residue
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void parseAlternative(Element a_objAlternativeElement) throws SugarImporterException, GlycoconjugateException 
    {
        String t_strAttribute;
        SugarUnitAlternative t_objAlternative = new SugarUnitAlternative();
        // add ms to sugar
        this.m_objSugarUnit.addNode(t_objAlternative);
        this.m_hashGraphs.put(t_objAlternative,this.m_objSugarUnit);
        t_strAttribute = a_objAlternativeElement.getAttributeValue("id");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<res> must have a id.");
        }
        Integer t_iID = Integer.parseInt(t_strAttribute);
        if ( this.m_hashResidues.containsKey(t_iID) )
        {
            throw new SugarImporterException("Dupplicated residue ID.");
        }
        this.m_hashResidues.put(t_iID,t_objAlternative);
        // alternativeId
        t_strAttribute = a_objAlternativeElement.getAttributeValue("alternativeId");
        if ( t_strAttribute == null )
        {
            throw new SugarImporterException("<res> must have a name attribute.");            
        }        
        t_iID = Integer.parseInt(t_strAttribute);
        if ( this.m_hashAlternatives.containsKey(t_iID) )
        {
            throw new SugarImporterException("Dupplicated alternative ID.");
        }
        this.m_hashAlternatives.put(t_iID,t_objAlternative);
    }
    
    /**
     * @param main
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     * @throws JDOMException 
     */
    private void parseAlternativeSection(Element a_objAlternativeElement) throws SugarImporterException, GlycoconjugateException, JDOMException 
    {
        String t_strAttribute;
        List t_lAlternativeElements = a_objAlternativeElement.getChildren();
        for (Iterator t_iterElements = t_lAlternativeElements.iterator(); t_iterElements.hasNext();) 
        {
            Element t_objUnitElement = (Element) t_iterElements.next();
            if ( t_objUnitElement.getName().equals("unit"))
            {
                // unit
                t_strAttribute = t_objUnitElement.getAttributeValue("id");
                if ( t_strAttribute == null )
                {
                    throw new SugarImporterException("<unit> must have a id.");
                }
                SugarUnitAlternative t_objAlternative = this.m_hashAlternatives.get(Integer.parseInt(t_strAttribute));
                if ( t_objAlternative == null )
                {
                    throw new SugarImporterException("Critical error alternative unit id " + t_strAttribute + " never declarated before.");
                }
                List t_lUnitElements = t_objUnitElement.getChildren();
                for (Iterator t_iterSubstructure = t_lUnitElements.iterator(); t_iterSubstructure.hasNext();) 
                {
                    Element t_objSubstructuresElement = (Element) t_iterSubstructure.next();
                    if ( t_objSubstructuresElement.getName().equals("substructure") )
                    {
                        // substructures
                        GlycoGraphAlternative t_objAltGraph = new GlycoGraphAlternative();
                        t_objAlternative.addAlternative(t_objAltGraph);
                        this.m_objSugarUnit = t_objAltGraph;
                        List t_lSubElements = t_objSubstructuresElement.getChildren();             
                        for (Iterator t_iterSubElements = t_lSubElements.iterator(); t_iterSubElements.hasNext();) 
                        {
                            Element t_objSubElement = (Element) t_iterSubElements.next();
                            if (t_objSubElement.getName().equals("residues"))
                            {
                                this.parseResidueSection(t_objSubElement);
                            }
                            else if (t_objSubElement.getName().equals("linkages"))
                            {
                                this.parseLinkageSection(t_objSubElement);
                            }
                            else if (t_objSubElement.getName().equals("lead_in"))
                            {
                                t_strAttribute = t_objSubElement.getAttributeValue("residue_id");
                                if ( t_strAttribute == null )
                                {
                                    throw new SugarImporterException("<lead_in> must have a residue_id.");
                                }
                                GlycoNode t_objNode = this.m_hashResidues.get(Integer.parseInt(t_strAttribute));
                                t_objAlternative.setLeadInNode(t_objNode,t_objAltGraph);
                            }
                            else if (t_objSubElement.getName().equals("lead_out"))
                            {
                                t_strAttribute = t_objSubElement.getAttributeValue("residue_id");
                                if ( t_strAttribute == null )
                                {
                                    throw new SugarImporterException("<lead_out> must have a residue_id.");
                                }
                                GlycoNode t_objNodeInner = this.m_hashResidues.get(Integer.parseInt(t_strAttribute));
                                t_strAttribute = t_objSubElement.getAttributeValue("connected_to");
                                if ( t_strAttribute == null )
                                {
                                    throw new SugarImporterException("<lead_out> must have a connected_to.");
                                }
                                GlycoNode t_objNodeOuter = this.m_hashResidues.get(Integer.parseInt(t_strAttribute));
                                if ( t_objNodeInner == null || t_objNodeOuter == null )
                                {
                                    throw new SugarImporterException("Values for lead out are invalide. Residues are not defined before.");
                                }
                                t_objAlternative.addLeadOutNodeToNode(t_objNodeInner,t_objAltGraph,t_objNodeOuter);
                            }
                        }
                    }    
                }
            }
        }
    }
}