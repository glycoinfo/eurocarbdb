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
*   Last commit: $Rev: 1891 $ by $Author: srikalyansswayam $ on $Date:: 2010-03-11 #$  
*/

package org.eurocarbdb.dataaccess;

//  stdlib imports
import java.util.Map;
import java.net.URLEncoder;
// import javax.servlet.http.HttpSessionBindingListener;
// import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpServletRequest;

//  3rd party imports 
import org.apache.log4j.Logger;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.opensymphony.webwork.ServletActionContext;

//  eurocarb imports
import org.eurocarbdb.action.RequiresLogin;
import org.eurocarbdb.action.EditingAction;
import org.eurocarbdb.action.RequiresAdminLogin;
import org.eurocarbdb.dataaccess.core.Contributor;

import static org.eurocarbdb.util.JavaUtils.*;

/**
*<p>
*   Implements basic user ({@link Contributor}) authentication. 
*   The principle mechanism of which is the storage of the current 
*   {@link Contributor}'s contributor_id value in the current 
*   {@link ActionContext#getSession session hash}.
*</p>
*<p>
*   Note that this interceptor must be included in the interceptor
*   stack for all actions that are for Eurocarb. Read the info
*   for the {@link #intercept} method for more specific info on how this 
*   {@link Interceptor} works.
*</p>
*
*   @author mjh
*/
public class AuthenticationInterceptor implements Interceptor
{
    /** Logging handle. */
    private static final Logger log 
        = Logger.getLogger( AuthenticationInterceptor.class );
    
    /** Called implicitly by webwork when class is first used */
    public void init() 
    {
        if ( log.isDebugEnabled() )
            log.debug( this.getClass().getName() + " loaded" );
    }

    
    /** Called implicitly by webwork on shutdown */
    public void destroy() 
    {
    }
    
    
    /** 
    *   Called implicitly by webwork for every request/action
    *   that is configured to be subject to this interceptor.
    *   Works by:
    *<ol>
    *<li>retrieving webwork session</li>
    *<li>checks for presence of 'contributor_id' in session</li>
    *<li>looks up & verifies existence of a Contributor matching this id</li>
    *<li>sets Eurocarb.currentContributor with this Contributor</li>
    *<li>short circuits currently running Action if it implements RequiresLogin
    *    interface & Contributor is null. Does this by returning the "login"
    *    global result</li>
    *</ol>
    */
    public String intercept( ActionInvocation ai )
    throws Exception
    {
        HttpServletRequest req = ServletActionContext.getRequest();
        
        if ( req == null )
        {
            log.info("Running EuroCarbDB outside of session");
//            throw new RuntimeException(
//                "Eurocarb can't be run outside of a Servlet Context (yet)!!!");   
        }
                
        Map session = ai.getInvocationContext().getSession();
        
        int contrib_id = 0;
        
        if ( session != null && session.containsKey("contributor_id") )
        {
            contrib_id = (Integer) session.get("contributor_id");
            if ( log.isTraceEnabled() )
                log.trace( "session contributor id = " + contrib_id );
        }
        else
        {
            if ( log.isTraceEnabled() )
                log.trace( "session contributor id = 0 "
                         + "(ie: user is NOT logged in at the moment)");
        }

        //  need to reset Eurocarb.currentContributor for every HTTP req
        //Eurocarb.currentContributor = null;
        Eurocarb.currentContributor.set( null );
        Eurocarb.currentContributorId.set( contrib_id );
        
        //  lookup Contributor, redirect to error page if invalid
        Contributor c = Eurocarb.getCurrentContributor();
        
        //  if contrib_id > 0 and c == null then user probably has an
        //  old/forged session, and we should regard them as being 
        //  effectively not logged in.
        if ( contrib_id > 0 && c != null )
        {
            log.info( "user is logged in as Contributor '" 
                + c.getContributorName() 
                + "' (contributor_id="
                + contrib_id
                + ")"
            );
        }
        else
        {
            log.info( "user is GUEST (ie: not logged in, contributor_id=0");
        }
        
        //  if current Action requires login and user is not logged-in
        //  then redirect to login action.
        Action action = (Action) ai.getAction();
        
        if ( req != null && (action instanceof RequiresLogin) || (action instanceof RequiresAdminLogin) ) 
        {
            System.out.println("this is check point 1");
            if ( contrib_id == 0 || c == null )
            {
                log.info( "*** User is not logged in, shortcircuiting "
                        + "current Action && redirecting to login page ***"
                        );

                String base_url    = req.getRequestURL().toString();
                String params      = req.getQueryString();
                
                
                //String current_url = URLEncoder.encode( base_url + "?" + params, "UTF-8" );
                String current_url = (params != null) 
                                   ? base_url + "?" + params
                                   : base_url;
                
                session.put("redirected_from", current_url );
                System.out.println("returning login ");
                return "login";
            }
        
            if( (action instanceof RequiresAdminLogin) && !c.getIsAdmin() ) 
            {
                log.info("*** Logged user is not an administrator ***" );
                System.out.println("returning requires_admin");
                return "requires_admin";    
            }
        }
                   
        //  now run action as per usual
        String result_code = ai.invoke();
        
        //~~~  any code from here on executes *after* the view has been rendered ~~~        
        
        return result_code;
    }
    
    // /** 
    // *   Subclass of {@link Eurocarb.Client} that listens for when it
    // *   is associated to a {@link HttpSession}, or removed from a 
    // *   {@link HttpSession} manually or when the Session itself expires 
    // *   naturally by timing out.
    // *   @author mjh
    // */
    // static class HttpClient extends Eurocarb.Client
    // implements HttpSessionBindingListener
    // {
        // public HttpClient( String ip, String id ) {  super( ip, id );  }
        // 
        // /** Called when placed into a {@link HttpSession}. */
        // public void valueBound( HttpSessionBindingEvent event )
        // {
            // /*  do nothing */
        // }
        // 
        // /** 
        // *   Called when taken out of a {@link HttpSession} or the
        // *   session expires of its own accord. 
        // */
        // public void valueUnbound( HttpSessionBindingEvent event ) 
        // {
            // String session_id = this.getSessionId();
// 
            // log.info( "Removing " 
                    // + client 
                    // + " from the set of active Eurocarb users..."
                    // );
            // 
            // Eurocarb.Client client = Eurocarb.activeClients.remove( session_id );
            // assert client == this;
            // 
            // return;
        // }
        // 
    // } // end class HttpClient
    
} // end class AuthenticationInterceptor
