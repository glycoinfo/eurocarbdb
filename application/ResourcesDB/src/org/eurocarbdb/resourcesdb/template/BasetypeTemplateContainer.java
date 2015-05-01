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
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.monosaccharide.Monosaccharide;
import org.eurocarbdb.resourcesdb.monosaccharide.StereoConfiguration;
import org.eurocarbdb.resourcesdb.monosaccharide.Stereocode;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class BasetypeTemplateContainer {

    private Config config = null;
    
    //*****************************************************************************
    //*** Constructors: ***********************************************************
    //*****************************************************************************
    
    public BasetypeTemplateContainer() {
        this.setConfig(new Config());
    }
    
    public BasetypeTemplateContainer(Config conf) {
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
    
    private HashMap<String, BasetypeTemplate> basetypeTemplateByBasetypeNameMap;
    private ArrayList<String> basetypeListSpecific;
    private ArrayList<String> basetypeListSuperclass;
    private HashMap<String, BasetypeTemplate> basetypeTemplateByStereocodeMap;

    public HashMap<String, BasetypeTemplate> getBasetypeTemplateByBasetypeNameMap() throws ResourcesDbException {
        if(basetypeTemplateByBasetypeNameMap == null) {
            this.setData(this.getConfig());
        }
        return basetypeTemplateByBasetypeNameMap;
    }

    public void setBasetypeTemplateByBasetypeNameMap(HashMap<String, BasetypeTemplate> msTemplateByBasetypeMap) {
        this.basetypeTemplateByBasetypeNameMap = msTemplateByBasetypeMap;
    }
    
    public BasetypeTemplate getBasetypeTemplateByName(String basetypeName) throws ResourcesDbException {
        return(getBasetypeTemplateByBasetypeNameMap().get(basetypeName.toLowerCase()));
    }
    
    public ArrayList<String> getBasetypeList() throws ResourcesDbException {
        ArrayList<String> basetypeList = new ArrayList<String>();
        basetypeList.addAll(getBasetypeListSuperclass());
        basetypeList.addAll(getBasetypeListSpecific());
        return(basetypeList);
    }

    public ArrayList<String> getBasetypeListSuperclass() throws ResourcesDbException {
        if(basetypeListSuperclass == null) {
            this.setData(this.getConfig());
        }
        return basetypeListSuperclass;
    }

    public void setBasetypeListSuperclass(ArrayList<String> basetypeListSuperclass) {
        this.basetypeListSuperclass = basetypeListSuperclass;
    }

    public ArrayList<String> getBasetypeListSpecific() throws ResourcesDbException {
        if(basetypeListSpecific == null) {
            this.setData(this.getConfig());
        }
        return basetypeListSpecific;
    }

    public void setBasetypeListSpecific(ArrayList<String> basetypeListSpecific) {
        this.basetypeListSpecific = basetypeListSpecific;
    }
    
    /**
     * @return the basetypeTemplateByStereocodeMap
     */
    private HashMap<String, BasetypeTemplate> getBasetypeTemplateByStereocodeMap() throws ResourcesDbException {
        if(basetypeTemplateByStereocodeMap == null) {
            this.setData(this.getConfig());
        }
        return basetypeTemplateByStereocodeMap;
    }

    /**
     * @param templateByStereocodeMap the basetypeTemplateByStereocodeMap to set
     */
    private void setBasetypeTemplateByStereocodeMap(HashMap<String, BasetypeTemplate> templateByStereocodeMap) {
        this.basetypeTemplateByStereocodeMap = templateByStereocodeMap;
    }
    
    public BasetypeTemplate getBasetypeTemplateByStereoString(String stereo) throws ResourcesDbException {
        if(Stereocode.stereoStringHasRelativePosition(stereo)) {
            if(Stereocode.stereoStringContainsAbsoluteAndRelative(stereo)) {
                throw new ResourcesDbException("Cannot get basetype template by a stereocode string that contains both absolute and relative configurations: " + stereo);
            }
            stereo = Stereocode.relativeToAbsolute(stereo);
        }
        if(Stereocode.getConfigurationFromStereoString(stereo).equals(StereoConfiguration.Laevus)) {
            stereo = Stereocode.changeDLinStereoString(stereo);
        }
        BasetypeTemplate tmpl = getBasetypeTemplateByStereocodeMap().get(stereo);
        if(tmpl == null) {
            throw new ResourcesDbException("Cannot get basetype template by stereocode string " + stereo);
        }
        return(tmpl);
    }
    
    public BasetypeTemplate getSuperclassTemplateBySize(int size) throws ResourcesDbException {
        ArrayList<String> superclassTemplateNames = this.getBasetypeListSuperclass();
        for(int i = 0; i < superclassTemplateNames.size(); i++) {
            BasetypeTemplate template = this.getBasetypeTemplateByName(superclassTemplateNames.get(i));
            if(template.getSize() == size) {
                return(template);
            }
        }
        return(null);
    }
    
    /** 
     * Determine the root templates for a given monosaccharide
     * The stereocode of the monosaccharide has to be "final", i.e. it must comprise all positions (1 to n) and positions with loss of stereochemistry have to be marked correctly.
     * @param ms The monosaccharide, for which the root templates are to be assigned
     * @return ArrayList of Monosaccharide Templates
     * @throws ResourcesDbException
     */
    public ArrayList<BasetypeTemplate> assignRootTemplates(Monosaccharide ms) throws ResourcesDbException {
        ArrayList<BasetypeTemplate> rootTemplates;
        if(ms.getSize() < 7) {
            String stereo = ms.getStereoStr();
            if(ms.getRingStart() > 1) {
                //*** make sure no anomeric is set (only matters if ring start is > 1, as position 1 is removed anyway ***
                stereo = stereo.substring(0, ms.getRingStart() - 1) + StereoConfiguration.Nonchiral.getStereosymbol() + stereo.substring(ms.getRingStart());
            }
            stereo = stereo.substring(1, stereo.length() - 1);
            rootTemplates = Stereocode.getRootTemplatesByStereoString(stereo, this);
            if(ms.isAlditol()) {
                ArrayList<BasetypeTemplate> rotatedTemplates = Stereocode.getRootTemplatesByStereoString(Stereocode.rotateStereoString(stereo), this);
                rootTemplates.addAll(rotatedTemplates);
            }
        } else {
            rootTemplates = new ArrayList<BasetypeTemplate>();
        }
        return(rootTemplates);
    }

    //*****************************************************************************
    //*** Methods for filling static template data maps/lists: ********************
    //*****************************************************************************
    
    public void setData(Config conf) throws ResourcesDbException {
        URL xmlUrl = conf.getMonosaccharideTemplatesXmlUrl();
        setDataFromXmlFile(xmlUrl);
    }
    
    private void setDataFromXmlFile(URL fileUrl) throws ResourcesDbException {
        HashMap<String, BasetypeTemplate> templateByBasetypeMap = new HashMap<String, BasetypeTemplate>();
        HashMap<String, BasetypeTemplate> templateByStereostrMap = new HashMap<String, BasetypeTemplate>();
        ArrayList<String> superclassList = new ArrayList<String>();
        ArrayList<String> specificList = new ArrayList<String>();
        SAXBuilder parser = new SAXBuilder();
        try {
            Document doc = parser.build(fileUrl);
            org.jdom.Element root = doc.getRootElement();
            List<?> templateList = root.getChildren();
            Iterator<?> templatesIter = templateList.iterator();
            while(templatesIter.hasNext()) {
                org.jdom.Element xmlTemplate = (org.jdom.Element) templatesIter.next();
                BasetypeTemplate template = getTemplateFromXmlTree(xmlTemplate);
                if(template != null) {
                    if(template.isSuperclass()) {
                        superclassList.add(template.getBaseName().toLowerCase());
                    } else {
                        specificList.add(template.getBaseName().toLowerCase());
                    }
                    templateByBasetypeMap.put(template.getBaseName().toLowerCase(), template);
                    templateByStereostrMap.put(template.getStereocode(), template);                    
                }
            }
        } catch (JDOMException je) {
            throw new ResourcesDbException("Exception in parsing basetype templates from xml file.", je);
        } catch (IOException ie) {
            throw new ResourcesDbException("Exception in parsing basetype templates from xml file.", ie);
        }   
        setBasetypeTemplateByBasetypeNameMap(templateByBasetypeMap);
        setBasetypeTemplateByStereocodeMap(templateByStereostrMap);
        setBasetypeListSpecific(specificList);
        setBasetypeListSuperclass(superclassList);
    }
    
    private static BasetypeTemplate getTemplateFromXmlTree(org.jdom.Element xmlTemplate) {
        BasetypeTemplate mt = null;
        if(xmlTemplate.getName().equals("template")) {
            mt = new BasetypeTemplate();
            List<?> propList = xmlTemplate.getChildren();
            Iterator<?> propIter = propList.iterator();
            while(propIter.hasNext()) {
                org.jdom.Element property = (org.jdom.Element) propIter.next();
                if(property.getName().equals("name")) {
                    mt.setBaseName(property.getValue());
                } else if(property.getName().equals("longname")) {
                    mt.setLongName(property.getValue());
                } else if(property.getName().equals("size")) {
                    mt.setSize(Integer.parseInt(property.getValue()));
                } else if(property.getName().equals("carbonyl_position")) {
                    mt.setCarbonylPosition(Integer.parseInt(property.getValue()));
                } else if(property.getName().equals("stereocode")) {
                    mt.setStereocode(property.getValue());
                } else if(property.getName().equals("is_superclass")) {
                    mt.setIsSuperclass(property.getValue().toLowerCase().equals("true"));
                } else if(property.getName().equals("default_configuration")) {
                    if(property.getValue().length() > 0) {
                        try {
                            mt.setDefaultConfiguration(StereoConfiguration.forNameOrSymbol(property.getValue()));
                        } catch(ResourcesDbException rex) {
                            System.err.println("illegal stereoconfiguration symbol: '" + property.getValue() + "'");
                        }
                    }
                } else if(property.getName().equals("default_ringtype")) {
                    if(property.getValue().equals("p")) {
                        mt.setDefaultRingend(mt.getCarbonylPosition() + 4);
                    } else if(property.getValue().equals("f")) {
                        mt.setDefaultRingend(mt.getCarbonylPosition() + 3);
                    }
                } else {
                    System.err.println("warning: unknown tag in monosaccharide templates xml file: " + property.getName());
                }
            }
        }
        return(mt);
    }
    
}
