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

import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.ResourcesDbObject;
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.LinkageType;
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;
import org.eurocarbdb.resourcesdb.template.TrivialnameTemplate;

/**
* A basic MonosaccharideImporter class, providing a number of fields and methods that are needed in most importers.
*
* @author Thomas Luetteke
*/
public abstract class StandardImporter extends ResourcesDbObject {

    private String inputName;
    private boolean foundMs;
    private int parsingPosition = 0;
    
    private GlycanNamescheme namescheme;
    
    private String tmpStereocode;
    private TrivialnameTemplate detectedTrivialname;

    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public StandardImporter(GlycanNamescheme scheme, Config confObj) {
        this(null, null, null);
    }
    
    public StandardImporter(GlycanNamescheme scheme, Config confObj, TemplateContainer container) {
        this.namescheme = scheme;
        this.setConfig(confObj);
        this.setTemplateContainer(container);
        this.inputName = null;
        this.foundMs = false;
    }
    
    public StandardImporter() {
        this(null, null);
    }

    //*****************************************************************************
    //*** getters/setters: ********************************************************
    //*****************************************************************************

    public GlycanNamescheme getNamescheme() {
        return this.namescheme;
    }

    public void setNamescheme(GlycanNamescheme scheme) {
        this.namescheme = scheme;
    }

    /**
     * @return the foundMs
     */
    public boolean isFoundMs() {
        return this.foundMs;
    }

    /**
     * @param foundMs the foundMs to set
     */
    protected void setFoundMs(boolean foundMs) {
        this.foundMs = foundMs;
    }

    /**
     * @return the inputName
     */
    public String getInputName() {
        return this.inputName;
    }

    /**
     * @param inputName the inputName to set
     */
    public void setInputName(String inputName) {
        this.inputName = inputName;
    }
    
    /**
     * @return the tmpStereocode
     */
    protected String getTmpStereocode() {
        return this.tmpStereocode;
    }

    /**
     * @param tmpStereocode the tmpStereocode to set
     */
    protected void setTmpStereocode(String tmpStereocode) {
        this.tmpStereocode = tmpStereocode;
    }

    /**
     * Get the current parsing position
     * @return the current parsing position
     */
    protected int getParsingPosition() {
        return this.parsingPosition;
    }
    
    /**
     * Set the current parsing position
     * @param ppos the position to set
     */
    protected void setParsingPosition(int ppos) {
        this.parsingPosition = ppos;
    }
    
    /**
     * Increase the current parsing position by 1 
     */
    protected void increaseParsingPosition() {
        this.parsingPosition ++;
    }
    
    /**
     * Increase the current parsing position by a given value
     * @param steps the value to add to the parsing position
     */
    protected void increaseParsingPosition(int steps) {
        this.parsingPosition += steps;
    }
    
    public TrivialnameTemplate getDetectedTrivialname() {
        return detectedTrivialname;
    }

    public void setDetectedTrivialname(TrivialnameTemplate detectedTrivialname) {
        this.detectedTrivialname = detectedTrivialname;
    }

    //*****************************************************************************
    //*** parsing methods: ********************************************************
    //*****************************************************************************
    
    /**
     * Returns the character that is in the <code>inputName</code> at the current <code>ParsingPosition</code>
     * @throws NameParsingException in case the current <code>parsingPosition</code> is out of range
     * @return the value of <code>getInputName().charAt(getParsingPosition())</code>
     */
    protected char getCurrentToken() throws NameParsingException {
        if(this.getInputName().length() <= this.getParsingPosition()) {
            throw new NameParsingException("unexpected end of string", this.getInputName(), this.getParsingPosition());
        }
        return this.getInputName().charAt(this.getParsingPosition());
    }
    
    /**
     * Get a Substring of <code>inputName</code> starting at the current <code>parsingPosition</code>.
     * The <code>parsingPosition</code> is not changed by this method.
     * @param length the length of the substring to return
     * @throws NameParsingException in case (part of) the resulting substring is out of range of the <code>inputName</code>
     * @return the Substring of <code>inputName</code> name starts at <code>parsingPosition</code> and has the given length
     */
    protected String getCurrentSubstring(int length) throws NameParsingException {
        if(this.getInputName().length() < this.getParsingPosition() + length) {
            throw new NameParsingException("unexpected end of string", this.getInputName(), this.getParsingPosition());
        }
        return this.getInputName().substring(this.getParsingPosition(), this.getParsingPosition() + length);
    }
    
    protected boolean hasCurrentSubstring(String cmpString) {
        try {
            return this.getCurrentSubstring(cmpString.length()).equals(cmpString);
        } catch(NameParsingException npe) {
            return false;
        }
    }
    
    /**
     * Increases the current <code>parsingPosition</code> and returns the character that is in the <code>inputName</code> at the resulting <code>ParsingPosition</code>
     * @throws NameParsingException in case the current <code>parsingPosition</code> is out of range
     * @return the character following the current <code>parsingPosition</code>
     */
    protected char getNextToken() throws NameParsingException {
        this.increaseParsingPosition();
        if(this.getInputName().length() <= this.getParsingPosition()) {
            throw new NameParsingException("unexpected end of string", this.getInputName(), this.getParsingPosition());
        }
        return this.getInputName().charAt(this.getParsingPosition());
    }
    
