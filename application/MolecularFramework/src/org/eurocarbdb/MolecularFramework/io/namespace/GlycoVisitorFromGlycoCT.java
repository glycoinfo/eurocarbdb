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
package org.eurocarbdb.MolecularFramework.io.namespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserTree;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbAnomer;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbBaseType;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbMonosaccharide;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbSuperclass;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConversion;
import org.eurocarbdb.resourcesdb.io.MonosaccharideExchangeObject;
import org.eurocarbdb.resourcesdb.io.SubstituentExchangeObject;

/**
* @author Logan
*
*/
public class GlycoVisitorFromGlycoCT implements GlycoVisitor
{
    private Sugar  m_objNewSugar = null;
    private MonosaccharideConversion m_objConverter = null;
    private GlycoGraph m_objUnit = null;
    private HashMap<GlycoNode,GlycoNode> m_hashResidues = new HashMap<GlycoNode,GlycoNode>();
    private GlycanNamescheme m_strSchema = GlycanNamescheme.GLYCOSCIENCES;
    private GlycoTraverser m_objTraverser = null;
    private SugarUnitRepeat m_objRepeat = null;
    private UnderdeterminedSubTree m_objSubTree = null;
    private ArrayList<AlternativeMapping> m_aAlternative = new ArrayList<AlternativeMapping>(); 
    private GlycoGraphAlternative m_objAlternative = null;

    /**
     * @param residueTranslator
     */
    public GlycoVisitorFromGlycoCT(MonosaccharideConversion a_objTranslator) 
    {
        super();
        this.m_objConverter = a_objTranslator;
    }

    public GlycoVisitorFromGlycoCT(MonosaccharideConversion a_objTranslator, GlycanNamescheme a_strSchema) 
    {
        super();
        this.m_objConverter = a_objTranslator;
        this.m_strSchema = a_strSchema;
    }

    /**
     * @throws GlycoVisitorException 
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#getTraverser(de.glycosciences.MolecularFrameWork.util.GlycoVisitor)
     */
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {
        return new GlycoTraverserTree(a_objVisitor);
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#clear()
     */
    public void clear()
    {
        this.m_objNewSugar = null;
        this.m_objUnit = null;
        this.m_hashResidues.clear();
        this.m_objRepeat = null;
        this.m_objSubTree = null;
        this.m_aAlternative.clear();
        this.m_objAlternative = null;
    }

    /**
     * @return
     */
    public Sugar getNormalizedSugar() 
    {
        return this.m_objNewSugar;
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.Linkage)
     */
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException
    {
        // nothing to do        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            try
            {
                GlycoNode t_objStart = this.m_hashResidues.get(a_objCyclic.getCyclicStart());
                GlycoEdge t_objEdge = a_objCyclic.getParentEdge();
                if ( t_objEdge == null )
                {
                    throw new GlycoVisitorException("Critical error in cyclic unit, no parent edge.");
                }
                GlycoNode t_objParent = this.m_hashResidues.get(a_objCyclic.getParentNode());
                if ( t_objStart == null || t_objParent == null )
                {
                    throw new GlycoVisitorException("Critical error in cyclic unit.");
                }
                // copy linkage
                t_objEdge = t_objEdge.copy();
                // reset to unvalidated
                Linkage t_objLinkage;
                for (Iterator<Linkage> t_iterLinkages = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();) 
                {
                    t_objLinkage = t_iterLinkages.next();
                    t_objLinkage.setParentLinkageType(LinkageType.UNVALIDATED);
                    t_objLinkage.setChildLinkageType(LinkageType.UNVALIDATED);
                }
                this.m_objUnit.addEdge(t_objParent,t_objStart,t_objEdge);
            } 
            catch (GlycoconjugateException e)
            {
                throw new GlycoVisitorException("Could not create cyclic residue : " + e.getMessage(),e);
            }
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode)
     */
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("UnvalidatedGlycoNode are not allowed for this visitor.");        
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.SugarUnitRepeat)
     */
    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            GlycoVisitorFromGlycoCT t_objVisitor = new GlycoVisitorFromGlycoCT( this.m_objConverter , this.m_strSchema );
            SugarUnitRepeat t_objUnit = t_objVisitor.start(a_objRepeate);
            this.m_hashResidues.put( a_objRepeate , t_objUnit ); 
            try
            {
                this.m_objUnit.addNode(t_objUnit);
            } 
            catch (GlycoconjugateException e)
            {
                throw new GlycoVisitorException("Could not create Repeat : ",e);
            }
            this.copyParentLinkage(a_objRepeate.getParentEdge(),t_objUnit);
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            SugarUnitAlternative t_objCopy = new SugarUnitAlternative();
            this.copyParentLinkage(a_objAlternative.getParentEdge(),t_objCopy);
            for (Iterator<GlycoGraphAlternative> t_iterGraphs = a_objAlternative.getAlternatives().iterator(); t_iterGraphs.hasNext();)
            {
                GlycoGraphAlternative t_objGraph = t_iterGraphs.next();
                GlycoVisitorFromGlycoCT t_objVisitor = new GlycoVisitorFromGlycoCT( this.m_objConverter , this.m_strSchema );
                GlycoGraphAlternative t_objGraphCopy = t_objVisitor.start(t_objGraph,t_objCopy);
                this.m_aAlternative.add(
                        new AlternativeMapping(
                                a_objAlternative,t_objGraph,
                                t_objCopy, t_objGraphCopy,
                                t_objVisitor.getResidueMapping()));                
            }            
            this.m_hashResidues.put( a_objAlternative , t_objCopy ); 
            try
            {
                this.m_objUnit.addNode(t_objCopy);
            } 
            catch (GlycoconjugateException e)
            {
                throw new GlycoVisitorException("Could not create alternative : ",e);
            }
        }
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.NonMonosaccharide)
     */
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            UnvalidatedGlycoNode t_objNode = new UnvalidatedGlycoNode();
            try 
            {
//                String t_strName = this.m_objConverter.convertAglycon(a_objResidue.getName(),
//                        GlycanNamescheme.GLYCOCT,
//                        this.m_strSchema);
//                t_objNode.setName(t_strName);
                t_objNode.setName(a_objResidue.getName());
            } 
            catch (GlycoconjugateException e) 
            {
                throw new GlycoVisitorException(e.getMessage(),e);
            } 
