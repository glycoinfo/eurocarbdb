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

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.*;
import org.eurocarbdb.dataaccess.core.*;


import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;


import java.util.Set;
import java.util.TreeSet;
import java.util.List;

import org.apache.log4j.Logger;



public class ShowHplc extends EurocarbAction {

    protected static final Logger log = Logger.getLogger( ShowHplc.class.getName() );

    private Profile profile = null;
    private Evidence evidence = null;
    private List<Profile> showSummary;
    private List<DigestProfile> showDigestSummary;

    private int evidenceId;
    private int searchEvidenceId;

    
    public Profile getProfile() {      
    return profile;  
    }

    public void setProfile(Profile p) {      
    profile = p;  
    }

    public int getEvidenceId() { return this.searchEvidenceId; }
    public void setEvidenceId( int search_evidence_id) {this.searchEvidenceId = search_evidence_id; }
    
    public Evidence getEvidence() {      
    return evidence;  
    }

    public void setEvidence(Evidence e) {      
    evidence = e;  
    }

    public List getShowSummary()
    {
        return this.showSummary;
    }

    public List getShowDigestSummary()
    {
    return this.showDigestSummary;
    }

    public String execute() {
    
    evidence = getEntityManager().lookup(Evidence.class, searchEvidenceId);
        if (evidence !=null) {
            
                        
              Profile ptest = Profile.lookupByEvidence(searchEvidenceId);
              int profileId = ptest.getProfileId();
            log.info("main query");

              profile = getEntityManager().lookup(Profile.class, searchEvidenceId);
    
            List<Profile> profileBiologicalContent = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Profile.EVIDCONTEXT")    
                                 .setParameter("evidId", evidence)
                                    .list();

            showSummary = profileBiologicalContent;
            
            
            List<DigestProfile> showDigest = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Profile.SUMMARY_DIGESTS")
                             .setParameter("profileId", profileId)
                             .list();
            
            showDigestSummary = showDigest;
            
            
    }
              return SUCCESS;
            
    }

}
