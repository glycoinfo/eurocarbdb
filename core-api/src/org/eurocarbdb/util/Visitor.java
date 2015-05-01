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
*   Last commit: $Rev: 1231 $ by $Author: glycoslave $ on $Date:: 2009-06-19 #$  
*/
package org.eurocarbdb.util;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;


/**
*<p>
*   Implementation of the <em>Visitor pattern</em>, which uses reflection 
*   rather than a Visitable interface to perform the double-dispatch
*   that is necessary to traverse arbitrarily complex data structures (which
*   is the point of the Visitor pattern).
*</p>
*<p>
*   To use this class to traverse a data structure, add a version of the 
*   method <code>public void accept(XXX x)</code> for each class/interface 
*   XXX that you are interested in visiting. Traversal is initiated 
*   by calling {@link #visit visit(Object)} on the top-level element. Similarly, 
*   each accept method is free to visit other objects from itself as
*   required, and Visitor classes can of course have state and be sub-classed
*   in the usual manner.
*</p>
*<h3>Example</h3>
*
*<pre>
        class A {}
        class B extends A {}
        interface I extends SomeInterface {}   
 
        class MyVisitor extends Visitor
        {
            public void accept( A a ) 
            { 
                // do something with A...
            }
            
            public void accept( B b ) 
            { 
                // do something with B... 
            }
            
            public void accept( I i ) 
            { 
                // do something with I...
            }
            
            public void accept( Collection c )
            {
                for ( Object item : collection)
                    visit( item );
            }
            
            public static void main( String[] args )
            {
                Object complex_data_structure = ...
                
                Visitor v = new MyVisitor();
                v.visit( complex_data_structure );
            }
        }   
*</pre>
*<p>
*   Note that the call to <tt>visit</tt> follows the normal Java rules
*   for method selection - if an <tt>accept</tt> method for a given object's
*   class cannot be found, then an <tt>accept</tt> method for its superclass
*   will be used, or if there are no accept methods for any superclass, then
*   accept methods for the implemented interfaces of that object will match,
*   then their superinterfaces, etc.
*</p>
*<p>
*   Note also that <tt>accept</tt> methods MUST be <tt>public</tt>, or they 
*   will not get visited. The classes that declare accept methods however,
*   do not need to be public.
*</p>
*
*<h3>Performance</h3>
*<p>
*   Here are some performance metrics taken from the unit test of this class
*   comparing performance of this class versus a tradition Visitable Visitor
*   implementation:
*<pre>
*       timing 100 iteration(s):
*       reflection visitor took 2 msec
*       traditional visitor took 1 msec
*       
*       timing 10000 iteration(s):
*       reflection visitor took 25 msec
*       traditional visitor took 15 msec
*</pre>
*   So, it's approximately 2 times slower, but for all intents and purposes,
*   the performance difference is negligible compared to the work done in
*   accept(Xxx) methods, and the reflection-based Visitor (ie: this class)
*   has the tremendous benefit that every class that requires visiting
*   does not need to implement a Visitable interface nor some arbitrary
*   <code>accept( Visitor v ) { v.visit(this); }</code> method.
*</p>
*<p>
*   Finally, a single visitor can be used to traverse many data structures,
*   (provided any recorded state is reset as appropriate).
*</p>
*
*   @see <a href="http://en.wikipedia.org/wiki/Visitor_pattern">Visitor pattern</a>
*   @author mjh
*/
public abstract class Visitor 
{    
    /** Inheritable logging handle. */
    protected static final Logger log = Logger.getLogger( Visitor.class );
    
    /** Controls compile-time addition/removal of debugging calls */
    private static final boolean DEBUG = false;
    
