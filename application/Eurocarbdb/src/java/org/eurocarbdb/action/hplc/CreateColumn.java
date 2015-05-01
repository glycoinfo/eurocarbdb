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
import org.eurocarbdb.dataaccess.hplc.Column;
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


public class CreateColumn extends EurocarbAction {

    protected static final Logger logger = Logger.getLogger( UserCreateColumn.class.getName() );

    private Column column = null;
    private String model;
    private String manufacturer;
    private String particleSize;
    private String packingMaterial;
    private double columnSizeLength;
    private double columnSizeWidth;
    
    public String execute() throws Exception {
    
    logger.info ("create new column test log");
    
    if ( this.getManufacturer() == null || this.getModel() == null) {
        logger.info("manufacturer and model details");
       
        return INPUT;
    }
    
    int sizeMan = manufacturer.length();
    int sizeModel = model.length();
    int sizeParticle = particleSize.length();
    int sizeMaterial = packingMaterial.length();
    //int sizeLength = columnSizeLength.length();
    //int sizeWidth =  columnSizeWidth.length();
    
    if (sizeMan <1 || sizeModel <1 || sizeParticle < 1 || sizeMaterial <1 || columnSizeLength <0.00005 || columnSizeWidth < 0.00005) {
    
    this.addActionError( "All fields are compulsory" );
    return ERROR;
    }
    
    if ( this.getManufacturer() != null || this.getModel() != null) {
    logger.info("check status of details entered");
    
    Criteria critColumn = getEntityManager().createQuery(Column.class)
    .add(Restrictions.eq("manufacturer", manufacturer))
    .add(Restrictions.eq("model", model))
    .add(Restrictions.eq("packingMaterial", packingMaterial))
    .add(Restrictions.eq("columnSizeWidth", columnSizeWidth))
    .add(Restrictions.eq("columnSizeLength", columnSizeLength))
    .add(Restrictions.eq("particleSize", particleSize));
    
    Collection<Column> columnList = critColumn.list();
    if( columnList==null || columnList.size()==0 ){
        
    Column storeColumn = new Column();
    storeColumn.setManufacturer(manufacturer);
    storeColumn.setModel(model);
    storeColumn.setPackingMaterial(packingMaterial);
    storeColumn.setColumnSizeWidth(columnSizeWidth);
    storeColumn.setColumnSizeLength(columnSizeLength);
    storeColumn.setParticleSize(particleSize);
    
        
    try{
    getEntityManager().store(storeColumn);
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
    



    
    
    public Column getColumn() {
        return column;
    }
    
    public void setColumn (Column column) {
        this.column = column;
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

    public void setPackingMaterial(String packingMaterial) {
                this.packingMaterial = packingMaterial;
        }

      public String getPackingMaterial() {
               return this.packingMaterial;
        }

    public void setColumnSizeWidth(Double columnSizeWidth) {
                this.columnSizeWidth = columnSizeWidth;
        }

      public Double getColumnSizeWidth() {
               return this.columnSizeWidth;
        }

    public void setColumnSizeLength(Double columnSizeLength) {
                this.columnSizeLength = columnSizeLength;
        }

      public Double getColumnSizeLength() {
               return this.columnSizeLength;
        }

    public void setParticleSize(String particleSize) {
                this.particleSize = particleSize;
        }

      public String getParticleSize() {
               return this.particleSize;
        }

}
