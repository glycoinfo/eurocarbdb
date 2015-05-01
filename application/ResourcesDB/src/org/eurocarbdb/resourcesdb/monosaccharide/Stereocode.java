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

import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.template.BasetypeTemplate;
import org.eurocarbdb.resourcesdb.template.BasetypeTemplateContainer;

public class Stereocode implements Cloneable {
    //*** standard stereocode symbols: ***
    public static final char StereoD = '2';
    public static final char StereoL = '1';
    public static final char StereoXD = '4';
    public static final char StereoXL = '3';
    public static final char StereoX = 'x';
    public static final char StereoN = '0';
    
    //*** extended stereocode symbols: ***
    public static final char ExtStereoDeoxy = 'd';
    public static final char ExtStereoEnOH = 'n';
    public static final char ExtStereoEnDeoxy = 'e';
    public static final char ExtStereoEnX = 'E';
    public static final char ExtStereoCH2OH = 'h';
    public static final char ExtStereoCH3 = 'm';
    public static final char ExtStereoAcid = 'a';
    public static final char ExtStereoCarbonyl = 'o';
    public static final char ExtStereoSp2 = 's';
    public static final char ExtStereoYn = 'y';
    public static final char ExtStereoKeto = 'k';
    public static final char ExtStereoUnknown = 'x';
    
    private String stereoStr;
    
    public static final String SUBTYPESTR = "subtype";
    public static final String BASETYPESTR = "basetype";

    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    /**
     * Constructor, creates a stereocode initialized with a given stereo string
     * @param code: stereocode string
     */
    public Stereocode(String code) {
        setStereoStr(code);
    }
    
    /**
     * Constructor, creates an empty stereocode
     */
    public Stereocode() {
        setStereoStr("");
    }
    
    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************
    
    /**
     * Get the stereocode string.
     * @return the stereocode string
     */
    public String getStereoStr() {
        return stereoStr;
    }

    /**
     * Set the stereocode string.
     * @param stereocode: The string to set.
     */
    public void setStereoStr(String stereocode) {
        this.stereoStr = stereocode;
    }
    
    //*****************************************************************************
    //*** methods to manipulate the stereocode: ***********************************
    //*****************************************************************************
    
    /**
     * Alter a stereocode string in a way as it would result from rotating an alditol residue by 180°.
     * @param stereo: The stereocode string to be altered.
     * @return The adjusted stereocode string
     */
    public static String rotateStereoString(String stereo) {
        String rotStereo = "";
        for(int i = stereo.length() - 1; i >= 0; i--) {
            rotStereo += stereo.substring(i, i + 1);
        }
        return(changeDLinStereoString(rotStereo));
    }
    
    /**
     * Alter the stereocode in a way as it would result from rotating an alditol residue by 180°.
     */
    public void rotate() {
        setStereoStr(Stereocode.rotateStereoString(getStereoStr()));
    }
    
    /**
     * Change a stereocode string in D configuration to one in L configuration and vice versa.
     * @param stereoIn: The stereocode string to be altered.
     * @return The altered stereocode string.
     */
    public static String changeDLinStereoString(String stereoIn) {
        String stereoOut = "";
        for(int i = 0; i < stereoIn.length(); i++) {
            if(stereoIn.charAt(i) == StereoD) {
                stereoOut += StereoL;
            } else if(stereoIn.charAt(i) == StereoL) {
                stereoOut += StereoD;
            } else if(stereoIn.charAt(i) == StereoXD) {
                stereoOut += StereoXL;
            } else if(stereoIn.charAt(i) == StereoXL) {
                stereoOut += StereoXD;
            } else {
                stereoOut += stereoIn.charAt(i);
            }
        }
        return(stereoOut);
    }
    
    /**
     * Change a stereocode in D configuration to one in L configuration and vice versa.
     */
    public void changeDL() {
        setStereoStr(Stereocode.changeDLinStereoString(getStereoStr()));
    }
    
    /**
     * Change relative D positions in a stereo string to relative L positions and vice versa.
     * Note: Absolute D or L positions are not changed by this method!
     * @param stereoIn: The stereocode string to be altered.
     * @return The altered stereocode string.
     */
    public static String changeRelativeDLinStereoString(String stereoIn) {
        String stereoOut = "";
        for(int i = 0; i < stereoIn.length(); i++) {
            if(stereoIn.charAt(i) == StereoXD) {
                stereoOut += StereoXL;
            } else if(stereoIn.charAt(i) == StereoXL) {
                stereoOut += StereoXD;
            } else {
                stereoOut += stereoIn.charAt(i);
            }
        }
        return(stereoOut);
    }
    
