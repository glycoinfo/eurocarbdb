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
package org.eurocarbdb.action;

//  stdlib imports
import java.util.Map;

//  3rd party imports 
import org.apache.log4j.Logger;
import com.opensymphony.webwork.interceptor.SessionAware;

//  eurocarb imports
import static org.eurocarbdb.util.JavaUtils.*;
import static org.eurocarbdb.util.StringUtils.join;


/**
*<p>
*   Interface for actions that implicitly work with or require
*   a (logged in) {@link User} in order to work.
*</p>
*<p>
*   This implementation makes use of a session {@link Map} whose
*   lifespan is expected to be equivalent to the lifespan of 
*   the client application. For a web application, this session 
*   hash would be the HttpSession Map that is provided by the Java
*   Servlet API. 
*</p>
*   author: mjh [glycoslave@gmail.com]
*/
public abstract class AbstractSessionAwareAction 
extends EurocarbAction implements SessionAware 
{
    private static final Logger log 
        = Logger.getLogger( AbstractSessionAwareAction.class );

    private Map<String,Object> session = null;

    /** Returns session hash. */
    protected Map<String,Object> getSession()
    {
        assert session != null;
        return session;
    }
    
    /** 
    *   Sets the {@link Map} that will be used as the session hash. 
    */
    public void setSession( Map the_session )
    {
        checkNotNull( the_session );
        if ( log.isDebugEnabled() )
            log.debug( "setting session hash -- current values:\n"
                     + ((the_session.size() > 0) 
                     ? join( the_session, ": ", "\n")
                     : "(empty)" )
                     );
            
        this.session = the_session;
    }
    
    /**
    *   Retrieves an object stored in the session by a given
    *   key, returning null if no object is associated with that key.
    */
    protected Object retrieveFromSession( String key )
    {
        checkNotNull( key );
        checkNotEmpty( key );
        
        if ( getSession().containsKey( key ) )
        {
            if ( log.isDebugEnabled() )
                log.debug("retrieving '" + key + "' from session");
            
            Object obj = getSession().get( key );   
            return obj;
        }
        else 
        {
            if ( log.isDebugEnabled() )
                log.debug( "object identified by '" 
                         + key 
                         + "' not in session, returning null..."
                         );
            return null;
        }
    }
    
    
    /**
    *   Stores the given object in the current session, identified
    *   by the given key. Objects that already exist in the session
    *   with the same key are simply replaced with the new value.
    */
    protected void storeInSession( String key, Object value )
    {
        if ( getSession().containsKey( key ) )
        {
            if ( log.isDebugEnabled() )
                log.debug( "replacing object identified by '" 
                         + key 
                         + "' in session"
                         );
            
            getSession().put( key, value );   
        }
        else 
        {
            if ( log.isDebugEnabled() )
                log.debug("storing object '" + key + "' in session");
            
            getSession().put( key, value );   
        }
    }
    
    
    /** 
    *   Retrieves an object stored in the session by class (name).
    *   It is expected that the object being retrieved was put there
    *   by the matching method {@link storeInSession(Object)}.
    */
    protected <T> T retrieveFromSession( Class<T> c )
    {
        String name = c.getName();
        return (T) retrieveFromSession( name );
    }
    
    
    /** 
    *   This method is effectively a wrapper around the 
    *   {@link storeInSession(String,Object)} method which uses the
    *   passed Object's class name as the string key to store in the 
    *   session, with the exception that this method does not accept
    *   null as a value.
    */
    protected void storeInSession( Object value )
    {
        checkNotNull( value );
        
        String name = value.getClass().getName();
        storeInSession( name, value );
    }
    
    
    protected Object removeFromSession( String key )
    {
        checkNotNull( key );
        checkNotEmpty( key );
        checkNotNull( session );
        
        if ( session.containsKey( key ) )
        {
            if ( log.isDebugEnabled() )
                log.debug("removing '" + key + "' from session");
        
            return session.remove( key );
        }
        else
        {
            log.warn( "Attempt to remove '" 
                    + key 
                    + "' from session failed because this key does not "
                    + "exist in the session -- valid session keys are: "
                    + join(", ", session.keySet() )
                    );   
            return null;
        }
    }
    
        
} // end class
