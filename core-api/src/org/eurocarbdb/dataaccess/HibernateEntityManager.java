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
*   Last commit: $Rev: 1552 $ by $Author: glycoslave $ on $Date:: 2009-07-20 #$  
*/
/**
*   $Id: HibernateEntityManager.java 1552 2009-07-19 18:36:56Z glycoslave $
*   Last changed $Author: glycoslave $
*   EUROCarbDB Project
*/

package org.eurocarbdb.dataaccess;

//  stdlib imports
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Hibernate;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.Criteria;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.metadata.ClassMetadata; 
import org.hibernate.hql.QueryTranslator;
import org.hibernate.hql.QueryTranslatorFactory;
import org.hibernate.engine.SessionFactoryImplementor;


//  eurocarb imports
// import org.eurocarbdb.dataaccess.hibernate.HibernateUtil;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.exception.EurocarbException;

import static java.util.Collections.emptyMap;
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.util.JavaUtils.printStackTrace;


/**
*   Class to handle the management of entities from the database using
*   the Hibernate API. Includes instantiation, population and 
*   deserialisation from state.
*
*   @author                  mjh
*   @author                  hirenj
*   @version                 $Rev: 1552 $
*/
public class HibernateEntityManager implements EntityManager 
{
    /** Logging handle. */
    static final Logger log = Logger.getLogger( HibernateEntityManager.class );
    
    /** Hibernate SessionFactory singleton, created upon request, 
    *   in {@link #init()}. */
    private static SessionFactory sessionFactory;

    /** Has a unit of work been started yet? ie: are we inside of a 
    *   {@link Transaction}? Hibernate gets very upset if we aren't,
    *   so it's an error to do anything without one.
    */
    private boolean uowStarted = false;
    
    /** Default JNDI config file for Eurocarb connection info */
    public static final String Default_Jndi_Config_File = "hibernate.jndi.cfg.xml";
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
    *   Init hibernate {@link SessionFactory}, this takes a while, but only 
    *   needs to be done once per application instance.
    */
    public static void init() 
    {
        if ( sessionFactory != null )
            throw new RuntimeException(
                "A non-null SessionFactory already exists");
        
        log.info("initialising hibernate...");
        boolean use_jndi = Boolean.valueOf( Eurocarb.getProperty("ecdb.use_jndi") );
        
        //  build SessionFactory
        if ( use_jndi )
        {
            log.info("using JNDI to obtain connection params");
            initFromJndi();
        }
        else if ( Eurocarb.getProperty("ecdb.db.username") != null &&
                  Eurocarb.getProperty("ecdb.db.password") != null &&
                  Eurocarb.getProperty("ecdb.db.name") != null &&
                  Eurocarb.getProperty("ecdb.db.hostname") != null
                )
        {
            log.info("Using application configuration for connection params (ecdb.db.* properties)");
            sessionFactory = new Configuration()
                            .configure()  // use default 'hibernate.cfg.xml'
                            .setProperty("hibernate.connection.username",Eurocarb.getProperty("ecdb.db.username"))
                            .setProperty("hibernate.connection.password",Eurocarb.getProperty("ecdb.db.password"))
                            .setProperty("hibernate.connection.url","jdbc:postgresql://"+Eurocarb.getProperty("ecdb.db.hostname")+"/"+Eurocarb.getProperty("ecdb.db.name"))
                            .buildSessionFactory();
        }
        else
        {
          log.info("using hibernate.properties for connection params");
          sessionFactory = new Configuration()
                          .configure()            // use default 'hibernate.cfg.xml'
                          .buildSessionFactory();          
        }
        //  check new SessionFactory validity
        if ( sessionFactory == null )
            throw new RuntimeException(
                "freshly-created sessionFactory was null");                            
                        
        if ( log.isDebugEnabled() )
        {
            Map<String,ClassMetadata> hash 
                = (Map<String,ClassMetadata>) sessionFactory.getAllClassMetadata();
                
            log.debug("SessionFactory created with " + hash.size() + " entities");
        }
    }
    
    
    static void initFromJndi()
    {    
        String jndi_conf = Eurocarb.getProperty("ecdb.use_jndi.config");
        if ( jndi_conf == null || jndi_conf.length() == 0 )
        {
            log.warn(
                "JNDI URL not given so reverting to default: " 
                + Default_Jndi_Config_File );
            jndi_conf = Default_Jndi_Config_File;
        }
        
        log.info("trying JNDI configuration from file: " + jndi_conf );
        sessionFactory = new Configuration()
                        .configure( jndi_conf )
                        .buildSessionFactory();
                        
        log.debug("JNDI config successful");
    }

    
    /**
    *   Converts the given {@link DetachedCriteria} into a {@link Criteria}
    *   that can be executed.
    */
    public static final Criteria convertToCriteria( DetachedCriteria dc )
    {
        return dc.getExecutableCriteria( 
            getSessionFactory().getCurrentSession() );
    }
    
    
    /**
    *   Returns an SQL query string from the given HQL query string.
    */
    public static final String translateHql2Sql( String hql_query_string )
    {
        SessionFactoryImplementor sfi = (SessionFactoryImplementor) sessionFactory;
        QueryTranslatorFactory qtf = sfi.getSettings().getQueryTranslatorFactory();
        
        QueryTranslator qt = qtf.createQueryTranslator( 
            "translated_hql_query"
            , hql_query_string
            , emptyMap()
            , sfi 
        );
        
        qt.compile( emptyMap(), false );
        
        return qt.getSQLString();
    }
    

