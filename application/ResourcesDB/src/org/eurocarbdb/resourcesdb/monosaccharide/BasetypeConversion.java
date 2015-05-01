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
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.*;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;

/** 
* Routines to convert monosaccharide basetypes of the EUROCarbDB sugar object model to ones of the MonoSaccharideDB object model and vice versa
* @author Thomas Luetteke
*
*/
public class BasetypeConversion {

    public static Monosaccharide eurcarbdbToMsdb(EcdbMonosaccharide eurocarbdbMs, Config conf, TemplateContainer container) throws ResourcesDbException {
        Monosaccharide msdbMs = new Monosaccharide(conf, container);
        eurocarbdbToMsdb(eurocarbdbMs, msdbMs);
        return msdbMs;
    }
    
    public static void eurocarbdbToMsdb(EcdbMonosaccharide eurocarbdbMs, Monosaccharide msdbMs) throws ResourcesDbException {
        msdbMs.setSize(eurocarbdbMs.getSuperclass().getNumberOfC());
        
        int ringStart = eurocarbdbMs.getRingStart();
        if(ringStart == EcdbMonosaccharide.OPEN_CHAIN) {
            ringStart = Basetype.OPEN_CHAIN;
        } else if(ringStart == EcdbMonosaccharide.UNKNOWN_RING) {
            ringStart = Basetype.UNKNOWN_RING;
        }
        msdbMs.setRingStartNoAdjustment(ringStart);
        msdbMs.setDefaultCarbonylPosition(ringStart);
        
        int ringEnd = eurocarbdbMs.getRingEnd();
        if(ringEnd == EcdbMonosaccharide.OPEN_CHAIN) {
            ringEnd = Basetype.OPEN_CHAIN;
        } else if(ringEnd == EcdbMonosaccharide.UNKNOWN_RING) {
            ringEnd = Basetype.UNKNOWN_RING;
        }
        msdbMs.setRingEnd(ringEnd);
        
        msdbMs.setAnomer(anomerEurocarbdbToMsdb(eurocarbdbMs.getAnomer()));
        //*** convert modifications: ***
        copyCoreModificationsFromEurocarbdbToMsdb(eurocarbdbMs, msdbMs);
        //*** get Stereocode from basetypes + modifications: ***
        String stereo = BasetypeConversion.getStereocodeFromBasetypeList(eurocarbdbMs.getBaseTypeList());
        if(stereo.length() > 0) {
            stereo = Stereocode.expandChiralonlyStereoString(stereo, msdbMs);
        } else { //*** residue is superclass only ***
            stereo = Stereocode.getSuperclassStereostring(msdbMs.getSize());
            stereo = Stereocode.markNonchiralPositionsInStereoString(stereo, msdbMs);
        }
        msdbMs.setStereoStr(stereo);
        msdbMs.setAnomerInStereocode();
    }
    
    public static org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbMonosaccharide msdbToEurocarbdb(Monosaccharide msdbMs) throws GlycoconjugateException {
        //*** create new object and set anomeric and superclass: ***
        org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbMonosaccharide eurocarbdbMs = new org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbMonosaccharide(anomerMsdbToEurocarbdb(msdbMs.getAnomer()), org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbSuperclass.forCAtoms(msdbMs.getSize()));
        
        int ringStart = msdbMs.getRingStart();
        if(ringStart == Basetype.OPEN_CHAIN) {
            ringStart = EcdbMonosaccharide.OPEN_CHAIN;
        } else if(ringStart == Basetype.UNKNOWN_RING) {
            ringStart = EcdbMonosaccharide.UNKNOWN_RING;
        }
        
        int ringEnd = msdbMs.getRingEnd();
        if(ringEnd == Basetype.OPEN_CHAIN) {
            ringEnd = EcdbMonosaccharide.OPEN_CHAIN;
        } else if(ringEnd == Basetype.UNKNOWN_RING) {
            ringEnd = EcdbMonosaccharide.UNKNOWN_RING;
        }
        
        if(ringEnd == EcdbMonosaccharide.UNKNOWN_RING) {
            ringStart = EcdbMonosaccharide.UNKNOWN_RING;
        }
        if(ringEnd == EcdbMonosaccharide.OPEN_CHAIN) {
            ringStart = EcdbMonosaccharide.OPEN_CHAIN;
        }
        eurocarbdbMs.setRing(ringStart, ringEnd);
        //*** set basetypes: ***
        ArrayList<EcdbBaseType> basetypeList = BasetypeConversion.getEurocarbdbMsBasetypesFromMsdbMonosaccharide(msdbMs);
        for(Iterator<EcdbBaseType> iter = basetypeList.iterator(); iter.hasNext();) {
            eurocarbdbMs.addBaseType(iter.next());
        }
        //*** convert modifications: ***
        for(Iterator<CoreModification> iter = msdbMs.getCoreModifications().iterator(); iter.hasNext();) {
            CoreModification msdbMod = iter.next();
            //*** check for modifications that are substituents in eurocarbdb: ***
            //*** (conversion is handled in MonosaccharideConverter.convertMonosaccharide()) ***
            if(msdbMod.getTemplate().equals(CoreModificationTemplate.ANHYDRO)) {
                continue; //*** anhydro is treated as substituent in eurocarbdb ***
            }
            if(msdbMod.getTemplate().equals(CoreModificationTemplate.LACTONE)) {
                continue; //*** lactone is treated as substituent in eurocarbdb ***
            }
            if(msdbMod.getTemplate().equals(CoreModificationTemplate.EPOXY)) {
                continue; //*** epoxy is treated as substituent in eurocarbdb ***
            }
            eurocarbdbMs.addModification(BasetypeConversion.ModificationMsdbToEurocarbdb(msdbMod));
        }
        return(eurocarbdbMs);
    }

