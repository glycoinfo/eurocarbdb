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
// Generated Jun 21, 2007 2:07:02 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Enzyme  *//**********************************************
*
*
*/ 
public class Enzyme extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int enzymeId;
      
    private String name;
      
    private String abbreviationId;
      
    private String accessionNumber;
      
    private String description;
      
    private String supplier;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Enzyme() {}

    /** Minimal constructor */
    public Enzyme( String name, String abbreviationId, String accessionNumber, String description ) 
    {
        this.name = name;
        this.abbreviationId = abbreviationId;
        this.accessionNumber = accessionNumber;
        this.description = description;
    }
    
    /** full constructor */
    public Enzyme( String name, String abbreviationId, String accessionNumber, String description, String supplier ) 
    {
        this.name = name;
        this.abbreviationId = abbreviationId;
        this.accessionNumber = accessionNumber;
        this.description = description;
        this.supplier = supplier;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getEnzymeId  *//******************************** 
    *
    */ 
    public int getEnzymeId() 
    {
        return this.enzymeId;
    }
    
    
    /*  setEnzymeId  *//******************************** 
    *
    */
    public void setEnzymeId( int enzymeId ) 
    {
        this.enzymeId = enzymeId;
    }
    

    /*  getName  *//******************************** 
    *
    */ 
    public String getName() 
    {
        return this.name;
    }
    
    
    /*  setName  *//******************************** 
    *
    */
    public void setName( String name ) 
    {
        this.name = name;
    }
    

    /*  getAbbreviationId  *//******************************** 
    *
    */ 
    public String getAbbreviationId() 
    {
        return this.abbreviationId;
    }
    
    
    /*  setAbbreviationId  *//******************************** 
    *
    */
    public void setAbbreviationId( String abbreviationId ) 
    {
        this.abbreviationId = abbreviationId;
    }
    

    /*  getAccessionNumber  *//******************************** 
    *
    */ 
    public String getAccessionNumber() 
    {
        return this.accessionNumber;
    }
    
    
    /*  setAccessionNumber  *//******************************** 
    *
    */
    public void setAccessionNumber( String accessionNumber ) 
    {
        this.accessionNumber = accessionNumber;
    }
    

    /*  getDescription  *//******************************** 
    *
    */ 
    public String getDescription() 
    {
        return this.description;
    }
    
    
    /*  setDescription  *//******************************** 
    *
    */
    public void setDescription( String description ) 
    {
        this.description = description;
    }
    

    /*  getSupplier  *//******************************** 
    *
    */ 
    public String getSupplier() 
    {
        return this.supplier;
    }
    
    
    /*  setSupplier  *//******************************** 
    *
    */
    public void setSupplier( String supplier ) 
    {
        this.supplier = supplier;
    }
    
       

    







} // end class
