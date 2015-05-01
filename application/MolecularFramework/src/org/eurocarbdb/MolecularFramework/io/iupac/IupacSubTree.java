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
package org.eurocarbdb.MolecularFramework.io.iupac;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;

/**
* @author rene
*
*/
public class IupacSubTree
{
    private GlycoNode m_objNode = null;
    private GlycoEdge m_objEdge = null;
    
    public void setGlycoEdge(GlycoEdge a_objEdge)
    {
        this.m_objEdge = a_objEdge;
    }
    
    public GlycoEdge getGlycoEdge()
    {
        return this.m_objEdge;
    }

    public void setGlycoNode(GlycoNode a_objNode)
    {
        this.m_objNode = a_objNode;
    }
    
    public GlycoNode getGlycoNode()
    {
        return this.m_objNode;
    }
    
    public void addLinkage(Linkage a_objLinkage) throws GlycoconjugateException
    {
        this.m_objEdge.addGlycosidicLinkage(a_objLinkage);
    }
}
