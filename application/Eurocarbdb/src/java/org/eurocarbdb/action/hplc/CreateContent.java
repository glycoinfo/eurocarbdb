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


public class CreateContent extends EurocarbAction implements RequiresLogin {
  
  protected static final Logger logger = Logger.getLogger( CreateContent.class.getName() );
 
  private int searchProfileId;
  private int ncbiTaxonomyId;
  private int ecdbTaxonomyId;
  private int profile_id;

  Profile parent = null;

  //private Content content;

  //public Integer getProfileId() { return this.searchProfileId; }
  //public void setProfileId( Integer search_profile ) { this.searchProfileId = search_profile;}

  public Integer getNcbiTaxonomyId() { return this.ncbiTaxonomyId; }
  public void setNcbiTaxonomyId( Integer search_tax ) { this.ncbiTaxonomyId = search_tax;}

  public Integer getEcdbTaxonomyId() { return this.ecdbTaxonomyId; }
  public void setEcdbTaxonomyId( Integer search_tax_ecdb ) { this.ecdbTaxonomyId = search_tax_ecdb;}
  
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
 
  public String execute() throws Exception {
  
    EntityManager em = getEntityManager();
    
   if( ecdbTaxonomyId >0 && ncbiTaxonomyId >0){
    RefLink storeValue = new RefLink();
     Content content = new Content();
              
     parent = Profile.lookupById(profile_id );
     int profile_id_store = parent.getProfileId();
     Contributor contributor = Contributor.getCurrentContributor();
     int contributorId = contributor.getContributorId();
     logger.info("conributor" + contributorId + "ecdb" + ecdbTaxonomyId + "parent" + parent + "check" + profile_id);
     content.setTaxonomyId(ecdbTaxonomyId);
     content.setTaxonomyNcbiId(ncbiTaxonomyId);
     content.setContributorId(contributorId);
     content.setProfile(parent);
     content.setParentProfileId(profile_id_store);
     em.store(content);
     
     
     

     return "success";
    }
     
 // if( submitAction!.equals("Add GU") 
   logger.info("start content workflow"); 
   //List<Content> searchContent = Content.lookupByProfile(profileId);
  
  return "input";
  
    }

    }      


