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
package org.eurocarbdb.MolecularFramework.io.Linucs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.io.StructureSpecialInformation;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.SugarImporterText;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.LinkageType;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;

/**
* start            ::= "[" "]" "[" <residuumname> "]" "{" { <subresiduum> } "}" 
*                      | "[" "]" "[" "L" "I" "N" "K" "]" "{" "[" "(" "U" "N" "T" "I" "L" "+" <linkage> ")" "]" "[" <residuumname> "]" "{" { <subresiduum> } "}" "}"
* linkage          ::= <number> { "/" <number> } [ ">" [ <repeatcount> ] ] | "?" | "N" | "S" | "P"
* residuumname     ::= <symbol> { <symbol> }
* symbol           ::= <character> | "0" | ... | "9" | "(" | ")" | ";" 
*                      | "," | ":" | ">" | "<" | " " | "\" 
*                      | "'" | "-" | "?"  | "_" | "+" | "/" | "." | "="
* subresiduum      ::= "[" "(" <link_r> + <link_n> ")" "]" "[" <residuumname> "]" "{" { <subresiduum> } "}"
* link_r           ::= <number> { "/" <number> } | "?" | "<" <number> { "/" <number> } | "<" "?" | "N" | "S" | "P" 
* link_n           ::= <number> { "/" <number> } [ ">" [ <repeatcount> ] ] | "REPEAT" | "?" | "CYCLIC" | "N" | "S" | "P"
* repeatcount      ::= <charakter> [ "X" ] | <number> [ "X" ]
* 
* @author rene
*
* SELECT r.LinucsID, e.errorID , e.text , e.position, r.LinucsCode FROM raw_glycosciences.raw_glycosciences r , raw_glycosciences.error e WHERE r.LinucsID = e.id AND e.type=1 AND r.LinucsCode not regexp "<.>" AND r.LinucsCode not like "%'%"  AND r.LinucsCode not like "%\%%" AND r.LinucsCode not like "%O+%" order by text
*/
public class SugarImporterLinucs extends SugarImporterText
{
    private ArrayList<StructureSpecialInformation> m_aSpecialList = new ArrayList<StructureSpecialInformation>(); 
    private int m_iRepeatCount = -1;
    private boolean m_bSpezialStart;    // true for start with "[][LINK]{[(UNITL+..." 
    private HashMap<String,Integer> m_hashRepeatInformation = new HashMap<String,Integer>();
    private ArrayList<String> m_aWarnings = new ArrayList<String>(); 
    /**
     * 
     */
    private void clear()
    {
        this.m_bSpezialStart = false;
        this.m_iRepeatCount = -1;
        this.m_aSpecialList.clear();
        this.m_hashRepeatInformation.clear();
        this.m_aWarnings.clear();
    }


    protected void start() throws SugarImporterException
    {
        try
        {
            this.startparsing();
        } 
        catch (GlycoconjugateException e)
        {
            throw new SugarImporterException("LINUCS021", this.m_iPosition);
        }
    }
    
