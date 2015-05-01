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
*   Last commit: $Rev: 1525 $ by $Author: matthew.campbell1980 $ on $Date:: 2009-07-15 #$  
*/
// Generated Jun 21, 2007 2:07:01 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports

import java.io.Serializable;
import java.util.List;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class DigestSingle  *//**********************************************
*
*
*/ 
public class DigestSingle extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int id;
      
    private int glycanId;
      
    private String name;
      
    private String enzyme;
      
    private int productId;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public DigestSingle() {}

    
    /** full constructor */
    public DigestSingle( int glycanId, String name, String enzyme, int productId ) 
    {
        this.glycanId = glycanId;
        this.name = name;
        this.enzyme = enzyme;
        this.productId = productId;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getId  *//******************************** 
    *
    */ 
    public int getId() 
    {
        return this.id;
    }
    
    
    /*  setId  *//******************************** 
    *
    */
    public void setId( int id ) 
    {
        this.id = id;
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
    

    /*  getProductId  *//******************************** 
    *
    */ 
    public int getProductId() 
    {
        return this.productId;
    }
    
    
    /*  setProductId  *//******************************** 
    *
    */
    public void setProductId( int productId ) 
    {
        this.productId = productId;
    }
    
       
    public static List<DigestSingle> lookupId ( int id)
    {
    List i = getEntityManager()
           .getQuery( "org.eurocarbdb.dataaccess.hplc.DigestSingle.GLYCAN_ENTRY_DIGESTS")
           .setParameter("glycan_id", id )
           .list();

    assert i instanceof DigestSingle;

    return (List<DigestSingle>) i;
    }

    







} // end class
