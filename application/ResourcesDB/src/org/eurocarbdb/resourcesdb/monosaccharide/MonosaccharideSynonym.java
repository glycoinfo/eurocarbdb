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
package org.eurocarbdb.resourcesdb.monosaccharide;

import java.util.ArrayList;
import java.util.List;

import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.ResourcesDbObject;
import org.eurocarbdb.resourcesdb.io.MonosaccharideExchangeObject;
import org.eurocarbdb.resourcesdb.io.SubstituentExchangeObject;

/**
* Class to store monosaccharide alias names
* @author Thomas Luetteke
*
*/
public class MonosaccharideSynonym extends ResourcesDbObject {
    
    private int msId;
    private GlycanNamescheme namescheme;
    private String name;
    private boolean isPrimary;
    private boolean isTrivialName;
    private int synonymDbId;
    private List<Substitution> externalSubstList;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public MonosaccharideSynonym() {
        this.init();
    }
    
    public MonosaccharideSynonym(GlycanNamescheme scheme) {
        this.init();
        this.setNamescheme(scheme);
    }
    
    public MonosaccharideSynonym(GlycanNamescheme scheme, String nameStr) {
        this.init();
        this.setNamescheme(scheme);
        this.setName(nameStr);
    }
    
    public MonosaccharideSynonym(GlycanNamescheme scheme, String nameStr, boolean isPrimary) {
        this.init();
        this.setNamescheme(scheme);
        this.setName(nameStr);
        this.setIsPrimary(isPrimary);
    }
    
    public MonosaccharideSynonym(MonosaccharideExchangeObject msObj) throws ResourcesDbException {
        this.setConfig(msObj.getConfig());
        this.setTemplateContainer(msObj.getTemplateContainer());
        this.init();
        this.setNamescheme(msObj.getMonosaccharideNamescheme());
        this.setName(msObj.getMonosaccharideName());
        for(SubstituentExchangeObject substEx : msObj.getSubstituents()) {
            Substitution subst = new Substitution(substEx, msObj.getTemplateContainer());
            subst.setNameWithoutTemplateAdjustment(substEx.getName());
            this.addExternalSubst(subst);
        }
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public boolean isPrimary() {
        return(this.getIsPrimary());
    }
    
    public boolean getIsPrimary() {
        return this.isPrimary;
    }
    
    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
    
    public boolean isTrivialName() {
        return(this.getIsTrivialName());
    }
    
    public boolean getIsTrivialName() {
        return this.isTrivialName;
    }
    
    public void setIsTrivialName(boolean isTrivialName) {
        this.isTrivialName = isTrivialName;
    }
    
    public int getMsId() {
        return this.msId;
    }
    
    public void setMsId(int msId) {
        this.msId = msId;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public GlycanNamescheme getNamescheme() {
        return this.namescheme;
    }
    
    public void setNamescheme(GlycanNamescheme namescheme) {
        this.namescheme = namescheme;
    }
    
    public String getNameschemeStr() {
        if(this.getNamescheme() == null) {
            return(null);
        }
        return(this.getNamescheme().name());
    }
    
    public void setNameschemeStr(String name) {
        if(name == null) {
            this.setNamescheme(null);
        } else {
            this.setNamescheme(GlycanNamescheme.forName(name));
        }
    }
    
    public int getSynonymDbId() {
        return this.synonymDbId;
    }

    public void setSynonymDbId(int synonymDbId) {
        this.synonymDbId = synonymDbId;
    }

    public List<Substitution> getExternalSubstList() {
        if(this.externalSubstList == null) {
            this.externalSubstList = new ArrayList<Substitution>();
        }
        return this.externalSubstList;
    }

    public void setExternalSubstList(List<Substitution> externalSubstList) {
        this.externalSubstList = externalSubstList;
    }
    
    public void addExternalSubst(Substitution subst) {
        this.getExternalSubstList().add(subst);
    }

    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        this.setMsId(0);
        this.setIsPrimary(true);
        this.setIsTrivialName(false);
        this.setName(null);
        this.setNamescheme(null);
        this.setExternalSubstList(new ArrayList<Substitution>());
    }
    
    /**
     * Check, if this MonosaccharideSynonym equals a given object.
     * @param o the Object to compare
     * @return
     */
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(!o.getClass().equals(MonosaccharideSynonym.class)) {
            return false;
        }
        MonosaccharideSynonym testAlias = (MonosaccharideSynonym) o;
        if(testAlias.isPrimary() != this.isPrimary()) {
            return false;
        }
        if(testAlias.isTrivialName() != this.isTrivialName()) {
            return false;
        }
        return this.equalsIgnoreBooleans(testAlias);
    }
    
    /**
     * Check, if this MonosaccharideSynonym equals a given object.
     * The boolean flags indicating if the synonym is the primary one or if it is a trivial name are ignored.
     * @param o the Object to compare
     * @return
     */
    public boolean equalsIgnoreBooleans(Object o) {
        if(o == null) {
            return false;
        }
        if(!o.getClass().equals(MonosaccharideSynonym.class)) {
            return false;
        }
        MonosaccharideSynonym testAlias = (MonosaccharideSynonym) o;
        if(!testAlias.getNamescheme().equals(this.getNamescheme())) {
            return false;
        }
        if(this.getNamescheme().isCaseSensitive()) {
            if(!testAlias.getName().equals(this.getName())) {
                return false;
            }
        } else {
            if(!testAlias.getName().equalsIgnoreCase(this.getName())) {
                return false;
            }
        }
        if(this.getExternalSubstList().size() != testAlias.getExternalSubstList().size()) {
            return false;
        }
        for(int i = 0; i < this.getExternalSubstList().size(); i++) {
            if(!this.getExternalSubstList().get(i).equals(testAlias.getExternalSubstList().get(i))) {
                return false;
            }
        }
        return true;
    }
    
    public String toString() {
        String outStr = this.getNamescheme().getNameStr() + "::" + this.getName();
        outStr += " subst ";
        outStr += this.getExternalSubstList().toString();
        if(this.getIsTrivialName()) {
            outStr += " (trivial name)";
        }
        if(this.getIsPrimary()) {
            outStr += " (primary)";
        }
        return(outStr);
    }
    
}
