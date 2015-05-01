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

package org.eurocarbdb.action.hplc;

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.*;
import org.eurocarbdb.dataaccess.hibernate.*;

import org.hibernate.*; 
import org.hibernate.criterion.*; 

import java.util.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
* @author                         aceroni
* @version                        $Rev: 1549 $
*/
public class InitDevices extends EurocarbAction 
{

    static String[][] instruments = new String[1][6];    

    public int added_manufacturers = 0;

    public int added_devices = 0;

    static {
        instruments[0] = new String[]{ "Agilent" , "XCT", "1", "1", "2", "3"};
    }
    

    public int getAddedManufacturers() {
        return added_manufacturers;
    }

    public int getAddedDevices() {
        return added_devices;
    }

    public Collection<Detector> getDetectors() {
        Criteria crit = getEntityManager().createQuery(Detector.class);
        return crit.list();        
    }

    
    public String execute() throws Exception {

        added_manufacturers = 0;  
        added_devices = 0;
        
        for( int i=0; i<instruments.length; i++ ) {

            //----------
            // add new device
            
            // set the manufacturer
            Detector detector = findDetector(instruments[i][0]);

                // store the manufacturer
                Eurocarb.getEntityManager().store(detector);              

                added_manufacturers++;

            // check if the device is existing
        }
        
        return SUCCESS;
    }      

    

    public Detector findDetector(String detector) {
        Criteria crit = getEntityManager().createQuery(Detector.class);
        
        Collection<Detector> list = crit.list();
        if( list==null || list.size()==0 )
            return null;
        return list.iterator().next();
    }  


}
