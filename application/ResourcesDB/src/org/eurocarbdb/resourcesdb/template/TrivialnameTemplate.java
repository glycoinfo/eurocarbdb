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
import java.util.HashMap;

import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.monosaccharide.CoreModification;
import org.eurocarbdb.resourcesdb.monosaccharide.CoreModificationTemplate;
import org.eurocarbdb.resourcesdb.monosaccharide.MonosaccharideException;
import org.eurocarbdb.resourcesdb.monosaccharide.Substitution;

public class TrivialnameTemplate extends BasetypeTemplate {
    
    private ArrayList<CoreModification> coreModifications;
    
    private ArrayList<Substitution> substitutions;
    private int substitutionCount;
    
    private boolean defaultConfigIsCompulsory;
    
    private ArrayList<GlycanNamescheme> nameschemes;
    private HashMap<String, HashMap<GlycanNamescheme, Boolean>> namesMap;
    private HashMap<GlycanNamescheme, ArrayList<String>> schemesMap;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public TrivialnameTemplate() {
        init();
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public ArrayList<CoreModification> getCoreModifications() {
        return this.coreModifications;
    }

    public void setCoreModifications(ArrayList<CoreModification> modifications) {
        this.coreModifications = modifications;
    }
    
    public void addCoreModification(CoreModification mod) throws MonosaccharideException {
        if(this.coreModifications == null) {
            this.setCoreModifications(new ArrayList<CoreModification>());
        }
        
        for(int i = 0; i < mod.getPositions().size(); i++) {
            for(int m = 0; m < this.coreModifications.size(); m++) {
                CoreModification exstMod = this.coreModifications.get(m);
                if(exstMod.equals(mod)) {
                    return;
                }
                if(!mod.isSubstitutable()) {
                    if(exstMod.isSubstitutable()) {
                        continue;
                    }
                    if(exstMod.getPositions().contains(mod.getPositions().get(i))) {
                        throw new MonosaccharideException("Trivialname Template " + this.getLongName() + " modified twice at position " + mod.getPositions().get(i).intValue());
                    }
                }
            }
        }
        
        this.coreModifications.add(mod);
        
    }
    
    /**
     * Test, if a given core modification is present in the trivialname template.
     * @param mod: The modification to be checked for.
     * @return true, if the modification is present, otherwise false.
     */
    public boolean hasCoreModification(CoreModification mod) {
        for(CoreModification presentMod : this.getCoreModifications()) {
            if(presentMod.equals(mod)) {
                return(true);
            }
        }
        return(false);
    }
    
    /**
     * Count the core modifications of a given type present in the trivialname template
     * @param tmpl: The core modification template to identify the modification type
     * @return the number of modifications of this type
     */
    public int countCoreModifications(CoreModificationTemplate tmpl) {
        int count = 0;
        for(CoreModification tmplMod : this.getCoreModifications()) {
            if(tmplMod.getTemplate().equals(tmpl)) {
                count ++;
            }
        }
        return count;
    }
    
    /**
     * Get the number of core modifications present in the trivialname template
     * @return the coreModificationCount
     */
    public int getCoreModificationCount() {
        return this.getCoreModifications().size();
    }

    /**
     * @return the substitutionCount
     */
    public int getSubstitutionCount() {
        return this.substitutionCount;
    }

    /**
     * @param substCount the substitutionCount to set
     */
    public void setSubstitutionCount(int substCount) {
        this.substitutionCount = substCount;
    }

    /**
     * @return the substitutions
     */
    public ArrayList<Substitution> getSubstitutions() {
        return this.substitutions;
    }
    
    public ArrayList<Substitution> getSubstitutionsClone() {
        ArrayList<Substitution> listClone = new ArrayList<Substitution>();
        for(Substitution subst: this.substitutions) {
            listClone.add(subst.clone());
        }
        return listClone;
    }
    
    public Substitution getSubstitutionByPosition(int position) {
        for(Substitution subst : this.getSubstitutions()) {
            if(subst.getIntValuePosition1() == position) {
                return(subst);
            }
        }
        return(null);
    }

    public Substitution getSubstitutionByPosition(int position, LinkageType linktype) {
        for(Substitution subst : this.getSubstitutions()) {
            if(subst.getIntValuePosition1() == position) {
                if(subst.getLinkagetype1().equals(linktype)) {
                    return(subst);
                }
            }
        }
        return(null);
    }

    /**
     * @param substitutions the substitutions to set
     */
    public void setSubstitutions(ArrayList<Substitution> substList) {
        this.substitutions = substList;
        this.setSubstitutionCount(this.substitutions.size());
    }

    public void addSubstitution(Substitution subst) throws MonosaccharideException {
        if(this.getSubstitutions() == null) {
            this.setSubstitutions(new ArrayList<Substitution>());
        }
        
        for(int i = 0; i < subst.getPositions().size(); i++) {
            for(int m = 0; m < this.getSubstitutions().size(); m++) {
                Substitution exstSubst = this.getSubstitutions().get(m);
                if(exstSubst.equals(subst)) {
                    return;
                }
                if(exstSubst.getPositions().contains(subst.getPositions().get(i))) {
                    throw new MonosaccharideException("Trivialname Template " + this.getLongName() + " modified twice at position " + subst.getPositions().get(i).intValue());
                }
            }
        }
        
        this.getSubstitutions().add(subst);
        
        this.setSubstitutionCount(this.getSubstitutionCount() + 1);
    }
    
    /**
     * Test, if a given substitution is present in the trivialname template.
     * @param subst: The substitution to be checked for.
     * @return true, if the substitution is present, otherwise false.
     */
    public boolean hasSubstitution(Substitution subst) {
        for(Substitution presentSubst : this.getSubstitutions()) {
            if(presentSubst.equals(subst)) {
                return(true);
            }
        }
        return(false);
    }
    
    /**
     * Get info if the default configuration is a compulsory property (like in Neu) or the more frequent alternative (like in Fuc)
     * @return the defaultConfigIsCompulsory
     */
    public boolean isDefaultConfigIsCompulsory() {
        return this.defaultConfigIsCompulsory;
    }

    /**
     * Set flag to mark whether the default configuration is a compulsory property (like in Neu) or the more frequent alternative (like in Fuc)
     * @param flag the defaultConfigIsCompulsory to set
     */
    public void setDefaultConfigIsCompulsory(boolean flag) {
        this.defaultConfigIsCompulsory = flag;
    }
    
    //*****************************************************************************
    //*** getters/setters for namescheme handling: ********************************
    //*****************************************************************************
    
    /**
     * Get a list of the name schemes in which the trivial name can be used.
     * @return the nameschemes
     */
    public ArrayList<GlycanNamescheme> getNameschemes() {
        if(this.nameschemes == null) {
            this.nameschemes = new ArrayList<GlycanNamescheme>();
        }
        return this.nameschemes;
    }

    public HashMap<GlycanNamescheme, ArrayList<String>> getSchemesMap() {
        if(this.schemesMap == null) {
            this.schemesMap = new HashMap<GlycanNamescheme, ArrayList<String>>();
        }
        return this.schemesMap;
    }
    
    private HashMap<String, HashMap<GlycanNamescheme, Boolean>> getNamesMap() {
        if(this.namesMap == null) {
            this.namesMap = new HashMap<String, HashMap<GlycanNamescheme, Boolean>>();
        }
        return this.namesMap;
    }
    
    public void addName(String name, GlycanNamescheme scheme, Boolean isPrimary) {
        ArrayList<GlycanNamescheme> schemesList = this.getNameschemes();
        if(!schemesList.contains(scheme)) {
            schemesList.add(scheme);
        }
        ArrayList<String> namesList = this.getSchemesMap().get(scheme);
        if(namesList == null) {
            namesList = new ArrayList<String>();
            this.getSchemesMap().put(scheme, namesList);
        }
        if(!namesList.contains(name)) {
            namesList.add(name);
        }
        HashMap<String, HashMap<GlycanNamescheme, Boolean>> namesMap = this.getNamesMap();
        HashMap<GlycanNamescheme, Boolean> nameStringMap = namesMap.get(name);
        if(nameStringMap == null) {
            nameStringMap = new HashMap<GlycanNamescheme, Boolean>();
            namesMap.put(name, nameStringMap);
        }
        nameStringMap.put(scheme, isPrimary);
    }
    
    /**
     * Check, if the trivial name is the primary name for the monosaccharide in a given name scheme
     * @param scheme the name scheme to check
     * @param name the name to check
     * @return true, if the trivial name is the primary name, otherwise false
     */
    public boolean isPrimaryName(GlycanNamescheme scheme, String name) {
        HashMap<GlycanNamescheme, Boolean> primaryMap = this.getNamesMap().get(name);
        if(primaryMap != null) {
            Boolean flagObj = primaryMap.get(scheme);
            if(flagObj != null) {
                return(flagObj.booleanValue());
            }
        }
        return(false);
    }
    
    public String getPrimaryName(GlycanNamescheme scheme) {
        ArrayList<String> namesList = this.getNamesList(scheme);
        if(namesList != null) {
            for(String name : namesList) {
                if(this.isPrimaryName(scheme, name)) {
                    return name;
                }
            }
        }
        return null;
    }
    
    public boolean isTrivialName(GlycanNamescheme scheme, String name) {
        HashMap<GlycanNamescheme, Boolean> primaryMap = this.getNamesMap().get(name);
        if(primaryMap != null) {
            Boolean flagObj = primaryMap.get(scheme);
            return flagObj != null;
        }
        return(false);
    }
    
    public ArrayList<String> getNamesList(GlycanNamescheme scheme) {
        return this.getSchemesMap().get(scheme);
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        super.init();
        this.setCoreModifications(new ArrayList<CoreModification>());
        this.setSubstitutions(new ArrayList<Substitution>());
        this.setIsSuperclass(false);
    }
    
    public String toString() {
        String outStr = "";
        outStr += this.getLongName();
        outStr += " [" + this.getSize() + "|" + this.getStereocode() + "|" + this.getDefaultConfiguration() + "|" + this.getDefaultRingend() + "]";
        String modStr = "";
        if(this.getCoreModifications() != null) {
            for(int i = 0; i < this.getCoreModifications().size(); i++) {
                modStr += this.getCoreModifications().get(i).toString();
            }
        }
        String substStr = "";
        if(this.getSubstitutions() != null) {
            for(Substitution subst : this.getSubstitutions()) {
                substStr += subst.toString();
            }
        }
        if(this.getCoreModificationCount() > 0) {
            outStr += " CoreModifications: " + modStr;
        }
        if(this.getSubstitutionCount() > 0) {
            outStr += " Substitutions: " + substStr;
        }
        return(outStr);
    }

}
