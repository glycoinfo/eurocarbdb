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
package org.eurocarbdb.MolecularFramework.io.namespace;

import java.util.HashMap;

import org.eurocarbdb.MolecularFramework.sugar.GlycoGraphAlternative;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;

/**
* @author Logan
*
*/
public class AlternativeMapping 
{
    private SugarUnitAlternative m_objOriginal;
    private SugarUnitAlternative m_objCopy;
    private HashMap<GlycoNode,GlycoNode> m_hResidueMap;
    private GlycoGraphAlternative m_objGraphOriginal;
    private GlycoGraphAlternative m_objGraphCopy;
    
    public AlternativeMapping(SugarUnitAlternative a_objOriginal, GlycoGraphAlternative a_objGraphOrignal,
            SugarUnitAlternative a_objCopy, GlycoGraphAlternative a_objGraphCopy,
            HashMap<GlycoNode,GlycoNode> a_hResidueMap)
    {
        this.m_objOriginal = a_objOriginal;
        this.m_objCopy = a_objCopy;
        this.m_hResidueMap = a_hResidueMap;
        this.m_objGraphCopy = a_objGraphCopy;
        this.m_objGraphOriginal = a_objGraphOrignal;
    }
    
    public SugarUnitAlternative getOriginal()
    {
        return this.m_objOriginal;
    }
    
    public SugarUnitAlternative getCopy()
    {
        return this.m_objCopy;
    }
    
    public HashMap<GlycoNode,GlycoNode> getMapping()
    {
        return this.m_hResidueMap;
    }
    
    public GlycoGraphAlternative getGraphOriginal()
    {
        return this.m_objGraphOriginal;
    }
    
    public GlycoGraphAlternative getGraphCopy()
    {
        return this.m_objGraphCopy;
    }
}
