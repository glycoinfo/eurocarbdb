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
import org.eurocarbdb.dataaccess.hplc.*;


import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.dataaccess.Eurocarb.*;
import org.eurocarbdb.dataaccess.EntityManager;
import org.hibernate.*;
import org.hibernate.cfg.*;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;


public class refine extends EurocarbAction  {

  
    private DigestProfile digestprofile;
    private HplcPeaksAnnotated annotated;
    private HplcPeaksAnnotated refineannotation;
    private HplcPeaksAnnotated preliminaryRefine;
    private HplcPeaksAnnotated tempdisappear;
    private Autogu productRefine;
    //private Disappeared disappear;
    private Disappeared disappearLocation;
    private Disappeared disappearLocationFirst;
    private Disappeared disappeartest;
    private DisRefine disappearRef;
    
    private Profile parent = null;
    private Autogu tempgulist;
    private Autogu tempgurefine;
    private DigestSingle tempds;
    private List<Autogu> gulist;
    private List<HplcPeaksAnnotated> disappear;
  
    private int digest_id;
    private int profile_id;
    private int digest_idtmp;
    private int refined_id = 1;
    private int digest_id_test = 1000001;
    private String enzyme;
    private String required = "required";
    private int testnew = 1;
    private int digestloopid = 0;
         
    protected static final Logger logger = Logger.getLogger ( refine.class.getName());
    private int sqltestid = 0;
     

