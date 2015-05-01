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
/**
* $Id: ShowDigestAssign.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/

package org.eurocarbdb.action.hplc;

import java.io.*;
import java.util.*;
import java.lang.*;
import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;


import org.apache.log4j.Logger;

/**
* @author             aceroni
* @version                $Rev: 1549 $
*/
public class ShowDigestAssign extends EurocarbAction {

    protected static final Logger log = Logger.getLogger( ShowPrelimAssign.class);

    private HplcPeaksAnnotated assignDigest;
    private List<HplcPeaksAnnotated> displayDigest;
    private List<HplcPeaksAnnotated> displayDigestList;

    private int profile_id;
    private int digest_id;
   
    public String execute() {

         EntityManager em = getEntityManager();

    log.info("id:" + profile_id);
    
    List displayDigest = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.HplcPeaksAnnotated.DIGEST_ASSIGN_DISPLAY")
        .setParameter("parent", profile_id)
        .setParameter("digest", digest_id)
        .list();
    
    displayDigestList = displayDigest;
         
    
        return INPUT;
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
    

       public List<HplcPeaksAnnotated> getQuery() {
        return displayDigestList;
    }

   public void setQuery( List<HplcPeaksAnnotated> displayDigestList) {
       this.displayDigestList = displayDigestList;
    }

    public List getDisplayDigestList()
    {
        return this.displayDigestList;
    }
    
    
    
     public List getdisplayDigest()
    {
        return this.displayDigest;
    }

    
}
