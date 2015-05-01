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
package org.eurocarbdb.resourcesdb.io;

import java.util.ArrayList;

import org.eurocarbdb.resourcesdb.monosaccharide.Monosaccharide;
import org.eurocarbdb.resourcesdb.template.TrivialnameTemplate;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;

/**
* Interface that defines the methods that are common to all monosaccharide exporters
* @author Thomas Luetteke
*
*/
public interface MonosaccharideExporter {
    
    /**
     * Generate the name string of a monosaccharide
     * @param ms: the monosaccharide to be exported
     * @return the string representation of the given monosaccharide
     * @throws ResourcesDbException in case a problem occurs in generating the name string
     */
    public String export(Monosaccharide ms) throws ResourcesDbException;
    
    /**
     * Generate a list of substituents, which are not included in the monosaccharide name but have to be treated as separate residues
     * @param ms: the monosaccharide, the substituents of which are checked
     * @return a list of substitutions, represented by SubstituentExchangeObjects
     * @throws ResourcesDbException
     */
    public ArrayList<SubstituentExchangeObject> getSeparateDisplaySubstituents(Monosaccharide ms) throws ResourcesDbException;
    
    /**
     * Generate a list of substituents, which are to be included in the monosaccharide name
     * @param ms: the monosaccharide, the substituents of which are checked
     * @return a list of substitutions, represented by SubstituentExchangeObjects
     * @throws ResourcesDbException
     */
    public ArrayList<SubstituentExchangeObject> getResidueIncludedSubstituents(Monosaccharide ms) throws ResourcesDbException;
    
    /**
     * Set the trivialname template that was used to generate the ms name string in the <code>export(Monosaccharide)</code> method.
     * @param triv the used TrivialnameTemplate to set
     */
    public void setUsedTrivialnameTemplate(TrivialnameTemplate triv);
    
    /**
     * Get the trivialname template that was used to generate the ms name string in the <code>export(Monosaccharide)</code> method.
     * @return the used TrivialnameTemplate or null if no trivial name was used to build the ms name 
     */
    public TrivialnameTemplate getUsedTrivialnameTemplate();
    
    /**
     * Set the Namescheme for this MonosaccharideExporter
     * @param scheme the GlycanNamescheme to set
     */
    public void setNamescheme(GlycanNamescheme scheme);
    
    /**
     * Get the GlycanNamescheme of this MonosaccharideExporter
     * @return the Namescheme
     */
    public GlycanNamescheme getNamescheme();
    
}
