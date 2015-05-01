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
* $Id: ShowPrelimAssign.java 1549 2009-07-19 02:40:46Z glycoslave $
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
public class ShowPrelimAssign extends EurocarbAction {

    protected static final Logger log = Logger.getLogger( ShowPrelimAssign.class);

    private HplcPeaksAnnotated assignPrelim;
    private List<HplcPeaksAnnotated> displayPrelim;
    private List<HplcPeaksAnnotated> displayPrelimList;

    private int profile_id;
    private int digest_id = 0;
   
    public String execute() {

         EntityManager em = getEntityManager();

    log.info("id:" + profile_id);
     //assignPrelim = Eurocarb.getEntityManager().lookup( HplcPeaksAnnotated.class, profile_id );
    List displayPrelim = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.HplcPeaksAnnotated.PRE_ASSIGN_DISPLAY")
        .setParameter("parent", profile_id)
        .list();
    
    displayPrelimList = displayPrelim;
         
    
        return INPUT;
    }

    
    
    public void setProfileId(int id) {
        this.profile_id = id;
    }

    public int getProfileId() {
        return this.profile_id;
    }

       public List<HplcPeaksAnnotated> getQuery() {
        return displayPrelimList;
    }

   public void setQuery( List<HplcPeaksAnnotated> displayPrelimList) {
       this.displayPrelimList = displayPrelimList;
    }

    public List getDisplayPrelimList()
    {
        return this.displayPrelimList;
    }
    
    
    
     public List getdisplayPrelim()
    {
        return this.displayPrelim;
    }

    
}
