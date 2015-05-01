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
package org.eurocarbdb.MolecularFramework.util.analytical.mass;

import java.util.ArrayList;
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
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

/**
* @author Logan
*
*/
public class GlycoVisitorRepeatLinkType implements GlycoVisitor 
{
    private GlycoEdge m_objEdge = null;
    private ArrayList<GlycoNode> m_aNodes = new ArrayList<GlycoNode>();
    private boolean m_bRepeatIn = true;

    public void clear() 
    {
        this.m_objEdge = null;
        this.m_aNodes = new ArrayList<GlycoNode>();
    }

    public GlycoTraverser getTraverser(GlycoVisitor visitor) throws GlycoVisitorException 
    {
        return null;
    }

    public void start(Sugar sugar) throws GlycoVisitorException 
    {
        this.clear();
    }

    public void visit(Monosaccharide a_objMonosaccharide) throws GlycoVisitorException 
    {
        this.clear();
    }

    public void visit(NonMonosaccharide residue) throws GlycoVisitorException 
    {
        this.clear();
    }

    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException 
    {
        GlycoVisitorNodeType t_visType = new GlycoVisitorNodeType();
        boolean t_bSimple = false;
        boolean t_bComplex = false;
        try 
        {
            if ( this.m_bRepeatIn )
            {
                for (Iterator<GlycoNode> t_iterNodes = a_objRepeat.getRootNodes().iterator(); t_iterNodes.hasNext();) 
                {
                    GlycoNode t_objNode = t_iterNodes.next();
                    if ( t_visType.isMonosaccharide(t_objNode) )
                    {
                        t_bSimple = true;
                    }
                    else if ( t_visType.isSubstituent(t_objNode) )
                    {
                        t_bSimple = true;
                    }
                    else if ( t_visType.isSugarUnitRepeat( t_objNode) )
                    {
                        t_bComplex = true;
                        t_visType.getSugarUnitRepeat( t_objNode).accept(this);
                    }
                }
                if ( t_bSimple && t_bComplex )
                {
                    this.clear();
                }
                else if ( t_bSimple )
                {
                    this.m_aNodes = a_objRepeat.getRootNodes();
                    this.m_objEdge = a_objRepeat.getRepeatLinkage();
                }
            }
            else
            {
                if ( a_objRepeat.getRepeatLinkage().getParent() == null )
                {
                    for (Iterator<GlycoNode> t_iterNodes = a_objRepeat.getNodes().iterator(); t_iterNodes.hasNext();) 
                    {
                        GlycoNode t_objNode = t_iterNodes.next();
                        if ( t_visType.isMonosaccharide(t_objNode) )
                        {
                            t_bSimple = true;
                        }
                        else if ( t_visType.isSubstituent(t_objNode) )
                        {
                            t_bSimple = true;
                        }
                        else if ( t_visType.isSugarUnitRepeat( t_objNode) )
                        {
                            t_bComplex = true;
                            t_visType.getSugarUnitRepeat( t_objNode).accept(this);
                        }
                    }
                    if ( t_bSimple && t_bComplex )
                    {
                        this.clear();
                    }
                    else if ( t_bSimple )
                    {
                        this.m_aNodes = a_objRepeat.getNodes();
                        this.m_objEdge = a_objRepeat.getRepeatLinkage();
                    }
                }
                else
                {
                    GlycoNode t_objNode = a_objRepeat.getRepeatLinkage().getParent();
                    if ( t_visType.isMonosaccharide(t_objNode) )
                    {
                        this.m_aNodes.clear();
                        this.m_aNodes.add(t_objNode);
                        this.m_objEdge = a_objRepeat.getRepeatLinkage();
                    }
                    else if ( t_visType.isSubstituent(t_objNode) )
                    {
                        this.m_aNodes.clear();
                        this.m_aNodes.add(t_objNode);
                        this.m_objEdge = a_objRepeat.getRepeatLinkage();
                    }
                    else if ( t_visType.isSugarUnitRepeat( t_objNode) )
                    {
                        t_visType.getSugarUnitRepeat(t_objNode).accept(this);
                    }
                }
            }
        } 
        catch (GlycoconjugateException e) 
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
    }

    public void visit(Substituent substituent) throws GlycoVisitorException 
    {
        this.clear();        
    }

    public void visit(SugarUnitCyclic cyclic) throws GlycoVisitorException 
    {
        this.clear();
    }

    public void visit(SugarUnitAlternative alternative) throws GlycoVisitorException 
    {
        this.clear();
    }

    public void visit(UnvalidatedGlycoNode unvalidated) throws GlycoVisitorException 
    {
        this.clear();        
    }

    public void visit(GlycoEdge linkage) throws GlycoVisitorException 
    {
        this.clear();        
    }

    public GlycoEdge getEdge() 
    {
        return this.m_objEdge;
    }

    public ArrayList<GlycoNode> getStartNodes() 
    {
        return this.m_aNodes;
    }

    /**
     * @param b
     */
    public void setRepeatIn(boolean b) 
    {
        this.m_bRepeatIn = b;        
    }

}
