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

package org.eurocarbdb.action.core;

//  stdlib imports
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.action.AbstractBrowseAction;
import org.eurocarbdb.dataaccess.core.Evidence;

import org.eurocarbdb.dataaccess.indexes.Index;
import org.eurocarbdb.dataaccess.indexes.Indexable;
import org.eurocarbdb.dataaccess.indexes.IndexByContributedDate;
import org.eurocarbdb.dataaccess.indexes.IndexByContributorName;

import org.eurocarbdb.action.ParameterChecking;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**  
*   Simple action that retrieves a {@link List} of {@link Evidence}
*   given an offset and max list size, potentially also ordered by a given  
*   {@link Index} type.
*/
@ParameterChecking( whitelist = {} )
public class BrowseEvidence extends AbstractBrowseAction<Evidence>//BrowseAction<Evidence> implements Indexable<Evidence>
{   
    
    public Map<String,Integer> getMapOfEvidenceCountByType()
    {
        
        Map<Evidence.Type,Integer> map = Evidence.getCountEvidenceByType();
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
    
    
    public final Class<Evidence> getIndexableType() 
    {
        return Evidence.class;
    }
    

    @Override    
    public List<Index<Evidence>> getIndexes()
    {
        return indexes;
    }
    
} // end class

