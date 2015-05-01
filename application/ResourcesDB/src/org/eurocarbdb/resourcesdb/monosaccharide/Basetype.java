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
import java.util.List;

import org.eurocarbdb.resourcesdb.*;
import org.eurocarbdb.resourcesdb.atom.*;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.io.GlycoCTExporter;
import org.eurocarbdb.resourcesdb.io.GlycoCTImporter;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;
import org.eurocarbdb.resourcesdb.util.Utils;

/**
* Basetype object, stores the properties of a monosaccharide basetype.
* 
* @author Thomas Luetteke
*/
public class Basetype extends MolecularEntity {
    private Stereocode stereocode;
    private int size;
    private int ringStart;
    private int ringEnd;
    private int defaultCarbonylPosition;
    private Anomer anomer;
    private StereoConfiguration configuration;
    private List<CoreModification> coreModifications;
    private String superclass;
    private Boolean isSuperclassFlag;
    
    private int dbId;
    
    /**
     * Constant to mark unknown ring size (0)
     */
    public static final int UNKNOWN_RING   = 0;
    
    /**
     * Constant to mark open chain residues (-1) 
     */
    public static final int OPEN_CHAIN   = -1;

    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public Basetype() {
        this(null, null);
    }
    
    public Basetype(Config conf, TemplateContainer container) {
        this.setConfig(conf);
        this.setTemplateContainer(container);
        this.init();
    }
    
    public Basetype(String glycoCTName) throws ResourcesDbException {
        this(glycoCTName, null, null);
    }

    public Basetype(String glycoCTName, Config conf, TemplateContainer container) throws ResourcesDbException {
        this.setConfig(conf);
        this.setTemplateContainer(container);
        this.init();
        GlycoCTImporter importer = new GlycoCTImporter(GlycanNamescheme.GLYCOCT, this.getConfig(), this.getTemplateContainer());
        Monosaccharide ms = new Monosaccharide(this);
        importer.parseMsString(glycoCTName, ms);
    }

    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    /**
     * @return the dbId
     */
    public int getDbId() {
        return dbId;
    }

    /**
     * @param dbId the dbId to set
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    /**
     * Get the ring start (carbonyl position) of the basetype.
     * @return the ring start position
     */
    public int getRingStart() {
        return this.ringStart;
    }

    /**
     * Set the ring start (carbonyl position) of the basetype.
     * In case the ring oxygen position is defined, it is adjusted to the new carbonyl position, i.e. the ring type is conserved
     * @param position the ring start position to set
     */
    public void setRingStart(int position) {
        int currentPosition = getRingStart();
        this.ringStart =position;
        if(getRingEnd() > 0) {  //*** ring oxygen was set before: adjust value to maintain correct ring type ***
            setRingEnd(getRingEnd() + (position - currentPosition));
        }
    }
    
    /**
     * Set the ring start (carbonyl position) of the basetype.
     * In contrast to the <code>setRingStart()</code> method, the ring oxygen is not touched here.
     * @param position the ring start position to set
     */
    public void setRingStartNoAdjustment(int position) {
        this.ringStart = position;
    }

    /**
     * Get the default carbonyl position (default ring start) of this basetype
     * @return the default carbonyl position
     */
    public int getDefaultCarbonylPosition() {
        return defaultCarbonylPosition;
    }

    /**
     * Set the default carbonyl position (default ring start) for this basetype
     * @param position the position to set
     */
    public void setDefaultCarbonylPosition(int position) {
        this.defaultCarbonylPosition = position;
    }

    /**
     * Get the size of the basetype
     * @return the size
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Set the size of the basetype
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Get the anomer of the basetype
     * @return the anomer
     */
    public Anomer getAnomer() {
        return this.anomer;
    }

    /**
     * Set the anomer of the basetype
     * @param anomer the anomer to set
     */
    public void setAnomer(Anomer anomer) {
        this.anomer = anomer;
    }
    
    /**
     * Set the anomer of the basetype by the anomer namestring
     * @param anomerStr the string representation of the anomer to set
     * @throws MonosaccharideException in case anomerStr cannot be translated into a valid anomer
     */
    public void setAnomer(String anomerStr) throws MonosaccharideException {
        setAnomer(Anomer.forNameOrSymbol(anomerStr));
    }
    
    /**
     * Get the anomer symbol of this basetype's anomer.
     * This direct access to the anomer symbol is needed for Hibernate mapping.
     * @return the anomer symbol, or null in case this.anomer is null
     */
    public String getAnomerSymbol() {
        if(this.getAnomer() == null) {
            return null;
        }
        return(this.getAnomer().getSymbol());
    }
    
    /**
     * This method is mainly needed for Hibernate mapping of the anomer to the database table, in which not the anomer itself but its symbol is stored.
     * If anomerStr is not null, the setAnomer(String) method is called.
     * @param anomerStr the symbol of the anomer to set, may be null (anomer is set to null then)
     * @throws MonosaccharideException
     */
    public void setAnomerSymbol(String anomerStr) throws MonosaccharideException {
        if(anomerStr == null) {
            this.setAnomer((Anomer)null);
        } else {
            this.setAnomer(anomerStr);
        }
    }

    /**
     * Get the configuration of the basetype
     * @return the configuration
     */
    public StereoConfiguration getConfiguration() {
        return this.configuration;
    }
    