    public static org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbAnomer anomerMsdbToEurocarbdb(Anomer anom) {
        if(Anomer.ALPHA.equals(anom)) {
            return(org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbAnomer.Alpha);
        }
        if(Anomer.BETA.equals(anom)) {
            return(org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbAnomer.Beta);
        }
        if(Anomer.OPEN_CHAIN.equals(anom) || Anomer.NONE.equals(anom)) {
            return(org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbAnomer.OpenChain);
        }
        return(org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbAnomer.Unknown);
    }
    
    public static Anomer anomerEurocarbdbToMsdb(org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbAnomer anom) {
        if(anom.equals(org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbAnomer.Alpha)) {
            return(Anomer.ALPHA);
        }
        if(anom.equals(org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbAnomer.Beta)) {
            return(Anomer.BETA);
        }
        if(anom.equals(org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbAnomer.OpenChain)) {
            //TODO: distinguish between openChain and no anomer in ring (would need entire monosaccharide, not just anomer)
            return(Anomer.OPEN_CHAIN);
        }
        return(Anomer.UNKNOWN);
    }
    
    private static HashMap<String, EcdbBaseType> eurocarbBasetypesByStereocodeMap = null;
    
    private static void fillEurocarbBasetypeByStereocodeMap() {
        BasetypeConversion.eurocarbBasetypesByStereocodeMap = new HashMap<String, EcdbBaseType>();
        for(EcdbBaseType bt : EcdbBaseType.values()) {
            BasetypeConversion.eurocarbBasetypesByStereocodeMap.put(bt.getStereo(), bt);
        }
    }
    
    private static EcdbBaseType getEurocarbBasetypeByStereoString(String stereo) throws ResourcesDbException {
        EcdbBaseType base = null;
        if(Stereocode.stereoStringHasRelativePosition(stereo)) {
            if(Stereocode.stereoStringContainsAbsoluteAndRelative(stereo)) {
                throw new MonosaccharideException("Cannot get EurocarbDB basetype from a stereocode string that contains both absolute and relative configurations: " + stereo);
            }
            return(getEuroCarbBasetypeByRelativeStereostring(stereo));
        } else {
            base = BasetypeConversion.eurocarbBasetypesByStereocodeMap.get(stereo);
        }
        if(base == null) {
            throw new ResourcesDbException("Cannot get EurocarbDB basetype from stereocode string " + stereo);
        }
        return(base);
    }
    
    private static EcdbBaseType getEuroCarbBasetypeByRelativeStereostring(String rStereo) throws ResourcesDbException {
        String aStereo = Stereocode.relativeToAbsolute(rStereo);
        EcdbBaseType aBase = BasetypeConversion.getEurocarbBasetypeByStereoString(aStereo);
        try {
            EcdbBaseType rBase = EcdbBaseType.forName("x" + aBase.getName().substring(1));
            return(rBase);
        } catch(GlycoconjugateException ge) {
            ResourcesDbException me = new ResourcesDbException("Cannot get EurocarbDB basetype from stereocode string " + rStereo);
            me.initCause(ge);
            throw me;
        }
    }
    
