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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/
package org.eurocarbdb.action.mapper;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import com.opensymphony.webwork.RequestUtils;
import com.opensymphony.webwork.dispatcher.mapper.*;

import org.apache.log4j.Logger;

import static org.eurocarbdb.util.StringUtils.join;


/*  class UrlActionMapper  *//***************************************
*<p>
*   This class handles the translation from URL to action name and 
*   back. This implementation expects to find an action name in the
*   chunk of text following the last '/' in the passed URI. Specific
*   extensions (like *.action, *.do) are not required, this is to allow
*   URLs to remain as "clean" as possible. 
*</p>
*<p>
*   Eg. Given <tt>http://foo.bar.com/my/app/login</tt>, the derived action 
*   name would be <tt>login</tt>.
*</p>
*<p>
*   Webwork/Struts also makes it possible to call specific methods 
*   on an action (if not given, WW defaults to calling <tt>execute()</tt>).
*   The syntax for this is to append the method name along with a 
*   fullstop to the action name. 
*</p>
*<p>
*   Eg. given the url <tt>http://foo.bar.com/my/app/create_foo.add_bar</tt>,
*   the action name is <tt>create_foo</tt>, and the method name becomes 
*   <tt>addBar</tt>. Webwork will look for either an <tt>addBar()</tt> 
*   or <tt>doAddBar()</tt> method on the <tt>create_foo</tt> action
*   (this method should return a String result type). 
*</p>
*   @author mjh [glycoslave@gmail.com]
*/
public class UrlActionMapper implements ActionMapper 
{
    //~~~ FIELDS ~~~//

    /** inheritable log handle. */
    protected static final Logger log 
        = Logger.getLogger( UrlActionMapper.class.getName() );

    
    //~~~  METHODS  ~~~//

    /*  getMapping  *//**********************************************
    *
    *   Translates the URI encapsulated in the passed HTTP request 
    *   into a specific action name, encapsulated in the returned 
    *   ActionMapping instance.
    */
    public ActionMapping getMapping( HttpServletRequest request ) 
    {
        ActionMapping mapping = new ActionMapping();
        
        String uri = getUri( request );
        log.debug("uri is '" + uri + "'" );
        
        // int nextSlash = uri.indexOf('/', 1);
        int last_slash = uri.lastIndexOf('/');
        if ( last_slash == -1 ) 
        {
            log.warn("(no '/' in URI, returning null)");
            return null;
        }

        String action_name = uri.substring( last_slash + 1 );
        String namespace   = uri.substring( 1, last_slash );
        String method      = null;
        
        //Map params         = request.getParameterMap();
        
        //  check for (redundant) extensions in action name.
        if ( action_name.endsWith(".action") )
        {
            throw new RuntimeException( "deprecated URL syntax -- action names " 
                                      + "should not have a '.action' extension"
                                      );
        }
        
        int fullstop = action_name.indexOf('.');
        if ( fullstop != -1 )
        {
            method = action_name.substring( fullstop + 1 );
            action_name = action_name.substring( 0, fullstop );
        }
            
        if ( log.isDebugEnabled() )
            log.debug( "action=" 
                     + action_name 
                     + ", namespace=" 
                     + namespace 
                     + ", method="
                     + method
                     );
        
        mapping.setName( action_name );
        mapping.setNamespace( namespace );
        mapping.setMethod( method );
        
        return mapping;
    }


    /*  getUriFromActionMapping  *//*********************************
    *
    *   Performs the reverse translation of action name/mapping to 
    *   URI, that is, the opposite of the <code>getMapping</code> method.
    */
    public String getUriFromActionMapping( ActionMapping mapping ) 
    {
        StringBuffer uri = new StringBuffer();
        
        uri.append("/");

        //  add namespace
        if ( ! mapping.getNamespace().equals("") )
        {
            uri.append( mapping.getNamespace() );
            uri.append("/");
        }
        
        //  add action name
        uri.append( mapping.getName() );    
            
        //  add method, if any
        String method = mapping.getMethod();
        if ( method != null && method.length() > 0 )
        {
            uri.append( "." );
            uri.append( method );
        }
            
        //  add params
        Map params = mapping.getParams();
        String param_string = join( params, "=", ";" );
    
        if ( param_string.length() > 0 )
            uri.append( "?" + param_string );
    
        return uri.toString();
    }
    
    
    //  copied from webwork's DefaultActionMapper
    private String getUri( HttpServletRequest request ) 
    {
        // handle http dispatcher includes.
        String uri = (String) request.getAttribute("javax.servlet.include.servlet_path");
        
        if ( uri != null ) 
            return uri;

        uri = RequestUtils.getServletPath(request);
        if ( uri != null && ! uri.equals("") ) 
            return uri;
        
        uri = request.getRequestURI();
        
        return uri.substring( request.getContextPath().length() );
    }
    
}
