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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.atom.Atom;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.template.BasetypeTemplate;
import org.eurocarbdb.resourcesdb.template.BasetypeTemplateContainer;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;

/**
* Methods to validate / check Monosaccharides
* @author Thomas Luetteke
*
*/
public class MonosaccharideValidation {
    
    public static void checkMonosaccharideConsistency(Monosaccharide ms, TemplateContainer container) throws ResourcesDbException {
        MonosaccharideValidation.checkMonosaccharideConsistency(ms, container, true);
    }
    
    public static void checkMonosaccharideConsistency(Monosaccharide ms, TemplateContainer container, Config conf) throws ResourcesDbException {
        MonosaccharideValidation.checkMonosaccharideConsistency(ms, container, !conf.isPreserveAlditolOrientation());
    }
    
    /**
     * Do a number of checks on a monosaccharide to test if it is well-formed.
     * These checks include tests, if given positions are within the allowed ranges,
     * if the anomeric state is consistent with the ring type,
     * if given substitutions and / or core modifications exclude each other or are in conflict with the ring closure positions.
     * Besides, it is tested if the stereocode is in accordance with modifications that cause a loss of stereochemistry,
     * if alditols are given in the correct orientation,
     * and if the deoxygenation pattern of "enx" core modifications is indeed unknown.
     * The enx check result in automatic adjustment of the monosaccharide if the data is inappropriate.
     * The same applies to the alditol orientation check, if the checkOrientation parameter is true.
     * @param ms the monosaccharide to be checked
     * @param container a TemplateContainer to get Templates needed for the checks
     * @param checkOrientation a flag to indicate whether alditol orientation shall be checked or not
     * @throws MonosaccharideException in case any of the checks fails.
     * @throws ResourcesDbException e.g. in case templates are not set
     */
    public static void checkMonosaccharideConsistency(Monosaccharide ms, TemplateContainer container, boolean checkOrientation) throws ResourcesDbException {
        //System.out.println("check ms: " + ms.toString());
        //*** check, if carbonyl position and ring oxygen are within allowed range: ***
        if(ms.getRingStart() > ms.getSize() || ms.getRingStart() < -1) {
            throw new MonosaccharideException("Carbonyl position out of range: " + ms.getRingStart());
        }
        if(ms.getRingEnd() > ms.getSize() || ms.getRingEnd() < -1) {
            throw new MonosaccharideException("Ring oxygen position out of range: " + ms.getRingEnd());
        }
        
        //*** check, if anomer and ringtype / core modifications are consistent: ***
        if(ms.getRingtype().equals(Ringtype.OPEN)) {
            if(ms.getAnomer().equals(Anomer.ALPHA) || ms.getAnomer().equals(Anomer.BETA)) {
                throw new MonosaccharideException("Anomer in open chain residue set.");
            }
        } else if(ms.isStereolossPositionWithIgnoreType(ms.getRingStart(), CoreModificationTemplate.KETO)) {
            if(ms.getAnomer().equals(Anomer.ALPHA) || ms.getAnomer().equals(Anomer.BETA)) {
                throw new MonosaccharideException("Anomer set in residue with non-chiral anomeric center.");
            }
            if(ms.getAnomer().equals(Anomer.UNKNOWN)) {
                ms.setAnomer(Anomer.NONE);
                ms.setStereoStr(Stereocode.setPositionInStereoString(ms.getStereoStr(), StereoConfiguration.Nonchiral, ms.getRingStart()));
            }
        } else {
            if(ms.getAnomer().equals(Anomer.NONE)) {
                throw new MonosaccharideException("Anomer 'none' in residue with chiral anomeric center.");
            }
        }
        
        //*** check modifications:                                     ***
        //*** check, if modification positions are possible,           ***
        //*** and if present combinations of modifications are allowed ***
        //TODO: take uncertain positions into account
        for(int pos = 1; pos <= ms.getSize(); pos++) {
            //*** get lists of coreModifications / substitutions at current position: ***
            ArrayList<CoreModification> coreModList = ms.getCoreModificationsByPosition(pos);
            ArrayList<Substitution> substList = ms.getSubstitutionsByPosition(pos);
            if(coreModList.size() == 0 && substList.size() == 0) { //*** no modifications present at current position ***
                continue;
            }
            
            //*** check core modifications: ***
            
            //*** rules for ring oxygen position: ***
            if(pos == ms.getRingEnd()) {
                if(substList.size() > 0) {
                    for(Substitution roSubst : substList) {
                        if(!(roSubst.getTemplate().isCanReplaceRingOxygen() || roSubst.getLinkagetypeByPosition(pos).equals(LinkageType.H_LOSE))) {
                            throw new MonosaccharideException("Substitution at ring oxygen.");
                        }
                    }
                }
                int enCount = 0;
                for(CoreModification mod : coreModList) {
                    if(mod.getTemplate().equals(CoreModificationTemplate.EN) || mod.getTemplate().equals(CoreModificationTemplate.ENX)) {
                        enCount ++;
                    } else {
                        throw new MonosaccharideException("Disallowed modification at ring oxygen: " + mod.getName());
                    }
                }
                if(enCount > 1) {
                    throw new MonosaccharideException("Multiple 'en' modifications at ring oxygen.");
                }
            }
            //*** rules for carbonyl position: ***
            if(pos == ms.getRingStart()) {
                if(substList.size() > 0 && ms.getRingEnd() == Basetype.OPEN_CHAIN && !ms.isAlditol()) {
                    throw new MonosaccharideException("Substitution at carbonyl position of an open chain monosaccharide.");
                }
                if(pos > 1) {
                    if(ms.isAlditol()) {
                        throw new MonosaccharideException("Alditols are not defined for ketoses");
                    }
                    if(ms.getCoreModification(CoreModificationTemplate.EN.getName(), pos) != null || ms.getCoreModification(CoreModificationTemplate.ENX.getName(), pos) != null) {
                        //*** anomeric center of a ketose is involved in a double bond: must be deoxy ***
                        if(ms.getCoreModification(CoreModificationTemplate.DEOXY.getName(), pos) == null) {
                            ms.addCoreModification(new CoreModification(CoreModificationTemplate.DEOXY, pos));
                        }
                    }
                }
                if(ms.getRingEnd() == Basetype.OPEN_CHAIN) { //*** open chain: only acid, en/x and sp2 core modifications allowed at carbonyl position ***
                    for(CoreModification mod : coreModList) {
                        if(mod.getTemplate().equals(CoreModificationTemplate.ACID)) {
                            continue;
                        }
                        if(mod.getTemplate().equals(CoreModificationTemplate.SP2)) {
                            continue;
                        }
                        if(mod.getTemplate().equals(CoreModificationTemplate.EN)) {
                            continue;
                        }
                        if(mod.getTemplate().equals(CoreModificationTemplate.ENX)) {
                            continue;
                        }
                        if(mod.getTemplate().equals(CoreModificationTemplate.DEOXY)) {
                            if(ms.getRingEnd() > 1 || !ms.isAlditol()) {
                                throw new MonosaccharideException("Deoxy modification at carbonyl position of open chain residue.");
                            }
                        }
                        if(mod.getTemplate().equals(CoreModificationTemplate.KETO)) {
                            continue;
                        }
                        throw new MonosaccharideException("Modification " + mod.getName() + " not allowed at carbonyl group of open chain residue.");
                    }
                }
            }
            //*** rules by modification type: ***
            int bondSum = 2;
            if(pos == 1 && ms.getRingStart() > 1) {
                bondSum--;
            }
            if(pos == ms.getSize() && !(pos == ms.getRingEnd())) {
                bondSum--;
            }
            for(CoreModification mod : coreModList) {
                if(mod.getTemplate().equals(CoreModificationTemplate.ACID)) {
                    if(pos > 1 && pos < ms.getSize()) {
                        throw new MonosaccharideException("Acid modification within backbone (pos. " + pos + ")");
                    }
                    if(pos == 1 && ms.getRingStart() < 2 && ms.getRingEnd() > 0) {
                        //TODO: check, if lactone modification is present, add it if not
                        throw new MonosaccharideException("Acid modification at ring closure position (" + pos + ").");
                    }
                    if(pos == ms.getRingEnd()) {
                        throw new MonosaccharideException("Acid modification at ring closure position (" + pos + ").");
                    }
                    if(coreModList.size() > 1) { //*** acid modification must not occurr together with other core modifications at one position ***
                        throw new MonosaccharideException("Acid modification at otherwise modified position (" + pos + ").");
                    }
                    if(pos == 1 && ms.isAlditol()) {
                        throw new MonosaccharideException("Excluding modifications: Alditol and 1-Acid");
                    }
                } else if(mod.getTemplate().equals(CoreModificationTemplate.KETO)) {
                    if(substList.size() > 0) {
                        throw new MonosaccharideException("Substitution at keto position (" + pos + ").");
                    }
                    if(CoreModificationTemplate.modListContainsModType(coreModList, CoreModificationTemplate.DEOXY) && pos != ms.getRingStart()) {
                        throw new MonosaccharideException("Deoxygenation at keto position (" + pos + ").");
                    }
                    if(CoreModificationTemplate.modListContainsModType(coreModList, CoreModificationTemplate.SP2)) {
                        if(pos != ms.getRingStart()) {
                            throw new MonosaccharideException("Sp2 hybride at keto position (" + pos + ").");
                        }
                    }
                    if(ms.isAlditol()) {
                        throw new MonosaccharideException("Core modifications ALDITOL and KETO must not occurr together in one monosaccharide.");
                    }
                } else if(mod.getTemplate().equals(CoreModificationTemplate.DEOXY)) {
                    for(Substitution subst : substList) {
                        if(!subst.getLinkagetypeByPosition(pos).equals(LinkageType.H_LOSE)) {
                            throw new MonosaccharideException("Substitution at deoxy position (" + pos + ").");
                        }
                    }
                } else if(mod.getTemplate().equals(CoreModificationTemplate.SP2)) {
                    //*** sp2 hybride requires substituent linked via double bond: ***
                    boolean hasDoublebondSubst = false;
                    for(Substitution subst : substList) {
                        if(subst.getBondOrder1() == 2) {
                            hasDoublebondSubst = true;
                            break;
                        }
                        if(subst.getBondOrder2() == 2) {
                            hasDoublebondSubst = true;
                            break;
                        }
                    }
                    if(!hasDoublebondSubst) {
                        throw new MonosaccharideException("Sp2 hybride without substitution of bond order 2");
                    }
                } else if(mod.getTemplate().equals(CoreModificationTemplate.ANHYDRO)) {
                    for(Substitution subst : substList) {
                        if(!subst.getLinkagetypeByPosition(pos).equals(LinkageType.H_LOSE)) {
                            throw new MonosaccharideException("Substitution at anhydro position (" + pos + ").");
                        }
                    }
                    if(CoreModificationTemplate.modListContainsModType(coreModList, CoreModificationTemplate.DEOXY)) {
                        throw new MonosaccharideException("Deoxygenation at anhydro position (" + pos + ").");
                    }
                    if(mod.getIntValuePosition2() == pos + 1) {
                        //*** set modification type to "epoxy" if anhydro modification occurs between neighbouring carbons ***
                        mod.setTemplate(CoreModificationTemplate.EPOXY);
                    }
                } else if(mod.getTemplate().equals(CoreModificationTemplate.EPOXY)) {
                    for(Substitution subst : substList) {
                        if(!subst.getLinkagetypeByPosition(pos).equals(LinkageType.H_LOSE)) {
                            throw new MonosaccharideException("Substitution at epoxy position (" + pos + ").");
                        }
                    }
                    if(CoreModificationTemplate.modListContainsModType(coreModList, CoreModificationTemplate.DEOXY)) {
                        throw new MonosaccharideException("Deoxygenation at epoxy position (" + pos + ").");
                    }
                    if(mod.getIntValuePosition2() != mod.getIntValuePosition1() + 1) {
                        throw new MonosaccharideException("Epoxy modification must be at neighbouring carbons (positions present: " + mod.getIntValuePosition1() + "-" + mod.getIntValuePosition2() + ")");
                    }
                }
            }
            
            //*** check substitutions: ***
            
            HashMap<LinkageType, Integer> linktypeCountMap = new HashMap<LinkageType, Integer>();
            for(Substitution subst : substList) {
                if(subst.containsPosition1(pos)) {
                    bondSum += subst.getBondOrder1();
                    if(subst.getLinkagetype1() != null) {
                        Integer linkcount = linktypeCountMap.get(subst.getLinkagetype1());
                        if(linkcount == null) {
                            linkcount = new Integer(0);
                        }
                        linkcount ++;
                        linktypeCountMap.put(subst.getLinkagetype1(), linkcount);
                    }
                }
                if(subst.containsPosition2(pos)) {
                    bondSum += subst.getBondOrder2();
                    if(subst.getLinkagetype2() != null) {
                        Integer linkcount = linktypeCountMap.get(subst.getLinkagetype2());
                        if(linkcount == null) {
                            linkcount = new Integer(0);
                        }
                        linkcount ++;
                        linktypeCountMap.put(subst.getLinkagetype2(), linkcount);
                    }
                }
            }
            if(bondSum > 4) {
                throw new MonosaccharideException("Modifications result in " + bondSum + " bonds for C" + pos);
            }
            for(LinkageType linktype : linktypeCountMap.keySet()) {
                Integer linkcount = linktypeCountMap.get(linktype);
                if(linkcount.intValue() > 1) {
                    throw new MonosaccharideException(linkcount.intValue() + " substituents of LinkageType " + linktype + " at position " + pos);
                }
                if(linktype.equals(LinkageType.H_AT_OH) && linkcount.intValue() > 0) {
                    Integer deoxyCount = linktypeCountMap.get(LinkageType.DEOXY);
                    if(deoxyCount != null && deoxyCount.intValue() > 0) {
                        throw new MonosaccharideException("Substituents of LinkageTypes DEOXY and H_AT_OH at position " + pos);
                    }
                }
                if(linktype.equals(LinkageType.H_LOSE) && linkcount.intValue() > 0) {
                    Integer rsCount = linktypeCountMap.get(LinkageType.R_CONFIG);
                    if(rsCount != null && rsCount.intValue() > 0) {
                        throw new MonosaccharideException("Substituents of LinkageTypes H_LOSE and R_CONFIG at position " + pos);
                    }
                    rsCount = linktypeCountMap.get(LinkageType.S_CONFIG);
                    if(rsCount != null && rsCount.intValue() > 0) {
                        throw new MonosaccharideException("Substituents of LinkageTypes H_LOSE and S_CONFIG at position " + pos);
                    }
                }
            }
        }
        //*** other checks: ***
        MonosaccharideValidation.checkStereocodeConsistency(ms);
        MonosaccharideValidation.checkEnxDeoxygenationStates(ms);
        MonosaccharideValidation.checkModifications(ms);
        if(checkOrientation) {
            MonosaccharideValidation.checkAlditolOrientation(ms, container.getBasetypeTemplateContainer());
        }
    }
    
