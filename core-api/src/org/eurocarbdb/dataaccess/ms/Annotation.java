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
*   Last commit: $Rev: 1425 $ by $Author: hirenj $ on $Date:: 2009-07-05 #$  
*/

package org.eurocarbdb.dataaccess.ms;

//  stdlib imports
import java.util.*;

import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.application.glycanbuilder.TextUtils;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import org.eurocarbdb.dataaccess.Contributed;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Annotation  *//**********************************************
*
*
*/ 
public class Annotation extends BasicEurocarbObject implements Serializable, Contributed
{

    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int annotationId;
   
    private Scan scan;
   
    private Contributor contributor;

    private GlycanSequence parentStructure;
  
    private Persubstitution persubstitution;

    private ReducingEnd reducingEnd;
               
    private Date dateEntered;
 
    private double contributorQuality;

    private Set<PeakAnnotation> peakAnnotations = new HashSet<PeakAnnotation>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    public Annotation() {
    }

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getAnnotationId  *//******************************** 
    *
    */ 
    public int getAnnotationId() 
    {
        return this.annotationId;
    }

  
    /*  setAnnotationId  *//******************************** 
    *
    */
    public void setAnnotationId( int annotationId ) 
    {
        this.annotationId = annotationId;
    }
    

    /*  getScan  *//******************************** 
    *
    */ 
    public Scan getScan() 
    {
        return this.scan;
    }
    
    
    /*  setScan  *//******************************** 
    *
    */
    public void setScan( Scan scan ) 
    {
        this.scan = scan;
    }
       
    /*  getContributor  *//******************************** 
    *
    */ 
    public Contributor getContributor() 
    {
        return this.contributor;
    }
    
    
    /*  setContributor  *//******************************** 
    *
    */
    public void setContributor( Contributor contributor ) 
    {
        this.contributor = contributor;
    }
       
    /*  getParentStructureId  *//******************************** 
    *
    */ 
    public GlycanSequence getParentStructure() 
    {
        return this.parentStructure;
    }
    
    
    /*  setParentStructure  *//******************************** 
    *
    */
    public void setParentStructure( GlycanSequence parentStructure ) 
    {
        this.parentStructure = parentStructure;
    }

    /*  getPersubstitution  *//******************************** 
    *
    */ 
    public Persubstitution getPersubstitution() 
    {
        return this.persubstitution;
    }
    
    
    /*  setPersubstitution  *//******************************** 
    *
    */
    public void setPersubstitution( Persubstitution persubstitution ) 
    {
        this.persubstitution = persubstitution;
    }

    /*  getReducingEnd  *//******************************** 
    *
    */ 
    public ReducingEnd getReducingEnd() 
    {
        return this.reducingEnd;
    }
    
    
    /*  setReducingEnd  *//******************************** 
    *
    */
    public void setReducingEnd( ReducingEnd reducingEnd ) 
    {
        this.reducingEnd = reducingEnd;
    }

    /*  getDateEntered  *//******************************** 
    *
    */ 
    public Date getDateEntered() 
    {
        return this.dateEntered;
    }
    
    
    /*  setDateEntered  *//******************************** 
    *
    */
    public void setDateEntered( Date dateEntered ) 
    {
        this.dateEntered = dateEntered;
    }
    

    /*  getContributorQuality  *//******************************** 
    *
    */ 
    public double getContributorQuality() 
    {
        return this.contributorQuality;
    }
    
    
    /*  setContributorQuality  *//******************************** 
    *
    */
    public void setContributorQuality( double contributorQuality ) 
    {
        this.contributorQuality = contributorQuality;
    }
    

    /*  getPeakAnnotations  *//******************************** 
    *
    */ 
    public Set<PeakAnnotation> getPeakAnnotations() 
    {
        return this.peakAnnotations;
    }
    
    
    /*  setPeakAnnotations  *//******************************** 
    *
    */
    public void setPeakAnnotations( Set<PeakAnnotation> peakAnnotations ) 
    {
        this.peakAnnotations = peakAnnotations;
    }

    public List<PeakAnnotation> getPeakAnnotationsOrdered() {
    
    List<PeakAnnotation> ret = new ArrayList<PeakAnnotation>(this.getPeakAnnotations());
    Collections.sort(ret,new PeakAnnotationComparator());
    return ret;
    }

}