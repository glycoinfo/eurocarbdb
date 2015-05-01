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

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraphAlternative;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.LinkageType;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.SubstituentType;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbMonosaccharide;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConversion;
import org.eurocarbdb.resourcesdb.io.MonosaccharideExchangeObject;
import org.eurocarbdb.resourcesdb.io.SubstituentExchangeObject;


/**
* iterieren ueber alle residues ==> aufbauen einer hashmap ==> etablieren der Linkage
* 
* @author Logan
*
*/
public class GlycoVisitorToGlycoCTextendMSDB extends GlycoVisitorToGlycoCT
{
    public GlycoVisitorToGlycoCTextendMSDB(MonosaccharideConversion translator)
    {
        super(translator);
    }

    public GlycoVisitorToGlycoCTextendMSDB(MonosaccharideConversion translator,GlycanNamescheme schema)
    {
        super(translator, schema);
    }

    
    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            GlycoVisitorToGlycoCTextendMSDB t_objVisitor = new GlycoVisitorToGlycoCTextendMSDB( this.m_objConverter,this.m_strSchema );
            t_objVisitor.setUseStrict(this.m_bStrict);
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

    public void visit(UnvalidatedGlycoNode a_objResidue) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            // do normalisation
            try
            {
                MonosaccharideExchangeObject t_objData;
                t_objData = this.m_objConverter.convertMonosaccharide(a_objResidue.getName(),
                        this.m_strSchema,
                        GlycanNamescheme.GLYCOCT);
                EcdbMonosaccharide t_objMSdbMS = t_objData.getBasetype();
                Monosaccharide t_objMS = this.createMS(t_objMSdbMS);
                this.m_hashResidues.put( a_objResidue , t_objMS );
                try
                {
                    this.m_objUnit.addNode(t_objMS);
                } 
                catch (GlycoconjugateException e)
                {
                    throw new GlycoVisitorException("Could not create normalized monosaccharide : " + e.getMessage(),e);
                }
                ArrayList<Integer> t_aPositions;
                // attache Sustituents
                for (Iterator<SubstituentExchangeObject> t_iterSubst = t_objData.getSubstituents().iterator(); t_iterSubst.hasNext();)
                {
                    SubstituentExchangeObject t_objSubstMSdb = t_iterSubst.next();
                    Substituent t_objSubst = new Substituent( SubstituentType.forName(t_objSubstMSdb.getName()) );
                    // create Edge 
                    GlycoEdge t_objEdge = new GlycoEdge();
                    // create linkage object
                    Linkage t_objLinkage = new Linkage();
                    t_objLinkage.setParentLinkageType( LinkageType.forName( t_objSubstMSdb.getLinkagetype1().getType()) );
                    t_objLinkage.setChildLinkageType( LinkageType.NONMONOSACCHARID );
                    // fill with linkage positions (parent)
                    // 0 = unknown attach position
                    t_aPositions = t_objSubstMSdb.getPosition1();
                    for (Iterator<Integer> t_iterPosition = t_aPositions.iterator(); t_iterPosition.hasNext();) 
                    {
                        Integer t_iPosition = t_iterPosition.next();
                        if ( t_iPosition == 0 )
                        {
                            if ( t_aPositions.size() != 1 )
                            {
                                throw new GlycoVisitorException("Linkage for substituent " + t_objSubst.getSubstituentType().getName() + " can only be unknown or a destinct linkage, not both.");
                            }
                            t_objLinkage.addParentLinkage( Linkage.UNKNOWN_POSITION );
                        }
                        else
                        {
                            t_objLinkage.addParentLinkage( t_iPosition );
                        }
                    }
                    // fill with linkage positions (child)
                    t_aPositions = t_objSubstMSdb.getSubstituentPosition1();
                    for (Iterator<Integer> t_iterPosition = t_aPositions.iterator(); t_iterPosition.hasNext();) 
                    {
                        Integer t_iPosition = t_iterPosition.next();
                        if ( t_iPosition == 0 )
                        {
                            if ( t_aPositions.size() != 1 )
                            {
                                throw new GlycoVisitorException("Linkage at substituent " + t_objSubst.getSubstituentType().getName() + " can only be unknown or a destinct linkage, not both.");
                            }
                            t_objLinkage.addChildLinkage( Linkage.UNKNOWN_POSITION );
                        }
                        else
                        {
                            t_objLinkage.addChildLinkage( t_iPosition );
                        }
                    }
                    t_objEdge.addGlycosidicLinkage(t_objLinkage);
                    // second positon
                    t_aPositions = t_objSubstMSdb.getPosition2();
                    if ( t_aPositions.size() > 0 )
                    {
                        // add second linkage
                        t_objLinkage = new Linkage();
                        t_objLinkage.setParentLinkageType( LinkageType.forName( t_objSubstMSdb.getLinkagetype2().getType() ) );
                        t_objLinkage.setChildLinkageType(LinkageType.NONMONOSACCHARID );
                        // fill with linkage positions (parent)
                        // 0 = unknown attach position
                        t_aPositions = t_objSubstMSdb.getPosition2();
                        for (Iterator<Integer> t_iterPosition = t_aPositions.iterator(); t_iterPosition.hasNext();) 
                        {
                            Integer t_iPosition = t_iterPosition.next();
                            if ( t_iPosition == 0 )
                            {
                                if ( t_aPositions.size() != 1 )
                                {
                                    throw new GlycoVisitorException("Linkage for substituent " + t_objSubst.getSubstituentType().getName() + " can only be unknown or a destinct linkage, not both.");
                                }
                                t_objLinkage.addParentLinkage( Linkage.UNKNOWN_POSITION );
                            }
                            else
                            {
                                t_objLinkage.addParentLinkage( t_iPosition );
                            }
                        }
                        // fill with linkage positions (child)
                        t_aPositions = t_objSubstMSdb.getSubstituentPosition2();
                        for (Iterator<Integer> t_iterPosition = t_aPositions.iterator(); t_iterPosition.hasNext();) 
                        {
                            Integer t_iPosition = t_iterPosition.next();
                            if ( t_iPosition == 0 )
                            {
                                if ( t_aPositions.size() != 1 )
                                {
                                    throw new GlycoVisitorException("Linkage at substituent " + t_objSubst.getSubstituentType().getName() + " can only be unknown or a destinct linkage, not both.");
                                }
                                t_objLinkage.addChildLinkage( Linkage.UNKNOWN_POSITION );
                            }
                            else
                            {
                                t_objLinkage.addChildLinkage( t_iPosition );
                            }
                        }
                        t_objEdge.addGlycosidicLinkage(t_objLinkage);
                    }     
                    // add egde to sugar
                    this.m_objUnit.addNode(t_objSubst);
                    this.m_objUnit.addEdge(t_objMS,t_objSubst,t_objEdge);
                }
                // copy parent
                GlycoEdge t_objPEdge = a_objResidue.getParentEdge();
                if ( t_objPEdge != null )
                {
                    t_objPEdge = a_objResidue.getParentEdge().copy();
                    if ( t_objPEdge != null )
                    {
                        GlycoNode t_objParent = this.m_hashResidues.get( a_objResidue.getParentNode() );
                        if ( t_objParent == null )
                        {
                            throw new GlycoVisitorException("Critical error: missing parent residue for subtituent : " + t_objMS.getGlycoCTName() ); 
                        }
                        this.m_objUnit.addEdge( t_objParent,t_objMS,t_objPEdge );
                    }
                }
            } 
            catch (ResourcesDbException e)
            {
                // not a monosaccharide
                try
                {
                    SubstituentExchangeObject t_objSubstExchange = new SubstituentExchangeObject(this.m_strSchema);
                    t_objSubstExchange.setName(a_objResidue.getName());
                    t_objSubstExchange = this.m_objConverter.convertSubstituent(t_objSubstExchange,
                            this.m_strSchema,
                            GlycanNamescheme.GLYCOCT);
                    Substituent t_objSubst = new Substituent( SubstituentType.forName(t_objSubstExchange.getName()));
                    this.m_hashResidues.put( a_objResidue , t_objSubst );
                    try
                    {
                        this.m_objUnit.addNode(t_objSubst);
                    } 
                    catch (GlycoconjugateException e2)
                    {
                        throw new GlycoVisitorException("Could not create Substituten : " + t_objSubst.getSubstituentType().getName() ,e2);
                    }
                    // linkage type
                    int t_iCounter = 1;
                    if ( this.m_objInternalOriginalEdge != null )
                    {
                        // substituent is part of the repeat opening
                        if ( this.m_objInternalOriginalEdge.getChild() == a_objResidue)
                        {
                            for (Iterator<Linkage> t_iterLinkages = this.m_objInternal.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();) 
                            {
                                Linkage t_objLinkage = t_iterLinkages.next();
                                t_objLinkage.setChildLinkageType(LinkageType.NONMONOSACCHARID);
                                t_objLinkage.setParentLinkageType(this.getLinkageType(t_iCounter++,t_objSubstExchange));
                            }
                        }
                    }
                    GlycoEdge t_objPEdge = a_objResidue.getParentEdge();
                    if ( t_objPEdge != null )
                    {
                        t_objPEdge = a_objResidue.getParentEdge().copy();
                        if ( t_objPEdge != null )
                        {
                            // there is a parent
                            for (Iterator<Linkage> t_iterLinkages = t_objPEdge.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();) 
                            {
                                Linkage t_objLinkage = t_iterLinkages.next();
                                t_objLinkage.setChildLinkageType(LinkageType.NONMONOSACCHARID);
                                t_objLinkage.setParentLinkageType(this.getLinkageType(t_iCounter++,t_objSubstExchange));
                            }
                            GlycoNode t_objParent = this.m_hashResidues.get( a_objResidue.getParentNode() );
                            if ( t_objParent == null )
                            {
                                throw new GlycoVisitorException("Critical error: missing parent residue for subtituent : " + t_objSubst.getSubstituentType().getName() ); 
                            }
                            this.m_objUnit.addEdge( t_objParent,t_objSubst,t_objPEdge );
                        }
                    }
                    // add edge to sugar
                    for (Iterator<GlycoEdge> t_iterEdge = a_objResidue.getChildEdges().iterator(); t_iterEdge.hasNext();) 
                    {
                        t_objPEdge = t_iterEdge.next();
                        for (Iterator<Linkage> t_iterLinkages = t_objPEdge.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();) 
                        {
                            Linkage t_objLinkage = t_iterLinkages.next();
                            t_objLinkage.setParentLinkageType(LinkageType.NONMONOSACCHARID);
                            t_objLinkage.setChildLinkageType(this.getLinkageType(t_iCounter++,t_objSubstExchange));
                        }
                    }
                    if ( this.m_objInternalOriginalEdge != null )
                    {
                        // substituent is part of the repeat ending
                        if ( this.m_objInternalOriginalEdge.getParent() == a_objResidue)
                        {
                            for (Iterator<Linkage> t_iterLinkages = this.m_objInternal.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();) 
                            {
                                Linkage t_objLinkage = t_iterLinkages.next();
                                t_objLinkage.setParentLinkageType(LinkageType.NONMONOSACCHARID);
                                if ( t_objLinkage.getChildLinkageType() != LinkageType.NONMONOSACCHARID )
                                {
                                    t_objLinkage.setChildLinkageType(this.getLinkageType(t_iCounter++,t_objSubstExchange));
                                }
                            }
                        }
                    }

                } 
                catch (ResourcesDbException e1)
                {
                    try
                    {
                        // not a substituent
                        if ( this.m_bStrict )
                        {
                            // validate aglyca
                            String t_strName;
                            try 
                            {
                                t_strName = this.m_objConverter.convertAglycon(a_objResidue.getName(),
                                        this.m_strSchema,
                                        GlycanNamescheme.GLYCOCT);
                                NonMonosaccharide t_objNonMS = new NonMonosaccharide(t_strName);
                                this.m_hashResidues.put( a_objResidue , t_objNonMS );
                                this.m_objUnit.addNode(t_objNonMS);
                                // copy parent
                                GlycoEdge t_objPEdge = a_objResidue.getParentEdge();
                                if ( t_objPEdge != null )
                                {
                                    t_objPEdge = a_objResidue.getParentEdge().copy();
                                    if ( t_objPEdge != null )
                                    {
                                        GlycoNode t_objParent = this.m_hashResidues.get( a_objResidue.getParentNode() );
                                        if ( t_objParent == null )
                                        {
                                            throw new GlycoVisitorException("Critical error: missing parent residue for subtituent : " + t_objNonMS.getName() ); 
                                        }
                                        this.m_objUnit.addEdge( t_objParent,t_objNonMS,t_objPEdge );
                                    }
                                }
                            } 
                            catch (ResourcesDbException e2) 
                            {
                                // not a valid aglyca
                                throw new GlycoVisitorException("Unknown residue (aglyca?): " + a_objResidue.getName() );
                            }
                        }
                        else
                        {
                            String t_strName = a_objResidue.getName();
                            NonMonosaccharide t_objNonMS = new NonMonosaccharide(t_strName);
                            this.m_hashResidues.put( a_objResidue , t_objNonMS );
                            this.m_objUnit.addNode(t_objNonMS);
                            // copy parent
                            GlycoEdge t_objPEdge = a_objResidue.getParentEdge();
                            if ( t_objPEdge != null )
                            {
                                t_objPEdge = a_objResidue.getParentEdge().copy();
                                if ( t_objPEdge != null )
                                {
                                    GlycoNode t_objParent = this.m_hashResidues.get( a_objResidue.getParentNode() );
                                    if ( t_objParent == null )
                                    {
                                        throw new GlycoVisitorException("Critical error: missing parent residue for historical data : " + t_objNonMS.getName() ); 
                                    }
                                    this.m_objUnit.addEdge( t_objParent,t_objNonMS,t_objPEdge );
                                }
                            }
                        }
                    }
                    catch (GlycoconjugateException ex) 
                    {
                        throw new GlycoVisitorException(ex.getMessage(),ex);
                    } 
                }
                catch (GlycoconjugateException ex) 
                {
                    throw new GlycoVisitorException(ex.getMessage(),ex);
                }        
            } 
            catch (GlycoconjugateException e) 
            {
                throw new GlycoVisitorException(e.getMessage(),e);
            }
        }
    }
    
    protected LinkageType getLinkageType(int a_iCounter, SubstituentExchangeObject a_objSubstExchange) throws GlycoconjugateException 
    {
        if ( a_iCounter == 1 )
        {
            // position 1
            org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType t_objType = a_objSubstExchange.getLinkagetype1();
            if ( t_objType != null )
            {
                return LinkageType.forName( t_objType.getType() );
            }
        }
        else if ( a_iCounter == 2 )
        {
            // position 2
            org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType t_objType = a_objSubstExchange.getLinkagetype2();
            if ( t_objType != null )
            {
                return LinkageType.forName( t_objType.getType() );
            }
        }
        else 
        {
            // position 3
            org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType t_objType = a_objSubstExchange.getLinkagetype3();
            if ( t_objType != null )
            {
                return LinkageType.forName( t_objType.getType() );
            }
        }
        if ( a_iCounter == 2 )
        {
            if ( a_objSubstExchange.getName().equals(SubstituentType.ETHANOLAMINE.getName()) )
            {
                return LinkageType.H_AT_OH;
            }
            else if ( a_objSubstExchange.getName().equals(SubstituentType.AMINO.getName()) )
            {
                return LinkageType.DEOXY;
            }
            else if ( a_objSubstExchange.getName().equals(SubstituentType.PHOSPHATE.getName()) )
            {
                return LinkageType.H_AT_OH;
            }
            else if ( a_objSubstExchange.getName().equals(SubstituentType.SULFATE.getName()) )
            {
                return LinkageType.H_AT_OH;
            }
            else if ( a_objSubstExchange.getName().equals(SubstituentType.R_PYRUVATE.getName()) )
            {
                return LinkageType.DEOXY;
            }
            else if ( a_objSubstExchange.getName().equals(SubstituentType.S_PYRUVATE.getName()) )
            {
                return LinkageType.DEOXY;
            }
        }
        throw new GlycoconjugateException(
                String.format("SubstituentExchangeObject does not contain information for linkage %d at substituten %s.",a_iCounter,a_objSubstExchange.getName()));
    }

    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            SugarUnitAlternative t_objCopy = new SugarUnitAlternative();
            this.copyParentLinkage(a_objAlternative.getParentEdge(),t_objCopy);
            for (Iterator<GlycoGraphAlternative> t_iterGraphs = a_objAlternative.getAlternatives().iterator(); t_iterGraphs.hasNext();)
            {
                GlycoGraphAlternative t_objGraph = t_iterGraphs.next();
                GlycoVisitorToGlycoCTextendMSDB t_objVisitor = new GlycoVisitorToGlycoCTextendMSDB( this.m_objConverter,this.m_strSchema );
                t_objVisitor.setUseStrict(this.m_bStrict);
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

    protected SugarUnitRepeat start(SugarUnitRepeat a_objSugar) throws GlycoVisitorException 
    {
        this.clear();
        if ( this.m_objConverter != null )
        {
            // create new Sugar object
            this.m_objInternalOriginalEdge = a_objSugar.getRepeatLinkage();
            this.m_objRepeat = new SugarUnitRepeat();
            this.m_objUnit = this.m_objRepeat;
            // copie repeat
            this.m_objRepeat.setMinRepeatCount( a_objSugar.getMinRepeatCount() );
            this.m_objRepeat.setMaxRepeatCount( a_objSugar.getMaxRepeatCount() );
            try 
            {
                // repeat linkage
                GlycoEdge t_objOringal = a_objSugar.getRepeatLinkage();
                this.m_objInternal = t_objOringal.copy();
                // traverse Sugar and fill Residue Hashmap
                this.m_objTraverser = this.getTraverser(this);
                this.m_objTraverser.traverseGraph(a_objSugar);
                // fill residues in repeat linkage
                this.m_objRepeat.setRepeatLinkage(this.m_objInternal,
                        this.m_hashResidues.get(t_objOringal.getParent()),
                        this.m_hashResidues.get(t_objOringal.getChild()));
                // speczial trees
                for (Iterator<UnderdeterminedSubTree> t_iterSpezialTrees = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterSpezialTrees.hasNext();) 
                {
                    GlycoVisitorToGlycoCTextendMSDB t_objVisitor = new GlycoVisitorToGlycoCTextendMSDB( this.m_objConverter,this.m_strSchema );
                    t_objVisitor.setUseStrict(this.m_bStrict);
                    t_objVisitor.setMonosaccharideConversion(this.m_objConverter);
                    UnderdeterminedSubTree t_objSubtreeOriginal = t_iterSpezialTrees.next();
                    UnderdeterminedSubTree t_objSubtree = t_objVisitor.start(t_objSubtreeOriginal);
                    this.m_objRepeat.addUndeterminedSubTree(t_objSubtree);
                    // copy properties
                    t_objSubtree.setConnection( t_objSubtreeOriginal.getConnection().copy() );
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
                    GlycoVisitorToGlycoCTextendMSDB t_objVisitor = new GlycoVisitorToGlycoCTextendMSDB( this.m_objConverter,this.m_strSchema );
                    t_objVisitor.setUseStrict(this.m_bStrict);
                    t_objVisitor.setMonosaccharideConversion(this.m_objConverter);
                    UnderdeterminedSubTree t_objSubtreeOriginal = t_iterSpezialTrees.next();
                    UnderdeterminedSubTree t_objSubtree = t_objVisitor.start(t_objSubtreeOriginal);
                    this.m_objNewSugar.addUndeterminedSubTree(t_objSubtree);
                    // copy properties
                    if ( t_objSubtreeOriginal.getConnection() != null )
                    {
                        t_objSubtree.setConnection( t_objSubtreeOriginal.getConnection().copy() );
                    }
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
            // linkage type normalisation
            GlycoVisitorLinkageTypeNormalisation t_objVisitor = new GlycoVisitorLinkageTypeNormalisation();
            t_objVisitor.start(this.m_objNewSugar);
        }
        if ( this.m_bFusion )
        {
            this.m_visFuse.start(this.m_objNewSugar);
        }
        if ( this.m_bSubstPosition )
        {
            this.m_visSubstPostion.start(this.m_objNewSugar);
        }
    }

}
