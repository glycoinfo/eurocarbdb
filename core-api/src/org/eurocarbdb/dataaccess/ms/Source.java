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
// Generated Apr 3, 2007 6:49:20 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.ms;

//  stdlib imports
import java.util.HashSet;
import java.util.Set;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Source  *//**********************************************
*
*
*/ 
public class Source extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int sourceId;
      
    private Device device;
      
    private String model;
      
    private String sourceType;
      
    private Set<SourceParameter> sourceParameters = new HashSet<SourceParameter>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Source() {}

    /** Minimal constructor */
    public Source( Device device, String sourceType ) 
    {
        this.device = device;
        this.sourceType = sourceType;
    }
    
    /** full constructor */
    public Source( Device device, String model, String sourceType, Set<SourceParameter> sourceParameters ) 
    {
        this.device = device;
        this.model = model;
        this.sourceType = sourceType;
        this.sourceParameters = sourceParameters;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getSourceId  *//******************************** 
    *
    */ 
    public int getSourceId() 
    {
        return this.sourceId;
    }
    
    
    /*  setSourceId  *//******************************** 
    *
    */
    public void setSourceId( int sourceId ) 
    {
        this.sourceId = sourceId;
    }
    

    /*  getDevice  *//******************************** 
    *
    */ 
    public Device getDevice() 
    {
        return this.device;
    }
    
    
    /*  setDevice  *//******************************** 
    *
    */
    public void setDevice( Device device ) 
    {
        this.device = device;
    }
    

    /*  getModel  *//******************************** 
    *
    */ 
    public String getModel() 
    {
        return this.model;
    }
    
    
    /*  setModel  *//******************************** 
    *
    */
    public void setModel( String model ) 
    {
        this.model = model;
    }
    

    /*  getSourceType  *//******************************** 
    *
    */ 
    public String getSourceType() 
    {
        return this.sourceType;
    }
    
    
    /*  setSourceType  *//******************************** 
    *
    */
    public void setSourceType( String sourceType ) 
    {
        this.sourceType = sourceType;
    }
    

    /*  getSourceParameters  *//******************************** 
    *
    */ 
    public Set<SourceParameter> getSourceParameters() 
    {
        return this.sourceParameters;
    }
    
    
    /*  setSourceParameters  *//******************************** 
    *
    */
    public void setSourceParameters( Set<SourceParameter> sourceParameters ) 
    {
        this.sourceParameters = sourceParameters;
    }
    
       

    







} // end class
