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

package org.eurocarbdb.application.glycoworkbench;

import org.eurocarbdb.application.glycanbuilder.*;

import java.io.*;
import java.util.*;

/**
   Contains all the different annotations associated with a single
   peak. The annotations can come from multiple structures, or from
   the same structure. Multiple annotations can be associated with the
   same structure. 

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class PeakAnnotationMultiple {

    protected Peak peak;
    protected Vector<Vector<Annotation>> annotations;
    
    /**
       Create a new object that can contain all the annotations for a
       specified peak
       @param no_structures the number of structures from which the
       annotations are derived from
     */
    public PeakAnnotationMultiple(Peak p, int no_structures) {
    peak = p.clone();
    annotations = new Vector<Vector<Annotation>>();
    for( int i=0; i<no_structures; i++ )
        addStructure();
    }

    /**
       Remove all annotations contained in this object
     */
    public void clearAnnotations() {
    for( Vector<Annotation> v : annotations )
        v.clear();
    }

    /**
       Make space for annotations deriving from an additional
       structure
     */
    public void addStructure() {
    annotations.add(new Vector<Annotation>());
    }

    /**
       Make space for annotations deriving from an additional
       structure
       @param s_ind the index of the new structure
     */
    public void insertStructureAt(int s_ind) {
    annotations.insertElementAt(new Vector<Annotation>(),s_ind);
    }

    /**
       Remove the space for all the annotations deriving from a
       specific structure
       @param s_ind the index of the structure
     */
    public void removeStructureAt(int s_ind) {
    annotations.removeElementAt(s_ind);
    }
    
    /**
       Add an annotation for a specific structure
       @param s_ind the index of the structure
       @param toadd the annotation to be added
       @return <code>true</code> if the annotation was correctly added
     */
    public boolean addAnnotation(int s_ind, Annotation toadd) {
    if( toadd!=null && !toadd.isEmpty() ) {
        // order annotations
        Vector<Annotation> v = annotations.elementAt(s_ind);
        for( int i=0; i<v.size(); i++ ) {        
        Annotation e = v.elementAt(i);
        
        int comp = e.compareTo(toadd);
        if( comp==0 )
            return false;
        if( comp>0 ) {
            v.insertElementAt(toadd,i);
            return true;
        }
        }

        v.add(toadd);
        return true;                
    }
    return false;
    }

    /**
       Remove an annotation for a specific structure
       @param s_ind the index of the structure
       @param toremove the annotation to be removed
       @return <code>true</code> if the annotation was correctly removed
     */
    public boolean removeAnnotation(int s_ind, Annotation toremove) {
    if( toremove==null || toremove.isEmpty() )
        return false;

    Vector<Annotation> v = annotations.elementAt(s_ind);
    for( int i=0; i<v.size(); i++ ) {
        // look for fragment
        Annotation e = v.elementAt(i);
        if( e.equals(toremove) ) {
        // remove it
        v.removeElementAt(i);
        return true;
        }    
    }    
    return false;
    }

    /**
       Return the peak associated with this object
     */
    public Peak getPeak() {
    return peak;
    }

    /**
       Return the minimum difference between experimental and
       predicted mass/charge values for a specific structure
       @param s_ind the index of the structure
     */      
    public double getBestAccuracy(int s_ind) {
    double best_acc = Double.MAX_VALUE;
    for( Annotation e : annotations.elementAt(s_ind) ) {
        double acc = e.getAccuracy(peak);
        if( Math.abs(acc)<Math.abs(best_acc) )
        best_acc = acc;
    }
    return best_acc;
    }


    /**
       Return the minimum difference in PPM between experimental and
       predicted mass/charge values for a specific structure
       @param s_ind the index of the structure
     */      
    public double getBestAccuracyPPM(int s_ind) {
    double best_acc = Double.MAX_VALUE;
    for( Annotation e : annotations.elementAt(s_ind) ) {
        double acc = e.getAccuracyPPM(peak);
        if( Math.abs(acc)<Math.abs(best_acc) )
        best_acc = acc;
    }
    return best_acc;
    }

    /**
       Return all annotations
     */
    public Vector<Vector<Annotation>> getAnnotations() {
    return annotations;
    }

    /**
       Return all annotations for a specific structure
     */
    public Vector<Annotation> getAnnotations(int s_ind) {
    return annotations.elementAt(s_ind);
    }

    /**
       Return <code>true</code> if the object contain at least one
       annotation
     */
    public boolean isAnnotated() {
    for( Vector<Annotation> va : annotations ) {
        if( va.size()>0 )
        return true;
    }
    return false;
    }

    /**
       Return <code>true</code> if the object contain at least one
       annotation for the specific structure
       @param s_ind the index of the structure
     */
    public boolean isAnnotated(int s_ind) {
    return (annotations.elementAt(s_ind).size()>0);
    }

}