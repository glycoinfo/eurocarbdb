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
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.GlycoconjugateException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;
import org.eurocarbdb.resourcesdb.util.Utils;

public class GlycoCTExporter extends StandardExporter implements MonosaccharideExporter {
    
    private org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbMonosaccharide eurocarbDbMs = null;
    
    private String substitutionStr = null;
    private String msGlycoCTstr = null;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public GlycoCTExporter(GlycanNamescheme scheme) {
        super(scheme);
    }

    public GlycoCTExporter(GlycanNamescheme scheme, Config conf, TemplateContainer container) {
        super(scheme, conf, container);
    }

    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    /**
     * @return the eurocarbDbMs
     */
    public org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbMonosaccharide getEurocarbDbMs() {
        return this.eurocarbDbMs;
    }

    /**
     * @param eurocarbDbMs the eurocarbDbMs to set
     */
    public void setEurocarbDbMs(org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbMonosaccharide eurocarbDbMs) {
        this.eurocarbDbMs = eurocarbDbMs;
    }

    /**
     * @return the substitutionStr
     */
    public String getSubstitutionStr() {
        return this.substitutionStr;
    }

    /**
     * @param substitutionStr the substitutionStr to set
     */
    public void setSubstitutionStr(String substitutionStr) {
        this.substitutionStr = substitutionStr;
    }

    /**
     * @return the msNamestr
     */
    public String getMsGlycoCTstr() {
        return this.msGlycoCTstr;
    }

    /**
     * @param msNamestr the msNamestr to set
     */
    public void setMsGlycoCTstr(String msNamestr) {
        this.msGlycoCTstr = msNamestr;
    }

    //*****************************************************************************
    //*** export methods: *********************************************************
    //*****************************************************************************
    
    public String export(Monosaccharide ms) throws MonosaccharideException {
        setEurocarbDbMs(null);
        try {
            this.eurocarbDbMs = BasetypeConversion.msdbToEurocarbdb(ms);
        } catch(GlycoconjugateException ge) {
            MonosaccharideException me = new MonosaccharideException("glycoconjugate exception in basetype conversion: " + ge.getMessage());
            me.initCause(ge);
            throw me;
        }
        this.setMsGlycoCTstr(this.getEurocarbDbMs().getGlycoCTName());
        String outStr;
        outStr = getMsGlycoCTstr();
        this.setSubstitutionStr(this.generateMsdbSubstituentsString(ms));
        if(this.getNamescheme().equals(GlycanNamescheme.MONOSACCHARIDEDB)) {
            String anhydroStr = this.generateAnhydroLactonoString(ms);
            if(anhydroStr != null && anhydroStr.length() > 0) {
                outStr += anhydroStr;
            }
            String substStr = this.getSubstitutionStr();
            if(substStr != null && substStr.length() > 0) {
                outStr += substStr;
            }
        }
        return(outStr);
    }
    
    public ArrayList<SubstituentExchangeObject> getSeparateDisplaySubstituents(Monosaccharide ms) throws ResourcesDbException {
        ArrayList<SubstituentExchangeObject> substList = super.getSeparateDisplaySubstituents(ms);
        if(this.getNamescheme().equals(GlycanNamescheme.GLYCOCT)) {
            //*** store anhydro / lactono core modifications as substituents in GlycoCT: ***
            for(CoreModification mod : ms.getCoreModifications()) {
                if(mod.getName().equals(CoreModificationTemplate.ANHYDRO.getName()) || mod.getName().equals(CoreModificationTemplate.LACTONE.getName()) || mod.getName().equals(CoreModificationTemplate.EPOXY.getName())) {
                    SubstituentExchangeObject subst = new SubstituentExchangeObject(this.getNamescheme(), this.getConfig(), this.getTemplateContainer());
                    subst.setName(mod.getName());
                    subst.setPosition1(mod.getPosition1());
                    subst.setPosition2(mod.getPosition2());
                    subst.setLinkagetype1(LinkageType.DEOXY);
                    subst.setLinkagetype1(LinkageType.H_AT_OH);
                    subst.setSubstituentPosition1(new ArrayList<Integer>());
                    subst.setSubstituentPosition2(new ArrayList<Integer>());
                    substList.add(subst);
                }
            }
            
        }
        return substList;
    }
    
    public String generateAnhydroLactonoString(Monosaccharide ms) throws MonosaccharideException {
        String anlacStr = "";
        for(CoreModification mod : ms.getCoreModifications(CoreModificationTemplate.ANHYDRO.getName())) {
            if(mod.getPositions().size() == 0 || mod.containsPosition(0)) {
                mod = mod.clone();
                //TODO: determine list of possible positions
            }
            anlacStr += "|" + Utils.formatPositionsString(mod.getPosition1(), "/", "0") + "," + Utils.formatPositionsString(mod.getPosition2(), "/", "0") + ":" + CoreModificationTemplate.ANHYDRO.getName();
        }
        for(CoreModification mod : ms.getCoreModifications(CoreModificationTemplate.LACTONE.getName())) {
            if(mod.getPositions().size() == 0 || mod.containsPosition(0)) {
                mod = mod.clone();
                //TODO: determine list of possible positions
            }
            anlacStr += "|" + Utils.formatPositionsString(mod.getPosition1(), "/", "0") + "," + Utils.formatPositionsString(mod.getPosition2(), "/", "0") + ":" + CoreModificationTemplate.LACTONE.getName();
        }
        return(anlacStr);
    }
    
    public String generateMsdbSubstituentsString(Monosaccharide ms, GlycanNamescheme scheme) throws MonosaccharideException {
        String substStr = "";
        if(scheme.equals(GlycanNamescheme.MONOSACCHARIDEDB)) {
            for(Substitution subst : ms.getSubstitutions()) {
                if(subst.getPositions().size() == 0 || subst.containsPosition(0)) {
                    subst = subst.clone();
                    //TODO: determine list of possible positions
                }
                substStr += "|" + generateSingleSubstituentString(subst, scheme);
            }
        } else if(scheme.equals(GlycanNamescheme.GLYCOCT)) {
            //*** substituents are not included in GlycoCT monosaccharide name, so do nothing here ***
        } else {
            throw new MonosaccharideException("unsupported namescheme in generateSubstituentsString(): " + scheme.getNameStr());
        }
        if(substStr.length() > 0) {
            substStr = "|" + substStr;
        }
        return(substStr);
    }
    
    public String generateMsdbSubstituentsString(Monosaccharide ms) throws MonosaccharideException {
        return(generateMsdbSubstituentsString(ms, this.getNamescheme()));
    }
    
    private String generateSingleSubstituentString(Substitution subst, GlycanNamescheme scheme) {
        String substStr = "";
        if(scheme.equals(GlycanNamescheme.MONOSACCHARIDEDB)) {
            substStr += "(";
            substStr += Utils.formatPositionsString(subst.getPosition1(), "/", "0");
            substStr += subst.getLinkagetype1().getType() + ":";
            substStr += Utils.formatPositionsString(subst.getSubstituentPosition1(), "/", "0");
            if(subst.hasPosition2()) {
                substStr += "," + Utils.formatPositionsString(subst.getPosition2(), "/", "0");
                substStr += subst.getLinkagetype2().getType() + ":";
                substStr += Utils.formatPositionsString(subst.getSubstituentPosition2(), "/", "0");
            }
            substStr += ")" + subst.getName();
        }
        return(substStr);
    }
    
}
