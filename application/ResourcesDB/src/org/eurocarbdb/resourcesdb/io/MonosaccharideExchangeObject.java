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
* 
*/
package org.eurocarbdb.resourcesdb.io;

import java.util.ArrayList;

import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbObject;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbMonosaccharide;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;

/** 
* Data storage class to exchange monosaccharides between EuroCarbDB and ResourcesDB/MonoSaccharideDB
* @author Thomas Luetteke
*
*/
public class MonosaccharideExchangeObject extends ResourcesDbObject {

    private EcdbMonosaccharide basetype = null;
    private ArrayList<SubstituentExchangeObject> substituents = new ArrayList<SubstituentExchangeObject>();

    private boolean orientationChanged = false;

    private String monosaccharideName = null;
    private GlycanNamescheme monosaccharideNamescheme = null;
    
    private ArrayList<MonosaccharideExchangeObject.ResidueTypes> residueType;
    
    private Double avgMass;
    private Double monoMass;
    
    /**
     * List of the possible residue types to be stored in this object
     */
    public enum ResidueTypes {
        monosaccharide,
        substituent,
        aglycon;
    }
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    /** 
     * Constructor setting basetype and substituents
     * @param basetype The basetype to set, a monosaccharide object of the EuroCarbDB SugarObjectModel
     * @param substituents ArrayList of SubstituentExchangeObjects
     */
    public MonosaccharideExchangeObject(EcdbMonosaccharide basetype, ArrayList<SubstituentExchangeObject> substituents) {
        this(basetype, substituents, null, null);
    }
    
    public MonosaccharideExchangeObject(EcdbMonosaccharide basetype, ArrayList<SubstituentExchangeObject> substituents, Config conf, TemplateContainer container) {
        this.setConfig(conf);
        this.setTemplateContainer(container);
        setBasetype(basetype);
        setSubstituents(substituents);
        setMonosaccharideName(null);
        setMonosaccharideNamescheme(null);
        setOrientationChanged(false);
        this.residueType = new ArrayList<MonosaccharideExchangeObject.ResidueTypes>();
    }
    
    /** 
     * Constructor setting monosaccharide name and notation scheme
     * @param name the monosaccharide name
     * @param scheme the notation scheme in which the monosaccharide is encoded
     */
    public MonosaccharideExchangeObject(String name, GlycanNamescheme scheme) {
        this(name, scheme, null, null);
    }
    
    /** 
     * Constructor setting monosaccharide name and notation scheme
     * @param name the monosaccharide name
     * @param scheme the notation scheme in which the monosaccharide is encoded
     */
    public MonosaccharideExchangeObject(String name, GlycanNamescheme scheme, Config conf, TemplateContainer container) {
        this.setConfig(conf);
        this.setTemplateContainer(container);
        this.setBasetype(null);
        this.setSubstituents(new ArrayList<SubstituentExchangeObject>());
        this.setMonosaccharideName(name);
        this.setMonosaccharideNamescheme(scheme);
        this.setOrientationChanged(false);
        this.residueType = new ArrayList<MonosaccharideExchangeObject.ResidueTypes>();
    }
    
    /** 
     * Constructor generating empty object
     */
    public MonosaccharideExchangeObject() {
        this(Config.getGlobalConfig(), null);
    }
    
    /** 
     * Constructor generating empty object
     */
    public MonosaccharideExchangeObject(Config conf) {
        this((String)null, null, conf, null);
    }
    
