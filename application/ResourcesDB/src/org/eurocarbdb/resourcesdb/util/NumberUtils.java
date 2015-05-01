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

public class NumberUtils {

    /**
     * Exception-save parsing of a Double value from a String.
     * @param str the String to be parsed
     * @param defaultValue the Double object to be returned if an exception occurs while parsing the String
     * @return the result of Double.parseDouble(str) if parsing was successful, otherwise the defaultValue
     */
    public static Double parseDoubleStr(String str, Double defaultValue) {
        try {
            return(Double.parseDouble(str));
        } catch(Throwable e) {
            return(defaultValue);
        }
    }

    /**
     * Exception-save parsing of an Integer value from a String.
     * @param str the String to be parsed
     * @param defaultValue the Integer object to be returned if an exception occurs while parsing the String
     * @return the result of Integer.parseInt(str) if parsing was successful, otherwise the defaultValue
     */
    public static Integer parseIntStr(String str, Integer defaultValue) {
        try {
            return(Integer.parseInt(str));
        } catch(Throwable e) {
            return(defaultValue);
        }
    }
    
    /**
     * Exception-save parsing of multiple Integer values from a String.
     * @param str the String to be parsed
     * @param delim a regular expression that defines the delimiter that separates the int values in the String (the argument to be used to call <code>String.split(String regex)</code>)
     * @param defaultValue the Integer object to be added to the results list if an ecxeption occurs while parsing a single int value
     * @return an ArrayList containing the Integer values which - separated by the given delimiter - are encoded in the given String
     */
    public static ArrayList<Integer> parseMultipleIntStr(String str, String delim, Integer defaultValue) {
        ArrayList<Integer> resultList = new ArrayList<Integer>();
        String[] substrings = str.split(delim);
        for(String substr : substrings) {
            try {
                Integer strValue = Integer.parseInt(substr);
                resultList.add(strValue);
            } catch(Throwable e) {
                resultList.add(defaultValue);
            }
        }
        return resultList;
    }

    /**
     * NullPointerException-save conversion of a Double Object to a simple double value.
     * @param theDouble the Double Object to be converted
     * @param defaultValue the value to be returned if theDouble is null
     * @return theDouble.doubleValue() if theDouble is not null, otherwise the defaultValue
     */
    public static double nullsaveDoubleValue(Double theDouble, double defaultValue) {
        if(theDouble != null) {
            return theDouble.doubleValue();
        } else {
            return defaultValue;
        }
    }

    /**
     * NullPointerException-save conversion of a Double Object to a simple double value.
     * @param theDouble the Double Object to be converted
     * @return theDouble.doubleValue() if theDouble is not null, otherwise 0.0
     */
    public static double nullsaveDoubleValue(Double theDouble) {
        return nullsaveDoubleValue(theDouble, 0.0);
    }

    /**
     * NullPointerException-save conversion of an Integer Object to a simple int value.
     * @param theInt the Integer Object to be converted
     * @param defaultValue the value to be returned if theInt is null
     * @return theInt.intValue() if theInt is not null, otherwise the defaultValue
     */
    public static int nullsaveIntValue(Integer theInt, int defaultValue) {
        if(theInt != null) {
            return theInt.intValue();
        } else {
            return defaultValue;
        }
    }

    /**
     * NullPointerException-save conversion of an Integer Object to a simple int value.
     * @param theInt the Integer Object to be converted
     * @return theInt.intValue() if theInt is not null, otherwise 0
     */
    public static int nullsaveIntValue(Integer theInt) {
        return nullsaveIntValue(theInt, 0);
    }

}
