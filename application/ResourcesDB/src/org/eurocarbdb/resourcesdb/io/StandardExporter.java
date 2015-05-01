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
package org.eurocarbdb.resourcesdb.io;

import java.util.ArrayList;

import org.eurocarbdb.resourcesdb.*;
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;
import org.eurocarbdb.resourcesdb.template.TrivialnameTemplate;

/**
* A basic MonosaccharideExporter class, providing a number of fields and methods that are needed in most exporters.
*
* @author Thomas Luetteke
*/
public abstract class StandardExporter extends ResourcesDbObject {

    private GlycanNamescheme namescheme = null;
    private TrivialnameTemplate usedTrivialnameTemplate = null;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public StandardExporter(GlycanNamescheme namescheme) {
        this.setNamescheme(namescheme);
        this.setConfig(Config.getGlobalConfig());
    }
    
    public StandardExporter(GlycanNamescheme namescheme, Config conf) {
        this.setNamescheme(namescheme);
        this.setConfig(conf);
    }
    
    public StandardExporter(GlycanNamescheme namescheme, Config conf, TemplateContainer container) {
        this.setNamescheme(namescheme);
        this.setConfig(conf);
        this.setTemplateContainer(container);
    }
    
    public StandardExporter(Config conf, TemplateContainer container) {
        this.setConfig(conf);
        this.setTemplateContainer(container);
    }
    
    public StandardExporter() {
        this(null, null, null);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    /**
     * @return the namescheme
     */
    public GlycanNamescheme getNamescheme() {
        return this.namescheme;
    }

    /**
     * @param scheme the namescheme to set
     */
    public void setNamescheme(GlycanNamescheme scheme) {
        this.namescheme = scheme;
    }

    /**
     * @return the used trivialname template
     */
    public TrivialnameTemplate getUsedTrivialnameTemplate() {
        return this.usedTrivialnameTemplate;
    }

    /**
     * @param template the trivialname template to set
     */
    public void setUsedTrivialnameTemplate(TrivialnameTemplate template) {
        this.usedTrivialnameTemplate = template;
    }

    //*****************************************************************************
    //*** export methods: *********************************************************
    //*****************************************************************************
    
    public ArrayList<SubstituentExchangeObject> getSeparateDisplaySubstituents(Monosaccharide ms) throws ResourcesDbException {
        ArrayList<SubstituentExchangeObject> outList = new ArrayList<SubstituentExchangeObject>();
        for(Substitution subst : ms.getSubstitutions()) {
            TrivialnameTemplate usedTriv = this.getUsedTrivialnameTemplate();
            if(usedTriv != null) {
                if(usedTriv.hasSubstitution(subst)) {
                    continue; //*** substitution is fully implied in trivial name ***
                }
            }
            String separateDisplayName = subst.getTemplate().getSeparateDisplay(this.getNamescheme(), subst.getLinkagetype1());
            if(separateDisplayName != null) {
                if(usedTriv != null) {
                    //TODO: check, if separateDisplayName is partially included in trivial name
                    int position1 = subst.getIntValuePosition1();
                    if(position1 > 0) {
                        Substitution trivSubst = usedTriv.getSubstitutionByPosition(position1, subst.getLinkagetype1());
                        if(trivSubst != null) {
                            /*if(trivSubst.equals(subst)) {
                                continue; //*** substitution is fully included in trivial name => must not repeated in substituents list ***
                            }*/
                        
                        }
                    }
                }
                if(subst.getTemplate().isSplit(this.getNamescheme(), subst.getLinkagetype1(), this.getTemplateContainer().getSubstituentTemplateContainer())) {
                    SubstituentExchangeObject splitSubst = new SubstituentExchangeObject(this.getNamescheme(), this.getConfig(), this.getTemplateContainer());
                    SubstituentTemplate extTemplate = subst.getTemplate().getSeparateDisplayTemplate(this.getNamescheme(), subst.getLinkagetype1(), this.getTemplateContainer().getSubstituentTemplateContainer());
                    splitSubst.setName(separateDisplayName);
                    splitSubst.setPosition1(subst.getPosition1());
                    splitSubst.setPosition2(subst.getPosition2());
                    splitSubst.setLinkagetype1(extTemplate.getDefaultLinkagetype1());
                    splitSubst.setLinkagetype2(extTemplate.getDefaultLinkagetype2());
                    splitSubst.setSubstituentPosition1(extTemplate.getDefaultLinkingPosition1());
                    splitSubst.setOriginalName(subst.getSourceName());
                    splitSubst.setOriginalLinkagetype1(subst.getSourceLinkagetype1());
                    splitSubst.setOriginalLinkagetype2(subst.getSourceLinkagetype2());
                    outList.add(splitSubst);
                } else {
                    SubstituentExchangeObject extSubst = new SubstituentExchangeObject(subst, this.getNamescheme());
                    extSubst.setName(separateDisplayName);
                    outList.add(extSubst);
                }
            }
        }
        return outList;
    }
    
    public ArrayList<SubstituentExchangeObject> getResidueIncludedSubstituents(Monosaccharide ms) throws ResourcesDbException {
        ArrayList<SubstituentExchangeObject> outList = new ArrayList<SubstituentExchangeObject>();
        for(Substitution subst : ms.getSubstitutions()) {
            String residueIncludedName = subst.getTemplate().getResidueIncludedName(this.getNamescheme(), subst.getLinkagetype1());
            if(residueIncludedName != null) {
                TrivialnameTemplate usedTriv = this.getUsedTrivialnameTemplate();
                if(usedTriv != null) {
                    //TODO: check, if residueIncluded part of the substituent is (partially) included in trivial name
                    int position1 = subst.getIntValuePosition1();
                    if(position1 > 0) {
                        Substitution trivSubst = usedTriv.getSubstitutionByPosition(position1, subst.getLinkagetype1());
                        if(trivSubst != null) {
                            if(trivSubst.equals(subst)) {
                                continue; //*** substitution is fully included in trivial name => must not repeated in substituents list ***
                            }
                        
                        }
                    }
                }
                SubstituentExchangeObject inclSubst = new SubstituentExchangeObject(subst, this.getNamescheme());
                inclSubst.setName(residueIncludedName);
                outList.add(inclSubst);
            }
        }
        return outList;
    }
    
}
