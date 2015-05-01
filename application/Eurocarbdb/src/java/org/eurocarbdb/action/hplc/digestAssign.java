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

import java.io.*;
import java.util.*;
import java.lang.*;
import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;


import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.hplc.*;


import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;
import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.criterion.*;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

public class digestAssign extends EurocarbAction  {

     //SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
 //Session session =sessionFactory.openSession();

    String a1s = "(G.a1 = 1 AND G.s = 1)";
        String a1f = "(G.a1 = 1 AND G.f6 =1)";
    String a1b = "(G.a1 = 1 AND G.b = 1)";
    String a1bgal = "(G.a1 = 1 AND G.bgal = 1)";
    String a1agal = "(G.a1 = 1 AND G.agal = 1)";
    String a1galnac = "(G.a1 = 1 AND G.galnac = 1)";
    String a1polylac = "(G.a1 = 1 AND G.polylac = 1)"; 
    String a1fouterarm = "(G.a1 = 1 AND G.fouterarm = 1)";
    String a1hybrid = "(G.a1 = 1 AND G.hybrid = 1)";
    String a1mannose = "(G.a1 = 1 AND G.mannose = 1)";
    
    short classA1;
    String A1S;
    short assignA1S;
    short assigna1s = 1;
    String A1F;
    short assignA1F;
    short assigna1f =1;
    String A1B;
    short assignA1B;
    short assigna1b =1;
    String A1BGAL;
    short assignA1BGAL;
    short assigna1bgal;
    String A1AGAL;
    short assignA1AGAL;
    short assigna1agal =1;
    String A1GALNAC;
    short assignA1GALNAC;
    short assigna1galnac =1;
    String A1POLYLAC;
    short assignA1POLYLAC;
    short assigna1polylac =1;
    String A1FOUTERARM;
    short assignA1FOUTERARM;
    short assigna1fouterarm =1;
    String A1HYBRID;
    short assignA1HYBRID;
    short assigna1hybrid =1;
    String A1MANNOSE;
    short assignA1MANNOSE;
    short assigna1mannose =1;
    
    String a2s = "(G.a2 = 1 AND G.s = 1)";
    String a2f = "(G.a2 = 1 AND G.f6 =1)";
    String a2b = "(G.a2 = 1 AND G.b = 1)";
    String a2bgal = "(G.a2 = 1 AND G.bgal = 1)";
    String a2agal = "(G.a2 = 1 AND G.agal = 1)";
    String a2galnac = "(G.a2 = 1 AND G.galnac = 1)";
    String a2polylac = "(G.a2 = 1 AND G.polylac = 1)"; 
    String a2fouterarm = "(G.a2 = 1 AND G.fouterarm = 1)";
    String a2hybrid = "(G.a2 = 1 AND G.hybrid = 1)";
    String a2mannose = "(G.a2 = 1 AND G.mannose = 1)";
    
    short classA2;
    String A2S;
    short assignA2S;
    short assigna2s = 1;
    String A2F;
    short assignA2F;
    short assigna2f =1;
    String A2B;
    short assignA2B;
    short assigna2b =1;
    String A2BGAL;
    short assignA2BGAL;
    short assigna2bgal;
    String A2AGAL;
    short assignA2AGAL;
    short assigna2agal =1;
    String A2GALNAC;
    short assignA2GALNAC;
    short assigna2galnac =1;
    String A2POLYLAC;
    short assignA2POLYLAC;
    short assigna2polylac =1;
    String A2FOUTERARM;
    short assignA2FOUTERARM;
    short assigna2fouterarm =1;
    String A2HYBRID;
    short assignA2HYBRID;
    short assigna2hybrid =1;
    String A2MANNOSE;
    short assignA2MANNOSE;
    short assigna2mannose =1;
    
    
    String a3s = "(G.a3 = 1 AND G.s = 1)";
    String a3f = "(G.a3 = 1 AND G.f6 =1)";
    String a3b = "(G.a3 = 1 AND G.b = 1)";
    String a3bgal = "(G.a3 = 1 AND G.bgal = 1)";
    String a3agal = "(G.a3 = 1 AND G.agal = 1)";
    String a3galnac = "(G.a3 = 1 AND G.galnac = 1)";
    String a3polylac = "(G.a3 = 1 AND G.polylac = 1)"; 
    String a3fouterarm = "(G.a3 = 1 AND G.fouterarm = 1)";
    String a3hybrid = "(G.a3 =1 AND G.hybrid = 1)";
    String a3mannose = "(G.a3 =1 AND G.mannose = 1)";
    

