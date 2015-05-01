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

public class GAGOptions {
    
    public String[] GAG_FAMILIES = new String[] {"heparan"};
    
    public int MIN_NO_UNITS = 1;
    public int MAX_NO_UNITS = 5;

    public int MIN_NO_ACETYLS = 0;
    public int MAX_NO_ACETYLS = 999;

    public int MIN_NO_SULFATES = 0;
    public int MAX_NO_SULFATES = 999;

    public boolean IS_REDUCED = false;
    public boolean IS_UNSATURATED = true;   
    public boolean ALLOW_REDEND_LOSS = false;
    public boolean ALLOW_UNLIKELY_ACETYLATION = false;
    public String DERIVATIZATION = MassOptions.NO_DERIVATIZATION;

    public static final String NO_MODIFICATIONS = "None";
    public static final String DE_2_SULFATION = "De 2-sulfation";
    public static final String DE_6_SULFATION = "De 6-sulfation";
    public static final String DE_N_SULFATION = "De N-sulfation";
    public static final String RE_ACETYLATION = "Re acetylation";
    public static final String[] ALL_MODIFICATIONS = new String[] {NO_MODIFICATIONS, DE_2_SULFATION, DE_6_SULFATION, DE_N_SULFATION, RE_ACETYLATION};
    public String[] MODIFICATIONS = new String[] {};
    
    //

    public boolean containsModification(String mod) {
    for( int i=0; i<MODIFICATIONS.length; i++ ){
        if( MODIFICATIONS[i].equals(mod) ) 
        return true;
    }
    return false;
    }

    // serialization

    public void store(Configuration config) {
    config.put("GAGOptions","gag_families",GAG_FAMILIES,',');
    
    config.put("GAGOptions","min_no_units",MIN_NO_UNITS);
    config.put("GAGOptions","max_no_units",MAX_NO_UNITS);

    config.put("GAGOptions","min_no_acetyls",MIN_NO_ACETYLS);
    config.put("GAGOptions","max_no_acetyls",MAX_NO_ACETYLS);

    config.put("GAGOptions","min_no_sulfates",MIN_NO_SULFATES);
    config.put("GAGOptions","max_no_sulfates",MAX_NO_SULFATES);

    config.put("GAGOptions","is_reduced",IS_REDUCED);
    config.put("GAGOptions","is_unsaturated",IS_UNSATURATED);

    config.put("GAGOptions","derivatization",DERIVATIZATION);    

    config.put("GAGOptions","modifications",MODIFICATIONS,',');
    config.put("GAGOptions","allow_unlikely_acetylation",ALLOW_UNLIKELY_ACETYLATION);
    config.put("GAGOptions","allow_redend_loss",ALLOW_REDEND_LOSS);
    }

    public void retrieve(Configuration config) {
    GAG_FAMILIES = config.get("GAGOptions","gag_families",GAG_FAMILIES,',');
    
    MIN_NO_UNITS = config.get("GAGOptions","min_no_units",MIN_NO_UNITS);
    MAX_NO_UNITS = config.get("GAGOptions","max_no_units",MAX_NO_UNITS);

    MIN_NO_ACETYLS = config.get("GAGOptions","min_no_acetyls",MIN_NO_ACETYLS);
    MAX_NO_ACETYLS = config.get("GAGOptions","max_no_acetyls",MAX_NO_ACETYLS);

    MIN_NO_SULFATES = config.get("GAGOptions","min_no_sulfates",MIN_NO_SULFATES);
    MAX_NO_SULFATES = config.get("GAGOptions","max_no_sulfates",MAX_NO_SULFATES);

    IS_REDUCED = config.get("GAGOptions","is_reduced",IS_REDUCED);
    IS_UNSATURATED = config.get("GAGOptions","is_unsaturated",IS_UNSATURATED);    

    DERIVATIZATION = config.get("GAGOptions","derivatization",DERIVATIZATION);    

    MODIFICATIONS = config.get("GAGOptions","modifications",MODIFICATIONS,',');
    ALLOW_UNLIKELY_ACETYLATION = config.get("GAGOptions","allow_unlikely_acetylation",ALLOW_UNLIKELY_ACETYLATION);
    ALLOW_REDEND_LOSS = config.get("GAGOptions","allow_redend_loss",ALLOW_REDEND_LOSS);

    }


}
