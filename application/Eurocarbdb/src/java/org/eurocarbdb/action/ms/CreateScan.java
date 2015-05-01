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
*   Last commit: $Rev: 1924 $ by $Author: khaleefah $ on $Date:: 2010-06-21 #$  
*/

package org.eurocarbdb.action.ms;

import org.eurocarbdb.action.*;
import org.eurocarbdb.action.exception.*;

import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.hibernate.*;
import org.eurocarbdb.application.glycoworkbench.PeakList;
import org.eurocarbdb.application.glycoworkbench.Peak;

import org.hibernate.*; 
import org.hibernate.criterion.*; 

import java.util.*;
import org.apache.commons.io.*;
import org.apache.log4j.Logger;

import com.opensymphony.xwork.validator.annotations.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import static org.eurocarbdb.util.StringUtils.paramToInt;

/**
* @author             aceroni
* @version            $Rev
*/

public class CreateScan extends EurocarbAction implements RequiresLogin, EditingAction {

    protected static final Logger log = Logger.getLogger( CreateScan.class.getName() );
    
    private Scan scan = null;
    private Acquisition acquisition = null;
 
    private int evidence_id = -1;

    private int acquisition_id = -1;

    private int parent_id = -1;

    public Acquisition getAcquisition() {
        return acquisition;
    }

    public void setAcquisition(Acquisition acquisition) {
        this.acquisition = acquisition;
    }

    public int getAcquisitionId() {
        return acquisition_id;
    }

    public void setAcquisitionId(int id) {
        acquisition_id = id;
    }

    public void setEvidenceId(int id) 
    {
        this.evidence_id = id;
    }

    public int getEvidenceId() 
    {
        return this.evidence_id;
    }

    public void setParentId(int id) 
    {
        this.parent_id = id;
    }

    public int getParentId() 
    {
        return this.parent_id;
    }

    public Scan getScan() {
        return scan;
    }
    
    public void setScan(Scan s) {
        scan = s;
    }

    public void setParameters(Map params)
    {   

        if (params.get("acquisitionId") != null)
        {
            params.put("acquisition.acquisitionId", params.get("acquisitionId"));
        }

        if (params.get("evidenceId") != null)
        {
            params.put("acquisition.acquisitionId", params.get("evidenceId"));
        }

        //acquisition = getObjectFromParams(Acquisition.class, params);
        acquisition=Eurocarb.getEntityManager().lookup( Acquisition.class , paramToInt(params.get("acquisition.evidenceId")));
        super.setParameters(params);
    }


    /**
     * Check that the acquisition that the scan is being appended to
     * is owned by the same contributor
     */
    public void checkPermissions() throws InsufficientPermissions
    {
        if (! getAcquisition().getContributor().equals(Eurocarb.getCurrentContributor())) {
            throw new InsufficientPermissions(this,"Acquisition does not belong to logged in user");
        }
    }
                    
    public String execute() throws Exception
    {
        if ( scan == null ) {
            return "input";
        }
        
        // create
        scan.setAcquisition(acquisition);
//        scan.setPeakProcessing(PeakProcessing.createOrLookup("no processing"));

        // set parent scan
        if( parent_id>0 ) {
            Scan parent = getEntityManager().lookup( Scan.class, parent_id );
            if( parent==null ) {
                this.addFieldError("parentId", "Invalid parentId: " + parent_id);
                return "input";
            }
//            scan.addMsMsRelationship(null,null,parent);
        }

        // store
        Eurocarb.getEntityManager().store(scan);

        acquisition.getScans().add(scan);

//        Eurocarb.getEntityManager().merge(acquisition);
        //Eurocarb.getEntityManager().store(mmr);  

        return "success";
    }    
}
