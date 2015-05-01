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

public class DictionaryStructureGenerator implements StructureGenerator {
    
    private Collection<StructureDictionary> dictionaries = null;

    private MassOptions mass_opt = null;
    private Iterator<StructureDictionary> sdi = null;
    private Iterator<StructureType> sti = null;
    private StructureDictionary last_dict = null;
    private StructureType last_type = null;


    public DictionaryStructureGenerator() {
    dictionaries = new Vector<StructureDictionary>();
    }


    public DictionaryStructureGenerator(StructureDictionary dict) {
    if( dict!=null )
        dictionaries = Collections.singleton(dict);
    else
        dictionaries = new Vector<StructureDictionary>();
    }

    public DictionaryStructureGenerator(Collection<StructureDictionary> dicts) {
    if( dicts!=null )
        dictionaries = dicts;
    else
        dictionaries = new Vector<StructureDictionary>();
    }

    public void add(StructureDictionary dict) {
    dictionaries.add(dict);
    }

    public void start(MassOptions _mass_opt) {
    mass_opt = _mass_opt;
    
    sdi = dictionaries.iterator();
    sti = null;
    last_dict = null;
    last_type = null;
    }

    public FragmentEntry next(boolean backtrack) {
    for(;;) {
        if( sti==null || !sti.hasNext() ) {
        if( sdi.hasNext() ) {
            last_dict = sdi.next();
            sti = last_dict.iterator();
        }
        else
            return null;
        }
        else {
        try {
            last_type = sti.next();
            return last_type.generateFragmentEntry(mass_opt);
        }
        catch(Exception e) {
            LogUtils.report(e);
            return null;
        }        
        }
    }    
    }
    
    public double computeScore(Glycan structure) {
    if( last_dict!=null && last_dict.getScorer()!=null )
        return last_dict.getScorer().computeScore(structure);
    return 0.;
    }

}
