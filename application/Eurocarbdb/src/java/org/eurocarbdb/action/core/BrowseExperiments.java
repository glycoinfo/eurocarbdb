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

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class BrowseExperiment  *//****************************************
*
*   Shows a detail page for an experiment given an experiment id.
*
*
*   @author   mjh <glycoslave@gmail.com>
*   @version  $Rev: 1932 $
*/
@org.eurocarbdb.action.ParameterChecking(whitelist={""})
public class BrowseExperiments extends EurocarbAction
{
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    protected static final Logger log 
        = Logger.getLogger( ShowExperiment.class.getName() );

    private List<Experiment> all_experiments = new ArrayList<Experiment>();
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    public List<Experiment> getAllExperiments()
    {
        return all_experiments;
    }

    public void setAllExperiments(List<Experiment> exps) {
    all_experiments = exps;
    }

    
    public String execute()
    {
    all_experiments = Experiment.getAllExperiments();
    return SUCCESS;
    }

} // end class
