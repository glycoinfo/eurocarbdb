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
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/
// Generated Jun 21, 2007 2:07:01 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class PictorialRepresentation  *//**********************************************
*
*
*/ 
public class PictorialRepresentation extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int glycanId;
      
    private String pictorialRepresentation;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public PictorialRepresentation() {}

    
    /** full constructor */
    public PictorialRepresentation( String pictorialRepresentation ) 
    {
        this.pictorialRepresentation = pictorialRepresentation;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getGlycanId  *//******************************** 
    *
    */ 
    public int getGlycanId() 
    {
        return this.glycanId;
    }
    
    
    /*  setGlycanId  *//******************************** 
    *
    */
    public void setGlycanId( int glycanId ) 
    {
        this.glycanId = glycanId;
    }
    

    /*  getPictorialRepresentation  *//******************************** 
    *
    */ 
    public String getPictorialRepresentation() 
    {
        return this.pictorialRepresentation;
    }
    
    
    /*  setPictorialRepresentation  *//******************************** 
    *
    */
    public void setPictorialRepresentation( String pictorialRepresentation ) 
    {
        this.pictorialRepresentation = pictorialRepresentation;
    }
    
       

    







} // end class
