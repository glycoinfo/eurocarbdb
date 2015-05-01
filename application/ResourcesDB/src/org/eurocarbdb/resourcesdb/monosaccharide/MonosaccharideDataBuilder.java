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
package org.eurocarbdb.resourcesdb.monosaccharide;

import java.util.ArrayList;
import java.util.List;

import org.eurocarbdb.resourcesdb.*;
import org.eurocarbdb.resourcesdb.atom.*;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;
import org.eurocarbdb.resourcesdb.io.MonosaccharideExchangeObject;
import org.eurocarbdb.resourcesdb.representation.*;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;

/**
* Methods to generate derived data of Monosaccharides
* such as atoms, synonyms, graphical representations, etc.
* @author Thomas Lütteke
*
*/
public class MonosaccharideDataBuilder {

    //*****************************************************************************
    //*** Notation related methods: ***********************************************
    //*****************************************************************************
    
    /**
     * Generate the alias names for a monosaccharide 
     * @param ms the Monosaccharide for which the alias names are to be built
     * @param container a TemplateContainer
     */
    public static void buildSynonyms(Monosaccharide ms, TemplateContainer container) {
        buildSynonyms(ms, container, null);
    }
    
    
    /**
     * Generate the alias names for a monosaccharide 
     * @param ms the Monosaccharide for which the alias names are to be built
     * @param container a TemplateContainer
     * @param scheme a GlycanNamesche, for which the synonyms shall be built
     */
    public static void buildSynonyms(Monosaccharide ms, TemplateContainer container, GlycanNamescheme scheme) {
        ms.initSynonyms();
        if(container == null) {
            container = ms.getTemplateContainer();
        }
        Config noTrivialNamesConf = Config.getGlobalConfig().clone();
        noTrivialNamesConf.setAllowTrivialNames(false);
        Config allowTrivialNamesConf = Config.getGlobalConfig().clone();
        allowTrivialNamesConf.setAllowTrivialNames(true);
        allowTrivialNamesConf.setForceTrivialNames(false);
        Config forceTrivialNamesConf = Config.getGlobalConfig().clone();
        forceTrivialNamesConf.setAllowTrivialNames(true);
        forceTrivialNamesConf.setForceTrivialNames(true);
        
        MonosaccharideConverter noTrivialConverter = new MonosaccharideConverter(noTrivialNamesConf, container);
        MonosaccharideConverter allowTrivialConverter = new MonosaccharideConverter(allowTrivialNamesConf, container);
        MonosaccharideConverter forceTrivialConverter = new MonosaccharideConverter(forceTrivialNamesConf, container);
        
        MonosaccharideExchangeObject noTrivialMsObj = null;
        MonosaccharideExchangeObject allowTrivialMsObj = null;
        MonosaccharideExchangeObject forceTrivialMsObj = null;
    
        //*** CarbBank name: ***
        if(scheme == null || scheme.equals(GlycanNamescheme.CARBBANK)) {
            try {
                noTrivialMsObj = noTrivialConverter.convertMonosaccharide(ms, GlycanNamescheme.CARBBANK);
                forceTrivialMsObj = forceTrivialConverter.convertMonosaccharide(ms, GlycanNamescheme.CARBBANK);
                MonosaccharideSynonym msNoTrivialAlias = new MonosaccharideSynonym(noTrivialMsObj);
                msNoTrivialAlias.setIsTrivialName(false);
                ms.addSynonym(msNoTrivialAlias);
                if(noTrivialMsObj.getMonosaccharideName().equals(forceTrivialMsObj.getMonosaccharideName())) {
                    msNoTrivialAlias.setIsPrimary(true);
                } else {
                    allowTrivialMsObj = allowTrivialConverter.convertMonosaccharide(ms, GlycanNamescheme.CARBBANK);
                    MonosaccharideSynonym msTrivialAlias = new MonosaccharideSynonym(forceTrivialMsObj);
                    msTrivialAlias.setIsTrivialName(true);
                    if(forceTrivialMsObj.getMonosaccharideName().equals(allowTrivialMsObj.getMonosaccharideName())) {
                        msTrivialAlias.setIsPrimary(true);
                        msNoTrivialAlias.setIsPrimary(false);
                    } else {
                        msTrivialAlias.setIsPrimary(false);
                        msNoTrivialAlias.setIsPrimary(true);
                    }
                    ms.addSynonym(msTrivialAlias);
                }
            } catch(ResourcesDbException ex) {
                if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                    System.err.println("Exception in building synonyms: " + ex);
                    //ex.printStackTrace();
                }
            }
        }
        
