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
package org.eurocarbdb.MolecularFramework.util.visitor;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserTree;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;


/**
* Calculates longest branch for a residue or a sugar. Cyclic structures are handled as if the cycle is open.
* 
* @author rene
*
*/
public class GlycoVisitorCountLongestBranch implements GlycoVisitor
{
    private GlycoTraverser m_objTraverser;
    private HashMap<GlycoNode,Integer> m_hashSubResidueCount = new HashMap<GlycoNode,Integer>(); 
    private HashMap<GlycoNode,Integer> m_hashSubMonosaccharideCount = new HashMap<GlycoNode,Integer>();
    private HashMap<GlycoNode,Integer> m_hashRepeatCount = new HashMap<GlycoNode,Integer>();
    private int m_iLongestBranchMS = 0;
    private int m_iLongestBranchResidue = 0;
    private SugarUnitRepeat m_objRepeat = null;
    
    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.LEAVE )
        {
            int t_iNumber = 0;
            int t_iLongestResidue = 0;
            int t_iLongestMS = 0;
            GlycoNode t_objResidue;
            for (Iterator<GlycoNode> t_iterResidues = a_objMonosaccharid.getChildNodes().iterator(); t_iterResidues.hasNext();)
            {
                t_objResidue = t_iterResidues.next();
                if ( this.m_hashSubResidueCount.containsKey(t_objResidue) )
                {
                    t_iNumber = this.m_hashSubResidueCount.get(t_objResidue);
                    if ( t_iLongestResidue < t_iNumber )
                    {
                        t_iLongestResidue = t_iNumber;
                    }
                }
                if ( this.m_hashSubMonosaccharideCount.containsKey(t_objResidue) )
                {
                    t_iNumber = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                    if ( t_iLongestMS < t_iNumber )
                    {
                        t_iLongestMS = t_iNumber;
                    }
                }
                if ( this.m_hashRepeatCount.containsKey(t_objResidue) )
                {
                    // residue is in the repeat chain
                    t_iNumber = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                    this.m_hashRepeatCount.put((GlycoNode)a_objMonosaccharid,t_iNumber+1);
                }
            }
            t_iLongestResidue++;
            t_iLongestMS++;
            this.m_hashSubResidueCount.put((GlycoNode)a_objMonosaccharid,t_iLongestResidue);
            this.m_hashSubMonosaccharideCount.put((GlycoNode)a_objMonosaccharid,t_iLongestMS);
        }        
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.NonMonosaccharide)
     */
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.LEAVE )
        {
            int t_iNumber = 0;
            int t_iLongestResidue = 0;
            int t_iLongestMS = 0;
            GlycoNode t_objResidue;
            for (Iterator<GlycoNode> t_iterResidues = a_objResidue.getChildNodes().iterator(); t_iterResidues.hasNext();)
            {
                t_objResidue = t_iterResidues.next();
                if ( this.m_hashSubResidueCount.containsKey(t_objResidue) )
                {
                    t_iNumber = this.m_hashSubResidueCount.get(t_objResidue);
                    if ( t_iLongestResidue < t_iNumber )
                    {
                        t_iLongestResidue = t_iNumber;
                    }
                }
                if ( this.m_hashSubMonosaccharideCount.containsKey(t_objResidue) )
                {
                    t_iNumber = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                    if ( t_iLongestMS < t_iNumber )
                    {
                        t_iLongestMS = t_iNumber;
                    }
                }
                if ( this.m_hashRepeatCount.containsKey(t_objResidue) )
                {
                    // residue is in the repeat chain
                    t_iNumber = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                    this.m_hashRepeatCount.put((GlycoNode)a_objResidue,t_iNumber+1);
                }
            }
            t_iLongestResidue++;
            t_iLongestMS++;
            this.m_hashSubResidueCount.put((GlycoNode)a_objResidue,t_iLongestResidue);
            this.m_hashSubMonosaccharideCount.put((GlycoNode)a_objResidue,t_iLongestMS);
        }        
    }

    /**
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
        this.m_hashSubResidueCount.clear();
        this.m_hashSubMonosaccharideCount.clear();
        this.m_iLongestBranchMS = 0;
        this.m_iLongestBranchResidue = 0;
        this.m_objRepeat = null;
        this.m_hashRepeatCount.clear();
    }

    public int getLongestBranchMonosaccharide()
    {
        return this.m_iLongestBranchMS;
    }
    
    public int getLongestBranchResidue()
    {
        return this.m_iLongestBranchResidue;
    }

    public void start(Sugar a_objSugar) throws GlycoVisitorException 
    {
        this.clear();
        this.m_objTraverser = this.getTraverser(this);
        this.m_objTraverser.traverseGraph(a_objSugar);
        // now we have to find the longest subbranch
        ArrayList<GlycoNode> t_aRoot;
        try
        {
            int t_iResidue = 0;
            int t_iMS = 0;
            GlycoNode t_objResidue;
            t_aRoot = a_objSugar.getRootNodes();
            Iterator<GlycoNode> t_objIterator = t_aRoot.iterator();
            while ( t_objIterator.hasNext() )
            {
                t_objResidue = t_objIterator.next();
                if ( this.m_hashSubMonosaccharideCount.containsKey(t_objResidue) )
                {
                    t_iMS = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                    if ( t_iMS > this.m_iLongestBranchMS )
                    {
                        this.m_iLongestBranchMS = t_iMS;
                    }
                }
                else
                {
                    throw new GlycoVisitorException("Critical fail : residue was not calculated.");
                }
                if ( this.m_hashSubResidueCount.containsKey(t_objResidue) )
                {
                    t_iResidue = this.m_hashSubResidueCount.get(t_objResidue);
                    if ( t_iResidue > this.m_iLongestBranchResidue )
                    {
                        this.m_iLongestBranchResidue = t_iResidue;
                    }
                }
                else
                {
                    throw new GlycoVisitorException("Critical fail : residue was not calculated.");
                }
            }
        } 
        catch (GlycoconjugateException e)
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.LEAVE )
        {
            this.m_hashSubResidueCount.put((GlycoNode)a_objCyclic,0);
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode)
     */
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.LEAVE )
        {
            int t_iNumber = 0;
            int t_iLongestResidue = 0;
            int t_iLongestMS = 0;
            GlycoNode t_objResidue;
            for (Iterator<GlycoNode> t_iterResidues = a_objUnvalidated.getChildNodes().iterator(); t_iterResidues.hasNext();)
            {
                t_objResidue = t_iterResidues.next();
                if ( this.m_hashSubResidueCount.containsKey(t_objResidue) )
                {
                    t_iNumber = this.m_hashSubResidueCount.get(t_objResidue);
                    if ( t_iLongestResidue < t_iNumber )
                    {
                        t_iLongestResidue = t_iNumber;
                    }
                }
                if ( this.m_hashSubMonosaccharideCount.containsKey(t_objResidue) )
                {
                    t_iNumber = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                    if ( t_iLongestMS < t_iNumber )
                    {
                        t_iLongestMS = t_iNumber;
                    }
                }
                if ( this.m_hashRepeatCount.containsKey(t_objResidue) )
                {
                    // residue is in the repeat chain
                    t_iNumber = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                    this.m_hashRepeatCount.put((GlycoNode)a_objUnvalidated,t_iNumber+1);
                }
            }
            t_iLongestResidue++;
            t_iLongestMS++;
            this.m_hashSubResidueCount.put((GlycoNode)a_objUnvalidated,t_iLongestResidue);
            this.m_hashSubMonosaccharideCount.put((GlycoNode)a_objUnvalidated,t_iLongestMS);
        }        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.GlycoEdge)
     */
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException
    {
        // nothing to do        
    }
    
    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Substituent)
     */
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.LEAVE )
        {
            int t_iNumber = 0;
            int t_iLongestResidue = 0;
            int t_iLongestMS = 0;
            GlycoNode t_objResidue;
            ArrayList<GlycoNode> t_aResidues = a_objSubstituent.getChildNodes();           
            for (Iterator<GlycoNode> t_iterResidues = t_aResidues.iterator(); t_iterResidues.hasNext();)
            {
                t_objResidue = t_iterResidues.next();
                if ( this.m_hashSubResidueCount.containsKey(t_objResidue) )
                {
                    t_iNumber = this.m_hashSubResidueCount.get(t_objResidue);
                    if ( t_iLongestResidue < t_iNumber )
                    {
                        t_iLongestResidue = t_iNumber;
                    }
                }
                if ( this.m_hashSubMonosaccharideCount.containsKey(t_objResidue) )
                {
                    t_iNumber = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                    if ( t_iLongestMS < t_iNumber )
                    {
                        t_iLongestMS = t_iNumber;
                    }
                }
                if ( this.m_hashRepeatCount.containsKey(t_objResidue) )
                {
                    // residue is in the repeat chain
                    t_iNumber = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                    this.m_hashRepeatCount.put((GlycoNode)a_objSubstituent,t_iNumber+1);
                }
            }
            if ( t_aResidues.size() > 0 )
            {
                this.m_hashSubMonosaccharideCount.put((GlycoNode)a_objSubstituent,t_iLongestMS+1);
            }
            this.m_hashSubResidueCount.put((GlycoNode)a_objSubstituent,t_iLongestResidue+1);
            this.m_hashSubMonosaccharideCount.put((GlycoNode)a_objSubstituent,t_iLongestMS);
        }        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.LEAVE )
        {
            int t_iNumber = 0;
            int t_iLongestResidue = 0;
            int t_iLongestMS = 0;
            GlycoNode t_objResidue;
            ArrayList<GlycoNode> t_aResidues = a_objAlternative.getChildNodes();           
            for (Iterator<GlycoNode> t_iterResidues = t_aResidues.iterator(); t_iterResidues.hasNext();)
            {
                t_objResidue = t_iterResidues.next();
                if ( this.m_hashSubResidueCount.containsKey(t_objResidue) )
                {
                    t_iNumber = this.m_hashSubResidueCount.get(t_objResidue);
                    if ( t_iLongestResidue < t_iNumber )
                    {
                        t_iLongestResidue = t_iNumber;
                    }
                }
                if ( this.m_hashSubMonosaccharideCount.containsKey(t_objResidue) )
                {
                    t_iNumber = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                    if ( t_iLongestMS < t_iNumber )
                    {
                        t_iLongestMS = t_iNumber;
                    }
                }
                if ( this.m_hashRepeatCount.containsKey(t_objResidue) )
                {
                    // residue is in the repeat chain
                    t_iNumber = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                    this.m_hashRepeatCount.put((GlycoNode)a_objAlternative,t_iNumber+1);
                }
            }
            t_iLongestResidue++;
            t_iLongestMS++;
            this.m_hashSubResidueCount.put((GlycoNode)a_objAlternative,t_iLongestResidue);
            this.m_hashSubMonosaccharideCount.put((GlycoNode)a_objAlternative,t_iLongestMS);
        }
    }

    public  void start(GlycoNode a_objNode) throws GlycoVisitorException
    {
        this.clear();
        this.m_objTraverser = this.getTraverser(this);
        this.m_objTraverser.traverse(a_objNode);
        // now we have to find the longest subbranch
        if ( this.m_hashSubMonosaccharideCount.containsKey(a_objNode) )
        {
            this.m_iLongestBranchMS = this.m_hashSubMonosaccharideCount.get(a_objNode);
        }
        else
        {
            throw new GlycoVisitorException("Critical fail : residue was not calculated.");
        }
        if ( this.m_hashSubResidueCount.containsKey(a_objNode) )
        {
            this.m_iLongestBranchResidue = this.m_hashSubResidueCount.get(a_objNode);
        }
        else
        {
            throw new GlycoVisitorException("Critical fail : residue was not calculated.");
        }
    }
    
    /**
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.SugarRepeatingUnit)
     */
    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.LEAVE )
        {
            int t_iNumber = 0;
            int t_iLongestResidue = 0;
            int t_iLongestMS = 0;
            int t_iLongestRepeat = 0;
            GlycoNode t_objResidue;
            ArrayList<GlycoNode> t_aResidues = a_objRepeate.getChildNodes();           
            for (Iterator<GlycoNode> t_iterResidues = t_aResidues.iterator(); t_iterResidues.hasNext();)
            {
                t_objResidue = t_iterResidues.next();
                if ( this.m_hashSubResidueCount.containsKey(t_objResidue) )
                {
                    t_iNumber = this.m_hashSubResidueCount.get(t_objResidue);
                    if ( t_iLongestResidue < t_iNumber )
                    {
                        t_iLongestResidue = t_iNumber;
                    }
                }
                if ( this.m_hashSubMonosaccharideCount.containsKey(t_objResidue) )
                {
                    t_iNumber = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                    if ( t_iLongestMS < t_iNumber )
                    {
                        t_iLongestMS = t_iNumber;
                    }
                }
                if ( this.m_hashRepeatCount.containsKey(t_objResidue) )
                {
                    t_iLongestRepeat = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                }
            }
            GlycoVisitorCountLongestBranch t_objVisitor = new GlycoVisitorCountLongestBranch(); 
            t_objVisitor.startRepeat(a_objRepeate);
            int t_iLongestBranchRepeat = t_objVisitor.getLongestBranchRepeat();
            if ( t_objVisitor.getLongestBranchMonosaccharide() > t_iLongestBranchRepeat + t_iLongestMS )
            {
              t_iLongestMS = t_objVisitor.getLongestBranchMonosaccharide();
            }
            else
            {
              t_iLongestMS = t_iLongestBranchRepeat + t_iLongestMS;
            }
            if ( t_objVisitor.getLongestBranchResidue() > t_iLongestBranchRepeat + t_iLongestResidue )
            {
              t_iLongestResidue = t_objVisitor.getLongestBranchResidue();
            }
            else
            {
              t_iLongestResidue = t_iLongestBranchRepeat + t_iLongestResidue;
            }
            if ( t_iLongestRepeat != 0 )
            {
                this.m_hashRepeatCount.put((GlycoNode)a_objRepeate,t_iLongestBranchRepeat + t_iLongestRepeat);
            }
            this.m_hashSubResidueCount.put((GlycoNode)a_objRepeate,t_iLongestResidue);
            this.m_hashSubMonosaccharideCount.put((GlycoNode)a_objRepeate,t_iLongestMS);
        }
    }

    /**
     * @param repeate
     */
    protected void startRepeat(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException
    {
        this.clear();
        this.m_objRepeat = a_objRepeat;
        // default values
        this.m_hashRepeatCount.put(a_objRepeat.getRepeatLinkage().getChild(),1);
        this.m_hashRepeatCount.put(a_objRepeat.getRepeatLinkage().getParent(),1);
        this.m_objTraverser = this.getTraverser(this);
        this.m_objTraverser.traverseGraph(a_objRepeat);
        // now we have to find the longest subbranch
        ArrayList<GlycoNode> t_aRoot;
        try
        {
            int t_iResidue = 0;
            int t_iMS = 0;
            GlycoNode t_objResidue;
            t_aRoot = a_objRepeat.getRootNodes();
            Iterator<GlycoNode> t_objIterator = t_aRoot.iterator();
            while ( t_objIterator.hasNext() )
            {
                t_objResidue = t_objIterator.next();
                if ( this.m_hashSubMonosaccharideCount.containsKey(t_objResidue) )
                {
                    t_iMS = this.m_hashSubMonosaccharideCount.get(t_objResidue);
                    if ( t_iMS > this.m_iLongestBranchMS )
                    {
                        this.m_iLongestBranchMS = t_iMS;
                    }
                }
                else
                {
                    throw new GlycoVisitorException("Critical fail : residue was not calculated.");
                }
                if ( this.m_hashSubResidueCount.containsKey(t_objResidue) )
                {
                    t_iResidue = this.m_hashSubResidueCount.get(t_objResidue);
                    if ( t_iResidue > this.m_iLongestBranchResidue )
                    {
                        this.m_iLongestBranchResidue = t_iResidue;
                    }
                }
                else
                {
                    throw new GlycoVisitorException("Critical fail : residue was not calculated.");
                }
            }
        } 
        catch (GlycoconjugateException e)
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }

    }

    /**
     * @return
     * @throws GlycoVisitorException 
     */
    private int getLongestBranchRepeat() throws GlycoVisitorException
    {
        if ( this.m_objRepeat == null )
        {
            return 0;
        }
        else
        {
            if ( this.m_hashRepeatCount.containsKey(this.m_objRepeat.getRepeatLinkage().getChild()) )
            {
                return this.m_hashRepeatCount.get(this.m_objRepeat.getRepeatLinkage().getChild());
            }
            else
            {
                throw new GlycoVisitorException("Critical fail : repeat was not calculated.");
            }
        }
    }
    
}