    /**
    *   Gets the Hibernate {@link SessionFactory} from which we're 
    *   getting our {@link Session}s.
    */
    public static SessionFactory getSessionFactory() 
    {
        if ( sessionFactory == null )
        {
            log.debug("sessionFactory == null, dynamically initialising one...");
            init();
        }
            
        return sessionFactory;  
    }
    
    
    /** 
    *   Rolls back current transaction, doesn't close Hibernate {@link Session}.
    *   {@inheritDoc}
    */
    public void abortUnitOfWork()
    {
        /*         
        if ( ! uowStarted )
            throw new EurocarbException(
                "Unit of work not yet started, cannot abort");
        */      
        
        Transaction tx = getHibernateSession().getTransaction();
        if ( tx == null ) 
            return;
        
        if ( ! tx.isActive() )
        {
            log.warn("a transaction is not active, cannot be aborted");
            return;
        }
            
        try
        {
            log.warn("aborting current transaction, trying to rollback...");
            tx.rollback();
            log.debug("transaction successfully rolled back");
        }
        catch ( Throwable ex ) 
        {
            log.error( "Could not rollback transaction - "
                     + "exception caught during rollback:", ex );
        }
        finally
        {
            uowStarted = false;   
        }
    }
    
    
    /** 
    *   Opens new (Hibernate-wrapped) transaction.
    *   {@inheritDoc}
    */
    public void beginUnitOfWork()
    {
        log.debug("starting database transaction");
        getHibernateSession().beginTransaction();
        uowStarted = true;
    }
        
    
    /** 
    *   Commits current transaction, rolls back if commit throws Exception. 
    *   {@inheritDoc}
    */
    public void endUnitOfWork() 
    {
        boolean success = false;
        try
        {
            if ( getHibernateSession().getTransaction().isActive() ) 
            {
                log.debug("*** committing database transaction ***");
                getHibernateSession().getTransaction().commit();
            }
            else 
            {
                log.warn("no active transaction, cannot be ended");
            }
            success = true;
        } 
        finally
        {
            if ( ! success )
                abortUnitOfWork();
            
            log.trace("(closing hibernate session)");
            getHibernateSession().close();
            uowStarted = false;
        }
    }
        
    
    /**
    *   Returns the current Hibernate {@link Session}, or creates one if necessary.
    *   @see {@link SessionFactory}
    */
    public Session getHibernateSession() 
    {
        return getSessionFactory().getCurrentSession();
    }
    
    
    /*  @see EntityManager#countAll(Class)  */
    public <T> int countAll( Class<T> c )
    {
        if ( log.isTraceEnabled() )
            log.trace("looking up total count of objects of " + c );
        
        Object count = getHibernateSession()
                      .createCriteria( c )
                      .setProjection( Projections.rowCount() )
                      .uniqueResult();
         
        if ( count == null ) 
            return 0;
        
        if ( count instanceof Integer )                      
        {
            return ((Integer) count).intValue(); 
        }
        else if ( count instanceof Long )
        {
            return ((Long) count).intValue();
        }
        else return 0;
    }
    
    
    /*  @see EntityManager#createNew(Class)  */
    public <T> T createNew( Class<T> c ) 
    {
        return __instance_of( c );
    }

    
    /*  @see EntityManager#flush()  */
    public void flush() 
    {
        log.trace("attempting to manually flush data to data store.");
        if ( getHibernateSession().getTransaction().isActive() ) 
        {
            log.debug("*** manually flushing data to data store ***");
            getHibernateSession().flush();
        }
        else 
        {
            log.warn("no active transaction, cannot flush data to store.");
        }
    }
    
    
    /*  @see EntityManager#createQuery(Class)  */
    public <T> Criteria createQuery( Class<T> c )
    {
        return getHibernateSession().createCriteria( c );
    }
    
    
    /*  @see EntityManager#getQuery(String)  */
    public Query getQuery( String name_of_query )
    {
        return getHibernateSession().getNamedQuery( name_of_query );
    }

    
    /*  @see EntityManager#lookup(Class,Serializable)  */
    @SuppressWarnings("unchecked") 
    // ^^^ unavoidable, due to lack of genericity in Hibernate lib
    public <T> T lookup( Class<T> c, int object_id ) 
    {
        if ( log.isDebugEnabled() )
            log.debug( "attempting to lookup object of " 
                     + c.getName() 
                     + " with id=" 
                     + object_id );
        
        try
        {
            Session session = getHibernateSession();
            T entity = (T) session.get( c, object_id );
            return entity;
        }
        catch ( NonUniqueObjectException e )
        {
            __log_exception( e, c );
            throw e;
        }
    }

    
    /** 
    *   Attempts to pre-fetch most of the data for the given object, including
    *   each of its properties and associations. If the passed object is a 
    *   {@link Collection} then the whole Collection will be pre-fetched.
    */
    public <T> void populate( T object )
    {
        Hibernate.initialize( object );       
    }
    
    
    /*  @see EntityManager#lookup(T,Serializable)  */
    public <T> void lookup( T destinationObject, int objectId ) 
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "attempting to populate existing object " 
                     + destinationObject
                     + " with id=" 
                     + objectId 
                     );
        }
        
        try
        {
            Session session = getHibernateSession();
            session.load(destinationObject, objectId);
        }
        catch ( NonUniqueObjectException e )
        {
            __log_exception( e, destinationObject );
            throw e;
        }
    }
       
    public <T> void revert( T entity )
    {
        Session s = getHibernateSession();
        s.evict(entity);
    }
        
    /*  @see EntityManager#remove(T)  */
    public <T> void remove( T entity )
    {
        if ( log.isDebugEnabled() )
            log.debug( "attempting to remove (detach) object " 
                     + entity
                     );
            
        Session s = getHibernateSession();
        s.delete(entity);
    }
    
    
    /*  @see EntityManager#removeAll  */
    public <T> void removeAll( Set<? extends T> entities )
    {
        for ( T e : entities )
            remove( e );
    }
    
    
    /*  @see EntityManager#store(T)  */
    public <T> void store( T entity ) 
    {
        if ( log.isDebugEnabled() )
            log.debug( "attempting to store (make persistent) object " 
                     + entity
                     );
            
        Session s = getHibernateSession();
        
        if ( entity instanceof EurocarbObject )
            validate( (EurocarbObject) entity );
        
        try
        {
            s.save( entity );
            //s.saveOrUpdate( entity );
        }
        catch ( NonUniqueObjectException ex )
        {
            log.warn( "passed " 
                 + entity.getClass().getSimpleName() 
                 + " was originally loaded in a different Session"
                 + "; attempting to merge changes..."
                 , ex 
             );
            
            s.merge( entity );   
            log.debug( "changes merged ok" );
        }
    }

    
    /**
    *   Checks that the passed {@link EurocarbObject} is valid, throws
    *   exceptions if it isn't.
    */
    void validate( EurocarbObject x ) throws EurocarbException
    {
        try
        {
            x.validate();
        }
        catch ( EurocarbException ex )
        {
            log.warn("Caught exception while validating " + x, ex );
            throw ex;
        }
        
        validateContributor(x);
        
    }
    
    private void validateContributor( Object x ) throws EurocarbException
    {
        return;
    }
    
    private void validateContributor( Contributed x ) throws EurocarbException
    {
        if (x.getContributor() == null) {
            throw new EurocarbException("Could not validate EurocarbObject, contributor is null");
        }
    }
    
    
    /*  @see EntityManager#update(T)  */
    public <T> void update( T entity ) 
    {        
        if ( log.isDebugEnabled() )
            log.debug("attempting to update object " + entity );
            
        Session s = getHibernateSession();
        s.update( entity );
    }


    public <T> void refresh( T entity ) 
    {
        if ( log.isDebugEnabled() )
            log.debug( "attempting to refresh object of " 
                     + entity.getClass() 
                     );
        Session s = getHibernateSession();    
        s.refresh( entity );
    }

    
    @Deprecated
    public void refreshContributor(Contributed entity) 
    {
        if( entity==null || entity.getContributor()==null || entity.getContributor().getContributorId()<0 )
            return;
        
        Contributor contributor = lookup(Contributor.class,entity.getContributor().getContributorId());
        if( contributor!=null )
            entity.setContributor(contributor);
    }

    
    public <T> void merge( T entity ) 
    {
        if ( log.isDebugEnabled() )
            log.debug( "attempting to merge object of " 
                     + entity.getClass() 
                     );
        Session s = getHibernateSession();    
        s.merge( entity );
    }
    

    public <T> void storeAll( Set<? extends T> entities )
    {
        for ( T e : entities )
            store( e );
    }
    
    
    /*  instantiates an instance of passed class; absorbs exceptions.  */
    private static final <T> T __instance_of( Class<T> c ) 
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
    

    private final void __log_exception( NonUniqueObjectException e, Object entity )
    {
        log.warn( "Caught NonUniqueObjectException while working with "
                + entity.getClass().getName()
                + ". This often means that you need to (re-)implement "
                + "the equals(Object) & hashCode() methods in this class "
                + "so that objects in the session can be compared for "
                + "*equality* rather than *identity*.",
                e
                );
    }
    
} // end class
