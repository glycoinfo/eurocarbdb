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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.util.NumberUtils;
import org.eurocarbdb.resourcesdb.util.Utils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
* Class to store and manage Trivial Name Templates
* @author Thomas Luetteke
*
*/
public class TrivialnameTemplateContainer {

    private Config config = null;
    private SubstituentTemplateContainer substContainer;
    
    //*****************************************************************************
    //*** Constructors: ***********************************************************
    //*****************************************************************************
    
    public TrivialnameTemplateContainer() {
        this.setConfig(new Config());
    }
    
    public TrivialnameTemplateContainer(Config conf) {
        this.setConfig(conf);
    }
    
    public TrivialnameTemplateContainer(Config conf, SubstituentTemplateContainer substTmplContainer) {
        this.setConfig(conf);
        this.setSubstContainer(substTmplContainer);
    }
    
    //*****************************************************************************
    //*** Getters/Setters: ********************************************************
    //*****************************************************************************
    
    private void setConfig(Config theConf) {
        this.config = theConf;
    }
    
    public Config getConfig() {
        return this.config;
    }
    
    public void setSubstContainer(SubstituentTemplateContainer container) {
        this.substContainer = container;
    }
    
    public SubstituentTemplateContainer getSubstContainer() {
        if(this.substContainer == null) {
            this.substContainer = new SubstituentTemplateContainer(this.getConfig());
        }
        return this.substContainer;
    }
    
    //*****************************************************************************
    //*** Template Lists / Maps: **************************************************
    //*****************************************************************************
    
    private ArrayList<TrivialnameTemplate> trivialnameTemplateList;
    private HashMap<GlycanNamescheme, ArrayList<String>> trivialnameTemplateNamelistsMap;
    
    public ArrayList<TrivialnameTemplate> getTemplateList() throws ResourcesDbException {
        if(this.trivialnameTemplateList == null) {
            this.trivialnameTemplateList = this.readTemplateList(this.getConfig());
        }
        return this.trivialnameTemplateList;
    }
    
    public TrivialnameTemplate forBasetypeName(GlycanNamescheme scheme, String basename) throws ResourcesDbException {
        for(TrivialnameTemplate template : this.getTemplateList()) {
            //TODO: adjust to new structure of template (done?)
            if(template.isTrivialName(scheme, basename)) {
                return template;
            }
            /*if(template.getBaseName().equalsIgnoreCase(basename)) {
                if(template.getNameschemes().contains(scheme)) {
                    return template;
                }
            }*/
        }
        return null;
    }
    
    public ArrayList<String> getTrivialnameBasetypeList(GlycanNamescheme scheme) throws ResourcesDbException {
        if(this.trivialnameTemplateNamelistsMap == null) {
            this.trivialnameTemplateNamelistsMap = new HashMap<GlycanNamescheme, ArrayList<String>>();
        }
        ArrayList<String> basetypeList = this.trivialnameTemplateNamelistsMap.get(scheme);
        if(basetypeList == null) {
            ArrayList<TrivialnameTemplate> templates = this.getTemplateList();
            basetypeList = new ArrayList<String>();
            for(TrivialnameTemplate template : templates) {
                if(template.getNameschemes().contains(scheme)) {
                    if(template.getSchemesMap().get(scheme) != null) {
                        basetypeList.addAll(template.getSchemesMap().get(scheme));
                    }
                }
            }
            this.trivialnameTemplateNamelistsMap.put(scheme, basetypeList);
        }
        return basetypeList;
    }
    
    public boolean isTrivialname(GlycanNamescheme scheme, String basename) throws ResourcesDbException {
        return (this.forBasetypeName(scheme, basename)!= null);
    }
    
    //*****************************************************************************
    //*** Methods for filling Template Lists / Maps: ******************************
    //*****************************************************************************
    
    private ArrayList<TrivialnameTemplate> readTemplateList(Config conf) throws ResourcesDbException {
        return this.getTemplateListFromXml(conf.getTrivialnameTemplatesXmlUrl());
    }
    
