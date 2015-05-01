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
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserSimple;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
* Normalize the linkage types according to IUPAC (parsers)
* Replaces only the LinkageType.NONMONOSACCHARID (if necessary) 
*  
* @author rene
*
*/
public class GlycoVisitorLinkageTypeNormalisation implements GlycoVisitor
{
    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.NonMonosaccharide)
     */
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException 
    {
        // nothing to do
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.Linkage)
     */
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException 
    {
        try 
        {
            for (Iterator<Linkage> t_iterLinkages = a_objLinkage.getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();) 
            {
                Linkage t_objLinkage = t_iterLinkages.next();
                if ( t_objLinkage.getChildLinkageType() == LinkageType.UNVALIDATED && 
                        t_objLinkage.getParentLinkageType() == LinkageType.UNVALIDATED )
                {
                    GlycoNode t_objParent = a_objLinkage.getParent();
                    GlycoNode t_objChild = a_objLinkage.getChild();
                    if ( t_objChild.getClass() == SugarUnitCyclic.class )
                    {
                        SugarUnitCyclic t_objUnit = (SugarUnitCyclic) t_objChild;
                        t_objChild = t_objUnit.getCyclicStart();
                    }
                    if ( t_objParent.getClass() == Monosaccharide.class )
                    {
                        if ( t_objChild.getClass() == Monosaccharide.class )
                        {
                            t_objLinkage.setParentLinkageType( LinkageType.H_AT_OH );
                            t_objLinkage.setChildLinkageType(LinkageType.DEOXY);
                        }
                        else if ( t_objChild.getClass() == Substituent.class )
                        {
                            throw new GlycoVisitorException("Unvalide linkage type between monosaccharide and substitutent.");
                        }
                        else if ( t_objChild.getClass() == SugarUnitAlternative.class )
                        {
                            // TODO: 
                            t_objLinkage.setParentLinkageType( LinkageType.UNKNOWN );
                            t_objLinkage.setChildLinkageType(LinkageType.UNKNOWN );                        
                        }
                        else if ( t_objChild.getClass() == SugarUnitRepeat.class )
                        {
                            t_objLinkage.setParentLinkageType( LinkageType.H_AT_OH );
                            t_objLinkage.setChildLinkageType(LinkageType.NONMONOSACCHARID);
                        }
                        else if ( t_objChild.getClass() == UnvalidatedGlycoNode.class )
                        {
                            throw new GlycoVisitorException("Unvalide linkage type between monosaccharide and unvalidated residues.");
                        }
                        else
                        {
                            // non ms
                            t_objLinkage.setParentLinkageType( LinkageType.UNKNOWN );
                            t_objLinkage.setChildLinkageType(LinkageType.NONMONOSACCHARID);
                        }
                    }
                    else if ( t_objParent.getClass() ==  Substituent.class )
                    {
                        if ( t_objChild.getClass() == Monosaccharide.class )
                        {
                            throw new GlycoVisitorException("Unvalide linkage type between substitutent and monosaccharide.");
                        }
                        else if ( t_objChild.getClass() == Substituent.class )
                        {
                            throw new GlycoVisitorException("Unvalide linkage type between substitutent and substitutent.");
                        }
                        else if ( t_objChild.getClass() == SugarUnitAlternative.class )
                        {
                            throw new GlycoVisitorException("Unvalide linkage type between substitutent and alternative residues.");
                        }
                        else if ( t_objChild.getClass() == SugarUnitRepeat.class )
                        {
                            t_objLinkage.setParentLinkageType( LinkageType.NONMONOSACCHARID );
                            t_objLinkage.setChildLinkageType(LinkageType.NONMONOSACCHARID);
                        }
                        else if ( t_objChild.getClass() == UnvalidatedGlycoNode.class )
                        {
                            throw new GlycoVisitorException("Unvalide linkage type between monosaccharide and unvalidated residues.");
                        }
                        else
                        {
                            // non ms
                            t_objLinkage.setParentLinkageType( LinkageType.NONMONOSACCHARID );
                            t_objLinkage.setChildLinkageType(LinkageType.NONMONOSACCHARID);
                        }
                    }
                    else if ( t_objParent.getClass() == SugarUnitAlternative.class )
                    {
                        // TODO:            
                        t_objLinkage.setParentLinkageType( LinkageType.UNKNOWN );
                        t_objLinkage.setChildLinkageType(LinkageType.UNKNOWN );                        
                    }
                    else if ( t_objParent.getClass() == SugarUnitRepeat.class )
                    {
                        if ( t_objChild.getClass() == Monosaccharide.class )
                        {
                            t_objLinkage.setParentLinkageType( LinkageType.NONMONOSACCHARID );
                            t_objLinkage.setChildLinkageType(LinkageType.DEOXY);
                        }
                        else if ( t_objChild.getClass() == Substituent.class )
                        {
                            t_objLinkage.setParentLinkageType( LinkageType.NONMONOSACCHARID );
                            t_objLinkage.setChildLinkageType(LinkageType.NONMONOSACCHARID);
                        }
                        else if ( t_objChild.getClass() == SugarUnitAlternative.class )
                        {
                            throw new GlycoVisitorException("Unvalide linkage type between monosaccharide and alternative residues.");
                        }
                        else if ( t_objChild.getClass() == SugarUnitRepeat.class )
                        {
                            t_objLinkage.setParentLinkageType( LinkageType.NONMONOSACCHARID);
                            t_objLinkage.setChildLinkageType(LinkageType.NONMONOSACCHARID);
                        }
                        else if ( t_objChild.getClass() == UnvalidatedGlycoNode.class )
                        {
                            throw new GlycoVisitorException("Unvalide linkage type between monosaccharide and unvalidated residues.");
                        }
                        else
                        {
                            // non ms
                            t_objLinkage.setParentLinkageType( LinkageType.NONMONOSACCHARID);
                            t_objLinkage.setChildLinkageType(LinkageType.NONMONOSACCHARID);
                        }
                    }
                    else
                    {
                        // parent is non ms
                        if ( t_objChild.getClass() == Monosaccharide.class )
                        {
                            t_objLinkage.setParentLinkageType( LinkageType.NONMONOSACCHARID );
                            t_objLinkage.setChildLinkageType(LinkageType.UNKNOWN);
                        }
                        else if ( t_objChild.getClass() == Substituent.class )
                        {
                            t_objLinkage.setParentLinkageType( LinkageType.NONMONOSACCHARID);
                            t_objLinkage.setChildLinkageType(LinkageType.NONMONOSACCHARID);
                        }
                        else if ( t_objChild.getClass() == SugarUnitAlternative.class )
                        {
                            throw new GlycoVisitorException("Unvalide linkage type between nonmonosaccharide and alternative residues.");
                        }
                        else if ( t_objChild.getClass() == SugarUnitRepeat.class )
                        {
                            t_objLinkage.setParentLinkageType( LinkageType.NONMONOSACCHARID);
                            t_objLinkage.setChildLinkageType(LinkageType.NONMONOSACCHARID);
                        }
                        else if ( t_objChild.getClass() == UnvalidatedGlycoNode.class )
                        {
                            throw new GlycoVisitorException("Unvalide linkage type between monosaccharide and unvalidated residues.");
                        }
                        else
                        {
                            // non ms
                            t_objLinkage.setParentLinkageType( LinkageType.NONMONOSACCHARID);
                            t_objLinkage.setChildLinkageType(LinkageType.NONMONOSACCHARID);
                        }
                    }
                }
                else if ( t_objLinkage.getChildLinkageType() == LinkageType.UNVALIDATED || 
                        t_objLinkage.getParentLinkageType() == LinkageType.UNVALIDATED )
                    
                {
                    throw new GlycoVisitorException("Unvalide linkage type composition.");
                }
            }
        } 
        catch (GlycoconjugateException e) 
        {
            throw new GlycoVisitorException(e.getMessage(),e);
        }
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#visit(de.glycosciences.MolecularFrameWork.sugar.SugarUnitRepeat)
     */
    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException 
    {
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objRepeate);
        if ( a_objRepeate.getRepeatLinkage() != null )
        {
            this.visit( a_objRepeate.getRepeatLinkage() );
        }
        // spezial trees
        for (Iterator<UnderdeterminedSubTree> t_iterTrees = a_objRepeate.getUndeterminedSubTrees().iterator(); t_iterTrees.hasNext();) 
        {
            UnderdeterminedSubTree t_objTree = t_iterTrees.next();
            t_objTraverser.traverseGraph(t_objTree);
        }
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#start(de.glycosciences.MolecularFrameWork.sugar.Sugar)
     */
    public void start(Sugar a_objSugar) throws GlycoVisitorException 
    {
        this.clear();
        // traverse Sugar and fill Residue Hashmap
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        // spezial trees
        for (Iterator<UnderdeterminedSubTree> t_iterTrees = a_objSugar.getUndeterminedSubTrees().iterator(); t_iterTrees.hasNext();) 
        {
            UnderdeterminedSubTree t_objTree = t_iterTrees.next();
            t_objTraverser.traverseGraph(t_objTree);
        }
    }

    /**
     * @throws GlycoVisitorException 
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#getTraverser(de.glycosciences.MolecularFrameWork.util.GlycoVisitor)
     */
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {
        return new GlycoTraverserSimple(a_objVisitor);
    }

    /**
     * @see de.glycosciences.MolecularFrameWork.util.GlycoVisitor#clear()
     */
    public void clear() 
    {
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException 
    {
        // nothing to do        
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.Substituent)
     */
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException 
    {
        // nothing to do
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException 
    {
        // nothing to do
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        for (Iterator<GlycoGraphAlternative> t_iterAltGraph = a_objAlternative.getAlternatives().iterator(); t_iterAltGraph.hasNext();) 
        {
            t_objTraverser.traverseGraph(t_iterAltGraph.next());    
        }
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode)
     */
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException 
    {
        // nothing to do
    }
}
