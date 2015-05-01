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

package org.eurocarbdb.action.exception;

import org.eurocarbdb.action.EurocarbAction;
/**
 * Exception thrown when executing actions and the permissions of the contributor
 * are not of a high enough/broad enough level.
 */
public class InsufficientPermissions extends EurocarbActionException
{
    public InsufficientPermissions( EurocarbAction a ) {  super( a );  }
    
    public InsufficientPermissions( EurocarbAction a, String msg ) {  super( a, msg );  }    
}
