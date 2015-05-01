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
public class ContactInformation 
{
    private String m_strEmail = "";
    private String m_strName = "";
    private String m_strType = "";
    private String m_strSubject = "";
    private String m_strContent = "";
    private boolean m_bSubject = false;
    private boolean m_bContent = false;
    private boolean m_bUsed = false;
    
    public void setUsed(boolean a_bMiss)
    {
        this.m_bUsed = a_bMiss;
    }
    
    public boolean getUsed()
    {
        return this.m_bUsed;
    }
    
    public void setMissSubject(boolean a_bMiss)
    {
        this.m_bSubject = a_bMiss;
    }
    
    public boolean getMissSubject()
    {
        return this.m_bSubject;
    }
    
    public void setMissContent(boolean a_bMiss)
    {
        this.m_bContent = a_bMiss;
    }
    
    public boolean getMissContent()
    {
        return this.m_bContent;
    }

    public void setEmail(String a_strMail)
    {
        this.m_bUsed = true;
        this.m_strEmail = a_strMail;
    }
    
    public String getEmail()
    {
        return this.m_strEmail;
    }
    
    public void setName(String a_strName)
    {
        this.m_bUsed = true;
        this.m_strName = a_strName;
    }
    
    public String getName()
    {
        return this.m_strName;
    }
    
    public void setType(String a_strType)
    {
        this.m_bUsed = true;
        this.m_strType = a_strType;
    }
    
    public String getType()
    {
        return this.m_strType;
    }
    
    public void setContent(String a_strText)
    {
        this.m_bUsed = true;
        this.m_strContent = a_strText;
    }
    
    public String getContent()
    {
        return this.m_strContent;
    }
    
    public String getSubject() 
    {
        return this.m_strSubject;
    }

    public void setSubject(String a_strSubject) 
    {
        this.m_bUsed = true;
        this.m_strSubject = a_strSubject;
    }
}