    /**
     * 
     * Startmethod of the recursive decent parser. Scannerposition is the first sign in the string
     * start            ::= "[" "]" "[" <residuumname> "]" "{" { <subresiduum> } "}" 
     *                  |   "[" "]" "[" "L" "I" "N" "K" "]" "{" "[" "(" "U" "N" "T" "I" "L" "+" <linkage> ")" "]" "[" <residuumname> "]" "{" { <subresiduum> } "}" "}"
     * @throws GlycoconjugateException 
     */
    protected void startparsing() throws SugarImporterException, GlycoconjugateException
    {
        StructureSpecialInformation t_objSpezialInfo = null;
        GlycoGraph t_objSubSugar = this.m_objSugar;
        this.clear();
        // Sugar object is the first Object there we store data, later on maybe repeatunits too
        // a new sugar was created from the parent class
        int t_iStartPosition = 0;
        String t_strName = "";
        UnvalidatedGlycoNode t_objResiduum;
        // "["
        if ( this.m_cToken != '[' )
        {
            throw new SugarImporterException("LINUCS001", this.m_iPosition);
        }
        // "[" "]"
        this.nextToken();
        if ( this.m_cToken != ']' )
        {
            throw new SugarImporterException("LINUCS001", this.m_iPosition);
        }
        // "[" "]" "["
        this.nextToken();
        if ( this.m_cToken != '[' )
        {
            throw new SugarImporterException("LINUCS002", this.m_iPosition);
        }
        // parse residuumname
        // "[" "]" "[" <residuumname> 
        this.nextToken();
        t_iStartPosition = this.m_iPosition;
        this.residuumname();
        t_strName = this.m_strText.substring( t_iStartPosition , this.m_iPosition );
        if ( t_strName.equalsIgnoreCase("LINK") )
        {
            this.m_bSpezialStart = true;
            // "[" "]" "[" "L" "I" "N" "K"
            // ==> CYCLIC or REPEAT
            // "[" "]" "[" "L" "I" "N" "K" "]" 
            String t_strTemp = "";
            for (int t_iCounter = 0; t_iCounter < 9; t_iCounter++)
            {
                t_strTemp += this.m_cToken;
                this.nextToken();
            }
            if ( !t_strTemp.equalsIgnoreCase("]{[(UNTIL") )
            {
                throw new SugarImporterException("LINUCS013", this.m_iPosition);
            }
            // "[" "]" "[" "L" "I" "N" "K" "]" "{" "[" "(" "U" "N" "T" "I" "L" "+"
            if ( this.m_cToken != '+' )
            {
                throw new SugarImporterException("LINUCS009", this.m_iPosition);
            }
            this.nextToken();
            // "[" "]" "[" "L" "I" "N" "K" "]" "{" "[" "(" "U" "N" "T" "I" "L" "+" <linkage> 
            // special linkage ... repeat or cyclic
            Linkage t_objLinkage = new Linkage();
            t_objLinkage.setChildLinkageType(LinkageType.UNVALIDATED);
            t_objLinkage.setParentLinkageType(LinkageType.UNVALIDATED);
            int t_iResult = this.linkage( t_objLinkage );
            // 0 normal linkage ; 3 internal repeat (repeatcount in this.m_iRepeatCount) 
            // "[" "]" "[" "L" "I" "N" "K" "]" "{" "[" "(" "U" "N" "T" "I" "L" "+" <linkage> ")" 
            if ( this.m_cToken != ')' )
            {
                throw new SugarImporterException("LINUCS010", this.m_iPosition);
            }
            this.nextToken();
            // "[" "]" "[" "L" "I" "N" "K" "]" "{" "[" "(" "U" "N" "T" "I" "L" "+" <linkage> ")" "]"
            if ( this.m_cToken != ']' )
            {
                throw new SugarImporterException("LINUCS010", this.m_iPosition);
            }
            this.nextToken();
            // "[" "]" "[" "L" "I" "N" "K" "]" "{" "[" "(" "U" "N" "T" "I" "L" "+" <linkage> ")" "]" "["
            if ( this.m_cToken != '[' )
            {
                throw new SugarImporterException("LINUCS002", this.m_iPosition);
            }
            this.nextToken();
            // "[" "]" "[" "L" "I" "N" "K" "]" "{" "[" "(" "U" "N" "T" "I" "L" "+" <linkage> ")" "]" "[" <residuumname>
            t_iStartPosition = this.m_iPosition;
            this.residuumname();
            t_strName = this.m_strText.substring( t_iStartPosition , this.m_iPosition );
            t_objResiduum = new UnvalidatedGlycoNode();
            t_objResiduum.setName( t_strName );
            // REPEAT or CYCLIC 
            if ( this.isCyclic() )
            {
                // CYCLIC
                if ( t_iResult == 3 )
                {
                    // internal repeat open
                    // new Repeatunit
                    SugarUnitRepeat t_objRepeat = new SugarUnitRepeat();
                    t_objSubSugar.addNode(t_objRepeat);
                    // add the cyclic information to the stack
                    t_objSpezialInfo = new StructureSpecialInformation( t_objRepeat , t_objLinkage , null , t_objSubSugar);
                    this.m_aSpecialList.add(t_objSpezialInfo);
                    // prepare internal linkage
                    Linkage t_objInternal = new Linkage();
                    t_objInternal.setChildLinkages( t_objLinkage.getChildLinkages() );
                    // create new spezial object, flip mainobject
                    t_objSpezialInfo = new StructureSpecialInformation( t_objResiduum , t_objInternal , this.m_iRepeatCount , t_objRepeat , t_objSpezialInfo , t_objRepeat);
                    this.m_aSpecialList.add(t_objSpezialInfo);
                    t_objSubSugar = t_objRepeat;
                    this.m_iRepeatCount = -1;
                    // add the residue
                    t_objSubSugar.addNode(t_objResiduum);
                }
                else
                {
                    // add the cyclic information to the stack
                    t_objSpezialInfo = new StructureSpecialInformation( t_objResiduum , t_objLinkage , null , t_objSubSugar);
                    this.m_aSpecialList.add(t_objSpezialInfo);
                    t_objSubSugar.addNode(t_objResiduum);
                }
            }
            else
            {
                // REPEAT
                if ( t_iResult == 3 )
                {
                    // internal repeat open
                    // stack is empty, add the new repeat unit to the stack
                    SugarUnitRepeat t_objRepeat = new SugarUnitRepeat(); 
                    SugarUnitRepeat t_objRepeatInternal = new SugarUnitRepeat();
                    t_objSpezialInfo = new StructureSpecialInformation( t_objRepeatInternal , t_objLinkage , -1 , t_objRepeat , null , t_objSubSugar);
                    this.m_aSpecialList.add(t_objSpezialInfo);
                    t_objSubSugar.addNode(t_objRepeat);
                    t_objSubSugar = t_objRepeat;
                    // internal Repeatunit
                    t_objSubSugar.addNode(t_objRepeatInternal);
                    Linkage t_objInternal = new Linkage();
                    // prepare internal linkage
                    t_objInternal.setChildLinkages( t_objLinkage.getChildLinkages() );
                    // add the repeat information to the stack
                    t_objSpezialInfo = new StructureSpecialInformation( t_objResiduum , t_objInternal, this.m_iRepeatCount , t_objRepeatInternal , t_objSpezialInfo , t_objSubSugar);
                    this.m_aSpecialList.add(t_objSpezialInfo);
                    t_objSubSugar = t_objRepeatInternal;
                    this.m_iRepeatCount = -1;
                    // add the residue
                    t_objSubSugar.addNode(t_objResiduum);
                }
                else
                {                    
                    // stack is empty, add the new repeat unit to the stack
                    SugarUnitRepeat t_objRepeat = new SugarUnitRepeat(); 
                    t_objSpezialInfo = new StructureSpecialInformation( t_objResiduum , t_objLinkage , -1 , t_objRepeat , null , t_objSubSugar);
                    this.m_aSpecialList.add(t_objSpezialInfo);
                    t_objSubSugar.addNode(t_objRepeat);
                    t_objSubSugar = t_objRepeat;
                    // add repeatblock to the sugar and define block as new SugarUnit
                    t_objSubSugar.addNode(t_objResiduum);
                }
            }
            // "[" "]" "[" "L" "I" "N" "K" "]" "{" "[" "(" "U" "N" "T" "I" "L" "+" <linkage> ")" "]" "[" <residuumname> "]"
            if ( this.m_cToken != ']' )
            {
                throw new SugarImporterException("LINUCS003", this.m_iPosition);
            }
            this.nextToken();
            // "[" "]" "[" "L" "I" "N" "K" "]" "{" "[" "(" "U" "N" "T" "I" "L" "+" <linkage> ")" "]" "[" <residuumname> "]" "{"
            if ( this.m_cToken != '{' )
            {
                throw new SugarImporterException("LINUCS004", this.m_iPosition);
            }
            this.nextToken();
            // "[" "]" "[" "L" "I" "N" "K" "]" "{" "[" "(" "U" "N" "T" "I" "L" "+" <linkage> ")" "]" "[" <residuumname> "]" "{" { <subresiduum> }
            while ( this.m_cToken != '}' )
            {
                this.subresiduum(t_objResiduum,t_objSpezialInfo,t_objSubSugar);
            }
            // "[" "]" "[" "L" "I" "N" "K" "]" "{" "[" "(" "U" "N" "T" "I" "L" "+" <linkage> ")" "]" "[" <residuumname> "]" "{" { <subresiduum> } "}"
            this.nextToken();
            // "[" "]" "[" "L" "I" "N" "K" "]" "{" "[" "(" "U" "N" "T" "I" "L" "+" <linkage> ")" "]" "[" <residuumname> "]" "{" { <subresiduum> } "}" "}"
            if ( this.m_cToken != '}' )
            {
                throw new SugarImporterException("LINUCS005", this.m_iPosition);
            }
            this.nextToken();
        }
        else
        {
            this.m_bSpezialStart = false;
            // save Residuename in Monosaccharid Object        
            t_objResiduum = new UnvalidatedGlycoNode();
            t_objResiduum.setName( t_strName );
            t_objSubSugar.addNode(t_objResiduum);
            // "[" "]" "[" <residuumname> "]"
            if ( this.m_cToken != ']' )
            {
                throw new SugarImporterException("LINUCS003", this.m_iPosition);
            }
            // "[" "]" "[" <residuumname> "]" "{"
            this.nextToken();
            if ( this.m_cToken != '{' )
            {
                throw new SugarImporterException("LINUCS004", this.m_iPosition);
            }
            // "[" "]" "[" <residuumname> "]" "{" { <subresiduum> } 
            this.nextToken();
            while ( this.m_cToken != '}' )
            {
                this.subresiduum(t_objResiduum , t_objSpezialInfo , t_objSubSugar);
            }
            // "[" "]" "[" <residuumname> "]" "{" { <subresiduum> } "}"
            this.nextToken();
        }
        // $        
        if ( ! this.finished() )
        {
            throw new SugarImporterException("LINUCS006", this.m_iPosition);
        }
        // test if all REPEATS and CYCLICS was closed
        Iterator<StructureSpecialInformation> t_iIterator = this.m_aSpecialList.iterator();
        while ( t_iIterator.hasNext() ) 
        {
            t_objSpezialInfo = t_iIterator.next();
            if ( !t_objSpezialInfo.isClosed() )
            {
                throw new SugarImporterException("LINUCS018", this.m_iPosition);
            }
        }
    }

