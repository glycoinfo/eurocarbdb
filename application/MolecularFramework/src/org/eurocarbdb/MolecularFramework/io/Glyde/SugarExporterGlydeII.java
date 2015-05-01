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
package org.eurocarbdb.MolecularFramework.io.Glyde;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
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
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserTreeSingle;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

/**
* 
* http://glycomics.ccrc.uga.edu/GLYDE-II/
* 
* @author Logan
*/
public class SugarExporterGlydeII implements GlycoVisitor
{
    private String m_strReferenceDB = "http://www.monosaccharideDB.org/GLYDE-II.jsp?G";
    private Document m_objDocument;
    private Element m_objRootElement;
    private Integer m_iResCounter;
    
    private boolean m_bRepeat = false;
    private ArrayList<Integer> m_aRepeatResidues = new ArrayList<Integer>(); 
    
    private HashMap<SugarUnitRepeat,ArrayList<Integer>> m_hashRepeats = new HashMap<SugarUnitRepeat,ArrayList<Integer>>();
    private ArrayList<UnderdeterminedSubTree> m_aUnderdetermindedTrees = new ArrayList<UnderdeterminedSubTree>(); 
    private ArrayList<UnderdeterminedSubTree> m_aStatisticTrees = new ArrayList<UnderdeterminedSubTree>();
    private HashMap<GlycoNode,Integer> m_hashResidueID = new HashMap<GlycoNode,Integer>();
    private ArrayList<GlycoEdge> m_aEdges = new ArrayList<GlycoEdge>();
    
    /**
     * @throws GlycoVisitorException 
     * @see org.glycomedb.MolecularFrameWork.util.visitor.SugarVisitor#getTraverser(org.glycomedb.MolecularFrameWork.util.visitor.SugarVisitor)
     */
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {        
        return new GlycoTraverserTreeSingle(a_objVisitor);
    }

