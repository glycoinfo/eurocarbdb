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
import org.eurocarbdb.resourcesdb.atom.Periodic;
import org.eurocarbdb.resourcesdb.nonmonosaccharide.AglyconAlias;
import org.eurocarbdb.resourcesdb.nonmonosaccharide.AglyconTemplate;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class AglyconTemplateContainer {

    private Config config = null;
    
    //*****************************************************************************
    //*** Constructors: ***********************************************************
    //*****************************************************************************
    
    public AglyconTemplateContainer() {
        this.setConfig(new Config());
    }
    
    public AglyconTemplateContainer(Config conf) {
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
    
    private ArrayList<AglyconTemplate> templateList;
    
    public ArrayList<AglyconTemplate> getTemplateList() throws ResourcesDbException {
        if(this.templateList == null) {
            this.templateList = this.readTemplates(this.getConfig());
        }
        return this.templateList;
    }
    
    public AglyconTemplate getAglyconTemplateByName(GlycanNamescheme scheme, String name) throws ResourcesDbException {
        for(AglyconTemplate aglyc : this.getTemplateList()) {
            for(AglyconAlias alias : aglyc.getAliasList()) {
                if(alias.getNamescheme().equals(scheme)) {
                    if(scheme.isCaseSensitive() && alias.getName().equals(name)) {
                        return aglyc;
                    }
                    if(!scheme.isCaseSensitive() && alias.getName().equalsIgnoreCase(name)) {
                        return aglyc;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * HashMap that stores a list of valid substituent names for each name scheme
     */
    private HashMap<GlycanNamescheme, ArrayList<String>> aglycTemplateListsMap;

    public ArrayList<String> getTemplateNameList(GlycanNamescheme namescheme) throws ResourcesDbException {
        if(this.aglycTemplateListsMap == null) {
            this.aglycTemplateListsMap = new HashMap<GlycanNamescheme, ArrayList<String>>();
        }
        ArrayList<String> nameschemeTemplateList = this.aglycTemplateListsMap.get(namescheme);
        if(nameschemeTemplateList == null) {
            nameschemeTemplateList = new ArrayList<String>();
            for(AglyconTemplate aglyc : this.getTemplateList()) {
                for(AglyconAlias alias : aglyc.getAliasList()) {
                    if(alias.getNamescheme().equals(namescheme)) {
                        nameschemeTemplateList.add(alias.getName());
                    }
                }
            }
        }
        return nameschemeTemplateList;
    }
    
    //*****************************************************************************
    //*** Methods for filling static template data maps/lists: ********************
    //*****************************************************************************
    
    public ArrayList<AglyconTemplate> readTemplates(Config conf) throws ResourcesDbException {
        Periodic.setDataIfNotSet(conf); //*** make sure element data is set first ***
        //*** read data: ***
        return readTemplatesFromXmlFile(conf.getAglyconTemplatesXmlUrl());
    }
    
    public ArrayList<AglyconTemplate> readTemplatesFromXmlFile(URL xmlUrl) throws ResourcesDbException {
        ArrayList<AglyconTemplate> tmplList = new ArrayList<AglyconTemplate>();
        SAXBuilder parser = new SAXBuilder();
        try {
            Document doc = parser.build(xmlUrl);
            org.jdom.Element root = doc.getRootElement();
            List<?> templateList = root.getChildren();
            Iterator<?> templatesIter = templateList.iterator();
            while(templatesIter.hasNext()) {
                org.jdom.Element xmlTemplate = (org.jdom.Element) templatesIter.next();
                AglyconTemplate template = getTemplateFromXmlTree(xmlTemplate);
                if(template != null) {
                    //System.out.println("Template: " + template.toString());
                    tmplList.add(template);
                }
            }
        } catch (JDOMException je) {
            throw new ResourcesDbException("Exception in setting aglycon template data from xml file.", je);
        } catch (IOException ie) {
            throw new ResourcesDbException("Exception in setting aglycon template data from xml file.", ie);
        }  
        return tmplList;
    }
    
    private static AglyconTemplate getTemplateFromXmlTree(org.jdom.Element xmlTemplate) throws ResourcesDbException {
        if(xmlTemplate.getName().equalsIgnoreCase("aglycon")) {
            AglyconTemplate aglycTemplate = new AglyconTemplate();
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
                    aglycTemplate.setName(propertyValue);
                    aglycTemplate.setPrimaryAlias(GlycanNamescheme.GLYCOCT, propertyValue);
                } else if(propertyName.equals("valence") && !propertyValue.equals("")) {
                    aglycTemplate.setValence(Integer.parseInt(propertyValue));
                } else if(propertyName.equals("default_linking_position1") && !propertyValue.equals("")) {
                    aglycTemplate.setDefaultLinkingPosition1(Integer.parseInt(propertyValue));
                } else if(propertyName.equals("default_linking_position2") && !propertyValue.equals("")) {
                    aglycTemplate.setDefaultLinkingPosition2(Integer.parseInt(propertyValue));
                } else if(propertyName.equals("formula")) {
                    aglycTemplate.setFormula(propertyValue);
                } else if(propertyName.equals("class")) {
                    aglycTemplate.setAglyconClass(propertyValue);
                } else if(propertyName.equals("atoms")) {
                    List<?> atomList = property.getChildren();
                    Iterator<?> atomIter = atomList.iterator();
                    while(atomIter.hasNext()) {
                        org.jdom.Element atomtag = (org.jdom.Element) atomIter.next();
                        Atom a = Atom.parseXmlAtomTag(atomtag);
                        aglycTemplate.addAtom(a);
                    }
                } else if(propertyName.equals("atom_connections")) {
                    List<?> connectionList = property.getChildren();
                    Iterator<?> connectionIter = connectionList.iterator();
                    while(connectionIter.hasNext()) {
                        org.jdom.Element atomtag = (org.jdom.Element) connectionIter.next();
                        AtomConnection.parseXmlAtomConnectionTag(atomtag, aglycTemplate);
                    }
                } else if(propertyName.equals("valid_linking_positions")) {
                    List<?> vlpList = property.getChildren();
                    Iterator<?> vlpIter = vlpList.iterator();
                    while(vlpIter.hasNext()) {
                        org.jdom.Element vlpTag = (org.jdom.Element) vlpIter.next();
                        int id = Integer.parseInt(vlpTag.getAttributeValue("id"));
                        int atomId = Integer.parseInt(vlpTag.getAttributeValue("atom_id"));
                        Atom linkAtom = aglycTemplate.getAtomById(atomId);
                        if(linkAtom == null) {
                            throw new ResourcesDbException("Cannot get atom id " + atomId + " for aglycon template " + aglycTemplate.getName());
                        }
                        Atom replAtom = null;
                        String replAtomStr = vlpTag.getAttributeValue("replaced_atom_id");
                        if(replAtomStr != null && replAtomStr.length() > 0) {
                            replAtom = aglycTemplate.getAtomById(Integer.parseInt(replAtomStr));
                            if(replAtom == null) {
                                throw new ResourcesDbException("Cannot get atom id " + replAtomStr + " for aglycon template " + aglycTemplate.getName());
                            }
                        }
                        Double bo = null;
                        String boStr = vlpTag.getAttributeValue("bond_order");
                        if(boStr != null && boStr.length() > 0) {
                            bo = Double.parseDouble(boStr);
                        }
                        aglycTemplate.addValidLinkingPosition(id, linkAtom, replAtom, bo, null);
                    }
                } else if(propertyName.equals("synonyms")) {
                    List<?> synonymList = property.getChildren();
                    Iterator<?> synonymIter = synonymList.iterator();
                    while(synonymIter.hasNext()) {
                        org.jdom.Element synonym = (org.jdom.Element) synonymIter.next();
                        AglyconAlias alias = parseXmlAliasTag(synonym);
                        if(alias != null) {
                            aglycTemplate.addAlias(alias);
                            if(alias.isPrimaryName()) {
                                aglycTemplate.setPrimaryAlias(alias.getNamescheme(), alias.getName());
                            }
                        }
                    }
                }
            }
            return(aglycTemplate);
        } else {
            return(null);
        }
    }
    
    private static AglyconAlias parseXmlAliasTag(org.jdom.Element synonym) {
        AglyconAlias alias = null;
        if(synonym.getName().equalsIgnoreCase("primary_alias") || synonym.getName().equalsIgnoreCase("secondary_alias")) {
            alias = new AglyconAlias();
            GlycanNamescheme scheme = GlycanNamescheme.forName(synonym.getAttribute("namescheme").getValue());
            alias.setNamescheme(scheme);
            alias.setName(synonym.getAttributeValue("name"));
            if(synonym.getName().equalsIgnoreCase("primary_alias")) {
                alias.setIsPrimary(true);
            } else {
                alias.setIsPrimary(false);
            }
        }
        return(alias);
    }
    
}
