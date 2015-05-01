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
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.application.glycoworkbench.GlycanWorkspace;
import org.eurocarbdb.application.glycanbuilder.GlycanDocument;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.ResidueType;
import org.eurocarbdb.application.glycoworkbench.PeakList;
import org.eurocarbdb.application.glycoworkbench.Peak;
import org.eurocarbdb.application.glycoworkbench.AnnotatedPeakList;
import org.eurocarbdb.application.glycanbuilder.MassOptions;
import org.eurocarbdb.application.glycanbuilder.FragmentOptions;
import org.eurocarbdb.application.glycoworkbench.AnnotationOptions;
import org.eurocarbdb.application.glycoworkbench.PeakAnnotationCollection;
import org.eurocarbdb.application.glycanbuilder.CrossRingFragmentType;
import org.eurocarbdb.application.glycanbuilder.Residue;
import org.eurocarbdb.application.glycanbuilder.TextUtils;
import org.eurocarbdb.application.glycanbuilder.Fragmenter;
import org.eurocarbdb.application.glycanbuilder.LogUtils;
import org.eurocarbdb.application.glycoworkbench.plugin.AnnotationThread;


import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.dataaccess.hibernate.*;

import org.eurocarbdb.dataaccess.Eurocarb;

import org.hibernate.*; 
import org.hibernate.criterion.*; 

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
*
*   @author aceroni
*/
public class AnnotationUtils 
{

    static final Logger log = Logger.getLogger( AnnotationUtils.class );
    
    
    public static AnnotatedPeakList parseAnnotations( File file ) throws Exception 
    {
        GlycanWorkspace gw = new GlycanWorkspace(); // init dictionaries
        AnnotatedPeakList ret = new AnnotatedPeakList();
        if( !ret.open(file,false,true) )
        throw new Exception("Error while reading the annotation file: " + LogUtils.getLastError());
        return ret;
    }
    

    public static AnnotatedPeakList restrictToSequence( AnnotatedPeakList annotations
                                                      , GlycanSequence glycan_sequence ) 
    {
        log.debug("Restrict to sequence");
        if( glycan_sequence==null || annotations==null )
            return annotations;

        AnnotatedPeakList ret = new AnnotatedPeakList();
        for( int i = 0; i < annotations.getNoStructures(); i++ ) 
        {
            if ( glycan_sequence.getSequenceCt().equals( annotations.getStructure(i).withNoReducingEndModification().toGlycoCTCondensed()) ) 
                ret.addPeakAnnotations(annotations.getStructure(i),annotations.getPeakAnnotationCollection(i),false);        
        }
        return ret;
    }

    
/*    public static AnnotatedPeakList annotate( GlycanSequence glycan_sequence
                                            , Scan scan
                                            , MassOptions mass_opt
                                            , FragmentOptions frag_opt
                                            , AnnotationOptions ann_opt ) 
    {
//        PeakList peaklist = createPeaklist(scan);
        GlycanDocument doc = parseSequence(glycan_sequence);
        return annotate(doc,peaklist,mass_opt,frag_opt,ann_opt);
    }*/

   
    private static AnnotatedPeakList annotate( GlycanDocument document
                                             , PeakList peaklist
                                             , MassOptions mass_opt
                                             , FragmentOptions frag_opt
                                             , AnnotationOptions ann_opt ) 
    {
        // create fragmenter
        Fragmenter frag = new Fragmenter(frag_opt);
    
        // set mass options
        log.debug(mass_opt.toString());
        document.setMassOptions(document.getStructures(),mass_opt);
    
        // run annotation
        AnnotationThread thread = new AnnotationThread(peaklist,document.getStructures(),frag,ann_opt);
        thread.run();
    
        return thread.getAnnotatedPeaks();
    }
    

    static public GlycanDocument parseSequence(GlycanSequence seq) 
    {
        GlycanDocument ret = new GlycanDocument(new GlycanWorkspace());
        if( !ret.importFromString(seq.getSequenceCt(),"glycoct_condensed") ) 
            log.info("error parsing structures");
        return ret;
    }
    

