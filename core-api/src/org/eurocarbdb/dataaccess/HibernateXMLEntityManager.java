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
*   Last commit: $Rev: 1236 $ by $Author: hirenj $ on $Date:: 2009-06-20 #$  
*/
/**
*   $Id: HibernateXMLEntityManager.java 1236 2009-06-19 15:32:49Z hirenj $
*   Last changed $Author: hirenj $
*   EUROCarbDB Project
*/

package org.eurocarbdb.dataaccess;

//  stdlib imports
import java.util.Set;
import java.util.List;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.EntityMode;

import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

//  eurocarb imports
// import org.eurocarbdb.dataaccess.hibernate.HibernateUtil;


/**
*   Imports & exports data as XML <strong>experimental</strong>. 
*
*   @author                  mjh
*   @version                 $Rev: 1236 $
*/
public class HibernateXMLEntityManager extends HibernateEntityManager 
{
    private Element lastObject = null;
    
    public Element getXmlElement() {  return lastObject;  }
    
    /*  @see EntityManager#createNew(Class)  */
    public <T> T createNew( Class<T> c ) 
    {
        return _instance_of( c );
    }

    
    /*  @see EntityManager#lookup(Class,Serializable)  */
    @SuppressWarnings("unchecked") 
    // ^^^ unavoidable, due to lack of genericity in Hibernate lib
    public <T> T lookup( Class<T> c, int object_id ) 
    {
        lastObject = (Element) getXmlSession().get( c, object_id );
        
        return null;
    }

    
    /*  @see EntityManager#getQuery(String)  */
    public Query getQuery( String name_of_query )
    {
        return getXmlSession().getNamedQuery( name_of_query );
    }


    /*  @see EntityManager#lookup(T,Serializable)  */
    public <T> void lookup( T destination_object, int object_id ) 
    {
        getXmlSession().load( destination_object, object_id );
    }
       
    
    public <T> void revert( T entity )
    {
        
    }
    
    /*  @see EntityManager#remove(T)  */
    public <T> void remove( T entity )
    {
        getXmlSession().delete( entity );
    }
    
    
    /*  @see EntityManager#removeAll  */
    public <T> void removeAll( Set<? extends T> entities )
    {
    }
    
    
    /*  @see EntityManager#store(T)  */
    public <T> void store( T entity ) 
    {
        getXmlSession().save( entity );
    }

    
    /*  @see EntityManager#update(T)  */
    public <T> void update( T entity ) 
    {
        getXmlSession().update( entity );
    }


    public <T> void storeAll( Set<? extends T> entities )
    {
    }

    /*  instantiates an instance of passed class; absorbs exceptions.  */
    private static final <T> T _instance_of( Class<T> c ) 
    {
        T entity = null;
        try 
        {
            entity = c.newInstance();    
        } 
        catch ( InstantiationException e ) 
        {
            e.printStackTrace();
        } 
        catch ( IllegalAccessException e ) 
        {
            e.printStackTrace();
        }
        
        assert entity != null;
        
        return entity;
    }
    

    public <T> void refresh( T entity ) 
    {
        getXmlSession().refresh( entity );
    }
    
    
    public final Session getXmlSession()
    {
        Session default_session = getHibernateSession();
        return default_session.getSession( EntityMode.DOM4J ); 
    }
    

} // end class
