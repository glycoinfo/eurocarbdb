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
package org.eurocarbdb.resourcesdb.glycoconjugate_derived;

import java.util.ArrayList;

import org.eurocarbdb.resourcesdb.*;
import org.eurocarbdb.resourcesdb.io.NameParsingException;

/*import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
 import org.eurocarbdb.MolecularFramework.sugar.Anomer;
 import org.eurocarbdb.MolecularFramework.sugar.BaseType;
 import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
 import org.eurocarbdb.MolecularFramework.sugar.Modification;
 import org.eurocarbdb.MolecularFramework.sugar.ModificationType;
 import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
 import org.eurocarbdb.MolecularFramework.sugar.Superclass; */

/**
* residue              ::= "b" ":" <anomer> "-" <config> "-" <superclass> "-" <ring_start> ":" <ring_end>
*                          | "s" <substituent>  
* @author Logan
*
*/
public class EcdbResidueParser {

    private char m_cToken = ' ';
    private String m_strMS = "";
    private int m_iPosition = -1;
    
    private GlycanNamescheme namescheme;
    
    public EcdbResidueParser(GlycanNamescheme scheme) {
        this.setNamescheme(scheme);
    }
    
    public GlycanNamescheme getNamescheme() {
        return namescheme;
    }

    public void setNamescheme(GlycanNamescheme namescheme) {
        this.namescheme = namescheme;
    }

    private void nextToken() throws NameParsingException {
        //*** next character ***
        m_iPosition++;
        try {
            //*** get next token ***
            this.m_cToken = this.m_strMS.charAt(this.m_iPosition);
        } catch (IndexOutOfBoundsException e) {
            //*** parsing error ***
            throw new NameParsingException("COMMON000 (unexpected end of residue name) ", this.m_strMS, this.m_iPosition);
        }
        //*** all successfull ***
    }
    
    /**
     * number          ::= "0" | ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
     * @return  number to be parsed 
     * @throws ImportExeption 
     */
    private int number() throws NameParsingException {
        int t_iResult = 0;
        int t_iDigit = 0;
        if (this.m_cToken == '0') {
            // "0"
            this.nextToken();
            return t_iResult;
        }
        // ( "1" | ... | "9" ) 
        t_iDigit = (int) this.m_cToken;
        if (t_iDigit < 49 || t_iDigit > 57) {
            throw new NameParsingException("COMMON004 (number expected) ", this.m_strMS, this.m_iPosition);
        }
        t_iResult = t_iDigit - 48;
        // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
        this.nextToken();
        t_iDigit = (int) this.m_cToken;
        while (t_iDigit > 47 && t_iDigit < 58) {
            t_iResult = (t_iResult * 10) + (t_iDigit - 48);
            // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
            this.nextToken();
            t_iDigit = (int) this.m_cToken;
        }
        return t_iResult;
    }

