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

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;

// import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.*;
import org.eurocarbdb.action.EurocarbAction;


import org.eurocarbdb.dataaccess.EntityManager;
import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.criterion.*;

// import com.opensymphony.xwork.Action;
// import com.opensymphony.xwork.Preparable;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

public class ShowSummaryDigests extends EurocarbAction  {


    private Profile parent = null;
 
    private DigestProfile digestprofile;
 

    private int profile_id;


    protected static final Logger logger = Logger.getLogger ( preAssign.class.getName());
  

    private List<DigestProfile> preliminaryenz;
    private List<Profile> showAllProfiles;
    private List<Profile> showSummary;
    private List<DigestProfile> showSummaryId;
    private List<DigestProfile> showAllId;

    public String execute() throws Exception {

    Profile p = Profile.lookupById(profile_id);
    int e = p.getEvidenceId();
    logger.info("the evidence id:" + e);

    List showAllProfiles = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DigestProfile.SUMMARY")
                 .setParameter("profileId", e)
            .list();
        
   /*String hql = "from DigestProfile where profile=" + profile_id;
    Query query = session.createQuery(hql);
    List showAllProfiles = query.list();
    */
    showSummary = showAllProfiles;
    
    logger.info("size" + showSummary.size());    
               
    return INPUT;

    } //end execute


   public void setProfileId(int id) {
        this.profile_id = id;
    }

   public int getProfileId() {
        return this.profile_id;
    }

  
    /*  public List<Profile> getQuery() {
        return showSummary;
    }

  public void setQuery( List<Profile>  showSummary) {
       this.showSummary = showSummary;
    }
    */
    public List getShowSummary()
    {
        return this.showSummary;
    }

 
    public List getShowAllProfiles()
    {
        return this.showAllProfiles;
    }
    

    public List<DigestProfile> getQuery() {
        return showSummaryId;
    }

    public void setQuery( List<DigestProfile>  showSummaryId) {
       this.showSummaryId = showSummaryId;
    }

    public List getShowSummaryId()
    {
        return this.showSummaryId;
    }

 
    public List getShowAllId()
    {
        return this.showAllId;
    }

}
