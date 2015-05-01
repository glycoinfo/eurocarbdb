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
import org.eurocarbdb.dataaccess.EntityManager;
import org.hibernate.*;
import org.hibernate.cfg.*;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

public class refinetest extends EurocarbAction  {

  
    private DigestProfile digestprofile;
    private HplcPeaksAnnotated annotated;
    private Profile parent;
  
  
    private int digest_id = 0;
    private int profile_id = 13;
    private int refined_id = 1;
  

  protected static final Logger logger = Logger.getLogger ( refinetest.class.getName());
     

public String execute() throws Exception {

    
    
//check that profile id exists in database

    parent = Eurocarb.getEntityManager().lookup( Profile.class, profile_id );
    List annotated = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.HplcPeaksAnnotated.REFINE_TEST")
    .setParameter("parent", profile_id)
    .setParameter("digest", digest_id)
    .list();
    
        for (Object product : annotated) {
        logger.info("product _id" + product);
    }
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
