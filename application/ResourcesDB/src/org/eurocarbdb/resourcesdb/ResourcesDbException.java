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
package org.eurocarbdb.resourcesdb;

/**
* The standard exception within the resources db project.
* This exception is extended by other exceptions, which mark certain types of errors.
* @see org.eurocarbdb.resourcesdb.io.NameParsingException
* @see org.eurocarbdb.resourcesdb.monosaccharide.MonosaccharideException
* @see org.eurocarbdb.resourcesdb.nonmonosaccharide.NonmonosaccharideException
*
* @author Thomas Lütteke
*/
public class ResourcesDbException extends Exception {


    /**
     * @param string
     */
    public ResourcesDbException(String message) {
        super(message);
    }
    
    public ResourcesDbException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method is only functional in the Subclass NameParsingException.
     * It was added here to avoid class casts.
     * @return an empty String
     * @see org.eurocarbdb.resourcesdb.io.NameParsingException.buildExplanationString()
     */
    public String buildExplanationString() {
        return "";
    }
}