    /**
     * @see org.glycomedb.MolecularFrameWork.util.visitor.SugarVisitor#clear()
     */
    public void clear() 
    {
        this.m_objDocument = null;
        this.m_objRootElement = null;
        this.m_iResCounter = 1;
        this.m_hashRepeats.clear();
        this.m_aUnderdetermindedTrees.clear();
        this.m_aStatisticTrees.clear(); 
        this.m_hashResidueID.clear();
        this.m_aEdges.clear();
        this.m_aRepeatResidues.clear();
        this.m_bRepeat = false;
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException 
    {
        if ( this.m_bRepeat )
        {
            int t_iCounter = this.m_iResCounter;
            this.m_aRepeatResidues.add(t_iCounter);
        }
        Element t_objResidue = new Element("residue");
        t_objResidue.setAttribute("subtype","base_type");
        this.m_hashResidueID.put(a_objMonosaccharid,this.m_iResCounter);
        t_objResidue.setAttribute("partid",this.m_iResCounter.toString());
        this.m_iResCounter++;
        t_objResidue.setAttribute("ref",this.m_strReferenceDB + "=" + a_objMonosaccharid.getGlycoCTName());
        this.m_objRootElement.addContent(t_objResidue);
        GlycoEdge t_objEdge = a_objMonosaccharid.getParentEdge(); 
        if ( t_objEdge != null )
        {
            this.m_aEdges.add(t_objEdge);
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Substituent)
     */
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException 
    {
        if ( this.m_bRepeat )
        {
            int t_iCounter = this.m_iResCounter;
            this.m_aRepeatResidues.add(t_iCounter);
        }
        Element t_objResidue = new Element("residue");
        t_objResidue.setAttribute("subtype","substituent");
        this.m_hashResidueID.put(a_objSubstituent,this.m_iResCounter);
        t_objResidue.setAttribute("partid",this.m_iResCounter.toString());
        this.m_iResCounter++;
        t_objResidue.setAttribute("ref",this.m_strReferenceDB + "=" + a_objSubstituent.getSubstituentType().getName());
        this.m_objRootElement.addContent(t_objResidue);
        GlycoEdge t_objEdge = a_objSubstituent.getParentEdge(); 
        if ( t_objEdge != null )
        {
            this.m_aEdges.add(t_objEdge);
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException 
    {
        GlycoEdge t_objEdge = a_objCyclic.getParentEdge(); 
        this.m_aEdges.add(t_objEdge);
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("SugarUnitAlternative are not supported.");
    }

    /**
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharides)
     */
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("NonMonosaccharides are not supported.");
    }

    /**
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.SugarUnitRepeat)
     */
    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException 
    {
        try
        {
            if ( this.m_bRepeat )
            {
                throw new GlycoVisitorException("GlydeII does not support nested repeat units.");
            }
            this.start(a_objRepeat);
            GlycoEdge t_objEdge = a_objRepeat.getParentEdge(); 
            if ( t_objEdge != null )
            {
                this.m_aEdges.add(t_objEdge);
            }
            for (Iterator<UnderdeterminedSubTree> t_iterSubtree = a_objRepeat.getUndeterminedSubTrees().iterator(); t_iterSubtree.hasNext();)
            {
                UnderdeterminedSubTree t_objTree = t_iterSubtree.next(); 
                this.start(t_objTree);
                if ( t_objTree.getParents().size() == 1 )
                {
                    this.m_aStatisticTrees.add(t_objTree);
                }
                else
                {
                    this.m_aUnderdetermindedTrees.add(t_objTree);
                }
            }
        } 
        catch (GlycoconjugateException e)
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }        
    }

    /**
     * @param repeat
     * @throws GlycoconjugateException 
     */
    private void start(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException, GlycoconjugateException 
    {
        if ( a_objRepeat.getRootNodes().size() != 1 )
        {
            throw new GlycoVisitorException("GlydeII can not handle unconnected sugars.");
        }
        this.m_aRepeatResidues = new ArrayList<Integer>();
        this.m_bRepeat = true;
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objRepeat);
        this.m_hashRepeats.put(a_objRepeat,this.m_aRepeatResidues);
        this.m_bRepeat = false;
    }

    /**
     * @param tree
     * @throws GlycoVisitorException 
     * @throws GlycoconjugateException 
     */
    private void start(UnderdeterminedSubTree a_objTree) throws GlycoVisitorException, GlycoconjugateException 
    {
        if ( a_objTree.getRootNodes().size() != 1 )
        {
            throw new GlycoVisitorException("GlydeII can not handle unconnected sugars.");
        }
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objTree);        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.GlycoEdge)
     */
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException 
    {
        // nothing to do
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode)
     */
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("UnvalidatedGlycoNode are not allowed for GlydeII.");
    }

    /**
     * Adds the complete sugar information below the given element
     * @param a_objSugar
     * @param t_objRootElement
     * @throws GlycoVisitorException
     */
    public void start(Sugar a_objSugar,Element a_objRootElement) throws GlycoVisitorException
    {
        this.clear();
        this.m_objRootElement =  new Element("molecule"); 
        this.m_objRootElement.setAttribute("subtype","glycan");        
        this.m_objRootElement.setAttribute("id","From_GlycoCT_Translation");
        try
        {
            this.export(a_objSugar);
        } 
        catch (GlycoconjugateException e)
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
    }
    
    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#start(org.eurocarbdb.MolecularFramework.sugar.Sugar)
     */
    public void start(Sugar a_objSugar) throws GlycoVisitorException 
    {
        this.clear();
        Element t_objRoot = new Element("GlydeII");
        this.m_objDocument = new Document(t_objRoot);
        this.m_objRootElement =  new  Element("molecule"); 
        this.m_objRootElement.setAttribute("subtype","glycan");
        this.m_objRootElement.setAttribute("id","From_GlycoCT_Translation");
        t_objRoot.addContent(this.m_objRootElement);
        try
        {
            this.export(a_objSugar);
        } 
        catch (GlycoconjugateException e)
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
    }
    
