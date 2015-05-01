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
package org.eurocarbdb.MolecularFramework.io.OGBI;

import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;

/**
* @author Logan
*
*/
public class OgbiResidue 
{
    public Monosaccharide m_objMS;
    public GlycoGraph m_objGraph;
    public boolean m_bUnderdeterminded;
    
    public OgbiResidue(Monosaccharide a_objMS,GlycoGraph a_objGraph,boolean a_bUnderdeterminded)
    {
        this.m_bUnderdeterminded = a_bUnderdeterminded;
        this.m_objMS = a_objMS;
        this.m_objGraph = a_objGraph;
    }    
}
