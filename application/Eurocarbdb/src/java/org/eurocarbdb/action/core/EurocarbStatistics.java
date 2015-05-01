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
*   Last commit: $Rev: 1561 $ by $Author: glycoslave $ on $Date:: 2009-07-21 #$  
*/

package org.eurocarbdb.action.core;

//  stdlib imports
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;

import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.Contributed;


//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

import org.eurocarbdb.dataaccess.core.Contributor;


/*  class UserSummary  *//*************************************
*
*   
*
*   @author   ac [a.ceroni@imperial.ac.uk]
*   @version  $Rev: 1561 $
*/
public class EurocarbStatistics extends EurocarbAction
{

    /** Logging handle. */
    private static final Logger log = Logger.getLogger( EurocarbStatistics.class );

    private Map<Evidence.Type,Long> evidenceCount = null;
    
    private static final String the_query 
        = Evidence.class.getName() + ".COUNT_BY_EVIDENCE_TYPE";
    
        
    private Map<Evidence.Type,Long> getEvidenceCount()
    {
        if ( evidenceCount != null )
            return evidenceCount;
        
        evidenceCount = new HashMap<Evidence.Type,Long>();
        
        log.debug("looking up evidence counts by evidence type");
        List<Object[]> pairs = (List<Object[]>) Eurocarb.getEntityManager()
                               .getQuery( the_query )
                               .list();
                               
        for ( Object[] pair : pairs )
        {
            Evidence.Type type = (Evidence.Type) pair[0];           
            Long count = (Long) pair[1];
            evidenceCount.put( type, count );
            log.debug("evidence type " + type + ": " + count );
        }
        
        return evidenceCount;
    }
        
        
    public long getCountAllReferences()
    {
        return getEntityManager().countAll( Reference.class );
    }

    
    public long getCountJournalReferences()
    {
        return getEntityManager().countAll( JournalReference.class );
        //return JournalReference.countJournalReferences();  
    }

    
    public long getCountAllStructures()
    {
        return getEntityManager().countAll( GlycanSequence.class );
    }
    
    
    public int getCountHplcEvidence()
    {
        Long count = getEvidenceCount().get( Evidence.Type.HPLC );
        if ( count == null ) 
            return 0;
        
        return count.intValue();
    }
    
    
    public int getCountMsEvidence()
    {
        Long count = getEvidenceCount().get( Evidence.Type.MS );
        if ( count == null ) 
            return 0;
        
        return count.intValue();
    }

    
    public int getCountNmrEvidence()
    {
        Long count = getEvidenceCount().get( Evidence.Type.NMR );
        if ( count == null ) 
            return 0;
        
        return count.intValue();
    }
    
    
    public int getCountTaxonomy()
    {
        return getEntityManager().countAll( Taxonomy.class );
    }
    

    public int getCountTissueTaxonomy()
    {
        return getEntityManager().countAll( TissueTaxonomy.class );
    }


    public int getCountDisease()
    {
        return getEntityManager().countAll( Disease.class );
    }
    
    public int getCountPerturbation()
    {
        return getEntityManager().countAll( Perturbation.class );
    }

    public int getCountGlycoconjugate()
    {
        return getEntityManager().countAll( Glycoconjugate.class );
    }
    
    /** Returns 10 most recently contributed objects */
    public List<Contributed> getRecentChanges()
    {
        int max_items = Integer.parseInt( Eurocarb.getProperty("pref.show_max_recent_items") );
        assert max_items >= 0;
        
        return Eurocarb.getRecentContributions( max_items );
    }
    

    public String getCurrentView() 
    {
        return "Home";
    }
    

    public List<Link> getViews() 
    {
        Contributor user = this.getCurrentContributor();
        LinkedList<Link> ret = new LinkedList<Link>();
    
        ret.add(new Link("Home","home.action"));
        ret.add(new Link("Search","search.action"));
        
        if ( user.isLoggedIn() )
        {
            ret.add(new Link("Contribute","contribute.action?${action.currentActionName}"));
            ret.add(new Link("Browse","browse.action"));
            
            if( user.isAdministrator() ) 
                ret.add(new Link("Admin","admin.action"));
            
            ret.add(new Link("Settings","settings.action"));
        }
        
        ret.add( new Link("Help","help.action") );
    
        return ret;
    }


    public List<Link> getActions() 
    {
        LinkedList<Link> ret = new LinkedList<Link>();
        ret.add( new Link("no action","home.action") );
        return ret;
    }


    /*  execute  *//*************************************************
    *
    */
    public String execute() throws Exception
    {        
    return SUCCESS;
    }

} // end class