    private void export(Sugar a_objSugar) throws GlycoVisitorException, GlycoconjugateException
    {
        if ( a_objSugar.getRootNodes().size() != 1 )
        {
            throw new GlycoVisitorException("GlydeII can not handle unconnected sugars.");
        }
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        for (Iterator<UnderdeterminedSubTree> t_iterSubtree = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterSubtree.hasNext();)
        {
            UnderdeterminedSubTree t_objTree = t_iterSubtree.next(); 
            this.start(t_objTree);
            if ( t_objTree.getParents().size() == 1 )
            {
                this.m_aStatisticTrees.add(t_objTree);
            }
            else
            {
                this.m_aUnderdetermindedTrees.add(t_objTree);
            }
        }
        // linkages
        for (Iterator<GlycoEdge> t_iterEdges = this.m_aEdges.iterator(); t_iterEdges.hasNext();) 
        {
            GlycoEdge t_objEdge = t_iterEdges.next();
            Integer t_iParentID = this.m_hashResidueID.get(t_objEdge.getParent()); 
            if ( t_iParentID == null )
            {
                // may be repeat/cyclic or error
                GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType();
                SugarUnitRepeat t_objRepeat = t_objNodeType.getSugarUnitRepeat(t_objEdge.getParent());
                if ( t_objRepeat == null )
                {
                    throw new GlycoconjugateException("Critical error: unknown residue in linkage.");
                }
                t_iParentID = this.m_hashResidueID.get(t_objRepeat.getRepeatLinkage().getParent());
                if ( t_iParentID == null )
                {
                    throw new GlycoconjugateException("Critical error: unknown repeat residue in linkage.");
                }
            }
            Integer t_iChildID = this.m_hashResidueID.get(t_objEdge.getChild());
            if ( t_iChildID == null )
            {
                // may be repeat or error
                GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType();
                SugarUnitRepeat t_objRepeat = t_objNodeType.getSugarUnitRepeat(t_objEdge.getChild());
                if ( t_objRepeat != null )
                {
                     t_iChildID = this.m_hashResidueID.get(t_objRepeat.getRepeatLinkage().getChild());
                    if ( t_iChildID == null )
                    {
                        throw new GlycoconjugateException("Critical error: unknown repeat residue in linkage.");
                    }
                }
                else
                {
                    SugarUnitCyclic t_objCyclic = t_objNodeType.getSugarUnitCyclic(t_objEdge.getChild());
                    if ( t_objCyclic == null )
                    {
                        throw new GlycoconjugateException("Critical error: unknown residue in linkage.");
                    }
                     t_iChildID = this.m_hashResidueID.get(t_objCyclic.getCyclicStart());
                    if ( t_iChildID == null )
                    {
                        // repeat
                        t_objRepeat = t_objNodeType.getSugarUnitRepeat(t_objEdge.getChild());
                        if ( t_objRepeat == null )
                        {
                            throw new GlycoconjugateException("Critical error: unknown residue in linkage.");
                        }
                        t_iParentID = this.m_hashResidueID.get(t_objRepeat.getRepeatLinkage().getChild());
                        if ( t_iParentID == null )
                        {
                            throw new GlycoconjugateException("Critical error: unknown repeat residue in linkage.");
                        }
                    }
                }
            }
            Element t_objEdgeElement = new Element("residue_link");
            t_objEdgeElement.setAttribute("from",t_iChildID.toString());
            t_objEdgeElement.setAttribute("to",t_iParentID.toString());
            this.m_objRootElement.addContent(t_objEdgeElement);
            int t_iCounter = 0;
            for (Iterator<Linkage> t_iterLinkage = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();) 
            {
                t_iCounter++;
                Linkage t_objLinkage = t_iterLinkage.next();
                Element t_objLinkageElement = new Element("atom_link");
                Atomnames t_objAtomNames = this.getAtomnames(t_objEdge.getParent(),t_objEdge.getChild(),t_objLinkage,t_iCounter);
                t_objLinkageElement.setAttribute("from",t_objAtomNames.m_strFrom);
                t_objLinkageElement.setAttribute("to",t_objAtomNames.m_strTo);
                if ( t_objAtomNames.m_strToReplace != null )
                {
                    t_objLinkageElement.setAttribute("to_replace",t_objAtomNames.m_strToReplace);
                }
                if ( t_objAtomNames.m_strFromReplace != null )
                {
                    t_objLinkageElement.setAttribute("from_replace",t_objAtomNames.m_strFromReplace);
                }
                t_objLinkageElement.setAttribute("bond_order","1");
                t_objEdgeElement.addContent(t_objLinkageElement);
            }
        }
        // statistical
        for (Iterator<UnderdeterminedSubTree> t_iterStats = this.m_aStatisticTrees.iterator(); t_iterStats.hasNext();) 
        {
            UnderdeterminedSubTree t_objTree = t_iterStats.next();
            GlycoEdge t_objEdge = t_objTree.getConnection();
            Integer t_iParentID = this.m_hashResidueID.get(t_objTree.getParents().get(0)); 
            if ( t_objTree.getRootNodes().size() != 1 )
            {
                throw new GlycoVisitorException("GlydeII does not support unconnected statistical distribution.");
            }
            Integer t_iChildID = this.m_hashResidueID.get(t_objTree.getRootNodes().get(0));
            Element t_objEdgeElement = new Element("residue_link");
            t_objEdgeElement.setAttribute("from",t_iChildID.toString());
            t_objEdgeElement.setAttribute("to",t_iParentID.toString());
            if ( t_objTree.getProbabilityLower() != t_objTree.getProbabilityUpper() )
            {
                throw new GlycoVisitorException("GlydeII does not support statistical distribution with an interval.");
            }
            t_objEdgeElement.setAttribute("stat",Double.toString(t_objTree.getProbabilityLower()));
            this.m_objRootElement.addContent(t_objEdgeElement);
            int t_iCounter = 0;
            for (Iterator<Linkage> t_iterLinkage = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();) 
            {
                t_iCounter++;
                Linkage t_objLinkage = t_iterLinkage.next();
                Element t_objLinkageElement = new Element("atom_link");
                Atomnames t_objAtomNames = this.getAtomnames(t_objTree.getParents().get(0),t_objTree.getRootNodes().get(0),t_objLinkage,t_iCounter);
                t_objLinkageElement.setAttribute("from",t_objAtomNames.m_strFrom);
                t_objLinkageElement.setAttribute("to",t_objAtomNames.m_strTo);
                if ( t_objAtomNames.m_strToReplace != null )
                {
                    t_objLinkageElement.setAttribute("to_replace",t_objAtomNames.m_strToReplace);
                }
                if ( t_objAtomNames.m_strFromReplace != null )
                {
                    t_objLinkageElement.setAttribute("from_replace",t_objAtomNames.m_strFromReplace);
                }
                t_objLinkageElement.setAttribute("bond_order","1");
                t_objEdgeElement.addContent(t_objLinkageElement);
            }
        }
        // combinations
        for (Iterator<UnderdeterminedSubTree> t_iterUnder = this.m_aUnderdetermindedTrees.iterator(); t_iterUnder.hasNext();)
        {
            UnderdeterminedSubTree t_objTree = t_iterUnder.next();
            Element t_objCombinationElement = new Element("combination");
            this.m_objRootElement.addContent(t_objCombinationElement);
            if ( t_objTree.getRootNodes().size() != 1 )
            {
                throw new GlycoVisitorException("GlydeII can not handle unconnected sugars.");
            }
            Integer t_iChildID = this.m_hashResidueID.get(t_objTree.getRootNodes().get(0));
            GlycoEdge t_objEdge = t_objTree.getConnection();
            for (Iterator<GlycoNode> t_iterParents = t_objTree.getParents().iterator(); t_iterParents.hasNext();)
            {
                GlycoNode t_objParent = t_iterParents.next();
                Integer t_iParentID = this.m_hashResidueID.get(t_objParent);
                Element t_objEdgeElement = new Element("residue_link");
                t_objCombinationElement.addContent(t_objEdgeElement);
                t_objEdgeElement.setAttribute("from",t_iChildID.toString());
                t_objEdgeElement.setAttribute("to",t_iParentID.toString());
                int t_iCounter = 0;
                for (Iterator<Linkage> t_iterLinkage = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();) 
                {
                    t_iCounter++;
                    Linkage t_objLinkage = t_iterLinkage.next();
                    Element t_objLinkageElement = new Element("atom_link");
                    Atomnames t_objAtomNames = this.getAtomnames(t_objParent,t_objTree.getRootNodes().get(0),t_objLinkage,t_iCounter);
                    t_objLinkageElement.setAttribute("from",t_objAtomNames.m_strFrom);
                    t_objLinkageElement.setAttribute("to",t_objAtomNames.m_strTo);
                    if ( t_objAtomNames.m_strToReplace != null )
                    {
                        t_objLinkageElement.setAttribute("to_replace",t_objAtomNames.m_strToReplace);
                    }
                    if ( t_objAtomNames.m_strFromReplace != null )
                    {
                        t_objLinkageElement.setAttribute("from_replace",t_objAtomNames.m_strFromReplace);
                    }
                    t_objLinkageElement.setAttribute("bond_order","1");
                    t_objEdgeElement.addContent(t_objLinkageElement);
                }
            }                        
        }        
        // repeats
        for (Iterator<SugarUnitRepeat> t_iterRepeat = this.m_hashRepeats.keySet().iterator(); t_iterRepeat.hasNext();)
        {
            SugarUnitRepeat t_objRepeat = t_iterRepeat.next();
            if ( t_objRepeat.getMaxRepeatCount() != t_objRepeat.getMinRepeatCount() )
            {
                throw new GlycoVisitorException("GlydeII does not support repeat count interval.");
            }
            Element t_objRepeatElement = new Element("repeat_block");
            if ( t_objRepeat.getMinRepeatCount() == SugarUnitRepeat.UNKNOWN )
            {
                t_objRepeatElement.setAttribute("repeat_number","n");
            }
            else
            {
                t_objRepeatElement.setAttribute("repeat_number",Integer.toString(t_objRepeat.getMinRepeatCount()));
            }
            this.m_objRootElement.addContent(t_objRepeatElement);
            ArrayList<Integer> t_aParts = this.m_hashRepeats.get(t_objRepeat);
            for (Iterator<Integer> t_iterResidues = t_aParts.iterator(); t_iterResidues.hasNext();)
            {
                Element t_objResiduePart = new Element("repeat_part");
                t_objResiduePart.setAttribute("ref",t_iterResidues.next().toString());
                t_objRepeatElement.addContent(t_objResiduePart);
            }
            // repeat linkage
            GlycoEdge t_objEdge = t_objRepeat.getRepeatLinkage();
            Integer t_iParentID = this.m_hashResidueID.get(t_objEdge.getParent()); 
            Integer t_iChildID = this.m_hashResidueID.get(t_objEdge.getChild());
            Element t_objEdgeElement = new Element("residue_link");
            t_objEdgeElement.setAttribute("from",t_iChildID.toString());
            t_objEdgeElement.setAttribute("to",t_iParentID.toString());
            t_objRepeatElement.addContent(t_objEdgeElement);
            int t_iCounter = 0;
            for (Iterator<Linkage> t_iterLinkage = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();) 
            {
                t_iCounter++;
                Linkage t_objLinkage = t_iterLinkage.next();
                Element t_objLinkageElement = new Element("atom_link");
                Atomnames t_objAtomNames = this.getAtomnames(t_objEdge.getParent(),t_objEdge.getChild(),t_objLinkage,t_iCounter);
                t_objLinkageElement.setAttribute("from",t_objAtomNames.m_strFrom);
                t_objLinkageElement.setAttribute("to",t_objAtomNames.m_strTo);
                if ( t_objAtomNames.m_strToReplace != null )
                {
                    t_objLinkageElement.setAttribute("to_replace",t_objAtomNames.m_strToReplace);
                }
                if ( t_objAtomNames.m_strFromReplace != null )
                {
                    t_objLinkageElement.setAttribute("from_replace",t_objAtomNames.m_strFromReplace);
                }
                t_objLinkageElement.setAttribute("bond_order","1");
                t_objEdgeElement.addContent(t_objLinkageElement);
            }
        }
    }

