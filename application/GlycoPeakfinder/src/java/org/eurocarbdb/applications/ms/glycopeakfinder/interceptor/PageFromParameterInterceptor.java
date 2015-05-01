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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
package org.eurocarbdb.applications.ms.glycopeakfinder.interceptor;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.AroundInterceptor;
import com.opensymphony.xwork.interceptor.NoParameters;
import com.opensymphony.xwork.util.OgnlContextState;
import com.opensymphony.xwork.util.OgnlValueStack;

import java.util.Map;

/**
* Gets the pageFrom input field and set it to the Action.
* 
* @author Rene Ranzinger
*/
public class PageFromParameterInterceptor extends AroundInterceptor 
{
    private static final long serialVersionUID = 1L;

    /**
     * not used
     */
    protected void after(ActionInvocation dispatcher, String result) throws Exception 
    {
    }
    
    protected void before(ActionInvocation invocation) throws Exception 
    {
        // have params?
        if (!(invocation.getAction() instanceof NoParameters)) 
        {
            // yes
            ActionContext t_objContext = invocation.getInvocationContext();
            final Map t_mapParameter = t_objContext.getParameters();
            
            if (t_mapParameter != null) 
            {
                Map t_objContextMap = t_objContext.getContextMap();
                try 
                {
                    OgnlContextState.setCreatingNullObjects(t_objContextMap, true);
       //             OgnlContextState.setDenyMethodExecution(t_objContextMap, true);
                    OgnlContextState.setReportingConversionErrors(t_objContextMap, true);
                    
                    OgnlValueStack stack = t_objContext.getValueStack();
                    if ( t_mapParameter.containsKey("pageFrom"))
                    {
                        Object value = t_mapParameter.get("pageFrom");
                        stack.setValue("pageFrom", value);
                    }
                } 
                finally 
                {
                    OgnlContextState.setCreatingNullObjects(t_objContextMap, false);
         //           OgnlContextState.setDenyMethodExecution(t_objContextMap, false);
                    OgnlContextState.setReportingConversionErrors(t_objContextMap, false);
                }
            }
        }
    }
}