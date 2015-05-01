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
package org.eurocarbdb.resourcesdb.util;

import java.util.ArrayList;
import org.eurocarbdb.resourcesdb.*;

public class Utils {
    
    /**
     * generate an output String of modification positions from a list of potential positions
     * @param positions: ArrayList containing the possible positions
     * @param delimiter: Character to be used to separate alternative positions
     * @param unknownPositionLabel: Character(s) to be used to label unknown positions
     * @return
     */
    public static String formatPositionsString(ArrayList<Integer> positions, String delimiter, String unknownPositionLabel) {
        String outStr = "";
        if(positions != null && positions.size() > 0) {
            Integer pos0 = positions.get(0);
            if(pos0.intValue() == 0) {
                outStr += unknownPositionLabel;
            } else {
                outStr += pos0;
            }
            for(int i = 1; i < positions.size(); i++) {
                outStr += delimiter + positions.get(i);
            }
        } else {
            outStr = unknownPositionLabel;
        }
        return(outStr);
    }
    
    /**
     * Get a clone of an ArrayList of Integers.
     * Not only the List itself but also the component Integers are cloned.
     * @param sourceList the list to be cloned
     * @return the clone of the list
     */
    public static ArrayList<Integer> cloneIntegerList(ArrayList<Integer> sourceList) {
        ArrayList<Integer> outputList = new ArrayList<Integer>();
        for(Integer position : sourceList) {
            outputList.add(new Integer(position.intValue()));
        }
        return(outputList);
    }
    
    /**
     * Parse a String into a true/false value.
     * Comparisons used in this method are not case-sensitive.
     * @param str the String to be parsed
     * @param defaultValue 
     * @return true, if the given String equals "true" or "yes", false, if it equals "false" or "no", the defaultValue in all other cases
     */
    public static Boolean parseTrueFalseString(String str, Boolean defaultValue) {
        if(str != null) {
            if(str.equalsIgnoreCase("true")) {
                return(new Boolean(true));
            }
            if(str.equalsIgnoreCase("false")) {
                return(new Boolean(false));
            }
            if(str.equalsIgnoreCase("yes")) {
                return(new Boolean(true));
            }
            if(str.equalsIgnoreCase("no")) {
                return(new Boolean(false));
            }
        }
        return(defaultValue);
    }
    
    /**
     * Set the template data that is needed for monosaccharide parsing and handling (if not set yet).
     * This includes elements, monosaccharide / substituent / trivialname / aglycon templates
     * @param conf the Config Object that indicates the template sources
     * @throws ResourcesDbException
     */
    public static void setTemplateDataIfNotSet(Config conf) throws ResourcesDbException {
        //Periodic.setDataIfNotSet(conf);
    }
    
    /**
     * Set the template data that is needed for monosaccharide parsing and handling (if not set yet).
     * This includes elements, monosaccharide / substituent / trivialname / aglycon templates.
     * Template sources are read from the global config.
     * @throws ResourcesDbException
     */
    public static void setTemplateDataIfNotSet() throws ResourcesDbException {
        Utils.setTemplateDataIfNotSet(Config.getGlobalConfig());
    }
        
}
