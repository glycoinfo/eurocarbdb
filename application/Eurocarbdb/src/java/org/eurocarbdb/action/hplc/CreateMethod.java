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

import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.eurocarbdb.action.*;

import org.hibernate.*;
import org.hibernate.criterion.*;
import java.util.*;
import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;


import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;
import org.hibernate.*;
import org.hibernate.cfg.*;

public class CreateMethod extends EurocarbAction {

    private MethodRun methodrun = null;
    private Profile parent = null; 
    
    private int profile_id = 65;
    private String solventA;
    private String solventB;
    private String solventC;
    private String solventD;
    private Double temperature;
    private Double flowRate;
    private String flowGradient;
    
    
     protected static final Logger logger = Logger.getLogger ( CreateMethod.class);
    
    
    public String execute() throws Exception {



       if ( solventA == null ) {
       
            return INPUT;
    }
    else {
       
    logger.info("solventa" + solventA + "PROFILEID" + profile_id);
    parent = Profile.lookupById(profile_id);
        //Eurocarb.getEntityManager().lookup( Profile.class, profile_id );
    logger.info("parent" + parent);
    methodrun = new MethodRun();
    methodrun.setProfile(parent);
    methodrun.setTemperature(temperature);
    methodrun.setSolventA(solventA);
    methodrun.setSolventB(solventB);
    methodrun.setSolventC(solventC);
    methodrun.setSolventD(solventD);
    methodrun.setFlowRate(flowRate);
    methodrun.setFlowGradient(flowGradient);
    
        Eurocarb.getEntityManager().store(methodrun);  
        return SUCCESS;
}
    }      


    public MethodRun getMethodRun() {
        return methodrun;
    }

    public void setMethodRun (MethodRun methodrun) {
        this.methodrun     = methodrun;
    }



// getter and setter for evaluting parent ids
  
     public void setProfileId(int id) {
        this.profile_id = id;
    }

    public int getProfileId() {
        return this.profile_id;
    }


    public String getSolventA() {
        return this.solventA;
    }
    
    public void setSolventA( String solvent){
        this.solventA = solvent;
    }
    
    public String getSolventB() {
        return this.solventB;
    }
    
    public void setSolventB( String solvent){
        this.solventB = solvent;
    }
    
    public String getSolventC() {
        return this.solventC;
    }
    
    public void setSolventC( String solvent){
        this.solventC = solvent;
    }
    
    
    public String getSolventD() {
        return this.solventD;
    }
    
    public void setSolventD( String solvent){
        this.solventD = solvent;
    }
    
    
    public Double getTemperature() {
        return this.temperature;
    }
    
    public void setTemperature( Double temp){
        this.temperature = temp;
    }
    
    
    
    public Double getFlowRate() {
        return this.flowRate;
    }
    
    
    public void setFlowRate( Double rate){
        this.flowRate = rate;
    }
    
    
    
    public String getFlowGradient() {
        return this.flowGradient;
    }

    
    public void setFlowGradient( String gradient){
        this.flowGradient = gradient;    
    }
    
    
}
