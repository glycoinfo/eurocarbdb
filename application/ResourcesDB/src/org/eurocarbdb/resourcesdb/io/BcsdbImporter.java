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
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.template.BasetypeTemplate;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;
import org.eurocarbdb.resourcesdb.template.TrivialnameTemplate;
import org.eurocarbdb.resourcesdb.util.StringUtils;

public class BcsdbImporter extends StandardImporter implements MonosaccharideImporter {

    private BasetypeTemplate detectedSuperclass = null;
    private ArrayList<BasetypeTemplate> btList = new ArrayList<BasetypeTemplate>();
    private Ringtype detectedRingtype = null;
    private String configurationSymbolsStr = "";
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public BcsdbImporter(Config confObj) {
        this(null, null);
    }
    
    public BcsdbImporter(Config confObj, TemplateContainer container) {
        super(GlycanNamescheme.BCSDB, confObj, container);
    }
    
    public BcsdbImporter() {
        this(null, null);
    }

    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************

    /**
     * @return the detectedSuperclass
     */
    public BasetypeTemplate getDetectedSuperclass() {
        return this.detectedSuperclass;
    }

    /**
     * @param detectedSuperclass the detectedSuperclass to set
     */
    public void setDetectedSuperclass(BasetypeTemplate detectedSuperclass) {
        this.detectedSuperclass = detectedSuperclass;
    }

    public ArrayList<BasetypeTemplate> getBtList() {
        return this.btList;
    }
    
    public void addBasetypeToBtList(BasetypeTemplate tmpl) {
        this.btList.add(tmpl);
    }

    public String getConfigurationSymbolsStr() {
        return this.configurationSymbolsStr;
    }

    private void setConfigurationSymbolsStr(String confSymbolsStr) {
        this.configurationSymbolsStr = confSymbolsStr;
    }
    
    private void addConfigurationSymbol(char confSym) {
        this.configurationSymbolsStr += confSym;
    }

    public Ringtype getDetectedRingtype() {
        return detectedRingtype;
    }

    public void setDetectedRingtype(Ringtype detectedRingtype) {
        this.detectedRingtype = detectedRingtype;
    }

    //*****************************************************************************
    //*** parsing methods: ********************************************************
    //*****************************************************************************

    public Monosaccharide parseMsString(String name) throws ResourcesDbException {
        Monosaccharide ms = new Monosaccharide(this.getConfig(), this.getTemplateContainer());
        this.parseMsString(name, ms);
        return ms;
    }
    