    /**
     * Change relative D positions in this Stereocode to relative L positions and vice versa
     */
    public void changeRelativeDL() {
        this.setStereoStr(Stereocode.changeRelativeDLinStereoString(this.getStereoStr()));
    }
    
    /**
     * Translate a stereocode string in absolute configuration into one in relative configuration
     * @param stereoIn: The stereocode string to be translated.
     * @return: The translated stereocode string.
     * @throws ResourcesDbException in case the stereocode string contains characters that do not encode any configuration status
     */
    public static String absoluteToRelative(String stereoIn) throws ResourcesDbException {
        String stereoOut = "";
        for(int i = 0; i < stereoIn.length(); i++) {
            stereoOut += StereoConfiguration.stereosymbolAbsoluteToRelative(stereoIn.charAt(i));
        }
        return(stereoOut);
    }
    
    /**
     * Translate a stereocode string in relative configuration into one in absolute configuration.
     * If both absolute and relative configurations are present in the stereocode string, the absolute positions are kept as they are.
     * @param stereoIn: The stereocode string to be translated.
     * @return: The translated stereocode string.
     * @throws ResourcesDbException in case the stereocode string contains characters that do not encode any configuration status
     */
    public static String relativeToAbsolute(String stereoIn) throws ResourcesDbException {
        String stereoOut = "";
        for(int i = 0; i < stereoIn.length(); i++) {
            stereoOut += StereoConfiguration.stereosymbolRelativeToAbsolute(stereoIn.charAt(i));
        }
        return(stereoOut);
    }
    
    /**
     * Check, if a stereo string contains both absolute and relative configuration positions
     * @param stereo: the stereo string to be checked
     * @return
     * @throws ResourcesDbException in case the stereo string contains characters that do not encode any configuration status
     */
    public static boolean stereoStringContainsAbsoluteAndRelative(String stereo) throws ResourcesDbException {
        boolean containsAbsolute = false;
        boolean containsRelative = false;
        for(int i = 0; i < stereo.length(); i++) {
            StereoConfiguration c = StereoConfiguration.forStereosymbol(stereo.charAt(i));
            if(c.equals(StereoConfiguration.Dexter) || c.equals(StereoConfiguration.Laevus)) {
                containsAbsolute = true;
            }
            if(c.equals(StereoConfiguration.XDexter) || c.equals(StereoConfiguration.XLaevus)) {
                containsRelative = true;
            }
        }
        return(containsAbsolute && containsRelative);
    }
    
    /**
     * Check, if a stereo string has at least one relative configuration position
     * @param stereo: the stereo string to be checked
     * @return
     */
    public static boolean stereoStringHasRelativePosition(String stereo) {
        for(int i = 0; i < stereo.length(); i++) {
            StereoConfiguration c;
            try {
                c = StereoConfiguration.forStereosymbol(stereo.charAt(i));
            } catch (ResourcesDbException e) {
                continue;
            }
            if(c.equals(StereoConfiguration.XDexter) || c.equals(StereoConfiguration.XLaevus)) {
                return(true);
            }
        }
        return(false);
    }
    
    /**
     * Check, if this Stereocode has at least one relative configuration position
     * @return
     */
    public boolean hasRelativePosition() {
        return Stereocode.stereoStringHasRelativePosition(this.getStereoStr());
    }
    
    /**
     * Get the configuration of a monosaccharide or a monosaccharide subpart based on a stereocode string
     * Note: a relative configuration (XD/XL) is handled as an unknown one here
     * @param stereo: the stereostring from which the configuration is determined
     * @return the configuration (D/L/X)
     */
    public static StereoConfiguration getConfigurationFromStereoString(String stereo) {
        stereo = stereo.replaceAll("" + StereoN, "");
        char configCode = ' ';
        if(stereo.length() == 0) {
            return(StereoConfiguration.Nonchiral);
        }
        if(stereo.length() < 4) {
            configCode = stereo.charAt(stereo.length() - 1);
        } else {
            configCode = stereo.charAt(3);
        }
        if(configCode == StereoD) {
            return(StereoConfiguration.Dexter);
        } else if(configCode == StereoL) {
            return(StereoConfiguration.Laevus);
        } else {
            return(StereoConfiguration.Unknown);
        }
    }
    
