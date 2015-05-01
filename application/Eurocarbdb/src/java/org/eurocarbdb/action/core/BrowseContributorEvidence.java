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
*   Last commit: $Rev: 1870 $ by $Author: david@nixbioinf.org $ on $Date:: 2010-02-23 #$  
*/

package org.eurocarbdb.action.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eurocarbdb.action.BrowseAction;
import org.eurocarbdb.dataaccess.core.Evidence;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.indexes.Index;
import org.eurocarbdb.dataaccess.indexes.IndexByContributedDate;
import org.eurocarbdb.dataaccess.indexes.IndexByContributorName;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class BrowseContributorEvidence
*
*   descr here
*
*/
@org.eurocarbdb.action.ParameterChecking(whitelist={"contributorId"})
public class BrowseContributorEvidence extends BrowseAction<Evidence>
{   
    private Contributor contributor = Contributor.getCurrentContributor();
    
    public void setContributorId(int id) 
    {
        contributor = getEntityManager().lookup( Contributor.class, id );
        if( contributor == null ) 
            contributor = Contributor.getGuestContributor();
    }

    public int getContributorId() 
    {
        return contributor.getContributorId();
    }


    public void setContributor(Contributor c) 
    { 
        contributor = c; 
        if( contributor == null ) 
            contributor = Contributor.getGuestContributor();
    }

    public Contributor getContributor() 
    {
        return contributor;
    } 
    
    
    public String execute()
    {
        setAllResults( contributor.getMyEvidence() );
        return "success";
    }
    
    
    public final Class<Evidence> getIndexableType() 
    {
        return Evidence.class;
    }
    
    public Map<String,Integer> getMapOfEvidenceCountByType()
    {
    	HashMap<Evidence.Type,Integer> map=new HashMap<Evidence.Type,Integer>();
    	List<Evidence> evidenceList=contributor.getMyEvidence();
    	for(Evidence evidenceObj:evidenceList){
    		Evidence.Type type=evidenceObj.getEvidenceType();
    		if(!map.containsKey(type))
    			map.put(type, new Integer(0));
    		
    		map.put(type,map.get(type)+1);
    	}
    	
        Map<String,Integer> smap = new HashMap<String,Integer>( map.size() );
        for ( Map.Entry e : map.entrySet() )
            smap.put( e.getKey().toString(), (Integer) e.getValue() );
            
        return smap;
    }
    
    
    /*  Indexes  */

    /** The {@link List} of {@link Index}es supported by this Action. */
    public static final List<Index<Evidence>> indexes = Arrays.asList(
        new IndexByContributedDate<Evidence>(),
        new IndexByContributorName<Evidence>()
    );
    

    /** Default index is the first index in the list of indexes */
    @Override
    public Index<Evidence> getDefaultIndex()
    {
        return indexes.get( 0 );    
    }
    

    @Override    
    public List<Index<Evidence>> getIndexes()
    {
        return indexes;
    }
    
    
} // end class

