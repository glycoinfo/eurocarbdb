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
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.template.TrivialnameTemplate;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.BasetypeTemplate;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;
import org.eurocarbdb.resourcesdb.util.NumberPrefix;
import org.eurocarbdb.resourcesdb.util.StringUtils;
import org.eurocarbdb.resourcesdb.util.Utils;

public class BcsdbExporter extends StandardExporter implements MonosaccharideExporter {

    private String configStr = null;
    private String basetypeStr = null;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public BcsdbExporter() {
        this(null, null);
    }
    
    public BcsdbExporter(Config conf) {
        this(conf, null);
    }
    
    public BcsdbExporter(Config conf, TemplateContainer container) {
        super(GlycanNamescheme.BCSDB, conf, container);
    }
    
    //*****************************************************************************
    //*** export methods: *********************************************************
    //*****************************************************************************
    
    /**
     * Generate the name string of a monosaccharide.
     * If a trivial name exists that will be used only if it is the primary alias for the monosaccharide.
     * @param ms the monosaccharide the name of which is generated
     * @return the name string
     * @throws ResourcesDbException
     */
    public String export(Monosaccharide ms) throws ResourcesDbException {
        return export(ms, this.getConfig().isForceTrivialNames());
    }
    
    /**
     * Generate the name string of a monosaccharide.
     * If forceTrivial is set to true and a trivial name exists that will be used regardless if the trivial name is the primary alias or not,
     * while forceTrivial set to false will return a trivial name only if it is the primary alias.
     * @param ms the monosaccharide the name of which is generated
     * @param forceTrivial a flag to indicate if a potential trivial name shall be used even if that is a secondary alias
     * @return the name string
     * @throws ResourcesDbException
     */
    public String export(Monosaccharide ms, boolean forceTrivial) throws ResourcesDbException {
        String outStr = "";
        if(ms.getConfiguration() == null) {
            String stereo = ms.getStereoStrWithoutAnomeric();
            ms.setConfiguration(Stereocode.getConfigurationFromStereoString(stereo));
        }
        
        //*** check for trivialname: ***
        TrivialnameTemplate trivTmpl = this.getTemplateContainer().getTrivialnameTemplateContainer().checkMsForTrivialname(GlycanNamescheme.BCSDB, ms);
        this.setUsedTrivialnameTemplate(trivTmpl);
        if(trivTmpl != null) {
            if(trivTmpl.isDefaultConfigIsCompulsory()) {
                this.configStr = "X";
            } else {
                this.configStr = String.valueOf(ms.getConfiguration().getBcsdbSymbol());
            }
            this.basetypeStr = trivTmpl.getPrimaryName(this.getNamescheme());
        } else {
            this.formatConfigAndBasetypeStrings(ms);
        }
        
        //*** write anomeric: ***
        outStr += ms.getAnomer().getBcsdbSymbol();
        
        //*** write config: ***
        outStr += this.configStr;
        
        //*** write anhydro modifications, if applicable: ***
        ArrayList<CoreModification> anhydroList = ms.getCoreModifications(CoreModificationTemplate.ANHYDRO);
        if(anhydroList != null && anhydroList.size() > 0) {
            String anhydroStr = "";
            int anhydroCount = 0;
            for(CoreModification mod : anhydroList) {
                if(this.trivialnameHasCoremod(mod)) {
                    continue;
                }
                if(anhydroCount > 0) {
                    anhydroStr += ",";
                }
                int pos1 = mod.getIntValuePosition1();
                int pos2 = mod.getIntValuePosition2();
                if(pos1 == 0) {
                    anhydroStr += "?";
                } else {
                    anhydroStr += pos1;
                }
                anhydroStr += ",";
                if(pos2 == 0) {
                    anhydroStr += "?";
                } else {
                    anhydroStr += pos2;
                }
                anhydroStr += "anh";
                anhydroCount ++;
            }
            if(anhydroCount > 0) {
                outStr += anhydroStr;
            }
        }
        
        //*** write deoxy modifications, if applicable: ***
        ArrayList<CoreModification> deoxyList = ms.getCoreModifications(CoreModificationTemplate.DEOXY);
        if(deoxyList != null && deoxyList.size() > 0) {
            String deoxyStr = "";
            int deoxyCount = 0;
            for(CoreModification mod : deoxyList) {
                if(this.trivialnameHasCoremod(mod)) {
                    continue;
                }
                if(deoxyCount > 0) {
                    deoxyStr += ",";
                }
                int pos = mod.getIntValuePosition1();
                if(pos == 0) {
                    deoxyStr += "?";
                } else {
                    deoxyStr += pos;
                }
                deoxyCount ++;
            }
            if(deoxyCount > 0) {
                deoxyStr += "d";
                outStr += deoxyStr;
            }
        }
        
        //*** write basetype: ***
        outStr += this.basetypeStr;
        
        //*** write ring type: ***
        if(ms.isAlditol()) {
            outStr += Ringtype.UNKNOWN.getBcsdbSymbol();
        } else {
            outStr += ms.getRingtype().getBcsdbSymbol();
        }
        
        //*** write en modifications, if applicable: ***
        ArrayList<CoreModification> enList = ms.getEnModifications();
        if(enList != null && enList.size() > 0) {
            String enString = "";
            int enCount = 0;
            for(CoreModification mod : enList) {
                if(this.trivialnameHasCoremod(mod)) {
                    continue;
                }
                if(enCount > 0) {
                    enString += ",";
                }
                int pos = mod.getIntValuePosition1();
                if(pos == 0) {
                    enString += "?";
                } else {
                    enString += pos;
                }
                enCount ++;
            }
            if(enCount > 0) {
                if(enCount == 1) {
                    enString += "en";
                } else {
                    enString += NumberPrefix.forSize(enCount) + "en";
                }
                outStr += enString;
            }
        }
        
        //*** write substitutions, if applicable: ***
        outStr += this.formatSubstitutionsString(ms);
        
        //*** write acid modification, if applicable: ***
        if(ms.isUronic()) {
            outStr += "A";
        }
        
        //*** write ulo / onic / aric modifications, if applicable: ***
        ArrayList<CoreModification> uloList = ms.getCoreModifications(CoreModificationTemplate.KETO);
        String uloString = "";
        int uloCount = 0;
        if(uloList != null && uloList.size() > 0) {
            for(CoreModification mod : uloList) {
                if(this.trivialnameHasCoremod(mod)) {
                    continue;
                }
                if(uloCount > 0) {
                    uloString += ",";
                }
                int pos = mod.getIntValuePosition1();
                uloString += "-";
                if(pos == 0) {
                    uloString += "?";
                } else {
                    uloString += pos;
                }
                uloCount ++;
            }
            if(uloCount > 0) {
                if(uloCount == 1 && uloString.equals("-2")) {
                    uloString = "";
                }
                uloString += "-";
                if(uloCount > 1) {
                    uloString += NumberPrefix.forSize(uloCount);
                }
                uloString += "ulo";
                outStr += uloString;
            }
        }
        if(ms.isAldonic()) {
            if(!this.trivialnameHasCoremod(ms.getCoreModification(CoreModificationTemplate.ACID, 1))) {
                if(uloCount > 0) {
                    outStr += "sonic";
                } else {
                    outStr += "-onic";
                }
            }
        } else if(ms.isAldaric()) {
            if(!this.trivialnameHasCoremod(ms.getCoreModification(CoreModificationTemplate.ACID, ms.getSize()))) {
                if(uloCount > 0) {
                    outStr += "saric";
                } else {
                    outStr += "-aric";
                }
            }
        }
        
        //*** write alditol modification, if applicable: ***
        if(ms.isAlditol()) {
            outStr += "-ol";
        }
        
        //*** return residue string: ***
        return outStr;
    }
    
