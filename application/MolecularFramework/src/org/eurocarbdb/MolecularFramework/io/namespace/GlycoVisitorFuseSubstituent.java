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
package org.eurocarbdb.MolecularFramework.io.namespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
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
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.Superclass;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserTreeSingle;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

/**
* Solves szenario
* 
* 1. N-(1-x)-MS-(x-1)-N
*     ==> MS-(x-1)-N
*    - only for AMINO (Glycosciences.de Problem)
*
* 2. N-(1-x)-MS-(x-1)-Ac
*     ==> MS-(x-1)-NAc
* 
* 3. MS-N-Ac
*     ==> MS-NAc 
* 
* 4. N-(1-x)-MS-(x-1)-N...
*  ==> MS-N...
*  
* 5. GroN-(2-6)-GlcA (BCSDB only)
* Gro-(2-1)-N-(1-6)... 
* @author Logan
*
*/
public class GlycoVisitorFuseSubstituent implements GlycoVisitor 
{
    private ArrayList<Substituent> m_aSubstituent = new ArrayList<Substituent>();
    private ArrayList<Monosaccharide> m_aMonosaccharides = new ArrayList<Monosaccharide>();
    private SugarUnitRepeat m_objRepeat = null;
    private boolean m_bSzenarioOne = true;
    private boolean m_bSzenarioTwo = true;
    private boolean m_bSzenarioThree = true;
    private boolean m_bSzenarioFour = true;
    private boolean m_bSzenarioFive = false;

    public void setSzenarioOne(boolean a_b)
    {
        this.m_bSzenarioOne = a_b;
    }

    public void setSzenarioTwo(boolean a_b)
    {
        this.m_bSzenarioTwo = a_b;
    }

    public void setSzenarioThree(boolean a_b)
    {
        this.m_bSzenarioThree = a_b;
    }

    public void setSzenarioFour(boolean a_b)
    {
        this.m_bSzenarioFour = a_b;
    }

    public void setSzenarioFive(boolean a_b)
    {
        this.m_bSzenarioFive = a_b;
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharide) throws GlycoVisitorException 
    {
        this.m_aMonosaccharides.add(a_objMonosaccharide);
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide)
     */
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException 
    {
        // do nothing        
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat)
     */
    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException
    {
        GlycoVisitorFuseSubstituent t_objVisitor = new GlycoVisitorFuseSubstituent();
        t_objVisitor.setSzenarioOne(this.m_bSzenarioOne);
        t_objVisitor.setSzenarioThree(this.m_bSzenarioThree);
        t_objVisitor.setSzenarioTwo(this.m_bSzenarioTwo);
        t_objVisitor.setSzenarioFour(this.m_bSzenarioFour);
        t_objVisitor.setSzenarioFive(this.m_bSzenarioFive);
        t_objVisitor.start(a_objRepeat);
    }

