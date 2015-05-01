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

/**
* Class to store monosaccharide core modifications.
*
* @author Thomas Lütteke
*/
public class CoreModification extends Modification implements Cloneable {
    
    private boolean isSubstitutable;
    private CoreModificationTemplate template;
    private int dbId;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public CoreModification() {
            init();
    }
    
    public CoreModification(CoreModificationTemplate template, int position) throws MonosaccharideException {
        setModification(template, position);
    }
    
    public CoreModification(CoreModificationTemplate template, int position1, int position2) throws MonosaccharideException {
        setDivalentModification(template, position1, position2);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public void setName(String name) {
        super.setName(name);
        if(name == null) {
            this.setTemplate(null);
        } else if(getTemplate() == null || !getTemplate().getName().equals(name)) {
            try {
                this.setTemplate(CoreModificationTemplate.forName(name));
            } catch(MonosaccharideException me) {
                this.setTemplate(null);
            }
        }
    }
    
    public boolean isSubstitutable() {
        return isSubstitutable;
    }

    public void setSubstitutable(boolean isSubstitutable) {
        this.isSubstitutable = isSubstitutable;
    }
    
    public CoreModificationTemplate getTemplate() {
        return template;
    }

    public void setTemplate(CoreModificationTemplate template) {
        if(template == null) {
            super.setName(null);
        } else {
            super.setName(template.getName());
        }
        this.template = template;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    //*****************************************************************************
    //*** methods to set modification data: ***************************************
    //*****************************************************************************
    
    public void setModification(CoreModificationTemplate template, int position) throws MonosaccharideException {
        if(template == null) {
            setTemplate(null);
            setPosition1(new ArrayList<Integer>());
        } else {
            setValence(template.getValence());
            setSubstitutable(template.isSubstitutable());
            setTemplate(template);
            setPosition1(position);
        } 
    }

    public void setDivalentModification(CoreModificationTemplate template, int position1, int position2) throws MonosaccharideException {
        setTemplate(template);
        setPosition1(position1);
        setPosition2(position2);
        setValence(template.getValence());
        setSubstitutable(template.isSubstitutable());
    }
    
    public void changeType(CoreModificationTemplate newTemplate) throws MonosaccharideException {
        if(this.getTemplate().getValence() != newTemplate.getValence()) {
            throw new MonosaccharideException("cannot change modification of valence " + this.getTemplate().getValence() + " to one of valence " + newTemplate.getValence());
        }
        this.setTemplate(newTemplate);
    }

    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        try {
            setModification(null, 0);
        } catch(MonosaccharideException me) {
            System.err.println("Internal error: Exception in CoreModification.init(): " + me.getMessage());
        }
    }
    
    public CoreModification clone() {
        CoreModification modClone = new CoreModification();
        modClone.setName(this.getName());
        modClone.setTemplate(this.getTemplate());
        modClone.setValence(this.getValence());
        modClone.setSubstitutable(this.isSubstitutable());
        modClone.setPosition1(this.getPosition1Clone());
        modClone.setPosition2(this.getPosition2Clone());
        return(modClone);
    }
    
}