     /** Lookup cache of visit method references for various classes.
    *   This is to avoid the high cost of using reflection for method discovery. */
    private final Map<Class<?>,Method> cache = new HashMap<Class<?>,Method>();
    
    
    /**
    *   This is the main method for dynamic dispatch to other 
    *   <tt>accept(XXX x)</tt> methods based on the class of <tt>x</tt>. 
    */
    public final void visit( Object object_to_visit )
    {
        if ( object_to_visit == null )
            return;
        
        Class<?> c = object_to_visit.getClass();
        
        //  lookup accept method in cache first, saves using reflection more 
        //  than what we have to. a method value of null means there's no
        //  suitable accept method to handle the given Object argument.       
        Method m =  null;
        if ( cache.containsKey( c ) )
        {
            m = cache.get( c ); 
        }
        else
        {
            //  find a suitable accept method. 
            //  returned method can be null, which means 'no method'.
            m = getVisitMethodForClass( c );    
            if ( DEBUG ) log.debug("caching " + m + " for " + c );
            synchronized ( cache ) 
            {
                cache.put( c, m );
            }
            
            //  allows us to call accept methods in non-public classes.
            if ( m != null )
                m.setAccessible( true );
        }
        
        if ( m == null )
        {
            if ( DEBUG ) log.debug("no matching accept methods for " + c + ", skipping" );
            return;
        }
            
        try
        {
            if ( DEBUG ) log.debug("invoking " + m );
            m.invoke( this, object_to_visit );    
        }
        
        //  this should only occur if subclass implementors are silly
        //  enough to make their accept methods private or package private
        //  and therefore inaccessible.
        catch ( IllegalAccessException ex )
        {
            log.warn(
                "Caught IllegalAccessException while invoking "
                + m.toString()
                + ". This most likely cause is that either the class in which "
                + "this method is declared, or the method itself, is not public." 
                , ex
            );  
            throw new RuntimeException( ex );
        }
        
        //  this exception is received if the method invoked throws an
        //  exception -- report it
        catch ( InvocationTargetException ex )
        {
            log.warn(
                "Caught InvocationTargetException while invoking "
                + m.toString()
                , ex
            );   
            throw new RuntimeException( ex );
        }
    }
    
    
    /** 
    *   Recurses through all superclasses & interfaces of passed Class 
    *   looking for a suitable accept( object of Class c ) method. 
    *   This method uses reflection and is, consequently, slow as shit. 
    *   Calls to it are minimised by caching the return value in  
    *   {@link #cache}. 
    *   @return a suitable accept method for objects of the given Class,
    *   or null if no suitable method could be found.
    */
    private final Method getVisitMethodForClass( Class c )
    {
        if ( DEBUG ) log.debug("looking for accept method for class " + c.getSimpleName() + ")" );
        
        Method m = null;
            
        try
        {
            m = this.getClass().getMethod( "accept", c );
            if ( DEBUG ) log.debug("found method: " + m );
            return m;
        }
        catch ( NoSuchMethodException ignore ) {}
        
        //  search for a visit method that handles superclasses of c, recursively
        if ( ! c.isInterface() )
        {
            Class s = c.getSuperclass();
            
            if ( s != null && s != Object.class ) 
            {
                if ( DEBUG ) log.debug("trying superclass " + s.getSimpleName() );
                m = getVisitMethodForClass( s );

                if ( m != null ) 
                    return m;
            }
        }

        //  search for a visit method that handles interfaces of c, recursively
        for ( Class s : c.getInterfaces() )
        {
            if ( DEBUG ) log.debug("trying interface " + s );
            m = getVisitMethodForClass( s );
            if ( m != null ) 
                return m;
        }
        
        try
        {
            m = this.getClass().getDeclaredMethod( "accept", Object.class );
            if ( DEBUG ) log.debug("found accept( Object ): " + m );
            return m;
        }
        catch ( NoSuchMethodException ignore ) {}
        
        return null;
    }
    