    /**
     * @param msdbMs
     * @return
     * @throws GlycoconjugateException
     */
    public static ArrayList<EcdbBaseType> getEurocarbdbMsBasetypesFromMsdbMonosaccharide(Monosaccharide msdbMs) throws GlycoconjugateException {
        if(BasetypeConversion.eurocarbBasetypesByStereocodeMap == null) {
            BasetypeConversion.fillEurocarbBasetypeByStereocodeMap();
        }
        ArrayList<EcdbBaseType> basetypeList = new ArrayList<EcdbBaseType>();
        try {
            String stereo = msdbMs.getStereoStr();
            if(msdbMs.getRingStart() > 1) { //*** anomeric center not at position1 => mask potential anomeric stereochemistry ***
                stereo = Stereocode.setPositionInStereoString(stereo, StereoConfiguration.Nonchiral.getStereosymbol(), msdbMs.getRingStart());
            }
            stereo = stereo.substring(1); //*** remove position1 (always nonchiral or anomeric) ***
            stereo = stereo.replaceAll("" + StereoConfiguration.Nonchiral.getStereosymbol(), "");
            if(!stereo.replaceAll("" + StereoConfiguration.Unknown.getStereosymbol(), "").equals("")) { //*** residue is not just a superclass ***
                if(stereo.contains("" + StereoConfiguration.Unknown.getStereosymbol())) {
                    throw new ResourcesDbException("MonosaccharideDB stereocode contains unknown configurations - cannot generate basetype list for EuroCarbDB monosaccharide from that.");
                }
                //*** translate stereocenters into basetype(s): ***
                while(stereo.length() > 0) {
                    if(stereo.length() > 4) {
                        basetypeList.add(0, BasetypeConversion.getEurocarbBasetypeByStereoString(stereo.substring(0, 4)));
                        stereo = stereo.substring(4);
                    } else {
                        basetypeList.add(0, BasetypeConversion.getEurocarbBasetypeByStereoString(stereo));
                        stereo = "";
                    }
                }
            }
        } catch(ResourcesDbException me) {
            throw new GlycoconjugateException("Error in translating stereocode to basetype list: " + me.getMessage());
        }
        return(basetypeList);
    }
    
    public static String getStereocodeFromBasetypeList(ArrayList<EcdbBaseType> basetypeList) throws ResourcesDbException {
        String stereo = "";
        for(Iterator<EcdbBaseType> iter = basetypeList.iterator(); iter.hasNext();) {
            EcdbBaseType basetype = iter.next();
            String tmpStereo = basetype.getStereo();
            if(tmpStereo.contains("*")) {
                String basename = basetype.getName();
                try {
                    basetype = EcdbBaseType.forName("d" + basename.substring(1));
                    tmpStereo = Stereocode.absoluteToRelative(basetype.getStereo());
                } catch(GlycoconjugateException ge) {
                    ResourcesDbException me = new ResourcesDbException("GetStereocodeFromBasetypeList: Cannot get absolute equivalent for relative basetype " + basename + " (d" + basename.substring(1) + ")");
                    me.initCause(ge);
                    throw me;
                }
            }
            stereo = tmpStereo + stereo;
        }
        return(stereo);
    }
    
    public static void copyCoreModificationsFromEurocarbdbToMsdb(org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbMonosaccharide eurocarbdbMs, Monosaccharide msdbMs) throws ResourcesDbException {
        ArrayList<org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification> eurocarbdbModificationList;
        eurocarbdbModificationList = eurocarbdbMs.getModificationList();
        for(Iterator<org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification> iter = eurocarbdbModificationList.iterator(); iter.hasNext();) {
            org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification eurocarbMod = iter.next();
            if(eurocarbMod.getName().equals(EcdbModificationType.ALDI.getName())) {
                if(eurocarbMod.getPositionOne() != 1) {
                    throw new ResourcesDbException("Alditol position other than one in EuroCarbDB monosaccharide");
                }
                msdbMs.setAlditol(true);
                continue;
            }
            CoreModification msdbCoremod = ModificationEurocarbdbToMsdb(eurocarbMod);
            //TO DO: take into account, that multiple keto modifications might be given in a residue whithout a defined ring start
            if(msdbCoremod.getTemplate().equals(CoreModificationTemplate.KETO) && (msdbMs.getDefaultCarbonylPosition() == Basetype.UNKNOWN_RING)) {
                msdbMs.setDefaultCarbonylPosition(msdbCoremod.getPosition1().get(0));
            }
            msdbMs.addCoreModification(msdbCoremod);
        }
    }
    
