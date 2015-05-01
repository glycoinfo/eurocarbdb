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
package org.eurocarbdb.resourcesdb.nonmonosaccharide;

import org.eurocarbdb.resourcesdb.GlycanNamescheme;

public class AglyconAlias {
    private String name = null;
    private GlycanNamescheme namescheme = null;
    private AglyconTemplate primaryTemplate = null;
    private boolean isPrimary = false;
    private int dbId = 0;

    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the namescheme
     */
    public GlycanNamescheme getNamescheme() {
        return namescheme;
    }
    /**
     * @param namescheme the namescheme to set
     */
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
    
    /**
     * @return the primaryTemplate
     */
    public AglyconTemplate getPrimaryTemplate() {
        return primaryTemplate;
    }
    
    /**
     * @param primaryTemplate the primaryTemplate to set
     */
    public void setPrimaryTemplate(AglyconTemplate primaryTemplate) {
        this.primaryTemplate = primaryTemplate;
    }
    
    /**
     * Check, if this substituent name is the primary name for the namescheme it is associated with
     * @return true, if this is a primary name; false, if this is an alias name
     */
    public boolean isPrimaryName() {
        return(this.isPrimary);
    }
    
    public boolean getIsPrimary() {
        return(this.isPrimaryName());
    }
    
    public void setIsPrimary(boolean primary) {
        this.isPrimary = primary;
    }
    
    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public String toString() {
        String outStr = this.getNamescheme().getNameStr() + "::" + this.getName();
        if(this.isPrimaryName()) {
            outStr += " (primary)";
        }
        return(outStr);
    }
    
}
