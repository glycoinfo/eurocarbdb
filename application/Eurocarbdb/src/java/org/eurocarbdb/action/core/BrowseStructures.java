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
import java.util.List;
import java.util.Arrays;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.action.AbstractBrowseAction;
import org.eurocarbdb.dataaccess.core.GlycanSequence;

import org.eurocarbdb.dataaccess.indexes.Index;
import org.eurocarbdb.dataaccess.indexes.Indexable;
import org.eurocarbdb.dataaccess.indexes.IndexByResidueCount;
import org.eurocarbdb.dataaccess.indexes.IndexByMostEvidence;
import org.eurocarbdb.dataaccess.indexes.IndexByContributedDate;
import org.eurocarbdb.dataaccess.indexes.IndexByContributorName;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**  
*   Simple action that retrieves a {@link List} of {@link GlycanSequence}s
*   given an offset and max list size, potentially also ordered by a given  
*   {@link Index} type.
*/
@org.eurocarbdb.action.ParameterChecking(whitelist={""})
public class BrowseStructures extends AbstractBrowseAction<GlycanSequence>
{
    
    /** The {@link List} of {@link Index}es supported by this Action. */
    public static final List<Index<GlycanSequence>> indexes = Arrays.asList(
        new IndexByContributedDate<GlycanSequence>(),
        new IndexByContributorName<GlycanSequence>(),
        new IndexByMostEvidence<GlycanSequence>(),
        new IndexByResidueCount<GlycanSequence>()
    );
    
    
    /** Default index is the first index in the list of indexes */
    @Override
    public Index<GlycanSequence> getDefaultIndex()
    {
        return indexes.get( 0 );    
    }
    
    
    @Override
    public List<Index<GlycanSequence>> getIndexes()
    {
        return indexes;
    }

    
    public final Class<GlycanSequence> getIndexableType()
    {
        return GlycanSequence.class;
    }
    
    
    /** Noop execute method used for generation of glycan structure ID XML. */
    public String executeGetIdsOnly() {  return "success";  }
    
    
    /** 
    *   Returns a {@link List} of all {@link GlycanSequence} Ids in the 
    *   data store.
    */
    public List<Integer> getAllGlycanStructureIds()
    {
        return (List<Integer>) 
            getEntityManager()
            .createQuery( GlycanSequence.class )
            .setProjection( org.hibernate.criterion.Projections.id() )
            .addOrder( org.hibernate.criterion.Order.desc("dateEntered") )
            .list()
        ;
    }

} // end class BrowseStructures

