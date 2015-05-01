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
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.*;
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.nonmonosaccharide.AglyconTemplate;
import org.eurocarbdb.resourcesdb.nonmonosaccharide.NonmonosaccharideException;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;

public class MonosaccharideConverter extends ResourcesDbObject implements MonosaccharideConversion {
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public MonosaccharideConverter() {
        this(null, null);
    }
    
    public MonosaccharideConverter(Config conf) {
        this(conf, null);
    }
    
    public MonosaccharideConverter(TemplateContainer container) {
        this(null, container);
    }
    
    public MonosaccharideConverter(Config conf, TemplateContainer container) {
        setConfig(conf);
        this.setTemplateContainer(container);
    }
    
    //*****************************************************************************
    //*** conversion methods: *****************************************************
    //*****************************************************************************
    
    /**
     * Read a given monosaccharide name into a Monosaccharide object
     * @param msNamestr the monosaccharide name
     * @param sourceScheme the notation scheme, in which the name is encoded
     * @return the Monosaccharide encoded by the given msNamestr
     * @throws ResourcesDbException in case the name cannot be parsed correctly
     */
    public Monosaccharide parseMsNamestr(String msNamestr, GlycanNamescheme sourceScheme) throws ResourcesDbException {
        Monosaccharide ms = null;
        if(GlycanNamescheme.CARBBANK.equals(sourceScheme.getBaseScheme())) {
            CarbbankImporter importer = new CarbbankImporter(sourceScheme, this.getConfig(), this.getTemplateContainer());
            ms = importer.parseMsString(msNamestr);
        } else if(GlycanNamescheme.GLYCOCT.equals(sourceScheme.getBaseScheme())) {
            GlycoCTImporter importer = new GlycoCTImporter(sourceScheme, this.getConfig(), this.getTemplateContainer());
            ms = importer.parseMsString(msNamestr);
        } else if(GlycanNamescheme.CFG.equals(sourceScheme.getBaseScheme())) {
            CfgImporter importer = new CfgImporter(this.getConfig(), this.getTemplateContainer());
            ms = importer.parseMsString(msNamestr);
        } else if(GlycanNamescheme.BCSDB.equals(sourceScheme.getBaseScheme())) {
            BcsdbImporter importer = new BcsdbImporter(this.getConfig(), this.getTemplateContainer());
            ms = importer.parseMsString(msNamestr);
        } else if(GlycanNamescheme.AUTO.equals(sourceScheme)) {
            ArrayList<Monosaccharide> msList = new ArrayList<Monosaccharide>();
            for(GlycanNamescheme testScheme : GlycanNamescheme.values()) {
                if(testScheme.equals(GlycanNamescheme.AUTO)) {
                    continue;
                }
                try {
                    ms = this.parseMsNamestr(msNamestr, testScheme);
                    if(ms != null) {
                        boolean addToList = true;
                        for(Monosaccharide listMs : msList) {
                            if(listMs.getName().equals(ms.getName())) {
                                addToList = false;
                            }
                        }
                        if(addToList) {
                            msList.add(ms);
                        }
                    }
                } catch(ResourcesDbException me) {
                    //*** string cannot be parsed correctly with currently tested namescheme, nothing to be done here ***
                }
            }
            if(msList.size() == 1) {
                return msList.get(0);
            } else {
                if(msList.size() == 0) {
                    throw new ResourcesDbException("Cannot auto-detect namescheme for monosaccharide " + msNamestr + ": the name cannot be parsed correctly in any namescheme.");
                } else {
                    throw new ResourcesDbException("Cannot auto-detect namescheme for monosaccharide " + msNamestr + ": " + msList.size() + " schemes reveal different results.");
                }
            }
        }
        else {
            throw new ResourcesDbException("unsupported source namescheme: " + sourceScheme.getNameStr());
        }
        return ms;
    }
    
