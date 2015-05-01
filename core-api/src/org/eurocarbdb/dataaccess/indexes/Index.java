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

import org.hibernate.Query;
import org.hibernate.Criteria;

import org.eurocarbdb.dataaccess.EurocarbObject;


/**
*<p>
*   Indexes provide a generic mechnism by which to apply an
*   arbitrarily complex ordering to either a {@link Query} or
*   {@link Criteria} query before it is executed, or otherwise to  
*   an existing result {@link List}. Indexes are generic; they are 
*   intended to be parameterised to the class/interface to which they
*   can be applied, ie: an index applying to {@link GlycanSequence}s
*   would be implemented in terms of Index&lt;GlycanSequence&gt;.
*</p>
*<p>
*   Indexes should be stateless and thread-safe, and should *never*
*   change the size of the result set.
*</p>
*
*   @author mjh
*/
public interface Index<T> extends Comparator<T>
{
    /** 
    *   Constant that may be used to indicate the state of being unindexed. 
    *   Does nothing, and returns non-null default values for all methods 
    *   that return values.
    */
    public static final Index<?> NONE = new Index<Object>() 
        {
            public final void apply( Criteria query ) {}
            public final void apply( List<Object> results ) {}
            public final int compare( Object o1, Object o2 ) { return 0; }
            public final Class<Object> getIndexableType() { return Object.class; }
            public final String getName() { return ""; }
            public final String getTitle() { return ""; }
            public final String getDescription() { return ""; }
        };
    
        
    /** Apply this Index's ordering to an existing {@link Criteria} query. */
    public void apply( Criteria query )
    ;
    
    
    /** 
    *   Apply this Index's ordering onto the given {@link List} of results.
    *   The general implementation of this method is the following:
    *<pre>
    *       Collections.sort( results, this );
    *</pre>
    *   since this interface extends the {@link Comparator} interface.
    *   For certain types of index, this simplsitic approach to sorting
    *   may perform very poorly due to numerous additional queries being
    *   issued. In this case it is preferable to collect all required 
    *   data in a single query upfront prior to sorting.
    */
    public void apply( List<T> results )
    ;
    
    
    // /** Compares 2 indexes for equivalence by {@link #getName()}. */
    // public boolean equals( Index<?> i )
    // ;
    
    
    /**
    *   Returns the {@link Class} that specifies the minimum interface
    *   to be able to be indexed by this {@link Index}. For an object
    *   to be indexed by this Index, it must have (ie: be castable to) 
    *   the returned class as a superclass/interface, *and* be able to be
    *   queried by the appropriate query predicates. 
    */
    public Class<? super T> getIndexableType()
    ;

    
    /** 
    *   Returns an (ideally unique) string identifying this Index, 
    *   such as what one might use as a hash key, cgi parameter, etc.  
    */
    public String getName()
    ;
    
    
    /** Returns a (user-friendly) name/title for this Index. */
    public String getTitle()
    ;
    
    
    /** Returns a short textual description of this Index. */
    public String getDescription()
    ;
    
}


