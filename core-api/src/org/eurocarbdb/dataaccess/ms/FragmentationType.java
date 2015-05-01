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
// Generated Apr 3, 2007 6:49:18 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.ms;

//  stdlib imports
import java.util.HashSet;
import java.util.Set;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class FragmentationType  *//**********************************************
*
*
*/ 
public class FragmentationType extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int fragmentationTypeId;
      
    private Device device;
      
    private String fragmentationType;
      
    private Set<FragmentationParameter> fragmentationParameters = new HashSet<FragmentationParameter>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public FragmentationType() {}

    /** Minimal constructor */
    public FragmentationType( Device device, String fragmentationType ) 
    {
        this.device = device;
        this.fragmentationType = fragmentationType;
    }
    
    /** full constructor */
    public FragmentationType( Device device, String fragmentationType, Set<FragmentationParameter> fragmentationParameters ) 
    {
        this.device = device;
        this.fragmentationType = fragmentationType;
        this.fragmentationParameters = fragmentationParameters;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getFragmentationTypeId  *//******************************** 
    *
    */ 
    public int getFragmentationTypeId() 
    {
        return this.fragmentationTypeId;
    }
    
    
    /*  setFragmentationTypeId  *//******************************** 
    *
    */
    public void setFragmentationTypeId( int fragmentationTypeId ) 
    {
        this.fragmentationTypeId = fragmentationTypeId;
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
    

    /*  getFragmentationType  *//******************************** 
    *
    */ 
    public String getFragmentationType() 
    {
        return this.fragmentationType;
    }
    
    
    /*  setFragmentationType  *//******************************** 
    *
    */
    public void setFragmentationType( String fragmentationType ) 
    {
        this.fragmentationType = fragmentationType;
    }
    

    /*  getFragmentationParameters  *//******************************** 
    *
    */ 
    public Set<FragmentationParameter> getFragmentationParameters() 
    {
        return this.fragmentationParameters;
    }
    
    
    /*  setFragmentationParameters  *//******************************** 
    *
    */
    public void setFragmentationParameters( Set<FragmentationParameter> fragmentationParameters ) 
    {
        this.fragmentationParameters = fragmentationParameters;
    }
    
       

    







} // end class
