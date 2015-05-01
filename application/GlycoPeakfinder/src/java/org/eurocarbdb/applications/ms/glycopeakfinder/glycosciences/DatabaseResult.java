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

import java.util.ArrayList;

/**
* @author Logan
*
*/
public class DatabaseResult 
{
    private String m_strError = null;
    private String m_strComposition = "";
    private ArrayList<GlycoSciencesEntry> m_aStructure = new ArrayList<GlycoSciencesEntry>();
    private ArrayList<Integer> m_aIds = new ArrayList<Integer>();
    private double m_dMass = 0;
    private ArrayList<Integer> m_aPages = new ArrayList<Integer>();
    private int m_iCurrentPage = 1;
    private int m_iLastPage = 1;
    private String m_strDatabase = "";
    
    public void setNextPage(int a_iPage)
    {}
     
    public int getNextPage()
    {
        return (this.m_iCurrentPage+1);
    }
    
    public void setPreviewPage(int a_iPage)
    {}
     
    public int getPreviewPage()
    {
        return (this.m_iCurrentPage-1);
    }
    
    public void setIds(ArrayList<Integer> a_aIds)
    {
        this.m_aIds = a_aIds;
    }
    
    public ArrayList<Integer> getIds()
    {
        return this.m_aIds;
    }
    
    /**
     * @param string
     */
    public void setError(String a_strError)
    {
        this.m_strError = a_strError;
    }

    public String getError()
    {
        return this.m_strError;
    }
    
    /**
     * @param string
     */

    public int getEntryCount()
    {
        return this.m_aIds.size();
    }
    
    public ArrayList<GlycoSciencesEntry> getEntry()
    {
        return this.m_aStructure;
    }
    
    public void setEntry(ArrayList<GlycoSciencesEntry> a_objEntry)
    {
        this.m_aStructure = a_objEntry;
    }
    /**
     * @param composition
     */
    public void setComposition(String a_strComposition)
    {
        this.m_strComposition = a_strComposition;
    }
    
    public String getComposition()
    {
        return this.m_strComposition;
    }
    
    public void setMass(double a_dMass)
    {
        this.m_dMass = a_dMass;
    }
    
    public double getMass()
    {
        return this.m_dMass;
    }

    public void setPages(ArrayList<Integer> a_aPages) 
    {
        this.m_aPages = a_aPages;
    }
    
    public ArrayList<Integer> getPages()
    {
        return this.m_aPages;
    }
    
    public void setCurrentPage(int a_iPage)
    {
        this.m_iCurrentPage = a_iPage;
    }
    
    public int getCurrentPage()
    {
        return this.m_iCurrentPage;
    }

    public void setLastPage(int a_iPage)
    {
        this.m_iLastPage = a_iPage;
    }
    
    public int getLastPage()
    {
        return this.m_iLastPage;
    }

    public String getDatabase() 
    {
        return this.m_strDatabase;
    }
    
    public void setDatabase(String a_strDB)
    {
        this.m_strDatabase = a_strDB;
    }
}
