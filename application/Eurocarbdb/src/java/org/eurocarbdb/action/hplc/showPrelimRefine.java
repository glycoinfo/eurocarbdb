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

import org.hibernate.*;
import org.hibernate.cfg.*;

import org.eurocarbdb.dataaccess.hplc.*;
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

public class showPrelimRefine extends EurocarbAction  
{

    private Profile parent = null;
    private Instrument instrument = null;
    
    private int instrument_id;
    
    String a1s = "(a1 = 1 AND s = 1)";
    String a1f = "(a1 = 1 AND f_6 =1)";
    String a1b = "(a1 = 1 AND b = 1)";
    String a1bgal = "(a1 = 1 AND bgal = 1)";
    String a1agal = "(a1 = 1 AND agal = 1)";
    String a1galnac = "(a1 = 1 AND galnac = 1)";
    String a1polylac = "(a1 = 1 AND polylac = 1)"; 
    String a1fouterarm = "(a1 = 1 AND fouterarm = 1)";
    String a1hybrid = "(a1 AND hybrid = 1)";
    String a1mannose = "(a1 AND mannose = 1)";
    
    String A1S;
    String A1F;
    String A1B;
    String A1BGAL;
    String A1AGAL;
    String A1GALNAC;
    String A1POLYLAC;
    String A1FOUTERARM;
    String A1HYBRID;
    String A1MANNOSE;
    
    String a2s = "(a2 = 1 AND s = 1)";
    String a2f = "(a2 = 1 AND f_6 =1)";
    String a2b = "(a2 = 1 AND b = 1)";
    String a2bgal = "(a2 = 1 AND bgal = 1)";
    String a2agal = "(a2 = 1 AND agal = 1)";
    String a2galnac = "(a2 = 1 AND galnac = 1)";
    String a2polylac = "(a2 = 1 AND polylac = 1)"; 
    String a2fouterarm = "(a2 = 1 AND fouterarm = 1)";
    String a2hybrid = "(a2 AND hybrid = 1)";
    String a2mannose = "(a2 AND mannose = 1)";
    
    String A2S;
    String A2F;
    String A2B;
    String A2BGAL;
    String A2AGAL;
    String A2GALNAC;
    String A2POLYLAC;
    String A2FOUTERARM;
    String A2HYBRID;
    String A2MANNOSE;
    
    String a3s = "(a3 = 1 AND s = 1)";
    String a3f = "(a3 = 1 AND f_6 =1)";
    String a3b = "(a3 = 1 AND b = 1)";
    String a3bgal = "(a3 = 1 AND bgal = 1)";
    String a3agal = "(a3 = 1 AND agal = 1)";
    String a3galnac = "(a3 = 1 AND galnac = 1)";
    String a3polylac = "(a3 = 1 AND polylac = 1)"; 
    String a3fouterarm = "(a3 = 1 AND fouterarm = 1)";
    String a3hybrid = "(a3 AND hybrid = 1)";
    String a3mannose = "(a3 AND mannose = 1)";
    
    String A3S;
    String A3F;
    String A3B;
    String A3BGAL;
    String A3AGAL;
    String A3GALNAC;
    String A3POLYLAC;
    String A3FOUTERARM;
    String A3HYBRID;
    String A3MANNOSE;
    
    String a4s = "(a4 = 1 AND s = 1)";
    String a4f = "(a4 = 1 AND f_6 =1)";
    String a4b = "(a4 = 1 AND b = 1)";
    String a4bgal = "(a4 = 1 AND bgal = 1)";
    String a4agal = "(a4 = 1 AND agal = 1)";
    String a4galnac = "(a4 = 1 AND galnac = 1)";
    String a4polylac = "(a4 = 1 AND polylac = 1)"; 
    String a4fouterarm = "(a4 = 1 AND fouterarm = 1)";
    String a4hybrid = "(a4 AND hybrid = 1)";
    String a4mannose = "(a4 AND mannose = 1)";
    
    String A4S;
    String A4F;
    String A4B;
    String A4BGAL;
    String A4AGAL;
    String A4GALNAC;
    String A4POLYLAC;
    String A4FOUTERARM;
    String A4HYBRID;
    String A4MANNOSE;

    String querya;

       protected static final Logger logger = Logger.getLogger ( preAssign.class.getName());
  
       private List<Glycan> preliminary;
       private List<HplcPeaksAnnotated> display; 
       private List<HplcPeaksAnnotated> prelimarytwo;
       private HplcPeaksAnnotated peaksannotated;
       private List<HplcPeaksAnnotated> refinedPrelim;
       private List<HplcPeaksAnnotated> showRefinedPrelim;

      int profile_id;
      int digest_id =0;
      
 
        

public String execute() throws Exception {

 SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
 Session session =sessionFactory.openSession();

    
    
    
    
String queryPartOne = A2S + A2F + A2B + A2BGAL + A2AGAL + A2GALNAC + A2POLYLAC + A2FOUTERARM + A2HYBRID + A2MANNOSE + A1B + A1BGAL + A1AGAL + A1GALNAC + A1POLYLAC + A1FOUTERARM + A1HYBRID + A1MANNOSE + A1S + A1F + A3S + A3F + A3B + A3BGAL + A3AGAL + A3GALNAC + A3POLYLAC + A3FOUTERARM + A3HYBRID + A3MANNOSE + A4S + A4F + A4B + A4BGAL + A4AGAL + A4GALNAC + A4POLYLAC + A4FOUTERARM + A4HYBRID + A4MANNOSE; 


//logger.info("query string:" + query);
//cat the string for the sql query based on checkbox ticks 

String queryreplace = queryPartOne.replaceAll("null", "");
String queryAnd = queryreplace.replaceAll( "\\)\\(", ") OR (");
logger.info("query string again:" + queryAnd);


logger.info("the profile id here is:" + profile_id);

int len = queryAnd.length();

logger.info("string len:" + len);

if (queryAnd != null && queryAnd.length() > 4){ 


String queryRefineComplete = "from HplcPeaksAnnotated where profileId = " + profile_id + " and glycanId in (select glycanId from Glycan where " + queryAnd + " )";
//String queryRefineComplete  = "from HplcPeaksAnnotated where profileId = 8";
logger.info("entire" + queryRefineComplete);

Query query = session.createQuery(queryRefineComplete);
List refinedPrelim = query.list();

showRefinedPrelim = refinedPrelim;

}

return SUCCESS;


}
    