    public String getXMLCode() throws IOException
    {
        Format t_objFormat = Format.getPrettyFormat();
        XMLOutputter t_objExportXML = new XMLOutputter(t_objFormat);
        StringWriter t_objWriter = new StringWriter();
        t_objExportXML.output(this.m_objDocument, t_objWriter );
        return t_objWriter.toString();
    }
    
    public Document getDocument()
    {
        return this.m_objDocument;
    }

    private String getChildReplaceString(LinkageType a_objType, Linkage a_objLinkage,GlycoNode a_objNode, int a_iLinkNumber) throws GlycoVisitorException
    {
        String t_strResult = "";
        String t_strSymbol = "";
        if ( a_objType == LinkageType.DEOXY )
        {
            t_strSymbol = "O";
        }
        else if ( a_objType == LinkageType.H_LOSE )
        {
            t_strSymbol = "HC";
        }
        else if ( a_objType == LinkageType.NONMONOSACCHARID )
        {
            GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType();
            if ( t_objNodeType.isSubstituent(a_objNode) )
            {
                return this.getSubstReplaceLink(t_objNodeType.getSubstituent(a_objNode),true ,a_iLinkNumber);
            }
            else if ( t_objNodeType.isSugarUnitRepeat(a_objNode) )
            {
                SugarUnitRepeat t_objRepeat = t_objNodeType.getSugarUnitRepeat(a_objNode);
                return this.getChildReplaceString(t_objRepeat.getRepeatLinkage().getGlycosidicLinkages().get(0).getParentLinkageType(), a_objLinkage, t_objRepeat.getRepeatLinkage().getParent(),a_iLinkNumber);
            }
        }
        else if ( a_objType == LinkageType.UNKNOWN )
        {
            throw new GlycoVisitorException("Linkage type UNKNOWN is not allowed.");
        }
        if ( t_strSymbol == null )
        {
            return null;
        }
        Iterator<Integer> t_iterPosition = a_objLinkage.getChildLinkages().iterator();
        t_strResult += t_strSymbol + t_iterPosition.next().toString();
        while ( t_iterPosition.hasNext() )
        {
            t_strResult += "|" + t_strSymbol + t_iterPosition.next().toString();            
        }
        return t_strResult;
    }