    public void start(SugarUnitRepeat a_objSugar) throws GlycoVisitorException
    {
        this.clear();
        this.m_objRepeat= a_objSugar;
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        try 
        {
            this.fuse(a_objSugar);
        } 
        catch (GlycoconjugateException e) 
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
        for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();) 
        {
            GlycoVisitorFuseSubstituent t_objNN = new GlycoVisitorFuseSubstituent();
            t_objNN.setSzenarioOne(this.m_bSzenarioOne);
            t_objNN.setSzenarioThree(this.m_bSzenarioThree);
            t_objNN.setSzenarioTwo(this.m_bSzenarioTwo);
            t_objNN.setSzenarioFour(this.m_bSzenarioFour);
            t_objNN.setSzenarioFive(this.m_bSzenarioFive);
            t_objNN.start(t_iterUnder.next());
        }
    }

    public void start(UnderdeterminedSubTree a_objSugar) throws GlycoVisitorException
    {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        try 
        {
            this.fuse(a_objSugar);
        } 
        catch (GlycoconjugateException e) 
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Substituent)
     */
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException 
    {
        this.m_aSubstituent.add(a_objSubstituent);
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException 
    {
        // do nothing        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
        // TODO        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode)
     */
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException 
    {
        // do nothing
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.GlycoEdge)
     */
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException 
    {
        // do nothing        
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#start(org.eurocarbdb.MolecularFramework.sugar.Sugar)
     */
    public void start(Sugar a_objSugar) throws GlycoVisitorException 
    {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        try 
        {
            this.fuse(a_objSugar);
        } 
        catch (GlycoconjugateException e) 
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
        for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();) 
        {
            GlycoVisitorFuseSubstituent t_objNN = new GlycoVisitorFuseSubstituent();
            t_objNN.setSzenarioOne(this.m_bSzenarioOne);
            t_objNN.setSzenarioThree(this.m_bSzenarioThree);
            t_objNN.setSzenarioTwo(this.m_bSzenarioTwo);
            t_objNN.setSzenarioFour(this.m_bSzenarioFour);
            t_objNN.setSzenarioFive(this.m_bSzenarioFive);
            t_objNN.start(t_iterUnder.next());
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#getTraverser(org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor)
     */
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {
        return new GlycoTraverserTreeSingle(a_objVisitor);
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#clear()
     */
    public void clear() 
    {
        this.m_objRepeat = null;
        this.m_aMonosaccharides.clear();
        this.m_aSubstituent.clear();
    }

    private void fuse(GlycoGraph a_objGraph) throws GlycoconjugateException, GlycoVisitorException 
    {
        if ( this.m_bSzenarioOne )
        {
            this.solveSzenarioOne(a_objGraph);
        }
        if ( this.m_bSzenarioTwo )
        {
            this.solveSzenarioTwo(a_objGraph);
        }
        if ( this.m_bSzenarioThree )
        {
            this.solveSzenarioThree(a_objGraph);
        }
        if ( this.m_bSzenarioFour )
        {
            this.solveSzenarioFour(a_objGraph);
        }
        if ( this.m_bSzenarioFive )
        {
            this.solveSzenarioFive(a_objGraph);
        }
    }

    private void solveSzenarioOne(GlycoGraph a_objSugar) throws GlycoVisitorException, GlycoconjugateException
    {
        GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType(); 
        ArrayList<Substituent> t_aDeletedSubst = new ArrayList<Substituent>();
        for (Iterator<Monosaccharide> t_iterMs = this.m_aMonosaccharides.iterator(); t_iterMs.hasNext();)
        {
            Monosaccharide t_objMS = t_iterMs.next();
            ArrayList<GlycoEdge> t_aLinkages = t_objMS.getChildEdges();
            if ( t_aLinkages.size() > 1 )
            {
                // clone all child edges
                ArrayList<GlycoEdge> t_aEdgeClone = new ArrayList<GlycoEdge>();
                for (GlycoEdge t_objLinkages : t_aLinkages)
                {
                    t_aEdgeClone.add(t_objLinkages);
                }
                // search for a amino
                for (Iterator<GlycoEdge> t_iterAmino = t_aEdgeClone.iterator(); t_iterAmino.hasNext();)
                {
                    GlycoEdge t_objEdgeAmino = t_iterAmino.next();
                    GlycoNode t_objResidue = t_objEdgeAmino.getChild();
                    if (  t_objNodeType.isSubstituent(t_objResidue) )
                    {
                        Substituent t_objAmino = (Substituent)t_objResidue;
                        if ( t_objAmino.getSubstituentType() == SubstituentType.AMINO && t_objEdgeAmino.getGlycosidicLinkages().size() == 1 )
                        {
                            if ( !t_aDeletedSubst.contains(t_objAmino) )
                            {
                                // found amino
                                Linkage t_objLinkageAmino = t_objEdgeAmino.getGlycosidicLinkages().get(0);
                                // found amino, now we look for a second amino
                                for (Iterator<GlycoEdge> t_iterAmino2 = t_aEdgeClone.iterator(); t_iterAmino2.hasNext();)
                                {
                                    GlycoEdge t_objEdgeAmino2 = t_iterAmino2.next();
                                    t_objResidue = t_objEdgeAmino2.getChild();
                                    if (  t_objNodeType.isSubstituent(t_objResidue) )
                                    {
                                        Substituent t_objAminoTwo = (Substituent)t_objResidue;
                                        if ( !t_aDeletedSubst.contains(t_objAminoTwo) )
                                        {
                                            if ( t_objEdgeAmino2.getGlycosidicLinkages().size() == 1 )
                                            {
                                                Linkage t_objLinkageSub = t_objEdgeAmino2.getGlycosidicLinkages().get(0);
                                                if ( t_objAminoTwo.getSubstituentType() == SubstituentType.AMINO && t_objAmino != t_objAminoTwo )
                                                {
                                                    // found second amino
                                                    if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageAmino.getParentLinkages() ) )
                                                    {
                                                        if ( t_objAmino.getChildEdges().size() > 0 && t_objAminoTwo.getChildEdges().size() > 0 )
                                                        {
                                                            throw new GlycoVisitorException("Error in Amino-Child linkage.");
                                                        }
                                                        if ( t_objAmino.getChildEdges().size() > 0 )
                                                        {
                                                            t_objLinkageAmino.setParentLinkageType(LinkageType.DEOXY);
                                                            a_objSugar.removeNode(t_objAminoTwo);
                                                            this.m_aSubstituent.remove(t_objAminoTwo);
                                                            t_aDeletedSubst.add(t_objAminoTwo);
                                                        }
                                                        else
                                                        {
                                                            t_objLinkageSub.setParentLinkageType(LinkageType.DEOXY);
                                                            a_objSugar.removeNode(t_objAmino);
                                                            this.m_aSubstituent.remove(t_objAmino);
                                                            t_aDeletedSubst.add(t_objAmino);
                                                        }
                                                    }
                                                }
                                            }    
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void solveSzenarioTwo(GlycoGraph a_objSugar) throws GlycoVisitorException, GlycoconjugateException
    {
        GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType(); 
        for (Iterator<Monosaccharide> t_iterMs = this.m_aMonosaccharides.iterator(); t_iterMs.hasNext();)
        {
            Monosaccharide t_objMS = t_iterMs.next();
            ArrayList<GlycoEdge> t_aEdges = t_objMS.getChildEdges();
            if ( t_aEdges.size() > 1 )
            {
                ArrayList<GlycoEdge> t_aEdgeClone = new ArrayList<GlycoEdge>();
                for (GlycoEdge t_objLinkages : t_aEdges)
                {
                    t_aEdgeClone.add(t_objLinkages);
                }
                // search for a amino
                for (Iterator<GlycoEdge> t_iterSubstOne = t_aEdgeClone.iterator(); t_iterSubstOne.hasNext();)
                {
                    GlycoEdge t_objEdgeSubstOne = t_iterSubstOne.next();
                    GlycoNode t_objResidue = t_objEdgeSubstOne.getChild();
                    if (  t_objNodeType.isSubstituent(t_objResidue) )
                    {
                        Substituent t_objSubstOne = (Substituent)t_objResidue;
                        if ( t_objSubstOne.getSubstituentType() == SubstituentType.AMINO && t_objSubstOne.getChildEdges().size() == 0 )
                        {
                            if ( t_objEdgeSubstOne.getGlycosidicLinkages().size() == 1 )
                            {
                                Linkage t_objLinkageSubstOne = t_objEdgeSubstOne.getGlycosidicLinkages().get(0);
                                // found amino, now we look for the other substituents
                                for (Iterator<GlycoEdge> t_iterSubstTwo = t_aEdgeClone.iterator(); t_iterSubstTwo.hasNext();)
                                {
                                    GlycoEdge t_objEdgeTwo = t_iterSubstTwo.next();
                                    t_objResidue = t_objEdgeTwo.getChild();
                                    if (  t_objNodeType.isSubstituent(t_objResidue) )
                                    {
                                        Substituent t_objSubstTwo = (Substituent)t_objResidue;
                                        if ( t_objEdgeTwo.getGlycosidicLinkages().size() == 1 )
                                        {
                                            Linkage t_objLinkageSub = t_objEdgeTwo.getGlycosidicLinkages().get(0);
                                            if ( t_objSubstTwo.getSubstituentType() == SubstituentType.ACETYL )
                                            {
                                                // found ac
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.DEOXY);
                                                    t_objSubstTwo.setSubstituentType(SubstituentType.N_ACETYL);
                                                    a_objSugar.removeNode(t_objSubstOne);        
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                            else if ( t_objSubstTwo.getSubstituentType() == SubstituentType.GLYCOLYL )
                                            {
                                                // found gc
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.DEOXY);
                                                    t_objSubstTwo.setSubstituentType(SubstituentType.N_GLYCOLYL);
                                                    a_objSugar.removeNode(t_objSubstOne);     
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                            else if ( t_objSubstTwo.getSubstituentType() == SubstituentType.FORMYL )
                                            {
                                                // found formyl
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.DEOXY);
                                                    t_objSubstTwo.setSubstituentType(SubstituentType.N_FORMYL);
                                                    a_objSugar.removeNode(t_objSubstOne);    
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                            else if ( t_objSubstTwo.getSubstituentType() == SubstituentType.AMIDINO )
                                            {
                                                // found amidino
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.DEOXY);
                                                    t_objSubstTwo.setSubstituentType(SubstituentType.N_AMIDINO);
                                                    a_objSugar.removeNode(t_objSubstOne);   
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                            else if ( t_objSubstTwo.getSubstituentType() == SubstituentType.METHYL )
                                            {
                                                // found methyl
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.DEOXY);
                                                    t_objSubstTwo.setSubstituentType(SubstituentType.N_METHYL);
                                                    a_objSugar.removeNode(t_objSubstOne);      
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                            else if ( t_objSubstTwo.getSubstituentType() == SubstituentType.SULFATE )
                                            {
                                                // found sulfate
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.DEOXY);
                                                    t_objSubstTwo.setSubstituentType(SubstituentType.N_SULFATE);
                                                    a_objSugar.removeNode(t_objSubstOne);   
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                        }
                                    }
                                }  
                            }
                        }
                        else if ( t_objSubstOne.getSubstituentType() == SubstituentType.PHOSPHATE && t_objSubstOne.getChildEdges().size() == 0 )
                        {
                            if ( t_objEdgeSubstOne.getGlycosidicLinkages().size() == 1 )
                            {
                                Linkage t_objLinkageSubstOne = t_objEdgeSubstOne.getGlycosidicLinkages().get(0);
                                // found phosphate, now we look for a the other substituents
                                for (Iterator<GlycoEdge> t_iterSubstTwo = t_aEdgeClone.iterator(); t_iterSubstTwo.hasNext();)
                                {
                                    GlycoEdge t_objEdgeTwo = t_iterSubstTwo.next();
                                    t_objResidue = t_objEdgeTwo.getChild();
                                    if (  t_objNodeType.isSubstituent(t_objResidue) )
                                    {
                                        Substituent t_objSubstTwo = (Substituent)t_objResidue;
                                        if ( t_objEdgeTwo.getGlycosidicLinkages().size() == 1 )
                                        {
                                            Linkage t_objLinkageSub = t_objEdgeTwo.getGlycosidicLinkages().get(0);
                                            if ( t_objSubstTwo.getSubstituentType() == SubstituentType.PHOSPHATE && t_objSubstOne != t_objSubstTwo )
                                            {
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.H_AT_OH);
                                                    t_objSubstTwo.setSubstituentType(SubstituentType.PYROPHOSPHATE);
                                                    a_objSugar.removeNode(t_objSubstOne);    
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                            else if ( t_objSubstTwo.getSubstituentType() == SubstituentType.PYROPHOSPHATE )
                                            {
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.H_AT_OH);
                                                    t_objSubstTwo.setSubstituentType(SubstituentType.TRIPHOSPHATE);
                                                    a_objSugar.removeNode(t_objSubstOne);
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                            else if ( t_objSubstTwo.getSubstituentType() == SubstituentType.ETHANOLAMINE )
                                            {
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.H_AT_OH);
                                                    t_objSubstTwo.setSubstituentType(SubstituentType.PHOSPHO_ETHANOLAMINE);
                                                    a_objSugar.removeNode(t_objSubstOne);     
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void solveSzenarioThree(GlycoGraph a_objGraph) throws GlycoVisitorException, GlycoconjugateException
    {
        for (Iterator<Substituent> t_iterSubst = this.m_aSubstituent.iterator(); t_iterSubst.hasNext();) 
        {
            boolean t_bFuse = false;
            Substituent t_objSubst = t_iterSubst.next();                
            if ( t_objSubst.getSubstituentType() == SubstituentType.ETHANOLAMINE )
            {
                if ( t_objSubst.getParentEdge() != null )
                {
                    GlycoNode t_objParent = t_objSubst.getParentEdge().getParent(); 
                    GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
                    Substituent t_objSubSubst = t_visNodeType.getSubstituent(t_objParent);
                    if ( t_objSubSubst != null )
                    {                    
                        if ( t_objSubSubst.getSubstituentType() == SubstituentType.PHOSPHATE ) 
                        {
                            t_objSubSubst.setSubstituentType(SubstituentType.PHOSPHO_ETHANOLAMINE);
                            this.moveChilds(a_objGraph,t_objParent,t_objSubst);
                            t_bFuse = true;
                        }
                        else if ( t_objSubSubst.getSubstituentType() == SubstituentType.PYROPHOSPHATE ) 
                        {
                            t_objSubSubst.setSubstituentType(SubstituentType.DIPHOSPHO_ETHANOLAMINE);
                            this.moveChilds(a_objGraph,t_objParent,t_objSubst);
                            t_bFuse = true;
                        }
                        if ( this.m_objRepeat != null && t_bFuse )
                        {
                            if ( this.m_objRepeat.getRepeatLinkage().getParent() == t_objSubst )
                            {
                                this.m_objRepeat.setRepeatLinkage(this.m_objRepeat.getRepeatLinkage(),t_objParent,this.m_objRepeat.getRepeatLinkage().getChild());
                            }
                        }
                    }
                }
            }
            else if ( t_objSubst.getSubstituentType() == SubstituentType.PHOSPHATE )
            {
                if ( t_objSubst.getParentEdge() != null )
                {
                    GlycoNode t_objParent = t_objSubst.getParentEdge().getParent(); 
                    GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
                    Substituent t_objSubSubst = t_visNodeType.getSubstituent(t_objParent);
                    if ( t_objSubSubst != null )
                    {                    
                        if ( t_objSubSubst.getSubstituentType() == SubstituentType.PHOSPHATE ) 
                        {
                            t_objSubSubst.setSubstituentType(SubstituentType.PYROPHOSPHATE);
                            this.moveChilds(a_objGraph,t_objParent,t_objSubst);
                            t_bFuse = true;
                        }
                        else if ( t_objSubSubst.getSubstituentType() == SubstituentType.PYROPHOSPHATE ) 
                        {
                            t_objSubSubst.setSubstituentType(SubstituentType.TRIPHOSPHATE);
                            this.moveChilds(a_objGraph,t_objParent,t_objSubst);
                            t_bFuse = true;
                        }
                        if ( this.m_objRepeat != null && t_bFuse )
                        {
                            if ( this.m_objRepeat.getRepeatLinkage().getParent() == t_objSubst )
                            {
                                this.m_objRepeat.setRepeatLinkage(this.m_objRepeat.getRepeatLinkage(),t_objParent,this.m_objRepeat.getRepeatLinkage().getChild());
                            }
                        }
                    }
                }
            }
            else if ( t_objSubst.getSubstituentType() == SubstituentType.PYROPHOSPHATE )
            {
                if ( t_objSubst.getParentEdge() != null )
                {
                    GlycoNode t_objParent = t_objSubst.getParentEdge().getParent(); 
                    GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
                    Substituent t_objSubSubst = t_visNodeType.getSubstituent(t_objParent);
                    if ( t_objSubSubst != null )
                    {                    
                        if ( t_objSubSubst.getSubstituentType() == SubstituentType.PHOSPHATE ) 
                        {
                            t_objSubSubst.setSubstituentType(SubstituentType.TRIPHOSPHATE);
                            this.moveChilds(a_objGraph,t_objParent,t_objSubst);
                            t_bFuse = true;
                        }
                        if ( this.m_objRepeat != null && t_bFuse )
                        {
                            if ( this.m_objRepeat.getRepeatLinkage().getParent() == t_objSubst )
                            {
                                this.m_objRepeat.setRepeatLinkage(this.m_objRepeat.getRepeatLinkage(),t_objParent,this.m_objRepeat.getRepeatLinkage().getChild());
                            }
                        }
                    }
                }
            }
            else if ( t_objSubst.getSubstituentType() == SubstituentType.PHOSPHO_ETHANOLAMINE )
            {
                if ( t_objSubst.getParentEdge() != null )
                {
                    GlycoNode t_objParent = t_objSubst.getParentEdge().getParent(); 
                    GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
                    Substituent t_objSubSubst = t_visNodeType.getSubstituent(t_objParent);
                    if ( t_objSubSubst != null )
                    {                    
                        if ( t_objSubSubst.getSubstituentType() == SubstituentType.PHOSPHATE ) 
                        {
                            t_objSubSubst.setSubstituentType(SubstituentType.DIPHOSPHO_ETHANOLAMINE );
                            this.moveChilds(a_objGraph,t_objParent,t_objSubst);
                            t_bFuse = true;
                        }
                        if ( this.m_objRepeat != null && t_bFuse )
                        {
                            if ( this.m_objRepeat.getRepeatLinkage().getParent() == t_objSubst )
                            {
                                this.m_objRepeat.setRepeatLinkage(this.m_objRepeat.getRepeatLinkage(),t_objParent,this.m_objRepeat.getRepeatLinkage().getChild());
                            }
                        }
                    }
                }
            }
            else if ( t_objSubst.getSubstituentType() == SubstituentType.SULFATE )
            {
                if ( t_objSubst.getParentEdge() != null )
                {
                    GlycoNode t_objParent = t_objSubst.getParentEdge().getParent(); 
                    GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
                    Substituent t_objSubSubst = t_visNodeType.getSubstituent(t_objParent);
                    if ( t_objSubSubst != null )
                    {                    
                        if ( t_objSubSubst.getSubstituentType() == SubstituentType.AMINO ) 
                        {
                            t_objSubSubst.setSubstituentType(SubstituentType.N_SULFATE );
                            this.moveChilds(a_objGraph,t_objParent,t_objSubst);
                            t_bFuse = true;
                        }
                        if ( this.m_objRepeat != null && t_bFuse )
                        {
                            if ( this.m_objRepeat.getRepeatLinkage().getParent() == t_objSubst )
                            {
                                this.m_objRepeat.setRepeatLinkage(this.m_objRepeat.getRepeatLinkage(),t_objParent,this.m_objRepeat.getRepeatLinkage().getChild());
                            }
                        }
                    }
                }
            }
            else if ( t_objSubst.getSubstituentType() == SubstituentType.METHYL )
            {
                if ( t_objSubst.getParentEdge() != null )
                {
                    GlycoNode t_objParent = t_objSubst.getParentEdge().getParent(); 
                    GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
                    Substituent t_objSubSubst = t_visNodeType.getSubstituent(t_objParent);
                    if ( t_objSubSubst != null )
                    {                    
                        if ( t_objSubSubst.getSubstituentType() == SubstituentType.AMINO ) 
                        {
                            t_objSubSubst.setSubstituentType(SubstituentType.N_METHYL );
                            this.moveChilds(a_objGraph,t_objParent,t_objSubst);
                            t_bFuse = true;
                        }
                        if ( this.m_objRepeat != null && t_bFuse )
                        {
                            if ( this.m_objRepeat.getRepeatLinkage().getParent() == t_objSubst )
                            {
                                this.m_objRepeat.setRepeatLinkage(this.m_objRepeat.getRepeatLinkage(),t_objParent,this.m_objRepeat.getRepeatLinkage().getChild());
                            }
                        }
                    }
                }
            }
            else if ( t_objSubst.getSubstituentType() == SubstituentType.GLYCOLYL )
            {
                if ( t_objSubst.getParentEdge() != null )
                {
                    GlycoNode t_objParent = t_objSubst.getParentEdge().getParent(); 
                    GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
                    Substituent t_objSubSubst = t_visNodeType.getSubstituent(t_objParent);
                    if ( t_objSubSubst != null )
                    {                    
                        if ( t_objSubSubst.getSubstituentType() == SubstituentType.AMINO ) 
                        {
                            t_objSubSubst.setSubstituentType(SubstituentType.N_GLYCOLYL );
                            this.moveChilds(a_objGraph,t_objParent,t_objSubst);
                            t_bFuse = true;
                        }
                        if ( this.m_objRepeat != null && t_bFuse )
                        {
                            if ( this.m_objRepeat.getRepeatLinkage().getParent() == t_objSubst )
                            {
                                this.m_objRepeat.setRepeatLinkage(this.m_objRepeat.getRepeatLinkage(),t_objParent,this.m_objRepeat.getRepeatLinkage().getChild());
                            }
                        }
                    }
                }
            }
            else if ( t_objSubst.getSubstituentType() == SubstituentType.FORMYL )
            {
                if ( t_objSubst.getParentEdge() != null )
                {
                    GlycoNode t_objParent = t_objSubst.getParentEdge().getParent(); 
                    GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
                    Substituent t_objSubSubst = t_visNodeType.getSubstituent(t_objParent);
                    if ( t_objSubSubst != null )
                    {                    
                        if ( t_objSubSubst.getSubstituentType() == SubstituentType.AMINO ) 
                        {
                            t_objSubSubst.setSubstituentType(SubstituentType.N_FORMYL );
                            this.moveChilds(a_objGraph,t_objParent,t_objSubst);
                            t_bFuse = true;
                        }
                        if ( this.m_objRepeat != null && t_bFuse )
                        {
                            if ( this.m_objRepeat.getRepeatLinkage().getParent() == t_objSubst )
                            {
                                this.m_objRepeat.setRepeatLinkage(this.m_objRepeat.getRepeatLinkage(),t_objParent,this.m_objRepeat.getRepeatLinkage().getChild());
                            }
                        }
                    }
                }
            }
            else if ( t_objSubst.getSubstituentType() == SubstituentType.AMIDINO )
            {
                if ( t_objSubst.getParentEdge() != null )
                {
                    GlycoNode t_objParent = t_objSubst.getParentEdge().getParent(); 
                    GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
                    Substituent t_objSubSubst = t_visNodeType.getSubstituent(t_objParent);
                    if ( t_objSubSubst != null )
                    {                    
                        if ( t_objSubSubst.getSubstituentType() == SubstituentType.AMINO ) 
                        {
                            t_objSubSubst.setSubstituentType(SubstituentType.N_AMIDINO );
                            this.moveChilds(a_objGraph,t_objParent,t_objSubst);
                            t_bFuse = true;
                        }
                        if ( this.m_objRepeat != null && t_bFuse )
                        {
                            if ( this.m_objRepeat.getRepeatLinkage().getParent() == t_objSubst )
                            {
                                this.m_objRepeat.setRepeatLinkage(this.m_objRepeat.getRepeatLinkage(),t_objParent,this.m_objRepeat.getRepeatLinkage().getChild());
                            }
                        }
                    }
                }
            }
            else if ( t_objSubst.getSubstituentType() == SubstituentType.ACETYL )
            {
                if ( t_objSubst.getParentEdge() != null )
                {
                    GlycoNode t_objParent = t_objSubst.getParentEdge().getParent(); 
                    GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
                    Substituent t_objSubSubst = t_visNodeType.getSubstituent(t_objParent);
                    if ( t_objSubSubst != null )
                    {                    
                        if ( t_objSubSubst.getSubstituentType() == SubstituentType.AMINO ) 
                        {
                            t_objSubSubst.setSubstituentType(SubstituentType.N_ACETYL );
                            this.moveChilds(a_objGraph,t_objParent,t_objSubst);
                            t_bFuse = true;
                        }
                        if ( this.m_objRepeat != null && t_bFuse )
                        {
                            if ( this.m_objRepeat.getRepeatLinkage().getParent() == t_objSubst )
                            {
                                this.m_objRepeat.setRepeatLinkage(this.m_objRepeat.getRepeatLinkage(),t_objParent,this.m_objRepeat.getRepeatLinkage().getChild());
                            }
                        }
                    }
                }
            }
            else if ( t_objSubst.getSubstituentType() == SubstituentType.SUCCINATE )
            {
                if ( t_objSubst.getParentEdge() != null )
                {
                    GlycoNode t_objParent = t_objSubst.getParentEdge().getParent(); 
                    GlycoVisitorNodeType t_visNodeType = new GlycoVisitorNodeType();
                    Substituent t_objSubSubst = t_visNodeType.getSubstituent(t_objParent);
                    if ( t_objSubSubst != null )
                    {                    
                        if ( t_objSubSubst.getSubstituentType() == SubstituentType.AMINO ) 
                        {
                            t_objSubSubst.setSubstituentType(SubstituentType.N_SUCCINATE );
                            this.moveChilds(a_objGraph,t_objParent,t_objSubst);
                            t_bFuse = true;
                        }
                        if ( this.m_objRepeat != null && t_bFuse )
                        {
                            if ( this.m_objRepeat.getRepeatLinkage().getParent() == t_objSubst )
                            {
                                this.m_objRepeat.setRepeatLinkage(this.m_objRepeat.getRepeatLinkage(),t_objParent,this.m_objRepeat.getRepeatLinkage().getChild());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param graph
     * @param subst
     * @param subNode
     * @throws GlycoconjugateException 
     */
    private void moveChilds(GlycoGraph a_objGraph, GlycoNode a_objSubst, GlycoNode a_objRemoveSubst) throws GlycoconjugateException
    {
        ArrayList<GlycoEdge> t_aTemp = new ArrayList<GlycoEdge>();
        for (Iterator<GlycoEdge> t_iterChilds = a_objRemoveSubst.getChildEdges().iterator(); t_iterChilds.hasNext();)
        {
            t_aTemp.add(t_iterChilds.next());
        }
        for (Iterator<GlycoEdge> t_iterEdge = t_aTemp.iterator(); t_iterEdge.hasNext();)
        {
            GlycoEdge t_objEdge = t_iterEdge.next();
            GlycoNode t_objNode = t_objEdge.getChild();
            a_objGraph.removeEdge(t_objEdge);
            a_objGraph.addEdge(a_objSubst, t_objNode, t_objEdge);
        }
        a_objGraph.removeNode(a_objRemoveSubst);
    }

    private void solveSzenarioFour(GlycoGraph a_objSugar) throws GlycoVisitorException, GlycoconjugateException
    {
        GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType(); 
        for (Iterator<Monosaccharide> t_iterMs = this.m_aMonosaccharides.iterator(); t_iterMs.hasNext();)
        {
            Monosaccharide t_objMS = t_iterMs.next();
            ArrayList<GlycoEdge> t_aEdges = t_objMS.getChildEdges();
            if ( t_aEdges.size() > 1 )
            {
                ArrayList<GlycoEdge> t_aEdgeClone = new ArrayList<GlycoEdge>();
                for (GlycoEdge t_objLinkages : t_aEdges)
                {
                    t_aEdgeClone.add(t_objLinkages);
                }
                // search for a amino
                for (Iterator<GlycoEdge> t_iterSubstOne = t_aEdgeClone.iterator(); t_iterSubstOne.hasNext();)
                {
                    GlycoEdge t_objEdgeSubstOne = t_iterSubstOne.next();
                    GlycoNode t_objResidue = t_objEdgeSubstOne.getChild();
                    if (  t_objNodeType.isSubstituent(t_objResidue) )
                    {
                        Substituent t_objSubstOne = (Substituent)t_objResidue;
                        if ( t_objSubstOne.getSubstituentType() == SubstituentType.AMINO && t_objSubstOne.getChildEdges().size() == 0 )
                        {
                            if ( t_objEdgeSubstOne.getGlycosidicLinkages().size() == 1 )
                            {
                                Linkage t_objLinkageSubstOne = t_objEdgeSubstOne.getGlycosidicLinkages().get(0);
                                // found amino, now we look for the other substituents
                                for (Iterator<GlycoEdge> t_iterSubstTwo = t_aEdgeClone.iterator(); t_iterSubstTwo.hasNext();)
                                {
                                    GlycoEdge t_objEdgeTwo = t_iterSubstTwo.next();
                                    t_objResidue = t_objEdgeTwo.getChild();
                                    if (  t_objNodeType.isSubstituent(t_objResidue) )
                                    {
                                        Substituent t_objSubstTwo = (Substituent)t_objResidue;
                                        if ( t_objEdgeTwo.getGlycosidicLinkages().size() == 1 )
                                        {
                                            Linkage t_objLinkageSub = t_objEdgeTwo.getGlycosidicLinkages().get(0);
                                            if ( t_objSubstTwo.getSubstituentType() == SubstituentType.N_ACETYL )
                                            {
                                                // found ac
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.DEOXY);
                                                    a_objSugar.removeNode(t_objSubstOne);        
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                            else if ( t_objSubstTwo.getSubstituentType() == SubstituentType.N_GLYCOLYL )
                                            {
                                                // found gc
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.DEOXY);
                                                    a_objSugar.removeNode(t_objSubstOne);     
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                            else if ( t_objSubstTwo.getSubstituentType() == SubstituentType.N_FORMYL )
                                            {
                                                // found formyl
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.DEOXY);
                                                    a_objSugar.removeNode(t_objSubstOne);    
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                            else if ( t_objSubstTwo.getSubstituentType() == SubstituentType.N_AMIDINO )
                                            {
                                                // found amidino
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.DEOXY);
                                                    a_objSugar.removeNode(t_objSubstOne);   
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                            else if ( t_objSubstTwo.getSubstituentType() == SubstituentType.N_METHYL )
                                            {
                                                // found methyl
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.DEOXY);
                                                    a_objSugar.removeNode(t_objSubstOne);      
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                            else if ( t_objSubstTwo.getSubstituentType() == SubstituentType.N_SULFATE )
                                            {
                                                // found sulfate
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.DEOXY);
                                                    a_objSugar.removeNode(t_objSubstOne);   
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                        }
                                    }
                                }  
                            }
                        }
                        else if ( t_objSubstOne.getSubstituentType() == SubstituentType.PHOSPHATE && t_objSubstOne.getChildEdges().size() == 0 )
                        {
                            if ( t_objEdgeSubstOne.getGlycosidicLinkages().size() == 1 )
                            {
                                Linkage t_objLinkageSubstOne = t_objEdgeSubstOne.getGlycosidicLinkages().get(0);
                                // found phosphate, now we look for a the other substituents
                                for (Iterator<GlycoEdge> t_iterSubstTwo = t_aEdgeClone.iterator(); t_iterSubstTwo.hasNext();)
                                {
                                    GlycoEdge t_objEdgeTwo = t_iterSubstTwo.next();
                                    t_objResidue = t_objEdgeTwo.getChild();
                                    if (  t_objNodeType.isSubstituent(t_objResidue) )
                                    {
                                        Substituent t_objSubstTwo = (Substituent)t_objResidue;
                                        if ( t_objEdgeTwo.getGlycosidicLinkages().size() == 1 )
                                        {
                                            Linkage t_objLinkageSub = t_objEdgeTwo.getGlycosidicLinkages().get(0);
                                            if ( t_objSubstTwo.getSubstituentType() == SubstituentType.PHOSPHO_ETHANOLAMINE )
                                            {
                                                if ( this.compareArrays(t_objLinkageSub.getParentLinkages(), t_objLinkageSubstOne.getParentLinkages() ) )
                                                {
                                                    t_objLinkageSub.setParentLinkageType(LinkageType.H_AT_OH);
                                                    a_objSugar.removeNode(t_objSubstOne);     
                                                    this.m_aSubstituent.remove(t_objSubstOne);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void solveSzenarioFive(GlycoGraph a_objSugar) throws GlycoVisitorException, GlycoconjugateException
    {
        GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType(); 
        for (Iterator<Monosaccharide> t_iterMs = this.m_aMonosaccharides.iterator(); t_iterMs.hasNext();)
        {
            Monosaccharide t_objMSGro = t_iterMs.next();
            if ( t_objMSGro.getSuperclass() == Superclass.TRI )
            {
                ArrayList<GlycoEdge> t_aLinkages = t_objMSGro.getChildEdges();
                // clone all child edges
                ArrayList<GlycoEdge> t_aEdgeClone = new ArrayList<GlycoEdge>();
                for (GlycoEdge t_objLinkages : t_aLinkages)
                {
                    t_aEdgeClone.add(t_objLinkages);
                }
                // search for a amino
                for (Iterator<GlycoEdge> t_iterAmino = t_aEdgeClone.iterator(); t_iterAmino.hasNext();)
                {
                    GlycoEdge t_objEdgeAmino = t_iterAmino.next();
                    GlycoNode t_objResidue = t_objEdgeAmino.getChild();
                    if (  t_objNodeType.isSubstituent(t_objResidue) )
                    {
                        Substituent t_objAmino = (Substituent)t_objResidue;
                        if ( t_objAmino.getSubstituentType() == SubstituentType.AMINO && t_objEdgeAmino.getGlycosidicLinkages().size() == 1 )
                        {
                            GlycoEdge t_objEdgeParent = t_objMSGro.getParentEdge();
                            GlycoNode t_objMSParent = t_objEdgeParent.getParent();
                            if ( t_objEdgeParent != null )
                            {
                                if ( this.samePosition(t_objEdgeParent,t_objEdgeAmino) )
                                {
                                    a_objSugar.removeEdge(t_objEdgeAmino);
                                    a_objSugar.removeEdge(t_objEdgeParent);
                                    GlycoEdge t_objEdge = new GlycoEdge();
                                    Linkage t_objLink = new Linkage();
                                    t_objLink.addChildLinkage(1);
                                    t_objLink.setChildLinkageType(LinkageType.NONMONOSACCHARID);
                                    t_objLink.setParentLinkages( t_objEdgeParent.getGlycosidicLinkages().get(0).getParentLinkages());
                                    t_objLink.setParentLinkageType(LinkageType.DEOXY);
                                    t_objEdge.addGlycosidicLinkage(t_objLink);
                                    a_objSugar.addEdge(t_objMSParent, t_objResidue, t_objEdge);
                                    t_objEdge = new GlycoEdge();
                                    t_objLink = new Linkage();
                                    t_objLink.addParentLinkage(1);
                                    t_objLink.setParentLinkageType(LinkageType.NONMONOSACCHARID);
                                    t_objLink.setChildLinkageType(LinkageType.DEOXY);
                                    t_objLink.setChildLinkages(t_objEdgeParent.getGlycosidicLinkages().get(0).getChildLinkages());
                                    t_objEdge.addGlycosidicLinkage(t_objLink);
                                    a_objSugar.addEdge(t_objResidue, t_objMSGro, t_objEdge);
                                }
                            }                                
                        }
                    }
                }
            }
        }
    }

    private boolean samePosition(GlycoEdge a_objEdgeParent, GlycoEdge a_objEdgeAmino) 
    {
        if ( a_objEdgeAmino.getGlycosidicLinkages().size() != 1 || a_objEdgeParent.getGlycosidicLinkages().size() != 1 )
        {
            return false;
        }
        return this.compareArrays(a_objEdgeAmino.getGlycosidicLinkages().get(0).getParentLinkages(), 
                a_objEdgeParent.getGlycosidicLinkages().get(0).getChildLinkages());
    }

    private boolean compareArrays(ArrayList<Integer> a_aOne, ArrayList<Integer> a_aTwo)
    {
        if ( a_aOne.size() != a_aTwo.size() )
        {
            return false;
        }
        Collections.sort(a_aOne);
        Collections.sort(a_aTwo);
        for (int t_iCounter = 0; t_iCounter < a_aOne.size(); t_iCounter++)
        {
            if ( a_aOne.get(t_iCounter) != a_aTwo.get(t_iCounter) )
            {
                return false;
            }            
        }
        return true;
    }
}