    /**
     * Set the configuration of the basetype
     * @param configuration the configuration to set
     */
    public void setConfiguration(StereoConfiguration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Set the configuration of the basetype by a configuration string
     * @param configStr the string representation of the configuration to set
     * @throws MonosaccharideException in case configStr does not encode a valid configuration
     */
    public void setConfiguration(String configStr) throws MonosaccharideException {
        setConfiguration(StereoConfiguration.forNameOrSymbol(configStr));
    }

    public String getConfigurationSymbol() {
        if(this.configuration == null) {
            return StereoConfiguration.Unknown.getSymbol();
        } else {
            return this.configuration.getSymbol();
        }
    }
    
    public void setConfigurationSymbol(String confSymbol) throws MonosaccharideException {
        if(confSymbol == null) {
            this.setConfiguration((StereoConfiguration) null);
        } else {
            this.setConfiguration(confSymbol);
        }
    }

    /**
     * Get the ring end of the basetype
     * @return the ring end position
     */
    public int getRingEnd() {
        return this.ringEnd;
    }

    /**
     * Set the ring end position of the basetype
     * @param pos the ring end position to set
     */
    public void setRingEnd(int pos) {
        this.ringEnd = pos;
    }
    
    public void setRingtype(Ringtype type) throws MonosaccharideException {
        if(type.equals(Ringtype.PYRANOSE)) {
            setRingEnd(getRingStart() + 4);
        } else if(type.equals(Ringtype.FURANOSE)) {
            setRingEnd(getRingStart() + 3);
        } else if(type.equals(Ringtype.OPEN)) {
            setRingStartNoAdjustment(Basetype.OPEN_CHAIN);
            setRingEnd(Basetype.OPEN_CHAIN);
        } else if(type.equals(Ringtype.UNKNOWN)) {
            setRingStartNoAdjustment(Basetype.UNKNOWN_RING);
            setRingEnd(Basetype.UNKNOWN_RING);
        }
    }
    
    public Ringtype getRingtype() {
        if(getRingEnd() == Basetype.OPEN_CHAIN) {
            return(Ringtype.OPEN);
        }
        if(getRingStart() <= 0 || getRingEnd() == Basetype.UNKNOWN_RING) {
            return(Ringtype.UNKNOWN);
        }
        if(getRingEnd() - getRingStart() == 4) {
            return(Ringtype.PYRANOSE);
        }
        if(getRingEnd() - getRingStart() == 3) {
            return(Ringtype.FURANOSE);
        }
        return(Ringtype.UNKNOWN);
    }
    
    /**
     * Get the CarbBank symbol for this basetype's ring type
     * @return the ring type symbol to be used in CarbBank style names
     */
    public String getRingtypeSymbol() {
        return(getRingtype().getCarbbankSymbol());
    }
    
    /**
     * Set the ring start and end in one step
     * @param start the ring start postition
     * @param end the ring end position
     */
    public void setRingClosure(int start, int end) {
        this.ringStart = start;
        this.ringEnd = end;
    }
    
    /**
     * Check, if this basetype is an alditol.
     * @return true, if this basetype has an alditol core modification at position 1
     */
    public boolean isAlditol() {
        return this.hasCoreModification(CoreModificationTemplate.ALDITOL, 1);
    }

    /**
     * Set / unset an alditol modification for this basetype
     * @param alditol flag to indicate whether to set (true) or unset (false) the alditol modification
     * @throws MonosaccharideException in case the ring oxygen is > 0 (i.e. monosaccharide is ring form, which excludes alditol)
     */
    public void setAlditol(boolean alditol) throws MonosaccharideException {
        if(alditol) {
            if(this.getRingEnd() > 0) {
                throw new MonosaccharideException("Monosaccharide cannot be an alditol if it is in ring form");
            }
            this.setRingEnd(Basetype.OPEN_CHAIN);
            this.setRingStart(Basetype.OPEN_CHAIN);
            if(!this.hasCoreModification(CoreModificationTemplate.ALDITOL, 1)) {
                this.addCoreModification(new CoreModification(CoreModificationTemplate.ALDITOL, 1));
            }
        } else {
            if(this.hasCoreModification(CoreModificationTemplate.ALDITOL, 1)) {
                this.deleteCoreModification(CoreModificationTemplate.ALDITOL, 1);
            }
        }
    }

    /**
     * Get the Boolean object that marks whether this basetype is a superclass or not.
     * @return the isSuperclass
     */
    public Boolean getIsSuperclassFlag() {
        return this.isSuperclassFlag;
    }
    
    /**
     * Get the booleanValue of the isSuperclass object.
     * @return
     */
    public boolean isSuperclass() {
        return this.isSuperclassFlag.booleanValue();
    }

    /**
     * @param isSuperclass the isSuperclass to set
     */
    public void setIsSuperclassFlag(Boolean isSuperclass) {
        this.isSuperclassFlag = isSuperclass;
    }
    
    public void setIsSuperclass(boolean flag) {
        this.isSuperclassFlag = new Boolean(flag);
    }
    
    public void checkIsSuperclass() {
        this.setIsSuperclass(Stereocode.getChiralOnlyStereoString(this.getStereoStr()).length() == 0);
    }

    /**
     * @return the superclass
     */
    public String getSuperclass() {
        return this.superclass;
    }

    /**
     * @param superclass the superclass to set
     */
    public void setSuperclass(String superclass) {
        this.superclass = superclass;
    }

    //*****************************************************************************
    //*** stereocode related methods: *********************************************
    //*****************************************************************************
    
    /**
     * Get the stereocode of the monosaccharide
     * @return the stereocode
     */
    public Stereocode getStereocode() {
        return this.stereocode;
    }

    /**
     * Set the stereocode of the monosaccharide
     * @param stereocode the stereocode to set
     */
    public void setStereocode(Stereocode stereocode) {
        this.stereocode = stereocode;
    }
    
    /**
     * Get the stereocode string of the monosaccharide
     * @return the string value of the stereocode
     */
    public String getStereoStr() {
        if(getStereocode() != null) {
            return(getStereocode().getStereoStr());
        }
        return("");
    }
    
    /**
     * Set the string value of the stereocode of the monosaccharide
     * @param stereoStr the stereo string to set
     */
    public void setStereoStr(String stereoStr) {
        if(getStereocode() == null) {
            setStereocode(new Stereocode(stereoStr));
        } else {
            getStereocode().setStereoStr(stereoStr);
        }
    }
    
    public String getStereoStrWithoutAnomeric() throws ResourcesDbException {
        String stereo = this.getStereoStr();
        if(this.getRingStart() > 0 && this.getRingStart() < stereo.length()) {
            stereo = Stereocode.maskAnomerInStereoString(stereo, this);
        }
        return stereo;
    }

    /** 
     * Set the stereochemistry resulting from the anomer in the stereocode
     */
    public void setAnomerInStereocode() throws ResourcesDbException {
        String stereo = getStereoStr();
        int anomerPosition = getRingStart();
        if(anomerPosition >= 1) {
            String stereosymbolAnomer = getAnomer().getStereosymbolD();
            StereoConfiguration refConf = getAnomericReferenceConfiguration();
            if(refConf.equals(StereoConfiguration.Laevus)) {
                stereosymbolAnomer = Stereocode.changeDLinStereoString(stereosymbolAnomer);
            } else if(refConf.equals(StereoConfiguration.XLaevus)) {
                stereosymbolAnomer = Stereocode.changeDLinStereoString(stereosymbolAnomer);
                stereosymbolAnomer = Stereocode.absoluteToRelative(stereosymbolAnomer);
            } else if(refConf.equals(StereoConfiguration.XDexter)) {
                stereosymbolAnomer = Stereocode.absoluteToRelative(stereosymbolAnomer);
            }
            stereo = stereo.substring(0, anomerPosition - 1) + stereosymbolAnomer + stereo.substring(anomerPosition);
        }
        setStereoStr(stereo);
    }
    
    /**
     * Get the configuration that to which the anomeric center has to be compared to decide whether a monosaccharide is alpha or beta
     * @return the configuration of the anomeric reference atom
     * @throws MonosaccharideException
     */
    public StereoConfiguration getAnomericReferenceConfiguration() throws ResourcesDbException {
        String stereo = getStereoStr();
        int anomerPosition = getRingStart();
        if(anomerPosition >= 1) {
            stereo = Stereocode.setPositionInStereoString(stereo, Stereocode.StereoN, anomerPosition);
        }
        stereo = Stereocode.getChiralOnlyStereoString(stereo);
        StereoConfiguration conf;
        if(stereo.length() > 4) {
            conf = StereoConfiguration.forStereosymbol(stereo.charAt(3));
        } else if(stereo.length() == 0) {
            conf = StereoConfiguration.Unknown;
        } else {
            conf = StereoConfiguration.forStereosymbol(stereo.charAt(stereo.length() - 1));
        }
        return(conf);
    }
    
    /**
     * Determine this basetype's anomer using the stereocode
     * @return the resulting anomer
     * @throws ResourcesDBException
     */
    public Anomer getAnomerFromStereocode() throws ResourcesDbException {
        if(this.getRingtype().equals(Ringtype.OPEN)) {
            return Anomer.OPEN_CHAIN;
        }
        if(this.getRingStart() > 0 && this.isStereolossPositionWithIgnoreType(this.getRingStart(), CoreModificationTemplate.KETO)) {
            return Anomer.NONE;
        }
        StereoConfiguration anomRefConf = this.getAnomericReferenceConfiguration();
        StereoConfiguration anomConf = Stereocode.getPositionFromStereoString(this.getStereoStr(), this.getRingStart());
        if(anomRefConf.equals(StereoConfiguration.Dexter)) {
            if(anomConf.equals(StereoConfiguration.Dexter)) {
                return Anomer.ALPHA;
            }
            if(anomConf.equals(StereoConfiguration.Laevus)) {
                return Anomer.BETA;
            }
        }
        if(anomRefConf.equals(StereoConfiguration.Laevus)) {
            if(anomConf.equals(StereoConfiguration.Laevus)) {
                return Anomer.ALPHA;
            }
            if(anomConf.equals(StereoConfiguration.Dexter)) {
                return Anomer.BETA;
            }
        }
        if(anomRefConf.equals(StereoConfiguration.XDexter)) {
            if(anomConf.equals(StereoConfiguration.XDexter)) {
                return Anomer.ALPHA;
            }
            if(anomConf.equals(StereoConfiguration.XLaevus)) {
                return Anomer.BETA;
            }
        }
        if(anomRefConf.equals(StereoConfiguration.XLaevus)) {
            if(anomConf.equals(StereoConfiguration.XLaevus)) {
                return Anomer.ALPHA;
            }
            if(anomConf.equals(StereoConfiguration.XDexter)) {
                return Anomer.BETA;
            }
        }
        return Anomer.UNKNOWN;
    }
    
    public StereoConfiguration getStereoConfigurationByPosition(int pos) throws ResourcesDbException {
        if(this.getStereoStr().length() >= pos) {
            return StereoConfiguration.forStereosymbol(this.getStereoStr().charAt(pos - 1));
        }
        return StereoConfiguration.Unknown;
    }
    
    //*****************************************************************************
    //*** modification related methods: *******************************************
    //*****************************************************************************
    
    /**
     * Get this basetype's core modifications
     * @return a list of core modifications
     */
    public List<CoreModification> getCoreModifications() {
        if(this.coreModifications == null) {
            this.coreModifications = new ArrayList<CoreModification>();
        }
        return this.coreModifications;
    } 
    
    /**
     * Set this basetype's core modifications
     * @param coreModList the list of core modifications to set
     */
    public void setCoreModifications(List<CoreModification> coreModList) {
        this.coreModifications = coreModList;
        sortCoreModifications();
    }

    /** 
     * Check, if the position of a modification is valid 
     * @param mod The modification to be checked
     * @return true, if modification is at valid position(s); false, if modification is already present
     * @throws MonosaccharideException (in case the modification is at an invalid position)
     */
    public boolean checkModificationPosition(CoreModification mod) throws MonosaccharideException {
        ArrayList<Integer> positionsList = mod.getPosition1Clone();
        positionsList.addAll(mod.getPosition2());
        Collections.sort(positionsList);
        for(int i = 0; i < positionsList.size(); i++) {
            int position = positionsList.get(i).intValue();
            if(position < 0 || position > getSize()) {
                throw new MonosaccharideException("Modification position out of range: " + mod.toString());
            }
            if(position > 0) {
                if(position == getRingStart()) {
                    /*if(mod.getTemplate().equals(CoreModificationTemplate.Ulo)) {
                        return(false);
                    }*/
                    //*** "acid" is allowed for open chain residues, other modifications are not allowed at carbonyl position ***
                    //TODO: check rules for modifications (e.g. EN is allowed)
                    if(!this.getRingtype().equals(Ringtype.OPEN) && !(mod.getTemplate().equals(CoreModificationTemplate.ACID) && this.getRingStart() == 1) && !(mod.getTemplate().equals(CoreModificationTemplate.KETO)) && !(mod.getTemplate().equals(CoreModificationTemplate.ENX)) && !(mod.getTemplate().equals(CoreModificationTemplate.EN)) && !(mod.getTemplate().equals(CoreModificationTemplate.DEOXY))) {
                        throw new MonosaccharideException("Cannot modify monosaccharide at carbonyl position (" + getRingStart() + ").");
                    }
                }
                ArrayList<CoreModification> posMods = getCoreModificationsByPosition(position); //*** list of modifications, that are already present at the position ***
                for(CoreModification exstMod : posMods) {
                    if(mod.equals(exstMod)) { //*** modification already present ***
                        System.out.println("checkModificationPosition(): modification already present");
                        return false;
                    }
                    if(!mod.isSubstitutable() && !exstMod.isSubstitutable()) {
                        throw new MonosaccharideException("Position already modified: " + exstMod.toString() + "\n Cannot add modification " + mod.toString());
                    }
                }
            }
        }
        return(true);
    }
    
    /** 
     * Add a modification to this monosaccharide
     * @param mod The modification to be added
     * @return true, if modification was added; false, if modification is already present
     * @throws MonosaccharideException (in case the modification is at an invalid position)
     */
    public boolean addCoreModification(CoreModification mod) throws MonosaccharideException {
        if(this.hasCoreModification(mod)) {
            return false;
        }
        int pos = mod.getIntValuePosition1();
        /*if(pos > 0 && pos == this.getCarbonylPosition()) {
            if(mod.getTemplate().equals(CoreModificationTemplate.Acid)) {
                if(this.getRingOxygen() > 0) {
                    throw new MonosaccharideException("Cannot add 'Acid' modification to carbonyl position in ring monosaccharide.");
                }
                this.setRingOxygen(-1);
            }
        }*/
        this.coreModifications.add(mod);
        if(pos > 0 && mod.getTemplate().equals(CoreModificationTemplate.KETO)) {
            if(this.getRingStart() == 1) {
                if(pos != 1 && this.getCoreModification(CoreModificationTemplate.KETO.getName(), 1) == null) {
                    setRingStart(pos);
                }
            } else if(this.getRingStart() < 1) {
                //TODO: check, if and how carbonyl position has to be adjusted
            }
        }
        sortCoreModifications();
        return true;
    }
    
    /** 
     * Add a modification to this monosaccharide
     * @param modTmpl The CoreModificationTemplate of the modification to be added
     * @param position The position of the modification to be added
     * @return true, if modification was added; false, if modification is already present
     * @throws MonosaccharideException (in case the modification is at an invalid position)
     */
    public boolean addCoreModification(CoreModificationTemplate modTmpl, int position) throws MonosaccharideException {
        return this.addCoreModification(new CoreModification(modTmpl, position));
    }
    
    public void deleteCoreModification(CoreModificationTemplate modTmpl, int position) throws MonosaccharideException {
        this.deleteCoreModification(modTmpl.getName(), position);
    }
    
    public void deleteCoreModification(String name, int position) throws MonosaccharideException {
        List<CoreModification> modList = getCoreModifications();
        for(int i = 0; i < modList.size(); i++) {
            CoreModification mod = modList.get(i);
            if(mod.getName().equals(name) && mod.getPosition1().get(0).intValue() == position) {
                modList.remove(i);
                return;
            }
        }
        throw new MonosaccharideException("Cannot remove CoreModification " + position + name + ": modification not found");
    }
    
    public void deleteCoreModification(CoreModification mod) throws MonosaccharideException {
        List<CoreModification> modList = getCoreModifications();
        for(int i = 0; i < modList.size(); i++) {
            CoreModification presentMod = modList.get(i);
            if(presentMod.equals(mod)) {
                modList.remove(i);
                return;
            }
        }
        throw new MonosaccharideException("Cannot remove CoreModification: modification not found (" + mod.toString() + ")");
    }
    
    public void initCoreModifications() {
        if(getCoreModifications() == null) {
            this.coreModifications = new ArrayList<CoreModification>();
        }
        getCoreModifications().clear();
    }
    
    /**
     * Get the number of core modifications
     * @return the core modification count
     */
    public int countCoreModifications() {
        return(getCoreModifications().size());
    }
    
    /**
     * Get the number of core modifications of a given type (identified by mod. name)
     * @param name the name of the core modification
     * @return the core modification count
     */
    public int countCoreModifications(String name) {
        int count = 0;
        List<CoreModification> modifications = getCoreModifications();
        for(int i = 0; i < modifications.size(); i++) {
            CoreModification mod = modifications.get(i);
            if(mod.getName().equals(name)) {
                count ++;
            }
        }
        return count;
    }
    
    /**
     * Get the number of core modifications of a given type (identified by mod. template)
     * @param tmpl the core modification template
     * @return the core modification count
     */
    public int countCoreModifications(CoreModificationTemplate tmpl) {
        int count = 0;
        List<CoreModification> modifications = getCoreModifications();
        for(int i = 0; i < modifications.size(); i++) {
            CoreModification mod = modifications.get(i);
            if(mod.getTemplate().equals(tmpl)) {
                count ++;
            }
        }
        return count;
    }
    
    public void setUronic() throws MonosaccharideException {
        CoreModification mod = new CoreModification(CoreModificationTemplate.ACID, getSize());
        addCoreModification(mod);
    }
    
    public boolean isUronic() {
        return(!this.hasCoreModification(CoreModificationTemplate.ACID, 1) && this.hasCoreModification(CoreModificationTemplate.ACID, getSize()));
    }
    
    public void setAldonic() throws MonosaccharideException {
        CoreModification mod = new CoreModification(CoreModificationTemplate.ACID, 1);
        addCoreModification(mod);
    }
    
    public boolean isAldonic()  {
        return(this.hasCoreModification(CoreModificationTemplate.ACID, 1) && !this.hasCoreModification(CoreModificationTemplate.ACID, getSize()));
    }
    
    public void setAldaric() throws MonosaccharideException {
        setAldonic();
        setUronic();
    }
    
    public boolean isAldaric() {
        return (this.hasCoreModification(CoreModificationTemplate.ACID, 1) && this.hasCoreModification(CoreModificationTemplate.ACID, getSize()));
    }
    
    /**
     * Check, if this basetype has a double bond at a given position
     * @param pos the position to check
     * @return true, if there is an EN or ENX core modification present at the given position
     */
    public boolean hasDoubleBond(int pos) {
        return (this.hasCoreModification(CoreModificationTemplate.EN, pos) || this.hasCoreModification(CoreModificationTemplate.ENX, pos));
    }
    
    /**
     * Get a list of all positions that have a modification which results in a loss of stereochemistry
     * @return list of achiral positions (empty list, if no hits are found)
     */
    public ArrayList<Integer> getStereolossPositions() {
        List<CoreModification> modifications = getCoreModifications();
        ArrayList<Integer> positions = new ArrayList<Integer>();
        for(int i = 0; i < modifications.size(); i++) {
            CoreModification mod = modifications.get(i);
            if(mod.getTemplate().isStereoLoss()) {
                ArrayList<Integer> modPositions = mod.getPositions();
                for(int p = 0; p < modPositions.size(); p++) {
                    Integer pos = modPositions.get(p);
                    if(!positions.contains(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        Collections.sort(positions);
        return(positions);
    }
    
    /**
     * Check, if there is a loss of stereochemistry at a given position (e.g. due to deoxygenation, double bonds, etc.)
     * @param pos the position to check
     * @return true, if the position has a loss of stereochemistry, otherwise false
     */
    public boolean isStereolossPosition(int pos) {
        ArrayList<CoreModification> modifications = getCoreModificationsByPosition(pos);
        for(CoreModification mod : modifications) {
            if(mod.getTemplate().isStereoLoss()) {
                return(true);
            }
        }
        return(false);
    }
    
    /**
     * Check, if there is a loss of stereochemistry at a given position (e.g. due to deoxygenation, double bonds, etc.)
     * that is not caused by the modification type specified in the "ignore" parameter.
     * @param pos the position to check
     * @param ignore the modification type to ignore
     * @return true, if the position has a loss of stereochemistry (not caused by the ignored modification type), otherwise false
     */
    public boolean isStereolossPositionWithIgnoreType(int pos, CoreModificationTemplate ignore) {
        ArrayList<CoreModification> modifications = getCoreModificationsByPosition(pos);
        for(CoreModification mod : modifications) {
            if(mod.getTemplate().equals(ignore)) {
                continue;
            }
            if(mod.getTemplate().isStereoLoss()) {
                return(true);
            }
        }
        return(false);        
    }
    
    /**
     * Get a list of all core modifications that are present at a given position
     * @param position the position for which the core modifications are listed
     * @return list of core modifications (empty list, if no hits are found)
     */
    public ArrayList<CoreModification> getCoreModificationsByPosition(int position) {
        ArrayList<CoreModification> positionMods = new ArrayList<CoreModification>();
        Integer positionInt = new Integer(position);
        for(CoreModification mod : getCoreModifications()) {
            if(mod.getPositions().contains(positionInt)) {
                positionMods.add(mod);
            }
        }
        return(positionMods);
    }
    
    /**
     * Get a list of all core modification types that are present at a given position
     * @param position the position for which the core modification types are listed
     * @return list of core modification templates (empty list, if no hits are found)
     */
    public ArrayList<CoreModificationTemplate> getCoreModificationTemplatesByPosition(int position) {
        ArrayList<CoreModificationTemplate> templateList = new ArrayList<CoreModificationTemplate>();
        for(CoreModification mod : this.getCoreModificationsByPosition(position)) {
            templateList.add(mod.getTemplate());
        }
        return(templateList);
    }
    
    /**
     * Get a list of all core modifications of a given type (identified by type name)
     * @param name the name of the core modification
     * @return list of core modifications (empty list, if no hits are found)
     */
    public ArrayList<CoreModification> getCoreModifications(String name) {
        ArrayList<CoreModification> modList = new ArrayList<CoreModification>();
        for(int i = 0; i < getCoreModifications().size(); i++) {
            if(getCoreModifications().get(i).getName().equalsIgnoreCase(name)) {
                modList.add(getCoreModifications().get(i));
            }
        }
        return(modList);
    }
    
    /**
     * Get a list of all core modifications of a given type (identified by type template)
     * @param tmpl the core modification template
     * @return list of core modifications (empty list, if no hits are found)
     */
    public ArrayList<CoreModification> getCoreModifications(CoreModificationTemplate tmpl) {
        ArrayList<CoreModification> modList = new ArrayList<CoreModification>();
        for(int i = 0; i < getCoreModifications().size(); i++) {
            if(getCoreModifications().get(i).getTemplate().equals(tmpl)) {
                modList.add(getCoreModifications().get(i));
            }
        }
        return(modList);
    }
    
    /**
     * Get a core modification specified by name and position
     * Note: If more than one modification matches the given criteria (e.g. due to uncertain positions) the first match is returned.
     * @param name the name of the core modification
     * @param position the position of the core modification
     * @return the core modification or null if no modification matching the given criteria is present
     */
    public CoreModification getCoreModification(String name, int position) {
        ArrayList<CoreModification> modList = getCoreModificationsByPosition(position);
        for(int i = 0; i < modList.size(); i++) {
            if(modList.get(i).getName().equals(name)) {
                return(modList.get(i));
            }
        }
        return(null);
    }
    
    public CoreModification getCoreModification(CoreModificationTemplate tmpl, int position) {
        ArrayList<CoreModification> modList = getCoreModificationsByPosition(position);
        for(int i = 0; i < modList.size(); i++) {
            if(modList.get(i).getTemplate().equals(tmpl)) {
                return(modList.get(i));
            }
        }
        return(null);
    }
    
    /**
     * Get a list of EN/ENX core modifications present in this basetype
     * @return
     */
    public ArrayList<CoreModification> getEnModifications() {
        ArrayList<CoreModification> enList = new ArrayList<CoreModification>();
        for(CoreModification mod : this.getCoreModifications()) {
            if(mod.getTemplate().equals(CoreModificationTemplate.EN)) {
                enList.add(mod);
            } else if(mod.getTemplate().equals(CoreModificationTemplate.ENX)) {
                enList.add(mod);
            }
        }
        return enList;
    }
    
    /**
     * Test, if a given core modification is present in the basetype.
     * @param mod: The core modification to be checked for.
     * @return true, if the core modification is present, otherwise false.
     */
    public boolean hasCoreModification(CoreModification mod) {
        for(int i = 0; i < getCoreModifications().size(); i++) {
            if(getCoreModifications().get(i).equals(mod)) {
                return(true);
            }
        }
        return(false);
    }
    
    /**
     * Test, if a given core modification is present in the basetype.
     * @param tmpl: the core modification template
     * @param position: the position of the core modification
     * @return true, if the core modification is present, otherwise false.
     */
    public boolean hasCoreModification(CoreModificationTemplate tmpl, int position) {
        try {
            CoreModification coreMod = new CoreModification(tmpl, position);
            return(hasCoreModification(coreMod));
        } catch(MonosaccharideException me) {
            return(false);
        }
    }
    
    /**
     * Test, if a core modification of a certain type is present in the basetype (at any position)
     * @param tmpl the CoreModificationTemplate indicating the modification type
     * @return true, if such a modification is present, otherwise false
     */
    public boolean hasCoreModification(CoreModificationTemplate tmpl) {
        for(CoreModification mod : this.getCoreModifications()) {
            if(mod.getName().equals(tmpl.getName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check, whether the monosaccharide contains a core modification that is located at an uncertain position
     * @return
     */
    public boolean hasUncertainCoremodificationPosition() {
        for(CoreModification mod : getCoreModifications()) {
            if(mod.hasUncertainLinkagePosition()) {
                return(true);
            }
        }
        return(false);
    }
    
    /** 
     * Adjust the positions of core modifications resulting from a rotation of the monosaccharide by 180 degrees
     * Such a rotation might occurr with alditol residues or aldaric acids.
     */
    public void mirrorCoreModificationPositions() {
        List<CoreModification> modList = getCoreModifications();
        for(int m = 0; m < modList.size(); m++) {
            CoreModification mod = modList.get(m);
            if(mod.getTemplate().equals(CoreModificationTemplate.ALDITOL)) {
                continue; //*** alditol modification remains at position 1 when a basetype is mirrored ***
            }
            ArrayList<Integer> positions1 = mod.getPosition1();
            for(int i = 0; i < positions1.size(); i++) {
                int pos = positions1.get(i);
                if(pos > 0) {
                    pos = getSize() + 1 - pos;
                    positions1.set(i, new Integer(pos));
                }
            }
            //Collections.sort(positions1);
            
            ArrayList<Integer> positions2 = mod.getPosition2();
            for(int i = 0; i < positions2.size(); i++) {
                int pos = positions2.get(i);
                if(pos > 0) {
                    pos = getSize() + 1 - pos;
                    positions2.set(i, new Integer(pos));
                }
            }
            //Collections.sort(positions2);
            
            mod.setPosition1(positions1);
            mod.setPosition2(positions2);
            
            mod.sortPositions();
        }
        //Collections.sort(modList, new Modification());
        sortCoreModifications();
    }
    
    public void sortCoreModifications() {
        List<CoreModification> modList = this.getCoreModifications();
        for(int i = 0; i < modList.size(); i++) {
            for(int j = modList.size() - 1; j > 0; j--) {
                if(modList.get(j).makeCmpString().compareTo(modList.get(j - 1).makeCmpString()) < 0) {
                    CoreModification tmpMod = modList.get(j - 1);
                    modList.set(j - 1, modList.get(j));
                    modList.set(j, tmpMod);
                }
            }
        }
    }
    
    //*****************************************************************************
    //*** atom related methods: ***************************************************
    //*****************************************************************************
    
    /**
     * Generate the atoms of this basetype using the global config.
     * @throws MonosaccharideException
     */
    public void buildAtoms() throws ResourcesDbException {
        this.buildAtoms(Config.getGlobalConfig());
    }
    
    /**
     * Generate the atoms of this basetype
     * @param conf the config. to be used for building the atoms
     * @throws MonosaccharideException
     */
    public void buildAtoms(Config conf) throws ResourcesDbException {
        this.setAtoms(new ArrayList<Atom>());
        Atom anomericCarbon = null;
        Atom previousCarbon = null;
        for(int pos = 1; pos <= this.getSize(); pos++) {
            int hCount = 1;
            if(pos == 1 || pos == this.getSize()) {
                hCount ++;
            }
            boolean hasOH = true;
            boolean isAcid = false;
            boolean isUlo = false;
            double boToPreviousCarbon = 1.0;
            Atom currentCarbon = new Atom(Periodic.C);
            
            if(pos == this.getRingStart() && !this.isAlditol()) {
                hCount --;
                if(this.getRingEnd() > 0) {
                    anomericCarbon = currentCarbon;
                } else {
                    hasOH = false;
                    isUlo = true;
                }
            }
            //*** check core modifications: ***
            for(CoreModification mod : this.getCoreModificationsByPosition(pos)) {
                if(mod.getIntValuePosition1() == 0) {
                    throw new MonosaccharideException("Cannot build atoms for basetype with uncertain core modification position.");
                }
                if(mod.getTemplate().equals(CoreModificationTemplate.EN)) {
                    hCount--;
                    if(mod.getIntValuePosition2() == pos) {
                        boToPreviousCarbon = 2;
                    }
                    continue;
                }
                if(mod.getTemplate().equals(CoreModificationTemplate.ENX)) {
                    throw new MonosaccharideException("Cannot build atoms for basetype with Enx core modification.");
                }
                if(mod.getTemplate().equals(CoreModificationTemplate.YN)) {
                    hCount -= 2;
                    if(mod.getIntValuePosition2() == pos) {
                        boToPreviousCarbon = 3;
                    }
                    continue;
                }
                if(mod.getTemplate().equals(CoreModificationTemplate.DEOXY)) {
                    hCount++;
                    hasOH = false;
                    continue;
                }
                if(mod.getTemplate().equals(CoreModificationTemplate.SP2)) {
                    hCount--;
                    continue;
                }
                if(mod.getTemplate().equals(CoreModificationTemplate.SP)) {
                    hCount -= 2;
                    continue;
                }
                if(mod.getTemplate().equals(CoreModificationTemplate.ACID)) {
                    isAcid = true;
                    hasOH = false;
                    hCount -= 2;
                    continue;
                }
                if(mod.getTemplate().equals(CoreModificationTemplate.KETO) && !(this.getRingStart() == pos)) {
                    hCount --;
                    hasOH = false;
                    isUlo = true;
                }
            }
            
            //*** build atoms for current position: ***
            if(isAcid) {
                if(conf.isCarboxylGroupsDeprotonated()) {
                    currentCarbon.setTemplateAndInit(AtomTemplate.COOH_C_OX);
                } else {
                    currentCarbon.setTemplateAndInit(AtomTemplate.COOH_C_OH);
                }
            } else {
                currentCarbon.setTemplateAndInit(AtomTemplate.BB_C);
            }
            currentCarbon.setName(currentCarbon.getTemplate().formatAtomName(pos));
            if(pos == 1) {
                this.addAtom(currentCarbon);
            } else {
                this.addAtom(currentCarbon, previousCarbon, boToPreviousCarbon);
            }
            if(hasOH) {
                if(pos == this.getRingEnd()) {
                    Atom ringO = new Atom(Periodic.O);
                    ringO.setTemplateAndInit(AtomTemplate.BB_OR);
                    ringO.setName(ringO.getTemplate().formatAtomName(pos));
                    this.addAtom(ringO, currentCarbon, 1);
                    this.addBond(ringO, anomericCarbon, 1);
                } else {
                    Atom ohO = new Atom(Periodic.O);
                    ohO.setTemplateAndInit(AtomTemplate.BB_O);
                    ohO.setName(ohO.getTemplate().formatAtomName(pos));
                    this.addAtom(ohO, currentCarbon, 1);
                    Atom ohH = new Atom(Periodic.H);
                    ohH.setTemplateAndInit(AtomTemplate.BB_HO);
                    ohH.setName(ohH.getTemplate().formatAtomName(pos));
                    this.addAtom(ohH, ohO, 1);
                }
            }
            if(isUlo) {
                Atom oc = new Atom(Periodic.O);
                oc.setTemplateAndInit(AtomTemplate.BB_O);
                oc.setName(oc.getTemplate().formatAtomName(pos));
                this.addAtom(oc, currentCarbon, 2);
            }
            if(isAcid) {
                if(conf.isCarboxylGroupsDeprotonated()) {
                    Atom oa = new Atom(Periodic.O);
                    oa.setTemplateAndInit(AtomTemplate.COOH_OX);
                    oa.setName(oa.getTemplate().formatAtomName(pos, 0));
                    this.addAtom(oa, currentCarbon, 1.5);
                    Atom ob = new Atom(Periodic.O);
                    ob.setTemplateAndInit(AtomTemplate.COOH_OX);
                    ob.setName(oa.getTemplate().formatAtomName(pos, 1));
                    this.addAtom(ob, currentCarbon, 1.5);
                } else {
                    Atom o = new Atom(Periodic.O);
                    o.setTemplateAndInit(AtomTemplate.COOH_O);
                    o.setName(o.getTemplate().formatAtomName(pos));
                    this.addAtom(o, currentCarbon, 2);
                    Atom ohO = new Atom(Periodic.O);
                    ohO.setTemplateAndInit(AtomTemplate.COOH_OH);
                    ohO.setName(ohO.getTemplate().formatAtomName(pos));
                    this.addAtom(ohO, currentCarbon, 1);
                    Atom ohH = new Atom(Periodic.H);
                    ohH.setTemplateAndInit(AtomTemplate.COOH_H);
                    ohH.setName(ohH.getTemplate().formatAtomName(pos));
                    this.addAtom(ohH, ohO, 1);
                }
            }
            if(hCount == 1) {
                Atom hn = new Atom(Periodic.H);
                hn.setTemplateAndInit(AtomTemplate.BB_H);
                hn.setName(hn.getTemplate().formatAtomName(pos));
                this.addAtom(hn, currentCarbon, 1);
            } else {
                //char indexChar = 'a';
                for(int h = 0; h < hCount; h++) {
                    Atom hn = new Atom(Periodic.H);
                    hn.setTemplateAndInit(AtomTemplate.BB_HX);
                    //hn.setName("H" + pos + indexChar++);
                    hn.setName(hn.getTemplate().formatAtomName(pos, h));
                    this.addAtom(hn, currentCarbon, 1);
                }
            }
            previousCarbon = currentCarbon;
        }
    }
    
    public Atom getLinkingAtom(int position, LinkageType linktype) throws ResourcesDbException {
        Atom linkAtom = null;
        Atom removeAtom = null;
        if(linktype.equals(LinkageType.H_AT_OH)) {
            linkAtom = this.getAtomByName(AtomTemplate.BB_O.formatAtomName(position));
            removeAtom = this.getAtomByName(AtomTemplate.BB_HO.formatAtomName(position));
        } else if(linktype.equals(LinkageType.DEOXY)) {
            linkAtom = this.getAtomByName(AtomTemplate.BB_C.formatAtomName(position));
            removeAtom = this.getAtomByName(AtomTemplate.BB_O.formatAtomName(position));
        } else if(linktype.equals(LinkageType.H_LOSE)) {
            linkAtom = this.getAtomByName(AtomTemplate.BB_C.formatAtomName(position));
            removeAtom = this.getAtomByName(AtomTemplate.BB_H.formatAtomName(position));
        }
        if(linkAtom == null) {
            throw new MonosaccharideException("Cannot get linking atom for " + linktype.name() + " linkage at position " + position);
        }
        if(removeAtom == null) {
            throw new MonosaccharideException("Cannot get atom to be removed for " + linktype.name() + " linkage at position " + position);
        }
        return linkAtom;
    }
    
    //*****************************************************************************
    //*** composition related methods: ********************************************
    //*****************************************************************************
    
    public void buildComposition() throws ResourcesDbException {
        this.buildComposition(Config.getGlobalConfig());
    }
    
    public void buildComposition(Config conf) throws ResourcesDbException {
        Composition compo = new Composition();
        //*** 1. set standard basetype composition: CnH2nOn ***
        compo.increaseCount(Periodic.C, this.getSize());
        compo.increaseCount(Periodic.H, 2 * this.getSize());
        compo.increaseCount(Periodic.O, this.getSize());
        //*** 2. add changes caused by core modifications: ***
        for(CoreModification mod : this.getCoreModifications()) {
            if(mod.getTemplate().equals(CoreModificationTemplate.ENX)) {
                throw new MonosaccharideException("Cannot build composition for basetype with enx core modification");
            }
            compo.addComposition(mod.getTemplate().getCompositionChanges());
            if(mod.getTemplate().equals(CoreModificationTemplate.ACID)) {
                if(mod.getIntValuePosition1() == 0) {
                    throw new MonosaccharideException("Cannot build composition for basetype with unknown acid position");
                } else if(this.getRingStart() == 0 && mod.getIntValuePosition1() == 1) {
                    throw new MonosaccharideException("Cannot build composition for basetype with unknown carbonyl position and acid modification at position 1");
                }
                if(mod.getIntValuePosition1() != this.getRingStart()) {
                    compo.decreaseCount(Periodic.H, 2);
                }
                if(conf.isCarboxylGroupsDeprotonated()) {
                    compo.decreaseCount(Periodic.H, 1);
                }
            }
            if(mod.getTemplate().equals(CoreModificationTemplate.LACTONE) && conf.isCarboxylGroupsDeprotonated()) {
                compo.increaseCount(Periodic.H, 1);
            }
            //*** check for keto group at position > 1: if no keto group at position 1 and not alditol, add alditol composition change (C1 of a ketose has the same composition as an alditol C1) ***
            if(mod.getTemplate().equals(CoreModificationTemplate.KETO) && mod.getIntValuePosition1() > 1) {
                if(!this.hasCoreModification(CoreModificationTemplate.KETO, 1) && !this.hasCoreModification(CoreModificationTemplate.ALDITOL, 1)) {
                    compo.addComposition(CoreModificationTemplate.ALDITOL.getCompositionChanges());
                }
            }
        }
        this.setComposition(compo);
    }
    
    //*****************************************************************************
    //*** notation related methods: ***********************************************
    //*****************************************************************************
    
    public void buildName() throws ResourcesDbException {
        GlycoCTExporter exporter = new GlycoCTExporter(GlycanNamescheme.MONOSACCHARIDEDB, this.getConfig(), this.getTemplateContainer());
        Monosaccharide ms = new Monosaccharide(this);
        this.setName(exporter.export(ms));
    }
    
    //*****************************************************************************
    //*** methods related to BuilderGroups: ***************************************
    //*****************************************************************************
    
    /**
     * Set the stereo and modification properties of a Basetype using an extended Stereocode String.
     * @param extStereoStr the stereostring to parse
     * @param bt the basetype, the properties of which are to be set. Size and ring properties of bt have to be set before calling this method!
     * @throws ResourcesDbException in case an the extStereoStr parameter cannot be parsed or does not match the size or ring properties of the basetye
     */
    public static void buildBasetypeByExtendedStereocode(String extStereoStr, Basetype bt) throws ResourcesDbException {
        Utils.setTemplateDataIfNotSet(Config.getGlobalConfig());
        if(bt == null) {
            throw new MonosaccharideException("Basetype must not be null in buildBasetypeByExtendedStereocode().");
        }
        if(extStereoStr == null) {
            throw new MonosaccharideException("StereoString must not be null in buildBasetypeByExtendedStereocode().");
        }
        if(extStereoStr.length() != bt.getSize()) {
            throw new MonosaccharideException("StereoString length (" + extStereoStr.length() + ") doesn't match basetype size (" + bt.size + ")");
        }
        //*** translate extended stereocode into BasetypeBuilderGroups: ***
        ArrayList<BasetypeBuilderGroup> groupList = new ArrayList<BasetypeBuilderGroup>(bt.getSize());
        for(int pos = 1; pos <= bt.getSize(); pos ++) {
            char posSymbol = extStereoStr.charAt(pos - 1);
            BasetypeBuilderGroup buildergroup = BasetypeBuilderGroup.forExtStereoSymbol(posSymbol);
            if(buildergroup == null) {
                throw new MonosaccharideException("Unknown stereocode symbol: '" + posSymbol + "'");
            }
            //*** check, if builder group is valid at current position: ***
            if(pos == 1 && bt.getRingStart() != 1) {
                if(!buildergroup.isHeadTail()) {
                    throw new MonosaccharideException("Stereocode symbol '" + posSymbol + "' is not allowed at position " + pos + " (only valid for non-terminal exocyclic positions)");
                }
            } else if(pos == bt.getSize() && bt.getRingEnd() < pos) {
                if(!buildergroup.isHeadTail()) {
                    throw new MonosaccharideException("Stereocode symbol '" + posSymbol + "' is not allowed at position " + pos + " (only valid for non-terminal exocyclic positions)");
                }
            } else if(buildergroup.isHeadTail() && pos < bt.getSize() && pos > 1) {
                throw new MonosaccharideException("Stereocode symbol '" + posSymbol + "' is not allowed at position " + pos + " (only valid for terminal positions)");
            }
            groupList.add(buildergroup);
        }
        //*** build basetype from BasetypeBuilderGroups: ***
        String stereo = "";
        for(int pos = 1; pos <= bt.getSize(); pos ++) {
            BasetypeBuilderGroup buildergroup = groupList.get(pos - 1);
            
            //*** check for explicit carbonyl group at ring start: ***
            if(pos == bt.getRingStart()) {
                if(buildergroup.equals(BasetypeBuilderGroup.KETO)) {
                    throw new MonosaccharideException("KETO modification not allowed at ring start position. Select D or L configuration to indicate orientation of OH group at anomeric center.");
                }
                if(buildergroup.equals(BasetypeBuilderGroup.CHO)) {
                    throw new MonosaccharideException("Carbonyl group not allowed at ring start position. Select D or L configuration to indicate orientation of OH group at anomeric center.");
                }
            }
            
            //*** check for alditol: ***
            if(pos == 1) {
                if(buildergroup.equals(BasetypeBuilderGroup.CH3) || buildergroup.equals(BasetypeBuilderGroup.H2COH)) {
                    if(bt.getRingEnd() == Basetype.OPEN_CHAIN) {
                        if(!BasetypeBuilderGroup.hasCoreModification(groupList, CoreModificationTemplate.KETO)) {
                            bt.addCoreModification(CoreModificationTemplate.ALDITOL, 1);
                        }
                    }
                }
            }
            
            //*** build position: ***
            stereo += buildergroup.getStereoSymbol();
            for(CoreModificationTemplate modTmpl : buildergroup.getCoreMods()) {
                if(modTmpl.equals(CoreModificationTemplate.EN)) {
                    CoreModification existEn = bt.getCoreModification(CoreModificationTemplate.EN, pos -1);
                    if(existEn != null && existEn.getIntValuePosition1() == pos - 1) {
                        continue; //*** current position is second position of an en modification, mod already added at previous position ***
                    }
                    if(pos < bt.getSize()) {
                        char nextPosSymbol = extStereoStr.charAt(pos);
                        BasetypeBuilderGroup nextPosGroup = BasetypeBuilderGroup.forExtStereoSymbol(nextPosSymbol);
                        if(nextPosGroup.getCoreMods() != null && nextPosGroup.getCoreMods().contains(CoreModificationTemplate.EN)) {
                            CoreModification mod = new CoreModification(modTmpl, pos, pos + 1);
                            bt.addCoreModification(mod);
                            continue;
                        }
                    }
                    throw new MonosaccharideException("Position " + pos + " is single EN modification position (must be followed or preceeded by another EN modification position)");
                } else if(modTmpl.equals(CoreModificationTemplate.YN)) {
                    CoreModification existYn = bt.getCoreModification(CoreModificationTemplate.YN, pos -1);
                    if(existYn != null && existYn.getIntValuePosition1() == pos - 1) {
                        continue; //*** current position is second position of an yn modification, mod already added at previous position ***
                    }
                    if(pos < bt.getSize()) {
                        char nextPosSymbol = extStereoStr.charAt(pos);
                        BasetypeBuilderGroup nextPosGroup = BasetypeBuilderGroup.forExtStereoSymbol(nextPosSymbol);
                        if(nextPosGroup.getCoreMods() != null && nextPosGroup.getCoreMods().contains(CoreModificationTemplate.YN)) {
                            CoreModification mod = new CoreModification(modTmpl, pos, pos + 1);
                            bt.addCoreModification(mod);
                            continue;
                        }
                    }
                    throw new MonosaccharideException("Position " + pos + " is single EN modification position (must be followed or preceeded by another EN modification position)");
                } else {
                    if(modTmpl.equals(CoreModificationTemplate.KETO) && pos > bt.getRingStart() && bt.getRingStart() > 0) {
                        if(!bt.hasCoreModification(CoreModificationTemplate.KETO, bt.getRingStart())) {
                            bt.addCoreModification(CoreModificationTemplate.KETO, bt.getRingStart());
                        }
                    }
                    bt.addCoreModification(modTmpl, pos);
                }
            }
        }
        bt.setStereoStr(stereo);
        bt.setConfiguration(Stereocode.getConfigurationFromStereoString(stereo));
        bt.setAnomer(bt.getAnomerFromStereocode());
        if(bt.getRingStart() > 1) {
            if(!bt.hasCoreModification(CoreModificationTemplate.KETO, bt.getRingStart())) {
                bt.addCoreModification(new CoreModification(CoreModificationTemplate.KETO, bt.getRingStart()));
            }
        } else {
            if(bt.hasCoreModification(CoreModificationTemplate.KETO, 1)) {
                if(bt.countCoreModifications(CoreModificationTemplate.KETO) == 1) {
                    bt.deleteCoreModification(CoreModificationTemplate.KETO, 1);
                }
            }
        }
        bt.buildName();
    }
    
    /**
     * Set the stereo and modification properties of this Basetype using an extended Stereocode String.
     * @param extStereoStr the stereostring to parse
     * @param size the Basetype size
     * @param ringStart the carbonyl position
     * @param ringEnd the position of the ring oxygen
     * @throws ResourcesDbException
     */
    public void buildByExtendedStereocode(String extStereoStr, int size, int ringStart, int ringEnd) throws ResourcesDbException {
        this.init();
        this.setSize(size);
        this.setRingStart(ringStart);
        this.setRingEnd(ringEnd);
        Basetype.buildBasetypeByExtendedStereocode(extStereoStr, this);
    }
    
    public ArrayList<BasetypeBuilderGroup> toBuilderGroups() throws ResourcesDbException {
        ArrayList<BasetypeBuilderGroup> outList = new ArrayList<BasetypeBuilderGroup>();
        for(int i = 1; i <= this.getSize(); i++) {
            StereoConfiguration posConf = this.getStereoConfigurationByPosition(i);
            if(posConf.equals(StereoConfiguration.Dexter) || posConf.equals(StereoConfiguration.XDexter)) {
                outList.add(BasetypeBuilderGroup.HCOH_D);
                continue;
            }
            if(posConf.equals(StereoConfiguration.Laevus) || posConf.equals(StereoConfiguration.XLaevus)) {
                outList.add(BasetypeBuilderGroup.HCOH_L);
                continue;
            }
            Boolean headTailFlag = false;
            if((i == 1 && i != this.getRingStart()) || i == this.getSize()) {
                headTailFlag = true;
            }
            ArrayList<CoreModificationTemplate> modList = this.getCoreModificationTemplatesByPosition(i);
            modList.remove(CoreModificationTemplate.ALDITOL);
            BasetypeBuilderGroup bbgroup = BasetypeBuilderGroup.forCoreModifications(modList, headTailFlag);
            if(bbgroup == null) {
                bbgroup = BasetypeBuilderGroup.UNKNOWN;
            }
            outList.add(bbgroup);
        }
        return outList;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    /**
     * Initialize the properties of this Basetype
     */
    public void init() {
        this.setStereocode(new Stereocode());
        this.setSize(0);
        this.setRingStart(Basetype.UNKNOWN_RING);
        this.setRingEnd(Basetype.UNKNOWN_RING);
        this.setDefaultCarbonylPosition(Basetype.UNKNOWN_RING);
        this.coreModifications = new ArrayList<CoreModification>();
        this.setAnomer((Anomer) null);
        this.setConfiguration((StereoConfiguration) null);
        this.setName(null);
        this.setComposition(null);
        this.setMonoMass(null);
        this.setAvgMass(null);
        this.setSuperclass(null);
        this.setIsSuperclassFlag(null);
        this.setInchi(null);
        this.setSmiles(null);
        this.setDbId(0);
    }
    
    public String toString() {
        String outStr = "Basetype: ";
        outStr += "[Name: " + getName() + "; ";
        String anomerStr = "null";
        if(getAnomer() != null) {
            anomerStr = getAnomer().getSymbol();
        }
        String stereocodeStr = "null";
        if(getStereocode() != null) {
            stereocodeStr = getStereocode().getStereoStr();
        }
        String configStr = "null";
        if(getConfiguration() != null) {
            configStr = getConfiguration().getSymbol();
        }
        String modStr = "";
        if(getCoreModifications() != null) {
            for(int i = 0; i < getCoreModifications().size(); i++) {
                modStr += getCoreModifications().get(i).toString();
            }
        }
        outStr += "Anomer: " + anomerStr + "; ";
        outStr += "Stereocode: " + stereocodeStr + "; ";
        outStr += "Configuration: " + configStr + "; ";
        outStr += "Ring atoms: " + getRingStart() + "/" + getRingEnd() + "; ";
        outStr += "Modifications: [" + modStr + "]";
        outStr += "]";
        return(outStr);
    }
}
