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
package org.eurocarbdb.resourcesdb;

/**
* This enumberation holds a list of the notation schemes used within the ResourcesDB project.
*
* @author Thomas Lütteke
*/
public enum GlycanNamescheme {
    GLYCOCT("GlycoCT", true),
    CARBBANK("CarbBank", false),
    IUPAC("IUPAC", GlycanNamescheme.CARBBANK, false),
    GLYCOSCIENCES("Glycosciences", GlycanNamescheme.CARBBANK, false),
    BCSDB("BCSDB", true),
    GLYDE("Glyde", true),
    SWEET2("Sweet2", GlycanNamescheme.CARBBANK, false),
    KEGG("KEGG", false),
    CFG("CFG", true),
    MONOSACCHARIDEDB("MsDb", GlycanNamescheme.GLYCOCT, true),
    GWB("GlycoWorkBench", GlycanNamescheme.CARBBANK, false),
    PDB("Protein Data Bank", false),
    CCPN("CCPN", false),
    GLYCAM("GlyCam", true),
    AUTO("auto-detect", false);
    
    /**
     * The name of the notation scheme (as e.g. used in database fields)
     */
    private String nameStr;
    
    /**
     * The notation scheme this scheme is based on.
     * This information is mainly needed to select the correct parsing / encoding classes.
     * For instance, all notations with the baseScheme CARBBANK can be parsed with the <code>CarbbankImporter</code> class.
     */
    private GlycanNamescheme baseScheme;
    
    /**
     * Flag to mark if the notation scheme is case sensitive or not
     */
    private boolean isCaseSensitive;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    /**
     * private constructor
     * @param name the name of the notation scheme
     */
    private GlycanNamescheme(String name, boolean isCaseSens) {
        this.nameStr = name;
        this.baseScheme = this;
        this.isCaseSensitive = isCaseSens;
    }
    
    /**
     * private constructor
     * @param name the name of the notation scheme
     * @param base the notation scheme the parser / encoder of which is to be used for this scheme
     */
    private GlycanNamescheme(String name, GlycanNamescheme base, boolean isCaseSens) {
        this.nameStr = name;
        this.baseScheme = base;
        this.isCaseSensitive = isCaseSens;
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    /**
     * @return the nameStr
     */
    public String getNameStr() {
        return this.nameStr;
    }
    
    public GlycanNamescheme getBaseScheme() {
        return this.baseScheme;
    }
    
    public boolean isCaseSensitive() {
        return this.isCaseSensitive;
    }

    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public static GlycanNamescheme forName(String name) {
        for(GlycanNamescheme scheme : GlycanNamescheme.values()) {
            if(scheme.getNameStr().equalsIgnoreCase(name) || scheme.name().equalsIgnoreCase(name)) {
                return(scheme);
            }
        }
        return(null);
    }
    
    public static GlycanNamescheme getGlycanNameschemeByNamestr(String name) {
        return forName(name);
    }
    
}
