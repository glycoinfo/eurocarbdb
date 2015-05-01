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
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.SugarImporterText;
import org.eurocarbdb.MolecularFramework.sugar.Anomer;
import org.eurocarbdb.MolecularFramework.sugar.BaseType;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraphAlternative;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.LinkageType;
import org.eurocarbdb.MolecularFramework.sugar.Modification;
import org.eurocarbdb.MolecularFramework.sugar.ModificationType;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.SubstituentType;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.Superclass;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;

/**
* start                 ::= <residue_block> <linkage_block> [ <repeat_block> ] [ <underdeterminded_block> ] [ <alternative_block> ] { <non_monosaccharide> }
* residue_block        ::= "R" "E" "S" <zeilen_abschluss> <residue_entry> <zeilen_abschluss> { <residue_entry> <zeilen_abschluss> } 
* linkage_block        ::= "L" "I" "N" <zeilen_abschluss> { <linkage> <zeilen_abschluss> }   
* residue_entry        ::= <number> <residue>
* residue              ::= "b" ":" <anomer> "-" <config> "-" <superclass> "-" <ring_start> ":" <ring_end>
*                          | "s" <substituent>  
* linkage              ::= <number> ":" <number> <linkagetype> "(" <number> "+" <number> ")" <number> <linkagetype>
* repeat_block            ::= "R" "E" "P" <zeilen_abschluss> <repeat_unit> { <repeat_unit> }
* repeat_unit            ::= "R" "E" "P" <number> ":" <number> <linkagetype> "(" <number> "+" <number> ")" <number> <linkagetype> "=" <number> "-" <number> <zeilen_abschluss> <residue_block> <linkage_block>
* 
* alternative_block    ::= "A" "L" "T" <zeilen_abschluss> <alternative_unit> { <alternative_unit> }
* alternative_unit        ::= "A" "L" "T" <number> <zeilen_abschluss> <alternative_graph> <alternative_graph> { <alternative_graph> }
* alternative_graph    ::= "L" "E" "A" "D" "-" "I" "N" " " "R" "E" "S" ":" <number> <zeilen_abschluss> "L" "E" "A" "D" "-" "O" "U" "T" " " "R" "E" "S" ":" { <number>+<number> "|" } <residue_block> <linkage_block> 
* underdeterminded_block    ::= "U" "N" "D" <zeilen_abschluss> <underdeterminded_unit> { <underdeterminded_unit> }
* underdeterminded_unit    ::= "U" "N" "D" <number> ":" <float_number> ":" <float_number> <zeilen_abschluss> "ParentIDs:" <number> { "|" <number> } <zeilen_abschluss> "SubtreeLinkageID" <number> <linkage_type> "(" <number> { "|" <number> } "+" <number> { "|" <number> } ")" <linkage_type> <zeilen_abschluss> <residue_block> <linkage_block>    
*  
*  Sonderregeln : anomber, superclass , linkagetype 
*                  => ans object model gebunden
* @author Logan
*
*/
public class SugarImporterGlycoCTCondensed extends SugarImporterText 
{
    private HashMap<Integer,GlycoNode> m_hashResidues = new HashMap<Integer,GlycoNode>(); 
    private HashMap<Integer,Linkage> m_hashLinkages = new HashMap<Integer,Linkage>();
    private HashMap<Integer,SugarUnitRepeat> m_hashRepeats = new HashMap<Integer,SugarUnitRepeat>();
    private HashMap<Integer,SugarUnitAlternative> m_hashAlternatives = new HashMap<Integer,SugarUnitAlternative>();
    private GlycoGraph m_objSugarUnit = null;
    private HashMap<GlycoNode,GlycoGraph> m_hashGraphs = new HashMap<GlycoNode,GlycoGraph>(); 
    private int m_iNonNumber = 0;
    private String m_strLineSeparator = null;
    
    public void setLineSeparator(String a_strSep) throws SugarImporterException
    {
        if ( a_strSep == null )
        {
            this.m_strLineSeparator = null;
        }
        else
        {
            if ( a_strSep.length() < 1 )
            {
                throw new SugarImporterException("GLYCOCTC046", this.m_iPosition);
            }
            if ( a_strSep.indexOf("$") != -1 )
            {
                throw new SugarImporterException("GLYCOCTC047", this.m_iPosition);
            }
            this.m_strLineSeparator = a_strSep;
        }
    }
    
    public String getLineSeparator()
    {
        return this.m_strLineSeparator;
    }
    
