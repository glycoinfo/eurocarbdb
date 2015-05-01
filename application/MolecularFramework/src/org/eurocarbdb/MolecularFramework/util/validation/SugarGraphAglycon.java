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
package org.eurocarbdb.MolecularFramework.util.validation;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;

/**
* @author rene
*
*/
public class SugarGraphAglycon
{
    private GlycoNode m_objAglyca = null;
    private GlycoNode m_objConnectedNode = null;
    private GlycoEdge m_objAglycaEdge = null;
    
    public SugarGraphAglycon( GlycoNode a_objAglyca , GlycoNode a_objReducing, GlycoEdge a_objAglycaEdge)
    {
        super();
        this.m_objAglyca = a_objAglyca;
        this.m_objConnectedNode = a_objReducing;
        this.m_objAglycaEdge = a_objAglycaEdge;
    }

    public void setAglyca(GlycoNode a_strAglyca)
    {
        this.m_objAglyca = a_strAglyca;
    }
    
    public GlycoNode getAglyca()
    {
        return this.m_objAglyca;
    }
    
    public void setAglycaEdge(GlycoEdge a_objEdge)
    {
        this.m_objAglycaEdge = a_objEdge;
    }
    
    public GlycoEdge getAglycaEdge()
    {
        return this.m_objAglycaEdge;
    }
    
    public void setConnectedGlycoNode(GlycoNode a_objNode)
    {
        this.m_objConnectedNode = a_objNode;
    }
    
    public GlycoNode getConnectedGlycoNode()
    {
        return this.m_objConnectedNode;
    }
}
