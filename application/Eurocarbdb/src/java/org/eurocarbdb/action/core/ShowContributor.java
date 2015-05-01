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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/
/*-------------------------------------------------------------------
*   $Id: ShowContributor.java 1932 2010-08-05 07:12:33Z glycoslave $
*   Last changed $Author: glycoslave $
*   EUROCarbDB Project
*------------------------------------------------------------------*/

package org.eurocarbdb.action.core;

//  stdlib imports
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;

//  3rd party imports 
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.Contributor;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class ShowContributor  *//****************************************
*
*   Shows a detail page for a {@link Contributor} if given a contributor_id,
*   otherwise defaults to the currently logged-in contributor.
*
*   @author   mjh [glycoslave@gmail.com]
*   @version  $Rev: 1932 $
*/
public class ShowContributor extends EurocarbAction
{
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    static final Logger log = Logger.getLogger( ShowContributor.class );

    /** The contributor we will detail, defaults to current contributor */
    private Contributor contributor = null;
    
    private int searchContributorId = 0;
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** 
    *   Returns the Contributor that was looked up. This will most likely be 
    *   null if the execute() method has not yet been called. 
    */
    public Contributor getContributor() 
    {  
        if ( contributor == null )
        {
            contributor = Contributor.getCurrentContributor();
            assert contributor != null;
        }
        return contributor;  
    }
    
    protected void setContributor( Contributor c ) {  contributor = c;  }

    /** Returns the contributor id that is being looked up. */
    public int getContributorId() {  return searchContributorId;  }
    
    /** Sets the contributor id to lookup. */
    public void setContributorId( int search_id ) {  searchContributorId = search_id;  }

    /**
    *   Returns a {@link Map} of {@link Contributor}s to the number of {@link GlycanSequence}s
    *   they have contributed.
    */
    public Map<Contributor,Integer> getMapOfGlycanSequenceCountByContributor()
    {
        return Contributor.getMapOfGlycanSequenceCountByContributor();      
    }

    
    public String execute()
    {
        if ( searchContributorId > 0 )
        {
            log.debug("looking up Contributor=" + searchContributorId );
            try 
            {
                Contributor c = getEntityManager()
                                .lookup( Contributor.class, searchContributorId );
                                
                contributor = c;
            }
            catch ( Exception e )
            {
                log.warn( "Caught exception while trying to lookup Contributor=" 
                        + searchContributorId
                        , e );
                contributor = null;
            }
        }
        else log.debug("no contributor_id given, defaulting to current contributor");
        
        if ( getContributor() == null )
            return "error__requested_contributor_doesnt_exist";   
        
        return "success";
    }

} // end class
