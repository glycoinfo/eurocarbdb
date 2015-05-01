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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
// Generated Apr 20, 2007 3:56:30 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.eurocarbdb.dataaccess.core.Experiment;
import org.eurocarbdb.dataaccess.core.ExperimentStep;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class ParentProfile  *//**********************************************
*
*
*/ 
public class ParentProfile extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int parentProfileId;
      
    private Detector detector;
      
    private Column column;
      
    private ExperimentStep experimentStep;
      
    private Instrument instrument;
      
    private Experiment experiment;
      
    private String acqSwVersion;
      
    private String operator;
      
    private Date dateAcquired;
      
    private String dextranStandard;
      
    private String sequentialDigest;
      
    private String userComments;
      
    private String waxUndigested;
      
    private Set<IntegrationMethod> integrationMethods = new HashSet<IntegrationMethod>(0);
      
    private Set<ProfileData> profileDatas = new HashSet<ProfileData>(0);
      
    private Set<HplcPeaksIntegrated> hplcPeaksIntegrateds = new HashSet<HplcPeaksIntegrated>(0);
      
    private Set<HplcPeaksAnnotated> hplcPeaksAnnotateds = new HashSet<HplcPeaksAnnotated>(0);
      
    private Set<DigestProfile> digestProfiles = new HashSet<DigestProfile>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public ParentProfile() {}

    /** Minimal constructor */
    public ParentProfile( Detector detector, Column column, Instrument instrument, String acqSwVersion, String operator, Date dateAcquired, String dextranStandard ) 
    {
        this.detector = detector;
        this.column = column;
        this.instrument = instrument;
        this.acqSwVersion = acqSwVersion;
        this.operator = operator;
        this.dateAcquired = dateAcquired;
        this.dextranStandard = dextranStandard;
    }
    
    /** full constructor */
    public ParentProfile( Detector detector, Column column, ExperimentStep experimentStep, Instrument instrument, Experiment experiment, String acqSwVersion, String operator, Date dateAcquired, String dextranStandard, String sequentialDigest, String userComments, String waxUndigested, Set<IntegrationMethod> integrationMethods, Set<ProfileData> profileDatas, Set<HplcPeaksIntegrated> hplcPeaksIntegrateds, Set<HplcPeaksAnnotated> hplcPeaksAnnotateds, Set<DigestProfile> digestProfiles ) 
    {
        this.detector = detector;
        this.column = column;
        this.experimentStep = experimentStep;
        this.instrument = instrument;
        this.experiment = experiment;
        this.acqSwVersion = acqSwVersion;
        this.operator = operator;
        this.dateAcquired = dateAcquired;
        this.dextranStandard = dextranStandard;
        this.sequentialDigest = sequentialDigest;
        this.userComments = userComments;
        this.waxUndigested = waxUndigested;
        this.integrationMethods = integrationMethods;
        this.profileDatas = profileDatas;
        this.hplcPeaksIntegrateds = hplcPeaksIntegrateds;
        this.hplcPeaksAnnotateds = hplcPeaksAnnotateds;
        this.digestProfiles = digestProfiles;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getParentProfileId  *//******************************** 
    *
    */ 
    public int getParentProfileId() 
    {
        return this.parentProfileId;
    }
    
    
    /*  setParentProfileId  *//******************************** 
    *
    */
    public void setParentProfileId( int parentProfileId ) 
    {
        this.parentProfileId = parentProfileId;
    }
    

    /*  getDetector  *//******************************** 
    *
    */ 
    public Detector getDetector() 
    {
        return this.detector;
    }
    
    
    /*  setDetector  *//******************************** 
    *
    */
    public void setDetector( Detector detector ) 
    {
        this.detector = detector;
    }
    

    /*  getColumn  *//******************************** 
    *
    */ 
    public Column getColumn() 
    {
        return this.column;
    }
    
    
    /*  setColumn  *//******************************** 
    *
    */
    public void setColumn( Column column ) 
    {
        this.column = column;
    }
    

    /*  getExperimentStep  *//******************************** 
    *
    */ 
    public ExperimentStep getExperimentStep() 
    {
        return this.experimentStep;
    }
    
    
    /*  setExperimentStep  *//******************************** 
    *
    */
    public void setExperimentStep( ExperimentStep experimentStep ) 
    {
        this.experimentStep = experimentStep;
    }
    

    /*  getInstrument  *//******************************** 
    *
    */ 
    public Instrument getInstrument() 
    {
        return this.instrument;
    }
    
    
    /*  setInstrument  *//******************************** 
    *
    */
    public void setInstrument( Instrument instrument ) 
    {
        this.instrument = instrument;
    }
    

    /*  getExperiment  *//******************************** 
    *
    */ 
    public Experiment getExperiment() 
    {
        return this.experiment;
    }
    
    
    /*  setExperiment  *//******************************** 
    *
    */
    public void setExperiment( Experiment experiment ) 
    {
        this.experiment = experiment;
    }
    

    /*  getAcqSwVersion  *//******************************** 
    *
    */ 
    public String getAcqSwVersion() 
    {
        return this.acqSwVersion;
    }
    
    
    /*  setAcqSwVersion  *//******************************** 
    *
    */
    public void setAcqSwVersion( String acqSwVersion ) 
    {
        this.acqSwVersion = acqSwVersion;
    }
    

    /*  getOperator  *//******************************** 
    *
    */ 
    public String getOperator() 
    {
        return this.operator;
    }
    
    
    /*  setOperator  *//******************************** 
    *
    */
    public void setOperator( String operator ) 
    {
        this.operator = operator;
    }
    

    /*  getDateAcquired  *//******************************** 
    *
    */ 
    public Date getDateAcquired() 
    {
        return this.dateAcquired;
    }
    
    
    /*  setDateAcquired  *//******************************** 
    *
    */
    public void setDateAcquired( Date dateAcquired ) 
    {
        this.dateAcquired = dateAcquired;
    }
    

    /*  getDextranStandard  *//******************************** 
    *
    */ 
    public String getDextranStandard() 
    {
        return this.dextranStandard;
    }
    
    
    /*  setDextranStandard  *//******************************** 
    *
    */
    public void setDextranStandard( String dextranStandard ) 
    {
        this.dextranStandard = dextranStandard;
    }
    

    /*  getSequentialDigest  *//******************************** 
    *
    */ 
    public String getSequentialDigest() 
    {
        return this.sequentialDigest;
    }
    
    
    /*  setSequentialDigest  *//******************************** 
    *
    */
    public void setSequentialDigest( String sequentialDigest ) 
    {
        this.sequentialDigest = sequentialDigest;
    }
    

    /*  getUserComments  *//******************************** 
    *
    */ 
    public String getUserComments() 
    {
        return this.userComments;
    }
    
    
    /*  setUserComments  *//******************************** 
    *
    */
    public void setUserComments( String userComments ) 
    {
        this.userComments = userComments;
    }
    

    /*  getWaxUndigested  *//******************************** 
    *
    */ 
    public String getWaxUndigested() 
    {
        return this.waxUndigested;
    }
    
    
    /*  setWaxUndigested  *//******************************** 
    *
    */
    public void setWaxUndigested( String waxUndigested ) 
    {
        this.waxUndigested = waxUndigested;
    }
    

    /*  getIntegrationMethods  *//******************************** 
    *
    */ 
    public Set<IntegrationMethod> getIntegrationMethods() 
    {
        return this.integrationMethods;
    }
    
    
    /*  setIntegrationMethods  *//******************************** 
    *
    */
    public void setIntegrationMethods( Set<IntegrationMethod> integrationMethods ) 
    {
        this.integrationMethods = integrationMethods;
    }
    

    /*  getProfileDatas  *//******************************** 
    *
    */ 
    public Set<ProfileData> getProfileDatas() 
    {
        return this.profileDatas;
    }
    
    
    /*  setProfileDatas  *//******************************** 
    *
    */
    public void setProfileDatas( Set<ProfileData> profileDatas ) 
    {
        this.profileDatas = profileDatas;
    }
    

    /*  getHplcPeaksIntegrateds  *//******************************** 
    *
    */ 
    public Set<HplcPeaksIntegrated> getHplcPeaksIntegrateds() 
    {
        return this.hplcPeaksIntegrateds;
    }
    
    
    /*  setHplcPeaksIntegrateds  *//******************************** 
    *
    */
    public void setHplcPeaksIntegrateds( Set<HplcPeaksIntegrated> hplcPeaksIntegrateds ) 
    {
        this.hplcPeaksIntegrateds = hplcPeaksIntegrateds;
    }
    

    /*  getHplcPeaksAnnotateds  *//******************************** 
    *
    */ 
    public Set<HplcPeaksAnnotated> getHplcPeaksAnnotateds() 
    {
        return this.hplcPeaksAnnotateds;
    }
    
    
    /*  setHplcPeaksAnnotateds  *//******************************** 
    *
    */
    public void setHplcPeaksAnnotateds( Set<HplcPeaksAnnotated> hplcPeaksAnnotateds ) 
    {
        this.hplcPeaksAnnotateds = hplcPeaksAnnotateds;
    }
    

    /*  getDigestProfiles  *//******************************** 
    *
    */ 
    public Set<DigestProfile> getDigestProfiles() 
    {
        return this.digestProfiles;
    }
    
    
    /*  setDigestProfiles  *//******************************** 
    *
    */
    public void setDigestProfiles( Set<DigestProfile> digestProfiles ) 
    {
        this.digestProfiles = digestProfiles;
    }
    
       

    







} // end class
