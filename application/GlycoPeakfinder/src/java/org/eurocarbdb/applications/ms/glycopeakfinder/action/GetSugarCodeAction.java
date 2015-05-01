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
package org.eurocarbdb.applications.ms.glycopeakfinder.action;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eurocarbdb.applications.ms.glycopeakfinder.util.DBInterface;

/**
* @author rene
*
*/
public class GetSugarCodeAction extends GlycoPeakfinderAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String m_strID = "";
    private String m_strType = "";
    private String m_strIupac = "";
    private String m_strLinucs = "";
    
    public void setId(String a_strID)
    {
        this.m_strID = a_strID;
    }
    
    public String getId()
    {
        return this.m_strID;
    }
    
    public void setType(String a_strType)
    {
        this.m_strType = a_strType;
    }
    
    public String getType()
    {
        return this.m_strType;
    }
    
    public InputStream getIupacStream() 
    {  
        ByteArrayInputStream t_streamString = new ByteArrayInputStream(this.m_strIupac.getBytes());
        return t_streamString;
    }
    
    public InputStream getLinucsStream() 
    {  
        ByteArrayInputStream t_streamString = new ByteArrayInputStream(this.m_strLinucs.getBytes());
        return t_streamString;
    }

    /**
     * @see com.opensymphony.xwork.ActionSupport#execute()
     */
    @Override
    public String execute() throws Exception
    {
        DBInterface t_objDB = new DBInterface(this.m_objConfiguration);
        try
        {
            if ( this.m_strType.equalsIgnoreCase("linucs") )
            {
                this.m_strLinucs = t_objDB.getLinucs(this.m_strID);
                return "success_linucs";
            }
            else
            {
                this.m_strIupac = t_objDB.getIupac(this.m_strID);
                return "success_iupac";
            }
        } 
        catch (Exception e)
        {
            this.handleExceptions("sugar code","execute", e);
        }
        return "success_linucs";
    }   
}