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

import java.io.*;

public class FileUtils {

    public static String readTextFile(String filename) {
        String inStr = null;
        try {
            FileReader fileRead = new FileReader(filename);
            BufferedReader bufRead = new BufferedReader(fileRead);
            inStr = "";
            String line = bufRead.readLine();
            while(line != null) {
                inStr += line + "\n";
                line = bufRead.readLine();
            }
        } catch(Exception ex) {
            System.err.println("exception in readBinaryFile(" + filename + "): " + ex);
        }
        return inStr;
    }
    
    /**
     * Read a binary file into a byte array
     * @param filename the file name of the file to read
     * @return the byte array read from the file or null in case an exception occurs during reading
     */
    public static byte[] readBinaryFile(String filename) {
        byte[] inArr = null;
        try {
            FileInputStream inStream = new FileInputStream(filename);
            int numberBytes = inStream.available();
            inArr = new byte[numberBytes];
            inStream.read(inArr);
            inStream.close();
        } catch(Exception ex) {
            System.err.println("exception in readBinaryFile(" + filename + "): " + ex);
        }
        return inArr;
    }
}
