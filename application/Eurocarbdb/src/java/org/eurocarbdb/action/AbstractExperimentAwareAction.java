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

package org.eurocarbdb.action;

//  stdlib imports

//  3rd party imports 
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.dataaccess.core.Experiment;
import org.eurocarbdb.dataaccess.Eurocarb;

/**
*   Convenience superclass for actions that implicitly work with or require
*   a user {@link Experiment} in order to work.
*   
*   author: mjh [glycoslave@gmail.com]
*/
public abstract class AbstractExperimentAwareAction 
extends AbstractUserAwareAction
implements ExperimentAware
{
    private static final Logger log 
        = Logger.getLogger( AbstractExperimentAwareAction.class.getName() );

    /** The current/set experiment */
    private Experiment experiment;

    /** @see ExperimentAware#getExperiment */
    public Experiment getExperiment()
    {
        if ( experiment != null )
            return experiment;   
        
        experiment = (Experiment) retrieveFromSession("experiment");
        if ( experiment != null && log.isDebugEnabled() )
            log.debug("Experiment " + experiment.getId() + " retrieved from session");
        
        return experiment;
    }
    
    
    /** @see ExperimentAware#setExperiment */
    public void setExperiment( Experiment e )
    {
        if ( log.isDebugEnabled() )
            log.debug("setting (current) experiment to " + e );
        
        storeInSession("experiment", e );
        experiment = e;   
    }
    
    
    /** @see ExperimentAware#setExperimentId */
    public void setExperimentId( int id )
    {
        if ( id > 0 )
        {
            Experiment e = Eurocarb.getEntityManager().lookup( Experiment.class, id );   
            if ( e == null )
            {
                if ( log.isInfoEnabled() )
                    log.info( "No experiments corresponding to experimentId=" 
                            + id 
                            + "; ignoring...");
                    
                this.addFieldError("experimentId", "Invalid experiment id");
                return;
            }
            else
            {
                log.debug("setting current experiment to experimentId=" + id );
                setExperiment( e );   
                return;
            }
        }
        else if ( id == 0 )
        {
            log.debug("(re)setting current experiment to null");
            setExperiment( null );
            return;
        }
        else return; //  ignore negative value
    }
    
    
} // end of class




