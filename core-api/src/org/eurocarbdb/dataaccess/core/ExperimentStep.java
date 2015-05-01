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
package org.eurocarbdb.dataaccess.core;
// Generated 3/08/2006 16:48:24 by Hibernate Tools 3.1.0.beta4

import java.util.Set;
import java.util.Date;
import java.util.HashSet;
import java.io.Serializable;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import org.eurocarbdb.dataaccess.BasicEurocarbObject;

/**
* 
*/
public class ExperimentStep extends BasicEurocarbObject 
implements Serializable, Comparable<ExperimentStep> 
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging handle. */
    protected static final Logger log 
        = Logger.getLogger( ExperimentStep.class.getName() );

    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    
    
    // Fields    

     private int experimentStepId;
     private Contributor contributor;
     private Experiment experiment;
     private Evidence evidence;
     private Technique technique;
     private Date dateEntered;
     private Date dateObtained;
     private String experimentStepParameters;
     private String experimentStepComments;
     
     private ExperimentStep parentExperimentStep;
     private Set<ExperimentStep> childExperimentSteps = new HashSet<ExperimentStep>(0);


    // Constructors

    /** default constructor */
    public ExperimentStep() 
    {
    }

    /** minimal constructor */
    public ExperimentStep(Contributor contributor, Technique technique) 
    {
        this.contributor = contributor;
        this.technique = technique;
    }
    
    /** full constructor */
    public ExperimentStep(ExperimentStep parentExperimentStep, Contributor contributor, Experiment experiment, Evidence evidence, Technique technique, Date dateEntered, Date dateObtained, String experimentStepParameters, String experimentStepComments, Set<ExperimentStep> childExperimentSteps) 
    {
        this.parentExperimentStep = parentExperimentStep;
        this.contributor = contributor;
        this.experiment = experiment;
        this.evidence = evidence;
        this.technique = technique;
        this.dateEntered = dateEntered;
        this.dateObtained = dateObtained;
        this.experimentStepParameters = experimentStepParameters;
        this.experimentStepComments = experimentStepComments;
        this.childExperimentSteps = childExperimentSteps;
    }
    
    
    // Property accessors

    public int getExperimentStepId() 
    {
        return this.experimentStepId;
    }
    
    public void setExperimentStepId(int experimentStepId) 
    {
        this.experimentStepId = experimentStepId;
    }

    public String getExperimentStepStringId() {        
        return ""+this.experimentStepId;
    }

    public void setExperimentStepStringId(String id) {        
        this.experimentStepId = Integer.parseInt(id);
    }    
    
    public ExperimentStep getParentExperimentStep() 
    {
        return this.parentExperimentStep;
    }
    
    public void setParentExperimentStep(ExperimentStep parentExperimentStep) 
    {
        this.parentExperimentStep = parentExperimentStep;
    }

    public Contributor getContributor() 
    {
        return this.contributor;
    }
    
    public void setContributor(Contributor contributor) 
    {
        this.contributor = contributor;
    }

    public Experiment getExperiment() 
    {
        return this.experiment;
    }
    
    public void setExperiment(Experiment experiment) 
    {
        this.experiment = experiment;
    }

    /**
    *   Retrieves the piece of evidence associated with this 
    *   experiment step, if any. Note that not all experiment steps have
    *   associated evidence, in which case this method returns null.
    */
    public Evidence getEvidence() 
    {
        return this.evidence;
    }
    
    /**
    *   Associates a piece of evidence with this experiment step.
    */
    public void setEvidence(Evidence evidence) 
    {
        this.evidence = evidence;
    }

    public Technique getTechnique() 
    {
        return this.technique;
    }
    
    public void setTechnique(Technique technique) 
    {
        this.technique = technique;
    }

    public Date getDateEntered() 
    {
        return this.dateEntered;
    }
    
    public void setDateEntered(Date dateEntered) 
    {
        this.dateEntered = dateEntered;
    }

    public Date getDateObtained() 
    {
        return this.dateObtained;
    }
    
    public void setDateObtained(Date dateObtained) 
    {
        this.dateObtained = dateObtained;
    }

    public String getExperimentStepParameters() 
    {
        return this.experimentStepParameters;
    }
    
    public void setExperimentStepParameters(String experimentStepParameters) 
    {
        this.experimentStepParameters = experimentStepParameters;
    }

    public String getExperimentStepComments() 
    {
        return this.experimentStepComments;
    }
    
    public void setExperimentStepComments(String experimentStepComments) 
    {
        this.experimentStepComments = experimentStepComments;
    }

    public Set<ExperimentStep> getChildExperimentSteps() {
        return this.childExperimentSteps;
    }

    public List<ExperimentStep> getChildExperimentStepsList() {
        return new ArrayList<ExperimentStep>(this.childExperimentSteps);
    }
    
    public void setChildExperimentSteps(Set<ExperimentStep> childExperimentSteps) 
    {
        this.childExperimentSteps = childExperimentSteps;
    }
   

    /** Sorts by date entered. */
    public int compareTo( ExperimentStep other )
    {
        assert other != null;
        if ( this.getDateEntered() == null ) return -1;
        if ( other.getDateEntered() == null ) return 1;
        return this.getDateEntered().compareTo( other.getDateEntered() );    
    }
   
}
