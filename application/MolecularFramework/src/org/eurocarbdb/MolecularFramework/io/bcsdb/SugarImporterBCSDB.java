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
package org.eurocarbdb.MolecularFramework.io.bcsdb;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.SugarImporterText;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorReplaceName;

/**
* start            ::= "-" [ "P" "-" ] <linkage_redu> ")" <rsugarchain> "(" <linkage_nonredu> "-" [ "P" "-" ] 
*                  | "P" "-" ")" [ <sidechain> ] <sugarchain>
*                  | <sugarchain> 
*
* rsugarchain      ::= [ <sidechain> ] <residue> { <linkages> [ <sidechain> ] <residue> }  <== spezielles zusatzkriterium ;
* 
* sugarchain       ::= <residue> { <linkage> [ <sidechain> ] <residue> } [ "(" <linkage_nonredu> "-" <residue> ] <== spezielles zusatzkriterium ; 
*
* sidechain        ::= "[" [ ":" ] <side_residue> [ <sidechain_follow> ] "]"
* 
* side_residue     ::= <residue> <linkage> | "P" "-" <linkage_redu> ")"  | "S" "-" <linkage_redu> ")"
* 
* sidechain_follow ::= "," <side_residue> [ <sidechain_follow> ] 
*                  | <sidechain> <residue> <linkage> [ <sidechain_follow> ]
*                  | <residue> <linkage> [ <sidechain_follow> ]  
*                  | ":" <side_residue> [ <sidechain_follow> ]
* 
* residue          ::= <character> | '?'  { <character> | <number> | '?' | '-' }
* 
* linkage          ::= "(" <linkage_nonredu> "-" { "P" "-" } <linkage_redu> ")"
* 
* linkage_redu     ::= "?" | <number>
* 
* linkage_nonredu  ::= "?" | <number>
* 
* @author rene
*
*/
public class SugarImporterBCSDB extends SugarImporterText
{    
    private int m_iMinRepeatCount = -1;
    private int m_iMaxRepeatCount = -1;
    private HashMap<SugarUnitRepeat,GlycoNode> m_hStartResiduum = new HashMap<SugarUnitRepeat,GlycoNode>(); 
    
    /**
     * Parse a string according the gramatic of the language. Uses recursiv decent
     *  
     * @param a_strStream        String that is to parse
     * @throws ImportExeption 
     */
    public Sugar parse(String a_strStream) throws SugarImporterException 
    {
        this.m_hStartResiduum.clear();
        String[] t_aCodes = a_strStream.split(" // ");
        
        this.m_objSugar = new Sugar();
        this.m_iPosition = -1;
        // Copie string and add endsymbol
        this.m_strText = t_aCodes[0].trim() + '$';
        this.m_iLength = this.m_strText.length();
        // get first token . Error ? ==> string empty
        this.nextToken();
        this.start();
        // sug definition
        if ( t_aCodes.length > 1 )
        {
            t_aCodes = t_aCodes[1].split(";");
            for (int t_iCounter = 0; t_iCounter < t_aCodes.length; t_iCounter++) 
            {
                // comment field exists
                int t_iPos = t_aCodes[t_iCounter].indexOf("=");
                if ( t_iPos != -1 )
                {
                    String t_strSug = t_aCodes[t_iCounter].substring(0,t_iPos).trim();
                    if ( this.isReplaceString(t_strSug) )
                    {
                        // found sugar residue
                        GlycoVisitorReplaceName t_objVisitor = new GlycoVisitorReplaceName( t_strSug , t_aCodes[t_iCounter].substring(t_iPos+1).trim() );
                        try 
                        {
                            t_objVisitor.start(this.m_objSugar);
                        } 
                        catch (GlycoVisitorException e) 
                        {
                            throw new SugarImporterException("BCSDB021", this.m_iPosition);
                        }
                    }
                }
                else
                {
                    throw new SugarImporterException("BCSDB020", this.m_iPosition);
                }
            }
        }
        return this.m_objSugar;
    }

