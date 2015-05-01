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
package org.eurocarbdb.applications.ms.glycopeakfinder.glycosciences;

/**
* @author Logan
*
*/
public class GlycoSciencesEntry 
{
    private String m_strLinucsID = "";
    private String m_strLinucs = "";
    private String m_strIupac = "";
    private int m_iHeight = 10;
    
    public void setLinucs(String a_strID)
    {
    this.m_strLinucsID = a_strID;
    }
    
    public String getLinucs()
    {
    return this.m_strLinucsID;
    }

    public void setLinucsCode(String a_strLinucs) {
    this.m_strLinucs = a_strLinucs;
    }

    public String getLinucsCode() {
    return this.m_strLinucs;
    }
    
    public void setIupac(String a_strCode)
    {
    this.m_strIupac = a_strCode;
    }
    
    public String getIupac()
    {
    return this.m_strIupac;
    }
    
    public void setHeight(int a_iH)
    {
    this.m_iHeight = a_iH;
    }
    
    public int getHeight()
    {
    return this.m_iHeight;
    }
}