    public MonosaccharideExchangeObject convertMonosaccharide(String msNamestr, GlycanNamescheme sourceScheme, GlycanNamescheme targetScheme) throws ResourcesDbException {
        MonosaccharideExchangeObject msObjOutput = new MonosaccharideExchangeObject(this.getConfig(), this.getTemplateContainer());
        Monosaccharide ms = null;
        
        //*** Convert namestring to internal representation: ***
        ms = this.parseMsNamestr(msNamestr, sourceScheme);
            
        //*** Generate output object from internal representation: ***
        msObjOutput = this.convertMonosaccharide(ms, targetScheme);
            
        return(msObjOutput);
    }
    
    public MonosaccharideExchangeObject convertMonosaccharide(MonosaccharideExchangeObject msObjInput, GlycanNamescheme sourceScheme, GlycanNamescheme targetScheme) throws ResourcesDbException {
        MonosaccharideExchangeObject msObjOutput = new MonosaccharideExchangeObject(this.getConfig(), this.getTemplateContainer());
        Monosaccharide ms = null;
        
        //*** convert exchange object to internal representation: ***
        if(GlycanNamescheme.GLYCOCT.equals(sourceScheme.getBaseScheme())) {
            if(msObjInput.getBasetype() != null) {
                ms = BasetypeConversion.eurcarbdbToMsdb(msObjInput.getBasetype(), msObjInput.getConfig(), msObjInput.getTemplateContainer());
                if(ms == null) {
                    throw new MonosaccharideException("cannot get monosaccharide object from eurocarbdb basetype.");
                }
            } else {
                ms = this.parseMsNamestr(msObjInput.getMonosaccharideName(), sourceScheme);
            }
            for(SubstituentExchangeObject substExch : msObjInput.getSubstituents()) {
                /*if(!GlycanNamescheme.MONOSACCHARIDEDB.equals(sourceScheme)) {
                    substExch.setPosition1(convertLinkagePositionsEcdbToMsdb(substExch.getPosition1()));
                    substExch.setPosition2(convertLinkagePositionsEcdbToMsdb(substExch.getPosition2()));
                }*/
                if(substExch.getName().equals(CoreModificationTemplate.ANHYDRO.getName())) {
                    CoreModification mod = new CoreModification();
                    mod.setName(CoreModificationTemplate.ANHYDRO.getName());
                    mod.setPosition1(substExch.getPosition1());
                    mod.setPosition2(substExch.getSubstituentPosition1());
                    mod.setValence(2);
                    ms.addCoreModification(mod);
                    continue;
                }
                if(substExch.getName().equals(CoreModificationTemplate.LACTONE)) {
                    CoreModification mod = new CoreModification();
                    mod.setName(CoreModificationTemplate.LACTONE.getName());
                    mod.setPosition1(substExch.getPosition1());
                    mod.setPosition2(substExch.getSubstituentPosition1());
                    mod.setValence(2);
                    mod.sortPositions();
                    ms.addCoreModification(mod);
                    //TODO: handle effect on ring closure
                    continue;
                }
                Substitution subst = new Substitution(substExch, sourceScheme, this.getConfig(), this.getTemplateContainer());
                ms.addSeparateDisplaySubstitution(subst, sourceScheme, this.getTemplateContainer().getSubstituentTemplateContainer(), false);
            }
        } else if(GlycanNamescheme.CARBBANK.equals(sourceScheme.getBaseScheme())) {
            ms = this.parseMsNamestr(msObjInput.getMonosaccharideName(), sourceScheme);
            if(msObjInput.hasSubstituentsInList()) {
                for(SubstituentExchangeObject substExchObj : msObjInput.getSubstituents()) {
                    Substitution subst = new Substitution(substExchObj, this.getConfig(), this.getTemplateContainer());
                    ms.addSeparateDisplaySubstitution(subst, sourceScheme, this.getTemplateContainer().getSubstituentTemplateContainer(), true);
                }
            }
        } else if(GlycanNamescheme.CFG.equals(sourceScheme.getBaseScheme())) {
            ms = this.parseMsNamestr(msObjInput.getMonosaccharideName(), sourceScheme);
            if(msObjInput.hasSubstituentsInList()) {
                for(SubstituentExchangeObject substExchObj : msObjInput.getSubstituents()) {
                    Substitution subst = new Substitution(substExchObj, this.getConfig(), this.getTemplateContainer());
                    ms.addSeparateDisplaySubstitution(subst, sourceScheme, this.getTemplateContainer().getSubstituentTemplateContainer(), true);
                }
            }
        } else if(GlycanNamescheme.BCSDB.equals(sourceScheme.getBaseScheme())) {
            ms = this.parseMsNamestr(msObjInput.getMonosaccharideName(), sourceScheme);
            if(msObjInput.hasSubstituentsInList()) {
                for(SubstituentExchangeObject substExchObj : msObjInput.getSubstituents()) {
                    Substitution subst = new Substitution(substExchObj, this.getConfig(), this.getTemplateContainer());
                    ms.addSeparateDisplaySubstitution(subst, sourceScheme, this.getTemplateContainer().getSubstituentTemplateContainer(), true);
                }
            }
        } else {
            throw new ResourcesDbException("unsupported source namescheme: " + sourceScheme.getNameStr());
        }
        
        //*** Generate output object from internal representation: ***
        msObjOutput = this.convertMonosaccharide(ms, targetScheme);
        
        return(msObjOutput);
    }
    
