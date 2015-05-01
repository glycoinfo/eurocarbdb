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
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.Collections;

//  3rd party imports
import org.apache.log4j.Logger;
import org.apache.commons.lang.ArrayUtils;

//  eurocarb imports
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.EurocarbObject;
import org.eurocarbdb.dataaccess.EntityDoesntExistException;

import org.eurocarbdb.dataaccess.indexes.Index;
import org.eurocarbdb.dataaccess.indexes.Indexable;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**  
*   Base class for various browsing-oriented actions that retrieve a {@link List} of 
*   data objects given an offset and max list size, potentially also ordered by a 
*   settable {@link Index} type.
*/
@ParameterChecking( whitelist={"resultsPerPage","page","indexedBy"} )
public abstract class BrowseAction<T> 
extends EurocarbAction implements PageableAction, Indexable<T>
{
    /** logging handle */
    static Logger log = Logger.getLogger( BrowseAction.class );
    
    // /// object variables
    
    /** The {@link List} of results to be browsed by this {@link Action}. */
    protected List<T> results = null;
    
    /** Total number of objects of type 'T' that are browseable by this {@link Action}. 
    *   Initially set to -1, indicating "not yet set or initialised". */
    protected int totalResults = -1;

    private int resultsPerPage = 20;
    
    private int currentPageNumber = 1;
    
    /** String version of input sequence. */
    private String sequence;

    // output message
    private String message = "";
    
    /**
     * Override the parameters whitelist method, so that we can inject the
     * whitelist for parameters that this class adds
     */
    @Override
    protected String[] parametersWhitelist() {
        String[] superWhitelist = super.parametersWhitelist();
        
        ParameterChecking security =
                        BrowseAction.class.getAnnotation(ParameterChecking.class);
        if (security != null) {
            return (String[]) ArrayUtils.addAll(superWhitelist,security.whitelist());
        }
        return null;
    }

    /**
     * Override the parameters blacklist method, so that we can inject the
     * blacklist for parameters that this class adds
     */
    @Override
    protected String[] parametersBlacklist() {
        String[] superBlacklist = super.parametersBlacklist();

        ParameterChecking security =
                        BrowseAction.class.getAnnotation(ParameterChecking.class);
        if (security != null) {
            return (String[]) ArrayUtils.addAll(superBlacklist,security.blacklist());
        }
        return null;
    }

    // /// object methods
    
    public int getResultsPerPage()
    {
        return resultsPerPage;
    }
    
    
    public void setResultsPerPage( int i )
    {
        if ( i > 100 )
        {
            log.warn( 
                "resultsPerPage = " 
                + i 
                + " > max (100), setting to 100..." 
            );
            i = 100;
        }
        log.debug("resultsPerPage set to " + i );
        resultsPerPage = i;
    }
    
    
    public int getPage()
    {
        return currentPageNumber;
    }
    
    
    public void setPage( int n )
    {
        log.debug("page set to " + n );
        currentPageNumber = n;        
    }
    
    
    /** 
    *   Calculates what would be the last page of results, given the 
    *   values returned from {@link #getTotalResults} and {@link #getResultsPerPage}.
    */
    public int getLastPage() 
    {
        return (int)Math.ceil((double)getTotalResults()/(double)getResultsPerPage());
    }
    

    public int getFirstShowing(int page) 
    {
        return Math.min(getTotalResults(),(page-1)*getResultsPerPage());
    }

    /** 
    *   Returns the index of the last entry that would be shown on the 
    *   given page number, from the current values of {@link #getResultsPerPage}
    *   and {@link #getTotalResults}. 
    */
    public int getLastShowing( int page ) 
    {
        return 
            Math.max( 
                0, 
                Math.min( 
                    getTotalResults(), 
                    page * getResultsPerPage()
                ) - 1
            );
    }

    
    public int getFirstShowing() 
    {
        return getFirstShowing(getPage());
    }
    

    public int getLastShowing() 
    {
        return getLastShowing(getPage());
    }
        
    
    public int getOffset() 
    {
        return Math.min( (getPage() - 1) * getResultsPerPage(), getTotalResults() );
    }
    
    
    public int getMaxResults() 
    {
        return 
            Math.max( 
                Math.min( 
                    getOffset() + getResultsPerPage(), 
                    getTotalResults() 
                ) - getOffset(), 
                0 
            );
    }      

    
    public List<T> getResults()
    {        
        return results;
    }
    
    
    public void setResults( List<T> list )
    {
        if( list == null )
            results = Collections.emptyList(); //new java.util.ArrayList<T>();
        else
            results = list;    
    }
    

    public void setAllResults(List<T> list) 
    {
        if( list == null ) 
        {
            setResults( Collections.EMPTY_LIST );
            setTotalResults(0);
        }
        else 
        {
            setTotalResults( list.size() );
            
            // setResults( list.subList( getOffset(), getOffset() + getMaxResults()) );
            int first_index = getOffset();
            if ( first_index < 0 ) first_index = 0;
            int last_index = first_index + getMaxResults();
            setResults( list.subList( first_index, last_index ) );
        }
    }    

    
    public String getMessage()
    {
        return message;
    }
    
    
    public void setMessage(String strMessage)
    {
        message = strMessage;
    }
        
    
    /** 
    *   Returns the total number of objects of generic type 'T'. 
    */
    public int getTotalResults() 
    {
        if ( totalResults < 0 )
        {
            log.warn(
                "totalResults has not been calculated/set, "
                + "which means that other properties that rely on this value "
                + "are probably going to be wrong. Please override this method "
                + "in subclasses or otherwise call setTotalResults(int) to avoid "
                + "this messsage." 
            );
        }
        return totalResults;
    }

    
    /** Explicitly sets the total number of results browseable by this {@link Action}. */
    protected void setTotalResults( int count )
    {
        log.debug("totalResults set to " + count );
        this.totalResults = count;    
    }
    
    
    /*~~~~~ implementation of Indexable ~~~~~*/
    
    /** The index with which to order results, initially null. 
    *   If equal to Index.NONE, results are returned in default 
    *   order (arbitrary, but stable order). */
    protected Index<T> index = null;
    
    
    public abstract Class<T> getIndexableType()
    ;
    
    
    public Index<T> getDefaultIndex()
    {
        return (Index<T>) Index.NONE;
    }
    
    
    public Index<T> getIndex()
    {
        if ( this.index == null )
            return getDefaultIndex();
        
        return this.index;    
    }
    
    
    public List<Index<T>> getIndexes()
    {
        return Collections.emptyList();
    }
    
    
    public void setIndexedBy( String name )
    {
        if ( name == null || name.length() == 0 )
        {
            this.index = getDefaultIndex();
        }
        
        for ( Index i : getIndexes() )
        {
            if ( name.equals( i.getName() ) )
            {
                this.index = i;
                return;
            }
        }
    }
    
    
    
    
} // end class

