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

import org.eurocarbdb.resourcesdb.Config;

/**
* Class to store Containers of Templates like SubstituentTemplate or TrivialnameTemplate.
* These containers hold Lists or Maps of Templates 
* together with methods to fill these Lists/Maps and methods to access the Templates.
* 
* @author Thomas Lütteke
*/
public class TemplateContainer {

    private Config config = null;
    
    public void setConfig(Config theConf) {
        this.config = theConf;
    }
    
    public Config getConfig() {
        if(this.config == null) {
            return Config.getGlobalConfig();
        }
        return this.config;
    }
    
    //*****************************************************************************
    //*** Constructors: ***********************************************************
    //*****************************************************************************
    
    public TemplateContainer() {
        this.setConfig(new Config());
    }
    
    public TemplateContainer(Config conf) {
        this.setConfig(conf);
    }
    
    //*****************************************************************************
    //*** Substituent Templates: **************************************************
    //*****************************************************************************
    
    private SubstituentTemplateContainer substituentTemplateContainer;
    
    public SubstituentTemplateContainer getSubstituentTemplateContainer() {
        if(this.substituentTemplateContainer == null) {
            this.substituentTemplateContainer = new SubstituentTemplateContainer(this.getConfig());
        }
        return this.substituentTemplateContainer;
    }
    
    public void setSubstituentTemplateContainer(SubstituentTemplateContainer container) {
        this.substituentTemplateContainer = container;
    }
    
    
    //*****************************************************************************
    //*** Trivial Name Templates: *************************************************
    //*****************************************************************************
    
    private TrivialnameTemplateContainer trivialnameTemplateContainer;
    
    public TrivialnameTemplateContainer getTrivialnameTemplateContainer() {
        if(this.trivialnameTemplateContainer == null) {
            this.trivialnameTemplateContainer = new TrivialnameTemplateContainer(this.getConfig(), this.getSubstituentTemplateContainer());
        }
        return this.trivialnameTemplateContainer;
    }
    
    public void setTrivialnameTemplateContainer(TrivialnameTemplateContainer container) {
        this.trivialnameTemplateContainer = container;
    }
    
    //*****************************************************************************
    //*** Basetype Templates: *****************************************************
    //*****************************************************************************
    
    private BasetypeTemplateContainer basetypeTemplateContainer;
    
    public BasetypeTemplateContainer getBasetypeTemplateContainer() {
        if(this.basetypeTemplateContainer == null) {
            this.basetypeTemplateContainer = new BasetypeTemplateContainer(this.getConfig());
        }
        return this.basetypeTemplateContainer;
    }
    
    public void setBasetypeTemplateContainer(BasetypeTemplateContainer container) {
        this.basetypeTemplateContainer = container;
    }
    
    //*****************************************************************************
    //*** Aglycon Templates: ******************************************************
    //*****************************************************************************
    
    private AglyconTemplateContainer aglyconTemplateContainer;
    
    public AglyconTemplateContainer getAglyconTemplateContainer() {
        if(this.aglyconTemplateContainer == null) {
            this.aglyconTemplateContainer = new AglyconTemplateContainer(this.getConfig());
        }
        return this.aglyconTemplateContainer;
    }
    
    public void setAglyconTemplateContainer(AglyconTemplateContainer container) {
        this.aglyconTemplateContainer = container;
    }
    
    //*****************************************************************************
    //*** Monosaccharide Dictionary: **********************************************
    //*****************************************************************************
    
    private MonosaccharideDictionary msDictionary = null;
    
    public MonosaccharideDictionary getMsDictionary() {
        if(this.msDictionary == null) {
            this.msDictionary = new MonosaccharideDictionary(this.getConfig());
        }
        return this.msDictionary;
    }
    
    public void setMsDictionary(MonosaccharideDictionary dict) {
        this.msDictionary = dict;
    }
}
