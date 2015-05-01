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
package org.eurocarbdb.resourcesdb;

import org.eurocarbdb.resourcesdb.template.TemplateContainer;

public abstract class ResourcesDbObject {

    private Config config = null;
    private TemplateContainer templateContainer = null;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public ResourcesDbObject() {
        this(null, null);
    }
    
    public ResourcesDbObject(Config conf) {
        this(conf, null);
    }
    
    public ResourcesDbObject(TemplateContainer container) {
        this(null, container);
    }
    
    public ResourcesDbObject(Config conf, TemplateContainer container) {
        this.setConfig(conf);
        this.setTemplateContainer(container);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public Config getConfig() {
        if(this.config == null) {
            this.config = Config.getGlobalConfig();
        }
        return this.config;
    }

    public void setConfig(Config conf) {
        this.config = conf;
    }

    public TemplateContainer getTemplateContainer() {
        if(this.templateContainer == null) {
            this.templateContainer = new TemplateContainer(this.getConfig());
        }
        return this.templateContainer;
    }
    
    public void setTemplateContainer(TemplateContainer container) {
        this.templateContainer = container;
    }
    
}