    /**
     * @param sug
     * @return
     */
    private boolean isReplaceString(String a_strString) 
    {
        if ( a_strString.equals("Sug") )
        {
            return true;
        }
        if ( a_strString.equals("Subst") )
        {
            return true;
        }
        if ( a_strString.equals("Subst1") )
        {
            return true;
        }
        if ( a_strString.equals("Subst2") )
        {
            return true;
        }
        if ( a_strString.equals("Subst3") )
        {
            return true;
        }
        if ( a_strString.equals("Subst4") )
        {
            return true;
        }
        if ( a_strString.equals("Subst5") )
        {
            return true;
        }
        if ( a_strString.equals("PEN") )
        {
            return true;
        }
        if ( a_strString.equals("HEX") )
        {
            return true;
        }
        if ( a_strString.equals("HEP") )
        {
            return true;
        }
        if ( a_strString.equals("DDHEP") )
        {
            return true;
        }
        if ( a_strString.equals("LDHEP") )
        {
            return true;
        }
        if ( a_strString.equals("OCT") )
        {
            return true;
        }
        if ( a_strString.equals("NON") )
        {
            return true;
        }
        if ( a_strString.equals("LIP") )
        {
            return true;
        }
        if ( a_strString.equals("CER") )
        {
            return true;
        }
        if ( a_strString.equals("ALK") )
        {
            return true;
        }
        return false;
    }

    protected void start() throws SugarImporterException
    {
        try
        {
            this.startparsing();
        } 
        catch (GlycoconjugateException e)
        {
            throw new SugarImporterException("COMMON013", this.m_iPosition);
        }
    }
    
    /**
     * start       ::= "-" [ "P" "-" ] <linkage_redu> ")" <rsugarchain> "(" <linkage_nonredu> "-" [ "P" "-" ] 
     *                  | "P" "-" ")" [ <sidechain> ] <sugarchain>
     *                  | <sugarchain> 
     * @throws GlycoconjugateException 
       * @see de.glycosciences.glycoconjugate.io.SugarImporterText#start()
     */
    protected void startparsing() throws SugarImporterException, GlycoconjugateException
    {
        if ( this.m_cToken == '-' )
        {
            Linkage t_objInternalLinkage = null;
            SugarUnitRepeat t_objRepeat = new SugarUnitRepeat();
            this.m_objSugar.addNode( t_objRepeat );
            Linkage t_objLinkage = null;
            BcsdbSubTree t_objTree = null;
            t_objRepeat.setMinRepeatCount( this.m_iMinRepeatCount);
            t_objRepeat.setMaxRepeatCount(this.m_iMaxRepeatCount);
            // Repeatsugar
            // "-" { "P" "-" } <linkage_redu> ")" <rsugarchain> "(" <linkage_nonredu> "-" [ "P" "-" ]
            this.nextToken();
            if ( (this.m_cToken == 'P' || this.m_cToken == 'S') && this.aheadToken(1) == '-' )
            {
                UnvalidatedGlycoNode t_objResiduum = new UnvalidatedGlycoNode();
                this.m_hStartResiduum.put(t_objRepeat,t_objResiduum);
                if ( this.m_cToken == 'S' )
                {
                    t_objResiduum.setName( "S" );
                }
                else
                {
                    t_objResiduum.setName( "P" );
                }
                t_objRepeat.addNode(t_objResiduum);
                this.nextToken();
                this.nextToken();
                t_objInternalLinkage = new Linkage();
                t_objInternalLinkage.addParentLinkage(1);
                t_objLinkage = new Linkage();
                t_objLinkage.addChildLinkage(1);
                t_objTree = new BcsdbSubTree();
                t_objTree.setGlycoNode(t_objResiduum);
                GlycoEdge t_objEdge = new GlycoEdge();
                t_objEdge.addGlycosidicLinkage(t_objLinkage);
                t_objTree.setGlycoEdge(t_objEdge);
            }
            int t_iPos = this.linkage_redu(); 
            if ( t_objInternalLinkage == null )
            {
                t_objInternalLinkage = new Linkage();
                t_objInternalLinkage.addParentLinkage(t_iPos);
            }
            else
            {
                t_objLinkage.addParentLinkage(t_iPos);
            }
            if ( this.m_cToken != ')' )
            {
                throw new SugarImporterException("BCSDB001", this.m_iPosition);
            }
            this.nextToken();
            GlycoNode t_objResidueChild = this.rsugarchain(t_objTree,t_objRepeat);
            if ( this.m_cToken != '(' )
            {
                throw new SugarImporterException("BCSDB002", this.m_iPosition);
            }
            this.nextToken();
            t_iPos = this.linkage_nonredu();
            if ( this.m_cToken != '-' )
            {
                throw new SugarImporterException("BCSDB003", this.m_iPosition);
            }
            this.nextToken();
            if ( this.m_cToken == 'P' && this.aheadToken(1) == '-' )
            {
                UnvalidatedGlycoNode t_objResiduum = new UnvalidatedGlycoNode();
                t_objResiduum.setName( "P" );
                t_objRepeat.addNode(t_objResiduum);
                this.nextToken();
                this.nextToken();
                Linkage t_objSubLinkage = new Linkage();
                t_objSubLinkage.addParentLinkage(1);
                t_objSubLinkage.addChildLinkage(t_iPos);
                GlycoEdge t_objEdge = new GlycoEdge();
                t_objEdge.addGlycosidicLinkage(t_objSubLinkage);
                t_objRepeat.addEdge(t_objResiduum,t_objResidueChild,t_objEdge);
                // finish internal repeat
                t_objInternalLinkage.addChildLinkage(1);
                t_objEdge = new GlycoEdge();
                t_objEdge.addGlycosidicLinkage(t_objInternalLinkage);
                t_objRepeat.setRepeatLinkage(t_objEdge,this.m_hStartResiduum.get(t_objRepeat),t_objResiduum);
            }
            else
            {
                t_objInternalLinkage.addChildLinkage(t_iPos);
                GlycoEdge t_objEdge = new GlycoEdge();
                t_objEdge.addGlycosidicLinkage(t_objInternalLinkage);
                t_objRepeat.setRepeatLinkage(t_objEdge,this.m_hStartResiduum.get(t_objRepeat),t_objResidueChild);
            }            
        }
        else if ( (this.m_cToken == 'P' || this.m_cToken == 'S') && this.aheadToken(1) == '-' )
        {
            // "P" "-" ")" [ <sidechain> ] <sugarchain>
            UnvalidatedGlycoNode t_objResiduum = new UnvalidatedGlycoNode();
            if ( this.m_cToken == 'S' )
            {
                t_objResiduum.setName( "S" );
            }
            else
            {
                t_objResiduum.setName( "P" );
            }
            this.nextToken();
            this.nextToken();
            int t_iLink = this.linkage_redu();
            if ( this.m_cToken != ')' )
            {
                throw new SugarImporterException("BCSDB007", this.m_iPosition);
            }
            this.nextToken();
            // subtrees speichern
            ArrayList<BcsdbSubTree> t_aSubtree= new ArrayList<BcsdbSubTree>();
            GlycoEdge t_objEdge = new GlycoEdge();
            Linkage t_objLinkage = new Linkage();
            t_objLinkage.addChildLinkage(1);
            t_objLinkage.addParentLinkage(t_iLink);
            t_objEdge.addGlycosidicLinkage(t_objLinkage);
            // add residue  
            this.m_objSugar.addNode(t_objResiduum);
            BcsdbSubTree t_objTree = new BcsdbSubTree();
            t_objTree.setGlycoEdge(t_objEdge);
            t_objTree.setGlycoNode(t_objResiduum);
            t_aSubtree.add(t_objTree);
            if ( this.m_cToken == '[' )
            {
                // parse sidechain
                this.sidechain(t_aSubtree,this.m_objSugar); 
            }
            this.sugarchain( t_aSubtree , this.m_objSugar);
        }
        else
        {
            // Nonrepeatsugar
            // <sugarchain>
            this.sugarchain( new ArrayList<BcsdbSubTree>() , this.m_objSugar );
        }
        if ( ! this.finished() )
        {
            throw new SugarImporterException("BCSDB004", this.m_iPosition);
        }
    }

