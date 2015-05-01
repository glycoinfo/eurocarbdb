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
*   Last commit: $Rev: 1313 $ by $Author: glycoslave $ on $Date:: 2009-06-29 #$  
*/

package org.eurocarbdb.dataaccess;


//  stdlib imports
import java.lang.reflect.Method;
import java.util.Date;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.util.StringUtils;
import org.eurocarbdb.util.XmlSerialiser;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.Taxonomy;
import org.eurocarbdb.dataaccess.core.Evidence;
import org.eurocarbdb.dataaccess.core.Reference;
import org.eurocarbdb.dataaccess.exception.EurocarbException;

//  static imports
import static org.eurocarbdb.util.Version.date2version;


/**
*   Provides a default implementation of the {@link EurocarbObject} 
*   interface, which is supported by every Eurocarb data access 
*   object (DAO). Key methods that may need to be overridden in 
*   sub-classes are:
*<ol>
*   <li>{@link #getIdentifierClass()} and {@link #getId()} -- various 
*       UI and reflection-based generic strategies rely on these methods.
*       Any class that is the base class for other classes (eg: such as
*       {@link Evidence} and {@link Reference}) will *have* to override
*       these methods if the subclasses are potentially 
*       {@link EntityManager#lookup looked up} by their superclass + 
*       superclass ID.<br/> 
*       It's never a bad idea to override {@link #getId()}
*       to point at your class's main ID getter as it saves a reflection-
*       based lookup, and is completely dependent on the convention for
*       there to be a getYourClassNameId() method.
*   </li>
*   <li>{@link #validate()} -- this is called by {@link EntityManager}</li>s
*       during any persistence operation. The default implementation of this
*       method does nothing, but subclasses should override it to check that
*       essential properties and associations have been set.
*</ol>
*
*   @author mjh
*/
public class BasicEurocarbObject implements EurocarbObject
{
    static Logger log = Logger.getLogger( BasicEurocarbObject.class );
    
    
    /*  getCanonicalId  *//******************************************
    *
    *   This default implementation returns 
    *   <code>{@link getType()} + "_" + {@link getId()}</code>. 
    */
    public String getCanonicalId()
    {
        return getType() + "_" + getId();
    }

    
    /**
    *   {@inheritDoc}
    *
    *   Currently a work in progress.
    *
    *   @see Version
    */
    public String getVersion()
    {
        if ( this instanceof Contributed )
        {
            Date d = ((Contributed) this).getDateEntered();
            return date2version( d );
        }
        
        return "";
    }
    
    
    /*  getId  *//***************************************************
    *<p>
    *   {@inheritDoc}
    *</p>
    *<p>
    *   Default implementation attempts to determine object id by
    *   reflection based on the class' name according to the basic
    *   convention that canonic id accessors are usually named 
    *   <tt>'get' + <class-name> + 'Id'</tt>. This is sufficient
    *   for most basic data objects.
    *</p>
    */
    public int getId()
    {
        String method_name = "get" + this.getClass().getSimpleName() + "Id";
        Throwable problem  = null;
        try
        {
            Method method = this.getClass().getMethod( method_name );
            if ( method == null ) throw new NoSuchMethodException();
            
            Object raw_id = method.invoke( this );
            return (Integer) raw_id;
        }
        catch ( NoSuchMethodException no_method ) 
        {
            log.fatal( "Couldn't locate a method with name '" 
                        + method_name
                        + "'"
                        );
                
            problem = no_method;
        }
        catch ( SecurityException security_crap ) 
        {
            log.fatal( "Security exception raised while attempting to accessing "
                        + "method with name '" 
                        + method_name
                        + "'"
                        );
                
            problem = security_crap;
        }
        catch ( Exception some_other_invocation_problem )
        {
            log.fatal( "Exception raised while invoking method '" 
                        + method_name
                        + "'"
                        );
                
            problem = some_other_invocation_problem;
        }
        
        throw new RuntimeException( problem );
    }
    
    
    /*  getType  *//*************************************************
    *
    *   Returns this object's <em>type</em>. For example, "reference"
    *   for a Reference object, "biological_context" for a BiologicalContext 
    *   object.
    *
    *   @see #getCanonicalId()
    *   @see #getIdentifierClass()
    *   @see StringUtils#toUnderscoreCase(String)
    */
    public String getType()
    {
        return StringUtils.toUnderscoreCase( 
            this.getIdentifierClass().getSimpleName() );
    }


    /** 
    *<p>
    *   {@inheritDoc}
    *</p>
    *<p>
    *   The default implementation returns the current class.
    *</p>
    */
    @SuppressWarnings("unchecked")
    public <T extends EurocarbObject> Class<T> getIdentifierClass()
    {
        return (Class<T>) this.getClass();
    }
    
    
    /*
    /-** 
    *   Default implementation of object equality comparison for 
    *   Eurocarb objects -- <em>sub-classes should provide their own 
    *   implementation of this method as much as possible</em>. This default
    *   implementation uses the method {@link #getCanonicalId}
    *   to compare equality.
    *-/
    public boolean equals2( Object other )
    {
        if ( other == this )
            return true;
        
        if ( other == null ) 
            return false;   
        
        if ( ! (other instanceof EurocarbObject) )
            return false;
        
        EurocarbObject that = (EurocarbObject) other;
        if ( this.getCanonicalId() != that.getCanonicalId() )
            return false;
        
        return true;
    }
    
    public boolean equals( Object other )
    {
        return EqualsBuilder.reflectionEquals( this, other );      
    }
    
    
    /-** 
    *   Default implementation of object hashCode for 
    *   Eurocarb objects -- <em>sub-classes should provide their own 
    *   implementation of this method as necessary</em>. This default
    *   implementation uses the method {@link #getCanonicalId}
    *   to compute hashCode.
    *-/
    public int hashCode()
    {
        return this.getCanonicalId().hashCode();
    }
    */
    
    public String toString()
    {
        return "[" 
            + getIdentifierClass().getSimpleName() 
            + "=" 
            + getId() 
            + "]";
    }
    
    
    /** 
    *   {@inheritDoc}
    *
    *   The default implementation does nothing. 
    */
    public void validate() throws EurocarbException
    {
        /*
        log.warn(
            "Validate method for " 
            + this.getClass() 
            + " not yet implemented -- write one!" );
        */
        
        //  does nothing by default
    }


} // end class
