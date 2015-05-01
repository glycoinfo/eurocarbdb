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
import org.eurocarbdb.dataaccess.core.ref.*;

import org.eurocarbdb.dataaccess.hibernate.*;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;
import org.hibernate.*;
import org.hibernate.cfg.*;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

//script to add glycan structures references

public class refUpload extends EurocarbAction implements RequiresLogin
{
    EntityManager em = Eurocarb.getEntityManager();
    private JournalReference journalReference = null;
    private List<Reference> references = null;
    private Reference reference = null;
    Reference tosave = null;

    protected static final Logger logger = Logger.getLogger (refUpload.class.getName());
    
    public String execute() throws Exception {


        /*List<RefLink> refList;
        refList = em.getQuery("org.eurocarbdb.dataaccess.hplc.RefLink.SELECT_GROUP_ID").list();

        //int value = em.getQuery("org.eurocarbdb.dataaccess.hplc.RefLink.SELECT_GROUP_ID").list();

        //for (RefLink reference : refList) {
        for (Iterator iterRef = refList.iterator(); iterRef.hasNext();) {
        Object [] refObject = (Object []) iterRef.next();
        Integer refGlycanId = (Integer) refObject[0];
        //int glycanLookup = refs.getGlycanId();
        logger.info("processing glycan:" + refGlycanId);
*/
        int refGlycanIdtest = 1211;
        List<Ref> refInfo = em.getQuery("org.eurocarbdb.dataaccess.hplc.Ref.SELECT_REFERENCES_CORE").list();

            for (Iterator iterRefDetails = refInfo.iterator(); iterRefDetails.hasNext();) {
            Object [] detailsObject = (Object []) iterRefDetails.next();
            String author = (String) detailsObject[0]; 
            String title = (String) detailsObject[1];
            String journal_name = (String) detailsObject[2];
            //String volume = (String) detailsObject[3];
            Integer vol = (Integer) detailsObject[3];
            Short year = (Short) detailsObject[4];
            Integer pubmed_id = (Integer) detailsObject[5];
            Integer startPage = (Integer) detailsObject[6];
            Integer endPage = (Integer) detailsObject[7];
            logger.info("error check" + vol + "title" + title + "author" + author);
            //need to convert volume to int
            //int vol = Integer.parseInt(volume);
            //need to convert year 
            int pubyear = year.intValue();
            String pubmedId = pubmed_id.toString();

            String type = "journal";
                        String comments = "Generated from GlycoBase entry";
                        String refName = "glycobase(dublin)";
            

            JournalReference jr = new JournalReference();
        /*    Reference newr = new Reference();
            newr.setReferenceType(type);
            newr.setExternalReferenceName(refName);
            newr.setExternalReferenceId(pubmedId);
            newr.storeOrLookup();
*/
            logger.info("Pubmed Id" + pubmed_id);
            jr.setJournal(Journal.createOrLookup(journal_name));

            jr.setPubmedId(pubmed_id);
            jr.setAuthors(author);
            jr.setTitle(title);
            jr.setPublicationYear(pubyear);
            jr.setJournalVolume(vol);
            jr.setFirstPage(startPage);
            jr.setLastPage(endPage);
            jr.setContributor(Contributor.getCurrentContributor());
            jr.setReferenceComments(comments);
            
            //jr.setReferenceType(type);
            jr.setExternalReferenceName(refName);
            //jr.setExternalReferenceId(pubmedId);
            //Eurocarb.getEntityManager().store(jr);
                jr.storeOrLookup();    
            }
    
        logger.info("successfully added all glycobase references. Now time for Seq-Ref associations");
        //now need to associated each hplc strcture with a reference in the core schema
/*
        List<Glycan> allGlycans = em.getQuery("org.eurocarbdb.dataaccess.hplc.Glycan.SELECT_ALL").list();
        for (Iterator glycans = allGlycans.iterator(); glycans.hasNext();) {
        Object [] glycanStore = (Object []) glycans.next();
        Integer glycanId = (Integer) glycanStore[0]; 
*/

//test
//        
            logger.info("testingareahere");

            logger.info("starting test from here");
            int glycanId = 159;
            logger.info("looking up" + glycanId);
            List<RefLink> allReferences = em.getQuery("org.eurocarbdb.dataaccess.hplc.RefLink.IMPORT_REFERENCE_GWS").setParameter("glycanId", glycanId).list();
            for (Iterator glycanRef = allReferences.iterator(); glycanRef.hasNext();) {
            Object [] referenceStore = (Object []) glycanRef.next();
            Integer lookupPubmed = (Integer) referenceStore[0];
            Integer lookupTranslationId = (Integer) referenceStore[1];
            Integer lookupRefId = (Integer) referenceStore[3];
            logger.info("ogbi translation id" + lookupTranslationId);        
            GlycanSequence glycanSeq =  em.lookup (GlycanSequence.class, lookupTranslationId);
                if (glycanSeq != null) {
                logger.info("finding gws with pubmed" + lookupPubmed);
                JournalReference lookupjr = JournalReference.createOrLookup(lookupPubmed);
                // test
                logger.info("check variables" + lookupPubmed + "trans" + lookupTranslationId + "ref" + lookupRefId);
                int storedCoreRefId = lookupjr.getReferenceId();
                logger.info("the reference id dealing with is:" + storedCoreRefId);
                int numberUpdates = em.getQuery("org.eurocarbdb.dataaccess.hplc.RefLink.UPDATE_CORE_REFERENCE_ID")
                .setParameter("glycanId", glycanId)
                .setParameter("storedCoreRefId", storedCoreRefId)
                .setParameter("lookupRefId", lookupRefId)
                .executeUpdate();
                // end test
                glycanSeq.addReference(lookupjr); 
            /*    getEntityManager().update(lookupjr);
            */    }
            } 
    //    }


/*        logger.info("am i testing");
        //just try getting a reference for a glycan
        int glycanTest = 159;
        GlycanSequence glycanSeqTest =  em.lookup (GlycanSequence.class, glycanTest);
        logger.info("get references for glycan");
        glycanSeqTest.getReferences();
        int sequence_id = glycanSeqTest.getGlycanSequenceId();
        logger.info("i am indeed and the id is:" + sequence_id);
        List<GlycanSequence> glycanReferences = em.getQuery("org.eurocarbdb.dataaccess.core.GlycanSequence.HPLC_REFERENCE").setParameter("sequence_id", sequence_id).list();
        //glycanSeqTest.getReferences();
*/
        return SUCCESS;        
        }}

    
    