    /**
     * rsugarchain ::= [ <sidecharin> ] <residue> { <linkages> [ "[" <sidechain> "]" ] <residue> }  <== spezielles zusatzkriterium ;
     * 
     * @param a_objSubLinkage sublinkage, can be null
     * @return letztes Residuem
     * @throws GlycoconjugateException 
     */
    private GlycoNode rsugarchain(BcsdbSubTree a_objSubTree,SugarUnitRepeat a_objRepeat) throws SugarImporterException, GlycoconjugateException
    {
        GlycoNode t_objResiduum;
        BcsdbSubTree t_objSubTree;
        ArrayList<BcsdbSubTree> t_aSubtrees = new ArrayList<BcsdbSubTree>();
        if ( a_objSubTree != null )
        {
            t_aSubtrees.add(a_objSubTree);
        }
        if ( this.m_cToken == '[' )
        {
            this.sidechain(t_aSubtrees,a_objRepeat);
        }
        int t_iStartPosition = this.m_iPosition;
        UnvalidatedGlycoNode t_objResiduumUn = new UnvalidatedGlycoNode();
        // save Residuename in Monosaccharid Object
        this.residue();
        t_objResiduumUn.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
        t_objResiduum = t_objResiduumUn;
        if ( !this.m_hStartResiduum.containsKey(a_objRepeat) )
        {
            this.m_hStartResiduum.put(a_objRepeat,t_objResiduum);
        }
        a_objRepeat.addNode(t_objResiduum);
        // add subresidues
        for (Iterator<BcsdbSubTree> t_iterSub = t_aSubtrees.iterator(); t_iterSub.hasNext();)
        {
            t_objSubTree = t_iterSub.next();
            a_objRepeat.addEdge(t_objResiduum,t_objSubTree.getGlycoNode(),t_objSubTree.getGlycoEdge());            
        }
        while ( this.m_cToken == '(' && this.m_strText.indexOf(')',this.m_iPosition) != -1 )
        {
            // there is one or more residues left to parse
            t_aSubtrees = new ArrayList<BcsdbSubTree>();
            // parse
            t_objSubTree = this.linkage(t_objResiduum,a_objRepeat);
            t_aSubtrees.add(t_objSubTree);
            if ( this.m_cToken == '[' )
            {
                this.sidechain(t_aSubtrees,a_objRepeat);
            }
            t_iStartPosition = this.m_iPosition;
            this.residue();
            t_objResiduumUn = new UnvalidatedGlycoNode();
            t_objResiduumUn.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
            a_objRepeat.addNode(t_objResiduumUn);
            t_objResiduum = t_objResiduumUn;
            // add subresidues
            for (Iterator<BcsdbSubTree> t_iterSub = t_aSubtrees.iterator(); t_iterSub.hasNext();)
            {
                t_objSubTree = t_iterSub.next();
                a_objRepeat.addEdge(t_objResiduum,t_objSubTree.getGlycoNode(),t_objSubTree.getGlycoEdge());            
            }
        }
        return t_objResiduum;
    }