    /**
     * Test, if the stereocode of a monosaccharide is in accordance with the residue size and the core modifications that cause a loss of stereochemistry
     * @param ms the monosaccharide to be checked
     * @throws MonosaccharideException in case the test fails
     */
    public static void checkStereocodeConsistency(Monosaccharide ms) throws MonosaccharideException {
        String stereo = ms.getStereoStr();
        if(stereo.length() != ms.getSize()) {
            throw new MonosaccharideException("Stereocode / residue size mismatch (" + stereo.length() + "/" + ms.getSize() + ")");
        }
        if(!ms.isSuperclass()) {
            ArrayList<Integer> stereoloss = ms.getStereolossPositions();
            for(Integer posInt : stereoloss) {
                int pos = posInt.intValue();
                if(pos == 0) {
                    continue;
                }
                if(pos == ms.getRingStart() && !ms.getAnomer().equals(Anomer.NONE) && ! ms.getAnomer().equals(Anomer.OPEN_CHAIN)) {
                    continue;
                }
                if(stereo.charAt(pos - 1) != StereoConfiguration.Nonchiral.getStereosymbol()) {
                    throw new MonosaccharideException("Stereocode error: position " + pos + " should be nonchiral (" + stereo + ")");
                }
            }
        }
    }
    
    /**
     * Test, if modifications are properly set.
     * This includes a check, if the template is set and if the given position(s) match the valence given in the template.
     * @param ms the Monosaccharide to check
     * @throws ResourcesDbException
     */
    public static void checkModifications(Monosaccharide ms) throws ResourcesDbException {
        for(CoreModification mod : ms.getCoreModifications()) {
            if(mod.getTemplate() == null) {
                throw new MonosaccharideException("missing core modification template: " + mod);
            }
            for(Integer pos : mod.getPositions()) {
                if(pos < -1 || (pos > ms.getSize() && ms.getSize() > 0)) {
                    throw new MonosaccharideException("Core modification (" + mod.getName() + ") position out of range: " + pos);
                }
            }
            if(mod.getTemplate().getValence() == 1) {
                if(mod.hasPosition2()) {
                    throw new MonosaccharideException("position 2 set in monovalent modification: " + mod);
                }
            } else if(mod.getTemplate().getValence() == 2) {
                if(!mod.hasPosition2()) {
                    throw new MonosaccharideException("missing position 2 of divalent modification: " + mod);
                }
            }
        }
        for(Substitution subst : ms.getSubstitutions()) {
            if(subst.getTemplate() == null) {
                throw new MonosaccharideException("missing substituent template: " + subst);
            }
            for(Integer pos : subst.getPositions()) {
                if(pos < -1 || (pos > ms.getSize() && ms.getSize() > 0)) {
                    throw new MonosaccharideException("Substitution (" + subst.getName() + ") position out of range: " + pos);
                }
            }
            if(subst.getTemplate().getMaxValence() == 1) {
                if(subst.hasPosition2()) {
                    throw new MonosaccharideException("position 2 set in monovalent substitution: " + subst);
                }
            } else if(subst.getTemplate().getMinValence() == 2) {
                if(!subst.hasPosition2()) {
                    throw new MonosaccharideException("missing position 2 of divalent substitution: " + subst);
                }
            }
            if(LinkageType.H_LOSE.equals(subst.getLinkagetype1()) || LinkageType.R_CONFIG.equals(subst.getLinkagetype1()) || LinkageType.S_CONFIG.equals(subst.getLinkagetype1())) {
                if(subst.getIntValuePosition1() == 1 || subst.getIntValuePosition1() == ms.getSize()) {
                    Atom linkatom = subst.getLinkingAtom1();
                    if(linkatom.getElementSymbol().equalsIgnoreCase("C")) {
                        throw new MonosaccharideException("C-linked substituent at terminal carbon.");
                    }
                }
            }
            if(subst.hasPosition2()) {
                if(LinkageType.H_LOSE.equals(subst.getLinkagetype2()) || LinkageType.R_CONFIG.equals(subst.getLinkagetype2()) || LinkageType.S_CONFIG.equals(subst.getLinkagetype2())) {
                    if(subst.getIntValuePosition2() == 1 || subst.getIntValuePosition2() == ms.getSize()) {
                        try {
                            if(subst.getLinkingAtom2().getElementSymbol().equalsIgnoreCase("C")) {
                                throw new MonosaccharideException("C-linked substituent at terminal carbon.");
                            }
                        } catch(ResourcesDbException rEx) {
                            throw new ResourcesDbException("Cannot check H_LOSE linked subst. at terminal carbon: no linking atom defined", rEx);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Check the deoxygenation patterns of "enx" core modifications present in a monosaccharide.
     * If an "enx" modification has a defined deoxygenation pattern, the modification type is changed to "en".
     * @param ms the monosaccharide to be checked
     */
    public static void checkEnxDeoxygenationStates(Monosaccharide ms) {
        for(CoreModification mod : ms.getCoreModifications(CoreModificationTemplate.ENX.getName())) {
            if(enDeoxypatternConfident(ms, mod)) {
                try {
                    mod.changeType(CoreModificationTemplate.EN);
                } catch (MonosaccharideException e) {
                    
                }
            }
        }
    }
    
    /**
     * Check, if the deoxygenation pattern of an "en"/"enx" core modification is certain
     * If this is the case, an "enx" modification can be changed to "en".
     * @param ms the monosaccharide to be checked
     * @param mod: the "en" or "enx" core modification
     * @return true, if the deoxy pattern is certain, otherwise false
     */
    public static boolean enDeoxypatternConfident(Monosaccharide ms, CoreModification mod) {
        int position1 = mod.getIntValuePosition1();
        if(position1 == 0) {
            return(false);
        }
        int position2 = position1 + 1;
        return(enDeoxyStatusPositionConfident(ms, position1) && enDeoxyStatusPositionConfident(ms, position2));
    }
    
    /**
     * Check, if the deoxygenation status of a single position is certain
     * @param ms the monosaccharide to be checked
     * @param position: the position of the "en" modification to be checked
     * @return true, if the deoxy pattern is certain, otherwise false
     */
    public static boolean enDeoxyStatusPositionConfident(Monosaccharide ms, int position) {
        //*** the deoxygenation status is confident if: ***
        //*** a) the position comprises the ring oxygen or ***
        if(position == ms.getRingEnd()) {
            return true;
        }
        //*** b) the position is explicitely marked as deoxy or ***
        if(ms.getCoreModification("deoxy", position) != null) {
            return true;
        }
        //*** c) the position carries a substituent or ***
        if(ms.getSubstitutionsByPosition(position).size() > 0) {
            return true;
        }
        //*** d) the position is the ring start and is > 1 (must be deoxy in that case) ***
        if(ms.getRingStart() == position && position > 1) {
            try {
                ms.addCoreModification(new CoreModification(CoreModificationTemplate.DEOXY, position));
            } catch(MonosaccharideException me) {
                if(Config.getGlobalConfig().isPrintErrorMsgs(1)) {
                    System.err.println("Exception: " + me);
                    me.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Check, if an alditol (or open chain aldaric acid) residue is given in the correct orientation according to IUPAC rules
     * If this is not the case, the orientation of the residue is changed, i.e. the residue is rotated by 180°.
     * The stereocode as well as modification / substitutent positions are adjusted as a result of this rotation.
     * @param ms the monosaccharide to be checked
     * @param container a BasetypeTemplateContainer to get Templates needed for the check
     * @return false, if orientation of residue was changed, otherwise true
     * @throws ResourcesDbException
     */
    public static boolean checkAlditolOrientation(Monosaccharide ms, BasetypeTemplateContainer container) throws ResourcesDbException {
        if(!hasCorrectAlditolOrientation(ms, container)) {
            if(ms.isAlditol()) {
                if(ms.isUronic()) { //*** change combination of alditol + uronic acid to ulonic acid + no alditol (adjustment of acid position is done in rotateAlditol() below, so here only adjustment of alditol necessary) ***
                    ms.setAlditol(false);
                }
            }
            ms.rotateAlditol();
            return false;
        } else {
            return true;
        }
    }
    
    public static boolean hasCorrectAlditolOrientation(Monosaccharide ms, BasetypeTemplateContainer container) throws ResourcesDbException {
        //*** (1) check, if the residue can be rotated, i.e. if there is no defined direction in the backbone ***
        //*** this is the case if the residue is an alditol or an open chain aldaric acid or an open chain residue with keto function ***
        boolean isRotatable = false;
        if(ms.isAlditol()) {
            isRotatable = true;
            if(ms.isUronic()) { //*** combination of alditol + uronic acid has to be ulonic acid + no alditol ***
                /*ms.rotateAlditol();
                ms.setAlditol(false);*/
                return false;
            }
        } else if(ms.isAldaric()) {
            if(ms.getRingEnd() == -1) {
                isRotatable = true;
            }
        } else if(ms.getRingtype().equals(Ringtype.OPEN)) {
            if(ms.hasCoreModification(CoreModificationTemplate.KETO)) {
                isRotatable = true;
            }
        }
        if(!isRotatable) {
            return true;
        }
        //*** (2) residue can be rotated, so check, if correct orientation is given: ***
        //TODO: check, if open chain residue has keto function, and if that is at lowest possible position
        String stereo = ms.getStereoStr().replaceAll("" + StereoConfiguration.Nonchiral.getStereosymbol(), "");
        if(stereo.contains("" + StereoConfiguration.Unknown.getStereosymbol())) {
            //TODO: Implement checks based just on modifications positions in case the stereochemistry is entirely undefined (e.g. in Hex5N-ol) 
            return true; //*** no checks possible ***
        }
        if(Stereocode.stereoStringContainsAbsoluteAndRelative(stereo)) {
            return true; //*** no checks possible ***
        }
        //*** check, if basetype resulting from current orientation is alphabetically lower than the one resulting from the rotated orientation: ***
        String stereo1;
        String stereo2;
        if(stereo.length() > 4) {
            stereo1 = stereo.substring(stereo.length() - 4, stereo.length());
            stereo2 = Stereocode.rotateStereoString(stereo.substring(0, 4));
        } else {
            stereo1 = stereo;
            stereo2 = Stereocode.rotateStereoString(stereo);
        }
        
        BasetypeTemplate basetype1 = container.getBasetypeTemplateByStereoString(stereo1);
        BasetypeTemplate basetype2 = container.getBasetypeTemplateByStereoString(stereo2);
        if(!basetype1.equals(basetype2)) {
             //*** basetypes resulting from the two orientations differ; check, if current one is the lexicographically lower one: ***
            if(basetype1.getBaseName().compareTo(basetype2.getBaseName()) > 0) {
                //ms.rotateAlditol();
                return false;
            }
        } else {
             //*** basetypes resulting from the two orientations are the same, check configurations: ***
            StereoConfiguration config1 = Stereocode.getConfigurationFromStereoString(stereo1);
            StereoConfiguration config2 = Stereocode.getConfigurationFromStereoString(stereo2);
            if(!config1.equals(config2)) {
                 //*** configurations differ; check, if current config is D ***
                if(config1.equals(StereoConfiguration.Laevus)) {
                    //ms.rotateAlditol();
                    return false;
                }
            } else {
                //*** residue is symmetric; check, if modifications are at lowest possible positions: ***
                //TODO: check, if all modifications or only substitutions have to be counted here, and how to handle second position of "en"
                int modSum1 = 0;
                int modSum2 = 0;
                int sizePlusOne = ms.getSize() + 1;
                List<Substitution> modList = ms.getSubstitutions();
                for(int m = 0; m < modList.size(); m++) {
                    ArrayList<Integer> positions = modList.get(m).getPositions();
                    for(int i = 0; i < positions.size(); i++) {
                        modSum1 += positions.get(i).intValue();
                        modSum2 += sizePlusOne - positions.get(i).intValue();
                    }
                }
                if(modSum1 > modSum2) {
                    //ms.rotateAlditol();
                    return false;
                } else if(modSum1 == modSum2) { 
                    //*** check for lexicographic order of lowest different modifications for both orientations: ***
                    for(int m = 1; m < sizePlusOne; m++) {
                        ArrayList<Substitution> mod1 = ms.getSubstitutionsByPosition(m);
                        ArrayList<Substitution> mod2 = ms.getSubstitutionsByPosition(sizePlusOne - m);
                        if(mod1.size() > 0 && mod2.size() > 0) {
                            //*** modification present at position m in both orientations; check lexicographic order: ***
                            ArrayList<String> modNames1 = new ArrayList<String>();
                            ArrayList<String> modNames2 = new ArrayList<String>();
                            for(int i = 0; i < mod1.size(); i ++) {
                                modNames1.add(mod1.get(i).getName());
                            }
                            Collections.sort(modNames1);
                            for(int i = 0; i < mod2.size(); i ++) {
                                modNames2.add(mod2.get(i).getName());
                            }
                            Collections.sort(modNames2);
                            for(int i = 0; i < Math.min(modNames1.size(), modNames2.size()); i++) {
                                if(modNames1.get(i).compareTo(modNames2.get(i)) > 0) {
                                    //ms.rotateAlditol();
                                    return false;
                                }
                            }
                            if(modNames1.size() < modNames2.size()) {
                                //ms.rotateAlditol();
                                return false;
                            } else if (modNames1.size() > modNames2.size()) {
                                return true;
                            }
                        } else if(mod1.size() == 0 && mod2.size() > 0) {
                            //ms.rotateAlditol();
                            return false;
                        } else if(mod1.size() > 0 && mod2.size() == 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Check, if a monosaccharide is fuzzily defined
     * @param ms the monosaccharide to be checked
     * @return true, if any uncertain property is found
     */
    public static boolean checkFuzziness(Monosaccharide ms) {
        if(ms.getStereocode().hasUncertainPosition()) {
            return true;
        }
        if(ms.getRingEnd() == Basetype.UNKNOWN_RING) {
            return true;
        }
        if(ms.getRingStart() == Basetype.UNKNOWN_RING) {
            return true;
        }
        for(CoreModification mod : ms.getCoreModifications()) {
            if(mod.getIntValuePosition1() == 0) {
                return true;
            }
            if(mod.hasPosition2() && mod.getIntValuePosition2() == 0) {
                return true;
            }
        }
        for(Substitution subst : ms.getSubstitutions()) {
            if(subst.getIntValuePosition1() == 0) {
                return true;
            }
            if(subst.getIntValueSubstituentPosition1() == 0) {
                return true;
            }
            if(subst.hasPosition2()) {
                if(subst.getIntValuePosition2() == 0) {
                    return true;
                }
                if(subst.getIntValueSubstituentPosition2() == 0) {
                    return true;
                }
            }
            if(subst.getTemplate() == null) {
                return true;
            }
            if(subst.getTemplate().isFuzzy()) {
                return true;
            }
        }
        return false;
    }

    /**
     * A quick check to determine if a substituent can be added at a given position of a monosaccharide.
     * For the check, the already existing core modifications and substitutions are taken into account, unless they contain fuzzy positions.
     * @param ms: The monosaccharide to be checked
     * @param position: The position to be checked
     * @param linktype: The LinkageType by which a substituent shall be linked.
     * @return true, if a substituent can be attached to the given position, otherwise false
     */
    public static boolean isSubstitutable(Monosaccharide ms, int position, LinkageType linktype) {
        if(position == 0) { //*** unknown position, no checks possible ***
            return true;
        }
        if((position < 0) || (position > ms.getSize())) { //*** position out of range ***
            return false;
        }
        if((position == ms.getRingStart()) || (position == ms.getRingEnd())) {
            return false;
        }
        //*** check core modifications: ***
        for(CoreModification mod: ms.getCoreModifications()) {
            if(linktype.equals(LinkageType.H_LOSE)) {
                if(mod.getTemplate().equals(CoreModificationTemplate.EN) || mod.getTemplate().equals(CoreModificationTemplate.ENX)) {
                    if(mod.position1equals(position) || mod.position2equals(position)) {
                        return false; //*** H_LOSE in combination with double bonds is not possible ***
                    }
                }
                if(mod.getTemplate().equals(CoreModificationTemplate.DEOXY)) {
                    continue; //*** H_LOSE modification can be added at a deoxy position ***
                }
            }
            if(mod.isSubstitutable()) {
                continue;
            }
            if(mod.position1equals(position)) {
                return false;
            }
            if(mod.position2equals(position)) {
                return false;
            }
        }
        //*** check substitutions: ***
        for(Substitution subst: ms.getSubstitutions()) {
            if(subst.hasPosition2()) {
                if(subst.position1equals(position) && subst.position2equals(position)) {
                    return false;
                }
                if(linktype.equals(LinkageType.H_LOSE)) {
                    if(subst.getLinkagetype1().equals(LinkageType.H_LOSE) && subst.position1equals(position)) {
                        return false;
                    }
                    if(subst.getLinkagetype2().equals(LinkageType.H_LOSE) && subst.position2equals(position)) {
                        return false;
                    }
                } else {
                    if(!subst.getLinkagetype1().equals(LinkageType.H_LOSE) && subst.position1equals(position)) {
                        return false;
                    }
                    if(!subst.getLinkagetype2().equals(LinkageType.H_LOSE) && subst.position2equals(position)) {
                        return false;
                    }
                }
            } else {
                if(linktype.equals(LinkageType.H_LOSE)) {
                    if(!subst.getLinkagetype1().equals(LinkageType.H_LOSE)) {
                        continue;
                    }
                } else {
                    if(subst.getLinkagetype1().equals(LinkageType.H_LOSE)) {
                        continue;
                    }
                }
                if(subst.position1equals(position)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Check, if the core modifications of a monosaccharide imply that it is open chain.
     * Otherwise, the open chain form has to be explicitely stressed in CarbBank style notations.
     * @param ms the Monosaccharide to be checked
     * @return
     */
    public static boolean impliesOpenChain(Monosaccharide ms) {
        if(ms.getRingtype().equals(Ringtype.OPEN)) {
            if(ms.isAlditol()) {
                return true;
            }
            if(ms.isAldaric() && !ms.hasCoreModification(CoreModificationTemplate.KETO)) {
                return true;
            }
            if(ms.isAldonic() && !ms.hasCoreModification(CoreModificationTemplate.KETO)) {
                return true;
            }
        }
        return false;
    }
    
}
