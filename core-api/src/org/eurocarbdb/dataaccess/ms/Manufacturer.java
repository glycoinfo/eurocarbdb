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
*   Last commit: $Rev: 1947 $ by $Author: khaleefah $ on $Date:: 2010-08-18 #$  
*/
// Generated Apr 3, 2007 6:49:19 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.ms;

//  stdlib imports
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Manufacturer  *//**********************************************
*
*
*/ 
public class Manufacturer extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int manufacturerId;
      
    private String name;
      
    private String url;
      
    private Set<Device> devices = new HashSet<Device>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Manufacturer() {}

    /** Minimal constructor */
    public Manufacturer( String name ) 
    {
        this.name = name;
    }
    
    /** full constructor */
    public Manufacturer( String name, String url, Set<Device> devices ) 
    {
        this.name = name;
        this.url = url;
        this.devices = devices;
    }
    

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~//
   
    /** Returns the {@link List} of all {@link Manufacturer}s. */
    @SuppressWarnings("unchecked")
    public static List<Manufacturer> getAllManufacturers()
    {
        return (List<Manufacturer>) getEntityManager()
            .getQuery("org.eurocarbdb.dataaccess.ms.Manufacturer.GET_ALL")
            .list();
    }
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getManufacturerId  *//******************************** 
    *
    */ 
    public int getManufacturerId() 
    {
        return this.manufacturerId;
    }
    
    
    /*  setManufacturerId  *//******************************** 
    *
    */
    void setManufacturerId( int manufacturerId ) 
    {
        this.manufacturerId = manufacturerId;
    }
    

    /*  getName  *//******************************** 
    *
    */ 
    public String getName() 
    {
        return this.name;
    }
    
    
    /*  setName  *//******************************** 
    *
    */
    public void setName( String name ) 
    {
        this.name = name;
    }
    

    /*  getUrl  *//******************************** 
    *
    */ 
    public String getUrl() 
    {
        return this.url;
    }
    
    
    /*  setUrl  *//******************************** 
    *
    */
    public void setUrl( String url ) 
    {
        this.url = url;
    }
    

    /*  getDevices  *//******************************** 
    *
    */ 
    public Set<Device> getDevices() 
    {
        return this.devices;
    }
    
    
    /*  setDevices  *//******************************** 
    *
    */
    public void setDevices( Set<Device> devices ) 
    {
        this.devices = devices;
    }
    public static Manufacturer getByName(String name)
    {
           Object i =getEntityManager().getQuery("org.eurocarbdb.dataaccess.ms.Manufacturer.GET_MAN")
    									   .setParameter("name",name)
    	                                   .uniqueResult();
           assert i instanceof Manufacturer;
           
           return (Manufacturer) i;
    }// end method
} // end class