    /**
     * sugarchain  ::= <residue> { <linkage> [ <sidechain> ] <residue> } [ "(" <linkage_nonredu> "-" <residue> ] <== spezielles zusatzkriterium ;
     * @param a_aSubLinkages    All linkages "below" this residue  
     * @throws GlycoconjugateException 
     */
    private void sugarchain(ArrayList<BcsdbSubTree> a_aSubLinkages,GlycoGraph a_objSugar) throws SugarImporterException, GlycoconjugateException
    {
        int t_iStartPosition = this.m_iPosition;
        UnvalidatedGlycoNode t_objResiduum1;
        UnvalidatedGlycoNode t_objResiduum2;
        ArrayList<BcsdbSubTree> t_aLinkages;
        // save Residuename in Monosaccharid Object
        this.residue();
        t_objResiduum1 = new UnvalidatedGlycoNode();
        t_objResiduum1.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
        a_objSugar.addNode(t_objResiduum1);
        // attache all sublinkages
        for (Iterator<BcsdbSubTree> t_iterLinkages = a_aSubLinkages.iterator(); t_iterLinkages.hasNext();)
        {
            BcsdbSubTree t_objSubLinake = t_iterLinkages.next();
            a_objSugar.addEdge(t_objResiduum1,t_objSubLinake.getGlycoNode(),t_objSubLinake.getGlycoEdge());
        }
        // { <linkage> [ <sidechain> ] <residue> } [ "(" <linkage_nonredu> "-" <residue> ] <== spezielles zusatzkriterium ;
        while ( this.m_cToken == '(' && this.m_strText.indexOf(')',this.m_iPosition) != -1 )
        {
            t_aLinkages = new ArrayList<BcsdbSubTree>();
            // <linkage> [ <sidechain> ] <residue>
            BcsdbSubTree t_objSubLinkage = this.linkage(t_objResiduum1,a_objSugar);
            t_aLinkages.add(t_objSubLinkage);
            if ( this.m_cToken == '[' )
            {
                this.sidechain( t_aLinkages , a_objSugar ); 
            }
            // parse residue
            t_iStartPosition = this.m_iPosition;
            this.residue();
            t_objResiduum2 = new UnvalidatedGlycoNode();
            t_objResiduum2.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
            a_objSugar.addNode(t_objResiduum2);
            // attache childs from the subchain
            for (Iterator<BcsdbSubTree> t_iterSub = t_aLinkages.iterator(); t_iterSub.hasNext();)
            {
                BcsdbSubTree t_objSub = t_iterSub.next();
                a_objSugar.addEdge(t_objResiduum2,t_objSub.getGlycoNode(),t_objSub.getGlycoEdge());
            }
            t_objResiduum1 = t_objResiduum2;
        }        
        // [ "(" <linkage_nonredu> "-" <residue> ]
        if ( this.m_cToken == '(' )
        {
            this.nextToken();
            int t_iPosition = this.linkage_nonredu();
            if ( this.m_cToken != '-' )
            {
                throw new SugarImporterException("BCSDB006", this.m_iPosition);
            }
            while ( this.m_cToken == '-' )
            {
                this.nextToken();
                t_iStartPosition = this.m_iPosition;
                this.residue();
                t_objResiduum2 = new UnvalidatedGlycoNode();
                t_objResiduum2.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
                a_objSugar.addNode(t_objResiduum2);
                // create Linkage
                Linkage t_objEndLinkage = new Linkage();
                t_objEndLinkage.addChildLinkage(t_iPosition);
                t_objEndLinkage.addParentLinkage(1);
                GlycoEdge t_objEndEdge = new GlycoEdge();
                t_objEndEdge.addGlycosidicLinkage(t_objEndLinkage);
                a_objSugar.addEdge(t_objResiduum2,t_objResiduum1,t_objEndEdge);
                t_objResiduum1 = t_objResiduum2;
            }
        }
    }

