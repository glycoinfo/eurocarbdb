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
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/

package org.eurocarbdb.action.hplc;

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.hplc.*;
import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;


import org.apache.log4j.Logger;

import java.util.*;
import java.lang.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


public class CreateGu extends EurocarbAction implements RequiresLogin {
  
  protected static final Logger logger = Logger.getLogger( CreateGu.class.getName() );
  
  private GlycanSequence glycan = null;
  private Reference reference = null;
  private Reference displayRefs;
  private JournalReference jr = null;
  private Glycan glycanHplc = null;
  private List<RefLink> stats = null;
  private List<RefLink> displayStats;
  private Glycan glycanAll = null;
  private Ref storeRef = null;

  private int searchGlycanId;
  private int searchReferenceId;
  private int glycanHplcId;
  private int searchJournalReferenceId;
  private double storeGu;
  private int ogbiId;

  public GlycanSequence getGlycan() {  return glycan;  }

  public GlycanSequence getGlycanSequence() {  return glycan;  }

  public int getGlycanSequenceId() {  return searchGlycanId;  }

  public void setGlycanSequenceId( int search_id ) {  searchGlycanId = search_id;  }
  
  public int getGlycanReferenceId() { return searchReferenceId; }
  
  public void setGlycanReferenceId( int search_ref_id ) { searchReferenceId = search_ref_id;}
  
  public void setGlycanJournalReferenceId ( int search_journal_id ) { searchJournalReferenceId = search_journal_id;}
  
  public Reference getReference() {return reference;}
  
  public List getRefLink() {return stats;}
  
  public List getDisplayStats() {return displayStats;}
  
  public Glycan getGlycanAll() {return glycanAll;}
  
  public Double getGuValue() { return storeGu; }
  
  public void setGuValue( double store_gu) {storeGu = store_gu;}
  
  public int getGlycanHplcId() { return glycanHplcId; }
  
  public void setGlycanHplcId( int glycan_hplc_id ) { glycanHplcId = glycan_hplc_id;}

  

  public String execute() throws Exception {
  
    EntityManager em = getEntityManager();
    
   if( submitAction!=null && submitAction.equals("Add") ){
     logger.info("check this query");
     reference = getEntityManager().lookup( Reference.class, searchReferenceId);
     logger.info("below we have a prob");
     //jr = getEntityManager().lookup( JournalReference.class, searchJournalReferenceId);
     jr = getEntityManager().lookup( JournalReference.class, searchReferenceId);
     String author = jr.getAuthors();
     String title = jr.getTitle();
     Journal journal = jr.getJournal();
     int year = jr.getPublicationYear();
     int pubmed = jr.getPubmedId();
     int first = jr.getFirstPage();
     int last = jr.getLastPage();
     
     String jname = journal.getJournalTitle();
     logger.info("pubmed:" + pubmed);
     Ref hplcRef = Ref.lookupPubmedId(pubmed);
     
     //if hplcRef null means i dont have the reference stored
     if ( hplcRef == null) {
       //convert pubyear to short
       //String test = "123";
       String stringYear = Integer.toString(year);
       short sYear = Short.parseShort(stringYear);
       storeRef.setAuthor(author);
       storeRef.setTitle(title);
       storeRef.setJournal(jname);
       storeRef.setPubYear(sYear);
       storeRef.setMedUi(pubmed);
       storeRef.setStartPage(first);
       storeRef.setEndPage(last);
       //storeRef.storeOrLookup();
      // em.store(storeRef);
     // int 
     }
     
     if (hplcRef !=null) {
       logger.info("i am here with id:" + glycanHplcId);
       int ogbi = hplcRef.getOgbiId();
       int reference_id = hplcRef.getRefId();
       int coreReferenceId = searchReferenceId;
       //need glycan id
       RefLink storeValue = new RefLink();
       
       //test area
      /* int gid = 1;
       storeValue.setGlycanId(gid);
       int o = 1;
       storeValue.setRefId(o);
       int r =1;
       storeValue.setRefRefId(r);
       int c =1;
       double pap = 1;
       
       storeValue.setCoreReferenceId(c);
       storeValue.setPaperGu(pap);
       */
       storeValue.setGlycanId(glycanHplcId);
       storeValue.setRefId(ogbi);
       storeValue.setRefRefId(reference_id);
       storeValue.setCoreReferenceId(coreReferenceId);
       storeValue.setContributor(Contributor.getCurrentContributor()); 
       storeValue.setPaperGu(storeGu);
       //storeValue.storeOrLookup();
       em.store(storeValue);
       //em.flush();
       
     }
    
     
 
     return "success";
  } 
 // if( submitAction!.equals("Add GU") 
   logger.info("i am here"); 
  glycan = getEntityManager().lookup( GlycanSequence.class, searchGlycanId );
  log.info("second query here what is the reference id" + searchReferenceId);
  reference = getEntityManager().lookup( Reference.class, searchReferenceId); 
  
  glycanHplc = Glycan.lookupByGWS(searchGlycanId);
  //for (Glycan g: glycanHplc) {
  int glycanId = glycanHplc.getGlycanId();
  logger.info("the glycan id is:" + glycanId);
  
  stats = getEntityManager()
                   .getQuery("org.eurocarbdb.dataaccess.hplc.RefLink.STATS")
                   .setParameter("glycanId", glycanId)
           .list();
                   //.uniqueResult();
           
  displayStats = stats;
  
  glycanAll = Glycan.lookupAllById(glycanId);

  //stats = RefLink.lookupStats(glycanId);
  return "input";
  
  
  
  
  }

    }      


