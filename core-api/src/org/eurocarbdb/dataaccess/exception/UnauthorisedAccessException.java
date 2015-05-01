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

import java.io.Serializable;

import org.eurocarbdb.dataaccess.EurocarbObject;
import org.eurocarbdb.dataaccess.core.Contributor;


/**
*   This exception specifically indicates that a {@link EurocarbObject} 
*   object of a given {@link DataAccessException#classAccessed class} 
*   and given {@link DataAccessException#idAccessed id} was accessed
*   by a {@link DataAccessException#contributor contributor} who didn't  
*   have the appropriate level of access.
*
*   @author mjh
*/
public class UnauthorisedAccessException extends DataAccessException
{
    
    //~~~~ constructors ~~~~
    
    /** Minimal contructor. */
    public UnauthorisedAccessException( String message ) 
    {  
        super( message );  
    }
    
    
    /** 
    *   Constructs an exception that contains which class/id object was 
    *   trying to be accessed by which Contributor when this exception 
    *   was generated.
    */
    public UnauthorisedAccessException( String message,
                                        Class<EurocarbObject> classAccessed, 
                                        Serializable idAccessed,
                                        Contributor c )
    {
        super( message, classAccessed, idAccessed, c );
    }
    
    
    //~~~~ methods ~~~~
    
} // end class
