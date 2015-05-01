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

import org.eurocarbdb.dataaccess.EurocarbObject;


/**
*<p>
*   Indicates that a data/results-producing class produces a result/data set that 
*   can have one or more {@link Index}es applied to it. For example, an Action 
*   class may generate a Set of {@link GlycanSequence}s that are indexable by 
*   various {@link Index}es, such as {@link IndexByDateContributed}.
*</p>
*<p>
*   It is suggested (but not required) that classes that can use/apply multiple
*   {@link Index}es provide a {@link List} of available Indexes as a 
*   <tt>protected static</tt> variable. ie:
<!--
*       class BrowseGlycanSequences implements Indexable<GlycanSequence>
*       {
*           protected static final List<Index<GlycanSequence>> index_list
*               = Arrays.asList( 
*                   new IndexByContributor(),
*                   new IndexByDateContributed(),
*                   new IndexGlycanSequenceByTaxonomy(),
*                   new IndexGlycanSequenceByEvidence()
*               );            
*
*           public List<Index<GlycanSequence>> getIndexes()
*           {
*               return index_list;
*           }
*       }
-->
*<pre>
*       class BrowseGlycanSequences implements Indexable&lt;GlycanSequence&gt;
*       {
*           protected static final List&lt;Index&lt;GlycanSequence&gt;&gt; index_list
*               = Arrays.asList( 
*                   new IndexByContributor(),
*                   new IndexByDateContributed(),
*                   new IndexGlycanSequenceByTaxonomy(),
*                   new IndexGlycanSequenceByEvidence()
*               );            
*
*           public List&lt;Index&lt;GlycanSequence&gt;&gt; getIndexes()
*           {
*               return index_list;
*           }
*       }
*</pre>
*</p>
*
*   @author mjh
*/
public interface Indexable<T>
{
    /** Returns the default {@link Index} for this {@link Indexable} object. */
    public Index<T> getDefaultIndex()
    ;
    
    
    /** 
    *   Returns the {@link Class} (the generic type) this class
    *   is able to apply indexes to. 
    */
    public Class<T> getIndexableType()
    ;
    
    
    /** Returns a {@link List} of all available {@link Index}es for this class. */
    public List<Index<T>> getIndexes()
    ;
    
    
    /**
    *   Sets the type of index to apply by name.
    */
    public void setIndexedBy( String name )
    ;
    
    
    /**
    *   Returns the currently set index for this class.
    *   @see Index.NONE
    */
    public Index<? extends T> getIndex()
    ;
    
    
    
}


