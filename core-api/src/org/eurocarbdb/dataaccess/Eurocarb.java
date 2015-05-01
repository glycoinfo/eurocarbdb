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
*   Last commit: $Rev: 1870 $ by $Author: david@nixbioinf.org $ on $Date:: 2010-02-23 #$  
*/
package org.eurocarbdb.dataaccess;

//  stdlib imports
import java.util.*;

import java.io.File;
import java.io.Serializable;

import java.net.URI;
import java.net.URISyntaxException;

//  3rd party imports
import org.apache.log4j.Logger;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;


import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.HibernateException;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.ProjectionList;

//  eurocarb imports
import org.eurocarbdb.dataaccess.core.*;
// import org.eurocarbdb.dataaccess.hibernate.HibernateUtil;
import org.eurocarbdb.dataaccess.EntityManager;

//  static imports
import static org.eurocarbdb.util.StringUtils.CR;
import static org.eurocarbdb.util.StringUtils.coerce;
import static org.eurocarbdb.util.StringUtils.repeat;


/*  class Eurocarb  *//**********************************************
*<p>
*   Core class for exposing key elements of data access and funcionality
*   for the Eurocarb platform.
*</p>
*<p>
*   All access to EurocarbDB and associated data normally occurs through
*   an {@link EntityManager} instance, which essentially acts as a factory class
*   for EurocarbDB objects. 
*</p>
*<p>
*   Code using EntityManagers to create/manipulate data objects
*   should statically import the getEntityManager methods from
*   this class at their own convenience.
*</p>
*<p>
*   ie:
*<tt>
*   import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
*</tt>
*</p>
*<p>
*   Access to project-wide constants and settings is provided through
*   the getProperty method of this class.
*</p>
*
*   @author mjh
*/
public final class Eurocarb
{
    /** Logging handle. */
    public static final Logger log = Logger.getLogger( Eurocarb.class );

    /** This class is not intended to be instantiated. */
    private Eurocarb() {}
    

    
    /*
    *   multiple sections to this class:
    *       -   EntityManager stuff
    *       -   Project-wide configuration properties
    *       -   user access & authentication
    *       -   utility methods
    */
    
    
    //~~~~~~~~~~~~~~~~~  EntityManager stuff  ~~~~~~~~~~~~~~~~~~~~~~~
    
    private static final Map<String,EntityManager> Entity_Managers
        = new HashMap<String,EntityManager>();


    /*/**************************************************************
    *
    *   Register all default EMs here.
    */
    static 
    {   
        registerEntityManager( "default", new HibernateEntityManager() );
    }


    /**
    *   Looks up a Eurocarb data object using its canonical id.
    */
    public static final <T> T lookup( Class<T> c, int id )
    {
        return getEntityManager().lookup( c, id );   
    }
    
    /*  getEntityManager  *//**************************************** 
    *
    *   Returns the default EntityManager. 
    */
    public static final EntityManager getEntityManager()
    {
        return getEntityManager("default");
    }
    
    /**
     * Clone of getEntityManager(), just prevents the need to cast my hand.
     * @return
     */
    public static final HibernateEntityManager getHibernateEntityManager(){
    	return (HibernateEntityManager) getEntityManager("default");
    }
    
    public static final Session getHibernateSession(){
    	return getHibernateEntityManager().getHibernateSession();
    }
    
    public static final Query getHqlQuery(String query){
    	return getHibernateEntityManager().getHibernateSession().createQuery(query);
    }

    /*  getEntityManager  *//**************************************** 
    *
    *   Returns the EntityManager associated with the given name.
    *   The default EntityManager can be obtained via the String name
    *   "default".
    */
    static final EntityManager getEntityManager( String name )
    {
        assert name != null;
        EntityManager em = Entity_Managers.get( name );
        assert( em != null );
        
        return em;
    }
    

    /*  registerEntityManager  *//*********************************** 
    *
    *   Registers an EntityManager for use. 
    */
    static final void registerEntityManager( String name, EntityManager em )
    {
        assert em != null;
        Entity_Managers.put( name, em );
    }

    
    // mjh: this is a placeholder for functionality that may be added in the future
    /*
    static final void unregisterEntityManager( String name )
    {
        assert( Entity_Managers.contains( name ) );
        
        Entity_Managers.remove( name );
    }
    */
    
    
    //~~~~~~~~~~~~~~~~~  User/Contributor stuff  ~~~~~~~~~~~~~~~~~~~~
    
