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
package org.eurocarbdb.MolecularFramework.io.kcf;

import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;

/**
* @author rene
*
*/
public class KCFResidue
{
    private UnvalidatedGlycoNode m_objResidue = null;
    private double m_dX = 0;
    private double m_dY = 0;
    private int m_iID = 0;
    
    public void init( UnvalidatedGlycoNode a_objResidue, double a_dX , double a_dY , int a_iId)
    {
        this.m_objResidue = a_objResidue;
        this.m_dX = a_dX;
        this.m_dY = a_dY;
        this.m_iID = a_iId;
    }
    
    public double getX()
    {
        return this.m_dX;
    }
    
    public double getY()
    {
        return this.m_dY;
    }
    
    public UnvalidatedGlycoNode getResidue()
    {
        return this.m_objResidue;
    }
    
    public int getId()
    {
        return this.m_iID;
    }
}
