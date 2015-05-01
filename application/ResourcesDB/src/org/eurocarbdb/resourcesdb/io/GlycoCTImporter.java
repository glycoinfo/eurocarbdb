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
import org.eurocarbdb.resourcesdb.glycoconjugate_derived.*;
import org.eurocarbdb.resourcesdb.monosaccharide.*;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.TemplateContainer;

public class GlycoCTImporter extends StandardImporter implements MonosaccharideImporter {

    private String substitutionStr;
    private char substitutionToken = ' ';
    private int substitutionParsingPosition = -1;
    private int substitutionStartPosition = -1;
    
    public static final String BASETYPE_SUBST_SEPARATOR = "||";

    //*****************************************************************************
    //*** constructors: ***********************************************************
    //*****************************************************************************
    
    public GlycoCTImporter(GlycanNamescheme scheme, Config confObj, TemplateContainer container) {
        super(scheme, confObj, container);
        this.init();
    }
    
    public GlycoCTImporter(GlycanNamescheme scheme, Config confObj) {
        this(scheme, confObj, null);
    }
    
    public GlycoCTImporter(GlycanNamescheme scheme) {
        this(scheme, null);
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
        setInputName(name);
        setFoundMs(false);
        if(GlycanNamescheme.MONOSACCHARIDEDB.equals(this.getNamescheme())) {
            String basetypeStr = null;
            String substitutionsStr = null;
            int separatorPos = name.indexOf(GlycoCTImporter.BASETYPE_SUBST_SEPARATOR);
            if(separatorPos >= 0) {
                basetypeStr = name.substring(0, separatorPos);
                this.substitutionStartPosition = separatorPos + 2;
                substitutionsStr = name.substring(this.substitutionStartPosition);
            } else {
                basetypeStr = name;
            }
            this.parseBasetypeString(basetypeStr, ms);
            ms.setCheckPositionsOnTheFly(false);
            ArrayList<Substitution> substList = this.parseSubstitutionsString(substitutionsStr);
            for(Substitution subst: substList) {
                ms.addSubstitution(subst);
            }
        } else if(GlycanNamescheme.GLYCOCT.equals(this.getNamescheme())) {
            this.parseBasetypeString(name, ms);
        } else {
            throw new MonosaccharideException("Namescheme " + this.getNamescheme() + " not supported in GlycoCTImporter.");
        }
        if(this.getConfig().isBuildMsDerivedData()) {
            MonosaccharideDataBuilder.buildDerivativeData(ms, this.getTemplateContainer());
        }
        MonosaccharideValidation.checkMonosaccharideConsistency(ms, this.getTemplateContainer(), this.getConfig());
        this.setFoundMs(true);
    }
    
    private void parseBasetypeString(String basetypeName, Monosaccharide ms) throws ResourcesDbException {
        EcdbResidueParser parser = new EcdbResidueParser(this.getNamescheme());
        EcdbMonosaccharide ecdbMs = parser.createEcdbResidue(basetypeName);
        BasetypeConversion.eurocarbdbToMsdb(ecdbMs, ms);
        //ms.buildDerivativeData();
    }
    
    private ArrayList<Substitution> parseSubstitutionsString(String nameStr) throws ResourcesDbException {
        if(nameStr != null && nameStr.length() > 0) {
            if(!nameStr.endsWith("|")) {
                nameStr += "|";
            }
        }
        ArrayList<Substitution> substList = new ArrayList<Substitution>();
        if(nameStr != null && nameStr.length() > 0) {
            int separatorPos = nameStr.indexOf("|");
            while(separatorPos >= 0) {
                String substStr = nameStr.substring(0, separatorPos);
                Substitution subst = this.parseSingleSubstitutionString(substStr);
                if(subst != null) {
                    substList.add(subst);
                }
                nameStr = nameStr.substring(separatorPos + 1);
                this.substitutionStartPosition += separatorPos + 1;
                separatorPos = nameStr.indexOf("|");
            }
        }
        return substList;
    }
    
