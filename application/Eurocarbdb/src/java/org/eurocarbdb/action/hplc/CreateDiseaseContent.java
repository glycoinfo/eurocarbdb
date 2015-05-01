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


public class CreateDiseaseContent extends EurocarbAction implements RequiresLogin {
  
  protected static final Logger logger = Logger.getLogger( CreateDiseaseContent.class.getName() );
 
  private int searchProfileId;
  private String meshDiseaseId;
  private int ecdbDiseaseId;

  private Content content;

  public Integer getProfileId() { return this.searchProfileId; }
  public void setProfileId( Integer search_profile ) { this.searchProfileId = search_profile;}

  public String getMeshDiseaseId() { return this.meshDiseaseId; }
  public void setMeshDiseaseId( String search_tax ) { this.meshDiseaseId = search_tax;}

  public Integer getEcdbDiseaseId() { return this.ecdbDiseaseId; }
  public void setEcdbDiseaseId( Integer search_tax_ecdb ) { this.ecdbDiseaseId = search_tax_ecdb;}
  
 
  public String execute() throws Exception {
  
    EntityManager em = getEntityManager();
    
   if( ecdbDiseaseId >0 && meshDiseaseId !=null){
    
    Profile parent = Profile.lookupById(searchProfileId );
    int idtest = parent.getProfileId();
    logger.info("logging profile id: " + idtest);
    logger.info("disease mesh id: " + meshDiseaseId);
    logger.info("ecdb: " + ecdbDiseaseId);
    
    int numberUpdates = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Content.UPDATE_DISEASE")
                        .setParameter("profileId", idtest)
                        .setParameter("mesh", meshDiseaseId)
                        .setParameter("ecdb", ecdbDiseaseId)
                        .executeUpdate();
    
     return "success";
    }
     
 // if( submitAction!.equals("Add GU") 
   logger.info("start content workflow"); 
   //List<Content> searchContent = Content.lookupByProfile(profileId);
  
  return "input";
  
    }

    }      


