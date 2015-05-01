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

import org.eurocarbdb.dataaccess.core.Contributor;


/**
*   This is the base class of exceptions thrown by Eurocarb code.
*
*   @author mjh
*/
public class EurocarbException extends RuntimeException
{
    public String message;
    
    /**
    *   If applicable, this is the {@link Contributor} whose access 
    *   caused this exception; may be null.
    */
    public Contributor contributor = null;

    /**
    *   If applicable, this is the underlying exception that caused
    *   this EurocarbException; may be null.
    */
    public Exception underlyingException = null;
    
    /** Minimal contructor. */
    public EurocarbException( String message ) 
    {  
        this.message = message;  
    }
    
    
    public String getMessage()
    {
        return message;   
    }

} // end class
