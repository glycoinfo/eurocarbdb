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

public class ProfilerOptions {
          
    public String[] DICTIONARIES = new String[] {"Carbbank"};

    public String DERIVATIZATION = MassOptions.NO_DERIVATIZATION;
    public String REDUCING_END = "freeEnd";
    public String OTHER_REDEND_NAME = "";
    public double OTHER_REDEND_MASS = 0.;

    public String LAST_DICT_NAME = "";
    public String LAST_TYPE = "";
    public String LAST_SOURCE = "";
    public List<String> USER_DICTIONARIES_FILENAME = new ArrayList<String>();
    
    public MassOptions getMassOptions() {    
    if( REDUCING_END.equals("XXX") ) {
        ResidueType re_type = ResidueType.createOtherReducingEnd(OTHER_REDEND_NAME,OTHER_REDEND_MASS);
        return new MassOptions(DERIVATIZATION,re_type.getName());
    }
    return new MassOptions(DERIVATIZATION,REDUCING_END);
    }

    // serialization    

    public void store(Configuration config) {
    config.put("ProfilerOptions","dictionaries",DICTIONARIES,',');    
    config.put("ProfilerOptions","derivatization",DERIVATIZATION);    
    config.put("ProfilerOptions","reducing_end",REDUCING_END);    
    config.put("ProfilerOptions","other_redend_name",OTHER_REDEND_NAME);    
    config.put("ProfilerOptions","other_redend_mass",OTHER_REDEND_MASS);    
    config.put("ProfilerOptions","last_dict_name",LAST_DICT_NAME);    
    config.put("ProfilerOptions","last_type",LAST_TYPE);    
    config.put("ProfilerOptions","last_source",LAST_SOURCE);    
    config.put("ProfilerOptions","user_dictionaries_filename",USER_DICTIONARIES_FILENAME,',');    
    }

    public void retrieve(Configuration config) {

    DICTIONARIES = config.get("ProfilerOptions","dictionaries",DICTIONARIES,',');    
    DERIVATIZATION = config.get("ProfilerOptions","derivatization",DERIVATIZATION);    
    REDUCING_END = config.get("ProfilerOptions","reducing_end",REDUCING_END);    
    OTHER_REDEND_NAME = config.get("ProfilerOptions","other_redend_name",OTHER_REDEND_NAME);    
    OTHER_REDEND_MASS = config.get("ProfilerOptions","other_redend_mass",OTHER_REDEND_MASS);    
    LAST_DICT_NAME = config.get("ProfilerOptions","last_dict_name",LAST_DICT_NAME);    
    LAST_TYPE = config.get("ProfilerOptions","last_type",LAST_TYPE);    
    LAST_SOURCE = config.get("ProfilerOptions","last_source",LAST_SOURCE);    
    USER_DICTIONARIES_FILENAME = config.get("ProfilerOptions","user_dictionaries_filename",USER_DICTIONARIES_FILENAME,',');    
    }


}