    /** 
    *   This is the ID of the {@link Contributor} that is "logged into" the current Thread. 
    *   This will be 0 (the guest contributor id) if noone is logged into this Thread. 
    */
    static ThreadLocal<Integer> currentContributorId = new ThreadLocal<Integer>();
 
    /** This is the {@link Contributor} that is logged into the current Thread. If noone
    *   is logged in atm, then this will be the "guest" contributor.
    */
    static ThreadLocal<Contributor> currentContributor = new ThreadLocal<Contributor>();   
    
    
    /** 
    *   Returns the {@link Contributor} whose contributor_id is bound 
    *   to the current thread. 
    */
    public static final Contributor getCurrentContributor()
    {
        if ( currentContributor.get() != null )
            return currentContributor.get();
            
        Integer contrib_id = currentContributorId.get();
        
        if ( contrib_id == null || contrib_id == 0 )
        {
            Contributor guest = Contributor.getGuestContributor();
            assert currentContributor != null;
            currentContributor.set( guest );
        }
        else if ( contrib_id < 0 )
        {
            throw new IllegalArgumentException(
                "currentContributorId was < 0" );
        }
        else
        {
            Contributor user = getEntityManager().lookup( 
                                    Contributor.class, contrib_id );
            
            if ( user == null )
            {
                log.warn( 
                    "Invalid contributorId '" 
                    + contrib_id 
                    + "'"
                );
                return null;
            }
            else currentContributor.set( user );
        }
        
        return currentContributor.get();   
    }
    
    
    //~~~~~~~~~~~~~~~~~~  Config/Property access  ~~~~~~~~~~~~~~~~~~~

    /** Where to find main property config file. */
    public static final String EUROCARB_CONF = "eurocarbdb-core.properties";

    /** Hash of project-wide properties. */
    static CompositeConfiguration config = null;     

    
    //  init property hash from conf file(s).
    static void initConfig() 
    {
        //if ( config == null )
        config = new CompositeConfiguration();

        try
        {
            // log.info("adding configuration: " + EUROCARB_OVERRIDES_CONF );
            // config.addConfiguration( 
            //     new PropertiesConfiguration( EUROCARB_OVERRIDES_CONF ) );
            // log.info( "configured properties for core-api: \n" 
            //     + ConfigurationUtils.toString( config ) );

            log.info("adding core-api configuration: " + EUROCARB_CONF );
            config.addConfiguration( 
                new PropertiesConfiguration( EUROCARB_CONF ) );
        }
        catch ( ConfigurationException ex )
        {
            throw new RuntimeException( ex );
        }
        
        if ( log.isInfoEnabled() )
        {
            log.info( 
                CR 
                + repeat('=', 20 )
                + " configured eurocarb core-api properties " 
                + repeat('=', 20 )
                + CR 
                + ConfigurationUtils.toString( config ) 
                + CR 
                + repeat('=', 80 )
            );
        }       
        
    }


    /*  getConfiguration  *//****************************************
    *
    *   Returns the current core-api configuration.
    */
    public static final CompositeConfiguration getConfiguration()
    {
        if ( config == null )
            initConfig();
        
        return config;
    }


    /*  getProperty  *//*********************************************
    *
    *   Shortcut method that returns the value of the passed property 
    *   name. Throws an exception if the given property name doesn't
    *   exist in the property hash.
    */
    public static final String getProperty( String property_name )
    {
        if (! getConfiguration().containsKey( property_name ) )
        {
            log.warn( "Given property '" 
                    + property_name 
                    + "' does not exist in current config" );
            return null;
        }
        
        try
        {
            return (String) getConfiguration().getProperty( property_name );
        }
        catch ( Exception e )
        {
            log.warn( "Caught exception while looking up property '"
                    + property_name
                    + "':"
                    , e
                    );
            return null;
        }
    }
    
    /*
    public static final void LogConfig()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Configured Eurocarb properties:" + CR );
        Iterator allKeys = config.getKeys();
        while ( allKeys.hasNext() )
        {
            Object key = allKeys.next();
            sb.append(  "    "
                     + key 
                     + " = " 
                     + config.getProperty((String) key)
                     + CR
                     );
        }            
        
        log.info( sb.toString() );
    }
    */
    
