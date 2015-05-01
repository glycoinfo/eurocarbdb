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
/*
* $Id: EntityManager.java 1236 2009-06-19 15:32:49Z hirenj $
* Last changed $Author: hirenj $
* EUROCarbDB Project
*/
package org.eurocarbdb.dataaccess;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

import org.hibernate.Query;
import org.hibernate.Criteria;

/**
*   Specifies the common interface for classes that act as data stores 
*   for CRUD (create/remove/update) persistance & query operations. 
*   The general idioms for each of these basic operations is given below.
*    
*<h2>Examples</h2>
*
*<h3>Create</h3>
*<pre>
*       //  creates a new (non-persistant, AKA transient) GlycanSequence object
*       GlycanSequence gs = Eurocarb.getEntityManager().createNew( GlycanSequence.class ); 
*</pre>
*
*<h3>Store (make persistant)</h3>
*<pre>
*       //  add data to object...
*       gs.setStuff( someStuff );
*       gs.addMoreData( someData );
*
*       //  save (make persistant) object to current data store
*       Eurocarb.getEntityManager().store( gs ); 
*</pre>
*
*<h3>Update</h3>
*<pre>
*       //   make changes to (already persistant) object
*       gs.setStuff( newValue );
*       gs.addMoreData( newData );
*
*       //  update state of object in data store
*       Eurocarb.getEntityManager().update( gs ); 
*</pre>
*
*<h3>Remove</h3>
*<pre>
*       //  remove (delete) the given object from this data store.
*       Eurocarb.getEntityManager().remove( gs );
*</pre>
*
*<h3>An actual, working example</h3>
*
*   To add a new BiologicalContext to a carbohydrate sequence:
*<pre>
        EntityManager em = Eurocarb.getEntityManager();
        
        //  lookup a carbohydrate sequence, which may or may not already exist
        String seq_glycoct = ...;
        SugarSequence seq = new SugarSequence( seq_glycoct, SequenceFormat.Glycoct );
        GlycanSequence gs = GlycanSequence.createOrLookup( seq );
        
        //  lookup some biological context info
        int tax_id = ..., tissue_id = ...;
        Taxonomy taxonomy = em.lookup( Taxonomy.class, tax_id );
        TissueTaxonomy tissue = em.lookup( TissueTaxonomy.class, tissue_id );
        
        //  create & populate the biological context, and add it to the sequence
        BiologicalContext bc = new BiologicalContext();
        bc.setTissueTaxonomy( tissue );
        bc.setTaxonomy( taxonomy );
        gs.addBiologicalContext( bc );
        
        //  explicitly record the changes
        em.store( gs );
*</pre>
*
*   @see        Eurocarb.getEntityManager()
*   @author     mjh, hirenj 
*   @version    $Rev: 1236 $
*/
public interface EntityManager 
{
    /** 
    *   Signals the beginning of what could be considered a 'unit of work'
    *   for this {@link EntityManager}, eg: for a database-backed EntityManager,
    *   this method would start a JDBC transaction.
    */
    public void beginUnitOfWork()
    ;
    
    
    /** 
    *   Signals the end of what could be considered a 'unit of work'
    *   for this {@link EntityManager}, eg: for a database-backed EntityManager,
    *   this method would attempt to commit a JDBC transaction.
    */
    public void endUnitOfWork()
    ;
    
    
    /** 
    *   Signals the beginning of what could be considered a 'unit of work'
    *   for this {@link EntityManager}, eg: for a database-backed EntityManager,
    *   this method would abort (rollback) a JDBC transaction.
    */
    public void abortUnitOfWork()
    ;    
    
    
    /** Count all objects of the given class in the current data store. */
    public <T> int countAll( Class<T> c )
    ;
    
    
    /**
    *   Create a new entity object of the given class. 
    *
    * @param <T>       Class of clazz
    * @param clazz     Class to create an entity object for
    * @return          New instance of the object
    */
    public <T> T createNew( Class<T> c )
    ;
        

    /**
    *   Returns a {@link Criteria} object, based on the given 
    *   {@link Class}, which can be used to perform adhoc queries
    *   against the data store represented by this {@link EntityManager}.
    */
    public <T> Criteria createQuery( Class<T> c )
    ;
    
    
    /**
    *   Returns a {@link Query} object that corresponds to an existing query
    *   with the given string name, which can be used to execute pre-prepared 
    *   queries against the data store represented by this {@link EntityManager}. 
    *   
    *   @param name_of_query 
    *   The name of the query - note that query names are generally
    *   prefixed by the fully-qualified class name of the object type
    *   they return.
    *           
    *   @return     
    *   an instance of a Query object that can perform the 
    *   given named query.
    */
    public Query getQuery( String name_of_query )
    ;


    /**
    *   Look up and return the entity object with the given objectId for the given
    *   class/type, returning null if no such object exists in the current data store. 
    *  
    * @param <T>       Class of entity to return
    * @param clazz     Class to create an entity object for
    * @param entityId  Id of entity to try populating data from
    * @return          Entity object with the given objectId, null otherwise
    */
    public <T> T lookup( Class<T> c, int entity_id ) 
    throws EntityDoesntExistException
    ;
    
    
    /**
    *   Populate the given object with data from this data store corresponding 
    *   to the given entity id. The class of the entity to look up is determined
    *   from the runtime type of the given destination object.
    *
    *   @param <T>                  Class of entity being returned
    *   @param destinationObject    Destination object to populate with data
    *   @param entityId             Id of the entity from which to populate data
    */
    public <T> void lookup( T destinationObject, int entity_id ) 
    throws EntityDoesntExistException
    ;
    

    public <T> void populate( T object )
    ;
    
    
    /**
    *   Store an entity in the data store that this EntityManager represents
    * 
    * @param <T>       Class of entity
    * @param entity    Entity to store
    */
    public <T> void store( T entity )
    ;

    
    /**
    *   Remove an entity from the data store that this EntityManager represents
    * 
    * @param <T>       Class of entity
    * @param entity    Entity to store
    */
    public <T> void remove( T entity );

    
    /**
    *   Update an entity in the data store that this EntityManager represents
    * 
    * @param <T>       Class of entity
    * @param entity    Entity to store
    */
    public <T> void update( T entity )
    ;

    public <T> void revert( T entity )
    ;
    
    /**
    *   Force (flush) the current set of changes to the data store. This
    *   is normally done as needed by the data store itself, however it is
    *   occasionally necessary to force this behaviour, for example, if the
    *   data store-generated ID for a new object is needed immediately for
    *   subsequent operations.
    */
    public void flush();
    
    
    public <T> void refresh( T entity );  
    
    
    @Deprecated
    public <T> void merge( T entity );
    
    
    @Deprecated
    public void refreshContributor( Contributed entity );
   
    
}