    /**
     * Convert a ResourcesDB Monosaccharide object to a MonosaccharideExchangeObject
     * @param ms the Monosaccharide to convert
     * @param targetScheme the target name scheme for the exchange object
     * @return
     * @throws ResourcesDbException
     */
    public MonosaccharideExchangeObject convertMonosaccharide(Monosaccharide ms, GlycanNamescheme targetScheme) throws ResourcesDbException {
        MonosaccharideExchangeObject msObjOutput = new MonosaccharideExchangeObject(this.getConfig(), this.getTemplateContainer());
        
        //*** Generate output namestring from internal representation: ***
        MonosaccharideExporter exporter = null;
        String nameStr = null;
        if(GlycanNamescheme.CARBBANK.equals(targetScheme.getBaseScheme())) {
            exporter = new CarbbankExporter(targetScheme, this.getConfig(), this.getTemplateContainer());
            nameStr = exporter.export(ms);
        } else if(GlycanNamescheme.GLYCOCT.equals(targetScheme.getBaseScheme())) {
            exporter = new GlycoCTExporter(targetScheme, this.getConfig(), this.getTemplateContainer());
            nameStr = exporter.export(ms);
        } else if(GlycanNamescheme.CFG.equals(targetScheme.getBaseScheme())) {
            exporter = new CfgExporter(this.getConfig(), this.getTemplateContainer());
            nameStr = exporter.export(ms);
        } else if(GlycanNamescheme.BCSDB.equals(targetScheme.getBaseScheme())) {
            exporter = new BcsdbExporter(this.getConfig(), this.getTemplateContainer());
            nameStr = exporter.export(ms);
        } else {
            throw new ResourcesDbException("Unsupported target namescheme: " + targetScheme.getNameStr());
        }
        msObjOutput.setMonosaccharideName(nameStr);
        msObjOutput.setMonosaccharideNamescheme(targetScheme);
        try {
            msObjOutput.setBasetype(BasetypeConversion.msdbToEurocarbdb(ms));
        } catch(GlycoconjugateException ge) {
            throw new MonosaccharideException("Error in basetype conversion: " + ge.getMessage(), ge);
        }

        //*** store substituents, which are not included in the namestring, in the exchange object: ***
        ArrayList<SubstituentExchangeObject> extSubstList = exporter.getSeparateDisplaySubstituents(ms);
        for(SubstituentExchangeObject substObj : extSubstList) {
            msObjOutput.addSubstituent(substObj);
        }
        /*for(Substitution subst : ms.getSubstitutions()) {
            String separateDisplayName = subst.getTemplate().getSeparateDisplay(targetScheme, subst.getLinkagetype1());
            if(separateDisplayName != null) {
                SubstituentExchangeObject substObj;
                if(subst.getTemplate().isSplit(targetScheme, subst.getLinkagetype1(), this.getTemplateContainer().getSubstituentTemplateContainer())) {
                    substObj = new SubstituentExchangeObject();
                    SubstituentTemplate extTemplate = subst.getTemplate().getSeparateDisplayTemplate(targetScheme, subst.getLinkagetype1(), this.getTemplateContainer().getSubstituentTemplateContainer());
                    substObj.setPosition1(subst.getPosition1());
                    substObj.setPosition2(subst.getPosition2());
                    substObj.setDefaultDataFromTemplate(targetScheme, extTemplate);
                    substObj.setOriginalName(subst.getSourceName());
                    substObj.setOriginalLinkagetype1(subst.getSourceLinkagetype1());
                    substObj.setOriginalLinkagetype2(subst.getSourceLinkagetype2());
                } else {
                    substObj = new SubstituentExchangeObject(subst);
                }
                substObj.setName(separateDisplayName);
                msObjOutput.addSubstituent(substObj);
            }
        }
        if(GlycanNamescheme.GLYCOCT.equals(targetScheme)) {
            //*** store anhydro / lactono core modifications as substituents in GlycoCT: ***
            for(CoreModification mod : ms.getCoreModifications()) {
                if(mod.getName().equals(CoreModificationTemplate.ANHYDRO.getName()) || mod.getName().equals(CoreModificationTemplate.LACTONE.getName()) || mod.getName().equals(CoreModificationTemplate.EPOXY.getName())) {
                    SubstituentExchangeObject substObj = new SubstituentExchangeObject();
                    substObj.setName(mod.getName());
                    substObj.setPosition1(mod.getPosition1());
                    substObj.setPosition2(mod.getPosition2());
                    substObj.setLinkagetype1(LinkageType.DEOXY);
                    substObj.setLinkagetype1(LinkageType.H_AT_OH);
                    substObj.setSubstituentPosition1(new ArrayList<Integer>());
                    substObj.setSubstituentPosition2(new ArrayList<Integer>());
                    msObjOutput.addSubstituent(substObj);
                }
            }
        }*/
        
        MonosaccharideValidation.checkMonosaccharideConsistency(ms, this.getTemplateContainer(), this.getConfig());
        msObjOutput.setOrientationChanged(ms.isOrientationChanged());

        msObjOutput.addResidueType(MonosaccharideExchangeObject.ResidueTypes.monosaccharide);
        
        //*** set masses: ***
        msObjOutput.setAvgMass(ms.getAvgMass());
        msObjOutput.setMonoMass(ms.getMonoMass());
        
        return msObjOutput;
    }
    
