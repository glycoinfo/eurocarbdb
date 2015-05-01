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
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.datatransfer.*;

public class DictionarySelection extends GlycanSelection {

    static public final DataFlavor dictionaryFlavor = new DataFlavor("application/x-glycoworkbench-dictionary","application/x-glycoworkbench-dictionary"); 

    // data in this selection
    private Collection<StructureType> structure_types;
    
    public DictionarySelection(Data _data, Collection<StructureType> _structure_types, GlycanRenderer _glycanRenderer, Collection<Glycan> _structures) {
    super(_data,_glycanRenderer,_structures);
    structure_types = _structure_types;
    }
    
    public Vector<DataFlavor> getTransferDataFlavorsVector() {    
    Vector<DataFlavor> supportedFlavors = super.getTransferDataFlavorsVector();
    supportedFlavors.add(dictionaryFlavor);
    return supportedFlavors;
    }


    public DataFlavor [] getTransferDataFlavors () {    
    return getTransferDataFlavorsVector().toArray(new DataFlavor[0]);
    }
    
    public boolean isDataFlavorSupported (DataFlavor parFlavor) {
    return super.isDataFlavorSupported(parFlavor) || (parFlavor.equals(dictionaryFlavor) && structure_types!=null);
    }
    
    public synchronized Object getTransferData (DataFlavor parFlavor) throws UnsupportedFlavorException, IOException {
    if( parFlavor==null )
        throw new NullPointerException();
    
    if( parFlavor.equals(dictionaryFlavor) && structure_types!=null  )
        return getStream(toString().getBytes());
    
    return super.getTransferData(parFlavor);
    }

    public Collection<StructureType> getStructureTypes() {
    return structure_types;
    }

    
    public String toString() {
    if( structure_types==null )
        return "";

    StringBuilder sb = new StringBuilder();
    for( StructureType st : structure_types ) {
        sb.append(st.toString());
        sb.append('\n');
    }

    return sb.toString();    
    }

    public static Collection<StructureType> parseString(String str) throws Exception {
    Vector<StructureType> ret = new Vector<StructureType>();
    for( String token : str.split("\n") )
        ret.add(StructureType.fromString(token));
    return ret;
    }

}


