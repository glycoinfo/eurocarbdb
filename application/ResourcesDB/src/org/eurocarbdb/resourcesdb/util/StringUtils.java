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

public class StringUtils {

    /**
     * Make a String of a given size containing of blanks only
     * @param size the length of the String
     * @return a String of the given size or an empty String if size < 1
     */
    public static String makeBlanks(int size) {
        return multiplyString(" ", size);
    }
    
    /**
     * "Multiply" a String, i.e. write it n times 
     * @param sourceStr the String to be multiplied
     * @param count the number of repeats
     * @return a String containing count times the sourceStr
     */
    public static String multiplyString(String sourceStr, int count) {
        String outStr = "";
        for(int i = 0; i < count; i++) {
            outStr += sourceStr;
        }
        return outStr;
    }

    /**
     * "Multiply" a character, i.e. write it n times to a String 
     * @param sourceChar the char to be multiplied
     * @param count the number of repeats
     * @return a String containing count times the sourceChar
     */
    public static String multiplyChar(char sourceChar, int count) {
        String outStr = "";
        for(int i = 0; i < count; i++) {
            outStr += sourceChar;
        }
        return outStr;
    }

    /**
     * String comparison, in which a null String equals an empty String
     * @param str1
     * @param str2
     * @return
     */
    public static boolean strCmpNullEqualsEmpty(String str1, String str2) {
        if(str1 == null || str1.equals("")) {
            if(str2 == null || str2.equals("")) {
                return(true);
            }
        }
        if(str1 == null) {
            return(false);
        }
        return(str1.equals(str2));
    }

    /**
     * Check, if a String is null, and make it an empty String in that case.
     * This method is an alias for calling <code>nullSaveString</code> with an empty String as second argument.
     * @param inStr the String to be checked
     * @return an empty String, if inStr is null, otherwise the inStr
     */
    public static String nullStrToEmptyStr(String inStr) {
        return nullSaveString(inStr, "");
    }
    
    /**
     * Check, if a String is null, and return a default String in that case.
     * @param inStr the String to be checked
     * @param nullDefault the String to be returned in case inStr is null
     * @return the given default String, if inStr is null, otherwise the inStr
     */
    public static String nullSaveString(String inStr, String nullDefault) {
        if(inStr == null) {
            return nullDefault;
        } else {
            return inStr;
        }
    }

    /**
     * Find the position of a closing bracket that matches an opening bracket.
     * @param inStr the String to parse, must start with an opening bracket: "(", "{", "[" or "<"
     * @return the position of the matching closing bracket or -1 if no such bracket is found
     */
    public static int findClosingBracketPosition(String inStr) {
        if(inStr != null && inStr.length() > 0) {
            String startBracket = inStr.substring(0, 1);
            String endBracket = null;
            if(startBracket.equals("(")) {
                endBracket = ")";
            } else if(startBracket.equals("{")) {
                endBracket = "}";
            } else if(startBracket.equals("[")) {
                endBracket = "]";
            } else if(startBracket.equals("<")) {
                endBracket = ">";
            }
            if(endBracket != null) {
                int openBracketCount = 1;
                for(int i = 1; i < inStr.length(); i++) {
                    if(inStr.substring(i, i + 1).equals(startBracket)) {
                        openBracketCount++;
                    } else if(inStr.substring(i, i + 1).equals(endBracket)) {
                        openBracketCount--;
                        if(openBracketCount == 0) {
                            return(i);
                        }
                    }
                }
            }
        }
        return(-1);
    }

    public static String camelCase(String inStr, int start, int end) {
        String outStr = "";
        outStr += inStr.substring(0, start).toLowerCase();
        outStr += inStr.substring(start, end + 1).toUpperCase();
        if(end < inStr.length()) {
            outStr += inStr.substring(end + 1).toLowerCase();
        }
        return(outStr);
    }

    public static String camelCase(String inStr, int index) {
        return(camelCase(inStr, index, index));
    }

    public static String camelCase(String inStr) {
        return(camelCase(inStr, 0));
    }

}
