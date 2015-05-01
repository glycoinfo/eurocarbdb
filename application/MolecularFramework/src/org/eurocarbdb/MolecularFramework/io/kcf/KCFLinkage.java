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
package org.eurocarbdb.MolecularFramework.io.kcf;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;

/**
* @author rene
*
*/
public class KCFLinkage
{
    private int m_iPosOne;
    private int m_iPosTwo;
    private int m_iResOne;
    private int m_iResTwo;
    private KCFResidue m_objResidueOne;
    private KCFResidue m_objResidueTwo;
    
    public void setResidues(KCFResidue a_objOne, KCFResidue a_objTwo)
    {
        this.m_objResidueOne = a_objOne;
        this.m_objResidueTwo = a_objTwo;
    }
    
    public KCFLinkage (int a_iPosOne, int a_iPosTwo ,int a_iResOne, int a_iResTwo)
    {
        this.m_iPosOne = a_iPosOne;
        this.m_iPosTwo = a_iPosTwo;
        this.m_iResOne = a_iResOne;
        this.m_iResTwo = a_iResTwo;
    }
    
    public int getResidueOne()
    {
        return this.m_iResOne;
    }
    
    public int getResidueTwo()
    {
        return this.m_iResTwo;
    }
    
    public int getPositionOne()
    {
        return this.m_iPosOne;
    }

    public int getPositionTwo()
    {
        return this.m_iPosTwo;
    }

    /**
     * @param b
     * @return
     * @throws GlycoconjugateException 
     */
    public GlycoEdge getEdge(boolean a_bCorrectDirection) throws GlycoconjugateException 
    {
        GlycoEdge t_objEdge = new GlycoEdge();
        
        Linkage t_objLinkage = new Linkage();
        if ( a_bCorrectDirection )
        {
            t_objLinkage.addParentLinkage(this.m_iPosOne);
            t_objLinkage.addChildLinkage(this.m_iPosTwo);
        }
        else
        {
            t_objLinkage.addParentLinkage(this.m_iPosTwo);
            t_objLinkage.addChildLinkage(this.m_iPosOne);
        }        
        t_objEdge.addGlycosidicLinkage(t_objLinkage);        
        return t_objEdge;
    }
    
    public KCFResidue getKCFResidueOne()
    {
        return this.m_objResidueOne;
    }
    
    public KCFResidue getKCFResidueTwo()
    {
        return this.m_objResidueTwo;
    }
}