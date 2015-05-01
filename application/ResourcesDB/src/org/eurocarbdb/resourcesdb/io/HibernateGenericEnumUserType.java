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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
package org.eurocarbdb.resourcesdb.io;

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
*   Code copied directly (with adjustment of class name) from <a href="http://www.hibernate.org/272.html">http://www.hibernate.org/272.html</a>.
*
*<h4>Example Mapping - inline &lt;type&gt; tag</h4>
*<pre>        
*    &lt;class ...&gt;
*      &lt;property name='suit'&gt;
*        &lt;type name="EnumUserType"&gt;
*          &lt;param name="enumClassName"&gt;com.company.project.Suit&lt;/param&gt;
*        &lt;/type&gt;
*      &lt;/property&gt;
*    &lt;/class&gt;
*</pre>
*
*<h4>Example Mapping - using &lt;typedef&gt;</h4>
*<pre>
*    &lt;typedef name="suit" class='EnumUserType'&gt;
*      &lt;param name="enumClassName"&gt;com.company.project.Suit&lt;/param&gt;
*    &lt;/typedef&gt;
*    
*    &lt;class ...&gt;
*      &lt;property name='suit' type='suit'/&gt;
*    &lt;/class&gt;
*</pre>
*  
*/
public class HibernateGenericEnumUserType implements UserType, ParameterizedType {

    private static final String DEFAULT_IDENTIFIER_METHOD_NAME = "name";
    private static final String DEFAULT_VALUE_OF_METHOD_NAME = "valueOf";

    @SuppressWarnings("unchecked")
    private Class<? extends Enum> enumClass;
    private Class<?> identifierType;
    private Method identifierMethod;
    private Method valueOfMethod;
    private NullableType type;
    private int[] sqlTypes;

    public void setParameterValues(Properties parameters) {
        String enumClassName = parameters.getProperty("enumClassName");
        try {
            enumClass = Class.forName( enumClassName ).asSubclass( Enum.class );
        } catch (ClassNotFoundException cnfe) {
            throw new HibernateException("Enum class not found", cnfe);
        }

        String identifierMethodName = parameters.getProperty("identifierMethod", DEFAULT_IDENTIFIER_METHOD_NAME);

        try {
            identifierMethod = enumClass.getMethod( identifierMethodName, new Class[0] );
            identifierType = identifierMethod.getReturnType();
        } catch (Exception e) {
            throw new HibernateException("Failed to obtain identifier method", e);
        }

        type = (NullableType) TypeFactory.basic( identifierType.getName() );

        if (type == null) {
            throw new HibernateException("Unsupported identifier type " + identifierType.getName());
        }

        sqlTypes = new int[] {type.sqlType()};

        String valueOfMethodName = parameters.getProperty("valueOfMethod", DEFAULT_VALUE_OF_METHOD_NAME);

        try {
            valueOfMethod = enumClass.getMethod( valueOfMethodName, new Class[] { identifierType });
        } catch (Exception e) {
            throw new HibernateException("Failed to obtain valueOf method", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Class returnedClass() {
        return enumClass;
    }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {  
        Object identifier = type.get(rs, names[0]);
        if (rs.wasNull()) {
            return null;
        }
        
        try {
            return valueOfMethod.invoke(enumClass, new Object[] { identifier });
        } catch (Exception e) {
            throw new HibernateException("Exception while invoking valueOf method '" + valueOfMethod.getName() + "' of enumeration class '" + enumClass + "'", e);
        }
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        try {
            if (value == null) {
                st.setNull(index, type.sqlType());
            } else {
                Object identifier = identifierMethod.invoke(value, new Object[0]);
                type.set(st, identifier, index);
            }
        } 
        catch (Exception e) {
            throw new HibernateException("Exception while invoking identifierMethod '" + identifierMethod.getName() + "' of enumeration class '" + enumClass + "'", e);
        }
    }

    public int[] sqlTypes() {
        return sqlTypes;
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public boolean isMutable() {
        return false;
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