    private ArrayList<TrivialnameTemplate> getTemplateListFromXml(URL xmlUrl) throws ResourcesDbException {
        ArrayList<TrivialnameTemplate> tmplList = new ArrayList<TrivialnameTemplate>();
        SAXBuilder parser = new SAXBuilder();
        try {
            Document doc = parser.build(xmlUrl);
            org.jdom.Element root = doc.getRootElement();
            List<?> templateTagsList = root.getChildren();
            Iterator<?> templatesIter = templateTagsList.iterator();
            while(templatesIter.hasNext()) {
                org.jdom.Element xmlTemplate = (org.jdom.Element) templatesIter.next();
                TrivialnameTemplate template = getTemplateFromXmlTree(xmlTemplate);
                if(template != null) {
                    tmplList.add(template);
                }
            }
        } catch (JDOMException je) {
            throw new ResourcesDbException("Exception in reading TrivialnameTemplate XML file.", je);
        } catch (IOException ie) {
            throw new ResourcesDbException("Exception in reading TrivialnameTemplate XML file.", ie);
        }  
        return tmplList;
    }
    
    private TrivialnameTemplate getTemplateFromXmlTree(org.jdom.Element xmlTemplate) {
        TrivialnameTemplate template = null;
        if(xmlTemplate.getName().equalsIgnoreCase("template")) {
            template = new TrivialnameTemplate();
            List<?> propList = xmlTemplate.getChildren();
            Iterator<?> propIter = propList.iterator();
            GlycanNamescheme scheme = null;
            try {
                while(propIter.hasNext()) {
                    org.jdom.Element property = (org.jdom.Element) propIter.next();
                    String propName = property.getName().toLowerCase();
                    if(propName.equals("notation_set")) {
                        String name = property.getAttributeValue("name");
                        List<?> schemeList = property.getChildren();
                        Iterator<?> schemeIter = schemeList.iterator();
                        while(schemeIter.hasNext()) {
                            org.jdom.Element schemeTag = (org.jdom.Element) schemeIter.next();
                            if(schemeTag.getName().toLowerCase().equals("namescheme")) {
                                scheme = GlycanNamescheme.forName(schemeTag.getValue());
                                String primaryStr = schemeTag.getAttributeValue("primary");
                                boolean isPrimary = Utils.parseTrueFalseString(primaryStr, false);
                                template.addName(name, scheme, isPrimary);
                            }
                        }
                    } else if(propName.equals("longname")) {
                        template.setLongName(property.getValue());
                    } else if(propName.equals("stereocode")) {
                        template.setStereocode(property.getValue());
                    } else if(propName.equals("size")) {
                        template.setSize(Integer.parseInt(property.getValue()));
                    } else if(propName.equals("carbonyl_position")) {
                        template.setCarbonylPosition(NumberUtils.parseIntStr(property.getValue(), 0));
                    } else if(propName.equals("default_configuration")) {
                        try {
                            template.setDefaultConfiguration(StereoConfiguration.forNameOrSymbol(property.getValue()));
                            String compulsory = property.getAttributeValue("compulsory");
                            boolean isCompulsory = false;
                            if(compulsory != null && compulsory.length() > 0) {
                                isCompulsory = Utils.parseTrueFalseString(compulsory, isCompulsory);
                            }
                            template.setDefaultConfigIsCompulsory(isCompulsory);
                        } catch(MonosaccharideException me) {}
                    } else if(propName.equals("default_ringtype")) {
                        if(property.getValue().equals("p")) {
                            template.setDefaultRingend(template.getCarbonylPosition() + 4);
                        } else if(property.getValue().equals("f")) {
                            template.setDefaultRingend(template.getCarbonylPosition() + 3);
                        }
                    } else if(propName.equals("coremodification_list")) {
                        template.setCoreModifications(getCoreModificationListFromXmlTree(property));
                    } else if(propName.equals("substitution_list")) {
                        template.setSubstitutions(getSubstitutionListFromXmlTree(GlycanNamescheme.GLYCOCT, property));
                    } else {
                        System.err.println("Warning: unknown tag in trivialname templates xml file: " + property.getName());
                    }
                }
            } catch(ResourcesDbException me) {
                System.err.println("Warning: error in parsing trivial name templates xml file " + me);
                me.printStackTrace();
                return(null);
            }
        }
        return(template);
    }
    
