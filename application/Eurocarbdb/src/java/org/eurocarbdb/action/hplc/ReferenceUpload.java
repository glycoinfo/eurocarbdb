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

import java.util.*;
import java.lang.*;

import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.eurocarbdb.action.*;

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.*;
import org.eurocarbdb.dataaccess.core.*;

import org.eurocarbdb.dataaccess.hibernate.*;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;
import org.hibernate.*;
import org.hibernate.cfg.*;


//add glycan structures references

public class ReferenceUpload extends EurocarbAction implements RequiresLogin
{
    EntityManager em = Eurocarb.getEntityManager();
    
    protected static final Logger logger = Logger.getLogger (ReferenceUpload.class.getName());
    
    public String execute() throws Exception {
    
    List<Ref> allReferences = (List<Ref>) em.getQuery("org.eurocarbdb.dataaccess.hplc.Ref.SELECT_ALL").list();
    
    for (Ref r : allReferences) {
    
    Integer pubmed_id = r.getMedUi();
    String author = r.getAuthor();
    String title = r.getTitle();
    Short year = r.getPubYear();
    String journal_name = r.getJournal();
    Integer volume = r.getVolume();
    Integer startPage = r.getStartPage();
    Integer endPage = r.getStartPage();
    Short ogbi = r.getOgbiId();
                
    int pubyear = year.intValue();
    String pubmedId = pubmed_id.toString();   
    String comments = "Generated from GlycoBase entry (published data)";
    
    JournalReference jr = new JournalReference();
    logger.info("Pubmed Id" + pubmed_id);
    jr.setJournal(Journal.createOrLookup(journal_name));

    jr.setPubmedId(pubmed_id);
    jr.setAuthors(author);
    jr.setTitle(title);
    jr.setPublicationYear(pubyear);
    jr.setJournalVolume(volume);
    jr.setFirstPage(startPage);
    jr.setLastPage(endPage);
    jr.setContributor(Contributor.getCurrentContributor());
    jr.setReferenceComments(comments);
    logger.info("what the hell");
    jr.storeOrLookup();
    em.flush();
    }
    
    List<Glycan> allGlycans = (List<Glycan>) em.getQuery("org.eurocarbdb.dataaccess.hplc.Glycan.SELECT_ALL").list();
    for(Glycan g : allGlycans) {
      int glycanId = g.getGlycanId();
      
      List<RefLink> FindGlycanIdLink = (List<RefLink>) em.getQuery("org.eurocarbdb.dataaccess.hplc.RefLink.REFERENCE_IMPORT").setParameter("glycanId", glycanId).list();
      for (Iterator glycanRef = FindGlycanIdLink.iterator(); glycanRef.hasNext();) {
    Object [] referenceStore = (Object []) glycanRef.next();
    Integer pubmed_id = (Integer) referenceStore[5];
    String pubmedId = pubmed_id.toString();
    Integer ogbi = (Integer) referenceStore[9];
    Integer lookupRefId = (Integer) referenceStore[10];
    GlycanSequence glycanSeq =  em.lookup (GlycanSequence.class, ogbi);
    
    //jrSeq.setPubmedId(pubmed_id);
    //jrSeq.storeOrLookup();
    
        
    
    if (glycanSeq != null) {
      JournalReference jrSeq = JournalReference.createOrLookup(pubmed_id);
      int refId = jrSeq.getReferenceId();
      int glycanSeqId = glycanSeq.getGlycanSequenceId();
      logger.info("test here" + pubmed_id + "refid" + refId);
      logger.info("we are here");
      glycanSeq.addReference( jrSeq );
      logger.info("something here");
      int glycanRefId = jrSeq.getReferenceId();
      
      int numberUpdates = em.getQuery("org.eurocarbdb.dataaccess.hplc.RefLink.UPDATE_CORE_REFERENCE_ID")
                .setParameter("glycanId", glycanId)
                .setParameter("storedCoreRefId", glycanRefId)
                .setParameter("lookupRefId", lookupRefId)
                .executeUpdate();
      em.flush();
    }
      }
      
    }
    
    /*List<Glycan> allGlycans = (List<Glycan>) em.getQuery("org.eurocarbdb.dataaccess.hplc.Glycan.SELECT_ALL").list();
    for(Glycan g : allGlycans) {
    //for(Iterator glycanEntry = allGlycans.iterator(); glycanEntry.hasNext();) {
    //Object [] glycanStore = (Object []) glycanEntry.next();
    //Integer glycanId = (Integer) glycanStore[0];
    int glycanId = g.getGlycanId();
    
    List<RefLink> allReferences = (List<RefLink>) em.getQuery("org.eurocarbdb.dataaccess.hplc.RefLink.REFERENCE_IMPORT").setParameter("glycanId", glycanId).list();
        for (Iterator glycanRef = allReferences.iterator(); glycanRef.hasNext();) {
        Object [] referenceStore = (Object []) glycanRef.next();
        Integer pubmed_id = (Integer) referenceStore[5];
        String author = (String) referenceStore[0];
        String title = (String) referenceStore[1];
        Short year = (Short) referenceStore[3];
        String journal_name = (String) referenceStore[2];
        Integer volume = (Integer) referenceStore[4];
        Integer startPage = (Integer) referenceStore[6];
        Integer endPage = (Integer) referenceStore[7];
        Integer ogbi = (Integer) referenceStore[9];
        Integer glycanFound = (Integer) referenceStore[8];
        logger.info("whatthehell" + pubmed_id);
        int pubyear = year.intValue();
        String pubmedId = pubmed_id.toString();   
        String glycanIdConvert = glycanFound.toString();

        //String type = "database";
        String comments = "Generated from GlycoBase entry";
        //String refName = "GlycoBaseNIBRT";

        logger.info("lookingatglycanid" + glycanFound + "query by" + glycanId);
        logger.info("ogbi translation id" + ogbi);
        JournalReference jr = new JournalReference();
        logger.info("Pubmed Id" + pubmed_id);
        jr.setJournal(Journal.createOrLookup(journal_name));

        jr.setPubmedId(pubmed_id);
        jr.setAuthors(author);
        jr.setTitle(title);
        jr.setPublicationYear(pubyear);
        jr.setJournalVolume(volume);
        jr.setFirstPage(startPage);
        jr.setLastPage(endPage);
        jr.setContributor(Contributor.getCurrentContributor());
        jr.setReferenceComments(comments);

        //jr.setReferenceType(type);
        //jr.setExternalReferenceName(refName);
    
        jr.storeOrLookup();
        em.flush();

        int RefId = jr.getReferenceId();
        logger.info("the returned refId is:" + RefId);
                //Reference r = em.lookup (Reference.class, RefId);
        GlycanSequence glycanSeq =  em.lookup (GlycanSequence.class, ogbi);
        glycanSeq.addReference( jr ); 
    /*
        int RefId = jr.getReferenceId();
        logger.info("the returned refId is:" + RefId);
    
    //if (ogbi > 0 ) {
        //Reference r = em.lookup (Reference.class, RefId);
        GlycanSequence glycanSeq =  em.lookup (GlycanSequence.class, ogbi);
        glycanSeq.addReference( jr );
        //}
      */  
      
        //}
    
    return SUCCESS;       
    }
    } 
