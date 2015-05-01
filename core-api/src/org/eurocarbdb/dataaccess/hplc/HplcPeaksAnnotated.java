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
// Generated Jun 21, 2007 2:07:01 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports

import java.io.Serializable;

//  eurocarb imports

//import org.eurocarbdb.dataaccess.core.Evidence;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;



/*  class HplcPeaksAnnotated  *//**********************************************
*
*
*/ 
public class HplcPeaksAnnotated implements Serializable //extends Evidence implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int hplcPeaksAnnotatedId;
      
    private Integer profileId;
      
    private Integer digestId;
      
    private Integer assignedPeak;
      
    private Double peakArea;
      
    private Double gu;
      
    private Double dbGu;
      
    private String nameAbbreviation;
      
    private Integer glycanId;
      
    private Integer refined;

    private Glycan glycan;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public HplcPeaksAnnotated() {
        //setEvidenceType( Evidence.Type.HPLC );
    }

    
    /** full constructor */
    public HplcPeaksAnnotated( Integer profileId, Integer digestId, Integer assignedPeak, Double peakArea, Double gu, Double dbGu, String nameAbbreviation, Integer glycanId, Integer refined ) 
    {
    this();

        this.profileId = profileId;
        this.digestId = digestId;
        this.assignedPeak = assignedPeak;
        this.peakArea = peakArea;
        this.gu = gu;
        this.dbGu = dbGu;
        this.nameAbbreviation = nameAbbreviation;
        this.glycanId = glycanId;
        this.refined = refined;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    
   /** 
    *   Always returns {@link Evidence.Type.HPLC}. 
    *   @see Evidence.Type
    */
    /*public Type getEvidenceType()
    {
        return Evidence.Type.HPLC;   
    }
    */

    /*  getHplcPeaksAnnotatedId  *//******************************** 
    *
    */ 
    public int getHplcPeaksAnnotatedId() 
    {
        return this.hplcPeaksAnnotatedId;
    }
    
    
    /*  setHplcPeaksAnnotatedId  *//******************************** 
    *
    */
    public void setHplcPeaksAnnotatedId( int hplcPeaksAnnotatedId ) 
    {
        this.hplcPeaksAnnotatedId = hplcPeaksAnnotatedId;
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
    

    /*  getAssignedPeak  *//******************************** 
    *
    */ 
    public Integer getAssignedPeak() 
    {
        return this.assignedPeak;
    }
    
    
    /*  setAssignedPeak  *//******************************** 
    *
    */
    public void setAssignedPeak( Integer assignedPeak ) 
    {
        this.assignedPeak = assignedPeak;
    }
    

    /*  getPeakArea  *//******************************** 
    *
    */ 
    public Double getPeakArea() 
    {
        return this.peakArea;
    }
    
    
    /*  setPeakArea  *//******************************** 
    *
    */
    public void setPeakArea( Double peakArea ) 
    {
        this.peakArea = peakArea;
    }
    

    /*  getGu  *//******************************** 
    *
    */ 
    public Double getGu() 
    {
        return this.gu;
    }
    
    
    /*  setGu  *//******************************** 
    *
    */
    public void setGu( Double gu ) 
    {
        this.gu = gu;
    }
    

    /*  getDbGu  *//******************************** 
    *
    */ 
    public Double getDbGu() 
    {
        return this.dbGu;
    }
    
    
    /*  setDbGu  *//******************************** 
    *
    */
    public void setDbGu( Double dbGu ) 
    {
        this.dbGu = dbGu;
    }
    

    /*  getNameAbbreviation  *//******************************** 
    *
    */ 
    public String getNameAbbreviation() 
    {
        return this.nameAbbreviation;
    }
    
    
    /*  setNameAbbreviation  *//******************************** 
    *
    */
    public void setNameAbbreviation( String nameAbbreviation ) 
    {
        this.nameAbbreviation = nameAbbreviation;
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
    

    /*  getRefined  *//******************************** 
    *
    */ 
    public Integer getRefined() 
    {
        return this.refined;
    }
    
    
    /*  setRefined  *//******************************** 
    *
    */
    public void setRefined( Integer refined ) 
    {
        this.refined = refined;
    }
    
       
//
    public Glycan getGlycan()
    {
        return this.glycan;
    }


    public void setGlycan( Glycan glycan )
    {
        this.glycan = glycan;
    }

    


    public static HplcPeaksAnnotated deleteById( int id )
    {
    HplcPeaksAnnotated deleteRecord = getEntityManager().lookup( HplcPeaksAnnotated.class, id );
    getEntityManager().remove(deleteRecord);
    return null;
    }


} // end class
