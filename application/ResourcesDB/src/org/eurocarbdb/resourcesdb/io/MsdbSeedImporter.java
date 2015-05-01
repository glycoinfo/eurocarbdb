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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.ResourcesDbObject;
import org.eurocarbdb.resourcesdb.monosaccharide.CoreModificationTemplate;
import org.eurocarbdb.resourcesdb.monosaccharide.Monosaccharide;
import org.eurocarbdb.resourcesdb.monosaccharide.MonosaccharideDataBuilder;
import org.eurocarbdb.resourcesdb.monosaccharide.MonosaccharideSynonym;
import org.eurocarbdb.resourcesdb.monosaccharide.MonosaccharideValidation;
import org.eurocarbdb.resourcesdb.monosaccharide.Ringtype;
import org.eurocarbdb.resourcesdb.monosaccharide.Substitution;
import org.eurocarbdb.resourcesdb.representation.*;
import org.eurocarbdb.resourcesdb.util.FileUtils;
import org.eurocarbdb.resourcesdb.util.NumberUtils;
import org.eurocarbdb.resourcesdb.util.Utils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
* Class to import seed files for filling MonoSaccharideDB
* @author Thomas Luetteke
*
*/
public class MsdbSeedImporter extends ResourcesDbObject {
    
    private GlycanNamescheme globalScheme;
    
    /**
     * the root path for import of representations
     */
    private static String rootpath = "/home/thomas/eclipse_projects/ResourcesDB/lib/";
    
    //*****************************************************************************
    //*** methods to read seed file: **********************************************
    //*****************************************************************************
    
    public void parseMsdbSeed(URL seedFileUrl, int offset, int quantity, boolean writeToDb) throws ResourcesDbException {
        SAXBuilder parser = new SAXBuilder();
        int count = 0;
        try {
            Document doc = parser.build(seedFileUrl);
            org.jdom.Element root = doc.getRootElement();
            if(root.getAttributeValue("namescheme") != null) {
                GlycanNamescheme scheme = GlycanNamescheme.forName(root.getAttributeValue("namescheme"));
                if(scheme != null) {
                    this.globalScheme = scheme;
                } else {
                    System.err.println("cannot get namescheme '" + root.getAttributeValue("namescheme") + "'");
                    this.globalScheme = GlycanNamescheme.AUTO;
                }
            }
            List<?> templateList = root.getChildren();
            Iterator<?> templatesIter = templateList.iterator();
            while(templatesIter.hasNext()) {
                org.jdom.Element xmlTemplate = (org.jdom.Element) templatesIter.next();
                count ++;
                if(count > offset && count <= offset + quantity) {
                    readTemplateFromXmlTree(xmlTemplate, writeToDb);
                }
            }
        } catch (JDOMException je) {
            throw new ResourcesDbException("JDOMException: " + je.getMessage());
        } catch (IOException ie) {
            throw new ResourcesDbException("IOException: " + ie.getMessage());
        }  
    }
    