    private String getParentReplaceString(LinkageType a_objType, Linkage a_objLinkage,GlycoNode a_objNode, int a_iLinkNumber) throws GlycoVisitorException
    {
        String t_strResult = "";
        String t_strSymbol = null;
        if ( a_objType == LinkageType.DEOXY )
        {
            t_strSymbol = "O";
        }
        else if ( a_objType == LinkageType.H_LOSE )
        {
            t_strSymbol = "HC";
        }
        else if ( a_objType == LinkageType.NONMONOSACCHARID )
        {
            GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType();
            if ( t_objNodeType.isSubstituent(a_objNode) )
            {
                return this.getSubstReplaceLink(t_objNodeType.getSubstituent(a_objNode),false ,a_iLinkNumber);
            }
            else if ( t_objNodeType.isSugarUnitRepeat(a_objNode) )
            {
                SugarUnitRepeat t_objRepeat = t_objNodeType.getSugarUnitRepeat(a_objNode);
                return this.getParentReplaceString(t_objRepeat.getRepeatLinkage().getGlycosidicLinkages().get(0).getChildLinkageType(), a_objLinkage, t_objRepeat.getRepeatLinkage().getChild(),a_iLinkNumber);
            }
        }
        else if ( a_objType == LinkageType.UNKNOWN )
        {
            throw new GlycoVisitorException("Linkage type UNKNOWN is not allowed.");
        }
        if ( t_strSymbol == null )
        {
            return null;
        }
        Iterator<Integer> t_iterPosition = a_objLinkage.getParentLinkages().iterator();
        t_strResult += t_strSymbol + t_iterPosition.next().toString();
        while ( t_iterPosition.hasNext() )
        {
            t_strResult += "|" + t_strSymbol + t_iterPosition.next().toString();            
        }
        return t_strResult;
    }

