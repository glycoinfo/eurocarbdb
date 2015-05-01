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
// Generated Apr 16, 2007 10:40:29 AM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.core;


//  stdlib imports
import java.io.Serializable;


//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class CompositionId  *//**********************************************
*
*
*/ 
public class CompositionId extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int glycanSequenceId;
      
    private String component;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public CompositionId() {}

    
    /** full constructor */
    public CompositionId( int glycanSequenceId, String component ) 
    {
        this.glycanSequenceId = glycanSequenceId;
        this.component = component;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    public boolean equals(Object other) 
    {
        if ( this == other) return true;
        
        if ((other ==null) || (other.getClass() != this.getClass() ) ) return false;
        
        CompositionId x = (CompositionId) other;
            
        return ((this.getGlycanSequenceId() == x.getGlycanSequenceId()) && (this.getComponent() == x.getComponent()));
    }
    
    
    public int getId() 
    {
        return this.getGlycanSequenceId();
    }
    
    
    public int hashCode()
    {
        return (this.getGlycanSequenceId() ^ component.hashCode());
    }


    /*  getGlycanSequenceId  *//******************************** 
    *
    */ 
    public int getGlycanSequenceId() 
    {
        return this.glycanSequenceId;
    }
    
    
    /*  setGlycanSequenceId  *//******************************** 
    *
    */
    public void setGlycanSequenceId( int glycanSequenceId ) 
    {
        this.glycanSequenceId = glycanSequenceId;
    }
    

    /*  getComponent  *//******************************** 
    *
    */ 
    public String getComponent() 
    {
        return this.component;
    }
    
    
    /*  setComponent  *//******************************** 
    *
    */
    public void setComponent( String component ) 
    {
        this.component = component;
    }
    
       

    







} // end class
