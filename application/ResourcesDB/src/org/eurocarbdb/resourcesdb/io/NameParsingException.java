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
package org.eurocarbdb.resourcesdb.io;

import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.util.StringUtils;

public class NameParsingException extends ResourcesDbException {

    private String nameStr;
    private int position = -1;
    
    /**
     * @param string
     */
    public NameParsingException(String message) {
        super(message);
    }
    
    public NameParsingException(String message, String name, int pos) {
        super(message);
        this.setNameStr(name);
        this.setPosition(pos);
    }

    public NameParsingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public NameParsingException(String message, String name, int pos, Throwable cause) {
        super(message, cause);
        this.setNameStr(name);
        this.setPosition(pos);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String getNameStr() {
        return this.nameStr;
    }

    public void setNameStr(String name) {
        this.nameStr = name;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int pos) {
        this.position = pos;
    }
    
    /**
     * Build a String that contains the parsed residue name in one line
     * and a "^" at the position that caused the exception in the second line
     * @return a two-line String
     */
    public String buildExplanationString() {
        String outStr = "";
        if(this.getPosition() >= 0 && this.getNameStr() != null) {
            outStr += this.getNameStr() + "\n";
            outStr += StringUtils.makeBlanks(this.getPosition());
            outStr += "^";
        }
        return outStr;
    }

}
