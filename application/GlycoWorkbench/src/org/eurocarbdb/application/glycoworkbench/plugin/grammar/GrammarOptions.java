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

package org.eurocarbdb.application.glycoworkbench.plugin.grammar;

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;

public class GrammarOptions {

    public boolean ADD_LINKAGE_INFO = false;
    public int MAX_LEVEL = 3;
    public boolean ADD_UNCLES = true;
    public boolean USE_SEEDS = false;
    public boolean TAG_CORES = true;

    public void store(Configuration config) {
    config.put("GrammarOptions","add_linkage_info",ADD_LINKAGE_INFO);
    config.put("GrammarOptions","max_level",MAX_LEVEL);
    config.put("GrammarOptions","add_uncles",ADD_UNCLES);
    config.put("GrammarOptions","use_seeds",USE_SEEDS);
    config.put("GrammarOptions","tag_cores",TAG_CORES);
    }

    public void retrieve(Configuration config) {
    ADD_LINKAGE_INFO = config.get("GrammarOptions","add_linkage_info",ADD_LINKAGE_INFO);
    MAX_LEVEL = config.get("GrammarOptions","max_level",MAX_LEVEL);
    ADD_UNCLES = config.get("GrammarOptions","add_uncles",ADD_UNCLES);
    USE_SEEDS = config.get("GrammarOptions","use_seeds",USE_SEEDS);
    TAG_CORES = config.get("GrammarOptions","tag_cores",TAG_CORES);
    }

}