    public void parseMsString(String name, Monosaccharide ms) throws ResourcesDbException {
        if(ms == null) {
            throw new NameParsingException("BcsdbImporter.parseMsString(String, Monosaccharide): Monosaccharide must not be null.");
        }
        if(name == null) {
            throw new NameParsingException("BcsdbImporter.parseMsString(String, Monosaccharide): Name must not be null.");
        }
        if(name.length()==0) {
            throw new NameParsingException("BcsdbImporter.parseMsString(String, Monosaccharide): Name must not be empty.");
        }
        ms.init();
        ms.setCheckPositionsOnTheFly(false);
        this.setInputName(name);
        this.setFoundMs(false);
        this.setParsingPosition(0);
        ArrayList<CoreModification> coreModList = new ArrayList<CoreModification>();
        TrivialnameTemplate trivTmpl = null;
        BasetypeTemplate btTmpl = null;
        //ArrayList<BasetypeTemplate> btTmplList = new ArrayList<BasetypeTemplate>();
        
        //*** get anomer: ***
        Anomer anomer = Anomer.forBcsdbSymbol(this.getCurrentToken());
        if(anomer == null) {
            if(!isBcsdbConfigSymbol(this.getCurrentToken())) {
                throw new NameParsingException("illegal anomer symbol: '" + this.getCurrentToken() + "'", name, 0);
            }
        } else {
            ms.setAnomer(anomer);
            this.increaseParsingPosition();
        }
        
        //*** get absolute configuration symbol(s): ***
        while(isBcsdbConfigSymbol(this.getCurrentToken())) {
            //*** check, if substring starting at current position is a basetype name or trivialname, the first letter of which could be misinterpreted as a configuration symbol (as e.g. in DLyx, wich has to be read as D + Lyx and not DL + yx) ***
            if(name.length() - this.getParsingPosition() >= 3) {
                if(this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeTemplateByName(name.substring(this.getParsingPosition(), this.getParsingPosition() + 3)) != null) {
                    break;
                }
            }
            String trivName = this.checkForTrivialname();
            if(trivName != null) {
                trivTmpl = this.getTemplateContainer().getTrivialnameTemplateContainer().forBasetypeName(this.getNamescheme(), trivName);
                this.increaseParsingPosition(trivName.length());
                break;
            }
            this.addConfigurationSymbol(this.getCurrentToken());
            this.increaseParsingPosition();
        }
        
        if(trivTmpl == null) {
            //*** check for "pre-basetype" core modifications (deoxy-/anhydro): ***
            while(Character.isDigit(this.getCurrentToken())) {
                //*** check first, if residue contains a trivial name starting with a digit (such as 4eLeg) at current position: ***
                String trivName = this.checkForTrivialname();
                if(trivName != null) {
                    trivTmpl = this.getTemplateContainer().getTrivialnameTemplateContainer().forBasetypeName(this.getNamescheme(), trivName);
                    this.increaseParsingPosition(trivName.length());
                    break;
                }
                
                //*** now check for the core modifications: ***
                ArrayList<Integer> modPosList = new ArrayList<Integer>();
                int pos = this.parseIntNumber();
                modPosList.add(pos);
                while(this.getCurrentToken() == ',') {
                    this.increaseParsingPosition();
                    pos = this.parseIntNumber();
                    modPosList.add(pos);
                }
                String deoxyName = CoreModificationTemplate.DEOXY.getBcsdbName();
                String anhydroName = CoreModificationTemplate.ANHYDRO.getBcsdbName();
                if(this.getCurrentSubstring(deoxyName.length()).equals(deoxyName)) {
                    for(Integer dPos : modPosList) {
                        CoreModification mod = new CoreModification(CoreModificationTemplate.DEOXY, dPos);
                        coreModList.add(mod);
                    }
                    this.increaseParsingPosition();
                } else if(this.getCurrentSubstring(anhydroName.length()).equals(anhydroName)) {
                    if(modPosList.size() != 2) {
                        throw new NameParsingException("anhydro modification requires two positions", this.getInputName(), this.getParsingPosition());
                    }
                    CoreModification mod = new CoreModification(CoreModificationTemplate.ANHYDRO, modPosList.get(0), modPosList.get(1));
                    coreModList.add(mod);
                    this.increaseParsingPosition(3);
                } else {
                    throw new NameParsingException("invalid modification name", this.getInputName(), this.getParsingPosition());
                }
            }
            //*** check again for trivial name: ***
            String trivName = this.checkForTrivialname();
            if(trivName != null) {
                trivTmpl = this.getTemplateContainer().getTrivialnameTemplateContainer().forBasetypeName(this.getNamescheme(), trivName);
                this.increaseParsingPosition(trivName.length());
            }
        }
        if(trivTmpl == null) {
            //*** get basetype: ***
            String btName = this.getCurrentSubstring(3);
            if(btName.equalsIgnoreCase("thr")) {
                btName = "tro";
            }
            btTmpl = this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeTemplateByName(btName);
            if(btTmpl == null) {
                throw new NameParsingException("basetype name expected", this.getInputName(), this.getParsingPosition());
            }
            if(btTmpl.isSuperclass()) {
                if(this.getDetectedSuperclass() != null) {
                    throw new NameParsingException("multiple basetype superclasses found", this.getInputName(), this.getParsingPosition());
                }
            } else {
                this.addBasetypeToBtList(btTmpl);
            }
            this.increaseParsingPosition(3);
            //*** check for further basetype definitions, as in 4dxylHex: ***
            while(this.countRemainingTokens() >= 3) {
                btName = this.getCurrentSubstring(3);
                if(btName.equalsIgnoreCase("thr")) {
                    btName = "tro";
                }
                btTmpl = this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeTemplateByName(btName);
                if(btTmpl == null) {
                    break;
                }
                if(btTmpl.isSuperclass()) {
                    if(this.getDetectedSuperclass() != null) {
                        throw new NameParsingException("multiple basetype superclasses found", this.getInputName(), this.getParsingPosition());
                    }
                    this.setDetectedSuperclass(btTmpl);
                } else {
                    this.addBasetypeToBtList(btTmpl);
                }
                this.increaseParsingPosition(3);
            }
            if(this.getDetectedSuperclass() != null) {
                ms.setSize(this.getDetectedSuperclass().getSize());
            } else {
                if(this.getBtList().size() == 1) {
                    ms.setSize(this.getBtList().get(0).getSize());
                }
            }
        } else {
            ms.setSize(trivTmpl.getSize());
            /*for(CoreModification mod : trivTmpl.getCoreModifications()) {
                ms.addCoreModification(mod);
            }
            for(Substitution subst : trivTmpl.getSubstitutions()) {
                ms.addSubstitution(subst);
            }*/
            this.setDetectedTrivialname(trivTmpl);
        }

        //*** get ring type: ***
        if(this.hasCurrentToken()) {
            if(isBcsdbRingtypeSymbol(this.getCurrentToken())) {
                this.setDetectedRingtype(Ringtype.forBcsdbSymbol(String.valueOf(this.getCurrentToken())));
                this.increaseParsingPosition();
            }
        }
        
        //*** add previously detected core modifications: ***
        for(CoreModification mod : coreModList) {
            ms.addCoreModification(mod);
        }
        
        //*** check for "post-basetype" core modifications / substitutions (including alditol or acid): ***
        this.parseModifications(ms);
        
        //*** build the monosaccharide from the parsed information: ***
        this.processParsedData(ms);
    }
    
