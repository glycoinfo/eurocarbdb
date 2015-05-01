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
*   Last commit: $Rev: 1392 $ by $Author: hirenj $ on $Date:: 2009-07-02 #$  
*/

package org.eurocarbdb.action.ms;

import org.eurocarbdb.action.*;
import org.eurocarbdb.action.exception.*;

import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.application.glycoworkbench.AnnotatedPeakList;
import org.eurocarbdb.application.glycanbuilder.MassOptions;
import org.eurocarbdb.application.glycanbuilder.FragmentOptions;
import org.eurocarbdb.application.glycoworkbench.AnnotationOptions;
import org.eurocarbdb.application.glycanbuilder.Fragmenter;
import org.eurocarbdb.application.glycoworkbench.plugin.AnnotationThread;

import org.eurocarbdb.dataaccess.hibernate.*;
import org.hibernate.*; 
import org.hibernate.criterion.*; 

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
* Annotation manager class
* @author             hirenj
* @version            $Rev: 1392 $
*/
public class EditAnnotations extends EurocarbAction implements RequiresLogin, EditingAction
{

    protected static final Logger log = Logger.getLogger( EditAnnotations.class.getName() );      

    private Annotation annotation = null;

    private String[] selected_annotations = null;

    private final static String[] PARAM_WHITELIST = new String[]{"annotation.annotationId","selectedAnnotations"};

    /**
     * Use a whitelist for this action, to determine which parameters we are going to allow
     */
    public boolean acceptableParameterName(String paramName)
    {
        return Arrays.binarySearch(PARAM_WHITELIST,paramName) >= 0;
    }

    /**
     * Check that the parent scan/acquisition belongs to the current contributor
     */
    public void checkPermissions() throws InsufficientPermissions {
        if (! getAnnotation().getContributor().equals(Eurocarb.getCurrentContributor())) {
            throw new InsufficientPermissions(this,"Annotation does not belong to logged in user");
        }
    }
    
    public Annotation getAnnotation() {
        return annotation;
    }
    
    public void setAnnotation(Annotation a) {
        annotation = a;
    }    

    public void setSelectedAnnotations(String[] sel_annotations) {
        selected_annotations = sel_annotations;
    }

    public String[] getSelectedAnnotations() {
        return selected_annotations;
    }


    public void setParameters(Map params)
    {   

        annotation = getObjectFromParams(Annotation.class, params);
        super.setParameters(params);
    }
    
    /**
     * Accept the list of annotations provided in the parameters, and then
     * remove them from the annotation
     */
    
    public String execute() throws Exception {    
        if( selected_annotations!=null ) {
            AnnotationUtils.deleteAnnotations(annotation,selected_annotations);
            Eurocarb.getEntityManager().store(annotation);
        }
        return "success";
    }
    
    /**
     * Delete this entire annotation from the scan
     */
    
    public String delete() throws Exception {
        if (annotation == null) {
            return "input";
        }

//        annotation.getScan().getAnnotations().remove(annotation);                        
        Eurocarb.getEntityManager().store(annotation.getScan());
        Eurocarb.getEntityManager().remove(annotation);

        annotation = null;

        return "success";
    }
    
    
}
