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

package org.eurocarbdb.sugar;

import java.util.Set;

import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.util.graph.Edge;
import org.eurocarbdb.util.graph.Vertex;


/** 
*   Interface for classes that represent annotations, and/or sub-regions,
*   of another {@link Sugar}.
*
*   @see Sugar
*   @see Graph
*   @version $Rev: 1147 $
*   @author mjh
*/
public interface SugarAnnotation
{
    
    /**
    *   Returns the {@link Set} of {@link Residue}s to which this 
    *   annotation applies. These residues do not necessarily have to
    *   be contiguous.
    */
    public Set<Residue> getAnnotatedResidues()
    ;
    
    
    /** Returns the {@link Sugar} to which this annotation applies. */
    public Sugar getAnnotatedSugar()
    ;    
    
}