    private void formatConfigAndBasetypeStrings(Monosaccharide ms) throws ResourcesDbException {
        ArrayList<String> stereoList = Stereocode.prepareStereocodeForBasetypeDetermination(ms);
        this.configStr = "";
        if(stereoList.size() == 0) { //*** residue is superclass ***
            this.configStr += StereoConfiguration.Unknown.getBcsdbSymbol();
            this.basetypeStr = StringUtils.camelCase(this.getTemplateContainer().getBasetypeTemplateContainer().getSuperclassTemplateBySize(ms.getSize()).getBaseName());
        } else if(stereoList.size() == 1) { //*** residue has up to 4 stereocenters (excl. anomeric) ***
            this.configStr += Stereocode.getConfigurationFromStereoString(stereoList.get(0)).getBcsdbSymbol();
            BasetypeTemplate btTmpl = this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeTemplateByStereoString(stereoList.get(0));
            String basename = btTmpl.getBaseName();
            if(btTmpl.getSize() == ms.getSize()) {
                if(basename.equalsIgnoreCase("tro")) {
                    basename = "thr";
                }
                this.basetypeStr = StringUtils.camelCase(basename);
            } else {
                if(basename.equalsIgnoreCase("tro")) {
                    basename = "thr";
                }
                this.basetypeStr = basename + StringUtils.camelCase(this.getTemplateContainer().getBasetypeTemplateContainer().getSuperclassTemplateBySize(ms.getSize()).getBaseName());
            }
        } else { //*** residue has more than 4 stereocenters (excl. anomeric) ***
            this.configStr += StereoConfiguration.Nonchiral.getBcsdbSymbol();
            this.basetypeStr = "";
            for(String stereoFragment : stereoList) {
                this.configStr += Stereocode.getConfigurationFromStereoString(stereoFragment).getBcsdbSymbol();
                if(stereoFragment.length() > 1) {
                    BasetypeTemplate btTmpl = this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeTemplateByStereoString(stereoFragment);
                    this.basetypeStr += btTmpl.getBaseName();
                }
            }
            this.basetypeStr += StringUtils.camelCase(this.getTemplateContainer().getBasetypeTemplateContainer().getSuperclassTemplateBySize(ms.getSize()).getBaseName());
        }
    }
    
