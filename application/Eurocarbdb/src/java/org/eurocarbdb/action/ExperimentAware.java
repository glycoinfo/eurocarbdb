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

import org.eurocarbdb.dataaccess.core.Experiment;

/**
*   Interface for actions that implicitly work with or require
*   a user {@link Experiment} in order to work.
*   
*   author: mjh [glycoslave@gmail.com]
*/
public interface ExperimentAware extends UserAware
{
    
    /*  getExperiment  *//*******************************************
    *
    *   Returns current experiment. Note that this may be null, 
    *   indicating <em>no</em> current experiment.
    */
    public Experiment getExperiment()
    ;
    
    
    /*  setExperiment  *//*******************************************
    *
    *   Sets current experiment. May be set to null to effectively
    *   indicate <em>no</em> current experiment.
    */
    public void setExperiment( Experiment e )
    ;
    
    
    /*  setExperimentId  *//*****************************************
    *   
    *   Sets (current) experiment to the given experiment id. This 
    *   value will be set implicitly by an incoming "experimentId=XX" 
    *   parameter.
    */
    public void setExperimentId( int id )
    ;
    
}

