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

package org.eurocarbdb.dataaccess.core;

import java.util.Set;
import java.util.List;
import org.eurocarbdb.dataaccess.core.*;

/**
*<p>
*   This interface identifies the implementing class as able
*   to be considered part of a {@link BiologicalContext}.
*</p>
*<p>
*   Since BiologicalContext is effectively an aggregation/abstraction
*   of different biological aspects ({@link Taxonomy}, 
*   {@link TissueTaxonomy}, {@link Disease}, {@link Perturbation},
*   {@link Glycoconjugate} association, etc) of a sample/biological 
*   source, some common interface (ie: this interface) is required to  
*   be able to group & manipulate these disparate information sources 
*   together.
*</p>
*<p>
*   Accordingly, any class that wants to be considered "part of" a
*   BiologicalContext should implement this interface.
*</p>
*
*   @author mjh
*   @see BiologicalContext
*   @see BiologicalContext.getAllAssociations()
*/
public interface BiologicalContextAssociation
{
    /**
    *   Returns the {@link List} of all {@link BiologicalContext}s
    *   that are associated to this object.
    */  
    public Set<BiologicalContext> getAllContexts()
    ;
    
    
    /** 
    *   Returns an ID for this object that identifies it uniquely 
    *   within its own hierachy. For example, for a {@link Taxonomy}
    *   this method returns its {@link Taxonomy.getTaxonomyId() Taxonomy id}. 
    */
    public int getId()
    ;
    
    
    /** 
    *   Returns a {@link Reference} for this object that would allow 
    *   it to be looked up outside of Eurocarb. 
    */
    public Reference getExternalReference()
    ;
    
} // end interface
