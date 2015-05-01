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
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/

package org.eurocarbdb.action;

//  stdlib imports
import java.util.List;
import java.util.Collections;

//  3rd party imports
import org.apache.log4j.Logger;
import org.hibernate.Criteria;

//  eurocarb imports
import org.eurocarbdb.action.BrowseAction;
import org.eurocarbdb.dataaccess.indexes.Index;
import org.eurocarbdb.dataaccess.indexes.Indexable;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

public abstract class AbstractBrowseAction<T> extends BrowseAction<T>
{
    /** logging handle */
    protected static Logger log = Logger.getLogger( AbstractBrowseAction.class );
    
    /** The Query that populates the {@link #results} {@link List}. */
    protected Criteria query = null;
    
    /** The results {@link List} */
    protected List<T> results;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public abstract Class<T> getIndexableType()
    ;
    
    
    /** 
    *   Returns the core query that obtains the main result set for 
    *   this browse action. 
    */
    public Criteria getQuery()
    {
        if ( query == null )
        {
            query = getEntityManager().createQuery( getIndexableType() );
            initQuery( query );
        }
        
        return query;
    }
    
    
    /** 
    *   Adds comments and offset/limit restrictions to the passed {@link Criteria} query. 
    *   Note that offset ({@link #getOffset}) and limit ({@link #getMaxResults}) parameters
    *   and their dependant values must be set <em>prior</em> to this method being called.
    */
    protected void initQuery( Criteria q )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug(
                "initialising query: offset = " 
                + this.getOffset()
                + ", max results = " 
                + this.getMaxResults() 
            );
        }
        
        q.setComment( this.getClass().getName() );
        q.setMaxResults( this.getMaxResults() );            
        q.setFirstResult( this.getOffset() );            
    }
    
    
    public String execute()
    {
        List<T> results = this.getResults(); //(List<GlycanSequence>) q.list();
        
        //this.setResults( results );
        assert results != null;
        log.debug( results.size() + " result(s)");
        
        return SUCCESS;
    }
 
    
    @Override
    public List<T> getResults()
    {
        if ( results == null )
        {
            Criteria q = getQuery();
            this.getIndex().apply( q );
            
            results = (List<T>) q.list();
            if ( results == null )
                return Collections.emptyList();
        }
        
        return results;
    }
    
        
    @Override
    public int getTotalResults() 
    {
        if ( totalResults <= 0 )
        {
            totalResults = getEntityManager().countAll( getIndexableType() );
            log.debug("calculated totalResults = " + totalResults );
        }
        
        return totalResults;   
    }

    
}


