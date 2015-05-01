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
import org.eurocarbdb.resourcesdb.template.BasetypeTemplate;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;
import org.eurocarbdb.resourcesdb.template.TrivialnameTemplate;
import org.eurocarbdb.resourcesdb.util.NumberPrefix;
import org.eurocarbdb.resourcesdb.util.StringUtils;

/**
* Importer class for monosaccharides encoded in CarbBank style formats (CarbBank, Glycosciences.de, Iupac)
* @author Thomas Luetteke
*
*/
public class CarbbankImporter extends StandardImporter implements MonosaccharideImporter {
    
    private String prePosStr;
    private String postPosStr;
    
    private BasetypeTemplate detectedBasetype;
    private BasetypeTemplate detectedSubtype;
    private Ringtype detectedRingtype;
    
    private int fuzzy;
    private int preposWildcards;
    
    private boolean useDefaultValues = false;
    
    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public CarbbankImporter() {
        this(GlycanNamescheme.CARBBANK, null, null);
    }

    public CarbbankImporter(GlycanNamescheme scheme, Config confObj) {
        this(scheme, confObj, null);
    }
    
    public CarbbankImporter(GlycanNamescheme scheme, Config confObj, TemplateContainer container) {
        super(scheme, confObj, container);
        this.init();
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************

    /**
     * @return the postPosStr
     */
    private String getPostPosStr() {
        return this.postPosStr;
    }

    /**
     * @param postPosStr the postPosStr to set
     */
    private void setPostPosStr(String postPosStr) {
        this.postPosStr = postPosStr;
    }

    /**
     * @return the prePosStr
     */
    private String getPrePosStr() {
        return this.prePosStr;
    }

    /**
     * @param prePosStr the prePosStr to set
     */
    private void setPrePosStr(String prePosStr) {
        this.prePosStr = prePosStr;
    }
    
    /**
     * @return the detectedBasetype
     */
    public BasetypeTemplate getDetectedBasetype() {
        return this.detectedBasetype;
    }

    /**
     * @param detectedBasetype the detectedBasetype to set
     */
    public void setDetectedBasetype(BasetypeTemplate detectedBasetype) {
        this.detectedBasetype = detectedBasetype;
    }

    /**
     * @return the detectedSubtype
     */
    public BasetypeTemplate getDetectedSubtype() {
        return this.detectedSubtype;
    }

    /**
     * @param detectedSubtype the detectedSubtype to set
     */
    public void setDetectedSubtype(BasetypeTemplate detectedSubtype) {
        this.detectedSubtype = detectedSubtype;
    }

    /**
     * @return the useDefaultValues
     */
    public boolean isUseDefaultValues() {
        return this.useDefaultValues;
    }

    /**
     * @param useDefaultValues the useDefaultValues to set
     */
    public void setUseDefaultValues(boolean useDefaultValues) {
        this.useDefaultValues = useDefaultValues;
    }

    /**
     * @return the preposWildcards
     */
    private int getPreposWildcards() {
        return this.preposWildcards;
    }

    /**
     * @param preposWildcards the preposWildcards to set
     */
    private void setPreposWildcards(int preposWildcards) {
        this.preposWildcards = preposWildcards;
    }
    
    private void increasePreposWildcards() {
        setPreposWildcards(getPreposWildcards() + 1);
    }
    
    private void decreasePreposWildcards() {
        setPreposWildcards(getPreposWildcards() - 1);
    }

    /**
     * @return the fuzzy
     */
    private int getFuzzy() {
        return this.fuzzy;
    }

    /**
     * @param fuzzy the fuzzy to set
     */
    private void setFuzzy(int fuzzy) {
        this.fuzzy = fuzzy;
    }
    
    private void addFuzzy() {
        setFuzzy(getFuzzy() + 1);
    }
    
    public boolean isFuzzy() {
        return(getFuzzy() > 0);
    }

    //*****************************************************************************
    //*** parsing methods: ********************************************************
    //*****************************************************************************

    public Monosaccharide parseMsString(String name) throws ResourcesDbException {
        Monosaccharide ms = new Monosaccharide(this.getConfig(), this.getTemplateContainer());
        this.parseMsString(name, ms);
        return(ms);
    }
    
    public void parseMsString(String name, Monosaccharide ms) throws ResourcesDbException {
        if(ms == null) {
            throw new NameParsingException("CarbbankImporter.parseMsString(String, Monosaccharide): Monosaccharide must not be null.");
        }
        ms.init();
        ms.setCheckPositionsOnTheFly(false);
        this.setInputName(name);
        this.setFoundMs(false);
        ArrayList<String> basetypesSuperclassList = this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeListSuperclass();
        ArrayList<String> basetypesSpecificList = this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeListSpecific();
        String nameLowercase = name.toLowerCase();
        
        //*** search for superclass basetype (like hex, hep, pen, ...): ***
        for(int i = 0; i < basetypesSuperclassList.size(); i++) {
            int basepos = nameLowercase.indexOf(basetypesSuperclassList.get(i));
            if(basepos  != -1) {
                this.setPrePosStr(name.substring(0, basepos));
                this.setPostPosStr(name.substring(basepos + basetypesSuperclassList.get(i).length()));
                BasetypeTemplate basetype = this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeTemplateByName(basetypesSuperclassList.get(i));
                this.setDetectedBasetype(basetype);
                ms.setSize(basetype.getSize());
                ms.setDefaultCarbonylPosition(basetype.getCarbonylPosition());
                if(getPrePosStr().length() >= 3) {
                    String subtypeStr = getPrePosStr().substring(getPrePosStr().length() - 3, getPrePosStr().length()).toLowerCase();
                    if(subtypeStr.equals("thr")) { //*** for threose subtype usually "thr" is used instead of "tro" ***
                        subtypeStr = "tro";
                    }
                    BasetypeTemplate subtype = this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeTemplateByName(subtypeStr);
                    if(subtype == null) {
                        //TODO: check for ketose subtype in case subtype is null
                        //*** no subtype found yet, check for ketose subtype: ***
                        TrivialnameTemplate trivSubtype = this.getTemplateContainer().getTrivialnameTemplateContainer().forBasetypeName(this.getNamescheme(), subtypeStr);
                        if(trivSubtype != null) {
                            
                        }
                    }
                    if(subtype != null) {
                        this.setDetectedSubtype(subtype);
                        this.setTmpStereocode(subtype.getStereocode());
                        ms.setDefaultCarbonylPosition(subtype.getCarbonylPosition());
                        this.setPrePosStr(getPrePosStr().substring(0,getPrePosStr().length() - 3));
                    }
                }
                this.setFoundMs(true);
                break;
            }
        }
        
        //*** if no ms basetype was found yet, search for specific type (like glc, gal, man, ...) ***
        if(!isFoundMs()) {
            for(int i = 0; i < basetypesSpecificList.size(); i++) {
                int basepos = nameLowercase.indexOf(basetypesSpecificList.get(i));
                if(basepos != -1) {
                    this.setPrePosStr(name.substring(0, basepos));
                    this.setPostPosStr(name.substring(basepos + basetypesSpecificList.get(i).length()));
                    BasetypeTemplate basetype = this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeTemplateByName(basetypesSpecificList.get(i));
                    this.setDetectedBasetype(basetype);
                    ms.setSize(basetype.getSize());
                    ms.setDefaultCarbonylPosition(basetype.getCarbonylPosition());
                    this.setTmpStereocode(basetype.getStereocode());
                    this.setFoundMs(true);
                    break;
                }
            }
        }
        
        //*** if still no ms basetype was found, try trivial names: ***
        if(!isFoundMs()) {
            ArrayList<String> trivialNamesList = this.getTemplateContainer().getTrivialnameTemplateContainer().getTrivialnameBasetypeList(GlycanNamescheme.CARBBANK);
            for(String basename : trivialNamesList) {
                int basepos = nameLowercase.indexOf(basename);
                if(basepos != -1) {
                    this.setPrePosStr(name.substring(0, basepos));
                    this.setPostPosStr(name.substring(basepos + basename.length()));
                    TrivialnameTemplate template = this.getTemplateContainer().getTrivialnameTemplateContainer().forBasetypeName(this.getNamescheme(), basename);
                    this.setDetectedBasetype(template);
                    this.setDetectedTrivialname(template);
                    this.setTmpStereocode(template.getStereocode());
                    this.setFoundMs(true);
                    ms.init(template);
                    /*ms.setSize(template.getSize());
                    ms.setDefaultCarbonylPosition(template.getCarbonylPosition());
                    for(CoreModification coremod : template.getCoreModifications()) {
                        try {
                            ms.addCoreModification(coremod.clone());
                        } catch(MonosaccharideException me) {
                            throw new MonosaccharideException("Internal error: " + me.getMessage(), me);
                        }
                    }
                    for(Substitution subst : template.getSubstitutions()) {
                        try {
                            ms.addSubstitution(subst.clone());
                        } catch(MonosaccharideException me) {
                            throw new MonosaccharideException("Internal error: " + me.getMessage(), me);
                        }
                    }*/
                    break;
                }
            }
        }
        
        if(isFoundMs()) {
            this.parsePreposStr(getPrePosStr(), ms);
            this.parsePostposStr(getPostPosStr(), ms);
            this.processParsedData(ms);
        } else {
            throw new NameParsingException("Could not find ms basetype in " + name);
        }
        if(this.getDetectedTrivialname() != null) {
            TrivialnameTemplate trivTmpl = this.getDetectedTrivialname();
            if(trivTmpl.isDefaultConfigIsCompulsory()) {
                if(ms.getConfiguration() == null) {
                    ms.setConfiguration(trivTmpl.getDefaultConfiguration());
                }
                if(!trivTmpl.getDefaultConfiguration().equals(ms.getConfiguration())) {
                    //TODO: store actually detected name and use this instead of primary name (in case more than one name is defined for a trivialname template in one namescheme)
                    throw new NameParsingException("Trivialname '" + trivTmpl.getPrimaryName(this.getNamescheme()) + "' is not defined for configuration " + ms.getConfiguration().getSymbol() + ".");
                }
            }
        }
        MonosaccharideValidation.checkMonosaccharideConsistency(ms, this.getTemplateContainer(), this.getConfig());
    }
    
    private void parsePreposStr(String preposStr, Monosaccharide ms) throws ResourcesDbException {
        while(preposStr.length() > 0) {
            if(preposStr.startsWith("-")) {
                preposStr = preposStr.substring(1); //*** make sure no dash is left at the beginning of parsed string ***
                this.increaseParsingPosition();
                continue;
            }
            
            //*** check for open chain marker: ***
            if(preposStr.startsWith("aldehydo-")) {
                ms.setRingEnd(Basetype.OPEN_CHAIN);
                ms.setRingStart(Basetype.OPEN_CHAIN);
                preposStr = preposStr.substring(9);
                this.increaseParsingPosition(9);
                this.detectedRingtype = Ringtype.OPEN;
                continue;
            }
            if(preposStr.startsWith("keto-")) {
                ms.setRingEnd(Basetype.OPEN_CHAIN);
                ms.setRingStart(Basetype.OPEN_CHAIN);
                preposStr = preposStr.substring(5);
                this.increaseParsingPosition(5);
                this.detectedRingtype = Ringtype.OPEN;
                continue;
            }
            //TODO: add checks, if marker type matches ms type (aldose / ketose) 
            
            //*** check for wildcard: ***
            if(preposStr.startsWith("?-")) {
                this.increasePreposWildcards();
                preposStr = preposStr.substring(2);
                this.increaseParsingPosition(2);
                //*** check, if a monosaccharide basetype name follows the wildcard (something like ?-gro) ***
                if(preposStr.length() > 2) {
                    BasetypeTemplate subtype2 = this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeTemplateByName(preposStr.substring(0, 3));
                    if(subtype2 != null) {
                        preposStr = preposStr.substring(3);
                        this.increaseParsingPosition(3);
                        String subStereoStr = Stereocode.absoluteToRelative(subtype2.getStereocode());
                        ms.setStereoStr(subStereoStr + ms.getStereoStr());
                        addFuzzy();
                        this.decreasePreposWildcards();
                    }
                }
                continue;
            }
            
            //*** check for anomer: ***
            if(preposStr.toLowerCase().startsWith("a-") || preposStr.toLowerCase().startsWith("b-")) {
                if(ms.getAnomer() != null) { //*** anomer already set ***
                    throw new NameParsingException("multiple definition of anomer.", this.getInputName(), this.getParsingPosition());
                } else {
                    ms.setAnomer(preposStr.substring(0,1));
                }
                preposStr = preposStr.substring(2);
                this.increaseParsingPosition(2);
                continue;
            }
            if(preposStr.toLowerCase().startsWith("alpha")) {
                if(ms.getAnomer() != null) { //*** anomer already set ***
                    throw new NameParsingException("multiple definition of anomer.", this.getInputName(), this.getParsingPosition());
                } else {
                    ms.setAnomer("a");
                }
                preposStr = preposStr.substring(5);
                this.increaseParsingPosition(5);
                continue;
            }
            if(preposStr.toLowerCase().startsWith("beta")) {
                if(ms.getAnomer() != null) { //*** anomer already set ***
                    throw new NameParsingException("multiple definition of anomer.", this.getInputName(), this.getParsingPosition());
                } else {
                    ms.setAnomer("b");
                }
                preposStr = preposStr.substring(4);
                this.increaseParsingPosition(4);
                continue;
            }
            
            //*** check for configuration: ***
            int tmpParsingPosition = this.getParsingPosition();
            if(preposStr.toLowerCase().startsWith("d-") || preposStr.toLowerCase().startsWith("l-")) {
                String configStr = preposStr.substring(0, 1).toLowerCase();
                preposStr = preposStr.substring(2);
                this.increaseParsingPosition(2);
                //*** check, if a monosaccharide basetype name follows the configuration (something like "d-gro" in d-gro-a-d-manhepp) ***
                BasetypeTemplate subtype2 = null;
                if(preposStr.length() > 2) {
                    String subtypeStr = preposStr.substring(0, 3);
                    subtype2 = this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeTemplateByName(subtypeStr);
                }
                if(subtype2 != null) {
                    String subStereo = subtype2.getStereocode();
                    if(configStr.equals("l")) {
                        subStereo = Stereocode.changeDLinStereoString(subStereo);
                    }
                    ms.setStereoStr(subStereo + ms.getStereoStr());
                    preposStr = preposStr.substring(3);
                    this.increaseParsingPosition(3);
                    continue;
                }
                if(ms.getConfiguration() != null) {
                    throw new NameParsingException("Found multiple configuration definitions.", this.getInputName(), tmpParsingPosition);
                } else {
                    ms.setConfiguration(configStr);
                }
                continue;
            }
            
            //*** check for modifications: ***
            String tmpStr = preposStr;
            preposStr = parseModifications(preposStr, ms);
            if(!tmpStr.equals(preposStr)) { //*** modifications where found ***
                continue;
            }
            
            //*** check for additional subtype (as it might occurr in residues with more than 6 backbone carbon atoms): ***
            //*** it will only be found here if no configuration symbol is assigned to this subtype - otherwise it will have been parsed above already ***
            //*** therefore, the stereocode has to be set to a relative definition if a subtype is found here: ***
            if(preposStr.length() > 2) {
                BasetypeTemplate subtype2 = null;
                subtype2 = this.getTemplateContainer().getBasetypeTemplateContainer().getBasetypeTemplateByName(preposStr.substring(0, 3));
                if(subtype2 != null) {
                    String subStereo = Stereocode.absoluteToRelative(subtype2.getStereocode());
                    ms.setStereoStr(subStereo + ms.getStereoStr());
                    preposStr = preposStr.substring(3);
                    this.increaseParsingPosition(3);
                    continue;
                }
            }
            
            //*** no known elements detected in current part of preposStr => no further parsing possible ***
            throw new NameParsingException("Cannot parse " + preposStr, this.getInputName(), this.getParsingPosition());
        }
    }
    
    private void parsePostposStr(String postposStr, Monosaccharide ms) throws ResourcesDbException {
        if(postposStr.startsWith("-")) {
            postposStr = postposStr.substring(1);
            this.increaseParsingPosition(1);
        }
        //*** check for ulo modification that might stand between base type and ring type: ***
        if(postposStr.toLowerCase().matches("^[0-9]+(,[0-9])*(-)?(di|tri|tetra){0,1}(-)?ulo(.*)")) {
            postposStr = parseModifications(postposStr, ms);
        }
        //*** check for ring type: ***
        if(postposStr.toLowerCase().startsWith("p")) {
            this.detectedRingtype = Ringtype.PYRANOSE;
            //ms.setRingtype(Ringtype.pyranose);
            postposStr = postposStr.substring(1);
            this.increaseParsingPosition(1);
        } else if(postposStr.toLowerCase().startsWith("f")) {
            this.detectedRingtype = Ringtype.FURANOSE;
            //ms.setRingtype(Ringtype.furanose);
            postposStr = postposStr.substring(1);
            this.increaseParsingPosition(1);
        }
        while(postposStr.length() > 0) {
            if(postposStr.startsWith("-")) {
                postposStr = postposStr.substring(1);
                this.increaseParsingPosition(1);
                continue;
            }
            if(postposStr.toLowerCase().startsWith("ol")) {
                ms.setAlditol(true);
                this.detectedRingtype = Ringtype.OPEN;
                postposStr = postposStr.substring(2);
                this.increaseParsingPosition(2);
                continue;
            }
            if(postposStr.toLowerCase().startsWith("onic")) {
                ms.setAldonic();
                postposStr = postposStr.substring(4);
                this.increaseParsingPosition(4);
                continue;
            }
            if(postposStr.toLowerCase().startsWith("aric")) {
                ms.setAldaric();
                postposStr = postposStr.substring(4);
                this.increaseParsingPosition(4);
                continue;
            }
            if(postposStr.toLowerCase().startsWith("a")) {
                boolean matchesSubstitution = false;
                ArrayList<String> substNameList = this.getTemplateContainer().getSubstituentTemplateContainer().getResidueIncludedNameList(this.getNamescheme());
                for(int i = 0; i < substNameList.size(); i++) {
                    if(postposStr.toLowerCase().startsWith(substNameList.get(i).toLowerCase())) {
                        matchesSubstitution = true;
                        break;
                    }
                }
                if(!matchesSubstitution) {
                    ms.setUronic();
                    postposStr = postposStr.substring(1);
                    this.increaseParsingPosition(1);
                    continue;
                }
            }
            //*** check for modifications: ***
            String tmpStr = postposStr;
            postposStr = parseModifications(postposStr, ms);
            if(!tmpStr.equals(postposStr)) { //*** modifications where found ***
                continue;
            }
            
            //*** unparsable string: ***
            throw new NameParsingException("Cannot parse " + postposStr, this.getInputName(), this.getParsingPosition());
        }
    }
    
    private void processParsedData(Monosaccharide ms) throws ResourcesDbException {
        
        //*** set ring: ***
        if(ms.getRingStart() == Basetype.UNKNOWN_RING) {
            ms.setRingStart(ms.getDefaultCarbonylPosition());
        }
        if(this.detectedRingtype == null) {
            if(this.useDefaultValues) {
                if(this.getDetectedTrivialname() != null) {
                    ms.setRingEnd(this.getDetectedTrivialname().getDefaultRingend());
                } else if(this.getDetectedBasetype() != null) {
                    ms.setRingEnd(this.getDetectedBasetype().getDefaultRingend());
                }
            }
        } else {
            ms.setRingtype(this.detectedRingtype);
        }
        
        //*** check for completeness / excluding information: ***
        if(ms.getConfiguration() == null) {
            if(isUseDefaultValues() && getDetectedBasetype().getDefaultConfiguration() != null) {
                ms.setConfiguration(getDetectedBasetype().getDefaultConfiguration());
            } else {
                if(isUseDefaultValues() && getDetectedSubtype() != null && getDetectedSubtype().getDefaultConfiguration() != null) {
                    ms.setConfiguration(getDetectedSubtype().getDefaultConfiguration());
                } else {
                    ms.setConfiguration(StereoConfiguration.Unknown);
                }
            }
        }
        
        //*** set stereocode: ***
        String stereo;
        if(getTmpStereocode() == null || getTmpStereocode().length() == 0) {
            if(getDetectedBasetype().isSuperclass()) {
                /*for(int i = 0; i < getDetectedBasetype().getSize() - 2 - ms.getStereoStr().length(); i++) {
                    setTmpStereocode(getTmpStereocode() + StereoConfiguration.Unknown.getStereosymbol());
                }*/
                setTmpStereocode(StringUtils.multiplyChar(StereoConfiguration.Unknown.getStereosymbol(), getDetectedBasetype().getSize() - 2 - ms.getStereoStr().length()));
            }
        }
        if(ms.getConfiguration().equals(StereoConfiguration.Laevus)) {
            stereo = Stereocode.changeDLinStereoString(getTmpStereocode()) + ms.getStereoStr();
        } else if(ms.getConfiguration() == null || ms.getConfiguration().equals(StereoConfiguration.Unknown)) { //*** absolute configuration is not known ***
            stereo = Stereocode.absoluteToRelative(getTmpStereocode()) + ms.getStereoStr();
        } else {
            stereo = getTmpStereocode() + ms.getStereoStr();
        }
        
        //TODO: consider lactone modification
        if(ms.hasCoreModification(CoreModificationTemplate.ACID, 1)) {
            if(ms.getRingStart() == 1) {
                if(ms.getRingEnd() > 0) {
                    throw new MonosaccharideException("Aldonic residue with open chain cannot have ring oxygen " + ms.getRingEnd());
                } else {
                    ms.setRingEnd(Basetype.OPEN_CHAIN);
                }
            }
        }
        
        if(ms.getAnomer() == null) {
            if(ms.isAlditol() || ms.getRingEnd() == Basetype.OPEN_CHAIN) {
                ms.setAnomer(Anomer.OPEN_CHAIN);
            } else {
                ms.setAnomer(Anomer.UNKNOWN);
            }
        }
        
        stereo = StereoConfiguration.Nonchiral.getStereosymbol() + stereo + StereoConfiguration.Nonchiral.getStereosymbol();
        //*** handle loss of stereochemistry: ***
        if(getDetectedBasetype().isSuperclass()) {
            if(getDetectedSubtype() != null) { //*** monosaccharide name contains combination of superclass and subtype, like xylHex ***
                stereo = Stereocode.getChiralOnlyStereoString(stereo);
                stereo = Stereocode.expandChiralonlyStereoString(stereo, ms);
                if(stereo.length() != ms.getSize()) {
                    throw new MonosaccharideException("Error in stereocode: size is " + stereo.length() + ", ms size is " + ms.getSize());
                }
            }
        } else {
            stereo = Stereocode.markNonchiralPositionsInStereoString(stereo, ms);
        }
        
        ms.setStereoStr(stereo);
        
        //*** set stereocode resulting from anomeric: ***
        ms.setAnomerInStereocode();
        
        //*** root templates: ***
        //setRootTemplates(ms);
        
        ms.setFuzzy(this.isFuzzy());
    }
    
    private String parseModifications(String parseStr, Monosaccharide ms) throws ResourcesDbException {
        if(parseStr.startsWith(";")) { //*** in case a semicolon is used as delimiter, remove it ***
            parseStr = parseStr.substring(1);
            this.increaseParsingPosition(1);
        }
        if(parseStr.startsWith("-")) { //*** make sure that no dash is left at the beginning of the string ***
            parseStr = parseStr.substring(1);
            this.increaseParsingPosition(1);
        }
        if(parseStr.length() > 0) {
            //*** read position(s): ***
            int defaultPos = 0;
            ArrayList<Integer> positions = new ArrayList<Integer>();
            ArrayList<Integer> autoPosList = new ArrayList<Integer>();
            String posStr = "";
            boolean foundSubstitution = false;
            boolean foundCoreModification = false;
            while(parseStr.matches("^[0-9].*")) {
                posStr += parseStr.substring(0,1);
                parseStr = parseStr.substring(1);
                this.increaseParsingPosition(1);
            }
            if(posStr.equals("")) {
                if(parseStr.startsWith("?")) {
                    posStr = "0";
                    parseStr = parseStr.substring(1);
                    this.increaseParsingPosition(1);
                } else if(this.getDetectedTrivialname() != null && this.getDetectedTrivialname().getPrimaryName(this.getNamescheme()).equalsIgnoreCase("neu")) {
                    if(ms.countSubstitutions() == 1 && ms.getSubstitution(SubstituentTemplate.AMINOTEMPLATENAME, 5, LinkageType.DEOXY) != null) { //*** no modifications apart from the 5N that is included in the trivial name set so far ***
                        //TODO: replace 
                        if(parseStr.toLowerCase().matches("^n{0,1}([ag]c)$") || parseStr.toLowerCase().matches("^n{0,1}([ag]c)[0-9](.*)")) {
                            if(isUseDefaultValues()) {
                                defaultPos = 5;
                                posStr = "5";
                            } else {
                                defaultPos = -1;
                                posStr = "0";
                            }
                        }
                    }
                } else if(ms.countSubstitutions() == 0) {
                    //if(this.namescheme.equals(GlycanNamescheme.GLYCOSCIENCES)) {
                        defaultPos = 2;
                        posStr = "2";
                    /*} else {
                        if(parseStr.toLowerCase().matches("^n(ac){0,1}$") || parseStr.toLowerCase().matches("^n(ac){0,1}[0-9].*") || parseStr.toLowerCase().matches("^n(ac){0,1}-.*")) {
                            defaultPos = 2;
                            posStr = "2";
                        }
                    }*/
                } else {
                    defaultPos = -1;
                    posStr = "0";
                }
            }
            if(posStr.equals("")) {
                defaultPos = -1;
                posStr = "0";
            }
            if(defaultPos > 0) {
                autoPosList.add(new Integer(posStr));
            } else {
                positions.add(new Integer(posStr));
            }
            
            //*** check for further, comma-separated positions: ***
            while(parseStr.matches("^,[0-9?].*")) {
                posStr = parseStr.substring(1,2);
                parseStr = parseStr.substring(2);
                this.increaseParsingPosition(2);
                while(parseStr.matches("^[0-9].*")) {
                    posStr += parseStr.substring(0,1);
                    parseStr = parseStr.substring(1);
                    this.increaseParsingPosition(1);
                }
                positions.add(new Integer(posStr));
            }
            if(parseStr.startsWith("-")) {
                parseStr = parseStr.substring(1);
                this.increaseParsingPosition(1);
            }
            
            //*** get modification name: ***
            String modStr = "";
            ArrayList<String> coreModList = CoreModificationTemplate.getCarbbankNamesList();
            for(int i = 0; i < coreModList.size(); i++) {
                String coremodName = coreModList.get(i);
                if(coremodName.length() > modStr.length()) {
                    if(parseStr.toLowerCase().startsWith(coremodName)) {
                        modStr = coremodName;
                        foundCoreModification = true;
                    }
                }
            }
            ArrayList<String> substTemplateList = this.getTemplateContainer().getSubstituentTemplateContainer().getResidueIncludedNameList(this.getNamescheme());
            for(int i = 0; i < substTemplateList.size(); i++) {
                String substName = substTemplateList.get(i).toLowerCase();
                if(substName.length() > modStr.length()) {
                    if(parseStr.toLowerCase().startsWith(substName)) {
                        modStr = substName;
                        foundSubstitution = true;
                    }
                }
            }
            
            ArrayList<NumberPrefix> numberPrefixList = new ArrayList<NumberPrefix>();
            int numberPrefixStrLength = 0;
            if((modStr.length() == 0) && (positions.size() + autoPosList.size() > 1)) { //*** no modification, but positions found so far ***
                numberPrefixList = NumberPrefix.getPrefixListBySize(positions.size() + autoPosList.size());
                for(NumberPrefix prefix : numberPrefixList) {
                    String numberPrefixStr = prefix.getPrefixStr();
                    for(int i = 0; i < coreModList.size(); i++) {
                        String coremodName = coreModList.get(i);
                        if(coremodName.length() > modStr.length()) {
                            if(parseStr.toLowerCase().startsWith(numberPrefixStr + coremodName)) {
                                modStr = coremodName;
                                foundCoreModification = true;
                                numberPrefixStrLength = numberPrefixStr.length();
                            }
                        }
                    }
                    for(int i = 0; i < substTemplateList.size(); i++) {
                        String substName = substTemplateList.get(i);
                        if(substName.length() > modStr.length()) {
                            if(parseStr.toLowerCase().startsWith(numberPrefixStr + substName)) {
                                modStr = substName;
                                foundSubstitution = true;
                                numberPrefixStrLength = numberPrefixStr.length();
                            }
                        }
                    }
                    if(modStr.length() > 0) {
                        break;
                    }
                }
            }
            
            if(modStr.length() > 0) {
                if(foundCoreModification) {
                    for(int i = 0; i < autoPosList.size(); i++) {
                        positions.add(new Integer(0)); //*** default positions are only valid for substituents ***
                    }
                    CoreModification mod;
                    CoreModificationTemplate modTemplate = CoreModificationTemplate.forCarbbankName(modStr);
                    if(modTemplate == null) {
                        throw new ResourcesDbException("Cannot get template for core modification " + modStr + " (carbbank style)");
                    }
                    if(modTemplate.equals(CoreModificationTemplate.EN)) {
                        modTemplate = CoreModificationTemplate.ENX;
                    }
                    if(modTemplate.equals(CoreModificationTemplate.EN) || modTemplate.equals(CoreModificationTemplate.ENX) || modTemplate.equals(CoreModificationTemplate.YN)) {
                        for(int i = 0; i < positions.size(); i++) {
                            int position = positions.get(i).intValue();
                            mod = new CoreModification();
                            mod.setDivalentModification(modTemplate, position, position + 1);
                            mod.setSourceName(modStr);
                            ms.addCoreModification(mod);
                        }
                    } else if(modTemplate.getValence() == 1) {
                        for(int i = 0; i < positions.size(); i++) {
                            int position = positions.get(i).intValue();
                            if(this.getNamescheme().equals(GlycanNamescheme.IUPAC) && modTemplate.equals(CoreModificationTemplate.DEOXY)) {
                                ArrayList<Substitution> substList = ms.getSubstitutionsByPosition(position);
                                for(Substitution subst: substList) {
                                    if(subst.getLinkagetype1().equals(LinkageType.DEOXY)) {
                                        continue; //*** deoxygenation is implied in existing substitution ***
                                    }
                                }
                            }
                            if(position > 1 && modTemplate.equals(CoreModificationTemplate.KETO)) {
                                if(ms.getDefaultCarbonylPosition() == 1 && !ms.hasCoreModification(CoreModificationTemplate.KETO, 1)) {
                                    ms.setDefaultCarbonylPosition(position);
                                }
                            }
                            mod = new CoreModification();
                            mod.setModification(modTemplate, position);
                            mod.setSourceName(modStr);
                            ms.addCoreModification(mod);
                        }
                    } else if(modTemplate.getValence() == 2) {
                        if(positions.size() == 2) {
                            mod = new CoreModification();
                            mod.setDivalentModification(modTemplate, positions.get(0).intValue(), positions.get(1).intValue());
                            mod.setSourceName(modStr);
                            ms.addCoreModification(mod);
                        } else {
                            throw new NameParsingException("Divalent core modification " + modStr + " requires two positions.");
                        }
                    }
                } else if(foundSubstitution) {
                    positions.addAll(autoPosList);
                    SubstituentTemplate substTemplate = this.getTemplateContainer().getSubstituentTemplateContainer().forResidueIncludedName(this.getNamescheme(), modStr);
                    if(substTemplate == null) {
                        throw new ResourcesDbException("Cannot get substituent template for substituent name " + modStr + " and namescheme "  + this.getNamescheme().getNameStr());
                    }
                    if(substTemplate.getMaxValence() == 1) {
                        LinkageType linktype = substTemplate.getLinkageTypeBySubstituentName(this.getNamescheme(), modStr);
                        for(int i = 0; i < positions.size(); i++) {
                            Substitution subst = new Substitution(this.getTemplateContainer());
                            subst.setSourceName(modStr);
                            if(this.getNamescheme().equals(GlycanNamescheme.IUPAC)) {
                                if(LinkageType.DEOXY.equals(linktype)) {
                                    //*** substituent implies deoxygenation ***
                                    //*** check, if explicit deoxy was given before, and if so replace that with current substituent ***
                                    CoreModification mod = ms.getCoreModification(CoreModificationTemplate.DEOXY.getName(), positions.get(i).intValue());
                                    if(mod != null) {
                                        ms.deleteCoreModification(mod);
                                        continue;
                                    }
                                }
                            }
                            if(positions.size() == 1 && getDetectedBasetype().isTrivialname()) {
                                //*** check, if a trivial name implies an amine, which might be extended like in neu5ac: ***
                                TrivialnameTemplate trivTemplate = (TrivialnameTemplate) getDetectedBasetype();
                                for(Substitution trivSubst : trivTemplate.getSubstitutions()) {
                                    if(trivSubst.getValence() == 1 && trivSubst.getPosition1().get(0).equals(positions.get(i))) {
                                        if(trivSubst.getName().equals(this.getTemplateContainer().getSubstituentTemplateContainer().forResidueIncludedName(GlycanNamescheme.CARBBANK, "n").getName())) {
                                            //*** trivial name contains amine, now if the substituent is n-linked or can be added to an n to build an n-linked one (like Ac to NAc), the amine is to be replaced by this ***
                                            Substitution nSubst = ms.getSubstitution(trivSubst.getName(), trivSubst.getPosition1().get(0).intValue(), LinkageType.DEOXY);
                                            if(nSubst != null) {
                                                if(substTemplate.getDefaultLinkingAtom1().getElement().getPeriodicNumber() == 7) {
                                                    nSubst.alterSubstituentTemplate(substTemplate);
                                                    substTemplate = null;
                                                } else {
                                                    SubstituentTemplate tmpSubstTmpl = this.getTemplateContainer().getSubstituentTemplateContainer().forName(GlycanNamescheme.CARBBANK, "n" + modStr);
                                                    if(tmpSubstTmpl != null && tmpSubstTmpl.getDefaultLinkingAtom1().getElement().getPeriodicNumber() == 7) {
                                                        nSubst.alterSubstituentTemplate(tmpSubstTmpl);
                                                        substTemplate = null;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if(substTemplate != null) {
                                subst.setSubstitution(substTemplate, positions.get(i).intValue(), linktype);
                                ms.addSubstitution(subst);
                                if(subst.getBondOrder1() == 2) {
                                    if(subst.getPosition1().size() == 1 && subst.getPosition1().get(0).intValue() != 0) {
                                        ms.addCoreModification(new CoreModification(CoreModificationTemplate.SP2, subst.getPosition1().get(0).intValue()));
                                    } else {
                                        System.out.println("Warning: substitution with sp2 hybrid at unknown position.");
                                    }
                                }
                            }
                        }
                    } else if(substTemplate.getMaxValence() == 2) {
                        Substitution subst = new Substitution();
                        subst.setSourceName(modStr);
                        if(positions.size() == 2) {
                            subst.setDivalentSubstitution(substTemplate, positions.get(0).intValue(), positions.get(1).intValue());
                            ms.addSubstitution(subst);
                        } else {
                            if(substTemplate.getMinValence() == 2) {
                                throw new NameParsingException("Only one position given for divalent substituent " + modStr);
                            } else {
                                for(Integer pos : positions) {
                                    subst = new Substitution();
                                    subst.setSourceName(modStr);
                                    subst.setSubstitution(substTemplate, pos.intValue());
                                    ms.addSubstitution(subst);
                                }
                            }
                        }
                    }
                }
                parseStr = parseStr.substring(modStr.length() + numberPrefixStrLength);
                this.increaseParsingPosition(modStr.length() + numberPrefixStrLength);
            } else { //*** no modification found ***
                if(defaultPos == 0) { //*** position was given explicitely ***
                    throw new NameParsingException("Cannot assign modification in " + parseStr, this.getInputName(), this.getParsingPosition());
                }
            }
        }
        return(parseStr);
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************

    public void init() {
        super.init();
        this.setPrePosStr("");
        this.setPostPosStr("");
        this.setDetectedBasetype(null);
        this.setDetectedSubtype(null);
        this.setFuzzy(0);
        this.setUseDefaultValues(false);
        this.detectedRingtype = null;
    }
    
}
