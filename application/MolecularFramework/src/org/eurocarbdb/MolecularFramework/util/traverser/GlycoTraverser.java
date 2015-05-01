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
package org.eurocarbdb.MolecularFramework.util.traverser;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;



public abstract class GlycoTraverser  
{
    public static final int ENTER     = 0;
    public static final int LEAVE     = 1;
    public static final int RETURN     = 2;
    
    protected GlycoVisitor m_objVisitor = null;
    protected int m_iState = 0; 

    
    public GlycoTraverser ( GlycoVisitor a_objVisitor ) throws GlycoVisitorException
    {
        if ( a_objVisitor == null )
        {
            throw new GlycoVisitorException("Null visitor given to traverser");
        }
        this.m_objVisitor = a_objVisitor;
    }

    public abstract void traverse( GlycoNode a_objNode ) throws GlycoVisitorException;
    public abstract void traverse( GlycoEdge a_objEdge ) throws GlycoVisitorException;    

    public abstract void traverseGraph( GlycoGraph a_objSugar ) throws GlycoVisitorException;
    
    public int getState()
    {
        return this.m_iState;
    }
}
