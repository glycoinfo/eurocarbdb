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

import java.lang.*;
import java.util.*;
import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;

import org.apache.log4j.Logger;

import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.core.Technique;
import org.eurocarbdb.dataaccess.hplc.Column;
import org.eurocarbdb.dataaccess.hplc.Detector;
import org.eurocarbdb.dataaccess.hplc.Instrument;
import org.hibernate.*;
import org.hibernate.cfg.*;

public class SelectSettings extends EurocarbAction  implements RequiresLogin {

    private List<Profile> detectorLookup;
    private List<Profile> columnLookup;
    private List<Profile> instrumentLookup;
    private List<Profile> softwareLookup;
    private List<Profile> displayDetector;
    private List<Profile> displayColumn;
    private List<Profile> displayInstrument;
    private List<Profile> displaySoftware;
    private List<Column> showTypes;
    
    private Profile parent;
    private Detector detector; 
    private Column column;
    private Instrument instrument;
  
    private int detector_id;
    private int instrument_id;
    private int column_id;
    private int parent_id = -1;
    private String acqSwVersion;
    private String dextran = "dextran";
        
    public List getDisplayColumn(){return displayColumn;}
    public List getDisplayInstrument(){return displayInstrument;}
    public List getDisplayDetector(){return displayDetector;}
    public List getDisplaySoftware(){return displaySoftware;}
    
    public Profile getProfile() {
        return parent;
    }

    public void setProfile (Profile parent) {
        this.parent = parent;
    }
  
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
    
    public void setAcqSwVersion ( String acqSwVersion) {
    this.acqSwVersion = acqSwVersion;
    }
    
    public String getAcqSwVersion() {
        return this.acqSwVersion;
    }
    
     public List<Column> getQuery() {
        return showTypes;
    }

   public void setQuery( List<Column> showTypes) {
       this.showTypes = showTypes;
    }

    public List getShowTypes()
    {
        return this.showTypes;
    }
    
    protected static final Logger logger = Logger.getLogger( SelectSettings.class.getName() );

    public String execute() {
                   
    EntityManager em = getEntityManager();
    
    Contributor contributor = Contributor.getCurrentContributor();
    int contributorIda = contributor.getContributorId();
    String contributorId = Integer.toString(contributorIda);
    log.info("Contributor" + contributorId);
   
        
    if( submitAction.equals("Next") ) {
    /*
    detector = Eurocarb.getEntityManager().lookup( Detector.class, detector_id );
    instrument = Eurocarb.getEntityManager().lookup( Instrument.class, instrument_id );
    column = Eurocarb.getEntityManager().lookup( Column.class, column_id );
    if (detector != null && column != null  && instrument != null) {            
    log.info("in loop Detector info " + detector + " instrument " + instrument + " column " + column);
        
    parent.setInstrument(instrument);
    parent.setColumn(column);
    parent.setDetector(detector);
    parent.setContributor(Contributor.getCurrentContributor());
    parent.setTechnique(Technique.lookupAbbrev("hplc"));
    parent.setOperator(contributorId);
    parent.setAcqSwVersion(acqSwVersion);
    parent.setDextranStandard(dextran);
        
    //Eurocarb.getEntityManager().store(parent);  
    */
    return SUCCESS;
    
    //}
    }
    
      if (detector == null && column == null  && instrument == null) {
    
    detectorLookup = Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Profile.USER_DETECTOR_SETTINGS")
           .setParameter("operator", contributorId)
           .list();
           
    displayDetector = detectorLookup;
             
    columnLookup = Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Profile.USER_COLUMN_SETTINGS")
         .setParameter("operator", contributorId)
         .list();
         
    displayColumn = columnLookup;
        
    instrumentLookup = Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Profile.USER_INSTRUMENT_SETTINGS")
             .setParameter("operator", contributorId)
             .list();
    
    displayInstrument = instrumentLookup;
    
    softwareLookup = Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Profile.SOFTWARE")
             .setParameter("operator", contributorId)
             .list();
    
    displaySoftware = softwareLookup;
    
    List colInfo = getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Profile.USER_COLUMN_SETTINGS").setParameter("operator", contributorId).list();

    showTypes = colInfo;
    
    return INPUT;
      }
    
    return INPUT;
    }
    
    

}

 