    short classA3;
    String A3S;
    short assignA3S;
    short assigna3s = 1;
    String A3F;
    short assignA3F;
    short assigna3f =1;
    String A3B;
    short assignA3B;
    short assigna3b =1;
    String A3BGAL;
    short assignA3BGAL;
    short assigna3bgal;
    String A3AGAL;
    short assignA3AGAL;
    short assigna3agal =1;
    String A3GALNAC;
    short assignA3GALNAC;
    short assigna3galnac =1;
    String A3POLYLAC;
    short assignA3POLYLAC;
    short assigna3polylac =1;
    String A3FOUTERARM;
    short assignA3FOUTERARM;
    short assigna3fouterarm =1;
    String A3HYBRID;
    short assignA3HYBRID;
    short assigna3hybrid =1;
    String A3MANNOSE;
    short assignA3MANNOSE;
    short assigna3mannose =1;;
    
    String a4s = "(G.a4 = 1 AND G.s = 1)";
    String a4f = "(G.a4 = 1 AND G.f6 =1)";
    String a4b = "(G.a4 = 1 AND G.b = 1)";
    String a4bgal = "(G.a4 = 1 AND G.bgal = 1)";
    String a4agal = "(G.a4 = 1 AND G.agal = 1)";
    String a4galnac = "(G.a4 = 1 AND G.galnac = 1)";
    String a4polylac = "(G.a4 = 1 AND G.polylac = 1)"; 
    String a4fouterarm = "(G.a4 = 1 AND G.fouterarm = 1)";
    String a4hybrid = "(G.a4 =1 AND G.hybrid = 1)";
    String a4mannose = "(G.a4 =1 AND G.mannose = 1)";
    
    short classA4;
    String A4S;
    short assignA4S;
    short assigna4s = 1;
    String A4F;
    short assignA4F;
    short assigna4f =1;
    String A4B;
    short assignA4B;
    short assigna4b =1;
    String A4BGAL;
    short assignA4BGAL;
    short assigna4bgal;
    String A4AGAL;
    short assignA4AGAL;
    short assigna4agal =1;
    String A4GALNAC;
    short assignA4GALNAC;
    short assigna4galnac =1;
    String A4POLYLAC;
    short assignA4POLYLAC;
    short assigna4polylac =1;
    String A4FOUTERARM;
    short assignA4FOUTERARM;
    short assigna4fouterarm =1;
    String A4HYBRID;
    short assignA4HYBRID;
    short assigna4hybrid =1;
    String A4MANNOSE;
    short assignA4MANNOSE;
    short assigna4mannose =1;

    private Profile parent = null;
    private Instrument instrument = null;
    private DigestProfile digestprofile;
 
    private int instrument_id;
    private int profile_id;
    private int digest_id;
    private String enzyme;
    short classType = 1;
    private String refineAssignment;
    short replaceSearch = 100;
    private int delete_entry;
    private int criteriaResults;

    protected static final Logger logger = Logger.getLogger ( preAssign.class.getName());
  
    private List<Glycan> preliminary; // = new List();
    private List<HplcPeaksAnnotated> display;
    private List<DigestProfile> preliminaryenz;
    private HplcPeaksAnnotated peaksannotated;
    private List<HplcPeaksAnnotated> displayDigest;
    private List<HplcPeaksAnnotated> showCriteria;
    private int refineDigestId;    