    public String convertSubstituent(String sourceSubstituentName, GlycanNamescheme sourceScheme, GlycanNamescheme targetScheme) throws ResourcesDbException {
        SubstituentTemplate subst = this.getTemplateContainer().getSubstituentTemplateContainer().forName(sourceScheme, sourceSubstituentName);
        if(subst == null) {
            throw new NonmonosaccharideException(sourceSubstituentName + " is not a valid substituent name in " + sourceScheme.getNameStr() + " namescheme.");
        }
        return(subst.getPrimaryAlias(targetScheme, subst.getDefaultLinkagetype1()).getSeparateDisplayName());
    }
    
    public SubstituentExchangeObject convertSubstituent(SubstituentExchangeObject sourceSubst, GlycanNamescheme sourceScheme, GlycanNamescheme targetScheme) throws ResourcesDbException {
        SubstituentTemplate subst = this.getTemplateContainer().getSubstituentTemplateContainer().forName(sourceScheme, sourceSubst.getName());
        if(subst == null) {
            throw new NonmonosaccharideException(sourceSubst.getName() + " is not a valid substituent name in " + sourceScheme.getNameStr() + " namescheme.");
        }
        LinkageType linktype = sourceSubst.getLinkagetype1();
        if(linktype == null) {
            linktype = subst.getLinkageTypeBySubstituentName(sourceScheme, sourceSubst.getName());
        }
        SubstituentExchangeObject substOut = new SubstituentExchangeObject(targetScheme, this.getConfig(), this.getTemplateContainer());
        substOut.setName(subst.getPrimaryAlias(targetScheme, linktype).getSeparateDisplayName());
        substOut.setPosition1(sourceSubst.getPosition1());
        substOut.setPosition2(sourceSubst.getPosition2());
        substOut.setSubstituentPosition1(sourceSubst.getSubstituentPosition1());
        substOut.setSubstituentPosition2(sourceSubst.getSubstituentPosition2());
        substOut.setLinkagetype1(linktype);
        substOut.setOriginalName(sourceSubst.getName());
        substOut.setOriginalLinkagetype1(sourceSubst.getLinkagetype1());
        substOut.setOriginalLinkagetype2(sourceSubst.getLinkagetype2());
        return(substOut);
    }
    
