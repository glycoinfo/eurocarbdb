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

package org.eurocarbdb.interceptor;

import com.opensymphony.webwork.interceptor.CookieInterceptor;
import com.opensymphony.xwork.ActionInvocation;


/**
* Workaround for a bug in CookieInterceptor. 
* 
* @author             aceroni
* @version                $Rev: 1549 $
*/
public class DefaultCookieInterceptor extends CookieInterceptor {

  
    protected void before(ActionInvocation invocation) {
    try {
        super.before(invocation);
    }
    catch(Exception e){
    }

    }
    
}