    private ArrayList<CoreModification> getCoreModificationListFromXmlTree(org.jdom.Element xmlElement) throws MonosaccharideException {
        ArrayList<CoreModification> modList = new ArrayList<CoreModification>();
        List<?> xmlModList = xmlElement.getChildren();
        Iterator<?> modIter = xmlModList.iterator();
        while(modIter.hasNext()) {
            CoreModification mod = getCoreModificationFromXmlTree((org.jdom.Element)modIter.next());
            if(mod == null) {
                throw new MonosaccharideException("Error in parsing core modifications of trivialname templates xml file.");
            }
            modList.add(mod);
        }
        return(modList);
    }
    
    private CoreModification getCoreModificationFromXmlTree(org.jdom.Element xmlElement) throws MonosaccharideException {
        CoreModification mod = null;
        if(xmlElement.getName().equalsIgnoreCase("modification")) {
            CoreModificationTemplate modTemplate = null;
            int modPosition1 = 0;
            int modPosition2 = 0;
            Iterator<?> modpropIter = xmlElement.getChildren().iterator();
            while(modpropIter.hasNext()) {
                org.jdom.Element property = (org.jdom.Element) modpropIter.next();
                if(property.getName().equalsIgnoreCase("type")) {
                    modTemplate = CoreModificationTemplate.forName(property.getValue());
                } else if(property.getName().equalsIgnoreCase("position1")) {
                    modPosition1 = Integer.parseInt(property.getValue());
                } else if(property.getName().equalsIgnoreCase("position2")) {
                    if(property.getValue().length() > 0) {
                        modPosition2 = Integer.parseInt(property.getValue());
                    }
                }
            }
            if(modTemplate != null && modPosition1 != 0) {
                mod = new CoreModification();
                if(modPosition2 == 0) {
                    mod.setModification(modTemplate, modPosition1);
                } else {
                    mod.setDivalentModification(modTemplate, modPosition1, modPosition2);
                }
            }
        }
        return(mod);
    }
    
    private ArrayList<Substitution> getSubstitutionListFromXmlTree(GlycanNamescheme scheme, org.jdom.Element xmlElement) throws ResourcesDbException {
        ArrayList<Substitution> substList = new ArrayList<Substitution>();
        List<?> xmlSubstList = xmlElement.getChildren();
        Iterator<?> substIter = xmlSubstList.iterator();
        while(substIter.hasNext()) {
            org.jdom.Element xmlElem = (org.jdom.Element) substIter.next();
            Substitution subst = getSubstitutionFromXmlTree(scheme, xmlElem);
            if(subst == null) {
                throw new MonosaccharideException("Error in parsing substitutions of trivialname templates xml file.");
            }
            substList.add(subst);
        }
        return(substList);
    }
    
