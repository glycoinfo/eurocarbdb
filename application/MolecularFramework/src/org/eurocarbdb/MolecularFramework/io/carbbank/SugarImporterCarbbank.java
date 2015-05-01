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
package org.eurocarbdb.MolecularFramework.io.carbbank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.io.StructureSpecialInformation;
import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;


/**
*
* 
* @author rene
*
*/
public class SugarImporterCarbbank extends SugarImporter
{
    private HashMap<String,Integer> m_hashRepeatCountMin = new HashMap<String,Integer>();
    private HashMap<String,Integer> m_hashRepeatCountMax = new HashMap<String,Integer>();
    private int m_iMinRepeatCount = -2;
    private int m_iMaxRepeatCount = -2;
    private ArrayList<StructureSpecialInformation> m_aSpecialList = new ArrayList<StructureSpecialInformation>();
    private String [] m_aLines;
    private int m_iLineCount = 0;
    private int m_iCharacterCounter = 0;
    private ArrayList<String> m_aWarnings = new ArrayList<String>(); 
    
    /* (non-Javadoc)
     * @see org.eurocarbdb.glybelfish.SugarImporter#parse(java.lang.String)
     */
    @Override
    public Sugar parse(String a_strStream) throws SugarImporterException
    {
        // reset
        this.m_aWarnings.clear();
        this.m_iCharacterCounter = 0;
        this.m_iLineCount = 0;
        this.m_objSugar = new Sugar();
        this.m_aSpecialList.clear();
        // start parsing
        int t_iLongestLine = 0;
        int t_iLineLength = 0;
        String t_strStructure = a_strStream.replace('\r',' ');
        t_strStructure = t_strStructure.replaceAll("\t","    ");
        this.m_aLines = t_strStructure.split("\n");
        // find longest String
        this.m_iLineCount = this.m_aLines.length;
        for (int i = 0 ; i < this.m_iLineCount ; i++)  
        {
            t_iLineLength = this.m_aLines[i].length();
            if ( t_iLongestLine < t_iLineLength )
            {
                t_iLongestLine = t_iLineLength;
            }
        }
        // create a complette 2D Matrix with Linecount x longestline (fill with " ")
        for (int i = 0 ; i < this.m_iLineCount ; i++)  
        {
            this.m_aLines[i] += this.createFillString( t_iLongestLine - this.m_aLines[i].length() );
        }
        // now we have to find the start point , most right non space character
        for ( int x = (t_iLongestLine - 1) ; x > -1  ; x--)  
        {
            for ( int y = 0 ; y < this.m_iLineCount ; y++)  
            {
                if ( this.m_aLines[y].charAt( x ) != ' ' )
                {
                    // found it, start parsing
                    // for cyclic and repear units, it starts with "-" "(" <zahl> "-"
                    if ( this.m_aLines[y].charAt( x ) == '-' )
                    {
                        if ( x < 4 )
                        {
                            throw new SugarImporterException("IUPAC2D000", x , y );
                        }
                        // "-" "(" <zahl> 
                        if ( this.isCyclic() )
                        {
                            // cyclic
                            Linkage t_objLinkage = new Linkage();
                            StructureSpecialInformation t_objSpezialInfo = new StructureSpecialInformation(null,t_objLinkage,null,this.m_objSugar);
                            this.m_aSpecialList.add(t_objSpezialInfo);
                            this.parseLeftLinkage(x,y,null,null,t_objLinkage,t_objSpezialInfo,this.m_objSugar);
                        }
                        else
                        {
                            // repeat
                            SugarUnitRepeat t_objRepeat = new SugarUnitRepeat();
                            try 
                            {
                                this.m_objSugar.addNode(t_objRepeat);
                            } 
                            catch (GlycoconjugateException e) 
                            {
                                throw new SugarImporterException("COMMON013", x , y );
                            }
                            int t_iMin = this.m_iMinRepeatCount;
                            int t_iMax = this.m_iMaxRepeatCount;
                            if ( t_iMax == -2 )
                            {
                                t_iMax = SugarUnitRepeat.UNKNOWN;
                                this.m_aWarnings.add("max. number of repeat interval for outer repeat not set");
                            }
                            if ( t_iMin == -2 )
                            {
                                t_iMin = SugarUnitRepeat.UNKNOWN;
                                this.m_aWarnings.add("min. number of repeat interval for outer repeat not set");
                            }
                            t_objRepeat.setMaxRepeatCount( this.m_iMaxRepeatCount );
                            t_objRepeat.setMinRepeatCount( this.m_iMinRepeatCount );
                            Linkage t_objLinkage = new Linkage();
                            StructureSpecialInformation t_objSpezialInfo = new StructureSpecialInformation(null,t_objLinkage,t_iMin,t_iMax,t_objRepeat,null,this.m_objSugar);
                            this.m_aSpecialList.add(t_objSpezialInfo);
                            this.parseLeftLinkage(x,y,null,null,t_objLinkage,t_objSpezialInfo,t_objRepeat);
                        }
                    }
                    else
                    {
                        // parent, linkage, speciealinfo, graph, start residue muss noch gesetzt werden?
                        this.lookAround( x , y , null , null , null, this.m_objSugar ,false , "");
                    }
                    x = -1;
                    y = this.m_iLineCount;
                }
            }
        }
        // we are finished
        // haben wir wirklich alles gefunden?
        int t_iChars = 0;
        for ( int y = 0 ; y < this.m_iLineCount ; y++)  
        {
            for (int x = 0; x < t_iLongestLine; x++) 
            {
                if ( this.m_aLines[y].charAt(x) != ' ' )
                {
                    t_iChars++;
                }
            }
        }
        if ( t_iChars != this.m_iCharacterCounter )
        {
            throw new SugarImporterException("IUPAC2D011", -1 , -1 );
        }                    
        // all spezial units closed?
        for (Iterator<StructureSpecialInformation> t_iterSpezials = this.m_aSpecialList.iterator(); t_iterSpezials.hasNext();) 
        {
            if ( !t_iterSpezials.next().isClosed() )
            {
                  throw new SugarImporterException("IUPAC2D012", -1 , -1 );
            }            
        }
        return this.m_objSugar;
    }

