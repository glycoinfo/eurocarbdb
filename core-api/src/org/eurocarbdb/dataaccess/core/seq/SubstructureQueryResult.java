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

package org.eurocarbdb.dataaccess.core.seq;

import org.apache.log4j.Logger;

import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.seq.GlycanResidue;


/** 
*   Represents a single {@link SubstructureQuery} result, that is, 
*   a single {@link GlycanSequence}, as well as the {@link Set} of 
*   {@link Residue}s (in the form of {@link GlycanResidue}s) that 
*   matched the original search sub-structure. 
*
*   @see SubstructureQuery
*   @see SubstructureQueryGenerator
*   @author mjh
*/
public class SubstructureQueryResult
{
    /** logging handle */
    static final Logger log = SubstructureQuery.log;
    
    /** The GS that matched the substructure query */
    private GlycanSequence glycanSequence;
    
    /** The exact GR's of the GS that were matched by the query */
    private GlycanResidue[] matchedGlycanResidues;

    
    public SubstructureQueryResult( GlycanSequence match )
    {
        this.glycanSequence = match;
        this.matchedGlycanResidues = null;//matched_residues;
    }


    //  mjh: this *should* work, but it doesn't. temporarily disabled 
    //  until someone requests it...
    /*
    public SubstructureQueryResult( GlycanSequence match, GlycanResidue... matched_residues )
    {
        this.glycanSequence = match;
        this.matchedGlycanResidues = matched_residues;
    }
    */

    /** 
    *   Returns the {@link GlycanSequence} that was matched by the 
    *   {@link SubstructureQuery} that creating this 
    *   {@link SubstructureQueryResult}
    */
    public GlycanSequence getMatchedGlycanSequence()
    {
        return glycanSequence;    
    }
    
} // end class SubstructureQueryResult

