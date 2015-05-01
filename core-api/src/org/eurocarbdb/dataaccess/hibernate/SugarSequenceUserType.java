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
*   Last commit: $Rev: 1279 $ by $Author: glycoslave $ on $Date:: 2009-06-27 #$  
*/

package org.eurocarbdb.dataaccess.hibernate;

import java.util.Properties;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
//import org.hibernate.usertype.ParameterizedType;

import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.sugar.SequenceFormat;

/**
*   This class implements a {@link SugarSequence} 
*   <a href="http://www.hibernate.org/hib_docs/reference/en/html/mapping-types.html#mapping-types-custom"> 
*   Hibernate custom type</a>. It basically handles the interconversion of a JDBC SQL string 
*   to and from a SugarSequence object.
*
*   @see http://www.hibernate.org/hib_docs/reference/en/html/mapping.html#mapping-types-custom
*   @see SugarSequence
*   @see UserType
*   @author mjh
*/
public final class SugarSequenceUserType implements UserType
{
/* 
    public void setParameterValues( Properties parameters ) 
    {
    }
*/   

    /** The class type this class converts stuff to. */
    public Class returnedClass() 
    {
        return SugarSequence.class;
    }

    
    /**
    *   Handles translation of JDBC {@link ResultSet} to 
    *   a returned {@link SugarSequence} object, returning null
    *   if the result set {@link ResultSet#wasNull was null}.
    */
    public Object nullSafeGet( ResultSet resultSet, 
                               String[] names, 
                               Object owner  ) 
    throws HibernateException, SQLException 
    {  
        String seq = resultSet.getString( names[0] );
        if ( resultSet.wasNull() ) 
            return null;
        
        return new SugarSequence( seq );
    }

    
    /**
    *   Handles translation of {@link SugarSequence} object data
    *   to a JDBC {@link PreparedStatement}.
    */
    public void nullSafeSet( PreparedStatement st, 
                             Object value, 
                             int index  ) 
    throws HibernateException, SQLException 
    {
        if ( value == null )
        {
            st.setNull( index, Hibernate.STRING.sqlType() );    
        }
        else 
        {
            assert value instanceof SugarSequence;
            SugarSequence sseq = (SugarSequence) value;
            st.setString( index, sseq.toString( SequenceFormat.Glycoct ) );
        }
    }

    
    /** 
    *   Array of SQL types required by this user type. This 
    *   used for instance, for automatic DDL generation. 
    */
    public int[] sqlTypes() 
    {
        return new int[] { Hibernate.STRING.sqlType() };
    }

    
    /** 
    *   {@link SugarSequence} is immutable, so directly returns the 
    *   cached object argument. 
    */
    public Object assemble( Serializable cached, Object owner ) 
    throws HibernateException 
    {
        return cached;
    }

    
    /** 
    *   Makes a deep copy of the passed value. {@link SugarSequence} 
    *   is immutable, so this method only returns the passed value. 
    */
    public Object deepCopy( Object value ) throws HibernateException 
    {
        return value;
    }

    
    /** 
    *   {@link SugarSequence} is immutable and serialisable, so 
    *   directly returns the passed object argument. 
    */
    public Serializable disassemble( Object value ) throws HibernateException 
    {
        return (Serializable) value;
    }

    
    public boolean equals( Object x, Object y ) throws HibernateException 
    {
        if ( x == y ) return true;
        if ( x == null || y == null ) return false;
        return x.equals( y );
    }
    

    public int hashCode( Object x ) throws HibernateException 
    {
        return x.hashCode();
    }
    

    /** 
    *   Always returns false, since {@link SugarSequence} is 
    *   an immutable class. 
    */
    public boolean isMutable() 
    {
        return false;
    }

    
    /** 
    *   Merges object state; we just return the 'original' argument 
    *   as SugarSequence is immutable. 
    */
    public Object replace( Object original, Object target, Object owner ) 
    throws HibernateException 
    {
        return original;
    }

} // end class


