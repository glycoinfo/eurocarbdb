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
public class ErrorInformation 
{
    private String m_strTitle = "";
    private String m_strText = "";
    private ArrayList<String> m_aErrors = new ArrayList<String>();
    private String m_strBackURL = "";
    
    public void setBackUrl(String a_strURL)
    {
        this.m_strBackURL = a_strURL;
    }
    
    public String getBackUrl()
    {
        return this.m_strBackURL;
    }
    
    public void addErrors(ArrayList<String> a_aErrors)
    {
        this.m_aErrors.addAll(a_aErrors);
    }
    
    public void setTitle(String a_strMail)
    {
        this.m_strTitle = a_strMail;
    }
    
    public String getTitle()
    {
        return this.m_strTitle;
    }
    
    public void setText(String a_strName)
    {
        this.m_strText = a_strName;
    }
    
    public String getText()
    {
        return this.m_strText;
    }
    
    public void setErrors(ArrayList<String> a_strType)
    {
        this.m_aErrors = a_strType;
    }
    
    public ArrayList<String> getErrors()
    {
        return this.m_aErrors;
    }

    /**
     * @param message
     */
    public void addError(String message) 
    {
        if ( message != null )
        {
            this.m_aErrors.add(message);
        }        
    }    
}