 /*   static public PeakList createPeaklist(Scan scan) {
        PeakList ret = new PeakList();
        for( PeakLabeled p : scan.getPeakLabeleds() ) 
            ret.add(new Peak(p.getMzValue(),p.getIntensityValue()));
    
        return ret;
    }
    

    static public void clearPeaks(Scan scan) {
        scan.setPeakLabeleds(new HashSet<PeakLabeled>());
    }
    
    
   static public void clearAnnotations(Scan scan) {
        for( PeakLabeled p : scan.getPeakLabeleds() ) 
            p.setPeakAnnotations(new HashSet<PeakAnnotation>());
        scan.setAnnotations(new HashSet<Annotation>());
    }*/
    

  /*  static public Vector<Annotation> setAnnotations(Scan scan, AnnotatedPeakList annotated_peaklist, Contributor contributor, double contributor_quality)  throws Exception 
    {       
        Vector<Annotation> ret = new Vector<Annotation>();
        if( annotated_peaklist==null || annotated_peaklist.getNoStructures()==0 )        
            return ret;
    
        for ( int i = 0; i < annotated_peaklist.getNoStructures(); i++ ) 
        {
            // create annotation
            log.debug("building annotation object");

            // get structure 
            Glycan glycan = annotated_peaklist.getStructure(i);

            // set parent
            glycan.removeReducingEndModification();
            SugarSequence seq = new SugarSequence( glycan.toGlycoCTCondensed() );
            GlycanSequence sequence = GlycanSequence.lookupOrCreateNew( seq );
            int structure_id = sequence.getGlycanSequenceId();

            Persubstitution persubstitution = Persubstitution.lookup(glycan.getMassOptions().DERIVATIZATION);
            ReducingEnd reducingEnd = ReducingEnd.lookup(glycan.getMassOptions().REDUCING_END_TYPE.getName());


            Annotation ann_obj = new Annotation();
            ann_obj.setContributor(contributor);
            ann_obj.setParentStructure(sequence);
            ann_obj.setPersubstitution(persubstitution);
            ann_obj.setReducingEnd(reducingEnd);
            ann_obj.setDateEntered(new Date());

            log.debug("About to introduce Annotation object to the scan");

            scan.getAnnotations().add(ann_obj);
            ann_obj.setScan(scan);
            ret.add(ann_obj);
        
            // add annotations
            for( int l=0; l<annotated_peaklist.getNoPeaks(); l++ ) 
            {            
    
                // retrieve peak
                PeakLabeled pl_obj = findPeak(scan,annotated_peaklist.getPeak(l));
            
                // create peak annotation
                PeakAnnotation pan_obj = new PeakAnnotation(pl_obj,ann_obj);
                ann_obj.getPeakAnnotations().add(pan_obj);
                pl_obj.getPeakAnnotations().add(pan_obj);
    
                // add annotations 
                for( org.eurocarbdb.application.glycoworkbench.Annotation a : annotated_peaklist.getAnnotations(l,i) ) 
                {
                    // associate annotation
                    PeakAnnotated pa_obj = new PeakAnnotated( pan_obj
                                                            , structure_id
                                                            , a.getFragmentEntry().getStructure()
                                                            ,"",0.,a.getMZ(),contributor_quality);
                    pan_obj.getPeakAnnotateds().add(pa_obj);
                
                    // associate fragmentations
                    if ( a.getFragmentEntry().getFragment()!=null ) 
                    {
                        for( Residue cleav : a.getFragmentEntry().getFragment().getCleavages() ) 
                        {
                            Fragmentation f_obj = null;
                            if ( cleav.isRingFragment() ) 
                            {
                                CrossRingFragmentType crt = (CrossRingFragmentType) cleav.getType();
                                f_obj = new Fragmentation( pa_obj
                                                         , crt.getRingFragmentType()
                                                         , ""
                                                         , ""
                                                         ,-1
                                                         , crt.getFirstPos()
                                                         , crt.getLastPos()
                                                         );
                            }
                            else
                            {
                                f_obj = new Fragmentation(pa_obj,cleav.getCleavageType(),"","",-1,0,0);
                            }
                            pa_obj.getFragmentations().add( f_obj );
                        }
                    }
                
                    // associate ions            
                    for( Map.Entry<String,Integer> e : a.getIons().getIonsMap().entrySet() ) 
                    {
                        Ion ion_obj = Ion.lookup(e.getKey());
                        PeakAnnotatedToIon pati_obj = new PeakAnnotatedToIon(ion_obj,pa_obj,e.getValue(),e.getValue()>0);
                        pa_obj.getPeakAnnotatedToIons().add(pati_obj);
                    }        
                }
            }
        }
        
        return ret;
    }*/

   
    static public void deleteAnnotations(Annotation annotation, String[] selected_annotations) 
    {    
        if( annotation==null || selected_annotations==null || selected_annotations.length==0 )
            return;
        
        log.info("Removing "+selected_annotations.length+" annotations");
        
        ArrayList<PeakAnnotated> toRemove = new ArrayList<PeakAnnotated>();
        
        for (String sel : selected_annotations) {
            int peak_id = getPeakId(sel);
            // Why would you use a weird made-up id based upon the ordering
            // of a Set? Each PeakAnnotated has its own id. Not refactoring
            // this bit of wackiness, but this method really needs to move
            // to using the PeakAnnotated IDs.
            int ann_id = getAnnotationId(sel);
            toRemove.add(new ArrayList<PeakAnnotated>(annotation.getPeakAnnotationsOrdered().get(peak_id).getPeakAnnotateds()).get(ann_id));
        }

      /*  for (PeakAnnotated pa : toRemove) {
            pa.getPeakAnnotation().getPeakAnnotateds().remove(pa);
            Eurocarb.getEntityManager().remove(pa);            
        }*/
        
    }

