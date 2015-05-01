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
import org.eurocarbdb.resourcesdb.ResourcesDbObject;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.GlycoconjugateException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.io.NameParsingException;
import org.eurocarbdb.resourcesdb.monosaccharide.CoreModification;
import org.eurocarbdb.resourcesdb.monosaccharide.CoreModificationTemplate;
import org.eurocarbdb.resourcesdb.monosaccharide.Modification;
import org.eurocarbdb.resourcesdb.monosaccharide.Monosaccharide;
import org.eurocarbdb.resourcesdb.monosaccharide.Substitution;
import org.eurocarbdb.resourcesdb.util.NumberUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class MonosaccharideDictionary extends ResourcesDbObject {
    
    private HashMap<GlycanNamescheme, HashMap<String, MonosaccharideDictionaryEntry>> schemeMap;
    private HashMap<GlycanNamescheme, ArrayList<String>> nameListMap;
    
    private static final int ERROR_POSITION = -999;

    //*****************************************************************************
    //*** Constructors: ***********************************************************
    //*****************************************************************************
    
    public MonosaccharideDictionary() {
        this(null, null);
    }
    
    public MonosaccharideDictionary(Config conf) {
        this(conf, null);
    }
    
    public MonosaccharideDictionary(TemplateContainer container) {
        this(null, container);
    }
    
    public MonosaccharideDictionary(Config conf, TemplateContainer container) {
        this.init();
        this.setConfig(conf);
        this.setTemplateContainer(container);
    }
    
    //*****************************************************************************
    //*** Getters/Setters: ********************************************************
    //*****************************************************************************
    
    public MonosaccharideDictionaryEntry getEntry(GlycanNamescheme scheme, String msName) {
        if(this.schemeMap == null) {
            this.fillMap();
        }
        HashMap<String, MonosaccharideDictionaryEntry> nameMap = this.schemeMap.get(scheme);
        if(nameMap != null) {
            if(scheme.isCaseSensitive()) {
                return nameMap.get(msName);
            }
            return nameMap.get(msName.toLowerCase());
        }
        return null;
    }
    
    private HashMap<String, MonosaccharideDictionaryEntry> getEntryMap(GlycanNamescheme scheme) {
        if(this.schemeMap == null) {
            this.schemeMap = new HashMap<GlycanNamescheme, HashMap<String, MonosaccharideDictionaryEntry>>();
        }
        if(this.schemeMap.get(scheme) == null) {
            this.schemeMap.put(scheme, new HashMap<String, MonosaccharideDictionaryEntry>());
        }
        return this.schemeMap.get(scheme);
    }
    
    private ArrayList<String> getNamesList(GlycanNamescheme scheme) {
        if(this.nameListMap == null) {
            this.nameListMap = new HashMap<GlycanNamescheme, ArrayList<String>>();
        }
        if(this.nameListMap.get(scheme) == null) {
            this.nameListMap.put(scheme, new ArrayList<String>());
        }
        return this.nameListMap.get(scheme);
    }
    
    public void addEntry(MonosaccharideDictionaryEntry entry) {
        HashMap<String, MonosaccharideDictionaryEntry> entryMap = this.getEntryMap(entry.getScheme());
        ArrayList<String> namesList = this.getNamesList(entry.getScheme());
        if(entry.getScheme().isCaseSensitive()) {
            entryMap.put(entry.getForeignName(), entry);
            namesList.add(entry.getForeignName());
        } else {
            entryMap.put(entry.getForeignName().toLowerCase(), entry);
            namesList.add(entry.getForeignName().toLowerCase());
        }
    }
    
    //*****************************************************************************
    //*** Methods to fill the dictionary from an xml file: ************************
    //*****************************************************************************
    
    private void fillMap() {
        try {
            this.fillMap(this.getConfig().getMsDictionaryUrl());
        } catch(ResourcesDbException rEx) {
            System.err.println(rEx);
            rEx.printStackTrace();
            this.schemeMap = new HashMap<GlycanNamescheme, HashMap<String,MonosaccharideDictionaryEntry>>();
        }
    }
    
    private void fillMap(URL xmlUrl) throws ResourcesDbException {
        SAXBuilder parser = new SAXBuilder();
        try {
            Document doc = parser.build(xmlUrl);
            org.jdom.Element root = doc.getRootElement();
            List<?> xmlTemplateList = root.getChildren();
            Iterator<?> templatesIter = xmlTemplateList.iterator();
            while(templatesIter.hasNext()) {
                org.jdom.Element xmlEntry = (org.jdom.Element) templatesIter.next();
                try {
                    MonosaccharideDictionaryEntry entry = getEntryFromXmlTree(xmlEntry);
                    //System.out.println(entry);
                    if(entry != null) {
                        this.addEntry(entry);
                    }
                } catch(ResourcesDbException rEx) {
                    System.err.println(rEx);
                }
            }
        } catch (JDOMException je) {
            throw new ResourcesDbException("Exception in reading TrivialnameTemplate XML file.", je);
        } catch (IOException ie) {
            throw new ResourcesDbException("Exception in reading TrivialnameTemplate XML file.", ie);
        }
    }
    
    private MonosaccharideDictionaryEntry getEntryFromXmlTree(org.jdom.Element xmlEntry) throws ResourcesDbException {
        MonosaccharideDictionaryEntry dictEntry = null;
        if(xmlEntry.getName().equalsIgnoreCase("MS")) {
            dictEntry = new MonosaccharideDictionaryEntry(this.getConfig(), this.getTemplateContainer());
            //*** set name scheme: ***
            GlycanNamescheme scheme = GlycanNamescheme.forName(xmlEntry.getAttributeValue("from"));
            if(scheme == null) {
                System.err.println("Cannot get Namescheme '" + xmlEntry.getAttributeValue("from") + "'");
                return null;
            } else {
                dictEntry.setScheme(scheme);
            }
            //*** set name in given notation scheme: ***
            dictEntry.setForeignName(xmlEntry.getAttributeValue("name"));
            //*** set basetype string: ***
            dictEntry.setBasetypeStr(xmlEntry.getAttributeValue("basetype"));
            List<?> substList = xmlEntry.getChildren("subst");
            if(substList != null) {
                Iterator<?> substIter = substList.iterator();
                while(substIter.hasNext()) {
                    org.jdom.Element substElement = (org.jdom.Element) substIter.next();
                    Modification subst = this.getSubstitutionFromXmlTree(substElement);
                    if(subst != null) {
                        dictEntry.addSubstitution(subst);
                    }
                }
            }
        }
        return dictEntry;
    }
    
    private Modification getSubstitutionFromXmlTree(org.jdom.Element substElement) throws ResourcesDbException {
        Substitution subst = null;
        CoreModification coremod = null;
        SubstituentTemplate substTemplate = null;
        if(substElement.getName().equalsIgnoreCase("subst")) {
            String substName = substElement.getAttributeValue("name");
            if(substName.equalsIgnoreCase("anhydro") || substName.equalsIgnoreCase("lactone")) {
                coremod = new CoreModification();
                coremod.setTemplate(CoreModificationTemplate.forName(substName));
            } else {
                subst = new Substitution();
                substTemplate = this.getTemplateContainer().getSubstituentTemplateContainer().forName(GlycanNamescheme.GLYCOCT, substName);
                if(substTemplate == null) {
                    throw new ResourcesDbException("Cannot get template for substituent " + substName);
                }
                subst.setTemplate(substTemplate);
            }
            String pos1Str = substElement.getAttributeValue("pos1");
            if(pos1Str != null && pos1Str.length() > 0) {
                ArrayList<Integer> pos1List = NumberUtils.parseMultipleIntStr(pos1Str, "\\|", ERROR_POSITION);
                for(int i = 0; i < pos1List.size(); i++) {
                    Integer pos1i = pos1List.get(i);
                    if(pos1i.intValue() == ERROR_POSITION) {
                        throw new ResourcesDbException("Illegal position value in " + pos1Str);
                    }
                    if(pos1i.intValue() == -1) {
                        pos1List.set(i, 0);
                    }
                }
                if(subst != null) {
                    subst.setPosition1(pos1List);
                    subst.setSubstituentPosition1(substTemplate.getDefaultLinkingPosition1());
                    LinkageType linktype;
                    try {
                        linktype = LinkageType.forName(substElement.getAttributeValue("pos1F"));
                        subst.setLinkagetype1(linktype);
                    } catch(GlycoconjugateException ge) {
                        throw new ResourcesDbException("illegal linkage type: '" + substElement.getAttributeValue("pos1F") + "'", ge);
                    }
                } else {
                    coremod.setPosition1(pos1List);
                }
            }
            String pos2Str = substElement.getAttributeValue("pos2");
            if(pos2Str != null && pos2Str.length() > 0) {
                ArrayList<Integer> pos2List = NumberUtils.parseMultipleIntStr(pos2Str, "\\|", ERROR_POSITION);
                for(int i = 0; i < pos2List.size(); i++) {
                    Integer pos2i = pos2List.get(i);
                    if(pos2i.intValue() == ERROR_POSITION) {
                        throw new ResourcesDbException("Illegal position value in " + pos2Str);
                    }
                    if(pos2i.intValue() == -1) {
                        pos2List.set(i, 0);
                    }
                }
                if(subst != null) {
                    subst.setPosition2(pos2List);
                    subst.setSubstituentPosition2(substTemplate.getDefaultLinkingPosition2());
                    LinkageType linktype;
                    try {
                        linktype = LinkageType.forName(substElement.getAttributeValue("pos2F"));
                        subst.setLinkagetype2(linktype);
                    } catch(GlycoconjugateException ge) {
                        throw new ResourcesDbException("illegal linkage type: '" + substElement.getAttributeValue("pos2F") + "'", ge);
                    }
                } else {
                    coremod.setPosition2(pos2List);
                }
            }
        }
        if(subst != null) {
            return subst;
        } else {
            return coremod;
        }
    }
    
    //*****************************************************************************
    //*** other Methods: **********************************************************
    //*****************************************************************************
    
    public void init() {
        this.setConfig(null);
        this.setTemplateContainer(null);
    }
    
    public void checkDictionary(GlycanNamescheme scheme, int start, int num) {
        if(this.schemeMap == null) {
            this.fillMap();
        }
        HashMap<String, MonosaccharideDictionaryEntry> entryMap = this.getEntryMap(scheme);
        if(entryMap == null) {
            System.err.println("no entries for namescheme " + scheme + " found");
            return;
        }
        int count = 0;
        TemplateContainer container = new TemplateContainer();
        //for(MonosaccharideDictionaryEntry entry : entryMap.values()) {
        for(String foreignName : this.getNamesList(scheme)) {
            MonosaccharideDictionaryEntry entry = this.getEntry(scheme, foreignName);
            count ++;
            if(count < start) {
                continue;
            }
            if(count >= start + num) {
                return;
            }
            Monosaccharide entryMs = null;
            entryMs = entry.getMs();
            if(entryMs == null) {
                System.err.println("cannot get ms from entry " + entry);
                continue;
            }
            if(!entryMs.getBasetype().getName().equals(entry.getBasetypeStr())) {
                System.out.println("basetype mismatch: " + entry.getForeignName());
                System.out.println("entry bt: " + entry.getBasetypeStr());
                System.out.println("gen. bt:  " + entryMs.getBasetype().getName());
            }
            Monosaccharide ms = null;
            try {
                ms = new Monosaccharide(entry.getScheme(), entry.getForeignName(), container);
                ms.buildName();
            } catch(ResourcesDbException rEx) {
                System.out.println("Exception in parsing " + entry.getForeignName());
                System.out.println(rEx);
                if(rEx.getClass().equals(NameParsingException.class)) {
                    String expl = ((NameParsingException) rEx).buildExplanationString();
                    System.out.println(expl);
                }
                continue;
            }
            if(!entryMs.equals(ms)) {
                System.out.println("Mismatch: " + entry.getForeignName());
                System.out.println("entry:  " + entryMs);
                System.out.println("parsed: " + ms);
            } else {
                System.out.println("Ok: " + entry.getForeignName() + "  " + ms);
            }
            System.out.println();
        }
    }
}
