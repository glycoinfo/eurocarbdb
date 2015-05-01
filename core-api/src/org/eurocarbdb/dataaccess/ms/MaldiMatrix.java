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

/*  class MaldiMatrix  *//**********************************************
*
*
*/ 
public class MaldiMatrix extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int maldiMatrixId;
      
    private String matrix;
      
    private Set<MaldiParameter> maldiParameters = new HashSet<MaldiParameter>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public MaldiMatrix() {}

    /** Minimal constructor */
    public MaldiMatrix( String matrix ) 
    {
        this.matrix = matrix;
    }
    
    /** full constructor */
    public MaldiMatrix( String matrix, Set<MaldiParameter> maldiParameters ) 
    {
        this.matrix = matrix;
        this.maldiParameters = maldiParameters;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getMaldiMatrixId  *//******************************** 
    *
    */ 
    public int getMaldiMatrixId() 
    {
        return this.maldiMatrixId;
    }
    
    
    /*  setMaldiMatrixId  *//******************************** 
    *
    */
    public void setMaldiMatrixId( int maldiMatrixId ) 
    {
        this.maldiMatrixId = maldiMatrixId;
    }
    

    /*  getMatrix  *//******************************** 
    *
    */ 
    public String getMatrix() 
    {
        return this.matrix;
    }
    
    
    /*  setMatrix  *//******************************** 
    *
    */
    public void setMatrix( String matrix ) 
    {
        this.matrix = matrix;
    }
    

    /*  getMaldiParameters  *//******************************** 
    *
    */ 
    public Set<MaldiParameter> getMaldiParameters() 
    {
        return this.maldiParameters;
    }
    
    
    /*  setMaldiParameters  *//******************************** 
    *
    */
    public void setMaldiParameters( Set<MaldiParameter> maldiParameters ) 
    {
        this.maldiParameters = maldiParameters;
    }
    
       

    







} // end class
