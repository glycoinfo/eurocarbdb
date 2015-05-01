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
// Generated Apr 18, 2007 5:02:28 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class RefLinkId  *//**********************************************
*
*
*/ 
public class RefLinkId extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int refId;
      
    private int glycanId;
      
    private Double paperGu;
      
    private String ms;
      
    private String msMs;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public RefLinkId() {}

    
    /** full constructor */
    public RefLinkId( int refId, int glycanId, Double paperGu, String ms, String msMs ) 
    {
        this.refId = refId;
        this.glycanId = glycanId;
        this.paperGu = paperGu;
        this.ms = ms;
        this.msMs = msMs;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getRefId  *//******************************** 
    *
    */ 
    public int getRefId() 
    {
        return this.refId;
    }
    
    
    /*  setRefId  *//******************************** 
    *
    */
    public void setRefId( int refId ) 
    {
        this.refId = refId;
    }
    

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
    

    /*  getPaperGu  *//******************************** 
    *
    */ 
    public Double getPaperGu() 
    {
        return this.paperGu;
    }
    
    
    /*  setPaperGu  *//******************************** 
    *
    */
    public void setPaperGu( Double paperGu ) 
    {
        this.paperGu = paperGu;
    }
    

    /*  getMs  *//******************************** 
    *
    */ 
    public String getMs() 
    {
        return this.ms;
    }
    
    
    /*  setMs  *//******************************** 
    *
    */
    public void setMs( String ms ) 
    {
        this.ms = ms;
    }
    

    /*  getMsMs  *//******************************** 
    *
    */ 
    public String getMsMs() 
    {
        return this.msMs;
    }
    
    
    /*  setMsMs  *//******************************** 
    *
    */
    public void setMsMs( String msMs ) 
    {
        this.msMs = msMs;
    }
    
       

    







} // end class
