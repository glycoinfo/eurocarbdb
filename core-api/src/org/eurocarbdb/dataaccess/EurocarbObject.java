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
*   Last commit: $Rev: 1313 $ by $Author: glycoslave $ on $Date:: 2009-06-29 #$  
*/

package org.eurocarbdb.dataaccess;

import org.eurocarbdb.dataaccess.exception.EurocarbException;

/**
*   Specifies the minimum interface for Eurocarb data objects.
*
*   The purpose of these methods is to provide a set of basic methods 
*   that are available for every Eurocarb object in the absence of 
*   specific class/type information, as well as provide a base type for 
*   the purposes of type-safe Collections and Arrays.
*
*   @author mjh
*/
public interface EurocarbObject 
{
    
    /*  getId  *//***************************************************
    *
    *   Returns a numeric identifer that, together with the value of 
    *   {@link #getIdentifierClass()}, uniquely identifies this object
    *   across all instances of objects of the same type (ie: 
    *   roughly equivalent to the primary key of a database table).
    *
    *   @see #getCanonicalId()
    */
    public int getId()
    ;
    
    
    /** 
    *   Returns the {@link Class}, which, together with the ID 
    *   returned by {@link #getId}, would be able to look up this 
    *   object uniquely. Certain EurocarbObject classes are 
    *   identified by an ID corresponding to one of their 
    *   superclasses; in those cases, this method must return
    *   that superclass.
    */
    public <T extends EurocarbObject> Class<T> getIdentifierClass()
    ;
    
    
    /*  getType  *//*************************************************
    *
    *   Returns this object's <em>type</em>. For example, "reference"
    *   for a Reference object, "biological_context" for a BiologicalContext 
    *   object.
    *
    *   @see #getCanonicalId()
    */
    public String getType()
    ;
    
    
    public String getVersion()
    ;
    
    
    /**
    *   Checks that this object contains all the necessary data and
    *   associations required to be submitted to the data-store.
    *   This method should throw typed exceptions to indicate
    *   an invalid state. This method can of course be called at
    *   any time, not just at save/flush time. 
    */
    public void validate() throws EurocarbException
    ;
    
    
} // end interface