    public String execute() throws Exception {
    
    
        
    logger.info("check the profile id" + profile_id);
    //check that profile id exists in database
    //remember evidenceId used in DigestProfile

    Profile parent = Profile.lookupById(profile_id);
    logger.info("check parent" + parent);
    

    List enzymelist = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DigestProfile.GET_ENZYMES") 
    .setParameter("parent", parent) 
    .list();
    //get all enzymes used for this parent
    //number of digests ran
    int sizeOfDigest = enzymelist.size();
    
        logger.info("number of digests" + enzymelist.size() + "confirm variable" + sizeOfDigest);
        Iterator iter = enzymelist.iterator();
        
    //identify those undigested assigned glycan structures which disappeared after first digest
        List goneFirst = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.HplcPeaksAnnotated.GONE_AT_THE_FIRST")
    .setParameter("parent", profile_id)
    .list();
    
    //needs to be replaced
    String firstEnzyme = "ABS";
    
    
    //store and assign glycan strcutures lost after first digestion
    Iterator iterAtFirst = goneFirst.iterator();
        while (iterAtFirst.hasNext()) {
                 HplcPeaksAnnotated tempFirst = (HplcPeaksAnnotated) iterAtFirst.next();
                 disappearLocationFirst = new Disappeared();
                 disappearLocationFirst.setProfileId(tempFirst.getProfileId());
                 disappearLocationFirst.setDigestId(digestloopid);
                 disappearLocationFirst.setGu(tempFirst.getGu());
                 disappearLocationFirst.setDbGu(tempFirst.getDbGu());
                 disappearLocationFirst.setNameAbbreviation(tempFirst.getNameAbbreviation());
                 disappearLocationFirst.setGlycanId(tempFirst.getGlycanId());
                  //String theenzyme = (String) en;
                  //disappearLocation.setEnzyme(theenzyme);
                 disappearLocationFirst.setEnzyme(firstEnzyme);
                 getEntityManager().store(disappearLocationFirst);
        }
    
    
        //for each enzyme uploaded for parent profile assignment
        for (Object en : enzymelist) {  
                            
            ++digestloopid;
            //some development info
                  logger.info("value of digestloopid is:" + digestloopid + "and the enzyme is:" + en);     
            
            //for each glycan assigned for digest =0
            //lets find where the glycan is last reported
                  List disappearaa = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.HplcPeaksAnnotated.wheretwo")
                  .setParameter("parent", profile_id)
                  .setParameter("digest", digestloopid)
                  .list();
              
            //Retrieve the corresponding enzyme
            //where glycan was last assigned to x digestion
            List disappearEnz = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DigestProfile.GET_ENZYME_DISAPPEAR")
                  .setParameter("parent", parent)
                  .setParameter("digest", digestloopid)
                  .list();     
              
                  StringBuffer strBuffer = new StringBuffer();
                
            //to get the enzyme use
                //--digestloopid;   
               if ( digestloopid < sizeOfDigest) {
                    strBuffer.append(String.valueOf(enzymelist.get(digestloopid))); 
                       logger.info("magic of '" + strBuffer.toString() + "'");
                     //String std;
                     String lastReportedEnz = strBuffer.toString();
                     //std = "last";
     
     
     
                 Iterator itera = disappearaa.iterator();
                      while (itera.hasNext()) {
                         logger.info("this trouble loop with enzyme" + en + "the digestid is:" + digestloopid);
                         // Disappeared tempdisappear = (Disappeared) itera.next();
                             HplcPeaksAnnotated temp = (HplcPeaksAnnotated) itera.next();
                         // DigestProfile tempDigestProfile = (DigestProfile) itera.next();
                        disappearLocation = new Disappeared();
                          // refineannotation = new HplcPeaksAnnotated();
                         //refineannotation.setProfileId(temp.getProfileId());
                                      //disappearLocation.setProfileId(temp.get(testnew)); 
                         disappearLocation.setProfileId(temp.getProfileId());
                          disappearLocation.setDigestId(digestloopid);
                          disappearLocation.setGu(temp.getGu());
                          disappearLocation.setDbGu(temp.getDbGu());
                          disappearLocation.setNameAbbreviation(temp.getNameAbbreviation());
                          disappearLocation.setGlycanId(temp.getGlycanId());
                          //String theenzyme = (String) en;
                          //disappearLocation.setEnzyme(theenzyme);
                          disappearLocation.setEnzyme(lastReportedEnz);
                          getEntityManager().store(disappearLocation);
                  
                      }

                }
                else{
                    String lastReportedEnz = "last";
                    logger.info("magic of '" + lastReportedEnz + "'");
        
        
                Iterator iterlast = disappearaa.iterator();
                  while (iterlast.hasNext()) {
                 logger.info("this trouble loop with enzyme" + en + "the digestid is:" + digestloopid);
                 // Disappeared tempdisappear = (Disappeared) itera.next();
                     HplcPeaksAnnotated temp = (HplcPeaksAnnotated) iterlast.next();
                 // DigestProfile tempDigestProfile = (DigestProfile) itera.next();
                disappearLocation = new Disappeared();
                  // refineannotation = new HplcPeaksAnnotated();
                 //refineannotation.setProfileId(temp.getProfileId());
                                              
                 //disappearLocation.setProfileId(temp.get(testnew)); 
                 disappearLocation.setProfileId(temp.getProfileId());
                  disappearLocation.setDigestId(digestloopid);
                  disappearLocation.setGu(temp.getGu());
                  disappearLocation.setDbGu(temp.getDbGu());
                  disappearLocation.setNameAbbreviation(temp.getNameAbbreviation());
                  disappearLocation.setGlycanId(temp.getGlycanId());
                  //String theenzyme = (String) en;
                  //disappearLocation.setEnzyme(theenzyme);
                  disappearLocation.setEnzyme(lastReportedEnz);
                  getEntityManager().store(disappearLocation);
                      }
                }
  
     
    
    //++digestloopid; //change reflects above mode
    
    
        logger.info("and the name is" + en);
    
                
        }



//new code here

//max selection
/*        String sql = "select max(digestId), glycanId, profileId from Disappeared where profileId = " + profile_id + "group by  glycanId, profileId";
        Query query = session.createQuery(sql);
        List sqlref = query.list();
        Iterator sqliter = sqlref.iterator();
*/
        
        List<Disappeared> maxDigestList = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Disappeared.MAX_DIGEST")
        .setParameter("parent", profile_id)
        .list();
        Iterator maxDigestIter = maxDigestList.iterator();
        
             while (maxDigestIter.hasNext()) {
            DisRefine disrefine = new DisRefine();
            Object [] maxDigestUseage = (Object []) maxDigestIter.next();
            Integer max = (Integer) maxDigestUseage[0];
            Integer queriedGlycanId = (Integer) maxDigestUseage[1];
            Integer queriedProfileId = (Integer) maxDigestUseage[2];
            logger.info("max int:" + max);
            logger.info("theglycanid:" + queriedGlycanId);
            logger.info("theprofileid:" + queriedProfileId);
            //disrefine.setDisRefineId(theprofileId);
            
            disrefine.setProfileId(queriedProfileId);
            disrefine.setDigestId(max);
            disrefine.setGlycanId(queriedGlycanId);
            disrefine.setEnzyme(required);
            getEntityManager().store(disrefine);
            logger.info("problem after store call");
            }
    
        List refineNoEnz= getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DisRefine.DIS_REFINE_GLYCANS")
        .setParameter("parent", profile_id)
        .list();

        int sizeFind = refineNoEnz.size();
        logger.info("the size of the refineNoEnz:" + sizeFind);




        //this is problem area but why is the question
        for (Object enzymeReported : refineNoEnz) {
            logger.info("check the report enz:" + enzymeReported);
            //List refineEnz = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Disappeared.FIND_ENZYME")
            List refineEnz = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DisRefine.FIND_ENZYME_DISAPPEARED")
            .setParameter("parent", profile_id)
            .setParameter("glycan", enzymeReported)
            .setMaxResults(1)    
            .list();
    
            //List refineEnz = refineEnzobject.list();

             //List ttttt = refineEnzobject.uniqueResult();
     

            Iterator refineWithEnz = refineEnz.iterator();
    
                 while (refineWithEnz.hasNext()) {
                DisRefine disRefineEnz = new DisRefine();
                Object [] disRefinewithEnz = (Object []) refineWithEnz.next();
                Integer profileIdEnz = (Integer) disRefinewithEnz[0];
                Integer digestIdEnz = (Integer) disRefinewithEnz[1];
                Integer glycanIdEnz = (Integer) disRefinewithEnz[2];
                String maxenz = (String) disRefinewithEnz[3];
                disRefineEnz.setProfileId(profileIdEnz);
                disRefineEnz.setDigestId(digestIdEnz);
                disRefineEnz.setGlycanId(glycanIdEnz);
                disRefineEnz.setEnzyme(maxenz);
                getEntityManager().store(disRefineEnz);
                 }

        }



    //previous section tells me where glycan ids occur in digests
        //we then need to find last known occurence.    





return INPUT;

} 

 
//getter and setters



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
    }

    public int getDigestId() {
        return this.digest_id;
    }
    
    public void setRefined(int id) {
        this.refined_id = id;
    }

    public int getRefined() {
        return this.refined_id;
    }

 
}
