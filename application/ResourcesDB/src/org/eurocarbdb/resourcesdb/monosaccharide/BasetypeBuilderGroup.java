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

public enum BasetypeBuilderGroup {
    H2COH("H2COH", "H2-C-OH [tail group]", true,
            Stereocode.ExtStereoCH2OH ,StereoConfiguration.Nonchiral.getStereosymbol(),
            (CoreModificationTemplate) null),
    HCOH_D("HCOH_D", "H-C-OH [dexter pos.]", false,
            Stereocode.StereoD, StereoConfiguration.Dexter.getStereosymbol(),
            (CoreModificationTemplate) null),
    HCOH_L("HCOH_L", "HO-C-H [laevus pos.]", false,
            Stereocode.StereoL, StereoConfiguration.Laevus.getStereosymbol(),
            (CoreModificationTemplate) null),
    HCH("HCH", "H-C-H [deoxy position]", false,
            Stereocode.ExtStereoDeoxy, StereoConfiguration.Nonchiral.getStereosymbol(),
            CoreModificationTemplate.DEOXY),
    COOH("COOH", "COOH [carboxyl group]", true,
            Stereocode.ExtStereoAcid, StereoConfiguration.Nonchiral.getStereosymbol(),
            CoreModificationTemplate.ACID),
    CHO("CHO", "H-C=O [carbonyl group]", true,
            Stereocode.ExtStereoCarbonyl, StereoConfiguration.Nonchiral.getStereosymbol(),
            CoreModificationTemplate.KETO),
    KETO("CO", "C=O [keto group]", false,
            Stereocode.ExtStereoKeto, StereoConfiguration.Nonchiral.getStereosymbol(),
            CoreModificationTemplate.KETO),
    HCH_EN_DEOXY("HCH_EN_DEOXY", "[en + deoxy]", false,
            Stereocode.ExtStereoEnDeoxy, StereoConfiguration.Nonchiral.getStereosymbol(),
            new CoreModificationTemplate[] {CoreModificationTemplate.EN, CoreModificationTemplate.DEOXY}),
    HCOH_EN("HCOH_EN", "[en]", false,
            Stereocode.ExtStereoEnOH, StereoConfiguration.Nonchiral.getStereosymbol(),
            CoreModificationTemplate.EN),
    CH3("CH3", "CH3 [methyl group]", true,
            Stereocode.ExtStereoCH3, StereoConfiguration.Nonchiral.getStereosymbol(),
            CoreModificationTemplate.DEOXY),
    UNKNOWN("X", "unknown", false,
            Stereocode.ExtStereoUnknown, StereoConfiguration.Unknown.getStereosymbol(),
            (CoreModificationTemplate) null);
    
    private String groupName;
    private String displayName;
    private boolean headTail;
    private char stereoSymbol;
    private char extStereoSymbol;
    private ArrayList<CoreModificationTemplate> coreMods;
    
    private BasetypeBuilderGroup(String nameStr, String displayStr, boolean ht, char extStereoChar, char stereoChar, CoreModificationTemplate mod1) {
        this(nameStr, displayStr, ht, extStereoChar, stereoChar, new CoreModificationTemplate[] {mod1});
    }
    
    private BasetypeBuilderGroup(String nameStr, String displayStr, boolean ht, char extStereoChar, char stereoChar, CoreModificationTemplate[] modArr) {
        this.setGroupName(nameStr);
        this.setDisplayName(displayStr);
        this.setHeadTail(ht);
        this.setExtStereoSymbol(extStereoChar);
        this.setStereoSymbol(stereoChar);
        this.setCoreMods(new ArrayList<CoreModificationTemplate>());
        if(modArr != null) {
            for(CoreModificationTemplate mod : modArr) {
                if(mod != null) {
                    this.getCoreMods().add(mod);
                }
            }
        }
    }
    
    public boolean isHeadTail() {
        return this.headTail;
    }
    
    public boolean isBody() {
        return !this.headTail;
    }
    
    public void setHeadTail(boolean ht) {
        this.headTail = ht;
    }
    
    public String getGroupName() {
        return this.groupName;
    }
    
    public void setGroupName(String nameStr) {
        this.groupName = nameStr;
    }
    
    public String getDisplayName() {
        return this.displayName;
    }
    
    public void setDisplayName(String displayNameStr) {
        this.displayName = displayNameStr;
    }
    
    public char getStereoSymbol() {
        return this.stereoSymbol;
    }
    
    public void setStereoSymbol(char stereoChar) {
        this.stereoSymbol = stereoChar;
    }
    
    public char getExtStereoSymbol() {
        return this.extStereoSymbol;
    }
    
    public String getExtStereoSymbolStr() {
        return "" + this.getExtStereoSymbol();
    }

    public void setExtStereoSymbol(char extStereoChar) {
        this.extStereoSymbol = extStereoChar;
    }

    public ArrayList<CoreModificationTemplate> getCoreMods() {
        return this.coreMods;
    }
    
    public int getCoreModCount() {
        if(this.getCoreMods() == null) {
            return 0;
        }
        return this.getCoreMods().size();
    }
    
    public void setCoreMods(ArrayList<CoreModificationTemplate> coreModList) {
        this.coreMods = coreModList;
    }
    
    public boolean hasCoreModification(CoreModificationTemplate modTmpl) {
        if(this.getCoreMods() != null && this.getCoreMods().contains(modTmpl)) {
            return true;
        }
        return false;
    }
    
    public static boolean hasCoreModification(List<BasetypeBuilderGroup> groupList, CoreModificationTemplate modTmpl) {
        if(groupList != null) {
            for(BasetypeBuilderGroup bbg : groupList) {
                if(bbg.hasCoreModification(modTmpl)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static BasetypeBuilderGroup forName(String name) {
        for(BasetypeBuilderGroup group: BasetypeBuilderGroup.values()) {
            if(group.groupName.equals(name)) {
                return group;
            }
        }
        return null;
    }

    public static BasetypeBuilderGroup forExtStereoSymbol(char extStereoChar) {
        for(BasetypeBuilderGroup group: BasetypeBuilderGroup.values()) {
            if(group.getExtStereoSymbol() == extStereoChar) {
                return group;
            }
        }
        return null;
    }
    
    /**
     * Get a BasetypeBuilderGroup using a List of CoreModificationTemplates and the <code>isHeadTail</code> flag.
     * The first BasetypeBuilderGroup matching the given parameters is returned.
     * @param modList the List of CoreModificationTemplates that has to be present in the returned group
     * @param headTailFlag the value of the <code>isHeadTail</code> flag of the returned group (argument may be null, then this check is skipped)
     * @return the BasetypeBuilderGroup matching the given parameters or null if no match is found
     */
    public static BasetypeBuilderGroup forCoreModifications(List<CoreModificationTemplate> modList, Boolean headTailFlag) {
        if(modList != null) {
            for(BasetypeBuilderGroup bbgroup : BasetypeBuilderGroup.values()) {
                if(headTailFlag != null && bbgroup.isHeadTail() != headTailFlag.booleanValue()) {
                    continue;
                }
                if(bbgroup.getCoreModCount() != modList.size()) {
                    continue;
                }
                boolean coreModsMatch = true;
                for(CoreModificationTemplate mod : modList) {
                    if(!bbgroup.hasCoreModification(mod)) {
                        coreModsMatch = false;
                        break;
                    }
                }
                if(coreModsMatch) {
                    return bbgroup;
                }
            }
        }
        return null;
    }
}