    /**
     * "b" ":" <anomer> "-" <config> "-" <superclass> "-" <ring_start> ":" <ring_end>
     * | 
     * "s" <substituent>
     * 
     * @return
     * @throws GlycoconjugateException 
     */
    public EcdbMonosaccharide createEcdbResidue(String a_strMS) throws NameParsingException {
        this.m_iPosition = -1;
        this.m_strMS = a_strMS + " ";
        this.nextToken();

        //*** anomer ***
        EcdbAnomer t_objAnomer;
        try {
            t_objAnomer = EcdbAnomer.forName(this.m_cToken);
        } catch(GlycoconjugateException ge) {
            NameParsingException npe = new NameParsingException("Illegal anomeric value", this.m_strMS, this.m_iPosition);
            npe.initCause(ge);
            throw npe;
        }

        this.nextToken();
        if (this.m_cToken != '-') {
            throw new NameParsingException("GLYCOCTC007 (unexpected token: '" + this.m_cToken + "')", this.m_strMS, this.m_iPosition);
        }
        this.nextToken();

        //*** configuration ***
        int t_iMaxPos = this.m_strMS.indexOf(":", this.m_iPosition) - 7;
        ArrayList<EcdbBaseType> t_aConfiguration = new ArrayList<EcdbBaseType>();
        String t_strInformation = "";
        while (this.m_iPosition < t_iMaxPos) {
            t_strInformation = "";
            for (int t_iCounter = 0; t_iCounter < 4; t_iCounter++) {
                t_strInformation += this.m_cToken;
                this.nextToken();
            }
            try {
                t_aConfiguration.add(EcdbBaseType.forName(t_strInformation));
            } catch (Exception e) {
                throw new NameParsingException("GLYCOCTC008 ", this.m_strMS, this.m_iPosition);
            }
            if (this.m_cToken != '-') {
                throw new NameParsingException("GLYCOCTC007 (unexpected token: " + this.m_cToken + ") ", this.m_strMS, this.m_iPosition);
            }
            this.nextToken();
        }

        //*** superclass ***
        t_strInformation = "";
        for (int t_iCounter = 0; t_iCounter < 3; t_iCounter++) {
            t_strInformation += this.m_cToken;
            this.nextToken();
        }
        EcdbSuperclass t_objSuper;
        try {
            t_objSuper = EcdbSuperclass.forName(t_strInformation.toLowerCase());
        } catch (Exception e) {
            throw new NameParsingException("GLYCOCTC009 ", this.m_strMS, this.m_iPosition);
        }
        EcdbMonosaccharide t_objMS;
        try {
            t_objMS = new EcdbMonosaccharide(t_objAnomer, t_objSuper);
            t_objMS.setBaseType(t_aConfiguration);
        } catch(GlycoconjugateException ge) {
            NameParsingException npe = new NameParsingException("Internal error in setting monosaccharide");
            npe.initCause(ge);
            throw npe;
        }
        if (this.m_cToken != '-') {
            throw new NameParsingException("GLYCOCTC007 (unexpected token: "
                    + this.m_cToken + ")", this.m_strMS, this.m_iPosition);
        }
        this.nextToken();

        //*** ring ***
        int t_iRingStart;
        if (this.m_cToken == 'x') {
            t_iRingStart = EcdbMonosaccharide.UNKNOWN_RING;
            this.nextToken();
        } else {
            t_iRingStart = this.number();
        }
        if (this.m_cToken != ':') {
            throw new NameParsingException("GLYCOCTC005 (unexpected token: "
                    + this.m_cToken + ")", this.m_strMS, this.m_iPosition);
        }
        this.nextToken();
        try {
            if (this.m_cToken == 'x') {
                t_objMS.setRing(t_iRingStart, EcdbMonosaccharide.UNKNOWN_RING);
                this.nextToken();
            } else {
                t_objMS.setRing(t_iRingStart, this.number());
            }
        } catch(GlycoconjugateException ge) {
            NameParsingException npe = new NameParsingException("Illegal ring value", this.m_strMS, this.m_iPosition);
            npe.initCause(ge);
            throw npe;
        }

        //*** modifications ***
        while (this.m_cToken == '|') {
            int t_iPosOne;
            Integer t_iPosTwo = null;
            this.nextToken();
            if (this.m_cToken == 'x') {
                t_iPosOne = EcdbModification.UNKNOWN_POSITION;
                this.nextToken();
            } else {
                t_iPosOne = this.number();
            }
            if (this.m_cToken == ',') {
                this.nextToken();
                t_iPosTwo = this.number();
            }
            if (this.m_cToken != ':') {
                throw new NameParsingException(
                        "GLYCOCTC005  (unexpected token: " + this.m_cToken
                                + ")", this.m_strMS, this.m_iPosition);
            }
            this.nextToken();
            int t_iStart = this.m_iPosition;
            this.modification_name();
            EcdbModificationType t_enumMod;
            try {
                t_enumMod = EcdbModificationType.forName(this.m_strMS
                        .substring(t_iStart, this.m_iPosition));
            } catch (Exception e) {
                throw new NameParsingException("GLYCOCTC010", this.m_strMS, this.m_iPosition);
            }
            try {
                EcdbModification t_objModi = new EcdbModification(t_enumMod, t_iPosOne, t_iPosTwo);
                if(t_objModi.getModificationType().isMsdbOnly() && !GlycanNamescheme.MONOSACCHARIDEDB.equals(this.getNamescheme())) {
                    throw new NameParsingException("Modification " + t_objModi.getName() + " is not valid in namescheme " + this.getNamescheme().getNameStr());
                }
                t_objMS.addModification(t_objModi);
            } catch(GlycoconjugateException ge) {
                NameParsingException npe = new NameParsingException("Error in setting modification", this.m_strMS, this.m_iPosition);
                npe.initCause(ge);
                throw npe;
            }
        }
        if (this.m_iPosition != this.m_strMS.length() - 1) {
            System.out.println("position: " + this.m_iPosition + " length: "
                    + this.m_strMS.length());
            throw new NameParsingException("GLYCOCTC005  (unexpected token: "
                    + this.m_cToken + ")", this.m_strMS, this.m_iPosition);
        }
        return t_objMS;
    }

    /**
     * 
     */
    private void modification_name() throws NameParsingException {
        boolean t_bNext = true;
        while (t_bNext) {
            t_bNext = false;
            if (this.m_cToken >= 'A' && this.m_cToken <= 'Z') {
                this.nextToken();
                t_bNext = true;
            } else if (this.m_cToken >= 'a' && this.m_cToken <= 'z') {
                this.nextToken();
                t_bNext = true;
            }
        }
    }
}