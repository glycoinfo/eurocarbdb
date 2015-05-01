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
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/

package org.eurocarbdb.dataaccess.exception;

import org.eurocarbdb.dataaccess.EurocarbObject;


/**
*   This exception indicates that a property of an object
*   was found to have invalid/uninitialised data in one of its 
*   properties during {@link EurocarbObject#validate object validation}.
*
*   @author mjh
*/
public class InvalidPropertyException extends DataException
{
    public final EurocarbObject invalidObject;
    
    public final String invalidPropertyName;
    
    
    /**  */
    public InvalidPropertyException
    ( 
        EurocarbObject invalid, 
        String invalidPropertyName, 
        String message 
    ) 
    {  
        super( message );  
        this.invalidObject = invalid;
        this.invalidPropertyName = invalidPropertyName;
    }
    
    
    public String getMessage()
    {
        return "Object "
            + invalidObject
            + "of class " 
            + invalidObject.getClass().getName()
            + " is invalid because it the property named '"
            + invalidPropertyName
            + " contains invalid/uninitialised data"
            + ((message != null && message.length() > 0) 
                ? ": " + message
                : "" )
            ;
    }

} // end class