    static public void storeAnnotations(Scan scan) 
    {    
        if( scan==null )
            return;
    
        //
 //       Collection<PeakLabeled> peaks = scan.getPeakLabeleds();
 //       Collection<Annotation> annotations = scan.getAnnotations();
        Eurocarb.getEntityManager().refresh(scan); 

        // store new peaks
/*        for( PeakLabeled pl : peaks ) {
            if( pl.getPeakLabeledId()<=0 )
            Eurocarb.getEntityManager().store(pl); 
        }*/
        
            // store annotations
/*        for( Annotation annotation : annotations ) {            
            Contributor contributor = Eurocarb.lookup(Contributor.class,annotation.getContributor().getContributorId());

            // store GlycanSequence
            GlycanSequence parentStructure = annotation.getParentStructure();
            if( parentStructure.getGlycanSequenceId()>0 )
            Eurocarb.getEntityManager().refresh(parentStructure); 
            else {
            parentStructure.setContributor(contributor);
            Eurocarb.getEntityManager().store(parentStructure);          
            }        

            // store Annotation
            log.debug("Storing annotation with contributor of id "+contributor.getContributorId());
            annotation.setContributor(contributor);
            Eurocarb.getEntityManager().store(annotation);
        }

        // connect evidence to structure    
        for( Annotation annotation : annotations ) {
            
            GlycanSequence parentStructure = annotation.getParentStructure();
            Acquisition acquisition = scan.getAcquisition();
            Eurocarb.getEntityManager().refresh(acquisition);      
       
            for( BiologicalContext bc : acquisition.getBiologicalContexts() ) 
            parentStructure.addBiologicalContext(bc);

            for( Reference ref : acquisition.getReferences() ) 
            parentStructure.addReference(ref);

            parentStructure.addEvidence( acquisition );
        
            Eurocarb.getEntityManager().update(parentStructure);
        }*/
    }
        
    static public int getPeakId(String sel) 
    {
        Vector<String> tokens = TextUtils.tokenize(sel,"_");
        return Integer.parseInt(tokens.elementAt(0));
    }
    
    static public int getAnnotationId(String sel) 
    {
        Vector<String> tokens = TextUtils.tokenize(sel,"_");
        return Integer.parseInt(tokens.elementAt(1));
    }
    
    static public PeakLabeled findPeak(Scan scan, Peak p) 
    {
        PeakLabeled pl_obj = (PeakLabeled) Eurocarb.getEntityManager().createQuery(PeakLabeled.class)
        .add(Restrictions.eq( "scan", scan ))
        .add(Restrictions.eq( "mzValue", p.getMZ() ))
        .add(Restrictions.eq( "intensityValue", p.getIntensity() ))
        .uniqueResult();

    /*    if (pl_obj == null) {
            pl_obj = new PeakLabeled(scan,p.getMZ(),p.getIntensity(),true,1,0,0.);
        }*/
            
        return pl_obj;
    }

}
