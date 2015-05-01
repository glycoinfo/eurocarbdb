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
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.LinkageType;
import org.eurocarbdb.MolecularFramework.sugar.Modification;
import org.eurocarbdb.MolecularFramework.sugar.ModificationType;
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
* NOTE: THIS VISITOR HANDLES ONLY FULL CONNECTED MONOSACCHARIDES
* 
* @author rene
*
*/
public class GlycoVisitorMass implements GlycoVisitor
{
    protected boolean m_bMonoisotopic = true;
    protected double m_dMass = 0;
    protected MassComponents m_objMasses = new MassComponents();

    public double getMass()
    {
        return this.m_dMass;
    }

    public void setMonoisotopic(boolean a_bMonoisotpic)
    {
        this.m_bMonoisotopic = a_bMonoisotpic;
    }

    public boolean getMonoisotopic()
    {
        return this.m_bMonoisotopic;
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#clear()
     */
    public void clear()
    {
        this.m_dMass = 0;        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#getTraverser(org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor)
     */
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        return new GlycoTraverserTreeSingle(a_objVisitor);
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharide) throws GlycoVisitorException
    {
        // mass of the basetype
        double t_dMass = this.m_objMasses.getSuperclassMass(a_objMonosaccharide.getSuperclass(), this.m_bMonoisotopic);
        // mass of the modifications
        for (Iterator<Modification> t_iterModi = a_objMonosaccharide.getModification().iterator(); t_iterModi.hasNext();)
        {
            Modification t_objModi = t_iterModi.next();
            if ( t_objModi.getModificationType() != ModificationType.KETO || t_objModi.getPositionOne() != 1 )
            {
                t_dMass += this.m_objMasses.getModificationMass(t_objModi.getModificationType(), this.m_bMonoisotopic,t_objModi.getPositionOne());    
            }
        }
        // mass of the linkages around
        if ( a_objMonosaccharide.getParentEdge() != null )
        {
            for (Iterator<Linkage> t_iterLinkages = a_objMonosaccharide.getParentEdge().getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
            {
                LinkageType t_objLinkageType = t_iterLinkages.next().getChildLinkageType();
                t_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkageType,this.m_bMonoisotopic);
            }
        }
        for (Iterator<GlycoEdge> t_iterEdges = a_objMonosaccharide.getChildEdges().iterator(); t_iterEdges.hasNext();)
        {
            for (Iterator<Linkage> t_iterLinkages = t_iterEdges.next().getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
            {
                LinkageType t_objLinkageType = t_iterLinkages.next().getParentLinkageType();
                t_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkageType,this.m_bMonoisotopic);
            }
        }
        this.m_dMass += t_dMass;
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Substituent)
     */
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException
    {
        double t_dMass = this.m_objMasses.getSubstitutionsMass(a_objSubstituent.getSubstituentType(), this.m_bMonoisotopic);
        int t_iLinkageCount = 0;
        if ( a_objSubstituent.getParentEdge() != null )
        {
            for (Iterator<Linkage> t_iterLinkage = a_objSubstituent.getParentEdge().getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();)
            {
                t_iterLinkage.next();
                t_iLinkageCount++;
            }
        }
        for (Iterator<GlycoEdge> t_iterEdge = a_objSubstituent.getChildEdges().iterator(); t_iterEdge.hasNext();)
        {
            for (Iterator<Linkage> t_iterLinkage = t_iterEdge.next().getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();)
            {
                t_iterLinkage.next();
                t_iLinkageCount++;
            }            
        }
        SubstituentType t_objSubstType = a_objSubstituent.getSubstituentType();
        if ( t_iLinkageCount < t_objSubstType.getMinValence() )
        {
            throw new GlycoMassException("Error with minimum linkage count of substituent " + a_objSubstituent.getSubstituentType().getName() + "." );
        }
        if ( t_iLinkageCount > t_objSubstType.getMinValence() )
        {
            t_dMass += this.handleMultipleLinkedSubstituents(0, t_iLinkageCount, a_objSubstituent );
        }
        this.m_dMass += t_dMass;
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode)
     */
    public void visit(UnvalidatedGlycoNode unvalidated) throws GlycoVisitorException
    {
        throw new GlycoMassException("Mass calculation of Unvalidated residues (UnvalidatedGlycoNode) is not supported ." );        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.GlycoEdge)
     */
    public void visit(GlycoEdge linkage) throws GlycoVisitorException
    {
        // nothing to do
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative alternative) throws GlycoVisitorException
    {
        throw new GlycoMassException("Mass calculation of alternative SugarUnits is not supported ." );
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide)
     */
    public void visit(NonMonosaccharide residue) throws GlycoVisitorException
    {
        throw new GlycoMassException("Mass calculation of NonMonosaccharide " + residue.getName() + " is not supported ." );   
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat)
     */
    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException
    {
        if ( a_objRepeate.getMinRepeatCount() != a_objRepeate.getMaxRepeatCount() || a_objRepeate.getMinRepeatCount() == SugarUnitRepeat.UNKNOWN )
        {
            throw new GlycoMassException("Mass calculation of repeat units with not exactly defined repeat count is not possible.." );            
        }
        GlycoVisitorMass t_objMass = new GlycoVisitorMass();
        GlycoTraverser t_trav = this.getTraverser(t_objMass);
        t_trav.traverseGraph(a_objRepeate);
        this.m_dMass += t_objMass.getMass() * a_objRepeate.getMinRepeatCount();
        // internal repeat linkage
        double t_dLinkIn = this.specialLinkage(a_objRepeate.getRepeatLinkage(),true,a_objRepeate);
        double t_dLinkOut = this.specialLinkage(a_objRepeate.getRepeatLinkage(),false,a_objRepeate);
        this.m_dMass += (t_dLinkIn + t_dLinkOut) * (a_objRepeate.getMinRepeatCount() - 1);       
        // in and out linkages
        if ( a_objRepeate.getParentEdge() != null )
        {
            if ( a_objRepeate.getParentEdge().getGlycosidicLinkages().size() !=  a_objRepeate.getRepeatLinkage().getGlycosidicLinkages().size() )
            {
                throw new GlycoMassException("Repeat in linkage and repeat linkage weight does not match." );
            }
            this.m_dMass += t_dLinkIn;
        }
        for (Iterator<GlycoEdge> t_iterEdges = a_objRepeate.getChildEdges().iterator(); t_iterEdges.hasNext();)
        {
            if ( t_iterEdges.next().getGlycosidicLinkages().size() !=  a_objRepeate.getRepeatLinkage().getGlycosidicLinkages().size() )
            {
                throw new GlycoMassException("Repeat out linkage and repeat linkage weight does not match." );
            }
            this.m_dMass += t_dLinkOut;
        }
        // underdetermined units        
        for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objRepeate.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();) 
        {
            UnderdeterminedSubTree t_objUTree = t_iterUnder.next();
            if ( t_objUTree.getProbabilityLower() < 100 )
            {
                throw new GlycoMassException("Mass calculation for stoichometric distribution is not possible.");
            }
            else
            {
                t_objMass = new GlycoVisitorMass();
                t_trav = this.getTraverser(t_objMass);
                t_trav.traverseGraph(t_objUTree);
                this.m_dMass += t_objMass.getMass();
                // incoming linkage
                for (Iterator<Linkage> t_iterLinkage = t_objUTree.getConnection().getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();)
                {
                    Linkage t_objLinkage= t_iterLinkage.next();
                    if ( t_objLinkage.getParentLinkageType() == LinkageType.NONMONOSACCHARID )
                    {
                        if ( this.isHomogenSubst( t_objUTree.getParents() ) )
                        {
                            this.m_dMass += this.checkAndCalculateSubstituent(t_objUTree.getParents(),t_objUTree.getConnection());
                        }
                        else
                        {
                            throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit is not possible." );
                        }
                    }
                    else
                    {
                        this.m_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkage.getParentLinkageType(),this.m_bMonoisotopic);
                    }
                    if ( t_objLinkage.getChildLinkageType() == LinkageType.NONMONOSACCHARID )
                    {
                        try
                        {
                            if ( this.isHomogenSubst( t_objUTree.getRootNodes() ) )
                            {
                                this.m_dMass += this.checkAndCalculateSubstituent(t_objUTree.getRootNodes(),t_objUTree.getConnection());
                            }
                            else
                            {
                                throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit is not possible." );
                            }
                        } 
                        catch (GlycoconjugateException e)
                        {
                            throw new GlycoVisitorException(e.getMessage(),e);
                        }
                    }
                    else
                    {
                        this.m_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkage.getChildLinkageType(),this.m_bMonoisotopic);
                    }
                }                 
            }
        }
    }

    /**
     * @param repeatLinkage
     * @throws GlycoVisitorException 
     */
    private double specialLinkage(GlycoEdge a_objRepeatEdge,boolean a_bIn,SugarUnitRepeat a_objRepeat) throws GlycoVisitorException
    {
        double t_dMass = 0;
        if ( a_bIn )
        {
            for (Iterator<Linkage> t_iterLinkage = a_objRepeatEdge.getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();) 
            {
                Linkage t_objLinkage = t_iterLinkage.next();
                if ( t_objLinkage.getChildLinkageType() == LinkageType.NONMONOSACCHARID )
                {
                    GlycoVisitorRepeatLinkType t_visStart = new GlycoVisitorRepeatLinkType();
                    a_objRepeat.accept(t_visStart);
                    if ( t_visStart.getEdge() == null )
                    {
                        throw new GlycoMassException("Mass calculation of repeat is not possible." );
                    }
                    else
                    {
                        if ( a_objRepeatEdge.getGlycosidicLinkages().size() != t_visStart.getEdge().getGlycosidicLinkages().size() )
                        {
                            throw new GlycoMassException("Repeat linkage and inner repeat linkage weight does not match." );
                        }
                        else
                        {
                            if ( this.isHomogenSubst( t_visStart.getStartNodes() ) )
                            {
                                t_dMass += this.checkAndCalculateSubstituent(t_visStart.getStartNodes(),t_visStart.getEdge());
                            }
                            else
                            {
                                throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit is not possible." );
                            }
                        }
                    }
                }
                else
                {
                    t_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkage.getChildLinkageType(),this.m_bMonoisotopic);
                }
            }
        }
        else
        {
            for (Iterator<Linkage> t_iterLinkage = a_objRepeatEdge.getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();) 
            {
                Linkage t_objLinkage = t_iterLinkage.next();
                if ( t_objLinkage.getParentLinkageType() == LinkageType.NONMONOSACCHARID )
                {
                    GlycoVisitorRepeatLinkType t_visStart = new GlycoVisitorRepeatLinkType();
                    t_visStart.setRepeatIn(false);
                    a_objRepeat.accept(t_visStart);
                    if ( t_visStart.getEdge() == null )
                    {
                        throw new GlycoMassException("Mass calculation of repeat is not possible." );
                    }
                    else
                    {
                        if ( a_objRepeatEdge.getGlycosidicLinkages().size() != t_visStart.getEdge().getGlycosidicLinkages().size() )
                        {
                            throw new GlycoMassException("Repeat linkage and inner repeat linkage weight does not match." );
                        }
                        else
                        {
                            if ( this.isHomogenSubst( t_visStart.getStartNodes() ) )
                            {
                                t_dMass += this.checkAndCalculateSubstituent(t_visStart.getStartNodes(),t_visStart.getEdge());
                            }
                            else
                            {
                                throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit is not possible." );
                            }
                        }
                    }
                }
                else
                {
                    t_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkage.getParentLinkageType(),this.m_bMonoisotopic);
                }
            }
        }
        return t_dMass;
    }
    

    /*
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#start(org.eurocarbdb.MolecularFramework.sugar.Sugar)
     */
    public void start(Sugar a_objSugar) throws GlycoVisitorException
    {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        // underdetermined units        
        for (Iterator<UnderdeterminedSubTree> t_iterUnder = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterUnder.hasNext();) 
        {
            UnderdeterminedSubTree t_objUTree = t_iterUnder.next();
            if ( t_objUTree.getProbabilityLower() < 100 )
            {
                throw new GlycoMassException("Mass calculation for stoichometric distribution is not possible.");
            }
            else
            {
                GlycoVisitorMass t_objMass = new GlycoVisitorMass();
                GlycoTraverser t_trav = this.getTraverser(t_objMass);
                t_trav.traverseGraph(t_objUTree);
                this.m_dMass += t_objMass.getMass();
                // incoming linkage
                for (Iterator<Linkage> t_iterLinkage = t_objUTree.getConnection().getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();)
                {
                    Linkage t_objLinkage= t_iterLinkage.next();
                    if ( t_objLinkage.getParentLinkageType() == LinkageType.NONMONOSACCHARID )
                    {
                        if ( this.isHomogenSubst( t_objUTree.getParents() ) )
                        {
                            this.m_dMass += this.checkAndCalculateSubstituent(t_objUTree.getParents(),t_objUTree.getConnection());
                        }
                        else
                        {
                            throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit is not possible." );
                        }
                    }
                    else
                    {
                        this.m_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkage.getParentLinkageType(),this.m_bMonoisotopic);
                    }
                    if ( t_objLinkage.getChildLinkageType() == LinkageType.NONMONOSACCHARID )
                    {
                        try
                        {
                            if ( this.isHomogenSubst( t_objUTree.getRootNodes() ) )
                            {
                                this.m_dMass += this.checkAndCalculateSubstituent(t_objUTree.getRootNodes(),t_objUTree.getConnection());
                            }
                            else
                            {
                                throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit is not possible." );
                            }
                        } 
                        catch (GlycoconjugateException e)
                        {
                            throw new GlycoVisitorException(e.getMessage(),e);
                        }
                    }
                    else
                    {
                        this.m_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkage.getChildLinkageType(),this.m_bMonoisotopic);
                    }
                }               
            }
        }
    }

    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException
    {
        if ( a_objCyclic.getParentEdge() != null )
        {
            for (Iterator<Linkage> t_iterLinkages = a_objCyclic.getParentEdge().getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
            {
                // for each linkage
                LinkageType t_objLinkageType = t_iterLinkages.next().getChildLinkageType();
                if ( t_objLinkageType == LinkageType.NONMONOSACCHARID )
                {
                    GlycoVisitorRepeatLinkType t_visStart = new GlycoVisitorRepeatLinkType();
                    a_objCyclic.getCyclicStart().accept(t_visStart);
                    if ( t_visStart.getEdge() == null )
                    {
                        throw new GlycoMassException("Mass calculation of cyclic start is not possible." );
                    }
                    else
                    {
                        if ( a_objCyclic.getParentEdge().getGlycosidicLinkages().size() != t_visStart.getEdge().getGlycosidicLinkages().size() )
                        {
                            throw new GlycoMassException("Cyclic linkage and repeat linkage weight does not match." );
                        }
                        else
                        {
                            if ( this.isHomogenSubst( t_visStart.getStartNodes() ) )
                            {
                                this.m_dMass += this.checkAndCalculateSubstituent(t_visStart.getStartNodes(),t_visStart.getEdge());
                            }
                            else
                            {
                                throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit is not possible." );
                            }
                        }
                    }
                }
                else
                {
                    this.m_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkageType,this.m_bMonoisotopic);
                }
            }
        }
        else
        {
            throw new GlycoMassException("Mass calculation of unconnected cylcic unit is not possible." );            
        }
    }

    /**
     * @param startNodes
     * @param edge
     * @return
     * @throws GlycoVisitorException 
     */
    private double checkAndCalculateSubstituent( ArrayList<GlycoNode> a_aStartNodes, GlycoEdge a_objEdge) throws GlycoVisitorException 
    {
        double t_dMass = -1;
        double t_dMassTemp = -1;
        int t_iLinkageCount = 0;
        GlycoVisitorNodeType t_visType = new GlycoVisitorNodeType();
        for (Iterator<GlycoNode> t_iterNodes = a_aStartNodes.iterator(); t_iterNodes.hasNext();)
        {
             GlycoNode t_objNode = t_iterNodes.next();
             if ( t_objNode.getParentEdge() != null )
             {
                 t_iLinkageCount += t_objNode.getParentEdge().getGlycosidicLinkages().size(); 
             }
             for (Iterator<GlycoEdge> t_iterLinkage = t_objNode.getChildEdges().iterator(); t_iterLinkage.hasNext();)
             {
                 t_iLinkageCount += t_iterLinkage.next().getGlycosidicLinkages().size();
                
             }
             t_dMassTemp = this.handleMultipleLinkedSubstituents(t_iLinkageCount, a_objEdge.getGlycosidicLinkages().size(),t_visType.getSubstituent(t_objNode));
             if ( t_dMass == -1 )
             {
                 t_dMass = t_dMassTemp;
             }
             else
             {
                 if ( t_dMass != t_dMassTemp )
                 {
                     throw new GlycoMassException("Mass calculation of heterogen repeat substituents is not possible." );
                 }
             }
        }
        return t_dMass;
    }

    private boolean isHomogenSubst(ArrayList<GlycoNode> startNodes) throws GlycoVisitorException 
    {
        boolean t_bOther = false;
        GlycoVisitorNodeType t_visType = new GlycoVisitorNodeType();
        for (Iterator<GlycoNode> t_iter = startNodes.iterator(); t_iter.hasNext();) 
        {
            if ( !t_visType.isSubstituent(t_iter.next()) )
            {
                t_bOther = true;
            }
        }
        return !t_bOther;
    }

    private double handleMultipleLinkedSubstituents(int t_iLinkageCalculated, int t_iLinkageCountNew , Substituent a_objSubstituent) throws GlycoMassException
    {
        SubstituentType t_objSubstType = a_objSubstituent.getSubstituentType();
        if ( t_objSubstType == SubstituentType.PHOSPHATE )
        {
            double t_dIncMass = 0;
            if ( this.m_bMonoisotopic )
            {
                t_dIncMass = 17.0027396541;
            }
            else
            {
                t_dIncMass = 17.00734568218410;
            }
            if ( (t_iLinkageCountNew + t_iLinkageCalculated) > t_objSubstType.getMaxValence() )
            {
                throw new GlycoMassException("Error with max. linkage count) of substituent phosphate." );
            }
            return (1-(t_iLinkageCalculated+t_iLinkageCountNew)) * t_dIncMass;  
        }
        throw new GlycoMassException("Error with linkage count of substituent " + a_objSubstituent.getSubstituentType().getName() + "." );        
    }
}
