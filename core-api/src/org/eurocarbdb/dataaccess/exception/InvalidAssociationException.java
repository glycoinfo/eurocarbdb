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
*   This exception indicates that a required association/link 
*   between 2 objects was not found (or was invalid) in an object 
*   during {@link EurocarbObject#validate object validation}.
*
*   @author mjh
*/
public class InvalidAssociationException extends DataException
{
    public final EurocarbObject invalidObject;
    
    public final Class<?> missingObjectClass;
    
    /**  @param message is optional */
    public InvalidAssociationException
    ( 
        EurocarbObject invalid, 
        Class<?> associationClass, 
        String message 
    ) 
    {  
        super( message );  
        this.invalidObject = invalid;
        this.missingObjectClass = associationClass;
    }
    
    
    public String getMessage()
    {
        return "Object "
            + invalidObject
            + "of class " 
            + invalidObject.getClass().getName()
            + " is invalid because it requires an association of object(s) of class "
            + missingObjectClass.getName()
            + ((message != null && message.length() > 0) 
                ? ": " + message
                : "" )
            ;
    }

} // end class