    private String formatSubstitutionsString(Monosaccharide ms) throws ResourcesDbException {
        String substStr = "";
        int substCount = 0;
        for(Substitution subst : ms.getSubstitutions()) {
            //*** format position(s): ***
            String posStr;
            posStr = Utils.formatPositionsString(subst.getPosition1(), "/", "?");
            if(posStr.equals("2") && (subst.getTemplate().getName().equalsIgnoreCase(SubstituentTemplate.AMINOTEMPLATENAME) || subst.getTemplate().isExtendedAmine(this.getTemplateContainer().getSubstituentTemplateContainer())) && substCount == 0 && !subst.hasPosition2()) {
                posStr = "";
            }
            if(subst.hasPosition2()) {
                posStr += "," + Utils.formatPositionsString(subst.getPosition2(), "/", "?");
            }
            
            //*** format substituent name: ***
            String substName = subst.getResidueIncludedName(GlycanNamescheme.BCSDB);
            
            //*** consider special cases that occur with trivial names like Neu5Ac: ***
            if(this.getUsedTrivialnameTemplate() != null) {
                //*** check, if substitution is (fully or partly) included in trivial name: ***
                int position1 = subst.getIntValuePosition1();
                if(position1 > 0) {
                    Substitution trivSubst = this.getUsedTrivialnameTemplate().getSubstitutionByPosition(position1);
                    if(trivSubst != null) { //*** trivial name includes substitution at current position ***
                        if(trivSubst.equals(subst)) {
                            continue; //*** substitution is fully included in trivial name => must not repeated in substituents list ***
                        }
                        if(trivSubst.getName().equals(SubstituentTemplate.AMINOTEMPLATENAME)) { //*** substitution implied in trivial name is an amino group, check, what part of the substituent is to be added to the residue name: ***
                            if(subst.getResidueIncludedName(this.getNamescheme()) != null && this.getTemplateContainer().getSubstituentTemplateContainer().forName(this.getNamescheme(), subst.getResidueIncludedName(this.getNamescheme())).getName().equals(SubstituentTemplate.AMINOTEMPLATENAME)) {
                                continue; //*** most simple case: amine, which is already implied in trivial name, is residueIncludedName, and the rest of the substituent will be given as a separate residue ***
                            }
                            if(subst.getTemplate().isExtendedAmine(this.getTemplateContainer().getSubstituentTemplateContainer())) {
                                //*** trivial name includes amino group, substituent is N-linked => add name of corresponding o-linked substituent (without leading "O"), if possible: ***
                                String oLinkedEquivName = subst.getTemplate().getOLinkedEquivalent(this.getTemplateContainer().getSubstituentTemplateContainer());
                                if(oLinkedEquivName != null && oLinkedEquivName.length() > 0) {
                                    //*** o-linked equivalent is defined, get template: ***
                                    SubstituentTemplate oLinkedEquivTmpl = this.getTemplateContainer().getSubstituentTemplateContainer().forName(GlycanNamescheme.GLYCOCT, oLinkedEquivName);
                                    
                                    String oLinkTmplSepDisp = oLinkedEquivTmpl.getSeparateDisplay(this.getNamescheme(), LinkageType.H_AT_OH);
                                    String substTmplSepDisp = null; //subst.getTemplate().getSeparateDisplay(this.namescheme, LinkageType.H_AT_OH);
                                    if(StringUtils.strCmpNullEqualsEmpty(oLinkTmplSepDisp, substTmplSepDisp)) {
                                        oLinkedEquivName = oLinkedEquivTmpl.getPrimaryAlias(this.getNamescheme(), LinkageType.H_AT_OH).getResidueIncludedName();
                                        substName = oLinkedEquivTmpl.getResidueIncludedName(this.getNamescheme(), LinkageType.H_AT_OH);
                                        if(substName == null || substName.length() == 0) {
                                            substName = oLinkedEquivTmpl.getSeparateDisplay(this.getNamescheme(), LinkageType.H_AT_OH);
                                        }
                                        //*** check, if leading "O" or "O-" is present in primary alias for the substitution,
                                        //*** and if name without these letters is a secondary alias ***
                                        //*** in that case, use the secondary alias: ***
                                        if(oLinkedEquivName.startsWith("O")) {
                                            oLinkedEquivName = oLinkedEquivName.substring(1);
                                        }
                                        if(oLinkedEquivName.startsWith("-")) {
                                            oLinkedEquivName = oLinkedEquivName.substring(1);
                                        }
                                        if(oLinkedEquivTmpl.equals(this.getTemplateContainer().getSubstituentTemplateContainer().forName(this.getNamescheme(), oLinkedEquivName))) {
                                            substName = oLinkedEquivName;
                                        } else {
                                            substName = subst.getResidueIncludedName(this.getNamescheme());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            //*** add position(s) + substituent name to ms name string: ***
            if(substName != null && substName.length() > 0) {
                substStr += posStr + substName;
                substCount++;
            }
            
        }
        return substStr;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    private boolean trivialnameHasCoremod(CoreModification mod) {
        if(this.getUsedTrivialnameTemplate() != null) {
            return this.getUsedTrivialnameTemplate().hasCoreModification(mod);
        }
        return false;
    }
    
}
