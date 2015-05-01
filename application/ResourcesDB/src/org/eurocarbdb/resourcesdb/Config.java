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
package org.eurocarbdb.resourcesdb;

import org.eurocarbdb.resourcesdb.util.Utils;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class Config implements Cloneable {

    private static final String msTemplatesXmlDefaultFilename = "basetype_templates.xml";
    private static final String substituentTemplatesXmlDefaultFilename = "substituent_templates.xml";
    private static final String trivialnameTemplatesXmlDefaultFilename = "trivialname_templates.xml";
    private static final String aglyconTemplatesXmlDefaultFilename = "aglycon_templates.xml";
    private static final String elementsXmlDefaultFilename = "elements.xml";
    private static final String msDictionaryDefaultFilename = "ms_dictionary.xml";
    
    private String msDictionaryFilename = null;
    private boolean useMsDictionaryFromFileSystem = false;
    
    private boolean allowTrivialNames = true;
    private boolean forceTrivialNames = false;
    
    private boolean carboxylGroupsDeprotonated = false;
    
    private boolean preserveAlditolOrientation = false;
    
    private boolean buildMsDerivedData = false;
    
    private int printErrorMsgLevel = 0;
    
    private boolean configRead = false; //*** private flag to mark of the config file was already read or not ***
    
    private static Config globalConfig;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    /**
     * Create a default configuration
     */
    public Config() {
        
    }
    
    /**
     * Create a configuration from data stored in a config file
     * @param filename the file name of the configuration file
     * @throws ResourcesDbException
     */
    public Config(String filename) throws ResourcesDbException {
        this.readConfigFile(filename);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public static synchronized Config getGlobalConfig() {
        if(globalConfig == null) { //*** no global configuration set so far, so set it to a standard configuration ***
            globalConfig = new Config();
        }
        return globalConfig;
    }

    public static synchronized void setGlobalConfig(Config globalConfig) {
        Config.globalConfig = globalConfig;
    }
    
    /**
     * @return the monosaccharideTemplatesXmlURL
     */
    public URL getMonosaccharideTemplatesXmlUrl() {
        return Config.getMonosaccharideTemplatesDefaultXmlUrl();
    }
    
    public static URL getMonosaccharideTemplatesDefaultXmlUrl() {
        return(Config.class.getResource("/" + Config.msTemplatesXmlDefaultFilename));
    }
    
    /**
     * @return the substituentTemplatesXmlURL
     */
    public URL getSubstituentTemplatesXmlUrl() {
        return(getSubstituentTemplatesDefaultXmlUrl());
    }
    
    public static URL getSubstituentTemplatesDefaultXmlUrl() {
        return(Config.class.getResource("/" + Config.substituentTemplatesXmlDefaultFilename));        
    }
    
    /**
     * @return the aglyconTemplatesXmlURL
     */
    public URL getAglyconTemplatesXmlUrl() {
        return(getAglyconTemplatesDefaultXmlUrl());
    }
    
    public static URL getAglyconTemplatesDefaultXmlUrl() {
        return(Config.class.getResource("/" + Config.aglyconTemplatesXmlDefaultFilename));        
    }
    
    /**
     * @return the trivialnameTemplatesXmlURL
     */
    public URL getTrivialnameTemplatesXmlUrl() {
        return(getTrivialnameTemplatesDefaultXmlUrl());
    }
    
    public static URL getTrivialnameTemplatesDefaultXmlUrl() {
        return(Config.class.getResource("/" + Config.trivialnameTemplatesXmlDefaultFilename));        
    }
    
    /**
     * @return the elementsXmlLocation
     */
    public URL getElementsXmlUrl() {
        return(getElementsDefaultXmlUrl());
    }
    
    public static URL getElementsDefaultXmlUrl() {
        return(Config.class.getResource("/" + Config.elementsXmlDefaultFilename));        
    }
    
    public URL getMsDictionaryUrl() {
        URL dictUrl = null;
        if(this.useMsDictionaryFromFileSystem) {
            try {
                File dictFile = new File(this.getMsDictionaryFilename());
                dictUrl = dictFile.toURI().toURL();
            } catch(MalformedURLException mex) {
                System.err.println(mex);
            }
        } else {
            dictUrl = Config.class.getResource("/" + Config.msDictionaryDefaultFilename);
        }
        return dictUrl;
    }

    public String getMsDictionaryFilename() {
        return msDictionaryFilename;
    }

    public void setMsDictionaryFilename(String filename) {
        this.msDictionaryFilename = filename;
    }

    public boolean isUseMsDictionaryFromFileSystem() {
        return this.useMsDictionaryFromFileSystem;
    }

    public void setUseMsDictionaryFromFileSystem(boolean flag) {
        this.useMsDictionaryFromFileSystem = flag;
    }

    /**
     * @return the allowTrivialNames
     */
    public boolean isAllowTrivialNames() {
        return this.allowTrivialNames;
    }

    /**
     * Set the flag to mark if trivial names are allowed in MonosaccharideExporters.
     * If this flag is set to false, the forceTrivialNames flag is set to false as well.
     * @param flag the allowTrivialNames flag to set
     */
    public void setAllowTrivialNames(boolean flag) {
        this.allowTrivialNames = flag;
        if(!flag) {
            this.setForceTrivialNames(false);
        }
    }

    /**
     * @return the forceTrivialNames
     */
    public boolean isForceTrivialNames() {
        return this.forceTrivialNames;
    }

    /**
     * Set the flag to mark if trivial names are to be used in MonosaccharideExporters in any case,
     * regardless if they are primary synonyms or not.
     * If this flag is set to true, the allowTrivialNames flag is set to true as well.
     * @param flag the forceTrivialNames flag to set
     */
    public void setForceTrivialNames(boolean flag) {
        this.forceTrivialNames = flag;
        if(flag) {
            this.setAllowTrivialNames(true);
        }
    }

    public boolean isCarboxylGroupsDeprotonated() {
        return carboxylGroupsDeprotonated;
    }

    public void setCarboxylGroupsDeprotonated(boolean carboxylGroupsDeprotonated) {
        this.carboxylGroupsDeprotonated = carboxylGroupsDeprotonated;
    }

    public boolean isPreserveAlditolOrientation() {
        return this.preserveAlditolOrientation;
    }

    public void setPreserveAlditolOrientation(boolean preserveAlditolOri) {
        this.preserveAlditolOrientation = preserveAlditolOri;
    }

    public boolean isBuildMsDerivedData() {
        return this.buildMsDerivedData;
    }

    public void setBuildMsDerivedData(boolean flag) {
        this.buildMsDerivedData = flag;
    }

    public boolean isPrintErrorMsgs() {
        return this.printErrorMsgLevel > 0;
    }
    
    public boolean isPrintErrorMsgs(int level) {
        return this.printErrorMsgLevel >= level;
    }
    
    public int getPrintErrorMsgLevel() {
        return this.printErrorMsgLevel;
    }

    public void setPrintErrorMsgLevel(int value) {
        this.printErrorMsgLevel = value;
    }

    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public Config clone() {
        Config confClone = new Config();
        confClone.setAllowTrivialNames(this.isAllowTrivialNames());
        return(confClone);
    }
    
    public void readConfigFile() throws ResourcesDbException {
        if(!this.configRead) {
            String filename = System.getenv("org.eurocarbdb.resourcedb.configfile");
            if(filename != null) {
                readConfigFile(filename);
                this.configRead = true;
            } else {
                throw new ResourcesDbException("Cannot get config filename from environment variable org.eurocarbdb.resourcedb.configfile");
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public void readConfigFile(String filename) throws ResourcesDbException {
        SAXBuilder parser = new SAXBuilder();
        try {
            Document doc = parser.build(filename);
            org.jdom.Element root = doc.getRootElement();
            //listChildren(root, 0);
            List configList = root.getChildren();
            Iterator configIter = configList.iterator();
            while(configIter.hasNext()) {
                org.jdom.Element xmlElement = (org.jdom.Element) configIter.next();
                if(xmlElement.getName().equals("file_locations")) {
                } else if(xmlElement.getName().equals("residue_parsing")) {
                    parseResidueParsingPart(xmlElement);
                } else if(xmlElement.getName().equals("residue_name_generation")) {
                    parseResidueNameGenerationPart(xmlElement);
                }
            }
        } catch (JDOMException je) {
            System.out.println("JDOMException: " + je.getMessage());
            throw new ResourcesDbException("Error reading config file: " + je.getMessage());
        } catch (IOException ie) {
            System.out.println("IOException: " + ie.getMessage());
            throw new ResourcesDbException("Error reading config file: " + ie.getMessage());
        }  
    }
    
    @SuppressWarnings("unchecked")
    private void parseResidueNameGenerationPart(org.jdom.Element currentElement) {
        List childrenList = currentElement.getChildren();
        Iterator childrenIter = childrenList.iterator();
        while(childrenIter.hasNext()) {
            org.jdom.Element childTag = (org.jdom.Element) childrenIter.next();
            if(childTag.getName().equals("allow_trivial_names")) {
                this.setAllowTrivialNames(Utils.parseTrueFalseString(childTag.getValue(), true));
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void parseResidueParsingPart(org.jdom.Element currentElement) {
        List childrenList = currentElement.getChildren();
        Iterator childrenIter = childrenList.iterator();
        while(childrenIter.hasNext()) {
        }
    }

}
