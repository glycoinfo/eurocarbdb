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
package org.eurocarbdb.MolecularFramework.io.GlycoCT;


import java.util.ArrayList;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.sugar.Anomer;
import org.eurocarbdb.MolecularFramework.sugar.BaseType;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Modification;
import org.eurocarbdb.MolecularFramework.sugar.ModificationType;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Superclass;

/**
* residue              ::= "b" ":" <anomer> "-" <config> "-" <superclass> "-" <ring_start> ":" <ring_end>
*                          | "s" <substituent>  

* @author Logan
*
*/
public class BasetypeFactory  
{    
    private char m_cToken = ' ';
    private String m_strMS = "";
    private int m_iPosition = -1;
    
    private void nextToken() throws Exception   
    {
        // next character
        m_iPosition++;
        try 
        {
            // get next token
            this.m_cToken = this.m_strMS.charAt( this.m_iPosition );
        } 
        catch (IndexOutOfBoundsException e) 
        {
            // parsing error
            throw new Exception("COMMON000");
        }        
        // all successfull
    }
    
    /**
     * number          ::= "0" | ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
     * @return  number to be parsed 
     * @throws ImportExeption 
     */
    private int number() throws Exception
    {
        int t_iResult = 0;
        int t_iDigit = 0;
        if ( this.m_cToken =='0' )
        {
            // "0"
            this.nextToken();
            return t_iResult;
        }
        // ( "1" | ... | "9" ) 
        t_iDigit = (int) this.m_cToken;
        if ( t_iDigit < 49 || t_iDigit > 57 )
        {
            throw new SugarImporterException("COMMON004" , this.m_iPosition);
        }
        t_iResult = t_iDigit - 48;
        // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
        this.nextToken();
        t_iDigit = (int) this.m_cToken;
        while ( t_iDigit > 47 && t_iDigit < 58 )
        {
            t_iResult = ( t_iResult * 10 ) + ( t_iDigit - 48 );
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
    public Monosaccharide createResidue(String a_strMS) throws Exception
    {
        this.m_iPosition = -1;
        this.m_strMS = a_strMS;
        this.nextToken();
        // anomer
        Anomer t_objAnomer;
        t_objAnomer = Anomer.forSymbol( this.m_cToken );

        this.nextToken();
        if ( this.m_cToken != '-' )
        {
            throw new SugarImporterException("GLYCOCTC007", this.m_iPosition);
        }
        this.nextToken();
        // configuration
        int t_iMaxPos = this.m_strMS.indexOf(":",this.m_iPosition) - 7;
        ArrayList<BaseType> t_aConfiguration = new ArrayList<BaseType>(); 
        String t_strInformation = "";
        while (this.m_iPosition < t_iMaxPos )
        {
            t_strInformation = "";
            for (int t_iCounter = 0; t_iCounter < 4; t_iCounter++)
            {
                t_strInformation += this.m_cToken;
                this.nextToken();
            }
            try
            {
                t_aConfiguration.add(BaseType.forName(t_strInformation));
            }
            catch (Exception e)
            {
                throw new SugarImporterException("GLYCOCTC008", this.m_iPosition);
            }            
            if ( this.m_cToken != '-' )
            {
                throw new SugarImporterException("GLYCOCTC007", this.m_iPosition);
            }
            this.nextToken();
        }
        // superclass
        t_strInformation = "";
        for (int t_iCounter = 0; t_iCounter < 3; t_iCounter++)
        {
            t_strInformation += this.m_cToken;
            this.nextToken();
        }
        Superclass t_objSuper;
        try 
        {
            t_objSuper = Superclass.forName(t_strInformation.toLowerCase());    
        } 
        catch (Exception e) 
        {
            throw new SugarImporterException("GLYCOCTC009", this.m_iPosition);
        }
        Monosaccharide t_objMS = new Monosaccharide(t_objAnomer,t_objSuper);   
        t_objMS.setBaseType(t_aConfiguration);
        if ( this.m_cToken != '-' )
        {
            throw new SugarImporterException("GLYCOCTC007", this.m_iPosition);
        }
        this.nextToken();
        // ring
        int t_iRingStart;
        if ( this.m_cToken == 'x' )
        {
            t_iRingStart = Monosaccharide.UNKNOWN_RING;
            this.nextToken();
        }
        else
        {
            t_iRingStart = this.number();
        }
        if ( this.m_cToken != ':' )
        {
            throw new SugarImporterException("GLYCOCTC005", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken == 'x' )
        {
            t_objMS.setRing(t_iRingStart,Monosaccharide.UNKNOWN_RING);
            this.nextToken();
        }
        else
        {
            t_objMS.setRing(t_iRingStart,this.number());
        }
        // modifications
        while ( this.m_cToken == '|' )
        {
            int t_iPosOne;
            Integer t_iPosTwo = null;
            this.nextToken();
            if ( this.m_cToken == 'x' )
            {
                t_iPosOne = Modification.UNKNOWN_POSITION;
                this.nextToken();
            }
            else
            {
                t_iPosOne = this.number();
            }
            if ( this.m_cToken == ',' )
            {
                this.nextToken();
                t_iPosTwo = this.number();
            }
            if ( this.m_cToken != ':' )
            {
                throw new SugarImporterException("GLYCOCTC005", this.m_iPosition);
            }
            this.nextToken();
            int t_iStart = this.m_iPosition;
            this.modification_name();
            ModificationType t_enumMod;
            try 
            {
                t_enumMod = ModificationType.forName(this.m_strMS.substring( t_iStart , this.m_iPosition ));    
            } 
            catch (Exception e) 
            {
                throw new SugarImporterException("GLYCOCTC010", this.m_iPosition);
            }
            Modification t_objModi = new Modification(t_enumMod,t_iPosOne,t_iPosTwo);
            t_objMS.addModification(t_objModi);
        }            
        return t_objMS;
    }


    /**
     * 
     */
    private void modification_name() throws Exception 
    {
        boolean t_bNext = true;
        while ( t_bNext )
        {
            t_bNext = false;
            if ( this.m_cToken >= 'A' && this.m_cToken <= 'Z' )
            {
                this.nextToken();
                t_bNext = true;
            }
            else if ( this.m_cToken >= 'a' && this.m_cToken <= 'z' )
            {
                this.nextToken();
                t_bNext = true;
            }
        }        
    }
}