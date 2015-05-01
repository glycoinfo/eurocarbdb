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
import org.eurocarbdb.action.exception.*;


import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.hibernate.*;

import org.eurocarbdb.action.exception.*;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

import org.hibernate.*; 
import org.hibernate.criterion.*; 

import java.util.*;
import java.io.*;
import org.apache.commons.io.*;
import org.apache.log4j.Logger;


import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.util.StringUtils.paramToInt;

/**
* @author             aceroni
* @version            $Rev: 1870 $
*/
public class EditAcquisition extends AbstractMsAction implements EditingAction, RequiresLogin
{

    protected static final Logger log = Logger.getLogger( EditAcquisition.class.getName() );

    private Acquisition acquisition = null;     

    /** Input data structure */
    private Reference reference;

    public Acquisition getAcquisition() {
        return acquisition;
    }

    public void setAcquisition(Acquisition acquisition) {
        this.acquisition = acquisition;
    } 

    public Reference getReference() 
    {
        return reference;
    }

    public void setReference(Reference ref) 
    {
        reference = ref;
    }


    /**
     * Check that the acquisition we are editing is owned by the current
     * contributor
     */
    public void checkPermissions() throws InsufficientPermissions
    {
        if (! getAcquisition().getContributor().equals(getContributor())) {
            throw new InsufficientPermissions(this,"Acquisition ("+getAcquisition().getAcquisitionId()+") belongs to "+getAcquisition().getContributor()+" but logged in contributor is "+getContributor());
        }
    }


    public void setParameters(Map params)
    {
    	log.debug("Evidence ID is: "+params.get("evidenceId"));
    	acquisition=Eurocarb.getEntityManager().lookup( Acquisition.class , paramToInt(params.get("evidenceId")));
        //acquisition = getObjectFromParams(Acquisition.class, params);
        
        Device device = getObjectFromParams(Device.class, params, "acquisition.device.deviceId");
        if (device != null) {
            acquisition.setDevice(device);
        }
        
        super.setParameters(params);
    }

    public String addSequence() throws Exception
    {
        BiologicalContext context = null;
        
        if (getParameters().get("biologicalContextId") != null) {
            context = getObjectFromParams(BiologicalContext.class, getParameters(), "biologicalContextId");

            // if ( context == null ) {
            //     addActionError("No biological context");
            //     return "input";
            // }

            if (context != null && ! acquisition.getBiologicalContexts().contains(context)) {
                addActionError("Biological context not associated with acquisition");
                return "input";
            }
        }
        
        GlycanSequence sequence = getObjectFromParams(GlycanSequence.class, getParameters(), "glycanSequenceId");
        
        if (sequence == null) {
            addActionError("No sequence given");
            return "input";
        }
        
        if (context != null) {
            sequence.addBiologicalContext(context);
        }
        
        sequence.addEvidence(acquisition);
        
        Eurocarb.getEntityManager().store(sequence);
        
        if (context != null) {
            Eurocarb.getEntityManager().store(context);        
        }
        Eurocarb.getEntityManager().store(acquisition);
        
        return "success";
    }

    public String addReference() throws Exception
    {
        reference = getObjectFromParams(Reference.class, getParameters());            

        if ( reference == null ) {
            return "input";
        }

        acquisition.addReference(reference);

        Eurocarb.getEntityManager().store(acquisition);

        return "success";
    }

    public String deleteReference() throws Exception
    {

        reference = getObjectFromParams(Reference.class, getParameters());            

        if ( reference == null ) {
            return "input";
        }
        if (! getCurrentContributor().equals(acquisition.deleteReference(reference).getContributor())) {
            throw new InsufficientPermissions(this,"Reference to acquisition relationship not owned by current logged in user");
        }
        Eurocarb.getEntityManager().store(acquisition);
        Eurocarb.getEntityManager().store(reference);
        return "success";
    }

    public String execute() throws Exception 
    {
        // store changes
        Eurocarb.getEntityManager().store( acquisition );      

        return "success";
    }

} // end class
