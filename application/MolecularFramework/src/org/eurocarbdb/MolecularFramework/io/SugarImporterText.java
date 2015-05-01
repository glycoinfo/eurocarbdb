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
package org.eurocarbdb.MolecularFramework.io;

import org.eurocarbdb.MolecularFramework.sugar.Sugar;

public abstract class SugarImporterText extends SugarImporter 
{
    // current token
    protected char m_cToken = 0;
    // current position in m_strText
    protected int  m_iPosition = -1;
    // length of the string
    protected int  m_iLength = -1;
    // store the string that is to parse
    protected String m_strText = "";
    
    public SugarImporterText()
    {
        this.m_cToken = 0;
        this.m_iPosition = 0;
        this.m_iLength = 0;
        this.m_strText = "";
    }
    
    protected char aheadToken(int a_iPosition) throws SugarImporterException
    {
        try 
        {
            // get next token
            return this.m_strText.charAt( this.m_iPosition + a_iPosition);
        } 
        catch (IndexOutOfBoundsException e) 
        {
            // parsing error
            throw new SugarImporterException("COMMON012");
        }       
        // all successfull
    }
    
    /**
     * set the pointer to the next token in the string 
     * 
     */
    protected void nextToken() throws SugarImporterException   
    {
        // next character
        m_iPosition++;
        try 
        {
            // get next token
            this.m_cToken = this.m_strText.charAt( this.m_iPosition );
        } 
        catch (IndexOutOfBoundsException e) 
        {
            // parsing error
            throw new SugarImporterException("COMMON000");
        }        
        // all successfull
    }

    /**
     * number          ::= "0" | ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
     * @return  number to be parsed 
     * @throws ImportExeption 
     */
    protected int number() throws SugarImporterException
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
     * natural_number       ::= ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
     * @return  number to be parsed 
     * @throws ImportExeption 
     */
    protected int natural_number() throws SugarImporterException
    {
        int t_iResult = 0;
        int t_iDigit = 0;
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
     * float_number  ::= ( "0" | ( "1" | ... | "9" ) { "0" | "1" | ... | "9" } ) [ "." ( "0" | ... | "9" ) { "0" | ... | "9" } ]
     * @throws SugarImporterException 
     */
    protected double float_number() throws SugarImporterException
    {
        double t_dResult = 0.0;
        double t_dDezi = 10;
        int t_iDigit = 0;
        if ( this.m_cToken =='0' )
        {
            // "0"
            this.nextToken();
        }
        else
        {
            // ( "1" | ... | "9" ) 
            t_iDigit = (int) this.m_cToken;
            if ( t_iDigit < 49 || t_iDigit > 57 )
            {
                throw new SugarImporterException("COMMON002" , this.m_iPosition);
            }
            t_dResult = t_iDigit - 48;
            // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
            this.nextToken();
            t_iDigit = (int) this.m_cToken;
            while ( t_iDigit > 47 && t_iDigit < 58 )
            {
                t_dResult = ( t_dResult * 10 ) + ( t_iDigit - 48 );
                // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
                this.nextToken();
                t_iDigit = (int) this.m_cToken;
            }
        }
        // "."
        if ( this.m_cToken == '.' )
        {
            this.nextToken();
            // ( "1" | ... | "9" ) 
            t_iDigit = (int) this.m_cToken;
            if ( t_iDigit < 48 || t_iDigit > 57 )
            {
                throw new SugarImporterException("COMMON009" , this.m_iPosition);
            }
            t_dResult += (t_iDigit - 48) / t_dDezi;
            // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
            this.nextToken();
            t_iDigit = (int) this.m_cToken;
            while ( t_iDigit > 47 && t_iDigit < 58 )
            {
                t_dDezi *= 10;
                t_dResult += ( t_iDigit - 48 ) / t_dDezi;
                // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
                this.nextToken();
                t_iDigit = (int) this.m_cToken;
            }
        }
        return t_dResult;        
    }

    /**
     * character        ::= "a" ... "z" | "A" ... "Z" 
     */
    protected void character() throws SugarImporterException
    {
        // "A" ... "Z"
        if ( this.m_cToken >= 'A' && this.m_cToken <= 'Z' )
        {
            this.nextToken();
        }
        else
        {
            // "a" ... "z"
            if ( this.m_cToken >= 'a' && this.m_cToken <= 'z' )
            {
                this.nextToken();
            }
            else
            {
                throw new SugarImporterException("COMMON003" , this.m_iPosition);                    
            }
        }
    }
        
    /**
     * character_l      ::= "a" ... "z" 
     */
    protected void character_l() throws SugarImporterException
    {
        // "a" ... "z"
        if ( this.m_cToken >= 'a' && this.m_cToken <= 'z' )
        {
            this.nextToken();
        }
        else
        {
            throw new SugarImporterException("COMMON010" , this.m_iPosition);                    
        }
    }

    /**
     * character_u      ::= "A" ... "Z" 
     */
    protected void character_u() throws SugarImporterException
    {
        // "A" ... "Z"
        if ( this.m_cToken >= 'A' && this.m_cToken <= 'Z' )
        {
            this.nextToken();
        }
        else
        {
            throw new SugarImporterException("COMMON011" , this.m_iPosition);                    
        }
    }

    /**
     * string_l            ::= <charakter_l> { <charater_l> } 
     */
    protected void string_l() throws SugarImporterException
    {
        this.character_l();
        while( this.m_cToken >= 'a' && this.m_cToken <= 'z' )
        {
            this.character_l();
        }
    }

    /**
     * string_u            ::= <charakter_u> { <charater_u> } 
     */
    protected void string_u() throws SugarImporterException
    {
        this.character_u();
        while( this.m_cToken >= 'A' && this.m_cToken <= 'Z' )
        {
            this.character_u();
        }
    }

    /**
     * float_number_signed ::= [ "-" | "+" ] <float_number>
     * @throws SugarImporterException 
     */
    protected double float_number_signed() throws SugarImporterException
    {
        // [ "-" | "+" ]
        if ( this.m_cToken == '+' )
        {
            this.nextToken();
            return this.float_number();
        }
        if ( this.m_cToken == '-' )
        {
            this.nextToken();
            return (0 - this.float_number());
        }
        return this.float_number();
    }

    /**
     * string            ::= <charakter> { <charater> } 
     */
    protected void string() throws SugarImporterException
    {
        this.character();
        while( this.m_cToken >= 'A' && this.m_cToken <= 'Z' || this.m_cToken >= 'a' && this.m_cToken <= 'z' )
        {
            this.character();
        }
    }

    /**
     * Parse a string according the gramatic of the language. Uses recursiv decent
     *  
     * @param a_strStream        String that is to parse
     * @throws ImportExeption 
     */
    public Sugar parse(String a_strStream) throws SugarImporterException 
    {
        this.m_objSugar = new Sugar();
        this.m_iPosition = -1;
        // Copie string and add endsymbol
        this.m_strText = a_strStream + '$';
        this.m_iLength = this.m_strText.length();
        // get first token . Error ? ==> string empty
        this.nextToken();
        this.start();
        return this.m_objSugar;
    }
    
    /**
     * @return true if the endsign is reached , otherwise false
     */
    protected boolean finished()
    {
        if ( this.m_cToken == '$' && ( this.m_iPosition + 1 ) == this.m_iLength )
        {
            return true;
        }
        return false;
    }

    /**
     * Startmethod of the recursive decent parser. Scannerposition is the first sign in the string
     * @throws ImportExeption 
     */
    protected abstract void start() throws SugarImporterException;
    
}
