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
*   Last commit: $Rev: 1870 $ by $Author: david@nixbioinf.org $ on $Date:: 2010-02-23 #$  
*/

package org.eurocarbdb.action.ms;

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;
import org.eurocarbdb.dataaccess.core.*;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;

/**
* @author             aceroni
* @version            $Rev
*/

public class StartWorkflow extends EurocarbAction implements RequiresLogin {
    
    private int experimentStepId = -1;
    private int glycanSequenceId = -1;
    private int glycanSequenceContextId = -1;

    private ExperimentStep experimentStep = null;
    private GlycanSequence glycanSequence = null;

    // Glycan sequence contexts that the user wishes to choose from
    private List<GlycanSequenceContext> glycanSequenceContexts = null;

    // Glycan Sequence context we want to add data to
    private GlycanSequenceContext glycanSequenceContext = null;

    /**
     *  Get accessor for glycanSequenceContextId
     *  Identifier for retrieving a glycanSequenceContextId from the database
     */
    public int getGlycanSequenceContextId()
    {
        return this.glycanSequenceContextId;
    }

    /**
     *  Set accessor for glycanSequenceContextId
     *  @param glycanSequenceContextId Data to set
     *  Identifier for retrieving a glycanSequenceContextId from the database
     */
    public void setGlycanSequenceContextId(int glycanSequenceContextId)
    {
        this.glycanSequenceContextId = glycanSequenceContextId;
    }

    /**
     *  Get accessor for glycanSequenceContexts
     *  Glycan sequence contexts that the user wishes to choose from
     */
    public List<GlycanSequenceContext> getGlycanSequenceContexts()
    {
        return this.glycanSequenceContexts;
    }

    /**
     *  Set accessor for glycanSequenceContexts
     *  @param glycanSequenceContexts Data to set
     *  Glycan sequence contexts that the user wishes to choose from
     */
    public void setGlycanSequenceContexts(List<GlycanSequenceContext> glycanSequenceContexts)
    {
        this.glycanSequenceContexts = glycanSequenceContexts;
    }


    /**
     *  Get accessor for glycanSequenceContext
     *  Glycan Sequence context we want to add data to
     */
    public GlycanSequenceContext getGlycanSequenceContext()
    {
        return this.glycanSequenceContext;
    }

    /**
     *  Set accessor for glycanSequenceContext
     *  @param glycanSequenceContext Data to set
     *  Glycan Sequence context we want to add data to
     */
    public void setGlycanSequenceContext(GlycanSequenceContext glycanSequenceContext)
    {
        this.glycanSequenceContext = glycanSequenceContext;
    }

    public void setExperimentStepId(int id) 
    {
        experimentStepId = id;
    }  

    public void setExperimentStep(ExperimentStep exp_step)
    {
        experimentStep = exp_step;
    }

    public ExperimentStep getExperimentStep()
    {
        return experimentStep;
    }   

    public void setGlycanSequenceId(int id) 
    {
        glycanSequenceId = id;
    } 

    public void setGlycanSequence(GlycanSequence seq)
    {
        glycanSequence = seq;
    }

    public GlycanSequence getGlycanSequence()
    {
        return glycanSequence;
    }

    /*
     * Go through the parameters supplied to this object, and if there are ids
     * for various objects, re-instantiate the object from the database
     */
    private void populateObjectsFromParams()
    {
        if( experimentStepId > 0 ) {
            log.debug("Retrieving experiment step " + experimentStepId);
            experimentStep = Eurocarb.getEntityManager().lookup( ExperimentStep.class, experimentStepId);
        }

        if ( glycanSequenceId > 0 ) {
            log.info("Retrieving glycan sequence " + glycanSequenceId);
            glycanSequence = Eurocarb.getEntityManager().lookup( GlycanSequence.class, glycanSequenceId);
        }

        if ( glycanSequenceContextId > 0 )  {
            log.info("Retrieving context " + glycanSequenceContextId);
            glycanSequenceContext = Eurocarb.getEntityManager().lookup( GlycanSequenceContext.class, glycanSequenceContextId);
        }        
    }

    public String execute() throws Exception {

        populateObjectsFromParams();

        /* Logic for first steps in the MS workflow
         *  1)  If there is a GlycanSequenceContext specified to the workflow,
         *      add it here, and then move on to ask about additional references.
         *  2)  If there is no specific GlycanSequenceContext, we need to select
         *      a context from the list of contexts for this sequence. Present 
         *      the list of contexts to the user to select.
         *  3)  If there is neither a sequence or a glycanSequenceContext
         *      defined, present all the GlycanSequenceContexts to the user
         */

        if (glycanSequenceContext != null) {
            return "success";
        }
        glycanSequenceContexts = getCurrentContributor().getMyContributionsOf(GlycanSequenceContext.class);
        
        if (glycanSequence != null) {
            glycanSequenceContexts.retainAll(glycanSequence.getGlycanSequenceContexts());
            if(glycanSequenceContexts.size()==0){
            	this.passErrorMessage="You haven't associated a biological context with the selected glycan sequence you wish to add evidence to.\nPlease add a biological context first.";
            	return "create_bc_context";
            }
        }
        
        if (glycanSequenceContexts.size() > 10) {
            glycanSequenceContexts = glycanSequenceContexts.subList(0,10);
        }

        return "select_context";
    }      
  
}