    /**
     * linkage          ::= ( <number> { "/" <number> } | "?" ) [ ">" [ <repeatcount> ] ] 
     * @param a_objLinkage
     * @return  0 normal linkage ; 3 internal repeat (repeatcount in this.m_iRepeatCount) 
     *            
     */
    private int linkage( Linkage a_objLinkage) throws SugarImporterException
    {
        if ( this.m_cToken == '?' )
        {
            // "?" [ ">" [ <repeatcount> ] ]
            this.nextToken();
            a_objLinkage.addChildLinkage( Linkage.UNKNOWN_POSITION );
        }
        else
        {
            // <number> { "/" <number> }
            int t_iPos = this.number();
            if ( t_iPos == 0 )
            {
                a_objLinkage.addChildLinkage( 1 );
            }
            else
            {
                a_objLinkage.addChildLinkage( t_iPos );
            }
            while ( this.m_cToken == '/' )
            {
                this.nextToken();
                a_objLinkage.addChildLinkage( this.natural_number() );
            }
        }
        // [ ">" [ <repeatcount> ] ]
        if ( this.m_cToken == '>' )
        {
            this.nextToken();
            if ( this.m_cToken != ')' )
            {
                this.m_iRepeatCount = this.repeatcount();
            }
            else
            {
                this.m_aWarnings.add("No count given for repeat part.");
            }
            return 3;
        }
        else
        {
            return 0;
        }
    }

