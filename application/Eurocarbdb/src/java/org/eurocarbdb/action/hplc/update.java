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


public class update extends EurocarbAction  
{
    
    private Profile parent = null;
    private List<HplcPeaksAnnotated> disappear;
    private List<HplcPeaksAnnotated> displayRefinement;
    
    private int digest_id;
    private int profile_idtmp;
  

    
    protected static final Logger logger = Logger.getLogger ( refine.class.getName());

     

    public String execute() throws Exception 
    {
    
    
    logger.info("again check the profile id" + profile_idtmp);
    
//check that profile id exists in database

    parent = Eurocarb.getEntityManager().lookup( Profile.class, profile_idtmp );
    //Query to get all glycanIds in DisRefine
    List refineNoEnz= getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DisRefine.DIS_REFINE_GLYCANS")
    .setParameter("parent", profile_idtmp)
    .list();
    

    int temp = refineNoEnz.size();
    logger.info("number entries in DISREFINE" + temp + "and the profileid = " + profile_idtmp);
    //for each glycanId
    for (Object glycanReported : refineNoEnz) {
        logger.info("whats the profile id temp here:" + profile_idtmp);
        logger.info("number of glycan id product search" + glycanReported);
        List productRefineEnz = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DisRefine.PRODUCT_FIND")
        .setParameter("parent", profile_idtmp)
        .setParameter("glycan", glycanReported)
        .list();

        int temp2 = productRefineEnz.size();    
        int goingaround = 0;

        Iterator refineWithProduct = productRefineEnz.iterator();
    
        while (refineWithProduct.hasNext()) {
            DisRefine disRefineProduct = new DisRefine();
            Object [] disRefinewithProduct = (Object []) refineWithProduct.next();
            Integer profileIdProduct = (Integer) disRefinewithProduct[0];
            Integer digestIdProduct = (Integer) disRefinewithProduct[1];
            Integer glycanIdProduct = (Integer) disRefinewithProduct[2];
            String enzProduct = (String) disRefinewithProduct[3];
            Integer productIdProduct = (Integer) disRefinewithProduct[4];
            
            goingaround++;
            logger.info("where am i in the loop" + goingaround);
        
            disRefineProduct.setProfileId(profileIdProduct);
            disRefineProduct.setDigestId(digestIdProduct);
            disRefineProduct.setGlycanId(glycanIdProduct);
            disRefineProduct.setEnzyme(enzProduct);
            disRefineProduct.setProductId(productIdProduct);
            getEntityManager().store(disRefineProduct);
            //try replacing with update()
            //Eurocarb.getEntityManager().update(disRefineProduct);
            }
        }    
    
        //remeber use of evidence id here
        //below is fine since profileId is a PK
        Profile parent = Profile.lookupById(profile_idtmp);
            logger.info("check parent" + parent);

         List numberOfDigests = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DigestProfile.GET_DIGEST_NUMBER")
            .setParameter("parent", parent)
             .list();

        logger.info("lets check relationship" + parent);

             for (Object digestNumber : numberOfDigests) {
                    logger.info ("digest id:" + digestNumber);
                       //Transaction tx = s.beginTransaction();
                     int numberChanges = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DisRefine.UPDATE")
                     .setParameter("parent", profile_idtmp)
                     .setParameter("digest", digestNumber)
                     .executeUpdate();
             //session.flush();
             // tx.commit();
        }         

    // dont forget digest =0
    //    Transaction txx = s.beginTransaction();
                 int numberChanges = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DisRefine.UPDATE_FIRST_DIGEST")
                 .setParameter("parent", profile_idtmp)
                 .executeUpdate();
         getEntityManager().flush();
    //      txx.commit();

         //now make those structures in *annotated where product of glycan appears
     
    //     Transaction txxx = s.beginTransaction();
        int numberUpdates = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DisRefine.UPDATE_ANNOTATED")
        .setParameter("parent", profile_idtmp)
        .executeUpdate();
    //    session.flush();    
    //    txxx.commit();

    //ensure any structures in final digest found in pre assign are noted
    
            List maxDigest = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DigestProfile.LARGEST_DIGEST")
        .setParameter("parent", parent)
        .setMaxResults(1)
        .list();
        
        //DigestSingle product = (DigestSingle) maxDigest.uniqueResult();
        
        logger.info("maxdigest:" + maxDigest);
        
        for (Object maxDigestInt : maxDigest) {
            
        //Transaction txfinal = s.beginTransaction();
        int numberFinalUpdates = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.HplcPeaksAnnotated.LARGEST_DIGEST_REFINE")
        .setParameter("parent", profile_idtmp)
        .setParameter("largeDigest", maxDigestInt)
        .executeUpdate();
        }
        
        
        //finally lets see those refinements
        
        List showRefinements = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.HplcPeaksAnnotated.GET_REFINEMENT")
        .setParameter("parent", profile_idtmp)
        .list();
        
         displayRefinement = showRefinements;
        
        
        return SUCCESS;
} 

 
//getter and setters

   public List<HplcPeaksAnnotated> getQuery() {
        return displayRefinement;
    }

   public void setQuery( List<HplcPeaksAnnotated> displayRefinement) {
       this.displayRefinement = displayRefinement;
    }

    public List getDisplayRefinement()
    {
        return this.displayRefinement;
    }
    
    

    public Profile getProfile() {
        return parent;
    }

    public void setProfile (Profile parent) {
        this.parent = parent;
    }



    public void setProfileId(int id) {
        this.profile_idtmp = id;
    }

    public int getProfileId() {
        return this.profile_idtmp;
    }


   public void setDigestId(int id) {
        this.digest_id = id;
    }

    public int getDigestId() {
        return this.digest_id;
    }
    




}
