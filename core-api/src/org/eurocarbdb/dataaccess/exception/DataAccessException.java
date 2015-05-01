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
*   This exception class is indicative of a general failure that 
*   occurred while fetching Eurocarb data.
*
*   @author mjh
*/
public class DataAccessException extends EurocarbException
{
    /** 
    *   If applicable, this is the Eurocarb {@link Class} that
    *   was being accessed when this exception occurred.
    */
    public Class<EurocarbObject> classAccessed = null;
    
    /**
    *   If applicable, this is the id of the class object given
    *   by {@link #classAccessed} that was being accessed when this
    *   exception occurred.
    */
    public Serializable idAccessed = null;
    
    
    //  constructors
    
    /** Minimal contructor. */
    public DataAccessException( String message ) 
    {  
        super( message );  
    }
    
    /** 
    *   Constructs an exception that contains which class/id object was 
    *   trying to be accessed by which Contributor when this exception 
    *   was generated.
    */
    public DataAccessException( String message,
                                Class<EurocarbObject> classAccessed, 
                                Serializable idAccessed,
                                Contributor c )
    {
        super( message );
        this.classAccessed = classAccessed;
        this.idAccessed    = idAccessed;
        this.contributor   = c;
    }

    
    //~~~~ methods ~~~~
  /*   
    @Override
    public String getMessage()
    {
        if ( classAccessed != null )
        {
            return "";   
        }
    } */

} // end class