    /**
     * residuumname     ::= <symbol> { <symbol> }
     * @throws ImportExeption 
     */
    private void residuumname() throws SugarImporterException
    {
        // <symbol> 
        this.symbol();
        while ( this.m_cToken != ']' )
        {
            // <symbol> { <symbol> }
            this.symbol();
        }        
    }

    /**
     * symbol           ::= <character> | "0" | ... | "9" | "(" | ")" | ";" | "," | ":" | ">" | "<" | " " | "\" | "'" | "-" | "?"  | "_" | "+" | "/" | "." | "="
     * @throws ImportExeption 
     */
    private void symbol() throws SugarImporterException
    {
        int t_iDigit = (int) this.m_cToken;
        // "0" | ... | "9"
        if ( t_iDigit > 47 && t_iDigit < 58 )
        {
            this.nextToken();
            return;
        }
        // "(" | ")" | ">" | "<"
        if ( this.m_cToken == '(' || this.m_cToken == ')' || this.m_cToken == '<' || this.m_cToken == '>' )
        {
            this.nextToken();
            return;
        }
        // ";" | "," | ":" | " " 
        if ( this.m_cToken == ';' || this.m_cToken == ',' || this.m_cToken == ':' || this.m_cToken == ' ' )
        {
            this.nextToken();
            return;
        }
        // "\" | "'" | "-" | "?"
        if ( this.m_cToken == '\\' || this.m_cToken == '\'' || this.m_cToken == '-' || this.m_cToken == '?' )
        {
            this.nextToken();
            return;
        }
        // "_" | "+" | "." | "=" 
        if ( this.m_cToken == '_' || this.m_cToken == '+' || this.m_cToken == '.' || this.m_cToken == '='  )
        {
            this.nextToken();
            return;
        }
        // "/"  
        if ( this.m_cToken == '/' )
        {
            this.nextToken();
            return;
        }
        this.character();
    }

