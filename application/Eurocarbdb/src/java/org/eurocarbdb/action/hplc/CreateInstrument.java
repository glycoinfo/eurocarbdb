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
import org.eurocarbdb.dataaccess.hplc.Instrument;
import org.eurocarbdb.dataaccess.hibernate.*;
import org.apache.log4j.Logger;

import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.cfg.*;

import java.util.*;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;



public class CreateInstrument extends EurocarbAction {

    private Instrument instrument = null;
    protected static final Logger logger = Logger.getLogger ( CreateInstrument.class.getName());
    private String manufacturer;
    private String model;
    private String solventA;
    private String solventB;
    private String solventC;
    private String solventD;
    private String flowGradient;
    private double temperature;
    private double flowRate;
    
    private String error = "please retry";

    public String execute() throws Exception {

    
    if ( this.getManufacturer() == null || this.getModel() == null) {
      return INPUT;
    }

    int sizeMan = manufacturer.length();
    int sizeModel = model.length();
    if (sizeMan <1 || sizeModel <1) {
    
    this.addActionError( "The manufactuer and model are compulsory fields" );
    return ERROR;

    //return INPUT;
    }
    
    if ( this.getManufacturer() != null || this.getModel() != null) {
    logger.info("check status of details entered");

    Criteria critInstrument = getEntityManager().createQuery(Instrument.class)
            .add(Restrictions.eq("manufacturer", manufacturer))
            .add(Restrictions.eq("model", model));
            /*.add(Restrictions.eq("temperature", temperature))
            .add(Restrictions.eq("solventA", solventA))
            .add(Restrictions.eq("solventB", solventB))
            .add(Restrictions.eq("solventC", solventC))
            .add(Restrictions.eq("solventD", solventD))
            .add(Restrictions.eq("flowGradient", flowGradient))
            .add(Restrictions.eq("flowRate", flowRate));*/

    Collection<Instrument> instrumentList = critInstrument.list();
        if( instrumentList==null || instrumentList.size()==0 ){
        
        /*int sizeA = solventA.length();
        int sizeB = solventB.length();
        int sizeFlowGradient = flowGradient.length();
    
    
        if (  sizeA == 0) { solventA = "n/a";}
        if ( sizeB == 0) { solventB = "n/a";}
        if ( sizeFlowGradient == 0) { flowGradient = "no details";}
        if ( temperature < 1) { temperature = 0;}
        if ( flowRate < 1) { flowRate = 0;}
        */

        Instrument storeInstrument = new Instrument();
        storeInstrument.setManufacturer(manufacturer);
        storeInstrument.setModel(model);
        /*storeInstrument.setTemperature(temperature);
        storeInstrument.setSolventA(solventA);
        storeInstrument.setSolventB(solventB);
        storeInstrument.setSolventC(solventC);
        storeInstrument.setSolventD(solventD);
        storeInstrument.setFlowGradient(flowGradient);
        storeInstrument.setFlowRate(flowRate);*/
        getEntityManager().store(storeInstrument);
     
    return SUCCESS;
    }
    }
    
    return INPUT;
    }

/*    
    //Instrument db_instrument = findInstrument(instrument.getManufacturer());
    //Instrument db_instrument_model = findInstrumentModel(instrument.getModel());
    
    Instrument db_instrument = findInstrument(manufacturer);
    Instrument db_instrument_model = findInstrumentModel(model);

    logger.info("check query results:" + db_instrument);

    if( db_instrument!=null ){
            //Eurocarb.getEntityManager().store(detector);
    //this.addActionError( "Form not filled" );
    this.addFieldError( "Manufacturer", "Invalid  id " + db_instrument_model );
    return ERROR;}
    
    if( db_instrument_model!=null ){
            //Eurocarb.getEntityManager().store(detector);
    //this.addActionError( "Form not filled" );
    this.addFieldError( "Model", "Invalid  id " + db_instrument_model );
    return ERROR;}
    
    if( db_instrument!=null || db_instrument_model!=null ){
            //Eurocarb.getEntityManager().store(detector);
    //this.addActionError( "Form not filled" );
    this.addFieldError( "Manufacturer and Model", "Invalid");
    return ERROR;}
    
    
    if( db_instrument==null && db_instrument_model==null ) {  
    logger.info("should be here now with man" + manufacturer);

    //Eurocarb.getEntityManager().store(instrument);  
    }
*/
    //    return SUCCESS;
    
/*    else {Eurocarb.getEntityManager().store(instrument);  
        return SUCCESS;
    }
*/
 //   }      

/*    
    public Instrument findInstrument(String instrument) {
        Criteria crit = getEntityManager().createQuery(Instrument.class).add(Restrictions.eq("manufacturer", instrument));

        Collection<Instrument> list = crit.list();
        if( list==null || list.size()==0 )
            return null;
        return list.iterator().next();
    }

    
    public Instrument findInstrumentModel(String detector) {
        Criteria crit = getEntityManager().createQuery(Instrument.class).add(Restrictions.eq("model", instrument));

        Collection<Instrument> list = crit.list();
        if( list==null || list.size()==0 )
            return null;
        return list.iterator().next();
    }
*/
    
    
    

        public Instrument getInstrument() {
        return instrument;
        }

        public void setInstrument (Instrument instrument) {
        this.instrument = instrument;
        }

    
        public void setManufacturer(String manufacturer) {
                this.manufacturer = manufacturer;
        }

      public String getManufacturer() {
               return this.manufacturer;
        }

    public void setModel(String model) {
                this.model = model;
        }

      public String getModel() {
               return this.model;
        }

    public void setTemperature(Double temperature) {
                this.temperature = temperature;
        }

      public Double getTemperature() {
               return this.temperature;
        }

    public void setSolventA (String solventA) {
                this.solventA = solventA;
        }

      public String getSolventA() {
               return this.solventA;
        }

    public void setSolventB (String solventB) {
                this.solventB = solventB;
        }

      public String getSolventB() {
               return this.solventB;
        }

    public void setSolventC (String solventC) {
                this.solventC = solventC;
        }

      public String getSolventC() {
               return this.solventC;
        }

    public void setSolventD (String solventD) {
                this.solventD = solventD;
        }

      public String getSolventD() {
               return this.solventD;
        }

    public void setFlowGradient (String flowGradient) {
                this.flowGradient = flowGradient;
        }

      public String getFlowGradient() {
               return this.flowGradient;
        }
    
    public void setflowRate (Double flowRate) {
                this.flowRate = flowRate;
        }

      public Double getFlowRate() {
               return this.flowRate;
        }



}