    private void readTemplateFromXmlTree(org.jdom.Element xmlElement, boolean writeToDb) {
        if(xmlElement.getName().equalsIgnoreCase("monosaccharide")) {
            String msName = null;
            GlycanNamescheme scheme = null;
            if(xmlElement.getAttribute("name") != null) {
                msName = xmlElement.getAttributeValue("name");
            }
            if(xmlElement.getAttributeValue("scheme") != null) {
                scheme = GlycanNamescheme.forName(xmlElement.getAttributeValue("scheme"));
            }
            int msCount = 0;
            ArrayList<ResidueRepresentation> repList = new ArrayList<ResidueRepresentation>();
            ArrayList<MonosaccharideSynonym> aliasList = new ArrayList<MonosaccharideSynonym>();
            List<?> propList = xmlElement.getChildren();
            if(propList != null && propList.size() > 0) {
                Iterator<?> propIter = propList.iterator();
                while(propIter.hasNext()) {
                    org.jdom.Element propertyTag = (org.jdom.Element) propIter.next();
                    String propertyName = propertyTag.getName().toLowerCase();
                    String propertyValue = propertyTag.getValue();
                    if(propertyValue == null) {
                        propertyValue = "";
                    }
                    if(propertyName.equalsIgnoreCase("name")) {
                        msName = propertyValue;
                        if(propertyTag.getAttributeValue("scheme") != null) {
                            scheme = GlycanNamescheme.forName(propertyTag.getAttributeValue("scheme"));
                        }
                    } else if(propertyName.equalsIgnoreCase("count")) {
                        msCount = NumberUtils.parseIntStr(propertyValue, new Integer(-1));
                    } else if(propertyName.equalsIgnoreCase("representation")) {
                        ResidueRepresentation rep = getRepresentationFromXmlTag(propertyTag);
                        if(rep != null) {
                            repList.add(rep);
                        }
                    } else if(propertyName.equalsIgnoreCase("alias")) {
                        MonosaccharideSynonym alias = getAliasFromXmlTag(propertyTag);
                        if(alias != null) {
                            aliasList.add(alias);
                        }
                    } else {
                        System.err.println("unknown tag: " + propertyName);
                    }
                }
            }
            System.out.println("name / count: " + msName + " / " + msCount);
            if(msName.toLowerCase().indexOf("anhydro") >= 0) {
                System.out.flush();
                System.err.println("skipped anhydro residue...");
                System.err.flush();
                return;
            }
            if(scheme == null) {
                scheme = this.globalScheme;
            }
            Monosaccharide ms = null;
            try {
                ms = new Monosaccharide(scheme, msName);
                MonosaccharideDataBuilder.buildDerivativeData(ms, this.getTemplateContainer());
                System.out.println("ms: " + ms.toString());
                MonosaccharideSynonym alias = ms.getPrimaryAliasObject(scheme);
                String aliasName = alias.getName();
                if(msName.equalsIgnoreCase(aliasName)) {
                    System.out.print("  identical after parsing.");
                } else {
                    System.out.flush();
                    System.err.println("\n  mismatch: " + aliasName);
                    if(alias.getExternalSubstList().size() > 0) {
                        System.err.println("   Subst:");
                        for(Substitution subst : alias.getExternalSubstList()) {
                            System.err.println("     " + subst.toString());
                        }
                    }
                    System.err.flush();
                }
                ms.setFuzzy(MonosaccharideValidation.checkFuzziness(ms));
                if(ms.isFuzzy()) {
                    System.out.println(" - fuzzy");
                } else {
                    System.out.println("");
                }
                if(!ms.isFuzzy()) {
                    ms.buildRepresentations();
                    ms.addRepresentations(repList);
                    ms.addSynonyms(aliasList, true);
                    if(ms.getRingStart() > 0 && ms.getSubstitutionsByPosition(ms.getRingStart()).size() > 0) {
                        System.out.println("will not enter into db because of substitution at anomeric center: " + ms.getName());
                    } else if(ms.getRingStart() > 0 && ms.hasCoreModification(CoreModificationTemplate.DEOXY, ms.getRingStart())) {
                        System.out.println("will not enter into db because of deoxy modification at anomeric center: " + ms.getName());
                    } else if(ms.getRingtype().equals(Ringtype.OPEN) && ms.hasCoreModification(CoreModificationTemplate.ANHYDRO)) {
                        System.out.println("will not enter into db open chain residue with anhydro modification: " + ms.getName());
                    } else {
                        System.out.println("ok to insert into db: " + ms.getName());
                        if(writeToDb) {
                            ms = HibernateAccess.storeOrUpdateMonosaccharide(ms, this.getTemplateContainer());
                            System.out.println("dbId: " + ms.getDbId());
                            //System.out.println("  ms: " + ms.toString());
                        }
                    }
                }
            } catch(Exception ex) {
                System.out.flush();
                System.err.println("Exception: " + ex);
                System.err.flush();
                //ex.printStackTrace();
            }
            System.out.println();
        }
    }
    
    private static ResidueRepresentation getRepresentationFromXmlTag(org.jdom.Element repTag) {
        String sourceFileName = MsdbSeedImporter.rootpath + repTag.getValue();
        ResidueRepresentation outRep = null;
        if(sourceFileName != null && sourceFileName.length() > MsdbSeedImporter.rootpath.length()) {
            ResidueRepresentationType type = ResidueRepresentationType.forName(repTag.getAttributeValue("type"));
            ResidueRepresentationFormat format = ResidueRepresentationFormat.forName(repTag.getAttributeValue("format"));
            if(type != null && format != null) {
                int width = NumberUtils.parseIntStr(repTag.getAttributeValue("width"), 0);
                int height = NumberUtils.parseIntStr(repTag.getAttributeValue("height"), 0);
                outRep = new ResidueRepresentation(type, format);
                outRep.setSize(width, height);
                if(format.isBinary()) {
                    outRep.setData(FileUtils.readBinaryFile(sourceFileName));
                } else {
                    outRep.setData(FileUtils.readTextFile(sourceFileName));
                }
                if(outRep.getData() == null) {
                    return null;
                }
            }
        }
        return outRep;
    }
    
    private static MonosaccharideSynonym getAliasFromXmlTag(org.jdom.Element aliasTag) {
        MonosaccharideSynonym alias = null;
        GlycanNamescheme scheme = null;
        try {
            scheme = GlycanNamescheme.forName(aliasTag.getAttributeValue("scheme"));
        } catch(Exception ex) {
            System.err.println("Cannot assign namescheme '" + aliasTag.getAttributeValue("scheme") + "':");
            System.err.println(ex);
        }
        boolean isPrimary = Utils.parseTrueFalseString(aliasTag.getAttributeValue("primary"), false);
        String aliasName = aliasTag.getValue();
        if(scheme != null && aliasName != null && aliasName.length() > 0) {
            alias = new MonosaccharideSynonym(scheme, aliasName, isPrimary);
        }
        return alias;
    }
    
    //*****************************************************************************
    //*** methods to read alias file: *********************************************
    //*****************************************************************************
    