    /**
     * subresiduum      ::= "[" "(" <link_r> + <link_n> ")" "]" "[" <residuumname> "]" "{" { <subresiduum> } "}"
     * 
     * @param a_objParentResidue 
     * @param a_objSpezialInfo 
     * @return 
     * @throws GlycoconjugateException 
     */
    private void subresiduum(GlycoNode a_objParentResiduum, StructureSpecialInformation a_objSpezialInfo, GlycoGraph a_objSugar) throws SugarImporterException, GlycoconjugateException
    {
        StructureSpecialInformation t_objSpezialInfo = a_objSpezialInfo;
        GlycoNode t_objParent = a_objParentResiduum;
        int t_iResultR = -1;
        int t_iResultN = -1;
        int t_iStartPosition = 0;
        UnvalidatedGlycoNode t_objResiduum;
        GlycoEdge t_objEdge;
        Linkage t_objLinkage = new Linkage();
        GlycoGraph t_objSugar = a_objSugar;
        // "["
        if ( this.m_cToken != '[' )
        {
            throw new SugarImporterException("LINUCS008", this.m_iPosition);
        }
        // "[" "(" 
        this.nextToken();
        if ( this.m_cToken != '(' )
        {
            throw new SugarImporterException("LINUCS008", this.m_iPosition);
        }
        // "[" "(" <link_r>
        this.nextToken();
        t_iResultR = this.link_r(t_objLinkage);
        if ( t_iResultR == 1 )
        {
            // ending repeat ; first we have to test if a repeating block was opend before
            if ( t_objSpezialInfo == null )
            {
                throw new SugarImporterException("LINUCS017", this.m_iPosition);
            }
            if ( t_objSpezialInfo.getType() != StructureSpecialInformation.REPEAT )
            {
                throw new SugarImporterException("LINUCS017", this.m_iPosition);
            }       
            // was the repeatblock closed before?
            if ( t_objSpezialInfo.isClosed() )
            {
                throw new SugarImporterException("LINUCS019", this.m_iPosition);
            }
            t_objSpezialInfo.close();
            // internal linkage?            
            Linkage t_objInternalLinkage = t_objSpezialInfo.getIncomingLinkage();
            // fill reducing part of the linkage
            t_objInternalLinkage.setParentLinkages(t_objLinkage.getParentLinkages());
            GlycoNode t_objTarget = t_objSpezialInfo.getTarget();
            t_objEdge = new GlycoEdge();
            t_objEdge.addGlycosidicLinkage(t_objInternalLinkage);
            // generate linkage
            t_objSpezialInfo.getRepeatBlock().setRepeatLinkage(t_objEdge,t_objParent,t_objTarget);
            // restore level of structure (above repeat)
            t_objParent = t_objSpezialInfo.getRepeatBlock();
            t_objSugar = t_objSpezialInfo.getParentUnit();
            t_objSpezialInfo = t_objSpezialInfo.getParentInfo();
        }
        else if ( t_iResultR == 2 || t_iResultR == 3 || t_iResultR == 4)
        {
            // 2 = N-Linakge || 3 = S-Linkage || 4 = P-linkage
            t_objLinkage.addChildLinkage(Linkage.UNKNOWN_POSITION);
            t_objLinkage.addParentLinkage(1);
            UnvalidatedGlycoNode t_objChild = new UnvalidatedGlycoNode();
            if ( t_iResultR == 2 )
            {
                t_objChild.setName("N");
            }
            else if ( t_iResultR == 3)
            {
                t_objChild.setName("S");
            }
            else
            {
                t_objChild.setName("P");
            }
            t_objSugar.addNode(t_objChild);
            t_objEdge = new GlycoEdge();
            t_objEdge.addGlycosidicLinkage(t_objLinkage);
            t_objSugar.addEdge(t_objParent,t_objChild,t_objEdge);
            // create new objects
            t_objLinkage = new Linkage();
            t_objLinkage.addParentLinkage(1);
            t_objParent = t_objChild;
        }
        // "[" "(" <link_r> +
        if ( this.m_cToken != '+' )
        {
            throw new SugarImporterException("LINUCS009", this.m_iPosition);
        }
        this.nextToken();
        // "[" "(" <link_r> + <link_n> 
        t_iResultN = this.link_n(t_objLinkage);
        if ( t_iResultN == 1 || t_iResultN == 2)
        {
            // REPEAT && CYCLIC ==> without Link-Until at beginning ==> Error
            if ( !this.m_bSpezialStart )
            {
                if ( t_iResultN == 1 )
                {
                    throw new SugarImporterException("LINUCS014", this.m_iPosition);
                }
                else
                {
                    throw new SugarImporterException("LINUCS015", this.m_iPosition);
                }
            }
            // now must come ')][LINK]{}'
            String t_strTemp = "";
            for (int t_iCounter = 0; t_iCounter < 10; t_iCounter++)
            {
                t_strTemp += this.m_cToken;
                this.nextToken();
            }
            if ( !t_strTemp.equalsIgnoreCase(")][LINK]{}") )
            {
                throw new SugarImporterException("LINUCS016", this.m_iPosition);
            }
            if ( t_iResultN == 1 )
            {
                // REPEAT
                // ending repeat ; first we have to test if a repeating block was opend before
                if ( t_objSpezialInfo == null )
                {
                    throw new SugarImporterException("LINUCS014", this.m_iPosition);
                }
                if ( t_objSpezialInfo.getType() != StructureSpecialInformation.REPEAT )
                {
                    throw new SugarImporterException("LINUCS014", this.m_iPosition);
                }
                // was the repeatblock closed before?
                if ( t_objSpezialInfo.isClosed() )
                {
                    throw new SugarImporterException("LINUCS019", this.m_iPosition);
                }
                t_objSpezialInfo.close();
                // internal linkage?            
                Linkage t_objInternalLinkage = t_objSpezialInfo.getIncomingLinkage();
                // fill reducing part of the linkage
                t_objInternalLinkage.setParentLinkages(t_objLinkage.getParentLinkages());
                GlycoNode t_objTarget = t_objSpezialInfo.getTarget();
                t_objEdge = new GlycoEdge();
                t_objEdge.addGlycosidicLinkage(t_objInternalLinkage);
                // generate linkage
                t_objSpezialInfo.getRepeatBlock().setRepeatLinkage(t_objEdge,t_objParent,t_objTarget);
            }
            else
            {
                // CYCLIC
                if ( t_objSpezialInfo == null )
                {
                    throw new SugarImporterException("LINUCS015", this.m_iPosition);
                }
                if ( t_objSpezialInfo.getType() != StructureSpecialInformation.CYCLIC )
                {
                    throw new SugarImporterException("LINUCS015", this.m_iPosition);
                }
                // was the closedblock closed before?
                if ( t_objSpezialInfo.isClosed() )
                {
                    throw new SugarImporterException("LINUCS019", this.m_iPosition);
                }
                t_objSpezialInfo.close();
                // CYCLIC linkage
                Linkage t_objInternalLinkage = t_objSpezialInfo.getIncomingLinkage();
                // fill reducing part of the linkage
                t_objInternalLinkage.setParentLinkages(t_objLinkage.getParentLinkages());
                t_objEdge = new GlycoEdge();
                t_objEdge.addGlycosidicLinkage(t_objInternalLinkage);
                // target node
                GlycoNode t_objTarget = t_objSpezialInfo.getTarget();
                // generate linkage
                t_objSugar.addEdge(t_objParent,t_objTarget,t_objEdge);
            }
            return;
        }
        else if ( t_iResultN == 4 || t_iResultN == 5 || t_iResultN == 6 )
        {
            // 4 = N-Linakge || 5 = S-Linkage || 6 = P-Linkage
            t_objLinkage.addChildLinkage(1);
            UnvalidatedGlycoNode t_objChild = new UnvalidatedGlycoNode();
            if ( t_iResultN == 4 )
            {
                t_objChild.setName("N");
            }
            else if ( t_iResultN == 5)
            {
                t_objChild.setName("S");
            }
            else
            {
                t_objChild.setName("P");
            }
            t_objSugar.addNode(t_objChild);
            t_objEdge = new GlycoEdge();
            t_objEdge.addGlycosidicLinkage(t_objLinkage);
            t_objSugar.addEdge(t_objParent,t_objChild,t_objEdge);
            // create new objects
            t_objLinkage = new Linkage();
            t_objLinkage.addParentLinkage(1);
            t_objLinkage.addChildLinkage(Linkage.UNKNOWN_POSITION);
            t_objParent = t_objChild;
        }
        // "[" "(" <link_r> + <link_n> ")"
        if ( this.m_cToken != ')' )
        {
            throw new SugarImporterException("LINUCS010", this.m_iPosition);
        }
        // "[" "(" <link_r> + <link_n> ")" "]"
        this.nextToken();
        if ( this.m_cToken != ']' )
        {
            throw new SugarImporterException("LINUCS010", this.m_iPosition);
        }
        // "[" "(" <link_r> + <link_n> ")" "]" "[" 
        this.nextToken();
        if ( this.m_cToken != '[' )
        {
            throw new SugarImporterException("LINUCS002", this.m_iPosition);
        }
        // "[" "(" <link_r> + <link_n> ")" "]" "[" <residuumname> 
        this.nextToken();
        t_iStartPosition = this.m_iPosition;
        this.residuumname();
        // save Residuename in Monosaccharid Object
        t_objResiduum = new UnvalidatedGlycoNode();
        t_objResiduum.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
        // if a internal repeat was opend then some spezial things to do
        if ( t_iResultN == 3 )
        {
            // internal repeat open
            // new Repeatunit
            Linkage t_objInternal = new Linkage();
            t_objInternal.setChildLinkages( t_objLinkage.getChildLinkages() );
            SugarUnitRepeat t_objRepeat = new SugarUnitRepeat(); 
            t_objRepeat.setRepeatCount(this.m_iRepeatCount);
            // attach to linkage and Parent Unit
            t_objSugar.addNode(t_objRepeat);
            t_objEdge = new GlycoEdge();
            t_objEdge.addGlycosidicLinkage(t_objLinkage);
            t_objSugar.addEdge(t_objParent,t_objRepeat,t_objEdge);
            // create new spezial object
            t_objSpezialInfo = new StructureSpecialInformation( t_objResiduum , t_objInternal , this.m_iRepeatCount , t_objRepeat , t_objSpezialInfo , t_objSugar);
            this.m_aSpecialList.add(t_objSpezialInfo);
            this.m_iRepeatCount = -1;
            // define block as new SugarUnit
            t_objSugar = t_objRepeat;
            t_objSugar.addNode(t_objResiduum);
        }
        else
        {
            // add residue and build edge
            t_objSugar.addNode(t_objResiduum);
            t_objEdge = new GlycoEdge();
            t_objEdge.addGlycosidicLinkage(t_objLinkage);
            t_objSugar.addEdge(t_objParent,t_objResiduum,t_objEdge);
        }
        // "[" "(" <link_r> + <link_n> ")" "]" "[" <residuumname> "]" 
        if ( this.m_cToken != ']' )
        {
            throw new SugarImporterException("LINUCS003", this.m_iPosition);
        }
        // "[" "(" <link_r> + <link_n> ")" "]" "[" <residuumname> "]" "{" 
        this.nextToken();
        if ( this.m_cToken != '{' )
        {
            throw new SugarImporterException("LINUCS004", this.m_iPosition);
        }
        // "[" "(" <link_r> + <link_n> ")" "]" "[" <residuumname> "]" "{" { <subresiduum> } 
        this.nextToken();
        while ( this.m_cToken != '}' )
        {
            this.subresiduum(t_objResiduum,t_objSpezialInfo,t_objSugar);
        }
        // "[" "(" <link_r> + <link_n> ")" "]" "[" <residuumname> "]" "{" { <subresiduum> } "}"
        if ( this.m_cToken != '}' )
        {
            throw new SugarImporterException("LINUCS005", this.m_iPosition);
        }
        this.nextToken();
    }