    public String execute() throws Exception {

    refineDigestId = digest_id;


    if (classA1 == 0) {
        classA1 = replaceSearch;
    }

    if (classA2 == 0) {
        classA2 = replaceSearch;
    }

    if (classA3 == 0) {
        classA3 = replaceSearch;
    }

    if (classA4 == 0) {
        classA4 = replaceSearch;
    }

    if( refineAssignment!=null ) {
    SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
     Session session =sessionFactory.openSession();
    logger.info("user whats a refinement displayed");
    logger.info("confirm digest id for criteria" + refineDigestId);

    Criteria criteria = session.createCriteria(HplcPeaksAnnotated.class);
    Disjunction disjunction = Restrictions.disjunction();
    ProjectionList proList = Projections.projectionList();
    criteria.add(Expression.eq("profileId", profile_id));
    criteria.add(Expression.eq("digestId", refineDigestId));
    criteria.createAlias("glycan", "G");
    proList.add(Projections.property("nameAbbreviation"));
    proList.add(Projections.property("gu"));
    proList.add(Projections.property("dbGu"));
    proList.add(Projections.property("peakArea"));
    proList.add(Projections.property("G.ogbitranslation"));
    proList.add(Projections.property("hplcPeaksAnnotatedId"));
    
    criteria.setProjection(proList);

    if(assignA3S==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a3", classA3),
                    Expression.eq("G.s", assignA3S)
                    ));
    }
    if(assignA3F ==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a3", classA3),
                    Expression.eq("G.f6", assignA3F)
                    ));
    logger.info("assigned a3 f");
    }
    
    if(assignA3FOUTERARM ==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a3", classA3),
                    Expression.eq("G.fouterarm", assignA3FOUTERARM)
                    ));
    }
    
    if(assignA3B ==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a3", classA3),
                    Expression.eq("G.b", assignA3B)
                    ));
    logger.info("assigned a3 b");
    }
    
    if(assignA3BGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a3", classA3),
                    Expression.eq("G.bgal", assignA3BGAL)
                    ));
    }
    
    if(assignA3AGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a3", classA3),
                    Expression.eq("G.agal", assignA3AGAL)
                    ));
    }
    if(assignA3GALNAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a3", classA3),
                    Expression.eq("G.galnac", assignA3GALNAC)
                    ));
    }
    
    if(assignA3POLYLAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a3", classA3),
                    Expression.eq("G.polylac", assignA3POLYLAC)
                    ));
    }
    if(assignA3HYBRID==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a3", classA3),
                    Expression.eq("G.hybrid", assignA3HYBRID)
                    ));
    }
    if(assignA3MANNOSE==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a3", classA3),
                    Expression.eq("G.mannose", assignA3MANNOSE)
                    ));
    }
    if(assignA2S==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a2", classA2),
                    Expression.eq("G.s", assignA2S)
                    ));
    }
    if(assignA2F==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a2", classA2),
                    Expression.eq("G.f6", assignA2F)
                    ));
    }
    if(assignA2FOUTERARM==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a2", classA2),
                    Expression.eq("G.fouterarm", assignA2FOUTERARM)
                    ));
    }
    if(assignA2B==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a2", classA2),
                    Expression.eq("G.b", assignA2B)
                    ));
    }
    if(assignA2BGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a2", classA2),
                    Expression.eq("G.bgal", assignA2BGAL)
                    ));
    }
    if(assignA2AGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a2", classA2),
                    Expression.eq("G.agal", assignA2AGAL)
                    ));
    }
    if(assignA2GALNAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a2", classA2),
                    Expression.eq("G.galnac", assignA2GALNAC)
                    ));
    }
    if(assignA2POLYLAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a2", classA2),
                    Expression.eq("G.polylac", assignA2POLYLAC)
                    ));
    }
    if(assignA2HYBRID==1){
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a2", classA2),
                    Expression.eq("G.hybrid", assignA2HYBRID)
                    ));
    }
    if(assignA2MANNOSE==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a2", classA2),
                    Expression.eq("G.mannose", assignA2MANNOSE)
                    ));
    }
    
    if(assignA1S==1){
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a1", classA1),
                    Expression.eq("G.s", assignA1S)
                    ));
    }
    if(assignA1F==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a1", classA1),
                    Expression.eq("G.f6", assignA1F)
                    ));
    }
    if(assignA1FOUTERARM==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a1", classA1),
                    Expression.eq("G.fouterarm", assignA1FOUTERARM)
                    ));
    }
    if(assignA1B==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a1", classA1),
                    Expression.eq("G.b", assignA1B)
                    ));
    }
    if(assignA1BGAL==1){
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a1", classA1),
                    Expression.eq("G.bgal", assignA1BGAL)
                    ));
    }
    if(assignA1AGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a1", classA1),
                    Expression.eq("G.agal", assignA1AGAL)
                    ));
    }
    if(assignA1GALNAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a1", classA1),
                    Expression.eq("G.galnac", assignA1GALNAC)
                    ));
    }
    if(assignA1POLYLAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a1", classA1),
                    Expression.eq("G.polylac", assignA1POLYLAC)
                    ));
    }
    if(assignA1HYBRID==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a1", classA1),
                    Expression.eq("G.hybrid", assignA1HYBRID)
                    ));
    }
    if(assignA1MANNOSE==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a1", classA1),
                    Expression.eq("G.mannose", assignA1MANNOSE)
                    ));
    }
    if(assignA4S==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a4", classA4),
                    Expression.eq("G.s", assignA4S)
                    ));
    }
    if(assignA4F==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a4", classA4),
                    Expression.eq("G.f6", assignA4F)
                    ));
    }
    if(assignA4FOUTERARM==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a4", classA4),
                    Expression.eq("G.fouterarm", assignA4FOUTERARM)
                    ));
    }
    if(assignA4B==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a4", classA4),
                    Expression.eq("G.b", assignA4B)
                    ));
    }
    if(assignA4BGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a4", classA4),
                    Expression.eq("G.bgal", assignA4BGAL)
                    ));
    }
    if(assignA4AGAL==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a4", classA4),
                    Expression.eq("G.agal", assignA4AGAL)
                    ));
    }
    if(assignA4GALNAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a4", classA4),
                    Expression.eq("G.galnac", assignA4GALNAC)
                    ));
    }
    if(assignA4POLYLAC==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a4", classA4),
                    Expression.eq("G.polylac", assignA4POLYLAC)
                    ));
    }
    if(assignA4HYBRID==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a4", classA4),
                    Expression.eq("G.hybrid", assignA4HYBRID)
                    ));
    }
    if(assignA4MANNOSE==1) {
    disjunction.add(Restrictions.and(
                    Expression.eq("G.a4", classA4),
                    Expression.eq("G.mannose", assignA4MANNOSE)
                    ));
    }

    criteria.add(disjunction);

    List displayCriteria = criteria.list();

               
    showCriteria = displayCriteria;
        
    int criteriaSelection = showCriteria.size();
    logger.info("lets lookup at refinement" + criteriaSelection);

    //What if the refinement lists nothing
    if (criteriaSelection <= 0) {
        logger.info("selection criteria generated no results");
        criteriaResults = 0;
        List display = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.HplcPeaksAnnotated.DIGEST_DISPLAY")
        .setParameter("parent", profile_id)
        .setParameter("digest", digest_id)
        .list();


        displayDigest = display;
    }
    
    
    Iterator iterCrit = showCriteria.iterator();
              while (iterCrit.hasNext()) {
        Object [] tempcrit = (Object[])iterCrit.next();
        String namecrit = (String) tempcrit[0];
        
                 }

    }


    if( refineAssignment==null && delete_entry >0) {
    logger.info("record for deleting" + delete_entry);
    HplcPeaksAnnotated deletedRecord = HplcPeaksAnnotated.deleteById(delete_entry);
    List  afterDelete = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.HplcPeaksAnnotated.DIGEST_ASSIGN_DISPLAY")
        .setParameter("parent", profile_id)
        .setParameter("digest", refineDigestId)
        .list();
     displayDigest = afterDelete;
    }






    if( refineAssignment == null && delete_entry == 0)  {

    File file = new File ("/tmp/digest" + profile_id + digest_id + ".txt");
    //Session s = HibernateUtil.getSession();
    //Transaction tx = s.beginTransaction();

     EntityManager em = getEntityManager();
     ArrayList<Double> arrayGu = new ArrayList<Double>(); 
        

        FileReader input = new FileReader(file);
            
    BufferedReader bufRead = new BufferedReader(input);
            
        String line;     // String that holds current file line
        int count = 0;    // Line number of count 

            // Read first line
        line = bufRead.readLine();

        while (line != null){

        String rec = line;
                String [] gu = rec.split("\t");
                double gu_c = Double.parseDouble(gu[1]); 
                arrayGu.add(gu_c);
        double area = Double.parseDouble(gu[0]);
                line = bufRead.readLine();
        
          // for (Double gu_value : arrayGu) {

        // logger.info("print gu value" + gu_c);
               List list = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Glycan.PRELIM_ASSIGN") .setParameter( "gu_value", gu_c) .list();

               preliminary = list;
               logger.info("sizeofprel" + preliminary.size());

              Iterator iter = preliminary.iterator();
              while (iter.hasNext()) {
        
                Glycan temp = (Glycan) iter.next();
        //logger.info("print contents" + temp);
                peaksannotated = new HplcPeaksAnnotated();
                //peaksannotated.setGlycan(temp);
                peaksannotated.setPeakArea(area);
                peaksannotated.setGu(gu_c);
                peaksannotated.setDbGu(temp.getGu());
                peaksannotated.setProfileId(profile_id);
        peaksannotated.setDigestId(digest_id);
                //peaksannotated.setGlycanId(temp.getGlycanId());
        //modification to support relationship change
                peaksannotated.setGlycan(temp);
        peaksannotated.setNameAbbreviation(temp.getName());
                //getEntityManager().store(peaksannotated);
        //peaksannotated.setContributor(Contributor.getCurrentContributor());
            //peaksannotated.setTechnique(Technique.lookupAbbrev("hplc"));
        getEntityManager().store(peaksannotated);
        logger.info("check storage process");
              }  //close the iteration over the query result 


              }  // end gu value loop

                bufRead.close(); //close the gu file
        
        Profile p = Profile.lookupById(profile_id);
        logger.info("themagic is" + p);
        DigestProfile dp = new DigestProfile();
            dp.setDigestId(digest_id);
        dp.setSequentialDigest(enzyme);
        dp.setProfile(p);
        getEntityManager().store(dp);

        int idTest = p.getProfileId();
        logger.info("the value of idTest:" + idTest);
        //note: due to use of evidenceId relationships the profileId in DIgestProfile is the evidenceId
        //to get the corresponfing profileId from  Profile use the getProfileId()

        

        //get the enzymes used
                 logger.info ("check profile id" + profile_id);
                 logger.info ("check digest id" + digest_id);

         List enzymelist = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DigestProfile.GET_ENZYMES") //.setParameter("parent", parent) .list(); // .setParameter("digest_id", digest_id) .list();
         //recurring problem with the use of evidence id
         //if use evidence id instead of profile id for HplcPeaks... will cause core probs
         .setParameter("parent", p).list();
                 logger.info ("enzymelist" + enzymelist);

//loop over the enzymes


               preliminaryenz = enzymelist;
               logger.info("size report" + preliminaryenz.size());



//delete records which have a digest product for that given enzyme

        for (Object enzymeused : enzymelist) {
        logger.info("loopstufftoheckenz" + enzymeused);
        List annolist = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.HplcPeaksAnnotated.ATTEMPT") 
        .setParameter("parent", profile_id) 
        .setParameter("digest", digest_id)
        .setParameter("enzymeused", enzymeused)
        .list();

        logger.info("what now");

        Iterator iteraa = annolist.iterator();
              while (iteraa.hasNext()) {
              HplcPeaksAnnotated tempanno = (HplcPeaksAnnotated) iteraa.next();
        logger.info ("whatshouldberemoved" + tempanno);
          //tx.begin();
          //s.delete( tempanno );
              Eurocarb.getEntityManager().remove( tempanno);
          //tx.commit();
               }


        }


        //preview the results
        List display = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.HplcPeaksAnnotated.DIGEST_DISPLAY")
        .setParameter("parent", profile_id)
        .setParameter("digest", digest_id)
        .list();


        displayDigest = display;
        logger.info(displayDigest.size());



                   digest_id++;

        }
              return SUCCESS;
} 

 
//getter and setters

    public List<HplcPeaksAnnotated> getQuery() {
        return displayDigest;
    }

   public void setQuery( List<HplcPeaksAnnotated> displayDigest) {
       this.displayDigest = displayDigest;
    }

    public List getDisplayDigest()
    {
        return this.displayDigest;
    }


     public List getDisplay()
    {
        return this.display;
    }
