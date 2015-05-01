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
package org.eurocarbdb.resourcesdb.template;

import java.util.ArrayList;

import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbObject;
import org.eurocarbdb.resourcesdb.monosaccharide.*;

public class MonosaccharideDictionaryEntry extends ResourcesDbObject {
    
    private String foreignName = null;
    private GlycanNamescheme scheme = null;
    private String basetypeStr = null;
    private ArrayList<Modification> substList = new ArrayList<Modification>();
    private Monosaccharide ms;
    
    //*****************************************************************************
    //*** Constructors: ***********************************************************
    //*****************************************************************************
    
    public MonosaccharideDictionaryEntry() {
        this(null, null);
    }
    
    public MonosaccharideDictionaryEntry(Config conf, TemplateContainer container) {
        this.setConfig(conf);
        this.setTemplateContainer(container);
        this.init();
    }
    
    //*****************************************************************************
    //*** Getters/Setters: ********************************************************
    //*****************************************************************************
    
    public String getForeignName() {
        return foreignName;
    }
    
    public void setForeignName(String foreignNameStr) {
        this.foreignName = foreignNameStr;
    }
    
    public GlycanNamescheme getScheme() {
        return scheme;
    }
    
    public void setScheme(GlycanNamescheme theScheme) {
        this.scheme = theScheme;
    }
    
    public String getBasetypeStr() {
        return basetypeStr;
    }
    
    public void setBasetypeStr(String basetypeName) {
        this.basetypeStr = basetypeName;
    }
    
    public ArrayList<Modification> getSubstList() {
        return substList;
    }
    
    public void setSubstList(ArrayList<Modification> substlist) {
        this.substList = substlist;
    }
    
    public void addSubstitution(Modification subst) {
        this.getSubstList().add(subst);
    }
    
    public Monosaccharide getMs() {
        if(ms == null) {
            ms = this.buildMs();
        }
        return ms;
    }
    
    public void setMs(Monosaccharide mono) {
        this.ms = mono;
    }
    
    //*****************************************************************************
    //*** other Methods: **********************************************************
    //*****************************************************************************
    
    public Monosaccharide buildMs() {
        Monosaccharide ms = null;
        try {
            ms = new Monosaccharide(GlycanNamescheme.GLYCOCT, this.getBasetypeStr(), this.getTemplateContainer(), this.getConfig());
            for(Modification mod : this.getSubstList()) {
                if(mod.getClass().equals(Substitution.class)) {
                    ms.addSubstitution((Substitution) mod);
                } else {
                    ms.addCoreModification((CoreModification) mod);
                }
            }
            //ms.setSubstitutions(this.getSubstList());
            ms.sortModifications();
            String stereo = ms.getStereoStrWithoutAnomeric();
            ms.setConfiguration(Stereocode.getConfigurationFromStereoString(stereo));
            ms.buildName();
            MonosaccharideValidation.checkMonosaccharideConsistency(ms, new TemplateContainer());
        } catch(Exception ex) {
            System.err.println("Exception in building monosaccharide from dictionary entry " + this);
            System.err.println(ex);
            //ex.printStackTrace();
        }
        return ms;
    }
    
    public void init() {
        this.setForeignName(null);
        this.setBasetypeStr(null);
        this.setScheme(null);
        this.setMs(null);
        if(this.getSubstList() == null) {
            this.setSubstList(new ArrayList<Modification>());
        } else {
            this.getSubstList().clear();
        }
    }
    
    public String toString() {
        String outStr = "";
        outStr += "[";
        outStr += this.getForeignName() + " (";
        if(this.getScheme() != null) {
            outStr += this.getScheme().getNameStr();
        } else {
            outStr += "unknown scheme";
        }
        outStr += "): ";
        outStr += this.getBasetypeStr();
        outStr += " - " + this.getSubstList().toString();
        outStr += "]";
        return outStr;
    }
}