    /**
     * link_n           ::= <number> { "/" <number> } [ ">" [ <repeatcount> ] ] | "REPEAT" | "?" | "CYCLIC" | "N" | "S" | "P"
     * @param a_objLinkage
     * @return  0 normal linkage ; 1 REPEAT ; 2 CYCLIC ; 3 internal repeat (repeatcount in this.m_iRepeatCount) ; 4 N-Linkage ; 5 S-Linkage ; 6 P-LINKAGE
     *            
     */
    private int link_n( Linkage a_objLinkage ) throws SugarImporterException
    {
        // "REPEAT"
        if ( this.m_cToken == 'R' || this.m_cToken == 'r' )
        {
            String t_strTemp = "";
            for (int t_iCounter = 0; t_iCounter < 6; t_iCounter++)
            {
                t_strTemp += this.m_cToken;
                this.nextToken();
            }
            if ( !t_strTemp.equalsIgnoreCase("REPEAT") )
            {
                throw new SugarImporterException("LINUCS011", this.m_iPosition);
            }
            return 1;
        }
        else if ( this.m_cToken == '?' )
        {
            // "?" [ ">" [ <repeatcount> ] ]
            this.nextToken();
            a_objLinkage.addChildLinkage( Linkage.UNKNOWN_POSITION );
            if ( this.m_cToken == '>' )
            {
                this.nextToken();
                if ( this.m_cToken != ')' )
                {
                    this.m_iRepeatCount = this.repeatcount();
                }
                else
                {
                    this.m_aWarnings.add("No count given for repeat part.");
                }
                return 3;
            }
            else
            {
                return 0;
            }
        }
        else if ( this.m_cToken == 'N' )
        {
            this.nextToken();
            return 4;
        }
        else if ( this.m_cToken == 'S' )
        {
            this.nextToken();
            return 5;
        }
        else if ( this.m_cToken == 'P' )
        {
            this.nextToken();
            return 6;
        }
        else if ( this.m_cToken == 'c' || this.m_cToken == 'C' )
        {
            // CYCLIC
            String t_strTemp = "";
            for (int t_iCounter = 0; t_iCounter < 6; t_iCounter++)
            {
                t_strTemp += this.m_cToken;
                this.nextToken();
            }
            if ( !t_strTemp.equalsIgnoreCase("CYCLIC") )
            {
                throw new SugarImporterException("LINUCS012", this.m_iPosition);
            }
            return 2;
        }
        else
        {
            // <number> { "/" <number> } [ ">" [ <repeatcount> ] ]
            int t_iPos = this.number();
            // special linkage to S or P (related to carbbank translation)
            if ( t_iPos == 0 )
            {
                a_objLinkage.addChildLinkage( 1 );
            }
            else
            {
                a_objLinkage.addChildLinkage( t_iPos );
            }
            while ( this.m_cToken == '/' )
            {
                this.nextToken();
                a_objLinkage.addChildLinkage( this.natural_number() );
            }
            if ( this.m_cToken == '>' )
            {
                this.nextToken();
                if ( this.m_cToken != ')' )
                {
                    this.m_iRepeatCount = this.repeatcount();
                }
                else
                {
                    this.m_aWarnings.add("No count given for repeat part.");
                }
                return 3;
            }
            else
            {
                return 0;
            }
        }
    }