    /** 
    *   Same as {@link #getProperty}, except the property value is pre-cast to
    *   the given {@link Class}. Eg:
    *<pre>
    *       int limit = getProperty("some_property_name", Integer.class ); 
    *</pre>
    *   This method works fine for the majority of Java primitive types 
    *   ({@link Integer}, {@link Long}, {@link Float}, {@link Double}, etc), 
    *   with the exception of {@link Character}.
    *
    *   @throws ClassCastException 
    *   If the property value could not be coerced to the given class.
    *   @throws NumberFormatException
    *   If the desired class was a primitive numeric type and couldn't be parsed.
    */
    public static final <T> T getProperty( String property_name, Class<T> as_class )
    {
        String property_value = getProperty( property_name );
        return coerce( property_value, as_class );
    }
    
    
    /*  getPropertyAsURI  *//****************************************
    *
    *   Convenience method to return the value of a property as a URI object. 
    *   @deprecated use <tt>getProperty("property_name", URI.class );</tt>
    */
    @Deprecated
    public static final URI getPropertyAsURI( String property_name )
    {
        String url_property = getProperty( property_name );
    
        //  basic sanity check 
        if ( url_property == null || url_property.length() == 0 )
            throw new RuntimeException( "Expected a value for property '" 
                                      + url_property 
                                      + "'" 
                                      );
        
        //  ensure it's a valid URI
        URI uri;
        try 
        {  
            uri = new URI( url_property );  
        }
        catch ( URISyntaxException e ) 
        {  
            throw new RuntimeException( "Malformed url syntax for property '" 
                                      + url_property 
                                      + "'" 
                                      );
        }

        return uri;
    }
 
    
    
    //~~~~~~~~~~~~~~~~~~~ UTILITY METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /*  getRecentContributions  *//********************************** 
    *
    *   Returns a {@link List} of given length of the most recently 
    *   {@link Contributed} objects to the current data store, in order
    *   of most to least recent, or an empty list if there are no
    *   {@link Contributed} objects. Note that this method only
    *   returns additions -- modifications to older objects will not
    *   be included.
    *   
    *   @see Contributor.getMyRecentContributions(int)
    */
    public static List<Contributed> getRecentContributions( int max_results )
    {
    // hibernate cannot limit polymorphic queries in the database
    // we have to do it one class by one
        log.debug("looking up all Contributed objects");

        // get all contributed objects
        ArrayList<Contributed> changes = new ArrayList<Contributed>();
        changes.addAll( getRecentlyContributed( GlycanSequence.class, max_results));
        changes.addAll( getRecentlyContributed( Evidence.class, max_results));
        /**
         * FIXME: I do have a fix in mind for this, but I want to make sure I catch
         * everything that was going via BiologicalContext before I reimplement the 
         * Contributed interface on this class.
         */
        //changes.addAll( getRecentlyContributed( BiologicalContext.class, max_results));
        changes.addAll( getRecentlyContributed( Reference.class, max_results));
    
        // sort by date
        Collections.sort( 
            changes,
            new Comparator<Contributed> () 
            {        
                public int compare(Contributed o1, Contributed o2) 
                {
                    return - o1.getDateEntered().compareTo(o2.getDateEntered());
                }
                
                public boolean equals(Object obj) 
                {
                    return this==obj;
                }
            } 
        );
        
        // get sublist
        if (changes.size() < max_results) {
            return changes;
        }
        
        return changes.subList( 0, max_results );
    }
    

    public static <T> List<T> getRecentlyContributed( Class<T> c, int max_results ) 
    {
        //  does c implement Contributed?
        if ( Contributed.class.isAssignableFrom( c ) )
        {
            /*  TODO: remove explicit hibernate reference */
            List results = 
                ((HibernateEntityManager) getEntityManager())
                .getHibernateSession()
                .createCriteria( c )
                .setResultTransformer( CriteriaSpecification.DISTINCT_ROOT_ENTITY )
                .addOrder( Order.desc("dateEntered") )
                .setMaxResults(max_results)
                .list();
                
            if ( results == null )
                return Collections.emptyList();
            
            return (List<T>) results;
        }
        else
        {
            List results = 
                ((HibernateEntityManager) getEntityManager())
                .getHibernateSession()
                .createCriteria( c )
                .addOrder( Order.desc("id") )
                .setMaxResults(max_results)
                .list();
                
            if ( results == null )
                return Collections.emptyList();
            
            return (List<T>) results;
        }
    }
    
    
} // end class
