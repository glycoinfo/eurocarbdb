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
import org.apache.commons.io.*;
import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.hplc.*;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;


import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;
import org.hibernate.*;
import org.hibernate.cfg.*;

import javax.servlet.http.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;


public class UploadAction extends EurocarbAction implements RequiresLogin {
    
SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
Session session = sessionFactory.openSession();



//Session session =sessionFactory.getCurrentSession();
    //String seid = session.getId();
    //Session s = HibernateUtil.currentSession();
   //factory.getCurrentSession()
    
//   Session s = (Session) session.get();
  
//InetAddress inetAddress = InetAddress.getByName("www.interviewjava.blogspot.com");
//System.out.println ("IP  Address: " + inetAddress.getHostAddress());
   
    
       private Profile parent = null;
    private Detector detector = null;
    private Column column = null;
    private Instrument instrument = null;
           private int detector_id;
       private int instrument_id;
        private int column_id;
        private int parent_id;
        private int profile_id;


       private File file;
       private String contentType;
       private String filename;
       private int digest_id;
       
       protected static final Logger logger = Logger.getLogger ( refine.class.getName());
       
       
       
       
      // File targetDir = new File(System.getProperty("java.io.tmpdir"));
      // File tar = new File ("/tmp/undigested" + profile_id + ".txt");
       
       
       
//lets get the seesion id
/*protected void doGet(HttpServletRequest request, HttpServletResponse response){
HttpSession session = request.getSession(true);


String id = session.getId();



logger.info("which session id:" + id);
}
    Session sesssss = HibernateUtil.getSessionFactory().getCurrentSession().getId();
*/

/* test copy section
       File source = new File("/tmp/january.doc");
       File target = new File("/tmp/january-backup.doc");
     //  String location = file.getAbsolutePath();
*/

public String execute() throws Exception {

    if (submitAction.equals("Upload")) {
//     String location = file.getAbsolutePath();
         
//going to move the file uploaded to /tmp an label with profile id
File targetDir = new File(System.getProperty("java.io.tmpdir"));
       File tar = new File ("/tmp/undigested" + profile_id + ".txt");

    FileUtils.copyFileToDirectory(file, targetDir);
         FileUtils.copyFile(file, tar);

     
     logger.info("check the profile id:" + profile_id);
     
     //HttpServletRequest request;
         //HttpServletResponse response;
     //HttpSession session = request.getSession(true);
     //String id = session.getId();
     //logger.info("my session" + session);
     //logger.info("my id" + id);
    return SUCCESS;
} 

else {return INPUT;}  
}


       public void setUpload(File file) {
          this.file = file;
       }

       public void setUploadContentType(String contentType) {
          this.contentType = contentType;
       }

       public void setUploadFileName(String filename) {
          this.filename = filename;
       }


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



// getter and setter for evaluting parent ids
  
      public Detector getDetector() {
        return detector;
    }


    public void setDetector (Detector detector) {
        this.detector = detector;
    }

    public void setDetectorId(int id) {
        this.detector_id = id;
    }

    public int getDetectorId() {
        return this.detector_id;
    }




    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument (Instrument instrument) {
        this.instrument = instrument;
    }



   public void setInstrumentId(int id) {
        this.instrument_id = id;
    }

    public int getInstrumentId() {
        return this.instrument_id;
    }






    public Column getColumn() {
        return column;
    }

    public void setColumn (Column column) {
        this.column = column;
    }

     public void setColumnId(int id) {
        this.column_id = id;
    }

    public int getColumnId() {
        return this.column_id;
    }

      
       

}
