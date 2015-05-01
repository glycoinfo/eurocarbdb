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

package org.eurocarbdb.dataaccess;

import java.util.Date;
import org.eurocarbdb.dataaccess.core.Contributor;

/**
*   This interface indicates that an object is trackable
*   through its association to a {@link Contributor} (user).
*
*   @author mjh
*/
public interface Contributed
{
   
    /** Returns the {@link Contributor} of this object. */
    public Contributor getContributor()
    ;
    
    
    /** Sets the {@link Contributor} of this object. */
    public void setContributor( Contributor c )
    ;
    
    
    /** Returns the {@link Date} this object was contributed. */
    public Date getDateEntered()
    ;
    
}
