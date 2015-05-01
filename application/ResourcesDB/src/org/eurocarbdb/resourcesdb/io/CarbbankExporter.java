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
import org.eurocarbdb.resourcesdb.monosaccharide.CoreModification;
import org.eurocarbdb.resourcesdb.monosaccharide.CoreModificationTemplate;
import org.eurocarbdb.resourcesdb.monosaccharide.Monosaccharide;
import org.eurocarbdb.resourcesdb.monosaccharide.MonosaccharideException;
import org.eurocarbdb.resourcesdb.monosaccharide.MonosaccharideValidation;
import org.eurocarbdb.resourcesdb.monosaccharide.Ringtype;
import org.eurocarbdb.resourcesdb.monosaccharide.Stereocode;
import org.eurocarbdb.resourcesdb.monosaccharide.Substitution;
import org.eurocarbdb.resourcesdb.template.BasetypeTemplate;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;
import org.eurocarbdb.resourcesdb.template.TrivialnameTemplate;
import org.eurocarbdb.resourcesdb.util.StringUtils;
import org.eurocarbdb.resourcesdb.util.Utils;

/**
* This class stores the exporter, i.e. the name builder, for carbohydrate notations that are based on the CarbBank nomenclature, like CarbBank itself, Linucs/GlycoSciences, Sweet, etc.
* @author Thomas Lütteke
*
*/
public class CarbbankExporter extends StandardExporter implements MonosaccharideExporter {
    
    private TrivialnameTemplate trivialTmpl = null;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public CarbbankExporter(GlycanNamescheme namescheme) throws ResourcesDbException {
        super(namescheme);
    }
    
    public CarbbankExporter(GlycanNamescheme namescheme, Config conf) throws ResourcesDbException {
        super(namescheme, conf);
    }
    
    public CarbbankExporter(GlycanNamescheme namescheme, Config conf, TemplateContainer container) throws ResourcesDbException {
        super(namescheme, conf, container);
    }
    
