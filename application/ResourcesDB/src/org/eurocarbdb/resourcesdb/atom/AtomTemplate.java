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
package org.eurocarbdb.resourcesdb.atom;

import org.eurocarbdb.resourcesdb.ResourcesDbException;

/**
* Templates for basetype atoms.
* This enum contains templates that are used for generating basetype atom names.
* The <code>nameTemplate</code> field allows two variables: <code>${pos}</code> and <code>${abc}</code>.
* The former marks the position of the atom within the basetype backbone, the latter is used to distinguish multiple atoms of the same element type at one position
* (e.g. the H6a, H6b and H6c atom of a 6-deoxy hexose).
* 
* @author Thomas Luetteke
*
*/
public enum AtomTemplate {
    
    BB_C("BB_C", Periodic.C, "C${pos}", null, "backbone carbon"),
    BB_O("BB_O", Periodic.O, "O${pos}", null, "backbone oxygen"),
    BB_OR("BB_OR", Periodic.O, "O${pos}", null, "backbone ring oxygen"),
    BB_H("BB_H", Periodic.H, "H${pos}", "H", "backbone hydrogen (single), carbon-linked"),
    BB_HO("BB_HO", Periodic.H, "HO${pos}", "H", "backbone hydroxyl hydrogen"),
    BB_HX("BB_HX", Periodic.H, "H${pos}${abc}", "H", "backbone hydrogen (multiple H at one C), carbon-linked"),
    BB_HR("BB_HR", Periodic.H, "H${pos}r", "H", "backbone hydrogen, pro-R-configuration"),
    BB_HS("BB_HS", Periodic.H, "H${pos}s", "H", "backbone hydrogen, pro-S-configuration"),
    COOH_C_OH("COOH_C", Periodic.C, "C${pos}", null, "carboxyl carbon with protonated oxygen"),
    COOH_C_OX("COOH_C", Periodic.C, "C${pos}", null, "carboxyl carbon with deprotonated oxygen"),
    COOH_O("COOH_O", Periodic.O, "O${pos}", null, "carboxyl oxygen, double bonded"),
    COOH_OH("COOH_OH", Periodic.O, "OH${pos}", null, "carboxyl oxygen, protonated"),
    COOH_OX("COOH_OX", Periodic.O, "O${pos}${abc}", null, "carboxyl oxygen of a deprotonated carboxyl group"),
    COOH_H("COOH_H", Periodic.H, "HO${pos}", "H", "carboxyl hydrogen"),
    SUBST_X("SUBST_X", null, "", null, "substituent atom");
    
    private String tmplName = null;
    private Periodic element = null;
    private String nameTemplate = null;
    private String mol2Type = null;
    private String description = null;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    /**
     * private constructor
     * @param elementSymbol symbol of the atom's periodic element
     * @param nameTmpl the template for the atom's name
     * @param mol2type the mol2 atom type
     * @param desc the atom template description
     */
    private AtomTemplate(String name, Periodic element, String nameTmpl, String mol2type, String desc) {
        this.setTmplName(name);
        this.setElement(element);
        this.setNameTemplate(nameTmpl);
        this.setMol2Type(mol2type);
        this.setDescription(desc);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public String getTmplName() {
        return tmplName;
    }

    private void setTmplName(String tmplName) {
        this.tmplName = tmplName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Periodic getElement() {
        return element;
    }
    
    private void setElement(Periodic element) {
        this.element = element;
    }
    
    public String getNameTemplate() {
        return nameTemplate;
    }
    
    public void setNameTemplate(String nameTemplate) {
        this.nameTemplate = nameTemplate;
    }
    
    public String getMol2Type() {
        return mol2Type;
    }

    public void setMol2Type(String mol2Type) {
        this.mol2Type = mol2Type;
    }

    //*****************************************************************************
    //*** atom name related methods: **********************************************
    //*****************************************************************************
    
    /**
     * Build the atom name for an atom that does not require any further parameters
     * @return the nameTemplate String of this AtomTemplate
     * @throws ResourcesDbException in case a parameter (e.g. the position) is required to build the atom name
     */
    public String formatAtomName() throws ResourcesDbException {
        String nameStr = this.getNameTemplate();
        if(nameStr == null) {
            return "";
        }
        if(nameStr.indexOf("$") >= 0) { //*** name template requires parameters ***/
            throw new ResourcesDbException("required parameter for atom name missing in " + nameStr);
        }
        return nameStr;
    }
    
    /**
     * Build the atom name for an atom that includes the position within the monosaccharide backbone
     * @param pos the position
     * @return the formatted atom name
     * @throws ResourcesDbException in case the <code>nameTemplate</code> String of this atom template requires further parameters than just the position.
     */
    public String formatAtomName(int pos) throws ResourcesDbException {
        String nameStr = this.getNameTemplate();
        if(nameStr == null) {
            return "";
        }
        /*if(nameStr.indexOf("${pos}") == -1) {
            throw new ResourcesDbException("position parameter not applicable to atom name template " + nameStr);
        }*/
        while(nameStr.indexOf("${pos}") >= 0) {
            int index = nameStr.indexOf("${pos}");
            nameStr = nameStr.substring(0, index) + Integer.toString(pos) + nameStr.substring(index + 6);
        }
        if(nameStr.indexOf("$") >= 0) { //*** name template requires further parameters ***/
            throw new ResourcesDbException("required parameter for atom name missing in " + nameStr);
        }
        return nameStr;
    }

    /**
     * Build the atom name for an atom that includes the position within the monosaccharide backbone and a further index parameter
     * @param pos the position
     * @param charIndex the second parameter to distinguish multiple atoms of the same type at one position
     * @return the formatted atom name
     * @throws ResourcesDbException in case the <code>nameTemplate</code> String of this atom template requires further parameters.
     */
    public String formatAtomName(int pos, int charIndex) throws ResourcesDbException {
        String nameStr = this.getNameTemplate();
        if(nameStr == null) {
            return "";
        }
        /*if(nameStr.indexOf("${pos}") == -1) {
            throw new ResourcesDbException("position parameter not applicable to atom name template " + nameStr);
        }*/
        while(nameStr.indexOf("${pos}") >= 0) {
            int index = nameStr.indexOf("${pos}");
            nameStr = nameStr.substring(0, index) + Integer.toString(pos) + nameStr.substring(index + 6);
        }
        String abcStr = String.valueOf((char)('a' + charIndex));
        while(nameStr.indexOf("${abc}") >= 0) {
            int index = nameStr.indexOf("${abc}");
            nameStr = nameStr.substring(0, index) + abcStr + nameStr.substring(index + 6);
        }
        if(nameStr.indexOf("$") >= 0) { //*** name template requires further parameters ***/
            throw new ResourcesDbException("required parameter for atom name missing in " + nameStr);
        }
        return nameStr;
    }

    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public static AtomTemplate forName(String name) {
        for(AtomTemplate atmpl : AtomTemplate.values()) {
            if(atmpl.getTmplName().equalsIgnoreCase(name)) {
                return atmpl;
            }
        }
        return null;
    }
    
}