    /**
     * repeatcount      ::= <charakter> [ "X" ] | <number> [ "X" ]
     * @throws ImportExeption 
     */
    private int repeatcount() throws SugarImporterException
    {
        int t_iResult = -1;
        int t_iTemp = (int)this.m_cToken;
        if ( t_iTemp > 47 && t_iTemp < 58)
        {
            // <number> "X"
            t_iResult = this.number();
            if ( this.m_cToken == 'X' )
            {
                this.nextToken();
            }
        }
        else
        {
            // <charakter>
            String t_strWildcard = "";
            t_strWildcard += this.m_cToken;
            this.character();
            if ( this.m_cToken == 'X' )
            {
                this.nextToken();
            }
            Integer t_iCount = this.m_hashRepeatInformation.get(t_strWildcard);
            if ( t_iCount == null )
            {
                // Warning
                this.m_aWarnings.add("Repeatcount not set for symbol : " + t_strWildcard );
                t_iResult = -1;
            }
            else
            {
                t_iResult = t_iCount;
            }            
        }        
        return t_iResult;
    }

    /**
     * Fills a_objLinkage with the data.
     * link_r           ::= <number> { "/" <number> } | "?" | "<" <number> { "/" <number> } | "<" "?" | "N" | "S" | "P"
     * @return  0 normal linkage ; 1 internal repeat ; 2 N-Linkage ; 3 S-Linkage ; 4 P-LINKAGE
     */
    private int link_r(Linkage a_objLinkage) throws SugarImporterException
    {
        if ( this.m_cToken == '?' )
        {
            a_objLinkage.addParentLinkage( Linkage.UNKNOWN_POSITION );
            this.nextToken();
            return 0;
        }
        else if ( this.m_cToken == 'N' )
        {
            this.nextToken();
            return 2;
        }
        else if ( this.m_cToken == 'S' )
        {
            this.nextToken();
            return 3;
        }
        else if ( this.m_cToken == 'P' )
        {
            this.nextToken();
            return 4;
        }
        else if ( this.m_cToken == '<' )
        {
            // generate repate
            this.nextToken();
            if ( this.m_cToken == '?' )
            {
                a_objLinkage.addParentLinkage( Linkage.UNKNOWN_POSITION );
                this.nextToken();                
            }
            else
            {
                a_objLinkage.addParentLinkage(this.number());
                while ( this.m_cToken == '/' )
                {
                    // <number> { "/" <number> }
                    this.nextToken();
                    a_objLinkage.addParentLinkage(this.natural_number());
                }     
            }
            return 1;
        } 
        else
        {
            int t_iPos = this.number();
            // special linkage to S or P (related to carbbank translation)
            if ( t_iPos == 0 )
            {
                a_objLinkage.addParentLinkage( 1 );
            }
            else
            {
                a_objLinkage.addParentLinkage( t_iPos );
            }
            while ( this.m_cToken == '/' )
            {
                // <number> { "/" <number> }
                this.nextToken();
                a_objLinkage.addParentLinkage(this.natural_number());
            }   
            return 0;
        }
    }
    
    /**
     * Returns if the string contains a cylic structure or not. 
     * @return true if the String contains a cyclic structure AND the startpoint was not found yet
     */
    private boolean isCyclic()
    {
        String t_strTemp = this.m_strText.toUpperCase();
        // find a cyclic mark position
        if ( t_strTemp.indexOf("CYCLIC)][LINK]") == -1 )
        {
            return false;
        }
        return true;        
    }
    
    public void setRepeatCount(HashMap<String,Integer> a_hashInformation)
    {
        this.m_hashRepeatInformation = a_hashInformation;
    }
    
    public ArrayList<String> getWarnings()
    {
        return this.m_aWarnings;
    }
}