    // this isn't any faster than recursive method...
    /*
    private final Method getVisitMethodForClass( Class<?> c )
    {
        if ( DEBUG ) log.debug("looking for accept method for " + c.getSimpleName() );

        Method m;

        try
        {
            m = this.getClass().getMethod( "accept", c );    
            if ( DEBUG ) log.debug("found accept( " + m.getParameterTypes()[0].getSimpleName() + ")" );
            return m;
        }
        catch ( NoSuchMethodException ignore )  {}
        
        //  look for method: accept( superclass )
        for ( Class<?> s = c.getSuperclass(); s != Object.class; s = s.getSuperclass() )
        {
            if ( DEBUG ) log.debug("searching superclass " + s.getSimpleName() );
            try
            {
                m = this.getClass().getMethod( "accept", s );    
                if ( DEBUG ) log.debug("found accept( " + m.getParameterTypes()[0].getSimpleName() + ")" );
                return m;
            }
            catch ( NoSuchMethodException ignore )  {}
        }
        
        //  look for method: accept( interface )
        for ( Class<?> i : c.getInterfaces() )
        {
            if ( DEBUG ) log.debug("searching interface " + i.getSimpleName() );
            try
            {
                m = this.getClass().getMethod( "accept", i );    
                if ( DEBUG ) log.debug("found accept( " + m.getParameterTypes()[0].getSimpleName() + ")" );
                return m;
            }
            catch ( NoSuchMethodException ignore )  {}
        }
        
        //  finally, look for method: accept( Object )
        try
        {
            m = this.getClass().getMethod( "accept", Object.class );    
            if ( DEBUG ) log.debug("found method for Object.class" );
            return m;
        }
        catch ( NoSuchMethodException ignore )  {}
        
        return null;
    }
    */

    //  even with extra caching, this is slowest of all...
    /*
    private final Method getVisitMethodForClass2( Class<?> c )
    {
        if ( DEBUG ) log.debug("looking for accept method for " + c.getSimpleName() );

        if ( cache.containsKey( c ) )
            return cache.get( c );
        Method m;

        // try
        // {
        //     m = this.getClass().getDeclaredMethod( "accept", c );    
        //     if ( DEBUG ) log.debug("found accept( " + m.getParameterTypes()[0].getSimpleName() + ")" );
        //     cache( c, m );
        //     return m;
        // }
        // catch ( NoSuchMethodException ignore )  {}
        
        //  look for method: accept( superclass )
        for ( Class<?> s = c.getSuperclass(); s != Object.class; s = s.getSuperclass() )
        {
            if ( cache.containsKey( s ) )
                return cache.get( s );
            
            if ( DEBUG ) log.debug("searching superclass " + s.getSimpleName() );
            try
            {
                m = this.getClass().getDeclaredMethod( "accept", s );    
                if ( DEBUG ) log.debug("found accept( " + m.getParameterTypes()[0].getSimpleName() + ")" );
                cache( c, m );
                cache( s, m );
                return m;
            }
            catch ( NoSuchMethodException ignore )  {}
        }
        
        //  look for method: accept( interface )
        for ( Class<?> i : c.getInterfaces() )
        {
            if ( cache.containsKey( i ) )
                return cache.get( i );
            
            if ( DEBUG ) log.debug("searching interface " + i.getSimpleName() );
            try
            {
                m = this.getClass().getDeclaredMethod( "accept", i );    
                if ( DEBUG ) log.debug("found accept( " + m.getParameterTypes()[0].getSimpleName() + ")" );
                cache( c, m );
                cache( i, m );
                return m;
            }
            catch ( NoSuchMethodException ignore )  {}
        }
        
        //  finally, look for method: accept( Object )
        
        if ( cache.containsKey( Object.class ) )
            return cache.get( Object.class );
        
        try
        {
            m = this.getClass().getDeclaredMethod( "accept", Object.class );    
            if ( DEBUG ) log.debug("found method for Object.class" );
            cache( Object.class, m );
            return m;
        }
        catch ( NoSuchMethodException ignore ) 
        {
            cache( Object.class, null );
        }
        
        cache( c, null );
        
        return null;
    }
    */
    
    
} // end class Visitor