    /**
     * @param name a substitution in a format like "(2d:1)n-acetyl"
     * @return
     * @throws NameParsingException
     */
    private Substitution parseSingleSubstitutionString(String name) throws ResourcesDbException {
        Substitution subst = null;
        if(name != null && name.length() > 0) {
            this.substitutionStr = name;
            this.substitutionParsingPosition = -1;
            subst = new Substitution();
            
            //*** (1): parse linkage: ***
            
            //*** opening bracket: ***
            this.nextSubstitutionToken();
            if(this.substitutionToken != '(') {
                throw new NameParsingException("Unexpected token: '" + this.substitutionToken + "'", this.getInputName(), this.substitutionStartPosition + this.substitutionParsingPosition);
            }
            
            //*** position at monosaccharide: ***
            this.nextSubstitutionToken();
            ArrayList<Integer> msPositionList = this.parsePositionsString();
            subst.setPosition1(msPositionList);
            //*** linkage type at monosaccharide: ***
            subst.setLinkagetype1(this.parseLinkageType());
            
            //*** separating colon: ***
            this.nextSubstitutionToken();
            if(this.substitutionToken != ':') {
                throw new NameParsingException("Unexpected token: '" + this.substitutionToken + "'", this.getInputName(), this.substitutionStartPosition + this.substitutionParsingPosition);
            }
            
            //*** position at substituent: ***
            this.nextSubstitutionToken();
            ArrayList<Integer> substPositionList = this.parsePositionsString();
            subst.setSubstituentPosition1(substPositionList);
            
            //*** check for second linkage (in case of bivalent substituents): ***
            if(this.substitutionToken == ',') {
                //*** position at monosaccharide: ***
                ArrayList<Integer> msPositionList2 = this.parsePositionsString();
                subst.setPosition2(msPositionList2);
                
                //*** linkage type at monosaccharide: ***
                subst.setLinkagetype2(this.parseLinkageType());
                
                //*** separating colon: ***
                if(this.getNextSubstitutionToken() != ':') {
                    throw new NameParsingException("Unexpected token: '" + this.substitutionToken + "'", this.getInputName(), this.substitutionStartPosition + this.substitutionParsingPosition);
                }
                
                //*** position at substituent: ***
                ArrayList<Integer> substPositionList2 = this.parsePositionsString();
                subst.setSubstituentPosition2(substPositionList2);
            }
            if(this.substitutionToken != ')') {
                throw new NameParsingException("Unexpected token: '" + this.substitutionToken + "'", this.getInputName(), this.substitutionStartPosition + this.substitutionParsingPosition);
            }
            
            //*** (2): parse substituent name: ***
            this.nextSubstitutionToken();
            int substNameStart = this.substitutionParsingPosition;
            String substName = this.parseSubstituentName();
            SubstituentTemplate substTmpl = this.getTemplateContainer().getSubstituentTemplateContainer().forName(GlycanNamescheme.GLYCOCT, substName);
            if(substTmpl == null) {
                throw new NameParsingException("unknown substituent", this.getInputName(), this.substitutionStartPosition + substNameStart);
            }
            subst.setTemplate(substTmpl);
        }
        return subst;
    }
    
    private ArrayList<Integer> parsePositionsString() throws NameParsingException {
        ArrayList<Integer> posList = new ArrayList<Integer>();
        int pos = this.parseNumber();
        posList.add(new Integer(pos));
        while(this.substitutionToken == '/') {
            this.nextSubstitutionToken();
            pos = this.parseNumber();
            posList.add(new Integer(pos));
        }
        return posList;
    }
    
    private LinkageType parseLinkageType() throws NameParsingException {
        LinkageType linktype = null;
        try {
            linktype = LinkageType.forName(this.substitutionToken);
        } catch(GlycoconjugateException ge) {
            throw new NameParsingException("Cannot get linkage type from substitution string", this.getInputName(), this.substitutionStartPosition + this.substitutionParsingPosition);
        }
        return linktype;
    }
    
    private String parseSubstituentName() {
        String outStr = "";
        while(this.substitutionToken != '|') {
            outStr += this.substitutionToken;
            try {
                this.nextSubstitutionToken();
            } catch(NameParsingException npe) {
                break;
            }
        }
        return outStr;
    }
    
    private int parseNumber() throws NameParsingException {
        int result = 0;
        int digit = 0;
        if(this.substitutionToken == '0') {
            this.nextSubstitutionToken();
            return 0;
        }
        //*** ( "1" | ... | "9" ) ***
        digit = (int) this.substitutionToken;
        if (digit < 49 || digit > 57) {
            throw new NameParsingException("number expected ", this.getInputName(), this.substitutionStartPosition + this.substitutionParsingPosition);
        }
        result = digit - 48;
        //*** ( "1" | ... | "9" ) { "0" | "1" | ... | "9" } ***
        this.nextSubstitutionToken();
        digit = (int) this.substitutionToken;
        //*** ( "1" | ... | "9" ) { "0" | "1" | ... | "9" } ***
        while (digit > 47 && digit < 58) {
            result = (result * 10) + (digit - 48);
            this.nextSubstitutionToken();
            digit = (int) this.substitutionToken;
        }
        return result;
    }
    
    private boolean hasNextSubstitutionToken() {
        if(this.substitutionStr != null && this.substitutionStr.length() > this.substitutionParsingPosition + 1) {
            return true;
        }
        return false;
    }
    
    private void nextSubstitutionToken() throws NameParsingException {
        if(this.hasNextSubstitutionToken()) {
            this.substitutionParsingPosition ++;
            this.substitutionToken = this.substitutionStr.charAt(this.substitutionParsingPosition);
        } else {
            throw new NameParsingException("unexpected end of string", this.getInputName(), this.substitutionStartPosition + this.substitutionParsingPosition);
        }
    }
    
    private char getNextSubstitutionToken() throws NameParsingException {
        this.nextSubstitutionToken();
        return this.substitutionToken;
    }
    
    //*****************************************************************************
    //*** other methods: **********************************************************
    //*****************************************************************************

    public void init() {
        setInputName("");
        setFoundMs(false);
    }
    
}
