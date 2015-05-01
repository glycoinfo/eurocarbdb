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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
/**
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

package org.eurocarbdb.application.glycoworkbench.plugin;

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;
import java.util.*;

public class CollectionStructureGenerator implements StructureGenerator {
    
    private Collection<Glycan> structures = null;
    private Iterator<Glycan> structure_iter = null;

    public CollectionStructureGenerator() {
    structures = new Vector<Glycan>();
    }

    public CollectionStructureGenerator(Collection<Glycan> c) {
    if( c!=null )
        structures = c;
    else
        structures = new Vector<Glycan>();
    }    

    
    public void start(MassOptions _mass_opt) {    
    structure_iter = structures.iterator();
    }

    public FragmentEntry next(boolean backtrack) {
    if( structure_iter.hasNext() )
        return new FragmentEntry(structure_iter.next(),"");
    return null;    
    }
    
    public double computeScore(Glycan structure) {
    return 0.;
    }

}
