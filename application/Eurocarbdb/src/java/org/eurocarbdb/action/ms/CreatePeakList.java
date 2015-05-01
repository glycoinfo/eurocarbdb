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
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.ms.*;
import org.eurocarbdb.application.glycoworkbench.PeakList;
import org.eurocarbdb.application.glycoworkbench.Peak;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
* @author             aceroni
* @version            $Rev
*/

public class CreatePeakList extends EurocarbAction implements RequiresLogin, EditingAction {

    protected static final Logger log = Logger.getLogger( CreatePeakList.class.getName() );

    private File peaklistFile = null;
    private String peaklistFileContentType = null;
    private String peaklistFileFilename = null;
       
    private int scan_id = -1;
   
    private Scan scan = null;

    private HashSet<PeakLabeled> removed_peaks = new HashSet<PeakLabeled>(); // used to permanently remove peaks from the scan on commit

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public void setPeaklistFile(File file) {
        this.peaklistFile = file;
    }

    public void setPeaklistFileContentType(String contentType) {
        this.peaklistFileContentType = contentType;
    }

    public void setPeaklistFileFileName(String filename) {
        this.peaklistFileFilename = filename;
    }

/*    public void setParameters(Map params)
    {   

        scan = getObjectFromParams(Scan.class, params);

        super.setParameters(params);
    }*/
    
    public void checkPermissions() throws InsufficientPermissions
    {
        if (! getScan().getAcquisition().getContributor().equals(Eurocarb.getCurrentContributor()))
        {
            throw new InsufficientPermissions(this,"Acquisition does not belong to logged in user");
        }
        return;
    }

    public String execute() throws Exception
    {
        // upload peaklist from file
        if ( peaklistFile != null ) {
//            removed_peaks.addAll(scan.getPeakLabeleds());
//            scan.setPeakLabeleds(parsePeaks(scan,peaklistFile));
        } else {
            addActionError("No peaklist supplied");
            return "input";
        }

 //       Set<PeakLabeled> peaks = scan.getPeakLabeleds();
        
        // delete old peaks
        for( PeakLabeled p : removed_peaks ) {
            try {
                Eurocarb.getEntityManager().remove(p);
            }
            catch(Exception e) {
                log.debug("There was an error removing peaks from a peaklist",e);
            }
        }

        removed_peaks.clear();

//        log.debug("Storing "+peaks.size()+" peaks");
        // store new peaks
 /*       for( PeakLabeled p : peaks ) {
            if (p.getId() == 0) {
                Eurocarb.getEntityManager().store(p);
            }
        }*/

        // update scan
        Eurocarb.getEntityManager().store(scan);  
    
        return "success";
    }         
    
    static private Set<PeakLabeled> parsePeaks(Scan s, File _file) throws Exception {
        // parse peak list
        PeakList pl = new PeakList();
        pl.open(_file,false,true);
    
        // create labeled peaks
        HashSet<PeakLabeled> ret = new HashSet<PeakLabeled>();
        log.info("Parsing peaklist gives us "+pl.getPeaks().size()+" peaks");
        for( Peak p : pl.getPeaks() ) {
 //           ret.add(new PeakLabeled(s,p.getMZ(),p.getIntensity(),true,1,0.,0.));
        }
        return ret;
    }

}


