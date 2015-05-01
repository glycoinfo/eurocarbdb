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

/**
* @author Logan
*
*/
public class Compound 
{
    private String m_strID = "";
    private String m_strName = "";
    private String m_strAbbr = "";
    private boolean m_bUsed = false;

    public void setName(String a_strName)
    {
        this.m_strName = a_strName;
    }
    
    public String getName()
    {
        return this.m_strName;
    }
    
    public void setAbbr(String a_strAbbr)
    {
        this.m_strAbbr = a_strAbbr;
    }
    
    public String getAbbr()
    {
        return this.m_strAbbr;
    }
    
    public void setId(String a_strID)
    {
        this.m_strID = a_strID;
    }
    
    public String getId()
    {
        return this.m_strID;
    }
    
    public void setUsed(boolean a_bUsed)
    {
        this.m_bUsed = a_bUsed;
    }
    
    public boolean getUsed()
    {
        return this.m_bUsed;
    }
}
