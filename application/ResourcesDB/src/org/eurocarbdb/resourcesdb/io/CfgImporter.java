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
* Importer class for CFG LinearCode residue names
* @author Thomas Luetteke
*
*/
public class CfgImporter extends StandardImporter implements MonosaccharideImporter {
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public CfgImporter() {
        this(null, null);
    }

    public CfgImporter(Config confObj) {
        this(confObj, null);
    }
    
    public CfgImporter(Config confObj, TemplateContainer container) {
        super(GlycanNamescheme.CFG, confObj, container);
    }
    
    //*****************************************************************************
    //*** parsing methods: ********************************************************
    //*****************************************************************************

    public Monosaccharide parseMsString(String name) throws ResourcesDbException {
        Monosaccharide ms = new Monosaccharide(this.getConfig(), this.getTemplateContainer());
        this.parseMsString(name, ms);
        return ms;
    }
    
    public Monosaccharide parseMsString(String name, Monosaccharide ms) throws ResourcesDbException {
        if(ms == null) {
            throw new NameParsingException("CfgImporter.parseMsString(String, Monosaccharide): Monosaccharide must not be null.");
        }
        this.init();
        this.setInputName(name);
        ms.init();
        ms.setCheckPositionsOnTheFly(false);
        
        String residueBaseStr = "";
        boolean hasNonStdConfig = false;
        boolean hasNonStdRingtype = false;
        Anomer anomeric = null;
        ArrayList<Substitution> substList = null;
        
        //*** parse name: ***
        
        //*** get base name: ***
        while(this.hasCurrentToken() && Character.isUpperCase(name.charAt(this.getParsingPosition()))) {
            residueBaseStr += name.charAt(this.getParsingPosition());
            this.increaseParsingPosition();
        }
        if(residueBaseStr.length() == 0) {
            throw new NameParsingException("Cfg residue name must start with uppercase letter.", this.getInputName(), this.getParsingPosition());
        }
        TrivialnameTemplate template = this.getTemplateContainer().getTrivialnameTemplateContainer().forBasetypeName(GlycanNamescheme.CFG, residueBaseStr);
        if(template == null) {
            throw new NameParsingException("unknown cfg base name: " + residueBaseStr, this.getInputName(), 0);
        }
        this.setDetectedTrivialname(template);
        
        //*** check for non standard configuration: ***
        if(name.length() > this.getParsingPosition()) {
            if(name.charAt(this.getParsingPosition()) == '\'') {
                hasNonStdConfig = true;
                this.increaseParsingPosition();
            }
        }
        //*** check for non standard ring type: ***
        if(name.length() > this.getParsingPosition()) {
            if(name.charAt(this.getParsingPosition()) == '^') {
                hasNonStdRingtype = true;
                this.increaseParsingPosition();
            }
        }
        //*** check for both non standard ring type and configuration: ***
        if(name.length() > this.getParsingPosition()) {
            if(name.charAt(this.getParsingPosition()) == '~') {
                hasNonStdRingtype = true;
                hasNonStdConfig = true;
                this.increaseParsingPosition();
            }
        }
        //*** check for substitutions: ***
        if(name.length() > this.getParsingPosition()) {
            substList = this.parseSubstitutions();
        }
        //*** get anomeric: ***
        if(name.length() > this.getParsingPosition()) {
            if(name.charAt(this.getParsingPosition()) == 'a') {
                anomeric = Anomer.ALPHA;
                this.increaseParsingPosition();
            } else if(name.charAt(this.getParsingPosition()) == 'b') {
                anomeric = Anomer.BETA;
                this.increaseParsingPosition();
            } else if(name.charAt(this.getParsingPosition()) == '?') {
                anomeric = Anomer.UNKNOWN;
                this.increaseParsingPosition();
            }
        } else {
            anomeric = Anomer.UNKNOWN;
        }
        if(name.length() > this.getParsingPosition()) {
            throw new NameParsingException("unexpected token: '" + name.charAt(this.getParsingPosition()) + "'", this.getInputName(), this.getParsingPosition());
        }
        
        //*** build monosaccharide: ***
        
        //*** set stereocode + anomer: ***
        ms.setSize(template.getSize());
        String stereoStr = Stereocode.StereoN + template.getStereocode() + Stereocode.StereoN;
        if(template.getDefaultConfiguration().equals(StereoConfiguration.Laevus)) {
            stereoStr = Stereocode.changeDLinStereoString(stereoStr);
        }
        ms.setStereoStr(stereoStr);
        ms.setAnomer(anomeric);
        ms.setAnomerInStereocode();
        //*** set configuration: ***
        if(hasNonStdConfig) {
            if(template.isDefaultConfigIsCompulsory()) {
                throw new NameParsingException("Change of configuration not allowed for " + template.getPrimaryName(this.getNamescheme()), this.getInputName(), this.getInputName().indexOf("'"));
            }
            ms.setConfiguration(StereoConfiguration.invert(template.getDefaultConfiguration()));
            ms.setStereoStr(Stereocode.changeDLinStereoString(ms.getStereoStr()));
        } else {
            ms.setConfiguration(template.getDefaultConfiguration());
        }
        //*** set ring: ***
        int ringstart = template.getCarbonylPosition();
        ms.setDefaultCarbonylPosition(ringstart);
        ms.setRingStart(ringstart);
        int ringend = template.getDefaultRingend();
        if(hasNonStdRingtype) {
            if(ringend - ringstart == 4) {
                ringend --;
            } else {
                ringend ++;
            }
        }
        ms.setRingEnd(ringend);
        //*** set modifications: ***
        ms.getBasetype().setCoreModifications(template.getCoreModifications());
        ms.setSubstitutions(template.getSubstitutionsClone());
        if(substList != null) {
            for(Substitution subst : substList) {
                ms.addSeparateDisplaySubstitution(subst, GlycanNamescheme.CFG, this.getTemplateContainer().getSubstituentTemplateContainer(), false);
            }
        }
        
        return ms;
    }
    
