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
package org.eurocarbdb.MolecularFramework.util.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
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
import org.eurocarbdb.MolecularFramework.util.validation.GlycoVisitorContainsNode;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
* This works only for fully conected sugars. Cut all nonmonosaccharide part before the first
* monosaccharide.
* 
* @author rene
*
*/
public class GlycoVisitorSugarGraph implements GlycoVisitor
{
    private HashMap<GlycoNode,GlycoNode> m_hashResidues = new HashMap<GlycoNode,GlycoNode>();
    private ArrayList<SugarGraphInformation> m_aSugarGraphs = new ArrayList<SugarGraphInformation>();
    private GlycoNode m_objStartResidue = null;
    private GlycoTraverser m_objTraverser = null;
    private GlycoGraph m_objSugarUnit;
    private SugarGraphInformation m_objCurrentSugarGraph = null;
    private GlycoNode m_objLastResidue = null;
    private GlycoEdge m_objLastEdge = null;
    private GlycoNode m_objTerminalAglyca = null;
    
    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharide) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            if ( this.m_objStartResidue != null )
            {
                try
                {
                    // add this MS to the new sugar
                    Monosaccharide t_objMs = a_objMonosaccharide.copy();
                    this.m_objLastResidue = a_objMonosaccharide;
                    this.m_objSugarUnit.addNode(t_objMs);
                    this.m_hashResidues.put(a_objMonosaccharide,t_objMs);
                    this.m_objCurrentSugarGraph.addTopLevelNode(a_objMonosaccharide,t_objMs);
                    // copy parent linkage
                    GlycoEdge t_objEdgeOriginal = a_objMonosaccharide.getParentEdge();
                    GlycoEdge t_objEdgeNew = t_objEdgeOriginal.copy();
                    GlycoNode t_objParent = this.m_hashResidues.get( t_objEdgeOriginal.getParent() );
                    if ( t_objParent == null )
                    {
                        throw new GlycoVisitorException("Unable to copy monosaccharide : " + a_objMonosaccharide.getGlycoCTName() + " parent was not translated.");
                    }
                    this.m_objSugarUnit.addEdge(t_objParent,t_objMs,t_objEdgeNew);
                } 
                catch (GlycoconjugateException e)
                {
                    throw new GlycoVisitorException(e.getMessage(),e);
                }
            }
            else
            {
                // begin a new sugar
                Sugar t_objSugar = new Sugar();
                this.m_hashResidues.clear();
                if ( a_objMonosaccharide.getParentEdge() != null )
                {
                    this.m_objCurrentSugarGraph = new SugarGraphInformation(t_objSugar,a_objMonosaccharide.getParentNode(),a_objMonosaccharide,a_objMonosaccharide.getParentEdge());
                }
                else
                {
                    this.m_objCurrentSugarGraph = new SugarGraphInformation(t_objSugar,null,a_objMonosaccharide,a_objMonosaccharide.getParentEdge());
                }
                this.m_aSugarGraphs.add(this.m_objCurrentSugarGraph);
                this.m_objSugarUnit = t_objSugar;
                this.m_objStartResidue = a_objMonosaccharide;
                // copie information of the ms
                try
                {
                    Monosaccharide t_objMs = a_objMonosaccharide.copy();
                    this.m_objLastResidue = a_objMonosaccharide;
                    this.m_objSugarUnit.addNode(t_objMs);
                    this.m_hashResidues.put(a_objMonosaccharide,t_objMs);
                    this.m_objCurrentSugarGraph.addTopLevelNode(a_objMonosaccharide,t_objMs);
                } 
                catch (GlycoconjugateException e)
                {
                    throw new GlycoVisitorException(e.getMessage(),e);
                }
            }
        }
        if ( this.m_objTraverser.getState() == GlycoTraverser.LEAVE )
        {
            if ( a_objMonosaccharide == this.m_objStartResidue )
            {
                this.m_objStartResidue = null;                
            }
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Substituent)
     */
    public void visit(Substituent a_objSubst) throws GlycoVisitorException
    {
        if ( this.m_objTerminalAglyca != null )
        {
            return;
        }
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            if ( this.m_objStartResidue != null )
            {
                try
                {
                    // add this MS to the new sugar
                    Substituent t_objSubst = a_objSubst.copy();
                    this.m_objLastResidue = a_objSubst;
                    this.m_objSugarUnit.addNode(t_objSubst);
                    this.m_hashResidues.put(a_objSubst,t_objSubst);
                    this.m_objCurrentSugarGraph.addTopLevelNode(a_objSubst,t_objSubst);
                    // copy parent linkage
                    GlycoEdge t_objEdgeOriginal = a_objSubst.getParentEdge();
                    GlycoEdge t_objEdgeNew = t_objEdgeOriginal.copy();
                    GlycoNode t_objParent = this.m_hashResidues.get( t_objEdgeOriginal.getParent() );
                    if ( t_objParent == null )
                    {
                        throw new GlycoVisitorException("Unable to copy monosaccharide : " + a_objSubst.getSubstituentType().getName() + " parent was not translated.");
                    }
                    this.m_objSugarUnit.addEdge(t_objParent,t_objSubst,t_objEdgeNew);
                } 
                catch (GlycoconjugateException e)
                {
                    throw new GlycoVisitorException(e.getMessage(),e);
                }
            }
        }               
    }


    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.SimpleGlycosidicLinkage)
     */
    public void visit(GlycoEdge arg0) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            this.m_objLastEdge = arg0;
        }
    }


    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException
    {
        if ( this.m_objTerminalAglyca != null )
        {
            return;
        }
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            if ( this.m_objStartResidue != null )
            {
                GlycoNode t_objTarget = this.m_hashResidues.get(a_objCyclic.getCyclicStart());
                if ( t_objTarget == null )
                {
                    throw new GlycoVisitorException("Start point of a cyclic unit is not part of the new sugar.");
                }
                // found target in sugar
                GlycoEdge t_objEdgeOriginal = a_objCyclic.getParentEdge();
                GlycoEdge t_objEdgeNew;
                try
                {
                    t_objEdgeNew = t_objEdgeOriginal.copy();
                    GlycoNode t_objParent = this.m_hashResidues.get( t_objEdgeOriginal.getParent() );
                    if ( t_objParent == null )
                    {
                        throw new GlycoVisitorException("Unable to copy cylic unit : parent was not translated.");
                    }
                    this.m_objSugarUnit.addEdge(t_objParent,t_objTarget,t_objEdgeNew);
                } 
                catch (GlycoconjugateException e)
                {
                    throw new GlycoVisitorException(e.getMessage(),e);
                }
            }
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException
    {
        if ( this.m_objTerminalAglyca != null )
        {
            return;
        }
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            if ( this.m_objStartResidue == null )
            {
                GlycoVisitorContainsNode t_objVisitor = new GlycoVisitorContainsNode();
                t_objVisitor.start(a_objAlternative);
                if ( t_objVisitor.getMonosaccharideCount() > 0 )
                {
                    if ( t_objVisitor.getNonMonosaccharideCount() > 0 )
                    {
                        throw new GlycoVisitorException("GlycoVisitorSugarGraph can not handle alternative sugar units that consist of monosaccharides and aglyca.");
                    }
                    // new Sugar
                    Sugar t_objSugar = new Sugar();
                    this.m_hashResidues.clear();
                    if ( a_objAlternative.getParentEdge() != null )
                    {
                        this.m_objCurrentSugarGraph = new SugarGraphInformation(t_objSugar,a_objAlternative.getParentNode(),a_objAlternative,a_objAlternative.getParentEdge());
                    }
                    else
                    {
                        this.m_objCurrentSugarGraph = new SugarGraphInformation(t_objSugar,null,a_objAlternative,a_objAlternative.getParentEdge());
                    }
                    this.m_aSugarGraphs.add(this.m_objCurrentSugarGraph);
                    this.m_objSugarUnit = t_objSugar;
                    this.m_objStartResidue = a_objAlternative;
                    // copie information of the ms
                    try
                    {
                        SugarUnitAlternative t_objMs = a_objAlternative.copy();
                        this.m_objLastResidue = a_objAlternative;
                        this.m_objSugarUnit.addNode(t_objMs);
                        this.m_hashResidues.put(a_objAlternative,t_objMs);
                        this.m_objCurrentSugarGraph.addTopLevelNode(a_objAlternative,t_objMs);
                    } 
                    catch (GlycoconjugateException e)
                    {
                        throw new GlycoVisitorException(e.getMessage(),e);
                    }                    
                }
            }
            else
            {
                GlycoVisitorContainsNode t_objVisitor = new GlycoVisitorContainsNode();
                t_objVisitor.start(a_objAlternative);
                if ( t_objVisitor.getMonosaccharideCount() > 0 )
                {
                    if ( t_objVisitor.getNonMonosaccharideCount() > 0 )
                    {
                        throw new GlycoVisitorException("GlycoVisitorSugarGraph can not handle alternative sugar units that consist of monosaccharides and aglyca.");
                    }
                    try
                    {
                        SugarUnitAlternative t_objMs = a_objAlternative.copy();
                        this.m_objLastResidue = a_objAlternative;
                        this.m_objSugarUnit.addNode(t_objMs);
                        this.m_hashResidues.put(a_objAlternative,t_objMs);
                        this.m_objCurrentSugarGraph.addTopLevelNode(a_objAlternative,t_objMs);
                    } 
                    catch (GlycoconjugateException e)
                    {
                        throw new GlycoVisitorException(e.getMessage(),e);
                    }                    
                }                
            }
        }   
        if ( this.m_objTraverser.getState() == GlycoTraverser.LEAVE )
        {
            throw new GlycoVisitorException("GlycoVisitorSugarGraph can not handle alternative residues.");
            // unable to set lead in and out , mapping missing
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode)
     */
    public void visit(UnvalidatedGlycoNode arg0) throws GlycoVisitorException
    {
        throw new GlycoVisitorException("UnvalidatedGlycoNodes are not allowed for Visitor GlycoVisitorSugarGraph.");
    }
    
    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.NonMonosaccharide)
     */
    public void visit(NonMonosaccharide a_objNonMonosaccharide) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            if ( this.m_objStartResidue == null )
            {
            }
            else if ( this.m_objTerminalAglyca != null )
            {
                return;
            }
            else
            {
                // terminal aglyca
                SugarGraphAglycon t_objTerminalAglycon = new SugarGraphAglycon(a_objNonMonosaccharide,this.m_objLastResidue,this.m_objLastEdge); 
                this.m_objCurrentSugarGraph.addTerminalInformation(t_objTerminalAglycon);
                if ( a_objNonMonosaccharide.getChildEdges().size() > 0 )
                {
                    GlycoVisitorContainsNodeBelow t_visNodes = new GlycoVisitorContainsNodeBelow();
                    t_visNodes.setDescent(true);
                    t_visNodes.start(a_objNonMonosaccharide);
                    if ( t_visNodes.getMonosaccharideCount() != 0 )
                    {
                        throw new GlycoVisitorException("Unable to handle terminal aglyca with child residues : " + a_objNonMonosaccharide.getName());
                    }
                }
                this.m_objTerminalAglyca = a_objNonMonosaccharide;
            }
        }   
        else if ( this.m_objTraverser.getState() == GlycoTraverser.LEAVE )
        {
            if ( this.m_objTerminalAglyca == a_objNonMonosaccharide )
            {
                this.m_objTerminalAglyca = null;
            }
        }
    }

    /**
     * @throws GlycoVisitorException 
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#getTraverser(de.glycosciences.MolecularFrameWork.util.SugarVisitor)
     */
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        return new GlycoTraverserTree(a_objVisitor);
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#clear()
     */
    public void clear()
    {
        this.m_aSugarGraphs.clear();
        this.m_hashResidues.clear();
        this.m_objStartResidue = null;
        this.m_objCurrentSugarGraph = null;
        this.m_objTerminalAglyca = null;
        this.m_objTraverser = null;
        this.m_objLastEdge = null;
        this.m_objLastResidue = null;
        this.m_objSugarUnit = null;
    }

    /**
     * @return
     */
    public ArrayList<SugarGraphInformation> getSugarGraphs()
    {
        return this.m_aSugarGraphs;
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.SugarRepeatingUnit)
     */
    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException
    {
        if ( this.m_objTerminalAglyca != null )
        {
            return;
        }
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            if ( this.m_objStartResidue == null )
            {
                // no sugar startet
                // 1. does not contain monosaccharides
                // 2. does contain monosaccharides
                //      1. on top level
                //          1. also contain nonmonosaccharide ==> error
                //          2. ok
                //      2. not on top level
                //          1. contain monosachares as childs ==> error
                //          2. ok, abstieg
                GlycoVisitorContainsNode t_objVisitor = new GlycoVisitorContainsNode();
                t_objVisitor.start(a_objRepeat);
                int t_iNonMsCount = t_objVisitor.getNonMonosaccharideCount();
                if ( t_objVisitor.getMonosaccharideCount() > 0 )
                {
                    t_objVisitor.setDescent(false);
                    t_objVisitor.start(a_objRepeat);
                    if ( t_objVisitor.getMonosaccharideCount() > 0 )
                    {
                        if ( t_iNonMsCount > 0 )
                        {
                            throw new GlycoVisitorException("Unable to handle repeat units that contains NonMonosaccharide Units.");                            
                        }
                        Sugar t_objSugar = new Sugar();
                        this.m_hashResidues.clear();
                        if ( a_objRepeat.getParentEdge() != null )
                        {
                            this.m_objCurrentSugarGraph = new SugarGraphInformation(t_objSugar,a_objRepeat.getParentNode(),a_objRepeat,a_objRepeat.getParentEdge());
                        }
                        else
                        {
                            this.m_objCurrentSugarGraph = new SugarGraphInformation(t_objSugar,null,a_objRepeat,a_objRepeat.getParentEdge());
                        }
                        this.m_aSugarGraphs.add(this.m_objCurrentSugarGraph);
                        this.m_objSugarUnit = t_objSugar;
                        this.m_objStartResidue = a_objRepeat;
                        // copie information of the ms
                        try
                        {
                            SugarUnitRepeat t_objMs = a_objRepeat.copy();
                            this.m_objLastResidue = a_objRepeat;
                            this.m_objSugarUnit.addNode(t_objMs);
                            this.m_hashResidues.put(a_objRepeat,t_objMs);
                            this.m_objCurrentSugarGraph.addTopLevelNode(a_objRepeat,t_objMs);
                        } 
                        catch (GlycoconjugateException e)
                        {
                            throw new GlycoVisitorException(e.getMessage(),e);
                        }
                    }
                    else
                    {
                        GlycoVisitorContainsNodeBelow t_objVisitorBelow = new GlycoVisitorContainsNodeBelow(); 
                        t_objVisitorBelow.setDescent(true);
                        t_objVisitorBelow.start(a_objRepeat);
                        if ( t_objVisitor.getMonosaccharideCount() > 0 )
                        {
                            throw new GlycoVisitorException("Unable to handle repeat units that contains NonMonosaccharide Units and have Monosaccharide childs.");
                        }
                        GlycoVisitorSugarGraph t_objVisitorGraph = new GlycoVisitorSugarGraph();
                        t_objVisitorGraph.start(a_objRepeat);
                        this.m_aSugarGraphs.addAll(t_objVisitorGraph.getSugarGraphs());
                    }
                }
            }
            else
            {
                GlycoVisitorContainsNode t_objVisitor = new GlycoVisitorContainsNode();
                t_objVisitor.start(a_objRepeat);
                if ( t_objVisitor.getNonMonosaccharideCount() > 0 )
                {
                    throw new GlycoVisitorException("Unable to handle repeat units that contains NonMonosaccharide Units.");
                }
                // attache this unit to the new sugar
                try
                {
                    SugarUnitRepeat t_objUnit = a_objRepeat.copy();
                    this.m_hashResidues.put(a_objRepeat,t_objUnit);
                    this.m_objCurrentSugarGraph.addTopLevelNode(a_objRepeat,t_objUnit);
                    this.m_objSugarUnit.addNode(t_objUnit);
                    this.m_objLastResidue = a_objRepeat;
                    // copy parent linkage
                    GlycoEdge t_objEdgeOriginal = a_objRepeat.getParentEdge();
                    GlycoEdge t_objEdgeNew = t_objEdgeOriginal.copy();
                    GlycoNode t_objParent = this.m_hashResidues.get( t_objEdgeOriginal.getParent() );
                    if ( t_objParent == null )
                    {
                        throw new GlycoVisitorException("Unable to copy monosaccharide : repeat unit's parent was not translated.");
                    }
                    this.m_objSugarUnit.addEdge(t_objParent,t_objUnit,t_objEdgeNew);
                } 
                catch (GlycoconjugateException e)
                {
                    throw new GlycoVisitorException(e.getMessage(),e);
                }
            }
        }
        if ( this.m_objTraverser.getState() == GlycoTraverser.LEAVE )
        {
            if ( a_objRepeat == this.m_objStartResidue )
            {
                this.m_objStartResidue = null;
            }
        }
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#start(de.glycosciences.MolecularFrameWork.sugar.Sugar)
     */
    public void start(Sugar a_objSugar) throws GlycoVisitorException
    {
        try
        {
            this.clear();
            ArrayList<GlycoNode> t_aRoots = a_objSugar.getRootNodes();
            if ( t_aRoots.size() != 1 )
            {
                throw new GlycoVisitorException("Unable to create sugar graphs from fragmented sugars.");
            }
            this.m_objTraverser = this.getTraverser(this);
            this.m_objTraverser.traverseGraph(a_objSugar);
            for (Iterator<SugarGraphInformation> t_iterSugarGraph = this.m_aSugarGraphs.iterator(); t_iterSugarGraph.hasNext();)
            {
                // for each new sugar we have to look if a underdeterminded tree is attache to him
                SugarGraphInformation t_objInfo = t_iterSugarGraph.next();
                HashMap<GlycoNode,GlycoNode> t_hMapping = t_objInfo.getTopLevelNodes();
                Sugar t_objSugar = t_objInfo.getSugar();
                for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();)
                {
                    UnderdeterminedSubTree t_objUnder = t_iterUnder.next();
                    // look how many parents nodes are in the sugar
                    int t_iCount = 0;
                    for (Iterator<GlycoNode> t_iterParent = t_objUnder.getParents().iterator(); t_iterParent.hasNext();)
                    {
                        if ( t_hMapping.get(t_iterParent.next()) != null )
                        {
                            t_iCount++;
                        }                        
                    }
                    if ( t_iCount > 0 )
                    {
                        if ( t_iCount != t_objUnder.getParents().size() )
                        {
                            throw new GlycoVisitorException("There is at least one UnderdetermindedSubtree that is only partially connected to a SugarGraph.");
                        }
                        // add the tree to the 
                        UnderdeterminedSubTree t_objUnderNew = t_objUnder.copy();
                        t_objSugar.addUndeterminedSubTree(t_objUnderNew);
                        for (Iterator<GlycoNode> t_iterParent = t_objUnder.getParents().iterator(); t_iterParent.hasNext();)
                        {
                            t_objSugar.addUndeterminedSubTreeParent(t_objUnderNew,t_hMapping.get(t_iterParent.next()));
                        }
                    }
                }
            }
        } 
        catch (GlycoconjugateException e)
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }        
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#start(de.glycosciences.MolecularFrameWork.sugar.Sugar)
     */
    public void start(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException
    {
        try
        {
            ArrayList<GlycoNode> t_aRoots = a_objRepeat.getRootNodes();
            if ( t_aRoots.size() != 1 )
            {
                throw new GlycoVisitorException("Unable to create sugar graphs from fragmented sugars.");
            }
            this.m_objTraverser = this.getTraverser(this);
            this.m_objTraverser.traverseGraph(a_objRepeat);
            for (Iterator<SugarGraphInformation> t_iterSugarGraph = this.m_aSugarGraphs.iterator(); t_iterSugarGraph.hasNext();)
            {
                // for each new sugar we have to look if a underdeterminded tree is attache to him
                SugarGraphInformation t_objInfo = t_iterSugarGraph.next();
                HashMap<GlycoNode,GlycoNode> t_hMapping = t_objInfo.getTopLevelNodes();
                Sugar t_objSugar = t_objInfo.getSugar();
                for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objRepeat.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();)
                {
                    UnderdeterminedSubTree t_objUnder = t_iterUnder.next();
                    // look how many parents nodes are in the sugar
                    int t_iCount = 0;
                    for (Iterator<GlycoNode> t_iterParent = t_objUnder.getParents().iterator(); t_iterParent.hasNext();)
                    {
                        if ( t_hMapping.get(t_iterParent.next()) != null )
                        {
                            t_iCount++;
                        }                        
                    }
                    if ( t_iCount > 0 )
                    {
                        if ( t_iCount != t_objUnder.getParents().size() )
                        {
                            throw new GlycoVisitorException("There is at least one UnderdetermindedSubtree that is only partially connected to a SugarGraph.");
                        }
                        // add the tree to the 
                        UnderdeterminedSubTree t_objUnderNew = t_objUnder.copy();
                        t_objSugar.addUndeterminedSubTree(t_objUnderNew);
                        for (Iterator<GlycoNode> t_iterParent = t_objUnder.getParents().iterator(); t_iterParent.hasNext();)
                        {
                            t_objSugar.addUndeterminedSubTreeParent(t_objUnderNew,t_hMapping.get(t_iterParent.next()));
                        }
                    }
                }
            }
 
        } 
        catch (GlycoconjugateException e)
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }        
    }
}