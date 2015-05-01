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
/**
* 
*/
package org.eurocarbdb.MolecularFramework.io.glycobase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;

/**
* @author rene
*
*/
public class SugarImporterGlycobase
{
    private String m_strReducingType = null;
    private ArrayList<String > m_aIncrements = new ArrayList<String>();
    private Sugar m_objSugar = null;
    private ArrayList<GlycobaseResidue> m_aResidues = new ArrayList<GlycobaseResidue>();
    private HashMap<String, GlycoNode> m_hashResidue = new HashMap<String, GlycoNode>();
    
    private String m_strParseString = null;
    private char m_cToken = 0;
    private int  m_iPosition = -1;

    protected void nextToken() throws SugarImporterException   
    {
        // next character
        m_iPosition++;
        try 
        {
            // get next token
            this.m_cToken = this.m_strParseString.charAt( this.m_iPosition );
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
     * @param string
     */
    public void setReducingType(String a_strReducingType)
    {
        this.m_strReducingType = a_strReducingType;
    }

    /**
     * @param glycobaseIncrements
     */
    public void setIncrements(ArrayList<String> a_aIncrements)
    {
        this.m_aIncrements = a_aIncrements;
    }

    /**
     * @return
     */
    public Sugar parse() throws SugarImporterException
    {
        this.clear();
        // testing 
        if ( this.m_strReducingType == null )
        {
            throw new SugarImporterException("GLYCOBASE002");
        }
        try
        {
            this.buildCoreStructure();
            for (Iterator<String> t_iterIncrements = this.m_aIncrements.iterator(); t_iterIncrements.hasNext();)
            {
                this.m_aResidues.add(this.createResidue(t_iterIncrements.next()));            
            }
            GlycobaseResidueComparator t_compResidues = new GlycobaseResidueComparator();
            Collections.sort(this.m_aResidues,t_compResidues);        
            for (Iterator<GlycobaseResidue> t_itreResidues = this.m_aResidues.iterator(); t_itreResidues.hasNext();)
            {
                this.addResidue(t_itreResidues.next());            
            }
        }
        catch (GlycoconjugateException e) 
        {
            throw new SugarImporterException("COMMON013");
        }
        return this.m_objSugar;
    }

    /**
     * @param next
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void addResidue(GlycobaseResidue a_objGlycobaseResidue) throws SugarImporterException, GlycoconjugateException 
    {
        int t_iPosition = a_objGlycobaseResidue.m_strPosition.indexOf(",");
        GlycoNode t_objParent = null;
        if ( t_iPosition == -1 )
        {
            t_objParent = this.m_hashResidue.get("");
        }
        else
        {
            String t_Str = a_objGlycobaseResidue.m_strPosition.substring(t_iPosition+1);
            t_objParent = this.m_hashResidue.get( t_Str );
        }
        if ( t_objParent == null )
        {
            throw new SugarImporterException("GLYCOBASE011");
        }
        UnvalidatedGlycoNode t_objNode = new UnvalidatedGlycoNode();
        t_objNode.setName(a_objGlycobaseResidue.m_strResidue);
        this.m_objSugar.addNode(t_objNode);
        GlycoEdge t_objEdge = new GlycoEdge();
        Linkage t_objLinkage = new Linkage();
        t_objLinkage.addChildLinkage(a_objGlycobaseResidue.m_iLinkageChild);
        t_objLinkage.addParentLinkage(a_objGlycobaseResidue.m_iLinkageParent);
        t_objEdge.addGlycosidicLinkage(t_objLinkage);
        this.m_objSugar.addEdge(t_objParent, t_objNode, t_objEdge);
        // hashmap
        this.m_hashResidue.put(a_objGlycobaseResidue.m_strPosition,t_objNode);
        // with
        if ( a_objGlycobaseResidue.m_strWith != null )
        {
            String t_strSubst = "";
            this.m_strParseString = a_objGlycobaseResidue.m_strWith.trim() + "$";
            this.m_iPosition = -1;
            this.nextToken();
            this.with();
            while ( this.m_cToken != '(' )
            {
                t_strSubst += this.m_cToken;
                this.nextToken();                
            }
            this.nextToken();
            t_iPosition = this.number();
            if ( this.m_cToken != ')' )
            {
                throw new SugarImporterException("GLYCOBASE012");
            }
            UnvalidatedGlycoNode t_objNodeSubst = new UnvalidatedGlycoNode();
            t_objNodeSubst.setName(t_strSubst.trim());
            this.m_objSugar.addNode(t_objNodeSubst);
            t_objEdge = new GlycoEdge();
            t_objLinkage = new Linkage();
            t_objLinkage.addChildLinkage(1);
            t_objLinkage.addParentLinkage(t_iPosition);
            t_objEdge.addGlycosidicLinkage(t_objLinkage);
            this.m_objSugar.addEdge(t_objNode, t_objNodeSubst, t_objEdge);
        }
    }

    /**
     * @throws SugarImporterException 
     * 
     */
    private void with() throws SugarImporterException 
    {
        if ( this.m_cToken != 'w')
        {
            throw new SugarImporterException("GLYCOBASE013");
        }
        this.nextToken();
        if ( this.m_cToken != 'i')
        {
            throw new SugarImporterException("GLYCOBASE013");
        }
        this.nextToken();
        if ( this.m_cToken != 't')
        {
            throw new SugarImporterException("GLYCOBASE013");
        }
        this.nextToken();
        if ( this.m_cToken != 'h')
        {
            throw new SugarImporterException("GLYCOBASE013");
        }
        this.nextToken();
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
    }

    /**
     * @throws GlycoconjugateException 
     * 
     */
    private void buildCoreStructure() throws GlycoconjugateException 
    {
        UnvalidatedGlycoNode t_objResidue = new UnvalidatedGlycoNode();
        t_objResidue.setName(this.m_strReducingType);
        this.m_objSugar.addNode(t_objResidue);
        this.m_hashResidue.put("", t_objResidue);
    }

    public void clear()
    {
        this.m_objSugar = new Sugar();
        this.m_aResidues.clear();
        this.m_hashResidue.clear();
    }

    /**
     * "[incr. 4,6] &beta;-D-GalNAc<i>p</i> (1-4) (acidic) with sulfate (4)"
     * @param next
     * @return
     * @throws SugarImporterException 
     */
    private GlycobaseResidue createResidue(String a_strIncrement) throws SugarImporterException
    {
        GlycobaseResidue t_objResidue = new GlycobaseResidue();
        this.m_strParseString = a_strIncrement.trim() + "$";
        this.m_iPosition = -1;
        this.nextToken();
        if ( this.m_cToken != '[' )
        {
            throw new SugarImporterException("GLYCOBASE006");
        }
        this.nextToken();
        this.increment();
        int t_iPosition = this.m_iPosition;
        this.number();
        while ( this.m_cToken == ',' )
        {
            this.nextToken();
            this.number();
        }
        t_objResidue.m_strPosition = this.m_strParseString.substring( t_iPosition, this.m_iPosition ).trim(); 
        if ( this.m_cToken != ']' )
        {
            throw new SugarImporterException("GLYCOBASE007");
        }
        this.nextToken();
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        t_objResidue.m_strResidue = this.residuename();
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        this.linkage(t_objResidue);
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        if ( this.m_cToken == '(' )
        {
            while ( this.m_cToken != ')' )
            {
                this.nextToken();
            }   
            this.nextToken();
        }
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }
        if ( this.m_cToken != '$' )
        {
            t_objResidue.m_strWith = this.m_strParseString.substring( this.m_iPosition );
        }
        return t_objResidue;
    }

    /**
     * (1-4)
     * @throws SugarImporterException 
     */
    private void linkage(GlycobaseResidue a_objResidue) throws SugarImporterException
    {
        if ( this.m_cToken != '(' )
        {
            throw new SugarImporterException("GLYCOBASE010");
        }
        this.nextToken();
        a_objResidue.m_iLinkageChild = this.number();
        if ( this.m_cToken != '-' )
        {
            throw new SugarImporterException("GLYCOBASE010");
        }
        this.nextToken();
        a_objResidue.m_iLinkageParent = this.number();
        if ( this.m_cToken != ')' )
        {
            throw new SugarImporterException("GLYCOBASE010");
        }
        this.nextToken();
    }

    /**
     * &beta;-D-GalNAc<i>p</i> 
     * @throws SugarImporterException 
     */
    private String residuename() throws SugarImporterException
    {
        String t_strResult = "";
        if ( this.m_cToken == '&' )
        {
            String t_strTemp = "";
            this.nextToken();
            if ( this.m_cToken == 'b' )
            {
                t_strTemp += this.m_cToken;
                int t_iMax = this.m_iPosition + 3;
                while ( this.m_iPosition < t_iMax )
                {
                    this.nextToken();
                    t_strTemp += this.m_cToken;
                }
                if ( !t_strTemp.equalsIgnoreCase("beta") )
                {
                    throw new SugarImporterException("GLYCOBASE008");
                }
                this.nextToken();
                if ( this.m_cToken != ';' )
                {
                    throw new SugarImporterException("GLYCOBASE008");
                }
                this.nextToken();
                t_strResult = "b";
            }
            else if ( this.m_cToken == 'a' )
            {
                t_strTemp += this.m_cToken;
                int t_iMax = this.m_iPosition + 4;
                while ( this.m_iPosition < t_iMax )
                {
                    this.nextToken();
                    t_strTemp += this.m_cToken;
                }
                if ( !t_strTemp.equalsIgnoreCase("alpha") )
                {
                    throw new SugarImporterException("GLYCOBASE008");
                }
                this.nextToken();
                if ( this.m_cToken != ';' )
                {
                    throw new SugarImporterException("GLYCOBASE008");
                }
                this.nextToken();
                t_strResult = "a";
            }
            else
            {
                throw new SugarImporterException("GLYCOBASE008");
            }                
        }
        while ( this.m_cToken != '<' && this.m_cToken != ' ' && this.m_cToken != '$' )
        {
            t_strResult += this.m_cToken;
            this.nextToken();
        }
        if ( this.m_cToken == '<' )
        {
            this.nextToken();
            if ( this.m_cToken != 'i' )
            {
                throw new SugarImporterException("GLYCOBASE009");
            }
            this.nextToken();
            if ( this.m_cToken != '>' )
            {
                throw new SugarImporterException("GLYCOBASE009");
            }
            this.nextToken();
            while ( this.m_cToken != '<' )
            {
                t_strResult += this.m_cToken;
                this.nextToken();
            }
            this.nextToken();
            if ( this.m_cToken != '/' )
            {
                throw new SugarImporterException("GLYCOBASE009");
            }
            this.nextToken();
            if ( this.m_cToken != 'i' )
            {
                throw new SugarImporterException("GLYCOBASE009");
            }
            this.nextToken();
            if ( this.m_cToken != '>' )
            {
                throw new SugarImporterException("GLYCOBASE009");
            }
            this.nextToken();
            while ( this.m_cToken != ' ' && this.m_cToken != '$' )
            {
                t_strResult += this.m_cToken;
                this.nextToken();
            }
        }
        return t_strResult;
    }

    /**
     * incr. 
     * @throws SugarImporterException 
     */
    private void increment() throws SugarImporterException
    {
        if ( this.m_cToken != 'i' )
        {
            throw new SugarImporterException("GLYCOBASE007");
        }
        this.nextToken();
        if ( this.m_cToken != 'n' )
        {
            throw new SugarImporterException("GLYCOBASE007");
        }
        this.nextToken();
        if ( this.m_cToken != 'c' )
        {
            throw new SugarImporterException("GLYCOBASE007");
        }
        this.nextToken();
        if ( this.m_cToken != 'r' )
        {
            throw new SugarImporterException("GLYCOBASE007");
        }
        this.nextToken();
        if ( this.m_cToken != '.' )
        {
            throw new SugarImporterException("GLYCOBASE007");
        }
        this.nextToken();
        while ( this.m_cToken == ' ' )
        {
            this.nextToken();
        }        
    }

}
