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

package org.eurocarbdb.sugar.impl;

import java.util.Set;
import java.util.HashSet;

import org.apache.log4j.Logger;

import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.SequenceFormat;

import org.eurocarbdb.util.graph.Graph;

import static org.eurocarbdb.util.StringUtils.join;


public class ResidueDisjunction //extends GenericResidue
{
    public Set<Residue> alternates = new HashSet<Residue>(2);
    
    
    public ResidueDisjunction()
    {
        alternates = new HashSet<Residue>(2);       
    }
    
    
    public ResidueDisjunction( Residue... residues )
    {
        alternates = new HashSet<Residue>( residues.length );
        for ( Residue r : residues )
            alternates.add( r );
    }

    
    public void add( Residue r )
    {
        if ( ! alternates.add( r ))
            throw new RuntimeException(
                "Cannot add duplicate residue '" + r + "' to alternate");
    }
    
    public String toString()
    {
        return "<" 
            + join("|", alternates )
            + ">";    
    }

}

