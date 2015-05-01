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
// Generated Jun 21, 2007 2:07:02 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports
import java.util.HashSet;
import java.util.Set;
import java.io.Serializable;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;

import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Ref  *//**********************************************
*
*
*/ 
public class Ref extends BasicEurocarbObject implements Serializable 
{

   private static final Logger logger = Logger.getLogger( Ref.class );

    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int refId;
      
    private String author;
      
    private String title;
      
    private String journal;
      
    private String abstract_;
      
    private Short pubYear;
      
    private String pubDate;
      
    private Integer volume;
      
    private String issue;
      
    private String pages;
      
    private Integer medUi;
      
    private short ogbiId;

    private Integer startPage;

    private Integer endPage;

    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Ref() {}

    /** Minimal constructor */
    public Ref( short ogbiId ) 
    {
        this.ogbiId = ogbiId;
    }
    
    /** full constructor */
    public Ref( String author, String title, String journal, String abstract_, Short pubYear, String pubDate, Integer volume, String issue, String pages, Integer medUi, short ogbiId, Integer startPage, Integer endPage ) 
    {
        this.author = author;
        this.title = title;
        this.journal = journal;
        this.abstract_ = abstract_;
        this.pubYear = pubYear;
        this.pubDate = pubDate;
        this.volume = volume;
        this.issue = issue;
        this.pages = pages;
        this.medUi = medUi;
        this.ogbiId = ogbiId;
    this.startPage = startPage;
    this.endPage = endPage;
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
    

    /*  getAuthor  *//******************************** 
    *
    */ 
    public String getAuthor() 
    {
        return this.author;
    }
    
    
    /*  setAuthor  *//******************************** 
    *
    */
    public void setAuthor( String author ) 
    {
        this.author = author;
    }
    

    /*  getTitle  *//******************************** 
    *
    */ 
    public String getTitle() 
    {
        return this.title;
    }
    
    
    /*  setTitle  *//******************************** 
    *
    */
    public void setTitle( String title ) 
    {
        this.title = title;
    }
    

    /*  getJournal  *//******************************** 
    *
    */ 
    public String getJournal() 
    {
        return this.journal;
    }
    
    
    /*  setJournal  *//******************************** 
    *
    */
    public void setJournal( String journal ) 
    {
        this.journal = journal;
    }
    

    /*  getAbstract_  *//******************************** 
    *
    */ 
    public String getAbstract_() 
    {
        return this.abstract_;
    }
    
    
    /*  setAbstract_  *//******************************** 
    *
    */
    public void setAbstract_( String abstract_ ) 
    {
        this.abstract_ = abstract_;
    }
    

    /*  getPubYear  *//******************************** 
    *
    */ 
    public Short getPubYear() 
    {
        return this.pubYear;
    }
    
    
    /*  setPubYear  *//******************************** 
    *
    */
    public void setPubYear( Short pubYear ) 
    {
        this.pubYear = pubYear;
    }
    

    /*  getPubDate  *//******************************** 
    *
    */ 
    public String getPubDate() 
    {
        return this.pubDate;
    }
    
    
    /*  setPubDate  *//******************************** 
    *
    */
    public void setPubDate( String pubDate ) 
    {
        this.pubDate = pubDate;
    }
    

    /*  getVolume  *//******************************** 
    *
    */ 
    public Integer getVolume() 
    {
        return this.volume;
    }
    
    
    /*  setVolume  *//******************************** 
    *
    */
    public void setVolume( Integer volume ) 
    {
        this.volume = volume;
    }
    

    /*  getIssue  *//******************************** 
    *
    */ 
    public String getIssue() 
    {
        return this.issue;
    }
    
    
    /*  setIssue  *//******************************** 
    *
    */
    public void setIssue( String issue ) 
    {
        this.issue = issue;
    }
    

    /*  getPages  *//******************************** 
    *
    */ 
    public String getPages() 
    {
        return this.pages;
    }
    
    
    /*  setPages  *//******************************** 
    *
    */
    public void setPages( String pages ) 
    {
        this.pages = pages;
    }
    

    /*  getMedUi  *//******************************** 
    *
    */ 
    public Integer getMedUi() 
    {
        return this.medUi;
    }
    
    
    /*  setMedUi  *//******************************** 
    *
    */
    public void setMedUi( Integer medUi ) 
    {
        this.medUi = medUi;
    }
    

    /*  getOgbiId  *//******************************** 
    *
    */ 
    public short getOgbiId() 
    {
        return this.ogbiId;
    }
    
    
    /*  setOgbiId  *//******************************** 
    *
    */
    public void setOgbiId( short ogbiId ) 
    {
        this.ogbiId = ogbiId;
    }

    public Integer getStartPage()
    {
    return this.startPage;
    }

    public void setStartPage( Integer startPage )
    {
    this.startPage = startPage;
    }

    public Integer getEndPage()
    {
    return this.endPage;
    }

    public void setEndPage( Integer endPage )
    {
    this.endPage = endPage;
    }
    
    public static Ref lookupPubmedId ( int id)
    {
    Object i = getEntityManager()
           .getQuery("org.eurocarbdb.dataaccess.hplc.Ref.LOOKUP_PUBMEDID")
           .setParameter("pubmedId", id)
           .uniqueResult();

    assert i instanceof Ref;

    return (Ref) i;
   }

    

   public Ref storeOrLookup() throws Exception
   {
        logger.debug("storeOrLookup");
    Ref ref = lookupPubmedId ( this.medUi);
    if(ref!=null){
        throw new Exception("The HPLC database contains an existing reference");
    }
    EntityManager em = getEntityManager();
    em.store (this);
    return this;
   }





} // end class
