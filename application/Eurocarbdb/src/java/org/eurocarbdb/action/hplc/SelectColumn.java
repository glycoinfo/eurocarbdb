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

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;

import org.hibernate.*; 
import org.hibernate.criterion.*; 

import java.io.*;
import java.util.*;
import org.apache.commons.io.*;
import org.apache.log4j.Logger;


public class SelectColumn extends EurocarbAction {

protected static final Logger log = Logger.getLogger( SelectColumn.class.getName() );


    private String manufacturer;
    private String model;
    private int column_id = -1;
    private Column column = null;
    private List<Column> showTypes;
    private Instrument instrument;
    private Detector detector;

    private int detector_id;
    private int instrument_id;

    public String execute() throws Exception {

log.info("instr_id:" + instrument_id + "detect" + detector_id);
    
        
    if( column_id <0 ) {

        List display = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Column.SELECT_ALL")
        .list();

        showTypes = display;
        
        log.info("dis size" + display.size());
        
        
        
        return INPUT;
    }
        // store
    else  {
      //  column = Eurocarb.getEntityManager().lookup( Column.class, column_id );
       //  Eurocarb.getEntityManager().store(detector);  

       return SUCCESS; 
    }
    }      

   public Collection<Column> getColumns() {
    log.info("getColumns");
    Criteria crit = getEntityManager().createQuery(Column.class);
    log.info("getColumns " + crit.list());
    return crit.list();
    } 
    
    
/*
    public Collection<Column> getColumns() {
    Criteria crit = getEntityManager().createQuery(Column.class);
    return crit.list();
    } 
*/

//new 
     public List<Column> getQuery() {
        return showTypes;
    }

   public void setQuery( List<Column> showTypes) {
       this.showTypes = showTypes;
    }

    public List getShowTypes()
    {
        return this.showTypes;
    }

    //end new



    public void setColumnId(int id) {
    this.column_id = id;
    }

    public int getColumnId() {
    return this.column_id;
    }


     public Column getColumn() {
        return column;
    }



    public void setColumn (Column column) {
        this.column = column;
    }


    public String getManufacturer()
    {
        return this.manufacturer;
    }


    public void setManufacturer( String manufacturer )
    {
        this.manufacturer = manufacturer;
    }


    public Instrument getInstrument() {
        return instrument;
    }



    public void setInstrument (Instrument instrument) {
        this.instrument = instrument;
    }

    public Detector getDetector() {
    return detector;
    }

    public void setDetector (Detector detector) {
        this.detector = detector;
    }

    public void setInstrumentId(int id) {
        this.instrument_id = id;
    }

    public int getInstrumentId() {
        return this.instrument_id;
    }

    public void setDetectorId(int id) {
        this.detector_id = id;
    }

    public int getDetectorId() {
        return this.detector_id;
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
