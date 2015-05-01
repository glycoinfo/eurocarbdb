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
* @author             aceroni
* @version            $Rev: 1924 $
*/
public class CreateAnnotations extends EurocarbAction implements RequiresLogin, EditingAction
{

    protected static final Logger log = Logger.getLogger( CreateAnnotations.class.getName() );      

    // input
    private int allow_new_structures = 1;

    private Scan scan = null;        
    private Annotation annotation = null;
    
    private MassOptions mass_opt = null;
    private FragmentOptions frag_opt = null;
    private AnnotationOptions ann_opt = null;
    
    private GlycanSequence glycan_sequence = null;
    private String structure = "";
    private int contributor_quality = 0;
    private String[] selected_annotations = null;

    public CreateAnnotations() 
    {
        // set mass options
        mass_opt = new MassOptions();
        mass_opt.ISOTOPE = mass_opt.ISOTOPE_MONO;
        mass_opt.DERIVATIZATION = mass_opt.NO_DERIVATIZATION;
    
        // set fragment options
        frag_opt = new FragmentOptions();
        frag_opt.ADD_AFRAGMENTS = false;
        frag_opt.ADD_BFRAGMENTS = true;
        frag_opt.ADD_CFRAGMENTS = false;
        frag_opt.ADD_XFRAGMENTS = false;
        frag_opt.ADD_YFRAGMENTS = true;
        frag_opt.ADD_ZFRAGMENTS = false;
        frag_opt.MAX_NO_CLEAVAGES = 2;
        frag_opt.MAX_NO_CROSSRINGS = 1;
    
        // set annotation options
        ann_opt = new AnnotationOptions();
        ann_opt.NEGATIVE_MODE = true;
        ann_opt.DERIVE_FROM_PARENT = false;
        ann_opt.MASS_ACCURACY = 1000.;
        ann_opt.MASS_ACCURACY_UNIT = ann_opt.MASS_ACCURACY_PPM;
    }

    /**
     * Check that the parent scan/acquisition belongs to the current contributor
     */
    public void checkPermissions() throws InsufficientPermissions {
        if (! getScan().getAcquisition().getContributor().equals(Eurocarb.getCurrentContributor())) {
            throw new InsufficientPermissions(this,"Scan does not belong to logged in user");
        }
    }
    
    public void setAllowNewStructures(int f) {
    allow_new_structures = f;
    }
    
    public int getAllowNewStructures() {
    return allow_new_structures;
    }

    public Scan getScan() {
    return scan;
    }
    
    public void setScan(Scan s) {
    scan = s;
    }    

    public Annotation getAnnotation() {
    return annotation;
    }
    
    public void setAnnotation(Annotation a) {
    annotation = a;
    }    
   

    public GlycanSequence getGlycanSequence() {
    return glycan_sequence;
    }

    public void setGlycanSequence(GlycanSequence s) {
    glycan_sequence = s;
    }

    public void setSelectedAnnotations(String[] sel_annotations) {
    selected_annotations = sel_annotations;
    }

    public String[] getSelectedAnnotations() {
    return selected_annotations;
    }

    public int getContributorQuality() {
    return contributor_quality;
    }

    public void setContributorQuality(int v){
    contributor_quality= v;
    }

 /*   public Collection<Persubstitution> getPersubstitutions() {
    return Persubstitution.getAllPersubstitutions();
    }*/
   
/*    public Collection<ReducingEnd> getReducingEnds() {
    return ReducingEnd.getAllReducingEnds();
    }*/
    
    public MassOptions getMassOptions() {
    return mass_opt;
    }

    public void setMassOptions(MassOptions opt) {
    mass_opt = opt;
    }

    public FragmentOptions getFragmentOptions() {
    return frag_opt;
    }

    public void setFragmentOptions(FragmentOptions opt) {
    frag_opt = opt;
    }

    public AnnotationOptions getAnnotationOptions() {
    return ann_opt;
    }

    public void setAnnotationOptions(AnnotationOptions opt) {
    ann_opt = opt;
    }

 /*   public List<PeakLabeled> getOrderedPeaks() {
    return scan.getPeakLabeledsOrdered();
    }

    public void setParameters(Map params)
    {   

        scan = getObjectFromParams(Scan.class, params);

        glycan_sequence = getObjectFromParams(GlycanSequence.class, params);

        if (glycan_sequence != null && ! scan.getAcquisition().getGlycanSequences().contains(glycan_sequence)) {
            addActionError("Sequence not associated with acquisition");
        }
        
        super.setParameters(params);
    }*/

    public String input() throws Exception {
        boolean hasErrors = false;
        if (glycan_sequence == null) {
            addActionError("No glycan sequence selected");
            hasErrors = true;
        }
        if (scan == null) {
            addActionError("No scan selected");
            hasErrors = true;
        }
        if (hasErrors) {
            throw new InsufficientParams(this);
        }
        return "input";
    }

    public String execute() throws Exception {
        log.debug("Annotating the peak list for a scan with a glycan sequence: "+glycan_sequence.getGlycanSequenceId());
//        AnnotatedPeakList annotated_peaklist = AnnotationUtils.annotate(glycan_sequence,scan,mass_opt,frag_opt,ann_opt);
        log.debug("Setting the annotations for the peak list");
 //       Collection<Annotation> annotations = AnnotationUtils.setAnnotations(scan,annotated_peaklist,getCurrentContributor(),contributor_quality);
//        annotation = annotations.iterator().next();
        AnnotationUtils.storeAnnotations(scan);
        return "success";
    }
    
    public String delete() throws Exception {    
        if( selected_annotations!=null ) 
            AnnotationUtils.deleteAnnotations(annotation,selected_annotations);        
        return "success";
    }
    
}