    public void parseSynonyms(URL synonymFileUrl, int offset, int quantity) throws ResourcesDbException {
        SAXBuilder parser = new SAXBuilder();
        int count = 0;
        try {
            Document doc = parser.build(synonymFileUrl);
            org.jdom.Element root = doc.getRootElement();
            if(root.getAttributeValue("ms_scheme") != null) {
                GlycanNamescheme scheme = GlycanNamescheme.forName(root.getAttributeValue("ms_scheme"));
                if(scheme != null) {
                    this.globalScheme = scheme;
                } else {
                    System.err.println("cannot assign namescheme '" + root.getAttributeValue("ms_scheme") + "'");
                    this.globalScheme = GlycanNamescheme.AUTO;
                }
            }
            List<?> templateList = root.getChildren();
            Iterator<?> templatesIter = templateList.iterator();
            while(templatesIter.hasNext()) {
                org.jdom.Element xmlTag = (org.jdom.Element) templatesIter.next();
                count ++;
                if(count > offset && count <= offset + quantity) {
                    parseAliasTagFromSynonymsFile(xmlTag);
                }
            }
        } catch (JDOMException je) {
            throw new ResourcesDbException("JDOMException: " + je.getMessage());
        } catch (IOException ie) {
            throw new ResourcesDbException("IOException: " + ie.getMessage());
        }  
        
    }
    
    private void parseAliasTagFromSynonymsFile(org.jdom.Element aliasTag) throws ResourcesDbException {
        String msName = aliasTag.getAttributeValue("ms");
        if(msName == null || msName.length() == 0) {
            return;
        }
        String aliasName = aliasTag.getAttributeValue("name");
        if(aliasName == null || aliasName.length() == 0) {
            return;
        }
        if(Utils.parseTrueFalseString(aliasTag.getAttributeValue("skip"), false)) {
            System.out.println("skipped ms " + msName);
            return;
        }
        
        GlycanNamescheme aliasScheme = GlycanNamescheme.forName(aliasTag.getAttributeValue("scheme"));
        if(aliasScheme == null) {
            throw new ResourcesDbException("Cannot assign alias namescheme (" + aliasTag.getAttributeValue("scheme") + ")");
        }
        GlycanNamescheme msNamescheme = GlycanNamescheme.forName(aliasTag.getAttributeValue("ms_scheme"));
        if(msNamescheme == null) {
            msNamescheme = this.globalScheme;
        }
        boolean isSecondaryAlias = Utils.parseTrueFalseString(aliasTag.getAttributeValue("secondary"), false);
        MonosaccharideSynonym msAlias = new MonosaccharideSynonym(aliasScheme, aliasName, !isSecondaryAlias);
        Monosaccharide ms = null;
        try {
            System.out.println("process ms " + msName);
            ms = new Monosaccharide(msNamescheme, msName);
            ms.buildName();
            System.out.println("ms: " + ms.toString());
            Monosaccharide dbMs = HibernateAccess.getMonosaccharideFromDB(ms.getName());
            if(dbMs != null) {
                //*** monosaccharide is already present in database, add synonym if appropriate: ***
                if(dbMs.addSynonym(msAlias)) {
                    HibernateAccess.storeOrUpdateMonosaccharideSynonym(msAlias);
                    HibernateAccess.updateMonosaccharide(dbMs);
                    System.out.println("added alias " + msAlias + " to ms id " + dbMs.getDbId());
                } else {
                    System.out.println("alias " + msAlias + " was not added to ms id " + dbMs.getDbId());
                }
            } else {
                //*** monosaccharide is not yet present in database, enter it if appropriate: ***
                MonosaccharideDataBuilder.buildDerivativeData(ms, this.getTemplateContainer());
                ms.setFuzzy(MonosaccharideValidation.checkFuzziness(ms));
                if(ms.isFuzzy()) {
                    System.out.println("Monosaccharide is fuzzy - will not enter into db.");
                } else {
                    ms.buildRepresentations();
                    ms.addSynonym(msAlias);
                    if(ms.getRingStart() > 0 && ms.getSubstitutionsByPosition(ms.getRingStart()).size() > 0) {
                        System.out.println("will not enter into db because of substitution at anomeric center: " + ms.getName());
                    } else if(ms.getRingStart() > 0 && ms.hasCoreModification(CoreModificationTemplate.DEOXY, ms.getRingStart())) {
                        System.out.println("will not enter into db because of deoxy modification at anomeric center: " + ms.getName());
                    } else if(ms.getRingtype().equals(Ringtype.OPEN) && ms.hasCoreModification(CoreModificationTemplate.ANHYDRO)) {
                        System.out.println("will not enter into db open chain residue with anhydro modification: " + ms.getName());
                    } else {
                        System.out.println("ok to insert into db: " + ms.getName());
                        if(true) {
                            ms = HibernateAccess.storeOrUpdateMonosaccharide(ms, this.getTemplateContainer());
                            System.out.println("dbId: " + ms.getDbId());
                            System.out.println("  ms: " + ms.toString());
                        }
                    }
                }
            }
        } catch(Exception ex) {
            System.out.flush();
            System.err.println("Exception: " + ex);
            System.err.flush();
            ex.printStackTrace();
        }
        System.out.println();
    }
    
}