    private void parseModifications(Monosaccharide ms) throws ResourcesDbException {
        while(this.hasCurrentToken()) {
            if(this.getCurrentToken() == '-') {
                this.increaseParsingPosition();
            }
            ArrayList<Integer> digitList = null;
            if(Character.isDigit(this.getCurrentToken())) {
                digitList = this.parseIntNumberList();
                if(this.getCurrentToken() == '-') {
                    this.increaseParsingPosition();
                }
            }
            if(this.hasCurrentSubstring("en")) {
                if(digitList == null) {
                    throw new NameParsingException("number expected", this.getInputName(), this.getParsingPosition());
                }
                for(Integer pos1 : digitList) {
                    CoreModification mod = new CoreModification(CoreModificationTemplate.EN, pos1, pos1 + 1);
                    ms.addCoreModification(mod);
                }
                this.increaseParsingPosition(2);
                continue;
            }
            if(this.hasCurrentSubstring("ulo")) {
                if(digitList == null) {
                    digitList = new ArrayList<Integer>();
                    digitList.add(2); //*** default keto position ***
                }
                for(Integer pos1 : digitList) {
                    CoreModification mod = new CoreModification(CoreModificationTemplate.KETO, pos1);
                    ms.addCoreModification(mod);
                }
                this.increaseParsingPosition(3);
                //*** check, if residue is ulosonic or ulosaric: ***
                if(this.hasCurrentSubstring("sonic")) {
                    ms.setAldonic();
                    this.increaseParsingPosition(5);
                } else if(this.hasCurrentSubstring("saric")) {
                    ms.setAldaric();
                    this.increaseParsingPosition(5);
                }
                continue;
            }
            if(this.hasCurrentSubstring("onic")) {
                if(digitList != null) {
                    throw new NameParsingException("Cannot assign given position to 'onic' modification", this.getInputName(), this.getParsingPosition());
                }
                ms.setAldonic();
                this.increaseParsingPosition(4);
                continue;
            } else if(this.hasCurrentSubstring("aric")) {
                if(digitList != null) {
                    throw new NameParsingException("Cannot assign given position to 'aric' modification", this.getInputName(), this.getParsingPosition());
                }
                ms.setAldaric();
                this.increaseParsingPosition(4);
                continue;
            }
            String matchedSubstName = "";
            for(String substName : this.getTemplateContainer().getSubstituentTemplateContainer().getResidueIncludedNameList(this.getNamescheme())) {
                if(this.hasCurrentSubstring(substName)) {
                    if(substName.length() > matchedSubstName.length()) {
                        matchedSubstName = substName;
                    }
                }
            }
            if(matchedSubstName.length() > 0) { //*** substitution found ***
                if(digitList == null) {
                    digitList = new ArrayList<Integer>();
                    digitList.add(2);
                }
                this.addParsedSubstitution(ms, matchedSubstName, digitList);
                this.increaseParsingPosition(matchedSubstName.length());
                continue;
            }
            if(this.getCurrentToken() == 'A') {
                if(digitList != null) {
                    throw new NameParsingException("Cannot assign given position to 'A' modification", this.getInputName(), this.getParsingPosition());
                }
                ms.setUronic();
                this.increaseParsingPosition();
                continue;
            }
            if(this.hasCurrentSubstring("ol")) {
                if(digitList != null) {
                    throw new NameParsingException("Cannot assign given position to 'ol' modification", this.getInputName(), this.getParsingPosition());
                }
                ms.setAlditol(true);
                this.increaseParsingPosition(2);
                continue;
            }
            throw new NameParsingException("cannot parse substring '" + this.getInputName().substring(this.getParsingPosition()) + "'", this.getInputName(), this.getParsingPosition());
        }
    }
    