    /**
     * Check, if the <code>inputName</code> has a character after the current <code>parsingPosition</code>
     * @return
     */
    protected boolean hasNextToken() {
        return this.getInputName() != null && this.getParsingPosition() + 1 < this.getInputName().length();
    }
    
    /**
     * Get the number of remaining tokens in <code>inputName</code>, starting with the current parsing position
     * @return
     */
    protected int countRemainingTokens() {
        if(this.getInputName() == null) {
            return -1;
        }
        return this.getInputName().length() - this.getParsingPosition();
    }
    
    /**
     * Check, if the <code>inputName</code> has a character at the current <code>parsingPosition</code>
     * (i.e. if the current <code>parsingPosition</code> is within the length range of <code>inputName</code>)
     * @return true, if 0 <= parsingPosition < inputName.length
     */
    protected boolean hasCurrentToken() {
        return this.getInputName() != null && this.getParsingPosition() < this.getInputName().length() && this.getParsingPosition() >= 0;
    }
    
    protected int parseIntNumber() throws NameParsingException {
        return parseIntNumber(false);
    }

    protected int parseIntNumber(boolean allowWildcard) throws NameParsingException {
        if(allowWildcard && this.getCurrentToken() == '?') {
            this.increaseParsingPosition();
            return 0;
        }
        String numberString = "";
        while(this.hasCurrentToken() && Character.isDigit(this.getCurrentToken())) {
            numberString += this.getCurrentToken();
            this.increaseParsingPosition();
        }
        if(numberString.length()==0) {
            throw new NameParsingException("number expected ", this.getInputName(), this.getParsingPosition());
        }
        return Integer.parseInt(numberString);
    }
    
    protected ArrayList<Integer> parseIntNumberList() throws NameParsingException {
        return parseIntNumberList(false);
    }
    
    protected ArrayList<Integer> parseIntNumberList(boolean allowWildcard) throws NameParsingException {
        ArrayList<Integer> outList = new ArrayList<Integer>();
        int num = parseIntNumber(allowWildcard);
        outList.add(new Integer(num));
        while(this.hasCurrentToken() && this.getCurrentToken() == ',') {
            this.increaseParsingPosition();
            num = parseIntNumber(allowWildcard);
            outList.add(new Integer(num));
        }
        return outList;
    }
    
    /**
     * Check, if the <code>inputName</code> contains a trivial name at the current <code>parsingPosition</code>.
     * If more than one matching trivial names are found (e.g. "G" and "GN" in CFG notation), the longest one is retured.
     * The <code>parsingPosition</code> is not changed by this method.
     * @return the detected trivial name or null if no match is found 
     * @throws ResourcesDbException
     */
    protected String checkForTrivialname() throws ResourcesDbException {
        String detectedTrivName = null;
        for(String trivName : this.getTemplateContainer().getTrivialnameTemplateContainer().getTrivialnameBasetypeList(this.getNamescheme())) {
            if(detectedTrivName != null && detectedTrivName.length() >= trivName.length()) {
                continue;
            }
            if(this.getInputName().length() - this.getParsingPosition() >= trivName.length()) {
                if(this.getInputName().substring(this.getParsingPosition(), this.getParsingPosition() + trivName.length()).equals(trivName)) {
                    detectedTrivName = trivName;
                }
            }
        }
        return detectedTrivName;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************

    /**
     * Initialize a substitution with data parsed from a residue name and add it to a Monosaccharide
     * @param ms the Monosaccharide, to which the substition shall be added
     * @param parsedName the substituent name as it is present in the residue
     * @param posList a list of subsituent position as parsed from the residue
     * @throws ResourcesDbException
     */
    protected void addParsedSubstitution(Monosaccharide ms, String parsedName, ArrayList<Integer> posList) throws ResourcesDbException {
        SubstituentTemplate substTmpl = this.getTemplateContainer().getSubstituentTemplateContainer().forResidueIncludedName(this.getNamescheme(), parsedName);
        if(substTmpl == null) {
            throw new ResourcesDbException("internal error: cannot get template for substituent " + parsedName);
        }
        
        LinkageType linktype1 = substTmpl.getLinkageTypeBySubstituentName(this.getNamescheme(), parsedName);
        if(substTmpl.getMaxValence() == 1 || (posList.size() == 1 && substTmpl.getMinValence() == 1)) {
            for(Integer pos : posList) {
                Substitution subst = new Substitution();
                subst.setSubstitution(substTmpl, pos, linktype1);
                subst.setSourceName(parsedName);
                ms.addSubstitution(subst);
            }
        } else if(substTmpl.getMaxValence() == 2) {
            if(posList.size() != 2) {
                throw new NameParsingException("Only one position given for divalent substituent " + parsedName, this.getInputName(), this.getParsingPosition());
            }
            LinkageType linktype2 = substTmpl.getLinkageType2BySubstituentName(this.getNamescheme(), parsedName);
            Substitution subst = new Substitution();
            subst.setDivalentSubstitution(substTmpl, posList.get(0), linktype1, substTmpl.getDefaultLinkingPosition1(), posList.get(1), linktype2, substTmpl.getDefaultLinkingPosition2());
            subst.setSourceName(parsedName);
            ms.addSubstitution(subst);
        }
    }
    
    public void init() {
        this.setInputName(null);
        this.setParsingPosition(0);
        this.setFoundMs(false);
        this.setTmpStereocode(null);
        this.setDetectedTrivialname(null);
    }
    
}
