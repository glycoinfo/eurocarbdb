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

/**
* @author             aceroni
* @version            $Rev: 1924 $
*/

public class EditScan extends EurocarbAction implements RequiresLogin,EditingAction 
{

    static final Logger log = Logger.getLogger( EditScan.class );
    
    private int scan_id = -1;
    
    private Scan scan = null;
    private Annotation annotation = null;
    private int annotation_index = -1;
    
    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public int getAnnotationIndex() {
    return annotation_index;
    }

    public void setAnnotationIndex(int index) {
    annotation_index = index;
    }
    
    public int getScanId() {
        return scan_id;
    }

    public void setScanId(int id) {
        this.scan_id = id;
    }
    
    public void setParameters(Map params)
    {   

//        scan = getObjectFromParams(Scan.class, params);        
        super.setParameters(params);
    }
    
    /**
     * Check that the acquisition that the scan belongs to to
     * is owned by the same contributor
     */
    public void checkPermissions() throws InsufficientPermissions
    {
        if (! getScan().getAcquisition().getContributor().equals(Eurocarb.getCurrentContributor())) {
            throw new InsufficientPermissions(this,"Scan does not belong to logged in user");
        }
    }
    
    public String delete() throws Exception {
        if (scan == null) {
            this.addActionError("A scan must be supplied to edit");
            return "error";
        }
        log.info("Removing scan "+scan.getScanId()+" from database");
        scan.getAcquisition().getScans().remove(scan);
        Eurocarb.getEntityManager().remove(scan);
        scan = null;
        return "success";
    }
    
    public String input() throws Exception {

        if (scan == null) {
            this.addActionError("A scan must be supplied to edit");
            return "error";
        }
        return "input";
    }
    
    public String execute() throws Exception {

        if (scan == null) {
            this.addActionError("A scan must be supplied to edit");
            return "error";
        }
        
        Eurocarb.getEntityManager().store(scan);
        
        return "success";
    }              
                
}
