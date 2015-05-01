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

package org.eurocarbdb.action.ms;

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;
import org.eurocarbdb.dataaccess.hibernate.*;

import org.hibernate.*; 
import org.hibernate.criterion.*; 

import java.util.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
* @author             aceroni
* @version            $Rev: 1549 $
*/
public class CreateDevice extends AbstractMsAction 
{

    private Manufacturer manufacturer = null;
    private Device device = null;
    
    public String execute() throws Exception 
    {
        if( device==null || manufacturer==null )
            return INPUT;
                
        // add new manufacturer if necessary
        Manufacturer db_man = findManufacturer(manufacturer.getName());
        if( db_man==null ) 
            Eurocarb.getEntityManager().store(manufacturer);  
        else
            manufacturer = db_man;
        
        // add new device
        device.setManufacturer(manufacturer);
        Eurocarb.getEntityManager().store(device);  
    
            return SUCCESS;
    }      

    public Manufacturer findManufacturer(String manufacturer) 
    {
        Criteria crit = getEntityManager().createQuery(Manufacturer.class).add(Restrictions.eq("name", manufacturer));
        
        Collection<Manufacturer> list = crit.list();
        if( list==null || list.size()==0 )
            return null;
        return list.iterator().next();
    }

    
    public Device getDevice() 
    {
        return device;
    }

    public void setDevice(Device _device) 
    {
        this.device = _device;
    }


    public Manufacturer getManufacturer() 
    {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer _manufacturer) 
    {
        this.manufacturer = _manufacturer;
    }

}
