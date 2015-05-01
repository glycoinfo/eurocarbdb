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
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/

package org.eurocarbdb.util;

import java.util.Map;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collection;

import java.io.PrintStream;

/** Miscellaneous helper functions */
public final class JavaUtils
{
    /**
    *
    *   @throws IllegalArgumentException
    *   if passed Object is null
    */
    public static final void checkNotNull( Object x )
    {
        if ( x == null )
            throw new IllegalArgumentException(
                "Argument cannot be null");
    }

    /**
    *
    *   @throws IllegalArgumentException
    *   if passed Object is null
    */
    public static final void checkNotNull( Object x, String msg )
    {
        if ( x == null )
            throw new IllegalArgumentException( msg );
    }
    
    
    /**
    *
    *   @throws IllegalArgumentException
    *   if passed String is zero-length
    */
    public static final void checkNotEmpty( String s )
    {
        checkNotNull( s );
        if ( s.length() == 0 )
            throw new IllegalArgumentException(
                "Passed String argument cannot be zero-length");
    }
    
    
    /**
    *
    *   @throws IllegalArgumentException
    *   if passed {@link Collection} is zero-length
    */
    public static final void checkNotEmpty( Collection c )
    {
        checkNotNull( c );
        if ( c.size() == 0 )
            throw new IllegalArgumentException(
                "Passed Collection cannot be zero-length");
    }
    
    
    public static final void checkNotEmpty( Object[] a )
    {
        checkNotNull( a );
        if ( a.length == 0 )
            throw new IllegalArgumentException(
                "Array argument cannot be zero-length");
    }
 
    
    public static final void checkPositive( int i )
    {
        if ( i <= 0 )
            throw new IllegalArgumentException(
                "Integer argument must be positive, got " + i );
    }
    
    
    /** 
    *   This is for debugging use only - prints a snapshot of the current 
    *   stack trace to the given {@link PrintStream}.
    */
    public static final void printStackTrace( PrintStream out )
    {
        try { throw new RuntimeException(); }
        catch ( RuntimeException dummy )
        {
            StackTraceElement[] stack = dummy.getStackTrace();
            out.println("CURRENT STACK SNAPSHOT (depth=" + stack.length + " frames)");

            for ( int i = 1; i < stack.length; i++ )
            {
                out.println(
                    " -> method "
                    + stack[i-1].getMethodName()
                    + " in "
                    + stack[i].getFileName()
                    + ", line "
                    + stack[i].getLineNumber()
                );   
            }
        }
    }
    
    
    /**
    *   Returns true if the passed {@link Class} corresponds to a primitive
    *   type <em>or</em> one of the Object wrapper class types (Integer.class, etc).
    *   This is needed because, to Java, <tt>Integer.class.isPrimitive()</tt> is <tt>false</tt>,
    *   whereas <tt>isReallyPrimitive( Integer.class )</tt> returns <tt>true</tt>
    */
    public static final <T> boolean isReallyPrimitive( Class<T> c )
    {
        return isPrimitiveWrapper( c ) || c.isPrimitive();   
    }
    
    
    /**
    *   Returns true if the passed {@link Class} corresponds to a primitive
    *   object wrapper class, eg: <tt>Integer.class</tt>, <tt>Long.class</tt>.
    */
    public static final <T> boolean isPrimitiveWrapper( Class<T> c )
    {
        return c == Integer.class   
            || c == Double.class 
            || c == Boolean.class
            || c == Long.class
            || c == Float.class   
            || c == Character.class   
            || c == Short.class   
            || c == Byte.class   
            || c == Void.class
        ;
    }
    
    
    /** 
    *   Populates the passed {@link Map} with the passed {@link Collection},
    *   by addition of elements from the Collection in pairwise fashion --
    *   key, value, key, value, etc. For example the {@link List}:
    *   <pre>
    *       [ "abc", "def", "ghi", "jkl"]
    *   </pre>
    *   would become the following {@link Map}:
    *   <pre>
    *       { "abc" => "def", 
    *         "ghi" => "jkl"  }
    *   </pre>
    *   Note that the passed Map does not necessarily have to be empty.
    *
    *   @throws IllegalArgumentException 
    *   if the size of the passed Collection is not even, 
    *   or if the passed Map is null
    *   @throws ClassCastException
    *   if elements of the passed Collection cannot be cast to the types
    *   specified by the passed Map.
    */
    @SuppressWarnings("unchecked")
    public static final <K,V> Map<K,V> toMap( Map<K,V> map, Collection<?> values )
    {
        if ( map == null )
            throw new IllegalArgumentException(
                "Passed Map cannot be null");
            
        if ( values.size() == 0 )
            return map;
        
        if ( (values.size() % 2) != 0 )
            throw new IllegalArgumentException(
                "Passed Collection cannot have an uneven number of elements");
            
        Iterator iter = values.iterator();
        
        for ( int i = 0; i < values.size(); i += 2 )
        {
            K key = (K) iter.next();
            V value = (V) iter.next();
            
            map.put( key, value );
        }
        
        return map;
    }
    

    /** @see #toMap  */
    public static final <K,V> Map<K,V> toMap( Map<K,V> map, Object... values )
    {
        return toMap( map, Arrays.asList( values ) );   
    }
    
    
    
    
} // end class
