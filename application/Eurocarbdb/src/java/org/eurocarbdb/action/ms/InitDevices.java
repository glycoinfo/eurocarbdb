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

import java.util.*;

import org.hibernate.*; 
import org.hibernate.criterion.*; 

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;
import org.eurocarbdb.dataaccess.hibernate.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
* @author                         aceroni
* @version                        $Rev: 1549 $
*/
public class InitDevices extends EurocarbAction 
{

    static String[][] instruments = new String[13][3];    

    public int added_manufacturers = 0;

    public int added_devices = 0;

    static 
    {
        instruments[0] = new String[]{ "Agilent" , "XCT", "ESI"};
        instruments[1] = new String[]{ "Bruker Daltonik", "Ultraflex I", "MALDI"};
        instruments[2] = new String[]{ "Bruker Daltonik", "Ultraflex II", "MALDI"};
        instruments[3] = new String[]{ "Bruker Daltonik", "Esquire3000", "ESI"};
        instruments[4] = new String[]{ "Bruker Daltonik", "HCT ultra", "ESI"};
        instruments[5] = new String[]{ "Bruker Daltonik", "MicrOTOF", "ESI"};
        instruments[6] = new String[]{ "Bruker Daltonik", "MicrOTOF-Q", "ESI"};
        instruments[7] = new String[]{ "Bruker Daltonik", "Apex IV", "ESI"};
        instruments[8] = new String[]{ "Applied Biosystem", "Voyager", "MALDI"};
        instruments[9] = new String[]{ "Applied Biosystem", "4800 TOF/TOF", "MALDI"};
        instruments[10] = new String[]{ "Applied Biosystem", "Q-Star", "ESI"};
        instruments[11] = new String[]{ "Waters", "Q-TOF", "ESI"};
        instruments[12] = new String[]{ "Waters", "Q-TOF premiere", "ESI"};

    }
    

    public int getAddedManufacturers() 
    {
        return added_manufacturers;
    }

    public int getAddedDevices() 
    {
        return added_devices;
    }

    public Collection<Manufacturer> getManufacturers() 
    {
        Criteria crit = getEntityManager().createQuery(Manufacturer.class);
        return crit.list();        
    }

    public Collection<Device> getDevices() 
    {
        Criteria crit = getEntityManager().createQuery(Device.class);
        return crit.list();
    } 

    
    public String execute() throws Exception 
    {

        added_manufacturers = 0;  
        added_devices = 0;
        
        for( int i=0; i<instruments.length; i++ ) 
        {

            //----------
            // add new device
            
            // set the manufacturer
            Manufacturer manufacturer = findManufacturer(instruments[i][0]);
            if( manufacturer==null )
            {
                // create a new manufacturer
                manufacturer = new Manufacturer(instruments[i][0]);

                // store the manufacturer
                Eurocarb.getEntityManager().store(manufacturer);              

                added_manufacturers++;
            }

            // check if the device is existing
            Device device = findDevice(instruments[i][1]);
            if( device==null ) 
            {

                // create a new device
                device = new Device(manufacturer,instruments[i][1],instruments[i][2]);

                // store
                Eurocarb.getEntityManager().store(device);          

                added_devices++;    
            }
        }
        
        return SUCCESS;
    }      
    

    public Manufacturer findManufacturer(String manufacturer) 
    {
        Criteria crit = getEntityManager()
                        .createQuery(Manufacturer.class)
                        .add(Restrictions.eq("name", manufacturer));
        
        Collection<Manufacturer> list = crit.list();
        if( list==null || list.size()==0 )
            return null;
        return list.iterator().next();
    }  

    
    public Device findDevice(String device) 
    {
        Criteria crit = getEntityManager()
                        .createQuery(Device.class)
                        .add(Restrictions.eq("model", device));
        
        Collection<Device> list = crit.list();
        if( list==null || list.size()==0 )
            return null;
        return list.iterator().next();
    }
      

}
