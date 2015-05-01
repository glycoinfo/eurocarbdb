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
* $Id: ShowSummary.java 1549 2009-07-19 02:40:46Z glycoslave $
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
public class ShowSummary extends EurocarbAction {

   
    private Profile profileList;
    private List<Profile> showAllProfiles;
    private List<Profile> showSummary;

  
    public String execute() {
            

List showAllProfiles = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Profile.SELECT_ALL").list();

      showSummary = showAllProfiles;
          
      if (showSummary.size() == 0) {
          return ERROR;
      }
      else      
        return INPUT;
    }
    
    

    public Profile getProfile() {
        return profileList;
    }
    
   
    public List<Profile> getQuery() {
        return showSummary;
    }

   public void setQuery( List<Profile>  showSummary) {
       this.showSummary = showSummary;
    }

    public List getShowSummary()
    {
        return this.showSummary;
    }
    
    
    
     public List getShowAllProfiles()
    {
        return this.showAllProfiles;
    }
    
}
