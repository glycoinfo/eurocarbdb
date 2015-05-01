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
import org.eurocarbdb.resourcesdb.atom.Atom;
import org.eurocarbdb.resourcesdb.atom.AtomConnection;
import org.eurocarbdb.resourcesdb.atom.Composition;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.GlycoconjugateException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.monosaccharide.SubstituentAlias;
import org.eurocarbdb.resourcesdb.monosaccharide.SubstituentSubpartTreeNode;
import org.eurocarbdb.resourcesdb.util.Utils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
* Class to store and manage Substituent Templates
* @author Thomas Lütteke
*
*/
public class SubstituentTemplateContainer {

    private Config config = null;
    
    //*****************************************************************************
    //*** Constructors: ***********************************************************
    //*****************************************************************************
    
    public SubstituentTemplateContainer() {
        this.setConfig(new Config());
    }
    
    public SubstituentTemplateContainer(Config conf) {
        this.setConfig(conf);
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
    
    //*****************************************************************************
    //*** Template Lists / Maps: **************************************************
    //*****************************************************************************
    
    private ArrayList<SubstituentTemplate> substituentTemplateList;
    private HashMap<GlycanNamescheme, ArrayList<String>> includedNameListsMap;
    
    public ArrayList<SubstituentTemplate> getTemplateList() throws ResourcesDbException {
        if(this.substituentTemplateList == null) {
            this.substituentTemplateList = SubstituentTemplateContainer.readTemplateList(this.getConfig());
        }
        return this.substituentTemplateList;
    }
    
    public ArrayList<String> getResidueIncludedNameList(GlycanNamescheme scheme) throws ResourcesDbException {
        if(this.includedNameListsMap == null) {
            this.includedNameListsMap = new HashMap<GlycanNamescheme, ArrayList<String>>();
        }
        ArrayList<String> nameList = this.includedNameListsMap.get(scheme);
        if(nameList == null) {
            ArrayList<SubstituentTemplate> templates = this.getTemplateList();
            nameList = new ArrayList<String>();
            for(SubstituentTemplate template : templates) {
                for(SubstituentAlias alias : template.getAliasList(scheme, null)) {
                    if(alias.getResidueIncludedName() != null && alias.getResidueIncludedName().length() > 0) {
                        if(!nameList.contains(alias.getResidueIncludedName())) {
                            nameList.add(alias.getResidueIncludedName());
                        }
                    }
                }
            }
            this.includedNameListsMap.put(scheme, nameList);
        }
        return nameList;
    }
    
    public SubstituentTemplate forResidueIncludedName(GlycanNamescheme scheme, String name) throws ResourcesDbException {
        for(SubstituentTemplate substTmpl : this.getTemplateList()) {
            for(SubstituentAlias alias : substTmpl.getAliasList()) {
                if(alias.getNamescheme().equals(scheme)) {
                    if(name.equalsIgnoreCase(alias.getResidueIncludedName())) {
                        if(alias.getSeparateDisplayName() == null || alias.getSeparateDisplayName().length() == 0) {
                            return substTmpl;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public SubstituentTemplate forSeparateDisplayName(GlycanNamescheme scheme, String name) throws ResourcesDbException {
        for(SubstituentTemplate substTmpl : this.getTemplateList()) {
            for(SubstituentAlias alias : substTmpl.getAliasList()) {
                if(alias.getNamescheme().equals(scheme)) {
                    if(name.equalsIgnoreCase(alias.getSeparateDisplayName())) {
                        if(alias.getResidueIncludedName() == null || alias.getResidueIncludedName().length() == 0) {
                            return substTmpl;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public SubstituentTemplate forName(GlycanNamescheme scheme, String name) throws ResourcesDbException {
        if(name != null) {
            for(SubstituentTemplate substTmpl : this.getTemplateList()) {
                for(SubstituentAlias alias : substTmpl.getAliasList()) {
                    if(alias.getNamescheme().equals(scheme)) {
                        if(name.equalsIgnoreCase(alias.getResidueIncludedName())) {
                            if(alias.getSeparateDisplayName() == null) {
                                return substTmpl;
                            }
                        }
                        if(name.equalsIgnoreCase(alias.getSeparateDisplayName())) {
                            if(alias.getResidueIncludedName() == null) {
                                return substTmpl;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    //*****************************************************************************
    //*** Methods for filling Template Lists / Maps: ******************************
    //*****************************************************************************
    
    private static ArrayList<SubstituentTemplate> readTemplateList(Config conf) throws ResourcesDbException {
        return SubstituentTemplateContainer.getTemplateListFromXml(conf.getSubstituentTemplatesXmlUrl());
    }
    
    private static ArrayList<SubstituentTemplate> getTemplateListFromXml(URL xmlUrl) throws ResourcesDbException {
        SAXBuilder parser = new SAXBuilder();
        ArrayList<SubstituentTemplate> templateList = new ArrayList<SubstituentTemplate>();
        try {
            Document doc = parser.build(xmlUrl);
            org.jdom.Element root = doc.getRootElement();
            List<?> xmlTemplateList = root.getChildren();
            Iterator<?> templatesIter = xmlTemplateList.iterator();
            while(templatesIter.hasNext()) {
                org.jdom.Element xmlTemplate = (org.jdom.Element) templatesIter.next();
                SubstituentTemplate template = getTemplateFromXmlTree(xmlTemplate);
                if(template != null) {
                    templateList.add(template);
                }
            }
        } catch (JDOMException je) {
            throw new ResourcesDbException("Exception in reading TrivialnameTemplate XML file.", je);
        } catch (IOException ie) {
            throw new ResourcesDbException("Exception in reading TrivialnameTemplate XML file.", ie);
        }  
        return templateList;
    }
    
    private static SubstituentTemplate getTemplateFromXmlTree(org.jdom.Element xmlTemplate) throws ResourcesDbException {
        if(xmlTemplate.getName().equalsIgnoreCase("template")) {
            SubstituentTemplate substTemplate = new SubstituentTemplate();
            List<?> propList = xmlTemplate.getChildren();
            Iterator<?> propIter = propList.iterator();
            while(propIter.hasNext()) {
                org.jdom.Element property = (org.jdom.Element) propIter.next();
                String propertyName = property.getName().toLowerCase();
                String propertyValue = property.getValue();
                if(propertyValue == null) {
                    propertyValue = "";
                }
                if(propertyName.equals("name")) {
                    substTemplate.setName(propertyValue);
                    //TODO: add explicit alias names for allowed linkage types to substituents xml file and remove the subsequent loop:
                    for(LinkageType link : LinkageType.values()) {
                        //*** in GlycoCT notation all substituents are separate residues: ***
                        //substTemplate.setSeparateDisplay(GlycanNamescheme.GLYCOCT, link, propertyValue);
                        if(link.equals(LinkageType.NONMONOSACCHARID)) {
                            continue;
                        }
                        SubstituentAlias msdbAlias = new SubstituentAlias(GlycanNamescheme.MONOSACCHARIDEDB, link, propertyValue, null, true);
                        substTemplate.addAlias(msdbAlias);
                        SubstituentAlias glycoctAlias = new SubstituentAlias(GlycanNamescheme.GLYCOCT, link, null, propertyValue, true);
                        substTemplate.addAlias(glycoctAlias);
                        //substTemplate.setPrimaryAlias(GlycanNamescheme.GLYCOCT, link, alias);
                    }
                } else if(propertyName.equals("valence") && !propertyValue.equals("")) {
                    if(property.getChildren() != null && property.getChildren().size() > 0) {
                        HashMap<String, Integer> minMaxMap = SubstituentTemplateContainer.parseMinMaxTags(property);
                        if(minMaxMap.get("MIN") != null) {
                            substTemplate.setMinValence(minMaxMap.get("MIN").intValue());
                        }
                        if(minMaxMap.get("MAX") != null) {
                            substTemplate.setMaxValence(minMaxMap.get("MAX").intValue());
                        }
                    } else {
                        substTemplate.setValence(Integer.parseInt(propertyValue));
                    }
                } else if(propertyName.equals("default_linking_position1") && !propertyValue.equals("")) {
                    substTemplate.setDefaultLinkingPosition1(Integer.parseInt(propertyValue));
                } else if(propertyName.equals("default_linking_position2") && !propertyValue.equals("")) {
                    substTemplate.setDefaultLinkingPosition2(Integer.parseInt(propertyValue));
                } else if(propertyName.equals("default_linkage_type1") && !propertyValue.equals("")) {
                    /*try {
                        substTemplate.setDefaultLinkagetype1(LinkageType.forName(propertyValue));
                    } catch(GlycoconjugateException ge) {
                        throw new ResourcesDbException("Error in reading substituent template from xml: unknown LinkageType " + propertyValue, ge);
                    }*/
                } else if(propertyName.equals("default_linkage_type2") && !propertyValue.equals("")) {
                    /*try {
                        substTemplate.setDefaultLinkagetype2(LinkageType.forName(propertyValue));
                    } catch(GlycoconjugateException ge) {
                        throw new ResourcesDbException("Error in reading substituent template from xml: unknown LinkageType " + propertyValue, ge);
                    }*/
                } else if(propertyName.equals("bondorder1")) {
                    //substTemplate.setBondOrder1(Integer.parseInt(propertyValue));
                } else if(propertyName.equals("bondorder2") && !propertyValue.equals("")) {
                    //substTemplate.setBondOrder2(Integer.parseInt(propertyValue));
                } else if(propertyName.equals("can_replace_ring_oxygen")) {
                    substTemplate.setCanReplaceRingOxygen(Utils.parseTrueFalseString(propertyValue, false));
                } else if(propertyName.equals("is_linkable")) {
                    substTemplate.setLinkable(Utils.parseTrueFalseString(propertyValue, false));
                } else if(propertyName.equals("haworth_name")) {
                    substTemplate.setHaworthName(propertyValue);
                } else if(propertyName.equals("mirrored_haworth_name")) {
                    substTemplate.setMirroredHaworthName(propertyValue);
                } else if(propertyName.equals("olinked_equivalent") && !propertyValue.equals("")) {
                    substTemplate.setOLinkedEquivalent(propertyValue);
                } else if(propertyName.equals("formula")) {
                    substTemplate.setFormula(propertyValue);
                    if(substTemplate.getFormula() != null) {
                        substTemplate.setComposition(new Composition(substTemplate.getFormula()));
                    }
                } else if(propertyName.equals("atoms")) {
                    List<?> atomList = property.getChildren();
                    Iterator<?> atomIter = atomList.iterator();
                    while(atomIter.hasNext()) {
                        org.jdom.Element atomtag = (org.jdom.Element) atomIter.next();
                        Atom a = Atom.parseXmlAtomTag(atomtag);
                        substTemplate.addAtom(a);
                    }
                } else if(propertyName.equals("atom_connections")) {
                    List<?> connectionList = property.getChildren();
                    Iterator<?> connectionIter = connectionList.iterator();
                    while(connectionIter.hasNext()) {
                        org.jdom.Element atomtag = (org.jdom.Element) connectionIter.next();
                        AtomConnection.parseXmlAtomConnectionTag(atomtag, substTemplate);
                    }
                } else if(propertyName.equals("valid_linking_positions")) {
                    List<?> vlpList = property.getChildren();
                    Iterator<?> vlpIter = vlpList.iterator();
                    while(vlpIter.hasNext()) {
                        org.jdom.Element vlpTag = (org.jdom.Element) vlpIter.next();
                        int id = Integer.parseInt(vlpTag.getAttributeValue("id"));
                        int atomId = Integer.parseInt(vlpTag.getAttributeValue("atom_id"));
                        Atom linkAtom = substTemplate.getAtomById(atomId);
                        if(linkAtom == null) {
                            throw new ResourcesDbException("Cannot get atom id " + atomId + " for substituent template " + substTemplate.getName());
                        }
                        Atom replAtom = null;
                        String replAtomStr = vlpTag.getAttributeValue("replaced_atom_id");
                        if(replAtomStr != null && replAtomStr.length() > 0) {
                            replAtom = substTemplate.getAtomById(Integer.parseInt(replAtomStr));
                            if(replAtom == null) {
                                throw new ResourcesDbException("Cannot get atom id " + replAtomStr + " for substituent template " + substTemplate.getName());
                            }
                        }
                        Double bo = null;
                        String boStr = vlpTag.getAttributeValue("bond_order");
                        if(boStr != null && boStr.length() > 0) {
                            bo = Double.parseDouble(boStr);
                        }
                        LinkageType defaultLinktype = null;
                        String linktypeStr = vlpTag.getAttributeValue("default_linkage_type");
                        if(linktypeStr != null && linktypeStr.length() > 0) {
                            try {
                                defaultLinktype = LinkageType.forName(linktypeStr);
                            } catch(GlycoconjugateException ge) {
                                //*** linktypeStr cannot be matched to a LinkageType ***
                                throw new ResourcesDbException("Error in reading substituent template from xml: unknown LinkageType " + linktypeStr, ge);
                            }
                        }
                        substTemplate.addValidLinkingPosition(id, linkAtom, replAtom, bo, defaultLinktype);
                    }
                } else if(propertyName.equals("fuzzy")) {
                    substTemplate.setFuzzy(Utils.parseTrueFalseString(propertyValue, false));
                } else if(propertyName.equals("synonyms")) {
                    List<?> synonymList = property.getChildren();
                    Iterator<?> synonymIter = synonymList.iterator();
                    while(synonymIter.hasNext()) {
                        org.jdom.Element synonym = (org.jdom.Element) synonymIter.next();
                        SubstituentAlias alias = parseXmlAliasTag(synonym);
                        if(alias != null) {
                            substTemplate.addAlias(alias);
                        }
                    }
                } else if(propertyName.equals("substituent_part")) {
                    substTemplate.setSubparts(parseSubstituentPartsTag(property));
                }
            }
            return(substTemplate);
        } else {
            return(null);
        }
    }
    
    private static SubstituentAlias parseXmlAliasTag(org.jdom.Element synonymElem) throws ResourcesDbException {
        SubstituentAlias alias = null;
        if(synonymElem.getName().equalsIgnoreCase("primary_alias") || synonymElem.getName().equalsIgnoreCase("secondary_alias")) {
            alias = new SubstituentAlias();
            GlycanNamescheme scheme = GlycanNamescheme.forName(synonymElem.getAttribute("namescheme").getValue());
            if(scheme == null) {
                throw new ResourcesDbException("Unknown or empty namescheme string: '" + synonymElem.getAttribute("namescheme").getValue() + "'");
            }
            alias.setNamescheme(scheme);
            String linkTypeName = synonymElem.getAttributeValue("linkage_type");
            if(linkTypeName != null) {
                try {
                    alias.setLinktype1(LinkageType.forName(linkTypeName));
                } catch(GlycoconjugateException ge) {
                    throw new ResourcesDbException("Error in reading substituent alias from xml: unknown LinkageType " + linkTypeName, ge);
                }
            }
            String linkTypeName2 = synonymElem.getAttributeValue("linkage_type2");
            if(linkTypeName2 != null) {
                try {
                    alias.setLinktype2(LinkageType.forName(linkTypeName2));
                } catch(GlycoconjugateException ge) {
                    throw new ResourcesDbException("Error in reading substituent alias from xml: unknown LinkageType " + linkTypeName2, ge);
                }
            }
            alias.setResidueIncludedName(synonymElem.getAttributeValue("residue_included"));
            alias.setSeparateDisplayName(synonymElem.getAttributeValue("separate_display"));
            if(synonymElem.getName().equalsIgnoreCase("primary_alias")) {
                alias.setIsPrimary(true);
            } else {
                alias.setIsPrimary(false);
            }
        }
        return(alias);
    }
    
    private static SubstituentSubpartTreeNode parseSubstituentPartsTag(org.jdom.Element partsTag) throws ResourcesDbException {
        SubstituentSubpartTreeNode substPartNode = null;
        substPartNode = new SubstituentSubpartTreeNode();
        substPartNode.setUserObject(partsTag.getAttributeValue("name"));
        substPartNode.setName(partsTag.getAttributeValue("name"));
        List<?> childTagsList = partsTag.getChildren();
        if(childTagsList != null) {
            Iterator<?> childtagIter = childTagsList.iterator();
            while(childtagIter.hasNext()) {
                org.jdom.Element childTag = (org.jdom.Element) childtagIter.next();
                if(childTag.getName().equalsIgnoreCase("substituent_part")) {
                    substPartNode.add(parseSubstituentPartsTag(childTag));
                } else {
                    throw new ResourcesDbException("Illegal child tag of substituent_part: " + childTag.getName());
                }
            }
        }
        return(substPartNode);
    }
    
    private static HashMap<String, Integer> parseMinMaxTags(org.jdom.Element parentTag) {
        HashMap<String, Integer> retMap = new HashMap<String, Integer>();
        List<?> childList = parentTag.getChildren();
        if(childList != null) {
            for(int i = 0; i < parentTag.getChildren().size(); i++) {
                org.jdom.Element childTag = (org.jdom.Element) parentTag.getChildren().get(i);
                if(childTag.getName().toLowerCase().equals("min")) {
                    try {
                        retMap.put("MIN", Integer.parseInt(childTag.getValue()));
                    } catch(NumberFormatException nfe) {
                        
                    }
                    continue;
                }
                if(childTag.getName().toLowerCase().equals("max")) {
                    try {
                        retMap.put("MAX", Integer.parseInt(childTag.getValue()));
                    } catch(NumberFormatException nfe) {
                        
                    }
                    continue;
                }
            }
        }
        return retMap;
    }
    
}