/*
  public List<Glycan> getQuery() {
        return preliminary;
    }

   public void setQuery( List<Glycan> preliminary) {
       this.preliminary = preliminary;
    }

    public List getPreliminary()
    {
        return this.preliminary;
    }
*/


    public Profile getProfile() {
        return parent;
    }

    public void setProfile (Profile parent) {
        this.parent = parent;
    }


    public void setProfileId(int id) {
        this.profile_id = id;
    }

    public int getProfileId() {
        return this.profile_id;
    }


   public void setDigestId(int id) {
        this.digest_id = id;
    this.refineDigestId = digest_id - 1;
    }

    public int getDigestId() {
        return this.digest_id;
    }

    public List getShowCriteria()
    {
        return this.showCriteria;
    }


    public int getDeleteEntry() {
        return this.delete_entry;
    }

    public void setDeleteEntry(int deleteId){
        this.delete_entry = deleteId;
    }

    

  public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument (Instrument instrument) {
        this.instrument = instrument;
    }



   public void setInstrumentId(int id) {
        this.instrument_id = id;
    }

   public int getInstrumentId() {
        return this.instrument_id;
    }

  public void setSequentialDigest(String enzymeused) {
        this.enzyme = enzymeused;
   }

   public String getSequentialDigest() {
        return this.enzyme;
   }


    public String getRefineAssignment() { 
        return this.refineAssignment;
    }
       
    public void setRefineAssignment( String refine) {
        this.refineAssignment = refine;
    }

    public int getRefineDigestId() {
        return this.refineDigestId;
    }
    
    public void setRefineDigestId ( int digest_id) {
        this.refineDigestId = digest_id;
    }

    public void setCriteriaResults ( int criteriaResults) {
        this.criteriaResults = criteriaResults;
    }
    
    public int getCriteriaResults() {
        return this.criteriaResults;
    }


    public void setA1s(String tempa1s) {
        this.A1S = a1s;
        this. assignA1S = assigna1s;
        this.classA1 = classType;
    }
    
    public void setA1f(String tempa1f) {
        this.A1F = a1f;
        this. assignA1F = assigna1f;
        this.classA1 = classType;
    }
    
    public void setA1b(String tempa1b) {
        this.A1B = a1b;
        this. assignA1B = assigna1b;
        this.classA1 = classType;
    }
    
    public void setA1bgal(String tempa1bgal) {
        this.A1BGAL = a1bgal;
        this. assignA1BGAL = assigna1bgal;
        this.classA1 = classType;
    }
    
    public void setA1agal(String tempa1agal) {
        this.A1AGAL = a1agal;
        this. assignA1AGAL = assigna1agal;
        this.classA1 = classType;
    }
    
    public void setA1galnac(String tempa1galnac) {
        this.A1GALNAC = a1galnac;
        this. assignA1GALNAC = assigna1galnac;
        this.classA1 = classType;
    }
    
    public void setA1polylac(String tempa1polylac) {
        this.A1POLYLAC = a1polylac;
        this. assignA1POLYLAC = assigna1polylac;
        this.classA1 = classType;
    }
    
    public void setA1fouterarm(String tempa1fouterarm) {
        this.A1FOUTERARM = a1fouterarm;
        this. assignA1FOUTERARM = assigna1fouterarm;
        this.classA1 = classType;
    }
    
    public void setA1hybrid(String tempa1hybrid) {
        this.A1HYBRID = a1hybrid;
        this. assignA1HYBRID = assigna1hybrid;
        this.classA1 = classType;
    }
    
    public void setA1mannose(String tempa1mannose) {
        this.A1MANNOSE = a1mannose;
        this. assignA1MANNOSE = assigna1mannose;
        this.classA1 = classType;
    }
    
    
    public void setA2s(String tempa2s) {
        this.A2S = a2s;
        this. assignA2S = assigna2s;
        this.classA2 = classType;
    }
    
    public void setA2f(String tempa2f) {
        this.A2F = a2f;
        this. assignA2F = assigna2f;
        this.classA2 = classType;
    }
    
    public void setA2b(String tempa2b) {
        this.A2B = a2b;
        this. assignA2B = assigna2b;
        this.classA2 = classType;
    }
    
    public void setA2bgal(String tempa2bgal) {
        this.A2BGAL = a2bgal;
        this. assignA2BGAL = assigna2bgal;
        this.classA2 = classType;
    }
    
    public void setA2agal(String tempa2agal) {
        this.A2AGAL = a2agal;
        this. assignA2AGAL = assigna2agal;
        this.classA2 = classType;
    }
    
    public void setA2galnac(String tempa2galnac) {
        this.A2GALNAC = a2galnac;
        this. assignA2GALNAC = assigna2galnac;
        this.classA2 = classType;
    }
    
    public void setA2polylac(String tempa2polylac) {
        this.A2POLYLAC = a2polylac;
        this. assignA2POLYLAC = assigna2polylac;
        this.classA2 = classType;
    }
    
    public void setA2fouterarm(String tempa2fouterarm) {
        this.A2FOUTERARM = a2fouterarm;
        this. assignA2FOUTERARM = assigna2fouterarm;
        this.classA2 = classType;
    }
    
    public void setA2hybrid(String tempa2hybrid) {
        this.A2HYBRID = a2hybrid;
        this. assignA2HYBRID = assigna2hybrid;
        this.classA2 = classType;
    }
    
    public void setA2mannose(String tempa2mannose) {
        this.A2MANNOSE = a2mannose;
        this. assignA2MANNOSE = assigna2mannose;
        this.classA2 = classType;
    }
    
    public void setA3s(String tempa3s) {
        this.A3S = a3s;
        this. assignA3S = assigna3s;
        this.classA3 = classType;
    }
    
    public void setA3f(String tempa3f) {
        this.A3F = a3f;
        this. assignA3F = assigna3f;
        this.classA3 = classType;
    }
    
    public void setA3b(String tempa3b) {
        this.A3B = a3b;
        this. assignA3B = assigna3b;
        this.classA3 = classType;
    }
    
    public void setA3bgal(String tempa3bgal) {
        this.A3BGAL = a3bgal;
        this. assignA3BGAL = assigna3bgal;
        this.classA3 = classType;
    }
    
    public void setA3agal(String tempa3agal) {
        this.A3AGAL = a3agal;
        this. assignA3AGAL = assigna3agal;
        this.classA3 = classType;
    }
    
    public void setA3galnac(String tempa3galnac) {
        this.A3GALNAC = a3galnac;
        this. assignA3GALNAC = assigna3galnac;
        this.classA3 = classType;
    }
    
    public void setA3polylac(String tempa3polylac) {
        this.A3POLYLAC = a3polylac;
        this. assignA3POLYLAC = assigna3polylac;
        this.classA3 = classType;
    }
    
    public void setA3fouterarm(String tempa3fouterarm) {
        this.A3FOUTERARM = a3fouterarm;
        this. assignA3FOUTERARM = assigna3fouterarm;
        this.classA3 = classType;
    }
    
    public void setA3hybrid(String tempa3hybrid) {
        this.A3HYBRID = a3hybrid;
        this. assignA3HYBRID = assigna3hybrid;
        this.classA3 = classType;
    }
    
    public void setA3mannose(String tempa3mannose) {
        this.A3MANNOSE = a3mannose;
        this. assignA3MANNOSE = assigna3mannose;
        this.classA3 = classType;
    }
    
    
    
    public void setA4s(String tempa4s) {
        this.A4S = a4s;
        this. assignA4S = assigna4s;
        this.classA4 = classType;
    }
    
    public void setA4f(String tempa4f) {
        this.A4F = a4f;
        this. assignA4F = assigna4f;
        this.classA4 = classType;
    }
    
    public void setA4b(String tempa4b) {
        this.A4B = a4b;
        this. assignA4B = assigna4b;
        this.classA4 = classType;
    }
    
    public void setA4bgal(String tempa4bgal) {
        this.A4BGAL = a4bgal;
        this. assignA4BGAL = assigna4bgal;
        this.classA4 = classType;
    }
    
    public void setA4agal(String tempa4agal) {
        this.A4AGAL = a4agal;
        this. assignA4AGAL = assigna4agal;
        this.classA4 = classType;
    }
    
    public void setA4galnac(String tempa4galnac) {
        this.A4GALNAC = a4galnac;
        this. assignA4GALNAC = assigna4galnac;
        this.classA4 = classType;
    }
    
    public void setA4polylac(String tempa4polylac) {
        this.A4POLYLAC = a4polylac;
        this. assignA4POLYLAC = assigna4polylac;
        this.classA4 = classType;
    }
    
    public void setA4fouterarm(String tempa4fouterarm) {
        this.A4FOUTERARM = a4fouterarm;
        this. assignA4FOUTERARM = assigna2fouterarm;
        this.classA4 = classType;
    }
    
    public void setA4hybrid(String tempa4hybrid) {
        this.A4HYBRID = a4hybrid;
        this. assignA4HYBRID = assigna4hybrid;
        this.classA4 = classType;
    }
    
    public void setA4mannose(String tempa4mannose) {
        this.A4MANNOSE = a4mannose;
        this. assignA2MANNOSE = assigna2mannose;
        this.classA4 = classType;
    }


}