    private void processParsedData(Monosaccharide ms) throws ResourcesDbException {
        boolean expandChiralonly = false;
        if(this.getDetectedTrivialname() != null) {
            //*** set monosaccharide properties defined by the trivialname: ***
            ms.init(this.getDetectedTrivialname());
            //*** prepare stereocode: ***
            String stereo = this.getDetectedTrivialname().getStereocode();
            if(this.getDetectedTrivialname().isDefaultConfigIsCompulsory()) {
                if(!this.getConfigurationSymbolsStr().equals(String.valueOf(StereoConfiguration.Nonchiral.getBcsdbSymbol()))) {
                    if(!this.getConfigurationSymbolsStr().equals(String.valueOf(this.getDetectedTrivialname().getDefaultConfiguration().getBcsdbSymbol()))) {
                        if(!this.getConfigurationSymbolsStr().equals("")) {
                            throw new NameParsingException("trivial name " + this.getDetectedTrivialname().getLongName() + " requires absolute configuration " + StereoConfiguration.Nonchiral.getBcsdbSymbol());
                        }
                    }
                }
                if(this.getDetectedTrivialname().getDefaultConfiguration().equals(StereoConfiguration.Laevus)) {
                    stereo = Stereocode.changeDLinStereoString(stereo);
                }
            } else {
                if(this.getConfigurationSymbolsStr().equals(String.valueOf(StereoConfiguration.Unknown.getBcsdbSymbol()))) {
                    stereo = Stereocode.absoluteToRelative(stereo);
                } else if(this.getConfigurationSymbolsStr().equals(String.valueOf(StereoConfiguration.Laevus.getBcsdbSymbol()))) {
                    stereo = Stereocode.changeDLinStereoString(stereo);
                }
            }
            //stereo = StereoConfiguration.Nonchiral.getStereosymbol() + stereo + StereoConfiguration.Nonchiral.getStereosymbol();
            ms.setDefaultCarbonylPosition(this.getDetectedTrivialname().getCarbonylPosition());
            //ms.setStereoStr(stereo);
            this.setTmpStereocode(stereo);
        } else { //*** no trivial name ***
            //*** get monosaccharide size and stereocode from basetypelist: ***
            if(this.getDetectedSuperclass() == null) {
                if(this.getBtList().size() > 1) {
                    throw new NameParsingException("multiple basetypes but no superclass found", this.getInputName(), 0);
                }
                if(this.getBtList().size() == 0) {
                    throw new NameParsingException("no monosaccharide basetype detected", this.getInputName(), 0);
                }
                ms.setSize(this.getBtList().get(0).getSize());
                //*** prepare stereocode: ***
                String stereo = this.getBtList().get(0).getStereocode();
                if(this.getConfigurationSymbolsStr().length() == 0 || this.getConfigurationSymbolsStr().equals(String.valueOf(StereoConfiguration.Unknown.getBcsdbSymbol()))) {
                    stereo = Stereocode.absoluteToRelative(stereo);
                } else if(this.getConfigurationSymbolsStr().equals(String.valueOf(StereoConfiguration.Laevus.getBcsdbSymbol()))) {
                    stereo = Stereocode.changeDLinStereoString(stereo);
                } else if(!this.getConfigurationSymbolsStr().equals(String.valueOf(StereoConfiguration.Dexter.getBcsdbSymbol()))) {
                    throw new NameParsingException("cannot apply stereoconfiguration " + this.getConfigurationSymbolsStr() + " to basetype " + this.getBtList().get(0).getBaseName());
                }
                this.setTmpStereocode(stereo);
            } else { //*** main basetype is superclass (as in 4dxylHex) ***
                expandChiralonly = true;
                ms.setSize(this.getDetectedSuperclass().getSize());
                //*** prepare stereocode: ***
                String stereo = "";
                if(this.getConfigurationSymbolsStr().length() > 2) {
                    if(this.getConfigurationSymbolsStr().charAt(0) == StereoConfiguration.Nonchiral.getBcsdbSymbol()) {
                        //*** delete leading X in config string of a residue like aXDDmanHep: ***
                        this.setConfigurationSymbolsStr(this.getConfigurationSymbolsStr().substring(1));
                    }
                }
                if(this.getBtList().size() == 0) { //*** residue is superclass without stereo information ***
                    stereo = StringUtils.multiplyChar(StereoConfiguration.Unknown.getStereosymbol(), ms.getSize() - 2);
                    expandChiralonly = false;
                } else {
                    if(this.getConfigurationSymbolsStr().length() == this.getBtList().size() + 1) {
                        stereo += StereoConfiguration.forBcsdbSymbol(this.getConfigurationSymbolsStr().charAt(0)).getStereosymbol();
                        this.setConfigurationSymbolsStr(this.getConfigurationSymbolsStr().substring(1));
                    }
                    if(this.getConfigurationSymbolsStr().length() != this.getBtList().size()) {
                        throw new NameParsingException("number of configuration symbols does not match number of basetypes", this.getInputName(), 0);
                    }
                    for(int i = 0; i < this.getBtList().size(); i++) {
                        String tmpStereo = this.getBtList().get(i).getStereocode();
                        if(this.getConfigurationSymbolsStr().charAt(i) == StereoConfiguration.Laevus.getBcsdbSymbol()) {
                            tmpStereo = Stereocode.changeDLinStereoString(tmpStereo);
                        } else if(this.getConfigurationSymbolsStr().charAt(i) == StereoConfiguration.Unknown.getBcsdbSymbol()) {
                            tmpStereo = Stereocode.absoluteToRelative(tmpStereo);
                        }
                        stereo = tmpStereo + stereo;
                    }
                    this.setTmpStereocode(stereo);
                }
            }
            //*** set default ring start: ***
            ArrayList<CoreModification> ketoList = ms.getCoreModifications(CoreModificationTemplate.KETO);
            if(ketoList != null && ketoList.size() > 0) {
                for(CoreModification mod : ketoList) {
                    if(mod.getIntValuePosition1() > 0) {
                        if(ms.getDefaultCarbonylPosition() == 0) {
                            ms.setDefaultCarbonylPosition(mod.getIntValuePosition1());
                        } else if(mod.getIntValuePosition1() < ms.getDefaultCarbonylPosition()) {
                            ms.setDefaultCarbonylPosition(mod.getIntValuePosition1());
                        }
                    }
                }
            } else {
                ms.setDefaultCarbonylPosition(1);
            }
        }
        
        //*** set ring: ***
        if(ms.getRingStart() == Basetype.UNKNOWN_RING) {
            if(ms.isAlditol()) {
                ms.setRingStart(Basetype.OPEN_CHAIN);
            } else {
                ms.setRingStart(ms.getDefaultCarbonylPosition());
            }
        }
        if(this.getDetectedRingtype() != null) {
            if(ms.isAlditol()) {
                if(this.getDetectedRingtype().equals(Ringtype.PYRANOSE) || this.getDetectedRingtype().equals(Ringtype.FURANOSE)) {
                    throw new MonosaccharideException("Ringtype " + this.getDetectedRingtype().getName() + " is not allowed together with alditol modification.");
                }
            } else {
                ms.setRingtype(this.getDetectedRingtype());
            }
        }
        //*** set correct anomer: ***
        if(!(Anomer.ALPHA.equals(ms.getAnomer()) || Anomer.BETA.equals(ms.getAnomer()))) {
            if(ms.getRingStart() > 0) {
                if(ms.isStereolossPositionWithIgnoreType(ms.getRingStart(), CoreModificationTemplate.KETO)) {
                    ms.setAnomer(Anomer.NONE);
                } else {
                    ms.setAnomer(Anomer.UNKNOWN);
                }
            } else if(ms.getRingStart() == Basetype.OPEN_CHAIN) {
                ms.setAnomer(Anomer.OPEN_CHAIN);
            } else {
                ms.setAnomer(Anomer.UNKNOWN);
            }
        }
        //*** finish stereocode: ***
        String stereo = this.getTmpStereocode();
        if(expandChiralonly) {
            stereo = Stereocode.expandChiralonlyStereoString(stereo, ms);
        } else {
            stereo = StereoConfiguration.Nonchiral.getStereosymbol() + this.getTmpStereocode() + StereoConfiguration.Nonchiral.getStereosymbol();
            stereo = Stereocode.markNonchiralPositionsInStereoString(stereo, ms);
        }
        ms.setStereoStr(stereo);
        ms.setAnomerInStereocode();
        
        //*** check correctness and build ms name: ***
        MonosaccharideValidation.checkMonosaccharideConsistency(ms, this.getTemplateContainer(), this.getConfig());
        ms.buildName();
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************

    /**
     * Check, if a given character is a valid StereoConfiguration symbol in bcsdb notation
     * @param symbol
     * @return
     */
    public static boolean isBcsdbConfigSymbol(char symbol) {
        return StereoConfiguration.forBcsdbSymbol(symbol) != null;
    }
    
    /**
     * Check, if a given character is a valid Ringtype symbol in bcsdb notation
     * @param symbol
     * @return
     */
    public static boolean isBcsdbRingtypeSymbol(char symbol) {
        return Ringtype.forBcsdbSymbol(String.valueOf(symbol)) != null;
    }
    
    public void init() {
        super.init();
        this.setDetectedRingtype(null);
        this.setDetectedSuperclass(null);
        this.setConfigurationSymbolsStr("");
        this.btList.clear();
    }
    
}
