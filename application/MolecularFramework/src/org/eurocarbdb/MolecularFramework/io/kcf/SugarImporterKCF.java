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
package org.eurocarbdb.MolecularFramework.io.kcf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.SugarImporterText;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;

/**
* start             ::= <head> <nodes> <edges> [ <bracket> ] "/" "/" "/" [ "\n" ]
* head                 ::= "E" "N" "T" "R" "Y" " " { " " } <go_number> " " { " " } "G" "l" "y" "c" "a" "n" "\n"
* go_number         ::= "G" ( "0" | ... | "9" ) { ( "0" | ... | "9" ) } 
* nodes             ::= "N" "O" "D" "E" " " { " " } <number> "\n" <node> { <node> }
* node                 ::= { " " } <number> " " { " " } <glycan_name> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> "\n"
* glycan_name       ::= <symbol> { <symbol> } 
* edges             ::= "E" "D" "G" "E" " " { " " } <number> "\n" { <edge> }
* edge                 ::= { " " } <number> " " { " " } <number> [ ":" <link_information> ] " " { " " } <number> [ ":" <link_information> ] "\n"
* link_information  ::= ["a" | "b"]  [ ( <number> [ "," <number> ] ) | ( * [ "1" ] ) ]
* symbol            ::= <character> | "0" | ... | "9"
* signed_number     ::= [ "-" | "+" ] <number> 
* bracket           ::= "B" "R" "A" "C" "K" "E" "T" <bracket_line> <bracket_line> <bracket_final>
* bracket_line      ::= { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> "\n"
* bracket_final     ::= { " " } <number> " " { " " } ( <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } ( "n" [ "-" <number> ] | <number> [ "-" <number> ] ) | "n" | "m" ) "\n"
* signed_dec_number ::= [ "-" | "+" ] <number> [ "." ( 0 | ... | 9 ) { 0 | ... | 9 } ] 
* @author Logan
*
*/
public class SugarImporterKCF extends SugarImporterText
{
    private String m_strGONumber                        = "";
    private int m_iResidueCount                         = 0;
    private int m_iEdgeCount                            = 0;
    private int m_iBlockCount                           = 0;
    private HashMap<Integer,KCFResidue> m_hResidues     = new HashMap<Integer,KCFResidue>(); 
    private ArrayList<KCFLinkage> m_aLinkages           = new ArrayList<KCFLinkage>();
    private ArrayList<KCFLinkage> m_aHandledLinkages    = new ArrayList<KCFLinkage>();
    private ArrayList<KCFBlock> m_aBlock                = new ArrayList<KCFBlock>();
    private char m_cAnomer                                 = ' ';
    private int m_iLinkagePosition                      = -1;
    
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
        this.m_strText = a_strStream.replaceAll("\r", "") + '$';
        this.m_iLength = this.m_strText.length();
        // get first token . Error ? ==> string empty
        this.nextToken();
        this.start();
        return this.m_objSugar;
    }

    // start        ::= <head> <nodes> <edges> [ <bracket> ] "/" "/" "/" [ "\n" ]
    protected void start() throws SugarImporterException 
    {
        this.clear();
        // <head>
        this.head();
        // <head> <nodes> 
        this.nodes();
        // <head> <nodes> <edges>
        this.edges();
        // <head> <nodes> <edges> [ <bracket> ]
        if ( this.m_cToken == 'B' )
        {
            this.bracket();
        }
        // <head> <nodes> <edges> [ <bracket> ] "/" 
        if ( this.m_cToken != '/' )
        {
            throw new SugarImporterException("KCF000", this.m_iPosition);
        }
        this.nextToken();
        // <head> <nodes> <edges> [ <bracket> ] "/" "/" 
        if ( this.m_cToken != '/' )
        {
            throw new SugarImporterException("KCF000", this.m_iPosition);
        }
        this.nextToken();
        // <head> <nodes> <edges> [ <bracket> ] "/" "/" "/"
        if ( this.m_cToken != '/' )
        {
            throw new SugarImporterException("KCF000", this.m_iPosition);
        }
        this.nextToken();
        // <head> <nodes> <edges> [ <bracket> ] "/" "/" "/" [ "\n" ]
        if ( this.m_cToken == '\n' )
        {
            this.nextToken();
        }
        // $
        if ( ! this.finished() )
        {
            throw new SugarImporterException("KCF001", this.m_iPosition);
        }
        // bring the pieces together, create sugar
        this.m_iResidueCount = 0;
        this.m_iEdgeCount = 0;
        this.m_iBlockCount = 0;
        this.m_aHandledLinkages.clear();
        this.createSugar();
        if ( this.m_iResidueCount != this.m_hResidues.size() )
        {
            throw new SugarImporterException("KCF016", this.m_iPosition);
        }
        if ( this.m_iEdgeCount != this.m_aLinkages.size() )
        {
            throw new SugarImporterException("KCF017", this.m_iPosition);    
        }
        if ( this.m_iBlockCount != this.m_aBlock.size() )
        {
            throw new SugarImporterException("KCF018", this.m_iPosition);
        }
    }

    private void clear() 
    {
        // cleanup system
        this.m_strGONumber = "";
        this.m_iResidueCount = 0;
        this.m_iEdgeCount = 0;
        this.m_iBlockCount = 0;
        this.m_hResidues.clear();
        this.m_aBlock.clear();
        this.m_aLinkages.clear();
    }

    /**
     * edges        ::= "E" "D" "G" "E" " " { " " } <number> "\n" { <edge> }
     * @throws SugarImporterException 
     */
    private void edges() throws SugarImporterException
    {
        int t_iCounter = 0;
        // "E"
        if ( this.m_cToken != 'E' )
        {
            throw new SugarImporterException("KCF023", this.m_iPosition);
        }
        this.nextToken();
        // "E" "D" 
        if ( this.m_cToken != 'D' )
        {
            throw new SugarImporterException("KCF023", this.m_iPosition);
        }
        this.nextToken();
        // "E" "D" "G" 
        if ( this.m_cToken != 'G' )
        {
            throw new SugarImporterException("KCF023", this.m_iPosition);
        }
        this.nextToken();
        // "E" "D" "G" "E" 
        if ( this.m_cToken != 'E' )
        {
            throw new SugarImporterException("KCF023", this.m_iPosition);
        }
        this.nextToken();
        // "E" "D" "G" "E" " "
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF024", this.m_iPosition);
        }
        this.nextToken();
        // "E" "D" "G" "E" " " { " " } 
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // "E" "D" "G" "E" " " { " " } <number>
        this.m_iEdgeCount = this.number();
        // "E" "D" "G" "E" " " { " " } <number> "\n"
        if ( this.m_cToken != '\n' )
        {
            throw new SugarImporterException("KCF025", this.m_iPosition);
        }
        this.nextToken();
        // "E" "D" "G" "E" " " { " " } <number> "\n" { <edge> }
        while ( this.m_cToken != '/' && this.m_cToken != 'B' )
        {
            t_iCounter++;
            this.edge();
        }
        if ( this.m_iEdgeCount != t_iCounter )
        {
            throw new SugarImporterException("KCF026");
        }
    }

    /**
     * edge         ::= { " " } <number> " " { " " } <number> [ <link_information> ] " " { " " } <number> [ ":" <link_information> ] { " " } "\n"
     * @throws SugarImporterException 
     */
    private void edge() throws SugarImporterException
    {
        Integer t_iNodeOne = 0;
        Integer t_iNodeTwo = 0;
        int t_iLinkagePositionOne = Linkage.UNKNOWN_POSITION;
        int t_iLinkagePositionTwo = Linkage.UNKNOWN_POSITION;
        // { " " } 
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number>
        this.number();
        // { " " } <number> " " 
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF020", this.m_iPosition);
        }
        this.nextToken();
        // { " " } <number> " " { " " }
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number> " " { " " } <number>
        t_iNodeOne = this.number();
        if ( !this.m_hResidues.containsKey( t_iNodeOne ) )
        {
            throw new SugarImporterException("KCF027", this.m_iPosition);
        }
        // { " " } <number> " " { " " } <number> [ ":" ]
        if ( this.m_cToken == ':' )
        {
            this.nextToken();
            this.link_information();
            t_iLinkagePositionOne = this.m_iLinkagePosition;
            // add the anomer to the residue name
            if ( this.m_cAnomer != ' ' )
            {
                UnvalidatedGlycoNode t_objResidue = this.m_hResidues.get( t_iNodeOne ).getResidue(); 
                try
                {
                    t_objResidue.setName( String.format("%c",this.m_cAnomer) + "-" + t_objResidue.getName() );
                } 
                catch (GlycoconjugateException e)
                {
                    throw new SugarImporterException("KCF050",this.m_iPosition);
                }
                this.m_cAnomer = ' ';
            }
        }
        // { " " } <number> " " { " " } <number> [ ":" <link_information> ] " " 
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF020", this.m_iPosition);
        }
        this.nextToken();
        // { " " } <number> " " { " " } <number> [ ":" <link_information> ] " " { " " }
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number> " " { " " } <number> [ ":" <link_information> ] " " { " " } <number>
        t_iNodeTwo = this.number();
        if ( !this.m_hResidues.containsKey( t_iNodeTwo ) )
        {
            throw new SugarImporterException("KCF027", this.m_iPosition);
        }
        // { " " } <number> " " { " " } <number> [ ":" <link_information> ] " " { " " } <number> [ ":" ]
        if ( this.m_cToken == ':' )
        {
            this.nextToken();
            // { " " } <number> " " { " " } <number> [ ":" <link_information> ] " " { " " } <number> [ ":" <link_information> ]
            this.link_information();
            t_iLinkagePositionTwo = this.m_iLinkagePosition;
            // add the anomer to the residue name
            if ( this.m_cAnomer != ' ' )
            {
                UnvalidatedGlycoNode t_objResidue = this.m_hResidues.get( t_iNodeTwo ).getResidue(); 
                try
                {
                    t_objResidue.setName( String.format("%c",this.m_cAnomer) + "-" + t_objResidue.getName() );
                } 
                catch (GlycoconjugateException e)
                {
                    throw new SugarImporterException("KCF050",this.m_iPosition);
                }
                this.m_cAnomer = ' ';
            }
        }             
        // { " " } <number> " " { " " } <number> [ ":" <link_information> ] " " { " " } <number> [ ":" <link_information> ] { " " } 
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number> " " { " " } <number> [ ":" <link_information> ] " " { " " } <number> [ ":" <link_information> ] { " " } "\n"
        if ( this.m_cToken != '\n' )
        {
            throw new SugarImporterException("KCF021", this.m_iPosition);
        }
        this.nextToken();
        // Store edges
        KCFLinkage t_objKcfLinkage = new KCFLinkage(t_iLinkagePositionOne,
                t_iLinkagePositionTwo,  t_iNodeOne, t_iNodeTwo);
        this.m_aLinkages.add(t_objKcfLinkage);
    }

    /**
     * link_information  ::= ["a" | "b"]  [ ( <number> [ "," <number> ] ) | ( * [ "1" ] ) ]
     * @throws SugarImporterException 
     */
    private void link_information() throws SugarImporterException
    {
        int t_iDigit = 0;
        if ( this.m_cToken == 'a' || this.m_cToken == 'b' )
        {
            this.m_cAnomer = this.m_cToken;
            this.nextToken();
        }
        else if ( this.m_cToken == '*' )
        {
            this.m_cAnomer = ' ';
            this.nextToken();
        }
        else
        {
            this.m_cAnomer = ' ';
        }
        t_iDigit = (int)this.m_cToken;
        if ( t_iDigit > 48 && t_iDigit < 58 )
        {
            this.m_iLinkagePosition = this.number();
            
//            if ( this.m_cToken == ',' )
//            {
//                this.nextToken();
//                this.number();
//            }
        }
        else
        {
            this.m_iLinkagePosition = Linkage.UNKNOWN_POSITION;
        }
    }

    // nodes        ::= "N" "O" "D" "E" " " { " " } <number> "\n" <node> { <node> }
    private void nodes() throws SugarImporterException 
    {
        // "N" 
        if ( this.m_cToken != 'N' )
        {
            throw new SugarImporterException("KCF008", this.m_iPosition);
        }
        this.nextToken();
        // "N" "O" 
        if ( this.m_cToken != 'O' )
        {
            throw new SugarImporterException("KCF008", this.m_iPosition);
        }
        this.nextToken();
        // "N" "O" "D" 
        if ( this.m_cToken != 'D' )
        {
            throw new SugarImporterException("KCF008", this.m_iPosition);
        }
        this.nextToken();
        // "N" "O" "D" "E" 
        if ( this.m_cToken != 'E' )
        {
            throw new SugarImporterException("KCF008", this.m_iPosition);
        }
        this.nextToken();
        // "N" "O" "D" "E" " " 
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF032", this.m_iPosition);
        }
        this.nextToken();
        // "N" "O" "D" "E" " " { " " } 
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // "N" "O" "D" "E" " " { " " } <number> 
        this.m_iResidueCount = this.number();
        // "N" "O" "D" "E" " " { " " } <number> "\n" 
        if ( this.m_cToken != '\n' )
        {
            throw new SugarImporterException("KCF033", this.m_iPosition);
        }
        this.nextToken();
        // "N" "O" "D" "E" " " { " " } <number> "\n" <node> 
        this.node();
        // "N" "O" "D" "E" " " { " " } <number> "\n" <node> { <node> } "E" 
        while ( this.m_cToken != 'E' )
        {
            this.node();
        }
        if ( this.m_iResidueCount != this.m_hResidues.size() )
        {
            throw new SugarImporterException("KCF013");
        }
    }

    // node            ::= { " " } <number> " " { " " } <glycan_name> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> "\n"
    private void node() throws SugarImporterException 
    {
        int t_iStart = 0;
        Integer t_iID = 0;
        double t_dX = 0;
        double t_dY = 0;
        UnvalidatedGlycoNode t_objResidue = new UnvalidatedGlycoNode();
        KCFResidue t_objKCFResidue = new KCFResidue(); 
        // { " " }
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number> 
        t_iID = this.number();
        if ( this.m_hResidues.containsKey(t_iID) )
        {
            throw new SugarImporterException("KCF014", this.m_iPosition);
        }
        // { " " } <number> " " 
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF009", this.m_iPosition);
        }
        this.nextToken();
        // { " " } <number> " " { " " } 
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number> " " { " " } <glycan_name>
        t_iStart = this.m_iPosition;
        this.glycan_name();
        try
        {
            t_objResidue.setName(this.m_strText.substring(t_iStart,this.m_iPosition));
        } 
        catch (GlycoconjugateException e)
        {
            throw new SugarImporterException("KCF050",this.m_iPosition);
        }
        // { " " } <number> " " { " " } <glycan_name> " " 
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF010", this.m_iPosition);
        }
        this.nextToken();
        // { " " } <number> " " { " " } <glycan_name> " " { " " } 
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number> " " { " " } <glycan_name> " " { " " } <signed_dec_number>
        t_dX = this.float_number_signed();
        // { " " } <number> " " { " " } <glycan_name> " " { " " } <signed_dec_number> " " 
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF011", this.m_iPosition);
        }
        this.nextToken();
        // { " " } <number> " " { " " } <glycan_name> " " { " " } <signed_dec_number> " " { " " } 
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number> " " { " " } <glycan_name> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number>
        t_dY = this.float_number_signed();
        // { " " } <number> " " { " " } <glycan_name> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> "\n"
        if ( this.m_cToken != '\n' )
        {
            throw new SugarImporterException("KCF012", this.m_iPosition);
        }
        this.nextToken();
        // store the residue
        t_objKCFResidue.init( t_objResidue , t_dX , t_dY , t_iID );
        this.m_hResidues.put(t_iID,t_objKCFResidue);
    }

    /**
     *  glycan_name  ::= <symbol> { <symbol> }  
     */
    private void glycan_name() throws SugarImporterException
    {
        // <symbol> 
        this.symbol();
        // <symbol> { <symbol> }
        while ( this.m_cToken != ' ' )
        {
            this.symbol();
        }
        
    }

    /**
     * symbol       ::= <character> | "0" | ... | "9" | "/" | "-" | "*" | "," | "(" | ")"
     * @throws SugarImporterException 
     * 
     */
    private void symbol() throws SugarImporterException
    {
        int t_iDigit = (int) this.m_cToken;
        // "0" | ... | "9"
        if ( t_iDigit > 47 && t_iDigit < 58 )
        {
            this.nextToken();
        }
        else
        {
            // / | "-"
            if ( this.m_cToken == '/' || this.m_cToken == '-' || this.m_cToken == '*'  || this.m_cToken == ',' )
            {
                this.nextToken();
            }
            else
            {
                if ( this.m_cToken == '(' || this.m_cToken == ')' )
                {
                    this.nextToken();
                }
                else
                {
                    this.character();
                }
            }
        }
    }

    // "E" "N" "T" "R" "Y" " " { " " } <go_number> " " { " " } "G" "l" "y" "c" "a" "n" "\n"
    private void head() throws SugarImporterException 
    {
        // "E" 
        if ( this.m_cToken != 'E' )
        {
            throw new SugarImporterException("KCF002", this.m_iPosition);
        }
        this.nextToken();
        // "E" "N" 
        if ( this.m_cToken != 'N' )
        {
            throw new SugarImporterException("KCF002", this.m_iPosition);
        }
        this.nextToken();
        // "E" "N" "T" 
        if ( this.m_cToken != 'T' )
        {
            throw new SugarImporterException("KCF002", this.m_iPosition);
        }
        this.nextToken();
        // "E" "N" "T" "R" 
        if ( this.m_cToken != 'R' )
        {
            throw new SugarImporterException("KCF002", this.m_iPosition);
        }
        this.nextToken();
        // "E" "N" "T" "R" "Y" 
        if ( this.m_cToken != 'Y' )
        {
            throw new SugarImporterException("KCF002", this.m_iPosition);
        }
        this.nextToken();
        // "E" "N" "T" "R" "Y" " "
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF003", this.m_iPosition);
        }
        this.nextToken();
        // "E" "N" "T" "R" "Y" " " { " " } 
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // "E" "N" "T" "R" "Y" " " { " " } <go_number>
        this.go_number();
        // "E" "N" "T" "R" "Y" " " { " " } <gennumber> " "
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF004", this.m_iPosition);
        }
        this.nextToken();
        // "E" "N" "T" "R" "Y" " " { " " } <go_number> " " { " " } 
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // "E" "N" "T" "R" "Y" " " { " " } <go_number> " " { " " } "G" 
        if ( this.m_cToken != 'G' )
        {
            throw new SugarImporterException("KCF005", this.m_iPosition);
        }
        this.nextToken();
        // "E" "N" "T" "R" "Y" " " { " " } <go_number> " " { " " } "G" "l"
        if ( this.m_cToken != 'l' )
        {
            throw new SugarImporterException("KCF005", this.m_iPosition);
        }
        this.nextToken();
        // "E" "N" "T" "R" "Y" " " { " " } <go_number> " " { " " } "G" "l" "y" 
        if ( this.m_cToken != 'y' )
        {
            throw new SugarImporterException("KCF005", this.m_iPosition);
        }
        this.nextToken();
        // "E" "N" "T" "R" "Y" " " { " " } <go_number> " " { " " } "G" "l" "y" "c" 
        if ( this.m_cToken != 'c' )
        {
            throw new SugarImporterException("KCF005", this.m_iPosition);
        }
        this.nextToken();
        // "E" "N" "T" "R" "Y" " " { " " } <go_number> " " { " " } "G" "l" "y" "c" "a" 
        if ( this.m_cToken != 'a' )
        {
            throw new SugarImporterException("KCF005", this.m_iPosition);
        }
        this.nextToken();
        // "E" "N" "T" "R" "Y" " " { " " } <go_number> " " { " " } "G" "l" "y" "c" "a" "n" 
        if ( this.m_cToken != 'n' )
        {
            throw new SugarImporterException("KCF005", this.m_iPosition);
        }
        this.nextToken();
        // "E" "N" "T" "R" "Y" " " { " " } <go_number> " " { " " } "G" "l" "y" "c" "a" "n" "\n"
        if ( this.m_cToken != '\n' )
        {
            throw new SugarImporterException("KCF006", this.m_iPosition);
        }
        this.nextToken();
    }

    // go_number     ::= "G" ( "0" | ... | "9" ) { ( "0" | ... | "9" ) }
    private void go_number() throws SugarImporterException 
    {
        int t_iStart = this.m_iPosition;
        // "G" 
        if ( this.m_cToken != 'G' )
        {
            throw new SugarImporterException("KCF007", this.m_iPosition);
        }
        this.nextToken();
        // "G" ( "0" | ... | "9" )  
        int t_iDigit = (int) this.m_cToken;
        if ( t_iDigit < 48 || t_iDigit > 57 )
        {
            throw new SugarImporterException("KCF030" , this.m_iPosition);
        }
        this.nextToken();
        // "G" ( "0" | ... | "9" ) { ( "0" | ... | "9" ) }
        t_iDigit = (int) this.m_cToken;
        while ( t_iDigit > 47 && t_iDigit < 58 )
        {
            this.nextToken();
            t_iDigit = (int) this.m_cToken;
        }
        this.m_strGONumber = this.m_strText.substring(t_iStart,this.m_iPosition);
    }

    /**
     * bracket        ::= "B" "R" "A" "C" "K" "E" "T" <bracket_line> <bracket_line> <bracket_final> {<bracket_line> <bracket_line> <bracket_final> }
     */
    private void bracket() throws SugarImporterException
    {
        // "B" 
        if ( this.m_cToken != 'B' )
        {
            throw new SugarImporterException("KCF040", this.m_iPosition);
        }
        this.nextToken();
        // "B" "R" 
        if ( this.m_cToken != 'R' )
        {
            throw new SugarImporterException("KCF040", this.m_iPosition);
        }
        this.nextToken();
        // "B" "R" "A" 
        if ( this.m_cToken != 'A' )
        {
            throw new SugarImporterException("KCF040", this.m_iPosition);
        }
        this.nextToken();
        // "B" "R" "A" "C" 
        if ( this.m_cToken != 'C' )
        {
            throw new SugarImporterException("KCF040", this.m_iPosition);
        }
        this.nextToken();
        // "B" "R" "A" "C" "K" 
        if ( this.m_cToken != 'K' )
        {
            throw new SugarImporterException("KCF040", this.m_iPosition);
        }
        this.nextToken();
        // "B" "R" "A" "C" "K" "E" 
        if ( this.m_cToken != 'E' )
        {
            throw new SugarImporterException("KCF040", this.m_iPosition);
        }
        this.nextToken();
        // "B" "R" "A" "C" "K" "E" "T"
        if ( this.m_cToken != 'T' )
        {
            throw new SugarImporterException("KCF040", this.m_iPosition);
        }
        this.nextToken();        
        // "B" "R" "A" "C" "K" "E" "T" <bracket_line>
        KCFBlock t_objBlock = new KCFBlock();
        this.bracket_line(t_objBlock);
        // "B" "R" "A" "C" "K" "E" "T" <bracket_line> <bracket_line>
        this.bracket_line(t_objBlock);
        // "B" "R" "A" "C" "K" "E" "T" <bracket_line> <bracket_line> <bracket_final>
        this.bracket_final(t_objBlock);
        this.m_aBlock.add(t_objBlock);
        // "B" "R" "A" "C" "K" "E" "T" <bracket_line> <bracket_line> <bracket_final> {<bracket_line> <bracket_line> <bracket_final> }
        while ( this.m_cToken != '/' )
        {
            t_objBlock = new KCFBlock();
            // <bracket_line>
            this.bracket_line(t_objBlock);
            // <bracket_line> <bracket_line>
            this.bracket_line(t_objBlock);
            // <bracket_line> <bracket_line> <bracket_final>
            this.bracket_final(t_objBlock);
            this.m_aBlock.add(t_objBlock);
        }
    }

    /**
     * bracket_line   ::= { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> "\n"
     * @param a_objBlock 
     * @throws SugarImporterException 
     */
    private void bracket_line(KCFBlock a_objBlock) throws SugarImporterException
    {
        // { " " }
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number>
        this.number();
        // { " " } <number> " " 
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF043", this.m_iPosition);
        }
        this.nextToken();
        // { " " } <number> " " { " " } 
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number> " " { " " } <signed_dec_number>
        double t_dCoo1 = this.float_number_signed();
        // { " " } <number> " " { " " } <signed_dec_number> " " 
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF044", this.m_iPosition);
        }
        this.nextToken();
        // { " " } <number> " " { " " } <signed_dec_number> " " { " " } 
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number>
        double t_dCoo2 = this.float_number_signed();
        // { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " "
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF044", this.m_iPosition);
        }
        this.nextToken();
        // { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } 
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number>
        double t_dCoo3 = this.float_number_signed();        
        // { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " 
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF044", this.m_iPosition);
        }
        this.nextToken();
        // { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " }
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number>
        double t_dCoo4 = this.float_number_signed();
        // { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> "\n"
        if ( this.m_cToken != '\n' )
        {
            throw new SugarImporterException("KCF045", this.m_iPosition);
        }
        this.nextToken();
        if ( t_dCoo1 != t_dCoo3 )
        {
            throw new SugarImporterException("KCF015", this.m_iPosition);
        }
        a_objBlock.setLeftRight(t_dCoo1);
        a_objBlock.setUpDown(t_dCoo2,t_dCoo4);
    }

    /**
     * bracket_final     ::= { " " } <number> " " { " " } ( <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } ( "n" [ "-" <number> ] | <number> ) | "n" | "m" ) "\n"
     * @param a_objBlock 
     * @throws SugarImporterException 
     */
    private void bracket_final(KCFBlock a_objBlock) throws SugarImporterException
    {
        // { " " }
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        // { " " } <number>
        this.number();
        // { " " } <number> " "
        if ( this.m_cToken != ' ' )
        {
            throw new SugarImporterException("KCF046", this.m_iPosition);
        }
        this.nextToken();
        // { " " } <number> " " { " " } 
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        if ( this.m_cToken == 'n' )
        {
            this.nextToken();
            a_objBlock.setMin( SugarUnitRepeat.UNKNOWN );
            a_objBlock.setMax( SugarUnitRepeat.UNKNOWN );
        }
        else
        {
            if ( this.m_cToken == 'm' )
            {
                this.nextToken();
                a_objBlock.setMin( SugarUnitRepeat.UNKNOWN );
                a_objBlock.setMax( SugarUnitRepeat.UNKNOWN );
            }
            else
            {
                // { " " } <number> " " { " " } <signed_dec_number>
                this.float_number_signed();
                // { " " } <number> " " { " " } <signed_dec_number> " " 
                if ( this.m_cToken != ' ' )
                {
                    throw new SugarImporterException("KCF047", this.m_iPosition);
                }
                this.nextToken();
                // { " " } <number> " " { " " } <signed_dec_number> " " { " " } 
                while ( this.m_cToken == ' ' )
                {
                    this.nextToken();
                }
                // { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> 
                this.float_number_signed();
                // { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " 
                if ( this.m_cToken != ' ' )
                {
                    throw new SugarImporterException("KCF047", this.m_iPosition);
                }
                this.nextToken();
                // { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } 
                while ( this.m_cToken == ' ' )
                {
                    this.nextToken();
                }
                // { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } ( "n" | <number> )
                if ( this.m_cToken == 'n' )
                {
                    a_objBlock.setMin( SugarUnitRepeat.UNKNOWN );
                    a_objBlock.setMax( SugarUnitRepeat.UNKNOWN );
                    this.nextToken();
                    if ( this.m_cToken == '-' )
                    {
                        // "n" [ "-" <number> ]
                        this.nextToken();
                        a_objBlock.setMax( this.number() );
                    }
                }
                else
                {   
                    if ( this.m_cToken == 'm' )
                    {
                        a_objBlock.setMin( SugarUnitRepeat.UNKNOWN );
                        a_objBlock.setMax( SugarUnitRepeat.UNKNOWN );
                        this.nextToken();
                        if ( this.m_cToken == '-' )
                        {
                            // "n" [ "-" <number> ]
                            this.nextToken();
                            a_objBlock.setMax( this.number() );
                        }
                    }
                    else
                    {   
                        // <number>
                        a_objBlock.setMin( this.number() );
                        a_objBlock.setMax( this.number() );
                        // <number> [ "-" <number> ]
                        if ( this.m_cToken == '-' )
                        {
                            // "n" [ "-" <number> ]
                            this.nextToken();
                            a_objBlock.setMax( this.number() );
                        }
                    }
                }
            }
        }
        // { " " } <number> " " { " " } <signed_dec_number> " " { " " } <signed_dec_number> " " { " " } ( "n" | <number> ) "\n"
        if ( this.m_cToken != '\n' )
        {
            throw new SugarImporterException("KCF048", this.m_iPosition);
        }
        this.nextToken();
    }
    
    public String getGONumber()
    {
        return this.m_strGONumber;
    }

    /**
     * @throws SugarImporterException 
     * 
     */
    private void createSugar() throws SugarImporterException
    {
        this.m_objSugar = new Sugar();
        KCFResidue t_objResidue = this.findRootResidue();
        if ( t_objResidue != null )
        {
            try 
            {
                this.m_objSugar.addNode( t_objResidue.getResidue() );
                this.m_iResidueCount++;
                this.addChildResidue(t_objResidue,this.m_objSugar);
            } 
            catch (GlycoconjugateException e) 
            {
                throw new SugarImporterException("KCF050",this.m_iPosition);
            }
        }
    }

    private KCFResidue findRootResidue() throws SugarImporterException 
    {
        KCFResidue t_objResult = null;
        boolean t_bDouble = false;
        for (Iterator<KCFResidue> t_iterNodes = this.m_hResidues.values().iterator(); t_iterNodes.hasNext();) 
        {
            KCFResidue t_objElement = t_iterNodes.next();
            if ( t_objResult == null )
            {
                t_objResult = t_objElement;
            }
            else
            {
                if ( t_objResult.getX() < t_objElement.getX() )
                {
                    t_objResult = t_objElement;
                    t_bDouble = false;
                }
                else
                {
                    if ( t_objResult.getX() == t_objElement.getX() )
                    {
                        t_bDouble = true;
                    }
                }
            }
        }
        if ( t_bDouble )
        {
            throw new SugarImporterException("KCF019", this.m_iPosition);
        }
        return t_objResult;
    }

    private void addChildResidue(KCFResidue a_objResidue,GlycoGraph a_objGraph) throws GlycoconjugateException 
    {
        KCFResidue t_objResChild;
        GlycoEdge t_objEdge;
        int t_iId = a_objResidue.getId();
        for (Iterator<KCFLinkage> t_iterLinkages = this.m_aLinkages.iterator(); t_iterLinkages.hasNext();) 
        {
            KCFLinkage t_objLinkage = t_iterLinkages.next();
            if ( t_objLinkage.getResidueOne() == t_iId || t_objLinkage.getResidueTwo() == t_iId )
            {
                if ( !this.m_aHandledLinkages.contains(t_objLinkage) )
                {
                    // linkage not handled before
                    if ( t_objLinkage.getResidueOne() == t_iId )
                    {
                        t_objResChild = this.m_hResidues.get(t_objLinkage.getResidueTwo());
                        t_objEdge = t_objLinkage.getEdge(true);                        
                    }
                    else
                    {
                        t_objResChild = this.m_hResidues.get(t_objLinkage.getResidueOne());
                        t_objEdge = t_objLinkage.getEdge(false);
                    }
                    a_objGraph.addNode(t_objResChild.getResidue());
                    this.m_iResidueCount++;
                    a_objGraph.addEdge(a_objResidue.getResidue(),t_objResChild.getResidue(),t_objEdge);
                    this.m_iEdgeCount++;
                    this.m_aHandledLinkages.add(t_objLinkage);
                    this.addChildResidue(t_objResChild,a_objGraph);
                }
            }
        }        
    }

}