    /**
     * Get the configuration of a monosaccharide trivial name based on a stereocode string
     * This method differs from "getConfigurationFromStereostring(String stereo)" insofar that the configuration is always determined from the last non-chiral center, no matter how long the stereo string is.
     * This is needed to correctly assign the configuration of trivial names that cover more than 4 stereocenters.
     * Note: a relative configuration (XD/XL) is handled as an unknown one here
     * @param stereo: the stereostring from which the configuration is determined
     * @return the configuration (D/L/X)
     */
    public static StereoConfiguration getTrivialnameConfigurationFromStereoString(String stereo) {
        stereo = stereo.replaceAll("" + StereoN, "");
        char configCode = ' ';
        if(stereo.length() == 0) {
            return(StereoConfiguration.Nonchiral);
        }
        configCode = stereo.charAt(stereo.length() - 1);
        if(configCode == StereoD) {
            return(StereoConfiguration.Dexter);
        } else if(configCode == StereoL) {
            return(StereoConfiguration.Laevus);
        } else {
            return(StereoConfiguration.Unknown);
        }
    }
    
    /**
     * Delete all non-chiral or unknown positions from a stereocode string
     * @param stereo
     * @return
     */
    public static String getChiralOnlyStereoString(String stereo) {
        String stereoOut = "";
        for(int i = 0; i < stereo.length(); i++) {
            char symbol = stereo.charAt(i);
            if(symbol == StereoD || symbol == StereoL || symbol == StereoXD || symbol == StereoXL) {
                stereoOut += symbol;
            }
        }
        return(stereoOut);
    }
    
    public static String maskAnomerInStereoString(String stereo, Basetype bt) throws ResourcesDbException {
        if(stereo.length() != bt.getSize()) {
            throw new ResourcesDbException("stereostring length (" + stereo.length() + ") does not match basetype size (" + bt.getSize() + ")");
        }
        if(bt.getRingStart() > 0 && bt.getRingStart() <= bt.getSize()) {
            stereo = Stereocode.setPositionInStereoString(stereo, Stereocode.StereoN, bt.getRingStart());
        }
        return stereo;
    }
    
    /**
     * Get the correct basetype template for a monosaccharide based on its stereocode string.
     * If the template encodes a monosaccharide smaller than the given one (due to loss of stereochemistry), it is returned in the SUBTYPESTR field of the HashMap and the BASETYPESTR field contains the superclass template.
     * Otherwise, the BASETYPESTR field contains the template and the SUBTYPESTR field is null.
     * @param stereo The stereocode of the monosaccharide
     * @param ms The monosaccharide which the stereocode belongs to
     * @param container a BasetypeTemplateContainer to get the templates
     * @return HashMap with 2 fields, addressed by the labels Stereocode.BASETYPESTR and Stereocode.SUBTYPESTR 
     */
    public static HashMap<String, BasetypeTemplate> getBasetypeFromStereoString(String stereo, Monosaccharide ms, BasetypeTemplateContainer container) throws ResourcesDbException {
        if(stereo.length() == ms.getSize()/* && ms.getRingStart() > 0 && ms.getRingStart() <= ms.getSize()*/) {
            //*** make sure configuration of anomeric is not taken into account for basetype determination ***
            stereo = ms.getStereoStrWithoutAnomeric();
        }
        stereo = getChiralOnlyStereoString(stereo);
        if(stereo.length() > 4) {
            stereo = stereo.substring(0, 4);
        }
        HashMap<String, BasetypeTemplate> resultMap = new HashMap<String, BasetypeTemplate>();
        BasetypeTemplate msTmpl = container.getBasetypeTemplateByStereoString(stereo);
        if(msTmpl == null) {
            throw new MonosaccharideException("Cannot get basetype from stereocode " + stereo);
        }
        if(msTmpl.getSize() == ms.getSize()) {
            resultMap.put(Stereocode.BASETYPESTR, msTmpl);
            resultMap.put(Stereocode.SUBTYPESTR, null);
        } else {
            resultMap.put(Stereocode.BASETYPESTR, container.getSuperclassTemplateBySize(ms.getSize()));
            resultMap.put(Stereocode.SUBTYPESTR, msTmpl);
        }
        return(resultMap);
    }
    
