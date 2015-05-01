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

import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.atom.Composition;
import org.eurocarbdb.resourcesdb.atom.Periodic;

/**
* Enum of core modification templates
* @author Thomas Lütteke
*
*/
public enum CoreModificationTemplate {
    DEOXY("deoxy", "deoxygenation", "deoxy", "d", 1, true, false, "-1O"),
    EN("en", "double bond", "en", "en", 2, true, true, "-2H"), //*** note: "en", "enx" and "yn" are divalent, but the second position is usually not explicitely given, so this has to be handled separately in the source code ***
    ENX("enx", "double bond with unknown deoxygenation pattern", "", "", 2, true, true, ""), //*** note: see "en" for comment on valence / positions ***
    YN("yn", "triple bond", "yn", "yn", 2, true, false, "-4H"), //*** note: see "en" for comment on valence / positions ***
    ANHYDRO("anhydro", "anhydro", "anhydro", "anh", 2, false, false, "-2H-1O"),
    LACTONE("lactone", "lactone", "lactone", "", 2, false, false, "-2H-1O"),
    KETO("keto", "carbonyl group", "ulo", "ulo", 1, true, false, "-2H"),
    ACID("acid", "carboxyl group", "", "", 1, true, true, "+1O"), //*** note: composition change only valid at carbonyl position, at other positions "-2H" has to be added ***
    SP2("sp2", "sp2-hybride", "", "", 1, true, true, "-1H"),
    SP("sp", "sp-hybride", "", "", 1, true, true, "-1H"),
    ALDITOL("aldi", "alditol", "ol", "ol", 1, false, true, "+2H"),
    EPOXY("epoxy", "epoxy", "epoxy", "", 2, false, false, "-2H-1O");

    private String modName;
    private String description;
    private String carbbankName;
    private String bcsdbName;
    private int valence;
    private boolean stereoLoss;
    private boolean substitutable;
    private Composition compositionChanges;
    private String compositionChangesStr;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    /**
     * private constructor
     */
    private CoreModificationTemplate(String nameStr, String descriptionStr, String carbbankStr, String bcsdbString, int valence, boolean stereoLoss, boolean substitutable, String compoChange) {
        this.setName(nameStr);
        this.setDescription(descriptionStr);
        this.setCarbbankName(carbbankStr);
        this.setBcsdbName(bcsdbString);
        this.setValence(valence);
        this.setStereoLoss(stereoLoss);
        this.setSubstitutable(substitutable);
        this.setCompositionChanges(parseCompositionChangesString(compoChange));
        this.compositionChangesStr = compoChange;
    }

    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public String getDescription() {
        return this.description;
    }

    private void setDescription(String descStr) {
        this.description = descStr;
    }

    public String getName() {
        return this.modName;
    }

    private void setName(String nameStr) {
        this.modName = nameStr;
    }

    public String getCarbbankName() {
        return this.carbbankName;
    }

    private void setCarbbankName(String carbbankNameStr) {
        this.carbbankName = carbbankNameStr;
    }

    public String getBcsdbName() {
        return bcsdbName;
    }

    public void setBcsdbName(String bcsdbName) {
        this.bcsdbName = bcsdbName;
    }

    public boolean isStereoLoss() {
        return this.stereoLoss;
    }

    private void setStereoLoss(boolean stereoLossFlag) {
        this.stereoLoss = stereoLossFlag;
    }

    public boolean isSubstitutable() {
        return this.substitutable;
    }

    private void setSubstitutable(boolean substable) {
        this.substitutable = substable;
    }

    public int getValence() {
        return this.valence;
    }

    private void setValence(int theValence) {
        this.valence = theValence;
    }

    public Composition getCompositionChanges() {
        if(this.compositionChanges == null && this.compositionChangesStr != null) {
            this.setCompositionChanges(parseCompositionChangesString(this.compositionChangesStr));
        }
        return this.compositionChanges;
    }

    private void setCompositionChanges(Composition compChanges) {
        this.compositionChanges = compChanges;
    }
    
