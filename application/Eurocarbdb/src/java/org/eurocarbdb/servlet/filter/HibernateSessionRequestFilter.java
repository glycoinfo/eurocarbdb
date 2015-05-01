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
package org.eurocarbdb.servlet.filter;

import java.io.IOException;

//  java servlet imports
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;

import org.apache.log4j.Logger;

import org.eurocarbdb.dataaccess.Eurocarb;


/*  class HibernateSessionRequestFilter  *//*************************
*<p>
*   Servlet filter class that wraps around every user request, 
*   opening and closing a hibernate session per request. 
*   See the {@link #doFilter } method for details.
*</p>
*<p>
*   Code blatantly taken from Hibernate tutorial on the 
*   "<a href="http://www.hibernate.org/43.html">Open Session in View</a>"
*   design pattern.
*</p>
*   @author <a href="http://hibernate.org">Hibernate.org</a>
*   @author mjh
*/
public class HibernateSessionRequestFilter implements Filter 
{
    /** Logging instance */
    private static Logger log = Logger.getLogger( 
        HibernateSessionRequestFilter.class );

    // ** Hibernate session factory. */
    //private SessionFactory sf;
    
    private static long requestsReceived = 0;
    

    /*  doFilter  *//************************************************
    *   
    *   Opens hibernate transaction before application does anything,
    *   and then closes the transaction after the view has been rendered
    *   but not yet sent to the client.
    */
    public void doFilter( ServletRequest request,
                          ServletResponse response,
                          FilterChain chain )
    throws IOException, ServletException
    {
        //  this top section of code executes before any application 
        //  logic has been run, assuming that this filter is at the
        //  top of the servlet filter chain, which it should be...
        
        requestsReceived++;
        
        //  log current user request info.
        if ( log.isDebugEnabled() ) 
        {
            for ( int i = 0; i < 5; i++ ) 
                System.err.println();
            
            StringBuilder sb = new StringBuilder();
            sb.append( 
                "request " 
                + requestsReceived
                + " from " 
                + request.getRemoteHost() 
                + "\n"
            );
            
            if ( request instanceof HttpServletRequest )
            {
                HttpServletRequest hreq = (HttpServletRequest) request;
                sb.append(
                    "http request: " 
                    + hreq.getRequestURI() 
                    + "\n"
                    + "query string: "
                    + hreq.getQueryString()
                );
            }
            
            log.debug( sb.toString() );
        }   
            
        //  start a hibernate transaction
        Eurocarb.getEntityManager().beginUnitOfWork();            

        try 
        {
            //  Call the next filter (continue request processing).
            //  == MAIN APPLICATION EXECUTES HERE ==
            chain.doFilter( request, response );
        }
        catch ( Throwable t )
        {
            log.warn("Caught exception in application, aborting current unit of work");
            Eurocarb.getEntityManager().abortUnitOfWork();
            throw new ServletException( t );
        }
        
        //  main application logic has been run, commit and cleanup
        //  as appropriate.
        Eurocarb.getEntityManager().endUnitOfWork();

    }


    public void init( FilterConfig filterConfig ) throws ServletException 
    {
        log.debug("Initialising transaction management");
    }

    
    
    public void destroy() { /* do nothing */ }

}


