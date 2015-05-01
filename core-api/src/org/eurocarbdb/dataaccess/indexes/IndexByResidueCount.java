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
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/

package org.eurocarbdb.dataaccess.indexes;

//  stdlib imports
import java.util.List;
import java.util.Collections;

//  3rd party imports
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;

//  eurocarb imports
import org.eurocarbdb.dataaccess.core.GlycanSequence;

//  static imports

/**
*   Allows the sorting and comparison of {@link GlycanSequence}s by 
*   {@link GlycanSequence#getResidueCount number of residues}.
*
*   @see GlycanSequence.getResidueCount
*
*   @author mjh
*   @version $Rev: 1147 $
*/
public class IndexByResidueCount<T extends GlycanSequence> implements Index<T>
{

    public void apply( Criteria query )
    {
        query.addOrder( Order.desc("this.residueCount") );
    }
    
    
    public void apply( List<T> results )
    {
        Collections.sort( results, this );
    }    
    
    
    public int compare( T seq1, T seq2 ) 
    {
        return ((Integer) seq2.getResidueCount()).compareTo( seq1.getResidueCount() );
    }
    
    
    public Class<GlycanSequence> getIndexableType()
    {
        return GlycanSequence.class;
    }

    
    /** Returns "residue_count". */
    public String getName()
    {
        return "residue_count";
    }
    
    
    /** Returns "Number of residues". */
    public String getTitle()
    {
        return "Number of residues";
    }
    
    
    /** Returns "Orders results by most to least number of residues". */
    public String getDescription()
    {
        return "Orders results by most to least number of residues";
    }
    
} // end class