    /**
     * linkage      ::= "(" <linkage_nonredu> "-" { "P" "-" } <linkage_redu> ")"
     * 
     * Bastelt das übergebene Residue in die Linkage ein und gibt diese dann zurück. Residuem wird NICHT in den Zucker eingefügt.
     * 
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private BcsdbSubTree linkage(GlycoNode a_objChild, GlycoGraph a_objSugar) throws SugarImporterException, GlycoconjugateException
    {
        Linkage t_objLinkage = new Linkage();
        GlycoEdge t_objEdge = new GlycoEdge();
        GlycoNode t_objChildResidue = a_objChild;
        if ( this.m_cToken != '(' )
        {
            throw new SugarImporterException("BCSDB005", this.m_iPosition);
        }
        this.nextToken();
        int t_iPosi = this.linkage_nonredu();
        // fill data in
        t_objLinkage.addChildLinkage(t_iPosi);
        if ( this.m_cToken != '-' )
        {
            throw new SugarImporterException("BCSDB006", this.m_iPosition);
        }
        this.nextToken();
        while ( this.m_cToken == 'P' && this.aheadToken(1) == '-')
        {
            UnvalidatedGlycoNode t_objResiduum = new UnvalidatedGlycoNode();
            t_objResiduum.setName( "P" );
            a_objSugar.addNode(t_objResiduum);
            t_objLinkage.addParentLinkage(1);
            t_objEdge.addGlycosidicLinkage(t_objLinkage);
            a_objSugar.addEdge(t_objResiduum,t_objChildResidue,t_objEdge);
            // create new Linkage
            t_objLinkage = new Linkage();
            t_objEdge = new GlycoEdge();
            t_objLinkage.addChildLinkage(1);
            t_objChildResidue = t_objResiduum;
            this.nextToken();
            if (this.m_cToken != '-' )
            {
                throw new SugarImporterException("BCSDB010", this.m_iPosition);
            }
            this.nextToken();
        }
        t_iPosi = this.linkage_redu();
        t_objLinkage.addParentLinkage(t_iPosi);
        if ( this.m_cToken != ')' )
        {
            throw new SugarImporterException("BCSDB007", this.m_iPosition);
        }
        this.nextToken();
        BcsdbSubTree t_objSubTree = new BcsdbSubTree();
        t_objSubTree.setGlycoNode(t_objChildResidue);
        t_objEdge.addGlycosidicLinkage(t_objLinkage);
        t_objSubTree.setGlycoEdge(t_objEdge);
        return t_objSubTree;
    }

    /**
     * @throws SugarImporterException 
     * 
     */
    private int linkage_nonredu() throws SugarImporterException
    {
        if ( this.m_cToken == '?' )
        {
            this.nextToken();
            return Linkage.UNKNOWN_POSITION;
        }
        else
        {
            return this.number();            
        }
    }

    /**
     * linkage_redu     ::= "?" | <number>
     * @throws SugarImporterException 
     */
    private int linkage_redu() throws SugarImporterException
    {
        if ( this.m_cToken == '?' )
        {
            this.nextToken();
            return Linkage.UNKNOWN_POSITION;
        }
        else
        {
            return this.number();            
        }
    }