    private Substitution getSubstitutionFromXmlTree(GlycanNamescheme scheme, org.jdom.Element xmlElement) throws ResourcesDbException {
        Substitution subst = null;
        if(xmlElement.getName().equalsIgnoreCase("substitution")) {
            SubstituentTemplate substTemplate = null;
            int position1 = 0;
            int position2 = 0;
            int substPosition1 = 0;
            int substPosition2 = 0;
            LinkageType linktype1 = null;
            LinkageType linktype2 = null;
            Iterator<?> substIter = xmlElement.getChildren().iterator();
            while(substIter.hasNext()) {
                org.jdom.Element property = (org.jdom.Element) substIter.next();
                if(property.getName().equalsIgnoreCase("type")) {
                    substTemplate = this.getSubstContainer().forName(scheme, property.getValue());
                    if(substTemplate == null) {
                        substTemplate = this.substContainer.forName(GlycanNamescheme.GLYCOCT, property.getValue());
                    }
                    if(substTemplate == null) {
                        System.err.println("cannot get substitution template for " + scheme.getNameStr() + "::" + property.getValue());
                    }
                } else if(property.getName().equalsIgnoreCase("position1")) {
                    position1 = Integer.parseInt(property.getValue());
                } else if(property.getName().equalsIgnoreCase("position2")) {
                    if(property.getValue().length() > 0) {
                        position2 = Integer.parseInt(property.getValue());
                    }
                } else if(property.getName().equalsIgnoreCase("substituent_position1")) {
                    if(property.getValue().length() > 0) {
                        substPosition1 = Integer.parseInt(property.getValue());
                    }
                } else if(property.getName().equalsIgnoreCase("substituent_position2")) {
                    if(property.getValue().length() > 0) {
                        substPosition2 = Integer.parseInt(property.getValue());
                    }
                } else if(property.getName().equalsIgnoreCase("linkagetype1")) {
                    if(property.getValue().length() > 0) {
                        linktype1 = SubstituentTemplate.getLinkageTypeByLinkageName(property.getValue());
                    }
                } else if(property.getName().equalsIgnoreCase("linkagetype2")) {
                    if(property.getValue().length() > 0) {
                        linktype2 = SubstituentTemplate.getLinkageTypeByLinkageName(property.getValue());
                    }
                }
            }
            if(substTemplate != null && position1 != 0) {
                subst = new Substitution();
                if(linktype1 == null) {
                    linktype1 = substTemplate.getDefaultLinkagetype1();
                }
                if(substPosition1 == 0) {
                    substPosition1 = substTemplate.getDefaultLinkingPosition1();
                }
                if(position2 == 0) {
                    subst.setSubstitution(substTemplate, position1, linktype1, substPosition1);
                } else {
                    if(linktype2 == null) {
                        linktype2 = substTemplate.getDefaultLinkagetype2();
                    }
                    if(substPosition2 == 0) {
                        substPosition2 = substTemplate.getDefaultLinkingPosition2();
                    }
                    subst.setDivalentSubstitution(substTemplate, position1, linktype1, substPosition1, position2, linktype2, substPosition2);
                }
            }
        }
        return(subst);
    }

    //*****************************************************************************
    //*** Other Methods: **********************************************************
    //*****************************************************************************
    
