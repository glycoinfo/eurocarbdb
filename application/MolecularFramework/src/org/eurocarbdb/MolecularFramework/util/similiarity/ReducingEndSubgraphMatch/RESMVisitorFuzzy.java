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
package org.eurocarbdb.MolecularFramework.util.similiarity.ReducingEndSubgraphMatch;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.analytical.monosaccharideScalableName.MonosaccharideScalableName;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
* @author sherget
*
*/
public class RESMVisitorFuzzy implements GlycoVisitor {

    String m_sName;
    RESMFuzzy m_oMCSFuzzy = null;
    
    public RESMVisitorFuzzy (RESMFuzzy m){
        this.m_oMCSFuzzy=m;
    }
    
    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException {
        MonosaccharideScalableName t_oNameExporter = new MonosaccharideScalableName ();
        t_oNameExporter.reset();
        t_oNameExporter.displayAllModifications(this.m_oMCSFuzzy.m_BModifications);
        t_oNameExporter.displayAnomer(this.m_oMCSFuzzy.m_BAnomer);
        t_oNameExporter.displayRingsize(this.m_oMCSFuzzy.m_BRingsize);
        t_oNameExporter.displayStereochemistry(this.m_oMCSFuzzy.m_BStereochemistry);
        t_oNameExporter.displaySuperclass(this.m_oMCSFuzzy.m_BSuperclass);
        t_oNameExporter.displayAllSubstituent(false);
        t_oNameExporter.displaySubstituentFatherLinkage(false);
        t_oNameExporter.displaySubstituentChildLinkage(false);
        
        m_sName = t_oNameExporter.getName(a_objMonosaccharid);
        
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.NonMonosaccharide)
     */
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException {
        
        
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.SugarUnitRepeat)
     */
    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.Substituent)
     */
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException {
        m_sName=a_objSubstituent.getSubstituentType().getName();
        
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.UnvalidatedGlycoNode)
     */
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#visit(org.glycomedb.MolecularFrameWork.sugar.GlycoEdge)
     */
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#start(org.glycomedb.MolecularFrameWork.sugar.Sugar)
     */
    public void start(Sugar a_objSugar) throws GlycoVisitorException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#getTraverser(org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor)
     */
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glycomedb.MolecularFrameWork.util.visitor.GlycoVisitor#clear()
     */
    public void clear() {
        this.m_sName="";
        
    }
    
    public String getName(){
        return this.m_sName;
    }

}