    /**
     * The Coordinate describe the most right charakter of the monosaccharid (part of the name)
     * 
     * @param a_iPosX  
     * @param a_iPosY
     */
    private void lookAround(int a_iPosX, int a_iPosY, GlycoNode a_objParent, Linkage a_objParentLinkage, StructureSpecialInformation a_objSpezialinfo, GlycoGraph a_objGraph, boolean a_bLookingForTarget, String a_strResidueNamePart) throws SugarImporterException
    {
        int t_iHelper = 0;
        int t_iLine = 0;
        // first we have to find out the name of the Glycan ==> return is the position of the first Charakter of the name
        int t_iNameEnd = this.findName( a_iPosX , a_iPosY );
        this.m_iCharacterCounter += a_iPosX - t_iNameEnd + 1;
        UnvalidatedGlycoNode t_objResidue = new UnvalidatedGlycoNode();
        try
        {
            t_objResidue.setName( a_strResidueNamePart + this.m_aLines[a_iPosY].substring( t_iNameEnd , a_iPosX + 1 ) );
            // there are withespaces in the name?
            String t_strTemp = t_objResidue.getName();
            int t_iMax = t_strTemp.length();
            for (int t_iCounter = 0; t_iCounter < t_iMax; t_iCounter++) 
            {
                if ( t_strTemp.charAt(t_iCounter) == ' ' )
                {
                    this.m_iCharacterCounter--;
                }
            }
            a_objGraph.addNode( t_objResidue );
            if ( a_objParent != null && a_objParentLinkage != null )
            {
                // there is a parent and we have to add 
                GlycoEdge t_objEdge = new GlycoEdge();
                t_objEdge.addGlycosidicLinkage(a_objParentLinkage);
                a_objGraph.addEdge(a_objParent,t_objResidue,t_objEdge);
            }
            if ( a_bLookingForTarget )
            {
                a_objSpezialinfo.setTarget(t_objResidue);
            }
        } 
        catch (GlycoconjugateException e)
        {
            throw new SugarImporterException("IUPAC2D010", a_iPosX , a_iPosY );
        }
        // now we look for connected glycans
        // same line? ( 7 because there must be at least a linkage before  "-(x-y)-"
        if ( t_iNameEnd > 7 )
        {
            // ok there could be more, lets look
            if ( this.m_aLines[a_iPosY].charAt( t_iNameEnd - 1 ) != ' ' )
            {
                // follow the line/linkage to the next residue
                this.followLinkageBefore( t_iNameEnd - 1 , a_iPosY , t_objResidue , a_objSpezialinfo, a_objGraph );
            }
        }
        // above? ( there must be at least 1 lines )
        if ( a_iPosY > 0 )
        {
            t_iLine = a_iPosY - 1; 
            // there is a line above, so lets look 
            for ( t_iHelper = t_iNameEnd ; t_iHelper <= a_iPosX ; t_iHelper++)
            {
                if ( this.m_aLines[t_iLine].charAt( t_iHelper ) == '|' )
                {
                    // follow the linkage to the next residue
                    this.followLinkageAbove( t_iHelper , t_iLine , t_objResidue , a_objSpezialinfo, a_objGraph );
                }
            }
        }
        // below? ( there must be at least 1 lines )
        if ( a_iPosY < this.m_iLineCount - 2 )
        {
            t_iLine = a_iPosY + 1;
            // there is a line below, so lets look
            for ( t_iHelper = t_iNameEnd ; t_iHelper <= a_iPosX ; t_iHelper++)
            {
                if ( this.m_aLines[t_iLine].charAt( t_iHelper ) == '|' )
                {
                    // follow the linkage to the next residue
                    this.followLinkageBelow( t_iHelper , t_iLine , t_objResidue , a_objSpezialinfo, a_objGraph );
                }
            }
        }        
        // finished with this monosaccharid, lets go back
    }

    private String createFillString( int a_iCount )
    {
        String t_strResult = "";
        for (int i = 0; i < a_iCount; i++)
        {
            t_strResult += " ";
        }
        return t_strResult;
    }

    /**
     * 
     * @param a_iPosX first charakter of the Linkage
     * @throws SugarImporterException 
     */
    private void followLinkageBefore(int a_iPosX, int a_iPosY  , GlycoNode a_objParent, StructureSpecialInformation a_objSpezialinfo, GlycoGraph a_objGraph) throws SugarImporterException
    {
        int t_iX = a_iPosX;
        // first we have to parse the linkage "-" "(" <left linkage> "-" <right linkage> ")" "-"
        if ( this.m_aLines[a_iPosY].charAt(t_iX) != '-' )
        {
            throw new SugarImporterException("IUPAC2D001", t_iX , a_iPosY );
        }
        t_iX--;
        this.m_iCharacterCounter++;
        this.parseStartLinkage(t_iX,a_iPosY,a_objParent,a_objSpezialinfo,a_objGraph);
    }

    /**
     * Find a Residue above the current Residue. Start is the position there the first '|' should be.
     * 
     * @param a_iPosX
     * @param a_iPosY
     * @throws SugarImporterException 
     */
    private void followLinkageAbove(int a_iPosX, int a_iPosY , GlycoNode a_objParent, StructureSpecialInformation a_objSpezialinfo, GlycoGraph a_objGraph) throws SugarImporterException
    {
        int t_iX = a_iPosX;
        int t_iY = a_iPosY;
        // until the '+' ==> follow the '|'
        do 
        {
            if ( this.m_aLines[t_iY].charAt(t_iX) != '|' )
            {
                throw new SugarImporterException("IUPAC2D002", t_iX , t_iY );
            }
            t_iY--;
            this.m_iCharacterCounter++;
            if ( t_iY < 0 )
            {
                throw new SugarImporterException("IUPAC2D003", t_iX , t_iY );
            }
        }
        while ( this.m_aLines[t_iY].charAt(t_iX) != '+' );
        // we found the '+' ==> now horizontal parsing "-" "(" <zahl> "-" <zahl> ")" "+"
        // but we have to check the position against the size of the array
        // "-" "(" <zahl> "-" <zahl> ")" 
        if ( --t_iX < 0)
        {
            throw new SugarImporterException( "IUPAC2D004", t_iX , a_iPosY );
        }        
        this.m_iCharacterCounter++;
        this.parseStartLinkage(t_iX,t_iY,a_objParent,a_objSpezialinfo,a_objGraph);
    }

