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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eurocarbdb.resourcesdb.*;
import org.eurocarbdb.resourcesdb.template.NonBasetypeTemplate;

/**
* A NonmonosaccharideTemplate that stores general Aglycon information
* @author Thomas Lütteke
*
*/
public class AglyconTemplate extends NonBasetypeTemplate {
    private String aglyconClass;
    private HashMap<GlycanNamescheme, String> primaryAliasMap = new HashMap<GlycanNamescheme, String>();
    private List<AglyconAlias> aliasList;
    private int dbId;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public AglyconTemplate() {
        init();
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public String getAglyconClass() {
        return this.aglyconClass;
    }

    public void setAglyconClass(String aglyconClassStr) {
        this.aglyconClass = aglyconClassStr;
    }

    /**
     * @return the aliasList
     */
    public List<AglyconAlias> getAliasList() {
        return this.aliasList;
    }

    /**
     * @param aliasList the aliasList to set
     */
    public void setAliasList(List<AglyconAlias> aliasList) {
        this.aliasList = aliasList;
    }
    
    public void addAlias(AglyconAlias alias) {
        List<AglyconAlias> aliases = getAliasList();
        if(aliases == null) {
            aliases = new ArrayList<AglyconAlias>();
            setAliasList(aliases);
        }
        aliases.add(alias);
        
    }
    
    private HashMap<GlycanNamescheme, String> getPrimaryAliasMap() {
        return(this.primaryAliasMap);
    }
    
    public void setPrimaryAlias(GlycanNamescheme scheme, String name) {
        if(this.getPrimaryAliasMap() == null) {
            this.primaryAliasMap = new HashMap<GlycanNamescheme, String>();
        }
        this.getPrimaryAliasMap().put(scheme, name);
    }
    
    public String getPrimaryAlias(GlycanNamescheme scheme) throws NonmonosaccharideException {
        String alias = this.getPrimaryAliasMap().get(scheme);
        if(alias == null) {
            throw new NonmonosaccharideException("Cannot get primary alias of aglycon " + this.getName() + " in notation " + scheme.getNameStr());
        }
        return(alias);
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
    
    public void init() {
        super.init();
        this.setAglyconClass(null);
        this.primaryAliasMap = new HashMap<GlycanNamescheme, String>();
        this.setDbId(0);
    }
    
    public String toString() {
        String outStr = super.toString();
        outStr += "Class: " + this.getAglyconClass() + "\n";
        if(this.getAliasList() != null) {
            for(AglyconAlias alias : this.getAliasList()) {
                outStr += "Alias: " + alias.toString() + "\n";
            }
        }
        return(outStr);
    }

}
