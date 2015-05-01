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
import java.util.List;

import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.GlycoconjugateException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.monosaccharide.MonosaccharideException;
import org.eurocarbdb.resourcesdb.monosaccharide.SubstituentAlias;
import org.eurocarbdb.resourcesdb.monosaccharide.SubstituentSubpartTreeNode;

/**
* Class to store the templates for parsing / managing monosaccharide substituents
* @author Thomas Lütteke
*
*/
public class SubstituentTemplate extends NonBasetypeTemplate {
    
    public static String AMINOTEMPLATENAME = "amino";
    
    private boolean canReplaceRingOxygen = false;
    private boolean isLinkable = false;
    private String ringOxygenPositionName = null;
    private String haworthName;
    private String mirroredHaworthName;
    private String oLinkedEquivalent;
    private HashMap<GlycanNamescheme, HashMap<LinkageType, SubstituentAlias>> primaryAliasMap = new HashMap<GlycanNamescheme, HashMap<LinkageType, SubstituentAlias>>();
    private List<SubstituentAlias> aliasList;
    private SubstituentSubpartTreeNode subparts;
    
    private int dbId;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public SubstituentTemplate() {
        init();
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public String getHaworthName() {
        return this.haworthName;
    }

    public void setHaworthName(String haworthName) {
        this.haworthName = haworthName;
    }
    
    public String getMirroredHaworthName() {
        return this.mirroredHaworthName;
    }

    public void setMirroredHaworthName(String mirroredName) {
        this.mirroredHaworthName = mirroredName;
    }

    public String getRingOxygenPositionName() {
        return this.ringOxygenPositionName;
    }

    public void setRingOxygenPositionName(String ropName) {
        this.ringOxygenPositionName = ropName;
    }

    /**
     * @return the defaultLinkagetype1
     */
    public LinkageType getDefaultLinkagetype1() {
        if(this.getValidLinkingPosition(this.getDefaultLinkingPosition1()) != null) {
            return this.getValidLinkingPosition(this.getDefaultLinkingPosition1()).getDefaultLinktype();
        }
        return null;
    }

    /**
     * @return the defaultLinkagetype2
     */
    public LinkageType getDefaultLinkagetype2() {
        if(this.getValidLinkingPosition(this.getDefaultLinkingPosition2()) != null) {
            return this.getValidLinkingPosition(this.getDefaultLinkingPosition2()).getDefaultLinktype();
        }
        return null;
    }

    /**
     * @return the canReplaceRingOxygen
     */
    public boolean isCanReplaceRingOxygen() {
        return this.canReplaceRingOxygen;
    }

    /**
     * @param canReplaceRingOxygen the canReplaceRingOxygen to set
     */
    public void setCanReplaceRingOxygen(boolean canReplaceRingOxygen) {
        this.canReplaceRingOxygen = canReplaceRingOxygen;
    }
    
    public SubstituentAlias getPrimaryAlias(GlycanNamescheme namescheme, LinkageType linktype) throws ResourcesDbException {
        HashMap<LinkageType, SubstituentAlias> tempMap = this.primaryAliasMap.get(namescheme);
        if(tempMap == null) {
            tempMap = this.primaryAliasMap.get(namescheme.getBaseScheme());
            if(tempMap == null) {
                throw new ResourcesDbException("No primary alias set for substituent " + this.getName() + " and namescheme " + namescheme);
            }
        }
        SubstituentAlias alias = tempMap.get(linktype);
        if(alias == null) {
            throw new ResourcesDbException("No primary alias set for substituent " + this.getName() + ", namescheme " + namescheme + " and linkage type " + linktype);
        }
        return(alias);
    }
    
    private HashMap<LinkageType, SubstituentAlias> getPrimaryAliasMap(GlycanNamescheme namescheme) {
        return this.primaryAliasMap.get(namescheme);
    }
    
    public void setPrimaryAlias(GlycanNamescheme namescheme, LinkageType link, SubstituentAlias alias) {
        HashMap<LinkageType, SubstituentAlias> tempMap = getPrimaryAliasMap(namescheme);
        if(tempMap == null) {
            tempMap = new HashMap<LinkageType, SubstituentAlias>();
            this.primaryAliasMap.put(namescheme, tempMap);
        }
        tempMap.put(link, alias);
    }
    
    /**
     * @return the aliasList
     */
    public List<SubstituentAlias> getAliasList() {
        return this.aliasList;
    }
    
    /**
     * Get a list of synonyms for this substituent with the given notation scheme and linkage type
     * @param scheme the notation scheme (or null to match any scheme)
     * @param linktype the linkage type (or null to match any type)
     * @return a list of substituent aliases
     */
    public List<SubstituentAlias> getAliasList(GlycanNamescheme scheme, LinkageType linktype) {
        List<SubstituentAlias> retList = new ArrayList<SubstituentAlias>();
        for(SubstituentAlias substAlias : this.getAliasList()) {
            if(scheme != null && !scheme.equals(substAlias.getNamescheme())) {
                continue;
            }
            if(linktype != null && !linktype.equals(substAlias.getLinktype1())) {
                continue;
            }
            retList.add(substAlias);
        }
        return retList;
    }

    /**
     * @param aliasList the aliasList to set
     */
    public void setAliasList(List<SubstituentAlias> aliasList) {
        this.aliasList = aliasList;
    }
    
    public void addAlias(SubstituentAlias alias) {
        List<SubstituentAlias> aliases = getAliasList();
        if(aliases == null) {
            aliases = new ArrayList<SubstituentAlias>();
            setAliasList(aliases);
        }
        alias.setPrimaryTemplate(this);
        aliases.add(alias);
        if(alias.isPrimary()) {
            this.setPrimaryAlias(alias.getNamescheme(), alias.getLinktype1(), alias);
        }
    }
    
    public String getSeparateDisplay(GlycanNamescheme scheme, LinkageType linktype) throws ResourcesDbException {
        if(scheme == null) {
            throw new MonosaccharideException("Namescheme must not be null in getSeparateDisplay(scheme, linktype)");
        }
        if(linktype == null) {
            throw new MonosaccharideException("Linkage type must not be null in getSeparateDisplay(scheme, linktype)");
        }
        SubstituentAlias primAlias = this.getPrimaryAlias(scheme, linktype);
        if(primAlias == null) {
            //throw new MonosaccharideException("No primary alias set for substituent " + this.getName() + ", notation scheme " + scheme.getNameStr() + ", linkage type " + linktype.name());
            return null;
        }
        return(primAlias.getSeparateDisplayName());
    }
    
    public SubstituentTemplate getSeparateDisplayTemplate(GlycanNamescheme scheme, LinkageType linktype, SubstituentTemplateContainer stContainer) throws ResourcesDbException {
        String separateName = getSeparateDisplay(scheme, linktype);
        if(separateName != null) {
            return stContainer.forName(scheme, separateName);
        }
        return null;
    }
    
    public String getResidueIncludedName(GlycanNamescheme scheme, LinkageType linktype) throws ResourcesDbException {
        if(scheme == null) {
            throw new MonosaccharideException("Namescheme must not be null in getResidueIncludedName(scheme, linktype)");
        }
        if(linktype == null) {
            throw new MonosaccharideException("Linkage type must not be null in getResidueIncludedName(scheme, linktype)");
        }
        SubstituentAlias primAlias = this.getPrimaryAlias(scheme, linktype);
        if(primAlias == null) {
            //throw new MonosaccharideException("No primary alias set for substituent " + this.getName() + ", notation scheme " + scheme.getNameStr() + ", linkage type " + linktype.name());
            return null;
        }
        return(primAlias.getResidueIncludedName());
    }
    
    /**
     * Check, if the substituent is split into a residue included and a separate part in the given namescheme
     * @param scheme
     * @return
     */
    public boolean isSplit(GlycanNamescheme scheme, LinkageType linktype, SubstituentTemplateContainer stContainer) throws ResourcesDbException {
        if(getSeparateDisplayTemplate(scheme, linktype, stContainer) == null) {
            return(false);
        }
        if(getSeparateDisplayTemplate(scheme, linktype, stContainer).equals(this)) {
            return(false);
        }
        return(true);
    }

    /**
     * @return the oLinkedEquivalent
     */
    public String getOLinkedEquivalent(SubstituentTemplateContainer stContainer) throws ResourcesDbException {
        if(this.oLinkedEquivalent != null && this.oLinkedEquivalent.length() > 0) {
            return this.oLinkedEquivalent;
        }
        if(this.getSubparts() != null) {
            SubstituentTemplate rootTmpl = this.getSubparts().getSubstTmpl(stContainer); //stContainer.forName(GlycanNamescheme.GLYCOCT, (String)this.getSubparts().getUserObject());
            if(rootTmpl != null && rootTmpl.getName().equals(SubstituentTemplate.AMINOTEMPLATENAME)) {
                if(this.getSubparts().getChildCount() == 1) {
                    String subPart = ((SubstituentSubpartTreeNode)this.getSubparts().getFirstChild()).getName();
                    return subPart;
                }
            }
        }
        return null;
    }

    /**
     * @param linkedEquivalent the oLinkedEquivalent to set
     */
    public void setOLinkedEquivalent(String linkedEquivalent) {
        this.oLinkedEquivalent = linkedEquivalent;
    }
    
    public LinkageType getLinkageTypeBySubstituentName(GlycanNamescheme scheme, String name) throws MonosaccharideException {
        List<SubstituentAlias> aliaslist = getAliasList();
        for(SubstituentAlias alias : aliaslist) {
            if(alias.getNamescheme().equals(scheme)) {
                if(alias.getResidueIncludedName() != null && alias.getResidueIncludedName().equalsIgnoreCase(name)) {
                    return(alias.getLinktype1());
                }
                if(alias.getSeparateDisplayName() != null && alias.getSeparateDisplayName().equalsIgnoreCase(name)) {
                    return(alias.getLinktype1());
                }
            }
        }
        throw new MonosaccharideException("Substituent '" + name + "' is not defined in namescheme " + scheme);
    }
    
    public LinkageType getLinkageType2BySubstituentName(GlycanNamescheme scheme, String name) throws MonosaccharideException {
        List<SubstituentAlias> aliaslist = getAliasList();
        for(SubstituentAlias alias : aliaslist) {
            if(alias.getNamescheme().equals(scheme)) {
                if(alias.getResidueIncludedName() != null && alias.getResidueIncludedName().equalsIgnoreCase(name)) {
                    return(alias.getLinktype2());
                }
                if(alias.getSeparateDisplayName() != null && alias.getSeparateDisplayName().equalsIgnoreCase(name)) {
                    return(alias.getLinktype2());
                }
            }
        }
        throw new MonosaccharideException("Substituent '" + name + "' is not defined in namescheme " + scheme);
    }
    
    /**
     * @return the subparts
     */
    public SubstituentSubpartTreeNode getSubparts() {
        return subparts;
    }

    /**
     * @param subparts the subparts to set
     */
    public void setSubparts(SubstituentSubpartTreeNode subparts) {
        this.subparts = subparts;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public boolean isLinkable() {
        return isLinkable;
    }

    public void setLinkable(boolean isLinkable) {
        this.isLinkable = isLinkable;
    }

    public boolean isExtendedAmine(int position, SubstituentTemplateContainer stContainer) throws ResourcesDbException {
        if(this.getSubparts() != null) {
            if(this.getSubparts().getName().equals(stContainer.forName(GlycanNamescheme.MONOSACCHARIDEDB, AMINOTEMPLATENAME).getName())) {
                if(this.getSubparts().getChildCount() == 1) {
                    return true;
                }
            }
        }
        //TODO: delete check via oLinkedEquivalent after check via subparts is tested
        /*if(getLinkingAtom(position).getElement().getPeriodicNumber() == 7) {
            if(getOLinkedEquivalent(stContainer) != null && !getOLinkedEquivalent(stContainer).equals("")) {
                return true;
            }
        }*/
        return false;
    }
    
    public boolean isExtendedAmine(SubstituentTemplateContainer stContainer) throws ResourcesDbException {
        return isExtendedAmine(getDefaultLinkingPosition1(), stContainer);
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        super.init();
        this.setCanReplaceRingOxygen(false);
        this.setLinkable(false);
        this.setHaworthName("");
        this.setMirroredHaworthName("");
        this.setAliasList(null);
        this.setOLinkedEquivalent(null);
        this.primaryAliasMap = new HashMap<GlycanNamescheme, HashMap<LinkageType, SubstituentAlias>>();
        //this.residueIncludedName = new HashMap<GlycanNamescheme, HashMap<LinkageType, String>>();
        //this.separateDisplay = new HashMap<GlycanNamescheme, HashMap<LinkageType, String>>();
    }
    
    public static LinkageType getLinkageTypeByLinkageName(String name) {
        if(name != null) {
            try {
                return LinkageType.forName(name);
            } catch(GlycoconjugateException ge) {
                return null;
            }
        }
        return(null);
    }
    
    public String toString() {
        String outStr = super.toString();
        outStr += "default linkage type: " + this.getDefaultLinkagetype1();
        if(this.getMaxValence() == 2) {
            outStr += " / default linkage type2: " + this.getDefaultLinkagetype2();
        }
        outStr += "\n";
        /*if(this.getOLinkedEquivalent() != null) {
            outStr += "o-linked equiv.: " + this.getOLinkedEquivalent() + "\n";
        }*/
        if(this.getAliasList() != null) {
            for(SubstituentAlias alias : this.getAliasList()) {
                outStr += "Alias: " + alias.toString() + "\n";
            }
        }
        return outStr;
    }
    
    public boolean equals(Object anotherObject) {
        if(anotherObject == null) {
            return false;
        }
        if(!anotherObject.getClass().equals(SubstituentTemplate.class)) {
            return false;
        }
        SubstituentTemplate anotherTemplate = (SubstituentTemplate) anotherObject;
        return anotherTemplate.toString().equals(this.toString());
    }

}
