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
import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.core.Technique;
import org.eurocarbdb.dataaccess.hplc.Profile;
import org.eurocarbdb.dataaccess.hplc.Detector;
import org.eurocarbdb.dataaccess.hplc.Column;
import org.eurocarbdb.dataaccess.hplc.Instrument;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eurocarbdb.dataaccess.EntityManager;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

public class CreateParent extends EurocarbAction {

    private Profile parent = null;
    private Detector detector = null; 
    private Column column = null;
    private Instrument instrument = null;
  
    private int detector_id;
    private int instrument_id;
    private int column_id;
    private int parent_id = -1;
    protected static final Log log = LogFactory.getLog( CreateParent.class );
    
    public String execute() throws Exception {
    
    EntityManager em = getEntityManager();
    


        if ( parent == null ) {
       //Eurocarb.getEntityManager().store(parent);
 
            return INPUT;
    }
    else {
        detector = Eurocarb.getEntityManager().lookup( Detector.class, detector_id );
    instrument = Eurocarb.getEntityManager().lookup( Instrument.class, instrument_id );
    column = Eurocarb.getEntityManager().lookup( Column.class, column_id ); 
        if (detector == null) { addFieldError("detector_id", "Invalid detector id");  return INPUT;}
    
    Contributor contributor = Contributor.getCurrentContributor();
    int contributorIda = contributor.getContributorId();
    String contributorId = Integer.toString(contributorIda);
    
    log.info("Contributor" + contributorId + "i am here");
        parent.setDetector(detector);
    parent.setInstrument(instrument);
    parent.setColumn(column);
    parent.setContributor(Contributor.getCurrentContributor());
    parent.setTechnique(Technique.lookupAbbrev("hplc"));
    parent.setOperator(contributorId);
    log.info("execute store");
        //Eurocarb.getEntityManager().store(parent); 
    em.store(parent);
    em.flush();
        return SUCCESS;
    }
    }      


    public Profile getProfile() {
        return parent;
    }

    public void setProfile (Profile parent) {
        this.parent = parent;
    }


/*    public void setProfileId(int id) {
        this.profile_id = id;
    }

    public int getProfileId() {
        return this.profile_id;
    }
*/


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