    private Atomnames getAtomnames(GlycoNode a_objParent, GlycoNode a_objChild, Linkage a_objLinkage,int a_iLinknumber) throws GlycoVisitorException 
    {
        Atomnames t_objResult = new Atomnames();
        t_objResult.m_strTo = this.getParentString(a_objLinkage.getParentLinkageType(),a_objLinkage,a_objParent,a_iLinknumber);
        t_objResult.m_strToReplace = this.getParentReplaceString(a_objLinkage.getParentLinkageType(),a_objLinkage,a_objParent,a_iLinknumber);
        t_objResult.m_strFrom = this.getChildString(a_objLinkage.getChildLinkageType(),a_objLinkage,a_objChild,a_iLinknumber);
        t_objResult.m_strFromReplace = this.getChildReplaceString(a_objLinkage.getChildLinkageType(),a_objLinkage,a_objChild,a_iLinknumber);        
        return t_objResult;
    }

    private String getParentString(LinkageType a_objType,Linkage a_objLinkage,GlycoNode a_objNode,int a_iLinknumber) throws GlycoVisitorException
    {
        String t_strResult = "";
        String t_strSymbol = "";
        if ( a_objType == LinkageType.DEOXY )
        {
            t_strSymbol = "C";
        }
        else if ( a_objType == LinkageType.H_AT_OH )
        {
            t_strSymbol = "O";
        }
        else if ( a_objType == LinkageType.H_LOSE )
        {
            t_strSymbol = "C";
        }
        else if ( a_objType == LinkageType.NONMONOSACCHARID )
        {
            GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType();
            if ( t_objNodeType.isSubstituent(a_objNode) )
            {
                return this.getSubstLink(t_objNodeType.getSubstituent(a_objNode),false ,a_iLinknumber);
            }
            else if ( t_objNodeType.isSugarUnitRepeat(a_objNode) )
            {
                SugarUnitRepeat t_objRepeat = t_objNodeType.getSugarUnitRepeat(a_objNode);
                return this.getParentString(t_objRepeat.getRepeatLinkage().getGlycosidicLinkages().get(0).getChildLinkageType(), a_objLinkage, t_objRepeat.getRepeatLinkage().getChild(),a_iLinknumber);
            }
        }
        else if ( a_objType == LinkageType.UNKNOWN )
        {
            throw new GlycoVisitorException("Linkage type UNKNOWN is not allowed.");
        }
        else 
        {
            throw new GlycoVisitorException("Unsupported linkage type.");
        }
        Iterator<Integer> t_iterPosition = a_objLinkage.getParentLinkages().iterator();
        t_strResult += t_strSymbol + t_iterPosition.next().toString();
        while ( t_iterPosition.hasNext() )
        {
            t_strResult += "|" + t_strSymbol + t_iterPosition.next().toString();            
        }
        return t_strResult;
    }