    /**
     * Find a Residue below the current Residue. Start is the position there the first '|' should be.
     * 
     * @param a_iPosX
     * @param a_iPosY
     * @throws SugarImporterException 
     */
    private void followLinkageBelow(int a_iPosX, int a_iPosY , GlycoNode a_objParent, StructureSpecialInformation a_objSpezialinfo, GlycoGraph a_objGraph ) throws SugarImporterException
    {
        int t_iX = a_iPosX;
        int t_iY = a_iPosY;
        // until the '+' ==> follow the '|'
        do 
        {
            if ( this.m_aLines[t_iY].charAt(t_iX) != '|' )
            {
                throw new SugarImporterException( "IUPAC2D002", t_iX , t_iY );
            }
            t_iY++;
            this.m_iCharacterCounter++;
            if ( t_iY >= this.m_iLineCount )
            {
                throw new SugarImporterException("IUPAC2D003", t_iX , t_iY );
            }
        }
        while ( this.m_aLines[t_iY].charAt(t_iX) != '+' );
        // we found the '+' ==> now horizontal parsing "-" "(" <zahl> "-" <zahl> ")" "+"
        // but we have to check the position against the size of the array
        // "-" "(" <zahl> "-" <zahl> ")" 
        if ( --t_iX < 0)
        {
            throw new SugarImporterException( "IUPAC2D004", t_iX , t_iY );
        }        
        this.m_iCharacterCounter++;
        this.parseStartLinkage(t_iX,t_iY,a_objParent,a_objSpezialinfo,a_objGraph);
    }

    /**
     * Methode to find the name of a residue. A-Z a-z - ? <number> , "<" ">" "." ")" "(" "/" "[" "]"
     * @param posX
     * @param posY
     * @return
     */
    private int findName(int a_iPosX, int a_iPosY)
    {
        int t_iX = a_iPosX;
        boolean t_bCharakter = false;
        char t_cSign = ' ';
        int t_iSign = 0;
        do 
        {
            t_cSign = this.m_aLines[a_iPosY].charAt(t_iX);
            t_iSign = (int)t_cSign;
            t_bCharakter = false;
            if ( t_cSign >= 'A' && t_cSign <= 'Z' )
            {
                t_bCharakter = true;
            }
            else if ( t_cSign >= 'a' && t_cSign <= 'z' ) 
            {
                t_bCharakter = true;
            }
            else if ( t_iSign > 47 && t_iSign < 58 ) 
            {
                t_bCharakter = true;
            }
            else if ( t_cSign == '?' || t_cSign == '/' ) 
            {
                t_bCharakter = true;
            }
            else if ( t_cSign == ',' || t_cSign == '.' ) 
            {
                t_bCharakter = true;
            }
            else if ( t_cSign >= '0' && t_cSign <= '9' ) 
            {
                t_bCharakter = true;
            }
            else if ( t_cSign == '<' || t_cSign == '>' ) 
            {
                t_bCharakter = true;
            }
            else if ( t_cSign == '[' || t_cSign == ']' ) 
            {
                t_bCharakter = true;
            }
            else if ( t_cSign == '(' || t_cSign == ')' ) 
            {
                t_bCharakter = true;
            }
            else if ( t_cSign == '\'' || t_cSign == ':' )
            {
                t_bCharakter = true;
            }
            else if ( t_cSign == '+' || t_cSign == '='  )
            {
                t_bCharakter = true;
            }
            else if ( t_cSign == ';' || t_cSign == '_')
            {
                t_bCharakter = true;
            }
            else if ( t_cSign == '-' ) 
            {
                if ( t_iX < 1 )
                {
                    t_bCharakter = true;    
                }
                else
                {
                    if ( this.m_aLines[a_iPosY].charAt(t_iX-1) != ')' )
                    {
                        t_bCharakter = true;
                    }
                    else
                    {
                        if ( t_iX < 2 )
                        {
                            t_bCharakter = true;
                        }
                        else
                        {
                            int t_iDigit = this.m_aLines[a_iPosY].charAt(t_iX-2);
                            if ( (t_iDigit > 47 && t_iDigit < 58) || this.m_aLines[a_iPosY].charAt(t_iX-2) == '?' )
                            {
                                if ( t_iX < 3 )
                                {
                                    t_bCharakter = true;    
                                }
                                else
                                {
                                    t_iDigit = this.m_aLines[a_iPosY].charAt(t_iX-3);
                                    if (this.m_aLines[a_iPosY].charAt(t_iX-3) == '(' )
                                    {
                                        t_bCharakter = true;
                                    }
                                    else if (t_iDigit > 47 && t_iDigit < 58)
                                    {
                                        // noch ne zahl
                                        if ( t_iX < 4 )
                                        {
                                            t_bCharakter = false;    
                                        }
                                        else
                                        {
                                            if (this.m_aLines[a_iPosY].charAt(t_iX-4) == '(' )
                                            {
                                                t_bCharakter = true;
                                            }
                                            else
                                            {
                                                t_bCharakter = false;
                                            }
                                        }                                        
                                    }
                                    else
                                    {
                                        t_bCharakter = false;
                                    }
                                }
                                
                            }
                            else if ( this.m_aLines[a_iPosY].charAt(t_iX-2) == '\'' || this.m_aLines[a_iPosY].charAt(t_iX-2) == '\"' )
                            {
                                t_bCharakter = false;
                            }
                            else
                            {
                                if ( this.m_aLines[a_iPosY].charAt(t_iX-2) == '?' 
                                    || this.m_aLines[a_iPosY].charAt(t_iX-2) == 'O' 
                                    || this.m_aLines[a_iPosY].charAt(t_iX-2) == 'N' 
                                    || this.m_aLines[a_iPosY].charAt(t_iX-2) == 'S' )
                                {
                                    if ( t_iX < 3 )
                                    {
                                        t_bCharakter = true;
                                    }
                                    else
                                    {
                                        if ( this.m_aLines[a_iPosY].charAt(t_iX-3) == '-' || 
                                                ( this.m_aLines[a_iPosY].charAt(t_iX-2) == 'O' && 
                                                  this.m_aLines[a_iPosY].charAt(t_iX-3) == '[' ))
                                        {
                                            t_bCharakter = false;
                                        }
                                        else
                                        {
                                            t_bCharakter = true;
                                        }
                                    }
                                }
                                else
                                {
                                    t_bCharakter = true;
                                }
                            }
                        }
                        
                    }
                }
                
            }
            else if ( t_cSign == ' ' && t_iX > 0 )
            {
                if ( this.m_aLines[a_iPosY].charAt(t_iX-1) != ' ' && this.m_aLines[a_iPosY].charAt(t_iX-1) != '+' && this.m_aLines[a_iPosY].charAt(t_iX-1) != '|' )
                {
                    t_bCharakter = true;
                }
            }
            t_iX--;
            // last charakter reached
            if ( t_iX == -1 )
            {
                if ( t_bCharakter )
                {
                    t_iX--;
                }
                t_bCharakter = false;
            }
        }
        while ( t_bCharakter );
        return ( t_iX + 2 );
    }
    
