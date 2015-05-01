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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
/**
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

package org.eurocarbdb.application.glycoworkbench.plugin;

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;
import java.util.*;

public class ProfilerThread extends Thread {

    private int progress = 0;   
    private int matches = 0;   

    private AnnotatedPeakList annotated_peaks = null;
    private PeakList peaks = null;    
    private Glycan motif = null;
    private StructureGenerator generator = null;
    private AnnotationOptions ann_opt = null;
    private ProfilerOptions prof_opt = null;

    private boolean add_unmatched_peaks = true;

    public ProfilerThread(PeakList _peaks, Glycan _motif, StructureGenerator _generator, ProfilerOptions _prof_opt, AnnotationOptions _ann_opt) {    
    annotated_peaks = new AnnotatedPeakList();
    peaks = _peaks;
    motif = _motif;
    generator = _generator;
    prof_opt = _prof_opt;
    ann_opt = _ann_opt;
    }      

    public void setAddUnmatchedPeaks(boolean f) {
    add_unmatched_peaks = f;
    }

    public boolean getAddUnmatchedPeaks() {
    return add_unmatched_peaks;
    }

    
    public AnnotatedPeakList getAnnotatedPeaks() {
    return annotated_peaks;
    }

    public void setAnnotatedPeaks(AnnotatedPeakList apl) {
    annotated_peaks = apl;
    }

    public int getProgress() {
    return progress;
    }

    public int getMatches() {
    return matches;
    }

    public void run () {

    progress = 0; 
    matches = 0; 
    
    if( peaks==null || ann_opt==null || generator==null || motif == null ) {
        interrupt();
        return;
    }

    // annotate     
    try {
        match();
    }
    catch( Exception e) {
        LogUtils.report(e);
        interrupt();
    }
    }   

    

    public void match() throws Exception{

    // init
    Vector<IonCloud> ion_clouds = IonCloudUtils.getPossibleIonClouds(ann_opt);
    generator.start(prof_opt.getMassOptions());

    boolean backtrack = false;
    FragmentEntry fe = null;
    
    annotated_peaks.clear();
    while( (fe = generator.next(backtrack))!=null ) {
        
        boolean matched = false;
        boolean in_range = false;
        if( ann_opt.COMPUTE_EXCHANGES ) {        
        Vector<IonCloud> neutral_exchanges = IonCloudUtils.getPossibleNeutralExchanges(fe.fragment.countCharges(),ann_opt);            
        for( IonCloud nex : neutral_exchanges ) {
            for( IonCloud cloud : ion_clouds ) {
            if( cloud.and(nex).isRealistic() ) {
                for( Peak p : peaks.getPeaks() ) {
                double fmz = cloud.computeMZ(nex.getIonsMass() + fe.mass);
                if( match(fmz,p.getMZ(),ann_opt) ) {
                    fe.setScore(generator.computeScore(fe.fragment));
                    annotated_peaks.addPeakAnnotation(motif,new PeakAnnotation(p,fe.and(cloud,nex)),true);
                    matched = true;
                }
                else if( fmz<p.getMZ() )
                    in_range = true;
                }                                
            }                
            if( interrupted() )
                return;
            }
        }
        }
        else {
        for( IonCloud cloud : ion_clouds ) {
            for( Peak p : peaks.getPeaks() ) {
            double fmz = cloud.computeMZ(fe.mass);
            if( match(fmz,p.getMZ(),ann_opt) ) {
                fe.setScore(generator.computeScore(fe.fragment));
                annotated_peaks.addPeakAnnotation(motif,new PeakAnnotation(p,fe.and(cloud)),true);
                matched = true;
            }
            else if( fmz<p.getMZ() )
                in_range = true;
            }               
            if( interrupted() )
            return;                 
        }
        } 
        
        backtrack = !in_range;
        if( matched )
        matches++;
        progress++;
    }

    // check if all peaks have matched
    if( add_unmatched_peaks ) {
        PeakAnnotationCollection pac = annotated_peaks.getPeakAnnotationCollection(motif);
        for( Peak p : peaks.getPeaks() ) {
        if( pac==null || !pac.isAnnotated(p) )
            annotated_peaks.addPeakAnnotation(motif,new PeakAnnotation(p),true);
        }
    }

    } 

    static public boolean match(double fmz, double mz_ratio, AnnotationOptions ann_opt) {
    if( ann_opt.MASS_ACCURACY_UNIT.equals(ann_opt.MASS_ACCURACY_PPM) )
        return (Math.abs(1.-fmz/mz_ratio)<(0.000001*ann_opt.MASS_ACCURACY));
    return (Math.abs(mz_ratio-fmz)<ann_opt.MASS_ACCURACY);
    }    

   

}
