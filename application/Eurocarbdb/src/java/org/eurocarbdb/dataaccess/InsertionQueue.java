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
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/
/**
* $Id: InsertionQueue.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package org.eurocarbdb.dataaccess;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;

import com.opensymphony.xwork.ActionContext;

/**
* An InsertionQueue for entities waiting to be inserted into the database
* Utility class used by the SessionEntityManager. Implements a number of 
* lists which add entries into the session based upon the classname of the 
* objects being inserted.
* 
* @author          hirenj
* @version         $Rev: 1549 $
*/
class InsertionQueue {

    Logger logger = Logger.getRootLogger();
    
    InsertionQueue() {
    }

    /**
     * Add an object into the queue, returning the position it was added in
     * @param <T>
     * @param newObject    Object to add to the queue
     * @return             Position that the object was added
     */
    @SuppressWarnings("unchecked")
    <T> int add(T newObject) {
        Class clazz = newObject.getClass();
        List<T> queue = (List<T>) getQueue(clazz);
        queue.add(newObject);
        setQueue(clazz, queue);
        return queue.size();
    }
    
    /**
     * Add an object into the queue at a given position
     * @param <T>
     * @param newObject    Object to add to the queue
     * @param queueId      Position to add the object to
     * @return             Position that the object was added
     */
    @SuppressWarnings("unchecked")
    <T> int add(T newObject, int queueId) {
        Class clazz = newObject.getClass();
        List<T> queue = (List<T>) getQueue(clazz);
        queue.set(queueId - 1, newObject);
        setQueue(clazz, queue);
        return queueId;
    }

    /**
     * Delete an object from the queue
     * @param <T>
     * @param currentObject     Object to delete from queue
     * @param queueId           Id in the queue that the object can be found
     * @return                  Id of object that was deleted
     */
    @SuppressWarnings("unchecked")
    <T> int delete(T currentObject, int queueId) {
        Class clazz = currentObject.getClass();
        List<T> queue = (List<T>) getQueue(clazz);
        queue.set(queueId - 1, null);
        setQueue(clazz, queue);
        return queueId;
    }
    
    /**
     * Get an object from the queue
     * @param <T>
     * @param clazz            Get an object from the queue that has a given class
     * @param index            Index of the object in the queue to retrieve
     * @return                 The object at the given index. Null if no object exists 
     */
    @SuppressWarnings("unchecked")
    <T> T get(Class<T> clazz, int index) {
        List<T> queue = (List<T>) getQueue(clazz);
        try {
            T entity = queue.get(index - 1);
            return entity;
        } catch (IndexOutOfBoundsException e) {
            // TODO - Throw an exception here!
            return null;
        }
    } 

    private Map getSessionMap() {
        return (Map) ActionContext.getContext().get("session");
    }
    
    @SuppressWarnings({ "unchecked" })
    private <T> List<T> getQueue(Class<T> clazz) {
        Map session = getSessionMap();
        if (session.containsKey(clazz.getName())) {
            return (List<T>) session.get(clazz.getName());
        }
        List<T> queue = new ArrayList<T>();
        session.put(clazz.getName(), queue);
        return queue;
    }
    
    @SuppressWarnings("unchecked")
    private <T> void setQueue(Class<T> name, List<T> queue) {
        Map session = getSessionMap();
        session.put(name.getName(),queue);
    }
}