    /**
     * Check, whether there is a trivial name available for a monosaccharide in a given namescheme
     * @param scheme: the namescheme in which the monosaccharide shall be encoded
     * @param ms: the monosaccharide to be checked
     * @return: the appropriate TrivialnameTemplate or null if no trivial name is existing for the given monosaccharide
     * @throws ResourcesDbException
     */
    public TrivialnameTemplate checkMsForTrivialname(GlycanNamescheme scheme, Monosaccharide ms) throws ResourcesDbException {
        //*** (1) prepare ms stereostring for comparison: ***
        String msStereo = ms.getStereoStr();
        //*** (1a) mask anomeric position in ms stereostring: ***
        if(ms.getRingStart() > 0) {
            msStereo = Stereocode.setPositionInStereoString(msStereo, StereoConfiguration.Nonchiral, ms.getRingStart());
            //msStereo = msStereo.substring(0, ms.getCarbonylPosition() - 1) + StereoConfiguration.Nonchiral.getStereosymbol() + msStereo.substring(ms.getCarbonylPosition());
        }
        //*** (1b) chop of first and last position of stereocode: ***
        msStereo = msStereo.substring(1, msStereo.length() - 1);
        //*** (1c) if stereostring is composed of relative configurations, get absolute configurations for comparison to templates: ***
        try {
            if(Stereocode.stereoStringHasRelativePosition(msStereo)) {
                if(scheme.equals(GlycanNamescheme.CFG)) {
                    return null; //*** cfg notation doesn't support unknown configuration ***
                }
                msStereo = Stereocode.relativeToAbsolute(msStereo);
            }
        } catch(MonosaccharideException me) {
            return null;
        }
        //*** (1d) generate D/L mirrored ms stereostring: ***
        String msStereoDL = Stereocode.changeDLinStereoString(msStereo);
        
        //*** (2) search for trivial name template matching ms properties: ***
        ArrayList<String> trivialnameList = this.getTrivialnameBasetypeList(scheme);
        TrivialnameTemplate returnTemplate = null;
        if(trivialnameList == null) {
            return null; //*** no trivial names defined for given name scheme ***
        }
        int returnTemplateSplitCount = 0;
        for(String trivTmplName : trivialnameList) {
            int currentTemplateSplitCount = 0;
            TrivialnameTemplate template = this.forBasetypeName(scheme, trivTmplName);
            if(template.getSize() == ms.getSize()) {
                if(template.getCarbonylPosition() == ms.getRingStart() || ms.getRingStart() == Basetype.OPEN_CHAIN || ms.getRingStart() == Basetype.UNKNOWN_RING) {
                    //*** compare stereocodes (ignoring anomeric (masked above)): ***
                    String tmplStereo = template.getStereocode();
                    if(tmplStereo.equals(msStereo) || tmplStereo.equals(msStereoDL)) {
                        if(template.isDefaultConfigIsCompulsory()) {
                            if(!Stereocode.getConfigurationFromStereoString(msStereo).equals(template.getDefaultConfiguration())) {
                                continue;
                            }
                        }
                        //*** check core modifications: ***
                        if(scheme.equals(GlycanNamescheme.BCSDB)) {
                            //*** ms must contain exactly the same deoxy / keto / en(x) / yn modifications as the trivialname template ***
                            //*** and all other core mods. / substs. of the template, but may have further further core mods. (e.g. anhydro or acid) or substs. ***
                            if(ms.countCoreModifications() < template.getCoreModificationCount()) {
                                continue;
                            }
                            if(ms.countCoreModifications(CoreModificationTemplate.DEOXY) != template.countCoreModifications(CoreModificationTemplate.DEOXY)) {
                                continue;
                            }
                            if(ms.countCoreModifications(CoreModificationTemplate.KETO) != template.countCoreModifications(CoreModificationTemplate.KETO)) {
                                continue;
                            }
                            if(ms.countCoreModifications(CoreModificationTemplate.EN) != template.countCoreModifications(CoreModificationTemplate.EN)) {
                                continue;
                            }
                            if(ms.countCoreModifications(CoreModificationTemplate.ENX) != template.countCoreModifications(CoreModificationTemplate.ENX)) {
                                continue;
                            }
                            if(ms.countCoreModifications(CoreModificationTemplate.YN) != template.countCoreModifications(CoreModificationTemplate.YN)) {
                                continue;
                            }
                        } else {
                            //*** ms must contain exactly the same core modifications as the trivialname template, ***
                            //*** and all substitutions of the trivialname template (but may have further substitutions) ***
                            if(ms.countCoreModifications() != template.getCoreModificationCount()) {
                                continue;
                            }
                        }
                        //*** numbers of core modifications match, so check types / positions now: ***
                        ArrayList<CoreModification> trivialMods = template.getCoreModifications();
                        boolean modificationMissing = false;
                        for(int m = 0; m < trivialMods.size(); m++) {
                            CoreModification mod = trivialMods.get(m);
                            if(!ms.hasCoreModification(mod)) {
                                modificationMissing = true;
                                break;
                            }
                        }
                        if(modificationMissing) {
                            continue;
                        }
                        //*** check substitutions: ***
                        ArrayList<Substitution> trivialSubstList = template.getSubstitutions();
                        for(Substitution trivialSubst: trivialSubstList) {
                            if(!ms.hasSubstitution(trivialSubst)) {
                                modificationMissing = true;
                                if(scheme.equals(GlycanNamescheme.CFG)) {
                                    //*** check, if subst in trivial name is subpart of subst in ms, and remaining subpart of ms subst is defined in CFG namescheme: ***
                                    List<Substitution> msSubstList = ms.getSubstitutions();
                                    for(Substitution msSubst : msSubstList) {
                                        ///*** check, if positions match: ***
                                        if(!Utils.formatPositionsString(trivialSubst.getPosition1(), "/", "?").equals(Utils.formatPositionsString(msSubst.getPosition1(), "/", "?"))) {
                                            continue;
                                        }
                                        if(msSubst.hasPosition2()) {
                                            if(!trivialSubst.hasPosition2()) {
                                                continue;
                                            }
                                            if(!Utils.formatPositionsString(trivialSubst.getPosition2(), "/", "?").equals(Utils.formatPositionsString(msSubst.getPosition2(), "/", "?"))) {
                                                continue;
                                            }
                                        }
                                        //*** check for subparts: ***
                                        SubstituentSubpartTreeNode msSubstNode = msSubst.getTemplate().getSubparts();
                                        if(msSubstNode != null) {
                                            SubstituentTemplate msSubstNodeTmpl = msSubstNode.getSubstTmpl(this.getSubstContainer());
                                            if(trivialSubst.getTemplate().getName().equals(msSubstNodeTmpl.getName())) {
                                                if(msSubstNode.getChildCount() == 1) {
                                                    SubstituentSubpartTreeNode msChildNode = (SubstituentSubpartTreeNode)msSubstNode.getFirstChild();
                                                    if(!msChildNode.isLeaf()) {
                                                        continue;
                                                    }
                                                    SubstituentTemplate msChildTmpl = msChildNode.getSubstTmpl(this.getSubstContainer());
                                                    if(msChildTmpl == null) {
                                                        throw new ResourcesDbException("Cannot get SubstituentTemplate for subst. subpart " + msChildNode.getName());
                                                    }
                                                    try {
                                                        msChildTmpl.getPrimaryAlias(GlycanNamescheme.CFG, LinkageType.H_AT_OH);
                                                    } catch(ResourcesDbException rEx) {
                                                        continue; //*** no primary alias defined for subst. subpart in CFG notation ***
                                                    }
                                                    modificationMissing = false;
                                                    currentTemplateSplitCount ++;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    //*** check, if trivial name includes amino subst. and ms contains extended amino subst.: ***
                                    if(trivialSubst.getName().equalsIgnoreCase(SubstituentTemplate.AMINOTEMPLATENAME)) {
                                        List<Substitution> msSubstList = ms.getSubstitutions();
                                        for(Substitution msSubst : msSubstList) {
                                            if(msSubst.getPosition1().size() == 1 && msSubst.getIntValuePosition1() == trivialSubst.getIntValuePosition1()) {
                                                if(msSubst.getTemplate().isExtendedAmine(this.getSubstContainer())) {    
                                                    modificationMissing = false;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                if(modificationMissing) {
                                    break;
                                }
                            }
                        }
                        if(modificationMissing) {
                            continue;
                        }
                        
                        if(returnTemplate == null || template.getSubstitutionCount() > returnTemplate.getSubstitutionCount()) {
                            returnTemplate = template;
                            returnTemplateSplitCount = currentTemplateSplitCount;
                        } else {
                            if(currentTemplateSplitCount < returnTemplateSplitCount && template.getSubstitutionCount() == returnTemplate.getSubstitutionCount()) {
                                returnTemplate = template;
                                returnTemplateSplitCount = currentTemplateSplitCount;
                            }
                        }
                    }
                }
            }
        }
        return returnTemplate;
    }
    
}
