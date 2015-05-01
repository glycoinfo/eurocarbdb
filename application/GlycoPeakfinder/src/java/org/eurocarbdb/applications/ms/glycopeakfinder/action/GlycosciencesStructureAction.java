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

import java.util.Iterator;

import org.eurocarbdb.applications.ms.glycopeakfinder.glycosciences.DatabaseResult;
import org.eurocarbdb.applications.ms.glycopeakfinder.glycosciences.GlycoSciencesEntry;

/**
* @author Logan
*
*/
public class GlycosciencesStructureAction extends GlycoPeakfinderAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // database request
    private DatabaseResult m_objDatabaseResult = null;
    private String m_strId = "";
    private String m_strStructure = "";
    
    public GlycosciencesStructureAction()
    {
        this.m_strPageType = "calculation";
    }

    public void setId(String a_strID)
    {
        this.m_strId = a_strID;
    }

    public String getId()
    {
        return this.m_strId;
    }

    public void setStructure(String a_strID)
    {
        this.m_strStructure = a_strID;
    }

    public String getStructure()
    {
        return this.m_strStructure;
    }

    public void setDbResult(DatabaseResult a_objResult)
    {
        this.m_objDatabaseResult = a_objResult;
    }

    public DatabaseResult getDbResult()
    {
        return this.m_objDatabaseResult;
    }

    /**
     * @see com.opensymphony.xwork.ActionSupport#execute()
     */
    @Override
    public String execute() throws Exception
    {
        GlycoSciencesEntry t_objEntry;
        for (Iterator<GlycoSciencesEntry> t_iterStructures = this.m_objDatabaseResult.getEntry().iterator(); t_iterStructures.hasNext();) 
        {
            t_objEntry = t_iterStructures.next();
            if ( t_objEntry.getLinucs().equals(this.m_strId.trim()))
            {
                this.m_strStructure = t_objEntry.getIupac();
            }
        }
        return "success";
    }
}