    public CarbbankExporter(Config conf, TemplateContainer container) throws ResourcesDbException {
        super(GlycanNamescheme.CARBBANK, conf, container);
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    public TrivialnameTemplate getTrivialTmpl() {
        return this.trivialTmpl;
    }

    public void setTrivialTmpl(TrivialnameTemplate trivialTmpl) {
        this.trivialTmpl = trivialTmpl;
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
        if(getNamescheme() == null) {
            throw new MonosaccharideException("No namescheme set in CarbbankExporter.java");
        }
        String msNameStr = "";
        if(ms.getRingtype().equals(Ringtype.OPEN) && !MonosaccharideValidation.impliesOpenChain(ms)) {
            if(ms.hasCoreModification(CoreModificationTemplate.KETO) && !ms.hasCoreModification(CoreModificationTemplate.KETO, 1)) {
                msNameStr += "keto-";
            } else {
                msNameStr += "aldehydo-";
            }
        }
        
        //*** get basetypes and - if applicable - trivial name: ***
        ArrayList<String> basetypeList;
        TrivialnameTemplate trivTmpl = null;
        if(this.getConfig().isAllowTrivialNames()) {
            //trivTmpl = TrivialnameTemplateContainer.checkMsForTrivialname(this.namescheme, ms, this.getTemplateContainer());
            trivTmpl = this.getTemplateContainer().getTrivialnameTemplateContainer().checkMsForTrivialname(this.getNamescheme(), ms);
            if(trivTmpl != null && trivTmpl.getPrimaryName(this.getNamescheme()) == null && !forceTrivial) {
                trivTmpl = null;
            }
        }
        this.setTrivialTmpl(trivTmpl);
        if(trivTmpl != null) {
            basetypeList = new ArrayList<String>();
            basetypeList.add(Stereocode.getTrivialnameConfigurationFromStereoString(ms.getStereoStr()).getSymbol() + "-" + trivTmpl.getPrimaryName(this.getNamescheme()));
        } else {
            basetypeList = Stereocode.getBasetypelistFromStereocode(ms, this.getTemplateContainer().getBasetypeTemplateContainer());
        }
        int basetypeIndex = 0;
        
        //*** write additional basetypes for residues consisting of more than 6 backbone carbon atoms: ***
        while(basetypeIndex < basetypeList.size() - 1) {
            msNameStr += unknownConfigBasetypeXtoQMark(basetypeList.get(basetypeIndex)) + "-" + msNameStr;
            basetypeIndex++;
        }
        
        //*** write anomer: ***
        String anomerStr = ms.getAnomer().formatCarbbankSymbol();
        if(this.getNamescheme().equals(GlycanNamescheme.CARBBANK) && anomerStr.equals("?-")) {
            anomerStr = "";
        }
        msNameStr += anomerStr;
        
        //*** write configuration, basetype and core modifications: ***
        if(trivTmpl != null) { //*** trivial name found, so core modifications do not have to be considered here (are implied in trivial name) ***
            if(this.getNamescheme().equals(GlycanNamescheme.CARBBANK) && basetypeList.get(basetypeIndex).substring(0, 2).equalsIgnoreCase("X-")) {
                msNameStr += StringUtils.camelCase(deleteUnknownConfigBasetypeX(basetypeList.get(basetypeIndex)), 0, 0);
            } else {
                msNameStr += StringUtils.camelCase(unknownConfigBasetypeXtoQMark(basetypeList.get(basetypeIndex)), 0, 2);
            }
        } else {
            String tmpBasetypeStr;
            if(basetypeList.size() > 0) { //*** residue is not just superclass ***
                if(this.getNamescheme().equals(GlycanNamescheme.CARBBANK)) {
                    tmpBasetypeStr = deleteUnknownConfigBasetypeX(basetypeList.get(basetypeIndex));
                } else {
                    tmpBasetypeStr = unknownConfigBasetypeXtoQMark(basetypeList.get(basetypeIndex));
                }
            } else {
                tmpBasetypeStr = this.getTemplateContainer().getBasetypeTemplateContainer().getSuperclassTemplateBySize(ms.getSize()).getBaseName();
            }
            String configStr = "";
            String basetypeStr = tmpBasetypeStr;
            if(tmpBasetypeStr.length() == 5) {
                configStr = tmpBasetypeStr.substring(0, 2);
                basetypeStr = tmpBasetypeStr.substring(2);
            }
            msNameStr += configStr;
            boolean wroteDeoxy = false;
            for(CoreModification coreMod : ms.getCoreModifications()) {
                if(coreMod.getTemplate().equals(CoreModificationTemplate.ACID)) {
                    continue; //*** acids are handled either behind the ring size (uronic) or at the end of the namestring (-aric or -onic) ***
                }
                if(coreMod.getTemplate().equals(CoreModificationTemplate.KETO)) {
                    continue; //*** keto groups are handled in front of the ring size ***
                }
                if(coreMod.getTemplate().equals(CoreModificationTemplate.SP2)) {
                    continue; //*** sp2 hybrids are not explicitly mentioned (implied in substitution) ***
                }
                if(coreMod.getTemplate().equals(CoreModificationTemplate.ALDITOL)) {
                    continue; //*** alditols are marked by adding "-ol" at the end of the residue ***
                }
                //*** format position(s) string: ***
                String posStr = Utils.formatPositionsString(coreMod.getPosition1(), "/", "?");
                if(coreMod.getTemplate().equals(CoreModificationTemplate.DEOXY)) {
                    //*** join deoxy modifications: ***
                    if(wroteDeoxy) {
                        continue;
                    }
                    for(CoreModification deoxyMod : ms.getCoreModifications(CoreModificationTemplate.DEOXY.getName())) {
                        if(deoxyMod.equals(coreMod)) {
                            continue;
                        }
                        posStr += "," + Utils.formatPositionsString(deoxyMod.getPosition1(), "/", "?");
                    }
                    wroteDeoxy = true;
                }
                if(coreMod.getValence() == 2 && !(coreMod.getTemplate().equals(CoreModificationTemplate.EN) || coreMod.getTemplate().equals(CoreModificationTemplate.ENX) || coreMod.getTemplate().equals(CoreModificationTemplate.YN))) {
                    posStr += "," + Utils.formatPositionsString(coreMod.getPosition2(), "/", "?");
                }
                //*** add core modification to ms name string: ***
                msNameStr += posStr + "-" + coreMod.getName() + "-";
            }
            if(this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeTemplateByName(basetypeStr).getSize() == ms.getSize()) {
                //*** ms is classified by its basetype, such as in b-d-glcp ***
                basetypeStr = StringUtils.camelCase(basetypeStr);
            } else {
                //*** ms is classified by a combination of basetype and superclass, such as in b-d-4-deoxy-xylhexp ***
                //*** in this case, the basetype "tro" has to be replaced by "thr": ***
                if(basetypeStr.equals("tro")) {
                    basetypeStr = "thr";
                }
            }
            msNameStr += basetypeStr;
            if(basetypeList.size() > 0) { //*** residue not is only given by superclass ***
                BasetypeTemplate msTmpl = this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeTemplateByName(basetypeList.get(basetypeIndex).substring(2));
                if(msTmpl.getSize() < ms.getSize()) {
                    msNameStr += StringUtils.camelCase(this.getTemplateContainer().getBasetypeTemplateContainer().getSuperclassTemplateBySize(ms.getSize()).getBaseName());
                }
            }
            //*** write information on carbonyl groups at positions other than 1: ***
            /*if(ms.getCarbonylPosition() > 1) {
                msNameStr += ms.getCarbonylPosition() + "ulo";
            }*/
            for(CoreModification coreMod : ms.getCoreModifications(CoreModificationTemplate.KETO.getName())) {
                msNameStr += coreMod.getPosition1().get(0) + "ulo";
            }
        }
        
        //*** write ring type: ***
        msNameStr += ms.getRingtypeSymbol();
        
        //*** mark uronic acids: ***
        if(ms.isUronic() && trivTmpl == null) {
            msNameStr += "A";
        }
        
        //*** write substitutions: ***
        int substCount = 0; //*** substCount counts the substitutions that are written to the residue name ***
        for(Substitution subst : ms.getSubstitutions()) {
            
            //*** format position(s): ***
            String posStr;
            posStr = Utils.formatPositionsString(subst.getPosition1(), "/", "?");
            if(!GlycanNamescheme.GWB.equals(this.getNamescheme())) {
                if(posStr.equals("2") && (subst.getTemplate().getName().equalsIgnoreCase(SubstituentTemplate.AMINOTEMPLATENAME) || subst.getTemplate().isExtendedAmine(this.getTemplateContainer().getSubstituentTemplateContainer())) && substCount == 0 && !subst.hasPosition2()) {
                    posStr = "";
                }
            }
            if(subst.hasPosition2()) {
                posStr += "," + Utils.formatPositionsString(subst.getPosition2(), "/", "?");
            }
            
            //*** format substituent name: ***
            String substName = subst.getResidueIncludedName(this.getNamescheme());
            
            //*** consider special cases that occur with trivial names like Neu5Ac: ***
            if(trivTmpl != null) {
                //*** check, if substitution is (fully or partly) included in trivial name: ***
                int position1 = subst.getIntValuePosition1();
                if(position1 > 0) {
                    Substitution trivSubst = trivTmpl.getSubstitutionByPosition(position1);
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
                msNameStr += posStr + substName;
                substCount++;
            }
        }
        
        //*** mark alditols / aldaric acids / aldonic acids: ***
        if(ms.isAlditol()) {
            msNameStr += "-ol";
        }
        if(trivTmpl == null) {
            if(ms.isAldaric()) {
                msNameStr += "-aric";
            }
            if(ms.isAldonic()) {
                msNameStr += "-onic";
            }
        }
        return msNameStr;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    /**
     * replace the configuration symbol "X-" with "?-" in a basetype with unknown configuration
     * @param basetypeStr the basetype string, starting with the configuration symbol, e.g. "X-gal" or "D-glc"
     * @return
     */
    private static String unknownConfigBasetypeXtoQMark(String basetypeStr) {
        if(basetypeStr.substring(0, 2).equalsIgnoreCase("X-")) {
            return("?-" + basetypeStr.substring(2));
        }
        return basetypeStr;
    }
    
    private static String deleteUnknownConfigBasetypeX(String basetypeStr) {
        if(basetypeStr.substring(0, 2).equalsIgnoreCase("X-")) {
            return basetypeStr.substring(2);
        }
        return basetypeStr;
    }
    
}
