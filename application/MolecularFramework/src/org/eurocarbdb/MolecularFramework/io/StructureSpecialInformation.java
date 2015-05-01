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
package org.eurocarbdb.MolecularFramework.io;

import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;

/**
* @author rene
*
*/
public class StructureSpecialInformation
{
    public static final int SUGAR          = 0;
    public static final int CYCLIC         = 1;
    public static final int REPEAT         = 2;
    
    private GlycoNode m_objTargetResidue = null;
    private Linkage m_objIncomingLinkage = null;
    private int m_iType = 0;
    private int m_iRepeatCountMin = -1;
    private int m_iRepeatCountMax = -1;
    private SugarUnitRepeat m_objRepeatBlock = null;
    private GlycoGraph m_objParentUnit = null;
    private StructureSpecialInformation m_objParentInfo = null;
    private boolean m_bClosed = false;

    /**
     * Cyclic 
     */
    public StructureSpecialInformation( GlycoNode objStart , Linkage objIncoming , StructureSpecialInformation a_objParent , GlycoGraph a_objParentUnit)
    {
        this.m_objTargetResidue = objStart;
        this.m_objIncomingLinkage = objIncoming;
        this.m_iType = StructureSpecialInformation.CYCLIC;
        this.m_objParentInfo = a_objParent;
        this.m_objParentUnit = a_objParentUnit;
        this.m_bClosed = false;
    }
    
    /**
     * Repeat 
     */
    public StructureSpecialInformation( GlycoNode objStart , Linkage objIncoming, int iRepeat , SugarUnitRepeat a_objRepeat , StructureSpecialInformation a_objParent , GlycoGraph a_objParentUnit )
    {
        this.m_objTargetResidue = objStart;
        this.m_objIncomingLinkage = objIncoming;
        this.m_iType = StructureSpecialInformation.REPEAT;
        this.m_iRepeatCountMin = iRepeat;
        this.m_iRepeatCountMax = iRepeat;
        this.m_objRepeatBlock = a_objRepeat;
        this.m_objParentInfo = a_objParent;
        this.m_objParentUnit = a_objParentUnit;
        this.m_bClosed = false;
    }

    /**
     * Repeat 2
     */
    public StructureSpecialInformation( GlycoNode objStart , Linkage objIncoming, int iRepeatMin , int iRepeatMax , SugarUnitRepeat a_objRepeat , StructureSpecialInformation a_objParent , GlycoGraph a_objParentUnit )
    {
        this.m_objTargetResidue = objStart;
        this.m_objIncomingLinkage = objIncoming;
        this.m_iType = StructureSpecialInformation.REPEAT;
        this.m_iRepeatCountMin = iRepeatMin;
        this.m_iRepeatCountMax = iRepeatMax;
        this.m_objRepeatBlock = a_objRepeat;
        this.m_objParentInfo = a_objParent;
        this.m_objParentUnit = a_objParentUnit;
        this.m_bClosed = false;
    }

    public GlycoNode getTarget()
    {
        return this.m_objTargetResidue;
    }
    
    public void setTarget(GlycoNode a_objTarget)
    {
        this.m_objTargetResidue = a_objTarget;
    }
    
    public Linkage getIncomingLinkage()
    {
        return this.m_objIncomingLinkage;
    }
    
    public int getType()
    {
        return this.m_iType;
    }
    
    public int getRepeatCountMin()
    {
        return this.m_iRepeatCountMin;
    }

    public int getRepeatCountMax()
    {
        return this.m_iRepeatCountMax;
    }
    

    public SugarUnitRepeat getRepeatBlock()
    {
        return this.m_objRepeatBlock;
    }
    
    public GlycoGraph getParentUnit()
    {
        return this.m_objParentUnit;
    }

    public StructureSpecialInformation getParentInfo()
    {
        return this.m_objParentInfo;
    }
    
    public boolean isClosed()
    {
        return this.m_bClosed;
    }
    
    public void close()
    {
        this.m_bClosed = true;    
    }
    
    
}