    /**
     * @see org.eurocarbdb.MolecularFramework.io.SugarImporterText#start()
     */
    @Override
    protected void start() throws SugarImporterException
    {
        this.clear();
        try 
        {
            this.m_objSugarUnit = this.m_objSugar;
            this.residue_block();
            if ( this.m_cToken == 'L' )
            {
                this.linkage_block();
            }
            if ( this.m_cToken == 'R' )
            {
                this.repeat_block();
            }
            if ( this.m_cToken == 'U' )
            {
                this.underdeterminded_block();
            }
            if ( this.m_cToken == 'A' )
            {
                this.alternative_block();
            }
            while ( this.m_cToken == 'N' )
            {
                this.non_monosaccharide();
            }
        } 
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("COMMON013", this.m_iPosition);
        }  
        if ( ! this.finished() )
        {
            throw new SugarImporterException("GLYCOCTC018", this.m_iPosition);
        }
    }    
    

    /**
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     * 
     */
    private void non_monosaccharide() throws SugarImporterException, GlycoconjugateException 
    {
//        NON1
//        Parent:1  || Child:2
//        Linkage:o(1+1)n
//        Peptide:N
        if ( this.m_cToken != 'N' )
        {
            throw new SugarImporterException("GLYCOCTC038", this.m_iPosition);     
        }
        this.nextToken();
        if ( this.m_cToken != 'O' )
        {
            throw new SugarImporterException("GLYCOCTC038", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken != 'N' )
        {
            throw new SugarImporterException("GLYCOCTC038", this.m_iPosition);
        }
        this.nextToken();
        int t_iNumber = this.number();
        this.m_iNonNumber++;
        if ( t_iNumber != this.m_iNonNumber )
        {
            throw new SugarImporterException("GLYCOCTC039", this.m_iPosition);
        }
        this.zeilen_abschluss();
        boolean t_bParent = false;
        if ( this.m_cToken == 'P')
        {
            this.parseString("Parent:");
            t_bParent = true;
        }
        else
        {
            this.parseString("Child:");
        }
        int t_iResidue = this.number();
        this.zeilen_abschluss();
        this.parseString("Linkage:");
        GlycoEdge t_objEdge = this.specialLinkage();
        this.zeilen_abschluss();
        String t_strName = "";
        if ( this.m_cToken == 'H' )
        {
            this.parseString("HistoricalEntity:");
            t_strName = this.parseNonMsName();
        }
        else if ( this.m_cToken == 'S' )
        {
            this.parseString("SmallMolecule:");
            t_strName = this.parseNonMsName();
        }
        else if ( this.m_cToken == 'P' && this.aheadToken(1) == 'e' )
        {
            this.parseString("Peptide:");
            t_strName = this.parseNonMsName();
        }
        else
        {
            throw new SugarImporterException("GLYCOCTC041", this.m_iPosition);
        }
        NonMonosaccharide t_objNonMs = new NonMonosaccharide(t_strName);
        GlycoNode t_objRes = this.m_hashResidues.get(t_iResidue);
        if ( t_objRes == null )
        {
            throw new SugarImporterException("GLYCOCTC042", this.m_iPosition);
        }
        GlycoGraph t_objGraph = this.m_hashGraphs.get(t_objRes);
        if ( t_objGraph == null )
        {
            throw new SugarImporterException("GLYCOCTC043", this.m_iPosition);
        }
        t_objGraph.addNode(t_objNonMs);
        if ( t_bParent )
        {            
            t_objGraph.addEdge(t_objRes, t_objNonMs, t_objEdge);
        }
        else
        {
            if ( t_objRes.getParentEdge() != null )
            {
                throw new SugarImporterException("GLYCOCTC044", this.m_iPosition);
            }
            t_objGraph.addEdge(t_objNonMs, t_objRes, t_objEdge);
        }
    }


    /**
     * @return
     * @throws SugarImporterException 
     */
    private String parseNonMsName() throws SugarImporterException 
    {
        String t_strResult = "";
        if ( this.m_strLineSeparator == null )
        {
            while ( this.m_cToken != '\n' && this.m_cToken != '\r' && this.m_cToken != '$' )
            {
                t_strResult += this.m_cToken;
                this.nextToken();            
            }
        }
        else
        {
            while ( this.m_cToken != this.m_strLineSeparator.charAt(0) && this.m_cToken != '$' )
            {
                t_strResult += this.m_cToken;
                this.nextToken();            
            }
        }
        return t_strResult;
    }


    /**
     * @param string
     * @throws SugarImporterException 
     */
    private void parseString(String a_strString) throws SugarImporterException 
    {
        int t_iLength = a_strString.length()-1;
        String t_strString = "" + this.m_cToken;
        while (t_iLength > 0) 
        {
            t_iLength--;
            this.nextToken();
            t_strString += this.m_cToken;
        }
        if ( !t_strString.equals(a_strString) )
        {
            throw new SugarImporterException("GLYCOCTC040", this.m_iPosition);
        }
        this.nextToken();
    }


    /**
     * o(1+1)n
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private GlycoEdge specialLinkage() throws SugarImporterException, GlycoconjugateException 
    {
        Linkage t_objLinkage = new Linkage();
        LinkageType t_objLinkType;
        try 
        {
            t_objLinkType = LinkageType.forName(this.m_cToken);
            t_objLinkage.setParentLinkageType(t_objLinkType);
            this.nextToken();
        } 
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("GLYCOCTC013", this.m_iPosition);
        }
        // "(" <number> "+" <number> ")"  <linkagetype>
        if ( this.m_cToken != '(' )
        {
            throw new SugarImporterException("GLYCOCTC014", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken == '-' )
        {
            this.nextToken();
            if ( this.m_cToken != '1' )
            {
                throw new SugarImporterException("GLYCOCTC035", this.m_iPosition);
            }
            this.nextToken();
            t_objLinkage.addParentLinkage(Linkage.UNKNOWN_POSITION);
        }
        else
        {
            t_objLinkage.addParentLinkage(this.number());
            while( this.m_cToken == '|' )
            {
                this.nextToken();
                t_objLinkage.addParentLinkage(this.number());
            }
        }
        // "+" <number> ")"  <linkagetype>
        if ( this.m_cToken != '+' )
        {
            throw new SugarImporterException("GLYCOCTC015", this.m_iPosition);
        }
        this.nextToken();
        // <number> ")"  <linkagetype>
        if ( this.m_cToken == '-' )
        {
            this.nextToken();
            if ( this.m_cToken != '1' )
            {
                throw new SugarImporterException("GLYCOCTC035", this.m_iPosition);
            }
            this.nextToken();
            t_objLinkage.addChildLinkage(Linkage.UNKNOWN_POSITION);
        }
        else
        {
            t_objLinkage.addChildLinkage(this.number());
            while( this.m_cToken == '|' )
            {
                this.nextToken();
                t_objLinkage.addChildLinkage(this.number());
            }
        }
        if ( this.m_cToken != ')' )
        {
            throw new SugarImporterException("GLYCOCTC016", this.m_iPosition);
        }
        this.nextToken();
        // <linkagetype>
        try 
        {
            t_objLinkType = LinkageType.forName(this.m_cToken);
            t_objLinkage.setChildLinkageType(t_objLinkType);
            this.nextToken();
        } 
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("GLYCOCTC013", this.m_iPosition);
        }
        GlycoEdge t_objEdge = new GlycoEdge();
        t_objEdge.addGlycosidicLinkage(t_objLinkage);
        return t_objEdge;
    }


    /**
     * "U" "N" "D" <zeilen_abschluss> <underdeterminded_unit> { <underdeterminded_unit> }
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void underdeterminded_block() throws SugarImporterException, GlycoconjugateException 
    {
        if ( this.m_cToken != 'U' )
        {
            throw new SugarImporterException("GLYCOCTC028", this.m_iPosition);     
        }
        this.nextToken();
        if ( this.m_cToken != 'N' )
        {
            throw new SugarImporterException("GLYCOCTC028", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken != 'D' )
        {
            throw new SugarImporterException("GLYCOCTC028", this.m_iPosition);
        }
        this.nextToken();
        this.zeilen_abschluss();
        this.underdeterminded_unit();
        while ( this.m_cToken == 'U' )
        {
            this.underdeterminded_unit();
        }
    }


    /**
     * "U" "N" "D" <number> ":" <float_number> ":" <float_number> <zeilen_abschluss> "ParentIDs:" <number> { "|" <number> } <zeilen_abschluss> "SubtreeLinkageID" <number> <linkage_type> "(" <number> { "|" <number> } "+" <number> { "|" <number> } ")" <linkage_type> <zeilen_abschluss> <residue_block> <linkage_block> 
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void underdeterminded_unit() throws SugarImporterException, GlycoconjugateException 
    {
        // "U" "N" "D" <number> ":" <float_number> ":" <float_number> <zeilen_abschluss> "ParentIDs:" <number> { "|" <number> } <zeilen_abschluss> "SubtreeLinkageID" <number> <linkage_type> "(" <number> { "|" <number> } "+" <number> { "|" <number> } ")" <linkage_type> <zeilen_abschluss> <residue_block> <linkage_block>
        if ( this.m_cToken != 'U' )
        {
            throw new SugarImporterException("GLYCOCTC028", this.m_iPosition);     
        }
        this.nextToken();
        if ( this.m_cToken != 'N' )
        {
            throw new SugarImporterException("GLYCOCTC028", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken != 'D' )
        {
            throw new SugarImporterException("GLYCOCTC028", this.m_iPosition);
        }
        this.nextToken();
        this.number();
        UnderdeterminedSubTree t_objUnderSubtree = new UnderdeterminedSubTree();
        if ( this.m_cToken != ':' )
        {
            throw new SugarImporterException("GLYCOCTC005", this.m_iPosition);     
        }
        this.nextToken();
        double t_dMinStat = this.float_number_signed();
        if ( this.m_cToken != ':' )
        {
            throw new SugarImporterException("GLYCOCTC005", this.m_iPosition);     
        }
        this.nextToken();
        double t_dMaxStat = this.float_number_signed();
        this.zeilen_abschluss();
        t_objUnderSubtree.setProbability(t_dMinStat,t_dMaxStat);
        // "ParentIDs:" <number> { "|" <number> } <zeilen_abschluss> "SubtreeLinkageID" <number> <linkage_type> "(" <number> { "|" <number> } "+" <number> { "|" <number> } ")" <linkage_type> <zeilen_abschluss> <residue_block> <linkage_block>
        String t_strTemp = "";
        for (int t_iCounter = 0; t_iCounter < 10; t_iCounter++) 
        {
            t_strTemp += this.m_cToken;
            this.nextToken();
        }
        if ( !t_strTemp.equals("ParentIDs:") )
        {
            throw new SugarImporterException("GLYCOCTC029", this.m_iPosition);
        }
        ArrayList<GlycoNode> t_aParents = new ArrayList<GlycoNode>();
        GlycoNode t_objNode = this.m_hashResidues.get(this.number());
        if ( t_objNode == null )
        {
            throw new SugarImporterException("GLYCOCTC032", this.m_iPosition);
        }
        t_aParents.add(t_objNode);        
        while ( this.m_cToken == '|' )
        {
            this.nextToken();
            t_objNode = this.m_hashResidues.get(this.number());
            if ( t_objNode == null )
            {
                throw new SugarImporterException("GLYCOCTC032", this.m_iPosition);
            }
            t_aParents.add(t_objNode);
        }
        this.zeilen_abschluss();
        // "SubtreeLinkageID" <number> <linkage_type> "(" <number> { "|" <number> } "+" <number> { "|" <number> } ")" <linkage_type> <zeilen_abschluss> <residue_block> <linkage_block>
        t_strTemp = "";
        for (int t_iCounter = 0; t_iCounter < 16; t_iCounter++) 
        {
            t_strTemp += this.m_cToken;
            this.nextToken();
        }
        if ( !t_strTemp.equals("SubtreeLinkageID") )
        {
            throw new SugarImporterException("GLYCOCTC030", this.m_iPosition);
        }
        this.number();
        if ( this.m_cToken != ':' )
        {
            throw new SugarImporterException("GLYCOCTC005", this.m_iPosition);
        }
        this.nextToken();
        Linkage t_objLinkage = new Linkage();
        LinkageType t_objLinkType;
        try 
        {
            t_objLinkType = LinkageType.forName(this.m_cToken);
            t_objLinkage.setParentLinkageType(t_objLinkType);
            this.nextToken();
        } 
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("GLYCOCTC013", this.m_iPosition);
        }
        // "(" <number> "+" <number> ")" <number> <linkagetype>
        if ( this.m_cToken != '(' )
        {
            throw new SugarImporterException("GLYCOCTC014", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken == '-' )
        {
            this.nextToken();
            if ( this.m_cToken != '1' )
            {
                throw new SugarImporterException("GLYCOCTC035", this.m_iPosition);
            }
            this.nextToken();
            t_objLinkage.addParentLinkage(Linkage.UNKNOWN_POSITION);
        }
        else
        {
            t_objLinkage.addParentLinkage(this.number());
            while( this.m_cToken == '|' )
            {
                this.nextToken();
                t_objLinkage.addParentLinkage(this.number());
            }
        }
        // "+" <number> ")" <linkagetype>
        if ( this.m_cToken != '+' )
        {
            throw new SugarImporterException("GLYCOCTC015", this.m_iPosition);
        }
        this.nextToken();
        // <number> ")" <linkagetype>
        if ( this.m_cToken == '-' )
        {
            this.nextToken();
            if ( this.m_cToken != '1' )
            {
                throw new SugarImporterException("GLYCOCTC035", this.m_iPosition);
            }
            this.nextToken();
            t_objLinkage.addChildLinkage(Linkage.UNKNOWN_POSITION);
        }
        else
        {
            t_objLinkage.addChildLinkage(this.number());
            while( this.m_cToken == '|' )
            {
                this.nextToken();
                t_objLinkage.addChildLinkage(this.number());
            }
        }
        if ( this.m_cToken != ')' )
        {
            throw new SugarImporterException("GLYCOCTC016", this.m_iPosition);
        }
        this.nextToken();
        // <linkagetype>
        try 
        {
            t_objLinkType = LinkageType.forName(this.m_cToken);
            t_objLinkage.setChildLinkageType(t_objLinkType);
            this.nextToken();
        } 
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("GLYCOCTC013", this.m_iPosition);
        }
        GlycoEdge t_objEdge = new GlycoEdge();
        t_objEdge.addGlycosidicLinkage(t_objLinkage);
        t_objUnderSubtree.setConnection(t_objEdge);
        // <zeilen_abschluss> <residue_block> <linkage_block>
        this.m_objSugarUnit = t_objUnderSubtree;
        this.zeilen_abschluss();
        this.residue_block();
        if ( this.m_cToken == 'L' )
        {
            this.linkage_block();
        }
        // add parents
        if ( t_aParents.size() == 0 )
        {
            throw new SugarImporterException("GLYCOCTC031", this.m_iPosition);
        }
         GlycoGraph t_objUnit = this.m_hashGraphs.get( t_aParents.get(0) );
         if ( t_objUnit.getClass() == SugarUnitRepeat.class )
         {
             SugarUnitRepeat t_objRepeat = (SugarUnitRepeat)t_objUnit;
             t_objRepeat.addUndeterminedSubTree(t_objUnderSubtree);
             for (Iterator<GlycoNode> t_iterParents = t_aParents.iterator(); t_iterParents.hasNext();) 
             {
                 t_objNode = t_iterParents.next();
                 if ( this.m_hashGraphs.get( t_objNode ) != t_objUnit )
                 {
                     throw new SugarImporterException("GLYCOCTC033",this.m_iPosition);
                 }
                 t_objRepeat.addUndeterminedSubTreeParent(t_objUnderSubtree,t_objNode);
             }
         }
         else if ( t_objUnit.getClass() == Sugar.class )
         {
             Sugar t_objRepeat = (Sugar)t_objUnit;
             t_objRepeat.addUndeterminedSubTree(t_objUnderSubtree);
             for (Iterator<GlycoNode> t_iterParents = t_aParents.iterator(); t_iterParents.hasNext();) 
             {
                 t_objNode = t_iterParents.next();
                 if ( this.m_hashGraphs.get( t_objNode ) != t_objUnit )
                 {
                     throw new SugarImporterException("GLYCOCTC033",this.m_iPosition);
                 }
                 t_objRepeat.addUndeterminedSubTreeParent(t_objUnderSubtree,t_objNode);
             }
         }
         else
         {
             throw new SugarImporterException("GLYCOCTC034",this.m_iPosition);
         }
    }


    /**
     * "A" "L" "T" <zeilen_abschluss> <alternative_unit> { <alternative_unit> }
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void alternative_block() throws SugarImporterException, GlycoconjugateException 
    {
        // "A" "L" "T" <zeilen_abschluss> <alternative_unit> { <alternative_unit> }
        if ( this.m_cToken != 'A' )
        {
            throw new SugarImporterException("GLYCOCTC023", this.m_iPosition);     
        }
        this.nextToken();
        if ( this.m_cToken != 'L' )
        {
            throw new SugarImporterException("GLYCOCTC023", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken != 'T' )
        {
            throw new SugarImporterException("GLYCOCTC023", this.m_iPosition);
        }
        this.nextToken();
        this.zeilen_abschluss();
        this.alternative_unit();
        while ( this.m_cToken == 'A' )
        {
            this.alternative_unit();    
        }
    }


    /**
     * "A" "L" "T" <number> <zeilen_abschluss> <alternative_graph> <alternative_graph> { <alternative_graph> }
     * @throws GlycoconjugateException 
     */
    private void alternative_unit() throws SugarImporterException, GlycoconjugateException
    {
        if ( this.m_cToken != 'A' )
        {
            throw new SugarImporterException("GLYCOCTC023", this.m_iPosition);     
        }
        this.nextToken();
        if ( this.m_cToken != 'L' )
        {
            throw new SugarImporterException("GLYCOCTC023", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken != 'T' )
        {
            throw new SugarImporterException("GLYCOCTC023", this.m_iPosition);
        }
        this.nextToken();
        int t_iAltNumber = this.number();
        SugarUnitAlternative t_objAlternative = this.m_hashAlternatives.get(t_iAltNumber);
        if ( t_objAlternative == null )
        {
            throw new SugarImporterException("GLYCOCTC024", this.m_iPosition);
        }
        this.zeilen_abschluss();
        this.alternative_graph(t_objAlternative);
        this.alternative_graph(t_objAlternative);
        while( this.m_cToken != '$' && this.m_cToken != 'U' )
        {
            this.alternative_graph(t_objAlternative);
        }
    }


    /**
     * "L" "E" "A" "D" "-" "I" "N" " " "R" "E" "S" ":" <number> <zeilen_abschluss> "L" "E" "A" "D" "-" "O" "U" "T" " " "R" "E" "S" ":" { <number>+<number> "|" } <residue_block> <linkage_block>
     * 
     * @param a_objAltUnit 
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     * 
     */
    private void alternative_graph(SugarUnitAlternative a_objAltUnit) throws SugarImporterException, GlycoconjugateException
    {
        String t_strTemp = "";
        // "ALTSUBGRAPH" <number> <zeilen_abschluss>
        for (int t_iCounter = 0; t_iCounter < 11; t_iCounter++) 
        {
            t_strTemp += this.m_cToken;
            this.nextToken();
        }
        if ( !t_strTemp.equals("ALTSUBGRAPH") )
        {
            throw new SugarImporterException("GLYCOCTC036", this.m_iPosition);
        }
        this.number();
        this.zeilen_abschluss();
        // "L" "E" "A" "D" "-" "I" "N" " " "R" "E" "S" ":" <number> <zeilen_abschluss> "L" "E" "A" "D" "-" "O" "U" "T" " " "R" "E" "S" ":" { <number>+<number> "|" } <residue_block> <linkage_block>
        t_strTemp = "";
        for (int t_iCounter = 0; t_iCounter < 12; t_iCounter++) 
        {
            t_strTemp += this.m_cToken;
            this.nextToken();
        }
        if ( !t_strTemp.equals("LEAD-IN RES:") )
        {
            throw new SugarImporterException("GLYCOCTC025", this.m_iPosition);
        }
        int t_iLeadIn = this.number();
        this.zeilen_abschluss();
        // "L" "E" "A" "D" "-" "O" "U" "T" " " "R" "E" "S" ":" { <number>+<number> "|" }
        t_strTemp = "";
        for (int t_iCounter = 0; t_iCounter < 13; t_iCounter++) 
        {
            t_strTemp += this.m_cToken;
            this.nextToken();
        }
        if ( !t_strTemp.equals("LEAD-OUT RES:") )
        {
            throw new SugarImporterException("GLYCOCTC026", this.m_iPosition);
        }
        HashMap<Integer,Integer> t_hLeadOUT = new HashMap<Integer,Integer>();
        int t_iDigit = (int) this.m_cToken;;
        while ( t_iDigit > 47 && t_iDigit < 58 )
        {
            int t_iFrom = this.number();
            if ( this.m_cToken != '+' )
            {
                throw new SugarImporterException("GLYCOCTC015", this.m_iPosition);     
            }
            this.nextToken();
            int t_iTo = this.number();
            if ( t_hLeadOUT.get(t_iTo) != null )
            {
                throw new SugarImporterException("GLYCOCTC027", this.m_iPosition);
            }
            t_hLeadOUT.put(t_iTo,t_iFrom);
            if ( this.m_cToken != '|' )
            {
                throw new SugarImporterException("GLYCOCTC037", this.m_iPosition);
            }
            this.nextToken();
            t_iDigit = (int) this.m_cToken;
        }
        this.zeilen_abschluss();
        // <residue_block> <linkage_block>
        GlycoGraphAlternative t_objAlternative = new GlycoGraphAlternative();
        this.m_objSugarUnit = t_objAlternative;        
        this.residue_block();
        if ( this.m_cToken == 'L' )
        {
            this.linkage_block();
        }        
        this.zeilen_abschluss();
        // finish alternative unit
        a_objAltUnit.addAlternative(t_objAlternative);
        GlycoNode t_objNode = this.m_hashResidues.get(t_iLeadIn);
        a_objAltUnit.setLeadInNode(t_objNode,t_objAlternative);
        // lead out
        for (Iterator<Integer> t_iterLead = t_hLeadOUT.keySet().iterator(); t_iterLead.hasNext();) 
        {
            Integer t_iExtern = t_iterLead.next();
            Integer t_iIntern = t_hLeadOUT.get(t_iExtern);
            GlycoNode t_objParent = this.m_hashResidues.get(t_iIntern);
            GlycoNode t_objChild = this.m_hashResidues.get(t_iExtern);
            if ( t_objParent == null || t_objChild == null )
            {
                throw new SugarImporterException("GLYCOCTC017", this.m_iPosition);
            }
            a_objAltUnit.addLeadOutNodeToNode(t_objParent,t_objAlternative,t_objChild);    
        }        
    }


    /**
     * "R" "E" "P" <zeilen_abschluss> <repeat_unit> { <repeat_unit> }
     * @throws GlycoconjugateException 
     */
    private void repeat_block() throws SugarImporterException, GlycoconjugateException
    {
        if ( this.m_cToken != 'R' )
        {
            throw new SugarImporterException("GLYCOCTC019", this.m_iPosition);     
        }
        this.nextToken();
        if ( this.m_cToken != 'E' )
        {
            throw new SugarImporterException("GLYCOCTC019", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken != 'P' )
        {
            throw new SugarImporterException("GLYCOCTC019", this.m_iPosition);
        }
        this.nextToken();
        this.zeilen_abschluss();
        this.repeat_unit();
        while ( this.m_cToken == 'R' )
        {
            this.repeat_unit();
        }
    }

    /**
     * "R" "E" "P" <number> ":" <number> <linkagetype> "(" <number> "+" <number> ")" <number> <linkagetype> "=" <number> "-" <number> <zeilen_abschluss> <residue_block> <linkage_block>
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void repeat_unit() throws SugarImporterException, GlycoconjugateException 
    {
        // "R" "E" "P" <number> ":" <number> <linkagetype> "(" <number> "+" <number> ")" <number> <linkagetype> "=" <number> "-" <number> <zeilen_abschluss> <residue_block> <linkage_block>
        if ( this.m_cToken != 'R' )
        {
            throw new SugarImporterException("GLYCOCTC019", this.m_iPosition);     
        }
        this.nextToken();
        if ( this.m_cToken != 'E' )
        {
            throw new SugarImporterException("GLYCOCTC019", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken != 'P' )
        {
            throw new SugarImporterException("GLYCOCTC019", this.m_iPosition);
        }
        this.nextToken();
        int t_iRepeatNumber = this.number();
        if ( this.m_cToken != ':' )
        {
            throw new SugarImporterException("GLYCOCTC005", this.m_iPosition);
        }
        this.nextToken();
        // <number> <linkagetype> "(" <number> "+" <number> ")" <number> <linkagetype> "=" <number> "-" <number> <zeilen_abschluss> <residue_block> <linkage_block>
        Linkage t_objLinkage = new Linkage();
        int t_iParent = this.number();
        LinkageType t_objLinkType;
        try 
        {
            t_objLinkType = LinkageType.forName(this.m_cToken);
            t_objLinkage.setParentLinkageType(t_objLinkType);
            this.nextToken();
        } 
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("GLYCOCTC013", this.m_iPosition);
        }
        // "(" <number> "+" <number> ")" <number> <linkagetype> "=" <number> "-" <number> <zeilen_abschluss> <residue_block> <linkage_block>
        if ( this.m_cToken != '(' )
        {
            throw new SugarImporterException("GLYCOCTC014", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken == '-' )
        {
            this.nextToken();
            if ( this.m_cToken != '1' )
            {
                throw new SugarImporterException("GLYCOCTC035", this.m_iPosition);
            }
            this.nextToken();
            t_objLinkage.addParentLinkage(Linkage.UNKNOWN_POSITION);
        }
        else
        {
            t_objLinkage.addParentLinkage(this.number());
            while( this.m_cToken == '|' )
            {
                this.nextToken();
                t_objLinkage.addParentLinkage(this.number());
            }
        }
        // "+" <number> ")" <number> <linkagetype> "=" <number> "-" <number> <zeilen_abschluss> <residue_block> <linkage_block>
        if ( this.m_cToken != '+' )
        {
            throw new SugarImporterException("GLYCOCTC015", this.m_iPosition);
        }
        this.nextToken();
        // <number> ")" <number> <linkagetype> "=" <number> "-" <number> <zeilen_abschluss> <residue_block> <linkage_block>
        if ( this.m_cToken == '-' )
        {
            this.nextToken();
            if ( this.m_cToken != '1' )
            {
                throw new SugarImporterException("GLYCOCTC035", this.m_iPosition);
            }
            this.nextToken();
            t_objLinkage.addChildLinkage(Linkage.UNKNOWN_POSITION);
        }
        else
        {
            t_objLinkage.addChildLinkage(this.number());
            while( this.m_cToken == '|' )
            {
                this.nextToken();
                t_objLinkage.addChildLinkage(this.number());
            }
        }
        if ( this.m_cToken != ')' )
        {
            throw new SugarImporterException("GLYCOCTC016", this.m_iPosition);
        }
        this.nextToken();
        // <number> <linkagetype> "=" <number> "-" <number> <zeilen_abschluss> <residue_block> <linkage_block>
        int t_iChild = this.number();
        try 
        {
            t_objLinkType = LinkageType.forName(this.m_cToken);
            t_objLinkage.setChildLinkageType(t_objLinkType);
            this.nextToken();
        } 
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("GLYCOCTC013", this.m_iPosition);
        }
        // "=" <number> "-" <number> <zeilen_abschluss> <residue_block> <linkage_block>
        if ( this.m_cToken != '=' )
        {
            throw new SugarImporterException("GLYCOCTC020", this.m_iPosition);
        }
        this.nextToken();
        int t_iMin;
        if ( this.m_cToken == '-' )
        {
            this.nextToken();
            if ( this.m_cToken != '1' )
            {
                throw new SugarImporterException("GLYCOCTC021", this.m_iPosition);
            }
            this.nextToken();
            t_iMin = SugarUnitRepeat.UNKNOWN;
        }
        else
        {
            t_iMin = this.number();
        }
        // "-" <number> <zeilen_abschluss> <residue_block> <linkage_block>
        if ( this.m_cToken != '-' )
        {
            throw new SugarImporterException("GLYCOCTC007", this.m_iPosition);
        }
        this.nextToken();
        int t_iMax;
        if ( this.m_cToken == '-' )
        {
            this.nextToken();
            if ( this.m_cToken != '1' )
            {
                throw new SugarImporterException("GLYCOCTC021", this.m_iPosition);
            }
            this.nextToken();
            t_iMax = SugarUnitRepeat.UNKNOWN;
        }
        else
        {
            t_iMax = this.number();
        }
        // <zeilen_abschluss> <residue_block> <linkage_block>
        this.zeilen_abschluss();
        SugarUnitRepeat t_objRepeat = this.m_hashRepeats.get(t_iRepeatNumber);
        if ( t_objRepeat == null )
        {
            throw new SugarImporterException("GLYCOCTC022", this.m_iPosition);
        }
        t_objRepeat.setMinRepeatCount(t_iMin);
        t_objRepeat.setMaxRepeatCount(t_iMax);
        this.m_objSugarUnit = t_objRepeat;
        this.residue_block();
        if ( this.m_cToken == 'L' )
        {
            this.linkage_block();
        }
        GlycoEdge t_objEdge = new GlycoEdge();
        t_objEdge.addGlycosidicLinkage(t_objLinkage);
        GlycoNode t_objParent = this.m_hashResidues.get(t_iParent);
        GlycoNode t_objChild = this.m_hashResidues.get(t_iChild);
        if ( t_objParent == null || t_objChild == null )
        {
            throw new SugarImporterException("GLYCOCTC017", this.m_iPosition);
        }
        t_objRepeat.setRepeatLinkage(t_objEdge,t_objParent,t_objChild);
    }

    /**
     * "R" "E" "S" <zeilen_abschluss> <residue_entry> <zeilen_abschluss> { <residue_entry> <zeilen_abschluss> } 
     * @throws GlycoconjugateException 
     */
    private void residue_block() throws SugarImporterException, GlycoconjugateException
    {
        if ( this.m_cToken != 'R' )
        {
            throw new SugarImporterException("GLYCOCTC002", this.m_iPosition);     
        }
        this.nextToken();
        if ( this.m_cToken != 'E' )
        {
            throw new SugarImporterException("GLYCOCTC002", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken != 'S' )
        {
            throw new SugarImporterException("GLYCOCTC002", this.m_iPosition);
        }
        this.nextToken();
        this.zeilen_abschluss();
        this.residue_entry();
        this.zeilen_abschluss();
        int t_iDigit = (int) this.m_cToken;;
        while ( t_iDigit > 47 && t_iDigit < 58 )
        {
            this.residue_entry();
            this.zeilen_abschluss();
            t_iDigit = (int) this.m_cToken;;
        }
    }



    /**
     * residue_entry        ::= <number> <residue>
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void residue_entry() throws SugarImporterException, GlycoconjugateException
    {
        int t_iNumber = this.number();
        GlycoNode t_objNode = this.residue();
        // add ms to sugar
        this.m_objSugarUnit.addNode(t_objNode);
        this.m_hashGraphs.put(t_objNode,this.m_objSugarUnit);
        if ( this.m_hashResidues.containsKey(t_iNumber) )
        {
            throw new SugarImporterException("Dupplicated residue ID.");
        }
        this.m_hashResidues.put(t_iNumber,t_objNode);
    }

    /**
     * "b" ":" <anomer> "-" <config> "-" <superclass> "-" <ring_start> ":" <ring_end>
     * | 
     * "s" <substituent>
     * 
     * @return
     * @throws GlycoconjugateException 
     */
    private GlycoNode residue() throws SugarImporterException, GlycoconjugateException
    {
        if ( this.m_cToken == 's' )
        {
            this.nextToken();
            if ( this.m_cToken != ':' )
            {
                throw new SugarImporterException("GLYCOCTC005", this.m_iPosition);
            }
            this.nextToken();
            int t_iStart = this.m_iPosition;
            this.substituent_name();
            String t_strName = this.m_strText.substring( t_iStart , this.m_iPosition ); 
            try
            {
                return new Substituent(SubstituentType.forName(t_strName));    
            } 
            catch (Exception e)
            {
                throw new SugarImporterException("GLYCOCTC004", this.m_iPosition);
            }            
        }
        else if ( this.m_cToken == 'b' )
        {
            this.nextToken();
            if ( this.m_cToken != ':' )
            {
                throw new SugarImporterException("GLYCOCTC005", this.m_iPosition);
            }
            this.nextToken();
            // anomer
            Anomer t_objAnomer;
            try
            {
                t_objAnomer = Anomer.forSymbol( this.m_cToken );
            }
            catch (Exception e) 
            {
                throw new SugarImporterException("GLYCOCTC006", this.m_iPosition);
            }
            this.nextToken();
            if ( this.m_cToken != '-' )
            {
                throw new SugarImporterException("GLYCOCTC007", this.m_iPosition);
            }
            this.nextToken();
            // configuration
            int t_iMaxPos = this.m_strText.indexOf(":",this.m_iPosition) - 7;
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
                    t_enumMod = ModificationType.forName(this.m_strText.substring( t_iStart , this.m_iPosition ));    
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
        else if ( this.m_cToken == 'a' )
        {
            this.nextToken();
            if ( this.m_cToken != ':' )
            {
                throw new SugarImporterException("GLYCOCTC005", this.m_iPosition);
            }
            this.nextToken();
            if ( this.m_cToken != 'a' )
            {
                throw new SugarImporterException("GLYCOCTC011", this.m_iPosition);
            }
            this.nextToken();
            int t_iID = this.number();
            SugarUnitAlternative t_objAlternative = new SugarUnitAlternative();
            // alternativeId
            if ( this.m_hashAlternatives.containsKey(t_iID) )
            {
                throw new SugarImporterException("Dupplicated alternative ID.");
            }
            this.m_hashAlternatives.put(t_iID,t_objAlternative);
            return t_objAlternative;
        }
        else if ( this.m_cToken == 'r' )
        {
            this.nextToken();
            if ( this.m_cToken != ':' )
            {
                throw new SugarImporterException("GLYCOCTC005", this.m_iPosition);
            }
            this.nextToken();
            if ( this.m_cToken != 'r' )
            {
                throw new SugarImporterException("GLYCOCTC012", this.m_iPosition);
            }
            this.nextToken();
            int t_iID = this.number();
            SugarUnitRepeat t_objRepeat = new SugarUnitRepeat();
            // repeat ID
            if ( this.m_hashRepeats.containsKey(t_iID) )
            {
                throw new SugarImporterException("Dupplicated repeat ID.");
            }
            this.m_hashRepeats.put(t_iID,t_objRepeat);
            return t_objRepeat;
        }
        else
        {
            throw new SugarImporterException("GLYCOCTC003", this.m_iPosition);
        }
    }


    /**
     * 
     */
    private void modification_name() throws SugarImporterException 
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

    private void substituent_name() throws SugarImporterException
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
            else if ( this.m_cToken == '-' || this.m_cToken == '_' )
            {
                this.nextToken();
                t_bNext = true;
            }
            else if ( this.m_cToken == '(' || this.m_cToken == ')' )
            {
                this.nextToken();
                t_bNext = true;
            }
        }
    }



    /**
     * "L" "I" "N" <zeilen_abschluss> { <linkage> <zeilen_abschluss> }
     * 
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     * 
     */
    private void linkage_block() throws SugarImporterException, GlycoconjugateException
    {
        if ( this.m_cToken != 'L' )
        {
            throw new SugarImporterException("GLYCOCTC001", this.m_iPosition);     
        }
        this.nextToken();
        if ( this.m_cToken != 'I' )
        {
            throw new SugarImporterException("GLYCOCTC001", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken != 'N' )
        {
            throw new SugarImporterException("GLYCOCTC001", this.m_iPosition);
        }
        this.nextToken();
        this.zeilen_abschluss();
        int t_iDigit = (int) this.m_cToken;;
        while ( t_iDigit > 47 && t_iDigit < 58 )
        {
            this.linkage();
            this.zeilen_abschluss();
            t_iDigit = (int) this.m_cToken;
        }
    }


    /**
     * <number> ":" <number> <linkagetype> "(" <number> "+" <number> ")" <number> <linkagetype>
     * @throws GlycoconjugateException 
     */
    private void linkage() throws SugarImporterException, GlycoconjugateException
    {
        Linkage t_objLinkage = new Linkage();
        // <number> ":" <number> <linkagetype> "(" <number> "+" <number> ")" <number> <linkagetype>
        int t_iLinkageNumber = this.number();
        if ( this.m_cToken != ':' )
        {
            throw new SugarImporterException("GLYCOCTC005", this.m_iPosition);
        }
        this.nextToken();
        int t_iParent = this.number();
        LinkageType t_objLinkType;
        try 
        {
            t_objLinkType = LinkageType.forName(this.m_cToken);
            t_objLinkage.setParentLinkageType(t_objLinkType);
            this.nextToken();
        } 
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("GLYCOCTC013", this.m_iPosition);
        }
        // "(" <number> "+" <number> ")" <number> <linkagetype>
        if ( this.m_cToken != '(' )
        {
            throw new SugarImporterException("GLYCOCTC014", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken == '-' )
        {
            this.nextToken();
            if ( this.m_cToken != '1' )
            {
                throw new SugarImporterException("GLYCOCTC035", this.m_iPosition);
            }
            this.nextToken();
            t_objLinkage.addParentLinkage(Linkage.UNKNOWN_POSITION);
        }
        else
        {
            t_objLinkage.addParentLinkage(this.number());
            while( this.m_cToken == '|' )
            {
                this.nextToken();
                t_objLinkage.addParentLinkage(this.number());
            }
        }
        // "+" <number> ")" <number> <linkagetype>
        if ( this.m_cToken != '+' )
        {
            throw new SugarImporterException("GLYCOCTC015", this.m_iPosition);
        }
        this.nextToken();
        // <number> ")" <number> <linkagetype>
        if ( this.m_cToken == '-' )
        {
            this.nextToken();
            if ( this.m_cToken != '1' )
            {
                throw new SugarImporterException("GLYCOCTC035", this.m_iPosition);
            }
            this.nextToken();
            t_objLinkage.addChildLinkage(Linkage.UNKNOWN_POSITION);
        }
        else
        {
            t_objLinkage.addChildLinkage(this.number());
            while( this.m_cToken == '|' )
            {
                this.nextToken();
                t_objLinkage.addChildLinkage(this.number());
            }
        }
        if ( this.m_cToken != ')' )
        {
            throw new SugarImporterException("GLYCOCTC016", this.m_iPosition);
        }
        this.nextToken();
        // <number> <linkagetype>
        int t_iChild = this.number();
        try 
        {
            t_objLinkType = LinkageType.forName(this.m_cToken);
            t_objLinkage.setChildLinkageType(t_objLinkType);
            this.nextToken();
        } 
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("GLYCOCTC013", this.m_iPosition);
        }
        GlycoNode t_objParent = this.m_hashResidues.get(t_iParent);
        GlycoNode t_objChild = this.m_hashResidues.get(t_iChild);
        if ( t_objParent == null || t_objChild == null )
        {
            throw new SugarImporterException("GLYCOCTC017", this.m_iPosition);
        }
        GlycoEdge t_objEdge = t_objChild.getParentEdge();
        if ( t_objEdge == null )
        {
            // create new edge
            t_objEdge = new GlycoEdge();
            t_objEdge.addGlycosidicLinkage(t_objLinkage);
            this.m_objSugarUnit.addEdge(t_objParent,t_objChild,t_objEdge);
        }
        else
        {
            if ( t_objEdge.getParent() != t_objParent )
            {
                throw new SugarImporterException("GLYCOCTC016", this.m_iPosition);
            }
            t_objEdge.addGlycosidicLinkage(t_objLinkage);
        }        
        this.m_hashLinkages.put(t_iLinkageNumber,t_objLinkage);
    }

    /**
     * @throws SugarImporterException 
     * 
     */
    private void zeilen_abschluss() throws SugarImporterException
    {
        if ( this.m_strLineSeparator == null )
        {
            while ( this.m_cToken == '\r' || this.m_cToken == '\n' )
            {
                this.nextToken();
            }
        }
        else
        {
            if ( this.m_cToken != '$' )
            {
                for (int t_iCounter = 0; t_iCounter < this.m_strLineSeparator.length(); t_iCounter++) 
                {
                    if ( this.m_cToken != this.m_strLineSeparator.charAt(t_iCounter) )
                    {
                        throw new SugarImporterException("GLYCOCTC045", this.m_iPosition);
                    }
                    this.nextToken();
                }
            }
        }
    }
    
    private void clear() 
    {
        this.m_objSugarUnit = null;
        this.m_hashResidues.clear(); 
        this.m_hashResidues.clear();
        this.m_hashRepeats.clear();
        this.m_hashAlternatives.clear();
        this.m_hashGraphs.clear();
        this.m_hashLinkages.clear();
        this.m_iNonNumber = 0;
    }
}