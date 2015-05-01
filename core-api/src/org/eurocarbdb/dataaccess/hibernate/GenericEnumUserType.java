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

package org.eurocarbdb.dataaccess.hibernate;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.type.NullableType;
import org.hibernate.type.TypeFactory;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

/**
*   Hibernate {@link UserType} to support the use of 
*   <a href="http://java.sun.com/j2se/1.5.0/docs/guide/language/enums.html">Java 1.5 enums</a>.
*
*   A large part of the code for this class was copied from 
*   <a href="http://www.hibernate.org/272.html">http://www.hibernate.org/272.html</a>.
*
*<h4>Simple example mapping for a String-based Enum - inline &lt;type&gt; tag</h4>
*<pre>        
*   &lt;class ...&gt;
*       &lt;property name='suit'&gt;
*           &lt;column ... /&gt;    
*           &lt;type name="EnumUserType"&gt;
*               &lt;param name="enumClassName"&gt;com.company.project.Suit&lt;/param&gt;
*           &lt;/type&gt;
*       &lt;/property&gt;
*   &lt;/class&gt;
*</pre>
*
*<h4>Simple example mapping for a String-based Enum - using &lt;typedef&gt;</h4>
*<pre>
*   &lt;typedef name="suit" class='EnumUserType'&gt;
*       &lt;param name="enumClassName"&gt;com.company.project.Suit&lt;/param&gt;
*   &lt;/typedef&gt;
*    
*   &lt;class ...&gt;
*       &lt;property name='suit' type='suit'&gt;
*           &lt;column .../&gt;
*       &lt;/property&gt;
*    &lt;/class&gt;
*</pre>
*
*<h4>Example Mapping for a non-String-based Enum</h4>
*<p>  
*   If the mapping of the enum to the SQL column type is not String (for example,
*   you may want your enum class to map to a char or int column), then you need
*   a small amount of extra configuration.
*</p>
*<p>
*<ul>
*   <li>The enum type being represented by the certain user type must be set
*       by the 'enumClass' property.</li>
*   <li>The identifier representing the enum value is retrieved by the method named
*       by the '<tt>identifierMethod</tt>' property. That is, given an Enum object, 
*       this is the method that returns the corresponding SQL value of the given 
*       {@link Enum} object. If not specified, the enum's value is determined by 
*       the {@link Enum#name} method. Identifier type (Class) is automatically 
*       determined by the return-type of the identifierMethod.</li>
*   <li>Similarly, the value of the property '<tt>valueOfMethod</tt>' must provide 
*       the name of a method that returns the correct Enum object for a given identifier. 
*       That is, given an SQL value, this method must returning the corresponding 
*       Enum object. The default valueOfMethod method is {@link Enum#valueOf}.</li>
*</p> 
*<p>
*   Example of an enum type represented by an int value:
*<pre><code>
*       public enum SimpleNumber 
*       {
*           Unknown(-1), Zero(0), One(1), Two(2), Three(3);
*           int value;
*   
*           protected SimpleNumber(int value) 
*           {
*               this.value = value;
*           }
*
*           public int toInt() {  return value;  }
*
*           public static SimpleNumber fromInt( int value ) 
*           {
*               switch(value) 
*               {
*                   case 0: return Zero;
*                   case 1: return One;
*                   case 2: return Two;
*                   case 3: return Three;
*                   default: return Unknown;
*               }
*           }
*       }
*</code>
*</pre>
*</p>
*<p>
*The corresponding hibernate mapping config would look like this:
*<pre><code>
*   &lt;typedef name="SimpleNumber" class="GenericEnumUserType"&gt;
*       &lt;param name="enumClass"&gt;SimpleNumber&lt;/param&gt;
*       &lt;param name="identifierMethod"&gt;toInt&lt;/param&gt;
*       &lt;param name="valueOfMethod"&gt;fromInt&lt;/param&gt;
*   &lt;/typedef&gt;
*   &lt;class ...&gt;
*       ...
*       &lt;property name="number" column="number" type="SimpleNumber"/&gt;
*   &lt;/class&gt;
*</code></pre>
*</p>
*   @see http://www.hibernate.org/272.html
*   @see http://java.sun.com/j2se/1.5.0/docs/guide/language/enums.html
*   @author mjh
*/
public class GenericEnumUserType implements UserType, ParameterizedType 
{
    private static final String DEFAULT_IDENTIFIER_METHOD_NAME = "name";
    private static final String DEFAULT_VALUE_OF_METHOD_NAME = "valueOf";