    private static Composition parseCompositionChangesString(String changeStr) {
        Composition compo = new Composition();
        while(changeStr.length() > 0) {
            try {
                int factor = 1;
                if(changeStr.startsWith("-")) {
                    factor = -1;
                    changeStr = changeStr.substring(1);
                } else if(changeStr.startsWith("+")) {
                    changeStr = changeStr.substring(1);
                }
                int count = Integer.parseInt(changeStr.substring(0, 1));
                changeStr = changeStr.substring(1);
                String elemSymbol = "";
                while(changeStr.matches("^[A-Za-z].*")) {
                    elemSymbol += changeStr.substring(0, 1);
                    changeStr = changeStr.substring(1);
                }
                Periodic el = Periodic.getElementBySymbol(elemSymbol);
                compo.increaseCount(el, factor * count);
            } catch(ResourcesDbException re) {
                return null;
            }
        }
        return compo;
    }

    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    /** 
     * Returns the appropriate CoreModificationTemplate instance for the given name. 
     * @param name Name of the CoreModificationTemplate 
     * @throws MonosaccharideException */
    public static CoreModificationTemplate forName(String name) throws MonosaccharideException {
        if(name.equalsIgnoreCase("ulo")) {
            return KETO;
        }
        for(CoreModificationTemplate c : CoreModificationTemplate.values()) {
            if(name.equalsIgnoreCase(c.modName)) {
                return c;
            }
            if(name.equalsIgnoreCase(c.name())) {
                return c;
            }
        }
        throw new MonosaccharideException("Invalid core modification: " + name);
    }
    
    public static CoreModificationTemplate forCarbbankName(String name) {
        if(name != null) {
            for(CoreModificationTemplate c : CoreModificationTemplate.values()) {
                if(name.equalsIgnoreCase(c.getCarbbankName())) {
                    return c;
                }
            }
        }
        return null;
    }
    
    private static ArrayList<String> coreModNamesList = null;
    
    /**
     * Get an ArrayList containing the name strings of all available CoreModificationTemplates
     * @return
     */
    public static ArrayList<String> getCoreModificationNamesList() {
        if(coreModNamesList == null) {
            setCoreModificationNamesList();
        }
        return coreModNamesList;
    }
    
    /**
     * Create a list of the name strings of all available CoreModificationTemplates
     */
    private static void setCoreModificationNamesList() {
        coreModNamesList = new ArrayList<String>();
        for(CoreModificationTemplate c: CoreModificationTemplate.values()) {
            coreModNamesList.add(c.getName());
        }
    }
    
    private static ArrayList<String> carbbankNamesList = null;
    
    /**
     * Get an ArrayList containing the carbbank name strings of all CoreModificationTemplates, for which such a name is defined
     * @return
     */
    public static ArrayList<String> getCarbbankNamesList() {
        if(carbbankNamesList == null) {
            setCarbbankNamesList();
        }
        return carbbankNamesList;
    }
    
    /**
     * Create a list of the carbbank name strings of all CoreModificationTemplates, for which such a name is defined
     */
    private static void setCarbbankNamesList() {
        carbbankNamesList = new ArrayList<String>();
        for(CoreModificationTemplate c: CoreModificationTemplate.values()) {
            if(c.getCarbbankName().length() > 0) {
                carbbankNamesList.add(c.getCarbbankName());
            }
        }
    }
    
    /**
     * Get an ArrayList containing the bcsdb name strings of all CoreModificationTemplates, for which such a name is defined
     * @return
     */
    public static ArrayList<String> getBcsdbNamesList() {
        ArrayList<String> bcsdbNamesList = new ArrayList<String>();
        for(CoreModificationTemplate c: CoreModificationTemplate.values()) {
            if(c.getBcsdbName().length() > 0) {
                bcsdbNamesList.add(c.getBcsdbName());
            }
        }
        return bcsdbNamesList;
    }
    
   public String toString() {
        String outStr;
        outStr = this.name() + " " + this.modName + " " + this.description + " " + this.compositionChanges.toString();
        return outStr;
    }

    /**
     * Test, if a core modification of a given type is contained in a list of core modifications
     * @param modList: The modification list to be checked
     * @param modTmpl: The CoreModificationTemplate to be checked for
     * @return true, if a modification of the type modTmpl is present in modList, otherwise false
     */
    public static boolean modListContainsModType(ArrayList<CoreModification> modList, CoreModificationTemplate modTmpl) {
        for(CoreModification mod : modList) {
            if(mod.getTemplate().equals(modTmpl)) {
                return true;
            }
        }
        return false;
    }
}