    /**
     * Get the basetype for a monosaccharide (or for a monosaccharide subpart) based on its stereocode string.
     * In contrast to getBasetypeFromStereocode() it is not checked if the determined basetype corresponds to the size of the monosaccharide, i.e. no basetype / subtype are returned.
     * The anomeric must not be included in the stereocode.
     * @param stereo The stereocode to be checked
     * @param container a BasetypeTemplateContainer to get the needed templates
     * @return The MonosaccharideTemplate corresponding to the given stereocode string.
     * @throws ResourcesDbException in case no template matches the stereocode string.
     */
    public static BasetypeTemplate getBasetypeFromStereoStringNoSizecheck(String stereo, BasetypeTemplateContainer container) throws ResourcesDbException {
        stereo = getChiralOnlyStereoString(stereo);
        if(stereo.length() > 4) {
            stereo = stereo.substring(0, 4);
        }
        BasetypeTemplate msTmpl = container.getBasetypeTemplateByStereoString(stereo);
        return(msTmpl);
    }
    
    /** 
     * Get the root templates for a given stereocode string.
     * "root templates" are monosaccharide templates, from which a monosaccharide can be derived (can be more than one in case of loss of stereochemistry)
     * @param stereo Stereocode (stereo-relevant positions (2 to n-1) only)
     * @param container a BasetypeTemplateContainer to get the needed templates
     * @return ArrayList of Monosaccharide Templates which match the stereocode
     * @throws ResourcesDbException in case template lists are not set
     */
    public static ArrayList<BasetypeTemplate> getRootTemplatesByStereoString(String stereo, BasetypeTemplateContainer container) throws ResourcesDbException {
        ArrayList<BasetypeTemplate> rootList = new ArrayList<BasetypeTemplate>();
        
        //*** get stereorelevant positions and replace non-chiral positions by dots for regular expression matching: ***
        stereo.replaceAll("[" + StereoX + StereoN + "]", ".");
        
        //*** find templates with matching stereocodes: ***
        String stereoDL = changeDLinStereoString(stereo);
        ArrayList<String> basetypeList = container.getBasetypeListSpecific();
        for(int i = 0; i < basetypeList.size(); i++) {
            BasetypeTemplate tmpl = container.getBasetypeTemplateByName(basetypeList.get(i));
            if(tmpl.getStereocode().matches(stereo) || tmpl.getStereocode().matches(stereoDL)) {
                rootList.add(tmpl);
            }
        }
        return(rootList);
    }
    
    /** 
     * Expand a stereocode string that contains only the chiral positions to a stereocode string representing the entire monosaccharide.
     * Nonchiral positions are inserted into the given stereocode string.
     * @param stereo Stereocode with chiral positions only. In case of ring forms, the anomeric must NOT be contained in the stereocode.
     * @param ms Monosaccharide, to which the stereocode string belongs
     * @return full stereocode string of the monosaccharide (positions 1 to n), chirality resulting from anomeric is not set yet
     */
    public static String expandChiralonlyStereoString(String stereo, Monosaccharide ms) throws MonosaccharideException {
        String stereoOut = "";
        stereoOut += StereoConfiguration.Nonchiral.getStereosymbol(); //*** position 1 is non-chiral by default (if applicable, chirality resulting from anomeric is set later) ***
        if(ms.getRingStart() == 2) {
            stereoOut += StereoConfiguration.Nonchiral.getStereosymbol();
        }
        stereoOut += stereo;
        stereoOut += StereoConfiguration.Nonchiral.getStereosymbol();

        ArrayList<Integer> stereoLossPos = ms.getStereolossPositions(); //*** (positions list is already sorted, therefore no further sorting is necessary here) ***
        if(stereoLossPos.contains(new Integer(0))) { //*** no stereocode can be built if a position of a loss of stereochemistry is unknown ***
            throw new MonosaccharideException("Cannot assign stereochemistry due to unknown position of a modification that causes loss of stereochemistry");
        }
        for(int i = 0; i < stereoLossPos.size(); i++) {
            int position = stereoLossPos.get(i);
            if(position <= 0) {
                throw new MonosaccharideException("Error in setting stereocode: position out of range (" + position + ").");
            }
            if(position == ms.getRingStart() && position == 2) {
                continue; //*** position already set above ***
            }
            if(position == 1 || position == ms.getSize()) {
                continue; //*** first and last position are always non-chiral ***
            }
            if(position > stereoOut.length()) {
                throw new MonosaccharideException("Error in setting stereocode: position out of range (" + position + ").");
            }
            stereoOut = stereoOut.substring(0, position - 1) + StereoConfiguration.Nonchiral.getStereosymbol() + stereoOut.substring(position - 1);
        }
        return(stereoOut);
    }
    