//            catch (ResourcesDbException e) 
//            {
//                throw new GlycoVisitorException(e.getMessage(),e);
//            }
            this.m_hashResidues.put(a_objResidue,t_objNode);
            try 
            {
                this.m_objUnit.addNode(t_objNode);
            } 
            catch (GlycoconjugateException e) 
            {
                throw new GlycoVisitorException(e.getMessage(),e);
            }
            // copy linkage
            this.copyParentLinkage(a_objResidue.getParentEdge(),t_objNode);
        }
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            // do normalisation
            try
            {
                MonosaccharideExchangeObject t_objData = new MonosaccharideExchangeObject(); 
                t_objData.setBasetype( this.createBaseType(a_objMonosaccharid));
                // now fill substituent exchange object
                SubstituentExchangeObject t_objSubst;
                for (Iterator<GlycoEdge> t_iterEdges = a_objMonosaccharid.getChildEdges().iterator(); t_iterEdges.hasNext();)
                {
                    GlycoEdge t_objEdge = t_iterEdges.next(); 
                    GlycoNode t_objResidue = t_objEdge.getChild(); 
                    if ( t_objResidue.getClass() == Substituent.class )
                    {
                        Substituent t_objNonMS = (Substituent)t_objResidue;
                        if ( t_objNonMS.getChildEdges().size() == 0 )
                        {
                            t_objSubst = new SubstituentExchangeObject(GlycanNamescheme.GLYCOCT);
                            t_objSubst.setName(t_objNonMS.getSubstituentType().getName());
                            int t_iCounter = 0;
                            for (Iterator<Linkage> t_iterLinkages = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();) 
                            {
                                t_iCounter++;
                                Linkage t_objLinkage = t_iterLinkages.next();
                                if ( t_iCounter == 1 )
                                {
                                    t_objSubst.setLinkagetype1( this.createLinkageType(t_objLinkage.getParentLinkageType()));
                                    t_objSubst.setPosition1(this.createExchangeLinkagePosition(t_objLinkage.getParentLinkages()));
                                    t_objSubst.setSubstituentPosition1(this.createExchangeLinkagePosition(t_objLinkage.getChildLinkages()));
                                }
                                else if (t_iCounter == 2)
                                {
                                    t_objSubst.setLinkagetype2( this.createLinkageType(t_objLinkage.getParentLinkageType()));
                                    t_objSubst.setPosition2(this.createExchangeLinkagePosition(t_objLinkage.getParentLinkages()));
                                    t_objSubst.setSubstituentPosition2(this.createExchangeLinkagePosition(t_objLinkage.getChildLinkages()));
                                }
                                else if (t_iCounter == 3)
                                {
                                    t_objSubst.setLinkagetype3( this.createLinkageType(t_objLinkage.getParentLinkageType()));
                                    t_objSubst.setPosition3(this.createExchangeLinkagePosition(t_objLinkage.getParentLinkages()));
                                    t_objSubst.setSubstituentPosition3(this.createExchangeLinkagePosition(t_objLinkage.getChildLinkages()));
                                }
                                else
                                {
                                    throw new GlycoVisitorException("Substitutent " + t_objNonMS.getSubstituentType().getName() + " links to often to monosaccharide " + a_objMonosaccharid.getGlycoCTName() );
                                }    
                            }
                            t_objData.addSubstituent(t_objSubst);
                        }
                    }
                    // others are not of interest
                }
                // normalise
                t_objData = this.m_objConverter.convertMonosaccharide(t_objData,
                        GlycanNamescheme.GLYCOCT,
                        this.m_strSchema);
                // get name
                UnvalidatedGlycoNode t_objNonMS = new UnvalidatedGlycoNode();
                t_objNonMS.setName(t_objData.getMonosaccharideName());
                this.m_hashResidues.put(a_objMonosaccharid,t_objNonMS);
                this.m_objUnit.addNode(t_objNonMS);
                // now we have to find out which residue was collapsed and which not
                ArrayList<SubstituentExchangeObject> t_aSubsListe = t_objData.getSubstituents();
                for (Iterator<GlycoEdge> t_iterResidues = a_objMonosaccharid.getChildEdges().iterator(); t_iterResidues.hasNext();)
                {                
                    GlycoEdge t_objSubLinkage = t_iterResidues.next(); 
                    GlycoNode t_objResidue = t_objSubLinkage.getChild(); 
                    if ( t_objResidue.getClass() == Substituent.class )
                    {
                        if ( this.isCollapsed( t_objSubLinkage, (Substituent)t_objResidue, t_aSubsListe) )
                        {
                            // yes
                            this.m_hashResidues.put(t_objResidue,t_objNonMS);
                        }
                    }
                }
                // copy parent
                this.copyParentLinkage(a_objMonosaccharid.getParentEdge(),t_objNonMS);
            }
            catch (ResourcesDbException e)
            {
                // not a monosaccharide?
                throw new GlycoVisitorException("Unable to translate " + a_objMonosaccharid.getGlycoCTName() + " : " + e.getMessage(),e);
            } 
            catch (GlycoconjugateException e) 
            {
                throw new GlycoVisitorException(e.getMessage(),e);
            } 
            catch (org.eurocarbdb.resourcesdb.glycoconjugate_derived.GlycoconjugateException e) 
            {
                throw new GlycoVisitorException(e.getMessage(),e);
            }
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Substituent)
     */
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            if ( !this.m_hashResidues.containsKey(a_objSubstituent) )
            {
                // not collapsed
                // translate Substitutent
                SubstituentExchangeObject t_objSubst = new SubstituentExchangeObject(GlycanNamescheme.GLYCOCT);
                t_objSubst.setName( a_objSubstituent.getSubstituentType().getName() );
                int t_iCounter = 0;
                // parents
                if ( a_objSubstituent.getParentEdge() != null )
                {
                    for (Iterator<Linkage> t_iterParents = a_objSubstituent.getParentEdge().getGlycosidicLinkages().iterator(); t_iterParents.hasNext();)
                    {
                        t_iCounter++;
                        Linkage t_objElement = t_iterParents.next();
                        if ( t_iCounter == 1 )
                        {
                            t_objSubst.setLinkagetype1( this.createLinkageType(t_objElement.getParentLinkageType()) );
                            t_objSubst.setSubstituentPosition1(t_objElement.getChildLinkages());
                        }
                        else if ( t_iCounter == 2 )
                        {
                            t_objSubst.setLinkagetype2( this.createLinkageType(t_objElement.getParentLinkageType()));
                            t_objSubst.setSubstituentPosition2(t_objElement.getChildLinkages());
                        }
                        else if ( t_iCounter == 3 )
                        {
                            t_objSubst.setLinkagetype3( this.createLinkageType(t_objElement.getParentLinkageType()));
                            t_objSubst.setSubstituentPosition3(t_objElement.getChildLinkages());
                        }
                        else
                        {
                            throw new GlycoVisitorException("Substitutent " + a_objSubstituent.getSubstituentType().getName() + " has too many connections.");
                        }
                    }
                }
                // childs
                for (Iterator<GlycoEdge> t_iterChilds = a_objSubstituent.getChildEdges().iterator(); t_iterChilds.hasNext();) 
                {
                    for (Iterator<Linkage> t_iterLinkages = t_iterChilds.next().getGlycosidicLinkages().iterator();t_iterLinkages.hasNext();)
                    {
                        t_iCounter++;
                        Linkage t_objElement = t_iterLinkages.next();
                        if ( t_iCounter == 1 )
                        {
                            t_objSubst.setLinkagetype1( this.createLinkageType(t_objElement.getChildLinkageType()) );
                            t_objSubst.setSubstituentPosition1(t_objElement.getChildLinkages());
                        }
                        else if ( t_iCounter == 2 )
                        {
                            t_objSubst.setLinkagetype2( this.createLinkageType(t_objElement.getChildLinkageType()));
                            t_objSubst.setSubstituentPosition2(t_objElement.getChildLinkages());
                        }
                        else if ( t_iCounter == 3 )
                        {
                            t_objSubst.setLinkagetype3( this.createLinkageType(t_objElement.getChildLinkageType()));
                            t_objSubst.setSubstituentPosition3(t_objElement.getChildLinkages());
                        }
                        else
                        {
                            throw new GlycoVisitorException("Substitutent " + a_objSubstituent.getSubstituentType().getName() + " has too many connections.");
                        }
                    }
                }                    
                // do translation
                try
                {
                    t_objSubst = this.m_objConverter.convertSubstituent(t_objSubst,GlycanNamescheme.GLYCOCT,this.m_strSchema);
                    UnvalidatedGlycoNode t_objNew = new UnvalidatedGlycoNode();
                    t_objNew.setName(t_objSubst.getName());
                    this.m_hashResidues.put(a_objSubstituent,t_objNew);
                    this.m_objUnit.addNode(t_objNew);
                    // copy parent linkage
                    this.copyParentLinkage(a_objSubstituent.getParentEdge(),t_objNew);
                } 
                catch (ResourcesDbException e)
                {
                    throw new GlycoVisitorException("Unable to translate " + a_objSubstituent.getSubstituentType().getName() + " : " + e.getMessage(),e);
                } 
                catch (GlycoconjugateException e) 
                {
                    throw new GlycoVisitorException(e.getMessage(),e);
                }
            }
        }        
    }

    /**
     * @param childLinkageType
     * @return
     * @throws GlycoVisitorException 
     * @throws org.eurocarbdb.glycoconjugate.GlycoconjugateException 
     */
    private org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType createLinkageType(LinkageType a_objLinkageType) throws GlycoVisitorException  
    {
        try 
        {
            return org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType.forName(a_objLinkageType.getType());
        } 
        catch (org.eurocarbdb.resourcesdb.glycoconjugate_derived.GlycoconjugateException e) 
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
    }

    /**
     * @param subLinkage
     * @param subsListe
     * @return
     */
    private boolean isCollapsed(GlycoEdge a_objSubLinakge, Substituent a_objResidue, ArrayList<SubstituentExchangeObject> a_aSubstitutents)
    {
        for (Iterator<SubstituentExchangeObject> t_iterSubst = a_aSubstitutents.iterator(); t_iterSubst.hasNext();)
        {
            SubstituentExchangeObject t_objSubst = t_iterSubst.next();
            if ( this.isSubstituten( a_objResidue, a_objSubLinakge,t_objSubst) )
            {
                return false;
            }
        }
        return true;
    }


    /**
     * @param residue
     * @param subLinakge
     * @param subst
     * @return
     */
    private boolean isSubstituten(Substituent a_objSubst, GlycoEdge a_objEdge, SubstituentExchangeObject a_objSubstExchange) 
    {
        if ( a_objSubstExchange.getOriginalName().equals(a_objSubst.getSubstituentType().getName()) ) 
        {
            int t_iCounter = 0;
            if ( a_objSubstExchange.getLinkagetype1() != null )
            {
                t_iCounter++;
            }
            if ( a_objSubstExchange.getLinkagetype2() != null )
            {
                t_iCounter++;
            }
            if ( a_objSubstExchange.getLinkagetype3() != null )
            {
                t_iCounter++;
            }
            if ( t_iCounter != a_objEdge.getGlycosidicLinkages().size() )
            {
                return false;
            }
            // same name, now we have to compare the rest
            for (Iterator<Linkage> t_iterLinkage = a_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();) 
            {
                Linkage t_objLinkage = t_iterLinkage.next();
                if ( !this.isLinkage(t_objLinkage,a_objSubstExchange.getLinkagetype1(),this.createNormalLinkagePosition(a_objSubstExchange.getPosition1()),this.createNormalLinkagePosition(a_objSubstExchange.getSubstituentPosition1())) )
                {
                    if ( !this.isLinkage(t_objLinkage,a_objSubstExchange.getLinkagetype2(),this.createNormalLinkagePosition(a_objSubstExchange.getPosition2()),this.createNormalLinkagePosition(a_objSubstExchange.getSubstituentPosition2())) ) 
                    {
                        if ( !this.isLinkage(t_objLinkage,a_objSubstExchange.getLinkagetype3(),this.createNormalLinkagePosition(a_objSubstExchange.getPosition3()),this.createNormalLinkagePosition(a_objSubstExchange.getSubstituentPosition3())) ) 
                        {
                            return false;
                        }
                    }
                }
            }            
            return true;
        }
        return false;
    }

    /**
     * @param linkage
     * @param linkagetype3
     * @param position3
     * @param substituentPosition3
     * @return
     */
    private boolean isLinkage(Linkage a_objLinkage, org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType a_objLinkageType, ArrayList<Integer> a_aPositionsParent, ArrayList<Integer> a_aPositionsSubst) 
    {
        if ( a_objLinkageType == null )
        {
            return false;
        }
        if ( a_objLinkageType.getType() != a_objLinkage.getParentLinkageType().getType() )
        {
            return false;
        }
        if ( a_aPositionsParent.size() != a_objLinkage.getParentLinkages().size() )
        {
            return false;
        }
        if ( a_aPositionsSubst.size() != a_objLinkage.getChildLinkages().size() )
        {
            return false;
        }
        for (Iterator<Integer> t_iterPosition = a_objLinkage.getParentLinkages().iterator(); t_iterPosition.hasNext();) 
        {
            Integer t_iPosition = t_iterPosition.next();
            if ( t_iPosition == Linkage.UNKNOWN_POSITION )
            {
                if ( !a_aPositionsParent.contains(-1) )
                {
                    return false;
                }
            }
            else
            {
                if ( !a_aPositionsParent.contains(t_iPosition) )
                {
                    return false;
                }
            }            
        }
        for (Iterator<Integer> t_iterPosition = a_objLinkage.getChildLinkages().iterator(); t_iterPosition.hasNext();) 
        {
            Integer t_iPosition = t_iterPosition.next();
            if ( t_iPosition == Linkage.UNKNOWN_POSITION )
            {
                if ( !a_aPositionsSubst.contains(-1) )
                {
                    return false;
                }
            }
            else
            {
                if ( !a_aPositionsSubst.contains(t_iPosition) )
                {
                    return false;
                }
            }            
        }
        return true;
    }

    /**
     * @param sugar
     * @throws GlycoVisitorException 
     */
    public void start(Sugar a_objSugar) throws GlycoVisitorException 
    {
        this.clear();
        if ( this.m_objConverter != null )
        {
            // create new Sugar object
            this.m_objNewSugar = new Sugar();
            this.m_objUnit = this.m_objNewSugar;
            // traverse Sugar and copy to new sugar
            this.m_objTraverser = this.getTraverser(this);
            this.m_objTraverser.traverseGraph(a_objSugar);
            // speczial trees
            try 
            {
                for (Iterator<UnderdeterminedSubTree> t_iterSpezialTrees = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterSpezialTrees.hasNext();) 
                {
                    GlycoVisitorFromGlycoCT t_objVisitor = new GlycoVisitorFromGlycoCT( this.m_objConverter , this.m_strSchema );
                    UnderdeterminedSubTree t_objSubtreeOriginal = t_iterSpezialTrees.next();
                    UnderdeterminedSubTree t_objSubtree = t_objVisitor.start(t_objSubtreeOriginal);
                    this.m_objNewSugar.addUndeterminedSubTree(t_objSubtree);
                    // copy properties
                    GlycoEdge t_objEdge = t_objSubtreeOriginal.getConnection().copy();
                    // reset to unvalidated
                    Linkage t_objLinkage;
                    for (Iterator<Linkage> t_iterLinkages = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();) 
                    {
                        t_objLinkage = t_iterLinkages.next();
                        t_objLinkage.setParentLinkageType(LinkageType.UNVALIDATED);
                        t_objLinkage.setChildLinkageType(LinkageType.UNVALIDATED);
                    }
                    t_objSubtree.setConnection( t_objEdge );
                    t_objSubtree.setProbability( 
                            t_objSubtreeOriginal.getProbabilityLower() , 
                            t_objSubtreeOriginal.getProbabilityUpper() );
                    // copy parents
                    for (Iterator<GlycoNode> t_iterParents = t_objSubtreeOriginal.getParents().iterator(); t_iterParents.hasNext();) 
                    {
                        GlycoNode t_objParent = this.m_hashResidues.get(t_iterParents.next());
                        if ( t_objParent == null )
                        {
                            throw new GlycoVisitorException("Crictial error by adding null as a parent of a UnderdeterminedSubTree.");
                        }
                        this.m_objNewSugar.addUndeterminedSubTreeParent(t_objSubtree, t_objParent);
                    }
                }
                // correkt alternative attache positions
                for (Iterator<AlternativeMapping> t_iterAlternative = this.m_aAlternative.iterator(); t_iterAlternative.hasNext();)
                {
                    AlternativeMapping t_objAlternative = t_iterAlternative.next();
                    GlycoNode t_objInnerOld;
                    GlycoNode t_objInnerNew;
                    GlycoNode t_objOuterOld;
                    GlycoNode t_objOuterNew;
                    HashMap<GlycoNode,GlycoNode> t_hMapNew = new HashMap<GlycoNode,GlycoNode>();
                    HashMap<GlycoNode,GlycoNode> t_hMapOld = t_objAlternative.getGraphOriginal().getLeadOutNodeToNode();
                    HashMap<GlycoNode,GlycoNode> t_hIntern = t_objAlternative.getMapping();
                    for (Iterator<GlycoNode> t_iterPositions = t_hMapOld.keySet().iterator(); t_iterPositions.hasNext();)
                    {
                        // for each old lead out
                        t_objOuterOld = t_iterPositions.next();
                        t_objOuterNew = this.m_hashResidues.get(t_objOuterOld);
                        if ( t_objOuterNew == null )
                        {
                            throw new GlycoconjugateException("Error child attache position of alternative graph was not translated.");                 
                        }
                        t_objInnerOld = t_hMapOld.get(t_objOuterOld);
                        t_objInnerNew = t_hIntern.get(t_objInnerOld);
                        if ( t_objOuterNew == null )
                        {
                            throw new GlycoconjugateException("Error child inner attache position of alternative graph was not translated.");                 
                        }
                        t_hMapNew.put(t_objOuterNew, t_objInnerNew);
                    }
                    t_objAlternative.getCopy().setLeadOutNodeToNode(t_hMapNew,t_objAlternative.getGraphCopy());
                }
            } 
            catch (GlycoconjugateException e) 
            {
                throw new GlycoVisitorException(e.getMessage(),e);
            }
        }            
    }

    /**
     * @param sugar
     * @throws GlycoVisitorException 
     */
    private SugarUnitRepeat start(SugarUnitRepeat a_objSugar) throws GlycoVisitorException 
    {
        this.clear();
        if ( this.m_objConverter != null )
        {
            // create new Sugar object
            this.m_objRepeat = new SugarUnitRepeat();
            this.m_objUnit = this.m_objRepeat;
            // traverse Sugar and fill Residue Hashmap
            this.m_objTraverser = this.getTraverser(this);
            this.m_objTraverser.traverseGraph(a_objSugar);
            // copie repeat
            this.m_objRepeat.setMinRepeatCount( a_objSugar.getMinRepeatCount() );
            this.m_objRepeat.setMaxRepeatCount( a_objSugar.getMaxRepeatCount() );
            try 
            {
                // repeat linkage
                GlycoEdge t_objOringal = a_objSugar.getRepeatLinkage();
                GlycoEdge t_objInternal = t_objOringal.copy();
                Linkage t_objLinkage;
                for (Iterator<Linkage> t_iterLinkages = t_objInternal.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();) 
                {
                    t_objLinkage = t_iterLinkages.next();
                    t_objLinkage.setParentLinkageType(LinkageType.UNVALIDATED);
                    t_objLinkage.setChildLinkageType(LinkageType.UNVALIDATED);
                }
                this.m_objRepeat.setRepeatLinkage(t_objInternal,
                        this.m_hashResidues.get(t_objOringal.getParent()),
                        this.m_hashResidues.get(t_objOringal.getChild()));
                // speczial trees
                for (Iterator<UnderdeterminedSubTree> t_iterSpezialTrees = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterSpezialTrees.hasNext();) 
                {
                    GlycoVisitorFromGlycoCT t_objVisitor = new GlycoVisitorFromGlycoCT( this.m_objConverter  , this.m_strSchema );
                    UnderdeterminedSubTree t_objSubtreeOriginal = t_iterSpezialTrees.next();
                    UnderdeterminedSubTree t_objSubtree = t_objVisitor.start(t_objSubtreeOriginal);
                    this.m_objRepeat.addUndeterminedSubTree(t_objSubtree);
                    // copy properties
                    GlycoEdge t_objEdge = t_objSubtreeOriginal.getConnection().copy();
                    // reset to unvalidated
                    for (Iterator<Linkage> t_iterLinkages = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();) 
                    {
                        t_objLinkage = t_iterLinkages.next();
                        t_objLinkage.setParentLinkageType(LinkageType.UNVALIDATED);
                        t_objLinkage.setChildLinkageType(LinkageType.UNVALIDATED);
                    }
                    t_objSubtree.setConnection( t_objEdge );
                    t_objSubtree.setProbability( 
                            t_objSubtreeOriginal.getProbabilityLower() , 
                            t_objSubtreeOriginal.getProbabilityUpper() );
                    // copy parents
                    for (Iterator<GlycoNode> t_iterParents = t_objSubtreeOriginal.getParents().iterator(); t_iterParents.hasNext();) 
                    {
                        GlycoNode t_objParent = this.m_hashResidues.get(t_iterParents.next());
                        if ( t_objParent == null )
                        {
                            throw new GlycoVisitorException("Crictial error by adding null as a parent of a UnderdeterminedSubTree.");
                        }
                        this.m_objRepeat.addUndeterminedSubTreeParent(t_objSubtree, t_objParent);
                    }
                }
                // correkt alternative attache positions
                for (Iterator<AlternativeMapping> t_iterAlternative = this.m_aAlternative.iterator(); t_iterAlternative.hasNext();)
                {
                    AlternativeMapping t_objAlternative = t_iterAlternative.next();
                    GlycoNode t_objInnerOld;
                    GlycoNode t_objInnerNew;
                    GlycoNode t_objOuterOld;
                    GlycoNode t_objOuterNew;
                    HashMap<GlycoNode,GlycoNode> t_hMapNew = new HashMap<GlycoNode,GlycoNode>();
                    HashMap<GlycoNode,GlycoNode> t_hMapOld = t_objAlternative.getGraphOriginal().getLeadOutNodeToNode();
                    HashMap<GlycoNode,GlycoNode> t_hIntern = t_objAlternative.getMapping();
                    for (Iterator<GlycoNode> t_iterPositions = t_hMapOld.keySet().iterator(); t_iterPositions.hasNext();)
                    {
                        // for each old lead out
                        t_objOuterOld = t_iterPositions.next();
                        t_objOuterNew = this.m_hashResidues.get(t_objOuterOld);
                        if ( t_objOuterNew == null )
                        {
                            throw new GlycoconjugateException("Error child attache position of alternative graph was not translated.");                 
                        }
                        t_objInnerOld = t_hMapOld.get(t_objOuterOld);
                        t_objInnerNew = t_hIntern.get(t_objInnerOld);
                        if ( t_objOuterNew == null )
                        {
                            throw new GlycoconjugateException("Error child inner attache position of alternative graph was not translated.");                 
                        }
                        t_hMapNew.put(t_objOuterNew, t_objInnerNew);
                    }
                    t_objAlternative.getCopy().setLeadOutNodeToNode(t_hMapNew,t_objAlternative.getGraphCopy());
                }
            } 
            catch (GlycoconjugateException e) 
            {
                throw new GlycoVisitorException(e.getMessage(),e);
            }
        }   
        return this.m_objRepeat;
    }

    /**
     * @param subtreeOriginal
     * @return
     * @throws GlycoVisitorException 
     */
    private UnderdeterminedSubTree start(UnderdeterminedSubTree a_objSubtree) throws GlycoVisitorException 
    {
        this.clear();
        if ( this.m_objConverter != null )
        {
            // create new Sugar object
            this.m_objSubTree = new UnderdeterminedSubTree();
            this.m_objUnit = this.m_objSubTree;
            // traverse Sugar and fill Residue Hashmap
            this.m_objTraverser = this.getTraverser(this);
            this.m_objTraverser.traverseGraph(a_objSubtree);
        }
        return this.m_objSubTree;
    }

    private void copyParentLinkage(GlycoEdge a_objOrigin, GlycoNode a_objNode) throws GlycoVisitorException
    {
        if ( a_objOrigin != null )
        {
            GlycoNode t_objParent = this.m_hashResidues.get(a_objOrigin.getParent());
            if ( t_objParent == null )
            {
                throw new GlycoVisitorException("Critical error while translating: could not found a parent residue.");
            }
            GlycoEdge t_objNewLinkage;
            try 
            {
                t_objNewLinkage = a_objOrigin.copy();
                Linkage t_objLinkage;
                for (Iterator<Linkage> t_iterLinkages = t_objNewLinkage.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();) 
                {
                    t_objLinkage = t_iterLinkages.next();
                    t_objLinkage.setParentLinkageType(LinkageType.UNVALIDATED);
                    t_objLinkage.setChildLinkageType(LinkageType.UNVALIDATED);
                }
                this.m_objUnit.addEdge(t_objParent,a_objNode,t_objNewLinkage);
            } 
            catch (GlycoconjugateException e) 
            {
                throw new GlycoVisitorException(e.getMessage(),e);
            }
        }
    }

    /**
     * @param monosaccharid
     * @return
     * @throws GlycoconjugateException 
     * @throws org.eurocarbdb.glycoconjugate.GlycoconjugateException 
     */
    private EcdbMonosaccharide createBaseType(Monosaccharide a_objMS) throws org.eurocarbdb.resourcesdb.glycoconjugate_derived.GlycoconjugateException, GlycoconjugateException 
    {
        // anomer & superclass
        EcdbMonosaccharide t_objMS = new EcdbMonosaccharide( 
                EcdbAnomer.forName( a_objMS.getAnomer().getSymbol().charAt(0) ),
                EcdbSuperclass.forCAtoms( a_objMS.getSuperclass().getCAtomCount()));
        // ring
        Integer t_objStart;
        Integer t_objEnd;
        if ( a_objMS.getRingEnd() == Monosaccharide.UNKNOWN_RING )
        {
            t_objEnd = EcdbMonosaccharide.UNKNOWN_RING;
        }
        else
        {
            if ( a_objMS.getRingEnd() == Monosaccharide.OPEN_CHAIN )
            {
                t_objEnd = EcdbMonosaccharide.OPEN_CHAIN;
            }
            else
            {
                t_objEnd = a_objMS.getRingEnd();
            }
        }
        if ( a_objMS.getRingStart() == Monosaccharide.UNKNOWN_RING )
        {
            t_objStart = EcdbMonosaccharide.UNKNOWN_RING;
        }
        else
        {
            if ( a_objMS.getRingStart() == Monosaccharide.OPEN_CHAIN )
            {
                t_objStart = Monosaccharide.OPEN_CHAIN;
            }
            else
            {
                t_objStart = a_objMS.getRingStart();
            }
        }
        t_objMS.setRing(t_objStart,t_objEnd);
        // basetype
        for (Iterator<BaseType> t_iterBasetype = a_objMS.getBaseType().iterator(); t_iterBasetype.hasNext();) 
        {
            t_objMS.addBaseType(EcdbBaseType.forName(t_iterBasetype.next().getName()));         
        }
        // modification
        for (Iterator<Modification> t_iterModification = a_objMS.getModification().iterator(); t_iterModification.hasNext();)
        {
            t_objMS.addModification( this.createModification(t_iterModification.next()));
        }
        return t_objMS;
    }

    private EcdbModification createModification(Modification a_objModification) throws org.eurocarbdb.resourcesdb.glycoconjugate_derived.GlycoconjugateException 
    {
        if ( a_objModification.hasPositionTwo() )
        {
            return new EcdbModification(a_objModification.getName(),a_objModification.getPositionOne(),a_objModification.getPositionTwo());
        }
        return new EcdbModification(a_objModification.getName(),a_objModification.getPositionOne());
    }

    /**
     * parent and child connections are not translated
     * @param alternative
     * @return
     * @throws GlycoVisitorException 
     */
    private GlycoGraphAlternative start(GlycoGraphAlternative a_objAlternative,SugarUnitAlternative a_objAlternativeUnit) throws GlycoVisitorException
    {
        this.clear();
        if ( this.m_objConverter != null )
        {
            try
            {
                // create new Sugar object
                this.m_objAlternative = new GlycoGraphAlternative();
                this.m_objUnit = this.m_objAlternative;
                a_objAlternativeUnit.addAlternative(this.m_objAlternative);
                // traverse Sugar and fill Residue Hashmap
                this.m_objTraverser = this.getTraverser(this);
                this.m_objTraverser.traverseGraph(a_objAlternative);
                // copy parent connection information
                GlycoNode t_objNode = a_objAlternative.getLeadInNode();
                if ( t_objNode != null )
                {
                    t_objNode = this.m_hashResidues.get(t_objNode);
                    if ( t_objNode == null )
                    {
                        throw new GlycoVisitorException("Error translating alternative parent attach node.");
                    }
                    a_objAlternativeUnit.setLeadInNode(t_objNode,this.m_objAlternative);
                }
            } 
            catch (GlycoconjugateException e)
            {
                throw new GlycoVisitorException(e.getMessage(),e);
            }
        }
        return this.m_objAlternative;
    }

    private HashMap<GlycoNode,GlycoNode> getResidueMapping()
    {
        return this.m_hashResidues;
    }

    public void setNameScheme(GlycanNamescheme a_strSchema )
    {
        this.m_strSchema =a_strSchema; 
    }

    private ArrayList<Integer> createExchangeLinkagePosition(ArrayList<Integer> a_aPositions) 
    {
        ArrayList<Integer> t_aPositions = new ArrayList<Integer>();
        for (Iterator<Integer> t_iter = a_aPositions.iterator(); t_iter.hasNext();) 
        {
            Integer t_iPos = t_iter.next();
            if ( t_iPos == Linkage.UNKNOWN_POSITION )
            {
                t_aPositions.add(0);
            }
            else
            {
                t_aPositions.add(t_iPos);                
            }        
        }
        return t_aPositions;
    }

    private ArrayList<Integer> createNormalLinkagePosition(ArrayList<Integer> a_aPositions) 
    {
        ArrayList<Integer> t_aPositions = new ArrayList<Integer>();
        for (Iterator<Integer> t_iter = a_aPositions.iterator(); t_iter.hasNext();) 
        {
            Integer t_iPos = t_iter.next();
            if ( t_iPos == 0 )
            {
                t_aPositions.add(Linkage.UNKNOWN_POSITION);
            }
            else
            {
                t_aPositions.add(t_iPos);                
            }        
        }
        return t_aPositions;
    }

}