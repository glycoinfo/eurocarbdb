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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/
/*-------------------------------------------------------------------
*   $Id: ShowExperimentStep.java 1932 2010-08-05 07:12:33Z glycoslave $
*   Last changed $Author: glycoslave $
*   EUROCarbDB Project
*------------------------------------------------------------------*/

package org.eurocarbdb.action.core;

//  stdlib imports
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;

//  3rd party imports 
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.Experiment;
import org.eurocarbdb.dataaccess.core.ExperimentStep;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class ShowExperimentStep  *//************************************
*
*   Shows a detail page for an experiment step given an experiment step id.
*
*
*   @author   mjh <glycoslave@gmail.com>
*   @version  $Rev: 1932 $
*/
public class ShowExperimentStep extends EurocarbAction
{
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    protected static final Logger log 
        = Logger.getLogger( ShowExperimentStep.class.getName() );

    /** The experiment we will detail, created using given experiment id */
    private ExperimentStep step = null;

    /** Experiment ID for the experiment to detail, populated from input parameters */
    private int stepId = 1;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    public ExperimentStep getExperimentStep() {  return step;  }
    
    //public void setExperiment( Experiment t ) {  experiment = t;  }

    /** Returns the experiment step id that is being looked up. */
    public int getExperimentStepId() {  return stepId;  }
    
    /** Sets the experiment step id to lookup. */
    public void setExperimentStepId( int search_id ) {  stepId = search_id;  }


    public String execute()
    {
        if ( params == null || params.isEmpty() ) 
            return INPUT;
        
    if ( stepId <= 0 ) {
        this.addFieldError( "experimentStepId", "Invalid experiment step id!" );
        return INPUT;
    }
    
        this.step = getEntityManager().lookup( ExperimentStep.class, stepId );
    
    if ( step == null ) {
            log.info( "No experiment step with tax id " + stepId );
        this.addActionError( "No experiment step exists for given experiment step id" );
            return ERROR;
    }

        String tech_name   = step.getTechnique().getTechniqueAbbrev();
        String next_action = "show_" + tech_name; 
       
        return next_action;
    }

} // end class