    /**
     * @param t_ix
     * @param t_iy
     * @throws SugarImporterException 
     */
    private void parseStartLinkage(int a_iX, int a_iY, GlycoNode a_objParent, StructureSpecialInformation a_objSpezialinfo, GlycoGraph a_objGraph) throws SugarImporterException
    {
        int t_iX = a_iX;
        int t_iY = a_iY;
        if ( this.m_aLines[t_iY].charAt(t_iX) != ')' )
        {
            throw new SugarImporterException( "IUPAC2D005", t_iX , t_iY );
        }
        // "-" "(" <zahl> "-" <zahl>
        if ( --t_iX < 0)
        {
            throw new SugarImporterException( "IUPAC2D004", t_iX , t_iY );
        }
        this.m_iCharacterCounter++;
        this.parseRightLinkage( t_iX , t_iY , a_objParent , a_objSpezialinfo , a_objGraph );
    }
    
    private void parseEndLinkage(int a_iPosX , int a_iPosY , GlycoNode a_objParent , Linkage a_objParentLinkage , StructureSpecialInformation a_objSpezial , GlycoGraph a_objGraph, String a_strChildResidueNameEnd,boolean a_bLookingForTarget) throws SugarImporterException
    {
        // "-" "("   
        if ( a_iPosX < 0)
        {
            throw new SugarImporterException( "IUPAC2D004", a_iPosX , a_iPosY );
        }        
        if ( this.m_aLines[a_iPosY].charAt(a_iPosX) != '(' )
        {
            throw new SugarImporterException( "IUPAC2D005", a_iPosX , a_iPosY );
        }
        // "-" 
        if ( --a_iPosX < 0)
        {
            throw new SugarImporterException( "IUPAC2D004", a_iPosX , a_iPosY );
        }        
        if ( this.m_aLines[a_iPosY].charAt(a_iPosX) != '-' )
        {
            throw new SugarImporterException( "IUPAC2D005", a_iPosX , a_iPosY );
        }
        a_iPosX--;
        this.m_iCharacterCounter +=2;
        this.lookAround( a_iPosX , a_iPosY , a_objParent, a_objParentLinkage, a_objSpezial , a_objGraph , a_bLookingForTarget , a_strChildResidueNameEnd );
    }
    
