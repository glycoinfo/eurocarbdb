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

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;



public interface GlycoVisitor 
{    
    public void visit     ( Monosaccharide            a_objMonosaccharid  ) throws GlycoVisitorException;
    public void visit     ( NonMonosaccharide         a_objResidue           ) throws GlycoVisitorException;
    public void visit     ( SugarUnitRepeat           a_objRepeat         ) throws GlycoVisitorException;
    public void visit     ( Substituent               a_objSubstituent    ) throws GlycoVisitorException;
    public void visit     ( SugarUnitCyclic           a_objCyclic         ) throws GlycoVisitorException;
    public void visit     ( SugarUnitAlternative      a_objAlternative    ) throws GlycoVisitorException;
    public void visit      ( UnvalidatedGlycoNode       a_objUnvalidated       ) throws GlycoVisitorException;
    public void visit      ( GlycoEdge                   a_objLinkage          ) throws GlycoVisitorException;
    
    public void start      ( Sugar                       a_objSugar          ) throws GlycoVisitorException;
    
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException;
    
    public void clear();
}