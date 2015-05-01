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
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserTree;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConversion;
import org.eurocarbdb.resourcesdb.io.MonosaccharideExchangeObject;
import org.eurocarbdb.resourcesdb.io.SubstituentExchangeObject;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbBaseType;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbMonosaccharide;


/**
* iterieren ueber alle residues ==> aufbauen einer hashmap ==> etablieren der Linkage
* 
* @author Logan
*
*/
public class GlycoVisitorToGlycoCT implements GlycoVisitor
{
    protected Sugar m_objNewSugar = null;
    protected GlycoGraph m_objUnit = null;
    protected SugarUnitRepeat m_objRepeat = null;
    protected GlycoEdge m_objInternalOriginalEdge = null;
    protected UnderdeterminedSubTree m_objSubTree = null;
    protected MonosaccharideConversion m_objConverter = null;
    protected GlycoGraphAlternative m_objAlternative = null;
    protected HashMap<GlycoNode,GlycoNode> m_hashResidues = new HashMap<GlycoNode,GlycoNode>();
    protected boolean m_bStrict = true;
    protected GlycanNamescheme m_strSchema = GlycanNamescheme.GLYCOSCIENCES;
    protected GlycoTraverser m_objTraverser = null;
    protected ArrayList<AlternativeMapping> m_aAlternative = new ArrayList<AlternativeMapping>(); 
    protected GlycoEdge m_objInternal = null;
    protected GlycoVisitorFuseSubstituent m_visFuse = new GlycoVisitorFuseSubstituent();
    protected GlycoVisitorSubstituentUnknownPosition m_visSubstPostion = new GlycoVisitorSubstituentUnknownPosition();
    protected boolean m_bFusion = false;
    protected boolean m_bSubstPosition = false;
    
    public void setUseFusion(boolean a_bValue)
    {
        this.m_bFusion = a_bValue;
    }
    
    public void setUseSubstPosition(boolean a_bValue)
    {
        this.m_bSubstPosition = a_bValue;
    }

    public GlycoVisitorFuseSubstituent getFusionVisitor()
    {
        return this.m_visFuse;
    }
    
    public GlycoVisitorToGlycoCT(MonosaccharideConversion a_objTranslator) 
    {
        super();
        this.m_objConverter = a_objTranslator;
    }

    public GlycoVisitorToGlycoCT(MonosaccharideConversion a_objTranslator,GlycanNamescheme a_strSchema) 
    {
        super();
        this.m_objConverter = a_objTranslator;
        this.m_strSchema = a_strSchema;
    }

    /**
     * @return
     * @throws GlycoVisitorException 
     */
    public Sugar getNormalizedSugar() throws GlycoVisitorException 
    {
        return this.m_objNewSugar;
    }

    public void setUseStrict( boolean a_bStrict )
    {
        this.m_bStrict = a_bStrict;
    }
     
