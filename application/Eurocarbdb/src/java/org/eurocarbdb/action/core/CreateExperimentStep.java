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
* $Id: CreateExperimentStep.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package org.eurocarbdb.action.core;

import java.util.List;

import org.apache.log4j.Logger;

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.action.AbstractExperimentAwareAction;
import org.eurocarbdb.dataaccess.EntityManager;

import org.eurocarbdb.dataaccess.core.Technique;
import org.eurocarbdb.dataaccess.core.Experiment;
import org.eurocarbdb.dataaccess.core.ExperimentStep;
import org.eurocarbdb.dataaccess.core.Contributor;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*   Stores a newly-created ExperimentStep step.
*
*   @author             mjh
*   @version            $Rev: 1549 $
*/
public class CreateExperimentStep extends AbstractExperimentAwareAction 
{
    /** Logging handle. */
    protected static final Logger log 
        = Logger.getLogger( CreateExperimentStep.class.getName() );
    
    
    private ExperimentStep step;
       
    private Technique technique;

    private List<Technique> techniques;


    public int getExperimentId()
    {
        return getExperimentStep().getExperiment().getExperimentId();
    }
        
    public void setExperimentId( int id )
    {
        if ( id <= 0 )
        {
            addFieldError("experimentId", "Invalid experiment id");
            return;
        }
        
        Experiment e = getEntityManager().lookup( Experiment.class, id );
        getExperimentStep().setExperiment( e );
    }
    

    public ExperimentStep getExperimentStep() 
    {
        if ( this.step == null )
            this.step = new ExperimentStep();
        
        return this.step;
    }

    
    public void setExperimentStep( ExperimentStep e ) 
    {
        assert e != null;
        this.step = e;
    }
    
/* 
    public Technique getTechnique() 
    {
        if ( technique == null )
            technique = new Technique();
        
        return technique;  
    }
 
    public int getTechniqueId()
    {
        return getExperimentStep().getTechnique().getTechniqueId();
    }
*/
    
    public void setTechniqueId( int id ) 
    {  
        if ( id <= 0 )
        {
            addFieldError("techniqueId", "Invalid technique id");
            return;
        }
        
        Technique t = getEntityManager().lookup( Technique.class, id );
        getExperimentStep().setTechnique( t );
    }
        
    public List<Technique> getAllTechniques()
    {
        if ( techniques == null )
            techniques = Technique.getAllTechniques();   
        
        return techniques;
    }
    
    
    public String execute()
    {
        ExperimentStep s = this.getExperimentStep();
        Experiment     e = s.getExperiment();
        Technique      t = s.getTechnique();
        
        if ( getAllTechniques() == null )
        {
            addActionError( "No techniques defined in the DB."
                          + "This probably means your DB has not "
                          + "been installed correctly.");
            return "error";
        }
        
        
        if ( e == null ) 
        {
            log.debug("no experimentId given, returning input_experiment view");
            return "input_experiment";
        }
            
        if ( t == null || t.getTechniqueName() == null )
        {
            log.debug("no technique given, returning input view");
            return "input";
        }
            
            
        EntityManager em = getEntityManager();
        // try
        // {
            //em.lookup( t, t.getTechniqueId() );

        // }
        // catch ( EntityDoesntExistException ex )
        // {
            // log.info( "" );   
        // }

        
        log.debug("setting technique " + t );
        s.setTechnique( t );
        
        log.debug("setting contributor " );
        Contributor c = this.getContributor(); //em.lookup( Contributor.class, 1 );
        s.setContributor( c );
        
        log.debug("storing experiment step");
        em.store( s );
        
        String tech_name  = t.getTechniqueAbbrev();
        assert tech_name != null;
        
        String next_stage = "start_" + tech_name.replace('-', '_');
        
        log.debug("next stage is " + next_stage );
        
        return next_stage;
    }

} // end class
