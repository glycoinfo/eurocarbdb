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


public class SelectDetector extends EurocarbAction {
    protected static final Logger logger = Logger.getLogger( SelectDetector.class.getName() );
    
    private String manufacturer;
    private String model;
    private int detector_id = -1;
    private Detector detector = null;
    private Instrument instrument = null;
    private Column column = null;
    private int column_id;
    public String execute() throws Exception {
    if( detector_id <0 ) {
        return INPUT;
    }
        // store
    else  {
        detector = Eurocarb.getEntityManager().lookup( Detector.class, detector_id );
       //  Eurocarb.getEntityManager().store(detector);  

       return SUCCESS; 
    }
    }      



    public Collection<Detector> getDetectors() {
    logger.info("getDetectors");
    Criteria crit = getEntityManager().createQuery(Detector.class);
    logger.info("getDetectors " + crit.list());
    return crit.list();
    } 

  
    
    public void setDetectorId(int id) {
    this.detector_id = id;
    }

    public int getDetectorId() {
    return this.detector_id;
    }


     public Detector getDetector() {
        return detector;
    }



    public void setDetector (Detector detector) {
        this.detector = detector;
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

    
    
    public Column getColumn() {
        return column;
    }



    public void setColumn (Column column) {
        this.column = column;
    }

     public void setColumnId(int id) {
    this.column_id = id;
    }

    public int getColumnId() {
    return this.column_id;
    }
}