    public static CoreModification ModificationEurocarbdbToMsdb(org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification eurocarbdbMod) throws MonosaccharideException {
        CoreModification msdbMod = new CoreModification();
        if(eurocarbdbMod.getName().equals(EcdbModificationType.DEOXY.getName())) {
            msdbMod.setModification(CoreModificationTemplate.DEOXY, eurocarbdbMod.getPositionOne());
        } else if(eurocarbdbMod.getName().equals(EcdbModificationType.ACID.getName())) {
            msdbMod.setModification(CoreModificationTemplate.ACID, eurocarbdbMod.getPositionOne());
        } else if(eurocarbdbMod.getName().equals(EcdbModificationType.KETO.getName())) {
            msdbMod.setModification(CoreModificationTemplate.KETO, eurocarbdbMod.getPositionOne());
        } else if(eurocarbdbMod.getName().equals(EcdbModificationType.DOUBLEBOND.getName())) {
            msdbMod.setDivalentModification(CoreModificationTemplate.EN, eurocarbdbMod.getPositionOne(), eurocarbdbMod.getPositionOne() + 1);
        } else if(eurocarbdbMod.getName().equals(EcdbModificationType.UNKNOWN_BOUBLEBOND.getName())) {
            msdbMod.setDivalentModification(CoreModificationTemplate.ENX, eurocarbdbMod.getPositionOne(), eurocarbdbMod.getPositionOne() + 1);
        } else if(eurocarbdbMod.getName().equals(EcdbModificationType.SP2_HYBRID.getName())) {
            msdbMod.setModification(CoreModificationTemplate.SP2, eurocarbdbMod.getPositionOne());
        } else if(eurocarbdbMod.getName().equals(EcdbModificationType.GEMINAL.getName())) {
            throw new MonosaccharideException("Geminal residues not yet supported.");
            //TODO: implement geminal in msdb
        } else if(eurocarbdbMod.getName().equals(EcdbModificationType.ANHYDRO.getName())) {
            msdbMod.setDivalentModification(CoreModificationTemplate.ANHYDRO, eurocarbdbMod.getPositionOne(), eurocarbdbMod.getPositionTwo());
        } else {
            throw new MonosaccharideException("cannot convert eurocarbdb core modification " + eurocarbdbMod.getName());
        }
        return(msdbMod);
    }
    
    public static org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification ModificationMsdbToEurocarbdb(CoreModification msdbMod) throws GlycoconjugateException {
        org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification eurocarbdbMod = null;
        if(msdbMod.getTemplate().equals(CoreModificationTemplate.DEOXY)) {
            eurocarbdbMod = new org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification(EcdbModificationType.DEOXY.getName(), msdbMod.getIntValuePosition1());
        } else if(msdbMod.getTemplate().equals(CoreModificationTemplate.EN)) {
            eurocarbdbMod = new org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification(EcdbModificationType.DOUBLEBOND.getName(), msdbMod.getIntValuePosition1(), msdbMod.getIntValuePosition2());
        } else if(msdbMod.getTemplate().equals(CoreModificationTemplate.ENX)) {
            eurocarbdbMod = new org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification(EcdbModificationType.UNKNOWN_BOUBLEBOND.getName(), msdbMod.getIntValuePosition1(), msdbMod.getIntValuePosition2());
        } else if(msdbMod.getTemplate().equals(CoreModificationTemplate.ACID)) {
            eurocarbdbMod = new org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification(EcdbModificationType.ACID.getName(), msdbMod.getIntValuePosition1());
        } else if(msdbMod.getTemplate().equals(CoreModificationTemplate.KETO)) {
            eurocarbdbMod = new org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification(EcdbModificationType.KETO.getName(), msdbMod.getIntValuePosition1());
        } else if(msdbMod.getTemplate().equals(CoreModificationTemplate.SP2)) {
            eurocarbdbMod = new org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification(EcdbModificationType.SP2_HYBRID.getName(), msdbMod.getIntValuePosition1());
        } else if(msdbMod.getTemplate().equals(CoreModificationTemplate.ALDITOL)) {
            eurocarbdbMod = new org.eurocarbdb.resourcesdb.glycoconjugate_derived.EcdbModification(EcdbModificationType.ALDI.getName(), msdbMod.getIntValuePosition1());
        } else if(msdbMod.getTemplate().equals(CoreModificationTemplate.YN)) {
            throw new GlycoconjugateException("Core modification 'Yn' not defined for EuroCarbDB monosaccharides.");
        } else {
            throw new GlycoconjugateException("Unknown msdb core modification: " + msdbMod.getName());
        }
        return(eurocarbdbMod);
    }
    
}