    /**
     * := "repeat" | "cyclic" | <number> [ "]" <character> [ "x" ] ] | "O" | "N" | "S"
     * 
     * @return number of readed characters or -1 for the end of a repeat or cyclic unit 
     */
    private void parseLeftLinkage( int a_iPosX , int a_iPosY , GlycoNode a_objParent , Linkage a_objLinkageNormal , Linkage a_objLinkageStartRepeat , StructureSpecialInformation a_objSpezial , GlycoGraph a_objGraph) throws SugarImporterException
    {
        // "-"
        int t_iX = a_iPosX - 1;
        this.m_iCharacterCounter++;
        int t_iY = a_iPosY;
        String t_strNamesRest = "";
        // remove '
        while ( this.m_aLines[a_iPosY].charAt(t_iX) == '\'' )
        {
            this.m_iCharacterCounter++;
            t_iX--;
            if ( t_iX < 0 )
            {
                throw new SugarImporterException( "IUPAC2D024", t_iX+1 , t_iY );
            }
        }
        Linkage t_objLinkage = a_objLinkageStartRepeat;
        if ( a_objLinkageStartRepeat == null )
        {
            // normal linkage not linkage start
            t_objLinkage = a_objLinkageNormal;            
        }
        try 
        {
            if ( t_iX < 1 )
            {
                throw new SugarImporterException( "IUPAC2D004", t_iX , t_iY );
            }
            if ( this.m_aLines[t_iY].charAt(t_iX) == 'O' && this.m_aLines[t_iY].charAt(t_iX-1) == '(' )
            {
                this.m_iCharacterCounter ++;
                t_iX--;
                // sauerstoffverbindung
                t_objLinkage.addChildLinkage(1);
                if ( a_objLinkageStartRepeat == null )
                {
                    this.parseEndLinkage(t_iX,t_iY, a_objParent , t_objLinkage , a_objSpezial, a_objGraph , "O" ,false);
                }
                else
                {
                    this.parseEndLinkage(t_iX,t_iY, null , null , a_objSpezial, a_objGraph , "O" ,true);
                }
                return;
            }
            if ( this.m_aLines[t_iY].charAt(t_iX) == 'N' && this.m_aLines[t_iY].charAt(t_iX-1) == '(' )
            {
                // N linkage
                this.m_iCharacterCounter++;
                t_iX--;
                UnvalidatedGlycoNode t_objNode = new UnvalidatedGlycoNode();
                t_objNode.setName("N");
                a_objGraph.addNode(t_objNode);
                t_objLinkage.addChildLinkage(1);
                if ( a_objLinkageStartRepeat == null )
                {
                    GlycoEdge t_objEdge = new GlycoEdge();
                    t_objEdge.addGlycosidicLinkage(t_objLinkage);
                    a_objGraph.addEdge(a_objParent,t_objNode,t_objEdge);
                }
                else
                {
                    a_objSpezial.setTarget(t_objNode);
                }
                // prepare vor subtree
                Linkage t_objLinkageNew = new Linkage();
                t_objLinkageNew.addParentLinkage(1);
                t_objLinkageNew.addChildLinkage(Linkage.UNKNOWN_POSITION);
                this.parseEndLinkage(t_iX , a_iPosY , t_objNode, t_objLinkageNew , a_objSpezial , a_objGraph, "",false);
                return;
            }        
            if ( this.m_aLines[t_iY].charAt(t_iX) == 'S' && this.m_aLines[t_iY].charAt(t_iX-1) == '(' )
            {
                // S linkage
                this.m_iCharacterCounter++;
                t_iX--;
                UnvalidatedGlycoNode t_objNode = new UnvalidatedGlycoNode();
                t_objNode.setName("S");
                a_objGraph.addNode(t_objNode);
                t_objLinkage.addChildLinkage(1);
                if ( a_objLinkageStartRepeat == null )
                {
                    GlycoEdge t_objEdge = new GlycoEdge();
                    t_objEdge.addGlycosidicLinkage(t_objLinkage);
                    a_objGraph.addEdge(a_objParent,t_objNode,t_objEdge);
                }
                else
                {
                    a_objSpezial.setTarget(t_objNode);
                }
                // prepare vor subtree
                Linkage t_objLinkageNew = new Linkage();
                t_objLinkageNew.addParentLinkage(1);
                t_objLinkageNew.addChildLinkage(Linkage.UNKNOWN_POSITION);
                this.parseEndLinkage(t_iX , a_iPosY , t_objNode, t_objLinkageNew , a_objSpezial , a_objGraph, "",false);
                return;
            }  
            // test for cyclic & repeat
            if ( this.m_aLines[t_iY].charAt(t_iX) == 't' || this.m_aLines[t_iY].charAt(t_iX) == 'c' )
            {
                // there must be at least 6 signs
                if ( t_iX > 4 )
                {
                    String t_strPart = this.m_aLines[t_iY].substring( t_iX - 5 , t_iX + 1 );
                    if ( t_strPart.toLowerCase().equalsIgnoreCase("repeat") )
                    {
                        if ( a_objLinkageStartRepeat != null )
                        {
                            throw new SugarImporterException( "IUPAC2D023", t_iX , t_iY );
                        }
                        // repeat end
                        if ( a_objSpezial == null )
                        {
                            throw new SugarImporterException( "IUPAC2D015", a_iPosX , a_iPosY );
                        }
                        if ( a_objSpezial.getType() != StructureSpecialInformation.REPEAT )
                        {
                            throw new SugarImporterException( "IUPAC2D015", a_iPosX , a_iPosY );
                        }
                        t_iX -= 6;
                        this.m_iCharacterCounter +=6;
                        // is one charakter before? if so, then it must be a whitespace
                        if ( t_iX > -1 )
                        {
                            if ( this.m_aLines[t_iY].charAt(t_iX) != ' ' )
                            {
                                throw new SugarImporterException( "IUPAC2D006", t_iX , t_iY );                                
                            }
                        }
                        // fill repeat
                        Linkage t_objInternal = a_objSpezial.getIncomingLinkage();
                        t_objInternal.setParentLinkages(t_objLinkage.getParentLinkages());
                        GlycoEdge t_objEdge = new GlycoEdge();
                        t_objEdge.addGlycosidicLinkage(t_objInternal);
                        SugarUnitRepeat t_objRepeat = a_objSpezial.getRepeatBlock();
                        t_objRepeat.setRepeatLinkage(t_objEdge,a_objParent,a_objSpezial.getTarget());
                        t_objRepeat.setMinRepeatCount( a_objSpezial.getRepeatCountMin() );
                        t_objRepeat.setMaxRepeatCount( a_objSpezial.getRepeatCountMax() );
                        // restore spezial 
                        if ( a_objSpezial.isClosed() )
                        {
                            throw new SugarImporterException( "IUPAC2D015", t_iX , t_iY );
                        }
                        a_objSpezial.close();
                        return;
                    }
                    if ( t_strPart.toLowerCase().equalsIgnoreCase("cyclic") )
                    {
                        if ( a_objLinkageStartRepeat != null )
                        {
                            throw new SugarImporterException( "IUPAC2D023", t_iX , t_iY );
                        }
                        // cyclic end
                        if ( a_objSpezial == null )
                        {
                            throw new SugarImporterException( "IUPAC2D016", a_iPosX , a_iPosY );
                        }
                        if ( a_objSpezial.getType() != StructureSpecialInformation.CYCLIC )
                        {
                            throw new SugarImporterException( "IUPAC2D016", a_iPosX , a_iPosY );
                        }
                        t_iX -= 6;
                        this.m_iCharacterCounter +=6;
                        // is one charakter before? if so, then it must be a whitespace
                        if ( t_iX > -1 )
                        {
                            if ( this.m_aLines[t_iY].charAt(t_iX) != ' ' )
                            {
                                throw new SugarImporterException( "IUPAC2D007", t_iX , t_iY );
                            }                        
                        }
                        // cyclic aufbauen
                        GlycoEdge t_objEdge = new GlycoEdge();
                        Linkage t_objCyclic = a_objSpezial.getIncomingLinkage();
                        t_objCyclic.setParentLinkages(t_objLinkage.getParentLinkages());
                        t_objEdge.addGlycosidicLinkage(t_objCyclic);
                        a_objGraph.addEdge(a_objParent,a_objSpezial.getTarget(),t_objEdge);
                        if ( a_objSpezial.isClosed() )
                        {
                            throw new SugarImporterException( "IUPAC2D016", t_iX , t_iY );
                        }
                        a_objSpezial.close();
                        return;
                    }
                }
            }
            // TODO 
            if ( this.m_aLines[t_iY].charAt(t_iX) == ']' )
            {
                throw new SugarImporterException( "IUPAC2D020", t_iX , t_iY );
            }
            // <number> [ "]" <character> [ "x" ] ]
            int t_iDigit = (int)this.m_aLines[t_iY].charAt(t_iX);
            boolean t_bRepeat = false;
            String t_strRepeatSymbol = "";
            if ( (t_iDigit < 48 || t_iDigit > 57) && this.m_aLines[t_iY].charAt(t_iX) != '?' )
            {
                t_bRepeat = true;
                // no number ==> repeat opening
                while ( this.m_aLines[t_iY].charAt(t_iX) != ']' )
                {
                    t_strRepeatSymbol = this.m_aLines[t_iY].charAt(t_iX) + t_strRepeatSymbol;
                    t_iX--;
                    this.m_iCharacterCounter++;
                    if ( t_iX < 1 )
                    {
                        throw new SugarImporterException( "IUPAC2D019", t_iX , t_iY );
                    }                
                }
                t_iX--;
                this.m_iCharacterCounter++;
                t_iDigit = (int)this.m_aLines[t_iY].charAt(t_iX);
            }
            // linkage position
            if ( this.m_aLines[t_iY].charAt(t_iX) == '?' )
            {
                t_objLinkage.addChildLinkage(Linkage.UNKNOWN_POSITION);
                t_iX--;
                this.m_iCharacterCounter++;
            }
            else if ( this.m_aLines[t_iY].charAt(t_iX) == 'O' )
            {
                t_objLinkage.addChildLinkage(1);
                t_iX--;
                this.m_iCharacterCounter++;
                t_strNamesRest = "O";
            }
            else
            {
                if ( t_iDigit < 48 || t_iDigit > 57 )
                {
                    throw new SugarImporterException( "IUPAC2D019", t_iX , t_iY );
                }
                int t_iNumber = 0;
                // now a normal number or number / number
                while ( t_iDigit >= 48 && t_iDigit <= 57 )
                {                
                    t_iNumber = (10 * t_iNumber) + (t_iDigit-48);
                    // is a number
                    this.m_iCharacterCounter++;
                    t_iX--;
                    if ( t_iX >= 0 )
                    {
                        t_iDigit = (int)this.m_aLines[t_iY].charAt(t_iX);
                    }
                    else
                    {
                        t_iDigit = 0;
                    }
                }
                t_objLinkage.addChildLinkage(t_iNumber);
                // one character parsed
                while( this.m_aLines[t_iY].charAt(t_iX) == '/' )
                {
                    t_iX--;
                    this.m_iCharacterCounter++;
                    if ( t_iX < 0 )
                    {
                        throw new SugarImporterException( "IUPAC2D009", t_iX+1 , t_iY );
                    }
                    t_iDigit = (int)this.m_aLines[t_iY].charAt(t_iX);
                    if ( t_iDigit < 48 || t_iDigit > 57 )
                    {
                        throw new SugarImporterException( "IUPAC2D009", t_iX , t_iY );
                    }             
                    t_iNumber = 0;
                    while ( t_iDigit >= 48 && t_iDigit <= 57 )
                    {                
                        t_iNumber = (10 * t_iNumber) + (t_iDigit-48);
                        // is a number
                        this.m_iCharacterCounter++;
                        t_iX--;
                        if ( t_iX >= 0 )
                        {
                            t_iDigit = (int)this.m_aLines[t_iY].charAt(t_iX);
                        }
                        else
                        {
                            t_iDigit = 0;
                        }
                    }
                    t_objLinkage.addChildLinkage(t_iNumber);
                }
            }
            if ( t_bRepeat )
            {
                // create new repeat
                SugarUnitRepeat t_objRepeat = new SugarUnitRepeat();
                a_objGraph.addNode(t_objRepeat);
                if ( a_objLinkageStartRepeat == null )
                {
                    GlycoEdge t_objEdge = new GlycoEdge();
                    t_objEdge.addGlycosidicLinkage(t_objLinkage);
                    a_objGraph.addEdge(a_objParent,t_objRepeat,t_objEdge);
                }
                else
                {
                    a_objSpezial.setTarget(t_objRepeat);
                }
                if ( t_strRepeatSymbol.length() > 1 )
                {
                    if ( t_strRepeatSymbol.charAt(t_strRepeatSymbol.length()-1) == 'x' )
                    {
                        t_strRepeatSymbol = t_strRepeatSymbol.substring(0,t_strRepeatSymbol.length()-1);
                    }
                }
                Integer t_iRepCountMin = this.m_hashRepeatCountMin.get(t_strRepeatSymbol);
                if ( t_iRepCountMin == null )
                {
                    t_iRepCountMin = SugarUnitRepeat.UNKNOWN;
                    this.m_aWarnings.add("min. number of repeat interval for inter repeat '" + t_strRepeatSymbol + "' not set");
                }
                t_objRepeat.setMinRepeatCount( t_iRepCountMin );
                Integer t_iRepCountMax = this.m_hashRepeatCountMax.get(t_strRepeatSymbol);
                if ( t_iRepCountMax == null )
                {
                    t_iRepCountMax = SugarUnitRepeat.UNKNOWN ;
                    this.m_aWarnings.add("max. number of repeat interval for inter repeat '" + t_strRepeatSymbol + "' not set");
                }
                t_objRepeat.setMaxRepeatCount( t_iRepCountMax );
                // new special unit
                Linkage t_objLinkageNew = new Linkage();
                t_objLinkageNew.setChildLinkages( t_objLinkage.getChildLinkages() );
                StructureSpecialInformation t_objSpezialInfo = new StructureSpecialInformation(null,t_objLinkageNew,t_iRepCountMin,t_iRepCountMax,t_objRepeat,a_objSpezial,a_objGraph);
                this.m_aSpecialList.add(t_objSpezialInfo);
                this.parseEndLinkage(t_iX,t_iY,null,null,t_objSpezialInfo,t_objRepeat,t_strNamesRest,true);            
            }
            else
            {
                if ( a_objLinkageStartRepeat == null )
                {
                    this.parseEndLinkage(t_iX,t_iY,a_objParent,t_objLinkage,a_objSpezial,a_objGraph,t_strNamesRest,false);
                }
                else
                {
                    this.parseEndLinkage(t_iX,t_iY,null,null,a_objSpezial,a_objGraph,t_strNamesRest,true);
                }
            }
            return;
        } 
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException( "COMMON013", t_iX , t_iY );
        }
    }
    
    // right linkage : <number> | "N" | <zahl> "/" <zahl>
    private void parseRightLinkage( int a_iPosX , int a_iPosY , GlycoNode a_objParent , StructureSpecialInformation a_objSpezial , GlycoGraph a_objGraph) throws SugarImporterException
    {
        int t_iX = a_iPosX;
        int t_iY = a_iPosY;
        // remove '
        while ( this.m_aLines[a_iPosY].charAt(t_iX) == '\'' )
        {
            this.m_iCharacterCounter++;
            t_iX--;
            if ( t_iX < 0 )
            {
                throw new SugarImporterException( "IUPAC2D024", t_iX+1 , t_iY );
            }
        }
        int t_iDigit = (int)this.m_aLines[a_iPosY].charAt(t_iX);
        try 
        {
            if ( t_iDigit >= 48 && t_iDigit <= 57 )
            {
                Linkage t_objLinkage = new Linkage();
                int t_iNumber = 0;
                while ( t_iDigit >= 48 && t_iDigit <= 57 )
                {                
                    t_iNumber = (10 * t_iNumber) + (t_iDigit-48);
                    // is a number
                    this.m_iCharacterCounter++;
                    t_iX--;
                    if ( t_iX < 0 )
                    {
                        t_objLinkage.addParentLinkage(t_iNumber);
                        this.parseLeftLinkage(t_iX , a_iPosY , a_objParent, t_objLinkage , null , a_objSpezial , a_objGraph);
                        return;
                    }
                    t_iDigit = (int)this.m_aLines[t_iY].charAt(t_iX);
                }
                t_objLinkage.addParentLinkage(t_iNumber);
                // one character parsed
                while( this.m_aLines[t_iY].charAt(t_iX) == '/' )
                {
                    t_iX--;
                    this.m_iCharacterCounter++;
                    if ( t_iX < 0 )
                    {
                        throw new SugarImporterException( "IUPAC2D009", t_iX+1 , t_iY );
                    }
                    t_iDigit = (int)this.m_aLines[t_iY].charAt(t_iX);
                    if ( t_iDigit < 48 || t_iDigit > 57 )
                    {
                        throw new SugarImporterException( "IUPAC2D009", t_iX , t_iY );
                    }             
                    t_iNumber = 0;
                    while ( t_iDigit >= 48 && t_iDigit <= 57 )
                    {                
                        t_iNumber = (10 * t_iNumber) + (t_iDigit-48);
                        // is a number
                        this.m_iCharacterCounter++;
                        t_iX--;
                        if ( t_iX < 0 )
                        {
                            t_objLinkage.addParentLinkage(t_iNumber);
                            this.parseLeftLinkage(t_iX , a_iPosY , a_objParent, t_objLinkage , null , a_objSpezial , a_objGraph);
                            return;
                        }
                        t_iDigit = (int)this.m_aLines[t_iY].charAt(t_iX);
                    }
                    t_objLinkage.addParentLinkage(t_iNumber);
                }
                if ( this.m_aLines[t_iY].charAt(t_iX) == '[' )
                {
                    t_iX--;
                    this.m_iCharacterCounter++;
                    if ( a_objSpezial == null )
                    {
                        throw new SugarImporterException( "IUPAC2D014", a_iPosX , a_iPosY );
                    }
                    if ( a_objSpezial.getType() != StructureSpecialInformation.REPEAT )
                    {
                        throw new SugarImporterException( "IUPAC2D014", a_iPosX , a_iPosY );
                    }
                    // fill repeat
                    Linkage t_objInternal = a_objSpezial.getIncomingLinkage();
                    t_objInternal.setParentLinkages(t_objLinkage.getParentLinkages());
                    GlycoEdge t_objEdge = new GlycoEdge();
                    t_objEdge.addGlycosidicLinkage(t_objInternal);
                    SugarUnitRepeat t_objRepeat = a_objSpezial.getRepeatBlock();
                    t_objRepeat.setRepeatLinkage(t_objEdge,a_objParent,a_objSpezial.getTarget());
                    t_objRepeat.setMinRepeatCount( a_objSpezial.getRepeatCountMin() );
                    t_objRepeat.setMaxRepeatCount( a_objSpezial.getRepeatCountMax() );
                    // restore spezial 
                    if ( a_objSpezial.isClosed() )
                    {
                        throw new SugarImporterException( "IUPAC2D021", a_iPosX , a_iPosY );
                    }
                    a_objSpezial.close();
                    this.parseLeftLinkage(t_iX , a_iPosY , t_objRepeat, t_objLinkage , null , a_objSpezial.getParentInfo() , a_objSpezial.getParentUnit() );
                    return;
                }
                this.parseLeftLinkage(t_iX , a_iPosY , a_objParent, t_objLinkage , null , a_objSpezial , a_objGraph);
                return;
            }
            else if ( this.m_aLines[t_iY].charAt(t_iX) == '?' )
            {
                t_iX--;
                this.m_iCharacterCounter++;
                Linkage t_objLinkage = new Linkage();
                t_objLinkage.addParentLinkage(Linkage.UNKNOWN_POSITION);
                if ( t_iX < 0 )
                {
                    this.parseLeftLinkage(t_iX , a_iPosY , a_objParent, t_objLinkage , null , a_objSpezial , a_objGraph);
                }
                if ( this.m_aLines[t_iY].charAt(t_iX) == '[' )
                {
                    t_iX--;
                    this.m_iCharacterCounter++;
                    if ( a_objSpezial == null )
                    {
                        throw new SugarImporterException( "IUPAC2D014", a_iPosX , a_iPosY );
                    }
                    if ( a_objSpezial.getType() != StructureSpecialInformation.REPEAT )
                    {
                        throw new SugarImporterException( "IUPAC2D014", a_iPosX , a_iPosY );
                    }
                    // fill repeat
                    Linkage t_objInternal = a_objSpezial.getIncomingLinkage();
                    t_objInternal.setParentLinkages(t_objLinkage.getParentLinkages());
                    GlycoEdge t_objEdge = new GlycoEdge();
                    t_objEdge.addGlycosidicLinkage(t_objInternal);
                    SugarUnitRepeat t_objRepeat = a_objSpezial.getRepeatBlock();
                    t_objRepeat.setRepeatLinkage(t_objEdge,a_objParent,a_objSpezial.getTarget());
                    t_objRepeat.setMinRepeatCount( a_objSpezial.getRepeatCountMin() );
                    t_objRepeat.setMaxRepeatCount( a_objSpezial.getRepeatCountMax() );
                    // restore spezial 
                    if ( a_objSpezial.isClosed() )
                    {
                        throw new SugarImporterException( "IUPAC2D021", a_iPosX , a_iPosY );
                    }
                    a_objSpezial.close();
                    this.parseLeftLinkage(t_iX , a_iPosY , t_objRepeat, t_objLinkage , null , a_objSpezial.getParentInfo() , a_objSpezial.getParentUnit() );
                    return;
                }
                this.parseLeftLinkage(t_iX , a_iPosY , a_objParent, t_objLinkage , null , a_objSpezial , a_objGraph);
                return;
            }
            else if ( this.m_aLines[t_iY].charAt(t_iX) == 'O' )
            {
                t_iX--;
                this.m_iCharacterCounter++;
                if ( a_objParent.getClass() != UnvalidatedGlycoNode.class )
                {
                    throw new SugarImporterException( "IUPAC2D013", a_iPosX , a_iPosY );
                }
                UnvalidatedGlycoNode t_objNode = (UnvalidatedGlycoNode)a_objParent;
                t_objNode.setName( "O" + t_objNode.getName() );
                Linkage t_objLinkage = new Linkage();
                t_objLinkage.addParentLinkage(1);
                if ( t_iX < 0 )
                {
                    this.parseLeftLinkage(t_iX , a_iPosY , a_objParent, t_objLinkage , null , a_objSpezial , a_objGraph);
                    return;
                }
                if ( this.m_aLines[t_iY].charAt(t_iX) == '[' )
                {
                    t_iX--;
                    this.m_iCharacterCounter++;
                    if ( a_objSpezial == null )
                    {
                        throw new SugarImporterException( "IUPAC2D014", a_iPosX , a_iPosY );
                    }
                    if ( a_objSpezial.getType() != StructureSpecialInformation.REPEAT )
                    {
                        throw new SugarImporterException( "IUPAC2D014", a_iPosX , a_iPosY );
                    }
                    // fill repeat
                    Linkage t_objInternal = a_objSpezial.getIncomingLinkage();
                    t_objInternal.setParentLinkages(t_objLinkage.getParentLinkages());
                    GlycoEdge t_objEdge = new GlycoEdge();
                    t_objEdge.addGlycosidicLinkage(t_objInternal);
                    SugarUnitRepeat t_objRepeat = a_objSpezial.getRepeatBlock();
                    t_objRepeat.setRepeatLinkage(t_objEdge,a_objParent,a_objSpezial.getTarget());
                    t_objRepeat.setMinRepeatCount( a_objSpezial.getRepeatCountMin() );
                    t_objRepeat.setMaxRepeatCount( a_objSpezial.getRepeatCountMax() );
                    // restore spezial 
                    if ( a_objSpezial.isClosed() )
                    {
                        throw new SugarImporterException( "IUPAC2D021", a_iPosX , a_iPosY );
                    }
                    a_objSpezial.close();
                    this.parseLeftLinkage(t_iX , a_iPosY , t_objRepeat, t_objLinkage , null , a_objSpezial.getParentInfo() , a_objSpezial.getParentUnit() );
                    return;
                }
                this.parseLeftLinkage(t_iX , a_iPosY , a_objParent, t_objLinkage , null , a_objSpezial , a_objGraph);
                return;
            }
            else if ( this.m_aLines[t_iY].charAt(t_iX) == 'N' )
            {
                t_iX--;
                this.m_iCharacterCounter++;
                UnvalidatedGlycoNode t_objNode = new UnvalidatedGlycoNode();
                t_objNode.setName("N");
                a_objGraph.addNode(t_objNode);
                GlycoEdge t_objEdge = new GlycoEdge();
                Linkage t_objLinkage = new Linkage();
                t_objLinkage.addParentLinkage(Linkage.UNKNOWN_POSITION);
                t_objLinkage.addChildLinkage(1);
                t_objEdge.addGlycosidicLinkage(t_objLinkage);
                a_objGraph.addEdge(a_objParent,t_objNode,t_objEdge);
                t_objLinkage = new Linkage();
                t_objLinkage.addParentLinkage(1);
                this.parseLeftLinkage(t_iX , a_iPosY , t_objNode, t_objLinkage , null , a_objSpezial , a_objGraph);
                return;
            }
            else if ( this.m_aLines[t_iY].charAt(t_iX) == 'S' )
            {
                t_iX--;
                this.m_iCharacterCounter++;
                UnvalidatedGlycoNode t_objNode = new UnvalidatedGlycoNode();
                t_objNode.setName("S");
                a_objGraph.addNode(t_objNode);
                GlycoEdge t_objEdge = new GlycoEdge();
                Linkage t_objLinkage = new Linkage();
                t_objLinkage.addParentLinkage(Linkage.UNKNOWN_POSITION);
                t_objLinkage.addChildLinkage(1);
                t_objEdge.addGlycosidicLinkage(t_objLinkage);
                a_objGraph.addEdge(a_objParent,t_objNode,t_objEdge);
                t_objLinkage = new Linkage();
                t_objLinkage.addParentLinkage(1);
                this.parseLeftLinkage(t_iX , a_iPosY , t_objNode, t_objLinkage , null , a_objSpezial , a_objGraph);
                return;
            }
            throw new SugarImporterException( "IUPAC2D008", a_iPosX , a_iPosY );
        } 
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException( "COMMON013", a_iPosX,a_iPosY);
        }
        
    }
    
    public void setMinRepeatCount(String a_strSymbol, int a_iCount)
    {
        if ( a_strSymbol == null )
        {
            this.m_iMinRepeatCount = a_iCount;
        }
        else
        {
            this.m_hashRepeatCountMin.put(a_strSymbol,a_iCount);
        }
    }
    
    public void setMaxRepeatCount(String a_strSymbol, int a_iCount)
    {
        if ( a_strSymbol == null )
        {
            this.m_iMaxRepeatCount = a_iCount;
        }
        else
        {
            this.m_hashRepeatCountMax.put(a_strSymbol,a_iCount);
        }
    }

    /**
     * @return
     */
    private boolean isCyclic() 
    {
        for (int i = 0 ; i < this.m_iLineCount ; i++)  
        {
            String t_strTemp = this.m_aLines[i].toLowerCase();
            if ( t_strTemp.indexOf("cyclic") != -1 )
            {
                return true;
            }
        }
        return false;
    }
    
    public ArrayList<String> getWarnings()
    {
        return this.m_aWarnings;
    }
    
    public void clearRepeatCounts()
    {
        this.m_hashRepeatCountMax.clear();
        this.m_hashRepeatCountMin.clear();
        this.m_iMaxRepeatCount = -2;
        this.m_iMinRepeatCount = -2;
    }
}
