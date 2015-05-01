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
package org.eurocarbdb.MolecularFramework.util.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
import org.eurocarbdb.MolecularFramework.sugar.ModificationType;
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
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserValdidation;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

/** 
* Validation for connected Sugars. Validates each residue and his direct connections.
* 
* TODO: 
* - test linkage positions for substitutens (MSDB)
* - test/warn if a substituent typicly do not have a child linkage
* - uncertain terminal residues, structure comparision (2x same uncertain structure + 2x same two attach positions = destinct structure)
* - getSimpleGlycoNode returns null for Alternative nodes (which means in that case no further testing)
* - test if minimum constrain for alternative unit is given
* - test for hierarchy of non sharp information
*/
public class GlycoVisitorValidation implements GlycoVisitor 
{
    private ArrayList<String> m_aErrorList = new ArrayList<String>(); 
    private ArrayList<String> m_aWarningList = new ArrayList<String>();
    private GlycoGraph m_objGlycoGraph = null;
    private ArrayList<GlycoEdge> m_aEdge = new ArrayList<GlycoEdge>();
    private GlycoVisitorNodeType m_visNodeType = new GlycoVisitorNodeType(); 

    public ArrayList<String> getErrors()
    {
        return this.m_aErrorList;
    }

    public ArrayList<String> getWarnings()
    {
        return this.m_aWarningList;
    }

    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {
        return new GlycoTraverserValdidation(a_objVisitor);
    }

    /**
     * - must contain at least one linkage
     * - each linkage must have at least one parent and one child linkage position
     * - linkage position may not be duplicated 
     * - UNKNOWN_POSITION and another position value are not allowed in the same linkage
     * - parent and child may not be null and must be part of he GlycoGraph (m_objGlycoGraph)
     */
    public void visit(GlycoEdge a_objEdge) throws GlycoVisitorException 
    {
        if ( !this.m_aEdge.contains(a_objEdge) )
        {
            // Edge was not checked before.
            if ( a_objEdge.getChild() == null || a_objEdge.getParent() == null )
            {
                this.m_aErrorList.add("Child or parent are null in Edge.");
            }
            if ( !this.m_objGlycoGraph.containsNode(a_objEdge.getChild()) )
            {
                this.m_aErrorList.add("Child node of an linkage is not part of the same sugar block.");
            }
            if ( !this.m_objGlycoGraph.containsNode(a_objEdge.getParent()) )
            {
                this.m_aErrorList.add("Parent node of an linkage is not part of the same sugar block.");
            }
            this.testLinkageArray(a_objEdge);
            GlycoNode t_objNode = a_objEdge.getChild();
            if ( t_objNode.getParentEdge() != a_objEdge )
            {
                this.m_aErrorList.add("Child residue in edge does not have this edge as parent.");
            }
            t_objNode = a_objEdge.getParent();
            if ( !t_objNode.getChildEdges().contains(a_objEdge) )
            {
                this.m_aErrorList.add("Parent residue in edge does not have this edge as child.");
            }
            this.m_aEdge.add(a_objEdge);
        }
    }

