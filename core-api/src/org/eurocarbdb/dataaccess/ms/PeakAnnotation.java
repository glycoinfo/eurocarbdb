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
import org.eurocarbdb.dataaccess.core.Contributor;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class PeakAnnotation  *//**********************************************
*
*
*/ 
public class PeakAnnotation extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int peakAnnotationId;
      
    private PeakLabeled peakLabeled;

    private Annotation annotation;
           
    private Set<PeakAnnotated> peakAnnotateds = new HashSet<PeakAnnotated>(0);
      
  
    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public PeakAnnotation() {}

    /** Minimal constructor */
    public PeakAnnotation( PeakLabeled peakLabeled, Annotation annotation) 
    {
        this.peakLabeled = peakLabeled;
        this.annotation = annotation;
    }
    
    /** full constructor */
    public PeakAnnotation( PeakLabeled peakLabeled, Annotation annotation, Set<PeakAnnotated> peakAnnotateds) 
    {
        this.peakLabeled = peakLabeled;
        this.annotation = annotation;    
        this.peakAnnotateds = peakAnnotateds;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getPeakAnnotationId  *//******************************** 
    *
    */ 
    public int getPeakAnnotationId() 
    {
        return this.peakAnnotationId;
    }
    
    
    /*  setPeakAnnotationId  *//******************************** 
    *
    */
    public void setPeakAnnotationId( int peakAnnotationId ) 
    {
        this.peakAnnotationId = peakAnnotationId;
    }
    
   
    /*  getPeakLabeled  *//******************************** 
    *
    */ 
    public PeakLabeled getPeakLabeled() 
    {
        return this.peakLabeled;
    }
    
    
    /*  setPeakLabeled  *//******************************** 
    *
    */
    public void setPeakLabeled( PeakLabeled peakLabeled ) 
    {
        this.peakLabeled = peakLabeled;
    }
    
 /*  getAnnotation  *//******************************** 
    *
    */ 
    public Annotation getAnnotation() 
    {
        return this.annotation;
    }
    
    
    /*  setAnnotation  *//******************************** 
    *
    */
    public void setAnnotation( Annotation annotation ) 
    {
        this.annotation = annotation;
    }
    

    /*  getPeakAnnotated  *//******************************** 
    *
    */ 
    public Set<PeakAnnotated> getPeakAnnotateds() 
    {
        return this.peakAnnotateds;
    }
    
    
    /*  setPeakAnnotateds  *//******************************** 
    *
    */
    public void setPeakAnnotateds( Set<PeakAnnotated> peakAnnotateds ) 
    {
        this.peakAnnotateds = peakAnnotateds;
    }
       

} // end class
