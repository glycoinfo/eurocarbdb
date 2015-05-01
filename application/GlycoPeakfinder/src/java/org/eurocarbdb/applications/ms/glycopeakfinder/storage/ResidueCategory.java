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
package org.eurocarbdb.applications.ms.glycopeakfinder.storage;

import java.util.ArrayList;


/**
* @author Logan
*
*/
public class ResidueCategory 
{
    private String m_strName = "";
    private ArrayList<MassResidue> m_aResidues = new ArrayList<MassResidue>();
    private String m_strID = "";
    
    public void setId(String a_strID)
    {
        this.m_strID = a_strID;
    }
    
    public String getId()
    {
        return this.m_strID;
    }
    
    public void setName(String a_strName)
    {
        this.m_strName = a_strName;
    }
    
    public String getName()
    {
        return this.m_strName;
    }
    
    public void setResidues(ArrayList<MassResidue> a_aResidues)
    {
        this.m_aResidues = a_aResidues;
    }
    
    public ArrayList<MassResidue> getResidues()
    {
        return this.m_aResidues;
    }
}