    public void setMonosaccharideConversion( MonosaccharideConversion a_objConverter )
    {
        this.m_objConverter = a_objConverter;
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            try
            {
                // copy ms
                Monosaccharide t_objMS = a_objMonosaccharid.copy();
                this.m_hashResidues.put( a_objMonosaccharid , t_objMS );
                this.m_objUnit.addNode(t_objMS);
                // copy linkage
                this.copyParentLinkage(a_objMonosaccharid.getParentEdge(),t_objMS);
            } 
            catch (GlycoconjugateException e)
            {
                throw new GlycoVisitorException("Could not create monosaccharide : " + e.getMessage(),e);
            }
        }
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.Linkage)
     */
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException 
    {
        // nothing to do        
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.SugarUnitRepeat)
     */
    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            GlycoVisitorToGlycoCT t_objVisitor = new GlycoVisitorToGlycoCT( this.m_objConverter,this.m_strSchema );
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

    /**
     * @throws  
     * @see de.glycosciences.MolecularFrameWork.util.SugarVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.NonMonosaccharide)
     */
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
        this.m_hashResidues.clear();
        this.m_objUnit = null;
        this.m_objRepeat = null;
        this.m_objNewSugar = null;
        this.m_objSubTree = null;
        this.m_objAlternative = null;
        this.m_objInternal = null;
        this.m_objInternalOriginalEdge = null;
        this.m_aAlternative.clear();
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
                    GlycoVisitorToGlycoCT t_objVisitor = new GlycoVisitorToGlycoCT( this.m_objConverter,this.m_strSchema );
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

    /**
     * @param sugar
     * @throws GlycoVisitorException 
     */
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
                    GlycoVisitorToGlycoCT t_objVisitor = new GlycoVisitorToGlycoCT( this.m_objConverter,this.m_strSchema );
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

    /**
     * @param subtreeOriginal
     * @return
     * @throws GlycoVisitorException 
     */
    protected UnderdeterminedSubTree start(UnderdeterminedSubTree a_objSubtree) throws GlycoVisitorException 
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

    protected void copyParentLinkage(GlycoEdge a_objOrigin, GlycoNode a_objNode) throws GlycoVisitorException
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
                this.m_objUnit.addEdge(t_objParent,a_objNode,t_objNewLinkage);
            } 
            catch (GlycoconjugateException e) 
            {
                throw new GlycoVisitorException(e.getMessage(),e);
            }
        }
    }

    /**
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharides)
     */
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            try
            {
                // copy residue
                NonMonosaccharide t_objMS = a_objResidue.copy();
                this.m_hashResidues.put( a_objResidue , t_objMS );
                this.m_objUnit.addNode(t_objMS);
                // copy linkage
                this.copyParentLinkage(a_objResidue.getParentEdge(),t_objMS);
            } 
            catch (GlycoconjugateException e)
            {
                throw new GlycoVisitorException("Could not create nonmonosaccharide : " + e.getMessage(),e);
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
            try
            {
                // copy residue
                Substituent t_objMS = a_objSubstituent.copy();
                this.m_hashResidues.put( a_objSubstituent , t_objMS );
                this.m_objUnit.addNode(t_objMS);
                // copy linkage
                this.copyParentLinkage(a_objSubstituent.getParentEdge(),t_objMS);
            } 
            catch (GlycoconjugateException e)
            {
                throw new GlycoVisitorException("Could not create substitutent : " + e.getMessage(),e);
            }
        }
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
                this.m_objUnit.addEdge(t_objParent,t_objStart,t_objEdge);
            } 
            catch (GlycoconjugateException e)
            {
                throw new GlycoVisitorException("Could not create cyclic residue : " + e.getMessage(),e);
            }
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
                GlycoVisitorToGlycoCT t_objVisitor = new GlycoVisitorToGlycoCT( this.m_objConverter,this.m_strSchema );
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

    protected Monosaccharide createMS(EcdbMonosaccharide a_objMsDB) throws GlycoconjugateException 
    {
        // anomer & superclass
        Monosaccharide t_objMS = new Monosaccharide( 
                Anomer.forSymbol( a_objMsDB.getAnomer().getSymbol().charAt(0) ),
                Superclass.forCAtomCount( a_objMsDB.getSuperclass().getNumberOfC()));
        // ring
        if ( a_objMsDB.getRingEnd() == EcdbMonosaccharide.UNKNOWN_RING )
        {
            t_objMS.setRingEnd(Monosaccharide.UNKNOWN_RING);
        }
        else
        {
            if ( a_objMsDB.getRingEnd() == EcdbMonosaccharide.OPEN_CHAIN )
            {
                t_objMS.setRingEnd(Monosaccharide.OPEN_CHAIN);
            }
            else
            {
                t_objMS.setRingEnd(a_objMsDB.getRingEnd());
            }
        }
        if ( a_objMsDB.getRingStart() == EcdbMonosaccharide.UNKNOWN_RING )
        {
            t_objMS.setRingStart(Monosaccharide.UNKNOWN_RING);
        }
        else
        {
            if ( a_objMsDB.getRingStart() == EcdbMonosaccharide.OPEN_CHAIN )
            {
                t_objMS.setRingStart(Monosaccharide.OPEN_CHAIN);
            }
            else
            {
                t_objMS.setRingStart(a_objMsDB.getRingStart());
            }
        }
        // basetype
        for (Iterator<EcdbBaseType> t_iterBasetype = a_objMsDB.getBaseTypeList().iterator(); t_iterBasetype.hasNext();) 
        {
            t_objMS.addBaseType(BaseType.forName(t_iterBasetype.next().getName()));            
        }
        // modification
        for (Iterator<EcdbModification> t_iterModification = a_objMsDB.getModificationList().iterator(); t_iterModification.hasNext();)
        {
            t_objMS.addModification( this.createModification(t_iterModification.next()));
        }
        return t_objMS;
    }

    protected Modification createModification(EcdbModification a_objModification) throws GlycoconjugateException 
    {
        if ( a_objModification.hasPositionTwo() )
        {
            return new Modification(a_objModification.getName(),a_objModification.getPositionOne(),a_objModification.getPositionTwo());
        }
        return new Modification(a_objModification.getName(),a_objModification.getPositionOne());
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
        throw new GlycoconjugateException(
                String.format("SubstituentExchangeObject does not contain information for linkage %d at substituten %s.",a_iCounter,a_objSubstExchange.getName()));
    }

    /**
     * parent and child connections are not translated
     * @param alternative
     * @return
     * @throws GlycoVisitorException 
     */
    /**
     * parent and child connections are not translated
     * @param alternative
     * @return
     * @throws GlycoVisitorException 
     */
    protected GlycoGraphAlternative start(GlycoGraphAlternative a_objAlternative,SugarUnitAlternative a_objAlternativeUnit) throws GlycoVisitorException
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

    protected HashMap<GlycoNode,GlycoNode> getResidueMapping()
    {
        return this.m_hashResidues;
    }
    
    public void setNameScheme(GlycanNamescheme a_strSchema )
    {
        this.m_strSchema =a_strSchema; 
    }
}
