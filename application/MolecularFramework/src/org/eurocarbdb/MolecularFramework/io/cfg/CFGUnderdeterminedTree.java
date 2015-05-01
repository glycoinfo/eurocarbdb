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
package org.eurocarbdb.MolecularFramework.io.cfg;

import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;

/**
* @author rene
*
*/
public class CFGUnderdeterminedTree
{
    private UnderdeterminedSubTree m_objTree = null;
    private boolean m_bAdded = false;
    private Integer m_iID = 0;
    
    public void setId(Integer a_iID)
    {
        this.m_iID = a_iID;
    }
    
    public Integer getId()
    {
        return this.m_iID;
    }
    
    public void setTree(UnderdeterminedSubTree a_objNode)
    {
        this.m_objTree = a_objNode;
    }
    
    public UnderdeterminedSubTree getTree()
    {
        return this.m_objTree;
    }
    
    public boolean isAdded()
    {
        return this.m_bAdded;
    }
    
    public void setAdded(boolean a_bAdd)
    {
        this.m_bAdded = a_bAdd;
    }
}
