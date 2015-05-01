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

package org.eurocarbdb.application.glycoworkbench;

import org.eurocarbdb.application.glycanbuilder.*;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.datatransfer.*;

public class AnnotationSelection extends GlycanSelection {

    static public final DataFlavor annotationFlavor = new DataFlavor("application/x-glycoworkbench-annotations","application/x-glycoworkbench-annotations"); 

    // data in this selection
    private AnnotatedPeakList theAnnotations = null;

    public AnnotationSelection(Data _data) {
    super(_data);
    }

    public AnnotationSelection(Data _data, AnnotatedPeakList _annotations) {
    super(_data);
    theAnnotations = _annotations;
    }    

    public AnnotationSelection(Data _data, GlycanRenderer _glycanRenderer, Collection<Glycan> _structures) {
    super(_data,_glycanRenderer,_structures);
    }

    public AnnotationSelection(Data _data, AnnotatedPeakList _annotations, GlycanRenderer _glycanRenderer, Collection<Glycan> _structures) {
    super(_data,_glycanRenderer,_structures);
    theAnnotations = _annotations;
    }
    
    public Vector<DataFlavor> getTransferDataFlavorsVector() {    
    Vector<DataFlavor> supportedFlavors = super.getTransferDataFlavorsVector();
    supportedFlavors.add(annotationFlavor);
    return supportedFlavors;
    }


    public DataFlavor [] getTransferDataFlavors () {    
    return getTransferDataFlavorsVector().toArray(new DataFlavor[0]);
    }
    
    public boolean isDataFlavorSupported (DataFlavor parFlavor) {
    return super.isDataFlavorSupported(parFlavor) || (parFlavor.equals(annotationFlavor) && theAnnotations!=null);
    }
    
    public synchronized Object getTransferData (DataFlavor parFlavor) throws UnsupportedFlavorException, IOException {
    if( parFlavor==null )
        throw new NullPointerException();
    
    if( parFlavor.equals(annotationFlavor) && theAnnotations!=null  )
        return getStream(theAnnotations.toString().getBytes());
    
    return super.getTransferData(parFlavor);
    }

    public AnnotatedPeakList getAnnotations() {
    return theAnnotations;
    }

}


