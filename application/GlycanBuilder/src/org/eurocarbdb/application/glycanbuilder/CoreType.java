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

package org.eurocarbdb.application.glycanbuilder;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

/**
   This class contains the information about a core structure. A core
   structure is a common motif found at the reducing end of glycan
   structures.

   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/


public class CoreType {

    //

    protected String  name;
    protected String  superclass;
    protected String  structure;
    protected String  description;

    //--

    /**
       Create a new core type from an initialization string.
       @throws Exception if the string is in the wrong format
    */
    public CoreType(String init) throws Exception {
    Vector<String> tokens = TextUtils.tokenize(init,"\t");
    if( tokens.size()!=4 ) 
        throw new Exception("Invalid string format: " + init);

    name            = tokens.elementAt(0);
    superclass      = tokens.elementAt(1);
    structure       = tokens.elementAt(2);
    description     = tokens.elementAt(3);
    }

    /**
       Return the identifier of the core type.
     */
    public String getName() {
    return name;
    }

    /**
       Return the class of this core type.
     */
    public String getSuperclass() {
    return superclass;
    }

    /**
       Return the string representation of the structure associated
       with this core type.
     */
    public String getStructure() {
    return structure;
    }
    
    /**
       Return a description of this core type.
     */
    public String getDescription() {
    return description;
    }

    /**
       Create a new structure from this core type.
     */
    public Residue newCore() throws Exception {
    return new GWSParser().readSubtree(structure,false);
    }
}