    private ArrayList<Substitution> parseSubstitutions() throws ResourcesDbException {
        ArrayList<Substitution> outList = new ArrayList<Substitution>();
        String parseStr = this.getInputName();
        if(parseStr.charAt(this.getParsingPosition()) == '[') {
            this.increaseParsingPosition();
            Substitution subst;
            while((subst = this.parseSingleSubstitution()) != null) {
                outList.add(subst);
                if(parseStr.charAt(this.getParsingPosition()) == ',') {
                    this.increaseParsingPosition();
                } else if(parseStr.charAt(this.getParsingPosition()) == ']') {
                    break;
                }
            }
            if(parseStr.charAt(this.getParsingPosition()) != ']') {
                throw new NameParsingException("unexpected token '" + parseStr.charAt(this.getParsingPosition()) + "' (expected ']')", this.getInputName(), this.getParsingPosition());
            } else {
                this.increaseParsingPosition();
            }
        }
        return outList;
    }
    
    private Substitution parseSingleSubstitution() throws ResourcesDbException {
        String parseStr = this.getInputName();
        Substitution subst = new Substitution();
        int pos;
        pos = this.parseIntNumber(true);
        subst.addPosition1(pos);
        while(parseStr.charAt(this.getParsingPosition()) == '/') {
            this.increaseParsingPosition();
            pos = this.parseIntNumber(true);
            subst.addPosition1(pos);
        }
        if(parseStr.charAt(this.getParsingPosition()) == ',') {
            this.increaseParsingPosition();
            pos = this.parseIntNumber(true);
            subst.addPosition2(pos);
            while(parseStr.charAt(this.getParsingPosition()) == '/') {
                this.increaseParsingPosition();
                pos = this.parseIntNumber(true);
                subst.addPosition2(pos);
            }
        }
        String substName = "";
        while(Character.isUpperCase(parseStr.charAt(this.getParsingPosition()))) {
            substName += parseStr.charAt(this.getParsingPosition());
            this.increaseParsingPosition();
        }
        if(substName.length() == 0) {
            throw new NameParsingException("substituent identifier expected", parseStr, this.getParsingPosition());
        }
        SubstituentTemplate substTmpl = this.getTemplateContainer().getSubstituentTemplateContainer().forResidueIncludedName(GlycanNamescheme.CFG, substName); 
        if(substTmpl == null) {
            throw new NameParsingException("unknown substituent identifier", parseStr, this.getParsingPosition() - substName.length());
        }
        subst.setTemplate(substTmpl);
        subst.setLinkagetype1(substTmpl.getLinkageTypeBySubstituentName(GlycanNamescheme.CFG, substName));
        subst.setSubstituentPosition1(substTmpl.getDefaultLinkingPosition1());
        if(subst.hasPosition2()) {
            //TODO: set position 2 linkage type in template alias and get it from there
            subst.setLinkagetype2(substTmpl.getDefaultLinkagetype2());
            subst.setSubstituentPosition2(substTmpl.getDefaultLinkingPosition2());
        }
        return subst;
    }
    
}