    /** 
     * Constructor generating empty object
     */
    public MonosaccharideExchangeObject(Config conf, TemplateContainer container) {
        this((String)null, null, conf, container);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    /** 
     * Get the basetype of the current monosaccharide.
     * @return the basetype (a monosaccharide of the EuroCarbDB SugarObjectModel)
     */
    public EcdbMonosaccharide getBasetype() {
        return basetype;
    }

    /** 
     * Set the basetype of the monosaccharide to be exchanged
     * @param basetype the basetype to set (a monosaccharide of the EuroCarbDB SugarObjectModel)
     */
    public void setBasetype(EcdbMonosaccharide basetype) {
        this.basetype = basetype;
    }

    /** 
     * Get the value of the flag that marks a change of orientation of an alditol / aldaric acid residue during residue check
     * If the returned value is true, positions of attached monosaccharides have to adjusted.
     * @return true, if orientation was changed during notation check, otherwise false
     */
    public boolean isOrientationChanged() {
        return orientationChanged;
    }
    
    /** 
     * Set the flag for changed orientation of alditol / aldaric acid residue
     * This flag is set by the parsing routines implemented in MonoSaccharideDB.
     * @param orientationChanged
     */
    public void setOrientationChanged(boolean orientationChanged) {
        this.orientationChanged = orientationChanged;
    }
    
    /** 
     * Get the substituents for the current monosaccharide
     * For details of the format of the returned ArrayList see the description of the "setSubstituents" Method
     * @return ArrayList of HashMaps
     */
    public ArrayList<SubstituentExchangeObject> getSubstituents() {
        if(this.substituents == null) {
            setSubstituents(new ArrayList<SubstituentExchangeObject>());
        }
        return this.substituents;
    }
    
    /** 
     * Set a list of substituents for the current monosaccharide.
     * @param substituents ArrayList of SubstituentExchangeObjects 
     */
    public void setSubstituents(ArrayList<SubstituentExchangeObject> substituents) {
        this.substituents = substituents;
    }
    
    /** 
     * Add a substiutent to the substituents list
     * @param subst: the substituent to be added
     */
    public void addSubstituent(SubstituentExchangeObject subst) {
        getSubstituents().add(subst);
    }
    
    /**
     * Check, if the substituents list holds at least one substituent
     * @return true, if the substituents list is not empty; false, if it is empty or null
     */
    public boolean hasSubstituentsInList() {
        return(this.substituents != null && this.substituents.size() > 0);
    }

    /** 
     * Get the monosaccharide name
     * The monosaccharide name is formatted according the scheme set in setMonosaccharideNamescheme().
     * @return the monosaccharide name
     */
    public String getMonosaccharideName() {
        return monosaccharideName;
    }

    /** 
     * Set the monosaccharide name
     * The monosaccharide name must correspond to the naming scheme set in setMonosacharideNamescheme().
     * @param monosaccharideName the monosaccharideName to set
     */
    public void setMonosaccharideName(String monosaccharideName) {
        this.monosaccharideName = monosaccharideName;
    }

    /**
     * Get the name scheme used to encode the monosaccharide name (GlycoCT, CarbBank, KCF, ...)
     * @return the monosaccharideNamescheme
     */
    public GlycanNamescheme getMonosaccharideNamescheme() {
        return monosaccharideNamescheme;
    }

    /** 
     * Set the name scheme used to encode the monosaccharide name (GlycoCT, CarbBank, KCF, ...)
     * @param monosaccharideNamescheme the monosaccharideNamescheme to set
     */
    public void setMonosaccharideNamescheme(GlycanNamescheme monosaccharideNamescheme) {
        this.monosaccharideNamescheme = monosaccharideNamescheme;
    }

    /**
     * Get the (list of) residue type(s) of the residue stored in this object
     * The residue type is stored as a list rather than a single object because some residue names cannot be assigned to a certain residue type.
     * For example, "Me" could be both a substituent and an aglycon in CarbBank-like notations. This distinction can only be made with knowledge of the residue context (i.e. the carbohydrate structure), but not from the residue name alone
     * @return the residueType
     */
    public ArrayList<MonosaccharideExchangeObject.ResidueTypes> getResidueType() {
        if(this.residueType == null) {
            this.residueType = new ArrayList<MonosaccharideExchangeObject.ResidueTypes>();
        }
        return this.residueType;
    }

    /**
     * Add a residue type to the list of residue types of the residue stored in this object
     * @param residueType the residueType to be added
     */
    public void addResidueType(MonosaccharideExchangeObject.ResidueTypes residueType) {
        this.getResidueType().add(residueType);
    }
    
    /**
     * Clear the (list of) residue type(s) of the residue stored in this object
     */
    public void clearResidueType() {
        this.residueType.clear();
    }
    
    /**
     * Get the average mass
     * @return the average mass
     */
    public Double getAvgMass() {
        return avgMass;
    }

    /**
     * Set the average mass
     * @param avgMass the average mass to set
     */
    public void setAvgMass(Double avgMass) {
        this.avgMass = avgMass;
    }

    /**
     * Get the monoisotopic mass
     * @return the monoisotopic mass
     */
    public Double getMonoMass() {
        return monoMass;
    }

    /**
     * Set the monoisotopic mass
     * @param monoMass the monoisotopic mass to set
     */
    public void setMonoMass(Double monoMass) {
        this.monoMass = monoMass;
    }

    public String toString() {
        String outStr = "MonosaccharideExchangeObject [";
        outStr += "Scheme:" + this.getMonosaccharideNamescheme();
        outStr += " Name:" + this.getMonosaccharideName();
        outStr += " residueType:" + this.getResidueType().toString();
        if(this.getSubstituents() != null && this.getSubstituents().size() > 0) {
            outStr += " subst:" + this.getSubstituents().toString();
        }
        outStr += " Mass (avg/mono): " + this.getAvgMass() + "/" + this.getMonoMass();
        outStr += "]";
        return(outStr);
    }
    
}
