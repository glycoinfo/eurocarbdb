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

package org.eurocarbdb.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import org.eurocarbdb.MolecularFramework.sugar.*;
import org.eurocarbdb.MolecularFramework.util.visitor.*;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.GlycoCTTraverser;

import org.apache.log4j.Logger;



public class CompositionVisitor implements GlycoVisitor
{
    // ----------------------- static variables ---------------------
    
    protected static final Logger log = Logger.getLogger( CompositionVisitor.class.getName() );

    // ----------------------- object variables ---------------------
    
    private HashMap<String, Integer>    m_hashComposition = new HashMap<String, Integer>();
    private GlycoTraverser                 m_objTraverser;

    // ------------------------- methods ----------------------------    
    
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException 
    {        
        return new GlycoCTTraverser(a_objVisitor);
    }

    public void clear() 
    {
        m_hashComposition.clear();
    }
    
    protected void increment(String strComponent)
    {
        int i = 1;
    
        if (m_hashComposition.containsKey(strComponent))
        {
            i = (Integer)(m_hashComposition.get(strComponent)).intValue() + 1;
        }
        
        m_hashComposition.put(strComponent, new Integer(i));
    }
    
    public void start(Sugar a_objSugar) throws GlycoVisitorException 
    {
        this.clear();
        this.m_objTraverser = this.getTraverser(this);
        this.m_objTraverser.traverseGraph(a_objSugar);
    }

    public HashMap<String, Integer> getCompositionMap()
    {
        return this.m_hashComposition;
    }
    
    public void visit(Monosaccharide a_objMonosaccharide) throws GlycoVisitorException 
    {
        if ( this.m_objTraverser.getState() == GlycoTraverser.ENTER )
        {
            // count the superclass
            increment(a_objMonosaccharide.getSuperclass().getName());

            // count the basetype(s)
            ArrayList<BaseType> t_aBaseType = a_objMonosaccharide.getBaseType();
            int t_iMax = t_aBaseType.size();
            BaseType t_objBaseType;
            
            for (int t_iCounter = 0; t_iCounter < t_iMax; t_iCounter++) 
            {
                t_objBaseType = t_aBaseType.get(t_iCounter);
                increment(t_objBaseType.getName());
            }
            
        }
    }

    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException 
    {
    }

    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException 
    {
    }

    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException 
    {
    }

    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException 
    {
    }

    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException 
    {
    }

    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException 
    {
    }

    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException 
    {
    }    
}
