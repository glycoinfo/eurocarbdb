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

package org.eurocarbdb.application.glycoworkbench.plugin.reporting;

import org.eurocarbdb.application.glycoworkbench.plugin.*;
import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;

import java.io.*;
import java.util.*;
import java.awt.geom.*;

public class AnnotationObject {

    private boolean highlighted = false;
    private Point2D peak_point = null;
    private Vector<PeakAnnotation> peak_annotations = new Vector<PeakAnnotation>();
    private Vector<Glycan> structures = new Vector<Glycan>();   

    public AnnotationObject() {
    }

    public AnnotationObject(double x, double y) {
    peak_point = new Point2D.Double(x,y);
    }

    public AnnotationObject(PeakAnnotation pa) {
    peak_point = new Point2D.Double(pa.getPeak().getMZ(),pa.getPeak().getIntensity());
    peak_annotations.add(pa);
    structures.add(pa.getFragment());
    }

    public AnnotationObject(double x, double y, PeakAnnotation pa) {
    peak_point = new Point2D.Double(x,y);
    peak_annotations.add(pa);
    structures.add(pa.getFragment());
    }
    
    public AnnotationObject(Point2D pp, PeakAnnotation pa) {
    peak_point = pp;
    peak_annotations.add(pa);
    structures.add(pa.getFragment());
    }


    public Point2D getPeakPoint() {    
    return peak_point;
    }

    public Peak getPeak() {
    if( peak_annotations.size()==0 )
        return null;
    else
        return peak_annotations.firstElement().getPeak();
    }

    public boolean isHighlighted() {
    return highlighted;
    }

    public void setHighlighted(boolean f) {
    highlighted = f;
    }

    public void clear() {
    structures.clear();
    peak_annotations.clear();
    }

    public int size() {
    return peak_annotations.size();
    }
    
    public void add(PeakAnnotation pa) {
    if( peak_point==null )
        peak_point = new Point2D.Double(pa.getPeak().getMZ(),pa.getPeak().getIntensity());

    peak_annotations.add(pa);
    structures.add(pa.getFragment());
    }
 
    public void remove(PeakAnnotation pa) {
    if( pa!=null ) {
        peak_annotations.remove(pa);
        for( Iterator<Glycan> i=structures.iterator(); i.hasNext(); ) {
        if( i.next().equalsStructure(pa.getFragment()) )
            i.remove();
        }
    }
    }

    public boolean canGroup(AnnotationObject other) {
    return ( peak_point.equals(other.peak_point) );
    }

    public boolean group(AnnotationObject other) {
    if( canGroup(other) ) {
        peak_annotations.addAll(other.peak_annotations);
        structures.addAll(other.structures);
        return true;
    }
    return false;    
    }

    public Vector<AnnotationObject> ungroup() {
    Vector<AnnotationObject> ret = new Vector<AnnotationObject>();
    for(PeakAnnotation pa : peak_annotations) 
        ret.add(new AnnotationObject(peak_point,pa));
    return ret;        
    }

    public Vector<PeakAnnotation> getPeakAnnotations() {
    return peak_annotations;
    }

    public Vector<Glycan> getStructures() {
    return structures;
    }

    public boolean hasAnnotations() {
    for( PeakAnnotation pa : peak_annotations )
        if( pa.isAnnotated() )
        return true;
    return false;
    }

}