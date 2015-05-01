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

import java.util.Map;

import org.apache.log4j.Logger;

// import com.opensymphony.webwork.ServletActionContext;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Result;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.AbstractLifecycleInterceptor;

import org.eurocarbdb.action.EurocarbAction;

/**
*   Produces an XML result if the intercepted {@link EurocarbAction}
*   returns "xml" from its getOutput() method, and returns true from
*   its canGenerateXml method.
*
*   @author     mjh
*   @version    $Rev: 1549 $
*/
public class XmlSerialiserInterceptor extends AbstractLifecycleInterceptor 
{
    /** logging handle */
    static Logger log = Logger.getLogger( XmlSerialiserInterceptor.class );    
    
    
    public void beforeResult( ActionInvocation invocation, String resultcode ) 
    {
        //Map params = invocation.getInvocationContext().getParameters();
        //log.debug("checking params for output=xml");
        
        EurocarbAction action = null;
        try 
        {
            action = (EurocarbAction) invocation.getAction();   
        }
        catch ( Exception ex ) {  return;  }
        
        if ( ! "xml".equalsIgnoreCase( action.getOutput() ) )
            return;

        if ( ! action.canGenerateXml( resultcode ) )
        {
            log.info("Action " 
                + action.getCurrentActionName() 
                + " reports it does not support XML result generation for result code="
                + resultcode 
                + ", skipping XML generation..."
            );        
            
            return;
        }
        
        String new_resultcode = resultcode + "-xml";
        String action_name = action.getCurrentActionName();
        
        log.info(
            "Generating XML result for action=" 
            + action_name
            + ", result code=" 
            + resultcode 
            + "; trying to render XML result code="
            + new_resultcode
        );
        
        invocation.setResultCode( new_resultcode );
        
        return;
    }
    
} // end class



