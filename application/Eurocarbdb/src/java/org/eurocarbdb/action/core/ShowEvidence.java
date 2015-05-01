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
/*-------------------------------------------------------------------
*   $Id: ShowEvidence.java 1549 2009-07-19 02:40:46Z glycoslave $
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
import org.eurocarbdb.dataaccess.core.Evidence;


import org.hibernate.*; 
import org.hibernate.criterion.*; 

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


public class ShowEvidence extends EurocarbAction
{
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    protected static final Logger log 
        = Logger.getLogger( ShowEvidence.class.getName() );

    private Evidence evidence = null;
    
    private ExperimentStep experiment_step = null;

    private int evidenceId = 1;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    public Evidence getEvidence() {  return evidence;  }

    public int getEvidenceId() {  return evidenceId;  }
    
    public void setEvidenceId( int search_id ) {  evidenceId = search_id;  }

    public String execute()
    {
    // get evidence
    if ( evidenceId <= 0 ) {
        this.addFieldError( "experimentEvidenceId", "Invalid experiment evidence id!" );
        return ERROR;
    }
    
        this.evidence = getEntityManager().lookup( Evidence.class, evidenceId );
    if ( evidence == null ) {
            log.info( "No experiment evidence with id " + evidenceId );
        this.addActionError( "No experiment evidence exists for given evidence id" );
            return ERROR;
    }
    
    // get technique
    String tech_name   = evidence.getTechnique().getTechniqueAbbrev();
        String next_action = "show_" + tech_name; 
       
        return next_action;
    }

    
} // end class
