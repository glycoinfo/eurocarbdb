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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-11 #$  
*/

package org.eurocarbdb.interceptor;

import org.apache.log4j.Logger;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.AbstractLifecycleInterceptor;

import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.action.EditingAction;
import org.eurocarbdb.action.exception.InsufficientPermissions;


/**
*   Ensures that {@link Contributor}s running actions that implement 
*   the {@link EditingAction} interface are authorised to edit/change
*   the objects used by that action.
*
*   @see EditingAction#checkPermissions
*
*   @author hirenj
*   @author mjh
*/
public class AuthorisationInterceptor extends AbstractLifecycleInterceptor
{
    /** Logging handle. */
    static final Logger log = Logger.getLogger( AuthorisationInterceptor.class );

    /**
    *   When running an editing action, check the permissions for the action.
    *   Each action should know which permissions it requires, and set the 
    *   error state appropriately if the permissions are incorrect.
    */
    public void before( ActionInvocation invocation ) throws InsufficientPermissions 
    {
        Object action = invocation.getAction();
        if (action instanceof EditingAction) 
        {
            ((EditingAction) action).checkPermissions();
        }
    }    
}