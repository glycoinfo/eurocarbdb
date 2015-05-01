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
package org.eurocarbdb.MolecularFramework.io.Linucs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
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

/**
* @author Logan
*
*/
public class GlycoVisitorExportRepeat implements GlycoVisitor
{
    private String m_strBefore = "";
    private String m_strAfter = "";
    private String m_strCode = ""; 
    private GlycoTraverserLinucs m_objTraverser = null;
    private GlycoNode m_objResidueEnd = null;
    private GlycoNode m_objResidueStart = null;
    private ArrayList<Integer> m_aParentPos = null;
    private ArrayList<Integer> m_aChildPos = null;
    private boolean m_bRepeatBefore = false;
    private HashMap<SugarUnitRepeat,String> m_hashAfterText = new HashMap<SugarUnitRepeat,String>(); 
    private boolean m_bFirstResidue = true;

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("Seems to be a wrong namespace. Linucs exporter does not support monosaccharide objects. Use UnvalidatedGlycoNode.");
    }

    /**
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharides)
     */
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("Seems to be a wrong namespace. Linucs exporter does not support nonmonosaccharide objects. Use UnvalidatedGlycoNode.");        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Substituent)
     */
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("Seems to be a wrong namespace. Linucs exporter does not support substituent objects. Use UnvalidatedGlycoNode.");        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("Linucs exporter does not support alternative residue objects.");        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#start(org.eurocarbdb.MolecularFramework.sugar.Sugar)
     */
    public void start(Sugar a_objSugar) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("Not possible to visit sugar, only repeat units.");
    }

    public void start(SugarUnitRepeat a_objRepeat, GlycoEdge a_objOut) throws GlycoVisitorException
    {
        this.clear();
        this.m_objResidueStart = a_objRepeat.getRepeatLinkage().getChild();
        this.m_objResidueEnd = a_objRepeat.getRepeatLinkage().getParent();
        // nur ein residue 
        ArrayList<GlycoNode> t_aRoots;
        try 
        {
            t_aRoots = a_objRepeat.getRootNodes();
            if ( t_aRoots.size() != 1 )
            {
                throw new GlycoVisitorException("Linucs exporter does not support fragmented repeat units.");
            }
            // muss gleich start sein
            if ( t_aRoots.get(0) != this.m_objResidueStart )
            {
                throw new GlycoVisitorException("Invalide repeat unit (start and root residue does not match).");
            }
        } 
        catch (GlycoconjugateException e) 
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
        this.m_objTraverser = this.getTraverser(this);
        this.m_objTraverser.traverseGraph(a_objRepeat,a_objOut,this);
        this.m_strAfter = this.m_strCode;
    }
    /**
     * @throws GlycoVisitorException 
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#getTraverser(org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor)
     */
    public GlycoTraverserLinucs getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {
        return new GlycoTraverserLinucs(this);
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#clear()
     */
    public void clear() 
    {
        this.m_strBefore = "";
        this.m_strAfter = "";
        this.m_strCode = ""; 
        this.m_objTraverser = null;
        this.m_objResidueEnd = null;
        this.m_objResidueStart = null;
        this.m_bRepeatBefore = false;
        this.m_hashAfterText.clear();
        this.m_bFirstResidue = true;
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode)
     */
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            if ( this.m_bFirstResidue )
            {
                this.m_strCode += "[" + a_objUnvalidated.getName() + "]{";
                this.m_bFirstResidue = false;
            }
            else
            {
                this.m_strCode += "[(";
                if ( this.m_bRepeatBefore )
                {
                    this.m_strCode += "<";
                    this.m_bRepeatBefore = false;
                }
                this.writeLinkage(this.m_aParentPos);
                this.m_strCode += "+";
                this.writeLinkage(this.m_aChildPos);
                this.m_strCode += ")][" + a_objUnvalidated.getName() + "]{";
            }
        }
        else
        {
            // LEAVE
            this.m_strCode += "}";
        }        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.GlycoEdge)
     */
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException 
    {
        if ( a_objLinkage.getGlycosidicLinkages().size() != 1 )
        {
            throw new GlycoVisitorException("Linucs does not support multiple connected residues.");
        }
        for (Iterator<Linkage> t_iterLinkages = a_objLinkage.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
        {
            Linkage t_objLink = t_iterLinkages.next();
            this.m_aParentPos = t_objLink.getParentLinkages();
            this.m_aChildPos = t_objLink.getChildLinkages();
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException 
    {
        throw new GlycoVisitorException("Linucs does not support cyclic part in repeat units.");
    }
    
    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat)
     */
    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            if ( a_objRepeat == this.m_objResidueStart )
            {
                throw new GlycoVisitorException("LINUCS does not support nested repeat unit at the start residue.");
            }
            if ( a_objRepeat == this.m_objResidueEnd )
            {
                throw new GlycoVisitorException("LINUCS does not support nested repeat unit at the end residue.");
            }
            this.m_strCode += "[(";
            if ( this.m_bRepeatBefore )
            {
                this.m_strCode += "<";
                this.m_bRepeatBefore = false;
            }
            this.writeLinkage(this.m_aParentPos);
            this.m_strCode += "+";
            this.writeLinkage(this.m_aChildPos);
            if ( a_objRepeat.getMinRepeatCount() != a_objRepeat.getMaxRepeatCount() )
            {
                throw new GlycoVisitorException("LINUCS does not support repeat unit with different min and max count.");                
            }
            if ( a_objRepeat.getMaxRepeatCount() == SugarUnitRepeat.UNKNOWN )
            {
                this.m_strCode += ">N)]";
            }
            else
            {
                this.m_strCode += ">" + String.format("%d",a_objRepeat.getMaxRepeatCount()) + ")]";
            }
            if ( a_objRepeat.getRepeatLinkage().getGlycosidicLinkages().size() != 1 )
            {
                throw new GlycoVisitorException("LINUCS does not support multivalent repeat units.");
            }
            Collections.sort( a_objRepeat.getRepeatLinkage().getGlycosidicLinkages().get(0).getChildLinkages() );
            if ( !this.equalPositions(
                    a_objRepeat.getRepeatLinkage().getGlycosidicLinkages().get(0).getChildLinkages(),
                    a_objRepeat.getParentEdge().getGlycosidicLinkages().get(0).getChildLinkages()) 
                )
            {
                throw new GlycoVisitorException("LINUCS can not store differen child positions for internal and incoming repeat linkages");
            }
            GlycoVisitorExportRepeat t_objExporter = new GlycoVisitorExportRepeat();
            t_objExporter.start( a_objRepeat , a_objRepeat.getRepeatLinkage());
            if ( a_objRepeat.getChildEdges().size() != 1 )
            {
                throw new GlycoVisitorException("LINUCS can not store repeat units with more or less than one childbranch.");
            }
            Collections.sort( a_objRepeat.getRepeatLinkage().getGlycosidicLinkages().get(0).getParentLinkages() );
            if ( !this.equalPositions(
                    a_objRepeat.getRepeatLinkage().getGlycosidicLinkages().get(0).getParentLinkages(),
                    a_objRepeat.getChildEdges().get(0).getGlycosidicLinkages().get(0).getParentLinkages()) 
                )
            {
                throw new GlycoVisitorException("LINUCS can not store differen parent positions for internal and outgoing repeat linkages");
            }
            this.m_bRepeatBefore = true;
            this.m_strCode += t_objExporter.getBeforeCode();
            this.m_hashAfterText.put(a_objRepeat,t_objExporter.getAfterCode());
        }
        else
        {
            // leave 
            this.m_strCode += this.m_hashAfterText.get(a_objRepeat);
        }        
    }
    
    /**
     * @param parentLinkages
     * @param parentLinkages2
     * @return
     */
    private boolean equalPositions(ArrayList<Integer> a_aLinkages, ArrayList<Integer> a_aLinkages2) 
    {
        if ( a_aLinkages.size() == a_aLinkages2.size() )
        {
            for (int i = 0; i < a_aLinkages.size(); i++) 
            {
                if ( a_aLinkages.get(i) != a_aLinkages2.get(i) )
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void writeLinkage(ArrayList<Integer> a_aPositions) throws GlycoVisitorException
    {
        boolean t_bUnknown = false;
        if ( a_aPositions.size() < 1)
        {
            throw new GlycoVisitorException("Invalide linkage, no parent positions given.");
        }
        Iterator<Integer> t_iterPositions = a_aPositions.iterator();
        Integer t_iPosition = t_iterPositions.next();
        if ( t_iPosition == Linkage.UNKNOWN_POSITION )
        {
            t_bUnknown = true;
            this.m_strCode += "?";
        }        
        else
        {
            this.m_strCode += t_iPosition.toString();
        }

        while ( t_iterPositions.hasNext() )
        {
            t_iPosition = t_iterPositions.next();
            if ( t_iPosition == Linkage.UNKNOWN_POSITION || t_bUnknown )
            {
                throw new GlycoVisitorException("Invalide linkage, unknown and distinct positions mixed.");
            }   
            this.m_strCode += "/" + t_iPosition.toString();
        }
    }
    
    public String getBeforeCode()
    {
        return this.m_strBefore;
    }
    
    public String getAfterCode()
    {
        return this.m_strAfter;
    }
    
    public void switchStrings()
    {
        this.m_strBefore = this.m_strCode;
        this.m_strCode = "";
    }
}