        //*** Glycosciences.de name: ***
        if(scheme == null || scheme.equals(GlycanNamescheme.GLYCOSCIENCES)) {
            try {
                noTrivialMsObj = noTrivialConverter.convertMonosaccharide(ms, GlycanNamescheme.GLYCOSCIENCES);
                allowTrivialMsObj = allowTrivialConverter.convertMonosaccharide(ms, GlycanNamescheme.GLYCOSCIENCES);
                forceTrivialMsObj = forceTrivialConverter.convertMonosaccharide(ms, GlycanNamescheme.GLYCOSCIENCES);
                MonosaccharideSynonym msNoTrivialAlias = new MonosaccharideSynonym(noTrivialMsObj);
                msNoTrivialAlias.setIsTrivialName(false);
                ms.addSynonym(msNoTrivialAlias);
                if(noTrivialMsObj.getMonosaccharideName().equals(forceTrivialMsObj.getMonosaccharideName())) {
                    msNoTrivialAlias.setIsPrimary(true);
                } else {
                    MonosaccharideSynonym msTrivialAlias = new MonosaccharideSynonym(forceTrivialMsObj);
                    msTrivialAlias.setIsTrivialName(true);
                    if(forceTrivialMsObj.getMonosaccharideName().equals(allowTrivialMsObj.getMonosaccharideName())) {
                        msTrivialAlias.setIsPrimary(true);
                        msNoTrivialAlias.setIsPrimary(false);
                    } else {
                        msTrivialAlias.setIsPrimary(false);
                        msNoTrivialAlias.setIsPrimary(true);
                    }
                    ms.addSynonym(msTrivialAlias);
                }
            } catch(ResourcesDbException ex) {
                if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                    System.err.println("Exception in building synonyms: " + ex);
                    ex.printStackTrace();
                }
            }
        }
        
        //*** GlycoCT name: ***
        if(scheme == null || scheme.equals(GlycanNamescheme.GLYCOCT)) {
            try {
                noTrivialMsObj = noTrivialConverter.convertMonosaccharide(ms, GlycanNamescheme.GLYCOCT);
                MonosaccharideSynonym alias = new MonosaccharideSynonym(noTrivialMsObj);
                alias.setIsTrivialName(false);
                alias.setIsPrimary(true);
                ms.addSynonym(alias);
            } catch(ResourcesDbException ex) {
                if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                    System.err.println("Exception in building synonyms: " + ex);
                    ex.printStackTrace();
                }
            }
        }
        
        //*** cfg name: ***
        if(scheme == null || scheme.equals(GlycanNamescheme.CFG)) {
            try {
                allowTrivialMsObj = allowTrivialConverter.convertMonosaccharide(ms, GlycanNamescheme.CFG);
                if(allowTrivialMsObj.getMonosaccharideName() != null) {
                    MonosaccharideSynonym alias = new MonosaccharideSynonym(allowTrivialMsObj);
                    alias.setIsTrivialName(false);
                    alias.setIsPrimary(true);
                    ms.addSynonym(alias);
                }
            } catch(ResourcesDbException ex) {
                //*** for many residues no cfg alias is defined, therefore nothing to do in case an exception is thrown ***
            }
        }
        
        //*** bcsdb name: ***
        if(scheme == null || scheme.equals(GlycanNamescheme.BCSDB)) {
            try {
                noTrivialMsObj = noTrivialConverter.convertMonosaccharide(ms, GlycanNamescheme.BCSDB);
                allowTrivialMsObj = allowTrivialConverter.convertMonosaccharide(ms, GlycanNamescheme.BCSDB);
                forceTrivialMsObj = forceTrivialConverter.convertMonosaccharide(ms, GlycanNamescheme.BCSDB);
                MonosaccharideSynonym msNoTrivialAlias = new MonosaccharideSynonym(noTrivialMsObj);
                msNoTrivialAlias.setIsTrivialName(false);
                if(noTrivialMsObj.getMonosaccharideName().equals(forceTrivialMsObj.getMonosaccharideName())) {
                    msNoTrivialAlias.setIsPrimary(true);
                    ms.addSynonym(msNoTrivialAlias);
                } else {
                    MonosaccharideSynonym msTrivialAlias = new MonosaccharideSynonym(forceTrivialMsObj);
                    msTrivialAlias.setIsTrivialName(true);
                    if(forceTrivialMsObj.getMonosaccharideName().equals(allowTrivialMsObj.getMonosaccharideName())) {
                        msTrivialAlias.setIsPrimary(true);
                        msNoTrivialAlias.setIsPrimary(false);
                        ms.addSynonym(msTrivialAlias);
                    } else {
                        msTrivialAlias.setIsPrimary(false);
                        msNoTrivialAlias.setIsPrimary(true);
                        ms.addSynonym(msNoTrivialAlias);
                    }
                }
            } catch(ResourcesDbException ex) {
                if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                    System.err.println("Exception in building synonyms: " + ex);
                    ex.printStackTrace();
                }
            }
        }
    }

    //*****************************************************************************
    //*** Composition related methods: ********************************************
    //*****************************************************************************
    
    public static void adjustCompositionByLinkageType(Composition compo, LinkageType linktype) throws ResourcesDbException {
        if(linktype.equals(LinkageType.DEOXY)) {
            compo.decreaseCount(Periodic.O);
            compo.decreaseCount(Periodic.H);
        } else if(linktype.equals(LinkageType.H_AT_OH)) {
            compo.decreaseCount(Periodic.H);
        } else if(linktype.equals(LinkageType.H_LOSE)) {
            compo.decreaseCount(Periodic.H);
        } else if(linktype.equals(LinkageType.R_CONFIG)) {
            compo.decreaseCount(Periodic.H);
        } else if(linktype.equals(LinkageType.S_CONFIG)) {
            compo.decreaseCount(Periodic.H);
        } else {
            throw new MonosaccharideException("Cannot build monosaccharide composition: unexpected LinkageType (" + linktype.getType() + ")");
        }
    }

    public static void buildComposition(Monosaccharide ms) throws ResourcesDbException {
        //*** initialise monosaccharide composition with basetype composition: ***
        if(ms.getBasetype().getComposition() == null) {
            try {
                ms.getBasetype().buildComposition();
            } catch(ResourcesDbException me) {
                MonosaccharideException me2 = new MonosaccharideException("Cannot build basetype composition");
                me2.initCause(me);
                throw me2;
            }
        }
        Composition compo = new Composition(ms.getBasetype().getComposition());
        for(Substitution subst : ms.getSubstitutions()) {
            //*** add substituent composition: ***
            Composition substCompos = subst.getTemplate().getComposition();
            if(substCompos == null) {
                throw new MonosaccharideException("Cannot build monosaccharide composition: No composition set for substitutent " + subst.getName());
            }
            compo.addComposition(substCompos);
            //*** add changes caused by linkage between substituent and monosaccharide: ***
            LinkageType linktype = subst.getLinkagetype1();
            if(linktype == null) {
                throw new MonosaccharideException("Cannot build monosaccharide composition: LinkageType is null for substitution " + subst.toString());
            }
            adjustCompositionByLinkageType(compo, linktype);
            if(subst.getValence() == 2) {
                linktype = subst.getLinkagetype2();
                if(linktype == null) {
                    throw new MonosaccharideException("Cannot build monosaccharide composition: LinkageType2 is null for substitution " + subst.toString());
                }
                adjustCompositionByLinkageType(compo, linktype);
            }
        }
        ms.setComposition(compo);
    }

    //*****************************************************************************
    //*** Representation related methods: *****************************************
    //*****************************************************************************
    
    public static Haworth buildHaworth(Monosaccharide ms) throws ResourcesDbException {
        Haworth h = new Haworth();
        h.drawMonosaccharide(ms);
        return h;
    }

    public static Fischer buildFischer(Monosaccharide ms) throws ResourcesDbException {
        Fischer f = new Fischer();
        f.drawMonosaccharide(ms);
        return f;
    }

    public static void addHaworthRepresentations(Monosaccharide ms) {
        try {
            Haworth h = MonosaccharideDataBuilder.buildHaworth(ms);
            //*** add svg ***
            ResidueRepresentation monoRepSvg = new ResidueRepresentation();
            monoRepSvg.setFormat(ResidueRepresentationFormat.SVG);
            monoRepSvg.setType(ResidueRepresentationType.HAWORTH);
            monoRepSvg.setWidth(h.getSvgWidth());
            monoRepSvg.setHeight(h.getSvgHeight());
            monoRepSvg.setData(h.getSvgByteArr());
            if(monoRepSvg.getData() != null) {
                ms.addRepresentation(monoRepSvg);
            }
            //*** add png ***
            ResidueRepresentation monoRepPng = new ResidueRepresentation();
            monoRepPng.setFormat(ResidueRepresentationFormat.PNG);
            monoRepPng.setType(ResidueRepresentationType.HAWORTH);
            monoRepPng.setWidth(h.getSvgWidth());
            monoRepPng.setHeight(h.getSvgHeight());
            try {
                monoRepPng.setData(h.createPngImage());
                ms.addRepresentation(monoRepPng);
            } catch(Exception ex) {
                if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                    System.err.println("Exception in addHaworthRepresentation(): " + ex);
                    //ex.printStackTrace();
                }
            }
            //*** add jpg ***
            ResidueRepresentation monoRepJpg = new ResidueRepresentation();
            monoRepJpg.setFormat(ResidueRepresentationFormat.JPG);
            monoRepJpg.setType(ResidueRepresentationType.HAWORTH);
            monoRepJpg.setWidth(h.getSvgWidth());
            monoRepJpg.setHeight(h.getSvgHeight());
            try {
                monoRepJpg.setData(h.createJpgImage());
                ms.addRepresentation(monoRepJpg);
            } catch(Exception ex) {
                if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                    System.err.println("Exception in addHaworthRepresentation(): " + ex);
                    //ex.printStackTrace();
                }
            }
        } catch(ResourcesDbException me) {
            if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                System.err.println("ResourcesDbException in buildHaworth(): " + me.getMessage());
                //me.printStackTrace();
            }
        }
    }

    public static void updateHaworthRepresentations(Monosaccharide ms) {
        Haworth h;
        try {
            h = MonosaccharideDataBuilder.buildHaworth(ms);
        } catch(ResourcesDbException me) {
            if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                System.err.println("ResourcesDbException in buildHaworth(): " + me.getMessage());
                //me.printStackTrace();
            }
            return;
        }
        ResidueRepresentation monoRep;
        //*** add or update png: ***
        monoRep = ms.getRepresentation(ResidueRepresentationType.HAWORTH, ResidueRepresentationFormat.PNG);
        if(monoRep == null) {
            monoRep = new ResidueRepresentation(ResidueRepresentationType.HAWORTH, ResidueRepresentationFormat.PNG);
            ms.addRepresentation(monoRep);
        }
        monoRep.setSize(h.getSvgWidth(), h.getSvgHeight());
        try {
            monoRep.setData(h.createPngImage());
        } catch(Exception ex) {
            if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                System.err.println("Exception in updateHaworthRepresentation(): " + ex);
                //ex.printStackTrace();
            }
        }
        //*** add or update jpg: ***
        monoRep = ms.getRepresentation(ResidueRepresentationType.HAWORTH, ResidueRepresentationFormat.JPG);
        if(monoRep == null) {
            monoRep = new ResidueRepresentation(ResidueRepresentationType.HAWORTH, ResidueRepresentationFormat.JPG);
            ms.addRepresentation(monoRep);
        }
        monoRep.setSize(h.getSvgWidth(), h.getSvgHeight());
        try {
            monoRep.setData(h.createJpgImage());
        } catch(Exception ex) {
            if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                System.err.println("Exception in updateHaworthRepresentation(): " + ex);
                //ex.printStackTrace();
            }
        }
        //*** add or update svg: ***
        monoRep = ms.getRepresentation(ResidueRepresentationType.HAWORTH, ResidueRepresentationFormat.SVG);
        if(monoRep == null) {
            monoRep = new ResidueRepresentation(ResidueRepresentationType.HAWORTH, ResidueRepresentationFormat.SVG);
            ms.addRepresentation(monoRep);
        }
        monoRep.setSize(h.getSvgWidth(), h.getSvgHeight());
        monoRep.setData(h.getSvgByteArr());
    }
    
    public static void addFischerRepresentations(Monosaccharide ms) {
        Fischer f;
        try {
            f = MonosaccharideDataBuilder.buildFischer(ms);
        } catch(ResourcesDbException me) {
            if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                System.err.println("ResourcesDbException in buildFischer(): " + me.getMessage());
                //me.printStackTrace();
            }
            return;
        }
        //*** add svg ***
        ResidueRepresentation monoRepSvg = new ResidueRepresentation();
        monoRepSvg.setFormat(ResidueRepresentationFormat.SVG);
        monoRepSvg.setType(ResidueRepresentationType.FISCHER);
        monoRepSvg.setWidth(f.getSvgWidth());
        monoRepSvg.setHeight(f.getSvgHeight());
        monoRepSvg.setData(f.getSvgByteArr());
        if(monoRepSvg.getData() != null) {
            ms.addRepresentation(monoRepSvg);
        }
        //*** add png ***
        ResidueRepresentation monoRepPng = new ResidueRepresentation();
        monoRepPng.setFormat(ResidueRepresentationFormat.PNG);
        monoRepPng.setType(ResidueRepresentationType.FISCHER);
        monoRepPng.setWidth(f.getSvgWidth());
        monoRepPng.setHeight(f.getSvgHeight());
        try {
            monoRepPng.setData(f.createPngImage());
            ms.addRepresentation(monoRepPng);
        } catch(Exception ex) {
            if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                System.err.println("Exception in addFischerRepresentation(): " + ex);
                //ex.printStackTrace();
            }
        }
        //*** add jpg ***
        ResidueRepresentation monoRepJpg = new ResidueRepresentation();
        monoRepJpg.setFormat(ResidueRepresentationFormat.JPG);
        monoRepJpg.setType(ResidueRepresentationType.FISCHER);
        monoRepJpg.setWidth(f.getSvgWidth());
        monoRepJpg.setHeight(f.getSvgHeight());
        try {
            monoRepJpg.setData(f.createJpgImage());
            ms.addRepresentation(monoRepJpg);
        } catch(Exception ex) {
            if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                System.err.println("Exception in addFischerRepresentation(): " + ex);
                //ex.printStackTrace();
            }
        }
    }

    public static void updateFischerRepresentations(Monosaccharide ms) {
        Fischer f;
        try {
            f = MonosaccharideDataBuilder.buildFischer(ms);
        } catch(ResourcesDbException me) {
            if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                System.err.println("ResourcesDbException in buildFischer(): " + me.getMessage());
                me.printStackTrace();
            }
            return;
        }
        ResidueRepresentation monoRep;
        //*** add or update png: ***
        monoRep = ms.getRepresentation(ResidueRepresentationType.FISCHER, ResidueRepresentationFormat.PNG);
        if(monoRep == null) {
            monoRep = new ResidueRepresentation(ResidueRepresentationType.FISCHER, ResidueRepresentationFormat.PNG);
            ms.addRepresentation(monoRep);
        }
        monoRep.setSize(f.getSvgWidth(), f.getSvgHeight());
        try {
            monoRep.setData(f.createPngImage());
        } catch(Exception ex) {
            if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                System.err.println("Exception in updateFischerRepresentation(): " + ex);
                //ex.printStackTrace();
            }
        }
        //*** add or update jpg: ***
        monoRep = ms.getRepresentation(ResidueRepresentationType.FISCHER, ResidueRepresentationFormat.JPG);
        if(monoRep == null) {
            monoRep = new ResidueRepresentation(ResidueRepresentationType.FISCHER, ResidueRepresentationFormat.JPG);
            ms.addRepresentation(monoRep);
        }
        monoRep.setSize(f.getSvgWidth(), f.getSvgHeight());
        try {
            monoRep.setData(f.createJpgImage());
        } catch(Exception ex) {
            if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                System.err.println("Exception in updateFischerRepresentation(): " + ex);
                //ex.printStackTrace();
            }
        }
        //*** add or update svg: ***
        monoRep = ms.getRepresentation(ResidueRepresentationType.FISCHER, ResidueRepresentationFormat.SVG);
        if(monoRep == null) {
            monoRep = new ResidueRepresentation(ResidueRepresentationType.FISCHER, ResidueRepresentationFormat.SVG);
            ms.addRepresentation(monoRep);
        }
        monoRep.setSize(f.getSvgWidth(), f.getSvgHeight());
        monoRep.setData(f.getSvgByteArr());
    }
    
    //*****************************************************************************
    //*** Atom related methods: ***************************************************
    //*****************************************************************************
    
    /**
     * Build the atoms of this monosaccharide using the global config
     * @throws MonosaccharideException
     */
    public static void buildAtoms(Monosaccharide ms) throws ResourcesDbException {
        buildAtoms(ms, Config.getGlobalConfig());
    }

    /**
     * Build the atoms of this monosaccharide using a given config
     * @param conf the config to be used
     * @throws MonosaccharideException
     */
    public static void buildAtoms(Monosaccharide ms, Config conf) throws ResourcesDbException {
        //*** make sure basetype atoms are set: ***
        if(ms.getBasetype().getAtoms() == null || ms.getBasetype().getAtoms().size() == 0) {
            ms.getBasetype().buildAtoms(conf);
        }
        //*** initialize monosaccharide atoms with basetype atoms (subst. may remove some of the atoms / connections, so a clone is needed here): ***
        ms.setAtoms(ms.getBasetype().getAtomListClone());
        //*** add substitution atoms: ***
        for(Substitution subst : ms.getSubstitutions()) {
            if(subst.hasUncertainLinkagePosition()) {
                throw new MonosaccharideException("Cannot build atoms for monosaccharide with uncertain substitution position.");
            }
            if(subst.hasUncertainSubstituentPosition()) {
                throw new MonosaccharideException("Cannot build atoms for monosaccharide with uncertain substituent position.");
            }
            
            List<Atom> substAtomsClone = subst.getTemplate().getAtomListClone();
            
            if(subst.getReplacedAtom1() != null) {
                Atom.removeAtomFromList(substAtomsClone, subst.getReplacedAtom1(), true);
            }
            if(subst.getReplacedAtom2() != null) {
                Atom.removeAtomFromList(substAtomsClone, subst.getReplacedAtom2(), true);
            }
            
            Atom substLinkAtom1 = null;
            Atom substLinkAtom2 = null;
            for(Atom a : substAtomsClone) {
                if(a.equals(subst.getLinkingAtom1())) {
                    substLinkAtom1 = a;
                }
                if(subst.hasPosition2() && a.equals(subst.getLinkingAtom2())) {
                    substLinkAtom2 = a;
                }
            }
            if(substLinkAtom1 == null) {
                throw new ResourcesDbException("Cannot find substituent atom to link to the basetype");
            }
            if(subst.hasPosition2() && substLinkAtom2 == null) {
                throw new ResourcesDbException("Cannot find substituent atom2 to link to the basetype");
            }
            
            if(subst.getIntValuePosition1() == ms.getRingEnd()) {
                //*** substituent replaces ring oxygen, remove hydrogen from linking atom: ***
                int hCount = substLinkAtom1.countConnectedAtoms("H");
                if(hCount > 0) {
                    Atom h = substLinkAtom1.getConnectedAtom("H", hCount);
                    Atom.removeAtomFromList(substAtomsClone, h, false);
                }
                Atom anomericAtom = ms.getAtomByName(AtomTemplate.BB_C.formatAtomName(ms.getRingStart()));
                if(anomericAtom == null) {
                    throw new ResourcesDbException("Cannot find anomeric carbon in basetype atoms.");
                }
                substLinkAtom1.addConnection(anomericAtom, 1);
            }
            
            //*** add position to substituent atom names: ***
            int pos1 = subst.getIntValuePosition1();
            for(Atom a : substAtomsClone) {
                a.setName(a.getName() + "_" + pos1);
            }
            ms.getAtoms().addAll(substAtomsClone);
            
            linkSubstituentToMs(ms, pos1, subst.getLinkagetype1(), substLinkAtom1, subst.getBondOrder1());
            
            if(subst.hasPosition2()) {
                //*** add second linkage: ***
                linkSubstituentToMs(ms, subst.getIntValuePosition2(), subst.getLinkagetype2(), substLinkAtom2, subst.getBondOrder2());
            }
        }
    }

    /**
     * Link a substituent to a monosaccharide.
     * This method just removes the atoms on the monosaccharide side and adds the connections between the respective ms. and the subst. atoms.
     * The substituent atoms have to be prepared before (i.e. their names adjusted and, if applicable, the subst. atoms that have to be deleted removed).
     * @param pos the linkage position at the monosaccharide
     * @param linktype the linkage type at the monosaccharide
     * @param substLinkatom the substituent atom that is linked to the monosaccharide
     * @param bo the bond order of the linkage
     * @throws ResourcesDbException
     */
    public static void linkSubstituentToMs(Monosaccharide ms, int pos, LinkageType linktype, Atom substLinkatom, double bo) throws ResourcesDbException {
        Atom msLinkatom = null;
        Atom msRemoveAtom = null;
        if(linktype.equals(LinkageType.H_AT_OH)) {
            msLinkatom = ms.getAtomByName(AtomTemplate.BB_O.formatAtomName(pos));
            if(msLinkatom == null) {
                throw new MonosaccharideException("Cannot link substituent to ms: ms atom to link the substituent not found.");
            }
            msRemoveAtom = ms.getAtomByName(AtomTemplate.BB_HO.formatAtomName(pos));
            if(msRemoveAtom == null) {
                throw new MonosaccharideException("Cannot link substituent to ms: ms atom to be removed not found.");
            }
            ms.removeAtom(msRemoveAtom, true);
        } else if(linktype.equals(LinkageType.DEOXY)) {
            msLinkatom = ms.getAtomByName(AtomTemplate.BB_C.formatAtomName(pos));
            if(msLinkatom == null) {
                throw new MonosaccharideException("Cannot link substituent to ms: ms atom to link the substituent not found.");
            }
            msRemoveAtom = ms.getAtomByName(AtomTemplate.BB_O.formatAtomName(pos));
            if(msRemoveAtom == null) {
                throw new MonosaccharideException("Cannot link substituent to ms: ms atom to be removed not found.");
            }
            ms.removeAtom(msRemoveAtom, true);
        } else if(linktype.equals(LinkageType.H_LOSE)) {
            msLinkatom = ms.getAtomByName(AtomTemplate.BB_C.formatAtomName(pos));
            if(msLinkatom == null) {
                throw new MonosaccharideException("Cannot link substituent to ms: ms atom to link the substituent not found.");
            }
            msRemoveAtom = ms.getAtomByName(AtomTemplate.BB_H.formatAtomName(pos));
            if(msRemoveAtom == null) {
                throw new MonosaccharideException("Cannot link substituent to ms: ms atom to be removed not found.");
            }
            ms.removeAtom(msRemoveAtom, true);
        } else if(linktype.equals(LinkageType.R_CONFIG)) {
            msLinkatom = ms.getAtomByName(AtomTemplate.BB_C.formatAtomName(pos));
            if(msLinkatom == null) {
                throw new MonosaccharideException("Cannot link substituent to ms: ms atom to link the substituent not found.");
            }
            msRemoveAtom = ms.getAtomByName(AtomTemplate.BB_HX.formatAtomName(pos, 0));
            if(msRemoveAtom == null) {
                throw new MonosaccharideException("Cannot link substituent to ms: ms atom to be removed not found.");
            }
            ms.removeAtom(msRemoveAtom, true);
            ms.getAtomByName(AtomTemplate.BB_HX.formatAtomName(pos, 1)).setName(AtomTemplate.BB_H.formatAtomName(pos));
        } else if(linktype.equals(LinkageType.S_CONFIG)) {
            msLinkatom = ms.getAtomByName(AtomTemplate.BB_C.formatAtomName(pos));
            if(msLinkatom == null) {
                throw new MonosaccharideException("Cannot link substituent to ms: ms atom to link the substituent not found.");
            }
            msRemoveAtom = ms.getAtomByName(AtomTemplate.BB_HX.formatAtomName(pos, 0));
            if(msRemoveAtom == null) {
                throw new MonosaccharideException("Cannot link substituent to ms: ms atom to be removed not found.");
            }
            ms.removeAtom(msRemoveAtom, true);
            ms.getAtomByName(AtomTemplate.BB_HX.formatAtomName(pos, 1)).setName(AtomTemplate.BB_H.formatAtomName(pos));
        }
        msLinkatom.addConnection(substLinkatom, bo);
        substLinkatom.addConnection(msLinkatom, bo);
        
    }

    public static ArrayList<MonosaccharideLinkingPosition> buildPossibleLinkagePositions(Monosaccharide ms) {
        
        ArrayList<MonosaccharideLinkingPosition> linkPosList = new ArrayList<MonosaccharideLinkingPosition>();
        for(int pos = 1; pos <= ms.getSize(); pos++) {
            if(pos == ms.getRingEnd()) {
                continue;
            }
            if(pos == ms.getRingStart() && ms.getRingEnd() == Basetype.OPEN_CHAIN) {
                continue;
            }
            boolean isLinkable = true;
            
            //*** check core modifications: ***
            for(CoreModification mod : ms.getCoreModificationsByPosition(pos)) {
                if(CoreModificationTemplate.DEOXY.equals(mod.getTemplate())) {
                    isLinkable = false;
                    break;
                }
                if(CoreModificationTemplate.ACID.equals(mod.getTemplate())) {
                    //TODO: check, if acid really is not linkable
                    isLinkable = false;
                    break;
                }
                if(CoreModificationTemplate.ANHYDRO.equals(mod.getTemplate())) {
                    isLinkable = false;
                    break;
                }
                if(CoreModificationTemplate.YN.equals(mod.getTemplate())) {
                    isLinkable = false;
                    break;
                }
                if(CoreModificationTemplate.LACTONE.equals(mod.getTemplate())) {
                    isLinkable = false;
                    break;
                }
                if(CoreModificationTemplate.KETO.equals(mod.getTemplate())) {
                    if(!(pos == ms.getRingStart())) {
                        //*** keto modification which is not the anomeric center ***
                        isLinkable = false;
                        break;
                    }
                }
            }
            
            if(!isLinkable) {
                continue;
            }
            
            MonosaccharideLinkingPosition linkPos = new MonosaccharideLinkingPosition(pos);
            if(pos == ms.getRingStart() && ms.getRingEnd() > 0) {
                linkPos.setIsAnomeric(true);
            }
            
            //*** check substitutions: ***
            for(Substitution subst : ms.getSubstitutionsByPosition(pos)) {
                if(LinkageType.DEOXY.equals(subst.getLinkagetypeByPosition(pos)) || LinkageType.H_AT_OH.equals(subst.getLinkagetypeByPosition(pos))) {
                    if(subst.getTemplate().isLinkable()) {
                        linkPos.setLinkingSubstitution(subst);
                    } else {
                        isLinkable = false;
                        break;
                    }
                }
            }
            if(isLinkable) {
                linkPosList.add(linkPos);
            }
        }
        return linkPosList;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public static void buildDerivativeData(Monosaccharide ms, TemplateContainer container) throws ResourcesDbException {
        ms.getBasetype().buildName();
        ms.getBasetype().buildAtoms();
        ms.getBasetype().buildComposition();
        if(ms.getBasetype().getConfiguration() == null) {
            
        }
        ms.buildName();
        MonosaccharideDataBuilder.buildSynonyms(ms, container);
        try {
            MonosaccharideDataBuilder.buildAtoms(ms);
        } catch(ResourcesDbException ex) {
            ms.setAtoms(new ArrayList<Atom>());
        }
        try {
            buildComposition(ms);
        } catch(ResourcesDbException ex) {
            ms.setComposition(new Composition());
        }
        ms.calculateMassesFromComposition();
        /*if(!this.checkFuzziness()) {
            this.addHaworthRepresentation();
        }*/
    }

}
