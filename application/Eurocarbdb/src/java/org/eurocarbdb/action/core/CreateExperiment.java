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
* $Id: CreateExperiment.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package org.eurocarbdb.action.core;

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;

import org.eurocarbdb.dataaccess.core.Experiment;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*   Stores a newly-created Experiment.
*
*   @author             mjh
*   @author             hirenj
*   @version            $Rev: 1549 $
*/
public class CreateExperiment extends EurocarbAction 
{
    private Experiment experiment;
       
    public Experiment getExperiment() 
    {
        if ( experiment == null )
            experiment = new Experiment();
        
        return experiment;
    }

    
    public void setExperiment( Experiment e ) 
    {
        assert e != null;
        this.experiment = e;
    }

    
    public String execute()
    {
        Experiment e = this.getExperiment();
        
        if ( e == null || params.isEmpty() ) 
            return INPUT;
        
        String name = e.getExperimentName();
        if ( name == null || name.length() == 0 )
        {
            addFieldError("experiment.experimentName", 
                          "Experiment name must be defined");   
            return INPUT;
        }
        
        getEntityManager().store( e );
        
        return SUCCESS;
    }

} // end class
