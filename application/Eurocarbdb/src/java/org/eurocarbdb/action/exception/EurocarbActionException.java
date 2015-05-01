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
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/

package org.eurocarbdb.action.exception;

import org.eurocarbdb.action.EurocarbAction;
import static org.eurocarbdb.util.StringUtils.toUnderscoreCase;

public abstract class EurocarbActionException extends Exception
{
    protected EurocarbAction action;

    public EurocarbActionException( EurocarbAction a ) 
    {
        this( a, "(no message given)" );
    }
    
    
    public EurocarbActionException( EurocarbAction a, String message ) 
    {
        super( message );
        this.action = a;
        
        a.addActionError( message );
    }
    
    
    /** 
    *   Returns the canonic name of this exception class/type. 
    */
    public String getName()
    {
        return "error_" + toUnderscoreCase( this.getClass().getName() );    
    }
    
    
    public EurocarbAction getAction() {  return action;  }
    
}