    /**
     * residue          ::= <character> | '?'  { <character> | <number> | '?' | ',' | '|' | '-' | '@' | '{' | '}' |'=' }
     * @throws SugarImporterException 
     */
    private void residue() throws SugarImporterException
    {
        int t_iDigit = (int) this.m_cToken;;
        if ( this.m_cToken == '?' )
        {
            this.nextToken();
        }
        else if ( t_iDigit > 47 && t_iDigit < 58 )
        {
            this.number();
        }
        else
        {
            this.character();
        }
        while ( this.m_cToken != '$' && this.m_cToken != '(' && this.m_cToken != ']' )
        {
            t_iDigit = (int) this.m_cToken;
            if ( t_iDigit > 47 && t_iDigit < 58 )
            {
                this.number();
            }
            else if ( this.m_cToken == '?' )
            {
                this.nextToken();
            } 
            else if ( this.m_cToken == '|' )
            {
                this.nextToken();
            }
            else if ( this.m_cToken == ',' )
            {
                this.nextToken();
            }               
            else if ( this.m_cToken == '-')
            {
                this.nextToken();
            }
            else if ( this.m_cToken == '@')
            {
                this.nextToken();
            }
            else if ( this.m_cToken == '=')
            {
                this.nextToken();
            }
            else if ( this.m_cToken == '{')
            {
                this.nextToken();
            }
            else if ( this.m_cToken == '}')
            {
                this.nextToken();
            }
            else
            {
                this.character();
            }
        }
    }
    