    /**
     * @param linkage
     * @return
     * @throws GlycoVisitorException 
     */
    private String getChildString(LinkageType a_objType,Linkage a_objLinkage,GlycoNode a_objNode,int a_iLinkNumber) throws GlycoVisitorException
    {
        String t_strResult = "";
        String t_strSymbol = "";
        if ( a_objType == LinkageType.DEOXY )
        {
            t_strSymbol = "C";
        }
        else if ( a_objType == LinkageType.H_AT_OH )
        {
            t_strSymbol = "O";
        }
        else if ( a_objType == LinkageType.H_LOSE )
        {
            t_strSymbol = "C";
        }
        else if ( a_objType == LinkageType.NONMONOSACCHARID )
        {
            GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType();
            if ( t_objNodeType.isSubstituent(a_objNode) )
            {
                return this.getSubstLink(t_objNodeType.getSubstituent(a_objNode) ,true ,a_iLinkNumber);
            }
            else if ( t_objNodeType.isSugarUnitRepeat(a_objNode) )
            {
                SugarUnitRepeat t_objRepeat = t_objNodeType.getSugarUnitRepeat(a_objNode);
                return this.getParentString(t_objRepeat.getRepeatLinkage().getGlycosidicLinkages().get(0).getParentLinkageType(), a_objLinkage, t_objRepeat.getRepeatLinkage().getParent(),a_iLinkNumber);
            }
        }
        else if ( a_objType == LinkageType.UNKNOWN )
        {
            throw new GlycoVisitorException("Linkage type UNKNOWN is not allowed.");
        }
        else 
        {
            throw new GlycoVisitorException("Unsupported linkage type.");
        }
        Iterator<Integer> t_iterPosition = a_objLinkage.getChildLinkages().iterator();
        t_strResult += t_strSymbol + t_iterPosition.next().toString();
        while ( t_iterPosition.hasNext() )
        {
            t_strResult += "|" + t_strSymbol + t_iterPosition.next().toString();            
        }
        return t_strResult;
    }

