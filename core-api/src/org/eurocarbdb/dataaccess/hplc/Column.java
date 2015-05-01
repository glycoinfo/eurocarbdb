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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
// Generated 02-Aug-2007 17:05:47 by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports
import java.util.HashSet;
import java.util.Set;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Column  *//**********************************************
*
*
*/ 
public class Column extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int columnId;
      
    private String manufacturer;
      
    private String packingMaterial;
      
    private double columnSizeWidth;
      
    private double columnSizeLength;
      
    private String particleSize;
      
    private String model;
      
    private Set<Profile> profiles = new HashSet<Profile>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Column() {}

    /** Minimal constructor */
    public Column( String manufacturer, 
                   String packingMaterial, 
                   double columnSizeWidth, 
                   double columnSizeLength, 
                   String particleSize  ) 
    {
        this.manufacturer = manufacturer;
        this.packingMaterial = packingMaterial;
        this.columnSizeWidth = columnSizeWidth;
        this.columnSizeLength = columnSizeLength;
        this.particleSize = particleSize;
    }
    
    /** full constructor */
    public Column( String manufacturer, 
                   String packingMaterial, 
                   double columnSizeWidth, 
                   double columnSizeLength, 
                   String particleSize, 
                   String model, 
                   Set<Profile> profiles  ) 
    {
        this.manufacturer = manufacturer;
        this.packingMaterial = packingMaterial;
        this.columnSizeWidth = columnSizeWidth;
        this.columnSizeLength = columnSizeLength;
        this.particleSize = particleSize;
        this.model = model;
        this.profiles = profiles;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getColumnId  *//********************************************* 
    *
    */ 
    public int getColumnId() 
    {
        return this.columnId;
    }
    
    
    /*  setColumnId  *//********************************************* 
    *
    */
    public void setColumnId( int columnId ) 
    {
        this.columnId = columnId;
    }
    

    /*  getManufacturer  *//***************************************** 
    *
    */ 
    public String getManufacturer() 
    {
        return this.manufacturer;
    }
    
    
    /*  setManufacturer  *//***************************************** 
    *
    */
    public void setManufacturer( String manufacturer ) 
    {
        this.manufacturer = manufacturer;
    }
    

    /*  getPackingMaterial  *//************************************** 
    *
    */ 
    public String getPackingMaterial() 
    {
        return this.packingMaterial;
    }
    
    
    /*  setPackingMaterial  *//************************************** 
    *
    */
    public void setPackingMaterial( String packingMaterial ) 
    {
        this.packingMaterial = packingMaterial;
    }
    

    /*  getColumnSizeWidth  *//************************************** 
    *
    */ 
    public double getColumnSizeWidth() 
    {
        return this.columnSizeWidth;
    }
    
    
    /*  setColumnSizeWidth  *//************************************** 
    *
    */
    public void setColumnSizeWidth( double columnSizeWidth ) 
    {
        this.columnSizeWidth = columnSizeWidth;
    }
    

    /*  getColumnSizeLength  *//************************************* 
    *
    */ 
    public double getColumnSizeLength() 
    {
        return this.columnSizeLength;
    }
    
    
    /*  setColumnSizeLength  *//************************************* 
    *
    */
    public void setColumnSizeLength( double columnSizeLength ) 
    {
        this.columnSizeLength = columnSizeLength;
    }
    

    /*  getParticleSize  *//***************************************** 
    *
    */ 
    public String getParticleSize() 
    {
        return this.particleSize;
    }
    
    
    /*  setParticleSize  *//***************************************** 
    *
    */
    public void setParticleSize( String particleSize ) 
    {
        this.particleSize = particleSize;
    }
    

    /*  getModel  *//************************************************
    *
    */ 
    public String getModel() 
    {
        return this.model;
    }
    
    
    /*  setModel  *//************************************************ 
    *
    */
    public void setModel( String model ) 
    {
        this.model = model;
    }
    

    /*  getProfiles  *//********************************************* 
    *
    */ 
    public Set<Profile> getProfiles() 
    {
        return this.profiles;
    }
    
    
    /*  setProfiles  *//********************************************* 
    *
    */
    public void setProfiles( Set<Profile> profiles ) 
    {
        this.profiles = profiles;
    }

} // end class