    public String convertAglycon(String sourceAglyconName, GlycanNamescheme sourceScheme, GlycanNamescheme targetScheme) throws ResourcesDbException {
        AglyconTemplate aglycTemplate = this.getTemplateContainer().getAglyconTemplateContainer().getAglyconTemplateByName(sourceScheme, sourceAglyconName);
        if(aglycTemplate == null) {
            throw new NonmonosaccharideException(sourceAglyconName + " is not a valid aglycon name in notation " + sourceScheme.getNameStr());
        }
        String targetName = aglycTemplate.getPrimaryAlias(targetScheme);
        if(targetName == null) {
            throw new NonmonosaccharideException("No primary alias available for aglycon " + aglycTemplate.getName() + " in notation " + sourceScheme.getNameStr());
        }
        return(targetName);
    }
    
    public String checkSubstituent(String substName, GlycanNamescheme sourceScheme) throws ResourcesDbException {
        return(convertSubstituent(substName, sourceScheme, sourceScheme));
    }
    
    public String checkAglycon(String aglyconName, GlycanNamescheme sourceScheme) throws ResourcesDbException {
        return(convertAglycon(aglyconName, sourceScheme, sourceScheme));
    }
    
    public MonosaccharideExchangeObject convertResidue(String resNamestr, GlycanNamescheme sourceScheme, GlycanNamescheme targetScheme) throws ResourcesDbException {
        MonosaccharideExchangeObject retObj = null;
        //*** check, if residue can be a monosaccharide: ***
        try {
            retObj = convertMonosaccharide(resNamestr, sourceScheme, targetScheme);
        } catch(ResourcesDbException rex) {
        }
        //*** check, if residue can be a substituent: ***
        try {
            String resOutnameStr = convertSubstituent(resNamestr, sourceScheme, targetScheme);
            if(retObj == null) {
                retObj = new MonosaccharideExchangeObject(resOutnameStr, targetScheme, this.getConfig(), this.getTemplateContainer());
            }
            retObj.addResidueType(MonosaccharideExchangeObject.ResidueTypes.substituent);
        } catch(ResourcesDbException rex) {
        }
        //*** check, if residue can be an aglycon: ***
        try {
            String resOutnameStr = convertAglycon(resNamestr, sourceScheme, targetScheme);
            if(retObj == null) {
                retObj = new MonosaccharideExchangeObject(resOutnameStr, targetScheme, this.getConfig(), this.getTemplateContainer());
            }
            retObj.addResidueType(MonosaccharideExchangeObject.ResidueTypes.aglycon);        
        } catch(ResourcesDbException rex) {
        }
        //*** if residue can be neither monosaccharide nor substituent nor aglycon, throw an exception: ***
        if(retObj == null) {
            throw new ResourcesDbException("Cannot convert residue '" + resNamestr + "' from " + sourceScheme.getNameStr() + " to " + targetScheme.getNameStr());
        }
        //*** otherwise return the exchange object: ***
        return(retObj);
    }
    
    public MonosaccharideExchangeObject validateGlycoCT(MonosaccharideExchangeObject msObj) throws ResourcesDbException {
        return(convertMonosaccharide(msObj, GlycanNamescheme.GLYCOCT, GlycanNamescheme.GLYCOCT));
    }
    
}
