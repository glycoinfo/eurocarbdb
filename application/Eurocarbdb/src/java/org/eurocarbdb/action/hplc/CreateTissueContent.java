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


public class CreateTissueContent extends EurocarbAction implements RequiresLogin {
  
  protected static final Logger logger = Logger.getLogger( CreateTissueContent.class.getName() );
 
  private int searchProfileId;
  private String meshTissueId;
  private int ecdbTissueId;
  private Profile parent;
  private Evidence evidence;
  private Evidence evidenceId;

  private Content content;

  public Integer getProfileId() { return this.searchProfileId; }
  public void setProfileId( Integer search_profile ) { this.searchProfileId = search_profile;}

  public String getMeshTissueId() { return this.meshTissueId; }
  public void setMeshTissueId( String search_tax ) { this.meshTissueId = search_tax;}

  public Integer getEcdbTissueId() { return this.ecdbTissueId; }
  public void setEcdbTissueId( Integer search_tax_ecdb ) { this.ecdbTissueId = search_tax_ecdb;}
  
  public Evidence getEvidence() {
    return evidence;
  }
  
  public Evidence getEvidenceId() { return evidenceId;}
  
  public Profile getProfile() {
        return parent;
  }

  public void setProfile (Profile parent) {
        this.parent = parent;
  }
  
 
  public String execute() throws Exception {
  
    EntityManager em = getEntityManager();
    
   if( ecdbTissueId >0 && meshTissueId != null){
    
    Profile parent = Profile.lookupById(searchProfileId );
    int idtest = parent.getProfileId();
    logger.info("logging profile id: " + idtest);
    logger.info("tissue mesh id: " + meshTissueId);
    logger.info("ecdb: " + ecdbTissueId);
        
    
    int numberUpdates = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Content.UPDATE_TISSUE")
                        .setParameter("profileId", idtest)
                        .setParameter("mesh", meshTissueId)
                        .setParameter("ecdb", ecdbTissueId)
                        .executeUpdate();
    
     return "success";
    }
     
 // if( submitAction!.equals("Add GU") 
   logger.info("start content workflow"); 
   //List<Content> searchContent = Content.lookupByProfile(profileId);
  
  return "input";
  
    }

    }      