    private Class<? extends Enum> enumClass;
    private Class<?> identifierType;
    private Method identifierMethod;
    private Method valueOfMethod;
    private NullableType type;
    private int[] sqlTypes;

    /**
    *   Called by hibernate with all propertiies given as params in 
    *   a given mapping. Valid values for this class are:
    *   enumClassName, identifierMethod, and valueOfMethod, as described
    *   in the class preamble.
    */
    public void setParameterValues(Properties parameters) 
    {
        String enumClassName = parameters.getProperty("enumClassName");
        try 
        {
            enumClass = Class.forName( enumClassName ).asSubclass( Enum.class );
        } 
        catch (ClassNotFoundException cfne) 
        {
            throw new HibernateException(
                "Enum class given by property 'enumClassName' not found", cfne );
        }

        String identifierMethodName 
            = parameters.getProperty(
                "identifierMethod", DEFAULT_IDENTIFIER_METHOD_NAME);

        try 
        {
            identifierMethod = enumClass.getMethod( identifierMethodName, new Class[0] );
            identifierType = identifierMethod.getReturnType();
        } 
        catch ( Exception e ) 
        {
            throw new HibernateException(
                "Failed to obtain identifier method named '"
                + identifierMethodName
                + "' in class '"
                + enumClass.getName()
                + "' given by property 'identifierMethod'"
                , e
            );
        }

        type = (NullableType) TypeFactory.basic( identifierType.getName() );

        if (type == null)
            throw new HibernateException(
                "Unsupported identifier type " + identifierType.getName());

        sqlTypes = new int[] { type.sqlType() };

        String valueOfMethodName 
            = parameters.getProperty( 
                "valueOfMethod", DEFAULT_VALUE_OF_METHOD_NAME);

        try 
        {
            valueOfMethod = enumClass.getMethod( valueOfMethodName, new Class[] { identifierType });
        } 
        catch (Exception e) 
        {
            throw new HibernateException(
                "Failed to obtain a method named '" 
                + valueOfMethodName
                + "', which accepts an argument of type '"
                + identifierType
                + "' in class '"
                + enumClass.getName()
                + "' for property 'valueOfMethod'"
                , e
            );
        }
    }

    public Class returnedClass() 
    {
        return enumClass;
    }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) 
    throws HibernateException, SQLException 
    {  
        Object identifier = type.get(rs, names[0]);
        if (rs.wasNull()) 
        {
            return null;
        }
        
        try 
        {
            return valueOfMethod.invoke(enumClass, new Object[] { identifier });
        } 
        catch (Exception e) 
        {
            throw new HibernateException(
                "Exception while invoking valueOf method '" 
                + valueOfMethod.getName() 
                + "' of enumeration class '" 
                + enumClass 
                + "'"
                , e
            );
        }
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index) 
    throws HibernateException, SQLException 
    {
        try 
        {
            if (value == null) 
            {
                st.setNull( index, type.sqlType() );
            } 
            else 
            {
                Object identifier = identifierMethod.invoke( value, new Object[0] );
                type.set( st, identifier, index );
            }
        } 
        catch (Exception e) 
        {
            throw new HibernateException(
                "Exception while invoking identifierMethod '" 
                + identifierMethod.getName() 
                + "' of enumeration class '" 
                + enumClass 
                + "'"
                , e
            );
        }
    }

    public int[] sqlTypes() 
    {
        return sqlTypes;
    }

    public Object assemble(Serializable cached, Object owner) 
    throws HibernateException 
    {
        return cached;
    }

    public Object deepCopy(Object value) throws HibernateException 
    {
        return value;
    }

    public Serializable disassemble(Object value) throws HibernateException 
    {
        return (Serializable) value;
    }

    public boolean equals(Object x, Object y) throws HibernateException 
    {
        return x == y;
    }

    public int hashCode(Object x) throws HibernateException 
    {
        return x.hashCode();
    }

    public boolean isMutable() 
    {
        return false;
    }

    public Object replace(Object original, Object target, Object owner) 
    throws HibernateException 
    {
        return original;
    }
    
}

