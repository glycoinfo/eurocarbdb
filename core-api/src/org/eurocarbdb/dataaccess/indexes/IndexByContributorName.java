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

import java.util.List;
import java.util.Comparator;
import java.util.Collections;

import org.eurocarbdb.dataaccess.EurocarbObject;
import org.eurocarbdb.dataaccess.Contributed;
import org.eurocarbdb.dataaccess.core.Contributor;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;


/**
*   Allows the sorting and comparison of any objects that implement the 
*   {@link Contributed} interface by {@link Contributor} name, then by 
*   {@link Date} entered.
*
*   @see Contributed.getContributor()
*   @see Contributed.getDateEntered()
*   @see Contributor.getContributorName()
*   @see Indexable
*
*   @author mjh
*   @version $Rev: 1147 $
*/
public class IndexByContributorName<T extends Contributed> implements Index<T>
{

    public void apply( Criteria query )
    {
        //  join needs to be to the root entity of the query, therefore
        //  need to use 'this' qualifier.
        query
            .createAlias("this.contributor", "c" ) 
            .addOrder( Order.asc("c.contributorName") )
            .addOrder( Order.asc("this.dateEntered") )
        ;
    }
    
    
    public void apply( List<T> results )
    {
        Collections.sort( results, this );
    }    
    
    
    /** Compares {@link Contributed.getContributorName()} alphabetically. */
    public int compare( T cont1, T cont2 ) 
    {
        return cont1.getContributor()
                    .getContributorName()
                    .compareTo( cont2.getContributor().getContributorName() );
    }
    
    
    /** Returns Contributed.class */
    public Class<Contributed> getIndexableType()
    {
        return Contributed.class;
    }

    
    /** Returns "contributor". */
    public String getName()
    {
        return "contributor";
    }
    
    
    /** Returns "Contributor". */
    public String getTitle()
    {
        return "Contributor";
    }
    
    
    /** Returns a short textual description of this Index. */
    public String getDescription()
    {
        return "Orders results by contributor name";
    }
    
}