    /**
     * 
     */
    private void testLinkageArray(GlycoEdge a_objEdge)
    {
        if ( a_objEdge.getGlycosidicLinkages().size() == 0 )
        {
            this.m_aErrorList.add("GlycoEdge contains no Linkages.");
        }
        for (Iterator<Linkage> t_iterLinkages = a_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
        {
            Linkage t_objLinkage = t_iterLinkages.next();
            // child side
            if ( t_objLinkage.getChildLinkages().size() == 0 )
            {
                this.m_aErrorList.add("No child linkage position given in GlycoEdge.");
            }
            ArrayList<Integer> t_aPositions = new ArrayList<Integer>();
            Integer t_iPosition = null;
            for (Iterator<Integer> t_iterPosition = t_objLinkage.getChildLinkages().iterator(); t_iterPosition.hasNext();)
            {
                t_iPosition = t_iterPosition.next();
                if ( t_aPositions.contains(t_iPosition) )
                {
                    this.m_aErrorList.add("Duplicated child linkage position in GlycoEdge.");
                }
                t_aPositions.add(t_iPosition);
            }
            t_aPositions = t_objLinkage.getChildLinkages();
            if ( t_aPositions.size() > 1 && t_aPositions.contains(Linkage.UNKNOWN_POSITION) )
            {
                this.m_aErrorList.add("Unknown and defined positions in one GlycoEdge.");
            }
            // parent side
            if ( t_objLinkage.getParentLinkages().size() == 0 )
            {
                this.m_aErrorList.add("No parent linkage position given in GlycoEdge.");
            }
            t_aPositions = new ArrayList<Integer>();
            t_iPosition = null;
            for (Iterator<Integer> t_iterPosition = t_objLinkage.getParentLinkages().iterator(); t_iterPosition.hasNext();)
            {
                t_iPosition = t_iterPosition.next();
                if ( t_aPositions.contains(t_iPosition) )
                {
                    this.m_aErrorList.add("Duplicated parent linkage position in GlycoEdge.");
                }
                t_aPositions.add(t_iPosition);
            }
            t_aPositions = t_objLinkage.getParentLinkages();
            if ( t_aPositions.size() > 1 && t_aPositions.contains(Linkage.UNKNOWN_POSITION) )
            {
                this.m_aErrorList.add("Unknown and defined positions in one GlycoEdge.");
            }
            if ( t_objLinkage.getChildLinkageType() == null || t_objLinkage.getParentLinkageType() == null )
            {
                this.m_aErrorList.add("Linkagetype is not set.");
            }
        }
    }

    /**
     * - test if all Linkagetypes around the NonMS are set to LinkageType.NONMONOSACCHARID
     * - warning is not allowed for unique GlycoCT 
     */
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException 
    {
        this.m_aWarningList.add("NonMonosaccharides are not allowed in unique GlycoCT.");
        GlycoEdge t_objEdge = a_objResidue.getParentEdge();
        if ( t_objEdge != null )
        {
            for (Iterator<Linkage> t_iterLinkages = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
            {
                Linkage t_objLinkage = t_iterLinkages.next();
                if ( t_objLinkage.getChildLinkageType() != LinkageType.NONMONOSACCHARID )
                {
                    this.m_aErrorList.add("LinkageType for NonMonosaccharide must be LinkageType.NONMONOSACCHARID.");
                }
            }
        }
        for (Iterator<GlycoEdge> t_iterEdges = a_objResidue.getChildEdges().iterator(); t_iterEdges.hasNext();)
        {
            t_objEdge = t_iterEdges.next();  
            for (Iterator<Linkage> t_iterLinkages = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
            {
                Linkage t_objLinkage = t_iterLinkages.next();
                if ( t_objLinkage.getParentLinkageType() != LinkageType.NONMONOSACCHARID )
                {
                    this.m_aErrorList.add("LinkageType for NonMonosaccharide must be LinkageType.NONMONOSACCHARID.");
                }
            }
        }
    }

    /**
     * Simple : Not allowed!
     */
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException 
    {
        this.m_aErrorList.add("UnvalidatedGlycoNode in Sugar.");
    }

    /**
     * - test if all Linkagetypes around the substituents are set to LinkageType.NONMONOSACCHARID
     * - test if count of linkages match with minimal count in SubstituentType
     * - epoxy, anhydro, lacton only 2 linkage to the same monosaccharide and no child linkages
     * - subst - subst linkage forbidden (beside repeat and alternative)
     */
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException 
    {
        GlycoEdge t_objEdge = a_objSubstituent.getParentEdge();
        int t_iLinkageCount = 0;
        if ( t_objEdge != null )
        {
            for (Iterator<Linkage > t_iterLinkage = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();)
            {
                t_iLinkageCount++;
                Linkage t_objLinkage = t_iterLinkage.next();

                if (!a_objSubstituent.getSubstituentType().getComplexType())
                {
                    for (Iterator <Integer> iter = t_objLinkage.getChildLinkages().iterator(); iter.hasNext();) 
                    {
                        Integer element =  iter.next();
                        if (!element.equals(1)){
                            this.m_aErrorList.add("For this substituent "+a_objSubstituent.getSubstituentType().getName()+" linkage pos must be 1.");
                        }
                    }
                }
                else
                {
                    for (Iterator <Integer> iter = t_objLinkage.getChildLinkages().iterator(); iter.hasNext();) 
                    {
                        int element =  iter.next();
                        if ( element < 1 && element != Linkage.UNKNOWN_POSITION )
                        {
                            this.m_aErrorList.add("For this substituent "+a_objSubstituent.getSubstituentType().getName()+" linkage pos must be larger than 0.");
                        }
                    }
                }
                if ( t_objLinkage.getChildLinkageType() != LinkageType.NONMONOSACCHARID )
                {
                    this.m_aErrorList.add("LinkageType for substituent must be LinkageType.NONMONOSACCHARID.");
                }
            }
        }
        for (Iterator<GlycoEdge> t_iterEdges = a_objSubstituent.getChildEdges().iterator(); t_iterEdges.hasNext();)
        {
            t_objEdge = t_iterEdges.next();
            for (Iterator<Linkage > t_iterLinkage = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkage.hasNext();)
            {
                t_iLinkageCount++;
                Linkage t_objLinkage = t_iterLinkage.next();

                if (!a_objSubstituent.getSubstituentType().getComplexType()){
                    for (Iterator <Integer> iter = t_objLinkage.getParentLinkages().iterator(); iter.hasNext();) {
                        Integer element =  iter.next();
                        if (!element.equals(1)){
                            this.m_aErrorList.add("For this substituent linkage pos must be 1.");
                        }

                    }
                }
                else
                {
                    for (Iterator <Integer> iter = t_objLinkage.getParentLinkages().iterator(); iter.hasNext();) 
                    {
                        int element =  iter.next();
                        if ( element < 1 && element != Linkage.UNKNOWN_POSITION )
                        {
                            this.m_aErrorList.add("For this substituent "+a_objSubstituent.getSubstituentType().getName()+" linkage pos must be larger than 0.");
                        }
                    }
                }
                if ( t_objLinkage.getParentLinkageType() != LinkageType.NONMONOSACCHARID )
                {
                    this.m_aErrorList.add("LinkageType for substituent must be LinkageType.NONMONOSACCHARID.");
                }
            }        
        }
        if ( a_objSubstituent.getSubstituentType() == null )
        {
            this.m_aErrorList.add("SubstituentType for substituent is null.");
        }
        else { 
            SubstituentType t_objSubstitutent = a_objSubstituent.getSubstituentType(); 
            if ( t_iLinkageCount < t_objSubstitutent.getMinValence() )
            {
                this.m_aErrorList.add("Minimum valence constraint for substituent " + a_objSubstituent.getSubstituentType().getName() + " not fulfilled.");
            }
            if ( t_iLinkageCount > t_objSubstitutent.getMaxValence() && t_objSubstitutent.getMaxValence() > 0 )
            {
                this.m_aErrorList.add("Maximum valence constraint for substituent " + a_objSubstituent.getSubstituentType().getName() + " broken.");
            }
            if ( t_objSubstitutent == SubstituentType.EPOXY || t_objSubstitutent == SubstituentType.LACTONE 
                    || t_objSubstitutent == SubstituentType.ANHYDRO )
            {
                if ( a_objSubstituent.getChildEdges().size() != 0 )
                {
                    this.m_aErrorList.add("SubstituentType.EPOXY, SubstituentType.LACTONE, SubstituentType.ANHYDRO can not have child linkages.");                
                }
                if ( a_objSubstituent.getParentEdge().getGlycosidicLinkages().size() != 2 )
                {
                    this.m_aErrorList.add("SubstituentType.EPOXY, SubstituentType.LACTONE, SubstituentType.ANHYDRO can not have more than two linkages.");
                }
            }        
        }
        // test if the parent is also an substituent
        if ( a_objSubstituent.getParentEdge() != null )
        {
            GlycoNode t_objNode = a_objSubstituent.getParentEdge().getParent();
            if ( t_objNode != null )
            {
                if ( this.m_visNodeType.isSubstituent(t_objNode) )
                {
                    this.m_aErrorList.add("Substituent - Substituent linkages are not allowed.");                    
                }
            }
        }
    }

    /**
     * - cyclic must have a start residue
     * - start residue may not be a SugarUnitCyclic
     * - start residue must be part of the same GlycoNode
     * - cyclic may not have child residues 
     */
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException 
    {
        GlycoNode t_objNode = a_objCyclic.getCyclicStart();
        if ( t_objNode == null )
        {
            this.m_aErrorList.add("Start residue for cyclic structure is null.");
        }
        if ( this.m_visNodeType.isSugarUnitCyclic(t_objNode) )
        {
            this.m_aErrorList.add("Start residue for cyclic structure is a SugarUnitCyclic.");
        }
        if ( !this.m_objGlycoGraph.containsNode(t_objNode) )
        {
            this.m_aErrorList.add("Start residue is not part of the same GlycoNode as SugarUnitCyclic.");
        }
        if ( a_objCyclic.getChildEdges().size() != 0 )
        {
            this.m_aErrorList.add("SugarUnitCyclic can not have child linkages.");
        }
        if ( a_objCyclic.getParentEdge() == null )
        {
            this.m_aErrorList.add("SugarUnitCyclic must have a parent linkage.");
        }
        else
        {
            for (Iterator<Linkage> t_iterEdge = a_objCyclic.getParentEdge().getGlycosidicLinkages().iterator(); t_iterEdge.hasNext();) 
            {
                Linkage t_objLinkage = t_iterEdge.next();
                for (Iterator<Integer> t_iterPos = t_objLinkage.getChildLinkages().iterator(); t_iterPos.hasNext();) 
                {
                    Integer t_iPos = t_iterPos.next();
                    if ( t_iPos < 1 && t_iPos != Linkage.UNKNOWN_POSITION )
                    {
                        this.m_aErrorList.add("Linkage position smaller than 1 are not allowed.");
                    }                            
                }
            }
        }
    }

    /**
     * - sugar only one root residue (connected sugar)
     * - UnderdeterminedSubTree only one root residue
     * - UnderdeterminedSubTree LowerProb == 100 (no statistic for sugar only repeat)
     * - UnderdeterminedSubTree UpperProb > 100 forbidden
     * - UnderdeterminedSubTree at least 2 parents for each uncertain terminal residue
     * - Root nodes of the sugar can not be substituents
     */
    public void start(Sugar a_objSugar) throws GlycoVisitorException 
    {
        this.clear();
        try
        {
            this.m_objGlycoGraph = a_objSugar;
            if ( a_objSugar.getRootNodes().size() != 1 )
            {
                this.m_aErrorList.add("Sugar has more than one root residue.");
            }
            GlycoTraverser t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(a_objSugar);
            GlycoVisitorNodeType t_visType = new GlycoVisitorNodeType();
            for (Iterator<GlycoNode> t_iterRoot = a_objSugar.getRootNodes().iterator(); t_iterRoot.hasNext();) 
            {
                GlycoNode t_objNode = t_iterRoot.next();
                if ( t_visType.isSubstituent(t_objNode) )
                {
                    this.m_aErrorList.add("A substituent can not be the root node auf an sugar.");
                }
            }
            for (Iterator<UnderdeterminedSubTree> t_iterSubTree = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterSubTree.hasNext();)
            {
                UnderdeterminedSubTree t_objTree = t_iterSubTree.next();
                this.m_objGlycoGraph = t_objTree;
                if ( t_objTree.getRootNodes().size() != 1 )
                {
                    this.m_aErrorList.add("UnderdeterminedSubTree has more than one root residue.");
                }
                t_objTraverser.traverseGraph(t_objTree);
                this.testUnderdeterminded(t_objTree,a_objSugar);
                if ( t_objTree.getProbabilityLower() < 100.0 )
                {
                    this.m_aErrorList.add("Sugar can not have a statistical distribution.");                    
                }
                if ( t_objTree.getParents().size() < 2 )
                {
                    this.m_aErrorList.add("Each uncertain terminal block needs at least 2 parent residues.");
                }
            }
        } 
        catch (GlycoconjugateException e)
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }        
    }

    /**
     * - UnderdeterminedSubTree parents must be part of sugar
     * - UnderdeterminedSubTree LowerProb <= UpperProb
     * - UnderdeterminedSubTree UpperProb > 100 forbidden
     * - UnderdeterminedSubTree Connection - parent and child == null
     * - UnderdeterminedSubTree Connection - normal linkage position tests (duplication, UNKNOWN + KOWN etc.)
     * - UnderdeterminedSubTree Connection test if LinkageType is valid for parent and Lead in residue + in case of two monosaccharides if the connection is a valid glycosidic linkage
     * @throws GlycoVisitorException 
     * @throws GlycoconjugateException 
     */
    private void testUnderdeterminded(UnderdeterminedSubTree a_objTree,GlycoGraph a_objGraph ) throws GlycoVisitorException, GlycoconjugateException
    {

        for (Iterator<GlycoNode> t_iterParents = a_objTree.getParents().iterator(); t_iterParents.hasNext();)
        {
            GlycoNode t_objNode = t_iterParents.next();
            if ( !a_objGraph.containsNode(t_objNode) )
            {
                this.m_aErrorList.add("Parent node of UnderdeterminedSubTree is not part of the attached GlycoGraph.");
            }
        }
        if ( a_objTree.getProbabilityLower() > a_objTree.getProbabilityUpper() )
        {
            this.m_aErrorList.add("Lower border of probabilitic value for UnderdeterminedSubTree is larger than uper border.");
        }
        if ( a_objTree.getProbabilityUpper() > 100.0 )
        {
            this.m_aErrorList.add("A probabilitic value for UnderdeterminedSubTree larger than 100.0% is not possible.");
        }
        if ( a_objTree.getParents().size() < 2 )
        {
            if( a_objTree.getProbabilityLower() >= 100.0 )
            {
                this.m_aErrorList.add("Each uncertain terminal block needs at least 2 parent residues.");
            }
        }
        // connection test
        GlycoEdge t_objEdge = a_objTree.getConnection();
        if ( t_objEdge.getChild() != null || t_objEdge.getParent() != null )
        {
            this.m_aErrorList.add("Parent and Child for connection into UnderdetermindedSubTree must be null.");
        }
        if ( t_objEdge.getGlycosidicLinkages().size() == 0 )
        {
            this.m_aErrorList.add("Connection into UnderdetermindedSubTree contains no Linkages.");
        }
        // test linkage positions
        for (Iterator<Linkage> t_iterLinkages = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
        {
            Linkage t_objLinkage = t_iterLinkages.next();
            // child side
            if ( t_objLinkage.getChildLinkages().size() == 0 )
            {
                this.m_aErrorList.add("No child linkage position given in GlycoEdge.");
            }
            ArrayList<Integer> t_aPositions = new ArrayList<Integer>();
            Integer t_iPosition = null;
            for (Iterator<Integer> t_iterPosition = t_objLinkage.getChildLinkages().iterator(); t_iterPosition.hasNext();)
            {
                t_iPosition = t_iterPosition.next();
                if ( t_aPositions.contains(t_iPosition) )
                {
                    this.m_aErrorList.add("Duplicated child linkage position in GlycoEdge.");
                }
                t_aPositions.add(t_iPosition);
            }
            t_aPositions = t_objLinkage.getChildLinkages();
            if ( t_aPositions.size() > 1 && t_aPositions.contains(Linkage.UNKNOWN_POSITION) )
            {
                this.m_aErrorList.add("Unknown and defined positions in one GlycoEdge.");
            }
            // parent side
            if ( t_objLinkage.getParentLinkages().size() == 0 )
            {
                this.m_aErrorList.add("No parent linkage position given in GlycoEdge.");
            }
            t_aPositions = new ArrayList<Integer>();
            t_iPosition = null;
            for (Iterator<Integer> t_iterPosition = t_objLinkage.getParentLinkages().iterator(); t_iterPosition.hasNext();)
            {
                t_iPosition = t_iterPosition.next();
                if ( t_aPositions.contains(t_iPosition) )
                {
                    this.m_aErrorList.add("Duplicated parent linkage position in GlycoEdge.");
                }
                t_aPositions.add(t_iPosition);
            }
            t_aPositions = t_objLinkage.getParentLinkages();
            if ( t_aPositions.size() > 1 && t_aPositions.contains(Linkage.UNKNOWN_POSITION) )
            {
                this.m_aErrorList.add("Unknown and defined positions in one GlycoEdge.");
            }
            // test linkage types - child
            LinkageType t_objLinkTypeChild = null;
            if ( a_objTree.getRootNodes().size() == 1 )
            {
                // otherwise this test do not make sense
                GlycoNode t_objNode = a_objTree.getRootNodes().get(0);
                if ( this.m_visNodeType.isMonosaccharide(t_objNode) )
                {
                    if ( t_objLinkage.getChildLinkageType() == LinkageType.NONMONOSACCHARID 
                            || t_objLinkage.getChildLinkageType() == LinkageType.UNVALIDATED )
                    {
                        this.m_aErrorList.add("LinkageType for Start residue of UnderdetermindedSubTree is not allowed (for Monosaccharide).");
                    }
                    else
                    {
                        t_objLinkTypeChild = t_objLinkage.getChildLinkageType(); 
                    }
                }
                else if ( this.m_visNodeType.isSubstituent(t_objNode) )
                {
                    if ( t_objLinkage.getChildLinkageType() != LinkageType.NONMONOSACCHARID )
                    {
                        this.m_aErrorList.add("LinkageType of start residue of UnderdetermindedSubTree is not allowed (for Substituent).");
                    }
                }
                else if ( this.m_visNodeType.isNonMonosaccharide(t_objNode) )
                {
                    if ( t_objLinkage.getChildLinkageType() != LinkageType.NONMONOSACCHARID )
                    {
                        this.m_aErrorList.add("LinkageType of start residue of UnderdetermindedSubTree is not allowed (for NonMonosaccharide).");
                    }
                }
            }
            // test linkage types - parent
            boolean t_bContainMS = false;
            boolean t_bContainSubst = false;
            boolean t_bContainNonMS = false;
            for (Iterator<GlycoNode> t_iterParent = a_objTree.getParents().iterator(); t_iterParent.hasNext();)
            {
                GlycoNode t_objParent = this.getSimpleGlycoNode(t_iterParent.next(),false);
                LinkageType t_objLinkTypeParent = null;
                if ( this.m_visNodeType.isMonosaccharide(t_objParent) )
                {
                    if ( t_objLinkage.getParentLinkageType() == LinkageType.NONMONOSACCHARID 
                            || t_objLinkage.getParentLinkageType() == LinkageType.UNVALIDATED )
                    {
                        this.m_aErrorList.add("LinkageType for parent of UnderdetermindedSubTree is not allowed (for Monosaccharide).");
                    }
                    else
                    {
                        // test for glycosidic linkage
                        t_objLinkTypeParent = t_objLinkage.getParentLinkageType(); 
                        if ( t_objLinkTypeChild != null )                                    
                        {
                            // both are monosaccharides
                            if ( t_objLinkTypeParent != LinkageType.UNKNOWN )
                            {
                                if ( t_objLinkTypeChild != LinkageType.DEOXY || t_objLinkTypeParent != LinkageType.H_AT_OH )
                                {
                                    this.m_aErrorList.add("UnderdetermindedSubTree connection is not a glycosidic linkage (for two Monosaccharides).");
                                }
                            }
                        }
                    }
                    t_bContainMS = true;
                }
                else if ( this.m_visNodeType.isNonMonosaccharide(t_objParent) )
                {
                    if ( t_objLinkage.getParentLinkageType() != LinkageType.NONMONOSACCHARID && t_objLinkage.getParentLinkageType() != LinkageType.UNKNOWN )
                    {
                        this.m_aErrorList.add("LinkageType of parent residue of UnderdetermindedSubTree is not allowed (for NonMonosaccharide).");
                    }
                    t_bContainNonMS = true;
                }
                else if ( this.m_visNodeType.isSubstituent(t_objParent) )
                {
                    if ( t_objLinkage.getParentLinkageType() != LinkageType.NONMONOSACCHARID && t_objLinkage.getParentLinkageType() != LinkageType.UNKNOWN )
                    {
                        this.m_aErrorList.add("LinkageType of parent residue of UnderdetermindedSubTree is not allowed (for Substituent).");
                    }
                    t_bContainSubst = true;
                }
            }
            if ( t_bContainMS )
            {
                if ( t_bContainNonMS || t_bContainSubst )
                {
                    if ( t_objLinkage.getParentLinkageType() != LinkageType.UNKNOWN )
                    {
                        this.m_aErrorList.add("LinkageType of parent residue of UnderdetermindedSubTree must be UNKNOWN (for different types of parent residues).");                        
                    }                    
                }
                else
                {
                    if ( t_objLinkage.getParentLinkageType() == LinkageType.UNKNOWN )
                    {
                        this.m_aErrorList.add("LinkageType of parent residue of UnderdetermindedSubTree can not be UNKNOWN (for same types of parent residues).");                        
                    }                    
                }
            }
            else if ( t_bContainNonMS )
            {
                if ( t_bContainSubst )
                {
                    if ( t_objLinkage.getParentLinkageType() != LinkageType.UNKNOWN )
                    {
                        this.m_aErrorList.add("LinkageType of parent residue of UnderdetermindedSubTree must be UNKNOWN (for different types of parent residues).");                        
                    }                    
                }
                else
                {
                    if ( t_objLinkage.getParentLinkageType() == LinkageType.UNKNOWN )
                    {
                        this.m_aErrorList.add("LinkageType of parent residue of UnderdetermindedSubTree can not be UNKNOWN (for same types of parent residues).");                        
                    }                    
                }
            }
            else if ( t_bContainSubst )
            {
                if ( t_objLinkage.getParentLinkageType() == LinkageType.UNKNOWN )
                {
                    this.m_aErrorList.add("LinkageType of parent residue of UnderdetermindedSubTree can not be UNKNOWN (for same types of parent residues).");                        
                }                    

            }
        }
        // TODO : this.testMS(); for underdetermined start & parents because of the special linkages
    }

    /**
     * - normal test of GlycoGraph
     * - if min == max repeat count they must be larger than 6 or UNKNOWN
     * - min and max repeat count may be larger than 0 (beside UNKNOWN) 
     * - min repeat count may be smaller than max repeat count (beside UNKNOWN)
     * - test internal repeat linkage (nodes are part of the repeat, linkagetype correct)
     * - test linkage type of linkage into repeat
     * - test linkage type of linkage from repeat 
     * - UnderdeterminedSubTree only one root residue
     * - UnderdeterminedSubTree parents must be part of repeat unit
     * - UnderdeterminedSubTree LowerProb <= UpperProb
     * - UnderdeterminedSubTree UpperProb > 100 forbidden
     * - UnderdeterminedSubTree at least 2 parents for each uncertain terminal residue (for statistical one parent is possible)
     */
    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException 
    {
        try
        {
            GlycoGraph t_objGraph = this.m_objGlycoGraph;            
            this.m_objGlycoGraph = a_objRepeat;
            if ( a_objRepeat.getRootNodes().size() != 1 )
            {
                this.m_aErrorList.add("SugarUnitRepeat has more than one root residue.");
            }
            GlycoTraverser t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(a_objRepeat);            
            // repeat count
            if ( a_objRepeat.getMinRepeatCount() == a_objRepeat.getMaxRepeatCount() )
            {
                if ( a_objRepeat.getMinRepeatCount() < 7 && a_objRepeat.getMinRepeatCount() != SugarUnitRepeat.UNKNOWN )
                {
                    this.m_aErrorList.add("SugarUnitRepeat with repeat count less than 7 are not allowed.");
                }
            }
            else
            {
                if ( a_objRepeat.getMinRepeatCount() > a_objRepeat.getMaxRepeatCount() && a_objRepeat.getMaxRepeatCount() != SugarUnitRepeat.UNKNOWN )
                {
                    this.m_aErrorList.add("Min repeat count of SugarUnitRepeat must be smaller than max repeat count.");
                }
                if ( a_objRepeat.getMinRepeatCount() < 1 && a_objRepeat.getMinRepeatCount() != SugarUnitRepeat.UNKNOWN )
                {
                    this.m_aErrorList.add("Negative min repeat count of SugarUnitRepeat is not allowed.");
                }
                if ( a_objRepeat.getMaxRepeatCount() < 1 && a_objRepeat.getMaxRepeatCount() != SugarUnitRepeat.UNKNOWN )
                {
                    this.m_aErrorList.add("Negative max repeat count of SugarUnitRepeat is not allowed.");
                }
            }
            // test internal linkage
            GlycoEdge t_objInternal = a_objRepeat.getRepeatLinkage(); 
            this.testLinkageArray(t_objInternal);
            // internal nodes part of repeat?
            if ( t_objInternal.getChild() == null || t_objInternal.getParent() == null )
            {
                this.m_aErrorList.add("Child or parent residue of internal repeat linkage is null.");
            }
            else
            {
                if ( !a_objRepeat.containsNode(t_objInternal.getChild()) )
                {
                    this.m_aErrorList.add("Child of repeat linkage is not part of the repeat unit.");
                }
                if ( !a_objRepeat.getRootNodes().contains(t_objInternal.getChild()) )
                {
                    this.m_aErrorList.add("Child of repeat linkage is not part of the root nodes of the repeat unit.");
                }
                if ( !a_objRepeat.containsNode(t_objInternal.getParent()) )
                {
                    this.m_aErrorList.add("Parent of repeat linkage is not part of the repeat unit.");
                }
                GlycoNode t_objParent = this.getSimpleGlycoNode(t_objInternal.getParent(),false);
                GlycoNode t_objChild  = this.getSimpleGlycoNode(t_objInternal.getChild(),true);
                // internal linkagetypes
                for (Iterator<Linkage> t_iterLinkages = t_objInternal.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
                {
                    Linkage t_objLinkage = t_iterLinkages.next();
                    if ( t_objParent != null )
                    {
                        if ( this.m_visNodeType.isMonosaccharide(t_objParent) )
                        {
                            if ( t_objLinkage.getParentLinkageType() == LinkageType.NONMONOSACCHARID ||
                                    t_objLinkage.getParentLinkageType() == LinkageType.UNVALIDATED )
                            {
                                this.m_aErrorList.add("Wrong linkage type in internal repeat unit (for Monosaccharide).");
                            }
                        }
                        else if ( this.m_visNodeType.isNonMonosaccharide(t_objParent) )
                        {
                            if ( t_objLinkage.getParentLinkageType() != LinkageType.NONMONOSACCHARID )
                            {
                                this.m_aErrorList.add("Wrong linkage type in internal repeat unit (for NonMonosaccharide).");
                            }
                        }
                        else if ( this.m_visNodeType.isSubstituent(t_objParent) )
                        {
                            if ( t_objLinkage.getParentLinkageType() != LinkageType.NONMONOSACCHARID )
                            {
                                this.m_aErrorList.add("Wrong linkage type in internal repeat unit (for Substituent).");
                            }
                        }
                        else if ( this.m_visNodeType.isSugarUnitCyclic(t_objParent) )
                        {
                            this.m_aErrorList.add("Cyclic unit can not be the parent residue in a repeat linkage.");
                        }
                    }
                    if ( t_objChild != null )
                    {
                        if ( this.m_visNodeType.isMonosaccharide(t_objChild) )
                        {
                            if ( t_objLinkage.getChildLinkageType() == LinkageType.NONMONOSACCHARID ||
                                    t_objLinkage.getChildLinkageType() == LinkageType.UNVALIDATED )
                            {
                                this.m_aErrorList.add("Wrong linkage type in internal repeat unit (for Monosaccharide).");
                            }
                        }
                        else if ( this.m_visNodeType.isNonMonosaccharide(t_objChild) )
                        {
                            if ( t_objLinkage.getChildLinkageType() != LinkageType.NONMONOSACCHARID )
                            {
                                this.m_aErrorList.add("Wrong linkage type in internal repeat unit (for NonMonosaccharide).");
                            }
                        }
                        else if ( this.m_visNodeType.isSubstituent(t_objChild) )
                        {
                            if ( t_objLinkage.getChildLinkageType() != LinkageType.NONMONOSACCHARID )
                            {
                                this.m_aErrorList.add("Wrong linkage type in internal repeat unit (for Substituent).");
                            }
                        }
                        else if ( this.m_visNodeType.isSugarUnitCyclic(t_objChild) )
                        {
                            this.m_aErrorList.add("Cyclic unit can not be the parent residue in a repeat linkage.");
                        }
                    }
                    if ( t_objChild != null && t_objParent != null )
                    {
                        if ( this.m_visNodeType.isMonosaccharide(t_objChild) && this.m_visNodeType.isMonosaccharide(t_objParent) )
                        {
                            if ( t_objLinkage.getParentLinkageType() != LinkageType.H_AT_OH || t_objLinkage.getChildLinkageType() != LinkageType.DEOXY )
                            {
                                this.m_aErrorList.add("Repeatlinkage is not a glycosidic linkage.");
                            }
                        }
                    }
                    for (Iterator<Integer> t_iterChilds = t_objLinkage.getChildLinkages().iterator(); t_iterChilds.hasNext();) 
                    {
                        Integer t_iPos = t_iterChilds.next();
                        if ( t_iPos < 1 && t_iPos != Linkage.UNKNOWN_POSITION )
                        {
                            this.m_aErrorList.add("Linkage position smaller than 1 are not allowed.");
                        }
                    }
                    for (Iterator<Integer> t_iterChilds = t_objLinkage.getParentLinkages().iterator(); t_iterChilds.hasNext();) 
                    {
                        Integer t_iPos = t_iterChilds.next();
                        if ( t_iPos < 1 && t_iPos != Linkage.UNKNOWN_POSITION )
                        {
                            this.m_aErrorList.add("Linkage position smaller than 1 are not allowed.");
                        }
                    }
                }
                // linkage type into repeat
                GlycoEdge t_objEdge= a_objRepeat.getParentEdge();
                if ( t_objEdge != null )
                {
                    for (Iterator<Linkage> t_iterLinkages = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
                    {
                        Linkage t_objLinkage = t_iterLinkages.next();
                        // MUST BE NONMONOSACCHARIDE
                        if ( t_objLinkage.getChildLinkageType() != LinkageType.NONMONOSACCHARID )
                        {
                            this.m_aErrorList.add("Wrong linkage type in linkage to repeat unit.");
                        }
                        for (Iterator<Integer> t_iterPos = t_objLinkage.getChildLinkages().iterator(); t_iterPos.hasNext();) 
                        {
                            Integer t_iPos = t_iterPos.next();
                            if ( t_iPos < 1 && t_iPos != Linkage.UNKNOWN_POSITION )
                            {
                                this.m_aErrorList.add("Linkage position smaller than 1 are not allowed.");
                            }                            
                        }

//                        if ( t_objChild != null )
//                        {
//                        if ( this.m_visNodeType.isMonosaccharide(t_objChild) )
//                        {
//                        if ( t_objLinkage.getChildLinkageType() == LinkageType.NONMONOSACCHARID ||
//                        t_objLinkage.getChildLinkageType() == LinkageType.UNVALIDATED )
//                        {
//                        this.m_aErrorList.add("Wrong linkage type in internal repeat unit (for Monosaccharide).");
//                        }
//                        }
//                        else if ( this.m_visNodeType.isNonMonosaccharide(t_objChild) )
//                        {
//                        if ( t_objLinkage.getChildLinkageType() != LinkageType.NONMONOSACCHARID )
//                        {
//                        this.m_aErrorList.add("Wrong linkage type in internal repeat unit (for NonMonosaccharide).");
//                        }
//                        }
//                        else if ( this.m_visNodeType.isSubstituent(t_objChild) )
//                        {
//                        if ( t_objLinkage.getChildLinkageType() != LinkageType.NONMONOSACCHARID )
//                        {
//                        this.m_aErrorList.add("Wrong linkage type in internal repeat unit (for Substituent).");
//                        }
//                        }
//                        else if ( this.m_visNodeType.isSugarUnitCyclic(t_objChild) )
//                        {
//                        this.m_aErrorList.add("Cyclic unit can not be the parent residue in a repeat linkage.");
//                        }
//                        }
                    }
                    // TODO: test if linkage is possible
                    if ( t_objEdge.getGlycosidicLinkages().size() != a_objRepeat.getRepeatLinkage().getGlycosidicLinkages().size() )
                    {
                        this.m_aErrorList.add("Number of linkages into repeat and internal repeat linkages does not match.");
                    }
                }
                // linkage type from repeat
                for (Iterator<GlycoEdge> t_iterChilds = a_objRepeat.getChildEdges().iterator(); t_iterChilds.hasNext();)
                {
                    t_objEdge = t_iterChilds.next();
                    for (Iterator<Linkage> t_iterLinkages = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
                    {
                        Linkage t_objLinkage = t_iterLinkages.next();
                        // MUST BE NONMONOSACCHARIDE
                        if ( t_objLinkage.getParentLinkageType() != LinkageType.NONMONOSACCHARID )
                        {
                            this.m_aErrorList.add("Wrong linkage type in linkage from repeat unit.");
                        }
                        for (Iterator<Integer> t_iterPos = t_objLinkage.getParentLinkages().iterator(); t_iterPos.hasNext();) 
                        {
                            Integer t_iPos = t_iterPos.next();
                            if ( t_iPos < 1 && t_iPos != Linkage.UNKNOWN_POSITION )
                            {
                                this.m_aErrorList.add("Linkage position smaller than 1 are not allowed.");
                            }                            
                        }
//                        if ( t_objParent != null )
//                        {
//                        if ( this.m_visNodeType.isMonosaccharide(t_objParent) )
//                        {
//                        if ( t_objLinkage.getParentLinkageType() == LinkageType.NONMONOSACCHARID ||
//                        t_objLinkage.getParentLinkageType() == LinkageType.UNVALIDATED )
//                        {
//                        this.m_aErrorList.add("Wrong linkage type in internal repeat unit (for Monosaccharide).");
//                        }
//                        }
//                        else if ( this.m_visNodeType.isNonMonosaccharide(t_objParent) )
//                        {
//                        if ( t_objLinkage.getParentLinkageType() != LinkageType.NONMONOSACCHARID )
//                        {
//                        this.m_aErrorList.add("Wrong linkage type in internal repeat unit (for NonMonosaccharide).");
//                        }
//                        }
//                        else if ( this.m_visNodeType.isSubstituent(t_objParent) )
//                        {
//                        if ( t_objLinkage.getParentLinkageType() != LinkageType.NONMONOSACCHARID )
//                        {
//                        this.m_aErrorList.add("Wrong linkage type in internal repeat unit (for Substituent).");
//                        }
//                        }
//                        else if ( this.m_visNodeType.isSugarUnitCyclic(t_objParent) )
//                        {
//                        this.m_aErrorList.add("Cyclic unit can not be the parent residue in a repeat linkage.");
//                        }
//                        }
                    }
                    if ( t_objEdge.getGlycosidicLinkages().size() != a_objRepeat.getRepeatLinkage().getGlycosidicLinkages().size() )
                    {
                        this.m_aErrorList.add("Number of linkages out of repeat and internal repeat linkages does not match.");
                    }
                }
                // TODO : test repeat in and out residue if linkage is possible
            }
            for (Iterator<UnderdeterminedSubTree> t_iterSubTree = a_objRepeat.getUndeterminedSubTrees().iterator(); t_iterSubTree.hasNext();)
            {
                UnderdeterminedSubTree t_objTree = t_iterSubTree.next();
                this.m_objGlycoGraph = t_objTree;
                if ( t_objTree.getRootNodes().size() != 1 )
                {
                    this.m_aErrorList.add("UnderdeterminedSubTree has more than one root residue.");
                }
                t_objTraverser.traverseGraph(t_objTree);
                this.testUnderdeterminded(t_objTree, a_objRepeat);
            }
            this.m_objGlycoGraph = t_objGraph;
        } 
        catch (GlycoconjugateException e)
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }        
    }

    /**
     * gives you NonMS, MS, Unval. oder Subst 
     * if a_objResidue is a complex Node you get the first/last (a_bRepeatIn) Simple GlycoNode of the object
     * in case a_objResidue is an alternative unit you get null   
     */
    private GlycoNode getSimpleGlycoNode(GlycoNode a_objResidue, boolean a_bRepeatIn) throws GlycoVisitorException
    {
        int t_iNodeType = this.m_visNodeType.getNodeType(a_objResidue);
        if ( t_iNodeType == GlycoVisitorNodeType.REPEAT )
        {
//            if ( a_bRepeatIn )
//            {
//            return this.getSimpleGlycoNode(
//            this.m_visNodeType.getSugarUnitRepeat(a_objResidue).getRepeatLinkage().getChild(),
//            a_bRepeatIn);
//            }
//            else
//            {
//            return this.getSimpleGlycoNode(
//            this.m_visNodeType.getSugarUnitRepeat(a_objResidue).getRepeatLinkage().getParent(),
//            a_bRepeatIn);
//            }
            return a_objResidue;
        }
        else if ( t_iNodeType == GlycoVisitorNodeType.ALTERNATIVE )
        {
            return null;
        }
        else if ( t_iNodeType == GlycoVisitorNodeType.CYCLIC )
        {
            return this.getSimpleGlycoNode(
                    this.m_visNodeType.getSugarUnitCyclic(a_objResidue).getCyclicStart(),
                    a_bRepeatIn);
        }        
        return a_objResidue;
    }

    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        if ( a_objAlternative.getAlternatives().size() < 2 )
        {
            this.m_aErrorList.add("SugarUnitAlternative must have at least two alternative units.");
        }        
        for (Iterator<GlycoGraphAlternative> t_iterAlt = a_objAlternative.getAlternatives().iterator(); t_iterAlt.hasNext();) 
        {
            GlycoGraphAlternative t_objAltGraph = t_iterAlt.next();
            t_objTraverser.traverseGraph(t_objAltGraph);
            // test lead in
            if ( a_objAlternative.getParentEdge() == null )
            {
                if ( t_objAltGraph.getLeadInNode() != null )
                {
                    this.m_aErrorList.add("SugarUnitAlternative without parent edge can not have lead in nodes.");
                }
            }
            else
            {
                if ( t_objAltGraph.getLeadInNode() == null )
                {
                    this.m_aErrorList.add("SugarUnitAlternative with parent edge has to have lead in nodes.");
                }
            }
            // test lead out nodes
            for (Iterator<GlycoEdge> t_iterChilds = a_objAlternative.getChildEdges().iterator(); t_iterChilds.hasNext();)
            {
                GlycoNode t_objChild = t_iterChilds.next().getChild();
                if ( !t_objAltGraph.getLeadOutNodeToNode().containsKey(t_objChild) )
                {
                    this.m_aErrorList.add("Child node is missing in lead out node definition.");
                }
            }
            if ( t_objAltGraph.getLeadOutNodeToNode().size() != a_objAlternative.getChildEdges().size() )
            {
                this.m_aErrorList.add("Number of lead out nodes for a alternative tree and number of child residues for a Alternative unit must be identical.");
            }

        }
        // test linkages into alternative
        GlycoNode t_objLead = null;
        boolean t_bAllSame = true;
        if ( a_objAlternative.getParentEdge() != null )
        {
            for (Iterator<GlycoGraphAlternative> t_iterAlt = a_objAlternative.getAlternatives().iterator(); t_iterAlt.hasNext();) 
            {
                GlycoGraphAlternative t_objAltGraph = t_iterAlt.next();
                if ( t_objLead == null )
                {
                    t_objLead = t_objAltGraph.getLeadInNode();
                }
                else
                {
                    if ( this.m_visNodeType.getNodeType(t_objLead) != this.m_visNodeType.getNodeType(t_objAltGraph.getLeadInNode()) )
                    {
                        t_bAllSame = false;
                    }
                }
            }
            if ( t_bAllSame )
            {
                if ( this.testLinkageType(a_objAlternative.getParentEdge().getParent(),a_objAlternative.getParentEdge(),t_objLead) )
                {
                    this.m_aErrorList.add("Lead in linkage of alternative trees have the false linkage type.");                    
                }
            }
            else
            {
                for (Iterator<Linkage> t_iterLinakge = a_objAlternative.getParentEdge().getGlycosidicLinkages().iterator(); t_iterLinakge.hasNext();)
                {
                    Linkage t_objLinkage = t_iterLinakge.next();
                    if ( t_objLinkage.getChildLinkageType() != LinkageType.UNKNOWN )
                    {
                        this.m_aErrorList.add("For alternative trees with diferent types of lead in node the linkage type of the parent linkage has to be UNKNOWN.");
                    }
                }
            }
        }
        // test linkages from alternative         
        for (Iterator<GlycoEdge> t_iterChild = a_objAlternative.getChildEdges().iterator(); t_iterChild.hasNext();)
        {
            GlycoEdge t_objEdge = t_iterChild.next();
            t_bAllSame = true;
            t_objLead = null;
            for (Iterator<GlycoGraphAlternative> t_iterAlt = a_objAlternative.getAlternatives().iterator(); t_iterAlt.hasNext();) 
            {
                GlycoGraphAlternative t_objAltGraph = t_iterAlt.next();
                if ( t_objLead == null )
                {
                    t_objLead = t_objAltGraph.getLeadOutNodeToNode().get(t_objEdge.getChild());
                }
                else
                {
                    if ( this.m_visNodeType.getNodeType(t_objLead) != this.m_visNodeType.getNodeType(t_objAltGraph.getLeadOutNodeToNode().get(t_objEdge.getChild())) )
                    {
                        t_bAllSame = false;
                    }
                }
            }
            if ( t_bAllSame )
            {
                if ( this.testLinkageType(t_objLead,t_objEdge,t_objEdge.getChild()) )
                {
                    this.m_aErrorList.add("Lead out linkage of alternative trees have the false linkage type.");                    
                }
            }
            else
            {
                for (Iterator<Linkage> t_iterLinakge = t_objEdge.getGlycosidicLinkages().iterator(); t_iterLinakge.hasNext();)
                {
                    Linkage t_objLinkage = t_iterLinakge.next();
                    if ( t_objLinkage.getParentLinkageType() != LinkageType.UNKNOWN )
                    {
                        this.m_aErrorList.add("For alternative trees with different types of lead out node, the linkage type of the corresponding child linkage has to be UNKNOWN.");
                    }
                }
            }
        }
    }

    /**
     * @throws GlycoVisitorException 
     * 
     */
    private boolean testLinkageType(GlycoNode a_objParent, GlycoEdge a_objEdge, GlycoNode a_objChild) throws GlycoVisitorException
    {
        GlycoNode t_objParent = this.getSimpleGlycoNode(a_objParent, false);
        GlycoNode t_objChild  = this.getSimpleGlycoNode(a_objChild , true );
        boolean t_bResult = true;
        for (Iterator<Linkage> t_iterLinkages = a_objEdge.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
        {
            Linkage t_objLinkage = t_iterLinkages.next();
            if ( t_objParent != null )
            {
                if ( this.m_visNodeType.isMonosaccharide(t_objParent) )
                {
                    if ( t_objLinkage.getParentLinkageType() == LinkageType.NONMONOSACCHARID ||
                            t_objLinkage.getParentLinkageType() == LinkageType.UNVALIDATED )
                    {
                        this.m_aErrorList.add("Wrong linkage type in linkage (for Monosaccharide).");
                    }
                }
                else if ( this.m_visNodeType.isNonMonosaccharide(t_objParent) )
                {
                    if ( t_objLinkage.getParentLinkageType() != LinkageType.NONMONOSACCHARID )
                    {
                        this.m_aErrorList.add("Wrong linkage type in linkage (for NonMonosaccharide).");
                    }
                }
                else if ( this.m_visNodeType.isSubstituent(t_objParent) )
                {
                    if ( t_objLinkage.getParentLinkageType() != LinkageType.NONMONOSACCHARID )
                    {
                        this.m_aErrorList.add("Wrong linkage type in linkage (for Substituent).");
                    }
                }
                else if ( this.m_visNodeType.isSugarUnitRepeat(t_objParent) )
                {
                    if ( t_objLinkage.getParentLinkageType() != LinkageType.NONMONOSACCHARID )
                    {
                        this.m_aErrorList.add("Wrong linkage type in linkage (for Repeat).");
                    }
                    t_objParent = null;
                }
            }
            if ( t_objChild != null )
            {
                if ( this.m_visNodeType.isMonosaccharide(t_objChild) )
                {
                    if ( t_objLinkage.getChildLinkageType() == LinkageType.NONMONOSACCHARID ||
                            t_objLinkage.getChildLinkageType() == LinkageType.UNVALIDATED )
                    {
                        this.m_aErrorList.add("Wrong linkage type in linkage (for Monosaccharide).");
                    }
                }
                else if ( this.m_visNodeType.isNonMonosaccharide(t_objChild) )
                {
                    if ( t_objLinkage.getChildLinkageType() != LinkageType.NONMONOSACCHARID )
                    {
                        this.m_aErrorList.add("Wrong linkage type in linkage (for NonMonosaccharide).");
                    }
                }
                else if ( this.m_visNodeType.isSubstituent(t_objChild) )
                {
                    if ( t_objLinkage.getChildLinkageType() != LinkageType.NONMONOSACCHARID )
                    {
                        this.m_aErrorList.add("Wrong linkage type in linkage (for Substituent).");
                    }
                }
                else if ( this.m_visNodeType.isSugarUnitRepeat(t_objChild) )
                {
                    if ( t_objLinkage.getChildLinkageType() != LinkageType.NONMONOSACCHARID )
                    {
                        this.m_aErrorList.add("Wrong linkage type in linkage (for Repeat).");
                    }
                    t_objChild = null;
                }
            }
            if ( t_objChild != null && t_objParent != null )
            {
                if ( this.m_visNodeType.isMonosaccharide(t_objChild) && this.m_visNodeType.isMonosaccharide(t_objParent) )
                {
                    if ( t_objLinkage.getParentLinkageType() != LinkageType.H_AT_OH || t_objLinkage.getChildLinkageType() != LinkageType.DEOXY )
                    {
                        this.m_aErrorList.add("Linkage is not a glycosidic linkage.");
                    }
                }
            }                    
        }
        return t_bResult;
    }

    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException 
    {
        // Monosaccharide properties

        // Null Values
        if (a_objMonosaccharid.getAnomer()==null){
            this.m_aErrorList.add("Anomer null not valid.");            
        }
        if (a_objMonosaccharid.getSuperclass()==null){
            this.m_aErrorList.add("Superclass null not valid.");            
        }

        if (a_objMonosaccharid.getBaseType()==null){
            this.m_aErrorList.add("Basetype List null not valid."); 
        }

        Integer t_iChainLength= a_objMonosaccharid.getSuperclass().getCAtomCount();
        if ( a_objMonosaccharid.getSuperclass() == Superclass.SUG )
        {
            t_iChainLength = 100;
        }
        for (Modification t_objModi : a_objMonosaccharid.getModification()){        

            if ( t_iChainLength < t_objModi.getPositionOne() )
            {
                this.m_aErrorList.add("Modification is out of C-bounds");
            }
            if (t_objModi.hasPositionTwo())
            {
                if (t_iChainLength < t_objModi.getPositionTwo() )
                {
                    this.m_aErrorList.add("Modification is out of C-bounds");
                }
            }
            //aldi only at C1
            if ( t_objModi.getModificationType() == ModificationType.ALDI && t_objModi.getPositionOne() != 1 )
            {
                this.m_aErrorList.add("Alditol is only allowed for C1");

                if (a_objMonosaccharid.getRingStart()==1){
                    this.m_aErrorList.add("C1 cannot be ringstart in alditols");
                }
            }
            //  deoxy not on C1
            if ( t_objModi.getModificationType() == ModificationType.DEOXY && t_objModi.getPositionOne() == 1 )
            {
                this.m_aErrorList.add("Deoxy on C1 impossible");
            }
            // Acidic only at terminal C
            if (t_objModi.getModificationType() == ModificationType.ACID)
            {
                if ( t_objModi.getPositionOne() != 1 && t_objModi.getPositionOne() != t_iChainLength )
                {
                    this.m_aErrorList.add("Acidic functions only at terminal C");
                }
            }
            //     TODO Enx  en can only be in conjunction with d and aldi.
            //  Other conjunctions are not valid      
            if (t_objModi.getModificationType() == ModificationType.UNKNOWN_DOUBLEBOND ||
                    t_objModi.getModificationType() == ModificationType.DOUBLEBOND    )
            {
                for (Modification m : a_objMonosaccharid.getModification()){
                    if (m.getPositionOne()==t_objModi.getPositionOne()){
                        if (m!=t_objModi){
                            if ( m.getModificationType()!=ModificationType.DEOXY &&
                                    m.getModificationType()!=ModificationType.ALDI){
                                this.m_aErrorList.add("Double bonds cannot have other modifications than Deoxy or aldi");
                            }                        
                        }            

                    }
                }
                for (Modification m : a_objMonosaccharid.getModification()){
                    if (m.getPositionOne()==t_objModi.getPositionOne()+1){
                        if (m!=t_objModi){
                            if ( m.getModificationType()!=ModificationType.DEOXY ){
                                this.m_aErrorList.add("Double bonds cannot have other modifications than Deoxy at n+1");
                            }                        
                        }            

                    }
                }            
            }        
        }

        // TODO     
        //check if basetype sequence is correct according to definition IUPAC (max size first)

        if (a_objMonosaccharid.getBaseType().size()>1){
            for (int i = 0; i < a_objMonosaccharid.getBaseType().size(); i++) {
                BaseType t_basetype = a_objMonosaccharid.getBaseType().get(i);

                if (i!=0 && t_basetype.getStereoCode().length()!=4){                    
                    this.m_aErrorList.add("Basetype order is not according to IUPAC");                    
                }

            }
        }


        // check if superclass corresponds to Basetype/modifications pattern
        // impossible for jokers like hep, oct, non etc
        if (a_objMonosaccharid.getBaseType().size()!=0)
        {
            Integer t_IntSuperclass = a_objMonosaccharid.getSuperclass().getCAtomCount();
            HashMap <Integer,String> modihash = new HashMap <Integer,String>();
            Integer t_IntTheoreticalNumber=0;       

            for (Iterator<BaseType> iter = a_objMonosaccharid.getBaseType().iterator(); iter.hasNext();) {          
                BaseType element = iter.next();          
                t_IntTheoreticalNumber+=element.getStereoCode().length();           
            }
            t_IntTheoreticalNumber+=2; // Basetype length indicated

            for (Iterator<Modification> iter = a_objMonosaccharid.getModification().iterator(); iter.hasNext();) {

                Modification element = iter.next();
                //deoxys and ketos
                if ((element.getName()=="d" || element.getName()=="keto") && 
                        (element.getPositionOne()!=1 && element.getPositionOne()!=a_objMonosaccharid.getSuperclass().getCAtomCount())){
                    if (!modihash.containsKey(element.getPositionOne())){
                        modihash.put(element.getPositionOne(),element.getName());
                        t_IntTheoreticalNumber++;
                    }               
                }
                //en - cases
                Integer d_count=0;
                if (element.getName()=="enx"||element.getName()=="en"){    
                    // scan for possible deoxys around
                    for (Iterator<Modification> iter2 = a_objMonosaccharid.getModification().iterator(); iter2.hasNext();) {
                        Modification element2 = iter2.next();
                        //deoxy at pos1
                        if (element2.getPositionOne()==element.getPositionOne() && element2.getName()=="d"){
                            d_count++;
                        }
                        //deoxy at pos2
                        if (element2.getPositionOne()==element.getPositionTwo() && element2.getName()=="d"){
                            d_count++;
                        }   
                    }
                    // Add 2 for en, reduce number for named deoxys within double bond               
                    t_IntTheoreticalNumber=t_IntTheoreticalNumber+2-d_count;
                }
            }

            if (t_IntTheoreticalNumber!=t_IntSuperclass){
                this.m_aErrorList.add("Error on superclass definition"+t_IntTheoreticalNumber+"===="+t_IntSuperclass);
            }
        }   

        // ringStart < ringEnd and not >C-atom count
        if ( a_objMonosaccharid.getRingEnd() > t_iChainLength || a_objMonosaccharid.getRingStart() > t_iChainLength)
        {
            this.m_aErrorList.add("Ring end out of C-backbone");
        }


        //check if ring size is valid and corresponds to Carbonyl-function(s).        
        // TODO 
        if ( a_objMonosaccharid.getRingEnd() > 0 && a_objMonosaccharid.getRingStart() > 0 )
        {
            //get all keto functions
            Integer t_IntRingSize=0;
            ArrayList <Integer>  t_IntKeto= new ArrayList <Integer>();

            for (Iterator<Modification> iter3 = a_objMonosaccharid.getModification().iterator(); iter3.hasNext();) 
            {
                Modification element3 = iter3.next();           
                if (element3.getModificationType()==ModificationType.KETO)
                {
                    t_IntKeto.add(element3.getPositionOne());
                }
            }
            if (!(t_IntKeto.contains(a_objMonosaccharid.getRingStart()) || a_objMonosaccharid.getRingStart()==1))
            {
                this.m_aErrorList.add("Ring has to start at a carbonyl function");
            }
            t_IntRingSize=a_objMonosaccharid.getRingEnd()-a_objMonosaccharid.getRingStart();

            if (a_objMonosaccharid.getRingStart()+t_IntRingSize>a_objMonosaccharid.getSuperclass().getCAtomCount())
            {
                this.m_aErrorList.add("Ring size exceeds backbone");
            }           
        }


        //anomer settings open ring?        
        if (a_objMonosaccharid.getAnomer()==Anomer.OpenChain)
        {
            if (a_objMonosaccharid.getRingStart() != Monosaccharide.OPEN_CHAIN || a_objMonosaccharid.getRingEnd()!= Monosaccharide.OPEN_CHAIN )
            {
                this.m_aErrorList.add("Open chain has no ring closure");
            }
        }
        if (a_objMonosaccharid.getRingStart()!=Monosaccharide.OPEN_CHAIN && a_objMonosaccharid.getRingEnd()!= Monosaccharide.OPEN_CHAIN)
        {
            if (a_objMonosaccharid.getAnomer()==Anomer.OpenChain)
            {
                this.m_aErrorList.add("Open chain has no ring closure");
            }
        }





        // holen aller linkages um das monosaccharide und pruefen ob die positionen und linkagetypen sein koennnen
        // ob linkagetypen fuer ms-ms linkages stimmen ist schon getestet

        // prepare Arrays 
        ArrayList<Boolean> t_aOH = new ArrayList<Boolean>();
        ArrayList<Boolean> t_aH = new ArrayList<Boolean>();
        for (int t_iCounter = 0; t_iCounter <= t_iChainLength; t_iCounter++)
        {
            t_aH.add(true);
            t_aOH.add(true);
        }
        // run over modification
        for (Iterator<Modification> t_iterModi = a_objMonosaccharid.getModification().iterator(); t_iterModi.hasNext();)
        {
            Modification t_objModi = t_iterModi.next();
            if ( t_objModi.getPositionOne() > 0 )
            {
                int t_iTwo = 0;
                if ( t_objModi.getPositionTwo() != null )
                {
                    if ( t_objModi.getPositionTwo() > 0 )
                    {
                        t_iTwo = 0;
                    }
                }
                if ( t_iTwo > t_iChainLength || t_objModi.getPositionOne() > t_iChainLength )
                {
                    this.m_aErrorList.add("Modification postition out of chain length.");
                }
                else
                {
                    if ( t_objModi.getModificationType() == ModificationType.ACID )
                    {
                        t_aH.set(t_objModi.getPositionOne(), false);
                    }
                    else if ( t_objModi.getModificationType() == ModificationType.DEOXY )
                    {
                        t_aOH.set(t_objModi.getPositionOne(), false);
                    }
                    else if ( t_objModi.getModificationType() == ModificationType.KETO )
                    {
                        t_aH.set(t_objModi.getPositionOne(), false);
                        if ( t_objModi.getPositionOne() != a_objMonosaccharid.getRingStart() && a_objMonosaccharid.getRingStart() != Monosaccharide.UNKNOWN_RING )
                        {
                            t_aOH.set(t_objModi.getPositionOne(), false);
                        }
                    }
                    else if ( t_objModi.getModificationType() == ModificationType.TRIPLEBOND )
                    {
                        t_aH.set(t_objModi.getPositionOne(), false);
                        t_aOH.set(t_objModi.getPositionOne(), false);
                    }
                }
            }
        }
        Linkage t_objLinkage;
        int t_iPos;
        // arrays are prepared now we look for parent array ... first ignore alternative linkages
        if ( a_objMonosaccharid.getParentEdge() != null )
        {
            for (Iterator<Linkage> t_iterEdge = a_objMonosaccharid.getParentEdge().getGlycosidicLinkages().iterator(); t_iterEdge.hasNext();)
            {
                t_objLinkage = t_iterEdge.next();
                if ( t_objLinkage.getChildLinkages().size() == 1 )
                {
                    for (Iterator<Integer> t_iterPos = t_objLinkage.getChildLinkages().iterator(); t_iterPos.hasNext();)
                    {
                        t_iPos = t_iterPos.next();
                        if ( t_iPos > 0 )
                        {
                            if ( t_iPos > t_iChainLength )
                            {
                                this.m_aErrorList.add("Attache position of monosaccharide parent edge is out of chain length.");                                 
                            }
                            else
                            {
                                if ( t_objLinkage.getChildLinkageType() == LinkageType.DEOXY )
                                {
                                    if ( t_aOH.get(t_iPos) )
                                    {
                                        t_aOH.set(t_iPos,false);
                                    }
                                    else
                                    {
                                        this.m_aErrorList.add("Attache position of monosaccharide parent edge is not possible (DEOXY).");
                                    }
                                }
                                else if ( t_objLinkage.getChildLinkageType() == LinkageType.H_AT_OH )
                                {
                                    if ( t_aOH.get(t_iPos) )
                                    {
                                        t_aOH.set(t_iPos,false);
                                    }
                                    else
                                    {
                                        this.m_aErrorList.add("Attache position of monosaccharide parent edge is not possible (H_AT_OH).");
                                    }
                                }
                                else if ( t_objLinkage.getChildLinkageType() == LinkageType.H_LOSE )
                                {
                                    if ( t_aH.get(t_iPos) )
                                    {
                                        t_aH.set(t_iPos,false);
                                    }
                                    else
                                    {
                                        this.m_aErrorList.add("Attache position of monosaccharide parent edge is not possible (H_LOSE).");
                                    }
                                }
                                else if ( t_objLinkage.getChildLinkageType() == LinkageType.NONMONOSACCHARID )
                                {
                                    this.m_aErrorList.add("Nonmonosaccharide linkage tpyes are not allowed in monosaccharide parent edge.");                                 
                                }
                                else if ( t_objLinkage.getChildLinkageType() == LinkageType.UNVALIDATED )
                                {
                                    this.m_aErrorList.add("Unvalidated linkage tpyes are not allowed in monosaccharide parent edge.");
                                }
                            }
                        }
                        else if ( t_iPos < 1 && t_iPos != Linkage.UNKNOWN_POSITION )
                        {
                            this.m_aErrorList.add("Linkage positions smaller than 1 are not allowed.");                             
                        }
                    }
                }
            }
        }
        // do the same thing for child edges
        for (GlycoEdge t_edge : a_objMonosaccharid.getChildEdges())
        {
            for (Iterator<Linkage> t_iterEdge = t_edge.getGlycosidicLinkages().iterator(); t_iterEdge.hasNext();)
            {
                t_objLinkage = t_iterEdge.next();
                if ( t_objLinkage.getParentLinkages().size() == 1 )
                {
                    for (Iterator<Integer> t_iterPos = t_objLinkage.getParentLinkages().iterator(); t_iterPos.hasNext();)
                    {
                        t_iPos = t_iterPos.next();
                        if ( t_iPos > 0 )
                        {
                            if ( t_iPos > t_iChainLength )
                            {
                                this.m_aErrorList.add("Attache position of monosaccharide parent edge is out of chain length.");                                 
                            }
                            else
                            {
                                if ( t_objLinkage.getParentLinkageType() == LinkageType.DEOXY )
                                {
                                    if ( t_aOH.get(t_iPos) )
                                    {
                                        t_aOH.set(t_iPos,false);
                                    }
                                    else
                                    {
                                        this.m_aErrorList.add("Attache position of monosaccharide child edge is not possible (DEOXY).");
                                    }
                                }
                                else if ( t_objLinkage.getParentLinkageType() == LinkageType.H_AT_OH )
                                {
                                    if ( t_aOH.get(t_iPos) )
                                    {
                                        t_aOH.set(t_iPos,false);
                                    }
                                    else
                                    {
                                        this.m_aErrorList.add("Attache position of monosaccharide child edge is not possible (H_AT_OH).");
                                    }
                                }
                                else if ( t_objLinkage.getParentLinkageType() == LinkageType.H_LOSE )
                                {
                                    if ( t_aH.get(t_iPos) )
                                    {
                                        t_aH.set(t_iPos,false);
                                    }
                                    else
                                    {
                                        this.m_aErrorList.add("Attache position of monosaccharide child edge is not possible (H_LOSE).");
                                    }
                                }
                                else if ( t_objLinkage.getParentLinkageType() == LinkageType.NONMONOSACCHARID )
                                {
                                    this.m_aErrorList.add("Nonmonosaccharide linkage tpyes are not allowed in monosaccharide parent edge.");                                 
                                }
                                else if ( t_objLinkage.getChildLinkageType() == LinkageType.UNVALIDATED )
                                {
                                    this.m_aErrorList.add("Unvalidated linkage tpyes are not allowed in monosaccharide parent edge.");
                                }
                            }
                        }
                        else if ( t_iPos < 1 && t_iPos != Linkage.UNKNOWN_POSITION )
                        {
                            this.m_aErrorList.add("Linkage positions smaller than 1 are not allowed.");                             
                        }
                    }
                }
            }
        }
        // now we test if the positions of alternative linkages are still available
        if ( a_objMonosaccharid.getParentEdge() != null )
        {
            for (Iterator<Linkage> t_iterEdge = a_objMonosaccharid.getParentEdge().getGlycosidicLinkages().iterator(); t_iterEdge.hasNext();)
            {
                t_objLinkage = t_iterEdge.next();
                if ( t_objLinkage.getChildLinkages().size() > 1 )
                {
                    for (Iterator<Integer> t_iterPos = t_objLinkage.getChildLinkages().iterator(); t_iterPos.hasNext();)
                    {
                        t_iPos = t_iterPos.next();
                        if ( t_iPos > 0 )
                        {
                            if ( t_iPos > t_iChainLength )
                            {
                                this.m_aErrorList.add("Alternative attache position of monosaccharide parent edge is out of chain length.");                                 
                            }
                            else
                            {
                                if ( t_objLinkage.getChildLinkageType() == LinkageType.DEOXY )
                                {
                                    if ( !t_aOH.get(t_iPos) )
                                    {
                                        this.m_aErrorList.add("Alternative attache position of monosaccharide parent edge is not possible (DEOXY).");
                                    }
                                }
                                else if ( t_objLinkage.getChildLinkageType() == LinkageType.H_AT_OH )
                                {
                                    if ( !t_aOH.get(t_iPos) )
                                    {
                                        this.m_aErrorList.add("Alternative attache position of monosaccharide parent edge is not possible (H_AT_OH).");
                                    }
                                }
                                else if ( t_objLinkage.getChildLinkageType() == LinkageType.H_LOSE )
                                {
                                    if ( !t_aH.get(t_iPos) )
                                    {
                                        this.m_aErrorList.add("Alternative attache position of monosaccharide parent edge is not possible (H_LOSE).");
                                    }
                                }
                                else if ( t_objLinkage.getChildLinkageType() == LinkageType.NONMONOSACCHARID )
                                {
                                    this.m_aErrorList.add("Nonmonosaccharide linkage tpyes are not allowed in monosaccharide parent edge.");                                 
                                }
                                else if ( t_objLinkage.getChildLinkageType() == LinkageType.UNVALIDATED )
                                {
                                    this.m_aErrorList.add("Unvalidated linkage tpyes are not allowed in monosaccharide parent edge.");
                                }
                            }
                        }
                        else if ( t_iPos < 1 && t_iPos != Linkage.UNKNOWN_POSITION )
                        {
                            this.m_aErrorList.add("Linkage positions smaller than 1 are not allowed.");                             
                        }
                    }
                }
            }
        }
        for (GlycoEdge t_edge : a_objMonosaccharid.getChildEdges())
        {
            for (Iterator<Linkage> t_iterEdge = t_edge.getGlycosidicLinkages().iterator(); t_iterEdge.hasNext();)
            {
                t_objLinkage = t_iterEdge.next();
                if ( t_objLinkage.getParentLinkages().size() > 1 )
                {
                    for (Iterator<Integer> t_iterPos = t_objLinkage.getParentLinkages().iterator(); t_iterPos.hasNext();)
                    {
                        t_iPos = t_iterPos.next();
                        if ( t_iPos > 0 )
                        {
                            if ( t_iPos > t_iChainLength )
                            {
                                this.m_aErrorList.add("Alternative attache position of monosaccharide parent edge is out of chain length.");                                 
                            }
                            else
                            {
                                if ( t_objLinkage.getParentLinkageType() == LinkageType.DEOXY )
                                {
                                    if ( !t_aOH.get(t_iPos) )
                                    {
                                        this.m_aErrorList.add("Alternative attache position of monosaccharide child edge is not possible (DEOXY).");
                                    }
                                }
                                else if ( t_objLinkage.getParentLinkageType() == LinkageType.H_AT_OH )
                                {
                                    if ( !t_aOH.get(t_iPos) )
                                    {
                                        this.m_aErrorList.add("Alternative attache position of monosaccharide child edge is not possible (H_AT_OH).");
                                    }
                                }
                                else if ( t_objLinkage.getParentLinkageType() == LinkageType.H_LOSE )
                                {
                                    if ( !t_aH.get(t_iPos) )
                                    {
                                        this.m_aErrorList.add("Alternative attache position of monosaccharide child edge is not possible (H_LOSE).");
                                    }
                                }
                                else if ( t_objLinkage.getParentLinkageType() == LinkageType.NONMONOSACCHARID )
                                {
                                    this.m_aErrorList.add("Nonmonosaccharide linkage tpyes are not allowed in monosaccharide parent edge.");                                 
                                }
                                else if ( t_objLinkage.getChildLinkageType() == LinkageType.UNVALIDATED )
                                {
                                    this.m_aErrorList.add("Unvalidated linkage tpyes are not allowed in monosaccharide parent edge.");
                                }
                            }
                        }
                        else if ( t_iPos < 1 && t_iPos != Linkage.UNKNOWN_POSITION )
                        {
                            this.m_aErrorList.add("Linkage positions smaller than 1 are not allowed.");                             
                        }
                    }
                }
            }
        }

        // test ob linkage richtung stimmt. 
        for (GlycoEdge t_edge : a_objMonosaccharid.getChildEdges())
        {
            this.validateDirection(t_edge);
        }




        // test if parent edge is a ms-ms linkage
        if ( a_objMonosaccharid.getParentEdge() != null )
        {
            this.testLinkageType(a_objMonosaccharid.getParentNode(), a_objMonosaccharid.getParentEdge(), a_objMonosaccharid);
        }      

    }


    private boolean validateDirection(GlycoEdge t_edge) throws GlycoVisitorException {

        GlycoVisitorNodeType t_gvis = new GlycoVisitorNodeType();
        if (t_gvis.isMonosaccharide(t_edge.getChild())){

            Monosaccharide t_mono = t_gvis.getMonosaccharide(t_edge.getChild());

            ArrayList <Integer>  t_aIntKeto= new ArrayList <Integer>();

            for (Iterator<Modification> iter3 = t_mono.getModification().iterator(); iter3.hasNext();) 
            {
                Modification element3 = iter3.next();           
                if (element3.getModificationType()==ModificationType.KETO)
                {
                    t_aIntKeto.add(element3.getPositionOne());
                }
            }

            if (t_aIntKeto.size()==0){
                t_aIntKeto.add(1);
            }

            for (Linkage t_lin : t_edge.getGlycosidicLinkages()){

                for (Integer i : t_lin.getChildLinkages()){

                    if (t_aIntKeto.contains(i))
                    {
                        return true;
                    }

                }

            }
            this.m_aErrorList.add("Child parent relationship (direction of linkages) broken");
            return false;
        }

        else if (t_gvis.isMonosaccharide(t_edge.getParent())){


            Monosaccharide t_mono = t_gvis.getMonosaccharide(t_edge.getParent());

            ArrayList <Integer>  t_aIntKeto= new ArrayList <Integer>();

            for (Iterator<Modification> iter3 = t_mono.getModification().iterator(); iter3.hasNext();) 
            {
                Modification element3 = iter3.next();           
                if (element3.getModificationType()==ModificationType.KETO)
                {
                    t_aIntKeto.add(element3.getPositionOne());
                }
            }

            if (t_aIntKeto.size()==0){
                t_aIntKeto.add(1);
            }

            for (Linkage t_lin : t_edge.getGlycosidicLinkages()){

                for (Integer i : t_lin.getParentLinkages()){

                    if (t_aIntKeto.contains(i))
                    {
                        this.m_aErrorList.add("Parent child relationship (direction of linkages) broken.");
                        return false;
                    }

                }

            }
            return true;
        }
        return true;        
    }



    public void clear() 
    {
        this.m_aErrorList.clear();
        this.m_aWarningList.clear();
        this.m_aEdge.clear();
    }
}


