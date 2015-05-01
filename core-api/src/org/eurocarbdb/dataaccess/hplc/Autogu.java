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
// Generated Jun 22, 2007 2:11:15 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Autogu  *//**********************************************
*
*
*/ 
public class Autogu extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int autoguId;
      
    private Integer glycanId;
      
    private Integer productId;
      
    private String enzyme;
      
    private Integer profileId;
      
    private Integer refinedId;
      
    private Integer digestId;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Autogu() {}

    
    /** full constructor */
    public Autogu( Integer glycanId, Integer productId, String enzyme, Integer profileId, Integer refinedId, Integer digestId ) 
    {
        this.glycanId = glycanId;
        this.productId = productId;
        this.enzyme = enzyme;
        this.profileId = profileId;
        this.refinedId = refinedId;
        this.digestId = digestId;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getAutoguId  *//******************************** 
    *
    */ 
    public int getAutoguId() 
    {
        return this.autoguId;
    }
    
    
    /*  setAutoguId  *//******************************** 
    *
    */
    public void setAutoguId( int autoguId ) 
    {
        this.autoguId = autoguId;
    }
    

    /*  getGlycanId  *//******************************** 
    *
    */ 
    public Integer getGlycanId() 
    {
        return this.glycanId;
    }
    
    
    /*  setGlycanId  *//******************************** 
    *
    */
    public void setGlycanId( Integer glycanId ) 
    {
        this.glycanId = glycanId;
    }
    

    /*  getProductId  *//******************************** 
    *
    */ 
    public Integer getProductId() 
    {
        return this.productId;
    }
    
    
    /*  setProductId  *//******************************** 
    *
    */
    public void setProductId( Integer productId ) 
    {
        this.productId = productId;
    }
    

    /*  getEnzyme  *//******************************** 
    *
    */ 
    public String getEnzyme() 
    {
        return this.enzyme;
    }
    
    
    /*  setEnzyme  *//******************************** 
    *
    */
    public void setEnzyme( String enzyme ) 
    {
        this.enzyme = enzyme;
    }
    

    /*  getProfileId  *//******************************** 
    *
    */ 
    public Integer getProfileId() 
    {
        return this.profileId;
    }
    
    
    /*  setProfileId  *//******************************** 
    *
    */
    public void setProfileId( Integer profileId ) 
    {
        this.profileId = profileId;
    }
    

    /*  getRefinedId  *//******************************** 
    *
    */ 
    public Integer getRefinedId() 
    {
        return this.refinedId;
    }
    
    
    /*  setRefinedId  *//******************************** 
    *
    */
    public void setRefinedId( Integer refinedId ) 
    {
        this.refinedId = refinedId;
    }
    

    /*  getDigestId  *//******************************** 
    *
    */ 
    public Integer getDigestId() 
    {
        return this.digestId;
    }
    
    
    /*  setDigestId  *//******************************** 
    *
    */
    public void setDigestId( Integer digestId ) 
    {
        this.digestId = digestId;
    }
    
} // end class
