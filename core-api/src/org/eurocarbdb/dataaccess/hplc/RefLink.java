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
// Generated Jul 2, 2007 4:31:41 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports

import java.io.Serializable;
import java.util.*;
import java.util.Date;

//  eurocarb imports
import org.eurocarbdb.dataaccess.Contributed;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.Eurocarb;

import org.apache.log4j.Logger;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class RefLink  *//**********************************************
*
*
*/ 
public class RefLink extends BasicEurocarbObject implements Serializable, Contributed 
{

    private static final Logger logger = Logger.getLogger( RefLink.class );
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int refLinkId;
      
    private int refId;
      
    private int glycanId;
      
    private Double paperGu;
      
    private String ms;
      
    private String msMs;

    private int coreReferenceId;

    private int refRefId;

    private Contributor contributor; 

    private Date dateEntered;


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public RefLink() {}

    /** Minimal constructor */
    public RefLink( int refId, int glycanId, int coreReferenceId, int refRefId ) 
    {
        this.refId = refId;
        this.glycanId = glycanId;
    this.coreReferenceId = coreReferenceId;
    this.refRefId = refRefId;
    }
    
    /** full constructor */
    public RefLink( int refId, int glycanId, Double paperGu, String ms, String msMs, int coreReferenceId, int refRefId ) 
    {
        this.refId = refId;
        this.glycanId = glycanId;
        this.paperGu = paperGu;
        this.ms = ms;
        this.msMs = msMs;
    this.coreReferenceId = coreReferenceId;
    this.refRefId = refRefId;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getRefLinkId  *//******************************** 
    *
    */ 
    public int getRefLinkId() 
    {
        return this.refLinkId;
    }
    
    
    /*  setRefLinkId  *//******************************** 
    *
    */
    public void setRefLinkId( int refLinkId ) 
    {
        this.refLinkId = refLinkId;
    }
    

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

    public int getCoreReferenceId()
    {
    return this.coreReferenceId;
    }

    public void setCoreReferenceId ( int coreReferenceId )
    {
        this.coreReferenceId = coreReferenceId;
    }

    public int getRefRefIId()
    {
     return this.refRefId;
    }

    public void setRefRefId ( int refRefId)
    {
    this.refRefId = refRefId;
    }

    public Contributor getContributor()
    {
    if (contributor == null)
        contributor = Contributor.getCurrentContributor();

    return this.contributor;
    }

    public void setContributor( Contributor c)
    {
    this.contributor = c;
    }
    
    public Date getDateEntered()
    {
    return dateEntered;
    }

    public static RefLink lookupStats ( int id )
    {
    Object i = getEntityManager()
           .getQuery("org.eurocarbdb.dataaccess.hplc.RefLink.STATS")
           .setParameter("glycanId", id)
           .uniqueResult();
           
    assert i instanceof RefLink;
    return (RefLink) i;
    }

    public RefLink storeOrLookup() throws Exception
    {
    logger.debug("storeOrLookup");
    EntityManager em = getEntityManager();
    em.store(this);
    return this;
    }





} // end class
