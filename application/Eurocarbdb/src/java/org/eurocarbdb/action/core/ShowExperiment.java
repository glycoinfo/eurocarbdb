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
import org.eurocarbdb.action.Result;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.EurocarbObject;
import org.eurocarbdb.dataaccess.core.Experiment;
import org.eurocarbdb.dataaccess.core.ExperimentStep;
import org.eurocarbdb.dataaccess.core.GlycanSequence;

import org.eurocarbdb.action.RequiresLogin;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class ShowExperiment  *//****************************************
*
*   Shows a detail page for an experiment given an experiment id.
*
*
*   @author   mjh <glycoslave@gmail.com>
*   @version  $Rev: 1932 $
*/
public class ShowExperiment extends EurocarbAction implements RequiresLogin
{
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    static final Logger log = Logger.getLogger( ShowExperiment.class );

    /** The experiment we will detail, created using given experiment id */
    private Experiment experiment = null;

    /** Experiment ID for the experiment to detail, populated from input parameters */
    private int searchExperimentId = 1;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Returns the Experiment that was looked up. This will most likely be 
    *   null if the execute() method has not yet been called. 
    */
    public Experiment getExperiment() {  return experiment;  }
 
    public void setExperiment(Experiment e) { experiment = e; }
   
    //public void setExperiment( Experiment t ) {  experiment = t;  }

    /** Returns the experiment id that is being looked up. */
    public int getExperimentId() {  return searchExperimentId;  }
    
    /** Sets the experiment id to lookup. */
    public void setExperimentId( int search_id ) {  searchExperimentId = search_id;  }


    /** Returns a list of all of the current user's experiments. */
    public List<Experiment> getAllExperiments()
    {
        return Experiment.getAllExperiments();
    }
    

    /** Temp hack to return exp steps as a list instead of a set. */
    public List<ExperimentStep> getExperimentSteps()
    {
        Set<ExperimentStep> steps = getExperiment().getExperimentSteps();
        return new ArrayList<ExperimentStep>( steps );
    }

    
    public String execute()
    {
        if ( params != null && params.isEmpty() ) 
            return INPUT;
        
        if ( searchExperimentId <= 0 )
        {
            this.addFieldError( "experimentId", "Invalid experiment id!" );
            return INPUT;
        }
    
        experiment = getEntityManager().lookup( Experiment.class, searchExperimentId );
    
        if ( experiment == null )
        {
            log.info( "No experiment associated with tax id " + searchExperimentId );
            this.addActionError( "No experiment exists for given experiment id" );
            return ERROR;
        }

        return ! this.hasActionErrors() ? SUCCESS : ERROR;
    }

} // end class
