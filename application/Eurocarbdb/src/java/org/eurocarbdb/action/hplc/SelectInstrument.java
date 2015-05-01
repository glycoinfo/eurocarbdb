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
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.hibernate.*;

import org.hibernate.*; 
import org.hibernate.criterion.*; 

import java.io.*;
import java.util.*;
import org.apache.commons.io.*;
import org.apache.log4j.Logger;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


public class SelectInstrument extends EurocarbAction {

    private String manufacturer;
    private String model;
    private int instrument_id = -1;
    private Instrument instrument = null;

    public String execute() throws Exception {
        if( instrument_id <0 ) {
            return INPUT;
        }
            // store
        else  {
            instrument = Eurocarb.getEntityManager().lookup( Instrument.class, instrument_id );
           //  Eurocarb.getEntityManager().store(detector);  

           return SUCCESS; 
        }
    }      



    public Collection<Instrument> getInstruments() {
        Criteria crit = getEntityManager().createQuery(Instrument.class);
        return crit.list();
    } 

    public void setInstrumentId(int id) {
        this.instrument_id = id;
    }

    public int getInstrumentId() {
        return this.instrument_id;
    }


     public Instrument getInstrument() {
        return instrument;
    }



    public void setInstrument (Instrument instrument) {
        this.instrument = instrument;
    }





    public String getManufacturer()
    {
        return this.manufacturer;
    }


    public void setManufacturer( String manufacturer )
    {
        this.manufacturer = manufacturer;
    }



    public String getModel()
    {
        return this.model;
    }


    public void setModel( String model )
    {
        this.model = model;
    }

    
}