    private String getSubstLink(Substituent a_objSubstituent, boolean a_bChildResidue,int a_iLinkNumber) throws GlycoVisitorException 
    {
        if (a_bChildResidue)
        {
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.NITRATE )
            {
                return "N";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.SULFATE )
            {
                return "S";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.PHOSPHATE )
            {
                return "P";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.PHOSPHO_ETHANOLAMINE )
            {
                return "P1";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.PHOSPHO_CHOLINE )
            {
                return "P1";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.N_SUCCINATE )
            {
                return "N4H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.PYRUVATE )
            {
                return "C2";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.N_ALANINE )
            {
                return "N1H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.GLYCOLYL )
            {
                return "C1";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.N_GLYCOLYL )
            {
                return "N1H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.N_TRIFLOUROACETYL )
            {
                return "N1H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.N_METHYLCARBAMOYL )
            {
                return "C1";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.ACETYL )
            {
                return "C1";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.FORMYL )
            {
                return "C1";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.N_FORMYL )
            {
                return "N1H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.N_SULFATE )
            {
                return "N1H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.N_ACETYL )
            {
                return "N1H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.METHYL )
            {
                return "C1";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.ETHYL )
            {
                return "C1";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.N_METHYL )
            {
                return "N1H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.ETHANOLAMINE )
            {
                return "C1";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.THIO )
            {
                return "S1H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.FLOURO )
            {
                return "F";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.CHLORO )
            {
                return "Cl";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.BROMO )
            {
                return "Br";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.IODO )
            {
                return "I";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.AMINO )
            {
                return "N1H";
            }
            throw new GlycoVisitorException("Undefined atomname for child residue " + a_objSubstituent.getSubstituentType().getName());
        }
        else
        {
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.PHOSPHATE )
            {
                return "P";
            }
            throw new GlycoVisitorException("Undefined atomname for parent residue " + a_objSubstituent.getSubstituentType().getName());            
        }
    }
    
    private String getSubstReplaceLink(Substituent a_objSubstituent, boolean a_bChildResidue, int a_iLinkNumber) 
    {
        if ( a_bChildResidue )
        {
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.NITRATE )
            {
                return "OH";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.SULFATE )
            {
                return "OHA";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.PHOSPHATE )
            {
                return "OHA";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.PHOSPHO_ETHANOLAMINE )
            {
                return "O1HR";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.PHOSPHO_CHOLINE )
            {
                return "O1HR";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.PYRUVATE )
            {
                if ( a_iLinkNumber == 1 )
                {
                    return "O2HR";
                }
                return "O2HS";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.GLYCOLYL )
            {
                return "O1H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.N_METHYLCARBAMOYL )
            {
                return "O1H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.FORMYL )
            {
                return "O1H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.METHYL )
            {
                return "O1H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.ETHYL )
            {
                return "O1H";
            }
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.ETHANOLAMINE )
            {
                return "O1H";
            }
        }
        else
        {
            if ( a_objSubstituent.getSubstituentType() == SubstituentType.PHOSPHATE )
            {
                return "OHB";
            }
        }
        return null;
    }

    
}