        public List getShowRefinedPrelim()
    {
        return this.showRefinedPrelim;
    }

 
    public List getRefinedPrelim()
    {
        return this.refinedPrelim;
    }


    public List<HplcPeaksAnnotated> getQuery() {
        return prelimarytwo;
    }

   public void setQuery( List<HplcPeaksAnnotated> prelimarytwo) {
       this.prelimarytwo = prelimarytwo;
    }

    public List getPrelimarytwo()
    {
        return this.prelimarytwo;
    }
    
    
    
    public List getDisplay()
    {
        return this.display;
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
    
    public void setA1s(String tempa1s) {
    this.A1S = a1s;
    }
    
    public void setA1f(String tempa1f) {
    this.A1F = a1f;
    }
    
    public void setA1b(String tempa1b) {
    this.A1B = a1b;
    }
    
    public void setA1bgal(String tempa1bgal) {
    this.A1BGAL = a1bgal;
    }
    
    public void setA1agal(String tempa1agal) {
    this.A1AGAL = a1agal;
    }
    
    public void setA1galnac(String tempa1galnac) {
    this.A1GALNAC = a1galnac;
    }
    
    public void setA1polylac(String tempa1polylac) {
    this.A1POLYLAC = a1polylac;
    }
    
    public void setA1fouterarm(String tempa1fouterarm) {
    this.A1FOUTERARM = a1fouterarm;
    }
    
    public void setA1hybrid(String tempa1hybrid) {
    this.A1HYBRID = a1hybrid;
    }
    
    public void setA1mannose(String tempa1mannose) {
    this.A1MANNOSE = a1mannose;
    }


    public void setA2s(String tempa2s) {
    this.A2S = a2s;
    }
    
   public void setA2f(String tempa2f) {
    this.A2F = a2f;
    }
    
    public void setA2b(String tempa2b) {
    this.A2B = a2b;
    }
    
    public void setA2bgal(String tempa2bgal) {
    this.A2BGAL = a2bgal;
    }
    
    public void setA2agal(String tempa2agal) {
    this.A2AGAL = a2agal;
    }
    
    public void setA2galnac(String tempa2galnac) {
    this.A2GALNAC = a2galnac;
    }
    
    public void setA2polylac(String tempa2polylac) {
    this.A2POLYLAC = a2polylac;
    }
    
    public void setA2fouterarm(String tempa2fouterarm) {
    this.A2FOUTERARM = a2fouterarm;
    }
    
    public void setA2hybrid(String tempa2hybrid) {
    this.A2HYBRID = a2hybrid;
    }
    
    public void setA2mannose(String tempa2mannose) {
    this.A2MANNOSE = a2mannose;
    }
       
    public void setA3s(String tempa3s) {
    this.A3S = a3s;
    }
    
   public void setA3f(String tempa3f) {
    this.A3F = a3f;
    }
    
    public void setA3b(String tempa3b) {
    this.A3B = a3b;
    }
    
    public void setA3bgal(String tempa3bgal) {
    this.A3BGAL = a3bgal;
    }
    
    public void setA3agal(String tempa3agal) {
    this.A3AGAL = a3agal;
    }
    
    public void setA3galnac(String tempa3galnac) {
    this.A3GALNAC = a3galnac;
    }
    
    public void setA3polylac(String tempa3polylac) {
    this.A3POLYLAC = a3polylac;
    }
    
    public void setA3fouterarm(String tempa3fouterarm) {
    this.A3FOUTERARM = a3fouterarm;
    }
    
    public void setA3hybrid(String tempa3hybrid) {
    this.A3HYBRID = a3hybrid;
    }
    
    public void setA3mannose(String tempa3mannose) {
    this.A3MANNOSE = a3mannose;
    }

   
    
   public void setA4s(String tempa4s) {
    this.A4S = a4s;
    }
    
   public void setA4f(String tempa4f) {
    this.A4F = a4f;
    }
    
    public void setA4b(String tempa4b) {
    this.A4B = a4b;
    }
    
    public void setA4bgal(String tempa4bgal) {
    this.A4BGAL = a4bgal;
    }
    
    public void setA4agal(String tempa4agal) {
    this.A4AGAL = a4agal;
    }
    
    public void setA4galnac(String tempa4galnac) {
    this.A4GALNAC = a4galnac;
    }
    
    public void setA4polylac(String tempa4polylac) {
    this.A4POLYLAC = a4polylac;
    }
    
    public void setA4fouterarm(String tempa4fouterarm) {
    this.A4FOUTERARM = a4fouterarm;
    }
    
    public void setA4hybrid(String tempa4hybrid) {
    this.A4HYBRID = a4hybrid;
    }
    
    public void setA4mannose(String tempa4mannose) {
    this.A4MANNOSE = a4mannose;
    }
    
    
    





}
