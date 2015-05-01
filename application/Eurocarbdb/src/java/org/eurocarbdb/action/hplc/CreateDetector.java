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
import org.eurocarbdb.dataaccess.hplc.Detector;
import org.eurocarbdb.dataaccess.hibernate.*;

import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.cfg.*;

import java.util.*;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;
import org.apache.log4j.Logger;

public class CreateDetector extends EurocarbAction {

    protected static final Logger logger = Logger.getLogger( CreateDetector.class.getName() );

    private Detector detector = null;
    private String manufacturer;
    private String model;
    private short excitation;
    private short emission;
    private double bandwidth;
    private short samplingRate;
    
    public String execute() throws Exception {

    logger.info ("create new detector test log");
    
    if ( this.getManufacturer() == null || this.getModel() == null) {
        logger.info("manufacturer and model details");
       
        return INPUT;
    }
    
    int sizeMan = manufacturer.length();
    int sizeModel = model.length();
    
    if (sizeMan <1 || sizeModel <1) {
    
    this.addActionError( "All fields are compulsory" );
    return ERROR;
    }
    
    if ( this.getManufacturer() != null || this.getModel() != null) {
    logger.info("check status of details entered");
    
    Criteria critDetector = getEntityManager().createQuery(Detector.class)
    .add(Restrictions.eq("manufacturer", manufacturer))
    .add(Restrictions.eq("model", model));
        
    Collection<Detector> detectorList = critDetector.list();
    if( detectorList==null || detectorList.size()==0 ){
        
    Detector storeDetector = new Detector();
    storeDetector.setManufacturer(manufacturer);
    storeDetector.setModel(model);
          
    try{
    getEntityManager().store(storeDetector);
    }
        catch ( Exception e ) {
        this.addActionError( "All fields are compulsory!" );
        return ERROR;
    }
    return SUCCESS;
    }
    }
  
    return INPUT;
    }
    


/*    public Detector findDetector(String detector) {
        Criteria crit = getEntityManager().createQuery(Detector.class).add(Restrictions.eq("manufacturer", detector));

        Collection<Detector> list = crit.list();
        if( list==null || list.size()==0 )
            return null;
        return list.iterator().next();
    }

    
    public Detector findDetectorModel(String detector) {
        Criteria crit = getEntityManager().createQuery(Detector.class).add(Restrictions.eq("model", detector));

        Collection<Detector> list = crit.list();
        if( list==null || list.size()==0 )
            return null;
        return list.iterator().next();
    }


    public Collection<Detector> getDetectors() {
        Criteria crit = getEntityManager().createQuery(Detector.class);
        return crit.list();
    }
*/


    public Detector getDetector() {
        return detector;
    }
    
    
    
    public void setDetector (Detector detector) {
        this.detector = detector;
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

    public void setBandwidth(Double bandwidth) {
                this.bandwidth = bandwidth;
        }

      public Double getBandwidth() {
               return this.bandwidth;
        }

    public void setEmission(short emission) {
                this.emission = emission;
        }

      public short getEmission() {
               return this.emission;
        }

    public void setExcitation(short excitation) {
                this.excitation = excitation;
        }

      public short getExcitation() {
               return this.excitation;
        }

    public void setSamplingRate(short samplingRate) {
                this.samplingRate = samplingRate;
        }

      public short getSamplingRate() {
               return this.samplingRate;
        }


}
