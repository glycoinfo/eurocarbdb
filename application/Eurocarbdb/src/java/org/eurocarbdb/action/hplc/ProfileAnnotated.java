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

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;


public class ProfileAnnotated extends EurocarbAction {

    private Profile parent = null;
    private Detector detector = null; 
    private Column column = null;
    private Instrument instrument = null;
    private HplcPeaksAnnotated anno = null;
 
    private int detector_id;
    private int instrument_id;
    private int column_id;
    private int parent_id;
    private int profile_id;
 
    public String execute() throws Exception {



        if ( anno == null ) {
       //Eurocarb.getEntityManager().store(parent);
 
            return INPUT;
    }
    else {
        parent = Eurocarb.getEntityManager().lookup( Profile.class, profile_id );
        if (parent == null) { addFieldError("detector_id", "Invalid detector id");  return INPUT;}

         //anno.setProfile(parent);
        
       // Eurocarb.getEntityManager().store(parent);  
        return SUCCESS;
    }
    }      


    public HplcPeaksAnnotated getHplcPeaksAnnotated() {
        return anno;
    }

    public void setHplcPeaksAnnotated (HplcPeaksAnnotated anno) {
        this.anno = anno;
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
