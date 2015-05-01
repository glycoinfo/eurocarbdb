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
import org.eurocarbdb.application.glycoworkbench.AnnotatedPeakList;
import org.eurocarbdb.application.glycanbuilder.Fragmenter;
import org.eurocarbdb.application.glycoworkbench.plugin.AnnotationThread;
import org.eurocarbdb.dataaccess.hibernate.*;
import org.hibernate.*; 
import org.hibernate.criterion.*; 

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
* @author             aceroni
* @version            $Rev: 1924 $
*/

public class UploadAnnotations extends EurocarbAction implements RequiresLogin, EditingAction
{

    protected static final Logger log = Logger.getLogger( UploadAnnotations.class.getName() );
    
    private int allow_new_structures = 1;

    // files       
    private File   annotations_file = null;
    private String annotations_file_contenttype = null;
    private String annotations_file_filename = null;
   
    // objects
    private GlycanSequence glycan_sequence = null; 
    private Scan scan = null;        
    private int contributor_quality = 0;
    private String[] selected_annotations = null;
      
    public void setAllowNewStructures(int f) {
    allow_new_structures = f;
    }

    public int getAllowNewStructures() {
    return allow_new_structures;
    }

    public Scan getScan() 
    {
        return scan;
    }
    
    public void setScan(Scan s) 
    {
        scan = s;
    }        

    public GlycanSequence getGlycanSequence() {
    return glycan_sequence;
    }

    public void setGlycanSequence(GlycanSequence s) {
    glycan_sequence = s;
    }

    public void setSelectedAnnotations(String[] sel_annotations) 
    {
        selected_annotations = sel_annotations;
    }

    public String[] getSelectedAnnotations() 
    {
        return selected_annotations;
    }

    public int getContributorQuality() 
    {
        return contributor_quality;
    }

    public void setContributorQuality(int v)
    {
        contributor_quality= v;
    }
    

    public boolean hasAnnotationsFile() 
    {
        return annotations_file!=null;
    }
    
    public void setAnnotationsFile(File file) 
    {
        annotations_file = file;
    }

    public void setAnnotationsFileContentType(String contentType) 
    {
        annotations_file_contenttype = contentType;
    }

    public void setAnnotationsFileFileName(String filename) 
    {
        annotations_file_filename = filename;
    }

    public String getAnnotationsFileFileName() 
    {
        return annotations_file_filename;
    }   
      

/*    public List<PeakLabeled> getOrderedPeaks() 
    {
//        return scan.getPeakLabeledsOrdered();
    }*/


    public void setParameters(Map params)
    {
 //       scan = getObjectFromParams(Scan.class, params);        
        super.setParameters(params);
    }
    
    /**
     * Check that the acquisition that the annotations are being appended to
     * are owned by the same contributor
     */
    public void checkPermissions() throws InsufficientPermissions
    {
        if (! getScan().getAcquisition().getContributor().equals(Eurocarb.getCurrentContributor())) {
            throw new InsufficientPermissions(this,"Acquisition does not belong to logged in user");
        }
    }

    // logic

    public String execute() throws Exception 
    {
        if (annotations_file == null) {
            addActionError("No annotation file supplied");
            return "input";
        }
        AnnotatedPeakList annotations = null;
        try {
            annotations = AnnotationUtils.parseAnnotations(annotations_file);
        } catch (Exception e) {
            log.trace("Error reading annotation file",e);
            addActionError("This is not a valid annotation file");
            return "input";
        }
        if( allow_new_structures==0 ) {
            annotations = AnnotationUtils.restrictToSequence(annotations,glycan_sequence);
        }
        if( annotations.getNoStructures()==0 ) {
            addActionError("The file does not contain annotations for the selected structure");
            return "input";
        }
        
 /*       AnnotationUtils.clearPeaks(scan);
        AnnotationUtils.clearAnnotations(scan);
        try {
            AnnotationUtils.setAnnotations(scan,annotations,Contributor.getCurrentContributor(),0.); 
        } catch (Exception e) {
            addActionError("There was an error contained within this annotation file");
            throw e;
        }*/
        AnnotationUtils.storeAnnotations(scan);       
        return "success";
    }      
  

}
