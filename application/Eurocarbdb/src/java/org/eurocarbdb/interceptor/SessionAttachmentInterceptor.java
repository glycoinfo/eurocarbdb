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

//  3rd party imports 
import org.apache.log4j.Logger;

import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionProxy;

import java.util.Map;
import java.util.Collection;

import com.opensymphony.webwork.interceptor.ScopeInterceptor;

import org.eurocarbdb.dataaccess.EurocarbObject;
import org.eurocarbdb.dataaccess.core.*;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
* Reattach all EUROCarbDB objects stored in the session
* 
* @author             hirenj
* @version            $Rev: 1549 $
*/
public class SessionAttachmentInterceptor extends ScopeInterceptor {

    public String[] session;
    public String key;

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//
   
    /** Logging handle. */
    protected static final Logger log  = Logger.getLogger( SessionAttachmentInterceptor.class.getName() );

    private String getKey(ActionInvocation invocation) {
        ActionProxy proxy = invocation.getProxy();
        if (key == null || "CLASS".equals(key)) {
            return "webwork.ScopeInterceptor:" + proxy.getAction().getClass();
        } else if ("ACTION".equals(key)) {
            return "webwork.ScopeInterceptor:" + proxy.getNamespace() + ":" + proxy.getActionName();
        }
        return key;
    }

    //list of session scoped properties
    public void setKey(String key) {
        this.key = key;
        super.setKey(key);
    }


    //list of session scoped properties
    public void setSession(String s) {
        if (s != null) {
            session = s.split(" *, *");
        }
        super.setSession(s);
    }

    @SuppressWarnings(value = "unchecked")  
    protected void before(ActionInvocation invocation) {
        /*
         * Run the original interceptor first. This will re-populate any
         * variables in the actions
         */
        try {
            super.before(invocation);
        } catch (Exception e) {
            log.warn("Could not re-attach session, skipping");
        }
        
        /*
         * Loop through the session values, checking to see if a EurocarbObject
         * or a Collection<EurocarbObject> is stored within the session. If
         * any of these do exist, go through, find each of the EurocarbObjects
         * and re-attach them to the session.
         */        
        Map<String,Object> sess = (Map<String,Object>) ActionContext.getContext().get("session");
        if ( sess == null ) {
            log.debug("We can't find a session object, so returning void from session re-attachment");
            return;
        }


        for (int i = 0; i < session.length; i++) {
            String sessionKey = key+session[i];
            Object storedObject = sess.get(sessionKey);
            if (storedObject == null) {
                continue;
            }
            log.debug("Looking at key "+sessionKey);
            if (storedObject instanceof EurocarbObject) {
                log.debug("Re-attaching "+sessionKey+" to session");
                if (((EurocarbObject) storedObject).getId() != 0) {                
                    getEntityManager().update(storedObject);
                }

            }
            if (storedObject instanceof Collection) {
                try {
                    log.debug("Re-attaching "+sessionKey+" to session going through the collection");
                    for (EurocarbObject object : (Collection<EurocarbObject>) storedObject) {
                        if (object.getId() != 0) {
                            getEntityManager().update(object);
                        }
                    }
                } catch (ClassCastException e) {
                    log.debug("Collection in "+sessionKey+" does not store only EurocarbObject, skipping");
                }
            }            
        }
    }
    
}