    /** 
     * Mark the nonchiral positions in a stereocode string.
     * In contrast to "expandChiralonlyStereocode()" this method overwrites the stereochemistry of existing positions.
     * It can be used to mark nonchiral positions in a residue like a-D-4-deoxy-Glcp (i.e. the basetype is a specific type or a trivial name)
     * @param stereo StereoString to be processed, must cover the full monosaccharide (positions 1 to n)
     * @param ms Monosaccharide, from which the information about nonchiral positions is received
     * @return stereocode string with nonchiral positions marked
     * @throws MonosaccharideException 
     */
    public static String markNonchiralPositionsInStereoString(String stereo, Monosaccharide ms) throws MonosaccharideException {
        if(stereo.length() != ms.getSize()) {
            throw new MonosaccharideException("Size mismatch error in markNonchiralPositions: " + stereo + "/" + ms.getSize());
        } else {
            ArrayList<Integer> stereoLossPos = ms.getStereolossPositions();
            char[] stereoChars = stereo.toCharArray();
            for(int i = 0; i < stereoLossPos.size(); i++) {
                int position = stereoLossPos.get(i);
                if(position == 0) { //*** unknown position ***
                    System.out.println("Warning: skipped unknown stereoloss position (markNonchiralPositions).");
                    continue;
                }
                if(position < 0 || position > ms.getSize()) {
                    throw new MonosaccharideException("Error in markNonchiralPositions: position out of range: " + position);
                }
                stereoChars[position - 1] = StereoConfiguration.Nonchiral.getStereosymbol();
            }
            stereo = String.valueOf(stereoChars);
        }
        return(stereo);
    }
    
    /**
     * Generate a stereocode string for a superclass residue
     * (i.e. a stereocode string that consists of unknown positions only)
     * @param size the size of the residue
     * @return the stereo string, e.g. for a hexose "0xxxx0";
     */
    public static String getSuperclassStereostring(int size) {
        String outStr = "";
        if(size > 0) {
            outStr += Stereocode.StereoN;
        }
        for(int i = 0; i < size - 2; i++) {
            outStr += Stereocode.StereoX;
        }
        if(size > 1) {
            outStr += Stereocode.StereoN;
        }
        return outStr;
    }
    
    /** 
     * Returns an ArrayList containing the configurations represented by this stereocode.
     * Each element of the List represents the stereochemistry of one single monosaccharide backbone atom
     * @return ArrayList of configurations
     * @throws ResourcesDbException 
     */
    public ArrayList<StereoConfiguration> toConfigurationList() throws ResourcesDbException {
        ArrayList<StereoConfiguration> configList = new ArrayList<StereoConfiguration>();
        for(int i = 0; i < getStereoStr().length(); i++) {
            //try {
                configList.add(StereoConfiguration.forStereosymbol(getStereoStr().charAt(i)));
            //} catch(MonosaccharideException me) { //*** stereoString contains illegal symbols ***
            //    System.err.println("Cannot assign configuration to stereocode symbol (" + getStereoStr().substring(i, i + 1) + ")");
            //    configList.add(StereoConfiguration.Unknown);
            //}
        }
        return(configList);
    }
    
    /**
     * Set a single position in a stereocode string.
     * @param stereo: The stereocode string to be altered.
     * @param posStr: The configuration symbol to be assigned to the given position (must be a valid configuration symbol of size 1).
     * @param pos: The position at which the configuration symbol is set.
     * @return The altered stereocode string.
     * @throws ResourcesDbException in case "pos" is not a position covered by the stereocode string or "posStr" is not a valid configuration symbol.
     */
    public static String setPositionInStereoString(String stereo, char posChar, int pos) throws ResourcesDbException {
        if(pos <= 0 || pos > stereo.length()) {
            throw new MonosaccharideException("Stereocode.setPosition: position out of range (" + (pos + 1) + ")");
        }
        if(StereoConfiguration.forStereosymbol(posChar) == null) {
            throw new MonosaccharideException("Stereocode.setPosition: Symbol to be set is not a valid configuration symbol: " + posChar);
        }
        if(pos < stereo.length()) {
            stereo = stereo.substring(0, pos - 1) + posChar + stereo.substring(pos);
        } else {
            stereo = stereo.substring(0, pos - 1) + posChar;
        }
        return(stereo);
    }
    
    public static String setPositionInStereoString(String stereo, StereoConfiguration posConf, int pos) throws ResourcesDbException {
        return Stereocode.setPositionInStereoString(stereo, posConf.getStereosymbol(), pos);
    }
    
