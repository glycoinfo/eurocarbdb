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
*   Last commit: $Rev: 1557 $ by $Author: glycoslave $ on $Date:: 2009-07-20 #$  
*/
package org.eurocarbdb.dataaccess;

import java.util.Calendar;
import org.hibernate.criterion.DetachedCriteria;
 
/*  class SavedGlycanSequenceSearch  *//*************************************
*
*
*   @see org.eurocarbdb.action.core.SearchGlycanSequence
*   @author hirenj
*/
public class SavedGlycanSequenceSearch
{
    /*  Criteria that can be plugged into sub select queries directly
        in Hibernate
     */
    public DetachedCriteria queryCriteria;
    
    public Calendar queryTime;
    public String description;
    public String userDescription;
    public int resultCount;
    public String sequence;
    
    public SavedGlycanSequenceSearch() 
    {
        this.queryTime = Calendar.getInstance();
    }

    
    public DetachedCriteria getQueryCriteria() 
    {
        return this.queryCriteria;
    }

    
    /** The timestamp that this object was created at */
    public Calendar getQueryTime() 
    {
        return this.queryTime;
    }

    
    /** Automatically generated description of the query */
    public String getDescription() 
    {
        return this.description;
    }
    
    
    /** Count of results from this one query at time of running */
    public int getResultCount() 
    {
        return this.resultCount;
    }
    
    
    public String getSearchSequence() {  return sequence;  }
    
    
    /** User defined description for this query. */
    public String getUserDescription() 
    {
        return this.userDescription;
    }

    
    /** Set the user defined description for this query. */
    public void setUserDescription(String description) 
    {
        this.userDescription = description;
    }
    
}