    /**
     * muss was global ablegen um multi-valent residues zu vervollständigen 
     * 
     * sidechain        ::= "[" [ ":" ] <side_residue> [ <sidechain_follow> ] "]"
     * 
     * @param a_aSubTrees 
     * 
     * @throws GlycoconjugateException 
     */
    private void sidechain(ArrayList<BcsdbSubTree> a_aSubTrees,GlycoGraph a_objSugar) throws SugarImporterException, GlycoconjugateException
    {
        BcsdbSubTree t_objSubTree = null;
        boolean t_bMultilinked = false;
        if ( this.m_cToken != '[' )
        {
            throw new SugarImporterException("BCSDB009", this.m_iPosition);
        }
        this.nextToken();
        if ( this.m_cToken == ':' )
        {
            this.nextToken();
            t_bMultilinked = true;
        }
        // wenn multi = true dann wird das residue nicht im zucker gesetzt
        t_objSubTree = this.side_residue(t_bMultilinked,a_objSugar);
        if ( t_bMultilinked )
        {
            if ( this.m_cToken != ']' && this.m_cToken != ',' && this.m_cToken != ':' )
            {
                throw new SugarImporterException("BCSDB015", this.m_iPosition);
            }
            // multiresidue einhängen
            int t_iPosition = a_aSubTrees.size() - 1;
            if ( t_iPosition < 0 )
            {
                throw new SugarImporterException("BCSDB016", this.m_iPosition);
            }
            BcsdbSubTree t_objOrginal = a_aSubTrees.get( t_iPosition );
            if ( t_objOrginal.getGlycoNode().getClass() != UnvalidatedGlycoNode.class ||
                    t_objSubTree.getGlycoNode().getClass() != UnvalidatedGlycoNode.class )
            {
                throw new SugarImporterException("BCSDB017", this.m_iPosition);
            }
            UnvalidatedGlycoNode t_objChildOne = (UnvalidatedGlycoNode)t_objOrginal.getGlycoNode();
            UnvalidatedGlycoNode t_objChildTwo = (UnvalidatedGlycoNode)t_objSubTree.getGlycoNode();
            if ( !t_objChildOne.getName().equals( t_objChildTwo.getName() ) )
            {
                throw new SugarImporterException("BCSDB011", this.m_iPosition);
            }
            GlycoEdge t_objEdge = t_objOrginal.getGlycoEdge();
            for (Iterator<Linkage> t_iterLinkages = t_objSubTree.getGlycoEdge().getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
            {
                t_objEdge.addGlycosidicLinkage(t_iterLinkages.next());                
            }
            t_objSubTree = null;
        }
        if( this.m_cToken != ']' )
        {
            this.sidechain_follow(a_aSubTrees,t_objSubTree,a_objSugar);
        }
        else
        {
            if ( t_objSubTree != null )
            {
                a_aSubTrees.add(t_objSubTree);
            }
        }
        if ( this.m_cToken != ']' )
        {
            throw new SugarImporterException("BCSDB008", this.m_iPosition);
        }
        this.nextToken();
    }

    /**
     * 
     * wenn multi = true dann wird das residue nicht im zucker gesetzt
     * 
     * side_residue     ::= <residue> <linkage> | "P" "-" <linkage_redu> ")"  | "S" "-" <linkage_redu> ")"
     * 
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     * 
     */
    private BcsdbSubTree side_residue(boolean a_bMultilinked,GlycoGraph a_objSugar) throws SugarImporterException, GlycoconjugateException
    {
        Linkage t_objLinkage; 
        GlycoEdge t_objEdge;
        int t_iPos;
        if ( ( this.m_cToken == 'P' || this.m_cToken == 'S' ) && this.aheadToken(1) == '-' )
        {
            // "P" "-" <linkage_redu> ")" 
            int t_iResidueCounter = 0;
            UnvalidatedGlycoNode t_objResiduum = new UnvalidatedGlycoNode();
            if ( this.m_cToken == 'P' )
            {
                t_objResiduum.setName( "P" );
            }
            else
            {
                t_objResiduum.setName( "S" );
            }
            t_iResidueCounter++;
            this.nextToken();
            this.nextToken();
            if ( !a_bMultilinked )
            {
                a_objSugar.addNode(t_objResiduum);
            }            
            while( ( this.m_cToken == 'P' || this.m_cToken == 'S' ) && this.aheadToken(1) == '-' )
            {
                UnvalidatedGlycoNode t_objRes2 = new UnvalidatedGlycoNode();
                if ( this.m_cToken == 'P' )
                {
                    t_objRes2.setName( "P" );
                }
                else
                {
                    t_objRes2.setName( "S" );
                }
                this.nextToken();
                this.nextToken();
                a_objSugar.addNode(t_objRes2);
                // Edge
                t_objEdge = new GlycoEdge();
                t_objLinkage = new Linkage();
                t_objLinkage.addChildLinkage(1);
                t_objLinkage.addParentLinkage(1);
                t_objEdge.addGlycosidicLinkage(t_objLinkage);
                a_objSugar.addEdge(t_objRes2,t_objResiduum,t_objEdge);
                t_objResiduum = t_objRes2;
                t_iResidueCounter++;
            }
            if ( a_bMultilinked && t_iResidueCounter > 1 )
            {
                throw new SugarImporterException("BCSDB015", this.m_iPosition);
            }
            t_iPos = this.linkage_redu();
            // Edge
            t_objEdge = new GlycoEdge();
            t_objLinkage = new Linkage();
            t_objLinkage.addChildLinkage(1);
            t_objLinkage.addParentLinkage(t_iPos);
            t_objEdge.addGlycosidicLinkage(t_objLinkage);
            if ( this.m_cToken != ')' )
            {
                throw new SugarImporterException("BCSDB007", this.m_iPosition);
            }
            this.nextToken();
            BcsdbSubTree t_objSubTree = new BcsdbSubTree();
            t_objSubTree.setGlycoEdge(t_objEdge);
            t_objSubTree.setGlycoNode(t_objResiduum);
            return t_objSubTree;
        }
        else
        {
            // <residue> <linkage>
            int t_iStartPosition = this.m_iPosition;
            this.residue();
            UnvalidatedGlycoNode t_objResiduum = new UnvalidatedGlycoNode();
            t_objResiduum.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
            if ( !a_bMultilinked )
            {
                a_objSugar.addNode(t_objResiduum);
            }
            return this.linkage(t_objResiduum,a_objSugar);
        }        
    }

    /**
     * sidechain_follow ::= "," <side_residue> [ <sidechain_follow> ] 
     *                  | <sidechain> <residue> <linkage> [ <sidechain_follow> ]
     *                  | <residue> <linkage> [ <sidechain_follow> ]  
     *                  | ":" <side_residue> [ <sidechain_follow> ]
     * @param a_objLastLinkage 
     * @param a_aSubLinkages 
     * @throws SugarImporterException 
     * @throws GlycoconjugateException 
     */
    private void sidechain_follow(ArrayList<BcsdbSubTree> a_aSubLinkages, BcsdbSubTree a_objLastLinkage, GlycoGraph a_objSugar) throws SugarImporterException, GlycoconjugateException
    {
        BcsdbSubTree t_objSubtree = null;
        int t_iStartPosition = 0;
        if ( this.m_cToken == ',' )
        {
            // new Branch
            // "," <side_residue> [ <sidechain_follow> ]
            // add old Linkage to the SubLinkage array
            if ( a_objLastLinkage != null )
            {
                a_aSubLinkages.add(a_objLastLinkage);
            }
            // parsing new branch
            this.nextToken();
            t_objSubtree = this.side_residue(false,a_objSugar);
            if ( this.m_cToken != ']' )
            {
                this.sidechain_follow(a_aSubLinkages,t_objSubtree,a_objSugar);
            }
            else
            {
                a_aSubLinkages.add(t_objSubtree);
            }
        } 
        else if ( this.m_cToken == '[' )
        {
            // additional level of sidechain
            if ( a_objLastLinkage == null )
            {
                throw new SugarImporterException("BCSDB014", this.m_iPosition);
            }
            // <sidechain> <residue> <linkage> [ <sidechain_follow> ]
            ArrayList<BcsdbSubTree> t_aSubLinkages = new ArrayList<BcsdbSubTree>();
            t_aSubLinkages.add(a_objLastLinkage);
            this.sidechain(t_aSubLinkages,a_objSugar);
            t_iStartPosition = this.m_iPosition;
            this.residue();
            UnvalidatedGlycoNode t_objResiduum = new UnvalidatedGlycoNode();
            t_objResiduum.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
            a_objSugar.addNode(t_objResiduum);
            // attach all subresidues
            for (Iterator<BcsdbSubTree> t_iterSub = t_aSubLinkages.iterator(); t_iterSub.hasNext();)
            {
                BcsdbSubTree t_objSub = t_iterSub.next();
                a_objSugar.addEdge(t_objResiduum,t_objSub.getGlycoNode(),t_objSub.getGlycoEdge());
            }
            t_objSubtree = this.linkage(t_objResiduum,a_objSugar);
            if ( this.m_cToken != ']' )
            {
                this.sidechain_follow(a_aSubLinkages,t_objSubtree,a_objSugar);
            }
            else
            {
                a_aSubLinkages.add(t_objSubtree);
            }
        }
        else if ( this.m_cToken == ':' )
        {
            // multi linked residue
            if ( a_objLastLinkage == null )
            {
                throw new SugarImporterException("BCSDB013", this.m_iPosition);
            }
            a_aSubLinkages.add(a_objLastLinkage);
            // ":" <side_residue> [ <sidechain_follow> ]
            this.nextToken();
            t_objSubtree = this.side_residue(true,a_objSugar);
            if ( this.m_cToken != ']' && this.m_cToken != ',' && this.m_cToken != ':' )
            {
                throw new SugarImporterException("BCSDB015", this.m_iPosition);
            }
            // multiresidue einhaengen
            int t_iPosition = a_aSubLinkages.size() - 1;
            if ( t_iPosition < 0 )
            {
                throw new SugarImporterException("BCSDB016", this.m_iPosition);
            }
            BcsdbSubTree t_objOrginal = a_aSubLinkages.get( t_iPosition );
            if ( t_objOrginal.getGlycoNode().getClass() != UnvalidatedGlycoNode.class ||
                    t_objSubtree.getGlycoNode().getClass() != UnvalidatedGlycoNode.class )
            {
                throw new SugarImporterException("BCSDB017", this.m_iPosition);
            }
            UnvalidatedGlycoNode t_objChildOne = (UnvalidatedGlycoNode)t_objOrginal.getGlycoNode();
            UnvalidatedGlycoNode t_objChildTwo = (UnvalidatedGlycoNode)t_objSubtree.getGlycoNode();
            if ( !t_objChildOne.getName().equals( t_objChildTwo.getName() ) )
            {
                throw new SugarImporterException("BCSDB011", this.m_iPosition);
            }
            GlycoEdge t_objEdge = t_objOrginal.getGlycoEdge();
            for (Iterator<Linkage> t_iterLinkages = t_objSubtree.getGlycoEdge().getGlycosidicLinkages().iterator(); t_iterLinkages.hasNext();)
            {
                t_objEdge.addGlycosidicLinkage(t_iterLinkages.next());                
            }
            if ( this.m_cToken != ']' )
            {
                this.sidechain_follow(a_aSubLinkages,null,a_objSugar);
            }
        }
        else
        {
            // normal chain
            if ( a_objLastLinkage == null )
            {
                throw new SugarImporterException("BCSDB013", this.m_iPosition);
            }
            // <residue> <linkage> [ <sidechain_follow> ]
            t_iStartPosition = this.m_iPosition;
            this.residue();
            UnvalidatedGlycoNode t_objResiduum = new UnvalidatedGlycoNode();
            t_objResiduum.setName( this.m_strText.substring( t_iStartPosition , this.m_iPosition ) );
            a_objSugar.addNode(t_objResiduum);
            // attach sub residues
            a_objSugar.addEdge(t_objResiduum,a_objLastLinkage.getGlycoNode(),a_objLastLinkage.getGlycoEdge());
            t_objSubtree = this.linkage(t_objResiduum,a_objSugar);
            if ( this.m_cToken != ']' )
            {
                this.sidechain_follow(a_aSubLinkages,t_objSubtree,a_objSugar);
            }
            else
            {
                a_aSubLinkages.add(t_objSubtree);
            }
        }
    }
    
    public void setMinRepeatCount(int a_iCount)
    {
        this.m_iMinRepeatCount = a_iCount;
    }
    
    public void setMaxRepeatCount(int t_iCount)
    {
        this.m_iMaxRepeatCount = t_iCount;
    }
}