    public static StereoConfiguration getPositionFromStereoString(String stereo, int pos) throws ResourcesDbException {
        if(pos < 1 || pos > stereo.length()) {
            throw new MonosaccharideException("position out of range: " + pos);
        }
        return(StereoConfiguration.forStereosymbol(stereo.charAt(pos - 1)));
    }
    
    public StereoConfiguration getPositionConfiguration(int pos) throws ResourcesDbException {
        return getPositionFromStereoString(this.getStereoStr(), pos);
    }
    
    public static ArrayList<String> getBasetypelistFromStereocode(Monosaccharide ms, BasetypeTemplateContainer container) throws ResourcesDbException {
        return(getBasetypelistFromStereoString(ms.getStereoStr(), ms, container));
    }
    
    public static ArrayList<String> getBasetypelistFromStereoString(String stereo, Monosaccharide ms, BasetypeTemplateContainer container) throws ResourcesDbException {
        if(ms.getRingStart() > 1) { //*** anomeric center not at position1 => mask potential anomeric stereochemistry ***
            stereo = Stereocode.setPositionInStereoString(stereo, StereoConfiguration.Nonchiral.getStereosymbol(), ms.getRingStart());
        }
        stereo = stereo.substring(1); //*** remove position1 (always nonchiral or anomeric) ***
        stereo = stereo.replaceAll("" + StereoConfiguration.Nonchiral.getStereosymbol(), "");
        ArrayList<String> basetypeList = new ArrayList<String>();
        if(!stereo.replaceAll("" + StereoConfiguration.Unknown.getStereosymbol(), "").equals("")) { //*** residue is not just a superclass ***
            if(stereo.indexOf(StereoConfiguration.Unknown.getStereosymbol()) != -1) {
                throw new MonosaccharideException("Stereocode string contains unknown positions.");
            }
            String basetypeStr;
            while(stereo.length() > 0) {
                if(stereo.length() > 4) {
                    basetypeStr = container.getBasetypeTemplateByStereoString(stereo.substring(0, 4)).getBaseName();
                    basetypeStr = Stereocode.getConfigurationFromStereoString(stereo.substring(0, 4)).getSymbol() + "-" + basetypeStr;
                    stereo = stereo.substring(4);
                } else {
                    basetypeStr = container.getBasetypeTemplateByStereoString(stereo).getBaseName();
                    basetypeStr = Stereocode.getConfigurationFromStereoString(stereo).getSymbol() + "-" + basetypeStr;
                    stereo = "";
                }
                basetypeList.add(0, basetypeStr);
            }
        }
        return(basetypeList);
    }
    
    public static ArrayList<String> prepareStereocodeForBasetypeDetermination(Monosaccharide ms) throws ResourcesDbException {
        ArrayList<String> stereoList = new ArrayList<String>();
        String stereo = ms.getStereoStr();
        if(ms.getRingStart() > 1) { //*** anomeric center not at position1 => mask potential anomeric stereochemistry ***
            stereo = Stereocode.setPositionInStereoString(stereo, StereoConfiguration.Nonchiral.getStereosymbol(), ms.getRingStart());
        }
        stereo = stereo.substring(1); //*** remove position1 (always nonchiral or anomeric) ***
        stereo = stereo.replaceAll("" + StereoConfiguration.Nonchiral.getStereosymbol(), ""); //*** remove nonchiral positions ***
        if(!stereo.replaceAll("" + StereoConfiguration.Unknown.getStereosymbol(), "").equals("")) { //*** residue is not just a superclass ***
            while(stereo.length() > 0) {
                if(stereo.length() > 4) {
                    stereoList.add(0, stereo.substring(0, 4));
                    stereo = stereo.substring(4);
                } else {
                    stereoList.add(0, stereo);
                    stereo = "";
                }
            }
        }
        return stereoList;
    }
    
    /**
     * Check, if this stereocode contains an uncertain position
     * @return true, if a position other than D, L or non-chiral is found
     */
    public boolean hasUncertainPosition() {
        return Stereocode.stereoStringHasUncertainPosition(this.getStereoStr());
    }
    
    /**
     * Check, if a stereo String has an uncertain position
     * @param stereo the stereo String to check
     * @return true, if a position other than D, L or non-chiral is found
     */
    public static boolean stereoStringHasUncertainPosition(String stereo) {
        if(!stereo.matches("^[" + Stereocode.StereoD + Stereocode.StereoL + Stereocode.StereoN + "]*$")) {
            return true;
        }
        return false;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************
    
    public String toString() {
        return(this.stereoStr);
    }
    
    public Stereocode clone() {
        return new Stereocode(this.getStereoStr());
    }
    
}
